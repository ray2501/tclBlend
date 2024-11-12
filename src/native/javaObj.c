/* 
 * javaObj.c --
 *
 *	This file implements the routines that maintain the correspondence
 *	between TclObject instances and Tcl_Obj * references.
 *
 * Copyright (c) 1997 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: javaObj.c,v 1.17 2002/12/31 20:16:27 mdejong Exp $
 */

#include "java.h"
#include "tcl_lang_CObject.h"
#include "tcl_lang_Interp.h"
#include "tcl_lang_TclList.h"
#include "tcl_lang_Util.h"
#include "tcl_lang_IdleHandler.h"
#include "tcl_lang_Notifier.h"
#include "tcl_lang_TimerHandler.h"

static void		DupJavaCmdInternalRep(Tcl_Obj *srcPtr,
			    Tcl_Obj *dupPtr);
static void		DupTclObject(Tcl_Obj *srcPtr, Tcl_Obj *destPtr);
static void		FreeJavaCmdInternalRep(Tcl_Obj *objPtr);
static void		FreeTclObject(Tcl_Obj *objPtr);
static int		SetJavaCmdFromAny(Tcl_Interp *interp, Tcl_Obj *objPtr);
static int		SetTclObject(Tcl_Interp *interp, Tcl_Obj *objPtr);
static void		UpdateTclObject(Tcl_Obj *objPtr);

static void
ThrowNullPointerException(
    JNIEnv *env,		/* Java environment pointer. */
    const char *msg)			/* Message to include in exception. */
{
    jclass nullClass = (*env)->FindClass(env,
	    "java/lang/NullPointerException");
    if (!msg) {
	msg = "Invalid CObject.";
    }
    (*env)->ThrowNew(env, nullClass, msg);
    (*env)->DeleteLocalRef(env, nullClass);
}

/*
 * TclObject type information.
 */

static Tcl_ObjType tclObjectType = {
     "TclObject",
     FreeTclObject,
     DupTclObject,
     UpdateTclObject,
     SetTclObject
};

/*
 * Pointer to old cmdType information.
 */

static Tcl_ObjType oldCmdType;
static Tcl_ObjType *cmdTypePtr = NULL;
static const Tcl_ObjType *listTypePtr = NULL;

/*
 * Mutex to serialize access to cmdTypePtr.
 */

static Tcl_Mutex cmdTypePtrLock;


/*
 *----------------------------------------------------------------------
 *
 * JavaObjInit --
 *
 *	Initialize the JavaObj module.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Registers the TclObject type and hijacks the cmdName type.
 *
 *----------------------------------------------------------------------
 */

void
JavaObjInit()
{
    /*
     * The JavaObjInit method could get called
     * from multiple threads. We only want to
     * init the object type once.
     */

    Tcl_MutexLock(&cmdTypePtrLock);

    if (cmdTypePtr == NULL) {
        Tcl_RegisterObjType(&tclObjectType);
    
        /*
         * Interpose on the "cmdName" type to preserve 
         * java objects.
         */

        cmdTypePtr = (Tcl_ObjType *) Tcl_GetObjType("cmdName");
        oldCmdType = *cmdTypePtr;
        cmdTypePtr->freeIntRepProc = FreeJavaCmdInternalRep;
        cmdTypePtr->dupIntRepProc = DupJavaCmdInternalRep;
        cmdTypePtr->setFromAnyProc = SetJavaCmdFromAny;

        /*
         * Grab a pointer to the Tcl list type.
         */

        listTypePtr = Tcl_GetObjType("list");
    }

    Tcl_MutexUnlock(&cmdTypePtrLock);
}

/*
 *----------------------------------------------------------------------
 *
 * printString --
 *
 *	Dump the string representation of an object to stdout. This
 *	function is purely for debugging purposes.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

void
printString(
    JNIEnv *env,		/* Java environment. */
    jobject object)
{
    JavaInfo* jcache = JavaGetCache();
    jstring string = (*env)->CallObjectMethod(env, object, jcache->toString);
    const char *str = (*env)->GetStringUTFChars(env, string, NULL);
    printf("toString: %x '%s'\n", (unsigned int) object, str);
    (*env)->ReleaseStringUTFChars(env, string, str);
    (*env)->DeleteLocalRef(env, string);
}

/*
 *----------------------------------------------------------------------
 *
 * DupTclObject --
 *
 *	Copy the internal rep for a TclObject.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Increments the reference count on the TclObject.  Creates a
 *	new global reference to the object.
 *
 *----------------------------------------------------------------------
 */

static void
DupTclObject(
    Tcl_Obj *srcPtr,
    Tcl_Obj *destPtr)
{
    jobject object = (jobject)(srcPtr->internalRep.twoPtrValue.ptr2);
    JNIEnv *env = JavaGetEnv();
    JavaInfo* jcache = JavaGetCache();
    jobject exception;

    /*
     * Clear pending Java exception.
     */

    exception = (*env)->ExceptionOccurred(env);
    if (exception)
        (*env)->ExceptionClear(env);

    /*
     * Add a global reference to represent the new copy.
     */

    object = (*env)->NewGlobalRef(env, object);
    destPtr->typePtr = srcPtr->typePtr;
    destPtr->internalRep.twoPtrValue.ptr2 = (void*) object;
    (*env)->CallVoidMethod(env, object, jcache->preserve);
    if ((*env)->ExceptionOccurred(env)) {
        (*env)->ExceptionDescribe(env);
	Tcl_Panic("DupTclObject : exception in TclObject._preserve()");
    }

    /*
     * Rethrow pending Java exception.
     */

    if (exception) {
        (*env)->Throw(env, exception);
        (*env)->DeleteLocalRef(env, exception);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * FreeTclObject --
 *
 *	Free the internal representation for a TclObject.
 *	This method is invoked by Tcl when a Tcl_Obj that
 *	wraps a TclObject has its ref count decremented to zero.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Decrements the reference count of the TclObject and frees the
 *	global object reference.
 *
 *----------------------------------------------------------------------
 */

static void
FreeTclObject(
    Tcl_Obj *objPtr)		/* Object to free. */
{
    jobject object = (jobject)(objPtr->internalRep.twoPtrValue.ptr2);
    JNIEnv *env = JavaGetEnv();
    JavaInfo* jcache = JavaGetCache();
    jobject exception;

    /*
     * Clear pending Java exception.
     */

    exception = (*env)->ExceptionOccurred(env);
    if (exception)
        (*env)->ExceptionClear(env);

    /*
     * Delete the global ref.
     */

    (*env)->CallVoidMethod(env, object, jcache->release);
    if ((*env)->ExceptionOccurred(env)) {
        (*env)->ExceptionDescribe(env);
	Tcl_Panic("FreeTclObject : exception in TclObject._release()");
    }
    (*env)->DeleteGlobalRef(env, object);
    objPtr->internalRep.twoPtrValue.ptr2 = NULL;

    /*
     * Rethrow pending Java exception.
     */

    if (exception) {
        (*env)->Throw(env, exception);
        (*env)->DeleteLocalRef(env, exception);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * SetTclObject --
 *
 *	No conversion to a TclObject is possible.
 *
 * Results:
 *	Always returns TCL_ERROR.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

static int
SetTclObject(
    Tcl_Interp *interp,
    Tcl_Obj *objPtr)
{
    if (interp) {
	Tcl_ResetResult(interp);
	Tcl_SetStringObj(Tcl_GetObjResult(interp),
		"cannot convert to TclObject", -1);
    }
    return TCL_ERROR;
}

/*
 *----------------------------------------------------------------------
 *
 * UpdateTclObject --
 *
 *	Retrieve the string representation from the TclObject.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Updates the string representation of the Tcl_Obj.
 *
 *----------------------------------------------------------------------
 */

static void
UpdateTclObject(Tcl_Obj *objPtr)
{
    jstring string;
    jobject object = (jobject)(objPtr->internalRep.twoPtrValue.ptr2);
    JNIEnv *env = JavaGetEnv();
    JavaInfo* jcache = JavaGetCache();
    jobject exception;

    /*
     * If object was freed already, or if the ptr2 field was
     * explicitly set to NULL, then do nothing.
     */
    if (object == NULL)
         return;
    
    /* 
     * Clear pending Java exception.
     */

    exception = (*env)->ExceptionOccurred(env);
    if (exception)
        (*env)->ExceptionClear(env);

    /*
     * Update Tcl_Obj.bytes to result of TclObject.toString() call.
     */

    string = (*env)->CallObjectMethod(env, object, jcache->toString);
    if ((*env)->ExceptionOccurred(env)) {
        (*env)->ExceptionDescribe(env);
	Tcl_Panic("UpdateTclObject : exception in TclObject.toString()");
    }
    objPtr->bytes = JavaGetString(env, string, &objPtr->length);
    (*env)->DeleteLocalRef(env, string);

    /*
     * Rethrow pending Java exception.
     */

    if (exception) {
        (*env)->Throw(env, exception);
        (*env)->DeleteLocalRef(env, exception);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * JavaGetTclObj --
 *
 *	Retrieve the Tcl_Obj that corresponds to the given Java
 *	TclObject. Creates a new Tcl_Obj of type TclObject with an internal
 *	representation that points at the Java object.
 *
 * Results:
 *	Returns the Tcl_Obj that corresponds to the TclObject.
 *
 * Side effects:
 *	Adds a reference to the TclObject.
 *
 *----------------------------------------------------------------------
 */

Tcl_Obj*
JavaGetTclObj(
    JNIEnv *env,		/* Java environment. */
    jobject object)		/* TclObject. */
{
    Tcl_Obj *objPtr;
    jlong objRef;
    JavaInfo* jcache = JavaGetCache();

    if ((*env)->ExceptionOccurred(env)) {
	(*env)->ExceptionDescribe(env);
	Tcl_Panic("JavaGetTclObj : unexpected pending exception");
    }

    objRef = (*env)->CallLongMethod(env, object, jcache->getCObjectPtr);
    if ((*env)->ExceptionOccurred(env)) {
	(*env)->ExceptionDescribe(env);
	Tcl_Panic("JavaGetTclObj : exception in TclObject.getCObjectPtr()");
    }

    if (objRef != 0) {
	/*
	 * This is either a TclList or a CObject, convert to Tcl_Obj*.
	 */

	objPtr = *(Tcl_Obj**)&objRef;

#ifdef TCL_MEM_DEBUG
	if (objPtr->refCount == 0x61616161) {
	    Tcl_Panic("JavaGetTclObj : disposed object");
	}
#endif
    } else {
	/*
	 * This object is of an unknown type so we create a new Tcl object to
	 * hold the object reference.
	 */

	object = (*env)->NewGlobalRef(env, object);
	objPtr = Tcl_NewObj();
	objPtr->bytes = NULL;
	objPtr->typePtr = &tclObjectType;
	objPtr->internalRep.twoPtrValue.ptr2 = (void*) object;

	/*
	 * Increment the reference count on the TclObject.
	 */

	(*env)->CallVoidMethod(env, object, jcache->preserve);
	if ((*env)->ExceptionOccurred(env)) {
	    (*env)->ExceptionDescribe(env);
	    Tcl_Panic("JavaGetTclObj : exception in TclObject._preserve()");
	}
    }
    return objPtr;
}

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_CObject_getString --
 *
 *	Retrieve the string representation for an object.
 *
 * Class:     tcl_lang_CObject
 * Method:    getString
 * Signature: (J)Ljava/lang/String;
 *
 * Results:
 *	Returns a new Java string.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

jstring JNICALL
Java_tcl_lang_CObject_getString(
    JNIEnv *env,		/* Java environment. */
    jclass class,		/* Handle to CObject class. */
    jlong obj)			/* Value of CObject.objPtr. */
{
    Tcl_Obj *objPtr = *(Tcl_Obj **) &obj;
    jchar *buf;
    char *str;
    jstring result;
    int length;
    char *p, *end;
#if TCL_MAJOR_VERSION < 9
    Tcl_UniChar *w;
#else
    unsigned short *w;
#endif


    if (!objPtr) {
	ThrowNullPointerException(env, NULL);
	return NULL;
    }

#ifdef TCL_MEM_DEBUG
    if (objPtr->refCount == 0x61616161) {
	Tcl_Panic("Java_tcl_lang_CObject_getString : disposed object");
    }
#endif

    /*
     * Convert the string rep into a Unicode string.
     */

    str = Tcl_GetStringFromObj(objPtr, &length);
    if (length > 0) {
        buf = (jchar*) ckalloc(length*sizeof(jchar));

	w = buf;
	end = str + length;
	for (p = str; p < end; ) {
	  /*
	  fprintf(stderr, "UTF index %d is %d -> '%c'\n",
		  ((int) (p - str)), ((int) *p), *p);
	  */

#if TCL_MAJOR_VERSION < 9
	  p += Tcl_UtfToUniChar(p, w);
#else
	  p += Tcl_UtfToChar16(p, w);
#endif
	  /*
	  if (((unsigned int) *w) > ((unsigned int) 254)) {
	    fprintf(stderr, "unicode char %d added\n", *w);
	  }
	  */
	  w++;
	}

	/*
	 * The UTF-8 encoded string length could be larger
	 * than the unicode version (in chars not bytes),
	 * so we need to set the length to the number of
	 * unicode chars that were converted from UTF-8
	 */

	length = (w - buf);
	result = (*env)->NewString(env, buf, length);
	ckfree((char*) buf);
    } else {
	result = (*env)->NewString(env, NULL, 0);
    }
    return result;
}

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_CObject_incrRefCount --
 *
 *	Increment the reference count of the given object.
 *
 * Class:     tcl_lang_CObject
 * Method:    incrRefCount
 * Signature: (J)V
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

void JNICALL
Java_tcl_lang_CObject_incrRefCount(
    JNIEnv *env,		/* Java environment. */
    jclass class,		/* Handle to CObject class. */
    jlong obj)			/* Value of CObject.objPtr. */
{
    Tcl_Obj *objPtr = *(Tcl_Obj **) &obj;

    if (!objPtr) {
	ThrowNullPointerException(env, NULL);
	return;
    }
    Tcl_IncrRefCount(objPtr);
}

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_CObject_decrRefCount --
 *
 *	Decrement the reference count for the given object.
 *
 * Class:     tcl_lang_CObject
 * Method:    decrRefCount
 * Signature: (J)V
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

void JNICALL Java_tcl_lang_CObject_decrRefCount(
    JNIEnv *env,		/* Java environment. */
    jclass class,		/* Handle to CObject class. */
    jlong obj)			/* Value of CObject.objPtr. */
{
    Tcl_Obj *objPtr = *(Tcl_Obj **) &obj;

    if (!objPtr) {
	ThrowNullPointerException(env, NULL);
	return;
    }
    Tcl_DecrRefCount(objPtr);
}

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_CObject_makeRef --
 *
 *	Convert the Tcl_Obj into a TclObject.
 *
 * Class:     tcl_lang_CObject
 * Method:    makeRef
 * Signature: (JLtcl/lang/TclObject;)V
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Modifies the internal representation of the object.
 *
 *----------------------------------------------------------------------
 */

void JNICALL
Java_tcl_lang_CObject_makeRef(
    JNIEnv *env,		/* Java environment. */
    jclass class,		/* Handle to CObject class. */
    jlong obj,			/* Value of CObject.objPtr. */
    jobject object)		/* Handle to the TclObject. */
{
    Tcl_Obj *objPtr = *(Tcl_Obj **) &obj;
    const Tcl_ObjType *oldTypePtr;
    int non_tclobject_cmd = 0;

    if (!objPtr) {
	ThrowNullPointerException(env, NULL);
	return;
    }

#ifdef TCL_MEM_DEBUG
    if (objPtr->refCount == 0x61616161) {
	Tcl_Panic("Java_tcl_lang_CObject_makeRef : disposed object");
    }
#endif

    /*
     * Free the old internalRep before setting the new one.
     * Watch for the special case of a command internal rep
     * that does not have a ref to a TclObject. We avoid
     * freeing the internal rep and add a ref in that case.
     */

    if ((objPtr->typePtr == cmdTypePtr) &&
	    (objPtr->internalRep.twoPtrValue.ptr2 == NULL)) {
	non_tclobject_cmd = 1;
    }

    oldTypePtr = objPtr->typePtr;
    if ((oldTypePtr != NULL) &&
	    (oldTypePtr->freeIntRepProc != NULL) &&
	    !non_tclobject_cmd) {
	oldTypePtr->freeIntRepProc(objPtr);
    }

    object = (*env)->NewGlobalRef(env, object);
    if (!non_tclobject_cmd)
	objPtr->typePtr = &tclObjectType;
    objPtr->internalRep.twoPtrValue.ptr2 = (void*) object;

    /*
     * Note that we don't change the TclObject ref count or
     * the Tcl_Obj ref count here. We expect that FreeTclObject
     * will be invoked when the ref count of this Tcl_Obj
     * reaches zero, and a CObject begins life with a
     * ref count of 1, so we are covered.
     */
}

/*
 *----------------------------------------------------------------------
 *
 * JavaBreakRef --
 *
 *	Check to see if a Tcl_Obj contains an invalid
 *	ref to a TclObject that has a CObject or TclList
 *	internal rep. This method breaks such a ref by
 *	setting the internal rep for the Tcl_Obj to
 *	a string or list rep.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Modifies the internal representation of the Tcl_Obj.
 *
 *----------------------------------------------------------------------
 */

void
JavaBreakRef(
    JNIEnv *env,		/* Java environment. */
    Tcl_Obj *objPtr)		/* Object to check. */
{
    jobject object;
    int isTclList, isCObject, dummy;
    int inst;
    JavaInfo* jcache = JavaGetCache();

#ifdef TCL_MEM_DEBUG
    if (objPtr->refCount == 0x61616161) {
	Tcl_Panic("JavaBreakRef : disposed object");
    }
#endif

    if ((objPtr->typePtr == &tclObjectType)
            || ((objPtr->typePtr == cmdTypePtr) &&
                    (objPtr->internalRep.twoPtrValue.ptr2) != NULL)) {
	object = (jobject)(objPtr->internalRep.twoPtrValue.ptr2);
	inst = (*env)->CallIntMethod(env, object, jcache->getCObjectInst);
	if ((*env)->ExceptionOccurred(env)) {
	    (*env)->ExceptionDescribe(env);
	    Tcl_Panic("JavaBreakRef : exception in TclObject.getCObjectInst()");
	}
	/* Constants returned by getCObjectInst() to indicate type. */
	isCObject = (inst == 1);
	isTclList = (inst == 2);

	if (isTclList) {
	    /*fprintf(stderr, "breaking ref for TclList \"%s\"\n", Tcl_GetString(objPtr));*/
	    Tcl_ListObjLength((Tcl_Interp *) NULL, objPtr, &dummy);
	} else if (isCObject) {
	    /*fprintf(stderr, "breaking ref for String \"%s\"\n", Tcl_GetString(objPtr));*/
	    Tcl_GetCharLength(objPtr);
	}
    }
}

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_CObject_newCObject --
 *
 *	Allocate a new Tcl_Obj with the given string rep.
 *
 * Class:     tcl_lang_CObject
 * Method:    newCObject
 * Signature: (Ljava/lang/String;)J
 *
 * Results:
 *	Returns the address of the new Tcl_Obj with refcount of 0.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

jlong JNICALL
Java_tcl_lang_CObject_newCObject(
    JNIEnv *env,		/* Java environment. */
    jclass class,		/* Handle to CObject class. */
    jstring string)		/* Initial string rep. */
{
    Tcl_Obj *objPtr;
    jlong obj;

    objPtr = Tcl_NewObj();
    if (string) {
	objPtr->bytes = JavaGetString(env, string, &objPtr->length);
    }
    obj = 0;
    *(Tcl_Obj **)&obj = objPtr;
    return obj;	
}

/*
 *----------------------------------------------------------------------
 *
 * JavaGetTclObject --
 *
 *	Retrieve the Java TclObject that shadows the given Tcl_Obj.
 *	Creates a new TclObject of type CObject or TclObject that refers
 *	to the given Tcl_Obj, unless the Tcl_Obj is a TclObject already.
 *
 * Results:
 *	Returns a TclObject associated with the Tcl_Obj.
 *
 * Side effects:
 *	May allocate a new local reference in the JVM. Note that
 *	if this routine is not called as a result of a native method
 *	invocation, the caller is responsible for deleting the local
 *	reference explicitly. Will increment the TclObject.refCount
 *	for a newely created CObject wrapper.
 *
 *----------------------------------------------------------------------
 */

jobject
JavaGetTclObject(
    JNIEnv *env,
    Tcl_Obj *objPtr,		/* Object to get jobject for. */
    int *isLocalPtr)		/* 1 if returned handle is a local ref. */
{
    jobject object;
    jlong lvalue;
    JavaInfo* jcache = JavaGetCache();

    if (!objPtr) {
	return NULL;
    }

    /*
     * Make sure the Tcl_Obj does not have
     * an invalid ref to a TclObject that has a
     * CObject or TclList internal rep.
     */

    JavaBreakRef(env, objPtr);

    if ((objPtr->typePtr == &tclObjectType)
	    || ((objPtr->typePtr == cmdTypePtr) &&
		    (objPtr->internalRep.twoPtrValue.ptr2) != NULL)) {
	/*
	 * This object is a reference to a TclObject, so we extract the
	 * jobject.
	 */

	object = (jobject)(objPtr->internalRep.twoPtrValue.ptr2);
	if (isLocalPtr) {
	    *isLocalPtr = 0;
	}
    } else {
	/*
	 *
	 * We should be able to use the following statement below:
	 *
	 *     lvalue = (jlong) objPtr;
	 *
	 * But gcc warns "cast to pointer from integer of different size"
	 * so use an ugly workaround to avoid the compiler warning.
	 * Note that the value is zeroed out before assigning to
	 * handle the case where jlong is 64 while a ptr is 32 bits.
	 */

	lvalue = 0;
	*(Tcl_Obj **)&lvalue = objPtr;

	/*
	 * This object is of an unknown type, so create a new TclObject.
	 * If the Tcl object is a list, then create a TclList.
	 *
	 *    TclObject tobj = TclList.newInstance(long objPtr);
	 *
	 * Otherwise we don't know the type so create a CObject.
	 *
	 *    TclObject tobj = CObject.newInstance(long objPtr);
	 *
	 */

	if (objPtr->typePtr == listTypePtr) {
	    object = (*env)->CallStaticObjectMethod(env, jcache->TclList,
	        jcache->newTclListInstance, lvalue);
	} else {
	    object = (*env)->CallStaticObjectMethod(env, jcache->CObject,
	        jcache->newCObjectInstance, lvalue);
	}
	
	if ((*env)->ExceptionOccurred(env)) {
	    (*env)->ExceptionDescribe(env);
	    Tcl_Panic("JavaGetTclObject : exception in newInstance()");
	}

	/*
	 * Increment the ref count of the new TclObject so that
	 * it starts life with a ref count of 1. This operation
	 * does not change the ref count of the Tcl_Obj.
	 * We expect to be able to drop a ref to a CObject
	 * with a ref count of 1 without leaking memory in C.
	 */

	(*env)->CallVoidMethod(env, object, jcache->preserve);
	if ((*env)->ExceptionOccurred(env)) {
	    (*env)->ExceptionDescribe(env);
	    Tcl_Panic("JavaGetTclObject : exception in TclObject._preserve()");
	}

	if (isLocalPtr) {
	    *isLocalPtr = 1;
	}
    }
    return object;    
}

/*
 *----------------------------------------------------------------------
 *
 * FreeJavaCmdInternalRep --
 *
 *	Free the internal rep for a java object.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Decrements the refcount on a java object, and frees it if
 *	the last reference is gone.
 *
 *----------------------------------------------------------------------
 */

static void
FreeJavaCmdInternalRep(
    Tcl_Obj *objPtr)
{
    jobject jobj = (jobject) objPtr->internalRep.twoPtrValue.ptr2;

    if (jobj) {
	FreeTclObject(objPtr);
    }
    (oldCmdType.freeIntRepProc)(objPtr);
}

/*
 *----------------------------------------------------------------------
 *
 * DupJavaCmdInternalRep --
 *
 *	Initialize the internal representation of a java Tcl_Obj to a
 *	copy of the internal representation of an existing java object. 
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	"dupPtr"s internal rep is set to the java object corresponding to
 *	"srcPtr"s internal rep and the refcount on the java object
 *	in incremented.
 *
 *----------------------------------------------------------------------
 */

static void
DupJavaCmdInternalRep(
    Tcl_Obj *srcPtr,
    Tcl_Obj *dupPtr)
{
    jobject jobj = (jobject) srcPtr->internalRep.twoPtrValue.ptr2;
    (oldCmdType.dupIntRepProc)(srcPtr, dupPtr);
    dupPtr->internalRep.twoPtrValue.ptr2 = jobj;
    if (jobj) {
	DupTclObject(srcPtr, dupPtr);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * SetJavaCmdFromAny --
 *
 *	Attempt to generate a command object from an arbitrary type.
 *	This routine is a wrapper around the standard cmdName setFromAny
 *	procedure.  If the object holds a reference to a TclObject,
 *	save and restore the reference after the object is converted.
 *
 * Results:
 *	The return value is a standard object Tcl result. If an error occurs
 *	during conversion, an error message is left in the interpreter's
 *	result unless "interp" is NULL.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

static int
SetJavaCmdFromAny(
    Tcl_Interp *interp,
    Tcl_Obj *objPtr)
{
    int result;

    /*
     * Invoke the normal command type routine, but make sure
     * it doesn't free the java object by setting the typePtr
     * to NULL. Note that we have to restore the ptr2 value after
     * the conversion, since it gets set to NULL by setFromAnyProc.
     */

    if ((objPtr->typePtr == &tclObjectType) ||
	    ((objPtr->typePtr == cmdTypePtr) &&
		    (objPtr->internalRep.twoPtrValue.ptr2 != NULL))) {
	void *ptr2;
	if (objPtr->bytes == NULL) {
	    UpdateTclObject(objPtr);
	}
	objPtr->typePtr = NULL;
	objPtr->internalRep.twoPtrValue.ptr2 = NULL;
	result = (oldCmdType.setFromAnyProc)(interp, objPtr);
	objPtr->internalRep.twoPtrValue.ptr2 = ptr2;
    } else {
	result = (oldCmdType.setFromAnyProc)(interp, objPtr);
    }
    return result;
}

/*
 *----------------------------------------------------------------------
 *
 * JavaIsRef --
 *
 *	Return true if this Tcl_Obj* contains a ref to a TclObject.
 *
 * Results:
 *	1 or 0.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

int
JavaIsRef(
    Tcl_Obj *objPtr)		/* Object to check .*/
{
    return ((objPtr->typePtr == &tclObjectType) ||
            ((objPtr->typePtr == cmdTypePtr) &&
                    (objPtr->internalRep.twoPtrValue.ptr2) != NULL));
}

/*
 *----------------------------------------------------------------------
 *
 * JavaObjType --
 *
 *	Return a string that describes the internal rep type for objPtr.
 *
 * Results:
 *	A Tcl_Obj*.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

Tcl_Obj*
JavaObjType(
    Tcl_Obj *objPtr)		/* Object to check .*/
{
    const char *type;
    const Tcl_ObjType *stringTypePtr =  Tcl_GetObjType("string");

    if (objPtr->typePtr == &tclObjectType)
        type = "tclobject";
    else if ((objPtr->typePtr == cmdTypePtr) &&
            ((objPtr->internalRep.twoPtrValue.ptr2) != NULL))
        type = "cmdtclobject";
    else if (objPtr->typePtr == cmdTypePtr)
        type = "cmd";
    else if (objPtr->typePtr == listTypePtr)
        type = "list";
    else if (objPtr->typePtr == stringTypePtr)
        type = "string";
    else
        type = "unknown";

    return Tcl_NewStringObj(type, -1);
}

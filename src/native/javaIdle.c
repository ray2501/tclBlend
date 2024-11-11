/* 
 * javaIdle.c --
 *
 *	This file contains the native methods for the IdleHandler class.
 *
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: javaIdle.c,v 1.7 2006/07/26 20:55:27 mdejong Exp $
 */

#include "java.h"
#include "tcl_lang_CObject.h"
#include "tcl_lang_Interp.h"
#include "tcl_lang_TclList.h"
#include "tcl_lang_Util.h"
#include "tcl_lang_IdleHandler.h"
#include "tcl_lang_Notifier.h"
#include "tcl_lang_TimerHandler.h"

/*
 * Static functions used in this file.
 */

static void	JavaIdleProc(ClientData clientData);

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_IdleHandler_doWhenIdle --
 *
 *	Create a C level idle handler for a Java IdleHandler object.
 *
 * Results:
 *	Retuns an address to be stored in clientData.
 *
 * Side effects:
 *	Will create a new global ref to this object.
 *
 *----------------------------------------------------------------------
 */

jlong JNICALL
Java_tcl_lang_IdleHandler_doWhenIdle(
    JNIEnv *env,		/* Java environment. */
    jobject idle)		/* Handle to IdleHandler object. */
{
    jobject gref = (*env)->NewGlobalRef(env, idle);

    /*
     * It is not possible to store the global ref as an Object
     * field because the garbage collector could move the
     * object in memory before cancel was invoked. Instead,
     * store it as a long.
     */

    jlong clientData = 0;
    *(jobject *)&clientData = gref;

    /*
    fprintf(stderr, "doWhenIdle idle instance is %x\n", idle);
    fprintf(stderr, "doWhenIdle wrote long %llx\n", clientData);
    fprintf(stderr, "doWhenIdle gref is %x\n", gref);
    */

    Tcl_DoWhenIdle(JavaIdleProc, (ClientData) gref);
    return clientData;
}

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_IdleHandler_cancelIdleCall --
 *
 *	Delete a C level idle handler for a Java IdleHandler object.
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
Java_tcl_lang_IdleHandler_cancelIdleCall(
    JNIEnv *env,		/* Java environment. */
    jobject idle,		/* Handle to IdleHandler object. */
    jlong clientData)		/* Ptr to global ref passed as client data */
{
    jobject gref = *(jobject *)&clientData;

    /*
    fprintf(stderr, "cancelIdleCall idle instance is %x\n", idle);
    fprintf(stderr, "cancelIdleCall read long %llx\n", clientData);
    fprintf(stderr, "cancelIdleCall gref is %x\n", gref);
    */

    Tcl_CancelIdleCall(JavaIdleProc, (ClientData) gref);

    /*
     * Free global ref so object can be garbage collected.
     */

    (*env)->DeleteGlobalRef(env, gref);
}

/*
 *----------------------------------------------------------------------
 *
 * JavaIdleProc --
 *
 *	This function is called when a Java idle event occurs.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Invokes arbitrary Java code.
 *
 *----------------------------------------------------------------------
 */

static void
JavaIdleProc(
    ClientData clientData)	/* Global IdleHandler reference */
{
    JNIEnv *env = JavaGetEnv();
    jobject exception;
    jobject gref = (jobject) clientData;
    JavaInfo* jcache = JavaGetCache();
    int fromJNIMethod = JavaNotifierInDoOneEvent();

#ifdef TCLBLEND_DEBUG
    fprintf(stderr, "TCLBLEND_DEBUG: JavaIdleProc : fromJNIMethod is %d\n", fromJNIMethod);
#endif /* TCLBLEND_DEBUG */

    /*fprintf(stderr, "JavaIdleProc got global ref %x\n", gref);*/

    if ((*env)->ExceptionOccurred(env)) {
	(*env)->ExceptionDescribe(env);
	Tcl_Panic("JavaIdleProc : unexpected pending exception");
    }

    /*
     * Call IdleHandler.invoke(). If an exception was raised and
     * this method was invoked as a result of a Notifier.doOneEvent
     * then propagate the exception. If this method was
     * called from Tcl, then just print the error since we can't
     * leave the exception pending.
     */

    (*env)->CallVoidMethod(env, gref, jcache->invokeIdle);
    exception = (*env)->ExceptionOccurred(env);
    if (exception) {
	if (!fromJNIMethod) {
	    fprintf(stderr, "Java Exception in JavaIdleProc (Tcl_DoWhenIdle handler)\n");
	    (*env)->ExceptionDescribe(env);
	}
	(*env)->ExceptionClear(env);
    }

    /*
     * Free global ref so object can be garbage collected.
     */

    (*env)->DeleteGlobalRef(env, gref);

    /*
     * Rethrow the exception.
     */

    if (exception) {
	 if (fromJNIMethod) {
#ifdef TCLBLEND_DEBUG
    fprintf(stderr, "TCLBLEND_DEBUG: JavaIdleProc : rethrowing Exception\n");
#endif /* TCLBLEND_DEBUG */

	     (*env)->Throw(env, exception);
	 }
	 (*env)->DeleteLocalRef(env, exception);
    }
}


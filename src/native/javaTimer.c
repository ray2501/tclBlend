/* 
 * javaTimer.c --
 *
 *	This file contains the native methods for the TimerHandler class.
 *
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: javaTimer.c,v 1.7 2002/12/30 05:53:29 mdejong Exp $
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
 * The following structure is used to maintain a mapping from timer tokens to
 * Java objects so they can be freed if the timer is deleted.
 */

typedef struct TimerInfo {
    jobject obj;
    Tcl_TimerToken token;
} TimerInfo;

/*
 * Static functions used in this file.
 */

static void	JavaTimerProc(ClientData clientData);

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_TimerHandler_createTimerHandler --
 *
 *	Create a C level timer handler for a Java TimerHandler object.
 *
 * Results:
 *	Returns the Tcl_TimerToken.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

jlong JNICALL
Java_tcl_lang_TimerHandler_createTimerHandler(
    JNIEnv *env,		/* Java environment. */
    jobject timer,		/* Handle to TimerHandler object. */
    jint ms)
{
    jlong lvalue;
    TimerInfo *infoPtr;

    infoPtr = (TimerInfo *) ckalloc(sizeof(TimerInfo));
    infoPtr->obj = (*env)->NewGlobalRef(env, timer);
    infoPtr->token = Tcl_CreateTimerHandler(ms, JavaTimerProc,
	    (ClientData) infoPtr);

    lvalue = 0;
    *(TimerInfo**)&lvalue = infoPtr;
    return lvalue;
}

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_TimerHandler_deleteTimerHandler --
 *
 *	Delete a C level timer handler for a Java TimerHandler object.
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
Java_tcl_lang_TimerHandler_deleteTimerHandler(
    JNIEnv *env,		/* Java environment. */
    jobject timerObj,		/* Handle to TimerHandler object. */
    jlong info)			/* TimerInfo of timer to delete. */
{
    TimerInfo *infoPtr = *(TimerInfo**)&info;
    
    Tcl_DeleteTimerHandler(infoPtr->token);
    (*env)->DeleteGlobalRef(env, infoPtr->obj);
    ckfree((char *)infoPtr);
}

/*
 *----------------------------------------------------------------------
 *
 * JavaTimerProc --
 *
 *	This function is called when a Java timer expires.
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
JavaTimerProc(
    ClientData clientData)	/* Pointer to TimerInfo. */
{
    TimerInfo *infoPtr = (TimerInfo *) clientData;
    jobject exception;
    JNIEnv *env = JavaGetEnv();
    JavaInfo* jcache = JavaGetCache();
    int fromJNIMethod = JavaNotifierInDoOneEvent();

    if ((*env)->ExceptionOccurred(env)) {
	(*env)->ExceptionDescribe(env);
	panic("JavaTimerProc : unexpected pending exception");
    }

    /*
     * Call TimerHandler.invoke(). If an exception was raised and
     * this method was invoked as a result of a Notifier.doOneEvent
     * then propagate the exception. If this method was
     * called from Tcl, then print the error since we can't
     * leave the exception pending.
     */

    (*env)->CallVoidMethod(env, infoPtr->obj, jcache->invokeTimer);
    exception = (*env)->ExceptionOccurred(env);
    if (exception) {
	if (!fromJNIMethod) {
	    fprintf(stderr, "Java Exception in JavaTimerProc (Tcl_CreateTimerHandler handler)\n");
	    (*env)->ExceptionDescribe(env);
	}
	(*env)->ExceptionClear(env);
    }

    /*
     * Cean up the timer info since the timer has fired.
     */

    (*env)->DeleteGlobalRef(env, infoPtr->obj);
    ckfree((char *)infoPtr);

    /*
     * Rethrow the exception.
     */

    if (exception) {
	 if (fromJNIMethod)
	     (*env)->Throw(env, exception);
	 (*env)->DeleteLocalRef(env, exception);
    }
}


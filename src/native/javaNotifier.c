/* 
 * javaNotifier.c --
 *
 *	 This file contains the native methods for the Notifier class.
 *
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: javaNotifier.c,v 1.7 2002/12/30 05:53:29 mdejong Exp $
 */

#include "java.h"
#include "tcl_lang_CObject.h"
#include "tcl_lang_Interp.h"
#include "tcl_lang_TclList.h"
#include "tcl_lang_Util.h"
#include "tcl_lang_IdleHandler.h"
#include "tcl_lang_Notifier.h"
#include "tcl_lang_TimerHandler.h"

typedef struct ThreadSpecificData {

  jobject notifierObj;

  int eventQueued;

  int doOneEventCount;
} ThreadSpecificData;

static Tcl_ThreadDataKey dataKey;

/* Define this here so that we do not need to include tclInt.h */
#define TCL_TSD_INIT(keyPtr)	(ThreadSpecificData *)Tcl_GetThreadData((keyPtr), sizeof(ThreadSpecificData))

/*
 * Declarations for functions used only in this file.
 */

static int	JavaEventProc(Tcl_Event *evPtr, int flags);
static void	NotifierCheck(ClientData data, int flags);
static void	NotifierSetup(ClientData data, int flags);

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_Notifier_init --
 *
 *	Initialize the Java Notifier event source.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

jlong JNICALL
Java_tcl_lang_Notifier_init(
    JNIEnv *env,		/* Java environment. */
    jobject notifierObj)	/* Handle to Notifier object. */
{
    ThreadSpecificData *tsdPtr = TCL_TSD_INIT(&dataKey);
    jlong tid;

    tsdPtr->notifierObj     = (*env)->NewGlobalRef(env, notifierObj);
    tsdPtr->eventQueued     = 0;
    tsdPtr->doOneEventCount = 0;

    /* If we segfault near here under Windows, try removing tclblend.dll
     * from the current directory.  Tcl Blend has problems loading
     * dlls from a remote directory if there is a dll with the
     * same name in the local directory.
     */
    Tcl_CreateEventSource(NotifierSetup, NotifierCheck, NULL);

    tid = 0;
    *(Tcl_ThreadId *)&tid = Tcl_GetCurrentThread();
    return tid;
}

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_Notifier_dispose --
 *
 *	Clean up the Java Notifier event source.
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
Java_tcl_lang_Notifier_dispose(
    JNIEnv *env,		/* Java environment. */
    jobject notifierObj)	/* Handle to Notifier object. */
{
    ThreadSpecificData *tsdPtr = TCL_TSD_INIT(&dataKey);

    Tcl_DeleteEventSource(NotifierSetup, NotifierCheck, NULL);
    (*env)->DeleteGlobalRef(env, tsdPtr->notifierObj);
    tsdPtr->notifierObj = NULL;
}

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_Notifier_doOneEvent --
 *
 *	Process one event from the event queue.
 *
 * Results:
 *	Returns the result of Tcl_DoOneEvent().
 *
 * Side effects:
 *	May invoke arbitrary code.
 *
 *----------------------------------------------------------------------
 */

jint JNICALL
Java_tcl_lang_Notifier_doOneEvent(
    JNIEnv *env,		/* Java environment. */
    jobject notifierObj,	/* Handle to Notifier object. */
    jint flags)			/* Miscellaneous flag values: may be any
				 * combination of TCL.DONT_WAIT,
				 * TCL.WINDOW_EVENTS, TCL.FILE_EVENTS,
				 * TCL.TIMER_EVENTS, TCL.IDLE_EVENTS,
				 * or others defined by event sources. */
{
    int result;
    ThreadSpecificData *tsdPtr = TCL_TSD_INIT(&dataKey);

    tsdPtr->doOneEventCount++;
    result = Tcl_DoOneEvent(flags);
    tsdPtr->doOneEventCount--;
    return result;
}

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_Notifier_alertNotifier --
 *
 *	Wake up the Tcl Notifier so it can check the event sources.
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
Java_tcl_lang_Notifier_alertNotifier(
    JNIEnv *env,		/* Java environment. */
    jobject notifierObj,        /* Handle to Notifier object. */
    jlong tid)	                /* Tcl_ThreadId for the notifier thread */
{
    Tcl_ThreadAlert(*(Tcl_ThreadId *)&tid);
}

/*
 *----------------------------------------------------------------------
 *
 * NotifierSetup --
 *
 *	This routine checks to see if there are any events on the
 *	Java Notifier queue.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	May set the block time to 0.
 *
 *----------------------------------------------------------------------
 */

static void
NotifierSetup(
    ClientData data,		/* Not used. */
    int flags)			/* Same as for Tcl_DoOneEvent. */
{
    JNIEnv *env = JavaGetEnv();
    JavaInfo* jcache = JavaGetCache();
    ThreadSpecificData *tsdPtr = TCL_TSD_INIT(&dataKey);

    if ((*env)->CallBooleanMethod(env, tsdPtr->notifierObj, jcache->hasEvents)
	    == JNI_TRUE) {
	Tcl_Time timeout = { 0, 0 };
	Tcl_SetMaxBlockTime(&timeout);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * NotifierCheck --
 *
 *	This routine checks to see if there are any events on the
 *	Java Notifier queue.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	May queue a JavaEvent on the event queue.
 *
 *----------------------------------------------------------------------
 */

static void
NotifierCheck(
    ClientData data,		/* Not used. */
    int flags)			/* Same as for Tcl_DoOneEvent. */
{
    Tcl_Event *ePtr;
    JNIEnv *env = JavaGetEnv();
    JavaInfo* jcache = JavaGetCache();
    ThreadSpecificData *tsdPtr = TCL_TSD_INIT(&dataKey);

    /*
     * Only queue a new event if there isn't already one queued and
     * there are events on the Java event queue.
     */

    if (!tsdPtr->eventQueued && 
        (*env)->CallBooleanMethod(env, tsdPtr->notifierObj,
                                  jcache->hasEvents) == JNI_TRUE) {
	ePtr = (Tcl_Event *) ckalloc(sizeof(Tcl_Event));
	ePtr->proc = JavaEventProc;
	Tcl_QueueEvent(ePtr, TCL_QUEUE_TAIL);
	tsdPtr->eventQueued = 1;
    }
}

/*
 *----------------------------------------------------------------------
 *
 * JavaEventProc --
 *
 *	This procedure is invoked when a JavaEvent is processed from
 *	the Tcl event queue.  
 *
 * Results:
 *	None.  
 *
 * Side effects:
 *	Invokes arbitrary Java code.
 *
 *----------------------------------------------------------------------
 */

static int
JavaEventProc(
    Tcl_Event *evPtr,		/* The event that is being processed. */
    int flags)			/* The flags passed to Tcl_ServiceEvent. */
{
    JNIEnv *env = JavaGetEnv();
    JavaInfo* jcache = JavaGetCache();
    ThreadSpecificData *tsdPtr = TCL_TSD_INIT(&dataKey);

    if ((*env)->ExceptionOccurred(env)) {
	(*env)->ExceptionDescribe(env);
	Tcl_Panic("JavaEventProc : unexpected pending exception");
    }

    /*
     * Call Notifier.serviceEvent() to handle invoking the next event and
     * signaling any threads that are waiting on the event.
     */

    tsdPtr->eventQueued = 0;
    
    (void) (*env)->CallIntMethod(env, tsdPtr->notifierObj, 
                                 jcache->serviceEvent, flags);
    if ((*env)->ExceptionOccurred(env)) {
	(*env)->ExceptionDescribe(env);
	Tcl_Panic("JavaEventProc : exception in Notifier.serviceEvent()");
    }

    return 1;
}

/*
 *----------------------------------------------------------------------
 *
 * Java_tcl_lang_Notifier_finalizeThreadCheck --
 *
 *     Checks to see if the Notifier for this thread has been
 *     released because the last interpreter in the thread was
 *     disposed of. If so, the cleanup Tcl's thread local storage.
 *
 * Class:     tcl_lang_Notifier
 * Method:    finalizeThreadCheck
 * Signature: ()V;
 *
 * Results:
 *     Releases thread specific data.
 *
 * Side effects:
 *     Thread may detach from JVM, or JVM may be destroyed.
 *
 *----------------------------------------------------------------------
 */

void JNICALL
Java_tcl_lang_Notifier_finalizeThreadCheck(
    JNIEnv *env,               /* Java environment. */
    jclass notifierClass)      /* Handle to Notifier class. */
{
    ThreadSpecificData *tsdPtr = TCL_TSD_INIT(&dataKey);

    if ((tsdPtr->notifierObj == NULL) && JavaWasJavaThreadInit()) {
#ifdef TCLBLEND_DEBUG
        fprintf(stderr, "TCLBLEND_DEBUG: Invoking Tcl_FinalizeThread\n");
#endif /* TCLBLEND_DEBUG */

        Tcl_FinalizeThread();

#ifdef TCLBLEND_DEBUG
        fprintf(stderr, "TCLBLEND_DEBUG: Done with Tcl_FinalizeThread\n");
#endif /* TCLBLEND_DEBUG */
    } else {
#ifdef TCLBLEND_DEBUG
        fprintf(stderr, "TCLBLEND_DEBUG: Tcl Thread data not finalized\n");
#endif /* TCLBLEND_DEBUG */
    }
}

/*
 *----------------------------------------------------------------------
 *
 * JavaNotifierInDoOneEvent --
 *
 *     Returns true if the notifier for this thread is currently
 *     executing Tcl_DoOneEvent in the doOneEvent() native
 *     method.
 *
 * Results:
 *     1 or 0.
 *
 * Side effects:
 *     None.
 *
 *----------------------------------------------------------------------
 */

int 
JavaNotifierInDoOneEvent()
{
    ThreadSpecificData *tsdPtr = TCL_TSD_INIT(&dataKey);
    return (tsdPtr->doOneEventCount > 0);
}

/*
 * java.h --
 *
 *	Declarations of structures and functions used to implement
 *	the native Java support in Tcl.
 *
 * Copyright (c) 1997 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: java.h,v 1.18 2003/01/21 09:42:55 mdejong Exp $
 */

#ifndef _JAVA
#define _JAVA

#include <tcl.h>
#include <jni.h>

/*
 * Macros used to declare a function to be exported by a DLL.
 * Used by Windows, maps to no-op declarations on non-Windows systems.
 * The default build on windows is for a DLL, which causes the DLLIMPORT
 * and DLLEXPORT macros to be nonempty. To build a static library, the
 * macro STATIC_BUILD should be defined.
 * The support follows the convention that a macro called BUILD_xxxx, where
 * xxxx is the name of a library we are building, is set on the compile line
 * for sources that are to be placed in the library. See BUILD_tcl in this
 * file for an example of how the macro is to be used.
 */

#ifdef __WIN32__
# undef DLLIMPORT
# undef DLLEXPORT
# ifdef STATIC_BUILD
#  define DLLIMPORT
#  define DLLEXPORT
# else
#  if defined(_MSC_VER) || defined(__MINGW32__)
#   define DLLIMPORT __declspec(dllimport)
#   define DLLEXPORT __declspec(dllexport)
#  else
#   define DLLIMPORT
#   define DLLEXPORT
#  endif
# endif
#else
# define DLLIMPORT
#   if defined(__GNUC__) && __GNUC__ > 3
#       define DLLEXPORT __attribute__ ((visibility("default")))
#   else
#       define DLLEXPORT
#   endif
#endif

#ifdef TCLBLEND_STORAGE_CLASS
# undef TCLBLEND_STORAGE_CLASS
#endif
#define BUILD_tclblend
#ifdef BUILD_tclblend
# define TCLBLEND_STORAGE_CLASS DLLEXPORT
#else
# define TCLBLEND_STORAGE_CLASS DLLIMPORT
#endif

#ifdef __cplusplus
#   define TCLBLEND_EXTERN extern "C" TCLBLEND_STORAGE_CLASS
#else
#   define TCLBLEND_EXTERN extern TCLBLEND_STORAGE_CLASS
#endif

#if defined(__WIN32__)
#   define WIN32_LEAN_AND_MEAN
#   include <windows.h>
#   undef WIN32_LEAN_AND_MEAN

/*
 * VC++ has an alternate entry point called DllMain, so we need to rename
 * our entry point.
 */
#if 0
#   if defined(_MSC_VER)
#	define EXPORT(a,b) __declspec(dllexport) a b
#	define DllEntryPoint DllMain
#   else
#	if defined(__BORLANDC__)
#	    define EXPORT(a,b) a _export b
#	else
#	    define EXPORT(a,b) a b
#	endif
#   endif
#else
#   define EXPORT(a,b) a b
#endif
#else /* __WIN32__ */
#   define EXPORT(a,b) a b
#endif

/*
 * Added Tcl_Size definition
 */
#if TCL_MAJOR_VERSION < 9
    #ifndef Tcl_Size
        typedef int Tcl_Size;
    #endif
#endif

/*
 * The following structure contains cached class information.
 */

typedef struct JavaInfo {
    jclass Object;
    jmethodID toString;
    jclass Interp;
    jmethodID callCommand;
    jmethodID dispose;
    jmethodID interpC;
    jfieldID interpPtr;
    jclass Command;
    jmethodID cmdProc;
    jclass TclObject;
    jmethodID preserve;
    jmethodID release;
    jmethodID getCObjectPtr;
    jmethodID getCObjectInst;
    jclass TclException;
    jmethodID tclexceptionC;
    jmethodID tclexceptionCcode;
    jclass CommandWithDispose;
    jmethodID disposeCmd;
    jclass CObject;
    jmethodID newCObjectInstance;
    jclass TclList;
    jmethodID newTclListInstance;
    jclass Extension;
    jmethodID init;
    jclass BlendExtension;
    jmethodID blendC;
    jclass VarTrace;
    jmethodID traceProc;
    jclass Notifier;
    jmethodID serviceEvent;
    jmethodID hasEvents;
    jclass IdleHandler;
    jmethodID invokeIdle;
    jclass TimerHandler;
    jmethodID invokeTimer;
} JavaInfo;

/*
 * Declarations for functions shared across files.
 */

TCLBLEND_EXTERN JNIEnv *	JavaGetEnv();
TCLBLEND_EXTERN JavaInfo *	JavaGetCache();
TCLBLEND_EXTERN int		JavaWasJavaThreadInit();
TCLBLEND_EXTERN Tcl_Interp *	JavaGetInterp(JNIEnv *env, jobject interpObj);
TCLBLEND_EXTERN char *		JavaGetString(JNIEnv *env, jstring str,
			    	    Tcl_Size *lengthPtr);
TCLBLEND_EXTERN Tcl_Obj *	JavaGetTclObj(JNIEnv *env, jobject object);
TCLBLEND_EXTERN jobject		JavaGetTclObject(JNIEnv *env, Tcl_Obj *objPtr,
			    	    int *isLocal);
TCLBLEND_EXTERN void		JavaBreakRef(JNIEnv *env, Tcl_Obj *objPtr);
TCLBLEND_EXTERN int		JavaIsRef(Tcl_Obj *objPtr);
TCLBLEND_EXTERN Tcl_Obj *	JavaObjType(Tcl_Obj *objPtr);
TCLBLEND_EXTERN int		JavaNotifierInDoOneEvent();
TCLBLEND_EXTERN int		JavaSetupJava(JNIEnv *env, Tcl_Interp *interp);
TCLBLEND_EXTERN int		JavaInitBlend(JNIEnv *env, Tcl_Interp *interp,
			    	    jobject interpObj);
TCLBLEND_EXTERN void		JavaInterpDeleted(ClientData clientData,
			    	    Tcl_Interp *interp);
TCLBLEND_EXTERN void		JavaObjInit();
TCLBLEND_EXTERN void		JavaThrowTclException(JNIEnv *env,
				    Tcl_Interp *interp, int result);
void				TclBlendTrace(const char *str);
TCLBLEND_EXTERN int		TclBlendWasTclLoad();

/*
 * Declarations for exported functions.
 */

TCLBLEND_EXTERN int Tclblend_Init (Tcl_Interp *interp);

#endif /* _JAVA */

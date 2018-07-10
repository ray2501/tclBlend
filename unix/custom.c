#include <stdlib.h>
#include <tcl.h>
#include <jni.h>


JavaVM *vm;			/* Java virtual machine. */
JNIEnv *env;			/* Java run-time environment. */



/*
 *----------------------------------------------------------------------
 *
 * main --
 *
 *	This is the main program for the custom shell.
 *
 * Results:
 *	None: Tcl_Main never returns here, so this procedure never
 *	returns either.
 *
 * Side effects:
 *	Whatever the application does.
 *
 *----------------------------------------------------------------------
 */

int
main(argc, argv)
    int argc;			/* Number of command-line arguments. */
    char **argv;		/* Values of command-line arguments. */
{
    jsize nVMs;
    char *path, *newPath;
    int oldSize, size;
    Tcl_Interp *interp;

#ifdef JDK1_2

    JavaVMOption *options;
    JavaVMInitArgs vm_args;

#else

#ifdef TCLBLEND_KAFFE

    JavaVMInitArgs vm_args;

#else

    JDK1_1InitArgs vm_args;

#endif
#endif




    if (JNI_GetCreatedJavaVMs(&vm, 1, &nVMs) < 0) {
	printf("JNI_GetCreatedJavaVMs failed!\n");
	exit(1);
    }

    if (nVMs == 0) {
        memset(&vm_args, 0, sizeof(vm_args));

#ifdef JDK1_2

	#define maxOptions 2

	options = (JavaVMOption *) ckalloc(sizeof(JavaVMOption) * maxOptions);
        vm_args.version = 0x00010002;
        vm_args.options = options;
        vm_args.ignoreUnrecognized = 1;
        vm_args.nOptions = 0;

#else

	vm_args.version = 0x00010001;

#endif



        JNI_GetDefaultJavaVMInitArgs(&vm_args);
	path = getenv("CLASSPATH");

#ifdef JDK1_2

        #define JAVA_CLASS_PATH_ARG "-Djava.class.path="

	if (path) {
	    size = strlen(path) + strlen(JAVA_CLASS_PATH_ARG);
            options[0].optionString = ckalloc(size+2);
            vm_args.nOptions++;
	    strcpy(options[0].optionString, JAVA_CLASS_PATH_ARG);
	    strcat(options[0].optionString, path);
	    options[0].extraInfo = (void *)NULL;
	}

#else

	if (path && vm_args.classpath) {
	    oldSize = strlen(path);
	    size = oldSize + strlen(vm_args.classpath);
	    newPath = ckalloc(size+2);
	    strcpy(newPath, path);
#ifdef __WIN32__
	    newPath[oldSize] = ';';
#else
	    newPath[oldSize] = ':';
#endif
	    strcpy(newPath+oldSize+1, vm_args.classpath);
	    vm_args.classpath = newPath;
	} else if (path) {
	    vm_args.classpath = path;
	}

#endif


#ifdef JDK1_2

	if (JNI_CreateJavaVM(&vm, (void **) &env, &vm_args) < 0){

#else

	if (JNI_CreateJavaVM(&vm, &env, &vm_args) < 0) {

#endif

	    printf("can't initialize JVM, JNI_CreateJavaVM Failed\n");
	    exit(2);
	}
    } else {
	printf("%d JVMs already in process space\n", nVMs);
    }

    Tcl_Main(argc, argv, Tcl_AppInit);
    return 0;			/* Needed only to prevent compiler warning. */
}

/*
 *----------------------------------------------------------------------
 *
 * Tcl_AppInit --
 *
 *	This procedure performs application-specific initialization.
 *	Most applications, especially those that incorporate additional
 *	packages, will have their own version of this procedure.
 *
 * Results:
 *	Returns a standard Tcl completion code, and leaves an error
 *	message in interp->result if an error occurs.
 *
 * Side effects:
 *	Depends on the startup script.
 *
 *----------------------------------------------------------------------
 */

int
Tcl_AppInit(interp)
    Tcl_Interp *interp;		/* Interpreter for application. */
{
    if (Tcl_Init(interp) == TCL_ERROR) {
	return TCL_ERROR;
    }
    if (Tclblend_Init(interp) == TCL_ERROR) {
	return TCL_ERROR;
    }
    return TCL_OK;
}


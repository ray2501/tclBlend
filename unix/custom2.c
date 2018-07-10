#include <stdlib.h>
#include <string.h>
#include <tcl.h>
#include <jni.h>


JavaVM *vm;			/* Java virtual machine. */
JNIEnv *env;			/* Java run-time environment. */



/*
 *----------------------------------------------------------------------
 *
 * main --
 *
 *	This is the main program for the custom2 shell. It create
 *	a JVM and executes the main() method for the class named
 *	on the command line.
 *
 * Results:
 *	None:
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
    int i;

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

    jclass mainClass;
    jmethodID mainMethod;
    jobjectArray mainArgs;
    jstring mainArg;
    char *mainClassName;

    /*
     * Pass name of Java class to test and any command line
     * arguments on the custom2 command line. These will
     * be passed to the args for the main() method in the
     * named class.
     */
    if (argc < 2) {
        fprintf(stderr, "Usage: custom2 CLASSNAME ARGS ...\n");
        exit(1);
    }
    for (i=0; i < argc; i++) {
        printf("argv[%d] is \"%s\"\n", i, argv[i]);
    }
    mainClassName = strdup(argv[1]);

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

    /* Run main() for class named on command line */

    /* Replace '.' with '/' in class name */
    for (i=0; i<strlen(mainClassName); i++) {
        if (mainClassName[i] == '.') {
            mainClassName[i] = '/';
        }
    }
    mainClass = (*env)->FindClass(env, mainClassName);
    mainMethod = (*env)->GetStaticMethodID(env, mainClass,
        "main", "([Ljava/lang/String;)V");

    mainArgs = (*env)->NewObjectArray(env, (argc - 2),
        (*env)->FindClass(env, "java/lang/String"), NULL);

    for (i=2; i < argc; i++) {
        printf("mainArgs[%d] = \"%s\"\n", i-2, argv[i]);

        mainArg = (*env)->NewStringUTF(env, argv[i]);
	(*env)->SetObjectArrayElement(env, mainArgs, i-2, mainArg);
    }

    (*env)->CallStaticVoidMethod(env, mainClass, mainMethod, mainArgs);

    return 0;			/* Needed only to prevent compiler warning. */
}

/* This method exists so that the tclblend shared library is linked
 * to this program. This makes it easier to debug in gdb.
 */

void uncalled() {
    Tclblend_Init((Tcl_Interp *) NULL);
}


#------------------------------------------------------------------------
# AC_MSG_LOG( MSG, ?LOGONLY? )
#
#	Write the message out to the config.log file and the console.
#	If 1 is passed as the second argument, then write to the
#	config.log file only.
#
# Arguments:
#	1. The message to log
#	2. Optional boolean, if true then write to config.log only
#------------------------------------------------------------------------

AC_DEFUN([AC_MSG_LOG], [
    echo $1 >&AS_MESSAGE_LOG_FD
    m4_ifval([$2],,[echo $1])
])

#------------------------------------------------------------------------
# AC_GREP_FILE( PATTERN, FILE, ACTION-IF-FOUND, [ACTION-IF-NOT-FOUND])
#
#	Use grep to search for a pattern in a file. If the pattern
#	is not found then return a non zero exit status. No information
#	will be echoed to the screen by this macro.
#
# Arguments:
#	1. The pattern to search for
#	2. The name of the file to be grep'ed
#	3. The script to execute if PATTERN is found in FILE
#	4. The script to execute if PATTERN is not found in FILE (optional)
#------------------------------------------------------------------------

AC_DEFUN([AC_GREP_FILE], [
    AC_MSG_LOG([grep in $2 for pattern '"$1"'], 1)
    if (grep "$1" $2 > /dev/null 2>&1) ; then
        AC_MSG_LOG([grep result : yes], 1)
        $3
    else
        AC_MSG_LOG([grep result : no], 1)
        m4_ifval([$4], [
            $4
        ])dnl
    fi
])


#------------------------------------------------------------------------
# TCLJAVA_VERSION_CHECK
#
#	Check the TCLJAVA_VERSION variable set at the top of configure.in.
#	If it has changed since we last ran configure, then we need to
#	update the hard coded tcljava version numbers in source files.
#
# Arguments:
#	NONE
#------------------------------------------------------------------------

AC_DEFUN([TCLJAVA_VERSION_CHECK], [
    TOOLS=$srcdir/unix/tools

    # The tools directory is not distributed in dist .tar files.
    if test -d "$TOOLS" ; then

        # Check that grep is working by looking for the match to the
        # current version number in the configure.in file.

	AC_GREP_FILE(TCLJAVA_VERSION=$TCLJAVA_VERSION, $srcdir/configure.in, , [
            AC_MSG_ERROR([TCLJAVA_VERSION grep failed.
            did you rerun autoconf after changing the version number in configure.in?])
        ])

        # Check that the files we need actually exist

        if test ! -d $TOOLS/vutil ; then
            AC_MSG_ERROR([$TOOLS/vutil does not exist])
        fi

        if test ! -f $TOOLS/vutil/vsub.sh ; then
            AC_MSG_ERROR([$TOOLS/vutil/vsub.sh does not exist])
        fi

        # Find out if we need to update the version numbers by checking to
        # see if the one we got differs from the one in the file "current".

        AC_GREP_FILE(TCLJAVA_VERSION=$TCLJAVA_VERSION, $TOOLS/vutil/current, , [
            AC_MSG_LOG([Updating version numbers in source files])
            TMP_CWD=`pwd`
            cd $TOOLS/vutil
            $SHELL vsub.sh $TCLJAVA_VERSION
            cd $TMP_CWD
        ])
    fi
])




#------------------------------------------------------------------------
# TCLJAVA_ENABLE_JACL_OR_TCLBLEND
#
#	Check to see is --enable-jacl or --enable-tclblend is given
#	at the command line. If one of them is given then configure
#	and build for that subsystem only, otherwise do an automated
#	check to see which one we should build. Configure to build
#	them both if possible (for instance, when checked out of the CVS)
#
# Arguments:
#	NONE
#
# VARIABLES SET:
#	TCLJAVA is set to "jacl" "tclblend" or "both".
#
#------------------------------------------------------------------------

AC_DEFUN([TCLJAVA_ENABLE_JACL_OR_TCLBLEND], [

    AC_ARG_ENABLE(jacl, [  --enable-jacl            build Jacl only, used with CVS version],
	[ok=$enableval], [ok=no])
    if test "$ok" = "yes"; then
	TCLJAVA=jacl
    fi

    AC_ARG_ENABLE(tclblend, [  --enable-tclblend        build Tcl Blend only, used with CVS version],
	[ok=$enableval], [ok=no])
    if test "$ok" = "yes"; then
	TCLJAVA=tclblend
    fi

    if test "x$TCLJAVA" = "x"; then
        if test -d $srcdir/src/tclblend && test -d $srcdir/src/jacl ; then
            AC_MSG_LOG(configuring for both jacl and tclblend)
            TCLJAVA=both
        elif test -d $srcdir/src/tclblend ; then
            TCLJAVA=tclblend
        elif  test -d $srcdir/src/jacl ; then
            TCLJAVA=jacl
        else
            AC_MSG_ERROR([Cannot find jacl or tclblend sources])
        fi
    fi

    if test "$TCLJAVA" = "jacl"; then
        case "`uname -s`" in
	    *MINGW* | *win32* | *WIN32* | *CYGWIN_NT* |*CYGWIN_98*|*CYGWIN_95*)
                ac_cv_tcl_win32="yes"
                ;;
            *)
                ac_cv_tcl_win32="no"
                ;;
        esac
    fi
])




#------------------------------------------------------------------------
# AC_JAVA_WITH_JDK
#
#	Check to see if the --with-jdk command line option is given.
#	If it was, then set ac_java_with_jdk to the DIR argument.
#
# Arguments:
#	NONE
#
# VARIABLES SET:
#	ac_java_with_jdk can be set to the directory where the jdk lives
#	ac_java_jvm_name can be set to "jdk"
#------------------------------------------------------------------------

AC_DEFUN([AC_JAVA_WITH_JDK], [
    AC_ARG_WITH(jdk, [  --with-jdk=DIR          use Sun's JDK from DIR], ok=$withval, ok=no)
    if test "$ok" = "no" ; then
        NO=op
    elif test "$ok" = "yes" || test ! -d "$ok"; then
        AC_MSG_ERROR([--with-jdk=DIR option, must pass a valid DIR])
    elif test "$ok" != "no" ; then
        ac_java_jvm_dir=$ok
        ac_java_jvm_name=jdk
    fi
])

#------------------------------------------------------------------------
# AC_PROG_JAVAC
#
#	If JAVAC is not already defined, then search for "javac" on
#	the path. If a java compiler is found, then test it to make
#	sure it actually works.
#
# Arguments:
#	NONE
#
# VARIABLES SET:
#	JAVAC can be set to the path name of the java compiler
#	JAVAC_FLAGS can be set to compiler specific flags
#	ac_java_jvm_dir can be set to the jvm's root directory
#------------------------------------------------------------------------

AC_DEFUN([AC_PROG_JAVAC], [
    if test "x$JAVAC" = "x" ; then
        AC_PATH_PROG(JAVAC, javac)
        if test "x$JAVAC" = "x" ; then
            AC_MSG_ERROR([javac not found on PATH ... did you forget --with-jdk=DIR])
        fi
    fi
    if test ! -f "$JAVAC" ; then
        AC_MSG_ERROR([javac '$JAVAC' does not exist.
        Perhaps Java is not installed or you passed a bad dir to a --with option.])
    fi

    # Check for Solaris install which uses a symlink in /usr/bin to /usr/java/bin
    if test -h "$JAVAC" ; then
        BASE=`basename $JAVAC`
        DIR=`dirname $JAVAC`
        if test -f $DIR/../java/bin/$BASE ; then
            JAVAC=`cd $DIR/../java/bin;pwd`/$BASE
        fi
    fi

    # If we were searching for javac, then set ac_java_jvm_dir
    if test "x$ac_java_jvm_dir" = "x"; then
        TMP=`dirname $JAVAC`
        TMP=`dirname $TMP`
        ac_java_jvm_dir=$TMP
    fi

    # Look for a setting for the CLASSPATH, we might need one to run JAVAC
    AC_JAVA_CLASSPATH

    # FIXME : add detection of command line arguments for JAVAC

    JAVAC_FLAGS=-g
    JAVAC_D_FLAG=-d
    JAVAC_H_FLAG=-h

    dnl Test out the Java compiler with an empty class
    AC_MSG_CHECKING([to see if the java compiler works])
    AC_JAVA_TRY_COMPILE(,,works=yes)
    if test "$works" = "yes" ; then
        AC_MSG_RESULT($works)
    else
        AC_MSG_ERROR([Could not compile simple Java program with '$JAVAC'])
    fi

    AC_MSG_LOG([Using JAVAC=$JAVAC], 1)
])


#------------------------------------------------------------------------
# AC_JAVA_TRY_COMPILE(imports, main-body, action-if-worked, [action-if-failed])
#
#	Try to compile a Java program. This works a lot like AC_TRY_COMPILE
#	except is supports Java instead of C or C++. This macro will create
#	a file named Test.java and try to compile it.
#
# Arguments:
#	imports should contain Java import statements like [import java.util.*;]
#       main-body should contain the code to appear in the main() method
#	action-if-worked should contain the code to run if the compile worked
#	action-if-failed should contain the code to run if the compile failed (optional)
#------------------------------------------------------------------------

AC_DEFUN([AC_JAVA_TRY_COMPILE], [
    cat << \EOF > conftest.java
// [#]line __oline__ "configure"
[$1]

public class conftest {
    public static void main(String[[]] argv) {
        [$2]
    }
}
EOF

    CLASSPATH=$ac_java_classpath
    export CLASSPATH
    cmd="$JAVAC ${JAVAC_FLAGS} conftest.java"
    if (echo $cmd >&AS_MESSAGE_LOG_FD ; eval $cmd >&AS_MESSAGE_LOG_FD 2>&AS_MESSAGE_LOG_FD) ; then
        echo "yes" >&AS_MESSAGE_LOG_FD
        $3
    else
        echo "configure: failed program was:" >&AS_MESSAGE_LOG_FD
        cat conftest.java >&AS_MESSAGE_LOG_FD
        echo "configure: CLASSPATH was $CLASSPATH" >&AS_MESSAGE_LOG_FD
        m4_ifval([$4],
        [  $4
        ])dnl
    fi
])

#------------------------------------------------------------------------
# AC_JAVA_DETECT_JVM
#
#	Figure out what JVM to build with. If no JVM was already defined
#	using a --with command line option then we search for one
#	by looking for the javac executable.
#
# Arguments:
#	NONE
#
# VARIABLES SET:
#	JAVAC
#	ac_java_jvm_version can be set to 1.4, or 1.5
#	ac_java_jvm_dir can be set to the jvm's root directory
#
# DEPENDS ON:
#	This macro can depend on the values set by the following macros:
#	AC_JAVA_WITH_JDK
#	AC_PROG_JAVAC
#------------------------------------------------------------------------

AC_DEFUN([AC_JAVA_DETECT_JVM], [

    # if we do not know the jvm dir, javac will be found on the PATH
    if test "x$JAVAC" = "x" && test "x$ac_java_jvm_dir" != "x"; then
        ac_java_jvm_dir=`cd $ac_java_jvm_dir ; pwd`
        JAVAC=$ac_java_jvm_dir/bin/javac${EXEEXT}
    fi

    # Search for and test the javac compiler
    AC_PROG_JAVAC

    AC_MSG_LOG([Java found in $ac_java_jvm_dir])

    # Try to detect non JDK JVMs. If we can't, then just assume a jdk

    AC_MSG_CHECKING([type of jvm]) 

    if test "x$ac_java_jvm_name" = "x" ; then
        AC_JAVA_TRY_COMPILE([import gnu.java.io.EncodingManager;],,ac_java_jvm_name=gcj)
    fi

    if test "x$ac_java_jvm_name" = "x" ; then
       ac_java_jvm_name=jdk
    fi

    AC_MSG_RESULT([$ac_java_jvm_name])

    case "$ac_java_jvm_name" in
        gcj) DO=nothing ;;
        jdk) DO=nothing ;;
        *) AC_MSG_ERROR(['$ac_java_jvm_name' is not a supported JVM]) ;;
    esac

    # Try to detect the version of java that is installed

    AC_MSG_CHECKING([java API version])

    # The class java.nio.charset.Charset is new to 1.4

    AC_JAVA_TRY_COMPILE([import java.nio.charset.Charset;], , ac_java_jvm_version=1.4)

    # The class java.lang.StringBuilder is new to 1.5

    AC_JAVA_TRY_COMPILE([import java.lang.StringBuilder;], , ac_java_jvm_version=1.5)

    if test "x$ac_java_jvm_version" = "x" ; then
        AC_MSG_ERROR([Could not detect Java version, 1.4 or newer is required])
    fi

    AC_MSG_RESULT([$ac_java_jvm_version])

])


#------------------------------------------------------------------------
# AC_JAVA_CLASSPATH
#
#	Find out which .zip or .jar files need to be included on
#	the CLASSPATH if we are setting it via an env variable.
#
# Arguments:
#	NONE
#
# VARIABLES SET:
#	ac_java_classpath
#
# DEPENDS ON:
#	This macro is used by the AC_JAVA_DETECT_JVM macro.
#	It depends on the ac_java_jvm_dir variable.
#------------------------------------------------------------------------

AC_DEFUN([AC_JAVA_CLASSPATH], [
    AC_MSG_CHECKING([for zip or jar files to include on CLASSPATH])

    if test "x$ac_java_jvm_dir" = "x" ; then
        AC_MSG_ERROR([jvm directory not set])
    fi

    # GNU gcj does not need to set the CLASSPATH.

    # Assume that JDK 1.4 and newer systems will
    # not need to explicitly set the CLASSPATH.

    # Append CLASSPATH if env var is set. Avoid append
    # under msys because CLASSPATH is in Win32 format
    # and we can't combine it with a msys path.
    if test "x$CLASSPATH" != "x" && test "$ac_cv_tcl_win32" != "yes" ; then
        AC_MSG_LOG([Adding user supplied CLASSPATH env var])
        ac_java_classpath="${ac_java_classpath}:${CLASSPATH}"
    fi

    AC_MSG_LOG([Using CLASSPATH=$ac_java_classpath], 1)
    AC_MSG_RESULT($ac_java_classpath)
])


#------------------------------------------------------------------------
# AC_JAVA_TOOLS
#
#	Figure out the paths of any Java tools we will need later on.
#
# Arguments:
#	NONE
#
# VARIABLES SET:
#	JAVA
#	JAVA_G
#	JAVAC
#	JAR
#	JDB
#
# DEPENDS ON:
#	This macro must be run after the AC_JAVA_DETECT_JVM macro as
#	it depends on the ac_java_jvm_name, ac_java_jvm_version and
#	ac_java_jvm_dir variables
#------------------------------------------------------------------------

AC_DEFUN([AC_JAVA_TOOLS], [
    AC_JAVA_TOOLS_CHECK(JAVA, java, $ac_java_jvm_dir/bin)

    # Don't error if java_g can not be found
    AC_JAVA_TOOLS_CHECK(JAVA_G, java_g, $ac_java_jvm_dir/bin, 1)

    if test "x$JAVA_G" = "x" ; then
        JAVA_G=$JAVA
    fi

    AC_JAVA_TOOLS_CHECK(JAR, jar, $ac_java_jvm_dir/bin)

    # Don't error if jdb can not be found
    AC_JAVA_TOOLS_CHECK(JDB, jdb, $ac_java_jvm_dir/bin, 1)

    case "$ac_java_jvm_version" in
        *)
            # JDK on Win32 does not allow connection with suspend=n
            if test "$ac_cv_tcl_win32" = "yes"; then
                suspend="y"
            else
                suspend="n"
            fi
            JDB_ATTACH_FLAGS="-attach 8757"
            JAVA_G_FLAGS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8757,server=y,suspend=$suspend"
            JDB_ATTACH_FLAGS="-attach 8757"
            ;;
    esac
])

#------------------------------------------------------------------------
# AC_JAVA_TOOLS_CHECK(VARIABLE, TOOL, PATH, NOERR)
#
#	Helper function that will look for the given tool on the
#	given PATH. If cross compiling and the tool can not
#	be found on the PATH, then search for the same tool
#	on the users PATH. If the tool still can not be found
#	then give up with an error unless NOERR is 1.
#
# Arguments:
#	1. The variable name we pass to AC_PATH_PROG
#	2. The name of the tool
#	3. The path to search on
#	4. Pass 1 if you do not want any error generated 
#------------------------------------------------------------------------

AC_DEFUN([AC_JAVA_TOOLS_CHECK], [
    if test "$cross_compiling" = "yes" ; then
        AC_PATH_PROG($1, $2)
    else
        AC_PATH_PROG($1, $2, , $3)
    fi

    # Check to see if $1 could not be found

    m4_ifval([$4],,[
    if test "x[$]$1" = "x" ; then
        AC_MSG_ERROR([Cannot find $2 on $3])
    fi
    ])
])

#------------------------------------------------------------------------
# AC_JAVA_JNI_INCLUDE
#
#	Figure out where jni.h and jni_md.h include files are installed.
#
# Arguments:
#	NONE
#
# VARIABLES SET:
#	ac_java_jvm_jni_include_flags : Flags that we pass to the compiler
#           so that it can locate JNI headers. (for example: -I/usr/jdk/include)
#
# DEPENDS ON:
#	This macro must be run after the AC_JAVA_DETECT_JVM macro as
#	it depends on the ac_java_jvm_dir variable.
#------------------------------------------------------------------------

AC_DEFUN([AC_JAVA_JNI_INCLUDE], [

    # Look for jni.h in the subdirectory $ac_java_jvm_dir/include

    F=$ac_java_jvm_dir/include/jni.h
    if test -f "$F" ; then
         ac_java_jvm_jni_include_flags="-I`dirname $F`"
    else
         F=`ls $ac_java_jvm_dir/include/*/jni.h 2>/dev/null`
         if test -f "$F" ; then
             ac_java_jvm_jni_include_flags="-I`dirname $F`"
         else
             AC_MSG_ERROR([Could not locate Java's jni.h include file])
         fi
    fi

    # Look for jni_md.h in an arch specific subdirectory
    # we assume that there is only one arch subdirectory,
    # if that is not the case we would need to use $host

    F=`ls $ac_java_jvm_dir/include/*/jni_md.h 2>/dev/null`
    if test -f "$F" ; then
        ac_java_jvm_jni_include_flags="$ac_java_jvm_jni_include_flags -I`dirname $F`"
    fi

    AC_MSG_LOG([Using the following JNI include flags $ac_java_jvm_jni_include_flags])

    # Make sure a simple #include <jni.h> will compile.

    AC_REQUIRE([AC_PROG_CC])

    AC_CACHE_CHECK(to see if jni.h can be included,
        ac_java_jvm_jni_working,[
        AC_LANG_PUSH(C)
        ac_saved_cflags=$CFLAGS
        CFLAGS="$CFLAGS $ac_java_jvm_jni_include_flags"
        AC_TRY_COMPILE([
            #include <jni.h>
        ],[return 0;],
        ac_java_jvm_jni_working=yes,
        AC_MSG_ERROR([could not compile file that includes jni.h]))
        AC_LANG_POP()
        CFLAGS=$ac_saved_cflags
    ])

    # FIXME: should we look for or require a include/native_threads dir?
])


#------------------------------------------------------------------------
# AC_JAVA_JNI_LIBS
#
#	Figure out where the native threads libraries for JNI live.
#
# Arguments:
#	NONE
#
# VARIABLES SET:
#	ac_java_jvm_ld_preload : list of libraries to include in LD_PROLOAD
#	ac_java_jvm_ld_bind_now : if set to 1, then use LD_BIND_NOW=1
#	ac_java_jvm_jni_lib_flags : library flags that we will pass to the compiler.
#	    For instance, we might pass -L/usr/jdk/lib -ljava
#	ac_java_jvm_jni_lib_runtime_path : colon separated path of directories
#	    that is typically passed to rld.
#
# DEPENDS ON:
#	This macro must be run after the AC_JAVA_DETECT_JVM macro as
#	it depends on the ac_java_jvm_dir variable.
#------------------------------------------------------------------------

AC_DEFUN([AC_JAVA_JNI_LIBS], [
    machine=`uname -m`
    case "$machine" in
        i?86)
          machine=i386
          ;;
    esac

    # Check for known JDK installation layouts

    if test "$ac_java_jvm_name" = "jdk"; then

        # OpenJDK 1.8 for Linux (server JVM)

        F=jre/lib/$machine/libjava.so
        if test "x$ac_java_jvm_jni_lib_flags" = "x" ; then
            AC_MSG_LOG([Looking for $ac_java_jvm_dir/$F], 1)
            if test -f $ac_java_jvm_dir/$F ; then
                AC_MSG_LOG([Found $ac_java_jvm_dir/$F], 1)

                D=`dirname $ac_java_jvm_dir/$F`
                ac_java_jvm_jni_lib_runtime_path=$D
                ac_java_jvm_jni_lib_flags="-L$D -ljava -lverify"

                D=$ac_java_jvm_dir/jre/lib/$machine/server
                ac_java_jvm_jni_lib_runtime_path="${ac_java_jvm_jni_lib_runtime_path}:$D"
                ac_java_jvm_jni_lib_flags="$ac_java_jvm_jni_lib_flags -L$D -ljvm"
            fi
        fi

        # Sun JDK 1.5 for AMD64 Linux (server JVM)

        F=jre/lib/amd64/libjava.so
        if test "x$ac_java_jvm_jni_lib_flags" = "x" ; then
            AC_MSG_LOG([Looking for $ac_java_jvm_dir/$F], 1)
            if test -f $ac_java_jvm_dir/$F ; then
                AC_MSG_LOG([Found $ac_java_jvm_dir/$F], 1)

                D=`dirname $ac_java_jvm_dir/$F`
                ac_java_jvm_jni_lib_runtime_path=$D
                ac_java_jvm_jni_lib_flags="-L$D -ljava -lverify"

                D=$ac_java_jvm_dir/jre/lib/amd64/server
                ac_java_jvm_jni_lib_runtime_path="${ac_java_jvm_jni_lib_runtime_path}:$D"
                ac_java_jvm_jni_lib_flags="$ac_java_jvm_jni_lib_flags -L$D -ljvm"
            fi
        fi

        # OpenJDK 9 Linux (server JVM)

        F=lib/libjava.so
        if test "x$ac_java_jvm_jni_lib_flags" = "x" ; then
            AC_MSG_LOG([Looking for $ac_java_jvm_dir/$F], 1)
            if test -f $ac_java_jvm_dir/$F ; then
                AC_MSG_LOG([Found $ac_java_jvm_dir/$F], 1)

                D=`dirname $ac_java_jvm_dir/$F`
                ac_java_jvm_jni_lib_runtime_path=$D
                ac_java_jvm_jni_lib_flags="-L$D -ljava -lverify"

                D=$ac_java_jvm_dir/lib/server
                ac_java_jvm_jni_lib_runtime_path="${ac_java_jvm_jni_lib_runtime_path}:$D"
                ac_java_jvm_jni_lib_flags="$ac_java_jvm_jni_lib_flags -L$D -ljvm"
            fi
        fi

        # Sun JDK 1.4 and 1.5 for Win32 (client JVM)

        F=lib/jvm.lib
        if test "x$ac_java_jvm_jni_lib_flags" = "x" ; then
            AC_MSG_LOG([Looking for $ac_java_jvm_dir/$F], 1)
            if test -f $ac_java_jvm_dir/$F ; then
                # jre/bin/client must contain jvm.dll
                # jre/bin/server directory could also contain jvm.dll,
                # just assume the user wants to use the client JVM.
                DLL=jre/bin/client/jvm.dll
                if test -f $ac_java_jvm_dir/$DLL ; then
                    AC_MSG_LOG([Found $ac_java_jvm_dir/$F], 1)
                    D1=$ac_java_jvm_dir/jre/bin
                    D2=$ac_java_jvm_dir/jre/bin/client
                    ac_java_jvm_jni_lib_runtime_path="${D1}:${D2}"
                    ac_java_jvm_jni_lib_flags="$ac_java_jvm_dir/$F"
                fi
            fi
        fi
        
        # Sun JDK 1.4 and 1.5 for Win32 (server JVM)

        F=lib/jvm.lib
        if test "x$ac_java_jvm_jni_lib_flags" = "x" ; then
            AC_MSG_LOG([Looking for $ac_java_jvm_dir/$F], 1)
            if test -f $ac_java_jvm_dir/$F ; then
                # jre/bin/client must contain jvm.dll
                # jre/bin/server directory could also contain jvm.dll,
                # just assume the user wants to use the server JVM.
                DLL=jre/bin/server/jvm.dll
                if test -f $ac_java_jvm_dir/$DLL ; then
                    AC_MSG_LOG([Found $ac_java_jvm_dir/$F], 1)
                    D1=$ac_java_jvm_dir/jre/bin
                    D2=$ac_java_jvm_dir/jre/bin/server
                    ac_java_jvm_jni_lib_runtime_path="${D1}:${D2}"
                    ac_java_jvm_jni_lib_flags="$ac_java_jvm_dir/$F"
                fi
            fi
        fi        
    fi

    # Generate error for unsupported JVM layout

    if test "x$ac_java_jvm_jni_lib_flags" = "x" ; then
        AC_MSG_ERROR([Could not detect the location of the Java
            shared library. You will need to update tcljava.m4
            to add support for this JVM configuration.])
    fi

    AC_MSG_LOG([Using the following JNI library flags $ac_java_jvm_jni_lib_flags])
    AC_MSG_LOG([Using the following runtime library path $ac_java_jvm_jni_lib_runtime_path])

    AC_MSG_LOG([Using LD_PRELOAD=$ac_java_jvm_ld_preload], 1)
    AC_MSG_LOG([Using LD_BIND_NOW=$ac_java_jvm_ld_bind_now], 1)

    # Make sure we can compile and link a trivial JNI program

    AC_REQUIRE([AC_PROG_CC])

    AC_CACHE_CHECK(to see if we can link a JNI application,
        ac_java_jvm_working_jni_link,[
        AC_LANG_PUSH(C)
        ac_saved_cflags=$CFLAGS
        ac_saved_libs=$LIBS
        CFLAGS="$CFLAGS $ac_java_jvm_jni_include_flags"
        LIBS="$LIBS $ac_java_jvm_jni_lib_flags"
        AC_TRY_LINK([
            #include <jni.h>
        ],[JNI_GetCreatedJavaVMs(NULL,0,NULL);],
            ac_java_jvm_working_jni_link=yes,
            ac_java_jvm_working_jni_link=no)
        AC_LANG_POP()
        CFLAGS=$ac_saved_cflags
        LIBS=$ac_saved_libs
    ])

    # gcc can't link with some JDK .lib files under Win32.
    # Work around this problem by linking with win/libjvm.dll.a

    if test "$ac_java_jvm_working_jni_link" != "yes" &&
      test "$ac_cv_tcl_win32" = "yes"; then
        AC_LANG_PUSH(C)
        ac_saved_cflags=$CFLAGS
        ac_saved_libs=$LIBS
        CFLAGS="$CFLAGS $ac_java_jvm_jni_include_flags"
        LIBS="$LIBS -L$srcdir/win -ljvm"
        AC_TRY_LINK([
            #include <jni.h>
        ],[JNI_GetCreatedJavaVMs(NULL,0,NULL);],
            ac_java_jvm_working_jni_link=yes,
            ac_java_jvm_working_jni_link=no)
        AC_LANG_POP()
        CFLAGS=$ac_saved_cflags
        LIBS=$ac_saved_libs

        if test "$ac_java_jvm_working_jni_link" = "yes"; then
            AC_MSG_LOG([Using custom JNI link lib])
            ac_java_jvm_jni_lib_flags="-L$srcdir/win -ljvm"
        fi
    fi

    if test "$ac_java_jvm_working_jni_link" != "yes"; then
        AC_MSG_ERROR([could not link file that includes jni.h
        Either the configure script does not know how to deal with
        this JVM configuration or the JVM install is broken or corrupted.])
    fi
])




#------------------------------------------------------------------------
# TCLJAVA_WITH_TCL
#
#	Check to see if the --with-tcl command line option is given.
#	If it was, then load Tcl configure info from tclConfig.sh
#	This option is not used when configuring for Jacl.
#
# Arguments:
#	NONE
#
# VARIABLES SET:
#	TCL_BIN_DIR
#	Vars defined by tclConfig.sh
#------------------------------------------------------------------------

AC_DEFUN([TCLJAVA_WITH_TCL], [

if test $TCLJAVA = "tclblend" || test $TCLJAVA = "both"; then

    #--------------------------------------------------------------------
    #	See if there was a command-line option for where Tcl is;  if
    #	not, assume that its top-level directory is a sibling of ours.
    #--------------------------------------------------------------------
    
    AC_ARG_WITH(tcl, [  --with-tcl=DIR          build directory for Tcl 8.3.2 (or newer) source release from DIR],
    	TCL_BIN_DIR=$withval, TCL_BIN_DIR=default)

    # See if a default directory exist
    if test "$TCL_BIN_DIR" = "default" ; then
        if test -d $srcdir/../tcl8.4.1/unix ; then
            TCL_BIN_DIR=$srcdir/../tcl8.4.1/unix
        else
            TCL_BIN_DIR=
        fi
    fi

    if test "$TCL_BIN_DIR" = "" || test "$TCL_BIN_DIR" = "no" ; then
        AC_MSG_ERROR([Use the --with-tcl=<dirName> configure flag to indicate
where the Tcl build directory is.])
    fi

    if test ! -d "$TCL_BIN_DIR"; then
        AC_MSG_ERROR([Tcl build directory $TCL_BIN_DIR could not be located.
Use the --with-tcl=<dirName> configure flag to specify the location.])
    else
	TCL_BIN_DIR=`cd $TCL_BIN_DIR; pwd`
    fi

    AC_MSG_LOG([checking for Tcl build in $TCL_BIN_DIR])

    if test ! -f $TCL_BIN_DIR/tclConfig.sh; then
        # provide shortcut if --with-tcl=$PATH/tcl8.X was given
        if test -f $TCL_BIN_DIR/unix/tclConfig.sh; then
            TCL_BIN_DIR=$TCL_BIN_DIR/unix
        else
	    AC_MSG_ERROR([Tcl was not configured in the directory $TCL_BIN_DIR.])
        fi
    fi

    #--------------------------------------------------------------------
    #	Read in configuration information generated by Tcl for shared
    #	libraries, and arrange for it to be substituted into our
    #	Makefile.
    #--------------------------------------------------------------------

    file=$TCL_BIN_DIR/tclConfig.sh
    . $file
    if test "$TCL_SHARED_BUILD" = "0" ; then
        AC_MSG_ERROR([Tcl was not built correctly.  
Make sure Tcl was configured with --enable-shared.])
    fi

    if test "$TCL_DLL_FILE" != "" && test "x$ac_cv_tcl_win32" = "x" ; then
        ac_cv_tcl_win32=yes
    fi

    CC=$TCL_CC
    SHLIB_CFLAGS=$TCL_SHLIB_CFLAGS

    # We need to add stdcall aliases when building a dll
    # under Win32 so that Java finds exported JNI symbols
    if test "$ac_cv_tcl_win32" = "yes"; then
        SHLIB_LD="$TCL_SHLIB_LD -mwindows -Wl,--add-stdcall-alias"
    else
        SHLIB_LD=$TCL_SHLIB_LD
    fi

    # Tcl < 8.4.2 does not define TCL_SHLIB_LD_LIBS for win32
    if test "$TCL_SHLIB_LD_LIBS" = "" &&
       test "$ac_cv_tcl_win32" = "yes" ; then
        SHLIB_LD_LIBS='${LIBS}'
    else
        SHLIB_LD_LIBS=$TCL_SHLIB_LD_LIBS
    fi

    if test "$ac_cv_tcl_win32" = "yes" ; then
        SHLIB_PREFIX=""
    else
        SHLIB_PREFIX=lib
    fi

    # Tcl < 8.4.2 does not define TCL_SHLIB_SUFFIX for win32
    if test "$TCL_SHLIB_SUFFIX" = "" &&
       test "$ac_cv_tcl_win32" = "yes" ; then
        SHLIB_SUFFIX=".dll"
    else
        SHLIB_SUFFIX=$TCL_SHLIB_SUFFIX
    fi
    SHLIB_VERSION=$TCL_SHLIB_VERSION

    # Tcl < 8.4.2 does not define TCL_BUILD_LIB_SPEC for win32
    if test "$TCL_BUILD_LIB_SPEC" = "" &&
       test "$ac_cv_tcl_win32" = "yes" ; then
        TCL_BUILD_LIB_SPEC="$TCL_BIN_DIR/$TCL_LIB_FILE"
    fi

    # Set debug extension for the Tcl Blend shared lib
    # as defined by the Java method System.loadLibrary()

    # FIXME : this is removed until System.loadLibrary() bugs are fixed.
    #if test "$TCL_DBGX" = "g"; then
    #    TCLJAVA_DBGX=_g
    #fi

    # Add -g to compile flags
    if test "$TCL_DBGX" = "g"; then
        TCL_CFLAGS=$TCL_CFLAGS_DEBUG
    else
        TCL_CFLAGS=$TCL_CFLAGS_OPTIMIZE
    fi

#FIXME: replace this, does this happend in Tcl 8.3?
    # Fix up the TCL_LD_SEARCH_FLAGS (known problem fixed by TEA)

    case "`uname -s`" in
        SunOS*)
            TCL_LD_SEARCH_FLAGS=`echo ${TCL_LD_SEARCH_FLAGS} | sed -e 's/-Wl,-R,/-R /'`
            ;;
        IRIX)
            TCL_LD_SEARCH_FLAGS=`echo ${TCL_LD_SEARCH_FLAGS} | sed -e 's/-Wl,-rpath,/-rpath /'`
            ;;
    esac

fi
])


#------------------------------------------------------------------------
# TCLJAVA_WITH_THREAD
#
#	Check to see if the --with-thread command line option is given.
#	If it was, use the tcl thread extension located in that directory.
#	This option is not used when configuring for Jacl.
#
# Arguments:
#	NONE
#
# VARIABLES SET:
#	THREAD_BIN_DIR : DIR when Tcl Thread extension has been built.
#------------------------------------------------------------------------

AC_DEFUN([TCLJAVA_WITH_THREAD], [

if test $TCLJAVA = "tclblend" || test $TCLJAVA = "both"; then

    AC_ARG_WITH(thread, [  --with-thread=DIR          build directory for Tcl Thread Extension],
    	THREAD_BIN_DIR=$withval, THREAD_BIN_DIR=default)

    # See if a default directory exist
    if test "$THREAD_BIN_DIR" = "default" ; then
        if test -d $srcdir/../thread/unix ; then
            THREAD_BIN_DIR=$srcdir/../thread/unix
        else
            THREAD_BIN_DIR=
        fi
    fi

    if test "$THREAD_BIN_DIR" = "" || test "$THREAD_BIN_DIR" = "no" ; then
        AC_MSG_ERROR([Use the --with-thread=<dirName> configure flag to indicate
where the required Thread extension build directory is.])
    fi

    if test ! -d "$THREAD_BIN_DIR"; then
        AC_MSG_ERROR([Thread directory $THREAD_BIN_DIR could not be located.
Use the --with-thread=<dirName> configure flag to specify the location.])
    else
	THREAD_BIN_DIR=`cd $THREAD_BIN_DIR; pwd`
    fi

    AC_MSG_LOG([checking for Thread build in $THREAD_BIN_DIR])

    if test ! -f $THREAD_BIN_DIR/pkgIndex.tcl; then
        AC_MSG_ERROR([Thread pkgIndex.tcl not found in the directory $THREAD_BIN_DIR.])
    fi

    AC_SUBST(THREAD_BIN_DIR)
fi
])

#------------------------------------------------------------------------
# TCLJAVA_CHECK_TCLSH
#
#	Check for the installed version of tclsh and wish. we need to use the
#	one we compiled against because you can not compile with one version
#	and then load into another. If you compiled Tcl Blend with Tcl 8.1 and
#	then load it into a Tcl 8.0 interp, it will segfault. Also make
#	sure that this shell was compiled with threads support.
#
# Arguments:
#	NONE
#
# VARIABLES SET:
#	FIXME
#------------------------------------------------------------------------

AC_DEFUN([TCLJAVA_CHECK_TCLSH], [

if test $TCLJAVA = "tclblend" || test $TCLJAVA = "both"; then

  # Check to make sure that tclsh has been built by looking for the
  # tclsh executable in the TCL_BIN_DIR directory.

  TCL_VERSION_NO_DOTS=`echo $TCL_VERSION | sed 's/\.//g'`

  if test "$ac_cv_tcl_win32" = "yes"; then
    TCLSH_LOC=$TCL_BIN_DIR/tclsh${TCL_VERSION_NO_DOTS}${TCL_DBGX}
  else
    TCLSH_LOC=$TCL_BIN_DIR/tclsh
  fi
  if test ! -x "$TCLSH_LOC"; then
    AC_MSG_ERROR([Tcl was configued in $TCL_BIN_DIR, but it has not been built, please build it and run configure again.])
  fi

  # Double check that tclsh works and that it is tcl 8.3.2 or better
  # We need to set LD_LIBRARY_PATH and SHLIB_PATH so that Tcl can find its
  # shared library in the build directory on a Unix or HP-UX system. Also
  # set TCL_LIBRARY so that Tcl can init itself from a build dir.

  LD_LIBRARY_PATH=$TCL_BIN_DIR:$LD_LIBRARY_PATH
  export LD_LIBRARY_PATH
  SHLIB_PATH=$TCL_BIN_DIR:$SHLIB_PATH
  export SHLIB_PATH
  TCL_LIBRARY=$TCL_SRC_DIR/library
  export TCL_LIBRARY

  rm -f tcl_version.tcl

  echo 'puts HELLO' > tcl_version.tcl
  if test "`$TCLSH_LOC tcl_version.tcl 2>&AS_MESSAGE_LOG_FD`" != "HELLO"; then
    AC_MSG_ERROR([$TCLSH_LOC is broken, I could not run a simple Tcl script with it])
  fi

  echo '
        if {[[catch {package require Tcl 8.6-} err]]} {
          puts stderr $err
          exit -1
        }
        puts 1
        exit 0
       ' > tcl_version.tcl

  if test "`$TCLSH_LOC tcl_version.tcl 2>&AS_MESSAGE_LOG_FD`" = "1"; then
      AC_MSG_RESULT([Tcl executable $TCLSH_LOC works])
      rm -f tcl_version.tcl
  else
      rm -f tcl_version.tcl
      AC_MSG_ERROR([$TCLSH_LOC is not version 8.6 or newer])
  fi

  # Check that Tcl was compiled with thread support.

  echo '
        if {[[package vsatisfies [package provide Tcl] 9.0-]]} {
          puts 1
          exit 0
        } else {
          if {! [[info exists tcl_platform(threaded)]]} {
            puts stderr $err
            exit -1
          }
          puts 1
          exit 0
       }
       ' > tcl_threads.tcl

  if test "`$TCLSH_LOC tcl_threads.tcl 2>&AS_MESSAGE_LOG_FD`" = "1"; then
      AC_MSG_RESULT([Tcl was compiled with Thread support])
      rm -f tcl_threads.tcl
  else
      rm -f tcl_threads.tcl
      AC_MSG_ERROR([Tcl must be compiled with Thread support (--enable-threads)])
  fi


  # Now check to see if "make install" has been run in the tcl directory.
  # The installed executable name is something like tclsh8.3.
  # We also assume that wish is going to be installed in the same
  # location, which could be incorrect but oh well.

  TCL_INSTALL_LIB_DIR=$TCL_EXEC_PREFIX/lib

  if test "$ac_cv_tcl_win32" = "yes"; then
    TCLSH=$TCL_EXEC_PREFIX/bin/tclsh${TCL_VERSION_NO_DOTS}${TCL_DBGX}
    WISH=$TCL_EXEC_PREFIX/bin/wish${TCL_VERSION_NO_DOTS}${TCL_DBGX}
  else
    TCLSH=$TCL_EXEC_PREFIX/bin/tclsh$TCL_VERSION
    WISH=$TCL_EXEC_PREFIX/bin/wish$TCL_VERSION
  fi

  if test ! -x "$TCLSH"; then
      AC_MSG_WARN([Tcl has not been installed yet, it must be installed before installing Tcl Blend])
  fi

fi

])

#------------------------------------------------------------------------
# AC_GCC_BUGS()
#
#	Check for specific GCC bug(s) that break Tcl Blend.
#
#------------------------------------------------------------------------

AC_DEFUN([AC_GCC_BUGS], [
    if test "$TCLJAVA" = "tclblend" || test "$TCLJAVA" = "both" ; then

    AC_REQUIRE([AC_PROG_CC])

    # GCC 4.1.0 compiled with -O2 contains an optimizer bug
    # that causes pointer conversion from long long to fail.

    AC_CACHE_CHECK(for GCC 4.1.0 optimizer bug,
        ac_java_gcc_optimizer_bug,[
        AC_LANG_PUSH(C)
        ac_saved_cflags=$CFLAGS
        CFLAGS="$CFLAGS -O2"
        AC_TRY_RUN([
            int main(int argc, char **argv) {
              void * p= (void *) 0xcccccccc;
              unsigned long long int x = 0;
              *((void **)&x) = p;
              if (x == 0xcccccccc) {
                return 0;
              } else {
                return 1;
              }
            }
        ],
        ac_java_gcc_optimizer_bug=no,
        ac_java_gcc_optimizer_bug=yes,
        ac_java_gcc_optimizer_bug=no)
        AC_LANG_POP()
        CFLAGS=$ac_saved_cflags
    ])

    if test "$ac_java_gcc_optimizer_bug" = "yes"; then
        AC_MSG_ERROR([This gcc release contains an optimizer bug that breaks Tcl Blend, please use another version of gcc])
    fi

    fi
])




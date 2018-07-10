tclBlend
===== 

It is my personal backup. This is tclBlend 1.4.1 source code via several
patches from tclBlend website.

* [21 allow TclBlend to load Tcl 8.5 into a Java process](http://sourceforge.net/p/tcljava/patches/21/)
* [22 Compile/​Run against Tcl 8.6](http://sourceforge.net/p/tcljava/patches/22/)
* [24 Patch for TclJava Bug 2866640](http://sourceforge.net/p/tcljava/patches/24/)

And for [TIP 330](http://www.tcl.tk/cgi-bin/tct/tip/330.html),
modified tclBlend\src\native\javaInterp.c

    #if TCL_MAJOR_VERSION > 8 || \
        (TCL_MAJOR_VERSION == 8 && TCL_MINOR_VERSION > 5)
        ckalloc((unsigned) (strlen(Tcl_GetStringResult(interp)) + 1));
    #else
        ckalloc((unsigned) (strlen(interp->result) + 1));
    #endif
    #if TCL_MAJOR_VERSION > 8 || \
        (TCL_MAJOR_VERSION == 8 && TCL_MINOR_VERSION > 5)
    strcpy(tPtr->errMsg, Tcl_GetStringResult(interp));
    #else
    strcpy(tPtr->errMsg, interp->result);
    #endif

And if you want to build tclBlend on Windows 7 x86_64 via JDK 8 (64bit), update tcljava.m4:

    # Sun JDK 1.4 and 1.5 for Win32 x86_64 (server JVM)

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

Then execute `autoconf` to gen a new configure file.
Or you can modify `configure` file directly.

Build script for Tcl/Tk 8.6.x (using MSYS2):

    #!/bin/sh
    mkdir -p /src
    mkdir -p /opt/tcl
    mkdir -p /build/tcl
    mkdir -p /build/tk
    [ -e /src/tcl ] && {
        cd /build/tcl
        /src/tcl/win/configure --prefix=/opt/tcl --enable-64bit --enable-threads \
            && make && make install && {
            [ -e /src/tk ] && {
            cd /build/tk
            /src/tk/win/configure --prefix=/opt/tcl --enable-threads --with-tcl=/build/tcl \
                && make && make install
            }
        }
    }

And build script for tclBlend (remember update thread extension version):

    #!/bin/sh
    mkdir -p /build/tclblend
    [ -e /src/tclBlend ] && {
        cd /build/tclblend
        /src/tclBlend/configure --prefix=/opt/tcl --with-tcl=/build/tcl \
        --with-thread=/build/tcl/pkgs/thread2.7.3/ --with-jdk=/c/JDK/ \
        && make && make install
    }
    

If you want to build tclBlend on Ubuntu 14.04, it is possible to build tclBlend from current Tcl binary files:  
First step is to update tcljava.m4.

    sed -i -e 's,TCLSH_LOC=\$TCL_BIN_DIR/tclsh,TCLSH_LOC=/usr/bin/tclsh,' tcljava.m4

Then do `autoconf` to update configure file.

Next step is to execute configure (Environment - Open JDK 8, thread extension version is 2.7.0):

    ./configure --prefix=/usr --with-jdk=/usr/lib/jvm/java-8-openjdk-amd64 --with-tcl=/usr/lib
    --with-thread=/usr/lib/tcltk/thread2.7.0

openSUSE (64bit) Tcl extension folder is at /usr/lib64/tcl,
so I add libdir option to handle this case:

    ./configure --prefix=/usr --libdir=/usr/lib64/tcl --with-tcl=/usr/lib64
    --with-jdk=/usr/lib64/jvm/java-1.8.0-openjdk --with-thread=/usr/lib64/tcl/thread2.8.2

And execute `make` and `make install`.

Tcl 8.6.1-8.6.5 maybe need patch, please check [tclBlend](http://wiki.tcl.tk/1313).

Notice:
Now requires JDK >= 8,
For JEP 313: Remove the Native-Header Generation Tool update


#!/bin/sh

# empty.sh will regenerate a empty.jar file from the
# source files in tcljava/src/empty. It must be run
# from the tcljava/src/empty directoy with javac
# and jar executables on the path.

export CLASSPATH=`cd ../tcljava;pwd`

mkdir -p tmp1/tcl/lang tmp2/tcl/lang
javac -d tmp1 tcl/lang/*.java

cp tmp1/tcl/lang/Interp.class \
   tmp1/tcl/lang/TclList.class \
   tmp1/tcl/lang/CObject.class \
   tmp1/tcl/lang/TclObject.class \
   tmp1/tcl/lang/Notifier.class \
   tmp1/tcl/lang/Util.class \
   tmp2/tcl/lang

cd tmp2
jar -cf ../empty.jar tcl
cd ..

rm -rf tmp1 tmp2


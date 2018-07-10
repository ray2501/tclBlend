empty.jar is used to work around a problem that occurs when compiling
Jacl or Tcl Blend that results in extra classes being included in the
tcljava.jar file.

This process can be run with the empty.sh script, it is described in
detail below.

When building, the classes below
 tcl/lang/CObject.class
 tcl/lang/Interp.class
 tcl/lang/Notifier.class
 tcl/lang/TclList.class
 tcl/lang/TclObject.class
 tcl/lang/Util.class

get incorrectly included in tcljava.jar.  These .class files belong
in tclblend.jar or jacl.jar.  If these .class files are in tcljava.jar,
then we cannot share the same tcljava.jar file between Jacl and Tcl Blend.

Configuring with --srcdir= and compiling in a separate directory
is the main cause of this bug, compiling so that the .class files
end up in the same directory as the .java files masks this bug.

The workaround here is to compile with an empty.jar file that contains
stubs to the methods in the offending classes.  Note that empty.jar
need not be included at runtime, it is only necessary at compile time.

If you change any of the public APIs to the classes above, you will
have to regenerate a new empty.jar file for use with the multiple
compiling system.


To do this set your CLASSPATH to include ROOT/src/tcljava and then
execute the following comands in the dir ROOT/src/empty.

mkdir -p tmp1/tcl/lang tmp2/tcl/lang
javac -d tmp1 tcl/lang/*.java

# Then copy the listed classes from tmp1 to tmp2 like this.

cp tmp1/tcl/lang/Interp.class \
   tmp1/tcl/lang/TclList.class \
   tmp1/tcl/lang/CObject.class \
   tmp1/tcl/lang/TclObject.class \
   tmp1/tcl/lang/Notifier.class \
   tmp1/tcl/lang/Util.class \
   tmp2/tcl/lang


# Then cd to tmp2 and make a jar file for the classes like this.

cd tmp2
jar -cf ../empty.jar tcl
cd ..


# Finally clean up the tmp dirs like this.

rm -rf tmp1 tmp2


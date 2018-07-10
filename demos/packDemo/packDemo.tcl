package require java

#make sure . is in the CLASSPATH
set env(TCL_CLASSPATH) .

#define the java_pack comand
source java_pack.tcl

set mid [java::new java.awt.Button Mid]
set bot [java::new java.awt.Button Bot]
set right [java::new java.awt.Button Right]

set f [java::new java.awt.Frame]
$f setSize 200 200

java_pack $right -in $f -side right -fill y -expand false -anchor n

java_pack $bot -in $f -side bottom -fill both -expand true \
	-anchor s -padx 10 -pady 10

java_pack $mid -in $f -anchor center

$f show

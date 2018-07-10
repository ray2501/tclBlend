puts "Testing installed program"
flush stdout

if {[catch {
package require java
} err]} {
  puts stderr "\"package require java\" failed with the following error"
  puts stderr $err
  exit -1
}

set env(TCL_CLASSPATH) .
catch {java::call Test isOK} res

if {$res == "OK"} {
  puts "Installed program is working correctly"
  exit 0
} else {
  puts stderr "Installed program is not working correctly, please recheck installation"
  puts stderr "Error was -> $res"
  puts stderr "Test was run from [pwd]"
  puts stderr "CLASSPATH was $env(CLASSPATH)"
  exit -1
}

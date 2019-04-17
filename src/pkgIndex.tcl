# Cross platform init script for Tcl Blend. Known to work on unix and windows.
# Author:  Christopher Hylands, Mo Dejong
# RCS: @(#) $Id: pkgIndex.tcl,v 1.27 2007/06/07 20:52:14 mdejong Exp $

proc loadtclblend {dir} {
    global tclblend_init env tcljava

    if {[info exists tcljava(tcljava)] && $tcljava(tcljava) == "jacl"} {
	# If Jacl is trying to load Tcl Blend because they share
	# a common auto_path entry, ignore the attempt
	return
    }

    # We do not want to get bitten by env array bugs in old versions
    # of Tcl, so we require that they use Tcl 8.3 or newer
    package require Tcl 8.3

    # Load the XpUtils package
    package require XpUtils

    # Set to true to get extra debug output
    set debug_loadtclblend 0

    # Turn on debug messages if tclblend_init is set to debug
    if { [info exists tclblend_init] && "$tclblend_init" == "debug" } {
	set debug_loadtclblend 1
    }

    if {$debug_loadtclblend} {
	puts "\ncalled loadtclblend \"$dir\""
    }

    # Find tcljava.jar and tclblend.jar in Tcl/lib/tcljava${VERSION}

    set tj_name tcljava.jar
    set tb_name tclblend.jar
    set jacl_name jacl.jar

    set tj_jar [file join $dir $tj_name]
    set tb_jar [file join $dir $tb_name]

    if {! [file exists $tj_jar]} {
	error "could not find $tj_jar"
    }

    if {! [file exists $tb_jar]} {
	error "could not find $tb_jar"
    }

    # We need to know the CLASSPATH value.  On Windows, this may have
    # arbitrary capitalization, so we need to copy it into the all-caps
    # form for later use.

    if {! [info exists env(CLASSPATH)]} {
	foreach name [array names env] {
	    if {[string equal -nocase $name "CLASSPATH"]} {
		set env(CLASSPATH) $env($name)
		break
	    }
	}
    }

    if {! [info exists env(CLASSPATH)]} {
        if {$debug_loadtclblend} {
	    puts "setting env(CLASSPATH) to {}"
        }

	# Ack! We can not just set this to {} because that
	# would unset the CLASSPATH under windows.
	set env(CLASSPATH) [XpUtils::getPathSeparator]
    }

    # now we need to search on the CLASSPATH to see if tclblend.jar
    # or tcljava.jar are already located on the CLASSPATH. If one
    # of these two files is already on the CLASSPATH then we must not
    # change the CLASSPATH because it should already be correct.
    # this can heppend in two cases. First the user could set the
    # CLASSPATH to use a custom tclblend.jar or tcljava.jar so it should
    # not be overridden. Second, if another interp loads tclblend
    # and then the current interp loads tclblend we will run into
    # a huge bug in Tcl 8.0 which ends up deleting values in the env
    # array. This bug has been fixed in tcl8.1 but not in 8.0.4!

    foreach path [XpUtils::splitpath $env(CLASSPATH)] {
	if {[file tail $path] == $tb_name} {

	    # If Jacl's jar file appears on the CLASSPATH before
	    # Tcl Blend's, the user would get a confusing error
	    # message while loading. Don't let this happen!

	    if {[info exists found_jacl]} {
		error "$jacl_name found on env(CLASSPATH) before $tb_name"
	    }

	    if {! [info exists found_tclblend]} {
		set found_tclblend $path
	    } else {
		if {$debug_loadtclblend} {
		    puts "Warning: multiple $tb_name files found on env(CLASSPATH), found at $found_tclblend then $path"
		}
	    }
	}

	if {[file tail $path] == $tj_name} {
	    if {! [info exists found_tcljava]} {
		set found_tcljava $path
	    } else {
		if {$debug_loadtclblend} {
		    puts "Warning: multiple $tj_name files found on env(CLASSPATH), found at $found_tcljava then $path"
		}
	    }
	}

	if {[file tail $path] == $jacl_name} {
	    if {! [info exists found_jacl]} {
		set found_jacl $path
	    }
	}	
    }

    if {$debug_loadtclblend} {
	if {[info exists found_jacl]} {
	    puts "found $jacl_name on env(CLASSPATH) at $found_jacl"
	}
	if {[info exists found_tcljava]} {
	    puts "found $tj_name on env(CLASSPATH) at $found_tcljava"
	}
	if {[info exists found_tclblend]} {
	    puts "found $tb_name on env(CLASSPATH) at $found_tclblend"
	}

	set saved_classpath $env(CLASSPATH)
    }

    # prepend the tclblend jar onto the CLASSPATH if needed.

    if {! [info exists found_tclblend]} {
	if {$debug_loadtclblend} {
	    puts "prepending ${tb_jar} onto env(CLASSPATH)"
	}

	XpUtils::prependpath env(CLASSPATH) $tb_jar
    }

    # prepend the tcljava jar onto the CLASSPATH if needed.

    if {! [info exists found_tcljava]} {
	if {$debug_loadtclblend} {
	    puts "prepending ${tj_jar} onto env(CLASSPATH)"
	}

	XpUtils::prependpath env(CLASSPATH) $tj_jar
    }

    if {$debug_loadtclblend} {
	if {$saved_classpath != $env(CLASSPATH)} {
	  puts "before jar prepend env(CLASSPATH) was \"$env(CLASSPATH)\""
	  puts "after  jar prepend env(CLASSPATH) was \"$env(CLASSPATH)\""
        } else {
	  puts "before shared lib load, env(CLASSPATH) was \"$env(CLASSPATH)\""
        }
    }

    # Load the tclblend native lib after the .jar files are on the CLASSPATH.
    # If loading of the shared libs fails try to figure out why it failed.

    # FIXME : removed until System.loadLibrary() is fixed
    #set extdbg _g
    set extdbg ""

    if {[catch {XpUtils::iload -d $dir -extdbg "" tclblend} errMsg]} {
        error "\"XpUtils::iload -d $dir tclblend\" failed:\n $errMsg"
    }

    # See src/tcljava/tcl/lang/BlendExtension.java
    # for other places the version info is hardcoded

    package provide java 1.4.7

    # Delete proc from interp, if other interps do a package require
    # they will source this file again anyway

    rename loadtclblend {}
}

package ifneeded java 1.4.7 [list loadtclblend $dir]

# Return the char used to split up elements in a system path

namespace eval XpUtils {}

proc XpUtils::getPathSeparator { } {
    global tcl_platform

    # FIXME: Mac switch broken

    switch $tcl_platform(platform) {
        unix {
            set PATH_SEPARATOR :
        }
        windows {
	    set PATH_SEPARATOR \;
        }
        mac -
        default {
            error "unsupported platform \"$tcl_platform(platform)\""	
        }
    }

    return $PATH_SEPARATOR
}


proc XpUtils::expandSharedLibName { shortname {extdbg DEFAULT} } {
    global tcl_platform

    # FIXME: Mac switch broken

    switch $tcl_platform(platform) {
        unix {
	    set SHLIB_PREFIX lib
	    set SHLIB_SUFFIX [info sharedlibextension]
	    if {$extdbg == "DEFAULT"} {
		set extdbg g
	    }
        }
        windows {
            set SHLIB_PREFIX {}
	    set SHLIB_SUFFIX [info sharedlibextension]
	    if {$extdbg == "DEFAULT"} {
		set extdbg d
	    }
        }
        mac -
        default {
            error "unsupported platform \"$tcl_platform(platform)\""	
        }
    }

    # extext is only appended to a shared lib name in debug mode
    if {! [info exists tcl_platform(debug)]} {
	set extdbg ""
    }

    return ${SHLIB_PREFIX}${shortname}${extdbg}${SHLIB_SUFFIX}
}

proc XpUtils::getTmpDir { } {
    global tcl_platform

    # FIXME: Mac switch broken

    switch $tcl_platform(platform) {
        unix {
            return /tmp
        }
        windows {
	    if {[info exists env(TEMP)]} {
		return $env(TEMP)
	    } else {
		set tmp C:/Temp
		if {! [info exists $tmp]} {
		    file mkdir $tmp
		    return $tmp
		}
	    }
        }
        mac -
        default {
            error "unsupported platform \"$tcl_platform(platform)\""	
        }
    }
}

# This command will return a system path as a Tcl list. A system
# path is a list of files or directories separated by a system
# define character like : or ;

proc XpUtils::splitpath { path } {
    set sep [XpUtils::getPathSeparator]

    set path_list [list]

    foreach p [split $path $sep] {
	if {$p != {}} {
	    lappend path_list $p
	}
    }

    return $path_list
}

# This command will append a value onto a path. This provides
# a handy way to avoid platform dependent nightmares!

proc XpUtils::appendpath { var elem } {
    set sep [XpUtils::getPathSeparator]
    uplevel 1 [list append $var $sep$elem]
}


# This command will prepend a value onto a path. This provides
# a handy way to avoid platform dependent nightmares!

proc XpUtils::prependpath { var elem } {
    set sep [XpUtils::getPathSeparator]

    set tmp [uplevel 1 [list set $var]]
    uplevel 1 [list set $var ${elem}${sep}]
    uplevel 1 [list append $var $tmp]
}


if {0} {

# shlibsearch { shlibs envvar searchdirs }

# shlibsearch {foo bob joe} VAR {/tmp /tmp/f}

{foo {/tmp/libfoo.so /tmp/f/libfoo.so}}
{bob {}}
{joe {libjoe.so}}



# This method could just search the searchdir but it could
# also go ahead and search the LD_LIBRARY_PATH or whatever
# is needed on a system!

    proc shlib_search { shlibs envvar searchdirs } {
	global env
	upvar debug_loadtclblend debug_loadtclblend

	if {[llength $shlibs] == 0} {
	    error "no shlib names provided"
	}
	
	# iterate over shlibs to set up the location array
	
	foreach shlib $shlibs {
	    if {$shlib == ""} {
		error "empty shlib name"
	    }
	    set shlibloc($shlib) ""
	}
	
	foreach dir $searchdirs {
	    if {$dir == {}} {
		continue
	    }
	    if {! [file isdirectory $dir]} {
		if {$debug_loadtclblend} {
		    puts "directory \"$dir\" from $envvar does not exist"
		}
		continue
	    }
	    
	    foreach shlib $shlibs {
		set file [file join $dir $shlib]
		
		if {[file exists $file]} {
		    if {$shlibloc($shlib) == ""} {
			set shlibloc($shlib) $file
		    } else {
			if {$debug_loadtclblend} {
			    puts "found duplicate $shlib on $envvar at\
				    \"$file\", first was at $shlibloc($shlib)"
			}
		    }
		}
	    }
	}
	
	foreach shlib $shlibs {
	    if {$shlibloc($shlib) == ""} {
		puts "could not find $shlib, you may need to add the\
                        directory where $shlib lives to your $envvar\
                        environmental variable."
	    } else {
		if {$debug_loadtclblend} {
		    puts "found $shlib on $envvar at \"$shlibloc($shlib)\"."
		}
	    }
	}
    }





    switch $tcl_platform(platform) {
	unix {
	    # on a UNIX box shared libs can be found using the
	    # LD_LIBRARY_PATH environmental variable or they can be
	    # defined a ldconfig config file somewhere. We are only
	    # able to check the LD_LIBRARY_PATH here.
	    
	    set VAR LD_LIBRARY_PATH
	    set shlibdir lib
	    
	    # of course HP does it differently
	    if {$tcl_platform(os) == "HP-UX"} {
		if {! [info exists env($VAR)]} {
                    set VAR SHLIB_PATH
		}
	    }
	    
	}
	windows {
	    # on a Windows box the PATH env var is used to find dlls
	    # look on the PATH and see if we can find tclblend.dll
	    
	    set VAR PATH
	    set shlibdir bin

	    if {$debug_loadtclblend} {
		puts "users should have directories like\
			C:\\jdk1.4\\jre\\bin and\
			C:\\jdk1.4\\jre\\bin\\client on the PATH."
	    }
	}
	mac -
	default {
	    error "unsupported platform \"$tcl_platform(platform)\""	
	}
    }





}

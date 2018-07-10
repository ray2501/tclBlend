# iload is an improved version of the load command
# iload will automatically load shared libraries by
# name on both Unix and Windows systems. It will also
# take into account debug extensions like ...g and ...d

# The command syntax is as follows
# iload ?-dirs dirlist? ?-extdbg ext? ?-nopath? libname ?packageName? ?interp?

# Search for a shared lib named ...tclblend... in $dir and on the path
#
# iload -d $dir tclblend

# Search for a shared lib with a funky debug extension (Java uses _g)
# This will load ...tclblend... or ...tclblend_g... in debug mode
#
# iload -d $dir -extdbg _g tclblend

# Search for a shared lib named ...tclblend... in $dir in . and on the path
#
# iload -dirs [list $dir .] tclblend

# Search for a shared lib named ...tclblend... in $dir only
#
# iload -d $dir -nopath tclblend

# Search for a shared lib named ...tclblend... on the path only
#
# iload tclblend

namespace eval XpUtils {}

set XpUtils::iload_debug 0

proc XpUtils::iload {args} {
    global tcl_platform env tcljava

    # FIXME : can this ever happen? If not remove it!
    if {[info exists tcljava(tcljava)] &&
	$tcljava(tcljava) == "jacl"} {
        # This can happend if iload is called from Jacl, do nothing
	return
    }

    set usage "iload ?-dirs dirlist? ?-extdbg ext? ?-nopath? libname ?packageName? ?interp?"

    if {[llength $args] == 0} {
	error $usage
    }

    # Iterate over the args looking for arguments that start with a -
    set pathsearch 1

    for {set i 0} {$i < [llength $args]} {incr i} {
	set arg [lindex $args $i]
	#puts "arg $i is \"$arg\""

	# Check for -dirs or any substring thereof
	if {[string first $arg -dirs] == 0} {

	    # error "iload -d" or  "iload -d libname"
	    if {($i + 1) >= ([llength $args] - 1)} {
		error $usage
	    }

	    # error "iload -d /tmp -d /foo libname"
	    if {[info exists dirs]} {
		error $usage
	    }

	    # error "iload -d -foo libname"
	    set nextarg [lindex $args [expr {$i + 1}]]
	    if {[string index $nextarg 0] == "-"} {
		error $usage
	    }

	    set dirs $nextarg
	    incr i
	    continue
	}

	# Check for -extdbg or any substring thereof
	if {[string first $arg -extdbg] == 0} {

	    # error "iload -extdbg" or  "iload -extdbg libname"
	    if {($i + 1) >= ([llength $args] - 1)} {
		error $usage
	    }

	    # error "iload -extdbg _g -extdbg _g libname"
	    if {[info exists extdbg]} {
		error $usage
	    }

	    set nextarg [lindex $args [expr {$i + 1}]]
	    set extdbg $nextarg
	    incr i
	    continue
	}

	
	# Check for -nopath or any substring thereof
	if {[string first $arg -nopath] == 0} {

	    # error "iload -nopath"
	    if {$i >= ([llength $args] - 1)} {
		error $usage
	    }

	    # error "iload -nopath -nopath libname"
	    if {$pathsearch == 0} {
		error $usage
	    }

	    set pathsearch 0
	    continue
	}

	# Check for -dirs -something
	if {[string first $arg -] == 0} {

	    # error "iload -nopath libname" (need -dirs if -nopath is given)
	    if {![info exists dirs]} {
		error $usage
	    }

	    set pathsearch 0
	    continue
	}


	# We must have reached an argument that
	# does not start with a - we can remove
	# the ones we already found!

	set args [lreplace $args 0 [expr {$i - 1}]]

	# We can not have more than 3 arguments
	# after the - options have been stripped

	# error "iload tclblend java interp foo"
	if {[llength $args] > 3} {
	    error $usage
	}

	if {![info exists dirs]} {
	    set dirs [list]
	}

	# error "iload -nopath libname" would have no place to search!
	if {$dirs == {} && $pathsearch == 0} {
	    error $usage
	}

	if {![info exists extdbg]} {
	    set extdbg DEFAULT
	}

	break
    }
    if {[llength $args] == 0} {
	# error "iload" : need shared lib name!
	error $usage
    }

    set sharedlib [lindex $args 0]
    set args [lreplace $args 0 0]

    if {$XpUtils::iload_debug} {
	puts "dirs are \{$dirs\}"
	puts "pathsearch is \"$pathsearch\""
	puts "sharedlib is \"$sharedlib\""
	puts "extdbg is \"$extdbg\""
	puts "args are \"$args\""
    }

    # Invoke the next stage of the iload process

    XpUtils::__iload_stage1 $dirs $pathsearch $sharedlib $extdbg $args
}





# This is a private method that provides a hook for testing
# the implementation of the iload command. It should never
# be called from user code!

# dirs is the list of directory arguments

# if pathsearch is true the system path will be searched. This will
# only happen if the given shared lib can not be found on the path!

# sharedlib is the "short" name of the shared lib (like tclblend)

# extdbg is the "short" name extension to use if in debug mode!

# extra_args can have from 0 to 2 args (packagename interp)

proc XpUtils::__iload_stage1 { dirs pathsearch sharedlib extdbg extra_args } {
    global tcl_platform

    if {$tcl_platform(platform) == "windows"} {
	# Expand the pathname in case it is something like
	# c:/Progra~1/Tcl/lib
	# Without this expansion we have problems loading a .dll

	set expanded_dirs [list]

	foreach dir $dirs {
	    lappend expanded_dirs [file attributes $dir -longname]
	}

	set dirs $expanded_dirs
    }

    # Expand the sharedlibrary name, only pass a dbgx if we know we
    # want to set it to some value (like g, d, or _g)

    if {$extdbg == "DEFAULT"} {
	set sharedlib [XpUtils::expandSharedLibName $sharedlib]
    } else {
	set sharedlib [XpUtils::expandSharedLibName $sharedlib $extdbg]
    }

    # Search for the shared lib on the dirs the user passed in.

    set fullpath {}

    foreach dir $dirs {
	set fullpath [file join $dir $sharedlib]

	if {$XpUtils::iload_debug} {
	    puts "checking for fullpath \"$fullpath\""
	}

	if {[file exists $fullpath]} {
	    break
	} else {
	    set fullpath {}
	}
    }

    if {$fullpath == {} && !$pathsearch} {
	error "could not find \"$sharedlib\" on path \{$dirs\}"
    } elseif {$fullpath == {}} {
	set fullpath $sharedlib
    }

    if {$XpUtils::iload_debug} {
	puts "fullpath is \"$fullpath\""
    }

    XpUtils::__iload_stage2 $fullpath $extra_args
}


# This is a private method that provides a hook for testing
# the implementation of the iload command. It should never
# be called from user code!

# fullpath is the fully qualified name of the shared lib.
# at this point it could be a fully qualified name like
# /tmp/libfoo.so or it could be libfoo.so. If the iload
# command was given the -nopath flag, then this argument
# would always be fully qualified.

# extra_args can have from 0 to 2 args (packagename interp)

proc XpUtils::__iload_stage2 { fullpath extra_args } {
    if {$XpUtils::iload_debug} {
	puts "XpUtils::__iload_stage2 \"$fullpath\" \{$extra_args\}"
    }
    uplevel #0 [list load $fullpath] $extra_args 
}


# tcl_platform(debug) does not seem to get set under UNIX!

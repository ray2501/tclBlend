# grid.tcl --
#
#	Emulates the TK "grid" command using the AWT GridBagLayout
#	class.
#
# Copyright (c) 1997 Sun Microsystems, Inc.
#
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL
# WARRANTIES.
# 
# RCS: @(#) $Id: grid.tcl,v 1.1 1999/05/08 05:51:54 dejong Exp $


# Save the constants from the GridBagLayout class into an array
# so that we don't need to look them up each time.

foreach c {RELATIVE REMAINDER NONE BOTH HORIZONTAL VERTICAL CENTER
    NORTH NORTHEAST EAST SOUTHEAST SOUTH SOUTHWEST WEST NORTHWEST} {

    set gridConst($c) [java::field java.awt.GridBagConstraints $c]
}

# java_grid --
#
#	This command is similar to the "grid" command in TK. However,
#	it's syntax (as well as sematics) are not fully compatible
#	with TK.
#
# Arguments:
#	master		Handle to an AWT container widget.
#	slave		Handle to an AWT component widget to put in the
#			master
#	args		A variable number of option-value pairs that
#			controls the slave's appearance inside the master.

proc java_grid {master slave args} {

    global gridConst
    if {[expr [llength $args] %2] != 0} {
	error "wrong # args"
    }
    
    set constr [java::new java.awt.GridBagConstraints]
    set insets [java::field $constr insets]

    foreach {opt val} $args {
	set x [string toupper $val]
	if [info exists gridConst($x)] {
	    set val $gridConst($x)
	}
	case $opt {
	    -x {
		java::field $constr gridx $val
	    }
	    -y {
		java::field $constr gridy $val
	    }
	    -width {
		java::field $constr gridwidth $val
	    }
	    -height {
		java::field $constr gridheight $val
	    }
	    -fill {
		java::field $constr fill $val
	    }
	    -ipadx {
		java::field $constr ipadx $val
	    }
	    -ipadx {
		java::field $constr ipady $val
	    }
	    -weightx {
		java::field $constr weightx $val
	    }
	    -weighty {
		java::field $constr weighty $val
	    }
	    -anchor {
		java::field $constr anchor $val
	    }
	    -padx {
		java::field $insets left $val
		java::field $insets right $val
	    }
	    -pady {
		java::field $insets top $val
		java::field $insets bottom $val
	    }
	    -insettop {
		java::field $insets top $val
	    }
	    -insetbottom {
		java::field $insets bottom $val
	    }
	    -insetleft {
		java::field $insets left $val
	    }
	    -insetright {
		java::field $insets right $val
	    }
	    default {
		error "unknown optopn $opt"
	    }
	}
    }

    set layout [$master getLayout]
    if {! [java::instanceof $layout java.awt.GridBagLayout]} {
	set layout [java::new java.awt.GridBagLayout]
	$master setLayout $layout
    } else {
	#if it is a GridBagLayout cast it up from LayoutManager
	set layout [java::cast java.awt.GridBagLayout $layout]
    }

    $layout setConstraints $slave $constr
    $master {add java.awt.Component} $slave
}


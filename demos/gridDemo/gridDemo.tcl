# gridDemo
#
#	This file demonstrates how to use the java::* commands to
#	create GUI components and handle user events.
#
# Copyright (c) 1997 Sun Microsystems, Inc.
#
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL
# WARRANTIES.
# 
# RCS: @(#) $Id: gridDemo.tcl,v 1.1 1999/05/08 05:51:54 dejong Exp $

package require java
source grid.tcl

# setButtons --
#
#	Enable/disable the buttons depending on whether an entry
#	has been selected in the corresponding listbox.

proc setButtons {} {
    global list1 list2 b1 b2

    if {[$list1 getSelectedIndex] == -1} {
	$b1 setEnabled false
    } else {
	$b1 setEnabled true
    }

    if {[$list2 getSelectedIndex] == -1} {
	$b2 setEnabled false
    } else {
	$b2 setEnabled true
    }
}

# moveEmployee --
#
#	Move an employee from the $from listbox to the $to listbox

proc moveEmployee {from to} {
    global list1 status
    set idx [$from getSelectedIndex]
    if {$idx != -1} {
	if {$from == $list1} {
	    set str [$from getItem $idx]
	    if {$str == "Bozo the Clown"} {
		setStatus "Essential employee Bozo cannot be fired"
		return
	    }
	}

	set str [$from getItem $idx]
	$from delItem $idx
	$to add $str
    }
    setButtons
}

# setStatus --
#
#	Set a message on the status bar. The status message will be
#	cleared after 200 milliseconds.

proc setStatus {msg} {
    global status afterId

    $status setText $msg

    if [info exists afterId] {
	after cancel $afterId
    }
    set afterId [after 2000 {$status setText ""; catch {unset afterId}}]
}


#
# Create the lisboxes and buttons; lay them out using the "grid" command.
#


set f [java::new java.awt.Frame]

set list1 [java::new java.awt.List]
set list2 [java::new java.awt.List]
$list1 setMultipleMode false
$list2 setMultipleMode false

set lab1 [java::new java.awt.Label "Employees to keep: "]
set lab2 [java::new java.awt.Label "Employees to fire: "]
set status [java::new java.awt.Label ""]

set b1 [java::new java.awt.Button ==>]
set b2 [java::new java.awt.Button <==]
set bx [java::new java.awt.Button "   Exit   "]

java_grid $f $lab1 -x 0 -y 0 -anchor WEST -padx 4 -pady 2
java_grid $f $lab2 -x 2 -y 0 -anchor WEST -padx 4 -pady 2

java_grid $f $list1 -x 0 -y 1 -width 1 -height 2 -padx 4 -pady 2 -fill both \
    -weightx 1.0
java_grid $f $list2 -x 2 -y 1 -width 1 -height 2 -padx 4 -pady 2 -fill both \
    -weightx 1.0

java_grid $f $b1 -x 1 -y 1 -fill none -pady 10 -weighty 1.0 -anchor SOUTH
java_grid $f $b2 -x 1 -y 2 -fill none -pady 10 -weighty 1.0 -anchor NORTH

java_grid $f $status -x 0 -y 4 -width 3 -fill both -pady 4 -padx 4 -anchor southeast
java_grid $f $bx -x 2 -y 3 -fill none -pady 4 -padx 4 -anchor southeast

#
# Add entries to the "Employees to keep" listbox.
#

set employees {
    {Bill Clinton}
    {Larry Ellison}
    {Bill Gates}
    {Andy Grove}
    {Scott McNeily}
    {Bozo the Clown}
}

foreach x $employees {
    $list1 add $x
}

$f setLocation 100 100
$f pack
$f show
$f toFront
$f setTitle "Downsize Assistant"

#
# Event bindings
#

java::bind $list1 actionPerformed "moveEmployee $list1 $list2"
java::bind $list2 actionPerformed "moveEmployee $list2 $list1"
java::bind $b1    actionPerformed "moveEmployee $list1 $list2"
java::bind $b2    actionPerformed "moveEmployee $list2 $list1"
java::bind $bx    actionPerformed "set done yes"

java::bind $list1 itemStateChanged "setButtons"
java::bind $list2 itemStateChanged "setButtons"

java::bind $f windowClosing "set done yes"

setButtons

#
# Loop forever to handle GUI events
#

vwait done

#
# We're done with the window. Dispose it.
#

$f dispose

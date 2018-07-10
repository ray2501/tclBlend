# StopWatchPackage.java --
#
# Copyright (c) 1997 Sun Microsystems, Inc.
#
#   This file contains Tcl code to interface Tcl with the
#   StopWatchThread class.
#
# See the file "license.terms" for information on usage and
# redistribution of this file, and for a DISCLAIMER OF ALL
# WARRANTIES.
# 
# RCS: @(#) $Id: swCmd.tcl,v 1.1.1.1 1998/10/14 21:09:23 cvsadmin Exp $


# This Tcl Demo shows how to use the Reflection API to access java
# class methods from within Tcl.  In this case we don't have to load
# any package into tcl.  As long as the StopWatchThread is in our
# classpath, you can access it.

# swNew --
#
#   starts a new stopwatch at time 0 and returns the watch's id

proc swNew {} {
    # create an instance of the thread class with handle "id"

    set id [java::new StopWatchThread]

    # start running the thread

    $id start

    return $id
}

# swSet --
#
#   sets the stopwatch to the specified time and starts counting down

proc swSet {id newTime} {

    return [$id setTime $newTime]

}

# swStop --
#
#   halts the stopwatch and returns current time

proc swStop {id} {

    return [$id stopCountdown]

}

# swResume --
#
#   starts counting down and returns current time

proc swResume {id} {

    return [$id resumeCountdown]

}

# swDie --
#
#   kills the stopwatch

proc swDie {id} {

    return [$id die]

}


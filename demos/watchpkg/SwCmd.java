/*
 * SwCmd.java
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 *   This file implements the SwCmd class.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

import tcl.lang.*;

/**
 * This class implements the "sw" command in StopWatchPackage.
 */

class SwCmd implements Command {
    
    StopWatchThread swThread = null;  // GUI and internal time monitor

    // This procedure is invoked to process the "sw" Tcl command
    // -- it takes the following subcommands:  new, set, stop, resume,
    //    and die
    //
    // sw new       --> starts a new stopwatch at time 0
    //
    // sw set [sec] --> sets the stopwatch to specified time,
    //                  starts counting down
    //
    // sw stop      --> returns current time, halts the stopwatch
    //
    // sw resume    --> returns current time, starts counting down
    //
    // sw die       --> kills the stopwatch (by hiding the frame and
    //                     stopping the StopWatchThread)

    public void cmdProc(Interp interp, TclObject[] objv)
	    throws TclException {

	if (objv.length < 2) {
	    throw new TclNumArgsException(interp, 1, objv, 
		    "[new|set seconds|stop|resume|die]");
	}

	if (objv[1].toString().equals("new")) {
	    if (objv.length != 2) {
		throw new TclNumArgsException(interp, 2, objv, "");
	    }
	    if (swThread != null) {
		throw new TclException(interp, 
			"error:  stopwatch is already running");
	    }
	    interp.setResult(0);
	    swThread = new StopWatchThread();
	    swThread.start();	
	    return;
	}

	if (swThread == null) {
	    throw new TclException(interp, 
		    "error:  no stopwatch currently running");
	}

	if (objv[1].toString().equals("set")) {
	    if (objv.length != 3) {
		throw new TclNumArgsException(interp, 2, objv, "seconds");
	    }
	    swThread.setTime(TclInteger.get(interp, objv[2]));
	    return;
	}
	if (objv.length != 2) {
	    throw new TclNumArgsException(interp, 1, objv, 
		    "[set seconds|stop|resume|die]");
	}
	if (objv[1].toString().equals("stop")) {
	    int sec = swThread.stopCountdown();
	    interp.setResult(sec);
	    return;
	}
	if (objv[1].toString().equals("resume")) {
	    int sec = swThread.resumeCountdown();
	    interp.setResult(sec);
	    return;
	}
	if (objv[1].toString().equals("die")) {
	    swThread.die();
	    swThread = null;
	    return;
	}
	throw new TclException(interp, "bad sw option \""
                    + objv[1].toString() 
                    + "\": must be new, set, stop, resume, or die");
    }
}

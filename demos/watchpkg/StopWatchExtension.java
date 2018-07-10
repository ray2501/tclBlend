/*
 * StopWatchExtension.java --
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 *   This file implements the StopWatchExtension class.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

import tcl.lang.*;

/*
 * This class implements a Tcl extension package "StopWatchExtension". This
 * extension contains one Tcl command "sw". See the API documentation
 * of the tcl.lang.Extension class for details.
 */

public class StopWatchExtension extends Extension {

    /*
     * Create all the commands in the stopwatch package.
     */

    public void init(Interp interp) {
	interp.createCommand("sw", new SwCmd());
    }
}


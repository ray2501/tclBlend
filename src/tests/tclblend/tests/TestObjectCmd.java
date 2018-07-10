/* 
 * TestObjectCmd.java --
 *
 *	This file contains the implementation for the "testobject" command.
 *
 * Copyright (c) 1997 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: TestObjectCmd.java,v 1.1 1999/05/10 04:08:53 dejong Exp $
 */

package tests;
import tcl.lang.*;

public class TestObjectCmd implements Command {

/*
 * The following string array, constants, and option list define the valid
 * options this command.
 */

private static final String cmds[] = {
    "type",
    "convert",
    "shared"
};
private static final int OPT_TYPE = 0;
private static final int OPT_CONVERT = 1;
private static final int OPT_SHARED = 2;

/*
 *----------------------------------------------------------------------
 *
 * cmdProc --
 *
 *	This method implements the "testobject" command.
 *
 * Results:
 *	A standard Tcl result.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public void
cmdProc(
    Interp interp,		// The current Tcl interpreter.
    TclObject argv[])		// The arguments passed to the command.
throws
    TclException		// The standard Tcl exception.
{
    if (argv.length < 2) {
	throw new TclNumArgsException(interp, 1, argv, 
		"option ?arg arg ...?");
    }
    int index = TclIndex.get(interp, argv[1], cmds, "option", 0);
    switch (index) {
    case OPT_TYPE:
	if (argv.length != 3) {
	    throw new TclNumArgsException(interp, 2, argv, 
		    "object");
	}
	interp.setResult(argv[2].getInternalRep().getClass().getName());
	return;
    case OPT_CONVERT:
	if (argv.length != 4) {
	    throw new TclNumArgsException(interp, 2, argv, 
		    "object type");
	}
	if (argv[3].toString().compareTo("int") == 0) {
	    TclInteger.get(interp, argv[2]);
	}
	interp.setResult(argv[2]);
	return;
    case OPT_SHARED:
	if (argv.length != 3) {
	    throw new TclNumArgsException(interp, 2, argv, 
		    "object");
	}
	interp.setResult(argv[2].isShared());
	return;
    }
}

} // end TestObjectCmd


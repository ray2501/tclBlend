/*
 * TestcompcodeCmd.java --
 *
 *	This command is loaded into a test version of Tcl Blend and Jacl
 *	to perform regression tests to test the handling of completion
 *	code.
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 * RCS: @(#) $Id: TestcompcodeCmd.java,v 1.1 1999/05/10 04:09:00 dejong Exp $
 *
 */

package tcl.lang;

class TestcompcodeCmd implements Command {

/*
 *----------------------------------------------------------------------
 *
 * cmdProc --
 *
 *	Returns the given completion code and result.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	The interp's result object is changed to argv[2].
 *
 *----------------------------------------------------------------------
 */

public void
cmdProc(
    Interp interp,		// Current interpreter.
    TclObject argv[]) 		// Argument list.
throws
    TclException		// Standard Tcl exception.
{
    if (argv.length != 3) {
	throw new TclNumArgsException(interp, 1, argv, "code result");
    }
    int code = TclInteger.get(interp, argv[1]);
    interp.setResult(argv[2]);
    throw new TclException(code);
}

} // end TestcompcodeCmd


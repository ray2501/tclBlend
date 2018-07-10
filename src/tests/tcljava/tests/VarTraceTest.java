/* 
 * VarTraceTest.java --
 *
 *	This class is used to test the variable trace Interp interfaces.
 *
 * Copyright (c) 1997 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: VarTraceTest.java,v 1.1 1999/05/10 04:09:03 dejong Exp $
 */

package tests;
import tcl.lang.*;

public class VarTraceTest implements VarTrace {

public String script;

public StringBuffer varsCalled = new StringBuffer();

public VarTraceTest(String s) {
    script = s;
}

/*
 *----------------------------------------------------------------------
 *
 * traceProc --
 *
 *	This function gets called when a variable is accessed.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	The traceProc can cause arbitrary side effects. If a
 *	TclException is thrown, error message is stored in the result
 *	of the interpreter.
 *
 *----------------------------------------------------------------------
 */

public void
traceProc(
    Interp interp,		// Current interpreter.
    String part1,		// First part of the variable name.
    String part2,		// Second part of the var name. May be null.
    int flags)			// TCL.TRACE_READS, TCL.TRACE_WRITES or
				// TCL.TRACE_UNSETS (exactly one of these
				// bits will be set.)
throws
     TclException		// The traceProc may throw a TclException
				// to indicate an error during the trace.
{
    varsCalled.append(part1 + "(" + part2 + ") ");

    if (script.length() != 0) {
	interp.eval(script);
    }
}

} // end VarTraceTest


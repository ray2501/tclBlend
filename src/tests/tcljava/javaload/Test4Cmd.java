/*
 *  Test4.java
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: Test4Cmd.java,v 1.1 2006/01/31 00:49:21 mdejong Exp $
 *
 */

package tests.javaload;

import tcl.lang.*;

public class
Test4Cmd implements Command {
    public void cmdProc(Interp interp, TclObject argv[]) throws TclException {
	interp.setResult("test works");
    }
}


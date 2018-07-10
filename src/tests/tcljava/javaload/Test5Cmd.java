/*
 *  Test5.java
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: Test5Cmd.java,v 1.1 2006/02/08 23:53:47 mdejong Exp $
 *
 */

package tests.javaload;

import tcl.lang.*;

public class
Test5Cmd implements Command {
    public void cmdProc(Interp interp, TclObject argv[]) throws TclException {
	interp.setResult("test works");
    }
}


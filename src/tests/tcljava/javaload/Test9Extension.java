/*
 * Test9Extension.java
 *
 *    Test the loading of a class file w/ no specified package 
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: Test9Extension.java,v 1.1 1999/05/10 04:08:57 dejong Exp $
 *
 */

import tcl.lang.*; 

public class
Test9Extension extends Extension {
    public void init(Interp interp) {
	interp.createCommand("test9", new Test2Cmd());
    }
}


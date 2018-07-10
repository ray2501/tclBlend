/*
 * Test10Extension.java
 *
 *    Test the loading of a class file from a jar.
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: Test10Extension.java,v 1.1 1999/05/10 04:08:54 dejong Exp $
 *
 */

import tcl.lang.*; 

public class
Test10Extension extends Extension {
    public void init(Interp interp) {
	interp.setResult("Test10 Loaded");
    }
}


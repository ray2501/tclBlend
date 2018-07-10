/*
 * Test4Extension.java
 *
 *    Loads a class from a different classpath
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: Test4Extension.java,v 1.1 1999/05/10 04:08:55 dejong Exp $
 *
 */

import tcl.lang.*; 

public class
Test4Extension extends Extension {
    public void init(Interp interp) {
	interp.createCommand("test4", new tests.javaload.Test1Cmd());
    }
}


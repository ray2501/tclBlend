/*
 * SimpleExtension.java --
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 *   This file implements the SimpleExtension class.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: SimpleExtension.java,v 1.2 1999/05/08 23:27:42 dejong Exp $
 */

import tcl.lang.*; 

/**
 * This class implements a simple Tcl extension package "SimpleExtension".
 * This extension contains one Tcl command "sayhello".  See the API 
 * documentation of the tcl.lang.Extension class for details.
 */

public class SimpleExtension extends Extension {

    // Create all the commands in the Simple package. 

    public void init(Interp interp) {
	interp.createCommand("sayhello", new SayhelloCmd());
    }
}


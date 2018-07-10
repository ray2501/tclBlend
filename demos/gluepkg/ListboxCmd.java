/*
 * ListboxCmd.java
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: ListboxCmd.java,v 1.3 1999/08/27 23:19:21 mo Exp $
 *
 */

import tcl.lang.*;

/**
 * This class implements the "listbox" demo command.
 *
 * The role of the GUI is to take a list of TclObjects and add them to an
 * AWT List component.  Two Buttons are added; OK and Cancel.  When either
 * of the buttons are pressed, an event is generated and handled un the
 * callbacks defined in glue.tcl
 */

public class ListboxCmd implements Command {
  
    /**
     * cmdProc --
     *
     * This procedure is invoked to process the listbox command.
     * If no arguments are supplied then the listbox directory begins
     * in the current directory, otherwise it begins in the supplied dir.
     * If dir and file are valid the result is set to the valid absolute path.
     */

    public void cmdProc(Interp interp, TclObject[] objv)
            throws TclException {

	TclObject  result = null;   // The Tcl result if this command
        ListboxApp listbox;         // The List widget
	String     dirName = null;  // The dir to start with, if any
	String[]   args;            // Args passed to Listbox constructor
	String[]   items;           // List of selected list items

	if ((objv.length < 2)) {
	    throw new TclNumArgsException(interp, 1, objv, "arg ?arg...?");
	}

	args = new String[objv.length - 1];
	for (int i = 1; i < objv.length; i++) {
	    args[i-1] = objv[i].toString();
	}

	listbox = new ListboxApp(args);

	// Return the ListboxApp object we just created

	interp.setResult(
	    ReflectObject.newInstance(interp, ListboxApp.class, listbox));
    }
}

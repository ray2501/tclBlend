/* 
 * HoldRefCmd.java --
 *
 *      This file tests incrementing and decrementing a
 *      the ref count of a native object.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: HoldRefCmd.java,v 1.1 2002/12/21 04:05:08 mdejong Exp $
 */

package tests;

import tcl.lang.*;
import java.util.Vector;
import java.util.Enumeration;

public class HoldRefCmd implements CommandWithDispose {
    Vector holding = new Vector();

    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        if (objv.length != 2) {
            throw new TclNumArgsException(interp, 1, objv, "obj");
        }
        TclObject hold = objv[1];
        hold.preserve();
        holding.addElement(hold);
    }

    // Called when the command is deleted

    public void
    disposeCmd()
    {
        TclObject hold;
        Enumeration search;
        for (search = holding.elements(); search.hasMoreElements() ; ) {
            hold = (TclObject) search.nextElement();
            hold.release();
	}
    }
}

/* 
 * ListLengthCmd.java --
 *
 *      This file tests changing an argument object's internal
 *      rep to TclList.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: ListLengthCmd.java,v 1.1 2002/12/18 03:39:54 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class ListLengthCmd implements Command {
    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        if (objv.length != 2) {
            throw new TclNumArgsException(interp, 1, objv, "obj");
        }
        interp.setResult(TclList.getLength(interp, objv[1]));
        return;
    }
}

/* 
 * SameArgumentObjectsCmd.java --
 *
 *      Return 1 if each arguments to the command
 *      is the same TclObject.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: SameArgumentObjectsCmd.java,v 1.1 2002/12/31 05:37:17 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class SameArgumentObjectsCmd implements Command {
    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        if (objv.length < 3) {
            throw new TclNumArgsException(interp, 1, objv, "obj obj ?obj ...?");
        }
        TclObject obj = objv[1];
        boolean same = true;
        for (int i=2; i < objv.length; i++) {
            if (obj != objv[i]) {
                same = false;
                break;
            }
        }
        interp.setResult(same);
        return;
    }
}

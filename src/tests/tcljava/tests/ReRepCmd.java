/* 
 * ReRepCmd.java --
 *
 *      This file tests modification of an internal rep.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: ReRepCmd.java,v 1.1 2002/12/21 04:05:19 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class ReRepCmd implements Command {
    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        if (objv.length != 2) {
            throw new TclNumArgsException(interp, 1, objv, "obj");
        }
        TclObject obj = objv[1];
        TclObject str = TclString.newInstance(obj.toString());
        // preserve so that a CObject's ref count gets incremented
        obj.preserve();
        // CObject.dispose() will drop any native refrences
        // before switching to the new internal rep
        obj.setInternalRep(str.getInternalRep());
        // Does not effect CObject because it is now a TclString
        obj.release();
        return;
    }
}

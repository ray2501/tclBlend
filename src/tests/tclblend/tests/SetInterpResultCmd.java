/* 
 * SetInterpResultCmd.java --
 *
 *      Check overloaded setResult methods of an interp.
 *      These methods may have a optimized native implementation
 *      that sets a result directly in c.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: SetInterpResultCmd.java,v 1.1 2002/12/25 08:29:31 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class SetInterpResultCmd implements Command {
    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        if ((objv.length < 2) && (objv.length > 3)) {
            throw new TclNumArgsException(interp, 1, objv, "type ?arg?");
        }
        TclObject type = objv[1], arg = null;
        if (objv.length == 3)
            arg = objv[2];

        if (type.toString().equals("min_int")) {
            interp.setResult(Integer.MIN_VALUE);
        } else if (type.toString().equals("max_int")) {
            interp.setResult(Integer.MAX_VALUE);
        } else if (type.toString().equals("true")) {
            interp.setResult(true);
        } else if (type.toString().equals("false")) {
            interp.setResult(false);
        } else if (type.toString().equals("int")) {
            interp.setResult(TclInteger.get(interp, arg));
        } else {
            throw new TclException(interp, "unknown type \"" + type + "\"");
        }
    }
}

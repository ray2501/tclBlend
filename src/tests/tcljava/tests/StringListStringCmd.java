/* 
 * StringListStringCmd.java --
 *
 *      This file tests modification of an internal rep.
 *      The behavior we don't want is for TclBlend to
 *      convert the Tcl_Obj created for the list into
 *      a ref to the TclString. We only want to convert
 *      a Tcl_Obj from C into a ref.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: StringListStringCmd.java,v 1.1 2002/12/21 04:05:19 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class StringListStringCmd implements Command {
    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        String s = "1 2 3 4 5";
        TclObject obj = TclString.newInstance(s);
        TclList.getLength(interp, obj);
        TclString.append(obj, " 6 7 8 9 10");
        return;
    }
}

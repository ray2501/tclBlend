/* 
 * LostListCmd.java --
 *
 *      This file test the special cleanup queue for
 *      a CObject in Tcl Blend. A native Tcl_Obj
 *      is created for the TclList instance, if
 *      this object is never preserved or released,
 *      memory from the C side would not get released.
 *      There is a special cleanup queue inside the
 *      CObject class that will handle this by
 *      incrementing and decrementing the ref
 *      count of there sorts of objects when
 *      the method returns.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: LostListCmd.java,v 1.1 2002/12/21 04:05:09 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class LostListCmd implements Command {
    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        String s = "1 2 3 4 5";
        TclObject obj = TclString.newInstance(s);
        TclObject e1  = TclList.index(interp, obj, 0);
        TclObject e2  = TclList.index(interp, obj, 1);
        TclObject e3  = TclList.index(interp, obj, 2);
        TclObject e4  = TclList.index(interp, obj, 3);
        TclObject e5  = TclList.index(interp, obj, 4);
        return;
    }
}

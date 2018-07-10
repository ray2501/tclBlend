/* 
 * ReleaseNewListCmd.java --
 *
 *      When a list that was allocated in Java is released, it
 *      should not be released again by the cleanup queue.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: ReleaseNewListCmd.java,v 1.1 2002/12/21 04:05:19 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class ReleaseNewListCmd implements Command {
    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        TclObject obj = TclList.newInstance();
        TclList.append(interp, obj,
            TclString.newInstance("E1"));
        TclList.append(interp, obj,
            TclString.newInstance("E2"));
        TclList.append(interp, obj,
            TclString.newInstance("E3"));

        obj.release();
        return;
    }
}

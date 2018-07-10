/* 
 * ArgConvertCmd.java --
 *
 *      Convert the internal rep of the argument to
 *      a TclDouble and then to a TclList. The
 *      conversion from TclDouble to TclList will
 *      cause an allocation for a new list. This
 *      should also break the bond between the
 *      native Tcl_Obj.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: ArgConvertCmd.java,v 1.1 2002/12/21 04:04:19 mdejong Exp $
 */

package tcl.lang;

public class ArgConvertCmd implements Command {
    CObject saved = null;

    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        if (objv.length > 2) {
            if (saved == objv[1].getInternalRep() &&
                    saved.onCleanupQueue == false) {
                throw new TclException(interp, "passed in deallocated CObject");
            }
            interp.setResult("checked");
        } else {
            TclDouble.get(interp, objv[1]); // Convert to TclDouble
            TclList.getLength(interp, objv[1]); // Convert to TclList

            saved = (CObject) objv[1].getInternalRep();
            if (!saved.onCleanupQueue)
                throw new TclException(interp, "not added to cleanup queue");
            interp.setResult("converted");
        }
    }
}

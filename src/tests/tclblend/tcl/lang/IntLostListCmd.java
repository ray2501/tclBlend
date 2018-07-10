/* 
 * IntLostListCmd.java --
 *
 *      Check internal TclBlend variables to
 *      ensure that TclList objects created
 *      in a Java method are being added
 *      to the special cleanup queue,
 *      that they are not being removed
 *      from this queue too early, and
 *      that they are being removed when
 *      the method returns.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: IntLostListCmd.java,v 1.1 2002/12/21 04:04:19 mdejong Exp $
 */

package tcl.lang;

public class IntLostListCmd implements Command {
    CObject saved = null;

    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        // Pass an argument to this method to check the
        // cleanup status of the saved CObject
        if (objv.length == 2) {
            if (saved == null)
                interp.setResult(-1);
            else
                interp.setResult(saved.onCleanupQueue?1:0);
            saved = null;
            return;
        }

        String s = "1 2 3 4 5";
        TclObject obj = TclString.newInstance(s);

        // Create native Tcl_Obj and add to cleanup queue
        TclList.getLength(interp, obj);

        CObject cobj = (CObject) obj.getInternalRep();
        if (!cobj.onCleanupQueue)
            throw new TclException(interp, "not added to cleanup queue");

        interp.eval("java::isnull java0x0");

        if (!cobj.onCleanupQueue)
            throw new TclException(interp, "removed from cleanup queue too soon");

        saved = cobj;
        interp.setResult("saved");
        return;
    }
}

/* 
 * ListReplaceCmd.java --
 *
 *      This file tests replacement of elements in a TclList.
 *      The list created from a string is not copied so
 *      only the newely allocated list CObject will need
 *      to be added to the cleanup queue. The list created
 *      from an argument will duplicate the native object
 *      and the new list will need to be cleaned up. An
 *      empty list will need to be deallocated too.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: ListReplaceCmd.java,v 1.2 2003/01/09 02:15:40 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class ListReplaceCmd implements Command {
    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        TclObject obj;

        if (objv.length == 2) {
            obj = objv[1];
            if (obj.isShared())
                obj = obj.duplicate();
        } else if (objv.length == 3) {
            obj = TclList.newInstance();
            TclList.append(interp, obj, TclString.newInstance("UNO"));
            TclList.append(interp, obj, TclString.newInstance("2"));
            TclList.append(interp, obj, TclString.newInstance("3"));
            TclList.append(interp, obj, TclString.newInstance("4"));
            TclList.append(interp, obj, TclString.newInstance("5"));
        } else {
            obj = TclString.newInstance("1 2 3 4 5");
        }

        TclObject[] arr = {TclString.newInstance("two"),
                       TclString.newInstance("three"),
                       TclString.newInstance("four")};

        TclList.replace(interp, obj, 1, 3, arr, 0, 2);
        return;
    }
}

/* 
 * RuntimeExceptionCmd.java --
 *
 *      Raise a RuntimeException in a Java command proc.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: RuntimeExceptionCmd.java,v 1.1 2002/12/30 22:49:24 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class RuntimeExceptionCmd implements Command {
    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        throw new NullPointerException("RuntimeExceptionCmd");
    }
}

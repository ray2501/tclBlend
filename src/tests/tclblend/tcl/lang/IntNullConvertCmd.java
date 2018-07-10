/* 
 * IntNullConvertCmd.java --
 *
 *      Create two different TclObjects that have
 *      the same CObject internal rep. Then,
 *      try to convert them both to ReflectObjects.
 *      This is a really twisted case, but the
 *      makeRef method of the CObjet class should
 *      not crash when running into this.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: IntNullConvertCmd.java,v 1.1 2002/12/31 05:22:16 mdejong Exp $
 */

package tcl.lang;

public class IntNullConvertCmd implements Command {
    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        if (objv.length != 2) {
            throw new TclNumArgsException(interp, 1, objv, "java_null_string");
        }

        // This should be a CObject, with a string rep of "java0x0"
        TclObject nullstr = objv[1];

        if (!nullstr.toString().equals("java0x0")) {
            throw new TclRuntimeError("should be string \"java0x0\"");
        }
        if (!(nullstr.getInternalRep() instanceof CObject)) {
            throw new TclRuntimeError("should be CObject");
        }

        TclObject dup = new TclObject(nullstr.getInternalRep().duplicate());

        Object ret1 = ReflectObject.get(interp, nullstr);
        // This second call will try to convert the internal CObject
        // into a ref to a Java object type, but the earlier call to get()
        // already did that. The best we can do here is just to free the
        // internal rep from the first call and move on.
        Object ret2 = ReflectObject.get(interp, dup);

        if ((ret1 == null) && (ret2 == null))
            interp.setResult("ok");
        else
            interp.setResult("ERROR");
    }
}

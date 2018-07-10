/* 
 * JavaLoadTclBlend.java --
 *
 *      This file tests loading of Tcl Blend into a JVM.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: JavaLoadTclBlend.java,v 1.2 2002/07/22 09:27:20 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class JavaLoadTclBlend {
    public static void main(String[] args) throws Exception {
        Interp interp = null;

        try {
            interp = new Interp();
            interp.eval("expr {1 + 2}");
            if (!interp.getResult().toString().equals("3"))
                System.exit(-1);
        } finally {
            if (interp != null)
                interp.dispose();
        }
        System.exit(0);
    }
}


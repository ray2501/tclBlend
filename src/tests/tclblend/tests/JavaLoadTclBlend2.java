/* 
 * JavaLoadTclBlend2.java --
 *
 *      This file tests loading of Tcl Blend into a JVM.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: JavaLoadTclBlend2.java,v 1.1 2002/07/22 09:27:20 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class JavaLoadTclBlend2 {
    public static void main(String[] args) throws Exception {
        Interp interp1 = null;
        Interp interp2 = null;

        try {
            interp1 = new Interp();
            interp1.eval("expr {1 + 2}");
            if (!interp1.getResult().toString().equals("3"))
                System.exit(-1);
            interp2 = new Interp();
            interp2.eval("expr {1 + 2}");
            if (!interp2.getResult().toString().equals("3"))
                System.exit(-1);
        } finally {
            if (interp1 != null)
                interp1.dispose();
            if (interp2 != null)
                interp2.dispose();
        }
        System.exit(0);
    }
}


/* 
 * JavaThreadLoadTclBlend.java --
 *
 *      This class implements a thread that uses Tcl Blend.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: JavaThreadLoadTclBlend.java,v 1.2 2002/07/22 09:27:20 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class JavaThreadLoadTclBlend implements Runnable {
    final boolean debug = false;

    public void run() {
        Interp interp = null;

        try {
            if (debug)
                System.out.println("run()");
            interp = new Interp();
            interp.eval("expr {1 + 2}");
            String num = interp.getResult().toString();
            if (!num.equals("3"))
                System.exit(-1);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            if (interp != null) {
                if (debug) {
                    System.out.println("now to dispose of interp");
                }
                interp.dispose();
                if (debug) {
                    System.out.println("interp dispose finished");
                }
            }
        }
        if (debug)
            System.out.println("run() finished");
        return; // Thread dies at this point
    }
}


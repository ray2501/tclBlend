/* 
 * JavaEval.java --
 *
 *      This class creates a Tcl interpreter, evaluates a command,
 *      and returns the result as a Java String.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: JavaEval.java,v 1.3 2005/07/19 23:10:55 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class JavaEval {
    public static String eval(String cmd) throws Exception {
        Interp interp = null;
        String result = null;
        try {
            interp = new Interp();
            interp.eval(cmd);
            result = interp.getResult().toString();
        } finally {
            if (interp != null)
                interp.dispose();
        }
        return result;
    }

    public static String eval(Interp interp, String cmd) throws Exception {
        interp.eval(cmd);
        String result = interp.getResult().toString();
        return result;
    }

    public static void main(String[] args) throws Exception {
        String cmd = null, expected = null, result;
        if (args.length == 0) {
            cmd = "";
        } else if (args.length == 1) {
            cmd = args[0];
        } else if (args.length == 2) {
            cmd = args[0];
            expected = args[1];
        } else {
            System.out.println("Wrong # args : should be \"cmd ?expected?\"");
            System.exit(2);
        }
        result = eval(cmd);
        if (expected != null) {
            if (result.compareTo(expected) != 0) {
                System.out.println("result mismatch");
                System.exit(3);
            } else {
                System.exit(0); // Result matched expected result
            }
        }
        System.exit(1); // We expect the script to call exit when not matching
    }
}

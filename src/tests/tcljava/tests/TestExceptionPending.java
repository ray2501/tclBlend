/* 
 * TestExceptionPending.java --
 *
 *      This file tests interp APIs that deal with raising
 *      exceptions in Java APIs. These may involve pending
 *      exceptions from commands that are evaluated.
 *
 * Copyright (c) 2006 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: TestExceptionPending.java,v 1.1 2006/07/26 20:55:27 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class TestExceptionPending {

    public static boolean pending1() {
        // Create a new Interpreter and then
        // invoke Interp.eval(), this should
        // raise a Java exception. This test
        // checks that an interpreter is created
        // with the proper flags.

        Interp interp = new Interp();

        try {
            interp.eval("unknown_command");
        } catch (TclException te) {
            return true;
        } finally {
            interp.dispose();
        }
        return false;
    }

    public static boolean pending2(Interp interp) {
        // Invoke Interp.eval(), this should
        // raise a Java exception because there
        // is no command with the given name.
        // This test checks that invoking the
        // Interp.eval() API from Java will
        // raise the exception as expected.

        try {
            interp.eval("unknown_command");
        } catch (TclException te) {
            return true;
        }

        return false;
    }

    public static boolean pending3(Interp interp) {
        // Invoke Interp.eval(), this should raise
        // a Java exception because there is a
        // runtime error since the variable in
        // question is not set.

        try {
            interp.eval("set unknown_global_variable");
        } catch (TclException te) {
            return true;
        }

        return false;
    }

    public static boolean pending4(Interp interp) {
        // Throw a Java exception using a test
        // command implemented in Java. This
        // exception should be propagated
        // up the stack to this caller.

        try {
            interp.eval("jtest tclexception");
        } catch (TclException te) {
            return true;
        }

        return false;
    }

    public static boolean pending5(Interp interp) {
        // Throw a Java exception using a test
        // command implemented in Java. This
        // exception should be propagated
        // up the stack to this caller. This
        // excetpion is not derived from
        // TclException.

        try {
            interp.eval("jtest npe");
        } catch (TclException npe) {
            // No-op
        } catch (NullPointerException npe) {
            return true;
        }

        return false;
    }

}


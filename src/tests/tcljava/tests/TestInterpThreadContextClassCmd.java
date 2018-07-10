/* 
 * TestInterpThreadContextClassCmd.java --
 *
 *      This file tests changing an argument object's internal
 *      rep to TclList.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: TestInterpThreadContextClassCmd.java,v 1.2 2006/04/13 07:36:51 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class TestInterpThreadContextClassCmd
    implements Command, Runnable
{
    static boolean done;
    static boolean ok;

    public void 
    cmdProc(
	Interp interp,
	TclObject[] objv)
    throws TclException
    {
        if (objv.length != 1) {
            throw new TclNumArgsException(interp, 1, objv, "");
        }

        /* Create a new Thread and setup a context loader for it */

        Thread t = new Thread(this);

        done = false;
        ok = false;

        // Setup specific class loader that should be used *before*
        // the TclClassLoader when searching for resources. This
        // loader uses the system loader as a parent.

        t.setContextClassLoader(
            new TestInterpThreadClassLoader(
                ClassLoader.getSystemClassLoader()
            )
        );
        t.setName("TestInterpThread");
        t.start();

        while (!done) {
            try {
            Thread.yield();
            Thread.sleep(100);
            } catch (Exception e) {}
        }

        if (ok) {
            interp.setResult("OK");
        } else {
            interp.setResult("NOPE");
        }
        return;
    }

    public void run() {
        try {
        //System.out.println("HELLO FROM THREAD");

        Interp interp = new Interp();
        ClassLoader loader = interp.getClassLoader();

        Class c = null;
        try {
            c = loader.loadClass("TestInterpThreadUnknown");
        } catch (ClassNotFoundException e) {
        } catch (PackageNameException e) {
        }

        if (c != null) {
            ok = true;
            //System.out.println("LOADED " + c.getName() + " IN THREAD");
        }

        interp.dispose();

        //System.out.println("GOODBYE FROM THREAD");
        } finally {
        done = true;
        }
    }
}


/* 
 * JavaThreadsLoadTclBlend.java --
 *
 *      This file tests loading of Tcl Blend into 2 Java threads.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: JavaThreadsLoadTclBlend.java,v 1.1 2002/07/20 05:36:54 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class JavaThreadsLoadTclBlend {
    public static void main(String[] args) throws Exception {
        Thread thr;
        thr = new Thread(new JavaThreadLoadTclBlend());
        thr.start();
        Thread.sleep(2000);
        thr = new Thread(new JavaThreadLoadTclBlend());
        thr.start();
        Thread.sleep(2000);
        System.exit(0);
    }
}

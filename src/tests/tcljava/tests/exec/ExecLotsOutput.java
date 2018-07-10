/*
 * ExecLotsOutput.java --
 *
 * jacl/tests/exec/ExecLotsOutput.java
 *
 * Test exec implementation for a process that writes lots of
 * data to both stdout and stderr.
 *
 * Copyright (c) 1998 by Moses DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: ExecLotsOutput.java,v 1.1 2006/04/05 23:03:39 mdejong Exp $
 *
 */


package tests.exec;

public class ExecLotsOutput {
  public static void main(String[] argv) {
    for (int i=0; i < 1000; i++) {
        System.out.print("OOO");
        System.err.print("EEE");
    }
    System.exit(0);
  }
}


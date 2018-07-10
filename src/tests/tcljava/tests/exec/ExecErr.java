/*
 * ExecErr.java --
 *
 * jacl/tests/exec/ExecErr.java
 *
 * Copyright (c) 1998 by Moses DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: ExecErr.java,v 1.1 1999/05/10 04:09:04 dejong Exp $
 *
 */

package tests.exec;

public class ExecErr {
  public static void main(String[] argv) {
    System.out.println("!stdout!");
    System.err.println("!stderr!");
    System.exit(-1);
  }
}


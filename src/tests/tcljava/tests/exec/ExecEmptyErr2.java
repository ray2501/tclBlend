/*
 * ExecEmptyErr2.java --
 *
 * jacl/tests/exec/ExecEmptyErr2.java
 *
 * Copyright (c) 1998 by Moses DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: ExecEmptyErr2.java,v 1.1 1999/05/10 04:09:03 dejong Exp $
 *
 */


package tests.exec;

public class ExecEmptyErr2 {
  public static void main(String[] argv) {
    System.out.println("!stdout!");
    System.exit(-1);
  }
}


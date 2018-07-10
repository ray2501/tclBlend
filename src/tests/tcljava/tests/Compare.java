/* 
 * Compare.java --
 *
 * tcljava/tests/Compare.java
 *
 * Copyright (c) 1998 by Moses DeJong.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: Compare.java,v 1.1 1999/05/10 04:09:01 dejong Exp $
 *
 */


package tests;

import java.util.*;

public class Compare {
  private Hashtable thing = new Hashtable();

  public Compare() {}

  public void empty() {
    thing = null;
  }

  public Object get1() {
    return thing;
  }

  public Dictionary get2() {
    return thing;
  }

  public Hashtable get3() {
    return thing;
  }

  public boolean compare(Object o1, Object o2) {
    return (o1 == o2);
  }

  public static void main(String[] argv) {
    Compare c = new Compare();
    Object r1 = c.get1();
    Dictionary r2 = c.get2();
    Hashtable r3 = c.get3();

    System.out.println("(r1 == r2) is " + (r1 == r2));
    System.out.println("(r2 == r3) is " + (r2 == r3));
  }

}


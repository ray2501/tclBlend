/*
 * MethodInvoker5.java --
 *
 * tcljava/tests/signature/MethodInvoker5.java
 *
 * Copyright (c) 1998 by Moses DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: MethodInvoker5.java,v 1.2 2002/12/27 14:33:20 mdejong Exp $
 *
 */

package tests.signature;

import java.util.*;

public class MethodInvoker5 {

  public static String call(A obj) {
    return "A";
  }

  public static String call(I obj) {
    return "I";
  }

  public static String call(I2 obj) {
    return "I2";
  }

  
  public static interface I {}
  public static interface I2 extends I {}
  public static interface I3 extends I2 {}

  private static class A {}
  private static class B implements I {}
  private static class C implements I2 {}
  private static class D implements I3 {}
  
  
  public static I getI() {
    return new B();
  }

  public static I2 getI2() {
    return new C();
  }

  public static I3 getI3() {
    return new D();
  }


  //this test determines if the method lookup system
  //is correctly handling cases where interfaces
  //are used. an overloaded function that
  //could accept and interface that it directly implements
  //or it could also take a superclass of B

  public static void main(String[] argv) {

    I i = getI();
    I2 i2 = getI2();
    I3 i3 = getI3();
    
    String s;
    
    s = call(i); //should return "I"
    p(s);
    
    s = call(i2); //should return "I2"
    p(s);

    s = call(i3); //should return "I2"
    p(s);

  }


  public static void p(String arg) {
    System.out.println(arg);
  }


}

/*
 * MethodInvoker6.java --
 *
 * tcljava/tests/signature/MethodInvoker6.java
 *
 * Copyright (c) 1998 by Moses DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: MethodInvoker6.java,v 1.2 2002/12/27 14:33:20 mdejong Exp $
 *
 */

package tests.signature;

public class MethodInvoker6 {

  private static interface I {}
  public static class A extends Object implements I {}
  
  public static String call(Object obj) {
    return "O";
  }
  public static String call(I obj) {
    return "I";
  }


  public static Object getO() {
    return new A();
  }

  public static A getA() {
    return new A();
  }


  //this test is used to check the exceptions to the
  //method resolver rules when the class Object is
  //involved. When resolving we consider a signature
  //with the Object class as less important then
  //an interface that matches the signature

  public static void main(String[] argv) {

    Object o = getO();
    A a = getA();

    String s;

    s = call( o ); //should return "O"
    p(s);

    s = call( a ); //should return "I"
    p(s);

  }

  public static void p(String arg) {
    System.out.println(arg);
  }

}

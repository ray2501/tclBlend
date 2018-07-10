/*
 * MethodInvoker3.java --
 *
 * tcljava/tests/signature/MethodInvoker3.java
 *
 * Copyright (c) 1998 by Moses DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: MethodInvoker3.java,v 1.1 1999/05/10 04:09:09 dejong Exp $
 *
 */


package tests.signature;

import java.util.*;


public class MethodInvoker3 {

  //instance methods

  public String call(Hashtable h) {
    return "Hashtable";
  }

  public String call(Vector v) {
    return "Vector";
  }

  public String call(Hashtable2 h) {
    return "Hashtable2";
  }

  public String call(Vector2 v) {
    return "Vector2";
  }

  public String call(Vector v, Hashtable h) {
    return "Vector+Hashtable";
  }

  public String call(Vector v, Vector v2) {
    return "Vector+Vector";
  }




  public static class Hashtable2 extends Hashtable {}
  public static class Hashtable3 extends Hashtable2 {}
  public static class Vector2 extends Vector {}


  public static Hashtable getHashtable() {
    return new Hashtable();
  }

  public static Hashtable2 getHashtable2() {
    return new Hashtable2();
  }

  public static Hashtable3 getHashtable3() {
    return new Hashtable3();
  }


  public static Vector getVector() {
    return new Vector();
  }

  public static Vector2 getVector2() {
    return new Vector2();
  }






  //test methods

  private void private_method() {}

  protected void protected_method() {}

  void package_method() {}

  public void public_method() {}

  public static void static_method() {}


  private void test_method() {}
  public  void test_method(int i) {}  


}

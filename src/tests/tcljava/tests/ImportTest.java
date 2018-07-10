/*
 * ImportTest.java --
 *
 * This class is used to regression test the java::import command.
 *
 * Copyright (c) 1999 by Moses DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: ImportTest.java,v 1.2 2006/04/13 07:36:51 mdejong Exp $
 *
 */

package tests;

import java.util.*;

public class ImportTest {
  private String type;

  // constructors

  public ImportTest() {
      type = "None";
  }

  public ImportTest(Hashtable h) {
      type = "Hashtable";
  }

  public ImportTest(Vector v) {
      type = "Vector";
  }

  // getType() is used to determine which constructor was called

  public String getType() {
      return type;
  }

  // instance methods

  public String call(Hashtable h) {
      return "Hashtable";
  }

  public String call(Vector v) {
      return "Vector";
  }

  // static methods

  public static String scall(Hashtable h) {
    return "Hashtable";
  }

  public static String scall(Vector v) {
      return "Vector";
  }

  // static class member

  public final static int ten = 10;

}


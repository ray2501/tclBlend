/*
 * InnerTest.java --
 *
 * This class is used to regression test inner class access using
 * the java package.
 *
 * Copyright (c) 2006 by Moses DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: InnerTest.java,v 1.1 2006/04/13 07:36:51 mdejong Exp $
 *
 */

package tests;

import java.util.*;

public class InnerTest {

  // constructor

  public InnerTest() {}

  // static class member
// FIXME: class member should be accessble
// from static inner classes? Test this.

  public final static int ten = 10;

  // static inner interface member

  public static interface InnerInterface1 {}

  // static inner class members

  public static class InnerClass1 {
      public InnerClass1() {}
  }

  public static class InnerClass2 implements InnerInterface1 {
      public InnerClass2() {}
  }

  // inner class has same fully qualfied name as toplevel class.

  public static class DupName {
      public DupName() {}
  }

  // Inner class inside static inner class

  public static class InnerClass3 {
      public InnerClass3() {}

      public static class InnerClass4 {
          public InnerClass4() {}
      }
  }

  public static
  InnerClass3.InnerClass4 getInnerClass4() {
      return new tests.InnerTest.InnerClass3.InnerClass4();
  }

}


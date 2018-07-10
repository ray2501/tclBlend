/*
 * InnerImport.java --
 *
 * Test use of Java's import statement with inner class names.
 *
 * Copyright (c) 2006 by Moses DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: InnerImport.java,v 1.1 2006/04/13 07:36:51 mdejong Exp $st.java,v 1.1 1999/08/09 08:52:36 mo Exp $
 *
 */

package tests;

import tests.InnerTest.InnerClass3;
import tests.InnerTest.InnerClass3.InnerClass4;

public class InnerImport {

  public static InnerTest.InnerClass3 newA() {
      return new InnerTest.InnerClass3();
  }

  public static InnerClass3 newB() {
      return new InnerClass3();
  }

  public static InnerClass4 newC() {
      return new InnerClass3.InnerClass4();
  }

  public static InnerClass4 newD() {
      return new InnerClass4();
  }

  public static InnerClass4 newE() {
      return new tests.InnerTest.InnerClass3.InnerClass4();
  }

}


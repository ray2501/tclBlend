/* 
 * JavaTestSub.java --
 *
 *	This file contains the JavaTestSub class used by java.test.
 *
 * Copyright (c) 1997 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: JavaTestSub.java,v 1.1 1999/05/10 04:09:02 dejong Exp $
 *
 */

package tests;

public class JavaTestSub extends JavaTest {

/*
 * We redefine some public fields in JavaTest. These fields are used
 * to test field signatures.
 */

/*
 * Instance fields.
 */

public int iint;
public String istr;

/*
 * Static fields.
 */

public static int sint;
public static String sstr;

}


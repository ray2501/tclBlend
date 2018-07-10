/*
 * Tester1Listener.java --
 *
 *	This is an event listener interface that test various capability
 *	of the AdaptorGen class.
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 * RCS: @(#) $Id: Tester1Listener.java,v 1.1 1999/05/10 04:09:00 dejong Exp $
 */

package tcl.lang;

import java.util.*;

/*
 * This is an event listener interface that test various capability of
 * the AdaptorGen class. AdaptorGen should be able to handle an
 * interface whose methods may take any arguments, return any type of
 * value and throw arbitrary exceptions.
 */

public interface Tester1Listener extends EventListener {

/*
 * A very conventional event method.
 */

public void method0(Tester1Event eventObj);

/*
 * A method with a non-conventional parameter.
 */

public void method1(boolean p0);

/*
 * A method with many non-conventional parameters.
 */

public void method2(boolean p0, byte p1, char p2, double p3, float p4,
    int p5, long p6, short p7, String p8);

/*
 * A method with a checked exception.
 */

public void method3(Tester1Event eventObj) throws SecurityException;

/*
 * A more complex method with checked exceptions.
 */

public Vector method4(Tester1Event eventObj, boolean b, int i, Object o)
    throws SecurityException, NullPointerException, NumberFormatException,
    IllegalAccessError;

/*
 * Methods that return values.
 */

public boolean method_boolean(Tester1Event eventObj);
public byte    method_byte   (Tester1Event eventObj);
public char    method_char   (Tester1Event eventObj);
public double  method_double (Tester1Event eventObj);
public float   method_float  (Tester1Event eventObj);
public int     method_int    (Tester1Event eventObj);
public long    method_long   (Tester1Event eventObj);
public short   method_short  (Tester1Event eventObj);
public Object  method_Object (Tester1Event eventObj);
public String  method_String (Tester1Event eventObj);
public Vector  method_Vector (Tester1Event eventObj);

} // end Tester1Listener



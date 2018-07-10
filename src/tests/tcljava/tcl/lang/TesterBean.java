/*
 * TesterBean.java --
 *
 *	This is a tester JavaBean that tests the java::bind command.
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 * RCS: @(#) $Id: TesterBean.java,v 1.1 1999/05/10 04:09:01 dejong Exp $
 */

package tcl.lang;

import java.util.*;


/*
 * This is a tester JavaBean that tests the java::bind command.
 */

public class TesterBean {

Tester1Listener t1;
Tester2Listener t2;

public TesterBean() {}

public synchronized void addTester1Listener(Tester1Listener l) {
    t1 = l;
}

public synchronized void removeTester1Listener(Tester1Listener l) {
    t1 = null;
}

public synchronized void addTester2Listener(Tester2Listener l) {
    t2 = l;
}

public synchronized void removeTester2Listener(Tester2Listener l) {
    t2 = null;
}

public void fire0() {
    Tester1Event evtObj = new Tester1Event(this);
    t1.method0(evtObj);
}

public void fire1(boolean b) {
    t1.method1(b);
}

public void fire2(boolean p0, byte p1, char p2, double p3, float p4,
        int p5, long p6, short p7, String p8) {
    t1.method2(p0, p1, p2, p3, p4, p5, p6, p7, p8);
}

public void fire3(Tester1Event eventObj) throws SecurityException
{
    t1.method3(eventObj);
}

public Vector fire4(Tester1Event eventObj, boolean b, int i, Object o)
    throws SecurityException, NullPointerException, NumberFormatException,
    IllegalAccessError
{
    return t1.method4(eventObj, b, i, o);
}

public boolean fire_boolean(Tester1Event eventObj) {
    return t1.method_boolean(eventObj);
}
public byte fire_byte(Tester1Event eventObj) {
    return t1.method_byte(eventObj);
}
public char fire_char(Tester1Event eventObj) {
    return t1.method_char(eventObj);
}
public double fire_double(Tester1Event eventObj) {
    return t1.method_double(eventObj);
}
public float fire_float(Tester1Event eventObj) {
    return t1.method_float(eventObj);
}
public int fire_int(Tester1Event eventObj) {
    return t1.method_int(eventObj);
}
public long fire_long(Tester1Event eventObj) {
    return t1.method_long(eventObj);
}
public short fire_short(Tester1Event eventObj) {
    return t1.method_short(eventObj);
}
public Object fire_Object (Tester1Event eventObj) {
    return t1.method_Object(eventObj);
}
public String fire_String (Tester1Event eventObj) {
    return t1.method_String(eventObj);
}
public Vector fire_Vector (Tester1Event eventObj) {
    return t1.method_Vector(eventObj);
}

} // end TesterBean


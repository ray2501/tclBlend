/* 
 * SuperTestClass.java --
 *
 *	Used to test the java::info command
 *
 * Copyright (c) 1997 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: SuperTestClass.java,v 1.1 1999/05/10 04:09:06 dejong Exp $
 */

package tests.javainfo;

public class SuperTestClass extends InfoTestClass {
    
    public int y;
    int x;
    public int a;
    public int j;
    public static int c;
    public static int i;

    public InfoTestClass[] tArray;
    public String[] sArray;
    public byte[] bArray;
    public char[] cArray;
    public int[] iArray;
    public Integer[] IArray;
    public float[] fArray;
    
    public SuperTestClass() {

        x = 4;
        y = 5;
        z = 6;
    }

    public int sum() {
	return (x + y + z);
    }

    public static String clue() {
	return ("hint: z = x + 1");
    }

    public static String herring(int a) {
	return ("hint: z = x + 1");
    }

    public static String herring(int a, double b, char c) {
	return ("hint: z = x + 1");
    }

    public void add1() {
	++x;
	++y;
	++z;
    }

    public void add(int i, int j, int k) {
	x += i;
	y += j;
	z += k;
    }

    public int[] addArray(InfoTestClass[] itc, String[] s, Integer[] I) {
	x += 1;
	y += 2;
	z += 3;
	return iArray;
    }

    public String[][] addArrayArray(String[][] s, int[][][] iAA) {
	x += 1;
	y += 2;
	z += 3;
	return s;
    }

    private void guess() {
	y = x + z - y;
    }
}


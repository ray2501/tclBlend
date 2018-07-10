/* 
 * ArrayTestClass.java --
 *
 *	Used to test the java::info command
 *
 * Copyright (c) 1997 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: ArrayTestClass.java,v 1.1 1999/05/10 04:09:05 dejong Exp $
 */

package tests.invoke;

public class ArrayTestClass {
    
    public int i;
    public int[] iA;
    public int[][] iAA;
    public String s;
    public String[] sA;
    public String[][] sAA;
    
    public ArrayTestClass() {
        i = 5;
	iA = new int[2];
	iAA = new int[4][2];
	s = "white";
	sA = new String[2];
	sAA = new String[4][2];
    }

    public static String yellow() {
	return ("yello moons");
    }

    public static String rainbow(String color) {
	return ("new feature color: " + color);
    }

    public int[] pink() {
	int[] tt;
	tt = new int[2];
	tt[0] = 8;
	tt[1] = 9;
	return (tt);
    }

    public String pink(int a, int b, int c) {
	return ("purple horseshoes: " + iA[0] + " " + iA[1]);
    }

    public static int[][] pink(int a) {
	int[][] tt;
	tt = new int[4][2];
	return (tt);
    }

    public static String pink(int[][] a, int b) {
	return ("pink hearts, int[][]");
    }

    public String[] blue() {
	String[] tt;
	tt = new String[2];
	tt[0] = "orange";
	tt[1] = "stars";
	return (tt);
    }

    public String blue(int a, int b, int c) {
	return (sA[0] + " " + sA[1]);
    }

    public static String[][] blue(int a) {
	String[][] tt;
	tt = new String[4][2];
	return (tt);
    }

    public static String blue(String[][] a, int b) {
	return ("blue diamonds, String[][]");
    }

    public static Integer[][] green(int a) {
	Integer[][] ii;
	ii = new Integer[4][2];
	return (ii);
    }

    public static String green(Integer[][] a, int b) {
	return ("green clovers, java.lang.Integer[][]");
    }
}


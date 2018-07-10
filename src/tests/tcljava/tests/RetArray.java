/* 
 * RetArray.java --
 *
 *
 * Copyright (c) 1997 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: RetArray.java,v 1.1 1999/05/10 04:09:03 dejong Exp $
 *
 */

package tests;

public class RetArray {

  private static String[] arr1;
  private static String[][] arr2;

  static {
    arr1 = new String[5];
    arr1[0] = "0";
    arr1[1] = "1";
    arr1[2] = "2";
    arr1[3] = "3";
    arr1[4] = "4";

    arr2 = new String[2][2];

    arr2[0][0] = "00";
    arr2[0][1] = "01";

    arr2[1][0] = "10";
    arr2[1][1] = "11";
  }

  public static Object[] getObjectArr1() {
    return arr1;
  }

  public static String[] getStringArr1() {
    return arr1;
  }

  public static Object[][] getObjectArr2() {
    return arr2;
  }

  public static String[][] getStringArr2() {
    return arr2;
  }

}


/*

Test it like this:

set obj_arr1 [java::call tests.RetArray getObjectArr1]

java::info class $obj_arr1
>should be java.lang.Object[]

set obj_elem [$obj_arr1 -noconvert get 0]

java::info class $obj_elem
>should be java.lang.Object

$obj_elem toString
>should be 0



set str_arr1 [java::call tests.RetArray getStringArr1]

java::info class $str_arr1
>should be java.lang.String[]

set str_elem [$str_arr1 -noconvert get 0]

java::info class $str_elem
>should be java.lang.String

$str_elem toString


string compare [$obj_elem -noconvert toString] $str_elem
>should be 0






>tests for the two dimensional array are simmilar


*/


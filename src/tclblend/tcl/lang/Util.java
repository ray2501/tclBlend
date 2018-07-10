/* 
 * Util.java --
 *
 *	This file implements the native version of the tcl.lang.Util class.
 *
 * Copyright (c) 1997 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: Util.java,v 1.3 2006/05/15 01:25:46 mdejong Exp $
 */

package tcl.lang;

/**
 * This class handles parsing of various data types.
 */
public class Util {

/**
 * Given a string, return a boolean value corresponding
 * to the string.
 *
 * @param interp current interpreter.
 * @param string string representation of the boolean.
 * @exception TclException for malformed boolean values.
 */
static final native boolean getBoolean(Interp interp, String s)
throws TclException;

/**
 * Converts an ASCII string to an integer.
 *
 * @param interp current interpreter.
 * @param s the string to convert from. Must be in valid Tcl integer
 *      format.
 * @return the integer value of the string.
 * @exception TclException if the string is not a valid Tcl integer.
 */    

static final native int getInt(Interp interp, String s)
throws TclException;

/**
 * Converts an ASCII string to a double. Certain special strings
 * like "NaN" and "-Inf" need to mapped to Java values. These
 * need to be checked for before passing the string value into
 * Tcl's native parsing logic.
 *
 * @param interp current interpreter.
 * @param s the string to convert from. Must be in valid Tcl double
 *      format.
 * @return the double value of the string.
 * @exception TclException if the string is not a valid Tcl double.
 */

static final double getDouble(Interp interp, String s)
    throws TclException
{
    if (s != null) {
        // Return special value for the string "NaN"

        if (s.toLowerCase().equals("nan")) {
            return Double.NaN;
        }

        // The strings "Inf", "-Inf", "Infinity", and "-Infinity"
        // map to special double values.

        char c = s.charAt(0);
        String sub;
        boolean negative;

        if (c == '-') {
            sub = s.substring(1);
            negative = true;
        } else {
            sub = s;
            negative = false;
        }

        if (sub.equals("Inf") || sub.equals("Infinity")) {
            return (negative ?
                Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
        }
    }

    // Pass string to Tcl native double parsing routines once
    // we know it is not a special value.

    return getDoubleNative(interp, s);
}

static final native double getDoubleNative(Interp interp, String s)
throws TclException;


/*
 *----------------------------------------------------------------------
 *
 * printDouble --
 *
 *	Returns the string form of a double. The exact formatting
 *	of the string depends on the tcl_precision variable.  Calls
 *	Tcl_PrintDouble() in C.
 *
 * Results:
 *	Returns the string form of double number.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

static final native String
printDouble(
    double number);		// The number to format into a string.

/*
 *----------------------------------------------------------------------
 *
 * getCwd --
 *
 *	Retrieve the current working directory.
 *
 * Results:
 *	Returns the string value of the working directory.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

static final native String
getCwd();

/*
 *----------------------------------------------------------------------
 *
 * stringMatch --
 *
 *	Compare a string to a globbing pattern.  The matching operation
 *	permits the following special characters in the pattern: *?\[]
 *	(see the manual entry for details).
 *
 * Results:
 *	Returns true if the string matches the pattern.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

static public final native boolean
stringMatch(
    String string,		// String to match.
    String pattern);		// Pattern to compare against.

/*
 *----------------------------------------------------------------------
 *
 * isJacl --
 *
 *	Returns true if running in Jacl. This method is used
 *	by conditional logic in the tcljava module.
 *
 * Results:
 *	Returns a boolean.
 *
 * Side effects:
 *	 None.
 *
 *----------------------------------------------------------------------
 */


static boolean
isJacl() {
    return false;
}

/*
 *----------------------------------------------------------------------
 *
 * looksLikeInt --
 *
 *	Returns true when isJacl() is true and this string looks
 *	like an integer.
 *
 * Results:
 *	Returns a boolean.
 *
 * Side effects:
 *	 None.
 *
 *----------------------------------------------------------------------
 */

static boolean
looksLikeInt(String s) {
    return false;
}

} // end Util


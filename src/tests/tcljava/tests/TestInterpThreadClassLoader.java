/* 
 * TestInterpThreadClassLoader.java --
 *
 *      This file tests changing an argument object's internal
 *      rep to TclList.
 *
 * Copyright (c) 2002 by Mo DeJong
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: TestInterpThreadClassLoader.java,v 1.1 2006/02/08 23:53:47 mdejong Exp $
 */

package tests;

import tcl.lang.*;

public class TestInterpThreadClassLoader extends ClassLoader {

public TestInterpThreadClassLoader(ClassLoader parent) {
    super(parent);
}

// bytes of class data from: TestInterpThreadUnknown.java
// public class TestInterpThreadUnknown {}

byte[] hexbytes = {
(byte)202, (byte)254, (byte)186, (byte)190, (byte)0, (byte)0, 
(byte)0, (byte)46, (byte)0, (byte)13, (byte)10, (byte)0, 
(byte)3, (byte)0, (byte)10, (byte)7, (byte)0, (byte)11, 
(byte)7, (byte)0, (byte)12, (byte)1, (byte)0, (byte)6, 
(byte)60, (byte)105, (byte)110, (byte)105, (byte)116, (byte)62, 
(byte)1, (byte)0, (byte)3, (byte)40, (byte)41, (byte)86, 
(byte)1, (byte)0, (byte)4, (byte)67, (byte)111, (byte)100, 
(byte)101, (byte)1, (byte)0, (byte)15, (byte)76, (byte)105, 
(byte)110, (byte)101, (byte)78, (byte)117, (byte)109, (byte)98, 
(byte)101, (byte)114, (byte)84, (byte)97, (byte)98, (byte)108, 
(byte)101, (byte)1, (byte)0, (byte)10, (byte)83, (byte)111, 
(byte)117, (byte)114, (byte)99, (byte)101, (byte)70, (byte)105, 
(byte)108, (byte)101, (byte)1, (byte)0, (byte)28, (byte)84, 
(byte)101, (byte)115, (byte)116, (byte)73, (byte)110, (byte)116, 
(byte)101, (byte)114, (byte)112, (byte)84, (byte)104, (byte)114, 
(byte)101, (byte)97, (byte)100, (byte)85, (byte)110, (byte)107, 
(byte)110, (byte)111, (byte)119, (byte)110, (byte)46, (byte)106, 
(byte)97, (byte)118, (byte)97, (byte)12, (byte)0, (byte)4, 
(byte)0, (byte)5, (byte)1, (byte)0, (byte)23, (byte)84, 
(byte)101, (byte)115, (byte)116, (byte)73, (byte)110, (byte)116, 
(byte)101, (byte)114, (byte)112, (byte)84, (byte)104, (byte)114, 
(byte)101, (byte)97, (byte)100, (byte)85, (byte)110, (byte)107, 
(byte)110, (byte)111, (byte)119, (byte)110, (byte)1, (byte)0, 
(byte)16, (byte)106, (byte)97, (byte)118, (byte)97, (byte)47, 
(byte)108, (byte)97, (byte)110, (byte)103, (byte)47, (byte)79, 
(byte)98, (byte)106, (byte)101, (byte)99, (byte)116, (byte)0, 
(byte)33, (byte)0, (byte)2, (byte)0, (byte)3, (byte)0, 
(byte)0, (byte)0, (byte)0, (byte)0, (byte)1, (byte)0, 
(byte)1, (byte)0, (byte)4, (byte)0, (byte)5, (byte)0, 
(byte)1, (byte)0, (byte)6, (byte)0, (byte)0, (byte)0, 
(byte)29, (byte)0, (byte)1, (byte)0, (byte)1, (byte)0, 
(byte)0, (byte)0, (byte)5, (byte)42, (byte)183, (byte)0, 
(byte)1, (byte)177, (byte)0, (byte)0, (byte)0, (byte)1, 
(byte)0, (byte)7, (byte)0, (byte)0, (byte)0, (byte)6, 
(byte)0, (byte)1, (byte)0, (byte)0, (byte)0, (byte)1, 
(byte)0, (byte)1, (byte)0, (byte)8, (byte)0, (byte)0, 
(byte)0, (byte)2, (byte)0, (byte)9 
};

// Implement overloader class loader, this will load a class
// named "TestInterpThreadUnknown".

protected Class
loadClass(
    String className,       // The name of the desired Class.
    boolean resolveIt)      // If true, then resolve all referenced classes.
throws
    ClassNotFoundException, // The class could not be found.
    SecurityException
{
    Class result = null;

    try {
        result = Class.forName(className, resolveIt, getParent());
    } catch (ClassNotFoundException ex) {
    } catch (IllegalArgumentException ex) {
        ex.printStackTrace(System.err);
    } catch (NoClassDefFoundError ex) {
    } catch (IncompatibleClassChangeError ex) {
        ex.printStackTrace(System.err);
    }

    if ((result == null) && className.equals("TestInterpThreadUnknown")) {
        try {
        result = defineClass(className, hexbytes, 0, hexbytes.length);
        } catch (NoClassDefFoundError ex) {
            ex.printStackTrace(System.err);
        } catch (ClassFormatError ex) {
            ex.printStackTrace(System.err);
        }
    }

    if (result != null) {
        if (resolveIt) {
            resolveClass(result);
        }
        return result;
    }

    throw new ClassNotFoundException(className);
}

}


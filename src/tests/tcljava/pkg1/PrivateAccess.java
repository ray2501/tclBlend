/*
 * PrivateAccess --
 *
 *	This file tests the tcl.lang.reflect.PkgInvoker class.
 *	One should be able to access default members when a
 *	TclPkgInvoker exists, but private members are off limits.
 *
 * Copyright (c) 2003 Mo DeJong
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 * RCS: @(#) $Id: PrivateAccess.java,v 1.1 2003/03/11 10:26:41 mdejong Exp $
 *
 */

package pkg1;

public class PrivateAccess {
    private int private_int = -1;
    int default_int = 0;

    private void private_method() {}
    void default_method() {}

    private PrivateAccess(boolean b) {}
    PrivateAccess(int i) {}
}

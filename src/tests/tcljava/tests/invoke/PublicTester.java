/*
 * PublicTester.java --
 *
 *	This file defines a public class and a package protected class.
 *	These classes are used in the tcljava/FuncSig.test to test the
 *	correctness of FuncSig.getAccessibleMethods.
 *
 * Copyright (c) 1997 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: PublicTester.java,v 1.3 2002/12/27 14:33:20 mdejong Exp $
 */

package tests.invoke;

/*
 * This class is public so it can be created using the java::new command.
 * All of its public methods can also be called from Tcl.
 */

public class PublicTester implements IntC {

public String getStringC() {
    return "implements IntC.getStringC()";
}

public String publicFunc() {
    return "access ok: Tester.publicFunc()";
}

public static PublicTester getCNonPublicTester() {
    return (PublicTester) new NonPublicTester();
}

public static NonPublicTester getNonPublicTester() {
    return new NonPublicTester();
}

public NonPublicTester getNonPublicTesterI() {
    return new NonPublicTester();
}

String noAccess() {
    return "can't call this one";
}

public static NonPublicTester nptf = null;

}

/*
 * This class is not public so it cannot be created using the java::new
 * command. However, we can get an instance of this class via
 * PublicTester.getNonPublicTester and we should be able to call all of
 * it methods that are in the public API.
 */

class NonPublicTester extends PublicTester implements IntA {

/*
 * Can call this method because it was first declared in a public
 * class.
 */

public String publicFunc() {
    return 
	"access ok: NonPublicTester.publicFunc() overrides Tester.publicFunc()";
}

/*
 * Can't call this method because it's not public.
 */

String noAccess2() {
    return "can't call this one, either";
}

/*
 * Can't call this method because the classes declaring class
 * (NonPublicTester) is not public.
 */

public String noAccess3() {
    return "can't call this one, either";
}

public String getStringA() {
    return 
	"access ok: NonPublicTester.getStringA() implements IntA.getStringA()";
}

public String getStringC() {
    return
	"access ok: NonPublicTester.getStringC() implements IntC.getStringC()";
}

}


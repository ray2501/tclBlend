/* 
 * BlendExtension.java --
 *
 *	This extension encapsulates the java::* commands.
 *
 * Copyright (c) 1997-1998 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: BlendExtension.java,v 1.26 2007/06/07 20:52:15 mdejong Exp $
 */

package tcl.lang;

public class BlendExtension extends Extension {

/*
 *----------------------------------------------------------------------
 *
 * init --
 *
 *	Initialize the java pacakge.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Creates the java namespace and java::* commands.
 *
 *----------------------------------------------------------------------
 */

public void
init(
    Interp interp)		// Interpreter to intialize.
throws TclException
{
    if (interp.getCommand("java::new") != null) {
        throw new TclRuntimeError("BlendExtension.init() invoked twice in same interp");
    }

    // Create the commands in the Java package

    loadOnDemand(interp, "java::bind",        "tcl.lang.JavaBindCmd");
    loadOnDemand(interp, "java::call",        "tcl.lang.JavaCallCmd");
    loadOnDemand(interp, "java::cast",        "tcl.lang.JavaCastCmd");
    loadOnDemand(interp, "java::defineclass", "tcl.lang.JavaDefineClassCmd");
    loadOnDemand(interp, "java::event",       "tcl.lang.JavaEventCmd");
    loadOnDemand(interp, "java::field",       "tcl.lang.JavaFieldCmd");
    loadOnDemand(interp, "java::for",         "tcl.lang.JavaForCmd");
    loadOnDemand(interp, "java::getinterp",   "tcl.lang.JavaGetInterpCmd");
    loadOnDemand(interp, "java::import",      "tcl.lang.JavaImportCmd");
    loadOnDemand(interp, "java::info",        "tcl.lang.JavaInfoCmd");
    loadOnDemand(interp, "java::instanceof",  "tcl.lang.JavaInstanceofCmd");
    loadOnDemand(interp, "java::isnull",      "tcl.lang.JavaIsNullCmd");
    loadOnDemand(interp, "java::listify",     "tcl.lang.JavaListifyCmd");
    loadOnDemand(interp, "java::load",        "tcl.lang.JavaLoadCmd");
    loadOnDemand(interp, "java::new",         "tcl.lang.JavaNewCmd");
    loadOnDemand(interp, "java::null",        "tcl.lang.JavaNullCmd");
    loadOnDemand(interp, "java::prop",        "tcl.lang.JavaPropCmd");
    loadOnDemand(interp, "java::throw",       "tcl.lang.JavaThrowCmd");
    loadOnDemand(interp, "java::try",         "tcl.lang.JavaTryCmd");


    // Set up namespace exporting of these java commands.
    // FIXME : double check that this works with one demand loaded clases.

    interp.eval("namespace eval ::java {namespace export bind call cast defineclass event field getinterp import info instanceof isnull load new null prop throw try}");


    // load unsupported command(s)
    loadOnDemand(interp, "unsupported::jdetachcall",
		 "tcl.lang.UnsupportedJDetachCallCmd");




    // Part of the java package is defined in Tcl code.  We source
    // in that code now.
    
    interp.evalResource("/tcl/lang/library/java/javalock.tcl");

    // Set up the tcljava array which will store info about
    // The system that scripts may want to access to.

    // Set tcljava(tcljava) to jacl or tclblend

    TclObject plat = interp.getVar("tcl_platform", "platform",
				   TCL.GLOBAL_ONLY);

    if (plat.toString().equals("java")) {
        interp.setVar("tcljava", "tcljava",
            TclString.newInstance("jacl"), TCL.GLOBAL_ONLY);
    } else {
        interp.setVar("tcljava", "tcljava",
            TclString.newInstance("tclblend"), TCL.GLOBAL_ONLY);
    }


    // set tcljava(java.version) to the JVM version number

    interp.setVar("tcljava", "java.version",
        TclString.newInstance(System.getProperty("java.version")),
        TCL.GLOBAL_ONLY);

    // set tcljava(java.home) to the root of the JVM install

    interp.setVar("tcljava", "java.home",
        TclString.newInstance(System.getProperty("java.home")),
        TCL.GLOBAL_ONLY);

    // set tcljava(java.vendor) to the info from the JVM provider

    interp.setVar("tcljava", "java.vendor",
        TclString.newInstance(System.getProperty("java.vendor")),
        TCL.GLOBAL_ONLY);

    // set tcljava(java.vendor.url) to the info from the JVM provider

    interp.setVar("tcljava", "java.vendor.url",
        TclString.newInstance(System.getProperty("java.vendor.url")),
        TCL.GLOBAL_ONLY);

    // set tcljava(java.vendor.url.bug) to the info from the JVM provider

    interp.setVar("tcljava", "java.vendor.url.bug",
        TclString.newInstance(System.getProperty("java.vendor.url.bug")),
        TCL.GLOBAL_ONLY);





    // Provide the Tcl/Java package with the version info.
    // The version is also set in:
    // src/jacl/tcl/lang/Interp.java
    // src/pkgIndex.tcl
    // win/makefile.vc
    // unix/configure.in

    interp.eval("package provide java 1.4.7");

}

} // end BlendExtension


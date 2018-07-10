package tests.signature;

import tcl.lang.TclObject;
import tcl.lang.Interp;

public class PassTclObject {

    // Get the string value for a TclObject, used to test
    // passing of a TclObject to a Java method.

    public static String getTclObjectString(TclObject to) {
        return to.toString();
    }

    // These next two methods also test passing of a TclObject
    // but with a signature that is ambiguous.

    public static String getTclObjectString2(TclObject to) {
        return to.toString();
    }

    public static String getTclObjectString2(Integer i) {
        return "Integer";
    }

}

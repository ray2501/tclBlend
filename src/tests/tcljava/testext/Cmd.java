// Test class for extension loading. It should be possible to
// load extension classes from a jar file defined on the
// TCL_CLASSPATH. This class checks to make sure that actually
// works.

package testext;

import tcl.lang.*;

public class Cmd implements Command {
    public void cmdProc(Interp interp, TclObject[] objv)
            throws TclException {
        interp.setResult("OK");
    }
}


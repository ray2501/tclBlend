/*
empty implementation of Interp used for compiling in multiple packages
*/

package tcl.lang;

import java.io.*;
import java.util.*;

public class Interp {

public Interp() { throw new RuntimeException("empty Interp used"); }

// Used ONLY by ReflectObject
HashMap reflectObjTable = null;
long reflectObjCount = 0;
HashMap reflectConflictTable = null;

// Used ONLY by JavaImportCmd
HashMap[] importTable = null;


public void dispose() {}

File getWorkingDir()
{
  return null;
}

public final TclObject
setVar(String name1, String name2, TclObject value, int flags)
throws TclException
{
  return null;
}


public final TclObject
setVar(String name, TclObject value, int flags)
throws TclException
{
    return null;
}

public final TclObject getVar(String name1, String name2, int flags)
throws TclException {
    return null;
}


public final TclObject getVar(String name, int flags)
throws TclException
{
    return null;
}    

public final void unsetVar(String name, int flags) throws TclException {}


public final void unsetVar(String name1, String name2, int flags)
throws TclException {}


public void traceVar(String name, VarTrace trace, int flags)
throws TclException {}



public void traceVar(String part1, String part2, VarTrace trace, int flags)
throws TclException {}


public void untraceVar(String name, VarTrace trace, int flags)	
throws TclException {}



public void untraceVar( String part1, String part2,
 VarTrace trace, int flags) throws TclException {}

public void createCommand(String name, Command cmd) {}


public int deleteCommand(String name)
{
  return 0;
}


public Command getCommand(String name)
{
  return null;
}


public static boolean commandComplete(String cmd)
{
  return true;
}


public final TclObject getResult()
{
  return null;
}

public final void setResult(TclObject r) {}

public final void setResult(String r) {}

public final void setResult(int r) {}

public final void setResult(boolean r) {}

public final void setResult(double r) {}

public final void resetResult() {}

public void eval(String script,int flags) throws TclException {}

public void eval(String script) throws TclException {}

public void eval(TclObject tobj, int flags) throws TclException {}

public void evalFile(String s) throws TclException {}

public void evalResource(String resName) throws TclException {}

public void setErrorCode(TclObject code) {}

public void addErrorInfo(String message) {}

public void backgroundError() {}

public Notifier getNotifier()
{
    return null;
}

public void setAssocData(String name, AssocData data) {}

public void deleteAssocData(String name) {}

public AssocData getAssocData(String name)
{
  return null;
}

public final void pkgProvide(String name, String version) {}

public final String pkgRequire(String pkgname, String version, boolean exact)
{
  return null;
}

public ClassLoader
getClassLoader()
{
  return null;
}

} // end Interp


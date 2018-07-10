/*
 * Interp.java
 *
 * Copyright (c) 1997 Cornell University.
 * Copyright (c) 1998 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: Interp.java,v 1.40 2006/07/28 20:53:06 mdejong Exp $
 *
 */

package tcl.lang;

import java.util.*;
import java.io.*;
import java.net.*;

/*
 * The Tcl interpreter class.
 */
public class Interp {


// Load the Tcl Blend shared library to make JNI
// methods visible to the JVM. We need to actually
// call a JNI method to make sure the loading worked
// in case the JVM does not check for the symbols
// until the method is actually invoked. We invoke
// this method once the very first time a constructor
// is called so that if an exception occurs, it can
// be propagated out to the caller. If this same code
// appeared in a static initializer (the old approach)
// there would be no means to propagate the exception.

private static boolean shlib_loaded = false;

private synchronized final static void shlib_load()
    throws UnsatisfiedLinkError
{
    System.loadLibrary("tclblend");
    Interp.commandComplete("");
    Interp.initName();
    shlib_loaded = true;
}

// Invoked after the Tcl Blend library has been loaded
// and JNI native methods have been associated. This method
// sets the name of the Tcl executable if it needs to be
// done (when Tcl Blend is loaded into a JVM).

private final static native void initName();

// The interpPtr contains the C Tcl_Interp* used in native code.  This
// field is declared with package visibility so that it can be passed
// to native methods by other classes in this package.

long interpPtr;


// The following three variables are used to maintain a translation
// table between ReflectObject's and their string names. These
// variables are accessed by the ReflectObject class, they
// are defined here be cause we need them to be per interp data.

// Translates Object to ReflectObject. This makes sure we have only
// one ReflectObject internalRep for the same Object -- this
// way Object identity can be done by string comparison.

HashMap reflectObjTable = new HashMap();

// Number of reflect objects created so far inside this Interp
// (including those that have be freed)

long reflectObjCount = 0;

// Table used to store reflect hash index conflicts, see
// ReflectObject implementation for more details

HashMap reflectConflictTable = new HashMap();

// The Notifier associated with this Interp.

private Notifier notifier;

// Hash table for associating data with this interpreter. Cleaned up
// when this interpreter is deleted.

HashMap<String, AssocData> assocDataTab;

// Used ONLY by JavaImportCmd
HashMap[] importTable = {new HashMap(), new HashMap()};

// Used ONLY by CObject
ArrayList<CObject> cobjCleanup = new ArrayList<CObject>();

// This field is set to a non-null value when
// a Tcl command implemented as a Java
// Command.cmdProc() raises an exception.

Throwable pendingException = null;

// Java thread this interp was created in. This is used
// to check for user coding errors where the user tries
// to create an interp in one thread and then invoke
// methods from another thread.

private Thread cThread;

// The ClassLoader for this interp

TclClassLoader classLoader = null;


/*
 *----------------------------------------------------------------------
 *
 * Interp --
 *
 *	Create a new Interp to wrap an existing C Tcl_Interp.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

private
Interp(
    long l)			// Pointer to Tcl_Interp.
{
    if (!shlib_loaded) {
        shlib_load();
    }

    interpPtr = l;

    cThread  = Thread.currentThread();
    notifier = Notifier.getNotifierForThread(cThread);
    notifier.preserve();
}

/*
 *----------------------------------------------------------------------
 *
 * Interp --
 *
 *	Create a new Tcl interpreter.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Calls init() on the new interpreter.  If init() fails,
 *	disposes of the interpreter.
 *
 *----------------------------------------------------------------------
 */

public
Interp()
{
    if (!shlib_loaded) {
        shlib_load();
    }

    interpPtr = create();

    cThread  = Thread.currentThread();
    notifier = Notifier.getNotifierForThread(cThread);
    notifier.preserve();

    if (init(interpPtr) != TCL.OK) {
	String result = getResult().toString();
	dispose();
	throw new TclRuntimeError(result);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * create --
 *
 *	Call Tcl_CreateInterp to initialize a new interpreter.
 *
 * Results:
 *	Returns a new Tcl_Interp *. Will raise a TclRuntimeError
 *	if an interp could not be created.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

private final native long
create();

/*
 *----------------------------------------------------------------------
 *
 * dispose --
 *
 *	This method cleans up the state of the interpreter so that
 *	it can be garbage collected safely.  This routine needs to
 *	break any circular references that might keep the interpreter
 *	alive indefinitely.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Cleans up the interpreter.
 *
 *----------------------------------------------------------------------
 */

public void
dispose()
{
    if (Thread.currentThread() != cThread) {
        throw new TclRuntimeError(
            "Interp.dispose() invoked in thread other than the one it was created in");
    }

    // Remove all the assoc data tied to this interp.
	
    if (assocDataTab != null) {
	for (Iterator iter = assocDataTab.entrySet().iterator(); iter.hasNext() ;) {
	    Map.Entry entry = (Map.Entry) iter.next();
	    AssocData data = (AssocData) entry.getValue();
	    data.disposeAssocData(this);
	    iter.remove();
	}
	assocDataTab = null;
    }

    // Release the notifier.

    if (notifier != null) {
	notifier.release();
	notifier = null;
    }

    // Clean up the C state.

    if (interpPtr != 0) {
	doDispose(interpPtr);
	interpPtr = 0;
    }

    // See if we need to cleanup this Java thread
    Notifier.finalizeThreadCheck();
}

/*
 *----------------------------------------------------------------------
 *
 * finalize --
 *
 *	Interpreter finalization method. We print a message to
 *	stderr if the user neglected to dispose of an Interp
 *	properly. We can't call dispose here because the
 *	finalize method is called from the gc thread and
 *	Tcl thread specific data needs to be cleaned up
 *	in the thread it was allocated in.
 *
 * Results:
 *	Prints to stderr.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

protected void
finalize() throws Throwable
{
    if (notifier != null) {
        System.err.println("finalized interp has not been disposed");
    }
    super.finalize();
}

/*
 *----------------------------------------------------------------------
 *
 * getWorkingDir --
 *
 *	Retrieve the current working directory for this interpreter.
 *
 * Results:
 *	Returns the File for the directory.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

File
getWorkingDir()
{
    return new File(Util.getCwd());
}

/*-----------------------------------------------------------------
 *
 *	                     VARIABLES
 *
 *-----------------------------------------------------------------
 */

/*
 *----------------------------------------------------------------------
 *
 * setVar --
 *
 *	Set the value of a variable.
 *
 * Results:
 *	Returns the new value of the variable.
 *
 * Side effects:
 *	May trigger traces.
 *
 *----------------------------------------------------------------------
 */

public final native TclObject
setVar(
    String name1,		// If name2 is null, this is name of a scalar
				// variable. Otherwise it is the name of an
				// array. 
    String name2,		// Name of an element within an array, or
				// null.
    TclObject value,		// New value for variable.
    int flags)			// Various flags that tell how to set value:
				// any of TCL.GLOBAL_ONLY, TCL.NAMESPACE_ONLY,
				// TCL.APPEND_VALUE, TCL.LIST_ELEMENT, TCL.LEAVE_ERR_MSG,
				// or TCL.PARSE_PART1. 
throws
    TclException;

/*
 *----------------------------------------------------------------------
 *
 * setVar --
 *
 *	Set the value of a variable.
 *
 * Results:
 *	Returns the new value of the variable.
 *
 * Side effects:
 *	May trigger traces.
 *
 *----------------------------------------------------------------------
 */

public final
TclObject
setVar(
    String name,		// Name of variable, array, or array element
				// to set.
    TclObject value,		// New value for variable.
    int flags)			// Various flags that tell how to set value:
				// any of TCL.GLOBAL_ONLY, TCL.NAMESPACE_ONLY,
				// TCL.APPEND_VALUE, TCL.LIST_ELEMENT, or
				// TCL.LEAVE_ERR_MSG. 
throws
    TclException
{
    return setVar(name, null, value, (flags | TCL.PARSE_PART1));
}

/*
 *----------------------------------------------------------------------
 *
 * setVar --
 *
 *	Set a variable to the value in a String argument.
 *
 * Results:
 *	Returns the new value of the variable.
 *
 * Side effects:
 *	May trigger traces.
 *
 *----------------------------------------------------------------------
 */

public final
TclObject
setVar(
    String name1,		// If name2 is null, this is name of a scalar
				// variable. Otherwise it is the name of an
				// array. 
    String name2,		// Name of an element within an array, or
				// null. 
    String strValue,		// New value for variable. 
    int flags)			// Various flags that tell how to set value:
				// any of TCL.GLOBAL_ONLY, TCL.NAMESPACE_ONLY,
				// TCL.APPEND_VALUE, or TCL.LIST_ELEMENT.
				// TCL.LEAVE_ERR_MSG.
throws
    TclException
{
    return setVar(name1, name2, TclString.newInstance(strValue), flags);
}

/*
 *----------------------------------------------------------------------
 *
 * setVar --
 *
 *	Set a variable to the value in an int argument.
 *
 * Results:
 *	Returns the new value of the variable.
 *
 * Side effects:
 *	May trigger traces.
 *
 *----------------------------------------------------------------------
 */

public final
TclObject
setVar(
    String name1,		// If name2 is null, this is name of a scalar
				// variable. Otherwise it is the name of an
				// array. 
    String name2,		// Name of an element within an array, or
				// null. 
    int intValue,		// New value for variable. 
    int flags)			// Various flags that tell how to set value:
				// any of TCL.GLOBAL_ONLY, TCL.NAMESPACE_ONLY,
				// TCL.APPEND_VALUE, or TCL.LIST_ELEMENT.
				// TCL.LEAVE_ERR_MSG.
throws
    TclException
{
    return setVar(name1, name2, TclInteger.newInstance(intValue), flags);
}

/*
 *----------------------------------------------------------------------
 *
 * setVar --
 *
 *	Set a variable to the value in a double argument.
 *
 * Results:
 *	Returns the new value of the variable.
 *
 * Side effects:
 *	May trigger traces.
 *
 *----------------------------------------------------------------------
 */

public final
TclObject
setVar(
    String name1,		// If name2 is null, this is name of a scalar
				// variable. Otherwise it is the name of an
				// array. 
    String name2,		// Name of an element within an array, or
				// null. 
    double dValue,		// New value for variable. 
    int flags)			// Various flags that tell how to set value:
				// any of TCL.GLOBAL_ONLY, TCL.NAMESPACE_ONLY,
				// TCL.APPEND_VALUE, or TCL.LIST_ELEMENT.
				// TCL.LEAVE_ERR_MSG.
throws
    TclException
{
    return setVar(name1, name2, TclDouble.newInstance(dValue), flags);
}

/*
 *----------------------------------------------------------------------
 *
 * setVar --
 *
 *	Set a variable to the value in a boolean argument.
 *
 * Results:
 *	Returns the new value of the variable.
 *
 * Side effects:
 *	May trigger traces.
 *
 *----------------------------------------------------------------------
 */

public final
TclObject
setVar(
    String name1,		// If name2 is null, this is name of a scalar
				// variable. Otherwise it is the name of an
				// array. 
    String name2,		// Name of an element within an array, or
				// null. 
    boolean bValue,		// New value for variable. 
    int flags)			// Various flags that tell how to set value:
				// any of TCL.GLOBAL_ONLY, TCL.NAMESPACE_ONLY,
				// TCL.APPEND_VALUE, or TCL.LIST_ELEMENT.
				// TCL.LEAVE_ERR_MSG.
throws
    TclException
{
    return setVar(name1, name2, TclBoolean.newInstance(bValue), flags);
}

/*
 *----------------------------------------------------------------------
 *
 * getVar --
 *
 *	Get the value of a variable.
 *
 * Results:
 *	Returns the value of the variable.
 *
 * Side effects:
 *	May trigger traces.
 *
 *----------------------------------------------------------------------
 */

public final native TclObject
getVar(
    String name1,		// If name2 is null, this is name of a scalar
				// variable. Otherwise it is the name of an
				// array. 
    String name2,		// Name of an element within an array, or
				// null.
    int flags)			// Various flags that tell how to get value:
				// any of TCL.GLOBAL_ONLY, TCL.NAMESPACE_ONLY,
				// TCL.LEAVE_ERR_MSG, or TCL.PARSE_PART1. 
throws
    TclException;

/*
 *----------------------------------------------------------------------
 *
 * getVar --
 *
 *	Get the value of a variable.
 *
 * Results:
 *	Returns the value of the variable.
 *
 * Side effects:
 *	May trigger traces.
 *
 *----------------------------------------------------------------------
 */

public final TclObject
getVar(
    String name,		// The name of a variable, array, or array
				// element.
    int flags)			// Various flags that tell how to get value:
				// any of TCL.GLOBAL_ONLY, TCL.NAMESPACE_ONLY,
				// or TCL.LEAVE_ERR_MSG.
throws TclException
{
    return getVar(name, null, (flags | TCL.PARSE_PART1));
}    

/*
 *----------------------------------------------------------------------
 *
 * unsetVar --
 *
 *	Unset a variable.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	May trigger traces.
 *
 *----------------------------------------------------------------------
 */

public final void
unsetVar(
    String name,		// The name of a variable, array, or array
				// element.
    int flags)			// Various flags that tell how to get value:
				// any of TCL.GLOBAL_ONLY, TCL.NAMESPACE_ONLY,
				// or TCL.LEAVE_ERR_MSG.
throws
    TclException
{
    unsetVar(name, null, (flags | TCL.PARSE_PART1));
}

/*
 *----------------------------------------------------------------------
 *
 * unsetVar --
 *
 *	Unset a variable.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	May trigger traces.
 *
 *----------------------------------------------------------------------
 */

public final native void
unsetVar(
    String name1,		// If name2 is null, this is name of a scalar
				// variable. Otherwise it is the name of an
				// array. 
    String name2,		// Name of an element within an array, or
				// null.
    int flags)			// Various flags that tell how to get value:
				// any of TCL.GLOBAL_ONLY, TCL.NAMESPACE_ONLY,
				// TCL.LEAVE_ERR_MSG, or TCL.PARSE_PART1. 
throws
    TclException;

/*
 *----------------------------------------------------------------------
 *
 * traceVar --
 *
 *	Add a trace to a variable.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public void
traceVar(
    String name,		// Name of variable;  may end with "(index)"
				// to signify an array reference.
    VarTrace trace,		// Object to notify when specified ops are
				// invoked upon varName.
    int flags)			// OR-ed collection of bits, including any
				// of TCL.TRACE_READS, TCL.TRACE_WRITES,
				// TCL.TRACE_UNSETS, TCL.GLOBAL_ONLY,
				// TCL.NAMESPACE_ONLY.
throws
    TclException
{
    traceVar(name, null, trace, (flags | TCL.PARSE_PART1));
}

/*
 *----------------------------------------------------------------------
 *
 * traceVar --
 *
 *	Add a trace to a variable.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public native void
traceVar(
    String part1,		// Name of scalar variable or array.
    String part2,		// Name of element within array;  null means
				// trace applies to scalar variable or array
				// as-a-whole.  
    VarTrace trace,		// Object to notify when specified ops are
				// invoked upon varName.
    int flags)			// OR-ed collection of bits, including any
				// of TCL.TRACE_READS, TCL.TRACE_WRITES,
				// TCL.TRACE_UNSETS, TCL.GLOBAL_ONLY,
				// TCL.NAMESPACE_ONLY and
				// TCL.PARSE_PART1.
throws
    TclException;		// If variable doesn't exist.

/*
 *----------------------------------------------------------------------
 *
 * untraceVar --
 *
 *	Remove a trace from a variable.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public void
untraceVar(
    String name,		// Name of variable;  may end with "(index)"
				// to signify an array reference.
    VarTrace trace,		// Object associated with trace.
    int flags)			// OR-ed collection of bits describing current
				// trace, including any of TCL.TRACE_READS,
				// TCL.TRACE_WRITES, TCL.TRACE_UNSETS,
				// TCL.GLOBAL_ONLY, TCL.NAMESPACE_ONLY and
				// TCL.PARSE_PART1. 
throws
    TclException
{
    untraceVar(name, null, trace, (flags | TCL.PARSE_PART1));
}

/*
 *----------------------------------------------------------------------
 *
 * untraceVar --
 *
 *	Remove a trace from a variable.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public native void
untraceVar(
    String part1,		// Name of scalar variable or array.
    String part2,		// Name of element within array;  null means
				// trace applies to scalar variable or array
				// as-a-whole.  
    VarTrace trace,		// Object associated with trace.
    int flags)			// OR-ed collection of bits describing current
				// trace, including any of TCL.TRACE_READS,
				// TCL.TRACE_WRITES, TCL.TRACE_UNSETS,
				// TCL.GLOBAL_ONLY, TCL.NAMESPACE_ONLY and
				// TCL.PARSE_PART1. 
throws
    TclException;

/*
 *----------------------------------------------------------------------
 *
 * createCommand --
 *
 *	Create a new Tcl command that is implemented by a Java object.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public native void
createCommand(
    String name,		// Name of new command.
    Command cmd);		// Object that implements the command.

/*
 *----------------------------------------------------------------------
 *
 * deleteCommand --
 *
 *	Remove a command from the interpreter.
 *
 * Results:
 *	Returns 0 if the command was deleted successfully, else -1.
 *
 * Side effects:
 *	May invoke the disposeCmd() method on the Command object.
 *
 *----------------------------------------------------------------------
 */

public native int
deleteCommand(
    String name);		// Name of command to delete.

/*
 *----------------------------------------------------------------------
 *
 * getCommand --
 *
 *	Returns the command procedure of the given command.
 *
 * Results:
 *	The command procedure of the given command, or null if
 *      the command doesn't exist.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public native Command
getCommand(
    String name); 		// String name of the command.

/*
 *----------------------------------------------------------------------
 *
 * commandComplete --
 *
 *	Tests if the String is a complete command.
 *
 * Results:
 *	Boolean value.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public static native boolean
commandComplete(
    String cmd);	// Complete or partially complete command

/*
 *----------------------------------------------------------------------
 *
 * getResult --
 *
 *	Retrieve the result of the last interpreter action.
 *
 * Results:
 *	The result object.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public final native TclObject
getResult();

/*
 *----------------------------------------------------------------------
 *
 * setResult --
 *
 *	Set the interpreter result to the given TclObject.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public final native void
setResult(
    TclObject r);		// New result object.

/*
 *----------------------------------------------------------------------
 *
 * setResult --
 *
 *	These routines are convenience wrappers that accept
 *	commonly used Java types and set the interpreter result.
 *	Some create a TclObject type wrapper before setting
 *	the result to this object. Others use native code to
 *	set the interp result directly in C code instead of
 *	creating a TclObject wrapper, since a wrapper
 *	involves a non-trivial amount of overhead.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public final void
setResult(
    String r)			// String to use as result.
{
    setResult(TclString.newInstance(r));
}

public final void
setResult(
    double r)			// Double to use as result.
{
    setResult(TclDouble.newInstance(r));
}

public final native void
setResult(
    int r);			// int to use as result.

public final native void
setResult(
    boolean r);			// boolean to use as result.


/*
 *----------------------------------------------------------------------
 *
 * resetResult --
 *
 *	Clears the interpreter result.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public final native void
resetResult();

/*
 *----------------------------------------------------------------------
 *
 * eval --
 *
 *	Execute a Tcl command in a string or Tcl Object.
 *
 * Results:
 *	The return value is void.  However, a standard Tcl Exception
 *	may be generated.  The interpreter's result object will contain
 *	the value of the evaluation but will persist only until the next 
 *	call to one of the eval functions.
 *
 * Side effects:
 *	The side effects will be determined by the exact Tcl code to be 
 *	evaluated.
 *
 *----------------------------------------------------------------------
 */

private native int
evalString(
    String script,	// A script to evaluate.
    int flags);		// Flags, either 0 or TCL.EVAL_GLOBAL.

private native int
evalTclObject(
    long objPtr,	// Tcl_Obj* from CObject
    String string,	// String to evaluate
    int flags);		// Flags, either 0 or TCL.EVAL_GLOBAL.

public void
eval(
    String script,	// A script to evaluate.
    int flags)		// Flags, either 0 or TCL.EVAL_GLOBAL.
throws
    TclException 	// A standard Tcl exception.
{
    pendingException = null;
    int ccode = evalString(script, flags);
    checkPendingException(ccode);
}

public void 
eval(
    String script)	// A script to evaluate.
throws
    TclException 	// A standard Tcl exception.
{
    eval(script, 0);
}


public void 
eval(
    TclObject tobj,	// A Tcl object holding a script to evaluate.
    int flags)		// Flags, either 0 or TCL.EVAL_GLOBAL.
throws
    TclException 	// A standard Tcl exception.
{
    // Pass the Tcl_Obj ptr or the String object
    // directly to evalTclObject for efficiency

    long objPtr = tobj.getCObjectPtr();
    String str = null;
    if (objPtr == 0) {
        str = tobj.toString();
    }

    pendingException = null;
    tobj.preserve();
    int ccode = evalTclObject(objPtr, str, flags);
    tobj.release();
    checkPendingException(ccode);
}

/*
 *----------------------------------------------------------------------
 *
 * Tcl_RecordAndEvalObj -> recordAndEval
 *
 *	This procedure adds its command argument to the current list of
 *	recorded events and then executes the command by calling eval.
 *
 * Results:
 *	The return value is void.  However, a standard Tcl Exception
 *	may be generated.  The interpreter's result object will contain
 *	the value of the evaluation but will persist only until the next 
 *	call to one of the eval functions.
 *
 * Side effects:
 *	The side effects will be determined by the exact Tcl code to be 
 *	evaluated.
 *
 *----------------------------------------------------------------------
 */

public void 
recordAndEval(
    TclObject script,	// A script to evaluate.
    int flags)		// Flags, either 0 or TCL.EVAL_GLOBAL.
throws 
    TclException 	// A standard Tcl exception.
{
    // FIXME : need native implementation
    throw new TclRuntimeError("Not implemented yet.");
}

/*
 *----------------------------------------------------------------------
 *
 * evalFile --
 *	Loads a Tcl script from a file and evaluates it in the
 * 	current interpreter.
 *
 * Results:
 * 	None.
 *
 * Side effects:
 *	The side effects will be determined by the exact Tcl code to be 
 *	evaluated.
 *
 *----------------------------------------------------------------------
 */

public void
evalFile(
    String s)			// The name of file to evaluate.
throws 
    TclException
{
    // Create pure list object, then evaluate the
    // list object as a command.

    TclObject cmd = TclList.newInstance();
    TclList.append(this, cmd,
        TclString.newInstance("source"));
    TclList.append(this, cmd,
        TclString.newInstance(s));

    eval(cmd, 0);
}

/*
 *----------------------------------------------------------------------
 *
 * evalResource --
 *
 *	Execute a Tcl script stored in the given Java resource location.
 *
 * Results:
 *	The return value is void.  However, a standard Tcl Exception
 *	may be generated. The interpreter's result object will contain
 *	the value of the evaluation but will persist only until the next 
 *	call to one of the eval functions.
 *
 * Side effects:
 *	The side effects will be determined by the exact Tcl code to be 
 *	evaluated.
 *
 *----------------------------------------------------------------------
 */

public
void
evalResource(
    String resName) 	// The location of the Java resource. See
			// the Java documentation of
			// Class.getResourceAsStream()
			// for details on resources naming.
throws
    TclException
{
    InputStream stream = getResourceAsStream(resName);

    if (stream == null) {
	throw new TclException(this, "cannot read resource \"" + resName
		+ "\"");
    }

    // It is not possible to set the "scriptFile" attribute of
    // an interp to something logical here because Tcl Blend's
    // source command can't handle a "resource:/" prefix so
    // getting [info script] to return a path inside a resource
    // is pointless. A better long term fix might be to create
    // a filesystem mount point named "resource:/" and then
    // read files out of Java resources using that prefix.

/*
    String oldScript = scriptFile;
    scriptFile = "resource:" + resName;

    try {
        String script = readScriptFromInputStream(stream);
        eval(script, 0);
    } finally {
        scriptFile = oldScript;
    }
*/

    String script = readScriptFromInputStream(stream);

    eval(script, 0);
}

/*
 *----------------------------------------------------------------------
 *
 * readScriptFromInputStream --
 *
 *	Read a script from a Java InputStream into a string.
 *
 * Results:
 *	Returns the content of the script.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

private String
readScriptFromInputStream(
    InputStream s)			// Java InputStream containing script
{
    BufferedReader r;
    CharArrayWriter w;
    String line = null;

    r = new BufferedReader(new InputStreamReader(s));
    w = new CharArrayWriter();

    try {
        while ((line = r.readLine()) != null){
            w.write(line);
            w.write('\n');
        }
        return w.toString();
    } catch (IOException e) {
        return null;
    } finally {
        closeInputStream(s);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * closeInputStream --
 *
 *	Close the InputStream; catch any IOExceptions and ignore them.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

private void
closeInputStream(
    InputStream fs)
{
    try {
	fs.close();
    }
    catch (IOException e) {}
}

/*
 *----------------------------------------------------------------------
 *
 * callCommand --
 *
 *	Invoke a Command object's cmdProc method and deal with any
 *	errors that result. This method is only ever invoked from
 *	the C function JavaCmdProc via JNI when a command is
 *	evaluated in the Tcl interpreter. This method should never
 *	throw an exception or fail to catch an exception raised
 *	during the cmdProc method, since it is invoked from Tcl
 *	and other JNI layer interactions are not well defined
 *	when an exception is left pending. This method will
 *	set the pendingException field inside the interp if
 *	an exception in the cmdProc method was caught, the caller
 *	should invoke checkPendingException from a Java method
 *	that can throw an exception.
 *
 * Results:
 *	Returns the result code.
 *
 * Side effects:
 *	Whatever the command does.
 *
 *----------------------------------------------------------------------
 */

private int
callCommand(
    Command cmd,		// Command to invoke.
    TclObject[] objv)		// Argument array for command.
{
    final boolean debug = false;

    if (debug) {
        System.err.println("Interp.callCommand()");
        for (int i=0; i < objv.length; i++) {
            System.err.println("objv[" + i + "] = " + objv[i]);
        }
    }

    try {
	CObject.cleanupPush(this);
	pendingException = null;
	cmd.cmdProc(this, objv);

	if (debug) {
	    System.err.println("No Exception in Command.cmdProc() returning TCL.OK");
	}

	return TCL.OK;
    } catch (TclException e) {
	if (debug) {
	    System.err.println("caught TclException during Command.cmdProc()");
	}

	pendingException = e;
	int ccode = e.getCompletionCode();

	if (debug) {
	    System.err.println("pendingException changed to : " +
	        ((pendingException == null) ? null :
	            "non-null : " + pendingException.toString()));
	    System.err.println("returing ccode : " + ccode);
	}

	return ccode;
    } catch (RuntimeException e) {
	if (debug) {
	    System.err.println("caught RuntimeException during Command.cmdProc()");
	}

	pendingException = e;

	if (debug) {
	    System.err.println("pendingException changed to : " +
	        ((pendingException == null) ? null :
	            "non-null : " + pendingException.toString()));
	}

	return TCL.ERROR;
    } finally {
	CObject.cleanupPop(this);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * checkPendingException --
 *
 *	Invoked after Tcl code has been evaluated. This method
 *	checks for a Java exception that might have been raised
 *	during execution of the Tcl code. If there was no Java
 *	exception pending and the result code from the evaluation
 *	is not TCL.OK, then a TclException is raised. This method
 *	will only ever raise a TclException or a RuntimeException.
 *	This method is a no-op if there was no error.
 *
 *----------------------------------------------------------------------
 */

private void
checkPendingException(int ccode)
    throws TclException, RuntimeException
{
    final boolean debug = false;

    if (debug) {
        System.err.println("checkPendingException");
        System.err.println("ccode is " + ccode);
        System.err.println("pendingException is : " +
                ((pendingException == null) ? null :
                    ("non-null : " + pendingException.toString())));
    }

    if (pendingException != null) {
        if (pendingException instanceof TclException) {
            // If the Tcl layer indicated that the return
            // result was not TCL.OK, then raise the pending
            // exception now.

            if (ccode != TCL.OK) {
                TclException te = (TclException) pendingException;
                pendingException = null;
                throw te;
            }
        } else if (pendingException instanceof RuntimeException) {
            // Note that a pending RuntimeException is always
            // thrown even if the Tcl thinks that an error has
            // been caught via Tcl's catch command.

            RuntimeException re = (RuntimeException) pendingException;
            pendingException = null;
            throw re;
        } else {
            pendingException = null;
            throw new TclRuntimeError("expected TclException or RuntimeException" +
                " but got " + pendingException.getClass().getName() + ": " +
                pendingException.getMessage());
        }
    }

    if (ccode != TCL.OK) {
        // If there was no pending exception, but Tcl returns
        // an error code, then wrap the error code into a
        // TclException and throw it. This logic is from
        // the C method JavaThrowTclException().

        String msg = getResult().toString();
        throw new TclException(null, msg, ccode);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * setErrorCode --
 *
 *	These functions set the errorCode variable in the interpreter
 *	to the given value.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Sets the interpreter error state so the interpreter doesn't
 *	set errorCode to NONE after the current eval returns.
 *
 *----------------------------------------------------------------------
 */

public native void
setErrorCode(
    TclObject code);

/*
 *----------------------------------------------------------------------
 *
 * addErrorInfo --
 *
 *	This function adds the given string to the errorInfo variable.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public native void
addErrorInfo(
    String message);		// Message to add to errorInfo

/*
 *----------------------------------------------------------------------
 *
 * backgroundError --
 *
 *	This procedure is invoked to handle errors that occur in Tcl
 *	commands that are invoked in "background" (e.g. from event or
 *	timer bindings).
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	The command "bgerror" is invoked later as an idle handler to
 *	process the error, passing it the error message.  If that fails,
 *	then an error message is output on stderr.
 *
 *----------------------------------------------------------------------
 */

public native void
backgroundError();

/*
 *----------------------------------------------------------------------
 *
 * getNotifier --
 *
 *	Retrieve the Notifier associated with this Interp.
 *
 * Results:
 *	Returns the Notifier.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public Notifier
getNotifier()
{
    return notifier;
}

/*
 *----------------------------------------------------------------------
 *
 * setAssocData --
 *
 *	Creates a named association between user-specified data and
 *	this interpreter.  If the association already exists the
 *	olddata is overwritten with the new data. The
 *	data.deleteAssocData() method will be invoked if the
 *	interpreter is deleted before the association is deleted.
 *
 *	NOTE: deleteAssocData() is not called when old data is
 *	replaced by new data.  The caller of setAssocData() is
 *	responsible for deleting the old data.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Initializes the assocDataTab if necessary.
 *
 *----------------------------------------------------------------------
 */

public void
setAssocData(
    String name,		// Name for association.
    AssocData data)		// Object associated with the name.
{
    if (assocDataTab == null) {
	assocDataTab = new HashMap<String, AssocData>();
    }
    assocDataTab.put(name, data);
}

/*
 *----------------------------------------------------------------------
 *
 * deleteAssocData --
 *
 *	Deletes a named association of user-specified data with
 *	the specified interpreter.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public void
deleteAssocData(
    String name)		// Name of association.
{
    if (assocDataTab == null) {
	return;
    }

    assocDataTab.remove(name);
}

/*
 *----------------------------------------------------------------------
 *
 * getAssocData --
 *
 *	Returns the AssocData instance associated with this name in
 *	the specified interpreter.
 *
 * Results:
 *	The AssocData instance in the AssocData record denoted by the
 *	named association, or null.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public AssocData
getAssocData(
    String name)			// Name of association.
{
    if (assocDataTab == null) {
	return null;
    } else {
	return (AssocData) assocDataTab.get(name);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * init --
 *
 *	Call the init methods on an interpreter pointer.
 *
 * Results:
 *	Returns TCL.OK if the intialization succeeded, else TCL.ERROR.
 *
 * Side effects:
 *	Calls Tcl_Init and Java_Init. 
 *
 *----------------------------------------------------------------------
 */

private final native int
init(
    long interpPtr);		// Tcl_Interp pointer.

/*
 *----------------------------------------------------------------------
 *
 * doDispose --
 *
 *	Call Tcl_DeleteInterp on the given interpPtr.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	lots of callbacks could be invoked.
 *
 *----------------------------------------------------------------------
 */

private static final native void
doDispose(
    long interpPtr);		// Tcl_Interp pointer.

/*
 *----------------------------------------------------------------------
 *
 * pkgProvide --
 *
 *	Call Tcl_PkgProvide on the given interpPtr.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Package and version are stored in the interpPtr.
 *
 *----------------------------------------------------------------------
 */
public final native void
pkgProvide(
    String name, 
    String version);

/*
 *----------------------------------------------------------------------
 *
 * pkgRequire --
 *	Loads the package to the interpPtr.
 *
 * Results:
 *	The version number of the loaded package on success,
 *	otherwise a TclException is generated.
 *
 * Side effects:
 *	Possibly evals a script.
 *
 *----------------------------------------------------------------------
 */

public final native String
pkgRequire(
    String pkgname, 
    String version, 
    boolean exact);

/*
 *----------------------------------------------------------------------
 *
 * createBTestCommand --
 *
 *	Create a Tcl command called "btest", used for
 *	test cases and debugging Tcl Blend.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

native void
createBTestCommand();


/*
 *----------------------------------------------------------------------
 *
 * getClassLoader --
 *
 *	Get the TclClassLoader used for the interp. This
 *	class loader delagates to the context class loader
 *	which delagates to the system class loader.
 *	The TclClassLoader will read classes and resources
 *	from the env(TCL_CLASSPATH).
 *
 * Results:
 *	This method will return the classloader in use,
 *	it will never return null.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public
ClassLoader
getClassLoader()
{
    // Allocate a TclClassLoader that will delagate to the
    // context class loader, or to the loader that loaded
    // tcl.lang.Interp, or to the system class loader.
    // If the parent class loader can't find the class
    // or resource, then env(TCL_CLASSPATH) is searched.

    if (classLoader == null) {
        ClassLoader ctx = Thread.currentThread().getContextClassLoader();
        if (ctx == null) {
            ctx = Interp.class.getClassLoader();
        }
        if (ctx == null) {
            ctx = ClassLoader.getSystemClassLoader();
        }
        if (ctx == null) {
            throw new TclRuntimeError("could not locate parent class loader");
        }

        classLoader = new TclClassLoader(this, null, ctx);
    }
    return classLoader;
}

/*
 *----------------------------------------------------------------------
 *
 * getResourceAsStream --
 *
 *	Resolve a resource name into an InputStream. This method
 *	will search for a resource using the TclClassLoader.
 *	This method will return null if a resource can't be found.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

InputStream
getResourceAsStream(String resName)
{
    if (classLoader == null) {
        getClassLoader();
    }

    try {
        return classLoader.getResourceAsStream(resName);
    } catch (PackageNameException e) {
        return null;
    } catch (SecurityException e) {
        return null;
    }
}

} // end Interp


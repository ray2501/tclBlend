/*
 * TclPkgInvoker --
 *
 *	This class tests the tcl.lang.reflect.PkgInvoker class. Please
 *	see the comments in PkgInvoker.java for details.
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 * RCS: @(#) $Id: TclPkgInvoker.java,v 1.1 1999/05/10 04:08:57 dejong Exp $
 *
 */

package pkg1;

import tcl.lang.reflect.*;
import java.lang.reflect.*;
import java.beans.*;

public class TclPkgInvoker extends PkgInvoker {

/*
 *----------------------------------------------------------------------
 *
 * invokeConstructor --
 *
 *	Invoke the given constructor with the arguments.
 *
 * Results:
 *	The new object instance returned by the constructor.
 *
 * Side effects:
 *	The constructor may have arbitraty side effects.
 *
 *----------------------------------------------------------------------
 */

public Object 
invokeConstructor(
    Constructor constructor,	// The constructor to invoke.
    Object args[])		// Arguments for the constructor.
throws
    InstantiationException,	// Standard exceptions thrown by
    IllegalAccessException,	// Constructor.newInstance.
    IllegalArgumentException,
    InvocationTargetException
{
    return constructor.newInstance(args);
}

/*
 *----------------------------------------------------------------------
 *
 * invokeMethod --
 *
 *	Invoke the given method of the obj with the arguments.
 *
 * Results:
 *	The value returned by the method.
 *
 * Side effects:
 *	The method may have arbitraty side effects.
 *
 *----------------------------------------------------------------------
 */

public Object
invokeMethod(
    Method method,		// The method to invoke.
    Object obj,			// The object associated with the method.
				// May be null if the method is static.
    Object args[])		// The arguments for the method.
throws
    IllegalAccessException,	// Standard exceptions throw by Method.Invoke.
    IllegalArgumentException,
    InvocationTargetException
{
    return method.invoke(obj, args);
}

/*
 *----------------------------------------------------------------------
 *
 * getField --
 *
 *	Query the value of the given field.
 *
 * Results:
 *	The value of the field.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */


public Object
getField(
    Field field,		// The field to query.
    Object obj)			// The object that owns the field. May be
				// null for static fields.
throws
    IllegalArgumentException,	// Standard exceptions thrown by Field.get().
    IllegalAccessException
{
    return field.get(obj);
}

/*
 *----------------------------------------------------------------------
 *
 * setField --
 *
 *	Modify the value of the given field.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	When successful, the field is modified to be the new value.
 *
 *----------------------------------------------------------------------
 */

public void
setField(
    Field field,		// The field to modify.
    Object obj,			// The object that owns the field. May be
				// null for static fields.
    Object value)		// New value for the field.
throws
    IllegalArgumentException,	// Standard exceptions thrown by Field.set().
    IllegalAccessException
{
    field.set(obj, value);
}

} // end TclPkgInvoker


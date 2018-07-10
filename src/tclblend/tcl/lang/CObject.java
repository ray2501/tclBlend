/* 
 * CObject.java --
 *
 *	This class is used as the internal rep for TclObject instances
 *	that refer to C based Tcl_Obj structures.
 *
 * Copyright (c) 1997 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: CObject.java,v 1.7 2006/01/26 19:49:19 mdejong Exp $
 */

package tcl.lang;

import java.util.ArrayList;

// The CObject class encapsulates a reference to a Tcl_Obj implemented in
// native code.  When an object is passed to Java from C, a new CObject is
// constructed to hold the object pointer.  A TclList can also be created
// in Java code, which will allocate a native Tcl_Obj for its own use.
// A TclList allocated in Java code will add to a special cleanup queue
// which will be flushed when the Java method returns. The reference count
// of the native Tcl_Obj is not incremented implicitly.  It is assumed to
// be zero for a TclList allocated in Java. A Tcl object passed into Java
// will have a ref count of at least 1. The preserve() and release()
// methods of the TclObject class can be used to modify the ref count
// of the underlying Tcl_Obj.

class CObject implements InternalRep {

// This long really contains a Tcl_Obj*.  It is declared with protected
// visibility so that subclasses that define type specific functionality
// can query the Tcl_Obj*. This field can be read from C code. The
// final modifier is commented out because the javac in JDK 1.1 is
// stupid and generates an incorrect compiler error.

protected /*final*/ long objPtr;

// Number of times the refCount for the wrapped Tcl_Obj has
// been incremented. This is needed for the case where
// dispose() is called before all the C side references have
// been released.
protected int refsHeld;

// Status flags
protected boolean refCountUnchanged;
protected boolean onCleanupQueue;
protected boolean emptyNeedsCleanup;
protected boolean disposed;


/*
 *----------------------------------------------------------------------
 *
 * CObject --
 *
 *	Construct a new CObject around a newly allocated Tcl_Obj.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

protected CObject()
{
    this(newCObject(null));
    emptyNeedsCleanup = true;
    
    if (TclObject.saveObjRecords) {
        String key = "CObject";
        Integer num = (Integer) TclObject.objRecordMap.get(key);
        if (num == null) {
            num = new Integer(1);
        } else {
            num = new Integer(num.intValue() + 1);
        }
	TclObject.objRecordMap.put(key, num);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * CObject --
 *
 *	Construct a new CObject to wrap the given Tcl_Obj.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

protected CObject(
    long objPtr)		// Pointer to Tcl_Obj from C.
{
    if (objPtr == 0)
        throw new TclRuntimeError("objPtr can not be 0");
    this.objPtr = objPtr;
    refsHeld = 0;
    refCountUnchanged = true;
    onCleanupQueue = false;
    emptyNeedsCleanup = false;
    disposed = false;
    //System.err.println("new CObject() \"" + toString() + "\" " + Long.toHexString(objPtr));

    if (TclObject.saveObjRecords) {
        String key = "CObject";
        Integer num = (Integer) TclObject.objRecordMap.get(key);
        if (num == null) {
            num = new Integer(1);
        } else {
            num = new Integer(num.intValue() + 1);
        }
	TclObject.objRecordMap.put(key, num);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * dispose --
 *
 *	Dispose of the CObject.
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
dispose()
{
    if (disposed)
        throw new TclRuntimeError("CObject already disposed");

    while (refsHeld > 0) {
        decrRefCount();
    }
    if (refCountUnchanged && (onCleanupQueue || emptyNeedsCleanup)) {
        // An object that is on the cleanup queue will need
        // to be deallocated now. Do this by incrementing
        // and then decrementing the native ref count. This
        // will only deallocate the object if it has never
        // been incremented or decremented on the Java side
        // or the C side. This same cleanup is also needed
        // for an empty object.

        //System.out.println("cleaning up " + Long.toHexString(objPtr) + " in dispose");
        incrRefCount(objPtr);
        decrRefCount(objPtr);
        emptyNeedsCleanup = false;
    }
    disposed = true;
}

/*
 *----------------------------------------------------------------------
 *
 * duplicate --
 *
 *	Makes a new CObject that refers to the same Tcl_Obj.  Note
 *	that we don't modify the Tcl_Obj ref count here.
 *
 * Results:
 *	Returns a new CObject instance
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public InternalRep
duplicate()
{
    if (disposed)
        throw new TclRuntimeError("CObject was disposed");

    if (TclObject.saveObjRecords) {
        String key = "CObject.duplicate()";
        Integer num = (Integer) TclObject.objRecordMap.get(key);
        if (num == null) {
            num = new Integer(1);
        } else {
            num = new Integer(num.intValue() + 1);
        }
	TclObject.objRecordMap.put(key, num);
    }
   
    return new CObject(objPtr);
}

/*
 *----------------------------------------------------------------------
 *
 * toString --
 *
 *	Return the string form of the internal rep.  Calls down to
 *	native code to get the string rep of the Tcl_Obj.
 *
 * Results:
 *	Returns the string rep.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public String
toString()
{
    //System.out.println("CObject.toString() for " + Long.toHexString(objPtr));
    if (disposed)
        throw new TclRuntimeError("CObject was disposed");
    return getString(objPtr);
}

/*
 *----------------------------------------------------------------------
 *
 * newInstance --
 *
 *	Construct a new TclObject from a Tcl_Obj*.  This routine is only
 *	called from C. It is also the only CObject method that can be
 *	called from C.
 *
 * Results:
 *	Returns a newly allocated TclObject.
 *
 * Side effects:
 *	Constructs a new CObject.
 *
 *----------------------------------------------------------------------
 */

private static TclObject
newInstance(
    long objPtr)		// Tcl_Obj to wrap.
{
    //System.out.println("called CObject.newInstance(" + Long.toHexString(objPtr) + ")");
    return new TclObject(new CObject(objPtr));
}

/*
 *----------------------------------------------------------------------
 *
 * decrRefCount --
 *
 *	Decrement the refcount of a Tcl_Obj.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	May delete the Tcl_Obj.
 *
 *----------------------------------------------------------------------
 */

private static final native void
decrRefCount(
    long objPtr);		// Pointer to Tcl_Obj.

final void
decrRefCount() {
    if (disposed)
        throw new TclRuntimeError("CObject was disposed");
    decrRefCount(objPtr);
    refsHeld--;
    refCountUnchanged = false;
}

/*
 *----------------------------------------------------------------------
 *
 * incrRefCount --
 *
 *	 Increment the reference count of a Tcl_Obj.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

private static final native void
incrRefCount(
    long objPtr);		// Pointer to Tcl_Obj.

final void
incrRefCount() {
    if (disposed)
        throw new TclRuntimeError("CObject was disposed");
    incrRefCount(objPtr);
    refsHeld++;
    refCountUnchanged = false;
}

/*
 *----------------------------------------------------------------------
 *
 * newCObject --
 *
 *	Allocate a new Tcl_Obj with the given string rep.
 *
 * Results:
 *	Returns the address of the new Tcl_Obj with refcount of 0.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

private static final native long
newCObject(
    String rep);		// Initial string rep.

/*
 *----------------------------------------------------------------------
 *
 * getString --
 *
 *	Retrieve the string rep of a Tcl_Obj.
 *
 * Results:
 *	Returns a string.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

private static final native String
getString(
    long objPtr);		// Pointer to Tcl_Obj.

/*
 *----------------------------------------------------------------------
 *
 * makeReference --
 *
 *	Convert the underlying Tcl_Obj into a TclObject reference.
 *	This method is only called from TclObject.setInternalRep().
 *	The complication here is that we do not want to convert
 *	a Tcl_Obj allocated in Java code into a ref to a TclObject
 *	since it does not represent a ref held by Tcl and would
 *	not be deallocated by Tcl later on.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	May change the type of the underlying Tcl_Obj but not its
 *	refcount. Will increment the refcount of the TclObject.
 *
 *----------------------------------------------------------------------
 */

final void
makeReference(
    TclObject object)		// The object to create a new reference to.
{
    if (!onCleanupQueue) {
        makeRef(objPtr, object);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * makeRef --
 *
 *	Convert a Tcl_Obj into a reference to a TclObject.  This routine
 *	is used when the internal rep is being set from the Java side.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Changes the internal representation of the Tcl_Obj.
 *
 *----------------------------------------------------------------------
 */

private static final native void
makeRef(
    long objPtr,		// Pointer to Tcl_Obj.
    TclObject object);		// Object that Tcl_Obj should refer to.

/*
 *----------------------------------------------------------------------
 *
 * getCObjectPtr --
 *
 *	Return the objPtr member, this method should only be called
 *	from the TclObject.getCObjectPtr() method.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

final long
getCObjectPtr() {
    return objPtr;
}

/*
 *----------------------------------------------------------------------
 *
 * cleanupAdd --
 *
 *	Add a CObject to the special set of objects that
 *	were allocated in Java and need to be explicitly
 *	cleaned up. If an object was incremented from
 *	Java or C, it will not be cleaned up here.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

static void cleanupAdd(Interp interp, CObject cobj) {
    if (cobj == null)
        throw new NullPointerException();
    if (cobj.onCleanupQueue == true)
        throw new TclRuntimeError("CObject already in cleanup queue");
    if (cobj.disposed == true)
        throw new TclRuntimeError("CObject already disposed");
    interp.cobjCleanup.add(cobj);
    cobj.onCleanupQueue = true;
    //System.out.println("added \"" + cobj.toString() + "\" " + Long.toHexString(cobj.objPtr) + " to cleanup queue");
    //System.out.println("cleanupAdd");
    //dump(interp.cobjCleanup);
}

/*
 *----------------------------------------------------------------------
 *
 * cleanupPush --
 *
 *	Push is invoked before a method implemented in Java
 *	is invoked. The effect is that any of the objects
 *	already added will not be cleaned up until a
 *	corresponding pop, after the method has completed.
 *	This method is only invoked by Interp.callCommand().
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

static void cleanupPush(Interp interp) {
    interp.cobjCleanup.add(null);
    //System.out.println("cleanupPush");
    //dump(interp.cobjCleanup);
}

/*
 *----------------------------------------------------------------------
 *
 * cleanupPop --
 *
 *	Pop is invoked after a method implemented in Java
 *	is invoked. The effect is that any of the objects
 *	added since the last push will be cleaned up.
 *	This method is only invoked by Interp.callCommand().
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Can deallocate native Tcl_Objs.
 *
 *----------------------------------------------------------------------
 */

static void cleanupPop(Interp interp) {
    //System.out.println("cleanupPop (before)");
    //dump(interp.cobjCleanup);
    ArrayList<CObject> cleanup = interp.cobjCleanup;
    int last = cleanup.size() - 1;
    CObject cobj = (CObject) cleanup.get(last);
    cleanup.remove(last);

    while (cobj != null) {
        if (cobj.onCleanupQueue == false)
            throw new TclRuntimeError("CObject not in queue");

        // Increment and then decrement the ref count to deallocate
        // the native pointer. This will only deallocate the ref count
        // if it is 0, meaning the ref count was never changed. If the
        // C side has incremented the ref count this will do nothing.

        if (cobj.refCountUnchanged && !cobj.disposed) {
            //System.out.println("cleaning up \"" + cobj.toString() + "\" " + Long.toHexString(cobj.objPtr) + " in queue");
            incrRefCount(cobj.objPtr);
            decrRefCount(cobj.objPtr);
            //System.out.println("done cleaning up");
        }
        cobj.onCleanupQueue = false;

        last -= 1;
        cobj = (CObject) cleanup.get(last);
        cleanup.remove(last);
    }
}

private static void dump(ArrayList alist) {
    java.util.ListIterator iter = alist.listIterator();
    CObject cobj;
    while (iter.hasNext()) {
        cobj = (CObject) iter.next();
        if (cobj == null) {
            System.out.println("XXX FRAME PUSHED XXX");
        } else {
            if (cobj.disposed) {
                System.out.println("XXX " + Long.toHexString(cobj.objPtr) + " DISPOSED XXX");
            } else {
                System.out.println("\"" + cobj.toString() + "\" at " + Long.toHexString(cobj.objPtr));
            }
        }
    }
    System.out.println("-----------------");
}

} // end CObject


/*
 * TclObject.java
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: TclObject.java,v 1.4 2006/06/09 20:13:39 mdejong Exp $
 *
 */

package tcl.lang;

import java.util.Hashtable;

/**
 * This class extends TclObjectBase to implement the basic notion of
 * an object in Tcl.
 */

public final class TclObject extends TclObjectBase {

    static final boolean saveObjRecords = TclObjectBase.saveObjRecords;
    static Hashtable<String, Integer> objRecordMap = TclObjectBase.objRecordMap;

    /**
     * Creates a TclObject with the given InternalRep. This method should be
     * called only by an InternalRep implementation.
     *
     * @param rep the initial InternalRep for this object.
     */
    public TclObject(final InternalRep rep) {
        super(rep);
    }

    /**
     * Creates a TclObject with the given InternalRep and stringRep.
     * This constructor is used by the TclString class only. No other place
     * should call this constructor.
     *
     * @param rep the initial InternalRep for this object.
     * @param s the initial string rep for this object.
     */
    protected TclObject(final TclString rep, final String s) {
        super(rep, s);
    }

    /**
     * Creates a TclObject with the given integer value.
     * This constructor is used by the TclInteger class only. No other place
     * should call this constructor.
     *
     * @param ivalue the integer value
     */
    protected TclObject(final int ivalue) {
        super(ivalue);
    }

    /**
     * Change the internal rep of the object. The old internal rep
     * will be deallocated as a result. This method should be
     * called only by an InternalRep implementation. This method
     * overloads the setInternalRep() method in TclObjectBase
     * so that the CObject internal rep special case is handled.
     *
     * @param rep the new internal rep.
     */
    public void setInternalRep(InternalRep rep) {
	if (internalRep == null) {
	    disposedError();
	}
	if (rep == null) {
	    throw new TclRuntimeError("null InternalRep");
	}
	if (rep == internalRep) {
	    return;
	}

	// In the special case where the internal representation is a CObject,
	// we want to call the special interface to convert the underlying
	// native object into a reference to the Java TclObject.  Note that
	// this test will always fail if we are not using the native
	// implementation. Also note that the makeReference method
	// will do nothing in the case where the Tcl_Obj inside the
	// CObject was originally allocated in Java. When converting
	// to a CObject we need to break the link made earlier.

	if ((internalRep instanceof CObject) && !(rep instanceof CObject)) {
	    // We must ensure that the string rep is copied into Java
	    // before we lose the reference to the underlying CObject.
	    // Otherwise we will lose the original string information
	    // when the backpointer is lost.

	    if (stringRep == null) {
		stringRep = internalRep.toString();
	    }
	    ((CObject) internalRep).makeReference(this);
	}

        //System.out.println("TclObject setInternalRep for \"" + stringRep + "\"");
        //System.out.println("from \"" + internalRep.getClass().getName() +
        //    "\" to \"" + rep.getClass().getName() + "\"");
	internalRep.dispose();
	internalRep = rep;
	ivalue = 0;
    }

    /**
     * Tcl_IncrRefCount -> preserve
     *
     * Increments the refCount to indicate the caller's intent to
     * preserve the value of this object. Each preserve() call must be matched
     * by a corresponding release() call.
     *
     * @exception TclRuntimeError if the object has already been deallocated.
     */
    public final void preserve() {
	if (internalRep == null) {
	    disposedError();
	}
	if (internalRep instanceof CObject) {
	    ((CObject) internalRep).incrRefCount();
	}
	_preserve();
    }

    /**
     * _preserve
     *
     * Private implementation of preserve() method.
     * This method will be invoked from Native code
     * to change the TclObject's ref count without
     * effecting the ref count of a CObject.
     */
    private final void _preserve() {
	refCount++;
    }

    /**
     * Tcl_DecrRefCount -> release
     *
     * Decrements the refCount to indicate that the caller is no longer
     * interested in the value of this object. If the refCount reaches 0,
     * the obejct will be deallocated.
     */
    public final void release() {
	if (internalRep == null) {
	    disposedError();
	}
	if (internalRep instanceof CObject) {
	    ((CObject) internalRep).decrRefCount();
	}
	_release();
    }

    /**
     * _release
     *
     * Private implementation of preserve() method.
     * This method will be invoked from Native code
     * to change the TclObject's ref count without
     * effecting the ref count of a CObject.
     */
    private final void _release() {
	if (--refCount <= 0) {
	    disposeObject();
	}
    }

    /**
     * Returns the Tcl_Obj* objPtr member for a CObject or TclList.
     * This method is only called from Tcl Blend.
     */

    final long getCObjectPtr() {
	if (internalRep instanceof CObject) {
	    return ((CObject) internalRep).getCObjectPtr();
	} else {
	    return 0;
	}
    }

    /**
     * Returns 2 if the internal rep is a TclList.
     * Returns 1 if the internal rep is a CObject.
     * Otherwise returns 0.
     * This method provides an optimization over
     * invoking getInternalRep() and two instanceof
     * checks via JNI. It is only used by Tcl Blend.
     */

    final int getCObjectInst() {
	if (internalRep instanceof CObject) {
	    if (internalRep instanceof TclList)
	        return 2;
	    else
	        return 1;
	} else {
	    return 0;
	}
    }

    /**
     * Return a String that describes TclObject and internal
     * rep type allocations and conversions. The string is
     * in lines separated by newlines. The saveObjRecords
     * needs to be set to true and Jacl recompiled for
     * this method to return a useful value.
     */

    public static String getObjRecords() {
        return TclObjectBase.getObjRecords();
    }

}


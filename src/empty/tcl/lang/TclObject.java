// empty implementation of TclObject used for compiling in multiple packages
// only those functions called from tcljava classes are listed here.

package tcl.lang;

import java.util.Hashtable;

public final class TclObject extends TclObjectBase {

static final boolean saveObjRecords = TclObjectBase.saveObjRecords;
static Hashtable<String, Integer> objRecordMap = TclObjectBase.objRecordMap;

public TclObject(final InternalRep rep) {
    super(rep);
}

protected TclObject(final TclString rep, final String s) {
    super(rep, s);
}

protected TclObject(final int ivalue) {
    super(ivalue);
}

public final void preserve() {}

public final void release() {}

} // end TclObject

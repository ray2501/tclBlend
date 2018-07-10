/*
empty implementation of TclList used for compiling in multiple packages
*/

package tcl.lang;

import java.util.*;

public class TclList extends CObject {

TclList() {}

TclList(long objPtr) {}

public static TclObject newInstance()
{
    return null;
}

static void
setListFromAny(Interp interp, TclObject tobj)
throws TclException {}

public static final void append(Interp interp, TclObject tobj, TclObject elemObj)
throws TclException {}

public static final int getLength(Interp interp, TclObject tobj)
throws TclException
{
  return 0;
}

public static TclObject[] getElements(Interp interp, TclObject tobj)
throws TclException
{
    return null;
}

public static final TclObject index(Interp interp, TclObject tobj, int index)
throws TclException
{
    return null;
}

public static final void
replace(
    Interp interp,
    TclObject tobj,
    int index,	
    int count,
    TclObject elements[],
    int from,
    int to)
throws TclException {}

} // end TclList


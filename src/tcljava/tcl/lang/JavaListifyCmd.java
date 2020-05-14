/* 
 * JavaListifyCmd.java --
 *
 *	This class implements the built-in "java::listify" command in Tcl.
 */

package tcl.lang;

import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;


class JavaListifyCmd implements Command {

private static boolean isBasic (Class cls) {
    if (
	   (cls == Integer.class) ||
	   (cls == Long.class)    ||
	   (cls == Short.class)   ||
	   (cls == Byte.class)    ||
	   (cls == Double.class)  ||
	   (cls == Float.class)   ||
	   (cls == Boolean.class) ||
	   (cls == Character.class) ||
	   (cls == String.class) 
	   ) { return true; }
	   
	return false;
}

/*
 *----------------------------------------------------------------------
 *
 * cmdProc --
 *
 *	This procedure is invoked to process the "java::listify" Tcl
 *      command.  See the user documentation for details on what it
 *      does.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	A standard Tcl result is stored in the interpreter.
 *
 *----------------------------------------------------------------------
 */

public void
cmdProc(
    Interp interp,              // The current interpreter.
    TclObject argv[])           // The command arguments.
throws
    TclException		// Standard Tcl Exception.
{
	boolean conversionToBasic;
	Class forcedCastTo = null;
    Object collectionObj;
	
	switch (argv.length) {
		case 2: {
			conversionToBasic = true;
			forcedCastTo = null;
		    collectionObj = ReflectObject.get(interp, argv[1]);
			break;
		}
		case 3: {
			conversionToBasic = false;
			String arg1 = argv[1].toString();
			if ("-noconvert".equals(arg1)) {
				forcedCastTo = null;
			} else {
				forcedCastTo = ClassRep.get(interp, argv[1]);
			}
		    collectionObj = ReflectObject.get(interp, argv[2]);
			break;
		}
		default: {
			throw new TclNumArgsException(interp, 1, argv, "?-noconvert|class? collectionObject");
		}
	}

	TclObject resultListObj = TclList.newInstance();

	if (collectionObj == null) {	
		interp.setResult(resultListObj);
		return;
	} 	
	
	//  collectionObj must be conform to the Collection interface, or (*plus*)
	//   conform to the Enumeration interface

	boolean isCollection = (collectionObj instanceof Collection);
	boolean isEnumeration = (collectionObj instanceof Enumeration);
	
	if ( ! isCollection  && ! isEnumeration) {
		throw new TclException(interp, "passed argument " +
			"of type " +
			JavaInfoCmd.getNameFromClass(collectionObj.getClass()) +
			" which does not implement the Collection nor the Enumeration interface");
	}
	Iterator iterator = null;
	if ( isCollection ) {
		iterator= ((Collection) collectionObj).iterator();
    }
	try {
	  Object elem;
	  TclObject tclObj;
	
	  while ( true ) {
	  	boolean hasNext ;
	  	if ( isCollection ) {
			hasNext = iterator.hasNext();  
		} else {
			hasNext = ((Enumeration)collectionObj).hasMoreElements();
		}
		if ( ! hasNext ) break;

	  	if ( isCollection ) {
			elem = iterator.next();  
		} else {
			elem = ((Enumeration)collectionObj).nextElement();
		}
		
	    if (elem == null) {
	    	if (conversionToBasic) {
				tclObj = TclString.newInstance("");
			} else {
				tclObj = ReflectObject.newInstance(interp, null, null);
			}
		} else {
			Class cls = elem.getClass();
			if (conversionToBasic && isBasic(cls)) {
				tclObj = TclString.newInstance(elem.toString());
			}  else {
				if( forcedCastTo!= null ) cls = forcedCastTo;
				tclObj = ReflectObject.newInstance(interp, cls, elem);
			}
		}
		TclList.append(interp, resultListObj, tclObj);
	  }
	} catch (TclException e) {
		resultListObj.release();
		throw e;
	}
	interp.setResult(resultListObj);
}

}

/* 
 * TestObjCmd.java --
 *
 *	This file contains command procedures for the additional Tcl
 *	commands that are used for testing implementations of the Tcl object
 *	types. These commands are not normally included in Tcl
 *	applications; they're only used for testing. Ported from tclTestObj.c.
 *
 * Copyright (c) 1997 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: TestObjCmd.java,v 1.2 2005/10/12 22:39:39 mdejong Exp $
 */

package tcl.lang;

public class TestObjCmd implements Command {

// An array of TclObject pointers used in the commands that operate on or get
// the values of Tcl object-valued variables. varPtr[i] is the i-th
// variable's TclObject.

final static int NUMBER_OF_OBJECT_VARS = 20;
final static TclObject[] varPtr = new TclObject[NUMBER_OF_OBJECT_VARS];


/*
 *----------------------------------------------------------------------
 *
 * TclObjTest_Init -> TestObjCmd.init()
 *
 *	This procedure creates additional commands that are used to test the
 *	Tcl object support.
 *
 * Results:
 *
 *
 * Side effects:
 *	Creates and registers several new testing commands.
 *
 *----------------------------------------------------------------------
 */

public static void init(Interp interp)
{
    int i;

    for (i = 0;  i < NUMBER_OF_OBJECT_VARS;  i++) {
        varPtr[i] = null;
    }

    interp.createCommand("testbooleanobj", new TestBooleanObjCmd());
    interp.createCommand("testconvertobj", new TestConvertObjCmd());
    interp.createCommand("testdoubleobj", new TestDoubleObjCmd());
    interp.createCommand("testintobj", new TestIntObjCmd());
    interp.createCommand("testindexobj", new TestIndexObjCmd());
    interp.createCommand("testobj", new TestObjCmd());
    interp.createCommand("teststringobj", new TestStringObjCmd());
}


/*
 *----------------------------------------------------------------------
 *
 * cmdProc --
 *
 *	This method implements the "testobject" command.
 *
 * Results:
 *	A standard Tcl result.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public void
cmdProc(
    Interp interp,		// The current Tcl interpreter.
    TclObject[] objv)		// The arguments passed to the command.
throws
    TclException		// The standard Tcl exception.
{
    TestObjCmdImpl.cmdProc(interp, objv);
}

} // end TestObjectCmd



/*
 *----------------------------------------------------------------------
 *
 * TestbooleanobjCmd -> TestBooleanObjCmd
 *
 *	This class implements the "testbooleanobj" command.  It is used
 *	to test the boolean Tcl object type implementation.
 *
 * Results:
 *	A standard Tcl object result.
 *
 * Side effects:
 *	Creates and frees boolean objects, and also converts objects to
 *	have boolean type.
 *
 *----------------------------------------------------------------------
 */

class TestBooleanObjCmd implements Command {

public void
cmdProc(
    Interp interp,		// The current Tcl interpreter.
    TclObject[] objv)		// The arguments passed to the command.
throws
    TclException		// The standard Tcl exception.
{
    int varIndex;
    boolean boolValue;
    String index, subCmd;

    if (objv.length < 3) {
        throw new TclNumArgsException(interp, 1, objv, 
		"option ?arg arg ...?");
    }

    index = objv[2].toString();
    varIndex = TestObjCmdUtil.GetVariableIndex(interp, index);

    subCmd = objv[1].toString();
    if (subCmd.equals("set")) {
	if (objv.length != 4) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option ?arg arg ...?");
	}
	boolValue = TclBoolean.get(interp, objv[3]);

        // The C implementation changes the internal rep of an unshared
        // object in the varPtr array. Jacl does not support functions
        // like Tcl_SetBooleanObj() so always use SetVarToObj().

        TestObjCmdUtil.SetVarToObj(varIndex, TclBoolean.newInstance(boolValue));
        interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else if (subCmd.equals("get")) {
	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option ?arg arg ...?");
	}
	TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else if (subCmd.equals("not")) {
	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option ?arg arg ...?");
	}
	TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	boolValue = TclBoolean.get(interp, TestObjCmd.varPtr[varIndex]);

        // The C implementation changes the internal rep of an unshared
        // object in the varPtr array. Jacl does not support functions
        // like Tcl_SetBooleanObj() so always use SetVarToObj().

        TestObjCmdUtil.SetVarToObj(varIndex, TclBoolean.newInstance(!boolValue));
        interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else {
	throw new TclException(interp,
            "bad option \"" + objv[1] +
            "\": must be set, get, or not");
    }
}

} // end class TestBooleanObjCmd



/*
 *----------------------------------------------------------------------
 *
 * TestconvertobjCmd -> TestConvertObjCmd
 *
 *	This procedure implements the "testconvertobj" command. It is used
 *	to test converting objects to new types.
 *
 * Results:
 *	A standard Tcl object result.
 *
 * Side effects:
 *	Converts objects to new types.
 *
 *----------------------------------------------------------------------
 */

class TestConvertObjCmd implements Command {

public void
cmdProc(
    Interp interp,		// The current Tcl interpreter.
    TclObject[] objv)		// The arguments passed to the command.
throws
    TclException		// The standard Tcl exception.
{
    String subCmd;

    if (objv.length < 3) {
	throw new TclNumArgsException(interp, 1, objv, 
		"option arg ?arg ...?");
    }

    subCmd = objv[1].toString();
    if (subCmd.equals("double")) {
	double d;

	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	d = TclDouble.get(interp, objv[2]);
	interp.setResult("" + d); // Convert double to String
    } else {
	throw new TclException(interp,
	    "bad option \"" + objv[1] +
	    "\": must be double");
    }
}

}  // end class TestConvertObjCmd



/*
 *----------------------------------------------------------------------
 *
 * TestdoubleobjCmd --
 *
 *	This procedure implements the "testdoubleobj" command.  It is used
 *	to test the double-precision floating point Tcl object type
 *	implementation.
 *
 * Results:
 *	A standard Tcl object result.
 *
 * Side effects:
 *	Creates and frees double objects, and also converts objects to
 *	have double type.
 *
 *----------------------------------------------------------------------
 */


class TestDoubleObjCmd implements Command {

public void
cmdProc(
    Interp interp,		// The current Tcl interpreter.
    TclObject[] objv)		// The arguments passed to the command.
throws
    TclException		// The standard Tcl exception.
{
    int varIndex;
    double doubleValue;
    String index, subCmd, string;

    if (objv.length < 3) {
	throw new TclNumArgsException(interp, 1, objv, 
		"option arg ?arg ...?");
    }

    index = objv[2].toString();
    varIndex = TestObjCmdUtil.GetVariableIndex(interp, index);

    subCmd = objv[1].toString();
    if (subCmd.equals("set")) {
	if (objv.length != 4) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	string = objv[3].toString();
	doubleValue = Util.getDouble(interp, string);

        // The C implementation changes the internal rep of an unshared
        // object in the varPtr array. Jacl does not support functions
        // like Tcl_SetDoubleObj() so always use SetVarToObj().

        TestObjCmdUtil.SetVarToObj(varIndex, TclDouble.newInstance(doubleValue));
        interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else if (subCmd.equals("get")) {
	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else if (subCmd.equals("mult10")) {
	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	doubleValue = TclDouble.get(interp, TestObjCmd.varPtr[varIndex]);

        // The C implementation changes the internal rep of an unshared
        // object in the varPtr array. Jacl does not support functions
        // like Tcl_SetDoubleObj() so always use SetVarToObj().

        TestObjCmdUtil.SetVarToObj(varIndex,
            TclDouble.newInstance((doubleValue * 10.0)));
	interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else if (subCmd.equals("div10")) {
	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	doubleValue = TclDouble.get(interp, TestObjCmd.varPtr[varIndex]);

        // The C implementation changes the internal rep of an unshared
        // object in the varPtr array. Jacl does not support functions
        // like Tcl_SetDoubleObj() so always use SetVarToObj().

        TestObjCmdUtil.SetVarToObj(varIndex,
            TclDouble.newInstance((doubleValue / 10.0)));
	interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else {
	throw new TclException(interp,
	    "bad option \"" + objv[1] +
	    "\": must be set, get, mult10, or div10");
    }
}

}  // end class TestDoubleObjCmd



/*
 *----------------------------------------------------------------------
 *
 * TestindexobjCmd -> TestIndexObjCmd
 *
 *	This procedure implements the "testindexobj" command. It is used to
 *	test the index Tcl object type implementation.
 *
 * Results:
 *	A standard Tcl object result.
 *
 * Side effects:
 *	Creates and frees int objects, and also converts objects to
 *	have int type.
 *
 *----------------------------------------------------------------------
 */


class TestIndexObjCmd implements Command {

public void
cmdProc(
    Interp interp,		// The current Tcl interpreter.
    TclObject[] objv)		// The arguments passed to the command.
throws
    TclException		// The standard Tcl exception.
{
    boolean  allowAbbrev, setError;
    int index, index2, i, result;
    String[] argv;
    String[] tablePtr = {"a", "b", "check", null};

    InternalRep indexRep;

    if ((objv.length == 3) && (objv[1].toString().equals("check"))) {
	// This code checks to be sure that the results of
	// Tcl_GetIndexFromObj are properly cached in the object and
	// returned on subsequent lookups.

	index2 = TclInteger.get(interp, objv[2]);

	index = TclIndex.get(null, objv[1], tablePtr,
	        "token", 0);
	indexRep = objv[1].getInternalRep();
	((TclIndex) indexRep).testUpdateIndex(index2);
	index = TclIndex.get(null, objv[1], tablePtr,
	        "token", 0);
	interp.setResult(index);
	return;
    }

    if (objv.length < 5) {
	throw new TclException(interp, "wrong # args");
    }

    setError = TclBoolean.get(interp, objv[1]);
    allowAbbrev = TclBoolean.get(interp, objv[2]);

    argv = new String[objv.length-3];
    for (i = 4; i < objv.length; i++) {
	argv[i-4] = objv[i].toString();
    }
    argv[objv.length-4] = null;

    // No need to worry about a cached table pointer matching the
    // newly allocated array pointer.

    index = TclIndex.get((setError? interp : null), objv[3],
            argv, "token", (allowAbbrev? 0 : TCL.EXACT));
    interp.setResult(index);
}

}  // end class TestIndexObjCmd



/*
 *----------------------------------------------------------------------
 *
 * TestintobjCmd -> TestIntObjCmd
 *
 *	This procedure implements the "testintobj" command. It is used to
 *	test the int Tcl object type implementation.
 *
 * Results:
 *	A standard Tcl object result.
 *
 * Side effects:
 *	Creates and frees int objects, and also converts objects to
 *	have int type.
 *
 *----------------------------------------------------------------------
 */

class TestIntObjCmd implements Command {

public void
cmdProc(
    Interp interp,		// The current Tcl interpreter.
    TclObject[] objv)		// The arguments passed to the command.
throws
    TclException		// The standard Tcl exception.
{
    int intValue, varIndex, i;
    int longValue;
    String index, subCmd, string;

    if (objv.length < 3) {
	throw new TclNumArgsException(interp, 1, objv, 
	    "option arg ?arg ...?");
    }

    index = objv[2].toString();
    varIndex = TestObjCmdUtil.GetVariableIndex(interp, index);

    subCmd = objv[1].toString();
    if (subCmd.equals("set")) {
	if (objv.length != 4) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	string = objv[3].toString();
	i = Util.getInt(interp, string);
	intValue = i;

        // The C implementation changes the internal rep of an unshared
        // object in the varPtr array. Jacl does not support functions
        // like Tcl_SetIntObj() so always use SetVarToObj().

        TestObjCmdUtil.SetVarToObj(varIndex, TclInteger.newInstance(intValue));
	interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else if (subCmd.equals("set2")) { // doesn't set result
	if (objv.length != 4) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	string = objv[3].toString();
	i = Util.getInt(interp, string);
	intValue = i;

        // The C implementation changes the internal rep of an unshared
        // object in the varPtr array. Jacl does not support functions
        // like Tcl_SetIntObj() so always use SetVarToObj().

        TestObjCmdUtil.SetVarToObj(varIndex, TclInteger.newInstance(intValue));
    } else if (subCmd.equals("setlong")) {
	if (objv.length != 4) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	string = objv[3].toString();
	i = Util.getInt(interp, string);
	intValue = i;

        // The C implementation changes the internal rep of an unshared
        // object in the varPtr array. Jacl does not support functions
        // like Tcl_SetLongObj() so always use SetVarToObj().

        TestObjCmdUtil.SetVarToObj(varIndex, TclInteger.newInstance(intValue));
	interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else if (subCmd.equals("setmaxlong")) {
	int maxLong = Integer.MAX_VALUE;
	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}

        // The C implementation changes the internal rep of an unshared
        // object in the varPtr array. Jacl does not support functions
        // like Tcl_SetLongObj() so always use SetVarToObj().

        TestObjCmdUtil.SetVarToObj(varIndex, TclInteger.newInstance(maxLong));
    } else if (subCmd.equals("ismaxlong")) {
	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	longValue = TclInteger.get(interp, TestObjCmd.varPtr[varIndex]);
	interp.setResult(((longValue == Integer.MAX_VALUE)? "1" : "0"));
    } else if (subCmd.equals("get")) {
	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else if (subCmd.equals("get2")) {
	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	string = TestObjCmd.varPtr[varIndex].toString();
	interp.setResult(string);
    } else if (subCmd.equals("inttoobigtest")) {
	// If long ints have more bits than ints on this platform, verify
	// that Tcl_GetIntFromObj returns an error if the long int held
	// in an integer object's internal representation is too large
	// to fit in an int.

	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}

	// 64 bit integer type not supported in Java
	interp.setResult(1);
    } else if (subCmd.equals("mult10")) {
	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	intValue = TclInteger.get(interp, TestObjCmd.varPtr[varIndex]);

        // The C implementation changes the internal rep of an unshared
        // object in the varPtr array. Jacl does not support functions
        // like Tcl_SetIntObj() so always use SetVarToObj().

        TestObjCmdUtil.SetVarToObj(varIndex, TclInteger.newInstance(intValue * 10));
	interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else if (subCmd.equals("div10")) {
	if (objv.length != 3) {
	    throw new TclNumArgsException(interp, 1, objv, 
	        "option arg ?arg ...?");
	}
	TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	intValue = TclInteger.get(interp, TestObjCmd.varPtr[varIndex]);

        // The C implementation changes the internal rep of an unshared
        // object in the varPtr array. Jacl does not support functions
        // like Tcl_SetIntObj() so always use SetVarToObj().

        TestObjCmdUtil.SetVarToObj(varIndex, TclInteger.newInstance(intValue / 10));
	interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else {
	throw new TclException(interp,
	    "bad option \"" + objv[1] +
	    "\": must be set, get, get2, mult10, or div10");
    }
}

}  // end class TestIntObjCmd



/*
 *----------------------------------------------------------------------
 *
 * TestobjCmd --
 *
 *	This procedure implements the "testobj" command. It is used to test
 *	the type-independent portions of the Tcl object type implementation.
 *
 * Results:
 *	A standard Tcl object result.
 *
 * Side effects:
 *	Creates and frees objects.
 *
 *----------------------------------------------------------------------
 */


class TestObjCmdImpl {

public static void
cmdProc(
    Interp interp,		// The current Tcl interpreter.
    TclObject[] objv)		// The arguments passed to the command.
throws
    TclException		// The standard Tcl exception.
{
    int varIndex, destIndex, i;
    String index, subCmd, string;

    if (objv.length < 2) {
	throw new TclNumArgsException(interp, 1, objv, 
		"option arg ?arg ...?");
    }

    subCmd = objv[1].toString();
    if (subCmd.equals("assign")) {
        if (objv.length != 4) {
            throw new TclNumArgsException(interp, 1, objv, 
                "option arg ?arg ...?");
        }
        index = objv[2].toString();
        varIndex = TestObjCmdUtil.GetVariableIndex(interp, index);
        TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
        string = objv[3].toString();
        destIndex = TestObjCmdUtil.GetVariableIndex(interp, string);
        TestObjCmdUtil.SetVarToObj(destIndex, TestObjCmd.varPtr[varIndex]);
        interp.setResult(TestObjCmd.varPtr[destIndex]);
     } else if (subCmd.equals("convert")) {
        String typeName;
        if (objv.length != 4) {
            throw new TclNumArgsException(interp, 1, objv, 
                "option arg ?arg ...?");
        }
        index = objv[2].toString();
        varIndex = TestObjCmdUtil.GetVariableIndex(interp, index);
        TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
        typeName = objv[3].toString();

        if (! TestObjCmdUtil.IsSupportedType(typeName)) {
            throw new TclException(interp,
                "no type " + typeName + " found");
        }
        TestObjCmdUtil.ConvertToType(interp,
            TestObjCmd.varPtr[varIndex], typeName);
        interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else if (subCmd.equals("duplicate")) {
        if (objv.length != 4) {
            throw new TclNumArgsException(interp, 1, objv, 
                "option arg ?arg ...?");
        }
        index = objv[2].toString();
        varIndex = TestObjCmdUtil.GetVariableIndex(interp, index);
        TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	string = objv[3].toString();
	destIndex = TestObjCmdUtil.GetVariableIndex(interp, string);
	TestObjCmdUtil.SetVarToObj(destIndex,
            TestObjCmd.varPtr[varIndex].duplicate());
	interp.setResult(TestObjCmd.varPtr[destIndex]);
    } else if (subCmd.equals("freeallvars")) {
        if (objv.length != 2) {
            throw new TclNumArgsException(interp, 1, objv, 
                "option arg ?arg ...?");
        }
        for (i = 0;  i < TestObjCmd.NUMBER_OF_OBJECT_VARS;  i++) {
            if (TestObjCmd.varPtr[i] != null) {
                TestObjCmd.varPtr[i].release();
                TestObjCmd.varPtr[i] = null;
            }
        }
    } else if (subCmd.equals("invalidateStringRep")) {
        if (objv.length != 3) {
            throw new TclNumArgsException(interp, 1, objv, 
                "option arg ?arg ...?");
        }
	index = objv[2].toString();
	varIndex = TestObjCmdUtil.GetVariableIndex(interp, index);
	TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	TestObjCmd.varPtr[varIndex].invalidateStringRep();
	interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else if (subCmd.equals("newobj")) {
        if (objv.length != 3) {
            throw new TclNumArgsException(interp, 1, objv, 
                "option arg ?arg ...?");
        }
        index = objv[2].toString();
        varIndex = TestObjCmdUtil.GetVariableIndex(interp, index);
        TestObjCmdUtil.SetVarToObj(varIndex, TclString.newInstance(""));
	interp.setResult(TestObjCmd.varPtr[varIndex]);
    } else if (subCmd.equals("objtype")) {
	String typeName;

	// return an object containing the name of the argument's type
	// of internal rep.  If none exists, return "none".

        if (objv.length != 3) {
            throw new TclNumArgsException(interp, 1, objv, 
                "option arg ?arg ...?");
        }
        typeName = TestObjCmdUtil.GetObjType(objv[2]);
        if (typeName == null) {
            typeName = "none";
        }
        interp.setResult(typeName);
    } else if (subCmd.equals("refcount")) {
        if (objv.length != 3) {
            throw new TclNumArgsException(interp, 1, objv, 
                "option arg ?arg ...?");
        }
        index = objv[2].toString();
        varIndex = TestObjCmdUtil.GetVariableIndex(interp, index);
        TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
        interp.setResult(TestObjCmd.varPtr[varIndex].getRefCount());
    } else if (subCmd.equals("type")) {
	String typeName;

        if (objv.length != 3) {
            throw new TclNumArgsException(interp, 1, objv, 
                "option arg ?arg ...?");
        }
        index = objv[2].toString();
        varIndex = TestObjCmdUtil.GetVariableIndex(interp, index);
        TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
        typeName = TestObjCmdUtil.GetObjType(TestObjCmd.varPtr[varIndex]);
        if (typeName == null) {
            typeName = "string";
        }
        interp.setResult(typeName);
    } else if (subCmd.equals("types")) {
        if (objv.length != 2) {
            throw new TclNumArgsException(interp, 1, objv, 
                "option arg ?arg ...?");
        }
        interp.setResult(TestObjCmdUtil.GetObjTypes());
    } else {
	throw new TclException(interp,
	    "bad option \"" +
            objv[1] +
            "\": must be assign, convert, duplicate, freeallvars, " +
            "newobj, objcount, objtype, refcount, type, or types");
    }
}

}  // end class TestObjCmdImpl



/*
 *----------------------------------------------------------------------
 *
 * TeststringobjCmd -> TestStringObjCmd
 *
 *	This procedure implements the "teststringobj" command. It is used to
 *	test the string Tcl object type implementation.
 *
 * Results:
 *	A standard Tcl object result.
 *
 * Side effects:
 *	Creates and frees string objects, and also converts objects to
 *	have string type.
 *
 *----------------------------------------------------------------------
 */

class TestStringObjCmd implements Command {

public void
cmdProc(
    Interp interp,		// The current Tcl interpreter.
    TclObject[] objv)		// The arguments passed to the command.
throws
    TclException		// The standard Tcl exception.
{
    int varIndex, option, i, length;
    final int MAX_STRINGS = 11;
    String[] strings = new String[MAX_STRINGS+1];
    String index, string;
    String[] options = {
	"append", "appendstrings", "get", "get2", "length", "length2",
	"set", "set2", "setlength", "ualloc", "getunicode", 
	null
    };

    if (objv.length < 3) {
	throw new TclNumArgsException(interp, 1, objv, 
	    "option arg ?arg ...?");
    }

    index = objv[2].toString();
    varIndex = TestObjCmdUtil.GetVariableIndex(interp, index);
    option = TclIndex.get(interp, objv[1], options,"option", 0);

    switch (option) {
	case 0: {			// append
	    if (objv.length != 5) {
	        throw new TclNumArgsException(interp, 1, objv, 
	            "option arg ?arg ...?");
	    }
	    length = TclInteger.get(interp, objv[4]);
	    if (TestObjCmd.varPtr[varIndex] == null) {
		TestObjCmdUtil.SetVarToObj(varIndex, TclString.newInstance(""));
	    }

	    // If the object bound to variable "varIndex" is shared, we must
	    // "copy on write" and append to a copy of the object. 

	    if (TestObjCmd.varPtr[varIndex].isShared()) {
		TestObjCmdUtil.SetVarToObj(varIndex, TestObjCmd.varPtr[varIndex].duplicate());
	    }
	    string = objv[3].toString();
	    if (length != -1) {
	        string = string.substring(0, length);
	    }
	    TclString.append(TestObjCmd.varPtr[varIndex], string);
	    interp.setResult(TestObjCmd.varPtr[varIndex]);
	    break;
	}
	case 1:	{			// appendstrings
	    if (objv.length > (MAX_STRINGS+3)) {
	        throw new TclNumArgsException(interp, 1, objv, 
	            "option arg ?arg ...?");
	    }
	    if (TestObjCmd.varPtr[varIndex] == null) {
		TestObjCmdUtil.SetVarToObj(varIndex, TclString.newInstance(""));
	    }

	    // If the object bound to variable "varIndex" is shared, we must
	    // "copy on write" and append to a copy of the object. 

	    if (TestObjCmd.varPtr[varIndex].isShared()) {
		TestObjCmdUtil.SetVarToObj(varIndex, TestObjCmd.varPtr[varIndex].duplicate());
	    }
	    for (i = 3;  i < objv.length;  i++) {
		strings[i-3] = objv[i].toString();
	    }
	    for ( ; i < (MAX_STRINGS+1) + 3; i++) {
		strings[i - 3] = null;
	    }
            // FIXME: Use of TclString.append() not same as Tcl_AppendStringsToObj()
            // WRT buffer capacity management.
	    for (i = 0 ; i < (MAX_STRINGS+1) && strings[i] != null; i++) {
		TclString.append(TestObjCmd.varPtr[varIndex], strings[i]);
	    }
	    interp.setResult(TestObjCmd.varPtr[varIndex]);
	    break;
	}
	case 2:	{			// get
	    if (objv.length != 3) {
	        throw new TclNumArgsException(interp, 1, objv, 
	            "option arg ?arg ...?");
	    }
	    TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	    interp.setResult(TestObjCmd.varPtr[varIndex]);
	    break;
	}
	case 3:	{			// get2
	    if (objv.length != 3) {
	        throw new TclNumArgsException(interp, 1, objv, 
	            "option arg ?arg ...?");
	    }
	    TestObjCmdUtil.CheckIfVarUnset(interp, varIndex);
	    string = TestObjCmd.varPtr[varIndex].toString();
	    interp.setResult(string);
	    break;
	}
	case 4:	{			// length
	    if (objv.length != 3) {
	        throw new TclNumArgsException(interp, 1, objv, 
	            "option arg ?arg ...?");
	    }
	    interp.setResult((TestObjCmd.varPtr[varIndex] != null)
	            ? TestObjCmd.varPtr[varIndex].toString().length() : -1);
	    break;
	}
	case 5:	{			// length2
	    if (objv.length != 3) {
	        throw new TclNumArgsException(interp, 1, objv, 
	            "option arg ?arg ...?");
	    }
	    if (TestObjCmd.varPtr[varIndex] != null) {
		TclString tstr = (TclString) TestObjCmd.varPtr[varIndex].getInternalRep();
		// C Tcl's String.allocated is the number of bytes allocated for
		// a UTF-8 string - 1 byte for the termination char.
		length = (tstr.sbuf == null ? 0 : tstr.sbuf.capacity());
		if (length != 0 && tstr.sbuf.length() == 0) {
		    // Empty string rep, report zero capacity
		    length = 0;
		}
	    } else {
		length = -1;
	    }
	    interp.setResult(length);
	    break;
	}
	case 6:	{			// set
	    if (objv.length != 4) {
	        throw new TclNumArgsException(interp, 1, objv, 
	            "option arg ?arg ...?");
	    }

	    // The C implementation changes the internal rep of an unshared
	    // object in the varPtr array. Jacl does not support functions
	    // like Tcl_SetStringObj() so always use SetVarToObj().

	    string = objv[3].toString();
	    // Manage StringBuffer capacity so that tests pass
	    StringBuffer sbuf = new StringBuffer(string.length());
	    sbuf.append(string);
	    TestObjCmdUtil.SetVarToObj(varIndex, TclString.newInstance(sbuf));
	    interp.setResult(TestObjCmd.varPtr[varIndex]);

	    break;
	}
	case 7:	{			// set2
	    if (objv.length != 4) {
	        throw new TclNumArgsException(interp, 1, objv, 
	            "option arg ?arg ...?");
	    }
	    TestObjCmdUtil.SetVarToObj(varIndex, objv[3]);
	    break;
	}
	case 8:	{			// setlength
	    if (objv.length != 4) {
	        throw new TclNumArgsException(interp, 1, objv, 
	            "option arg ?arg ...?");
	    }
	    length = TclInteger.get(interp, objv[3]);
	    if (TestObjCmd.varPtr[varIndex] != null) {
		// Jacl does not support Tcl_SetObjLength() so inline the logic here.
		TclObject tobj = TestObjCmd.varPtr[varIndex];
		TclString.append(tobj, ""); // Convert to TclString internal rep
		TclString tstr = (TclString) tobj.getInternalRep();
		// Allocate a new StringBuffer so that we can control the capacity.
		int prev_length = tstr.sbuf.length();
		String prev_str = tstr.sbuf.toString();
		if (length == 0) {
		    tstr.sbuf = null;
		} else if (length < prev_length) {
		    // Retain original capacity and shorten the length
		    tstr.sbuf.setLength(length);
		} else if (length > prev_length) {
		    // Expand capacity but keep the original string
		    tstr.sbuf = new StringBuffer(length);
		    tstr.sbuf.append(prev_str);
		    tstr.sbuf.setLength(length);
		}
		tobj.invalidateStringRep();
	    }
	    break;
	}
	case 9:	{			// ualloc
	    if (objv.length != 3) {
	        throw new TclNumArgsException(interp, 1, objv, 
	            "option arg ?arg ...?");
	    }
	    if (TestObjCmd.varPtr[varIndex] != null) {
		TclString tstr = (TclString) TestObjCmd.varPtr[varIndex].getInternalRep();
		// C Tcl's String.uallocated is the number of bytes allocated - 2
		// bytes for termination char. Jacl has no termination char.
		length = (tstr.sbuf == null ? 0 : tstr.sbuf.capacity() * 2);
	    } else {
		length = -1;
	    }
	    interp.setResult(length);
	    break;
	}
	case 10: {			// getunicode
	    if (objv.length != 3) {
	        throw new TclNumArgsException(interp, 1, objv, 
	            "option arg ?arg ...?");
	    }
	    TestObjCmd.varPtr[varIndex].toString();
	    break;
	}
    }
}

}  // end class TestStringObjCmd



class TestObjCmdUtil {

/*
 *----------------------------------------------------------------------
 *
 * SetVarToObj -> TestObjCmdUtil.SetVarToObj
 *
 *	Utility routine to assign a TclObject to a test variable. The
 *	TclObject can be null.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	This routine handles ref counting details for assignment:
 *	i.e. the old value's ref count must be decremented (if not null) and
 *	the new one incremented (also if not null).
 *
 *----------------------------------------------------------------------
 */


static void
SetVarToObj(int varIndex, TclObject objPtr)
{
    if (TestObjCmd.varPtr[varIndex] != null) {
        TestObjCmd.varPtr[varIndex].release();
    }
    TestObjCmd.varPtr[varIndex] = objPtr;
    if (objPtr != null) {
	objPtr.preserve();
    }
}



/*
 *----------------------------------------------------------------------
 *
 * GetVariableIndex -> TestObjCmdUtil.GetVariableIndex
 *
 *	Utility routine to get a test variable index from the command line.
 *
 * Results:
 *	Returns the variable index.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

static int
GetVariableIndex(
    Interp interp,
    String string) // String containing a variable index
                   // specified as a nonnegative number less
                   // than NUMBER_OF_OBJECT_VARS.
        throws TclException
{
    int index;

    index = Util.getInt(interp, string);
    if (index < 0 || index >= TestObjCmd.NUMBER_OF_OBJECT_VARS) {
        throw new TclException(interp, "bad variable index");
    }

    return index;
}



/*
 *----------------------------------------------------------------------
 *
 * CheckIfVarUnset -> TestObjCmdUtil.CheckIfVarUnset
 *
 *	Utility procedure that checks whether a test variable is readable:
 *	i.e., that varPtr[varIndex] is non-null.
 *
 * Results:
 *	Raises a TclException if the var is unset.
 *
 * Side effects:
 *
 *----------------------------------------------------------------------
 */

static void
CheckIfVarUnset(
    Interp interp,
    int varIndex)               // Index of the test variable to check.
        throws TclException
{
    if (TestObjCmd.varPtr[varIndex] == null) {
        String msg = "variable " + varIndex + " is unset (NULL)";
        throw new TclException(interp, msg);
    }
}

// Return true if this is a supported type. This methods exists since
// Jacl has no way to lookup supported types at runtime.

static boolean
IsSupportedType(
    String typeName)
{
    // Note, many types like "end-offset" are not actually supported in Jacl

    if (typeName.equals("int")) {
        return true;
    } else if (typeName.equals("double")) {
        return true;
    } else if (typeName.equals("boolean")) {
        return true;
    } else if (typeName.equals("end-offset")) {
        return true;
    } else {
        return false;
    }
}

// Convert a TclObject to a named type. This method exists because
// Jacl has no way to lookup a type or convert at runtime.

static void
ConvertToType(
    Interp interp,
    TclObject tobj,
    String typeName)
        throws TclException
{
    if (typeName.equals("int")) {
        TclInteger.get(interp, tobj);
    } else if (typeName.equals("double")) {
        TclDouble.get(interp, tobj);
    } else if (typeName.equals("boolean")) {
        TclBoolean.get(interp, tobj);
    }
}

// Return the type name string of a TclObject.

static String
GetObjType(
    TclObject tobj)
{
    InternalRep irep = tobj.getInternalRep();

    if (irep instanceof TclInteger) {
        return "int";
    } else if (irep instanceof TclDouble) {
        return "double";
    } else if (irep instanceof TclBoolean) {
        return "boolean";
    } else if (irep instanceof TclList) {
        return "list";
    } else if (irep instanceof TclString) {
        return "string";
    } else {
        return null;
    }
}

// Return a list of available types

static String
GetObjTypes()
{
    String types = "{array search} boolean bytearray bytecode double end-offset index " +
        "int list nsName procbody string";
    return types;
}

} // end class TestObjCmdUtil



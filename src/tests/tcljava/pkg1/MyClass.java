/*
 * MyClass --
 *
 *	This file tests the tcl.lang.reflect.PkgInvoker class. Please
 *	see the comments in PkgInvoker.java for details.
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 * RCS: @(#) $Id: MyClass.java,v 1.1 1999/05/10 04:08:57 dejong Exp $
 *
 */

package pkg1;

public class MyClass {

int foo;
protected int pfoo = 5432;

static String sstring = "can get";

protected static String psstring = "protected can get";

public MyClass()
{
    foo = 4321;
}

MyClass(int f)
{
    foo = f;
}

protected MyClass(short f)
{
    foo = f;
}

int someMethod()
{
    return 1234;
}

protected int someProtectedMethod()
{
    return 4321;
}

public int getFoo()
{
    return foo;
}

public void setFoo(int f)
{
    foo = f;
}

public void setWriteOnly(int f)
{

}

public int getReadOnly()
{
    return 0;
}

} // end MyClass

class ProtectedClass {

int foo;
protected int pfoo = 5432;

static String sstring = "can get";

protected static String psstring = "protected can get";

public ProtectedClass()
{
    foo = 4321;
}

ProtectedClass(int f)
{
    foo = f;
}

protected ProtectedClass(short f)
{
    foo = f;
}

int someMethod()
{
    return 1234;
}

protected int someProtectedMethod()
{
    return 4321;
}

public int getFoo()
{
    return foo;
}

public void setFoo(int f)
{
    foo = f;
}


}


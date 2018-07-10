/*
 * Tester1Event.java --
 *
 *	This is an event object that tests the java::bind and java::event
 *	commands.
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 * RCS: @(#) $Id: Tester1Event.java,v 1.1 1999/05/10 04:09:00 dejong Exp $
 */

package tcl.lang;

import java.util.*;

public class Tester1Event extends EventObject {

public Tester1Event(Object source)
{
    super(source);
}

public int getIntValue() {
    return 1234;
}
public String getStringValue() {
    return "string value";
}
public String getStringValue_null() {
    return null;
}
public String getStringValue_empty() {
    return "";
}

public Object getObjectValue_null() {
    return null;
}

} // end Tester1Event


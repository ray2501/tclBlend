/*
 * Tester2Event.java --
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
 * RCS: @(#) $Id: Tester2Event.java,v 1.1 1999/05/10 04:09:00 dejong Exp $
 */

package tcl.lang;

import java.util.*;

public class Tester2Event extends EventObject {

public Tester2Event(Object source)
{
    super(source);
}

} // end Tester2Event


/* 
 * IdleHandlerTest.java --
 *
 *	This file is used by tclblend/javaIdle.test to test the IdleHandler
 *	interfaces.
 *
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: IdleHandlerTest.java,v 1.1 1999/05/10 04:08:53 dejong Exp $
 */

package tests;
import tcl.lang.*;

public class IdleHandlerTest extends IdleHandler {

public int value = 0;
public boolean err = false;


/*
 *----------------------------------------------------------------------
 *
 * IdleHandlerTest --
 *
 *	Create a idle handler to be fired the next time the notifier
 *	is idle.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	The idle is registered in the list of idle handlers in the
 *	given notifier.
 *
 *----------------------------------------------------------------------
 */

public
IdleHandlerTest(
    Notifier n)			// The notifier to fire the event.
{
    super(n);
}

/*
 *----------------------------------------------------------------------
 *
 * processIdleEvent --
 *
 *	This method is called when the idle handleris expired.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	It increments the counter for this object.
 * 
 *----------------------------------------------------------------------
 */

public void
processIdleEvent() {
    value++;
    if (err) {
	throw new NullPointerException("IdleHandlerTest");
    }
}

} // end IdleHandlerTest


/* 
 * TimerHandlerTest.java --
 *
 *	This file is used by javaTimer.test to test the TimerHandler
 *	interfaces.
 *
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: TimerHandlerTest.java,v 1.1 1999/05/10 04:08:53 dejong Exp $
 */

package tests;
import tcl.lang.*;

public class TimerHandlerTest extends TimerHandler {

public int value = 0;
public boolean err = false;


/*
 *----------------------------------------------------------------------
 *
 * TimerHandlerTest --
 *
 *	Create a timer handler to be fired after the given time lapse.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	The timer is registered in the list of timers in the given
 *	notifier. After milliseconds have elapsed, the
 *	processTimerEvent() method will be invoked exactly once inside
 *	the primary thread of the notifier.
 *
 *----------------------------------------------------------------------
 */

public
TimerHandlerTest(
    Notifier n,			// The notifier to fire the event.
    int milliseconds)
{
    super(n, milliseconds);
}

/*
 *----------------------------------------------------------------------
 *
 * processTimerEvent --
 *
 *	This method is called when the timer is expired.
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
processTimerEvent() {
    value++;
    if (err) {
	throw new NullPointerException("TimerHandlerTest");
    }
}

} // end TestTimer


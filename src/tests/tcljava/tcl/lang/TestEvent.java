/* 
 * TestEvent.java --
 *
 *	This file contains classes needed to test the TclEvent  and
 *	Notifier classes.
 *
 * Copyright (c) 1998 by Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 *
 * RCS: @(#) $Id: TestEvent.java,v 1.1 1999/05/10 04:08:59 dejong Exp $
 */

package tcl.lang;
import java.util.*;

class TestEvent extends TclEvent {
/*
 * Set variables inside this interp.
 */

Interp interp;

String script;

public TestEvent(Interp i, String s)
{
    interp = i;
    script = s;
}


/*
 * Use the testEvtReply global varable to control what this method should
 * return. It should be 1 most of the time -- if you want the event
 * to be removed from the event queue after it's processed.
 */

public int
processEvent(
    int flags)			// Same as flags passed to Notifier.doOneEvent.
{
    try {
	interp.eval(script);

	int k = TclInteger.get(interp, interp.getVar("testEvtReply",
		TCL.GLOBAL_ONLY));
	return k;
    } catch (TclException e) {
	System.out.println("TclException caught");
	e.printStackTrace();

	return 1;
    }
}

} // end TestEvent

/*
 * Used by TclEvent.test.
 */

class TestEventThread1 extends Thread {

Interp interp;
String script;

public TestEventThread1(Interp i, String s)
{
    interp = i;
    script = s;
}

public void run()
{
    TestEvent evt = new TestEvent(interp, script);
    synchronized (this) {
	try {
	    wait(1000);
	} catch (InterruptedException e) {
	}
    }
    interp.getNotifier().queueEvent(evt, TCL.QUEUE_TAIL);
    evt.sync();
}


} // end TestEventThread1

/*
 * This class tests how well the Notifier can handle events sent from
 * different threads.
 *
 */

class TestEventThread2 extends Thread {

Interp interp;

Vector scripts = new Vector();
Vector waitTime = new Vector();

public TestEventThread2(Interp i) {
    interp = i;

}

/*
 * This method should be called in the primary thread before the thread runs.
 */

public void addEvent(String script, int wtime)
{
    scripts.addElement(script);
    waitTime.addElement(new Integer(wtime));
}

/*
 * When the thread runs, it sends all the scripts to be evaluated by
 * the interpreter in the primary Thread. If the <wtime> corresponding
 * to the script is -1, it will sync() on the event. Otherwise, it
 * will sleep for <wtime> milliseconds before queueing the next event.
 */

public void run()
{
    for (int i = 0; i < scripts.size(); i++) {
	int wtime = ((Integer)waitTime.elementAt(i)).intValue();
	TestEvent evt = new TestEvent(interp, (String)scripts.elementAt(i));
	interp.getNotifier().queueEvent(evt, TCL.QUEUE_TAIL);
	if (wtime == -1) {
	    evt.sync();
	} else {
	    try {
		sleep((long)wtime);
	    } catch (InterruptedException e) {
		/*
		 * do nothing.
		 */
	    }
	}
    }
}

} // end TestEventThread2

class TestEventDeleter implements EventDeleter {

Interp interp;

public TestEventDeleter(Interp i) {
    interp = i;
}

String myScript;

public void delete(String s) {
    myScript = s;

    interp.getNotifier().deleteEvents(this);
}

public int deleteEvent(TclEvent evt)
{
    if (evt instanceof TestEvent) {
	if (((TestEvent)evt).script.equals(myScript)) {
	    return 1;
	}
    }

    return 0;
}

} // end TestEventDeleter


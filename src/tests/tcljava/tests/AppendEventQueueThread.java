package tests;

import tcl.lang.*;

/*
This class is designed to test a deadlock condition in the
tcl.lang.Notifier class. There was a problem with deadlocking
if a vwait was entered through the Notifier loop, it would
keep other events from being queued up. This class is used
in tcljava/TclEvent.test in test TclEvent-1.4.
*/

public class AppendEventQueueThread extends Thread {
    private Interp interpInOtherThread;
    public final int expectedNumQueued = 5;
    public int numQueued = 0;
    public int numProcessed = 0;

    private static final boolean debug = false;

    public AppendEventQueueThread(Interp i) {
	interpInOtherThread = i;
    }
    
    public void run() {
	if (debug) {
	    System.out.println("running AppendEventQueueThread");
	}

        for (int i = 0; i < expectedNumQueued ; i++) {
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {}


	    TclEvent event = new TclEvent() {
		public int processEvent (int flags) {
		    numProcessed++;
                    if (debug) {
	                System.out.println("processed event " + numProcessed);
	            }
		    return 1;
		}
	    };

	    // Add the event to the thread safe event queue
	    interpInOtherThread.getNotifier().queueEvent(event, TCL.QUEUE_TAIL);
	    numQueued++;
            if (debug) {
	        System.out.println("queued event " + numQueued);
	    }
	}

	if (debug) {
	    System.out.println("done running AppendEventQueueThread");
	}
    }

    public static void queueVwaitEvent(final Interp interp) {
	    TclEvent event = new TclEvent() {
		public int processEvent (int flags) {
		    try {
			interp.eval("after 10000 {set wait 1}", 0);
			interp.eval("vwait wait", 0);
		    } catch (TclException ex) {
			ex.printStackTrace();
		    }
		    return 1;
		}
	    };

	    // Add the event to the thread safe event queue
	    interp.getNotifier().queueEvent(event, TCL.QUEUE_TAIL);
            if (debug) {
	        System.out.println("queued vwait event");
	    }
    }
}

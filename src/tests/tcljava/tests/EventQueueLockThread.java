package tests;

import tcl.lang.*;

// This class is designed to test the mutual exclusion approach
// of the tcl.lang.Notifier class. An event should be added to
// the queue right away, even if another time consuming event
// is being processed. This class is used in
// tcljava/TclEvent.test in test TclEvent-1.5.

public class EventQueueLockThread extends Thread {
    private Interp interpInOtherThread;

    public boolean slow_event_started = false;
    public boolean slow_event_finished = false;
    public boolean thread_finished = false;
    public boolean test_passed = false;

    private static final boolean debug = false;

    public EventQueueLockThread(Interp i) {
	interpInOtherThread = i;
    }
    
    public void run() {
	if (debug) {
	    System.out.println("running EventQueueLockThread");
	}

        // In this new thread, queue an event that will take a
        // long time to process, it will actually be processed
        // in the original thread.

	TclEvent event = new TclEvent() {
	    public int processEvent (int flags) {
                if (debug) {
	            System.out.println("started processing slow event");
	        }
	        slow_event_started = true;
	        try {
		    Thread.sleep(10000);
	        } catch (InterruptedException e) {}
	        slow_event_finished = true;
                if (debug) {
	            System.out.println("finished processing slow event");
	        }
		return 1;
	    }
	};

	// Add the event to the thread safe event queue
        if (debug) {
	    System.out.println("about to add slow event to queue");
	}
	interpInOtherThread.getNotifier().queueEvent(event, TCL.QUEUE_TAIL);

        // Now wait around a bit to give the notifier a
        // chance to start processing the slow event.

	try {
	    Thread.sleep(2000);
	} catch (InterruptedException e) {}

        if (slow_event_started == false) {
            throw new RuntimeException("slow event was not started");
        }
        if (slow_event_finished == true) {
            throw new RuntimeException("slow event was already finished");
        }

	TclEvent other_event = new TclEvent() {
	    public int processEvent (int flags) {
                if (debug) {
	            System.err.println("processed other event");
	        }
		return 1;
	    }
	};

	// This event should get added to the queue right away,
        // it should not get queued after the slow event has
        // finished running.

        if (debug) {
	    System.err.println("about to add other event to queue");
	}
	interpInOtherThread.getNotifier().queueEvent(other_event, TCL.QUEUE_TAIL);
        if (debug) {
	    System.err.println("added other event to queue");
	}

        if (slow_event_finished == false) {
            if (debug) {
	        System.err.println("queueEvent returned before slow event finished");
	    }
            test_passed = true;
        } else {
            if (debug) {
	        System.err.println("slow event finished before queueEvent returned");
	    }
        }

        thread_finished = true;

	if (debug) {
	    System.err.println("done running EventQueueLockThread");
	}
    }
}

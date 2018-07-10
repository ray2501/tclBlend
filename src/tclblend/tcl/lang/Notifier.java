/*
 * Notifier.java --
 *
 *	Implements the Blend version of the Notifier class.
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: Notifier.java,v 1.8 2006/01/26 19:49:19 mdejong Exp $
 *
 */

package tcl.lang;

import java.util.HashMap;

// Implements the Tcl Blend version of the Notifier class. The Notifier is
// the lowest-level part of the event system. It is used by
// higher-level event sources such as file, JavaBean and timer
// events. The Notifier manages an event queue that holds TclEvent
// objects.
//
// The Tcl Blend Notifier is designed to run in a multi-threaded
// environment. Each notifier instance is associated with a primary
// thread. Any thread can queue (or dequeue) events using the
// queueEvent (or deleteEvents) call. However, only the primary thread
// may process events in the queue using the doOneEvent()
// call. Attepmts to call doOneEvent() from a non-primary thread will
// cause a TclRuntimeError.
//
// This class does not have a public constructor and thus cannot be
// instantiated. The only way to for a Tcl extension to get an
// Notifier is to call Interp.getNotifier() (or
// Notifier.getNotifierForThread() ), which returns the Notifier for that
// interpreter (thread).

public class Notifier implements EventDeleter {

private static Notifier globalNotifier;

// First pending event, or null if none.

private TclEvent firstEvent;

// Last pending event, or null if none.

private TclEvent lastEvent;

// Last high-priority event in queue, or null if none.

private TclEvent markerEvent;

// Event that was just processed by serviceEvent

private TclEvent servicedEvent = null;

// The primary thread of this notifier. Only this thread should process
// events from the event queue.

Thread primaryThread;

// Reference count of the notifier. It's used to tell when a notifier
// is no longer needed.

int refCount;

// Stores the Notifier for each thread.

private static HashMap<Thread, Notifier> notifierTable = new HashMap<Thread,Notifier>();

// tcl ThreadId

private long tclThreadId;


/*
 *----------------------------------------------------------------------
 *
 * Notifier --
 *
 *	Creates a Notifier instance.
 *
 * Side effects:
 *	Member fields are initialized.
 *
 *----------------------------------------------------------------------
 */

private
Notifier(
    Thread primaryTh)		// The primary thread for this Notifier.
{
    primaryThread     = primaryTh;
    firstEvent        = null;
    lastEvent         = null;
    markerEvent       = null;
    tclThreadId       = init();
    refCount          = 0;
}

/*
 *----------------------------------------------------------------------
 *
 * getNotifierForThread --
 *
 *	Get the notifier for this thread, creating the Notifier,
 *	when necessary.
 *
 * Results:
 *	The Notifier for this thread.
 *
 * Side effects:
 *	The Notifier is created when necessary.
 *
 *----------------------------------------------------------------------
 */

public static synchronized Notifier
getNotifierForThread(
    Thread thread)		// The thread that owns this Notifier.
{
    Notifier notifier = (Notifier) notifierTable.get(thread);
    if (notifier == null) {
	notifier = new Notifier(thread);
	notifierTable.put(thread, notifier);
    }

    return notifier;
}

/*
 *----------------------------------------------------------------------
 *
 * preserve --
 *
 *	Increment the reference count of the notifier. The notifier will
 *	be kept in the notifierTable (and alive) as long as its reference
 *	count is greater than zero.  This method is concurrent safe.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	The refCount is incremented.
 *
 *----------------------------------------------------------------------
 */

public synchronized void
preserve()
{
    if (refCount < 0) {
        throw new TclRuntimeError(
                "Attempting to preserve a freed Notifier");
    }
    ++refCount;
}

/*
 *----------------------------------------------------------------------
 *
 * release --
 *
 *	Decrement the reference count of the notifier. The notifier will
 *	be freed when its refCount goes from one to zero.  This method is
 *	concurrent safe.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	The notifier may be removed from the notifierTable when its
 *	refCount reaches zero.
 *
 *----------------------------------------------------------------------
 */

public synchronized void
release()
{
    if ((refCount == 0) && (primaryThread != null)) {
	throw new TclRuntimeError(
		"Attempting to release a Notifier before it's preserved");
    }
    if (refCount <= 0) {
	throw new TclRuntimeError("Attempting to release a freed Notifier");
    }
    --refCount;
    if (refCount == 0) {
	notifierTable.remove(primaryThread);
	primaryThread = null;
	dispose();
    }
}

/*
 *----------------------------------------------------------------------
 *
 * queueEvent --
 * 
 *	Insert an event into the event queue at one of three
 *	positions: the head, the tail, or before a floating marker.
 *	Events inserted before the marker will be processed in
 *	first-in-first-out order, but before any events inserted at
 *	the tail of the queue.  Events inserted at the head of the
 *	queue will be processed in last-in-first-out order.  This method is
 *	concurrent safe. 
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	If this method is invoked by a non-primary thread, the
 *	primaryThread of this Notifier will be notified about the new
 *	event.
 *
 *----------------------------------------------------------------------
 */

public synchronized void
queueEvent(
    TclEvent evt,		// The event to put in the queue.
    int position)		// One of TCL.QUEUE_TAIL,
				// TCL.QUEUE_HEAD or TCL.QUEUE_MARK.
{
    evt.notifier = this;

    if (position == TCL.QUEUE_TAIL) {
	// Append the event on the end of the queue.
	evt.next = null;

	if (firstEvent == null) {
	    firstEvent = evt;
	} else {
	    lastEvent.next = evt;
	}
	lastEvent = evt;
    } else if (position == TCL.QUEUE_HEAD) {
	// Push the event on the head of the queue.
	evt.next = firstEvent;
	if (firstEvent == null) {
	    lastEvent = evt;
	}
	firstEvent = evt;
    } else if (position == TCL.QUEUE_MARK) {
	// Insert the event after the current marker event and advance
	// the marker to the new event.
	if (markerEvent == null) {
	    evt.next = firstEvent;
	    firstEvent = evt;
	} else {
	    evt.next = markerEvent.next;
	    markerEvent.next = evt;
	}
	markerEvent = evt;
	if (evt.next == null) {
	    lastEvent = evt;
	}
    } else {
	// Wrong flag.
	throw new TclRuntimeError("wrong position \"" + position +
	       "\", must be TCL.QUEUE_HEAD, TCL.QUEUE_TAIL or TCL.QUEUE_MARK");
    }

    if (Thread.currentThread() != primaryThread) {
	alertNotifier(tclThreadId);
    }
}

/*
 *----------------------------------------------------------------------
 *
 * deleteEvents --
 *
 *	Calls an EventDeleter for each event in the queue and deletes
 *	those for which deleter.deleteEvent() returns 1. Events
 *	for which the deleter returns 0 are left in the queue.
 *	This method includes code to handle the special case of the
 *	Notifier wanting to delete a single event after is has
 *	been serviced. This method is concurrent safe.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	Potentially removes one or more events from the event queue.
 *
 *----------------------------------------------------------------------
 */

public synchronized void
deleteEvents(
    EventDeleter deleter)	// The deleter that checks whether an event
				// should be removed.
{
    TclEvent evt, prev;
    TclEvent servicedEvent = null;

    // Handle the special case of deletion of a single event that was just
    // processed by the serviceEvent() method.

    if (deleter == this) {
        servicedEvent = this.servicedEvent;
        if (servicedEvent == null)
            throw new TclRuntimeError(
                    "servicedEvent was not set by serviceEvent()");
        this.servicedEvent = null;
    }

    for (prev = null, evt = firstEvent; evt != null; evt = evt.next) {
        if (((servicedEvent == null) && (deleter.deleteEvent(evt) == 1))
                || (evt == servicedEvent)) {
            if (evt == firstEvent) {
                firstEvent = evt.next;
            } else {
                prev.next = evt.next;
            }
            if (evt.next == null) {
                lastEvent = prev;
            }
            if (evt == markerEvent) {
                markerEvent = prev;
            }
            if (evt == servicedEvent) {
                servicedEvent = null;
                break; // Just service this one event in the special case
            }
        } else {
            prev = evt;
        }
    }
    if (servicedEvent != null) {
        throw new TclRuntimeError(
                "servicedEvent was not removed from the queue");
    }
}

/*
 *----------------------------------------------------------------------
 *
 * deleteEvent --
 *
 *	This method is required to implement the EventDeleter interface
 *	It is not actually used though, see deleteEvents method for
 *	special casing of the deletion of a specific event.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

public int
deleteEvent(
    TclEvent evt) {
    throw new TclRuntimeError(
        "The Notifier.deleteEvent() method should not be called");
}

/*
 *----------------------------------------------------------------------
 *
 * serviceEvent --
 *
 *	Process one event from the event queue.  This method is concurrent 
 *	safe and can be reentered recursively. This method is invoked
 *	only from C code.
 *
 * Results:
 *	The return value is 1 if the procedure actually found an event
 *	to process. If no processing occurred, then 0 is returned.
 *
 * Side effects:
 *	Invokes all of the event handlers for the highest priority
 *	event in the event queue.  May collapse some events into a
 *	single event or discard stale events.
 *
 *----------------------------------------------------------------------
 */
int
serviceEvent(
    int flags)			// Indicates what events should be processed.
				// May be any combination of TCL.WINDOW_EVENTS
				// TCL.FILE_EVENTS, TCL.TIMER_EVENTS, or other
				// flags defined elsewhere.  Events not
				// matching this will be skipped for processing
				// later.
{
    TclEvent evt;

    // No event flags is equivalent to TCL_ALL_EVENTS.
    
    if ((flags & TCL.ALL_EVENTS) == 0) {
	flags |= TCL.ALL_EVENTS;
    }

    // Loop through all the events in the queue until we find one
    // that can actually be handled.

    evt = null;
    while ((evt = getAvailableEvent(evt)) != null) {
	// Call the handler for the event.  If it actually handles the
	// event then free the storage for the event.  There are two
	// tricky things here, both stemming from the fact that the event
	// code may be re-entered while servicing the event:
	//
	// 1. Set the "isProcessing" field to true. This is a signal to
	//    ourselves that we shouldn't reexecute the handler if the
	//    event loop is re-entered.
	// 2. When freeing the event, must search the queue again from the
	//    front to find it.  This is because the event queue could
	//    change almost arbitrarily while handling the event, so we
	//    can't depend on pointers found now still being valid when
	//    the handler returns.

	evt.isProcessing = true;

	if (evt.processEvent(flags) != 0) {
	    evt.isProcessed = true;
	    // Don't allocate/grab the monitor for the event unless sync()
	    // has been called in another thread. This is thread safe
	    // since sync() checks the isProcessed flag before calling wait.
	    if (evt.needsNotify) {
	        synchronized (evt) {
	            evt.notifyAll();
	        }
	    }
	    // Remove this specific event from the queue
	    servicedEvent = evt;
	    deleteEvents(this);
	    return 1;
	} else {
	    // The event wasn't actually handled, so we have to
	    // restore the isProcessing field to allow the event to be
	    // attempted again.

	    evt.isProcessing = false;

	    // The handler for this event asked to defer it.  Just go on to
	    // the next event.  we will try to find another event to
	    // process when the while loop continues
	}
    }

    return 0;
}

/*
 *----------------------------------------------------------------------
 *
 * getAvailableEvent --
 *
 *	Search through the internal event list to find the first event
 *	that is has not being processed AND the event is not equal to the given
 *	'skipEvent'.  This method is concurrent safe.
 *	
 * Results:
 *	The return value is a pointer to the first found event that can be
 * 	processed.  If no event is found, this method returns null.
 *
 * Side effects:
 *	This method synchronizes on the 'notifierMutex', which will block any
 *	other thread from adding or removing events from the event queue.
 *
 *----------------------------------------------------------------------
 */
    
private synchronized TclEvent
getAvailableEvent(
    TclEvent    skipEvent)	// Indicates that the given event should not
				// be returned.  This argument can be null.
{
    TclEvent evt;
    for (evt = firstEvent ; evt != null ; evt = evt.next) {
	if ((evt.isProcessing == false) &&
	        (evt.isProcessed  == false) &&
	        (evt != skipEvent)) {
	    return evt;
	}
    }
    return null;
}

/*
 *----------------------------------------------------------------------
 *
 * doOneEvent --
 *
 *	Process a single event of some sort.  If there's no work to
 *	do, wait for an event to occur, then process it. May delay
 *	execution of process while waiting for an event, unless
 *	TCL.DONT_WAIT is set in the flags argument.  This routine
 *	should only be called from the primary thread for the
 *	notifier.
 *	
 * Results:
 *	The return value is 1 if the procedure actually found an event
 *	to process. If no processing occurred, then 0 is returned
 *	(this can happen if the TCL.DONT_WAIT flag is set or if there
 *	are no event handlers to wait for in the set specified by
 *	flags).
 *
 * Side effects:
 *	May delay execution of process while waiting for an event,
 *	unless TCL.DONT_WAIT is set in the flags argument. Event
 *	sources are invoked to check for and queue events. Event
 *	handlers may produce arbitrary side effects.
 *
 *----------------------------------------------------------------------
 */

public native int
doOneEvent(
    int flags);			// Miscellaneous flag values: may be any
				// combination of TCL.DONT_WAIT,
				// TCL.WINDOW_EVENTS, TCL.FILE_EVENTS,
				// TCL.TIMER_EVENTS, TCL.IDLE_EVENTS,
				// or others defined by event sources.

/*
 *----------------------------------------------------------------------
 *
 * alertNotifier --
 *
 *	Wake up the C notifier.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	May cause the C level event loop to wake up.
 *
 *----------------------------------------------------------------------
 */

private final native void
alertNotifier(long tid);


/*
 *----------------------------------------------------------------------
 *
 * init --
 *
 *	Initialize the C event source for the Java notifier.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

private final native long
init();

/*
 *----------------------------------------------------------------------
 *
 * dispose --
 *
 *	Tear down the C event source for the Java notifier.
 *
 * Results:
 *	None.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

final native void
dispose();

/*
 *----------------------------------------------------------------------
 *
 * hasEvents --
 *
 *	Check to see if there are events waiting to be processed.  This
 *	method is concurrent safe.
 *
 * Results:
 *	Returns true if there are events on the Notifier queue.
 *
 * Side effects:
 *	None.
 *
 *----------------------------------------------------------------------
 */

private final boolean 
hasEvents()
{
    boolean result = (getAvailableEvent(null) != null);

    return result;
}

/*
 *----------------------------------------------------------------------
 *
 * finalizeThreadCheck --
 *	Checks to see if thread specific data needs to be cleaned
 *	up and does so if needed.
 *
 * Results:
 *	Will release thread specific data.
 *
 * Side effects:
 *	Thread may detach from JVM, or JVM may be destroyed.
 *
 *----------------------------------------------------------------------
 */

static final native void finalizeThreadCheck();

} // end Notifier

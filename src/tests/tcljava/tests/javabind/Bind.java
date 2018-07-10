// File Bind.java

package tests.javabind;

// This class implements a bindable object that can
// be used to test the java::bind command without
// loading the awt.

public class Bind implements Runnable {
    private ActionListener actionListener = null;
    private Thread t = null;
    private boolean started;
    private boolean stopped;
    private int tnum = 0;

    private final boolean debug = false;

    public void addActionListener(ActionListener l) {
	actionListener = l;
    }

    public void removeActionListener(ActionListener l) {
	actionListener = null;
    }

    public void doLater() {
	// Create new thread that will invoke the call to
	// actionListener.actionPerformed(ActionEvent)
	// in another thread at a later time
	
	started = false;
	stopped = false;

	tnum++;
	t = new Thread(this);
	t.setDaemon(true);
	t.setName("Bind delay thread " + tnum);
	t.start();

	if (debug) {
	    System.err.println("Waiting to invoke action");
	}
    }

    public void run() {
	started = true;

	// Go to sleep for some time
	try {
	    Thread.sleep(1000);
	}
	catch (InterruptedException ie) {}

	if (debug) {
	    System.err.println("Now to invoke action");
	}

	// Now invoke the callback (from a different thread)
	if (actionListener != null)
	    actionListener.actionPerformed(new ActionEvent(this));

	stopped = true;
	t = null;
    }

    public boolean wasStarted() {
	return started;
    }

    public boolean wasStopped() {
	return stopped;
    }
}

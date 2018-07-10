/*
 * StopWatchThread.java
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 *   This file implements the StopWatchThread class.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: StopWatchThread.java,v 1.2 1999/05/08 23:28:22 dejong Exp $
 */

import java.awt.*;
import java.awt.event.*;

/**
 * The StopWatchThread class implements a thread which operates the GUI 
 * and acts as a monitor for the stopwatch's internal time.  
 */

public class StopWatchThread extends Thread {
    
    private int internalTime;          /* stopwatch's time */
    private boolean currentlyCounting; /* whether the stopwatch is counting */

    // Declare GUI objects

    private Frame swFrame;
    private Button quitButton;
    private Label timeLabel;
    private Label statusLabel;
    
    // constructor initializes private variables and GUI

    public StopWatchThread() {

        internalTime = 0;
	currentlyCounting = false;
        
	swFrame = new Frame("StopWatch");
        timeLabel = new Label(internalTime + " seconds       ");
        statusLabel = new Label("halted:          ");
	
        GridBagLayout g = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
	quitButton = new Button(" Quit ");
	quitButton.addActionListener(new QuitButtonListener());
        swFrame.setLayout(g);

	c.gridx = 1;
	c.gridy = 1;
	c.gridwidth = 1;
	c.insets.top    = 20;
	c.insets.bottom = 20;
	c.insets.left   = 20;
	c.insets.right  = 20;
	g.setConstraints(timeLabel, c);
	swFrame.add(timeLabel);

	c.gridx = 0;
	c.gridy = 1;
	c.gridwidth = 1;
	c.insets.top    = 20;
	c.insets.bottom = 20;
	c.insets.left   = 20;
	c.insets.right  = 20;
	g.setConstraints(statusLabel, c);
	swFrame.add(statusLabel);

	c.gridx = 0;
	c.gridy = 0;
	c.gridwidth = 1;
	c.insets.top    = 20;
	c.insets.bottom = 20;
	c.insets.left   = 20;
	c.insets.right  = 20;
	g.setConstraints(quitButton, c);
	swFrame.add(quitButton);

  	swFrame.pack();
  	swFrame.show();
    }

    /**
     * When the sw.start() method is called, this is the method
     * that runs.
     *
     * run() loop alternates between waiting and counting down
     */

    public synchronized void run() {

 	while (true) {
	    // wait for other synchronized methods to call notify()

 	    try {
 		wait();
  	    } catch (Exception e) {System.out.println("Exception on wait");}

	    // countdown loop alternates between updating values and
	    // waiting 1 second

	    while ((internalTime > 0) && currentlyCounting) {
		// Decrement internal time value.

		--internalTime;
		timeLabel.setText(internalTime + " seconds       ");
		swFrame.repaint();

		// If internal time is 0, update GUI and currentlyCounting flag
		// accordingly, and break out of the countdown loop.

		if (internalTime < 1) {
		    currentlyCounting = false;
		    statusLabel.setText("halted:          ");
		    swFrame.repaint();
		    break;
		}

		// Wait up to 1 second for other synchronized methods to call 
		// notify().

		try {
		    wait(1000);
		} catch (Exception e) {System.out.println("Exception on wait");}
	    }
 	}
    }

    /**
     * setTime() updates the stopwatch's internal time and resumes the countdown
     */

    public synchronized void setTime(int newTime) {
        internalTime = newTime;
	timeLabel.setText(internalTime + " seconds       ");
	resumeCountdown();
	return;
    }

    /**
     * resumeCountdown() sets the stopwatch's counting status to true and
     * wakes the thread from wait call in run() method, thereby inducing
     * the countdown.
     *
     * Returns the stopwatch's internal time value.
     */

    public synchronized int resumeCountdown() {
	if (internalTime < 1) {
	    return 0;
	}
	statusLabel.setText("counting:        ");
	swFrame.repaint();

	// The thread sleeps for 1 second before inducing the countdown
	// to ensure the clock is stable for at least 1 second before
	// its value is decremented.

	currentlyCounting = false;
 	try {
 	    sleep(1000);
 	} catch (Exception e) {System.out.println("Exception on sleep");}

	// Wake the thread up and allow it to enter the countdown loop.

	currentlyCounting = true;
	notify();
	return internalTime;
    }

    /**
     * stopCountdown() wakes the thread from wait call in run() method, thereby
     * forcing it to exit the countdown loop.
     *
     * Returns the stopwatch's internal time value.
     */

    public synchronized int stopCountdown() {
	statusLabel.setText("halted:          ");
	swFrame.repaint();
	currentlyCounting = false;
	notify();
	return internalTime;
    }

    // die() disposes of the GUI and permanently stops the thread.

    public synchronized void die() {
	swFrame.dispose();
	stop();
	return;
    }
}

// This class implements the "quit" button in the StopWatch GUI.

class QuitButtonListener implements ActionListener {

    QuitButtonListener() {
    }
    
    public void actionPerformed(ActionEvent event) {
	System.out.println("Exitting Stop Watch.");
	System.exit(0);
    }
}


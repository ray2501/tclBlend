/*
 * ListboxCmd.java
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: ListboxApp.java,v 1.3 1999/08/27 23:19:21 mo Exp $
 *
 */

import java.awt.*;
import java.awt.event.*;

/**
 * A simple application that displays a listbox, with one or more 
 * items, and two buttons, 'OK' and 'Cancel'.  Once the constructor
 * is called, it runs the "application" and polls until an actionEvent
 * occurs (i.e. OK or Cancel is pressed..  At this point, items is 
 * set with a list of selected list items and can be retieved by 
 * calling 'getSelectedItems()'.
 */

public class ListboxApp {
    // The Java AWT Widgets (ummm... I mean Components.) 
    public List     lst;
    public Frame    frm;
    public Button   okButton;
    public Button   cancelButton;

    /**
     * ListboxApp --
     *
     * Create the application and insert listItems into the AWT List.
     */

    public ListboxApp(String[] listItems) {

        GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();

	frm = new Frame();
	frm.setLayout(gbl);

	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
		
	lst = new List(6, true);
	for (int i = 0; i < listItems.length; i++) {
	    lst.add(listItems[i]);
	}
	gbl.setConstraints(lst, gbc);
	frm.add(lst);

	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0.0;
	gbc.weighty = 0.0;

	gbc.gridwidth = 1;
	gbc.gridx = 0;
	gbc.gridy = 1;

	okButton = new Button("OK");
	gbl.setConstraints(okButton, gbc);
	frm.add(okButton);

	gbc.gridx = 1;
	gbc.gridy = 1;

	cancelButton = new Button("Cancel");
	gbl.setConstraints(cancelButton, gbc);
	frm.add(cancelButton);

	frm.setSize(170,170);
	frm.show();
    }
}

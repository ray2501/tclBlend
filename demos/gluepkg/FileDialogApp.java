/*
 * FileDialogApp.java
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: FileDialogApp.java,v 1.3 1999/08/27 23:19:21 mo Exp $
 *
 */

import java.awt.*;
import java.io.*;

/**
 * A simple "application" that just wraps around the AWT FileDialog 
 * component.  Once the FileDialog is no longer displayed, then calls
 * to getFile() and getDirectory() will return information on which path 
 * was selected.
 */

public class FileDialogApp {

    /**
     * The Java AWT FileDialog. 
     */

    private FileDialog fd;              

    /**
     * FileDialogApp --
     *
     * Create a FileDialog.  Since the AWT FileDialog is modal, it
     * will block until the window removed.
     */

    public FileDialogApp() {
	Frame f = new Frame();
	fd = new FileDialog(f, "Select a JAR File.");
	fd.setFilenameFilter(new JarFilenameFilter());
	fd.show();
    }

    /**
     * getFile --
     *
     * Gets the file of the Dialog.
     */

    public String getFile() {
        return fd.getFile();
    }

    /**
     * getDirectory --
     *
     * Gets the directory of the Dialog.
     */

    public String getDirectory() {
        return fd.getDirectory();
    }

    /**
     * setDirectory --
     *
     * Set the directory of the Dialog to the specified directory.
     */

    public void setDirectory(String dirName) {
        fd.setDirectory(dirName);
    }
}

/*
 * This is an attempt to implement a FilenameFilter that only displays 
 * '*.jar' files.  After much hemming and hawing, I discovered that there
 * is a bug in Java and the accept() method is never called.  However, this
 * was left in hopes that it will someday work...
 */

class JarFilenameFilter implements FilenameFilter {
    
    public boolean accept(File dir, String name) {
        System.out.println("Name: " + name);
        if (name.endsWith(".jar") || name.endsWith(".zip")) {
	    return(true);
	} else {
	    return(false);
	}
    }			
}

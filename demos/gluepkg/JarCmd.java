/*
 * JarCmd.java
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: JarCmd.java,v 1.4 2005/01/13 06:21:14 mdejong Exp $
 *
 */

import tcl.lang.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;

/**
 * This class implements the "jar" demo command.
 */

class JarCmd implements Command {

    // An array of acceptable options.

    private static final String cmds[] = {
        "-extract"
    };

    private final int OPT_EXTRACT = 0;

    /*
     * cmdProc --
     *
     * This procedure is invoked to process the jar command.
     *
     * If only the jar filename is passed the result will be a listing
     * of the contents of the jar file.  Otherwise if "-extract" is passed
     * with a filename, then that file will be extracted from the jar and 
     * the result will a TclString of the file contents.
     */

    public void cmdProc(Interp interp, TclObject argv[])
            throws TclException {
	TclObject   result;                  // Stores the TclResult
        String      jarFileName;             // Name of jar file
        String      extFileName = null;      // Name of file to extract
	ZipFile     zFileObj    = null;      // Handle to opened jar file
	ZipEntry    zEntry;                  // Handle to each jar entry
	Enumeration e;                       // Enum of each jar entry
	boolean     extract = false;         // True if extracting a file
	int         index;                   // Result of TclIndex.get()

	if ((argv.length != 2) && (argv.length != 4))  {
	    throw new TclNumArgsException(interp, 1, argv, 
		    "filename ?-extract filename?");
	} 
	    
	if (argv.length == 4) {
	    index = TclIndex.get(interp, argv[2], cmds, "option", 0);
	    switch (index) {
  	        case OPT_EXTRACT: 
		    extract = true;
		    extFileName = argv[3].toString();
		    break;
	    }
	}
	
	jarFileName = argv[1].toString();
	zFileObj  = openZipFile(interp, jarFileName);
	
	result = TclList.newInstance();
	e   = zFileObj.entries();

	while (e.hasMoreElements()) { 

	    zEntry = (ZipEntry) e.nextElement();

	    if (!extract) {
		// If not extracting, simply append each filename to the 
		// end of the TclList.

	        TclList.append(interp, result, 
	                TclString.newInstance(zEntry.getName()));
	    } else {
		// We are extracting a file, so see if the current ZipEntry's
		// name equals the file we want to extract.

	        if (extFileName.equals(zEntry.getName())) {
		    // If the file that we want to extract is found, extract
		    // the contents, convert it to a TclString, set the 
		    // result and return.

		    String s = null;

		    try {
		        s = extractZipFile(zFileObj, zEntry);
		    } catch (IOException e) {
		        break;
		    }
		    result = TclString.newInstance(s);
		    interp.setResult(result);
		    return;
		}		    
	    }
	}
	
	if (!extract) {
	    interp.setResult(result);
	} else {
	    throw new TclException(interp, "\"" + extFileName + 
                    "\" not found in \"" + jarFileName + "\"");
	} 
    }

    /*
     * extractZipFile --
     * 
     * Takes a ZipFile and current ZipEntry, opens an inputStream for that
     * ZipEntry, and reads all of the data to a StringBuffer.
     * Returns contents of the ZipEntry
     */

    private static String extractZipFile(ZipFile zFile, ZipEntry zEntry) 
            throws IOException {
	int i;
	boolean      eof = false;
	InputStream  in = zFile.getInputStream(zEntry);;
	StringBuffer sbuf = new StringBuffer();

	while (!eof) {
	    i = in.read();
	    eof = (i == -1);
	    if (!eof) {
	        sbuf.append((char)i);
	    }
	}
	return (sbuf.toString());
    } 

    // openZipFile --
    //
    // Opens a file, specified by fileName, in the form of a ZipFile
    // Returns the ZipFile object.

    private static ZipFile openZipFile(Interp interp, String fileName) 
            throws TclException {
	boolean zipErr = false;    // True if not a jar file
	boolean ioErr  = false;    // True if file dosent exist
	ZipFile zf     = null;     // The return value, set to null
				   // to make the compilier happy

	try {
	    zf = new ZipFile(fileName);
	} catch (ZipException e) {
	    zipErr = true;
	} catch (IOException e) {
	    ioErr = true;
	} finally {
	    if (zipErr) {
	        throw new TclException(interp, "\"" + fileName 
                        + "\" is not a valid jar file");
	    }
	    if (ioErr) {
	        throw new TclException(interp, "invalid filename: \""
                        + fileName + "\"");
	    }
	}
	return(zf);
    }
}

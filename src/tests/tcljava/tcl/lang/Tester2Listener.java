/*
 * Tester2Listener.java --
 *
 *	This is an event listener interface that test various capability
 *	of the AdaptorGen class.
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 * RCS: @(#) $Id: Tester2Listener.java,v 1.1 1999/05/10 04:09:00 dejong Exp $
 */

package tcl.lang;

import java.util.*;

/*
 * This is an event listener interface that test various capability of
 * the AdaptorGen class. AdaptorGen should be able to handle an
 * interface whose methods may take any arguments, return any type of
 * value and throw arbitrart exceptions.
 */

public interface Tester2Listener extends EventListener {

/*
 * A very conventional event method.
 */

public void method0(Tester2Event eventObj);

} // end Tester2Listener




/*
empty implementation of Notifier used for compiling in multiple packages
*/

package tcl.lang;

import java.lang.Thread;

public class Notifier {

Thread primaryThread = null;

public static synchronized Notifier
getNotifierForThread(Thread thread)
{
    return null;
}

public synchronized void preserve() {}

public synchronized void release() {}

public synchronized void queueEvent(TclEvent evt, int position) {}

public synchronized void deleteEvents(EventDeleter deleter) {}

synchronized int serviceEvent(int flags) {
  return 0;
}

public int doOneEvent(int flags) {
  return 0;
}

final void dispose() {}

} // end Notifier


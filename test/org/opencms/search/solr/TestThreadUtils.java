
package org.opencms.search.solr;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Utility class for thread analyzing.<p>
 */
public final class TestThreadUtils {

    /**
     * Hide default constructor.<p>
     */
    private TestThreadUtils() {

        // noop
    }

    /**
     * Returns all thread infos.<p>
     *
     * @return all thread infos
     */
    public static ThreadInfo[] getAllThreadInfos() {

        final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
        final long[] ids = thbean.getAllThreadIds();

        ThreadInfo[] infos;
        if (!thbean.isObjectMonitorUsageSupported() || !thbean.isSynchronizerUsageSupported()) {
            infos = thbean.getThreadInfo(ids);
        } else {
            infos = thbean.getThreadInfo(ids, true, true);
        }

        final ThreadInfo[] notNulls = new ThreadInfo[infos.length];
        int nNotNulls = 0;
        for (ThreadInfo info : infos) {
            if (info != null) {
                notNulls[nNotNulls++] = info;
            }
        }
        if (nNotNulls == infos.length) {
            return infos;
        }
        return java.util.Arrays.copyOf(notNulls, nNotNulls);
    }

    /**
     * Returns all threads.<p>
     *
     * @return all threads
     */
    public static Thread[] getAllThreads() {

        final ThreadGroup root = getRootThreadGroup();
        final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
        int nAlloc = thbean.getThreadCount();
        int n = 0;
        Thread[] threads;
        do {
            nAlloc *= 2;
            threads = new Thread[nAlloc];
            n = root.enumerate(threads, true);
        } while (n == nAlloc);
        return java.util.Arrays.copyOf(threads, n);
    }

    /**
     * Returns the thread that is blocking the given one.<p>
     *
     * @param thread the thread to get the blocking thread for
     *
     * @return the thread that is blocking the given one
     */
    public static Thread getBlockingThread(final Thread thread) {

        final ThreadInfo info = getThreadInfo(thread);
        if (info == null) {
            return null;
        }
        final long id = info.getLockOwnerId();
        if (id == -1) {
            return null;
        }
        return getThread(id);
    }

    /**
     * Returns a list of all threads belonging to one thread group.<p>
     *
     * @param group the group to get the threads for
     *
     * @return an array of threads for the given group
     */
    public static Thread[] getGroupThreads(final ThreadGroup group) {

        if (group == null) {
            throw new NullPointerException("Null thread group");
        }
        int nAlloc = group.activeCount();
        int n = 0;
        Thread[] threads;
        do {
            nAlloc *= 2;
            threads = new Thread[nAlloc];
            n = group.enumerate(threads);
        } while (n == nAlloc);
        return java.util.Arrays.copyOf(threads, n);
    }

    /**
     * Returns the thread that is locking the thread identified by the given id.<p>
     *
     * @param identity the thread id to get the locking thread for
     *
     * @return the locaking thread or <code>null</code> if the thread for the given id is not locked
     */
    public static Thread getLockingThread(long identity) {

        final Thread[] allThreads = getAllThreads();
        ThreadInfo info = null;
        MonitorInfo[] monitors = null;
        for (Thread thread : allThreads) {
            info = getThreadInfo(thread.getId());
            if (info == null) {
                continue;
            }
            monitors = info.getLockedMonitors();
            for (MonitorInfo monitor : monitors) {
                if (identity == monitor.getIdentityHashCode()) {
                    return thread;
                }
            }
        }
        return null;
    }

    /**
     * Returns the locking thread for the given object.<p>
     *
     * @param object the object that is potentially locked
     *
     * @return the locking thread for the given object or <code>null</code> if not locked
     */
    public static Thread getLockingThread(final Object object) {

        if (object == null) {
            throw new NullPointerException("Null object");
        }
        final long identity = System.identityHashCode(object);

        final Thread[] allThreads = getAllThreads();
        ThreadInfo info = null;
        MonitorInfo[] monitors = null;
        for (Thread thread : allThreads) {
            info = getThreadInfo(thread.getId());
            if (info == null) {
                continue;
            }
            monitors = info.getLockedMonitors();
            for (MonitorInfo monitor : monitors) {
                if (identity == monitor.getIdentityHashCode()) {
                    return thread;
                }
            }
        }
        return null;
    }

    /**
     * Returns the thread group of the current thread.<p>
     *
     * @return the thread group
     */
    public static ThreadGroup getRootThreadGroup() {

        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        ThreadGroup ptg;
        while ((ptg = tg.getParent()) != null) {
            tg = ptg;
        }
        return tg;
    }

    /**
     * Returns the thread for the given id.<p>
     *
     * @param id of a thread
     *
     * @return the thread or <code>null</code> if not existent
     */
    public static Thread getThread(final long id) {

        final Thread[] threads = getAllThreads();
        for (Thread thread : threads) {
            if (thread.getId() == id) {
                return thread;
            }
        }
        return null;
    }

    /**
     * Returns the thread for the given name.<p>
     *
     * @param name the name of the thread to get
     *
     * @return the thread for the given name or <code>null</code> if not existent
     */
    public static Thread getThread(final String name) {

        if (name == null) {
            throw new NullPointerException("Null name");
        }
        final Thread[] threads = getAllThreads();
        for (Thread thread : threads) {
            if (thread.getName().equals(name)) {
                return thread;
            }
        }
        return null;
    }

    /**
     * Returns the thread info for the thread identified by the given id.<p>
     *
     * @param id the id og the thread to get the info for
     *
     * @return the thread info for the thread identified by the given id or <code>null</code> if not existent
     */
    public static ThreadInfo getThreadInfo(final long id) {

        final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();

        if (!thbean.isObjectMonitorUsageSupported() || !thbean.isSynchronizerUsageSupported()) {
            return thbean.getThreadInfo(id);
        }

        final ThreadInfo[] infos = thbean.getThreadInfo(new long[] {id}, true, true);
        if (infos.length == 0) {
            return null;
        }
        return infos[0];
    }

    /**
     * Returns the thread info for the thread identified by the given name.<p>
     *
     * @param name the name of the thread to get the info for
     *
     * @return the thread info for the thread identified by the given name or <code>null</code> if not existent
     */
    public static ThreadInfo getThreadInfo(final String name) {

        if (name == null) {
            throw new NullPointerException("Null name");
        }
        final Thread[] threads = getAllThreads();
        for (Thread thread : threads) {
            if (thread.getName().equals(name)) {
                return getThreadInfo(thread.getId());
            }
        }
        return null;
    }

    /**
     * Returns the thread info of the given thread.<p>
     *
     * @param thread the thread to get the info for
     *
     * @return  the thread info of the given thread or <code>null</code> if not existent
     */
    public static ThreadInfo getThreadInfo(final Thread thread) {

        if (thread == null) {
            throw new NullPointerException("Null thread");
        }
        return getThreadInfo(thread.getId());
    }

}

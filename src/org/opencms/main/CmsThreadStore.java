/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsThreadStore.java,v $
 * Date   : $Date: 2011/03/23 14:51:30 $
 * Version: $Revision: 1.24 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.main;

import org.opencms.db.CmsSecurityManager;
import org.opencms.publish.CmsPublishJobRunning;
import org.opencms.publish.CmsPublishManager;
import org.opencms.report.A_CmsReportThread;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * The OpenCms "Grim Reaper" thread store were all system Threads are maintained.<p>
 *
 * This thread executes all 60 seconds and checks if report threads are still active.
 * A report thread usually waits for a user to get the contents written to the report.
 * However, if the user does not request the reports content (e.g. because the 
 * browser was closed), then the report thread becomes abandoned. This Grim Reaper
 * will collect all such abandoned report threads and remove them after further 
 * 60 seconds.<p>
 *
 * Moreover, the Grim Reaper checks for all invalid user sessions that have times out for
 * 5 or more minutes, and removes them as well.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.24 $
 * 
 * @since 6.0.0
 */
public final class CmsThreadStore extends Thread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsThreadStore.class);

    /** Indicates that this thread store is alive. */
    private boolean m_alive;

    /** The security manager instance. */
    private CmsSecurityManager m_securityManager;

    /** A map to store all system Threads in. */
    private Map m_threads;

    /**
     * Hides the public constructor.<p>
     * 
     * @param securityManager needed for scheduling "undercover-jobs" 
     *      that increase stability and fault tolerance
     */
    protected CmsThreadStore(CmsSecurityManager securityManager) {

        super(new ThreadGroup("OpenCms Thread Store"), "OpenCms: Grim Reaper");
        setDaemon(true);
        // Hashtable is still the most efficient form of a synchronized HashMap
        m_threads = new Hashtable();
        m_alive = true;
        m_securityManager = securityManager;
        start();
    }

    /**
     * Adds a Thread to this Thread store.<p>
     * 
     * @param thread the Thread to add
     */
    public void addThread(A_CmsReportThread thread) {

        m_threads.put(thread.getUUID(), thread);
        if (LOG.isDebugEnabled()) {
            dumpThreads();
        }
    }

    /**
     * Retrieves a Thread from this Thread store.<p>
     * 
     * @param key the key of the Thread to retrieve
     * @return the Thread form this Thread store that matches the given key
     */
    public A_CmsReportThread retrieveThread(CmsUUID key) {

        if (LOG.isDebugEnabled()) {
            dumpThreads();
        }
        return (A_CmsReportThread)m_threads.get(key);
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        int m_minutesForSessionUpdate = 0;
        int minutesForPublishEngineCheck = 0;
        while (m_alive) {
            // the Grim Reaper is eternal, of course
            try {
                // one minute sleep time
                sleep(60000);
            } catch (InterruptedException e) {
                // let's go on reaping...
            }
            try {
                Iterator i;
                i = m_threads.keySet().iterator();
                Set doomed = new HashSet();
                // first collect all doomed Threads
                while (i.hasNext()) {
                    CmsUUID key = (CmsUUID)i.next();
                    A_CmsReportThread thread = (A_CmsReportThread)m_threads.get(key);
                    if (thread.isDoomed()) {
                        doomed.add(key);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(Messages.get().getBundle().key(
                                Messages.LOG_THREADSTORE_DOOMED_2,
                                thread.getName(),
                                thread.getUUID()));
                        }
                    }
                }
                i = doomed.iterator();
                // no remove all doomed Threads from the Thread store
                while (i.hasNext()) {
                    m_threads.remove(i.next());
                }
                if (LOG.isDebugEnabled()) {
                    dumpThreads();
                }
            } catch (Throwable t) {
                // the Grim Reaper must not be stopped by any error 
                LOG.error(Messages.get().getBundle().key(Messages.LOG_THREADSTORE_CHECK_THREADS_ERROR_0), t);
            }

            // check the session manager for invalid sessions not removed for whatever reason
            m_minutesForSessionUpdate++;
            if (m_minutesForSessionUpdate >= 5) {
                // do this every 5 minutes
                m_minutesForSessionUpdate = 0;
                try {
                    CmsSessionManager sessionInfoManager = OpenCms.getSessionManager();
                    if (sessionInfoManager != null) {
                        // will be null if only the shell is running
                        sessionInfoManager.validateSessionInfos();
                    }
                } catch (Throwable t) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_THREADSTORE_CHECK_SESSIONS_ERROR_0), t);
                }
                // additionally every 5 minutes: save the resource locks to db
                try {
                    m_securityManager.writeLocks();
                } catch (Throwable t) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(org.opencms.lock.Messages.get().getBundle().key(
                            org.opencms.lock.Messages.ERR_WRITE_LOCKS_0), t);
                    }
                }
            }

            // check the publish manager if the current thread is still active
            minutesForPublishEngineCheck++;
            if (minutesForPublishEngineCheck >= 10) {
                // do this every 10 minutes
                minutesForPublishEngineCheck = 0;

                try {
                    CmsPublishManager publishManager = OpenCms.getPublishManager();
                    if (publishManager == null) {
                        // this can happen during shutdown
                        continue;
                    }
                    // get the current publish job
                    CmsPublishJobRunning publishJob = publishManager.getCurrentPublishJob();
                    if (publishJob != null) {
                        // get the thread id of the current publish job
                        CmsUUID uid = publishJob.getThreadUUID();
                        if ((uid != null) && (!uid.isNullUUID())) {
                            // find the thread
                            A_CmsReportThread thread = (A_CmsReportThread)m_threads.get(uid);
                            if (thread != null) {
                                // check if the report still has output and so is active
                                if (CmsStringUtil.isEmptyOrWhitespaceOnly(thread.getReportUpdate())) {
                                    // log thread state
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug(Messages.get().getBundle().key(
                                            Messages.LOG_THREADSTORE_PUBLISH_THREAD_INTERRUPT_2,
                                            thread.getName(),
                                            thread.getUUID()));
                                    }

                                    // interrupt/kill thread
                                    thread.interrupt();

                                    // clean up the interrupted thread
                                    publishManager.checkCurrentPublishJobThread();
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_THREADSTORE_CHECK_PUBLISH_THREAD_ERROR_0), t);
                }
            }
        }
    }

    /**
     * Shut down this thread store.<p>
     */
    protected synchronized void shutDown() {

        m_alive = false;
        interrupt();
    }

    /**
     * Method to dump all currently known Threads.<p> 
     */
    private void dumpThreads() {

        if (LOG.isDebugEnabled()) {
            StringBuffer b = new StringBuffer(512);
            Iterator i = m_threads.keySet().iterator();
            while (i.hasNext()) {
                CmsUUID key = (CmsUUID)i.next();
                A_CmsReportThread thread = (A_CmsReportThread)m_threads.get(key);
                b.append(thread.getName());
                b.append(" - ");
                b.append(thread.getUUID());
                b.append('\n');
            }
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_THREADSTORE_POOL_CONTENT_2,
                new Integer(m_threads.size()),
                b.toString()));
        }
    }
}
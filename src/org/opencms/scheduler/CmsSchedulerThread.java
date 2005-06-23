/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/CmsSchedulerThread.java,v $
 * Date   : $Date: 2005/06/23 11:11:24 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.scheduler;

import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

/**
 * A worker thread for the OpenCms scheduler.<p>
 * 
 * @author Alexander Kandzior 
 *  
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSchedulerThread extends Thread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSchedulerThread.class);

    /** The scheduler thread pool this thread belongs to. */
    private CmsSchedulerThreadPool m_pool;

    /** A flag that signals the thread to terminate. */
    private boolean m_run;

    /** A runnable class. */
    private Runnable m_runnable;

    /**
     * Create a scheduler thread that runs continuosly,
     * waiting for new runnables to be provided by the scheduler thread pool.<p>
     * 
     * @param pool the pool to use
     * @param threadGroup the thread group to use
     * @param threadName the name for the thread
     * @param prio the priority of the thread
     * @param isDaemon controls if this should be a deamon thread or not
     */
    CmsSchedulerThread(
        CmsSchedulerThreadPool pool,
        ThreadGroup threadGroup,
        String threadName,
        int prio,
        boolean isDaemon) {

        this(pool, threadGroup, threadName, prio, isDaemon, null);
    }

    /**
     * Create a scheduler thread that runs the specified runnable exactly once.<p>
     * 
     * @param pool the pool to use
     * @param threadGroup the thread group to use
     * @param threadName the name for the thread
     * @param prio the priority of the thread
     * @param isDaemon controls if this should be a deamon thread or not
     * @param runnable the runnable to run
     */
    CmsSchedulerThread(
        CmsSchedulerThreadPool pool,
        ThreadGroup threadGroup,
        String threadName,
        int prio,
        boolean isDaemon,
        Runnable runnable) {

        super(threadGroup, threadName);
        m_run = true;
        m_pool = pool;
        m_runnable = runnable;
        setPriority(prio);
        setDaemon(isDaemon);
        start();
    }

    /**
     * Loop, executing targets as they are received.<p>
     */
    public void run() {

        boolean runOnce = (m_runnable != null);

        while (m_run) {
            setPriority(m_pool.getThreadPriority());
            try {
                if (m_runnable == null) {
                    m_runnable = m_pool.getNextRunnable();
                }

                if (m_runnable != null) {
                    m_runnable.run();
                }
            } catch (InterruptedException e) {
                LOG.error(Messages.get().key(Messages.LOG_THREAD_INTERRUPTED_1, getName()), e);
            } catch (Throwable t) {
                LOG.error(Messages.get().key(Messages.LOG_THREAD_ERROR_1, getName()), t);
            } finally {
                if (runOnce) {
                    m_run = false;
                }
                m_runnable = null;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_THREAD_SHUTDOWN_1, getName()));
        }
    }

    /**
     * Signal the thread that it should terminate.<p>
     */
    void shutdown() {

        m_run = false;
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/CmsSchedulerThreadPool.java,v $
 * Date   : $Date: 2005/06/22 14:19:40 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * 
 * This library is based to some extend on code from the 
 * OpenSymphony Quartz project. Original copyright notice:
 * 
 * Copyright James House (c) 2001-2005
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.opencms.scheduler;

import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

/**
 * Simple thread pool used for the Quartz scheduler in OpenCms.<p>
 * 
 * @author Alexander Kandzior 
 * @author James House
 * @author Juergen Donnerstag
 *
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSchedulerThreadPool implements ThreadPool {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSchedulerThreadPool.class);

    private int m_currentThreadCount;

    private boolean m_inheritGroup;

    private boolean m_inheritLoader;

    private int m_initialThreadCount;

    private boolean m_isShutdown;

    private boolean m_makeThreadsDaemons;

    private int m_maxThreadCount;

    private Runnable m_nextRunnable;

    private Object m_nextRunnableLock;

    private ThreadGroup m_threadGroup;

    private String m_threadNamePrefix;

    private int m_threadPriority;

    private CmsSchedulerThread[] m_workers;

    /**
     * Create a new <code>CmsSchedulerThreadPool</code> with default values.
     * 
     * This will create a pool with 0 initial and 10 maximum threads running 
     * in normal priority.<p>
     * 
     * @see #CmsSchedulerThreadPool(int, int, int)
     */
    public CmsSchedulerThreadPool() {

        this(0, 10, Thread.NORM_PRIORITY);
    }

    /**
     * Create a new <code>CmsSchedulerThreadPool</code> with the specified number
     * of threads that have the given priority.
     * 
     * The OpenCms scheduler thread pool will initially start with provided number of
     * active scheduler threads.
     * When a thread is requested by the scheduler, and no "free" threads are available,
     * a new thread will be added to the pool and used for execution. The pool 
     * will be allowed to grow until it has reached the configured number 
     * of maximum threads.<p>
     * 
     * @param initialThreadCount the initial number of threads for the pool
     * @param maxThreadCount maximum number of threads the pool is allowed to grow
     * @param threadPriority the thread priority for the scheduler threads
     * 
     * @see java.lang.Thread
     */
    public CmsSchedulerThreadPool(int initialThreadCount, int maxThreadCount, int threadPriority) {

        m_inheritGroup = true;
        m_inheritLoader = true;
        m_nextRunnableLock = new Object();
        m_threadNamePrefix = "OpenCms: Scheduler Thread ";
        m_makeThreadsDaemons = true;
        m_initialThreadCount = initialThreadCount;
        m_currentThreadCount = 0;
        m_maxThreadCount = maxThreadCount;
        m_threadPriority = threadPriority;
    }

    /**
     * @see org.quartz.spi.ThreadPool#getPoolSize()
     */
    public int getPoolSize() {

        return m_currentThreadCount;
    }

    /**
     * Returns the thread priority of the threads in the scheduler pool.<p>
     * 
     * @return the thread priority of the threads in the scheduler pool 
     */
    public int getThreadPriority() {

        return m_threadPriority;
    }

    /**
     * @see org.quartz.spi.ThreadPool#initialize()
     */
    public void initialize() throws SchedulerConfigException {

        if (m_maxThreadCount <= 0 || m_maxThreadCount > 200) {
            throw new SchedulerConfigException(Messages.get().key(Messages.ERR_MAX_THREAD_COUNT_BOUNDS_0));
        }
        if (m_initialThreadCount < 0 || m_initialThreadCount > m_maxThreadCount) {
            throw new SchedulerConfigException(Messages.get().key(Messages.ERR_INIT_THREAD_COUNT_BOUNDS_0));
        }
        if (m_threadPriority <= 0 || m_threadPriority > 9) {
            throw new SchedulerConfigException(Messages.get().key(Messages.ERR_SCHEDULER_PRIORITY_BOUNDS_0));
        }

        if (m_inheritGroup) {
            m_threadGroup = Thread.currentThread().getThreadGroup();
        } else {
            // follow the threadGroup tree to the root thread group
            m_threadGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parent = m_threadGroup;
            while (!parent.getName().equals("main")) {
                m_threadGroup = parent;
                parent = m_threadGroup.getParent();
            }
            m_threadGroup = new ThreadGroup(parent, this.getClass().getName());
        }

        if (m_inheritLoader) {
            LOG.debug(Messages.get().key(Messages.LOG_USING_THREAD_CLASSLOADER_1, Thread.currentThread().getName()));
        }

        // create the worker threads and start them
        m_workers = new CmsSchedulerThread[m_maxThreadCount];
        for (int i = 0; i < m_initialThreadCount; ++i) {
            growThreadPool();
        }
    }

    /**
     * Run the given <code>Runnable</code> object in the next available
     * <code>Thread</code>.<p>
     * 
     * If while waiting the thread pool is asked to
     * shut down, the Runnable is executed immediately within a new additional
     * thread.<p>
     * 
     * @param runnable the <code>Runnable</code> to run
     * @return true if the <code>Runnable</code> was run
     */
    public boolean runInThread(Runnable runnable) {

        if (runnable == null) {
            return false;
        }

        if (m_isShutdown) {
            LOG.debug(Messages.get().key(Messages.LOG_THREAD_POOL_UNAVAILABLE_0));
            return false;
        }

        if ((m_currentThreadCount == 0) || (m_nextRunnable != null)) {
            // try to grow the thread pool since other runnables are already waiting
            growThreadPool();
        }

        synchronized (m_nextRunnableLock) {

            // wait until a worker thread has taken the previous Runnable
            // or until the thread pool is asked to shutdown
            while ((m_nextRunnable != null) && !m_isShutdown) {
                try {
                    m_nextRunnableLock.wait(1000);
                } catch (InterruptedException e) {
                    // can be ignores
                }
            }

            // during normal operation, not shutdown, set the nextRunnable
            // and notify the worker threads waiting (getNextRunnable())
            if (!m_isShutdown) {
                m_nextRunnable = runnable;
                m_nextRunnableLock.notifyAll();
            }
        }

        // if the thread pool is going down, execute the Runnable
        // within a new additional worker thread (no thread from the pool)
        // note: the synchronized section should be as short (time) as
        // possible as starting a new thread is not a quick action
        if (m_isShutdown) {
            new CmsSchedulerThread(
                this,
                m_threadGroup,
                m_threadNamePrefix + "(final)",
                m_threadPriority,
                false,
                runnable);
        }

        return true;
    }

    /**
     * Terminate any worker threads in this thread group.<p>
     * 
     * Jobs currently in progress will be allowed to complete.<p>
     */
    public void shutdown() {

        shutdown(true);
    }

    /**
     * Terminate all threads in this thread group.<p>
     * 
     * @param waitForJobsToComplete if true,, all current jobs will be allowed to complete
     */
    public void shutdown(boolean waitForJobsToComplete) {

        m_isShutdown = true;

        // signal each scheduler thread to shut down
        for (int i = 0; i < m_currentThreadCount; i++) {
            if (m_workers[i] != null) {
                m_workers[i].shutdown();
            }
        }

        // give waiting (wait(1000)) worker threads a chance to shut down
        // active worker threads will shut down after finishing their
        // current job
        synchronized (m_nextRunnableLock) {
            m_nextRunnableLock.notifyAll();
        }

        if (waitForJobsToComplete) {
            // wait until all worker threads are shut down
            int alive = m_currentThreadCount;
            while (alive > 0) {
                alive = 0;
                for (int i = 0; i < m_currentThreadCount; i++) {
                    if (m_workers[i].isAlive()) {
                        try {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(Messages.get().key(Messages.LOG_THREAD_POOL_WAITING_1, new Integer(i)));
                            }

                            // note: with waiting infinite - join(0) - the application 
                            // may appear to 'hang' 
                            // waiting for a finite time however requires an additional loop (alive)
                            alive++;
                            m_workers[i].join(200);
                        } catch (InterruptedException e) {
                            // can be ignored
                        }
                    }
                }
            }

            int activeCount = m_threadGroup.activeCount();
            if (activeCount > 0 && LOG.isInfoEnabled()) {
                LOG.info(Messages.get().key(Messages.LOG_THREAD_POOL_STILL_ACTIVE_1, new Integer(activeCount)));
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_THREAD_POOL_SHUTDOWN_0));
            }
        }
    }

    /**
     * Dequeue the next pending <code>Runnable</code>.<p>
     * 
     * @return the next pending <code>Runnable</code>
     * @throws InterruptedException if something goes wrong
     */
    protected Runnable getNextRunnable() throws InterruptedException {

        Runnable toRun = null;

        // Wait for new Runnable (see runInThread()) and notify runInThread()
        // in case the next Runnable is already waiting.
        synchronized (m_nextRunnableLock) {
            if (m_nextRunnable == null) {
                m_nextRunnableLock.wait(1000);
            }

            if (m_nextRunnable != null) {
                toRun = m_nextRunnable;
                m_nextRunnable = null;
                m_nextRunnableLock.notifyAll();
            }
        }

        return toRun;
    }

    /**
     * Grows the thread pool by one new thread if the maximum pool size 
     * has not been reached.<p>
     */
    private void growThreadPool() {

        if (m_currentThreadCount < m_maxThreadCount) {
            // if maximum number is not reached grow the thread pool
            synchronized (m_nextRunnableLock) {
                m_workers[m_currentThreadCount] = new CmsSchedulerThread(this, m_threadGroup, m_threadNamePrefix
                    + m_currentThreadCount, m_threadPriority, m_makeThreadsDaemons);
                if (m_inheritLoader) {
                    m_workers[m_currentThreadCount].setContextClassLoader(Thread.currentThread().getContextClassLoader());
                }
                // increas the current size
                m_currentThreadCount++;
                // notify the waiting threads
                m_nextRunnableLock.notifyAll();
            }
        }
    }
}
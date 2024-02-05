/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search;

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;

import java.io.IOException;

import org.apache.commons.logging.Log;

/**
 * Implements the management of indexing threads.<p>
 *
 * @since 6.0.0
 */
public class CmsIndexingThreadManager {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsIndexingThreadManager.class);

    /** Number of threads abandoned. */
    private int m_abandonedCounter;

    /** The time the last error was written to the log. */
    private long m_lastLogErrorTime;

    /** The time the last warning was written to the log. */
    private long m_lastLogWarnTime;

    /** The maximum number of modifications before a commit in the search index is triggered. */
    private int m_maxModificationsBeforeCommit;

    /** Number of thread returned. */
    private int m_returnedCounter;

    /** Overall number of threads started. */
    private int m_startedCounter;

    /** Timeout for abandoning threads. */
    private long m_timeout;

    /**
     * Creates and starts a thread manager for indexing threads.<p>
     *
     * @param timeout timeout after a thread is abandoned
     * @param maxModificationsBeforeCommit the maximum number of modifications before a commit in the search index is triggered
     */
    public CmsIndexingThreadManager(long timeout, int maxModificationsBeforeCommit) {

        m_timeout = timeout;
        m_maxModificationsBeforeCommit = maxModificationsBeforeCommit;
    }

    /**
     * Creates and starts a new indexing thread for a resource.<p>
     *
     * After an indexing thread was started, the manager suspends itself
     * and waits for an amount of time specified by the <code>timeout</code>
     * value. If the timeout value is reached, the indexing thread is
     * aborted by an interrupt signal.<p>
     *
     * @param indexer the VFS indexer to create the index thread for
     * @param writer the index writer that can update the index
     * @param res the resource
     */
    public void createIndexingThread(CmsVfsIndexer indexer, I_CmsIndexWriter writer, CmsResource res) {

        I_CmsReport report = indexer.getReport();
        m_startedCounter++;
        CmsIndexingThread thread = new CmsIndexingThread(
            indexer.getCms(),
            res,
            indexer.getIndex(),
            m_startedCounter,
            report);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        try {
            thread.join(m_timeout);
        } catch (InterruptedException e) {
            // ignore
        }
        if (thread.isAlive()) {
            // the thread has not finished - so it must be marked as an abandoned thread
            m_abandonedCounter++;
            thread.interrupt();
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_INDEXING_TIMEOUT_1, res.getRootPath()));
            }
            if (report != null) {
                report.println();
                report.print(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                    I_CmsReport.FORMAT_WARNING);
                report.println(
                    Messages.get().container(Messages.RPT_SEARCH_INDEXING_TIMEOUT_1, res.getRootPath()),
                    I_CmsReport.FORMAT_WARNING);
            }
        } else {
            // the thread finished normally
            m_returnedCounter++;
        }
        I_CmsSearchDocument doc = thread.getResult();
        if (doc != null) {
            // write the document to the index
            indexer.updateResource(writer, res.getRootPath(), doc);
        } else {
            indexer.deleteResource(writer, new CmsPublishedResource(res));
        }
        if ((m_startedCounter % m_maxModificationsBeforeCommit) == 0) {
            try {
                writer.commit();
            } catch (IOException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        Messages.get().getBundle().key(
                            Messages.LOG_IO_INDEX_WRITER_COMMIT_2,
                            indexer.getIndex().getName(),
                            indexer.getIndex().getPath()),
                        e);
                }
            }
        }
    }

    /**
     * Returns if the indexing manager still have indexing threads.<p>
     *
     * @return true if the indexing manager still have indexing threads
     */
    public boolean isRunning() {

        if (m_lastLogErrorTime <= 0) {
            m_lastLogErrorTime = System.currentTimeMillis();
            m_lastLogWarnTime = m_lastLogErrorTime;
        } else {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - m_lastLogWarnTime) > 30000) {
                // write warning to log after 30 seconds
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        Messages.get().getBundle().key(
                            Messages.LOG_WAITING_ABANDONED_THREADS_2,
                            Integer.valueOf(m_abandonedCounter),
                            Integer.valueOf((m_startedCounter - m_returnedCounter))));
                }
                m_lastLogWarnTime = currentTime;
            }
            if ((currentTime - m_lastLogErrorTime) > 600000) {
                // write error to log after 10 minutes
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.LOG_WAITING_ABANDONED_THREADS_2,
                        Integer.valueOf(m_abandonedCounter),
                        Integer.valueOf((m_startedCounter - m_returnedCounter))));
                m_lastLogErrorTime = currentTime;
            }
        }

        boolean result = (m_returnedCounter + m_abandonedCounter) < m_startedCounter;
        if (result && LOG.isInfoEnabled()) {
            // write a note to the log that all threads have finished
            LOG.info(Messages.get().getBundle().key(Messages.LOG_THREADS_FINISHED_0));
        }
        return result;
    }

    /**
     * Writes statistical information to the report.<p>
     *
     * The method reports the total number of threads started
     * (equals to the number of indexed files), the number of returned
     * threads (equals to the number of successfully indexed files),
     * and the number of abandoned threads (hanging threads reaching the timeout).
     *
     * @param report the report to write the statistics to
     */
    public void reportStatistics(I_CmsReport report) {

        if (report != null) {
            CmsMessageContainer message = Messages.get().container(
                Messages.RPT_SEARCH_INDEXING_STATS_4,
                new Object[] {
                    Integer.valueOf(m_startedCounter),
                    Integer.valueOf(m_returnedCounter),
                    Integer.valueOf(m_abandonedCounter),
                    report.formatRuntime()});

            report.println(message);
            if (!(report instanceof CmsLogReport) && LOG.isInfoEnabled()) {
                // only write to the log if report is not already a log report
                LOG.info(message.key());
            }
        }
    }
}
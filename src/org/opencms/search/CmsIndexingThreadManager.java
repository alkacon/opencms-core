/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsIndexingThreadManager.java,v $
 * Date   : $Date: 2005/07/29 12:13:00 $
 * Version: $Revision: 1.23 $
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

package org.opencms.search;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsDocumentFactory;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

/**
 * Implements the management of indexing threads.<p>
 * 
 * @author Carsten Weinholz 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.23 $ 
 * 
 * @since 6.0.0 
 */
public class CmsIndexingThreadManager extends Thread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsIndexingThreadManager.class);

    /** Number of threads abandoned. */
    private int m_abandonedCounter;

    /** Cache for extracted documents. */
    private Map m_documentCache;

    /** Overall number of threads started. */
    private int m_fileCounter;

    /** The report. */
    private I_CmsReport m_report;

    /** Number of thread returned. */
    private int m_returnedCounter;

    /** Timeout for abandoning threads. */
    private long m_timeout;

    /**
     * Creates and starts a thread manager for indexing threads.<p>
     * @param report the report to write out progress information
     * @param timeout timeout after a thread is abandoned
     * @param indexName the name of the index
     * @param documentCache cache for storing indexed documents in (to avoid multiple text extraction)
     */
    public CmsIndexingThreadManager(I_CmsReport report, long timeout, String indexName, Map documentCache) {

        super("OpenCms: Indexing thread manager for search index '" + indexName + "'");

        m_report = report;
        m_timeout = timeout;
        m_fileCounter = 0;
        m_abandonedCounter = 0;
        m_returnedCounter = 0;
        m_documentCache = documentCache;
        this.start();
    }

    /**
     * Caches the generated Document for the index resource to avoid multiple text extraction.<p>
     *  
     * @param res the index resource to cache the Document for
     * @param locale the locale to chache the Document fot
     * @param doc the Document to cache
     */
    public synchronized void addDocument(A_CmsIndexResource res, String locale, Document doc) {

        if (m_documentCache != null) {
            m_documentCache.put(res.getId().toString().concat(locale), doc);
        }
    }

    /**
     * Creates and starts a new indexing thread for a resource.<p>
     * 
     * After an indexing thread was started, the manager suspends itself 
     * and waits for an amount of time specified by the <code>timeout</code>
     * value. If the timeout value is reached, the indexing thread is
     * aborted by an interrupt signal.<p>
     * 
     * @param cms the cms object
     * @param writer the write to write the index
     * @param res the resource
     * @param index the index
     */
    public void createIndexingThread(CmsObject cms, IndexWriter writer, A_CmsIndexResource res, CmsSearchIndex index) {

        I_CmsDocumentFactory factory = OpenCms.getSearchManager().getDocumentFactory(res);
        if ((factory == null) || !index.getDocumenttypes(res.getRootPath()).contains(factory.getName())) {
            // document type dos not match the ones configured for the index, skip document

            m_fileCounter++;
            m_returnedCounter++;
            if (m_report != null) {
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SKIPPED_0),
                    I_CmsReport.FORMAT_NOTE);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_SKIPPED_1, res.getRootPath()));
            }

            // no need to continue
            return;
        }

        Document cachedDoc;
        if ((m_documentCache != null)
            && ((cachedDoc = (Document)m_documentCache.get(res.getId().toString().concat(index.getLocale()))) != null)) {
            // search document for the resource has already been cached, just re-use without extra Thread

            m_fileCounter++;
            m_returnedCounter++;
            try {
                writer.addDocument(cachedDoc);
                if (m_report != null) {
                    m_report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                }
            } catch (IOException e) {
                if (m_report != null) {
                    m_report.println();
                    m_report.print(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                        I_CmsReport.FORMAT_WARNING);
                    m_report.println(e);

                }
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().key(
                        Messages.ERR_INDEX_RESOURCE_FAILED_2,
                        res.getRootPath(),
                        index.getName()), e);
                }
            }
        } else {
            // search document for the resource has not been found in cache, 
            // extract the information from the resource in a separate Thread

            CmsIndexingThread thread = new CmsIndexingThread(cms, writer, res, factory, index, m_report, this);

            try {
                m_fileCounter++;
                thread.start();
                thread.join(m_timeout);

                if (thread.isAlive()) {

                    if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().key(Messages.LOG_INDEXING_TIMEOUT_1, res.getRootPath()));
                    }
                    m_report.println();
                    m_report.print(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                        I_CmsReport.FORMAT_WARNING);
                    m_report.println(
                        Messages.get().container(Messages.RPT_SEARCH_INDEXING_TIMEOUT_1, res.getRootPath()),
                        I_CmsReport.FORMAT_WARNING);

                    m_abandonedCounter++;
                    thread.interrupt();
                }
            } catch (InterruptedException exc) {
                // noop
            }
        }
    }

    /**
     * Signals the thread manager that a thread has finished its job and will exit immediately.<p>
     */
    public synchronized void finished() {

        m_returnedCounter++;
    }

    /**
     * Gets the current thread (file) count.<p>
     * 
     * @return the current thread count
     */
    public int getCounter() {

        return m_fileCounter;
    }

    /**
     * Returns if the indexing manager still have indexing threads.<p>
     * 
     * @return true if the indexing manager still have indexing threads
     */
    public boolean isRunning() {

        return (m_returnedCounter + m_abandonedCounter < m_fileCounter);
    }

    /**
     * Writes statistical information to the report.<p>
     * 
     * The method reports the total number of threads started
     * (equals to the number of indexed files), the number of returned
     * threads (equals to the number of successfully indexed files),
     * and the number of abandoned threads (hanging threads reaching the timeout). 
     */
    public void reportStatistics() {

        CmsMessageContainer message = Messages.get().container(
            Messages.RPT_SEARCH_INDEXING_STATS_4,
            new Object[] {
                new Integer(m_fileCounter),
                new Integer(m_returnedCounter),
                new Integer(m_abandonedCounter),
                m_report.formatRuntime()});

        if (m_report != null) {
            m_report.println(message);
        }        
        if (LOG.isInfoEnabled()) {
            LOG.info(message.key());
        }
    }

    /**
     * Starts the thread manager to look for non-terminated threads<p>
     * The thread manager looks all 10 minutes if threads are not returned
     * and reports the number to the log file.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        int max = 20;

        try {
            // wait 30 seconds for the initial indexing
            Thread.sleep(30000);

            while (m_fileCounter > m_returnedCounter && max-- > 0) {

                Thread.sleep(30000);
                // wait 30 seconds before we start checking for "dead" index threads
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().key(
                        Messages.LOG_WAITING_ABANDONED_THREADS_2,
                        new Integer(m_abandonedCounter),
                        new Integer((m_fileCounter - m_returnedCounter))));
                }

            }
        } catch (Exception exc) {
            // noop
        }

        if (max > 0) {
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().key(Messages.LOG_THREADS_FINISHED_0));
            }
        } else {
            LOG.error(Messages.get().key(
                Messages.LOG_THREADS_FINISHED_0,
                new Integer(m_fileCounter - m_returnedCounter)));
        }
    }
}
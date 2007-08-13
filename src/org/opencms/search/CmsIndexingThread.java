/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsIndexingThread.java,v $
 * Date   : $Date: 2007/08/13 16:29:59 $
 * Version: $Revision: 1.28 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsDocumentFactory;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

/**
 * Implements the indexing method for a single resource as thread.<p>
 * 
 * The indexing of a single resource was wrapped into a single thread
 * in order to prevent the indexer from hanging.<p>
 *  
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.28 $ 
 * 
 * @since 6.0.0 
 */
public class CmsIndexingThread extends Thread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsIndexingThread.class);

    /** The cms object. */
    private CmsObject m_cms;

    /** The document type factory to index the resource with. */
    private I_CmsDocumentFactory m_documentType;

    /** The current index. */
    private CmsSearchIndex m_index;

    /** The current report. */
    private I_CmsReport m_report;

    /** The resource to index. */
    private CmsResource m_res;

    /** The index writer. */
    private IndexWriter m_writer;

    /**
     * Creates a new indexing thread for a single resource.<p>
     * 
     * @param cms the cms object
     * @param writer the writer
     * @param res the resource to index
     * @param documentType the document type factory to index the resource with
     * @param index the index
     * @param report the report to write out progress information
     */
    public CmsIndexingThread(
        CmsObject cms,
        IndexWriter writer,
        CmsResource res,
        I_CmsDocumentFactory documentType,
        CmsSearchIndex index,
        I_CmsReport report) {

        super("OpenCms: Indexing '" + res.getName() + "'");

        m_cms = cms;
        m_writer = writer;
        m_res = res;
        m_documentType = documentType;
        m_index = index;
        m_report = report;
    }

    /**
     * Starts the thread to index a single resource.<p>
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_INDEXING_WITH_FACTORY_2,
                m_res.getRootPath(),
                m_documentType.getName()));
        }

        // flag for logging in the "final" block
        boolean docOk = false;

        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_CREATING_INDEX_DOC_0));
            }
            Document doc = m_documentType.createDocument(m_cms, m_res, m_index);

            if (doc == null) {
                throw new CmsIndexException(Messages.get().container(Messages.ERR_CREATING_INDEX_DOC_0));
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_WRITING_INDEX_TO_WRITER_1,
                    String.valueOf(m_writer)));
            }

            if (!isInterrupted()) {
                // write the document to the index
                m_writer.addDocument(doc);
            }

            // indicate that the document was correctly handled
            docOk = true;

            if ((m_report != null) && !isInterrupted()) {
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_WRITE_SUCCESS_0));
                }
            }

            if (isInterrupted() && LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ABANDONED_THREAD_FINISHED_1, m_res.getRootPath()));
            }
        } catch (Exception exc) {
            // Ignore exception caused by empty documents, so that the report is not messed up with error message
            Throwable cause = exc.getCause();
            if (((cause != null) && (cause instanceof CmsIndexException) && ((CmsIndexException)cause).getMessageContainer().getKey().equals(
                org.opencms.search.documents.Messages.ERR_NO_CONTENT_1))
                || ((exc instanceof CmsIndexException) && ((CmsIndexException)exc).getMessageContainer().getKey().equals(
                    org.opencms.search.documents.Messages.ERR_NO_CONTENT_1))) {
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            } else {
                if (m_report != null) {
                    m_report.println(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_FAILED_0), I_CmsReport.FORMAT_ERROR);
                    m_report.println(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        exc.toString()), I_CmsReport.FORMAT_ERROR);
                }
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.ERR_INDEX_RESOURCE_FAILED_2,
                        m_res.getRootPath(),
                        m_index.getName()), exc);
                }
            }

            // set flag to avoid logging in finally block
            docOk = true;

        } finally {
            if (!docOk) {
                // apparently there was a Throwable that causes an issue
                if (m_report != null) {
                    m_report.println(org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_FAILED_0), I_CmsReport.FORMAT_ERROR);
                    m_report.println(Messages.get().container(
                        Messages.ERR_INDEX_RESOURCE_FAILED_2,
                        m_res.getRootPath(),
                        m_index.getName()), I_CmsReport.FORMAT_ERROR);
                }
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.ERR_INDEX_RESOURCE_FAILED_2,
                        m_res.getRootPath(),
                        m_index.getName()));
                }
            }
        }
    }
}
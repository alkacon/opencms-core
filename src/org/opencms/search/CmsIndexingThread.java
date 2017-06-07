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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.CmsIndexNoContentException;
import org.opencms.search.documents.I_CmsDocumentFactory;

import org.apache.commons.logging.Log;

/**
 * Implements the indexing method for a single resource as thread.<p>
 *
 * The indexing of a single resource is wrapped into a thread
 * in order to prevent the overall indexer from hanging.<p>
 *
 * @since 6.0.0
 */
public class CmsIndexingThread extends Thread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsIndexingThread.class);

    /** The cms object. */
    private CmsObject m_cms;

    /** The counter to output for the report. */
    private int m_count;

    /** The current index. */
    private CmsSearchIndex m_index;

    /** The current report. */
    private I_CmsReport m_report;

    /** The resource to index. */
    private CmsResource m_res;

    /** The result document. */
    private I_CmsSearchDocument m_result;

    /**
     * Create a new indexing thread.<p>
     *
     * @param cms the current OpenCms user context
     * @param res the resource to index
     * @param index the index to update the resource in
     * @param count the report count
     * @param report the report to write the output to
     */
    public CmsIndexingThread(CmsObject cms, CmsResource res, CmsSearchIndex index, int count, I_CmsReport report) {

        super("OpenCms: Indexing '" + res.getName() + "'");

        m_cms = cms;
        m_res = res;
        m_index = index;
        m_count = count;
        m_report = report;
        m_result = null;
    }

    /**
     * Returns the document created by this indexer thread.<p>
     *
     * In case the resource could not be indexed, <code>null</code> is returned.<p>
     *
     * @return the document created by this indexer thread
     */
    public I_CmsSearchDocument getResult() {

        return m_result;
    }

    /**
     * Starts the thread to index a single resource.<p>
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        // flag for logging in the "final" block
        boolean docOk = false;
        try {

            // create the index document
            m_result = createIndexDocument(m_cms, m_res, m_index, m_count, m_report);
            docOk = true;

            // check if the thread was interrupted
            if (isInterrupted() && LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_ABANDONED_THREAD_FINISHED_1, m_res.getRootPath()));
            }

        } catch (CmsIndexNoContentException e) {
            // Ignore exception caused by empty documents, so that the report is not messed up with error message
            m_report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);
        } catch (Throwable exc) {
            if (m_report != null) {
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                    I_CmsReport.FORMAT_ERROR);
                m_report.println(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        exc.toString()),
                    I_CmsReport.FORMAT_ERROR);
            }
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_INDEX_RESOURCE_FAILED_2,
                        m_res.getRootPath(),
                        m_index.getName()),
                    exc);
            }
            // set flag to avoid logging in finally block
            docOk = true;
        } finally {
            if (!docOk) {
                // apparently there was a Throwable that causes an issue
                if (m_report != null) {
                    m_report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                        I_CmsReport.FORMAT_ERROR);
                    m_report.println(
                        Messages.get().container(
                            Messages.ERR_INDEX_RESOURCE_FAILED_2,
                            m_res.getRootPath(),
                            m_index.getName()),
                        I_CmsReport.FORMAT_ERROR);
                }
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.ERR_INDEX_RESOURCE_FAILED_2,
                            m_res.getRootPath(),
                            m_index.getName()));
                }
            }
        }
    }

    /**
     * Creates the search index document.<p>
     *
     * @param cms the current OpenCms user context
     * @param res the resource to index
     * @param index the index to update the resource in
     * @param count the report count
     * @param report the report to write the output to
     *
     * @return the created search index document
     *
     * @throws CmsException in case of issues while creating the search index document
     */
    protected I_CmsSearchDocument createIndexDocument(
        CmsObject cms,
        CmsResource res,
        CmsSearchIndex index,
        int count,
        I_CmsReport report) throws CmsException {

        I_CmsSearchDocument result = null;

        if (report != null) {
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_1,
                    String.valueOf(count)),
                I_CmsReport.FORMAT_NOTE);
            report.print(Messages.get().container(Messages.RPT_SEARCH_INDEXING_FILE_BEGIN_0), I_CmsReport.FORMAT_NOTE);
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    report.removeSiteRoot(res.getRootPath())));
            report.print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
                I_CmsReport.FORMAT_DEFAULT);
        }

        // check if this resource should be excluded from the index, if so skip it
        boolean excludeFromIndex = index.excludeFromIndex(cms, res);

        if (!excludeFromIndex) {
            // resource is to be included in the index
            I_CmsDocumentFactory documentFactory = index.getDocumentFactory(res);
            if (documentFactory != null) {
                // some resources e.g. JSP do not have a default document factory
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_INDEXING_WITH_FACTORY_2,
                            res.getRootPath(),
                            documentFactory.getName()));
                }
                // create the document
                result = documentFactory.createDocument(cms, res, index);
            }
        }
        if (result == null) {
            // this resource is not contained in the given search index or locale did not match
            if (report != null) {
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SKIPPED_0),
                    I_CmsReport.FORMAT_NOTE);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_SKIPPED_1, res.getRootPath()));
            }
        } else {
            // index document was successfully created
            if ((m_report != null)) {
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            }
        }

        return result;
    }
}
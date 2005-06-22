/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsVfsIndexer.java,v $
 * Date   : $Date: 2005/06/22 10:38:15 $
 * Version: $Revision: 1.25 $
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
 */

package org.opencms.search;

import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsDocumentFactory;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

/**
 * Implementation for an indexer indexing VFS Cms resources.<p>
 * 
 * @version $Revision: 1.25 $ $Date: 2005/06/22 10:38:15 $
 * @author Carsten Weinholz 
 * @author Thomas Weckert  
 * @since 5.3.1
 */
public class CmsVfsIndexer implements I_CmsIndexer {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsIndexer.class);  
    
    /** The index. */
    private CmsSearchIndex m_index;

    /** The report. */
    private I_CmsReport m_report;

    /** The thread manager. */
    private CmsIndexingThreadManager m_threadManager;

    /** The writer. */
    private IndexWriter m_writer;

    /**
     * @see org.opencms.search.I_CmsIndexer#getIndexResource(org.opencms.file.CmsObject, org.apache.lucene.document.Document)
     */
    public A_CmsIndexResource getIndexResource(CmsObject cms, Document doc) throws CmsException {

        Field f;
        A_CmsIndexResource result = null;

        if ((f = doc.getField(I_CmsDocumentFactory.DOC_PATH)) != null) {

            String path = cms.getRequestContext().removeSiteRoot(f.stringValue());
            CmsResource resource = cms.readResource(path);
            // an exception would have been thrown if the user has no read persmissions
            result = new CmsVfsIndexResource(resource);
        }

        return result;
    }

    /**
     * @see org.opencms.search.I_CmsIndexer#init(org.opencms.report.I_CmsReport, org.opencms.search.CmsSearchIndex, org.opencms.search.CmsSearchIndexSource, org.apache.lucene.index.IndexWriter, org.opencms.search.CmsIndexingThreadManager)
     */
    public void init(
        I_CmsReport report,
        CmsSearchIndex index,
        CmsSearchIndexSource indexSource,
        IndexWriter writer,
        CmsIndexingThreadManager threadManager) {

        m_writer = writer;
        m_index = index;
        m_report = report;
        m_threadManager = threadManager;
    }

    /**
     * @see org.opencms.search.I_CmsIndexer#updateIndex(CmsObject, java.lang.String, java.lang.String)
     */
    public void updateIndex(CmsObject cms, String source, String path) throws CmsIndexException {

        boolean folderReported = false;
        List resources = null;
        CmsResource res = null;

        try {
            if (CmsResource.isFolder(path)) {
                resources = cms.getResourcesInFolder(path, CmsResourceFilter.DEFAULT);
            } else {
                resources = Collections.EMPTY_LIST;
            }

            for (int i = 0; i < resources.size(); i++) {

                res = (CmsResource)resources.get(i);

                if (!res.isInternal()) {
                    // we only have to index those resources that are not marked as internal
                    if (res instanceof CmsFolder) {
                        updateIndex(cms, source, cms.getRequestContext().removeSiteRoot(res.getRootPath()));
                        continue;
                    }

                    if (m_report != null && !folderReported) {
                        m_report.print(Messages.get().container(Messages.RPT_SEARCH_INDEXING_FOLDER_0), I_CmsReport.C_FORMAT_NOTE);
                        m_report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, path));
                        folderReported = true;
                    }

                    if (m_report != null) {
                        m_report.print(
                            org.opencms.report.Messages.get().container(
                                org.opencms.report.Messages.RPT_SUCCESSION_1,
                                String.valueOf(m_threadManager.getCounter() + 1)),
                            I_CmsReport.C_FORMAT_NOTE);
                        m_report.print(Messages.get().container(
                            Messages.RPT_SEARCH_INDEXING_FILE_BEGIN_0), I_CmsReport.C_FORMAT_NOTE);
                        m_report.print(org.opencms.report.Messages.get().container(
                            org.opencms.report.Messages.RPT_ARGUMENT_1, res.getName()));
                        m_report.print(org.opencms.report.Messages.get().container(
                            org.opencms.report.Messages.RPT_DOTS_0), I_CmsReport.C_FORMAT_DEFAULT);
                    }

                    A_CmsIndexResource ires = new CmsVfsIndexResource(res);
                    m_threadManager.createIndexingThread(cms, m_writer, ires, m_index);
                }
            }

        } catch (CmsIndexException exc) {

            if (m_report != null) {
                m_report.println();
                m_report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_FAILED_0), I_CmsReport.C_FORMAT_WARNING);
                m_report.println(exc);
            }
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_INDEXING_PATH_FAILED_1, path), exc);
            } 

        } catch (CmsException exc) {

            if (m_report != null) {
                m_report.println(Messages.get().container(
                    Messages.RPT_SEARCH_INDEXING_FOLDER_FAILED_2,
                    path,
                    exc.getLocalizedMessage()), I_CmsReport.C_FORMAT_WARNING);
            }
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_INDEXING_PATH_FAILED_1, path), exc);
            } 

        } catch (Exception exc) {

            if (m_report != null) {
                m_report.println(
                    Messages.get().container(Messages.RPT_SEARCH_INDEXING_FOLDER_0),
                    I_CmsReport.C_FORMAT_WARNING);
            }
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_INDEXING_PATH_FAILED_1, path), exc);
            } 

            throw new CmsIndexException(Messages.get().container(Messages.LOG_INDEXING_FAILED_1));
            
        }
    }

}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsVfsIndexer.java,v $
 * Date   : $Date: 2004/07/07 14:12:30 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsDocumentFactory;

import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

/**
 * Implementation for an indexer indexing VFS Cms resources.<p>
 * 
 * @version $Revision: 1.13 $ $Date: 2004/07/07 14:12:30 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.1
 */
public class CmsVfsIndexer implements I_CmsIndexer {

    /** The writer. */
    private IndexWriter m_writer;

    /** The index. */
    private CmsSearchIndex m_index;

    /** The report. */
    private I_CmsReport m_report;

    /** The thread manager. */
    private CmsIndexingThreadManager m_threadManager;

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
     * @see org.opencms.search.I_CmsIndexer#updateIndex(CmsObject, java.lang.String)
     */
    public void updateIndex(CmsObject cms, String path) throws CmsIndexException {

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
                if (res instanceof CmsFolder) {
                    updateIndex(cms, cms.getRequestContext().removeSiteRoot(res.getRootPath()));
                    continue;
                }

                if (m_report != null && !folderReported) {
                    m_report.print(m_report.key("search.indexing_folder"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.println(path, I_CmsReport.C_FORMAT_DEFAULT);
                    folderReported = true;
                }

                if (m_report != null) {
                    m_report.print("( " + (m_threadManager.getCounter() + 1) + " ) ", I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("search.indexing_file_begin"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(res.getName(), I_CmsReport.C_FORMAT_DEFAULT);
                    m_report.print(m_report.key("search.dots"), I_CmsReport.C_FORMAT_DEFAULT);
                }

                A_CmsIndexResource ires = new CmsVfsIndexResource(res);
                m_threadManager.createIndexingThread(m_writer, ires, m_index);
            }

        } catch (CmsIndexException exc) {

            if (m_report != null) {
                m_report.println();
                m_report.println(
                    m_report.key("search.indexing_file_failed") + " : " + exc.getMessage(),
                    I_CmsReport.C_FORMAT_WARNING);
            }
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Failed to index " + path, exc);
            }

        } catch (CmsException exc) {

            if (m_report != null) {
                m_report.println(m_report.key("search.indexing_folder")
                    + path
                    + m_report.key("search.indexing_folder_failed")
                    + " : "
                    + exc.getMessage(), I_CmsReport.C_FORMAT_WARNING);
            }
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Failed to index " + path, exc);
            }

        } catch (Exception exc) {

            if (m_report != null) {
                m_report.println(m_report.key("search.indexing_folder_failed"), I_CmsReport.C_FORMAT_WARNING);
            }
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Failed to index " + path, exc);
            }

            throw new CmsIndexException("Indexing contents of " + path + " failed.", exc);
        }
    }

    /**
     * @see org.opencms.search.I_CmsIndexer#getIndexResource(org.opencms.file.CmsObject, org.apache.lucene.document.Document)
     */
    public A_CmsIndexResource getIndexResource(CmsObject cms, Document doc) throws CmsException {

        Field f = null;
        String path = null;
        CmsResource resource = null;
        A_CmsIndexResource result = null;

        if ((f = doc.getField(I_CmsDocumentFactory.DOC_PATH)) != null) {

            path = cms.getRequestContext().removeSiteRoot(f.stringValue());
            resource = cms.readResource(path);

            if (cms.hasPermissions(resource, I_CmsConstants.C_READ_ACCESS)) {

                result = new CmsVfsIndexResource(resource);
            }
        }

        return result;
    }

}
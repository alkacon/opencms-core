/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsVfsIndexer.java,v $
 * Date   : $Date: 2004/06/06 10:44:58 $
 * Version: $Revision: 1.7 $
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
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;

import java.util.List;

import org.apache.lucene.index.IndexWriter;

/**
 * Implements the indexing of vfs data.<p>
 * 
 * @version $Revision: 1.7 $ $Date: 2004/06/06 10:44:58 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.3.1
 */
public class CmsVfsIndexer implements I_CmsIndexer {

    /** The cms object */
    private CmsObject m_cms;
        
    /** the writer */
    private IndexWriter m_writer;
    
    /** the index */
    private CmsSearchIndex m_index;
    
    /** the report */
    private I_CmsReport m_report;
    
    /** the thread manager */
    private CmsIndexingThreadManager m_threadManager;
    
    /**
     * Creates a new vfs indexer.<p>
     */
    public CmsVfsIndexer () {
        //noop
    }

    /**
     * Initializes the indexer.<p>
     * 
     * @param cms the cms object
     * @param className not used here
     * @param writer writer to write the index
     * @param index the index
     * @param report the report
     * @param threadManager the tread manager
     */
    public void init(CmsObject cms, String className, IndexWriter writer, CmsSearchIndex index, I_CmsReport report, CmsIndexingThreadManager threadManager) {
        m_cms = cms;
        m_writer = writer;
        m_index = index;
        m_report = report;
        m_threadManager = threadManager;        
    }
    
    /**
     * Creates new index entries for all vfs resources below the given path.<p>
     * 
     * @param path the path to the root of the subtree to index
     * @throws CmsIndexException if something goes wrong
     */
    public void updateIndex(String path) throws CmsIndexException {
        
        boolean folderReported = false;
        
        try {
            List resources = m_cms.getResourcesInFolder(path, CmsResourceFilter.DEFAULT);
            CmsResource res;
            
            // process resources
            for (int i = 0; i < resources.size(); i++) {
                
                res = (CmsResource)resources.get(i);
                if (res instanceof CmsFolder) {                    
                    updateIndex(m_cms.getRequestContext().removeSiteRoot(res.getRootPath()));
                    continue;
                } 

                if (m_report != null && !folderReported) {
                    m_report.print(m_report.key("search.indexing_folder"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.println(path, I_CmsReport.C_FORMAT_DEFAULT);
                    folderReported = true;
                }

                if (m_report != null) {
                    m_report.print("( " + (m_threadManager.getCounter()+1) + " ) ", I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("search.indexing_file_begin"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(res.getName(), I_CmsReport.C_FORMAT_DEFAULT);
                    m_report.print(m_report.key("search.dots"), I_CmsReport.C_FORMAT_DEFAULT);
                }
                
                CmsIndexResource ires = new CmsVfsIndexResource(res); 
                m_threadManager.createIndexingThread(m_writer, ires, m_index);
            }
            
        } catch (CmsIndexException exc) {
            
            if (m_report != null) {
                m_report.println();
                m_report.println(m_report.key("search.indexing_file_failed") + " : " + exc.getMessage(), 
                        I_CmsReport.C_FORMAT_WARNING);
            }
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Failed to index " + path, exc);
            }

        } catch (CmsException exc) {
            
            if (m_report != null) {
                m_report.println(m_report.key("search.indexing_folder") + path + m_report.key("search.indexing_folder_failed") + " : " + exc.getMessage(), 
                        I_CmsReport.C_FORMAT_WARNING);
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
}

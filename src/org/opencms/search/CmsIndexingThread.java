/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsIndexingThread.java,v $
 * Date   : $Date: 2004/02/20 19:50:58 $
 * Version: $Revision: 1.5 $
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


import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsDocumentFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

/**
 * Implements the indexing method for a single resource as thread.<p>
 * 
 * The indexing of a single resource was wrapped into a single thread
 * in order to prevent the indexer from hanging.
 *  
 * @version $Revision: 1.5 $ $Date: 2004/02/20 19:50:58 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.3.1
 */
public class CmsIndexingThread extends Thread {

    /** Internal debug flag */
    private static boolean DEBUG = false;
    
    /** the search manager */
    CmsSearchManager m_manager;
    
    /** the index writer */
    IndexWriter m_writer; 
    
    /** the resource to index */
    CmsIndexResource m_res; 
    
    /** the  current index */
    CmsSearchIndex m_index;
    
    /** the current report */
    I_CmsReport m_report;
    
    /** the thread manager */
    CmsIndexingThreadManager m_threadManager;
    
    /**
     * Creates a new indexing thread for a single resource<p>
     * 
     * @param manager the index manager
     * @param writer the writer
     * @param res the resource to index
     * @param index the index
     * @param report the report to write out progress information
     * @param threadManager the thread manager
     */
    public CmsIndexingThread(CmsSearchManager manager, IndexWriter writer, CmsIndexResource res, CmsSearchIndex index, I_CmsReport report, CmsIndexingThreadManager threadManager) {
    
        super("OpenCms: Indexing '" + res.getName() +"'");
        
        m_manager = manager;
        m_writer = writer;
        m_res = res;
        m_index = index;
        m_report = report;
        m_threadManager = threadManager;        
    }
    
    /**
     * Starts the thread to index a single resource.<p>
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
  
        I_CmsDocumentFactory documentFactory = m_manager.getDocumentFactory(m_res);
        if (documentFactory != null && !m_index.getDocumenttypes(m_res.getRootPath()).contains(documentFactory.getName())) {
            documentFactory = null;
        }
                                        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Indexing " + m_res.getRootPath());
        }
                    
        if (documentFactory != null) {
            try {

                if (DEBUG && OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Creating lucene index document");
                }
                                                    
                Document doc = documentFactory.newInstance(m_res, m_index.getLanguage());
                if (doc == null) {
                    throw new CmsIndexException("Creating index document failed");
                }
                
                if (DEBUG && OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Writing document to index, writer = " 
                        + ((m_writer != null) ? m_writer.toString() : "null"));
                }
                
                if (!isInterrupted()) {       
                    m_writer.addDocument(doc);
                }
                
                if (m_report != null && !isInterrupted()) {
                    m_report.println(m_report.key("search.indexing_file_end"), I_CmsReport.C_FORMAT_OK);
                    if (DEBUG && OpenCms.getLog(this).isDebugEnabled()) {
                        OpenCms.getLog(this).debug("Document successfully written to index");
                    }
                }
                    
                if (isInterrupted() && OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("Abandoned thread for indexing " + m_res.getRootPath() + " finished");
                }
                                                                     
            } catch (Exception exc) {
                        
                if (m_report != null) {
                    m_report.println();
                    m_report.println(m_report.key("search.indexing_file_failed") + " : " + exc.getMessage(), 
                        I_CmsReport.C_FORMAT_WARNING);
                }    
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("Failed to index " + m_res.getRootPath(), exc);
                }
            }
        } else {
    
            if (m_report != null) {
                m_report.println(m_report.key("search.indexing_file_skipped"), I_CmsReport.C_FORMAT_NOTE);
            }
                        
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Skipped " + m_res.getRootPath() + ", no matching documenttype");
            }
        }           
        
        m_threadManager.finished();
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/Attic/CmsCosIndexer.java,v $
 * Date   : $Date: 2004/02/13 13:41:45 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.CmsException;
import org.opencms.report.I_CmsReport;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.util.CmsUUID;

import com.opencms.defaults.master.CmsMasterContent;
import com.opencms.defaults.master.CmsMasterDataSet;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import java.util.Iterator;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

/**
 * Package to implement indexing of cos data.<p>
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/13 13:41:45 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsCosIndexer extends CmsMasterContent {

    private CmsObject m_cms;
    
    private IndexWriter m_writer;
    
    private CmsSearchIndex m_index;
    
    private I_CmsReport m_report;
    
    private CmsIndexingThreadManager m_threadManager;
    
    private int m_subId;
    
    private CmsMasterContent m_contentDefinition;
    
    /**
     * Creates a new cos indexer.<p>
     * 
     * @param cms the cms object
     * @param cdClassName name of the content definition class
     * @param writer wariter to write the index
     * @param index the index
     * @param report the report
     * @param threadManager the tread manager
     * @throws CmsIndexException if something goes wrong
     */
    public CmsCosIndexer (CmsObject cms, String cdClassName, IndexWriter writer, CmsSearchIndex index, I_CmsReport report, CmsIndexingThreadManager threadManager) throws CmsIndexException {
    
        try {
            m_cms = cms;
            m_writer = writer;
            m_index = index;
            m_report = report;
            m_threadManager = threadManager;
            
            m_contentDefinition = (CmsMasterContent)Class.forName(cdClassName).newInstance();
            m_subId = m_contentDefinition.getSubId();

        } catch (Exception exc) {
            throw new CmsIndexException("Indexing contents of class" + cdClassName + " failed.", exc);
        }
    }
    
    /**
     * Just to fulfill implementation requirements of CmsMasterContent
     * @see com.opencms.defaults.master.CmsMasterContent#getSubId()
     */
    public int getSubId() {
        return 0;
    }
    
    /**
     * Creates new index entries for all cos resources below the given path.<p>
     * 
     * @param channel the channel to index
     * @throws CmsIndexException if something goes wrong
     */
    public void updateIndex(String channel) throws CmsIndexException {
        
        boolean channelReported = false;
        
        try {
            String channelId = getChannelId(channel).toString();
            
            Vector resources = readAllByChannel(m_cms, channelId, m_subId);
            
            for (Iterator i = resources.iterator(); i.hasNext();) {
                
                CmsMasterDataSet ds = (CmsMasterDataSet)i.next();
                
                if (m_report != null && !channelReported) {
                    m_report.print(m_report.key("search.indexing_folder"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.println(channel, I_CmsReport.C_FORMAT_DEFAULT);
                    channelReported = true;
                }
                
                if (m_report != null) {
                    m_report.print("( " + (m_threadManager.getCounter()+1) + " )", I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("search.indexing_file_begin"), I_CmsReport.C_FORMAT_NOTE);
                    if (ds.m_title != null) {
                        m_report.print(ds.m_title, I_CmsReport.C_FORMAT_DEFAULT);
                    }
                    m_report.print(m_report.key("search.dots"), I_CmsReport.C_FORMAT_DEFAULT);
                }
                
                CmsIndexResource ires = new CmsIndexResource(ds, "path", channel, m_contentDefinition.getClass().getName());
                m_threadManager.createIndexingThread(m_writer, ires, m_index);
            }
        } catch (Exception exc) {
            throw new CmsIndexException("Indexing contents of " + channel + " failed.", exc);
        }
    }
    
    /**
     * Returns the uuid of the channel.<p>
     * 
     * @param channelName name of the channel 
     * @return the uuid of the channel
     * @throws CmsIndexException if something goes wrong
     */
    private CmsUUID getChannelId(String channelName) throws CmsIndexException {

        String siteRoot = m_cms.getRequestContext().getSiteRoot();
        m_cms.setContextToCos();        
        CmsUUID id = null;
        try {         
            CmsResource channel = m_cms.readFolder(channelName);
            id = channel.getResourceId();
        } catch (Exception exc) {
            throw new CmsIndexException("Can't access channel " + channelName, exc);
        } finally {
            m_cms.getRequestContext().setSiteRoot(siteRoot);
        }
        return id;
    }
    
    /**
     * Reads the data of a cos resource specified by the given search document.<p>
     * 
     * @param cms the cms object
     * @param doc the document retrived from search index
     * @return the cos data
     * @throws CmsException if something goes wrong
     */
    public static CmsIndexResource readResource(CmsObject cms, Document doc) throws CmsException {
        
        try {
            String channel   = doc.getField(I_CmsDocumentFactory.DOC_CHANNEL).stringValue();
            String path      = doc.getField(I_CmsDocumentFactory.DOC_PATH).stringValue();
            String cdClass   = doc.getField(I_CmsDocumentFactory.DOC_CONTENT_DEFINITION).stringValue();
            String contentId = doc.getField(I_CmsDocumentFactory.DOC_CONTENT_ID).stringValue();
            
            Class clazz = Class.forName(cdClass);
            CmsMasterContent contentDefinition = (CmsMasterContent)clazz.getDeclaredConstructor(
                    new Class[] {org.opencms.file.CmsObject.class}).newInstance(new Object[] {cms});

            CmsMasterDataSet ds = new CmsMasterDataSet();
            CmsMasterContent.getDbAccessObject(contentDefinition.getSubId()).read(cms, contentDefinition, ds, new CmsUUID(contentId));
            
            if (ds != null) {
                return new CmsIndexResource(ds, path, channel, cdClass);
            }
            
            return null;
            
        } catch (Exception exc) {
            throw new CmsException ("Instanciation of index resource failed", exc);
        }
    }
}

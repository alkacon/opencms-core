/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/legacy/Attic/CmsCosIndexer.java,v $
 * Date   : $Date: 2004/02/20 13:35:45 $
 * Version: $Revision: 1.1 $
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
package com.opencms.legacy;

import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsIndexResource;
import org.opencms.search.CmsIndexingThreadManager;
import org.opencms.search.CmsSearchIndex;
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
 * Implements the indexing of cos data.<p>
 * 
 * @version $Revision: 1.1 $ $Date: 2004/02/20 13:35:45 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.3.1
 */
public class CmsCosIndexer extends CmsMasterContent {
    
    /** cms object */
    private CmsObject m_cms;
    
    /** index writer */
    private IndexWriter m_writer;
    
    /** current index */
    private CmsSearchIndex m_index;
    
    /** report */
    private I_CmsReport m_report;
    
    /** the thrad manager */
    private CmsIndexingThreadManager m_threadManager;
    
    /** id for identifying module content */
    private int m_subId;
    
    /** general class to handle module content */
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
        updateIndex (channel, channel);
    }
    
    /**
     * Creates new index entries for all cos resources below the given path.<p>
     * 
     * @param channel the channel to index
     * @param root the root channel
     * @throws CmsIndexException if something goes wrong
     */    
    public void updateIndex(String channel, String root) throws CmsIndexException {
        
        boolean channelReported = false;
        
        try {
            String channelId = getChannelId(channel).toString();
            Vector subChannels = CmsMasterContent.getAllSubChannelsOf(m_cms, channel);
            
            // index subchannels
            for (int i = 0; i < subChannels.size(); i++) {
                
                String subChannel = (String)subChannels.get(i);                   
                updateIndex(subChannel, root);
            } 
                
            // now index channel
            Vector resources = readAllByChannel(m_cms, channelId, m_subId);
            for (Iterator i = resources.iterator(); i.hasNext();) {
                
                CmsMasterDataSet ds = (CmsMasterDataSet)i.next();
                
                if (m_report != null && !channelReported) {
                    m_report.print(m_report.key("search.indexing_channel"), I_CmsReport.C_FORMAT_NOTE);
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
                
                String path = m_index.getChannelDisplayUri(root)
                    + "?" + m_index.getChannelDisplayparam(root)
                    + "=" + ds.m_masterId;
                
                CmsIndexResource ires = new CmsCosIndexResource(ds, path, channel, m_contentDefinition.getClass().getName());
                m_threadManager.createIndexingThread(m_writer, ires, m_index);
            }
        } catch (Exception exc) {
            
            if (m_report != null) {
                m_report.println(m_report.key("search.indexing_folder_failed"), I_CmsReport.C_FORMAT_WARNING);
            }
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Failed to index " + channel, exc);
            }
            
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
                return new CmsCosIndexResource(ds, path, channel, cdClass);
            }
            
            return null;
            
        } catch (Exception exc) {
            throw new CmsException ("Instanciation of index resource failed", exc);
        }
    }
}

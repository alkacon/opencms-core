/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/legacy/Attic/CmsCosIndexer.java,v $
 * Date   : $Date: 2004/07/05 15:37:34 $
 * Version: $Revision: 1.11 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.search.A_CmsIndexResource;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsIndexingThreadManager;
import org.opencms.search.CmsSearchDocumentType;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.I_CmsIndexer;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.util.CmsStringSubstitution;
import org.opencms.util.CmsUUID;

import com.opencms.defaults.master.CmsMasterContent;
import com.opencms.defaults.master.CmsMasterDataSet;

import java.util.Iterator;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

/**
 * Implements the indexing of cos data.<p>
 * 
 * @version $Revision: 1.11 $ $Date: 2004/07/05 15:37:34 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.1
 */
public class CmsCosIndexer extends CmsMasterContent implements I_CmsIndexer {
    
    /** Constant for display uri. */
    public static final String C_PARAM_CHANNEL_DISPLAY_URI = "displayuri";
        
    /** Constant for display parameter. */
    public static final String C_PARAM_CHANNEL_DISPLAY_PARAM = "displayparam";
    
    /** The index writer. */
    private IndexWriter m_writer;
    
    /** The current index. */
    private CmsSearchIndex m_index;
    
    /** The search index source. */
    private CmsSearchIndexSource m_indexSource;
    
    /** The report. */
    private I_CmsReport m_report;
    
    /** The thread manager. */
    private CmsIndexingThreadManager m_threadManager;
    
    /** The id for identifying module content. */
    private int m_subId;
    
    /** General class to handle module content. */
    private CmsMasterContent m_contentDefinition;

    /**
     * @see org.opencms.search.I_CmsIndexer#init(org.opencms.report.I_CmsReport, org.opencms.search.CmsSearchIndex, CmsSearchIndexSource, org.apache.lucene.index.IndexWriter, org.opencms.search.CmsIndexingThreadManager)
     */
    public void init(I_CmsReport report, CmsSearchIndex index, CmsSearchIndexSource indexSource, IndexWriter writer, CmsIndexingThreadManager threadManager) throws CmsIndexException {
        
        String cdClassName = null;
        String resourceType = null;
        CmsSearchDocumentType documentType = null;
        
        try {
            m_writer = writer;
            m_index = index;
            m_indexSource = indexSource;
            m_report = report;
            m_threadManager = threadManager;
            
            resourceType = (String)indexSource.getDocumentTypes().get(0);
            documentType = OpenCms.getSearchManager().getDocumentTypeConfig(resourceType);            
            cdClassName = (String)documentType.getResourceTypes().get(0);
            
            m_contentDefinition = (CmsMasterContent)Class.forName(cdClassName).newInstance();
            m_subId = m_contentDefinition.getSubId();

        } catch (Exception exc) {
            throw new CmsIndexException("Indexing contents of class" + cdClassName + " failed.", exc);
        }        
    }
    
    /**
     * Just to fulfill implementation requirements of CmsMasterContent.<p>
     * 
     * @see com.opencms.defaults.master.CmsMasterContent#getSubId()
     */
    public int getSubId() {
        return 0;
    }
    
    /**
     * @see org.opencms.search.I_CmsIndexer#updateIndex(org.opencms.file.CmsObject, java.lang.String)
     */
    public void updateIndex(CmsObject cms, String channel) throws CmsIndexException {
        updateIndex (cms, channel, channel);
    }

    /**
     * Creates new index entries for all cos resources below the given path.<p>
     * 
     * @param cms the current user's CmsObject
     * @param channel the channel to index
     * @param root the root channel
     * @throws CmsIndexException if something goes wrong
     */    
    protected void updateIndex(CmsObject cms, String channel, String root) throws CmsIndexException {
        
        boolean channelReported = false;
        CmsProject currentProject = null;
        CmsRequestContext context = cms.getRequestContext();         
        
        try {
            // save the current project
            currentProject = context.currentProject();
            // switch to the configured project
            context.setCurrentProject(cms.readProject(m_index.getProject()));
            
            String channelId = getChannelId(cms, channel).toString();
            Vector subChannels = CmsMasterContent.getAllSubChannelsOf(cms, channel);
            
            // index subchannels
            for (int i = 0; i < subChannels.size(); i++) {
                
                String subChannel = (String)subChannels.get(i);                   
                updateIndex(cms, subChannel, root);
            } 
                
            // now index channel
            Vector resources = readAllByChannel(cms, channelId, m_subId);
            for (Iterator i = resources.iterator(); i.hasNext();) {
                
                CmsMasterDataSet ds = (CmsMasterDataSet)i.next();
                
                if (m_report != null && !channelReported) {
                    m_report.print(m_report.key("search.indexing_channel"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.println(channel, I_CmsReport.C_FORMAT_DEFAULT);
                    channelReported = true;
                }
                
                if (m_report != null) {
                    m_report.print("( " + (m_threadManager.getCounter()+1) + " ) ", I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("search.indexing_file_begin"), I_CmsReport.C_FORMAT_NOTE);
                    if (ds.m_title != null) {
                        String title = ds.m_title;
                        title = CmsStringSubstitution.substitute(title, "'", "\\'");
                        title = CmsStringSubstitution.substitute(title, "\"", "\\\"");
                        m_report.print(title, I_CmsReport.C_FORMAT_DEFAULT);
                    }
                    m_report.print(m_report.key("search.dots"), I_CmsReport.C_FORMAT_DEFAULT);
                }

                String path = m_indexSource.getParam(C_PARAM_CHANNEL_DISPLAY_URI)
                    + "?" + m_indexSource.getParam(C_PARAM_CHANNEL_DISPLAY_PARAM)
                    + "=" + ds.m_masterId;
                
                A_CmsIndexResource ires = new CmsCosIndexResource(ds, path, channel, m_contentDefinition.getClass().getName());
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
        } finally {
            
            // switch back to the original project
            context.setCurrentProject(currentProject);             
        }
    }
    
    /**
     * Returns the uuid of the channel.<p>
     * 
     * @param cms the current user's CmsObject
     * @param channelName name of the channel 
     * @return the uuid of the channel
     * @throws CmsIndexException if something goes wrong
     */
    protected CmsUUID getChannelId(CmsObject cms, String channelName) throws CmsIndexException {

        String siteRoot = cms.getRequestContext().getSiteRoot();
        cms.getRequestContext().setSiteRoot(I_CmsConstants.VFS_FOLDER_COS);        
        CmsUUID id = null;
        try {         
            CmsResource channel = cms.readFolder(channelName, CmsResourceFilter.IGNORE_EXPIRATION);
            id = channel.getResourceId();
        } catch (Exception exc) {
            throw new CmsIndexException("Can't access channel " + channelName, exc);
        } finally {
            cms.getRequestContext().setSiteRoot(siteRoot);
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
    public A_CmsIndexResource readResource(CmsObject cms, Document doc) throws CmsException {
        
        try {
            String channel   = doc.getField(I_CmsCosDocumentFactory.DOC_CHANNEL).stringValue();
            String path      = doc.getField(I_CmsDocumentFactory.DOC_PATH).stringValue();
            String cdClass   = doc.getField(I_CmsCosDocumentFactory.DOC_CONTENT_DEFINITION).stringValue();
            String contentId = doc.getField(I_CmsCosDocumentFactory.DOC_CONTENT_ID).stringValue();
            
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
    
    /**
     * @see org.opencms.search.I_CmsIndexer#getIndexResource(CmsObject, String, org.apache.lucene.document.Document)
     */
    public A_CmsIndexResource getIndexResource(CmsObject cms, String searchRoot, Document doc) throws CmsException {

        Field f = null;
        String channel = null;
        A_CmsIndexResource result = null;
        
        if ((f = doc.getField(I_CmsCosDocumentFactory.DOC_CHANNEL)) != null) {
            channel = f.stringValue();
            
            if (channel != null) {
                result = readResource(cms, doc);
            }
        }
        
        return result;
    }    
    
}

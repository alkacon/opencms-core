/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchResult.java,v $
 * Date   : $Date: 2004/02/13 11:27:46 $
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

import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.monitor.I_CmsMemoryMonitorable;
import org.opencms.search.documents.I_CmsDocumentFactory;

import com.opencms.core.CmsException;

import java.util.Date;

import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * @version $Revision: 1.2 $ $Date: 2004/02/13 11:27:46 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsSearchResult implements I_CmsMemoryMonitorable {
    
    /*
     * The document found
     */
    private Document m_document;

    /*
     * The index  
     */
    private CmsSearchIndex m_index;
      
    /*
     * The query
     */
    private String m_query;
    
    /* 
     * The resource found
     */
    private CmsIndexResource m_resource;
    
    /*
     * The score of this search result
     */
    private int m_score;
    
    /**
     * Constructor to create a new, single search result entry.<p>
     * 
     * @param index the search index
     * @param query the query
     * @param res the resource found
     * @param doc the document found
     * @param score the search score
     */
    protected CmsSearchResult (CmsSearchIndex index, String query, CmsIndexResource res, Document doc, int score) {
        
        m_index = index;
        m_query = query;
        m_resource = res;
        m_document = doc;
        m_score = score;
    }
    
    /**
     * Gets the description.<p>
     * 
     * @return the description
     */
    public String getDescription() {

        Field f =  m_document.getField(I_CmsDocumentFactory.DOC_DESCRIPTION);
        if (f != null) {
            return f.stringValue();
        }
        
        return null;
    }
    
    /**
     * Gets the excerpt.<p>
     * 
     * @return the excerpt
     */
    public String getExcerpt() {

        try {
            return m_index.getExcerpt(this);      
        } catch (Exception exc) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                String message = "Could not generate excerpt for query ["+m_query+"], and resource "+m_resource+":";
                OpenCms.getLog(this).error(message, exc); 
            }
        }
        
        return null;
    }
    
    /**
     * Gets the index.<p>
     * 
     * @return the index
     */
    public CmsSearchIndex getIndex() {
        
        return m_index;
    }
    
    /**
     * Gets the keywords.<p>
     * 
     * @return the keywords
     */
    public String getKeywords() {

        Field f = m_document.getField(I_CmsDocumentFactory.DOC_KEYWORDS);
        if (f != null) {
            return f.stringValue();
        }
        
        return null;
    }

    /**
     * Get the query.<p>
     * 
     * @return the query
     */
    public String getQuery() {
        
        return m_query;
    }
    
    /**
     * Get the content.<p>
     * 
     * @return the content
     */
    public String getRawContent() {
        
        Field f =  m_document.getField(I_CmsDocumentFactory.DOC_CONTENT);
        String rawContent = null;
        
        if (f.isStored()) {
            rawContent = f.stringValue();
        } else {
            try {
                rawContent = m_index.getIndexManager().getDocumentFactory(m_resource).getRawContent(m_resource, m_index.getLanguage());
            } catch (CmsException exc) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Could not generate raw content", exc);
                }
            }
        }
        
        return rawContent;
    }
    
    /**
     * Gets the resource.<p>
     * 
     * @return the CmsResource
     */
    public Object getResource() {

        return m_resource;
    }
    
    /**
     * Gets the score .<p>
     * 
     * @return the score
     */
    public int getScore() {
 
        return m_score;
    }
       
    /**
     * Gets the title.<p>
     * 
     * @return the title
     */
    public String getTitle() {        

        Field f = m_document.getField(I_CmsDocumentFactory.DOC_TITLE);
        if (f != null) {
            return f.stringValue();
        }
        
        return null;
    }

    /**
     * Gets the last modification date.<p>
     * 
     * @return the last modification date
     */
    public Date getDateLastModified() {
        
        Field f = m_document.getField(I_CmsDocumentFactory.DOC_DATE_LASTMODIFIED);
        if (f != null) {
            return DateField.stringToDate(f.stringValue());
        }
        
        return null;
    }
    
    /**
     * Get the access path.<p>
     * 
     * @return the access path
     */
    public String getPath() {
        
        Field f = m_document.getField(I_CmsDocumentFactory.DOC_PATH);
        if (f != null) {
            return f.stringValue();
        }
        
        return null;
    }
    
    /**
     * @see org.opencms.monitor.I_CmsMemoryMonitorable#getMemorySize()
     */
    public int getMemorySize() {
        int result = 8;
        if (m_resource != null) {
            result += CmsMemoryMonitor.getMemorySize(m_resource);
        }
        if (m_query != null) {
            result += CmsMemoryMonitor.getMemorySize(m_query);
        }
        if (m_document != null) {
            result += 1024 * 10; // 10 kb average size 
        }
        if (m_index != null) {
            result += 1024;
        }
        return result;
    }
}
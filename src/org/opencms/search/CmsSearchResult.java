/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchResult.java,v $
 * Date   : $Date: 2008/08/06 10:47:20 $
 * Version: $Revision: 1.26 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.monitor.I_CmsMemoryMonitorable;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsStringUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Contains the data of a single item in a search result.<p>
 * 
 * @author Alexander Kandzior
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.26 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchResult implements I_CmsMemoryMonitorable, Comparable {

    /** The creation date of this search result. */
    protected Date m_dateCreated;

    /** The last modification date of this search result. */
    protected Date m_dateLastModified;

    /** The document type of the search result. */
    protected String m_documentType;

    /** The excerpt of this search result. */
    protected String m_excerpt;

    /** The resource path of this search result. */
    protected String m_path;

    /** The score of this search result. */
    protected int m_score;

    /** Holds the values of the search result fields. */
    Map m_fields;

    /** Contains the pre-calculated memory size. */
    private int m_memorySize;

    /**
     * Creates a new search result.<p>
     * 
     * @param score the score of this search result
     * @param doc the Lucene document to extract fields from such as description, title, key words etc. pp.
     * @param excerpt the excerpt of the search result's content
     */
    public CmsSearchResult(int score, Document doc, String excerpt) {

        m_score = score;
        m_excerpt = excerpt;
        m_fields = new HashMap();

        Iterator i = doc.getFields().iterator();
        while (i.hasNext()) {
            Field field = (Field)i.next();
            if ((field != null) && field.isStored()) {
                // content can be displayed only if it has been stored in the field
                String name = field.name();
                String value = field.stringValue();
                if (CmsStringUtil.isNotEmpty(value)
                    && !CmsSearchField.FIELD_PATH.equals(name)
                    && !CmsSearchField.FIELD_DATE_CREATED.equals(name)
                    && !CmsSearchField.FIELD_DATE_LASTMODIFIED.equals(name)) {
                    // these "hard coded" fields are treated differently
                    m_fields.put(name, value);
                }
            }
        }

        Field f = doc.getField(CmsSearchField.FIELD_PATH);
        if (f != null) {
            m_path = f.stringValue();
        } else {
            m_path = null;
        }

        f = doc.getField(CmsSearchField.FIELD_DATE_CREATED);
        if (f != null) {
            try {
                m_dateCreated = DateTools.stringToDate(f.stringValue());
            } catch (ParseException exc) {
                m_dateCreated = null;
            }
        } else {
            m_dateCreated = null;
        }

        f = doc.getField(CmsSearchField.FIELD_DATE_LASTMODIFIED);
        if (f != null) {
            try {
                m_dateLastModified = DateTools.stringToDate(f.stringValue());
            } catch (ParseException exc) {
                m_dateLastModified = null;
            }
        } else {
            m_dateLastModified = null;
        }

        f = doc.getField(CmsSearchField.FIELD_TYPE);
        if (f != null) {
            m_documentType = f.stringValue();
        } else {
            m_documentType = null;
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsSearchResult) {
            return ((CmsSearchResult)obj).m_score - m_score;
        }
        return 0;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsSearchResult) {
            CmsSearchResult other = (CmsSearchResult)obj;
            return m_documentType.equals(other.m_documentType) && m_path.equals(other.m_path);
        }
        return false;
    }

    /**
     * Returns the date created.<p>
     *
     * @return the date created
     */
    public Date getDateCreated() {

        return (Date)m_dateCreated.clone();
    }

    /**
     * Returns the date last modified.<p>
     *
     * @return the date last modified
     */
    public Date getDateLastModified() {

        return (Date)m_dateLastModified.clone();
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     * 
     * @deprecated use {@link #getField(String)} instead with the name of the field, 
     *      for example use {@link CmsSearchField#FIELD_DESCRIPTION} to get the description (if available)
     */
    public String getDescription() {

        return getField(CmsSearchField.FIELD_DESCRIPTION);
    }

    /**
     * Returns the document type of the search result document.<p>
     * 
     * Usually this will be a VFS resource type String that can be used in the 
     * resource type manager with {@link org.opencms.loader.CmsResourceManager#getResourceType(String)}.
     * However, what is stored in the document type field depends only on the indexer used, and therefore it
     * may also be some String not referring  a VFS resource type but some external type or application.
     * It may also be <code>null</code> in case it has not been set by a non-standard indexer.<p>  
     * 
     * @return the document type of the search result document
     * 
     * @see org.opencms.loader.CmsResourceManager#getResourceType(String)
     */
    public String getDocumentType() {

        return m_documentType;
    }

    /**
     * Returns the excerpt.<p>
     *
     * @return the excerpt
     */
    public String getExcerpt() {

        return m_excerpt;
    }

    /**
     * Returns the text stored in the search index field with the given name.<p>
     * 
     * @param fieldName the name of the field to get the stored text for
     * 
     * @return the text stored in the search index field with the given name
     */
    public String getField(String fieldName) {

        return (String)m_fields.get(fieldName);
    }

    /**
     * Returns the key words.<p>
     *
     * @return the key words
     * 
     * @deprecated use {@link #getField(String)} instead with the name of the field, 
     *      for example use {@link CmsSearchField#FIELD_KEYWORDS} to get the keywords (if available)
     */
    public String getKeywords() {

        return getField(CmsSearchField.FIELD_KEYWORDS);
    }

    /**
     * @see org.opencms.monitor.I_CmsMemoryMonitorable#getMemorySize()
     */
    public int getMemorySize() {

        if (m_memorySize == 0) {
            int result = 8;
            if (m_dateCreated != null) {
                result += CmsMemoryMonitor.getMemorySize(m_dateCreated);
            }
            if (m_dateLastModified != null) {
                result += CmsMemoryMonitor.getMemorySize(m_dateLastModified);
            }
            if (m_path != null) {
                result += CmsMemoryMonitor.getMemorySize(m_path);
            }
            if (m_fields != null) {
                Iterator entries = m_fields.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry)entries.next();
                    result += CmsMemoryMonitor.getMemorySize(entry.getKey());
                    result += CmsMemoryMonitor.getMemorySize(entry.getValue());
                }
            }
            if (m_excerpt != null) {
                result += CmsMemoryMonitor.getMemorySize(m_excerpt);
            }
            m_memorySize = result;
        }
        return m_memorySize;
    }

    /**
     * Returns the path.<p>
     *
     * @return the path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the score.<p>
     *
     * @return the score
     */
    public int getScore() {

        return m_score;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     * 
     * @deprecated use {@link #getField(String)} instead with the name of the field, 
     *      for example use {@link CmsSearchField#FIELD_TITLE} to get the title (if available)
     */
    public String getTitle() {

        return getField(CmsSearchField.FIELD_TITLE);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_documentType.hashCode() * 1109 + m_path.hashCode();
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchResult.java,v $
 * Date   : $Date: 2005/03/26 11:36:35 $
 * Version: $Revision: 1.14 $
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

import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.monitor.I_CmsMemoryMonitorable;
import org.opencms.search.documents.I_CmsDocumentFactory;

import java.util.Date;

import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Contains the data of a single item in a search result.<p>
 * 
 * @version $Revision: 1.14 $ $Date: 2005/03/26 11:36:35 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.1
 */
public class CmsSearchResult implements I_CmsMemoryMonitorable, Comparable {

    /** The creation date of this search result. */
    protected Date m_dateCreated;

    /** The last modification date of this search result. */
    protected Date m_dateLastModified;

    /** The description of this search result. */
    protected String m_description;

    /** The excerpt of this search result. */
    protected String m_excerpt;

    /** The key words of this search result. */
    protected String m_keyWords;

    /** The resource path of this search result. */
    protected String m_path;

    /** The score of this search result. */
    protected int m_score;

    /** The title of this search result. */
    protected String m_title;

    /**
     * Creates a new search result.<p>
     * 
     * @param score the score of this search result
     * @param luceneDocument the Lucene document to extract fields from such as description, title, key words etc. pp.
     * @param excerpt the excerpt of the search result's content
     */
    protected CmsSearchResult(int score, Document luceneDocument, String excerpt) {

        Field f = null;

        m_score = score;
        m_excerpt = excerpt;

        if ((f = luceneDocument.getField(I_CmsDocumentFactory.DOC_DESCRIPTION)) != null) {
            m_description = f.stringValue();
        } else {
            m_description = null;
        }

        if ((f = luceneDocument.getField(I_CmsDocumentFactory.DOC_KEYWORDS)) != null) {
            m_keyWords = f.stringValue();
        } else {
            m_keyWords = null;
        }

        if ((f = luceneDocument.getField(I_CmsDocumentFactory.DOC_TITLE_KEY)) != null) {
            m_title = f.stringValue();
        } else {
            m_title = null;
        }

        if ((f = luceneDocument.getField(I_CmsDocumentFactory.DOC_PATH)) != null) {
            m_path = f.stringValue();
        } else {
            m_path = null;
        }

        if ((f = luceneDocument.getField(I_CmsDocumentFactory.DOC_DATE_CREATED)) != null) {
            m_dateCreated = DateField.stringToDate(f.stringValue());
        } else {
            m_dateCreated = null;
        }

        if ((f = luceneDocument.getField(I_CmsDocumentFactory.DOC_DATE_LASTMODIFIED)) != null) {
            m_dateLastModified = DateField.stringToDate(f.stringValue());
        } else {
            m_dateLastModified = null;
        }

    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object object) {

        if (object == null || !(object instanceof CmsSearchResult)) {
            return 0;
        }

        try {
            int score = ((CmsSearchResult)object).getScore();

            if (m_score > score) {
                return -1;
            }

            if (m_score < score) {
                return 1;
            }
        } catch (Exception e) {
            // noop
        }

        return 0;
    }

    /**
     * Returns the date created.<p>
     *
     * @return the date created
     */
    public Date getDateCreated() {

        return m_dateCreated;
    }

    /**
     * Returns the date last modified.<p>
     *
     * @return the date last modified
     */
    public Date getDateLastModified() {

        return m_dateLastModified;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
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
     * Returns the key words.<p>
     *
     * @return the key words
     */
    public String getKeywords() {

        return m_keyWords;
    }

    /**
     * @see org.opencms.monitor.I_CmsMemoryMonitorable#getMemorySize()
     */
    public int getMemorySize() {

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

        if (m_title != null) {
            result += CmsMemoryMonitor.getMemorySize(m_title);
        }

        if (m_description != null) {
            result += CmsMemoryMonitor.getMemorySize(m_description);
        }

        if (m_keyWords != null) {
            result += CmsMemoryMonitor.getMemorySize(m_keyWords);
        }

        if (m_excerpt != null) {
            result += CmsMemoryMonitor.getMemorySize(m_excerpt);
        }

        return result;
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
     */
    public String getTitle() {

        return m_title;
    }

}
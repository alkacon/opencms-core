/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/galleries/CmsGallerySearchResult.java,v $
 * Date   : $Date: 2010/01/11 13:26:40 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.galleries;

import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsUUID;

import org.apache.lucene.document.Document;

/**
 * Contains a single search result from the ADE gallery search.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearchResult implements Comparable<CmsGallerySearchResult> {

    /** The additional info stored for this search result. */
    protected String m_additionalInfos;

    /** The creation date of this search result. */
    protected long m_dateCreated;

    /** The last modification date of this search result. */
    protected long m_dateLastModified;

    /** The excerpt of this search result. */
    protected String m_excerpt;

    /** The VFS structure id of this search result. */
    protected CmsUUID m_id;

    /** The resource path of this search result. */
    protected String m_path;

    /** The resource type of the search result. */
    protected String m_resourceType;

    /** The score of this search result. */
    protected int m_score;

    /**
     * Creates a new search result.<p>
     * 
     * @param score the score of this search result
     * @param doc the Lucene document to extract fields from such as description, title, key words etc. pp.
     * @param excerpt the excerpt of the search result's content
     */
    public CmsGallerySearchResult(int score, Document doc, String excerpt) {

        /** TODO: implement this. */
    }

    /**
     * Compares two search results.<p>
     * 
     * @param other the result to compare this result with
     * @return the comparison result
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsGallerySearchResult other) {

        if (other == this) {
            return 0;
        }
        return other.m_score - m_score;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsGallerySearchResult) {
            CmsGallerySearchResult other = (CmsGallerySearchResult)obj;
            return m_id.equals(other.m_id);
        }
        return false;
    }

    /**
     * Returns the additional info stored for this search result.<p>
     * 
     * @return the additional info stored for this search result
     */
    public String getAdditionalInfos() {

        return m_additionalInfos;
    }

    /**
     * Returns the date created.<p>
     *
     * @return the date created
     */
    public long getDateCreated() {

        return m_dateCreated;
    }

    /**
     * Returns the date last modified.<p>
     *
     * @return the date last modified
     */
    public long getDateLastModified() {

        return m_dateLastModified;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return getField(CmsSearchField.FIELD_DESCRIPTION);
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

        /** TODO: implement this */
        return null;
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
     * Returns the resource type of the search result document.<p>
     * 
     * @return the resource type of the search result document
     * 
     * @see org.opencms.loader.CmsResourceManager#getResourceType(String)
     */
    public String getResourceType() {

        return m_resourceType;
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

        return getField(CmsSearchField.FIELD_TITLE);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_id.hashCode();
    }
}
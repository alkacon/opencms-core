/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/galleries/CmsGallerySearchResult.java,v $
 * Date   : $Date: 2010/01/20 09:12:48 $
 * Version: $Revision: 1.4 $
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
import org.opencms.util.CmsStringUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

/**
 * Contains a single search result from the ADE gallery search.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0 
 */
/**
 *
 */
public class CmsGallerySearchResult implements Comparable<CmsGallerySearchResult> {

    /** The additional information for the ADE galleries. */
    protected String m_additonalInfo;

    /** The supported container types of this search result. */
    protected List<String> m_containerTypes;

    /** The creation date of this search result. */
    protected Date m_dateCreated;

    /** The expiration date of this search result. */
    protected Date m_dateExpired;

    /** The last modification date of this search result. */
    protected Date m_dateLastModified;

    /** The release date of this search result. */
    protected Date m_dateReleased;

    /** The description of this search result. */
    protected String m_description;

    /** The excerpt of this search result. */
    protected String m_excerpt;

    /** The length of the search result. */
    protected int m_length;

    /** The resource path of this search result. */
    protected String m_path;

    /** The resource type of the search result. */
    protected String m_resourceType;

    /** The score of this search result. */
    protected int m_score;

    /** The state of the search result. */
    protected int m_state;

    /** The structure UUID of the resource. */
    protected String m_structureId;

    /** The title of this search result. */
    protected String m_title;

    /** The user who created the search result resource. */
    protected String m_userCreated;

    /** The user who last modified the search result resource. */
    protected String m_userLastModified;

    /**
     * Creates a new gallery search result.<p>
     * 
     * @param score the score of this search result
     * @param doc the Lucene document to extract fields from such as description, title, key words etc. pp.
     * @param excerpt the excerpt of the search result's content
     */
    public CmsGallerySearchResult(int score, Document doc, String excerpt) {

        m_score = score;
        m_excerpt = excerpt;

        m_path = null;
        Fieldable f = doc.getFieldable(CmsSearchField.FIELD_PATH);
        if (f != null) {
            m_path = f.stringValue();
        }

        m_title = null;
        f = doc.getFieldable(CmsSearchField.FIELD_TITLE);
        if (f != null) {
            m_title = f.stringValue();
        }

        m_description = null;
        f = doc.getFieldable(CmsSearchField.FIELD_DESCRIPTION);
        if (f != null) {
            m_description = f.stringValue();
        }

        m_resourceType = null;
        f = doc.getFieldable(CmsSearchField.FIELD_TYPE);
        if (f != null) {
            m_resourceType = f.stringValue();
        }

        m_dateCreated = null;
        f = doc.getFieldable(CmsSearchField.FIELD_DATE_CREATED);
        if (f != null) {
            try {
                m_dateCreated = DateTools.stringToDate(f.stringValue());
            } catch (ParseException exc) {
                // NOOP, date is null
            }
        }

        m_dateLastModified = null;
        f = doc.getFieldable(CmsSearchField.FIELD_DATE_LASTMODIFIED);
        if (f != null) {
            try {
                m_dateLastModified = DateTools.stringToDate(f.stringValue());
            } catch (ParseException exc) {
                // NOOP, date is null
            }
        }

        m_dateExpired = null;
        f = doc.getFieldable(CmsGallerySearchFieldMapping.FIELD_RESOURCE_DATE_EXPIRED);
        if (f != null) {
            try {
                m_dateExpired = DateTools.stringToDate(f.stringValue());
            } catch (ParseException exc) {
                // NOOP, date is null
            }
        }

        m_dateReleased = null;
        f = doc.getFieldable(CmsGallerySearchFieldMapping.FIELD_RESOURCE_DATE_RELEASED);
        if (f != null) {
            try {
                m_dateReleased = DateTools.stringToDate(f.stringValue());
            } catch (ParseException exc) {
                // NOOP, date is null
            }
        }

        m_length = 0;
        f = doc.getFieldable(CmsGallerySearchFieldMapping.FIELD_RESOURCE_LENGTH);
        if (f != null) {
            try {
                m_length = Integer.parseInt(f.stringValue());
            } catch (NumberFormatException exc) {
                // NOOP, default is 0
            }
        }

        m_state = 0;
        f = doc.getFieldable(CmsGallerySearchFieldMapping.FIELD_RESOURCE_STATE);
        if (f != null) {
            try {
                m_state = Integer.parseInt(f.stringValue());
            } catch (NumberFormatException exc) {
                // NOOP, default is 0
            }
        }

        m_userCreated = null;
        f = doc.getFieldable(CmsGallerySearchFieldMapping.FIELD_RESOURCE_USER_CREATED);
        if (f != null) {
            m_userCreated = f.stringValue();
        }

        m_structureId = null;
        f = doc.getFieldable(CmsGallerySearchFieldMapping.FIELD_RESOURCE_STRUCTURE_ID);
        if (f != null) {
            m_structureId = f.stringValue();
        }

        m_userLastModified = null;
        f = doc.getFieldable(CmsGallerySearchFieldMapping.FIELD_RESOURCE_USER_LASTMODIFIED);
        if (f != null) {
            m_userLastModified = f.stringValue();
        }

        m_additonalInfo = null;
        f = doc.getFieldable(CmsGallerySearchFieldMapping.FIELD_ADDITIONAL_INFO);
        if (f != null) {
            m_additonalInfo = f.stringValue();
        }

        m_containerTypes = null;
        f = doc.getFieldable(CmsGallerySearchFieldMapping.FIELD_CONTAINER_TYPES);
        if (f != null) {
            String containers = f.stringValue();
            m_containerTypes = CmsStringUtil.splitAsList(containers, ' ');
        }
    }

    /**
     * Compares two search results based on the score of the result.<p>
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
            return m_path.equals(other.m_path);
        }
        return false;
    }

    /**
     * Returns the additional information stored for this search result for the ADE galleries.<p>
     * 
     * @return the additional information stored for this search result for the ADE galleries
     */
    public String getAdditonalInfo() {

        return m_additonalInfo;
    }

    /**
     * Returns the containers supported by this resource.<p>
     * 
     * @return the containers supported by this resource
     */
    public List<String> getContainerTypes() {

        return m_containerTypes;
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
     * Returns the date the resource expires.<p>
     * 
     * @return the date the resource expires
     * 
     * @see org.opencms.file.CmsResource#getDateExpired()
     */
    public Date getDateExpired() {

        return m_dateExpired;
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
     * Returns the date the resource is released.<p>
     * 
     * @return the date the resource is released
     * 
     * @see  org.opencms.file.CmsResource#getDateReleased()
     */
    public Date getDateReleased() {

        return m_dateReleased;
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
     * Returns the length of the resource.<p>
     * 
     * @return the length of the resource
     * 
     * @see org.opencms.file.CmsResource#getLength()
     */
    public int getLength() {

        return m_length;
    }

    /**
     * Returns the resource root path.<p>
     *
     * @return the resource root path
     * 
     * @see org.opencms.file.CmsResource#getRootPath()
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
     * Returns the Lucene search score for this result.<p>
     *
     * @return the Lucene search score for this result
     */
    public int getScore() {

        return m_score;
    }

    /**
     * Returns the state of the resource.<p>
     * 
     * @return the state of the resource
     * 
     * @see org.opencms.file.CmsResource#getState()
     */
    public int getState() {

        return m_state;
    }

    /**
     * Returns the structure id of the resource.<p>
     * 
     * @return the structure id of the resource
     */
    public String getStructureId() {

        return m_structureId;
    }

    /**
     * Returns the title of the resource.<p>
     *
     * @return the title of the resource
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns the name of the user who created the resource.<p>
     * 
     * @return the name of the user who created the resource
     * 
     * @see org.opencms.file.CmsResource#getUserCreated()
     */
    public String getUserCreated() {

        return m_userCreated;
    }

    /**
     * Returns the name of the user who last modified the resource.<p>
     * 
     * @return the name of the user who last modified the resource
     * 
     * @see org.opencms.file.CmsResource#getUserLastModified()
     */
    public String getUserLastModified() {

        return m_userLastModified;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_path.hashCode();
    }
}
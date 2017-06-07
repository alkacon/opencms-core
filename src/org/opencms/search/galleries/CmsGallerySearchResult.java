/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Contains a single search result from the gallery search index.<p>
 *
 * @since 8.0.0
 */
/**
 *
 */
public class CmsGallerySearchResult implements Comparable<CmsGallerySearchResult> {

    /** The logger instance for this class. */
    public static final Log LOG = CmsLog.getLog(CmsGallerySearchResult.class);

    /** The additional information for the gallery search index. */
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

    /** The locales in which the content is available. */
    protected List<String> m_locales;

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
     * Creates a fake gallery search result by reading the necessary data from a VFS resource.<p>
     *
     * @param cms the current CMS context
     * @param res the resource from which the data should be read
     */
    public CmsGallerySearchResult(CmsObject cms, CmsResource res) {

        try {
            Map<String, String> props = CmsProperty.toMap(
                cms.readPropertyObjects(res, CmsResourceTypeXmlContainerPage.isContainerPage(res)));
            m_title = props.get(CmsPropertyDefinition.PROPERTY_TITLE);
            m_description = props.get(CmsPropertyDefinition.PROPERTY_DESCRIPTION);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        if (m_description == null) {
            m_description = "";
        }
        if (m_title == null) {
            m_title = res.getName();
        }
        m_dateCreated = new Date(res.getDateCreated());
        m_dateExpired = new Date(res.getDateExpired());
        m_dateLastModified = new Date(res.getDateLastModified());
        m_dateReleased = new Date(res.getDateReleased());
        m_length = res.getLength();
        m_locales = null;
        m_path = res.getRootPath();
        try {
            m_resourceType = OpenCms.getResourceManager().getResourceType(res.getTypeId()).getTypeName();
        } catch (CmsLoaderException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        m_state = res.getState().getState();
        m_structureId = res.getStructureId().toString();
        try {
            m_userCreated = cms.readUser(res.getUserCreated()).getFullName();
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        try {
            m_userLastModified = cms.readUser(res.getUserLastModified()).getFullName();
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Creates a new gallery search result.
     *
     * @param cms the current CMS context (used for reading information missing from the index)
     * @param doc the I_CmsSearchResult document to extract information from.
     * @param score the score of this search result
     * @param locale the locale to create the result for
     */
    public CmsGallerySearchResult(I_CmsSearchDocument doc, CmsObject cms, int score, Locale locale) {

        if (null == doc) {
            throw new IllegalArgumentException();
        }

        m_score = score; //(int)doc.getScore();

        m_path = doc.getFieldValueAsString(CmsSearchField.FIELD_PATH);

        if (null == locale) {
            OpenCms.getLocaleManager();
            locale = CmsLocaleManager.getDefaultLocale();
        }

        String effFieldName = CmsSearchFieldConfiguration.getLocaleExtendedName(
            CmsSearchField.FIELD_TITLE_UNSTORED,
            locale.toString()) + "_s";
        m_title = doc.getFieldValueAsString(effFieldName);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_title)) {
            m_title = doc.getFieldValueAsString(
                CmsPropertyDefinition.PROPERTY_TITLE + CmsSearchField.FIELD_DYNAMIC_PROPERTIES_DIRECT);
        }

        effFieldName = CmsSearchFieldConfiguration.getLocaleExtendedName(
            CmsSearchField.FIELD_DESCRIPTION,
            locale.toString()) + "_s";
        m_description = doc.getFieldValueAsString(effFieldName);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_description)) {
            m_description = doc.getFieldValueAsString(
                CmsPropertyDefinition.PROPERTY_DESCRIPTION + CmsSearchField.FIELD_DYNAMIC_PROPERTIES);
        }

        m_resourceType = doc.getFieldValueAsString(CmsSearchField.FIELD_TYPE);

        m_dateCreated = doc.getFieldValueAsDate(CmsSearchField.FIELD_DATE_CREATED);

        m_dateLastModified = doc.getFieldValueAsDate(CmsSearchField.FIELD_DATE_LASTMODIFIED);

        m_dateExpired = doc.getFieldValueAsDate(CmsSearchField.FIELD_DATE_EXPIRED);

        m_dateReleased = doc.getFieldValueAsDate(CmsSearchField.FIELD_DATE_RELEASED);

        m_length = 0;
        final String s_length = doc.getFieldValueAsString(CmsSearchField.FIELD_SIZE);
        if (null != s_length) {
            try {
                m_length = Integer.parseInt(s_length);
            } catch (NumberFormatException exc) {
                // NOOP, default is 0
            }
        }

        m_state = 0;
        final String s_state = doc.getFieldValueAsString(CmsSearchField.FIELD_STATE);
        if (s_state != null) {
            try {
                m_state = Integer.parseInt(s_state);
            } catch (NumberFormatException exc) {
                // NOOP, default is 0
            }
        }

        m_userCreated = doc.getFieldValueAsString(CmsSearchField.FIELD_USER_CREATED);

        m_structureId = doc.getFieldValueAsString(CmsSearchField.FIELD_ID);

        m_userLastModified = doc.getFieldValueAsString(CmsSearchField.FIELD_USER_LAST_MODIFIED);

        m_additonalInfo = doc.getFieldValueAsString(CmsSearchField.FIELD_ADDITIONAL_INFO);
        final String s_containerTypes = doc.getFieldValueAsString(CmsSearchField.FIELD_CONTAINER_TYPES);
        if (s_containerTypes != null) {
            m_containerTypes = CmsStringUtil.splitAsList(s_containerTypes, ' ');
        } else {
            m_containerTypes = new ArrayList<String>(0);
        }

        final String s_locales = doc.getFieldValueAsString(CmsSearchField.FIELD_RESOURCE_LOCALES);
        if (null != s_locales) {
            m_locales = CmsStringUtil.splitAsList(s_locales, ' ');
        } else {
            m_locales = new ArrayList<String>(0);
        }

        if ((null != cms) && (null != m_structureId)) {
            initializeMissingFieldsFromVfs(cms, new CmsUUID(m_structureId));
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
     * Returns the additional information stored for this search result in the gallery search index.<p>
     *
     * @return the additional information stored for this search result in the gallery search index
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
     * Returns the list of locales this search result is available for.<p>
     *
     * @return the list of locales this search result is available for
     */
    public List<String> getLocales() {

        return m_locales;
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

    /**
     * Returns if the related resource is released and not expired.<p>
     *
     * @param cms the cms context
     *
     * @return <code>true</code> if the related resource is released and not expired
     */
    public boolean isReleaseAndNotExpired(CmsObject cms) {

        long time = cms.getRequestContext().getRequestTime();
        return (time == CmsResource.DATE_RELEASED_EXPIRED_IGNORE)
            || ((time > m_dateReleased.getTime()) && (time < m_dateExpired.getTime()));
    }

    /**
     * Initializes missing fields by reading the information from the VFS.<p>
     *
     * @param cms the current CMS context
     * @param structureId the current structure id
     */
    protected void initializeMissingFieldsFromVfs(CmsObject cms, CmsUUID structureId) {

        if (structureId == null) {
            return;
        }
        if ((m_title != null) && (m_description != null)) {
            return;
        }

        try {
            CmsResource res = cms.readResource(structureId);
            if (m_description == null) {
                CmsProperty descProp = cms.readPropertyObject(
                    res,
                    CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                    CmsResourceTypeXmlContainerPage.isContainerPage(res));
                m_description = descProp.getValue();
            }

            if (m_title == null) {
                CmsProperty titleProp = cms.readPropertyObject(
                    res,
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    CmsResourceTypeXmlContainerPage.isContainerPage(res));
                m_title = titleProp.getValue();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_title)) {
                    m_title = res.getName();
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return;
        }
    }
}
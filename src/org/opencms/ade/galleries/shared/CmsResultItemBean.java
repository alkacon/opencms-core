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

package org.opencms.ade.galleries.shared;

import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.sort.I_CmsHasPath;
import org.opencms.gwt.shared.sort.I_CmsHasTitle;
import org.opencms.gwt.shared.sort.I_CmsHasType;
import org.opencms.util.CmsStringUtil;

/**
 * A specific bean holding all info to be displayed in {@link org.opencms.ade.galleries.client.ui.CmsResultListItem}s.<p>
 *
 * @see org.opencms.ade.galleries.client.ui.CmsResultListItem
 *
 * @since 8.0.0
 */
public class CmsResultItemBean extends CmsListInfoBean implements I_CmsHasTitle, I_CmsHasPath, I_CmsHasType {

    /** The structured id of the resource. */
    private String m_clientId;

    /** The formatted date of the last modification. */
    private String m_dateLastModified;

    /** The result item description. */
    private String m_description;

    /** The image dimensions. */
    private String m_dimension;

    /** Flag which indicates whether the resource for this result is a copy model. */
    private boolean m_isCopyModel;

    /** A flag which indicates whether this result item corresponds to a preset value in the editor.<p> */
    private boolean m_isPreset;

    /** The reason this resource may not be edited. Editable if empty. */
    private String m_noEditReson;

    /** The resource path as a unique resource id. */
    private String m_path;

    /** The pseudo resource type, used to override the default type icon. */
    private String m_pseudoType;

    /** The raw title, without any status information attached. */
    private String m_rawTitle = "";

    /** Flag indicating if the result item resource is currently released and not expired. */
    private boolean m_releasedAndNotExpired;

    /** The name of the user who last modified the resource. */
    private String m_userLastModified;

    /** The link for displaying the resource. */
    private String m_viewLink;

    /**
     * Default constructor.<p>
     */
    public CmsResultItemBean() {

        // empty default constructor
    }

    /**
     * Returns the structured id.<p>
     *
     * @return the structured id
     */
    public String getClientId() {

        return m_clientId;
    }

    /**
     * Returns the formatted last modification date.<p>
     *
     * @return the formatted last modification date
     */
    public String getDateLastModified() {

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
     * Returns the dimension.<p>
     *
     * @return the dimension
     */
    public String getDimension() {

        return m_dimension;
    }

    /**
     * Returns the noEditReson.<p>
     *
     * @return the noEditReson
     */
    public String getNoEditReson() {

        return m_noEditReson;
    }

    /**
     * Returns the resourcePath.<p>
     *
     * @return the resourcePath
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the pseudo resource type, used to override the default type icon.<p>
     *
     * @return the pseudo resource type
     */
    public String getPseudoType() {

        return m_pseudoType;
    }

    /**
     * Gets the raw title, without status information attached.<p>
     *
     * @return the raw title
     */
    public String getRawTitle() {

        return m_rawTitle;
    }

    /**
     * @see org.opencms.gwt.shared.CmsListInfoBean#getSubTitle()
     */
    @Override
    public String getSubTitle() {

        String fieldSubTitle = super.getSubTitle();
        if (fieldSubTitle != null) {
            return fieldSubTitle;
        }
        return m_userLastModified + " / " + m_dateLastModified;
    }

    /**
     * Returns the resource type name.<p>
     *
     * @return the resource type name
     */
    public String getType() {

        return getResourceType();
    }

    /**
     * Gets the name of the user who last modified the resource.<p>
     *
     * @return the name of the user who last modified the resource
     */
    public String getUserLastModified() {

        return m_userLastModified;
    }

    /**
     * Gets the link for displaying the resource.<p>
     *
     * @return the link for displaying the resource
     */
    public String getViewLink() {

        return m_viewLink;
    }

    /**
     * Returns true if the result resource is a copy model.<p>
     *
     * @return true if the resource is a copy model
     */
    public boolean isCopyModel() {

        return m_isCopyModel;
    }

    /**
     * Returns if the represented resource is editable by the current user.<p>
     *
     * @return <code>true</code> if editable
     */
    public boolean isEditable() {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(m_noEditReson);
    }

    /**
     * True if this is result item corresponds to a preset value in the editor.<p>
     *
     * @return true if this corresponds to a preset value
     */
    public boolean isPreset() {

        return m_isPreset;
    }

    /**
     * Returns if the result item resource is currently released and not expired.<p>
     *
     * @return <code>true</code> if the result item resource is currently released and not expired
     */
    public boolean isReleasedAndNotExpired() {

        return m_releasedAndNotExpired;
    }

    /**
     * Sets the structure id.<p>
     *
     * @param clientId the structure id to set
     */
    public void setClientId(String clientId) {

        m_clientId = clientId;
    }

    /**
     * Sets the formatted last modification date.<p>
     *
     * @param formattedDate the formatted last modification date
     */
    public void setDateLastModified(String formattedDate) {

        m_dateLastModified = formattedDate;
    }

    /**
     * Sets the description.<p>
     *
     * Also used as sub-title.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the dimension.<p>
     *
     * @param dimension the dimension to set
     */
    public void setDimension(String dimension) {

        m_dimension = dimension;
    }

    /**
     * Sets the "copy model" status of this result bean.<p>
     *
     * @param isCopyModel true if this result should be marked as a copy model
     */
    public void setIsCopyModel(boolean isCopyModel) {

        m_isCopyModel = isCopyModel;
    }

    /**
     * Sets the reason this resource may not be edited.<p>
     *
     * @param noEditReson the reason this resource may not be edited to set
     */
    public void setNoEditReson(String noEditReson) {

        m_noEditReson = noEditReson;
    }

    /**
     * Sets the resource path.<p>
     *
     * @param path the resource path to set
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * Sets the "is preset" flag.<p>
     *
     * @param preset the "is preset" flag
     */
    public void setPreset(boolean preset) {

        m_isPreset = preset;
    }

    /**
     * Sets the pseudo resource type, used to override the default type icon.<p>
     *
     * @param pseudoType the pseudo resource type
     */
    public void setPseudoType(String pseudoType) {

        m_pseudoType = pseudoType;
    }

    /**
     * Sets the raw title.<p>
     *
     * @param rawTitle the raw title
     */
    public void setRawTitle(String rawTitle) {

        m_rawTitle = rawTitle;
    }

    /**
     * Sets if the result item resource is currently released and not expired.<p>
     *
     * @param releasedAndNotExpired if the result item resource is currently released and not expired
     */
    public void setReleasedAndNotExpired(boolean releasedAndNotExpired) {

        m_releasedAndNotExpired = releasedAndNotExpired;
    }

    /**
     * Sets the resource type name.<p>
     *
     * @param type the resource type name to set
     */
    public void setType(String type) {

        setResourceType(type);
    }

    /**
     * Sets the name of the user who last modified the resource.<p>
     *
     * @param userLastModified a user name
     */
    public void setUserLastModified(String userLastModified) {

        m_userLastModified = userLastModified;
    }

    /**
     * Sets the link for displaying the resource.<p>
     *
     * @param viewLink the link for displaying the
     */
    public void setViewLink(String viewLink) {

        m_viewLink = viewLink;
    }

}

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

import java.util.ArrayList;

/**
 * Represents a gallery folder.<p>
 *
 * @since 8.0.0
 */
public class CmsGalleryFolderBean extends CmsListInfoBean implements I_CmsHasPath {

    /** The gallery group. */
    private CmsGalleryGroup m_group;

    /** A list with content types corresponding to this gallery type. */
    private ArrayList<String> m_contentTypes;

    /** Flag to indicate if the user has write permissions to the gallery folder. */
    private boolean m_editable;

    /** The folder site-path. */
    private String m_path;

    /** The name of the JavaScript method to get an upload button provider object. */
    private String m_uploadAction;

    /** The label for the gallery group. */
    private String m_groupLabel;

    /**
     * Returns the content types which can be used within this gallery type.<p>
     *
     * @return the contentTypes
     */
    public ArrayList<String> getContentTypes() {

        return m_contentTypes;
    }

    /**
     * Gets the gallery group.
     *
     * @return the gallery group
     */
    public CmsGalleryGroup getGroup() {

        return m_group;
    }

    /**
     * Gets the label for the gallery group.
     *
     * @return the label for the gallery group
     */
    public String getGroupLabel() {

        return m_groupLabel;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getPath() {

        return m_path;
    }

    /**
     * @see org.opencms.gwt.shared.CmsListInfoBean#getSubTitle()
     */
    @Override
    public String getSubTitle() {

        return getPath();
    }

    /**
     * Gets the upload action.
     *
     * @return the upload action
     */
    public String getUploadAction() {

        return m_uploadAction;
    }

    /**
     * Returns the editable flag. Indicate if the user has write permissions to the gallery folder.<p>
     *
     * @return the editable flag
     */
    public boolean isEditable() {

        return m_editable;
    }

    /**
     * Returns if the gallery matches the given filter.<p>
     *
     * @param filter the filter to match
     *
     * @return <code>true</code> if the gallery matches the given filter.<p>
     */
    public boolean matchesFilter(String filter) {

        filter = filter.toLowerCase();
        return getTitle().toLowerCase().contains(filter) || m_path.toLowerCase().contains(filter);
    }

    /**
     * Sets the content types which can be used within this gallery type.<p>
     *
     * @param contentTypes the contentTypes to set
     */
    public void setContentTypes(ArrayList<String> contentTypes) {

        m_contentTypes = contentTypes;
    }

    /**
     * Sets if the user has write permissions to the gallery folder.<p>
     *
     * @param editable <code>true</code> if the user has write permissions to the gallery folder
     */
    public void setEditable(boolean editable) {

        m_editable = editable;
    }

    /**
     * Sets the gallery group.
     *
     * @param group the gallery group
     */
    public void setGroup(CmsGalleryGroup group) {

        m_group = group;
    }

    /**
     * Sets the gallery group label.
     *
     * @param groupLabel the gallery group label
     */
    public void setGroupLabel(String groupLabel) {

        m_groupLabel = groupLabel;
    }

    /**
     * Sets the description.<p>
     *
     * @param path the description to set
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * Sets the upload action.<p>
     *
     * @param jsFunctionName the name of the JavaScript function to call when the upload button is pressed.
     */
    public void setUploadAction(String jsFunctionName) {

        m_uploadAction = jsFunctionName;
    }

}

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

import org.opencms.gwt.shared.sort.I_CmsHasPath;
import org.opencms.gwt.shared.sort.I_CmsHasTitle;
import org.opencms.gwt.shared.sort.I_CmsHasType;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a gallery folder.<p>
 *
 * @since 8.0.0
 */
public class CmsGalleryFolderBean implements I_CmsHasTitle, I_CmsHasPath, I_CmsHasType, IsSerializable {

    /** A list with content types corresponding to this gallery type. */
    private ArrayList<String> m_contentTypes;

    /** Flag to indicate if the user has write permissions to the gallery folder. */
    private boolean m_editable;

    /** The folder site-path. */
    private String m_path;

    /** The gallery folder title. */
    private String m_title;

    /** The gallery type name. */
    private String m_type;

    /**
     * Returns the content types which can be used within this gallery type.<p>
     *
     * @return the contentTypes
     */
    public ArrayList<String> getContentTypes() {

        return m_contentTypes;
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
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns the gallery type name.<p>
     *
     * @return the gallery type name
     */
    public String getType() {

        return m_type;
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
        return m_title.toLowerCase().contains(filter) || m_path.toLowerCase().contains(filter);
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
     * Sets the description.<p>
     *
     * @param path the description to set
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Sets the gallery type name.<p>
     *
     * @param type the type name of this gallery
     */
    public void setType(String type) {

        m_type = type;
    }
}

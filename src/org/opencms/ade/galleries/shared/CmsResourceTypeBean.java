/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.gwt.shared.sort.I_CmsHasTitle;
import org.opencms.gwt.shared.sort.I_CmsHasType;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean representing resource type information.<p>
 * 
 * @since 8.0.0
 */
public class CmsResourceTypeBean implements I_CmsHasTitle, I_CmsHasType, IsSerializable {

    /** Flag to indicate if the current user may create a new resource of this type. */
    private boolean m_creatableType;

    /** The resource type description. */
    private String m_description;

    /** An array of gallery type names associated with this content type. */
    private ArrayList<String> m_galleryTypeNames;

    /** The path to the resource type icon. */
    private String m_iconResource;

    /** The name of the preview provider. */
    private String m_previewProviderName;

    /** The title. */
    private String m_title;

    /** The resource type name as a unique id. */
    private String m_type;

    /** The resource type id. */
    private int m_typeId;

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the list with the gallery types names associated with this resource type.<p>
     *
     * @return the gallery type names
     */
    public ArrayList<String> getGalleryTypeNames() {

        return m_galleryTypeNames;
    }

    /**
     * Returns the resource type icon.<p>
     *
     * @return the iconResource the icon for the resource type
     */
    public String getIconResource() {

        return m_iconResource;
    }

    /**
     * Returns the preview provider name.<p>
     *
     * @return the preview provider name
     */
    public String getPreviewProviderName() {

        return m_previewProviderName;
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
     * Returns the resource type name.<p>
     *
     * @return the resource type name
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the resource type id.<p>
     *
     * @return the resource type id
     */
    public int getTypeId() {

        return m_typeId;
    }

    /**
     * Returns if the current user may create a new resource of this type.<p>
     *
     * @return <code>true</code> if the current user may create a new resource of this type
     */
    public boolean isCreatableType() {

        return m_creatableType;
    }

    /**
     * Sets flag to indicate if the current user may create a new resource of this type.<p>
     *
     * @param creatableType <code>true</code> if the current user may create a new resource of this type
     */
    public void setCreatableType(boolean creatableType) {

        m_creatableType = creatableType;
    }

    /**
     * Sets the description.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the list with the gallery types names associated with this resource type.<p>
     *
     * @param galleryNames the list with gallery type names to set
     */
    public void setGalleryTypeNames(ArrayList<String> galleryNames) {

        m_galleryTypeNames = galleryNames;
    }

    /**
     * Sets the preview provider name.<p>
     *
     * @param previewProviderName the preview provider name to set
     */
    public void setPreviewProviderName(String previewProviderName) {

        m_previewProviderName = previewProviderName;
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
     * Sets the resource type name.<p>
     *
     * @param type the resource type name to set
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * Sets the resource type id.<p>
     *
     * @param typeId the resource type id to set
     */
    public void setTypeId(int typeId) {

        m_typeId = typeId;
    }
}
/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.galleries.shared;

import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;

/**
 * Bean representing resource type information.<p>
 *
 * @since 8.0.0
 */
public class CmsResourceTypeBean extends CmsListInfoBean {

    /** Enum for data source from which resource type bean was constructed. */
    public enum Origin {
        /** from sitemap config. */
        config,

        /** from other source. */
        other,
        /** unknown. */
        unknown
    }

    /** Enum representing the visibility of a resource type in the bean. */
    public enum TypeVisibility {
        /** Never displayed. */
        hidden,
        /** Always show the type. */
        showAlways,
        /** The user may choose to display the type, but it's not shown by default. */
        showOptional
    }

    /** Flag to indicate if the current user may create a new resource of this type. */
    private boolean m_creatableType;

    /** The creation path. */
    private String m_createPath;

    /** The deactivated flag. */
    private boolean m_deactivated;

    /** An array of gallery type names associated with this content type. */
    private ArrayList<String> m_galleryTypeNames;

    /** The naming pattern for new resources. */
    private String m_namePattern;

    /** Origin. */
    private Origin m_origin = Origin.unknown;

    /** The name of the preview provider. */
    private String m_previewProviderName;

    /** The resource type id. */
    private int m_typeId;

    /** Visibility of this type. */
    private TypeVisibility m_visibility = TypeVisibility.showAlways;

    /**
     * Gets the creation path.<p>
     *
     * @return the creation path
     */
    public String getCreatePath() {

        return m_createPath;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return getSubTitle();
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
     * Returns the naming pattern for new resources.<p>
     *
     * @return the naming pattern
     */
    public String getNamePattern() {

        return m_namePattern;
    }

    /**
     * Returns the origin.<p>
     *
     * @return the origin
     */
    public Origin getOrigin() {

        return m_origin;
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
     * Returns the resource type id.<p>
     *
     * @return the resource type id
     */
    public int getTypeId() {

        return m_typeId;
    }

    /**
     * Gets the visibility.<p>
     *
     * @return the visibility
     */
    public TypeVisibility getVisibility() {

        return m_visibility;
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
     * Returns if the type is deactivated.<p>
     *
     * @return if the type is deactivated
     */
    public boolean isDeactivated() {

        return m_deactivated;
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
     * Sets the creation path.<p>
     *
     * @param createPath the creation path
     */
    public void setCreatePath(String createPath) {

        m_createPath = createPath;
    }

    /**
     * Sets the type deactivated.<p>
     *
     * @param deactivated if the type is deactivated
     */
    public void setDeactivated(boolean deactivated) {

        m_deactivated = deactivated;
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
     * Sets the naming pattern for new resources.<p>
     *
     * @param pattern the naming pattern for new resources
     */
    public void setNamePattern(String pattern) {

        m_namePattern = pattern;
    }

    /**
     * Sets the origin.<p>
     *
     * @param origin the origin to set
     */
    public void setOrigin(Origin origin) {

        m_origin = origin;
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
     * Sets the resource type id.<p>
     *
     * @param typeId the resource type id to set
     */
    public void setTypeId(int typeId) {

        m_typeId = typeId;
    }

    /**
     * Sets the visibility.<p>
     *
     * @param visibility the new visibility
     */
    public void setVisibility(TypeVisibility visibility) {

        m_visibility = visibility;

    }

}
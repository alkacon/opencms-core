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

package org.opencms.ade.sitemap.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The gallery type bean.<p>
 */
public class CmsGalleryType implements IsSerializable {

    /** The gallery content type names. */
    private List<String> m_contentTypeNames;

    /** The resource type description. */
    private String m_description;

    /** The localized resource type name. */
    private String m_niceName;

    /** The resource type id. */
    private int m_typeId;

    /** The resource type name.*/
    private String m_typeName;

    /**
     * Returns the gallery content type names.<p>
     *
     * @return the gallery content type names
     */
    public List<String> getContentTypeNames() {

        return m_contentTypeNames;
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
     * Returns the nice name.<p>
     *
     * @return the nice name
     */
    public String getNiceName() {

        return m_niceName;
    }

    /**
     * Returns the type id.<p>
     *
     * @return the type id
     */
    public int getTypeId() {

        return m_typeId;
    }

    /**
     * Returns the type name.<p>
     *
     * @return the type name
     */
    public String getTypeName() {

        return m_typeName;
    }

    /**
     * Sets the gallery content type names.<p>
     *
     * @param contentTypeNames the gallery content type names to set
     */
    public void setContentTypeNames(List<String> contentTypeNames) {

        m_contentTypeNames = contentTypeNames;
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
     * Sets the nice name.<p>
     *
     * @param niceName the niceName to set
     */
    public void setNiceName(String niceName) {

        m_niceName = niceName;
    }

    /**
     * Sets the type id.<p>
     *
     * @param typeId the type id to set
     */
    public void setTypeId(int typeId) {

        m_typeId = typeId;
    }

    /**
     * Sets the type name.<p>
     *
     * @param typeName the type name to set
     */
    public void setTypeName(String typeName) {

        m_typeName = typeName;
    }
}

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

import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Gallery folder entry bean.<p>
 */
public class CmsGalleryFolderEntry implements IsSerializable {

    /** The resource properties. */
    private Map<String, CmsClientProperty> m_ownProperties;

    /** The resource type name. */
    private String m_resourceType;

    /** The site path. */
    private String m_sitePath;

    /** The structure id. */
    private CmsUUID m_structureId;

    /** The sub galleries. */
    private List<CmsGalleryFolderEntry> m_subGalleries;

    /**
     * Constructor.<p>
     */
    public CmsGalleryFolderEntry() {

        m_subGalleries = new ArrayList<CmsGalleryFolderEntry>();
    }

    /**
     * Adds a sub gallery.<p>
     *
     * @param gallery the gallery to add
     */
    public void addSubGallery(CmsGalleryFolderEntry gallery) {

        m_subGalleries.add(gallery);
    }

    /**
     * Returns the own properties.<p>
     *
     * @return the own properties
     */
    public Map<String, CmsClientProperty> getOwnProperties() {

        return m_ownProperties;
    }

    /**
     * Returns the resource type.<p>
     *
     * @return the resource type
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the structure id.<p>
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns the sub galleries.<p>
     *
     * @return the sub galleries
     */
    public List<CmsGalleryFolderEntry> getSubGalleries() {

        return m_subGalleries;
    }

    /**
     * Sets the own properties.<p>
     *
     * @param ownProperties the own properties to set
     */
    public void setOwnProperties(Map<String, CmsClientProperty> ownProperties) {

        m_ownProperties = ownProperties;
    }

    /**
     * Sets the resource type.<p>
     *
     * @param resourceType the resource type to set
     */
    public void setResourceType(String resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * Sets the site path.<p>
     *
     * @param sitePath the site path to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the structure id.<p>
     *
     * @param structureId the structure id to set
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

    /**
     * Sets the sub galleries.<p>
     *
     * @param subGalleries the subGalleries to set
     */
    public void setSubGalleries(List<CmsGalleryFolderEntry> subGalleries) {

        m_subGalleries = subGalleries;
    }

}

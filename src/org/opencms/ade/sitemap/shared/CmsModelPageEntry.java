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

import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.util.CmsUUID;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bean representing a model page, for use in the model page mode of the sitemap editor.<p>
 */
public class CmsModelPageEntry implements IsSerializable {

    /** The resource properties. */
    private Map<String, CmsClientProperty> m_ownProperties;

    /** The resource type name. */
    private String m_resourceType;

    /** The site path. */
    private String m_rootPath;

    /** The structure id. */
    private CmsUUID m_structureId;

    /** The site path (may be null if in different site). */
    private String m_sitePath;

    /** The list info bean containing the information to display in widgets showing this entry. */
    private CmsListInfoBean m_listInfoBean;

    /** The disabled flag. */
    private boolean m_disabled;

    /** The default page flag. */
    private boolean m_default;

    /**
     * Default constructor.<p>
     */
    public CmsModelPageEntry() {

        // empty default constructor
    }

    /**
     * Returns the listInfoBean.<p>
     *
     * @return the listInfoBean
     */
    public CmsListInfoBean getListInfoBean() {

        return m_listInfoBean;
    }

    /**
     * Returns the ownProperties.<p>
     *
     * @return the ownProperties
     */
    public Map<String, CmsClientProperty> getOwnProperties() {

        return m_ownProperties;
    }

    /**
     * Returns the resourceType.<p>
     *
     * @return the resourceType
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the rootPath.<p>
     *
     * @return the rootPath
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Returns the sitePath.<p>
     *
     * @return the sitePath
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the structureId.<p>
     *
     * @return the structureId
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns whether the given page is the default model.<p>
     *
     * @return if the given page is the default model
     */
    public boolean isDefault() {

        return m_default;
    }

    /**
     * Returns if the model page is disabled.<p>
     *
     * @return if the model page is disabled
     */
    public boolean isDisabled() {

        return m_disabled;
    }

    /**
     * Sets if the given page is the default model.<p>
     *
     * @param defaultl the given page is the default model
     */
    public void setDefault(boolean defaultl) {

        m_default = defaultl;
    }

    /**
     * Sets if the model page is disabled.<p>
     *
     * @param disabled if the model page is disabled
     */
    public void setDisabled(boolean disabled) {

        m_disabled = disabled;
    }

    /**
     * Sets the listInfoBean.<p>
     *
     * @param listInfoBean the listInfoBean to set
     */
    public void setListInfoBean(CmsListInfoBean listInfoBean) {

        m_listInfoBean = listInfoBean;
    }

    /**
     * Sets the ownProperties.<p>
     *
     * @param ownProperties the ownProperties to set
     */
    public void setOwnProperties(Map<String, CmsClientProperty> ownProperties) {

        m_ownProperties = ownProperties;
    }

    /**
     * Sets the resourceType.<p>
     *
     * @param resourceType the resourceType to set
     */
    public void setResourceType(String resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * Sets the rootPath.<p>
     *
     * @param rootPath the rootPath to set
     */
    public void setRootPath(String rootPath) {

        m_rootPath = rootPath;
    }

    /**
     * Sets the sitePath.<p>
     *
     * @param sitePath the sitePath to set
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the structureId.<p>
     *
     * @param structureId the structureId to set
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }
}

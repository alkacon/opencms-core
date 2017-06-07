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

package org.opencms.gwt.shared.property;

import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A bean containing the information needed to edit the properties of a resource.<p>
 *
 * @since 8.0.0
 */
public class CmsPropertiesBean implements IsSerializable {

    /** A list of all property names. */
    private List<String> m_allProperties;

    /** The map of inherited properties of the resource. */
    private Map<String, CmsClientProperty> m_inheritedProperties;

    /** Indicates if the resource is a container page. */
    private boolean m_isContainerPage;

    /** Indicates if the resource is a folder. */
    private boolean m_isFolder;

    /** A map of the resource's own properties. */
    private Map<String, CmsClientProperty> m_ownProperties;

    /** Page information for displaying a list item representing the resource. */
    private CmsListInfoBean m_pageInfo;

    /** A map of configured properties. */
    private Map<String, CmsXmlContentProperty> m_propertyDefinitions;

    /** The site path of the resource. */
    private String m_sitePath;

    /** The structure id of the resource. */
    private CmsUUID m_structureId;

    /** The map of available templates. */
    private Map<String, CmsClientTemplateBean> m_templates;

    /** Flag which indicates whether the properties are read-only. */
    private boolean m_isReadOnly;

    /**
     * Creates a new instance.<p>
     */
    public CmsPropertiesBean() {

        // do nothing
    }

    /**
     * Returns a list of all property names.<p>
     *
     * @return a list of all property names
     */
    public List<String> getAllProperties() {

        return m_allProperties;
    }

    /**
     * Returns a map of the inherited properties.<p>
     *
     * @return a map of inherited properties
     */
    public Map<String, CmsClientProperty> getInheritedProperties() {

        return m_inheritedProperties;
    }

    /**
     * Gets a map of the resource's own properties.<p>
     *
     * @return the resource's own properties
     */
    public Map<String, CmsClientProperty> getOwnProperties() {

        return m_ownProperties;
    }

    /**
     * Gets the list info bean for the resource.<p>
     *
     * @return a list info bean
     */
    public CmsListInfoBean getPageInfo() {

        return m_pageInfo;
    }

    /**
     * Gets a map of the configured properties.<p>
     *
     * @return the configured properties as a map
     */
    public Map<String, CmsXmlContentProperty> getPropertyDefinitions() {

        return m_propertyDefinitions;
    }

    /**
     * Gets the site path of the resource.<p>
     *
     * @return the site path of the resource
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Gets the structure id of the resource.<p>
     *
     * @return the structure id of the resource
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Gets a map of the available templates.<p>
     *
     * @return the available templates
     */
    public Map<String, CmsClientTemplateBean> getTemplates() {

        return m_templates;
    }

    /**
     * Returns the if the resource is a container page.<p>
     *
     * @return the if the resource is a container page
     */
    public boolean isContainerPage() {

        return m_isContainerPage;
    }

    /**
     * Returns the if the resource is a folder.<p>
     *
     * @return the if the resource is a folder
     */
    public boolean isFolder() {

        return m_isFolder;
    }

    /**
     * If true, the properties can't be modified.<p>
     *
     * @return true if the properties can't be modified
     */
    public boolean isReadOnly() {

        return m_isReadOnly;
    }

    /**
     * Sets the list of property names.<p>
     *
     * @param allProperties the list of property names
     */
    public void setAllProperties(List<String> allProperties) {

        m_allProperties = allProperties;
    }

    /**
     * Sets if the resource is a container page.<p>
     *
     * @param isContainerPage <code>true</code> if the resource is a container page
     */
    public void setContainerPage(boolean isContainerPage) {

        m_isContainerPage = isContainerPage;
    }

    /**
     * Sets if the resource is a folder.<p>
     *
     * @param isFolder <code>true</code> if the resource is a folder
     */
    public void setFolder(boolean isFolder) {

        m_isFolder = isFolder;
    }

    /**
     * Sets the inherited properties.<p>
     *
     * @param inheritedProperties the inherited properties
     */
    public void setInheritedProperties(Map<String, CmsClientProperty> inheritedProperties) {

        m_inheritedProperties = inheritedProperties;
    }

    /**
     * Sets the resource's own properties.<p>
     *
     * @param ownProperties the resource's own properties
     */
    public void setOwnProperties(Map<String, CmsClientProperty> ownProperties) {

        m_ownProperties = ownProperties;
    }

    /**
     * Sets the page info.<p>
     *
     * @param pageInfo the page info
     */
    public void setPageInfo(CmsListInfoBean pageInfo) {

        m_pageInfo = pageInfo;
    }

    /**
     * Sets the property configuration.<p>
     *
     * @param propertyDefinitions the property configuration
     */
    public void setPropertyDefinitions(Map<String, CmsXmlContentProperty> propertyDefinitions) {

        m_propertyDefinitions = propertyDefinitions;
    }

    /**
     * Sets "readonly mode".<p>
     *
     * @param isReadOnly true if "readonly mode" should be enabled
     */
    public void setReadOnly(boolean isReadOnly) {

        m_isReadOnly = isReadOnly;
    }

    /**
     * Sets the site path.<p>
     * @param sitePath
     */
    public void setSitePath(String sitePath) {

        m_sitePath = sitePath;
    }

    /**
     * Sets the structure id.<p>
     *
     * @param structureId the structure id
     */
    public void setStructureId(CmsUUID structureId) {

        m_structureId = structureId;
    }

    /**
     * Sets the available templates.<p>
     *
     * @param templates the available templates
     */
    public void setTemplates(Map<String, CmsClientTemplateBean> templates) {

        m_templates = templates;
    }
}

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

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Data used for the property editor in the sitemap editor's locale comparison view.<p>
 */
public class CmsLocaleComparePropertyData implements IsSerializable {

    /** The default file id. */
    private CmsUUID m_defaultFileId;

    /** The default file properties. */
    private Map<String, CmsClientProperty> m_defaultFileProperties;

    /** The forbidden URL names. */
    private List<String> m_forbiddenUrlNames;

    /** True if the entry has an editable file name. */
    private boolean m_hasEditableName;

    /** The id. */
    private CmsUUID m_id;

    /** The inherited properties. */
    private Map<String, CmsClientProperty> m_inheritedProperties;

    /** True if the entry is a folder. */
    private boolean m_isFolder;

    /** The current file name. */
    private String m_name;

    /** The folder properties. */
    private Map<String, CmsClientProperty> m_ownProperties;

    /** The path. */
    private String m_path;

    /**
     * Default constructor.<p>
     *
     * Creates a new, empty instance.
     */
    public CmsLocaleComparePropertyData() {
        // do nothing

    }

    /**
     * Returns the defaultFileId.<p>
     *
     * @return the defaultFileId
     */
    public CmsUUID getDefaultFileId() {

        return m_defaultFileId;
    }

    /**
     * Returns the defaultFileProperties.<p>
     *
     * @return the defaultFileProperties
     */
    public Map<String, CmsClientProperty> getDefaultFileProperties() {

        return m_defaultFileProperties;
    }

    /**
     * Gets the list of forbidden url names.<p>
     *
     * @return the list of forbidden url names
     */
    public List<String> getForbiddenUrlNames() {

        return m_forbiddenUrlNames;
    }

    /**
     * Gets the structure id of the entry to edit.<p>
     *
     * @return the structure id of the entry to edit
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Gets the inherited property with the given name , or null if none exist.<p>
     *
     * @param name the property name
     * @return the inherited property
     */
    public CmsClientProperty getInheritedProperty(String name) {

        return m_inheritedProperties.get(name);
    }

    /**
     * Gets the current name.<p>
     *
     * @return the current name
     */
    public String getName() {

        return m_name;
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
     * Returns the path.<p>
     *
     * @return the path
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns tree if the entry has an editable name.<p>
     *
     * @return true if the entry has an editable name
     */
    public boolean hasEditableName() {

        return m_hasEditableName;
    }

    /**
     * Returns the isFolder.<p>
     *
     * @return the isFolder
     */
    public boolean isFolder() {

        return m_isFolder;
    }

    /**
     * Sets the defaultFileId.<p>
     *
     * @param defaultFileId the defaultFileId to set
     */
    public void setDefaultFileId(CmsUUID defaultFileId) {

        m_defaultFileId = defaultFileId;
    }

    /**
     * Sets the defaultFileProperties.<p>
     *
     * @param defaultFileProperties the defaultFileProperties to set
     */
    public void setDefaultFileProperties(Map<String, CmsClientProperty> defaultFileProperties) {

        m_defaultFileProperties = defaultFileProperties;
    }

    /**
     * Sets the isFolder.<p>
     *
     * @param isFolder the isFolder to set
     */
    public void setFolder(boolean isFolder) {

        m_isFolder = isFolder;
    }

    /**
     * Sets the forbiddenUrlNames.<p>
     *
     * @param forbiddenUrlNames the forbiddenUrlNames to set
     */
    public void setForbiddenUrlNames(List<String> forbiddenUrlNames) {

        m_forbiddenUrlNames = forbiddenUrlNames;
    }

    /**
     * Enables / disables editing of the file name.<p>
     *
     * @param editable true if the file name should be editable
     */
    public void setHasEditableName(boolean editable) {

        m_hasEditableName = editable;
    }

    /**
     * Sets the structure id of the entry to edit.<p>
     *
     * @param id the id
     */
    public void setId(CmsUUID id) {

        m_id = id;
    }

    /**
     * Sets the inherited properties.<p>
     *
     * @param inheritedProps the inherited properties
     */
    public void setInheritedProperties(Map<String, CmsClientProperty> inheritedProps) {

        m_inheritedProperties = inheritedProps;

    }

    /**
     * Sets the name.<p>
     *
     * @param name the name
     */
    public void setName(String name) {

        m_name = name;
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
     * Sets the path.<p>
     *
     * @param path the path to set
     */
    public void setPath(String path) {

        m_path = path;
    }

}

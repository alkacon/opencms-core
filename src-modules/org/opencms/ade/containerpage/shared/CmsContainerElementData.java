/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/shared/Attic/CmsContainerElementData.java,v $
 * Date   : $Date: 2010/10/12 06:55:30 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.shared;

import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bean holding all element information including it's formatted contents.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsContainerElementData extends CmsContainerElement {

    /** The element client id. */
    private String m_clientId;

    /** The contents by container type. */
    private Map<String, String> m_contents;

    /** The sub-container description. */
    private String m_description;

    /** The full site path. */
    private String m_sitePath;

    /** Flag for indicating whether this is a subcontainer. */
    private boolean m_isSubContainer;

    /** The last user modifying the element. */
    private String m_lastModifiedByUser;

    /** The date of last modification. */
    private long m_lastModifiedDate;

    /** The element navText property. */
    private String m_navText;

    /** The no edit reason. If empty editing is allowed. */
    private String m_noEditReason;

    /** The properties for this container entry. */
    private Map<String, String> m_properties;

    /** The property for this container element. */
    private Map<String, CmsXmlContentProperty> m_propertyConfig;

    /** The resource status. */
    private char m_status;

    /** The contained sub-item id's. */
    private List<String> m_subItems;

    /** The element title property. */
    private String m_title;

    /** The supported container types of a sub-container. */
    private Set<String> m_types;

    /**
     * Returns the supported container types.<p>
     *
     * @return the supported container types
     */
    public Set<String> getTypes() {

        return m_types;
    }

    /**
     * Sets the supported container types.<p>
     *
     * @param types the supported container types to set
     */
    public void setTypes(Set<String> types) {

        m_types = types;
    }

    /**
     * Returns the contents.<p>
     *
     * @return the contents
     */
    public Map<String, String> getContents() {

        return m_contents;
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
     * Returns the last modifying user.<p>
     *
     * @return the last modifying user
     */
    public String getLastModifiedByUser() {

        return m_lastModifiedByUser;
    }

    /**
     * Returns the last modification date.<p>
     *
     * @return the last modification date
     */
    public long getLastModifiedDate() {

        return m_lastModifiedDate;
    }

    /**
     * Returns the navText.<p>
     *
     * @return the navText
     */
    public String getNavText() {

        return m_navText;
    }

    /**
     * Returns the no edit reason. If empty editing is allowed.<p>
     *
     * @return the no edit reason
     */
    public String getNoEditReason() {

        return m_noEditReason;
    }

    /**
     * Returns the properties for this container element.<p>
     * 
     * @return a map of properties
     */
    public Map<String, String> getProperties() {

        return m_properties;
    }

    /**
     * Gets the property configuration for this container element.<p>
     * 
     * @return the property configuration map 
     */
    public Map<String, CmsXmlContentProperty> getPropertyConfig() {

        return m_propertyConfig;
    }

    /**
     * Returns the status.<p>
     *
     * @return the status
     */
    public char getStatus() {

        return m_status;
    }

    /**
     * Returns the sub-items.<p>
     *
     * @return the sub-items
     */
    public List<String> getSubItems() {

        return m_subItems;
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
     * Returns if the element is a sub-container.<p>
     *
     * @return <code>true</code> if the element is a sub-container
     */
    public boolean isSubContainer() {

        if (m_subItems == null) {
            m_subItems = new ArrayList<String>();
        }
        return m_isSubContainer;
    }

    /**
     * Sets the contents.<p>
     *
     * @param contents the contents to set
     */
    public void setContents(Map<String, String> contents) {

        m_contents = contents;
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
     * Sets the modifying userdByUser.<p>
     *
     * @param lastModifiedByUser the last modifying user to set
     */
    public void setLastModifiedByUser(String lastModifiedByUser) {

        m_lastModifiedByUser = lastModifiedByUser;
    }

    /**
     * Sets the last modification date.<p>
     *
     * @param lastModifiedDate the last modification date to set
     */
    public void setLastModifiedDate(long lastModifiedDate) {

        m_lastModifiedDate = lastModifiedDate;
    }

    /**
     * Sets the navText.<p>
     *
     * @param navText the navText to set
     */
    public void setNavText(String navText) {

        m_navText = navText;
    }

    /**
     * Sets the no edit reason.<p>
     *
     * @param noEditReason the no edit reason to set
     */
    public void setNoEditReason(String noEditReason) {

        m_noEditReason = noEditReason;
    }

    /**
     * Sets the properties for this container element.<p>
     * 
     * @param properties the new properties
     */
    public void setProperties(Map<String, String> properties) {

        m_properties = properties;
    }

    /**
     * Sets the property configuration of this container element.<p>
     * 
     * @param propConfig the new property configuration 
     */
    public void setPropertyConfig(Map<String, CmsXmlContentProperty> propConfig) {

        m_propertyConfig = propConfig;
    }

    /**
     * Sets the status.<p>
     *
     * @param status the status to set
     */
    public void setStatus(char status) {

        m_status = status;
    }

    /**
     * Sets whether the element is a sub-container.<p>
     *
     * @param isSubContainer <code>true</code> if the element is a sub-container
     */
    public void setSubContainer(boolean isSubContainer) {

        m_isSubContainer = isSubContainer;
    }

    /**
     * Sets the sub-items.<p>
     *
     * @param subItems the sub-items to set
     */
    public void setSubItems(List<String> subItems) {

        m_subItems = subItems;
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("Title: ").append(m_title).append("  File: ").append(m_sitePath).append("  ClientId: ").append(
            m_clientId);
        return result.toString();
    }

}

/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/shared/Attic/CmsContainerElementData.java,v $
 * Date   : $Date: 2011/04/21 10:30:33 $
 * Version: $Revision: 1.5 $
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

import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Bean holding all element information including it's formatted contents.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public class CmsContainerElementData extends CmsContainerElement {

    /** The element client id. */
    private String m_clientId;

    /** The contents by container type. */
    private Map<String, String> m_contents;

    /** The group-container description. */
    private String m_description;

    /** Flag for indicating whether this is a group-container. */
    private boolean m_isGroupContainer;

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

    /** The full site path. */
    private String m_sitePath;

    /** The resource status. */
    private char m_status;

    /** The contained sub-item id's. */
    private List<String> m_subItems;

    /** The element title property. */
    private String m_title;

    /** The supported container types of a group-container. */
    private Set<String> m_types;

    /** 
     * Indicates if the current user has view permissions on the element resource. 
     * Without view permissions, the element can neither be edited, nor moved. 
     **/
    private boolean m_viewPermission;

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
     * Returns the individual element properties formated with nice-names to be used as additional-info.<p>
     * 
     * @return the properties map
     */
    public Map<String, CmsPair<String, String>> getFormatedIndividualProperties() {

        Map<String, CmsPair<String, String>> result = new HashMap<String, CmsPair<String, String>>();
        if (m_properties != null) {
            for (Entry<String, String> propertyEntry : m_properties.entrySet()) {
                String propertyKey = propertyEntry.getKey();
                if (m_propertyConfig.containsKey(propertyEntry.getKey())) {
                    String niceName = m_propertyConfig.get(propertyEntry.getKey()).getNiceName();
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_propertyConfig.get(propertyEntry.getKey()).getNiceName())) {
                        propertyKey = niceName;
                    }
                }
                result.put(propertyKey, new CmsPair<String, String>(propertyEntry.getValue(), null));
            }
        }
        return result;
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
     * Returns the supported container types.<p>
     *
     * @return the supported container types
     */
    public Set<String> getTypes() {

        return m_types;
    }

    /**
     * Returns if the current user has view permissions for the element resource.<p>
     *
     * @return <code>true</code> if the current user has view permissions for the element resource
     */
    public boolean hasViewPermission() {

        return m_viewPermission;
    }

    /**
     * Returns if the element is a group-container.<p>
     *
     * @return <code>true</code> if the element is a group-container
     */
    public boolean isGroupContainer() {

        if (m_subItems == null) {
            m_subItems = new ArrayList<String>();
        }
        return m_isGroupContainer;
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
     * Sets whether the element is a group-container.<p>
     *
     * @param isGroupContainer <code>true</code> if the element is a group-container
     */
    public void setGroupContainer(boolean isGroupContainer) {

        m_isGroupContainer = isGroupContainer;
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
     * Sets the supported container types.<p>
     *
     * @param types the supported container types to set
     */
    public void setTypes(Set<String> types) {

        m_types = types;
    }

    /**
     * Sets if the current user has view permissions for the element resource.<p>
     *
     * @param viewPermission the view permission to set
     */
    public void setViewPermission(boolean viewPermission) {

        m_viewPermission = viewPermission;
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

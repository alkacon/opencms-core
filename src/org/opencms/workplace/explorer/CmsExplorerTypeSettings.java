/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorerTypeSettings.java,v $
 * Date   : $Date: 2004/08/19 11:26:34 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.explorer;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Holds all information to build the explorer context menu of a resource type 
 * and information for the new resource dialog.<p>
 * 
 * Objects of this type are sorted by their order value which specifies the order
 * in the new resource dialog.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.3.3
 */
public class CmsExplorerTypeSettings implements Comparable {
    
    private Map m_accessControl;  
    private CmsAccessControlList m_accessControlList;
    private CmsExplorerContextMenu m_contextMenu;
    
    private List m_contextMenuEntries;  
    private String m_icon;
    
    private boolean m_isResourceType;
    private String m_key;
    
    private String m_name;
    private Integer m_newResourceOrder;
    
    private String m_newResourceUri;
    private List m_properties;
    
    private boolean m_propertiesEnabled;
    private boolean m_showNavigation;
    
    /**
     * Default constructor.<p>
     */
    public CmsExplorerTypeSettings() {
        m_accessControl = new HashMap();
        m_accessControlList = new CmsAccessControlList();
        m_properties = new ArrayList();
        m_contextMenuEntries = new ArrayList();
        m_contextMenu = new CmsExplorerContextMenu();
        m_isResourceType = false;
        m_propertiesEnabled = false;
        m_showNavigation = false;
    }
    
    /** 
     * Adds a single access entry to the map of access entries of the explorer type setting.<p>
     * 
     * This stores the configuration data in a map which is used in the initialize process 
     * to create the access control list.<p> 
     * 
     * @param key the principal of the ace
     * @param value the permissions for the principal
     */
    public void addAccessEntry(String key, String value) {
        m_accessControl.put(key, value);
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Adding entry: " + key + ", " + value);
        }      
    }
    
    /**
     * Adds a single context menu entry to the list of context menu items.<p>
     * 
     * @param key the key of the current entry 
     * @param uri the dialog URI to call with the current entry
     * @param rules the display rules
     * @param target the frame target of the menu entry
     * @param order the sort order of the current entry
     * @param isXml true, if the used dialog uses the legacy XMLTemplate mechanism
     */
    public void addContextMenuEntry(String key, String uri, String rules, String target, String order, String isXml) {
        boolean isXmlValue = Boolean.valueOf(isXml).booleanValue();
        Integer orderValue = new Integer(0);
        try {
            orderValue = Integer.valueOf(order);
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Wrong order for context menu entry: " + key);
            }      
        }
        CmsExplorerContextMenuItem item = new CmsExplorerContextMenuItem(CmsExplorerContextMenuItem.C_TYPE_ENTRY, key, uri, rules, target, orderValue, isXmlValue);
        m_contextMenuEntries.add(item);
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Adding menu entry: " + key + ", " + order);
        }      
    }
    
    /**
     * Adds a menu separator to the list of context menu items.<p>
     * 
     * @param order the sort order of the separator
     */
    public void addContextMenuSeparator(String order) {
        Integer orderValue = new Integer(0);
        try {
            orderValue = Integer.valueOf(order);
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Wrong order for context menu separator.");
            }      
        }
        CmsExplorerContextMenuItem item = new CmsExplorerContextMenuItem(CmsExplorerContextMenuItem.C_TYPE_SEPARATOR, null, null, null, null, orderValue, false);
        m_contextMenuEntries.add(item);
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Adding menu separator: " + order);
        }     
    }
    
    /**
     * Adds a property definition name to the list of editable properties.<p>
     * 
     * @param propertyName the name of the property definition to add
     * @return true if the property definition was added properly
     */
    public boolean addProperty(String propertyName) {
        if (propertyName != null && !"".equals(propertyName.trim())) {
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Adding property: " + propertyName);
            }     
            return m_properties.add(propertyName);
        } else {
            return false;
        }
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (o instanceof CmsExplorerTypeSettings) {
            return m_newResourceOrder.compareTo(Integer.valueOf(((CmsExplorerTypeSettings)o).getNewResourceOrder()));
        }
        return 0;
    }
    
    /** 
     * Creates the access control list from the temporary map.<p> 
     * 
     * @param cms the CmsObject
     * @throws CmsException if reading a group or user fails
     */
    public void createAccessControlList(CmsObject cms) throws CmsException {
        m_accessControlList = new CmsAccessControlList();
        Iterator i = m_accessControl.keySet().iterator();
        while (i.hasNext()) {
            String key = (String)i.next();
            String value = (String)m_accessControl.get(key);
            CmsUUID principalId = new CmsUUID();
            // get the principal name from the principal String
            String principal = key.substring(key.indexOf(".") + 1, key.length());
    
            if (key.startsWith(I_CmsPrincipal.C_PRINCIPAL_GROUP)) {
                // read the group
                principal = OpenCms.getImportExportManager().translateGroup(principal);  
                principalId = cms.readGroup(principal).getId();
            } else {
                // read the user
                principal = OpenCms.getImportExportManager().translateUser(principal);  
                principalId = cms.readUser(principal).getId();
            }
            // create a new entry for the principal
            CmsAccessControlEntry entry = new CmsAccessControlEntry(null, principalId , value);
            m_accessControlList.add(entry);
        }
    }
    
    /**
     * Adds all context menu entries to the context menu object.<p>
     * 
     * This method has to be called when all context menu entries have been 
     * added to the list of entries.<p>
     */
    public void createContextMenu() {
        m_contextMenu.addEntries(getContextMenuEntries());
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Creating context menu for " + getName() + ".");
        }     
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (! (o instanceof CmsExplorerTypeSettings)) {
            return false;
        }
        CmsExplorerTypeSettings other = (CmsExplorerTypeSettings)o;
        return getName().equals(other.getName());
    }
     
    /**
     * Returns the list of access control entries of the explorer type setting.<p>
     * 
     * @return the list of access control entries of the explorer type setting
     */
    public CmsAccessControlList getAccessControlList() {
        return m_accessControlList;
    }
    
    /**
     * Returns the map of access entries of the explorer type setting.<p>
     * 
     * @return the map of access entries of the explorer type setting
     */
    public Map getAccessEntries() {
        return m_accessControl;
    }
    
    /**
     * Returns the context menu.<p>
     * @return the context menu
     */
    public CmsExplorerContextMenu getContextMenu() {
        return m_contextMenu;
    }

    /**
     * Returns the list of context menu entries of the explorer type setting.<p>
     * 
     * @return the list of context menu entries of the explorer type setting
     */
    public List getContextMenuEntries() {
        return m_contextMenuEntries;
    }
    
    /**
     * Returns the icon path and file name of the explorer type setting.<p>
     * 
     * @return the icon path and file name of the explorer type setting
     */
    public String getIcon() {
        return m_icon;
    }
    
    /**
     * Returns the key name of the explorer type setting.<p>
     * 
     * @return the key name of the explorer type setting
     */
    public String getKey() {
        return m_key;
    }
    
    /**
     * Returns the name of the explorer type setting.<p>
     * 
     * @return the name of the explorer type setting
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Returns the order for the new resource dialog of the explorer type setting.<p>
     * 
     * @return the order for the new resource dialog of the explorer type setting
     */
    public String getNewResourceOrder() {
        return "" + m_newResourceOrder;
    }
    
    /**
     * Returns the URI for the new resource dialog of the explorer type setting.<p>
     * 
     * @return the URI for the new resource dialog of the explorer type setting
     */
    public String getNewResourceUri() {
        return m_newResourceUri;
    }

    /**
     * Returns the list of properties of the explorer type setting.<p>
     * @return the list of properties of the explorer type setting
     */
    public List getProperties() {
        return m_properties;
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Returns if this explorer type setting uses a special properties dialog.<p>
     * 
     * @return true, if this explorer type setting uses a special properties dialog
     */
    public boolean isPropertiesEnabled() {
        return m_propertiesEnabled;
    }
    
    /**
     * Returns true if this settings object is mapped to an existing resource type.<p>
     *  
     * @return true, if this settings object is mapped to an existing resource type, otherwise false
     */
    public boolean isResourceType() {
        return m_isResourceType;
    }

    /**
     * Returns if this explorer type setting displays the navigation properties in the special properties dialog.<p>
     * 
     * @return true, if this explorer type setting displays the navigation properties in the special properties dialog
     */
    public boolean isShowNavigation() {
        return m_showNavigation;
    }

    /**
     * Sets the list of context menu entries of the explorer type setting.<p>
     * 
     * @param entries the list of context menu entries of the explorer type setting
     */
    public void setContextMenuEntries(List entries) {
        m_contextMenuEntries = entries;
    }
    
    /**
     * Sets the icon path and file name of the explorer type setting.<p>
     * 
     * @param icon the icon path and file name of the explorer type setting
     */
    public void setIcon(String icon) {
        m_icon = icon;
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Setting icon: " + m_icon);
        }      
    }
    
    /** 
     * Sets the flag if this settings object is mapped to an existing resource type.<p>
     * 
     * This is determined by the presence of the &lt;editoptions&gt; node in the Cms workplace configuration.<p>
     */
    public void setIsResourceType() {
        m_isResourceType = true;
    }
    
    /**
     * Sets the key name of the explorer type setting.<p>
     * 
     * @param key the key name of the explorer type setting
     */
    public void setKey(String key) {
        m_key = key;
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Setting key: " + m_key);
        }      
    }
    
    /**
     * Sets the name of the explorer type setting.<p>
     * 
     * @param name the name of the explorer type setting
     */
    public void setName(String name) {
        m_name = name;
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Setting name: " + m_name);
        }      
    }
    
    /**
     * Sets the order for the new resource dialog of the explorer type setting.<p>
     * 
     * @param newResourceOrder the order for the new resource dialog of the explorer type setting
     */
    public void setNewResourceOrder(String newResourceOrder) {
        try {
            m_newResourceOrder = Integer.valueOf(newResourceOrder);
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Setting new resource order: " + newResourceOrder);
            }     
        } catch (Exception e) {
            // can usually be ignored
            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(e);
            }            
            m_newResourceOrder = new Integer(0);
        }
    }
    
    /**
     * Sets the URI for the new resource dialog of the explorer type setting.<p>
     * 
     * @param newResourceUri the URI for the new resource dialog of the explorer type setting
     */
    public void setNewResourceUri(String newResourceUri) {
        m_newResourceUri = newResourceUri;
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Setting new resource uri: " + newResourceUri);
        }     
    }

    /**
     * Sets the list of properties of the explorer type setting.<p>
     * 
     * @param properties the list of properties of the explorer type setting
     */
    public void setProperties(List properties) {
        m_properties = properties;
    }

    /**
     * Sets if this explorer type setting uses a special properties dialog.<p>
     * 
     * @param enabled true, if this explorer type setting uses a special properties dialog
     */
    public void setPropertiesEnabled(boolean enabled) {
        m_propertiesEnabled = enabled;
    }
    
    /**
     * Sets the dfault settings for the property display dialog.<p>
     * 
     * @param enabled true, if this explorer type setting uses a special properties dialog
     * @param showNavigation true, if this explorer type setting displays the navigation properties in the special properties dialog
     */
    public void setPropertyDefaults(String enabled, String showNavigation) {
        setPropertiesEnabled(Boolean.valueOf(enabled).booleanValue());
        setShowNavigation(Boolean.valueOf(showNavigation).booleanValue());
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Setting property defaults: " + enabled + ", " + showNavigation);
        }     
    }

    /**
     * Sets if this explorer type setting displays the navigation properties in the special properties dialog.<p>
     * 
     * @param navigation true, if this explorer type setting displays the navigation properties in the special properties dialog
     */
    public void setShowNavigation(boolean navigation) {
        m_showNavigation = navigation;
    }
    
    /**
     * Sets the basic attributes of the type settings.<p>
     * 
     * @param name the name of the type setting
     * @param key the key name of the explorer type setting 
     * @param icon the icon path and file name of the explorer type setting
     */
    public void setTypeAttributes(String name, String key, String icon) {
        setName(name);
        setKey(key);
        setIcon(icon);
    }

}

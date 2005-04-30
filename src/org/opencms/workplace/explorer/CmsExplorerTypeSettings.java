/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorerTypeSettings.java,v $
 * Date   : $Date: 2005/04/30 11:15:38 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all information to build the explorer context menu of a resource type 
 * and information for the new resource dialog.<p>
 * 
 * Objects of this type are sorted by their order value which specifies the order
 * in the new resource dialog.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.6 $
 * 
 * @since 5.3.3
 */
public class CmsExplorerTypeSettings implements Comparable {
    
    private CmsExplorerTypeAccess m_access;
    private CmsExplorerContextMenu m_contextMenu;
    
    private List m_contextMenuEntries;  
    
    private boolean m_hasEditOptions;
    private String m_icon;
    private String m_key;
    
    private String m_name;
    private Integer m_newResourceOrder;
    private String m_newResourcePage;
    private String m_newResourceUri;
    private List m_properties;
    
    private boolean m_propertiesEnabled;
    private String m_reference;
   
    private boolean m_showNavigation;
    
    /** Flag for showing that this is an additional resource type which defined in a module. */
    private boolean m_addititionalModuleExplorerType;    
    
    /**
     * Default constructor.<p>
     */
    public CmsExplorerTypeSettings() {
        m_access = new CmsExplorerTypeAccess();
        m_properties = new ArrayList();
        m_contextMenuEntries = new ArrayList();
        m_contextMenu = new CmsExplorerContextMenu();
        m_hasEditOptions = false;
        m_propertiesEnabled = false;
        m_showNavigation = false;
        m_addititionalModuleExplorerType = false;
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
     * Adds all context menu entries to the context menu object.<p>
     * 
     * This method has to be called when all context menu entries have been 
     * added to the list of entries.<p>
     */
    public void createContextMenu() {
        m_contextMenu.addEntries(getContextMenuEntries());        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Creating context menu for " + getName() + '.');
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
     * Gets the access object of the type settings.<p>
     * 
     * @return access object of the type settings
     */
    public CmsExplorerTypeAccess getAccess() {
        if (m_access.isEmpty()) {
            CmsWorkplaceManager workplaceManager = OpenCms.getWorkplaceManager();
            if (workplaceManager != null) {
                m_access = workplaceManager.getDefaultAccess();
            }
        }
        return m_access;        
    }
         
    /**
     * Returns the context menu.<p>
     * @return the context menu
     */
    public CmsExplorerContextMenu getContextMenu() {
        if ((m_reference != null) && (m_contextMenu.isEmpty())) {
            m_contextMenu = (CmsExplorerContextMenu)OpenCms.getWorkplaceManager().getExplorerTypeSetting(m_reference).getContextMenu().clone();
        }
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
     * Returns the page.<p>
     *
     * @return the page
     */
    public String getNewResourcePage() {

        return m_newResourcePage;
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
     * Returns the reference of the explorer type setting.<p>
     * 
     * @return the reference of the explorer type setting
     */
    public String getReference() {
        return m_reference;
    }
    
    /**
     * Returns true if this explorer type entry has explicit edit options set.<p>
     *  
     * @return true if this explorer type entry has explicit edit options set
     */
    public boolean hasEditOptions() {        
        return m_hasEditOptions;
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
     * Returns if this explorer type setting displays the navigation properties in the special properties dialog.<p>
     * 
     * @return true, if this explorer type setting displays the navigation properties in the special properties dialog
     */
    public boolean isShowNavigation() {
        return m_showNavigation;
    }
    
    /**
     * Sets the access object of the type settings.<p>
     * 
     * @param access access object
     */
    public void setAccess (CmsExplorerTypeAccess access) {
        m_access = access;
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
     * Sets the flag if this explorer type entry has explicit edit options set.<p>
     * 
     * This is determined by the presence of the &lt;editoptions&gt; node in the Cms workplace configuration.<p>
     */
    public void setEditOptions() {
        m_hasEditOptions = true;
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
     * Sets the page.<p>
     *
     * @param page the page to set
     */
    public void setNewResourcePage(String page) {

        m_newResourcePage = page;
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
     * Sets the reference of the explorer type setting.<p>
     * 
     * @param reference the reference of the explorer type setting
     */
    public void setReference(String reference) {
        m_reference = reference;
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Setting reference: " + m_reference);
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

    /**
     * Sets the basic attributes of the type settings.<p>
     * 
     * @param name the name of the type setting
     * @param key the key name of the explorer type setting 
     * @param icon the icon path and file name of the explorer type setting
     * @param reference the reference of the explorer type setting
     */
    public void setTypeAttributes(String name, String key, String icon, String reference) {
        setName(name);
        setKey(key);
        setIcon(icon);        
        setReference(reference);
    }
    
    /**
     * Indicates that this is an additional explorer type which is defined in a module.<p>
     * 
     * @return true or false
     */
    public boolean isAddititionalModuleExplorerType() {

        return m_addititionalModuleExplorerType;
    }
    
    /**
     * Sets the additional explorer type flag.<p>
     * 
     * @param addititionalModuleExplorerType true or false
     */
    public void setAddititionalModuleExplorerType(boolean addititionalModuleExplorerType) {

        m_addititionalModuleExplorerType = addititionalModuleExplorerType;
    }
    
}

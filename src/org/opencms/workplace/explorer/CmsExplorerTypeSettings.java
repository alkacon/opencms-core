/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorerTypeSettings.java,v $
 * Date   : $Date: 2005/10/19 09:58:11 $
 * Version: $Revision: 1.16.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceManager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Holds all information to build the explorer context menu of a resource type 
 * and information for the new resource dialog.<p>
 * 
 * Objects of this type are sorted by their order value which specifies the order
 * in the new resource dialog.<p>
 * 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.16.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExplorerTypeSettings implements Comparable {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExplorerTypeSettings.class);

    private CmsExplorerTypeAccess m_access;

    /** Flag for showing that this is an additional resource type which defined in a module. */
    private boolean m_addititionalModuleExplorerType;
    private boolean m_autoSetNavigation;
    private boolean m_autoSetTitle;
    private CmsExplorerContextMenu m_contextMenu;
    private List m_contextMenuEntries;
    private boolean m_hasEditOptions;
    private String m_icon;
    private String m_key;
    private String m_name;

    /** Optional class name for a new resource handler. */
    private String m_newResourceHandlerClassName;
    private Integer m_newResourceOrder;
    private String m_newResourcePage;
    private String m_newResourceUri;
    private List m_properties;
    private boolean m_propertiesEnabled;
    private String m_reference;
    private boolean m_showNavigation;

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
        m_newResourceOrder = new Integer(0);
    }

    /**
     * Adds a single context menu entry to the list of context menu items.<p>
     * 
     * @param key the key of the current entry 
     * @param uri the dialog URI to call with the current entry
     * @param rules the display rules
     * @param target the frame target of the menu entry
     * @param order the sort order of the current entry
     */
    public void addContextMenuEntry(String key, String uri, String rules, String target, String order) {

        Integer orderValue = new Integer(0);
        try {
            orderValue = Integer.valueOf(order);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(Messages.LOG_WRONG_ORDER_CONTEXT_MENU_1, key));
            }
        }
        CmsExplorerContextMenuItem item = new CmsExplorerContextMenuItem(
            CmsExplorerContextMenuItem.TYPE_ENTRY,
            key,
            uri,
            rules,
            target,
            orderValue);
        
        m_contextMenuEntries.add(item);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_ADD_MENU_ENTRY_2, key, order));
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
            LOG.error(Messages.get().key(Messages.LOG_WRONG_MENU_SEP_ORDER_0, order));
        }
        CmsExplorerContextMenuItem item = new CmsExplorerContextMenuItem(
            CmsExplorerContextMenuItem.TYPE_SEPARATOR,
            null,
            null,
            null,
            null,
            orderValue);
        m_contextMenuEntries.add(item);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_WRONG_MENU_SEP_ORDER_0, order));
        }
    }

    /**
     * Adds a property definition name to the list of editable properties.<p>
     * 
     * @param propertyName the name of the property definition to add
     * @return true if the property definition was added properly
     */
    public boolean addProperty(String propertyName) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(propertyName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_ADD_PROP_1, propertyName));
            }
            return m_properties.add(propertyName);
        } else {
            return false;
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsExplorerTypeSettings) {
            CmsExplorerTypeSettings other = (CmsExplorerTypeSettings)obj;
            String myPage = getNewResourcePage();
            String otherPage = other.getNewResourcePage();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(myPage)) {
                myPage = "";
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(otherPage)) {
                otherPage = "";
            }
            int result = myPage.compareTo(otherPage);
            if (result == 0) {
                result = m_newResourceOrder.compareTo(other.m_newResourceOrder);
            }
            return result;
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
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_CREATE_CONTEXT_MENU_1, getName()));
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {

        if (!(o instanceof CmsExplorerTypeSettings)) {
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
     * Returns the class name of the new resource handler used to create new resources of a specified resource type.<p>
     * 
     * @return the class name of the new resource handler
     */
    public String getNewResourceHandlerClassName() {

        return m_newResourceHandlerClassName;
    }

    /**
     * Returns the order for the new resource dialog of the explorer type setting.<p>
     * 
     * @return the order for the new resource dialog of the explorer type setting
     */
    public String getNewResourceOrder() {

        return String.valueOf(m_newResourceOrder);
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
     * Indicates that this is an additional explorer type which is defined in a module.<p>
     * 
     * @return true or false
     */
    public boolean isAddititionalModuleExplorerType() {

        return m_addititionalModuleExplorerType;
    }

    /**
     * Returns true if navigation properties should automatically be added on resource creation.<p>
     * 
     * @return true if navigation properties should automatically be added on resource creation, otherwise false
     */
    public boolean isAutoSetNavigation() {

        return m_autoSetNavigation;
    }

    /**
     * Returns true if the title property should automatically be added on resource creation.<p>
     * 
     * @return true if the title property should automatically be added on resource creation, otherwise false
     */
    public boolean isAutoSetTitle() {

        return m_autoSetTitle;
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
    public void setAccess(CmsExplorerTypeAccess access) {

        m_access = access;
    }

    /**
     * Sets the additional explorer type flag.<p>
     * 
     * @param addititionalModuleExplorerType true or false
     */
    public void setAddititionalModuleExplorerType(boolean addititionalModuleExplorerType) {

        m_addititionalModuleExplorerType = addititionalModuleExplorerType;
    }

    /**
     * Sets if navigation properties should automatically be added on resource creation.<p>
     *
     * @param autoSetNavigation true if properties should be added, otherwise false
     */
    public void setAutoSetNavigation(String autoSetNavigation) {

        m_autoSetNavigation = Boolean.valueOf(autoSetNavigation).booleanValue();
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SET_AUTO_NAV_1, autoSetNavigation));
        }
    }

    /**
     * Sets if the title property should automatically be added on resource creation.<p>
     *
     * @param autoSetTitle true if title should be added, otherwise false
     */
    public void setAutoSetTitle(String autoSetTitle) {

        m_autoSetTitle = Boolean.valueOf(autoSetTitle).booleanValue();
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SET_AUTO_TITLE_1, autoSetTitle));
        }
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
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SET_ICON_1, icon));
        }
    }

    /**
     * Sets the key name of the explorer type setting.<p>
     * 
     * @param key the key name of the explorer type setting
     */
    public void setKey(String key) {

        m_key = key;
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SET_KEY_1, key));
        }
    }

    /**
     * Sets the name of the explorer type setting.<p>
     * 
     * @param name the name of the explorer type setting
     */
    public void setName(String name) {

        m_name = name;
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SET_NAME_1, name));
        }
    }

    /**
     * Sets the class name of the new resource handler used to create new resources of a specified resource type.<p>
     * 
     * @param newResourceHandlerClassName the class name of the new resource handler
     */
    public void setNewResourceHandlerClassName(String newResourceHandlerClassName) {

        m_newResourceHandlerClassName = newResourceHandlerClassName;
    }

    /**
     * Sets the order for the new resource dialog of the explorer type setting.<p>
     * 
     * @param newResourceOrder the order for the new resource dialog of the explorer type setting
     */
    public void setNewResourceOrder(String newResourceOrder) {

        try {
            m_newResourceOrder = Integer.valueOf(newResourceOrder);
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_SET_NEW_RESOURCE_ORDER_1, newResourceOrder));
            }
        } catch (Exception e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
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
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SET_NEW_RESOURCE_URI_1, newResourceUri));
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
     * Sets the default settings for the property display dialog.<p>
     * 
     * @param enabled true, if this explorer type setting uses a special properties dialog
     * @param showNavigation true, if this explorer type setting displays the navigation properties in the special properties dialog
     */
    public void setPropertyDefaults(String enabled, String showNavigation) {

        setPropertiesEnabled(Boolean.valueOf(enabled).booleanValue());
        setShowNavigation(Boolean.valueOf(showNavigation).booleanValue());
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SET_PROP_DEFAULTS_2, enabled, showNavigation));
        }
    }

    /**
     * Sets the reference of the explorer type setting.<p>
     * 
     * @param reference the reference of the explorer type setting
     */
    public void setReference(String reference) {

        m_reference = reference;
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SET_REFERENCE_1, m_reference));
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
}
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Holds all information to build the explorer context menu of a resource type
 * and information for the new resource dialog.<p>
 *
 * Objects of this type are sorted by their order value which specifies the order
 * in the new resource dialog.<p>
 *
 * @since 6.0.0
 */
public class CmsExplorerTypeSettings implements Comparable<CmsExplorerTypeSettings> {

    /** File name for the big default icon. */
    public static final String DEFAULT_BIG_ICON = "document_big.png";

    /** File name for the normal default icon. */
    public static final String DEFAULT_NORMAL_ICON = "document.png";

    /** The default order start value for context menu entries. */
    public static final int ORDER_VALUE_DEFAULT_START = 100000;

    /** The default order value for context menu separator entries without order attribute. */
    public static final String ORDER_VALUE_SEPARATOR_DEFAULT = "999999";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExplorerTypeSettings.class);

    /** Default view orders. */
    private static Map<String, Integer> m_defaultViewOrders = new HashMap<String, Integer>() {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        {
            put("folder", new Integer(50));
            put("plain", new Integer(200));
            put("jsp", new Integer(300));
            put("htmlredirect", new Integer(400));
            put("containerpage", new Integer(500));

            put("imagegallery", new Integer(100));
            put("downloadgallery", new Integer(200));
            put("linkgallery", new Integer(300));
            put("subsitemap", new Integer(400));
            put("content_folder", new Integer(500));
            put("formatter_config", new Integer(100));

            put("xmlvfsbundle", new Integer(200));
            put("propertyvfsbundle", new Integer(300));
            put("bundledescriptor", new Integer(350));
            put("sitemap_config", new Integer(400));
            put("sitemap_master_config", new Integer(500));
            put("module_config", new Integer(600));
            put("elementview", new Integer(700));
            put("seo_file", new Integer(800));
            put("containerpage_template", new Integer(900));
            put("inheritance_config", new Integer(1000));

            put("xmlcontent", new Integer(100));
            put("pointer", new Integer(200));

            put("modelgroup", new Integer(100));
        }
    };

    /** The explorer type access. */
    private CmsExplorerTypeAccess m_access;

    /** Flag for showing that this is an additional resource type which defined in a module. */
    private boolean m_addititionalModuleExplorerType;

    /** The auto set navigation flag. */
    private boolean m_autoSetNavigation;

    /** The auto set title flag. */
    private boolean m_autoSetTitle;

    /** The name of the big icon for this explorer type. */
    private String m_bigIcon;

    /** The context menu. */
    private CmsExplorerContextMenu m_contextMenu;

    /** The context menu entries. */
    private List<CmsExplorerContextMenuItem> m_contextMenuEntries;

    /** The description image. */
    private String m_descriptionImage;

    /** The element view for this explorer type. */
    private String m_elementView;

    /**The edit options flag. */
    private boolean m_hasEditOptions;

    /** The icon. */
    private String m_icon;

    /** The icon rules for this explorer type. */
    private Map<String, CmsIconRule> m_iconRules;

    /** The info. */
    private String m_info;

    /** Flag indicating whether this explorer type represents a view. */
    private boolean m_isView;

    /** The key. */
    private String m_key;

    /** The name. */
    private String m_name;

    /** The name pattern. */
    private String m_namePattern;

    /** Optional class name for a new resource handler. */
    private String m_newResourceHandlerClassName;

    /** The new resource order value. */
    private Integer m_newResourceOrder;

    /** The new resource page. */
    private String m_newResourcePage;

    /** The new resource URI. */
    private String m_newResourceUri;

    /** The properties. */
    private List<String> m_properties;

    /** The enabled properties. */
    private boolean m_propertiesEnabled;

    /** The reference. */
    private String m_reference;

    /** The show in navigation flag. */
    private boolean m_showNavigation;

    /** The title key. */
    private String m_titleKey;

    /** The configured view order. */
    private Integer m_viewOrder;

    /**
     * Default constructor.<p>
     */
    public CmsExplorerTypeSettings() {

        m_access = new CmsExplorerTypeAccess();
        m_properties = new ArrayList<String>();
        m_contextMenuEntries = new ArrayList<CmsExplorerContextMenuItem>();
        m_contextMenu = new CmsExplorerContextMenu();
        m_hasEditOptions = false;
        m_propertiesEnabled = false;
        m_showNavigation = false;
        m_addititionalModuleExplorerType = false;
        m_newResourceOrder = new Integer(0);
        m_iconRules = new HashMap<String, CmsIconRule>();

    }

    /**
     * Gets the default view order for the given type name (or null, if there is no default view order).<p>
     *
     * @param typeName the type name
     *
     * @return the default view order for the type
     */
    public static Integer getDefaultViewOrder(String typeName) {

        return m_defaultViewOrders.get(typeName);
    }

    /**
     * Adds a menu entry to the list of context menu items.<p>
     *
     * @param item the entry item to add to the list
     */
    public void addContextMenuEntry(CmsExplorerContextMenuItem item) {

        item.setType(CmsExplorerContextMenuItem.TYPE_ENTRY);
        m_contextMenuEntries.add(item);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_MENU_ENTRY_2, item.getKey(), item.getUri()));
        }
    }

    /**
     * Adds a menu separator to the list of context menu items.<p>
     *
     * @param item the separator item to add to the list
     */
    public void addContextMenuSeparator(CmsExplorerContextMenuItem item) {

        item.setType(CmsExplorerContextMenuItem.TYPE_SEPARATOR);
        m_contextMenuEntries.add(item);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_MENU_SEPARATOR_1, item.getType()));
        }
    }

    /**
     * Adds a new icon rule to this explorer type.<p>
     *
     * @param extension the extension for the icon rule
     * @param icon the small icon
     * @param bigIcon the big icon
     */
    public void addIconRule(String extension, String icon, String bigIcon) {

        CmsIconRule rule = new CmsIconRule(extension, icon, bigIcon);
        m_iconRules.put(extension, rule);
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
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_PROP_1, propertyName));
            }
            return m_properties.add(propertyName);
        } else {
            return false;
        }
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        CmsExplorerTypeSettings result = new CmsExplorerTypeSettings();
        result.m_access = m_access;
        result.m_properties = new ArrayList<String>(m_properties);
        result.m_contextMenuEntries = m_contextMenuEntries;
        result.m_contextMenu = (CmsExplorerContextMenu)m_contextMenu.clone();
        result.m_hasEditOptions = m_hasEditOptions;
        result.m_propertiesEnabled = m_propertiesEnabled;
        result.m_showNavigation = m_showNavigation;
        result.m_addititionalModuleExplorerType = m_addititionalModuleExplorerType;
        result.m_newResourceOrder = m_newResourceOrder;
        result.m_autoSetNavigation = m_autoSetNavigation;
        result.m_autoSetTitle = m_autoSetTitle;
        result.m_bigIcon = m_bigIcon;
        result.m_descriptionImage = m_descriptionImage;
        result.m_hasEditOptions = m_hasEditOptions;
        result.m_icon = m_icon;
        result.m_info = m_info;
        result.m_key = m_key;
        result.m_name = m_name;
        result.m_newResourceHandlerClassName = m_newResourceHandlerClassName;
        result.m_newResourcePage = m_newResourcePage;
        result.m_newResourceUri = m_newResourceUri;
        result.m_reference = m_reference;
        result.m_titleKey = m_titleKey;

        result.m_iconRules = new HashMap<String, CmsIconRule>();
        for (Map.Entry<String, CmsIconRule> rule : m_iconRules.entrySet()) {
            result.m_iconRules.put(rule.getKey(), (CmsIconRule)rule.getValue().clone());
        }

        result.m_contextMenuEntries = new ArrayList<CmsExplorerContextMenuItem>();
        for (CmsExplorerContextMenuItem entry : m_contextMenuEntries) {
            // TODO: must also be cloned
            result.m_contextMenuEntries.add(entry);
        }

        return result;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsExplorerTypeSettings other) {

        if (other == this) {
            return 0;
        }
        if (other != null) {
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
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_CREATE_CONTEXT_MENU_1, getName()));
        }
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
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
     * Returns the big icon.<p>
     *
     * @return an icon name
     */
    public String getBigIcon() {

        return m_bigIcon;
    }

    /**
     * Returns the biggest icon available.<p>
     *
     * @return the biggest icon available
     */
    public String getBigIconIfAvailable() {

        return m_bigIcon != null ? m_bigIcon : (m_icon != null ? m_icon : DEFAULT_BIG_ICON);
    }

    /**
     * Returns the context menu.<p>
     * @return the context menu
     */
    public CmsExplorerContextMenu getContextMenu() {

        if ((m_reference != null) && (m_contextMenu.isEmpty())) {
            m_contextMenu = (CmsExplorerContextMenu)OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                m_reference).getContextMenu().clone();
        }
        return m_contextMenu;
    }

    /**
     * Returns the list of context menu entries of the explorer type setting.<p>
     *
     * @return the list of context menu entries of the explorer type setting
     */
    public List<CmsExplorerContextMenuItem> getContextMenuEntries() {

        return m_contextMenuEntries;
    }

    /**
     * Returns the descriptionImage.<p>
     *
     * @return the descriptionImage
     */
    public String getDescriptionImage() {

        return m_descriptionImage;
    }

    /**
     * Gets the element view name.<p>
     *
     * @return the element view name
     */
    public String getElementView() {

        return m_elementView;
    }

    /**
     * Returns the icon path and file name of the explorer type setting.<p>
     *
     * @return the icon path and file name of the explorer type setting
     */
    public String getIcon() {

        if (m_icon != null) {

            return m_icon;
        }
        return DEFAULT_NORMAL_ICON;
    }

    /**
     * Returns a map from file extensions to icon rules for this explorer type.<p>
     *
     * @return a map from file extensions to icon rules
     */
    public Map<String, CmsIconRule> getIconRules() {

        return Collections.unmodifiableMap(m_iconRules);
    }

    /**
     * Returns the info.<p>
     *
     * @return the info
     */
    public String getInfo() {

        return m_info;
    }

    /**
     * Builds the Javascript to create the context menu.<p>
     *
     * @param settings the explorer type settings for which the context menu is created
     * @param resTypeId the id of the resource type which uses the context menu
     * @param messages the messages to generate the context menu with (should be the workplace messages)
     *
     * @return the JavaScript output to create the context menu
     */
    public String getJSEntries(CmsExplorerTypeSettings settings, int resTypeId, CmsMessages messages) {

        // entries not yet in Map, so generate them
        StringBuffer result = new StringBuffer(4096);

        // create the JS for the resource object
        result.append("\nvi.resource[").append(resTypeId).append("]=new res(\"").append(settings.getName()).append(
            "\", ");
        result.append("\"");
        result.append(messages.key(settings.getKey()));
        result.append("\", vi.skinPath + \"" + CmsWorkplace.RES_PATH_FILETYPES);
        result.append(settings.getIcon());
        result.append("\", \"");
        result.append(settings.getNewResourceUri());
        result.append("\", true);\n");

        return result.toString();
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
     * Gets the name pattern.<p>
     *
     * @return the name pattern
     */
    public String getNamePattern() {

        return m_namePattern;
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
     * Gets the original icon name from the configuration.<p>
     *
     * @return an icon name
     */
    public String getOriginalIcon() {

        return m_icon;
    }

    /**
     * Returns the list of properties of the explorer type setting.<p>
     * @return the list of properties of the explorer type setting
     */
    public List<String> getProperties() {

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
     * Returns the titleKey.<p>
     *
     * @return the titleKey
     */
    public String getTitleKey() {

        return m_titleKey;
    }

    /**
     * Gets the view order, optionally using a default value if the view order is not configured.<p>
     *
     * @param useDefault true if a default should be returned in the case where the view order is not configured
     *
     * @return the view order
     */
    public Integer getViewOrder(boolean useDefault) {

        Integer defaultViewOrder = getDefaultViewOrder(m_name);
        Integer result = null;
        if (m_viewOrder != null) {
            result = m_viewOrder;
        } else if (useDefault) {
            if (defaultViewOrder != null) {
                result = defaultViewOrder;
            } else {
                result = new Integer(9999);
            }
        }
        return result;
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
    @Override
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
     * Checks if the current user has write permissions on the given resource.<p>
     *
     * @param cms the current cms context
     * @param resource the resource to check
     *
     * @return <code>true</code> if the current user has write permissions on the given resource
     */
    public boolean isEditable(CmsObject cms, CmsResource resource) {

        if (!cms.getRequestContext().getCurrentProject().isOnlineProject()
            && OpenCms.getRoleManager().hasRole(cms, CmsRole.ROOT_ADMIN)) {
            return true;
        }
        // determine if this resource type is editable for the current user
        CmsPermissionSet permissions = getAccess().getPermissions(cms, resource);
        return permissions.requiresWritePermission();
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
     * Returns true if this explorer type represents a view.<p>
     *
     * @return true if this explorer type represents a view
     */
    public boolean isView() {

        return m_isView;
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
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SET_AUTO_NAV_1, autoSetNavigation));
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
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SET_AUTO_TITLE_1, autoSetTitle));
        }
    }

    /**
     * Sets the file name of the big icon for this explorer type.<p>
     *
     * @param bigIcon the file name of the big icon
     */
    public void setBigIcon(String bigIcon) {

        m_bigIcon = bigIcon;
    }

    /**
     * Sets the list of context menu entries of the explorer type setting.<p>
     *
     * @param entries the list of context menu entries of the explorer type setting
     */
    public void setContextMenuEntries(List<CmsExplorerContextMenuItem> entries) {

        m_contextMenuEntries = entries;
    }

    /**
     * Sets the descriptionImage.<p>
     *
     * @param descriptionImage the descriptionImage to set
     */
    public void setDescriptionImage(String descriptionImage) {

        m_descriptionImage = descriptionImage;
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(Messages.LOG_SET_NEW_RESOURCE_DESCRIPTION_IMAGE_1, descriptionImage));
        }
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
     * Sets the reference of the explorer type setting.<p>
     *
     * @param elementView the element view
     */
    public void setElementView(String elementView) {

        m_elementView = elementView;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting element view to " + elementView);

        }
    }

    /**
     * Sets the icon path and file name of the explorer type setting.<p>
     *
     * @param icon the icon path and file name of the explorer type setting
     */
    public void setIcon(String icon) {

        m_icon = icon;
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SET_ICON_1, icon));
        }
    }

    /**
     * Sets the info.<p>
     *
     * @param info the info to set
     */
    public void setInfo(String info) {

        m_info = info;
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SET_INFO_1, info));
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
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SET_KEY_1, key));
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
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SET_NAME_1, name));
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
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_SET_NEW_RESOURCE_ORDER_1, newResourceOrder));
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
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SET_NEW_RESOURCE_URI_1, newResourceUri));
        }
    }

    /**
     * Sets the list of properties of the explorer type setting.<p>
     *
     * @param properties the list of properties of the explorer type setting
     */
    public void setProperties(List<String> properties) {

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
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SET_PROP_DEFAULTS_2, enabled, showNavigation));
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
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SET_REFERENCE_1, m_reference));
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
     * Sets the titleKey.<p>
     *
     * @param titleKey the titleKey to set
     */
    public void setTitleKey(String titleKey) {

        m_titleKey = titleKey;
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SET_TITLE_KEY_1, titleKey));
        }
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
     * @param bigIcon the file name of the big icon
     * @param reference the reference of the explorer type setting
     * @param elementView the element view
     * @param isView 'true' if this type represents an element view
     * @param namePattern the name pattern
     * @param viewOrder the view order
     */
    public void setTypeAttributes(
        String name,
        String key,
        String icon,
        String bigIcon,
        String reference,
        String elementView,
        String isView,
        String namePattern,
        String viewOrder) {

        setName(name);
        setKey(key);
        setIcon(icon);
        setBigIcon(bigIcon);
        setReference(reference);
        setElementView(elementView);
        try {
            m_viewOrder = Integer.valueOf(viewOrder);
        } catch (NumberFormatException e) {
            LOG.debug("Type " + name + " has no or invalid view order:" + viewOrder);
        }
        m_isView = Boolean.valueOf(isView).booleanValue();
        m_namePattern = namePattern;

    }

}
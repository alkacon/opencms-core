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
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class CmsExplorerTypeSettings implements Comparable<CmsExplorerTypeSettings>, Serializable {

    /** The default big file type icon style class. */
    public static final String ICON_STYLE_DEFAULT_BIG = "oc-icon-24-default";

    /** The default small file type icon style class. */
    public static final String ICON_STYLE_DEFAULT_SMALL = "oc-icon-16-default";

    /** The model group reuse big file type icon style class. */
    public static final String ICON_STYLE_MODEL_GROUP_COPY_BIG = "oc-icon-24-modelgroup_copy";

    /** The model group reuse small file type icon style class. */
    public static final String ICON_STYLE_MODEL_GROUP_COPY_SMALL = "oc-icon-16-modelgroup_copy";

    /** The nav level big file type icon style class. */
    public static final String ICON_STYLE_NAV_LEVEL_BIG = "oc-icon-24-navlevel";

    /** The nav level small file type icon style class. */
    public static final String ICON_STYLE_NAV_LEVEL_SMALL = "oc-icon-16-navlevel";

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
            put("folder", Integer.valueOf(50));
            put("plain", Integer.valueOf(200));
            put("jsp", Integer.valueOf(300));
            put("htmlredirect", Integer.valueOf(400));
            put("containerpage", Integer.valueOf(500));

            put("imagegallery", Integer.valueOf(100));
            put("downloadgallery", Integer.valueOf(200));
            put("linkgallery", Integer.valueOf(300));
            put("subsitemap", Integer.valueOf(400));
            put("content_folder", Integer.valueOf(500));
            put("formatter_config", Integer.valueOf(100));

            put("xmlvfsbundle", Integer.valueOf(200));
            put("propertyvfsbundle", Integer.valueOf(300));
            put("bundledescriptor", Integer.valueOf(350));
            put("sitemap_config", Integer.valueOf(400));
            put("sitemap_master_config", Integer.valueOf(500));
            put("module_config", Integer.valueOf(600));
            put("elementview", Integer.valueOf(700));
            put("seo_file", Integer.valueOf(800));
            put("containerpage_template", Integer.valueOf(900));
            put("inheritance_config", Integer.valueOf(1000));

            put(CmsResourceTypeXmlContent.getStaticTypeName(), Integer.valueOf(100));
            put("pointer", Integer.valueOf(200));

            put("modelgroup", Integer.valueOf(100));
        }
    };

    /** The serial version id. */
    private static final long serialVersionUID = 7014251115525259136L;

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

    /** The big icon CSS style class. */
    private String m_bigIconStyle;

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

    /** The new resource order value. */
    private Integer m_newResourceOrder;

    /** The creatable flag, <code>false</code> for types that can not be created through the workplace UI. */
    private boolean m_creatable;

    /** The properties. */
    private List<String> m_properties;

    /** The enabled properties. */
    private boolean m_propertiesEnabled;

    /** The reference. */
    private String m_reference;

    /** The show in navigation flag. */
    private boolean m_showNavigation;

    /** The small icon CSS style class. */
    private String m_smallIconStyle;

    /** The title key. */
    private String m_titleKey;

    /** The configured view order. */
    private Integer m_viewOrder;

    /** Properties which are required on upload. */
    private Set<String> m_requiredOnUpload = new HashSet<>();

    /**
     * Default constructor.<p>
     */
    public CmsExplorerTypeSettings() {

        m_access = new CmsExplorerTypeAccess();
        m_properties = new ArrayList<String>();
        m_creatable = true;
        m_hasEditOptions = false;
        m_propertiesEnabled = false;
        m_showNavigation = false;
        m_addititionalModuleExplorerType = false;
        m_newResourceOrder = Integer.valueOf(0);
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
     * Adds a new icon rule to this explorer type.<p>
     *
     * @param extension the extension for the icon rule
     * @param icon the small icon
     * @param bigIcon the big icon
     * @param smallIconStyle the small icon CSS style class
     * @param bigIconStyle the big icon CSS style class
     */
    public void addIconRule(String extension, String icon, String bigIcon, String smallIconStyle, String bigIconStyle) {

        CmsIconRule rule = new CmsIconRule(extension, icon, bigIcon, smallIconStyle, bigIconStyle);
        m_iconRules.put(extension, rule);
    }

    /**
     * Adds a property definition name to the list of editable properties.<p>
     *
     * @param propertyName the name of the property definition to add
     * @param requiredOnUpload if "true", mark the property as required after upload
     * @return true if the property definition was added properly
     */
    public boolean addProperty(String propertyName, String requiredOnUpload) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(propertyName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_PROP_1, propertyName));
            }
            if (Boolean.valueOf(requiredOnUpload).booleanValue()) {
                m_requiredOnUpload.add(propertyName);
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
        result.m_addititionalModuleExplorerType = m_addititionalModuleExplorerType;
        result.m_autoSetNavigation = m_autoSetNavigation;
        result.m_autoSetTitle = m_autoSetTitle;
        result.m_bigIcon = m_bigIcon;
        result.m_bigIconStyle = m_bigIconStyle;
        result.m_elementView = m_elementView;
        result.m_hasEditOptions = m_hasEditOptions;
        result.m_icon = m_icon;
        result.m_info = m_info;
        result.m_isView = m_isView;
        result.m_key = m_key;
        result.m_name = m_name;
        result.m_namePattern = m_namePattern;
        result.m_newResourceOrder = m_newResourceOrder;
        result.m_properties = new ArrayList<String>(m_properties);
        result.m_propertiesEnabled = m_propertiesEnabled;
        result.m_reference = m_reference;
        result.m_showNavigation = m_showNavigation;
        result.m_smallIconStyle = m_smallIconStyle;
        result.m_titleKey = m_titleKey;
        result.m_viewOrder = m_viewOrder;
        result.m_iconRules = new HashMap<String, CmsIconRule>();
        for (Map.Entry<String, CmsIconRule> rule : m_iconRules.entrySet()) {
            result.m_iconRules.put(rule.getKey(), (CmsIconRule)rule.getValue().clone());
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
            return m_newResourceOrder.compareTo(other.m_newResourceOrder);
        }
        return 0;
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
     * Returns the big icon CSS style class.<p>
     *
     * @return the big icon style
     */
    public String getBigIconStyle() {

        return m_bigIconStyle;
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

        return m_icon;
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
     * Returns the order for the new resource dialog of the explorer type setting.<p>
     *
     * @return the order for the new resource dialog of the explorer type setting
     */
    public String getNewResourceOrder() {

        return String.valueOf(m_newResourceOrder);
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
     * Returns the small icon CSS style class.<p>
     *
     * @return the small icon style
     */
    public String getSmallIconStyle() {

        return m_smallIconStyle;
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
                result = Integer.valueOf(9999);
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
     * Returns if this type is creatable.<p>
     *
     * @return <code>true</code> in case this type is creatable
     */
    public boolean isCreatable() {

        return m_creatable;
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
     * Check if property is required on upload.
     *
     * @param propName the property name
     * @return true if the property is required on upload
     */
    public boolean isPropertyRequiredOnUpload(String propName) {

        return m_requiredOnUpload.contains(propName);
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
     * Sets the big icon CSS style class.<p>
     *
     * @param bigIconStyle the big icon style to set
     */
    public void setBigIconStyle(String bigIconStyle) {

        m_bigIconStyle = bigIconStyle;
    }

    /**
     * Sets the creatable flag.<p>
     *
     * @param creatable the non creatable flag to set
     */
    public void setCreatable(boolean creatable) {

        m_creatable = creatable;
    }

    /**
     * Sets the creatable flag.<p>
     *
     * @param creatable the creatable flag to set
     */
    public void setCreatable(String creatable) {

        m_creatable = Boolean.parseBoolean(creatable);
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
                LOG.info(e.getLocalizedMessage(), e);
            }
            m_newResourceOrder = Integer.valueOf(0);
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
     * Sets the small icon CSS style class.<p>
     *
     * @param smallIconStyle the small icon CSS style class to set
     */
    public void setSmallIconStyle(String smallIconStyle) {

        m_smallIconStyle = smallIconStyle;
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
     * @param smallIconStyle the small icon CSS style class
     * @param bigIconStyle the big icon CSS style class
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
        String smallIconStyle,
        String bigIconStyle,
        String reference,
        String elementView,
        String isView,
        String namePattern,
        String viewOrder) {

        setName(name);
        setKey(key);
        setIcon(icon);
        setBigIcon(bigIcon);
        setSmallIconStyle(smallIconStyle);
        setBigIconStyle(bigIconStyle);
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
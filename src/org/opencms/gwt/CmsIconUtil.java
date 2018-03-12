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

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeUnknownFile;
import org.opencms.file.types.CmsResourceTypeUnknownFolder;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsIconRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class to generate the resource icon CSS.<p>
 *
 * @since 8.0.0
 */
public final class CmsIconUtil implements I_CmsEventListener {

    /**
     * Inner helper class for building the CSS rules.<p>
     */
    static class CssBuilder {

        /** The buffer into which the CSS is written. */
        private StringBuffer m_buffer = new StringBuffer(1024);

        /**
         * Builds the CSS for all resource types.<p>
         *
         * @return a string containing the CSS rules for all resource types
         */
        public String buildResourceIconCss() {

            for (CmsExplorerTypeSettings type : OpenCms.getWorkplaceManager().getExplorerTypeSettings()) {
                addCssForType(type);
            }
            return m_buffer.toString();
        }

        /**
         * Writes the CSS for a single icon rule to a buffer.<p>
         *
         * @param typeName the name of the resource type
         * @param rule the icon rule
         */
        private void addCssForIconRule(String typeName, CmsIconRule rule) {

            String extension = rule.getExtension();
            if (rule.getBigIcon() != null) {
                IconCssRuleBuilder cssBig = new IconCssRuleBuilder();
                cssBig.addSelectorForSubType(typeName, extension, false);
                cssBig.setImageUri(getIconUri(rule.getBigIcon()));
                cssBig.writeCss(m_buffer);

                IconCssRuleBuilder cssSmall = new IconCssRuleBuilder();
                cssSmall.addSelectorForSubType(typeName, extension, true);
                cssSmall.setImageUri(getIconUri(rule.getIcon()));
                cssSmall.writeCss(m_buffer);

            } else {
                IconCssRuleBuilder css = new IconCssRuleBuilder();
                css.addSelectorForSubType(typeName, extension, false);
                css.addSelectorForSubType(typeName, extension, true);
                css.setImageUri(getIconUri(rule.getIcon()));
                css.writeCss(m_buffer);

            }
        }

        /**
         * Helper method for appending the CSS for a single resource type to a buffer.<p>
         *
         * @param explorerType the explorer type for which the CSS should be generated
         */
        private void addCssForType(CmsExplorerTypeSettings explorerType) {

            String typeName = explorerType.getName();
            if (explorerType.getBigIconStyle() == null) {
                if (explorerType.getBigIcon() != null) {
                    IconCssRuleBuilder css = new IconCssRuleBuilder();
                    css.setImageUri(getIconUri(explorerType.getBigIcon()));
                    css.addSelectorForType(typeName, false);
                    css.writeCss(m_buffer);

                    IconCssRuleBuilder cssSmall = new IconCssRuleBuilder();
                    cssSmall.setImageUri(getIconUri(explorerType.getIcon()));
                    cssSmall.addSelectorForType(typeName, true);
                    cssSmall.writeCss(m_buffer);
                } else if (explorerType.getOriginalIcon() != null) {
                    IconCssRuleBuilder css = new IconCssRuleBuilder();
                    css.setImageUri(getIconUri(explorerType.getIcon()));
                    css.addSelectorForType(typeName, true);
                    css.addSelectorForType(typeName, false);
                    css.writeCss(m_buffer);
                }
            }
            Map<String, CmsIconRule> iconRules = explorerType.getIconRules();
            for (Map.Entry<String, CmsIconRule> entry : iconRules.entrySet()) {
                CmsIconRule rule = entry.getValue();
                addCssForIconRule(typeName, rule);
            }
        }

        /**
         * Converts an icon file name to a full icon URI.<p>
         *
         * @param icon the file name of the icon
         *
         * @return the full icon uri
         */
        private String getIconUri(String icon) {

            return CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + icon);
        }

    }

    /**
     * Helper class for creating the text of the CSS rule for a single icon based on resource type and file suffix.<p>
     */
    static class IconCssRuleBuilder {

        /** The uri of the icon image. */
        private String m_imageUri = "INVALID_ICON";

        /** The list of selector strings. */
        private List<String> m_selectors = new ArrayList<String>();

        /**
         * Adds a selector for a resource type and a file suffix.<p>
         *
         * @param type the resource type name
         * @param suffix the file suffix
         * @param small true if the selector should be for the small icon
         */
        public void addSelectorForSubType(String type, String suffix, boolean small) {

            String template = " .%1$s.%2$s.%3$s";
            String selector = String.format(
                template,
                CmsGwtConstants.TYPE_ICON_CLASS,
                getResourceTypeIconClass(type, small),
                getResourceSubTypeIconClass(type, suffix, small));
            m_selectors.add(selector);
        }

        /**
         * Adds a selector for a resource type.<p>
         *
         * @param type the name of the resource type
         * @param small true if the selector should be for the small icon
         */
        public void addSelectorForType(String type, boolean small) {

            String template = " div.%1$s.%2$s, span.%1$s.%2$s";
            String selector = String.format(
                template,
                CmsGwtConstants.TYPE_ICON_CLASS,
                getResourceTypeIconClass(type, small));
            m_selectors.add(selector);
        }

        /**
         * Sets the URI of the icon image file.<p>
         *
         * @param imageUri the URI of the icon image file
         */
        public void setImageUri(String imageUri) {

            m_imageUri = imageUri;
        }

        /**
         * Writes the CSS to a string buffer.<p>
         *
         * @param buffer the string buffer to which the
         */
        public void writeCss(StringBuffer buffer) {

            buffer.append(CmsStringUtil.listAsString(m_selectors, ", "));
            buffer.append(" { background-image: url(\"");
            buffer.append(m_imageUri);
            buffer.append("\");} ");
        }
    }

    /** Pseudo type icon. */
    public static final String ICON_MODEL_GROUP_BIG = CmsGwtConstants.TYPE_ICON_CLASS + " oc-icon-24-modelgroup_copy";

    /** Pseudo type icon. */
    public static final String ICON_MODEL_GROUP_COPY_BIG = CmsGwtConstants.TYPE_ICON_CLASS
        + " "
        + CmsExplorerTypeSettings.ICON_STYLE_MODEL_GROUP_COPY_BIG;

    /** Pseudo type icon. */
    public static final String ICON_MODEL_GROUP_COPY_SMALL = CmsGwtConstants.TYPE_ICON_CLASS
        + " "
        + CmsExplorerTypeSettings.ICON_STYLE_MODEL_GROUP_COPY_SMALL;

    /** Pseudo type icon. */
    public static final String ICON_NAV_LEVEL_BIG = CmsGwtConstants.TYPE_ICON_CLASS
        + " "
        + CmsExplorerTypeSettings.ICON_STYLE_NAV_LEVEL_BIG;

    /** Pseudo type icon. */
    public static final String ICON_NAV_LEVEL_SMALL = CmsGwtConstants.TYPE_ICON_CLASS
        + " "
        + CmsExplorerTypeSettings.ICON_STYLE_NAV_LEVEL_SMALL;

    /** The big resource not found icon name. */
    public static final String NOT_FOUND_ICON_BIG = CmsGwtConstants.TYPE_ICON_CLASS + " oc-icon-24-warning";

    /** The small resource not found icon name. */
    public static final String NOT_FOUND_ICON_SMALL = CmsGwtConstants.TYPE_ICON_CLASS + " oc-icon-16-warning";

    /** The suffix for the CSS classes for small icons. */
    public static final String SMALL_SUFFIX = "_small";

    /** Type for resource not found. */
    public static final String TYPE_RESOURCE_NOT_FOUND = "cms_resource_not_found";

    /** The cached CSS. */
    private static String m_cachedCss;

    /** The extension icon mapping. */
    private static Map<String, String> m_extensionIconMapping;

    /** Flag indicating the 'clear caches' event listener has been registered. */
    private static boolean m_listenerRegistered;

    /**
     * Constructor.<p>
     */
    private CmsIconUtil() {

    }

    /**
     * Builds the CSS for all resource types.<p>
     *
     * @return a string containing the CSS rules for all resource types
     */
    public static String buildResourceIconCss() {

        if (!m_listenerRegistered) {
            registerListener();
        }
        if (m_cachedCss == null) {
            rebuildCss();
        }
        return m_cachedCss;
    }

    /**
     * Returns the resource type name used to display the resource icon.
     * This may differ from the actual resource type in case of navigation level folders and model groups.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     *
     * @return the display type name
     */
    public static String getDisplayType(CmsObject cms, CmsResource resource) {

        String result;
        if (CmsJspNavBuilder.isNavLevelFolder(cms, resource)) {
            result = CmsGwtConstants.TYPE_NAVLEVEL;
        } else if (CmsResourceTypeXmlContainerPage.isModelCopyGroup(cms, resource)) {
            result = CmsGwtConstants.TYPE_MODELGROUP_COPY;
        } else {
            result = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        }
        return result;
    }

    /**
     * Returns the extension icon mapping used when uploading files.<p>
     *
     * @return the extension icon mapping
     */
    public static Map<String, String> getExtensionIconMapping() {

        if (m_extensionIconMapping == null) {
            m_extensionIconMapping = new HashMap<String, String>();
            for (Entry<String, String> entry : OpenCms.getResourceManager().getExtensionMapping().entrySet()) {
                m_extensionIconMapping.put(
                    entry.getKey(),
                    getIconClasses(entry.getValue(), "_." + entry.getKey(), false));
            }
            m_extensionIconMapping.put("", getIconClasses("plain", null, false));
        }
        // returning a copy of the icon map, as GWT will not work with unmodifiable maps
        return new HashMap<String, String>(m_extensionIconMapping);
    }

    /**
     * Returns the resource type icon CSS classes for the given type.<p>
     * Use within ADE context only.<p>
     *
     * @param typeSettings the explorer type settings
     * @param resourceName the resource name
     * @param small <code>true</code> to get the small icon classes
     *
     * @return the icon CSS classes
     */
    public static String getIconClasses(CmsExplorerTypeSettings typeSettings, String resourceName, boolean small) {

        String result = null;
        if (typeSettings == null) {
            typeSettings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                (resourceName != null) && CmsResource.isFolder(resourceName)
                ? CmsResourceTypeUnknownFile.RESOURCE_TYPE_NAME
                : CmsResourceTypeUnknownFolder.RESOURCE_TYPE_NAME);
        }
        if (!typeSettings.getIconRules().isEmpty() && (resourceName != null)) {
            String extension = CmsResource.getExtension(resourceName);
            if (extension != null) {
                // check for a matching sub type icon rule
                CmsIconRule rule = typeSettings.getIconRules().get(extension);
                if (rule != null) {
                    result = small ? rule.getSmallIconStyle() : rule.getBigIconStyle();
                }
            }
        }
        if (result == null) {
            if (small && (typeSettings.getSmallIconStyle() != null)) {
                result = typeSettings.getSmallIconStyle();
            } else if (small && (typeSettings.getIcon() == null)) {
                result = CmsExplorerTypeSettings.ICON_STYLE_DEFAULT_SMALL;
            } else if (!small && (typeSettings.getBigIconStyle() != null)) {
                result = typeSettings.getBigIconStyle();
            } else if (!small && (typeSettings.getBigIcon() == null)) {
                result = CmsExplorerTypeSettings.ICON_STYLE_DEFAULT_BIG;
            }

            if (result != null) {
                result = CmsGwtConstants.TYPE_ICON_CLASS + " " + result;
            } else {
                result = getResourceIconClasses(typeSettings.getName(), resourceName, small);
            }
        }
        return result;
    }

    /**
     * Returns the resource type icon CSS classes for the given type.<p>
     * Use within ADE context only.<p>
     *
     * @param resourceType the resource type name
     * @param resourceName the resource name
     * @param small <code>true</code> to get the small icon classes
     *
     * @return the icon CSS classes
     */
    public static String getIconClasses(String resourceType, String resourceName, boolean small) {

        String result;
        if (resourceType.equals(CmsGwtConstants.TYPE_NAVLEVEL)) {
            if (small) {
                result = ICON_NAV_LEVEL_SMALL;
            } else {
                result = ICON_NAV_LEVEL_BIG;
            }
        } else if (resourceType.equals(CmsGwtConstants.TYPE_MODELGROUP_COPY)) {
            if (small) {
                result = ICON_MODEL_GROUP_COPY_SMALL;
            } else {
                result = ICON_MODEL_GROUP_COPY_BIG;
            }
        } else if (resourceType.equals(TYPE_RESOURCE_NOT_FOUND)) {
            if (small) {
                result = NOT_FOUND_ICON_SMALL;
            } else {
                result = NOT_FOUND_ICON_BIG;
            }
        } else {
            result = getIconClasses(
                OpenCms.getWorkplaceManager().getExplorerTypeSetting(resourceType),
                resourceName,
                small);
        }
        return result;
    }

    /**
     * Returns the CSS class for a given resource type name and file name extension.<p>
     *
     * @param resourceTypeName the resource type name
     * @param suffix the file name extension
     * @param small if true, get the icon class for the small icon, else for the biggest one available
     *
     * @return the CSS class for the type and extension
     */
    static String getResourceSubTypeIconClass(String resourceTypeName, String suffix, boolean small) {

        StringBuffer buffer = new StringBuffer(CmsGwtConstants.TYPE_ICON_CLASS).append("_").append(
            resourceTypeName.hashCode()).append("_").append(suffix);
        if (small) {
            buffer.append(SMALL_SUFFIX);
        }
        return buffer.toString();
    }

    /**
     * Returns the CSS class for the given resource type.<p>
     *
     * @param resourceTypeName the resource type name
     * @param small if true, get the icon class for the small icon, else for the biggest one available
     *
     * @return the CSS class
     */
    static String getResourceTypeIconClass(String resourceTypeName, boolean small) {

        StringBuffer sb = new StringBuffer(CmsGwtConstants.TYPE_ICON_CLASS);
        sb.append("_").append(resourceTypeName.hashCode());
        if (small) {
            sb.append(SMALL_SUFFIX);
        }
        return sb.toString();
    }

    /**
     * Returns the CSS class for the given filename.<p>
     *
     * @param resourceTypeName the resource type name
     * @param fileName the filename
     * @param small if true, get the CSS class for the small icon, else for the biggest one available
     *
     * @return the CSS class
     */
    private static String getFileTypeIconClass(String resourceTypeName, String fileName, boolean small) {

        if ((fileName != null) && fileName.contains(".")) {
            int last = fileName.lastIndexOf(".");
            if (fileName.length() > (last + 1)) {
                String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                return getResourceSubTypeIconClass(resourceTypeName, suffix, small);
            }
        }
        return "";

    }

    /**
     * Returns the CSS classes of the resource icon for the given resource type and filename.<p>
     *
     * Use this the resource type and filename is known.<p>
     *
     * @param resourceTypeName the resource type name
     * @param fileName the filename
     * @param small if true, get the icon classes for the small icon, else for the biggest one available
     *
     * @return the CSS classes
     */
    private static String getResourceIconClasses(String resourceTypeName, String fileName, boolean small) {

        StringBuffer sb = new StringBuffer(CmsGwtConstants.TYPE_ICON_CLASS);
        sb.append(" ").append(getResourceTypeIconClass(resourceTypeName, small)).append(" ").append(
            getFileTypeIconClass(resourceTypeName, fileName, small));
        return sb.toString();
    }

    /**
     * Rebuilds the icon CSS.<p>
     */
    private static synchronized void rebuildCss() {

        if (m_cachedCss == null) {
            CssBuilder builder = new CssBuilder();
            m_cachedCss = builder.buildResourceIconCss();
        }
    }

    /**
     * Registers the 'clear caches' event listener.<p>
     */
    private static synchronized void registerListener() {

        if (!m_listenerRegistered) {
            OpenCms.getEventManager().addCmsEventListener(
                new CmsIconUtil(),
                new int[] {I_CmsEventListener.EVENT_CLEAR_CACHES});
            m_listenerRegistered = true;
        }
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        m_cachedCss = null;
    }
}

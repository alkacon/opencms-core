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

package org.opencms.widgets;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.galleries.shared.CmsGalleryTabConfiguration;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.Factory;
import org.apache.commons.logging.Log;

import com.google.common.base.Objects;

/**
 * Provides a OpenCms VFS file selection widget, for use on a widget dialog.<p>
 *
 * @since 6.0.0
 */
public class CmsVfsFileWidget extends A_CmsWidget implements I_CmsADEWidget {

    /** Macro resolver factory to get the default searchable types. */
    protected class SearchTypesFactory implements Factory {

        /** The CMS context. */
        private CmsObject m_cms;

        /** The resource. */
        private CmsResource m_resource;

        /**
         * Constructor.<p>
         *
         * @param cms the CMS context
         * @param resource the resource
         */
        public SearchTypesFactory(CmsObject cms, CmsResource resource) {

            m_cms = cms;
            m_resource = resource;
        }

        /**
         * @see org.apache.commons.collections.Factory#create()
         */
        public Object create() {

            return getDefaultSearchTypes(m_cms, m_resource);
        }
    }

    /** Configuration parameter to set the flag to include files in popup resource tree. */
    public static final String CONFIGURATION_EXCLUDEFILES = "excludefiles";

    /** Configuration parameter to restrict the widget to gallery selection only. */
    public static final String CONFIGURATION_GALLERYSELECT = "galleryselect";

    /** Configuration parameter to set the flag to show the site selector in popup resource tree. */
    public static final String CONFIGURATION_HIDESITESELECTOR = "hidesiteselector";

    /** Configuration parameter to set the flag to include files in popup resource tree. */
    public static final String CONFIGURATION_INCLUDEFILES = "includefiles";

    /** Configuration parameter to prevent the project awareness flag in the popup resource tree. */
    public static final String CONFIGURATION_NOTPROJECTAWARE = "notprojectaware";

    /** Configuration parameter to set the project awareness flag in the popup resource tree. */
    public static final String CONFIGURATION_PROJECTAWARE = "projectaware";

    /** Configuration parameter to set search types of the gallery widget. */
    public static final String CONFIGURATION_SEARCHTYPES = "searchtypes";

    /** Configuration parameter to set the selectable types of the gallery widget. */
    public static final String CONFIGURATION_SELECTABLETYPES = "selectabletypes";

    /** Configuration parameter to set the flag to show the site selector in popup resource tree. */
    public static final String CONFIGURATION_SHOWSITESELECTOR = "showsiteselector";

    /** Configuration parameter to set start folder. */
    public static final String CONFIGURATION_STARTFOLDER = "startfolder";

    /** Configuration parameter to set start site of the popup resource tree. */
    public static final String CONFIGURATION_STARTSITE = "startsite";

    /** The default search types macro name. */
    public static final String DEFAULT_SEARCH_TYPES_MACRO = "defaultSearchTypes";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsFileWidget.class);

    /** Flag which, when set, restricts the user to select only galleries or folders. */
    private boolean m_gallerySelect;

    /** Flag to determine if files should be shown in popup window. */
    private boolean m_includeFiles;

    /** Flag to determine project awareness, ie. if resources outside of the current project should be displayed as normal. */
    private boolean m_projectAware;

    /** The type shown in the gallery types tab. */
    private String m_searchTypes;

    /** The types that may be selected through the gallery widget. */
    private String m_selectableTypes;

    /** Flag to determine if the site selector should be shown in popup window. */
    private boolean m_showSiteSelector;

    /** The start folder. */
    private String m_startFolder;

    /** The start site used in the popup window. */
    private String m_startSite;

    /**
     * Creates a new vfs file widget.<p>
     */
    public CmsVfsFileWidget() {

        // empty constructor is required for class registration
        this("");
    }

    /**
     * Creates a new vfs file widget with the parameters to configure the popup tree window behavior.<p>
     *
     * @param showSiteSelector true if the site selector should be shown in the popup window
     * @param startSite the start site root for the popup window
     */
    public CmsVfsFileWidget(boolean showSiteSelector, String startSite) {

        this(showSiteSelector, startSite, true);
    }

    /**
     * Creates a new vfs file widget with the parameters to configure the popup tree window behavior.<p>
     *
     * @param showSiteSelector true if the site selector should be shown in the popup window
     * @param startSite the start site root for the popup window
     * @param includeFiles true if files should be shown in the popup window
     */
    public CmsVfsFileWidget(boolean showSiteSelector, String startSite, boolean includeFiles) {

        this(showSiteSelector, startSite, includeFiles, true);
    }

    /**
     * Creates a new vfs file widget with the parameters to configure the popup tree window behavior.<p>
     *
     * @param showSiteSelector true if the site selector should be shown in the popup window
     * @param startSite the start site root for the popup window
     * @param includeFiles <code>true</code> if files should be shown in the popup window
     * @param projectAware <code>true</code> if resources outside of the current project should be displayed as normal
     */
    public CmsVfsFileWidget(boolean showSiteSelector, String startSite, boolean includeFiles, boolean projectAware) {

        m_showSiteSelector = showSiteSelector;
        m_startSite = startSite;
        m_includeFiles = includeFiles;
        m_projectAware = projectAware;
    }

    /**
     * Creates a new vfs file widget with the given configuration.<p>
     *
     * @param configuration the configuration to use
     */
    public CmsVfsFileWidget(String configuration) {

        super(configuration);
    }

    /**
     * Returns a comma separated list of the default search type names.<p>
     *
     * @param cms the CMS context
     * @param resource the edited resource
     *
     * @return a comma separated list of the default search type names
     */
    public static String getDefaultSearchTypes(CmsObject cms, CmsResource resource) {

        StringBuffer result = new StringBuffer();
        String referenceSitePath = cms.getSitePath(resource);
        String configPath;
        if (resource == null) {
            // not sure if this can ever happen?
            configPath = cms.addSiteRoot(cms.getRequestContext().getUri());
        } else {
            configPath = resource.getRootPath();
        }
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, configPath);
        Set<String> detailPageTypes = OpenCms.getADEManager().getDetailPageTypes(cms);
        for (CmsResourceTypeConfig typeConfig : config.getResourceTypes()) {
            String typeName = typeConfig.getTypeName();
            if (!detailPageTypes.contains(typeName)) {
                continue;
            }
            if (typeConfig.checkViewable(cms, referenceSitePath)) {
                result.append(typeName).append(",");
            }
        }
        result.append(CmsResourceTypeXmlContainerPage.getStaticTypeName()).append(",");
        result.append(CmsResourceTypeBinary.getStaticTypeName()).append(",");
        result.append(CmsResourceTypeImage.getStaticTypeName()).append(",");
        result.append(CmsResourceTypePlain.getStaticTypeName());
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#getConfiguration()
     */
    @Override
    public String getConfiguration() {

        StringBuffer result = new StringBuffer(8);

        // append site selector flag to configuration
        if (m_showSiteSelector) {
            result.append(CONFIGURATION_SHOWSITESELECTOR);
        } else {
            result.append(CONFIGURATION_HIDESITESELECTOR);
        }

        // append start site to configuration
        if (m_startSite != null) {
            result.append("|");
            result.append(CONFIGURATION_STARTSITE);
            result.append("=");
            result.append(m_startSite);
        }

        // append flag for including files
        result.append("|");
        if (m_includeFiles) {
            result.append(CONFIGURATION_INCLUDEFILES);
        } else {
            result.append(CONFIGURATION_EXCLUDEFILES);
        }

        if (m_gallerySelect) {
            result.append("|");
            result.append(CONFIGURATION_GALLERYSELECT);
        }

        // append flag for project awareness
        result.append("|");
        if (m_projectAware) {
            result.append(CONFIGURATION_PROJECTAWARE);
        } else {
            result.append(CONFIGURATION_NOTPROJECTAWARE);
        }
        if (m_searchTypes != null) {
            result.append("|");
            result.append(CONFIGURATION_SEARCHTYPES);
            result.append("=");
            result.append(m_searchTypes);
        }
        if (m_selectableTypes != null) {
            result.append("|");
            result.append(CONFIGURATION_SELECTABLETYPES);
            result.append("=");
            result.append(m_selectableTypes);
        }
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getConfiguration(org.opencms.file.CmsObject, org.opencms.xml.types.A_CmsXmlContentValue, org.opencms.i18n.CmsMessages, org.opencms.file.CmsResource, java.util.Locale)
     */
    public String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        JSONObject config = getJsonConfig(cms, schemaType, messages, resource, contentLocale);
        return config.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getCssResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getCssResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getDefaultDisplayType()
     */
    public DisplayType getDefaultDisplayType() {

        return DisplayType.wide;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogIncludes(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogIncludes(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "commons/tree.js"));
        result.append("\n");
        result.append(getJSIncludeFile(CmsWorkplace.getSkinUri() + "components/widgets/fileselector.js"));
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitCall(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogInitCall(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        return "\tinitVfsFileSelector();\n";
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogInitMethod(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog)
     */
    @Override
    public String getDialogInitMethod(CmsObject cms, I_CmsWidgetDialog widgetDialog) {

        StringBuffer result = new StringBuffer(16);
        result.append("function initVfsFileSelector() {\n");
        //initialize tree javascript, does parts of <code>CmsTree.initTree(CmsObject, encoding, skinuri);</code>
        result.append("\tinitResources(\"");
        result.append(OpenCms.getWorkplaceManager().getEncoding());
        result.append("\", \"");
        result.append(CmsWorkplace.VFS_PATH_WORKPLACE);
        result.append("\", \"");
        result.append(CmsWorkplace.getSkinUri());
        result.append("\", \"");
        result.append(OpenCms.getSystemInfo().getOpenCmsContext());
        result.append("\");\n");
        result.append("}\n");
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#getDialogWidget(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    public String getDialogWidget(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        String id = param.getId();
        StringBuffer result = new StringBuffer(128);

        result.append("<td class=\"xmlTd\">");
        result.append(
            "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"maxwidth\"><tr><td style=\"width: 100%;\">");
        result.append("<input style=\"width: 99%;\" class=\"xmlInput");
        if (param.hasError()) {
            result.append(" xmlInputError");
        }
        result.append("\" value=\"");
        result.append(param.getStringValue(cms));
        result.append("\" name=\"");
        result.append(id);
        result.append("\" id=\"");
        result.append(id);
        result.append("\"></td>");
        result.append(widgetDialog.dialogHorizontalSpacer(10));
        result.append(
            "<td><table class=\"editorbuttonbackground\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");

        StringBuffer buttonJs = new StringBuffer(8);
        buttonJs.append("javascript:openTreeWin('EDITOR',  '");
        buttonJs.append(id);
        buttonJs.append("', document, ");
        buttonJs.append(m_showSiteSelector);
        buttonJs.append(", '");
        if (m_startSite != null) {
            buttonJs.append(m_startSite);
        } else {
            buttonJs.append(cms.getRequestContext().getSiteRoot());
        }
        buttonJs.append("', ");
        // include files
        buttonJs.append(m_includeFiles);
        // project awareness
        buttonJs.append(", ");
        buttonJs.append(m_projectAware);
        buttonJs.append(");return false;");

        result.append(
            widgetDialog.button(
                buttonJs.toString(),
                null,
                "folder",
                org.opencms.workplace.Messages.GUI_DIALOG_BUTTON_SEARCH_0,
                widgetDialog.getButtonStyle()));
        result.append("</tr></table>");
        result.append("</td></tr></table>");

        result.append("</td>");

        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getInitCall()
     */
    public String getInitCall() {

        return null;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getJavaScriptResourceLinks(org.opencms.file.CmsObject)
     */
    public List<String> getJavaScriptResourceLinks(CmsObject cms) {

        return null;
    }

    /**
     * Returns the start site root shown by the widget when first displayed.<p>
     *
     * If <code>null</code> is returned, the dialog will display the current site of
     * the current user.<p>
     *
     * @return the start site root shown by the widget when first displayed
     */
    public String getStartSite() {

        return m_startSite;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    public String getWidgetName() {

        return CmsVfsFileWidget.class.getName();
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#isInternal()
     */
    public boolean isInternal() {

        return true;
    }

    /**
     * Returns <code>true</code> if the site selector is shown.<p>
     *
     * The default is <code>true</code>.<p>
     *
     * @return <code>true</code> if the site selector is shown
     */
    public boolean isShowingSiteSelector() {

        return m_showSiteSelector;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsVfsFileWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsWidget#setConfiguration(java.lang.String)
     */
    @Override
    public void setConfiguration(String configuration) {

        m_showSiteSelector = true;
        m_includeFiles = true;
        m_projectAware = true;

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration)) {
            if (configuration.contains(CONFIGURATION_HIDESITESELECTOR)) {
                // site selector should be hidden
                m_showSiteSelector = false;
            }
            int siteIndex = configuration.indexOf(CONFIGURATION_STARTSITE);
            if (siteIndex != -1) {
                // start site is given
                String site = configuration.substring(siteIndex + CONFIGURATION_STARTSITE.length() + 1);
                if (site.indexOf('|') != -1) {
                    // cut eventual following configuration values
                    site = site.substring(0, site.indexOf('|'));
                }
                m_startSite = site;
            }
            if (configuration.contains(CONFIGURATION_EXCLUDEFILES)) {
                // files should not be included
                m_includeFiles = false;
            }
            if (configuration.contains(CONFIGURATION_GALLERYSELECT)) {
                m_gallerySelect = true;
            }

            if (configuration.contains(CONFIGURATION_NOTPROJECTAWARE)) {
                // resources outside of the current project should not be disabled
                m_projectAware = false;
            }
            int searchTypesIndex = configuration.indexOf(CONFIGURATION_SEARCHTYPES);
            if (searchTypesIndex != -1) {
                String searchTypes = configuration.substring(searchTypesIndex + CONFIGURATION_SEARCHTYPES.length() + 1);
                if (searchTypes.contains("|")) {
                    m_searchTypes = searchTypes.substring(0, searchTypes.indexOf("|"));
                } else {
                    m_searchTypes = searchTypes;
                }
            }
            int selectableTypesIndex = configuration.indexOf(CONFIGURATION_SELECTABLETYPES);
            if (selectableTypesIndex != -1) {
                String selectableTypes = configuration.substring(
                    selectableTypesIndex + CONFIGURATION_SELECTABLETYPES.length() + 1);
                if (selectableTypes.contains("|")) {
                    m_selectableTypes = selectableTypes.substring(0, selectableTypes.indexOf("|"));
                } else {
                    m_selectableTypes = selectableTypes;
                }
            }
            int startFolderIndex = configuration.indexOf(CONFIGURATION_STARTFOLDER);
            if (startFolderIndex != -1) {
                String startFolder = configuration.substring(startFolderIndex + CONFIGURATION_STARTFOLDER.length() + 1);
                if (startFolder.contains("|")) {
                    m_startFolder = startFolder.substring(0, startFolder.indexOf("|"));
                } else {
                    m_startFolder = startFolder;
                }
            }
        }
        super.setConfiguration(configuration);
    }

    /**
     * Gets the JSON configuration.<p>
     *
     * @param cms the current CMS context
     * @param schemaType the schema type
     * @param messages the messages
     * @param resource the content resource
     * @param contentLocale the content locale
     *
     * @return the JSON configuration object
     */
    protected JSONObject getJsonConfig(
        CmsObject cms,
        A_CmsXmlContentValue schemaType,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale) {

        JSONObject config = new JSONObject();
        try {
            config.put(I_CmsGalleryProviderConstants.CONFIG_START_SITE, m_startSite);

            config.put(I_CmsGalleryProviderConstants.CONFIG_SHOW_SITE_SELECTOR, m_showSiteSelector);
            config.put(I_CmsGalleryProviderConstants.CONFIG_REFERENCE_PATH, cms.getSitePath(resource));
            config.put(I_CmsGalleryProviderConstants.CONFIG_LOCALE, contentLocale.toString());
            config.put(I_CmsGalleryProviderConstants.CONFIG_GALLERY_MODE, GalleryMode.widget.name());
            config.put(I_CmsGalleryProviderConstants.CONFIG_GALLERY_STORAGE_PREFIX, "linkselect");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_selectableTypes)) {
                config.put(I_CmsGalleryProviderConstants.CONFIG_RESOURCE_TYPES, m_selectableTypes.trim());
            }
            String tabConfig = null;
            if (m_includeFiles) {
                tabConfig = CmsGalleryTabConfiguration.TC_SELECT_ALL;
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_selectableTypes)
                    && !Arrays.asList(m_selectableTypes.split("[, ]+")).contains(
                        CmsResourceTypeXmlContainerPage.getStaticTypeName())) {
                    tabConfig = CmsGalleryTabConfiguration.TC_SELECT_ALL_NO_SITEMAP;
                }
            } else {
                tabConfig = CmsGalleryTabConfiguration.TC_FOLDERS;
            }
            config.put(I_CmsGalleryProviderConstants.CONFIG_TAB_CONFIG, tabConfig);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_searchTypes)) {
                CmsMacroResolver resolver = CmsMacroResolver.newInstance();
                resolver.addDynamicMacro(DEFAULT_SEARCH_TYPES_MACRO, new SearchTypesFactory(cms, resource));
                String searchTypes = resolver.resolveMacros(m_searchTypes.trim());
                config.put(I_CmsGalleryProviderConstants.CONFIG_SEARCH_TYPES, searchTypes);
            } else if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_selectableTypes)) {
                config.put(I_CmsGalleryProviderConstants.CONFIG_SEARCH_TYPES, getDefaultSearchTypes(cms, resource));
            }
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_startFolder)) {
                config.put(I_CmsGalleryProviderConstants.CONFIG_START_FOLDER, m_startFolder);
            }
            String treeToken = ""
                + Objects.hashCode(m_startSite, cms.getRequestContext().getSiteRoot(), "" + m_selectableTypes);
            config.put(I_CmsGalleryProviderConstants.CONFIG_TREE_TOKEN, treeToken);

            if (m_gallerySelect) {
                config.put(I_CmsGalleryProviderConstants.CONFIG_GALLERIES_SELECTABLE, "true");
                config.put(I_CmsGalleryProviderConstants.CONFIG_RESULTS_SELECTABLE, "false");
                config.put(I_CmsGalleryProviderConstants.CONFIG_TAB_CONFIG, CmsGalleryTabConfiguration.TC_GALLERIES);
            }

        } catch (JSONException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return config;
    }

    /**
     * Computes the tree token, which is used to decide which preloaded tree, if any, to load for the VFS/sitemap tabs.<p>
     *
     * @param cms the current CMS context
     * @param value the content value
     * @param resource the content resource
     * @param contentLocale the content locale
     *
     * @return the tree token
     */
    protected String getTreeToken(
        CmsObject cms,
        A_CmsXmlContentValue value,
        CmsResource resource,
        Locale contentLocale) {

        return cms.getRequestContext().getSiteRoot();
    }
}

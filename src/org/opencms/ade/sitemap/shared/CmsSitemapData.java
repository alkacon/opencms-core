/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsClientTemplateBean;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Sitemap initialization data bean for prefetching.<p>
 * 
 * @since 8.0
 */
public class CmsSitemapData implements IsSerializable {

    /** The sitemap editor modes. */
    public enum EditorMode {
        /** The galleries mode. */
        galleries,
        /** The navigation mode. */
        navigation,
        /** The VFS mode. */
        vfs
    }

    /** Name of the used js variable. */
    public static final String DICT_NAME = "org_opencms_ade_sitemap";

    /** The editor mode parameter name. */
    public static final String PARAM_EDITOR_MODE = "editormode";

    /** The URL of the JSP used to import aliases. */
    private String m_aliasImportUrl;

    /** The list of property names. */
    private List<String> m_allPropertyNames;

    /** Flag to indicate whether the user can open the alias editor. */
    private boolean m_canEditAliases;

    /** Flag to indicate whether detail pages can be edited. */
    private boolean m_canEditDetailPages;

    /** The clipboard data. */
    private CmsSitemapClipboardData m_clipboardData;

    /** The sitemap context menu entries. */
    private List<CmsContextMenuEntryBean> m_contextMenuEntries;

    /** A flag which controls whether a new folder should be created for subsitemaps. */
    private boolean m_createNewFolderForSubsitemap;

    /** The default info bean for new elements. **/
    private CmsNewResourceInfo m_defaultNewElementInfo;

    /** The detail page table. */
    private CmsDetailPageTable m_detailPageTable;

    /** Flag to control the display of the toolbar. */
    private boolean m_displayToolbar;

    /** The editor mode. */
    private EditorMode m_editorMode;

    /** The export RFS prefix. */
    private String m_exportRfsPrefix;

    /** A flag which indicates whether the site which contains the sitemap is a secure site. */
    private boolean m_isSecure;

    /** The maximum sitemap depth. */
    private int m_maxDepth;

    /** The new element information. */
    private List<CmsNewResourceInfo> m_newElementInfos;

    /** The new navigation level element info. */
    private CmsNewResourceInfo m_newNavigatioLevelElementInfo;

    /** The new redirect element info. */
    private CmsNewResourceInfo m_newRedirectElementInfo;

    /** The reason why the current sitemap is not editable. */
    private String m_noEditReason;

    /** The path at which the sitemap should be opened, or null. */
    private String m_openPath;

    /** The properties of the root's parent. */
    private Map<String, CmsClientProperty> m_parentProperties;

    /** The path to the parent sitemap or <code>null</code>. */
    private String m_parentSitemap;

    /** The sitemap properties. */
    private Map<String, CmsXmlContentProperty> m_properties;

    /** The resource type information. */
    private List<CmsNewResourceInfo> m_resourceTypeInfos;

    /** The return page code. */
    private String m_returnCode;

    /** The sitemap root. */
    private CmsClientSitemapEntry m_root;

    /** The list info beans for possible sitemap folder types. */
    private List<CmsListInfoBean> m_sitemapFolderTypeInfos;

    /** The sitemap info. */
    private CmsSitemapInfo m_sitemapInfo;

    /** The available templates. */
    private Map<String, CmsClientTemplateBean> m_templates;

    /**
     * Constructor.<p>
     */
    public CmsSitemapData() {

        // empty
    }

    /**
     * Constructor.<p>
     * 
     * @param templates the available templates
     * @param properties the properties
     * @param clipboardData the clipboard data
     * @param contextMenuEntries the sitemap context menu entries
     * @param parentProperties the root entry's parent's inherited properties 
     * @param allPropNames the names of all properties 
     * @param exportRfsPrefix the export RFS prefix 
     * @param isSecure true if there is a secure server configuration for the site which contains the sitemap 
     * @param noEditReason the reason why the current sitemap is not editable
     * @param displayToolbar the flag to control the display of the toolbar
     * @param defaultNewElementInfo the type of the container page resource
     * @param newElementInfos the new element information
     * @param newRedirectElementInfo the new redirect element info
     * @param newNavigationLevelElementInfo the new navigation level element info
     * @param sitemapInfo the sitemap info bean
     * @param parentSitemap the path to the parent sitemap or <code>null</code>
     * @param root the sitemap root
     * @param openPath the path at which the sitemap should be opened 
     * @param maxDepth the maximum sitemap depth
     * @param detailPageTable the detail page table 
     * @param resourceTypeInfos the resource type information for the detail pages  
     * @param returnCode return page code
     * @param canEditDetailPages flag to indicate whether detail pages can be edited
     * @param aliasImportUrl the URL of the JSP used to import aliases 
     * @param canEditAliases flag to indicate whether the current user can edit the alias table 
     * @param createNewFoldersForSubsitemaps flag to control whether new folders should be created for subsitemaps 
     * @param subsitemapTypeInfos the type information beans for the available subsitemap folder types 
     * @param editorMode the editor mode
     */
    public CmsSitemapData(
        Map<String, CmsClientTemplateBean> templates,
        Map<String, CmsXmlContentProperty> properties,
        CmsSitemapClipboardData clipboardData,
        List<CmsContextMenuEntryBean> contextMenuEntries,
        Map<String, CmsClientProperty> parentProperties,
        List<String> allPropNames,
        String exportRfsPrefix,
        boolean isSecure,
        String noEditReason,
        boolean displayToolbar,
        CmsNewResourceInfo defaultNewElementInfo,
        List<CmsNewResourceInfo> newElementInfos,
        CmsNewResourceInfo newRedirectElementInfo,
        CmsNewResourceInfo newNavigationLevelElementInfo,
        CmsSitemapInfo sitemapInfo,
        String parentSitemap,
        CmsClientSitemapEntry root,
        String openPath,
        int maxDepth,
        CmsDetailPageTable detailPageTable,
        List<CmsNewResourceInfo> resourceTypeInfos,
        String returnCode,
        boolean canEditDetailPages,
        String aliasImportUrl,
        boolean canEditAliases,
        boolean createNewFoldersForSubsitemaps,
        List<CmsListInfoBean> subsitemapTypeInfos,
        EditorMode editorMode) {

        m_templates = templates;
        m_properties = properties;
        m_clipboardData = clipboardData;
        m_contextMenuEntries = contextMenuEntries;
        m_noEditReason = noEditReason;
        m_displayToolbar = displayToolbar;
        m_defaultNewElementInfo = defaultNewElementInfo;
        m_sitemapInfo = sitemapInfo;
        m_parentSitemap = parentSitemap;
        m_parentProperties = parentProperties;
        m_root = root;
        m_openPath = openPath;
        m_exportRfsPrefix = exportRfsPrefix;
        m_isSecure = isSecure;
        m_maxDepth = maxDepth;
        m_detailPageTable = detailPageTable;
        m_resourceTypeInfos = resourceTypeInfos;
        m_canEditDetailPages = canEditDetailPages;
        m_allPropertyNames = allPropNames;
        m_returnCode = returnCode;
        m_newElementInfos = newElementInfos;
        m_newRedirectElementInfo = newRedirectElementInfo;
        m_newNavigatioLevelElementInfo = newNavigationLevelElementInfo;
        m_aliasImportUrl = aliasImportUrl;
        m_canEditAliases = canEditAliases;
        m_createNewFolderForSubsitemap = createNewFoldersForSubsitemaps;
        m_sitemapFolderTypeInfos = subsitemapTypeInfos;
        m_editorMode = editorMode;
    }

    /**
     * Checks whether the current user can edit the aliases.<p>
     * 
     * @return true if the current user can edit the aliases 
     */
    public boolean canEditAliases() {

        return m_canEditAliases;

    }

    /**
     * Returns true if the detail pages can be edited.
     *  
     * @return true if the detail pages can be edited 
     */
    public boolean canEditDetailPages() {

        return m_canEditDetailPages && (m_resourceTypeInfos != null) && !m_resourceTypeInfos.isEmpty();
    }

    /**
     * Gets the URL of the JSP used to import aliases.<p>
     * 
     * @return the alias import URL 
     */
    public String getAliasImportUrl() {

        return m_aliasImportUrl;
    }

    /**
     * Returns the names of all properties.<p>
     * 
     * @return the names of all properties 
     */
    public List<String> getAllPropertyNames() {

        return m_allPropertyNames;
    }

    /**
     * Returns the clipboard data.<p>
     *
     * @return the clipboard data
     */
    public CmsSitemapClipboardData getClipboardData() {

        return m_clipboardData;
    }

    /**
     * Returns the sitemap context menu entries.<p>
     *
     * @return the sitemap context menu entries
     */
    public List<CmsContextMenuEntryBean> getContextMenuEntries() {

        return m_contextMenuEntries;
    }

    /**
     * Returns the type of the container page resource.<p>
     *
     * @return the type of the container page resource
     */
    public CmsNewResourceInfo getDefaultNewElementInfo() {

        return m_defaultNewElementInfo;
    }

    /**
     * Gets the detail page table.
     *
     * @return the detail page table
     */
    public CmsDetailPageTable getDetailPageTable() {

        return m_detailPageTable;
    }

    /**
     * Returns the editor mode.<p>
     * 
     * @return the editor mode
     */
    public EditorMode getEditorMode() {

        return m_editorMode;
    }

    /**
     * Returns the export RFS prefix.<p>
     * 
     * @return the export RFS prefix
     */
    public String getExportRfsPrefix() {

        return m_exportRfsPrefix;
    }

    /**
     * Returns the maximum sitemap depth.<p>
     * 
     * @return the maximum sitemap depth
     */
    public int getMaxDepth() {

        return m_maxDepth;
    }

    /**
     * Returns the new element information.<p>
     *
     * @return the new element information
     */
    public List<CmsNewResourceInfo> getNewElementInfos() {

        return m_newElementInfos;
    }

    /**
     * Returns the new navigation level element info.<p>
     * 
     * @return the new navigation level element info
     */
    public CmsNewResourceInfo getNewNavigationLevelElementInfo() {

        return m_newNavigatioLevelElementInfo;
    }

    /**
     * Returns the new redirect element info.<p>
     *
     * @return the new redirect element info
     */
    public CmsNewResourceInfo getNewRedirectElementInfo() {

        return m_newRedirectElementInfo;
    }

    /**
     * Returns the reason why the current sitemap is not editable.<p>
     *
     * @return the reason why the current sitemap is not editable
     */
    public String getNoEditReason() {

        return m_noEditReason;
    }

    /**
     * Gets the path at which the sitemap should be opened (may be null).<p>
     * 
     * @return the path at which the sitemap should be opened 
     */
    public String getOpenPath() {

        return m_openPath;
    }

    /**
     * Returns the properties of the sitemap root's parent.<p>
     * 
     * @return the properties of the sitemap root'S parent
     */
    public Map<String, CmsClientProperty> getParentProperties() {

        return m_parentProperties;
    }

    /**
     * Returns the path to the parent sitemap or <code>null</code>.<p>
     *
     * @return the path to the parent sitemap or <code>null</code>
     */
    public String getParentSitemap() {

        return m_parentSitemap;
    }

    /**
     * Returns the properties.<p>
     *
     * @return the properties
     */
    public Map<String, CmsXmlContentProperty> getProperties() {

        return m_properties;
    }

    /**
     * Gets the resource type info beans for the detail pages.<p>
     *
     * @return the resource type info beans for the detail pages 
     */
    public List<CmsNewResourceInfo> getResourceTypeInfos() {

        return m_resourceTypeInfos;
    }

    /**
     * Returns the return page code.<p>
     *
     * @return the return page code
     */
    public String getReturnCode() {

        return m_returnCode;
    }

    /**
     * Returns the sitemap root.<p>
     *
     * @return the sitemap root
     */
    public CmsClientSitemapEntry getRoot() {

        return m_root;
    }

    /**
     * Returns the sitemap info.<p>
     *
     * @return the sitemap info
     */
    public CmsSitemapInfo getSitemapInfo() {

        return m_sitemapInfo;
    }

    /**
     * Returns the list info beans for the available sitemap folder types.<p>
     * 
     * @return the list info beans for the available sitemap folder types 
     */
    public List<CmsListInfoBean> getSubsitemapFolderTypeInfos() {

        return m_sitemapFolderTypeInfos;
    }

    /**
     * Returns the available templates.<p>
     *
     * @return the available templates
     */
    public Map<String, CmsClientTemplateBean> getTemplates() {

        return m_templates;
    }

    /**
     * Returns true if new folders should be created for subsitemaps.<p>
     * 
     * @return true if new folders should be created for subsitemaps 
     */
    public boolean isCreateNewFoldersForSubsitemaps() {

        return m_createNewFolderForSubsitemap;
    }

    /**
     * Checks if to display the toolbar.<p>
     *
     * @return <code>true</code> if to display the toolbar
     */
    public boolean isDisplayToolbar() {

        return m_displayToolbar;
    }

    /**
     * Returns true if there is a secure server configured for the site which contains the sitemap.<p>
     * 
     * @return true if there is a secure server configured for the site which contains the sitemap 
     */
    public boolean isSecure() {

        return m_isSecure;
    }

    /**
     * Sets the return page code.<p>
     *
     * @param returnCode the return page code to set
     */
    public void setReturnCode(String returnCode) {

        m_returnCode = returnCode;
    }

}

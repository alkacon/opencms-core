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

    /** Name of the used js variable. */
    public static final String DICT_NAME = "org_opencms_ade_sitemap";

    /** The list of property names. */
    private List<String> m_allPropertyNames;

    /** Flag to indicate whether detail pages can be edited. */
    private boolean m_canEditDetailPages;

    /** The clipboard data. */
    private CmsSitemapClipboardData m_clipboardData;

    /** The default info bean for new elements. **/
    private CmsNewResourceInfo m_defaultNewElementInfo;

    /** The detail page table. */
    private CmsDetailPageTable m_detailPageTable;

    /** Flag to control the display of the toolbar. */
    private boolean m_displayToolbar;

    /** The export name of the site which contains the sitemap. */
    private String m_exportName;

    /** The export RFS prefix. */
    private String m_exportRfsPrefix;

    /** A flag which indicates whether the site which contains the sitemap is a secure site. */
    private boolean m_isSecure;

    /** The maximum sitemap depth. */
    private int m_maxDepth;

    /** The new element information. */
    private List<CmsNewResourceInfo> m_newElementInfos;

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
     * @param parentProperties the root entry's parent's inherited properties 
     * @param allPropNames the names of all properties 
     * @param exportRfsPrefix the export RFS prefix 
     * @param isSecure true if there is a secure server configuration for the site which contains the sitemap 
     * @param noEditReason the reason why the current sitemap is not editable
     * @param displayToolbar the flag to control the display of the toolbar
     * @param defaultNewElementInfo the type of the container page resource
     * @param newElementInfos the new element information
     * @param newRedirectElementInfo the new redirect element info
     * @param sitemapInfo the sitemap info bean
     * @param parentSitemap the path to the parent sitemap or <code>null</code>
     * @param root the sitemap root
     * @param openPath the path at which the sitemap should be opened 
     * @param maxDepth the maximum sitemap depth
     * @param detailPageTable the detail page table 
     * @param resourceTypeInfos the resource type information for the detail pages  
     * @param returnCode return page code
     * @param canEditDetailPages flag to indicate whether detail pages can be edited
     */
    public CmsSitemapData(
        Map<String, CmsClientTemplateBean> templates,
        Map<String, CmsXmlContentProperty> properties,
        CmsSitemapClipboardData clipboardData,
        Map<String, CmsClientProperty> parentProperties,
        List<String> allPropNames,
        String exportRfsPrefix,
        boolean isSecure,
        String noEditReason,
        boolean displayToolbar,
        CmsNewResourceInfo defaultNewElementInfo,
        List<CmsNewResourceInfo> newElementInfos,
        CmsNewResourceInfo newRedirectElementInfo,
        CmsSitemapInfo sitemapInfo,
        String parentSitemap,
        CmsClientSitemapEntry root,
        String openPath,
        int maxDepth,
        CmsDetailPageTable detailPageTable,
        List<CmsNewResourceInfo> resourceTypeInfos,
        String returnCode,
        boolean canEditDetailPages) {

        m_templates = templates;
        m_properties = properties;
        m_clipboardData = clipboardData;
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
     * Returns the export name from the sitemap configuration.<p>
     *  
     * @return the export name
     */
    public String getExportName() {

        return m_exportName;
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
     * Returns the available templates.<p>
     *
     * @return the available templates
     */
    public Map<String, CmsClientTemplateBean> getTemplates() {

        return m_templates;
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

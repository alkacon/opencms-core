/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsSitemapData.java,v $
 * Date   : $Date: 2011/02/02 07:37:52 $
 * Version: $Revision: 1.14 $
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

package org.opencms.ade.sitemap.shared;

import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.sitemap.CmsDetailPageTable;
import org.opencms.xml.sitemap.properties.CmsComputedPropertyValue;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Sitemap initialization data bean for prefetching.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.14 $
 * 
 * @since 8.0
 */
public class CmsSitemapData implements IsSerializable {

    /** Name of the used js variable. */
    public static final String DICT_NAME = "org_opencms_ade_sitemap";

    /** Flag to indicate whether detail pages can be edited. */
    private boolean m_canEditDetailPages;

    /** The clipboard data. */
    private CmsSitemapClipboardData m_clipboardData;

    /** The type of the container page resource. */
    private int m_cntPageType;

    /** The default template. */
    private CmsSitemapTemplate m_defaultTemplate;

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

    /** The reason why the current sitemap is not editable. */
    private String m_noEditReason;

    /** The path at which the sitemap should be opened, or null. */
    private String m_openPath;

    /** A map of properties which the root entry of the sitemap should inherit. */
    private Map<String, CmsComputedPropertyValue> m_parentProperties;

    /** The path to the parent sitemap or <code>null</code>. */
    private String m_parentSitemap;

    /** The sitemap properties. */
    private Map<String, CmsXmlContentProperty> m_properties;

    /** The resource type information. */
    private List<CmsResourceTypeInfo> m_resourceTypeInfos;

    /** The sitemap root. */
    private CmsClientSitemapEntry m_root;

    /** The available templates. */
    private Map<String, CmsSitemapTemplate> m_templates;

    /**
     * Constructor.<p>
     */
    public CmsSitemapData() {

        // empty
    }

    /**
     * Constructor.<p>
     * 
     * @param defaultTemplate the default template
     * @param templates the available templates
     * @param properties the properties
     * @param clipboardData the clipboard data
     * @param parentProperties the root entry's parent's inherited properties 
     * @param exportName the configured export name for the site which contains the sitemap
     * @param exportRfsPrefix the export RFS prefix 
     * @param isSecure true if there is a secure server configuration for the site which contains the sitemap 
     * @param noEditReason the reason why the current sitemap is not editable
     * @param displayToolbar the flag to control the display of the toolbar
     * @param cntPageType the type of the container page resource
     * @param parentSitemap the path to the parent sitemap or <code>null</code>
     * @param root the sitemap root
     * @param openPath the path at which the sitemap should be opened 
     * @param maxDepth the maximum sitemap depth
     * @param detailPageTable the detail page table 
     * @param resourceTypeInfos the resource type information for the detail pages  
     * @param canEditDetailPages flag to indicate whether detail pages can be edited
     */
    public CmsSitemapData(
        CmsSitemapTemplate defaultTemplate,
        Map<String, CmsSitemapTemplate> templates,
        Map<String, CmsXmlContentProperty> properties,
        CmsSitemapClipboardData clipboardData,
        Map<String, CmsComputedPropertyValue> parentProperties,
        String exportName,
        String exportRfsPrefix,
        boolean isSecure,
        String noEditReason,
        boolean displayToolbar,
        int cntPageType,
        String parentSitemap,
        CmsClientSitemapEntry root,
        String openPath,
        int maxDepth,
        CmsDetailPageTable detailPageTable,
        List<CmsResourceTypeInfo> resourceTypeInfos,
        boolean canEditDetailPages) {

        m_defaultTemplate = defaultTemplate;
        m_templates = templates;
        m_properties = properties;
        m_clipboardData = clipboardData;
        m_noEditReason = noEditReason;
        m_displayToolbar = displayToolbar;
        m_cntPageType = cntPageType;
        m_parentSitemap = parentSitemap;
        m_parentProperties = parentProperties;
        m_root = root;
        m_openPath = openPath;
        m_exportName = exportName;
        m_exportRfsPrefix = exportRfsPrefix;
        m_isSecure = isSecure;
        m_maxDepth = maxDepth;
        m_detailPageTable = detailPageTable;
        m_resourceTypeInfos = resourceTypeInfos;
        m_canEditDetailPages = canEditDetailPages;
    }

    /**
     * Returns true if the detail pages can be edited.
     *  
     * @return true if the detail pages can be edited 
     */
    public boolean canEditDetailPages() {

        return m_canEditDetailPages;
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
    public int getCntPageType() {

        return m_cntPageType;
    }

    /**
     * Returns the default template.<p>
     *
     * @return the default template
     */
    public CmsSitemapTemplate getDefaultTemplate() {

        return m_defaultTemplate;
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
     * Returns the properties which the root entry of this sitemap should inherit.<p>
     * 
     * @return the set of inherited properties 
     */
    public Map<String, CmsComputedPropertyValue> getParentProperties() {

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
    public List<CmsResourceTypeInfo> getResourceTypeInfos() {

        return m_resourceTypeInfos;
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
     * Returns the available templates.<p>
     *
     * @return the available templates
     */
    public Map<String, CmsSitemapTemplate> getTemplates() {

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
}

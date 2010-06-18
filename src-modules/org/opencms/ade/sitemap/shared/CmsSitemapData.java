/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsSitemapData.java,v $
 * Date   : $Date: 2010/06/18 07:29:54 $
 * Version: $Revision: 1.5 $
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

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Sitemap initialization data bean for prefetching.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0
 */
public class CmsSitemapData implements IsSerializable {

    /** Name of the used js variable. */
    public static final String DICT_NAME = "org_opencms_ade_sitemap";

    /** The type of the container page resource. */
    private int m_cntPageType;

    /** The default template. */
    private CmsSitemapTemplate m_defaultTemplate;

    /** Flag to control the display of the toolbar. */
    private boolean m_displayToolbar;

    /** The reason why the current sitemap is not editable. */
    private String m_noEditReason;

    /** The path at which the sitemap should be opened, or null. */
    private String m_openPath;

    /** The path to the parent sitemap or <code>null</code>. */
    private String m_parentSitemap;

    /** The sitemap properties. */
    private Map<String, CmsXmlContentProperty> m_properties;

    /** The recent list. */
    private List<CmsClientSitemapEntry> m_recentList;

    /** The sitemap root. */
    private CmsClientSitemapEntry m_root;

    /** The available templates. */
    private Map<String, CmsSitemapTemplate> m_templates;

    /** The sitemap resource last modification date. */
    private long m_timestamp;

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
     * @param recentList the recent list
     * @param noEditReason the reason why the current sitemap is not editable
     * @param displayToolbar the flag to control the display of the toolbar
     * @param cntPageType the type of the container page resource
     * @param parentSitemap the path to the parent sitemap or <code>null</code>
     * @param root the sitemap root
     * @param timestamp the sitemap resource last modification date
     * @param openPath the path at which the sitemap should be opened 
     */
    public CmsSitemapData(
        CmsSitemapTemplate defaultTemplate,
        Map<String, CmsSitemapTemplate> templates,
        Map<String, CmsXmlContentProperty> properties,
        List<CmsClientSitemapEntry> recentList,
        String noEditReason,
        boolean displayToolbar,
        int cntPageType,
        String parentSitemap,
        CmsClientSitemapEntry root,
        long timestamp,
        String openPath) {

        m_defaultTemplate = defaultTemplate;
        m_templates = templates;
        m_properties = properties;
        m_recentList = recentList;
        m_noEditReason = noEditReason;
        m_displayToolbar = displayToolbar;
        m_cntPageType = cntPageType;
        m_parentSitemap = parentSitemap;
        m_root = root;
        m_timestamp = timestamp;
        m_openPath = openPath;

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
     * Returns the recent list.<p>
     *
     * @return the recent list
     */
    public List<CmsClientSitemapEntry> getRecentList() {

        return m_recentList;
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
     * Returns the sitemap resource last modification date.<p>
     *
     * @return the sitemap resource last modification date
     */
    public long getTimestamp() {

        return m_timestamp;
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
     * Sets the timestamp.<p>
     *
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {

        m_timestamp = timestamp;
    }

}

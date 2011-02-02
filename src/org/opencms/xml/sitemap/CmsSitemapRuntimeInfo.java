/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapRuntimeInfo.java,v $
 * Date   : $Date: 2011/02/02 07:37:52 $
 * Version: $Revision: 1.2 $
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

package org.opencms.xml.sitemap;

import org.opencms.adeconfig.CmsConfigurationParser;
import org.opencms.file.types.I_CmsResourceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Runtime information about the sitemap data from a single sitemap file in a given locale.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsSitemapRuntimeInfo {

    /** The runtime info beans of the sub-sitemaps. */
    private List<CmsSitemapRuntimeInfo> m_children = new ArrayList<CmsSitemapRuntimeInfo>();

    /** The configuration data for the sitemap. */
    private CmsConfigurationParser m_configuration;

    /** The detail page information. */
    private CmsDetailPageTable m_detailPageTable;

    /** The entry point of the sitemap corresponding to this info bean. */
    private String m_entryPoint;

    /** True if this is a dummy runtime info object. */
    private boolean m_isDummy;

    /** The locale of the corresponding sitemap. */
    private Locale m_locale;

    /** The runtime info object of the parent sitemap. */
    private CmsSitemapRuntimeInfo m_parent;

    /** The creatable resource types for the given sitemap. */
    private List<I_CmsResourceType> m_resourceTypes = new ArrayList<I_CmsResourceType>();

    /**
     * Creates a dummy runtime info object.<p>
     */
    public CmsSitemapRuntimeInfo() {

        m_isDummy = true;
    }

    /**
     * Creates a new runtime info object for a given entry point and locale.<p>
     * 
     * @param entryPoint the entry point of the sitemap 
     * @param locale the locale of the sitemap 
     */
    public CmsSitemapRuntimeInfo(String entryPoint, Locale locale) {

        m_entryPoint = entryPoint;
        m_locale = locale;
    }

    /**
     * Adds a runtime info bean for a sub-sitemap.<p>
     * 
     * @param info the runtime info bean of the sub-sitemap 
     */
    public void addChildInfo(CmsSitemapRuntimeInfo info) {

        info.m_parent = this;
        m_children.add(info);
    }

    /**
     * Adds a configured detail page to the runtime info for this sitemap.<p>
     * 
     * @param info an info bean for the detail page 
     */
    public void addDetailPageInfo(CmsDetailPageInfo info) {

        m_detailPageTable.add(info);
    }

    /**
     * Adds a new resource type.<p>
     * 
     * @param type the resource type which should be added 
     */
    public void addResourceType(I_CmsResourceType type) {

        m_resourceTypes.add(type);
    }

    /**
     * Removes all children.<p>
     */
    public void clear() {

        m_children.clear();
    }

    /**
     * Returns the best detail page for the given type, or null if there is no detail page for the given type.<p>
     * 
     * @param type the type which the detail page should render 
     * 
     * @return the bean representing the best detail page for the type 
     */
    public CmsDetailPageInfo getBestDetailPage(String type) {

        if (m_isDummy || (m_parent == null)) {
            return null;
        }
        CmsDetailPageInfo result = m_detailPageTable.getBestDetailPage(type);
        if (result == null) {
            result = m_parent.getBestDetailPage(type);
        }
        return result;
    }

    /**
     * Returns the sub-sitemap runtime info beans.<p>
     * 
     * @return the sub-sitemap runtime info beans 
     */
    public List<CmsSitemapRuntimeInfo> getChildren() {

        return Collections.unmodifiableList(m_children);
    }

    /**
     * Returns the configuration data.<p>
     * 
     * @return the configuration data 
     */
    public CmsConfigurationParser getConfiguration() {

        return m_configuration;
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
     * Returns the entry point for the sitemap.<p>
     * 
     * @return the entry point for the sitemap 
     */
    public String getEntryPoint() {

        return m_entryPoint;
    }

    /**
     * Returns the locale for the sitemap.<p>
     * 
     * @return the locale for the sitemap 
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the resource types.<p>
     * 
     * @return the resource types 
     */
    public List<I_CmsResourceType> getResourceTypes() {

        return Collections.unmodifiableList(m_resourceTypes);
    }

    /**
     * Returns true if this is a dummy sitemap runtime info object.<p>
     *  
     * @return true if this is a dummy sitemap runtime info object
     */
    public boolean isDummy() {

        return m_isDummy;
    }

    /**
     * Returns true if this info bean corresponds to a root-level sitemap.<p>
     * 
     * @return true if the corresponding sitemap is a root-level sitemap 
     */
    public boolean isRootSitemap() {

        return (m_parent != null) || m_parent.isDummy();
    }

    /** 
     * Sets the configuration for the given sitemap.<p>
     * 
     * @param configuration the configuration data 
     */
    public void setConfiguration(CmsConfigurationParser configuration) {

        m_configuration = configuration;
    }

    /**
     * Sets the detail page table.
     *
     * @param table the new detail page table
     */
    public void setDetailPageTable(CmsDetailPageTable table) {

        m_detailPageTable = table;
    }

}

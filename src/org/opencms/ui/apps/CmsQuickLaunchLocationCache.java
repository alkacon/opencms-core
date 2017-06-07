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

package org.opencms.ui.apps;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * Stores the last opened locations for file explorer, page editor and sitemap editor.<p>
 */
public class CmsQuickLaunchLocationCache implements Serializable {

    /** The serial version id. */
    private static final long serialVersionUID = -6144984854691623070L;

    /** The page editor locations. */
    private Map<String, String> m_pageEditorLocations;

    /** The sitemap editor locations. */
    private Map<String, String> m_sitemapEditorLocations;

    /** The file explorer locations. */
    private Map<String, String> m_fileExplorerLocations;

    /**
     * Constructor.<p>
     */
    public CmsQuickLaunchLocationCache() {
        m_pageEditorLocations = new HashMap<String, String>();
        m_sitemapEditorLocations = new HashMap<String, String>();
        m_fileExplorerLocations = new HashMap<String, String>();
    }

    /**
     * Returns the location cache from the user session.<p>
     *
     * @param session the session
     *
     * @return the location cache
     */
    public static CmsQuickLaunchLocationCache getLocationCache(HttpSession session) {

        CmsQuickLaunchLocationCache cache = (CmsQuickLaunchLocationCache)session.getAttribute(
            CmsQuickLaunchLocationCache.class.getName());
        if (cache == null) {
            cache = new CmsQuickLaunchLocationCache();
            session.setAttribute(CmsQuickLaunchLocationCache.class.getName(), cache);
        }
        return cache;
    }

    /**
     * Returns the file explorer location for the given site root.<p>
     *
     * @param siteRoot the site root
     *
     * @return the location
     */
    public String getFileExplorerLocation(String siteRoot) {

        return m_fileExplorerLocations.get(siteRoot);
    }

    /**
     * Returns the page editor location for the given site root.<p>
     *
     * @param siteRoot the site root
     *
     * @return the location
     */
    public String getPageEditorLocation(String siteRoot) {

        return m_pageEditorLocations.get(siteRoot);
    }

    /**
     * Returns the sitemap editor location for the given site root.<p>
     *
     * @param siteRoot the site root
     *
     * @return the location
     */
    public String getSitemapEditorLocation(String siteRoot) {

        return m_sitemapEditorLocations.get(siteRoot);
    }

    /**
     * Sets the latest file explorer location for the given site.<p>
     *
     * @param siteRoot the site root
     * @param location the location
     */
    public void setFileExplorerLocation(String siteRoot, String location) {

        m_fileExplorerLocations.put(siteRoot, location);
    }

    /**
     * Sets the latest page editor location for the given site.<p>
     *
     * @param siteRoot the site root
     * @param location the location
     */
    public void setPageEditorLocation(String siteRoot, String location) {

        m_pageEditorLocations.put(siteRoot, location);
    }

    /**
     * Sets the latest sitemap editor location for the given site.<p>
     *
     * @param siteRoot the site root
     * @param location the location
     */
    public void setSitemapEditorLocation(String siteRoot, String location) {

        m_sitemapEditorLocations.put(siteRoot, location);
    }
}

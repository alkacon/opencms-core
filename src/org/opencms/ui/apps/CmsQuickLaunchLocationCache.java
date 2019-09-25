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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Stores the last opened locations for file explorer, page editor and sitemap editor.<p>
 */
public class CmsQuickLaunchLocationCache implements Serializable {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsQuickLaunchLocationCache.class);

    /** The serial version id. */
    private static final long serialVersionUID = -6144984854691623070L;

    /** The page editor locations. */
    private Map<String, CmsResource> m_pageEditorResources = new HashMap<>();

    /** The sitemap editor locations. */
    private Map<String, String> m_sitemapEditorLocations;

    /** The file explorer locations. */
    private Map<String, String> m_fileExplorerLocations;

    /**
     * Constructor.<p>
     */
    public CmsQuickLaunchLocationCache() {

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
     * @param cms the current CMS context
     * @param siteRoot the site root
     *
     * @return the location
     */
    public String getPageEditorLocation(CmsObject cms, String siteRoot) {

        CmsResource res = m_pageEditorResources.get(siteRoot);
        if (res == null) {
            return null;
        }
        try {
            String sitePath = cms.getSitePath(res);
            cms.readResource(sitePath, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            return sitePath;
        } catch (CmsVfsResourceNotFoundException e) {
            try {
                CmsResource newRes = cms.readResource(res.getStructureId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(newRes.getRootPath());
                if (site == null) {
                    return null;
                }
                if (normalizePath(site.getSiteRoot()).equals(normalizePath(siteRoot))) {
                    return cms.getSitePath(newRes);
                } else {
                    return null;
                }

            } catch (CmsVfsResourceNotFoundException e2) {
                return null;
            } catch (CmsException e2) {
                LOG.error(e.getLocalizedMessage(), e2);
                return null;
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }

    }

    /**
     * Gets the cached location resource for the given site root.
     *
     * @param siteRoot the site root
     * @return the location resource
     */
    public CmsResource getPageEditorResource(String siteRoot) {

        return m_pageEditorResources.get(siteRoot);
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
     * @param resource the location resource
     */
    public void setPageEditorResource(String siteRoot, CmsResource resource) {

        m_pageEditorResources.put(siteRoot, resource);
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

    /**
     * Ensures the given path begins and ends with a slash.
     *
     * @param path the path
     * @return the normalized path
     */
    private String normalizePath(String path) {

        return CmsStringUtil.joinPaths("/", path, "/");
    }
}

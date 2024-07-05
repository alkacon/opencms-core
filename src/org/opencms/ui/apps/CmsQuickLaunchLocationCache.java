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

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavBuilder.Visibility;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Stores the last opened locations for file explorer, page editor and sitemap editor.<p>
 */
public class CmsQuickLaunchLocationCache implements Serializable {

    /**
     * Contains contextual information for the last edited page, to find a page "near" it to edit in case the original page was deleted.
     */
    static class PageLocationWithContext {

        /** The original page. */
        private CmsResource m_resource;

        /** True if this was a non-container page resource. */
        private boolean m_isNotPage;

        /** The original navigation position. */
        private float m_navPos;

        /** The ancestor folders, if available. */
        private List<CmsResource> m_ancestors = new ArrayList<>();

        /** The navigation start resource, if available. */
        private CmsResource m_navResource;

        public PageLocationWithContext(CmsObject cms, CmsResource resource) {

            m_resource = resource;
            if (!CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
                m_isNotPage = true;
            } else {
                try {
                    Map<String, CmsProperty> props = CmsProperty.getPropertyMap(
                        cms.readPropertyObjects(resource, false));
                    if (hasNavigationProps(props)) {
                        initNavigationData(cms, resource, props);
                    } else {
                        CmsResource parent = cms.readParentFolder(m_resource.getStructureId());
                        Map<String, CmsProperty> parentProps = CmsProperty.getPropertyMap(
                            cms.readPropertyObjects(parent, false));
                        if (hasNavigationProps(parentProps)) {
                            initNavigationData(cms, parent, parentProps);
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }

        /**
         * Does the same thing as getNearestPageInternal, but as an additional fallback, tries to read any
         * container page of the subsitemap if the former returns null.
         *
         * @param cms the current CMS context
         * @return the 'nearest' page to the original page
         */
        public CmsResource getNearestPage(CmsObject cms) {

            CmsResource result = getNearestPageInternal(cms);
            if (result == null) {
                try {
                    CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
                        cms,
                        m_resource.getRootPath());
                    if (CmsStringUtil.isPrefixPath(cms.getRequestContext().getSiteRoot(), config.getBasePath())) {
                        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(
                            CmsResourceTypeXmlContainerPage.RESOURCE_TYPE_NAME);
                        List<CmsResource> resources = cms.readResources(
                            cms.getRequestContext().removeSiteRoot(config.getBasePath()),
                            CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(type),
                            true);
                        for (CmsResource resource : resources) {
                            if (resource.getRootPath().endsWith("/index.html")) {
                                return resource;
                            }
                        }
                        for (CmsResource resource : resources) {
                            return resource;
                        }
                    }

                } catch (Exception e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }
            return result;
        }

        /**
         * Gets the navigation position from a map of properties.
         *
         * @param props the map of properties
         * @return the navigation position
         */
        private float getNavPos(Map<String, CmsProperty> props) {

            float result = Float.MAX_VALUE;

            CmsProperty prop = props.get(CmsPropertyDefinition.PROPERTY_NAVPOS);
            if (prop != null) {
                try {
                    result = Float.parseFloat(prop.getValue());
                } catch (Exception e) {
                    /*ignore*/
                }
            }
            return result;
        }

        /**
         * Gets the 'nearest' page to the original resource.
         *
         *  <ul>
         *  <li>check if the original resource exists and return it if so
         *  <li>try to find the default file of the parent folder of the original resource, and return it if it exists
         *  <li>try the page with the smallest navigation position greater than the original resource's navigation position in its original folder
         *  <li>try the page with the greatest navigation position less than  the original resource's navigation position in its original folder
         *  </ul>
         *
         * @param cms the CMS context
         * @return
         */
        private CmsResource getNearestPageInternal(CmsObject cms) {

            if (cms.existsResource(m_resource.getStructureId(), CmsResourceFilter.IGNORE_EXPIRATION)) {
                return m_resource;
            }
            if (m_isNotPage) {
                return null;
            }
            if ((m_navResource != m_resource)
                && (m_navResource != null)
                && cms.existsResource(m_navResource.getStructureId(), CmsResourceFilter.IGNORE_EXPIRATION)) {
                try {
                    CmsResource defaultFile = cms.readDefaultFile(
                        cms.getRequestContext().getSitePath(m_navResource),
                        CmsResourceFilter.IGNORE_EXPIRATION);
                    if (defaultFile != null) {
                        return defaultFile;
                    }
                } catch (Exception e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }
            CmsJspNavBuilder builder = new CmsJspNavBuilder();
            builder.init(cms, Locale.ENGLISH);
            for (int ancestorIndex = 0; ancestorIndex < m_ancestors.size(); ancestorIndex++) {
                try {
                    // re-read to ensure path is correct
                    CmsResource ancestor = cms.readResource(
                        m_ancestors.get(ancestorIndex).getStructureId(),
                        CmsResourceFilter.IGNORE_EXPIRATION);
                    if (!CmsStringUtil.isPrefixPath(cms.getRequestContext().getSiteRoot(), ancestor.getRootPath())) {
                        return null;
                    }
                    if (ancestorIndex == 0) {
                        List<CmsJspNavElement> navigation = builder.getNavigationForFolder(
                            cms.getRequestContext().getSitePath(ancestor),
                            Visibility.navigation,
                            CmsResourceFilter.IGNORE_EXPIRATION);
                        List<CmsJspNavElement> before = new ArrayList<>();
                        List<CmsJspNavElement> after = new ArrayList<>();
                        for (CmsJspNavElement elem : navigation) {
                            if (elem.getNavPosition() < m_navPos) {
                                before.add(elem);
                            } else {
                                after.add(elem);
                            }
                        }

                        for (CmsJspNavElement afterElem : after) {
                            try {
                                // this is *not* always the same as afterElem.getResource() !
                                CmsResource candidate = cms.readResource(
                                    afterElem.getResourceName(),
                                    CmsResourceFilter.IGNORE_EXPIRATION);
                                return candidate;
                            } catch (CmsException e) {
                                LOG.debug(e.getLocalizedMessage(), e);
                            }
                        }
                        Collections.reverse(before);
                        for (CmsJspNavElement beforeElem : before) {
                            try {
                                // this is *not* always the same as beforeElem.getResource() !
                                CmsResource candidate = cms.readResource(
                                    beforeElem.getResourceName(),
                                    CmsResourceFilter.IGNORE_EXPIRATION);
                                return candidate;
                            } catch (CmsException e) {
                                LOG.debug(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                    CmsJspNavElement ancestorElem = builder.getNavigationForResource(
                        cms.getRequestContext().getSitePath(ancestor),
                        CmsResourceFilter.IGNORE_EXPIRATION);
                    if ((ancestorElem != null) && ancestorElem.isInNavigation()) {
                        try {
                            CmsResource candidate = cms.readResource(
                                ancestorElem.getResourceName(),
                                CmsResourceFilter.IGNORE_EXPIRATION);
                            return candidate;
                        } catch (CmsException e) {
                            LOG.debug(e.getLocalizedMessage(), e);
                        }
                    }

                } catch (Exception e) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }
            return null;
        }

        /**
         * Checks if the property map contains navigation properties.
         *
         * @param props the navigation properties
         * @return true if navigation properties exist in the property map
         */
        private boolean hasNavigationProps(Map<String, CmsProperty> props) {

            return props.containsKey(CmsPropertyDefinition.PROPERTY_NAVTEXT)
                || props.containsKey(CmsPropertyDefinition.PROPERTY_NAVPOS);
        }

        /**
         * Initializes the context information for the given navigation resource.
         * @param cms the CMS context
         * @param navResource a resource in the navigation
         * @param props the properties of the navigation resource
         */
        private void initNavigationData(CmsObject cms, CmsResource navResource, Map<String, CmsProperty> props) {

            m_navPos = getNavPos(props);
            m_navResource = navResource;
            CmsResource currentResource = navResource;
            while (true) {
                if (cms.getRequestContext().getSitePath(currentResource).equals("/")) {
                    break;
                }
                try {
                    currentResource = cms.readParentFolder(currentResource.getStructureId());
                    if (currentResource != null) {
                        m_ancestors.add(currentResource);
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    break;
                }
            }
        }
    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsQuickLaunchLocationCache.class);

    /** The serial version id. */
    private static final long serialVersionUID = -6144984854691623070L;

    private Map<String, PageLocationWithContext> m_pageEditorLocations = new ConcurrentHashMap<String, CmsQuickLaunchLocationCache.PageLocationWithContext>();

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

        PageLocationWithContext location = m_pageEditorLocations.get(siteRoot);
        CmsResource res = null;
        if (location != null) {
            res = location.getNearestPage(cms);
        }

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
    public void setPageEditorResource(CmsObject cms, String siteRoot, CmsResource resource) {

        PageLocationWithContext location = new PageLocationWithContext(cms, resource);
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

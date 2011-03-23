/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/site/CmsSiteManagerImpl.java,v $
 * Date   : $Date: 2011/03/23 14:51:56 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.site;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Manages all configured sites in OpenCms.<p>
 *
 * To obtain the configured site manager instance, use {@link OpenCms#getSiteManager()}.<p>
 *
 * @author  Alexander Kandzior 
 *
 * @version $Revision: 1.13 $ 
 * 
 * @since 7.0.2
 */
public final class CmsSiteManagerImpl {

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSiteManagerImpl.class);

    /** The path to the "/sites/" folder. */
    private static final String SITES_FOLDER = "/sites/";

    /** The length of the "/sites/" folder plus 1. */
    private static final int SITES_FOLDER_POS = SITES_FOLDER.length() + 1;

    /** A list of additional site roots, that is site roots that are not belows the "/sites/" folder. */
    private List m_additionalSiteRoots;

    /** 
     * The list of aliases for the site that is configured at the moment, 
     * needed for the sites added during configuration. */
    private List m_aliases;

    /** The default site root. */
    private CmsSite m_defaultSite;

    /** The default URI. */
    private String m_defaultUri;

    /** Indicates if the configuration is finalized (frozen). */
    private boolean m_frozen;

    /** List to access the time offsets. */
    private List m_matchers;

    /** Maps site matchers to sites. */
    private Map m_siteMatcherSites;

    /** The set of all configured site root paths (as String). */
    private Set m_siteRoots;

    /** Maps site roots to sites. */
    private Map m_siteRootSites;

    /** The workplace server. */
    private String m_workplaceServer;

    /** The site matcher that matches the workplace site. */
    private CmsSiteMatcher m_workplaceSiteMatcher;

    /**
     * Creates a new CmsSiteManager.<p>
     *
     */
    public CmsSiteManagerImpl() {

        m_siteMatcherSites = new HashMap();
        m_siteRootSites = new HashMap();
        m_aliases = new ArrayList();
        m_matchers = new ArrayList();
        m_additionalSiteRoots = new ArrayList();

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_START_SITE_CONFIG_0));
        }
    }

    /**
     * Adds an alias to the currently configured site.
     * 
     * @param alias the URL of the alias server
     * @param offset the optional time offset for this alias
     */
    public void addAliasToConfigSite(String alias, String offset) {

        long timeOffset = 0;
        try {
            timeOffset = Long.parseLong(offset);
        } catch (Throwable e) {
            // ignore
        }
        CmsSiteMatcher siteMatcher = new CmsSiteMatcher(alias, timeOffset);
        m_aliases.add(siteMatcher);
    }

    /**
     * Adds a new CmsSite to the list of configured sites, 
     * this is only allowed during configuration.<p>
     * 
     * If this method is called after the configuration is finished, 
     * a <code>RuntimeException</code> is thrown.<p>
     * 
     * @param server the Server
     * @param uri the VFS path
     * @param secureServer a secure server, can be <code>null</code>
     * @param exclusive if set to <code>true</code>, secure resources will only be available using the configured secure url
     * @param error if exclusive, and set to <code>true</code> will generate a 404 error, 
     *                             if set to <code>false</code> will redirect to secure URL
     * 
     * @throws CmsConfigurationException if the site contains a server name, that is already assigned
     */
    public void addSite(String server, String uri, String secureServer, String exclusive, String error)
    throws CmsConfigurationException {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }
        CmsSiteMatcher matcher = new CmsSiteMatcher(server);
        CmsSite site = new CmsSite(uri, matcher);
        addServer(matcher, site);
        if (CmsStringUtil.isNotEmpty(secureServer)) {
            matcher = new CmsSiteMatcher(secureServer);
            site.setSecureServer(matcher);
            site.setExclusiveUrl(Boolean.valueOf(exclusive).booleanValue());
            site.setExclusiveError(Boolean.valueOf(error).booleanValue());
            addServer(matcher, site);
        }

        // note that Digester first calls the addAliasToConfigSite method.
        // therefore, the aliases are already set
        site.setAliases(m_aliases);
        Iterator i = m_aliases.iterator();
        while (i.hasNext()) {
            matcher = (CmsSiteMatcher)i.next();
            addServer(matcher, site);
        }
        m_aliases = new ArrayList();
        m_siteRootSites.put(site.getSiteRoot(), site);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SITE_ROOT_ADDED_1, site.toString()));
        }
    }

    /**
     * Returns a list of all sites available for the current user.<p>
     * 
     * @param cms the current OpenCms user context 
     * @param workplaceMode if true, the root and current site is included for the admin user
     *                      and the view permission is required to see the site root
     * 
     * @return a list of all site available for the current user
     */
    public List getAvailableSites(CmsObject cms, boolean workplaceMode) {

        return getAvailableSites(cms, workplaceMode, cms.getRequestContext().getOuFqn());
    }

    /**
     * Returns a list of all {@link CmsSite} instances that are compatible to the given organizational unit.<p>
     * 
     * @param cms the current OpenCms user context 
     * @param workplaceMode if true, the root and current site is included for the admin user
     *                      and the view permission is required to see the site root
     * @param ouFqn the organizational unit
     * 
     * @return a list of all site available for the current user
     */
    public List getAvailableSites(CmsObject cms, boolean workplaceMode, String ouFqn) {

        List siteroots = new ArrayList(m_siteMatcherSites.size() + 1);
        Map siteServers = new HashMap(m_siteMatcherSites.size() + 1);
        List result = new ArrayList(m_siteMatcherSites.size() + 1);

        Iterator i;
        // add site list
        i = m_siteMatcherSites.keySet().iterator();
        while (i.hasNext()) {
            CmsSite site = (CmsSite)m_siteMatcherSites.get(i.next());
            String folder = site.getSiteRoot() + "/";
            if (!siteroots.contains(folder)) {
                siteroots.add(folder);
                siteServers.put(folder, site.getSiteMatcher());
            }
        }
        // add default site
        if (workplaceMode && (m_defaultSite != null)) {
            String folder = m_defaultSite.getSiteRoot() + "/";
            if (!siteroots.contains(folder)) {
                siteroots.add(folder);
            }
        }

        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            // for all operations here we need no context
            cms.getRequestContext().setSiteRoot("/");
            if (workplaceMode && OpenCms.getRoleManager().hasRole(cms, CmsRole.DEVELOPER)) {
                if (!siteroots.contains("/")) {
                    siteroots.add("/");
                }
                if (!siteroots.contains(storedSiteRoot + "/")) {
                    siteroots.add(storedSiteRoot + "/");
                }
            }
            List resources;
            try {
                resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ouFqn);
            } catch (CmsException e) {
                return Collections.EMPTY_LIST;
            }

            Collections.sort(siteroots); // sort by resource name
            i = siteroots.iterator();
            while (i.hasNext()) {
                String folder = (String)i.next();
                boolean compatible = false;
                Iterator itResources = resources.iterator();
                while (itResources.hasNext()) {
                    CmsResource resource = (CmsResource)itResources.next();
                    if (resource.getRootPath().startsWith(folder) || folder.startsWith(resource.getRootPath())) {
                        compatible = true;
                        break;
                    }
                }
                // select only sites compatibles to the given organizational unit 
                if (compatible) {
                    try {
                        CmsResource res = cms.readResource(folder);
                        if (!workplaceMode
                            || cms.hasPermissions(
                                res,
                                CmsPermissionSet.ACCESS_VIEW,
                                false,
                                CmsResourceFilter.ONLY_VISIBLE)) {
                            String title = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
                            if (title == null) {
                                title = folder;
                            }
                            String position = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_NAVPOS, false).getValue();
                            result.add(new CmsSite(
                                folder,
                                res.getStructureId(),
                                title,
                                (CmsSiteMatcher)siteServers.get(folder),
                                position));
                        }
                    } catch (CmsException e) {
                        // user probably has no read access to the folder, ignore and continue iterating            
                    }
                }
            }

            Collections.sort(result);
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_READ_SITE_PROP_FAILED_0), t);
        } finally {
            // restore the user's current context 
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
        }
        return result;
    }

    /**
     * Returns the current site for the provided OpenCms user context object.<p>
     * 
     * In the unlikely case that no site matches with the provided OpenCms user context,
     * the default site is returned.<p>
     * 
     * @param cms the OpenCms user context object to check for the site
     * 
     * @return the current site for the provided OpenCms user context object
     */
    public CmsSite getCurrentSite(CmsObject cms) {

        CmsSite site = getSiteForSiteRoot(cms.getRequestContext().getSiteRoot());
        return (site == null) ? m_defaultSite : site;
    }

    /**
     * Returns the default site.<p>
     * 
     * @return the default site
     */
    public CmsSite getDefaultSite() {

        return m_defaultSite;
    }

    /**
     * Returns the defaultUri.<p>
     *
     * @return the defaultUri
     */
    public String getDefaultUri() {

        return m_defaultUri;
    }

    /**
     * Returns the site with has the provided site root, 
     * or <code>null</code> if no configured site has that site root.<p>
     * 
     * The site root must have the form:
     * <code>/sites/default</code>.<br>
     * That means there must be a leading, but no trailing slash.<p>
     * 
     * @param siteRoot the site root to look up the site for
     * 
     * @return the site with has the provided site root, 
     *      or <code>null</code> if no configured site has that site root
     *      
     * @deprecated use {@link #getSiteForSiteRoot(String)} instead
     * 
     * @see #getSiteForSiteRoot(String)
     */
    public CmsSite getSite(String siteRoot) {

        return (CmsSite)m_siteRootSites.get(siteRoot);
    }

    /**
     * Returns the site for the given resource path, using the fall back site root
     * in case the resource path is no root path.<p>
     * 
     * In case neither the given resource path, nor the given fall back site root 
     * matches any configured site, the default site is returned.<p>
     * 
     * Usually the fall back site root should be taken from {@link org.opencms.file.CmsRequestContext#getSiteRoot()},
     * in which case a site for the site root should always exist.<p>
     * 
     * This is the same as first calling {@link #getSiteForRootPath(String)} with the 
     * <code>resourcePath</code> parameter, and if this fails calling 
     * {@link #getSiteForSiteRoot(String)} with the <code>fallbackSiteRoot</code> parameter,
     * and if this fails calling {@link #getDefaultSite()}.<p>
     * 
     * @param rootPath the resource root path to get the site for
     * @param fallbackSiteRoot site root to use in case the resource path is no root path
     * 
     * @return the site for the given resource path, using the fall back site root
     *      in case the resource path is no root path
     *      
     * @see #getSiteForRootPath(String)
     */
    public CmsSite getSite(String rootPath, String fallbackSiteRoot) {

        CmsSite result = getSiteForRootPath(rootPath);
        if (result == null) {
            result = getSiteForSiteRoot(fallbackSiteRoot);
            if (result == null) {
                result = getDefaultSite();
            }
        }
        return result;
    }

    /**
     * Returns the site for the given resources root path, 
     * or <code>null</code> if the resources root path does not match any site.<p>
     * 
     * @param rootPath the root path of a resource
     * 
     * @return the site for the given resources root path, 
     *      or <code>null</code> if the resources root path does not match any site
     *      
     * @see #getSiteForSiteRoot(String)
     * @see #getSiteRoot(String)
     */
    public CmsSite getSiteForRootPath(String rootPath) {

        // most sites will be below the "/sites/" folder, 
        CmsSite result = lookupSitesFolder(rootPath);
        if (result != null) {
            return result;
        }
        // look through all folders that are not below "/sites/"
        String siteRoot = lookupAdditionalSite(rootPath);
        return (siteRoot != null) ? getSiteForSiteRoot(siteRoot) : null;
    }

    /**
     * Returns the site with has the provided site root, 
     * or <code>null</code> if no configured site has that site root.<p>
     * 
     * The site root must have the form:
     * <code>/sites/default</code>.<br>
     * That means there must be a leading, but no trailing slash.<p>
     * 
     * @param siteRoot the site root to look up the site for
     * 
     * @return the site with has the provided site root, 
     *      or <code>null</code> if no configured site has that site root
     *      
     * @see #getSiteForRootPath(String)
     */
    public CmsSite getSiteForSiteRoot(String siteRoot) {

        return (CmsSite)m_siteRootSites.get(siteRoot);
    }

    /**
     * Returns the site root part for the given resources root path, 
     * or <code>null</code> if the given resources root path does not match any site root.<p>
     * 
     * The site root returned will have the form:
     * <code>/sites/default</code>.<br>
     * That means there will a leading, but no trailing slash.<p>
     * 
     * @param rootPath the root path of a resource
     * 
     * @return the site root part of the resources root path, 
     *      or <code>null</code> if the path does not match any site root
     *      
     * @see #getSiteForRootPath(String)
     */
    public String getSiteRoot(String rootPath) {

        // most sites will be below the "/sites/" folder, 
        CmsSite site = lookupSitesFolder(rootPath);
        if (site != null) {
            return site.getSiteRoot();
        }
        // look through all folders that are not below "/sites/"
        return lookupAdditionalSite(rootPath);
    }

    /**
     * Returns an unmodifiable set of all configured site roots (Strings).<p>
     *  
     * @return an unmodifiable set of all configured site roots (Strings)
     */
    public Set getSiteRoots() {

        return m_siteRoots;
    }

    /**
     * Returns the map of configured sites, using 
     * {@link CmsSiteMatcher} objects as keys and {@link CmsSite} objects as values.<p>
     * 
     * @return the map of configured sites, using {@link CmsSiteMatcher} 
     *      objects as keys and {@link CmsSite} objects as values
     */
    public Map getSites() {

        return m_siteMatcherSites;
    }

    /**
     * Returns the workplace server.<p>
     *
     * @return the workplace server
     */
    public String getWorkplaceServer() {

        return m_workplaceServer;
    }

    /**
     * Returns the site matcher that matches the workplace site.<p>
     * 
     * @return the site matcher that matches the workplace site
     */
    public CmsSiteMatcher getWorkplaceSiteMatcher() {

        return m_workplaceSiteMatcher;
    }

    /**
     * Initializes the site manager with the OpenCms system configuration.<p>
     * 
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     */
    public void initialize(CmsObject cms) {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_NUM_SITE_ROOTS_CONFIGURED_1,
                new Integer((m_siteMatcherSites.size() + ((m_defaultUri != null) ? 1 : 0)))));
        }

        // check the presence of sites in VFS
        Iterator i = m_siteMatcherSites.values().iterator();
        while (i.hasNext()) {
            CmsSite site = (CmsSite)i.next();
            if (site != null) {
                try {
                    cms.readResource(site.getSiteRoot());
                } catch (Throwable t) {
                    if (CmsLog.INIT.isWarnEnabled()) {
                        CmsLog.INIT.warn(Messages.get().getBundle().key(Messages.INIT_NO_ROOT_FOLDER_1, site));
                    }
                }
            }
        }

        // check the presence of the default site in VFS
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_defaultUri)) {
            m_defaultSite = null;
        } else {
            m_defaultSite = new CmsSite(m_defaultUri, CmsSiteMatcher.DEFAULT_MATCHER);
            try {
                cms.readResource(m_defaultSite.getSiteRoot());
            } catch (Throwable t) {
                if (CmsLog.INIT.isWarnEnabled()) {
                    CmsLog.INIT.warn(Messages.get().getBundle().key(
                        Messages.INIT_NO_ROOT_FOLDER_DEFAULT_SITE_1,
                        m_defaultSite));
                }
            }
        }
        if (m_defaultSite == null) {
            m_defaultSite = new CmsSite("/", CmsSiteMatcher.DEFAULT_MATCHER);
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            if (m_defaultSite != null) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DEFAULT_SITE_ROOT_1, m_defaultSite));
            } else {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DEFAULT_SITE_ROOT_0));
            }
        }
        m_workplaceSiteMatcher = new CmsSiteMatcher(m_workplaceServer);
        if (CmsLog.INIT.isInfoEnabled()) {
            if (m_workplaceSiteMatcher != null) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_WORKPLACE_SITE_1, m_workplaceSiteMatcher));
            } else {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_WORKPLACE_SITE_0));
            }
        }

        // set site lists to unmodifiable 
        m_siteMatcherSites = Collections.unmodifiableMap(m_siteMatcherSites);
        m_matchers = Collections.unmodifiableList(m_matchers);
        m_siteRoots = Collections.unmodifiableSet(m_siteRootSites.keySet());

        // store additional site roots to optimize lookups later
        Iterator j = m_siteRoots.iterator();
        while (j.hasNext()) {
            String root = (String)j.next();
            if (!root.startsWith(SITES_FOLDER)) {
                m_additionalSiteRoots.add(root);
            }
        }

        // initialization is done, set the frozen flag to true 
        m_frozen = true;
    }

    /**
     * Returns <code>true</code> if the given site matcher matches any configured site,
     * which includes the workplace site.<p>
     * 
     * @param matcher the site matcher to match the site with
     * 
     * @return <code>true</code> if the matcher matches a site
     */
    public boolean isMatching(CmsSiteMatcher matcher) {

        boolean result = m_siteMatcherSites.get(matcher) != null;
        if (!result) {
            // try to match the workplace site
            result = (m_workplaceSiteMatcher != null) && m_workplaceSiteMatcher.equals(matcher);
        }
        return result;
    }

    /**
     * Returns <code>true</code> if the given site matcher matches the current site.<p>
     * 
     * @param cms the current OpenCms user context 
     * @param matcher the site matcher to match the site with
     * 
     * @return <code>true</code> if the matcher matches the current site
     */
    public boolean isMatchingCurrentSite(CmsObject cms, CmsSiteMatcher matcher) {

        return m_siteMatcherSites.get(matcher) == getCurrentSite(cms);
    }

    /**
     * Returns <code>true</code> if the given site matcher matches the configured OpenCms workplace.<p> 
     * 
     * @param matcher the site matcher to match the site with
     * 
     * @return <code>true</code> if the given site matcher matches the configured OpenCms workplace
     */
    public boolean isWorkplaceRequest(CmsSiteMatcher matcher) {

        return (m_workplaceSiteMatcher != null) && m_workplaceSiteMatcher.equals(matcher);
    }

    /**
     * Returns <code>true</code> if the given request is against the configured OpenCms workplace.<p> 
     * 
     * @param req the request to match 
     * 
     * @return <code>true</code> if the given request is against the configured OpenCms workplace
     */
    public boolean isWorkplaceRequest(HttpServletRequest req) {

        if (req == null) {
            // this may be true inside a static export test case scenario
            return false;
        }
        return isWorkplaceRequest(getRequestMatcher(req));
    }

    /**
     * Matches the given request against all configures sites and returns 
     * the matching site, or the default site if no sites matches.<p>
     * 
     * @param req the request to match 
     * 
     * @return the matching site, or the default site if no sites matches
     */
    public CmsSite matchRequest(HttpServletRequest req) {

        CmsSiteMatcher matcher = getRequestMatcher(req);
        if (matcher.getTimeOffset() != 0) {
            HttpSession session = req.getSession();
            if (session != null) {
                session.setAttribute(CmsContextInfo.ATTRIBUTE_REQUEST_TIME, new Long(System.currentTimeMillis()
                    + matcher.getTimeOffset()));
            }
        }
        CmsSite site = matchSite(matcher);

        if (LOG.isDebugEnabled()) {
            String requestServer = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_MATCHING_REQUEST_TO_SITE_2,
                requestServer,
                site.toString()));
        }
        return site;
    }

    /**
     * Return the configured site that matches the given site matcher,
     * or the default site if no sites matches.<p>
     * 
     * @param matcher the site matcher to match the site with
     * @return the matching site, or the default site if no sites matches
     */
    public CmsSite matchSite(CmsSiteMatcher matcher) {

        CmsSite site = (CmsSite)m_siteMatcherSites.get(matcher);
        if (site == null) {
            // return the default site (might be null as well)
            site = m_defaultSite;
        }
        return site;
    }

    /**
     * Sets the default URI, this is only allowed during configuration.<p>
     * 
     * If this method is called after the configuration is finished, 
     * a <code>RuntimeException</code> is thrown.<p>
     * 
     * @param defaultUri the defaultUri to set
     */
    public void setDefaultUri(String defaultUri) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }
        m_defaultUri = defaultUri;
    }

    /**
     * Sets the workplace server, this is only allowed during configuration.<p>
     * 
     * If this method is called after the configuration is finished, 
     * a <code>RuntimeException</code> is thrown.<p>
     * 
     * @param workplaceServer the workplace server to set
     */
    public void setWorkplaceServer(String workplaceServer) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }
        m_workplaceServer = workplaceServer;
    }

    /**
     * Adds a new Site matcher object to the map of server names.
     * 
     * @param matcher the SiteMatcher of the server
     * @param site the site to add
     * 
     * @throws CmsConfigurationException if the site contains a server name, that is already assigned
     */
    private void addServer(CmsSiteMatcher matcher, CmsSite site) throws CmsConfigurationException {

        if (m_siteMatcherSites.containsKey(matcher)) {
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_DUPLICATE_SERVER_NAME_1,
                matcher.getUrl()));
        }
        m_matchers.add(matcher);
        m_siteMatcherSites.put(matcher, site);
    }

    /**
     * Returns the site matcher for the given request.<p>
     * 
     * @param req the request to get the site matcher for
     * 
     * @return the site matcher for the given request
     */
    private CmsSiteMatcher getRequestMatcher(HttpServletRequest req) {

        CmsSiteMatcher matcher = new CmsSiteMatcher(req.getScheme(), req.getServerName(), req.getServerPort());
        // this is needed to get the right configured time offset
        int index = m_matchers.indexOf(matcher);
        if (index < 0) {
            return matcher;
        }
        return (CmsSiteMatcher)m_matchers.get(index);
    }

    /**
     * Returns <code>true</code> if the given root path matches any of the stored additional sites.<p> 
     * 
     * @param rootPath the root path to check
     * 
     * @return <code>true</code> if the given root path matches any of the stored additional sites
     */
    private String lookupAdditionalSite(String rootPath) {

        for (int i = 0, size = m_additionalSiteRoots.size(); i < size; i++) {
            String siteRoot = (String)m_additionalSiteRoots.get(i);
            if (rootPath.startsWith(siteRoot)) {
                return siteRoot;
            }
        }
        return null;
    }

    /**
     * Returns the configured site if the given root path matches site in the "/sites/" folder, 
     * or <code>null</code> otherwise.<p> 
     * 
     * @param rootPath the root path to check
     * 
     * @return the configured site if the given root path matches site in the "/sites/" folder, 
     *      or <code>null</code> otherwise
     */
    private CmsSite lookupSitesFolder(String rootPath) {

        int pos = rootPath.indexOf('/', SITES_FOLDER_POS);
        if (pos > 0) {
            // this assumes that the root path may likely start with something like "/sites/default/" 
            // just cut the first 2 directories from the root path and do a direct lookup in the internal map
            return (CmsSite)m_siteRootSites.get(rootPath.substring(0, pos));
        }
        return null;
    }
}
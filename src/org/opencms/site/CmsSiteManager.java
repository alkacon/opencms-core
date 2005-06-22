/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/site/CmsSiteManager.java,v $
 * Date   : $Date: 2005/06/22 14:58:54 $
 * Version: $Revision: 1.45 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.site;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Manages all configured sites in OpenCms.<p>
 *
 * @author  Alexander Kandzior 
 *
 * @version $Revision: 1.45 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsSiteManager implements Cloneable {

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSiteManager.class);

    /** The site that is configured at the moment, need to be recorded for the case that
     * alias server are added during configuration. */
    private List m_aliases;

    /** The default site root. */
    private CmsSite m_defaultSite;

    /** The default uri. */
    private String m_defaultUri;

    /** Indicates if the configuration is finalized (frozen). */
    private boolean m_frozen;

    /** The set of all configured site root paths (as String). */
    private Set m_siteRoots;

    /** The map of configured sites. */
    private Map m_sites;

    /** The workplace server. */
    private String m_workplaceServer;

    /** The site matcher that matches the workplace site. */
    private CmsSiteMatcher m_workplaceSiteMatcher;

    /**
     * Creates a new CmsSiteManager.<p>
     *
     */
    public CmsSiteManager() {

        m_sites = new HashMap();
        m_siteRoots = new HashSet();
        m_aliases = new ArrayList();

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_START_SITE_CONFIG_0));
        }
    }

    /**
     * Returns a list of all site available for the current user.<p>
     * 
     * @param cms the current cms context 
     * @param workplaceMode if true, the root and current site is included for the admin user
     * and the view permission is required to see the site root
     * @return a list of all site available for the current user
     */
    public static List getAvailableSites(CmsObject cms, boolean workplaceMode) {

        Map sites = OpenCms.getSiteManager().getSites();
        List siteroots = new ArrayList(sites.size() + 1);
        Map siteServers = new HashMap(sites.size() + 1);
        List result = new ArrayList(sites.size() + 1);

        Iterator i;
        // add site list
        i = sites.keySet().iterator();
        while (i.hasNext()) {
            CmsSite site = (CmsSite)sites.get(i.next());
            String folder = site.getSiteRoot() + "/";
            if (!siteroots.contains(folder)) {
                siteroots.add(folder);
                siteServers.put(folder, site.getSiteMatcher());
            }
        }
        // add default site
        if (workplaceMode && OpenCms.getSiteManager().getDefaultSite() != null) {
            String folder = OpenCms.getSiteManager().getDefaultSite().getSiteRoot() + "/";
            if (!siteroots.contains(folder)) {
                siteroots.add(folder);
            }
        }

        String currentRoot = cms.getRequestContext().getSiteRoot();
        cms.getRequestContext().saveSiteRoot();
        try {
            // for all operations here we need no context
            cms.getRequestContext().setSiteRoot("/");
            if (workplaceMode && cms.hasRole(CmsRole.ROOT_FOLDER_ACCESS)) {
                if (!siteroots.contains("/")) {
                    siteroots.add("/");
                }
                if (!siteroots.contains(currentRoot + "/")) {
                    siteroots.add(currentRoot + "/");
                }
            }
            Collections.sort(siteroots);
            i = siteroots.iterator();
            while (i.hasNext()) {
                String folder = (String)i.next();
                try {
                    CmsResource res = cms.readResource(folder);
                    if (!workplaceMode || cms.hasPermissions(res, CmsPermissionSet.ACCESS_VIEW)) {
                        String title = cms.readPropertyObject(folder, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
                        if (title == null) {
                            title = folder;
                        }
                        result.add(new CmsSite(
                            folder,
                            res.getStructureId(),
                            title,
                            (CmsSiteMatcher)siteServers.get(folder)));
                    }

                } catch (CmsException e) {
                    // user probably has no read access to the folder, ignore and continue iterating            
                }
            }
        } catch (Throwable t) {
            LOG.error(Messages.get().key(Messages.LOG_READ_SITE_PROP_FAILED_0), t);
        } finally {
            // restore the user's current context 
            cms.getRequestContext().restoreSiteRoot();
        }
        return result;
    }

    /**
     * Returns the current site for the provided cms context object.<p>
     * 
     * @param cms the cms context object to check for the site
     * @return the current site for the provided cms context object
     */
    public static CmsSite getCurrentSite(CmsObject cms) {

        String siteRoot = cms.getRequestContext().getSiteRoot();
        CmsSite site = getSite(siteRoot);
        if (site == null) {
            return OpenCms.getSiteManager().getDefaultSite();
        } else {
            return site;
        }
    }

    /**
     * Returns the site with has the provided site root path, 
     * or null if no configured site has that root path.<p>
     * 
     * @param siteRoot the root path to look up the site for
     * @return the site with has the provided site root path, 
     *      or null if no configured site has that root path
     */
    public static CmsSite getSite(String siteRoot) {

        Map sites = OpenCms.getSiteManager().getSites();
        Iterator i = sites.keySet().iterator();
        while (i.hasNext()) {
            CmsSite site = (CmsSite)sites.get(i.next());
            if (siteRoot.equals(site.getSiteRoot())) {
                return site;
            }
        }
        return null;
    }

    /**
     * Returns the site root part of the resources root path, 
     * or null if the path does not match any site root.<p>
     * 
     * @param path the root path of a resource
     * @return the site root part of the resources root path, or null if the path does not match any site root
     */
    public static String getSiteRoot(String path) {

        Set roots = OpenCms.getSiteManager().getSiteRoots();
        // most sites will be subfolders of the "/sites/" folder, 
        int pos = path.indexOf('/', 7);
        if (pos > 0) {
            String candidate = path.substring(0, pos);
            if (roots.contains(candidate)) {
                return candidate;
            }
        }
        // site root not found as subfolder of "/sites/"
        Iterator i = roots.iterator();
        while (i.hasNext()) {
            String siteRoot = (String)i.next();
            if (path.startsWith(siteRoot)) {
                return siteRoot;
            }
        }
        return null;
    }

    /**
     * Adds an alias to the currently configured site.
     * 
     * @param alias the url of the alias server
     */
    public void addAliasToConfigSite(String alias) {

        CmsSiteMatcher siteMatcher = new CmsSiteMatcher(alias);
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
     * @param uri the vfs path
     * @param secureServer a secure server, can be null
     * @throws CmsConfigurationException if the site contains a servername, that is already assigned
     */
    public void addSite(String server, String uri, String secureServer) throws CmsConfigurationException {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }
        CmsSiteMatcher matcher = new CmsSiteMatcher(server);
        CmsSite site = new CmsSite(uri, matcher);
        addServer(matcher, site);
        if (CmsStringUtil.isNotEmpty(secureServer)) {
            matcher = new CmsSiteMatcher(secureServer);
            site.setSecureServer(matcher);
            addServer(matcher, site);
        }

        // Note that Digester first calls the addAliasToConfigSite method.
        // Therefore, the aliases are already 
        site.setAliases(m_aliases);
        Iterator i = m_aliases.iterator();
        while (i.hasNext()) {
            matcher = (CmsSiteMatcher)i.next();
            addServer(matcher, site);
        }
        m_aliases = new ArrayList();
        m_siteRoots.add(site.getSiteRoot());
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SITE_ROOT_ADDED_1, site.toString()));
        }
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
     * Returns an unmodifiable set of all configured site roots (Strings).<p>
     *  
     * @return an unmodifiable set of all configured site roots (Strings)
     */
    public Set getSiteRoots() {

        return m_siteRoots;
    }

    /**
     * Returns a map of configured sites.<p>
     * 
     * The map uses CmsSiteMatcher objects as key and CmsSite as value.<p>
     * 
     * @return a map of configured sites
     */
    public Map getSites() {

        return m_sites;
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
            CmsLog.INIT.info(Messages.get().key(
                Messages.INIT_NUM_SITE_ROOTS_CONFIGURED_1,
                new Integer((m_sites.size() + ((m_defaultUri != null) ? 1 : 0)))));
        }

        // check the presence of sites in VFS
        Iterator i = m_sites.values().iterator();
        while (i.hasNext()) {
            CmsSite site = (CmsSite)i.next();
            if (site != null) {
                try {
                    cms.readResource(site.getSiteRoot());
                } catch (Throwable t) {
                    if (CmsLog.INIT.isWarnEnabled()) {
                        CmsLog.INIT.warn(Messages.get().key(Messages.INIT_NO_ROOT_FOLDER_1, site));
                    }
                }
            }
        }

        // check the presence of the default site in VFS
        if ((m_defaultUri == null) || "".equals(m_defaultUri.trim())) {
            m_defaultSite = null;
        } else {
            m_defaultSite = new CmsSite(m_defaultUri, CmsSiteMatcher.DEFAULT_MATCHER);
            try {
                cms.readResource(m_defaultSite.getSiteRoot());
            } catch (Throwable t) {
                if (CmsLog.INIT.isWarnEnabled()) {
                    CmsLog.INIT.warn(Messages.get().key(Messages.INIT_NO_ROOT_FOLDER_DEFAULT_SITE_1, m_defaultSite));
                }
            }
        }
        if (m_defaultSite == null) {
            m_defaultSite = new CmsSite("/", CmsSiteMatcher.DEFAULT_MATCHER);
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            if (m_defaultSite != null) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_DEFAULT_SITE_ROOT_1, m_defaultSite));
            } else {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_DEFAULT_SITE_ROOT_0));
            }
        }
        m_workplaceSiteMatcher = new CmsSiteMatcher(m_workplaceServer);
        if (CmsLog.INIT.isInfoEnabled()) {
            if (m_workplaceSiteMatcher != null) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_WORKPLACE_SITE_1, m_workplaceSiteMatcher));
            } else {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_WORKPLACE_SITE_0));
            }
        }

        // set site lists to unmodifiable 
        m_sites = Collections.unmodifiableMap(m_sites);
        m_siteRoots = Collections.unmodifiableSet(m_siteRoots);

        // initialization is done, set the frozen flag to true 
        m_frozen = true;
    }

    /**
     * Returns true if the given site matcher matches a site.<p>
     * 
     * @param matcher the site matcher to match the site with
     * @return true if the matcher matches a site
     */
    public boolean isMatching(CmsSiteMatcher matcher) {

        return m_sites.get(matcher) != null;
    }

    /**
     * Returns if the given site matcher matches the current site.<p>
     * 
     * @param cms the cms object
     * @param matcher the site matcher to match the site with
     * @return true if the matcher matches the current site
     */
    public boolean isMatchingCurrentSite(CmsObject cms, CmsSiteMatcher matcher) {

        return m_sites.get(matcher) == getCurrentSite(cms);
    }

    /**
     * Returns <code>true</code> if the given request is against the configured OpenCms workplace.<p> 
     * 
     * @param req the request to match 
     * @return <code>true</code> if the given request is against the configured OpenCms workplace
     */
    public boolean isWorkplaceRequest(HttpServletRequest req) {

        if (req == null) {
            // this may be true inside a static export test case scenario
            return false;
        }
        CmsSiteMatcher matcher = new CmsSiteMatcher(req.getScheme(), req.getServerName(), req.getServerPort());
        return m_workplaceSiteMatcher.equals(matcher);
    }

    /**
     * Matches the given request against all configures sites and returns 
     * the matching site, or the default site if no sites matches.<p>
     * 
     * @param req the request to match 
     * @return the matching site, or the default site if no sites matches
     */
    public CmsSite matchRequest(HttpServletRequest req) {

        CmsSiteMatcher matcher = new CmsSiteMatcher(req.getScheme(), req.getServerName(), req.getServerPort());
        CmsSite site = matchSite(matcher);
        if (LOG.isDebugEnabled()) {
            String requestServer = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
            LOG.debug(Messages.get().key(Messages.LOG_MATCHING_REQUEST_TO_SITE_2, requestServer, site.toString()));
        }
        return site;
    }

    /**
     * Return the site that matches the given site matcher,
     * or the default site if no sites matches.<p>
     * 
     * @param matcher the site matcher to match the site with
     * @return the matching site, or the defaule site if no sites matches
     */
    public CmsSite matchSite(CmsSiteMatcher matcher) {

        CmsSite site = (CmsSite)m_sites.get(matcher);
        if (site == null) {
            // return the default site (might be null as well)
            site = m_defaultSite;
        }
        return site;
    }

    /**
     * Sets the default uri, this is only allowed during configuration.<p>
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
     * Adds a new Sitematcher object to the map of server names.
     * 
     * If this method  
     * a <code>RuntimeException</code> is thrown.<p>
     * 
     * @param server, the SiteMatcher of the server
     * @param site the site to add
     * @throws CmsConfigurationException, if the site contains a servername, that is already assigned
     */
    private void addServer(CmsSiteMatcher server, CmsSite site) throws CmsConfigurationException {

        if (m_sites.containsKey(server)) {
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_DUPLICATE_SERVER_NAME_1,
                server.getUrl()));
        }
        m_sites.put(server, site);
    }
}
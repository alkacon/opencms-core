/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/site/CmsSiteManager.java,v $
 * Date   : $Date: 2004/11/26 13:39:31 $
 * Version: $Revision: 1.31 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Manages all configured sites in OpenCms.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.31 $
 * @since 5.1
 */
public final class CmsSiteManager implements Cloneable {

    /** Error message if a configuration change is attempted after configuration is frozen. */
    public static final String C_MESSAGE_FROZEN = "Site configuration has been frozen and can not longer be changed!";

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

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Site configuration   : starting");
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
            if (workplaceMode && cms.isAdmin()) {
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
                        String title = cms.readPropertyObject(folder, I_CmsConstants.C_PROPERTY_TITLE, false)
                            .getValue();
                        if (title == null) {
                            title = folder;
                        }
                        result.add(new CmsSite(folder, res.getStructureId(), title, (CmsSiteMatcher)siteServers
                            .get(folder)));
                    }

                } catch (CmsException e) {
                    // user probably has no read access to the folder, ignore and continue iterating            
                }
            }
        } catch (Throwable t) {
            if (OpenCms.getLog(CmsSiteManager.class).isErrorEnabled()) {
                OpenCms.getLog(CmsSiteManager.class).error("Error reading site properties", t);
            }
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
     * Adds a new CmsSite to the list of configured sites, 
     * this is only allowed during configuration.<p>
     * 
     * If this method is called after the configuration is finished, 
     * a <code>RuntimeException</code> is thrown.<p>
     * 
     * @param server the Server
     * @param uri the vfs path
     */
    public void addSite(String server, String uri) {

        if (m_frozen) {
            throw new RuntimeException(C_MESSAGE_FROZEN);
        }
        CmsSiteMatcher matcher = new CmsSiteMatcher(server);
        CmsSite site = new CmsSite(uri, matcher);
        m_sites.put(site.getSiteMatcher(), site);
        m_siteRoots.add(site.getSiteRoot());
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Site root added      : " + site.toString());
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

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Site roots configured: " + (m_sites.size() + ((m_defaultUri != null) ? 1 : 0)));
        }

        // check the presence of sites in VFS
        Iterator i = m_sites.values().iterator();
        while (i.hasNext()) {
            CmsSite site = (CmsSite)i.next();
            if (site != null) {
                try {
                    cms.readResource(site.getSiteRoot());
                } catch (Throwable t) {
                    if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                        OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(
                            "Root folder for site " + site + " does not exist (ignoring this site entry)");
                    }
                }
            }
        }

        // check the presence of the default site in VFS
        if ((m_defaultUri == null) || "".equals(m_defaultUri.trim())) {
            m_defaultSite = null;
        } else {
            m_defaultSite = new CmsSite(m_defaultUri, CmsSiteMatcher.C_DEFAULT_MATCHER);
            try {
                cms.readResource(m_defaultSite.getSiteRoot());
            } catch (Throwable t) {
                if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(
                        "Root folder for default site "
                            + m_defaultSite
                            + " does not exist (setting default site root to '/')");
                }
            }
        }
        if (m_defaultSite == null) {
            m_defaultSite = new CmsSite("/", CmsSiteMatcher.C_DEFAULT_MATCHER);
        }
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Site root default    : " + (m_defaultSite != null ? "" + m_defaultSite : "(not configured)"));
        }
        m_workplaceSiteMatcher = new CmsSiteMatcher(m_workplaceServer);
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Site of workplace    : "
                    + (m_workplaceSiteMatcher != null ? "" + m_workplaceSiteMatcher : "(not configured)"));
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
     * Matches the given request against all configures sites and returns 
     * the matching site, or the default site if no sites matches.<p>
     * 
     * @param req the request to match 
     * @return the matching site, or the defaule site if no sites matches
     */
    public CmsSite matchRequest(HttpServletRequest req) {

        CmsSiteMatcher matcher = new CmsSiteMatcher(req.getScheme(), req.getServerName(), req.getServerPort());
        return matchSite(matcher);
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
            throw new RuntimeException(C_MESSAGE_FROZEN);
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
            throw new RuntimeException(C_MESSAGE_FROZEN);
        }
        m_workplaceServer = workplaceServer;
    }
}
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import com.alkacon.simapi.CmykJpegReader.StringUtil;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsSitesConfiguration;
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsCore;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPath;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Manages all configured sites in OpenCms.<p>
 *
 * To obtain the configured site manager instance, use {@link OpenCms#getSiteManager()}.<p>
 *
 * @since 7.0.2
 */
public final class CmsSiteManagerImpl implements I_CmsEventListener {

    /**
     * Holds data for the alternative site root mappings.
     */
    private static class AlternativeSiteData {

        /** Map from site roots as strings to the corresponding alternative site roots. */
        private Map<CmsPath, CmsSite> m_alternativeSites = new HashMap<>();

        /** Site roots for the alternative site data. */
        private Set<String> m_siteRoots = new HashSet<>();

        /**
         * Creates a new instance from the alternative site root mappings of the given site.
         *
         * @param normalSites the normal sites
         */
        public AlternativeSiteData(Collection<CmsSite> normalSites) {

            for (CmsSite site : normalSites) {
                if (site.getAlternativeSiteRootMapping().isPresent()) {
                    CmsSite extensionSite = site.createAlternativeSiteRootSite();
                    CmsPath key = new CmsPath(extensionSite.getSiteRoot());
                    m_alternativeSites.put(key, extensionSite);
                    m_siteRoots.add(key.asString());
                }

            }
        }

        /**
         * Gets the site for the given root path, or null if no site for that path is found.
         *
         * @param path a root path
         * @return the site for the root path, or null
         */
        public CmsSite getSiteForRootPath(String path) {

            for (Map.Entry<CmsPath, CmsSite> entry : m_alternativeSites.entrySet()) {
                CmsPath key = entry.getKey();
                if (key.isPrefixOfStr(path)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        /**
         * Gets the site for the given site root.
         *
         * @param path a site root
         * @return the site for the site root
         */
        public CmsSite getSiteForSiteRoot(String path) {

            CmsPath key = new CmsPath(path);
            CmsSite result = m_alternativeSites.get(key);
            return result;
        }

        /**
         * Gets the site roots for the alternative site root mappings.
         *
         * @return the site roots
         */
        public Set<String> getSiteRoots() {

            return Collections.unmodifiableSet(m_siteRoots);
        }

    }

    /** The default shared folder name. */
    public static final String DEFAULT_SHARED_FOLDER = "shared";

    /**
     * The VFS root path to the system shared folder, where shared content that belongs to modules,
     * and that should not be edited by normal editors can be stored.
     * The folder is searched in the gallery search when shared folders should be searched.
     */
    public static final String PATH_SYSTEM_SHARED_FOLDER = "/system/shared/";

    /** A placeholder for the title of the shared folder. */
    public static final String SHARED_FOLDER_TITLE = "%SHARED_FOLDER%";

    /** Path to config template. */
    public static final String WEB_SERVER_CONFIG_CONFIGTEMPLATE = "configtemplate";

    /**prefix for files. */
    public static final String WEB_SERVER_CONFIG_FILENAMEPREFIX = "filenameprefix";

    /**Path to write logs to. */
    public static final String WEB_SERVER_CONFIG_LOGGINGDIR = "loggingdir";

    /** Path to secure template. */
    public static final String WEB_SERVER_CONFIG_SECURETEMPLATE = "securetemplate";

    /** Path to target. */
    public static final String WEB_SERVER_CONFIG_TARGETPATH = "targetpath";

    /** Path of webserver script.*/
    public static final String WEB_SERVER_CONFIG_WEBSERVERSCRIPT = "webserverscript";

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSiteManagerImpl.class);

    /** The path to the "/sites/" folder. */
    private static final String SITES_FOLDER = "/sites/";

    /** The length of the "/sites/" folder plus 1. */
    private static final int SITES_FOLDER_POS = SITES_FOLDER.length() + 1;

    /** A list of additional site roots, that is site roots that are not below the "/sites/" folder. */
    private List<String> m_additionalSiteRoots;

    /** Data for the alternative site root rules. */
    private volatile AlternativeSiteData m_alternativeSiteData = new AlternativeSiteData(new ArrayList<>());

    /**Map with webserver scripting parameter. */
    private Map<String, String> m_apacheConfig;

    /**CmsObject.*/
    private CmsObject m_clone;

    /** The default site root. */
    private CmsSite m_defaultSite;

    /** The default URI. */
    private String m_defaultUri;

    /** Indicates if the configuration is finalized (frozen). */
    private boolean m_frozen;

    /**Is the publish listener already set? */
    private boolean m_isListenerSet;

    /**Old style secure server allowed? */
    private boolean m_oldStyleSecureServer;

    /**Site which are only available for offline project. */
    private List<CmsSite> m_onlyOfflineSites;

    /** The shared folder name. */
    private String m_sharedFolder;

    /** Contains all configured site matchers in a list for direct access. */
    private List<CmsSiteMatcher> m_siteMatchers;

    /** Maps site matchers to sites. */
    private Map<CmsSiteMatcher, CmsSite> m_siteMatcherSites;

    /** Maps site roots to sites. */
    private Map<String, CmsSite> m_siteRootSites;

    /**Map from CmsUUID to CmsSite.*/
    private Map<CmsUUID, CmsSite> m_siteUUIDs;

    /** The workplace site matchers. */
    private List<CmsSiteMatcher> m_workplaceMatchers;

    /** The workplace servers. */
    private Map<String, CmsSSLMode> m_workplaceServers;

    /**
     * Creates a new CmsSiteManager.<p>
     *
     */
    public CmsSiteManagerImpl() {

        m_siteMatcherSites = new HashMap<CmsSiteMatcher, CmsSite>();
        m_siteRootSites = new HashMap<String, CmsSite>();
        m_additionalSiteRoots = new ArrayList<String>();
        m_workplaceServers = new LinkedHashMap<String, CmsSSLMode>();
        m_workplaceMatchers = new ArrayList<CmsSiteMatcher>();
        m_oldStyleSecureServer = true;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_START_SITE_CONFIG_0));
        }
    }

    /**
     * Creates a site matcher for an alias read from the configuration.
     *
     * @param alias the alias
     * @param redirect redirection enabled (true/false)
     * @param offset time offset or empty
     *
     * @return the alias site matcher
     */
    public static CmsSiteMatcher createAliasSiteMatcher(String alias, String redirect, String offset) {

        long timeOffset = 0;
        try {
            timeOffset = Long.parseLong(offset);
        } catch (Throwable e) {
            // ignore
        }
        CmsSiteMatcher siteMatcher = new CmsSiteMatcher(alias, timeOffset);
        CmsSiteMatcher.RedirectMode redirectMode = CmsSiteMatcher.RedirectMode.parse(redirect);
        siteMatcher.setRedirectMode(redirectMode);
        return siteMatcher;
    }

    /**
     * Parses the given string as an URI and returns its host component.
     *
     * @param uriStr the URI string
     * @return the host component, or null if the URI can't be parsed
     */
    private static String getHost(String uriStr) {

        try {
            URI uri = new URI(uriStr);
            return uri.getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * Adds a site.<p>
     *
     * @param cms the CMS object
     * @param site the site to add
     *
     * @throws CmsException if something goes wrong
     */
    public void addSite(CmsObject cms, CmsSite site) throws CmsException {

        // check permissions
        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            // simple unit tests will have runlevel 1 and no CmsObject
            OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);
        }

        validateSiteRoot(site.getSiteRoot());

        // un-freeze
        m_frozen = false;

        String secureUrl = null;
        if (site.hasSecureServer()) {
            secureUrl = site.getSecureUrl();
        }

        // add the site
        addSite(
            site.getUrl(),
            site.getSiteRoot(),
            site.getTitle(),
            Float.toString(site.getPosition()),
            site.getErrorPage(),
            Boolean.toString(site.isWebserver()),
            site.getSSLMode().getXMLValue(),
            secureUrl,
            Boolean.toString(site.isExclusiveUrl()),
            Boolean.toString(site.isExclusiveError()),
            Boolean.toString(site.usesPermanentRedirects()),
            Boolean.toString(site.isSubsiteSelectionEnabled()),
            site.getParameters(),
            site.getAliases(),
            site.getAlternativeSiteRootMapping());

        // re-initialize, will freeze the state when finished
        initialize(cms);
        OpenCms.writeConfiguration(CmsSitesConfiguration.class);
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
     * @param title the display title for this site
     * @param position the display order for this site
     * @param errorPage the URI to use as error page for this site
     * @param sslMode the SSLMode of the site
     * @param webserver indicates whether to write the web server configuration for this site or not
     * @param secureServer a secure server, can be <code>null</code>
     * @param exclusive if set to <code>true</code>, secure resources will only be available using the configured secure url
     * @param error if exclusive, and set to <code>true</code> will generate a 404 error,
     *                             if set to <code>false</code> will redirect to secure URL
     * @param usePermanentRedirects if set to "true", permanent redirects should be used when redirecting to the secure URL
     * @param subsiteSelection true if subsite selection should be enabled
     * @param params the site parameters
     * @param aliases the aliases for the site
     * @param alternativeSiteRootMapping an optional alternative site root mapping
     *
     * @throws CmsConfigurationException if the site contains a server name, that is already assigned
     */
    public void addSite(
        String server,
        String uri,
        String title,
        String position,
        String errorPage,
        String webserver,
        String sslMode,
        String secureServer,
        String exclusive,
        String error,
        String usePermanentRedirects,
        String subsiteSelection,
        SortedMap<String, String> params,
        List<CmsSiteMatcher> aliases,
        java.util.Optional<CmsAlternativeSiteRootMapping> alternativeSiteRootMapping)
    throws CmsConfigurationException {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }

        if (getSiteRoots().contains(uri)) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_SITE_ALREADY_CONFIGURED_1, uri));
        }

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(server)) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_EMPTY_SERVER_URL_0));
        }

        validateSiteRoot(uri);

        // create a new site object
        CmsSiteMatcher matcher = new CmsSiteMatcher(server);
        CmsSite site = new CmsSite(uri, matcher);
        // set the title
        site.setTitle(title);
        // set the position
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(position)) {
            float pos = Float.MAX_VALUE;
            try {
                pos = Float.parseFloat(position);
            } catch (Throwable e) {
                // m_position will have Float.MAX_VALUE, so this site will appear last
            }
            site.setPosition(pos);
        }
        // set the error page
        site.setErrorPage(errorPage);
        site.setWebserver(Boolean.valueOf(webserver).booleanValue());
        site.setSSLMode(CmsSSLMode.getModeFromXML(sslMode));
        if (CmsStringUtil.isNotEmpty(secureServer)) {
            matcher = new CmsSiteMatcher(secureServer);
            site.setSecureServer(matcher);
            site.setExclusiveUrl(Boolean.valueOf(exclusive).booleanValue());
            site.setExclusiveError(Boolean.valueOf(error).booleanValue());
            site.setUsePermanentRedirects(Boolean.valueOf(usePermanentRedirects).booleanValue());
        }
        site.setSubsiteSelectionEnabled(Boolean.parseBoolean(subsiteSelection));

        // note that Digester first calls the addAliasToConfigSite method.
        // therefore, the aliases are already set
        site.setAliases(aliases);

        boolean valid = true;
        List<CmsSiteMatcher> toAdd = new ArrayList<CmsSiteMatcher>();
        for (CmsSiteMatcher matcherToAdd : site.getAllMatchers()) {
            valid = valid & isServerValid(matcherToAdd) & !toAdd.contains(matcherToAdd);
            toAdd.add(matcherToAdd);
        }

        if (!valid) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_DUPLICATE_SERVER_NAME_1, matcher.getUrl()));
        }

        for (CmsSiteMatcher matcherToAdd : site.getAllMatchers()) {
            addServer(matcherToAdd, site);
        }

        site.setParameters(params);
        site.setAlternativeSiteRootMapping(alternativeSiteRootMapping);
        m_siteRootSites = new HashMap<String, CmsSite>(m_siteRootSites);
        m_siteRootSites.put(site.getSiteRoot(), site);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SITE_ROOT_ADDED_1, site.toString()));
        }
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
     * @param title the display title for this site
     * @param position the display order for this site
     * @param errorPage the URI to use as error page for this site
     * @param sslMode the SSLMode of the site
     * @param webserver indicates whether to write the web server configuration for this site or not
     * @param secureServer a secure server, can be <code>null</code>
     * @param exclusive if set to <code>true</code>, secure resources will only be available using the configured secure url
     * @param error if exclusive, and set to <code>true</code> will generate a 404 error,
     *                             if set to <code>false</code> will redirect to secure URL
     * @param usePermanentRedirects if set to "true", permanent redirects should be used when redirecting to the secure URL
     * @param subsiteSelection true if subsite selection should be enabled for this site
     * @param params the site parameters
     * @param aliases the aliases
     * @param alternativeSiteRoot an optional alternative site root mapping
     *
     * @throws CmsConfigurationException in case the site was not configured correctly
     *
     */
    public void addSiteInternally(
        String server,
        String uri,
        String title,
        String position,
        String errorPage,
        String webserver,
        String sslMode,
        String secureServer,
        String exclusive,
        String error,
        String usePermanentRedirects,
        String subsiteSelection,
        SortedMap<String, String> params,
        List<CmsSiteMatcher> aliases,
        java.util.Optional<CmsAlternativeSiteRootMapping> alternativeSiteRoot)
    throws CmsConfigurationException {

        try {
            addSite(
                server,
                uri,
                title,
                position,
                errorPage,
                webserver,
                sslMode,
                secureServer,
                exclusive,
                error,
                usePermanentRedirects,
                subsiteSelection,
                params,
                aliases,
                alternativeSiteRoot);

        } catch (CmsConfigurationException e) {
            LOG.error("Error reading definitions. Trying to read without aliases.", e);

            //If this fails, the webserver was defined before ->throw exception

            addSite(
                server,
                uri,
                title,
                position,
                errorPage,
                webserver,
                sslMode,
                secureServer,
                exclusive,
                error,
                usePermanentRedirects,
                subsiteSelection,
                params,
                new ArrayList<>(),
                alternativeSiteRoot); //If the aliases are making problems, just remove thems

        }
    }

    /**
     * Adds a workplace server, this is only allowed during configuration.<p>
     *
     * @param workplaceServer the workplace server
     * @param sslmode CmsSSLMode of workplace server
     */
    public void addWorkplaceServer(String workplaceServer, String sslmode) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }
        if (!m_workplaceServers.containsKey(workplaceServer)) {
            m_workplaceServers.put(workplaceServer, CmsSSLMode.getModeFromXML(sslmode));
        }
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        try {
            CmsProject project = getOfflineProject();
            m_clone.getRequestContext().setCurrentProject(project);
            List<CmsPublishedResource> res = null;

            List<CmsPublishedResource> foundSites = new ArrayList<CmsPublishedResource>();

            res = m_clone.readPublishedResources(
                new CmsUUID((String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID)));

            if (res != null) {
                for (CmsPublishedResource r : res) {
                    if (!foundSites.contains(r)) {
                        if (m_siteUUIDs.containsKey(r.getStructureId())) {
                            foundSites.add(r);
                        }
                    }
                }
            }
            project = m_clone.readProject(CmsProject.ONLINE_PROJECT_ID);
            m_clone.getRequestContext().setCurrentProject(project);
            Map<CmsSite, CmsSite> updateMap = new HashMap<CmsSite, CmsSite>();

            for (CmsPublishedResource r : foundSites) {
                if (m_clone.existsResource(r.getStructureId())) {
                    //Resource was not deleted
                    CmsResource siteRoot = m_clone.readResource(r.getStructureId());
                    if (!m_siteRootSites.containsKey(CmsFileUtil.removeTrailingSeparator(siteRoot.getRootPath()))
                        | m_onlyOfflineSites.contains(m_siteUUIDs.get(r.getStructureId()))) {
                        //Site was moved or site root was renamed.. or site was published the first time
                        CmsSite oldSite = m_siteUUIDs.get(siteRoot.getStructureId());
                        CmsSite newSite = oldSite.clone();
                        newSite.setSiteRoot(siteRoot.getRootPath());
                        updateMap.put(oldSite, newSite);
                    }
                }
            }

            for (CmsSite site : updateMap.keySet()) {
                updateSite(m_clone, site, updateMap.get(site));
            }
        } catch (CmsException e) {
            LOG.error("Unable to handle publish event", e);
        }

    }

    /**
     * Returns all wrong configured sites.<p>
     *
     * @param cms CmsObject
     * @param workplaceMode workplace mode
     * @return List of CmsSite
     */
    public List<CmsSite> getAvailableCorruptedSites(CmsObject cms, boolean workplaceMode) {

        List<CmsSite> res = new ArrayList<CmsSite>();
        List<CmsSite> visSites = getAvailableSites(cms, workplaceMode);
        Map<CmsSiteMatcher, CmsSite> allsites = getSites();
        for (CmsSiteMatcher matcher : allsites.keySet()) {
            CmsSite site = allsites.get(matcher);
            if (!visSites.contains(site) & !res.contains(site)) {
                res.add(site);
            }
        }
        return res;
    }

    /**
     * Returns a list of all sites available (visible) for the current user.<p>
     *
     * @param cms the current OpenCms user context
     * @param workplaceMode if true, the root and current site is included for the admin user
     *                      and the view permission is required to see the site root
     *
     * @return a list of all sites available for the current user
     */
    public List<CmsSite> getAvailableSites(CmsObject cms, boolean workplaceMode) {

        return getAvailableSites(cms, workplaceMode, cms.getRequestContext().getOuFqn());
    }

    /**
     * Returns a list of all {@link CmsSite} instances that are compatible to the given organizational unit.<p>
     *
     * @param cms the current OpenCms user context
     * @param workplaceMode if true, the root and current site is included for the admin user
     *                      and the view permission is required to see the site root
     * @param showShared if the shared folder should be shown
     * @param ouFqn the organizational unit
     *
     * @return a list of all site available for the current user
     */
    public List<CmsSite> getAvailableSites(CmsObject cms, boolean workplaceMode, boolean showShared, String ouFqn) {

        return getAvailableSites(cms, workplaceMode, showShared, ouFqn, null);
    }

    /**
     * Returns a list of all {@link CmsSite} instances that are compatible to the given organizational unit.<p>
     *
     * @param cms the current OpenCms user context
     * @param workplaceMode if true, the root and current site is included for the admin user
     *                      and the view permission is required to see the site root
     * @param showShared if the shared folder should be shown
     * @param ouFqn the organizational unit
     * @param filterMode The CmsSLLMode to filter, null if no filter
     *
     * @return a list of all site available for the current user
     */
    public List<CmsSite> getAvailableSites(
        CmsObject cms,
        boolean workplaceMode,
        boolean showShared,
        String ouFqn,
        CmsSSLMode filterMode) {

        List<String> siteroots = new ArrayList<String>(m_siteMatcherSites.size() + 1);
        Map<String, CmsSiteMatcher> siteServers = new HashMap<String, CmsSiteMatcher>(m_siteMatcherSites.size() + 1);
        List<CmsSite> result = new ArrayList<CmsSite>(m_siteMatcherSites.size() + 1);

        for (CmsSite mainSite : m_siteMatcherSites.values()) {
            List<CmsSite> sitesToProcess = new ArrayList<>();
            sitesToProcess.add(mainSite);
            CmsSite extensionFolderSite = mainSite.createAlternativeSiteRootSite();
            if (extensionFolderSite != null) {
                sitesToProcess.add(extensionFolderSite);
            }
            for (CmsSite site : sitesToProcess) {
                String folder = CmsFileUtil.addTrailingSeparator(site.getSiteRoot());
                if (!siteroots.contains(folder)) {
                    siteroots.add(folder);
                    siteServers.put(folder, site.getSiteMatcher());
                }
            }
        }
        // add default site
        if (workplaceMode && (m_defaultSite != null)) {
            String folder = CmsFileUtil.addTrailingSeparator(m_defaultSite.getSiteRoot());
            if (!siteroots.contains(folder)) {
                siteroots.add(folder);
            }
        }

        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            // for all operations here we need no context
            cms.getRequestContext().setSiteRoot("/");
            if (workplaceMode && OpenCms.getRoleManager().hasRole(cms, CmsRole.VFS_MANAGER)) {
                if (!siteroots.contains("/")) {
                    // add the root site if the user is in the workplace and has the required role
                    siteroots.add("/");
                }
                if (!siteroots.contains(CmsFileUtil.addTrailingSeparator(storedSiteRoot))) {
                    siteroots.add(CmsFileUtil.addTrailingSeparator(storedSiteRoot));
                }
            }
            // add the shared site
            String shared = OpenCms.getSiteManager().getSharedFolder();
            if (showShared && (shared != null) && !siteroots.contains(shared)) {
                siteroots.add(shared);
            }
            // all sites are compatible for root admins in the root OU, skip unnecessary tests
            boolean allCompatible = OpenCms.getRoleManager().hasRole(cms, CmsRole.ROOT_ADMIN)
                && (ouFqn.isEmpty() || ouFqn.equals(CmsOrganizationalUnit.SEPARATOR));
            List<CmsResource> resources = Collections.emptyList();
            if (!allCompatible) {
                try {
                    resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ouFqn);
                } catch (CmsException e) {
                    return Collections.emptyList();
                }
            }
            Collections.sort(siteroots); // sort by resource name
            Iterator<String> roots = siteroots.iterator();
            while (roots.hasNext()) {
                String folder = roots.next();
                boolean compatible = allCompatible;
                if (!compatible) {
                    Iterator<CmsResource> itResources = resources.iterator();
                    while (itResources.hasNext()) {
                        CmsResource resource = itResources.next();
                        if (resource.getRootPath().startsWith(folder) || folder.startsWith(resource.getRootPath())) {
                            compatible = true;
                            break;
                        }
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

                            // get the title and the position from the system configuration first
                            CmsSite configuredSite = getSiteForSiteRoot(CmsFileUtil.removeTrailingSeparator(folder));
                            // CmsSite configuredSite = m_siteRootSites.get(CmsFileUtil.removeTrailingSeparator(folder));

                            // get the title
                            String title = null;
                            if ((configuredSite != null)
                                && CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuredSite.getTitle())) {
                                title = configuredSite.getTitle();
                            }
                            if (title == null) {
                                title = getSiteTitle(cms, res);
                            }

                            // get the position
                            String position = null;
                            if ((configuredSite != null) && (configuredSite.getPosition() != Float.MAX_VALUE)) {
                                position = Float.toString(configuredSite.getPosition());
                            }
                            if (position == null) {
                                // not found, use the 'NavPos' property
                                position = cms.readPropertyObject(
                                    res,
                                    CmsPropertyDefinition.PROPERTY_NAVPOS,
                                    false).getValue();
                            }
                            if (configuredSite != null) {
                                float pos = Float.MAX_VALUE;
                                try {
                                    pos = Float.parseFloat(position);
                                } catch (Throwable e) {
                                    // m_position will have Float.MAX_VALUE, so this site will appear last
                                }
                                CmsSite clone = configuredSite.clone();
                                clone.setPosition(pos);
                                clone.setTitle(title);
                                if (filterMode == null) {
                                    result.add(clone);
                                } else {
                                    if (filterMode.equals(clone.getSSLMode())) {
                                        result.add(clone);
                                    }
                                }
                            } else {
                                // add the site to the result

                                result.add(
                                    new CmsSite(
                                        folder,
                                        res.getStructureId(),
                                        title,
                                        siteServers.get(folder),
                                        position));
                            }
                        }
                    } catch (CmsException e) {
                        // user probably has no read access to the folder, ignore and continue iterating
                    }
                }
            }

            // sort and ensure that the shared folder is the last element in the list
            Collections.sort(result, new Comparator<CmsSite>() {

                public int compare(CmsSite o1, CmsSite o2) {

                    if (isSharedFolder(o1.getSiteRoot())) {
                        return +1;
                    }
                    if (isSharedFolder(o2.getSiteRoot())) {
                        return -1;
                    }
                    return o1.compareTo(o2);
                }
            });
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_READ_SITE_PROP_FAILED_0), t);
        } finally {
            // restore the user's current context
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
        }
        return result;

    }

    /**
     * Returns a list of all sites available (visible) for the current user.<p>
     *
     * @param cms the current OpenCms user context
     * @param workplaceMode if true, the root and current site is included for the admin user
     *                      and the view permission is required to see the site root
     * @param filterMode The CmsSLLMode to filter, null if no filter
     *
     * @return a list of all sites available for the current user
     */
    public List<CmsSite> getAvailableSites(CmsObject cms, boolean workplaceMode, CmsSSLMode filterMode) {

        return getAvailableSites(cms, workplaceMode, workplaceMode, cms.getRequestContext().getOuFqn(), filterMode);
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
    public List<CmsSite> getAvailableSites(CmsObject cms, boolean workplaceMode, String ouFqn) {

        return getAvailableSites(cms, workplaceMode, workplaceMode, ouFqn);
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
     * Returns the shared folder path.<p>
     *
     * @return the shared folder path
     */
    public String getSharedFolder() {

        return m_sharedFolder;
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
     * Gets the site which is mapped to the default uri, or the 'absent' value of no such site exists.<p>
     *
     * @return the optional site mapped to the default uri
     */
    public Optional<CmsSite> getSiteForDefaultUri() {

        String defaultUri = getDefaultUri();
        CmsSite candidate = m_siteRootSites.get(CmsFileUtil.removeTrailingSeparator(defaultUri));
        return Optional.fromNullable(candidate);
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

        if ((rootPath.length() > 0) && !rootPath.endsWith("/")) {
            rootPath = rootPath + "/";
        }
        // most sites will be below the "/sites/" folder,
        CmsSite result = lookupSitesFolder(rootPath);
        if (result != null) {
            return result;
        }
        // look through all folders that are not below "/sites/"
        String siteRoot = lookupAdditionalSite(rootPath);
        if (siteRoot != null) {
            return getSiteForSiteRoot(siteRoot);
        }
        return m_alternativeSiteData.getSiteForRootPath(rootPath);

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

        if (siteRoot == null) {
            return null;
        }
        CmsSite result = m_siteRootSites.get(siteRoot);
        if (result != null) {
            return result;
        } else {
            return m_alternativeSiteData.getSiteForSiteRoot(siteRoot);
        }
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

        // add a trailing slash, because the path may be the path of a site root itself
        if (!rootPath.endsWith("/")) {
            rootPath = rootPath + "/";
        }
        // most sites will be below the "/sites/" folder,
        CmsSite site = lookupSitesFolder(rootPath);
        if (site != null) {
            return site.getSiteRoot();
        }
        // look through all folders that are not below "/sites/"
        String result = lookupAdditionalSite(rootPath);
        if (result != null) {
            return result;
        }
        CmsSite extSite = m_alternativeSiteData.getSiteForRootPath(rootPath);
        if (extSite != null) {
            result = extSite.getSiteRoot();
        }
        return result;

    }

    /**
     * Returns an unmodifiable set of all configured site roots (Strings).<p>
     *
     * @return an unmodifiable set of all configured site roots (Strings)
     */
    public Set<String> getSiteRoots() {

        return Sets.union(m_siteRootSites.keySet(), m_alternativeSiteData.getSiteRoots());

    }

    /**
     * Returns the map of configured sites, using
     * {@link CmsSiteMatcher} objects as keys and {@link CmsSite} objects as values.<p>
     *
     * @return the map of configured sites, using {@link CmsSiteMatcher}
     *      objects as keys and {@link CmsSite} objects as values
     */
    public Map<CmsSiteMatcher, CmsSite> getSites() {

        return m_siteMatcherSites;
    }

    /**
     * Returns the site title.<p>
     *
     * @param cms the cms context
     * @param resource the site root resource
     *
     * @return the title
     *
     * @throws CmsException in case reading the title property fails
     */
    public String getSiteTitle(CmsObject cms, CmsResource resource) throws CmsException {

        String title = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        if (title == null) {
            title = resource.getRootPath();
        }
        if (resource.getRootPath().equals(getSharedFolder())) {
            title = SHARED_FOLDER_TITLE;
        }
        return title;
    }

    /**
     * Gets the SSLMode for given workplace server.<p>
     *
     * @param server to obtain ssl mode for
     * @return CmsSSLMode
     */
    public CmsSSLMode getSSLModeForWorkplaceServer(String server) {

        if (server == null) {
            return CmsSSLMode.NO;
        }
        if (!m_workplaceServers.containsKey(server)) {
            return CmsSSLMode.NO;
        }

        return m_workplaceServers.get(server);
    }

    /**
     * Get web server scripting configurations.<p>
     *
     * @return Map with configuration data
     */
    public Map<String, String> getWebServerConfig() {

        return m_apacheConfig;
    }

    /**
     * Returns the workplace server.<p>
     *
     * @return the workplace server
     */
    public String getWorkplaceServer() {

        return m_workplaceServers.keySet().isEmpty() ? null : m_workplaceServers.keySet().iterator().next();
    }

    /**
     * Gets the first configured workplace server that matches the host from the current CmsRequestContext, or
     * the first configured workplace server if there is no match.
     *
     * <p>If there are no workplace configured at all, null is returned.
     *
     * @param cms the CmsObject used to check the host
     * @return the workplace server
     */
    public String getWorkplaceServer(CmsObject cms) {

        if (m_workplaceServers.keySet().isEmpty()) {
            return null;
        }
        CmsSiteMatcher requestMatcher = cms.getRequestContext().getRequestMatcher();
        if (requestMatcher != null) {
            String reqHost = getHost(requestMatcher.toString());
            if (reqHost != null) {
                for (String wpServer : m_workplaceServers.keySet()) {
                    String wpHost = getHost(wpServer);
                    if (reqHost.equals(wpHost)) {
                        return wpServer;
                    }
                }
            }
        }
        return m_workplaceServers.keySet().iterator().next();
    }

    /**
     * Returns the configured worklace servers.<p>
     *
     * @return the workplace servers
     */
    public List<String> getWorkplaceServers() {

        return Collections.unmodifiableList(new ArrayList<String>(m_workplaceServers.keySet()));
    }

    /**
     * Returns the configured worklace servers.<p>
     *
     * @param filterMode CmsSSLMode to filter results for.
     * @return the workplace servers
     */
    public List<String> getWorkplaceServers(CmsSSLMode filterMode) {

        if (filterMode == null) {
            return getWorkplaceServers();
        }
        List<String> ret = new ArrayList<String>();
        for (String server : m_workplaceServers.keySet()) {
            if (m_workplaceServers.get(server).equals(filterMode)) {
                ret.add(server);
            }
        }
        return ret;
    }

    /**
     * Returns the configured worklace servers.<p>
     *
     * @return the workplace servers
     */
    public Map<String, CmsSSLMode> getWorkplaceServersMap() {

        return Collections.unmodifiableMap(m_workplaceServers);
    }

    /**
     * Returns the site matcher that matches the workplace site.<p>
     *
     * @return the site matcher that matches the workplace site
     */
    public CmsSiteMatcher getWorkplaceSiteMatcher() {

        return m_workplaceMatchers.isEmpty() ? null : m_workplaceMatchers.get(0);
    }

    /**
     * Initializes the site manager with the OpenCms system configuration.<p>
     *
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     */
    public void initialize(CmsObject cms) {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_NUM_SITE_ROOTS_CONFIGURED_1,
                    Integer.valueOf((m_siteMatcherSites.size() + ((m_defaultUri != null) ? 1 : 0)))));
        }

        try {

            m_clone = OpenCms.initCmsObject(cms);
            m_clone.getRequestContext().setSiteRoot("");
            m_clone.getRequestContext().setCurrentProject(m_clone.readProject(CmsProject.ONLINE_PROJECT_NAME));

            CmsObject cms_offline = OpenCms.initCmsObject(m_clone);
            CmsProject tempProject = null;
            try {
                tempProject = cms_offline.createProject(
                    "tempProjectSites",
                    "",
                    "/Users",
                    "/Users",
                    CmsProject.PROJECT_TYPE_TEMPORARY);
                cms_offline.getRequestContext().setCurrentProject(tempProject);

            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }

            m_siteUUIDs = new HashMap<CmsUUID, CmsSite>();
            // check the presence of sites in VFS

            m_onlyOfflineSites = new ArrayList<CmsSite>();

            for (CmsSite site : m_siteMatcherSites.values()) {
                checkUUIDOfSiteRoot(site, m_clone, tempProject != null ? cms_offline : null);
                try {
                    CmsResource siteRes = m_clone.readResource(site.getSiteRoot());
                    site.setSiteRootUUID(siteRes.getStructureId());

                    m_siteUUIDs.put(siteRes.getStructureId(), site);
                    // during server startup the digester can not access properties, so set the title afterwards
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(site.getTitle())) {
                        String title = m_clone.readPropertyObject(
                            siteRes,
                            CmsPropertyDefinition.PROPERTY_TITLE,
                            false).getValue();
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
                            site.setTitle(title);
                        }
                    }
                } catch (Throwable t) {
                    if (CmsLog.INIT.isWarnEnabled()) {
                        CmsLog.INIT.warn(Messages.get().getBundle().key(Messages.INIT_NO_ROOT_FOLDER_1, site));
                    }
                }
            }
            if (tempProject != null) {
                cms_offline.deleteProject(tempProject.getUuid());
            }

            // check the presence of the default site in VFS
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_defaultUri)) {
                m_defaultSite = null;
            } else {
                m_defaultSite = new CmsSite(m_defaultUri, CmsSiteMatcher.DEFAULT_MATCHER);
                try {
                    m_clone.readResource(m_defaultSite.getSiteRoot());
                } catch (Throwable t) {
                    if (CmsLog.INIT.isWarnEnabled()) {
                        CmsLog.INIT.warn(
                            Messages.get().getBundle().key(Messages.INIT_NO_ROOT_FOLDER_DEFAULT_SITE_1, m_defaultSite));
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
            initWorkplaceMatchers();

            // set site lists to unmodifiable
            setSiteMatcherSites(m_siteMatcherSites);

            // store additional site roots to optimize lookups later
            for (String root : m_siteRootSites.keySet()) {
                if (!root.startsWith(SITES_FOLDER) || (root.split("/").length >= 4)) {
                    m_additionalSiteRoots.add(root);
                }
            }

            initExtensionSites();

            if (m_sharedFolder == null) {
                m_sharedFolder = DEFAULT_SHARED_FOLDER;
            }

            // initialization is done, set the frozen flag to true
            m_frozen = true;
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        if (!m_isListenerSet) {
            OpenCms.addCmsEventListener(this, new int[] {I_CmsEventListener.EVENT_PUBLISH_PROJECT});
            m_isListenerSet = true;
        }
    }

    /**
     * Checks if web server scripting is enabled.<p>
     *
     * @return true if web server scripting is set to available
     */
    public boolean isConfigurableWebServer() {

        return m_apacheConfig != null;
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
            result = isWorkplaceRequest(matcher);
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
     * Checks if old style secure server is allowed.<p>
     *
     * @return boolean
     */
    public boolean isOldStyleSecureServerAllowed() {

        return m_oldStyleSecureServer;
    }

    /**
     * Indicates if given site is only available for offline repository.<p>
     *
     * @param site to be looked up
     * @return true if only offline exists, false otherwise
     */
    public boolean isOnlyOfflineSite(CmsSite site) {

        return m_onlyOfflineSites.contains(site);
    }

    /**
     * Checks if the given path is that of a shared folder.<p>
     *
     * @param name a path prefix
     *
     * @return true if the given prefix represents a shared folder
     */
    public boolean isSharedFolder(String name) {

        return (m_sharedFolder != null) && m_sharedFolder.equals(CmsStringUtil.joinPaths("/", name, "/"));
    }

    /**
     * Checks whether a given root path is a site root.<p>
     *
     * @param rootPath a root path
     *
     * @return true if the given path is the path of a site root
     */
    public boolean isSiteRoot(String rootPath) {

        String siteRoot = getSiteRoot(rootPath);
        rootPath = CmsStringUtil.joinPaths(rootPath, "/");
        return rootPath.equals(siteRoot);

    }

    /**
     * Checks if a given site is under another site.<p>
     *
     * @param site CmsSite to check
     * @return true if given site is invalid
     */
    public boolean isSiteUnderSite(CmsSite site) {

        return isSiteUnderSite(site.getSiteRoot());
    }

    /**
     * Checks if a given site is under another site.<p>
     *
     * @param siteRootPath site root path to check
     * @return true if given site is invalid
     */
    public boolean isSiteUnderSite(String siteRootPath) {

        for (String siteRoot : getSiteRoots()) {
            if ((siteRootPath.length() > siteRoot.length())
                & siteRootPath.startsWith(CmsFileUtil.addTrailingSeparator(siteRoot))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the given site matcher matches the configured OpenCms workplace.<p>
     *
     * @param matcher the site matcher to match the site with
     *
     * @return <code>true</code> if the given site matcher matches the configured OpenCms workplace
     */
    public boolean isWorkplaceRequest(CmsSiteMatcher matcher) {

        return m_workplaceMatchers.contains(matcher);
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
                session.setAttribute(
                    CmsContextInfo.ATTRIBUTE_REQUEST_TIME,
                    Long.valueOf(System.currentTimeMillis() + matcher.getTimeOffset()));
            }
        }
        CmsSite site = matchSite(matcher);
        if (site.matchAlternativeSiteRoot(OpenCmsCore.getPathInfo(req))) {
            CmsSite alternativeSite = site.createAlternativeSiteRootSite();
            if (alternativeSite != null) {
                LOG.debug(
                    req.getRequestURL().toString()
                        + ": "
                        + "Matched extension folder rule, changing site root from "
                        + site.getSiteRoot()
                        + " to "
                        + alternativeSite.getSiteRoot());
                site = alternativeSite;
            }
        }

        if (LOG.isDebugEnabled()) {
            String requestServer = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
            LOG.debug(
                Messages.get().getBundle().key(
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
     * Does NOT match auto-generated sites from alternative site root mappings, since the site matcher does not contain path information.
     *
     * @param matcher the site matcher to match the site with
     * @return the matching site, or the default site if no sites matches
     */
    public CmsSite matchSite(CmsSiteMatcher matcher) {

        CmsSite site = m_siteMatcherSites.get(matcher);
        if (site == null) {
            // return the default site (might be null as well)
            site = m_defaultSite;
        }
        return site;
    }

    /**
     * Removes a site from the list of configured sites.<p>
     *
     * @param cms the cms object
     * @param site the site to remove
     *
     * @throws CmsException if something goes wrong
     */
    public void removeSite(CmsObject cms, CmsSite site) throws CmsException {

        // check permissions
        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            // simple unit tests will have runlevel 1 and no CmsObject
            OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);
        }

        // un-freeze
        m_frozen = false;

        // create a new map containing all existing sites without the one to remove
        Map<CmsSiteMatcher, CmsSite> siteMatcherSites = new HashMap<CmsSiteMatcher, CmsSite>();
        List<CmsSiteMatcher> matchersForSite = site.getAllMatchers();
        for (Map.Entry<CmsSiteMatcher, CmsSite> entry : m_siteMatcherSites.entrySet()) {
            if (!(matchersForSite.contains(entry.getKey()))) {
                // entry not the site itself nor an alias of the site nor the secure URL of the site, so add it
                siteMatcherSites.put(entry.getKey(), entry.getValue());
            }
        }
        setSiteMatcherSites(siteMatcherSites);

        // remove the site from the map holding the site roots as keys and the sites as values
        Map<String, CmsSite> siteRootSites = new HashMap<String, CmsSite>(m_siteRootSites);
        siteRootSites.remove(site.getSiteRoot());
        m_siteRootSites = Collections.unmodifiableMap(siteRootSites);

        // re-initialize, will freeze the state when finished
        initialize(cms);
        OpenCms.writeConfiguration(CmsSitesConfiguration.class);
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
     * Sets the old style secure server boolean.<p>
     *
     * @param value value
     */
    public void setOldStyleSecureServerAllowed(String value) {

        m_oldStyleSecureServer = Boolean.parseBoolean(StringUtil.toLowerCase(value));
    }

    /**
     * Sets the shared folder path.<p>
     *
     * @param sharedFolder the shared folder path
     */
    public void setSharedFolder(String sharedFolder) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_FROZEN_0));
        }
        m_sharedFolder = CmsStringUtil.joinPaths("/", sharedFolder, "/");
    }

    /**
     * Set webserver script configuration.<p>
     *
     *
     * @param webserverscript path
     * @param targetpath path
     * @param configtemplate path
     * @param securetemplate path
     * @param filenameprefix to add to files
     * @param loggingdir path
     */
    public void setWebServerScripting(
        String webserverscript,
        String targetpath,
        String configtemplate,
        String securetemplate,
        String filenameprefix,
        String loggingdir) {

        m_apacheConfig = new HashMap<String, String>();
        m_apacheConfig.put(WEB_SERVER_CONFIG_WEBSERVERSCRIPT, webserverscript);
        m_apacheConfig.put(WEB_SERVER_CONFIG_TARGETPATH, targetpath);
        m_apacheConfig.put(WEB_SERVER_CONFIG_CONFIGTEMPLATE, configtemplate);
        m_apacheConfig.put(WEB_SERVER_CONFIG_SECURETEMPLATE, securetemplate);
        m_apacheConfig.put(WEB_SERVER_CONFIG_FILENAMEPREFIX, filenameprefix);
        m_apacheConfig.put(WEB_SERVER_CONFIG_LOGGINGDIR, loggingdir);
    }

    /**
     * Returns true if the path starts with the shared folder path.<p>
     *
     * @param path the path to check
     *
     * @return true if the path starts with the shared folder path
     */
    public boolean startsWithShared(String path) {

        return (m_sharedFolder != null) && CmsFileUtil.addTrailingSeparator(path).startsWith(m_sharedFolder);
    }

    /**
     * Method for backward compability reasons. Not sure if really needed //TODO check!
     * CmsSSLMode are set to No as default.<p>
     *
     * @param cms the cms to use
     * @param defaultUri the default URI
     * @param workplaceServersList the workplace server URLs
     * @param sharedFolder the shared folder URI
     *
     * @throws CmsException if something goes wrong
     */
    public void updateGeneralSettings(
        CmsObject cms,
        String defaultUri,
        List<String> workplaceServersList,
        String sharedFolder)
    throws CmsException {

        Map<String, CmsSSLMode> workplaceServers = new LinkedHashMap<String, CmsSSLMode>();
        for (String server : workplaceServersList) {
            if (m_workplaceServers.containsKey(server)) {
                workplaceServers.put(server, m_workplaceServers.get(server));
            } else {
                workplaceServers.put(server, CmsSSLMode.NO);
            }
        }
        updateGeneralSettings(cms, defaultUri, workplaceServers, sharedFolder);
    }

    /**
     * Updates the general settings.<p>
     *
     * @param cms the cms to use
     * @param defaulrUri the default URI
     * @param workplaceServers the workplace server URLs
     * @param sharedFolder the shared folder URI
     *
     * @throws CmsException if something goes wrong
     */
    public void updateGeneralSettings(
        CmsObject cms,
        String defaulrUri,
        Map<String, CmsSSLMode> workplaceServers,
        String sharedFolder)
    throws CmsException {

        CmsObject clone = OpenCms.initCmsObject(cms);
        clone.getRequestContext().setSiteRoot("");

        // set the shared folder
        if ((sharedFolder == null)
            || sharedFolder.equals("")
            || sharedFolder.equals("/")
            || !sharedFolder.startsWith("/")
            || !sharedFolder.endsWith("/")
            || sharedFolder.startsWith("/sites/")) {
            throw new CmsException(
                Messages.get().container(Messages.ERR_INVALID_PATH_FOR_SHARED_FOLDER_1, sharedFolder));
        }

        m_frozen = false;
        setDefaultUri(clone.readResource(defaulrUri).getRootPath());
        setSharedFolder(clone.readResource(sharedFolder).getRootPath());
        m_workplaceServers = workplaceServers;
        initialize(cms);
        m_frozen = true;
    }

    /**
     * Updates or creates a site.<p>
     *
     * @param cms the CMS object
     * @param oldSite the site to remove if not <code>null</code>
     * @param newSite the site to add if not <code>null</code>
     *
     * @throws CmsException if something goes wrong
     */
    public void updateSite(CmsObject cms, CmsSite oldSite, CmsSite newSite) throws CmsException {

        if (oldSite != null) {
            // remove the old site
            removeSite(cms, oldSite);
        }

        if (newSite != null) {
            // add the new site
            addSite(cms, newSite);
        }
    }

    /**
     * Returns true if this request goes to a secure site.<p>
     *
     * @param req the request to check
     *
     * @return true if the request goes to a secure site
     */
    public boolean usesSecureSite(HttpServletRequest req) {

        CmsSite site = matchRequest(req);
        if (site == null) {
            return false;
        }
        CmsSiteMatcher secureMatcher = site.getSecureServerMatcher();
        boolean result = false;
        if (secureMatcher != null) {
            result = secureMatcher.equals(getRequestMatcher(req));
        }
        return result;
    }

    /**
     * Validates the site root, throwing an exception if the validation fails.
     *
     * @param siteRoot the site root to check
     */
    public void validateSiteRoot(String siteRoot) {

        if (!isValidSiteRoot(siteRoot)) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_INVALID_SITE_ROOT_1, siteRoot));
        }
    }

    /**
     * Adds a new Site matcher object to the map of server names.
     *
     * @param matcher the SiteMatcher of the server
     * @param site the site to add
     */
    private void addServer(CmsSiteMatcher matcher, CmsSite site) {

        Map<CmsSiteMatcher, CmsSite> siteMatcherSites = new HashMap<CmsSiteMatcher, CmsSite>(m_siteMatcherSites);
        siteMatcherSites.put(matcher, site);
        setSiteMatcherSites(siteMatcherSites);
    }

    /**
     * Fetches UUID for given site root from online and offline repository.<p>
     *
     * @param site to read and set UUID for
     * @param clone online CmsObject
     * @param cms_offline offline CmsObject
     */
    private void checkUUIDOfSiteRoot(CmsSite site, CmsObject clone, CmsObject cms_offline) {

        CmsUUID id = null;
        try {
            id = clone.readResource(site.getSiteRoot()).getStructureId();
        } catch (CmsException e) {
            //Ok, site root not available for online repository.
        }

        if ((id == null) && (cms_offline != null)) {
            try {
                id = cms_offline.readResource(site.getSiteRoot()).getStructureId();
                m_onlyOfflineSites.add(site);
            } catch (CmsException e) {
                //Siteroot not valid for on- and offline repository.
            }
        }
        if (id != null) {
            site.setSiteRootUUID(id);
            LOG.debug("Initializing site id: " + site + " => " + id);
            m_siteUUIDs.put(id, site);
        }
    }

    /**
     * Gets an offline project to read offline resources from.<p>
     *
     * @return CmsProject
     */
    private CmsProject getOfflineProject() {

        try {
            return m_clone.readProject("Offline");
        } catch (CmsException e) {
            try {
                for (CmsProject p : OpenCms.getOrgUnitManager().getAllAccessibleProjects(m_clone, "/", true)) {
                    if (!p.isOnlineProject()) {
                        return p;
                    }
                }
            } catch (CmsException e1) {
                LOG.error("Unable to get ptoject", e);
            }
        }
        return null;
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
        // this is required to get the right configured time offset
        int index = m_siteMatchers.indexOf(matcher);
        if (index < 0) {
            return matcher;
        }
        return m_siteMatchers.get(index);
    }

    /**
     * Finds the configured extension folders for all normal sites and stores them in a separate list.
     */
    private void initExtensionSites() {

        m_alternativeSiteData = new AlternativeSiteData(m_siteMatcherSites.values());
    }

    /**
     * Initializes the workplace matchers.<p>
     */
    private void initWorkplaceMatchers() {

        List<CmsSiteMatcher> matchers = new ArrayList<CmsSiteMatcher>();
        if (!m_workplaceServers.isEmpty()) {
            Map<String, CmsSiteMatcher> matchersByUrl = Maps.newHashMap();
            for (String server : m_workplaceServers.keySet()) {
                CmsSSLMode mode = m_workplaceServers.get(server);
                CmsSiteMatcher matcher = new CmsSiteMatcher(server);
                if ((mode == CmsSSLMode.LETS_ENCRYPT) || (mode == CmsSSLMode.MANUAL_EP_TERMINATION)) {
                    CmsSiteMatcher httpMatcher = matcher.forDifferentScheme("http");
                    CmsSiteMatcher httpsMatcher = matcher.forDifferentScheme("https");
                    for (CmsSiteMatcher current : new CmsSiteMatcher[] {httpMatcher, httpsMatcher}) {
                        matchersByUrl.put(current.getUrl(), current);
                        if (CmsLog.INIT.isInfoEnabled()) {
                            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_WORKPLACE_SITE_1, matcher));
                        }
                    }
                } else {
                    matchersByUrl.put(matcher.getUrl(), matcher);
                    if (CmsLog.INIT.isInfoEnabled()) {
                        CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_WORKPLACE_SITE_1, matcher));
                    }

                }
            }
            matchers = Lists.newArrayList(matchersByUrl.values());
        } else if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_WORKPLACE_SITE_0));
        }
        m_workplaceMatchers = matchers;
    }

    /**
     * Checks whether the given matcher is included in the currently configured and valid matchers.<p>
     *
     * @param matcher the matcher to check
     *
     * @return <code>true</code> in case the given matcher is included in the currently configured and valid matchers
     */
    private boolean isServerValid(CmsSiteMatcher matcher) {

        return !m_siteMatcherSites.containsKey(matcher);

    }

    /**
     * Validates the site root.
     *
     * @param uri the site root to validate
     * @return true if the site root is valid
     */
    private boolean isValidSiteRoot(String uri) {

        if ("".equals(uri)
            || "/".equals(uri)
            || CmsSiteManagerImpl.SITES_FOLDER.equals(uri)
            || CmsSiteManagerImpl.SITES_FOLDER.equals(uri + "/")) {
            return false;
        }
        return true;
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
            String siteRoot = m_additionalSiteRoots.get(i);
            if (rootPath.startsWith(siteRoot + "/")) {
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
            return m_siteRootSites.get(rootPath.substring(0, pos));
        }
        return null;
    }

    /**
     * Sets the class member variables {@link #m_siteMatcherSites} and  {@link #m_siteMatchers}
     * from the provided map of configured site matchers.<p>
     *
     * @param siteMatcherSites the site matches to set
     */
    private void setSiteMatcherSites(Map<CmsSiteMatcher, CmsSite> siteMatcherSites) {

        m_siteMatcherSites = Collections.unmodifiableMap(siteMatcherSites);
        m_siteMatchers = Collections.unmodifiableList(new ArrayList<CmsSiteMatcher>(m_siteMatcherSites.keySet()));
    }
}
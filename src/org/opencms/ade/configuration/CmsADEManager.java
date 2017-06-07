/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.ade.configuration.CmsADEConfigData.DetailInfo;
import org.opencms.ade.configuration.CmsElementView.ElementViewComparator;
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCache;
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCacheState;
import org.opencms.ade.containerpage.inherited.CmsContainerConfigurationCache;
import org.opencms.ade.containerpage.inherited.CmsContainerConfigurationWriter;
import org.opencms.ade.containerpage.inherited.CmsInheritedContainerState;
import org.opencms.ade.detailpage.CmsDetailPageConfigurationWriter;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.detailpage.CmsSitemapDetailPageFinder;
import org.opencms.ade.detailpage.I_CmsDetailPageFinder;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.shared.CmsPermissionInfo;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.jsp.CmsJspTagLink;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsADECache;
import org.opencms.xml.containerpage.CmsADECacheSettings;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.Messages;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This is the main class used to access the ADE configuration and also accomplish some other related tasks
 * like loading/saving favorite and recent lists.<p>
 */
public class CmsADEManager {

    /** JSON property name constant. */
    protected enum FavListProp {
        /** element property. */
        ELEMENT,
        /** formatter property. */
        FORMATTER,
        /** properties property. */
        PROPERTIES;
    }

    /**
     * A status enum for the initialization status.<p>
     */
    protected enum Status {
        /** already initialized. */
        initialized,
        /** currently initializing. */
        initializing,
        /** not initialized. */
        notInitialized
    }

    /** The client id separator. */
    public static final String CLIENT_ID_SEPERATOR = "#";

    /** The configuration file name. */
    public static final String CONFIG_FILE_NAME = ".config";

    /** The name of the sitemap configuration file type. */
    public static final String CONFIG_FOLDER_TYPE = "content_folder";

    /** The path for sitemap configuration files relative from the base path. */
    public static final String CONFIG_SUFFIX = "/"
        + CmsADEManager.CONTENT_FOLDER_NAME
        + "/"
        + CmsADEManager.CONFIG_FILE_NAME;

    /** The name of the sitemap configuration file type. */
    public static final String CONFIG_TYPE = "sitemap_config";

    /** The content folder name. */
    public static final String CONTENT_FOLDER_NAME = ".content";

    /** Default favorite/recent list size constant. */
    public static final int DEFAULT_ELEMENT_LIST_SIZE = 10;

    /** The name of the element view configuration file type. */
    public static final String ELEMENT_VIEW_TYPE = "elementview";

    /** The name of the module configuration file type. */
    public static final String MODULE_CONFIG_TYPE = "module_config";

    /** Node name for the nav level link value. */
    public static final String N_LINK = "Link";

    /** Node name for the nav level type value. */
    public static final String N_TYPE = "Type";

    /** The path to the sitemap editor JSP. */
    public static final String PATH_SITEMAP_EDITOR_JSP = "/system/workplace/commons/sitemap.jsp";

    /** User additional info key constant. */
    protected static final String ADDINFO_ADE_FAVORITE_LIST = "ADE_FAVORITE_LIST";

    /** User additional info key constant. */
    protected static final String ADDINFO_ADE_RECENT_LIST = "ADE_RECENT_LIST";

    /** User additional info key constant. */
    protected static final String ADDINFO_ADE_SHOW_EDITOR_HELP = "ADE_SHOW_EDITOR_HELP";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEManager.class);

    /** The cache instance. */
    private CmsADECache m_cache;

    /** The sitemap configuration file type. */
    private I_CmsResourceType m_configType;

    /** The detail page finder. */
    private I_CmsDetailPageFinder m_detailPageFinder = new CmsSitemapDetailPageFinder();

    /** The element view configuration file type. */
    private I_CmsResourceType m_elementViewType;

    /** The initialization status. */
    private Status m_initStatus = Status.notInitialized;

    /** The module configuration file type. */
    private I_CmsResourceType m_moduleConfigType;

    /** The online cache instance. */
    private CmsConfigurationCache m_offlineCache;

    /** The offline CMS context. */
    private CmsObject m_offlineCms;

    /** The offline inherited container configuration cache. */
    private CmsContainerConfigurationCache m_offlineContainerConfigurationCache;

    /** The detail id cache for the Offline project. */
    private CmsDetailNameCache m_offlineDetailIdCache;

    /** The offline formatter bean cache. */
    private CmsFormatterConfigurationCache m_offlineFormatterCache;

    /** The offline cache instance. */
    private CmsConfigurationCache m_onlineCache;

    /** The online CMS context. */
    private CmsObject m_onlineCms;

    /** The online inherited container configuration cache. */
    private CmsContainerConfigurationCache m_onlineContainerConfigurationCache;

    /** The Online project detail id cache. */
    private CmsDetailNameCache m_onlineDetailIdCache;

    /** The online formatter bean cache. */
    private CmsFormatterConfigurationCache m_onlineFormatterCache;

    /** ADE parameters. */
    private Map<String, String> m_parameters;

    /**
     * Creates a new ADE manager.<p>
     *
     * @param adminCms a CMS context with admin privileges
     * @param memoryMonitor the memory monitor instance
     * @param systemConfiguration the system configuration
     */
    public CmsADEManager(
        CmsObject adminCms,
        CmsMemoryMonitor memoryMonitor,
        CmsSystemConfiguration systemConfiguration) {

        // initialize the ade cache
        CmsADECacheSettings cacheSettings = systemConfiguration.getAdeCacheSettings();
        if (cacheSettings == null) {
            cacheSettings = new CmsADECacheSettings();
        }
        m_onlineCms = adminCms;
        m_cache = new CmsADECache(memoryMonitor, cacheSettings);
        m_parameters = new LinkedHashMap<String, String>(systemConfiguration.getAdeParameters());
        // further initialization is done by the initialize() method. We don't do that in the constructor,
        // because during the setup the configuration resource types don't exist yet.
    }

    /**
     * Finds the entry point to a sitemap.<p>
     *
     * @param cms the CMS context
     * @param openPath the resource path to find the sitemap to
     *
     * @return the sitemap entry point
     */
    public String findEntryPoint(CmsObject cms, String openPath) {

        CmsADEConfigData configData = lookupConfiguration(cms, openPath);
        String result = configData.getBasePath();
        if (result == null) {
            return cms.getRequestContext().addSiteRoot("/");
        }
        return result;
    }

    /**
     * Flushes inheritance group changes so the cache is updated.<p>
     *
     * This is useful for test cases.
     */
    public void flushInheritanceGroupChanges() {

        m_offlineContainerConfigurationCache.flushUpdates();
        m_onlineContainerConfigurationCache.flushUpdates();
    }

    /**
     * Gets the complete list of beans for the currently configured detail pages.<p>
     *
     * @param cms the CMS context to use
     *
     * @return the list of detail page infos
     */
    public List<CmsDetailPageInfo> getAllDetailPages(CmsObject cms) {

        return getCacheState(isOnline(cms)).getAllDetailPages();
    }

    /**
     * Gets the containerpage cache instance.<p>
     *
     * @return the containerpage cache instance
     */
    public CmsADECache getCache() {

        return m_cache;
    }

    /**
     * Gets the cached formatter beans.<p>
     *
     * @param online true if the Online project formatters should be returned, false for the Offline formatters
     *
     * @return the formatter configuration cache state
     */
    public CmsFormatterConfigurationCacheState getCachedFormatters(boolean online) {

        CmsFormatterConfigurationCache cache = online ? m_onlineFormatterCache : m_offlineFormatterCache;
        return cache.getState();
    }

    /**
     * Gets the configuration file type.<p>
     *
     * @return the configuration file type
     */
    public I_CmsResourceType getConfigurationType() {

        return m_configType;
    }

    /**
     * Returns the names of the bundles configured as workplace bundles in any module configuration.
     * @return the names of the bundles configured as workplace bundles in any module configuration.
     */
    public Set<String> getConfiguredWorkplaceBundles() {

        CmsADEConfigData configData = internalLookupConfiguration(null, null);
        return configData.getConfiguredWorkplaceBundles();
    }

    /**
     * Reads the current element bean from the request.<p>
     *
     * @param req the servlet request
     *
     * @return the element bean
     *
     * @throws CmsException if no current element is set
     */
    public CmsContainerElementBean getCurrentElement(ServletRequest req) throws CmsException {

        CmsContainerElementBean element = CmsJspStandardContextBean.getInstance(req).getElement();
        if (element == null) {
            throw new CmsException(Messages.get().container(Messages.ERR_READING_ELEMENT_FROM_REQUEST_0));
        }
        return element;
    }

    /**
     * Gets the detail id cache for the Online or Offline projects.<p>
     *
     * @param online if true, gets the Online project detail id
     *
     * @return the detail name cache
     */
    public CmsDetailNameCache getDetailIdCache(boolean online) {

        return online ? m_onlineDetailIdCache : m_offlineDetailIdCache;
    }

    /**
     * Gets the detail page information for  everything.<p>
     *
     * @param cms the current CMS context
     *
     * @return the list with all the detail page information
     */
    public List<DetailInfo> getDetailInfo(CmsObject cms) {

        return getCacheState(isOnline(cms)).getDetailInfosForSubsites(cms);
    }

    /**
     * Gets the detail page for a content element.<p>
     *
     * @param cms the CMS context
     * @param pageRootPath the element's root path
     * @param originPath the path in which the the detail page is being requested
     *
     * @return the detail page for the content element
     */
    public String getDetailPage(CmsObject cms, String pageRootPath, String originPath) {

        return getDetailPage(cms, pageRootPath, originPath, null);
    }

    /**
     * Gets the detail page for a content element.<p>
     *
     * @param cms the CMS context
     * @param pageRootPath the element's root path
     * @param originPath the path in which the the detail page is being requested
     * @param targetDetailPage the target detail page to use
     *
     * @return the detail page for the content element
     */
    public String getDetailPage(CmsObject cms, String pageRootPath, String originPath, String targetDetailPage) {

        boolean online = isOnline(cms);
        String resType = getCacheState(online).getParentFolderType(pageRootPath);
        if (resType == null) {
            return null;
        }
        if ((targetDetailPage != null) && getDetailPages(cms, resType).contains(targetDetailPage)) {
            return targetDetailPage;
        }

        String originRootPath = cms.getRequestContext().addSiteRoot(originPath);
        CmsADEConfigData configData = lookupConfiguration(cms, originRootPath);
        CmsADEConfigData targetConfigData = lookupConfiguration(cms, pageRootPath);
        boolean targetFirst = targetConfigData.isPreferDetailPagesForLocalContents();
        List<CmsADEConfigData> configs = targetFirst
        ? Arrays.asList(targetConfigData, configData)
        : Arrays.asList(configData, targetConfigData);
        for (CmsADEConfigData config : configs) {
            List<CmsDetailPageInfo> pageInfo = config.getDetailPagesForType(resType);
            if ((pageInfo != null) && !pageInfo.isEmpty()) {
                return pageInfo.get(0).getUri();
            }
        }
        return null;
    }

    /**
     * Gets the detail page finder.<p>
     *
     * @return the detail page finder
     */
    public I_CmsDetailPageFinder getDetailPageFinder() {

        return m_detailPageFinder;
    }

    /**
     * Returns the main detail pages for a type in all of the VFS tree.<p>
     *
     * @param cms the current CMS context
     * @param type the resource type name
     * @return a list of detail page root paths
     */
    public List<String> getDetailPages(CmsObject cms, String type) {

        CmsConfigurationCache cache = isOnline(cms) ? m_onlineCache : m_offlineCache;
        return cache.getState().getDetailPages(type);
    }

    /**
     * Gets the set of types for which detail pages are defined.<p>
     *
     * @param cms the current CMS context
     *
     * @return the set of types for which detail pages are defined
     */
    public Set<String> getDetailPageTypes(CmsObject cms) {

        return getCacheState(isOnline(cms)).getDetailPageTypes();
    }

    /**
     * Returns the element settings for a given resource.<p>
     *
     * @param cms the current cms context
     * @param resource the resource
     *
     * @return the element settings for a given resource
     *
     * @throws CmsException if something goes wrong
     */
    public Map<String, CmsXmlContentProperty> getElementSettings(CmsObject cms, CmsResource resource)
    throws CmsException {

        if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
            Map<String, CmsXmlContentProperty> result = new LinkedHashMap<String, CmsXmlContentProperty>();
            Map<String, CmsXmlContentProperty> settings = CmsXmlContentDefinition.getContentHandlerForResource(
                cms,
                resource).getSettings(cms, resource);
            result.putAll(settings);
            return CmsXmlContentPropertyHelper.copyPropertyConfiguration(result);
        }
        return Collections.<String, CmsXmlContentProperty> emptyMap();
    }

    /**
     * Returns the available element views.<p>
     *
     * @param cms the cms context
     *
     * @return the element views
     */
    public Map<CmsUUID, CmsElementView> getElementViews(CmsObject cms) {

        CmsConfigurationCache cache = getCache(isOnline(cms));
        List<CmsElementView> viewList = Lists.newArrayList();
        viewList.addAll(cache.getState().getElementViews().values());
        viewList.addAll(OpenCms.getWorkplaceManager().getExplorerTypeViews().values());
        Collections.sort(viewList, new ElementViewComparator());
        Map<CmsUUID, CmsElementView> result = Maps.newLinkedHashMap();
        for (CmsElementView viewValue : viewList) {
            result.put(viewValue.getId(), viewValue);
        }
        return result;
    }

    /**
     * Gets the element view configuration resource type.<p>
     *
     * @return the element view configuration resource type
     */
    public I_CmsResourceType getElementViewType() {

        return m_elementViewType;
    }

    /**
     * Returns the favorite list, or creates it if not available.<p>
     *
     * @param cms the cms context
     *
     * @return the favorite list
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsContainerElementBean> getFavoriteList(CmsObject cms) throws CmsException {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        Object obj = user.getAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST);

        List<CmsContainerElementBean> favList = new ArrayList<CmsContainerElementBean>();
        if (obj instanceof String) {
            try {
                JSONArray array = new JSONArray((String)obj);
                for (int i = 0; i < array.length(); i++) {
                    try {
                        favList.add(elementFromJson(array.getJSONObject(i)));
                    } catch (Throwable e) {
                        // should never happen, catches wrong or no longer existing values
                        LOG.warn(e.getLocalizedMessage());
                    }
                }
            } catch (Throwable e) {
                // should never happen, catches json parsing
                LOG.warn(e.getLocalizedMessage());
            }
        } else {
            // save to be better next time
            saveFavoriteList(cms, favList);
        }

        return favList;
    }

    /**
     * Returns the inheritance state for the given inheritance name and resource.<p>
     *
     * @param cms the current cms context
     * @param resource the resource
     * @param name the inheritance name
     *
     * @return the inheritance state
     */
    public CmsInheritedContainerState getInheritedContainerState(CmsObject cms, CmsResource resource, String name) {

        String rootPath = resource.getRootPath();
        if (!resource.isFolder()) {
            rootPath = CmsResource.getParentFolder(rootPath);
        }
        CmsInheritedContainerState result = new CmsInheritedContainerState();
        boolean online = isOnline(cms);
        CmsContainerConfigurationCache cache = online
        ? m_onlineContainerConfigurationCache
        : m_offlineContainerConfigurationCache;
        result.addConfigurations(cache, rootPath, name);
        return result;

    }

    /**
     * Returns the inheritance state for the given inheritance name and root path.<p>
     *
     * @param cms the current cms context
     * @param rootPath the root path
     * @param name the inheritance name
     *
     * @return the inheritance state
     *
     * @throws CmsException if something goes wrong
     */
    public CmsInheritedContainerState getInheritedContainerState(CmsObject cms, String rootPath, String name)
    throws CmsException {

        String oldSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("");
            CmsResource resource = cms.readResource(rootPath);
            return getInheritedContainerState(cms, resource, name);
        } finally {
            cms.getRequestContext().setSiteRoot(oldSiteRoot);
        }
    }

    /**
     * Gets the maximum sitemap depth.<p>
     *
     * @return the maximum sitemap depth
     */
    public int getMaxSitemapDepth() {

        return 20;
    }

    /**
     * Gets the module configuration resource type.<p>
     *
     * @return the module configuration resource type
     */
    public I_CmsResourceType getModuleConfigurationType() {

        return m_moduleConfigType;
    }

    /**
     * Gets ADE parameters.<p>
     *
     * @param cms the current CMS context
     * @return the ADE parameters for the current user
     */
    public Map<String, String> getParameters(CmsObject cms) {

        Map<String, String> result = new LinkedHashMap<String, String>(m_parameters);
        if (cms != null) {
            String userParamsStr = (String)(cms.getRequestContext().getCurrentUser().getAdditionalInfo().get(
                "ADE_PARAMS"));
            if (userParamsStr != null) {
                Map<String, String> userParams = CmsStringUtil.splitAsMap(userParamsStr, "|", ":");
                result.putAll(userParams);
            }
        }
        return result;
    }

    /**
     * Returns the permission info for the given resource.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     * @param contextPath the context path
     *
     * @return the permission info
     *
     * @throws CmsException if checking the permissions fails
     */
    public CmsPermissionInfo getPermissionInfo(CmsObject cms, CmsResource resource, String contextPath)
    throws CmsException {

        boolean hasView = cms.hasPermissions(
            resource,
            CmsPermissionSet.ACCESS_VIEW,
            false,
            CmsResourceFilter.ALL.addRequireVisible());
        boolean hasWrite = false;
        if (hasView) {
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
            hasView = (settings == null) || settings.getAccess().getPermissions(cms, resource).requiresViewPermission();
            if (hasView
                && CmsResourceTypeXmlContent.isXmlContent(resource)
                && !CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
                if (contextPath == null) {
                    contextPath = resource.getRootPath();
                }
                CmsResourceTypeConfig localConfigData = lookupConfiguration(cms, contextPath).getResourceType(
                    type.getTypeName());
                if (localConfigData != null) {
                    Map<CmsUUID, CmsElementView> elmenetViews = getElementViews(cms);
                    hasView = elmenetViews.containsKey(localConfigData.getElementView())
                        && elmenetViews.get(localConfigData.getElementView()).hasPermission(cms, resource);
                }
            }
            // the user may only have write permissions if he is allowed to view the resource
            hasWrite = hasView
                && cms.hasPermissions(
                    resource,
                    CmsPermissionSet.ACCESS_WRITE,
                    false,
                    CmsResourceFilter.IGNORE_EXPIRATION)
                && ((settings == null) || settings.getAccess().getPermissions(cms, resource).requiresWritePermission());
        }

        String noEdit = new CmsResourceUtil(cms, resource).getNoEditReason(
            OpenCms.getWorkplaceManager().getWorkplaceLocale(cms),
            true);
        return new CmsPermissionInfo(hasView, hasWrite, noEdit);
    }

    /**
     * Returns the favorite list, or creates it if not available.<p>
     *
     * @param cms the cms context
     *
     * @return the favorite list
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsContainerElementBean> getRecentList(CmsObject cms) throws CmsException {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        Object obj = user.getAdditionalInfo(ADDINFO_ADE_RECENT_LIST);

        List<CmsContainerElementBean> recentList = new ArrayList<CmsContainerElementBean>();
        if (obj instanceof String) {
            try {
                JSONArray array = new JSONArray((String)obj);
                for (int i = 0; i < array.length(); i++) {
                    try {
                        recentList.add(elementFromJson(array.getJSONObject(i)));
                    } catch (Throwable e) {
                        // should never happen, catches wrong or no longer existing values
                        LOG.warn(e.getLocalizedMessage());
                    }
                }
            } catch (Throwable e) {
                // should never happen, catches json parsing
                LOG.warn(e.getLocalizedMessage());
            }
        } else {
            // save to be better next time
            saveRecentList(cms, recentList);
        }

        return recentList;
    }

    /**
     * Gets the sitemap configuration resource type.<p>
     *
     * @return the resource type for sitemap configurations
     */
    public I_CmsResourceType getSitemapConfigurationType() {

        return m_configType;
    }

    /**
     * Returns all sub sites below the given path.<p>
     *
     * @param cms the cms context
     * @param subSiteRoot the sub site root path
     *
     * @return the sub site root paths
     */
    public List<String> getSubSitePaths(CmsObject cms, String subSiteRoot) {

        List<String> result = new ArrayList<String>();
        String normalizedRootPath = CmsStringUtil.joinPaths("/", subSiteRoot, "/");
        CmsADEConfigCacheState state = getCacheState(isOnline(cms));
        Set<String> siteConfigurationPaths = state.getSiteConfigurationPaths();
        for (String path : siteConfigurationPaths) {
            if ((path.length() > normalizedRootPath.length()) && path.startsWith(normalizedRootPath)) {
                result.add(path);
            }
        }
        return result;
    }

    /**
     * Tries to get the subsite root for a given resource root path.<p>
     *
     * @param cms the current CMS context
     * @param rootPath the root path for which the subsite root should be found
     *
     * @return the subsite root
     */
    public String getSubSiteRoot(CmsObject cms, String rootPath) {

        CmsADEConfigData configData = lookupConfiguration(cms, rootPath);
        String basePath = configData.getBasePath();
        String siteRoot = OpenCms.getSiteManager().getSiteRoot(rootPath);
        if (siteRoot == null) {
            siteRoot = "";
        }
        if ((basePath == null) || !basePath.startsWith(siteRoot)) {
            // the subsite root should always be below the site root
            return siteRoot;
        } else {
            return basePath;
        }
    }

    /**
     * Processes a HTML redirect content.<p>
     *
     * This needs to be in the ADE manager because the user for whom the HTML redirect is being loaded
     * does not necessarily have read permissions for the redirect target, so we read the redirect target
     * with admin privileges.<p>
     *
     * @param userCms the CMS context of the current user
     * @param request the servlet request
     * @param response the servlet response
     * @param htmlRedirect the path of the HTML redirect resource
     *
     * @throws CmsException if something goes wrong
     */
    public void handleHtmlRedirect(
        CmsObject userCms,
        HttpServletRequest request,
        HttpServletResponse response,
        String htmlRedirect)
    throws CmsException {

        CmsObject cms = OpenCms.initCmsObject(m_offlineCms);
        CmsRequestContext userContext = userCms.getRequestContext();
        CmsRequestContext currentContext = cms.getRequestContext();
        currentContext.setCurrentProject(userContext.getCurrentProject());
        currentContext.setSiteRoot(userContext.getSiteRoot());
        currentContext.setLocale(userContext.getLocale());
        currentContext.setUri(userContext.getUri());

        CmsFile file = cms.readFile(htmlRedirect);
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);

        // find out the locale to use for reading values from the redirect
        List<Locale> candidates = new ArrayList<Locale>();
        candidates.add(currentContext.getLocale());
        candidates.add(CmsLocaleManager.getDefaultLocale());
        candidates.add(Locale.ENGLISH);
        candidates.addAll(content.getLocales());
        Locale contentLocale = currentContext.getLocale();
        for (Locale candidateLocale : candidates) {
            if (content.hasLocale(candidateLocale)) {
                contentLocale = candidateLocale;
                break;
            }
        }

        String typeValue = content.getValue(N_TYPE, contentLocale).getStringValue(cms);
        String lnkUri = "";
        Integer errorCode;
        if ("sublevel".equals(typeValue)) {
            // use the nav builder to get the first sub level entry
            CmsJspNavBuilder navBuilder = new CmsJspNavBuilder(cms);
            if (navBuilder.getNavigationForFolder().size() > 0) {
                CmsJspNavElement target = navBuilder.getNavigationForFolder().get(0);
                lnkUri = CmsJspTagLink.linkTagAction(target.getResourceName(), request);
                errorCode = Integer.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY);
            } else {
                // send error 404 if no sub entry available
                errorCode = Integer.valueOf(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            String linkValue = content.getValue(N_LINK, contentLocale).getStringValue(cms);
            lnkUri = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, linkValue);
            try {
                errorCode = Integer.valueOf(typeValue);
            } catch (NumberFormatException e) {
                LOG.error(e.getMessage(), e);
                // fall back to default
                errorCode = Integer.valueOf(307);
            }
        }
        request.setAttribute(CmsRequestUtil.ATTRIBUTE_ERRORCODE, errorCode);
        response.setHeader("Location", CmsEncoder.convertHostToPunycode(lnkUri));
        response.setHeader("Connection", "close");
        response.setStatus(errorCode.intValue());
    }

    /**
     * Initializes the configuration by reading all configuration files and caching their data.<p>
     */
    public synchronized void initialize() {

        if (m_initStatus == Status.notInitialized) {
            try {
                m_initStatus = Status.initializing;
                m_configType = OpenCms.getResourceManager().getResourceType(CONFIG_TYPE);
                m_moduleConfigType = OpenCms.getResourceManager().getResourceType(MODULE_CONFIG_TYPE);
                m_elementViewType = OpenCms.getResourceManager().getResourceType(ELEMENT_VIEW_TYPE);
                CmsProject temp = getTempfileProject(m_onlineCms);
                m_offlineCms = OpenCms.initCmsObject(m_onlineCms);
                m_offlineCms.getRequestContext().setCurrentProject(temp);
                m_onlineCache = new CmsConfigurationCache(
                    m_onlineCms,
                    m_configType,
                    m_moduleConfigType,
                    m_elementViewType);
                m_offlineCache = new CmsConfigurationCache(
                    m_offlineCms,
                    m_configType,
                    m_moduleConfigType,
                    m_elementViewType);
                m_onlineCache.initialize();
                m_offlineCache.initialize();
                m_onlineContainerConfigurationCache = new CmsContainerConfigurationCache(
                    m_onlineCms,
                    "online inheritance groups");
                m_offlineContainerConfigurationCache = new CmsContainerConfigurationCache(
                    m_offlineCms,
                    "offline inheritance groups");
                m_onlineContainerConfigurationCache.initialize();
                m_offlineContainerConfigurationCache.initialize();
                m_offlineFormatterCache = new CmsFormatterConfigurationCache(m_offlineCms, "offline formatters");
                m_onlineFormatterCache = new CmsFormatterConfigurationCache(m_onlineCms, "online formatters");
                m_offlineFormatterCache.reload();
                m_onlineFormatterCache.reload();

                m_offlineDetailIdCache = new CmsDetailNameCache(m_offlineCms);
                m_onlineDetailIdCache = new CmsDetailNameCache(m_onlineCms);
                m_offlineDetailIdCache.initialize();
                m_onlineDetailIdCache.initialize();

                CmsGlobalConfigurationCacheEventHandler handler = new CmsGlobalConfigurationCacheEventHandler(
                    m_onlineCms);
                handler.addCache(m_offlineCache, m_onlineCache, "ADE configuration cache");
                handler.addCache(
                    m_offlineContainerConfigurationCache,
                    m_onlineContainerConfigurationCache,
                    "Inherited container cache");
                handler.addCache(m_offlineFormatterCache, m_onlineFormatterCache, "formatter configuration cache");
                handler.addCache(m_offlineDetailIdCache, m_onlineDetailIdCache, "Detail ID cache");
                OpenCms.getEventManager().addCmsEventListener(handler);
                m_initStatus = Status.initialized;
            } catch (CmsException e) {
                m_initStatus = Status.notInitialized;
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Checks whether the given resource is configured as a detail page.<p>
     *
     * @param cms the current CMS context
     * @param resource the resource which should be tested
     *
     * @return true if the resource is configured as a detail page
     */
    public boolean isDetailPage(CmsObject cms, CmsResource resource) {

        return getCache(isOnline(cms)).isDetailPage(cms, resource);
    }

    /**
     * Checks whether the ADE manager is initialized (this should usually be the case except during the setup).<p>
     *
     * @return true if the ADE manager is initialized
     */
    public boolean isInitialized() {

        return m_initStatus == Status.initialized;
    }

    /**
     * Returns the show editor help flag.<p>
     *
     * @param cms the cms context
     *
     * @return the show editor help flag
     */
    public boolean isShowEditorHelp(CmsObject cms) {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        String showHelp = (String)user.getAdditionalInfo(ADDINFO_ADE_SHOW_EDITOR_HELP);
        return CmsStringUtil.isEmptyOrWhitespaceOnly(showHelp) || Boolean.parseBoolean(showHelp);
    }

    /**
     * Looks up the configuration data for a given sitemap path.<p>
     *
     * @param cms the current CMS context
     * @param rootPath the root path for which the configuration data should be looked up
     *
     * @return the configuration data
     */
    public CmsADEConfigData lookupConfiguration(CmsObject cms, String rootPath) {

        CmsADEConfigData configData = internalLookupConfiguration(cms, rootPath);
        return configData;
    }

    /**
     * Reloads the configuration.<p>
     *
     * Normally you shouldn't call this directly since the event handlers take care of updating the configuration.
     */
    public void refresh() {

        m_onlineCache.initialize();
        m_offlineCache.initialize();
    }

    /**
     * Saves a list of detail pages.<p>
     * @param cms the cms context
     * @param rootPath the root path
     * @param detailPages the detail pages
     * @param newId the id to use for new detail pages without an id
     * @return true if the detail pages could be successfully saved
     *
     * @throws CmsException if something goes wrong
     */
    public boolean saveDetailPages(CmsObject cms, String rootPath, List<CmsDetailPageInfo> detailPages, CmsUUID newId)
    throws CmsException {

        CmsADEConfigData configData = lookupConfiguration(cms, rootPath);
        CmsDetailPageConfigurationWriter configWriter;
        String originalSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("");
            if (configData.isModuleConfiguration()) {
                return false;
            }
            CmsResource configFile = configData.getResource();
            configWriter = new CmsDetailPageConfigurationWriter(cms, configFile);
            configWriter.updateAndSave(detailPages, newId);
            return true;
        } finally {
            cms.getRequestContext().setSiteRoot(originalSiteRoot);
        }
    }

    /**
     * Saves the favorite list, user based.<p>
     *
     * @param cms the cms context
     * @param favoriteList the element list
     *
     * @throws CmsException if something goes wrong
     */
    public void saveFavoriteList(CmsObject cms, List<CmsContainerElementBean> favoriteList) throws CmsException {

        saveElementList(cms, favoriteList, ADDINFO_ADE_FAVORITE_LIST);
    }

    /**
     * Saves the inheritance container information.<p>
     *
     * @param cms the current cms context
     * @param pageResource the resource or parent folder
     * @param name the inheritance name
     * @param newOrder if the element have been reordered
     * @param elements the elements
     *
     * @throws CmsException if something goes wrong
     */
    public void saveInheritedContainer(
        CmsObject cms,
        CmsResource pageResource,
        String name,
        boolean newOrder,
        List<CmsContainerElementBean> elements)
    throws CmsException {

        CmsContainerConfigurationWriter writer = new CmsContainerConfigurationWriter();
        writer.save(cms, name, newOrder, pageResource, elements);

        // Inheritance groups are usually reloaded directly after saving them,
        // so the cache needs to be up to date after this method is called
        m_offlineContainerConfigurationCache.flushUpdates();
    }

    /**
     * Saves the inheritance container information.<p>
     *
     * @param cms the current cms context
     * @param sitePath the site path of the resource or parent folder
     * @param name the inheritance name
     * @param newOrder if the element have been reordered
     * @param elements the elements
     *
     * @throws CmsException if something goes wrong
     */
    public void saveInheritedContainer(
        CmsObject cms,
        String sitePath,
        String name,
        boolean newOrder,
        List<CmsContainerElementBean> elements)
    throws CmsException {

        saveInheritedContainer(cms, cms.readResource(sitePath), name, newOrder, elements);
    }

    /**
     * Saves the favorite list, user based.<p>
     *
     * @param cms the cms context
     * @param recentList the element list
     *
     * @throws CmsException if something goes wrong
     */
    public void saveRecentList(CmsObject cms, List<CmsContainerElementBean> recentList) throws CmsException {

        saveElementList(cms, recentList, ADDINFO_ADE_RECENT_LIST);
    }

    /**
     * Sets the show editor help flag.<p>
     *
     * @param cms the cms context
     * @param showHelp the show help flag
     * @throws CmsException if writing the user info fails
     */
    public void setShowEditorHelp(CmsObject cms, boolean showHelp) throws CmsException {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        user.setAdditionalInfo(ADDINFO_ADE_SHOW_EDITOR_HELP, String.valueOf(showHelp));
        cms.writeUser(user);
    }

    /**
     * The method which is called when the OpenCms instance is shut down.<p>
     */
    public void shutdown() {

        // do nothing
    }

    /**
     * Waits until the next time the cache is updated.<p>
     *
     * @param online true if we want to wait for the online cache, false for the offline cache
     */
    public void waitForCacheUpdate(boolean online) {

        getCache(online).getWaitHandleForUpdateTask().enter(2 * CmsConfigurationCache.TASK_DELAY_MILLIS);
    }

    /**
     * Waits until the formatter cache has finished updating itself.<p>
     *
     * This method is only intended for use in test cases.
     *
     * @param online true if we should wait for the online formatter cache,false for the offline cache
     */
    public void waitForFormatterCache(boolean online) {

        CmsFormatterConfigurationCache cache = online ? m_onlineFormatterCache : m_offlineFormatterCache;
        cache.waitForUpdate();
    }

    /**
     * Creates an element from its serialized data.<p>
     *
     * @param data the serialized data
     *
     * @return the restored element bean
     *
     * @throws JSONException if the serialized data got corrupted
     */
    protected CmsContainerElementBean elementFromJson(JSONObject data) throws JSONException {

        CmsUUID element = new CmsUUID(data.getString(FavListProp.ELEMENT.name().toLowerCase()));
        CmsUUID formatter = null;
        if (data.has(FavListProp.FORMATTER.name().toLowerCase())) {
            formatter = new CmsUUID(data.getString(FavListProp.FORMATTER.name().toLowerCase()));
        }
        Map<String, String> properties = new HashMap<String, String>();

        JSONObject props = data.getJSONObject(FavListProp.PROPERTIES.name().toLowerCase());
        Iterator<String> keys = props.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            properties.put(key, props.getString(key));
        }

        return new CmsContainerElementBean(element, formatter, properties, false);
    }

    /**
     * Converts the given element to JSON.<p>
     *
     * @param element the element to convert
     * @param excludeSettings the keys of settings which should not be written to the JSON
     *
     * @return the JSON representation
     */
    protected JSONObject elementToJson(CmsContainerElementBean element, Set<String> excludeSettings) {

        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put(FavListProp.ELEMENT.name().toLowerCase(), element.getId().toString());
            if (element.getFormatterId() != null) {
                data.put(FavListProp.FORMATTER.name().toLowerCase(), element.getFormatterId().toString());
            }
            JSONObject properties = new JSONObject();
            for (Map.Entry<String, String> entry : element.getIndividualSettings().entrySet()) {
                String settingKey = entry.getKey();
                if (!excludeSettings.contains(settingKey)) {
                    properties.put(entry.getKey(), entry.getValue());
                }
            }
            data.put(FavListProp.PROPERTIES.name().toLowerCase(), properties);
        } catch (JSONException e) {
            // should never happen
            if (!LOG.isDebugEnabled()) {
                LOG.warn(e.getLocalizedMessage());
            }
            LOG.debug(e.getLocalizedMessage(), e);
            return null;
        }
        return data;
    }

    /**
     * Gets the configuration cache instance.<p>
     *
     * @param online true if you want the online cache, false for the offline cache
     *
     * @return the ADE configuration cache instance
     */
    protected CmsConfigurationCache getCache(boolean online) {

        return online ? m_onlineCache : m_offlineCache;
    }

    /**
     * Gets the current ADE configuration cache state.<p>
     *
     * @param online true if you want the online state, false for the offline state
     *
     * @return the configuration cache state
     */
    protected CmsADEConfigCacheState getCacheState(boolean online) {

        return (online ? m_onlineCache : m_offlineCache).getState();
    }

    /**
     * Gets the offline cache.<p>
     *
     * @return the offline configuration cache
     */
    protected CmsConfigurationCache getOfflineCache() {

        return m_offlineCache;
    }

    /**
     * Gets the online cache.<p>
     *
     * @return the online configuration cache
     */
    protected CmsConfigurationCache getOnlineCache() {

        return m_onlineCache;
    }

    /**
     * Gets the root path for a given resource structure id.<p>
     *
     * @param structureId the structure id
     * @param online if true, the resource will be looked up in the online project ,else in the offline project
     *
     * @return the root path for the given structure id
     *
     * @throws CmsException if something goes wrong
     */
    protected String getRootPath(CmsUUID structureId, boolean online) throws CmsException {

        CmsConfigurationCache cache = online ? m_onlineCache : m_offlineCache;
        return cache.getPathForStructureId(structureId);
    }

    /**
     * Gets a tempfile project, creating one if it doesn't exist already.<p>
     *
     * @param cms the CMS context to use
     * @return the tempfile project
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsProject getTempfileProject(CmsObject cms) throws CmsException {

        try {
            return cms.readProject(I_CmsProjectDriver.TEMP_FILE_PROJECT_NAME);
        } catch (CmsException e) {
            return cms.createTempfileProject();
        }
    }

    /**
     * Internal configuration lookup method.<p>
     *
     * @param cms the cms context
     * @param rootPath the root path for which to look up the configuration
     *
     * @return the configuration for the given path
     */
    protected CmsADEConfigData internalLookupConfiguration(CmsObject cms, String rootPath) {

        boolean online = (null == cms) || isOnline(cms);
        CmsADEConfigCacheState state = getCacheState(online);
        return state.lookupConfiguration(rootPath);
    }

    /**
     * Returns true if the project set in the CmsObject is the Online project.<p>
     *
     * @param cms the CMS context to check
     *
     * @return true if the project set in the CMS context is the Online project
     */
    private boolean isOnline(CmsObject cms) {

        return cms.getRequestContext().getCurrentProject().isOnlineProject();
    }

    /**
     * Saves an element list to the user additional infos.<p>
     *
     * @param cms the cms context
     * @param elementList the element list
     * @param listKey the list key
     *
     * @throws CmsException if something goes wrong
     */
    private void saveElementList(CmsObject cms, List<CmsContainerElementBean> elementList, String listKey)
    throws CmsException {

        // limit the favorite list size to avoid the additional info size limit
        if (elementList.size() > DEFAULT_ELEMENT_LIST_SIZE) {
            elementList = elementList.subList(0, DEFAULT_ELEMENT_LIST_SIZE);
        }

        JSONArray data = new JSONArray();

        Set<String> excludedSettings = new HashSet<String>();
        // do not store the template contexts, since dragging an element into the page which might be invisible
        // doesn't make sense
        excludedSettings.add(CmsTemplateContextInfo.SETTING);

        for (CmsContainerElementBean element : elementList) {
            data.put(elementToJson(element, excludedSettings));
        }
        CmsUser user = cms.getRequestContext().getCurrentUser();
        user.setAdditionalInfo(listKey, data.toString());
        cms.writeUser(user);
    }
}

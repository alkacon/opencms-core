/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.galleries;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsResourceTypeConfig;
import org.opencms.ade.galleries.preview.I_CmsPreviewProvider;
import org.opencms.ade.galleries.shared.CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchScope;
import org.opencms.ade.galleries.shared.CmsGalleryTabConfiguration;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.CmsSitemapEntryBean;
import org.opencms.ade.galleries.shared.CmsVfsEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.CmsVfsService;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavBuilder.Visibility;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsPermalinkResourceHandler;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchManager;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchIndex;
import org.opencms.search.galleries.CmsGallerySearchParameters;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.search.galleries.CmsGallerySearchResultList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsUriSplitter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsPreferences;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

import com.google.common.collect.Sets;

/**
 * Handles all RPC services related to the gallery dialog.<p>
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.galleries.CmsGalleryService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService
 * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync
 */
public class CmsGalleryService extends CmsGwtService implements I_CmsGalleryService {

    /**
     * Gallery info object.<p>
     */
    protected class CmsGalleryTypeInfo {

        /** The content types using this gallery. */
        private List<I_CmsResourceType> m_contentTypes;

        /** The gallery folder resources. */
        private List<CmsResource> m_galleries;

        /** The resource type of this gallery. */
        private I_CmsResourceType m_resourceType;

        /**
         * Constructor.<p>
         * 
         * @param resourceType the resource type of the gallery
         * @param contentType the resource type of the gallery content
         * @param galleries the gallery resources
         */
        protected CmsGalleryTypeInfo(
            I_CmsResourceType resourceType,
            I_CmsResourceType contentType,
            List<CmsResource> galleries) {

            m_resourceType = resourceType;
            m_contentTypes = new ArrayList<I_CmsResourceType>();
            m_contentTypes.add(contentType);
            m_galleries = galleries;
        }

        /**
         * Adds a type to the list of content types.<p>
         * 
         * @param type the type to add
         */
        protected void addContentType(I_CmsResourceType type) {

            m_contentTypes.add(type);
        }

        /**
         * Returns the contentTypes.<p>
         *
         * @return the contentTypes
         */
        protected List<I_CmsResourceType> getContentTypes() {

            return m_contentTypes;
        }

        /**
         * Returns the gallery folder resources.<p>
         *
         * @return the resources
         */
        protected List<CmsResource> getGalleries() {

            return m_galleries;
        }

        /**
         * Returns the resourceType.<p>
         *
         * @return the resourceType
         */
        protected I_CmsResourceType getResourceType() {

            return m_resourceType;
        }

        /**
         * Sets the contentTypes.<p>
         *
         * @param contentTypes the contentTypes to set
         */
        protected void setContentTypes(List<I_CmsResourceType> contentTypes) {

            m_contentTypes = contentTypes;
        }

        /**
         * Sets the galleries.<p>
         *
         * @param galleries the gallery resource list to set
         */
        protected void setGalleries(List<CmsResource> galleries) {

            m_galleries = galleries;
        }

        /**
         * Sets the resourceType.<p>
         *
         * @param resourceType the resourceType to set
         */
        protected void setResourceType(I_CmsResourceType resourceType) {

            m_resourceType = resourceType;
        }
    }

    /** The key used for storing the last used gallery in adeview mode. */
    public static final String KEY_LAST_USED_GALLERY_ADEVIEW = "__adeView";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGalleryService.class);

    /** Serialization uid. */
    private static final long serialVersionUID = 1673026761080584889L;

    /** The instance of the resource manager. */
    CmsResourceManager m_resourceManager;

    /** The workplace settings of the current user. */
    private CmsWorkplaceSettings m_workplaceSettings;

    /** The workplace locale from the current user's settings. */
    private Locale m_wpLocale;

    /**
     * Returns the initial gallery settings.<p>
     * 
     * @param request the current request
     * @param config the gallery configuration
     * 
     * @return the initial gallery settings 
     * 
     * @throws CmsRpcException if something goes wrong 
     */
    public static CmsGalleryDataBean getInitialSettings(HttpServletRequest request, CmsGalleryConfiguration config)
    throws CmsRpcException {

        CmsGalleryService srv = new CmsGalleryService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        CmsGalleryDataBean result = null;
        try {
            result = srv.getInitialSettings(config);
        } finally {
            srv.clearThreadStorage();
        }
        return result;
    }

    /**
     * Returns the initial search data.<p>
     * 
     * @param request the current request
     * @param config the gallery configuration
     * 
     * @return the search data 
     */
    public static CmsGallerySearchBean getSearch(HttpServletRequest request, CmsGalleryDataBean config) {

        CmsGalleryService srv = new CmsGalleryService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        CmsGallerySearchBean result = null;
        try {
            result = srv.getSearch(config);
        } finally {
            srv.clearThreadStorage();
        }
        return result;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#deleteResource(java.lang.String)
     */
    public void deleteResource(String resourcePath) throws CmsRpcException {

        try {
            ensureLock(resourcePath);
            getCmsObject().deleteResource(resourcePath, CmsResource.DELETE_PRESERVE_SIBLINGS);
        } catch (Exception e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getAdeViewModeConfiguration()
     */
    public CmsGalleryConfiguration getAdeViewModeConfiguration() {

        CmsGalleryConfiguration result = new CmsGalleryConfiguration();
        List<String> typeNames = new ArrayList<String>();

        for (I_CmsResourceType type : OpenCms.getResourceManager().getResourceTypes()) {
            Class<?> typeClass = type.getClass();
            if (CmsResourceTypeXmlContent.class.isAssignableFrom(typeClass)
                || CmsResourceTypeXmlPage.class.isAssignableFrom(typeClass)) {
                continue;
            }
            if (type.getGalleryTypes().size() > 0) {
                typeNames.add(type.getTypeName());
            }
        }
        result.setSearchTypes(typeNames);
        result.setResourceTypes(typeNames);
        result.setGalleryMode(GalleryMode.adeView);
        result.setTabConfiguration(CmsGalleryTabConfiguration.resolve("selectDoc"));
        return result;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getGalleries(java.util.List)
     */
    public List<CmsGalleryFolderBean> getGalleries(List<String> resourceTypes) {

        return buildGalleriesList(readGalleryInfosByTypeNames(resourceTypes));
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getInfoForResource(java.lang.String, java.lang.String)
     */
    public CmsResultItemBean getInfoForResource(String linkPath, String locale) throws CmsRpcException {

        CmsResultItemBean result = null;
        CmsObject cms = getCmsObject();
        CmsMessages messageBundle = Messages.get().getBundle(getWorkplaceLocale());
        try {
            if (new CmsUriSplitter(linkPath).getProtocol() != null) {
                result = new CmsResultItemBean();
                result.setTitle(messageBundle.key(Messages.GUI_EXTERNAL_LINK_0));
                result.setSubTitle("");
                result.setType(CmsResourceTypePointer.getStaticTypeName());
            } else {
                boolean notFound = false;
                String path = linkPath;
                String siteRoot = OpenCms.getSiteManager().getSiteRoot(linkPath);
                String oldSite = cms.getRequestContext().getSiteRoot();
                try {
                    //                    if ((siteRoot == null) && !cms.existsResource(path)) {
                    //                        // if no site root was found and the resource does not exist in the current site, assume the root site
                    //                        siteRoot = "/";
                    //                    }
                    if (siteRoot != null) {
                        // only switch the site if needed
                        cms.getRequestContext().setSiteRoot(siteRoot);
                        // remove the site root, because the link manager call will append it anyway
                        path = cms.getRequestContext().removeSiteRoot(linkPath);
                    }
                    // remove parameters, if not the link manager call might fail
                    int pos = path.indexOf(CmsRequestUtil.URL_DELIMITER);
                    int anchorPos = path.indexOf('#');
                    if ((pos == -1) || ((anchorPos > -1) && (pos > anchorPos))) {
                        pos = anchorPos;
                    }
                    if (pos > -1) {
                        path = path.substring(0, pos);
                    }
                    // get the root path
                    path = OpenCms.getLinkManager().getRootPath(cms, path);

                } catch (Exception e) {
                    notFound = true;
                } finally {
                    if (siteRoot != null) {
                        cms.getRequestContext().setSiteRoot(oldSite);
                    }
                }
                notFound = notFound || (path == null);
                boolean isInTimeRange = true;
                if (!notFound) {
                    CmsObject rootCms = OpenCms.initCmsObject(cms);
                    rootCms.getRequestContext().setSiteRoot("");
                    try {
                        CmsResource selectedResource = rootCms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
                        long currentTime = System.currentTimeMillis();
                        isInTimeRange = selectedResource.isReleasedAndNotExpired(currentTime);
                        if (selectedResource.isFolder()) {
                            result = new CmsResultItemBean();
                            CmsJspNavElement folderNav = new CmsJspNavBuilder(rootCms).getNavigationForResource(
                                selectedResource.getRootPath(),
                                CmsResourceFilter.IGNORE_EXPIRATION);
                            CmsResource defaultFileResource = null;
                            if (folderNav.isInNavigation() && !folderNav.isNavigationLevel()) {
                                try {
                                    defaultFileResource = rootCms.readDefaultFile(
                                        selectedResource,
                                        CmsResourceFilter.ONLY_VISIBLE);
                                } catch (Exception e) {
                                    log(e.getMessage(), e);
                                }
                            }
                            CmsResource resourceForType = defaultFileResource != null
                            ? defaultFileResource
                            : selectedResource;
                            result.setType(OpenCms.getResourceManager().getResourceType(resourceForType).getTypeName());

                            String title = folderNav.getProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT);
                            if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                                title = folderNav.getTitle();
                            } else if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
                                title = CmsResource.getName(path);
                                if (title.contains("/")) {
                                    title = title.substring(0, title.indexOf("/"));
                                }
                            }
                            result.setTitle(title);
                            try {
                                String userName = cms.readUser(selectedResource.getUserLastModified()).getFullName();
                                result.setUserLastModified(userName);
                            } catch (CmsException e) {
                                log(e.getMessage(), e);
                            }
                            Date date = new Date(selectedResource.getDateLastModified());
                            String formattedDate = CmsDateUtil.getDateTime(
                                date,
                                DateFormat.MEDIUM,
                                getWorkplaceLocale());
                            result.setDateLastModified(formattedDate);

                        } else {
                            CmsGallerySearchResult resultItem = null;
                            try {
                                resultItem = CmsGallerySearch.searchByPath(
                                    cms,
                                    path,
                                    CmsLocaleManager.getLocale(locale));
                            } catch (CmsVfsResourceNotFoundException ex) {
                                // ignore
                            }
                            notFound = resultItem == null;
                            if (!notFound) {
                                result = buildSingleSearchResultItem(getCmsObject(), resultItem, null);
                            }
                        }
                    } catch (CmsException ex) {
                        notFound = true;
                    }
                }
                if (notFound) {
                    result = new CmsResultItemBean();
                    result.setTitle(messageBundle.key(Messages.GUI_RESOURCE_NOT_FOUND_0));
                    result.setSubTitle("");
                    result.setType(CmsIconUtil.TYPE_RESOURCE_NOT_FOUND);
                } else if (!isInTimeRange && (result != null)) {
                    result.setType(CmsIconUtil.TYPE_RESOURCE_NOT_FOUND);
                    result.setTitle(messageBundle.key(Messages.GUI_RESOURCE_OUT_OF_TIME_RANGE_1, result.getTitle()));
                }
            }
        } catch (Throwable t) {
            error(t);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getInitialSettings(org.opencms.ade.galleries.shared.CmsGalleryConfiguration)
     */
    public CmsGalleryDataBean getInitialSettings(CmsGalleryConfiguration conf) throws CmsRpcException {

        try {
            return getInitialSettingsInternal(conf);
        } catch (Throwable t) {
            error(t);
            return null; // will never be reached 
        }
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getSearch(org.opencms.ade.galleries.shared.CmsGalleryDataBean)
     */
    public CmsGallerySearchBean getSearch(CmsGalleryDataBean data) {

        CmsGallerySearchBean result = null;
        // search within all available types
        List<String> types = getTypeNames(data);
        switch (data.getMode()) {
            case editor:
            case view:
            case adeView:
            case widget:
                String currentelement = data.getCurrentElement();
                try {
                    CmsSitemapEntryBean sitemapPreloadData = null;
                    CmsVfsEntryBean vfsPreloadData = null;
                    boolean disablePreview = false;
                    GalleryTabId startTab = null;
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(currentelement)) {
                        log("looking up:" + currentelement);
                        // removing the servlet context if present
                        if (currentelement.startsWith(OpenCms.getSystemInfo().getOpenCmsContext())) {
                            currentelement = currentelement.substring(OpenCms.getSystemInfo().getOpenCmsContext().length());
                            log("removed context - result: " + currentelement);
                        }
                        // get search results given resource path
                        result = findResourceInGallery(currentelement, data);
                        if (result != null) {
                            sitemapPreloadData = result.getSitemapPreloadData();
                            vfsPreloadData = result.getVfsPreloadData();
                        }
                        if ((sitemapPreloadData != null)
                            && data.getTabConfiguration().getTabs().contains(GalleryTabId.cms_tab_sitemap)) {
                            startTab = GalleryTabId.cms_tab_sitemap;
                            disablePreview = true;
                        }
                    } else {
                        CmsTreeOpenState vfsState = getVfsTreeState(data.getTreeToken());
                        if (vfsState != null) {
                            A_CmsTreeTabDataPreloader<CmsVfsEntryBean> vfsloader = new A_CmsTreeTabDataPreloader<CmsVfsEntryBean>() {

                                @Override
                                protected CmsVfsEntryBean createEntry(CmsObject cms, CmsResource resource)
                                throws CmsException {

                                    String title = cms.readPropertyObject(
                                        resource,
                                        CmsPropertyDefinition.PROPERTY_TITLE,
                                        false).getValue();
                                    return internalCreateVfsEntryBean(
                                        resource,
                                        title,
                                        true,
                                        isEditable(cms, resource),
                                        null);
                                }

                            };
                            vfsPreloadData = vfsloader.preloadData(
                                getCmsObject(),
                                readAll(vfsState.getOpenItems(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                            //   startTab = GalleryTabId.cms_tab_vfstree;
                        }
                        CmsTreeOpenState sitemapState = getSitemapTreeState(data.getTreeToken());
                        if (sitemapState != null) {
                            A_CmsTreeTabDataPreloader<CmsSitemapEntryBean> sitemaploader = new A_CmsTreeTabDataPreloader<CmsSitemapEntryBean>() {

                                @Override
                                protected CmsSitemapEntryBean createEntry(CmsObject cms, CmsResource resource)
                                throws CmsException {

                                    return internalCreateSitemapEntryBean(cms, resource);
                                }

                                /**
                                 * @see org.opencms.ade.galleries.A_CmsTreeTabDataPreloader#getChildren(org.opencms.file.CmsResource)
                                 */
                                @Override
                                protected List<CmsResource> getChildren(CmsResource resource) throws CmsException {

                                    return getSitemapSubEntryResources(resource.getRootPath());
                                }

                            };
                            sitemapPreloadData = sitemaploader.preloadData(
                                getCmsObject(),
                                readAll(sitemapState.getOpenItems(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));

                            //        if ((vfsState == null) || (vfsState.getTimestamp() < sitemapState.getTimestamp())) {
                            //              startTab = GalleryTabId.cms_tab_sitemap;
                            //       }
                        }
                    }
                    if ((result == null) || (result.getResults() == null) || result.getResults().isEmpty()) {
                        result = new CmsGallerySearchBean();
                        result.setGalleryMode(data.getMode());
                        result.setIgnoreSearchExclude(true);
                        String gallery = data.getStartGallery();
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(gallery)) {
                            List<String> galleries = new ArrayList<String>();
                            galleries.add(gallery);
                            result.setGalleries(galleries);
                        }
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(data.getStartFolder())) {
                            Set<String> folders = new HashSet<String>();
                            folders.add(data.getStartFolder());
                            result.setFolders(folders);
                        }
                        result.setTypes(types);
                        result.setLocale(data.getLocale());
                        CmsGallerySearchScope scope = data.getScope();
                        if (scope == null) {
                            scope = OpenCms.getWorkplaceManager().getGalleryDefaultScope();
                        }
                        result.setScope(scope);
                        result = search(result);
                    }
                    result.setSitemapPreloadData(sitemapPreloadData);
                    result.setVfsPreloadData(vfsPreloadData);
                    result.setInitialTabId(startTab);
                    result.setDisablePreview(disablePreview);
                    if (types.size() > 1) {
                        // only remove types parameter if there is more than one type available
                        result.setTypes(null);
                    }
                } catch (CmsException e) {
                    logError(e);
                    result = null;
                }
                break;
            case ade:
            default:
                break;
        }
        return result;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getSearch(CmsGallerySearchBean)
     */
    public CmsGallerySearchBean getSearch(CmsGallerySearchBean searchObj) throws CmsRpcException {

        CmsGallerySearchBean gSearchObj = null;

        try {
            gSearchObj = search(searchObj);
            getWorkplaceSettings().setLastSearchScope(searchObj.getScope());
            setLastOpenedGallery(gSearchObj);
        } catch (Throwable e) {
            error(e);
        }

        return gSearchObj;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getSubEntries(java.lang.String, boolean)
     */
    public List<CmsSitemapEntryBean> getSubEntries(String rootPath, boolean isRoot) throws CmsRpcException {

        try {
            return getSubEntriesInternal(rootPath, isRoot);
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getSubFolders(java.lang.String)
     */
    public List<CmsVfsEntryBean> getSubFolders(String rootPath) throws CmsRpcException {

        try {
            CmsObject cms = OpenCms.initCmsObject(getCmsObject());
            cms.getRequestContext().setSiteRoot("");
            List<CmsVfsEntryBean> result = new ArrayList<CmsVfsEntryBean>();
            if (cms.existsResource(rootPath, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                CmsResource resource = cms.readResource(rootPath, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                List<CmsResource> resources = cms.getSubFolders(
                    resource.getRootPath(),
                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                for (CmsResource res : resources) {
                    String title = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
                    result.add(internalCreateVfsEntryBean(res, title, false, isEditable(cms, res), null));
                }
            }
            return result;
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#loadVfsEntryBean(java.lang.String)
     */
    public CmsVfsEntryBean loadVfsEntryBean(String path) throws CmsRpcException {

        try {
            CmsObject cms = OpenCms.initCmsObject(getCmsObject());
            cms.getRequestContext().setSiteRoot("");
            if (!cms.existsResource(path, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                String startFolder = CmsStringUtil.joinPaths(
                    path,
                    getWorkplaceSettings().getUserSettings().getStartFolder());
                if (cms.existsResource(startFolder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                    path = startFolder;
                }
            }
            CmsResource optionRes = cms.readResource(path);
            CmsVfsEntryBean entryBean = internalCreateVfsEntryBean(
                optionRes,
                path,
                true,
                isEditable(cms, optionRes),
                null);
            return entryBean;

        } catch (Throwable e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#saveTreeOpenState(java.lang.String, java.lang.String, java.lang.String, java.util.Set)
     */
    public void saveTreeOpenState(String treeName, String treeToken, String siteRoot, Set<CmsUUID> openItems)
    throws CmsRpcException {

        try {
            HttpServletRequest request = getRequest();
            HttpSession session = request.getSession();
            String attributeName = getTreeOpenStateAttributeName(treeName, treeToken);
            if (openItems.isEmpty()) {
                CmsObject cms = OpenCms.initCmsObject(getCmsObject());
                cms.getRequestContext().setSiteRoot("");
                CmsResource resource = cms.readResource(siteRoot);
                openItems = Sets.newHashSet(resource.getStructureId());
            }
            CmsTreeOpenState treeState = new CmsTreeOpenState(treeName, siteRoot, openItems);
            session.setAttribute(attributeName, treeState);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#updateIndex()
     */
    public void updateIndex() throws CmsRpcException {

        try {
            OpenCms.getSearchManager().updateOfflineIndexes(2 * CmsSearchManager.DEFAULT_OFFLINE_UPDATE_FREQNENCY);
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * Gets the sitemap sub entries for a given path as resources.<p>
     * 
     * @param rootPath the root path 
     * @return the sitemap sub entry resources 
     * @throws CmsException if something goes wrong 
     */
    protected List<CmsResource> getSitemapSubEntryResources(String rootPath) throws CmsException {

        CmsObject rootCms = OpenCms.initCmsObject(getCmsObject());
        List<CmsResource> result = new ArrayList<CmsResource>();
        rootCms.getRequestContext().setSiteRoot("");
        CmsJspNavBuilder navBuilder = new CmsJspNavBuilder(rootCms);
        for (CmsJspNavElement navElement : navBuilder.getNavigationForFolder(
            rootPath,
            Visibility.all,
            CmsResourceFilter.ONLY_VISIBLE)) {
            if ((navElement != null) && navElement.isInNavigation()) {
                result.add(navElement.getResource());
            }
        }
        return result;
    }

    /**
     * Internal method for getting sitemap sub entries for a given root path.<p>
     * 
     * @param rootPath the root path 
     * @param isRoot true if this method is used to get the root entries of a sitemap 
     * @return the list of sitemap sub-entry beans 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected List<CmsSitemapEntryBean> getSubEntriesInternal(String rootPath, boolean isRoot) throws CmsException {

        CmsObject rootCms = OpenCms.initCmsObject(getCmsObject());
        rootCms.getRequestContext().setSiteRoot("");
        CmsJspNavBuilder navBuilder = new CmsJspNavBuilder(rootCms);
        List<CmsSitemapEntryBean> result = new ArrayList<CmsSitemapEntryBean>();
        for (CmsJspNavElement navElement : navBuilder.getNavigationForFolder(
            rootPath,
            Visibility.all,
            CmsResourceFilter.ONLY_VISIBLE)) {
            if ((navElement != null) && navElement.isInNavigation()) {
                CmsSitemapEntryBean nextEntry = prepareSitemapEntry(rootCms, navElement, false);

                result.add(nextEntry);
            }
        }
        if (isRoot) {
            CmsJspNavElement navElement = navBuilder.getNavigationForResource(rootPath, CmsResourceFilter.ONLY_VISIBLE);
            if (navElement == null) {
                return result;
            }
            CmsSitemapEntryBean root = prepareSitemapEntry(rootCms, navElement, isRoot);
            root.setChildren(result);
            return Collections.singletonList(root);
        }
        return result;
    }

    /** 
     * Gets the type names from the gallery data bean.<p>
     * 
     * @param data the gallery data bean 
     * @return the type names 
     */
    protected List<String> getTypeNames(CmsGalleryDataBean data) {

        List<String> types = new ArrayList<String>();
        for (CmsResourceTypeBean info : data.getTypes()) {
            types.add(info.getType());
        }
        return types;
    }

    /**
     * Checks whether a given resource is a sitemap entry.<p>
     * 
     * This is used for preselected entries in the gallery widget.<p>
     * 
     * 
     * @param cms the current CMS context   
     * @param resource the resource to check 
     * @return true if the resource is a sitemap entry 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected boolean isSitemapEntry(CmsObject cms, CmsResource resource) throws CmsException {

        CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED;
        List<CmsResource> ancestors = new ArrayList<CmsResource>();
        CmsResource currentResource = resource;
        String siteRoot = OpenCms.getSiteManager().getSiteRoot(resource.getRootPath());
        if (CmsStringUtil.isEmpty(siteRoot)) {
            return false;
        }
        while (true) {
            CmsResource parent = cms.readParentFolder(currentResource.getStructureId());
            if ((parent == null) || !cms.existsResource(parent.getStructureId(), filter)) {
                break;
            }
            ancestors.add(parent);
            if (CmsStringUtil.comparePaths(siteRoot, parent.getRootPath())) {
                break;
            }
            currentResource = parent;
        }
        Collections.reverse(ancestors);
        boolean first = true;
        for (CmsResource ancestor : ancestors) {
            if (first) {
                if (null == OpenCms.getSiteManager().getSiteRoot(ancestor.getRootPath())) {
                    return false;
                }
            } else {
                if (!hasNavigationProperty(cms, ancestor)) {
                    return false;
                }
            }
            first = false;
        }
        if (resource.isFile()) {
            if (ancestors.isEmpty()) {
                return false;
            }
            CmsResource defaultFile = cms.readDefaultFile(ancestors.get(ancestors.size() - 1), filter);
            if (!resource.equals(defaultFile)) {
                return false;
            }
        } else {
            if (!hasNavigationProperty(cms, resource)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads the resources for a collection of structure ids and returns the list of resources which could be read.<p>
     * 
     * @param structureIds the structure ids for which we want to read the resources 
     * @param filter the filter used to read the resource 
     * 
     * @return the list of resources for the given structure id  
     */
    protected List<CmsResource> readAll(Collection<CmsUUID> structureIds, CmsResourceFilter filter) {

        List<CmsResource> result = new ArrayList<CmsResource>();
        for (CmsUUID id : structureIds) {
            try {
                result.add(getCmsObject().readResource(id, filter));
            } catch (CmsException e) {
                LOG.info(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Gets the sitemap tree open state.<p>
     * 
     * @param treeToken the tree token to use 
     * 
     * @return the sitemap tree open state 
     */
    CmsTreeOpenState getSitemapTreeState(String treeToken) {

        return (CmsTreeOpenState)(getRequest().getSession().getAttribute(getTreeOpenStateAttributeName(
            I_CmsGalleryProviderConstants.TREE_SITEMAP,
            treeToken)));
    }

    /**
     * Gets the VFS tree open state.<p>
     * 
     * @param treeToken the tree token 
     * 
     * @return the VFS tree open state 
     */
    CmsTreeOpenState getVfsTreeState(String treeToken) {

        return (CmsTreeOpenState)(getRequest().getSession().getAttribute(getTreeOpenStateAttributeName(
            I_CmsGalleryProviderConstants.TREE_VFS,
            treeToken)));
    }

    /**
     * Creates the sitemap entry bean for a resource.<p>
     * 
     * @param cms the current CMS context 
     * @param resource the resource for which the sitemap entry bean should be created
     *  
     * @return the created sitemap entry bean 
     * 
     * @throws CmsException if something goes wrong 
     */
    CmsSitemapEntryBean internalCreateSitemapEntryBean(CmsObject cms, CmsResource resource) throws CmsException {

        cms = OpenCms.initCmsObject(cms);
        cms.getRequestContext().setSiteRoot("");
        CmsJspNavBuilder navBuilder = new CmsJspNavBuilder(cms);
        CmsJspNavElement entry = navBuilder.getNavigationForResource(resource.getRootPath());
        if (entry == null) {
            // may be null for expired resources 
            return null;
        }
        return prepareSitemapEntry(cms, entry, false);
    }

    /**
     * Creates the VFS entry bean for a resource.<p>
     *
     * @param resource the resource for which to create the VFS entry bean 
     * @param title the title 
     * @param isRoot true if this is a root entry 
     * @param isEditable true if this entry is editable 
     * @param children the children of the entry 
     * 
     * @return the created VFS entry bean 
     * @throws CmsException 
     */
    CmsVfsEntryBean internalCreateVfsEntryBean(
        CmsResource resource,
        String title,
        boolean isRoot,
        boolean isEditable,
        List<CmsVfsEntryBean> children) throws CmsException {

        String rootPath = resource.getRootPath();
        CmsUUID structureId = resource.getStructureId();
        CmsVfsEntryBean result = new CmsVfsEntryBean(rootPath, structureId, title, isRoot, isEditable, children);
        String siteRoot = null;
        if (resource.isFolder()) {
            CmsObject cms = OpenCms.initCmsObject(getCmsObject());
            cms.getRequestContext().setSiteRoot("");
            List<CmsResource> realChildren = cms.getResourcesInFolder(
                rootPath,
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            List<CmsResource> effectiveChildren = new ArrayList<CmsResource>();
            for (CmsResource realChild : realChildren) {
                if (realChild.isFolder()) {
                    effectiveChildren.add(realChild);
                }
            }
            if (effectiveChildren.isEmpty()) {
                result.setChildren(new ArrayList<CmsVfsEntryBean>());
            }
        }

        if (OpenCms.getSiteManager().startsWithShared(rootPath)) {
            siteRoot = OpenCms.getSiteManager().getSharedFolder();
        } else {
            String tempSiteRoot = OpenCms.getSiteManager().getSiteRoot(rootPath);
            if (tempSiteRoot != null) {
                siteRoot = tempSiteRoot;
            } else {
                siteRoot = "";
            }
        }
        result.setSiteRoot(siteRoot);
        return result;
    }

    /**
     * Checks if the current user has write permissions on the given resource.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource to check
     * 
     * @return <code>true</code> if the current user has write permissions on the given resource
     */
    boolean isEditable(CmsObject cms, CmsResource resource) {

        try {
            return cms.hasPermissions(resource, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            return false;
        }
    }

    /**
     * Adds galleries for a given type.<p>
     * 
     * @param galleryTypeInfos the gallery type infos 
     * @param typeName the type name 
     * 
     * @throws CmsLoaderException if something goes wrong 
     */
    private void addGalleriesForType(Map<String, CmsGalleryTypeInfo> galleryTypeInfos, String typeName)
    throws CmsLoaderException {

        I_CmsResourceType contentType = getResourceManager().getResourceType(typeName);
        for (I_CmsResourceType galleryType : contentType.getGalleryTypes()) {
            try {
                if (galleryTypeInfos.containsKey(galleryType.getTypeName())) {
                    CmsGalleryTypeInfo typeInfo = galleryTypeInfos.get(galleryType.getTypeName());
                    typeInfo.addContentType(contentType);
                } else {
                    CmsGalleryTypeInfo typeInfo;

                    typeInfo = new CmsGalleryTypeInfo(
                        galleryType,
                        contentType,
                        getGalleriesByType(galleryType.getTypeId()));

                    galleryTypeInfos.put(galleryType.getTypeName(), typeInfo);
                }
            } catch (CmsException e) {
                logError(e);
            }
        }
    }

    /**
     * Returns the map with the available galleries.<p>
     * 
     * The map uses gallery path as the key and stores the CmsGalleriesListInfoBean as the value.<p>
     * 
     * @param galleryTypes the galleries
     * 
     * @return the map with gallery info beans
     */
    private List<CmsGalleryFolderBean> buildGalleriesList(Map<String, CmsGalleryTypeInfo> galleryTypes) {

        List<CmsGalleryFolderBean> list = new ArrayList<CmsGalleryFolderBean>();
        if (galleryTypes == null) {
            return list;
        }
        Iterator<Entry<String, CmsGalleryTypeInfo>> iGalleryTypes = galleryTypes.entrySet().iterator();
        while (iGalleryTypes.hasNext()) {
            Entry<String, CmsGalleryTypeInfo> ent = iGalleryTypes.next();
            CmsGalleryTypeInfo tInfo = ent.getValue();
            ArrayList<String> contentTypes = new ArrayList<String>();
            Iterator<I_CmsResourceType> it = tInfo.getContentTypes().iterator();
            while (it.hasNext()) {
                contentTypes.add(String.valueOf(it.next().getTypeName()));
            }
            Iterator<CmsResource> ir = tInfo.getGalleries().iterator();
            while (ir.hasNext()) {
                CmsResource res = ir.next();
                CmsGalleryFolderBean bean = new CmsGalleryFolderBean();
                String sitePath = getCmsObject().getSitePath(res);
                String title = "";
                try {
                    // read the gallery title
                    title = getCmsObject().readPropertyObject(sitePath, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue(
                        "");
                } catch (CmsException e) {
                    // error reading title property
                    logError(e);
                }
                // sitepath as gallery id 
                bean.setPath(sitePath);
                // content types
                bean.setContentTypes(contentTypes);
                // title
                bean.setTitle(title);
                // gallery type name
                bean.setType(tInfo.getResourceType().getTypeName());
                bean.setEditable(isEditable(getCmsObject(), res));
                list.add(bean);
            }
        }
        return list;
    }

    /**
     * Returns a map with the available locales.<p>
     * 
     * The map entry key is the current locale and the value the localized nice name.<p>
     * 
     * @return the map representation of all available locales
     */
    private Map<String, String> buildLocalesMap() {

        TreeMap<String, String> localesMap = new TreeMap<String, String>();
        Iterator<Locale> it = OpenCms.getLocaleManager().getAvailableLocales().iterator();
        while (it.hasNext()) {
            Locale locale = it.next();
            localesMap.put(locale.toString(), locale.getDisplayName(getWorkplaceLocale()));
        }
        return localesMap;
    }

    /**
     * Returns the list of beans for the given search results.<p>
     * 
     * @param searchResult the list of search results
     * @param presetResult the search result which corresponds to a preset value in the editor 
     * 
     * @return the list with the current search results
     */
    private List<CmsResultItemBean> buildSearchResultList(
        List<CmsGallerySearchResult> searchResult,
        CmsGallerySearchResult presetResult) {

        ArrayList<CmsResultItemBean> list = new ArrayList<CmsResultItemBean>();
        if ((searchResult == null) || (searchResult.size() == 0)) {
            return list;
        }
        CmsObject cms = getCmsObject();
        for (CmsGallerySearchResult sResult : searchResult) {
            try {
                CmsResultItemBean bean = buildSingleSearchResultItem(cms, sResult, presetResult);
                list.add(bean);
            } catch (Exception e) {
                logError(e);
            }
        }
        return list;
    }

    /**
     * Builds a single search result list item for the client from a server-side search result.<p>
     * 
     * @param cms the current CMS context 
     * @param sResult the server-side search result 
     * @param presetResult the preselected result 
     * 
     * @return the client side search result item
     *  
     * @throws CmsLoaderException
     * @throws CmsException
     * @throws ParseException
     */
    private CmsResultItemBean buildSingleSearchResultItem(
        CmsObject cms,
        CmsGallerySearchResult sResult,
        CmsGallerySearchResult presetResult) throws CmsLoaderException, CmsException, ParseException {

        Locale wpLocale = getWorkplaceLocale();
        CmsResultItemBean bean = new CmsResultItemBean();
        if (sResult == presetResult) {
            bean.setPreset(true);
        }
        bean.setReleasedAndNotExpired(sResult.isReleaseAndNotExpired(cms));
        String path = sResult.getPath();
        path = cms.getRequestContext().removeSiteRoot(path);

        // resource path as id
        bean.setPath(path);
        // title
        bean.setTitle(CmsStringUtil.isEmptyOrWhitespaceOnly(sResult.getTitle())
        ? CmsResource.getName(sResult.getPath())
        : sResult.getTitle());
        // resource type
        bean.setType(sResult.getResourceType());
        // structured id
        bean.setClientId(sResult.getStructureId());
        CmsResource resultResource = cms.readResource(
            new CmsUUID(sResult.getStructureId()),
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        CmsVfsService.addLockInfo(cms, resultResource, bean);
        String permalink = CmsStringUtil.joinPaths(
            OpenCms.getSystemInfo().getOpenCmsContext(),
            CmsPermalinkResourceHandler.PERMALINK_HANDLER,
            sResult.getStructureId().toString());
        bean.setViewLink(permalink);

        // set nice resource type name as subtitle
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(sResult.getResourceType());
        String resourceTypeDisplayName = CmsWorkplaceMessages.getResourceTypeName(wpLocale, type.getTypeName());
        String description = sResult.getDescription();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(description)) {
            bean.setDescription(description);
            bean.addAdditionalInfo(
                Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_RESULT_LABEL_DESCRIPTION_0),
                description);
        } else {
            bean.setDescription(resourceTypeDisplayName);
        }
        bean.setUserLastModified(sResult.getUserLastModified());
        Date lastModDate = sResult.getDateLastModified();
        String formattedDate = lastModDate != null
        ? CmsDateUtil.getDateTime(lastModDate, DateFormat.MEDIUM, wpLocale)
        : "";
        bean.setDateLastModified(formattedDate);
        if (!type.getTypeName().equals(CmsResourceTypeImage.getStaticTypeName())) {
            bean.addAdditionalInfo(
                Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_RESULT_LABEL_RESOURCE_TYPE_0),
                resourceTypeDisplayName);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(sResult.getExcerpt())) {
            bean.addAdditionalInfo(
                Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_RESULT_LABEL_EXCERPT_0),
                sResult.getExcerpt(),
                CmsListInfoBean.CSS_CLASS_MULTI_LINE);
        }
        if (type instanceof CmsResourceTypeImage) {
            CmsProperty imageDimensionProp = cms.readPropertyObject(
                resultResource,
                CmsPropertyDefinition.PROPERTY_IMAGE_SIZE,
                false);
            if (!imageDimensionProp.isNullProperty()) {
                String dimensions = imageDimensionProp.getValue();
                dimensions = dimensions.substring(2).replace(",h:", " x ");
                bean.setDimension(dimensions);
                bean.addAdditionalInfo(
                    Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_RESULT_LABEL_DIMENSION_0),
                    dimensions);
            }
        }

        if (type instanceof CmsResourceTypeXmlContent) {
            CmsProperty elementModelProperty = cms.readPropertyObject(
                resultResource,
                CmsPropertyDefinition.PROPERTY_ELEMENT_MODEL,
                true);
            if (!elementModelProperty.isNullProperty()) {
                if (Boolean.valueOf(elementModelProperty.getValue()).booleanValue()) {
                    bean.setIsCopyModel(true);
                }
            }
        }

        bean.addAdditionalInfo(
            Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_RESULT_LABEL_SIZE_0),
            (sResult.getLength() / 1000) + " kb");
        if (lastModDate != null) {
            bean.addAdditionalInfo(
                Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_RESULT_LABEL_DATE_CHANGED_0),
                CmsDateUtil.getDate(lastModDate, DateFormat.SHORT, getWorkplaceLocale()));
        }
        if ((sResult.getDateExpired().getTime() != CmsResource.DATE_EXPIRED_DEFAULT)
            && !sResult.getDateExpired().equals(CmsSearchFieldMapping.getDefaultDateExpired())) {
            bean.addAdditionalInfo(
                Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_RESULT_LABEL_DATE_EXPIRED_0),
                CmsDateUtil.getDate(sResult.getDateExpired(), DateFormat.SHORT, getWorkplaceLocale()));
        }

        bean.setNoEditReson(new CmsResourceUtil(cms, resultResource).getNoEditReason(OpenCms.getWorkplaceManager().getWorkplaceLocale(
            cms)));
        return bean;
    }

    /**
     * Generates a map with all available content types.<p>
     * 
     * The map uses resource type name as the key and stores the CmsTypesListInfoBean as the value.
     * 
     * @param types the resource types
     * @param creatableTypes the creatable types 
     * 
     * @return the map containing the available resource types
     */
    private List<CmsResourceTypeBean> buildTypesList(List<I_CmsResourceType> types, List<String> creatableTypes) {

        ArrayList<CmsResourceTypeBean> list = new ArrayList<CmsResourceTypeBean>();
        if (types == null) {
            return list;
        }
        Map<I_CmsResourceType, I_CmsPreviewProvider> typeProviderMapping = getPreviewProviderForTypes(types);
        Iterator<I_CmsResourceType> it = types.iterator();
        while (it.hasNext()) {

            I_CmsResourceType type = it.next();
            try {
                CmsResourceTypeBean bean = createTypeBean(
                    type,
                    typeProviderMapping.get(type),
                    creatableTypes.contains(type.getTypeName()));
                list.add(bean);
            } catch (Exception e) {
                if (type != null) {
                    log(
                        Messages.get().getBundle(getWorkplaceLocale()).key(
                            Messages.ERR_BUILD_TYPE_LIST_1,
                            type.getTypeName()),
                        e);
                }
            }
        }
        return list;
    }

    /**
     * Creates a resource type bean for the given type.<p>
     * 
     * @param type the resource type
     * @param preview the preview provider 
     * @param creatable if the type may be created by the current user
     * 
     * @return the resource type bean
     */
    private CmsResourceTypeBean createTypeBean(I_CmsResourceType type, I_CmsPreviewProvider preview, boolean creatable) {

        CmsResourceTypeBean result = new CmsResourceTypeBean();

        result.setType(type.getTypeName());
        result.setTypeId(type.getTypeId());
        Locale wpLocale = getWorkplaceLocale();
        // type title and subtitle
        result.setTitle(CmsWorkplaceMessages.getResourceTypeName(wpLocale, type.getTypeName()));
        result.setDescription(CmsWorkplaceMessages.getResourceTypeDescription(wpLocale, type.getTypeName()));
        // gallery id of corresponding galleries
        ArrayList<String> galleryNames = new ArrayList<String>();
        Iterator<I_CmsResourceType> galleryTypes = type.getGalleryTypes().iterator();
        while (galleryTypes.hasNext()) {
            I_CmsResourceType galleryType = galleryTypes.next();
            galleryNames.add(galleryType.getTypeName());
        }
        result.setGalleryTypeNames(galleryNames);
        if (preview != null) {
            result.setPreviewProviderName(preview.getPreviewName());
        }
        result.setCreatableType(creatable);
        return result;
    }

    /**
     * Returns the search object containing the list with search results and the path to the specified resource.<p>
     * 
     * @param resourceName the given resource
     * @param data the gallery data bean 
     * 
     * @return the gallery search object containing the current search parameter and the search result list
     * 
     * @throws CmsException if the search fails 
     */
    private CmsGallerySearchBean findResourceInGallery(String resourceName, CmsGalleryDataBean data)
    throws CmsException {

        CmsResource resource = null;
        int pos = resourceName.indexOf("?");
        String resName = resourceName;
        String query = "";
        if (pos > -1) {
            query = resourceName.substring(pos);
            resName = resourceName.substring(0, pos);
        }
        String resNameWithoutServer = OpenCms.getLinkManager().getRootPath(getCmsObject(), resName);
        if (resNameWithoutServer != null) {
            resName = resNameWithoutServer;
        }
        CmsObject cms = getCmsObject();
        try {
            log("reading resource: " + resName);
            resource = cms.readResource(resName, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        } catch (CmsException e) {
            String originalSiteRoot = cms.getRequestContext().getSiteRoot();
            try {
                cms.getRequestContext().setSiteRoot("");
                resource = cms.readResource(resName, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
            } catch (CmsException e2) {
                logError(e);
                return null;
            } finally {
                cms.getRequestContext().setSiteRoot(originalSiteRoot);
            }
        }
        ArrayList<String> types = new ArrayList<String>();
        String resType = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        types.add(resType);
        CmsGallerySearchBean initialSearchObj = new CmsGallerySearchBean();
        initialSearchObj.setGalleryMode(data.getMode());
        initialSearchObj.setIgnoreSearchExclude(true);
        initialSearchObj.setTypes(types);
        ArrayList<String> galleries = new ArrayList<String>();
        for (CmsGalleryFolderBean gallery : data.getGalleries()) {
            String galleryPath = gallery.getPath();
            String galleryRootPath = cms.addSiteRoot(galleryPath);
            String folderPath = CmsResource.getFolderPath(resName);
            if (galleryPath.equals(folderPath) || galleryRootPath.equals(folderPath)) {
                galleries.add(gallery.getPath());
                initialSearchObj.setGalleries(galleries);
                break;
            }
        }
        if (galleries.isEmpty()) {
            ArrayList<String> vfsFolders = new ArrayList<String>();
            vfsFolders.add(CmsResource.getFolderPath(resource.getRootPath()));
            initialSearchObj.setFolders(new HashSet<String>(vfsFolders));
        }
        initialSearchObj.setLocale(data.getLocale());
        CmsGallerySearchBean searchObj = new CmsGallerySearchBean(initialSearchObj);
        searchObj.setSortOrder(CmsGallerySearchParameters.CmsGallerySortParam.DEFAULT.toString());
        int currentPage = 1;
        boolean found = false;
        searchObj.setPage(currentPage);
        CmsGallerySearchParameters params = prepareSearchParams(searchObj);
        org.opencms.search.galleries.CmsGallerySearch searchBean = new org.opencms.search.galleries.CmsGallerySearch();
        searchBean.init(getCmsObject());
        searchBean.setIndex(CmsGallerySearchIndex.GALLERY_INDEX_NAME);

        CmsGallerySearchResultList searchResults = null;
        CmsGallerySearchResultList totalResults = new CmsGallerySearchResultList();
        CmsGallerySearchResult foundItem = null;
        while (!found) {
            params.setResultPage(currentPage);
            searchResults = searchBean.getResult(params);
            Iterator<CmsGallerySearchResult> resultsIt = searchResults.listIterator();
            totalResults.append(searchResults);
            while (resultsIt.hasNext()) {
                CmsGallerySearchResult searchResult = resultsIt.next();
                if (searchResult.getPath().equals(resource.getRootPath())) {
                    found = true;
                    foundItem = searchResult;
                    break;
                }
            }
            if (!found && ((searchResults.getHitCount() / (currentPage * params.getMatchesPerPage())) >= 1)) {
                currentPage++;
            } else {
                break;
            }
        }
        boolean hasResults = searchResults != null;
        searchResults = totalResults;
        if (found && hasResults) {
            initialSearchObj.setSortOrder(params.getSortOrder().name());
            initialSearchObj.setResultCount(searchResults.getHitCount());
            initialSearchObj.setPage(params.getResultPage());
            initialSearchObj.setResults(buildSearchResultList(searchResults, foundItem));
            initialSearchObj.setPage(1);
            initialSearchObj.setLastPage(currentPage);
            initialSearchObj.setTabId(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_results.name());
            initialSearchObj.setResourcePath(resName + query);
            initialSearchObj.setResourceType(resType);

        } else {
            log("could not find selected resource");
        }
        if ((resource != null)) {
            if (isSitemapEntry(cms, resource)) {
                A_CmsTreeTabDataPreloader<CmsSitemapEntryBean> loader = new A_CmsTreeTabDataPreloader<CmsSitemapEntryBean>() {

                    @Override
                    protected CmsSitemapEntryBean createEntry(CmsObject innerCms, CmsResource innerResource)
                    throws CmsException {

                        return internalCreateSitemapEntryBean(innerCms, innerResource);
                    }

                    /**
                     * @see org.opencms.ade.galleries.A_CmsTreeTabDataPreloader#getChildren(org.opencms.file.CmsResource)
                     */
                    @Override
                    protected List<CmsResource> getChildren(CmsResource parent) throws CmsException {

                        return getSitemapSubEntryResources(parent.getRootPath());
                    }
                };
                CmsSitemapEntryBean entryBean = loader.preloadData(cms, Collections.singletonList(resource));
                initialSearchObj.setSitemapPreloadData(entryBean);
            } else if (resource.isFolder()) {
                A_CmsTreeTabDataPreloader<CmsVfsEntryBean> loader = new A_CmsTreeTabDataPreloader<CmsVfsEntryBean>() {

                    @Override
                    protected CmsVfsEntryBean createEntry(CmsObject innerCms, CmsResource innerResource)
                    throws CmsException {

                        String title = innerCms.readPropertyObject(
                            innerResource,
                            CmsPropertyDefinition.PROPERTY_TITLE,
                            false).getValue();
                        return internalCreateVfsEntryBean(
                            innerResource,
                            title,
                            true,
                            isEditable(innerCms, innerResource),
                            null);
                    }
                };
                CmsVfsEntryBean entryBean = loader.preloadData(cms, Collections.singletonList(resource));
                initialSearchObj.setVfsPreloadData(entryBean);
            }
        }

        return initialSearchObj;
    }

    /**
     * Generates a list of available galleries for the given gallery-type.<p>
     * 
     * @param galleryTypeId the gallery-type
     * 
     * @return the list of galleries
     * 
     * @throws CmsException if something goes wrong
     */
    private List<CmsResource> getGalleriesByType(int galleryTypeId) throws CmsException {

        List<CmsResource> galleries = new ArrayList<CmsResource>();
        galleries = getCmsObject().readResources(
            "/",
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId));

        String siteRoot = getCmsObject().getRequestContext().getSiteRoot();
        // if the current site is NOT the root site - add all other galleries from the system path
        if (!siteRoot.equals("")) {
            List<CmsResource> systemGalleries = null;
            // get the galleries in the /system/ folder
            systemGalleries = getCmsObject().readResources(
                CmsWorkplace.VFS_PATH_SYSTEM,
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId));
            if (systemGalleries != null) {
                // add the found system galleries to the result
                galleries.addAll(systemGalleries);
            }
        }

        if (!OpenCms.getSiteManager().isSharedFolder(siteRoot)) {
            String shared = OpenCms.getSiteManager().getSharedFolder();
            List<CmsResource> sharedGalleries = getCmsObject().readResources(
                shared,
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId));
            if (sharedGalleries != null) {
                galleries.addAll(sharedGalleries);
            }
        }
        return galleries;
    }

    /**
     * Helper method for getting the initial gallery settings.<p>
     * 
     * @param conf the gallery configration 
     * @return the gallery settings 
     * 
     * @throws CmsRpcException 
     */
    private CmsGalleryDataBean getInitialSettingsInternal(CmsGalleryConfiguration conf) throws CmsRpcException {

        CmsGalleryDataBean data = new CmsGalleryDataBean();
        data.setMode(conf.getGalleryMode());
        data.setLocales(buildLocalesMap());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(conf.getLocale())) {
            data.setLocale(conf.getLocale());
        } else {
            data.setLocale(getCmsObject().getRequestContext().getLocale().toString());
        }
        data.setVfsRootFolders(getRootEntries());
        data.setScope(getWorkplaceSettings().getLastSearchScope());

        List<CmsResourceTypeBean> types = null;
        data.setTabIds(conf.getGalleryMode().getTabs());
        switch (conf.getGalleryMode()) {
            case editor:
            case view:
            case adeView:
            case widget:
                if (conf.getTabIds() != null) {
                    data.setTabIds(conf.getTabIds());
                }
                data.setCurrentElement(conf.getCurrentElement());
                String referencePath = conf.getReferencePath();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(referencePath)) {
                    referencePath = conf.getGalleryPath();
                }
                data.setReferenceSitePath(referencePath);
                types = getResourceTypeBeans(
                    conf.getGalleryMode(),
                    data.getReferenceSitePath(),
                    conf.getResourceTypes());
                data.setTypes(types);
                Map<String, CmsGalleryTypeInfo> galleryTypeInfos = readGalleryInfosByTypeBeans(types);
                // in case the 'gallerytypes' parameter is set, allow only the given galleries
                if (conf.getGalleryTypes() != null) {
                    Map<String, CmsGalleryTypeInfo> infos = new HashMap<String, CmsGalleryTypeInfo>();
                    for (int i = 0; i < conf.getGalleryTypes().length; i++) {
                        CmsGalleryTypeInfo typeInfo = galleryTypeInfos.get(conf.getGalleryTypes()[i]);
                        if (typeInfo != null) {
                            infos.put(conf.getGalleryTypes()[i], typeInfo);
                        }
                    }
                    galleryTypeInfos = infos;
                }
                data.setGalleries(buildGalleriesList(galleryTypeInfos));
                String startGallery = conf.getGalleryPath();
                // check if the configured gallery path really is an existing gallery
                boolean galleryAvailable = false;
                for (CmsGalleryFolderBean folderBean : data.getGalleries()) {
                    if (folderBean.getPath().equals(startGallery)) {
                        galleryAvailable = true;
                        break;
                    }
                }
                data.setStartGallery(galleryAvailable ? startGallery : null);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(conf.getStartFolder())) {
                    try {
                        CmsObject cloneCms = OpenCms.initCmsObject(getCmsObject());
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(conf.getStartSite())) {
                            cloneCms.getRequestContext().setSiteRoot(conf.getStartSite());
                        }
                        if (cloneCms.existsResource(conf.getStartFolder(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                            data.setStartFolder(cloneCms.getRequestContext().addSiteRoot(conf.getStartFolder()));
                        }
                    } catch (CmsException e) {
                        log(e.getMessage(), e);
                    }
                }

                if (CmsStringUtil.isEmptyOrWhitespaceOnly(data.getStartGallery()) && !data.getGalleries().isEmpty()) {
                    startGallery = null;
                    if (!data.getGalleries().isEmpty()) {
                        // check the user preferences for any configured start gallery
                        String galleryTypeName = data.getGalleries().get(0).getType();
                        startGallery = getWorkplaceSettings().getUserSettings().getStartGallery(
                            galleryTypeName,
                            getCmsObject());
                        if (CmsPreferences.INPUT_DEFAULT.equals(startGallery)) {
                            startGallery = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartGallery(
                                galleryTypeName);
                        }
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(startGallery)) {
                            startGallery = getCmsObject().getRequestContext().removeSiteRoot(startGallery);
                        }
                    }
                    // check if the gallery is available in this site and still exists
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(startGallery)
                        && getCmsObject().existsResource(startGallery)) {
                        data.setStartGallery(startGallery);
                    } else {
                        // don't select any gallery
                        data.setStartGallery(null);
                    }
                }

                GalleryTabId defaultTab = conf.getTabConfiguration().getDefaultTab();
                data.setTabConfiguration(conf.getTabConfiguration());
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(data.getStartGallery())
                    && CmsStringUtil.isEmptyOrWhitespaceOnly(data.getCurrentElement())
                    && CmsStringUtil.isEmptyOrWhitespaceOnly(data.getStartFolder())) {
                    data.setStartTab(defaultTab);
                } else {
                    data.setStartTab(GalleryTabId.cms_tab_results);
                }
                break;
            case ade:
                data.setReferenceSitePath(getCmsObject().getRequestContext().getUri());
                types = getResourceTypeBeans(
                    conf.getGalleryMode(),
                    data.getReferenceSitePath(),
                    conf.getResourceTypes());
                data.setTypes(types);
                Map<String, CmsGalleryTypeInfo> adeGalleryTypeInfos = readGalleryInfosByTypeBeans(types);
                data.setGalleries(buildGalleriesList(adeGalleryTypeInfos));
                data.setStartTab(GalleryTabId.cms_tab_types);
                break;
            default:
                break;
        }

        CmsSiteSelectorOptionBuilder optionBuilder = new CmsSiteSelectorOptionBuilder(getCmsObject());
        optionBuilder.addNormalSites(true, getWorkplaceSettings().getUserSettings().getStartFolder());
        optionBuilder.addSharedSite();
        data.setVfsSiteSelectorOptions(optionBuilder.getOptions());

        CmsSiteSelectorOptionBuilder sitemapOptionBuilder = new CmsSiteSelectorOptionBuilder(getCmsObject());
        sitemapOptionBuilder.addNormalSites(false, null);
        if (data.getReferenceSitePath() != null) {
            sitemapOptionBuilder.addCurrentSubsite(getCmsObject().addSiteRoot(data.getReferenceSitePath()));
        }
        data.setSitemapSiteSelectorOptions(sitemapOptionBuilder.getOptions());
        data.setDefaultScope(OpenCms.getWorkplaceManager().getGalleryDefaultScope());
        return data;
    }

    /**
     * Reads the preview provider configuration and generates needed type-provider mappings.<p>
     * 
     * @param types the resource types 
     * 
     * @return a map from resource types to preview providers 
     */
    private Map<I_CmsResourceType, I_CmsPreviewProvider> getPreviewProviderForTypes(List<I_CmsResourceType> types) {

        Map<String, I_CmsPreviewProvider> previewProviderMap = new HashMap<String, I_CmsPreviewProvider>();
        Map<I_CmsResourceType, I_CmsPreviewProvider> typeProviderMapping = new HashMap<I_CmsResourceType, I_CmsPreviewProvider>();
        for (I_CmsResourceType type : types) {
            String providerClass = type.getGalleryPreviewProvider();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(providerClass)) {
                providerClass = providerClass.trim();
                try {
                    if (previewProviderMap.containsKey(providerClass)) {
                        typeProviderMapping.put(type, previewProviderMap.get(providerClass));
                    } else {
                        I_CmsPreviewProvider previewProvider = (I_CmsPreviewProvider)Class.forName(providerClass).newInstance();
                        previewProviderMap.put(providerClass, previewProvider);
                        typeProviderMapping.put(type, previewProvider);
                    }
                } catch (Exception e) {
                    logError(new CmsException(Messages.get().container(
                        Messages.ERR_INSTANCING_PREVIEW_PROVIDER_2,
                        providerClass,
                        type.getTypeName()), e));
                }
            }
        }
        return typeProviderMapping;
    }

    /**
     * Returns the resourceManager.<p>
     *
     * @return the resourceManager
     */
    private CmsResourceManager getResourceManager() {

        if (m_resourceManager == null) {
            m_resourceManager = OpenCms.getResourceManager();
        }
        return m_resourceManager;
    }

    /**
     * Returns the resource types configured to be used within the given gallery mode.<p>
     * 
     * @param galleryMode the gallery mode
     * @param referenceSitePath the reference site-path to check permissions for
     * @param resourceTypesList the resource types parameter
     * 
     * @return the resource types
     * 
     * @throws CmsRpcException if something goes wrong reading the configuration
     */
    private List<CmsResourceTypeBean> getResourceTypeBeans(
        GalleryMode galleryMode,
        String referenceSitePath,
        List<String> resourceTypesList) throws CmsRpcException {

        List<I_CmsResourceType> resourceTypes = null;
        List<String> creatableTypes = null;
        switch (galleryMode) {
            case editor:
            case view:
            case adeView:
            case widget:
                resourceTypes = readResourceTypesFromRequest(resourceTypesList);
                creatableTypes = Collections.<String> emptyList();
                break;
            case ade:
                resourceTypes = new ArrayList<I_CmsResourceType>();
                creatableTypes = new ArrayList<String>();
                try {
                    CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(
                        getCmsObject(),
                        getCmsObject().getRequestContext().addSiteRoot(getCmsObject().getRequestContext().getUri()));
                    for (CmsResourceTypeConfig typeConfig : config.getResourceTypes()) {
                        if (typeConfig.isAddDisabled()) {
                            continue;
                        }
                        if (typeConfig.checkViewable(getCmsObject(), referenceSitePath)) {
                            String typeName = typeConfig.getTypeName();
                            resourceTypes.add(getResourceManager().getResourceType(typeName));
                        }
                    }
                    for (CmsResourceTypeConfig typeConfig : config.getCreatableTypes(getCmsObject())) {
                        if (typeConfig.isAddDisabled()) {
                            continue;
                        }
                        String typeName = typeConfig.getTypeName();
                        creatableTypes.add(typeName);
                    }
                } catch (CmsException e) {
                    error(e);
                }
                break;
            default:
                resourceTypes = Collections.<I_CmsResourceType> emptyList();
                creatableTypes = Collections.<String> emptyList();
        }
        return buildTypesList(resourceTypes, creatableTypes);
    }

    /**
     * Returns the VFS root entries.<p>
     * 
     * @return the VFS root entries
     * 
     * @throws CmsRpcException if something goes wrong
     */
    private List<CmsVfsEntryBean> getRootEntries() throws CmsRpcException {

        List<CmsVfsEntryBean> rootFolders = new ArrayList<CmsVfsEntryBean>();
        CmsObject cms = getCmsObject();
        try {
            String path = "/";
            if (!cms.existsResource(path, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                String startFolder = getWorkplaceSettings().getUserSettings().getStartFolder();
                if (cms.existsResource(startFolder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                    path = startFolder;
                } else {
                    path = null;
                }
            }
            if (path != null) {
                CmsResource rootFolderResource = getCmsObject().readResource(
                    path,
                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                String title = cms.readPropertyObject(path, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
                rootFolders.add(internalCreateVfsEntryBean(
                    rootFolderResource,
                    title,
                    true,
                    isEditable(getCmsObject(), rootFolderResource),
                    null));
            }

        } catch (CmsException e) {
            error(e);
        }
        return rootFolders;
    }

    /**
     * Gets the attribute name for a tree open state.<p>
     * 
     * @param treeName the tree name 
     * @param treeToken the tree token
     *  
     * @return the attribute name for the tree 
     */
    private String getTreeOpenStateAttributeName(String treeName, String treeToken) {

        return "tree_" + treeName + "_" + treeToken;
    }

    /**
     * Returns the workplace locale from the current user's settings.<p>
     * 
     * @return the workplace locale
     */
    private Locale getWorkplaceLocale() {

        if (m_wpLocale == null) {
            m_wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject());
        }
        return m_wpLocale;
    }

    /**
     * Returns the workplace settings of the current user.<p>
     * 
     * @return the workplace settings
     */
    private CmsWorkplaceSettings getWorkplaceSettings() {

        if (m_workplaceSettings == null) {
            m_workplaceSettings = (CmsWorkplaceSettings)getRequest().getSession().getAttribute(
                CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
            // ensure workplace settings attribute is set
            if (m_workplaceSettings == null) {
                // creating any instance of {@link org.opencms.workplace.CmsWorkplaceSettings} and store it
                m_workplaceSettings = CmsWorkplace.initWorkplaceSettings(getCmsObject(), null, false);
                getRequest().getSession().setAttribute(
                    CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS,
                    m_workplaceSettings);
            }
        }
        return m_workplaceSettings;
    }

    /**
     * Checks whether a resource has a navigation property.<p>
     * 
     * @param cms the current CMS context 
     * @param resource the resource to use 
     * 
     * @return true if the resource has a navigation property
     * 
     * @throws CmsException if something goes wrong 
     */
    private boolean hasNavigationProperty(CmsObject cms, CmsResource resource) throws CmsException {

        List<CmsProperty> props = cms.readPropertyObjects(resource, false);
        Map<String, String> propMap = CmsProperty.toMap(props);
        return propMap.containsKey(CmsPropertyDefinition.PROPERTY_NAVPOS)
            || propMap.containsKey(CmsPropertyDefinition.PROPERTY_NAVTEXT);
    }

    /**
     * Returns the search parameters for the given query data.<p>
     * 
     * @param searchData the query data
     * 
     * @return the prepared search parameters
     */
    private CmsGallerySearchParameters prepareSearchParams(CmsGallerySearchBean searchData) {

        // create a new search parameter object
        CmsGallerySearchParameters params = new CmsGallerySearchParameters();
        CmsObject cms = getCmsObject();

        // set the selected types to the parameters
        if (searchData.getTypes() != null) {
            params.setResourceTypes(searchData.getTypes());
        }

        // set the selected galleries to the parameters 
        if (searchData.getGalleries() != null) {
            List<String> paramGalleries = new ArrayList<String>();
            for (String gallerySitePath : searchData.getGalleries()) {
                paramGalleries.add(cms.getRequestContext().addSiteRoot(gallerySitePath));
            }
            params.setGalleries(paramGalleries);
        }

        // set the sort order for the galleries to the parameters
        CmsGallerySearchParameters.CmsGallerySortParam sortOrder;
        String temp = searchData.getSortOrder();
        try {
            sortOrder = CmsGallerySearchParameters.CmsGallerySortParam.valueOf(temp);
        } catch (Exception e) {
            sortOrder = CmsGallerySearchParameters.CmsGallerySortParam.DEFAULT;
        }
        params.setSortOrder(sortOrder);
        if (searchData.getScope() == null) {
            params.setScope(OpenCms.getWorkplaceManager().getGalleryDefaultScope());
        } else {
            params.setScope(searchData.getScope());
        }
        params.setReferencePath(searchData.getReferencePath());

        // set the selected folders to the parameters
        params.setFolders(new ArrayList<String>(searchData.getFolders()));

        // set the categories to the parameters
        if (searchData.getCategories() != null) {
            params.setCategories(searchData.getCategories());
        }

        // set the search query to the parameters
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(searchData.getQuery())) {
            params.setSearchWords(searchData.getQuery());
        }

        // set the result page to the parameters
        int page = searchData.getPage();
        params.setResultPage(page);

        // set the locale to the parameters
        String locale = searchData.getLocale();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(locale)) {
            locale = getCmsObject().getRequestContext().getLocale().toString();
        }
        params.setSearchLocale(locale);

        // set the matches per page to the parameters
        params.setMatchesPerPage(searchData.getMatchesPerPage());

        // get the date range input
        long dateCreatedStart = searchData.getDateCreatedStart();
        long dateCreatedEnd = searchData.getDateCreatedEnd();
        long dateModifiedStart = searchData.getDateModifiedStart();
        long dateModifiedEnd = searchData.getDateModifiedEnd();

        // set the date created range to the parameters
        if ((dateCreatedStart != -1L) && (dateCreatedEnd != -1L)) {
            params.setDateCreatedTimeRange(dateCreatedStart, dateCreatedEnd);
        } else if (dateCreatedStart != -1L) {
            params.setDateCreatedTimeRange(dateCreatedStart, Long.MAX_VALUE);
        } else if (dateCreatedEnd != -1L) {
            params.setDateCreatedTimeRange(Long.MIN_VALUE, dateCreatedEnd);
        }

        // set the date modified range to the parameters
        if ((dateModifiedStart != -1L) && (dateModifiedEnd != -1L)) {
            params.setDateLastModifiedTimeRange(dateModifiedStart, dateModifiedEnd);
        } else if (dateModifiedStart != -1L) {
            params.setDateLastModifiedTimeRange(dateModifiedStart, Long.MAX_VALUE);
        } else if (dateModifiedEnd != -1L) {
            params.setDateLastModifiedTimeRange(Long.MIN_VALUE, dateModifiedEnd);
        }
        params.setIgnoreSearchExclude(searchData.isIgnoreSearchExclude());
        return params;
    }

    /**
     * Prepares a sitemap entry bean from the given navigation element.<p>
     * 
     * @param cms the cms context
     * @param navElement the navigation element
     * @param isRoot <code>true</code> if this is a site root entry
     * 
     * @return the sitemap entry
     * 
     * @throws CmsException if something goes wrong reading types and resources
     */
    private CmsSitemapEntryBean prepareSitemapEntry(CmsObject cms, CmsJspNavElement navElement, boolean isRoot)
    throws CmsException {

        CmsResource ownResource = navElement.getResource();
        CmsResource defaultFileResource = null;
        CmsJspNavBuilder navBuilder = new CmsJspNavBuilder(cms);
        if (ownResource.isFolder() && !navElement.isNavigationLevel()) {
            defaultFileResource = cms.readDefaultFile(ownResource, CmsResourceFilter.ONLY_VISIBLE);
        }
        String type;
        if (defaultFileResource != null) {
            type = OpenCms.getResourceManager().getResourceType(defaultFileResource.getTypeId()).getTypeName();
        } else {
            type = OpenCms.getResourceManager().getResourceType(ownResource.getTypeId()).getTypeName();
        }
        boolean isNavLevel = ownResource.isFolder() && navElement.isNavigationLevel();
        // make sure not to show ??? NavText ???
        String title = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(navElement.getProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT))) {
            title = navElement.getProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT);
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(navElement.getProperty(CmsPropertyDefinition.PROPERTY_TITLE))) {
            title = navElement.getProperty(CmsPropertyDefinition.PROPERTY_TITLE);
        } else {
            title = navElement.getFileName();
            if (title.contains("/")) {
                title = title.substring(0, title.indexOf("/"));
            }
        }
        String childPath = navElement.getResource().getRootPath();
        boolean noChildren = true;

        List<CmsJspNavElement> childNav = navBuilder.getNavigationForFolder(
            childPath,
            Visibility.all,
            CmsResourceFilter.ONLY_VISIBLE);
        for (CmsJspNavElement childNavEntry : childNav) {
            if (childNavEntry.isInNavigation()) {
                noChildren = false;
                break;
            }
        }

        CmsSitemapEntryBean result = new CmsSitemapEntryBean(
            navElement.getResource().getRootPath(),
            navElement.getResourceName(),
            ownResource.getStructureId(),
            title,
            type,
            ownResource.isFolder(),
            isRoot,
            navElement.isHiddenNavigationEntry(),
            isNavLevel);
        result.setSiteRoot(OpenCms.getSiteManager().getSiteRoot(ownResource.getRootPath()));
        if (noChildren) {
            result.setChildren(new ArrayList<CmsSitemapEntryBean>());
        }
        return result;
    }

    /**
     * Returns a map with gallery type names associated with the list of available galleries for this type.<p>
     * 
     * @param resourceTypes the resources types to collect the galleries for 
     * 
     * @return a map with gallery type and  the associated galleries
     */
    private Map<String, CmsGalleryTypeInfo> readGalleryInfosByTypeBeans(List<CmsResourceTypeBean> resourceTypes) {

        Map<String, CmsGalleryTypeInfo> galleryTypeInfos = new HashMap<String, CmsGalleryTypeInfo>();
        for (CmsResourceTypeBean typeBean : resourceTypes) {
            try {
                addGalleriesForType(galleryTypeInfos, typeBean.getType());
            } catch (CmsLoaderException e1) {
                logError(e1);
            }
        }
        return galleryTypeInfos;
    }

    /**
     * Returns a map with gallery type names associated with the list of available galleries for this type.<p>
     * 
     * @param resourceTypes the resources types to collect the galleries for 
     * 
     * @return a map with gallery type and  the associated galleries
     */
    private Map<String, CmsGalleryTypeInfo> readGalleryInfosByTypeNames(List<String> resourceTypes) {

        Map<String, CmsGalleryTypeInfo> galleryTypeInfos = new HashMap<String, CmsGalleryTypeInfo>();
        for (String typeName : resourceTypes) {
            try {
                addGalleriesForType(galleryTypeInfos, typeName);
            } catch (CmsLoaderException e1) {
                logError(e1);
            }
        }
        return galleryTypeInfos;
    }

    /**
     * Returns a list of resource types by a request parameter.<p>
     * 
     * @param resourceTypes the resource types parameter
     * 
     * @return the resource types
     */
    private List<I_CmsResourceType> readResourceTypesFromRequest(List<String> resourceTypes) {

        List<I_CmsResourceType> result = new ArrayList<I_CmsResourceType>();
        if (resourceTypes != null) {
            for (String type : resourceTypes) {
                try {
                    result.add(getResourceManager().getResourceType(type.trim()));
                } catch (Exception e) {
                    logError(e);
                }
            }
        }
        if (result.size() == 0) {
            result = getResourceManager().getResourceTypes();
        }
        return result;
    }

    /**
     * Returns the gallery search object containing the results for the current parameter.<p>
     * 
     * @param searchObj the current search object 
     * 
     * @return the search result
     * 
     * @throws CmsException if the search fails 
     */
    private CmsGallerySearchBean search(CmsGallerySearchBean searchObj) throws CmsException {

        CmsGallerySearchBean searchObjBean = new CmsGallerySearchBean(searchObj);
        if (searchObj == null) {
            return searchObjBean;
        }
        // search
        CmsGallerySearchParameters params = prepareSearchParams(searchObj);
        org.opencms.search.galleries.CmsGallerySearch searchBean = new org.opencms.search.galleries.CmsGallerySearch();
        if (searchObj.isIncludeExpired()) {
            CmsObject searchCms = OpenCms.initCmsObject(getCmsObject());
            searchCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
            searchBean.init(searchCms);
        } else {
            searchBean.init(getCmsObject());
        }
        searchBean.setIndex(CmsGallerySearchIndex.GALLERY_INDEX_NAME);
        CmsGallerySearchResultList searchResults = searchBean.getResult(params);
        // set only the result dependent search params for this search
        // the user dependent params(galleries, types etc.) remain unchanged
        searchObjBean.setSortOrder(params.getSortOrder().name());
        searchObjBean.setScope(params.getScope());
        searchObjBean.setResultCount(searchResults.getHitCount());
        searchObjBean.setPage(params.getResultPage());
        searchObjBean.setLastPage(params.getResultPage());
        searchObjBean.setResults(buildSearchResultList(searchResults, null));

        return searchObjBean;
    }

    /**
     * Sets the last opened gallery information for the current user.<p>
     * 
     * @param searchObject the current search
     */
    private void setLastOpenedGallery(CmsGallerySearchBean searchObject) {

        if ((searchObject.getGalleries() != null) && (searchObject.getGalleries().size() == 1)) {
            String galleryPath = searchObject.getGalleries().get(0);
            CmsWorkplaceSettings settings = getWorkplaceSettings();
            if (searchObject.getGalleryMode() == GalleryMode.adeView) {
                settings.setLastUsedGallery(KEY_LAST_USED_GALLERY_ADEVIEW, galleryPath);
            } else {
                for (String typeName : searchObject.getTypes()) {
                    try {
                        settings.setLastUsedGallery(""
                            + OpenCms.getResourceManager().getResourceType(typeName).getTypeId(), galleryPath);
                    } catch (CmsLoaderException e) {
                        this.log(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }
}

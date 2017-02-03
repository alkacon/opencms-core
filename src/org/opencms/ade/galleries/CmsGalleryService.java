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

package org.opencms.ade.galleries;

import org.opencms.ade.galleries.CmsGalleryFilteredNavTreeBuilder.NavigationNode;
import org.opencms.ade.galleries.preview.I_CmsPreviewProvider;
import org.opencms.ade.galleries.shared.CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchScope;
import org.opencms.ade.galleries.shared.CmsGalleryTabConfiguration;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean.TypeVisibility;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.CmsSitemapEntryBean;
import org.opencms.ade.galleries.shared.CmsVfsEntryBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsCoreService;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.CmsVfsService;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavBuilder.Visibility;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsPermalinkResourceHandler;
import org.opencms.main.CmsStaticResourceHandler;
import org.opencms.main.OpenCms;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.galleries.CmsGallerySearch;
import org.opencms.search.galleries.CmsGallerySearchParameters;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.search.galleries.CmsGallerySearchResultList;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsUriSplitter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.containerpage.CmsADESessionCache;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
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

    /** Bean used to store a single type together with a flag indicating its visibility. */
    class TypeWithVisibility {

        /** True if the type should only be shown in the full list. */
        private boolean m_onlyShowInFullList;

        /** The resource type. */
        private I_CmsResourceType m_type;

        /**
         * Creates a new instance.<p>
         *
         * @param type the resource type
         * @param onlyShowInFullList the flag to control the visibility
         */
        public TypeWithVisibility(I_CmsResourceType type, boolean onlyShowInFullList) {

            m_type = type;
            m_onlyShowInFullList = onlyShowInFullList;
        }

        /**
         * Returns the type.<p>
         *
         * @return the type
         */
        public I_CmsResourceType getType() {

            return m_type;
        }

        /**
         * Returns the onlyShowInFullList.<p>
         *
         * @return the onlyShowInFullList
         */
        public boolean isOnlyShowInFullList() {

            return m_onlyShowInFullList;
        }

    }

    /** Key for additional info gallery folder filter. */
    public static final String FOLDER_FILTER_ADD_INFO_KEY = "gallery_folder_filter";

    /** Limit to the number results loaded on initial search. */
    public static final int INITIAL_SEARCH_MAX_RESULTS = 200;

    /** The key used for storing the last used gallery in adeview mode. */
    public static final String KEY_LAST_USED_GALLERY_ADEVIEW = "__adeView";

    /** Name for the 'galleryShowInvalidDefault' preference. */
    public static final String PREF_GALLERY_SHOW_INVALID_DEFAULT = "galleryShowInvalidDefault";

    /** Key for additional info gallery result view type. */
    public static final String RESULT_VIEW_TYPE_ADD_INFO_KEY = "gallery_result_view_type";

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
     * Generates the pre-loaded contents for the VFS tab of the gallery dialog.<p>
     *
     * @param cms the current CMS context
     * @param vfsState the saved VFS tree state (may be null)
     * @param folders the saved search folders (may be null)
     *
     * @return the root tree entry for the VFS tab
     */
    public static CmsVfsEntryBean generateVfsPreloadData(
        final CmsObject cms,
        final CmsTreeOpenState vfsState,
        final Set<String> folders) {

        CmsVfsEntryBean vfsPreloadData = null;

        A_CmsTreeTabDataPreloader<CmsVfsEntryBean> vfsloader = new A_CmsTreeTabDataPreloader<CmsVfsEntryBean>() {

            @SuppressWarnings("synthetic-access")
            @Override
            protected CmsVfsEntryBean createEntry(CmsObject innerCms, CmsResource resource) throws CmsException {

                String title = innerCms.readPropertyObject(
                    resource,
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    false).getValue();
                boolean isEditable = false;
                try {
                    isEditable = innerCms.hasPermissions(
                        resource,
                        CmsPermissionSet.ACCESS_WRITE,
                        false,
                        CmsResourceFilter.ALL);
                } catch (CmsException e) {
                    LOG.info(e.getLocalizedMessage(), e);
                }

                return internalCreateVfsEntryBean(innerCms, resource, title, true, isEditable, null, false);
            }

        };
        Set<CmsResource> treeOpenResources = Sets.newHashSet();
        if (vfsState != null) {

            for (CmsUUID structureId : vfsState.getOpenItems()) {
                try {
                    treeOpenResources.add(cms.readResource(structureId, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
        }
        CmsObject rootCms = null;
        Set<CmsResource> folderSetResources = Sets.newHashSet();
        try {
            rootCms = OpenCms.initCmsObject(cms);
            rootCms.getRequestContext().setSiteRoot("");
            if (!((folders == null) || folders.isEmpty())) {
                for (String folder : folders) {
                    try {
                        folderSetResources.add(rootCms.readResource(folder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED));
                    } catch (CmsException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }
            }
        } catch (CmsException e1) {
            LOG.error(e1.getLocalizedMessage(), e1);
        }
        try {
            vfsPreloadData = vfsloader.preloadData(cms, treeOpenResources, folderSetResources);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        return vfsPreloadData;

    }

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
     * Gets the attribute name for a tree open state.<p>
     *
     * @param treeName the tree name
     * @param treeToken the tree token
     *
     * @return the attribute name for the tree
     */
    public static String getTreeOpenStateAttributeName(String treeName, String treeToken) {

        return "tree_" + treeName + "_" + treeToken;
    }

    /**
     * Convenience method for reading the saved VFS tree state from the session.<p>
     *
     * @param request the current request
     * @param treeToken the tree token (may be null)
     *
     * @return the saved tree open state (may be null)
     */
    public static CmsTreeOpenState getVfsTreeState(HttpServletRequest request, String treeToken) {

        return (CmsTreeOpenState)request.getSession().getAttribute(
            getTreeOpenStateAttributeName(I_CmsGalleryProviderConstants.TREE_VFS, treeToken));

    }

    /**
     * Creates the VFS entry bean for a resource.<p>
     *
     * @param cms the CMS context to use
     * @param resource the resource for which to create the VFS entry bean
     * @param title the title
     * @param isRoot true if this is a root entry
     * @param isEditable true if this entry is editable
     * @param children the children of the entry
     * @param isMatch true if the VFS entry bean is a match for the quick search
     *
     * @return the created VFS entry bean
     * @throws CmsException if something goes wrong
     */
    public static CmsVfsEntryBean internalCreateVfsEntryBean(
        CmsObject cms,
        CmsResource resource,
        String title,
        boolean isRoot,
        boolean isEditable,
        List<CmsVfsEntryBean> children,
        boolean isMatch)
    throws CmsException {

        String rootPath = resource.getRootPath();
        CmsUUID structureId = resource.getStructureId();
        CmsVfsEntryBean result = new CmsVfsEntryBean(
            rootPath,
            structureId,
            title,
            isRoot,
            isEditable,
            children,
            isMatch);
        String siteRoot = null;
        if (resource.isFolder()) {
            cms = OpenCms.initCmsObject(cms);
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
     * Returns the resource types beans.<p>
     *
     * @param resourceTypes the resource types
     * @param creatableTypes the creatable types
     * @param deactivatedTypes the deactivated types
     * @param typesForTypeTab the types which should be shown in the types tab according to the gallery configuration
     *
     * @return the resource types
     */
    public List<CmsResourceTypeBean> buildTypesList(
        List<I_CmsResourceType> resourceTypes,
        Set<String> creatableTypes,
        Set<String> deactivatedTypes,
        final List<String> typesForTypeTab) {

        List<CmsResourceTypeBean> result = buildTypesList(resourceTypes, creatableTypes);

        for (CmsResourceTypeBean typeBean : result) {
            if ((typesForTypeTab != null) && (typesForTypeTab.size() > 0)) {
                if (!typesForTypeTab.contains(typeBean.getType())) {
                    if (typeBean.getVisibility() != TypeVisibility.hidden) {
                        typeBean.setVisibility(TypeVisibility.showOptional);
                    }
                }
            }
            typeBean.setDeactivated(deactivatedTypes.contains(typeBean.getType()));
        }
        if ((typesForTypeTab != null) && (typesForTypeTab.size() > 0)) {
            Collections.sort(result, new Comparator<CmsResourceTypeBean>() {

                public int compare(CmsResourceTypeBean first, CmsResourceTypeBean second) {

                    return ComparisonChain.start().compare(searchTypeRank(first), searchTypeRank(second)).compare(
                        first.getType(),
                        second.getType()).result();
                }

                int searchTypeRank(CmsResourceTypeBean type) {

                    int index = typesForTypeTab.indexOf(type.getType());
                    if (index == -1) {
                        return Integer.MAX_VALUE;
                    } else {
                        return index;
                    }
                }
            });

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
        result.setGalleryStoragePrefix("" + GalleryMode.adeView);
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
            } else if (CmsStaticResourceHandler.isStaticResourceUri(linkPath)) {
                result = new CmsResultItemBean();
                result.setTitle(messageBundle.key(Messages.GUI_STATIC_RESOURCE_0));
                result.setSubTitle(CmsStaticResourceHandler.removeStaticResourcePrefix(linkPath));
                result.setType(CmsResourceTypeBinary.getStaticTypeName());
            } else {
                boolean notFound = false;
                String path = linkPath;
                String siteRoot = OpenCms.getSiteManager().getSiteRoot(linkPath);
                String oldSite = cms.getRequestContext().getSiteRoot();
                try {
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
     * Returns the initial gallery data for the container page editor.<p>
     *
     * @param types the available resource types
     * @param uri the page URI
     * @param locale the content locale
     *
     * @return the gallery data
     */
    public CmsGalleryDataBean getInitialSettingsForContainerPage(
        List<CmsResourceTypeBean> types,
        String uri,
        String locale) {

        CmsGalleryDataBean data = null;
        try {
            data = new CmsGalleryDataBean();
            boolean galleryShowInvalidDefault = Boolean.parseBoolean(
                getWorkplaceSettings().getUserSettings().getAdditionalPreference(
                    PREF_GALLERY_SHOW_INVALID_DEFAULT,
                    true));
            data.setIncludeExpiredDefault(galleryShowInvalidDefault);
            data.setResultViewType(readResultViewType());
            data.setMode(GalleryMode.ade);
            data.setGalleryStoragePrefix("");
            data.setLocales(buildLocalesMap());
            data.setLocale(locale);

            data.setVfsRootFolders(getRootEntries());

            data.setScope(getWorkplaceSettings().getLastSearchScope());
            data.setSortOrder(getWorkplaceSettings().getLastGalleryResultOrder());

            data.setTabIds(GalleryMode.ade.getTabs());
            data.setReferenceSitePath(uri);
            data.setTypes(types);
            Map<String, CmsGalleryTypeInfo> adeGalleryTypeInfos = readGalleryInfosByTypeBeans(types);
            data.setGalleries(buildGalleriesList(adeGalleryTypeInfos));
            data.setStartTab(GalleryTabId.cms_tab_types);
            Set<String> folderFilter = readFolderFilters();
            data.setStartFolderFilter(folderFilter);
            if ((folderFilter != null) && !folderFilter.isEmpty()) {
                try {
                    data.setVfsPreloadData(generateVfsPreloadData(getCmsObject(), null, folderFilter));
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
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
            CmsCoreService service = new CmsCoreService();
            service.setCms(getCmsObject());
            data.setCategories(service.getCategoriesForSitePath(data.getReferenceSitePath()));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return data;
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
                        currentelement = CmsLinkManager.removeOpenCmsContext(currentelement);
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
                            // in the case
                            sitemapPreloadData = sitemaploader.preloadData(
                                getCmsObject(),
                                Sets.newHashSet(
                                    readAll(sitemapState.getOpenItems(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)),
                                null);
                        }
                    }
                    if ((result == null) || (result.getResults() == null) || result.getResults().isEmpty()) {
                        result = new CmsGallerySearchBean();
                        result.setOriginalGalleryData(data);
                        result.setGalleryMode(data.getMode());
                        result.setGalleryStoragePrefix(data.getGalleryStoragePrefix());
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
                        } else if (data.getStartFolderFilter() != null) {
                            result.setFolders(data.getStartFolderFilter());
                        }
                        result.setTypes(types);
                        result.setLocale(data.getLocale());
                        CmsGallerySearchScope scope = data.getScope();
                        if (scope == null) {
                            scope = OpenCms.getWorkplaceManager().getGalleryDefaultScope();
                        }
                        result.setSortOrder(data.getSortOrder().name());
                        result.setScope(scope);
                        result.setIncludeExpired(data.getIncludeExpiredDefault());
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
            getWorkplaceSettings().setLastGalleryResultOrder(SortParams.valueOf(searchObj.getSortOrder()));
            setLastOpenedGallery(searchObj);
        } catch (Throwable e) {
            error(e);
        }

        return gSearchObj;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getSubEntries(java.lang.String, boolean, java.lang.String)
     */
    public List<CmsSitemapEntryBean> getSubEntries(String rootPath, boolean isRoot, String filter)
    throws CmsRpcException {

        try {
            return getSubEntriesInternal(rootPath, isRoot, filter);
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
                    result.add(
                        internalCreateVfsEntryBean(
                            getCmsObject(),
                            res,
                            title,
                            false,
                            isEditable(cms, res),
                            null,
                            false));
                }
            }
            return result;
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#loadVfsEntryBean(java.lang.String, java.lang.String)
     */
    public CmsVfsEntryBean loadVfsEntryBean(String path, String filter) throws CmsRpcException {

        try {
            if (CmsStringUtil.isEmpty(filter)) {

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
                String title = cms.readPropertyObject(
                    optionRes,
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    false).getValue();
                if (CmsStringUtil.isEmpty(title)) {
                    title = path;
                }
                CmsVfsEntryBean entryBean = internalCreateVfsEntryBean(
                    getCmsObject(),
                    optionRes,
                    title,
                    true,
                    isEditable(cms, optionRes),
                    null,
                    false);
                return entryBean;
            } else {
                filter = filter.toLowerCase();
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
                List<CmsResource> folders = cms.readResources(
                    optionRes.getRootPath(),
                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireFolder());
                folders.add(optionRes);
                Set<CmsResource> folderSet = Sets.newHashSet(folders);
                List<CmsResource> titleResources = cms.readResourcesWithProperty(
                    path,
                    CmsPropertyDefinition.PROPERTY_TITLE);
                titleResources.retainAll(folderSet);
                Set<CmsResource> filterMatches = Sets.newHashSet();
                for (CmsResource folder : folderSet) {
                    if (folder.getName().toLowerCase().contains(filter)) {
                        filterMatches.add(folder);
                        titleResources.remove(folder); // we don't need to check the title if the name already matched
                    }
                }
                for (CmsResource titleRes : titleResources) {
                    CmsProperty prop = cms.readPropertyObject(titleRes, CmsPropertyDefinition.PROPERTY_TITLE, false);
                    String title = prop.getValue();
                    if ((title != null) && title.toLowerCase().contains(filter)) {
                        filterMatches.add(titleRes);
                    }
                }
                Set<String> filterMatchAncestorPaths = Sets.newHashSet();
                if (filterMatches.size() > 0) {
                    for (CmsResource filterMatch : filterMatches) {
                        String currentPath = filterMatch.getRootPath();
                        while (currentPath != null) {
                            filterMatchAncestorPaths.add(currentPath);
                            currentPath = CmsResource.getParentFolder(currentPath);
                        }
                    }
                    Set<String> allPaths = Sets.newHashSet();
                    Set<String> parentPaths = Sets.newHashSet();
                    for (CmsResource folder : folderSet) {
                        allPaths.add(folder.getRootPath());
                        String parent = CmsResource.getParentFolder(folder.getRootPath());
                        if (parent != null) {
                            parentPaths.add(parent);
                        }
                    }
                    parentPaths.retainAll(allPaths);

                    Set<CmsResource> filterMatchAncestors = Sets.newHashSet();
                    for (CmsResource folderRes : folderSet) {
                        if (filterMatchAncestorPaths.contains(folderRes.getRootPath())) {
                            filterMatchAncestors.add(folderRes);
                        }
                    }
                    Map<String, CmsResource> resourcesByPath = Maps.newHashMap();
                    for (CmsResource treeRes : filterMatchAncestors) {
                        resourcesByPath.put(treeRes.getRootPath(), treeRes);
                    }
                    Multimap<CmsResource, CmsResource> childMap = ArrayListMultimap.create();
                    for (CmsResource res : filterMatchAncestors) {
                        CmsResource parent = resourcesByPath.get(CmsResource.getParentFolder(res.getRootPath()));
                        if (parent != null) {
                            childMap.put(parent, res);
                        }
                    }
                    return buildVfsEntryBeanForQuickSearch(optionRes, childMap, filterMatches, parentPaths, true);
                } else {
                    return null;
                }
            }
        } catch (Throwable e) {
            error(e);
            return null;
        }
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#saveResultViewType(java.lang.String)
     */
    public void saveResultViewType(String resultViewType) {

        CmsUser user = getCmsObject().getRequestContext().getCurrentUser();
        user.setAdditionalInfo(RESULT_VIEW_TYPE_ADD_INFO_KEY, resultViewType);
        try {
            getCmsObject().writeUser(user);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
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
            OpenCms.getSearchManager().updateOfflineIndexes();
        } catch (Throwable e) {
            error(e);
        }
    }

    /**
     * Gets an initialized CmsObject to be used for the actual search for a given search bean.<p>
     *
     * @param searchObj the search object
     * @return the initialized CmsObject
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsObject getSearchCms(CmsGallerySearchBean searchObj) throws CmsException {

        CmsObject searchCms = getCmsObject();
        if (searchObj.isIncludeExpired()) {
            searchCms = OpenCms.initCmsObject(getCmsObject());
            searchCms.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
        }
        return searchCms;
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
     * @param filter the filter string (only relevant if isRoot is true)
     * @return the list of sitemap sub-entry beans
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsSitemapEntryBean> getSubEntriesInternal(String rootPath, boolean isRoot, String filter)
    throws CmsException {

        CmsObject rootCms = OpenCms.initCmsObject(getCmsObject());
        rootCms.getRequestContext().setSiteRoot("");
        CmsJspNavBuilder navBuilder = new CmsJspNavBuilder(rootCms);
        if (isRoot) {
            if (CmsStringUtil.isEmpty(filter)) {
                List<CmsSitemapEntryBean> result = new ArrayList<CmsSitemapEntryBean>();
                for (CmsJspNavElement navElement : navBuilder.getNavigationForFolder(
                    rootPath,
                    Visibility.all,
                    CmsResourceFilter.ONLY_VISIBLE)) {
                    if ((navElement != null) && navElement.isInNavigation()) {
                        CmsSitemapEntryBean nextEntry = prepareSitemapEntry(rootCms, navElement, false, true);
                        result.add(nextEntry);
                    }
                }
                CmsJspNavElement navElement = navBuilder.getNavigationForResource(
                    rootPath,
                    CmsResourceFilter.ONLY_VISIBLE);
                if (navElement == null) {
                    return result;
                }
                CmsSitemapEntryBean root = prepareSitemapEntry(rootCms, navElement, isRoot, true);
                root.setChildren(result);
                return Collections.singletonList(root);
            } else {
                CmsGalleryFilteredNavTreeBuilder navTreeBuilder = new CmsGalleryFilteredNavTreeBuilder(
                    rootCms,
                    rootPath);
                navTreeBuilder.initTree(filter);
                if (navTreeBuilder.hasMatches()) {
                    return Lists.newArrayList(convertNavigationTreeToBean(rootCms, navTreeBuilder.getRoot(), true));
                } else {
                    return Lists.newArrayList();
                }
            }
        } else {
            List<CmsSitemapEntryBean> result = new ArrayList<CmsSitemapEntryBean>();
            for (CmsJspNavElement navElement : navBuilder.getNavigationForFolder(
                rootPath,
                Visibility.all,
                CmsResourceFilter.ONLY_VISIBLE)) {
                if ((navElement != null) && navElement.isInNavigation()) {
                    CmsSitemapEntryBean nextEntry = prepareSitemapEntry(rootCms, navElement, false, true);
                    result.add(nextEntry);
                }
            }
            return result;
        }
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

        return (CmsTreeOpenState)(getRequest().getSession().getAttribute(
            getTreeOpenStateAttributeName(I_CmsGalleryProviderConstants.TREE_SITEMAP, treeToken)));
    }

    /**
     * Gets the VFS tree open state.<p>
     *
     * @param treeToken the tree token
     *
     * @return the VFS tree open state
     */
    CmsTreeOpenState getVfsTreeState(String treeToken) {

        return (CmsTreeOpenState)(getRequest().getSession().getAttribute(
            getTreeOpenStateAttributeName(I_CmsGalleryProviderConstants.TREE_VFS, treeToken)));
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
        return prepareSitemapEntry(cms, entry, false, true);
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
    @SuppressWarnings("deprecation")
    private void addGalleriesForType(Map<String, CmsGalleryTypeInfo> galleryTypeInfos, String typeName)
    throws CmsLoaderException {

        I_CmsResourceType contentType = getResourceManager().getResourceType(typeName);
        for (I_CmsResourceType galleryType : contentType.getGalleryTypes()) {

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
                    title = getCmsObject().readPropertyObject(
                        sitePath,
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        false).getValue("");
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
     * @throws CmsException if something goes wrong
     * @throws ParseException if date parsing fails
     */
    private CmsResultItemBean buildSingleSearchResultItem(
        CmsObject cms,
        CmsGallerySearchResult sResult,
        CmsGallerySearchResult presetResult)
    throws CmsException, ParseException {

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
        String rawTitle = CmsStringUtil.isEmptyOrWhitespaceOnly(sResult.getTitle())
        ? CmsResource.getName(sResult.getPath())
        : sResult.getTitle();
        bean.setTitle(rawTitle);
        bean.setRawTitle(rawTitle);
        // resource type
        bean.setType(sResult.getResourceType());
        CmsResource resultResource = cms.readResource(
            new CmsUUID(sResult.getStructureId()),
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
        bean.setDetailResourceType(CmsResourceIcon.getDefaultFileOrDetailType(cms, resultResource));
        // structured id
        bean.setClientId(sResult.getStructureId());

        CmsVfsService.addLockInfo(cms, resultResource, bean);

        String permalinkId = sResult.getStructureId().toString();
        String permalink = CmsStringUtil.joinPaths(
            OpenCms.getSystemInfo().getOpenCmsContext(),
            CmsPermalinkResourceHandler.PERMALINK_HANDLER,
            permalinkId);

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
            CmsProperty copyrightProp = cms.readPropertyObject(
                resultResource,
                CmsPropertyDefinition.PROPERTY_COPYRIGHT,
                false);
            if (!copyrightProp.isNullProperty()) {
                bean.addAdditionalInfo(
                    Messages.get().getBundle(getWorkplaceLocale()).key(Messages.GUI_RESULT_LABEL_COPYRIGHT_0),
                    copyrightProp.getValue());
            }
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

        if (CmsResourceTypeXmlContainerPage.isModelReuseGroup(cms, resultResource)) {
            bean.setPseudoType(CmsGwtConstants.TYPE_MODELGROUP_REUSE);
        }

        bean.setResourceState(resultResource.getState());
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

        bean.setNoEditReson(
            new CmsResourceUtil(cms, resultResource).getNoEditReason(
                OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)));
        bean.setMarkChangedState(true);
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
    private List<CmsResourceTypeBean> buildTypesList(List<I_CmsResourceType> types, Set<String> creatableTypes) {

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
     * Recursively builds the VFS entry bean for the quick filtering function in the folder tab.<p<
     *
     * @param resource the resource
     * @param childMap map from parent to child resources
     * @param filterMatches the resources matching the filter
     * @param parentPaths root paths of resources which are not leaves
     * @param isRoot true if this the root node
     *
     * @return the VFS entry bean for the client
     *
     * @throws CmsException if something goes wrong
     */
    private CmsVfsEntryBean buildVfsEntryBeanForQuickSearch(
        CmsResource resource,
        Multimap<CmsResource, CmsResource> childMap,
        Set<CmsResource> filterMatches,
        Set<String> parentPaths,
        boolean isRoot)
    throws CmsException {

        CmsObject cms = getCmsObject();
        String title = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
        boolean isMatch = filterMatches.contains(resource);
        List<CmsVfsEntryBean> childBeans = Lists.newArrayList();

        Collection<CmsResource> children = childMap.get(resource);
        if (!children.isEmpty()) {
            for (CmsResource child : children) {
                CmsVfsEntryBean childBean = buildVfsEntryBeanForQuickSearch(
                    child,
                    childMap,
                    filterMatches,
                    parentPaths,
                    false);
                childBeans.add(childBean);
            }
        } else if (filterMatches.contains(resource)) {
            if (parentPaths.contains(resource.getRootPath())) {
                childBeans = null;
            }
            // otherwise childBeans remains an empty list
        }

        String rootPath = resource.getRootPath();
        CmsVfsEntryBean result = new CmsVfsEntryBean(
            rootPath,
            resource.getStructureId(),
            title,
            isRoot,
            isEditable(cms, resource),
            childBeans,
            isMatch);
        String siteRoot = null;
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
     * Helper method to construct a sitemap entry bean for the sitemap tab filter functionality.<p>
     *
     * @param cms the CMS context
     * @param node the root node of the filtered tree
     * @param isRoot true if this is the root node
     *
     * @return the sitemap entry bean
     */
    private CmsSitemapEntryBean convertNavigationTreeToBean(CmsObject cms, NavigationNode node, boolean isRoot) {

        CmsSitemapEntryBean bean = null;
        try {
            bean = prepareSitemapEntry(cms, node.getNavElement(), isRoot, false);
            bean.setSearchMatch(node.isMatch());
            List<NavigationNode> children = node.getChildren();
            List<CmsSitemapEntryBean> childBeans = Lists.newArrayList();
            if (children.size() > 0) {
                for (NavigationNode child : children) {
                    childBeans.add(convertNavigationTreeToBean(cms, child, false));
                }
            } else if (node.isLeaf()) {
                childBeans = Lists.newArrayList();
            } else {
                // no children in filter result, but can still load children by clicking on tree item
                childBeans = null;
            }
            bean.setChildren(childBeans);

        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return bean;
    }

    /**
     * Returns a list of resource types by a request parameter.<p>
     *
     * @param resourceTypes the resource types parameter
     *
     * @return the resource types
     */
    private List<I_CmsResourceType> convertTypeNamesToTypes(List<String> resourceTypes) {

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

        return result;
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
    @SuppressWarnings("deprecation")
    private CmsResourceTypeBean createTypeBean(
        I_CmsResourceType type,
        I_CmsPreviewProvider preview,
        boolean creatable) {

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
        if (type.isFolder()) {
            result.setVisibility(TypeVisibility.hidden);
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
        boolean typeOk = true;
        if ((data.getTypes() != null) && !data.getTypes().isEmpty()) {
            typeOk = false;
            for (CmsResourceTypeBean type : data.getTypes()) {
                if (OpenCms.getResourceManager().matchResourceType(type.getType(), resource.getTypeId())) {
                    typeOk = true;
                }
            }
        }
        if (!typeOk) {
            LOG.debug(
                "Selected resource " + resource.getRootPath() + " has invalid type for configured gallery widget.");
            return null;
        }
        ArrayList<String> types = new ArrayList<String>();
        String resType = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        types.add(resType);
        Set<String> folders = null;
        if (data.getStartFolderFilter() != null) {
            for (String folder : data.getStartFolderFilter()) {
                if (resource.getRootPath().startsWith(folder)) {
                    folders = data.getStartFolderFilter();
                    break;
                }
            }
        }
        CmsGallerySearchBean initialSearchObj = new CmsGallerySearchBean();
        initialSearchObj.setGalleryMode(data.getMode());
        initialSearchObj.setGalleryStoragePrefix(data.getGalleryStoragePrefix());
        initialSearchObj.setIncludeExpired(data.getIncludeExpiredDefault());
        initialSearchObj.setIgnoreSearchExclude(true);
        initialSearchObj.setTypes(types);
        if (folders != null) {
            initialSearchObj.setFolders(folders);
        }
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
        searchBean.init(getSearchCms(searchObj));
        searchBean.setIndex(CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);
        CmsGallerySearchResultList searchResults = null;
        CmsGallerySearchResultList totalResults = new CmsGallerySearchResultList();
        CmsGallerySearchResult foundItem = null;
        while (!found) {
            params.setResultPage(currentPage);
            searchResults = searchBean.getResult(params);
            totalResults.append(searchResults);
            Iterator<CmsGallerySearchResult> resultsIt = searchResults.listIterator();
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
            if (searchResults.getHitCount() > INITIAL_SEARCH_MAX_RESULTS) {
                // in case the hit count is too large, don't continue the search to avoid slow load times
                break;
            }
        }
        boolean hasResults = searchResults != null;
        searchResults = totalResults;
        if (hasResults) {
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
                CmsSitemapEntryBean entryBean = loader.preloadData(
                    cms,
                    Sets.newHashSet(Collections.singletonList(resource)),
                    null);
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
                            getCmsObject(),
                            innerResource,
                            title,
                            true,
                            isEditable(innerCms, innerResource),
                            null,
                            false);
                    }
                };
                CmsVfsEntryBean entryBean = loader.preloadData(
                    cms,
                    Sets.newHashSet(Collections.singletonList(resource)),
                    null);
                initialSearchObj.setVfsPreloadData(entryBean);
            }
        }

        return initialSearchObj;
    }

    /**
     * Get default types for gallery together with visibility.<p>
     *
     * @return the default types
     */
    private List<I_CmsResourceType> getDefaultTypesForGallery() {

        return OpenCms.getResourceManager().getResourceTypes();
    }

    /**
     * Generates a list of available galleries for the given gallery-type.<p>
     *
     * @param galleryTypeId the gallery-type
     *
     * @return the list of galleries
     *
     */
    @SuppressWarnings("deprecation")
    private List<CmsResource> getGalleriesByType(int galleryTypeId) {

        List<CmsResource> galleries = new ArrayList<CmsResource>();

        // We swallow errors in this method because we don't  want a failure to read some folders (e.g. because of permission problems) to
        // cause an empty gallery list as a result

        try {
            galleries.addAll(
                getCmsObject().readResources(
                    "/",
                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId)));
        } catch (Exception e) {
            LOG.error("Could not read site galleries: " + e.getLocalizedMessage(), e);
        }

        String siteRoot = getCmsObject().getRequestContext().getSiteRoot();
        // if the current site is NOT the root site - add all other galleries from the system path

        try {
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
        } catch (Exception e) {
            LOG.info("Could not read system galleries: " + e.getLocalizedMessage(), e);
        }

        try {
            if (!OpenCms.getSiteManager().isSharedFolder(siteRoot)) {
                String shared = OpenCms.getSiteManager().getSharedFolder();
                List<CmsResource> sharedGalleries = getCmsObject().readResources(
                    shared,
                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId));
                if (sharedGalleries != null) {
                    galleries.addAll(sharedGalleries);
                }
            }
        } catch (Exception e) {
            LOG.info("Could not read shared galleries: " + e.getLocalizedMessage(), e);
        }
        return galleries;
    }

    /**
     * Helper method for getting the initial gallery settings.<p>
     *
     * @param conf the gallery configration
     * @return the gallery settings
     *
     * @throws CmsRpcException if something goes wrong
     */
    private CmsGalleryDataBean getInitialSettingsInternal(CmsGalleryConfiguration conf) throws CmsRpcException {

        CmsGalleryDataBean data = new CmsGalleryDataBean();
        data.setMode(conf.getGalleryMode());
        data.setResultViewType(readResultViewType());
        boolean galleryShowInvalidDefault = Boolean.parseBoolean(
            getWorkplaceSettings().getUserSettings().getAdditionalPreference(PREF_GALLERY_SHOW_INVALID_DEFAULT, true));
        data.setIncludeExpiredDefault(galleryShowInvalidDefault);

        data.setGalleryStoragePrefix(conf.getGalleryStoragePrefix());
        data.setLocales(buildLocalesMap());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(conf.getLocale())) {
            data.setLocale(conf.getLocale());
        } else {
            data.setLocale(getCmsObject().getRequestContext().getLocale().toString());
        }
        data.setVfsRootFolders(getRootEntries());
        data.setScope(getWorkplaceSettings().getLastSearchScope());
        Set<String> folderFilter = readFolderFilters();
        data.setStartFolderFilter(folderFilter);
        if ((folderFilter != null) && !folderFilter.isEmpty()) {
            try {
                data.setVfsPreloadData(
                    generateVfsPreloadData(
                        getCmsObject(),
                        CmsGalleryService.getVfsTreeState(getRequest(), conf.getTreeToken()),
                        folderFilter));
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        data.setSortOrder(getWorkplaceSettings().getLastGalleryResultOrder());

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
                    conf.getResourceTypes(),
                    conf.getSearchTypes());
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

                    String key = "";
                    if (!types.isEmpty() && !CmsStringUtil.isEmptyOrWhitespaceOnly(conf.getReferencePath())) {
                        key = types.get(0).getType();
                        try {
                            CmsResource refResource = getCmsObject().readResource(conf.getReferencePath());
                            String referenceType = OpenCms.getResourceManager().getResourceType(
                                refResource).getTypeName();
                            key = CmsGallerySearchBean.getGalleryStorageKey(
                                data.getGalleryStoragePrefix(),
                                referenceType);
                        } catch (Exception e) {
                            LOG.error("Could not read reference resource: " + conf.getReferencePath());
                        }
                    }

                    if (!data.getGalleries().isEmpty()) {
                        startGallery = getWorkplaceSettings().getLastUsedGallery(key);
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(startGallery)) {
                            // check the user preferences for any configured start gallery
                            String galleryTypeName = data.getGalleries().get(0).getType();
                            startGallery = getWorkplaceSettings().getUserSettings().getStartGallery(
                                galleryTypeName,
                                getCmsObject());
                            if (CmsWorkplace.INPUT_DEFAULT.equals(startGallery)) {
                                startGallery = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartGallery(
                                    galleryTypeName);
                            }
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(startGallery)) {
                                startGallery = getCmsObject().getRequestContext().removeSiteRoot(startGallery);
                            }
                        }
                    }
                    // check if the gallery is available in this site and still exists
                    if (!conf.isResultsSelectable()) {
                        // if selecting results is explicitly disabled, opening the start gallery does not make much sense
                        data.setStartGallery(null);
                    } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(startGallery)) {
                        boolean galleryExists = getCmsObject().existsResource(startGallery);
                        if (galleryExists) {
                            data.setStartGallery(startGallery);
                        } else {
                            LOG.error("Could not read start gallery: " + startGallery);
                            data.setStartGallery(null);
                        }
                    } else {
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
                    conf.getResourceTypes(),
                    conf.getSearchTypes());
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
                        I_CmsPreviewProvider previewProvider = (I_CmsPreviewProvider)Class.forName(
                            providerClass).newInstance();
                        previewProviderMap.put(providerClass, previewProvider);
                        typeProviderMapping.put(type, previewProvider);
                    }
                } catch (Exception e) {
                    logError(
                        new CmsException(
                            Messages.get().container(
                                Messages.ERR_INSTANCING_PREVIEW_PROVIDER_2,
                                providerClass,
                                type.getTypeName()),
                            e));
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
     * @param typesForTypeTab the types which should be shown in the types tab according to the gallery configuration
     *
     * @return the resource types
     */
    private List<CmsResourceTypeBean> getResourceTypeBeans(
        GalleryMode galleryMode,
        String referenceSitePath,
        List<String> resourceTypesList,
        final List<String> typesForTypeTab) {

        List<I_CmsResourceType> resourceTypes = null;
        Set<String> creatableTypes = null;
        switch (galleryMode) {
            case editor:
            case view:
            case adeView:
            case widget:
                resourceTypes = convertTypeNamesToTypes(resourceTypesList);
                if (resourceTypes.size() == 0) {
                    resourceTypes = Lists.newArrayList(getDefaultTypesForGallery());
                }
                creatableTypes = Collections.<String> emptySet();
                break;
            case ade:
                throw new IllegalStateException("This code should never be called");
                // ADE case is handled by container page service
            default:
                resourceTypes = Collections.<I_CmsResourceType> emptyList();
                creatableTypes = Collections.<String> emptySet();
        }
        return buildTypesList(resourceTypes, creatableTypes, Collections.<String> emptySet(), typesForTypeTab);
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
                rootFolders.add(
                    internalCreateVfsEntryBean(
                        getCmsObject(),
                        rootFolderResource,
                        title,
                        true,
                        isEditable(getCmsObject(), rootFolderResource),
                        null,
                        false));
            }

        } catch (CmsException e) {
            error(e);
        }
        return rootFolders;
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
            m_workplaceSettings = CmsWorkplace.getWorkplaceSettings(getCmsObject(), getRequest());
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
        if (searchData.getServerSearchTypes() != null) {
            params.setResourceTypes(searchData.getServerSearchTypes());
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
     * @param checkHasChildren if true, check if the entry has any children; set to false if you want to handle the list of children manually
     *
     * @return the sitemap entry
     *
     * @throws CmsException if something goes wrong reading types and resources
     */
    private CmsSitemapEntryBean prepareSitemapEntry(
        CmsObject cms,
        CmsJspNavElement navElement,
        boolean isRoot,
        boolean checkHasChildren)
    throws CmsException {

        CmsResource ownResource = navElement.getResource();
        CmsResource defaultFileResource = null;
        CmsJspNavBuilder navBuilder = new CmsJspNavBuilder(cms);
        if (ownResource.isFolder() && !navElement.isNavigationLevel()) {
            try {
                defaultFileResource = cms.readDefaultFile(ownResource, CmsResourceFilter.ONLY_VISIBLE);
            } catch (CmsPermissionViolationException e) {
                // user has insufficient rights, can be ignored
            }
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
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(
            navElement.getProperty(CmsPropertyDefinition.PROPERTY_TITLE))) {
            title = navElement.getProperty(CmsPropertyDefinition.PROPERTY_TITLE);
        } else {
            title = navElement.getFileName();
            if (title.contains("/")) {
                title = title.substring(0, title.indexOf("/"));
            }
        }
        String childPath = navElement.getResource().getRootPath();
        boolean noChildren = true;

        if (checkHasChildren) {
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

        if (checkHasChildren && noChildren) {
            result.setChildren(new ArrayList<CmsSitemapEntryBean>());
        }

        return result;
    }

    /**
     * Reads the folder filters for the current site.<p>
     *
     * @return the folder filters
     */
    private Set<String> readFolderFilters() {

        JSONObject storedFilters = readUserFolderFilters();
        Set<String> result = null;
        if (storedFilters.has(getCmsObject().getRequestContext().getSiteRoot())) {
            try {
                org.opencms.json.JSONArray folders = storedFilters.getJSONArray(
                    getCmsObject().getRequestContext().getSiteRoot());
                result = new HashSet<String>();
                for (int i = 0; i < folders.length(); i++) {
                    result.add(folders.getString(i));
                }
            } catch (JSONException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
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
                if (!typeBean.isDeactivated()) {
                    addGalleriesForType(galleryTypeInfos, typeBean.getType());
                }
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
     * Reads the result view type from the current user.<p>
     *
     * @return the result view type
     */
    private String readResultViewType() {

        return (String)getCmsObject().getRequestContext().getCurrentUser().getAdditionalInfo(
            RESULT_VIEW_TYPE_ADD_INFO_KEY);
    }

    /**
     * Reads the users folder filters from the additional info.<p>
     *
     * @return the folder filters
     */
    private JSONObject readUserFolderFilters() {

        CmsUser user = getCmsObject().getRequestContext().getCurrentUser();
        String addInfo = (String)user.getAdditionalInfo(FOLDER_FILTER_ADD_INFO_KEY);
        JSONObject result = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(addInfo)) {
            try {
                result = new JSONObject(addInfo);
            } catch (JSONException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        if (result == null) {
            result = new JSONObject();
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
        // store folder filter
        storeFolderFilter(searchObj.getFolders());

        // search
        CmsGallerySearchParameters params = prepareSearchParams(searchObj);
        org.opencms.search.galleries.CmsGallerySearch searchBean = new org.opencms.search.galleries.CmsGallerySearch();
        CmsObject searchCms = getSearchCms(searchObj);
        searchBean.init(searchCms);

        CmsGallerySearchResultList searchResults = OpenCms.getSearchManager().getIndexSolr(
            "Solr Offline").gallerySearch(searchCms, params);
        searchResults.calculatePages(params.getResultPage(), params.getMatchesPerPage());

        // set only the result dependent search params for this search
        // the user dependent params(galleries, types etc.) remain unchanged
        searchObjBean.setSortOrder(params.getSortOrder().name());
        searchObjBean.setScope(params.getScope());
        searchObjBean.setResultCount(searchResults.getHitCount());
        searchObjBean.setPage(params.getResultPage());
        searchObjBean.setLastPage(params.getResultPage());
        searchObjBean.setResults(buildSearchResultList(searchResults, null));
        if (searchObj.getGalleryMode().equals(GalleryMode.ade)) {
            if (searchObjBean.getResultCount() > 0) {
                CmsADESessionCache cache = CmsADESessionCache.getCache(getRequest(), getCmsObject());
                cache.setLastPageEditorGallerySearch(searchObj);
            }
        }

        return searchObjBean;
    }

    /**
     * Sets the last opened gallery information for the current user.<p>
     *
     * @param searchObject the current search
     */
    private void setLastOpenedGallery(CmsGallerySearchBean searchObject) {

        if ((searchObject.getGalleries() != null)
            && (searchObject.getGalleries().size() <= 1) // if the size is 0, the user has actively deselected the galleries, so we want to handle this case too
            && searchObject.haveGalleriesChanged()) {
            String galleryPath = searchObject.getGalleries().isEmpty() ? null : searchObject.getGalleries().get(0);
            CmsWorkplaceSettings settings = getWorkplaceSettings();
            if (searchObject.getGalleryMode() == GalleryMode.adeView) {
                settings.setLastUsedGallery("" + GalleryMode.adeView, galleryPath);
            } else {
                String referencePath = searchObject.getReferencePath();

                String referenceTypeName = "";

                try {
                    CmsObject cms = getCmsObject();
                    CmsResource referenceResource = cms.readResource(referencePath);
                    I_CmsResourceType referenceType = OpenCms.getResourceManager().getResourceType(referenceResource);
                    referenceTypeName = referenceType.getTypeName();
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
                settings.setLastUsedGallery(
                    CmsGallerySearchBean.getGalleryStorageKey(
                        searchObject.getGalleryStoragePrefix(),
                        referenceTypeName),
                    galleryPath);
            }
        }
    }

    /**
     * Stores the folder filters for the current site.<p>
     *
     * @param folders the folder filters
     */
    private void storeFolderFilter(Set<String> folders) {

        JSONObject storedFilters = readUserFolderFilters();
        try {
            storedFilters.put(getCmsObject().getRequestContext().getSiteRoot(), folders);
            CmsUser user = getCmsObject().getRequestContext().getCurrentUser();
            user.setAdditionalInfo(FOLDER_FILTER_ADD_INFO_KEY, storedFilters.toString());
            getCmsObject().writeUser(user);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}

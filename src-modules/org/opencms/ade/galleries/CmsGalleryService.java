/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/Attic/CmsGalleryService.java,v $
 * Date   : $Date: 2010/05/21 13:20:08 $
 * Version: $Revision: 1.17 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.ReqParam;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.CmsCoreService;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.galleries.CmsGallerySearchParameters;
import org.opencms.search.galleries.CmsGallerySearchResult;
import org.opencms.search.galleries.CmsGallerySearchResultList;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.xml.sitemap.CmsSitemapEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

/**
 * Handles all RPC services related to the gallery dialog.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.17 $ 
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

    /** The advanced gallery index name. */
    public static final String ADVANCED_GALLERY_INDEX = "ADE Gallery Index";

    /** Serialization uid. */
    private static final long serialVersionUID = 1673026761080584889L;

    /** The instance of the resource manager. */
    CmsResourceManager m_resourceManager;

    /** The workplace locale from the current user's settings. */
    private Locale m_wpLocale;

    /**
     * Returns a new configured service instance.<p>
     * 
     * @param request the current request
     * 
     * @return a new service instance
     */
    public static CmsGalleryService newInstance(HttpServletRequest request) {

        CmsGalleryService srv = new CmsGalleryService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        return srv;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getCategoryTreeGalleries(java.util.List)
     */
    public CmsCategoryTreeEntry getCategoryTreeGalleries(List<String> galleries) throws CmsRpcException {

        List<CmsResource> galleryFolders = new ArrayList<CmsResource>();
        if (galleries != null) {
            for (String foldername : galleries) {
                try {
                    galleryFolders.add(getCmsObject().readResource(foldername));
                } catch (CmsException e) {
                    logError(e);
                }
            }
        }
        return readCategoriesTree(galleryFolders);
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getCategoryTreeTypes(java.util.List)
     */
    public CmsCategoryTreeEntry getCategoryTreeTypes(List<String> types) throws CmsRpcException {

        Map<String, CmsGalleryTypeInfo> typeInfos = readGalleryTypes(readResourceTypes(types));
        List<CmsResource> galleryFolders = new ArrayList<CmsResource>();
        for (CmsGalleryTypeInfo info : typeInfos.values()) {
            galleryFolders.addAll(info.getGalleries());
        }
        return readCategoriesTree(galleryFolders);
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getGalleries(java.util.List)
     */
    public List<CmsGalleryFolderBean> getGalleries(List<String> resourceTypes) {

        return buildGalleriesList(readGalleryTypes(readResourceTypes(resourceTypes)));
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getInitialSettings(org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode)
     */
    public CmsGalleryDataBean getInitialSettings(GalleryMode galleryMode) throws CmsRpcException {

        CmsGalleryDataBean data = new CmsGalleryDataBean();
        data.setMode(galleryMode);
        data.setLocales(buildLocalesMap());
        switch (galleryMode) {

            case editor:
            case view:
            case widget:
                List<I_CmsResourceType> types = readResourceTypesFromRequest();
                data.setTypes(buildTypesList(types));
                data.setGalleries(buildGalleriesList(readGalleryTypes(types)));
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getRequest().getParameter(ReqParam.gallerypath.name()))) {
                    data.setStartTab(GalleryTabId.cms_tab_results);
                } else {
                    data.setStartTab(GalleryTabId.cms_tab_galleries);
                }
                break;
            case ade:
                data.setTypes(buildTypesList(readResourceTypesForContainerpage()));
                data.setStartTab(GalleryTabId.cms_tab_types);
                break;
            case sitemap:
                data.setTypes(buildTypesList(readResourceTypesForSitemap()));
                data.setStartTab(GalleryTabId.cms_tab_search);
                break;
            default:
                break;
        }
        return data;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getSearch(org.opencms.ade.galleries.shared.CmsGalleryDataBean)
     */
    public CmsGallerySearchBean getSearch(CmsGalleryDataBean data) {

        CmsGallerySearchBean result = new CmsGallerySearchBean();
        // search within all available types
        List<String> types = new ArrayList<String>();
        for (CmsResourceTypeBean info : data.getTypes()) {
            types.add(info.getType());
        }
        result.setTypes(types);
        switch (data.getMode()) {
            case editor:
            case view:
            case widget:
                String gallery = getRequest().getParameter(ReqParam.gallerypath.name());
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(gallery)) {
                    List<String> galleries = new ArrayList<String>();
                    galleries.add(gallery);
                    result.setGalleries(galleries);
                }
                String currentelement = getRequest().getParameter(ReqParam.currentelement.name());
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(currentelement)) {
                    CmsSitemapEntry entry = null;
                    try {
                        entry = OpenCms.getSitemapManager().getEntryForUri(getCmsObject(), currentelement);
                    } catch (CmsException e) {
                        logError(e);
                    }
                    if ((entry != null) && entry.isSitemap()) {
                        //TODO: handle sitemap entries
                    } else {
                        // get search results given resource path
                        result = findResourceInGallery(currentelement, result);
                    }
                } else {
                    result = search(result);
                }
                // remove all types
                result.setTypes(null);
                break;
            case ade:
                //TODO: get last search from session
                result = search(result);
                // remove all types
                result.setTypes(null);
                break;
            case sitemap:
                //TODO: get last search from session
                result = search(result);
                // remove all types
                result.setTypes(null);
                break;
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
        } catch (Throwable e) {
            error(e);
        }
        return gSearchObj;
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
     * 
     * @return the list with the current search results
     */
    private List<CmsResultItemBean> buildSearchResultList(List<CmsGallerySearchResult> searchResult) {

        ArrayList<CmsResultItemBean> list = new ArrayList<CmsResultItemBean>();
        if ((searchResult == null) || (searchResult.size() == 0)) {
            return list;
        }
        Iterator<CmsGallerySearchResult> iSearchResult = searchResult.iterator();
        while (iSearchResult.hasNext()) {
            try {
                Locale wpLocale = getWorkplaceLocale();
                CmsGallerySearchResult sResult = iSearchResult.next();
                CmsResultItemBean bean = new CmsResultItemBean();
                String path = sResult.getPath();
                path = getCmsObject().getRequestContext().removeSiteRoot(path);

                // resource path as id
                bean.setPath(path);
                // title
                bean.setTitle(sResult.getTitle());
                // resource type
                bean.setType(sResult.getResourceType());
                // structured id
                bean.setClientId(sResult.getStructureId());
                // TODO: set following infos if required: date last modified, description, structured id

                // set nice resource type name as subtitle
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(sResult.getResourceType());
                bean.setDescription(CmsWorkplaceMessages.getResourceTypeName(wpLocale, type.getTypeName()));
                bean.setExcerpt(sResult.getExcerpt());
                list.add(bean);
            } catch (Exception e) {
                logError(e);
            }
        }
        return list;
    }

    /**
     * Generates a map with all available content types.<p>
     * 
     * The map uses resource type name as the key and stores the CmsTypesListInfoBean as the value.
     * 
     * @param types the resource types
     * 
     * @return the map containing the available resource types
     */
    private List<CmsResourceTypeBean> buildTypesList(List<I_CmsResourceType> types) {

        ArrayList<CmsResourceTypeBean> list = new ArrayList<CmsResourceTypeBean>();
        if (types == null) {
            return list;
        }
        Iterator<I_CmsResourceType> it = types.iterator();
        while (it.hasNext()) {
            I_CmsResourceType type = it.next();
            CmsResourceTypeBean bean = new CmsResourceTypeBean();

            // unique id
            bean.setType(type.getTypeName());
            // type nice name            
            Locale wpLocale = getWorkplaceLocale();
            // TODO: remove:  bean.setTypeNiceName(CmsWorkplaceMessages.getResourceTypeDescription(wpLocale, type.getTypeName()));
            // type title and subtitle
            bean.setTitle(CmsWorkplaceMessages.getResourceTypeName(wpLocale, type.getTypeName()));
            bean.setDescription(CmsWorkplaceMessages.getResourceTypeDescription(wpLocale, type.getTypeName()));
            // gallery id of corresponding galleries
            ArrayList<String> galleryNames = new ArrayList<String>();
            Iterator<I_CmsResourceType> galleryTypes = type.getGalleryTypes().iterator();
            while (galleryTypes.hasNext()) {
                I_CmsResourceType galleryType = galleryTypes.next();
                galleryNames.add(galleryType.getTypeName());
            }
            bean.setGalleryTypeNames(galleryNames);
            list.add(bean);

        }
        return list;
    }

    /**
     * Returns the search object containing the list with search results and the path to the specified resource.<p>
     * 
     * @param resourceName the given resource
     * @param initialSearchObj the initial search object
     * 
     * @return the gallery search object containing the current search parameter and the search result list
     */
    private CmsGallerySearchBean findResourceInGallery(String resourceName, CmsGallerySearchBean initialSearchObj) {

        CmsResource resource = null;
        CmsProperty locale = CmsProperty.getNullProperty();
        try {
            resource = getCmsObject().readResource(resourceName);
            locale = getCmsObject().readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_LOCALE, true);
        } catch (CmsException e) {
            logError(e);
        }
        CmsGallerySearchBean searchObj = new CmsGallerySearchBean(initialSearchObj);
        if (resource == null) {
            return searchObj;
        }

        // prepare the search object
        String rootPath = resource.getRootPath();
        ArrayList<String> types = new ArrayList<String>();
        types.add(String.valueOf(resource.getTypeId()));
        searchObj.setTypes(types);

        ArrayList<String> galleries = new ArrayList<String>();
        galleries.add(CmsResource.getFolderPath(resourceName));
        searchObj.setGalleries(galleries);
        searchObj.setSortOrder(CmsGallerySearchParameters.CmsGallerySortParam.DEFAULT.toString());
        if (!locale.isNullProperty()) {
            searchObj.setLocale(locale.getValue());
        }
        int currentPage = 1;
        boolean found = false;
        searchObj.setPage(currentPage);
        CmsGallerySearchParameters params = prepareSearchParams(searchObj);
        org.opencms.search.galleries.CmsGallerySearch searchBean = new org.opencms.search.galleries.CmsGallerySearch();
        searchBean.init(getCmsObject());
        searchBean.setIndex(ADVANCED_GALLERY_INDEX);

        CmsGallerySearchResultList searchResults = null;
        while (!found) {
            params.setResultPage(currentPage);
            searchResults = searchBean.getResult(params);
            Iterator<CmsGallerySearchResult> resultsIt = searchResults.listIterator();
            while (resultsIt.hasNext()) {
                CmsGallerySearchResult searchResult = resultsIt.next();
                if (searchResult.getPath().equals(rootPath)) {
                    found = true;
                    break;
                }
            }
            if (!found && (searchResults.getHitCount() / (currentPage * params.getMatchesPerPage()) >= 1)) {
                currentPage++;
            } else {
                break;
            }
        }
        CmsGallerySearchBean searchResultsObj = new CmsGallerySearchBean();
        if (found && (searchResults != null)) {
            searchResultsObj.setSortOrder(params.getSortOrder().name());
            searchResultsObj.setResultCount(searchResults.getHitCount());
            searchResultsObj.setPage(params.getResultPage());
            searchResultsObj.setResults(buildSearchResultList(searchResults));
            searchResultsObj.setPage(currentPage);
            searchResultsObj.setTabId(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_results.name());
        }
        return searchResultsObj;
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

        // if the current site is NOT the root site - add all other galleries from the system path
        if (!getCmsObject().getRequestContext().getSiteRoot().equals("")) {
            List<CmsResource> systemGalleries = null;
            // get the galleries in the /system/ folder
            systemGalleries = getCmsObject().readResources(
                CmsWorkplace.VFS_PATH_SYSTEM,
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId));
            if ((systemGalleries != null) && (systemGalleries.size() > 0)) {
                // add the found system galleries to the result
                galleries.addAll(systemGalleries);
            }
        }

        return galleries;
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
     * Returns the search parameters for the given query data.<p>
     * 
     * @param queryData the query data
     * 
     * @return the prepared search parameters
     */
    private CmsGallerySearchParameters prepareSearchParams(CmsGallerySearchBean searchData) {

        List<String> types = searchData.getTypes();
        List<String> galleries = searchData.getGalleries();
        List<String> categories = searchData.getCategories();
        String queryStr = searchData.getQuery();
        int matches = searchData.getMachesPerPage();
        CmsGallerySearchParameters.CmsGallerySortParam sortOrder;
        String temp = searchData.getSortOrder();
        try {
            sortOrder = CmsGallerySearchParameters.CmsGallerySortParam.valueOf(temp);
        } catch (Exception e) {
            sortOrder = CmsGallerySearchParameters.CmsGallerySortParam.DEFAULT;
        }

        int page = searchData.getPage();
        String locale = searchData.getLocale();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(locale)) {
            locale = getCmsObject().getRequestContext().getLocale().toString();
        }

        CmsGallerySearchParameters params = new CmsGallerySearchParameters();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(queryStr)) {
            params.setSearchWords(queryStr);
        }
        params.setSearchLocale(locale);
        params.setSortOrder(sortOrder);
        params.setMatchesPerPage(matches);
        params.setResultPage(page);
        if (types != null) {
            params.setResourceTypes(types);
        }
        if (categories != null) {
            params.setCategories(categories);
        }
        if (galleries != null) {
            params.setGalleries(galleries);
        }
        return params;
    }

    /**
     * Generates a list of all available CmsCategory objects.<p>
     * 
     * @param galleries the galleries
     * 
     * @return a list of categories
     * 
     * @throws CmsRpcException error happens during reading categories
     */
    private CmsCategoryTreeEntry readCategoriesTree(List<CmsResource> galleries) throws CmsRpcException {

        List<String> refPath = new ArrayList<String>();
        if ((galleries != null) && !galleries.isEmpty()) {
            Iterator<CmsResource> iGalleries = galleries.iterator();

            while (iGalleries.hasNext()) {
                CmsResource res = iGalleries.next();
                refPath.add(getCmsObject().getSitePath(res));
            }
        }

        CmsCoreService coreService = new CmsCoreService();
        coreService.setCms(getCmsObject());
        CmsCategoryTreeEntry categoryTreeEntry = null;

        categoryTreeEntry = coreService.getCategories("", true, refPath);

        return categoryTreeEntry;
    }

    /**
     * Returns a map with gallery type names associated with the list of available galleries for this type.<p>
     * 
     * @param resourceTypes the resources types to collect the galleries for 
     * 
     * @return a map with gallery type and  the associated galleries
     */
    private Map<String, CmsGalleryTypeInfo> readGalleryTypes(List<I_CmsResourceType> resourceTypes) {

        Map<String, CmsGalleryTypeInfo> galleryTypeInfos = new HashMap<String, CmsGalleryTypeInfo>();
        Iterator<I_CmsResourceType> iTypes = resourceTypes.iterator();
        while (iTypes.hasNext()) {
            I_CmsResourceType contentType = iTypes.next();
            Iterator<I_CmsResourceType> galleryTypes = contentType.getGalleryTypes().iterator();
            while (galleryTypes.hasNext()) {
                try {
                    I_CmsResourceType galleryType = galleryTypes.next();
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
        return galleryTypeInfos;
    }

    /**
     * Reads a list of resource types by the given names.<p>
     * 
     * @param typeNames the type names
     * 
     * @return the resource types
     */
    private List<I_CmsResourceType> readResourceTypes(List<String> typeNames) {

        List<I_CmsResourceType> resTypes = new ArrayList<I_CmsResourceType>();
        for (String typeName : typeNames) {
            try {
                resTypes.add(getResourceManager().getResourceType(typeName));
            } catch (CmsLoaderException e) {
                logError(e);
            }
        }
        return resTypes;
    }

    /**
     * Returns the resource types configured to be used within the container-page editor.<p>
     * 
     * @return the resource types
     * @throws CmsRpcException if something goes wrong reading the configuration
     */
    private List<I_CmsResourceType> readResourceTypesForContainerpage() throws CmsRpcException {

        List<I_CmsResourceType> result = new ArrayList<I_CmsResourceType>();
        try {
            List<CmsResource> resources = OpenCms.getADEManager().getSearchableResourceTypes(
                getCmsObject(),
                getCmsObject().getRequestContext().getUri(),
                getThreadLocalRequest());
            for (CmsResource resource : resources) {
                result.add(getResourceManager().getResourceType(resource));
            }
        } catch (CmsException e) {
            error(e);
        }
        return result;
    }

    /**
     * Returns the resource types configured to be used within the sitemap editor.<p>
     * 
     * @return the resource types
     * 
     * @throws CmsRpcException if something goes wrong
     */
    private List<I_CmsResourceType> readResourceTypesForSitemap() throws CmsRpcException {

        List<I_CmsResourceType> result = new ArrayList<I_CmsResourceType>();
        try {
            result.add(getResourceManager().getResourceType(CmsResourceTypeXmlContainerPage.getStaticTypeName()));
        } catch (Exception e) {
            error(e);
        }
        return result;
    }

    /**
     * Returns a list of resource types by a request parameter.<p>
     * 
     * @return the resource types
     */
    private List<I_CmsResourceType> readResourceTypesFromRequest() {

        List<I_CmsResourceType> result = new ArrayList<I_CmsResourceType>();
        String typesParam = getRequest().getParameter(ReqParam.types.name());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(typesParam)) {
            String[] temp = typesParam.split(",");
            for (int i = 0; i < temp.length; i++) {
                try {
                    result.add(getResourceManager().getResourceType(temp[i].trim()));
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
     */
    private CmsGallerySearchBean search(CmsGallerySearchBean searchObj) {

        CmsGallerySearchBean searchObjBean = new CmsGallerySearchBean(searchObj);
        if (searchObj == null) {
            return searchObjBean;
        }
        // search
        CmsGallerySearchParameters params = prepareSearchParams(searchObj);
        org.opencms.search.galleries.CmsGallerySearch searchBean = new org.opencms.search.galleries.CmsGallerySearch();
        searchBean.init(getCmsObject());
        searchBean.setIndex(ADVANCED_GALLERY_INDEX);
        CmsGallerySearchResultList searchResults = searchBean.getResult(params);
        // set only the result dependent search params for this search
        // the user dependent params(galleries, types etc.) remain unchanged
        searchObjBean.setSortOrder(params.getSortOrder().name());
        searchObjBean.setResultCount(searchResults.getHitCount());
        searchObjBean.setPage(params.getResultPage());
        searchObjBean.setResults(buildSearchResultList(searchResults));

        return searchObjBean;
    }

}
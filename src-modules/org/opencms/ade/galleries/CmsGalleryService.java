/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/Attic/CmsGalleryService.java,v $
 * Date   : $Date: 2010/04/12 14:00:39 $
 * Version: $Revision: 1.3 $
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

import org.opencms.ade.galleries.shared.CmsCategoriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleryDialogBean;
import org.opencms.ade.galleries.shared.CmsGalleryInfoBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchObject;
import org.opencms.ade.galleries.shared.CmsResultsListInfoBean;
import org.opencms.ade.galleries.shared.CmsTypesListInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.shared.rpc.CmsRpcException;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.galleries.CmsGallerySearch;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;

/**
 * Handles all RPC services related to the gallery dialog.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $ 
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

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGalleryService.class);

    /** Serialization uid. */
    private static final long serialVersionUID = 1673026761080584889L;

    /** The instance of the resource manager. */
    CmsResourceManager m_resourceManager;

    /** The available resource type id's. */
    private JSONArray m_resourceTypeNames;

    /** The available resource types. */
    private List<I_CmsResourceType> m_resourceTypes;

    /** The workplace locale from the current user's settings. */
    private Locale m_wpLocale;

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getCriteriaLists(java.util.ArrayList)
     */
    public CmsGalleryInfoBean getCriteriaLists(ArrayList<String> tabs) throws CmsRpcException {

        CmsGalleryInfoBean gInfoBean = new CmsGalleryInfoBean();
        try {
            gInfoBean.setDialogInfo(buildSearchParamsLists(tabs));
            return gInfoBean;
        } catch (Throwable e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }
    }

    /** 
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getInitialSearch(org.opencms.ade.galleries.shared.CmsGallerySearchObject)
     */
    public CmsGalleryInfoBean getInitialSearch(CmsGallerySearchObject searchObj) throws CmsRpcException {

        CmsGalleryInfoBean gInfoBean = new CmsGalleryInfoBean();
        try {
            gInfoBean.setSearchObject(buildInitialSearch(searchObj));
            return gInfoBean;
        } catch (Throwable e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getInitialSettings(ArrayList, CmsGallerySearchObject, String)
     */
    public CmsGalleryInfoBean getInitialSettings(
        ArrayList<String> tabs,
        CmsGallerySearchObject searchObj,
        String dialogMode) throws CmsRpcException {

        CmsGalleryInfoBean gInfoBean = new CmsGalleryInfoBean();
        try {
            gInfoBean.setDialogInfo(buildSearchParamsLists(tabs));
            gInfoBean.setSearchObject(buildInitialSearch(searchObj));
            gInfoBean.setDialogMode(dialogMode);
            return gInfoBean;
        } catch (Throwable e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }

    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getSearch(CmsGallerySearchObject)
     */
    public CmsGalleryInfoBean getSearch(CmsGallerySearchObject searchObj) throws CmsRpcException {

        CmsGalleryInfoBean gInfoBean = new CmsGalleryInfoBean();
        try {
            gInfoBean.setSearchObject(search(searchObj));
            return gInfoBean;
        } catch (Throwable e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }
    }

    /**
     * Returns the map with given categories.<p>
     * 
     * The map uses category path as the key and stores the CmsCategoriesListInfoBean as the value.
     * 
     * @param categories the categories
     * @return the map with categories
     */
    private LinkedHashMap<String, CmsCategoriesListInfoBean> buildCategoriesList(List<CmsCategory> categories) {

        LinkedHashMap<String, CmsCategoriesListInfoBean> map = new LinkedHashMap<String, CmsCategoriesListInfoBean>();
        if ((categories == null) || (categories.size() == 0)) {
            return map;
        }
        // the next lines sort the categories according to their path 
        Map<String, CmsCategory> sorted = new TreeMap<String, CmsCategory>();

        Iterator<CmsCategory> i = categories.iterator();
        while (i.hasNext()) {
            CmsCategory category = i.next();
            String categoryPath = category.getPath();
            if (sorted.get(categoryPath) != null) {
                continue;
            }
            sorted.put(categoryPath, category);
        }

        List<CmsCategory> sortedCategories = new ArrayList<CmsCategory>(sorted.values());

        i = sortedCategories.iterator();
        while (i.hasNext()) {
            CmsCategory cat = i.next();
            CmsCategoriesListInfoBean bean = new CmsCategoriesListInfoBean();
            try {
                // 1: category path as id
                bean.setId(cat.getPath());
                // 2: category title
                bean.setTitle(cat.getTitle());
                // 3: category path
                bean.setSubTitle(cat.getPath());
                // 4: category root path
                bean.setRootPath(cat.getRootPath());
                // 5: category level                
                int level = CmsResource.getPathLevel(cat.getPath());
                bean.setLevel(level);
                // 6: set icon path 
                String iconPath = CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES
                    + OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypeFolder.RESOURCE_TYPE_NAME).getIcon());
                bean.setIconResource(iconPath);
                map.put(cat.getPath(), bean);
            } catch (Exception e) {
                // TODO: Improve error handling
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return map;
    }

    /**
     * Returns the map with the available galleries.<p>
     * 
     * The map uses gallery path as teh key and stores the CmsGalleriesListInfoBean as the value.
     * 
     * @param galleryTypes the galleries
     * @return the map with gallery info beans
     */
    private LinkedHashMap<String, CmsGalleriesListInfoBean> buildGalleriesList(
        Map<String, CmsGalleryTypeInfo> galleryTypes) {

        LinkedHashMap<String, CmsGalleriesListInfoBean> map = new LinkedHashMap<String, CmsGalleriesListInfoBean>();
        if (galleryTypes == null) {
            return map;
        }
        Iterator<Entry<String, CmsGalleryTypeInfo>> iGalleryTypes = galleryTypes.entrySet().iterator();
        while (iGalleryTypes.hasNext()) {
            Entry<String, CmsGalleryTypeInfo> ent = iGalleryTypes.next();
            CmsGalleryTypeInfo tInfo = ent.getValue();
            String iconPath = CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES
                + OpenCms.getWorkplaceManager().getExplorerTypeSetting(tInfo.getResourceType().getTypeName()).getIcon());
            ArrayList<String> contentTypes = new ArrayList<String>();
            Iterator<I_CmsResourceType> it = tInfo.getContentTypes().iterator();
            while (it.hasNext()) {
                contentTypes.add(String.valueOf(it.next().getTypeName()));
            }
            Iterator<CmsResource> ir = tInfo.getGalleries().iterator();
            while (ir.hasNext()) {
                CmsResource res = ir.next();
                CmsGalleriesListInfoBean bean = new CmsGalleriesListInfoBean();
                String sitePath = getCmsObject().getSitePath(res);
                String title = "";
                try {
                    // read the gallery title
                    title = getCmsObject().readPropertyObject(sitePath, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue(
                        "");
                } catch (CmsException e) {
                    // error reading title property
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                // 1: sitepath as gallery id 
                bean.setId(sitePath);
                // TODO: set the resource
                //CmsFormatterInfoBean formatterInfo = new CmsFormatterInfoBean(tInfo.getResourceType(), false);
                //formatterInfo.setResource(res);
                // 2: content types
                bean.setContentTypes(contentTypes);
                // 3: title
                bean.setTitle(title);
                // 4: gallery path as sub title            
                bean.setSubTitle(sitePath);
                // 5: gallery icon
                bean.setIconResource(iconPath);
                // 6: gallery type name
                bean.setGalleryTypeName(tInfo.getResourceType().getTypeName());

                // TODO:: active flag
                //jsonObj.put(ItemKey.gallerytypeid.toString(), tInfo.getResourceType().getTypeId());

                map.put(sitePath, bean);
            }
        }
        return map;
    }

    /**
     * Returns the current search object containing the initial search results.<p>
     * 
     * These could be the list of search results specified by the provided search object or 
     * a list of search results containing a specific resource, which was previously selected. 
     * The path to the selected resource is provided by the search object.
     * 
     * @param initialSearch the search object with parameters for the initial search
     * @return the search object containing the results of the initial search
     */
    private CmsGallerySearchObject buildInitialSearch(CmsGallerySearchObject initialSearch) {

        CmsGallerySearchObject searchObj = new CmsGallerySearchObject();
        try {
            String resourcePath = initialSearch.getResourcePath();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resourcePath)) {
                CmsSitemapEntry entry = OpenCms.getSitemapManager().getEntryForUri(getCmsObject(), resourcePath);
                if (entry.isSitemap()) {
                    //TODO: put the sitemap parameter in results
                    //result.put(ResponseKey.sitemap.name(), buildJSONForSitemap(resourcePath, null, null, true));
                } else {
                    // get search results given resource path
                    searchObj = findResourceInGallery(resourcePath, initialSearch);
                }
            } else {
                // search with the initial search
                searchObj = search(initialSearch);
            }
            return searchObj;
        } catch (Exception e) {
            // TODO: improve error handling
            LOG.error(e.getLocalizedMessage(), e);
        }
        return searchObj;
    }

    /**
     * Returns a map with the available locales.<p>
     * 
     * The map entry key is the current locale and the value the localized nice.
     * 
     * @return the map representation of all available locales
     */
    private TreeMap<String, String> buildLocalesMap() {

        TreeMap<String, String> localesMap = new TreeMap<String, String>();
        Iterator<Locale> it = OpenCms.getLocaleManager().getAvailableLocales().iterator();
        while (it.hasNext()) {
            Locale locale = it.next();
            localesMap.put(locale.toString(), locale.getDisplayName(getWorkplaceLocale()));
        }
        return localesMap;
    }

    /**
     * Returns the gallery dialog bean containing the content of the configured tabs.<p>
     * 
     * @param tabs the configured tabs for the gallery dialog
     * @return the content of the gallery dialog
     */
    private CmsGalleryDialogBean buildSearchParamsLists(ArrayList<String> tabs) {

        CmsGalleryDialogBean bean = new CmsGalleryDialogBean();
        // set the tabs to display in the gallery
        bean.setTabs(tabs);

        Map<String, CmsGalleryTypeInfo> galleryTypes = readGalleryTypes(getResourceTypes());
        // collect galleries
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name())) {
            LinkedHashMap<String, CmsGalleriesListInfoBean> galleries = buildGalleriesList(galleryTypes);
            bean.setGalleries(galleries);
        }
        // collect types
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name())) {
            LinkedHashMap<String, CmsTypesListInfoBean> types = buildTypesList(getResourceTypes());
            bean.setTypes(types);
        }
        // collect categories
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name())) {
            List<CmsResource> galleryFolders = new ArrayList<CmsResource>();
            Iterator<Entry<String, CmsGalleryTypeInfo>> iGalleryTypes = galleryTypes.entrySet().iterator();
            while (iGalleryTypes.hasNext()) {
                galleryFolders.addAll(iGalleryTypes.next().getValue().getGalleries());
            }
            LinkedHashMap<String, CmsCategoriesListInfoBean> categories = buildCategoriesList(readCategories(galleryFolders));
            bean.setCategories(categories);
        }
        // collect sitemap data
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_sitemap.name())) {
            // TODO: implement, change target uri to "/",add available site roots 
            //result.put(ResponseKey.sitemap.name(), buildJSONForSitemap("/demo_t3/", null, null, true));
        }
        // collect vfs tree data
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_vfstree.name())) {
            //TODO: implement add available site roots
            //result.put(ResponseKey.vfstree.name(), buildJSONForVfsTree(null));
        }
        // collect container page types
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_containerpage.name())) {
            //TODO: implement
        }
        // TODO: set current locale if required        
        // set the available locales
        bean.setLocales(buildLocalesMap());

        return bean;
    }

    /**
     * Returns the list of beans for the given search results.<p>
     * 
     * @param searchResult the list of search results
     * 
     * @return the list with the current search results
     */
    private ArrayList<CmsResultsListInfoBean> buildSearchResultList(List<CmsGallerySearchResult> searchResult) {

        ArrayList<CmsResultsListInfoBean> list = new ArrayList<CmsResultsListInfoBean>();
        if ((searchResult == null) || (searchResult.size() == 0)) {
            return list;
        }
        Iterator<CmsGallerySearchResult> iSearchResult = searchResult.iterator();
        while (iSearchResult.hasNext()) {
            try {
                Locale wpLocale = getWorkplaceLocale();
                CmsGallerySearchResult sResult = iSearchResult.next();
                CmsResultsListInfoBean bean = new CmsResultsListInfoBean();
                String path = sResult.getPath();
                path = getCmsObject().getRequestContext().removeSiteRoot(path);
                String fileIcon = getFileIconName(path);
                String iconPath = CmsWorkplace.RES_PATH_FILETYPES;
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(fileIcon)) {
                    iconPath += OpenCms.getWorkplaceManager().getExplorerTypeSetting(sResult.getResourceType()).getIcon();
                } else {
                    iconPath += "mimetype/" + fileIcon;
                }
                iconPath = CmsWorkplace.getResourceUri(iconPath);

                // 1: resource path as id
                bean.setId(path);
                // 2: title
                bean.setTitle(sResult.getTitle());
                // 3: resource type
                bean.setResourceType(sResult.getResourceType());
                // 4: icon path
                bean.setIconResource(iconPath);
                // TODO: set following infos if required: date last modified, description, structured id

                // set nice resource type name as subtitle
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(sResult.getResourceType());
                bean.setSubTitle(CmsWorkplaceMessages.getResourceTypeName(wpLocale, type.getTypeName()));

                // TODO: only add excerpt if not empty
                // if (!CmsStringUtil.isEmptyOrWhitespaceOnly(sResult.getExcerpt())) {
                //      formatterInfo.addAdditionalInfo(EXCERPT_FIELD_NAME, OpenCms.getWorkplaceManager().getMessages(
                //      wpLocale).key(Messages.GUI_LABEL_EXCERPT), sResult.getExcerpt());
                // }
                list.add(bean);
            } catch (Exception e) {
                // TODO: Improve error handling
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
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
    private LinkedHashMap<String, CmsTypesListInfoBean> buildTypesList(List<I_CmsResourceType> types) {

        LinkedHashMap<String, CmsTypesListInfoBean> map = new LinkedHashMap<String, CmsTypesListInfoBean>();
        if (types == null) {
            return map;
        }
        Iterator<I_CmsResourceType> it = types.iterator();
        while (it.hasNext()) {
            I_CmsResourceType type = it.next();
            CmsTypesListInfoBean bean = new CmsTypesListInfoBean();
            // 1: unique id
            bean.setId(type.getTypeName());
            // 2: type nice name
            Locale wpLocale = getWorkplaceLocale();
            bean.setTypeNiceName(CmsWorkplaceMessages.getResourceTypeDescription(wpLocale, type.getTypeName()));
            // 3: type title and subtitle
            bean.setTitle(CmsWorkplaceMessages.getResourceTypeName(wpLocale, type.getTypeName()));
            bean.setSubTitle(CmsWorkplaceMessages.getResourceTypeDescription(wpLocale, type.getTypeName()));
            // 4: resouce type icon
            String iconPath = CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES
                + OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName()).getIcon());
            bean.setIconResource(iconPath);
            // 5: gallery id of corresponding galleries
            ArrayList<String> galleryNames = new ArrayList<String>();
            Iterator<I_CmsResourceType> galleryTypes = type.getGalleryTypes().iterator();
            while (galleryTypes.hasNext()) {
                I_CmsResourceType galleryType = galleryTypes.next();
                galleryNames.add(galleryType.getTypeName());
            }
            bean.setGalleryTypeNames(galleryNames);
            map.put(type.getTypeName(), bean);

        }
        return map;
    }

    /**
     * Returns the search object containing the list with search results and the path to the specified resource.<p>
     * 
     * @param resourceName the given resource
     * @param initialSearchObj the initial search object
     * 
     * @return the gallery search object containing the current search parameter and the search result list
     */
    private CmsGallerySearchObject findResourceInGallery(String resourceName, CmsGallerySearchObject initialSearchObj) {

        CmsResource resource = null;
        CmsProperty locale = CmsProperty.getNullProperty();
        try {
            resource = getCmsObject().readResource(resourceName);
            locale = getCmsObject().readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_LOCALE, true);
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        CmsGallerySearchObject searchObj = new CmsGallerySearchObject(initialSearchObj);
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
        CmsGallerySearch searchBean = new CmsGallerySearch();
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
        CmsGallerySearchObject searchResultsObj = new CmsGallerySearchObject();
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
     * Gets the file-icon-name.<p>
     * 
     * @param path the file path
     * @return the file-icon-name for this files mime-type, or an empty string if none available
     */
    private String getFileIconName(String path) {

        String mt = OpenCms.getResourceManager().getMimeType(path, null);
        if (mt.equals("application/msword")
            || mt.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            mt = "msword.png";
        } else if (mt.equals("application/pdf")) {
            mt = "pdf.png";
        } else if (mt.equals("application/vnd.ms-excel")
            || mt.equals("application/excel")
            || mt.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            mt = "excel.png";
        } else if (mt.equals("application/vnd.ms-powerpoint")
            || mt.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
            mt = "powerpoint.png";
        } else if (mt.equals("image/jpeg")
            || mt.equals("image/gif")
            || mt.equals("image/png")
            || mt.equals("image/tiff")) {
            mt = "image.png";
        } else if (mt.equals("text/plain")) {
            mt = "plain.png";
        } else if (mt.equals("application/zip") || mt.equals("application/x-gzip") || mt.equals("application/x-tar")) {
            mt = "archiv.png";
        } else {
            mt = "";
        }

        return mt;
    }

    /**
     * Generates a list of available galleries for the given gallery-type.<p>
     * 
     * @param galleryTypeId the gallery-type
     * @return the list of galleries
     */
    private List<CmsResource> getGalleriesByType(int galleryTypeId) {

        List<CmsResource> galleries = new ArrayList<CmsResource>();
        try {
            // get the galleries of the current site
            galleries = getCmsObject().readResources(
                "/",
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId));
        } catch (CmsException e) {
            // error reading resources with filter
            LOG.error(e.getLocalizedMessage(), e);
        }

        // if the current site is NOT the root site - add all other galleries from the system path
        if (!getCmsObject().getRequestContext().getSiteRoot().equals("")) {
            List<CmsResource> systemGalleries = null;
            try {
                // get the galleries in the /system/ folder
                systemGalleries = getCmsObject().readResources(
                    CmsWorkplace.VFS_PATH_SYSTEM,
                    CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId));
            } catch (CmsException e) {
                // error reading resources with filter
                LOG.error(e.getLocalizedMessage(), e);
            }

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
     * Returns the available resource type names.<p>
     * 
     * @return the resource type names
     */
    private JSONArray getResourceTypeNames() {

        if (m_resourceTypeNames == null) {
            try {
                JSONObject data = CmsGalleryProvider.get().getData(getRequest());
                String typesString = data.optString(I_CmsGalleryProviderConstants.ReqParam.types.name());
                String[] typesArray = CmsStringUtil.splitAsArray(typesString, ",");
                m_resourceTypeNames = new JSONArray(typesArray);
            } catch (JSONException e) {
                // TODO: improve error handling
                LOG.error(e.getLocalizedMessage(), e);
            }
            if ((m_resourceTypeNames == null) || (m_resourceTypeNames.length() == 0)) {
                // using all available types if typeNames is null or empty
                m_resourceTypes = getResourceManager().getResourceTypes();
                m_resourceTypeNames = new JSONArray();
                for (I_CmsResourceType type : m_resourceTypes) {
                    m_resourceTypeNames.put(type.getTypeName());
                }
            }
        }
        return m_resourceTypeNames;
    }

    /**
     * Returns the available resource types.<p>
     * 
     * @return the resource types
     */
    private List<I_CmsResourceType> getResourceTypes() {

        if (m_resourceTypes == null) {
            m_resourceTypes = readContentTypes(getResourceTypeNames());
        }
        return m_resourceTypes;
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
    private CmsGallerySearchParameters prepareSearchParams(CmsGallerySearchObject searchData) {

        List<String> types = searchData.getTypes();
        ArrayList<String> galleries = searchData.getGalleries();
        ArrayList<String> categories = searchData.getCategories();
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
     * Generates a list of all available CmsCategory obejcts.<p>
     * 
     * @param galleries the galleries
     * @return a list of categories
     */
    private List<CmsCategory> readCategories(List<CmsResource> galleries) {

        CmsCategoryService catService = CmsCategoryService.getInstance();
        List<String> repositories = new ArrayList<String>();
        if ((galleries != null) && !galleries.isEmpty()) {
            Iterator<CmsResource> iGalleries = galleries.iterator();

            while (iGalleries.hasNext()) {
                CmsResource res = iGalleries.next();
                repositories.addAll(catService.getCategoryRepositories(getCmsObject(), getCmsObject().getSitePath(res)));
            }
        } else {
            repositories.add(CmsCategoryService.CENTRALIZED_REPOSITORY);
        }
        List<CmsCategory> categories = null;
        try {
            categories = catService.readCategoriesForRepositories(getCmsObject(), "", true, repositories);
        } catch (CmsException e) {
            // error reading categories
            LOG.error(e.getLocalizedMessage(), e);
        }
        return categories;
    }

    /**
     * Reads the resource-types for given resource-type-id's.<p>
     * 
     * @param types the type-names
     * @return the list of resource-types
     */
    // TODO: replace parameter with list
    private List<I_CmsResourceType> readContentTypes(JSONArray types) {

        List<I_CmsResourceType> result = new ArrayList<I_CmsResourceType>();
        if ((types == null) || (types.length() == 0)) {
            return result;
        }
        for (int i = 0; i < types.length(); i++) {
            try {
                result.add(getResourceManager().getResourceType(types.getString(i)));
            } catch (CmsLoaderException e) {
                // TODO: Improve error handling
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            } catch (JSONException e) {
                // TODO: Improve error handling
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Returns a map with gallery type names associated with the list of available galleries for this type.<p>
     * 
     * @param resourceTypes the resources types to collect the galleries for 
     * @return a map with gallery type and  the associated galleries
     */
    private Map<String, CmsGalleryTypeInfo> readGalleryTypes(List<I_CmsResourceType> resourceTypes) {

        Map<String, CmsGalleryTypeInfo> galleryTypeInfos = new HashMap<String, CmsGalleryTypeInfo>();
        Iterator<I_CmsResourceType> iTypes = resourceTypes.iterator();
        while (iTypes.hasNext()) {
            I_CmsResourceType contentType = iTypes.next();
            Iterator<I_CmsResourceType> galleryTypes = contentType.getGalleryTypes().iterator();
            while (galleryTypes.hasNext()) {
                I_CmsResourceType galleryType = galleryTypes.next();
                if (galleryTypeInfos.containsKey(galleryType.getTypeName())) {
                    CmsGalleryTypeInfo typeInfo = galleryTypeInfos.get(galleryType.getTypeName());
                    typeInfo.addContentType(contentType);
                } else {
                    CmsGalleryTypeInfo typeInfo = new CmsGalleryTypeInfo(
                        galleryType,
                        contentType,
                        getGalleriesByType(galleryType.getTypeId()));
                    galleryTypeInfos.put(galleryType.getTypeName(), typeInfo);
                }
            }
        }
        return galleryTypeInfos;
    }

    /**
     * Returns the gallery search object containing the results for the current parameter.<p>
     * 
     * @param searchObj the current search object 
     */
    private CmsGallerySearchObject search(CmsGallerySearchObject searchObj) {

        CmsGallerySearchObject searchObjBean = new CmsGallerySearchObject(searchObj);
        if (searchObj == null) {
            return searchObjBean;
        }
        // search
        CmsGallerySearchParameters params = prepareSearchParams(searchObj);
        CmsGallerySearch searchBean = new CmsGallerySearch();
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
/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/Attic/CmsGalleryService.java,v $
 * Date   : $Date: 2010/03/19 10:11:54 $
 * Version: $Revision: 1.1 $
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

import org.opencms.ade.galleries.shared.CmsGalleryDialogBean;
import org.opencms.ade.galleries.shared.CmsGalleryInfoBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchObject;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.shared.CmsListInfoBean;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;

/**
 * Handles all RPC services related to the sitemap.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $ 
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
    private JSONArray m_resourceTypeIds;

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
            gInfoBean.setDialogInfo(buildCriteriaLists(tabs));
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
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService#getInitialSettings(java.util.ArrayList, org.opencms.ade.galleries.shared.CmsGallerySearchObject)
     */
    public CmsGalleryInfoBean getInitialSettings(ArrayList<String> tabs, CmsGallerySearchObject searchObj)
    throws CmsRpcException {

        CmsGalleryInfoBean gInfoBean = new CmsGalleryInfoBean();
        try {
            gInfoBean.setDialogInfo(buildCriteriaLists(tabs));
            gInfoBean.setSearchObject(buildInitialSearch(searchObj));
            return gInfoBean;
        } catch (Throwable e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }

    }

    //TODO:test thsi function
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
     * Returns the list with given categories.<p>
     * 
     * @param categories the categories
     * @return the list with categories
     */
    private ArrayList<CmsListInfoBean> buildCategoriesList(List<CmsCategory> categories) {

        ArrayList<CmsListInfoBean> list = new ArrayList<CmsListInfoBean>();
        if ((categories == null) || (categories.size() == 0)) {
            return list;
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

            //TODO: replace through the specific bean
            CmsListInfoBean bean = new CmsListInfoBean();
            try {
                // 1: category title
                bean.setTitle(cat.getTitle());

                // 2: category path
                bean.setSubTitle(cat.getPath());
                // 3: category root path
                //TODO: set category root path
                //jsonObj.put(ItemKey.rootpath.toString(), cat.getRootPath());
                // 4 category level
                // TODO: set category level
                int level = CmsResource.getPathLevel(cat.getPath());
                // TODO: set icon path 
                String iconPath = CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES
                    + OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypeFolder.RESOURCE_TYPE_NAME).getIcon());
                //                formatterInfo.setIcon(iconPath);
                //                jsonObj.put(ItemKey.itemhtml.toString(), getFormattedListContent(formatterInfo));
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
     * Returns the gallery dialog bean containing the content of the configured tabs.<p>
     * 
     * @param tabs the configured tabs for the gallery dialog
     * @return the content of the gallery dialog
     */
    private CmsGalleryDialogBean buildCriteriaLists(ArrayList<String> tabs) {

        //TODO: erweitern, so dass automatisch nur der Inahlt fuer die angegebene Tabs generiert und zurueckgegeben wird.

        CmsGalleryDialogBean bean = new CmsGalleryDialogBean();
        // set the tabs to display in the gallery
        bean.setTabs(tabs);

        Map<String, CmsGalleryTypeInfo> galleryTypes = readGalleryTypes(getResourceTypes());
        // collect galleries
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name())) {
            ArrayList<CmsListInfoBean> galleries = buildGalleriesList(galleryTypes);
            bean.setGalleries(galleries);
        }
        // collect types
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name())) {
            ArrayList<CmsListInfoBean> types = buildTypesList(getResourceTypes());
            bean.setTypes(types);
        }
        // collect categories
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name())) {
            List<CmsResource> galleryFolders = new ArrayList<CmsResource>();
            Iterator<Entry<String, CmsGalleryTypeInfo>> iGalleryTypes = galleryTypes.entrySet().iterator();
            while (iGalleryTypes.hasNext()) {
                galleryFolders.addAll(iGalleryTypes.next().getValue().getGalleries());
            }
            ArrayList<CmsListInfoBean> categories = buildCategoriesList(readCategories(galleryFolders));
            bean.setCategories(categories);
        }
        // TODO: collect sitemap data
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_sitemap.name())) {
            // TODO: change target uri to "/"
            // TODO add available site roots
            //result.put(ResponseKey.sitemap.name(), buildJSONForSitemap("/demo_t3/", null, null, true));
        }
        //TODO: collect vfs tree data
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_vfstree.name())) {
            //result.put(ResponseKey.vfstree.name(), buildJSONForVfsTree(null));
            //TODO: add available site roots
        }
        //TODO: collect container page types
        if (tabs.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_containerpage.name())) {
            //TODO: implement
        }
        // set current locale        
        //TODO: do we need the locale of the requested resource here? set the locale if needed
        //Locale locale = getCmsObject().getRequestContext().getLocale();       

        // set the available locales
        bean.setLocales(buildLocalesMap());

        return bean;
    }

    //TODO: use the special gallery list bean, replace the generic one
    /**
     * Returns the list of list beans for the available galleries.<p>
     * 
     * @param galleryTypes the galleries
     * @return the list of gallery info beans
     */
    private ArrayList<CmsListInfoBean> buildGalleriesList(Map<String, CmsGalleryTypeInfo> galleryTypes) {

        ArrayList<CmsListInfoBean> list = new ArrayList<CmsListInfoBean>();
        if (galleryTypes == null) {
            return list;
        }
        Iterator<Entry<String, CmsGalleryTypeInfo>> iGalleryTypes = galleryTypes.entrySet().iterator();
        while (iGalleryTypes.hasNext()) {
            Entry<String, CmsGalleryTypeInfo> ent = iGalleryTypes.next();
            CmsGalleryTypeInfo tInfo = ent.getValue();
            String iconPath = CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES
                + OpenCms.getWorkplaceManager().getExplorerTypeSetting(tInfo.getResourceType().getTypeName()).getIcon());
            JSONArray contentTypes = new JSONArray();
            Iterator<I_CmsResourceType> it = tInfo.getContentTypes().iterator();
            while (it.hasNext()) {
                contentTypes.put(it.next().getTypeId());
            }
            Iterator<CmsResource> ir = tInfo.getGalleries().iterator();
            while (ir.hasNext()) {
                CmsResource res = ir.next();
                CmsListInfoBean bean = new CmsListInfoBean();
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

                // TODO: set the resource
                //CmsFormatterInfoBean formatterInfo = new CmsFormatterInfoBean(tInfo.getResourceType(), false);
                //formatterInfo.setResource(res);
                //TODO: set the content types
                //jsonObj.put(ItemKey.contenttypes.toString(), contentTypes);
                bean.setTitle(title);
                //jsonObj.put(ItemKey.title.toString(), title);
                //formatterInfo.setTitleInfo(ItemKey.title.toString(), ItemKey.title.toString(), title);

                // 2: gallery path

                //TODO: set the sitepath as bean member
                //jsonObj.put(ItemKey.path.toString(), sitePath);
                bean.setSubTitle(sitePath);
                // formatterInfo.setSubTitleInfo(ItemKey.path.toString(), ItemKey.path.toString(), sitePath);
                //TODO: set the icon
                // formatterInfo.setIcon(iconPath);

                // 3: active flag
                // TODO: was soll hier gemacht werden?
                //jsonObj.put(ItemKey.gallerytypeid.toString(), tInfo.getResourceType().getTypeId());
                //jsonObj.put(ItemKey.icon.toString(), iconPath);

                list.add(bean);
            }
        }
        return list;
    }

    /**
     * Returns the current search object containing the initial search results.<p>
     * 
     * These could be the list of search results specified by the provided search object or 
     * a list of search results containing a specific resource, which was previously selected. 
     * The path to the selected resource is provides through the search object.
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
                //TODO: search with the initial search
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
     * Returns the list of beans for the given search results.<p>
     * 
     * @param searchResult the list of search results
     * 
     * @return the list with the current search results
     */
    private ArrayList<CmsListInfoBean> buildSearchResultList(List<CmsGallerySearchResult> searchResult) {

        //TODO: replace through specific bean
        ArrayList<CmsListInfoBean> list = new ArrayList<CmsListInfoBean>();
        if ((searchResult == null) || (searchResult.size() == 0)) {
            return list;
        }
        Iterator<CmsGallerySearchResult> iSearchResult = searchResult.iterator();
        while (iSearchResult.hasNext()) {
            try {
                Locale wpLocale = getWorkplaceLocale();
                CmsGallerySearchResult sResult = iSearchResult.next();
                //TODO: replace throhgh the specific bean
                CmsListInfoBean bean = new CmsListInfoBean();
                String path = sResult.getPath();
                //TODO: prove: does this work???
                path = getCmsObject().getRequestContext().removeSiteRoot(path);
                String fileIcon = getFileIconName(path);
                String iconPath = CmsWorkplace.RES_PATH_FILETYPES;
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(fileIcon)) {
                    iconPath += OpenCms.getWorkplaceManager().getExplorerTypeSetting(sResult.getResourceType()).getIcon();
                } else {
                    iconPath += "mimetype/" + fileIcon;
                }
                iconPath = CmsWorkplace.getResourceUri(iconPath);
                // TODO: set the required infos for the bean

                // TODO: date last modified
                //resultEntry.put(ItemKey.datemodified.toString(), sResult.getDateLastModified());
                bean.setTitle(sResult.getTitle());
                // TODO: set Description
                //resultEntry.put(ItemKey.info.toString(), sResult.getDescription());
                // TODO: set resouce type
                //resultEntry.put(ItemKey.type.toString(), sResult.getResourceType());                
                // TODO: set the path
                //resultEntry.put(ItemKey.path.toString(), path);
                //TODO: set the icon path
                //resultEntry.put(ItemKey.icon.toString(), iconPath);
                // TODO: set the structured id
                //resultEntry.put(ItemKey.clientid.toString(), sResult.getStructureId());

                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(sResult.getResourceType());
                bean.setSubTitle(CmsWorkplaceMessages.getResourceTypeName(wpLocale, type.getTypeName()));

                // TODO: only add excerpt if not empty
                //                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(sResult.getExcerpt())) {
                //                    formatterInfo.addAdditionalInfo(EXCERPT_FIELD_NAME, OpenCms.getWorkplaceManager().getMessages(
                //                        wpLocale).key(Messages.GUI_LABEL_EXCERPT), sResult.getExcerpt());
                //                }
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
     * Generates list of beans with all available content types.<p>
     * 
     * @param types the resource types
     * 
     * @return the list with available resource types
     */
    private ArrayList<CmsListInfoBean> buildTypesList(List<I_CmsResourceType> types) {

        ArrayList<CmsListInfoBean> list = new ArrayList<CmsListInfoBean>();
        if (types == null) {
            return list;
        }
        Iterator<I_CmsResourceType> it = types.iterator();
        while (it.hasNext()) {
            I_CmsResourceType type = it.next();
            //TODO: replace with specific bean
            CmsListInfoBean bean = new CmsListInfoBean();
            Locale wpLocale = getWorkplaceLocale();
            bean.setTitle(CmsWorkplaceMessages.getResourceTypeName(wpLocale, type.getTypeName()));
            //TODO: set type id
            // TODO: set type name
            //jType.put(ItemKey.typeid.toString(), type.getTypeId());
            //jType.put(ItemKey.type.toString(), type.getTypeName());
            //                jType.put(ItemKey.info.toString(), CmsWorkplaceMessages.getResourceTypeDescription(
            //                    wpLocale,
            //                    type.getTypeName()));
            bean.setSubTitle(CmsWorkplaceMessages.getResourceTypeDescription(wpLocale, type.getTypeName()));
            //TODO: set the icon
            String iconPath = CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES
                + OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName()).getIcon());
            //jType.put(ItemKey.icon.toString(), iconPath);
            //formatterInfo.setIcon(iconPath);
            JSONArray galleryIds = new JSONArray();
            Iterator<I_CmsResourceType> galleryTypes = type.getGalleryTypes().iterator();
            while (galleryTypes.hasNext()) {
                I_CmsResourceType galleryType = galleryTypes.next();
                galleryIds.put(galleryType.getTypeId());

            }
            //TODO: set the gallery ids for the types
            //jType.put(ItemKey.gallerytypeid.toString(), galleryIds);
            list.add(bean);

        }
        return list;
    }

    /**
     * Returns the list with search results containing the specified resource.<p>
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
        searchObj.setTypeNames(types);

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

    //TODO: refactor during reimplementation of dictionary
    /**
     * Returns the available resource type id's.<p>
     * 
     * @return the resource type id's
     */
    private JSONArray getResourceTypeIds() {

        if (m_resourceTypeIds == null) {
            try {
                // TODO: work with JSOM when reading parameter from request!!!!
                JSONObject data = CmsGalleryProvider.get().getData(getCmsObject(), getRequest());
                String typesString = data.optString(I_CmsGalleryProviderConstants.ReqParam.types.name());
                String[] typesArray = CmsStringUtil.splitAsArray(typesString, ",");
                m_resourceTypeIds = new JSONArray(typesArray);
            } catch (JSONException e) {
                // TODO: improve error handling
                LOG.error(e.getLocalizedMessage(), e);
            }
            if ((m_resourceTypeIds == null) || (m_resourceTypeIds.length() == 0)) {
                // using all available types if typeIds is null or empty
                m_resourceTypes = getResourceManager().getResourceTypes();
                m_resourceTypeIds = new JSONArray();
                for (I_CmsResourceType type : m_resourceTypes) {
                    m_resourceTypeIds.put(type.getTypeId());
                }
            }

        }
        return m_resourceTypeIds;
    }

    /**
     * Returns the available resource types.<p>
     * 
     * @return the resource types
     */
    private List<I_CmsResourceType> getResourceTypes() {

        if (m_resourceTypes == null) {
            m_resourceTypes = readContentTypes(getResourceTypeIds());
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

        ArrayList<String> types = searchData.getTypeNames();
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
     * Generates a list of all available categories.<p>
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
     * @param types the type-id's
     * @return the list of resource-types
     */
    private List<I_CmsResourceType> readContentTypes(JSONArray types) {

        List<I_CmsResourceType> result = new ArrayList<I_CmsResourceType>();
        if ((types == null) || (types.length() == 0)) {
            return result;
        }
        for (int i = 0; i < types.length(); i++) {
            try {
                result.add(getResourceManager().getResourceType(types.getInt(i)));
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
        // TODO: proove this call?
        if (searchObj == null) {
            return searchObjBean;
        }
        // search
        CmsGallerySearchParameters params = prepareSearchParams(searchObj);
        CmsGallerySearch searchBean = new CmsGallerySearch();
        searchBean.init(getCmsObject());
        searchBean.setIndex(ADVANCED_GALLERY_INDEX);
        CmsGallerySearchResultList searchResults = searchBean.getResult(params);
        searchObjBean.setSortOrder(params.getSortOrder().name());
        searchObjBean.setResultCount(searchResults.getHitCount());
        searchObjBean.setPage(params.getResultPage());
        searchObjBean.setResults(buildSearchResultList(searchResults));

        return searchObjBean;
    }
}
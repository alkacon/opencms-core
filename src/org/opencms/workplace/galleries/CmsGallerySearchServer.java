/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsGallerySearchServer.java,v $
 * Date   : $Date: 2009/11/06 13:02:31 $
 * Version: $Revision: 1.8 $
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

package org.opencms.workplace.galleries;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.CmsGallerySearch;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.CmsSearchResult;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Gallery search server used for client/server communication.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 7.6
 */
public class CmsGallerySearchServer extends CmsJspActionElement {

    /** Request parameter action value constants. */
    protected enum Action {

        /** To get all available galleries and categories. */
        ALL,

        /** To get all available categories. */
        CATEGORIES,

        /** To get all available container types. */
        CONTAINERS,

        /** To get all available galleries. */
        GALLERIES,

        /** To retrieve search results. */
        SEARCH;
    }

    /** Json property name constants. */
    protected enum JsonKeys {

        /** The categories. */
        CATEGORIES("categories"),

        /** The content types. */
        CONTENTTYPES("contenttypes"),

        /** The errors. */
        ERRORS("errors"),

        /** The galleries. */
        GALLERIES("galleries"),

        /** The gallery types. */
        GALLERYTYPEID("gallerytypeid"),

        /** The level. */
        LEVEL("level"),

        /** The page. */
        PAGE("page"),

        /** The path. */
        PATH("path"),

        /** The query-string. */
        QUERY("query"),

        /** The query data */
        QUERYDATA("querydata"),

        /** The root-path. */
        ROOTPATH("rootpath"),

        /** The search result. */
        SEARCHRESULT("searchresult"),

        /** The sort-by param. */
        SORTBY("sortby"),

        /** The sort-order. */
        SORTORDER("sortorder"),

        /** The title. */
        TITLE("title"),

        /** The type. */
        TYPE("type"),

        /** The type id. */
        TYPEID("typeid"),

        /** The gallery-types. */
        TYPES("types");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonKeys(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** Request parameter name constants. */
    protected enum ReqParam {

        /** The action of execute. */
        ACTION("action"),

        /** Generic data parameter. */
        DATA("data"),

        /** The current locale. */
        LOCALE("locale");

        /** Parameter name. */
        private String m_name;

        /** Constructor.<p> */
        private ReqParam(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /**
     * Gallery info object.<p>
     */
    protected class CmsGalleryTypeInfo {

        /** The gallery folder resources. */
        private List<CmsResource> m_galleries;

        /** The content types using this gallery. */
        private List<I_CmsResourceType> m_contentTypes;

        /** The resource type of this gallery. */
        private I_CmsResourceType m_resourceType;

        /**
         * 
         * 
         * @param resourceType
         * @param contentType
         * @param galleries
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
         * Returns the gallery folder resources.<p>
         *
         * @return the resources
         */
        protected List<CmsResource> getGalleries() {

            return m_galleries;
        }

        /**
         * Sets the resource.<p>
         *
         * @param resource the resource to set
         */
        protected void setResource(List<CmsResource> galleries) {

            m_galleries = galleries;
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
         * Sets the contentTypes.<p>
         *
         * @param contentTypes the contentTypes to set
         */
        protected void setContentTypes(List<I_CmsResourceType> contentTypes) {

            m_contentTypes = contentTypes;
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
         * Sets the resourceType.<p>
         *
         * @param resourceType the resourceType to set
         */
        protected void setResourceType(I_CmsResourceType resourceType) {

            m_resourceType = resourceType;
        }
    }

    /** Mime type constant. */
    public static final String MIMETYPE_APPLICATION_JSON = "application/json";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGallerySearchServer.class);

    /** The workplace messages. */
    private CmsWorkplaceMessages m_workplaceMessages;

    /** The current instance of the cms object. */
    private CmsObject m_cms;

    /** The instance of the resource manager. */
    CmsResourceManager m_resourceManager;

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
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsGallerySearchServer(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
        m_cms = getCmsObject();
        m_workplaceMessages = new CmsWorkplaceMessages(
            CmsWorkplace.initUserSettings(m_cms, null, false).getUserSettings().getLocale());

    }

    /**
     * Transforms an <code>JSONArray</code> to an <code>int[]</code>.
     * Returns null if the JSON is null or empty.<p>
     * 
     * @param arr
     * @return the resulting array
     * @throws JSONException if something goes wrong
     */
    public static int[] transformToIntArray(JSONArray arr) throws JSONException {

        if ((arr == null) || (arr.length() == 0)) {
            return null;
        }
        int[] ret = new int[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            ret[i] = arr.getInt(i);
        }
        return ret;
    }

    /**
     * Transforms an <code>JSONArray</code> to an <code>String[]</code>.
     * Returns null if the JSON is null or empty.<p>
     * 
     * @param arr
     * @return the resulting array
     * @throws JSONException if something goes wrong
     */
    public static String[] transformToStringArray(JSONArray arr) throws JSONException {

        if ((arr == null) || (arr.length() == 0)) {
            return null;
        }
        String[] ret = new String[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            ret[i] = arr.getString(i);
        }
        return ret;
    }

    /**
     * Transforms an <code>JSONArray</code> to a <code>List&lt;String&gt;</code>.
     * Returns null if the JSON is null or empty.<p>
     * 
     * @param arr
     * @return the resulting list
     * @throws JSONException if something goes wrong
     */
    public static List<String> transformToStringList(JSONArray arr) throws JSONException {

        if ((arr == null) || (arr.length() == 0)) {
            return null;
        }
        List<String> ret = new ArrayList<String>();
        for (int i = 0; i < arr.length(); i++) {
            ret.add(arr.getString(i));
        }
        return ret;
    }

    /**
     * Generates a JSON-Map of all system categories. The category-path is used as the key.<p>
     * 
     * @return a JSON-Map of categories
     */
    private JSONArray readSystemCategories() {

        CmsCategoryService catService = CmsCategoryService.getInstance();
        List<CmsCategory> foundCategories = Collections.emptyList();
        // String editedResource = null;
        //        if (CmsStringUtil.isNotEmpty(getParamResource())) {
        //            editedResource = getParamResource();
        //        }
        try {
            foundCategories = catService.readCategories(m_cms, "", true, null);
        } catch (CmsException e) {
            // error reading categories
        }
        return buildJSONForCategories(foundCategories);

    }

    /**
     * Generates a JSON-Map of all available categories. The category-path is used as the key.<p>
     * 
     * @param galleries the galleries
     * @return a JSON-Map of categories
     */
    private List<CmsCategory> readCategories(List<CmsResource> galleries) {

        CmsCategoryService catService = CmsCategoryService.getInstance();
        Iterator<CmsResource> iGalleries = galleries.iterator();
        List<String> repositories = new ArrayList<String>();
        while (iGalleries.hasNext()) {
            CmsResource res = iGalleries.next();
            repositories.addAll(catService.getCategoryRepositories(m_cms, m_cms.getSitePath(res)));
        }
        List<CmsCategory> categories = null;
        try {
            categories = catService.readCategoriesForRepositories(m_cms, "", true, repositories);
        } catch (CmsException e) {
            // error reading categories
            LOG.error(e.getLocalizedMessage(), e);
        }
        return categories;
    }

    /**
     * Generates a JSON-Map for all available content types.
     * 
     * @param types the gallery-types
     * @return the content-types
     * @throws CmsLoaderException if something goes wrong
     * @throws JSONException if something goes wrong
     */
    private JSONArray buildJSONForTypes(List<I_CmsResourceType> types) throws JSONException {

        JSONArray result = new JSONArray();
        if (types == null) {
            return result;
        }
        Iterator<I_CmsResourceType> it = types.iterator();
        while (it.hasNext()) {
            I_CmsResourceType type = it.next();
            JSONObject jType = new JSONObject();
            jType.put(JsonKeys.TITLE.getName(), type.getTypeName());
            jType.put(JsonKeys.TYPEID.getName(), type.getTypeId());
            int galleryId = 0;
            if (type.getGalleryType() != null) {
                galleryId = type.getGalleryType().getTypeId();

            }
            jType.put(JsonKeys.GALLERYTYPEID.getName(), galleryId);
            result.put(jType);
        }

        return result;
    }

    /**
     * Returns the type names for the given type-ids.<p>
     * 
     * @param types the types
     * @return the type names
     * @throws CmsLoaderException if something goes wrong
     * @throws JSONException if something goes wrong
     */
    private List<String> getTypeNames(JSONArray types) throws CmsLoaderException, JSONException {

        List<String> ret = new ArrayList<String>();
        if ((types == null) || (types.length() == 0)) {
            return ret;
        }
        CmsResourceManager rm = OpenCms.getResourceManager();
        for (int i = 0; i < types.length(); i++) {
            ret.add(rm.getResourceType(types.getInt(i)).getTypeName());
        }
        return ret;
    }

    /**
     * Main method that handles all requests.<p>
     * 
     * @throws IOException if there is any problem while writing the result to the response 
     * @throws JSONException if there is any problem with JSON
     */
    public void serve() throws JSONException, IOException {

        // set the mime type to application/json
        CmsFlexController controller = CmsFlexController.getController(getRequest());
        controller.getTopResponse().setContentType(MIMETYPE_APPLICATION_JSON);

        JSONObject result = new JSONObject();

        HttpServletRequest request = getRequest();
        CmsObject cms = getCmsObject();

        String actionParam = request.getParameter(ReqParam.ACTION.getName());
        String localeParam = request.getParameter(ReqParam.LOCALE.getName());
        String dataParam = request.getParameter(ReqParam.DATA.getName());
        JSONArray errors = new JSONArray();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(actionParam)) {
            errors.put("Missing parameter: " + ReqParam.ACTION.getName());
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(dataParam)) {
            errors.put("Missing parameter: " + ReqParam.DATA.getName());
        }
        if (errors.length() == 0) {
            try {
                Action action = Action.valueOf(actionParam.toUpperCase());
                cms.getRequestContext().setLocale(CmsLocaleManager.getLocale(localeParam));
                JSONObject data = new JSONObject(dataParam);
                if (action.equals(Action.ALL)) {
                    JSONArray resourceTypesParam = data.getJSONArray(JsonKeys.TYPES.getName());
                    List<I_CmsResourceType> resourceTypes = readContentTypes(resourceTypesParam);
                    result.put(JsonKeys.TYPES.getName(), buildJSONForTypes(resourceTypes));
                    Map<String, CmsGalleryTypeInfo> galleryTypes = readGalleryTypes(resourceTypes);

                    result.put(JsonKeys.GALLERIES.getName(), buildJSONForGalleries(galleryTypes));
                    List<CmsResource> galleryFolders = new ArrayList<CmsResource>();
                    Iterator<Entry<String, CmsGalleryTypeInfo>> iGalleryTypes = galleryTypes.entrySet().iterator();
                    while (iGalleryTypes.hasNext()) {
                        galleryFolders.addAll(iGalleryTypes.next().getValue().getGalleries());
                    }
                    result.put(JsonKeys.CATEGORIES.getName(), buildJSONForCategories(readCategories(galleryFolders)));
                    // TODO: Add containers.
                } else if (action.equals(Action.CATEGORIES)) {
                    result.put(JsonKeys.CATEGORIES.getName(), readSystemCategories());
                } else if (action.equals(Action.CONTAINERS)) {
                    // TODO:  Will be implemented later.
                } else if (action.equals(Action.GALLERIES)) {
                    JSONArray resourceTypesParam = data.getJSONArray(JsonKeys.TYPES.getName());
                    List<I_CmsResourceType> resourceTypes = readContentTypes(resourceTypesParam);
                    Map<String, CmsGalleryTypeInfo> galleryTypes = readGalleryTypes(resourceTypes);

                    result.put(JsonKeys.GALLERIES.getName(), buildJSONForGalleries(galleryTypes));
                } else if (action.equals(Action.SEARCH)) {
                    JSONObject query = data.getJSONObject(JsonKeys.QUERYDATA.getName());
                    result.put(JsonKeys.SEARCHRESULT.getName(), search(query));
                }
            } catch (Exception e) {
                errors.put(e.getMessage());
            }
        }
        if (errors.length() != 0) {
            result.put(JsonKeys.ERRORS.getName(), errors);
        }
        // write the result
        result.write(getResponse().getWriter());

    }

    /**
     * Returns the JSON-Object for the given list of categories.<p>
     * 
     * @param categories the categories
     * @return the JSON-Object
     */
    private JSONArray buildJSONForCategories(List<CmsCategory> categories) {

        JSONArray result = new JSONArray();
        if ((categories == null) || (categories.size() == 0)) {
            return result;
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

            JSONObject jsonObj = new JSONObject();
            try {
                // 1: category title
                jsonObj.put(JsonKeys.TITLE.getName(), cat.getTitle());
                // 2: category path
                jsonObj.put(JsonKeys.PATH.getName(), cat.getPath());
                // 3: category root path
                jsonObj.put(JsonKeys.ROOTPATH.getName(), cat.getRootPath());
                // 4 category level
                jsonObj.put(JsonKeys.LEVEL.getName(), CmsResource.getPathLevel(cat.getPath()));
                result.put(jsonObj);
            } catch (JSONException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }

        return result;
    }

    /**
     * Returns the JSON-Object for the given list of galleries.<p>
     * 
     * @param cms the cms-object
     * @param galleryTypes the galleries
     * @return the JSON-Object
     */
    private JSONArray buildJSONForGalleries(Map<String, CmsGalleryTypeInfo> galleryTypes) {

        JSONArray result = new JSONArray();
        if (galleryTypes == null) {
            return result;
        }
        CmsResourceManager rm = OpenCms.getResourceManager();
        Iterator<Entry<String, CmsGalleryTypeInfo>> iGalleryTypes = galleryTypes.entrySet().iterator();
        while (iGalleryTypes.hasNext()) {
            Entry<String, CmsGalleryTypeInfo> ent = iGalleryTypes.next();
            CmsGalleryTypeInfo tInfo = ent.getValue();
            JSONArray contentTypes = new JSONArray();
            Iterator<I_CmsResourceType> it = tInfo.getContentTypes().iterator();
            while (it.hasNext()) {
                contentTypes.put(it.next().getTypeId());
            }
            Iterator<CmsResource> ir = tInfo.getGalleries().iterator();
            while (ir.hasNext()) {
                CmsResource res = ir.next();
                JSONObject jsonObj = new JSONObject();
                String sitePath = m_cms.getSitePath(res);
                String title = "";
                try {
                    // read the gallery title
                    title = m_cms.readPropertyObject(sitePath, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue("");
                } catch (CmsException e) {
                    // error reading title property
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                try {
                    jsonObj.put(JsonKeys.CONTENTTYPES.getName(), contentTypes);
                    jsonObj.put(JsonKeys.TITLE.getName(), title);
                    // 2: gallery path
                    jsonObj.put(JsonKeys.PATH.getName(), sitePath);
                    // 3: active flag
                    jsonObj.put(JsonKeys.GALLERYTYPEID.getName(), tInfo.getResourceType().getTypeId());
                } catch (JSONException e) {
                    // TODO: Auto-generated catch block
                    e.printStackTrace();
                }
                result.put(jsonObj);
            }
        }
        return result;
    }

    private JSONArray buildJSONForSearchResult(List<CmsSearchResult> searchResult) throws JSONException {

        JSONArray result = new JSONArray();
        if ((searchResult == null) || (searchResult.size() == 0)) {
            return result;
        }
        Iterator<CmsSearchResult> iSearchResult = searchResult.iterator();
        while (iSearchResult.hasNext()) {
            CmsSearchResult sResult = iSearchResult.next();
            JSONObject resultEntry = new JSONObject();
            resultEntry.put("changedate", sResult.getDateLastModified());
            resultEntry.put("title", sResult.getField("title"));
            resultEntry.put("type", sResult.getDocumentType());
            resultEntry.put("path", sResult.getPath());
            result.put(resultEntry);
        }
        return result;
    }

    /**
     * Generates a list of available galleries for the given gallery-type.<p>
     * 
     * @param galleryTypeId the gallery-type
     * @return the galleries
     */
    private List<CmsResource> getGalleriesByType(int galleryTypeId) {

        List<CmsResource> galleries = new ArrayList<CmsResource>();
        try {
            // get the galleries of the current site
            galleries = m_cms.readResources(
                "/",
                CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId));
        } catch (CmsException e) {
            // error reading resources with filter
            LOG.error(e.getLocalizedMessage(), e);
        }

        // if the current site is NOT the root site - add all other galleries from the system path
        if (!m_cms.getRequestContext().getSiteRoot().equals("")) {
            List<CmsResource> systemGalleries = null;
            try {
                // get the galleries in the /system/ folder
                systemGalleries = m_cms.readResources(
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
     * Returns the list of galleries for the given types.<p>
     * 
     * @param types the gallery-types
     * @return the galleries
     */
    private Map<String, CmsGalleryTypeInfo> readGalleryTypes(List<I_CmsResourceType> resourceTypes) {

        Map<String, CmsGalleryTypeInfo> galleryTypeInfos = new HashMap<String, CmsGalleryTypeInfo>();
        Iterator<I_CmsResourceType> iTypes = resourceTypes.iterator();
        while (iTypes.hasNext()) {
            I_CmsResourceType contentType = iTypes.next();
            I_CmsResourceType galleryType = contentType.getGalleryType();
            if (galleryType != null) {
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
     * Returns the search result.<p>
     * 
     * @param query the query parameters
     * @return the search result
     * @throws JSONException if something goes wrong
     */
    private JSONObject search(JSONObject query) throws JSONException {

        JSONObject result = new JSONObject();
        if (query == null) {
            return result;
        }
        JSONArray types = query.getJSONArray(JsonKeys.TYPES.getName());
        JSONArray galleries = query.getJSONArray(JsonKeys.GALLERIES.getName());
        List<String> galleriesList = transformToStringList(galleries);
        JSONArray categories = query.getJSONArray(JsonKeys.CATEGORIES.getName());
        List<String> categoriesList = transformToStringList(categories);
        String queryStr = query.getString(JsonKeys.QUERY.getName());
        CmsGallerySearch.SortParam sortOrder = CmsGallerySearch.SortParam.DEFAULT;
        try {
            sortOrder = CmsGallerySearch.SortParam.valueOf(query.getString(JsonKeys.SORTORDER.getName()).toUpperCase());
        } catch (Exception e) {
            //may happen
        }
        int page = query.getInt(JsonKeys.PAGE.getName());

        List<String> typeNames = new ArrayList<String>();

        try {
            typeNames = getTypeNames(types);
        } catch (CmsLoaderException e) {
            // TODO: Auto-generated catch block
            e.printStackTrace();
        }

        // TODO: searching the old index, replace with new search
        CmsUser user = m_cms.getRequestContext().currentUser();
        String indexName = new CmsUserSettings(user).getWorkplaceSearchIndexName();

        CmsSearchParameters params = new CmsSearchParameters(queryStr);
        params.setIndex(indexName);
        params.setMatchesPerPage(20);
        params.setSearchPage(page);
        if (typeNames != null) {
            params.setResourceTypes(typeNames);
        }
        if (categoriesList != null) {
            params.setCategories(categoriesList);
        }
        if (galleriesList != null) {
            params.setRoots(galleriesList);
        }
        // search
        CmsSearch searchBean = new CmsSearch();
        searchBean.init(m_cms);
        searchBean.setParameters(params);
        List<CmsSearchResult> searchResults = searchBean.getSearchResult();
        // TODO: use for new search
        //        CmsGallerySearch gSearch = new CmsGallerySearch();
        //        gSearch.setCategories(categoriesArr);
        //        gSearch.setGalleries(galleriesArr);
        //        gSearch.setResultPage(page);
        //        gSearch.setQuery(queryStr);
        //        gSearch.setTypes(typesArr);
        //        gSearch.setSortBy(sortBy);
        //        gSearch.setSortOrder(sortOrder);
        //        result.put("sortorder", gSearch.getSortOrder().getName());
        //        result.put("sortby", gSearch.getSortBy().getName());
        //        result.put("resultcount", gSearch.getSearchResultCount());
        //        result.put("resultpage", gSearch.getResultPage());
        //        result.put("resultpagecount", gSearch.getPageCount());
        //        result.put("resultlist", buildJSONForSearchResult(gSearch.getResult()));

        result.put("sortorder", searchBean.getSortOrder());
        result.put("resultcount", searchBean.getSearchResultCount());
        result.put("resultpage", searchBean.getSearchPage());
        result.put("resultlist", buildJSONForSearchResult(searchResults));

        return result;
    }

    private List<I_CmsResourceType> readContentTypes(JSONArray types) {

        List<I_CmsResourceType> result = new ArrayList<I_CmsResourceType>();
        if ((types == null) || (types.length() == 0)) {
            return result;
        }
        for (int i = 0; i < types.length(); i++) {
            try {
                result.add(getResourceManager().getResourceType(types.getInt(i)));
            } catch (CmsLoaderException e) {
                // TODO: Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO: Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;
    }

}

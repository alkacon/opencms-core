/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsGallerySearchServer.java,v $
 * Date   : $Date: 2009/11/03 15:28:20 $
 * Version: $Revision: 1.2 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.CmsGallerySearch;
import org.opencms.search.CmsSearchResult;
import org.opencms.workplace.CmsWorkplace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Gallery search server used for client/server communication.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
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

        /** The galleries. */
        GALLERIES("galleries"),

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

    /** Mime type constant. */
    public static final String MIMETYPE_APPLICATION_JSON = "application/json";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGallerySearchServer.class);

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsGallerySearchServer(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);

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
     * Generates a JSON-Map of all system categories. The category-path is used as the key.<p>
     * 
     * @param cms  the cms-object
     * @return a JSON-Map of categories
     */
    public JSONObject getCategories(CmsObject cms) {

        CmsCategoryService catService = CmsCategoryService.getInstance();
        List<CmsCategory> foundCategories = Collections.emptyList();
        // String editedResource = null;
        //        if (CmsStringUtil.isNotEmpty(getParamResource())) {
        //            editedResource = getParamResource();
        //        }
        try {
            foundCategories = catService.readCategories(cms, "", true, null);
        } catch (CmsException e) {
            // error reading categories
        }
        return buildJSONForCategories(foundCategories);

    }

    /**
     * Generates a JSON-Map of all available categories. The category-path is used as the key.<p>
     * 
     * @param cms the cms-object
     * @param galleries the galleries
     * @return a JSON-Map of categories
     */
    public JSONObject getCategories(CmsObject cms, List<CmsResource> galleries) {

        CmsCategoryService catService = CmsCategoryService.getInstance();
        Iterator<CmsResource> iGalleries = galleries.iterator();
        List<String> repositories = new ArrayList<String>();
        while (iGalleries.hasNext()) {
            CmsResource res = iGalleries.next();
            repositories.addAll(catService.getCategoryRepositories(cms, cms.getSitePath(res)));
        }
        List<CmsCategory> categories = null;
        try {
            categories = catService.readCategoriesForRepositories(cms, "", true, repositories);
        } catch (CmsException e) {
            // error reading categories
            LOG.error(e.getLocalizedMessage(), e);
        }
        return buildJSONForCategories(categories);
    }

    /**
     * Generates a JSON-Map for all available galleries for the given types.<p>
     * 
     * @param types the gallery-types
     * @param cms the cms-object
     * @return the galleries
     * @throws JSONException 
     */
    public JSONObject getGalleries(CmsObject cms, JSONArray types) throws JSONException {

        int[] typesArr = transformToIntArray(types);
        List<CmsResource> galleries = getGalleriesForTypes(cms, typesArr);
        return buildJSONForGalleries(cms, galleries);

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
        Action action = Action.valueOf(actionParam.toUpperCase());
        cms.getRequestContext().setLocale(CmsLocaleManager.getLocale(localeParam));
        JSONObject data = new JSONObject(request.getParameter(ReqParam.DATA.getName()));
        if (action.equals(Action.ALL)) {
            JSONArray types = data.getJSONArray(JsonKeys.TYPES.getName());
            int[] typesArr = transformToIntArray(types);
            List<CmsResource> galleries = getGalleriesForTypes(cms, typesArr);
            result.put(JsonKeys.GALLERIES.getName(), buildJSONForGalleries(cms, galleries));
            result.put(JsonKeys.CATEGORIES.getName(), getCategories(cms, galleries));
            // TODO: Add containers.
        } else if (action.equals(Action.CATEGORIES)) {
            result.put(JsonKeys.CATEGORIES.getName(), getCategories(cms));
        } else if (action.equals(Action.CONTAINERS)) {
            // TODO:  Will be implemented later.
        } else if (action.equals(Action.GALLERIES)) {
            result.put(JsonKeys.GALLERIES.getName(), getGalleries(cms, data.getJSONArray(JsonKeys.TYPES.getName())));
        } else if (action.equals(Action.SEARCH)) {
            JSONObject query = data.getJSONObject(JsonKeys.QUERYDATA.getName());
            result.put(JsonKeys.SEARCHRESULT.getName(), search(query));
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
    private JSONObject buildJSONForCategories(List<CmsCategory> categories) {

        JSONObject result = new JSONObject();
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
                result.put(cat.getPath(), jsonObj);
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
     * @param galleries the galleries
     * @return the JSON-Object
     */
    private JSONObject buildJSONForGalleries(CmsObject cms, List<CmsResource> galleries) {

        JSONObject result = new JSONObject();
        if ((galleries == null) || (galleries.size() == 0)) {
            return result;
        }
        Iterator<CmsResource> iGalleries = galleries.iterator();
        while (iGalleries.hasNext()) {
            JSONObject jsonObj = new JSONObject();
            CmsResource res = iGalleries.next();
            String sitePath = cms.getSitePath(res);
            String title = "";
            try {
                // read the gallery title
                title = cms.readPropertyObject(sitePath, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue("");
            } catch (CmsException e) {
                // error reading title property
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            try {
                jsonObj.put(JsonKeys.TITLE.getName(), title);
                // 2: gallery path
                jsonObj.put(JsonKeys.PATH.getName(), sitePath);
                // 3: active flag
                jsonObj.put(JsonKeys.TYPE.getName(), res.getTypeId());
                result.put(sitePath, jsonObj);
            } catch (JSONException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }

        }
        return result;
    }

    private JSONObject buildJSONForSearchResult(List<CmsSearchResult> searchResult) throws JSONException {

        JSONObject result = new JSONObject();
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

        }
        return result;
    }

    /**
     * Generates a list of available galleries for the given gallery-type.<p>
     * 
     * @param galleryTypeId the gallery-type
     * @param cms the cms-object
     * @return the galleries
     */
    private List<CmsResource> getGalleriesByType(CmsObject cms, int galleryTypeId) {

        List<CmsResource> galleries = new ArrayList<CmsResource>();
        try {
            // get the galleries of the current site
            galleries = cms.readResources("/", CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(galleryTypeId));
        } catch (CmsException e) {
            // error reading resources with filter
            LOG.error(e.getLocalizedMessage(), e);
        }

        // if the current site is NOT the root site - add all other galleries from the system path
        if (!cms.getRequestContext().getSiteRoot().equals("")) {
            List<CmsResource> systemGalleries = null;
            try {
                // get the galleries in the /system/ folder
                systemGalleries = cms.readResources(
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
     * @param cms the cms-object
     * @param types the gallery-types
     * @return the galleries
     */
    private List<CmsResource> getGalleriesForTypes(CmsObject cms, int[] types) {

        List<CmsResource> galleries = new ArrayList<CmsResource>();
        for (int i = 0; i < types.length; i++) {
            galleries.addAll(getGalleriesByType(cms, types[i]));
        }
        return galleries;
    }

    private JSONObject search(JSONObject query) throws JSONException {

        JSONObject result = new JSONObject();
        if (query == null) {
            return result;
        }
        JSONArray types = query.getJSONArray(JsonKeys.TYPES.getName());
        int[] typesArr = transformToIntArray(types);
        JSONArray galleries = query.getJSONArray(JsonKeys.GALLERIES.getName());
        String[] galleriesArr = transformToStringArray(galleries);
        JSONArray categories = query.getJSONArray(JsonKeys.CATEGORIES.getName());
        String[] categoriesArr = transformToStringArray(categories);
        String queryStr = query.getString(JsonKeys.QUERY.getName());
        CmsGallerySearch.SortParam sortOrder = CmsGallerySearch.SortParam.valueOf(query.getString(JsonKeys.SORTORDER.getName()));
        CmsGallerySearch.SortParam sortBy = CmsGallerySearch.SortParam.valueOf(query.getString(JsonKeys.SORTBY.getName()));
        int page = query.getInt(JsonKeys.PAGE.getName());
        CmsGallerySearch gSearch = new CmsGallerySearch();
        gSearch.setCategories(categoriesArr);
        gSearch.setGalleries(galleriesArr);
        gSearch.setResultPage(page);
        gSearch.setQuery(queryStr);
        gSearch.setTypes(typesArr);
        gSearch.setSortBy(sortBy);
        gSearch.setSortOrder(sortOrder);
        result.put("sortorder", gSearch.getSortOrder().getName());
        result.put("sortby", gSearch.getSortBy().getName());
        result.put("resultcount", gSearch.getSearchResultCount());
        result.put("resultpage", gSearch.getResultPage());
        result.put("resultpagecount", gSearch.getPageCount());
        result.put("resultlist", buildJSONForSearchResult(gSearch.getResult()));

        return result;
    }

}

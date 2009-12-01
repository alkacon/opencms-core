/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsGallerySearchServer.java,v $
 * Date   : $Date: 2009/12/01 13:03:29 $
 * Version: $Revision: 1.30 $
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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsLoaderException;
import org.opencms.loader.CmsResourceManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.CmsGallerySearch;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.editors.ade.A_CmsAjaxServer;
import org.opencms.workplace.editors.ade.CmsFormatterInfoBean;
import org.opencms.xml.containerpage.CmsADEManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Gallery search server used for client/server communication.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.30 $
 * 
 * @since 7.6
 */
public class CmsGallerySearchServer extends A_CmsAjaxServer {

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

        /** To get the preview html of a resource. */
        PREVIEW,

        /** To retrieve search results. */
        SEARCH,

        /** To set the resource properties. */
        SETPROPERTIES;
    }

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

    /** Gallery mode constants. */
    public enum GalleryMode {

        /** The advanced direct edit mode. */
        ADE("ade"),

        /** The sitemap editor mode. */
        SITEMAP("sitemap"),

        /** The FCKEditor mode. */
        EDITOR("editor"),

        /** The explorer mode. */
        VIEW("view"),

        /** The widget mode. */
        WIDGET("widget");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private GalleryMode(String name) {

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

    /** Json property name constants. */
    protected enum JsonKeys {

        /** The categories. */
        CATEGORIES("categories"),

        /** The client-side resource-id. */
        CLIENTID("clientid"),

        /** The content types. */
        CONTENTTYPES("contenttypes"),

        /** The date modified. */
        DATEMODIFIED("datemodified"),

        /** The errors. */
        ERRORS("errors"),

        /** The galleries. */
        GALLERIES("galleries"),

        /** The gallery types. */
        GALLERYTYPEID("gallerytypeid"),

        /** The icon. */
        ICON("icon"),

        /** The info. */
        INFO("info"),

        /** The item html-content. */
        ITEMHTML("itemhtml"),

        /** The level. */
        LEVEL("level"),

        /** The matches per page. */
        MATCHESPERPAGE("matchesperpage"),

        /** The name. */
        NAME("name"),

        /** The page. */
        PAGE("page"),

        /** The path. */
        PATH("path"),

        /** The preview data. */
        PREVIEWDATA("previewdata"),

        /** The properties. */
        PROPERTIES("properties"),

        /** The query-string. */
        QUERY("query"),

        /** The query data */
        QUERYDATA("querydata"),

        /** The resource path. */
        RESOURCEPATH("resourcepath"),

        /** The result count. */
        RESULTCOUNT("resultcount"),

        /** The result list. */
        RESULTLIST("resultlist"),

        /** The root-path. */
        ROOTPATH("rootpath"),

        /** The search result. */
        SEARCHRESULT("searchresult"),

        /** The sort-by param. */
        SORTBY("sortby"),

        /** The sort-order. */
        SORTORDER("sortorder"),

        /** The resource state. */
        STATE("state"),

        /** The tab-id. */
        TABID("tabid"),

        /** The title. */
        TITLE("title"),

        /** The type. */
        TYPE("type"),

        /** The type id. */
        TYPEID("typeid"),

        /** The type id's. */
        TYPEIDS("typeids"),

        /** The gallery-types. */
        TYPES("types"),

        /** The value. */
        VALUE("value");

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

        /** The current element. */
        CURRENTELEMENT("currentelement"),

        /** Generic data parameter. */
        DATA("data"),

        /** The dialog mode. */
        DIALOGMODE("dialogmode"),

        /** The current gallery item parameter. */
        GALLERYITEM("__galleryitem"),

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

    /** The advanced gallery path to the JSPs in the workplace. */
    public static final String ADVANCED_GALLERY_PATH = "/system/workplace/resources/editors/ade/galleries.jsp";

    /** The advanced gallery search server path to the JSP in the workplace. */
    public static final String ADVANCED_GALLERY_SERVER_PATH = "/system/workplace/galleries/gallerySearch.jsp";

    /** The excerpt field constant. */
    public static final String EXCERPT_FIELD_NAME = "excerpt";

    /** The result tab id. */
    public static final String RESULT_TAB_ID = "#tabs-result";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGallerySearchServer.class);

    /** The instance of the resource manager. */
    CmsResourceManager m_resourceManager;

    /** The current instance of the cms object. */
    private CmsObject m_cms;

    /** The users locale. */
    private Locale m_locale;

    /** The JSON data request object. */
    private JSONObject m_reqDataObj;

    /** The default matchers per search result page. */
    private static final int MATCHES_PER_PAGE = 8;

    /**
     * Empty constructor, required for every JavaBean.
     */
    public CmsGallerySearchServer() {

        super();
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
        m_locale = getWorkplaceLocale();
    }

    /**
     * @see org.opencms.jsp.CmsJspBean#init(javax.servlet.jsp.PageContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void init(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super.init(context, req, res);
        m_cms = getCmsObject();
        m_locale = getWorkplaceLocale();
    }

    /**
     * Handles all publish related requests.<p>
     * 
     * @return the result
     * 
     * @throws Exception if there is a problem
     */
    @Override
    public JSONObject executeAction() throws Exception {

        JSONObject result = new JSONObject();
        HttpServletRequest request = getRequest();
        if (checkParameters(request, result, ReqParam.ACTION.getName(), ReqParam.DATA.getName())) {
            String actionParam = request.getParameter(ReqParam.ACTION.getName());
            String localeParam = request.getParameter(ReqParam.LOCALE.getName());
            String dataParam = request.getParameter(ReqParam.DATA.getName());
            Action action = Action.valueOf(actionParam.toUpperCase());
            m_cms.getRequestContext().setLocale(CmsLocaleManager.getLocale(localeParam));
            JSONObject data = new JSONObject(dataParam);
            if (action.equals(Action.ALL)) {
                JSONArray resourceTypesParam = data.getJSONArray(JsonKeys.TYPES.getName());

                result.merge(getAllLists(resourceTypesParam, null), true, true);

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
            } else if (action.equals(Action.PREVIEW)) {
                String resourcePath = data.getString(JsonKeys.PATH.getName());
                result.put(JsonKeys.PREVIEWDATA.getName(), getPreviewData(resourcePath));
            } else if (action.equals(Action.SETPROPERTIES)) {
                String resourcePath = data.getString(JsonKeys.PATH.getName());
                JSONArray properties = data.getJSONArray(JsonKeys.PROPERTIES.getName());
                result.put(JsonKeys.PREVIEWDATA.getName(), setProperties(resourcePath, properties));
            }
        }
        return result;
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
                CmsFormatterInfoBean formatterInfo = new CmsFormatterInfoBean(
                    OpenCms.getResourceManager().getResourceType("folder"),
                    false);
                // 1: category title
                jsonObj.put(JsonKeys.TITLE.getName(), cat.getTitle());
                formatterInfo.setTitleInfo(JsonKeys.TITLE.getName(), JsonKeys.TITLE.getName(), cat.getTitle());

                // 2: category path
                jsonObj.put(JsonKeys.PATH.getName(), cat.getPath());
                formatterInfo.setSubTitleInfo(JsonKeys.PATH.getName(), JsonKeys.PATH.getName(), cat.getPath());
                // 3: category root path
                jsonObj.put(JsonKeys.ROOTPATH.getName(), cat.getRootPath());
                // 4 category level
                jsonObj.put(JsonKeys.LEVEL.getName(), CmsResource.getPathLevel(cat.getPath()));
                String iconPath = CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES
                    + OpenCms.getWorkplaceManager().getExplorerTypeSetting("folder").getIcon());
                formatterInfo.setIcon(iconPath);
                jsonObj.put(JsonKeys.ITEMHTML.getName(), getFormattedListContent(formatterInfo));
                result.put(jsonObj);
            } catch (Exception e) {
                // TODO: Improve error handling
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
                    CmsFormatterInfoBean formatterInfo = new CmsFormatterInfoBean(tInfo.getResourceType(), false);
                    formatterInfo.setResource(res);
                    jsonObj.put(JsonKeys.CONTENTTYPES.getName(), contentTypes);
                    jsonObj.put(JsonKeys.TITLE.getName(), title);
                    formatterInfo.setTitleInfo(JsonKeys.TITLE.getName(), JsonKeys.TITLE.getName(), title);

                    // 2: gallery path
                    jsonObj.put(JsonKeys.PATH.getName(), sitePath);
                    formatterInfo.setSubTitleInfo(JsonKeys.PATH.getName(), JsonKeys.PATH.getName(), sitePath);
                    formatterInfo.setIcon(iconPath);
                    // 3: active flag
                    jsonObj.put(JsonKeys.GALLERYTYPEID.getName(), tInfo.getResourceType().getTypeId());
                    jsonObj.put(JsonKeys.ICON.getName(), iconPath);
                    jsonObj.put(JsonKeys.ITEMHTML.getName(), getFormattedListContent(formatterInfo));
                } catch (Exception e) {
                    // TODO: Improve error handling
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                result.put(jsonObj);
            }
        }
        return result;
    }

    /**
     * Returns the JSON-Object for the given list of search-results.<p>
     * 
     * @param searchResult the search-result-list
     * @return the JSON representation of the search-result
     */
    private JSONArray buildJSONForSearchResult(List<CmsSearchResult> searchResult) {

        JSONArray result = new JSONArray();
        if ((searchResult == null) || (searchResult.size() == 0)) {
            return result;
        }
        Iterator<CmsSearchResult> iSearchResult = searchResult.iterator();
        while (iSearchResult.hasNext()) {
            try {
                CmsSearchResult sResult = iSearchResult.next();
                JSONObject resultEntry = new JSONObject();
                String path = sResult.getPath();
                path = getRequestContext().removeSiteRoot(path);
                String fileIcon = getFileIconName(path);
                String iconPath = CmsWorkplace.RES_PATH_FILETYPES;
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(fileIcon)) {
                    iconPath += OpenCms.getWorkplaceManager().getExplorerTypeSetting(sResult.getDocumentType()).getIcon();
                } else {
                    iconPath += "mimetype/" + fileIcon;
                }
                iconPath = CmsWorkplace.getResourceUri(iconPath);
                resultEntry.put(JsonKeys.DATEMODIFIED.getName(), sResult.getDateLastModified());
                resultEntry.put(JsonKeys.TITLE.getName(), sResult.getField(CmsSearchField.FIELD_TITLE));
                resultEntry.put(JsonKeys.INFO.getName(), sResult.getField(CmsSearchField.FIELD_DESCRIPTION));
                resultEntry.put(JsonKeys.TYPE.getName(), sResult.getDocumentType());
                resultEntry.put(JsonKeys.PATH.getName(), path);
                resultEntry.put(JsonKeys.ICON.getName(), iconPath);

                // TODO: the resource-id should be read from the search-result object once this info has been added to the index
                resultEntry.put(JsonKeys.CLIENTID.getName(), OpenCms.getADEManager().convertToClientId(
                    m_cms.readResource(path).getStructureId()));

                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(sResult.getDocumentType());
                CmsFormatterInfoBean formatterInfo = new CmsFormatterInfoBean(type, false);
                formatterInfo.setTitleInfo(
                    JsonKeys.TITLE.getName(),
                    JsonKeys.TITLE.getName(),
                    sResult.getField(CmsSearchField.FIELD_TITLE));
                formatterInfo.setSubTitleInfo(
                    JsonKeys.TYPE.getName(),
                    JsonKeys.TYPE.getName(),
                    CmsWorkplaceMessages.getResourceTypeName(m_locale, type.getTypeName()));
                formatterInfo.setIcon(iconPath);
                formatterInfo.addAdditionalInfo(
                    EXCERPT_FIELD_NAME,
                    OpenCms.getWorkplaceManager().getMessages(m_locale).key(Messages.GUI_LABEL_EXCERPT),
                    sResult.getExcerpt());
                resultEntry.put(JsonKeys.ITEMHTML.getName(), getFormattedListContent(formatterInfo));
                result.put(resultEntry);
            } catch (Exception e) {
                // TODO: Improve error handling
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * Generates a JSON-Map for all available content types.
     * 
     * @param types the gallery-types
     * 
     * @return the content-types
     * 
     * @throws CmsLoaderException if something goes wrong
     */
    private JSONArray buildJSONForTypes(List<I_CmsResourceType> types) {

        JSONArray result = new JSONArray();
        if (types == null) {
            return result;
        }
        Iterator<I_CmsResourceType> it = types.iterator();
        while (it.hasNext()) {
            I_CmsResourceType type = it.next();
            CmsFormatterInfoBean formatterInfo = new CmsFormatterInfoBean(type, true);
            try {
                JSONObject jType = new JSONObject();
                jType.put(JsonKeys.TITLE.getName(), CmsWorkplaceMessages.getResourceTypeName(
                    m_locale,
                    type.getTypeName()));
                formatterInfo.setTitleInfo(
                    JsonKeys.TITLE.getName(),
                    JsonKeys.TITLE.getName(),
                    CmsWorkplaceMessages.getResourceTypeName(m_locale, type.getTypeName()));
                jType.put(JsonKeys.TYPEID.getName(), type.getTypeId());
                jType.put(JsonKeys.TYPE.getName(), type.getTypeName());
                jType.put(JsonKeys.INFO.getName(), CmsWorkplaceMessages.getResourceTypeDescription(
                    m_locale,
                    type.getTypeName()));
                formatterInfo.setSubTitleInfo(
                    JsonKeys.INFO.getName(),
                    JsonKeys.INFO.getName(),
                    CmsWorkplaceMessages.getResourceTypeDescription(m_locale, type.getTypeName()));
                String iconPath = CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES
                    + OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName()).getIcon());
                jType.put(JsonKeys.ICON.getName(), iconPath);
                formatterInfo.setIcon(iconPath);
                JSONArray galleryIds = new JSONArray();
                Iterator<I_CmsResourceType> galleryTypes = type.getGalleryTypes().iterator();
                while (galleryTypes.hasNext()) {
                    I_CmsResourceType galleryType = galleryTypes.next();
                    galleryIds.put(galleryType.getTypeId());

                }
                jType.put(JsonKeys.GALLERYTYPEID.getName(), galleryIds);
                jType.put(JsonKeys.ITEMHTML.getName(), getFormattedListContent(formatterInfo));
                result.put(jType);
            } catch (Exception e) {
                // TODO: Improve error handling
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }

        return result;
    }

    /**
     * Returns the search result page for a given resource, or an empty JSON object if result page could not be found(This should never happen).<p>
     * 
     * @param resourceName the resource name
     * 
     * @return the search result page containing the given resource
     * 
     * @throws JSONException if something goes wrong 
     */
    public JSONObject findResourceInGallery(String resourceName) throws JSONException {

        CmsResource resource = null;
        try {
            resource = m_cms.readResource(resourceName);
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        if (resource == null) {
            return new JSONObject();
        }
        String rootPath = resource.getRootPath();
        JSONObject queryData = new JSONObject();
        JSONArray types = new JSONArray();
        types.put(resource.getTypeId());
        queryData.put(JsonKeys.TYPES.getName(), types);
        JSONArray galleries = new JSONArray();
        galleries.put(CmsResource.getFolderPath(resourceName));
        queryData.put(JsonKeys.GALLERIES.getName(), galleries);
        queryData.put(JsonKeys.CATEGORIES.getName(), new JSONArray());
        queryData.put(JsonKeys.MATCHESPERPAGE.getName(), MATCHES_PER_PAGE);
        queryData.put(JsonKeys.QUERY.getName(), "");
        queryData.put(JsonKeys.SORTORDER.getName(), CmsGallerySearch.SortParam.DEFAULT.getName());
        int currentPage = 1;
        boolean found = false;
        queryData.put(JsonKeys.PAGE.getName(), currentPage);
        CmsSearchParameters params = prepareSearchParams(queryData);
        CmsSearch searchBean = new CmsSearch();
        searchBean.init(m_cms);
        searchBean.setParameters(params);
        List<CmsSearchResult> searchResults = null;
        while (!found) {
            searchBean.setSearchPage(currentPage);
            searchResults = searchBean.getSearchResult();
            Iterator<CmsSearchResult> resultsIt = searchResults.iterator();
            while (resultsIt.hasNext()) {
                CmsSearchResult searchResult = resultsIt.next();
                if (searchResult.getPath().equals(rootPath)) {
                    found = true;
                    break;
                }
            }
            if (!found && (searchBean.getSearchResultCount() / (currentPage * searchBean.getMatchesPerPage()) >= 1)) {
                currentPage++;
            } else {
                break;
            }
        }
        JSONObject result = new JSONObject();
        if (found) {
            JSONObject sResult = new JSONObject();
            sResult.put(JsonKeys.SORTORDER.getName(), searchBean.getSortOrder());
            sResult.put(JsonKeys.RESULTCOUNT.getName(), searchBean.getSearchResultCount());
            sResult.put(JsonKeys.PAGE.getName(), searchBean.getSearchPage());
            sResult.put(JsonKeys.RESULTLIST.getName(), buildJSONForSearchResult(searchResults));
            queryData.put(JsonKeys.PAGE.getName(), currentPage);
            queryData.put(JsonKeys.TABID.getName(), RESULT_TAB_ID);
            result.put(JsonKeys.QUERYDATA.getName(), queryData);
            result.put(JsonKeys.SEARCHRESULT.getName(), sResult);
        }

        return result;
    }

    /**
     * Returns the JSON for type, galleries and categories tab. Uses the given types or all available resource types.<p>
     * 
     * @param typeIds the requested resource type id's
     * @param modeName the dialog mode name
     * 
     * @return available types, galleries and categories as JSON 
     * 
     * @throws JSONException if something goes wrong generating the JSON
     */
    public JSONObject getAllLists(JSONArray typeIds, String modeName) throws JSONException {

        JSONObject result = new JSONObject();
        // using all available types if typeIds is null or empty
        List<I_CmsResourceType> resourceTypes;
        if ((typeIds == null) || (typeIds.length() == 0)) {
            resourceTypes = getResourceManager().getResourceTypes();
            typeIds = new JSONArray();
            for (I_CmsResourceType type : resourceTypes) {
                typeIds.put(type.getTypeId());
            }
        } else {
            resourceTypes = readContentTypes(typeIds);
        }
        result.put(JsonKeys.TYPES.getName(), buildJSONForTypes(resourceTypes));
        result.put(JsonKeys.TYPEIDS.getName(), typeIds);
        Map<String, CmsGalleryTypeInfo> galleryTypes = readGalleryTypes(resourceTypes);

        result.put(JsonKeys.GALLERIES.getName(), buildJSONForGalleries(galleryTypes));
        List<CmsResource> galleryFolders = new ArrayList<CmsResource>();
        Iterator<Entry<String, CmsGalleryTypeInfo>> iGalleryTypes = galleryTypes.entrySet().iterator();
        while (iGalleryTypes.hasNext()) {
            galleryFolders.addAll(iGalleryTypes.next().getValue().getGalleries());
        }
        result.put(JsonKeys.CATEGORIES.getName(), buildJSONForCategories(readCategories(galleryFolders)));
        return result;
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
     * Returns the JSON-data for the preview of a resource.<p>
     * 
     * @param resourcePath the resource site-path
     * @return the JSON-data
     * @throws Exception if something goes wrong
     */
    private JSONObject getPreviewData(String resourcePath) throws Exception {

        JSONObject result = new JSONObject();

        // getting formatted content
        Map<String, Object> reqAttributes = new HashMap<String, Object>();
        CmsResource resource = m_cms.readResource(resourcePath);
        I_CmsResourceType type = getResourceManager().getResourceType(resource.getTypeId());
        CmsGalleryItemBean reqItem = new CmsGalleryItemBean(resource);
        reqItem.setTypeId(resource.getTypeId());
        reqItem.setTypeName(type.getTypeName());
        reqAttributes.put(ReqParam.GALLERYITEM.getName(), reqItem);
        result.put(JsonKeys.ITEMHTML.getName(), type.getFormattedContent(
            m_cms,
            getRequest(),
            getResponse(),
            I_CmsResourceType.Formatter.GALLERY_PREVIEW,
            reqAttributes));
        result.put(JsonKeys.STATE.getName(), resource.getState().getState());

        // reading default explorer-type properties
        JSONArray propertiesJSON = new JSONArray();
        List<String> properties = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName()).getProperties();
        Iterator<String> propIt = properties.iterator();
        while (propIt.hasNext()) {
            String propertyName = propIt.next();
            CmsProperty property = m_cms.readPropertyObject(resource, propertyName, false);
            JSONObject propertyJSON = new JSONObject();
            propertyJSON.put(JsonKeys.NAME.getName(), propertyName);
            propertyJSON.put(JsonKeys.VALUE.getName(), property.getValue());
            propertiesJSON.put(propertyJSON);
        }
        result.put(JsonKeys.PROPERTIES.getName(), propertiesJSON);
        return result;

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
     * Returns the search parameters for the given query data.<p>
     * 
     * @param queryData the query data
     * 
     * @return the prepared search parameters
     * @throws JSONException if something goes wrong reading the JSON
     */
    private CmsSearchParameters prepareSearchParams(JSONObject queryData) throws JSONException {

        JSONArray types = queryData.getJSONArray(JsonKeys.TYPES.getName());
        JSONArray galleries = queryData.getJSONArray(JsonKeys.GALLERIES.getName());
        List<String> galleriesList = transformToStringList(galleries);
        JSONArray categories = queryData.getJSONArray(JsonKeys.CATEGORIES.getName());
        List<String> categoriesList = transformToStringList(categories);
        String queryStr = queryData.getString(JsonKeys.QUERY.getName());
        int matches = queryData.getInt(JsonKeys.MATCHESPERPAGE.getName());
        //        CmsGallerySearch.SortParam sortOrder = CmsGallerySearch.SortParam.DEFAULT;
        //        try {
        //            sortOrder = CmsGallerySearch.SortParam.valueOf(query.getString(JsonKeys.SORTORDER.getName()).toUpperCase());
        //        } catch (Exception e) {
        //            //may happen
        //        }
        int page = queryData.getInt(JsonKeys.PAGE.getName());

        List<String> typeNames = new ArrayList<String>();

        try {
            typeNames = getTypeNames(types);
        } catch (CmsLoaderException e) {
            // TODO: Improve error handling
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        // TODO: searching the old index, replace with new search
        CmsUser user = m_cms.getRequestContext().currentUser();
        String indexName = new CmsUserSettings(user).getWorkplaceSearchIndexName();

        CmsSearchParameters params = new CmsSearchParameters();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(queryStr)) {
            params.setIgnoreQuery(true);
        } else {
            params.setQuery(queryStr);
        }
        params.setSort(CmsSearchParameters.SORT_TITLE);
        params.setIndex(indexName);
        params.setMatchesPerPage(matches);
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
        return params;
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
     * Reads the resource-types for given resource-type-id's.
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
     * Returns the search result.<p>
     * 
     * @param query the query parameters
     * @return the search result
     * @throws JSONException if something goes wrong
     */
    public JSONObject search(JSONObject query) throws JSONException {

        JSONObject result = new JSONObject();
        if (query == null) {
            return result;
        }

        // search
        CmsSearch searchBean = new CmsSearch();
        searchBean.init(m_cms);
        searchBean.setParameters(prepareSearchParams(query));
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

    /**
     * Sets the properties for a resource and returns the updated preview data.<p>
     * 
     * @param resourcePath the site-path of the resource
     * @param properties the properties to set as JSON-data
     * @return the preview data
     * @throws Exception if something goes wrong
     */
    private JSONObject setProperties(String resourcePath, JSONArray properties) throws Exception {

        CmsResource resource = m_cms.readResource(resourcePath);
        if (properties != null) {
            for (int i = 0; i < properties.length(); i++) {
                String propertyName = properties.getJSONObject(i).getString(JsonKeys.NAME.getName());
                String propertyValue = properties.getJSONObject(i).getString(JsonKeys.VALUE.getName());
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(propertyValue)) {
                    propertyValue = null;
                }
                try {
                    CmsProperty currentProperty = m_cms.readPropertyObject(resource, propertyName, false);
                    // detect if property is a null property or not
                    if (currentProperty.isNullProperty()) {
                        // create new property object and set key and value
                        currentProperty = new CmsProperty();
                        currentProperty.setName(propertyName);
                        if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                            // set structure value
                            currentProperty.setStructureValue(propertyValue);
                            currentProperty.setResourceValue(null);
                        } else {
                            // set resource value
                            currentProperty.setStructureValue(null);
                            currentProperty.setResourceValue(propertyValue);
                        }
                    } else if (currentProperty.getStructureValue() != null) {
                        // structure value has to be updated
                        currentProperty.setStructureValue(propertyValue);
                        currentProperty.setResourceValue(null);
                    } else {
                        // resource value has to be updated
                        currentProperty.setStructureValue(null);
                        currentProperty.setResourceValue(propertyValue);
                    }
                    CmsLock lock = m_cms.getLock(resource);
                    if (lock.isUnlocked()) {
                        // lock resource before operation
                        m_cms.lockResource(resourcePath);
                    }
                    // write the property to the resource
                    m_cms.writePropertyObject(resourcePath, currentProperty);
                    // unlock the resource
                    m_cms.unlockResource(resourcePath);
                } catch (CmsException e) {
                    // writing the property failed, log error
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }

        }
        return getPreviewData(resourcePath);
    }

    /**
     * Transforms an <code>JSONArray</code> to a <code>List&lt;String&gt;</code>.
     * Returns null if the JSON is null or empty.<p>
     * 
     * @param arr
     * @return the resulting list
     * @throws JSONException if something goes wrong
     */
    private List<String> transformToStringList(JSONArray arr) throws JSONException {

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
     * Returns the rendered item html.
     * 
     * @param type the resource-type
     * @param title the title
     * @param subtitle the subtitle
     * @param iconPath the icon path
     * @param searchResult the search-result if applicable 
     * @return the html string
     * @throws UnsupportedEncodingException if something goes wrong
     * @throws ServletException if something goes wrong
     * @throws IOException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    private String getFormattedListContent(CmsFormatterInfoBean formatterInfo)
    throws UnsupportedEncodingException, ServletException, IOException, CmsException {

        Map<String, Object> reqAttributes = new HashMap<String, Object>();
        reqAttributes.put(CmsADEManager.ATTR_FORMATTER_INFO, formatterInfo);
        return formatterInfo.getResourceType().getFormattedContent(
            m_cms,
            getRequest(),
            getResponse(),
            I_CmsResourceType.Formatter.GALLERY_LIST,
            reqAttributes);
    }

    /**
     * Generates the java-script script-tags necessary for the given resource types.
     * Providing extra functionality within the galleries.<p>
     * 
     * @param cms the current instance of the cms object 
     * @param types the resource types
     * @return the script tags to include into the gallery page
     */
    public static String getJSIncludeForTypes(CmsObject cms, List<I_CmsResourceType> types) {

        StringBuffer result = new StringBuffer(32);
        Iterator<I_CmsResourceType> typeIt = types.iterator();
        while (typeIt.hasNext()) {
            I_CmsResourceType type = typeIt.next();
            String jsPath = type.getConfiguration().get("js.path");
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(jsPath) && cms.existsResource(jsPath)) {
                result.append("<script type=\"text/javascript\" src=\"");
                result.append(CmsWorkplace.getResourceUri(jsPath));
                result.append("\"></script>\n");
            }
        }
        return result.toString();
    }

    /**
     * Returns the request data parameter as JSON.<p> 
     * 
     * @return the JSON object
     * @throws JSONException if something goes wrong parsing the parameter string
     */
    private JSONObject getReqDataObj() throws JSONException {

        if (m_reqDataObj == null) {
            String dataParam = getRequest().getParameter(ReqParam.DATA.getName());
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(dataParam)) {
                m_reqDataObj = new JSONObject(dataParam);
            }
        }
        return m_reqDataObj;
    }

    /**
     * Returns the URI of the gallery JSP.<p>
     * 
     * @return the URI string
     */
    public String getGalleryUri() {

        return link(ADVANCED_GALLERY_PATH);
    }

    /**
     * Returns the URI of the gallery server JSP.<p>
     * 
     * @return the URI string
     */
    public String getGalleryServerUri() {

        return link(ADVANCED_GALLERY_SERVER_PATH);
    }

    /**
     * Returns the JSON as string for type, galleries and categories tab. Uses the given types or all available resource types.<p>
     * 
     * @return the type, galleries and categories data as JSON string
     * 
     * @throws JSONException if something goes wrong
     */
    public String getListConfig() throws JSONException {

        JSONArray resourceTypesParam = null;
        try {
            JSONObject data = getReqDataObj();
            if ((data != null) && data.has(JsonKeys.TYPES.getName())) {
                resourceTypesParam = data.getJSONArray(JsonKeys.TYPES.getName());
            }
        } catch (JSONException e) {
            // TODO: improve error handling
            LOG.error(e.getLocalizedMessage(), e);
        }
        JSONObject result = getAllLists(resourceTypesParam, getModeName());
        return result.toString();
    }

    /**
     * Returns the JSON as string for the initial search. All search parameters are taken from the request parameters.<p>
     * 
     * @return the search result data as JSON string
     */
    public String getInitialSearch() {

        JSONObject result = new JSONObject();

        try {
            JSONObject data = getReqDataObj();
            if ((data != null) && data.has(JsonKeys.RESOURCEPATH.getName())) {
                String resourcePath = data.getString(JsonKeys.RESOURCEPATH.getName());
                result = findResourceInGallery(resourcePath);
            } else if ((data != null) && data.has(JsonKeys.QUERYDATA.getName())) {
                JSONObject queryData = data.getJSONObject(JsonKeys.QUERYDATA.getName());
                result.put(JsonKeys.QUERYDATA.getName(), queryData);
                result.put(JsonKeys.SEARCHRESULT.getName(), search(queryData));
            }
        } catch (JSONException e) {
            // TODO: improve error handling
            LOG.error(e.getLocalizedMessage(), e);
        }
        return result.toString();
    }

    /**
     * Returns the current dialog mode name.<p>
     * 
     * @return the mode name
     */
    public String getModeName() {

        // TODO: read request parameter
        return "";
    }
}

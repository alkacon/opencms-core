/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEServer.java,v $
 * Date   : $Date: 2009/10/21 16:07:38 $
 * Version: $Revision: 1.1.2.37 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors.ade;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.CmsSearchResult;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.containerpage.CmsADEManager;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsSearchOptions;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * ADE server used for client/server communication.<p>
 * 
 * see jsp files under <tt>/system/workplace/editors/ade/</tt>.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.37 $
 * 
 * @since 7.6
 */
public class CmsADEServer extends CmsJspActionElement {

    /** Request parameter action value constants. */
    protected enum Action {
        /** First call to get all the data. */
        ALL,
        /** To save the container page. */
        CNT,
        /** To delete elements. */
        DEL,
        /** To get element information. */
        ELEM,
        /** To get an element formatted with the given properties. */
        ELEMPROPS,
        /** To get or save the favorites list. */
        FAV,
        /** To get the last search results. */
        LS,
        /** To create a new element. */
        NEW,
        /** To retrieve the configured properties. */
        PROPS,
        /** To publish. */
        PUBLISH,
        /** To retrieve the publish list. */
        PUBLISH_LIST,
        /** To retrieve or save the recent list. */
        REC,
        /** To retrieve search results. */
        SEARCH;
    }

    /** Json property name constants for container elements. */
    protected enum JsonCntElem {

        /** The formatter's uri. */
        FORMATTER("formatter"),
        /** The element id. */
        ID("id"),
        /** The element's uri. */
        URI("uri");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonCntElem(String name) {

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

    /** Json property name constants for container pages. */
    protected enum JsonCntPage {

        /** Boolean value indicating if edition is allowed. */
        ALLOW_EDIT("allowEdit"),
        /** The list of containers. */
        CONTAINERS("containers"),
        /** The list of elements. */
        ELEMENTS("elements"),
        /** The favorites list. */
        FAVORITES("favorites"),
        /** The locale. */
        LOCALE("locale"),
        /** The name of the user that has locked the resource. */
        LOCKED("locked"),
        /** The order of creatable elements. */
        NEWORDER("newOrder"),
        /** The recent list. */
        RECENT("recent"),
        /** The max size of the recent list. */
        RECENT_LIST_SIZE("recentListSize"),
        /** The order of searchable resource types. */
        SEARCH_ORDER("searchOrder"),
        /** The container page state. */
        STATE("state");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonCntPage(String name) {

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

    /** Json property name constants for containers. */
    protected enum JsonContainer {

        /** The list of elements. */
        ELEMENTS("elements"),
        /** The max allowed number of elements in the container. */
        MAXELEMENTS("maxElem"),
        /** The container name. */
        NAME("name"),
        /** The object type: container. */
        OBJTYPE("objtype"),
        /** The container type. */
        TYPE("type");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonContainer(String name) {

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

    /** Json property name constants for new created resources. */
    protected enum JsonNewRes {

        /** The resource's structure id. */
        ID("id"),
        /** The resource's uri. */
        URI("uri");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonNewRes(String name) {

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

    /** Json property name constants for resources. */
    protected enum JsonResource {

        /** The resource type icon path. */
        ICON("icon"),
        /** The structure id. */
        ID("id"),
        /** The additional information name. */
        INFO_NAME("infoName"),
        /** The additional information value. */
        INFO_VALUE("infoValue"),
        /** The name of the user that has locked the resource. */
        LOCKED_BY("lockedBy"),
        /** The reason a resource can not be published. */
        REASON("reason"),
        /** The resource state. */
        STATE("state"),
        /** Resource title. */
        TITLE("title"),
        /** Resource uri. */
        URI("uri");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonResource(String name) {

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

    /** Json property name constants for responses. */
    protected enum JsonResponse {

        /** List of elements. */
        ELEMENTS("elements"),
        /** The favorites list. */
        FAVORITES("favorites"),
        /** The recent list. */
        RECENT("recent"),
        /** A list of resources. */
        RESOURCES("resources");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonResponse(String name) {

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

    /** Json property name constants for resource type information. */
    protected enum JsonResType {

        /** The element's resource type. */
        TYPE("type"),
        /** The element's resource type nice name. */
        TYPENAME("typename");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonResType(String name) {

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

    /** Json property name constants for searching. */
    protected enum JsonSearch {

        /** The number of search results. */
        COUNT("count"),
        /** If the search has more results. */
        HASMORE("hasmore"),
        /** The search location. */
        LOCATION("location"),
        /** Search query. */
        TEXT("text"),
        /** The resource type(s). */
        TYPE("type");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonSearch(String name) {

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

    /** Reason value constants, when resources can not be published. */
    protected enum NoPubReason {

        /** Resource is locked by another user. */
        LOCKED("locked"),
        /** User does not have enough permissions. */
        PERMISSIONS("perm"),
        /** Resource has been already published. */
        PUBLISHED("pub");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private NoPubReason(String name) {

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
        /** The container uri. */
        CNTPAGE("cntpage"),
        /** Generic data parameter. */
        DATA("data"),
        /** Element id or list of element ids. */
        ELEM("elem"),
        /** The current locale. */
        LOCALE("locale"),
        /** Elements properties. */
        PROPERTIES("properties"),
        /** Element uri. */
        URI("uri");

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

    /** Request parameter name constants for publishing. */
    protected enum ParamPublish {

        /** Flag to indicate if to publish with related resources. */
        RELATED("related"),
        /** The resources to remove from the publish list. */
        REMOVE_RESOURCES("remove-resources"),
        /** The resources to publish. */
        RESOURCES("resources"),
        /** Flag to indicate if to publish with siblings. */
        SIBLINGS("siblings");

        /** Parameter name. */
        private String m_name;

        /** Constructor.<p> */
        private ParamPublish(String name) {

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

    /** State Constant for client-side element type 'new element configuration'. */
    public static final String ELEMENT_NEWCONFIG = "NC";

    /** Additional info key constant. */
    public static final String INFO_PUBLISH_LIST = "__INFO_PUBLISH_LIST__";

    /** Mime type constant. */
    public static final String MIMETYPE_APPLICATION_JSON = "application/json";

    /** Request path constant. */
    public static final String REQUEST_GET = "/system/workplace/editors/ade/get.jsp";

    /** Request path constant. */
    public static final String REQUEST_SET = "/system/workplace/editors/ade/set.jsp";

    /** JSON response property constant. */
    public static final String RES_ERROR = "error";

    /** JSON response property constant. */
    public static final String RES_STATE = "state";

    /** JSON response state value constant. */
    public static final String RES_STATE_ERROR = "error";

    /** JSON response state value constant. */
    public static final String RES_STATE_OK = "ok";

    /** Session attribute name constant. */
    public static final String SESSION_ATTR_ADE_CACHE = "__OCMS_ADE_CACHE__";

    /** JSON response state value constant. */
    public static final String TYPE_CONTAINER = "Container";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEServer.class);

    /** The ADE manager instance. */
    private CmsADEManager m_manager;

    /** The session cache. */
    private CmsADESessionCache m_sessionCache;

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsADEServer(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
        m_manager = OpenCms.getADEManager();
        m_sessionCache = (CmsADESessionCache)req.getSession().getAttribute(SESSION_ATTR_ADE_CACHE);
        if (m_sessionCache == null) {
            m_sessionCache = new CmsADESessionCache(getCmsObject());
            req.getSession().setAttribute(SESSION_ATTR_ADE_CACHE, m_sessionCache);
        }
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
        try {
            // handle request depending on the requested jsp
            String action = getRequest().getPathInfo();
            if (action.equals(REQUEST_GET)) {
                result = executeActionGet();
            } else if (action.equals(REQUEST_SET)) {
                result = executeActionSet();
            } else {
                result.put(RES_ERROR, Messages.get().getBundle().key(Messages.ERR_JSON_INVALID_ACTION_URL_1, action));
            }
        } catch (Exception e) {
            // a serious error occurred, should not...
            result.put(RES_ERROR, e.getLocalizedMessage() == null ? "NPE" : e.getLocalizedMessage());
            LOG.error(Messages.get().getBundle().key(
                Messages.ERR_SERVER_EXCEPTION_1,
                CmsRequestUtil.appendParameters(
                    getRequest().getRequestURL().toString(),
                    CmsRequestUtil.createParameterMap(getRequest().getQueryString()),
                    false)), e);
        }
        // add state info
        if (result.has(RES_ERROR)) {
            // add state=error in case an error occurred 
            result.put(RES_STATE, RES_STATE_ERROR);
        } else if (!result.has(RES_STATE)) {
            // add state=ok i case no error occurred
            result.put(RES_STATE, RES_STATE_OK);
        }
        // write the result
        result.write(getResponse().getWriter());
    }

    /**
     * Returns a list of container elements from a json array with client ids.<p>
     * 
     * @param array the json array
     * 
     * @return a list of resource ids
     * 
     * @throws JSONException if something goes wrong
     * @throws CmsException 
     */
    protected List<CmsContainerElementBean> arrayToElementList(JSONArray array) throws JSONException, CmsException {

        List<CmsContainerElementBean> result = new ArrayList<CmsContainerElementBean>(array.length());
        for (int i = 0; i < array.length(); i++) {
            String id = array.getString(i);
            try {
                result.add(getCachedElement(id));
            } catch (CmsIllegalArgumentException e) {
                if (!LOG.isDebugEnabled()) {
                    LOG.warn(e.getLocalizedMessage());
                }
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Checks whether a list of parameters are present as attributes of a request.<p>
     * 
     * If this isn't the case, an error message is written to the JSON result object.
     * 
     * @param request the request which contains the parameters
     * @param result the JSON object which the error message should be written into, can be <code>null</code>
     * @param params the array of parameter names which should be checked
     * 
     * @return true if and only if all parameters are present in the request
     * 
     * @throws JSONException if something goes wrong with JSON
     */
    protected boolean checkParameters(HttpServletRequest request, JSONObject result, String... params)
    throws JSONException {

        for (String param : params) {
            String value = request.getParameter(param);
            if (value == null) {
                if (result != null) {
                    storeErrorMissingParam(result, param);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Compares two search option objects.<p>
     * 
     * Better than to implement the {@link CmsSearchOptions#equals(Object)} method,
     * since the page number is not considered in this comparison.<p>
     * 
     * @param o1 the first search option object
     * @param o2 the first search option object
     * 
     * @return <code>true</code> if they are equal
     */
    protected boolean compareSearchOptions(CmsSearchOptions o1, CmsSearchOptions o2) {

        if (o1 == o2) {
            return true;
        }
        if ((o1 == null) || (o2 == null)) {
            return false;
        }
        if (!o1.getLocation().equals(o2.getLocation())) {
            return false;
        }
        if (!o1.getText().equals(o2.getText())) {
            return false;
        }
        if (!o1.getType().equals(o2.getType())) {
            return false;
        }
        return true;

    }

    /**
     * Creates a new CmsContainerElementBean for a structure-id and given properties.<p> 
     * 
     * @param structureId the structure-id
     * @param properties the properties-map
     * 
     * @return the element bean
     * 
     * @throws CmsException if something goes wrong reading the element resource
     * @throws JSONException if something goes wrong parsing JSON
     */
    protected CmsContainerElementBean createElement(CmsUUID structureId, JSONObject properties)
    throws CmsException, JSONException {

        Map<String, String> cnfProps = new HashMap<String, String>();
        if (properties != null) {
            Iterator<String> itProperties = properties.keys();
            while (itProperties.hasNext()) {
                String propertyName = itProperties.next();
                cnfProps.put(propertyName, properties.getString(propertyName));
            }
        }
        return new CmsContainerElementBean(getCmsObject().readResource(structureId), null, cnfProps);
    }

    /**
     * Deletes the given elements from server.<p>
     * 
     * @param elems the array of client-side element ids
     * 
     * @throws CmsException if something goes wrong 
     */
    protected void deleteElements(JSONArray elems) throws CmsException {

        CmsObject cms = getCmsObject();
        for (int i = 0; i < elems.length(); i++) {
            CmsResource res = cms.readResource(m_manager.convertToServerId(elems.optString(i)));
            String path = cms.getSitePath(res);
            cms.lockResource(path);
            cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
            cms.unlockResource(path);
        }
    }

    /**
     * Handles all ADE get requests.<p>
     * 
     * @return the result
     * 
     * @throws JSONException if there is any problem with JSON
     * @throws CmsException if there is a problem with the cms context
     */
    protected JSONObject executeActionGet() throws CmsException, JSONException {

        JSONObject result = new JSONObject();

        HttpServletRequest request = getRequest();

        if (!checkParameters(request, result, ReqParam.ACTION.getName())) {
            return result;
        }
        String actionParam = request.getParameter(ReqParam.ACTION.getName());
        Action action = Action.valueOf(actionParam.toUpperCase());
        if (action.equals(Action.PUBLISH_LIST)) {
            if (checkParameters(request, null, ParamPublish.REMOVE_RESOURCES.getName())) {
                // remove the resources from the user's publish list
                String remResParam = request.getParameter(ParamPublish.REMOVE_RESOURCES.getName());
                JSONArray resourcesToRemove = new JSONArray(remResParam);
                removeResourcesFromPublishList(resourcesToRemove);
            }
            if (checkParameters(request, null, ParamPublish.RELATED.getName(), ParamPublish.SIBLINGS.getName())) {
                // get list of resources to publish
                String relatedParam = request.getParameter(ParamPublish.RELATED.getName());
                String siblingsParam = request.getParameter(ParamPublish.SIBLINGS.getName());
                boolean related = Boolean.parseBoolean(relatedParam);
                boolean siblings = Boolean.parseBoolean(siblingsParam);
                JSONArray resourcesToPublish = getPublishList(related, siblings);
                result.put(JsonResponse.RESOURCES.getName(), resourcesToPublish);
            } else {
                // get list of resources that can not be published
                boolean related = true;
                boolean siblings = false;

                JSONArray resources = getResourcesToPublishWithProblems(related, siblings);
                if (resources.length() == 0) {
                    // if no problems just get the list of resources to publish
                    resources = getPublishList(related, siblings);
                }
                result.put(JsonResponse.RESOURCES.getName(), resources);
            }
            return result;
        } else if (action.equals(Action.PUBLISH)) {
            if (!checkParameters(request, result, ParamPublish.RESOURCES.getName())) {
                return result;
            }
            // resources to publish
            String resourcesParam = request.getParameter(ParamPublish.RESOURCES.getName());
            JSONArray resourcesToPublish = new JSONArray(resourcesParam);
            // get the resources with link check problems
            JSONArray resources = getResourcesWithLinkCheck(resourcesToPublish);
            if (resources.length() == 0) {
                // publish resources
                publishResources(resourcesToPublish);
            } else {
                // return resources with problems
                result.put(JsonResponse.RESOURCES.getName(), resources);
            }
            return result;
        }
        if (!checkParameters(
            request,
            result,
            ReqParam.CNTPAGE.getName(),
            ReqParam.LOCALE.getName(),
            ReqParam.URI.getName())) {
            return result;
        }
        String cntPageParam = request.getParameter(ReqParam.CNTPAGE.getName());
        String localeParam = request.getParameter(ReqParam.LOCALE.getName());
        String uriParam = request.getParameter(ReqParam.URI.getName());

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setLocale(CmsLocaleManager.getLocale(localeParam));
        CmsResource cntPageRes = cms.readResource(cntPageParam);
        CmsXmlContainerPage xmlCntPage = CmsXmlContainerPageFactory.unmarshal(cms, cntPageRes, request);
        CmsContainerPageBean cntPage = xmlCntPage.getCntPage(cms, cms.getRequestContext().getLocale());

        if (action.equals(Action.ALL)) {
            // first load, get everything
            result = getContainerPage(cntPageRes, cntPage, uriParam.equals(cntPageParam) ? null : uriParam);
        } else if (action.equals(Action.ELEM)) {
            // get element data
            if (!checkParameters(request, result, ReqParam.ELEM.getName())) {
                return result;
            }
            String elemParam = request.getParameter(ReqParam.ELEM.getName());
            CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, request, getResponse());
            JSONObject resElements = new JSONObject();
            if (elemParam.startsWith("[")) {
                // element list
                JSONArray elems = new JSONArray(elemParam);
                for (int i = 0; i < elems.length(); i++) {
                    String elemId = elems.getString(i);
                    CmsContainerElementBean element = getCachedElement(elemId);
                    resElements.put(element.getClientId(), elemUtil.getElementData(element, cntPage.getTypes()));
                }
            } else {
                // single element
                CmsContainerElementBean element = getCachedElement(elemParam);
                resElements.put(element.getClientId(), elemUtil.getElementData(element, cntPage.getTypes()));
            }
            result.put(JsonResponse.ELEMENTS.getName(), resElements);
        } else if (action.equals(Action.ELEMPROPS)) {
            // element formatted with the given properties
            if (!checkParameters(request, result, ReqParam.ELEM.getName(), ReqParam.PROPERTIES.getName())) {
                return result;
            }
            String elemParam = request.getParameter(ReqParam.ELEM.getName());
            String propertiesParam = request.getParameter(ReqParam.PROPERTIES.getName());
            CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, request, getResponse());
            JSONObject resElements = new JSONObject();
            JSONObject properties = new JSONObject(propertiesParam);
            CmsContainerElementBean element = createElement(m_manager.convertToServerId(elemParam), properties);
            String clientId = element.getClientId();
            m_sessionCache.setCacheContainerElement(element.getClientId(), element);
            resElements.put(clientId, elemUtil.getElementData(element, cntPage.getTypes()));
            result.put(JsonResponse.ELEMENTS.getName(), resElements);
        } else if (action.equals(Action.FAV)) {
            // get the favorite list
            result.put(JsonResponse.FAVORITES.getName(), getFavoriteList(null, cntPage.getTypes()));
        } else if (action.equals(Action.REC)) {
            // get recent list
            result.put(JsonResponse.RECENT.getName(), getRecentList(null, cntPage.getTypes()));
        } else if (action.equals(Action.SEARCH)) {
            // new search
            CmsSearchOptions searchOptions = new CmsSearchOptions(request);
            JSONObject searchResult = getSearchResult(cntPageParam, searchOptions, cntPage.getTypes());
            result.merge(searchResult, true, false);
        } else if (action.equals(Action.LS)) {
            // last search
            CmsSearchOptions searchOptions = new CmsSearchOptions(request);
            JSONObject searchResult = getLastSearchResult(cntPageParam, searchOptions, cntPage.getTypes());

            // we need those on the client side to make scrolling work
            CmsSearchOptions oldOptions = m_sessionCache.getADESearchOptions();
            if (oldOptions != null) {
                result.put(JsonSearch.TYPE.getName(), oldOptions.getTypes());
                result.put(JsonSearch.TEXT.getName(), oldOptions.getText());
                result.put(JsonSearch.LOCATION.getName(), oldOptions.getLocation());
            }
            result.merge(searchResult, true, false);
        } else if (action.equals(Action.NEW)) {
            // get a new element
            if (!checkParameters(request, result, ReqParam.DATA.getName())) {
                return result;
            }
            String dataParam = request.getParameter(ReqParam.DATA.getName());
            String type = dataParam;

            CmsResource newResource = m_manager.createNewElement(cms, cntPageParam, request, type);
            result.put(JsonNewRes.ID.getName(), m_manager.convertToClientId(newResource.getStructureId()));
            result.put(JsonNewRes.URI.getName(), cms.getSitePath(newResource));
        } else if (action.equals(Action.PROPS)) {
            // get property dialog information
            if (!checkParameters(request, result, ReqParam.ELEM.getName())) {
                return result;
            }
            String elemParam = request.getParameter(ReqParam.ELEM.getName());
            CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, request, getResponse());
            CmsContainerElementBean element = getCachedElement(elemParam);
            result = elemUtil.getElementPropertyInfo(element);
        } else {
            result.put(RES_ERROR, Messages.get().getBundle().key(
                Messages.ERR_JSON_WRONG_PARAMETER_VALUE_2,
                ReqParam.ACTION.getName(),
                actionParam));
        }
        return result;
    }

    /**
     * Handles all ADE set requests.<p>
     * 
     * @return the result
     * 
     * @throws JSONException if there is any problem with JSON
     * @throws CmsException if there is a problem with the cms context
     */
    protected JSONObject executeActionSet() throws JSONException, CmsException {

        JSONObject result = new JSONObject();
        HttpServletRequest request = getRequest();
        if (!checkParameters(request, result, ReqParam.ACTION.getName(), ReqParam.LOCALE.getName())) {
            return result;
        }
        String actionParam = request.getParameter(ReqParam.ACTION.getName());
        Action action = Action.valueOf(actionParam.toUpperCase());
        String localeParam = request.getParameter(ReqParam.LOCALE.getName());

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setLocale(CmsLocaleManager.getLocale(localeParam));
        if (action.equals(Action.FAV)) {
            // save the favorite list
            if (!checkParameters(request, result, ReqParam.DATA.getName())) {
                return result;
            }
            String dataParam = request.getParameter(ReqParam.DATA.getName());
            JSONArray list = new JSONArray(dataParam);
            m_manager.saveFavoriteList(cms, arrayToElementList(list));
        } else if (action.equals(Action.REC)) {
            // save the recent list
            if (!checkParameters(request, result, ReqParam.DATA.getName())) {
                return result;
            }
            String dataParam = request.getParameter(ReqParam.DATA.getName());
            JSONArray list = new JSONArray(dataParam);
            m_sessionCache.setCacheADERecentList(arrayToElementList(list));
        } else if (action.equals(Action.CNT)) {
            // save the container page
            if (!checkParameters(request, result, ReqParam.CNTPAGE.getName(), ReqParam.DATA.getName())) {
                return result;
            }
            String cntPageParam = request.getParameter(ReqParam.CNTPAGE.getName());
            String dataParam = request.getParameter(ReqParam.DATA.getName());
            JSONObject cntPage = new JSONObject(dataParam);
            setContainerPage(cntPageParam, cntPage);
        } else if (action.equals(Action.DEL)) {
            // delete elements
            if (!checkParameters(request, result, ReqParam.DATA.getName())) {
                return result;
            }
            String dataParam = request.getParameter(ReqParam.DATA.getName());
            JSONArray elems = new JSONArray(dataParam);
            deleteElements(elems);
        } else {
            result.put(RES_ERROR, Messages.get().getBundle().key(
                Messages.ERR_JSON_WRONG_PARAMETER_VALUE_2,
                ReqParam.ACTION.getName(),
                actionParam));
        }
        return result;
    }

    /**
     * Reads the cached element-bean for the given client-side-id from cache.<p>
     * 
     * @param clientId the client-side-id
     * @return the CmsContainerElementBean
     * @throws CmsException - if the resource could not be read for any reason
     */
    protected CmsContainerElementBean getCachedElement(String clientId) throws CmsException {

        String id = clientId;
        CmsContainerElementBean element = null;
        element = m_sessionCache.getCacheContainerElement(id);
        if (element != null) {
            return element;
        }
        if (id.contains("#")) {
            id = id.substring(0, id.indexOf("#"));
            element = m_sessionCache.getCacheContainerElement(id);
            if (element != null) {
                return element;
            }
        }
        // this is necessary if the element has not been cached yet
        element = new CmsContainerElementBean(getCmsObject().readResource(m_manager.convertToServerId(id)), null, null);
        m_sessionCache.setCacheContainerElement(id, element);
        return element;
    }

    /**
     * Returns the data for the given container page.<p>
     * 
     * @param resource the container page's resource 
     * @param cntPage the container page to use
     * @param elemUri the current element uri, <code>null</code> if not to be used as template
     * 
     * @return the data for the given container page
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected JSONObject getContainerPage(CmsResource resource, CmsContainerPageBean cntPage, String elemUri)
    throws CmsException, JSONException {

        CmsObject cms = getCmsObject();

        // create empty result object
        JSONObject result = new JSONObject();
        JSONObject resElements = new JSONObject();
        JSONObject resContainers = new JSONObject();
        result.put(JsonCntPage.ELEMENTS.getName(), resElements);
        result.put(JsonCntPage.CONTAINERS.getName(), resContainers);
        result.put(JsonCntPage.LOCALE.getName(), cms.getRequestContext().getLocale().toString());
        result.put(JsonCntPage.RECENT_LIST_SIZE.getName(), m_manager.getRecentListMaxSize(cms));

        // get the container page itself
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
        Set<String> types = cntPage.getTypes();

        // collect some basic data
        result.put(JsonCntPage.ALLOW_EDIT.getName(), resUtil.getLock().isLockableBy(
            cms.getRequestContext().currentUser())
            && resUtil.isEditable());
        result.put(JsonCntPage.LOCKED.getName(), resUtil.getLockedByName());

        // collect creatable type elements
        resElements.merge(getNewResourceTypes(cms.getSitePath(resource), types), true, false);
        // collect searchable type elements
        resElements.merge(getSearchResourceTypes(cms.getSitePath(resource), types), true, false);

        // collect page elements
        CmsElementUtil elemUtil = new CmsElementUtil(
            cms,
            getRequest().getParameter(ReqParam.URI.getName()),
            getRequest(),
            getResponse());
        Set<String> ids = new HashSet<String>();
        for (Map.Entry<String, CmsContainerBean> entry : cntPage.getContainers().entrySet()) {
            CmsContainerBean container = entry.getValue();

            // set the container data
            JSONObject resContainer = new JSONObject();
            resContainer.put(JsonContainer.OBJTYPE.getName(), TYPE_CONTAINER);
            resContainer.put(JsonContainer.NAME.getName(), container.getName());
            resContainer.put(JsonContainer.TYPE.getName(), container.getType());
            resContainer.put(JsonContainer.MAXELEMENTS.getName(), container.getMaxElements());
            JSONArray resContainerElems = new JSONArray();
            resContainer.put(JsonContainer.ELEMENTS.getName(), resContainerElems);

            // get the actual number of elements to render
            int renderElems = container.getElements().size();
            if ((container.getMaxElements() > -1) && (renderElems > container.getMaxElements())) {
                renderElems = container.getMaxElements();
            }
            // add the template element
            if ((elemUri != null) && container.getType().equals(CmsContainerPageBean.TYPE_TEMPLATE)) {
                renderElems--;

                CmsResource elemRes = cms.readResource(elemUri);
                CmsContainerElementBean element = new CmsContainerElementBean(elemRes, null, null);
                m_sessionCache.setCacheContainerElement(element.getClientId(), element);
                // check if the element already exists
                String id = element.getClientId();
                // collect ids
                resContainerElems.put(id);
                if (ids.contains(id)) {
                    continue;
                }
                // get the element data
                JSONObject resElement = elemUtil.getElementData(element, types);
                // store element data
                ids.add(id);
                resElements.put(id, resElement);
            }

            // iterate the elements
            for (CmsContainerElementBean element : container.getElements()) {
                if (renderElems < 1) {
                    // just collect as many elements as allowed in the template
                    break;
                }
                renderElems--;

                // collect ids
                String id = element.getClientId();
                resContainerElems.put(id);
                if (ids.contains(id)) {
                    continue;
                }
                m_sessionCache.setCacheContainerElement(element.getClientId(), element);
                // get the element data
                JSONObject resElement = elemUtil.getElementData(element, types);

                // get subcontainer elements
                if (resElement.has(CmsElementUtil.JsonElement.SUBITEMS.getName())) {
                    // this container page should contain exactly one container
                    CmsXmlContainerPage subXmlCntPage = CmsXmlContainerPageFactory.unmarshal(
                        cms,
                        element.getElement(),
                        getRequest());
                    CmsContainerPageBean subCntPage = subXmlCntPage.getCntPage(cms, cms.getRequestContext().getLocale());
                    CmsContainerBean subContainer = subCntPage.getContainers().values().iterator().next();

                    // adding all sub-items to the elements data
                    for (CmsContainerElementBean subElement : subContainer.getElements()) {
                        if (!ids.contains(subElement.getElement().getStructureId())) {
                            String subId = subElement.getClientId();
                            if (ids.contains(subId)) {
                                continue;
                            }
                            JSONObject subItemData = elemUtil.getElementData(subElement, types);
                            ids.add(subId);
                            resElements.put(subId, subItemData);
                        }
                    }
                }

                // store element data
                ids.add(id);
                resElements.put(id, resElement);
            }

            resContainers.put(container.getName(), resContainer);
        }
        // collect the favorites
        JSONArray resFavorites = getFavoriteList(resElements, types);
        result.put(JsonCntPage.FAVORITES.getName(), resFavorites);
        // collect the recent list
        JSONArray resRecent = getRecentList(resElements, types);
        result.put(JsonCntPage.RECENT.getName(), resRecent);

        return result;
    }

    /**
     * Returns the current user's favorites list.<p>
     * 
     * @param resElements the current page's element list
     * @param types the supported container page types
     * 
     * @return the current user's favorites list
     * 
     * @throws CmsException if something goes wrong 
     */
    protected JSONArray getFavoriteList(JSONObject resElements, Collection<String> types) throws CmsException {

        JSONArray result = new JSONArray();
        CmsElementUtil elemUtil = new CmsElementUtil(
            getCmsObject(),
            getRequest().getParameter(ReqParam.URI.getName()),
            getRequest(),
            getResponse());

        // iterate the list and create the missing elements
        List<CmsContainerElementBean> favList = m_manager.getFavoriteList(getCmsObject());
        for (CmsContainerElementBean element : favList) {
            String id = element.getClientId();
            if ((resElements != null) && !resElements.has(id)) {
                try {
                    resElements.put(id, elemUtil.getElementData(element, types));
                    m_sessionCache.setCacheContainerElement(element.getClientId(), element);
                    result.put(id);
                } catch (Exception e) {
                    // ignore any problems
                    if (!LOG.isDebugEnabled()) {
                        LOG.warn(e.getLocalizedMessage());
                    }
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            } else {
                result.put(id);
            }
        }

        return result;
    }

    /**
     * Returns elements for the search result matching the given options.<p>
     * 
     * @param cntPageUri the container page uri
     * @param options the search options
     * @param types the supported container types
     * 
     * @return JSON object with 2 properties, {@link JsonResponse#ELEMENTS} and {@link JsonSearch#HASMORE}
     * 
     * @throws JSONException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    protected JSONObject getLastSearchResult(String cntPageUri, CmsSearchOptions options, Set<String> types)
    throws JSONException, CmsException {

        CmsSearchOptions lastOptions = m_sessionCache.getADESearchOptions();
        if ((lastOptions == null) || compareSearchOptions(lastOptions, options)) {
            return new JSONObject();
        }
        return getSearchResult(cntPageUri, lastOptions, types);
    }

    /**
     * Returns the data for new elements from the given configuration file.<p>
     * 
     * @param cntPageUri the container page uri
     * @param types the supported container page types
     * 
     * @return the data for the given container page
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected JSONObject getNewResourceTypes(String cntPageUri, Set<String> types) throws CmsException, JSONException {

        JSONObject resElements = new JSONObject();
        resElements.put(JsonCntPage.NEWORDER.getName(), new JSONArray());
        HttpServletRequest request = getRequest();
        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, cntPageUri, request, getResponse());

        List<CmsResource> creatableElements = m_manager.getCreatableElements(cms, cntPageUri, request);
        for (CmsResource creatableElement : creatableElements) {
            String type = OpenCms.getResourceManager().getResourceType(creatableElement).getTypeName();
            JSONObject resElement = elemUtil.getElementData(
                new CmsContainerElementBean(creatableElement, null, null),
                types);
            // overwrite some special fields for new elements
            resElement.put(CmsElementUtil.JsonElement.ID.getName(), type);
            resElement.put(CmsElementUtil.JsonElement.STATUS.getName(), ELEMENT_NEWCONFIG);
            resElement.put(JsonResType.TYPE.getName(), type);
            resElement.put(JsonResType.TYPENAME.getName(), CmsWorkplaceMessages.getResourceName(
                cms.getRequestContext().getLocale(),
                type));
            resElements.put(type, resElement);
            // additional array to keep track of the order
            resElements.accumulate(JsonCntPage.NEWORDER.getName(), type);
        }
        return resElements;
    }

    /**
     * Returns the list of resources that can be published.<p>
     * 
     * @param related to include related resources
     * @param siblings to include siblings
     * 
     * @return a list of resources that can be published
     * 
     * @throws CmsException if something goes wrong 
     * @throws JSONException if something goes wrong 
     */
    protected JSONArray getPublishList(boolean related, boolean siblings) throws CmsException, JSONException {

        JSONArray resources = new JSONArray();

        // TODO: this is just a skeleton
        JSONObject jsonRes = resourceToJson(getCmsObject().readResource("/demo_t3/_content/events/event_0003.html"));
        jsonRes.put(JsonResource.INFO_NAME.getName(), "Unpublished related resource, will be published :");
        jsonRes.put(JsonResource.INFO_VALUE.getName(), "/demo_t3/images/Rose.jpg");
        resources.put(jsonRes);

        JSONObject jsonRes2 = resourceToJson(getCmsObject().readResource("/demo_t3/images/Rose.jpg"));
        jsonRes2.put(JsonResource.INFO_NAME.getName(), "Resource related to :");
        jsonRes2.put(JsonResource.INFO_VALUE.getName(), "/demo_t3/_content/events/event_0003.html");
        resources.put(jsonRes2);

        return resources;
    }

    /**
     * Returns the current user's recent list.<p>
     * 
     * @param resElements the current page's element list
     * @param types the supported container types
     * 
     * @return the current user's recent list
     * 
     * @throws CmsException if something goes wrong 
     */
    protected JSONArray getRecentList(JSONObject resElements, Collection<String> types) throws CmsException {

        JSONArray result = new JSONArray();
        CmsElementUtil elemUtil = new CmsElementUtil(
            getCmsObject(),
            getRequest().getParameter(ReqParam.URI.getName()),
            getRequest(),
            getResponse());

        // get the cached list
        List<CmsContainerElementBean> recentList = m_sessionCache.getADERecentList();
        // iterate the list and create the missing elements
        for (CmsContainerElementBean element : recentList) {
            String id = element.getClientId();
            if ((resElements != null) && !resElements.has(id)) {
                try {
                    resElements.put(id, elemUtil.getElementData(element, types));
                    result.put(id);
                } catch (Exception e) {
                    // ignore any problems
                    if (!LOG.isDebugEnabled()) {
                        LOG.warn(e.getLocalizedMessage());
                    }
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            } else {
                result.put(id);
            }
        }

        return result;
    }

    /**
     * Returns a list of resources that should be published but can not.<p>
     * This can be due to several reasons:<br>
     * <ul>
     *  <li>Locks</li>
     *  <li>Permissions</li>
     *  <li>Already published</li>
     * </ul>
     * 
     * @param related to include related resources
     * @param siblings to include siblings
     * 
     * @return a list of resources that should be published but can not
     * 
     * @throws CmsException if something goes wrong 
     * @throws JSONException if something goes wrong 
     */
    protected JSONArray getResourcesToPublishWithProblems(boolean related, boolean siblings)
    throws JSONException, CmsException {

        JSONArray resources = new JSONArray();

        // TODO: this is just a skeleton
        JSONObject jsonRes = resourceToJson(getCmsObject().readResource("/demo_t3/_content/events/event_0003.html"));
        jsonRes.put(JsonResource.REASON.getName(), NoPubReason.PERMISSIONS.getName());
        resources.put(jsonRes);

        JSONObject jsonRes2 = resourceToJson(getCmsObject().readResource("/demo_t3/images/Rose.jpg"));
        jsonRes2.put(JsonResource.REASON.getName(), NoPubReason.PUBLISHED.getName());
        resources.put(jsonRes2);

        return resources;
    }

    /**
     * Checks for possible broken links when the given list of resources would be published.<p>
     * 
     * @param ids list of structure ids identifying the resources to be published
     * 
     * @return a sublist of JSON resources that would produce broken links when published 
     * 
     * @throws CmsException if something goes wrong 
     * @throws JSONException if something goes wrong 
     */
    protected JSONArray getResourcesWithLinkCheck(JSONArray ids) throws JSONException, CmsException {

        JSONArray resources = new JSONArray();

        // TODO: this is just a skeleton
        JSONObject jsonRes = resourceToJson(getCmsObject().readResource("/demo_t3/index.html"));
        jsonRes.put(JsonResource.INFO_NAME.getName(), "Broken link sources :");
        jsonRes.put(JsonResource.INFO_VALUE.getName(), "/demo_t3/_content/articles/intro.html (Html link)");
        resources.put(jsonRes);

        return resources;
    }

    /**
     * Returns the data for searchable resource types from the given configuration file.<p>
     * 
     * @param cntPageUri the container page uri
     * @param types the supported container page types
     * 
     * @return the data for the given container page
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected JSONObject getSearchResourceTypes(String cntPageUri, Set<String> types)
    throws CmsException, JSONException {

        JSONObject resElements = new JSONObject();
        resElements.put(JsonCntPage.SEARCH_ORDER.getName(), new JSONArray());
        HttpServletRequest request = getRequest();
        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, cntPageUri, request, getResponse());

        List<CmsResource> creatableElements = m_manager.getSearchableResourceTypes(cms, cntPageUri, request);
        for (CmsResource searchableElement : creatableElements) {
            String type = OpenCms.getResourceManager().getResourceType(searchableElement).getTypeName();
            JSONObject resElement = elemUtil.getElementData(
                new CmsContainerElementBean(searchableElement, null, null),
                types);
            // overwrite some special fields for searchable elements
            resElement.put(CmsElementUtil.JsonElement.ID.getName(), type);
            resElement.put(CmsElementUtil.JsonElement.STATUS.getName(), ELEMENT_NEWCONFIG);
            resElement.put(JsonResType.TYPE.getName(), type);
            resElement.put(JsonResType.TYPENAME.getName(), CmsWorkplaceMessages.getResourceName(
                cms.getRequestContext().getLocale(),
                type));
            resElements.put(type, resElement);
            // additional array to keep track of the order
            resElements.accumulate(JsonCntPage.SEARCH_ORDER.getName(), type);
        }
        return resElements;
    }

    /**
     * Returns elements for the search result matching the given options.<p>
     * 
     * @param cntPageUri the container page uri
     * @param options the search options
     * @param types the supported container types
     * 
     * @return JSON object with 3 properties, {@link JsonResponse#ELEMENTS}, {@link JsonSearch#HASMORE} and {@link JsonSearch#COUNT}
     * 
     * @throws JSONException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    protected JSONObject getSearchResult(String cntPageUri, CmsSearchOptions options, Set<String> types)
    throws JSONException, CmsException {

        CmsObject cms = getCmsObject();

        JSONObject result = new JSONObject();
        JSONArray elements = new JSONArray();
        result.put(JsonResponse.ELEMENTS.getName(), elements);

        CmsUser user = cms.getRequestContext().currentUser();

        // if there is no type or no text to search, no search is needed 
        if (options.isValid()) {
            // get the configured search index 
            String indexName = new CmsUserSettings(user).getWorkplaceSearchIndexName();

            // get the page size
            int pageSize = m_manager.getSearchPageSize(cms);

            // set the search parameters
            CmsSearchParameters params = new CmsSearchParameters(options.getText());
            params.setIndex(indexName);
            params.setMatchesPerPage(pageSize);
            params.setSearchPage(options.getPage() + 1);
            params.setResourceTypes(options.getTypesAsList());

            // search
            CmsSearch searchBean = new CmsSearch();
            searchBean.init(cms);
            searchBean.setParameters(params);
            searchBean.setSearchRoot(options.getLocation());
            List<CmsSearchResult> searchResults = searchBean.getSearchResult();
            if (searchResults != null) {
                // helper
                CmsElementUtil elemUtil = new CmsElementUtil(
                    cms,
                    getRequest().getParameter(ReqParam.URI.getName()),
                    getRequest(),
                    getResponse());

                // iterate result list and generate the elements
                Iterator<CmsSearchResult> it = searchResults.iterator();
                while (it.hasNext()) {
                    CmsSearchResult sr = it.next();
                    // get the element data
                    String uri = cms.getRequestContext().removeSiteRoot(sr.getPath());
                    try {
                        CmsResource resource = cms.readResource(uri);
                        JSONObject resElement = elemUtil.getElementData(new CmsContainerElementBean(
                            resource,
                            null,
                            null), types);
                        // store element data
                        elements.put(resElement);
                    } catch (Exception e) {
                        if (!LOG.isDebugEnabled()) {
                            LOG.warn(e.getLocalizedMessage());
                        }
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }
            }
            // check if there are more search pages
            int results = searchBean.getSearchPage() * searchBean.getMatchesPerPage();
            boolean hasMore = (searchBean.getSearchResultCount() > results);
            result.put(JsonSearch.HASMORE.getName(), hasMore);
            result.put(JsonSearch.COUNT.getName(), searchBean.getSearchResultCount());
        } else {
            // no search
            result.put(JsonSearch.HASMORE.getName(), false);
            result.put(JsonSearch.COUNT.getName(), 0);
        }

        m_sessionCache.setCacheADESearchOptions(options);

        return result;
    }

    /**
     * Publishes the given list of resources.<p>
     * 
     * @param ids list of structure ids identifying the resources to publish
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong
     */
    protected void publishResources(JSONArray ids) throws CmsException, JSONException {

        List<CmsResource> resources = new ArrayList<CmsResource>(ids.length());
        CmsObject cms = getCmsObject();
        for (int i = 0; i < ids.length(); i++) {
            resources.add(cms.readResource(new CmsUUID(ids.optString(i))));
        }
        // TODO: publish the resources
        // OpenCms.getPublishManager().publishProject(cms, report, publishList);
        removeResourcesFromPublishList(ids);
    }

    /**
     * Removes the given resources from the user's publish list.<p>
     * 
     * @param ids list of structure ids identifying the resources to be removed
     * 
     * @throws JSONException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    protected void removeResourcesFromPublishList(JSONArray ids) throws JSONException, CmsException {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.readUser(cms.getRequestContext().currentUser().getId());
        String info = (String)user.getAdditionalInfo(INFO_PUBLISH_LIST);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(info)) {
            // publish list is empty, nothing to do
            return;
        }
        JSONArray userPubList = new JSONArray(info);
        JSONArray newPubList = new JSONArray();
        for (int i = 0; i < userPubList.length(); i++) {
            String id = userPubList.optString(i);
            if (!ids.containsString(id)) {
                newPubList.put(id);
            }
        }
        cms.getRequestContext().currentUser().setAdditionalInfo(INFO_PUBLISH_LIST, newPubList.toString());
        cms.writeUser(user);
    }

    /**
     * Converts a resource to a JSON object.<p>
     * 
     * @param resource the resource to convert
     * 
     * @return a JSON object representing the resource
     * 
     * @throws JSONException if something goes wrong
     */
    protected JSONObject resourceToJson(CmsResource resource) throws JSONException {

        CmsObject cms = getCmsObject();
        JSONObject jsonRes = new JSONObject();
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
        jsonRes.put(JsonResource.ID.getName(), resource.getStructureId());
        jsonRes.put(JsonResource.URI.getName(), resUtil.getFullPath());
        jsonRes.put(JsonResource.TITLE.getName(), resUtil.getTitle());
        jsonRes.put(JsonResource.ICON.getName(), resUtil.getIconPathExplorer());
        jsonRes.put(JsonResource.STATE.getName(), "" + resUtil.getStateAbbreviation());
        jsonRes.put(JsonResource.LOCKED_BY.getName(), resUtil.getLockedByName());
        return jsonRes;
    }

    /**
     * Saves the new state of the container page.<p>
     * 
     * @param uri the uri of the container page to save
     * @param cntPage the container page data
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected void setContainerPage(String uri, JSONObject cntPage) throws CmsException, JSONException {

        CmsObject cms = getCmsObject();
        String paramUri = getRequest().getParameter(ReqParam.URI.getName());

        cms.lockResourceTemporary(uri);
        CmsFile containerPage = cms.readFile(uri);
        CmsXmlContainerPage xmlCnt = CmsXmlContainerPageFactory.unmarshal(cms, containerPage);
        Locale locale = CmsLocaleManager.getLocale(cntPage.getString(JsonCntPage.LOCALE.getName()));
        if (xmlCnt.hasLocale(locale)) {
            // remove the locale 
            xmlCnt.removeLocale(locale);
        }
        xmlCnt.addLocale(cms, locale);

        JSONObject cnts = cntPage.getJSONObject(JsonCntPage.CONTAINERS.getName());
        int cntCount = 0;
        Iterator<String> itCnt = cnts.keys();
        while (itCnt.hasNext()) {
            String cntKey = itCnt.next();
            JSONObject cnt = cnts.getJSONObject(cntKey);

            I_CmsXmlContentValue cntValue = xmlCnt.getValue(CmsXmlContainerPage.N_CONTAINER, locale, cntCount);
            if (cntValue == null) {
                cntValue = xmlCnt.addValue(cms, CmsXmlContainerPage.N_CONTAINER, locale, cntCount);
            }

            String name = cnt.getString(JsonContainer.NAME.getName());
            xmlCnt.getValue(CmsXmlUtils.concatXpath(cntValue.getPath(), CmsXmlContainerPage.N_NAME), locale, 0).setStringValue(
                cms,
                name);

            String type = cnt.getString(JsonContainer.TYPE.getName());
            xmlCnt.getValue(CmsXmlUtils.concatXpath(cntValue.getPath(), CmsXmlContainerPage.N_TYPE), locale, 0).setStringValue(
                cms,
                type);

            JSONArray elems = cnt.getJSONArray(JsonCntPage.ELEMENTS.getName());
            for (int i = 0; i < elems.length(); i++) {
                JSONObject elem = elems.getJSONObject(i);

                String formatter = elem.getString(JsonCntElem.FORMATTER.getName());
                String elemUri = elem.getString(JsonCntElem.URI.getName());
                if (type.equals(CmsContainerPageBean.TYPE_TEMPLATE) && elemUri.equals(paramUri)) {
                    // skip main-content if acting as template
                    continue;
                }
                String clientId = elem.getString(JsonCntElem.ID.getName());
                I_CmsXmlContentValue elemValue = xmlCnt.addValue(cms, CmsXmlUtils.concatXpath(
                    cntValue.getPath(),
                    CmsXmlContainerPage.N_ELEMENT), locale, i);
                xmlCnt.getValue(CmsXmlUtils.concatXpath(elemValue.getPath(), CmsXmlContainerPage.N_URI), locale, 0).setStringValue(
                    cms,
                    elemUri);
                xmlCnt.getValue(
                    CmsXmlUtils.concatXpath(elemValue.getPath(), CmsXmlContainerPage.N_FORMATTER),
                    locale,
                    0).setStringValue(cms, formatter);

                // checking if there are any properties to set
                if (clientId.contains("#")) {
                    CmsContainerElementBean element = getCachedElement(clientId);
                    Map<String, CmsProperty> properties = m_manager.getElementProperties(element);
                    Map<String, CmsXmlContentProperty> propertiesConf = m_manager.getElementPropertyConfiguration(element.getElement());
                    Iterator<String> itProps = properties.keySet().iterator();

                    // index of the property
                    int j = 0;

                    // iterating all properties
                    while (itProps.hasNext()) {
                        String propertyName = itProps.next();

                        // only if there is a value set and the property is configured in the schema we will save it to the container-page 
                        if ((properties.get(propertyName).getStructureValue() != null)
                            && propertiesConf.containsKey(propertyName)) {
                            I_CmsXmlContentValue propValue = xmlCnt.addValue(cms, CmsXmlUtils.concatXpath(
                                elemValue.getPath(),
                                CmsXmlContainerPage.N_PROPERTIES), locale, j);
                            xmlCnt.getValue(
                                CmsXmlUtils.concatXpath(propValue.getPath(), CmsXmlContainerPage.N_NAME),
                                locale,
                                0).setStringValue(cms, propertyName);
                            I_CmsXmlContentValue valValue = xmlCnt.addValue(cms, CmsXmlUtils.concatXpath(
                                propValue.getPath(),
                                CmsXmlContainerPage.N_VALUE), locale, 0);
                            if (propertiesConf.get(propertyName).getPropertyType() == CmsXmlContentProperty.T_URI) {
                                xmlCnt.addValue(
                                    cms,
                                    CmsXmlUtils.concatXpath(valValue.getPath(), CmsXmlContainerPage.N_URI),
                                    locale,
                                    0).setStringValue(cms, properties.get(propertyName).getStructureValue());
                            } else {
                                xmlCnt.addValue(
                                    cms,
                                    CmsXmlUtils.concatXpath(valValue.getPath(), CmsXmlContainerPage.N_STRING),
                                    locale,
                                    0).setStringValue(cms, properties.get(propertyName).getStructureValue());
                            }
                            j++;
                        }
                    }
                }

            }
            cntCount++;
        }
        containerPage.setContents(xmlCnt.marshal());
        cms.writeFile(containerPage);
    }

    /**
     * Stores an error message for a missing parameter in a JSON object.
     * 
     * @param result the JSON object in which the error message should be stored
     * @param parameterName the name of the missing parameter
     * @throws JSONException if something goes wrong.
     */
    protected void storeErrorMissingParam(JSONObject result, String parameterName) throws JSONException {

        result.put(RES_ERROR, Messages.get().getBundle().key(Messages.ERR_JSON_MISSING_PARAMETER_1, parameterName));
    }
}

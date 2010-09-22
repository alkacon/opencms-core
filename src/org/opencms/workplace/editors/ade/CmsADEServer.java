/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEServer.java,v $
 * Date   : $Date: 2010/09/22 14:27:48 $
 * Version: $Revision: 1.38 $
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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.CmsSearchResult;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.A_CmsAjaxServer;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.editors.ade.CmsElementUtil.JsonElement;
import org.opencms.xml.containerpage.CmsADEManager;
import org.opencms.xml.containerpage.CmsADESessionCache;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsSubContainerBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.containerpage.CmsXmlSubContainer;
import org.opencms.xml.containerpage.CmsXmlSubContainerFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * ADE server used for client/server communication.<p>
 * 
 * see jsp file <tt>/system/workplace/editors/ade/server.jsp</tt>.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.38 $
 * 
 * @since 7.6
 */
public class CmsADEServer extends A_CmsAjaxServer {

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
        /** To retrieve the favorite or recent list. */
        GET,
        /** To get the last search results. */
        LS,
        /** To create a new element. */
        NEW,
        /** To create a new sub container element. */
        NEWSUB,
        /** To retrieve the configured properties. */
        PROPS,
        /** To retrieve search results. */
        SEARCH,
        /** To retrieve the favorite or recent list. */
        SET,
        /** To lock the container page. */
        STARTEDIT,
        /** To unlock the container page. */
        STOPEDIT,
        /** To save a sub container element. */
        SUBCNT;
    }

    /** Json property name constants for container elements. */
    protected enum JsonCntElem {

        /** The formatter's uri. */
        formatter,
        /** The element id. */
        id,
        /** The element's uri. */
        uri;
    }

    /** Json property name constants for container pages. */
    protected enum JsonCntPage {

        /** The list of containers. */
        containers,
        /** The list of elements. */
        elements,
        /** The favorites list. */
        favorites,
        /** The locale. */
        locale,
        /** The order of creatable elements. */
        newOrder,
        /** The recent list. */
        recent,
        /** The max size of the recent list. */
        recentListSize,
        /** The order of searchable resource types. */
        searchOrder,
        /** The container page state. */
        state;
    }

    /** Json property name constants for new created resources. */
    protected enum JsonNewRes {

        /** The resource's structure id. */
        id,
        /** The resource's uri. */
        uri;
    }

    /** Json property name constants for request parameters. */
    protected enum JsonRequest {

        /** Element id or list of element ids. */
        elem,
        /** To get or save the favorites list. */
        fav,
        /** Elements properties. */
        properties,
        /** To retrieve or save the recent list. */
        rec,
        /** Resource type. */
        type;
    }

    /** Json property name constants for responses. */
    protected enum JsonResponse {

        /** List of elements. */
        elements,
        /** The favorites list. */
        favorites,
        /** The recent list. */
        recent;
    }

    /** Json property name constants for resource type information. */
    protected enum JsonResType {

        /** The element's resource type. */
        type,
        /** The element's resource type id. */
        typeid,
        /** The element's resource type nice name. */
        typename;
    }

    /** Json property name constants for searching. */
    protected enum JsonSearch {

        /** The number of search results. */
        count,
        /** If the search has more results. */
        hasmore,
        /** The search location. */
        location,
        /** The search page. */
        page,
        /** Search query. */
        text,
        /** The resource type(s). */
        type;
    }

    /** Request parameter name constants. */
    protected enum ReqParam {

        /** The action of execute. */
        action,
        /** The container uri. */
        cntpage,
        /** Generic data parameter. */
        data,
        /** The current locale. */
        locale,
        /** The request's parameters. */
        params,
        /** Element uri. */
        uri;
    }

    /** State Constant for client-side element type 'new element configuration'. */
    public static final String ELEMENT_NEWCONFIG = "NC";

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
        m_sessionCache = (CmsADESessionCache)req.getSession().getAttribute(CmsADESessionCache.SESSION_ATTR_ADE_CACHE);
        if (m_sessionCache == null) {
            m_sessionCache = new CmsADESessionCache(getCmsObject());
            req.getSession().setAttribute(CmsADESessionCache.SESSION_ATTR_ADE_CACHE, m_sessionCache);
        }
    }

    /**
     * Helper method for treating a Map<X, Set<Y>> as a multimap from Xs to Ys which adds a new Y for a given X.<p>
     * 
     * If there is no entry in the map with the given key, an empty Set is created before the value is inserted.  
     * 
     * @param <X> the key type 
     * @param <Y> the value type 
     * @param multimap the map to add a new value to 
     * @param key the key for which the new value should be inserted 
     * @param value the new value 
     */
    private static <X, Y> void addToMultiMap(Map<X, Set<Y>> multimap, X key, Y value) {

        if (!multimap.containsKey(key)) {
            multimap.put(key, new HashSet<Y>());
        }
        multimap.get(key).add(value);
    }

    /**
     * Creates a new element bean for a structure id and given properties.<p> 
     * 
     * @param structureId the structure-id
     * @param properties the properties-map
     * 
     * @return the element bean
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong parsing JSON
     */
    public CmsContainerElementBean createElement(CmsUUID structureId, JSONObject properties)
    throws JSONException, CmsException {

        CmsObject cms = getCmsObject();
        Map<String, CmsXmlContentProperty> propertiesConf = m_manager.getElementPropertyConfiguration(
            cms,
            cms.readResource(structureId));

        Map<String, String> cnfProps = new HashMap<String, String>();
        if (properties != null) {
            Iterator<String> itProperties = properties.keys();
            while (itProperties.hasNext()) {
                String propertyName = itProperties.next();
                cnfProps.put(propertyName, CmsXmlContentPropertyHelper.getPropValueIds(cms, propertiesConf.get(
                    propertyName).getPropertyType(), properties.getString(propertyName)));
            }
        }
        return new CmsContainerElementBean(structureId, null, cnfProps);
    }

    /**
     * Creates a new sub container resource.<p>
     * 
     * @param cntPageUri the container page uri to read the appropriate configuration
     * @param request the current request object
     * @return the new sub container resource
     * @throws CmsException if something goes wrong
     */
    public CmsResource createNewSubContainer(String cntPageUri, HttpServletRequest request) throws CmsException {

        return m_manager.createNewElement(
            getCmsObject(),
            cntPageUri,
            request,
            CmsResourceTypeXmlContainerPage.SUB_CONTAINER_TYPE_NAME);
    }

    /**
     * Deletes the given elements from server.<p>
     * 
     * @param elems the array of client-side element ids
     */
    public void deleteElements(JSONArray elems) {

        CmsObject cms = getCmsObject();
        for (int i = 0; i < elems.length(); i++) {
            String path;
            try {
                CmsResource res = cms.readResource(m_manager.convertToServerId(elems.optString(i)));
                path = cms.getSitePath(res);
                cms.lockResource(path);
            } catch (Exception e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
                continue;
            }
            try {
                cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
            } catch (Exception e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
            } finally {
                try {
                    cms.unlockResource(path);
                } catch (Exception e) {
                    // should really never happen
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Handles all ADE requests.<p>
     * 
     * @return the result
     * 
     * @throws JSONException if there is any problem with JSON
     * @throws CmsException if there is a problem with the cms context
     */
    @Override
    public JSONObject executeAction() throws CmsException, JSONException {

        JSONObject result = new JSONObject();

        HttpServletRequest request = getRequest();
        CmsObject cms = getCmsObject();

        if (!checkParameters(
            request,
            result,
            ReqParam.action.name(),
            ReqParam.locale.name(),
            ReqParam.cntpage.name(),
            ReqParam.params.name(),
            ReqParam.uri.name())) {
            // every request needs to have at least these parameters 
            return result;
        }
        String actionParam = request.getParameter(ReqParam.action.name());
        Action action = Action.valueOf(actionParam.toUpperCase());
        String localeParam = request.getParameter(ReqParam.locale.name());
        cms.getRequestContext().setLocale(CmsLocaleManager.getLocale(localeParam));
        String cntPageParam = request.getParameter(ReqParam.cntpage.name());
        String uriParam = request.getParameter(ReqParam.uri.name());
        JSONObject reqParams = new JSONObject(request.getParameter(ReqParam.params.name()));
        CmsFlexController.getController(request).getCurrentRequest().addParameterMap(
            CmsRequestUtil.getParameterMapFromJSON(reqParams));

        JSONObject data = new JSONObject();
        if (checkParameters(request, null, ReqParam.data.name())) {
            String dataParam = request.getParameter(ReqParam.data.name());
            data = new JSONObject(dataParam);
        }

        if (action.equals(Action.ALL)) {
            // get container types
            if (!checkParameters(data, result, JsonCntPage.containers.name())) {
                return result;
            }
            Set<String> cntTypes = new HashSet<String>();
            JSONArray types = data.optJSONArray(JsonCntPage.containers.name());
            for (int i = 0; i < types.length(); i++) {
                cntTypes.add(types.getString(i));
            }
            // first load, get everything
            CmsResource cntPageRes = cms.readResource(cntPageParam);
            CmsXmlContainerPage xmlCntPage = CmsXmlContainerPageFactory.unmarshal(cms, cntPageRes, request);
            CmsContainerPageBean cntPage = xmlCntPage.getCntPage(cms, cms.getRequestContext().getLocale());
            result = getContainerPage(cntPageRes, cntPage, uriParam.equals(cntPageParam) ? null : uriParam, cntTypes);
        } else if (action.equals(Action.ELEM)) {
            if (!checkParameters(data, result, JsonRequest.elem.name(), JsonCntPage.containers.name())) {
                return result;
            }
            // get container types
            Set<String> cntTypes = new HashSet<String>();
            JSONArray types = data.optJSONArray(JsonCntPage.containers.name());
            for (int i = 0; i < types.length(); i++) {
                cntTypes.add(types.getString(i));
            }
            // get elements
            JSONArray elems = data.optJSONArray(JsonRequest.elem.name());
            if (elems == null) {
                // single element
                elems = new JSONArray();
                elems.put(data.optString(JsonRequest.elem.name()));
            }
            result.put(JsonResponse.elements.name(), getElements(elems, uriParam, request, cntTypes));
        } else if (action.equals(Action.ELEMPROPS)) {
            // element formatted with the given properties
            if (!checkParameters(data, result, JsonRequest.elem.name(), JsonRequest.properties.name())) {
                return result;
            }
            String elemParam = data.optString(JsonRequest.elem.name());
            JSONObject properties = data.optJSONObject(JsonRequest.properties.name());

            CmsContainerElementBean element = createElement(m_manager.convertToServerId(elemParam), properties);

            m_sessionCache.setCacheContainerElement(element.getClientId(), element);

            JSONObject resElements = new JSONObject();
            CmsResource cntPageRes = cms.readResource(cntPageParam);
            CmsXmlContainerPage xmlCntPage = CmsXmlContainerPageFactory.unmarshal(cms, cntPageRes, request);
            CmsContainerPageBean cntPage = xmlCntPage.getCntPage(cms, cms.getRequestContext().getLocale());
            CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, request, getResponse());
            resElements.put(element.getClientId(), elemUtil.getElementData(element, cntPage.getTypes()));
            result.put(JsonResponse.elements.name(), resElements);
        } else if (action.equals(Action.GET)) {
            CmsResource cntPageRes = cms.readResource(cntPageParam);
            CmsXmlContainerPage xmlCntPage = CmsXmlContainerPageFactory.unmarshal(cms, cntPageRes, request);
            CmsContainerPageBean cntPage = xmlCntPage.getCntPage(cms, cms.getRequestContext().getLocale());
            if (checkParameters(data, null, JsonRequest.fav.name())) {
                // get the favorite list
                result.put(JsonResponse.favorites.name(), getFavoriteList(null, cntPage.getTypes()));
            }
            if (checkParameters(data, result, JsonRequest.rec.name())) {
                // get recent list
                result.put(JsonResponse.recent.name(), getRecentList(null, cntPage.getTypes()));
            }
            return result;
        } else if (action.equals(Action.SEARCH)) {
            // new search
            CmsSearchOptions searchOptions = getSearchOptions(data);
            CmsResource cntPageRes = cms.readResource(cntPageParam);
            CmsXmlContainerPage xmlCntPage = CmsXmlContainerPageFactory.unmarshal(cms, cntPageRes, request);
            CmsContainerPageBean cntPage = xmlCntPage.getCntPage(cms, cms.getRequestContext().getLocale());
            JSONObject searchResult = getSearchResult(cntPageParam, searchOptions, cntPage.getTypes());
            result.merge(searchResult, true, false);
        } else if (action.equals(Action.LS)) {
            // last search
            CmsSearchOptions searchOptions = getSearchOptions(data);
            CmsResource cntPageRes = cms.readResource(cntPageParam);
            CmsXmlContainerPage xmlCntPage = CmsXmlContainerPageFactory.unmarshal(cms, cntPageRes, request);
            CmsContainerPageBean cntPage = xmlCntPage.getCntPage(cms, cms.getRequestContext().getLocale());
            JSONObject searchResult = getLastSearchResult(cntPageParam, searchOptions, cntPage.getTypes());

            // we need those on the client side to make scrolling work
            CmsSearchOptions oldOptions = m_sessionCache.getSearchOptions();
            if (oldOptions != null) {
                result.put(JsonSearch.type.name(), oldOptions.getTypes());
                result.put(JsonSearch.text.name(), oldOptions.getText());
                result.put(JsonSearch.location.name(), oldOptions.getLocation());
            }
            result.merge(searchResult, true, false);
        } else if (action.equals(Action.NEW)) {
            // get a new element
            if (!checkParameters(data, result, JsonRequest.type.name())) {
                return result;
            }
            String type = data.optString(JsonRequest.type.name());

            CmsResource newResource = m_manager.createNewElement(cms, cntPageParam, request, type);
            result.put(JsonNewRes.id.name(), newResource.getStructureId().toString());
            result.put(JsonNewRes.uri.name(), cms.getSitePath(newResource));
        } else if (action.equals(Action.PROPS)) {
            // get property dialog information
            if (!checkParameters(data, result, JsonRequest.elem.name())) {
                return result;
            }
            String elemParam = data.optString(JsonRequest.elem.name());
            CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, request, getResponse());
            CmsContainerElementBean element = getCachedElement(elemParam);
            result.put(JsonRequest.properties.name(), elemUtil.getElementPropertyInfo(element));
        } else if (action.equals(Action.SET)) {
            if (checkParameters(data, null, JsonRequest.fav.name())) {
                // save the favorite list
                JSONArray list = data.optJSONArray(JsonRequest.fav.name());
                m_manager.saveFavoriteList(cms, arrayToElementList(list));
            } else if (checkParameters(data, result, JsonRequest.rec.name())) {
                // save the recent list
                JSONArray list = data.optJSONArray(JsonRequest.rec.name());
                m_sessionCache.setCacheRecentList(arrayToElementList(list));
            } else {
                return result;
            }
        } else if (action.equals(Action.CNT)) {
            // save the container page
            setContainerPage(cntPageParam, data);
        } else if (action.equals(Action.STARTEDIT)) {
            // lock the container page
            try {
                cms.lockResourceTemporary(cntPageParam);
            } catch (CmsException e) {
                error(result, e.getLocalizedMessage(getWorkplaceLocale()));
            }
        } else if (action.equals(Action.STOPEDIT)) {
            // lock the container page
            try {
                cms.unlockResource(cntPageParam);
            } catch (CmsException e) {
                error(result, e.getLocalizedMessage(getWorkplaceLocale()));
            }
        } else if (action.equals(Action.DEL)) {
            // delete elements
            if (!checkParameters(data, result, JsonRequest.elem.name())) {
                return result;
            }
            JSONArray elems = data.optJSONArray(JsonRequest.elem.name());
            deleteElements(elems);
        } else if (action.equals(Action.SUBCNT)) {
            // save sub container
            if (!checkParameters(data, result, JsonRequest.elem.name())) {
                return result;
            }
            try {
                setSubContainer(data.getJSONObject(JsonRequest.elem.name()));
            } catch (Exception e) {
                error(result, e.getLocalizedMessage());
            }
        } else if (action.equals(Action.NEWSUB)) {
            // save sub container
            if (!checkParameters(data, result, JsonRequest.elem.name(), JsonElement.types.name())) {
                return result;
            }
            try {
                JSONArray typesJson = data.getJSONArray(JsonElement.types.name());
                List<String> types = new ArrayList<String>();
                for (int i = 0; i < typesJson.length(); i++) {
                    types.add(typesJson.optString(i));
                }
                CmsResource newSub = createNewSubContainer(cntPageParam, request);
                JSONObject subcontainer = data.getJSONObject(JsonRequest.elem.name());
                subcontainer.put(CmsElementUtil.JsonElement.file.name(), cms.getSitePath(newSub));
                setSubContainer(subcontainer);
                CmsContainerElementBean subcontainerElement = new CmsContainerElementBean(
                    newSub.getStructureId(),
                    null,
                    null);
                CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, request, getResponse());
                JSONObject subcontainerData = elemUtil.getElementData(subcontainerElement, types);

                JSONObject elementsObj = new JSONObject();
                elementsObj.put(subcontainerElement.getClientId(), subcontainerData);
                result.put(JsonResponse.elements.name(), elementsObj);
                result.put(JsonNewRes.id.name(), newSub.getStructureId().toString());
                result.put(JsonNewRes.uri.name(), cms.getSitePath(newSub));
            } catch (Exception e) {
                error(result, e.getLocalizedMessage());
            }
        } else {
            error(result, Messages.get().getBundle(getWorkplaceLocale()).key(
                Messages.ERR_JSON_WRONG_PARAMETER_VALUE_2,
                ReqParam.action.name(),
                actionParam));
        }
        return result;
    }

    /**
     * Returns the data for the given container page.<p>
     * 
     * @param resource the container page's resource 
     * @param cntPage the container page to use
     * @param elemUri the current element uri, <code>null</code> if not to be used as template
     * @param types the container page types
     * 
     * @return the data for the given container page
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    public JSONObject getContainerPage(
        CmsResource resource,
        CmsContainerPageBean cntPage,
        String elemUri,
        Set<String> types) throws CmsException, JSONException {

        CmsObject cms = getCmsObject();
        CmsContainerElementBean detailViewElement = null;
        if (elemUri != null) {
            CmsResource elemRes = cms.readResource(elemUri);
            detailViewElement = new CmsContainerElementBean(elemRes.getStructureId(), null, null);
        }
        Map<CmsUUID, Set<String>> elementContainerTypes = getElementContainerTypes(cms, cntPage, detailViewElement);

        // create empty result object
        JSONObject result = new JSONObject();
        JSONObject resElements = new JSONObject();
        result.put(JsonCntPage.elements.name(), resElements);
        result.put(JsonCntPage.locale.name(), cms.getRequestContext().getLocale().toString());
        result.put(JsonCntPage.recentListSize.name(), m_manager.getRecentListMaxSize(cms));

        // collect creatable type elements
        resElements.merge(getNewResourceTypes(cms.getSitePath(resource), types), true, false);
        // collect searchable type elements
        resElements.merge(getSearchResourceTypes(cms.getSitePath(resource), types), true, false);

        CmsElementUtil elemUtil = new CmsElementUtil(
            cms,
            getRequest().getParameter(ReqParam.uri.name()),
            getRequest(),
            getResponse());
        Set<String> ids = new HashSet<String>();

        // add the detail view element
        if (detailViewElement != null) {
            m_sessionCache.setCacheContainerElement(detailViewElement.getClientId(), detailViewElement);
            // check if the element already exists
            String id = detailViewElement.getClientId();
            // get the element data
            JSONObject resElement = elemUtil.getElementData(
                detailViewElement,
                elementContainerTypes.get(detailViewElement.getElementId()));
            // store element data
            ids.add(id);
            resElements.put(id, resElement);
        }

        // collect page elements
        for (Map.Entry<String, CmsContainerBean> entry : cntPage.getContainers().entrySet()) {
            CmsContainerBean container = entry.getValue();

            // get the actual number of elements to render
            int renderElems = container.getElements().size();
            if ((container.getMaxElements() > -1) && (renderElems > container.getMaxElements())) {
                renderElems = container.getMaxElements();
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
                if (ids.contains(id)) {
                    continue;
                }
                m_sessionCache.setCacheContainerElement(element.getClientId(), element);
                // get the element data
                JSONObject resElement = elemUtil.getElementData(
                    element,
                    elementContainerTypes.get(element.getElementId()));

                // get subcontainer elements
                if (resElement.has(CmsElementUtil.JsonElement.subItems.name())) {
                    // this container page should contain exactly one container
                    CmsResource elementRes = cms.readResource(element.getElementId());
                    CmsXmlSubContainer xmlSubContainer = CmsXmlSubContainerFactory.unmarshal(
                        cms,
                        elementRes,
                        getRequest());
                    CmsSubContainerBean subContainer = xmlSubContainer.getSubContainer(
                        cms,
                        cms.getRequestContext().getLocale());

                    // adding all sub-items to the elements data
                    for (CmsContainerElementBean subElement : subContainer.getElements()) {
                        if (!ids.contains(subElement.getElementId())) {
                            m_sessionCache.setCacheContainerElement(subElement.getClientId(), subElement);
                            String subId = subElement.getClientId();
                            if (ids.contains(subId)) {
                                continue;
                            }
                            JSONObject subItemData = elemUtil.getElementData(
                                subElement,
                                elementContainerTypes.get(subElement.getElementId()));
                            ids.add(subId);
                            resElements.put(subId, subItemData);
                        }
                    }
                }

                // store element data
                ids.add(id);
                resElements.put(id, resElement);
            }
        }
        // It's the client's responsibility to load contents for favorites/recent list
        // items when the user starts dragging them, so we don't load them here.
        Set<String> noTypes = Collections.<String> emptySet();
        // collect the favorites
        JSONArray resFavorites = getFavoriteList(resElements, noTypes);
        result.put(JsonCntPage.favorites.name(), resFavorites);
        // collect the recent list
        JSONArray resRecent = getRecentList(resElements, noTypes);
        result.put(JsonCntPage.recent.name(), resRecent);

        return result;
    }

    /**
     * Helper method which computes a map from element ids to the types of containers in which they occur in a container page.<p>
     * 
     * @param cms the CmsObject to use for VFS operations 
     * @param cntPage the container page bean
     * @param detailViewElement optional detail view element
     *  
     * @return a map from uuids to sets of container types
     *  
     * @throws CmsException if something goes wrong 
     */
    public Map<CmsUUID, Set<String>> getElementContainerTypes(
        CmsObject cms,
        CmsContainerPageBean cntPage,
        CmsContainerElementBean detailViewElement) throws CmsException {

        Map<CmsUUID, Set<String>> result = new HashMap<CmsUUID, Set<String>>();
        for (CmsContainerBean container : cntPage.getContainers().values()) {
            if (detailViewElement != null) {
                addToMultiMap(result, detailViewElement.getElementId(), container.getType());
            }
            for (CmsContainerElementBean element : container.getElements()) {
                addToMultiMap(result, element.getElementId(), container.getType());
                if (element.isSubcontainer(cms)) {
                    CmsResource elementRes = cms.readResource(element.getElementId());
                    // the unmarshalled subcontainer is cached in the request
                    CmsXmlSubContainer xmlSubContainer = CmsXmlSubContainerFactory.unmarshal(
                        cms,
                        elementRes,
                        getRequest());
                    CmsSubContainerBean subContainer = xmlSubContainer.getSubContainer(
                        cms,
                        cms.getRequestContext().getLocale());

                    for (CmsContainerElementBean subElement : subContainer.getElements()) {
                        addToMultiMap(result, subElement.getElementId(), container.getType());
                    }
                }
            }
        }
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
    public JSONArray getFavoriteList(JSONObject resElements, Collection<String> types) throws CmsException {

        JSONArray result = new JSONArray();
        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(
            cms,
            getRequest().getParameter(ReqParam.uri.name()),
            getRequest(),
            getResponse());

        // iterate the list and create the missing elements
        List<CmsContainerElementBean> favList = m_manager.getFavoriteList(cms);
        for (CmsContainerElementBean element : favList) {
            // checking if resource exists
            if (cms.existsResource(element.getElementId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                String id = element.getClientId();
                if ((resElements != null) && !resElements.has(id)) {
                    try {
                        JSONObject elemData = elemUtil.getElementData(element, types);
                        resElements.put(id, elemData);
                        if (elemData.has(CmsElementUtil.JsonElement.subItems.name())) {
                            // this container page should contain exactly one container
                            CmsResource elementRes = cms.readResource(element.getElementId());
                            CmsXmlSubContainer xmlSubContainer = CmsXmlSubContainerFactory.unmarshal(
                                cms,
                                elementRes,
                                getRequest());
                            CmsSubContainerBean subContainer = xmlSubContainer.getSubContainer(
                                cms,
                                cms.getRequestContext().getLocale());

                            // adding all sub-items to the elements data
                            for (CmsContainerElementBean subElement : subContainer.getElements()) {
                                String subId = subElement.getClientId();
                                if (!resElements.has(subId)) {
                                    JSONObject subItemData = elemUtil.getElementData(subElement, types);
                                    resElements.put(subId, subItemData);
                                    m_sessionCache.setCacheContainerElement(subId, subElement);
                                }
                            }
                        }
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
     * @return JSON object with 2 properties, {@link JsonResponse#elements} and {@link JsonSearch#hasmore}
     * 
     * @throws JSONException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    public JSONObject getLastSearchResult(String cntPageUri, CmsSearchOptions options, Set<String> types)
    throws JSONException, CmsException {

        CmsSearchOptions lastOptions = m_sessionCache.getSearchOptions();
        if ((lastOptions == null) || compareSearchOptions(lastOptions, options)) {
            return new JSONObject();
        }
        return getSearchResult(cntPageUri, lastOptions, types);
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
    public JSONArray getRecentList(JSONObject resElements, Collection<String> types) throws CmsException {

        JSONArray result = new JSONArray();
        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(
            cms,
            getRequest().getParameter(ReqParam.uri.name()),
            getRequest(),
            getResponse());

        // get the cached list
        List<CmsContainerElementBean> recentList = m_sessionCache.getRecentList();
        // iterate the list and create the missing elements
        for (CmsContainerElementBean element : recentList) {
            // checking if the resource exists
            if (cms.existsResource(element.getElementId(), CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                String id = element.getClientId();
                if ((resElements != null) && !resElements.has(id)) {
                    try {
                        JSONObject elemData = elemUtil.getElementData(element, types);
                        resElements.put(id, elemData);
                        if (elemData.has(CmsElementUtil.JsonElement.subItems.name())) {
                            // this container page should contain exactly one container
                            CmsResource elementRes = cms.readResource(element.getElementId());
                            CmsXmlSubContainer xmlSubContainer = CmsXmlSubContainerFactory.unmarshal(
                                cms,
                                elementRes,
                                getRequest());
                            CmsSubContainerBean subContainer = xmlSubContainer.getSubContainer(
                                cms,
                                cms.getRequestContext().getLocale());

                            // adding all sub-items to the elements data
                            for (CmsContainerElementBean subElement : subContainer.getElements()) {
                                String subId = subElement.getClientId();
                                if (!resElements.has(subId)) {
                                    JSONObject subItemData = elemUtil.getElementData(subElement, types);
                                    resElements.put(subId, subItemData);
                                    m_sessionCache.setCacheContainerElement(subId, subElement);
                                }
                            }
                        }
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
        }

        return result;
    }

    /**
     * Retrieves the search options from the JSON request.<p>
     * 
     * @param data the JSON request
     * 
     * @return the search options from the JSON request
     */
    public CmsSearchOptions getSearchOptions(JSONObject data) {

        String location = data.optString(JsonSearch.location.name());
        String text = data.optString(JsonSearch.text.name());
        String type = data.optString(JsonSearch.type.name());
        int page = 0;
        if (data.has(JsonSearch.page.name())) {
            page = data.optInt(JsonSearch.page.name());
        }
        CmsSearchOptions searchOptions = new CmsSearchOptions(location, text, type, page);
        return searchOptions;
    }

    /**
     * Returns elements for the search result matching the given options.<p>
     * 
     * @param cntPageUri the container page uri
     * @param options the search options
     * @param types the supported container types
     * 
     * @return JSON object with 3 properties, {@link JsonResponse#elements}, {@link JsonSearch#hasmore} and {@link JsonSearch#count}
     * 
     * @throws JSONException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    public JSONObject getSearchResult(String cntPageUri, CmsSearchOptions options, Set<String> types)
    throws JSONException, CmsException {

        CmsObject cms = getCmsObject();

        JSONObject result = new JSONObject();
        JSONArray elements = new JSONArray();
        result.put(JsonResponse.elements.name(), elements);

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
                    getRequest().getParameter(ReqParam.uri.name()),
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
                            resource.getStructureId(),
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
            result.put(JsonSearch.hasmore.name(), hasMore);
            result.put(JsonSearch.count.name(), searchBean.getSearchResultCount());
        } else {
            // no search
            result.put(JsonSearch.hasmore.name(), false);
            result.put(JsonSearch.count.name(), 0);
        }

        m_sessionCache.setCacheSearchOptions(options);

        return result;
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
    public void setContainerPage(String uri, JSONObject cntPage) throws CmsException, JSONException {

        CmsObject cms = getCmsObject();

        CmsXmlContainerPage xmlCnt = CmsXmlContainerPageFactory.unmarshal(cms, cms.readFile(uri));
        xmlCnt.save(cms, jsonToCntPage(cntPage));
    }

    /**
     * Saves the new state of the sub container.<p>
     * 
     * @param subcontainer the sub container data
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong parsing the JSON
     */
    public void setSubContainer(JSONObject subcontainer) throws CmsException, JSONException {

        CmsObject cms = getCmsObject();

        String resourceName = subcontainer.getString(CmsElementUtil.JsonElement.file.name());
        CmsXmlSubContainer xmlSubContainer = CmsXmlSubContainerFactory.unmarshal(cms, cms.readFile(resourceName));
        xmlSubContainer.save(cms, jsonToSubCnt(subcontainer));
    }

    /**
     * Returns a list of container elements from a json array with client ids.<p>
     * 
     * @param array the json array
     * 
     * @return a list of resource ids
     * 
     * @throws JSONException if something goes wrong
     */
    protected List<CmsContainerElementBean> arrayToElementList(JSONArray array) throws JSONException {

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
     * Reads the cached element-bean for the given client-side-id from cache.<p>
     * 
     * @param clientId the client-side-id
     * 
     * @return the cached container element bean
     */
    protected CmsContainerElementBean getCachedElement(String clientId) {

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
        element = new CmsContainerElementBean(m_manager.convertToServerId(id), null, null);
        m_sessionCache.setCacheContainerElement(id, element);
        return element;
    }

    /**
     * Returns the data of the given elements.<p>
     * 
     * @param elements the list of IDs of the elements to retrieve the data for
     * @param uriParam the current URI
     * @param request the current request
     * @param types the container types to consider
     * 
     * @return the elements data
     * 
     * @throws CmsException if something really bad happens
     * @throws JSONException if there is a problem with JSON operation
     */
    protected JSONObject getElements(JSONArray elements, String uriParam, HttpServletRequest request, Set<String> types)
    throws CmsException, JSONException {

        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, request, getResponse());
        JSONObject resElements = new JSONObject();
        Set<String> ids = new HashSet<String>();
        for (int i = 0; i < elements.length(); i++) {
            String elemId = elements.getString(i);
            if (ids.contains(elemId)) {
                continue;
            }
            CmsContainerElementBean element = getCachedElement(elemId);
            JSONObject elementData = elemUtil.getElementData(element, types);
            resElements.put(element.getClientId(), elementData);
            if (elementData.has(CmsElementUtil.JsonElement.subItems.name())) {
                // this is a sub-container 

                CmsResource elementRes = cms.readResource(element.getElementId());
                CmsXmlSubContainer xmlSubContainer = CmsXmlSubContainerFactory.unmarshal(cms, elementRes, getRequest());
                CmsSubContainerBean subContainer = xmlSubContainer.getSubContainer(
                    cms,
                    cms.getRequestContext().getLocale());

                // adding all sub-items to the elements data
                for (CmsContainerElementBean subElement : subContainer.getElements()) {
                    if (!ids.contains(subElement.getElementId())) {
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
            ids.add(elemId);
        }
        return resElements;
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
        resElements.put(JsonCntPage.newOrder.name(), new JSONArray());
        HttpServletRequest request = getRequest();
        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, cntPageUri, request, getResponse());

        Collection<CmsResource> creatableElements = m_manager.getCreatableElements(cms, cntPageUri, request);
        for (CmsResource creatableElement : creatableElements) {
            String type = OpenCms.getResourceManager().getResourceType(creatableElement).getTypeName();
            JSONObject resElement = elemUtil.getElementData(new CmsContainerElementBean(
                creatableElement.getStructureId(),
                null,
                null), types);
            // overwrite some special fields for new elements
            resElement.put(CmsElementUtil.JsonElement.id.name(), type);
            resElement.put(CmsElementUtil.JsonElement.status.name(), ELEMENT_NEWCONFIG);
            resElement.put(JsonResType.type.name(), type);
            resElement.put(JsonResType.typename.name(), CmsWorkplaceMessages.getResourceTypeName(
                cms.getRequestContext().getLocale(),
                type));
            resElements.put(type, resElement);
            // additional array to keep track of the order
            resElements.accumulate(JsonCntPage.newOrder.name(), type);
        }
        return resElements;
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
        resElements.put(JsonCntPage.searchOrder.name(), new JSONArray());
        HttpServletRequest request = getRequest();
        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, cntPageUri, request, getResponse());
        Collection<CmsResource> searchableElements = m_manager.getSearchableResourceTypes(cms, cntPageUri, request);
        for (CmsResource searchableElement : searchableElements) {
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(searchableElement);
            String typeName = type.getTypeName();
            JSONObject resElement = elemUtil.getElementData(new CmsContainerElementBean(
                searchableElement.getStructureId(),
                null,
                null), types);
            // overwrite some special fields for searchable elements
            resElement.put(CmsElementUtil.JsonElement.id.name(), typeName);
            resElement.put(CmsElementUtil.JsonElement.status.name(), ELEMENT_NEWCONFIG);
            resElement.put(JsonResType.type.name(), typeName);
            resElement.put(JsonResType.typeid.name(), type.getTypeId());
            resElement.put(JsonResType.typename.name(), CmsWorkplaceMessages.getResourceTypeName(
                cms.getRequestContext().getLocale(),
                typeName));
            resElements.put(typeName, resElement);
            // additional array to keep track of the order
            resElements.accumulate(JsonCntPage.searchOrder.name(), typeName);
        }
        return resElements;
    }

    /**
     * Converts the given JSON object into a container page bean.<p>
     * 
     * @param json the JSON object to convert
     * 
     * @return the created container page bean
     * 
     * @throws JSONException if something goes wrong while parsing JSON
     */
    protected CmsContainerPageBean jsonToCntPage(JSONObject json) throws JSONException {

        CmsObject cms = getCmsObject();

        List<CmsContainerBean> containers = new ArrayList<CmsContainerBean>();

        JSONObject jsonCnts = json.getJSONObject(JsonCntPage.containers.name());
        Iterator<String> itCnt = jsonCnts.keys();
        while (itCnt.hasNext()) {
            String cntKey = itCnt.next();
            JSONObject jsonCnt = jsonCnts.getJSONObject(cntKey);
            String name = jsonCnt.getString(CmsJspTagContainer.JsonContainer.name.name());
            String type = jsonCnt.getString(CmsJspTagContainer.JsonContainer.type.name());
            List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();

            JSONArray elems = jsonCnt.getJSONArray(JsonCntPage.elements.name());
            for (int i = 0; i < elems.length(); i++) {
                JSONObject jsonElem = elems.getJSONObject(i);

                String elemUri = jsonElem.getString(JsonCntElem.uri.name());
                CmsResource elem;
                try {
                    elem = cms.readResource(elemUri);
                } catch (CmsException e) {
                    // should never happen
                    LOG.error(e.getLocalizedMessage(), e);
                    continue;
                }
                String formatterUri = jsonElem.getString(JsonCntElem.formatter.name());
                CmsResource formatter;
                try {
                    formatter = cms.readResource(formatterUri);
                } catch (CmsException e) {
                    // should never happen
                    LOG.error(e.getLocalizedMessage(), e);
                    continue;
                }
                String clientId = jsonElem.getString(JsonCntElem.id.name());
                Map<String, String> props = new HashMap<String, String>();
                // checking if there are any properties to set
                if (clientId.contains("#")) {
                    props = getCachedElement(clientId).getProperties();
                }
                elements.add(new CmsContainerElementBean(elem.getStructureId(), formatter.getStructureId(), props));
            }
            containers.add(new CmsContainerBean(name, type, -1, elements));
        }
        return new CmsContainerPageBean(cms.getRequestContext().getLocale(), containers);
    }

    /**
     * Converts the given JSON object into a sub-container bean.<p>
     * 
     * @param json the JSON object to convert
     * 
     * @return the created sub-container bean
     * 
     * @throws JSONException if something goes wrong while parsing JSON
     */
    protected CmsSubContainerBean jsonToSubCnt(JSONObject json) throws JSONException {

        CmsObject cms = getCmsObject();

        String title = json.getString(CmsElementUtil.JsonElement.title.name());
        String description = json.getString(CmsElementUtil.JsonElement.description.name());

        JSONArray jsonTypes = json.getJSONArray(CmsElementUtil.JsonElement.types.name());
        List<String> types = new ArrayList<String>();
        for (int i = 0; i < jsonTypes.length(); i++) {
            String type = jsonTypes.getString(i);
            types.add(type);
        }
        List<CmsContainerElementBean> elements = new ArrayList<CmsContainerElementBean>();

        JSONArray elems = json.getJSONArray(CmsElementUtil.JsonElement.subItems.name());
        for (int i = 0; i < elems.length(); i++) {
            JSONObject jsonElem = elems.getJSONObject(i);

            String elemUri = jsonElem.getString(JsonCntElem.uri.name());
            CmsResource elem;
            try {
                elem = cms.readResource(elemUri);
            } catch (CmsException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
                continue;
            }
            String clientId = jsonElem.getString(JsonCntElem.id.name());
            Map<String, String> props = new HashMap<String, String>();
            // checking if there are any properties to set
            if (clientId.contains("#")) {
                props = getCachedElement(clientId).getProperties();
            }
            elements.add(new CmsContainerElementBean(elem.getStructureId(), null, props));
        }

        return new CmsSubContainerBean(title, description, elements, types);
    }
}

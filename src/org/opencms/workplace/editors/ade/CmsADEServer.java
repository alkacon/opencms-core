/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEServer.java,v $
 * Date   : $Date: 2009/09/01 08:44:21 $
 * Version: $Revision: 1.1.2.12 $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsContainerPageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
 * @version $Revision: 1.1.2.12 $
 * 
 * @since 7.6
 */
public class CmsADEServer extends CmsJspActionElement {

    /** Request path constant. */
    public static final String ACTION_GET = "/system/workplace/editors/ade/get.jsp";

    /** Request path constant. */
    public static final String ACTION_SET = "/system/workplace/editors/ade/set.jsp";

    /** User additional info key constant. */
    public static final String ADDINFO_ADE_FAVORITE_LIST = "ADE_FAVORITE_LIST";

    /** State Constant for client-side element type 'new element configuration'. */
    public static final String ELEMENT_NEWCONFIG = "NC";

    /** Mime type constant. */
    public static final String MIMETYPE_APPLICATION_JSON = "application/json";

    /** Request parameter obj value constant. */
    public static final String OBJ_ALL = "all";

    /** Request parameter obj value constant. */
    public static final String OBJ_CNT = "cnt";

    /** Request parameter obj value constant. */
    public static final Object OBJ_ELEM = "elem";

    /** Request parameter obj value constant. */
    public static final String OBJ_FAV = "fav";

    /** Request parameter obj value constant. */
    public static final String OBJ_LS = "ls";

    /** Request parameter obj value constant. */
    public static final String OBJ_NEW = "new";

    /** Request parameter obj value constant. */
    public static final String OBJ_REC = "rec";

    /** Request parameter obj value constant. */
    public static final String OBJ_SEARCH = "search";

    /** JSON property constant file. */
    public static final String P_ALLOWEDIT = "allowEdit";

    /** JSON property constant file. */
    public static final String P_ALLOWMOVE = "allowMove";

    /** JSON property constant containers. */
    public static final String P_CONTAINERS = "containers";

    /** JSON property constant contents. */
    public static final String P_CONTENTS = "contents";

    /** JSON property constant file. */
    public static final String P_DATE = "date";

    /** JSON property constant element. */
    public static final String P_ELEMENTS = "elements";

    /** JSON property constant favorites. */
    public static final String P_FAVORITES = "favorites";

    /** JSON property constant file. */
    public static final String P_FILE = "file";

    /** JSON property constant formatter. */
    public static final String P_FORMATTER = "formatter";

    /** JSON property constant formatters. */
    public static final String P_FORMATTERS = "formatters";

    /** JSON property constant element. */
    public static final String P_HASMORE = "hasmore";

    /** JSON property constant id. */
    public static final String P_ID = "id";

    /** JSON property constant file. */
    public static final String P_LOCALE = "locale";

    /** JSON property constant file. */
    public static final String P_LOCKED = "locked";

    /** JSON property constant file. */
    public static final String P_MAXELEMENTS = "maxElem";

    /** JSON property constant file. */
    public static final String P_NAME = "name";

    /** JSON property constant file. */
    public static final String P_NAVTEXT = "navText";

    /** JSON property constant recent. */
    public static final String P_RECENT = "recent";

    /** JSON property constant file. */
    public static final String P_STATUS = "status";

    /** JSON property constant file. */
    public static final String P_SUBITEMS = "subItems";

    /** JSON property constant file. */
    public static final String P_TITLE = "title";

    /** JSON property constant file. */
    public static final String P_TYPE = "type";

    /** JSON response property constant. */
    public static final String P_TYPENAME = "typename";

    /** JSON property constant uri. */
    public static final String P_URI = "uri";

    /** JSON property constant file. */
    public static final String P_USER = "user";

    /** Request parameter name constant. */
    public static final String PARAMETER_DATA = "data";

    /** Request parameter name constant. */
    public static final String PARAMETER_ELEM = "elem";

    /** Request parameter name constant. */
    public static final String PARAMETER_LOCALE = "locale";

    /** Request parameter name constant. */
    public static final String PARAMETER_LOCATION = "location";

    /** Request parameter name constant. */
    public static final String PARAMETER_OBJ = "obj";

    /** Request parameter name constant. */
    public static final String PARAMETER_PAGE = "page";

    /** Request parameter name constant. */
    public static final String PARAMETER_TEXT = "text";

    /** Request parameter obj value constant. */
    public static final String PARAMETER_TYPE = "type";

    /** Request parameter name constant. */
    public static final String PARAMETER_URL = "url";

    /** JSON response property constant. */
    public static final String RES_ERROR = "error";

    /** JSON response property constant. */
    public static final String RES_STATE = "state";

    /** JSON response state value constant. */
    public static final String RES_STATE_ERROR = "error";

    /** JSON response state value constant. */
    public static final String RES_STATE_OK = "ok";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEServer.class);

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsADEServer(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
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
            if (action.equals(ACTION_GET)) {
                result = executeActionGet();
            } else if (action.equals(ACTION_SET)) {
                result = executeActionSet();
            } else {
                result.put(RES_ERROR, Messages.get().getBundle().key(Messages.ERR_JSON_INVALID_ACTION_URL_1, action));
            }
        } catch (Exception e) {
            // a serious error occurred, should not...
            result.put(RES_ERROR, e.getMessage());
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

        String objParam = request.getParameter(PARAMETER_OBJ);
        if (objParam == null) {
            result.put(RES_ERROR, Messages.get().getBundle().key(Messages.ERR_JSON_MISSING_PARAMETER_1, PARAMETER_OBJ));
            return result;
        }
        String urlParam = request.getParameter(PARAMETER_URL);
        if (urlParam == null) {
            result.put(RES_ERROR, Messages.get().getBundle().key(Messages.ERR_JSON_MISSING_PARAMETER_1, PARAMETER_URL));
            return result;
        }

        CmsObject cms = getCmsObject();
        CmsResource cntPageRes = cms.readResource(urlParam);
        CmsContainerPageBean cntPage = CmsContainerPageCache.getInstance().getCache(
            cms,
            cntPageRes,
            cms.getRequestContext().getLocale());

        if (objParam.equals(OBJ_ALL)) {
            // first load, get everything
            result = getContainerPage(cntPageRes, cntPage);
        } else if (objParam.equals(OBJ_ELEM)) {
            // get element data
            String elemParam = request.getParameter(PARAMETER_ELEM);
            if (elemParam == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_ELEM));
                return result;
            }
            CmsElementUtil elemUtil = new CmsElementUtil(cms, request, getResponse());
            JSONObject resElements = new JSONObject();
            if (elemParam.startsWith("[")) {
                // element list
                JSONArray elems = new JSONArray(elemParam);
                for (int i = 0; i < elems.length(); i++) {
                    String elem = elems.getString(i);
                    resElements.put(elem, elemUtil.getElementData(CmsElementUtil.parseId(elem), cntPage.getTypes()));
                }
            } else {
                // single element
                resElements.put(elemParam, elemUtil.getElementData(
                    CmsElementUtil.parseId(elemParam),
                    cntPage.getTypes()));
            }
            result.put(P_ELEMENTS, resElements);
        } else if (objParam.equals(OBJ_FAV)) {
            // get the favorite list
            result.put(P_FAVORITES, getFavoriteList(null, cntPage.getTypes()));
        } else if (objParam.equals(OBJ_REC)) {
            // get recent list
            result.put(P_RECENT, CmsRecentListManager.getInstance().getRecentList(
                cms,
                null,
                cntPage.getTypes(),
                request,
                getResponse()));
        } else if (objParam.equals(OBJ_SEARCH)) {
            // new search
            String containerPageUri = request.getParameter(PARAMETER_URL);
            if (containerPageUri == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_URL));
                return result;
            }
            CmsSearchOptions searchOptions = new CmsSearchOptions(request);
            JSONObject searchResult = CmsSearchListManager.getInstance().getSearchResult(
                cms,
                searchOptions,
                cntPage.getTypes(),
                request,
                getResponse());
            result.merge(searchResult, true, false);
        } else if (objParam.equals(OBJ_LS)) {
            // last search
            String containerPageUri = request.getParameter(PARAMETER_URL);
            if (containerPageUri == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_URL));
                return result;
            }
            CmsSearchOptions searchOptions = new CmsSearchOptions(request);
            JSONObject searchResult = CmsSearchListManager.getInstance().getLastSearchResult(
                cms,
                searchOptions,
                cntPage.getTypes(),
                request,
                getResponse());
            result.merge(searchResult, true, false);
        } else if (objParam.equals(OBJ_NEW)) {
            // get a new element
            String dataParam = request.getParameter(PARAMETER_DATA);
            if (dataParam == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_DATA));
                return result;
            }
            String containerPageUri = request.getParameter(PARAMETER_URL);
            if (containerPageUri == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_URL));
                return result;
            }

            String type = dataParam;
            CmsElementCreator elemCreator = new CmsElementCreator(cms, cms.readResource(containerPageUri));

            CmsResource newResource = elemCreator.createElement(cms, type);
            result.put(P_ID, CmsElementUtil.createId(newResource.getStructureId()));
            result.put(P_URI, cms.getSitePath(newResource));
        } else {
            result.put(RES_ERROR, Messages.get().getBundle().key(
                Messages.ERR_JSON_WRONG_PARAMETER_VALUE_2,
                PARAMETER_OBJ,
                objParam));
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
        String objParam = getRequest().getParameter(PARAMETER_OBJ);
        if (objParam == null) {
            result.put(RES_ERROR, Messages.get().getBundle().key(Messages.ERR_JSON_MISSING_PARAMETER_1, PARAMETER_OBJ));
            return result;
        }
        if (objParam.equals(OBJ_FAV)) {
            // save the favorite list
            String dataParam = getRequest().getParameter(PARAMETER_DATA);
            if (dataParam == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_DATA));
                return result;
            }
            JSONArray list = new JSONArray(dataParam);
            setFavoriteList(getCmsObject(), list);
        } else if (objParam.equals(OBJ_REC)) {
            // save the recent list
            String dataParam = getRequest().getParameter(PARAMETER_DATA);
            if (dataParam == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_DATA));
                return result;
            }
            JSONArray list = new JSONArray(dataParam);
            CmsRecentListManager.getInstance().setRecentList(getCmsObject(), list);
        } else if (objParam.equals(OBJ_CNT)) {
            // save the container page
            String urlParam = getRequest().getParameter(PARAMETER_URL);
            if (urlParam == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_URL));
                return result;
            }
            String dataParam = getRequest().getParameter(PARAMETER_DATA);
            if (dataParam == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_DATA));
                return result;
            }
            JSONObject cntPage = new JSONObject(dataParam);
            setContainerPage(urlParam, cntPage);
        } else {
            result.put(RES_ERROR, Messages.get().getBundle().key(
                Messages.ERR_JSON_WRONG_PARAMETER_VALUE_2,
                PARAMETER_OBJ,
                objParam));
        }
        return result;
    }

    /**
     * Returns the data for the given container page.<p>
     * 
     * @param resource the container page's resource 
     * @param cntPage the container page to use
     * 
     * @return the data for the given container page
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected JSONObject getContainerPage(CmsResource resource, CmsContainerPageBean cntPage)
    throws CmsException, JSONException {

        CmsObject cms = getCmsObject();

        // create empty result object
        JSONObject result = new JSONObject();
        JSONObject resElements = new JSONObject();
        JSONObject resContainers = new JSONObject();
        result.put(P_ELEMENTS, resElements);
        result.put(P_CONTAINERS, resContainers);
        result.put(P_LOCALE, cms.getRequestContext().getLocale().toString());

        // get the container page itself
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
        Set<String> types = cntPage.getTypes();

        // collect some basic data
        result.put(CmsADEServer.P_ALLOWEDIT, resUtil.getLock().isLockableBy(cms.getRequestContext().currentUser())
            && resUtil.isEditable());
        result.put(CmsADEServer.P_LOCKED, resUtil.getLockedByName());

        // collect new elements
        resElements.merge(getNewElements(cntPage.getNewConfig(), types), true, false);

        // collect page elements
        CmsElementUtil elemUtil = new CmsElementUtil(cms, getRequest(), getResponse());
        Set<CmsUUID> ids = new HashSet<CmsUUID>();
        for (Map.Entry<String, CmsContainerBean> entry : cntPage.getContainers().entrySet()) {
            CmsContainerBean container = entry.getValue();

            // set the container data
            JSONObject resContainer = new JSONObject();
            resContainer.put(P_NAME, container.getName());
            resContainer.put(P_TYPE, container.getType());
            resContainer.put(P_MAXELEMENTS, container.getMaxElements());
            JSONArray resContainerElems = new JSONArray();
            resContainer.put(P_ELEMENTS, resContainerElems);

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

                // check if the element already exists
                String id = CmsElementUtil.createId(element.getElement().getStructureId());
                // collect ids
                resContainerElems.put(id);
                if (ids.contains(element.getElement().getStructureId())) {
                    continue;
                }
                // get the element data
                JSONObject resElement = elemUtil.getElementData(element.getElement(), types);
                // store element data
                ids.add(element.getElement().getStructureId());
                resElements.put(id, resElement);
            }

            resContainers.put(container.getName(), resContainer);
        }
        // collect the favorites
        JSONArray resFavorites = getFavoriteList(resElements, types);
        result.put(P_FAVORITES, resFavorites);
        // collect the recent list
        JSONArray resRecent = CmsRecentListManager.getInstance().getRecentList(
            cms,
            resElements,
            types,
            getRequest(),
            getResponse());
        result.put(P_RECENT, resRecent);

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
     * @throws JSONException if something goes wrong in the json manipulation
     */
    protected JSONArray getFavoriteList(JSONObject resElements, Collection<String> types)
    throws JSONException, CmsException {

        CmsObject cms = getCmsObject();
        HttpServletRequest req = getRequest();
        HttpServletResponse res = getResponse();

        CmsElementUtil elemUtil = new CmsElementUtil(cms, req, res);

        JSONArray result = getFavoriteListFromStore(cms);

        // iterate the list and create the missing elements
        for (int i = 0; i < result.length(); i++) {
            String id = result.optString(i);
            if ((resElements != null) && !resElements.has(id)) {
                resElements.put(id, elemUtil.getElementData(CmsElementUtil.parseId(id), types));
            }
        }

        return result;
    }

    /**
     * Returns the cached list, or creates it if not available.<p>
     * 
     * @param cms the current cms context
     * 
     * @return the cached recent list
     * 
     * @throws JSONException if something goes wrong
     */
    protected JSONArray getFavoriteListFromStore(CmsObject cms) throws JSONException {

        CmsUser user = cms.getRequestContext().currentUser();
        String favListStr = (String)user.getAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST);
        JSONArray favoriteList = new JSONArray();
        if (favListStr != null) {
            favoriteList = new JSONArray(favListStr);
        }
        return favoriteList;
    }

    /**
     * Returns the data for new elements from the given configuration file.<p>
     * 
     * @param newConfig the configuration file to use 
     * @param types the supported container page types
     * 
     * @return the data for the given container page
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected JSONObject getNewElements(CmsResource newConfig, Set<String> types) throws CmsException, JSONException {

        JSONObject resElements = new JSONObject();
        CmsElementUtil elemUtil = new CmsElementUtil(getCmsObject(), getRequest(), getResponse());
        CmsElementCreator creator = new CmsElementCreator(getCmsObject(), newConfig);
        Map<String, CmsTypeConfigurationItem> typeConfig = creator.getConfiguration();
        for (Map.Entry<String, CmsTypeConfigurationItem> entry : typeConfig.entrySet()) {
            String type = entry.getKey();
            String elementUri = entry.getValue().getSourceFile();
            JSONObject resElement = elemUtil.getElementData(elementUri, types);
            // overwrite some special fields for new elements
            resElement.put(P_ID, type);
            resElement.put(P_STATUS, ELEMENT_NEWCONFIG);
            resElement.put(P_TYPE, type);
            resElement.put(P_TYPENAME, CmsWorkplaceMessages.getResourceName(
                getCmsObject().getRequestContext().getLocale(),
                type));
            resElements.put(type, resElement);
        }
        return resElements;
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
        cms.lockResourceTemporary(uri);
        CmsFile containerPage = cms.readFile(uri);
        CmsXmlContent xmlCnt = CmsXmlContentFactory.unmarshal(cms, containerPage);

        Locale locale = CmsLocaleManager.getLocale(cntPage.getString(P_LOCALE));
        if (xmlCnt.hasLocale(locale)) {
            // remove the locale 
            xmlCnt.removeLocale(locale);
        }
        xmlCnt.addLocale(cms, locale);

        JSONObject cnts = cntPage.getJSONObject(P_CONTAINERS);
        int cntCount = 0;
        Iterator<String> itCnt = cnts.keys();
        while (itCnt.hasNext()) {
            String cntKey = itCnt.next();
            JSONObject cnt = cnts.getJSONObject(cntKey);

            I_CmsXmlContentValue cntValue = xmlCnt.getValue(CmsContainerPageLoader.N_CONTAINER, locale, cntCount);
            if (cntValue == null) {
                cntValue = xmlCnt.addValue(cms, CmsContainerPageLoader.N_CONTAINER, locale, cntCount);
            }

            String name = cnt.getString(P_NAME);
            xmlCnt.getValue(CmsXmlUtils.concatXpath(cntValue.getPath(), CmsContainerPageLoader.N_NAME), locale, 0).setStringValue(
                cms,
                name);

            String type = cnt.getString(P_TYPE);
            xmlCnt.getValue(CmsXmlUtils.concatXpath(cntValue.getPath(), CmsContainerPageLoader.N_TYPE), locale, 0).setStringValue(
                cms,
                type);

            JSONArray elems = cnt.getJSONArray(P_ELEMENTS);
            for (int i = 0; i < elems.length(); i++) {
                JSONObject elem = cnt.getJSONArray(P_ELEMENTS).getJSONObject(i);

                String formatter = elem.getString(P_FORMATTER);
                String elemUri = elem.getString(P_URI);

                I_CmsXmlContentValue elemValue = xmlCnt.addValue(cms, CmsXmlUtils.concatXpath(
                    cntValue.getPath(),
                    CmsContainerPageLoader.N_ELEMENT), locale, i);
                xmlCnt.getValue(CmsXmlUtils.concatXpath(elemValue.getPath(), CmsContainerPageLoader.N_URI), locale, 0).setStringValue(
                    cms,
                    elemUri);
                xmlCnt.getValue(
                    CmsXmlUtils.concatXpath(elemValue.getPath(), CmsContainerPageLoader.N_FORMATTER),
                    locale,
                    0).setStringValue(cms, formatter);
            }
            cntCount++;
        }
        containerPage.setContents(xmlCnt.marshal());
        cms.writeFile(containerPage);
    }

    /**
     * Sets the favorite list.<p>
     * 
     * @param cms the cms context
     * @param list the element id list
     * 
     * @throws CmsException if something goes wrong 
     */
    protected void setFavoriteList(CmsObject cms, JSONArray list) throws CmsException {

        CmsUser user = cms.getRequestContext().currentUser();
        user.setAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST, list.toString());
        cms.writeUser(user);
    }
}

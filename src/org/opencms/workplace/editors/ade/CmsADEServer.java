/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEServer.java,v $
 * Date   : $Date: 2009/08/25 10:37:25 $
 * Version: $Revision: 1.1.2.4 $
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
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsContainerPageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

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
 * @version $Revision: 1.1.2.4 $
 * 
 * @since 7.6
 */
public class CmsADEServer extends CmsJspActionElement {

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

    /** JSON property constant formatters. */
    public static final String P_FORMATTERS = "formatters";

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

    /** JSON property constant file. */
    public static final String P_USER = "user";

    /** JSON response property constant. */
    public static final String RES_ERROR = "error";

    /** JSON response property constant. */
    public static final String RES_STATE = "state";

    /** JSON response state value constant. */
    public static final String RES_STATE_ERROR = "error";

    /** JSON response state value constant. */
    public static final String RES_STATE_OK = "ok";

    /** Request path constant. */
    protected static final String ACTION_GET = "/system/workplace/editors/ade/get.jsp";

    /** Request path constant. */
    protected static final String ACTION_SET = "/system/workplace/editors/ade/set.jsp";

    /** Mime type constant. */
    protected static final String MIMETYPE_APPLICATION_JSON = "application/json";

    /** Request parameter obj value constant. */
    protected static final String OBJ_ALL = "all";

    /** Request parameter obj value constant. */
    protected static final String OBJ_CNT = "cnt";

    /** Request parameter obj value constant. */
    protected static final Object OBJ_ELEM = "elem";

    /** Request parameter obj value constant. */
    protected static final String OBJ_FAV = "fav";

    /** Request parameter obj value constant. */
    protected static final String OBJ_REC = "rec";

    /** Request parameter obj value constant. */
    protected static final String OBJ_NEW = "new";

    /** Request parameter obj value constant. */
    protected static final String PARAMETER_TYPE = "type";

    /** Request parameter name constant. */
    protected static final String PARAMETER_DATA = "data";

    /** Request parameter name constant. */
    protected static final String PARAMETER_ELEM = "elem";

    /** Request parameter name constant. */
    protected static final String PARAMETER_LOCALE = "locale";

    /** Request parameter name constant. */
    protected static final String PARAMETER_OBJ = "obj";

    /** Request parameter name constant. */
    protected static final String PARAMETER_URL = "url";

    /**
     * Path to the configuration file. 
     */
    protected static final String NEW_CONFIG_PATH = "/system/workplace/editors/ade/type_config.xml";

    /** Container page loader reference. */
    private static final CmsContainerPageLoader LOADER = (CmsContainerPageLoader)OpenCms.getResourceManager().getLoader(
        CmsContainerPageLoader.RESOURCE_LOADER_ID);

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
        String objParam = getRequest().getParameter(PARAMETER_OBJ);
        if (objParam == null) {
            result.put(RES_ERROR, Messages.get().getBundle().key(Messages.ERR_JSON_MISSING_PARAMETER_1, PARAMETER_OBJ));
            return result;
        }
        String urlParam = getRequest().getParameter(PARAMETER_URL);
        if (urlParam == null) {
            result.put(RES_ERROR, Messages.get().getBundle().key(Messages.ERR_JSON_MISSING_PARAMETER_1, PARAMETER_URL));
            return result;
        }
        if (objParam.equals(OBJ_ALL)) {
            result = getContainerPage(urlParam);
        } else if (objParam.equals(OBJ_ELEM)) {
            String elemParam = getRequest().getParameter(PARAMETER_ELEM);
            if (elemParam == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_ELEM));
                return result;
            }
            JSONObject resElements = new JSONObject();
            if (elemParam.startsWith("[")) {
                // element list
                JSONArray elems = new JSONArray(elemParam);
                for (int i = 0; i < elems.length(); i++) {
                    String elem = elems.getString(i);
                    resElements.put(elem, CmsADEElementManager.getInstance().getElementData(
                        getCmsObject(),
                        CmsADEElementManager.getInstance().parseId(elem),
                        urlParam,
                        getRequest(),
                        getResponse()));
                }
            } else {
                // single element
                resElements.put(elemParam, CmsADEElementManager.getInstance().getElementData(
                    getCmsObject(),
                    CmsADEElementManager.getInstance().parseId(elemParam),
                    urlParam,
                    getRequest(),
                    getResponse()));
            }
            result.put(P_ELEMENTS, resElements);
        } else if (objParam.equals(OBJ_FAV)) {
            result.put(P_FAVORITES, CmsADEElementManager.getInstance().getFavoriteList(
                getCmsObject(),
                null,
                urlParam,
                getRequest(),
                getResponse()));
        } else if (objParam.equals(OBJ_REC)) {
            result.put(P_RECENT, CmsADEElementManager.getInstance().getRecentList(
                getCmsObject(),
                null,
                urlParam,
                getRequest(),
                getResponse()));
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
            String dataParam = getRequest().getParameter(PARAMETER_DATA);
            if (dataParam == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_DATA));
                return result;
            }
            JSONArray list = new JSONArray(dataParam);
            CmsADEElementManager.getInstance().setFavoriteList(getCmsObject(), list);
        } else if (objParam.equals(OBJ_REC)) {
            String dataParam = getRequest().getParameter(PARAMETER_DATA);
            if (dataParam == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_DATA));
                return result;
            }
            JSONArray list = new JSONArray(dataParam);
            CmsADEElementManager.getInstance().setRecentList(getCmsObject(), list);
        } else if (objParam.equals(OBJ_CNT)) {
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

        } else if (objParam.equals(OBJ_NEW)) {
            CmsObject cms = getCmsObject();
            String dataParam = getRequest().getParameter(PARAMETER_DATA);
            if (dataParam == null) {
                result.put(RES_ERROR, Messages.get().getBundle().key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    PARAMETER_DATA));
                return result;
            }
            JSONArray dataArray = new JSONArray(dataParam);
            String type = dataArray.getString(0);

            try {
                CmsADEElementCreator elemCreator = new CmsADEElementCreator(
                    cms,
                    "/system/workplace/editors/ade/type_config.xml");

                CmsResource newResource = elemCreator.createElement(cms, type);
                result.put(P_ID, "ade_" + newResource.getStructureId().toString());
                result.put(P_FILE, cms.getSitePath(newResource));
            } catch (Exception e) {
                LOG.error(e);
                for (StackTraceElement el : e.getStackTrace()) {
                    LOG.error("at " + el.toString());
                }
                result.put(RES_ERROR, e.toString());
            }

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
     * @param uri the uri of the container page to use
     * 
     * @return the data for the given container page
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected JSONObject getContainerPage(String uri) throws CmsException, JSONException {

        CmsObject cms = getCmsObject();

        // create empty result object
        JSONObject result = new JSONObject();
        JSONObject resElements = new JSONObject();
        JSONObject resContainers = new JSONObject();
        result.put(P_ELEMENTS, resElements);
        result.put(P_CONTAINERS, resContainers);
        result.put(P_LOCALE, cms.getRequestContext().getLocale().toString());

        // get the container page itself
        CmsResource containerPage = cms.readResource(uri);
        JSONObject containers = LOADER.getCache(cms, containerPage, cms.getRequestContext().getLocale());
        Collection types = CmsADEElementManager.getInstance().getContainerPageTypes(cms, uri);

        CmsADEElementCreator creator = new CmsADEElementCreator(cms, "/system/workplace/editors/ade/type_config.xml");
        Map<String, CmsADETypeConfigurationItem> typeConfig = creator.getConfiguration();
        for (Map.Entry<String, CmsADETypeConfigurationItem> entry : typeConfig.entrySet()) {
            String type = entry.getKey();
            String elementUri = entry.getValue().getSourceFile();
            JSONObject resElement = CmsADEElementManager.getInstance().getElementData(
                cms,
                cms.readResource(elementUri),
                types,
                getRequest(),
                getResponse());
            resElement.put(P_ID, type);
            resElement.put(P_STATUS, "n");
            resElement.put(P_TYPE, type);
            resElements.put(type, resElement);
        }

        Map<String, String> ids = new HashMap<String, String>();

        Iterator itKeys = containers.keys();
        while (itKeys.hasNext()) {
            String containerName = (String)itKeys.next();
            JSONObject container = containers.getJSONObject(containerName);

            // get the name
            String name = container.getString(CmsContainerPageLoader.N_NAME);
            // get the type
            String type = container.getString(CmsContainerPageLoader.N_TYPE);
            // get the maximal elements
            int maxElements = container.getInt(CmsContainerPageLoader.N_MAXELEMENTS);

            // set the container data
            JSONObject resContainer = new JSONObject();
            resContainer.put(P_NAME, name);
            resContainer.put(P_TYPE, type);
            resContainer.put(P_MAXELEMENTS, maxElements);
            JSONArray resContainerElems = new JSONArray();
            resContainer.put(P_ELEMENTS, resContainerElems);

            // iterate the elements
            JSONArray elements = container.optJSONArray(CmsContainerPageLoader.N_ELEMENT);
            // get the actual number of elements to render
            int renderElems = elements.length();
            if ((maxElements > -1) && (renderElems > maxElements)) {
                renderElems = maxElements;
            }
            for (int i = 0; i < renderElems; i++) {
                JSONObject element = elements.optJSONObject(i);
                String elementUri = element.optString(CmsContainerPageLoader.N_URI);

                // check if the element already exists
                String id = ids.get(elementUri);
                if (id != null) {
                    // collect ids
                    resContainerElems.put(id);
                    continue;
                }
                // get the element data
                JSONObject resElement = CmsADEElementManager.getInstance().getElementData(
                    cms,
                    cms.readResource(elementUri),
                    types,
                    getRequest(),
                    getResponse());
                // store element data
                id = resElement.getString(P_ID);
                ids.put(elementUri, id);
                resElements.put(id, resElement);
                // collect ids
                resContainerElems.put(id);
            }

            resContainers.put(name, resContainer);
        }
        // get the favorites
        JSONArray resFavorites = CmsADEElementManager.getInstance().getFavoriteList(
            cms,
            resElements,
            uri,
            getRequest(),
            getResponse());
        result.put(P_FAVORITES, resFavorites);
        // get the recent list
        JSONArray resRecent = CmsADEElementManager.getInstance().getRecentList(
            cms,
            resElements,
            uri,
            getRequest(),
            getResponse());
        result.put(P_RECENT, resRecent);

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
        Iterator itCnt = cnts.keys();
        while (itCnt.hasNext()) {
            String cntKey = (String)itCnt.next();
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
                String elem = elems.getString(i);

                CmsUUID id = CmsADEElementManager.getInstance().parseId(elem);
                CmsResource res = cms.readResource(id);

                String formatter = cnt.getJSONArray(P_FORMATTERS).getString(i);

                I_CmsXmlContentValue elemValue = xmlCnt.addValue(cms, CmsXmlUtils.concatXpath(
                    cntValue.getPath(),
                    CmsContainerPageLoader.N_ELEMENT), locale, i);
                xmlCnt.getValue(CmsXmlUtils.concatXpath(elemValue.getPath(), CmsContainerPageLoader.N_URI), locale, 0).setStringValue(
                    cms,
                    cms.getSitePath(res));
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

}

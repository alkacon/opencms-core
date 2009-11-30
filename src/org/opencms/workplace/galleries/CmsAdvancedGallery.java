/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsAdvancedGallery.java,v $
 * Date   : $Date: 2009/11/30 12:43:31 $
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

package org.opencms.workplace.galleries;

import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.galleries.CmsGallerySearchServer.JsonKeys;
import org.opencms.workplace.galleries.CmsGallerySearchServer.ReqParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Bean to be used in advanced galleries.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 7.6
 * 
 */
public class CmsAdvancedGallery extends CmsJspActionElement {

    /** The advanced gallery path to the JSPs in the workplace. */
    public static final String ADVANCED_GALLERY_PATH = "/system/workplace/resources/editors/ade/galleries.jsp";

    /** The advanced gallery search server path to the JSP in the workplace. */
    public static final String ADVANCED_GALLERY_SERVER_PATH = "/system/workplace/galleries/gallerySearch.jsp";

    /** Request parameter name for the dialog mode (widget, editor, view, ade or sitemap). */
    public static final String PARAM_DIALOGMODE = "dialogmode";

    /** Request parameter value for the dialog mode: editor. */
    public static final String MODE_EDITOR = "editor";

    /** Request parameter value for the dialog mode: view. */
    public static final String MODE_VIEW = "view";

    /** Request parameter value for the dialog mode: widget. */
    public static final String MODE_WIDGET = "widget";

    /** The gallery search server. */
    private CmsGallerySearchServer m_gallerySearchServer;

    /** The JSON data request object. */
    private JSONObject m_reqDataObj;

    /**
     * Empty constructor, required for every JavaBean.
     */
    public CmsAdvancedGallery() {

        super();
    }

    /**
     * Constructor, with parameters.
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsAdvancedGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super();
        init(context, req, res);

    }

    /**
     * Returns the gallery search server.<p>
     * 
     * @return the gallery search server
     */
    private CmsGallerySearchServer getGallerySearchServer() {

        if (m_gallerySearchServer == null) {
            m_gallerySearchServer = new CmsGallerySearchServer(getJspContext(), getRequest(), getResponse());
        }
        return m_gallerySearchServer;
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
            // TODO: add error handling
        }
        CmsGallerySearchServer gs = getGallerySearchServer();
        JSONObject result = gs.getAllLists(resourceTypesParam, getModeName());
        return result.toString();
    }

    /**
     * Returns the JSON as string for the initial search. All search parameters are taken from the request parameters.<p>
     * 
     * @return the search result data as JSON string
     * 
     * @throws JSONException if something goes wrong
     * @throws CmsException  if something goes wrong
     */
    public String getInitialSearch() throws JSONException, CmsException {

        JSONObject result = new JSONObject();
        JSONObject data = getReqDataObj();
        if ((data != null) && data.has(JsonKeys.RESOURCEPATH.getName())) {
            String resourcePath = data.getString(JsonKeys.RESOURCEPATH.getName());
            result = getGallerySearchServer().findResourceInGallery(resourcePath);
        } else if ((data != null) && data.has(JsonKeys.QUERYDATA.getName())) {
            JSONObject queryData = data.getJSONObject(JsonKeys.QUERYDATA.getName());
            result.put(JsonKeys.QUERYDATA.getName(), queryData);
            result.put(JsonKeys.SEARCHRESULT.getName(), getGallerySearchServer().search(queryData));
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

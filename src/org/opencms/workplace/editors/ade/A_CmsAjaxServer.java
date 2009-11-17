/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/A_CmsAjaxServer.java,v $
 * Date   : $Date: 2009/11/17 07:42:26 $
 * Version: $Revision: 1.2 $
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
import org.opencms.flex.CmsFlexController;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsRequestUtil;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Skeleton for an Ajax server used for client/server communication.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 7.9
 */
public abstract class A_CmsAjaxServer extends CmsJspActionElement {

    /** Json property name constants for responses. */
    protected enum JsonResponse {

        /** The error message. */
        ERROR("error"),
        /** The response state. */
        STATE("state");

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

    /** Json property name constants for response status. */
    protected enum JsonState {

        /** An error occurred. */
        ERROR("error"),
        /** The request was executed with any problems. */
        OK("ok");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonState(String name) {

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
    private static final Log LOG = CmsLog.getLog(A_CmsAjaxServer.class);

    /** The workplace locale from the current user's settings. */
    private Locale m_wpLocale;

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public A_CmsAjaxServer(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Handles all requests.<p>
     * 
     * @return the result
     * 
     * @throws Exception if there is a problem
     */
    public abstract JSONObject executeAction() throws Exception;

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
            result = executeAction();
        } catch (Exception e) {
            // a serious error occurred, should not...
            error(result, e.getLocalizedMessage() == null ? "NPE" : e.getLocalizedMessage());
            LOG.error(Messages.get().getBundle().key(
                Messages.ERR_SERVER_EXCEPTION_1,
                CmsRequestUtil.appendParameters(
                    getRequest().getRequestURL().toString(),
                    CmsRequestUtil.createParameterMap(getRequest().getQueryString()),
                    false)), e);
        }
        // add state info
        if (result.has(JsonResponse.ERROR.getName())) {
            // add state=error in case an error occurred 
            result.put(JsonResponse.STATE.getName(), JsonState.ERROR.getName());
        } else if (!result.has(JsonResponse.STATE.getName())) {
            // add state=ok i case no error occurred
            result.put(JsonResponse.STATE.getName(), JsonState.OK.getName());
        }
        // write the result
        result.write(getResponse().getWriter());
    }

    /**
     * Checks whether a list of parameters are present as attributes of a request.<p>
     * 
     * If this isn't the case, an error message is written to the JSON result object.
     * 
     * @param request the request which contains the parameters
     * @param result the JSON object which the error message should be written into, can be <code>null</code>
     * @param params the array of parameters which should be checked
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
                error(result, Messages.get().getBundle(getWorkplaceLocale()).key(
                    Messages.ERR_JSON_MISSING_PARAMETER_1,
                    param));
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether a list of parameters are present as attributes of a request.<p>
     * 
     * If this isn't the case, an error message is written to the JSON result object.
     * 
     * @param data the JSONObject data which contains the parameters
     * @param result the JSON object which the error message should be written into, can be <code>null</code>
     * @param params the array of parameters which should be checked
     * 
     * @return <code>true</code> if and only if all parameters are present in the given data
     * 
     * @throws JSONException if something goes wrong with JSON
     */
    protected boolean checkParameters(JSONObject data, JSONObject result, String... params) throws JSONException {

        for (String param : params) {
            if (!data.has(param)) {
                if (result != null) {
                    error(result, Messages.get().getBundle(getWorkplaceLocale()).key(
                        Messages.ERR_JSON_MISSING_PARAMETER_1,
                        param));
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Adds an error message to the current response.<p>
     * 
     * @param result the current response
     * @param errorMessage the error message
     * 
     * @throws JSONException if there is any problem with JSON
     */
    protected void error(JSONObject result, String errorMessage) throws JSONException {

        result.put(JsonResponse.ERROR.getName(), errorMessage);
        LOG.error(errorMessage);
    }

    /**
     * Adds an error message to the current response.<p>
     * 
     * @param result the current response
     * @param error the error
     * 
     * @throws JSONException if there is any problem with JSON
     */
    protected void error(JSONObject result, Throwable error) throws JSONException {

        String msg = error.getLocalizedMessage();
        if (error instanceof CmsException) {
            msg = ((CmsException)error).getLocalizedMessage(getWorkplaceLocale());
        } else if (error instanceof CmsRuntimeException) {
            msg = ((CmsRuntimeException)error).getLocalizedMessage(getWorkplaceLocale());
        }
        result.put(JsonResponse.ERROR.getName(), msg);
        LOG.error(error.getMessage(), error);
    }

    /**
     * Returns the workplace locale from the current user's settings.<p>
     * 
     * @return the workplace locale
     */
    protected Locale getWorkplaceLocale() {

        if (m_wpLocale == null) {
            m_wpLocale = new CmsUserSettings(getCmsObject().getRequestContext().currentUser()).getLocale();
            if (m_wpLocale == null) {
                // fall back
                m_wpLocale = getCmsObject().getRequestContext().getLocale();
            }
        }
        return m_wpLocale;
    }
}

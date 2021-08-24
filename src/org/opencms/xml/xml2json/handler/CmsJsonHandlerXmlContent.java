/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.xml.xml2json.handler;

import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;
import org.opencms.xml.xml2json.CmsJsonRequest;
import org.opencms.xml.xml2json.CmsJsonResult;
import org.opencms.xml.xml2json.document.CmsJsonDocumentXmlContent;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Sub-handler for converting XML contents to JSON, either as a whole or just specific locales or paths.
 */
public class CmsJsonHandlerXmlContent implements I_CmsJsonHandler {

    /**
     * Exception thrown when path lookup fails.
     */
    public static class PathNotFoundException extends Exception {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new exception for a given message.<p>
         *
         * @param string the message
         */
        public PathNotFoundException(String string) {

            super(string);
        }
    }

    /** Request parameter name. */
    public static final String PARAM_LOCALE = "locale";

    /** Request parameter name. */
    public static final String PARAM_PATH = "path";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJsonHandlerXmlContent.class);

    /**
     * Creates an empty JSON object.
     *
     * @return the empty JSON object
     */
    public static JSONObject empty() {

        try {
            return new JSONObject("{}");
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * @see org.opencms.xml.xml2json.handler.I_CmsJsonHandler#getOrder()
     */
    public double getOrder() {

        return 100.0;

    }

    /**
     * @see org.opencms.xml.xml2json.handler.I_CmsJsonHandler#matches(org.opencms.xml.xml2json.handler.CmsJsonHandlerContext)
     */
    public boolean matches(CmsJsonHandlerContext context) {

        return CmsResourceTypeXmlContent.isXmlContent(context.getResource())
            && !CmsResourceTypeXmlContainerPage.isContainerPage(context.getResource());
    }

    /**
     * @see org.opencms.xml.xml2json.handler.I_CmsJsonHandler#renderJson(org.opencms.xml.xml2json.handler.CmsJsonHandlerContext)
     */
    public CmsJsonResult renderJson(CmsJsonHandlerContext context) {

        return renderJson(context, true);
    }

    /**
     * Renders a JSON representation of an XML content.<p>
     *
     * @param context the handler context
     * @param throwError whether to throw an error if a locale or path selection fails
     * @return the XML content as JSON
     */
    public CmsJsonResult renderJson(CmsJsonHandlerContext context, boolean throwError) {

        try {
            CmsJsonRequest jsonRequest = new CmsJsonRequest(context, this);
            jsonRequest.validate();
            if (jsonRequest.hasErrors()) {
                return new CmsJsonResult(jsonRequest.getErrorsAsJson(), HttpServletResponse.SC_BAD_REQUEST);
            }
            CmsJsonDocumentXmlContent jsonDocument = new CmsJsonDocumentXmlContent(jsonRequest, context.getContent());
            return new CmsJsonResult(jsonDocument.getJson(), HttpServletResponse.SC_OK);
        } catch (JSONException | PathNotFoundException e) {
            LOG.info(e.getLocalizedMessage(), e);
            return new CmsJsonResult(e.getLocalizedMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (CmsJsonHandlerException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            return new CmsJsonResult(e.getLocalizedMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return new CmsJsonResult(e.getLocalizedMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}

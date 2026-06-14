/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.test.mock;

import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.I_CmsRequestHandler;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * Generic test helper that dispatches a {@link CmsMockHttpServletRequest} to an
 * {@link I_CmsRequestHandler} and captures the response status and body, for asserting OpenCms
 * request handlers (such as the MCP handler) without a servlet container.<p>
 *
 * @see CmsMockHttpServletRequest
 * @see CmsMockHttpServletResponse
 */
public final class CmsRequestHandlerTestDispatcher {

    /**
     * The captured result of a request handler call: the HTTP status and the response body.<p>
     */
    public static final class Result {

        /** The response body. */
        private final String m_body;

        /** The HTTP status. */
        private final int m_status;

        /**
         * Creates a new result.<p>
         *
         * @param status the HTTP status
         * @param body the response body
         */
        Result(int status, String body) {

            m_status = status;
            m_body = body;
        }

        /**
         * Returns the raw response body.<p>
         *
         * @return the response body
         */
        public String getBody() {

            return m_body;
        }

        /**
         * Returns the HTTP status.<p>
         *
         * @return the HTTP status
         */
        public int getStatus() {

            return m_status;
        }

        /**
         * Parses the response body as a JSON object.<p>
         *
         * @return the response body as a JSON object
         *
         * @throws JSONException if the body is not a JSON object
         */
        public JSONObject json() throws JSONException {

            return new JSONObject(m_body);
        }
    }

    /**
     * Hides the public constructor.
     */
    private CmsRequestHandlerTestDispatcher() {

        // utility class
    }

    /**
     * Dispatches the given request to the handler, using the handler's first registered name, and
     * returns the captured response.<p>
     *
     * @param handler the request handler to invoke
     * @param request the request to dispatch
     *
     * @return the captured response
     *
     * @throws IOException if the handler throws it
     * @throws ServletException if the handler throws it
     */
    public static Result dispatch(I_CmsRequestHandler handler, CmsMockHttpServletRequest request)
    throws IOException, ServletException {

        String name = handler.getHandlerNames().length > 0 ? handler.getHandlerNames()[0] : "";
        return dispatch(handler, request, name);
    }

    /**
     * Dispatches the given request to the handler under the given handler name and returns the
     * captured response.<p>
     *
     * @param handler the request handler to invoke
     * @param request the request to dispatch
     * @param name the handler name to invoke
     *
     * @return the captured response
     *
     * @throws IOException if the handler throws it
     * @throws ServletException if the handler throws it
     */
    public static Result dispatch(I_CmsRequestHandler handler, CmsMockHttpServletRequest request, String name)
    throws IOException, ServletException {

        CmsMockHttpServletResponse response = new CmsMockHttpServletResponse();
        handler.handle(request, response, name);
        return new Result(response.getStatus(), response.getContentAsString());
    }
}

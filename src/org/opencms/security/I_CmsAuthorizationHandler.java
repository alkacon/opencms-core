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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.security;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Defines general authorization methods.<p>
 *
 * One of the application scenarios for this interface is a personalized SSO implementation.<p>
 *
 * @since 6.5.4
 */
public interface I_CmsAuthorizationHandler {

    /**
     * Class providing the privileged login action.<p>
     */
    interface I_PrivilegedLoginAction {

        /**
         * Used to provide an initial cms object.<p>
         *
         * @param cms an initial cms object
         */
        void setCmsObject(CmsObject cms);

        /**
         * Returns the cms object.<p>
         *
         * @return the cms object
         */
        CmsObject getCmsObject();

        /**
         * Performs a privileged login action and returns a cms object initialized for the principal.<p>
         *
         * @param request the current request
         * @param principal the principal to login
         *
         * @return a cms object initialized for the principal
         * @throws CmsException if the login action fails
         */
        CmsObject doLogin(HttpServletRequest request, String principal) throws CmsException;
    }

    /**
     * Returns the full URL used to call a login form with additional parameters and a callbackURL.<p>
     *
     * @param loginFormURL the form URL specified in the cms (either as a property or system-wide)
     * @param params additional parameters to provide to the login form
     * @param callbackURL the call-back URL to redirect after a successful login
     *
     * @return the full URL used to call a login form
     */
    String getLoginFormURL(String loginFormURL, String params, String callbackURL);

    /**
     * Creates a new cms object from the given request object.<p>
     *
     * This method is called by OpenCms every time a resource is requested
     * and the session can not automatically be authenticated.<p>
     *
     * @param request the HTTP request to authenticate
     *
     * @return the cms context object associated to the current session
     */
    CmsObject initCmsObject(HttpServletRequest request);

    /**
     * Creates a new cms object from the given request object.<p>
     *
     * This method is called by OpenCms every time a resource is requested
     * and the session can not automatically be authenticated.<p>
     *
     * @param request the HTTP request to authenticate
     * @param loginAction the privileged login action
     *
     * @return the cms context object associated to the current session
     */
    CmsObject initCmsObject(HttpServletRequest request, I_PrivilegedLoginAction loginAction);

    /**
     * Authenticates the current request with additional user information.<p>
     *
     * You have to call this method by your own.<p>
     *
     * @param request the HTTP request to authenticate
     * @param userName the user name to authenticate
     * @param pwd the user password to authenticate with
     *
     * @return the cms context object associated to the given user
     *
     * @throws CmsException if something goes wrong
     */
    CmsObject initCmsObject(HttpServletRequest request, String userName, String pwd) throws CmsException;

    /**
     * This method sends a request to the client to display a login form,
     * it is needed for HTTP-Authentication.<p>
     *
     * @param req the client request
     * @param res the response
     * @param loginFormURL the full URL used for form based authentication
     *
     * @throws IOException if something goes wrong
     */
    void requestAuthorization(HttpServletRequest req, HttpServletResponse res, String loginFormURL) throws IOException;

    /**
     * Sets parameters which can be configured additionally for an authorization handler.<p>
     *
     * @param parameters the map of parameters
     */
    void setParameters(Map<String, String> parameters);
}
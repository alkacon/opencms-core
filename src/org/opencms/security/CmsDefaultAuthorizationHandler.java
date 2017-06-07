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
import org.opencms.main.A_CmsAuthorizationHandler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsHttpAuthenticationSettings;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.workplace.CmsWorkplaceManager;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

/**
 * Defines default authorization methods.<p>
 *
 * @since 6.5.4
 */
public class CmsDefaultAuthorizationHandler extends A_CmsAuthorizationHandler {

    /** Basic authorization prefix constant. */
    public static final String AUTHORIZATION_BASIC_PREFIX = "BASIC ";
    /** Authorization header constant. */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    /** Credentials separator constant. */
    public static final String SEPARATOR_CREDENTIALS = ":";

    /**
     * @see org.opencms.security.I_CmsAuthorizationHandler#getLoginFormURL(java.lang.String, java.lang.String, java.lang.String)
     */
    public String getLoginFormURL(String loginFormURL, String params, String callbackURL) {

        if (loginFormURL != null) {

            StringBuffer fullURL = new StringBuffer(loginFormURL);
            if (callbackURL != null) {
                fullURL.append("?");
                fullURL.append(CmsWorkplaceManager.PARAM_LOGIN_REQUESTED_RESOURCE);
                fullURL.append("=");
                fullURL.append(callbackURL);
            }
            if (params != null) {
                fullURL.append((callbackURL != null) ? "&" : "?");
                fullURL.append(params);
            }

            return fullURL.toString();
        }

        return null;
    }

    /**
     * @see I_CmsAuthorizationHandler#initCmsObject(HttpServletRequest)
     */
    public CmsObject initCmsObject(HttpServletRequest request) {

        // check if "basic" authorization data is provided
        CmsObject cms = checkBasicAuthorization(request);
        // basic authorization successful?
        if (cms != null) {
            try {
                // register the session into OpenCms and
                // return successful logged in user
                return registerSession(request, cms);
            } catch (CmsException e) {
                // ignore and threat the whole login process as failed
            }
        }
        // failed
        return null;
    }

    /**
     * @see org.opencms.security.I_CmsAuthorizationHandler#initCmsObject(javax.servlet.http.HttpServletRequest, org.opencms.security.I_CmsAuthorizationHandler.I_PrivilegedLoginAction)
     */
    public CmsObject initCmsObject(
        HttpServletRequest request,
        I_CmsAuthorizationHandler.I_PrivilegedLoginAction loginAction) {

        return initCmsObject(request);
    }

    /**
     * @see I_CmsAuthorizationHandler#initCmsObject(HttpServletRequest, String, String)
     */
    public CmsObject initCmsObject(HttpServletRequest request, String userName, String pwd) throws CmsException {

        // first, try to validate the session
        CmsObject cms = initCmsObjectFromSession(request);
        if (cms != null) {
            return cms;
        }
        // try to login with the given credentials
        cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        // this will throw an exception if login fails
        cms.loginUser(userName, pwd);
        // register the session into OpenCms and
        // return successful logged in user
        return registerSession(request, cms);
    }

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
    public void requestAuthorization(HttpServletRequest req, HttpServletResponse res, String loginFormURL)
    throws IOException {

        CmsHttpAuthenticationSettings httpAuthenticationSettings = OpenCms.getSystemInfo().getHttpAuthenticationSettings();

        if (loginFormURL == null) {
            if (httpAuthenticationSettings.useBrowserBasedHttpAuthentication()) {
                // HTTP basic authentication is used
                res.setHeader(
                    CmsRequestUtil.HEADER_WWW_AUTHENTICATE,
                    "BASIC realm=\"" + OpenCms.getSystemInfo().getOpenCmsContext() + "\"");
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;

            } else if (httpAuthenticationSettings.getFormBasedHttpAuthenticationUri() != null) {
                loginFormURL = httpAuthenticationSettings.getFormBasedHttpAuthenticationUri();
            } else {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_UNSUPPORTED_AUTHENTICATION_MECHANISM_1,
                        httpAuthenticationSettings.getBrowserBasedAuthenticationMechanism()));
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_AUTHENTICATE_PROPERTY_2,
                    loginFormURL,
                    req.getRequestURI()));
        }
        // finally redirect to the login form
        res.sendRedirect(loginFormURL);
    }

    /**
     * Checks if the current request contains HTTP basic authentication information in
     * the headers, if so the user is tried to log in with this data, and on success a
     * session is generated.<p>
     *
     * @param req the current HTTP request
     *
     * @return the authenticated cms object, or <code>null</code> if failed
     */
    protected CmsObject checkBasicAuthorization(HttpServletRequest req) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking for basic authorization.");
        }
        try {
            CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
            // no user identified from the session and basic authentication is enabled
            String auth = req.getHeader(HEADER_AUTHORIZATION);
            if ((auth == null) || !auth.toUpperCase().startsWith(AUTHORIZATION_BASIC_PREFIX)) {
                // no authorization data is available
                return cms;
            }
            // get encoded user and password, following after "BASIC "
            String base64Token = auth.substring(6);

            // decode it, using base 64 decoder
            String token = new String(Base64.decodeBase64(base64Token.getBytes()));
            String username = null;
            String password = null;
            int pos = token.indexOf(SEPARATOR_CREDENTIALS);
            if (pos != -1) {
                username = token.substring(0, pos);
                password = token.substring(pos + 1);
            }
            // authentication in the DB
            cms.loginUser(username, password);

            // authorization was successful create a session
            req.getSession(true);
            return cms;
        } catch (CmsException e) {
            // authorization failed
            return null;
        }
    }
}
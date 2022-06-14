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

import org.opencms.crypto.CmsEncryptionException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.A_CmsAuthorizationHandler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsHttpAuthenticationSettings;
import org.opencms.main.OpenCms;
import org.opencms.ui.login.CmsLoginHelper;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Joiner;

/**
 * Defines default authorization methods.<p>
 *
 * @since 6.5.4
 */
public class CmsDefaultAuthorizationHandler extends A_CmsAuthorizationHandler {

    /** Configuration parameter to control for which paths startup settings should be applied after HTTP Basic authentication. */
    public static final String PARAM_HTTP_BASICAUTH_USESTARTSETTINGS_PATHS = "http.basicauth.usestartsettings.paths";

    /** Configuration parameter to control for which users startup settings should be applied after HTTP Basic authentication. */
    public static final String PARAM_HTTP_BASICAUTH_USESTARTSETTINGS_USERS = "http.basicauth.usestartsettings.users";

    /** Basic authorization prefix constant. */
    public static final String AUTHORIZATION_BASIC_PREFIX = "BASIC ";
    /** Authorization header constant. */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /** Parameter for passing the encrypted version of the requested resource. */
    public static final String PARAM_ENCRYPTED_REQUESTED_RESOURCE = "encryptedRequestedResource";

    /** Credentials separator constant. */
    public static final String SEPARATOR_CREDENTIALS = ":";

    /**
     * Checks if a request URI path matches a given set of prefix paths.
     *
     * @param uri the request URI path
     * @param pathSpec a comma separated list of path prefixes, which may contain %(contextPath) macros
     * @return true if the URI path matches the path spec
     */
    protected static boolean checkPath(String uri, String pathSpec) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(pathSpec)) {
            return false;
        }
        CmsMacroResolver resolver = new CmsMacroResolver();
        pathSpec = resolver.resolveMacros(pathSpec);
        String[] pathPatterns = pathSpec.split(",");
        for (String pathToken : pathPatterns) {
            if (CmsStringUtil.isPrefixPath(pathToken, uri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the authenticated user matches a user specification string.
     *
     * <p>The user specification string is a comma-separed list of entries of the form TYPE.Name, where
     * TYPE is either ROLE, GROUP, or USER. The method returns true if the user matches any of the groups, roles, or user names from this list.
     *
     * <p>It's also possible to configure an entry "*", which always matches.
     *
     * @param cms the CMS context
     * @param userSpec the user specification
     * @return true if the user matches any entry from the user specification
     */
    protected static boolean checkUser(CmsObject cms, String userSpec) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(userSpec)) {
            return false;
        }

        Set<String> groupsOfUser = null; // lazily initialized
        String[] entries = userSpec.split(",");
        for (String userSpecEntry : entries) {
            userSpecEntry = userSpecEntry.trim();
            if ("*".equals(userSpecEntry)) {
                return true;
            } else if (userSpecEntry.startsWith(I_CmsPrincipal.PRINCIPAL_USER)) {
                String userName = CmsUser.removePrefix(userSpecEntry);
                if (cms.getRequestContext().getCurrentUser().getName().equals(userName)) {
                    return true;
                }
            } else if (userSpecEntry.startsWith(CmsRole.PRINCIPAL_ROLE)) {
                String actualRole = CmsRole.removePrefix(userSpecEntry);
                CmsRole roleObj = null;
                if (actualRole.contains("/")) {
                    roleObj = CmsRole.valueOfRoleName(actualRole);
                } else {
                    roleObj = CmsRole.valueOfRoleName(actualRole).forOrgUnit(null);
                }
                if (OpenCms.getRoleManager().hasRole(cms, roleObj)) {
                    return true;
                }
            } else if (userSpecEntry.startsWith(I_CmsPrincipal.PRINCIPAL_GROUP)) {
                String groupName = CmsGroup.removePrefix(userSpecEntry);

                if (groupsOfUser == null) {
                    try {
                        groupsOfUser = cms.getGroupsOfUser(
                            cms.getRequestContext().getCurrentUser().getName(),
                            false).stream().map(group -> group.getName()).collect(Collectors.toSet());
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        continue;
                    }
                }
                if (groupsOfUser.contains(groupName)) {
                    return true;
                }
            }
        }
        return false;
    }

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
            List<String> paramList;
            if (params != null) {
                paramList = new ArrayList<>(Arrays.asList(params.split("&")));
            } else {
                paramList = new ArrayList<>();
            }
            if (callbackURL != null) {
                try {
                    paramList.add(
                        PARAM_ENCRYPTED_REQUESTED_RESOURCE
                            + "="
                            + OpenCms.getDefaultTextEncryption().encrypt(CmsEncoder.decode(callbackURL)));
                } catch (CmsEncryptionException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            fullURL.append((callbackURL != null) ? "&" : "?");
            fullURL.append(Joiner.on("&").join(paramList));
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
                    "BASIC realm=\"" + OpenCms.getSystemInfo().getServerName() + "\"");
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
            if (OpenCms.getSystemInfo().getHttpAuthenticationSettings().getBrowserBasedAuthenticationMechanism() == null) {
                // browser base authorization is not enabled, return Guest user CmsObject
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Browser based authorization not enabled.");
                }
                return cms;
            }
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
            HttpSession session = req.getSession(true);
            String requestUri = req.getRequestURI();
            boolean isWorkplace = requestUri.startsWith(OpenCms.getSystemInfo().getWorkplaceContext())
                || requestUri.startsWith(
                    CmsStringUtil.joinPaths(OpenCms.getSystemInfo().getOpenCmsContext(), "/system/workplace"));
            isWorkplace = isWorkplace && OpenCms.getRoleManager().hasRole(cms, CmsRole.ELEMENT_AUTHOR);
            LOG.debug("isWorkplace = " + isWorkplace);
            boolean initStartSettings = isWorkplace || shouldUseStartSettingsForHttpBasicAuth(cms, req);
            LOG.debug("initStartSettings = " + initStartSettings);
            OpenCms.getSiteManager().isWorkplaceRequest(req);
            if (initStartSettings) {
                CmsWorkplaceSettings settings = CmsLoginHelper.initSiteAndProject(cms);
                session.setAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS, settings);
            }

            return cms;
        } catch (CmsException e) {
            // authorization failed
            return null;
        }
    }

    /**
     * Checks whether start settings should be used after HTTP Basic authentication.
     *
     * <p>This method will not be called for workplace requests; for these the start settings will always be used.
     *
     * @param cms the CMS context initialized with the user from the HTTP Basic authentication
     * @param req the current request
     *
     * @return true if the start settings should be used
     */
    protected boolean shouldUseStartSettingsForHttpBasicAuth(CmsObject cms, HttpServletRequest req) {

        String userSpec = m_parameters.get(PARAM_HTTP_BASICAUTH_USESTARTSETTINGS_USERS);
        String pathSpec = m_parameters.get(PARAM_HTTP_BASICAUTH_USESTARTSETTINGS_PATHS);

        if (!checkPath(req.getRequestURI(), pathSpec)) {
            LOG.debug("checkPath returned false for " + req.getRequestURI() + ", pathSpec=" + pathSpec);
            return false;
        }
        if (!checkUser(cms, userSpec)) {
            LOG.debug(
                "checkUser returned false for "
                    + cms.getRequestContext().getCurrentUser().getName()
                    + ", userSpec = "
                    + userSpec);
            return false;
        }

        return true;
    }
}
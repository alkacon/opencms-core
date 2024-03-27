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

package org.opencms.ui.login;

import org.opencms.crypto.CmsEncryptionException;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsAcceptLanguageHeaderParser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsDefaultAuthorizationHandler;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.ui.apps.CmsAppHierarchyConfiguration;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.ui.apps.CmsPageEditorConfiguration;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceLoginHandler;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.Messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Utility to login users to the OpenCms workplace.<p>
 */
public class CmsLoginHelper extends CmsJspLoginBean {

    /**
     * Holds the current login parameters.<p>
     */
    public static class LoginParameters implements Serializable {

        /** The serial version id. */
        private static final long serialVersionUID = -2636377967076796207L;

        /** The authorization token. */
        private String m_authToken;

        /** The locale to use for display, this will not be the workplace locale, but the browser locale. */
        private Locale m_locale;

        /** The logout flag. */
        private boolean m_logout;

        /** The value of the organizational unit parameter. */
        private String m_oufqn;

        /** The value of the PC type parameter. */
        private String m_pcType;

        /** The redirect URL after a successful login. */
        private String m_requestedResource;

        /** The value of the user name parameter. */
        private String m_username;

        /** Reset password flag. */
        private boolean m_reset;

        /**
         * Constructor.<p>
         *
         * @param username the user name
         * @param pcType the pc type
         * @param oufqn the ou fqn
         * @param requestedResource the requested resource
         * @param locale the locale
         * @param authToken the authorization token
         * @param logout the logout flag
         * @param reset flag to indicate whether we are in 'reset password' mode
         */
        public LoginParameters(
            String username,
            String pcType,
            String oufqn,
            String requestedResource,
            Locale locale,
            String authToken,
            boolean logout,
            boolean reset) {

            m_username = username;
            m_pcType = pcType;
            m_oufqn = oufqn;
            m_requestedResource = requestedResource;
            m_locale = locale;
            m_authToken = authToken;
            m_logout = logout;
            m_reset = reset;
        }

        /**
         * Gets the authorization token.<p>
         *
         * @return the authorization token
         */
        public String getAuthToken() {

            return m_authToken;
        }

        /**
         * Returns the locale.<p>
         *
         * @return the locale
         */
        public Locale getLocale() {

            return m_locale;
        }

        /**
         * Returns the ou fqn.<p>
         *
         * @return the ou fqn
         */
        public String getOufqn() {

            return m_oufqn;
        }

        /**
         * Returns the pc type.<p>
         *
         * @return the pc type
         */
        public String getPcType() {

            return m_pcType;
        }

        /**
         * Returns the requested resource.<p>
         *
         * @return the requested resource
         */
        public String getRequestedResource() {

            return m_requestedResource;
        }

        /**
         * Returns the user name.<p>
         *
         * @return the user name
         */
        public String getUsername() {

            return m_username;
        }

        /**
         * Returns if a logout is requested.<p>
         *
         * @return the logout flag
         */
        public boolean isLogout() {

            return m_logout;
        }

        /**
         * Returns whether the pc type is private.<p>
         *
         * @return <code>true</code> if the pc type is private
         */
        public boolean isPrivatePc() {

            return (m_pcType == null) || m_pcType.equals(PCTYPE_PRIVATE);
        }

        /**
         * Returns true if we are in 'reset password' mode.<p>
         *
         * @return true in reset mode, false otherwise
         */
        public boolean isReset() {

            return m_reset;
        }
    }

    /** Action constant: Default action, display the dialog. */
    public static final int ACTION_DISPLAY = 0;

    /** Action constant: Login successful. */
    public static final int ACTION_LOGIN = 1;

    /** Action constant: Logout. */
    public static final int ACTION_LOGOUT = 2;

    /** The parameter name for the "getoulist" action. */
    public static final String PARAM_ACTION_GETOULIST = "getoulist";

    /** The parameter name for the "login" action. */
    public static final String PARAM_ACTION_LOGIN = "login";

    /** The parameter name for the "logout" action. */
    public static final String PARAM_ACTION_LOGOUT = "logout";

    /** Parameter name for the authorization token. */
    public static final String PARAM_AUTHTOKEN = "at";

    /** The html id for the login form. */
    public static final String PARAM_FORM = "ocLoginForm";

    /** The parameter name for the organizational unit. */
    public static final String PARAM_OUFQN = "ocOuFqn";

    /** The parameter name for the search organizational unit. */
    public static final String PARAM_OUSEARCH = "ocOuSearch";

    /** The parameter name for the password. */
    public static final String PARAM_PASSWORD = "ocPword";

    /** The parameter name for the PC type. */
    public static final String PARAM_PCTYPE = "ocPcType";

    /** The parameter name for the organizational unit. */
    public static final String PARAM_PREDEF_OUFQN = "ocPredefOuFqn";

    /** The parameter name for the user name. */
    public static final String PARAM_USERNAME = "ocUname";

    /** Parameter used to open the 'send reset mail' view instead of the login dialog. */
    public static final String PARAM_RESET_PASSWORD = "reset";

    /** The parameter name for the workplace data. */
    public static final String PARAM_WPDATA = "ocWpData";

    /** PC type constant: private PC. */
    public static final String PCTYPE_PRIVATE = "private";

    /** PC type constant: public PC. */
    public static final String PCTYPE_PUBLIC = "public";

    /** The oufqn cookie name. */
    private static final String COOKIE_OUFQN = "OpenCmsOuFqn";

    /** The PC type cookie name. */
    private static final String COOKIE_PCTYPE = "OpenCmsPcType";

    /** The username cookie name. */
    private static final String COOKIE_USERNAME = "OpenCmsUserName";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLoginHelper.class);

    /**
     * Gets the copyright information HTML.<p>
     *
     * @param locale the locale for which to get the copyright info
     *
     * @return the copyright info HTML
     */
    public static String getCopyrightHtml(Locale locale) {

        StringBuffer html = new StringBuffer();
        html.append("<div style=\"text-align: center; font-size: 10px; white-space: nowrap;\">");
        html.append("<a href=\"http://www.opencms.org\" target=\"_blank\">OpenCms</a> ");
        html.append(Messages.get().getBundle(locale).key(Messages.GUI_LOGIN_OPENCMS_IS_FREE_SOFTWARE_0));
        html.append("</div>\n");
        html.append("<div style=\"text-align: center; font-size: 10px; white-space: nowrap;\">");
        html.append(Messages.get().getBundle(locale).key(Messages.GUI_LOGIN_TRADEMARKS_0));
        html.append("</div>\n");
        html.append("<div style=\"text-align: center; font-size: 10px; white-space: nowrap;\">");
        html.append("&copy; 2002 - 2024 Alkacon Software GmbH &amp; Co. KG. ");
        html.append(Messages.get().getBundle(locale).key(Messages.GUI_LOGIN_RIGHTS_RESERVED_0));
        html.append("</div>\n");
        return html.toString();
    }

    /**
     * Returns the direct edit path from the user settings, or <code>null</code> if not set.<p>
     *
     * @param cms the CMS context to use
     * @param userSettings the user settings
     * @param forceDirectEdit <code>true</code> to ignore the start view
     *
     * @return the direct edit path
     */
    public static String getDirectEditPath(CmsObject cms, CmsUserSettings userSettings, boolean forceDirectEdit) {

        if (forceDirectEdit
            || (userSettings.getStartView().equals(CmsWorkplace.VIEW_DIRECT_EDIT)
                | userSettings.getStartView().equals(CmsPageEditorConfiguration.APP_ID))) {

            try {
                CmsObject cloneCms = OpenCms.initCmsObject(cms);
                String startSite = CmsWorkplace.getStartSiteRoot(cloneCms, userSettings);
                cloneCms.getRequestContext().setSiteRoot(startSite);
                String projectName = userSettings.getStartProject();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(projectName)) {
                    cloneCms.getRequestContext().setCurrentProject(cloneCms.readProject(projectName));
                }
                String folder = userSettings.getStartFolder();
                if (!cloneCms.existsResource(folder)) {
                    folder = "/";
                }
                CmsResource targetRes = cloneCms.readDefaultFile(folder, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED);
                if (targetRes != null) {
                    return cloneCms.getSitePath(targetRes);
                }
            } catch (Exception e) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return null;
    }

    /**
     * Returns the login parameters for the current request.<p>
     *
     * @param cms the cms context
     * @param request the request
     * @param workplaceUiRequest true if this is called from a workplace UI request
     *
     * @return the login parameters
     */
    public static LoginParameters getLoginParameters(
        CmsObject cms,
        HttpServletRequest request,
        boolean workplaceUiRequest) {

        String authToken = request.getParameter(PARAM_AUTHTOKEN);

        String actionLogout = CmsRequestUtil.getNotEmptyParameter(request, PARAM_ACTION_LOGOUT);
        boolean logout = Boolean.valueOf(actionLogout).booleanValue();
        String oufqn = request.getParameter(PARAM_OUFQN);
        if (oufqn == null) {
            oufqn = getPreDefOuFqn(cms, request, logout);
        }
        String pcType = getPcType(request);

        String username = CmsRequestUtil.getNotEmptyParameter(request, PARAM_USERNAME);
        if (username != null) {
            // remove white spaces, can only lead to confusion on user name
            username = username.trim();
        }
        // get cookies only on private PC types (or if security option is disabled)
        if ((pcType == null) || PCTYPE_PRIVATE.equals(pcType)) {
            // get the user name cookie
            Cookie userNameCookie = getCookie(request, COOKIE_USERNAME);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userNameCookie.getValue())
                && !"null".equals(userNameCookie.getValue())) {
                // only set the data if needed
                if (username == null) {
                    username = userNameCookie.getValue();
                }
                if (pcType == null) {
                    // set PC type to private PC if the user cookie is found
                    pcType = PCTYPE_PRIVATE;
                }
            }
            if (oufqn == null) {
                // get the organizational unit cookie
                Cookie ouFqnCookie = getCookie(request, COOKIE_OUFQN);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(ouFqnCookie.getValue())
                    && !"null".equals(ouFqnCookie.getValue())) {
                    oufqn = ouFqnCookie.getValue();
                }
            }
        }
        String requestedResource = CmsRequestUtil.getNotEmptyParameter(
            request,
            CmsWorkplaceManager.PARAM_LOGIN_REQUESTED_RESOURCE);
        boolean validRequestedResource = false;
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(requestedResource)) {
            String encryptedRequestedResource = request.getParameter(
                CmsDefaultAuthorizationHandler.PARAM_ENCRYPTED_REQUESTED_RESOURCE);
            try {
                String decryptedResource = OpenCms.getDefaultTextEncryption().decrypt(encryptedRequestedResource);
                if (requestedResource.equals(decryptedResource)) {
                    validRequestedResource = true;
                }
            } catch (CmsEncryptionException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
            if (!validRequestedResource) {
                requestedResource = null;
            }
        }
        Locale locale = getLocaleForRequest(request);
        String resetStr = request.getParameter(PARAM_RESET_PASSWORD);
        boolean reset = (resetStr != null);
        return new LoginParameters(username, pcType, oufqn, requestedResource, locale, authToken, logout, reset);
    }

    /**
     * Gets the list of OUs which should be selectable in the login dialog.<p>
     *
     * @param cms the CMS context to use
     * @param predefOu the predefined OU
     *
     * @return the list of organizational units for the OU selector
     */
    public static List<CmsOrganizationalUnit> getOrgUnitsForLoginDialog(CmsObject cms, String predefOu) {

        List<CmsOrganizationalUnit> result = new ArrayList<CmsOrganizationalUnit>();
        try {
            if (predefOu == null) {
                result.add(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ""));
                result.addAll(OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, "", true));
                Iterator<CmsOrganizationalUnit> itOus = result.iterator();
                while (itOus.hasNext()) {
                    CmsOrganizationalUnit ou = itOus.next();
                    if (ou.hasFlagHideLogin() || ou.hasFlagWebuser()) {
                        itOus.remove();
                    }
                }
            } else {
                result.add(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, predefOu));
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return result;

    }

    /**
     * Returns the start view.<p>
     *
     * @param cms the cms context
     *
     * @return the start view
     */
    public static String getStartView(CmsObject cms) {

        CmsUserSettings settings = new CmsUserSettings(cms);
        String targetView = getDirectEditPath(cms, settings, false);
        if (targetView == null) {
            if (settings.getStartView().startsWith("/")) {
                if (CmsWorkplace.VIEW_WORKPLACE.equals(settings.getStartView())) {
                    targetView = "#" + CmsFileExplorerConfiguration.APP_ID;
                } else if (CmsWorkplace.VIEW_ADMIN.equals(settings.getStartView())) {
                    targetView = "#" + CmsAppHierarchyConfiguration.APP_ID;
                }
            } else {
                targetView = "#" + settings.getStartView();
            }
        }
        return targetView;
    }

    /**
     * Gets the window title for a given locale.<p>
     *
     * @param locale the locale
     * @return the window title
     */
    public static String getTitle(Locale locale) {

        return Messages.get().getBundle(locale).key(Messages.GUI_LOGIN_TITLE_0);
    }

    /**
     * Initializes the site and project for a CMS context after login, and returns the workplace settings for the corresponding user.<p>
     *
     * @param cms the CMS context which should be initialized
     * @return the workplace set
     */
    public static CmsWorkplaceSettings initSiteAndProject(CmsObject cms) {

        CmsWorkplaceSettings workplaceSettings = CmsWorkplace.initWorkplaceSettings(cms, null, false);
        String startSite = CmsWorkplace.getStartSiteRoot(cms, workplaceSettings);
        // switch to the preferred site
        workplaceSettings.setSite(startSite);
        cms.getRequestContext().setSiteRoot(startSite);
        // store the workplace settings
        CmsUserSettings settings = workplaceSettings.getUserSettings();
        // get the direct edit path

        try {
            CmsProject project = cms.readProject(settings.getStartProject());
            if (OpenCms.getOrgUnitManager().getAllAccessibleProjects(cms, project.getOuFqn(), false).contains(
                project)) {
                // user has access to the project, set this as current project
                workplaceSettings.setProject(project.getUuid());
                cms.getRequestContext().setCurrentProject(project);
            }
        } catch (CmsException e) {
            // unable to set the startup project, bad but not critical
            LOG.warn(
                Messages.get().getBundle().key(
                    Messages.LOG_LOGIN_NO_STARTUP_PROJECT_2,
                    cms.getRequestContext().getCurrentUser().getName(),
                    settings.getStartProject()),
                e);
        }
        return workplaceSettings;
    }

    /**
     * Sets the cookie data.<p>
     *
     * @param pcType the pctype value
     * @param username the username value
     * @param oufqn the oufqn value
     *
     * @param request the current request
     * @param response the current response
     */
    public static void setCookieData(
        String pcType,
        String username,
        String oufqn,
        HttpServletRequest request,
        HttpServletResponse response) {

        if (CmsStringUtil.isEmpty(oufqn)) {
            oufqn = "/";
        }
        // set the PC type cookie only if security dialog is enabled
        if (OpenCms.getLoginManager().isEnableSecurity() && CmsStringUtil.isNotEmpty(pcType)) {
            Cookie pcTypeCookie = getCookie(request, COOKIE_PCTYPE);
            pcTypeCookie.setValue(pcType);
            setCookie(pcTypeCookie, false, request, response);
        }

        // only store user name and OU cookies on private PC types
        if (PCTYPE_PRIVATE.equals(pcType)) {
            // set the user name cookie
            Cookie userNameCookie = getCookie(request, COOKIE_USERNAME);
            userNameCookie.setValue(username);
            setCookie(userNameCookie, false, request, response);

            // set the organizational unit cookie
            Cookie ouFqnCookie = getCookie(request, COOKIE_OUFQN);
            ouFqnCookie.setValue(oufqn);
            setCookie(ouFqnCookie, false, request, response);
        } else if (OpenCms.getLoginManager().isEnableSecurity() && PCTYPE_PUBLIC.equals(pcType)) {
            // delete user name and organizational unit cookies
            Cookie userNameCookie = getCookie(request, COOKIE_USERNAME);
            setCookie(userNameCookie, true, request, response);
            Cookie ouFqnCookie = getCookie(request, COOKIE_OUFQN);
            setCookie(ouFqnCookie, true, request, response);

        }
    }

    /**
     * Checks that the user name and password are not empty, and returns an error message if they are.<p>
     *
     * @param username the user name
     * @param password the password
     *
     * @return the error message, or null if the user name and password are OK
     */
    public static CmsMessageContainer validateUserAndPasswordNotEmpty(String username, String password) {

        boolean userEmpty = CmsStringUtil.isEmpty(username);
        boolean passwordEmpty = CmsStringUtil.isEmpty(password);

        // login was requested
        if (userEmpty && passwordEmpty) {
            return Messages.get().container(Messages.GUI_LOGIN_NO_DATA_0);
        } else if (userEmpty) {
            return Messages.get().container(Messages.GUI_LOGIN_NO_NAME_0);
        } else if (passwordEmpty) {
            return Messages.get().container(Messages.GUI_LOGIN_NO_PASSWORD_0);
        }
        return null;
    }

    /**
     * Returns the cookie with the given name, if not cookie is found a new one is created.<p>
     *
     * @param request the current request
     * @param name the name of the cookie
     *
     * @return the cookie
     */
    protected static Cookie getCookie(HttpServletRequest request, String name) {

        Cookie[] cookies = request.getCookies();
        for (int i = 0; (cookies != null) && (i < cookies.length); i++) {
            if (name.equalsIgnoreCase(cookies[i].getName())) {
                return cookies[i];
            }
        }
        return new Cookie(name, "");
    }

    /**
     * Sets the cookie in the response.<p>
     *
     * @param cookie the cookie to set
     * @param delete flag to determine if the cookir should be deleted
     * @param request the current request
     * @param response the current response
     */
    protected static void setCookie(
        Cookie cookie,
        boolean delete,
        HttpServletRequest request,
        HttpServletResponse response) {

        if (request.getAttribute(PARAM_PREDEF_OUFQN) != null) {
            // prevent the use of cookies if using a direct ou login url
            return;
        }
        int maxAge = 0;
        if (!delete) {
            // set the expiration date of the cookie to six months from today
            GregorianCalendar cal = new GregorianCalendar();
            cal.add(Calendar.MONTH, 6);
            maxAge = (int)((cal.getTimeInMillis() - System.currentTimeMillis()) / 1000);
        }
        cookie.setMaxAge(maxAge);
        // set the path
        cookie.setPath(
            CmsStringUtil.joinPaths(
                OpenCms.getStaticExportManager().getVfsPrefix(),
                CmsWorkplaceLoginHandler.LOGIN_HANDLER));
        // set the cookie
        response.addCookie(cookie);
    }

    /**
     * Returns the locale for the given request.<p>
     *
     * @param req the request
     *
     * @return the locale
     */
    private static Locale getLocaleForRequest(HttpServletRequest req) {

        CmsAcceptLanguageHeaderParser parser = new CmsAcceptLanguageHeaderParser(
            req,
            OpenCms.getWorkplaceManager().getDefaultLocale());
        List<Locale> acceptedLocales = parser.getAcceptedLocales();
        List<Locale> workplaceLocales = OpenCms.getWorkplaceManager().getLocales();
        Locale locale = OpenCms.getLocaleManager().getFirstMatchingLocale(acceptedLocales, workplaceLocales);
        if (locale == null) {
            // no match found - use OpenCms default locale
            locale = OpenCms.getWorkplaceManager().getDefaultLocale();
        }
        return locale;
    }

    /**
     * Returns the pc type of the current request.<p>
     *
     * @param request the request
     *
     * @return the pc type
     */
    private static String getPcType(HttpServletRequest request) {

        String pcType = null;
        if (!OpenCms.getLoginManager().isEnableSecurity()) {
            // if security option is disabled, just set PC type to "private" to get common login dialog
            pcType = PCTYPE_PRIVATE;
        } else {
            // security option is enabled, try to get PC type from request parameter
            pcType = CmsRequestUtil.getNotEmptyParameter(request, PARAM_PCTYPE);
            if (pcType == null) {
                Cookie pcTypeCookie = getCookie(request, COOKIE_PCTYPE);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(pcTypeCookie.getValue())
                    && !"null".equals(pcTypeCookie.getValue())) {
                    pcType = pcTypeCookie.getValue();
                }
            }

        }
        return pcType;
    }

    /**
     * Returns the pre defined ou fqn.<p>
     *
     * @param cms the cms context
     * @param request the request
     * @param logout in case of a logout
     *
     * @return the ou fqn
     */
    private static String getPreDefOuFqn(CmsObject cms, HttpServletRequest request, boolean logout) {

        if (logout && (request.getAttribute(PARAM_PREDEF_OUFQN) == null)) {
            String oufqn = cms.getRequestContext().getOuFqn();
            if (!oufqn.startsWith(CmsOrganizationalUnit.SEPARATOR)) {
                oufqn = CmsOrganizationalUnit.SEPARATOR + oufqn;
            }
            request.setAttribute(CmsLoginHelper.PARAM_PREDEF_OUFQN, oufqn);
        }
        return (String)request.getAttribute(PARAM_PREDEF_OUFQN);
    }
}

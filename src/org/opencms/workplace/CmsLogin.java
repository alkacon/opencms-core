/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace;

import org.opencms.db.CmsLoginMessage;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsAcceptLanguageHeaderParser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUriSplitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Handles the login of Users to the OpenCms workplace.<p> 
 * 
 * @since 6.0.0 
 */
public class CmsLogin extends CmsJspLoginBean {

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

    /** The workplace data cookie name, value stores following information: ${left},${top},${width},${height}. */
    private static final String COOKIE_WP_DATA = "OpenCmsWpData";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLogin.class);

    /** The action to perform. */
    private int m_action;

    /** The value of the "login" action parameter. */
    private String m_actionLogin;

    /** The value of the "logout" action parameter. */
    private String m_actionLogout;

    /** The path to open if direct edit is selected as start view. */
    private String m_directEditPath;

    /** The locale to use for display, this will not be the workplace locale, but the browser locale. */
    private Locale m_locale;

    /** The message to display with the dialog in a JavaScrip alert. */
    private CmsMessageContainer m_message;

    /** The selected organizational unit. */
    private CmsOrganizationalUnit m_ou;

    /** The value of the organizational unit parameter. */
    private String m_oufqn;

    /** The list of all organizational units. */
    private List<CmsOrganizationalUnit> m_ous;

    /** The value of the password parameter. */
    private String m_password;

    /** The value of the PC type parameter. */
    private String m_pcType;

    /** The redirect URL after a successful login. */
    private String m_requestedResource;

    /** The value of the user name parameter. */
    private String m_username;

    /**
     * Public constructor for login page.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsLogin(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);

        // this page must never be cached
        res.setDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED, System.currentTimeMillis());
        CmsRequestUtil.setNoCacheHeaders(res);

        // divine the best locale from the users browser settings
        CmsAcceptLanguageHeaderParser parser = new CmsAcceptLanguageHeaderParser(
            req,
            OpenCms.getWorkplaceManager().getDefaultLocale());
        List<Locale> acceptedLocales = parser.getAcceptedLocales();
        List<Locale> workplaceLocales = OpenCms.getWorkplaceManager().getLocales();
        m_locale = OpenCms.getLocaleManager().getFirstMatchingLocale(acceptedLocales, workplaceLocales);
        if (m_locale == null) {
            // no match found - use OpenCms default locale
            m_locale = OpenCms.getWorkplaceManager().getDefaultLocale();
        }
    }

    /**
     * Returns the HTML code for selecting an organizational unit.<p>
     * 
     * @return the HTML code for selecting an organizational unit
     */
    public String buildOrgUnitSelector() {

        StringBuffer html = new StringBuffer();
        html.append("<select style='width: 100%;' size='1' ");
        appendId(html, PARAM_OUFQN);
        html.append(">\n");
        for (CmsOrganizationalUnit ou : getOus()) {
            String selected = "";
            if (ou.getName().equals(m_oufqn)
                || (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_oufqn) && ou.getName().equals(m_oufqn.substring(1)))) {
                selected = " selected='selected'";
            }
            html.append("<option value='").append(ou.getName()).append("'").append(selected).append(">");
            html.append(ou.getDisplayName(m_locale));
            html.append("</option>\n");
        }
        html.append("</select>\n");
        return html.toString();
    }

    /**
     * Returns the HTML for the login dialog in it's current state.<p>
     * 
     * @return the HTML for the login dialog
     * 
     * @throws IOException in case a redirect fails
     */
    public String displayDialog() throws IOException {

        if ((OpenCms.getSiteManager().getSites().size() > 1)
            && !OpenCms.getSiteManager().isWorkplaceRequest(getRequest())) {

            // this is a multi site-configuration, but not a request to the configured Workplace site
            StringBuffer loginLink = new StringBuffer(256);
            loginLink.append(OpenCms.getSiteManager().getWorkplaceSiteMatcher().toString());
            loginLink.append(getFormLink());
            // send a redirect to the workplace site
            getResponse().sendRedirect(loginLink.toString());
            return null;
        }

        CmsObject cms = getCmsObject();

        m_message = null;
        if (cms.getRequestContext().getCurrentUser().isGuestUser()) {
            // user is not currently logged in
            m_action = ACTION_DISPLAY;
            m_username = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_USERNAME);
            if (m_username != null) {
                // remove white spaces, can only lead to confusion on user name
                m_username = m_username.trim();
            }
            m_password = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_PASSWORD);
            m_actionLogin = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_ACTION_LOGIN);
            m_oufqn = getRequest().getParameter(PARAM_OUFQN);
            if (m_oufqn == null) {
                m_oufqn = getPreDefOuFqn();
            }
            if (OpenCms.getLoginManager().isEnableSecurity()) {
                // security option is enabled, try to get PC type from request parameter
                m_pcType = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_PCTYPE);
            } else {
                // if security option is disabled, just set PC type to "private" to get common login dialog
                m_pcType = PCTYPE_PRIVATE;
            }
            // try to get some info from a cookie
            getCookieData();

            // set PC type to "public" as default if not already set by cookie, request or if security option is disabled
            if (m_pcType == null) {
                m_pcType = PCTYPE_PUBLIC;
            }
        } else {
            // user is already logged in
            m_oufqn = cms.getRequestContext().getOuFqn();
            m_action = ACTION_LOGIN;
            m_actionLogout = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_ACTION_LOGOUT);
        }

        if (m_oufqn == null) {
            m_oufqn = CmsOrganizationalUnit.SEPARATOR;
        }

        String actionGetOus = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_ACTION_GETOULIST);
        if (Boolean.TRUE.toString().equals(actionGetOus)) {
            return getJsonOrgUnitList();
        }

        // initialize the right ou
        m_ou = null;
        try {
            m_ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCmsObject(), m_oufqn);
        } catch (CmsException e) {
            m_oufqn = CmsOrganizationalUnit.SEPARATOR;
            try {
                m_ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCmsObject(), m_oufqn);
            } catch (CmsException exc) {
                LOG.error(exc.getLocalizedMessage(), exc);
            }
        }

        // initialize the requested resource
        m_requestedResource = CmsRequestUtil.getNotEmptyParameter(
            getRequest(),
            CmsWorkplaceManager.PARAM_LOGIN_REQUESTED_RESOURCE);
        if (m_requestedResource == null) {
            // no resource was requested, use default workplace URI
            m_requestedResource = CmsFrameset.JSP_WORKPLACE_URI;
        }

        if (Boolean.valueOf(m_actionLogin).booleanValue()) {
            // login was requested
            if ((m_username == null) && (m_password == null)) {
                m_message = Messages.get().container(Messages.GUI_LOGIN_NO_DATA_0);
            } else if (m_username == null) {
                m_message = Messages.get().container(Messages.GUI_LOGIN_NO_NAME_0);
            } else if (m_password == null) {
                m_message = Messages.get().container(Messages.GUI_LOGIN_NO_PASSWORD_0);
            } else if ((m_username != null) && (m_password != null)) {

                // try to login with the given user information
                login((m_oufqn == null ? CmsOrganizationalUnit.SEPARATOR : m_oufqn) + m_username, m_password);

                if (getLoginException() == null) {
                    // the login was successful
                    m_action = ACTION_LOGIN;

                    // set the default project of the user
                    CmsUserSettings settings = new CmsUserSettings(cms);

                    // get the direct edit path
                    m_directEditPath = getDirectEditPath(settings);

                    try {
                        CmsProject project = cms.readProject(settings.getStartProject());
                        if (OpenCms.getOrgUnitManager().getAllAccessibleProjects(cms, project.getOuFqn(), false).contains(
                            project)) {
                            // user has access to the project, set this as current project
                            cms.getRequestContext().setCurrentProject(project);
                        }
                    } catch (CmsException e) {
                        // unable to set the startup project, bad but not critical
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_LOGIN_NO_STARTUP_PROJECT_2,
                                m_username,
                                settings.getStartProject()),
                            e);
                    }
                } else {
                    // there was an error during login

                    if (org.opencms.security.Messages.ERR_LOGIN_FAILED_DISABLED_2 == getLoginException().getMessageContainer().getKey()) {
                        // the user account is disabled
                        m_message = Messages.get().container(Messages.GUI_LOGIN_FAILED_DISABLED_0);
                    } else if (org.opencms.security.Messages.ERR_LOGIN_FAILED_TEMP_DISABLED_4 == getLoginException().getMessageContainer().getKey()) {
                        // the user account is temporarily disabled because of too many login failures
                        m_message = Messages.get().container(Messages.GUI_LOGIN_FAILED_TEMP_DISABLED_0);
                    } else if (org.opencms.security.Messages.ERR_LOGIN_FAILED_WITH_MESSAGE_1 == getLoginException().getMessageContainer().getKey()) {
                        // all logins have been disabled be the Administration
                        CmsLoginMessage loginMessage = OpenCms.getLoginManager().getLoginMessage();
                        if (loginMessage != null) {
                            m_message = Messages.get().container(
                                Messages.GUI_LOGIN_FAILED_WITH_MESSAGE_1,
                                loginMessage.getMessage());
                        }
                    }
                    if (m_message == null) {
                        // any other error - display default message
                        m_message = Messages.get().container(Messages.GUI_LOGIN_FAILED_0);
                    }
                }
            }
        } else if (Boolean.valueOf(m_actionLogout).booleanValue()) {
            m_action = ACTION_LOGOUT;
            // store the workplace window data
            Cookie wpDataCookie = getCookie(COOKIE_WP_DATA);
            String wpData = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_WPDATA);
            if (wpData != null) {
                wpData = CmsEncoder.escapeXml(wpData);
                wpDataCookie.setValue(wpData);
                setCookie(wpDataCookie, false);
            }
            // after logout this will automatically redirect to the login form again
            logout();
            return null;
        }

        if (m_action == ACTION_LOGIN) {
            // clear message
            m_message = null;
            // login is successful, check if the requested resource can be read
            CmsUriSplitter splitter = new CmsUriSplitter(m_requestedResource, true);
            String resource = splitter.getPrefix();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(resource)) {
                // bad resource name, use workplace as default
                resource = CmsFrameset.JSP_WORKPLACE_URI;
            }
            if (!getCmsObject().existsResource(resource, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED)) {
                // requested resource does either not exist or is not readable by user
                if (CmsFrameset.JSP_WORKPLACE_URI.equals(resource)) {
                    // we know the Workplace exists, so the user does not have access to the Workplace
                    // probably this is a "Guest" user in a default setup where "Guest" has no access to the Workplace
                    m_message = Messages.get().container(Messages.GUI_LOGIN_FAILED_NO_WORKPLACE_PERMISSIONS_0);
                    m_action = ACTION_DISPLAY;
                } else if (getCmsObject().existsResource(CmsFrameset.JSP_WORKPLACE_URI)) {
                    // resource does either not exist or is not readable, but general workplace permissions are granted
                    m_message = Messages.get().container(Messages.GUI_LOGIN_UNKNOWN_RESOURCE_1, m_requestedResource);
                    m_requestedResource = CmsFrameset.JSP_WORKPLACE_URI;
                } else {
                    // resource does not exist and no general workplace permissions granted
                    m_message = Messages.get().container(
                        Messages.GUI_LOGIN_FAILED_NO_TARGET_PERMISSIONS_1,
                        m_requestedResource);
                    m_action = ACTION_DISPLAY;
                }
            }
            if (m_action == ACTION_DISPLAY) {
                // the login was invalid
                m_requestedResource = null;
                // destroy the generated session
                HttpSession session = getRequest().getSession(false);
                if (session != null) {
                    session.invalidate();
                }
            } else {
                // successfully logged in, so set the cookie
                setCookieData();
            }
        }

        return displayLoginForm();
    }

    /**
     * Gets the login info from the cookies.<p>
     */
    public void getCookieData() {

        // get the PC type cookie
        Cookie pcTypeCookie = getCookie(COOKIE_PCTYPE);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(pcTypeCookie.getValue())) {
            // only set the data if needed
            if (m_pcType == null) {
                m_pcType = pcTypeCookie.getValue();
            }
        }
        if ("null".equals(m_pcType)) {
            m_pcType = null;
        }
        // get other cookies only on private PC types (or if security option is disabled)
        if ((m_pcType == null) || PCTYPE_PRIVATE.equals(m_pcType)) {
            // get the user name cookie
            Cookie userNameCookie = getCookie(COOKIE_USERNAME);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(userNameCookie.getValue())) {
                // only set the data if needed
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_username)) {
                    m_username = userNameCookie.getValue();
                }
                if (m_pcType == null) {
                    // set PC type to private PC if the user cookie is found
                    m_pcType = PCTYPE_PRIVATE;
                }
            }
            if ("null".equals(m_username)) {
                m_username = null;
            }
            // get the organizational unit cookie
            Cookie ouFqnCookie = getCookie(COOKIE_OUFQN);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(ouFqnCookie.getValue())) {
                // only set the data if needed
                if (m_oufqn == null) {
                    m_oufqn = ouFqnCookie.getValue();
                }
            }
            if ("null".equals(m_oufqn)) {
                m_oufqn = null;
            }
        }
    }

    /**
     * @see org.opencms.jsp.CmsJspLoginBean#getFormLink()
     */
    @Override
    public String getFormLink() {

        if (getPreDefOuFqn() == null) {
            return super.getFormLink();
        }
        String preDefOuFqn = (String)getRequest().getAttribute(PARAM_PREDEF_OUFQN);
        try {
            OpenCms.getOrgUnitManager().readOrganizationalUnit(getCmsObject(), preDefOuFqn);
        } catch (CmsException e) {
            // organizational unit does not exist
            return super.getFormLink();
        }
        return link("/system/login" + CmsEncoder.escapeXml(preDefOuFqn));
    }

    /**
     * Returns the available organizational units as JSON array string.<p>
     * 
     * @return the available organizational units as JSON array string
     */
    public String getJsonOrgUnitList() {

        List<CmsOrganizationalUnit> allOus = getOus();
        List<JSONObject> jsonOus = new ArrayList<JSONObject>(allOus.size());
        int index = 0;
        for (CmsOrganizationalUnit ou : allOus) {
            JSONObject jsonObj = new JSONObject();
            try {
                // 1: OU fully qualified name
                jsonObj.put("name", ou.getName());
                // 2: OU display name
                jsonObj.put("displayname", ou.getDisplayName(m_locale));
                // 3: OU simple name
                jsonObj.put("simplename", ou.getSimpleName());
                // 4: OU description
                jsonObj.put("description", ou.getDescription(m_locale));
                // 5: selection flag
                boolean isSelected = false;
                if (ou.getName().equals(m_oufqn)
                    || (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_oufqn) && ou.getName().equals(m_oufqn.substring(1)))) {
                    isSelected = true;
                }
                jsonObj.put("active", isSelected);
                // 6: level of the OU
                jsonObj.put("level", CmsResource.getPathLevel(ou.getName()));
                // 7: OU index
                jsonObj.put("index", index);
                // add the generated JSON object to the result list
                jsonOus.add(jsonObj);
                index++;
            } catch (JSONException e) {
                // error creating JSON object, skip this OU
            }
        }
        // generate a JSON array from the JSON object list
        JSONArray jsonArr = new JSONArray(jsonOus);
        return jsonArr.toString();
    }

    /**
     * Sets the login cookies.<p>
     */
    public void setCookieData() {

        // set the PC type cookie only if security dialog is enabled
        if (OpenCms.getLoginManager().isEnableSecurity() && CmsStringUtil.isNotEmpty(m_pcType)) {
            Cookie pcTypeCookie = getCookie(COOKIE_PCTYPE);
            pcTypeCookie.setValue(m_pcType);
            setCookie(pcTypeCookie, false);
        }

        // only store user name and OU cookies on private PC types
        if (PCTYPE_PRIVATE.equals(m_pcType)) {
            // set the user name cookie
            Cookie userNameCookie = getCookie(COOKIE_USERNAME);
            userNameCookie.setValue(m_username);
            setCookie(userNameCookie, false);

            // set the organizational unit cookie
            Cookie ouFqnCookie = getCookie(COOKIE_OUFQN);
            ouFqnCookie.setValue(m_oufqn);
            setCookie(ouFqnCookie, false);
        } else if (OpenCms.getLoginManager().isEnableSecurity() && PCTYPE_PUBLIC.equals(m_pcType)) {
            // delete user name and organizational unit cookies 
            Cookie userNameCookie = getCookie(COOKIE_USERNAME);
            setCookie(userNameCookie, true);
            Cookie ouFqnCookie = getCookie(COOKIE_OUFQN);
            setCookie(ouFqnCookie, true);

        }
    }

    /**
     * Appends the JavaScript for the login screen to the given HTML buffer.<p>
     * 
     * @param html the HTML buffer to append the script to
     * @param message the message to display after an unsuccessful login
     */
    protected void appendDefaultLoginScript(StringBuffer html, CmsMessageContainer message) {

        html.append("<script type=\"text/javascript\" src=\"");
        html.append(CmsWorkplace.getSkinUri()).append("jquery/packed/jquery.js");
        html.append("\"></script>\n");
        html.append("<script type=\"text/javascript\">\n");
        if (message != null) {
            html.append("function showAlert() {\n");
            html.append("\talert(\"");
            html.append(CmsStringUtil.escapeJavaScript(message.key(m_locale)));
            html.append("\");\n");
            html.append("}\n");
        }
        html.append("var orgUnitShow = false;\n");
        html.append("var orgUnits = null;\n");
        html.append("var activeOu = -1;\n");
        html.append("var searchTimeout;\n");
        html.append("var searchDefaultValue = \"");
        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_ORGUNIT_SEARCH_0));
        html.append("\";\n");

        // triggers the options to select the OU to login to
        html.append("function orgUnitSelection() {\n");
        html.append("\tif (!orgUnitShow) {\n");
        html.append("\t\tif (orgUnits == null) {\n");
        html.append("\t\t\t$.post(\"");
        html.append(getFormLink());
        html.append("\", { ");
        html.append(PARAM_ACTION_GETOULIST);
        html.append(": \"true\" }");
        html.append(", function(data){ fillOrgUnits(data); });\n");
        html.append("\t\t}\n");
        html.append("\t\tdocument.getElementById('ouSelId').style.display = 'block';\n");
        html.append("\t\tdocument.getElementById('ouLabelId').style.display = 'block';\n");
        html.append("\t\tdocument.getElementById('ouSearchId').style.display = 'block';\n");
        html.append("\t\tdocument.getElementById('ouBtnId').value = '");
        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_ORGUNIT_SELECT_OFF_0));
        html.append("';\n");
        html.append("\t} else {\n");
        html.append("\t\tdocument.getElementById('ouSelId').style.display = 'none';\n");
        html.append("\t\tdocument.getElementById('ouLabelId').style.display = 'none';\n");
        html.append("\t\tdocument.getElementById('ouSearchId').style.display = 'none';\n");
        html.append("\t\tdocument.getElementById('ouBtnId').value = '");
        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_ORGUNIT_SELECT_ON_0));
        html.append("';\n");
        html.append("\t}\n");
        html.append("\torgUnitShow = !orgUnitShow;\n");
        html.append("\tdocument.getElementById('titleId').style.display = 'block';\n");
        html.append("\tdocument.getElementById('titleIdOu').style.display = 'none';\n");
        html.append("}\n");

        // creates the HTML for the OUs to login to
        html.append("function fillOrgUnits(data) {\n");
        html.append("\torgUnits = eval(data);\n");
        html.append("\tvar html = \"\";\n");
        html.append("\tvar foundOu = false;\n");
        html.append("\tvar activeIndex = -1;\n");
        html.append("\tfor (var i = 0; i < orgUnits.length; i++) {\n");
        html.append("\t\tvar currOu = orgUnits[i];\n");
        html.append("\t\tvar actClass = \"\";\n");
        html.append("\t\tif (currOu.active == true) {\n");
        html.append("\t\t\t// this is the active OU\n");
        html.append("\t\t\tactiveOu = currOu.index;\n");
        html.append("\t\t\tactClass = \" class=\\\"active\\\"\";\n");
        html.append("\t\t}\n");
        html.append("\t\tvar actStyle = \"\";\n");
        html.append("\t\tif (currOu.level > 0) {\n");
        html.append("\t\t\tactStyle = \" style=\\\"margin-left: \" + (currOu.level * 20) + \"px;\\\"\";\n");
        html.append("\t\t}\n");
        html.append("\t\thtml += \"<div\";\n");
        html.append("\t\thtml += actClass;\n");
        html.append("\t\thtml += actStyle;\n");
        html.append("\t\thtml += \" id=\\\"ou\" + currOu.index;\n");
        html.append("\t\thtml += \"\\\" onclick=\\\"selectOu('\";\n");
        html.append("\t\thtml += currOu.name;\n");
        html.append("\t\thtml += \"', \" + currOu.index;\n");
        html.append("\t\thtml += \");\\\"><span class=\\\"name\\\">\";\n");
        html.append("\t\thtml += currOu.description;\n");
        html.append("\t\thtml += \"</span>\";\n");
        html.append("\t\tif (currOu.name != \"\") {\n");
        html.append("\t\t\thtml += \"<span class=\\\"path\\\"\";\n");
        html.append("\t\t\thtml += \" title=\\\"\";\n");
        html.append("\t\t\thtml += currOu.name;\n");
        html.append("\t\t\thtml += \"\\\">\";\n");
        html.append("\t\t\thtml += currOu.simplename;\n");
        html.append("\t\t\thtml += \"</span>\";\n");
        html.append("\t\t}\n");
        html.append("\t\thtml += \"</div>\";\n");
        html.append("\t}\n");
        html.append("\thtml += \"<div id=\\\"nooufound\\\" style=\\\"display: none;\\\"><span class=\\\"name\\\">\";\n");
        html.append("\thtml += \"");
        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_ORGUNIT_SEARCH_NORESULTS_0));
        html.append("\";\n");
        html.append("\thtml += \"</span></div>\";\n");
        html.append("\t$(\"#ouSelId\").append(html);\n");
        html.append("\t$(\"#ouSelId\").slideDown();\n");
        html.append("\tscrollToActiveOu();\n");
        html.append("}\n");

        // shows the list of OUs matching the search term or all OUs if the search term is empty
        html.append("function showOrgUnits(searchTerm) {\n");
        html.append("\tvar html = \"\";\n");
        html.append("\tvar foundOu = false;\n");
        html.append("\tfor (var i = 0; i < orgUnits.length; i++) {\n");
        html.append("\t\tvar currOu = orgUnits[i];\n");
        html.append("\t\tif (searchTerm != \"\") {\n");
        html.append("\t\t\tvar stLower = searchTerm.toLowerCase();\n");
        html.append("\t\t\tif (currOu.name.toLowerCase().indexOf(stLower )== -1 && currOu.description.toLowerCase().indexOf(stLower) == -1) {\n");
        html.append("\t\t\t\t$(\"#ou\" + i + \":visible\").slideUp();\n");
        html.append("\t\t\t} else {\n");
        html.append("\t\t\t\t$(\"#ou\" + i + \":hidden\").slideDown();\n");
        html.append("\t\t\t\t$(\"#ou\" + i).removeAttr(\"style\");\n");
        html.append("\t\t\t\tfoundOu = true;\n");
        html.append("\t\t\t}\n");
        html.append("\t\t} else {\n");
        html.append("\t\t\tfoundOu = true;\n");
        html.append("\t\t\tvar actStyle = \"\";\n");
        html.append("\t\t\tif (currOu.level > 0) {\n");
        html.append("\t\t\t\tactStyle = \"margin-left: \" + (currOu.level * 20) + \"px;\";\n");
        html.append("\t\t\t}\n");
        html.append("\t\t\t$(\"#ou\" + i).attr(\"style\", actStyle);\n");
        html.append("\t\t\t$(\"#ou\" + i + \":hidden\").slideDown();\n");
        html.append("\t\t}\n");
        html.append("\t}\n");
        html.append("\tif (searchTerm != \"\" && foundOu == false) {\n");
        html.append("\t\t$(\"#nooufound:hidden\").slideDown();\n");
        html.append("\t} else {\n");
        html.append("\t\t$(\"#nooufound:visible\").slideUp();\n");
        html.append("\t}\n");
        html.append("\tif (searchTerm == \"\") {\n");
        html.append("\t\tscrollToActiveOu();\n");
        html.append("\t}\n");
        html.append("}\n");

        // selects the OU to login to
        html.append("function selectOu(ouPath, ouIndex) {\n");
        html.append("\tif (ouIndex != -1 && ouIndex != activeOu) {\n");
        html.append("\t\t$(\"#ou\" + ouIndex).addClass(\"active\");\n");
        html.append("\t\torgUnits[ouIndex].active = true;\n");
        html.append("\t\t$(\"#");
        html.append(PARAM_OUFQN);
        html.append("\").val(ouPath);\n");
        html.append("\t\tif (activeOu != -1) {\n");
        html.append("\t\t\torgUnits[activeOu].active = false;\n");
        html.append("\t\t\t$(\"#ou\" + activeOu).removeClass();\n");
        html.append("\t\t}\n");
        html.append("\t\tactiveOu = ouIndex;\n");
        html.append("\t}\n");
        html.append("}\n");

        // filters the OUs by the provided search term using a timeout, called by the onkeyup event of the search input field
        html.append("function searchOu() {\n");
        html.append("\tvar searchElem = $(\"#");
        html.append(PARAM_OUSEARCH);
        html.append("\");\n");
        html.append("\tvar searchTerm = searchElem.val();\n");
        html.append("\tif (searchTerm == searchDefaultValue) {");
        html.append("\t\tsearchTerm = \"\";");
        html.append("\t}");
        html.append("\tclearTimeout(searchTimeout);\n");
        html.append("\tsearchTimeout = setTimeout(\"showOrgUnits(\\\"\" + trim(searchTerm) + \"\\\");\", 750);\n");
        html.append("}\n");

        // sets the value of the OU search input field, called by the onfocus and onblur event of the field
        html.append("function checkOuValue() {\n");
        html.append("\tvar searchElem = $(\"#");
        html.append(PARAM_OUSEARCH);
        html.append("\");\n");
        html.append("\tif (searchElem.val() == searchDefaultValue) {");
        html.append("\t\tsearchElem.val(\"\");");
        html.append("\t\tsearchElem.removeAttr(\"class\");");
        html.append("\t} else if (searchElem.val() == \"\") {");
        html.append("\t\tsearchElem.val(searchDefaultValue);");
        html.append("\t\tsearchElem.attr(\"class\", \"inactive\");");
        html.append("\t}");
        html.append("}\n");

        // scrolls to the currently selected OU if it is out of visible range
        html.append("function scrollToActiveOu() {\n");
        html.append("\tif (activeOu != -1) {\n");
        html.append("\t\tvar activeOffset = $(\"#ou\" + activeOu).offset().top;\n");
        html.append("\t\tvar parentOffset = $(\"#ouSelId\").offset().top;\n");
        html.append("\t\tactiveOffset = activeOffset - parentOffset;\n");
        html.append("\t\tif (activeOffset > $(\"#ouSelId\").height()) {;\n");
        html.append("\t\t\t$(\"#ouSelId\").animate({scrollTop: activeOffset}, 500);\n");
        html.append("\t\t};\n");
        html.append("\t}\n");
        html.append("}\n");

        // called when the login form page is loaded
        html.append("function doOnload() {\n");
        html.append("\tdocument.");
        html.append(PARAM_FORM);
        html.append(".");
        html.append(PARAM_USERNAME);
        html.append(".select();\n");
        html.append("\tdocument.");
        html.append(PARAM_FORM);
        html.append(".");
        html.append(PARAM_USERNAME);
        html.append(".focus();\n");
        if (message != null) {
            html.append("\tshowAlert();\n");
        }
        html.append("}\n");

        // helper function to trim a given string
        html.append("function trim (myStr) {\n");
        html.append("\treturn myStr.replace(/^\\s+/, '').replace (/\\s+$/, '');\n");
        html.append("}\n");

        html.append("</script>\n");
    }

    /**
     * Appends the JavaScript that opens the Direct Edit window after a successful login
     * to the given HTML buffer.<p>
     * 
     * @param html the html buffer to append the script to
     */
    protected void appendDirectEditOpenerScript(StringBuffer html) {

        html.append("<script type=\"text/javascript\">\n");
        html.append("function doOnload() {\n");

        // the window's name must be the same as in:
        // system/workplace/resources/commons/explorer.js
        html.append("window.name='preview';");
        html.append("window.location.replace('");
        html.append(link(m_directEditPath));
        html.append("');");

        html.append("}\n");
        html.append("</script>\n");
    }

    /**
     * Appends the HTML form name/id code for the given id to the given html.<p>
     * 
     * @param html the html where to append the id to
     * @param id the id to append
     */
    protected void appendId(StringBuffer html, String id) {

        html.append(" name=\"");
        html.append(id);
        html.append("\" id=\"");
        html.append(id);
        html.append("\" ");
    }

    /**
     * Appends the JavaScript that opens the Workplace window after a successful login
     * to the given HTML buffer.<p>
     * 
     * @param html the html buffer to append the script to
     * @param requestedResource the requested resource to open in a new window
     * @param message the message to display if the originally requested resource is not available
     */
    protected void appendWorkplaceOpenerScript(StringBuffer html, String requestedResource, CmsMessageContainer message) {

        String winId = "OpenCms" + System.currentTimeMillis();

        html.append("<script type=\"text/javascript\">\n");

        html.append("function doOnload() {\n");

        // display missing resource warning if required
        if (message != null) {
            html.append("\talert(\"");
            html.append(CmsStringUtil.escapeJavaScript(message.key(m_locale)));
            html.append("\");\n");
        }

        // display login message if required
        CmsLoginMessage loginMessage = OpenCms.getLoginManager().getLoginMessage();
        if ((loginMessage != null) && (loginMessage.isActive())) {
            String msg;
            if (loginMessage.isLoginForbidden()) {
                // login forbidden for normal users, current user must be Administrator
                msg = Messages.get().container(
                    Messages.GUI_LOGIN_SUCCESS_WITH_MESSAGE_2,
                    loginMessage.getMessage(),
                    new Date(loginMessage.getTimeEnd())).key(m_locale);
            } else {
                // just display the message
                msg = loginMessage.getMessage();
            }
            html.append("\talert(\"");
            html.append(CmsStringUtil.escapeJavaScript(msg));
            html.append("\");\n");
        }

        String openResource = requestedResource;

        // check if user agreement should be shown
        CmsLoginUserAgreement agreementInfo = new CmsLoginUserAgreement(this);
        if (agreementInfo.isShowUserAgreement()) {
            openResource = agreementInfo.getConfigurationVfsPath()
                + "?"
                + CmsLoginUserAgreement.PARAM_WPRES
                + "="
                + requestedResource;
        }

        html.append("\tvar openUri = \"");
        html.append(link(openResource));
        html.append("\";\n");
        html.append("\tvar workplaceWin = openWorkplace(openUri, \"");
        html.append(winId);
        html.append("\");\n");
        html.append("\tif (window.name != \"");
        html.append(winId);
        html.append("\") {\n");
        html.append("\t\twindow.opener = workplaceWin;\n");
        html.append("\t\tif (workplaceWin != null) {\n");
        html.append("\t\t\twindow.close();\n");
        html.append("\t\t}\n");
        html.append("\t}\n");
        html.append("}\n");

        html.append("function openWorkplace(url, name) {\n");

        Cookie wpDataCookie = getCookie(COOKIE_WP_DATA);
        boolean useCookieData = false;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(wpDataCookie.getValue())) {
            String[] winValues = CmsStringUtil.splitAsArray(wpDataCookie.getValue(), '|');
            if (winValues.length == 4) {
                useCookieData = true;
                html.append("\tvar winLeft = ").append(winValues[0]).append(";\n");
                html.append("\tvar winTop = ").append(winValues[1]).append(";\n");
                html.append("\tvar winWidth = ").append(winValues[2]).append(";\n");
                html.append("\tvar winHeight = ").append(winValues[3]).append(";\n");
            }
        }

        if (!useCookieData) {
            html.append("\tvar isInWin = (window.name.match(/^OpenCms\\d+$/) != null);\n");
            html.append("\tvar winHeight = 0, winWidth = 0, winTop = 0, winLeft = 0;\n");
            html.append("\tif (window.innerHeight) {\n");
            // Mozilla
            html.append("\t\twinHeight = window.innerHeight;\n");
            html.append("\t\twinWidth = window.innerWidth;\n");
            html.append("\t} else if (document.documentElement && document.documentElement.clientHeight) {\n");
            // IE 6 "strict" mode
            html.append("\t\twinHeight = document.documentElement.clientHeight;\n");
            html.append("\t\twinWidth = document.documentElement.clientWidth;\n");
            html.append("\t} else if (document.body && document.body.clientHeight) {\n");
            // IE 5, IE 6 "relaxed" mode
            html.append("\t\twinHeight = document.body.clientWidth;\n");
            html.append("\t\twinWidth = document.body.clientHeight;\n");
            html.append("\t}\n");
            html.append("\tif (window.screenY) {\n");
            // Mozilla
            html.append("\t\twinTop = window.screenY;\n");
            html.append("\t\twinLeft = window.screenX;\n");
            html.append("\t\tif (! isInWin) {\n");
            html.append("\t\t\twinTop += 25;\n");
            html.append("\t\t\twinLeft += 25;\n");
            html.append("\t\t}\n");
            html.append("\t} else if (window.screenTop) {\n");
            // IE
            html.append("\t\twinTop = window.screenTop;\n");
            html.append("\t\twinLeft = window.screenLeft;\n");
            html.append("\t}\n");
            html.append("\n");
        }

        if (requestedResource.startsWith(CmsWorkplace.VFS_PATH_WORKPLACE)) {
            html.append("\tvar openerStr = \"width=\" + winWidth + \",height=\" + winHeight + \",left=\" + winLeft + \",top=\" + winTop + \",scrollbars=no,location=no,toolbar=no,menubar=no,directories=no,status=yes,resizable=yes\";\n");
        } else {
            html.append("\tvar openerStr = \"width=\" + winWidth + \",height=\" + winHeight + \",left=\" + winLeft + \",top=\" + winTop + \",scrollbars=yes,location=yes,toolbar=yes,menubar=yes,directories=no,status=yes,resizable=yes\";\n");
        }
        html.append("\tvar OpenCmsWin = window.open(url, name, openerStr);\n");
        html.append("\n");
        html.append("\ttry{\n");
        html.append("\t\tif (! OpenCmsWin.opener) {\n");
        html.append("\t\t\tOpenCmsWin.opener = self;\n");
        html.append("\t\t}\n");
        html.append("\t\tif (OpenCmsWin.focus) {\n");
        html.append("\t\t\tOpenCmsWin.focus();\n");
        html.append("\t\t}\n");
        html.append("\t} catch (e) {}\n");
        html.append("\n");
        html.append("\treturn OpenCmsWin;\n");
        html.append("}\n");

        html.append("</script>\n");
    }

    /**
     * Returns the HTML for the login form.<p>
     * 
     * @return the HTML for the login form
     */
    protected String displayLoginForm() {

        StringBuffer html = new StringBuffer(8192);

        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n");
        html.append("<html><head>\n");
        html.append("<title>");

        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_TITLE_0));
        html.append("OpenCms " + OpenCms.getSystemInfo().getVersionNumber());

        html.append("</title>\n");

        String encoding = getRequestContext().getEncoding();
        html.append("<meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=");
        html.append(encoding);
        html.append("\">\n");

        // append workplace CSS
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        html.append(CmsWorkplace.getStyleUri(this, "workplace.css"));
        html.append("\">\n");

        // append favicon relation
        html.append("<link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"");
        html.append(CmsWorkplace.getSkinUri()).append("commons/favicon.ico");
        html.append("\">\n");

        if (m_action == ACTION_DISPLAY) {
            // append default script
            appendDefaultLoginScript(html, m_message);
        } else if (m_action == ACTION_LOGIN) {
            // append window opener script
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_directEditPath)) {
                appendDirectEditOpenerScript(html);
            } else {
                appendWorkplaceOpenerScript(html, m_requestedResource, m_message);
            }
        }

        html.append("</head>\n");

        html.append("<body class=\"dialog\" onload=\"doOnload();\">\n");

        html.append("<div style=\"text-align: center; padding-top: 50px;\">");
        html.append("<img src=\"");
        html.append(CmsWorkplace.getResourceUri("commons/login_logo.png"));
        html.append("\" alt=\"OpenCms Logo\">");
        html.append("</div>\n");

        html.append("<table class=\"logindialog\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>\n");
        html.append("<table class=\"dialogbox\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>\n");
        html.append("<div class=\"dialoghead\">");

        if (m_oufqn == null) {
            m_oufqn = CmsOrganizationalUnit.SEPARATOR;
        }
        if (m_action == ACTION_DISPLAY) {
            html.append("<div id='titleId'");
            if (!m_oufqn.equals(CmsOrganizationalUnit.SEPARATOR)) {
                html.append(" style='display: none;'");
            }
            html.append(">\n");
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_HEADLINE_0));
            html.append("</div>\n");
            html.append("<div id='titleIdOu'");
            if (m_oufqn.equals(CmsOrganizationalUnit.SEPARATOR)) {
                html.append(" style='display: none;'");
            }
            html.append(">\n");
            html.append(Messages.get().getBundle(m_locale).key(
                Messages.GUI_LOGIN_HEADLINE_SELECTED_ORGUNIT_1,
                m_ou.getDescription(getCmsObject().getRequestContext().getLocale())));
            html.append("</div>\n");
        } else if (m_action == ACTION_LOGIN) {
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_HEADLINE_ALREADY_IN_0));
        }

        html.append("</div>\n");

        if (m_action == ACTION_DISPLAY) {
            // start form
            html.append("<form style=\"margin: 0px; padding: 0px;\" action=\"");
            html.append(getFormLink());
            html.append("\"");
            if (PCTYPE_PUBLIC.equals(m_pcType)) {
                html.append(" autocomplete=\"off\"");
            }
            appendId(html, PARAM_FORM);
            html.append("method=\"POST\">\n");
        }

        html.append("<div class=\"dialogcontent\">\n");
        html.append("<table border=\"0\">\n");

        // show security option box if enabled in configuration
        if ((m_action == ACTION_DISPLAY) && OpenCms.getLoginManager().isEnableSecurity()) {
            html.append("<tr>\n");
            html.append("<td rowspan=\"2\">\n");
            // security image should not be shown any more
            //html.append("<img src=\"");
            //html.append(CmsWorkplace.getResourceUri("commons/login_security.png"));
            //html.append("\" height=\"48\" width=\"48\" alt=\"\">");
            html.append("</td>\n");
            html.append("<td colspan=\"2\" style=\"white-space: nowrap;\">\n");
            html.append("<div style=\"padding-bottom: 5px;\"><b>");
            html.append(CmsStringUtil.escapeHtml(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_SECURITY_0)));
            html.append("</b></div>\n");
            html.append("</td>\n");
            html.append("</tr>\n");
            html.append("<tr>\n");
            html.append("<td colspan=\"2\" style=\"white-space: nowrap;\">");
            html.append("<div class=\"loginsecurity\">");
            html.append("<input type=\"radio\" value=\"");
            html.append(PCTYPE_PUBLIC);
            html.append("\" name=\"");
            html.append(PARAM_PCTYPE);
            html.append("\"");
            if (PCTYPE_PUBLIC.equals(m_pcType)) {
                html.append(" checked=\"checked\"");
            }
            html.append(">&nbsp;");
            html.append(CmsStringUtil.escapeHtml(Messages.get().getBundle(m_locale).key(
                Messages.GUI_LOGIN_PCTYPE_PUBLIC_0)));
            html.append("<br/>");
            html.append("<input type=\"radio\" value=\"");
            html.append(PCTYPE_PRIVATE);
            html.append("\" name=\"");
            html.append(PARAM_PCTYPE);
            html.append("\"");
            if (PCTYPE_PRIVATE.equals(m_pcType)) {
                html.append(" checked=\"checked\"");
            }
            html.append(">&nbsp;");
            html.append(CmsStringUtil.escapeHtml(Messages.get().getBundle(m_locale).key(
                Messages.GUI_LOGIN_PCTYPE_PRIVATE_0)));
            html.append("</div></td>\n");
            html.append("</tr>\n");
        }

        html.append("<tr>\n");
        html.append("<td></td>\n<td colspan=\"2\" style=\"white-space: nowrap;\">\n");
        html.append("<div style=\"padding-bottom: 10px;\">");

        if (m_action == ACTION_DISPLAY) {
            html.append(CmsStringUtil.escapeHtml(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_MESSAGE_0)));
        } else if (m_action == ACTION_LOGIN) {
            html.append(CmsStringUtil.escapeHtml(Messages.get().getBundle(m_locale).key(
                Messages.GUI_LOGIN_MESSAGE_ALREADY_IN_0)));
        }

        html.append("</div>\n");
        html.append("</td>\n");
        html.append("</tr>\n");

        html.append("<tr>\n");

        html.append("<td style=\"width: 60px; text-align: center; vertical-align: top\" rowspan=\"5\">");
        html.append("<img src=\"");
        html.append(CmsWorkplace.getResourceUri("commons/login.png"));
        html.append("\" height=\"48\" width=\"48\" alt=\"\">");
        html.append("</td>\n");

        html.append("<td style=\"white-space: nowrap;\"><b>");
        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_USERNAME_0));
        html.append("</b>&nbsp;&nbsp;</td>\n");
        html.append("<td style=\"width: 300px; white-space: nowrap;\">");

        if (m_action == ACTION_DISPLAY) {
            // append input for user name
            html.append("<input style=\"width: 300px;\" type=\"text\"");
            if (PCTYPE_PUBLIC.equals(m_pcType)) {
                html.append(" autocomplete=\"off\"");
            }
            appendId(html, PARAM_USERNAME);
            html.append("value=\"");
            html.append((CmsStringUtil.isEmpty(m_username) || PCTYPE_PUBLIC.equals(m_pcType))
            ? ""
            : CmsEncoder.escapeXml(m_username));
            html.append("\">");
        } else if (m_action == ACTION_LOGIN) {
            // append name of user that has been logged in
            html.append(getRequestContext().getCurrentUser().getFullName());
        }

        html.append("</td>\n");
        html.append("</tr>\n");

        if (m_action == ACTION_DISPLAY) {
            // append 2 rows: input for user name and login button
            html.append("<tr>\n");
            html.append("<td style=\"white-space: nowrap;\"><b>");
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_PASSWORD_0));
            html.append("</b>&nbsp;&nbsp;</td>\n");
            html.append("<td style=\"width: 300px; white-space: nowrap;\">");
            html.append("<input style=\"width: 300px;\" type=\"password\"");
            if (PCTYPE_PUBLIC.equals(m_pcType)) {
                html.append(" autocomplete=\"off\"");
            }
            appendId(html, PARAM_PASSWORD);
            html.append(">");
            html.append("</td>\n");
            html.append("</tr>\n");

            html.append("<tr>\n");
            html.append("<td style=\"white-space: nowrap;\"><div id='ouLabelId' style='display: none;'><b>");
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_ORGUNIT_0)).append(
                "</b>&nbsp;&nbsp;\n");
            html.append("</div></td>\n");
            html.append("<td style=\"width: 300px; white-space: nowrap;\"><div id='ouSearchId' style='display: none;'><input class=\"inactive\" style=\"width: 300px;\" type=\"text\" value=\"");
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_ORGUNIT_SEARCH_0));
            html.append("\"");
            appendId(html, PARAM_OUSEARCH);
            html.append(" onfocus=\"checkOuValue();\"");
            html.append(" onblur=\"checkOuValue();\"");
            html.append(" onkeyup=\"searchOu();\"");
            html.append("/>");
            html.append("<input type=\"hidden\" value=\"");
            html.append(m_oufqn == null ? "" : m_oufqn);
            html.append("\"");
            appendId(html, PARAM_OUFQN);
            html.append("/>");
            html.append("</div></td>\n");
            html.append("</tr>\n");

            html.append("<tr>\n");
            html.append("<td colspan=\"2\"><div id='ouSelId' style='display: none;'>");
            html.append("</div></td>\n");
            html.append("</tr>\n");

            html.append("<tr>\n");
            html.append("<td>\n");
            html.append("</td>\n");
            html.append("<td style=\"white-space: nowrap;\">\n");
            html.append("<input type=\"hidden\"");
            appendId(html, PARAM_ACTION_LOGIN);
            html.append("value=\"true\">\n");

            if (m_requestedResource != null) {
                html.append("<input type=\"hidden\"");
                appendId(html, CmsWorkplaceManager.PARAM_LOGIN_REQUESTED_RESOURCE);
                html.append("value=\"");
                html.append(CmsEncoder.escapeXml(m_requestedResource));
                html.append("\">\n");
            }

            html.append("<input class=\"loginbutton\" type=\"submit\" value=\"");
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_BUTTON_0));
            html.append("\">\n");

            if ((getOus().size() > 1)
                && ((getPreDefOuFqn() == null) || getPreDefOuFqn().equals(CmsOrganizationalUnit.SEPARATOR))) {
                // options
                html.append("&nbsp;<input id='ouBtnId' class='loginbutton' type='button' value='");
                html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_ORGUNIT_SELECT_ON_0));
                html.append("' onclick='javascript:orgUnitSelection();'>\n");
            }
            html.append("</td>\n");
            html.append("</tr>\n");
        } else if (m_action == ACTION_LOGIN) {
            // append 2 rows: one empty, other for button with re-open window script
            html.append("<tr><td></td><td></td></tr>\n");

            html.append("<tr>\n");
            html.append("<td></td>\n");
            html.append("<td style=\"width:100%; white-space: nowrap;\">\n");
            html.append("<input class=\"loginbutton\" type=\"button\" value=\"");
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_BUTTON_ALREADY_IN_0));
            html.append("\" onclick=\"doOnload()\">\n");
            html.append("</td>\n");
            html.append("</tr>\n");
        }

        html.append("</table>\n");
        html.append("</div>");

        if (m_action == ACTION_DISPLAY) {
            // end form
            html.append("</form>\n");
        }

        html.append("</td></tr></table>\n");
        html.append("</td></tr></table>\n");

        html.append("<div style=\"text-align: center; font-size: 10px; white-space: nowrap;\">");
        html.append("<a href=\"http://www.opencms.org\" target=\"_blank\">OpenCms</a> ");
        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_OPENCMS_IS_FREE_SOFTWARE_0));
        html.append("</div>\n");
        html.append("<div style=\"text-align: center; font-size: 10px; white-space: nowrap;\">");
        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_TRADEMARKS_0));
        html.append("</div>\n");
        html.append("<div style=\"text-align: center; font-size: 10px; white-space: nowrap;\">");
        html.append("&copy; 2002 - 2012 Alkacon Software GmbH. ");
        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_RIGHTS_RESERVED_0));
        html.append("</div>\n");

        html.append("<noscript>\n");
        html.append("<div style=\"text-align: center; font-size: 14px; border: 2px solid black; margin: 50px; padding: 20px; background-color: red; color: white; white-space: nowrap;\"><b>");
        html.append(CmsStringUtil.escapeHtml(Messages.get().getBundle(m_locale).key(
            Messages.GUI_LOGIN_NOSCRIPT_1,
            OpenCms.getSiteManager().getWorkplaceSiteMatcher())));
        html.append("</b></div>\n");
        html.append("</noscript>\n");

        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Returns the cookie with the given name, if not cookie is found a new one is created.<p>
     * 
     * @param name the name of the cookie
     * 
     * @return the cookie
     */
    protected Cookie getCookie(String name) {

        Cookie[] cookies = getRequest().getCookies();
        for (int i = 0; (cookies != null) && (i < cookies.length); i++) {
            if (name.equalsIgnoreCase(cookies[i].getName())) {
                return cookies[i];
            }
        }
        return new Cookie(name, "");
    }

    /**
     * Returns all organizational units in the system.<p>
     * 
     * @return a list of {@link CmsOrganizationalUnit} objects
     */
    protected List<CmsOrganizationalUnit> getOus() {

        if (m_ous == null) {
            m_ous = new ArrayList<CmsOrganizationalUnit>();
            try {
                if (getPreDefOuFqn() == null) {
                    m_ous.add(OpenCms.getOrgUnitManager().readOrganizationalUnit(getCmsObject(), ""));
                    m_ous.addAll(OpenCms.getOrgUnitManager().getOrganizationalUnits(getCmsObject(), "", true));
                    Iterator<CmsOrganizationalUnit> itOus = m_ous.iterator();
                    while (itOus.hasNext()) {
                        CmsOrganizationalUnit ou = itOus.next();
                        if (ou.hasFlagHideLogin() || ou.hasFlagWebuser()) {
                            itOus.remove();
                        }
                    }
                } else {
                    m_ous.add(OpenCms.getOrgUnitManager().readOrganizationalUnit(getCmsObject(), m_oufqn));
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return m_ous;
    }

    /**
     * Returns the predefined organizational unit fqn.<p>
     * 
     * This is normally selected by url, and set by the {@link CmsWorkplaceLoginHandler}.<p>
     * 
     * @return the predefined organizational unit fqn
     */
    protected String getPreDefOuFqn() {

        if (Boolean.valueOf(m_actionLogout).booleanValue() && (getRequest().getAttribute(PARAM_PREDEF_OUFQN) == null)) {
            String oufqn = getCmsObject().getRequestContext().getOuFqn();
            if (!oufqn.startsWith(CmsOrganizationalUnit.SEPARATOR)) {
                oufqn = CmsOrganizationalUnit.SEPARATOR + oufqn;
            }
            getRequest().setAttribute(CmsLogin.PARAM_PREDEF_OUFQN, oufqn);
        }
        return (String)getRequest().getAttribute(PARAM_PREDEF_OUFQN);
    }

    /**
     * Sets the cookie in the response.<p>
     * 
     * @param cookie the cookie to set
     * @param delete flag to determine if the cookir should be deleted
     */
    protected void setCookie(Cookie cookie, boolean delete) {

        if (getRequest().getAttribute(PARAM_PREDEF_OUFQN) != null) {
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
        cookie.setPath(link("/system/login"));
        // set the cookie
        getResponse().addCookie(cookie);
    }

    /**
     * Returns the direct edit path from the user settings, or <code>null</code> if not set.<p>
     * 
     * @param userSettings the user settings
     * 
     * @return the direct edit path
     */
    private String getDirectEditPath(CmsUserSettings userSettings) {

        if (userSettings.getStartView().equals(CmsWorkplace.VIEW_DIRECT_EDIT)) {
            String folder = userSettings.getStartFolder();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(getCmsObject().getRequestContext().getSiteRoot())
                || getCmsObject().getRequestContext().getSiteRoot().equals("/")) {
                folder = CmsStringUtil.joinPaths(userSettings.getStartSite(), folder);
            }
            try {
                CmsResource targetRes = getCmsObject().readDefaultFile(folder);
                if (targetRes != null) {
                    return targetRes.getRootPath();
                }
            } catch (Exception e) {
                LOG.debug(e);
            }
        }
        return null;
    }
}
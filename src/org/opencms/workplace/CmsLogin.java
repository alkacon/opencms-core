/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsLogin.java,v $
 * Date   : $Date: 2005/06/21 11:05:17 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace;

import org.opencms.db.CmsLoginMessage;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.i18n.CmsAcceptLanguageHeaderParser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Handles the login of Users to the OpenCms workplace.<p> 
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.9 $
 * 
 * @since 6.0
 */
public class CmsLogin extends CmsJspLoginBean {

    /** Action constant: Default action, display the dialog. */
    private static final int ACTION_DISPLAY = 0;

    /** Action constant: Login sucessful. */
    private static final int ACTION_LOGIN = 1;

    /** Action constant: Logout. */
    private static final int ACTION_LOGOUT = 2;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLogin.class);

    /** The parameter name for the "login" action. */
    private static final String PARAM_ACTION_LOGIN = "login";

    /** The parameter name for the "logout" action. */
    private static final String PARAM_ACTION_LOGOUT = "logout";

    /** The html id for the login form. */
    private static final String PARAM_FORM = "ocLoginForm";

    /** The parameter name for the password. */
    private static final String PARAM_PASSWORD = "ocPword";

    /** The parameter name for the user name. */
    private static final String PARAM_USERNAME = "ocUname";

    /** The action to perform. */
    private int m_action;

    /** The value of the "login" action parameter. */
    private String m_actionLogin;

    /** The value of the "logout" action parameter. */
    private String m_actionLogout;

    /** The locale to use for display, this will not be the workplace locale, but the browser locale. */
    private Locale m_locale;

    /** The message to display with the dialog in a JavaScrip alert. */
    private CmsMessageContainer m_message;

    /** The value of the password parameter. */
    private String m_password;

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
        res.setDateHeader(I_CmsConstants.C_HEADER_LAST_MODIFIED, System.currentTimeMillis());
        CmsRequestUtil.setNoCacheHeaders(res);

        // divine the best locale from the users browser settings
        CmsAcceptLanguageHeaderParser parser = new CmsAcceptLanguageHeaderParser(
            req,
            OpenCms.getWorkplaceManager().getDefaultLocale());
        List acceptedLocales = parser.getAcceptedLocales();
        List workplaceLocales = OpenCms.getWorkplaceManager().getLocales();
        m_locale = OpenCms.getLocaleManager().getFirstMatchingLocale(acceptedLocales, workplaceLocales);
        if (m_locale == null) {
            // no match found - use OpenCms default locale
            m_locale = OpenCms.getWorkplaceManager().getDefaultLocale();
        }
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
            StringBuffer loginLink = new StringBuffer();
            loginLink.append(OpenCms.getSiteManager().getWorkplaceSiteMatcher().toString());
            loginLink.append(getFormLink());
            // send a redirect to the workplace site
            getResponse().sendRedirect(loginLink.toString());
            return null;
        }

        CmsObject cms = getCmsObject();

        m_message = null;
        m_requestedResource = CmsRequestUtil.getNotEmptyDecodedParameter(
            getRequest(),
            CmsWorkplaceManager.PARAM_LOGIN_REQUESTED_RESOURCE);
        if (m_requestedResource == null) {
            // no resource was requested, use default workplace URI
            m_requestedResource = CmsWorkplaceAction.C_JSP_WORKPLACE_URI;
        }

        if (cms.getRequestContext().currentUser().isGuestUser()) {

            // user is not currently logged in
            m_action = ACTION_DISPLAY;
            m_username = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_USERNAME);
            if (m_username != null) {
                // remove white spaces, can only lead to confusion on user name
                m_username = m_username.trim();
            }
            m_password = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_PASSWORD);
            m_actionLogin = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_ACTION_LOGIN);

        } else {

            // user is already logged in
            m_action = ACTION_LOGIN;
            m_actionLogout = CmsRequestUtil.getNotEmptyParameter(getRequest(), PARAM_ACTION_LOGOUT);
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
                login(m_username, m_password);

                if (getLoginException() == null) {
                    // the login was successful
                    m_action = ACTION_LOGIN;

                    // set the default project of the user
                    CmsUserSettings settings = new CmsUserSettings(cms.getRequestContext().currentUser());
                    try {
                        CmsProject project = cms.readProject(settings.getStartProject());
                        if (cms.getAllAccessibleProjects().contains(project)) {
                            // user has access to the project, set this as current project
                            cms.getRequestContext().setCurrentProject(project);
                        }
                    } catch (Exception e) {
                        // unable to set the startup project, bad but not critical
                        LOG.warn(Messages.get().key(
                            Messages.LOG_LOGIN_NO_STARTUP_PROJECT_2,
                            m_username,
                            settings.getStartProject()), e);
                    }
                } else {
                    // there was an error during login

                    if (org.opencms.security.Messages.ERR_LOGIN_FAILED_DISABLED_3 == getLoginException().getMessageContainer().getKey()) {
                        // the user account is disabled
                        m_message = Messages.get().container(Messages.GUI_LOGIN_FAILED_DISABLED_0);
                    } else if (org.opencms.security.Messages.ERR_LOGIN_FAILED_TEMP_DISABLED_5 == getLoginException().getMessageContainer().getKey()) {
                        // the user account is temporarily disabled because of too many login failures
                        m_message = Messages.get().container(Messages.GUI_LOGIN_FAILED_TEMP_DISABLED_0);
                    } else if (org.opencms.security.Messages.ERR_LOGIN_FAILED_WITH_MESSAGE_1 == getLoginException().getMessageContainer().getKey()) {
                        // all logins have been diasabled be the Administration
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
            // after logout this will automatically redirect to the login form again
            logout();
            return null;
        }

        return displayLoginForm();
    }

    /**
     * Appends the JavaScript for the login screen
     * to the given HTML buffer.<p>
     * 
     * @param html the html buffer to append the script to
     * @param message the message to display after an unsuccessful login
     */
    protected void appendDefaultLoginScript(StringBuffer html, CmsMessageContainer message) {

        html.append("<script type=\"text/javascript\">\n");

        if (message != null) {
            html.append("function showAlert() {\n");
            html.append("\talert(\"");
            html.append(CmsStringUtil.escapeJavaScript(message.key(m_locale)));
            html.append("\");\n");
            html.append("}\n");
        }

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

        html.append("</script>\n");
    }

    /**
     * Appends the JavaScript that opens the Workplace window after a successful login
     * to the given HTML buffer.<p>
     * 
     * @param html the html buffer to append the script to
     * @param requestedResource the requested resource to open in a new window
     */
    protected void appendWorkplaceOpenerScript(StringBuffer html, String requestedResource) {

        String winId = "OpenCms" + System.currentTimeMillis();

        html.append("<script type=\"text/javascript\">\n");

        html.append("function doOnload() {\n");

        // display login message if required
        CmsLoginMessage loginMessage = OpenCms.getLoginManager().getLoginMessage();
        if ((loginMessage != null) && (loginMessage.isActive())) {
            String message;
            if (loginMessage.isLoginForbidden()) {
                // login forbidden for normal users, current user must be Administrator
                message = Messages.get().container(
                    Messages.GUI_LOGIN_SUCCESS_WITH_MESSAGE_2,
                    loginMessage.getMessage(),
                    new Date(loginMessage.getTimeEnd())).key(m_locale);
            } else {
                // just display the message
                message = loginMessage.getMessage();
            }
            html.append("\talert(\"");
            html.append(CmsStringUtil.escapeJavaScript(message));
            html.append("\");\n");
        }

        html.append("\tvar openUri = \"");
        html.append(link(requestedResource));
        html.append("\";\n");
        html.append("\tvar workplaceWin = openWorkplace(openUri, \"");
        html.append(winId);
        html.append("\");\n");
        html.append("\tif (window.name != \"");
        html.append(winId);
        html.append("\") {\n");
        html.append("\t\twindow.opener = workplaceWin;\n");
        html.append("\t\twindow.close();\n");
        html.append("\t}\n");
        html.append("}\n");

        html.append("function openWorkplace(url, name) {\n");
        html.append("\tvar isInWin = (window.name.match(/^OpenCms\\d+$/) != null);\n");
        html.append("\tif (window.innerHeight) {\n");
        // Mozilla
        html.append("\t\tvar winHeight = window.innerHeight;\n");
        html.append("\t\tvar winWidth = window.innerWidth;\n");
        html.append("\t} else if (document.documentElement && document.documentElement.clientHeight) {\n");
        // IE 6 "strict" mode
        html.append("\t\tvar winHeight = document.documentElement.clientHeight;\n");
        html.append("\t\tvar winWidth = document.documentElement.clientWidth;\n");
        html.append("\t} else if (document.body && document.body.clientHeight) {\n");
        // IE 5, IE 6 "relaxed" mode
        html.append("\t\tvar winHeight = document.body.clientWidth;\n");
        html.append("\t\tvar winWidth = document.body.clientHeight;\n");
        html.append("\t}\n");
        html.append("\tif (window.screenY) {\n");
        // Mozilla
        html.append("\t\tvar winTop = window.screenY;\n");
        html.append("\t\tvar winLeft = window.screenX;\n");
        html.append("\t\tif (! isInWin) {\n");
        html.append("\t\t\twinTop += 25;\n");
        html.append("\t\t\twinLeft += 25;\n");
        html.append("\t\t}\n");
        html.append("\t} else if (window.screenTop) {\n");
        // IE
        html.append("\t\tvar winTop = window.screenTop;\n");
        html.append("\t\tvar winLeft = window.screenLeft;\n");
        html.append("\t}\n");
        html.append("\n");
        html.append("\tvar openerStr = \"width=\" + winWidth + \",height=\" + winHeight + \",left=\" + winLeft + \",top=\" + winTop + \",location=no,toolbar=no,menubar=no,directories=no,status=yes,resizable=yes\";\n");
        html.append("\tvar OpenCmsWin = window.open(url, name, openerStr);\n");
        html.append("\n");
        html.append("\tif (! OpenCmsWin.opener) {\n");
        html.append("\t\tOpenCmsWin.opener = self;\n");
        html.append("\t}\n");
        html.append("\tif (OpenCmsWin.focus) {\n");
        html.append("\t\tOpenCmsWin.focus();\n");
        html.append("\t}\n");
        html.append("\t\n");
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

        StringBuffer html = new StringBuffer();

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

        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        html.append(CmsWorkplace.getStyleUri(this, "workplace.css"));
        html.append("\">\n");

        if (m_action == ACTION_DISPLAY) {
            // append default script
            appendDefaultLoginScript(html, m_message);
        } else if (m_action == ACTION_LOGIN) {
            // append window opener script
            appendWorkplaceOpenerScript(html, m_requestedResource);
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

        if (m_action == ACTION_DISPLAY) {
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_HEADLINE_0));
        } else if (m_action == ACTION_LOGIN) {
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_HEADLINE_ALREADY_IN_0));
        }

        html.append("</div>\n");

        if (m_action == ACTION_DISPLAY) {
            // start form
            html.append("<form style=\"margin: 0px; padding: 0px;\" action=\"");
            html.append(getFormLink());
            html.append("\"");
            appendId(html, PARAM_FORM);
            html.append("method=\"POST\">\n");
        }

        html.append("<div class=\"dialogcontent\">\n");
        html.append("<table border=\"0\">\n");

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

        html.append("<td style=\"width: 60px; text-align: center; vertical-align: top\" rowspan=\"3\">");
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
            html.append("<input style=\"width: 100%\" type=\"text\"");
            appendId(html, PARAM_USERNAME);
            html.append("value=\"");
            html.append(CmsStringUtil.isEmpty(m_username) ? "" : m_username);
            html.append("\">");
        } else if (m_action == ACTION_LOGIN) {
            // append name of user that has been logged in
            html.append(getRequestContext().currentUser().getFullName());
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
            html.append("<input style=\"width: 100%\" type=\"password\"");
            appendId(html, PARAM_PASSWORD);
            html.append(">");
            html.append("</td>\n");
            html.append("</tr>\n");

            html.append("<tr>\n");
            html.append("<td></td>\n<td style=\"white-space: nowrap;\">\n");
            html.append("<input type=\"hidden\"");
            appendId(html, PARAM_ACTION_LOGIN);
            html.append("value=\"true\">\n");

            if (m_requestedResource != null) {
                html.append("<input type=\"hidden\"");
                appendId(html, CmsWorkplaceManager.PARAM_LOGIN_REQUESTED_RESOURCE);
                html.append("value=\"");
                html.append(CmsEncoder.encode(m_requestedResource));
                html.append("\">\n");
            }

            html.append("<input class=\"loginbutton\" type=\"submit\" value=\"");
            html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_BUTTON_0));
            html.append("\">\n");

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
        html.append("&copy; 2005 Alkacon Software GmbH. ");
        html.append(Messages.get().getBundle(m_locale).key(Messages.GUI_LOGIN_RIGHTS_RESERVED_0));
        html.append("</div>\n");

        html.append("<noscript>\n");
        html.append("<div style=\"text-align: center; font-size: 14px; border: 2px solid black; margin: 50px; padding: 20px; background-color: red; color: white; white-space: nowrap;\"><b>");
        html.append(CmsStringUtil.escapeHtml(Messages.get().key(
            m_locale,
            Messages.GUI_LOGIN_NOSCRIPT_1,
            new Object[] {OpenCms.getSiteManager().getWorkplaceSiteMatcher()})));
        html.append("</b></div>\n");
        html.append("</noscript>\n");

        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Appends the HTML form name/id code for the given id to the given html.<p>
     * 
     * @param html the html where to append the id to
     * @param id the id to append
     */
    private void appendId(StringBuffer html, String id) {

        html.append(" name=\"");
        html.append(id);
        html.append("\" id=\"");
        html.append(id);
        html.append("\" ");
    }
}

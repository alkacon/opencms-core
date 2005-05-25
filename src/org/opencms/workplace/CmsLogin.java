/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsLogin.java,v $
 * Date   : $Date: 2005/05/25 10:56:53 $
 * Version: $Revision: 1.1 $
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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Handles the login of Users to the OpenCms workplace.<p> 
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 6.0
 */
public class CmsLogin extends CmsJspLoginBean {

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

    /** The value of the "login" action parameter. */
    private String m_actionLogin;

    /** The value of the "logout" action parameter. */
    private String m_actionLogout;

    /** The message to display with the dialog in a JavaScrip alert. */
    private String m_message;

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
        res.setHeader(I_CmsConstants.C_HEADER_CACHE_CONTROL, I_CmsConstants.C_HEADER_VALUE_MAX_AGE + "0");
        res.addHeader(I_CmsConstants.C_HEADER_CACHE_CONTROL, I_CmsConstants.C_HEADER_VALUE_MUST_REVALIDATE);
    }

    /**
     * Returns the HTML for the login dialog in it's current state.<p>
     * 
     * @return the HTML for the login dialog
     */
    public String displayDialog() {

        CmsObject cms = getCmsObject();

        m_username = CmsRequestUtil.getParameter(getRequest(), PARAM_USERNAME);
        m_password = CmsRequestUtil.getParameter(getRequest(), PARAM_PASSWORD);
        m_actionLogin = CmsRequestUtil.getParameter(getRequest(), PARAM_ACTION_LOGIN);
        m_actionLogout = CmsRequestUtil.getParameter(getRequest(), PARAM_ACTION_LOGOUT);
        m_requestedResource = CmsRequestUtil.getParameter(
            getRequest(),
            CmsWorkplaceManager.PARAM_LOGIN_REQUESTED_RESOURCE);

        m_message = null;

        if (Boolean.valueOf(m_actionLogin).booleanValue()) {

            // login was requested
            if ((m_username == null) && (m_password == null)) {
                m_message = "Please enter a user name and a password!";
            } else if (m_username == null) {
                m_message = "Please enter a user name!";
            } else if (m_password == null) {
                m_message = "Please enter a password!";
            } else if ((m_username != null) && (m_password != null)) {

                // all required parameters available
                if (m_requestedResource == null) {
                    // no resource was requested, use default workplace URI
                    m_requestedResource = CmsWorkplaceAction.C_JSP_WORKPLACE_URI;
                }

                // try to login with the given user information
                login(m_username, m_password);

                if (getLoginException() == null) {
                    // the login was successfull

                    // set the default project of the user
                    CmsUserSettings settings = new CmsUserSettings(cms.getRequestContext().currentUser());
                    try {
                        CmsProject project = cms.readProject(settings.getStartProject());
                        if (cms.getAllAccessibleProjects().contains(project)) {
                            // user has access to the project, set this as current project
                            cms.getRequestContext().setCurrentProject(project);
                        }
                    } catch (Exception e) {
                        // the project does not exist, maybe it was deleted
                        int todo = 0;
                        // TODO: just log this error
                        e.printStackTrace();
                    }
                    return createWorkplaceOpenerScript();
                } else {
                    m_message = "Login has failed!";
                }
            }

        } else if (Boolean.valueOf(m_actionLogout).booleanValue()) {

            // logout was requested
            try {
                // this will automatically redirect to the login form
                logout();
                return null;
            } catch (IOException e) {
                // redirect failed - nothing we can do about this
                int todo = 0;
                // TODO: just log this error
                e.printStackTrace();
            }
        }

        return displayLoginForm();
    }

    /**
     * Returns the HTML/JavaScript that opens the Workplace window after a successful login.<p>
     * 
     * @return the HTML/JavaScript that opens the Workplace window after a successful login
     */
    protected String createWorkplaceOpenerScript() {

        StringBuffer html = new StringBuffer();

        html.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n");
        html.append("<html><head>\n");
        html.append("<title>");

        html.append("Login to OpenCms " + OpenCms.getSystemInfo().getVersionNumber());

        html.append("</title>\n");
        html.append("<meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=");
        html.append(getRequestContext().getEncoding());
        html.append("\">\n");

        html.append("<script language=\"javascript\" type=\"text/javascript\">\n");

        String winId = "OpenCms" + System.currentTimeMillis();
        html.append("function doLogin() {\n");
        html.append("\tvar openUri = \"");
        html.append(link(m_requestedResource));
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
        html.append("\tif (window.innerHeight) {\n");
        html.append("\t\tvar winHeight = window.innerHeight;\n");
        html.append("\t\tvar winWidth = window.innerWidth;\n");
        html.append("\t} else if (document.documentElement && document.documentElement.clientHeight) {\n");
        html.append("\t\tvar winHeight = document.documentElement.clientHeight;\n");
        html.append("\t\tvar winWidth = document.documentElement.clientWidth;\n");
        html.append("\t} else if (document.body && document.body.clientHeight) {\n");
        html.append("\t\tvar winHeight = document.body.clientWidth;\n");
        html.append("\t\tvar winWidth = document.body.clientHeight;\n");
        html.append("\t}\n");
        html.append("\tif (window.screenY) {\n");
        html.append("\t\tvar winTop = window.screenY;\n");
        html.append("\t\tvar winLeft = window.screenX;\n");
        html.append("\t} else if (window.screenTop) {\n");
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
        html.append("</head>\n");

        html.append("<body onload=\"doLogin();\">");
        html.append("<h3>");
        html.append("OpenCms Login page: Please close this window!");
        html.append("</h3>");
        html.append("</body>");

        html.append("</html>\n");

        return html.toString();
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

        html.append("Login to OpenCms " + OpenCms.getSystemInfo().getVersionNumber());

        html.append("</title>\n");

        String encoding = getRequestContext().getEncoding();

        html.append("<meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=");
        html.append(encoding);
        html.append("\">\n");

        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/opencms/export/system/workplace/commons/style/workplace.css\">\n");

        html.append("<script language=\"javascript\" type=\"text/javascript\">\n");

        if (m_message != null) {
            html.append("function showAlert() {\n");
            html.append("\talert(\"");
            html.append(m_message);
            html.append("\");\n");
            html.append("}\n");
        }

        html.append("function setFocus() {\n");
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

        if (m_message != null) {
            html.append("showAlert();\n");
        }
        html.append("}\n");

        html.append("</script>\n");
        html.append("</head>\n");

        html.append("<body class=\"dialog\" onload=\"setFocus();\">");

        html.append("<table class=\"dialog\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>\n");
        html.append("<table class=\"dialogbox\" cellpadding=\"0\" cellspacing=\"0\"><tr><td>\n");
        html.append("<div class=\"dialoghead\">");

        html.append("Login to OpenCms");

        html.append("</div><div class=\"dialogcontent\">");

        html.append("<table border=\"0\">\n");

        html.append("<form action=\"");
        html.append(getFormLink());
        html.append("\"");
        appendId(html, PARAM_FORM);
        html.append("method=\"POST\">\n");

        html.append("<tr>");
        html.append("<td style=\"white-space: nowrap;\">");

        html.append("Username");

        html.append(":</td>");
        html.append("<td style=\"width: 300px; white-space: nowrap;\">");
        html.append("<input class=\"maxwidth\" type=\"text\"");
        appendId(html, PARAM_USERNAME);
        html.append("value=\"");
        html.append(CmsStringUtil.isEmptyOrWhitespaceOnly(m_username) ? "" : m_username.trim());
        html.append("\">");
        html.append("</td>");
        html.append("</tr>\n");

        html.append("<tr>");
        html.append("<td style=\"white-space: nowrap;\">");

        html.append("Password");

        html.append(":</td>");
        html.append("<td style=\"width: 300px; white-space: nowrap;\">");
        html.append("<input class=\"maxwidth\" type=\"password\"");
        appendId(html, PARAM_PASSWORD);
        html.append(">");
        html.append("</td>");
        html.append("</tr>\n");

        html.append("<tr>");
        html.append("<td></td><td style=\"white-space: nowrap;\">\n");
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

        html.append("<input type=\"submit\" value=\"");
        html.append("Login");
        html.append("\">\n");

        html.append("</td>");
        html.append("</tr>\n");

        html.append("</form>\n");
        html.append("</table>\n");

        html.append("</div></td></tr></table>\n");
        html.append("</td></tr></table>\n");

        html.append("<div style=\"text-align: center; white-space: nowrap;\">");
        html.append("&copy; 2005 Alkacon Software GmbH. All rights reserved.");
        html.append("</div>\n");
        html.append("<div style=\"text-align: center; white-space: nowrap;\">");
        html.append("<a href=\"http://www.opencms.org\" target=\"_blank\">OpenCms</a> is free software available under the GNU LGPL license.");
        html.append("</div>\n");

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

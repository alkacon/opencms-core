/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspLoginBean.java,v $
 * Date   : $Date: 2005/05/02 16:42:04 $
 * Version: $Revision: 1.7 $
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

package org.opencms.jsp;

import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides convenient wrappers usefull to create user login pages.<p>
 * 
 * Initialize this bean at the beginning of your JSP like this:
 * <pre>
 * &lt;jsp:useBean id="cmslogin" class="org.opencms.jsp.CmsJspLoginBean"&gt;
 * &lt% cmslogin.init(pageContext, request, response); %&gt;
 * &lt;/jsp:useBean&gt;
 * </pre>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.7 $
 * 
 * @since 5.3
 */
public class CmsJspLoginBean extends CmsJspActionElement {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspLoginBean.class);

    /** Flag to indicate if a login was successful. */
    private CmsException m_loginException;

    /**
     * Empty constructor, required for every JavaBean.<p>
     */
    public CmsJspLoginBean() {

        // noop, you must call the init() method after you create an instance
    }

    /**
     * Constructor, with parameters.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsJspLoginBean(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super();
        init(context, req, res);
    }

    /**
     * Returns the link to the form that contains the login element.<p>
     * 
     * @return the link to the form that contains the login element
     */
    public String getFormLink() {

        return link(getRequestContext().getUri());
    }

    /**
     * Returns the exception that was thrown after login, 
     * or null if no Exception was thrown (i.e. login was successul
     * or not attempted).<p>
     *  
     * @return the exception thrown after login
     */
    public CmsException getLoginException() {

        return m_loginException;
    }

    /**
     * Returns the currently logged in user.<p>
     * 
     * @return the currently logged in user
     */
    public CmsUser getUser() {

        return getRequestContext().currentUser();
    }

    /**
     * Returns the username of the currently logged in user.<p>
     * 
     * @return the username of the currently logged in user
     */
    public String getUserName() {

        return getRequestContext().currentUser().getName();
    }

    /**
     * Returns true if the current user is not the guest user, 
     * i.e. if he already has logged in with some other user account.<p>
     * 
     * @return true if the current user is already logged in
     */
    public boolean isLoggedIn() {

        return !getCmsObject().getRequestContext().currentUser().isGuestUser();
    }

    /**
     * Indicates if a login was successful or not.<p>
     * 
     * @return true if the login was successful
     */
    public boolean isLoginSuccess() {

        return (m_loginException == null);
    }

    /**
     * Logs a system user in to OpenCms.<p>
     * 
     * @param username the users name
     * @param password the password
     * 
     * @throws IOException in case redirect after login was not successful
     */
    public void login(String username, String password) throws IOException {

        login(username, password, null);
    }

    /**
     * Logs a system user in to OpenCms.<p>
     * 
     * @param username the users name
     * @param password the password
     * @param login_project the project to switch to after login (if null project is not switched)
     * 
     * @throws IOException in case redirect after login was not successful
     */
    public void login(String username, String password, String login_project) throws IOException {

        login(username, password, login_project, null);
    }

    /**
     * Logs a system user in to OpenCms.<p>
     * 
     * @param username the users name
     * @param password the password
     * @param login_project the project to switch to after login (if null project is not switched)
     * @param login_redirect the URI to redirect to after login (if null the current URI is used)
     * 
     * @throws IOException in case redirect after login was not successful
     */
    public void login(String username, String password, String login_project, String login_redirect) throws IOException {

        HttpSession session = null;
        m_loginException = null;
        try {
            // login the user and create a new session
            getCmsObject().loginUser(
                username,
                password,
                getRequestContext().getRemoteAddress(),
                I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
            // make sure we have a new session after login for security reasons
            session = getRequest().getSession(false);
            if (session != null) {
                session.invalidate();
            }
            session = getRequest().getSession(true);
            if (login_project != null) {
                getCmsObject().getRequestContext().setCurrentProject(getCmsObject().readProject(login_project));
            }
        } catch (CmsException e) {
            // any exception here indicates that the login has failed
            m_loginException = e;
            if (session != null) {
                session.invalidate();
            }
        }
        if (m_loginException == null) {
            // login was successful
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().key(
                    Messages.LOG_LOGIN_SUCCESSFUL_2,
                    username,
                    getRequestContext().addSiteRoot(getRequestContext().getUri())));
            }
            if (login_redirect != null) {
                getResponse().sendRedirect(link(login_redirect));
            } else {
                getResponse().sendRedirect(getFormLink());
            }
        } else {
            // login was not successful
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(
                    Messages.LOG_LOGIN_FAILED_2,
                    username,
                    getRequestContext().addSiteRoot(getRequestContext().getUri())));
            }
        }
    }

    /**
     * Logs a user out, i.e. destroys the current users session,
     * after that the current page will be redirected it itself one time to ensure
     * the users session is truly destroyed.<p>
     * 
     * @throws IOException if redirect after logout fails
     */
    public void logout() throws IOException {

        HttpSession session = getRequest().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // logout was successful
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(
                Messages.LOG_LOGOUT_SUCCESFUL_2,
                getRequestContext().addSiteRoot(getRequestContext().getUri())));
        }
        getResponse().sendRedirect(getFormLink());
    }
}

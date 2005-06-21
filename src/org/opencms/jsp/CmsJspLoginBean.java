/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspLoginBean.java,v $
 * Date   : $Date: 2005/06/21 15:49:58 $
 * Version: $Revision: 1.14 $
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

import org.opencms.db.CmsLoginMessage;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAuthentificationException;

import java.io.IOException;
import java.util.Date;

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
 * @version $Revision: 1.14 $
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
     * @param userName the users name
     * @param password the password
     */
    public void login(String userName, String password) {

        login(userName, password, null);
    }

    /**
     * Logs a system user into OpenCms.<p>
     * 
     * Note that if a login project name is provided, this project must exist,
     * otherwise the login is regarded as a failure even if the user data was correct.<p>
     * 
     * @param userName the users name
     * @param password the password
     * @param projectName the project to switch to after login (if null project is not switched)
     */
    public void login(String userName, String password, String projectName) {

        HttpSession session = null;
        m_loginException = null;
        try {

            // login the user and create a new session
            getCmsObject().loginUser(
                userName,
                password,
                getRequestContext().getRemoteAddress(),
                CmsUser.USER_TYPE_SYSTEMUSER);

            // make sure we have a new session after login for security reasons
            session = getRequest().getSession(false);
            if (session != null) {
                session.invalidate();
            }
            session = getRequest().getSession(true);
            if (projectName != null) {
                // if this fails, the login is regarded as a failure as well
                getCmsObject().getRequestContext().setCurrentProject(getCmsObject().readProject(projectName));
            }

        } catch (CmsException e) {
            // the login has failed
            m_loginException = e;
        }
        if (m_loginException == null) {
            // login was successful
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().key(
                    Messages.LOG_LOGIN_SUCCESSFUL_3,
                    userName,
                    getRequestContext().addSiteRoot(getRequestContext().getUri()),
                    getRequestContext().getRemoteAddress()));
            }
        } else {
            // login was not successful
            if (session != null) {
                session.invalidate();
            }

            if (m_loginException instanceof CmsAuthentificationException) {
                // the authentification of the user failed

                if (org.opencms.security.Messages.ERR_LOGIN_FAILED_DISABLED_3 == m_loginException.getMessageContainer().getKey()) {

                    // the user has been disabled
                    LOG.warn(Messages.get().key(
                        Messages.LOG_LOGIN_FAILED_DISABLED_3,
                        userName,
                        getRequestContext().addSiteRoot(getRequestContext().getUri()),
                        getRequestContext().getRemoteAddress()));

                } else if (org.opencms.security.Messages.ERR_LOGIN_FAILED_TEMP_DISABLED_5 == m_loginException.getMessageContainer().getKey()) {

                    // the user has been disabled
                    LOG.warn(Messages.get().key(
                        Messages.LOG_LOGIN_FAILED_TEMP_DISABLED_5,
                        new Object[] {
                            userName,
                            getRequestContext().addSiteRoot(getRequestContext().getUri()),
                            getRequestContext().getRemoteAddress(),
                            m_loginException.getMessageContainer().getArgs()[3],
                            m_loginException.getMessageContainer().getArgs()[4]}));

                } else if (org.opencms.security.Messages.ERR_LOGIN_FAILED_NO_USER_3 == m_loginException.getMessageContainer().getKey()) {

                    // the requested user does not exist in the database
                    LOG.warn(Messages.get().key(
                        Messages.LOG_LOGIN_FAILED_NO_USER_3,
                        userName,
                        getRequestContext().addSiteRoot(getRequestContext().getUri()),
                        getRequestContext().getRemoteAddress()));

                    
                } else if (org.opencms.security.Messages.ERR_LOGIN_FAILED_WITH_MESSAGE_1 == m_loginException.getMessageContainer().getKey()) {

                    // logins have been disabled by the administration
                    long endTime = CmsLoginMessage.DEFAULT_TIME_END;
                    if (OpenCms.getLoginManager().getLoginMessage() != null) {
                        endTime = OpenCms.getLoginManager().getLoginMessage().getTimeEnd();
                    }
                    LOG.info(Messages.get().key(
                        Messages.LOG_LOGIN_FAILED_WITH_MESSAGE_4,
                        new Object[] {
                            userName,
                            getRequestContext().addSiteRoot(getRequestContext().getUri()),
                            getRequestContext().getRemoteAddress(),
                            new Date(endTime)}));
                    
                } else {

                    // the user exists, so the password must have been wrong
                    CmsMessageContainer message = Messages.get().container(
                        Messages.LOG_LOGIN_FAILED_3,
                        userName,
                        getRequestContext().addSiteRoot(getRequestContext().getUri()),
                        getRequestContext().getRemoteAddress());
                    if (userName.equalsIgnoreCase(OpenCms.getDefaultUsers().getUserAdmin())) {
                        // someone tried to log in as "Admin", log this in a higher channel
                        LOG.error(message.key());
                    } else {
                        LOG.warn(message.key());
                    }
                }
            } else {
                // the error was database related, there may be an issue with the setup 
                // write the exception to the log as well
                LOG.error(Messages.get().key(
                    Messages.LOG_LOGIN_FAILED_DB_REASON_3,
                    userName,
                    getRequestContext().addSiteRoot(getRequestContext().getUri()),
                    getRequestContext().getRemoteAddress()), m_loginException);
            }
        }
    }

    /**
     * Logs a system user in to OpenCms.<p>
     * 
     * Note that if a login project name is provided, this project must exist,
     * otherwise the login is regarded as a failure even if the user data was correct.<p>
     * 
     * @param userName the users name
     * @param password the password
     * @param projectName the project to switch to after login (if null project is not switched)
     * @param redirectUri the URI to redirect to after login (if null the current URI is used)
     * 
     * @throws IOException in case redirect after login was not successful
     */
    public void login(String userName, String password, String projectName, String redirectUri) throws IOException {

        login(userName, password, projectName);
        if (m_loginException == null) {
            if (redirectUri != null) {
                getResponse().sendRedirect(link(redirectUri));
            } else {
                getResponse().sendRedirect(getFormLink());
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
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(
                Messages.LOG_LOGOUT_SUCCESFUL_3,
                getRequestContext().currentUser().getName(),
                getRequestContext().addSiteRoot(getRequestContext().getUri()),
                getRequestContext().getRemoteAddress()));
        }
        getResponse().sendRedirect(getFormLink());
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspLoginBean.java,v $
 * Date   : $Date: 2004/03/12 16:00:49 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.I_CmsConstants;
import org.opencms.security.CmsSecurityException;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;


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
 * @version $Revision: 1.1 $
 * 
 * @since 5.3
 */
public class CmsJspLoginBean extends CmsJspBean {
    
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
     * Logs a user out, i.e. destroys the current users session,
     * after that the current page will be redirected it itself one time so that
     * the users session is truly destroyed.<p>
     * 
     * @throws IOException if redirect fails
     */
    public void logout() throws IOException {
        HttpSession session = getRequest().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        getResponse().sendRedirect(CmsJspTagLink.linkTagAction(getRequestContext().getUri(), getRequest()));
    }
    
    /**
     * Logs a system user in to OpenCms.<p>
     * 
     * @param username the users name
     * @param password the password
     * 
     * @throws CmsSecurityException in case login was not successful
     */
    public void login(String username, String password) throws CmsSecurityException {
        getCmsObject().loginUser(username, password, getRequestContext().getRemoteAddress(), I_CmsConstants.C_USER_TYPE_SYSTEMUSER);        
    }
    
    /**
     * Logs a system user in to OpenCms, 
     * but only if the given resource URI can be accessed by the logged in user.<p>
     * 
     * A user might login to OpenCms when it's password is o.k. but then have no access
     * to the requested resource since the resource has permission settings that do not
     * allow the logged in user to read it. This method first checks the login, and then 
     * also checks the permission settings on the requested resource. 
     * If the resource permissions do not allow "read" for the user, 
     * a security exception is thrown that appears to the user like a usual
     * "login failed" message. The session of the user is destroyed in this case.<p>
     * 
     * Keep in mind that the project of the user will be switched to the
     * "Online" project after login. So that is where he must have read permissions on 
     * the given resource.<p>
     * 
     * @param username the users name
     * @param password the password
     * @param resourceUri file to check for permissions
     * 
     * @throws CmsSecurityException in case login was not successful
     */    
    public void login(String username, String password, String resourceUri) throws CmsSecurityException {
        getCmsObject().loginUser(username, password, getRequestContext().getRemoteAddress(), I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        try {
            getCmsObject().readFileHeader(resourceUri);
        } catch (Throwable t) {
            // logout the user
            // logout();
            // no read permissions, thow security execption
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_LOGIN_FAILED, t);
        }
    }

}

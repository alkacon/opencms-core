/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsSessionInfo.java,v $
 * Date   : $Date: 2005/02/17 12:44:35 $
 * Version: $Revision: 1.3 $
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

package org.opencms.main;

import org.opencms.util.CmsUUID;

import javax.servlet.http.HttpSession;

/**
 * Stores needed information about a logged in user.<p>
 * 
 * This information is needed for the list of logged in users in the OpenCms backoffice and to
 * broadcast messages to the users.
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.3 $
 * 
 * @since 5.3.0
 */
public class CmsSessionInfo {
    
    private String m_userName;
    private CmsUUID m_userId;
    private Integer m_project;
    private String m_currentSite;
    private boolean m_messagePending;
    private HttpSession m_session;

    /**
     * Returns the session of the user.<p>
     * 
     * @return the session
     */
    public HttpSession getSession() {
        return m_session;
    }
    
    /**
     * Sets the session of the user.<p>
     * 
     * @param session the session to set
     */
    public void setSession(HttpSession session) {
        m_session = session;
    }
    
    /**
     * Constructor, creates a new CmsSessionInfo object.<p>
     */
    public CmsSessionInfo() {
        // empty constructor
    }

    /**
     * Returns the current site of the user.<p>
     * 
     * @return the current site
     */
    public String getCurrentSite() {
        return m_currentSite;
    }

    /**
     * Sets the current site of the user.<p>
     * 
     * @param site the current site to set
     */
    public void setCurrentSite(String site) {
        m_currentSite = site;
    }

    /**
     * Returns the id of the project of the user.<p>
     * 
     * @return the id of the project
     */
    public Integer getProject() {
        return m_project;
    }

    /**
     * Sets the id of the project of the user.<p>
     * 
     * @param project the project id to set
     */
    public void setProject(Integer project) {
        this.m_project = project;
    }

    /**
     * Returns the user name.<p>
     * 
     * @return the user name
     */
    public String getUserName() {
        return m_userName;
    }

    /**
     * Sets the user name.<p>
     * 
     * @param name the user name to set
     */
    public void setUserName(String name) {
        m_userName = name;
    }
    
    /**
     * Returns the user id.<p>
     * 
     * @return the user id
     */
    public CmsUUID getUserId() {
        return m_userId;
    }

    /**
     * Sets the user id.<p>
     * 
     * @param id the user id to set
     */
    public void setUserId(CmsUUID id) {
        m_userId = id;
    }

    /**
     * Returns if a message for the user is pending.<p>
     * 
     * @return true if a message is pending, otherwise false
     */
    public boolean getMessagePending() {
        return m_messagePending;
    }

    /**
     * Sets if a message for the user is pending.<p>
     * 
     * @param pending true if a message is pending, otherwise false
     */
    public void setMessagePending(boolean pending) {
        m_messagePending = pending;
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsSessionInfo.java,v $
 * Date   : $Date: 2005/03/06 09:26:10 $
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

package org.opencms.main;

import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.BoundedFifoBuffer;

/**
 * Stores information about a user that has authenticated himself the OpenCms security system.<p>
 * 
 * This object is used to provide information about all authenticated users in the system 
 * with the {@link org.opencms.main.CmsSessionManager}.<p> 
 * 
 * This object is available for all authenticated users after login.
 * If a user has not logged in, he may have a session on the servlet engine,
 * but he will have no session info object attached. For example the "Guest" user
 * may have multiple sessions, but no session info is created for him.<p> 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.7 $
 * 
 * @since 5.3.0
 */
public class CmsSessionInfo implements Comparable {

    /** Maximum size of the broadcast queue for one user. */
    public static final int C_QUEUE_SIZE = 10;

    /** The broadcast queue buffer for the user of this session info. */
    private Buffer m_broadcastQueue;

    /** The current site of the user. */
    private String m_currentSiteRoot;

    /** The current project id of the user. */
    private Integer m_project;

    /** The session of the user. */
    private HttpSession m_session;

    /** The time this session info was created. */
    private long m_timeCreated;

    /** The time this session info was last updated. */
    private long m_timeUpdated;

    /** The user to which this session info belongs. */
    private CmsUser m_user;

    /**
     * Creates a new CmsSessionInfo object.<p>
     * 
     * @param context the user context to create this session info for
     * @param session the http session used by the user
     */
    public CmsSessionInfo(CmsRequestContext context, HttpSession session) {

        m_timeCreated = System.currentTimeMillis();
        update(context);
        m_session = session;
        m_broadcastQueue = BufferUtils.synchronizedBuffer(new BoundedFifoBuffer(C_QUEUE_SIZE));
    }

    /**
     * Allows sorting session info according to the user names.<p>
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {

        if (!(o instanceof CmsSessionInfo)) {
            // not our type...
            return 0;
        }

        CmsSessionInfo other = (CmsSessionInfo)o;
        // compare the user names
        return m_user.getName().compareTo(other.getUser().getName());
    }

    /**
     * Returns the broadcast queue of the user to which this session info belongs.<p>
     * 
     * @return the broadcast queue of the user to which this session info belongs
     */
    public Buffer getBroadcastQueue() {

        return m_broadcastQueue;
    }

    /**
     * Returns the current site root of the user.<p>
     * 
     * @return the current site root of the user
     */
    public String getCurrentSiteRoot() {

        return m_currentSiteRoot;
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
     * Returns the session of the user.<p>
     * 
     * @return the session
     */
    public HttpSession getSession() {

        return m_session;
    }

    /**
     * Returns the time this session has been active,
     * that is the time of the last update minus the creation time.<p> 
     * 
     * @return the time this session has been active
     */
    public long getTimeActive() {

        return m_timeUpdated - m_timeCreated;
    }

    /**
     * Returns the time this session info was created.<p>
     *
     * @return the time this session info was created
     */
    public long getTimeCreated() {

        return m_timeCreated;
    }

    /**
     * Returns the time this session info was last updated.<p>
     *
     * @return the time this session info was last updated
     */
    public long getTimeUpdated() {

        return m_timeUpdated;
    }

    /**
     * Returns the user to which this session info belongs.<p>
     * 
     * @return the user to which this session info belongs
     */
    public CmsUser getUser() {

        return m_user;
    }

    /**
     * Sets the current site root of the user.<p>
     * 
     * @param site the site root path to set
     */
    public void setCurrentSiteRoot(String site) {

        m_currentSiteRoot = site;
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
     * Sets the session of the user.<p>
     * 
     * @param session the session to set
     */
    public void setSession(HttpSession session) {

        m_session = session;
    }

    /**
     * Sets the user.<p>
     * 
     * @param user the user to set
     */
    public void setUser(CmsUser user) {

        m_user = user;
    }

    /**
     * Updates the session info object with the information from
     * the given request context.<p>
     * 
     * @param context the requrest context to update the session with
     */
    public void update(CmsRequestContext context) {

        setUser(context.currentUser());
        setProject(new Integer(context.currentProject().getId()));
        setCurrentSiteRoot(context.getSiteRoot());
        m_timeUpdated = System.currentTimeMillis();
    }
}
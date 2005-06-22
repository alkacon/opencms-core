/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsSessionInfo.java,v $
 * Date   : $Date: 2005/06/22 10:38:20 $
 * Version: $Revision: 1.10 $
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
 * @author Alexander Kandzior 
 * @author Andreas Zahner 
 * @version $Revision: 1.10 $
 * 
 * @since 5.3.0
 */
public class CmsSessionInfo implements Comparable {

    /** Maximum size of the broadcast queue for one user. */
    public static final int C_QUEUE_SIZE = 10;

    /** The broadcast queue buffer for the user of this session info. */
    private Buffer m_broadcastQueue;

    /** The maximum time, in seconds, this session info is allowed to be inactive. */
    private int m_maxInactiveInterval;

    /** The current project id of the user. */
    private int m_projectId;

    /** The id of the (http) session this session info belongs to. */
    private String m_sessionId;

    /** The current site of the user. */
    private String m_siteRoot;

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
     * @param sessionId id of the (http) session this session info belongs to
     * @param maxInactiveInterval the maximum time, in seconds, this session info is allowed to be inactive
     */
    public CmsSessionInfo(CmsRequestContext context, String sessionId, int maxInactiveInterval) {

        m_timeCreated = System.currentTimeMillis();
        m_sessionId = sessionId;
        m_maxInactiveInterval = maxInactiveInterval;
        m_user = context.currentUser();
        update(context);
        m_broadcastQueue = BufferUtils.synchronizedBuffer(new BoundedFifoBuffer(C_QUEUE_SIZE));
    }

    /**
     * Allows sorting session info according to the user names.<p>
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof CmsSessionInfo)) {
            return m_user.getName().compareTo(((CmsSessionInfo)obj).getUser().getName());
        }
        return 0;
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
     * Returns the maximum time, in seconds, this session info is allowed to be inactive.<p>
     *
     * The inactive time is the time since the last call to the {@link #update(CmsRequestContext)} 
     * method. If the inactive time is greater then the maximum allowed time, this
     * session info will be removed from the session manager.<p>
     *
     * @return the maximum time, in seconds, this session info is allowed to be inactive
     * 
     * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
     */
    public int getMaxInactiveInterval() {

        return m_maxInactiveInterval;
    }

    /**
     * Returns the id of the project of the user.<p>
     * 
     * @return the id of the project
     */
    public int getProject() {

        return m_projectId;
    }

    /**
     * Returns the id of the (http) session this session info belongs to.<p>
     *
     * @return the id of the (http) session this session info belongs to
     * 
     * @see javax.servlet.http.HttpSession#getId()
     */
    public String getSessionId() {

        return m_sessionId;
    }

    /**
     * Returns the current site root of the user.<p>
     * 
     * @return the current site root of the user
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Returns the time, in miliseconds, this session has been active,
     * that is the time of the last update minus the creation time.<p> 
     * 
     * @return the time, in miliseconds, this session has been active
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
     * Returns <code>true</code> if this session info has expired, that 
     * is it has not been updated in the time set by the maximum inactivitiy interval.<p>
     *  
     * @return <code>true</code> if this session info has expired
     */
    public boolean isExpired() {

        return ((System.currentTimeMillis() - m_timeUpdated) / 1000) > m_maxInactiveInterval;
    }

    /**
     * Sets the id of the current project of the user of this session info.<p>
     * 
     * @param projectId the project id to set
     */
    public void setProject(int projectId) {

        m_projectId = projectId;
    }

    /**
     * Updates the session info object with the information from
     * the given request context.<p>
     * 
     * @param context the requrest context to update the session with
     */
    public void update(CmsRequestContext context) {

        m_timeUpdated = System.currentTimeMillis();
        m_siteRoot = context.getSiteRoot();
        setProject(context.currentProject().getId());
    }
}
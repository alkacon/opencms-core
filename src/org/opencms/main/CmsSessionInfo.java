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

package org.opencms.main;

import org.opencms.file.CmsRequestContext;
import org.opencms.util.CmsUUID;

import java.io.Serializable;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.UnboundedFifoBuffer;

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
 * @since 6.0.0
 */
public class CmsSessionInfo implements Comparable<CmsSessionInfo>, Serializable {

    /** Name of the http session attribute the OpenCms session id is stored in. */
    public static final String ATTRIBUTE_SESSION_ID = "__org.opencms.main.CmsSessionInfo#m_sessionId";

    /** Maximum size of the broadcast queue for one user. */
    public static final int QUEUE_SIZE = 10;

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 927301527031117920L;

    /** The broadcast queue buffer for the user of this session info. */
    private transient Buffer m_broadcastQueue;

    /** The maximum time, in seconds, this session info is allowed to be inactive. */
    private int m_maxInactiveInterval;

    /** The fully qualified name of the organizational unit. */
    private String m_ouFqn;

    /** The current project id of the user. */
    private CmsUUID m_projectId;

    /** The id of the (http) session this session info belongs to. */
    private CmsUUID m_sessionId;

    /** The current site of the user. */
    private String m_siteRoot;

    /** The time this session info was created. */
    private long m_timeCreated;

    /** The time this session info was last updated. */
    private long m_timeUpdated;

    /** The id of user to which this session info belongs. */
    private CmsUUID m_userId;

    /**
     * Creates a new CmsSessionInfo object.<p>
     *
     * @param context the user context to create this session info for
     * @param sessionId OpenCms id of the (http) session this session info belongs to
     * @param maxInactiveInterval the maximum time, in seconds, this session info is allowed to be inactive
     */
    public CmsSessionInfo(CmsRequestContext context, CmsUUID sessionId, int maxInactiveInterval) {

        m_timeCreated = System.currentTimeMillis();
        m_sessionId = sessionId;
        m_maxInactiveInterval = maxInactiveInterval;
        m_userId = context.getCurrentUser().getId();
        update(context);
    }

    /**
     * Allows sorting session info according to the user names.<p>
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsSessionInfo obj) {

        if (obj == this) {
            return 0;
        }
        return m_userId.compareTo(obj.getUserId());
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsSessionInfo) {
            return m_userId.equals(((CmsSessionInfo)obj).getUserId());
        }
        return false;
    }

    /**
     * Returns the broadcast queue of the user to which this session info belongs.<p>
     *
     * @return the broadcast queue of the user to which this session info belongs
     */
    public Buffer getBroadcastQueue() {

        if (m_broadcastQueue == null) {
            m_broadcastQueue = BufferUtils.synchronizedBuffer(new UnboundedFifoBuffer(QUEUE_SIZE));
        }
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
     * Returns the fully qualified name of the organizational unit for this session.<p>
     *
     * @return the fully qualified name of the organizational unit for this session
     */
    public String getOrganizationalUnitFqn() {

        return m_ouFqn;
    }

    /**
     * Returns the id of the project of the user.<p>
     *
     * @return the id of the project
     */
    public CmsUUID getProject() {

        return m_projectId;
    }

    /**
     * Returns the id of the OpenCms (http) session this session info belongs to.<p>
     *
     * @return the id of the OpenCms (http) session this session info belongs to
     *
     * @see javax.servlet.http.HttpSession#getId()
     */
    public CmsUUID getSessionId() {

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
     * Returns the time, in milliseconds, this session has been active,
     * that is the time of the last update minus the creation time.<p>
     *
     * @return the time, in milliseconds, this session has been active
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
     * Returns the id of the user to which this session info belongs.<p>
     *
     * @return the id of the user to which this session info belongs
     */
    public CmsUUID getUserId() {

        return m_userId;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_userId.hashCode();
    }

    /**
     * Returns <code>true</code> if this session info has expired, that
     * is it has not been updated in the time set by the maximum inactivity interval.<p>
     *
     * @return <code>true</code> if this session info has expired
     */
    public boolean isExpired() {

        return ((System.currentTimeMillis() - m_timeUpdated) / 1000) > m_maxInactiveInterval;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer str = new StringBuffer(64);
        str.append("[");
        str.append("sessionId: ").append(m_sessionId).append(", ");
        str.append("userId: ").append(m_userId).append(", ");
        str.append("projectId: ").append(m_projectId).append(", ");
        str.append("siteRoot: ").append(m_siteRoot).append(", ");
        str.append("timeCreated: ").append(m_timeCreated).append(", ");
        str.append("timeUpdated: ").append(m_timeUpdated).append(", ");
        str.append("maxInactiveInterval: ").append(m_maxInactiveInterval);
        str.append("ouFqn: ").append(m_ouFqn);
        str.append("]");
        return str.toString();
    }

    /**
     * Sets the id of the current project of the user of this session info.<p>
     *
     * @param projectId the project id to set
     */
    protected void setProject(CmsUUID projectId) {

        m_projectId = projectId;
    }

    /**
     * Updates the session info object with the information from
     * the given request context.<p>
     *
     * @param context the request context to update the session with
     */
    protected void update(CmsRequestContext context) {

        m_timeUpdated = System.currentTimeMillis();
        m_siteRoot = context.getSiteRoot();
        setProject(context.getCurrentProject().getUuid());
        m_ouFqn = context.getOuFqn();
    }
}

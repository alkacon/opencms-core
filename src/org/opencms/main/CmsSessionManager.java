/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsSessionManager.java,v $
 * Date   : $Date: 2006/08/19 13:40:55 $
 * Version: $Revision: 1.12.4.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.FastHashMap;
import org.apache.commons.collections.buffer.BoundedFifoBuffer;
import org.apache.commons.logging.Log;

/**
 * Keeps track of the sessions running on the OpenCms server and
 * provides a session info storage which is used to get an overview
 * about currently authenticated OpenCms users, as well as sending broadcasts between users.<p> 
 *  
 * For each authenticated OpenCms user, a {@link org.opencms.main.CmsSessionInfo} object
 * holds the information about the users status.<p>
 *
 * When a user session is invalidated, the user info will be removed.
 * This happens when a user log out, or when his session times out.<p>
 * 
 * <b>Please Note:</b> The current implementation does not provide any permission checking,
 * so all users can access the methods of this manager. Permission checking
 * based on the current users OpenCms context may be added in a future OpenCms release.<p>
 * 
 * @author Alexander Kandzior 
 *
 * @version $Revision: 1.12.4.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSessionManager {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSessionManager.class);

    /** Lock object for synchronized session count updates. */
    private Object m_lockSessionCount;

    /** Counter for the currently active sessions. */
    private int m_sessionCountCurrent;

    /** Counter for all sessions created so far. */
    private int m_sessionCountTotal;

    /** Stores the session info objects mapped to the session id. */
    private FastHashMap m_sessions;

    /**
     * Creates a new instance of the OpenCms session manager.<p>
     */
    protected CmsSessionManager() {

        // create a lock object for the session counter
        m_lockSessionCount = new Object();
        // create a map for all sessions, these will be mapped using their session id
        m_sessions = new FastHashMap();
        // set to "fast" mode (will be reset for write access)
        m_sessions.setFast(true);
    }

    /**
     * Returns the broadcast queue for the given OpenCms session id.<p>
     * 
     * @param sessionId the OpenCms session id to get the broadcast queue for
     * 
     * @return the broadcast queue for the given OpenCms session id
     */
    public Buffer getBroadcastQueue(String sessionId) {

        CmsSessionInfo sessionInfo = getSessionInfo(getSessionUUID(sessionId));
        if (sessionInfo == null) {
            // return empty message buffer if the session is gone or not available
            return BufferUtils.synchronizedBuffer(new BoundedFifoBuffer(CmsSessionInfo.QUEUE_SIZE));
        }
        return sessionInfo.getBroadcastQueue();
    }

    /**
     * Returns the number of sessions currently authenticated in the OpenCms security system.<p>
     *
     * @return the number of sessions currently authenticated in the OpenCms security system
     */
    public int getSessionCountAuthenticated() {

        return m_sessions.size();
    }

    /**
     * Returns the number of current sessions, including the sessions of not authenticated guest users.<p>
     * 
     * @return the number of current sessions, including the sessions of not authenticated guest users
     */
    public int getSessionCountCurrent() {

        return m_sessionCountCurrent;
    }

    /**
     * Returns the number of total sessions generated so far, including already destroyed sessions.<p>
     * 
     * @return the number of total sessions generated so far, including already destroyed sessions
     */
    public int getSessionCountTotal() {

        return m_sessionCountTotal;
    }

    /**
     * Returns the complete user session info of a user from the session storage,
     * or <code>null</code> if this session id has no session info attached.<p>
     *
     * @param sessionId the OpenCms session id to return the session info for
     * 
     * @return the complete user session info of a user from the session storage
     */
    public CmsSessionInfo getSessionInfo(CmsUUID sessionId) {

        return (CmsSessionInfo)m_sessions.get(sessionId);
    }

    /**
     * Returns the OpenCms user session info for the given http session, 
     * or <code>null</code> if no user session is available.<p>
     * 
     * @param session the current http session
     * 
     * @return the OpenCms user session info for the given http session, or <code>null</code> if no user session is available
     */
    public CmsSessionInfo getSessionInfo(HttpSession session) {

        if (session == null) {
            return null;
        }
        CmsUUID sessionId = (CmsUUID)session.getAttribute(CmsSessionInfo.ATTRIBUTE_SESSION_ID);
        return (sessionId == null) ? null : getSessionInfo(sessionId);
    }

    /**
     * Returns the complete user session info of a user from the session storage,
     * or <code>null</code> if this session id has no session info attached.<p>
     *
     * @param sessionId the OpenCms session id to return the session info for,
     * this must be a String representation of a {@link CmsUUID}
     * 
     * @return the complete user session info of a user from the session storage
     * 
     * @see #getSessionInfo(CmsUUID)
     */
    public CmsSessionInfo getSessionInfo(String sessionId) {

        return getSessionInfo(getSessionUUID(sessionId));
    }

    /**
     * Returns all current session info objects.<p>
     *  
     * @return all current session info objects
     */
    public List getSessionInfos() {

        List result = new ArrayList();
        Iterator i = m_sessions.keySet().iterator();
        while (i.hasNext()) {
            CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(i.next());
            if (sessionInfo != null) {
                // may be the case in case of concurrent modification
                result.add(sessionInfo);
            }
        }
        return result;
    }

    /**
     * Returns a list of all active session info objects for the specified user.<p>
     * 
     * An OpenCms user can have many active sessions. 
     * This is e.g. possible when two people have logged in to the system using the
     * same username. Even one person can have multiple sessions if he
     * is logged in to OpenCms with several browser windows at the same time.<p>
     * 
     * @param userId the id of the user
     *  
     * @return a list of all active session info objects for the specified user
     */
    public List getSessionInfos(CmsUUID userId) {

        List userSessions = new ArrayList();
        Iterator i = m_sessions.keySet().iterator();
        while (i.hasNext()) {
            CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(i.next());
            if (userId.equals(sessionInfo.getUser().getId())) {
                userSessions.add(sessionInfo);
            }
        }
        return userSessions;
    }

    /**
     * Sends a broadcast to all sessions of all currently authenticated users.<p>
     * 
     * @param cms the OpenCms user context of the user sending the broadcast
     * 
     * @param message the message to broadcast
     */
    public void sendBroadcast(CmsObject cms, String message) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(message)) {
            // don't broadcast empty messages
            return;
        }
        // create the broadcast
        CmsBroadcast broadcast = new CmsBroadcast(cms.getRequestContext().currentUser(), message);
        // send the broadcast to all authenticated sessions
        Iterator i = m_sessions.keySet().iterator();
        while (i.hasNext()) {
            CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(i.next());
            if (sessionInfo != null) {
                // double check for concurrent modification
                sessionInfo.getBroadcastQueue().add(broadcast);
            }
        }
    }

    /**
     * Sends a broadcast to all sessions of a given user.<p>
     * 
     * @param cms the OpenCms user context of the user sending the broadcast
     * 
     * @param message the message to broadcast
     * @param user the target (reciever) of the broadcast
     */
    public void sendBroadcast(CmsObject cms, String message, CmsUser user) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(message)) {
            // don't broadcast empty messages
            return;
        }
        // create the broadcast
        CmsBroadcast broadcast = new CmsBroadcast(cms.getRequestContext().currentUser(), message);
        List userSessions = getSessionInfos(user.getId());
        Iterator i = userSessions.iterator();
        // send the broadcast to all sessions of the selected user
        while (i.hasNext()) {
            CmsSessionInfo sessionInfo = (CmsSessionInfo)i.next();
            sessionInfo.getBroadcastQueue().add(broadcast);
        }
    }

    /**
     * Sends a broadcast to the specified user session.<p>
     * 
     * @param cms the OpenCms user context of the user sending the broadcast
     * 
     * @param message the message to broadcast
     * @param sessionId the OpenCms session id target (reciever) of the broadcast
     */
    public void sendBroadcast(CmsObject cms, String message, String sessionId) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(message)) {
            // don't broadcast empty messages
            return;
        }
        // send the broadcast only to the selected session
        CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(getSessionUUID(sessionId));
        if (sessionInfo != null) {
            // double check for concurrent modification
            sessionInfo.getBroadcastQueue().add(new CmsBroadcast(cms.getRequestContext().currentUser(), message));
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer output = new StringBuffer();
        Iterator i = m_sessions.keySet().iterator();
        output.append("[CmsSessions]:\n");
        while (i.hasNext()) {
            CmsUUID key = (CmsUUID)i.next();
            CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(key);
            output.append(key.toString());
            output.append(" : ");
            output.append(sessionInfo.getUser().toString());
            output.append('\n');
        }
        return output.toString();
    }

    /**
     * Adds a new session info into the session storage.<p>
     *
     * @param sessionInfo the session info to store for the id
     */
    protected void addSessionInfo(CmsSessionInfo sessionInfo) {

        m_sessions.put(sessionInfo.getSessionId(), sessionInfo);
    }

    /**
     * Returns the OpenCms user session info for the given request, 
     * or <code>null</code> if no user session is available.<p>
     * 
     * @param req the current request
     * 
     * @return the OpenCms user session info for the given request, or <code>null</code> if no user session is available
     */
    protected CmsSessionInfo getSessionInfo(HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session == null) {
            // special case for acessing a session from "outside" requests (e.g. upload applet)
            String sessionId = req.getHeader(CmsRequestUtil.HEADER_JSESSIONID);
            return sessionId == null ? null : getSessionInfo(sessionId);
        }
        return getSessionInfo(session);
    }

    /**
     * Returns the UUID representation for the given session id String.<p>
     * 
     * @param sessionId the session id String to return the  UUID representation for
     * 
     * @return the UUID representation for the given session id String
     */
    protected CmsUUID getSessionUUID(String sessionId) {

        return new CmsUUID(sessionId);
    }

    /**
     * Called by the {@link OpenCmsListener} when a http session is created.<p>
     * 
     * @param event the http session event
     * 
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     * @see OpenCmsListener#sessionCreated(HttpSessionEvent)
     */
    protected void sessionCreated(HttpSessionEvent event) {

        synchronized (m_lockSessionCount) {
            m_sessionCountCurrent = (m_sessionCountCurrent <= 0) ? 1 : (m_sessionCountCurrent + 1);
            m_sessionCountTotal++;
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(
                    Messages.LOG_SESSION_CREATED_2,
                    new Integer(m_sessionCountTotal),
                    new Integer(m_sessionCountCurrent)));
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SESSION_CREATED_1, event.getSession().getId()));
        }
    }

    /**
     * Called by the {@link OpenCmsListener} when a http session is destroyed.<p>
     * 
     * @param event the http session event
     * 
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     * @see OpenCmsListener#sessionDestroyed(HttpSessionEvent)
     */
    protected void sessionDestroyed(HttpSessionEvent event) {

        synchronized (m_lockSessionCount) {
            m_sessionCountCurrent = (m_sessionCountCurrent <= 0) ? 0 : (m_sessionCountCurrent - 1);
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(
                    Messages.LOG_SESSION_DESTROYED_2,
                    new Integer(m_sessionCountTotal),
                    new Integer(m_sessionCountCurrent)));
            }
        }

        CmsSessionInfo sessionInfo = getSessionInfo(event.getSession());
        CmsUser user = null;
        if (sessionInfo != null) {
            user = sessionInfo.getUser();
            m_sessions.remove(sessionInfo.getSessionId());
        }

        if ((user != null) && (getSessionInfos(user.getId()).size() == 0)) {
            // remove the temporary locks of this user from memory
            OpenCms.getLockManager().removeTempLocks(user);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SESSION_DESTROYED_1, event.getSession().getId()));
        }
    }

    /**
     * Updates the the OpenCms session data used for quick authentication of users.<p>
     *
     * This is required if the user data (current group or project) was changed in
     * the requested document.<p>
     *
     * The user data is only updated if the user was authenticated to the system.
     *
     * @param cms the current OpenCms user context
     * @param req the current request
     */
    protected void updateSessionInfo(CmsObject cms, HttpServletRequest req) {

        if (!cms.getRequestContext().isUpdateSessionEnabled()) {
            // this request must not update the user session info
            // this is true for long running "thread" requests, e.g. during project publish
            return;
        }

        if (!cms.getRequestContext().currentUser().isGuestUser()) {
            // Guest user requests don't need to update the OpenCms user session information

            // get the session info object for the user
            CmsSessionInfo sessionInfo = getSessionInfo(req);
            if (sessionInfo != null) {
                // update the users session information
                sessionInfo.update(cms.getRequestContext());
            } else {
                HttpSession session = req.getSession(false);
                // only create session info if a session is already available 
                if (session != null) {
                    // create a new session info for the user
                    sessionInfo = new CmsSessionInfo(
                        cms.getRequestContext(),
                        new CmsUUID(),
                        session.getMaxInactiveInterval());
                    // append the session info to the http session
                    session.setAttribute(CmsSessionInfo.ATTRIBUTE_SESSION_ID, sessionInfo.getSessionId().clone());
                    // update the session info user data
                    addSessionInfo(sessionInfo);
                }
            }
        }
    }

    /**
     * Validates the sessions stored in this manager and removes 
     * any sessions that have become invalidated.<p>
     */
    protected void validateSessionInfos() {

        try {
            // change session map to full synchronization or "slow" mode
            m_sessions.setFast(false);
            Iterator i = m_sessions.keySet().iterator();
            while (i.hasNext()) {
                CmsUUID sessionId = (CmsUUID)i.next();
                CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(sessionId);
                if (sessionInfo != null) {
                    // may be the case in case of concurrent modification
                    if (sessionInfo.isExpired()) {
                        // session is invalid, try to remove it
                        try {
                            m_sessions.remove(sessionId);
                        } catch (ConcurrentModificationException ex) {
                            // ignore, better luck next time...
                        }
                    }
                }
            }
        } finally {
            // reset session map to "fast" mode
            m_sessions.setFast(true);
        }
    }
}
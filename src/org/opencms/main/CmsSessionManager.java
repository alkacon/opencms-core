/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsSessionManager.java,v $
 * Date   : $Date: 2005/06/22 10:38:20 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSessionEvent;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
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
 * @version $Revision: 1.7 $
 * @since 5.1
 */
public class CmsSessionManager {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSessionManager.class);

    /** Counter for the currently active sessions. */
    private int m_sessionCountCurrent;

    /** Counter for all sessions created so far. */
    private int m_sessionCountTotal;

    /** Stores the session info objects mapped to the session id. */
    private Map m_sessions;

    /**
     * Creates a new instance of the OpenCms session manager.<p>
     */
    protected CmsSessionManager() {

        super();
        // create a map for all sessions, these will be mapped using their session id
        m_sessions = Collections.synchronizedMap(new HashMap());
    }

    /**
     * Returns the broadcast queue for the given session id.<p>
     * 
     * @param sessionId the session id to get the broadcast queue for
     * 
     * @return the broadcast queue for the given session id
     */
    public Buffer getBroadcastQueue(String sessionId) {

        if (getSessionInfo(sessionId) == null) {
            // return empty message buffer if the session is gone
            return BufferUtils.synchronizedBuffer(new BoundedFifoBuffer(CmsSessionInfo.C_QUEUE_SIZE));
        }
        return getSessionInfo(sessionId).getBroadcastQueue();
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
     * @param sessionId the session id to return the session info for
     * 
     * @return the complete user session info of a user from the session storage
     */
    public CmsSessionInfo getSessionInfo(String sessionId) {

        return (CmsSessionInfo)m_sessions.get(sessionId);
    }

    /**
     * Returns all current session info objects.<p>
     *  
     * @return all current session info objects
     */
    public List getSessionInfos() {

        List result = new ArrayList();
        synchronized (m_sessions) {
            Iterator i = getConcurrentSessionIterator();
            while (i.hasNext()) {
                CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(i.next());
                if (sessionInfo != null) {
                    // may be the case in case of concurrent modification
                    result.add(sessionInfo);
                }
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
        synchronized (m_sessions) {
            Iterator i = getConcurrentSessionIterator();
            while (i.hasNext()) {
                String key = (String)i.next();
                CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(key);
                if (userId.equals(sessionInfo.getUser().getId())) {
                    userSessions.add(sessionInfo);
                }
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
        synchronized (m_sessions) {
            Iterator i = getConcurrentSessionIterator();
            while (i.hasNext()) {
                CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(i.next());
                if (sessionInfo != null) {
                    // double check for concurrent modification
                    sessionInfo.getBroadcastQueue().add(broadcast);
                }
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
        synchronized (m_sessions) {
            while (i.hasNext()) {
                CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(i.next());
                if (sessionInfo != null) {
                    // double check for concurrent modification
                    sessionInfo.getBroadcastQueue().add(broadcast);
                }
            }
        }
    }

    /**
     * Sends a broadcast to the specified user session.<p>
     * 
     * @param cms the OpenCms user context of the user sending the broadcast
     * 
     * @param message the message to broadcast
     * @param sessionId the session id target (reciever) of the broadcast
     */
    public void sendBroadcast(CmsObject cms, String message, String sessionId) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(message)) {
            // don't broadcast empty messages
            return;
        }
        // send the broadcast only to the selected session
        CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(sessionId);
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
        synchronized (m_sessions) {
            Iterator i = getConcurrentSessionIterator();
            output.append("[CmsSessions]:\n");
            while (i.hasNext()) {
                String key = (String)i.next();
                CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(key);
                output.append(key);
                output.append(" : ");
                output.append(sessionInfo.getUser().toString());
                output.append('\n');
            }
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
     * Called by the {@link OpenCmsListener} when a http session is created.<p>
     * 
     * @param event the http session event
     * 
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     * @see OpenCmsListener#sessionCreated(HttpSessionEvent)
     */
    protected synchronized void sessionCreated(HttpSessionEvent event) {

        m_sessionCountCurrent = (m_sessionCountCurrent <= 0) ? 1 : (m_sessionCountCurrent + 1);
        m_sessionCountTotal++;
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(
                Messages.LOG_SESSION_CREATED_2,
                new Integer(m_sessionCountTotal),
                new Integer(m_sessionCountCurrent)));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SESSION_CREATED_1, event.getSession().getId()));
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
    protected synchronized void sessionDestroyed(HttpSessionEvent event) {

        m_sessionCountCurrent = (m_sessionCountCurrent <= 0) ? 0 : (m_sessionCountCurrent - 1);

        String sessionId = event.getSession().getId();
        // remove the session for the session info storage
        CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(sessionId);
        CmsUUID userId = CmsUUID.getNullUUID();
        if (sessionInfo != null) {
            userId = sessionInfo.getUser().getId();
        }
        synchronized (m_sessions) {
            m_sessions.remove(sessionId);
        }
        if (!userId.isNullUUID() && getSessionInfos(userId).size() == 0) {
            // remove the temporary locks of this user from memory
            OpenCms.getLockManager().removeTempLocks(userId);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(
                Messages.LOG_SESSION_DESTROYED_2,
                new Integer(m_sessionCountTotal),
                new Integer(m_sessionCountCurrent)));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_SESSION_DESTROYED_1, event.getSession().getId()));
        }
    }

    /**
     * Validates the sessions stored in this manager and removes 
     * any sessions that have become invalidated.<p>
     */
    protected void validateSessionInfos() {

        synchronized (m_sessions) {
            Iterator i = getConcurrentSessionIterator();
            while (i.hasNext()) {
                String sessionId = (String)i.next();
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
        }
    }

    /**
     * Returns an iterator of a copy of the keyset of the current session map,
     * which should be used for iterators to avoid concurrent modification exceptions.<p>
     * 
     * @return an iterator of the keyset of the current session map 
     */
    private Iterator getConcurrentSessionIterator() {

        Set keySet;
        int count = 0;
        do {
            keySet = new HashSet(m_sessions.size());
            try {
                keySet.addAll(m_sessions.keySet());
            } catch (ConcurrentModificationException e) {
                // problem creating a copy of the keyset, try up to 5 times 
                count++;
                keySet = null;
            }
        } while ((keySet == null) && (count < 5));
        if (keySet == null) {
            // no success, so we return an empty set to avoid problems
            keySet = new HashSet();
        }
        return keySet.iterator();
    }
}
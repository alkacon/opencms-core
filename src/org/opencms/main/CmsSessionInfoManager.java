/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/Attic/CmsSessionInfoManager.java,v $
 * Date   : $Date: 2005/03/04 15:11:32 $
 * Version: $Revision: 1.9 $
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

/**
 * This class implements a session info storage which is used to get an overview
 * about currently logged in OpenCms users.<p> 
 *
 * There can be multiple sessions available for a user, since a user can login more then once.<p>
 *  
 * For each active user session, the current project of the user 
 * and other additional information are stored in a hashtable, using the session 
 * id as key to them.<p>
 *
 * When a user session is invalidated, the user will also be removed from the storage.<p>
 *
 * <b>Please Note:</b> The current implementation does not provide any permission checking,
 * so all users can all these methodss. Permission checking
 * based on the current users OpenCms context may be added in a future OpenCms release.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.9 $ 
 */
public class CmsSessionInfoManager {

    /**
     * Hashtable storage to store all active users.
     */
    private Map m_sessions;

    /**
     * Constructor, creates a new CmsSessionInfoManager object.
     */
    public CmsSessionInfoManager() {

        m_sessions = Collections.synchronizedMap(new HashMap());
    }

    /**
     * Adds a new session info into the session storage.<p>
     *
     * @param sessionId the session id to store the session info for
     * @param sessionInfo the session info to store for the id
     */
    public void addSessionInfo(String sessionId, CmsSessionInfo sessionInfo) {

        m_sessions.put(sessionId, sessionInfo);
    }

    /**
     * Returns the broadcast message queue for the given session id.<p>
     * 
     * @param sessionId the session id to store the session info for
     * 
     * @return the broadcast message queue for the given session id
     */
    public CmsBroadcastMessageQueue getBroadcastMessageQueue(String sessionId) {

        return getSessionInfo(sessionId).getBroadcastMessageQueue();
    }

    /**
     * Returns the current project of a user from the session storage.<p>
     *
     * @param sessionId the users session id
     * 
     * @return the current project of a user from the session storage
     */
    public Integer getCurrentProject(String sessionId) {

        Integer currentProject = null;
        CmsSessionInfo userinfo = getSessionInfo(sessionId);
        // this user does exist, so get his current project
        if (userinfo != null) {
            currentProject = userinfo.getProject();
        }
        if (currentProject == null) {
            return new Integer(I_CmsConstants.C_PROJECT_ONLINE_ID);
        } else {
            return currentProject;
        }
    }

    /**
     * Returns the current site of a user from the session storage.<p>
     *
     * @param sessionId the users session id
     * 
     * @return the current site of a user from the session storage
     */
    public String getCurrentSite(String sessionId) {

        String currentSite = null;
        CmsSessionInfo userinfo = getSessionInfo(sessionId);
        // this user does exist, so get his current site
        if (userinfo != null) {
            currentSite = userinfo.getCurrentSite();
        }
        if (currentSite == null) {
            return OpenCms.getSiteManager().getDefaultSite().getSiteRoot();
        } else {
            return currentSite;
        }
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
     * @return the message queue for the given session id
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
    public List getSessionInfosForUser(CmsUUID userId) {

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
     * Returns the user to whom the given session id belongs,
     * or <code>null</code> if that user is not in the session storage.<p>
     *
     * @param sessionId the users session id
     * @return the user to whom the given session id belongs
     */
    public CmsUser getUser(String sessionId) {

        CmsUser user = null;
        CmsSessionInfo sessionInfo = getSessionInfo(sessionId);
        if (sessionInfo != null) {
            // this session exists and is still valid
            user = sessionInfo.getUser();
        }
        return user;
    }

    /**
     * Returns <code>true</code> if there are pending broadcast messages for the given session id.<p>
     * 
     * @param sessionId the session id to get the messages for
     *  
     * @return <code>true</code> if there are pending broadcast messages for the given session id
     */
    public boolean hasBroadcastMessagesPending(String sessionId) {

        CmsBroadcastMessageQueue queue = getSessionInfo(sessionId).getBroadcastMessageQueue();
        return queue.hasBroadcastMessagesPending();
    }

    /**
     * Removes a session info from the session storage.<p>
     *
     * This should only be called when the session is invalidated.<p>
     *
     * @param sessionId the session id to remove the session info for
     */
    public void removeSessionInfo(String sessionId) {

        CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(sessionId);
        CmsUUID userId = CmsUUID.getNullUUID();
        if (sessionInfo != null) {
            userId = sessionInfo.getUser().getId();
        }
        synchronized (m_sessions) {
            m_sessions.remove(sessionId);
        }
        if (!userId.isNullUUID() && getSessionInfosForUser(userId).size() == 0) {
            // remove the temporary locks of this user from memory
            OpenCms.getLockManager().removeTempLocks(userId);
        }
    }

    /**
     * Broadcast a message to all logged in users.<p>
     * 
     * @param cms the OpenCms user context of the user sending the message
     * 
     * @param message the message to be send
     */
    public void sendBroadcastMessage(CmsObject cms, String message) {

        synchronized (m_sessions) {
            Iterator i = getConcurrentSessionIterator();
            long sendTime = System.currentTimeMillis();
            while (i.hasNext()) {
                CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(i.next());
                sessionInfo.getBroadcastMessageQueue().addBroadcastMessage(
                    new CmsBroadcastMessage(cms.getRequestContext().currentUser(), message, sendTime));
            }
        }
    }

    /**
     * Returns the number of currently active sessions that have a session info attached.<p>
     *
     * This should be identical with the number of user that have currently beem 
     * logged into the OpenCms system.<p>
     *
     * @return the number of currently active sessions that have a session info attached
     */
    public int size() {

        return m_sessions.size();
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
     * Validates the sessions stored in this manager and removes 
     * any sessions that have become invalidated.<p>
     */
    public void validateSessionInfos() {

        synchronized (m_sessions) {
            Iterator i = getConcurrentSessionIterator();
            while (i.hasNext()) {
                String sessionId = (String)i.next();
                CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(sessionId);
                if (sessionInfo != null) {
                    // may be the case in case of concurrent modification
                    try {
                        // access the session, this will lead to an exception for invalid sessions
                        sessionInfo.getSession().getLastAccessedTime();
                    } catch (java.lang.IllegalStateException e) {
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
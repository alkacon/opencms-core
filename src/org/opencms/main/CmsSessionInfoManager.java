/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/Attic/CmsSessionInfoManager.java,v $
 * Date   : $Date: 2004/12/23 10:32:03 $
 * Version: $Revision: 1.5 $
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

package org.opencms.main;


import org.opencms.file.CmsObject;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpSession;

/**
 * This class implements a session info storage which is mainly used to get an overview
 * about currently logged in OpenCms users.<p> 
 * 
 * For each active user session, the current project of the user 
 * and other additional information are stored in a hashtable, using the session 
 * Id as key to them.<p>
 *
 * When a user session is destroyed, the user will also be removed from the storage.<p>
 *
 * One of the main purposes of this stored user session list is the 
 * <code>sendBroadcastMessage()</code> method.
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.5 $ 
 * 
 * @see #sendBroadcastMessage(String message)
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
     * Removes a user session from the session storage,
     * this is done when the session of the user is destroyed.<p>
     *
     * @param sessionId the users session id
     */
    public void removeUserSession(String sessionId) {
        CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(sessionId);
        CmsUUID userId = CmsUUID.getNullUUID();
        if (sessionInfo != null) {
            userId = sessionInfo.getUserId();
        }
        m_sessions.remove(sessionId);
        if (!userId.isNullUUID() && getUserSessions(userId).size() == 0) {
            // remove the temporary locks of this user from memory
            OpenCms.getLockManager().removeTempLocks(userId);
            
        }
    }
    
    /**
     * Returns a list of all active CmsSessionInfo objects for the specified user id.<p>
     * 
     * An OpenCms user can have many active sessions. 
     * This is e.g. possible when two people have logged in to the system using the
     * same username. Even one person can have multiple sessions if he
     * is logged in to OpenCms with several browser windows at the same time.<p>
     * 
     * @param userId the id of the user
     * @return the list of all active CmsSessionInfo objects
     */
    public List getUserSessions(CmsUUID userId) {
        List userSessions = new ArrayList();
        synchronized (m_sessions) {
            Iterator i = m_sessions.keySet().iterator();
            while (i.hasNext()) {
                String key = (String)i.next();
                CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(key);
                if (userId.equals(sessionInfo.getUserId())) {
                    userSessions.add(sessionInfo);
                }
            }
        }
        return userSessions;
    }

    /**
     * Returns the current project of a user from the session storage.<p>
     *
     * @param sessionId the users session id
     * @return the current project of a user from the session storage
     */
    public Integer getCurrentProject(String sessionId) {
        Integer currentProject = null;
        CmsSessionInfo userinfo = getUserInfo(sessionId);
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
     * @return the current site of a user from the session storage
     */
    public String getCurrentSite(String sessionId) {
        String currentSite = null;
        CmsSessionInfo userinfo = getUserInfo(sessionId);
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
     * Returns the name of a user from the session storage,
     * or <code>null</code> if that user is not in the session storage.<p>
     *
     * @param sessionId the users session id
     * @return the name of a user from the session storage
     */
    public String getUserName(String sessionId) {
        String username = null;
        CmsSessionInfo userinfo = getUserInfo(sessionId);
        // this user does exist, so get his name.
        if (userinfo != null) {
            username = userinfo.getUserName();
        }
        return username;
    }

    /**
     * Gets the complete user information of a user from the session storage.
     *
     * @param sessionId A currently valid session id
     * @return table with user information or null
     */
    public CmsSessionInfo getUserInfo(String sessionId) {
        return (CmsSessionInfo)m_sessions.get(sessionId);
    }

    /**
     * Puts a new user into the session storage.<p>
     * 
     * This method also stores additional user information in a CmsSessionInfo object.<p>
     * 
     * A user is stored with its current session id after a positive authentification.<p>
     *
     * @param sessionId a currently valid session id
     * @param sessionInfo a CmsSessionInfo object containing information (e.g. the name) about the user
     */
    public void putUser(String sessionId, CmsSessionInfo sessionInfo) {
        m_sessions.put(sessionId, sessionInfo);
    }

    /**
     * Returns the number of current core sessions in the system.<p>
     *
     * @return the number of current core sessions in the system
     */
    public int size() {
        return m_sessions.size();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer output = new StringBuffer();
        String key;
        CmsSessionInfo sessionInfo;
        String name;
        synchronized (m_sessions) {
            Iterator i = m_sessions.keySet().iterator();
            output.append("[CmsSessions]:\n");
            while (i.hasNext()) {
                key = (String)i.next();
                output.append(key + " : ");
                sessionInfo = (CmsSessionInfo)m_sessions.get(key);
                name = sessionInfo.getUserName();
                output.append(name + "\n");
            }
        }
        return output.toString();
    }

    /**
     * Returns a Vector with all currently logged in users.<p>
     * 
     * The Vector elements are <code>Hashtables</code> with the users name, 
     * the current project, the current group a Boolean if current messages
     * are pending.<p>
     *
     * @return a list with all currently logged in users
     */
    public List getLoggedInUsers() {
        List output = new Vector();
        String key;
        CmsSessionInfo sessionInfo;

        // Hastable to return in the vector for one user-entry
        Hashtable userentry;

        synchronized (m_sessions) {
            Iterator i = m_sessions.keySet().iterator();
            while (i.hasNext()) {
                userentry = new Hashtable(4);
                key = (String)i.next();
                sessionInfo = (CmsSessionInfo)m_sessions.get(key);
                userentry.put(I_CmsConstants.C_SESSION_USERNAME, sessionInfo.getUserName());
                userentry.put(I_CmsConstants.C_SESSION_PROJECT, sessionInfo.getProject());
                userentry.put(I_CmsConstants.C_SESSION_MESSAGEPENDING, new Boolean(sessionInfo.getSession().getAttribute(I_CmsConstants.C_SESSION_BROADCASTMESSAGE) != null));    
                output.add(userentry);
            }
        }
        return output;
    }

    /**
     * Broadcasts a message to all logged in users.<p>
     * 
     * @param message the message to broadcast
     */
    public void sendBroadcastMessage(String message) {
        String key;
        CmsSessionInfo sessionInfo;

        HttpSession user_session;
        String session_message;
        
        synchronized (m_sessions) {
            Iterator i = m_sessions.keySet().iterator();
            while (i.hasNext()) {
                key = (String)i.next();
                sessionInfo = (CmsSessionInfo)m_sessions.get(key);
                user_session = sessionInfo.getSession();
                session_message = (String)user_session.getAttribute(I_CmsConstants.C_SESSION_BROADCASTMESSAGE);
                if (session_message == null) {
                    session_message = "";
                }
                session_message += message;
                user_session.setAttribute(I_CmsConstants.C_SESSION_BROADCASTMESSAGE, session_message);
            }
        }
    }

    /**
     * Send a broadcast message to all currently logged in users.<p>
     * 
     * @param cms the context info for security checks. 
     * @param message the message to send
     * 
     * @throws CmsException if something goes wrong
     */
    public void sendBroadcastMessage(CmsObject cms, String message) throws CmsException {

        if (cms.isAdmin()) {
            sendBroadcastMessage(message);
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] sendBroadcastMessage()",
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Returns a list of all currently logged in users.<p>
     * 
     * The returned list is a list of <code>Map</code>s, 
     * with some basic information about each user, like:<br>
     * <ul>
     * <li>The user name, at key <code>{@link I_CmsConstants#C_SESSION_USERNAME}</code></li>
     * <li>The current project for that user, at key 
     *      <code>{@link I_CmsConstants#C_SESSION_PROJECT}</code></li>
     * </ul><p>
     * 
     * @param cms the context info for security checks. 
     * 
     * @return a list of <code>{@link Map}</code>s representing 
     *          users that are currently logged in.
     * 
     * @throws CmsException if something goes wrong
     */
    public List getLoggedInUsers(CmsObject cms) throws CmsException {

        if (cms.isAdmin()) {
            return getLoggedInUsers();
        } else {
            throw new CmsSecurityException(
                "[" + this.getClass().getName() + "] getLoggedInUsers()",
                CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

}

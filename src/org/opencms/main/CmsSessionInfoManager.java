/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/Attic/CmsSessionInfoManager.java,v $
 * Date   : $Date: 2004/01/07 16:53:02 $
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

package org.opencms.main;

import com.opencms.core.I_CmsConstants;

import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This class implements a session info storage which is mainly used to count the
 * currently logged in OpenCms users.<p> 
 * 
 * It is required for user authentification of OpenCms. 
 * For each active user, its name and other additional information 
 * (like the current user group) are stored in a hashtable, using the session 
 * Id as key to them.<p>
 *
 * When the session gets destroyed, the user will removed from the storage.<p>
 *
 * One of the main purposes of this stored user session list is the 
 * <code>sendBroadcastMessage()</code> method.
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $ 
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
     * Removes a user from the session storage,
     * this is done when the session of the user is destroyed.<p>
     *
     * @param sessionId the users session id
     */
    public void deleteUser(String sessionId) {
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
     * @param sessionId A currently valid session id.
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
            output.append("[CmsCoreSessions]:\n");
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
     * @return a Vector with all currently logged in users
     */
    public Vector getLoggedInUsers() {
        Vector output = new Vector();
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
                // userentry.put(I_CmsConstants.C_SESSION_CURRENTGROUP, value.get(I_CmsConstants.C_SESSION_CURRENTGROUP));
                userentry.put(I_CmsConstants.C_SESSION_MESSAGEPENDING, new Boolean((sessionInfo.getSessionData()).containsKey(I_CmsConstants.C_SESSION_BROADCASTMESSAGE)));
    
                output.addElement(userentry);
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

        Hashtable session_data;
        String session_message;
        
        synchronized (m_sessions) {
            Iterator i = m_sessions.keySet().iterator();
            while (i.hasNext()) {
                key = (String)i.next();
                sessionInfo = (CmsSessionInfo)m_sessions.get(key);
                session_data = sessionInfo.getSessionData();
                session_message = (String)session_data.get(I_CmsConstants.C_SESSION_BROADCASTMESSAGE);
                if (session_message == null) {
                    session_message = "";
                }
                session_message += message;
                session_data.put(I_CmsConstants.C_SESSION_BROADCASTMESSAGE, session_message);
            }
        }
    }
}

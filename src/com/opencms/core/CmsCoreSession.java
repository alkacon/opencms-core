/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsCoreSession.java,v $
 * Date   : $Date: 2003/08/01 15:42:18 $
 * Version: $Revision: 1.16 $
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

package com.opencms.core;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class implements a session storage which is mainly used to count the
 * currently logged in OpenCms users.<p> 
 * 
 * It is required for user authentification of OpenCms. 
 * For each active user, its name and other additional information 
 * (like the current user group) are stored in a hashtable, useing the session 
 * Id as key to them.<p>
 *
 * When the session gets destroyed, the user will removed from the storage.<p>
 *
 * One of the main purposes of this stored user session list is the 
 * <code>sendBroadcastMessage()</code> method.
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.16 $ 
 * 
 * @see #sendBroadcastMessage(String message)
 */
public class CmsCoreSession {

    /**
     * Hashtable storage to store all active users.
     */
    private Hashtable m_sessions;

    /**
     * Constructor, creates a new CmsCoreSession object.
     */
    public CmsCoreSession() {
        m_sessions = new Hashtable();
    }

    /**
     * Removes a user from the session storage,
     * this is done when the session of the user is destroyed.<p>
     *
     * @param sessionId the users session id
     */
    public void deleteUser(String sessionId) {
        m_sessions.remove(sessionId);
    }

    /**
     * Returns the current project of a user from the session storage.<p>
     *
     * @param sessionId the users session id
     * @return the current project of a user from the session storage
     */
    public Integer getCurrentProject(String sessionId) {
        Integer currentProject = null;
        Hashtable userinfo = getUser(sessionId);
        // this user does exist, so get his current project
        if (userinfo != null) {
            currentProject = (Integer)userinfo.get(I_CmsConstants.C_SESSION_PROJECT);
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
        Hashtable userinfo = getUser(sessionId);
        // this user does exist, so get his current site
        if (userinfo != null) {
            currentSite = (String)userinfo.get(I_CmsConstants.C_SESSION_CURRENTSITE);
        }
        if (currentSite == null) {
            return A_OpenCms.getSiteManager().getDefaultSite().getSiteRoot();
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
        Hashtable userinfo = getUser(sessionId);
        // this user does exist, so get his name.
        if (userinfo != null) {
            username = (String)userinfo.get(I_CmsConstants.C_SESSION_USERNAME);
        }
        return username;
    }

    /**
     * Gets the complete user information of a user from the session storage.
     *
     * @param sessionId A currently valid session id.
     * @return table with user information or null
     */
    public Hashtable getUser(String sessionId) {
        return (Hashtable)m_sessions.get(sessionId);
    }

    /**
     * Puts a new user into the sesstion storage.<p>
     * 
     * A user is stored with its current
     * session id after a positive authentification.<p>
     *
     * @param sessionId  a currently valid session id
     * @param username the name of the user to be stored
     */
    public void putUser(String sessionId, String username) {
        Hashtable userinfo = new Hashtable();
        userinfo.put(I_CmsConstants.C_SESSION_USERNAME, username);
        putUser(sessionId, userinfo);
    }

    /**
     * Puts a new user into the sesstion storage.<p>
     * 
     * A user is stored with its current
     * session id after a positive authentification.<p>
     *
     * @param sessionId  a currently valid session id
     * @param username the name of the user to be stored
     * @param group the name of the users current group
     * @param project the id of the users current project
     */
    public void putUser(String sessionId, String username, String group, Integer project) {
        Hashtable userinfo = new Hashtable();
        userinfo.put(I_CmsConstants.C_SESSION_USERNAME, username);
        userinfo.put(I_CmsConstants.C_SESSION_CURRENTGROUP, group);
        userinfo.put(I_CmsConstants.C_SESSION_PROJECT, project);
        putUser(sessionId, userinfo);
    }

    /**
     * Puts a new user into the sesstion storage, 
     * this method also stores a complete hashtable with additional 
     * user information.<p>
     * 
     * A user is stored with its current
     * session id after a positive authentification.<p>
     *
     * @param sessionId a currently valid session id
     * @param userinfo a Hashtable containing information (including the name) about the user
     */
    public void putUser(String sessionId, Hashtable userinfo) {
        m_sessions.put(sessionId, userinfo);
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
        Hashtable value;
        String name;
        Enumeration enu = m_sessions.keys();
        output.append("[CmsCoreSessions]:\n");
        while (enu.hasMoreElements()) {
            key = (String)enu.nextElement();
            output.append(key + " : ");
            value = (Hashtable)m_sessions.get(key);
            name = (String)value.get(I_CmsConstants.C_SESSION_USERNAME);
            output.append(name + "\n");
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
        Hashtable value;

        // Hastable to return in the vector for one user-entry
        Hashtable userentry;

        Enumeration enu = m_sessions.keys();
        while (enu.hasMoreElements()) {
            userentry = new Hashtable(4);
            key = (String)enu.nextElement();
            value = (Hashtable)m_sessions.get(key);
            userentry.put(I_CmsConstants.C_SESSION_USERNAME, value.get(I_CmsConstants.C_SESSION_USERNAME));
            userentry.put(I_CmsConstants.C_SESSION_PROJECT, value.get(I_CmsConstants.C_SESSION_PROJECT));
            userentry.put(I_CmsConstants.C_SESSION_CURRENTGROUP, value.get(I_CmsConstants.C_SESSION_CURRENTGROUP));
            userentry.put(I_CmsConstants.C_SESSION_MESSAGEPENDING, new Boolean(((Hashtable)value.get(I_CmsConstants.C_SESSION_DATA)).containsKey(I_CmsConstants.C_SESSION_BROADCASTMESSAGE)));

            output.addElement(userentry);
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
        Hashtable value;

        Hashtable session_data;
        String session_message;

        Enumeration enu = m_sessions.keys();
        while (enu.hasMoreElements()) {
            key = (String)enu.nextElement();
            value = (Hashtable)m_sessions.get(key);
            session_data = (Hashtable)value.get(I_CmsConstants.C_SESSION_DATA);
            session_message = (String)session_data.get(I_CmsConstants.C_SESSION_BROADCASTMESSAGE);
            if (session_message == null) {
                session_message = "";
            }
            session_message += message;
            session_data.put(I_CmsConstants.C_SESSION_BROADCASTMESSAGE, session_message);
        }
    }
}

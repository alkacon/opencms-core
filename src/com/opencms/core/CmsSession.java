package com.opencms.core;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

 /**
 * This class implements a session storage. It is required for user authentification to the
 * OpenCms.
 * 
 * For each active user, its name and other additional information (like the current user
 * group) are stored in a hashtable, useing the session Id as key to them.
 * 
 * When the session gets destroyed, the user will remived from the storage.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 2000/01/04 15:01:50 $  
 */

public class CmsSession implements I_CmsConstants,I_CmsSession    
{
 
    
    /**
     * Hashtable storage to store all active users.
     */
    private Hashtable m_sessions;
    
    /**
     * Constructor, creates a new CmsSession object.
     */
    public CmsSession() {
        m_sessions=new Hashtable();
    }
    
    /**
     * Puts a new user into the sesstion storage. A user is stored with its actual 
     * session Id after a positive authentification.
     *
     * This method stores a complete hashtable with additional user information in the 
     * session storage.
     * 
     * When a new user is stored, the intrnal session storage is tested for expired sessions
     * which will be removed.
     * 
     * @param session  The actual user session.
     * @param userinfo A Hashtable containing informaion (including the name) about the user.
     */
    public void putUser(HttpSession session,Hashtable userinfo){
        
        // get the session context. This is requitred for checking for expired sessions.
        HttpSessionContext sessionContext=session.getSessionContext();
        boolean activeSession;
        
        // store the userinfo
        m_sessions.put(session.getId(),userinfo);
        
        //now check for expired sessions
        Enumeration enu = m_sessions.keys();
        while (enu.hasMoreElements()) {
            String key=(String)enu.nextElement();
            activeSession=false;
            Enumeration enus=sessionContext.getIds();
            while (enus.hasMoreElements()){
                if (((String)enus.nextElement()).equals(key)) {
                    activeSession=true;
                }
            }
            // this session is not active anymore, so delete it from the storage
            if (activeSession == false) {
                m_sessions.remove(key);
            }
        }
        
    }
 
     /**
     * Puts a new user into the sesstion storage. A user is stored with its actual 
     * session Id after a positive authentification.
     *
     * When a new user is stored, the intrnal session storage is tested for expired sessions
     * which will be removed.
     * 
     * @param session  The actual user session.
     * @param username The name of the user to be stored.
     */
    public void putUser(HttpSession session,String username){
        Hashtable userinfo=new Hashtable();
        userinfo.put(C_SESSION_USERNAME,username);
        putUser(session,userinfo);
    }
    
    /**
     * Gets the complete userinformation of a user from the session storage.
     * 
     * @param sessionID The actual session.
     * @return Hashtable with userinformation or null;
     */
    public Hashtable getUser(HttpSession session) {
        Hashtable userinfo=null;
        userinfo=(Hashtable)m_sessions.get(session.getId());
        return userinfo;       
    }
    

     /**
     * Gets the  username of a user from the session storage.
     * 
     * @param sessionID The actual session.
     * @return The name of the requested user or null;
     */
    public String getUserName(HttpSession session) {
        Hashtable userinfo=null;
        String username=null;
        userinfo=getUser(session);
        // this user does exist, so get his name.
        if (userinfo != null) {
            username=(String)userinfo.get(C_SESSION_USERNAME);
        }
        return username;
    }
    
     /**
     * Gets the current usergroup of a user from the session storage.
     * 
     * @param sessionID The actual session.
     * @return The name of the current group of the user or the default guest group;
     */
    public String getCurrentGroup(HttpSession session) {
        Hashtable userinfo=null;
        String currentGroup=C_GROUP_GUEST;
        
        userinfo=getUser(session);
        // this user does exist, so get his current Group.
        if (userinfo != null) {
            currentGroup=(String)userinfo.get(C_SESSION_CURRENTGROUP);
            if (currentGroup==null) {
                currentGroup=C_GROUP_GUEST;
            }
        }
        return currentGroup;
    }
    
     /**
     * Gets the current project of a user from the session storage.
     * 
     * @param sessionID The actual session.
     * @return The name of the project of the user or the default project;
     */
    public String getCurrentProject(HttpSession session) {
        Hashtable userinfo=null;
        String currentProject=C_PROJECT_ONLINE;
        
        userinfo=getUser(session);
        // this user does exist, so get his current Project.
        if (userinfo != null) {
            currentProject=(String)userinfo.get(C_SESSION_PROJECT);
            if (currentProject==null) {
                currentProject=C_PROJECT_ONLINE;
            }
        }
        return currentProject;
    }
    
    /**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
    public String toString(){
        StringBuffer output=new StringBuffer();
        String key;
        Hashtable value;
        String name;
        Enumeration enu=m_sessions.keys();
        output.append("[CmsSessions]:/n");
        while (enu.hasMoreElements()){
            key=(String)enu.nextElement();
            output.append(key+" : ");
            value=(Hashtable)m_sessions.get(key);
            name=(String)value.get(C_SESSION_USERNAME);
            output.append(name+"/n");
        }
        return output.toString();
    }
}

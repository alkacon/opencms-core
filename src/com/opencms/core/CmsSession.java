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
 * When the session gets destroyed, the user will removed from the storage.
 * 
 * ToDo: Removal of unused sessions!
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.5 $ $Date: 2000/01/14 13:46:51 $  
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
     * 
     * @param session  The actual user session Id.
     * @param userinfo A Hashtable containing informaion (including the name) about the user.
     */
    public void putUser(String sessionId,Hashtable userinfo){

        // store the userinfo
        m_sessions.put(sessionId,userinfo);
        
    }
 
     /**
     * Puts a new user into the sesstion storage. A user is stored with its actual 
     * session Id after a positive authentification.
     *
     * 
     * @param session  The actual user session Id.
     * @param username The name of the user to be stored.
     */
    public void putUser(String sessionId,String username){
        Hashtable userinfo=new Hashtable();
        userinfo.put(C_SESSION_USERNAME,username);
        putUser(sessionId,userinfo);
    }
    
     /**
     * Puts a new user into the sesstion storage. A user is stored with its actual 
     * session Id after a positive authentification.
     *
     * 
     * @param session  The actual user session Id.
     * @param username The name of the user to be stored.
     * @param group The name of the actual group.
     * @param project The name of the actual project.
     */
    public void putUser(String sessionId,String username,
                        String group, String project){
        Hashtable userinfo=new Hashtable();
        userinfo.put(C_SESSION_USERNAME,username);
        userinfo.put(C_SESSION_CURRENTGROUP,group);
        userinfo.put(C_SESSION_PROJECT,project);
        putUser(sessionId,userinfo);
    }
    
    
    /**
     * Gets the complete userinformation of a user from the session storage.
     * 
     * @param sessionID The actual session Id.
     * @return Hashtable with userinformation or null;
     */
    public Hashtable getUser(String sessionId) {
        Hashtable userinfo=null;
        userinfo=(Hashtable)m_sessions.get(sessionId);
        return userinfo;       
    }
    
     /**
     * Removes a user from the session storage.
     * This is done when the session of the User is destroyed.
     * 
     * @param sessionID The actual session Id.
     */
    public void deleteUser(String sessionId) {
        m_sessions.remove(sessionId) ;          
    }

     /**
     * Gets the  username of a user from the session storage.
     * 
     * @param sessionID The actual session Id.
     * @return The name of the requested user or null;
     */
    public String getUserName(String sessionId) {
        Hashtable userinfo=null;
        String username=null;
        userinfo=getUser(sessionId);
        // this user does exist, so get his name.
        if (userinfo != null) {
            username=(String)userinfo.get(C_SESSION_USERNAME);
        }
        return username;
    }
    
     /**
     * Gets the current usergroup of a user from the session storage.
     * 
     * @param sessionID The actual session Id.
     * @return The name of the current group of the user or the default guest group;
     */
    public String getCurrentGroup(String sessionId) {
        Hashtable userinfo=null;
        String currentGroup=C_GROUP_GUEST;
        
        userinfo=getUser(sessionId);
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
     * @param sessionID The actual session Id.
     * @return The name of the project of the user or the default project;
     */
    public String getCurrentProject(String sessionId) {
        Hashtable userinfo=null;
        String currentProject=C_PROJECT_ONLINE;
        
        userinfo=getUser(sessionId);
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
        output.append("[CmsSessions]:\n");
        while (enu.hasMoreElements()){
            key=(String)enu.nextElement();
            output.append(key+" : ");
            value=(Hashtable)m_sessions.get(key);
            name=(String)value.get(C_SESSION_USERNAME);
            output.append(name+"\n");
        }
        return output.toString();
    }
}

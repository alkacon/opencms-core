package com.opencms.core;

import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

 /**
 * This interface defines a session storage. Ii is required for user authentification to the
 * OpenCms.
 * 
 * For each active user, its name and other additional information (like the current user
 * group) are stored in a hashtable, useing the session Id as key to them.
 * 
 * When the session gets destroyed, the user will remived from the storage.
 * 
 * ToDo: Removal of unused sessions!
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.4 $ $Date: 2000/01/14 13:46:51 $  
 */

public interface I_CmsSession
{   
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
    public void putUser(String sessionId,Hashtable userinfo) ;
 
     /**
     * Puts a new user into the sesstion storage. A user is stored with its actual 
     * session Id after a positive authentification.
     *
     * 
     * @param session  The actual user session Id.
     * @param username The name of the user to be stored.
     */
    public void putUser(String sessionId,String username);
  
    /**
     * Gets the complete userinformation of a user from the session storage.
     * 
     * @param sessionID The actual session Id.
     * @return Hashtable with userinformation or null;
     */
    public Hashtable getUser(String sessionId) ;
    
     /**
     * Removes a user from the session storage.
     * This is done when the session of the User is destroyed.
     * 
     * @param sessionID The actual session Id.
     */
    public void deleteUser(String sessionId) ;       
    

      /**
     * Gets the  username of a user from the session storage.
     * 
     * @param sessionID The actual session Id.
     * @return The name of the requested user or null;
     */
    public String getUserName(String sessionId) ;
    
     /**
     * Gets the current usergroup of a user from the session storage.
     * 
     * @param sessionID The actual session Id.
     * @return The name of the current group of the user or the default guest group;
     */
    public String getCurrentGroup(String sessionId) ;
    
     /**
     * Gets the current project of a user from the session storage.
     * 
     * @param sessionID The actual session Id.
     * @return The name of the project of the user or the default project;
     */
    public String getCurrentProject(String sessionId) ;
}

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
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/01/04 12:23:27 $  
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
     * When a new user is stored, the intrnal session storage is tested for expired sessions
     * which will be removed.
     * 
     * @param session  The actual user session.
     * @param userinfo A Hashtable containing informaion (including the name) about the user.
     */
    public void putUser(HttpSession session,Hashtable userinfo) ;
 
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
    public void putUser(HttpSession session,String username);
    /**
     * Gets the complete userinformation of a user from the session storage.
     * 
     * @param sessionID The actual session.
     * @return Hashtable with userinformation or null;
     */
    public Hashtable getUser(HttpSession session) ;
    

      /**
     * Gets the  username of a user from the session storage.
     * 
     * @param sessionID The actual session.
     * @return The name of the requested user or null;
     */
    public String getUserName(HttpSession session) ;
    
     /**
     * Gets the current usergroup of a user from the session storage.
     * 
     * @param sessionID The actual session.
     * @return The name of the current group of the user or the default guest group;
     */
    public String getCurrentGroup(HttpSession session) ;
}

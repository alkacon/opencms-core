/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/I_CmsSession.java,v $
 * Date   : $Date: 2000/04/04 10:28:47 $
 * Version: $Revision: 1.6 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

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
 * @version $Revision: 1.6 $ $Date: 2000/04/04 10:28:47 $  
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
     * @return The id of the project of the user or the default project;
     */
    public Integer getCurrentProject(String sessionId) ;
}

package com.opencms.core;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsCoreSession.java,v $
 * Date   : $Date: 2000/08/25 13:40:05 $
 * Version: $Revision: 1.3 $
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
 * @version $Revision: 1.3 $ $Date: 2000/08/25 13:40:05 $  
 */

public class CmsCoreSession implements I_CmsConstants    
{
 
	
	/**
	 * Hashtable storage to store all active users.
	 */
	private Hashtable m_sessions;

	/**
	 * Constructor, creates a new CmsCoreSession object.
	 */
	public CmsCoreSession() {
		m_sessions=new Hashtable();
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
	public Integer getCurrentProject(String sessionId) {
		Hashtable userinfo=null;
		Integer currentProject=new Integer(C_PROJECT_ONLINE_ID);
		
		userinfo=getUser(sessionId);
		// this user does exist, so get his current Project.
		if (userinfo != null) {
			currentProject=(Integer)userinfo.get(C_SESSION_PROJECT);
			if (currentProject==null) {
				currentProject=new Integer(C_PROJECT_ONLINE_ID);
			}
		}
		return currentProject;
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
	 * @param project The id of the actual project.
	 */
	public void putUser(String sessionId,String username,
						String group, Integer project){
		Hashtable userinfo=new Hashtable();
		userinfo.put(C_SESSION_USERNAME,username);
		userinfo.put(C_SESSION_CURRENTGROUP,group);
		userinfo.put(C_SESSION_PROJECT,project);
		putUser(sessionId,userinfo);
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
		output.append("[CmsCoreSessions]:\n");
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

package com.opencms.file.oracleplsql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oracleplsql/Attic/CmsResourceBroker.java,v $
 * Date   : $Date: 2000/09/29 15:36:24 $
 * Version: $Revision: 1.2 $
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

import javax.servlet.http.*;
import java.util.*;
import java.net.*;
import java.io.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;
import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;


/**
 * This is THE resource broker. It merges all resource broker
 * into one public class. The interface is local to package. <B>All</B> methods
 * get additional parameters (callingUser and currentproject) to check the security-
 * police.
 * 
 * @author Andreas Schouten
 * @author Michaela Schleich
 * @author Michael Emmerich
 * @author Anders Fugmann
 * @version $Revision: 1.2 $ $Date: 2000/09/29 15:36:24 $
 */
public class CmsResourceBroker extends com.opencms.file.genericSql.CmsResourceBroker {
/**
 * Checks, if the user may create this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * 
 * @return wether the user has access, or not.
 */
public boolean accessCreate(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	return (dbAccess.accessCreate(currentUser, currentProject, resource));
}
/**
 * Checks, if the group may access this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * @param flags The flags to check.
 * 
 * @return wether the user has access, or not.
 */
protected boolean accessGroup(CmsUser currentUser, CmsProject currentProject, CmsResource resource, int flags) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	return (dbAccess.accessGroup(currentUser, currentProject, resource, flags));
}
/**
 * Checks, if the user may lock this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * 
 * @return wether the user has access, or not.
 */
public boolean accessLock(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	return (dbAccess.accessLock(currentUser, currentProject, resource));
}
/**
 * Checks, if the other may access this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * @param flags The flags to check.
 * 
 * @return wether the user has access, or not.
 */
protected boolean accessOther(CmsUser currentUser, CmsProject currentProject, CmsResource resource, int flags) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	return (dbAccess.accessOther(currentUser, currentProject, resource, flags));
}
/**
 * Checks, if the owner may access this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * @param flags The flags to check.
 * 
 * @return wether the user has access, or not.
 */
protected boolean accessOwner(CmsUser currentUser, CmsProject currentProject, CmsResource resource, int flags) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	return (dbAccess.accessOwner(currentUser, currentProject, resource, flags));
}
// Methods working with projects

/**
 * Tests if the user can access the project.
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param projectId the id of the project.
 * @return true, if the user has access, else returns false.
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public boolean accessProject(CmsUser currentUser, CmsProject currentProject, int projectId) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	return (dbAccess.accessProject(currentUser, currentProject));
}
/**
 * Checks, if the user may read this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * 
 * @return wether the user has access, or not.
 */
public boolean accessRead(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	return (dbAccess.accessRead(currentUser, currentProject, resource));
}
/**
 * Checks, if the user may write this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * 
 * @return wether the user has access, or not.
 */
public boolean accessWrite(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	return (dbAccess.accessWrite(currentUser, currentProject, resource));
}
/**
 * Copies a resource from the online project to a new, specified project.<br>
 * Copying a resource will copy the file header or folder into the specified 
 * offline project and set its state to UNCHANGED.
 * 
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user is the owner of the project</li>
 * </ul>
 *	 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The name of the resource.
 	 * @exception CmsException  Throws CmsException if operation was not succesful.
 */
public void copyResourceToProject(CmsUser currentUser, CmsProject currentProject, String resource) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	dbAccess.copyResourceToProject(currentUser, currentProject, resource);
}
/**
 * return the correct DbAccess class.
 * This method should be overloaded by all other Database Drivers 
 * Creation date: (09/15/00 %r)
 * @return com.opencms.file.genericSql.CmsDbAccess
 * @param configurations source.org.apache.java.util.Configurations
 * @exception com.opencms.core.CmsException Thrown if CmsDbAccess class could not be instantiated. 
 */
public com.opencms.file.genericSql.CmsDbAccess createDbAccess(Configurations configurations) throws CmsException
{
	return new com.opencms.file.oracleplsql.CmsDbAccess(configurations);
}
	/**
	 * Returns all projects, which are owned by the user or which are accessible
	 * for the group of the user.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjects(CmsUser currentUser, 
											CmsProject currentProject)
		 throws CmsException {

	    com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;		
		// get all projects which are owned by the user.
		Vector projects = dbAccess.getAllAccessibleProjects(currentUser);
		
		// return the vector of projects
		return(projects);
	 }
/**
 * Returns a list of groups of a user.<P/>
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param username The name of the user.
 * @return Vector of groups
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public Vector getGroupsOfUser(CmsUser currentUser, CmsProject currentProject, String username) throws CmsException {
		com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
		Vector allGroups = dbAccess.getGroupsOfUser(username);
/*
  allGroups = (Vector) m_usergroupsCache.get(C_USER + username);
	if ((allGroups == null) || (allGroups.size() == 0)) {
		com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
		Vector groups = dbAccess.getGroupsOfUser(username);
		m_usergroupsCache.put(C_USER + username, allGroups);
	}
*/	
	return allGroups;
}
	/**
	 * Returns a list of users in a group.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupname The name of the group to list users from.
	 * @return Vector of users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getUsersOfGroup(CmsUser currentUser, CmsProject currentProject, 
								  String groupname)
		throws CmsException {
		// check the security
		com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
		return dbAccess.getUsersOfGroup(currentUser, groupname, C_USER_TYPE_SYSTEMUSER);
/*
		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return dbAccess.getUsersOfGroup(currentUser, groupname, C_USER_TYPE_SYSTEMUSER);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + groupname, 
				CmsException.C_NO_ACCESS);
		}
*/		
	}
	/**
	 * Initializes the resource broker and sets up all required modules and connections.
	 * @param config The OpenCms configuration.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void init(Configurations config) throws CmsException {
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsResourceBroker] WARNING: this oracleplsql-resource-broker is experimentell and only for developing.");
		}
		super.init(config);
	}
/**
 * Determines, if the users may manage a project.<BR/>
 * Only the manager of a project may publish it.
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @return true, if the may manage this project.
 * @exception CmsException Throws CmsException if operation was not succesful.
 */
public boolean isManagerOfProject(CmsUser currentUser, CmsProject currentProject) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	return (dbAccess.isManagerOfProject(currentUser, currentProject));
}
/**
 * Locks a resource.<br>
 * 
 * Only a resource in an offline project can be locked. The state of the resource
 * is set to CHANGED (1).
 * If the content of this resource is not exisiting in the offline project already,
 * it is read from the online project and written into the offline project.
 * A user can lock a resource, so he is the only one who can write this 
 * resource. <br>
 * 
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can write the resource</li>
 * <li>the resource is not locked by another user</li>
 * </ul>
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The complete path to the resource to lock.
 * @param force If force is true, a existing locking will be oberwritten.
 * 
 * @exception CmsException  Throws CmsException if operation was not succesful.
 * It will also be thrown, if there is a existing lock
 * and force was set to false.
 */
public void lockResource(CmsUser currentUser, CmsProject currentProject, String resourcename, boolean force) throws CmsException {

	//CmsResource  cmsResource=null;
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	dbAccess.lockResource(currentUser, currentProject, resourcename, force);
}
/**
 * Locks a resource.<br>
 * 
 * Only a resource in an offline project can be locked. The state of the resource
 * is set to CHANGED (1).
 * If the content of this resource is not exisiting in the offline project already,
 * it is read from the online project and written into the offline project.
 * A user can lock a resource, so he is the only one who can write this 
 * resource. <br>
 * 
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can write the resource</li>
 * <li>the resource is not locked by another user</li>
 * </ul>
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The complete path to the resource to lock.
 * @param force If force is true, a existing locking will be oberwritten.
 * 
 * @exception CmsException  Throws CmsException if operation was not succesful.
 * It will also be thrown, if there is a existing lock
 * and force was set to false.
 */
public void unlockResource(CmsUser currentUser, CmsProject currentProject, String resourcename) throws CmsException {

	//CmsResource  cmsResource=null;
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	dbAccess.unlockResource(currentUser, currentProject, resourcename);
}
	/**
	 * Checks if a user is member of a group.<P/>
	 *  
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param callingUser The user who wants to use this method.
	 * @param nameuser The name of the user to check.
	 * @param groupname The name of the group to check.
	 * @return True or False
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public boolean userInGroup(CmsUser currentUser, CmsProject currentProject, 
							   String username, String groupname)
		throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsUser user = readUser(currentUser, currentProject, username);
	CmsGroup group = readGroup(currentUser, currentProject, groupname);
	return (dbAccess.userInGroup(user.getId(), group.getId()));
	}
}

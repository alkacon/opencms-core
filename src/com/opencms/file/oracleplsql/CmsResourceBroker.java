package com.opencms.file.oracleplsql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oracleplsql/Attic/CmsResourceBroker.java,v $
 * Date   : $Date: 2000/11/16 13:31:52 $
 * Version: $Revision: 1.7 $
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
 * @version $Revision: 1.7 $ $Date: 2000/11/16 13:31:52 $
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
 * adds a file extension to the list of known file extensions 
 * 
 * <B>Security:</B>
 * Users, which are in the group "administrators" are granted.<BR/>
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param extension a file extension like 'html'
 * @param resTypeName name of the resource type associated to the extension
 */

public void addFileExtension(CmsUser currentUser, CmsProject currentProject, String extension, String resTypeName) 
	throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	if (extension != null && resTypeName != null) {
		if (isAdmin(currentUser, currentProject)) {
			Hashtable suffixes = (Hashtable) dbAccess.readSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS);
			if (suffixes == null) {
				suffixes = new Hashtable();
				suffixes.put(extension, resTypeName);
				dbAccess.addSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS, suffixes);
			} else {
				suffixes.put(extension, resTypeName);
				dbAccess.writeSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS, suffixes);
			}
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + extension, CmsException.C_NO_ACCESS);
		}
	}
}
/**
 * Adds a CmsResourceTypes.
 * 
 * <B>Security:</B>
 * Users, which are in the group "administrators" are granted.<BR/>
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resourceType the name of the resource to get.
 * @param launcherType the launcherType-id
 * @param launcherClass the name of the launcher-class normaly ""
 * 
 * Returns a CmsResourceTypes.
 * 
 * @exception CmsException  Throws CmsException if operation was not succesful.
 */
public CmsResourceType addResourceType(CmsUser currentUser, CmsProject currentProject, String resourceType, int launcherType, String launcherClass) 
	throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	if (isAdmin(currentUser, currentProject)) {

		// read the resourceTypes from the propertys
		m_resourceTypes = (Hashtable) dbAccess.readSystemProperty(C_SYSTEMPROPERTY_RESOURCE_TYPE);
		synchronized (m_resourceTypes) {

			// get the last index and increment it.
			Integer lastIndex = new Integer(((Integer) m_resourceTypes.get(C_TYPE_LAST_INDEX)).intValue() + 1);

			// write the last index back
			m_resourceTypes.put(C_TYPE_LAST_INDEX, lastIndex);

			// add the new resource-type
			m_resourceTypes.put(resourceType, new CmsResourceType(lastIndex.intValue(), launcherType, resourceType, launcherClass));

			// store the resource types in the properties
			dbAccess.writeSystemProperty(C_SYSTEMPROPERTY_RESOURCE_TYPE, m_resourceTypes);
		}

		// the cached resource types aren't valid any more.
		m_resourceTypes = null;
		// return the new resource-type
		return (getResourceType(currentUser, currentProject, resourceType));
	} else {
		throw new CmsException("[" + this.getClass().getName() + "] " + resourceType, CmsException.C_NO_ACCESS);
	}
}
/** 
 * Adds a user to the Cms.
 * 
 * Only a adminstrator can add users to the cms.<P/>
 * 
 * <B>Security:</B>
 * Only users, which are in the group "administrators" are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param name The new name for the user.
 * @param password The new password for the user.
 * @param group The default groupname for the user.
 * @param description The description for the user.
 * @param additionalInfos A Hashtable with additional infos for the user. These
 * Infos may be stored into the Usertables (depending on the implementation).
 * @param flags The flags for a user (e.g. C_FLAG_ENABLED)
 * 
 * @return user The added user will be returned.
 * 
 * @exception CmsException Throws CmsException if operation was not succesfull.
 */
public CmsUser addUser(CmsUser currentUser, CmsProject currentProject, String name, String password, String group, String description, Hashtable additionalInfos, int flags) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;

	// Check the security
	if (isAdmin(currentUser, currentProject)) {
		// check the password minimumsize
		if ((name.length() > 0) && (password.length() >= C_PASSWORD_MINIMUMSIZE)) {
			CmsGroup defaultGroup = readGroup(currentUser, currentProject, group);
			CmsUser newUser = dbAccess.addUser(name, password, description, " ", " ", " ", 0, 0, C_FLAG_ENABLED, additionalInfos, defaultGroup, " ", " ", C_USER_TYPE_SYSTEMUSER);
			addUserToGroup(currentUser, currentProject, newUser.getName(), defaultGroup.getName());
			return newUser;
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_SHORT_PASSWORD);
		}
	} else {
		throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_NO_ACCESS);
	}
}
/** 
 * Adds a web user to the Cms. <br>
 * 
 * A web user has no access to the workplace but is able to access personalized
 * functions controlled by the OpenCms.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param name The new name for the user.
 * @param password The new password for the user.
 * @param group The default groupname for the user.
 * @param description The description for the user.
 * @param additionalInfos A Hashtable with additional infos for the user. These
 * Infos may be stored into the Usertables (depending on the implementation).
 * @param flags The flags for a user (e.g. C_FLAG_ENABLED)
 * 
 * @return user The added user will be returned.
 * 
 * @exception CmsException Throws CmsException if operation was not succesfull.
 */
public CmsUser addWebUser(CmsUser currentUser, CmsProject currentProject, String name, String password, String group, String description, Hashtable additionalInfos, int flags) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;

	// check the password minimumsize
	if ((name.length() > 0) && (password.length() >= C_PASSWORD_MINIMUMSIZE)) {
		CmsGroup defaultGroup = readGroup(currentUser, currentProject, group);
		CmsUser newUser = dbAccess.addUser(name, password, description, " ", " ", " ", 0, 0, C_FLAG_ENABLED, additionalInfos, defaultGroup, " ", " ", C_USER_TYPE_WEBUSER);
		CmsUser user;
		CmsGroup usergroup;
		user = dbAccess.readUser(newUser.getName(), C_USER_TYPE_WEBUSER);

		//check if the user exists
		if (user != null) {
			usergroup = readGroup(currentUser, currentProject, group);
			//check if group exists
			if (usergroup != null) {
				//add this user to the group
				m_dbAccess.addUserToGroup(user.getId(), usergroup.getId());
				// update the cache
				m_usergroupsCache.clear();
			} else {
				throw new CmsException("[" + this.getClass().getName() + "]" + group, CmsException.C_NO_GROUP);
			}
		} else {
			throw new CmsException("[" + this.getClass().getName() + "]" + name, CmsException.C_NO_USER);
		}
		return newUser;
	} else {
		throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_SHORT_PASSWORD);
	}
}
/**
 * Copies a file in the Cms. <br>
 * 
 * <B>Security:</B>
 * Access is cranted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can read the sourceresource</li>
 * <li>the user can create the destinationresource</li>
 * <li>the destinationresource dosn't exists</li>
 * </ul>
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param source The complete path of the sourcefile.
 * @param destination The complete path to the destination.
 * 
 * @exception CmsException  Throws CmsException if operation was not succesful.
 */
public void copyFile(CmsUser currentUser, CmsProject currentProject, String source, String destination) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	try {
		dbAccess.copyFile(currentProject, currentUser.getId(), source, destination);
		// inform about the file-system-change
		fileSystemChanged();
	} catch (CmsException e) {
		throw e;
	}
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
	 * Creates a new file with the given content and resourcetype. <br>
	 * 
	 * Files can only be created in an offline project, the state of the new file
	 * is set to NEW (2). <br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the folder-resource is not locked by another user</li>
	 * <li>the file dosn't exists</li>
	 * </ul>
	 * 
	 * @param currentUser The user who owns this file.
	 * @param currentGroup The group who owns this file.
	 * @param currentProject The project in which the resource will be used.
	 * @param folder The complete path to the folder in which the new folder will 
	 * be created.
	 * @param file The name of the new file (No pathinformation allowed).
	 * @param contents The contents of the new file.
	 * @param type The name of the resourcetype of the new file.
	 * @param propertyinfos A Hashtable of propertyinfos, that should be set for this folder.
	 * The keys for this Hashtable are the names for propertydefinitions, the values are
	 * the values for the propertyinfos.
	 * @return file The created file.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	 public CmsFile createFile(CmsUser currentUser, CmsGroup currentGroup, 
							   CmsProject currentProject, String folder,
							   String filename, byte[] contents, String type,
							   Hashtable propertyinfos) 
						
		 throws CmsException {
		com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;

		// check for mandatory metainfos
		checkMandatoryProperties(currentUser, currentProject, type, propertyinfos);
		  
		// checks, if the filename is valid, if not it throws a exception
		validFilename(filename);

		CmsFolder cmsFolder = readFolder(currentUser,currentProject, folder);
		if( accessCreate(currentUser, currentProject, (CmsResource)cmsFolder) ) {

			// write-access was granted - create and return the file.
			// this part uses the oracle-specific method because of using Oracle-BLOB		
			CmsFile file = dbAccess.createFile(currentUser, currentProject, 
											   onlineProject(currentUser, currentProject), 
											   folder + filename, 0, cmsFolder.getResourceId(),
											   contents, 
											   getResourceType(currentUser, currentProject, type));  
			// update the access flags
			Hashtable startSettings=null;
			Integer accessFlags=null;
			startSettings=(Hashtable)currentUser.getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);                    
			if (startSettings != null) {
				accessFlags=(Integer)startSettings.get(C_START_ACCESSFLAGS);
				if (accessFlags != null) {
					file.setAccessFlags(accessFlags.intValue());
				}
			}
			if(currentGroup != null) {                
				file.setGroupId(currentGroup.getId());
			}
			
			dbAccess.writeFileHeader(currentProject, file, false);

			m_subresCache.clear();    
			// write the metainfos
			m_dbAccess.writeProperties(propertyinfos, file.getResourceId(), file.getType());
			// inform about the file-system-change
			fileSystemChanged();
			return file ;
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + folder + filename, 
				CmsException.C_NO_ACCESS);
		}

	 }
/**
 * Creates a new task.
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param projectid The Id of the current project task of the user.
 * @param agentName User who will edit the task 
 * @param roleName Usergroup for the task
 * @param taskName Name of the task
 * @param taskType Type of the task 
 * @param taskComment Description of the task
 * @param timeout Time when the task must finished
 * @param priority Id for the priority
 * 
 * @return A new Task Object
 * 
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public CmsTask createTask(CmsUser currentUser, int projectid, String agentName, String roleName, String taskName, String taskComment, int taskType, long timeout, int priority) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsUser agent = dbAccess.readUser(agentName, C_USER_TYPE_SYSTEMUSER);
	CmsGroup role = m_dbAccess.readGroup(roleName);
	java.sql.Timestamp timestamp = new java.sql.Timestamp(timeout);
	java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
	CmsTask task = m_dbAccess.createTask(projectid, projectid, taskType, currentUser.getId(), agent.getId(), role.getId(), taskName, now, timestamp, priority);
	if (taskComment != null && !taskComment.equals("")) {
		m_dbAccess.writeTaskLog(task.getId(), currentUser.getId(), new java.sql.Timestamp(System.currentTimeMillis()), taskComment, C_TASKLOG_USER);
	}
	return task;
}
/**
 * Creates a new task.
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param agent Username who will edit the task 
 * @param role Usergroupname for the task
 * @param taskname Name of the task
 * @param taskcomment Description of the task.
 * @param timeout Time when the task must finished
 * @param priority Id for the priority
 * 
 * @return A new Task Object
 * 
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public CmsTask createTask(CmsUser currentUser, CmsProject currentProject, String agentName, String roleName, String taskname, String taskcomment, long timeout, int priority) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsGroup role = m_dbAccess.readGroup(roleName);
	java.sql.Timestamp timestamp = new java.sql.Timestamp(timeout);
	java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
	int agentId = C_UNKNOWN_ID;
	try {
		agentId = dbAccess.readUser(agentName, C_USER_TYPE_SYSTEMUSER).getId();
	} catch (Exception e) {
		// ignore that this user doesn't exist and create a task for the role
	}
	return m_dbAccess.createTask(currentProject.getTaskId(), currentProject.getTaskId(), 1, // standart Task Type
	currentUser.getId(), agentId, role.getId(), taskname, now, timestamp, priority);
}
/**
 * Forwards a task to a new user.
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param taskid The Id of the task to forward.
 * @param newRole The new Group for the task
 * @param newUser The new user who gets the task. if its "" the new agent will automatic selected
 * 
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public void forwardTask(CmsUser currentUser, CmsProject currentProject, int taskid, String newRoleName, String newUserName) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsGroup newRole = m_dbAccess.readGroup(newRoleName);
	CmsUser newUser = null;
	if (newUserName.equals("")) {
		// use new method readUserAgent because of protected method findAgent()
		newUser = dbAccess.readUserAgent(newRole.getId());
	} else {
		newUser = dbAccess.readUser(newUserName, C_USER_TYPE_SYSTEMUSER);
	}
	m_dbAccess.forwardTask(taskid, newRole.getId(), newUser.getId());
	m_dbAccess.writeSystemTaskLog(taskid, "Task fowarded from " + currentUser.getFirstname() + " " + currentUser.getLastname() + " to " + newUser.getFirstname() + " " + newUser.getLastname() + ".");
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
 * Returns a Vector with all I_CmsResourceTypes.
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * 
 * Returns a Hashtable with all I_CmsResourceTypes.
 * 
 * @exception CmsException  Throws CmsException if operation was not succesful.
 */
public Hashtable getAllResourceTypes(CmsUser currentUser, CmsProject currentProject) 
	throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;

	// check, if the resourceTypes were read bevore
	if (m_resourceTypes == null) {
		// read the resourceTypes from the propertys
		m_resourceTypes = (Hashtable) dbAccess.readSystemProperty(C_SYSTEMPROPERTY_RESOURCE_TYPE);

		// remove the last index.
		m_resourceTypes.remove(C_TYPE_LAST_INDEX);
	}

	// return the resource-types.
	return (m_resourceTypes);
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
	//Vector allGroups = dbAccess.getGroupsOfUser(username);

	Vector allGroups = (Vector) m_usergroupsCache.get(C_USER + username);
	if ((allGroups == null) || (allGroups.size() == 0)) {
		Vector groups = dbAccess.getGroupsOfUser(username);
		m_usergroupsCache.put(C_USER + username, groups);
		return groups;		
	}	
	return allGroups;
}
/**
 * Returns all users<P/>
 * 
 * <B>Security:</B>
 * All users are granted, except the anonymous user.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @return users A Vector of all existing users.
 * @exception CmsException Throws CmsException if operation was not succesful.
 */
public Vector getUsers(CmsUser currentUser, CmsProject currentProject) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;

	// check security
	if (!anonymousUser(currentUser, currentProject).equals(currentUser)) {
		return dbAccess.getUsers(C_USER_TYPE_SYSTEMUSER);
	} else {
		throw new CmsException("[" + this.getClass().getName() + "] " + currentUser.getName(), CmsException.C_NO_ACCESS);
	}
}
/**
 * Returns all users from a given type<P/>
 * 
 * <B>Security:</B>
 * All users are granted, except the anonymous user.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param type The type of the users. 
 * @return users A Vector of all existing users.
 * @exception CmsException Throws CmsException if operation was not succesful.
 */
public Vector getUsers(CmsUser currentUser, CmsProject currentProject, int type) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;

	// check security
	if (!anonymousUser(currentUser, currentProject).equals(currentUser)) {
		return dbAccess.getUsers(type);
	} else {
		throw new CmsException("[" + this.getClass().getName() + "] " + currentUser.getName(), CmsException.C_NO_ACCESS);
	}
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
//		return dbAccess.getUsersOfGroup(currentUser, groupname, C_USER_TYPE_SYSTEMUSER);

		if( ! anonymousUser(currentUser, currentProject).equals( currentUser ) ) {
			return dbAccess.getUsersOfGroup(currentUser, groupname, C_USER_TYPE_SYSTEMUSER);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + groupname, 
				CmsException.C_NO_ACCESS);
		}
		
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
	CmsResource cmsResource = null;
	CmsFolder cmsFolder = null;
	CmsFile cmsFile = null;
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	Vector resources = dbAccess.lockResource(currentUser, currentProject, resourcename, force);
	//dbAccess.lockResource(currentUser, currentProject, resourcename, force);
	//m_resourceCache.clear();
	// update the cache
	
	for (int i = 0; i < resources.size(); i++) {		
		cmsResource = (CmsResource) resources.elementAt(i);
		String resourceName = cmsResource.getAbsolutePath();
		if (resourceName.endsWith("/")) {
			cmsFolder = new CmsFolder(cmsResource.getResourceId(), cmsResource.getParentId(), 
									cmsResource.getFileId(), resourceName, cmsResource.getType(), 
									cmsResource.getFlags(), cmsResource.getOwnerId(), cmsResource.getGroupId(), 
									cmsResource.getProjectId(), cmsResource.getAccessFlags(), 
									cmsResource.getState(), cmsResource.isLockedBy(), cmsResource.getDateCreated(), 
									cmsResource.getDateLastModified(), cmsResource.getResourceLastModifiedBy());
				
			m_resourceCache.put(C_FOLDER + currentProject.getId() + resourceName, cmsFolder);
		} else {
			cmsFile = new CmsFile(cmsResource.getResourceId(), cmsResource.getParentId(), 
									cmsResource.getFileId(), resourceName, cmsResource.getType(), 
									cmsResource.getFlags(), cmsResource.getOwnerId(), cmsResource.getGroupId(), 
									cmsResource.getProjectId(), cmsResource.getAccessFlags(), 
									cmsResource.getState(), cmsResource.isLockedBy(), cmsResource.getLauncherType(), 
									cmsResource.getLauncherClassname(),	cmsResource.getDateCreated(), 
									cmsResource.getDateLastModified(), cmsResource.getResourceLastModifiedBy(), 
									new byte[0], cmsResource.getLength());
				
			m_resourceCache.put(C_FILE + currentProject.getId() + resourceName, cmsFile);
		}
	}
	
	m_subresCache.clear();
}
//  Methods working with user and groups

/**
 * Logs a user into the Cms, if the password is correct.
 * 
 * <B>Security</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param username The name of the user to be returned.
 * @param password The password of the user to be returned.
 * @return the logged in user.
 * 
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public CmsUser loginUser(CmsUser currentUser, CmsProject currentProject, String username, String password) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsUser newUser = readUser(currentUser, currentProject, username, password);

	// is the user enabled?
	if (newUser.getFlags() == C_FLAG_ENABLED) {
		// Yes - log him in!
		// first write the lastlogin-time.
		newUser.setLastlogin(new Date().getTime());
		// write the user back to the cms.
		dbAccess.writeUser(newUser);
		// update cache
		m_userCache.put(newUser.getName(), newUser);
		return (newUser);
	} else {
		// No Access!
		throw new CmsException("[" + this.getClass().getName() + "] " + username, CmsException.C_NO_ACCESS);
	}
}
/**
 * Logs a web user into the Cms, if the password is correct.
 * 
 * <B>Security</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param username The name of the user to be returned.
 * @param password The password of the user to be returned.
 * @return the logged in user.
 * 
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public CmsUser loginWebUser(CmsUser currentUser, CmsProject currentProject, String username, String password) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsUser newUser = readWebUser(currentUser, currentProject, username, password);

	// is the user enabled?
	if (newUser.getFlags() == C_FLAG_ENABLED) {
		// Yes - log him in!
		// first write the lastlogin-time.
		newUser.setLastlogin(new Date().getTime());
		// write the user back to the cms.
		dbAccess.writeUser(newUser);
		// update cache
		m_userCache.put(newUser.getName(), newUser);
		return (newUser);
	} else {
		// No Access!
		throw new CmsException("[" + this.getClass().getName() + "] " + username, CmsException.C_NO_ACCESS);
	}
}
/**
 * Publishes a project.
 * 
 * <B>Security</B>
 * Only the admin or the owner of the project can do this.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param id The id of the project to be published.
 * @return a vector of changed resources.
 * 
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public void publishProject(CmsUser currentUser, CmsProject currentProject, int id) throws CmsException {
	CmsProject publishProject = readProject(currentUser, currentProject, id);

	// check the security	
	if ((isAdmin(currentUser, currentProject) || isManagerOfProject(currentUser, publishProject)) && (publishProject.getFlags() == C_PROJECT_STATE_UNLOCKED)) {
		com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
		dbAccess.publishProject(currentUser, id, onlineProject(currentUser, currentProject));
		m_subresCache.clear();
		// inform about the file-system-change
		fileSystemChanged();

		// the project-state will be set to "published", the date will be set.
		// the project must be written to the cms.

		CmsProject project = readProject(currentUser, currentProject, id);
		project.setFlags(C_PROJECT_STATE_ARCHIVE);
		project.setPublishingDate(new Date().getTime());
		project.setPublishedBy(currentUser.getId());
		m_dbAccess.writeProject(project);
		m_projectCache.put(project.getId(), project);

		// finally set the refrish signal to another server if nescessary
		if (m_refresh.length() > 0) {
			try {
				URL url = new URL(m_refresh);
				URLConnection con = url.openConnection();
				con.connect();
				InputStream in = con.getInputStream();
				in.close();
			} catch (Exception ex) {
				throw new CmsException(0, ex);
			}
		}
	} else {
		throw new CmsException("[" + this.getClass().getName() + "] could not publish project " + id, CmsException.C_NO_ACCESS);
	}
}
// Methods working with system properties


/**
 * Reads the export-path for the system.
 * This path is used for db-export and db-import.
 * 
 * <B>Security:</B>
 * All users are granted.<BR/>
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @return the exportpath.
 */
public String readExportPath(CmsUser currentUser, CmsProject currentProject) 
	throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	return (String) dbAccess.readSystemProperty(C_SYSTEMPROPERTY_EXPORTPATH);
}
/**
 * Reads a file from a previous project of the Cms.<BR/>
 * 
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can read the resource</li>
 * </ul>
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param projectId The id of the project to read the file from.
 * @param filename The name of the file to be read.
 * 
 * @return The file read from the Cms.
 * 
 * @exception CmsException  Throws CmsException if operation was not succesful.
 * */
public CmsFile readFile(CmsUser currentUser, CmsProject currentProject, int projectId, String filename) throws CmsException
{
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsFile cmsFile = null;
	// read the resource from the projectId, 
	try
	{
		cmsFile = dbAccess.readFile(currentUser.getId(), projectId, onlineProject(currentUser, currentProject).getId(), filename);

	}
	catch (CmsException exc)
	{
		throw exc;
	}
	return cmsFile;
	}
//  Methods working with resources

/**
 * Reads a file from the Cms.<BR/>
 * 
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can read the resource</li>
 * </ul>
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param filename The name of the file to be read.
 * 
 * @return The file read from the Cms.
 * 
 * @exception CmsException  Throws CmsException if operation was not succesful.
 * */
public CmsFile readFile(CmsUser currentUser, CmsProject currentProject, String filename) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsFile cmsFile = null;
	// read the resource from the currentProject, or the online-project
	try {
		cmsFile = dbAccess.readFile(currentUser.getId(), currentProject.getId(), onlineProject(currentUser, currentProject).getId(), filename);
	} catch (CmsException exc) {
		// the resource was not readable
		throw exc;
	}
	return cmsFile;
}
/**
 * Gets the known file extensions (=suffixes) 
 * 
 * <B>Security:</B>
 * All users are granted access<BR/>
 * 
 * @param currentUser The user who requested this method, not used here
 * @param currentProject The current project of the user, not used here
 * 
 * @return Hashtable with file extensions as Strings
 */

public Hashtable readFileExtensions(CmsUser currentUser, CmsProject currentProject) 
	throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	Hashtable res = (Hashtable) dbAccess.readSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS);
	return ((res != null) ? res : new Hashtable());
}
/**
 * Gets the MimeTypes. 
 * The Mime-Types will be returned.
 * 
 * <B>Security:</B>
 * All users are garnted<BR/>
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * 
 * @return the mime-types.
 */
public Hashtable readMimeTypes(CmsUser currentUser, CmsProject currentProject) 
	throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	return (Hashtable) dbAccess.readSystemProperty(C_SYSTEMPROPERTY_MIMETYPES);
}
/**
 * Reads all tasks for a user in a project.
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param projectId The id of the Project in which the tasks are defined.
 * @param userName The user who has to process the task.
 * @param taskType Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
 * @param orderBy Chooses, how to order the tasks.
 * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public Vector readTasksForUser(CmsUser currentUser, CmsProject currentProject, int projectId, String userName, int taskType, String orderBy, String sort) 
	throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsUser user = dbAccess.readUser(userName, C_USER_TYPE_SYSTEMUSER);
	return m_dbAccess.readTasks(currentProject, user, null, null, taskType, orderBy, sort);
}
/**
 * Returns a user object.<P/>
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param id The id of the user that is to be read.
 * @return User
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, int id) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	try {
		CmsUser user = null;
		// try to read the user from cache
		user = (CmsUser) m_userCache.get(id);
		if (user == null) {
			user = dbAccess.readUser(id);
			m_userCache.put(id, user);
		}
		return user;
	} catch (CmsException ex) {
		return new CmsUser(C_UNKNOWN_ID, id + "", "deleted user");
	}
}
/**
 * Returns a user object.<P/>
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param username The name of the user that is to be read.
 * @return User
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, String username) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsUser user = null;
	// try to read the user from cache
	user = (CmsUser) m_userCache.get(username);
	if (user == null) {
		user = dbAccess.readUser(username, C_USER_TYPE_SYSTEMUSER);
		m_userCache.put(username, user);
	}
	return user;
}
/**
 * Returns a user object.<P/>
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param username The name of the user that is to be read.
 * @param type The type of the user.
 * @return User
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, String username, int type) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsUser user = null;
	// try to read the user from cache
	user = (CmsUser) m_userCache.get(username);
	if (user == null) {
		user = dbAccess.readUser(username, type);
		m_userCache.put(username, user);
	}
	return user;
}
/**
 * Returns a user object if the password for the user is correct.<P/>
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param username The username of the user that is to be read.
 * @param password The password of the user that is to be read.
 * @return User
 * 
 * @exception CmsException  Throws CmsException if operation was not succesful
 */
public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, String username, String password) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsUser user = dbAccess.readUser(username, password, C_USER_TYPE_SYSTEMUSER);
	// store user in cache
	if (user == null) {
		m_userCache.put(username, user);
	}
	return user;
}
/**
 * Returns a user object if the password for the user is correct.<P/>
 * 
 * <B>Security:</B>
 * All users are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param username The username of the user that is to be read.
 * @param password The password of the user that is to be read.
 * @return User
 * 
 * @exception CmsException  Throws CmsException if operation was not succesful
 */
public CmsUser readWebUser(CmsUser currentUser, CmsProject currentProject, String username, String password) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	CmsUser user = dbAccess.readUser(username, password, C_USER_TYPE_WEBUSER);
	// store user in cache
	if (user == null) {
		m_userCache.put(username, user);
	}
	return user;
}
/**
 * This method loads old sessiondata from the database. It is used 
 * for sessionfailover.
 * 
 * @param oldSessionId the id of the old session.
 * @return the old sessiondata.
 */
public Hashtable restoreSession(String oldSessionId) 
	throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	return dbAccess.readSession(oldSessionId);
}
/**
 * This method stores sessiondata into the database. It is used 
 * for sessionfailover.
 * 
 * @param sessionId the id of the session.
 * @param isNew determines, if the session is new or not.
 * @return data the sessionData.
 */
public void storeSession(String sessionId, Hashtable sessionData) 
	throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	// update the session
	int rowCount = dbAccess.updateSession(sessionId, sessionData);
	if (rowCount != 1) {
		// the entry dosn't exists - create it
		dbAccess.createSession(sessionId, sessionData);
	}
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

	CmsResource cmsResource = null;
	CmsFolder cmsFolder = null;
	CmsFile cmsFile = null;
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	Vector resources = dbAccess.unlockResource(currentUser, currentProject, resourcename);
	//dbAccess.unlockResource(currentUser, currentProject, resourcename);
	//m_resourceCache.clear();
	// update the cache

	for (int i=0; i < resources.size(); i++) {
		cmsResource = (CmsResource)resources.elementAt(i);
		String resourceName = cmsResource.getAbsolutePath();
		if (resourceName.endsWith("/")) {
			cmsFolder = new CmsFolder(cmsResource.getResourceId(), cmsResource.getParentId(), 
									cmsResource.getFileId(), resourceName, cmsResource.getType(), 
									cmsResource.getFlags(), cmsResource.getOwnerId(), cmsResource.getGroupId(), 
									cmsResource.getProjectId(), cmsResource.getAccessFlags(), 
									cmsResource.getState(), cmsResource.isLockedBy(), cmsResource.getDateCreated(), 
									cmsResource.getDateLastModified(), cmsResource.getResourceLastModifiedBy());

			m_resourceCache.put(C_FOLDER+currentProject.getId()+resourceName, cmsFolder);
		} else {
			cmsFile = new CmsFile(cmsResource.getResourceId(), cmsResource.getParentId(), 
									cmsResource.getFileId(), resourceName, cmsResource.getType(), 
									cmsResource.getFlags(), cmsResource.getOwnerId(), cmsResource.getGroupId(), 
									cmsResource.getProjectId(), cmsResource.getAccessFlags(), 
									cmsResource.getState(), cmsResource.isLockedBy(), cmsResource.getLauncherType(), 
									cmsResource.getLauncherClassname(),	cmsResource.getDateCreated(), 
									cmsResource.getDateLastModified(), cmsResource.getResourceLastModifiedBy(), 
									new byte[0], cmsResource.getLength());

			m_resourceCache.put(C_FILE+currentProject.getId()+resourceName, cmsFile);			
		}		
	}
	
	m_subresCache.clear();
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
/**
 * Writes the export-path for the system.
 * This path is used for db-export and db-import.
 * 
 * <B>Security:</B>
 * Users, which are in the group "administrators" are granted.<BR/>
 *  
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param mountpoint The mount point in the Cms filesystem.
 */
public void writeExportPath(CmsUser currentUser, CmsProject currentProject, String path) 
	throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;

	// check the security
	if (isAdmin(currentUser, currentProject)) {

		// security is ok - write the exportpath.
		if (dbAccess.readSystemProperty(C_SYSTEMPROPERTY_EXPORTPATH) == null) {
			// the property wasn't set before.
			dbAccess.addSystemProperty(C_SYSTEMPROPERTY_EXPORTPATH, path);
		} else {
			// overwrite the property.
			dbAccess.writeSystemProperty(C_SYSTEMPROPERTY_EXPORTPATH, path);
		}
	} else {
		throw new CmsException("[" + this.getClass().getName() + "] " + path, CmsException.C_NO_ACCESS);
	}
}
/**
 * Writes a file to the Cms.<br>
 * 
 * A file can only be written to an offline project.<br>
 * The state of the resource is set to  CHANGED (1). The file content of the file
 * is either updated (if it is already existing in the offline project), or created
 * in the offline project (if it is not available there).<br>
 * 
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can write the resource</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 * 
 * @param currentUser The user who own this file.
 * @param currentProject The project in which the resource will be used.
 * @param file The name of the file to write.
 * 
 * @exception CmsException  Throws CmsException if operation was not succesful.
 */
public void writeFile(CmsUser currentUser, CmsProject currentProject, CmsFile file) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	dbAccess.writeFile(currentProject, onlineProject(currentUser, currentProject), file, true);

	// update the cache
	m_resourceCache.put(C_FILE + currentProject.getId() + file.getAbsolutePath(), file);
	m_subresCache.clear();
	// inform about the file-system-change
	fileSystemChanged();
}
/**
 * Writes the file extensions  
 * 
 * <B>Security:</B>
 * Users, which are in the group "Administrators" are authorized.<BR/>
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param extensions Holds extensions as keys and resourcetypes (Stings) as values
 */

public void writeFileExtensions(CmsUser currentUser, CmsProject currentProject, Hashtable extensions) 
	throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	if (extensions != null) {
		if (isAdmin(currentUser, currentProject)) {
			if (dbAccess.readSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS) == null) {
				// the property wasn't set before.
				dbAccess.addSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS, extensions);
			} else {
				// overwrite the property.
				dbAccess.writeSystemProperty(C_SYSTEMPROPERTY_EXTENSIONS, extensions);
			}
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + extensions.size(), CmsException.C_NO_ACCESS);
		}
	}
}
/**
 * Writes a fileheader to the Cms.<br>
 * 
 * A file can only be written to an offline project.<br>
 * The state of the resource is set to  CHANGED (1). The file content of the file
 * is either updated (if it is already existing in the offline project), or created
 * in the offline project (if it is not available there).<br>
 * 
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can write the resource</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 * 
 * @param currentUser The user who own this file.
 * @param currentProject The project in which the resource will be used.
 * @param file The file to write.
 * 
 * @exception CmsException  Throws CmsException if operation was not succesful.
 */
public void writeFileHeader(CmsUser currentUser, CmsProject currentProject, CmsFile file) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;
	dbAccess.writeFileHeader(currentProject, file, true);

	// update the cache
	m_resourceCache.put(C_FILE + currentProject.getId() + file.getAbsolutePath(), file);
	// inform about the file-system-change
	m_subresCache.clear();
	fileSystemChanged();
}
/**
 * Updates the user information.<BR/>
 * 
 * Only the administrator can do this.<P/>
 * 
 * <B>Security:</B>
 * Only users, which are in the group "administrators" are granted.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param user The  user to be updated.
 * 
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public void writeUser(CmsUser currentUser, CmsProject currentProject, CmsUser user) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;

	// Check the security
	if (isAdmin(currentUser, currentProject) || (currentUser.equals(user))) {

		// prevent the admin to be set disabled!
		if (isAdmin(user, currentProject)) {
			user.setEnabled();
		}
		dbAccess.writeUser(user);
		// update the cache
		m_userCache.put(user.getName(), user);
	} else {
		throw new CmsException("[" + this.getClass().getName() + "] " + user.getName(), CmsException.C_NO_ACCESS);
	}
}
/**
 * Updates the user information of a web user.<BR/>
 * 
 * Only a web user can be updated this way.<P/>
 * 
 * <B>Security:</B>
 * Only users of the user type webuser can be updated this way.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param user The  user to be updated.
 * 
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public void writeWebUser(CmsUser currentUser, CmsProject currentProject, CmsUser user) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbAccess dbAccess = (com.opencms.file.oracleplsql.CmsDbAccess) m_dbAccess;

	// Check the security
	if (user.getType() == C_USER_TYPE_WEBUSER) {
		dbAccess.writeUser(user);
		// update the cache
		m_userCache.put(user.getName(), user);
	} else {
		throw new CmsException("[" + this.getClass().getName() + "] " + user.getName(), CmsException.C_NO_ACCESS);
	}
}
}

package com.opencms.file.oracleplsql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oracleplsql/Attic/CmsResourceBroker.java,v $
 * Date   : $Date: 2000/11/02 17:10:38 $
 * Version: $Revision: 1.4 $
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
 * @version $Revision: 1.4 $ $Date: 2000/11/02 17:10:38 $
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
}

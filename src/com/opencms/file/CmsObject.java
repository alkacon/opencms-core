package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsObject.java,v $
 * Date   : $Date: 2000/09/20 15:29:01 $
 * Version: $Revision: 1.113 $
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
import javax.servlet.http.*;
import source.org.apache.java.util.*;

import com.opencms.core.*;

/**
 * This class gains access to the OpenCms. 
 * <p>
 * The CmsObject encapsulates user identifaction and client request and is
 * the central object to transport information in the Cms Servlet.
 * <p>
 * All operations on the CmsObject are forwarded to the class which extends
 * A_CmsRessourceBroker to ensures user authentification in all operations.
 * 
 * @author Andreas Schouten
 * @author Michaela Schleich
 * @author Michael Emmerich
 *  
 * @version $Revision: 1.113 $ $Date: 2000/09/20 15:29:01 $ 
 * 
 */
public class CmsObject implements I_CmsConstants {
	
	/**
	 * The resource broker to access the cms.
	 */
	private I_CmsResourceBroker m_rb = null;
	
	/**
	 * The resource broker to access the cms.
	 */
	private CmsRequestContext m_context = null;
	
	/**
	 * The session storage of the cms
	 */
	private CmsCoreSession m_sessionStorage = null;

	 /**
	  * Accept a task from the Cms.
	  * 
	  * @param taskid The Id of the task to accept.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void acceptTask(int taskId)
		 throws CmsException {
		 m_rb.acceptTask(m_context.currentUser(), m_context.currentProject(), taskId);
	 }
	/**
	 * Checks, if the user may create this resource.
	 * 
	 * @param resource The resource to check.
	 * @return wether the user has access, or not.
	 */
	public boolean accessCreate(CmsResource resource) throws CmsException {
		try {
			return m_rb.accessCreate(m_context.currentUser(), m_context.currentProject(), resource);
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}			
	}
	/**
	 * Checks, if the user may lock this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @return wether the user may lock this resource, or not.
	 */
	public boolean accessLock(CmsResource resource) throws CmsException {
		try {
			return m_rb.accessLock(m_context.currentUser(), m_context.currentProject(), resource);
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}			
	}
	/**
	 * Tests if the user can access the project.
	 * 
	 * @param projectId the id of the project.
	 * 
	 * @return true, if the user has access, else returns false.
	 */
	public boolean accessProject(int projectId) 
		throws CmsException {
		return( m_rb.accessProject(m_context.currentUser(), 
								   m_context.currentProject(), projectId) );
	}
	/**
	 * Checks, if the user may read this resource.
	 * 
	 * @param resource The resource to check.
	 * @return wether the user has access, or not.
	 */
	public boolean accessRead(CmsResource resource) throws CmsException {
		try {
			return m_rb.accessRead(m_context.currentUser(), m_context.currentProject(), resource);
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}			
	}
	/**
	 * Checks, if the user may write this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @return wether the user has access, or not.
	 */
	public boolean accessWrite(CmsResource resource) throws CmsException {
		try {
			return m_rb.accessWrite(m_context.currentUser(), m_context.currentProject(), resource);
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}			
	}
	/**
	 * adds a file extension to the list of known file extensions 
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * 
	 * @param extension a file extension like 'html'
	 * @param resTypeName name of the resource type associated to the extension
	 */
	
	public void addFileExtension(String extension, String resTypeName)
		throws CmsException {
		m_rb.addFileExtension(m_context.currentUser(), m_context.currentProject(), 
							  extension, resTypeName);
	}
	/**
	 * Add a new group to the Cms.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the new group.
	 * @param description The description for the new group.
	 * @int flags The flags for the new group.
	 *
	 * @return Group
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	public CmsGroup addGroup(String name, String description, int flags, String parent)
		throws CmsException { 
		return( m_rb.addGroup(m_context.currentUser(), m_context.currentProject(),
							  name, description, flags, parent) );

	}
	/**
	 * Adds a CmsResourceTypes.
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * 
	 * @param resourceType the name of the resource to get.
	 * @param launcherType the launcherType-id
	 * @param launcherClass the name of the launcher-class normaly ""
	 * 
	 * Returns a CmsResourceTypes.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public CmsResourceType addResourceType(String resourceType, int launcherType, 
											 String launcherClass) 
		throws CmsException {
		return( m_rb.addResourceType(m_context.currentUser(), 
									 m_context.currentProject(), resourceType,
									 launcherType, launcherClass) );
	}
	/** 
	 * Adds a user to the Cms.
	 * 
	 * Only a adminstrator can add users to the cms.
	 * 
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
	public CmsUser addUser(String name, String password, String group, 
							 String description, Hashtable additionalInfos, int flags)
		throws CmsException { 
		
		return( m_rb.addUser(m_context.currentUser(), m_context.currentProject(),
							  name, password, group, description, additionalInfos, 
							  flags) );

	}
	/**
	 * Adds a user to a group.<BR/>
	 *
	 * Only the admin can do this.
	 * 
	 * @param username The name of the user that is to be added to the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	public void addUserToGroup(String username, String groupname)
		throws CmsException { 
		m_rb.addUserToGroup(m_context.currentUser(), m_context.currentProject(), 
							username, groupname );
	}
	 /** 
	 * Adds a user to the Cms.
	 * 
	 * 
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
	public CmsUser addWebUser(String name, String password, String group, 
							 String description, Hashtable additionalInfos, int flags)
		throws CmsException { 
		
		return( m_rb.addWebUser(m_context.currentUser(), m_context.currentProject(),
							  name, password, group, description, additionalInfos, 
							  flags) );
	}
	/**
	 * Returns the anonymous user object.
	 * 
	 * @return the anonymous user object.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsUser anonymousUser() 
		throws CmsException {
		return( m_rb.anonymousUser(m_context.currentUser(), 
								   m_context.currentProject()) );
	}
	/**
	 * Changes the group for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * @param filename The complete path to the resource.
	 * @param newGroup The new of the new group for this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if the newGroup doesn't exists.
	 */
	public void chgrp(String filename, String newGroup)
		throws CmsException { 
		m_rb.chgrp(m_context.currentUser(), m_context.currentProject(), 
				   filename, newGroup );
	}
	/**
	 * Changes the flags for this resource<BR/>
	 * 
	 * The user may change the flags, if he is admin of the resource.
	 * 
	 * @param filename The complete path to the resource.
	 * @param flags The new flags for the resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public void chmod(String filename, int flags)
		throws CmsException { 
		m_rb.chmod(m_context.currentUser(), m_context.currentProject(), 
				   filename, flags );
	}
	/**
	 * Changes the owner for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * @param filename The complete path to the resource.
	 * @param newOwner The name of the new owner for this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if the newOwner doesn't exists.
	 */
	public void chown(String filename, String newOwner)
		throws CmsException { 
		m_rb.chown(m_context.currentUser(), m_context.currentProject(), 
				   filename, newOwner );
	}
	 /**
	 * Changes the state for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * @param filename The complete path to the resource.
	 * @param state The new state of this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if the newOwner doesn't exists.
	 */
	public void chstate(String filename, int state)
		throws CmsException { 
		m_rb.chstate(m_context.currentUser(), m_context.currentProject(), 
				   filename, state );
	}
	 /**
	 * Changes the resourcetype for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * @param filename The complete path to the resource.
	 * @param newType The name of the new resourcetype for this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if the newType doesn't exists.
	 */
	public void chtype(String filename, String newType)
		throws CmsException { 
		m_rb.chtype(m_context.currentUser(), m_context.currentProject(), 
				   filename, newType );
	}
	/**
	 * Clears all internal DB-Caches.
	 */
	public void clearcache() {
		m_rb.clearcache();
	}
	/**
	 * Copies the file.
	 * 
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfolder.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be copied. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void copyFile(String source, String destination)
		throws CmsException { 
		m_rb.copyFile(m_context.currentUser(), m_context.currentProject(), 
					  source, destination);
	}
	 /**
	 * Copies a folder.
	 * 
	 * @param source The complete path of the sourcefolder.
	 * @param destination The complete path of the destinationfolder.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be copied. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void copyFolder(String source, String destination)
		throws CmsException { 
		m_rb.copyFolder(m_context.currentUser(), m_context.currentProject(), 
					  source, destination);
	}
	/**
	 * Copies a resource from the online project to a new, specified project.<br>
	 * Copying a resource will copy the file header or folder into the specified 
	 * offline project and set its state to UNCHANGED.
	 * 
	 * @param resource The name of the resource.
 	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void copyResourceToProject(String resource)
		throws CmsException {
		m_rb.copyResourceToProject(m_context.currentUser(), 
								   m_context.currentProject(), resource );
		
	}
	/**
	 * Returns a copyright-string for this OpenCms.
	 * 
	 * @return copyright A copyright-string.
	 */
	 public String[] copyright() {
		 return C_COPYRIGHT;
	 }
	/**
	 * Counts the locked resources in this project.
	 * 
	 * @param id The id of the project
	 * @return the amount of locked resources in this project.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public int countLockedResources(int id)
		throws CmsException {
		return m_rb.countLockedResources(m_context.currentUser(), m_context.currentProject(), id);
	}
	/**
	 * Creates a new file with the overgiven content and resourcetype.
	 * If there are some mandatory Propertydefinitions for the resourcetype, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory Properties.<BR/>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * 
	 * @param folder The complete path to the folder in which the file will be created.
	 * @param filename The name of the new file (No pathinformation allowed).
	 * @param contents The contents of the new file.
	 * @param type The resourcetype of the new file.
	 * 
	 * @return file The created file.
	 * 
	 * @exception CmsException will be thrown for missing Properties or if 
	 * resourcetype is set to folder. The CmsException is also thrown, if the 
	 * filename is not valid. The CmsException will also be thrown, if the user
	 * has not the rights for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * this name.
	 */
	public CmsFile createFile(String folder, String filename, 
							  byte[] contents, String type)
		throws CmsException { 
		return( m_rb.createFile(m_context.currentUser(), m_context.currentGroup(),
								m_context.currentProject(), 
								folder, filename, contents, type, 
								new Hashtable() ) );
	}
	/**
	 * Creates a new file with the overgiven content and resourcetype.
	 * If some mandatory Propertydefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory Properties.<BR/>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * 
	 * @param folder The complete path to the folder in which the file will be created.
	 * @param filename The name of the new file (No pathinformation allowed).
	 * @param contents The contents of the new file.
	 * @param type The resourcetype of the new file.
	 * @param properties A Hashtable of Properties, that should be set for this file.
	 * The keys for this Hashtable are the names for Propertydefinitions, the values are
	 * the values for the Properties.
	 * 
	 * @return file The created file.
	 * 
	 * @exception CmsException will be thrown for missing Properties, for worng Propertydefs
	 * or if resourcetype is set to folder. The CmsException is also thrown, if the 
	 * filename is not valid. The CmsException will also be thrown, if the user
	 * has not the rights for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * this name.
	 */
	public CmsFile createFile(String folder, String filename, byte[] contents, String type, 
							  Hashtable properties)
		throws CmsException { 
		return( m_rb.createFile(m_context.currentUser(), m_context.currentGroup(), 
								m_context.currentProject(), 
								folder, filename, contents, type, 
								properties ) );
	}
	/**
	 * Creates a new folder.
	 * If there are some mandatory Propertydefinitions for the folder-resourcetype, a 
	 * CmsException will be thrown, because the folder cannot be created without
	 * the mandatory Properties.<BR/>
	 * 
	 * @param folder The complete path to the folder in which the new folder 
	 * will be created.
	 * @param newFolderName The name of the new folder (No pathinformation allowed).
	 * 
	 * @return folder The created folder.
	 * 
	 * @exception CmsException will be thrown for missing properties.
	 * The CmsException is also thrown, if the foldername is not valid. 
	 * The CmsException will also be thrown, if the user has not the rights for 
	 * this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * this name.
	 */
	public CmsFolder createFolder(String folder, String newFolderName)
		throws CmsException { 
		return( m_rb.createFolder(m_context.currentUser(), m_context.currentGroup(), 
								  m_context.currentProject(), folder, 
								  newFolderName, new Hashtable() ) );
	}
	/**
	 * Creates a new folder.
	 * If some mandatory Propertydefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory Properties.<BR/>
	 * 
	 * @param folder The complete path to the folder in which the new folder will 
	 * be created.
	 * @param newFolderName The name of the new folder (No pathinformation allowed).
	 * @param properties A Hashtable of properties, that should be set for this folder.
	 * The keys for this Hashtable are the names for Propertydefinitions, the values are
	 * the values for the properties.
	 * 
	 * @return file The created file.
	 * 
	 * @exception CmsException will be thrown for missing properties, for worng Propertydefs
	 * or if the filename is not valid. The CmsException will also be thrown, if the 
	 * user has not the rights for this resource.
	 */
	public CmsFolder createFolder(String folder, String newFolderName, 
								  Hashtable properties)
		throws CmsException { 
		return( m_rb.createFolder(m_context.currentUser(), m_context.currentGroup(), 
								  m_context.currentProject(), folder, 
								  newFolderName, properties ) );
	}
	 /**
	  * Creates a new project for task handling.
	  * 
	  * @param projectname Name of the project
	  * @param projectType Type of the Project
	  * @param role Usergroup for the project
	  * @param timeout Time when the Project must finished
	  * @param priority Priority for the Project
	  * 
	  * @return The new task project
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public CmsTask createProject(String projectname, int projectType,
									String roleName, long timeout, 
									int priority)
		 throws CmsException {
						 
		 return m_rb.createProject(m_context.currentUser(), projectname, projectType, 
									   roleName, timeout, priority);
	 }
	/**
	 * Creates a project.
	 * 
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param groupname the name of the group to be set.
	 * @param managergroupname the name of the managergroup to be set.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject createProject(String name, String description, 
									   String groupname, String managergroupname)
		 throws CmsException {
		 return( m_rb.createProject(m_context.currentUser(), 
									m_context.currentProject(), name, description, 
									groupname, managergroupname) );
	 }
	/**
	 * Creates the Propertydefinition for the resource type.<BR/>
	 * 
	 * @param name The name of the Propertydefinition to overwrite.
	 * @param resourcetype The name of the resource-type for the Propertydefinition.
	 * @param type The type of the Propertydefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition createPropertydefinition(String name, String resourcetype, 
													int type)
		throws CmsException {
		return( m_rb.createPropertydefinition(m_context.currentUser(), 
										  m_context.currentProject(), 
										  name,
										  resourcetype,
										  type) );
	}
	 /**
	  * Creates a new task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param projectid The Id of the current project task of the user.
	  * @param agentname User who will edit the task 
	  * @param rolename Usergroup for the task
	  * @param taskname Name of the task
	  * @param tasktype Type of the task 
	  * @param taskcomment Description of the task
	  * @param timeout Time when the task must finished
	  * @param priority Id for the priority
	  * 
	  * @return A new Task Object
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public CmsTask createTask(int projectid, String agentName, String roleName, 
								 String taskname, String taskcomment, int tasktype,
								 long timeout, int priority)
		 throws CmsException {
	 
		 return m_rb.createTask(m_context.currentUser(), projectid, agentName, roleName,
									taskname, taskcomment, tasktype, timeout, priority);
	 }
	 /**
	  * Creates a new task.
	  * 
	  * @param agent User who will edit the task 
	  * @param role Usergroup for the task
	  * @param taskname Name of the task
	  * @param taskcomment Description of the task
	  * @param timeout Time when the task must finished
	  * @param priority Id for the priority
	  * 
	  * @return A new Task Object
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 
	 public CmsTask createTask(String agentName, String roleName, 
								 String taskname, String taskcomment, 
								 long timeout, int priority)
		 throws CmsException {
		 return( m_rb.createTask(m_context.currentUser(), m_context.currentProject(),
								 agentName, roleName, taskname, taskcomment, 
								 timeout, priority) );
	 }
	/**
	 * Deletes all Property for a file or folder.
	 * 
	 * @param resourcename The resource-name of which the Property has to be delteted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllProperties(String resourcename)
		throws CmsException { 
		m_rb.deleteAllProperties(m_context.currentUser(), 
									   m_context.currentProject(), 
									   resourcename);
	}
	/**
	 * Deletes the file.
	 * 
	 * @param filename The complete path of the file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be deleted. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void deleteFile(String filename)
		throws CmsException { 
		m_rb.deleteFile(m_context.currentUser(), m_context.currentProject(), 
						filename);
	}
	/**
	 * Deletes the folder.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * delted, too.
	 * 
	 * @param foldername The complete path of the folder.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be deleted. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void deleteFolder(String foldername)
		throws CmsException { 
		m_rb.deleteFolder(m_context.currentUser(), m_context.currentProject(), 
						  foldername );
	}
	/**
	 * Delete a group from the Cms.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param delgroup The name of the group that is to be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	public void deleteGroup(String delgroup)
		throws CmsException { 
		m_rb.deleteGroup(m_context.currentUser(), m_context.currentProject(), 
						 delgroup);
	}
	/**
	 * Deletes a project.
	 * 
	 * @param id The id of the project to be published.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deleteProject(int id)
		throws CmsException {
		m_rb.deleteProject(m_context.currentUser(), m_context.currentProject(), id);
	}
	/**
	 * Delete the Propertydefinition for the resource type.<BR/>
	 * 
	 * @param name The name of the Propertydefinition to overwrite.
	 * @param resourcetype The name of the resource-type for the Propertydefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deletePropertydefinition(String name, String resourcetype)
		throws CmsException { 
		m_rb.deletePropertydefinition(m_context.currentUser(), 
								  m_context.currentProject(), 
								  name, resourcetype);
	}
	/**
	 * Deletes a Property for a file or folder.
	 * 
	 * @param resourcename The resource-name of which the Property has to be delteted.
	 * @param property The Propertydefinition-name of which the Property has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteProperty(String resourcename, String property)
		throws CmsException { 
		m_rb.deleteProperty(m_context.currentUser(), 
								   m_context.currentProject(), 
								   resourcename, property);
	}
	/** 
	 * Deletes a user from the Cms.
	 * 
	 * Only a adminstrator can do this.
	 * 
	 * @param name The Id of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void deleteUser(int userId)
		throws CmsException { 
		m_rb.deleteUser(m_context.currentUser(), m_context.currentProject(), userId);
	}
	/** 
	 * Deletes a user from the Cms.
	 * 
	 * Only a adminstrator can do this.
	 * 
	 * @param name The name of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void deleteUser(String username)
		throws CmsException { 
		m_rb.deleteUser(m_context.currentUser(), m_context.currentProject(), username);
	}
 	/** 
	 * Deletes a web user from the Cms.
	 * 
	 * @param name The Id of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void deleteWebUser(int userId)
		throws CmsException { 
		m_rb.deleteWebUser(m_context.currentUser(), m_context.currentProject(), userId);
	}
	/**
	 * Destroys the resource borker and required modules and connections.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void destroy() 
		throws CmsException {
		m_rb.destroy();
	}
	 /**
	  * Ends a task from the Cms.
	  * 
	  * @param taskid The ID of the task to end.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void endTask(int taskid) 
		 throws CmsException {
		 m_rb.endTask(m_context.currentUser(), m_context.currentProject(), taskid);
	 }
	/**
	 * Exports cms-resources to zip.
	 * 
	 * @param exportFile the name (absolute Path) of the export resource (zip)
	 * @param exportPath the name (absolute Path) of folder from which should be exported
	 * 
	 * @exception Throws CmsException if something goes wrong.
	 */
	public void exportResources(String exportFile, String[] exportPaths)
		throws CmsException {
		// export the resources
		m_rb.exportResources(m_context.currentUser(), m_context.currentProject(), exportFile, exportPaths, this);
	}
	/**
	 * Exports cms-resources to zip.
	 * 
	 * @param exportFile the name (absolute Path) of the export resource (zip)
	 * @param exportPath the name (absolute Path) of folder from which should be exported
	 * @param includeSystem, desides if to include the system resources to the export.
	 * 
	 * @exception Throws CmsException if something goes wrong.
	 */
	public void exportResources(String exportFile, String[] exportPaths, boolean includeSystem)
		throws CmsException {
		// export the resources
		m_rb.exportResources(m_context.currentUser(), m_context.currentProject(), exportFile, exportPaths, this, includeSystem);
	}
	 /**
	  * Forwards a task to a new user.
	  * 
	  * @param taskid The Id of the task to forward.
	  * @param newRole The new Group for the task
	  * @param newUser The new user who gets the task.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void forwardTask(int taskid, String newRoleName, String newUserName) 
		 throws CmsException {
		 m_rb.forwardTask(m_context.currentUser(), m_context.currentProject(), taskid,
						  newRoleName, newUserName);
	 }
	/**
	 * Returns all projects, which the user may access.
	 * 
	 * @return a Vector of projects.
	 */
	public Vector getAllAccessibleProjects() 
		throws CmsException {
		return( m_rb.getAllAccessibleProjects(m_context.currentUser(), 
											  m_context.currentProject()) );
	}
	/**
	 * Returns all projects, which are owned by the user or which are manageable
	 * for the group of the user.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllManageableProjects()
		 throws CmsException {
		 return( m_rb.getAllManageableProjects(m_context.currentUser(), 
											   m_context.currentProject()) );

	 }
	/**
	 * Returns a Vector with all I_CmsResourceTypes.
	 * 
	 * Returns a Vector with all I_CmsResourceTypes.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Hashtable getAllResourceTypes() 
		throws CmsException { 
		return( m_rb.getAllResourceTypes(m_context.currentUser(), 
										 m_context.currentProject()) );
	}
/**
 * Returns all sites in a vector
 * 
 * @return all sites.
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public Vector getAllSites() throws CmsException
{
	return m_rb.getAllSites(m_context.currentUser(), m_context.currentProject());
}
	public Hashtable getCacheInfo() {
		return m_rb.getCacheInfo();
	}
	/**
	 * Returns all child groups of a group<P/>
	 * 
	 * @param groupname The name of the group.
	 * @return groups A Vector of all child groups or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getChild(String groupname)
		throws CmsException {
		return( m_rb.getChild(m_context.currentUser(), m_context.currentProject(), 
							  groupname ) );
	}
	/**
	 * Returns all child groups of a group<P/>
	 * This method also returns all sub-child groups of the current group.
	 * 
	 * @param groupname The name of the group.
	 * @return groups A Vector of all child groups or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getChilds(String groupname)
		throws CmsException {
		return( m_rb.getChilds(m_context.currentUser(), m_context.currentProject(), 
							   groupname ) );
	}
	/**
	 * Method to access the configurations of the properties-file.
	 * @return The Configurations of the properties-file.
	 */
	public Configurations getConfigurations() {
		return m_rb.getConfigurations(getRequestContext().currentUser(),
									  getRequestContext().currentProject());
	}
	/**
	 * Gets all groups to which the user directly belongs to
	 * 
	 * @param username The name of the user to get all groups from.
	 * @return all groups of a user.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getDirectGroupsOfUser(String username)
		throws CmsException { 
		return( m_rb.getDirectGroupsOfUser(m_context.currentUser(),
										   m_context.currentProject(), username ));
	}
	/**
	 * Returns a Vector with all subfiles.<BR/>
	 * 
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfiles A Vector with all subfiles for the overgiven folder.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public Vector getFilesInFolder(String foldername)
		throws CmsException { 
		return( m_rb.getFilesInFolder(m_context.currentUser(), m_context.currentProject(),
									  foldername) );
	}
/**
 * Returns a Vector with all resource-names that have set the given property to the given value.
 * 
 * @param propertydef, the name of the propertydefinition to check.
 * @param property, the value of the property for the resource.
 * 
 * @return Vector with all names of resources.
 * 
 * @exception CmsException Throws CmsException if operation was not succesful.
 */
public Vector getFilesWithProperty(String propertyDefinition, String propertyValue) throws CmsException {
	return m_rb.getFilesWithProperty(m_context.currentUser(), m_context.currentProject(), propertyDefinition, propertyValue);
}
	/**
	 * This method can be called, to determine if the file-system was changed 
	 * in the past. A module can compare its previosly stored number with this
	 * returned number. If they differ, a change was made.
	 * 
	 * @return the number of file-system-changes.
	 */
	 public long getFileSystemChanges() {
		return( m_rb.getFileSystemChanges(m_context.currentUser(), 
										  m_context.currentProject()) );
	 }
	/**
	 * Returns all groups in the Cms.
	 *  
	 * @return all groups in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Vector getGroups() 
		throws CmsException { 
		return( m_rb.getGroups(m_context.currentUser(), m_context.currentProject()) );
	}
	/**
	 * Gets all groups of a user.
	 * 
	 * @param username The name of the user to get all groups from.
	 * @return all groups of a user.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getGroupsOfUser(String username)
		throws CmsException { 
		return( m_rb.getGroupsOfUser(m_context.currentUser(), 
									 m_context.currentProject(), username ));
	}
	/**
	 * Returns the parent group of a group<P/>
	 * 
	 * @param groupname The name of the group.
	 * @return group The parent group or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup getParent(String groupname)
		throws CmsException {
		return( m_rb.getParent(m_context.currentUser(), m_context.currentProject(), 
							   groupname ) );
	}
	 /**
	 * Gets the Registry.<BR/>
	 *
	 *
	 * @exception Throws CmsException if access is not allowed.
	 */

	 public I_CmsRegistry getRegistry()
	 	throws CmsException {
	    return( m_rb.getRegistry(m_context.currentUser(), 
								 m_context.currentProject(),this ));
	 }
	/**
	 * Returns the current request-context.
	 * 
	 * @return the current request-context.
	 */
	public CmsRequestContext getRequestContext() {
		return( m_context );
	}
	/**
	 * Returns a I_CmsResourceTypes.
	 * 
	 * @param resourceType the id of the resource to get.
	 * 
	 * @return a CmsResourceTypes.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public CmsResourceType getResourceType(int resourceType) 
		throws CmsException {
		return( m_rb.getResourceType(m_context.currentUser(), 
									 m_context.currentProject(), resourceType) );
	}
	/**
	 * Returns a I_CmsResourceTypes.
	 * 
	 * @param resourceType the name of the resource to get.
	 * 
	 * @return a CmsResourceTypes.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public CmsResourceType getResourceType(String resourceType) 
		throws CmsException {
		return( m_rb.getResourceType(m_context.currentUser(), 
									 m_context.currentProject(), resourceType) );
	}
	/**
	 * Returns the session storage.
	 * @return The storage of all active users.
	 */
	public CmsCoreSession getSessionStorage() {
		return m_rb.getSessionStorage(m_context.currentUser(),m_sessionStorage);
	}
/**
 * Get the site based on the url
 *
 * @return com.opencms.file.CmsSite the site found.
 * @param url java.lang.StringBuffer the Url on wn which the lookup should be based.
 * @exception com.opencms.core.CmsException if an error occurs. The CmsException should be specialized.
 */
public CmsSite getSiteFromUrl(StringBuffer url) throws com.opencms.core.CmsException
{
	return m_rb.getSiteFromUrl(m_context.currentUser(),m_context.currentProject(),url);
}
/**
 * Return the online site of the project (the base project)
 * @return com.opencms.file.CmsSite the site for the project.
 * @param projectId int the project to be used in the lookup.
 * @throws CmsException If any error occured when looking up the site.
 */
 
public CmsSite getSite(int projectId) throws CmsException {
	return m_rb.getSite(m_context.currentUser(),m_context.currentProject(),projectId);
}
/**
 * Get the site based on the url
 *
 * @return com.opencms.file.CmsSite the site found.
 * @param url java.lang.StringBuffer the Url on wn which the lookup should be based.
 * @exception com.opencms.core.CmsException if an error occurs. The CmsException should be specialized.
 */
public CmsSite getSite(StringBuffer url) throws com.opencms.core.CmsException
{
	//return a dummy site.
	String host = null;
	try
	{
		java.net.URL siteUrl = new java.net.URL(url.toString());
		host = siteUrl.getHost();
	}
	catch (java.net.MalformedURLException mue)
	{
		//the StringBuffer was an illigal URL - we should throw an exception.
		host = "Unknown";
	}
	return getSite(host);
}
/**
 * Get the site based on the name of the site.
 *
 * @return com.opencms.file.CmsSite the site found.
 * @param url java.lang.String the name of the site to find.
 * @exception com.opencms.core.CmsException if an error occurs. The CmsException should be specialized.
 */
public CmsSite getSite(String siteName) throws CmsException {
	return m_rb.getSite(m_context.currentUser(),m_context.currentProject(),siteName);

}
	/**
	 * Returns a Vector with all subfolders.<BR/>
	 * 
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfolders A Vector with all subfolders for the overgiven folder.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public Vector getSubFolders(String foldername)
		throws CmsException { 
		return( m_rb.getSubFolders(m_context.currentUser(), 
								   m_context.currentProject(), foldername ) );
	}
	 /**
	  * Get a parameter value for a task.
	  * 
	  * @param taskid The Id of the task.
	  * @param parname Name of the parameter.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public String getTaskPar(int taskid, String parname)
		 throws CmsException {
		 return( m_rb.getTaskPar(m_context.currentUser(), m_context.currentProject(),
								 taskid, parname) );
	 }
	/**
	 * Get the template task id fo a given taskname.
	 * 
	 * @param taskname Name of the Task
	 * 
	 * @return id from the task template
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public int getTaskType(String taskname)
		throws CmsException {
			 return  m_rb.getTaskType(taskname);		
	}
	/**
	 * Returns all users of the type in the Cms.
	 * 
	 * @param type The type of the users.
	 * 
	 * @return all users of the type in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Vector getUsers(int type) 
		throws CmsException { 
		return( m_rb.getUsers(m_context.currentUser(), m_context.currentProject(), type));
	}
	/**
	 * Returns all users in the Cms.
	 *  
	 * @return all users in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Vector getUsers() 
		throws CmsException { 
		return( m_rb.getUsers(m_context.currentUser(), m_context.currentProject()) );
	}
	/**
	 * Gets all users in the group.
	 * 
	 * @param groupname The name of the group to get all users from.
	 * @return all users in the group.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getUsersOfGroup(String groupname)
		throws CmsException { 
		return( m_rb.getUsersOfGroup(m_context.currentUser(), 
									 m_context.currentProject(), groupname ));
	}
	/**
	 * Imports a import-resource (folder or zipfile) to the cms.
	 * 
	 * @param importFile the name (absolute Path) of the import resource (zip or folder)
	 * @param importPath the name (absolute Path) of folder in which should be imported
	 * 
	 * @exception Throws CmsException if something goes wrong.
	 */
	public void importFolder(String importFile, String importPath)
		throws CmsException {
		// import the resources
		clearcache();
		m_rb.importFolder(m_context.currentUser(), m_context.currentProject(), importFile, importPath, this);
		clearcache();
	}
	 // database import, export stuff

	/**
	 * Imports a import-resource (folder or zipfile) to the cms.
	 * 
	 * @param importFile the name (absolute Path) of the import resource (zip or folder)
	 * @param importPath the name (absolute Path) of folder in which should be imported
	 * 
	 * @exception Throws CmsException if something goes wrong.
	 */
	public void importResources(String importFile, String importPath)
		throws CmsException {
		// import the resources
		clearcache();
		m_rb.importResources(m_context.currentUser(), m_context.currentProject(), importFile, importPath, this);
		clearcache();
	}
	/**
	 * Initialises the CmsObject for each request.
	 * 
	 * @param broker the resourcebroker to access the database.
	 * @param req the CmsRequest.
	 * @param resp the CmsResponse.
	 * @param user The current user for this request.
	 * @param currentGroup The current group for this request.
	 * @param currentProjectId The current projectId for this request.
	 */
	public void init(I_CmsResourceBroker broker, I_CmsRequest req, I_CmsResponse resp, 
					 String user, String currentGroup, int currentProjectId ) 
		throws CmsException {
		m_rb = broker;
		m_context = new CmsRequestContext();
		m_context.init(m_rb, req, resp, user, currentGroup, currentProjectId);
	}
	/**
	 * Initialises the CmsObject without a request-context (current-user, 
	 * current-group, current-project).
	 * 
	 * @param broker the resourcebroker to access the database.
	 */
	public void init(I_CmsResourceBroker broker ) 
		throws CmsException {
		m_rb = broker;
	}
	/**
	 * Determines, if the users current group is the admin-group.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @return true, if the users current group is the admin-group, 
	 * else it returns false.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	public boolean isAdmin() 
		throws CmsException {
		return m_rb.isAdmin(getRequestContext().currentUser(), getRequestContext().currentProject());
	}
	/**
	 * Contructor 
	 * @param storage The reference to the session storage.
	 */
	public CmsObject (CmsCoreSession storage) {
		m_sessionStorage=storage;
	}
	/**
	 * Returns the user, who had locked the resource.<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource. This methods checks, if a resource was locked.
	 * 
	 * @param resource The resource.
	 * 
	 * @return true, if the resource is locked else it returns false.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. 
	 */
	public CmsUser lockedBy(CmsResource resource)
		throws CmsException {
		return( m_rb.lockedBy(m_context.currentUser(), m_context.currentProject(), 
							  resource) );
	}
	/**
	 * Returns the user, who had locked the resource.<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource. This methods checks, if a resource was locked.
	 * 
	 * @param resource The complete path to the resource.
	 * 
	 * @return true, if the resource is locked else it returns false.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. 
	 */
	public CmsUser lockedBy(String resource)
		throws CmsException {
		return( m_rb.lockedBy(m_context.currentUser(), m_context.currentProject(), 
							  resource) );
	}
	/**
	 * Locks a resource<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource.
	 * 
	 * @param resource The complete path to the resource to lock.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if there is a existing lock
	 * and force was set to false.
	 */
	public void lockResource(String resource)
		throws CmsException { 
		// try to lock the resource, prevent from overwriting an existing lock
		lockResource(resource, false);
	}
	/**
	 * Locks a resource<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource.
	 * 
	 * @param resource The complete path to the resource to lock.
	 * @param force If force is true, a existing locking will be oberwritten.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if there is a existing lock
	 * and force was set to false.
	 */
	public void lockResource(String resource, boolean force)
		throws CmsException { 
		m_rb.lockResource(m_context.currentUser(), 
					  m_context.currentProject(), resource, force );
	}
	/**
	 * Logs a user into the Cms, if the password is correct.
	 * 
	 * @param username The name of the user to be returned.
	 * @param password The password of the user to be returned.
	 * @return the name of the logged in user.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String loginUser(String username, String password) 
		throws CmsException { 
		// login the user
		CmsUser newUser = m_rb.loginUser(m_context.currentUser(), 
										   m_context.currentProject(),
										   username, password);
		// init the new user
		init(m_rb, m_context.getRequest(), m_context.getResponse(), newUser.getName(), 
			 newUser.getDefaultGroup().getName(), C_PROJECT_ONLINE_ID);
		// return the user-name
		return(newUser.getName());
	}
	 /**
	 * Logs a web user into the Cms, if the password is correct.
	 * 
	 * @param username The name of the user to be returned.
	 * @param password The password of the user to be returned.
	 * @return the name of the logged in user.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String loginWebUser(String username, String password) 
		throws CmsException { 
		// login the user
		CmsUser newUser = m_rb.loginWebUser(m_context.currentUser(), 
										   m_context.currentProject(),
										   username, password);
		// init the new user
		init(m_rb, m_context.getRequest(), m_context.getResponse(), newUser.getName(), 
			 newUser.getDefaultGroup().getName(), C_PROJECT_ONLINE_ID);
		// return the user-name
		return(newUser.getName());
	}
	/**
	 * Moves the file.
	 * 
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be moved. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * the destination filename.
	 */	
	public void moveFile(String source, String destination)
		throws CmsException { 
		m_rb.moveFile(m_context.currentUser(), m_context.currentProject(), 
					  source, destination );
	}
	/**
	 * Moves the folder.
	 * 
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be moved. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * the destination foldername.
	 */	
	 public void moveFolder(String source, String destination)
		throws CmsException {
		m_rb.moveFolder(m_context.currentUser(), m_context.currentProject(), 
					    source, destination );
	 }
/**
 * Creates a new Site in the OpenCms system based on the parameters given. <br>
 * This includes:<br>
 * 1) Creating a new online-project for the site.<br>
 * 2) Creating a single site_url record connecting the given url to the new site.<br>
 * 3) Creating a site_project record linking the new site to the new onlineproject.
 *
 * Creation date: (09/20/00 %r)
 *
 * @return com.opencms.file.CmsSite
 * @param Name java.lang.String
 * @param Description java.lang.String
 * @param Category int
 * @param Language int
 * @param Country int
 * @param url java.lang.String
 * @param user java.lang.String
 * @param group java.lang.String
 */
public CmsSite newSite(String Name, String Description, int Category, int Language, int Country, String url, String user, String group) throws CmsException
{
	return m_rb.newSite(Name, Description, Category, Language, Country, url, user, group, m_context.currentUser(), m_context.currentProject() );

}
	/**
	 * Returns the onlineproject. This is the default project. All anonymous 
	 * (or guest) user will see the rersources of this project.
	 * 
	 * @return the onlineproject object.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsProject onlineProject() 
		throws CmsException {
		return( m_rb.onlineProject(m_context.currentUser(), 
								   m_context.currentProject()) );
	}
	/**
	 * Publishes a project.
	 * 
	 * @param id The id of the project to be published.
	 * @return A Vector of resources, that were changed.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void publishProject(int id)
		throws CmsException { 
		 clearcache();
		 m_rb.publishProject(m_context.currentUser(), 
							 m_context.currentProject(), id);
		 clearcache();
	}
	/**
	 * Reads the agent of a task from the OpenCms.
	 * 
	 * @param task The task to read the agent from.
	 * @return The owner of a task.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readAgent(CmsTask task) 
		throws CmsException {
		return( m_rb.readAgent(m_context.currentUser(), m_context.currentProject(), 
							   task ) );
	}
	 /**
	 * Reads all file headers of a file in the OpenCms.<BR>
	 * This method returns a vector with the histroy of all file headers, i.e. 
	 * the file headers of a file, independent of the project they were attached to.<br>
	 * 
	 * The reading excludes the filecontent.
	 * 
	 * @param filename The name of the file to be read.
	 * 
	 * @return Vector of file headers read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	 public Vector readAllFileHeaders(String filename)
		 throws CmsException {
		 return( m_rb.readAllFileHeaders(m_context.currentUser(), 
										 m_context.currentProject(),
										 filename) );
	 }
	/**
	 * Returns a list of all Properties of a file or folder.
	 * 
	 * @param name The resource-name of which the Property has to be read
	 * 
	 * @return Vector of Property as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Hashtable readAllProperties(String name)
		throws CmsException { 
		return( m_rb.readAllProperties(m_context.currentUser(), 
											 m_context.currentProject(), 
											 name) );
	}
	 /**
	 * Reads all Propertydefinitions for the given resource type.
	 * 
	 * @param id The id of the resource type to read the  Propertydefinitions for.
	 * @param type The type of the Propertydefinition (normal|mandatory|optional).
	 * 
	 * @return propertydefinitions A Vector with Propertydefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllPropertydefinitions(int id, int type)
		throws CmsException {
		return( m_rb.readAllPropertydefinitions(m_context.currentUser(), 
											    m_context.currentProject(), 
											    id, type ) );
	}
	/**
	 * Reads all Propertydefinitions for the given resource type.
	 * 
	 * @param resourcetype The name of the resource type to read the 
	 * Propertydefinitions for.
	 * @param type The type of the Propertydefinition (normal|mandatory|optional).
	 * 
	 * @return propertydefinitions A Vector with Propertydefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllPropertydefinitions(String resourcetype, int type)
		throws CmsException {
		return( m_rb.readAllPropertydefinitions(m_context.currentUser(), 
											    m_context.currentProject(), 
											    resourcetype, type ) );
	}
	/**
	 * Reads all Propertydefinitions for the given resource type.
	 * 
	 * @param resourcetype The name of the resource type to read the 
	 * Propertydefinitions for.
	 * 
	 * @return propertydefinitions A Vector with Propertydefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllPropertydefinitions(String resourcetype)
		throws CmsException {
		return( m_rb.readAllPropertydefinitions(m_context.currentUser(), 
											m_context.currentProject(), 
											resourcetype ) );
	}
	/**
	 * Reads the export-path for the system.
	 * This path is used for db-export and db-import.
	 * 
	 * @return the exportpath.
	 */
	public String readExportPath()
		throws CmsException {
		return m_rb.readExportPath(m_context.currentUser(), m_context.currentProject());
	}
	/**
	 * Gets the known file extensions (=suffixes) 
	 * 
	 * <B>Security:</B>
	 * All users are granted access<BR/>
	 * 
	 * @return Hashtable with file extensions as Strings
	 */
	
	public Hashtable readFileExtensions()
		throws CmsException{
		return m_rb.readFileExtensions(null, null);
	}
	/**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent.
	 * 
	 * @param filename The complete path of the file to be read.
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public CmsResource readFileHeader(String filename)
		throws CmsException { 
	 	return( m_rb.readFileHeader(m_context.currentUser(), 
									m_context.currentProject(), 
									filename ) );
	}
	/**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent.
	 * 
	 * @param folder The complete path to the folder from which the file will be read.
	 * @param filename The name of the file to be read.
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public CmsResource readFileHeader(String folder, String filename)
		throws CmsException { 
	 	return( m_rb.readFileHeader(m_context.currentUser(), 
									m_context.currentProject(), 
									folder + filename ) );
	}
	/**
	 * Reads all file headers for a project from the Cms.<BR/>
	 * 
	 * @param projectId The id of the project to read the resources for.
	 * 
	 * @return a Vector of resources.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public Vector readFileHeaders(int projectId)
		throws CmsException { 
	 	return( m_rb.readFileHeaders(m_context.currentUser(), 
									 m_context.currentProject(), 
									 projectId ) );
	}
	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * @param filename The complete path to the file
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public CmsFile readFile(String filename)
		throws CmsException { 
   		return( m_rb.readFile(m_context.currentUser(), 
							  m_context.currentProject(), 
							  filename ) );
	}
	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * @param folder The complete path to the folder from which the file will be read.
	 * @param filename The name of the file to be read.
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public CmsFile readFile(String folder, String filename)
		throws CmsException { 
  		return( m_rb.readFile(m_context.currentUser(), 
							  m_context.currentProject(), 
							  folder + filename ) );
	}
	/**
	 * Reads a folder from the Cms.<BR/>
	 * 
	 * @param folder The complete path to the folder to be read.
	 * 
	 * @return folder The read folder.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public CmsFolder readFolder(String folder)
		throws CmsException {
		return( readFolder(folder, "") );
	}
	/**
	 * Reads a folder from the Cms.<BR/>
	 * 
	 * @param folder The complete path to the folder from which the folder will be 
	 * read.
	 * @param foldername The name of the folder to be read.
	 * 
	 * @return folder The read folder.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public CmsFolder readFolder(String folder, String folderName)
		throws CmsException { 
		return( m_rb.readFolder(m_context.currentUser(), 
								m_context.currentProject(), folder, folderName ) );
	}
	 /**
	  * Reads all given tasks from a user for a project.
	  * 
	  * @param projectId The id of the Project in which the tasks are defined.
	  * @param owner Owner of the task.
	  * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
	  * @param orderBy Chooses, how to order the tasks.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readGivenTasks(int projectId, String ownerName, int taskType, 
								  String orderBy, String sort) 
		 throws CmsException {
		 return( m_rb.readGivenTasks(m_context.currentUser(), m_context.currentProject(), 
									 projectId, ownerName, taskType, orderBy, sort) );
	 }
	/**
	 * Reads the group of a project from the OpenCms.
	 * 
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup readGroup(CmsProject project) 
		throws CmsException{
		return( m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), 
							   project ) );
	}
	/**
	 * Reads the group of a resource from the OpenCms.
	 * 
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup readGroup(CmsResource resource) 
		throws CmsException {
		return( m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), 
							   resource ) );
	}
	/**
	 * Reads the group (role) of a task from the OpenCms.
	 * 
	 * @param task The task to read from.
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup readGroup(CmsTask task) 
		throws CmsException {
		return( m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), 
							   task ) );
	}
	/**
	 * Returns a group in the Cms.
	 * 
	 * @param groupname The name of the group to be returned.
	 * @return a group in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public CmsGroup readGroup(String groupname) 
		throws CmsException { 
		return( m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), 
							   groupname));
	}
	/**
	 * Reads the managergroup of a project from the OpenCms.
	 * 
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup readManagerGroup(CmsProject project) 
		throws CmsException{
		return( m_rb.readManagerGroup(m_context.currentUser(), 
									  m_context.currentProject(), 
									  project ) );
	}
	/**
	 * Gets the MimeTypes. 
	 * The Mime-Types will be returned.
	 * 
	 * <B>Security:</B>
	 * All users are granted<BR/>
	 * 
	 * @return the mime-types.
	 */
	public Hashtable readMimeTypes()
		throws CmsException {
		return m_rb.readMimeTypes(null, null);
	}
	/**
	 * Reads the original agent of a task from the OpenCms.
	 * 
	 * @param task The task to read the original agent from.
	 * @return The owner of a task.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readOriginalAgent(CmsTask task) 
		throws CmsException {
		return( m_rb.readOriginalAgent(m_context.currentUser(), 
									   m_context.currentProject(), 
									   task ) );
	}
	/**
	 * Reads the owner of a project from the OpenCms.
	 * 
	 * @return The owner of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readOwner(CmsProject project) 
		throws CmsException{
		return( m_rb.readOwner(m_context.currentUser(), m_context.currentProject(), 
							   project ) );
	}
	/**
	 * Reads the owner of a resource from the OpenCms.
	 * 
	 * @return The owner of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readOwner(CmsResource resource) 
		throws CmsException {
		return( m_rb.readOwner(m_context.currentUser(), m_context.currentProject(), 
							   resource ) );
	}
	/**
	 * Reads the owner (initiator) of a task from the OpenCms.
	 * 
	 * @param task The task to read the owner from.
	 * @return The owner of a task.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readOwner(CmsTask task) 
		throws CmsException {
		return( m_rb.readOwner(m_context.currentUser(), m_context.currentProject(), 
							   task ) );
	}
	/**
	 * Reads the owner of a tasklog from the OpenCms.
	 * 
	 * @return The owner of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readOwner(CmsTaskLog log) 
		throws CmsException {
		return( m_rb.readOwner(m_context.currentUser(), m_context.currentProject(), 
							   log ) );
	}
	/**
	 * Reads a project from the Cms.
	 * 
	 * @param id The id of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsProject readProject(int id)
		throws CmsException { 
		return( m_rb.readProject(m_context.currentUser(), 
								 m_context.currentProject(), id) );
	}
	 /**
	 * Reads a project from the Cms.
	 * 
	 * @param name The resource of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsProject readProject(CmsResource res)
		throws CmsException { 
		return( m_rb.readProject(m_context.currentUser(), 
								 m_context.currentProject(), res) );
	}
	/**
	 * Reads a project from the Cms.
	 * 
	 * @param task The task of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsProject readProject(CmsTask task)
		throws CmsException { 
		return( m_rb.readProject(m_context.currentUser(), 
								 m_context.currentProject(), task) );
	}
	 /**
	  * Reads log entries for a project.
	  * 
	  * @param projectId The id of the projec for tasklog to read.
	  * @return A Vector of new TaskLog objects 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readProjectLogs(int projectId)
		 throws CmsException {
		 return m_rb.readProjectLogs(m_context.currentUser(), m_context.currentProject(), projectId);
	 }
	/**
	 * Reads the Propertydefinition for the resource type.<BR/>
	 * 
	 * @param name The name of the Propertydefinition to read.
	 * @param resourcetype The name of the resource type for the Propertydefinition.
	 * @return the Propertydefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition readPropertydefinition(String name, 
												  String resourcetype)
		throws CmsException { 
 		return( m_rb.readPropertydefinition(m_context.currentUser(), 
										m_context.currentProject(), 
										name,
										resourcetype) );
	}
	/**
	 * Returns a Property of a file or folder.
	 * 
	 * @param name The resource-name of which the Property has to be read.
	 * @param property The Propertydefinition-name of which the Property has to be read.
	 * 
	 * @return property The Property as string.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readProperty(String name, String property)
		throws CmsException { 
		return( m_rb.readProperty(m_context.currentUser(), 
										 m_context.currentProject(), 
										 name, property) );
	}
	 /**
	  * Read a task by id.
	  * 
	  * @param id The id for the task to read.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public CmsTask readTask(int id)
		 throws CmsException {
		 return( m_rb.readTask(m_context.currentUser(), m_context.currentProject(), 
							   id) );
	 }
	 /**
	  * Reads log entries for a task.
	  * 
	  * @param taskid The task for the tasklog to read .
	  * @return A Vector of new TaskLog objects 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTaskLogs(int taskid)
		 throws CmsException {
		 return m_rb.readTaskLogs(m_context.currentUser(), m_context.currentProject(), taskid);
	 }
	 /**
	  * Reads all tasks for a project.
	  * 
	  * @param projectId The id of the Project in which the tasks are defined. Can be null for all tasks
	  * @tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
	  * @param orderBy Chooses, how to order the tasks. 
	  * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasksForProject(int projectId, int tasktype, 
									   String orderBy, String sort) 
		 throws CmsException {
		 return(m_rb.readTasksForProject(m_context.currentUser(), 
										 m_context.currentProject(), projectId, 
										 tasktype, orderBy, sort) );
	 }
	 /**
	  * Reads all tasks for a role in a project.
	  * 
	  * @param projectId The id of the Project in which the tasks are defined.
	  * @param user The user who has to process the task.
	  * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
	  * @param orderBy Chooses, how to order the tasks.
	  * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasksForRole(int projectId, String roleName, int tasktype, 
									String orderBy, String sort) 
		 throws CmsException {
		 return( m_rb.readTasksForRole(m_context.currentUser(), m_context.currentProject(), 
									   projectId, roleName, tasktype, orderBy, sort) );
	 }
	/**
	  * Reads all tasks for a user in a project.
	  * 
	  * @param projectId The id of the Project in which the tasks are defined.
	  * @param role The user who has to process the task.
	  * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
	  * @param orderBy Chooses, how to order the tasks.
	  * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasksForUser(int projectId, String userName, int tasktype, 
									String orderBy, String sort) 
		 throws CmsException {
		 return( m_rb.readTasksForUser(m_context.currentUser(), m_context.currentProject(), 
									   projectId, userName, tasktype, orderBy, sort) );
	 }
	 /**
	 * Returns a user in the Cms.
	 * 
	 * @param id The id of the user to be returned.
	 * @return a user in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public CmsUser readUser(int id) 
		throws CmsException { 
		return( m_rb.readUser(m_context.currentUser(), 
							  m_context.currentProject(), 
							  id) );
	}
	/**
	 * Returns a user in the Cms.
	 * 
	 * @param username The name of the user to be returned.
	 * @param type The type of the user.
	 * @return a user in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public CmsUser readUser(String username,int type) 
		throws CmsException { 
		return( m_rb.readUser(m_context.currentUser(), 
							  m_context.currentProject(), 
							  username,type) );
	}
	/**
	 * Returns a user in the Cms.
	 * 
	 * @param username The name of the user to be returned.
	 * @return a user in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public CmsUser readUser(String username) 
		throws CmsException { 
		return m_rb.readUser(m_context.currentUser(), 
							  m_context.currentProject(), 
							  username);
	}
	/**
	 * Returns a user in the Cms, if the password is correct.
	 * 
	 * @param username The name of the user to be returned.
	 * @param password The password of the user to be returned.
	 * @return a user in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public CmsUser readUser(String username, String password) 
		throws CmsException { 
		return( m_rb.readUser(m_context.currentUser(), m_context.currentProject(),
							  username, password) );
	}
 	 /**
	  * Reaktivates a task from the Cms.
	  * 
	  * @param taskid The Id of the task to accept.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void reaktivateTask(int taskId)
		 throws CmsException {
		 m_rb.reaktivateTask(m_context.currentUser(), m_context.currentProject(), taskId);
	 }
	/** 
	 * Sets a new password only if the user knows his recovery-password.
	 * 
	 * @param username The name of the user.
	 * @param recoveryPassword The recovery password.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void recoverPassword(String username, String recoveryPassword, String newPassword)
		throws CmsException {
		m_rb.recoverPassword(m_context.currentUser(), m_context.currentProject(), 
						 username, recoveryPassword, newPassword );
	}
	/**
	 * Removes a user from a group.
	 * 
	 * Only the admin can do this.
	 * 
	 * @param username The name of the user that is to be removed from the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	public void removeUserFromGroup(String username, String groupname)
		throws CmsException { 
		m_rb.removeUserFromGroup(m_context.currentUser(), m_context.currentProject(), 
								 username, groupname );
	}
	/**
	 * Renames the file to the new name.
	 * 
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (No path information allowed).
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be renamed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */		
	public void renameFile(String oldname, String newname)
		throws CmsException { 
		m_rb.renameFile(m_context.currentUser(), m_context.currentProject(), 
						oldname, newname);
	}
	/**
	 * Renames the folder to the new name.
	 * 
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (No path information allowed).
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be renamed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */		
	 public void renameFolder(String oldname, String newname)
		 throws CmsException {
		 m_rb.renameFolder(m_context.currentUser(), m_context.currentProject(), 
					       oldname, newname);
	 }
	/**
	 * Returns the root-folder object.
	 * 
	 * @return the root-folder object.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsFolder rootFolder() 
		throws CmsException {
		return( readFolder(C_ROOT) );
	}
	 /**
	  * Set a new name for a task
	  * 
	  * @param taskid The Id of the task to set the percentage.
	  * @param name The new name value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setName(int taskId, String name)
		 throws CmsException {
		 m_rb.setName(m_context.currentUser(), m_context.currentProject(), taskId, name);
	 }
	/**
	 * Sets a new parent-group for an already existing group in the Cms.<BR/>
	 * 
	 * @param groupName The name of the group that should be written to the Cms.
	 * @param parentGroupName The name of the parentGroup to set, or null if the parent 
	 * group should be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	public void setParentGroup(String groupName, String parentGroupName)
		throws CmsException {
		m_rb.setParentGroup(m_context.currentUser(), m_context.currentProject(), 
							groupName, parentGroupName);
	}
	/** 
	 * Sets the password for a user.
	 * 
	 * @param username The name of the user.
	 * @param oldPassword The old password.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void setPassword(String username, String oldPassword, String newPassword)
		throws CmsException {
		m_rb.setPassword(m_context.currentUser(), m_context.currentProject(), 
						 username, oldPassword, newPassword );
	}
	/** 
	 * Sets the password for a user.
	 * 
	 * @param username The name of the user.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void setPassword(String username, String newPassword)
		throws CmsException {
		m_rb.setPassword(m_context.currentUser(), m_context.currentProject(), 
						 username, newPassword );
	}
	 /**
	  * Set priority of a task
	  * 
	  * @param taskid The Id of the task to set the percentage.
	  * @param new priority value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setPriority(int taskId, int priority)
		 throws CmsException {
		 m_rb.setPriority(m_context.currentUser(), m_context.currentProject(), 
						  taskId, priority);
	 }
	/** 
	 * Sets the recovery password for a user.
	 * 
	 * @param username The name of the user.
	 * @param password The password.
	 * @param newPassword The new recovery password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void setRecoveryPassword(String username, String oldPassword, String newPassword)
		throws CmsException {
		m_rb.setRecoveryPassword(m_context.currentUser(), m_context.currentProject(), 
						 username, oldPassword, newPassword );
	}
	 /**
	  * Set a Parameter for a task.
	  * 
	  * @param taskid The Id of the task.
	  * @param parname Name of the parameter.
	  * @param parvalue Value if the parameter.
	  * 
	  * @return The id of the inserted parameter or 0 if the parameter already exists for this task.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setTaskPar(int taskid, String parname, String parvalue)
		 throws CmsException {
		 m_rb.setTaskPar(m_context.currentUser(), m_context.currentProject(),
						 taskid, parname, parvalue);
	 }
 	 /**
	  * Set timeout of a task
	  * 
	  * @param taskid The Id of the task to set the percentage.
	  * @param new timeout value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setTimeout(int taskId, long timeout)
		 throws CmsException {
		 m_rb.setTimeout(m_context.currentUser(), m_context.currentProject(), 
						 taskId, timeout);
	 }
	/**
	 * Unlocks all resources in this project.
	 * 
	 * @param id The id of the project to be published.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void unlockProject(int id)
		throws CmsException {
		m_rb.unlockProject(m_context.currentUser(), m_context.currentProject(), id);
	}
	/**
	 * Unlocks a resource<BR/>
	 * 
	 * A user can unlock a resource, so other users may lock this file.
	 * 
	 * @param resource The complete path to the resource to lock.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if there is a existing lock
	 * and force was set to false.
	 */
	public void unlockResource(String resource)
		throws CmsException { 
		m_rb.unlockResource(m_context.currentUser(), 
					  m_context.currentProject(), resource);
	}
	/**
	 * Tests, if a user is in a group.
	 * 
	 * @param username The name of the user to test.
	 * @param groupname The name of the group to test.
	 * @return true, if the user is in the group else returns false.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public boolean userInGroup(String username, String groupname)
		throws CmsException { 
		return( m_rb.userInGroup(m_context.currentUser(), m_context.currentProject(), 
								 username, groupname));
	}
	/**
	 * The default constructor.
	 */
	public CmsObject () {
	}
	/**
	 * Returns a version-string for this OpenCms.
	 * 
	 * @return version A Version-string.
	 */
	 public String version() {
		 return( C_VERSION );
	 }
	/**
	 * Writes the export-path for the system.
	 * This path is used for db-export and db-import.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 */
	public void writeExportPath(String path)
		throws CmsException {
		m_rb.writeExportPath(m_context.currentUser(), m_context.currentProject(), path);
	}
	/**
	 * Writes the file extensions  
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "Administrators" are authorized.<BR/>
	 *  
	 * @param extensions Holds extensions as keys and resourcetypes (Stings) as values
	 */
	
	public void writeFileExtensions(Hashtable extensions) 
		throws CmsException {
		m_rb.writeFileExtensions(m_context.currentUser(), m_context.currentProject(), extensions);
	}
	/**
	 * Writes a file-header to the Cms.<BR/>
	 * 
	 * @param file The file to write.
	 * 
	 * @exception CmsException will be thrown for missing properties, for worng Propertydefs
	 * or if resourcetype is set to folder. The CmsException will also be thrown, 
	 * if the user has not the rights for this resource.
	 */	
	public void writeFileHeader(CmsFile file) 
		throws CmsException {
		m_rb.writeFileHeader(m_context.currentUser(), m_context.currentProject(), file);
	}
	/**
	 * Writes a file to the Cms.<BR/>
	 * If some mandatory Propertydefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be written without
	 * the mandatory Properties.<BR/>
	 * 
	 * @param file The file to write.
	 * 
	 * @exception CmsException will be thrown for missing properties, for worng Propertydefs
	 * or if resourcetype is set to folder. The CmsException will also be thrown, 
	 * if the user has not the rights for this resource.
	 */	
	public void writeFile(CmsFile file) 
		throws CmsException { 
		m_rb.writeFile(m_context.currentUser(), m_context.currentProject(), file);
	}
	/**
	 * Writes an already existing group in the Cms.<BR/>
	 * 
	 * @param group The group that should be written to the Cms.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	public void writeGroup(CmsGroup group)
		throws CmsException {
		m_rb.writeGroup(m_context.currentUser(), m_context.currentProject(),
						group);
	}
	/**
	 * Writes a couple of Properties for a file or folder.
	 * 
	 * @param name The resource-name of which the Property has to be set.
	 * @param properties A Hashtable with Propertydefinition- Property-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeProperties(String name, Hashtable properties)
		throws CmsException { 
		m_rb.writeProperties(m_context.currentUser(),m_context.currentProject(), 
								  name, properties);
	}
	/**
	 * Writes the Propertydefinition for the resource type.<BR/>
	 * 
	 * @param propertydef The Propertydef to be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition writePropertydefinition(CmsPropertydefinition definition)
		throws  CmsException { 
		return( m_rb.writePropertydefinition(m_context.currentUser(), 
										 m_context.currentProject(), 
										 definition) );
	}
	/**
	 * Writes a Property for a file or folder.
	 * 
	 * @param name The resource-name of which the Property has to be set.
	 * @param property The Propertydefinition-name of which the Property has to be set.
	 * @param value The value for the Property to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeProperty(String name, String property, String value)
		throws CmsException { 
		m_rb.writeProperty(m_context.currentUser(),m_context.currentProject(), 
								  name, property, value);
	}
	 /**
	  * Writes a new user tasklog for a task.
	  * 
	  * @param taskid The Id of the task .
	  * @param comment Description for the log
	  * @param tasktype Type of the tasklog. User tasktypes must be greater then 100.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void writeTaskLog(int taskid, String comment, int taskType)
		 throws CmsException {
		 m_rb.writeTaskLog(m_context.currentUser(), m_context.currentProject(), taskid, 
						   comment, taskType);
	 }
	 /**
	  * Writes a new user tasklog for a task.
	  * 
	  * @param taskid The Id of the task .
	  * @param comment Description for the log
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void writeTaskLog(int taskid, String comment)
		 throws CmsException {
		 m_rb.writeTaskLog(m_context.currentUser(), m_context.currentProject(), 
						   taskid, comment);
	 }
	/**
	 * Updates the userinformation.<BR/>
	 * 
	 * Only the administrator can do this.
	 * 
	 * @param user The user to be written.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeUser(CmsUser user)
		throws CmsException { 
		m_rb.writeUser(m_context.currentUser(), m_context.currentProject(), user );
	}
	 /**
	 * Updates the userinformation of a webuser.<BR/>
	 * 
	 * Only a web user can be updated this way.
	 * 
	 * @param user The user to be written.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeWebUser(CmsUser user)
		throws CmsException { 
		m_rb.writeWebUser(m_context.currentUser(), m_context.currentProject(), user );
	}
}

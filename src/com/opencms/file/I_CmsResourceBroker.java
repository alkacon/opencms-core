package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsResourceBroker.java,v $
 * Date   : $Date: 2001/01/04 09:42:24 $
 * Version: $Revision: 1.137 $
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
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;
import com.opencms.core.*;

/**
 * This interface describes THE resource broker. All DB-specific access modules must
 * implement this interface.
 * The interface is local to package. <B>All</B> methods
 * get additional parameters (callingUser and currentproject) to check the security-
 * police.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.137 $ $Date: 2001/01/04 09:42:24 $
 * 
 */

public interface I_CmsResourceBroker {

	 /**
	  * Accept a task from the Cms.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task to accept.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void acceptTask(CmsUser currentUser, CmsProject currentProject, int taskId)
		 throws CmsException ;
	/**
	 * Checks, if the user may create this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * 
	 * @return wether the user has access, or not.
	 */
	public boolean accessCreate(CmsUser currentUser, CmsProject currentProject,
								 String resourceName) throws CmsException;
	/**
	 * Checks, if the user may lock this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * 
	 * @return wether the user may lock this resource, or not.
	 */
	public boolean accessLock(CmsUser currentUser, CmsProject currentProject,
							   String resourceName) throws CmsException;
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
	public boolean accessProject(CmsUser currentUser, CmsProject currentProject,
								 int projectId) 
		throws CmsException;
	/**
	 * Checks, if the user may read this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * 
	 * @return wether the user has access, or not.
	 */
	public boolean accessRead(CmsUser currentUser, CmsProject currentProject,
							   String resourceName) throws CmsException;
	/**
	 * Checks, if the user may write this resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * 
	 * @return wether the user has access, or not.
	 */
	public boolean accessWrite(CmsUser currentUser, CmsProject currentProject,
								String resourceName) throws CmsException;
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
	
	public void addFileExtension(CmsUser currentUser, CmsProject currentProject,
								 String extension, String resTypeName)
		throws CmsException;
	/**
	 * Add a new group to the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the new group.
	 * @param description The description for the new group.
	 * @int flags The flags for the new group.
	 * @param name The name of the parent group (or null).
	 *
	 * @return Group
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	public CmsGroup addGroup(CmsUser currentUser, CmsProject currentProject, 
							   String name, String description, int flags, String parent)
		throws CmsException;
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
	public CmsResourceType addResourceType(CmsUser currentUser, 
											 CmsProject currentProject,
											 String resourceType, int launcherType, 
											 String launcherClass) 
		throws CmsException;
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
	public CmsUser addUser(CmsUser currentUser, CmsProject currentProject, 
							 String name, String password, 
					  String group, String description, 
					  Hashtable additionalInfos, int flags)
		throws CmsException;
	/**
	 * Adds a user to a group.<BR/>
	 *
	 * Only the admin can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user that is to be added to the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	public void addUserToGroup(CmsUser currentUser, CmsProject currentProject, 
							   String username, String groupname)
		throws CmsException;
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
	public CmsUser addWebUser(CmsUser currentUser, CmsProject currentProject, 
							 String name, String password, 
					  String group, String description, 
					  Hashtable additionalInfos, int flags)
		throws CmsException;
	/**
	 * Returns the anonymous user object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return the anonymous user object.
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public CmsUser anonymousUser(CmsUser currentUser, CmsProject currentProject) 
		throws CmsException;
	 /**
	 * Changes the group for this resource<br>
	 * 
	 * Only the group of a resource in an offline project can be changed. The state
	 * of the resource is set to CHANGED (1).
	 * If the content of this resource is not exisiting in the offline project already,
	 * it is read from the online project and written into the offline project.
	 * The user may change this, if he is admin of the resource. <br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user is owner of the resource or is admin</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The complete path to the resource.
	 * @param newGroup The name of the new group for this resource.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void chgrp(CmsUser currentUser, CmsProject currentProject,
					  String filename, String newGroup)
		throws CmsException;
	/**
	 * Changes the flags for this resource.<br>
	 * 
	 * Only the flags of a resource in an offline project can be changed. The state
	 * of the resource is set to CHANGED (1).
	 * If the content of this resource is not exisiting in the offline project already,
	 * it is read from the online project and written into the offline project.
	 * The user may change the flags, if he is admin of the resource <br>.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The complete path to the resource.
	 * @param flags The new accessflags for the resource.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void chmod(CmsUser currentUser, CmsProject currentProject,
					  String filename, int flags)
		throws CmsException;
	/**
	 * Changes the owner for this resource.<br>
	 * 
	 * Only the owner of a resource in an offline project can be changed. The state
	 * of the resource is set to CHANGED (1).
	 * If the content of this resource is not exisiting in the offline project already,
	 * it is read from the online project and written into the offline project.
	 * The user may change this, if he is admin of the resource. <br>
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user is owner of the resource or the user is admin</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The complete path to the resource.
	 * @param newOwner The name of the new owner for this resource.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void chown(CmsUser currentUser, CmsProject currentProject,
					  String filename, String newOwner)
		throws CmsException;
	 /**
	 * Changes the state for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 *  
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user is owner of the resource or is admin</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param filename The complete path to the resource.
	 * @param state The new state of this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. 
	 */
	public void chstate(CmsUser currentUser, CmsProject currentProject,
						String filename, int state)
		throws CmsException;
	 /**
	 * Changes the resourcetype for this resource<br>
	 * 
	 * Only the resourcetype of a resource in an offline project can be changed. The state
	 * of the resource is set to CHANGED (1).
	 * If the content of this resource is not exisiting in the offline project already,
	 * it is read from the online project and written into the offline project.
	 * The user may change this, if he is admin of the resource. <br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user is owner of the resource or is admin</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The complete path to the resource.
	 * @param newType The name of the new resourcetype for this resource.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void chtype(CmsUser currentUser, CmsProject currentProject,
					  String filename, String newType)
		throws CmsException;
	 /**
	 * Clears all internal DB-Caches.
	 */
	public void clearcache();
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
	public void copyFile(CmsUser currentUser, CmsProject currentProject,
						 String source, String destination)
		throws CmsException;
	 /**
	 * Copies a folder in the Cms. <br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the sourceresource</li>
	 * <li>the user can create the destinationresource</li>
	 * <li>the destinationresource dosn't exists</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param source The complete path of the sourcefolder.
	 * @param destination The complete path to the destination.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void copyFolder(CmsUser currentUser, CmsProject currentProject,
						 String source, String destination)
		throws CmsException;
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
	public void copyResourceToProject(CmsUser currentUser, 
									  CmsProject currentProject,
									  String resource)
		throws CmsException;
	/**
	 * Counts the locked resources in this project.
	 * 
	 * <B>Security</B>
	 * Only the admin or the owner of the project can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param id The id of the project
	 * @return the amount of locked resources in this project.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public int countLockedResources(CmsUser currentUser, CmsProject currentProject, int id)
		throws CmsException;
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
						
		 throws CmsException;
	/**
	 * Creates a new folder.
	 * If some mandatory propertydefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory propertyinformations.<BR/>
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
	 * @param currentGroup The group who requested this method.
	 * @param currentProject The current project of the user.
	 * @param folder The complete path to the folder in which the new folder will 
	 * be created.
	 * @param newFolderName The name of the new folder (No pathinformation allowed).
	 * @param propertyinfos A Hashtable of propertyinfos, that should be set for this folder.
	 * The keys for this Hashtable are the names for propertydefinitions, the values are
	 * the values for the propertyinfos.
	 * 
	 * @return file The created file.
	 * 
	 * @exception CmsException will be thrown for missing propertyinfos, for worng propertydefs
	 * or if the filename is not valid. The CmsException will also be thrown, if the 
	 * user has not the rights for this resource.
	 */
	public CmsFolder createFolder(CmsUser currentUser, CmsGroup currentGroup, 
								  CmsProject currentProject, 
								  String folder, String newFolderName, 
								  Hashtable propertyinfos)
		throws CmsException;
/**
 * Creates a project.
 * 
 * <B>Security</B>
 * Only the users which are in the admin or projectleader-group are granted.
 *
 * Changed: added the parent id
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param name The name of the project to read.
 * @param description The description for the new project.
 * @param group the group to be set.
 * @param managergroup the managergroup to be set.
 * @param parentId the parent project
 * @exception CmsException Throws CmsException if something goes wrong.
 * @author Martin Langelund
 */
public CmsProject createProject(CmsUser currentUser, CmsProject currentProject, String name, String description, String groupname, String managergroupname) throws com.opencms.core.CmsException;
	/**
	  * Creates a new project for task handling.
	  * 
	  * @param owner User who creates the project
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
	 public CmsTask createProject(CmsUser currentUser, String projectname, int projectType,
									String roleName, long timeout, 
									int priority)
		 throws CmsException;
	// Methods working with properties and propertydefinitions


	/**
	 * Creates the propertydefinition for the resource type.<BR/>
	 * 
	 * <B>Security</B>
	 * Only the admin can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the propertydefinition to overwrite.
	 * @param resourcetype The name of the resource-type for the propertydefinition.
	 * @param type The type of the propertydefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition createPropertydefinition(CmsUser currentUser, 
													CmsProject currentProject, 
													String name, 
													String resourcetype, 
													int type)
		throws CmsException;
/**
 * Insert the method's description here.
 * Creation date: (09-10-2000 11:35:40)
 * @param project com.opencms.file.CmsProject
 * @param onlineProject com.opencms.file.CmsProject
 * @param resource com.opencms.file.CmsResource
 * @exception com.opencms.core.CmsException The exception description.
 * author Martin Langelund
 */
public void createResource(CmsProject project, CmsProject onlineProject, CmsResource resource) throws com.opencms.core.CmsException;
	 /**
	  * Creates a new task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
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
	 public CmsTask createTask(CmsUser currentUser, int projectid, String agentName, String roleName, 
								 String taskname, String taskcomment, int tasktype,
								 long timeout, int priority)
		 throws CmsException;
	 /**
	  * Creates a new task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
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
	 
	 public CmsTask createTask(CmsUser currentUser, CmsProject currentProject, 
								 String agentName, String roleName, 
								 String taskname, String taskcomment, 
								 long timeout, int priority)
		 throws CmsException;
	/**
	 * Deletes all propertyinformation for a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to write the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the propertyinformations 
	 * have to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllProperties(CmsUser currentUser, 
										  CmsProject currentProject, 
										  String resource)
		throws CmsException;
	/**
	 * Deletes a file in the Cms.<br>
	 *
	 * A file can only be deleteed in an offline project. 
	 * A file is deleted by setting its state to DELETED (3). <br> 
	 * 
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callinUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The complete path of the file.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void deleteFile(CmsUser currentUser, CmsProject currentProject,
						   String filename)
		throws CmsException;
	 /**
	 * Deletes a folder in the Cms.<br>
	 * 
	 * Only folders in an offline Project can be deleted. A folder is deleted by 
	 * setting its state to DELETED (3). <br>
	 *  
	 * In its current implmentation, this method can ONLY delete empty folders.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write this resource and all subresources</li>
	 * <li>the resource is not locked</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param foldername The complete path of the folder.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void deleteFolder(CmsUser currentUser, CmsProject currentProject,
							 String foldername)
		throws CmsException;
	/**
	 * Delete a group from the Cms.<BR/>
	 * Only groups that contain no subgroups can be deleted.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param delgroup The name of the group that is to be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	public void deleteGroup(CmsUser currentUser, CmsProject currentProject, 
							String delgroup)
		throws CmsException;
	/**
	 * Deletes a project.
	 * 
	 * <B>Security</B>
	 * Only the admin or the owner of the project can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param id The id of the project to be published.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deleteProject(CmsUser currentUser, CmsProject currentProject,
							  int id)
		throws CmsException;
	/**
	 * Deletes a propertyinformation for a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to write the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the propertyinformation 
	 * has to be read.
	 * @param property The propertydefinition-name of which the propertyinformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteProperty(CmsUser currentUser, CmsProject currentProject, 
									  String resource, String property)
		throws CmsException;
	/**
	 * Delete the propertydefinition for the resource type.<BR/>
	 * 
	 * <B>Security</B>
	 * Only the admin can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the propertydefinition to read.
	 * @param resourcetype The name of the resource type for which the 
	 * propertydefinition is valid.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deletePropertydefinition(CmsUser currentUser, CmsProject currentProject, 
									 String name, String resourcetype)
		throws CmsException;
	/** 
	 * Deletes a user from the Cms.
	 * 
	 * Only a adminstrator can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param userId The Id of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void deleteUser(CmsUser currentUser, CmsProject currentProject, 
						   int userId)
		throws CmsException;
	/** 
	 * Deletes a user from the Cms.
	 * 
	 * Only a adminstrator can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void deleteUser(CmsUser currentUser, CmsProject currentProject, 
						   String username)
		throws CmsException;
  	/** 
	 * Deletes a web user from the Cms.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param userId The Id of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void deleteWebUser(CmsUser currentUser, CmsProject currentProject, 
						   int userId)
		throws CmsException;
	 /**
	 * Destroys the resource borker and required modules and connections.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void destroy() 
		throws CmsException;
	 /**
	  * Ends a task from the Cms.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The ID of the task to end.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void endTask(CmsUser currentUser, CmsProject currentProject, int taskid) 
		 throws CmsException;
	/**
	 * Exports cms-resources to zip.
	 * 
	 * <B>Security:</B>
	 * only Administrators can do this;
	 * 
	 * @param currentUser user who requestd themethod
	 * @param currentProject current project of the user
	 * @param exportFile the name (absolute Path) of the export resource (zip)
	 * @param exportPaths the name (absolute Path) of folders from which should be exported
	 * @param cms the cms-object to use for the export.
	 * 
	 * @exception Throws CmsException if something goes wrong.
	 */
	public void exportResources(CmsUser currentUser,  CmsProject currentProject, String exportFile, String[] exportPaths, CmsObject cms)
		throws CmsException;
	/**
	 * Exports cms-resources to zip.
	 * 
	 * <B>Security:</B>
	 * only Administrators can do this;
	 * 
	 * @param currentUser user who requestd themethod
	 * @param currentProject current project of the user
	 * @param exportFile the name (absolute Path) of the export resource (zip)
	 * @param exportPaths the name (absolute Path) of folders from which should be exported
	 * @param includeSystem, desides if to include the system resources to the export.
	 * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
	 * @param cms the cms-object to use for the export.
	 * 
	 * @exception Throws CmsException if something goes wrong.
	 */
	public void exportResources(CmsUser currentUser,  CmsProject currentProject, String exportFile, String[] exportPaths, CmsObject cms, boolean includeSystem, boolean excludeUnchanged)
		throws CmsException;
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
	  * @param newUser The new user who gets the task.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void forwardTask(CmsUser currentUser, CmsProject currentProject, int taskid, 
							 String newRoleName, String newUserName) 
		 throws CmsException;
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
		 throws CmsException;
	/**
	 * Returns all projects, which are owned by the user or which are manageable
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
	 public Vector getAllManageableProjects(CmsUser currentUser, 
											CmsProject currentProject)
		 throws CmsException;
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
	public Hashtable getAllResourceTypes(CmsUser currentUser, 
										 CmsProject currentProject) 
		throws CmsException;
	/**
	 * Returns informations about the cache.
	 * 
	 * @return a hashtable with informations about the cache.
	 */
	public Hashtable getCacheInfo();
	/**
	 * Returns all child groups of a group<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupname The name of the group.
	 * @return groups A Vector of all child groups or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getChild(CmsUser currentUser, CmsProject currentProject, 
						   String groupname)
		throws CmsException ;
	/**
	 * Returns all child groups of a group<P/>
	 * This method also returns all sub-child groups of the current group.
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupname The name of the group.
	 * @return groups A Vector of all child groups or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getChilds(CmsUser currentUser, CmsProject currentProject, 
							String groupname)
		throws CmsException ;
	// Method to access the configuration

	/**
	 * Method to access the configurations of the properties-file.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The Configurations of the properties-file.
	 */
	public Configurations getConfigurations(CmsUser currentUser, CmsProject currentProject);
	/**
	 * Returns the list of groups to which the user directly belongs to<P/>
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
	public Vector getDirectGroupsOfUser(CmsUser currentUser, CmsProject currentProject, 
										String username)
		throws CmsException;
	 /**
	 * Returns a Vector with all files of a folder.<br>
	 * 
	 * Files of a folder can be read from an offline Project and the online Project.<br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read this resource</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfiles A Vector with all subfiles for the overgiven folder.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public Vector getFilesInFolder(CmsUser currentUser, CmsProject currentProject,
								   String foldername)
		throws CmsException;
/**
 * Returns a Vector with all resource-names that have set the given property to the given value.
 * 
 * <B>Security:</B>
 * All users are granted.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param foldername the complete path to the folder.
 * @param propertydef, the name of the propertydefinition to check.
 * @param property, the value of the property for the resource.
 * 
 * @return Vector with all names of resources.
 * 
 * @exception CmsException Throws CmsException if operation was not succesful.
 */
public Vector getFilesWithProperty(CmsUser currentUser, CmsProject currentProject, String propertyDefinition, String propertyValue) throws CmsException;
	/**
	 * This method can be called, to determine if the file-system was changed 
	 * in the past. A module can compare its previosly stored number with this
	 * returned number. If they differ, a change was made.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * 
	 * @return the number of file-system-changes.
	 */
	public long getFileSystemChanges(CmsUser currentUser, CmsProject currentProject);
	/**
	 * This method can be called, to determine if the file-system was changed(only Folders) 
	 * in the past. A module can compare its previosly stored number with this
	 * returned number. If they differ, a change was made.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * 
	 * @return the number of file-system-changes.
	 */
	public long getFileSystemFolderChanges(CmsUser currentUser, CmsProject currentProject);
   	/**
	 * Returns a Vector with the complete folder-tree for this project.<br>
	 * 
	 * Subfolders can be read from an offline project and the online project. <br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read this resource</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * 
	 * @return subfolders A Vector with the complete folder-tree for this project.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public Vector getFolderTree(CmsUser currentUser, CmsProject currentProject)
		throws CmsException;
	/**
	 * Returns all groups<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return users A Vector of all existing groups.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getGroups(CmsUser currentUser, CmsProject currentProject)
		throws CmsException;
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
	public Vector getGroupsOfUser(CmsUser currentUser, CmsProject currentProject, 
								  String username)
		throws CmsException;
	/**
	 * Returns the parent group of a group<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupname The name of the group.
	 * @return group The parent group or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup getParent(CmsUser currentUser, CmsProject currentProject, 
								String groupname)
		throws CmsException ;
	/**
	 * Returns the parent resource of a resouce.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The name of the file to be read.
	 * 
	 * @return The file read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public CmsResource getParentResource(CmsUser currentUser, CmsProject currentProject,
										 String resourcename) 
		throws CmsException;
	  /**
	 * Gets the Registry.<BR/>
	 *
	 *
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param cms The actual CmsObject.
	 * @exception Throws CmsException if access is not allowed.
	 */

	 public I_CmsRegistry getRegistry(CmsUser currentUser, CmsProject currentProject, CmsObject cms)
	 	throws CmsException;
	/**
	 * Returns a Vector with the subresources for a folder.<br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read this resource</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param folder The name of the folder to get the subresources from.
	 * 
	 * @return subfolders A Vector with resources.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public Vector getResourcesInFolder(CmsUser currentUser, CmsProject currentProject, String folder)
		throws CmsException;
	/**
	 * Returns a CmsResourceTypes.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourceType the id of the resourceType to get.
	 * 
	 * Returns a CmsResourceTypes.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public CmsResourceType getResourceType(CmsUser currentUser, 
											 CmsProject currentProject,
											 int resourceType)
		throws CmsException;
	/**
	 * Returns a CmsResourceTypes.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourceType the name of the resource to get.
	 * 
	 * Returns a CmsResourceTypes.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public CmsResourceType getResourceType(CmsUser currentUser, 
											 CmsProject currentProject,
											 String resourceType) 
		throws CmsException;
	 /**
	 * Returns the session storage after a securtity check.
	 * 
	 * <B>Security:</B>
	 * All users except the guest user are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param storage The storage of all active users.
	 * @return The storage of all active users.
	 */
	public CmsCoreSession getSessionStorage(CmsUser currentUser, CmsCoreSession storage);
   	/**
	 * Returns a Vector with all subfolders.<br>
	 * 
	 * Subfolders can be read from an offline project and the online project. <br>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read this resource</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfolders A Vector with all subfolders for the given folder.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public Vector getSubFolders(CmsUser currentUser, CmsProject currentProject,
								String foldername)
		throws CmsException;
	 /**
	  * Get a parameter value for a task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task.
	  * @param parname Name of the parameter.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public String getTaskPar(CmsUser currentUser, CmsProject currentProject, 
							  int taskid, String parname)
		 throws CmsException;
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
		throws CmsException;
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
	public Vector getUsers(CmsUser currentUser, CmsProject currentProject)
		throws CmsException;
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
	public Vector getUsers(CmsUser currentUser, CmsProject currentProject, int type)
		throws CmsException;
	 /**
	 * Returns all users from a given type that start with a specified string<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted, except the anonymous user.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param type The type of the users.
	 * @param namestart The filter for the username
	 * @return users A Vector of all existing users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getUsers(CmsUser currentUser, CmsProject currentProject, int type, String namestart)
		throws CmsException;
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
		throws CmsException;
	/**
	 * Imports a import-resource (folder or zipfile) to the cms.
	 * 
	 * <B>Security:</B>
	 * only Administrators can do this;
	 * 
	 * @param currentUser user who requestd themethod
	 * @param currentProject current project of the user
	 * @param importFile the name (absolute Path) of the import resource (zip or folder)
	 * @param importPath the name (absolute Path) of folder in which should be imported
	 * @param cms the cms-object to use for the import.
	 * 
	 * @exception Throws CmsException if something goes wrong.
	 */
	public void importFolder(CmsUser currentUser,  CmsProject currentProject, String importFile, String importPath, CmsObject cms)
		throws CmsException;
	// Methods working with database import and export
	
	/**
	 * Imports a import-resource (folder or zipfile) to the cms.
	 * 
	 * <B>Security:</B>
	 * only Administrators can do this;
	 * 
	 * @param currentUser user who requestd themethod
	 * @param currentProject current project of the user
	 * @param importFile the name (absolute Path) of the import resource (zip or folder)
	 * @param importPath the name (absolute Path) of folder in which should be imported
	 * @param cms the cms-object to use for the import.
	 * 
	 * @exception Throws CmsException if something goes wrong.
	 */
	public void importResources(CmsUser currentUser,  CmsProject currentProject, String importFile, String importPath, CmsObject cms)
		throws CmsException;
	// Internal ResourceBroker methods
	
	
	/**
	 * Initializes the resource broker and sets up all required modules and connections.
	 * @param config The OpenCms configuration.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void init(Configurations config) 
		throws CmsException;
	/**
	 * Determines, if the users current group is the admin-group.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return true, if the users current group is the admin-group, 
	 * else it returns false.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	public boolean isAdmin(CmsUser currentUser, CmsProject currentProject) 
		throws CmsException;
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
	public boolean isManagerOfProject(CmsUser currentUser, CmsProject currentProject) 
		throws CmsException;
	/**
	 * Determines, if the users current group is the projectleader-group.<BR/>
	 * All projectleaders can create new projects, or close their own projects.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return true, if the users current group is the projectleader-group, 
	 * else it returns false.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	public boolean isProjectManager(CmsUser currentUser, CmsProject currentProject) 
		throws CmsException;
	/**
	 * Returns the user, who had locked the resource.<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource. This methods checks, if a resource was locked.
	 * 
	 * @param user The user who wants to lock the file.
	 * @param project The project in which the resource will be used.
	 * @param resource The resource.
	 * 
	 * @return the user, who had locked the resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. 
	 */
	public CmsUser lockedBy(CmsUser currentUser, CmsProject currentProject,
							  CmsResource resource)
		throws CmsException;
	/**
	 * Returns the user, who had locked the resource.<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource. This methods checks, if a resource was locked.
	 * 
	 * @param user The user who wants to lock the file.
	 * @param project The project in which the resource will be used.
	 * @param resource The complete path to the resource.
	 * 
	 * @return the user, who had locked the resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. 
	 */
	public CmsUser lockedBy(CmsUser currentUser, CmsProject currentProject,
							  String resource)
		throws CmsException;
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
	public void lockResource(CmsUser currentUser, CmsProject currentProject,
							 String resourcename, boolean force)
		throws CmsException;
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
	public CmsUser loginUser(CmsUser currentUser, CmsProject currentProject, 
							   String username, String password) 
		throws CmsException;
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
	public CmsUser loginWebUser(CmsUser currentUser, CmsProject currentProject, 
							    String username, String password) 
		throws CmsException;
	/**
	 * Moves the file.
	 * 
	 * This operation includes a copy and a delete operation. These operations
	 * are done with their security-checks.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be moved. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void moveFile(CmsUser currentUser, CmsProject currentProject,
						 String source, String destination)
		throws CmsException;
	/**
	 * Returns the onlineproject. This is the default project. All anonymous 
	 * (CmsUser callingUser, or guest) user will see the rersources of this project.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return the onlineproject object.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsProject onlineProject(CmsUser currentUser, 
									CmsProject currentProject)
		throws CmsException;
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
	public void publishProject(CmsUser currentUser, CmsProject currentProject,
								 int id)
		throws CmsException;
	/**
	 * Reads the agent of a task from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param task The task to read the agent from.
	 * @return The owner of a task.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readAgent(CmsUser currentUser, CmsProject currentProject, 
							   CmsTask task) 
		throws CmsException ;
	 /**
	 * Reads all file headers of a file in the OpenCms.<BR>
	 * This method returns a vector with the histroy of all file headers, i.e. 
	 * the file headers of a file, independent of the project they were attached to.<br>
	 * 
	 * The reading excludes the filecontent.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param filename The name of the file to be read.
	 * 
	 * @return Vector of file headers read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	 public Vector readAllFileHeaders(CmsUser currentUser, CmsProject currentProject, 
									  String filename)
		 throws CmsException;
	/**
	 * Returns a list of all propertyinformations of a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to view the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the propertyinformation has to be 
	 * read.
	 * 
	 * @return Vector of propertyinformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Hashtable readAllProperties(CmsUser currentUser, CmsProject currentProject, 
											 String resource)
		throws CmsException;
	/**
	 * Reads all propertydefinitions for the given resource type.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param id The id of the resource type to read the propertydefinitions for.
	 * @param type The type of the propertydefinition (normal|mandatory|optional).
	 * 
	 * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllPropertydefinitions(CmsUser currentUser, CmsProject currentProject, 
										     int id, int type)
		throws CmsException;
	/**
	 * Reads all propertydefinitions for the given resource type.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourcetype The name of the resource type to read the propertydefinitions for.
	 * 
	 * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllPropertydefinitions(CmsUser currentUser, CmsProject currentProject, 
										 String resourcetype)
		throws CmsException;
	 /**
	 * Reads all propertydefinitions for the given resource type.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resourcetype The name of the resource type to read the propertydefinitions for.
	 * @param type The type of the propertydefinition (normal|mandatory|optional).
	 * 
	 * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	public Vector readAllPropertydefinitions(CmsUser currentUser, CmsProject currentProject, 
										     String resourcetype, int type)
		throws CmsException;
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
		throws CmsException ;
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
	 public CmsFile readFile(CmsUser currentUser, CmsProject currentProject,
							 int projectId, String filename)
		throws CmsException;
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
	 public CmsFile readFile(CmsUser currentUser, CmsProject currentProject,
							 String filename)
		throws CmsException;
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
		throws CmsException;
	  /**
	 * Reads a file header a previous project of the Cms.<BR/>
	 * The reading excludes the filecontent. <br>
	 * 
	 * A file header can be read from an offline project or the online project.
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
	 */
	 public CmsResource readFileHeader(CmsUser currentUser, 
									   CmsProject currentProject,
									   int projectId,
									   String filename)
		 throws CmsException;
	 /**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent. <br>
	 * 
	 * A file header can be read from an offline project or the online project.
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
	 */
	 public CmsResource readFileHeader(CmsUser currentUser, 
										 CmsProject currentProject, String filename)
		 throws CmsException;
	/**
	 * Reads all file headers for a project from the Cms.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param projectId The id of the project to read the resources for.
	 * 
	 * @return a Vector of resources.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public Vector readFileHeaders(CmsUser currentUser, CmsProject currentProject,
								  int projectId)
		throws CmsException;
	  /**
	 * Reads a folder from the Cms.<BR/>
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
	 * @param foldername The complete path of the folder to be read.
	 * 
	 * @return folder The read folder.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public CmsFolder readFolder(CmsUser currentUser, CmsProject currentProject,
								String folder)
		throws CmsException ;
	/**
	 * Reads a folder from the Cms.<BR/>
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
	public CmsFolder readFolder(CmsUser currentUser, CmsProject currentProject,
								String folder, String folderName)
		throws CmsException ;
	 /**
	  * Reads all given tasks from a user for a project.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param projectId The id of the Project in which the tasks are defined.
	  * @param owner Owner of the task.
	  * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
	  * @param orderBy Chooses, how to order the tasks.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readGivenTasks(CmsUser currentUser, CmsProject currentProject,
								  int projectId, String ownerName, int taskType, 
								  String orderBy, String sort) 
		 throws CmsException;
	/**
	 * Reads the group of a project from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject, 
								CmsProject project) 
		throws CmsException;
	/**
	 * Reads the group of a resource from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject, 
							   CmsResource resource) 
		throws CmsException ;
	/**
	 * Reads the group (role) of a task from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param task The task to read from.
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject, 
							   CmsTask task) 
		throws CmsException ;
	/**
	 * Returns a group object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupname The name of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
	public CmsGroup readGroup(CmsUser currentUser, CmsProject currentProject, 
								String groupname)
		throws CmsException;
	/**
	 * Reads the managergroup of a project from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsGroup readManagerGroup(CmsUser currentUser, CmsProject currentProject, 
									   CmsProject project) 
		throws CmsException;
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
		throws CmsException;
	/**
	 * Reads the original agent of a task from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param task The task to read the original agent from.
	 * @return The owner of a task.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readOriginalAgent(CmsUser currentUser, CmsProject currentProject, 
									   CmsTask task) 
		throws CmsException ;
	/**
	 * Reads the owner of a project from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The owner of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readOwner(CmsUser currentUser, CmsProject currentProject, 
							   CmsProject project) 
		throws CmsException;
	/**
	 * Reads the owner of a resource from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The owner of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readOwner(CmsUser currentUser, CmsProject currentProject, 
							   CmsResource resource) 
		throws CmsException ;
	/**
	 * Reads the owner (initiator) of a task from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param task The task to read the owner from.
	 * @return The owner of a task.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readOwner(CmsUser currentUser, CmsProject currentProject, 
							   CmsTask task) 
		throws CmsException;
	/**
	 * Reads the owner of a tasklog from the OpenCms.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @return The owner of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public CmsUser readOwner(CmsUser currentUser, CmsProject currentProject, CmsTaskLog log) 
		throws CmsException ;
	/**
	 * Reads a project from the Cms.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param id The id of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject readProject(CmsUser currentUser, CmsProject currentProject, 
								   int id)
		 throws CmsException ;
	 /**
	 * Reads a project from the Cms.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param res The resource to read the project of.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject readProject(CmsUser currentUser, CmsProject currentProject, 
								   CmsResource res)
		 throws CmsException ;
	/**
	 * Reads a project from the Cms.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param task The task to read the project of.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject readProject(CmsUser currentUser, CmsProject currentProject, 
									 CmsTask task)
		 throws CmsException ;
	 /**
	  * Reads log entries for a project.
	  * 
	  * @param projectId The id of the projec for tasklog to read.
	  * @return A Vector of new TaskLog objects 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readProjectLogs(CmsUser currentUser, CmsProject currentProject,
								   int projectId)
		 throws CmsException;
	/**
	 * Returns a propertyinformation of a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to view the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the propertyinformation has 
	 * to be read.
	 * @param property The propertydefinition-name of which the propertyinformation has to be read.
	 * 
	 * @return propertyinfo The propertyinfo as string.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readProperty(CmsUser currentUser, CmsProject currentProject, 
									  String resource, String property)
		throws CmsException;
	/**
	 * Reads a definition for the given resource type.
	 * 
	 * <B>Security</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param name The name of the propertydefinition to read.
	 * @param resourcetype The name of the resource type for which the propertydefinition 
	 * is valid.
	 * 
	 * @return propertydefinition The propertydefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid propertydefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition readPropertydefinition(CmsUser currentUser, 
												  CmsProject currentProject, 
												  String name, String resourcetype)
		throws CmsException;
/**
 * Insert the method's description here.
 * Creation date: (09-10-2000 09:30:47)
 * @return java.util.Vector
 * @param project com.opencms.file.CmsProject
 * @exception com.opencms.core.CmsException The exception description.
 */
public Vector readResources(CmsProject project) throws com.opencms.core.CmsException;
	 /**
	  * Read a task by id.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param id The id for the task to read.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public CmsTask readTask(CmsUser currentUser, CmsProject currentProject, 
							   int id)
		 throws CmsException;
	 /**
	  * Reads log entries for a task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The task for the tasklog to read .
	  * @return A Vector of new TaskLog objects 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTaskLogs(CmsUser currentUser, CmsProject currentProject,
								int taskid)
		 throws CmsException;
	 /**
	  * Reads all tasks for a project.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param projectId The id of the Project in which the tasks are defined. Can be null for all tasks
	  * @tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
	  * @param orderBy Chooses, how to order the tasks. 
	  * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasksForProject(CmsUser currentUser, CmsProject currentProject,
									   int projectId, int tasktype, 
									   String orderBy, String sort)
		 throws CmsException;
	 /**
	  * Reads all tasks for a role in a project.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param projectId The id of the Project in which the tasks are defined.
	  * @param user The user who has to process the task.
	  * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
	  * @param orderBy Chooses, how to order the tasks.
	  * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasksForRole(CmsUser currentUser, CmsProject currentProject,
									int projectId, String roleName, int tasktype, 
									String orderBy, String sort) 
		 throws CmsException;
	 /**
	  * Reads all tasks for a user in a project.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param projectId The id of the Project in which the tasks are defined.
	  * @param role The user who has to process the task.
	  * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
	  * @param orderBy Chooses, how to order the tasks.
	  * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasksForUser(CmsUser currentUser, CmsProject currentProject,
									int projectId, String userName, int tasktype, 
									String orderBy, String sort) 
		 throws CmsException;
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
	public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, 
							  int id)
		throws CmsException ;
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
	public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, 
							  String username)
		throws CmsException;
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
	public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, 
							  String username,int type)
		throws CmsException ;
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
	public CmsUser readUser(CmsUser currentUser, CmsProject currentProject, 
							  String username, String password)
		throws CmsException;
	/**
	 * Returns a user object if the password for the user is correct.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The username of the user that is to be read.
	 * @return User
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */		
	public CmsUser readWebUser(CmsUser currentUser, CmsProject currentProject, 
							  String username)
		throws CmsException;
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
	public CmsUser readWebUser(CmsUser currentUser, CmsProject currentProject, 
							  String username, String password)
		throws CmsException;
	 /**
	  * Reaktivates a task from the Cms.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task to accept.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void reaktivateTask(CmsUser currentUser, CmsProject currentProject,
								int taskId)
		 throws CmsException;
	/** 
	 * Sets a new password only if the user knows his recovery-password.
	 * 
	 * All users can do this if he knows the recovery-password.<P/>
	 * 
	 * <B>Security:</B>
	 * All users can do this if he knows the recovery-password.<P/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user.
	 * @param recoveryPassword The recovery password.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void recoverPassword(CmsUser currentUser, CmsProject currentProject, 
							String username, String recoveryPassword, String newPassword)
		throws CmsException;
	/**
	 * Removes a user from a group.
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Only users, which are in the group "administrators" are granted.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user that is to be removed from the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	public void removeUserFromGroup(CmsUser currentUser, CmsProject currentProject, 
									String username, String groupname)
		throws CmsException;
	/**
	 * Renames the file to a new name. <br>
	 * 
	 * Rename can only be done in an offline project. To rename a file, the following
	 * steps have to be done:
	 * <ul>
	 * <li> Copy the file with the oldname to a file with the new name, the state 
	 * of the new file is set to NEW (2). 
	 * <ul>
	 * <li> If the state of the original file is UNCHANGED (0), the file content of the 
	 * file is read from the online project. </li>
	 * <li> If the state of the original file is CHANGED (1) or NEW (2) the file content
	 * of the file is read from the offline project. </li>
	 * </ul>
	 * </li>
	 * <li> Set the state of the old file to DELETED (3). </li> 
	 * </ul>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (CmsUser callingUser, No path information allowed).
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */		
	public void renameFile(CmsUser currentUser, CmsProject currentProject, 
					       String oldname, String newname)
		throws CmsException;
 	/**
	 * This method loads old sessiondata from the database. It is used 
	 * for sessionfailover.
	 * 
	 * @param oldSessionId the id of the old session.
	 * @return the old sessiondata.
	 */
	public Hashtable restoreSession(String oldSessionId) 
		throws CmsException;
	 /**
	  * Set a new name for a task
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task to set the percentage.
	  * @param name The new name value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setName(CmsUser currentUser, CmsProject currentProject, 
						 int taskId, String name)
		 throws CmsException;
	/**
	 * Sets a new parent-group for an already existing group in the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param groupName The name of the group that should be written to the Cms.
	 * @param parentGroupName The name of the parentGroup to set, or null if the parent 
	 * group should be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	public void setParentGroup(CmsUser currentUser, CmsProject currentProject, 
							   String groupName, String parentGroupName)
		throws CmsException;
	/** 
	 * Sets the password for a user.
	 * 
	 * Only a adminstrator can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void setPassword(CmsUser currentUser, CmsProject currentProject, 
							String username, String newPassword)
		throws CmsException;
	/** 
	 * Sets the password for a user.
	 * 
	 * Only a adminstrator or the curretuser can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * Current users can change their own password.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user.
	 * @param oldPassword The new password.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void setPassword(CmsUser currentUser, CmsProject currentProject, 
							String username, String oldPassword, String newPassword)
		throws CmsException;
	 /**
	  * Set priority of a task
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task to set the percentage.
	  * @param new priority value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setPriority(CmsUser currentUser, CmsProject currentProject,
							 int taskId, int priority)
		 throws CmsException;
	/** 
	 * Sets the recovery password for a user.
	 * 
	 * Only a adminstrator or the curretuser can do this.<P/>
	 * 
	 * <B>Security:</B>
	 * Users, which are in the group "administrators" are granted.<BR/>
	 * Current users can change their own password.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param username The name of the user.
	 * @param oldPassword The old password.
	 * @param newPassword The recovery password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void setRecoveryPassword(CmsUser currentUser, CmsProject currentProject, 
									String username, String password, String newPassword)
		throws CmsException;
	 /**
	  * Set a Parameter for a task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task.
	  * @param parname Name of the parameter.
	  * @param parvalue Value if the parameter.
	  * 
	  * @return The id of the inserted parameter or 0 if the parameter already exists for this task.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setTaskPar(CmsUser currentUser, CmsProject currentProject, 
						   int taskid, String parname, String parvalue)
		 throws CmsException;
 	 /**
	  * Set timeout of a task
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task to set the percentage.
	  * @param new timeout value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setTimeout(CmsUser currentUser, CmsProject currentProject,
							int taskId, long timeout)
		 throws CmsException;
	/**
	 * This method stores sessiondata into the database. It is used 
	 * for sessionfailover.
	 * 
	 * @param sessionId the id of the session.
	 * @param isNew determines, if the session is new or not.
	 * @return data the sessionData.
	 */
	public void storeSession(String sessionId, Hashtable sessionData) 
		throws CmsException;
	/**
	 * Unlocks all resources in this project.
	 * 
	 * <B>Security</B>
	 * Only the admin or the owner of the project can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param id The id of the project to be published.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void unlockProject(CmsUser currentUser, CmsProject currentProject, int id)
		throws CmsException;
	/**
	 * Unlocks a resource.<br>
	 * 
	 * Only a resource in an offline project can be unlock. The state of the resource
	 * is set to CHANGED (1).
	 * If the content of this resource is not exisiting in the offline project already,
	 * it is read from the online project and written into the offline project.
	 * Only the user who locked a resource can unlock it.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user had locked the resource before</li>
	 * </ul>
	 * 
	 * @param user The user who wants to lock the file.
	 * @param project The project in which the resource will be used.
	 * @param resourcename The complete path to the resource to lock.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void unlockResource(CmsUser currentUser,CmsProject currentProject,
							   String resourcename)
		throws CmsException;
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
		throws CmsException;
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
		throws CmsException ;
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
	public void writeFile(CmsUser currentUser, CmsProject currentProject, 
						  CmsFile file)
		throws CmsException ;
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
	
	public void writeFileExtensions(CmsUser currentUser, CmsProject currentProject,
									Hashtable extensions)
		throws CmsException;
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
	public void writeFileHeader(CmsUser currentUser, CmsProject currentProject, 
								CmsFile file)
		throws CmsException;
	 /**
	 * Writes an already existing group in the Cms.<BR/>
	 * 
	 * Only the admin can do this.<P/>
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param group The group that should be written to the Cms.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	public void writeGroup(CmsUser currentUser, CmsProject currentProject, 
						   CmsGroup group)
		throws CmsException;
	/**
	 * Writes a couple of propertyinformation for a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to write the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the propertyinformation 
	 * has to be read.
	 * @param propertyinfos A Hashtable with propertydefinition- propertyinfo-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeProperties(CmsUser currentUser, CmsProject currentProject, 
									  String resource, Hashtable propertyinfos)
		throws CmsException;
	/**
	 * Writes a propertyinformation for a file or folder.
	 * 
	 * <B>Security</B>
	 * Only the user is granted, who has the right to write the resource.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The name of the resource of which the propertyinformation has 
	 * to be read.
	 * @param property The propertydefinition-name of which the propertyinformation has to be set.
	 * @param value The value for the propertyinfo to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeProperty(CmsUser currentUser, CmsProject currentProject, 
									 String resource, String property, String value)
		throws CmsException;
	/**
	 * Updates the propertydefinition for the resource type.<BR/>
	 * 
	 * <B>Security</B>
	 * Only the admin can do this.
	 * 
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param propertydef The propertydef to be deleted.
	 * 
	 * @return The propertydefinition, that was written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsPropertydefinition writePropertydefinition(CmsUser currentUser, 
												   CmsProject currentProject, 
												   CmsPropertydefinition propertydef)
		throws CmsException;
	 /**
	  * Writes a new user tasklog for a task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task .
	  * @param comment Description for the log
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void writeTaskLog(CmsUser currentUser, CmsProject currentProject, 
							  int taskid, String comment)
		 throws CmsException ;
	 /**
	  * Writes a new user tasklog for a task.
	  * 
	  * <B>Security:</B>
	  * All users are granted.
	  * 
	  * @param currentUser The user who requested this method.
	  * @param currentProject The current project of the user.
	  * @param taskid The Id of the task .
	  * @param comment Description for the log
	  * @param tasktype Type of the tasklog. User tasktypes must be greater then 100.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void writeTaskLog(CmsUser currentUser, CmsProject currentProject, 
							  int taskid, String comment, int taskType)
		 throws CmsException;
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
	public void writeUser(CmsUser currentUser, CmsProject currentProject, 
						  CmsUser user)			
		throws CmsException;
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
	public void writeWebUser(CmsUser currentUser, CmsProject currentProject, 
						  CmsUser user)			
		throws CmsException;
}

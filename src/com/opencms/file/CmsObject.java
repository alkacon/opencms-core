package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsObject.java,v $
 * Date   : $Date: 2000/10/11 12:43:27 $
 * Version: $Revision: 1.135 $
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
 * This class provides access to the OpenCms and its resources. 
 * <br>
 * The CmsObject encapsulates user identification and client requests and
 * is the central object to transport information in the Cms Servlet.
 * <br>
 * All operations on the CmsObject are forwarded to the class which extends A_CmsRessourceBroker
 * to ensure user authentification in all operations.
 * 
 * @author Andreas Schouten
 * @author Michaela Schleich
 * @author Michael Emmerich
 *  
 * @version $Revision: 1.135 $ $Date: 2000/10/11 12:43:27 $ 
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
	 * The default constructor.
	 */
	public CmsObject () {
	}
/**
 * Constructor 
 * @param storage The reference to the session storage.
 */
public CmsObject(CmsCoreSession storage) {
	m_sessionStorage = storage;
}
/**
 * Accept a task from the Cms.
 * 
 * @param taskid the id of the task to accept.
 * 
 * @exception CmsException if operation was not successful.
 */
public void acceptTask(int taskId) throws CmsException {
	m_rb.acceptTask(m_context.currentUser(), m_context.currentProject(), taskId);
}
/**
 * Checks, if the user may create this resource.
 * 
 * @param resource the resource to check.
 * @return <code>true</code> if the user has the appropriate rigths to create the resource; <code>false</code> otherwise
 *
 * @exception CmsException if operation was not successful.
 */
public boolean accessCreate(CmsResource resource) throws CmsException {
	try {
		return m_rb.accessCreate(m_context.currentUser(), m_context.currentProject(), resource);
	} catch (Exception exc) {
		throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
	}
}
/**
 * Checks, if the user may lock this resource.
 * 
 * @param resource the resource to check.
 * @return <code>true</code> if the user has the appropriate rights to lock this resource; <code>false</code> otherwise
 *
 * @exception CmsException if operation was not successful.
 */
public boolean accessLock(CmsResource resource) throws CmsException {
	try {
		return m_rb.accessLock(m_context.currentUser(), m_context.currentProject(), resource);
	} catch (Exception exc) {
		throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
	}
}
/**
 * Checks if the user can access the project.
 * 
 * @param projectId the id of the project.
 * @return <code>true</code>, if the user may access this project; <code>false</code> otherwise
 *
 * @exception CmsException if operation was not successful.
 */
public boolean accessProject(int projectId) throws CmsException {
	return (m_rb.accessProject(m_context.currentUser(), m_context.currentProject(), projectId));
}
/**
 * Checks, if the user may read this resource.
 * 
 * @param resource The resource to check.
 * @return <code>true</code>, if the user has the appropriate rigths to read the resource; <code>false</code> otherwise.
 *
 * @exception CmsException if operation was not successful.
 */
public boolean accessRead(CmsResource resource) throws CmsException {
	try {
		return m_rb.accessRead(m_context.currentUser(), m_context.currentProject(), resource);
	} catch (Exception exc) {
		throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
	}
}
/**
 * Checks, if the user may write this resource.
 * 
 * @param resource the resource to check.
 * @return <code>true</code>, if the user has the appropriate rigths to write the resource; <code>false</code> otherwise.
 *
 * @exception CmsException if operation was not successful.
 */
public boolean accessWrite(CmsResource resource) throws CmsException {
	try {
		return m_rb.accessWrite(m_context.currentUser(), m_context.currentProject(), resource);
	} catch (Exception exc) {
		throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
	}
}
/**
 * Adds a file extension to the list of known file extensions. 
 * <p>
 * <b>Security:</b>
 * Only members of the group administrators are allowed to add a file extension.
 * 
 * @param extension a file extension like "html","txt" etc.
 * @param resTypeName name of the resource type associated with the extension.
 *
 * @exception CmsException if operation was not successful.
 */

public void addFileExtension(String extension, String resTypeName) throws CmsException {
	m_rb.addFileExtension(m_context.currentUser(), m_context.currentProject(), extension, resTypeName);
}
/**
 * Adds a new group to the Cms.
 * <p>
 * <b>Security:</b>
 * Only members of the group administrators are allowed to add a new group.
 * 
 * @param name the name of the new group.
 * @param description the description of the new group.
 * @int flags the flags for the new group.
 *
 * @return a <code>CmsGroup</code> object representing the newly created group.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsGroup addGroup(String name, String description, int flags, String parent) throws CmsException {
	return (m_rb.addGroup(m_context.currentUser(), m_context.currentProject(), name, description, flags, parent));
}
/**
 * Adds a resource type to the Cms.
 * <p>
 * <b>Security:</b>
 * Only members of the group administrators are allowed to add a resource type.
 * 
 * @param resourceType the name of the resource to get.
 * @param launcherType the launcherType-id.
 * @param launcherClass the name of the launcher-class normaly "".
 * 
 * @return a <code>CmsResourceType</code> object representing the new resource type.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsResourceType addResourceType(String resourceType, int launcherType, String launcherClass) throws CmsException {
	return (m_rb.addResourceType(m_context.currentUser(), m_context.currentProject(), resourceType, launcherType, launcherClass));
}
/** 
 * Adds a user to the Cms.
 * <p>
 * <b>Security:</b>
 * Only members of the group administrators are allowed to add a user.
 * 
 * @param name the new name for the user.
 * @param password the new password for the user.
 * @param group the default groupname for the user.
 * @param description the description for the user.
 * @param additionalInfos a Hashtable with additional infos for the user. These
 * Infos may be stored into the Usertables (depending on the implementation).
 * @param flags the flags for a user (e.g. C_FLAG_ENABLED).
 * 
 * @return a <code>CmsUser</code> object representing the added user.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsUser addUser(String name, String password, String group, String description, Hashtable additionalInfos, int flags) throws CmsException {
	return (m_rb.addUser(m_context.currentUser(), m_context.currentProject(), name, password, group, description, additionalInfos, flags));
}
/**
 * Adds a user to a group.
 * <p>
 * <b>Security:</b>
 * Only members of the group administrators are allowed to add a user to a group.
 * 
 * @param username the name of the user that is to be added to the group.
 * @param groupname the name of the group.
 * @exception CmsException if operation was not successful.
 */
public void addUserToGroup(String username, String groupname) throws CmsException {
	m_rb.addUserToGroup(m_context.currentUser(), m_context.currentProject(), username, groupname);
}
/** 
 * Adds a web user to the Cms.
 * <br>
 * A web user has no access to the workplace but is able to access personalized
 * functions controlled by the OpenCms.
 *
 * @param name the new name for the user.
 * @param password the new password for the user.
 * @param group the default groupname for the user.
 * @param description the description for the user.
 * @param additionalInfos a Hashtable with additional infos for the user. These
 * Infos may be stored into the Usertables (depending on the implementation).
 * @param flags the flags for a user (e.g. C_FLAG_ENABLED)
 * 
 * @return a <code>CmsUser</code> object representing the newly created user.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsUser addWebUser(String name, String password, String group, String description, Hashtable additionalInfos, int flags) throws CmsException {
	return (m_rb.addWebUser(m_context.currentUser(), m_context.currentProject(), name, password, group, description, additionalInfos, flags));
}
/**
 * Returns the anonymous user object.
 * 
 * @return a <code>CmsUser</code> object representing the anonymous user.
 * @exception CmsException if operation was not successful.
 */
public CmsUser anonymousUser() throws CmsException {
	return (m_rb.anonymousUser(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Changes the group of a resource.
 * <br>
 * Only the group of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * <p>
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user is owner of the resource or is admin</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 * 
 * @param filename the complete path to the resource.
 * @param newGroup the name of the new group for this resource.
 * 
 * @exception CmsException if operation was not successful.
 */
public void chgrp(String filename, String newGroup) throws CmsException {
	m_rb.chgrp(m_context.currentUser(), m_context.currentProject(), filename, newGroup);
}
/**
 * Changes the flags of a resource.
 * <br>
 * Only the flags of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * The user may change the flags, if he is admin of the resource.
 * <p>
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user can write the resource</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 * 
 * @param filename the complete path to the resource.
 * @param flags the new flags for the resource.
 * 
 * @exception CmsException if operation was not successful.
 * for this resource.
 */
public void chmod(String filename, int flags) throws CmsException {
	m_rb.chmod(m_context.currentUser(), m_context.currentProject(), filename, flags);
}
/**
 * Changes the owner of a resource.
 * <br>
 * Only the owner of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * The user may change this, if he is admin of the resource. 
 * <p>
 * <B>Security:</B>
 * Access is cranted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user is owner of the resource or the user is admin</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 * 
 * @param filename the complete path to the resource.
 * @param newOwner the name of the new owner for this resource.
 * 
 * @exception CmsException if operation was not successful.
 */
public void chown(String filename, String newOwner) throws CmsException {
	m_rb.chown(m_context.currentUser(), m_context.currentProject(), filename, newOwner);
}
/**
 * Changes the state of a resource.
 * <br>
 * Only the state of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not existing in the offline project already,
 * it is read from the online project and written into the offline project.
 * The user may change this, if he is admin of the resource.
 * <p>
 * <B>Security:</B>
 * Access is cranted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user is owner of the resource or the user is admin</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 * 
 * @param filename the complete path to the resource.
 * @param state the new state of this resource.
 * 
 * @exception CmsException if operation was not successful.
 */
public void chstate(String filename, int state) throws CmsException {
	m_rb.chstate(m_context.currentUser(), m_context.currentProject(), filename, state);
}
/**
 * Changes the resourcetype of a resource.
 * <br>
 * Only the resourcetype of a resource in an offline project can be changed. The state
 * of the resource is set to CHANGED (1).
 * If the content of this resource is not exisiting in the offline project already,
 * it is read from the online project and written into the offline project.
 * The user may change this, if he is admin of the resource. 
 * <p>
 * <B>Security:</B>
 * Access is granted, if:
 * <ul>
 * <li>the user has access to the project</li>
 * <li>the user is owner of the resource or is admin</li>
 * <li>the resource is locked by the callingUser</li>
 * </ul>
 * 
 * @param filename the complete path to the resource.
 * @param newType the name of the new resourcetype for this resource.
 * 
 * @exception CmsException if operation was not successful.
 */
public void chtype(String filename, String newType) throws CmsException {
	m_rb.chtype(m_context.currentUser(), m_context.currentProject(), filename, newType);
}
/**
 * Clears all internal DB-Caches.
 */
public void clearcache() {
	m_rb.clearcache();
}
/**
 * Copies a file.
 * 
 * @param source the complete path of the sourcefile.
 * @param destination the complete path of the destinationfolder.
 * 
 * @exception CmsException if the file couldn't be copied, or the user
 * has not the appropriate rights to copy the file.
 */
public void copyFile(String source, String destination) throws CmsException {
	m_rb.copyFile(m_context.currentUser(), m_context.currentProject(), source, destination);
}
/**
 * Copies a folder.
 * 
 * @param source the complete path of the sourcefolder.
 * @param destination the complete path of the destinationfolder.
 * 
 * @exception CmsException if the folder couldn't be copied, or if the
 * user has not the appropriate rights to copy the folder.
 */
public void copyFolder(String source, String destination) throws CmsException {
	m_rb.copyFolder(m_context.currentUser(), m_context.currentProject(), source, destination);
}
/**
 * Insert the method's description here.
 * Creation date: (09-10-2000 09:25:41)
 * @return java.util.Vector
 * @param fromProject com.opencms.file.CmsProject
 * @exception com.opencms.core.CmsException The exception description.
 * @author Martin Langelund
 */
public Vector copyProjectToProject(CmsProject fromProject) throws com.opencms.core.CmsException
{
	Vector current = m_rb.readResources(m_context.currentProject());
	Vector from = m_rb.readResources(fromProject);
	Vector toBeCopied = new Vector();
	Vector notCopied = new Vector();
	Hashtable compare = new Hashtable();
	if (current.size() == 0)
		toBeCopied = current;
	for (int i = 0; i < current.size(); i++)
		compare.put(((CmsResource) current.elementAt(i)).getAbsolutePath() + ((CmsResource) current.elementAt(i)).getName(), current.elementAt(i));
	for (int i = 0; i < from.size(); i++)
	{
		if (compare.containsKey(((CmsResource) from.elementAt(i)).getAbsolutePath() + ((CmsResource) from.elementAt(i)).getName()))
			notCopied.addElement(from.elementAt(i));
		else
			toBeCopied.addElement(from.elementAt(i));
	}
	/*	
	System.out.println("****************** current: ************************");
	for (int i=0; i<current.size(); i++)
	{
	System.out.println(((CmsResource) current.elementAt(i)).getAbsolutePath()+((CmsResource) current.elementAt(i)).getName());
	}
	System.out.println("****************** from: ***************************");
	for (int i=0; i<from.size(); i++)
	{
	System.out.println(((CmsResource) from.elementAt(i)).getAbsolutePath()+((CmsResource) from.elementAt(i)).getName());
	}
	System.out.println("****************** notCopied: ***************************");
	for (int i=0; i<notCopied.size(); i++)
	{
	System.out.println(((CmsResource) notCopied.elementAt(i)).getAbsolutePath()+((CmsResource) notCopied.elementAt(i)).getName());
	}
	System.out.println("****************** toBeCopied: ***************************");
	for (int i=0; i<toBeCopied.size(); i++)
	{
	System.out.println(((CmsResource) toBeCopied.elementAt(i)).getAbsolutePath()+((CmsResource) toBeCopied.elementAt(i)).getName());
	}
	*/
	for (int i = 0; i < toBeCopied.size(); i++)
	{
		CmsResource resource = (CmsResource) toBeCopied.elementAt(i);
		resource.setLocked(-1);
		resource.setGroupId(m_context.currentProject().getGroupId());
		resource.setUserId(m_context.currentProject().getOwnerId());
		m_rb.copyResourceToProject(m_context.currentProject(), fromProject, resource);
	}
	return notCopied;
}
/**
 * Insert the method's description here.
 * Creation date: (06-10-2000 08:55:42)
 * @param fromProject com.opencms.file.CmsProject
 * @param resource java.lang.String
 * @exception com.opencms.core.CmsException The exception description.
 * @author Martin Langelund
 */
public void copyResourceToProject(CmsProject fromProject, String resource) throws com.opencms.core.CmsException
{
	m_rb.copyResourceToProject(m_context.currentUser(), m_context.currentProject(), fromProject, resource);
}
/**
 * Copies a resource from the online project to a new, specified project.
 * <br>
 * Copying a resource will copy the file header or folder into the specified 
 * offline project and set its state to UNCHANGED.
 * 
 * @param resource the name of the resource.
 	 * @exception CmsException if operation was not successful.
 */
public void copyResourceToProject(String resource) throws CmsException {
	m_rb.copyResourceToProject(m_context.currentUser(), m_context.currentProject(), resource);
}
/**
 * Returns the copyright information for this OpenCms.
 * 
 * @return copyright a String arry containing copyright information.
 */
public String[] copyright() {
	return C_COPYRIGHT;
}
/**
 * Counts the locked resources in a project.
 * 
 * @param id the id of the project
 * @return the number of locked resources in this project.
 * 
 * @exception CmsException if operation was not successful.
 */
public int countLockedResources(int id) throws CmsException {
	return m_rb.countLockedResources(m_context.currentUser(), m_context.currentProject(), id);
}
/**
 * Creates a new category
 *
 * Only a adminstrator can do this.<P/>
 * 
 * <B>Security:</B>
 * @return com.opencms.file.CmsCategory
 * @param name java.lang.String
 * @param description java.lang.String
 * @param shortName java.lang.String
 * @param priority int
 * @exception com.opencms.core.CmsException The exception description.
 */
public CmsCategory createCategory(String name, String description, String shortName, int priority) throws com.opencms.core.CmsException
{
	return m_rb.createCategory(m_context.currentUser(), m_context.currentProject(), name, description, shortName, priority);
}
/**
 * Creates a new country
 *
 * Only a adminstrator can do this.<P/>
 * 
 * <B>Security:</B>
 * @return com.opencms.file.CmsCountry
 * @param name java.lang.String
 * @param shortName java.lang.String
 * @param priority int
 * @exception com.opencms.core.CmsException The exception description.
 */
public CmsCountry createCountry(String name, String shortName, int priority) throws com.opencms.core.CmsException
{
	return m_rb.createCountry(m_context.currentUser(), m_context.currentProject(), name, shortName, priority);
}
/**
 * Creates a new file with the given content and resourcetype.<br>
 * 
 * @param folder the complete path to the folder in which the file will be created.
 * @param filename the name of the new file.
 * @param contents the contents of the new file.
 * @param type the resourcetype of the new file.
 * 
 * @return file a <code>CmsFile</code> object representing the newly created file.
 * 
 * @exception CmsException if the mandatory property-definitions for this file are missing 
 * or if the resourcetype is set to folder. The CmsException is also thrown, if the 
 * filename is not valid or if the user has not the appropriate rights to create a new file.
 */
public CmsFile createFile(String folder, String filename, byte[] contents, String type) throws CmsException {
	return (m_rb.createFile(m_context.currentUser(), m_context.currentGroup(), m_context.currentProject(), folder, filename, contents, type, new Hashtable()));
}
/**
 * Creates a new file with the given content and resourcetype.
 * 
 * @param folder the complete path to the folder in which the file will be created.
 * @param filename the name of the new file.
 * @param contents the contents of the new file.
 * @param type the resourcetype of the new file.
 * @param properties A Hashtable of properties, that should be set for this file.
 * The keys for this Hashtable are the names for properties, the values are
 * the values for the properties.
 * 
 * @return file a <code>CmsFile</code> object representing the newly created file.
 * 
 * @exception CmsException if the mandatory property-definitions for this file are missing,
 * the wrong properties are given, or if the resourcetype is set to folder. 
 * The CmsException is also thrown, if the filename is not valid or if the user 
 * has not the appropriate rights to create a new file.
 */
public CmsFile createFile(String folder, String filename, byte[] contents, String type, Hashtable properties) throws CmsException {
	return (m_rb.createFile(m_context.currentUser(), m_context.currentGroup(), m_context.currentProject(), folder, filename, contents, type, properties));
}
/**
 * Creates a new folder.
 * 
 * @param folder the complete path to the folder in which the new folder 
 * will be created.
 * @param newFolderName the name of the new folder.
 * 
 * @return folder a <code>CmsFolder</code> object representing the newly created folder.
 * 
 * @exception CmsException if the mandatory property-definitions for this folder are missing 
 * , the foldername is not valid, or if the user has not the appropriate rights to create 
 * a new folder.
 */
public CmsFolder createFolder(String folder, String newFolderName) throws CmsException {
	return (m_rb.createFolder(m_context.currentUser(), m_context.currentGroup(), m_context.currentProject(), folder, newFolderName, new Hashtable()));
}
/**
 * Creates a new folder.
 * 
 * @param folder the complete path to the folder in which the new folder will 
 * be created.
 * @param newFolderName the name of the new folder.
 * @param properties A Hashtable of properties, that should be set for this folder.
 * The keys for this Hashtable are the names for property-definitions, the values are
 * the values for the properties.
 * 
 * @return a <code>CmsFolder</code> object representing the newly created folder.
 * @exception CmsException if the mandatory property-definitions for this folder are missing 
 * , the foldername is not valid, or if the user has not the appropriate rights to create 
 * a new folder.
 *
 */
public CmsFolder createFolder(String folder, String newFolderName, Hashtable properties) throws CmsException {
	return (m_rb.createFolder(m_context.currentUser(), m_context.currentGroup(), m_context.currentProject(), folder, newFolderName, properties));
}
/**
 * Creates a new language
 *
 * Only a adminstrator can do this.<P/>
 * 
 * <B>Security:</B>
 * @return com.opencms.file.CmsLanguage
 * @param name java.lang.String
 * @param shortName java.lang.String
 * @param priority int
 * @exception com.opencms.core.CmsException The exception description.
 */
public CmsLanguage createLanguage(String name, String shortName, int priority) throws com.opencms.core.CmsException
{
	return m_rb.createLanguage(m_context.currentUser(), m_context.currentProject(), name, shortName, priority);
}
/**
  * Creates a new project for task handling.
  * 
  * @param projectname the name of the project
  * @param projectType the type of the Project
  * @param role a Usergroup for the project
  * @param timeout the time when the Project must finished
  * @param priority  a Priority for the Project
  * 
  * @return a <code>CmsTask</code> object representing the newly created task.
  * 
  * @exception CmsException if operation was not successful.
  */
public CmsTask createProject(String projectname, int projectType, String roleName, long timeout, int priority) throws CmsException {
	return m_rb.createProject(m_context.currentUser(), projectname, projectType, roleName, timeout, priority);
}
/**
 * Creates a new project.
 * 
 * @param name the name of the project to read.
 * @param description the description for the new project.
 * @param groupname the name of the group to be set.
 * @param managergroupname the name of the managergroup to be set.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsProject createProject(String name, String description, String groupname, String managergroupname) throws CmsException
{
	CmsProject newProject = m_rb.createProject(m_context.currentUser(), m_context.currentProject(), name, description, groupname, managergroupname);
//	if (CmsConstants.USE_MULTISITE)
//	{
		CmsSite cs = getSite((m_context.currentProject()).getId());
		m_rb.newSiteProjectsRecord(
			m_context.currentUser(), 
			m_context.currentProject(), 
			cs.getId(), newProject.getId());
//	}
	return (newProject);
}
/**
 * Creates a new project.
 * 
 * @param name the name of the project to read.
 * @param description the description for the new project.
 * @param groupname the name of the group to be set.
 * @param managergroupname the name of the managergroup to be set.
 * 
 * @exception CmsException if operation was not successful.
 * @author Martin Langelund
 */
public CmsProject createProject(String name, String description, String groupname, String managergroupname, int parentId) throws CmsException
{
	CmsProject newProject = m_rb.createProject(m_context.currentUser(), m_context.currentProject(), name, description, groupname, managergroupname, parentId);
	CmsSite cs = getSite((m_context.currentProject()).getId());
	m_rb.newSiteProjectsRecord(m_context.currentUser(), m_context.currentProject(), cs.getId(), newProject.getId());
	return (newProject);
}
/**
 * Creates the property-definition for a resource type.
 * 
 * @param name the name of the property-definition to overwrite.
 * @param resourcetype the name of the resource-type for the property-definition.
 * @param type the type of the property-definition (normal|mandatory|optional)
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsPropertydefinition createPropertydefinition(String name, String resourcetype, int type) throws CmsException {
	return (m_rb.createPropertydefinition(m_context.currentUser(), m_context.currentProject(), name, resourcetype, type));
}
/**
  * Creates a new task.
  * <p>
  * <B>Security:</B>
  * All users can create a new task.
  * 
  * @param projectid the Id of the current project task of the user.
  * @param agentname the User who will edit the task. 
  * @param rolename a Usergroup for the task.
  * @param taskname a Name of the task.
  * @param tasktype the type of the task.
  * @param taskcomment a description of the task.
  * @param timeout the time when the task must finished.
  * @param priority the Id for the priority of the task.
  * 
  * @return a <code>CmsTask</code> object representing the newly created task.
  * 
  * @exception CmsException Throws CmsException if something goes wrong.
  */
public CmsTask createTask(int projectid, String agentName, String roleName, String taskname, String taskcomment, int tasktype, long timeout, int priority) throws CmsException {
	return m_rb.createTask(m_context.currentUser(), projectid, agentName, roleName, taskname, taskcomment, tasktype, timeout, priority);
}
/**
  * Creates a new task.
  * <p>
  * <B>Security:</B>
  * All users can create a new task.
  * @param agent the User who will edit the task. 
  * @param role a Usergroup for the task.
  * @param taskname the name of the task.
  * @param taskcomment a description of the task.
  * @param timeout the time when the task must finished.
  * @param priority the Id for the priority of the task.
  * 
  * @return a <code>CmsTask</code> object representing the newly created task.
  * 
  * @exception CmsException if operation was not successful.
  */
public CmsTask createTask(String agentName, String roleName, String taskname, String taskcomment, long timeout, int priority) throws CmsException {
	return (m_rb.createTask(m_context.currentUser(), m_context.currentProject(), agentName, roleName, taskname, taskcomment, timeout, priority));
}
/**
 * Deletes all properties for a file or folder.
 * 
 * @param resourcename the name of the resource for which all properties should be deleted.
 * 
 * @exception CmsException if operation was not successful.
 */
public void deleteAllProperties(String resourcename) throws CmsException {
	m_rb.deleteAllProperties(m_context.currentUser(), m_context.currentProject(), resourcename);
}
/**
 * Deletes a file.
 * 
 * @param filename the complete path of the file.
 * 
 * @exception CmsException if the file couldn't be deleted, or if the user
 * has not the appropriate rights to delete the file.
 */
public void deleteFile(String filename) throws CmsException {
	m_rb.deleteFile(m_context.currentUser(), m_context.currentProject(), filename);
}
/**
 * Deletes a folder.
 * <br>
 * This is a very complex operation, because all sub-resources may be
 * deleted too.
 * 
 * @param foldername the complete path of the folder.
 * 
 * @exception CmsException if the folder couldn't be deleted, or if the user 
 * has not the rights to delete this folder.
 */
public void deleteFolder(String foldername) throws CmsException {
	m_rb.deleteFolder(m_context.currentUser(), m_context.currentProject(), foldername);
}
/**
 * Deletes a group.
 * <p>
 * <b>Security:</b>
 * Only the admin user is allowed to delete a group.
 * 
 * @param delgroup the name of the group.
 * @exception CmsException  if operation was not successful.
 */
public void deleteGroup(String delgroup) throws CmsException {
	m_rb.deleteGroup(m_context.currentUser(), m_context.currentProject(), delgroup);
}
/**
 * Deletes a project.
 * 
 * @param id the id of the project.
 * 
 * @exception CmsException if operation was not successful.
 */
public void deleteProject(int id) throws CmsException {
	m_rb.deleteProject(m_context.currentUser(), m_context.currentProject(), id);
}
/**
 * Deletes a property for a file or folder.
 * 
 * @param resourcename the name of a resource for which the property should be deleted.
 * @param property the name of the property. 
 * 
 * @exception CmsException Throws if operation was not successful.
 */
public void deleteProperty(String resourcename, String property) throws CmsException {
	m_rb.deleteProperty(m_context.currentUser(), m_context.currentProject(), resourcename, property);
}
/**
 * Deletes the property-definition for a resource type.
 * 
 * @param name the name of the property-definition to delete.
 * @param resourcetype the name of the resource-type for the property-definition.
 * 
 * @exception CmsException if operation was not successful.
 */
public void deletePropertydefinition(String name, String resourcetype) throws CmsException {
	m_rb.deletePropertydefinition(m_context.currentUser(), m_context.currentProject(), name, resourcetype);
}
/**
 * Marks a site deleted
 *
 * Only a adminstrator can do this.<P/>
 * 
 * <B>Security:</B>
 * Only users, which are in the group "administrators" are granted.
 * Creation date: (28-09-2000 11:10:05)
 * @param siteId int
 * @exception com.opencms.core.CmsException The exception description.
 */
public void deleteSite(int siteId) throws com.opencms.core.CmsException
{
	m_rb.deleteSite(m_context.currentUser(), m_context.currentProject(), siteId);
}
/** 
 * Deletes a user from the Cms.
 * <p>
 * <b>Security:</b>
 * Only a admin user is allowed to delete a user.
 * 
 * @param name the Id of the user to be deleted.
 * 
 * @exception CmsException if operation was not successful.
 */
public void deleteUser(int userId) throws CmsException {
	m_rb.deleteUser(m_context.currentUser(), m_context.currentProject(), userId);
}
/** 
 * Deletes a user from the Cms.
 * <p>
 * <b>Security:</b>
 * Only a admin user is allowed to delete a user.
 * 
 * @param name the name of the user to be deleted.
 * 
 * @exception CmsException if operation was not successful.
 */
public void deleteUser(String username) throws CmsException {
	m_rb.deleteUser(m_context.currentUser(), m_context.currentProject(), username);
}
/** 
 * Deletes a web user from the Cms.
 * 
 * @param name the id of the user to be deleted.
 * 
 * @exception CmsException if operation was not successful.
 */
public void deleteWebUser(int userId) throws CmsException {
	m_rb.deleteWebUser(m_context.currentUser(), m_context.currentProject(), userId);
}
/**
 * Destroys the resource borker and required modules and connections.
 * @exception CmsException if operation was not successful.
 */
public void destroy() throws CmsException {
	m_rb.destroy();
}
/**
 * Ends a task of the Cms.
 * 
 * @param taskid the ID of the task to end.
 * 
 * @exception CmsException if operation was not successful.
 */
public void endTask(int taskid) throws CmsException {
	m_rb.endTask(m_context.currentUser(), m_context.currentProject(), taskid);
}
/**
 * Exports cms-resources to a zip-file.
 * 
 * @param exportFile the name (absolute Path) of the export resource (zip-file).
 * @param exportPath the name (absolute Path) of folder from which should be exported.
 * 
 * @exception CmsException if operation was not successful.
 */
public void exportResources(String exportFile, String[] exportPaths) throws CmsException {
	// export the resources
	m_rb.exportResources(m_context.currentUser(), m_context.currentProject(), exportFile, exportPaths, this);
}
/**
 * Exports cms-resources to a zip-file.
 * 
 * @param exportFile the name (absolute Path) of the export resource (zip-file).
 * @param exportPath the name (absolute Path) of folder from which should be exported.
 * @param includeSystem indicates if the system resources will be included in the export.
 * 
 * @exception CmsException if operation was not successful.
 */
public void exportResources(String exportFile, String[] exportPaths, boolean includeSystem) throws CmsException {
	// export the resources
	m_rb.exportResources(m_context.currentUser(), m_context.currentProject(), exportFile, exportPaths, this, includeSystem);
}
/**
 * Forwards a task to a new user.
 * 
 * @param taskid the id of the task which will be forwarded.
 * @param newRole the new group for the task.
 * @param newUser the new user who gets the task.
 * 
 * @exception CmsException if operation was not successful.
 */
public void forwardTask(int taskid, String newRoleName, String newUserName) throws CmsException {
	m_rb.forwardTask(m_context.currentUser(), m_context.currentProject(), taskid, newRoleName, newUserName);
}
/**
 * Returns all projects, which the current user can access.
 * 
 * @return a Vector of objects of type <code>CmsProject</code>.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector getAllAccessibleProjects() throws CmsException {
	return (m_rb.getAllAccessibleProjects(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Returns all categories.
 * 
 * @return all categories.
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public Vector getAllCategories() throws com.opencms.core.CmsException
{
	return m_rb.getAllCategories(m_context.currentUser(), m_context.currentProject());
}
/**
 * Returns all countries
 * @return java.util.Vector all countries
 * @exception com.opencms.core.CmsException The exception description.
 */
public Vector getAllCountries() throws com.opencms.core.CmsException
{
	return m_rb.getAllCountries(m_context.currentUser(), m_context.currentProject());
}
/**
 * Returns all languages
 * @return java.util.Vector all languages
 * @exception com.opencms.core.CmsException The exception description.
 */
public Vector getAllLanguages() throws com.opencms.core.CmsException
{
	return m_rb.getAllLanguages(m_context.currentUser(), m_context.currentProject());
}
/**
 * Returns all projects which are owned by the current user or which are manageable
 * for the group of the user.
 * 
 * @return a Vector of objects of type <code>CmsProject</code>.
 *
 * @exception CmsException if operation was not successful.
 */
public Vector getAllManageableProjects() throws CmsException {
	return (m_rb.getAllManageableProjects(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Returns a Hashtable with all I_CmsResourceTypes.
 * 
 * @rerun returns a Vector with all I_CmsResourceTypes.
 *
 * @exception CmsException if operation was not successful.
 */
public Hashtable getAllResourceTypes() throws CmsException {
	return (m_rb.getAllResourceTypes(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Returns a vector containing all sites of the current project.
 * 
 * @return vector containing all sites of the current project.
 * @exception CmsException if operation was not successful.
 */
public Vector getAllSites() throws CmsException {
	return m_rb.getAllSites(m_context.currentUser(), m_context.currentProject());
}
/**
 * Returns a vector containing all sites with the specified category id.
 *
 * @author Jesper Holme
 * @author Jan Krag
 *
 * @return vector containing all sites with specified category-id
 * @exception CmsException if operation was not successful.
 */
public Vector getAllSitesInCategory(int category) throws CmsException
{
	Enumeration sites = m_rb.getAllSites(m_context.currentUser(), m_context.currentProject()).elements();
	Vector filteredSites = new Vector();
	while (sites.hasMoreElements())
	{
		CmsSite site = (CmsSite) sites.nextElement();
		if (site.getCategoryId() == category)
		{
			filteredSites.addElement(site);
		}
	}
	return filteredSites;
}
/**
 * Returns all site urls
 * @return java.util.Vector site urls
 * @exception com.opencms.core.CmsException The exception description.
 */
public Vector getAllSiteUrls() throws com.opencms.core.CmsException
{
	return m_rb.getAllSiteUrls(m_context.currentUser(), m_context.currentProject());
}
/**
* Gets information about the cache size.
* <br>
* The size of the following caching areas is returned:
* <ul>
*  <li>GroupCache</li>
*  <li>UserGroupCache</li>
*  <li>ResourceCache</li>
*  <li>SubResourceCache</li>
*  <li>ProjectCache</li>
*  <li>PropertyCache</li>
*  <li>PropertyDefinitionCache</li>
*  <li>PropertyDefinitionVectorCache</li>
* </ul>
* @ return a Hashtable with information about the size of the various cache areas. 
*/
public Hashtable getCacheInfo() {
	return m_rb.getCacheInfo();
}
/**
 * Returns a CmsCategory object
 *
 * @param categoryId the category_id 
 * @return a CmsCategory object according to the categoryId
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public CmsCategory getCategory(int categoryId) throws CmsException
{
	return m_rb.getCategory(m_context.currentUser(), m_context.currentProject(), categoryId);
}
/**
 * Returns all child groups of a group.
 * 
 * @param groupname the name of the group.
 * @return groups a Vector of all child groups or null.
 * @exception CmsException if operation was not successful.
 */
public Vector getChild(String groupname) throws CmsException {
	return (m_rb.getChild(m_context.currentUser(), m_context.currentProject(), groupname));
}
/**
 * Returns all child groups of a group.
 * <br>
 * This method also returns all sub-child groups of the current group.
 * 
 * @param groupname the name of the group.
 * @return groups a Vector of all child groups or null.
 * @exception CmsException if operation was not successful.
 */
public Vector getChilds(String groupname) throws CmsException {
	return (m_rb.getChilds(m_context.currentUser(), m_context.currentProject(), groupname));
}
/**
 * Gets the configurations of the properties-file.
 * @return the configurations of the properties-file.
 */
public Configurations getConfigurations() {
	return m_rb.getConfigurations(getRequestContext().currentUser(), getRequestContext().currentProject());
}
/**
 * Returns a CmsCountry object
 * Creation date: (02-10-2000 17:15:36)
 * @return com.opencms.file.CmsCountry
 * @param countryId int
 * @exception com.opencms.core.CmsException The exception description.
 */
public CmsCountry getCountry(int countryId) throws com.opencms.core.CmsException
{
	return m_rb.getCountry(m_context.currentUser(), m_context.currentProject(), countryId);
}
/**
 * Insert the method's description here.
 * Creation date: (02-10-2000 16:47:15)
 * @return com.opencms.file.CmsSite
 * @exception com.opencms.core.CmsException The exception description.
 */
public CmsSite getCurrentSite() throws com.opencms.core.CmsException
{
	return this.getSite(this.onlineProject().getId());
}
/**
 * Gets all groups to which a given user directly belongs.
 * 
 * @param username the name of the user to get all groups for.
 * @return a Vector of all groups of a user.
 * 
 * @exception CmsException if operation was not successful.
 */
public Vector getDirectGroupsOfUser(String username) throws CmsException {
	return (m_rb.getDirectGroupsOfUser(m_context.currentUser(), m_context.currentProject(), username));
}
/**
 * Returns a Vector with all files of a given folder.
 * <br>
 * Files of a folder can be read from an offline Project and the online Project.
 * 
 * @param foldername the complete path to the folder.
 * 
 * @return subfiles a Vector with all files of the given folder.
 * 
 * @exception CmsException if the user has not hte appropriate rigths to access or read the resource.
 */
public Vector getFilesInFolder(String foldername) throws CmsException {
	return (m_rb.getFilesInFolder(m_context.currentUser(), m_context.currentProject(), foldername));
}
/**
 * Returns a Vector with all resource-names of the resources that have set the given property to the given value.
 * 
 * @param propertydef the name of the property-definition to check.
 * @param property the value of the property for the resource.
 * 
 * @return a Vector with all names of the resources.
 * 
 * @exception CmsException if operation was not successful.
 */
public Vector getFilesWithProperty(String propertyDefinition, String propertyValue) throws CmsException {
	return m_rb.getFilesWithProperty(m_context.currentUser(), m_context.currentProject(), propertyDefinition, propertyValue);
}
/**
 * This method can be called, to determine if the file-system was changed in the past.
 * <br>
 * A module can compare its previously stored number with the returned number. 
 * If they differ, the file system has been changed.
 * 
 * @return the number of file-system-changes.
 */
public long getFileSystemChanges() {
	return (m_rb.getFileSystemChanges(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Returns all groups in the Cms.
 *  
 * @return a Vector of all groups in the Cms.
 * 
 * @exception CmsException if operation was not successful
 */
public Vector getGroups() throws CmsException {
	return (m_rb.getGroups(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Gets all groups of a user.
 * 
 * @param username the name of the user to get all groups for.
 * @return Vector of all groups of a user.
 * 
 * @exception CmsException if operation was not succesful.
 */
public Vector getGroupsOfUser(String username) throws CmsException {
	return (m_rb.getGroupsOfUser(m_context.currentUser(), m_context.currentProject(), username));
}
/**
 * Returns a CmsLanguage object
 * Creation date: (02-10-2000 17:14:29)
 * @return com.opencms.file.CmsLanguage
 * @param languageId int
 * @exception com.opencms.core.CmsException The exception description.
 */
public CmsLanguage getLanguage(int languageId) throws com.opencms.core.CmsException
{
	return m_rb.getLanguage(m_context.currentUser(), m_context.currentProject(), languageId);
}
/**
 * Returns the parent group of a group.
 * 
 * @param groupname the name of the group.
 * @return group the parent group or null.
 * @exception CmsException if operation was not successful.
 */
public CmsGroup getParent(String groupname) throws CmsException {
	return (m_rb.getParent(m_context.currentUser(), m_context.currentProject(), groupname));
}
/**
 * return the parrent site based on the given site. 
 * Creation date: (10/04/00 %r)
 * @return com.opencms.file.CmsSite if there is a parrent site, or null if the no site is found.
 *
 * @param site the child of the returned site.
 * @exception com.opencms.core.CmsException The exception description.
 */
public CmsSite getParentSite(CmsSite site) throws com.opencms.core.CmsException {
	CmsProject project = m_rb.readProject(m_context.currentUser(),m_context.currentProject(),site.getOnlineProjectId());
	if (project.getParentId() != -1)
	  return m_rb.getSite(m_context.currentUser(),m_context.currentProject(),project.getParentId());
	else 
	  return null;
}
/**
 * Gets the Registry.
 *
 *
 * @exception CmsException if access is not allowed.
 */

public I_CmsRegistry getRegistry() throws CmsException {
	return (m_rb.getRegistry(m_context.currentUser(), m_context.currentProject(), this));
}
/**
 * Returns the current request-context.
 * 
 * @return the current request-context.
 */
public CmsRequestContext getRequestContext() {
	return (m_context);
}
/**
 * Returns a I_CmsResourceType.
 * 
 * @param resourceType the id of the resource to get.
 * 
 * @return a CmsResourceType.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsResourceType getResourceType(int resourceType) throws CmsException {
	return (m_rb.getResourceType(m_context.currentUser(), m_context.currentProject(), resourceType));
}
/**
 * Returns a I_CmsResourceType.
 * 
 * @param resourceType the name of the resource to get.
 * 
 * @return a CmsResourceType.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsResourceType getResourceType(String resourceType) throws CmsException {
	return (m_rb.getResourceType(m_context.currentUser(), m_context.currentProject(), resourceType));
}
/**
 * Returns the session storage.
 * @return the storage of all active users.
 */
public CmsCoreSession getSessionStorage() {
	return m_rb.getSessionStorage(m_context.currentUser(), m_sessionStorage);
}
/**
 * Return the online site of the project (the base project)
 * @return com.opencms.file.CmsSite the site for the project.
 * @param projectId int the project to be used in the lookup.
 * @throws CmsException If any error occured when looking up the site.
 */


public CmsSite getSite(int projectId) throws CmsException {
	return m_rb.getSite(m_context.currentUser(), m_context.currentProject(), projectId);
}
/**
 * Get the site based on the name of the site.
 *
 * @return com.opencms.file.CmsSite the site found.
 * @param url java.lang.String the name of the site to find.
 * @exception com.opencms.core.CmsException if an error occurs. The CmsException should be specialized.
 */

public CmsSite getSite(String siteName) throws CmsException {
	return m_rb.getSite(m_context.currentUser(), m_context.currentProject(), siteName);
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
 * Return the online site of the project (the base project)
 * @return com.opencms.file.CmsSite the site for the project.
 * @param projectId int the project to be used in the lookup.
 * @throws CmsException If any error occured when looking up the site.
 */


public CmsSite getSiteBySiteId(int siteId) throws CmsException {
	return m_rb.getSiteBySiteId(m_context.currentUser(), m_context.currentProject(), siteId);
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
 * Returns a vector containing all sites of the current project.
 * 
 * @return vector containing all sites of the current project.
 * @exception CmsException if operation was not successful.
 */
public Vector getSiteMatrixInfo() throws CmsException {
	return m_rb.getSiteMatrixInfo(m_context.currentUser(), m_context.currentProject());
}
/**
 * Returns all site urls for a specifik site
 * Creation date: (28-09-2000 15:23:38)
 * @return java.util.Vector
 * @param siteId int
 * @exception com.opencms.core.CmsException The exception description.
 */
public Vector getSiteUrls(int siteId) throws com.opencms.core.CmsException
{
	return m_rb.getSiteUrls(m_context.currentUser(), m_context.currentProject(), siteId);
}
/**
 * Returns a Vector with all subfolders of a given folder.
 * 
 * @param foldername the complete path to the folder.
 * 
 * @return subfolders a Vector with all subfolders for the given folder.
 * 
 * @exception CmsException if the user has not the rights to access or read the resource.
 */
public Vector getSubFolders(String foldername) throws CmsException {
	return (m_rb.getSubFolders(m_context.currentUser(), m_context.currentProject(), foldername));
}
/**
  * Get a parameter value for a task.
  * 
  * @param taskid the id of the task.
  * @param parname the name of the parameter.
  * @return the parameter value.
  *
  * @exception CmsException if operation was not successful. 
  */
public String getTaskPar(int taskid, String parname) throws CmsException {
	return (m_rb.getTaskPar(m_context.currentUser(), m_context.currentProject(), taskid, parname));
}
/**
 * Get the template task id fo a given taskname.
 * 
 * @param taskname the name of the task.
 * 
 * @return the id of the task template.
 * 
 * @exception CmsException if operation was not successful.
 */
public int getTaskType(String taskname) throws CmsException {
	return m_rb.getTaskType(taskname);
}
/**
 * Returns all users in the Cms.
 *  
 * @return a Vector of all users in the Cms.
 * 
 * @exception CmsException if operation was not successful.
 */
public Vector getUsers() throws CmsException {
	return (m_rb.getUsers(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Returns all users of the given type in the Cms.
 * 
 * @param type the type of the users.
 * 
 * @return vector of all users of the given type in the Cms.
 * 
 * @exception CmsException if operation was not successful.
 */
public Vector getUsers(int type) throws CmsException {
	return (m_rb.getUsers(m_context.currentUser(), m_context.currentProject(), type));
}
/**
 * Gets all users of a group.
 * 
 * @param groupname the name of the group to get all users for.
 * @return all users in the group.
 * 
 * @exception CmsException if operation was not successful.
 */
public Vector getUsersOfGroup(String groupname) throws CmsException {
	return (m_rb.getUsersOfGroup(m_context.currentUser(), m_context.currentProject(), groupname));
}
/**
 * Imports a import-resource (folder or zipfile) to the cms.
 * 
 * @param importFile the name (absolute Path) of the import resource (zipfile or folder).
 * @param importPath the name (absolute Path) of the folder in which should be imported.
 * 
 * @exception CmsException if operation was not successful.
 */
public void importFolder(String importFile, String importPath) throws CmsException {
	// import the resources
	clearcache();
	m_rb.importFolder(m_context.currentUser(), m_context.currentProject(), importFile, importPath, this);
	clearcache();
}
/**
 * Imports a import-resource (folder or zip-file) to the cms.
 * 
 * @param importFile the name (absolute Path) of the import resource (zipfile or folder).
 * @param importPath the name (absolute Path) of folder in which should be imported.
 * 
 * @exception CmsException if operation was not successful.
 */
public void importResources(String importFile, String importPath) throws CmsException {
	// import the resources
	clearcache();
	m_rb.importResources(m_context.currentUser(), m_context.currentProject(), importFile, importPath, this);
	clearcache();
}
/**
 * Initializes the CmsObject without a request-context (current-user, 
 * current-group, current-project).
 * 
 * @param broker the resourcebroker to access the database.
 * @exception CmsException if operation was not successful.
 */
public void init(I_CmsResourceBroker broker) throws CmsException {
	m_rb = broker;
}
/**
 * Initializes the CmsObject for each request.
 * 
 * @param broker the resourcebroker to access the database.
 * @param req the CmsRequest.
 * @param resp the CmsResponse.
 * @param user the current user for this request.
 * @param currentGroup the current group for this request.
 * @param currentProjectId the current projectId for this request.
 *
 * @exception CmsException if operation was not successful.
 */
public void init(I_CmsResourceBroker broker, I_CmsRequest req, I_CmsResponse resp, String user, String currentGroup, int currentProjectId) throws CmsException {
	m_rb = broker;
	m_context = new CmsRequestContext();
	m_context.init(m_rb, req, resp, user, currentGroup, currentProjectId);
}
/**
 * Checks, if the users current group is the admin-group.
 * 
 * 
 * @return <code>true</code>, if the users current group is the admin-group; <code>false</code> otherwise.
 * @exception CmsException if operation was not successful.
 */
public boolean isAdmin() throws CmsException {
	return m_rb.isAdmin(getRequestContext().currentUser(), getRequestContext().currentProject());
}
/**
 * Checks is the site name, url or combination of language, category and country already exists
 * Creation date: (04-10-2000 12:03:31)
 * @return boolean
 * @param siteId int
 * @param name java.lang.String
 * @param url java.lang.String
 * @param categoryId int
 * @param languageId int
 * @param countryId int
 * @exception com.opencms.core.CmsException The exception description.
 */
public boolean isSiteLegal(int siteId, String name, String url, int categoryId, int languageId, int countryId) throws com.opencms.core.CmsException
{
	return m_rb.isSiteLegal(m_context.currentUser(), m_context.currentProject(), siteId, name, url, categoryId, languageId, countryId);
}
/**
 * Returns the user, who has locked a given resource.
 * <br>
 * A user can lock a resource, so he is the only one who can write this 
 * resource. This methods checks, who has locked a resource.
 * 
 * @param resource the resource to check.
 * 
 * @return the user who has locked the resource.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsUser lockedBy(CmsResource resource) throws CmsException {
	return (m_rb.lockedBy(m_context.currentUser(), m_context.currentProject(), resource));
}
/**
 	* Returns the user, who has locked a given resource.
 	* <br>
 	* A user can lock a resource, so he is the only one who can write this 
 	* resource. This methods checks, who has locked a resource.
 	* 
 	* @param resource The complete path to the resource.
 	* 
 	* @return the user who has locked a resource.
 	* 
 	* @exception CmsException if operation was not successful.
 	*/
public CmsUser lockedBy(String resource) throws CmsException {
	return (m_rb.lockedBy(m_context.currentUser(), m_context.currentProject(), resource));
}
/**
 * Locks the given resource.
 * <br>
 * A user can lock a resource, so he is the only one who can write this 
 * resource.
 * 
 * @param resource The complete path to the resource to lock.
 * 
 * @exception CmsException if the user has not the rights to lock this resource. 
 * It will also be thrown, if there is an existing lock.
 *
 */
public void lockResource(String resource) throws CmsException {
	// try to lock the resource, prevent from overwriting an existing lock
	lockResource(resource, false);
}
/**
 * Locks a given resource.
 * <br>
 * A user can lock a resource, so he is the only one who can write this 
 * resource.
 * 
 * @param resource the complete path to the resource to lock.
 * @param force if force is <code>true</code>, a existing locking will be overwritten.
 * 
 * @exception CmsException if the user has not the rights to lock this resource.
 * It will also be thrown, if there is a existing lock and force was set to false.
 */
public void lockResource(String resource, boolean force) throws CmsException {
	m_rb.lockResource(m_context.currentUser(), m_context.currentProject(), resource, force);
}
/**
 * Logs a user into the Cms, if the password is correct.
 * 
 * @param username the name of the user.
 * @param password the password of the user.
 * @return the name of the logged in user.
 * 
 * @exception CmsException if operation was not successful
 */
public String loginUser(String username, String password) throws CmsException {
	// login the user
	CmsUser newUser = m_rb.loginUser(m_context.currentUser(), m_context.currentProject(), username, password);
	// init the new user
	init(m_rb, m_context.getRequest(), m_context.getResponse(), newUser.getName(), newUser.getDefaultGroup().getName(), C_PROJECT_ONLINE_ID);
	// return the user-name
	return (newUser.getName());
}
/**
 * Logs a web user into the Cms, if the password is correct.
 * 
 * @param username the name of the user.
 * @param password the password of the user.
 * @return the name of the logged in user.
 * 
 * @exception CmsException if operation was not successful
 */
public String loginWebUser(String username, String password) throws CmsException {
	// login the user
	CmsUser newUser = m_rb.loginWebUser(m_context.currentUser(), m_context.currentProject(), username, password);
	// init the new user
	init(m_rb, m_context.getRequest(), m_context.getResponse(), newUser.getName(), newUser.getDefaultGroup().getName(), C_PROJECT_ONLINE_ID);
	// return the user-name
	return (newUser.getName());
}
/**
 * Moves a file to the given destination.
 * 
 * @param source the complete path of the sourcefile.
 * @param destination the complete path of the destinationfile.
 * 
 * @exception CmsException if the user has not the rights to move this resource,
 * or if the file couldn't be moved.
 */
public void moveFile(String source, String destination) throws CmsException {
	m_rb.moveFile(m_context.currentUser(), m_context.currentProject(), source, destination);
}
/**
 * Moves the folder to the given destination.
 * 
 * @param source the complete path of the sourcefile.
 * @param destination the complete path of the destinationfile.
 * 
 * @exception CmsException if the user has not the rights to move this resource,
 * or if the file couldn't be moved. 
 */
public void moveFolder(String source, String destination) throws CmsException {
	m_rb.moveFolder(m_context.currentUser(), m_context.currentProject(), source, destination);
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
public CmsSite newSite(String Name, String Description, int Category, int Language, int Country, String url, String user, String group) throws CmsException {
	return m_rb.newSite(Name, Description, Category, Language, Country, url, user, group, m_context.currentUser(), m_context.currentProject());
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
 * @param parentId
 * @author Martin Langelund
 */
public CmsSite newSite(String Name, String Description, int Category, int Language, int Country, String url, String user, String group, int parentId) throws CmsException
{
	return m_rb.newSite(Name, Description, Category, Language, Country, url, user, group, m_context.currentUser(), m_context.currentProject(), parentId);
}
/**
 * Returns the online project.
 * <p>
 * This is the default project. All anonymous 
 * (or guest) user will see the resources of this project.
 * 
 * @return the online project object.
 * @exception CmsException if operation was not successful.
 */
public CmsProject onlineProject() throws CmsException {
	return (m_rb.onlineProject(m_context.currentUser(), m_context.currentProject()));
}
/**
 * Publishes a project.
 * 
 * @param id the id of the project to be published.
 * @return a Vector of resources, that have been changed.
 * 
 * @exception CmsException if operation was not successful.
 */
public void publishProject(int id) throws CmsException {
	clearcache();
	m_rb.publishProject(m_context.currentUser(), m_context.currentProject(), id);
	clearcache();
}
/**
 * Reads the agent of a task from the OpenCms.
 * 
 * @param task the task to read the agent from.
 * @return the owner of a task.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsUser readAgent(CmsTask task) throws CmsException {
	return (m_rb.readAgent(m_context.currentUser(), m_context.currentProject(), task));
}
/**
 * Reads all file headers of a file in the OpenCms.
 * <br>
 * This method returns a vector with the history of all file headers, i.e. 
 * the file headers of a file, independent of the project they were attached to.<br>
 * 
 * The reading excludes the filecontent.
 * 
 * @param filename the name of the file to be read.
 * 
 * @return a Vector of file headers read from the Cms.
 * 
 * @exception CmsException  if operation was not successful.
 */
public Vector readAllFileHeaders(String filename) throws CmsException {
	return (m_rb.readAllFileHeaders(m_context.currentUser(), m_context.currentProject(), filename));
}
/**
 * Returns a list of all properties of a file or folder.
 * 
 * @param name the name of the resource for which the property has to be read.
 * 
 * @return a Vector of properties as Strings.
 * 
 * @exception CmsException if operation was not succesful.
 */
public Hashtable readAllProperties(String name) throws CmsException {
	return (m_rb.readAllProperties(m_context.currentUser(), m_context.currentProject(), name));
}
/**
 * Reads all property-definitions for the given resource type.
 * 
 * @param id the id of the resource type to read the property-definitions for.
 * @param type the type of the property-definition (normal|mandatory|optional).
 * 
 * @return a Vector with property-defenitions for the resource type.
 * The Vector may be empty.
 * 
 * @exception CmsException if operation was not successful.
 */
public Vector readAllPropertydefinitions(int id, int type) throws CmsException {
	return (m_rb.readAllPropertydefinitions(m_context.currentUser(), m_context.currentProject(), id, type));
}
/**
 * Reads all property-definitions for the given resource type.
 * 
 * @param resourcetype the name of the resource type to read the 
 * property-definitions for.
 * 
 * @return a Vector with property-defenitions for the resource type.
 * The Vector may be empty.
 * 
 * @exception CmsException if operation was not successful.
 */
public Vector readAllPropertydefinitions(String resourcetype) throws CmsException {
	return (m_rb.readAllPropertydefinitions(m_context.currentUser(), m_context.currentProject(), resourcetype));
}
/**
 * Reads all property-definitions for the given resource type.
 * 
 * @param resourcetype The name of the resource type to read the 
 * property-definitions for.
 * @param type the type of the property-definition (normal|mandatory|optional).
 * 
 * @return a Vector with property-defenitions for the resource type.
 * The Vector may be empty.
 * 
 * @exception CmsException if operation was not successful.
 */
public Vector readAllPropertydefinitions(String resourcetype, int type) throws CmsException {
	return (m_rb.readAllPropertydefinitions(m_context.currentUser(), m_context.currentProject(), resourcetype, type));
}
/**
 * Reads the export-path of the system.
 * This path is used for db-export and db-import.
 * 
 * @return the exportpath.
 * @exception CmsException if operation was not successful.
 */
public String readExportPath() throws CmsException {
	return m_rb.readExportPath(m_context.currentUser(), m_context.currentProject());
}
/**
 * Reads a file from the Cms.
 * 
 * @param filename the complete path to the file.
 * 
 * @return file the read file.
 * 
 * @exception CmsException if the user has not the rights to read this resource,
 * or if the file couldn't be read. 
 */
public CmsFile readFile(String filename) throws CmsException {
	return (m_rb.readFile(m_context.currentUser(), m_context.currentProject(), filename));
}
/**
 * Reads a file from the Cms.
 * 
 * @param folder the complete path to the folder from which the file will be read.
 * @param filename the name of the file to be read.
 * 
 * @return file the read file.
 * 
 * @exception CmsException , if the user has not the rights 
 * to read this resource, or if the file couldn't be read. 
 */
public CmsFile readFile(String folder, String filename) throws CmsException {
	return (m_rb.readFile(m_context.currentUser(), m_context.currentProject(), folder + filename));
}
/**
 * Gets the known file extensions (=suffixes). 
 * 
 * 
 * @return a Hashtable with all known file extensions as Strings.
 *
 * @exception CmsException if operation was not successful.
 */
public Hashtable readFileExtensions() throws CmsException {
	return m_rb.readFileExtensions(null, null);
}
/**
 * Reads a file header from the Cms.
 * <br>
 * The reading excludes the filecontent.
 * 
 * @param filename the complete path of the file to be read.
 * 
 * @return file the read file.
 * 
 * @exception CmsException , if the user has not the rights 
 * to read the file headers, or if the file headers couldn't be read. 
 */
public CmsResource readFileHeader(String filename) throws CmsException {
	return (m_rb.readFileHeader(m_context.currentUser(), m_context.currentProject(), filename));
}
/**
 * Reads a file header from the Cms.
 * <br>
 * The reading excludes the filecontent.
 * 
 * @param folder the complete path to the folder from which the file will be read.
 * @param filename the name of the file to be read.
 * 
 * @return file the read file.
 * 
 * @exception CmsException if the user has not the rights 
 * to read the file header, or if the file header couldn't be read. 
 */
public CmsResource readFileHeader(String folder, String filename) throws CmsException {
	return (m_rb.readFileHeader(m_context.currentUser(), m_context.currentProject(), folder + filename));
}
/**
 * Reads all file headers of a project from the Cms.
 * 
 * @param projectId the id of the project to read the file headers for.
 * 
 * @return a Vector of resources.
 * 
 * @exception CmsException if the user has not the rights 
 * to read the file headers, or if the file headers couldn't be read.
 */
public Vector readFileHeaders(int projectId) throws CmsException {
	return (m_rb.readFileHeaders(m_context.currentUser(), m_context.currentProject(), projectId));
}
/**
 * Reads a folder from the Cms.
 * 
 * @param folder the complete path to the folder to be read.
 * 
 * @return folder the read folder.
 * 
 * @exception CmsException if the user has not the rights 
 * to read this resource, or if the folder couldn't be read.  
 */
public CmsFolder readFolder(String folder) throws CmsException {
	return (readFolder(folder, ""));
}
/**
 * Reads a folder from the Cms.
 * 
 * @param folder the complete path to the folder from which the folder will be 
 * read.
 * @param foldername the name of the folder to be read.
 * 
 * @return folder the read folder.
 * 
 * @exception CmsException if the user has not the rights 
 * to read this resource, or if the folder couldn't be read.  
 */
public CmsFolder readFolder(String folder, String folderName) throws CmsException {
	return (m_rb.readFolder(m_context.currentUser(), m_context.currentProject(), folder, folderName));
}
/**
  * Reads all given tasks from a user for a project.
  * 
  * @param projectId the id of the project in which the tasks are defined.
  * @param owner the owner of the task.
  * @param tasktype the type of task you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
  * @param orderBy specifies how to order the tasks.
  * 
  * @exception CmsException if operation was not successful.
  */
public Vector readGivenTasks(int projectId, String ownerName, int taskType, String orderBy, String sort) throws CmsException {
	return (m_rb.readGivenTasks(m_context.currentUser(), m_context.currentProject(), projectId, ownerName, taskType, orderBy, sort));
}
/**
 * Reads the group of a project from the OpenCms.
 * 
 * @return the group of the given project.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsGroup readGroup(CmsProject project) throws CmsException {
	return (m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), project));
}
/**
 * Reads the group of a resource from the Cms.
 * 
 * @return the group of a resource.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsGroup readGroup(CmsResource resource) throws CmsException {
	return (m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), resource));
}
/**
 * Reads the group (role) of a task from the Cms.
 * 
 * @param task the task to read the role from.
 * @return the group of the task.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsGroup readGroup(CmsTask task) throws CmsException {
	return (m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), task));
}
/**
 * Reads a group of the Cms.
 * 
 * @param groupname the name of the group to be returned.
 * @return a group in the Cms.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsGroup readGroup(String groupname) throws CmsException {
	return (m_rb.readGroup(m_context.currentUser(), m_context.currentProject(), groupname));
}
/**
 * Reads the managergroup of a project from the Cms.
 * 
 * @return the managergroup of a project.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsGroup readManagerGroup(CmsProject project) throws CmsException {
	return (m_rb.readManagerGroup(m_context.currentUser(), m_context.currentProject(), project));
}
/**
 * Gets all Mime-Types known by the system. 
 * 
 * @return  a Hashtable containing all mime-types.
 *
 * @exception CmsException if operation was not successful.
 */
public Hashtable readMimeTypes() throws CmsException {
	return m_rb.readMimeTypes(null, null);
}
/**
 * Reads the original agent of a task from the Cms.
 * 
 * @param task the task to read the original agent from.
 * @return the owner of a task.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsUser readOriginalAgent(CmsTask task) throws CmsException {
	return (m_rb.readOriginalAgent(m_context.currentUser(), m_context.currentProject(), task));
}
/**
 * Reads the owner of a project from the Cms.
 * 
 * @return the owner of the given project.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsUser readOwner(CmsProject project) throws CmsException {
	return (m_rb.readOwner(m_context.currentUser(), m_context.currentProject(), project));
}
/**
 * Reads the owner of a resource from the Cms.
 * 
 * @return the owner of a resource.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsUser readOwner(CmsResource resource) throws CmsException {
	return (m_rb.readOwner(m_context.currentUser(), m_context.currentProject(), resource));
}
/**
 * Reads the owner (initiator) of a task from the Cms.
 * 
 * @param tasktThe task to read the owner from.
 * @return the owner of a task.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsUser readOwner(CmsTask task) throws CmsException {
	return (m_rb.readOwner(m_context.currentUser(), m_context.currentProject(), task));
}
/**
 * Reads the owner of a tasklog from the Cms.
 * 
 * @return the owner of a resource.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsUser readOwner(CmsTaskLog log) throws CmsException {
	return (m_rb.readOwner(m_context.currentUser(), m_context.currentProject(), log));
}
/**
 * Reads a project from the Cms.
 * 
 * @param task the task for which the project will be read.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsProject readProject(int id) throws CmsException {
	return (m_rb.readProject(m_context.currentUser(), m_context.currentProject(), id));
}
/**
 * Reads a project from the Cms.
 * 
 * @param id the id of the project to read.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsProject readProject(CmsResource res) throws CmsException {
	return (m_rb.readProject(m_context.currentUser(), m_context.currentProject(), res));
}
/**
 * Reads a project from the Cms.
 * 
 * @param name the resource for which the project will be read.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsProject readProject(CmsTask task) throws CmsException {
	return (m_rb.readProject(m_context.currentUser(), m_context.currentProject(), task));
}
/**
  * Reads log entries for a project.
  * 
  * @param projectId the id of the project for which the tasklog will be read.
  * @return a Vector of new TaskLog objects 
  * @exception CmsException if operation was not successful.
  */
public Vector readProjectLogs(int projectId) throws CmsException {
	return m_rb.readProjectLogs(m_context.currentUser(), m_context.currentProject(), projectId);
}
/**
 * Returns a Property of a file or folder.
 * 
 * @param name the resource-name for which the property will be read.
 * @param property the property-definition name of the property that will be read.
 * 
 * @return property the Property as string.
 * 
 * @exception CmsException if operation was not successful
 */
public String readProperty(String name, String property) throws CmsException {
	return (m_rb.readProperty(m_context.currentUser(), m_context.currentProject(), name, property));
}
/**
 * Reads the property-definition for the resource type.
 * 
 * @param name the name of the property-definition to read.
 * @param resourcetype the name of the resource type for the property-definition.
 * @return the property-definition.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsPropertydefinition readPropertydefinition(String name, String resourcetype) throws CmsException {
	return (m_rb.readPropertydefinition(m_context.currentUser(), m_context.currentProject(), name, resourcetype));
}
/**
 * Reads the task with the given id.
 * 
 * @param id the id of the task to be read.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsTask readTask(int id) throws CmsException {
	return (m_rb.readTask(m_context.currentUser(), m_context.currentProject(), id));
}
/**
 * Reads log entries for a task.
 * 
 * @param taskid the task for which the tasklog will be read.
 * @return a Vector of new TaskLog objects. 
 * @exception CmsException if operation was not successful.
 */
public Vector readTaskLogs(int taskid) throws CmsException {
	return m_rb.readTaskLogs(m_context.currentUser(), m_context.currentProject(), taskid);
}
/**
 * Reads all tasks for a project.
 * 
 * @param projectId the id of the project in which the tasks are defined. Can be null to select all tasks.
 * @tasktype the type of task you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
 * @param orderBy specifies how to order the tasks. 
 * @param sort sort order: C_SORT_ASC, C_SORT_DESC, or null.
 * 
 * @exception CmsException if operation was not successful.
 */
public Vector readTasksForProject(int projectId, int tasktype, String orderBy, String sort) throws CmsException {
	return (m_rb.readTasksForProject(m_context.currentUser(), m_context.currentProject(), projectId, tasktype, orderBy, sort));
}
/**
 * Reads all tasks for a role in a project.
 * 
 * @param projectId the id of the Project in which the tasks are defined.
 * @param user the user who has to process the task.
 * @param tasktype the type of task you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
 * @param orderBy specifies how to order the tasks.
 * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
 * @exception CmsException if operation was not successful.
 */
public Vector readTasksForRole(int projectId, String roleName, int tasktype, String orderBy, String sort) throws CmsException {
	return (m_rb.readTasksForRole(m_context.currentUser(), m_context.currentProject(), projectId, roleName, tasktype, orderBy, sort));
}
/**
 * Reads all tasks for a user in a project.
 * 
 * @param projectId the id of the Project in which the tasks are defined.
 * @param role the user who has to process the task.
 * @param tasktype the type of task you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
 * @param orderBy specifies how to order the tasks.
 * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
 * @exception CmsException if operation was not successful.
 */
public Vector readTasksForUser(int projectId, String userName, int tasktype, String orderBy, String sort) throws CmsException {
	return (m_rb.readTasksForUser(m_context.currentUser(), m_context.currentProject(), projectId, userName, tasktype, orderBy, sort));
}
/**
 * Returns a user in the Cms.
 * 
 * @param id the id of the user to be returned.
 * @return a user in the Cms.
 * 
 * @exception CmsException if operation was not successful
 */
public CmsUser readUser(int id) throws CmsException {
	return (m_rb.readUser(m_context.currentUser(), m_context.currentProject(), id));
}
/**
 * Returns a user in the Cms.
 * 
 * @param username the name of the user to be returned.
 * @return a user in the Cms.
 * 
 * @exception CmsException if operation was not successful
 */
public CmsUser readUser(String username) throws CmsException {
	return m_rb.readUser(m_context.currentUser(), m_context.currentProject(), username);
}
/**
 * Returns a user in the Cms.
 * 
 * @param username the name of the user to be returned.
 * @param type the type of the user.
 * @return a user in the Cms.
 * 
 * @exception CmsException if operation was not successful
 */
public CmsUser readUser(String username, int type) throws CmsException {
	return (m_rb.readUser(m_context.currentUser(), m_context.currentProject(), username, type));
}
/**
 * Returns a user in the Cms, if the password is correct.
 * 
 * @param username the name of the user to be returned.
 * @param password the password of the user to be returned.
 * @return a user in the Cms.
 * 
 * @exception CmsException if operation was not successful
 */
public CmsUser readUser(String username, String password) throws CmsException {
	return (m_rb.readUser(m_context.currentUser(), m_context.currentProject(), username, password));
}
/**
 * Reactivates a task from the Cms.
 * 
 * @param taskid the Id of the task to accept.
 * 
 * @exception CmsException if operation was not successful.
 */
public void reaktivateTask(int taskId) throws CmsException {
	m_rb.reaktivateTask(m_context.currentUser(), m_context.currentProject(), taskId);
}
/** 
 * Sets a new password if the user knows his recovery-password.
 * 
 * @param username the name of the user.
 * @param recoveryPassword the recovery password.
 * @param newPassword the new password.
 * 
 * @exception CmsException if operation was not successfull.
 */
public void recoverPassword(String username, String recoveryPassword, String newPassword) throws CmsException {
	m_rb.recoverPassword(m_context.currentUser(), m_context.currentProject(), username, recoveryPassword, newPassword);
}
/**
 * Removes a user from a group.
 *
 * <p>
 * <b>Security:</b> 
 * Only the admin user is allowed to remove a user from a group.
 * 
 * @param username the name of the user that is to be removed from the group.
 * @param groupname the name of the group.
 * @exception CmsException if operation was not successful.
 */
public void removeUserFromGroup(String username, String groupname) throws CmsException {
	m_rb.removeUserFromGroup(m_context.currentUser(), m_context.currentProject(), username, groupname);
}
/**
 * Renames the file to the new name.
 * 
 * @param oldname the complete path to the file which will be renamed.
 * @param newname the new name of the file.
 * 
 * @exception CmsException if the user has not the rights 
 * to rename the file, or if the file couldn't be renamed.  
 */
public void renameFile(String oldname, String newname) throws CmsException {
	m_rb.renameFile(m_context.currentUser(), m_context.currentProject(), oldname, newname);
}
/**
 * Returns the root-folder object.
 * 
 * @return the root-folder object.
 * @exception CmsException if operation was not successful.
 */
public CmsFolder rootFolder() throws CmsException {
	return (readFolder(C_ROOT));
}
/**
 * Set a new name for a task.
 * 
 * @param taskid the id of the task.
 * @param name the new name of the task.
 * 
 * @exception CmsException if operationwas not successful.
 */
public void setName(int taskId, String name) throws CmsException {
	m_rb.setName(m_context.currentUser(), m_context.currentProject(), taskId, name);
}
/**
 * Sets a new parent-group for an already existing group in the Cms.
 * 
 * @param groupName the name of the group that should be written to the Cms.
 * @param parentGroupName the name of the parentGroup to set, or null if the parent 
 * group should be deleted.
 * @exception CmsException  if operation was not successfull.
 */
public void setParentGroup(String groupName, String parentGroupName) throws CmsException {
	m_rb.setParentGroup(m_context.currentUser(), m_context.currentProject(), groupName, parentGroupName);
}
/** 
 * Sets the password for a user.
 * 
 * @param username the name of the user.
 * @param newPassword the new password.
 * 
 * @exception CmsException if operation was not successful.
 */
public void setPassword(String username, String newPassword) throws CmsException {
	m_rb.setPassword(m_context.currentUser(), m_context.currentProject(), username, newPassword);
}
/** 
 * Sets the password for a user.
 * 
 * @param username the name of the user.
 * @param oldPassword the old password.
 * @param newPassword the new password.
 * 
 * @exception CmsException if operation was not successful.
 */
public void setPassword(String username, String oldPassword, String newPassword) throws CmsException {
	m_rb.setPassword(m_context.currentUser(), m_context.currentProject(), username, oldPassword, newPassword);
}
/**
 * Sets the priority of a task.
 * 
 * @param taskid the id of the task.
 * @param priority the new priority value.
 * 
 * @exception CmsException if operation was not successful.
 */
public void setPriority(int taskId, int priority) throws CmsException {
	m_rb.setPriority(m_context.currentUser(), m_context.currentProject(), taskId, priority);
}
/** 
 * Sets the recovery password for a user.
 * 
 * @param username the name of the user.
 * @param password the password.
 * @param newPassword the new recovery password.
 * 
 * @exception CmsException if operation was not successful.
 */
public void setRecoveryPassword(String username, String oldPassword, String newPassword) throws CmsException {
	m_rb.setRecoveryPassword(m_context.currentUser(), m_context.currentProject(), username, oldPassword, newPassword);
}
/**
 * Set a parameter for a task.
 * 
 * @param taskid the Id of the task.
 * @param parname the ame of the parameter.
 * @param parvalue the value of the parameter.
 * 
 * @return the id of the inserted parameter or 0 if the parameter already exists for this task.
 * 
 * @exception CmsException if operation was not successful.
 */
public void setTaskPar(int taskid, String parname, String parvalue) throws CmsException {
	m_rb.setTaskPar(m_context.currentUser(), m_context.currentProject(), taskid, parname, parvalue);
}
/**
 * Sets the timeout of a task.
 * 
 * @param taskid the id of the task.
 * @param timeout the new timeout value.
 * 
 * @exception CmsException if operation was not successful.
 */
public void setTimeout(int taskId, long timeout) throws CmsException {
	m_rb.setTimeout(m_context.currentUser(), m_context.currentProject(), taskId, timeout);
}
/**
 * Unlocks all resources of a project.
 * 
 * @param id the id of the project to be unlocked.
 * 
 * @exception CmsException if operation was not successful.
 */
public void unlockProject(int id) throws CmsException {
	m_rb.unlockProject(m_context.currentUser(), m_context.currentProject(), id);
}
/**
 * Unlocks a resource.
 * <br>
 * A user can unlock a resource, so other users may lock this file.
 * 
 * @param resource the complete path to the resource to be unlocked.
 * 
 * @exception CmsException if the user has not the rights 
 * to unlock this resource.
 */
public void unlockResource(String resource) throws CmsException {
	m_rb.unlockResource(m_context.currentUser(), m_context.currentProject(), resource);
}
/**
 * Updates a site
 *
 * Only a adminstrator can do this.<P/>
 * 
 * <B>Security:</B>
 * Only users, which are in the group "administrators" are granted.
 * @param siteId int
 * @param name java.lang.String
 * @param description java.lang.String
 * @param categoryId int
 * @param languageId int
 * @param countryId int
 * @param url java.lang.String
 * @exception com.opencms.core.CmsException The exception description.
 */
public void updateSite(int siteId, String name, String description, int categoryId, int languageId, int countryId, String url) throws com.opencms.core.CmsException
{
	m_rb.updateSite(m_context.currentUser(), m_context.currentProject(), siteId, name, description, categoryId, languageId, countryId, url);
}
/**
 * Tests, if a user is member of the given group.
 * 
 * @param username the name of the user to test.
 * @param groupname the name of the group to test.
 * @return <code>true</code>, if the user is in the group; <code>else</code> false otherwise.
 * 
 * @exception CmsException if operation was not successful.
 */
public boolean userInGroup(String username, String groupname) throws CmsException {
	return (m_rb.userInGroup(m_context.currentUser(), m_context.currentProject(), username, groupname));
}
/**
 * Returns a String containing version information for this OpenCms.
 * 
 * @return version a String containnig the version of OpenCms.
 */
public String version() {
	return (C_VERSION);
}
/**
 * Writes the export-path for the system.
 * <br>
 * This path is used for db-export and db-import.
 * 
 * @param mountpoint the mount point in the Cms filesystem.
 *
 * @exception CmsException if operation ws not successful.
 */
public void writeExportPath(String path) throws CmsException {
	m_rb.writeExportPath(m_context.currentUser(), m_context.currentProject(), path);
}
/**
 * Writes a file to the Cms.
 * 
 * @param file the file to write.
 * 
 * @exception CmsException if mandatory property-definitions for this resource are missing,
 * or if resourcetype is set to folder. The CmsException will also be thrown, 
 * if the user has not the rights write the file.
 */
public void writeFile(CmsFile file) throws CmsException {
	m_rb.writeFile(m_context.currentUser(), m_context.currentProject(), file);
}
/**
 * Writes the file extensions. 
 * <p>
 * <B>Security:</B>
 * Only the admin user is allowed to write file extensions.
 *  
 * @param extensions holds extensions as keys and resourcetypes (Strings) as values.
 */

public void writeFileExtensions(Hashtable extensions) throws CmsException {
	m_rb.writeFileExtensions(m_context.currentUser(), m_context.currentProject(), extensions);
}
/**
 * Writes a file-header to the Cms.
 * 
 * @param file the file to write.
 * 
 * @exception CmsException if mandatory property-definitions are missing,
 * or if resourcetype is set to folder. The CmsException will also be thrown, 
 * if the user has not the rights to write the file header..
 */
public void writeFileHeader(CmsFile file) throws CmsException {
	m_rb.writeFileHeader(m_context.currentUser(), m_context.currentProject(), file);
}
/**
 * Writes an already existing group to the Cms.
 * 
 * @param group the group that should be written to the Cms.
 * @exception CmsException  if operation was not successful.
 */
public void writeGroup(CmsGroup group) throws CmsException {
	m_rb.writeGroup(m_context.currentUser(), m_context.currentProject(), group);
}
/**
 * Writes a couple of Properties for a file or folder.
 * 
 * @param name the resource-name of which the Property has to be set.
 * @param properties a Hashtable with property-definitions and property values as Strings.
 * 
 * @exception CmsException if operation was not successful.
 */
public void writeProperties(String name, Hashtable properties) throws CmsException {
	m_rb.writeProperties(m_context.currentUser(), m_context.currentProject(), name, properties);
}
/**
 * Writes a property for a file or folder.
 * 
 * @param name the resource-name for which the property will be set.
 * @param property the property-definition name.
 * @param value the value for the property to be set.
 * 
 * @exception CmsException if operation was not successful.
 */
public void writeProperty(String name, String property, String value) throws CmsException {
	m_rb.writeProperty(m_context.currentUser(), m_context.currentProject(), name, property, value);
}
/**
 * Writes the property-definition for the resource type.
 * 
 * @param propertydef the property-definition to be written.
 * 
 * @exception CmsException if operation was not successful.
 */
public CmsPropertydefinition writePropertydefinition(CmsPropertydefinition definition) throws CmsException {
	return (m_rb.writePropertydefinition(m_context.currentUser(), m_context.currentProject(), definition));
}
/**
 * Writes a new user tasklog for a task.
 * 
 * @param taskid the Id of the task.
 * @param comment the description for the log.
 * 
 * @exception CmsException if operation was not successful.
 */
public void writeTaskLog(int taskid, String comment) throws CmsException {
	m_rb.writeTaskLog(m_context.currentUser(), m_context.currentProject(), taskid, comment);
}
/**
 * Writes a new user tasklog for a task.
 * 
 * @param taskid the Id of the task .
 * @param comment the description for the log
 * @param tasktype the type of the tasklog. User tasktypes must be greater than 100.
 * 
 * @exception CmsException if operation was not successful.
 */
public void writeTaskLog(int taskid, String comment, int taskType) throws CmsException {
	m_rb.writeTaskLog(m_context.currentUser(), m_context.currentProject(), taskid, comment, taskType);
}
/**
 * Updates the user information.
 * <p>
 * <b>Security:</b>
 * Only the admin user is allowed to update the user information.
 * 
 * @param user the user to be written.
 * 
 * @exception CmsException if operation was not successful.
 */
public void writeUser(CmsUser user) throws CmsException {
	m_rb.writeUser(m_context.currentUser(), m_context.currentProject(), user);
}
/**
 * Updates the user information of a web user.
 * <br>
 * Only a web user can be updated this way.
 * 
 * @param user the user to be written.
 * 
 * @exception CmsException if operation was not successful.
 */
public void writeWebUser(CmsUser user) throws CmsException {
	m_rb.writeWebUser(m_context.currentUser(), m_context.currentProject(), user);
}
}

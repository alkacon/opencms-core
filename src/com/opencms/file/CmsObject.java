/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsObject.java,v $
 * Date   : $Date: 2000/04/05 14:43:38 $
 * Version: $Revision: 1.62 $
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

package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

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
 * @version $Revision: 1.62 $ $Date: 2000/04/05 14:43:38 $ 
 * 
 */
public class CmsObject extends A_CmsObject implements I_CmsConstants {
	
	/**
	 * The resource broker to access the cms.
	 */
	private static I_CmsResourceBroker c_rb = null;
	
	/**
	 * The resource broker to access the cms.
	 */
	private A_CmsRequestContext m_context = null;

	/**
	 * Initialises the CmsObject with the resourceBroker. This only done ones!
	 * If the ressource broker was set before - it will not be overitten. This is
	 * for security reasons.
	 * 
	 * @param ressourceBroker the resourcebroker to access all resources.
	 * 
	 * @exception CmsException is thrown, when the resourceBroker was set before.
	 */
	public void init(I_CmsResourceBroker resourceBroker) {
		c_rb = resourceBroker;
	}
	
	/**
	 * Initialises the CmsObject for each request.
	 * 
	 * @param req the CmsRequest.
	 * @param resp the CmsResponse.
	 * @param user The current user for this request.
	 * @param currentGroup The current group for this request.
	 * @param currentProjectId The current projectId for this request.
	 */
	public void init(I_CmsRequest req, I_CmsResponse resp, 
					 String user, String currentGroup, int currentProjectId ) 
		throws CmsException {
		m_context = new CmsRequestContext();
		m_context.init(c_rb, req, resp, user, currentGroup, currentProjectId);
	}
	
	/**
	 * Returns the current request-context.
	 * 
	 * @return the current request-context.
	 */
	public A_CmsRequestContext getRequestContext() {
		return( m_context );
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
	 * Returns the anonymous user object.
	 * 
	 * @return the anonymous user object.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsUser anonymousUser() 
		throws CmsException {
		return( c_rb.anonymousUser(m_context.currentUser(), 
								   m_context.currentProject()) );
	}
	
	/**
	 * Returns the onlineproject. This is the default project. All anonymous 
	 * (or guest) user will see the rersources of this project.
	 * 
	 * @return the onlineproject object.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsProject onlineProject() 
		throws CmsException {
		return( c_rb.onlineProject(m_context.currentUser(), 
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
		return( c_rb.getAllResourceTypes(m_context.currentUser(), 
										 m_context.currentProject()) );
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
	public A_CmsResourceType getResourceType(String resourceType) 
		throws CmsException {
		return( c_rb.getResourceType(m_context.currentUser(), 
									 m_context.currentProject(), resourceType) );
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
	public A_CmsResourceType getResourceType(int resourceType) 
		throws CmsException {
		return( c_rb.getResourceType(m_context.currentUser(), 
									 m_context.currentProject(), resourceType) );
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
	public A_CmsResourceType addResourceType(String resourceType, int launcherType, 
											 String launcherClass) 
		throws CmsException {
		return( c_rb.addResourceType(m_context.currentUser(), 
									 m_context.currentProject(), resourceType,
									 launcherType, launcherClass) );
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
		return( c_rb.accessProject(m_context.currentUser(), 
								   m_context.currentProject(), projectId) );
	}

	/**
	 * Reads a project from the Cms.
	 * 
	 * @param id The id of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsProject readProject(int id)
		throws CmsException { 
		return( c_rb.readProject(m_context.currentUser(), 
								 m_context.currentProject(), id) );
	}
    
     /**
	 * Reads a project from the Cms.
	 * 
	 * @param name The resource of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsProject readProject(A_CmsResource res)
		throws CmsException { 
		return( c_rb.readProject(m_context.currentUser(), 
								 m_context.currentProject(), res) );
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
	 public A_CmsProject createProject(String name, String description, 
									   String groupname, String managergroupname)
		 throws CmsException {
		 return( c_rb.createProject(m_context.currentUser(), 
									m_context.currentProject(), name, description, 
									groupname, managergroupname) );
	 }
	
	/**
	 * Creates a project.
	 * 
	 * @param id The id of the new project, it must be unique.
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param groupname the name of the group to be set.
	 * @param managergroupname the name of the managergroup to be set.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsProject createProject(int id, String name, String description, 
									   String groupname, String managergroupname)
		 throws CmsException {
		 return( c_rb.createProject(m_context.currentUser(), 
									m_context.currentProject(), id, name, description, 
									groupname, managergroupname) );
	 }
	 
	/**
	 * Publishes a project.
	 * 
	 * @param id The id of the project to be published.
	 * @return A Vector of resources, that were changed.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector publishProject(int id)
		throws CmsException { 
		 return( c_rb.publishProject(m_context.currentUser(), 
									 m_context.currentProject(), id) );
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
		c_rb.deleteProject(m_context.currentUser(), m_context.currentProject(), id);
	}
	
	/**
	 * Returns all projects, which the user may access.
	 * 
	 * @return a Vector of projects.
	 */
	public Vector getAllAccessibleProjects() 
		throws CmsException {
		return( c_rb.getAllAccessibleProjects(m_context.currentUser(), 
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
		 return( c_rb.getAllManageableProjects(m_context.currentUser(), 
											   m_context.currentProject()) );

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
		return( c_rb.createFile(m_context.currentUser(), 
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
		return( c_rb.createFile(m_context.currentUser(), 
								m_context.currentProject(), 
								folder, filename, contents, type, 
								properties ) );
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
		return( c_rb.readFile(m_context.currentUser(), 
							  m_context.currentProject(), 
							  folder + filename ) );
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
		return( c_rb.readFile(m_context.currentUser(), 
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
	public A_CmsResource readFileHeader(String folder, String filename)
		throws CmsException { 
		return( c_rb.readFileHeader(m_context.currentUser(), 
									m_context.currentProject(), 
									folder + filename ) );
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
	public A_CmsResource readFileHeader(String filename)
		throws CmsException { 
		return( c_rb.readFileHeader(m_context.currentUser(), 
									m_context.currentProject(), 
									filename ) );
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
		c_rb.writeFile(m_context.currentUser(), m_context.currentProject(), file);
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
		c_rb.writeFileHeader(m_context.currentUser(), m_context.currentProject(), file);
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
		c_rb.renameFile(m_context.currentUser(), m_context.currentProject(), 
						oldname, newname);
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
		c_rb.deleteFile(m_context.currentUser(), m_context.currentProject(), 
						filename);
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
		c_rb.copyFile(m_context.currentUser(), m_context.currentProject(), 
					  source, destination);
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
		c_rb.moveFile(m_context.currentUser(), m_context.currentProject(), 
					  source, destination );
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
		c_rb.copyResourceToProject(m_context.currentUser(), 
								   m_context.currentProject(), resource );
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
		return( c_rb.createFolder(m_context.currentUser(), 
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
		return( c_rb.createFolder(m_context.currentUser(), 
								  m_context.currentProject(), folder, 
								  newFolderName, properties ) );
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
		return( c_rb.readFolder(m_context.currentUser(), 
								m_context.currentProject(), folder, folderName ) );
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
		c_rb.deleteFolder(m_context.currentUser(), m_context.currentProject(), 
						  foldername );
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
		return( c_rb.getSubFolders(m_context.currentUser(), 
								   m_context.currentProject(), foldername ) );
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
		return( c_rb.getFilesInFolder(m_context.currentUser(), m_context.currentProject(),
									  foldername) );
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
		 return( c_rb.readAllFileHeaders(m_context.currentUser(), 
										 m_context.currentProject(),
										 filename) );
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
		c_rb.chmod(m_context.currentUser(), m_context.currentProject(), 
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
		c_rb.chown(m_context.currentUser(), m_context.currentProject(), 
				   filename, newOwner );
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
		c_rb.chgrp(m_context.currentUser(), m_context.currentProject(), 
				   filename, newGroup );
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
		c_rb.chtype(m_context.currentUser(), m_context.currentProject(), 
				   filename, newType );
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
		c_rb.lockResource(m_context.currentUser(), 
					  m_context.currentProject(), resource, force );
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
		c_rb.unlockResource(m_context.currentUser(), 
					  m_context.currentProject(), resource);
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
	public A_CmsUser lockedBy(String resource)
		throws CmsException {
		return( c_rb.lockedBy(m_context.currentUser(), m_context.currentProject(), 
							  resource) );
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
	public A_CmsUser lockedBy(A_CmsResource resource)
		throws CmsException {
		return( c_rb.lockedBy(m_context.currentUser(), m_context.currentProject(), 
							  resource) );
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
		return( c_rb.readProperty(m_context.currentUser(), 
										 m_context.currentProject(), 
										 name, property) );
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
		c_rb.writeProperty(m_context.currentUser(),m_context.currentProject(), 
								  name, property, value);
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
		c_rb.writeProperties(m_context.currentUser(),m_context.currentProject(), 
								  name, properties);
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
		return( c_rb.readAllProperties(m_context.currentUser(), 
											 m_context.currentProject(), 
											 name) );
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
		c_rb.deleteAllProperties(m_context.currentUser(), 
									   m_context.currentProject(), 
									   resourcename);
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
		c_rb.deleteProperty(m_context.currentUser(), 
								   m_context.currentProject(), 
								   resourcename, property);
	}

	/**
	 * Reads the owner of a tasklog from the OpenCms.
	 * 
	 * @return The owner of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsUser readOwner(A_CmsTaskLog log) 
		throws CmsException {
		return( c_rb.readOwner(m_context.currentUser(), m_context.currentProject(), 
							   log ) );
	}
	
	/**
	 * Reads the owner of a resource from the OpenCms.
	 * 
	 * @return The owner of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsUser readOwner(A_CmsResource resource) 
		throws CmsException {
		return( c_rb.readOwner(m_context.currentUser(), m_context.currentProject(), 
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
	public A_CmsUser readOwner(A_CmsTask task) 
		throws CmsException {
		return( c_rb.readOwner(m_context.currentUser(), m_context.currentProject(), 
							   task ) );
	}
	
	/**
	 * Reads the agent of a task from the OpenCms.
	 * 
	 * @param task The task to read the agent from.
	 * @return The owner of a task.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsUser readAgent(A_CmsTask task) 
		throws CmsException {
		return( c_rb.readAgent(m_context.currentUser(), m_context.currentProject(), 
							   task ) );
	}

	/**
	 * Reads the original agent of a task from the OpenCms.
	 * 
	 * @param task The task to read the original agent from.
	 * @return The owner of a task.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsUser readOriginalAgent(A_CmsTask task) 
		throws CmsException {
		return( c_rb.readOriginalAgent(m_context.currentUser(), 
									   m_context.currentProject(), 
									   task ) );
	}
	
	/**
	 * Reads the group of a resource from the OpenCms.
	 * 
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsGroup readGroup(A_CmsResource resource) 
		throws CmsException {
		return( c_rb.readGroup(m_context.currentUser(), m_context.currentProject(), 
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
	public A_CmsGroup readGroup(A_CmsTask task) 
		throws CmsException {
		return( c_rb.readGroup(m_context.currentUser(), m_context.currentProject(), 
							   task ) );
	}
	
	/**
	 * Reads the owner of a project from the OpenCms.
	 * 
	 * @return The owner of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsUser readOwner(A_CmsProject project) 
		throws CmsException{
		return( c_rb.readOwner(m_context.currentUser(), m_context.currentProject(), 
							   project ) );
	}
	
	/**
	 * Reads the group of a project from the OpenCms.
	 * 
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsGroup readGroup(A_CmsProject project) 
		throws CmsException{
		return( c_rb.readGroup(m_context.currentUser(), m_context.currentProject(), 
							   project ) );
	}
	
	/**
	 * Reads the managergroup of a project from the OpenCms.
	 * 
	 * @return The group of a resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsGroup readManagerGroup(A_CmsProject project) 
		throws CmsException{
		return( c_rb.readManagerGroup(m_context.currentUser(), 
									  m_context.currentProject(), 
									  project ) );
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
		return( c_rb.getUsers(m_context.currentUser(), m_context.currentProject()) );
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
		return( c_rb.getGroups(m_context.currentUser(), m_context.currentProject()) );
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
		return( c_rb.getChild(m_context.currentUser(), m_context.currentProject(), 
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
		return( c_rb.getChilds(m_context.currentUser(), m_context.currentProject(), 
							   groupname ) );
	}
	
    /**
	 * Returns the parent group of a group<P/>
	 * 
	 * @param groupname The name of the group.
	 * @return group The parent group or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public A_CmsGroup getParent(String groupname)
		throws CmsException {
		return( c_rb.getParent(m_context.currentUser(), m_context.currentProject(), 
							   groupname ) );
	}
							
	/**
	 * Returns a user in the Cms.
	 * 
	 * @param username The name of the user to be returned.
	 * @return a user in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public A_CmsUser readUser(String username) 
		throws CmsException { 
		return( c_rb.readUser(m_context.currentUser(), 
							  m_context.currentProject(), 
							  username) );
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
	public A_CmsUser readUser(String username, String password) 
		throws CmsException { 
        return( c_rb.readUser(m_context.currentUser(), m_context.currentProject(),
							  username, password) );
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
		A_CmsUser newUser = c_rb.loginUser(m_context.currentUser(), 
										   m_context.currentProject(),
										   username, password);
		// init the new user
		init(m_context.getRequest(), m_context.getResponse(), newUser.getName(), 
			 newUser.getDefaultGroup().getName(), C_PROJECT_ONLINE_ID);
		// return the user-name
		return(newUser.getName());
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
		c_rb.setPassword(m_context.currentUser(), m_context.currentProject(), 
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
		c_rb.setPassword(m_context.currentUser(), m_context.currentProject(), 
						 username, newPassword );
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
	public A_CmsUser addUser(String name, String password, String group, 
							 String description, Hashtable additionalInfos, int flags)
		throws CmsException { 
		
		return( c_rb.addUser(m_context.currentUser(), m_context.currentProject(),
							  name, password, group, description, additionalInfos, 
							  flags) );

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
		c_rb.deleteUser(m_context.currentUser(), m_context.currentProject(), username);
	}
	
	/**
	 * Updated the userinformation.<BR/>
	 * 
	 * Only the administrator can do this.
	 * 
	 * @param user The user to be written.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeUser(A_CmsUser user)
		throws CmsException { 
		c_rb.writeUser(m_context.currentUser(), m_context.currentProject(), user );
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
		return( c_rb.getUsersOfGroup(m_context.currentUser(), 
									 m_context.currentProject(), groupname ));
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
		return( c_rb.getGroupsOfUser(m_context.currentUser(), 
									 m_context.currentProject(), username ));
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
		return( c_rb.userInGroup(m_context.currentUser(), m_context.currentProject(), 
								 username, groupname));
	}

	/**
	 * Returns a group in the Cms.
	 * 
	 * @param groupname The name of the group to be returned.
	 * @return a group in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public A_CmsGroup readGroup(String groupname) 
		throws CmsException { 
		return( c_rb.readGroup(m_context.currentUser(), m_context.currentProject(), 
							   groupname));
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
	public A_CmsGroup addGroup(String name, String description, int flags, String parent)
		throws CmsException { 
		return( c_rb.addGroup(m_context.currentUser(), m_context.currentProject(),
							  name, description, flags, parent) );

	}
	
    /**
	 * Writes an already existing group in the Cms.<BR/>
	 * 
	 * @param group The group that should be written to the Cms.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	public void writeGroup(A_CmsGroup group)
		throws CmsException {
		c_rb.writeGroup(m_context.currentUser(), m_context.currentProject(),
						group);
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
		c_rb.setParentGroup(m_context.currentUser(), m_context.currentProject(), 
							groupName, parentGroupName);
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
		c_rb.deleteGroup(m_context.currentUser(), m_context.currentProject(), 
						 delgroup);
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
		c_rb.addUserToGroup(m_context.currentUser(), m_context.currentProject(), 
							username, groupname );
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
		c_rb.removeUserFromGroup(m_context.currentUser(), m_context.currentProject(), 
								 username, groupname );
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
		return( c_rb.readAllPropertydefinitions(m_context.currentUser(), 
											m_context.currentProject(), 
											resourcetype ) );
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
		return( c_rb.readAllPropertydefinitions(m_context.currentUser(), 
											m_context.currentProject(), 
											resourcetype, type ) );
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
	public A_CmsPropertydefinition createPropertydefinition(String name, String resourcetype, 
													int type)
		throws CmsException {
		return( c_rb.createPropertydefinition(m_context.currentUser(), 
										  m_context.currentProject(), 
										  name,
										  resourcetype,
										  type) );
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
	public A_CmsPropertydefinition readPropertydefinition(String name, 
												  String resourcetype)
		throws CmsException { 
		return( c_rb.readPropertydefinition(m_context.currentUser(), 
										m_context.currentProject(), 
										name,
										resourcetype) );
	}

	/**
	 * Writes the Propertydefinition for the resource type.<BR/>
	 * 
	 * @param propertydef The Propertydef to be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsPropertydefinition writePropertydefinition(A_CmsPropertydefinition definition)
		throws  CmsException { 
		return( c_rb.writePropertydefinition(m_context.currentUser(), 
										 m_context.currentProject(), 
										 definition) );
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
		c_rb.deletePropertydefinition(m_context.currentUser(), 
								  m_context.currentProject(), 
								  name, resourcetype);
	}

	/**
	 * Writes a shedule-task to the Cms.<BR/>
	 * The user of the task will be set to the current user.
	 * 
	 * @param scheduleTask the task that should be written to the Cms.
	 * 
	 * @exception CmsException if something goes wrong.
	 */
	public void writeScheduleTask(A_CmsScheduleTask scheduleTask)
		throws CmsException { 
		return ; // TODO: implement this ScheduleTask operation! 
	}

	/**
	 * Deltes a shedule-task from the Cms.<BR/>
	 * 
	 * A task can only be deleted by the owner or a administrator.
	 * 
	 * @param scheduleTask the task that should be deleted.
	 * 
	 * @exception CmsException if something goes wrong.
	 */
	public void deleteScheduleTask(A_CmsScheduleTask scheduleTask)
		throws CmsException { 
		return ; // TODO: implement this ScheduleTask operation!
	}
	
	/**
	 * Reads all shedule-task from the Cms.
	 * 
	 * @return scheduleTasks A Vector with all schedule-Tasks of the Cms.
	 * 
	 * @exception CmsException if something goes wrong.
	 */
	public Vector readAllScheduleTasks()
		throws CmsException { 
		return null;  // TODO: implement this ScheduleTask operation!
	}
	
	/**
	 * Gets the MimeTypes. 
	 * The Mime-Types will be returned.
	 * 
	 * <B>Security:</B>
	 * All users are garnted<BR/>
	 * 
	 * @return the mime-types.
	 */
	public Hashtable readMimeTypes()
		throws CmsException {
		return c_rb.readMimeTypes(null, null);
	}
	
	/**
	 * Writes the export-path for the system.
	 * This path is used for db-export and db-import.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 */
	public void writeExportPath(String path)
		throws CmsException {
		c_rb.writeExportPath(m_context.currentUser(), m_context.currentProject(), path);
	}
	
	/**
	 * Reads the export-path for the system.
	 * This path is used for db-export and db-import.
	 * 
	 * @return the exportpath.
	 */
	public String readExportPath()
		throws CmsException {
		return c_rb.readExportPath(m_context.currentUser(), m_context.currentProject());
	}
	
    /**
	 * Adds a new CmsMountPoint. 
	 * A new mountpoint for a mysql filesystem is added.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 * @param driver The driver for the db-system. 
	 * @param connect The connectstring to access the db-system.
	 * @param name A name to describe the mountpoint.
	 */
	public void addMountPoint(String mountpoint, String driver, String connect,
							  String name)
		throws CmsException {
		c_rb.addMountPoint(m_context.currentUser(), m_context.currentProject(),
						   mountpoint, driver, connect, name);
	}

    /**
	 * Adds a new CmsMountPoint. 
	 * A new mountpoint for a disc filesystem is added.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 * @param mountpath The physical location this mount point directs to. 
	 * @param name The name of this mountpoint.
	 * @param user The default user for this mountpoint.
	 * @param group The default group for this mountpoint.
	 * @param type The default resourcetype for this mountpoint.
	 * @param accessFLags The access-flags for this mountpoint.
	 */
	synchronized public void addMountPoint(String mountpoint, String mountpath, 
										   String name, String user, String group,
										   String type, int accessFlags)
		throws CmsException {
		c_rb.addMountPoint(m_context.currentUser(), m_context.currentProject(),
						   mountpoint, mountpath, name, user, group, type, accessFlags);
	}
	
	/**
	 * Gets a CmsMountPoint. 
	 * A mountpoint will be returned.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 * 
	 * @return the mountpoint - or null if it doesen't exists.
	 */
	public A_CmsMountPoint readMountPoint(String mountpoint )
		throws CmsException {
		return( c_rb.readMountPoint(m_context.currentUser(), 
									m_context.currentProject(),
									mountpoint) );
	}
	
    /**
	 * Deletes a CmsMountPoint. 
	 * A mountpoint will be deleted.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 */
	public void deleteMountPoint(String mountpoint )
		throws CmsException {
		c_rb.deleteMountPoint(m_context.currentUser(), m_context.currentProject(),
							  mountpoint);
	}

	/**
	 * Gets all CmsMountPoints. 
	 * All mountpoints will be returned.
	 * 
	 * @return the mountpoints - or null if they doesen't exists.
	 */
	public Hashtable getAllMountPoints()
		throws CmsException {
		return( c_rb.getAllMountPoints(m_context.currentUser(), 
									   m_context.currentProject()) );
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
	 * Returns a copyright-string for this OpenCms.
	 * 
	 * @return copyright A copyright-string.
	 */
	 public String[] copyright() {
		 return C_COPYRIGHT;
	 }
	
	/**
	 * This method can be called, to determine if the file-system was changed 
	 * in the past. A module can compare its previosly stored number with this
	 * returned number. If they differ, a change was made.
	 * 
	 * @return the number of file-system-changes.
	 */
	 public long getFileSystemChanges() {
		return( c_rb.getFileSystemChanges(m_context.currentUser(), 
										  m_context.currentProject()) );
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
	 public A_CmsTask createProject(String projectname, int projectType,
									String roleName, long timeout, 
									int priority)
		 throws CmsException {
						 
		 return c_rb.createProject(m_context.currentUser(), projectname, projectType, 
									   roleName, timeout, priority);
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
	 
	 public A_CmsTask createTask(String agentName, String roleName, 
								 String taskname, String taskcomment, 
								 long timeout, int priority)
		 throws CmsException {
		 return( c_rb.createTask(m_context.currentUser(), m_context.currentProject(),
								 agentName, roleName, taskname, taskcomment, 
								 timeout, priority) );
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
	 public A_CmsTask createTask(int projectid, String agentName, String roleName, 
								 String taskname, String taskcomment, int tasktype,
								 long timeout, int priority)
		 throws CmsException {
	 
		 return c_rb.createTask(m_context.currentUser(), projectid, agentName, roleName,
									taskname, taskcomment, tasktype, timeout, priority);
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
		 c_rb.setTaskPar(m_context.currentUser(), m_context.currentProject(),
						 taskid, parname, parvalue);
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
		 return( c_rb.getTaskPar(m_context.currentUser(), m_context.currentProject(),
								 taskid, parname) );
	 }
	 
	 // database import, export stuff
	
	/**
	 * exports database (files, groups, users) into a specified file
	 * 
	 * @param exportFile the name (absolute Path) for the XML file
	 * @param exportPath the name (absolute Path) for the folder to export
	 * @param exportType what to export:
	 *			C_EXPORTUSERSFILES exports all
	 *			C_EXPORTONLYUSERS  exports only users and groups
	 *			C_EXPORTONLYFILES  exports only files
	 * 
	 */
	public void exportDb(String exportFile, String exportPath, int exportType) 
		throws CmsException {
		try {
			c_rb.exportDb(m_context.currentUser(), m_context.currentProject(), exportFile, exportPath, exportType);
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
	}
	
	/**
	 * imports a (files, groups, users) XML file into database
	 * 
	 * @param importPath the name (absolute Path) of folder in which should be imported
	 * @param importFile the name (absolute Path) of the XML import file
	 * 
	 */
	public void importDb(String importFile, String importPath)
		throws CmsException {
		try {
			c_rb.importDb(m_context.currentUser(), m_context.currentProject(), importFile, importPath);
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}			
	}

	/**
	 * Checks, if the user may read this resource.
	 * 
	 * @param resource The resource to check.
	 * @return wether the user has access, or not.
	 */
    public boolean accessRead(A_CmsResource resource) throws CmsException {
    	try {
			return c_rb.accessRead(m_context.currentUser(), m_context.currentProject(), resource);
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}			
    }

	/**
	 * Checks, if the user may create this resource.
	 * 
	 * @param resource The resource to check.
	 * @return wether the user has access, or not.
	 */
    public boolean accessCreate(A_CmsResource resource) throws CmsException {
    	try {
			return c_rb.accessCreate(m_context.currentUser(), m_context.currentProject(), resource);
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
    public boolean accessWrite(A_CmsResource resource) throws CmsException {
    	try {
			return c_rb.accessWrite(m_context.currentUser(), m_context.currentProject(), resource);
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
    public boolean accessLock(A_CmsResource resource) throws CmsException {
    	try {
			return c_rb.accessLock(m_context.currentUser(), m_context.currentProject(), resource);
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}			
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
		 return( c_rb.readTasksForUser(m_context.currentUser(), m_context.currentProject(), 
									   projectId, userName, tasktype, orderBy, sort) );
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
		 return(c_rb.readTasksForProject(m_context.currentUser(), 
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
		 return( c_rb.readTasksForRole(m_context.currentUser(), m_context.currentProject(), 
									   projectId, roleName, tasktype, orderBy, sort) );
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
		 return( c_rb.readGivenTasks(m_context.currentUser(), m_context.currentProject(), 
									 projectId, ownerName, taskType, orderBy, sort) );
	 }

	 /**
	  * Read a task by id.
	  * 
	  * @param id The id for the task to read.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public A_CmsTask readTask(int id)
		 throws CmsException {
		 return( c_rb.readTask(m_context.currentUser(), m_context.currentProject(), 
							   id) );
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
			 return  c_rb.getTaskType(taskname);		
	}


	 /**
	  * Accept a task from the Cms.
	  * 
	  * @param taskid The Id of the task to accept.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void acceptTask(int taskId)
		 throws CmsException {
		 c_rb.acceptTask(m_context.currentUser(), m_context.currentProject(), taskId);
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
		 c_rb.forwardTask(m_context.currentUser(), m_context.currentProject(), taskid,
						  newRoleName, newUserName);
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
		 c_rb.endTask(m_context.currentUser(), m_context.currentProject(), taskid);
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
		 c_rb.writeTaskLog(m_context.currentUser(), m_context.currentProject(), 
						   taskid, comment);
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
		 c_rb.writeTaskLog(m_context.currentUser(), m_context.currentProject(), taskid, 
						   comment, taskType);
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
		 return c_rb.readTaskLogs(m_context.currentUser(), m_context.currentProject(), taskid);
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
		 return c_rb.readProjectLogs(m_context.currentUser(), m_context.currentProject(), projectId);
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
		 c_rb.setTimeout(m_context.currentUser(), m_context.currentProject(), 
						 taskId, timeout);
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
		 c_rb.setPriority(m_context.currentUser(), m_context.currentProject(), 
						  taskId, priority);
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
		 c_rb.reaktivateTask(m_context.currentUser(), m_context.currentProject(), taskId);
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
		 c_rb.setName(m_context.currentUser(), m_context.currentProject(), taskId, name);
	 }
}

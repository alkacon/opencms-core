package com.opencms.file;

import java.util.*;
import javax.servlet.http.*;

import com.opencms.core.*;

/**
 * The class which implements this abstract class gains access to the OpenCms. 
 * <p>
 * The CmsObject encapsulates user identifaction and client request and is
 * the central object to transport information in the Cms Servlet.
 * <p>
 * All operations on the CmsObject are forwarded to the class which extends
 * A_CmsRessourceBroker to ensures user authentification in all operations.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.30 $ $Date: 2000/01/21 16:30:34 $ 
 */
public abstract class A_CmsObject {	

	/**
	 * Initialises the CmsObject with the resourceBroker. This only done ones!
	 * If the ressource broker was set before - it will not be overitten. This is
	 * for security reasons.
	 * 
	 * @param ressourceBroker the resourcebroker to access all resources.
	 * 
	 * @exception CmsException is thrown, when the resourceBroker was set before.
	 */
	abstract public void init(I_CmsResourceBroker resourceBroker);
	
	/**
	 * Initialises the CmsObject for each request.
	 * 
	 * @param req the CmsRequest.
	 * @param resp the CmsResponse.
	 * @param user The current user for this request.
	 * @param currentGroup The current group for this request.
	 * @param currentProject The current project for this request.
	 */
	abstract public void init(I_CmsRequest req, I_CmsResponse resp, 
							  String user, String currentGroup, String currentProject )
		throws CmsException;
	
	/**
	 * Returns the current request-context.
	 * 
	 * @return the current request-context.
	 */
	abstract public A_CmsRequestContext getRequestContext();

	/**
	 * Returns the root-folder object.
	 * 
	 * @return the root-folder object.
	 */
	abstract public CmsFolder rootFolder()
		throws CmsException;

	
	/**
	 * Returns the anonymous user object.
	 * 
	 * @return the anonymous user object.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract public A_CmsUser anonymousUser() 
		throws CmsException;
	
	/**
	 * Returns the onlineproject. This is the default project. All anonymous 
	 * (or guest) user will see the rersources of this project.
	 * 
	 * @return the onlineproject object.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract public A_CmsProject onlineProject() 
		throws CmsException;

	/**
	 * Returns a Vector with all I_CmsResourceTypes.
	 * 
	 * Returns a Vector with all I_CmsResourceTypes.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract public Hashtable getAllResourceTypes() 
		throws CmsException;
	
	/**
	 * Returns a CmsResourceTypes.
	 * 
	 * @param resourceType the name of the resource to get.
	 * 
	 * @return a CmsResourceTypes.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	abstract public A_CmsResourceType getResourceType(String resourceType) 
		throws CmsException ;
	
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
	abstract public A_CmsResourceType addResourceType(String resourceType, 
													  int launcherType, 
													  String launcherClass) 
		throws CmsException;
	
	/**
	 * Tests if the user can access the project.
	 * 
	 * @param projectname the name of the project.
	 * 
	 * @return true, if the user has access, else returns false.
	 */
	abstract public boolean accessProject(String projectname) 
		throws CmsException;

	/**
	 * Reads a project from the Cms.
	 * 
	 * @param name The name of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract public A_CmsProject readProject(String name)
		throws CmsException;
	
	/**
	 * Creates a project.
	 * 
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param groupname the name of the group to be set.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 abstract public A_CmsProject createProject(String name, String description, 
												String groupname)
		 throws CmsException;
	
	/**
	 * Publishes a project.
	 * 
	 * @param name The name of the project to be published.
	 * @return A Vector of resources, that were changed.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract public Vector publishProject(String name)
		throws CmsException;
	
	/**
	 * Returns all projects, which the user may access.
	 * 
	 * @return a Vector of projects.
	 */
	abstract public Vector getAllAccessibleProjects()
		throws CmsException;
	
	/**
	 * Creates a new file with the overgiven content and resourcetype.
	 * If there are some mandatory Metadefinitions for the resourcetype, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory Metainformations.<BR/>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * If there is already a file with this filename, a CmsDuplicateKey exception will
	 * be thrown.
	 * 
	 * @param folder The complete path to the folder in which the file will be created.
	 * @param filename The name of the new file (No pathinformation allowed).
	 * @param contents The contents of the new file.
	 * @param type The resourcetype of the new file.
	 * 
	 * @return file The created file.
	 * 
	 * @exception CmsException will be thrown for missing metainfos or if 
	 * resourcetype is set to folder. The CmsException is also thrown, if the 
	 * filename is not valid. The CmsException will also be thrown, if the user
	 * has not the rights for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * this name.
	 */
	abstract public CmsFile createFile(String folder, String filename, 
									   byte[] contents, String type)
		throws CmsException;
	
	/**
	 * Creates a new file with the overgiven content and resourcetype.
	 * If some mandatory Metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory Metainformations.<BR/>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * If there is already a file with this filename, a CmsDuplicateKey exception will
	 * be thrown.
	 * 
	 * @param folder The complete path to the folder in which the file will be created.
	 * @param filename The name of the new file (No pathinformation allowed).
	 * @param contents The contents of the new file.
	 * @param type The resourcetype of the new file.
	 * @param metainfos A Hashtable of metainfos, that should be set for this file.
	 * The keys for this Hashtable are the names for Metadefinitions, the values are
	 * the values for the metainfos.
	 * 
	 * @return file The created file.
	 * 
	 * @exception CmsException will be thrown for missing metainfos, for worng metadefs
	 * or if resourcetype is set to folder. The CmsException is also thrown, if the 
	 * filename is not valid. The CmsException will also be thrown, if the user
	 * has not the rights for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * this name.
	 */
	abstract public CmsFile createFile(String folder, String filename, byte[] contents, 
									   String type, Hashtable metainfos)
		throws CmsException;
	
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
	abstract public CmsFile readFile(String folder, String filename)
		throws CmsException;
	
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
	abstract public CmsFile readFile(String filename)
		throws CmsException;
	
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
	abstract public A_CmsResource readFileHeader(String folder, String filename)
		throws CmsException;
	
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
	abstract public A_CmsResource readFileHeader(String filename)
		throws CmsException;
	
	/**
	 * Writes a file to the Cms.<BR/>
	 * If some mandatory Metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be written without
	 * the mandatory Metainformations.<BR/>
	 * 
	 * @param file The file to write.
	 * 
	 * @exception CmsException will be thrown for missing metainfos, for worng metadefs
	 * or if resourcetype is set to folder. The CmsException will also be thrown, 
	 * if the user has not the rights for this resource.
	 */	
	abstract public void writeFile(CmsFile file) 
		throws CmsException;
	
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
	abstract public void renameFile(String oldname, String newname)
		throws CmsException;
	
	/**
	 * Deletes the file.
	 * 
	 * @param filename The complete path of the file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be deleted. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	abstract public void deleteFile(String filename)
		throws CmsException;
	
	/**
	 * Copies the file.
	 * 
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be copied. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * the destination filename.
	 */	
	abstract public void copyFile(String source, String destination)
		throws CmsException;
	
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
	abstract public void moveFile(String source, String destination)
		throws CmsException;

    /**
     * Copies a resource from the online project to a new, specified project.<br>
     * Copying a resource will copy the file header or folder into the specified 
     * offline project and set its state to UNCHANGED.
     * 
	 * @param resource The name of the resource.
 	 * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    abstract public void copyResourceToProject(String resource)
        throws CmsException;
	
	/**
	 * Creates a new folder.
	 * If there are some mandatory Metadefinitions for the folder-resourcetype, a 
	 * CmsException will be thrown, because the folder cannot be created without
	 * the mandatory Metainformations.<BR/>
	 * If there is already a folder with this filename, a CmsDuplicateKey exception 
	 * will be thrown.
	 * 
	 * @param folder The complete path to the folder in which the new folder 
	 * will be created.
	 * @param newFolderName The name of the new folder (No pathinformation allowed).
	 * 
	 * @return folder The created folder.
	 * 
	 * @exception CmsException will be thrown for missing metainfos.
	 * The CmsException is also thrown, if the foldername is not valid. 
	 * The CmsException will also be thrown, if the user has not the rights for 
	 * this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * this name.
	 */
	abstract public CmsFolder createFolder(String folder, String newFolderName)
		throws CmsException;
	
	/**
	 * Creates a new file with the overgiven content and resourcetype.
	 * If some mandatory Metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory Metainformations.<BR/>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * If there is already a file with this filename, a CmsDuplicateKey exception will
	 * be thrown.
	 * 
	 * @param folder The complete path to the folder in which the new folder will 
	 * be created.
	 * @param newFolderName The name of the new folder (No pathinformation allowed).
	 * @param metainfos A Hashtable of metainfos, that should be set for this folder.
	 * The keys for this Hashtable are the names for Metadefinitions, the values are
	 * the values for the metainfos.
	 * 
	 * @return file The created file.
	 * 
	 * @exception CmsException will be thrown for missing metainfos, for worng metadefs
	 * or if the filename is not valid. The CmsException will also be thrown, if the 
	 * user has not the rights for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * this name.
	 */
	abstract public CmsFolder createFolder(String folder, String newFolderName, 
											 Hashtable metainfos)
		throws CmsException;

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
	abstract public CmsFolder readFolder(String folder, String folderName)
		throws CmsException;
	
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
	abstract public CmsFolder readFolder(String folder)
		throws CmsException;
	
	
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
	abstract public void deleteFolder(String foldername)
		throws CmsException ;
	
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
	abstract public Vector getSubFolders(String foldername)
		throws CmsException;
	
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
	abstract public Vector getFilesInFolder(String foldername)
		throws CmsException;

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
	 abstract public Vector readAllFileHeaders(String filename)
		 throws CmsException;
	 
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
	abstract public void chmod(String filename, int flags)
		throws CmsException;
	
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
	abstract public void chown(String filename, String newOwner)
		throws CmsException;

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
	abstract public void chgrp(String filename, String newGroup)
		throws CmsException;

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
	abstract public void lockResource(String resource)
		throws CmsException;
	
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
	abstract public void lockResource(String resource, boolean force)
		throws CmsException;
	
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
	abstract public void unlockResource(String resource)
		throws CmsException;

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
	abstract public A_CmsUser lockedBy(String resource)
		throws CmsException;

	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The name of the resource type to read the 
	 * metadefinitions for.
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	abstract public Vector readAllMetadefinitions(String resourcetype)
		throws CmsException;
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param resourcetype The name of the resource type to read the 
	 * metadefinitions for.
	 * @param type The type of the metadefinition (normal|mandatory|optional).
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */	
	abstract public Vector readAllMetadefinitions(String resourcetype, int type)
		throws CmsException;
	
	/**
	 * Creates the metadefinition for the resource type.<BR/>
	 * 
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The name of the resource-type for the metadefinition.
	 * @param type The type of the metadefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract public A_CmsMetadefinition createMetadefinition(String name, 
															 String resourcetype, 
															 int type)
		throws CmsException;
	
	/**
	 * Returns a Metainformation of a file or folder.
	 * 
	 * @param name The resource-name of which the Metainformation has to be read.
	 * @param meta The Metadefinition-name of which the Metainformation has to be read.
	 * 
	 * @return metainfo The metainfo as string.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public String readMetainformation(String name, String meta)
		throws CmsException;	

	/**
	 * Writes a Metainformation for a file or folder.
	 * 
	 * @param name The resource-name of which the Metainformation has to be set.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * @param value The value for the metainfo to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public void writeMetainformation(String name, String meta, String value)
		throws CmsException;

	/**
	 * Writes a couple of Metainformation for a file or folder.
	 * 
	 * @param name The resource-name of which the Metainformation has to be set.
	 * @param metainfos A Hashtable with Metadefinition- metainfo-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public void writeMetainformations(String name, Hashtable metainfos)
		throws CmsException;

	/**
	 * Returns a list of all Metainformations of a file or folder.
	 * 
	 * @param name The resource-name of which the Metainformation has to be read
	 * 
	 * @return Hashtable of Metainformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public Hashtable readAllMetainformations(String name)
		throws CmsException;
	
	/**
	 * Deletes all Metainformation for a file or folder.
	 * 
	 * @param resourcename The resource-name of which the Metainformation has to be delteted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public void deleteAllMetainformations(String resourcename)
		throws CmsException;

	/**
	 * Deletes a Metainformation for a file or folder.
	 * 
	 * @param resourcename The resource-name of which the Metainformation has to be delteted.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public void deleteMetainformation(String resourcename, String meta)
		throws CmsException;

	/**
	 * Returns all users in the Cms.
	 *  
	 * @return all users in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public Vector getUsers()
		throws CmsException;

	/**
	 * Returns all groups in the Cms.
	 *  
	 * @return all groups in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public Vector getGroups()
		throws CmsException;

	/**
	 * Returns a user in the Cms.
	 * 
	 * @param username The name of the user to be returned.
	 * @return a user in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public A_CmsUser readUser(String username) 
		throws CmsException;	

	/**
	 * Returns a user in the Cms, if the password is correct.
	 * 
	 * @param username The name of the user to be returned.
	 * @param password The password of the user to be returned.
	 * @return a user in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public A_CmsUser readUser(String username, String password)
		throws CmsException;	

	/**
	 * Logs a user into the Cms, if the password is correct.
	 * 
	 * @param username The name of the user to be returned.
	 * @param password The password of the user to be returned.
	 * @return the name of the logged in user.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public String loginUser(String username, String password) 
		throws CmsException;
	
	/** 
	 * Sets the password for a user.
	 * 
	 * @param username The name of the user.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	abstract public void setPassword(String username, String newPassword)
		throws CmsException;
	
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
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a user with the given username exists already.
	 */
	abstract public A_CmsUser addUser(String name, String password, String group, 
							 String description, Hashtable additionalInfos, int flags)
		throws CmsException;
	
	/** 
	 * Deletes a user from the Cms.
	 * 
	 * Only a adminstrator can do this.
	 * 
	 * @param name The name of the user to be deleted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	abstract public void deleteUser(String username)
		throws CmsException;
	
	/**
	 * Updated the userinformation.<BR/>
	 * 
	 * Only the administrator can do this.
	 * 
	 * @param user The user to be written.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public void writeUser(A_CmsUser user)
		throws CmsException;
	
	/**
	 * Gets all users in the group.
	 * 
	 * @param groupname The name of the group to get all users from.
	 * @return all users in the group.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	abstract public Vector getUsersOfGroup(String groupname)
		throws CmsException;
	
	/**
	 * Gets all groups of a user.
	 * 
	 * @param username The name of the user to get all groups from.
	 * @return all groups of a user.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	abstract public Vector getGroupsOfUser(String username)
		throws CmsException;
	
    /**
	 * Returns all child groups of a group<P/>
	 * 
	 * @param groupname The name of the group.
	 * @return groups A Vector of all child groups or null.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	abstract public Vector getChild(String groupname)
        throws CmsException;

	/**
	 * Tests, if a user is in a group.
	 * 
	 * @param username The name of the user to test.
	 * @param groupname The name of the group to test.
	 * @return true, if the user is in the group else returns false.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	abstract public boolean userInGroup(String username, String groupname)
		throws CmsException;

	/**
	 * Returns a group in the Cms.
	 * 
	 * @param groupname The name of the group to be returned.
	 * @return a group in the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract public A_CmsGroup readGroup(String groupname)
		throws CmsException;

	/**
	 * Add a new group to the Cms.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the new group.
	 * @param description The description for the new group.
	 * @param flags The flags for the new group.
	 * @param parent The parent of this group.
	 *
	 * @return Group
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 * @exception MhtDuplicateKeyException Throws MhtDuplicateKeyException if 
	 * same group already exists.
	 */	
	abstract public A_CmsGroup addGroup(String name, String description, int flags, 
										String parent)
		throws CmsException;

	
    /**
	 * Writes an already existing group in the Cms.<BR/>
	 * 
	 * @param group The group that should be written to the Cms.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	abstract public void writeGroup(A_CmsGroup group)
		throws CmsException;

	/**
	 * Delete a group from the Cms.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param delgroup The name of the group that is to be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	abstract public void deleteGroup(String delgroup)
		throws CmsException;
	
	/**
	 * Adds a user to a group.<BR/>
     *
	 * Only the admin can do this.
	 * 
	 * @param username The name of the user that is to be added to the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */	
	abstract public void addUserToGroup(String username, String groupname)
		throws CmsException;
			   
	/**
	 * Removes a user from a group.
	 * 
	 * Only the admin can do this.
	 * 
	 * @param username The name of the user that is to be removed from the group.
	 * @param groupname The name of the group.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	abstract public void removeUserFromGroup(String username, String groupname)
		throws CmsException;
	
	/**
	 * Reads the Metadefinition for the resource type.<BR/>
	 * 
	 * @param name The name of the Metadefinition to read.
	 * @param resourcetype The name of the resource-type for the Metadefinition.
	 * @return the Metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract public A_CmsMetadefinition readMetadefinition(String name, 
														   String resourcetype)
		throws CmsException;

	/**
	 * Writes the Metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param metadef The metadef to be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract public A_CmsMetadefinition writeMetadefinition(A_CmsMetadefinition definition)
		throws  CmsException;
	
	/**
	 * Delete the Metadefinition for the resource type.<BR/>
	 * 
	 * @param name The name of the Metadefinition to overwrite.
	 * @param resourcetype The name of the resource-type for the Metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract public void deleteMetadefinition(String name, String resourcetype)
		throws CmsException;

	/**
	 * Writes a shedule-task to the Cms.<BR/>
	 * The user of the task will be set to the current user.
	 * 
	 * @param scheduleTask the task that should be written to the Cms.
	 * 
	 * @exception CmsException if something goes wrong.
	 */
	abstract public void writeScheduleTask(A_CmsScheduleTask scheduleTask)
		throws CmsException;

	/**
	 * Deltes a shedule-task from the Cms.<BR/>
	 * 
	 * A task can only be deleted by the owner or a administrator.
	 * 
	 * @param scheduleTask the task that should be deleted.
	 * 
	 * @exception CmsException if something goes wrong.
	 */
	abstract public void deleteScheduleTask(A_CmsScheduleTask scheduleTask)
		throws CmsException;
	
	/**
	 * Reads all shedule-task from the Cms.
	 * 
	 * @return scheduleTasks A Vector with all schedule-Tasks of the Cms.
	 * 
	 * @exception CmsException if something goes wrong.
	 */
	abstract public Vector readAllScheduleTasks()
		throws CmsException;
	
	/**
	 * Gets the MimeTypes. 
	 * The Mime-Types will be returned.
	 * 
	 * @return the mime-types.
	 */
	abstract public Hashtable readMimeTypes()
		throws CmsException;
	
    /**
	 * Adds a new CmsMountPoint. 
	 * A new mountpoint for a mysql filesystem is added.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 * @param driver The driver for the db-system. 
	 * @param connect The connectstring to access the db-system.
	 * @param name A name to describe the mountpoint.
	 */
	abstract public void addMountPoint(String mountpoint, String driver, 
									   String connect, String name)
		throws CmsException;

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
	abstract public void addMountPoint(String mountpoint, String mountpath, 
									   String name, String user, String group,
									   String type, int accessFlags)
		throws CmsException;
	
    /**
	 * Gets a CmsMountPoint. 
	 * A mountpoint will be returned.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 * 
	 * @return the mountpoint - or null if it doesen't exists.
	 */
	abstract public A_CmsMountPoint readMountPoint(String mountpoint )
		throws CmsException;

    /**
	 * Deletes a CmsMountPoint. 
	 * A mountpoint will be deleted.
	 * 
	 * @param mountpoint The mount point in the Cms filesystem.
	 */
	abstract public void deleteMountPoint(String mountpoint )
		throws CmsException;

	/**
	 * Gets all CmsMountPoints. 
	 * All mountpoints will be returned.
	 * 
	 * @return the mountpoints - or null if they doesen't exists.
	 */
	abstract public Hashtable getAllMountPoints()
		throws CmsException;

	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * @param filename The name of the file to be read.
	 * 
	 * @return The file read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	abstract public CmsFile readFile(A_CmsUser currentUser, A_CmsProject currentProject,
							 String filename)
		 throws CmsException;

	/**
	 * Returns a version-string for this OpenCms.
	 * 
	 * @return version A Version-string.
	 */
	abstract public String version();
}

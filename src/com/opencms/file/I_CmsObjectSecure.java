package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * The class which implements this Interface gains access to the OpenCms. 
 * <p>
 * The CmsObject encapsulates user identifaction and client request and is
 *  the central object to transport information in the Cms Servlet.
 * <p>
 * All operations on the CmsObject are forwarded to the class which implements
 * I_CmsRessourceBroker to ensures user authentification in all operations.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/07 17:25:04 $ 
 * 
 */
interface I_CmsObjectSecure {	
	
	/**
	 * Initialises the CmsObject with the resourceBroker. This only done ones!
	 * If the ressource broker was set before - it will not be overitten. This is
	 * for security reasons.
	 * 
	 * @param ressourceBroker the resourcebroker to access all resources.
	 * 
	 * @exception CmsException is thrown, when the resourceBroker was set before.
	 */
	void init(I_CmsResourceBroker resourceBroker) throws CmsException;
	
	/**
	 * Initialises the CmsObject for each request.
	 * 
	 * @param user The current user for this request.
	 * @param host The host of this request.
	 * @param url The url of this request.
	 * @param uri Teh uri of this request.
	 */
	void init(I_CmsUser user, String host, String url, String uri);
	
	/**
	 * Returns the uri for this CmsObject.
	 * 
	 * @return the uri for this CmsObject.
	 */
	public String getUri();
	
	/**
	 * Returns the url for this CmsObject.
	 * 
	 * @return the url for this CmsObject.
	 */
	public String getUrl();
	
	/**
	 * Returns the host for this CmsObject.
	 * 
	 * @return the host for this CmsObject.
	 */
	public String getHost();
	
	/**
	 * Returns the current folder object.
	 * 
	 * @return the current folder object.
	 */
	public I_CmsFolder currentFolder();	

	/**
	 * Returns the root-folder object.
	 * 
	 * @return the root-folder object.
	 */
	public I_CmsFolder rootFolder();
	
	/**
	 * Returns the current user object.
	 * 
	 * @return the current user object.
	 */
	public I_CmsUser currentUser();
	
	/**
	 * Returns the default group of the current user.
	 * 
	 * @return the default group of the current user.
	 */
	public I_CmsGroup userDefaultGroup();
	
	/**
	 * Returns the current group of the current user.
	 * 
	 * @return the current group of the current user.
	 */
	public I_CmsGroup userCurrentGroup();
	
	/**
	 * Returns the anonymous user object.
	 * 
	 * @return the anonymous user object.
	 */
	public I_CmsUser anonymousUser();
	
	/**
	 * Returns the onlineproject. This is the default project. All anonymous 
	 * (or guest) user will see the rersources of this project.
	 * 
	 * @return the onlineproject object.
	 */
	public I_CmsProject onlineProject();

	/**
	 * Returns a Vector with all I_CmsResourceTypes.
	 * 
	 * @return a Vector with all I_CmsResourceTypes.
	 */
	public Vector getAllResourceTypes();
	
	/**
	 * Determines, if the users current group is the admin-group.
	 * 
	 * @return true, if the users current group is the admin-group, 
	 * else it returns false.
	 */	
	public  boolean isAdmin();

	/**
	 * Determines, if the users current group is the projectleader-group.<BR>
	 * All projectleaders can create new projects, or close their own projects.
	 * 
	 * @return true, if the users current group is the projectleader-group, 
	 * else it returns false.
	 */	
	public  boolean isProjectLeader();

	/**
	 * Creates a new file with the overgiven content and resourcetype.
	 * The onlineproject will be used for this resource<BR>
	 * If there are some mandatory metadefinitions for the resourcetype, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory metainformations.<BR>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR>
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
	public I_CmsFile createFile(String folder, String filename, 
								byte[] contents, I_CmsResourceType type)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Creates a new file with the overgiven content and resourcetype.
	 * The onlineproject will be used for this resource<BR>
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory metainformations.<BR>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR>
	 * If there is already a file with this filename, a CmsDuplicateKey exception will
	 * be thrown.
	 * 
	 * @param folder The complete path to the folder in which the file will be created.
	 * @param filename The name of the new file (No pathinformation allowed).
	 * @param contents The contents of the new file.
	 * @param type The resourcetype of the new file.
	 * @param metainfos A Hashtable of metainfos, that should be set for this file.
	 * The keys for this Hashtable are the names for metadefinitions, the values are
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
	public I_CmsFile createFile(String folder, String filename, 
								byte[] contents, I_CmsResourceType type, 
								Hashtable metainfos)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Reads a file from the Cms.<BR>
	 * The onlineproject will be used for this resource<BR>
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
	public I_CmsFile readFile(String folder, String filename)
		throws CmsException;
	
	/**
	 * Reads a file header from the Cms.<BR>
	 * The reading excludes the filecontent. The onlineproject will be used for 
	 * this resource<BR>
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
	public I_CmsResource readFileHeader(String folder, String filename)
		throws CmsException;
	
	/**
	 * Writes a file to the Cms.<BR>
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be written without
	 * the mandatory metainformations.<BR>
	 * The onlineproject will be used for this resource.<BR>
	 * 
	 * @param file The file to write.
	 * 
	 * @exception CmsException will be thrown for missing metainfos, for worng metadefs
	 * or if resourcetype is set to folder. The CmsException will also be thrown, 
	 * if the user has not the rights for this resource.
	 */	
	public void writeFile(I_CmsFile file) 
		throws CmsException;
	
	/**
	 * Writes the fileheader to the Cms.
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be written without
	 * the mandatory metainformations.<BR>
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param resource The resource to write the header of.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be wrote. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void writeFileHeader(I_CmsResource resource)
		throws CmsException;
	
	/**
	 * Writes a file to the Cms.<BR>
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be written without
	 * the mandatory metainformations.<BR>
	 * The onlineproject will be used for this resource.<BR>
	 * 
	 * @param file The file to write.
	 * @param metainfos A Hashtable of metainfos, that should be set for this file.
	 * The keys for this Hashtable are the names for metadefinitions, the values are
	 * the values for the metainfos.
	 * 
	 * @exception CmsException will be thrown for missing metainfos, for worng metadefs
	 * or if resourcetype is set to folder. The CmsException will also be thrown, 
	 * if the user has not the rights for this resource.
	 */	
	public void writeFile(I_CmsFile file, Hashtable metainfos)
		throws CmsException;
	
	/**
	 * Writes the fileheader to the Cms.
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory metainformations.<BR>
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param resource The resource to write the header of.
	 * @param metainfos A Hashtable of metainfos, that should be set for this file.
	 * The keys for this Hashtable are the names for metadefinitions, the values are
	 * the values for the metainfos.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be wrote. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void writeFileHeader(I_CmsResource resource, Hashtable metainfos)
		throws CmsException;

	/**
	 * Renames the file to the new name.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (No path information allowed).
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be renamed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */		
	public void renameFile(String oldname, String newname)
		throws CmsException;
	
	/**
	 * Deletes the file.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param filename The complete path of the file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be deleted. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void deleteFile(String filename)
		throws CmsException;
	
	/**
	 * Copies the file.
	 * The onlineproject will be used for this resource<BR>
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
	public void copyFile(String source, String destination)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Moves the file.
	 * The onlineproject will be used for this resource<BR>
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
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Sets the resource-type of this resource.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param resource The complete path for the resource to be changed.
	 * @param type The new type for the resource.
	 * 
	 * @exception CmsException will be thrown, if the file type couldn't be changed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public void setResourceType(String resource, I_CmsResourceType newType)
		throws CmsException;

	/**
	 * Creates a new folder.
	 * The onlineproject will be used for this resource<BR>
	 * If there are some mandatory metadefinitions for the folder-resourcetype, a 
	 * CmsException will be thrown, because the folder cannot be created without
	 * the mandatory metainformations.<BR>
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
	public I_CmsFolder createFolder(String folder, String newFolderName)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Creates a new file with the overgiven content and resourcetype.
	 * The onlineproject will be used for this resource<BR>
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory metainformations.<BR>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR>
	 * If there is already a file with this filename, a CmsDuplicateKey exception will
	 * be thrown.
	 * 
	 * @param folder The complete path to the folder in which the new folder will 
	 * be created.
	 * @param newFolderName The name of the new folder (No pathinformation allowed).
	 * @param metainfos A Hashtable of metainfos, that should be set for this folder.
	 * The keys for this Hashtable are the names for metadefinitions, the values are
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
	public I_CmsFile createFolder(String folder, String newFolderName, 
								Hashtable metainfos)
		throws CmsException, CmsDuplicateKeyException;

	/**
	 * Reads a folder from the Cms.<BR>
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param folder The complete path to the folder from which the folder will be 
	 * read.
	 * @param foldername The name of the folder to be read.
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public I_CmsFile readFolder(String folder, String folderName)
		throws CmsException;
	
	/**
	 * Renames the folder to the new name.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * renamed, too.
	 * 
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (No path information allowed).
	 * @param force If force is set to true, all sub-resources will be renamed.
	 * If force is set to false, the folder will be renamed only if it is empty.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be renamed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */		
	public void renameFolder(String oldname, String newname, boolean force)
		throws CmsException;
	
	/**
	 * Deletes the folder.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * delted, too.
	 * 
	 * @param foldername The complete path of the folder.
	 * @param force If force is set to true, all sub-resources will be deleted.
	 * If force is set to false, the folder will be deleted only if it is empty.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be deleted. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void deleteFolder(String foldername, boolean force)
		throws CmsException;
	
	/**
	 * Copies a folder.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * copied, too.
	 * 
	 * @param source The complete path of the sourcefolder.
	 * @param destination The complete path of the destinationfolder.
	 * @param force If force is set to true, all sub-resources will be copied.
	 * If force is set to false, the folder will be copied only if it is empty.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be copied. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * the destination foldername.
	 */	
	public void copyFolder(String source, String destination, boolean force)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Moves a folder.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * moved, too.
	 * 
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * @param force If force is set to true, all sub-resources will be moved.
	 * If force is set to false, the folder will be moved only if it is empty.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be moved. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * the destination filename.
	 */	
	public void moveFolder(String source, String destination, boolean force)
		throws CmsException, CmsDuplicateKeyException;

	/**
	 * Returns a Vector with all subfolders.<BR>
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfolders A Vector with all subfolders for the overgiven folder.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public Vector getSubFolders(String foldername)
		throws CmsException;
	
	/**
	 * Returns a Vector with all subfiles.<BR>
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfiles A Vector with all subfiles for the overgiven folder.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public Vector getFilesInFolder(String foldername)
		throws CmsException;
	
	/**
	 * Tests if the user has full access to a resource.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user has full access, else returns false.
	 */
	public boolean accessFile(String filename);

	/**
	 * Tests if the user may read the resource.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user may read, else returns false.
	 */
	public boolean isReadable(String filename);	

	/**
	 * Tests if the user may write the resource.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user may write, else returns false.
	 */
	public boolean isWriteable(String filename);

	/**
	 * Tests if the user may view the resource.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user may view, else returns false.
	 */
	public boolean isViewable(String filename);

	/**
	 * Tests if the resource is an internal resource.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the resource is internal, else returns false.
	 */
	public boolean isInternal(String filename);	

	/**
	 * Tests if the resource exists.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the resource exists, else returns false.
	 */
	public boolean fileExists(String filename);
	
	/**
	 * Tests, if the user has admin-rights to this resource. Admin-rights
	 * are granted, if the resource is owned by the user or if the user is in
	 * the administrators-group.<BR>
	 * 
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user has admin-rights, else returns false.
	 */
	public boolean adminResource(I_CmsResource resource);
	
	/**
	 * Changes the flags for this resource<BR>
	 * 
	 * The user may change the flags, if he is admin of the resource.
	 * 
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param filename The complete path to the resource.
	 * @param flags The new flags for the resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public void chmod(String filename, int flags)
		throws CmsException;
	
	/**
	 * Changes the owner for this resource<BR>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param filename The complete path to the resource.
	 * @param newOwner The name of the new owner for this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if the newOwner doesn't exists.
	 */
	public void chown(String filename, String newOwner)
		throws CmsException;


	/**
	 * Changes the group for this resource<BR>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param filename The complete path to the resource.
	 * @param newGroup The new of the new group for this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if the newGroup doesn't exists.
	 */
	public void chgrp(String filename, String newGroup)
		throws CmsException;
	
	/**
	 * Returns all users<BR>
	 * 
	 * @return users A Vector of all existing users.
	 */
	public Vector getUsers();
	
	/**
	 * Returns all groups<BR>
	 * 
	 * @return users A Vector of all existing groups.
	 */
	public Vector getGroups();	

	/**
	 * Returns a user object.
	 * 
	 * @param username The name of the user that is to be read.
	 * @return User
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public I_CmsUser readUser(String username)
		throws CmsException;
	
	/**
	 * Returns a user object if the password for the user is correct.
	 * 
	 * @param username The username of the user that is to be read.
	 * @param password The password of the user that is to be read.
	 * @return User
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */		
	public I_CmsUser readUser(String username, String password)
		throws CmsException;
	
	/**
	 * Authentificates a user to the CmsSystem. If the user exists in the system, 
	 * a CmsUser object is created and his session is used for identification.
	 * 
	 * @param username The Name of the user.
	 * @param password The password of the user.
	 * @return A CmsUser Object if authentification was succesful, otherwise null 
	 * will be returned.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public I_CmsUser loginUser(String username, String password)
		throws CmsException;
	
	/**
	 * Returns a group object.
	 * 
	 * @param groupname The name of the group that is to be read.
	 * @return Group.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful
	 */
	public I_CmsGroup readGroup(String groupname)
		throws CmsException;
	
	/**
	 * Returns a list of users in a group.
	 * 
	 * @param groupname The name of the group to list users from.
	 * @return Vector of users.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public Vector getUsersOfGroup(String groupname)
		throws CmsException;
	
	/**
	 * Returns a list of groups of a user.
	 * 
	 * @param username The name of the user.
	 * @return Vector of groups
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Vector getGroupsOfUser(String username)
		throws CmsException;
	
	/**
	 * Checks if a user is member of a group.
	 *  
	 * @param nameuser The name of the user to check.
	 * @param groupname The name of the group to check.
	 * @return True or False
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public boolean userInGroup(String username, String groupname)
		throws CmsException;
		
	// Stuff about metadef and metainfo
	// ?? whats about metadef_types?? (normal|optional|mandatory)
	
	/**
	 * Reads a metadefinition for the given resource type.
	 * 
	 * @param name The name of the metadefinition to read.
	 * @param type The resource type for which the metadefinition is valid.
	 * 
	 * @return metadefinition The metadefinition that corresponds to the overgiven
	 * arguments - or null if there is no valid metadefinition.
	 */
	public I_CmsMetaDefinition readMetaDefinition(String name, I_CmsResourceType type);
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param type The resource type to read the metadefinitions for.
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 */	
	public Vector getAllMetaDefinitions(I_CmsResourceType type);
	
	/**
	 * Reads all metadefinitions for the given resource type.
	 * 
	 * @param type The resource type to read the metadefinitions for.
	 * @param type The type of the metadefinition (normal|mandatory|optional).
	 * 
	 * @return metadefinitions A Vector with metadefefinitions for the resource type.
	 * The Vector is maybe empty.
	 */	
	public Vector getAllMetaDefinitions(I_CmsResourceType resourcetype, int type);

	/**
	 * Returns a MetaInformation of a file or folder.<BR>
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param name The resource-name of which the MetaInformation has to be read.
	 * @param meta The metadefinition-name of which the MetaInformation has to be read.
	 * 
	 * @return metainfo The metainfo as string.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readMetaInformation(String name, String meta)
		throws CmsException;

	/**
	 * Writes a MetaInformation for a file or folder.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param name The resource-name of which the MetaInformation has to be set.
	 * @param meta The metadefinition-name of which the MetaInformation has to be set.
	 * @param value The value for the metainfo to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetaInformation(String name, String meta, String value)
		throws CmsException;

	/**
	 * Writes a couple of MetaInformation for a file or folder.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param name The resource-name of which the MetaInformation has to be set.
	 * @param metainfos A Hashtable with metadefinition- metainfo-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetaInformations(String name, Hashtable metainfos)
		throws CmsException;

	/**
	 * Returns a list of all MetaInformations of a file or folder.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param name The resource-name of which the MetaInformation has to be read
	 * 
	 * @return Vector of MetaInformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Vector getAllMetaInformations(String name)
		throws CmsException;
	
	/**
	 * Deletes all MetaInformation for a file or folder.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param resourcename The resource-name of which the MetaInformation has to be delteted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllMetaInformations(String resourcename)
		throws CmsException;

	/**
	 * Deletes a MetaInformation for a file or folder.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param resourcename The resource-name of which the MetaInformation has to be delteted.
	 * @param meta The metadefinition-name of which the MetaInformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteMetaInformations(String resourcename, String meta)
		throws CmsException;

	/** 
	 * Sets the password for a user.
	 * 
	 * Only a adminstrator or the curretuser can do this.
	 * 
	 * @param username The name of the user.
	 * @param newPassword The new password.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesfull.
	 */
	public void setPassword(String username, String newPassword)
		throws CmsException;
}

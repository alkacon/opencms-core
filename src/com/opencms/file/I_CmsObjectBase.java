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
 * @version $Revision: 1.2 $ $Date: 1999/12/06 09:39:22 $ 
 * 
 */
interface I_CmsObjectBase {	
	
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
	 * Returns a Vector with all I_CmsResourceTypes.
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
	 * @param folder The folder in which the file will be created.
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
	public I_CmsFile createFile(I_CmsFolder folder, String filename, 
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
	 * @param folder The folder in which the file will be created.
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
	public I_CmsFile createFile(I_CmsFolder folder, String filename, 
								byte[] contents, I_CmsResourceType type, 
								Hashtable metainfos)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Reads a file from the Cms.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param folder The folder from which the file will be read.
	 * @param filename The name of the file to be read.
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public I_CmsFile readFile(I_CmsFolder folder, String filename);
	
	/**
	 * Reads a file header from the Cms.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param folder The folder from which the file will be read.
	 * @param filename The name of the file to be read.
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public I_CmsResource readFileHeader(I_CmsFolder folder, String filename);
	
	/**
	 * Writes a file to the Cms.<BR>
	 * The onlineproject will be used for this resource.<BR>
	 * 
	 * @param file The file to write.
	 * 
	 * @exception CmsException will be thrown for missing metainfos, for worng metadefs
	 * or if resourcetype is set to folder. The CmsException will also be thrown, 
	 * if the user has not the rights for this resource.
	 */	
	public void writeFile(I_CmsFile file);
	
	/**
	 * Writes the fileheader to the Cms.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param file The file to write the header of.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be wrote. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void writeFileHeader(I_CmsFile file);
	
	/**
	 * renames the file to the new name.
	 * The onlineproject will be used for this resource<BR>
	 * 
	 * @param file The file to rename.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be wrote. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */		
	public void renameFile(I_CmsFile file, String newname);
	public void deleteFile(I_CmsFile file);	
	public void deleteFile(String filename);	
	public void copyFile(String source, String destination);	
	public void moveFile(String source, String destination);	

	// OK
	// folder creating, reading writing...
	// all is done widthin the online-project
	public I_CmsFolder createFolder(String foldername);	
	public I_CmsFolder createFolder(I_CmsFolder folder, String foldername);
	public I_CmsFolder readFolder(String foldername);
	public void deleteFolder(I_CmsFolder folder);
	public void deleteFolder(String foldername);
	public Vector getSubFolders(String foldername);
	public Vector getSubFolders(I_CmsFolder folder);	
	public Vector getFilesInFolder(String foldername);
	public Vector getFilesInFolder(I_CmsFolder folder);	
	
	// OK
	// test access to files
	public boolean accessFile(String filename);
	public boolean isReadable(String filename);	
	public boolean isWriteable(String filename);
	public boolean isViewable(String filename);
	public boolean isInternal(String filename);	
	public boolean fileExists(String filename);
	
	// ?? what means this?
    public boolean adminResource(I_CmsResource resource);
	public boolean userIsAdminOf(I_CmsResource resource);	
	public boolean userIsAdminOf(I_CmsGroup group);
	
	// OK
	public void chmod(I_CmsResource resource, int flags);
	public void chown(I_CmsResource resource, I_CmsUser newOwner);
	public void chgrp(I_CmsResource resource, I_CmsGroup newGroup);
	
	// OK
	// group and user stuff
	public Vector getUsers();
	public Vector getGroups();	
	public I_CmsUser addUser(String name, String password, I_CmsGroup group, String description, int flags);
	public void deleteUser(I_CmsUser deluser);
	public void deleteUser(String username);	
	public I_CmsUser readUser(String username);	
	public I_CmsUser readUser(String username, String password);	
	public I_CmsUser loginUser(String username, String password);
	public I_CmsGroup addGroup(String name, String description, int flags);
	public void deleteGroup(I_CmsGroup delgroup);	
	public void deleteGroup(String groupname);
	public I_CmsGroup readGroup(String groupname);	
	public void addUserToGroup(I_CmsUser user, I_CmsGroup group);	
	public void removeUserFromGroup(I_CmsUser user, I_CmsGroup group);
	public Vector getUsersOfGroup(I_CmsGroup group);
	public Vector getGroupsOfUser(I_CmsUser user);
	public boolean userInGroup(I_CmsUser user, I_CmsGroup group);
		
	// Stuff about metadef and metainfo
	// ?? whats about metadef_types?? (normal|optional|mandatory)
	public I_CmsMetaDefinition readMetaDefinition(String name, I_CmsResourceType type);
	public void writeMetaDefinition(String name, I_CmsResourceType type);
	public void deleteMetaDefinition(String name, I_CmsResourceType type);	
	public Vector getAllMetaDefinitions(I_CmsResourceType type);	
	public void updateMetaInformation(I_CmsMetaInformation metainfo);		
	public String readMetaInformationValue(String name, String meta);	
	public void writeMetaInformationValue(String name, String meta, String value);
	public void writeMetaInformation(I_CmsResource resource, I_CmsMetaInformation metainfo);	
	public Vector getAllMetaInformations(String name);
	public void deleteAllMetaInformations(I_CmsResource resource);

	// OK
	// Scheduler-stuff
	public void writeScheduleTask(I_CmsScheduleTask scheduleTask);
	public void deleteScheduleTask(I_CmsScheduleTask scheduleTask);
	public Vector readAllScheduleTasks();
}

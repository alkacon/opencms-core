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
 * @version $Revision: 1.2 $ $Date: 1999/12/07 18:50:35 $ 
 * 
 */
interface I_CmsObjectComplete extends I_CmsObjectSecure {	

	/**
	 * Reads a project from the Cms.
	 * 
	 * @param name The name of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public I_CmsProject readProject(String name)
		throws CmsException;
	
	/**
	 * Creates a project.
	 * 
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a project with the same name for this resource-type exists already.
	 */
	public I_CmsProject createProject(String name, String description)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Publishes a project.
	 * 
	 * @param name The name of the project to be published.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public I_CmsProject publishProject(String name)
		throws CmsException;
	
	/**
	 * Creates a new file with the overgiven content and resourcetype.
	 * If there are some mandatory metadefinitions for the resourcetype, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory metainformations.<BR/>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * If there is already a file with this filename, a CmsDuplicateKey exception will
	 * be thrown.
	 * 
	 * @param project The project in which the resource will be used.
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
	public I_CmsFile createFile(I_CmsProject project, String folder, String filename, 
								byte[] contents, I_CmsResourceType type)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Creates a new file with the overgiven content and resourcetype.
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory metainformations.<BR/>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * If there is already a file with this filename, a CmsDuplicateKey exception will
	 * be thrown.
	 * 
	 * @param project The project in which the resource will be used.
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
	public I_CmsFile createFile(I_CmsProject project, String folder, String filename, 
								byte[] contents, I_CmsResourceType type, 
								Hashtable metainfos)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param folder The complete path to the folder from which the file will be read.
	 * @param filename The name of the file to be read.
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public I_CmsFile readFile(I_CmsProject project, String folder, String filename)
		throws CmsException;
	
	/**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param folder The complete path to the folder from which the file will be read.
	 * @param filename The name of the file to be read.
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be read. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public I_CmsResource readFileHeader(I_CmsProject project, String folder, 
										String filename)
		throws CmsException;
	
	/**
	 * Writes a file to the Cms.<BR/>
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be written without
	 * the mandatory metainformations.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param file The file to write.
	 * 
	 * @exception CmsException will be thrown for missing metainfos, for worng metadefs
	 * or if resourcetype is set to folder. The CmsException will also be thrown, 
	 * if the user has not the rights for this resource.
	 */	
	public void writeFile(I_CmsProject project, I_CmsFile file) 
		throws CmsException;
	
	/**
	 * Writes the fileheader to the Cms.
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be written without
	 * the mandatory metainformations.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param resource The resource to write the header of.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be wrote. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void writeFileHeader(I_CmsProject project, I_CmsResource resource)
		throws CmsException;
	
	/**
	 * Writes a file to the Cms.<BR/>
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be written without
	 * the mandatory metainformations.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param file The file to write.
	 * @param metainfos A Hashtable of metainfos, that should be set for this file.
	 * The keys for this Hashtable are the names for metadefinitions, the values are
	 * the values for the metainfos.
	 * 
	 * @exception CmsException will be thrown for missing metainfos, for worng metadefs
	 * or if resourcetype is set to folder. The CmsException will also be thrown, 
	 * if the user has not the rights for this resource.
	 */	
	public void writeFile(I_CmsProject project, I_CmsFile file, Hashtable metainfos)
		throws CmsException;
	
	/**
	 * Writes the fileheader to the Cms.
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory metainformations.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param resource The resource to write the header of.
	 * @param metainfos A Hashtable of metainfos, that should be set for this file.
	 * The keys for this Hashtable are the names for metadefinitions, the values are
	 * the values for the metainfos.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be wrote. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void writeFileHeader(I_CmsProject project, I_CmsResource resource, 
								Hashtable metainfos)
		throws CmsException;

	/**
	 * Renames the file to the new name.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (No path information allowed).
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be renamed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */		
	public void renameFile(I_CmsProject project, String oldname, String newname)
		throws CmsException;
	
	/**
	 * Deletes the file.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path of the file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be deleted. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void deleteFile(I_CmsProject project, String filename)
		throws CmsException;
	
	/**
	 * Copies the file.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be copied. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * the destination filename.
	 */	
	public void copyFile(I_CmsProject project, String source, String destination)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Moves the file.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be moved. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 * @exception CmsDuplikateKeyException if there is already a resource with 
	 * the destination filename.
	 */	
	public void moveFile(I_CmsProject project, String source, String destination)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Sets the resource-type of this resource.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param resource The complete path for the resource to be changed.
	 * @param type The new type for the resource.
	 * 
	 * @exception CmsException will be thrown, if the file type couldn't be changed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public void setResourceType(I_CmsProject project, String resource, 
								I_CmsResourceType newType)
		throws CmsException;

	/**
	 * Creates a new folder.
	 * If there are some mandatory metadefinitions for the folder-resourcetype, a 
	 * CmsException will be thrown, because the folder cannot be created without
	 * the mandatory metainformations.<BR/>
	 * If there is already a folder with this filename, a CmsDuplicateKey exception 
	 * will be thrown.
	 * 
	 * @param project The project in which the resource will be used.
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
	public I_CmsFolder createFolder(I_CmsProject project, String folder, 
									String newFolderName)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Creates a new file with the overgiven content and resourcetype.
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory metainformations.<BR/>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * If there is already a file with this filename, a CmsDuplicateKey exception will
	 * be thrown.
	 * 
	 * @param project The project in which the resource will be used.
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
	public I_CmsFile createFolder(I_CmsProject project, String folder, 
								  String newFolderName, Hashtable metainfos)
		throws CmsException, CmsDuplicateKeyException;

	/**
	 * Reads a folder from the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
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
	public I_CmsFile readFolder(I_CmsProject project, String folder, String folderName)
		throws CmsException;
	
	/**
	 * Renames the folder to the new name.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * renamed, too.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (No path information allowed).
	 * @param force If force is set to true, all sub-resources will be renamed.
	 * If force is set to false, the folder will be renamed only if it is empty.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be renamed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */		
	public void renameFolder(I_CmsProject project, String oldname, 
							 String newname, boolean force)
		throws CmsException;
	
	/**
	 * Deletes the folder.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * delted, too.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete path of the folder.
	 * @param force If force is set to true, all sub-resources will be deleted.
	 * If force is set to false, the folder will be deleted only if it is empty.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be deleted. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void deleteFolder(I_CmsProject project, String foldername, boolean force)
		throws CmsException;
	
	/**
	 * Copies a folder.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * copied, too.
	 * 
	 * @param project The project in which the resource will be used.
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
	public void copyFolder(I_CmsProject project, String source, String destination, 
						   boolean force)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Moves a folder.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * moved, too.
	 * 
	 * @param project The project in which the resource will be used.
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
	public void moveFolder(I_CmsProject project, String source, 
						   String destination, boolean force)
		throws CmsException, CmsDuplicateKeyException;

	/**
	 * Returns a Vector with all subfolders.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfolders A Vector with all subfolders for the overgiven folder.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public Vector getSubFolders(I_CmsProject project, String foldername)
		throws CmsException;
	
	/**
	 * Returns a Vector with all subfiles.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfiles A Vector with all subfiles for the overgiven folder.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public Vector getFilesInFolder(I_CmsProject project, String foldername)
		throws CmsException;
	
	/**
	 * Tests if the user has full access to a resource.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user has full access, else returns false.
	 */
	public boolean accessFile(I_CmsProject project, String filename);

	/**
	 * Tests if the user may read the resource.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user may read, else returns false.
	 */
	public boolean isReadable(I_CmsProject project, String filename);	

	/**
	 * Tests if the user may write the resource.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user may write, else returns false.
	 */
	public boolean isWriteable(I_CmsProject project, String filename);

	/**
	 * Tests if the user may view the resource.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user may view, else returns false.
	 */
	public boolean isViewable(I_CmsProject project, String filename);

	/**
	 * Tests if the resource is an internal resource.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the resource is internal, else returns false.
	 */
	public boolean isInternal(I_CmsProject project, String filename);	

	/**
	 * Tests if the resource exists.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the resource exists, else returns false.
	 */
	public boolean fileExists(I_CmsProject project, String filename);
	
	/**
	 * Tests, if the user has admin-rights to this resource. Admin-rights
	 * are granted, if the resource is owned by the user or if the user is in
	 * the administrators-group.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user has admin-rights, else returns false.
	 */
	public boolean adminResource(I_CmsProject project, I_CmsResource resource);
	
	/**
	 * Changes the flags for this resource<BR/>
	 * 
	 * The user may change the flags, if he is admin of the resource.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path to the resource.
	 * @param flags The new flags for the resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public void chmod(I_CmsProject project, String filename, int flags)
		throws CmsException;
	
	/**
	 * Changes the owner for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path to the resource.
	 * @param newOwner The name of the new owner for this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if the newOwner doesn't exists.
	 */
	public void chown(I_CmsProject project, String filename, String newOwner)
		throws CmsException;

	/**
	 * Changes the group for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path to the resource.
	 * @param newGroup The new of the new group for this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if the newGroup doesn't exists.
	 */
	public void chgrp(I_CmsProject project, String filename, String newGroup)
		throws CmsException;
		
	/**
	 * Returns a MetaInformation of a file or folder.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param name The resource-name of which the MetaInformation has to be read.
	 * @param meta The metadefinition-name of which the MetaInformation has to be read.
	 * 
	 * @return metainfo The metainfo as string.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public String readMetaInformation(I_CmsProject project, String name, String meta)
		throws CmsException;	

	/**
	 * Writes a MetaInformation for a file or folder.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param name The resource-name of which the MetaInformation has to be set.
	 * @param meta The metadefinition-name of which the MetaInformation has to be set.
	 * @param value The value for the metainfo to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetaInformation(I_CmsProject project, String name, 
									 String meta, String value)
		throws CmsException;

	/**
	 * Writes a couple of MetaInformation for a file or folder.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param name The resource-name of which the MetaInformation has to be set.
	 * @param metainfos A Hashtable with metadefinition- metainfo-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetaInformations(I_CmsProject project, String name, 
									  Hashtable metainfos)
		throws CmsException;

	/**
	 * Returns a list of all MetaInformations of a file or folder.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param name The resource-name of which the MetaInformation has to be read
	 * 
	 * @return Vector of MetaInformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Vector getAllMetaInformations(I_CmsProject project, String name)
		throws CmsException;
	
	/**
	 * Deletes all MetaInformation for a file or folder.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param resourcename The resource-name of which the MetaInformation has to be delteted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllMetaInformations(I_CmsProject project, String resourcename)
		throws CmsException;

	/**
	 * Deletes a MetaInformation for a file or folder.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param resourcename The resource-name of which the MetaInformation has to be delteted.
	 * @param meta The metadefinition-name of which the MetaInformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteMetaInformations(I_CmsProject project, String resourcename, 
									   String meta)
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
	public I_CmsUser addUser(String name, String password, String group, 
							 String description, Hashtable additionalInfos, int flags)
		throws CmsException, CmsDuplicateKeyException;
	
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
		throws CmsException;
	
	/**
	 * Updated the userinformation.<BR/>
	 * 
	 * Only the administrator can do this.
	 * 
	 * @param username The name of the user to be updated.
	 * @param additionalInfos A Hashtable with additional infos for the user. These
	 * @param flag The new user access flags.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void updateUser(String username, Hashtable additionalInfos, int flag)
		throws CmsException;
	
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
	 * @exception MhtDuplicateKeyException Throws MhtDuplicateKeyException if 
	 * same group already exists.
	 */	
	public I_CmsGroup addGroup(String name, String description, int flags)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Delete a group from the Cms.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param delgroup The name of the group that is to be deleted.
	 * @exception CmsException  Throws CmsException if operation was not succesfull.
	 */	
	public void deleteGroup(String delgroup)
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
	public void addUserToGroup(String username, String groupname)
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
	public void removeUserFromGroup(String username, String groupname)
		throws CmsException;
	
	/**
	 * Writes the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The resource-type for the metadefinition.
	 * @param type The type of the metadefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a metadefinition with the same name for this resource-type exists already.
	 */
	public void writeMetaDefinition(String name, I_CmsResourceType resourcetype, 
									int type)
		throws CmsDuplicateKeyException, CmsException;
	
	/**
	 * Delete the metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the metadefinition to overwrite.
	 * @param resourcetype The resource-type for the metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deleteMetaDefinition(String name, I_CmsResourceType type)
		throws CmsException;

	/**
	 * Writes a shedule-task to the Cms.<BR/>
	 * The user of the task will be set to the current user.
	 * 
	 * @param scheduleTask the task that should be written to the Cms.
	 * 
	 * @exception CmsException if something goes wrong.
	 */
	public void writeScheduleTask(I_CmsScheduleTask scheduleTask)
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
	public void deleteScheduleTask(I_CmsScheduleTask scheduleTask)
		throws CmsException;
	
	/**
	 * Reads all shedule-task from the Cms.
	 * 
	 * @return scheduleTasks A Vector with all schedule-Tasks of the Cms.
	 * 
	 * @exception CmsException if something goes wrong.
	 */
	public Vector readAllScheduleTasks()
		throws CmsException;
}

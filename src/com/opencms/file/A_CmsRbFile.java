package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This abstract class describes a resource broker for files and folders in 
 * the Cms.<BR/>
 * <B>All</B> Methods get a first parameter: I_CmsUser. It is the current user. This 
 * is for security-reasons, to check if this current user has the rights to call the
 * method.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/13 16:29:59 $
 */
abstract class A_CmsRbFile {
	
	/**
	 * Returns the root-folder object.<P/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @return the root-folder object.
	 */
	abstract I_CmsFolder rootFolder(I_CmsUser callingUser);
	
	/**
	 * Creates a new file with the overgiven content and resourcetype.
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory metainformations.<BR/>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * If there is already a file with this filename, a CmsDuplicateKey exception will
	 * be thrown.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the folder-resource is not locked by another user</li>
	 * <li>the file dosn't exists</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param folder The complete path to the folder in which the file will be created.
	 * @param filename The name of the new file (I_CmsUser callingUser, No pathinformation allowed).
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
	abstract I_CmsFile createFile(I_CmsUser callingUser, String project, String folder, String filename, 
								byte[] contents, I_CmsResourceType type, 
								Hashtable metainfos)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
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
	abstract I_CmsFile readFile(I_CmsUser callingUser, String project, String folder, String filename)
		throws CmsException;
	
	/**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
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
	abstract I_CmsResource readFileHeader(I_CmsUser callingUser, String project, String folder, 
										String filename)
		throws CmsException;
	
	/**
	 * Writes a file to the Cms.<BR/>
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be written without
	 * the mandatory metainformations.<BR/>
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
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
	abstract void writeFile(I_CmsUser callingUser, String project, 
				   I_CmsFile file, Hashtable metainfos)
		throws CmsException;
	
	/**
	 * Writes the fileheader to the Cms.
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory metainformations.<BR/>
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is  locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
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
	abstract void writeFileHeader(I_CmsUser callingUser, String project, 
						 I_CmsResource resource, Hashtable metainfos)
		throws CmsException;

	/**
	 * Renames the file to the new name.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (I_CmsUser callingUser, No path information allowed).
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be renamed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */		
	abstract void renameFile(I_CmsUser callingUser, String project, 
					String oldname, String newname)
		throws CmsException;
	
	/**
	 * Deletes the file.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callinUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path of the file.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be deleted. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	abstract void deleteFile(I_CmsUser callingUser, String project, String filename)
		throws CmsException;
	
	/**
	 * Copies the file.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the sourceresource</li>
	 * <li>the user can write the destinationresource</li>
	 * <li>the destinationresource doesn't exists</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
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
	abstract void copyFile(I_CmsUser callingUser, String project, String source, String destination)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Moves the file.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write the sourceresource</li>
	 * <li>the user can write the destinationresource</li>
	 * <li>the sourceresource is locked by the user</li>
	 * <li>the destinationresource dosn't exists</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
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
	abstract void moveFile(I_CmsUser callingUser, String project, String source, 
				  String destination)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Sets the resource-type of this resource.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the calling user</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param resource The complete path for the resource to be changed.
	 * @param type The new type for the resource.
	 * @param metainfos A Hashtable of metainfos, that should be set for this file.
	 * 
	 * @exception CmsException will be thrown, if the file type couldn't be changed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	abstract void setResourceType(I_CmsUser callingUser, String project, String resource, 
								I_CmsResourceType newType, Hashtable metainfos)
		throws CmsException;

	/**
	 * Copies a file and its Metainformations to a new temporary file.<BR/>
	 * All accessflags will be copied, but all visible flags will be deleted.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param source The complete path to the sourcefile.
	 * @return file The new temporary file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if 
	 * same templfile already exists
	 */		
    abstract I_CmsFile copyTemporaryFile(I_CmsUser callingUser, String source)
            throws CmsException, CmsDuplicateKeyException;    
    
    /**
	 * Copies all changes in a temporary file to the original file
	 * and deletes the temporary file. 
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param source The complete path to the sourcefile.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
    abstract void commitTemporaryFile(I_CmsUser callingUser, String source) 
            throws CmsException;
    
    /**
	 * Deletes an existing temporary copy of a given file.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param source The complete path to the sourcefile.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
    abstract void deleteTemporaryFile(I_CmsUser callingUser, String source) 
            throws CmsException;
	
	/**
	 * Creates a new folder with the overgiven resourcetype and metainfos.
	 * If some mandatory metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory metainformations.<BR/>
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * If there is already a file with this filename, a CmsDuplicateKey exception will
	 * be thrown.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is not locked by another user</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param folder The complete path to the folder in which the new folder will 
	 * be created.
	 * @param newFolderName The name of the new folder (I_CmsUser callingUser, No pathinformation allowed).
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
	abstract I_CmsFolder createFolder(I_CmsUser callingUser, String project, String folder, 
								  String newFolderName, Hashtable metainfos)
		throws CmsException, CmsDuplicateKeyException;

	/**
	 * Reads a folder from the Cms.<BR/>
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
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
	abstract I_CmsFolder readFolder(I_CmsUser callingUser, String project, String folder, String folderName)
		throws CmsException;
	
	/**
	 * Renames the folder to the new name.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * renamed, too.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write this resource and all subresources</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (I_CmsUser callingUser, No path information allowed).
	 * @param force If force is set to true, all sub-resources will be renamed.
	 * If force is set to false, the folder will be renamed only if it is empty.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be renamed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */		
	abstract void renameFolder(I_CmsUser callingUser, String project, String oldname, 
							 String newname, boolean force)
		throws CmsException;
	
	/**
	 * Deletes the folder.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * delted, too.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write this resource and all subresources</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete path of the folder.
	 * @param force If force is set to true, all sub-resources will be deleted.
	 * If force is set to false, the folder will be deleted only if it is empty.
	 * 
	 * @exception CmsException will be thrown, if the folder couldn't be deleted. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	abstract void deleteFolder(I_CmsUser callingUser, String project, String foldername, boolean force)
		throws CmsException;
	
	/**
	 * Copies a folder.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * copied, too.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read this sourceresource and all subresources</li>
	 * <li>the user can write the targetresource</li>
	 * <li>the sourceresource is locked by the callingUser</li>
	 * <li>the targetresource dosn't exist</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
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
	abstract void copyFolder(I_CmsUser callingUser, String project, String source, String destination, 
						   boolean force)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Moves a folder.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * moved, too.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write this sourceresource and all subresources</li>
	 * <li>the user can write the targetresource</li>
	 * <li>the sourceresource is locked by the callingUser</li>
	 * <li>the targetresource dosn't exist</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
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
	abstract void moveFolder(I_CmsUser callingUser, String project, String source, 
						   String destination, boolean force)
		throws CmsException, CmsDuplicateKeyException;

	/**
	 * Returns a Vector with all subfolders.<BR/>
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read this resource</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfolders A Vector with all subfolders for the overgiven folder.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	abstract Vector getSubFolders(I_CmsUser callingUser, String project, String foldername)
		throws CmsException;
	
	/**
	 * Returns a Vector with all subfiles.<BR/>
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read this resource</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfiles A Vector with all subfiles for the overgiven folder.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	abstract Vector getFilesInFolder(I_CmsUser callingUser, String project, String foldername)
		throws CmsException;
	
	/**
	 * Tests if the user has full access to a resource.
	 * 
	 * <B>Security:</B>
	 * All users are granted.<BR/>
	 * <B>returns true, if</B>
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read write and view this resource</li>
	 * <li>this resource is not locked, or it is locked by the calling user</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user has full access, else returns false.
	 */
	abstract boolean accessFile(I_CmsUser callingUser, String project, String filename);

	/**
	 * Tests if the user may read the resource.
	 * 
	 * <B>Security:</B>
	 * All users are granted.<BR/>
	 * <B>returns true, if</B>
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read this resource</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user may read, else returns false.
	 */
	abstract boolean isReadable(I_CmsUser callingUser, String project, String filename);	

	/**
	 * Tests if the user may write the resource.
	 * 
	 * <B>Security:</B>
	 * All users are granted.<BR/>
	 * <B>returns true, if</B>
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write this resource</li>
	 * <li>this resource is not locked, or it is locked by the calling user</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user may write, else returns false.
	 */
	abstract boolean isWriteable(I_CmsUser callingUser, String project, String filename);

	/**
	 * Tests if the user may view the resource.
	 * 
	 * <B>Security:</B>
	 * All users are granted.<BR/>
	 * <B>returns true, if</B>
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can view this resource</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user may view, else returns false.
	 */
	abstract boolean isViewable(I_CmsUser callingUser, String project, String filename);

	/**
	 * Tests if the resource is an internal resource.
	 * 
	 * <B>Security:</B>
	 * All users are granted.<BR/>
	 * <B>returns true, if</B>
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can view this resource</li>
	 * <li>the resource has set the internal flag</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the resource is internal, else returns false.
	 */
	abstract boolean isInternal(I_CmsUser callingUser, String project, String filename);	

	/**
	 * Tests if the resource exists.
	 * 
	 * <B>Security:</B>
	 * All users are granted.<BR/>
	 * <B>returns true, if</B>
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can view this resource</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the resource exists, else returns false.
	 */
	abstract boolean fileExists(I_CmsUser callingUser, String project, String filename);

	/**
	 * Tests, if the user has admin-rights to this resource. Admin-rights
	 * are granted, if the resource is owned by the user or if the user is in
	 * the administrators-group.<BR/>
	 * 
	 * <B>Security:</B>
	 * All users are granted.<BR/>
	 * <B>returns true, if</B>
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user is owner of the project</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user has admin-rights, else returns false.
	 */
	abstract boolean adminResource(I_CmsUser callingUser, String project, String filename);
	
	/**
	 * Changes the flags for this resource<BR/>
	 * 
	 * The user may change the flags, if he is admin of the resource.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path to the resource.
	 * @param flags The new flags for the resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource.
	 */
	abstract void chmod(I_CmsUser callingUser, String project, String filename, int flags)
		throws CmsException;
	
	/**
	 * Changes the owner for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user is owner of the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path to the resource.
	 * @param newOwner The name of the new owner for this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if the newOwner doesn't exists.
	 */
	abstract void chown(I_CmsUser callingUser, String project, String filename, String newOwner)
		throws CmsException;

	/**
	 * Changes the group for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user is owner of the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path to the resource.
	 * @param newGroup The new of the new group for this resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if the newGroup doesn't exists.
	 */
	abstract void chgrp(I_CmsUser callingUser, String project, String filename, String newGroup)
		throws CmsException;

	/**
	 * Locks a resource<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is not locked by another user</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param resource The complete path to the resource to lock.
	 * @param force If force is true, a existing locking will be oberwritten.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. It will also be thrown, if there is a existing lock
	 * and force was set to false.
	 */
	abstract void lockFile(I_CmsUser callingUser, String project, String resource, boolean force)
		throws CmsException;
	
	/**
	 * Tests, if a resource was locked<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource. This methods checks, if a resource was locked.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param resource The complete path to the resource.
	 * 
	 * @return true, if the resource is locked else it returns false.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. 
	 */
	abstract boolean isLocked(I_CmsUser callingUser, String project, String resource)
		throws CmsException;
	
	/**
	 * Returns the user, who had locked the resource.<BR/>
	 * 
	 * A user can lock a resource, so he is the only one who can write this 
	 * resource. This methods checks, if a resource was locked.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param resource The complete path to the resource.
	 * 
	 * @return true, if the resource is locked else it returns false.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. 
	 */
	abstract I_CmsUser lockedBy(I_CmsUser callingUser, String project, String resource)
		throws CmsException;

	/**
	 * Returns a MetaInformation of a file or folder.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param name The resource-name of which the MetaInformation has to be read.
	 * @param meta The metadefinition-name of which the MetaInformation has to be read.
	 * 
	 * @return metainfo The metainfo as string.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract String readMetaInformation(I_CmsUser callingUser, String project, String name, String meta)
		throws CmsException;	

	/**
	 * Writes a MetaInformation for a file or folder.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param name The resource-name of which the MetaInformation has to be set.
	 * @param meta The metadefinition-name of which the MetaInformation has to be set.
	 * @param value The value for the metainfo to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract void writeMetaInformation(I_CmsUser callingUser, String project, String name, 
									 String meta, String value)
		throws CmsException;

	/**
	 * Writes a couple of MetaInformation for a file or folder.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param name The resource-name of which the MetaInformation has to be set.
	 * @param metainfos A Hashtable with metadefinition- metainfo-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract void writeMetaInformations(I_CmsUser callingUser, String project, String name, 
									  Hashtable metainfos)
		throws CmsException;

	/**
	 * Returns a list of all MetaInformations of a file or folder.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param name The resource-name of which the MetaInformation has to be read
	 * 
	 * @return Vector of MetaInformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract Vector readAllMetaInformations(I_CmsUser callingUser, String project, String name)
		throws CmsException;
	
	/**
	 * Deletes all MetaInformation for a file or folder.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param resourcename The resource-name of which the MetaInformation has to be delteted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract void deleteAllMetaInformations(I_CmsUser callingUser, String project, String resourcename)
		throws CmsException;

	/**
	 * Deletes a MetaInformation for a file or folder.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param resourcename The resource-name of which the MetaInformation has to be delteted.
	 * @param meta The metadefinition-name of which the MetaInformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	abstract void deleteMetaInformation(I_CmsUser callingUser, String project, String resourcename, 
									  String meta)
		throws CmsException;

	/**
	 * Declines a resource. The resource can be copied to the onlineproject.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user is owner of the project</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The name of the project.
	 * @param resource The full path to the resource, which will be declined.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract void declineResource(I_CmsUser callingUser, String project, String resource)
		throws CmsException;

	/**
	 * Rejects a resource. The resource will be copied to the following project,
	 * at publishing time.
	 * 
	 * <B>Security:</B>
	 * Access is cranted, if:
	 * <ul>
	 * <li>the user is owner of the project</li>
	 * </ul>
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The name of the project.
	 * @param resource The full path to the resource, which will be declined.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	abstract void rejectResource(I_CmsUser callingUser, String project, String resource)
		throws CmsException;

	/**
	 * Returns the actual number of Filesystem-changes since starting the cms.<BR/>
	 * This can be used to write intelligent caching-operations.
	 * 
	 * <B>Security:</B>
	 * All users are granted.
	 * 
	 * @return the actual number of Filesystem-changes since starting the cms.
	 */
	abstract long getNumberOfFsChanges(I_CmsUser callingUser);
}

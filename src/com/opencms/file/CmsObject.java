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
 * @version $Revision: 1.10 $ $Date: 2000/01/04 16:52:44 $ 
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
	 * @param req the HttpServletRequest.
	 * @param resp the HttpServletResponse.
	 * @param user The current user for this request.
	 * @param currentGroup The current group for this request.
	 * @param currentProject The current project for this request.
	 */
	public void init(HttpServletRequest req, HttpServletResponse resp, 
					 String user, String currentGroup, String currentProject ) 
		throws CmsException {
		m_context = new CmsRequestContext();
		m_context.init(c_rb, req, resp, user, currentGroup, currentProject);
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
	 */
	public CmsFolder rootFolder() {
		return null;
	}
	
	/**
	 * Returns the anonymous user object.
	 * 
	 * @return the anonymous user object.
	 */
	public A_CmsUser anonymousUser() {
		return null;
	}
	
	/**
	 * Returns the onlineproject. This is the default project. All anonymous 
	 * (or guest) user will see the rersources of this project.
	 * 
	 * @return the onlineproject object.
	 */
	public A_CmsProject onlineProject() {
		return null;
	}

	/**
	 * Returns a Vector with all I_CmsResourceTypes.
	 * 
	 * Returns a Vector with all I_CmsResourceTypes.
	 */
	public Vector getAllResourceTypes() { 
		return null; // TODO: implement this! 
	}
	
	/**
	 * Reads a project from the Cms.
	 * 
	 * @param name The name of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsProject readProject(String name)
		throws CmsException { 
		return null; // TODO: implement this! 
	}
	
	/**
	 * Creates a project.
	 * 
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param flags The flags for the project (e.g. visibility).
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a project with the same name for this resource-type exists already.
	 */
	public A_CmsProject createProject(String name, String description, int flags)
		throws CmsException { 
		return null; // TODO: implement this! 
	}
	
	/**
	 * Publishes a project.
	 * 
	 * @param name The name of the project to be published.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsProject publishProject(String name)
		throws CmsException { 
		return null; // TODO: implement this! 
	}
	
	/**
	 * Returns all projects, which the user may access.
	 * 
	 * @param projectname the name of the project.
	 * 
	 * @return a Vector of projects.
	 */
	public Vector getAllAccessibleProjects(String projectname) {
		return null; // TODO: implement this! 
	}
	
	/**
	 * Declines a resource. The resource can be copied to the onlineproject.
	 * 
	 * @param project The name of the project.
	 * @param resource The full path to the resource, which will be declined.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void declineResource(String project, String resource)
		throws CmsException { 
		return ; // TODO: implement this! 
	}

	/**
	 * Rejects a resource. The resource will be copied to the following project,
	 * at publishing time.
	 * 
	 * @param project The name of the project.
	 * @param resource The full path to the resource, which will be declined.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void rejectResource(String project, String resource)
		throws CmsException { 
		return ; // TODO: implement this! 
	}

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
	public CmsFile createFile(String folder, String filename, 
								byte[] contents, A_CmsResourceType type)
		throws CmsException { 
		return null; // TODO: implement this! 
	}
	
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
	public CmsFile createFile(String folder, String filename, 
								byte[] contents, A_CmsResourceType type, 
								Hashtable metainfos)
		throws CmsException { 
		return null; // TODO: implement this! 
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
		return null; // TODO: implement this! 
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
		return null; // TODO: implement this! 
	}
	
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
	public void writeFile(CmsFile file) 
		throws CmsException { 
		return ; // TODO: implement this! 
	}
	
	/**
	 * Writes the fileheader to the Cms.
	 * If some mandatory Metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be written without
	 * the mandatory Metainformations.<BR/>
	 * 
	 * @param resource The resource to write the header of.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be wrote. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void writeFileHeader(A_CmsResource resource)
		throws CmsException { 
		return ; // TODO: implement this! 
	}
	
	/**
	 * Writes a file to the Cms.<BR/>
	 * If some mandatory Metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be written without
	 * the mandatory Metainformations.<BR/>
	 * 
	 * @param file The file to write.
	 * @param metainfos A Hashtable of metainfos, that should be set for this file.
	 * The keys for this Hashtable are the names for Metadefinitions, the values are
	 * the values for the metainfos.
	 * 
	 * @exception CmsException will be thrown for missing metainfos, for worng metadefs
	 * or if resourcetype is set to folder. The CmsException will also be thrown, 
	 * if the user has not the rights for this resource.
	 */	
	public void writeFile(CmsFile file, Hashtable metainfos)
		throws CmsException {
		return ; // TODO: implement this! 
	}
	
	/**
	 * Writes the fileheader to the Cms.
	 * If some mandatory Metadefinitions for the resourcetype are missing, a 
	 * CmsException will be thrown, because the file cannot be created without
	 * the mandatory Metainformations.<BR/>
	 * 
	 * @param resource The resource to write the header of.
	 * @param metainfos A Hashtable of metainfos, that should be set for this file.
	 * The keys for this Hashtable are the names for Metadefinitions, the values are
	 * the values for the metainfos.
	 * 
	 * @exception CmsException will be thrown, if the file couldn't be wrote. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */	
	public void writeFileHeader(A_CmsResource resource, Hashtable metainfos)
		throws CmsException { 
		return ; // TODO: implement this! 
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
		return ; // TODO: implement this! 
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
		return ; // TODO: implement this! 
	}
	
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
	public void copyFile(String source, String destination)
		throws CmsException { 
		return ; // TODO: implement this! 
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
		return ; // TODO: implement this! 
	}
	
	/**
	 * Sets the resource-type of this resource.
	 * 
	 * @param resource The complete path for the resource to be changed.
	 * @param type The new type for the resource.
	 * 
	 * @exception CmsException will be thrown, if the file type couldn't be changed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public void setResourceType(String resource, A_CmsResourceType newType)
		throws CmsException { 
		return ; // TODO: implement this! 
	}
	
	/**
	 * Sets the resource-type of this resource.
	 * The onlineproject will be used for this resource<BR/>
	 * 
	 * @param resource The complete path for the resource to be changed.
	 * @param type The new type for the resource.
	 * @param metainfos A Hashtable of metainfos, that should be set for this file.
	 * 
	 * @exception CmsException will be thrown, if the file type couldn't be changed. 
	 * The CmsException will also be thrown, if the user has not the rights 
	 * for this resource.
	 */
	public void setResourceType(String resource, A_CmsResourceType newType, 
										 Hashtable metainfos)
		throws CmsException { 
		return ; // TODO: implement this! 
	}

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
	public CmsFolder createFolder(String folder, String newFolderName)
		throws CmsException { 
		return null; // TODO: implement this! 
	}
	
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
	public CmsFolder createFolder(String folder, String newFolderName, 
											 Hashtable metainfos)
		throws CmsException { 
		return null; // TODO: implement this! 
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
		return null; // TODO: implement this! 
	}
	
	/**
	 * Renames the folder to the new name.
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
	public void renameFolder(String oldname, String newname, 
									  boolean force)
		throws CmsException { 
		return ; // TODO: implement this! 
	}
	
	/**
	 * Deletes the folder.
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
		throws CmsException { 
		return ; // TODO: implement this! 
	}
	
	/**
	 * Copies a folder.
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
	public void copyFolder(String source, String destination, 
									boolean force)
		throws CmsException { 
		return ; // TODO: implement this! 
	}
	
	/**
	 * Moves a folder.
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
	public void moveFolder(String source, String destination, 
									boolean force)
		throws CmsException { 
		return ; // TODO: implement this! 
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
		return null; // TODO: implement this! 
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
		return null; // TODO: implement this! 
	}

	/**
	 * Tests if the user may write the resource.
	 * 
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the user may write, else returns false.
	 */
	public boolean isWriteable(String filename) { 
		return false; // TODO: implement this! 
	}

	/**
	 * Tests if the resource exists.
	 * 
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the resource exists, else returns false.
	 */
	public boolean fileExists(String filename) { 
		return false; // TODO: implement this! 
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
		return ; // TODO: implement this! 
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
		return ; // TODO: implement this! 
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
		return ; // TODO: implement this! 
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
	public void lockFile(String resource, boolean force)
		throws CmsException { 
		return ; // TODO: implement this! 
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
	public void unlockFile(String resource)
		throws CmsException { 
		return ; // TODO: implement this! 
	}

	/**
	 * Tests, if a resource was locked<BR/>
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
	public boolean isLocked(String resource)
		throws CmsException { 
		return false; // TODO: implement this! 
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
		return null; // TODO: implement this! 
	}

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
	public String readMetainformation(String name, String meta)
		throws CmsException { 
		return null; // TODO: implement this! 
	}

	/**
	 * Writes a Metainformation for a file or folder.
	 * 
	 * @param name The resource-name of which the Metainformation has to be set.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * @param value The value for the metainfo to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformation(String name, String meta, String value)
		throws CmsException { 
		return ; // TODO: implement this! 
	}

	/**
	 * Writes a couple of Metainformation for a file or folder.
	 * 
	 * @param name The resource-name of which the Metainformation has to be set.
	 * @param metainfos A Hashtable with Metadefinition- metainfo-pairs as strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void writeMetainformations(String name, Hashtable metainfos)
		throws CmsException { 
		return ; // TODO: implement this! 
	}

	/**
	 * Returns a list of all Metainformations of a file or folder.
	 * 
	 * @param name The resource-name of which the Metainformation has to be read
	 * 
	 * @return Vector of Metainformation as Strings.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public Vector readAllMetainformations(String name)
		throws CmsException { 
		return null; // TODO: implement this! 
	}
	
	/**
	 * Deletes all Metainformation for a file or folder.
	 * 
	 * @param resourcename The resource-name of which the Metainformation has to be delteted.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteAllMetainformations(String resourcename)
		throws CmsException { 
		return ; // TODO: implement this! 
	}

	/**
	 * Deletes a Metainformation for a file or folder.
	 * 
	 * @param resourcename The resource-name of which the Metainformation has to be delteted.
	 * @param meta The Metadefinition-name of which the Metainformation has to be set.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	public void deleteMetainformation(String resourcename, String meta)
		throws CmsException { 
		return ; // TODO: implement this! 
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
		return( c_rb.getUsers(m_context.currentUser(), m_context.getCurrentProject()) );
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
		return( c_rb.getGroups(m_context.currentUser(), m_context.getCurrentProject()) );
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
		return( c_rb.getChild(m_context.currentUser(), m_context.getCurrentProject(), 
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
							  m_context.getCurrentProject(), 
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
        return( c_rb.readUser(m_context.currentUser(), m_context.getCurrentProject(),
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
   		A_CmsUser newUser = readUser(username, password);
		
		// is the user enabled?
		if( ! ( ( newUser.getFlags() & C_FLAG_DISABLED ) == C_FLAG_DISABLED ) ) {
			// Yes - log him in!
			init(m_context.getRequest(), m_context.getResponse(), newUser.getName(), 
				 newUser.getDefaultGroup().getName(), C_PROJECT_ONLINE);
			return(newUser.getName());
		} else {
			// No Access!
			throw new CmsException(CmsException.C_EXTXT[CmsException.C_NO_ACCESS], 
				CmsException.C_NO_ACCESS );
		}		
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
		c_rb.setPassword(m_context.currentUser(), m_context.getCurrentProject(), 
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
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a user with the given username exists already.
	 */
	public A_CmsUser addUser(String name, String password, String group, 
							 String description, Hashtable additionalInfos, int flags)
		throws CmsException { 
		
		return( c_rb.addUser(m_context.currentUser(), m_context.getCurrentProject(),
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
		c_rb.deleteUser(m_context.currentUser(), m_context.getCurrentProject(), username);
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
		c_rb.writeUser(m_context.currentUser(), m_context.getCurrentProject(), user );
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
									 m_context.getCurrentProject(), groupname ));
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
									 m_context.getCurrentProject(), username ));
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
		return( c_rb.userInGroup(m_context.currentUser(), m_context.getCurrentProject(), 
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
		return( c_rb.readGroup(m_context.currentUser(), m_context.getCurrentProject(), 
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
	 * @exception MhtDuplicateKeyException Throws MhtDuplicateKeyException if 
	 * same group already exists.
	 */	
	public A_CmsGroup addGroup(String name, String description, int flags, String parent)
		throws CmsException { 
		return( c_rb.addGroup(m_context.currentUser(), m_context.getCurrentProject(),
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
		c_rb.writeGroup(m_context.currentUser(), m_context.getCurrentProject(),
						group);
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
		c_rb.deleteGroup(m_context.currentUser(), m_context.getCurrentProject(), 
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
		c_rb.addUserToGroup(m_context.currentUser(), m_context.getCurrentProject(), 
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
		c_rb.removeUserFromGroup(m_context.currentUser(), m_context.getCurrentProject(), 
								 username, groupname );
	}
	
	/**
	 * Reads the Metadefinition for the resource type.<BR/>
	 * 
	 * @param name The name of the Metadefinition to read.
	 * @param resourcetype The resource-type for the Metadefinition.
	 * @return the Metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsMetadefinition readMetadefinition(String name, 
														   A_CmsResourceType type)
		throws CmsException { 
		return null; // TODO: implement this! 
	}

	/**
	 * Reads all Metadefinitions for the resource type.<BR/>
	 * 
	 * @param resourcetype The resource-type for the Metadefinition.
	 * @return a Vector of Metadefinitions.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector getAllMetadefinitions(A_CmsResourceType type)
		throws CmsException { 
		return null; // TODO: implement this! 
	}

	/**
	 * Writes the Metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the Metadefinition to overwrite.
	 * @param resourcetype The resource-type for the Metadefinition.
	 * @param type The type of the Metadefinition (normal|mandatory|optional)
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a Metadefinition with the same name for this resource-type exists already.
	 */
	public void writeMetadefinition(String name, A_CmsResourceType resourcetype, 
									int type)
		throws  CmsException { 
		return ; // TODO: implement this! 
	}
	
	/**
	 * Delete the Metadefinition for the resource type.<BR/>
	 * 
	 * Only the admin can do this.
	 * 
	 * @param name The name of the Metadefinition to overwrite.
	 * @param resourcetype The resource-type for the Metadefinition.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void deleteMetadefinition(String name, A_CmsResourceType type)
		throws CmsException { 
		return ; // TODO: implement this! 
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
		return ; // TODO: implement this! 
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
		return ; // TODO: implement this! 
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
		return null; // TODO: implement this! 
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
		c_rb.addMountPoint(m_context.currentUser(), m_context.getCurrentProject(),
						   mountpoint, driver, connect, name);
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
									m_context.getCurrentProject(),
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
		c_rb.deleteMountPoint(m_context.currentUser(), m_context.getCurrentProject(),
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
									   m_context.getCurrentProject()) );
	}

	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * @param filename The name of the file to be read.
	 * 
	 * @return The file read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 * */
	 public CmsFile readFile(A_CmsUser currentUser, A_CmsProject currentProject,
							 String filename)
		 throws CmsException {
		 return( c_rb.readFile(m_context.currentUser(), 
							   m_context.getCurrentProject(), filename ) );
	 }
}

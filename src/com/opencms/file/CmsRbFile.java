package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This  class describes a resource broker for files and folders in 
 * the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.3 $ $Date: 1999/12/22 17:56:21 $
 */
 class CmsRbFile implements I_CmsRbFile, I_CmsConstants {
	
     /**
     * The file access object which is required to access the file database.
     */
    private I_CmsAccessFile m_accessFile;
    
     /**
     * Constructor, creates a new File Resource Broker.
     * 
     * @param accessFile The file access object.
     */
    public CmsRbFile(I_CmsAccessFile accessFile)
    {
        m_accessFile=accessFile;
    }
    
	
	/**
	 * Creates a new file with thegiven content and resourcetype.
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
	 * @param user The user who own this file.
	 * @param project The project in which the resource will be used.
	 * @param filename The name of the new file (including pathinformation).
	 * @param flags The flags of this resource.
	 * @param contents The contents of the new file.
	 * @param type The resourcetype of the new file.
	 * @return file The created file.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	 public CmsFile createFile(A_CmsUser user, A_CmsProject project,
                               String filename, int flags,
							   byte[] contents, A_CmsResourceType type) 
						
         throws CmsException {
               return m_accessFile.createFile(user,project,filename,flags,contents,type);
     }
	
	
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
	 * @param project The project in which the resource will be used.
	 * @param filename The name of the file to be read.
	 * 
	 * @return The file read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 * */
	 public CmsFile readFile(A_CmsProject project, String filename)
		throws CmsException{
        return m_accessFile.readFile(project,filename);
     }
	
	 /**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the resource</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The name of the file to be read.
	 * 
	 * @return The file read from the Cms.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	 public A_CmsResource readFileHeader(A_CmsProject project, String filename)
		 throws CmsException {
         return m_accessFile.readFileHeader(project,filename);
     }
	
	 /**
	 * Writes a file to the Cms.<BR/>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param file The file to write.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void writeFile(A_CmsProject project, CmsFile file)
		throws CmsException{
        m_accessFile.writeFile(project,file);
     }
	
	 /**
	 * Writes the fileheader to the Cms.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is  locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param file The file to write the header of.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void writeFileHeader(A_CmsProject project, CmsFile file)
		throws CmsException{
        m_accessFile.writeFileHeader(project,file);
     }

	 /**
	 * Renames the file to a new name.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource (A_CmsUser callingUser, No path information allowed).
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */		
	public void renameFile(A_CmsProject project, 
					       String oldname, String newname)
		throws CmsException {
        m_accessFile.renameFile(project,oldname,newname);
     }
	
	/**
	 * Deletes a file in the Cms.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callinUser</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path of the file.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void deleteFile(A_CmsProject project, String filename)
		throws CmsException{
        m_accessFile.deleteFile(project,filename);
     }
	

	/**
	 * Copies a file in the Cms
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read the sourceresource</li>
	 * <li>the user can write the destinationresource</li>
	 * <li>the destinationresource doesn't exists</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void copyFile(A_CmsProject project, String source, String destination)
		throws CmsException {
        m_accessFile.copyFile(project,source,destination);
     }
	
	 /**
	 * Moves a file in the Cms
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
	 * @param project The project in which the resource will be used.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void moveFile(A_CmsProject project, String source,String destination)
		throws CmsException {
        m_accessFile.moveFile(project,source,destination);
     }

	
	 /**
	 * Creates a new folder in the Cms.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is not locked by another user</li>
	 * </ul>
	 * 
	 * @param user The user who owns the new folder.
	 * @param project The project in which the resource will be used.
	 * @param folder The name of the new folder (including pathinformation).
	 * @param flags The flags of this resource.
	 * 
	 * @return The created folder.
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public CmsFolder createFolder(A_CmsUser user, A_CmsProject project, String folder,
                                  int flags)						
		throws CmsException{
         return m_accessFile.createFolder(user,project,folder,flags);
     }

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
	 * @param project The project in which the resource will be used.
	 * @param folder The name of the folder to be read.
	 * 
	 * @return The read folder read from the Cms.
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public CmsFolder readFolder(A_CmsProject project, String folder)
		throws CmsException{
         return m_accessFile.readFolder(project,folder);
     }
	
     /**
	 * Writes a folder to the Cms.<BR/>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param folder The folder to write.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void writeFolder(A_CmsProject project, CmsFolder folder)
        throws CmsException {
        m_accessFile.writeFolder(project, folder);
    }
    
	/**
	 * Renames the folder to the new name.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * renamed, too.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write this resource and all subresources</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource 
	 * @param force If force is set to true, all sub-resources will be renamed.
	 * If force is set to false, the folder will be renamed only if it is empty.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */		
	public void renameFolder(A_CmsProject project, String oldname, 
							 String newname, boolean force)
		throws CmsException {
        m_accessFile.renameFolder(project,oldname,newname,force);
     }
	
     /**
	 * Deletes a folder in the Cms.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * delted, too.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write this resource and all subresources</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete path of the folder.
	 * @param force If force is set to true, all sub-resources will be deleted.
	 * If force is set to false, the folder will be deleted only if it is empty.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void deleteFolder(A_CmsProject project, String foldername, boolean force)
		throws CmsException {
        m_accessFile.deleteFolder(project,foldername,force);
     }
	
	 /**
	 * Copies a folder in the Cms.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * copied, too.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read this sourceresource and all subresources</li>
	 * <li>the user can write the targetresource</li>
	 * <li>the sourceresource is locked by the callingUser</li>
	 * <li>the targetresource dosn't exist</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param source The complete path of the sourcefolder.
	 * @param destination The complete path of the destinationfolder.
	 * @param force If force is set to true, all sub-resources will be copied.
	 * If force is set to false, the folder will be copied only if it is empty.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void copyFolder(A_CmsProject project, String source, String destination, 
						   boolean force)
		throws CmsException {
        m_accessFile.copyFolder(project,source,destination,force);
     }
	
	 /**
	 * Moves a folder in the Cms.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * moved, too.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write this sourceresource and all subresources</li>
	 * <li>the user can write the targetresource</li>
	 * <li>the sourceresource is locked by the callingUser</li>
	 * <li>the targetresource dosn't exist</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param source The complete path of the sourcefolder.
	 * @param destination The complete path of the destinationfolder.
	 * @param force If force is set to true, all sub-resources will be moved.
	 * If force is set to false, the folder will be moved only if it is empty.
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void moveFolder(A_CmsProject project, String source, 
						   String destination, boolean force)
		throws CmsException {
        m_accessFile.moveFolder(project,source,destination,force);
     }

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
	 * @param project The project in which the resource will be used.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfolders A Vector with all subfolders for the given folder.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public Vector getSubFolders(A_CmsProject project, String foldername)
		throws CmsException{
         return m_accessFile.getSubFolders(project,foldername);
     }
	
	 /**
	 * Returns a Vector with all files of a folder.<BR/>
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read this resource</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfiles A Vector with all subfiles for the overgiven folder.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public Vector getFilesInFolder(A_CmsProject project, String foldername)
		throws CmsException {
         return m_accessFile.getFilesInFolder(project, foldername);
     }
	
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
    public boolean isWriteable(A_CmsProject project, String filename)
        throws CmsException{
        boolean isWritable=false;
        CmsFile file=null;
        
        // read the file header
        file=(CmsFile)readFileHeader(project,filename);
        // check if the write flags are set
        if ((file.getAccessFlags() & C_ACCESS_WRITE) >0) {
            isWritable=true;
        }
        return isWritable;
    }
    
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
	 * @param project The project in which the resource will be used.
	 * @param filename the complete path to the resource.
	 * 
	 * @return true, if the resource exists, else returns false.
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public boolean fileExists(A_CmsProject project, String filename)
          throws CmsException {
          boolean fileExists=false;
          CmsFile file=null;
          // read the file header
          file=(CmsFile)readFileHeader(project,filename);
          // check if the file exsits
          if (file != null) {
              fileExists=true;
          }
          return fileExists;
     }

	
     /**
	 * Changes the flags for this resource<BR/>
	 * 
	 * The user may change the flags, if he is admin of the resource.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can write the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path to the resource.
	 * @param flags The new accessflags for the resource.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void chmod(A_CmsProject project, String filename, int flags)
		throws CmsException {
        CmsResource resource = null;
        
        // check if its a file or a folder
        if (filename.endsWith("/")) {          
            //read the folder
            resource = readFolder(project,filename);
        } else {
            resource = (CmsFile)readFileHeader(project,filename);
        }
        //set the flags
        resource.setAccessFlags(flags);
        //update file
        if (filename.endsWith("/")) {   
            writeFolder(project,(CmsFolder)resource);
        } else {
            writeFileHeader(project,(CmsFile)resource);
        }
     }
	
	
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
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path to the resource.
	 * @param newOwner The new owner for this resource.
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void chown(A_CmsProject project, String filename, A_CmsUser newOwner)
		throws CmsException {
         CmsResource resource = null;
        
        // check if its a file or a folder
        if (filename.endsWith("/")) {          
            //read the folder
            resource = readFolder(project,filename);
        } else {
            resource = (CmsFile)readFileHeader(project,filename);
        }
        //set the flags
        resource.setOwnerId(newOwner.getId());
        //update file
        if (filename.endsWith("/")) {   
            writeFolder(project,(CmsFolder)resource);
        } else {
            writeFileHeader(project,(CmsFile)resource);
        }
     }

     /**
	 * Changes the group for this resource<BR/>
	 * 
	 * The user may change this, if he is admin of the resource.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user is owner of the resource</li>
	 * <li>the resource is locked by the callingUser</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete path to the resource.
	 * @param newGroup The new group for this resource.
	 * 
     * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	public void chgrp(A_CmsProject project, String filename, A_CmsGroup newGroup)
		throws CmsException{
        CmsResource resource = null;
        
        // check if its a file or a folder
        if (filename.endsWith("/")) {          
            //read the folder
            resource = readFolder(project,filename);
        } else {
            resource = (CmsFile)readFileHeader(project,filename);
        }
        //set the flags
        resource.setGroupId(newGroup.getId());
         //update file
        if (filename.endsWith("/")) {   
            writeFolder(project,(CmsFolder)resource);
        } else {
            writeFileHeader(project,(CmsFile)resource);
        }
     }

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
	 * @param user The user who wants to lock the file.
	 * @param project The project in which the resource will be used.
	 * @param resourcename The complete path to the resource to lock.
	 * @param force If force is true, a existing locking will be oberwritten.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 * It will also be thrown, if there is a existing lock
	 * and force was set to false.
	 */
	public void lockFile(A_CmsUser user,A_CmsProject project, String resourcename, boolean force)
		throws CmsException{
        CmsResource resource = null;
        
        // check if its a file or a folder
        if (resourcename.endsWith("/")) {          
            //read the folder
            resource = readFolder(project,resourcename);
        } else {
            resource = (CmsFile)readFileHeader(project,resourcename);
        }
        // check if the resource is already locked
        if (resource.isLocked()){
         // if the force switch is not set, throw an exception
            if (force==false) {
              throw new CmsException(CmsException.C_LOCKED); 
            }
        }    
        // lock the resouece
        resource.setLocked(user.getId());
        //update resource
        if (resourcename.endsWith("/")) {   
            writeFolder(project,(CmsFolder)resource);
        } else {
            writeFileHeader(project,(CmsFile)resource);
        }
     }
	
	
	/**
	 * Returns the user id, who had locked the resource.<BR/>
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
	 * @param project The project in which the resource will be used.
	 * @param resourcename The complete path to the resource.
	 * 
	 * @return The user id of the user who has locked the resource.
	 * 
	 * @exception CmsException will be thrown, if the user has not the rights 
	 * for this resource. 
	 */
	public int lockedBy(A_CmsProject project, String resourcename)
		throws CmsException {
        CmsResource resource = null;
                
        // check if its a file or a folder
        if (resourcename.endsWith("/")) {          
            //read the folder
            resource = readFolder(project,resourcename);
        } else {
            resource = (CmsFile)readFileHeader(project,resourcename);
        }
        // get the locking user
        return resource.isLockedBy();
     }

}

package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This class is the main module to access files and folders in the Cms.<BR/>
 * Depending on the URI of the requested dockument, it selects the database or filesystem
 * access module which has access to the document.
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 1999/12/23 16:47:39 $
 */
class CmsAccessFile implements I_CmsAccessFile, I_CmsConstants  {

    /**
     * Storage for all mountpoints
     */
    private Hashtable m_mountpointStorage=null;

    
    /**
     * Constructor, creates a new CmsAccessFile module.
     * 
     * @param mountpointStorage Hashtable containing all mointpoints and references to 
     * the appropriate access module.
     */
    public CmsAccessFile (Hashtable mountpointStorage) {
        m_mountpointStorage=mountpointStorage;
    }
    
    
	/**
	 * Creates a new file with the overgiven content and resourcetype.
     *
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * 
	 * @param user The user who wants to create the file.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * @param flags The flags of this resource.
	 * @param contents The contents of the new file.
	 * @param resourceType The resourceType of the new file.
	 * The keys for this Hashtable are the names for metadefinitions, the values are
	 * the values for the metainfos.
	 * 
	 * @return file The created file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful
     */
    
	 public CmsFile createFile(A_CmsUser user, A_CmsProject project,
                                String filename, int flags,
								byte[] contents, A_CmsResourceType resourceType)
        throws CmsException {
        
        return getFilesystem(filename).createFile(user,project,
                                                  filename,flags,
                                                  contents,resourceType);
        }
	
     /**
	 * Creates a new file from an given CmsFile object and a new filename.
     *
	 * <b>This method is not available for the main CmsAccess File module.</b>
	 * 
	 * 
	 * @param project The project in which the resource will be used.
	 * @param file The file to be written to the Cms.
	 * @param filename The complete nee name of the file (including pathinformation).
	 * 
	 * @return file The created file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful
     */
    
	 public CmsFile createFile(A_CmsProject project, CmsFile file,
                                String filename)
         throws CmsException {
         
         // this method is not available for the main CmsAccess File module.
         
         return null;
     }
	
     
     
	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public CmsFile readFile(A_CmsProject project, String filename)
         throws CmsException {
    
         return getFilesystem(filename).readFile(project,filename);
     }
	
	/**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent.
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public A_CmsResource readFileHeader(A_CmsProject project, String filename)
         throws CmsException {
         
         return getFilesystem(filename).readFileHeader(project,filename);
     }
	
	/**
	 * Writes a file to the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFile(A_CmsProject project, CmsFile file)
         throws CmsException {
         
         getFilesystem(file.getAbsolutePath()).writeFile(project,file);
     }
	
	/**
	 * Writes the fileheader to the Cms.
     * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	

	 public void writeFileHeader(A_CmsProject project, CmsFile file)
         throws CmsException {
         
         getFilesystem(file.getAbsolutePath()).writeFileHeader(project,file);
     }

	/**
	 * Renames the file to the new name.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */		
	 public void renameFile(A_CmsProject project, String oldname, String newname)
         throws CmsException {
         
         // get the file systems for the old and new name.
         I_CmsAccessFile oldFs=getFilesystem(oldname);
         I_CmsAccessFile newFs=getFilesystem(newname);
                
         // if both filesystems are the same, use the rename method there
        if (oldFs==newFs) {
            oldFs.renameFile(project,oldname,newname);
        } else {
            // copy the file form the old filesystem to the new one
            CmsFile file=oldFs.readFile(project,oldname);
            CmsFile newFile=newFs.createFile(project,file,newname);
            oldFs.deleteFile(project,oldname);
        }
     }
	
	/**
	 * Deletes the file.
	 * 
     * @param project The project in which the resource will be used.
	 * @param filename The complete path of the file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void deleteFile(A_CmsProject project, String filename)
         throws CmsException {
         getFilesystem(filename).deleteFile(project,filename);
     }
	
	/**
	 * Copies the file.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void copyFile(A_CmsProject project, String source, String destination)
         throws CmsException {
           // get the file systems for the source and the destination.
           I_CmsAccessFile sourceFs=getFilesystem(source);
           I_CmsAccessFile destinationFs=getFilesystem(destination);
                
          // if both filesystems are the same, use the rename method there
          if (sourceFs==destinationFs) {
             sourceFs.copyFile(project,source,destination);
        } else {
            // copy the file form the source filesystem to the destination one
            CmsFile file=sourceFs.readFile(project,source);
            CmsFile newFile=destinationFs.createFile(project,file,destination);
         }
     }
	
	/**
	 * Moves the file.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void moveFile(A_CmsProject project, String source, 
				  String destination)
         throws CmsException {
           renameFile(project,source,destination);
     }
	 
		
	/**
	 * Creates a new folder 
	 * 
	 * @param user The user who wants to create the folder.
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete path to the folder in which the new folder will 
	 * be created.
	 * @param flags The flags of this resource.
	 * 
	 * @return The created folder.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public CmsFolder createFolder(A_CmsUser user,
                                    A_CmsProject project, String foldername,
                                    int flags)
         throws CmsException {
         return getFilesystem(foldername).createFolder(user,project,foldername,flags);
     }

	/**
	 * Reads a folder from the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The name of the folder to be read.
	 * 
	 * @return The read folder.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public CmsFolder readFolder(A_CmsProject project, String foldername)
         throws CmsException {
         
         return getFilesystem(foldername).readFolder(project,foldername);
      }
	
     /**
	 * Writes a folder to the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete name of the folder (including pathinformation).
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFolder(A_CmsProject project, CmsFolder folder)
         throws CmsException {
         
         getFilesystem(folder.getAbsolutePath()).writeFolder(project,folder);
     }
     
     
	/**
	 * Renames the folder to the new name.
	 * 
	 * This is a very complex operation, because all sub-resources may be
	 * renamed, too.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource 
	 * @param force If force is set to true, all sub-resources will be renamed.
	 * If force is set to false, the folder will be renamed only if it is empty.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */		
	public void renameFolder(A_CmsProject project, String oldname, 
							   String newname, boolean force)
        throws CmsException {
    }
	
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
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void deleteFolder(A_CmsProject project, String foldername, boolean force)
         throws CmsException {
     }
	
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
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void copyFolder(A_CmsProject project, String source, String destination, 
						    boolean force)
         throws CmsException {
     }
	
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
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void moveFolder(A_CmsProject project, String source, 
						   String destination, boolean force)
         throws CmsException {
     }

	/**
	 * Returns a Vector with all subfolders.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return Vector with all subfolders for the given folder.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public Vector getSubFolders(A_CmsProject project, String foldername)
         throws CmsException {
         
         return getFilesystem(foldername).getSubFolders(project,foldername);
     }
	
	/**
	 * Returns a Vector with all file headers of a folder.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername the complete path to the folder.
	 * 
	 * @return subfiles A Vector with all file headers of the folder.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public Vector getFilesInFolder(A_CmsProject project, String foldername)
         throws CmsException {
         
         return getFilesystem(foldername).getFilesInFolder(project,foldername);
     }
     
     
     /**
      * Returns the  appropriate access module for a given file or folder.
      * 
      * @param name The name of the file or folder in the Cms.
      * @return CmsAccessFile module that can access the file or folder.
      */
     private I_CmsAccessFile getFilesystem(String name)
	{
		String mountpoint=null;
		String bestmountpoint = null;
		String bestmatch=new String("");
		I_CmsAccessFile accessFile=null;
		
        // calaculate the path to the file or folder
		String path=null;
		if (name.endsWith("/")) {
			path=name;
		} else {
			path = name.substring(0,name.lastIndexOf("/")+1);
		}
		
		// now check all available 
		Enumeration e = m_mountpointStorage.keys();
		while (e.hasMoreElements()) {
		  mountpoint=(String)e.nextElement();
          // is the path of the file covered by this mountpoint?
		  if (path.indexOf(mountpoint) != -1) {
            // is there a better mathch already existing?  
			if (mountpoint.length() > bestmatch.length()) {
                // the path must start with the moutpoint
                if(path.startsWith(mountpoint)) {
                    // set this mountpoint as the new bestmatch
					bestmatch=mountpoint;
				}
			}
		 } 
		}
	    accessFile = (I_CmsAccessFile)m_mountpointStorage.get(bestmatch);
	  return  accessFile;
	}
	
}

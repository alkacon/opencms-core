package com.opencms.file;

import java.util.*;
import java.io.*;
import java.sql.*;

import com.opencms.core.*;

/**
 * This class describes the access to files and folders in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 1999/12/22 19:12:24 $
 */
 class CmsAccessFileFilesystem implements I_CmsAccessFile, I_CmsConstants  {

    /**
    * This is the root path of this filesystem module in the local filesystem.
    */
    private String m_root  = null;
    
    /**
    * This is the mountpoint of this filesystem module.
    */
    private String m_mountpoint  = null;
    
    /**
     * The fileseperator of this filesystem.
     */
    private char m_fileseperator ='/';
  
    /**
     * Constructor, creartes a new CmsAccessFilefilesystem object.
     *
     * @param rootpath The rootpath for this filesystem access module.
     * @param mountpoint The mountpoint of this filesystem access module.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     * 
     */
    public CmsAccessFileFilesystem(String rootpath, String mountpoint)	
        throws CmsException {
        m_root=rootpath;
        m_mountpoint=mountpoint;
        //m_fileseperator=System.getProperty("file.seperator").charAt(0);
        
      }
    
	/**
	 * Creates a new file with the overgiven content and resourcetype.
     *
	 * If the resourcetype is set to folder, a CmsException will be thrown.<BR/>
	 * 
	 * @param user The user who wants to create the file.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * @param contents The contents of the new file.
	 * @param type The resourcetype of the new file.
	 * The keys for this Hashtable are the names for metadefinitions, the values are
	 * the values for the metainfos.
	 * 
	 * @return file The created file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public CmsFile createFile(A_CmsUser user, A_CmsProject project, 
                        String filename,int flags,
                        byte[] contents, A_CmsResourceType resourceType) 
							
         throws CmsException {
         
         // create new file
         File diskFile= new File(absoluteName(filename));
         // check if this file is already existing
         if (!diskFile.exists()){
             try {
                 // write the new file to disk
                 OutputStream s = new FileOutputStream(diskFile);
                 s.write(contents);
                 s.close();
             } catch (Exception e) {
               throw new CmsException(e.getMessage());
             }
         } else {
             throw new CmsException(CmsException.C_FILE_EXISTS);
         }
         return null;
     }
	
	/**
	 * Reads a file from the Cms.<BR/>
	 * 
	 *  
	 * @param callingUser The user who wants to use this method.
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public CmsFile readFile(A_CmsProject project, String filename)
         throws CmsException {
       
         return null;
     }
	
	/**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public A_CmsResource readFileHeader(A_CmsProject project, String filename)
         throws CmsException {
                 
      return null;
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
	 public void moveFile(A_CmsProject project, String source,  String destination)
         throws CmsException {   
      
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
         return null;
     }

	/**
	 * Reads a folder from the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The name of the folder to be read.
	 * 
	 * @return folder The read folder.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public CmsFolder readFolder(A_CmsProject project, String foldername)
         throws CmsException {
         
      
        return null;
        
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
		throws CmsException
     {
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
	 * Returns a abstract Vector with all subfolders.<BR/>
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
       
         return null;
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
       return null;  
     }
	       
    
   /**
	 * Calculates the absolute path to a file mounted in the filesystem
	 * accessed by this filesystem access module.
	 * 
	 * @param filename Name of a file in the OpenCms system.
	 * @return Absolute path of a the file in the disk filesystem.
	 */
     private String absoluteName(String filename) {
	   
       int pos=filename.indexOf(m_mountpoint);
	   int len=m_mountpoint.length();
	   
       String path=null;
	   String name=null;
	   
       // extract the filename after the mountpoint.
       if (pos != -1) {
		    name=filename.substring(pos+len);
	   } else {
			name=filename;
	   }
       
	   // new path is rootpath + extracted filenamen	   
	   path=m_root+name;
       
       // adjust the fileseperator
	   path=path.replace('/',m_fileseperator);
       return path;
     }
       
}

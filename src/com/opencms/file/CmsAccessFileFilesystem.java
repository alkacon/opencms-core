/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsAccessFileFilesystem.java,v $
 * Date   : $Date: 2000/04/07 15:22:16 $
 * Version: $Revision: 1.19 $
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
import java.io.*;
import java.sql.*;

import com.opencms.core.*;

/**
 * This class describes the access to files and folders in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.19 $ $Date: 2000/04/07 15:22:16 $
 */
 class CmsAccessFileFilesystem implements I_CmsAccessFile, I_CmsConstants  {
   
     
    /** The symbol to flag invisible files and folders to the system */ 
    private final static String C_INV_CHAR="#";
   
    /** The hidden projects folder */ 
    private final static String C_INV_PROJECTS="#projects/";
    
    /**
    * This is the mountpoint of this filesystem module.
    */
    private CmsMountPoint m_mountpoint  = null;
    
   
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
    public CmsAccessFileFilesystem(A_CmsMountPoint mountpoint)	
        throws CmsException {
        m_mountpoint = (CmsMountPoint) mountpoint;
    }
    
	 /**
	 * Creates a new file with the given content and resourcetype.
     *
	 * @param user The user who wants to create the file.
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * @param flags The flags of this resource.
	 * @param contents The contents of the new file.
	 * @param resourceType The resourceType of the new file.
	 * 
	 * @return file The created file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful
     */    
	 public CmsFile createFile(A_CmsUser user,
                               A_CmsProject project,
                               A_CmsProject onlineProject,
                               String filename, int flags,
							   byte[] contents, A_CmsResourceType resourceType)
							
         throws CmsException {
         
        
         // create new file              
         File diskFile= new File(absoluteName(filename,project));
         // check if this file is already existing
         if (!diskFile.exists()){
             try {
                 // write the new file to disk
                 OutputStream s = new FileOutputStream(diskFile);
                 s.write(contents);
                 s.close();
             } catch (Exception e) {
               throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage());
             }
             
             // now write the file header.
             // create a CmsFile from the given data.
             CmsFile file = new CmsFile(filename,resourceType.getResourceType(),flags,
                                        user.getId(),user.getDefaultGroupId(),project.getId(),
                                        C_ACCESS_DEFAULT_FLAGS,C_STATE_NEW,C_UNKNOWN_ID,
                                        resourceType.getLauncherType(),resourceType.getLauncherClass(),
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis(),
                                        new byte[0], contents.length);
             writeFileHeader(project,onlineProject,file,false);
                                        
             
         } else {
             throw new CmsException("[" + this.getClass().getName() + "] "+filename,CmsException.C_FILE_EXISTS);
         }
         return readFile(project,onlineProject,filename);
     }
	
     /**
	 * Creates a new file from an given CmsFile object and a new filename.
     *
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param file The file to be written to the Cms.
	 * @param filename The complete new name of the file (including pathinformation).
	 * 
	 * @return file The created file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful
     */    
	 public CmsFile createFile(A_CmsProject project,
                               A_CmsProject onlineProject,
                               CmsFile file, String filename)
         throws CmsException {
         
         // create new file                 
         File diskFile= new File(absoluteName(filename,project));
         // check if this file is already existing
         if (!diskFile.exists()){
             try {
                 // write the new file to disk
                 OutputStream s = new FileOutputStream(diskFile);
                 s.write(file.getContents());
                 s.close();                                                                 
             } catch (Exception e) {
               throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage());
             }
             
             // now write the file header.
             // create a CmsFile from the given file object
             CmsFile newfile = new CmsFile(filename,file.getType(),file.getFlags(),
                                        file.getOwnerId(),file.getGroupId(),project.getId(),
                                        file.getAccessFlags(),C_STATE_NEW,file.isLockedBy(),
                                        file.getLauncherType(),file.getLauncherClassname(),
                                        file.getDateCreated(),
                                        System.currentTimeMillis(),
                                        new byte[0], file.getLength());
             writeFileHeader(project,onlineProject,newfile,false);

        } else {
             throw new CmsException("[" + this.getClass().getName() + "] "+filename,CmsException.C_FILE_EXISTS);
         }
      
         return readFile(project,onlineProject,filename);
      
     }
     
     /**
	 * Creates a new resource from an given CmsResource object.
     *
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param resource The resource to be written to the Cms.
	 * 
	 * @return The created resource.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful
     */    
	 public A_CmsResource createResource(A_CmsProject project,
                                         A_CmsProject onlineProject,
                                         A_CmsResource resource)
         throws CmsException {
        
       return null;      
     }
     
    /**
	 * Reads a file from the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
	 * @return file The read file.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public CmsFile readFile(A_CmsProject project,
                             A_CmsProject onlineProject,
                             String filename)
         throws CmsException {
         
         // read the file header
         CmsFile file =(CmsFile)readFileHeader(project,filename);
         // check if the fileheader is not found
         if (file != null) {
             File discFile=new File(absoluteName(filename,project));
		     // check if it is a file
             if (discFile.isFile()){
                 try {
                    // read the file content form the filesystem
			        InputStream s = new FileInputStream(discFile);
				    byte[] discFileContent= new byte[(new Long(discFile.length())).intValue()];
				    int result = s.read(discFileContent);
                    // there was some content read, use it
                    if (result!=-1){
                        file.setContents(discFileContent);
                    }
				    s.close();
			   } catch (Exception e) {
                      throw new CmsException("[" + this.getClass().getName() + "] "+filename+e.getMessage());
              }
		    } 
         } else {
             throw new CmsException("[" + this.getClass().getName() + "] "+filename,CmsException.C_NOT_FOUND);
         }
		  return file;
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
	 public CmsFile readFileHeader(A_CmsProject project, String filename)
         throws CmsException {
         
         CmsFile file= null;
         // get file
         File discFile= new File (absoluteName(filename,project));
         // check if file exists in the filesystem.
         if (discFile.exists()) {
             // try to read the fileheader file. If it is not existing, get the mountpoint
             // defaults.                     
            String fileheadername=getFileHeaderName(absoluteName(filename,project));
            File discFileHeader = new File(fileheadername);
            if (discFileHeader.exists()) {
                 // read the exiting file header form disc.
                 try {
                    InputStream s = new FileInputStream(discFileHeader);
         		    byte[] discFileHeaderContent= new byte[(new Long(discFileHeader.length())).intValue()];
		            int result = s.read(discFileHeaderContent);
                    // a file header was loaded, so set it
                    if (result!=-1){
                              file=(CmsFile)setFileHeaderContent(discFileHeaderContent);
                      }
				    s.close();
			    } catch (Exception e) {
                      throw new CmsException("[" + this.getClass().getName() + "] "+filename+e.getMessage());
                }
           } else {
                // otherwise get the mountpoint defaults.
            
			    // get the correct type
			    int type;
			    if( discFile.isFile() ) {
				     type = m_mountpoint.getType();
			    } else {
				     type = C_TYPE_FOLDER;
			    }
			 
                // create file header
                file=new CmsFile(filename,
                                  type,
                                  m_mountpoint.getFlags(),
                                  m_mountpoint.getUser(),
                                  m_mountpoint.getGroup(),
                                  m_mountpoint.getProject(),
                                  //project.getId(),
                                  m_mountpoint.getAccessFlags(),
                                  C_STATE_UNCHANGED,
                                  C_UNKNOWN_ID,
                                  m_mountpoint.getLauncherId(),
                                  m_mountpoint.getLauncherClass(),
                                  discFile.lastModified(),
                                  discFile.lastModified(),
                                  new byte[0],
                                  new Long(discFile.length()).intValue());
            }
           } else {
            throw new CmsException("[" + this.getClass().getName() + "] "+filename,CmsException.C_NOT_FOUND);
           }
      return file;
     }

     
     /**
	 * Reads all file headers of a file in the OpenCms.<BR>
	 * The reading excludes the filecontent.
	 * 
     * @param filename The name of the file to be read.
	 * 
	 * @return Vector of file headers read from the Cms.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful
	 */
	 public Vector readAllFileHeaders(String filename)
         throws CmsException {
         
         Vector files=new Vector();
         // Since the files in the filesystem all belong to the same project, this method only
         // only returnes a fector which contains one or no file header. It is the same
         // file header that is returnes by the readFileHeader method.
         CmsFile file=readFileHeader(null,filename);
         if (file != null){
             files.addElement(file);
         }
         return files;
     }
	
	/**
	 * Writes a file to the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * @param changed Flag indicating if the file state must be set to changed.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFile(A_CmsProject project,
                           A_CmsProject onlineProject,
                           CmsFile file,boolean changed)
       throws CmsException {
          
          // create new file
         File diskFile= new File(absoluteName(file.getAbsolutePath(),project));
         // check if this file is already existing
         if (diskFile.exists()){
             try {
                 // write the new file to disk
                 OutputStream s = new FileOutputStream(diskFile);
                 s.write(file.getContents());
                 s.close();
             } catch (Exception e) {
               throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage());
             }
             // now write the file header
             writeFileHeader(project,onlineProject,file,changed);
         } else {
             throw new CmsException("[" + this.getClass().getName() + "] "+file.getAbsolutePath(),CmsException.C_FILE_EXISTS);
         }
     }
	
	 /**
	 * Writes the fileheader to the Cms.
     * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * @param changed Flag indicating if the file state must be set to changed.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFileHeader(A_CmsProject project,
                                 A_CmsProject onlineProject,
                                 CmsFile file,boolean changed)
         throws CmsException {
          
         String filename=absoluteName(file.getAbsolutePath(),project);
         String fileheadername=getFileHeaderName(filename);
         CmsFile fileheader=(CmsFile)file.clone();
         fileheader.setContents(new byte[0]);
         
         // set the correct file state
         int state=file.getState();
         if ((state == C_STATE_NEW) || (state == C_STATE_CHANGED)) {
            fileheader.setState(state);
         } else {                                                                       
            if (changed==true) {
                fileheader.setState(C_STATE_CHANGED);
             } else {
                fileheader.setState(state);
             }
         }
         
         byte[] value=getFileHeaderContent(fileheader);
        
         // create the file header
         File diskFile= new File(fileheadername);
        
         try {
            // write the new file to disk
            OutputStream s = new FileOutputStream(diskFile);
            s.write(value);
            s.close();
         } catch (Exception e) {
               throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage());
         }
     }
     
	/**
	 * Renames the file to the new name.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */		
	 public void renameFile(A_CmsProject project,
                            A_CmsProject onlineProject,
                            String oldname, String newname)
         throws CmsException {
                      
           	File discFile=new File(absoluteName(oldname,project));
		            
			// check if file exists
            if (discFile.exists())	{
                // check if it is really a file
                if (discFile.isFile()) {
                    // create a new file with the new name
                    CmsFile file=readFile(project,onlineProject,oldname);
                    createFile(project,onlineProject,file,newname);
                    // now either delete or remove the odl file, depending if the old file
                    // is already in the onlineproject or not
                    try {
                        readFileHeader(onlineProject,oldname);
                        deleteFile(project,oldname);
                    } catch (CmsException e) {
                        removeFile(project,oldname);
                    }
             
                } else {
                    throw new CmsException("[" + this.getClass().getName() + "] "+oldname,CmsException.C_NOT_FOUND);
                }
            } else {
                  throw new CmsException("[" + this.getClass().getName() + "] "+oldname,CmsException.C_NOT_FOUND);
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

         // modifiy the state of the original file
         CmsFile file=readFileHeader(project,filename);
         file.setState(C_STATE_DELETED);
         
         // now update the file header
         String fileheadername=getFileHeaderName(absoluteName(filename,project));
         CmsFile fileheader=(CmsFile)file.clone();
         fileheader.setContents(new byte[0]);
         
         byte[] value=getFileHeaderContent(fileheader);
        
         // create the file header
         File diskFile= new File(fileheadername);
        
         try {
            // write the new fileheader to disk
            OutputStream s = new FileOutputStream(diskFile);
            s.write(value);
            s.close();
         } catch (Exception e) {
               throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage());
         }
         
         
     }
	
     /**
	 * Undeletes the file. <br>
	 * This function is not possible in the filesystem yet.
	 * 
     * @param project The project in which the resource will be used.
	 * @param filename The complete path of the file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void undeleteFile(A_CmsProject project, String filename)
         throws CmsException {
         // this function is not possible, so throw an excection 
         throw new CmsException("[" + this.getClass().getName() + "] "+filename,CmsException.C_FILESYSTEM_ERROR);
     }
     
     
  
     /**
      * Deletes a file in the filesytem. 
      * 
      * @param project The project in which the resource will be used.
	  * @param filename The complete path of the file.
      * @exception CmsException Throws CmsException if operation was not succesful
      */
     public void removeFile(A_CmsProject project, String filename) 
        throws CmsException{
         
       String fileheadername=getFileHeaderName(absoluteName(filename,project));
         
         File discFile=new File(absoluteName(filename,project));
         File discFileHeader = new File(fileheadername);
		 // check if file exists
         if (discFile.exists()){
             // it is really a file
             if(discFile.isFile()) {
				boolean success=discFile.delete();
				if (!success) {
						 throw new CmsException("[" + this.getClass().getName() + "] "+filename,CmsException.C_FILESYSTEM_ERROR);
				}
                // check if there is a file header for this file existing
                if (discFileHeader.exists()) {
                    success=discFileHeader.delete();
				    if (!success) {
					   	 throw new CmsException("[" + this.getClass().getName() + "] "+filename,CmsException.C_FILESYSTEM_ERROR);
				    }
                }
                
             } else {
                 throw new CmsException("[" + this.getClass().getName() + "] "+filename,CmsException.C_NOT_FOUND);
             }
         } 	else {
             throw new CmsException("[" + this.getClass().getName() + "] "+filename,CmsException.C_NOT_FOUND);
         }
     }
     
	 /**
	 * Copies the file.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param source The complete path of the sourcefile.
	 * @param destination The complete path of the destinationfile.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void copyFile(A_CmsProject project,
                          A_CmsProject onlineProject,
                          String source, String destination)
         throws CmsException {
         
         // read the source file
         CmsFile file=readFile(project,onlineProject,source);
         // write the destination file
         File diskFile= new File(absoluteName(destination,project));
         // check if this file is already existing
         if (!diskFile.exists()){
             try {
                 // write the new file to disk
                 OutputStream s = new FileOutputStream(diskFile);
                 s.write(file.getContents());
                 s.close();
             } catch (Exception e) {
               throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage());
             }
             // now write the file header.
             // create a CmsFile from the given file object
             CmsFile newfile = new CmsFile(destination,file.getType(),file.getFlags(),
                                        file.getOwnerId(),file.getGroupId(),project.getId(),
                                        file.getAccessFlags(),C_STATE_NEW,file.isLockedBy(),
                                        file.getLauncherType(),file.getLauncherClassname(),
                                        file.getDateCreated(),
                                        System.currentTimeMillis(),
                                        new byte[0], file.getLength());
             writeFileHeader(project,onlineProject,newfile,false);
             
         } else {
             throw new CmsException("[" + this.getClass().getName() + "] "+destination,CmsException.C_FILE_EXISTS);
         }
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
                                   A_CmsProject project, 
                                   String foldername,
                                   int flags)
         throws CmsException {

          // create folder
		  File discFolder=new File(absoluteName(foldername,project));
          // check if this folder already exits
		  if (!discFolder.exists())	{
			boolean success=discFolder.mkdir();
			if (!success) {
				throw new CmsException("[" + this.getClass().getName() + "] "+foldername,CmsException.C_FILESYSTEM_ERROR);
			}
            
            String folderheadername=getFileHeaderName(absoluteName(foldername,project));               
            CmsFolder folder=new CmsFolder(foldername,C_TYPE_FOLDER,flags,user.getId(),
                                           user.getDefaultGroup().getId(),project.getId(),
                                           C_ACCESS_DEFAULT_FLAGS,C_STATE_NEW,
                                           C_UNKNOWN_ID,
                                           System.currentTimeMillis(),System.currentTimeMillis());
          
            byte[] value=getFileHeaderContent(folder);

            // create the file header
            File diskFile= new File(folderheadername);
        
            try {
                // write the new file to disk
                OutputStream s = new FileOutputStream(diskFile);
                s.write(value);
                s.close();
            } catch (Exception e) {
                   throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage());
            }
            
            
          } else {
              throw new CmsException("[" + this.getClass().getName() + "] "+foldername,CmsException.C_FILE_EXISTS);
          }
        return readFolder(project,foldername);
     }

     /**
	 * Creates a new folder from an existing folder object.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param folder The folder to be written to the Cms.
	 * @param foldername The complete path of the new name of this folder.
	 * 
	 * @return The created folder.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public CmsFolder createFolder(A_CmsProject project,
                                   CmsFolder folder,
                                   String foldername)
         throws CmsException {
        
         // create folder
		  File discFolder=new File(absoluteName(foldername,project));
          // check if this folder already exits
		  if (!discFolder.exists())	{
            boolean success=discFolder.mkdir();
			if (!success) {
				throw new CmsException("[" + this.getClass().getName() + "] "+foldername,CmsException.C_FILESYSTEM_ERROR);
			}		
            
            String folderheadername=getFileHeaderName(absoluteName(foldername,project));               
            CmsFolder newfolder=new CmsFolder(foldername,C_TYPE_FOLDER,folder.getFlags(),
                                           folder.getOwnerId(),folder.getGroupId(),
                                           project.getId(),folder.getAccessFlags(),
                                           C_STATE_NEW, C_UNKNOWN_ID,
                                           System.currentTimeMillis(),System.currentTimeMillis());
          
            byte[] value=getFileHeaderContent(newfolder);
        
            // create the file header
            File diskFile= new File(folderheadername);
        
            try {
                // write the new file to disk
                OutputStream s = new FileOutputStream(diskFile);
                s.write(value);
                s.close();
            } catch (Exception e) {
                   throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage());
            }
            
            
          } else {
            if (foldername.equals(m_mountpoint.getMountpoint())) {
                String folderheadername=getFileHeaderName(absoluteName(foldername,project));               
                CmsFolder newfolder=new CmsFolder(foldername,C_TYPE_FOLDER,folder.getFlags(),
                                                  folder.getOwnerId(),folder.getGroupId(),
                                                  project.getId(),folder.getAccessFlags(),
                                                  C_STATE_NEW, C_UNKNOWN_ID,
                                                  System.currentTimeMillis(),System.currentTimeMillis());
          
                byte[] value=getFileHeaderContent(newfolder);
        
                // create the file header
                File diskFile= new File(folderheadername);
        
                try {
                    // write the new file to disk
                    OutputStream s = new FileOutputStream(diskFile);
                    s.write(value);
                    s.close();
                } catch (Exception e) {
                       throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage());
                }
            
             } else {
                throw new CmsException("[" + this.getClass().getName() + "] "+foldername,CmsException.C_FILE_EXISTS);
             }
          }
        return readFolder(project,foldername);
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
 	     
	     CmsFolder folder = null;
        
	     File discFolder=new File(absoluteName(foldername,project));
         if (discFolder != null) {
	   	   if (discFolder.exists() && discFolder.isDirectory()){
             // try to read the folderheader file. If it is not existing, get the mountpoint
             // defaults.                     
            String folderheadername=getFileHeaderName(absoluteName(foldername,project));
            File discFileHeader = new File(folderheadername);
            if (discFileHeader.exists()) {
                 // read the exiting file header form disc.
                 try {
                    InputStream s = new FileInputStream(discFileHeader);
         		    byte[] discFileHeaderContent= new byte[(new Long(discFileHeader.length())).intValue()];
		            int result = s.read(discFileHeaderContent);
                    // a file header was loaded, so set it
                    if (result!=-1){
                              folder=(CmsFolder)setFileHeaderContent(discFileHeaderContent);
                      }
				    s.close();
			    } catch (Exception e) {
                      throw new CmsException("[" + this.getClass().getName() + "] "+foldername+e.getMessage());
                }
            } else {
               // get the mountpoint defaults                             
			   folder = new CmsFolder(foldername,
                                      C_TYPE_FOLDER,
                                      m_mountpoint.getFlags(),
                                      m_mountpoint.getUser(),
                                      m_mountpoint.getGroup(),
                                      m_mountpoint.getProject(),
                                      //project.getId(),
                                      m_mountpoint.getAccessFlags(),
                                      C_STATE_UNCHANGED,
                                      C_UNKNOWN_ID,
                                      discFolder.lastModified(),
                                      discFolder.lastModified());
            }
		   } else {
             throw new CmsException("[" + this.getClass().getName() + "] "+foldername,CmsException.C_NOT_FOUND);
           }
	     } else {
             throw new CmsException("[" + this.getClass().getName() + "] "+foldername,CmsException.C_NOT_FOUND);
         }
	   return folder;       
    }
	
     	
     /**
	 * Writes a folder to the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete name of the folder (including pathinformation).
     * @param changed Flag indicating if the file state must be set to changed.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFolder(A_CmsProject project, CmsFolder folder,
                             boolean changed)
         throws CmsException {
       
          String foldername=absoluteName(folder.getAbsolutePath(),project);
          String folderheadername=getFileHeaderName(foldername);
        
          // set the correct folder state
          int state=folder.getState();
          if ((state == C_STATE_NEW) || (state == C_STATE_CHANGED)) {
            folder.setState(state);
          } else {                                                                       
            if (changed==true) {
                folder.setState(C_STATE_CHANGED);
             } else {
                folder.setState(state);
             }
         }
          
         byte[] value=getFileHeaderContent(folder);
        
         // create the folder header
         File diskFile= new File(folderheadername);
        
         try {
            // write the new file to disk
            OutputStream s = new FileOutputStream(diskFile);
            s.write(value);
            s.close();
         } catch (Exception e) {
               throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage());
         }
          
     }

	
	/**
	 * Deletes the folder.
	 * 
	 * Only empty folders can be deleted yet.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete path of the folder.
	 * @param force If force is set to true, all sub-resources will be deleted.
	 * If force is set to false, the folder will be deleted only if it is empty.
	 * This parameter is not used yet as only empty folders can be deleted!
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void deleteFolder(A_CmsProject project, String foldername, boolean force)
         throws CmsException {
      	 
          // modifiy the state of the original folder
          CmsFolder folder=readFolder(project,foldername);
          folder.setState(C_STATE_DELETED);
         
         // now update the folder header
         String folderheadername=getFileHeaderName(absoluteName(foldername,project));
        
         byte[] value=getFileHeaderContent(folder);
        
         // create the folder header
         File diskFile= new File(folderheadername);
        
         try {
            // write the new fileheader to disk
            OutputStream s = new FileOutputStream(diskFile);
            s.write(value);
            s.close();
         } catch (Exception e) {
               throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage());
         }
     }
	
      /**
      * Deletes a folder in the database. 
      * This method is used to physically remove a folder form the database.
      * 
      * @param project The project in which the resource will be used.
	  * @param foldername The complete path of the folder.
      * @exception CmsException Throws CmsException if operation was not succesful
      */
     public void removeFolder(A_CmsProject project, String foldername) 
        throws CmsException{
         
        String folderheadername=getFileHeaderName(absoluteName(foldername,project));
         
  
        File discFolder=new File(absoluteName(foldername,project));
        File discFolderHeader = new File(folderheadername);
		// check if folder exists	
        if (discFolder.exists()) {
            // it is really a folder
			if (discFolder.isDirectory()) {
			    boolean success=discFolder.delete();
				if (!success) {
					throw new CmsException("[" + this.getClass().getName() + "] "+foldername, CmsException.C_FILESYSTEM_ERROR);
				}
                // check if there is a folder header for this folder existing
                if (discFolderHeader.exists()) {
                    success=discFolderHeader.delete();
				    if (!success) {
					   	 throw new CmsException("[" + this.getClass().getName() + "] "+foldername,CmsException.C_FILESYSTEM_ERROR);
				    }
                }
			}
		}
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
         
         CmsFolder folder=null;
         Vector v =new Vector();
         // get folder
		 File diskFolder= new File(absoluteName(foldername,project));		
		 
         if (diskFolder.exists()) {
            // get a list of all sub folders, sort them alphabetically.
            String[] diskFolders=SortEntrys(diskFolder.list());	
	
            // create the subfolder objects.
		    for (int i = 0; i < diskFolders.length; i++) {
    			File diskSubFolder=new File(absoluteName(foldername+diskFolders[i],project));
	    			// check if it is a folder.  
                    if (diskSubFolder.isDirectory()) {
                        folder = readFolder(project, foldername+diskFolders[i]+"/");
                        if (!folder.getName().startsWith(C_INV_CHAR)) {
                            v.addElement(folder);
                        }
                    }
			    }
         }
       return v;
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
         
         CmsFile file=null;
          Vector v =new Vector();

          // get folder
		  File diskFolder= new File(absoluteName(foldername,project));	
          
          if (diskFolder.exists()) { 
    	    // get a list of all files, sort them alphabetically.
            String[] diskFiles=SortEntrys(diskFolder.list());				
				
            for (int i = 0; i < diskFiles.length; i++) {
			    File diskFile=new File(absoluteName(foldername+diskFiles[i],project));
			    // check if it is a file
                if (diskFile.isFile()) {
				     file=(CmsFile)readFileHeader(project,foldername+diskFiles[i]);
                    if (!file.getName().startsWith(C_INV_CHAR)) {
			            v.addElement(file);					   
                    }
			    }
		    }
          }
		return v;		
     }
	    
     /**
     * Copies a resource from the online project to a new, specified project.<br>
     *
     * @param project The project to be published.
	 * @param onlineProject The online project of the OpenCms.
	 * @param resource The name of the resource.
 	 * @exception CmsException  Throws CmsException if operation was not succesful.
     */
     public void copyResourceToProject(A_CmsProject project,
                                       A_CmsProject onlineProject,
                                       String resourcename) 
         throws CmsException {

         // check if the resource is a file or a folder
         if (resourcename.endsWith("/")) {
             // this is a folder
             CmsFolder folder=readFolder(onlineProject,resourcename);         
             checkProjectFolder(project,onlineProject);
             folder=createFolder(project,folder,resourcename);                 
             folder.setState(C_STATE_UNCHANGED);    
             writeFolder(project,folder,false);
         } else {
             // this is a file
             CmsFile file=readFile(onlineProject,onlineProject,resourcename);
             checkProjectFolder(project,onlineProject);
             file=createFile(project,onlineProject,file,resourcename);               
             file.setState(C_STATE_UNCHANGED);
             writeFileHeader(project,onlineProject,file,false);
         }
     }
     
     /**
     * Publishes a specified project to the online project. <br>
     * 
     * Publishing in the filesytem is done on four steps:
     * <ul>
     * <li>Create all new folders and modifiy existing ones.</li>
     * <li>Create all new files and modify existing ones. </li>
     * <li>Delete all files that are marked as deleted. </li>
     * <li>Delete all folders that are marked as deleted. </li>
     *
     * @param project The project to be published.
	 * @param onlineProject The online project of the OpenCms.
	 * @return Vector of all resource names that are published.
     * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector publishProject(A_CmsProject project, A_CmsProject onlineProject)
        throws CmsException {
        
        // get the root of this mountpoint
        String rootFolder=m_mountpoint.getMountpoint();
        // Vectors to store all files and folders
        Vector allFiles=new Vector();
        Vector allFolders=new Vector();
        
        Vector resources= new Vector();
        
        getAllResources(rootFolder,project,allFiles,allFolders);

        // Step 1:
        // Now check all found folders and publish the new or changed ones.
        for (int i=0;i<allFolders.size();i++) {
            // get the next folder
            CmsFolder offlineFolder = (CmsFolder)allFolders.elementAt(i);
            
            // test if this folder is changed or new
            if ((offlineFolder.getState() == C_STATE_CHANGED) ||
                (offlineFolder.getState() == C_STATE_NEW)){
            
                // create a new folder fo the online project
                CmsFolder onlineFolder = new CmsFolder(offlineFolder.getAbsolutePath(),
                                                       offlineFolder.getType(),
                                                       offlineFolder.getFlags(),
                                                       offlineFolder.getOwnerId(),
                                                       offlineFolder.getGroupId(),
                                                       onlineProject.getId(),
                                                       offlineFolder.getAccessFlags(),
                                                       offlineFolder.getState(),
                                                       offlineFolder.isLockedBy(),
                                                       offlineFolder.getDateCreated(),
                                                       offlineFolder.getDateLastModified());
                // remove a lock if nescessary. This is a temporrary fix, this has to be
                // done in the resource broker.
                onlineFolder.setLocked(C_UNKNOWN_ID);
                // an exisiting folder was changed
                if (onlineFolder.getState() == C_STATE_CHANGED) {
                    onlineFolder.setState(C_STATE_UNCHANGED);
                    writeFolder(onlineProject,onlineFolder,false);                      
                } else {
                   // this is a new folder
                    onlineFolder=createFolder(onlineProject,onlineFolder,onlineFolder.getAbsolutePath());
                    onlineFolder.setState(C_STATE_UNCHANGED);
                    writeFolder(onlineProject,onlineFolder,false);     
                }
                resources.addElement(onlineFolder.getAbsolutePath());
            }
        }
            
            
        // Step 2:
        // Now check all found files and publish the new or changed ones.
        for (int i=0;i<allFiles.size();i++) {
            // get the next files
            CmsFile offlineFile = (CmsFile)allFiles.elementAt(i);
            
            // test if this files is changed or new
            if ((offlineFile.getState() == C_STATE_CHANGED) ||
                (offlineFile.getState() == C_STATE_NEW)){

                    CmsFile onlineFile= new CmsFile(offlineFile.getAbsolutePath(),
                                                    offlineFile.getType(),
                                                    offlineFile.getFlags(),
                                                    offlineFile.getOwnerId(),
                                                    offlineFile.getGroupId(),
                                                    onlineProject.getId(),
                                                    offlineFile.getAccessFlags(),
                                                    offlineFile.getState(),
                                                    offlineFile.isLockedBy(),
                                                    offlineFile.getLauncherType(),
                                                    offlineFile.getLauncherClassname(),
                                                    offlineFile.getDateCreated(),
                                                    offlineFile.getDateLastModified(),
                                                    readFile(project,onlineProject,offlineFile.getAbsolutePath()).getContents(),
                                                    offlineFile.getLength());
            
                    // remove a lock if nescessary. This is a temporrary fix, this has to be
                    // done in the resource broker.
                    onlineFile.setLocked(C_UNKNOWN_ID);
                    // an exisiting file was changed
                    if (onlineFile.getState() == C_STATE_CHANGED) {                   
                        onlineFile.setState(C_STATE_UNCHANGED);   
                        writeFile(onlineProject,onlineProject,onlineFile,false); 
                    } else {
                        onlineFile=createFile(onlineProject,onlineProject,onlineFile,onlineFile.getAbsolutePath());
                        onlineFile.setState(C_STATE_UNCHANGED);
                        writeFileHeader(onlineProject,onlineProject,onlineFile,false);
                    }
                    resources.addElement(onlineFile.getAbsolutePath());
            }                      
        }
        
        // Step 3:
        // Now check all found files and delete those marked as deleted
        for (int i=0;i<allFiles.size();i++) {
            // get the next files
            CmsFile offlineFile = (CmsFile)allFiles.elementAt(i);
            
            // test if this file is marked as deleted
            if ((offlineFile.getState() == C_STATE_DELETED)) {

                    CmsFile onlineFile= new CmsFile(offlineFile.getAbsolutePath(),
                                                    offlineFile.getType(),
                                                    offlineFile.getFlags(),
                                                    offlineFile.getOwnerId(),
                                                    offlineFile.getGroupId(),
                                                    onlineProject.getId(),
                                                    offlineFile.getAccessFlags(),
                                                    offlineFile.getState(),
                                                    offlineFile.isLockedBy(),
                                                    offlineFile.getLauncherType(),
                                                    offlineFile.getLauncherClassname(),
                                                    offlineFile.getDateCreated(),
                                                    offlineFile.getDateLastModified(),
                                                    readFile(project,onlineProject,offlineFile.getAbsolutePath()).getContents(),
                                                    offlineFile.getLength());
            
                    // remove a lock if nescessary. This is a temporrary fix, this has to be
                    // done in the resource broker.
                    onlineFile.setLocked(C_UNKNOWN_ID);
                    // delete the file in the online project
                    removeFile(onlineProject,onlineFile.getAbsolutePath());
                    resources.addElement(onlineFile.getAbsolutePath());
            }
        }
        
        // Step 4:
        // Now check all found folders and delete those marked as deleted
        for (int i=0;i<allFolders.size();i++) {
            // get the next folder
            CmsFolder offlineFolder = (CmsFolder)allFolders.elementAt(allFolders.size()-i-1);
            
            // test if this folder is marked as deleted
            if ((offlineFolder.getState() == C_STATE_DELETED)) {
            
                // create a new folder fo the online project
                CmsFolder onlineFolder = new CmsFolder(offlineFolder.getAbsolutePath(),
                                                       offlineFolder.getType(),
                                                       offlineFolder.getFlags(),
                                                       offlineFolder.getOwnerId(),
                                                       offlineFolder.getGroupId(),
                                                       onlineProject.getId(),
                                                       offlineFolder.getAccessFlags(),
                                                       offlineFolder.getState(),
                                                       offlineFolder.isLockedBy(),
                                                       offlineFolder.getDateCreated(),
                                                       offlineFolder.getDateLastModified());
                // remove a lock if nescessary. This is a temporrary fix, this has to be
                // done in the resource broker.
                onlineFolder.setLocked(C_UNKNOWN_ID);
                // an exisiting folder is marked as deleted
                removeFolder(onlineProject,onlineFolder.getAbsolutePath());
               
                resources.addElement(onlineFolder.getAbsolutePath());
            }
        }

        return resources;
    }
    
    
    /**
     * Gets all resources - files and subfolders - of a given folder.
     * @param rootFolder The name of the given folder.
     * @param project The current project.
     * @param allFiles Vector containing all files found so far. All files of this folder
     * will be added here as well.
     * @param allolders Vector containing all folders found so far. All subfolders of this folder
     * will be added here as well.
     */
    private void getAllResources(String rootFolder, A_CmsProject project,
                                 Vector allFiles, Vector allFolders) 
     throws CmsException {
        Vector folders=new Vector();
        Vector files=new Vector();
        
        // get files and folders of this rootFolder
        folders=getSubFolders(project,rootFolder);
        files=getFilesInFolder(project,rootFolder);

        
        
        //copy the values into the allFiles and allFolders Vectors
        for (int i=0;i<folders.size();i++) {
            allFolders.addElement((CmsFolder)folders.elementAt(i));
            getAllResources(((CmsFolder)folders.elementAt(i)).getAbsolutePath(),project,
                            allFiles,allFolders);
        }
        for (int i=0;i<files.size();i++) {
            allFiles.addElement((CmsFile)files.elementAt(i));
        } 
        
        
    }
    
    
    
    
   /**
	 * Calculates the absolute path to a file mounted in the filesystem
	 * accessed by this filesystem access module.
	 * 
	 * @param filename Name of a file in the OpenCms system.
	 * @return Absolute path of a the file in the disk filesystem.
	 */
     private String absoluteName(String filename,A_CmsProject project) {
	   
       int pos=filename.indexOf(m_mountpoint.getMountpoint());
	   int len=m_mountpoint.getMountpoint().length();
	   
      
       String path=null;
	   String name=null;
	   
       // extract the filename after the mountpoint.
       if (pos != -1) {
		    name=filename.substring(pos+len);
	   } else {
			name=filename;
	   }
       
       // adjust the resource name to the project
       if (!project.getName().equals(C_PROJECT_ONLINE)) {
            name=C_INV_PROJECTS+C_INV_CHAR+project.getId()+"/"+name;           
       }
       
	   // new path is rootpath + extracted filename
	   path=m_mountpoint.getMountPath()+name;
       // adjust the fileseperator
	   path=path.replace('/',m_fileseperator);
       getFileHeaderName(path);
       
       return path;
     }

      
     /**
      * Checks if the requires project folder si already available and creates this
      * folder if newscessary.
      * @param project The current project.
      * @param onlineProject The online project.
      */
     private void checkProjectFolder(A_CmsProject project, A_CmsProject onlineProject) 
        throws CmsException{
         // this is the name of the project folder
         String foldername=C_INV_PROJECTS+C_INV_CHAR+project.getId()+"/";      
         CmsFolder folder=null;
         boolean success=true;
         // try to read the project folder
         try {             
            folder=readFolder(onlineProject,foldername);   
         } catch (CmsException ex) {             
            // this failed, so create a new project folder.
            File projectFolder=new File(absoluteName(C_INV_PROJECTS+"/",onlineProject));
            File discFolder=new File(absoluteName(foldername,onlineProject));
            if (!projectFolder.exists()) {
                success=projectFolder.mkdir();
            }           
            if (!discFolder.exists())	{
			    success=discFolder.mkdir();
            }
         }
     }    
     
     /** 
      * Generates the name of a fileheader for a filesystem file.
      * Since files in the filesystem cannot store the information of a file header directly,
      * an invisible file is created to store all file header information.
      * This fileheader file has the same filename as the original file, the only difference
      * is a leading '#' in the filename.
      * @param filename The name of the file
      * @return Filename of the fileheader, including the leading '#'.
      */
     private String getFileHeaderName(String filename) {

         String fileheadername=null;
         
         // get the symbol for file headers
         String marker=C_INV_CHAR;
         
         // find the position where to add the # symbol
         if (filename.endsWith("/")) {
             filename=filename.substring(0,filename.length()-1);
             marker+=C_INV_CHAR;
         }
      
         int pos=filename.lastIndexOf('/');      
         fileheadername=filename.substring(0,pos+1)+marker+filename.substring(pos+1);
         return fileheadername;
     }
     
       
     /**
	 * Sorts a list of files or folders alphabetically. 
	 * This method uses an insertion sort algorithem.
	 * 
	 * @param unsortedList Array of strings containing the list of files or folders.
	 * @return Array of sorted strings.
	 */
	private String[] SortEntrys(String[] unsortedList) {
		int in,out;
		int nElem = unsortedList.length;
		
 		for(out=1; out < nElem; out++) {
			String temp= unsortedList[out];
			in = out;
			while (in >0 && unsortedList[in-1].compareTo(temp) >= 0){
				unsortedList[in]=unsortedList[in-1];
				--in;
			}
			unsortedList[in]=temp;
		}
		return unsortedList;
	}
    
    
    /**
     * Gets the fileheader content and transform it, so that it can be stored in the
     * filesystem. <br>
     * 
     * The currrent implemnetation serializes the fileheader.
     * 
     * @param fileheader The fileheader of the current file.
     * @return Array of bytes containing the transformed fileheader.
     * @exception Throws CmsException if something goes wrong.
     */
    private byte[] getFileHeaderContent(CmsResource fileheader)
    throws CmsException{
        byte[] value=null;     
        try {
            // serialize the object
            ByteArrayOutputStream bout= new ByteArrayOutputStream();            
            ObjectOutputStream oout=new ObjectOutputStream(bout);
            oout.writeObject(fileheader);
            oout.close();
            value=bout.toByteArray();
        } catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
		}
        return value;
    }
    
     /**
     * Sets the fileheader content by re-transform the stored data from the filesystem. <br>
     * 
     * The currrent implemnetation serializes the fileheader.
     * 
     * @param content Array of bytes containing the transformed fileheader.
     * @return Fileheader of the actual files
     * @exception Throws CmsException if something goes wrong.
     */
    private CmsResource setFileHeaderContent(byte[] content)
    throws CmsException{
       CmsResource header=null;     
        try {
           // deserialize the object
           ByteArrayInputStream bin= new ByteArrayInputStream(content);
           ObjectInputStream oin = new ObjectInputStream(bin);       
           header=(CmsResource)oin.readObject();     
        } catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			        
		}
        catch (ClassNotFoundException e){
            throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);			
          
		}	
        return header;
    }
    
}

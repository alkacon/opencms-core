/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRbFileCache.java,v $
 * Date   : $Date: 2000/04/07 15:57:37 $
 * Version: $Revision: 1.13 $
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

import com.opencms.core.*;

/**
 * This  class extends the resource broker for files and folders in 
 * the Cms.<BR/>
 * 
 * It provides a caching algorithem for file access methods.
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.13 $ $Date: 2000/04/07 15:57:37 $
 */
 class CmsRbFileCache extends CmsRbFile {
     
     /** The filecache */
     private CmsCache m_filecache=null;
     
     /** The file size */
     private final static int C_FILECACHE=200;
     
     /** The maximum filesize */
     private final static int C_MAXFILESIZE=350000;
     
     private final static String C_FILE="FILE";
     
     private final static String C_FOLDER="FOLDER";
     
     /**
     * Constructor, creates a new File Resource Broker.
     * 
     * @param accessFile The file access object.
     */
    public CmsRbFileCache(I_CmsAccessFile accessFile) {
        super(accessFile);
        m_filecache=new CmsCache(C_FILECACHE);
    }
	
     /**
	 * Reads a file header from the Cms.<BR/>
	 * The reading excludes the filecontent. <br>
	 * 
	 * A file header can be read from an offline project or the online project.
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
         A_CmsResource res=null;
         String key=null;
         if (filename.endsWith("/")) {
            key=C_FOLDER+project.getId()+filename;
            res=m_accessFile.readFolder(project,filename);
            m_filecache.remove(key);
        } else {
            key=C_FILE+project.getId()+filename;
            // check if resource is available in cache
            res=(A_CmsResource)m_filecache.get(key);
             // not found in cache, so get it from the database and add it to cache
            if (res == null) {
             res=m_accessFile.readFileHeader(project,filename);
             m_filecache.put(key,res);
            } 
         }
       return res;
     }
       
     
     /**
	 * Writes the fileheader to the Cms.<br>
	 * 
	 * A file header can only be written to an offline project.<br>
	 * The state of the resource is set to  CHANGED (1). If the file content is not
	 * exisiting in the offline project, it is read from the online project and written
	 * to the offline project as well. This is nescessary because all files in an 
	 * offline project with the state CHANGED (1) must have an existing file content in 
	 * the offline project as well. <br>
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
	 * @param onlineProject The online project of the OpenCms.
	 * @param file The file to write the header of.
	 * @param changed Flag indicating if the file state must be set to changed.
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void writeFileHeader(A_CmsProject project, 
                                A_CmsProject onlineProject,
                                CmsFile file,boolean changed)
		throws CmsException{
           String key= null;
           if (file.isFolder()) {
                key=C_FOLDER+project.getId()+file.getAbsolutePath();
            } else {
                key=C_FILE+project.getId()+file.getAbsolutePath();
            } 
            m_filecache.remove(key);
            m_accessFile.writeFileHeader(project,onlineProject,file,changed);
        }
    
    	 /**
	 * Writes a file to the Cms.<br>
	 * 
	 * A file can only be written to an offline project.<br>
	 * The state of the resource is set to  CHANGED (1). The file content of the file
	 * is either updated (if it is already existing in the offline project), or created
	 * in the offline project (if it is not available there).<br>
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
	 * @param onlineProject The online project of the OpenCms.
	 * @param file The file to write.
	 * @param changed Flag indicating if the file state must be set to changed.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void writeFile(A_CmsProject project,
                          A_CmsProject onlineProject,
                          CmsFile file, boolean changed)
		throws CmsException{        
        String key= null;
        if (file.isFolder()) {
            key=C_FOLDER+project.getId()+file.getAbsolutePath();
        } else {
            key=C_FILE+project.getId()+file.getAbsolutePath();
        } 
        m_filecache.remove(key);
        m_accessFile.writeFile(project,onlineProject,file,changed);
     }
    
    
     /**
	 * Deletes a file in the Cms.<br>
	 *
     * A file can only be deleted in an offline project. 
     * A file is deleted by setting its state to DELETED (3). <br> 
     * 
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
         String key=null;
          if (filename.endsWith("/")) {
            key=C_FOLDER+project.getId()+filename;
         } else {
            key=C_FILE+project.getId()+filename;
         } 
        // delete file in cache
        m_accessFile.deleteFile(project,filename);
        m_filecache.remove(key);
     }
    
     /**
	 * Removes a file in the Cms.<br>
	 *
     * In difference to the deleteFile method, the given file is physically removed <br> 
     * 
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
	 * @param filename The complete path of the file.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void removeFile(A_CmsProject project, String filename)
		throws CmsException{
         String key=null;
         if (filename.endsWith("/")) {
            key=C_FOLDER+project.getId()+filename;
         } else {
            key=C_FILE+project.getId()+filename;
         } 
        // delete file in cache
        m_accessFile.removeFile(project,filename);
        m_filecache.remove(key);
     }
     
    
	/**
	 * Reads a folder from the Cms.<br>
	 * 
	 * A folder can be either read from an offline Project or the online project.
	 * By reading a folder, its state is not changed. <br>
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
         CmsFolder res=null;
         String key=C_FOLDER+project.getId()+folder;
         // try to read folder from cache
         res=(CmsFolder)m_filecache.get(key);
         // not found in cache, so get it from the database and add it to cache
         if (res == null) {
             res= m_accessFile.readFolder(project,folder);
             m_filecache.put(key,res);
          }
         return res;
     }
    
      /**
	 * Writes a folder to the Cms.<br>
	 * 
	 * A folder can only be written to an offline project, the folder state is set to
	 * CHANGED (1).
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
	 * @param changed Flag indicating if the file state must be set to changed.
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void writeFolder(A_CmsProject project, CmsFolder folder, boolean changed)
        throws CmsException {
        String key=C_FOLDER+project.getId()+folder.getAbsolutePath();
        m_accessFile.writeFolder(project, folder,changed);
        m_filecache.remove(key);
    }
 
    	
     /**
	 * Deletes a folder in the Cms.<br>
	 * 
	 * Only folders in an offline Project can be deleted. A folder is deleted by 
	 * setting its state to DELETED (3). <br>
	 *  
	 * In its current implmentation, this method can ONLY delete empty folders.
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write this resource and all subresources</li>
	 * <li>the resource is not locked</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete path of the folder.
	 * @param force If force is set to true, all sub-resources will be deleted.
	 * If force is set to false, the folder will be deleted only if it is empty.
	 * This parameter is not used yet as only empty folders can be deleted!
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void deleteFolder(A_CmsProject project, String foldername, boolean force)
		throws CmsException {
        String key=C_FOLDER+project.getId()+foldername;
        m_accessFile.deleteFolder(project,foldername,force);
        m_filecache.remove(key);
     }
    
    /**
	 * Deletes a folder in the Cms.<br>
	 * 
     * In difference to the deleteFile method, the given file is physically removed <br> 
	 * 
	 * <B>Security:</B>
	 * Access is granted, if:
	 * <ul>
	 * <li>the user has access to the project</li>
	 * <li>the user can read and write this resource and all subresources</li>
	 * <li>the resource is not locked</li>
	 * </ul>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete path of the folder.	
	 * If force is set to false, the folder will be deleted only if it is empty.
	 * This parameter is not used yet as only empty folders can be deleted!
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void removeFolder(A_CmsProject project, String foldername)
		throws CmsException {
        String key=C_FOLDER+project.getId()+foldername;
        m_accessFile.removeFolder(project,foldername);
        m_filecache.remove(key);
     }
    
    /**
     * Deletes all resources of a project.
     *
     * @param project The project to be deleted.
     * @exception CmsException  Throws CmsException if operation was not succesfull.
     */
    public void deleteProject(A_CmsProject project)
        throws CmsException {
		// delete the filecache
		m_filecache = new CmsCache(C_FILECACHE);
		super.deleteProject(project);
    }
}

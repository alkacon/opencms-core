/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRbFileCache.java,v $
 * Date   : $Date: 2000/02/19 10:15:27 $
 * Version: $Revision: 1.1 $
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
 * @version $Revision: 1.1 $ $Date: 2000/02/19 10:15:27 $
 */
 class CmsRbFileCache extends CmsRbFile {
     
     /** The filecache */
     private CmsCache m_filecache=null;
     
     /** The file size */
     private final static int C_FILECACHE=200;
     
     /** The maximum filesize */
     private final static int C_MAXFILESIZE=350000;
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
         String key=project.getId()+filename;
         res=(A_CmsResource)m_filecache.get(key);
         // not found in cache, so get it from the database and add it to cache
         if (res == null) {
            // System.err.println("Not found in Cache, got from DB "+key);
             res=m_accessFile.readFileHeader(project,filename);
             m_filecache.put(key,res);
         } else {
            // System.err.println("Found in Cache "+key);
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
	 * 
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */	
	public void writeFileHeader(A_CmsProject project, 
                                A_CmsProject onlineProject,
                                CmsFile file)
		throws CmsException{
        String key=project.getId()+file.getAbsolutePath();
       // System.err.println("Update cache for "+key);
        // check for max filesize
        if (file.getLength()<C_MAXFILESIZE) {
            m_filecache.put(key,file);
        }
        m_accessFile.writeFileHeader(project,onlineProject,file);
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
        String key=project.getId()+filename;
     //   System.err.println("Delete from cache for "+key);
        m_filecache.remove(key);
        m_accessFile.deleteFile(project,filename);
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
        String key=project.getId()+filename;
      //  System.err.println("Delete from cache for "+key);
        m_filecache.remove(key);
        m_accessFile.removeFile(project,filename);
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
         String key=project.getId()+folder;
         res=(CmsFolder)m_filecache.get(key);
         // not found in cache, so get it from the database and add it to cache
         if (res == null) {
        //     System.err.println("Not found in Cache, got from DB "+key);
             res= m_accessFile.readFolder(project,folder);
             m_filecache.put(key,res);
         } else {
           //  System.err.println("Found in Cache "+key);
         }
         return res;
     }
    
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsAccessFile.java,v $
 * Date   : $Date: 2000/02/15 17:44:00 $
 * Version: $Revision: 1.10 $
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
 * This interface describes the access to files and folders in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.10 $ $Date: 2000/02/15 17:44:00 $
 */
interface I_CmsAccessFile {


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
        throws CmsException;
	
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
                               CmsFile file,String filename)
        throws CmsException;
	
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
         throws CmsException ;
       
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
		throws CmsException;
	
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
	 public CmsFile readFileHeader(A_CmsProject project, String filename)
		throws CmsException;
	
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
		throws CmsException;
     
     
	/**
	 * Writes a file to the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFile(A_CmsProject project,
                           A_CmsProject onlineProject,
                           CmsFile file)
		throws CmsException;
	
	/**
	 * Writes the fileheader to the Cms.
     * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	

	 public void writeFileHeader(A_CmsProject project,
                                 A_CmsProject onlineProject,
                                 CmsFile file)
		throws CmsException;

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
		throws CmsException;
	
	/**
	 * Deletes the file.
	 * 
     * @param project The project in which the resource will be used.
	 * @param filename The complete path of the file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void deleteFile(A_CmsProject project, String filename)
		throws CmsException;
	
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
		throws CmsException;
			
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
        throws CmsException;

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
        throws CmsException;
     
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
		throws CmsException;
	
     /**
	 * Writes a folder to the Cms.<BR/>
	 * 
	 * @param project The project in which the resource will be used.
	 * @param foldername The complete name of the folder (including pathinformation).
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFolder(A_CmsProject project, CmsFolder folder)
		throws CmsException;
     
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
		throws CmsException;

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
		throws CmsException;
	
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
		throws CmsException;
     
     
     /**
     * Copies a resource from the online project to a new, specified project.<br>
     *
     * @param project The project to be published.
	 * @param onlineProject The online project of the OpenCms.
	 * @param resourcename The name of the resource.
 	 * @exception CmsException  Throws CmsException if operation was not succesful.
     */
     public void copyResourceToProject(A_CmsProject project,
                                       A_CmsProject onlineProject,
                                       String resourcename) 
         throws CmsException;
     
     /**
     * Publishes a specified project to the online project. <br>
     *
     * @param project The project to be published.
	 * @param onlineProject The online project of the OpenCms.
	 * @return Vector of all resource names that are published.
     * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector publishProject(A_CmsProject project, A_CmsProject onlineProject)
        throws CmsException;
     
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsAccessFile.java,v $
 * Date   : $Date: 2000/05/18 15:31:30 $
 * Version: $Revision: 1.22 $
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
 * This class is the main module to access files and folders in the Cms.<BR/>
 * Depending on the URI of the requested dockument, it selects the database or filesystem
 * access module which has access to the document.
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.22 $ $Date: 2000/05/18 15:31:30 $
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
        
         
         
        return getFilesystem(filename).createFile(user,
                                                  project,onlineProject,
                                                  filename,flags,
                                                  contents,resourceType);
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
                               CmsFile file,String filename)
         throws CmsException {
         
         // this method is not available for the main CmsAccess File module.
         
         return null;
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
         // to be implemented
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
         return getFilesystem(filename).readFile(project,onlineProject,filename);
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
	 public CmsFile readFileHeader(A_CmsProject project, String filename)
         throws CmsException {
         
         return getFilesystem(filename).readFileHeader(project,filename);
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
         return getFilesystem(filename).readAllFileHeaders(filename);
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
         
         getFilesystem(file.getAbsolutePath()).writeFile(project,onlineProject,file,changed);
     }
	
	/**
	 * Writes the fileheader to the Cms.
     * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param filename The complete name of the new file (including pathinformation).
	 * @param changed Flag indicating if the file state must be set to changed.
	 *
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFileHeader(A_CmsProject project,
                                 A_CmsProject onlineProject,
                                 CmsFile file,boolean changed)
         throws CmsException {
         
         getFilesystem(file.getAbsolutePath()).writeFileHeader(project,onlineProject,file,changed);
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
         
         // get the file systems for the old and new name.
         I_CmsAccessFile oldFs=getFilesystem(oldname);
         I_CmsAccessFile newFs=getFilesystem(newname);
                
         // if both filesystems are the same, use the rename method there
        if (oldFs==newFs) {
            oldFs.renameFile(project,onlineProject,oldname,newname);
        } else {
            // copy the file form the old filesystem to the new one
            CmsFile file=oldFs.readFile(project,onlineProject,oldname);
            newFs.createFile(project,onlineProject,file,newname);
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
	 * Undeletes the file.
	 *  
     * @param project The project in which the resource will be used.
	 * @param filename The complete path of the file.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void undeleteFile(A_CmsProject project, String filename)
         throws CmsException {
         getFilesystem(filename).undeleteFile(project,filename);
     }
     
     
      /**
      * Deletes a file.
      * In difference to the deleteFile method the given file is physically deleted and
      * not only marked as deleted.
      * 
      * @param project The project in which the resource will be used.
	  * @param filename The complete path of the file.
      * @exception CmsException Throws CmsException if operation was not succesful
      */
     public void removeFile(A_CmsProject project, String filename) 
        throws CmsException{
        getFilesystem(filename).removeFile(project,filename);
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
           // get the file systems for the source and the destination.
           I_CmsAccessFile sourceFs=getFilesystem(source);
           I_CmsAccessFile destinationFs=getFilesystem(destination);
                
          // if both filesystems are the same, use the rename method there
          if (sourceFs==destinationFs) {
             sourceFs.copyFile(project,onlineProject,source,destination);
        } else {
            // copy the file form the source filesystem to the destination one
            CmsFile file=sourceFs.readFile(project,onlineProject,source);
            destinationFs.createFile(project,onlineProject,file,destination);
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
         return getFilesystem(foldername).createFolder(user,project,foldername,flags);
     }

     /**
	 * Creates a new folder from an existing folder object.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param folder The folder to be written to the Cms.
     *
	 * @param foldername The complete path of the new name of this folder.
	 * 
	 * @return The created folder.
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	 public CmsFolder createFolder(A_CmsProject project,
                                   A_CmsProject onlineProject,
                                   CmsFolder folder,
                                   String foldername)
         throws CmsException{
         // to be implemented
         return null;
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
	 * @param changed Flag indicating if the file state must be set to changed.
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFolder(A_CmsProject project, CmsFolder folder, 
                             boolean changed)
         throws CmsException {
         
         getFilesystem(folder.getAbsolutePath()).writeFolder(project,folder,changed);
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
         getFilesystem(foldername).deleteFolder(project,foldername,force);
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
             getFilesystem(foldername).removeFolder(project,foldername);
     }
     
     /**
	 * Copies a folder.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param source The complete path of the sourcefolder.
	 * @param destination The complete path of the destinationfolder.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void copyFolder(A_CmsProject project,
                            A_CmsProject onlineProject,
                            String source, String destination)
         throws CmsException {
        
         // get the file systems for the source and the destination.
         I_CmsAccessFile sourceFs=getFilesystem(source);
         I_CmsAccessFile destinationFs=getFilesystem(destination);
            
         // todo: implement a more effective method to copy folders within a single
         // access module
 
         // copy the file form the source filesystem to the destination one
         try {
            CmsFolder folder=sourceFs.readFolder(project,source);
            destinationFs.createFolder(project,onlineProject,folder,destination);        
         } catch (CmsException ex) {};
     }
          
     /**
	 * Renames the folder to the new name.
	 * 
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param oldname The complete path to the resource which will be renamed.
	 * @param newname The new name of the resource.
	 * 
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */		
	 public void renameFolder(A_CmsProject project,
                            A_CmsProject onlineProject,
                            String oldname, String newname)
         throws CmsException{
          
         // get the file systems for the source and the destination.
         I_CmsAccessFile oldFs=getFilesystem(oldname);
         I_CmsAccessFile newFs=getFilesystem(newname);
         
         // todo: implement a more effective method to copy folders within a single
         // access module
         
         CmsFolder folder=oldFs.readFolder(project,oldname);
         newFs.createFolder(project,onlineProject,folder,newname);
         oldFs.deleteFolder(project,oldname,true);         
         
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
                  
         Vector folders=getFilesystem(foldername).getSubFolders(project,foldername);
         
       /*  System.err.println("");
         System.err.println("");
         System.err.println("### Get Subfolders from "+foldername);
         System.err.println("### The project is "+project);*/
         for (int i=0;i<folders.size();i++) {             
             CmsFolder folder=(CmsFolder)folders.elementAt(i);
             //System.err.println("### Checking folder "+folder);
             if (getFilesystem(foldername) != getFilesystem(folder.getAbsolutePath())) {
                 CmsFolder newFolder=readFolder(project,folder.getAbsolutePath());
               //  System.err.println("### Getting folder from other mountpoint "+newFolder);
                 folders.setElementAt(newFolder,i);
             }                         
         }
         
         return folders;
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
         throws CmsException {         
         getFilesystem(resourcename).copyResourceToProject(project,onlineProject,resourcename);
         // If the resource is a folder, test if it is a mountpoint.
         // If it is a mountpoint, an anchor in the parent folder has to be set.
         if (resourcename.endsWith("/")) {
             CmsFolder folder=readFolder(project,resourcename);
             // get the parent folder
             String parentfolder=folder.getParent();
             if (parentfolder != null) {
                     // Check if the filesystem for the resource and the parent are different.
                     // If so, the resource is a mountpoint, so create the anchor in the parent
                     // folder.
                     if (getFilesystem(resourcename) != getFilesystem(parentfolder)) {
                        getFilesystem(parentfolder).copyResourceToProject(project,onlineProject,resourcename);
                    }
             }
         }             
     }
     
     /**
     * Publishes a specified project to the online project. <br>
     *
     * @param project The project to be published.
	 * @param onlineProject The online project of the OpenCms.
	 * @return Vector of all resource names that are published.
     * @exception CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector publishProject(A_CmsProject project, A_CmsProject onlineProject)
        throws CmsException {
        
        Vector resources;
        Vector allResources=new Vector();
        String mountpoint;
        I_CmsAccessFile accessFile=null;
        
        // initatite the publish process for all access modules
        Enumeration e = m_mountpointStorage.keys();
	  	 while (e.hasMoreElements()) {
		   mountpoint=(String)e.nextElement();

           accessFile=(I_CmsAccessFile)m_mountpointStorage.get(mountpoint);
           // publish the project data that is a attached to this specific mountpoint
           resources=accessFile.publishProject(project,onlineProject);
 
           // collect all the publishing information
           if (resources !=null) {
               Enumeration enu=resources.elements();
               while (enu.hasMoreElements()) {
                    allResources.addElement((String)enu.nextElement());           
               }
           }
         }
        
        return allResources;
    }
     
	/**
	 * Unlocks all resources in this project.
	 * 
	 * 
	 * @param id The id of the project to be published.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void unlockProject(A_CmsProject project)
		throws CmsException {
        Vector resources;
        String mountpoint;
        I_CmsAccessFile accessFile=null;
        
        // initatite the unlocking process for all access modules
        Enumeration e = m_mountpointStorage.keys();
		while (e.hasMoreElements()) {
			mountpoint=(String)e.nextElement();
			accessFile=(I_CmsAccessFile)m_mountpointStorage.get(mountpoint);
			// unlock the project
			accessFile.unlockProject(project);
		}
	}
	
    /**
     * Deletes all resources of a project.
     *
     * @param project The project to be deleted.
     * @exception CmsException  Throws CmsException if operation was not succesfull.
     */
    public void deleteProject(A_CmsProject project)
        throws CmsException {
        Vector resources;
        String mountpoint;
        I_CmsAccessFile accessFile=null;
        
        // initatite the deleting process for all access modules
        Enumeration e = m_mountpointStorage.keys();
		while (e.hasMoreElements()) {
			mountpoint=(String)e.nextElement();
			accessFile=(I_CmsAccessFile)m_mountpointStorage.get(mountpoint);
			// delete the project
			accessFile.deleteProject(project);
		}
	}
     
     /**
      * Returns the  appropriate access module for a given file or folder.
      * 
      * @param name The name of the file or folder in the Cms.
      * @return CmsAccessFile module that can access the file or folder.
      */
     private I_CmsAccessFile getFilesystem(String name)	{
		String mountpoint=null;
		String bestmatch=new String("/");
		I_CmsAccessFile accessFile=null;
	        
        // calaculate the path to the file or folder
		String path=null;
		if (name.endsWith("/")) {
			path=name;
		} else {
			path = name.substring(0,name.lastIndexOf("/")+1);
		}
		
		// now check all available mountpoints
		Enumeration e = m_mountpointStorage.keys();
		while (e.hasMoreElements()) {
		  mountpoint=(String)e.nextElement();
          // is the path of the file covered by this mountpoint?
		  if ((path.indexOf(mountpoint) != -1)) {
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

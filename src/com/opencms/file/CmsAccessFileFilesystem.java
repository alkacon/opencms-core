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
 * @version $Revision: 1.12 $ $Date: 2000/02/01 08:19:58 $
 */
 class CmsAccessFileFilesystem implements I_CmsAccessFile, I_CmsConstants  {
   
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
         File diskFile= new File(absoluteName(filename));
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
         File diskFile= new File(absoluteName(filename));
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
         
         // read the file header
         CmsFile file =(CmsFile)readFileHeader(project,filename);
         // check if the fileheader is not found
         if (file != null) {
             File discFile=new File(absoluteName(filename));
		     // check if it is a file
             if (discFile.isFile()){
                 try {
                    // read the file content form the filesystem
			        InputStream s = new FileInputStream(discFile);
				    byte[] discFileContent= new byte[(new Long(discFile.length())).intValue()];
				    int result = s.read(discFileContent);
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
         File discFile= new File (absoluteName(filename));
         // check if file exists in the filesystem.
         if (discFile.exists()) {
             // create file header
             file=new CmsFile(filename,
                              m_mountpoint.getType(),
                              m_mountpoint.getFlags(),
                              m_mountpoint.getUser(),
                              m_mountpoint.getGroup(),
                              m_mountpoint.getProject(),
                              m_mountpoint.getAccessFlags(),
                              C_STATE_UNCHANGED,
                              C_UNKNOWN_ID,
                              m_mountpoint.getLauncherId(),
                              m_mountpoint.getLauncherClass(),
                              discFile.lastModified(),
                              discFile.lastModified(),
                              new byte[0],
                              new Long(discFile.length()).intValue());
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
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFile(A_CmsProject project,
                           A_CmsProject onlineProject,
                           CmsFile file)
       throws CmsException {
          // create new file
         File diskFile= new File(absoluteName(file.getAbsolutePath()));
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
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFileHeader(A_CmsProject project,
                                 A_CmsProject onlineProject,
                                 CmsFile file)
         throws CmsException {
         
         // Since the file header informations of all files in a disc filesystem is 
         // controlled by its mountpoint, nothing is done here.
        
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
         	File discFile=new File(absoluteName(oldname));
			File newDiscFile =new File(absoluteName(newname));
			// check if file exists
            if (discFile.exists())	{
                // check if it is really a file
                if (discFile.isFile()) {
					boolean success=discFile.renameTo(newDiscFile);
					if (!success) {
                        throw new CmsException("[" + this.getClass().getName() + "] "+oldname,CmsException.C_FILESYSTEM_ERROR);
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
         File discFile=new File(absoluteName(filename));
		 // check if file exists
         if (discFile.exists()){
             // it is really a file
             if(discFile.isFile()) {
				boolean success=discFile.delete();
				if (!success) {
						 throw new CmsException("[" + this.getClass().getName() + "] "+filename,CmsException.C_FILESYSTEM_ERROR);
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
         CmsFile sourcefile=readFile(project,onlineProject,source);
         System.err.println(sourcefile.toString());
         // write the destination file
         File diskFile= new File(absoluteName(destination));
         // check if this file is already existing
         if (!diskFile.exists()){
             try {
                 // write the new file to disk
                 OutputStream s = new FileOutputStream(diskFile);
                 s.write(sourcefile.getContents());
                 s.close();
             } catch (Exception e) {
               throw new CmsException("[" + this.getClass().getName() + "] "+e.getMessage());
             }
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
		  File discFolder=new File(absoluteName(foldername));
          // check if this folder already exits
		  if (!discFolder.exists())	{
			boolean success=discFolder.mkdir();
			if (!success) {
				throw new CmsException("[" + this.getClass().getName() + "] "+foldername,CmsException.C_FILESYSTEM_ERROR);
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
         // this method is not required in this access module so far.
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
 	     
	     CmsFolder folder = null;
	     
         
	     File discFolder=new File(absoluteName(foldername));
         if (discFolder != null) {
	   	   if (discFolder.exists() && discFolder.isDirectory()){
			   folder = new CmsFolder(foldername,
                                      C_TYPE_FOLDER,
                                      m_mountpoint.getFlags(),
                                      m_mountpoint.getUser(),
                                      m_mountpoint.getGroup(),
                                      m_mountpoint.getProject(),
                                      m_mountpoint.getAccessFlags(),
                                      C_STATE_UNCHANGED,
                                      C_UNKNOWN_ID,
                                      discFolder.lastModified(),
                                      discFolder.lastModified());
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
	 * 
     * @exception CmsException Throws CmsException if operation was not succesful.
	 */	
	 public void writeFolder(A_CmsProject project, CmsFolder folder)
         throws CmsException {
         
                  
         // Since the folder informations of all folders in a disc filesystem are 
         // controlled by its mountpoint, nothing is done here.
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
         
      	 File discFolder=new File(absoluteName(foldername));
		    // check if file exists	
            if (discFolder.exists()) {
                // it is really a folder
				if (discFolder.isDirectory()) {
					boolean success=discFolder.delete();
					if (!success) {
						throw new CmsException("[" + this.getClass().getName() + "] "+foldername, CmsException.C_FILESYSTEM_ERROR);
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
		 File diskFolder= new File(absoluteName(foldername));		
		 
         // get a list of all sub folders, sort them alphabetically.
         String[] diskFolders=SortEntrys(diskFolder.list());	
	
         // create the subfolder objects.
		 for (int i = 0; i < diskFolders.length; i++) {
			File diskSubFolder=new File(absoluteName(foldername+diskFolders[i]));
				// check if it is a folder.  
                if (diskSubFolder.isDirectory()) {
                     folder = readFolder(project, foldername+diskFolders[i]+"/");
                     v.addElement(folder);
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
		  File diskFolder= new File(absoluteName(foldername));			
	     // get a list of all files, sort them alphabetically.
          String[] diskFiles=SortEntrys(diskFolder.list());				
				
          for (int i = 0; i < diskFiles.length; i++) {
			File diskFile=new File(absoluteName(foldername+diskFiles[i]));
			// check if it is a file
            if (diskFile.isFile()) {
				 file=(CmsFile)readFileHeader(project,foldername+diskFiles[i]);
			     v.addElement(file);					   
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
                                       String resource) 
         throws CmsException {
         
         // all files in a file system mounpoint belong to the online project,
         // so nothing is done here.
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
        
         // all files in a file system mounpoint belong to the online project,
         // so nothing is done here.
        
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
       
	   // new path is rootpath + extracted filenamen	   
	   path=m_mountpoint.getMountPath()+name;
       
       // adjust the fileseperator
	   path=path.replace('/',m_fileseperator);
       return path;
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
}

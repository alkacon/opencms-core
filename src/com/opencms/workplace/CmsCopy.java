/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsCopy.java,v $
 * Date   : $Date: 2000/08/01 17:31:26 $
 * Version: $Revision: 1.34 $
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

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.servlet.http.*;

import java.util.*;

/**
 * Template class for displaying the copy file screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @author Michaela Schleich
 * @version $Revision: 1.34 $ $Date: 2000/08/01 17:31:26 $
 */
public class CmsCopy extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                             I_CmsConstants {
       
      /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
        
    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the copy template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The copy template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, 
                             Hashtable parameters, String templateSelector)
        throws CmsException {
        CmsSession session= cms.getRequestContext().getSession(true);
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);          
 
        // the template to be displayed
        String template=null;
      
        // clear session values on first load
        String initial=(String)parameters.get(C_PARA_INITIAL);
        if (initial!= null) {
            // remove all session values
            session.removeValue(C_PARA_FILE);
            session.removeValue(C_PARA_NEWFILE);
            session.removeValue(C_PARA_NEWFOLDER);
            session.removeValue(C_PARA_FLAGS);
            session.removeValue(C_PARA_NAME);
            session.removeValue("lasturl");
        }
 
        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);
        
        // get the file to be copied
        String filename=(String)parameters.get(C_PARA_FILE);
        if (filename != null) {
            session.putValue(C_PARA_FILE,filename);        
        }
        filename=(String)session.getValue(C_PARA_FILE);
        
        // read all request parameters     
        String newFile=(String)parameters.get(C_PARA_NEWFILE);
        if (newFile != null) {
            session.putValue(C_PARA_NEWFILE,newFile);        
        }
        newFile=(String)session.getValue(C_PARA_NEWFILE);
        
        String newFolder=(String)parameters.get(C_PARA_NEWFOLDER);
        if (newFolder != null) {
            session.putValue(C_PARA_NEWFOLDER,newFolder);        
        }
        newFolder=(String)session.getValue(C_PARA_NEWFOLDER);

        String flags=(String)parameters.get(C_PARA_FLAGS);
        if (flags != null) {
            session.putValue(C_PARA_FLAGS,flags);        
        }
        flags=(String)session.getValue(C_PARA_FLAGS);
        
        String action = (String)parameters.get("action");      

        CmsResource file=(CmsResource)cms.readFileHeader(filename);
        
        // select the template to be displayed
        if (file.isFile()) {
            template="file";            
        } else {
            template="folder";
        }
        
        // modify the folderaname if nescessary (the root folder is always given
        // as a nice name)
        if (newFolder!= null) {
            CmsXmlLanguageFile lang=new CmsXmlLanguageFile(cms);
            if (newFolder.equals(lang.getLanguageValue("title.rootfolder"))) {
                newFolder="/";
            }
        }
  
        //check if the newfile parameter was included in the request
        // if not, the copy page is shown for the first time
        if (newFile == null) {
            session.putValue(C_PARA_NAME,file.getName());
        } else {       

            if (action== null) {
                template="wait";
                
            } else {
            
            // now check if the resource is a file or a folder            
            if (file.isFile()) {
                // this is a file, so copy it    
                try {
                    copyFile(cms,(CmsFile)file,newFolder,newFile,flags);
                } catch (CmsException ex) {
                    // something went wrong, so remove all session parameters
                    session.removeValue(C_PARA_FILE);
                    session.removeValue(C_PARA_NAME);
			        session.removeValue(C_PARA_NEWFILE);
                    session.removeValue(C_PARA_NEWFOLDER);
                    session.removeValue(C_PARA_FLAGS);
                    throw ex;
                }
                // everything is done, so remove all session parameters
                session.removeValue(C_PARA_FILE);
                session.removeValue(C_PARA_NAME);
			    session.removeValue(C_PARA_NEWFILE);
                session.removeValue(C_PARA_NEWFOLDER);
                session.removeValue(C_PARA_FLAGS);
                 // TODO: Error handling
                
                // now return to filelist
                try {
                    if(lasturl == null || "".equals(lasturl)) {
                        cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
                    } else {
                        cms.getRequestContext().getResponse().sendRedirect(lasturl);
                    }                            
                } catch (Exception e) {
                    throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST,CmsException.C_UNKNOWN_EXCEPTION,e);
                } 
                return null;
                         
            } else {
                // the selected resource is a folder, so copy it and all its subresources
                // get all subfolders and files
                Vector allFolders=new Vector();
                Vector allFiles=new Vector();
                getAllResources(cms,filename,allFiles,allFolders);
              
                try {
                    //copy the selected folder
                    cms.copyFolder(filename, newFolder+newFile+"/");                
                    checkFlags(cms, newFolder+newFile+"/",flags);                                           
           
                    // now copy all subfolders
                    for (int i=0;i<allFolders.size();i++) {
                        CmsFolder folder=(CmsFolder)allFolders.elementAt(i);  
                        if (folder.getState() != C_STATE_DELETED) {
                    
                            cms.copyFolder(folder.getAbsolutePath(), newFolder+newFile+"/"+folder.getAbsolutePath().substring(file.getAbsolutePath().length()));
                 
                            checkFlags(cms,newFolder+newFile+"/"+folder.getAbsolutePath().substring(file.getAbsolutePath().length()),flags);                    
                        }
                    }
              
                    // now copy all files in the subfolders
                    for (int i=0;i<allFiles.size();i++) {
                        CmsFile newfile=(CmsFile)allFiles.elementAt(i);  
                        if (newfile.getState() != C_STATE_DELETED) {
                    
                            copyFile(cms,newfile,newFolder+newFile+"/",newfile.getAbsolutePath().substring(file.getAbsolutePath().length()),flags);
                        }
                    }    
                  // finally lock everything
                   // cms.lockResource(newFolder+newFile+"/");
                    try {
                        cms.lockResource(C_CONTENTBODYPATH+(newFolder+newFile+"/").substring(1));
                    } catch (CmsException e) {
                    }
                           
                	// everything is done, so remove all session parameters		
                    session.removeValue(C_PARA_FILE);
                    session.removeValue(C_PARA_NAME);
			        session.removeValue(C_PARA_NEWFILE);
                    session.removeValue(C_PARA_NEWFOLDER);
                    session.removeValue(C_PARA_FLAGS);
					xmlTemplateDocument.setData("lasturl", lasturl);
                    template="update";
               
                    
                }catch (CmsException ex) {
                    // something went wrong, so remove all session parameters
                    session.removeValue(C_PARA_FILE);
                    session.removeValue(C_PARA_NAME);
			        session.removeValue(C_PARA_NEWFILE);
                    session.removeValue(C_PARA_NEWFOLDER);
                    session.removeValue(C_PARA_FLAGS);
                    throw ex;
                }

                }
            }
           
        }
         
        // set the required datablocks
        String title=cms.readProperty(file.getAbsolutePath(),C_PROPERTY_TITLE);
        if (title==null) {
            title="";
        }
        CmsUser owner=cms.readOwner(file);
        xmlTemplateDocument.setData("TITLE",title);
        xmlTemplateDocument.setData("STATE",getState(cms,file,new CmsXmlLanguageFile(cms)));
        xmlTemplateDocument.setData("OWNER",Utils.getFullName(owner));
        xmlTemplateDocument.setData("GROUP",cms.readGroup(file).getName());
		xmlTemplateDocument.setData("FILENAME",file.getName());
                   
        
        // process the selected template 
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);   
    }

    
     /**
     * Pre-Sets the value of the copy field.
     * This method is directly called by the content definiton.
     * @param Cms The CmsObject.
     * @param lang The language file.
     * @param parameters User parameters.
     * @return Value that is pre-set into the copy field.
     * @exception CmsExeption if something goes wrong.
     */
    public String setValue(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters)
        throws CmsException {
        CmsSession session= cms.getRequestContext().getSession(true);
        String name=(String)session.getValue(C_PARA_NAME);
        return name;
    }
  
     /**
     * Gets all folders to copy the selected file to.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * <P>
     * <code>names</code> will contain language specific view descriptions
     * and <code>values</code> will contain the correspondig URL for each
     * of these views after returning from this method.
     * <P>
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the available folders.
     * @exception CmsException
     */
    public Integer getFolder(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
            throws CmsException {
        
        Integer selected=new Integer(0);
        // Let's see if we have a session
        CmsSession session = cms.getRequestContext().getSession(true);
       
        // get current and root folder
        CmsFolder rootFolder=cms.rootFolder();
        
        //add the root folder
        names.addElement(lang.getLanguageValue("title.rootfolder"));
        values.addElement("/");
        getTree(cms,rootFolder,names,values);
   
        // now search for the current folder
         String currentFilelist=(String)session.getValue(C_PARA_FILELIST);
         for (int i=0;i<values.size();i++) {
             if (((String)values.elementAt(i)).equals(currentFilelist)) {
                 selected=new Integer(i);
             }
         }
        return selected;
    }

    /** 
     * Gets all folders of the filesystem. <br>
     * This method is used to create the selecebox for selecting the target directory.
     * @param cms The CmsObject.
     * @param root The root folder for the tree to be displayed.
     * @param names Vector for storing all names needed in the selectbox.
     * @param values Vector for storing values needed in the selectbox.
     */
    private void getTree(CmsObject cms,CmsFolder root,Vector names,Vector values)
        throws CmsException{
        Vector folders=cms.getSubFolders(root.getAbsolutePath());
        CmsProject currentProject = cms.getRequestContext().currentProject();
        Enumeration enu=folders.elements();
        while (enu.hasMoreElements()) {
            CmsFolder folder=(CmsFolder)enu.nextElement();
            // check if the current folder is part of the current project
            if (folder.inProject(currentProject)) {
                String name=folder.getAbsolutePath();
                name=name.substring(1,name.length()-1);
                names.addElement(name);
                values.addElement(folder.getAbsolutePath());
            }
            getTree(cms,folder,names,values);
        }
    }
	
	/**
       * This method checks if all nescessary folders are exisitng in the content body
       * folder and creates the missing ones. <br>
       * All page contents files are stored in the content body folder in a mirrored directory
       * structure of the OpenCms filesystem. Therefor it is nescessary to create the 
       * missing folders when a new page document is createg.
       * @param cms The CmsObject
       * @param path The path in the CmsFilesystem where the new page should be created.
       * @exception CmsException if something goes wrong.
       */
     private void checkFolders(CmsObject cms, String path) 
          throws CmsException {
         
         
          String completePath=C_CONTENTBODYPATH;
          StringTokenizer t=new StringTokenizer(path,"/");
          // check if all folders are there
          while (t.hasMoreTokens()) {
              String foldername=t.nextToken();
               try {
                // try to read the folder. if this fails, an exception is thrown  
      
                cms.readFolder(completePath+foldername+"/");
              } catch (CmsException e) {
                  // the folder could not be read, so create it.
                  String orgFolder=completePath+foldername+"/";
                  orgFolder=orgFolder.substring(C_CONTENTBODYPATH.length()-1);
                  CmsFolder newfolder=cms.createFolder(completePath,foldername);                              
                  CmsFolder folder=cms.readFolder(orgFolder);
                  cms.lockResource(newfolder.getAbsolutePath());
                  cms.chown(newfolder.getAbsolutePath(),cms.readOwner(folder).getName());
                  cms.chgrp(newfolder.getAbsolutePath(),cms.readGroup(folder).getName());
                  cms.chmod(newfolder.getAbsolutePath(),folder.getAccessFlags());
                  cms.unlockResource(newfolder.getAbsolutePath());
              }
              completePath+=foldername+"/";        
          }          
     }
	  
	/**
	 * method to check get the real body path from the content file
	 * 
	 * @param cms The CmsObject, to access the XML read file.
	 * @param file File in which the body path is stored.
	 */
	private String getBodyPath(CmsObject cms, CmsFile file)
		throws CmsException{
		file=cms.readFile(file.getAbsolutePath());
		CmsXmlControlFile hXml=new CmsXmlControlFile(cms, file);
		return hXml.getElementTemplate("body");
	}
	
	  /**
       * This method changes the path of the body file in the xml conten file
       * if file type name is page
       * 
       * @param cms The CmsObject
       * @param file The XML content file
       * @param bodypath the new XML content entry
       * @exception Throws CmsException if something goes wrong.
       */
	  private void changeContent(CmsObject cms, CmsFile file, String bodypath)
		  throws CmsException {

		  file=cms.readFile(file.getAbsolutePath());
		  CmsXmlControlFile hXml=new CmsXmlControlFile(cms, file);
		  hXml.setElementTemplate("body", bodypath);
		  hXml.write();
	  }
      
     /**
     * Gets a formated file state string.
     * @param cms The CmsObject.
     * @param file The CmsResource.
     * @param lang The content definition language file.
     * @return Formated state string.
     * @exception Throws CmsException if something goes wrong.
     */
     private String getState(CmsObject cms, CmsResource file,CmsXmlLanguageFile lang)
         throws CmsException {
         StringBuffer output=new StringBuffer();
         
         if (file.inProject(cms.getRequestContext().currentProject())) {
            int state=file.getState();
            output.append(lang.getLanguageValue("explorer.state"+state));
         } else {
            output.append(lang.getLanguageValue("explorer.statenip"));
         }
         return output.toString();
     }
     
         
    /**
     * Gets all resources - files and subfolders - of a given folder.
     * @param cms The CmsObject.
     * @param rootFolder The name of the given folder.
     * @param allFiles Vector containing all files found so far. All files of this folder
     * will be added here as well.
     * @param allolders Vector containing all folders found so far. All subfolders of this folder
     * will be added here as well.
     * @exception Throws CmsException if something goes wrong.
     */
    private void getAllResources(CmsObject cms, String rootFolder,
                                 Vector allFiles, Vector allFolders) 
     throws CmsException {
        Vector folders=new Vector();
        Vector files=new Vector();
        
        // get files and folders of this rootFolder
        folders=cms.getSubFolders(rootFolder);
        files=cms.getFilesInFolder(rootFolder);
        
        
        //copy the values into the allFiles and allFolders Vectors
        for (int i=0;i<folders.size();i++) {
            allFolders.addElement((CmsFolder)folders.elementAt(i));
            getAllResources(cms,((CmsFolder)folders.elementAt(i)).getAbsolutePath(),
                            allFiles,allFolders);
        }
        for (int i=0;i<files.size();i++) {
            allFiles.addElement((CmsFile)files.elementAt(i));
        } 
    }
 
    /**
     * Check if the access flags of the copied resource must be set to the default values.
     * @param cms The CmsObject.
     * @param filename The name of the file.
     * @param flags The flag to change the access flags.
     * @exception Throws CmsException if something goes wrong.
     */
    private void checkFlags(CmsObject cms,String filename,String flags) 
        throws CmsException {
        
        if (flags.equals("false")) {
            // set access flags of the new file to the default flags
                        
            Hashtable startSettings=null;
            Integer accessFlags=null;
            startSettings=(Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);                    
            if (startSettings != null) {
                accessFlags=(Integer)startSettings.get(C_START_ACCESSFLAGS);
            }
            if (accessFlags == null) {
                    accessFlags=new Integer(C_ACCESS_DEFAULT_FLAGS);
            }         
            cms.chmod(filename,accessFlags.intValue());
        }
        //cms.unlockResource(filename);                    
    }
    
    
    /**
     * Copies a file.
     * If the file is a of the file type "page", its body file is copied as well.
     * @param cms The CmsObject.
     * @param file The fiel to be copied.
     * @param newFolder The destination folder.
     * @param newFile The destination filename.
     * @param flags The given flags of the CmsRequest. Needed to decide if the access flags of the
     * destination has to be modified.
     * @param lock Flag showing if the resource has to locked.
     * @exception Throws CmsException if something goes wrong.
     */
    private void copyFile(CmsObject cms,CmsFile file, String newFolder, String newFile,String flags) 
        throws CmsException {

        // copy the file and set the access flags if nescessary

        cms.copyFile(file.getAbsolutePath(),newFolder+newFile);

	    //is file type plain
		if( (cms.getResourceType(file.getType()).getResourceName()).equals(C_TYPE_PAGE_NAME) ){
        
 	        String bodyPath = getBodyPath(cms, (CmsFile)file);
			int help = C_CONTENTBODYPATH.lastIndexOf("/");
			String hbodyPath=(C_CONTENTBODYPATH.substring(0,help))+(file.getAbsolutePath());
  			if (hbodyPath.equals(bodyPath)){      
             
                    String contentPath=newFolder+newFile;
                    contentPath=contentPath.substring(0,contentPath.lastIndexOf("/")+1);                    
			        checkFolders(cms, contentPath);
				    String newbodyPath=(C_CONTENTBODYPATH.substring(0,help))+newFolder+newFile;
		            CmsFile newContent = cms.readFile(newFolder+newFile);
         
			        changeContent(cms, newContent, newbodyPath);
            
				    cms.copyFile(bodyPath,newbodyPath);
    
			} else {
			    // unlock the template file, to prevent access errors, because
				// new template file will automatically be locked to the user
				//cms.unlockResource(file.getAbsolutePath());
		    }
		}
	    CmsFile newfile=cms.readFile(newFolder,newFile);    
        checkFlags(cms,newfile.getAbsolutePath(),flags);   
    }
}
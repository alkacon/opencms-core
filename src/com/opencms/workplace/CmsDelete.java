/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsDelete.java,v $
 * Date   : $Date: 2000/05/02 10:03:34 $
 * Version: $Revision: 1.20 $
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
import com.opencms.examples.news.*;

import javax.servlet.http.*;

import java.util.*;

/**
 * Template class for displaying the delete screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @author Michaela Schleich
  * @version $Revision: 1.20 $ $Date: 2000/05/02 10:03:34 $
 */
public class CmsDelete extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                             I_CmsConstants, I_CmsNewsConstants {
           

      /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
    
    
    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the delete template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The delete template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, 
                             Hashtable parameters, String templateSelector)
        throws CmsException {
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
        
        // the template to be displayed
        String template=null;
        
        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);
                      
        // clear session values on first load
        String initial=(String)parameters.get(C_PARA_INITIAL);
        if (initial!= null) {
            // remove all session values
            session.removeValue(C_PARA_DELETE);
            session.removeValue(C_PARA_FILE);   
        }
        
        
        String delete=(String)parameters.get(C_PARA_DELETE);          
        if (delete != null) {
            session.putValue(C_PARA_DELETE,delete);        
        }
        delete=(String)session.getValue(C_PARA_DELETE); 
        
        String filename=(String)parameters.get(C_PARA_FILE);
        if (filename != null) {
            session.putValue(C_PARA_FILE,filename);        
        }
        filename=(String)session.getValue(C_PARA_FILE);
        
        String action = (String)parameters.get("action");
        
		A_CmsResource file=(A_CmsResource)cms.readFileHeader(filename);

        if (file.isFile()) {
            template="file";
        } else {
            template="folder";
        }

        //check if the name parameter was included in the request
        // if not, the delete page is shown for the first time
    

        if (delete != null) {
            if (action== null) {
                template="wait";                
            } else {
            
            // check if the resource is a file or a folder
            if (file.isFile()) {            
                // its a file, so delete it
                deleteFile(cms,file,false);
                
                session.removeValue(C_PARA_DELETE);  
                session.removeValue(C_PARA_FILE);
                try {
                    if(lasturl == null || "".equals(lasturl)) {
                        cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
                    } else {
                        ((HttpServletResponse)(cms.getRequestContext().getResponse().getOriginalResponse())).sendRedirect(lasturl);                       
                    }                            
                } catch (Exception e) {
                    throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST,CmsException.C_UNKNOWN_EXCEPTION,e);
                } 
                
            } else {               
                // its a folder, so try to delete the folder and its subfolders
                // get all subfolders and files
                Vector allFolders=new Vector();
                Vector allFiles=new Vector();
         
                getAllResources(cms,filename,allFiles,allFolders);
               
                // unlock the folder, otherwise the subflders and files could not be
                // deleted.
                cms.unlockResource(filename);
             
                // now delete all files in the subfolders
                for (int i=0;i<allFiles.size();i++) {
                    CmsFile newfile=(CmsFile)allFiles.elementAt(i);  
                    if (newfile.getState() != C_STATE_DELETED) {
                        cms.lockResource(newfile.getAbsolutePath());
                        deleteFile(cms,newfile,true);
                    }
                }    
        
                // now delete all subfolders
                for (int i=0;i<allFolders.size();i++) {
                    CmsFolder folder=(CmsFolder)allFolders.elementAt(allFolders.size()-i-1);  
                    if (folder.getState() != C_STATE_DELETED) {
                        cms.lockResource(folder.getAbsolutePath());
                        cms.deleteFolder(folder.getAbsolutePath());
                    }
                }
         
                // finally delete the selected folder
                cms.lockResource(filename);
                cms.deleteFolder(filename);
                
                session.removeValue(C_PARA_DELETE);  
                session.removeValue(C_PARA_FILE);
                template="update";
            
            }
            }
            
          
            
            // TODO: Error handling
           
        }

        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);
        // set the required datablocks
        if (action == null) {
            String title=cms.readProperty(file.getAbsolutePath(),C_PROPERTY_TITLE);
            if (title==null) {
               title="";
            }
            A_CmsUser owner=cms.readOwner(file);
            xmlTemplateDocument.setData("TITLE",title);
            xmlTemplateDocument.setData("STATE",getState(cms,file,new CmsXmlLanguageFile(cms)));
            xmlTemplateDocument.setData("OWNER",Utils.getFullName(owner));
            xmlTemplateDocument.setData("GROUP",cms.readGroup(file).getName());
		    xmlTemplateDocument.setData("FILENAME",file.getName());
        }
        // process the selected template 
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template); 
    }
	
	/**
	 * method to check get the real body path from the content file
	 * 
	 * @param cms The CmsObject, to access the XML read file.
	 * @param file File in which the body path is stored.
	 */
	private String getBodyPath(A_CmsObject cms, CmsFile file)
	throws CmsException{
		file=cms.readFile(file.getAbsolutePath());
		CmsXmlControlFile hXml=new CmsXmlControlFile(cms, file);
		return hXml.getElementTemplate("body");
	}
    
	/**
	 * Get the real path of the news content file.
	 * 
	 * @param cms The CmsObject, to access the XML read file.
	 * @param file File in which the body path is stored.
	 */
    private String getNewsContentPath(A_CmsObject cms, CmsFile file) throws CmsException {

        String newsContentFilename = null;

        // The given file object contains the news page file.
        // we have to read out the article
        CmsXmlControlFile newsPageFile = new CmsXmlControlFile(cms, file.getAbsolutePath());
        String readParam = newsPageFile.getElementParameter("body", "read");
        String newsfolderParam = newsPageFile.getElementParameter("body", "newsfolder");
        
        if(readParam != null && !"".equals(readParam)) {
            // there is a read parameter given.
            // so we know which news file should be read.
            if(newsfolderParam == null || "".equals(newsfolderParam)) {
                newsfolderParam = C_NEWS_FOLDER_CONTENT;
            }
            newsContentFilename = newsfolderParam + readParam;
        }
        return newsContentFilename;
    }    

    
    /** 
     * Deletes a file.
     * If the file is a page file, its content will be deleted, too.
     * If the file is a news file, the content and folder will be deleted, too.
     * @param cms The CmsObject.
     * @param file The file to be deleted.
     * @param lock Flag showing if the resource has to locked.
     * @exception Throws CmsException if something goes wrong.
     */
    private void deleteFile (A_CmsObject cms, A_CmsResource file,boolean lock) 
        throws CmsException {
        
        boolean hDelete = true;
	    //check if the file type name is page
		//if so delete the file body and content
		if( (cms.getResourceType(file.getType()).getResourceName()).equals(C_TYPE_PAGE_NAME) ){
		    String bodyPath=getBodyPath(cms, (CmsFile)file);
			try {
	    	    int help = C_CONTENTBODYPATH.lastIndexOf("/");
    	        String hbodyPath=(C_CONTENTBODYPATH.substring(0,help))+(file.getAbsolutePath());
				if (hbodyPath.equals(bodyPath)){
                   // this method was called during the process of deleting a folder,
                   // so lock the content file
                   if (lock) {
                        cms.lockResource(hbodyPath);
                    }
				    cms.deleteFile(hbodyPath);
               				}
    		}catch (CmsException e){
	    	    //TODO: ErrorHandling
		    }
        //
		} else if((cms.getResourceType(file.getType()).getResourceName()).equals(C_TYPE_NEWSPAGE_NAME) ){
		    String newsContentPath = getNewsContentPath(cms, (CmsFile) file);
			try {
			    CmsFile newsContentFile=(CmsFile)cms.readFileHeader(newsContentPath);
				if((newsContentFile.isLocked()) && (newsContentFile.isLockedBy()==cms.getRequestContext().currentUser().getId()) ){
				    cms.deleteFile(newsContentPath);
				}
			}catch (CmsException e){
		    	//TODO: ErrorHandling
			}
            
            try {
                cms.deleteFile(file.getAbsolutePath());
                hDelete = false;
			}catch (CmsException e){
			    //TODO: ErrorHandling
			}
                        
            try {
                String parentFolderName = file.getParent();
                CmsFolder parentFolder = cms.readFolder(parentFolderName);

                if((!parentFolder.isLocked()) || (parentFolder.isLockedBy()!=cms.getRequestContext().currentUser().getId()) ){
                    cms.lockResource(parentFolderName);
                }
                cms.deleteFolder(parentFolderName);                                                                       
			}catch (CmsException e){
			    //TODO: ErrorHandling
            }                    
          }

          if(hDelete) {
            cms.deleteFile(file.getAbsolutePath());
          }
      
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
    private void getAllResources(A_CmsObject cms, String rootFolder,
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
     * Gets a formated file state string.
     * @param cms The CmsObject.
     * @param file The CmsResource.
     * @param lang The content definition language file.
     * @return Formated state string.
     * @exception Throws CmsException if something goes wrong.
     */
     private String getState(A_CmsObject cms, A_CmsResource file,CmsXmlLanguageFile lang)
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

}
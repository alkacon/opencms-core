/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsCopy.java,v $
 * Date   : $Date: 2000/02/29 16:44:47 $
 * Version: $Revision: 1.9 $
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
 * @version $Revision: 1.9 $ $Date: 2000/02/29 16:44:47 $
 */
public class CmsCopy extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                             I_CmsConstants {
       
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
     * Gets the content of the copy template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The copy template file
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
      
        // get the file to be copied
        String filename=(String)parameters.get(C_PARA_FILE);
        if (filename != null) {
            session.putValue(C_PARA_FILE,filename);        
        }
        filename=(String)session.getValue(C_PARA_FILE);
        
        // read all request parameters
        String newFile=(String)parameters.get(C_PARA_NEWFILE);
        String newFolder=(String)parameters.get(C_PARA_NEWFOLDER);
        String flags=(String)parameters.get(C_PARA_FLAGS);
        
        CmsFile file=(CmsFile)cms.readFileHeader(filename);
  
        //check if the newfile parameter was included in the request
        // if not, the copy page is shown for the first time
        if (newFile == null) {
            session.putValue(C_PARA_NAME,file.getName());
        } else {
             // copy the file and set the access flags if nescessary
             cms.copyFile(file.getAbsolutePath(),newFolder+newFile);
			 //is file type plain
			 if( (cms.getResourceType(file.getType()).getResourceName()).equals(C_TYPE_PAGE_NAME) ){
				String bodyPath = getBodyPath(cms, file);
				int help = C_CONTENTBODYPATH.lastIndexOf("/");
				String hbodyPath=(C_CONTENTBODYPATH.substring(0,help))+(file.getAbsolutePath());
				if (hbodyPath.equals(bodyPath)){
					checkFolders(cms, newFolder);
					String newbodyPath=(C_CONTENTBODYPATH.substring(0,help))+newFolder+newFile;
					CmsFile newContent = cms.readFile(newFolder+newFile);
					changeContent(cms, newContent, newbodyPath);
					cms.copyFile(bodyPath,newbodyPath);
					if (flags.equals("false")) {
						 // set access flags of the new file to the default flags
						CmsFile newfile=cms.readFile(newFolder,file.getName());
						// TODO: use the default flags in the user preferences when they are implemented             
						newfile.setAccessFlags(C_ACCESS_DEFAULT_FLAGS);  
				 		cms.writeFile(newfile);
				    }
				}else{
					// unlock the template file, to prevent access errors, because
					// new template file will automatically be locked to the user
					cms.unlockResource(file.getAbsolutePath());
				}
			}
	
             if (flags.equals("false")) {
                 // set access flags of the new file to the default flags
                 CmsFile newfile=cms.readFile(newFolder,newFile);
                 // TODO: use the default flags in the user preferences when they are implemented             
                 newfile.setAccessFlags(C_ACCESS_DEFAULT_FLAGS);  
                 cms.writeFile(newfile);
             }
			 session.removeValue(C_PARA_FILE);
			 session.removeValue(C_PARA_NAME);
             // TODO: Error handling
             try {
               cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
            } catch (Exception e) {
                  throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST,CmsException.C_UNKNOWN_EXCEPTION,e);
            } 
        }
         
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);          
        xmlTemplateDocument.setXmlData("OWNER",cms.readOwner(file).getName());
        xmlTemplateDocument.setXmlData("GROUP",cms.readGroup(file).getName());
		xmlTemplateDocument.setXmlData("FILENAME",file.getName());
        
        // process the selected template 
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);   
    }

    
     /**
     * Pre-Sets the value of the copy field.
     * This method is directly called by the content definiton.
     * @param Cms The CmsObject.
     * @param lang The language file.
     * @return Value that is pre-set into the copy field.
     * @exception CmsExeption if something goes wrong.
     */
    public String setValue(A_CmsObject cms, CmsXmlLanguageFile lang)
        throws CmsException {
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
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
     * @param cms A_CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the available folders.
     * @exception CmsException
     */
    public Integer getFolder(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
            throws CmsException {
        
        Integer selected=new Integer(0);
        // Let's see if we have a session
        A_CmsRequestContext reqCont = cms.getRequestContext();
        HttpSession session = ((HttpServletRequest)reqCont.getRequest().getOriginalRequest()).getSession(false);
       
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
    private void getTree(A_CmsObject cms,CmsFolder root,Vector names,Vector values)
        throws CmsException{
        Vector folders=cms.getSubFolders(root.getAbsolutePath());
        A_CmsProject currentProject = cms.getRequestContext().currentProject();
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
      private void checkFolders(A_CmsObject cms, String path) 
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
                  cms.createFolder(completePath,foldername);                              
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
	private String getBodyPath(A_CmsObject cms, CmsFile file)
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
       * @exception Exception if something goes wrong.
       */
	  private void changeContent(A_CmsObject cms, CmsFile file, String bodypath)
		  throws CmsException {
		  file=cms.readFile(file.getAbsolutePath());
		  CmsXmlControlFile hXml=new CmsXmlControlFile(cms, file);
		  hXml.setElementTemplate("body", bodypath);
		  hXml.write();
	  }
}
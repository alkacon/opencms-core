/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminPicGalleries.java,v $
 * Date   : $Date: 2000/06/02 13:02:54 $
 * Version: $ $
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

import java.util.*;
import java.io.*;
import javax.servlet.http.*;

/**
 * Template Class for administration of picture galleries
 * <p> 
 * 
 * @author Mario Stanke
 * @version $Revision: 1.5 $ $Date: 2000/06/02 13:02:54 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsAdminPicGalleries extends CmsWorkplaceDefault implements I_CmsConstants, I_CmsFileListUsers {
      
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
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters. 
     * 
     * @see getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
       
		HttpServletRequest orgReq = (HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest();    
		HttpSession session = orgReq.getSession(true);       
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
   
		// clear session values on first load
        String initial=(String)parameters.get(C_PARA_INITIAL);
        if (initial != null) {
            // remove all session values
            session.removeValue(C_PARA_FOLDER); 
            session.removeValue("lasturl");
        }
		// getting the URL to which we need to return when we're done
		String lasturl = getLastUrl(cms, parameters); 
		
		// read the parameters
		String foldername = (String) parameters.get(C_PARA_FOLDER);
		if (foldername != null) {
			// need the foldername in the session in case of an exception in the dialog
			session.putValue(C_PARA_FOLDER, foldername);
		} else {
			foldername = (String) session.getValue(C_PARA_FOLDER);
		}
		String action = (String) parameters.get("action"); 
		String newname=(String)parameters.get(C_PARA_NAME);
		String title = (String) parameters.get("TITLE"); // both for gallery and upload file
		String step = (String) parameters.get("step");   
		
		if (foldername == null) {
			foldername="";	
		} 
		if ("new".equals(action)) { 
			String  galleryname = (String) parameters.get("NAME"); 
			String  group = (String) parameters.get("GROUP");
			if (galleryname != null && group != null && galleryname != "" && group != "") {
				boolean  read = parameters.get("READ") != null;
				boolean  write = parameters.get("WRITE") != null; 
				try {   
					// create the folder
					// get the path from the workplace.ini
					String superfolder = getConfigFile(cms).getPicGalleryPath(); 
					CmsFolder folder=cms.createFolder(superfolder, galleryname); 
					cms.lockResource(folder.getAbsolutePath());
					if (title != null) {
						cms.writeProperty(folder.getAbsolutePath(), C_PROPERTY_TITLE, title);
					} 
					cms.chgrp(folder.getAbsolutePath(), group);
					int flag = folder.getAccessFlags(); 
					// set the access rights for 'other' users
					if (read != ((flag & C_ACCESS_PUBLIC_READ) != 0)) {
						flag ^= C_ACCESS_PUBLIC_READ;	
					} 
					if (write != ((flag & C_ACCESS_PUBLIC_WRITE) != 0)) {
						flag ^= C_ACCESS_PUBLIC_WRITE;	
					}  
					cms.chmod(folder.getAbsolutePath(), flag);
				} catch (CmsException ex) { 
					xmlTemplateDocument.setData("ERRORDETAILS", Utils.getStackTrace(ex));
					templateSelector="error"; 
				} 
			} else {
				templateSelector="datamissing";
			}
		} else if ("upload".equals(action)) { 
			// get filename and file content if available
			String filename=null;
			byte[] filecontent=new byte[0];
        
			// get the filename
			Enumeration files=cms.getRequestContext().getRequest().getFileNames();
			while (files.hasMoreElements()) {
			   filename=(String)files.nextElement();
			}             
			if (filename != null) {
			    session.putValue(C_PARA_FILE,filename);
			}        
			filename=(String)session.getValue(C_PARA_FILE);
        
			// get the filecontent
			if (filename != null) {
			    filecontent=cms.getRequestContext().getRequest().getFile(filename);
			}        
			if (filecontent != null) {
			    session.putValue(C_PARA_FILECONTENT,filecontent);          
			}        
			filecontent=(byte[])session.getValue(C_PARA_FILECONTENT);
			if ("0".equals(step)) {
				templateSelector="";	
			} else if ("1".equals(step)) { 
                // display the select filetype screen
				if (filename != null) {
                   // check if the file size is 0
                   if (filecontent.length == 0) {
                       templateSelector="error";
                       xmlTemplateDocument.setData("details", filename);                        
                   } else {   
						xmlTemplateDocument.setData("MIME",filename);
						xmlTemplateDocument.setData("SIZE","Not yet available");
						xmlTemplateDocument.setData("FILESIZE",new Integer(filecontent.length).toString()+" Bytes");
						xmlTemplateDocument.setData("FILENAME", filename);	
						templateSelector="step1";   
                   }
                } 
			 } else if ("2".equals(step)) {
				// check if a new filename is given
                if (newname != null) {
                    filename=newname;
				} 
				CmsFile file=cms.createFile(foldername, filename, filecontent, C_TYPE_IMAGE_NAME); 
				 
				if (title!= null) {
					String filepath= file.getAbsolutePath();
                    cms.lockResource(filepath);
                    cms.writeProperty(filepath, C_PROPERTY_TITLE,title); 
                } 
				
				try { 
					cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+lasturl);
				} catch (Exception ex) { 
                    throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+ lasturl, CmsException.C_UNKNOWN_EXCEPTION,ex);
                } 
			 }
		}
		
		xmlTemplateDocument.setData("link_value", foldername); 
		xmlTemplateDocument.setData("lasturl", lasturl);
		
        // Finally start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }
 
   /** 
    * From interface <code>I_CmsFileListUsers</code>.
    * <P>    
    * Collects all folders and files that are displayed in the file list.
    * @param cms The CmsObject.
    * @return A vector of folder and file objects.
    * @exception Throws CmsException if something goes wrong.
    */
    public Vector getFiles(A_CmsObject cms) 
        throws CmsException {
        Vector galleries = new Vector();
        Vector folders = cms.getSubFolders(getConfigFile(cms).getPicGalleryPath()); 
        int numFolders = folders.size();
		
        for(int i=0; i<numFolders; i++) {
            A_CmsResource currFolder = (A_CmsResource)folders.elementAt(i); 
            galleries.addElement(currFolder);            
        }                                                            
        return galleries;
    }
       
    /**
     * From interface <code>I_CmsFileListUsers</code>.
     * <P>    
     * Used to modify the bit pattern for hiding and showing columns in
     * the file list.
     * @param cms Cms object for accessing system resources.
     * @param prefs Old bit pattern.
     * @return New modified bit pattern.
     * @see I_CmsFileListUsers
     */
    public int modifyDisplayedColumns(A_CmsObject cms, int prefs) {  
        prefs = ((prefs & C_FILELIST_NAME) == 0) ? prefs : (prefs - C_FILELIST_NAME);
        prefs = ((prefs & C_FILELIST_TITLE) == 0) ? prefs : (prefs - C_FILELIST_TITLE);
        prefs = ((prefs & C_FILELIST_TYPE) == 0) ? prefs : (prefs - C_FILELIST_TYPE);
		prefs = ((prefs & C_FILELIST_SIZE) == 0) ? prefs : (prefs - C_FILELIST_SIZE);
        return prefs;
    }
    
    /**
     * From interface <code>I_CmsFileListUsers</code>.
     * <P>    
     * Fills all customized columns with the appropriate settings for the given file 
     * list entry. Any column filled by this method may be used in the customized template
     * for the file list.
     * @param cms Cms object for accessing system resources.
     * @param filelist Template file containing the definitions for the file list together with
     * the included customized defintions.
     * @param res A_CmsResource Object of the current file list entry.
     * @param lang Current language file.
     * @exception CmsException if access to system resources failed.
     * @see I_CmsFileListUsers
     */
    public void getCustomizedColumnValues(A_CmsObject cms, CmsXmlWpTemplateFile filelistTemplate, A_CmsResource res, CmsXmlLanguageFile lang) 
        throws CmsException {
		CmsXmlWpConfigFile config=this.getConfigFile(cms);
		filelistTemplate.fastSetXmlData(C_FILELIST_ICON_VALUE, config.getWpPictureUrl()+"ic_file_picgallery.gif");
		filelistTemplate.setData(C_FILELIST_NAME_VALUE, res.getName()); 
		filelistTemplate.setData(C_FILELIST_TITLE_VALUE, cms.readProperty(res.getAbsolutePath(), C_PROPERTY_TITLE)); 
	}      

	/**
     * Gets all groups for a select box
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors.
     * @exception CmsException
     */
    
	public Integer getGroups(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
		throws CmsException {
		// get all groups
		Vector groups = cms.getGroups();
		int retValue = 0;
		
		// fill the names and values 
		String prompt = lang.getDataValue("input.promptgroup"); 	
		names.addElement(prompt); 
		values.addElement("Aufforderung"); // without significance for the user
		for(int z = 0; z < groups.size(); z++) {
			String name = ((A_CmsGroup)groups.elementAt(z)).getName();
			names.addElement(name);
			values.addElement(name);
		}
		 
        return new Integer(retValue);
    } 
}

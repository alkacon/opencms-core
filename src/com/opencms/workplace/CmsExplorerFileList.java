/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsExplorerFileList.java,v $
 * Date   : $Date: 2000/07/18 16:13:50 $
 * Version: $Revision: 1.12 $
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
 * Template class for displaying the file list tree of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.12 $ $Date: 2000/07/18 16:13:50 $
 */
public class CmsExplorerFileList extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                                I_CmsConstants, I_CmsFileListUsers {    

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
    * Collects all folders and files that are displayed in the file list.
    * @param cms The CmsObject.
    * @return A vector of folder and file objects.
    * @exception Throws CmsException if something goes wrong.
    */
   public Vector getFiles(CmsObject cms) 
       throws CmsException {
       Vector filesfolders=new Vector();
  
        String foldername;
        String filelist;
        String currentFilelist;
        String currentFolder;
        // vectors to store all files and folders in the current folder.
        Vector files;
        Vector folders;
        Enumeration enum;
            
        // file and folder object required to create the filefolder list.
        CmsFile file;
        CmsFolder folder;
            
        CmsSession session= cms.getRequestContext().getSession(true);
              
         //check if a folder parameter was included in the request         
         foldername=cms.getRequestContext().getRequest().getParameter(C_PARA_FOLDER);
            if (foldername != null) {
                session.putValue(C_PARA_FOLDER,foldername);
            }

         // get the current folder to be displayed as maximum folder in the tree.
         currentFolder=(String)session.getValue(C_PARA_FOLDER);
            if (currentFolder == null) {
                 currentFolder=cms.rootFolder().getAbsolutePath();
            }
            
            
         //check if a filelist  parameter was included in the request.
         // if a filelist was included, overwrite the value in the session for later use.
            filelist=cms.getRequestContext().getRequest().getParameter(C_PARA_FILELIST);
            if (filelist != null) {
                session.putValue(C_PARA_FILELIST,filelist);
            }

        // get the current folder to be displayed as maximum folder in the tree.
            currentFilelist=(String)session.getValue(C_PARA_FILELIST);
            if (currentFilelist==null) {
                currentFilelist=cms.rootFolder().getAbsolutePath();
            }     

        // get all files and folders of the current folder
        folders=cms.getSubFolders(currentFilelist);
        files=cms.getFilesInFolder(currentFilelist);
       
        // combine both vectors
        enum=folders.elements();
        while (enum.hasMoreElements()) {
            folder = (CmsFolder)enum.nextElement();
            filesfolders.addElement(folder);
        }
        enum=files.elements();
        while (enum.hasMoreElements()) {
            file = (CmsFile)enum.nextElement();
            filesfolders.addElement(file);
        }
            
        return filesfolders;
   }

    /**
     * Fills all customized columns with the appropriate settings for the given file 
     * list entry. Any column filled by this method may be used in the customized template
     * for the file list.
     * @param cms Cms object for accessing system resources.
     * @param filelist Template file containing the definitions for the file list together with
     * the included customized defintions.
     * @param res CmsResource Object of the current file list entry.
     * @param lang Current language file.
     * @exception CmsException if access to system resources failed.
     * @see I_CmsFileListUsers
     */
    public void getCustomizedColumnValues(CmsObject cms, CmsXmlWpTemplateFile filelistTemplate, CmsResource res, CmsXmlLanguageFile lang) {
    }       

    /**
     * Used to modify the bit pattern for hiding and showing columns in
     * the file list.
     * @param cms Cms object for accessing system resources.
     * @param prefs Old bit pattern.
     * @return New modified bit pattern.
     * @see I_CmsFileListUsers
     */
    public int modifyDisplayedColumns(CmsObject cms, int prefs) {
        return prefs;
    }
   
}

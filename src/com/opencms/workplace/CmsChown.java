
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsChown.java,v $
* Date   : $Date: 2001/01/24 09:43:26 $
* Version: $Revision: 1.24 $
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
 * Template class for displaying the chown screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.24 $ $Date: 2001/01/24 09:43:26 $
 */

public class CmsChown extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {
    
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
    
    private void getAllResources(CmsObject cms, String rootFolder, Vector allFiles, 
            Vector allFolders) throws CmsException {
        Vector folders = new Vector();
        Vector files = new Vector();
        
        // get files and folders of this rootFolder
        folders = cms.getSubFolders(rootFolder);
        files = cms.getFilesInFolder(rootFolder);
        
        //copy the values into the allFiles and allFolders Vectors
        for(int i = 0;i < folders.size();i++) {
            allFolders.addElement((CmsFolder)folders.elementAt(i));
            getAllResources(cms, ((CmsFolder)folders.elementAt(i)).getAbsolutePath(), 
                    allFiles, allFolders);
        }
        for(int i = 0;i < files.size();i++) {
            allFiles.addElement((CmsFile)files.elementAt(i));
        }
    }
    
    /**
     * method to check get the real body path from the content file
     * 
     * @param cms The CmsObject, to access the XML read file.
     * @param file File in which the body path is stored.
     */
    
    private String getBodyPath(CmsObject cms, CmsFile file) throws CmsException {
        file = cms.readFile(file.getAbsolutePath());
        CmsXmlControlFile hXml = new CmsXmlControlFile(cms, file);
        return hXml.getElementTemplate("body");
    }
    
    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the chown template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The chown template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */
    
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, 
            Hashtable parameters, String templateSelector) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        
        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {
            
            // remove all session values
            session.removeValue(C_PARA_FILE);
            session.removeValue("lasturl");
        }
        
        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);
        
        // the template to be displayed
        String template = null;
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, 
                templateFile);
        String newowner = (String)parameters.get(C_PARA_NEWOWNER);
        String filename = (String)parameters.get(C_PARA_FILE);
        String flags = (String)parameters.get(C_PARA_FLAGS);
        if(flags == null) {
            flags = "false";
        }
        if(filename != null) {
            session.putValue(C_PARA_FILE, filename);
        }
        
        //check if the lock parameter was included in the request
        
        // if not, the lock page is shown for the first time
        filename = (String)session.getValue(C_PARA_FILE);
        CmsResource file = (CmsResource)cms.readFileHeader(filename);
        
        // select the template to be displayed
        if(file.isFile()) {
            template = "file";
        }
        else {
            template = "folder";
        }
        
        // a new owner was given in the request so try to change it
        if(newowner != null) {
            
            // check if the current user has the right to change the owner of the
            
            // resource. Only the owner of a file and the admin are allowed to do this.
            if((cms.getRequestContext().currentUser().equals(cms.readOwner(file))) 
                    || (cms.userInGroup(cms.getRequestContext().currentUser().getName(), C_GROUP_ADMIN))) {
                cms.chown(file.getAbsolutePath(), newowner);
                
                //check if the file type name is page
                
                //if so delete the file body and content
                
                // else delete only file
                if((cms.getResourceType(file.getType()).getResourceName()).equals(C_TYPE_PAGE_NAME)) {
                    String bodyPath = getBodyPath(cms, (CmsFile)file);
                    int help = C_CONTENTBODYPATH.lastIndexOf("/");
                    String hbodyPath = (C_CONTENTBODYPATH.substring(0, help)) 
                            + (file.getAbsolutePath());
                    if(hbodyPath.equals(bodyPath)) {
                        cms.chown(hbodyPath, newowner);
                    }
                }
                
                // if the resource is a folder, check if there is a corresponding 
                
                // directory in the content body folder
                if(file.isFolder()) {
                    String bodyFolder = C_CONTENTBODYPATH.substring(0, 
                            C_CONTENTBODYPATH.lastIndexOf("/")) + file.getAbsolutePath();
                    try {
                        cms.readFolder(bodyFolder);
                        
                        //  cms.lockResource(bodyFolder);
                        cms.chown(bodyFolder, newowner);
                    
                    //  cms.unlockResource(bodyFolder);
                    }
                    catch(CmsException ex) {
                        
                    
                    // no folder is there, so do nothing
                    }
                }
                
                // the resource was a folder and the recursive flag was set                   
                
                // do a recursive chown on all files and subfolders
                if(flags.equals("true")) {
                    
                    // get all subfolders and files
                    Vector allFolders = new Vector();
                    Vector allFiles = new Vector();
                    getAllResources(cms, filename, allFiles, allFolders);
                    
                    //cms.unlockResource(file.getAbsolutePath());
                    
                    // now modify all subfolders
                    for(int i = 0;i < allFolders.size();i++) {
                        CmsFolder folder = (CmsFolder)allFolders.elementAt(i);
                        if(folder.getState() != C_STATE_DELETED) {
                            cms.chown(folder.getAbsolutePath(), newowner);
                            
                            // check if there is a corresponding 
                            
                            // directory in the content body folder
                            String bodyFolder = C_CONTENTBODYPATH.substring(0, 
                                    C_CONTENTBODYPATH.lastIndexOf("/")) + folder.getAbsolutePath();
                            try {
                                cms.readFolder(bodyFolder);
                                cms.chown(bodyFolder, newowner);
                            }
                            catch(CmsException ex) {
                                
                            
                            // no folder is there, so do nothing
                            }
                        }
                    }
                    
                    // now modify all files in the subfolders
                    for(int i = 0;i < allFiles.size();i++) {
                        CmsFile newfile = (CmsFile)allFiles.elementAt(i);
                        if(newfile.getState() != C_STATE_DELETED) {
                            cms.chown(newfile.getAbsolutePath(), newowner);
                            if((cms.getResourceType(newfile.getType()).getResourceName()).equals(C_TYPE_PAGE_NAME)) {
                                String bodyPath = getBodyPath(cms, (CmsFile)newfile);
                                int help = C_CONTENTBODYPATH.lastIndexOf("/");
                                String hbodyPath = (C_CONTENTBODYPATH.substring(0, help)) 
                                        + (newfile.getAbsolutePath());
                                if(hbodyPath.equals(bodyPath)) {
                                    cms.chown(hbodyPath, newowner);
                                }
                            }
                        }
                    }
                }
                session.removeValue(C_PARA_FILE);
                
                // return to filelist
                try {
                    if(lasturl == null || "".equals(lasturl)) {
                        cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath() 
                                + C_WP_EXPLORER_FILELIST);
                    }
                    else {
                        cms.getRequestContext().getResponse().sendRedirect(lasturl);
                    }
                }
                catch(Exception e) {
                    throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath() 
                            + C_WP_EXPLORER_FILELIST, CmsException.C_UNKNOWN_EXCEPTION, e);
                }
                return null;
            }
            else {
                
                // the current user is not allowed to change the file owner
                xmlTemplateDocument.setData("details", "the current user is not allowed to change the file owner");
                template = "error";
                session.removeValue(C_PARA_FILE);
            }
        }
        
        // set the required datablocks
        String title = cms.readProperty(file.getAbsolutePath(), 
                C_PROPERTY_TITLE);
        if(title == null) {
            title = "";
        }
        CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
        CmsUser owner = cms.readOwner(file);
        xmlTemplateDocument.setData("TITLE", title);
        xmlTemplateDocument.setData("STATE", getState(cms, file, lang));
        xmlTemplateDocument.setData("OWNER", Utils.getFullName(owner));
        xmlTemplateDocument.setData("GROUP", cms.readGroup(file).getName());
        xmlTemplateDocument.setData("FILENAME", file.getName());
        
        // process the selected template 
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }
    
    /**
     * Gets a formated file state string.
     * @param cms The CmsObject.
     * @param file The CmsResource.
     * @param lang The content definition language file.
     * @return Formated state string.
     */
    
    private String getState(CmsObject cms, CmsResource file, CmsXmlLanguageFile lang) 
            throws CmsException {
        StringBuffer output = new StringBuffer();
        if(file.inProject(cms.getRequestContext().currentProject())) {
            int state = file.getState();
            output.append(lang.getLanguageValue("explorer.state" + state));
        }
        else {
            output.append(lang.getLanguageValue("explorer.statenip"));
        }
        return output.toString();
    }
    
    /**
     * Gets all users that can new owner of the file.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors.
     * @exception CmsException
     */
    
    public Integer getUsers(CmsObject cms, CmsXmlLanguageFile lang, Vector names, 
            Vector values, Hashtable parameters) throws CmsException {
        
        // get all groups
        Vector users = cms.getUsers();
        int retValue = -1;
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String filename = (String)session.getValue(C_PARA_FILE);
        if(filename != null) {
            CmsResource file = (CmsResource)cms.readFileHeader(filename);
            
            // fill the names and values
            for(int z = 0;z < users.size();z++) {
                String name = ((CmsUser)users.elementAt(z)).getName();
                if(cms.readOwner(file).getName().equals(name)) {
                    retValue = z;
                }
                names.addElement(name);
                values.addElement(((CmsUser)users.elementAt(z)).getName());
            }
        }
        
        // no current user, set index to -1
        return new Integer(retValue);
    }
    
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
    
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, 
            Hashtable parameters, String templateSelector) {
        return false;
    }
}

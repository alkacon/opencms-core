/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminDownGalleries.java,v $
* Date   : $Date: 2003/07/11 14:01:12 $
* Version: $Revision: 1.32 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.workplace;

import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsImportFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.I_CmsResourceType;
import com.opencms.util.Utils;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Template Class for administration of download galleries
 * <p>
 *
 * @author Mario Stanke
 * @version $Revision: 1.32 $ $Date: 2003/07/11 14:01:12 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminDownGalleries extends CmsAdminGallery {

    /** Vector containing all names of the radiobuttons */
    private Vector m_names = null;

    /** Vector containing all links attached to the radiobuttons */
    private Vector m_values = null;
    
    /**
     * This method must return the path to the gallery root folder.<p>
     * 
     * The root folder names are usually defined as constants in 
     * the I_CmsWpConstants interface.
     * 
     * @return The path to the gallery root folder
     * 
     * @see I_CmsWpConstants
     */ 
    public String getGalleryPath() {
        return C_VFS_GALLERY_DOWNLOAD;
    }
    
    /**
     * This method must return the path to the gallery icon.<p>
     * 
     * The gallery image is displayed in the list of available galleries.
     * 
     * @param cms The current CmsObject
     * @return The path to the gallery icon
     * @throws CmsException In case of problem accessing system resources
     */ 
    public String getGalleryIconPath(CmsObject cms) throws CmsException {
        CmsXmlWpConfigFile config = this.getConfigFile(cms);        
        return config.getWpPicturePath() + "ic_file_download.gif";
    }    
        
    /**
     * Default XMLTemplate method called to build the output,
     *
     * @param cms CmsObject for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template
     * @param parameters Hashtable with all template class parameters
     * @param templateSelector template section that should be processed
     * 
     * @return A HTML String converted to bytes that contains the generated output
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
                
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);

        // get the URL to which we need to return when we're done
        String lasturl = getLastUrl(cms, parameters);    
        
        // check if this is the inital call to the page
        getInitial(session, parameters)                  ;
                
        // Get the folder for the gallery
        String foldername = getGalleryPath(cms, session, parameters);
        CmsFolder thefolder = cms.readFolder(foldername);   
                        
        // Check if we must redirect to head_1
        if(foldername.equals(C_VFS_GALLERY_DOWNLOAD) && templateFile.endsWith("administration_head_downgalleries2")) {
            // we are in the wrong head - use the first one
            xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, "/system/workplace/administration/downloadgallery/administration_head_downgalleries1", elementName, parameters, templateSelector);
        }

        // Check if we must redirect to head_2
        try {
            if(foldername.startsWith(C_VFS_GALLERY_DOWNLOAD) && (thefolder.getParent().equals(C_VFS_GALLERY_PICS)) && templateFile.endsWith("administration_head_downgalleries1")) {
                // we are in the wrong head - use the second one
                xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, "/system/workplace/administration/htmlgallery/administration_head_downgalleries2", elementName, parameters, templateSelector);
            }
        }
        catch(Exception e) {}            
                
        // Now read further parameters    
        String action = (String)parameters.get("action");
        String unzip = (String) parameters.get("unzip");
        String nofolder = (String) parameters.get("NOFOLDER");
        //get the filetype
        String newtype = (String)parameters.get(C_PARA_NEWTYPE);
        if(newtype != null) {
            session.putValue(C_PARA_NEWTYPE, newtype);
        } else {
            newtype = (String)session.getValue(C_PARA_NEWTYPE);            
        }
        String newname = (String)parameters.get(C_PARA_NAME);
        String title = (String)parameters.get("TITLE"); // both for gallery and upload file
        String step = (String)parameters.get("step");
        
        String filename = null;
        if(foldername == null) {
            foldername = "";
        }
        if("new".equals(action)) {
            String galleryname = (String)parameters.get("NAME");
//            String group = (String)parameters.get("GROUP");
//            boolean read = parameters.get("READ") != null;
//            boolean write = parameters.get("WRITE") != null;
            try {

                // create the folder

                // get the path from the workplace.ini
                String superfolder = getConfigFile(cms).getDownGalleryPath();
                CmsFolder folder = (CmsFolder)cms.createResource(superfolder, galleryname, C_TYPE_FOLDER_NAME);
                cms.writeProperty(cms.readAbsolutePath(folder), C_PROPERTY_TITLE, title);
                // TODO: check how to set the appropriate access using acl 
                /*
                cms.chgrp(cms.readAbsolutePath(folder), group);
                int flag = folder.getAccessFlags();

                // set the access rights for 'other' users
                if(read != ((flag & C_ACCESS_PUBLIC_READ) != 0)) {
                    flag ^= C_ACCESS_PUBLIC_READ;
                }
                if(write != ((flag & C_ACCESS_PUBLIC_WRITE) != 0)) {
                    flag ^= C_ACCESS_PUBLIC_WRITE;
                }
                if((flag & C_ACCESS_GROUP_READ) == 0){
                    flag ^= C_ACCESS_GROUP_READ;
                }
                if((flag & C_ACCESS_GROUP_WRITE) == 0){
                    flag ^= C_ACCESS_GROUP_WRITE;
                }
                if((flag & C_ACCESS_GROUP_VISIBLE) == 0){
                    flag ^= C_ACCESS_GROUP_VISIBLE;
                }
                if((flag & C_PERMISSION_READ ) == 0){
                    flag ^= C_PERMISSION_READ;
                }
                if((flag & C_PERMISSION_WRITE) == 0){
                    flag ^= C_PERMISSION_WRITE;
                }
                if((flag & C_PERMISSION_VIEW) == 0){
                    flag ^= C_PERMISSION_VIEW;
                }
                if((flag & C_ACCESS_PUBLIC_VISIBLE) == 0){
                    flag ^= C_ACCESS_PUBLIC_VISIBLE;
                }
               cms.chmod(cms.readAbsolutePath(folder), flag);
               */
               cms.unlockResource(cms.readAbsolutePath(folder), false);
            }
            catch(CmsException ex) {
                xmlTemplateDocument.setData("ERRORDETAILS", Utils.getStackTrace(ex));
                templateSelector = "error";
            }
        }
        else {
            if("upload".equals(action)) {

                // get filename and file content if available
                byte[] filecontent = new byte[0];

                // get the filename
                Enumeration files = cms.getRequestContext().getRequest().getFileNames();
                while(files.hasMoreElements()) {
                    filename = (String)files.nextElement();
                }
                if(filename != null) {
                    session.putValue(C_PARA_FILE, filename);
                }
                filename = (String)session.getValue(C_PARA_FILE);

                // get the filecontent
                if(filename != null) {
                    filecontent = cms.getRequestContext().getRequest().getFile(filename);
                }
                if(filecontent != null) {
                    session.putValue(C_PARA_FILECONTENT, filecontent);
                }
                filecontent = (byte[])session.getValue(C_PARA_FILECONTENT);
                if("0".equals(step)) {
                    templateSelector = "";
                }
                else {
                    if("1".equals(step)) {

                        // display the select filetype screen
                        if(filename != null) {

                            // check if the file size is 0
                            if(filecontent.length == 0) {
                                templateSelector = "error";
                                xmlTemplateDocument.setData("details", filename);
                            }
                            else {
                                if(unzip != null) {
                                    // try to unzip the file here ...
                                    boolean noSubFolder = (nofolder != null ? true : false);
                                    CmsImportFolder zip = new CmsImportFolder(
                                        filecontent, foldername, cms, noSubFolder);
                                    if( zip.isValidZipFile() ) {

                                        // remove the values form the session
                                        session.removeValue(C_PARA_FILE);
                                        session.removeValue(C_PARA_FILECONTENT);
                                        session.removeValue(C_PARA_NEWTYPE);
                                        // return to the filelist
                                        try {
                                            //cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
                                            if((lasturl != null) && (lasturl != "")) {
                                                cms.getRequestContext().getResponse().sendRedirect(lasturl);
                                            }
                                            else {
                                                cms.getRequestContext().getResponse().sendCmsRedirect(
                                                    getConfigFile(cms).getWorkplaceActionPath() 
                                                    + CmsWorkplaceAction.getExplorerFileUri(cms));
                                            }
                                        } catch(Exception ex) {
                                            throw new CmsException(
                                                "Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath()
                                                + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, ex);
                                        }
                                        return null;
                                    }
                                } // else, zip was not valid, so continue ...
                                templateSelector = "step1";
                            }
                        }
                    }
                    else {
                        if("2".equals(step)) {

                            // get the selected resource and check if it is an image
                            I_CmsResourceType type = cms.getResourceType(newtype);
                            if(newtype.equals(C_TYPE_IMAGE_NAME)) {

                                // the file type is an image
                                templateSelector = "image";
                                xmlTemplateDocument.setData("MIME", filename);
                                xmlTemplateDocument.setData("SIZE", "Not yet available");
                                xmlTemplateDocument.setData("FILESIZE", new Integer(filecontent.length).toString() + " Bytes");
                            }
                            else {

                                // create the new file.
                                // todo: error handling if file already exits

                                try{
                                    cms.createResource(foldername, filename, type.getResourceTypeName(), new Hashtable(), filecontent);
                                }catch(CmsException e){
                                    // remove the values form the session
                                    session.removeValue(C_PARA_FILE);
                                    session.removeValue(C_PARA_FILECONTENT);
                                    session.removeValue(C_PARA_NEWTYPE);
                                    xmlTemplateDocument.setData("details", Utils.getStackTrace(e));
                                    return startProcessing(cms, xmlTemplateDocument, "", parameters, "error2");

                                }
                                // remove the values form the session
                                session.removeValue(C_PARA_FILE);
                                session.removeValue(C_PARA_FILECONTENT);
                                session.removeValue(C_PARA_NEWTYPE);

                                // return to the filelist
                                try {

                                    //cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
                                    if((lasturl != null) && (lasturl != "")) {
                                        cms.getRequestContext().getResponse().sendRedirect(lasturl);
                                    }
                                    else {
                                        cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath() + CmsWorkplaceAction.getExplorerFileUri(cms));
                                    }
                                }
                                catch(Exception ex) {
                                    throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath() + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, ex);
                                }
                                return null;
                            }
                        }
                        else {
                            if("3".equals(step)) {

                                // get the data from the special image upload dialog

                                // check if a new filename is given
                                if(newname != null) {
                                    filename = newname;
                                }

                                // create the new file.

                                // todo: error handling if file already exits
                                I_CmsResourceType type = cms.getResourceType(newtype);
                                Hashtable prop = new Hashtable();
                                // check if a file title was given
                                if(title != null) {
                                    prop.put(C_PROPERTY_TITLE, title);
                                }
                                cms.createResource(foldername, filename,
                                    type.getResourceTypeName(), prop, filecontent);

                                // remove the values form the session
                                session.removeValue(C_PARA_FILE);
                                session.removeValue(C_PARA_FILECONTENT);
                                session.removeValue(C_PARA_NEWTYPE);
                                session.removeValue("lasturl");

                                // return to the filelist
                                try {
                                    if((lasturl != null) && (lasturl != "")) {
                                        cms.getRequestContext().getResponse().sendRedirect(lasturl);
                                    }
                                    else {
                                        cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath() + CmsWorkplaceAction.getExplorerFileUri(cms));
                                    }
                                }
                                catch(Exception ex) {
                                    throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath() + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, ex);
                                }
                                return null;
                            }              
                        }
                    }
                }
            }
        }
 
        xmlTemplateDocument.setData("link_value", foldername);
        xmlTemplateDocument.setData("lasturl", lasturl);
        xmlTemplateDocument.setData("galleryRootFolder", C_VFS_GALLERY_DOWNLOAD);                
        
        if(filename != null) {
            xmlTemplateDocument.setData("FILENAME", filename);
        }

        // Finally start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

    /**
     * Gets the resources displayed in the Radiobutton group on the chtype dialog.
     * 
     * @param cms The CmsObject.
     * @param lang The langauge definitions.
     * @param names The names of the new rescources.
     * @param values The links that are connected with each resource.
     * @param parameters Hashtable of parameters (not used yet).
     * @param descriptions Description that will be displayed for the new resource.
     * @return The vectors names and values are filled with the information found in the workplace.ini.
     * @return the number of the preselected item, -1 if none preselected
     * @throws Throws CmsException if something goes wrong.
     */
    public int getResources(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Vector descriptions, Hashtable parameters) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String filename = (String)session.getValue(C_PARA_FILE);
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);
        suffix = suffix.toLowerCase(); // file extension of filename

        // read the known file extensions from the database
        Hashtable extensions = cms.readFileExtensions();
        String resType = new String();
        if(extensions != null) {
            resType = (String)extensions.get(suffix);
        }
        if(resType == null) {
            resType = "";
        }
        int ret = 0;

        // Check if the list of available resources is not yet loaded from the workplace.ini
        if(m_names == null || m_values == null) {
            m_names = new Vector();
            m_values = new Vector();
            CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
            configFile.getWorkplaceIniData(m_names, m_values, "RESOURCETYPES", "RESOURCE");
        }

        // Check if the temportary name and value vectors are not initialized, create
        // them if nescessary.
        if(names == null) {
            names = new Vector();
        }
        if(values == null) {
            values = new Vector();
        }
        if(descriptions == null) {
            descriptions = new Vector();
        }

        // OK. Now m_names and m_values contain all available
        // resource information.
        // Loop through the vectors and fill the result vectors.
        int numViews = m_names.size();
        for(int i = 0;i < numViews;i++) {
            String loopValue = (String)m_values.elementAt(i);
            String loopName = (String)m_names.elementAt(i);
            values.addElement(loopValue);
            names.addElement("file_" + loopName);
            String descr;
            if(lang != null) {
                descr = lang.getLanguageValue("fileicon." + loopName);
            }
            else {
                descr = loopName;
            }
            descriptions.addElement(descr);
            if(resType.equals(loopName)) {

                // known file extension
                ret = i;
            }
        }
        return ret;
    }   
}

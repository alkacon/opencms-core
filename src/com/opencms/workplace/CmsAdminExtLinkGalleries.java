/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminExtLinkGalleries.java,v $
* Date   : $Date: 2003/06/13 15:13:14 $
* Version: $Revision: 1.10 $
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
import com.opencms.file.CmsLinkCheck;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.util.Utils;

import java.util.Hashtable;

/**
 * Template Class for administration of picture galleries
 * <p>
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.10 $ $Date: 2003/06/13 15:13:14 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminExtLinkGalleries extends CmsAdminGallery  {

    /**
     * This method must be implemented by all galleries. 
     * It must return the path to the gallery root folder.<p>
     * 
     * The root folder names are usually defined as constants in 
     * the I_CmsWpConstants interface.
     * 
     * @see I_CmsWpConstants
     */ 
    public String getGalleryPath() {
        return C_VFS_GALLERY_EXTERNALLINKS;
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
        return config.getWpPicturePath() + "ic_file_extlinkgallery.gif";
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

        // Get the URL to which we need to return when we're done
        String lasturl = getLastUrl(cms, parameters);    
        
        // Check if this is the inital call to the page
        String initial = getInitial(session, parameters)                  ;
        
        // Get the folder for the gallery
        String foldername = getGalleryPath(cms, session, parameters);
        CmsFolder thefolder = cms.readFolder(foldername);        
        
        // Check if we must redirect to head_1
        if(foldername.equals(C_VFS_GALLERY_EXTERNALLINKS) && templateFile.endsWith("administration_head_extlinkgalleries2")) {
            // we are in the wrong head - use the first one
            xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, "/system/workplace/administration/externallinksgallery/administration_head_extlinkgalleries1", elementName, parameters, templateSelector);
        }
        
        // Check if we must redirect to head_2
        try {
            if(foldername.startsWith(C_VFS_GALLERY_EXTERNALLINKS) && (thefolder.getParent().equals(C_VFS_GALLERY_EXTERNALLINKS)) && templateFile.endsWith("administration_head_extlinkgalleries1")) {
                // we are in the wrong head - use the second one
                xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, "/system/workplace/administration/htmlgallery/administration_head_extlinkgalleries2", elementName, parameters, templateSelector);
            }
        }
        catch(Exception e) {}         

        // Now read further parameters
        String action = (String)parameters.get("action");                                
        String title = (String)parameters.get("TITLE"); // both for gallery and file
        String step = (String)parameters.get("step");       
        
        if(foldername == null) {
            foldername = "";
        }                
        if("new".equals(action)) {
            String galleryname = (String)parameters.get("NAME");
            String group = (String)parameters.get("GROUP");
            if(galleryname != null && group != null && galleryname != "" && group != "") {
//                boolean read = parameters.get("READ") != null;
//                boolean write = parameters.get("WRITE") != null;
                try {

                    // create the folder
                    CmsResource folder = cms.createResource(C_VFS_GALLERY_EXTERNALLINKS, galleryname, C_TYPE_FOLDER_NAME);
                    if(title != null) {
                        cms.writeProperty(folder.getAbsolutePath(), C_PROPERTY_TITLE, title);
                    }
                    cms.chgrp(folder.getAbsolutePath(), group);
                    int flag = folder.getAccessFlags();

                    // set the access rights for 'other' users
					// TODO: check how to set the appropriate access using acl
                    /*
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
                    */
                    cms.chmod(folder.getAbsolutePath(), flag);
                    cms.unlockResource(folder.getAbsolutePath());
                }
                catch(CmsException ex) {
                    xmlTemplateDocument.setData("ERRORDETAILS", Utils.getStackTrace(ex));
                    templateSelector = "error";
                }
            } else {
                templateSelector = "datamissing";
            }
        } else {
            if("newlink".equals(action)) {
                boolean checkurl = true;
                String error = "";
                if(initial != null) {
                    // remove all session values
                    session.removeValue("extlink.filename");
                    session.removeValue("extlink.linkurl");
                    session.removeValue("lasturl");
                }

                // get the parameters for the link file
                String filename = cms.getRequestContext().getRequest().getParameter(C_PARA_FILE);
                if(filename != null) {
                    session.putValue("extlink.filename", filename);
                } else {
                    // try to get the value from the session, e.g. after an error
                    filename = (String)session.getValue("extlink.filename")!=null?(String)session.getValue("extlink.filename"):"";
                }
                String link = cms.getRequestContext().getRequest().getParameter(C_PARA_LINK);
                if(link != null) {
                    session.putValue("extlink.linkurl", link);
                } else {
                    // try to get the value from the session, e.g. after an error
                    link = (String)session.getValue("extlink.linkurl")!=null?(String)session.getValue("extlink.linkurl"):"";
                }

                // set the values e.g. after an error
                xmlTemplateDocument.setData("LINKNAME", filename);
                xmlTemplateDocument.setData("LINKVALUE", link);

                // get the current phase of this wizard
                if(step != null) {
                    // step 1 - show the final selection screen
                    if(step.equals("1") || step.equals("2")) {
                        // step 1 - create the link
                        // step 2 - create the link without checking url
                        // get folder- and filename
                        foldername = (String)session.getValue(C_PARA_FOLDER);
                        if(foldername == null) {
                            foldername = cms.rootFolder().getAbsolutePath();
                        }
                        CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
                        String firstTitlePart = lang.getLanguageValue("explorer.linkto") + " " + link;
                        String type = "link";
                        // create the new file
                        Hashtable prop = new Hashtable();
                        prop.put(C_PROPERTY_TITLE, firstTitlePart);
                        try{
                            if(step.equals("1")){
                                checkurl = CmsLinkCheck.checkUrl(link);
                            }
                            if(checkurl){
                                cms.createResource(foldername, filename, type, prop, link.getBytes());
                            }
                        } catch (CmsException e){
                            error = e.getShortException();
                        }
                    }

                    if(checkurl && ("".equals(error.trim()))){
                        // remove values from session
                        session.removeValue("extlink.filename");
                        session.removeValue("extlink.linkurl");
                        // now return to appropriate filelist
                        try {
                            if(lasturl != null) {
                                cms.getRequestContext().getResponse().sendRedirect(lasturl);
                            } else {
                                cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceAdministrationPath()
                                    + "/externallinkgallery/");
                            }
                        } catch(Exception e) {
                            throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceAdministrationPath()
                                + "/externallinkgallery/", CmsException.C_UNKNOWN_EXCEPTION, e);
                        }
                        return null;
                    }
                } else {
                    session.removeValue("extlink.filename");
                    session.removeValue("extlink.linkurl");
                }

                if(lasturl == null) {
                    lasturl = CmsWorkplaceAction.getExplorerFileUri(cms);
                }
                xmlTemplateDocument.setData("lasturl", lasturl);
                // set the template for url check if check failed
                if(!checkurl){
                    xmlTemplateDocument.setData("folder", foldername);
                    session.putValue("extlink.linkurl", link);
                    session.putValue("extlink.filename", filename);
                    templateSelector = "errorcheckurl";
                }
                // for all other errors show the errorpage
                if(!"".equals(error.trim())){
                    xmlTemplateDocument.setData("errordetails", error);
                    session.putValue("extlink.linkurl", link);
                    session.putValue("extlink.filename", filename);
                    templateSelector = "error";
                }
            }
        }

        xmlTemplateDocument.setData("link_value", foldername);
        xmlTemplateDocument.setData("lasturl", lasturl);
        xmlTemplateDocument.setData("galleryRootFolder", C_VFS_GALLERY_EXTERNALLINKS);        
        
        // Finally start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters,
                templateSelector);
    }
}

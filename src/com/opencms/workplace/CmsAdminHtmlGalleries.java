/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminHtmlGalleries.java,v $
* Date   : $Date: 2004/02/04 17:18:07 $
* Version: $Revision: 1.19 $
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

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsResourceTypeFolder;
import com.opencms.util.Utils;

import java.util.Hashtable;

/**
 * Template Class for administration of html galleries
 * <p>
 *
 * @author simmeu
 * @version $Revision: 1.19 $ $Date: 2004/02/04 17:18:07 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminHtmlGalleries extends CmsAdminGallery {

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
        return C_VFS_GALLERY_HTML;
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
        return config.getWpPicturePath() + "ic_file_htmlgallery.gif";
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
        getInitial(session, parameters);   
                        
        // Get the folder for the gallery
        String foldername = getGalleryPath(cms, session, parameters);
        CmsFolder thefolder = cms.readFolder(foldername);        

        // Check if we must redirect to head_1
        if(foldername.equals(C_VFS_GALLERY_HTML) && templateFile.endsWith("administration_head_htmlgalleries2")) {
            // we are in the wrong head - use the first one
            xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, C_VFS_PATH_WORKPLACE + "administration/htmlgallery/administration_head_htmlgalleries1", elementName, parameters, templateSelector);
        }

        // Check if we must redirect to head_2
        try {
            String parent = CmsResource.getParentFolder(cms.readAbsolutePath(thefolder));
            if(foldername.startsWith(C_VFS_GALLERY_HTML) && (parent.equals(C_VFS_GALLERY_HTML)) && templateFile.endsWith("administration_head_htmlgalleries1")) {
                // we are in the wrong head - use the second one
                xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, C_VFS_PATH_WORKPLACE + "administration/htmlgallery/administration_head_htmlgalleries2", elementName, parameters, templateSelector);
            }
        }
        catch(Exception e) {}

        // Now read further parameters    
        String action = (String)parameters.get("action");
        String title = (String)parameters.get("TITLE"); // both for gallery and upload file

        if("new".equals(action)) {
            String galleryname = (String)parameters.get("NAME");
            String group = (String)parameters.get("GROUP");
            if(galleryname != null && group != null && galleryname != "" && group != "") {
//                boolean read = parameters.get("READ") != null;
//                boolean write = parameters.get("WRITE") != null;
                try {

                    // create the folder

                    // get the path from the workplace.ini
                    String superfolder = getConfigFile(cms).getHtmlGalleryPath();
                    CmsResource folder = cms.createResource(superfolder, galleryname, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
                    if(title != null) {
                        cms.writeProperty(cms.readAbsolutePath(folder), C_PROPERTY_TITLE, title);
                    }
//                  TODO: check how to set the appropriate access using acl
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
                    try {
                      cms.unlockResource(cms.readAbsolutePath(folder), false);
                    }
                    catch (CmsException e) {
                      String parent = CmsResource.getParentFolder(cms.readAbsolutePath(folder));
                      cms.unlockResource(parent, false);
                      cms.unlockResource(cms.readAbsolutePath(folder), false);

                    }
                }
                catch(CmsException ex) {
                    xmlTemplateDocument.setData("ERRORDETAILS", CmsException.getStackTraceAsString(ex));
                    templateSelector = "error";
                }
            }
            else {
                templateSelector = "datamissing";
            }
        }

		else if ("snippet".equalsIgnoreCase(action)) {
			if (foldername != null) {
				String filename = (String) parameters.get("NEUNAME");
				String pagetitle = (String) parameters.get("NEUTITEL");
				String type = (String) parameters.get("type");

				if (filename != null && !"".equals(filename)) {
					// create the new file
					Hashtable prop = new Hashtable();
					prop.put(C_PROPERTY_TITLE, pagetitle);
					cms.createResource(foldername, filename, cms.getResourceTypeId(type), prop, new byte[0]);
				}
			}
		}

        xmlTemplateDocument.setData("link_value", foldername);
        xmlTemplateDocument.setData("lasturl", lasturl);
        xmlTemplateDocument.setData("galleryRootFolder", C_VFS_GALLERY_HTML);

        // Finally start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters,
                templateSelector);
    }
}

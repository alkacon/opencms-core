/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/workplace/Attic/CmsAdminGallery.java,v $
 * Date   : $Date: 2005/06/21 15:49:59 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
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
 *
 * First created on 16. October 2002
 */

package com.opencms.workplace;

import org.opencms.file.CmsFolder;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.I_CmsWpConstants;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.A_CmsXmlContent;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * This class provides some common functions and methods shared among all 
 * workplace gallery implementations.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public abstract class CmsAdminGallery extends CmsWorkplaceDefault implements I_CmsFileListUsers {
     
     /**
      * This method checks if C_PARA_INITIAL is present with the requst parameters 
      * and clears the session variables if this it true,
      * 
      * @return The value of the parameter C_PARA_INITIAL
      */           
     public String getInitial(I_CmsSession session, Hashtable parameters) {
        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {
            // remove all session values
            session.removeValue(C_PARA_FOLDER);
            session.removeValue("lasturl");
            session.removeValue("lastgallery");
            session.removeValue("galleryRootFolder");
        }       
        return initial; 
     }           
           
    /**
     * This methods looks up the folder information for the gallery.
     * It also sets some required session information.
     * 
     * @param cms The current CmsObject
     * @param session The current session
     * @param parameters The current set of request parameters
     * @return The path to the current gallery 
     */
    public String getGalleryPath(CmsObject cms, I_CmsSession session, Hashtable parameters) {          
        // read the parameters
        String foldername = (String)parameters.get(C_PARA_FOLDER);
        String galleryPath = getGalleryPath();

        if (foldername != null) {
            try {
                CmsFolder fold = cms.readFolder(foldername);
                String parent = CmsResource.getParentFolder(cms.getSitePath(fold));
                if (!(parent.equals(galleryPath))) {
                    foldername = galleryPath;
                }
                if (fold.getState() == C_STATE_DELETED) {
                    foldername = galleryPath;
                }
            } catch (CmsException exc) {
                // couldn't read the folder - switch to default path
                foldername = galleryPath;
            }
            session.putValue("lastgallery", foldername);
            parameters.put(C_PARA_FOLDER, foldername);            
        } else {
            foldername = (String) session.getValue(C_PARA_FOLDER);
            String tmpFolder = (String) session.getValue("lastgallery");

            if (foldername == null) {

                if (tmpFolder != null) {
                    try {
                        // check if tmpfolder exists
                        cms.readFolder(tmpFolder);
                        foldername = tmpFolder;
                    } catch (CmsException e) {
                        foldername = galleryPath;
                    }
                } else {
                    foldername = galleryPath;
                }
            }
        }

        // need the foldername in the session in case of an exception in the dialog
        session.putValue(C_PARA_FOLDER, foldername); 
        return foldername;              
    }
    
    /**
     * This method is reuired by the XMLTemplate mechanism to indicate 
     * if the results of this class are cacheable.
     * This is always false for all galleries.
     *
     * @param cms CmsObject for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template
     * @param parameters Hashtable with all template class parameters
     * @param templateSelector template section that should be processed
     * @return Always false for all gelleries
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }    
       
    /**
     * From interface <code>I_CmsFileListUsers</code><p>
     * 
     * Used to modify the bit pattern for hiding and showing columns in
     * the file list.
     * This is usually the same for all galleries.
     * 
     * @param cms CmsObject for accessing system resources
     * @param prefs Old bit pattern
     * @return New modified bit pattern
     * 
     * @see I_CmsFileListUsers
     */

    public int modifyDisplayedColumns(CmsObject cms, int prefs) {
        prefs = ((prefs & C_FILELIST_NAME) == 0) ? prefs : (prefs - C_FILELIST_NAME);
        prefs = ((prefs & C_FILELIST_TITLE) == 0) ? prefs : (prefs - C_FILELIST_TITLE);
        prefs = ((prefs & C_FILELIST_TYPE) == 0) ? prefs : (prefs - C_FILELIST_TYPE);
        prefs = ((prefs & C_FILELIST_SIZE) == 0) ? prefs : (prefs - C_FILELIST_SIZE);
        return prefs;
    }       

    /**
     * Gets all groups for the select box in the "create a new gallery" dialog.<p>
     * 
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.<p>
     * 
     * This functionality is usually the same for all galleries.
     *
     * @param cms CmsObject for accessing system resources
     * @param names Vector to be filled with the appropriate values in this method
     * @param values Vector to be filled with the appropriate values in this method
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors
     * @throws CmsException In case there were problems accessing the language resources
     */
    public Integer getGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        // get all groups
        List groups = cms.getGroups();
        int retValue = 0;

        // fill the names and values
        String prompt = lang.getLanguageValue("input.promptgroup");
        names.addElement(prompt);
        values.addElement("Aufforderung"); // without significance for the user
        for(int z = 0;z < groups.size();z++) {
            String name = ((CmsGroup)groups.get(z)).getName();
            if(! OpenCms.getDefaultUsers().getGroupGuests().equals(name)){
                names.addElement(name);
                values.addElement(name);
            }
        }
        return new Integer(retValue);
    }
    
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
    public abstract String getGalleryPath();
    
    /**
     * Collect all folders and files that should be displayed in the 
     * list of galleries of this gallery type.<p>
     * 
     * @param cms The current CmsObject
     * @return A vector of folder and file objects
     * @throws CmsException In case of trouble accessing the VFS
     * 
     * @see I_CmsFileListUsers
     */
    public List getFiles(CmsObject cms) throws CmsException {
        List galleries = (List) new ArrayList();
        List folders = cms.getSubFolders(getGalleryPath());
        int numFolders = folders.size();
        for(int i = 0;i < numFolders;i++) {
            CmsResource currFolder = (CmsResource)folders.get(i);
            galleries.add(currFolder);
        }
        return galleries;
    }
    
    /**
     * This method is used in the gallery templates in the onLoad Javascript
     * page handler. It returns a location string to the current folder.
     */    
    public Object onLoad(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        Hashtable parameters = (Hashtable) userObj;
        String folder = (String)parameters.get("folder");

        if(folder != null) {
            String servletUrl = CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getServletUrl();
            return "window.top.body.admin_content.location.href='" + servletUrl + C_VFS_PATH_WORKPLACE + "action/explorer_files.html?mode=listonly&folder=" + folder + "'";
        } else {
            return "";
        }
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
    public abstract String getGalleryIconPath(CmsObject cms) throws CmsException;
    
    /**
     * This method builds the customized gallery file list.<p>
     * 
     * @param cms CmsObject for accessing system resources
     * @param filelist Template file containing the definitions for the file list together with
     *   the included customized defintions
     * @param res CmsResource Object of the current file list entry
     * @param lang Current language file
     * @throws CmsException if access to system resources failed.
     * 
     * @see I_CmsFileListUsers
     */
    public void getCustomizedColumnValues(CmsObject cms, CmsXmlWpTemplateFile filelistTemplate,
            CmsResource res, CmsXmlLanguageFile lang) throws CmsException {
        getConfigFile(cms);
        filelistTemplate.fastSetXmlData(C_FILELIST_ICON_VALUE,          
            CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getServletUrl() + getGalleryIconPath(cms) );
        filelistTemplate.setData(C_FILELIST_NAME_VALUE, res.getName());
        filelistTemplate.setData(C_FILELIST_TITLE_VALUE, cms.readProperty(cms.getSitePath(res),
                CmsPropertyDefinition.PROPERTY_TITLE));
    }    
                    
}

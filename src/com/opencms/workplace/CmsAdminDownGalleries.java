/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminDownGalleries.java,v $
* Date   : $Date: 2001/07/31 15:50:17 $
* Version: $Revision: 1.18 $
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

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import java.util.*;
import java.io.*;
import javax.servlet.http.*;

/**
 * Template Class for administration of download galleries
 * <p>
 *
 * @author Mario Stanke
 * @version $Revision: 1.18 $ $Date: 2001/07/31 15:50:17 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminDownGalleries extends CmsWorkplaceDefault implements I_CmsConstants,I_CmsFileListUsers {

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters.
     *
     * @see getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {

            // remove all session values
            session.removeValue(C_PARA_FOLDER);
        }

        // getting the URL to which we need to return when we're done
        String lasturl = getLastUrl(cms, parameters);

        // read the parameters
        String foldername = (String)parameters.get(C_PARA_FOLDER);
        if(foldername != null) {
            try {
                CmsFolder fold = cms.readFolder(foldername);
                if(!(fold.getParent().equals("/download/"))) {
                    foldername = "/download/";
                }
                if(fold.getState() == C_STATE_DELETED) {
                    foldername = "/download/";
                }
            } catch(CmsException exc) {
                // couldn't read the folder - switch to /download/
                foldername = "/download/";
            }

            parameters.put(C_PARA_FOLDER, foldername);

            // need the foldername in the session in case of an exception in the dialog
            session.putValue(C_PARA_FOLDER, foldername);

            // maybe we have to redirect to head_1
            if(foldername.equals("/download/") && templateFile.endsWith("administration_head_downgalleries2")) {
                // we are in the wrong head - use the first one
                xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, "/system/workplace/administration/downloadgallery/administration_head_downgalleries1", elementName, parameters, templateSelector);
            }
        }
        else {
            foldername = (String)session.getValue(C_PARA_FOLDER);
        }
        String action = (String)parameters.get("action");
        String newname = (String)parameters.get(C_PARA_NAME);
        String title = (String)parameters.get("TITLE"); // both for gallery and upload file
        String step = (String)parameters.get("step");
        if(foldername == null) {
            foldername = "";
        }
        if("new".equals(action)) {
            String galleryname = (String)parameters.get("NAME");
            String group = (String)parameters.get("GROUP");
            boolean read = parameters.get("READ") != null;
            boolean write = parameters.get("WRITE") != null;
            try {

                // create the folder

                // get the path from the workplace.ini
                String superfolder = getConfigFile(cms).getDownGalleryPath();
                CmsFolder folder = cms.createFolder(superfolder, galleryname);
                cms.writeProperty(folder.getAbsolutePath(), C_PROPERTY_TITLE, title);
                cms.chgrp(folder.getAbsolutePath(), group);
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
                if((flag & C_ACCESS_OWNER_READ ) == 0){
                    flag ^= C_ACCESS_OWNER_READ;
                }
                if((flag & C_ACCESS_OWNER_WRITE) == 0){
                    flag ^= C_ACCESS_OWNER_WRITE;
                }
                if((flag & C_ACCESS_OWNER_VISIBLE) == 0){
                    flag ^= C_ACCESS_OWNER_VISIBLE;
                }
                if((flag & C_ACCESS_PUBLIC_VISIBLE) == 0){
                    flag ^= C_ACCESS_PUBLIC_VISIBLE;
                }
               cms.chmod(folder.getAbsolutePath(), flag);
               cms.unlockResource(folder.getAbsolutePath());
            }
            catch(CmsException ex) {
                xmlTemplateDocument.setData("ERRORDETAILS", Utils.getStackTrace(ex));
                templateSelector = "error";
            }
        }
        else {
            if("upload".equals(action)) {

                // get filename and file content if available
                String filename = null;
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
                                xmlTemplateDocument.setData("MIME", filename);
                                xmlTemplateDocument.setData("SIZE", "Not yet available");
                                xmlTemplateDocument.setData("FILESIZE", new Integer(filecontent.length).toString() + " Bytes");
                                xmlTemplateDocument.setData("FILENAME", filename);
                                templateSelector = "step1";
                            }
                        }
                    }
                    else {
                        if("2".equals(step)) {

                            // check if a new filename is given
                            if(newname != null) {
                                filename = newname;
                            }
                            CmsFile file = cms.createFile(foldername, filename, filecontent, C_TYPE_PLAIN_NAME);
                            if(title != null) {
                                String filepath = file.getAbsolutePath();
                                cms.writeProperty(filepath, C_PROPERTY_TITLE, title);
                            }
                            try {
                                cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath() + lasturl);
                            }
                            catch(Exception ex) {
                                throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath()
                                        + lasturl, CmsException.C_UNKNOWN_EXCEPTION, ex);
                            }
                        }
                    }
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

    public void getCustomizedColumnValues(CmsObject cms, CmsXmlWpTemplateFile filelistTemplate,
            CmsResource res, CmsXmlLanguageFile lang) throws CmsException {
        CmsXmlWpConfigFile config = this.getConfigFile(cms);
        filelistTemplate.fastSetXmlData(C_FILELIST_ICON_VALUE, cms.getRequestContext().getRequest().getServletUrl() + config.getWpPicturePath() + "ic_file_download.gif");
        filelistTemplate.setData(C_FILELIST_NAME_VALUE, res.getName());
        String title = cms.readProperty(res.getAbsolutePath(), C_PROPERTY_TITLE);
        filelistTemplate.setData(C_FILELIST_TITLE_VALUE, title);
    }

    /**
     * From interface <code>I_CmsFileListUsers</code>.
     * <P>
     * Collects all folders and files that are displayed in the file list.
     * @param cms The CmsObject.
     * @return A vector of folder and file objects.
     * @exception Throws CmsException if something goes wrong.
     */

    public Vector getFiles(CmsObject cms) throws CmsException {
        Vector galleries = new Vector();
        Vector folders = cms.getSubFolders(getConfigFile(cms).getDownGalleryPath());
        int numFolders = folders.size();
        for(int i = 0;i < numFolders;i++) {
            CmsResource currFolder = (CmsResource)folders.elementAt(i);
            galleries.addElement(currFolder);
        }
        return galleries;
    }

    /**
     * Gets all groups for a select box
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

    public Integer getGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values,
             Hashtable parameters) throws CmsException {

        // get all groups
        Vector groups = cms.getGroups();
        int retValue = 0;

        // fill the names and values
        String prompt = lang.getDataValue("input.promptgroup");
        names.addElement(prompt);
        values.addElement("Aufforderung"); // without significance for the user
        for(int z = 0;z < groups.size();z++) {
            String name = ((CmsGroup)groups.elementAt(z)).getName();
            if(! C_GROUP_GUEST.equals(name)){
                names.addElement(name);
                values.addElement(name);
            }
        }
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

    public int modifyDisplayedColumns(CmsObject cms, int prefs) {
        prefs = ((prefs & C_FILELIST_NAME) == 0) ? prefs : (prefs - C_FILELIST_NAME);
        prefs = ((prefs & C_FILELIST_TITLE) == 0) ? prefs : (prefs - C_FILELIST_TITLE);
        prefs = ((prefs & C_FILELIST_TYPE) == 0) ? prefs : (prefs - C_FILELIST_TYPE);
        prefs = ((prefs & C_FILELIST_SIZE) == 0) ? prefs : (prefs - C_FILELIST_SIZE);
        return prefs;
    }

    public Object onLoad(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        Hashtable parameters = (Hashtable) userObj;
        String folder = (String)parameters.get("folder");

        if(folder != null) {
            String servletUrl = cms.getRequestContext().getRequest().getServletUrl() + "/";
            return "window.top.body.admin_content.location.href='" + servletUrl + "system/workplace/action/explorer_files.html?mode=listonly&folder=" + folder + "'";
        } else {
            return "";
        }
    }
}

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminHtmlGalleries.java,v $
* Date   : $Date: 2002/06/10 15:29:57 $
* Version: $Revision: 1.1 $
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
 * Template Class for administration of html galleries
 * <p>
 *
 * @author simmeu
 * @version $Revision: 1.1 $ $Date: 2002/06/10 15:29:57 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminHtmlGalleries extends CmsWorkplaceDefault implements I_CmsConstants,I_CmsFileListUsers {






    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

/*
            Enumeration pkeys = parameters.keys();
            Enumeration pvalues = parameters.elements();

            System.err.println("-----------------------------------------------");
            System.err.println("-------------Parameters--------------");
            while(pkeys.hasMoreElements()) {
              System.err.print("Key: " + pkeys.nextElement());
              System.err.print(" --- ");
              System.err.println("Value: " + pvalues.nextElement());
            }
            System.err.println("-----------------------------------------------");

*/


        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);


        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {

            // remove all session values
            session.removeValue(C_PARA_FOLDER);
            session.removeValue("lasturl");
        }


        // read the parameters
        String foldername = (String)parameters.get(C_PARA_FOLDER);

        if(foldername != null) {
          String htmlRootFolder = getConfigFile(cms).getHtmlGalleryPath();
            try {
                CmsFolder fold = cms.readFolder(foldername);
                if(!(fold.getParent().equals(htmlRootFolder))) {
                    foldername = htmlRootFolder;
                }
                if(fold.getState() == C_STATE_DELETED) {
                    foldername = htmlRootFolder;
                }
            } catch(CmsException exc) {
                // couldn't read the folder - switch to /htmlgalleries/
                foldername = htmlRootFolder;
            }
          session.putValue("lastgallery",foldername);
        }
        else {
            foldername = (String)session.getValue(C_PARA_FOLDER);
            String tmpFolder = (String)session.getValue("lastgallery") ;

            if(foldername == null) {

              if(tmpFolder != null) {
                try {
                  CmsFolder testfolder = cms.readFolder(tmpFolder);
                  foldername = tmpFolder;
                }
                catch(CmsException e) {
                  foldername = getConfigFile(cms).getHtmlGalleryPath();
                }
              }
              else {
                foldername = getConfigFile(cms).getHtmlGalleryPath();
              }
            }
        }

        parameters.put(C_PARA_FOLDER, foldername);

        // need the foldername in the session in case of an exception in the dialog
        session.putValue(C_PARA_FOLDER, foldername);

        CmsFolder thefolder = cms.readFolder(foldername);


        // maybe we have to redirect to head_1
        if(foldername.equals("/htmlgalleries/") && templateFile.endsWith("administration_head_htmlgalleries2")) {
            // we are in the wrong head - use the first one
            xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, "/system/workplace/administration/htmlgallery/administration_head_htmlgalleries1", elementName, parameters, templateSelector);
        }

        // maybe we have to redirect to head_2
        try {
            if(foldername.startsWith("/htmlgalleries/") && (thefolder.getParent().equals("/htmlgalleries/")) && templateFile.endsWith("administration_head_htmlgalleries1")) {
                // we are in the wrong head - use the second one
                xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, "/system/workplace/administration/htmlgallery/administration_head_htmlgalleries2", elementName, parameters, templateSelector);
            }
        }
        catch(Exception e) {}

        // getting the URL to which we need to return when we're done
        String lasturl = getLastUrl(cms, parameters);

        String action = (String)parameters.get("action");
        String newname = (String)parameters.get(C_PARA_NAME);
        String title = (String)parameters.get("TITLE"); // both for gallery and upload file
        String step = (String)parameters.get("step");
        String imagedescription = (String)parameters.get("DESCRIPTION");


        if("new".equals(action)) {
            String galleryname = (String)parameters.get("NAME");
            String group = (String)parameters.get("GROUP");
            if(galleryname != null && group != null && galleryname != "" && group != "") {
                boolean read = parameters.get("READ") != null;
                boolean write = parameters.get("WRITE") != null;
                try {

                    // create the folder

                    // get the path from the workplace.ini
                    String superfolder = getConfigFile(cms).getHtmlGalleryPath();
                    CmsResource folder = cms.createResource(superfolder, galleryname, C_TYPE_FOLDER_NAME);
                    if(title != null) {
                        cms.writeProperty(folder.getAbsolutePath(), C_PROPERTY_TITLE, title);
                    }
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

                    try {
                      cms.unlockResource(folder.getAbsolutePath());
                    }
                    catch (CmsException e) {

                      cms.unlockResource(folder.getParent());
                      cms.unlockResource(folder.getAbsolutePath());

                    }
                }
                catch(CmsException ex) {
                    xmlTemplateDocument.setData("ERRORDETAILS", Utils.getStackTrace(ex));
                    templateSelector = "error";
                }
            }
            else {
                templateSelector = "datamissing";
            }
        }

        else if("snippet".equalsIgnoreCase(action)) {
          if(foldername != null) {
            String filename = (String)parameters.get("NEUNAME");
            String pagetitle = (String)parameters.get("NEUTITEL");
            String type = (String)parameters.get("type");

            if(filename != null && !"".equals(filename)) {
              // create the new file
              Hashtable prop = new Hashtable();
              prop.put(C_PROPERTY_TITLE, pagetitle);
              cms.createResource(foldername, filename, type, prop, new byte[0]);
            }
/*
            try {
              cms.getRequestContext().getResponse().sendRedirect(lasturl);
            }
            catch(IOException e) {
              // no redirect possible
            }
            */
          }
        }


        xmlTemplateDocument.setData("link_value", foldername);
        xmlTemplateDocument.setData("lasturl", lasturl);

        xmlTemplateDocument.setData("htmlGalleryRootFolder", getConfigFile(cms).getHtmlGalleryPath());


        // Finally start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters,
                templateSelector);
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
        filelistTemplate.fastSetXmlData(C_FILELIST_ICON_VALUE,  cms.getRequestContext().getRequest().getServletUrl() + config.getWpPicturePath()
                + "ic_file_htmlgallery.gif" );
        filelistTemplate.setData(C_FILELIST_NAME_VALUE, res.getName());
        filelistTemplate.setData(C_FILELIST_TITLE_VALUE, cms.readProperty(res.getAbsolutePath(),
                C_PROPERTY_TITLE));
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
        Vector folders = cms.getSubFolders(getConfigFile(cms).getPicGalleryPath());
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

    public Integer getGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

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

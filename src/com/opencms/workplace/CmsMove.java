/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsMove.java,v $
* Date   : $Date: 2003/06/10 16:21:29 $
* Version: $Revision: 1.52 $
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
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.util.Encoder;
import com.opencms.util.Utils;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Template class for displaying the move file screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @author Michaela Schleich
 * @version $Revision: 1.52 $ $Date: 2003/06/10 16:21:29 $
 */

public class CmsMove extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the move template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The move template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @throws Throws CmsException if something goes wrong.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,
                templateFile);
        CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();

        // the template to be displayed
        String template = null;

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {

            // remove all session values
            session.removeValue(C_PARA_FILE);
            session.removeValue(C_PARA_NEWFOLDER);
            session.removeValue(C_PARA_FLAGS);
            session.removeValue("lasturl");
        }

        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);

        // get the file to be copied
        String filename = (String)parameters.get(C_PARA_FILE);
        if(filename != null) {
            session.putValue(C_PARA_FILE, filename);
        }
        filename = (String)session.getValue(C_PARA_FILE);
        CmsResource file = (CmsResource)cms.readFileHeader(filename);

        // read all request parameters
        String newFolder = new String();
        String newFile = new String();
        String wholePath = (String)parameters.get(C_PARA_NEWFOLDER);

        // the wholePath includes the folder and/or the filename
        if(wholePath != null && !("".equals(wholePath))){
            if(wholePath.startsWith("/")){
                // get the foldername
                newFolder = wholePath.substring(0, wholePath.lastIndexOf("/")+1);
                newFile = wholePath.substring(wholePath.lastIndexOf("/")+1);
                if (newFile == null || "".equals(newFile)){
                    newFile = file.getName();
                }
            } else {
                newFolder = file.getParent();
                newFile = wholePath;
            }
        }
        if(newFolder != null && !("".equals(newFolder))) {
            session.putValue(C_PARA_NEWFOLDER, newFolder);
        }
        newFolder = (String)session.getValue(C_PARA_NEWFOLDER);

        if(newFile != null && !("".equals(newFile))) {
            session.putValue(C_PARA_NEWFILE, newFile);
        }
        newFile = (String)session.getValue(C_PARA_NEWFILE);

        String flags = (String)parameters.get(C_PARA_FLAGS);
        if(flags != null) {
            session.putValue(C_PARA_FLAGS, flags);
        }
        flags = (String)session.getValue(C_PARA_FLAGS);
        String action = (String)parameters.get("action");
        // modify the folderaname if nescessary (the root folder is always given
        // as a nice name)
        if(newFolder != null && !("".equals(newFolder))) {
            if(newFolder.equals(lang.getLanguageValue("title.rootfolder"))) {
                newFolder = "/";
            }

            // ednfal: check if the user try to move the resource into itself
            if(newFolder.equals(file.getAbsolutePath())) {
                 // something went wrong, so remove all session parameters
                session.removeValue(C_PARA_FILE);
                session.removeValue(C_PARA_NEWFOLDER);
                session.removeValue(C_PARA_FLAGS);
                template = "error";
                xmlTemplateDocument.setData("details", "Can't move folder into itself");
                xmlTemplateDocument.setData("lasturl", lasturl);
                return startProcessing(cms, xmlTemplateDocument, "", parameters,
                            template);
            }

            // ednfal: try to read the destination folder
            try {
                cms.readFolder(newFolder);
            }
            catch(CmsException ex) {
                // something went wrong, so remove all session parameters
                session.removeValue(C_PARA_FILE);
                session.removeValue(C_PARA_NEWFOLDER);
                session.removeValue(C_PARA_FLAGS);

                template = "error";
                if(ex.getType() == CmsException.C_NOT_FOUND) {
                    xmlTemplateDocument.setData("details", "Destination folder not exists"+ex.getStackTraceAsString());
                } else {
                    xmlTemplateDocument.setData("details", ex.getStackTraceAsString());
                }
                xmlTemplateDocument.setData("lasturl", lasturl);
                return startProcessing(cms, xmlTemplateDocument, "", parameters,
                                template);
            }
        }

        // select the template to be displayed
        if(file.isFile()) {
            template = "file";
        }
        else {
            template = "folder";
        }

        //check if the newFolder parameter was included in the request
        //if not, the move page is shown for the first time
        if(newFolder != null && !("".equals(newFolder))) {
            if(action == null) {
                template = "wait";
            }
            else {
                if(file.isFile()) {

                    // this is a file, so move it
                    try {
                        cms.moveResource(((CmsFile)file).getAbsolutePath(), newFolder + newFile);
                    }
                    catch(CmsException ex) {

                        // something went wrong, so remove all session parameters
                        session.removeValue(C_PARA_FILE);
                        session.removeValue(C_PARA_NEWFOLDER);
                        session.removeValue(C_PARA_FLAGS);

                        //throw ex;
                        template = "error";
                        xmlTemplateDocument.setData("details", ex.getStackTraceAsString());
                        xmlTemplateDocument.setData("lasturl", lasturl);
                        return startProcessing(cms, xmlTemplateDocument, "", parameters,
                                template);
                    }

                    // everything is done, so remove all session parameters
                    session.removeValue(C_PARA_FILE);
                    session.removeValue(C_PARA_NEWFOLDER);
                    session.removeValue(C_PARA_FLAGS);

                    // return to the calling page
                    try {
                        if(lasturl == null || "".equals(lasturl)) {
                            cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath() + C_WP_EXPLORER_FILELIST);
                        }
                        else {
                            cms.getRequestContext().getResponse().sendRedirect(lasturl);
                        }
                    }
                    catch(Exception e) {
                        throw new CmsException("Redirect fails :"
                                + getConfigFile(cms).getWorkplaceActionPath()
                                + C_WP_EXPLORER_FILELIST, CmsException.C_UNKNOWN_EXCEPTION, e);
                    }
                    return null;
                }
                else {
                    // this is a folder
                    // get all subfolders and files
                    try {
                        cms.moveResource(((CmsFolder)file).getAbsolutePath(), newFolder + newFile);
                    } catch(CmsException e) {
                        // something went wrong, so remove all session parameters
                        session.removeValue(C_PARA_FILE);
                        session.removeValue(C_PARA_NEWFOLDER);
                        session.removeValue(C_PARA_FLAGS);

                        //throw e;
                        template = "error";
                        xmlTemplateDocument.setData("details", e.getStackTraceAsString());
                        xmlTemplateDocument.setData("lasturl", lasturl);
                        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
                    }

                    // everything is done, so remove all session parameters
                    session.removeValue(C_PARA_FILE);
                    session.removeValue(C_PARA_NEWFOLDER);
                    session.removeValue(C_PARA_FLAGS);
                    xmlTemplateDocument.setData("lasturl", lasturl);
                    template = "update";
                }
            }
        }

        // set the required datablocks
        if(action == null) {
            String title = cms.readProperty(file.getAbsolutePath(), C_PROPERTY_TITLE);
            if(title == null) {
                title = "";
            }
//			TODO fix this later
            // CmsUser owner = cms.readOwner(file);
            xmlTemplateDocument.setData("TITLE", Encoder.escapeXml(title));
            xmlTemplateDocument.setData("STATE", getState(cms, file, lang));
            xmlTemplateDocument.setData("OWNER", "" /* Utils.getFullName(owner) */);
            xmlTemplateDocument.setData("GROUP", "" /* cms.readGroup(file).getName() */);
            xmlTemplateDocument.setData("FILENAME", file.getName());
        }

        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }

    /**
     * Gets all folders to move the selected file to.
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
     * @param cms CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the available folders.
     * @throws CmsException
     */

    public Integer getFolder(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws CmsException {
        Integer selected = new Integer(0);
        // get the root folder
        CmsFolder rootFolder = cms.rootFolder();
        // add the root folder
        names.addElement(lang.getLanguageValue("title.rootfolder"));
        values.addElement("/");
        getTree(cms, rootFolder, names, values);
        return selected;
    }

    /**
     * Gets a formated file state string.
     * @param cms The CmsObject.
     * @param file The CmsResource.
     * @param lang The content definition language file.
     * @return Formated state string.
     */

    private String getState(CmsObject cms, CmsResource file, CmsXmlLanguageFile lang) throws CmsException {
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
     * Gets all folders of the filesystem. <br>
     * This method is used to create the selecebox for selecting the target directory.
     * @param cms The CmsObject.
     * @param root The root folder for the tree to be displayed.
     * @param names Vector for storing all names needed in the selectbox.
     * @param values Vector for storing values needed in the selectbox.
     */

    private void getTree(CmsObject cms, CmsFolder root, Vector names, Vector values) throws CmsException {
        Vector folders = cms.getSubFolders(root.getAbsolutePath());
        CmsProject currentProject = cms.getRequestContext().currentProject();
        Enumeration enu = folders.elements();
        while(enu.hasMoreElements()) {
            CmsFolder folder = (CmsFolder)enu.nextElement();

            // check if the current folder is part of the current project
            if(folder.inProject(currentProject)) {
                String name = folder.getAbsolutePath();
                name = name.substring(1, name.length() - 1);
                names.addElement(name);
                values.addElement(folder.getAbsolutePath());
            }
            getTree(cms, folder, names, values);
        }
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

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsChgrp.java,v $
* Date   : $Date: 2002/12/06 23:16:46 $
* Version: $Revision: 1.27 $
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
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.template.CmsXmlControlFile;
import com.opencms.util.Utils;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Template class for displaying the chgrp screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.27 $ $Date: 2002/12/06 23:16:46 $
 */

public class CmsChgrp extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {

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
     * Gets the content of the chgrp template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The chgrp template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);

        // the template to be displayed
        String template = null;
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {

            // remove all session values
            session.removeValue(C_PARA_FILE);
            session.removeValue("lasturl");
        }

        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);
        String newgroup = (String)parameters.get(C_PARA_NEWGROUP);
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
        if(newgroup != null) {

            // check if the current user has the right to change the group of the
            // resource. Only the owner of a file and the admin are allowed to do this.
            if((cms.getRequestContext().currentUser().equals(cms.readOwner(file)))
                    || (cms.userInGroup(cms.getRequestContext().currentUser().getName(),
                    C_GROUP_ADMIN))) {

                boolean rekursive = false;
                // if the resource is a folder, check if there is a corresponding
                if(file.isFolder() && flags.equals("true")) {
                    rekursive = true;
                }
                cms.chgrp(file.getAbsolutePath(), newgroup,rekursive);

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
                xmlTemplateDocument.setData("lasturl", lasturl);
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
     * Gets all groups that can new group of the resource.
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
        int retValue = -1;
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String filename = (String)session.getValue(C_PARA_FILE);
        if(filename != null) {
            CmsResource file = (CmsResource)cms.readFileHeader(filename);

            // fill the names and values
            for(int z = 0;z < groups.size();z++) {
                String name = ((CmsGroup)groups.elementAt(z)).getName();
                if(cms.readGroup(file).getName().equals(name)) {
                    retValue = z;
                }
                names.addElement(name);
                values.addElement(((CmsGroup)groups.elementAt(z)).getName());
            }
        }

        // no current user, set index to -1
        return new Integer(retValue);
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

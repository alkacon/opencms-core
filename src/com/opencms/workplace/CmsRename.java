/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsRename.java,v $
* Date   : $Date: 2003/07/31 13:19:36 $
* Version: $Revision: 1.53 $
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
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.util.Encoder;
import com.opencms.util.Utils;

import java.util.Hashtable;

/**
 * Template class for displaying the rename screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @author Michaela Schleich
 * @version $Revision: 1.53 $ $Date: 2003/07/31 13:19:36 $
 */

public class CmsRename extends CmsWorkplaceDefault {

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the rename template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The lock template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @throws Throws CmsException if something goes wrong.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // the template to be displayed
        String template = null;

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {

            // remove all session values
            session.removeValue(C_PARA_RESOURCE);
            session.removeValue(C_PARA_NAME);
            session.removeValue("lasturl");
        }

        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);

        String filename = (String)parameters.get(C_PARA_RESOURCE);
        if(filename != null) {
            session.putValue(C_PARA_RESOURCE, filename);
        }
        filename = (String)session.getValue(C_PARA_RESOURCE);
        String newFile = (String)parameters.get(C_PARA_NAME);
        if(session.getValue(C_PARA_NAME) != null) {
            if(newFile != null) {

                // Save the new parameter of the new filename.
                // Only do this, if the session value already exists.
                // We use the existance of a session value as a flag
                // For initial try / retry after exception.
                session.putValue(C_PARA_NAME, newFile);
            }
            else {

                // Get back the saved value (if one exists)
                newFile = (String)session.getValue(C_PARA_NAME);
            }
        }

        //newFile=(String)session.getValue(C_PARA_NAME);
        String action = (String)parameters.get("action");
        CmsResource file = cms.readFileHeader(filename);
        if(file.isFile()) {
            template = "file";
        }
        else {
            template = "folder";
        }

        //check if the name parameter was included in the request
        // if not, the lock page is shown for the first time
        //if (newFile == null) {
        if(newFile == null || session.getValue(C_PARA_NAME) == null) {
            if(newFile == null) {
                session.putValue(C_PARA_NAME, file.getResourceName());
            }
            else {
                session.putValue(C_PARA_NAME, newFile);
            }
        }
        else {
            if(action == null) {
                template = "wait";
            }
            else {

                // now check if the resource is a file or a folder
                if(file.isFile()) {

                    // this is a file, so rename it
                    try {
                        cms.renameResource(cms.readAbsolutePath(file), newFile);
                    }
                    catch(CmsException ex) {

                        // something went wrong, so remove all session parameters
                        session.removeValue(C_PARA_RESOURCE); //don't delete this. We really need this to try again.
                        session.removeValue(C_PARA_NAME);
                        xmlTemplateDocument.setData("details", Utils.getStackTrace(ex));
                        xmlTemplateDocument.setData("lasturl", lasturl);
                        return startProcessing(cms, xmlTemplateDocument, "", parameters, "errorbadname");
                    }

                    // everything is done, so remove all session parameters
                    session.removeValue(C_PARA_RESOURCE);
                    session.removeValue(C_PARA_NAME);
                    try {
                        if(lasturl == null || "".equals(lasturl)) {
                            cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                                    + CmsWorkplaceAction.getExplorerFileUri(cms));
                        }
                        else {
                            cms.getRequestContext().getResponse().sendRedirect(lasturl);
                        }
                    }
                    catch(Exception e) {
                        throw new CmsException("Redirect fails :" + getConfigFile(cms).getWorkplaceActionPath()
                                + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, e);
                    }
                    return null;
                }
                else {

                    // this is a folder
                    try {
                        cms.renameResource(cms.readAbsolutePath(file), newFile);
                    } catch(CmsException ex) {

                        // something went wrong, so remove all session parameters
                        session.removeValue(C_PARA_RESOURCE); //don't delete this. We really need this to try again.
                        session.removeValue(C_PARA_NAME);
                        xmlTemplateDocument.setData("details", Utils.getStackTrace(ex));
                        xmlTemplateDocument.setData("lasturl", lasturl);
                        return startProcessing(cms, xmlTemplateDocument, "", parameters, "errorbadname");
                    }

                    // everything is done, so remove all session parameters
                    session.removeValue(C_PARA_RESOURCE);
                    session.removeValue(C_PARA_NAME);
                    xmlTemplateDocument.setData("lasturl", lasturl);
                    template = "update";
                }
            }
        }

        // set the required datablocks
        if(action == null) {
            String title = cms.readProperty(cms.readAbsolutePath(file), C_PROPERTY_TITLE);
            if(title == null) {
                title = "";
            }
            CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
//			TODO fix this later
            // CmsUser owner = cms.readOwner(file);
            xmlTemplateDocument.setData("TITLE", Encoder.escapeXml(title));
            xmlTemplateDocument.setData("STATE", getState(cms, file, lang));
            xmlTemplateDocument.setData("OWNER", "" /* Utils.getFullName(owner) */);
            xmlTemplateDocument.setData("GROUP", "" /* cms.readGroup(file).getName() */);
            xmlTemplateDocument.setData("FILENAME", file.getResourceName());
        }

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

    private String getState(CmsObject cms, CmsResource file, CmsXmlLanguageFile lang) throws CmsException {
        StringBuffer output = new StringBuffer();
        //if(file.inProject(cms.getRequestContext().currentProject())) {
        if (cms.isInsideCurrentProject(file)) {
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

    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }

    /**
     * Pre-Sets the value of the new name input field.
     * This method is directly called by the content definiton.
     * @param Cms The CmsObject.
     * @param lang The language file.
     * @param parameters User parameters.
     * @return Value that is pre-set into the anew name field.
     * @throws CmsExeption if something goes wrong.
     */

    public String setValue(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String name = (String)session.getValue("name");
        return name;
    }
}

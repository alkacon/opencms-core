/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsDelete.java,v $
* Date   : $Date: 2003/07/22 00:29:22 $
* Version: $Revision: 1.49 $
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
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.util.Encoder;
import com.opencms.util.Utils;

import java.util.Hashtable;

/**
 * Template class for displaying the delete screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Michael Emmerich
 * @author Michaela Schleich
 * @version $Revision: 1.49 $ $Date: 2003/07/22 00:29:22 $
 */

public class CmsDelete extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the delete template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The delete template file
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
            session.removeValue(C_PARA_DELETE);
            session.removeValue(C_PARA_FILE);
            session.removeValue("lasturl");
        }

        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);
        String delete = (String)parameters.get(C_PARA_DELETE);
        if(delete != null) {
            session.putValue(C_PARA_DELETE, delete);
        }
        delete = (String)session.getValue(C_PARA_DELETE);
        String filename = (String)parameters.get(C_PARA_FILE);
        if(filename != null) {
            session.putValue(C_PARA_FILE, filename);
        }
        filename = (String)session.getValue(C_PARA_FILE);
        String action = (String)parameters.get("action");
        CmsResource file = cms.readFileHeader(filename);
        if(file.isFile()) {
            template = "file";
        }
        else {
            template = "folder";
        }

        //check if the name parameter was included in the request
        // if not, the delete page is shown for the first time
        if(delete != null) {
            if(action == null) {
                template = "wait";
            }
            else {

                // check if the resource is a file or a folder
                if(file.isFile()) {

                    // its a file, so delete it
                    try{
                        cms.deleteResource(cms.readAbsolutePath(file));
                        session.removeValue(C_PARA_DELETE);
                        session.removeValue(C_PARA_FILE);
                    }catch(CmsException e){
                        session.removeValue(C_PARA_DELETE);
                        session.removeValue(C_PARA_FILE);
                        xmlTemplateDocument.setData("details", Utils.getStackTrace(e));
                        xmlTemplateDocument.setData("lasturl", lasturl);
                        return startProcessing(cms, xmlTemplateDocument, "", parameters, "error");
                    }
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
                    // its a folder
                    try{
                        cms.deleteResource(cms.readAbsolutePath(file));
                    }catch(CmsException e){
                        session.removeValue(C_PARA_DELETE);
                        session.removeValue(C_PARA_FILE);
                        xmlTemplateDocument.setData("details", Utils.getStackTrace(e));
                        xmlTemplateDocument.setData("lasturl", lasturl);
                        return startProcessing(cms, xmlTemplateDocument, "", parameters, "error");
                    }
                    session.removeValue(C_PARA_DELETE);
                    session.removeValue(C_PARA_FILE);
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
            // TODO fix this later
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
     * @throws Throws CmsException if something goes wrong.
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

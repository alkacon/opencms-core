/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsUndochanges.java,v $
* Date   : $Date: 2003/07/16 10:12:10 $
* Version: $Revision: 1.12 $
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
import com.opencms.util.Utils;

import java.util.Hashtable;

/**
 * Template class for displaying the undo screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.12 $ $Date: 2003/07/16 10:12:10 $
 */

public class CmsUndochanges extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the undelete template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The undelete template file
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
            session.removeValue(C_PARA_FILE);
            session.removeValue("undochanges");
            session.removeValue("lasturl");
        }
        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);
        String undochanges = (String)parameters.get("undochanges");
        if(undochanges != null) {
            session.putValue("undochanges", undochanges);
        }
        undochanges = (String)session.getValue("undochanges");
        String filename = (String)parameters.get(C_PARA_FILE);
        if(filename != null) {
            session.putValue(C_PARA_FILE, filename);
        }
        filename = (String)session.getValue(C_PARA_FILE);
        String action = (String)parameters.get("action");

        CmsResource file = null;
        if(filename.endsWith("/")){
            file = (CmsResource)cms.readFolder(filename);
        } else {
            file = (CmsResource)cms.readFileHeader(filename);
        }
        //check if the name parameter was included in the request
        // if not, the undoChanges page is shown for the first time
        if(undochanges != null){
            if(action != null) {
                // undo changes of the resource
                try{
                    cms.undoChanges(cms.readAbsolutePath(file));
                    session.removeValue(C_PARA_FILE);
                    //template = "done";
                    // return to filelist
                    try {
                        if(lasturl == null || "".equals(lasturl)) {
                            cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                                        + CmsWorkplaceAction.getExplorerFileUri(cms));
                        }else {
                            cms.getRequestContext().getResponse().sendRedirect(lasturl);
                        }
                    }catch(Exception e) {
                        throw new CmsException("Redirect fails :"
                            + getConfigFile(cms).getWorkplaceActionPath()
                            + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, e);
                    }
                    return null;
                } catch(CmsException e){
                    session.removeValue(C_PARA_FILE);
                    xmlTemplateDocument.setData("details", Utils.getStackTrace(e));
                    xmlTemplateDocument.setData("lasturl", lasturl);
                    return startProcessing(cms, xmlTemplateDocument, "", parameters, "error");
                }
            } else {
                template = "wait";
            }
        }
        // set the required datablocks
        if(action == null) {
            xmlTemplateDocument.setData("CHANGEDATE", Utils.getNiceDate(file.getDateLastModified()));
            xmlTemplateDocument.setData("USER", cms.readUser(file.getUserLastModified()).getName());
            xmlTemplateDocument.setData("FILENAME", file.getResourceName());
        }

        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
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

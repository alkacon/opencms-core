/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsCopyToProject.java,v $
* Date   : $Date: 2003/07/31 13:19:37 $
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

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.util.Utils;

import java.util.Hashtable;

/**
 * Template class for displaying the copy to project screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.10 $ $Date: 2003/07/31 13:19:37 $
 */

public class CmsCopyToProject extends CmsWorkplaceDefault {

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
            session.removeValue(C_PARA_RESOURCE);
            session.removeValue("copy");
            session.removeValue("lasturl");
        }
        // save the lasturl parameter in the session
        getLastUrl(cms, parameters);
        String copy = (String)parameters.get("copy");
        if(copy != null) {
            session.putValue("copy", copy);
        }
        copy = (String)session.getValue("copy");
        String filename = (String)parameters.get(C_PARA_RESOURCE);
        if(filename != null) {
            session.putValue(C_PARA_RESOURCE, filename);
        }
        filename = (String)session.getValue(C_PARA_RESOURCE);
        String action = (String)parameters.get("action");

        CmsResource file = null;
        if(filename.endsWith("/")){
            file = cms.readFolder(filename);
        } else {
            file = cms.readFileHeader(filename);
        }
        //check if the name parameter was included in the request
        // if not, the copyToProject page is shown for the first time
        if(copy != null){
            if(action != null) {
                // copy the resource to the current project
                try{
                    if(isManager(cms)){
                        cms.copyResourceToProject(cms.readAbsolutePath(file));
                        session.removeValue(C_PARA_RESOURCE);
                        template = "done";
                    } else {
                        throw new CmsException("["+this.getClassName()+"] no management access to project.", CmsException.C_ACCESS_DENIED);
                    }
                } catch(CmsException e){
                    session.removeValue(C_PARA_RESOURCE);
                    xmlTemplateDocument.setData("details", Utils.getStackTrace(e));
                    return startProcessing(cms, xmlTemplateDocument, "", parameters, "error");
                }
            } else {
                template = "wait";
            }
        }
        // set the required datablocks
        if(action == null) {
            xmlTemplateDocument.setData("FILENAME", cms.readAbsolutePath(file));
        }
        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }

    /**
     * checks if the user has rights to manage the project
     */
    public boolean isManager(CmsObject cms) throws CmsException{
        // is the current user an administrator?
        if(cms.isAdmin() || cms.isManagerOfProject()){
            return true;
        }
        return false;
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


/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsRestoreResource.java,v $
* Date   : $Date: 2001/07/17 07:13:59 $
* Version: $Revision: 1.1 $
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import javax.servlet.http.*;
import java.util.*;

/**
 * Template class for displaying the restore resource screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.1 $ $Date: 2001/07/17 07:13:59 $
 */

public class CmsRestoreResource extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the undelete template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The undelete template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
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
            //session.removeValue("versionid");
            session.removeValue("restore");
            session.removeValue("lasturl");
        }
        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);
        String restore = (String)parameters.get("restore");
        if(restore != null) {
            session.putValue("restore", restore);
        }
        restore = (String)session.getValue("restore");
        String filename = (String)parameters.get(C_PARA_FILE);
        if(filename != null) {
            session.putValue(C_PARA_FILE, filename);
        }
        filename = (String)session.getValue(C_PARA_FILE);
A_OpenCms.log(A_OpenCms.C_OPENCMS_DEBUG, "get versionid "+(String)session.getValue("versionid"));
        String versionid = (String)parameters.get("versionid");
        if(versionid != null) {
            session.putValue("versionid", versionid);
        }
        versionid = (String)session.getValue("versionid");
A_OpenCms.log(A_OpenCms.C_OPENCMS_DEBUG, "versionid: "+versionid+", filename: "+filename);
        String action = (String)parameters.get("action");

        CmsResource file = null;
        if(filename.endsWith("/")){
            file = (CmsResource)cms.readFolder(filename);
        } else {
            file = (CmsResource)cms.readFileHeader(filename);
        }
        //check if the name parameter was included in the request
        // if not, the restoreresource page is shown for the first time
        if((restore != null)){
            if(action != null) {
                // restore the resource
                try{
                    cms.restoreResource(Integer.parseInt(versionid),file.getAbsolutePath());
                    session.removeValue(C_PARA_FILE);
                    template = "done";
                } catch(CmsException e){
                    session.removeValue(C_PARA_FILE);
                    xmlTemplateDocument.setData("details", Utils.getStackTrace(e));
                    return startProcessing(cms, xmlTemplateDocument, "", parameters, "error");
                }
            } else {
                template = "wait";
            }
        }
        // set the required datablocks
        if(action == null) {
            CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
            xmlTemplateDocument.setData("CHANGEDATE", Utils.getNiceDate(file.getDateLastModified()));
            xmlTemplateDocument.setData("USER", cms.readUser(file.getResourceLastModifiedBy()).getName());
            xmlTemplateDocument.setData("FILENAME", file.getName());
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

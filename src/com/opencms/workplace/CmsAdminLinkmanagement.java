/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminLinkmanagement.java,v $
* Date   : $Date: 2002/05/24 12:51:09 $
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
import java.text.*;
import java.util.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * Description:
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsAdminLinkmanagement extends CmsWorkplaceDefault implements I_CmsConstants{

    private final String C_LINKCHECK_THREAD = "linkcheckthread";
    private final String C_LM_TEXT = "adminLinkmanagemententryfortextsave";

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
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {

        CmsXmlWpTemplateFile templateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String action = (String)parameters.get("action");
        CmsXmlLanguageFile lang = templateDocument.getLanguageFile();

        if("start".equals(action)){
            // first call. Start the checking.
            CmsAdminLinkmanagementThread doCheck = new CmsAdminLinkmanagementThread(cms, cms.getRequestContext().currentProject().getId());
            doCheck.start();
            session.putValue(C_LINKCHECK_THREAD, doCheck);
            // store the text in the Session
            String text = lang.getDataValue("linkmanagement.label.text1")
                            + cms.getRequestContext().currentProject().getName()
                            + lang.getDataValue("linkmanagement.label.text2");
            session.putValue(C_LM_TEXT, text);
            templateDocument.setData("data", "");
            templateDocument.setData("endMethod", "");
            templateDocument.setData("text", text);
        }else if("working".equals(action)){
            CmsAdminLinkmanagementThread doCheck = (CmsAdminLinkmanagementThread)session.getValue(C_LINKCHECK_THREAD);
            //still working?
            if(doCheck.isAlive()){
                templateDocument.setData("endMethod", "");
                templateDocument.setData("text", (String)session.getValue(C_LM_TEXT));
            }else{
                templateDocument.setData("endMethod", templateDocument.getDataValue("endMethod"));
                templateDocument.setData("autoUpdate","");
                templateDocument.setData("text", (String)session.getValue(C_LM_TEXT)
                        + "<br>" + lang.getDataValue("linkmanagement.label.textende"));
                session.removeValue(C_LM_TEXT);
                session.removeValue(C_LINKCHECK_THREAD);
            }
            templateDocument.setData("data", doCheck.getReportUpdate());
        }
        // Now load the template file and start the processing
        return startProcessing(cms, templateDocument, elementName, parameters, templateSelector);
    }
}
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminLoggedInUsers.java,v $
* Date   : $Date: 2001/10/05 07:33:07 $
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
import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace admin users screens.
 * <P>
 *
 * @author Mario Stanke
 * @version $Revision: 1.1 $ $Date: 2001/10/05 07:33:07 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminLoggedInUsers extends CmsWorkplaceDefault  {

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

        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        if(parameters.get("message") != null) {
            // there is a message to all - send it
            cms.sendBroadcastMessage((String)parameters.get("message"));
        }

        Vector users = cms.getLoggedInUsers();
        Hashtable user;
        StringBuffer ret = new StringBuffer();
        for(int i = 0; i < users.size(); i++) {
            try {
                user = (Hashtable) users.get(i);
                CmsUser cmsUser = cms.readUser((String)user.get(I_CmsConstants.C_SESSION_USERNAME));
                CmsProject cmsProject = cms.readProject(((Integer)user.get(I_CmsConstants.C_SESSION_PROJECT)).intValue());
                xmlTemplateDocument.setData("username", cmsUser.getName());
                xmlTemplateDocument.setData("firstname", cmsUser.getFirstname()+"");
                xmlTemplateDocument.setData("lastname", cmsUser.getLastname()+"");
                xmlTemplateDocument.setData("email", cmsUser.getEmail()+"");
                xmlTemplateDocument.setData("currentgroup", (String)user.get(I_CmsConstants.C_SESSION_CURRENTGROUP));
                xmlTemplateDocument.setData("messagepending", ((Boolean)user.get(I_CmsConstants.C_SESSION_MESSAGEPENDING)).toString());
                xmlTemplateDocument.setData("currentproject", cmsProject.getName());
                ret.append( xmlTemplateDocument.getProcessedDataValue("line") );
            } catch(Exception exc) {
                // ignore all exceptions - don't show this user
            }
        }

        xmlTemplateDocument.setData("all_lines", ret.toString());

        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }
}

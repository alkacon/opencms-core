/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProjectResentFiles.java,v $
* Date   : $Date: 2001/07/31 15:50:17 $
* Version: $Revision: 1.17 $
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
 * News administration template class
 * <p>
 * Used both for displaying news administration overviews and
 * editing news.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.17 $ $Date: 2001/07/31 15:50:17 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminProjectResentFiles extends CmsWorkplaceDefault implements I_CmsConstants{

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
        // get the session
        I_CmsSession session = cms.getRequestContext().getSession(true);
        // load the template file
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);
        String filter = (String)parameters.get("filter");
        String projectId = (String)parameters.get("projectid");
        String action = (String)parameters.get("action");
        if(filter == null || "".equalsIgnoreCase(filter)){
            filter=(String)session.getValue("filter");
            if(filter == null || "".equalsIgnoreCase(filter)){
                filter = "all";
            }
            xmlTemplateDocument.setData("loadheader",xmlTemplateDocument.getProcessedDataValue("LOADHEADER", this));
        } else {
            xmlTemplateDocument.setData("loadheader",xmlTemplateDocument.getProcessedDataValue("NOTLOADHEADER", this));
        }
        if(projectId == null || "".equalsIgnoreCase(projectId)){
            projectId = (String)session.getValue("projectid");
            if(projectId == null || "".equalsIgnoreCase(projectId)){
                projectId = ""+cms.getRequestContext().currentProject().getId();
            }
        }

        // store the chosen filter and projectid into the session
        session.putValue("filter", filter);
        session.putValue("projectid", projectId);

        if(action != null && "restore".equalsIgnoreCase(action)){
            session.removeValue("projectid");
            session.removeValue("filter");
        }
        // Finally start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
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

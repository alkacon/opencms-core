/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProjectResentFiles.java,v $
* Date   : $Date: 2003/07/31 13:19:36 $
* Version: $Revision: 1.22 $
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

import java.io.IOException;
import java.util.Hashtable;

/**
 * News administration template class
 * <p>
 * Used both for displaying news administration overviews and
 * editing news.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.22 $ $Date: 2003/07/31 13:19:36 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminProjectResentFiles extends CmsWorkplaceDefault {

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters.
     *
     * @see #getContent(CmsObject, String, String, Hashtable, String)
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

        if(action != null && "restoreproject".equalsIgnoreCase(action)){
            session.removeValue("projectid");
            session.removeValue("filter");
            //redirect to the needed headfile
            try{
                cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                    +"empty.html");
            } catch (IOException exc){
                throw new CmsException("Could not redirect to empty.html", exc);
            }
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

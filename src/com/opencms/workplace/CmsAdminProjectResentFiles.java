/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProjectResentFiles.java,v $
* Date   : $Date: 2005/02/18 14:23:15 $
* Version: $Revision: 1.28 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001-2005  The OpenCms Group
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.io.IOException;
import java.util.Hashtable;

/**
 * News administration template class
 * <p>
 * Used both for displaying news administration overviews and
 * editing news.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.28 $ $Date: 2005/02/18 14:23:15 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
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
     * @throws CmsException if something goes wrong
     * @return byte array with the processed content
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        // get the session
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        // load the template file
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);
        String filter = (String)parameters.get("projectfilter");
        String projectId = (String)parameters.get("projectid");
        if (projectId == null || "".equalsIgnoreCase(projectId)) {
            projectId = (String)session.getValue("projectid");
            if (projectId == null || "".equalsIgnoreCase(projectId)) {
                projectId = ""+cms.getRequestContext().currentProject().getId();
            }
        }
        xmlTemplateDocument.setData("projectid", projectId);
        String action = (String)parameters.get("action");
        if (filter == null || "".equalsIgnoreCase(filter)) {
            filter=(String)session.getValue("projectfilter");
            if (filter == null || "".equalsIgnoreCase(filter)) {
                filter = "all";
            }
            xmlTemplateDocument.setData("filter", filter);
            xmlTemplateDocument.setData("loadheader", xmlTemplateDocument.getProcessedDataValue("LOADHEADER", this));
        } else {
            xmlTemplateDocument.setData("filter", filter);
            xmlTemplateDocument.setData("loadheader", xmlTemplateDocument.getProcessedDataValue("NOTLOADHEADER", this));
        }

        // store the chosen filter and projectid into the session
        session.putValue("projectfilter", filter);
        session.putValue("projectid", projectId);

        if (action != null && "restoreproject".equalsIgnoreCase(action)) {
            session.removeValue("projectid");
            session.removeValue("projectfilter");
            //redirect to the needed headfile
            try {
                CmsXmlTemplateLoader.getResponse(cms.getRequestContext()).sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                    +"empty.html");
            } catch (IOException exc) {
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

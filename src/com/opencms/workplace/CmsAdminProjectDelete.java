/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProjectDelete.java,v $
* Date   : $Date: 2004/02/22 13:52:26 $
* Version: $Revision: 1.32 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.threads.CmsProjectDeleteThread;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.Hashtable;

/**
 * Template class for displaying OpenCms workplace admin project resent.
 * <P>
 *
 * @author Andreas Schouten
 * @version $Revision: 1.32 $ $Date: 2004/02/22 13:52:26 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminProjectDelete extends CmsWorkplaceDefault {

    private final String C_DELETE_THREAD = "deleteprojectthread";

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

    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters,
            String templateSelector) throws CmsException {
        if(OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);

        CmsProject project = null;
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String action = (String)parameters.get("action");
        String projectId = (String)parameters.get("projectid");
        if(projectId == null || "".equals(projectId)){
            projectId = (String)session.getValue("delprojectid");
        } else {
            session.putValue("delprojectid", projectId);
        }
        if(parameters.get("ok") != null) {
            if(action == null){
                // start the deleting
                project = cms.readProject(Integer.parseInt(projectId));
                A_CmsReportThread doDelete = new CmsProjectDeleteThread(cms, Integer.parseInt(projectId));
                doDelete.start();
                session.putValue(C_DELETE_THREAD, doDelete);
                xmlTemplateDocument.setData("time", "10");
                templateSelector = "wait";
            } else if("working".equals(action)) {
                // still working?
                A_CmsReportThread doDelete = (A_CmsReportThread)session.getValue(C_DELETE_THREAD);
                if(doDelete.isAlive()) {
                    String time = (String)parameters.get("time");
                    int wert = Integer.parseInt(time);
                    wert += 2;
                    xmlTemplateDocument.setData("time", "" + wert);
                    templateSelector = "wait";
                } else {
                    // thread has come to an end, was there an error?
                    Throwable error = doDelete.getError();
                    if(error == null) {
                        // clear the languagefile cache
                        templateSelector = "done";
                        if (cms.getRequestContext().currentProject().getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
                            // in online project, submit the head project selector with "Online" selected to avoid an infinite loop
                            xmlTemplateDocument.setData("onlineId", "" + I_CmsConstants.C_PROJECT_ONLINE_ID);
                            xmlTemplateDocument.setData("switchjavascript", xmlTemplateDocument.getProcessedDataValue("switchheadproject"));
                        } else {
                            // in an offline project, the javascript for setting the project is not needed
                            xmlTemplateDocument.setData("switchjavascript", xmlTemplateDocument.getProcessedDataValue("dontswitchheadproject"));
                        }
                        session.removeValue("delprojectid");
                    } else {
                        // get errorpage
                        xmlTemplateDocument.setData("details", CmsException.getStackTraceAsString(error));
                        templateSelector = "error";
                        session.removeValue("delprojectid");
                    }
                }
            }
        } else {
            // show details about the project
            project = cms.readProject(Integer.parseInt(projectId));
            CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
            CmsProjectlist.setListEntryData(cms, lang, xmlTemplateDocument, project);

        // Now load the template file and start the processing
        }
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters,
                templateSelector);
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

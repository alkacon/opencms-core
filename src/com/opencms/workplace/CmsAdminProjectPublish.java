/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProjectPublish.java,v $
* Date   : $Date: 2001/07/31 15:50:17 $
* Version: $Revision: 1.18 $
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
 * Template class for displaying OpenCms workplace admin project resent.
 * <P>
 *
 * @author Andreas Schouten
 * @version $Revision: 1.18 $ $Date: 2001/07/31 15:50:17 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminProjectPublish extends CmsWorkplaceDefault implements I_CmsConstants,I_CmsLogChannels {

    private final String C_PUBLISH_THREAD = "publishprojectthread";

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
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() && C_DEBUG ) {
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "getting content of element " + ((elementName == null) ? "<root>" : elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: " + ((templateSelector == null) ? "<default>" : templateSelector));
        }
         CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String paraId = (String)parameters.get("projectid");
        int projectId = -1;
        int projectType = C_PROJECT_TYPE_TEMPORARY;
        if(paraId != null) {
            projectId = Integer.parseInt(paraId);
            CmsProject project = cms.readProject(projectId);
            projectType = project.getType();
            xmlTemplateDocument.setData("projectid", projectId + "");
            xmlTemplateDocument.setData("projectname", project.getName());
        }
        String action = (String)parameters.get("action");
        // look if user called from Explorer
        String fromExplorer = (String)parameters.get("fromExplorer");
        if (fromExplorer != null){
            // this is Explorer calling lets talk about currentProject
            CmsProject currentProject = cms.getRequestContext().currentProject();
            projectId = currentProject.getId();
            projectType = currentProject.getType();
            xmlTemplateDocument.setData("projectid", projectId + "");
            xmlTemplateDocument.setData("projectname", currentProject.getName());
            // in this case we have to check if there are locked resources in the project
            if ((action != null) && "check".equals(action)){
                if(cms.countLockedResources(projectId) == 0){
                    action = "ok";
                }else{
                    // ask user if the locks should be removed
                    return startProcessing(cms, xmlTemplateDocument, elementName, parameters,"asklock");
                }
            }else if((action != null) && "rmlocks".equals(action)){
                // remouve the locks and publish
                try{
                    cms.unlockProject(projectId);
                    action = "ok";
                }catch (CmsException exc){
                    xmlTemplateDocument.setData("details", Utils.getStackTrace(exc));
                    return startProcessing(cms, xmlTemplateDocument, elementName, parameters,"errorlock");
                }
            }
        }

        if((action != null) && "ok".equals(action)) {
            // start the publishing
            // first clear the session entry if necessary
            if(session.getValue(C_SESSION_THREAD_ERROR) != null) {
                session.removeValue(C_SESSION_THREAD_ERROR);
            }
            if(projectType == C_PROJECT_TYPE_TEMPORARY){
                cms.getRequestContext().setCurrentProject(cms.onlineProject().getId());
            }
            Thread doPublish = new CmsAdminPublishProjectThread(cms, projectId);
            doPublish.start();
            session.putValue(C_PUBLISH_THREAD, doPublish);
            xmlTemplateDocument.setData("time", "10");
            templateSelector = "wait";
        } else {
            if((action != null) && ("working".equals(action))) {

                // still working?
                Thread doPublish = (Thread)session.getValue(C_PUBLISH_THREAD);
                if(doPublish.isAlive()) {
                    String time = (String)parameters.get("time");
                    int wert = Integer.parseInt(time);
                    //wert += 20;
                    wert += 2;
                    xmlTemplateDocument.setData("time", "" + wert);
                    templateSelector = "wait";
                } else {
                    // thread has come to an end, was there an error?
                    String errordetails = (String)session.getValue(C_SESSION_THREAD_ERROR);
                    if(errordetails == null) {
                        // clear the languagefile cache
                        CmsXmlWpTemplateFile.clearcache();
                        templateSelector = "done";
                    } else {
                        // get errorpage:
                        xmlTemplateDocument.setData("details", errordetails);
                        templateSelector = "error";
                        session.removeValue(C_SESSION_THREAD_ERROR);
                    }
                }
            }
        }
        // Now load the template file and start the processing
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

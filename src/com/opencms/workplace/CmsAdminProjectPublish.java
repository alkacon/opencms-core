/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProjectPublish.java,v $
* Date   : $Date: 2004/02/22 13:52:26 $
* Version: $Revision: 1.46 $
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
import org.opencms.threads.CmsLinkHrefManagementThread;
import org.opencms.threads.CmsPublishThread;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.Hashtable;

/**
 * Template class for displaying OpenCms workplace admin project resent.
 * <P>
 *
 * @author Andreas Schouten
 * @version $Revision: 1.46 $ $Date: 2004/02/22 13:52:26 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminProjectPublish extends CmsWorkplaceDefault {

    private static String C_PUBLISH_THREAD = "publish.resource.thread";
    private static String C_PUBLISH_LINKCHECK_THREAD = "publish.linkcheck.thread";
    private static String C_PROJECT_ID_FOR_PUBLISH="project.id.for.publish";

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters.<p>
     *
     * @see #getContent(CmsObject, String, String, Hashtable, String)
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template
     * @param parameters Hashtable with all template class parameters
     * @param templateSelector template section that should be processed
     * @return the processed content
     * @throws CmsException if something goes wrong
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        if(OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
        String action = (String)parameters.get("action");

        // here we show the report updates when the threads are allready running.
        if("showResult".equals(action)){
            // ok. Thread is started and we shoud show the report information.
            CmsLinkHrefManagementThread doTheWork = (CmsLinkHrefManagementThread)session.getValue(C_PUBLISH_LINKCHECK_THREAD);
            //still working?
            if(doTheWork.isAlive()){
                xmlTemplateDocument.setData("endMethod", "");
                xmlTemplateDocument.setData("text", lang.getLanguageValue("project.publish.message_linkcheck"));
            }else{
                if(doTheWork.brokenLinksFound()){
                    xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod2"));
                    xmlTemplateDocument.setData("autoUpdate","");
                    xmlTemplateDocument.setData("text", lang.getLanguageValue("project.publish.message_brokenlinks")
                                                +"<br>"+lang.getLanguageValue("project.publish.message_brokenlinks2"));
                }else{
                    xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod3"));
                    xmlTemplateDocument.setData("autoUpdate","");
                    xmlTemplateDocument.setData("text", "");
                }
                session.removeValue(C_PUBLISH_LINKCHECK_THREAD);
            }
            xmlTemplateDocument.setData("data", doTheWork.getReportUpdate());
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "updateReport");
        }

        if("doThePublish".equals(action)){
            // linkcheck is ready. Now we can start the publishing
            //int projectId = ((Integer)session.getValue(C_PROJECT_ID_FOR_PUBLISH)).intValue();
            session.removeValue(C_PROJECT_ID_FOR_PUBLISH);
            A_CmsReportThread doPublish = new CmsPublishThread(cms);
            doPublish.start();
            session.putValue(C_PUBLISH_THREAD, doPublish);
            xmlTemplateDocument.setData("actionParameter", "showPublishResult");
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "showresult");
        }

        if("showPublishResult".equals(action)){
            // ok. Thread is started and we shoud show the report information.
            A_CmsReportThread doTheWork = (A_CmsReportThread)session.getValue(C_PUBLISH_THREAD);
            //still working?
            if(doTheWork.isAlive()){
                xmlTemplateDocument.setData("endMethod", "");
                xmlTemplateDocument.setData("text", lang.getLanguageValue("project.publish.message_publish"));
            }else{
                xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod"));
                xmlTemplateDocument.setData("autoUpdate","");
                xmlTemplateDocument.setData("text", lang.getLanguageValue("project.publish.message_publish2"));
                session.removeValue(C_PUBLISH_THREAD);
            }
            xmlTemplateDocument.setData("data", doTheWork.getReportUpdate());
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "updateReport");
        }

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
                    xmlTemplateDocument.setData("details", CmsException.getStackTraceAsString(exc));
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
            if (projectType == C_PROJECT_TYPE_TEMPORARY) {
                cms.getRequestContext().setCurrentProject(cms.readProject(I_CmsConstants.C_PROJECT_ONLINE_ID));
            }
            // first part of the publish: check for broken links
            A_CmsReportThread doCheck = new CmsLinkHrefManagementThread(cms);
            doCheck.start();
            session.putValue(C_PUBLISH_LINKCHECK_THREAD, doCheck);
            session.putValue(C_PROJECT_ID_FOR_PUBLISH, new Integer(projectId));
            templateSelector = "showresult";
        } else {
            if((action != null) && ("working".equals(action))) {

                // still working?
                A_CmsReportThread doPublish = (A_CmsReportThread)session.getValue(C_PUBLISH_THREAD);
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

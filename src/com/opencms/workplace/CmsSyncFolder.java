/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsSyncFolder.java,v $
* Date   : $Date: 2003/08/07 18:47:27 $
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

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsRequestContext;
import com.opencms.report.A_CmsReportThread;
import com.opencms.template.CmsXmlTemplateFile;
import com.opencms.util.Utils;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace synchronize screens.
 * <P>
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.22 $ $Date: 2003/08/07 18:47:27 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsSyncFolder extends CmsWorkplaceDefault {

    /**
     * The name of the tag for sync button.
     */
    static final String C_SYNC_BUTTON = "SYNC";

    /**
     * The name of the tag for disabled sync button.
     */
    static final String C_SYNC_BUTTON_DISABLED = "SYNC_DISABLED";

    /**
     * The name of the tag for enabled sync button.
     */
    static final String C_SYNC_BUTTON_ENABLED = "SYNC_ENABLED";

    /**
     *
     */
    private static String C_SYNCFOLDER_THREAD = "sync_folder_thread";

    /**
     * The flag is set true, if a new project for synchronisation has been created
     */
    private static boolean m_newProject = false;


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
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() && C_DEBUG) {
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName()
                    + "getting content of element " + ((elementName == null) ? "<root>" : elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: "
                    + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: "
                    + ((templateSelector == null) ? "<default>" : templateSelector));
        }
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsRequestContext reqCont = cms.getRequestContext();
        //CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        String action = new String();
        action = (String)parameters.get("action");
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile,
                elementName, parameters, templateSelector);

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {
        // remove all session values
            session.removeValue("lasturl");
            action = "start";
        }
        // create a vector with the resources to be synchronized
        Vector synchronizeResources = parseResources(cms.getRegistry().getSystemValues(C_SYNCHRONISATION_RESOURCE));

        // first we look if the thread is already running
        if((action != null) && ("working".equals(action))) {
            // still working?
            A_CmsReportThread doProjectNew = (A_CmsReportThread)session.getValue(C_SYNCFOLDER_THREAD);
            if(doProjectNew.isAlive()) {
                String time = (String)parameters.get("time");
                int wert = Integer.parseInt(time);
                wert += 5;
                xmlTemplateDocument.setData("time", "" + wert);
                return startProcessing(cms, xmlTemplateDocument, elementName, parameters,
                        "wait");
            } else {
                // thread has come to an end, was there an error?
                String errordetails = (String)session.getValue(C_SESSION_THREAD_ERROR);
                if(errordetails == null) {
                    return startProcessing(cms, xmlTemplateDocument, elementName,
                            parameters, "done");
                } else {
                    // get errorpage:
                    xmlTemplateDocument.setData("details", errordetails);
                    session.removeValue(C_SESSION_THREAD_ERROR);
                    return startProcessing(cms, xmlTemplateDocument, elementName,
                            parameters, "error");
                }
            }
        }

        // is the wait-page showing?
        if("start".equals(action)) {
            // synchronize the resources
            try {
                // this is because the simpler 'getParameterValues' method doesn't work with Silverstream
                int numRes = synchronizeResources.size();
                checkRedundancies(synchronizeResources);
                numRes = synchronizeResources.size(); // could have been changed
                Vector notWriteable = new Vector();
                for(int i = numRes - 1;i >= 0;i--) {
                    String theFolder = (String)synchronizeResources.elementAt(i);
                    // if a new project was created for synchronisation, copy the resource to the project
                    // this is now done in the thread
                                        //if (m_newProject){
                    //  cms.copyResourceToProject(theFolder);
                    //}
                    if(!checkWriteable(cms, theFolder)) {
                        notWriteable.addElement(theFolder);
                        templateSelector = "error";
                    }
                }

                if(!"error".equals(templateSelector)) {
                    // set the currentProject to the synchronizeProject
                    Vector allProjects = cms.getAllAccessibleProjects();
                    int count = 0;
                    CmsProject cmsProject = null;
                    int projectId = cms.getRequestContext().currentProject().getId();
                    String projectName = cms.getRegistry().getSystemValue(C_SYNCHRONISATION_PROJECT);
                    for( int i = 0; i < allProjects.size(); i++ ) {
                        cmsProject = (CmsProject)allProjects.elementAt(i);
                        if (cmsProject.getName().equals(projectName)){
                            projectId = cmsProject.getId();
                            count++;
                        }
                    }
                    if (count == 1){
                        // only one syncproject was found, so set this project
                        reqCont.setCurrentProject(projectId);
                        m_newProject = false;
                    } else if (count == 0){
                        // there is no syncproject, so create a new one and set this as the current project
                        // the necessary resources will be copied later to this project
                        reqCont.setCurrentProject(cms.createProject(
                            projectName, 
                            "Project for synchronisation", 
                            A_OpenCms.getDefaultUsers().getGroupUsers(), 
                            A_OpenCms.getDefaultUsers().getGroupProjectmanagers(), 
                            C_PROJECT_TYPE_NORMAL).getId()
                        );
                        m_newProject = true;
                    } else {
                        // there are too many projects with the name of the syncproject, so return an error
                        xmlTemplateDocument.setData("details", "Too many projects for synchronisation.");
                        return startProcessing(cms, xmlTemplateDocument, elementName,
                            parameters, "error");
                    }
                    // start the thread for: synchronize the resources
                    // first clear the session entry if necessary
                    if(session.getValue(C_SESSION_THREAD_ERROR) != null) {
                        session.removeValue(C_SESSION_THREAD_ERROR);
                    }
                    A_CmsReportThread doSyncFolder = new CmsSyncFolderThread(cms, synchronizeResources, m_newProject, session);
                    doSyncFolder.start();
                    session.putValue(C_SYNCFOLDER_THREAD, doSyncFolder);
                    xmlTemplateDocument.setData("time", "5");
                    templateSelector = "wait";
                } else {
                    // at least one of the choosen folders was not writeable -> don't synchronize.
                    xmlTemplateDocument.setData("details", "The following folders were not writeable:"
                            + notWriteable.toString());
                }
            } catch(Exception exc) {
                xmlTemplateDocument.setData("details", Utils.getStackTrace(exc));
                templateSelector = "error";
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

    /** Check whether some of the resources are redundant because a superfolder has also
     *  been selected.
     *
     * @param resources containts the full pathnames of all the resources
     * @return A vector with the same resources, but the paths in the return value are disjoint
     */

    private void checkRedundancies(Vector resources) {
        int i, j;
        if(resources == null) {
            return ;
        }
        Vector redundant = new Vector();
        int n = resources.size();
        if(n < 2) {
            // no check needed, because there is only one resource or
            // no resources selected, return empty Vector
            return ;
        }
        for(i = 0;i < n;i++) {
            redundant.addElement(new Boolean(false));
        }
        for(i = 0;i < n - 1;i++) {
            for(j = i + 1;j < n;j++) {
                if(((String)resources.elementAt(i)).length() <
                        ((String)resources.elementAt(j)).length()) {
                    if(((String)resources.elementAt(j)).startsWith((String)resources.elementAt(i))) {
                        redundant.setElementAt(new Boolean(true), j);
                    }
                }
                else {
                    if(((String)resources.elementAt(i)).startsWith((String)resources.elementAt(j))) {
                        redundant.setElementAt(new Boolean(true), i);
                    }
                }
            }
        }
        for(i = n - 1;i >= 0;i--) {
            if(((Boolean)redundant.elementAt(i)).booleanValue()) {
                resources.removeElementAt(i);
            }
        }
    }

    /**
     * Check if this resource is writeable.
     *
     * @param cms The CmsObject
     * @param res The resource to be checked.
     * @return True or false.
     * @throws CmsException if something goes wrong.
     */

    private boolean checkWriteable(CmsObject cms, String resPath)  throws CmsException {
    	return cms.hasPermissions(resPath, C_WRITE_ACCESS);
    }
    /**
     * Parse the hashtable which holds all resources
     *
     * @param resources containts the full pathnames of all the resources
     * @return A vector with the same resources
     */

    private Vector parseResources(Hashtable resources) {
        Vector ret = new Vector();
        int numRes = resources.size();
        for (int i=1 ; i<numRes+1; i++) {
            String path = (String)resources.get("res"+i);
            ret.addElement(path);
        }
        return ret;
    }
}

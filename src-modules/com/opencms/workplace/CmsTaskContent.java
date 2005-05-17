/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/workplace/Attic/CmsTaskContent.java,v $
* Date   : $Date: 2005/05/17 13:47:28 $
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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workflow.CmsTaskService;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace task content screens.
 * <P>
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 2005/05/17 13:47:28 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsTaskContent extends CmsWorkplaceDefault {
    
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
        if(OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String taskid = (String)session.getValue(C_PARA_STARTTASKID);
        if(session.getValue(C_PARA_STARTTASKID) != null) {
            session.removeValue(C_PARA_STARTTASKID);
            session.removeValue(C_PARA_VIEW);
            CmsXmlWpConfigFile conf = new CmsXmlWpConfigFile(cms);
            String actionPath = conf.getWorkplaceActionPath();
            try {
                CmsXmlTemplateLoader.getResponse(cms.getRequestContext()).sendCmsRedirect(actionPath + "tasks_content_detail.html?taskid=" + taskid);
            }
            catch(Exception e) {
                if(OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("Could not send redirect", e);
                }
            }
            return null;
        }
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        
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
    
    /**
     * Reads alls tasks for all roles of a user.
     * @param cms The cms object.
     * @param project The name of the project.
     * @param taskType The type of the task to read.
     * @param orderBy A order criteria.
     * @param groupBy A sort criteria.
     * @return a vector of tasks.
     * @throws Throws a exception, if something goes wrong.
     */
    
    private Vector readTasksForRole(CmsObject cms, int project, int taskType, String orderBy, String groupBy) throws CmsException {
        String name = cms.getRequestContext().currentUser().getName();
        List groups = cms.getGroupsOfUser(name);
        CmsGroup group;
        List tasks;
        Vector allTasks = new Vector();
        for(int i = 0;i < groups.size();i++) {
            group = (CmsGroup)groups.get(i);
            tasks = cms.getTaskService().readTasksForRole(project, group.getName(), taskType, orderBy, groupBy);
            
            // join the vectors
            for(int z = 0;z < tasks.size();z++) {
                allTasks.addElement(tasks.get(z));
            }
        }
        return allTasks;
    }
    
    /**
     * Gets the tasks.
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @return Vector representing the tasks.
     * @throws CmsException
     */
    
    public List taskList(CmsObject cms, CmsXmlLanguageFile lang) throws CmsException {
        String orderBy = "";
        String groupBy = "";
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        Object allProjects = session.getValue(C_SESSION_TASK_ALLPROJECTS);
        int project = cms.getRequestContext().currentProject().getId();
        String filter = (String)session.getValue(C_SESSION_TASK_FILTER);
        List retValue;
        
        // was the allprojects checkbox checked?
        if((allProjects != null) && (((Boolean)allProjects).booleanValue())) {
            project = C_UNKNOWN_ID;
        }
        if(filter == null) {
            filter = "a1";
        }
        int filterNum = Integer.parseInt(filter, 16);
        String userName = cms.getRequestContext().currentUser().getName();
        switch(filterNum) {
        case 0xa1:
            retValue = cms.getTaskService().readTasksForUser(project, userName, C_TASKS_NEW, orderBy, groupBy);
            break;
        
        case 0xa2:
            retValue = cms.getTaskService().readTasksForUser(project, userName, C_TASKS_ACTIVE, orderBy, groupBy);
            break;
        
        case 0xa3:
            retValue = cms.getTaskService().readTasksForUser(project, userName, C_TASKS_DONE, orderBy, groupBy);
            break;
        
        case 0xb1:
            retValue = readTasksForRole(cms, project, C_TASKS_NEW, orderBy, groupBy);
            break;
        
        case 0xb2:
            retValue = readTasksForRole(cms, project, C_TASKS_ACTIVE, orderBy, groupBy);
            break;
        
        case 0xb3:
            retValue = readTasksForRole(cms, project, C_TASKS_DONE, orderBy, groupBy);
            break;
        
        case 0xc1:
            retValue = cms.getTaskService().readTasksForProject(project, C_TASKS_NEW, orderBy, groupBy);
            break;
        
        case 0xc2:
            retValue = cms.getTaskService().readTasksForProject(project, C_TASKS_ACTIVE, orderBy, groupBy);
            break;
        
        case 0xc3:
            retValue = cms.getTaskService().readTasksForProject(project, C_TASKS_DONE, orderBy, groupBy);
            break;
        
        case 0xd1:
            retValue = cms.getTaskService().readGivenTasks(project, userName, C_TASKS_NEW, orderBy, groupBy);
            break;
        
        case 0xd2:
            retValue = cms.getTaskService().readGivenTasks(project, userName, C_TASKS_ACTIVE, orderBy, groupBy);
            break;
        
        case 0xd3:
            retValue = cms.getTaskService().readGivenTasks(project, userName, C_TASKS_DONE, orderBy, groupBy);
            break;
        
        default:
            retValue = cms.getTaskService().readTasksForUser(project, userName, C_TASKS_ALL, orderBy, groupBy);
            break;
        }
        if(retValue == null) {
            return new Vector();
        }
        else {
            return retValue;
        }
    }
}

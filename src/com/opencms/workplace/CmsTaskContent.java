/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsTaskContent.java,v $
 * Date   : $Date: 2000/05/18 14:35:01 $
 * Version: $Revision: 1.12 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;

import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace task content screens.
 * <P>
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.12 $ $Date: 2000/05/18 14:35:01 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsTaskContent extends CmsWorkplaceDefault implements I_CmsConstants, I_CmsWpConstants {
	
	/**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }    

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters. 
     * 
     * @see getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "getting content of element " + ((elementName==null)?"<root>":elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
		
		HttpSession session = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(false);
		String taskid=(String)session.getValue(C_PARA_STARTTASKID);
		if (session.getValue(C_PARA_STARTTASKID)!=null) {
			session.removeValue(C_PARA_STARTTASKID);
			session.removeValue(C_PARA_VIEW);			
			CmsXmlWpConfigFile conf=new CmsXmlWpConfigFile(cms);
			String actionPath=conf.getWorkplaceActionPath();
			try {
				cms.getRequestContext().getResponse().sendCmsRedirect(actionPath+"tasks_content_detail.html?taskid="+taskid);
			} catch (Exception e) {				
				if(A_OpenCms.isLogging()) {
					A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName()+ " " + e.getMessage());
				}
			}
            return null;
		}
		
		CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		
		// Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

    /**
     * Gets the tasks.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @return Vector representing the tasks.
     * @exception CmsException
     */
    public Vector taskList(A_CmsObject cms, CmsXmlLanguageFile lang)
		throws CmsException {
		String orderBy = "";
		String groupBy = "";
		HttpSession session = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
		Object allProjects = session.getValue(C_SESSION_TASK_ALLPROJECTS);
		int project = cms.getRequestContext().currentProject().getId();
		String filter = (String)session.getValue(C_SESSION_TASK_FILTER);
		Vector retValue;

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
				retValue = cms.readTasksForUser(project, userName, C_TASKS_NEW, orderBy, groupBy);
				break;
			case 0xa2:
				retValue = cms.readTasksForUser(project, userName, C_TASKS_ACTIVE, orderBy, groupBy);
				break;
			case 0xa3:
				retValue = cms.readTasksForUser(project, userName, C_TASKS_DONE, orderBy, groupBy);
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
				retValue = cms.readTasksForProject(project, C_TASKS_NEW, orderBy, groupBy);
				break;
			case 0xc2:
				retValue = cms.readTasksForProject(project, C_TASKS_ACTIVE, orderBy, groupBy);
				break;
			case 0xc3:
				retValue = cms.readTasksForProject(project, C_TASKS_DONE, orderBy, groupBy);
				break;

			case 0xd1:
				retValue = cms.readGivenTasks(project, userName, C_TASKS_NEW, orderBy, groupBy);
				break;
			case 0xd2:
				retValue = cms.readGivenTasks(project, userName, C_TASKS_ACTIVE, orderBy, groupBy);
				break;
			case 0xd3:
				retValue = cms.readGivenTasks(project, userName, C_TASKS_DONE, orderBy, groupBy);
				break;

			default:
				retValue = cms.readTasksForUser(project, userName, C_TASKS_ALL, orderBy, groupBy);
				break;
		}
		
		if(retValue == null) {
			return new Vector();
		} else {
			return retValue;
		}
    }
	
	/**
	 * Reads alls tasks for all roles of a user.
	 * @param cms The cms object.
	 * @param project The name of the project.
	 * @param taskType The type of the task to read.
	 * @param orderBy A order criteria.
	 * @param groupBy A sort criteria.
	 * @return a vector of tasks.
	 * @exception Throws a exception, if something goes wrong.
	 */
	private Vector readTasksForRole(A_CmsObject cms, int project, int taskType, String orderBy, String groupBy)
		throws CmsException {
		String name = cms.getRequestContext().currentUser().getName();
		Vector groups = cms.getGroupsOfUser(name);
		A_CmsGroup group;
		Vector tasks;
		Vector allTasks = new Vector();
		
		for(int i = 0; i < groups.size(); i++) {
			group = (A_CmsGroup) groups.elementAt(i);
			tasks = cms.readTasksForRole(project, group.getName(), taskType, orderBy, groupBy);
			
			// join the vectors
			for(int z = 0; z < tasks.size(); z++) {
				allTasks.addElement(tasks.elementAt(z));
			}
		}
		return allTasks;
	}
}

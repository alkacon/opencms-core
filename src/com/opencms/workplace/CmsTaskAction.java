/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsTaskAction.java,v $
 * Date   : $Date: 2000/03/15 14:32:15 $
 * Version: $Revision: 1.1 $
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
 * This helper-class is used to do task-actions like create or forward. It uses the
 * workplace-languagefile to add task-logs in the language of the user.
 * <P>
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 2000/03/15 14:32:15 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsTaskAction implements I_CmsConstants, I_CmsWpConstants {

	/**
	 * Constant for generating user javascriptlist
	 */
	private final static String C_ALL_ROLES = "___all";

	public static void accept(A_CmsObject cms, int taskid) 
		throws CmsException {
		cms.acceptTask(taskid);
		String comment = "";
		cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_ACCEPTED);
	}
	
	public static void take(A_CmsObject cms, int taskid) 
		throws CmsException {
		CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
		A_CmsRequestContext context = cms.getRequestContext();
		A_CmsTask task = cms.readTask(taskid);
		A_CmsUser newEditor = context.currentUser();
		A_CmsGroup oldRole = cms.readGroup(task);
		// has the user the correct role?
		if(cms.userInGroup(newEditor.getName(), oldRole.getName())) {
			cms.forwardTask(taskid, oldRole.getName(), newEditor.getName());
			String comment = lang.getLanguageValue("task.dialog.take.logmessage");
			comment += " " + Utils.getFullName(newEditor);
			cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_TAKE);
		}				
	}
	
	public static void forward(A_CmsObject cms, int taskid,
							   String newEditorName, String newRoleName)
		throws CmsException {
		CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
		A_CmsUser newEditor = cms.readUser(newEditorName);
		A_CmsGroup oldRole = cms.readGroup(newRoleName);

		cms.forwardTask(taskid, oldRole.getName(), newEditor.getName());

		String comment = lang.getLanguageValue("task.dialog.forward.logmessage");
		comment += " " + Utils.getFullName(newEditor);
		cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_FORWARDED);
	}
	
	public static void due(A_CmsObject cms, int taskid,
						   String timeoutString)
		throws CmsException {
		CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
		A_CmsTask task = cms.readTask(taskid);
		String splittetDate[] = Utils.split(timeoutString, ".");
		GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(splittetDate[2]),
													  Integer.parseInt(splittetDate[1]) - 1,
													  Integer.parseInt(splittetDate[0]), 0, 0, 0);
		long timeout = cal.getTime().getTime();
		cms.setTimeout(taskid, timeout);
		// add comment
		String comment = "";
		comment += lang.getLanguageValue("task.dialog.due.logmessage1") + " ";
		comment += Utils.getNiceShortDate(task.getTimeOut().getTime()) + " ";
		comment += lang.getLanguageValue("task.dialog.due.logmessage2") + " ";
		comment += Utils.getNiceShortDate(timeout);
		cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_DUECHANGED);
	}

	public static void priority(A_CmsObject cms, int taskid, 
								String priorityString) 
		throws CmsException {
		CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
		A_CmsTask task = cms.readTask(taskid);
		int priority = Integer.parseInt(priorityString);
		cms.setPriority(taskid, priority);

		// add comment
		String comment = "";
		comment += lang.getLanguageValue("task.dialog.priority.logmessage1") + " ";
		comment += lang.getLanguageValue("task.dialog.priority.logmessageprio" + task.getPriority() ) + " ";
		comment += lang.getLanguageValue("task.dialog.priority.logmessage2") + " ";
		comment += lang.getLanguageValue("task.dialog.priority.logmessageprio" + priority ) + " ";
		cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_PRIORITYCHANGED);
	}
	
	public static void reakt(A_CmsObject cms, int taskid, 
							 String agentName, String roleName, String taskName, 
							 String taskcomment, String timeoutString, 
							 String priorityString, String paraAcceptation,
							 String paraAll, String paraCompletion, String paraDelivery) 
		throws CmsException {
		CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
		A_CmsTask task = cms.readTask(taskid);
		if( roleName.equals(C_ALL_ROLES) ) {
			roleName = cms.readUser(agentName).getDefaultGroup().getName();
		}
		cms.setName(taskid, taskName);
				
		// try to reaktivate the task
		cms.reaktivateTask(taskid);
		int priority = Integer.parseInt(priorityString);
		cms.setPriority(taskid, priority);
		// create a long from the overgiven date.
		String splittetDate[] = Utils.split(timeoutString, ".");
		GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(splittetDate[2]),
													  Integer.parseInt(splittetDate[1]) - 1,
													  Integer.parseInt(splittetDate[0]), 0, 0, 0);
		long timeout = cal.getTime().getTime();
		cms.setTimeout(taskid, timeout);
				    		
		cms.setTaskPar(taskid,C_TASKPARA_ACCEPTATION, paraAcceptation);
		cms.setTaskPar(taskid,C_TASKPARA_ALL, paraAll);
		cms.setTaskPar(taskid,C_TASKPARA_COMPLETION, paraCompletion);
		cms.setTaskPar(taskid,C_TASKPARA_DELIVERY, paraDelivery);
		cms.setTaskPar(taskid,C_TASKPARA_COMMENT, taskcomment);
				
		cms.forwardTask(taskid, roleName, agentName);
				
		String comment = lang.getLanguageValue("task.label.forrole") + ": " + roleName + "\n";
		comment += lang.getLanguageValue("task.label.editor") + ": " +  Utils.getFullName(cms.readUser(agentName)) + "\n";
		comment += taskcomment;
		cms.writeTaskLog(task.getId(), comment, C_TASKLOGTYPE_REACTIVATED);
	}
	
	public static void end(A_CmsObject cms, int taskid) 
		throws CmsException {
		cms.endTask(taskid);
		String comment = "";
		cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_OK);
	}
	
	public static void message(A_CmsObject cms, int taskid, String message)
		throws CmsException {
		CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
		A_CmsTask task = cms.readTask(taskid);
		String comment = lang.getLanguageValue("task.dialog.message.head") + " ";
		if( (message != null) && (message.length() != 0)) {
			comment += Utils.getFullName(cms.readAgent(task)) + "\n";
			comment += message;
			cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_CALL);
		}
	}
	
	public static void query(A_CmsObject cms, int taskid, String message) 
		throws CmsException {
		CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
		A_CmsTask task = cms.readTask(taskid);
		String comment = lang.getLanguageValue("task.dialog.query.head") + " ";
		if( (message != null) && (message.length() != 0)) {
			comment += Utils.getFullName(cms.readOwner(task)) + "\n";
			comment += message;
			cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_CALL);
		}
	}
	
	public static void create(A_CmsObject cms, 
							 String agentName, String roleName, String taskName, 
							 String taskcomment, String timeoutString, 
							 String priorityString, String paraAcceptation,
							 String paraAll, String paraCompletion, String paraDelivery) 
		throws CmsException {
		CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
		if( roleName.equals(C_ALL_ROLES) ) {
			roleName = cms.readUser(agentName).getDefaultGroup().getName();
		}
			
		// try to create the task
		int priority = Integer.parseInt(priorityString);
		// create a long from the overgiven date.
		String splittetDate[] = Utils.split(timeoutString, ".");
		GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(splittetDate[2]),
													  Integer.parseInt(splittetDate[1]) - 1,
													  Integer.parseInt(splittetDate[0]), 0, 0, 0);
		long timeout = cal.getTime().getTime();
                		
		A_CmsTask task = cms.createTask(agentName, roleName, taskName, 
										taskcomment, timeout, priority);
		cms.setTaskPar(task.getId(),C_TASKPARA_ACCEPTATION, paraAcceptation);
		cms.setTaskPar(task.getId(),C_TASKPARA_ALL, paraAll);
		cms.setTaskPar(task.getId(),C_TASKPARA_COMPLETION, paraCompletion);
		cms.setTaskPar(task.getId(),C_TASKPARA_DELIVERY, paraDelivery);
		cms.setTaskPar(task.getId(),C_TASKPARA_COMMENT, taskcomment);
		String comment = lang.getLanguageValue("task.label.forrole") + ": " + roleName + "\n";
		comment += lang.getLanguageValue("task.label.editor") + ": " +  Utils.getFullName(cms.readUser(agentName)) + "\n";
		comment += taskcomment;
		cms.writeTaskLog(task.getId(), comment, C_TASKLOGTYPE_CREATED);
	}

}

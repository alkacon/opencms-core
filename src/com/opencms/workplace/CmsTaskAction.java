/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsTaskAction.java,v $
 * Date   : $Date: 2000/04/13 15:45:06 $
 * Version: $Revision: 1.5 $
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
 * @version $Revision: 1.5 $ $Date: 2000/04/13 15:45:06 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsTaskAction implements I_CmsConstants, I_CmsWpConstants {

	/**
	 * Constant for generating user javascriptlist
	 */
	private final static String C_ALL_ROLES = "___all";


	/**
	 * Accepts a task.
	 * @param cms The cms-object.
	 * @param int taskid The id of the task.
	 * @exception CmsException Throws CmsExceptions, that are be 
	 * thrown in calling methods.
	 */
	public static void accept(A_CmsObject cms, int taskid) 
		throws CmsException {
		cms.acceptTask(taskid);
		String comment = "";
		cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_ACCEPTED);
		
		// send an email if "Benachrichtigung bei Annahme" was selected.
		CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
		A_CmsTask task = cms.readTask(taskid);
		if (cms.getTaskPar(task.getId(),C_TASKPARA_ACCEPTATION)!=null) {	
			String content=lang.getLanguageValue("task.email.accept.content");
			String subject=lang.getLanguageValue("task.email.accept.subject");
			A_CmsUser[] users={cms.readAgent(task)};
			CmsMail mail=new CmsMail(cms,cms.readOwner(task),users,subject,content);
			mail.start();
		}
		
	}
	
	/**
	 * Takes a task. The calling user is now the agent for this task.
	 * @param cms The cms-object.
	 * @param int taskid The id of the task.
	 * @exception CmsException Throws CmsExceptions, that are be 
	 * thrown in calling methods.
	 */
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
		
		// send an email		
		String content=lang.getLanguageValue("task.email.take.content");
		String subject=lang.getLanguageValue("task.email.take.subject");
		A_CmsUser[] users={cms.readAgent(task)};
		CmsMail mail=new CmsMail(cms,cms.readOwner(task),users,subject,content);
		mail.start();
	}
	
	/**
	 * Forwards a task. The task is forwarded to a new editor in a new role.
	 * @param cms The cms-object.
	 * @param int taskid The id of the task.
	 * @param newEditorName The name of the new editor for this task.
	 * @param newRoleName The name of the new role for the user.
	 * @exception CmsException Throws CmsExceptions, that are be 
	 * thrown in calling methods.
	 */
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
		
		// send an email if "Benachrichtigung bei Weiterleitung" was selected.
		A_CmsTask task = cms.readTask(taskid);
		if (cms.getTaskPar(task.getId(),C_TASKPARA_DELIVERY)!=null) {	
			String content=lang.getLanguageValue("task.email.forward.content");
			String subject=lang.getLanguageValue("task.email.forward.subject");			
			// if "Alle Rollenmitglieder von Aufgabe Benachrichtigen" checkbox is selected.
			if (cms.getTaskPar(task.getId(),C_TASKPARA_ALL)!=null) {
				CmsMail mail=new CmsMail(cms,cms.getRequestContext().currentUser(),cms.readGroup(task),subject,content);
				mail.start();
			} else {
				// send a mail to user
				A_CmsUser[] user={cms.readAgent(task)};
				CmsMail mail1=new CmsMail(cms,cms.getRequestContext().currentUser(),user,subject,content);
				mail1.start();
				// send a mail to owner
				A_CmsUser[] owner={cms.readOwner(task)};
				CmsMail mail2=new CmsMail(cms,cms.getRequestContext().currentUser(),owner,subject,content);
				mail2.start();
			}
			
		}
	}
	
	/**
	 * Changes the timeou-date of the task.
	 * @param cms The cms-object.
	 * @param int taskid The id of the task.
	 * @param timeoutString The new timeout-date as a string in the following format:
	 * "dd.mm.yyyy"
	 * @exception CmsException Throws CmsExceptions, that are be 
	 * thrown in calling methods.
	 */
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

	/**
	 * Changes the priority of a task.
	 * @param cms The cms-object.
	 * @param int taskid The id of the task.
	 * @param priorityString the new priority as String ("1" = high, 
	 * "2" = normal or "3" = low)
	 * @exception CmsException Throws CmsExceptions, that are be 
	 * thrown in calling methods.
	 */
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
	
	/**
	 * Reaktivates a task.
	 * @param cms The cms-object.
	 * @param int taskid The id of the task.
	 * @param agentName The name of the new editor for this task.
	 * @param roleName The name of the new role for the user.
	 * @param taskName The new name of the task.
	 * @param taskcomment The new comment for this task.
	 * @param timeoutString The new timeout-date as a string in the following format:
	 * "dd.mm.yyyy"
	 * @param priorityString the new priority as String ("1" = high, 
	 * "2" = normal or "3" = low)
	 * @param paraAcceptation controls if a message should be send by acceptation. ("checked" | "")
	 * @param paraAll controls if a message should be send to all users in a role. ("checked" | "")
	 * @param paraCompletion controls if a message should be send by completing this task. ("checked" | "")
	 * @param paraDelivery controls if a message should be send by delivering a task. ("checked" | "")
	 * @exception CmsException Throws CmsExceptions, that are be 
	 * thrown in calling methods.
	 */
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
				
		cms.forwardTask(taskid, roleName, agentName);
				
		String comment = lang.getLanguageValue("task.label.forrole") + ": " + roleName + "\n";
		comment += lang.getLanguageValue("task.label.editor") + ": " +  Utils.getFullName(cms.readUser(agentName)) + "\n";
		comment += taskcomment;
		cms.writeTaskLog(task.getId(), comment, C_TASKLOGTYPE_REACTIVATED);
		
		// send an email
		String content=lang.getLanguageValue("task.email.reakt.content");
		String subject=lang.getLanguageValue("task.email.reakt.subject");
		A_CmsUser[] users={cms.readAgent(task)};
		CmsMail mail=new CmsMail(cms,cms.readOwner(task),users,subject,content);
		// if "Alle Rollenmitglieder von Aufgabe Benachrichtigen" checkbox is selected.
		if (cms.getTaskPar(task.getId(),C_TASKPARA_ALL)!=null) {
			mail=new CmsMail(cms,cms.readOwner(task),cms.readGroup(task),subject,content);
		}
		mail.start();
	}
	
	/**
	 * Ends a task.
	 * @param cms The cms-object.
	 * @param int taskid The id of the task.
	 * @exception CmsException Throws CmsExceptions, that are be 
	 * thrown in calling methods.
	 */
	public static void end(A_CmsObject cms, int taskid) 
		throws CmsException {
		cms.endTask(taskid);
		String comment = "";
		cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_OK);
		// send an email if "Benachrichtigung bei Abhacken" was selected.
		CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
		A_CmsTask task = cms.readTask(taskid);
		if (cms.getTaskPar(task.getId(),C_TASKPARA_COMPLETION)!=null) {	
			String content=lang.getLanguageValue("task.email.end.content");
			String subject=lang.getLanguageValue("task.email.end.subject");
			A_CmsUser[] users={cms.readOwner(task)};
			CmsMail mail=new CmsMail(cms,cms.readAgent(task),users,subject,content);
			mail.start();
		}
	}
	
	/**
	 * Sends a message to the editor of the task.
	 * @param cms The cms-object.
	 * @param int taskid The id of the task.
	 * @param message The text of the message.
	 * @exception CmsException Throws CmsExceptions, that are be 
	 * thrown in calling methods.
	 */
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
		
		// send an email 
		String content=lang.getLanguageValue("task.email.message.content");
		String subject=lang.getLanguageValue("task.email.message.subject");
		A_CmsUser[] users={cms.readAgent(task)};
		CmsMail mail=new CmsMail(cms,cms.readOwner(task),users,subject,content);
		mail.start();
	}
	
	/**
	 * Sends a message to the initiator (owner) of the task.
	 * @param cms The cms-object.
	 * @param int taskid The id of the task.
	 * @param message The text of the message.
	 * @exception CmsException Throws CmsExceptions, that are be 
	 * thrown in calling methods.
	 */
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
		
		// send an email.
		String content=lang.getLanguageValue("task.email.query.content");
		String subject=lang.getLanguageValue("task.email.query.subject");
		A_CmsUser[] users={cms.readOwner(task)};
		CmsMail mail=new CmsMail(cms,cms.readAgent(task),users,subject,content);
		mail.start();
		
	}
	
	/**
	 * Creates a new task.
	 * @param cms The cms-object.
	 * @param agentName The name of the new editor for this task.
	 * @param roleName The name of the new role for the user.
	 * @param taskName The new name of the task.
	 * @param taskcomment The new comment for this task.
	 * @param timeoutString The new timeout-date as a string in the following format:
	 * "dd.mm.yyyy"
	 * @param priorityString the new priority as String ("1" = high, 
	 * "2" = normal or "3" = low)
	 * @param paraAcceptation controls if a message should be send by acceptation. ("checked" | "")
	 * @param paraAll controls if a message should be send to all users in a role. ("checked" | "")
	 * @param paraCompletion controls if a message should be send by completing this task. ("checked" | "")
	 * @param paraDelivery controls if a message should be send by delivering a task. ("checked" | "")
	 * @exception CmsException Throws CmsExceptions, that are be 
	 * thrown in calling methods.
	 */
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
		String comment = lang.getLanguageValue("task.label.forrole") + ": " + roleName + "\n";
		comment += lang.getLanguageValue("task.label.editor") + ": " +  Utils.getFullName(cms.readUser(agentName)) + "\n";
		comment += taskcomment;
		
		cms.writeTaskLog(task.getId(), comment, C_TASKLOGTYPE_CREATED);
		// send an email
		
		// per default send a mail from task's organizer to task's recipient.
		
		String content=lang.getLanguageValue("task.email.create.content");
		String subject=lang.getLanguageValue("task.email.create.subject");
		A_CmsUser[] users={cms.readAgent(task)};
		CmsMail mail=new CmsMail(cms,cms.readOwner(task),users,subject,content);
		
		// if "Alle Rollenmitglieder von Aufgabe Benachrichtigen" checkbox is selected.
		if (cms.getTaskPar(task.getId(),C_TASKPARA_ALL)!=null) {
			mail=new CmsMail(cms,cms.readOwner(task),cms.readGroup(task),subject,content);
		}
		
		mail.start();
		
	}

	/**
	 * Gets the description of a task.
	 * The description is stored in the task-log.
	 * @param cms The cms-object.
	 * @param int taskid The id of the task.
	 * @return String the comment-string.
	 * @exception CmsException Throws CmsExceptions, that are be 
	 * thrown in calling methods.
	 */
	public static String getDescription(A_CmsObject cms, int taskid)
		throws CmsException {
		StringBuffer retValue = new StringBuffer("");
		CmsTaskLog tasklog;
		
		Vector taskdocs=cms.readTaskLogs(taskid);
        
        // go through all tasklogs to find the comment
        for (int i=1;i<=taskdocs.size();i++) {
            tasklog = (CmsTaskLog)taskdocs.elementAt(taskdocs.size()-i);
            int type=tasklog.getType();
            // check if this is a type "created" or "new"
            if ( (type == C_TASKLOGTYPE_CREATED) || (type == C_TASKLOGTYPE_REACTIVATED)) {
				String comment[] = Utils.split(tasklog.getComment(), "\n");
				for(int j = 2; j < comment.length; j++) {
					retValue.append(comment[j] + "\n");
				}
				break;
	        }     
        }
		
		return retValue.toString();
	}
}

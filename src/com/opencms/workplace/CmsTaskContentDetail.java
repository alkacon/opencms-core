/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsTaskContentDetail.java,v $
 * Date   : $Date: 2000/05/11 10:18:40 $
 * Version: $Revision: 1.15 $
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
import java.io.*;

import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace task content detail screens.
 * <P>
 * 
 * @author Andreas Schouten
 * @author Mario Stanke
 * @version $Revision: 1.15 $ $Date: 2000/05/11 10:18:40 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsTaskContentDetail extends CmsWorkplaceDefault implements I_CmsConstants, I_CmsWpConstants {
	
	/**
	 * Constant for generating user javascriptlist
	 */
	private final static String C_ALL_ROLES = "___all";
	
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
		
		A_CmsRequestContext context = cms.getRequestContext();
		CmsXmlWpTemplateFile xmlTemplateDocument = 
			(CmsXmlWpTemplateFile) getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		A_CmsTask task;
		int taskid = -1;
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
		
		// getting the URL to which we need to return when we're done
		String lastUrl;
		String lastRelUrl = (String) parameters.get("lastrelurl");
		if (lastRelUrl != null) {
			// have a URL relative to the workplace action path (when clicking on the task name)
			lastUrl = getConfigFile(cms).getWorkplaceActionPath()+lastRelUrl;
			session.putValue("lasturl", lastUrl);
		} else {
			// have the complete path (when using the context menu)
			lastUrl=this.getLastUrl(cms, parameters);
		} 
		
		try {
			Integer sessionTaskid = (Integer)session.getValue("taskid");
			if(sessionTaskid != null) {
				taskid = sessionTaskid.intValue();
			}
			
			try {
				taskid = Integer.parseInt((String)parameters.get("taskid"));
			} catch(Exception exc) {
				// no new taskid use the one from session
			}
			session.putValue("taskid", new Integer(taskid));
			parameters.put("taskid", taskid + "");
			task = cms.readTask(taskid);
			
			if("acceptok".equals((String)parameters.get("action"))){
				// accept the task
				CmsTaskAction.accept(cms, taskid);
			} else if("accept".equals((String)parameters.get("action"))){
				// show dialog
				templateSelector = "accept";
			} else if("take".equals((String)parameters.get("action"))){
				// show dialog
				templateSelector = "take";
			} else if("takeok".equals((String)parameters.get("action"))){
				// take the task
				CmsTaskAction.take(cms, taskid);
			} else if("forwardok".equals((String)parameters.get("action"))){
				// forward the task 
				CmsTaskAction.forward(cms, taskid, (String)parameters.get("USER"),
									  (String)parameters.get("TEAM"));
			} else if("due".equals((String)parameters.get("action"))){
				// show dialog
				templateSelector = "due";
			} else if("dueok".equals((String)parameters.get("action"))){
				// change the due-date of the task
				CmsTaskAction.due(cms, taskid, (String)parameters.get("DATE"));
			} else if("priorityok".equals((String)parameters.get("action"))){
				// change the priority of the task
				CmsTaskAction.priority(cms, taskid, 
									   (String)parameters.get("PRIORITY"));
			} else if("reaktok".equals((String)parameters.get("action"))){
				// reaktivate the task
				CmsTaskAction.reakt(cms, taskid, (String)parameters.get("USER"),
									(String)parameters.get("TEAM"), 
									(String)parameters.get("TASK"), 
									(String)parameters.get("DESCRIPTION"),
									(String)parameters.get("DATE"),
									(String)parameters.get("PRIORITY"),
									(String)parameters.get("MSG_ACCEPTATION"),
									(String)parameters.get("MSG_ALL"),
									(String)parameters.get("MSG_COMPLETION"),
									(String)parameters.get("MSG_DELIVERY"));
			} else if("okok".equals((String)parameters.get("action"))){
				// ok the task
				CmsTaskAction.end(cms, taskid);
			} else if("ok".equals((String)parameters.get("action"))) {
				// show dialog
				templateSelector = "ok";
			} else if("comment".equals((String)parameters.get("action"))) {
				// add comment
				String comment = (String)parameters.get("DESCRIPTION");
				if( (comment != null) && (comment.length() != 0)) {
					cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_COMMENT);
				}
			} else if("messageok".equals((String)parameters.get("action"))) {
				// add message
				CmsTaskAction.message(cms, taskid, (String)parameters.get("DESCRIPTION"));
			} else if("queryok".equals((String)parameters.get("action"))) {
				// add message
				CmsTaskAction.query(cms, taskid, (String)parameters.get("DESCRIPTION"));
			}
			
			// update the task-data
			// it maybe had been changed
			task = cms.readTask(taskid);
		} catch (Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
		
		A_CmsUser owner = null;
		String ownerName = "";
		try {
			owner = cms.readOwner(task);
			ownerName = owner.getName();
		} catch(Exception exc) {
			// ignore the exception
		}

		A_CmsUser editor = null;
		String editorName = "";
		try {
			editor = cms.readAgent(task);
			editorName = editor.getName();
		} catch(Exception exc) {
			// ignore the exception
		}
		
		A_CmsGroup role = null;
		String roleName = "";
		try {
			role = cms.readGroup(task);
			roleName = role.getName();
		} catch(Exception exc) {
			// ignore the exception
		}

		String priority;
		String projectname = "?";
		String style;
		String button1;
		String button2;
		String button3;
		String button4;
		String button5;
		String button6;
		long startTime;
		long timeout;
		GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new Date(System.currentTimeMillis()));
        cal.set(Calendar.HOUR,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        
        GregorianCalendar newcal = new GregorianCalendar(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),
                                                         cal.get(Calendar.DAY_OF_MONTH),0,0,0);
        
        long now = newcal.getTime().getTime();

		try {
			projectname = cms.readTask(task.getRoot()).getName();
		} catch(Exception exc) {
			// no root?!
		}
		priority = xmlTemplateDocument.getProcessedDataValue("priority" + task.getPriority(), this);
		startTime = task.getStartTime().getTime();
		timeout = task.getTimeOut().getTime();
		String due = "";
		try {
			due = Utils.getNiceShortDate(timeout);
		} catch(Exception exc) {
			// ignore the exception
		}

		String from = "";
		try {
			from = Utils.getNiceShortDate(startTime);
		} catch(Exception exc) {
			// ignore the exception
		}

		xmlTemplateDocument.setData("taskid", task.getId() + "");
		
		boolean isOwner = context.currentUser().equals(owner);
		boolean isEditor = context.currentUser().equals(editor);
		boolean isInRole = false;
		try {
			isInRole = cms.userInGroup(context.currentUser().getName(), roleName);
		} catch(Exception exc) {
			// ignore the exception
		} 
		
		// choose the right style and buttons
		if(task.getState() == C_TASK_STATE_ENDED) {
			if(isOwner) {
				// this is the owner of the task
				button1 = getButton(xmlTemplateDocument, "button_message", false);
				button2 = getButton(xmlTemplateDocument, "button_accept", false);
				button3 = getButton(xmlTemplateDocument, "button_due", false);
				button4 = getButton(xmlTemplateDocument, "button_priority", false);
				button5 = getButton(xmlTemplateDocument, "button_comment", false);
				button6 = getButton(xmlTemplateDocument, "button_reakt", true);
			} else if(isEditor) {
				// this user is the editor for this task
				button1 = getButton(xmlTemplateDocument, "button_query", false);
				button2 = getButton(xmlTemplateDocument, "button_forward", false);
				button3 = getButton(xmlTemplateDocument, "button_due", false);
				button4 = getButton(xmlTemplateDocument, "button_priority", false);
				button5 = getButton(xmlTemplateDocument, "button_comment", false);
				button6 = getButton(xmlTemplateDocument, "button_reakt", false);
			} else if(isInRole) {
				// this user is in the role for this task
				button1 = getButton(xmlTemplateDocument, "button_query", false);
				button2 = getButton(xmlTemplateDocument, "button_take", false);
				button3 = getButton(xmlTemplateDocument, "button_due", false);
				button4 = getButton(xmlTemplateDocument, "button_priority", false);
				button5 = getButton(xmlTemplateDocument, "button_comment", false);
				button6 = getButton(xmlTemplateDocument, "button_reakt", false);
			} else {
				// all other users
				button1 = getButton(xmlTemplateDocument, "button_query", false);
				button2 = getButton(xmlTemplateDocument, "button_take", false);
				button3 = getButton(xmlTemplateDocument, "button_due", false);
				button4 = getButton(xmlTemplateDocument, "button_priority", false);
				button5 = getButton(xmlTemplateDocument, "button_comment", false);
				button6 = getButton(xmlTemplateDocument, "button_reakt", false);
			}
			if(timeout < now ) {
				style = xmlTemplateDocument.getProcessedDataValue("style_ok", this);
			} else {
				style = xmlTemplateDocument.getProcessedDataValue("style_ok", this);
			}
		} else if(task.getPercentage() == 0) {
			if(isOwner && isEditor) {
				// this is a task from me and for me
				button1 = getButton(xmlTemplateDocument, "button_query", true);
				button2 = getButton(xmlTemplateDocument, "button_accept", true);
				button3 = getButton(xmlTemplateDocument, "button_due", true);
				button4 = getButton(xmlTemplateDocument, "button_priority", true);
				button5 = getButton(xmlTemplateDocument, "button_comment", true);
				button6 = getButton(xmlTemplateDocument, "button_ok", false);
			} else if(isOwner) {
				// this is the owner of the task
				button1 = getButton(xmlTemplateDocument, "button_message", true);
				button2 = getButton(xmlTemplateDocument, "button_forward", true);
				button3 = getButton(xmlTemplateDocument, "button_due", true);
				button4 = getButton(xmlTemplateDocument, "button_priority", true);
				button5 = getButton(xmlTemplateDocument, "button_comment", true);
				button6 = getButton(xmlTemplateDocument, "button_ok", false);
			} else if(isEditor) {
				// this user is the editor for this task
				button1 = getButton(xmlTemplateDocument, "button_query", true);
				button2 = getButton(xmlTemplateDocument, "button_accept", true);
				button3 = getButton(xmlTemplateDocument, "button_due", false);
				button4 = getButton(xmlTemplateDocument, "button_priority", false);
				button5 = getButton(xmlTemplateDocument, "button_comment", true);
				button6 = getButton(xmlTemplateDocument, "button_ok", false);
			} else if(isInRole) {
				// this user is in the role for this task
				button1 = getButton(xmlTemplateDocument, "button_query", false);
				button2 = getButton(xmlTemplateDocument, "button_take", true);
				button3 = getButton(xmlTemplateDocument, "button_due", false);
				button4 = getButton(xmlTemplateDocument, "button_priority", false);
				button5 = getButton(xmlTemplateDocument, "button_comment", false);
				button6 = getButton(xmlTemplateDocument, "button_ok", false);
			} else { 
				// all other users
				button1 = getButton(xmlTemplateDocument, "button_query", false);
				button2 = getButton(xmlTemplateDocument, "button_take", false);
				button3 = getButton(xmlTemplateDocument, "button_due", false);
				button4 = getButton(xmlTemplateDocument, "button_priority", false);
				button5 = getButton(xmlTemplateDocument, "button_comment", false);
				button6 = getButton(xmlTemplateDocument, "button_ok", false);
			}
			if(timeout < now ) {
				style = xmlTemplateDocument.getProcessedDataValue("style_alert", this);
			} else {
				style = xmlTemplateDocument.getProcessedDataValue("style_new", this);
			}
		} else {
			if(isOwner && isEditor) {
				// this is a task from me and for me
				button1 = getButton(xmlTemplateDocument, "button_query", true);
				button2 = getButton(xmlTemplateDocument, "button_forward", true);
				button3 = getButton(xmlTemplateDocument, "button_due", true);
				button4 = getButton(xmlTemplateDocument, "button_priority", true);
				button5 = getButton(xmlTemplateDocument, "button_comment", true);
				button6 = getButton(xmlTemplateDocument, "button_ok", true);
			} else if(isOwner) {
				// this is the owner of the task
				button1 = getButton(xmlTemplateDocument, "button_message", true);
				button2 = getButton(xmlTemplateDocument, "button_forward", true);
				button3 = getButton(xmlTemplateDocument, "button_due", true);
				button4 = getButton(xmlTemplateDocument, "button_priority", true);
				button5 = getButton(xmlTemplateDocument, "button_comment", true);
				button6 = getButton(xmlTemplateDocument, "button_ok", false);
			} else if(isEditor) {
				// this user is the editor for this task
				button1 = getButton(xmlTemplateDocument, "button_query", true);
				button2 = getButton(xmlTemplateDocument, "button_forward", true);
				button3 = getButton(xmlTemplateDocument, "button_due", false);
				button4 = getButton(xmlTemplateDocument, "button_priority", false);
				button5 = getButton(xmlTemplateDocument, "button_comment", true);
				button6 = getButton(xmlTemplateDocument, "button_ok", true);
			} else if(isInRole) {
				// this user is in the role for this task
				button1 = getButton(xmlTemplateDocument, "button_query", false);
				button2 = getButton(xmlTemplateDocument, "button_take", true);
				button3 = getButton(xmlTemplateDocument, "button_due", false);
				button4 = getButton(xmlTemplateDocument, "button_priority", false);
				button5 = getButton(xmlTemplateDocument, "button_comment", false);
				button6 = getButton(xmlTemplateDocument, "button_ok", false);
			} else {
				// all other users
				button1 = getButton(xmlTemplateDocument, "button_query", false);
				button2 = getButton(xmlTemplateDocument, "button_take", false);
				button3 = getButton(xmlTemplateDocument, "button_due", false);
				button4 = getButton(xmlTemplateDocument, "button_priority", false);
				button5 = getButton(xmlTemplateDocument, "button_comment", false);
				button6 = getButton(xmlTemplateDocument, "button_ok", false);
			}
			if(timeout < now ) {
				style = xmlTemplateDocument.getProcessedDataValue("style_alert", this);
			} else {
				style = xmlTemplateDocument.getProcessedDataValue("style_activ", this);
			}
		}
			  
		// get the processed list.
		xmlTemplateDocument.setData("style", style);
		xmlTemplateDocument.setData("priority", priority);
		xmlTemplateDocument.setData("task", task.getName());
		xmlTemplateDocument.setData("foruser", Utils.getFullName(editor));
		xmlTemplateDocument.setData("forrole", roleName);
		xmlTemplateDocument.setData("actuator", Utils.getFullName(owner));
		xmlTemplateDocument.setData("due", due);
		xmlTemplateDocument.setData("from", from);
		xmlTemplateDocument.setData("project", projectname);
		
		// now setting the buttons
		xmlTemplateDocument.setData("button1", button1);
		xmlTemplateDocument.setData("button2", button2);
		xmlTemplateDocument.setData("button3", button3);
		xmlTemplateDocument.setData("button4", button4);
		xmlTemplateDocument.setData("button5", button5);
		xmlTemplateDocument.setData("button6", button6); 
		
		// now check where to go back
		if ((templateSelector == null || templateSelector=="") && lastUrl != null) {   
			// tasks either completed or aborted, go back
			try { 
				if (lastUrl.startsWith("http:")) {
					// complete path 
					((HttpServletResponse) context.getResponse().getOriginalResponse()).sendRedirect(lastUrl);
				} else {
					// relative to the opencms path
					context.getResponse().sendCmsRedirect(lastUrl);	
				}
				session.removeValue("lasturl");
			} catch(IOException exc) {
				throw new CmsException("Could not redirect to " + lastUrl, exc);
			}
            return null;
		}
		// Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

	/**
	 * This private helper method generates a string-representation of a button.
	 * @param xmlTemplateDocument The xml-document in which the buttons are defined.
	 * @param name The name of the button to generate.
	 * @param enabled True, if the button is enabled, else false.
	 * @return the string-representation of the button.
	 * @exception Throws CmsException, if something goes wrong.
	 */
	private String getButton(CmsXmlWpTemplateFile xmlTemplateDocument, String name, 
							 boolean enabled) 
		throws CmsException {
		if(enabled) {
			// the button is enabled
			xmlTemplateDocument.setData("disabled", "");
		} else {
			// the button is disabled
			xmlTemplateDocument.setData("disabled", "disabled");
		}
		// return the generated button
		return xmlTemplateDocument.getProcessedDataValue(name, this);		
	}
}

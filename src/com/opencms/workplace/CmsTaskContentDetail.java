/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsTaskContentDetail.java,v $
 * Date   : $Date: 2000/02/29 16:44:48 $
 * Version: $Revision: 1.7 $
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
 * Template class for displaying OpenCms workplace task content detail screens.
 * <P>
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.7 $ $Date: 2000/02/29 16:44:48 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsTaskContentDetail extends CmsWorkplaceDefault implements I_CmsConstants, I_CmsWpConstants {
	
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
		
        // TODO: check, if this is neede: A_CmsRequestContext reqCont = cms.getRequestContext();
		CmsXmlWpTemplateFile xmlTemplateDocument = 
			(CmsXmlWpTemplateFile) getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		A_CmsTask task;
		int taskid = -1;
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
		
		
		try {
			Integer sessionTaskid = (Integer)session.getValue("taskid");
			if(sessionTaskid != null) {
				taskid = sessionTaskid.intValue();
			}
			
			try {
				taskid = Integer.parseInt((String)parameters.get("taskid"));
			} catch(Exception exc) {
				exc.printStackTrace();
			}
			session.putValue("taskid", new Integer(taskid));
			parameters.put("taskid", taskid + "");
			
			if("acceptok".equals((String)parameters.get("action"))){
				// accept the task
				cms.acceptTask(taskid);
				// TODO: this must be read from a xml-language-file
				String comment = "";
				cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_ACCEPTED);
			} else if("accept".equals((String)parameters.get("action"))){
				// show dialog
				templateSelector = "accept";
			} else if("okok".equals((String)parameters.get("action"))){
				// ok the task
				cms.endTask(taskid);
				// TODO: this must be read from a xml-language-file
				String comment = "";
				cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_OK);
			} else if("ok".equals((String)parameters.get("action"))) {
				// show dialog
				templateSelector = "ok";
			}
			
			task = cms.readTask(taskid);
		} catch (Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
		
		
		String priority;
		String projectname = "?";
		String style;
		String button2;
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
		priority = xmlTemplateDocument.getProcessedXmlDataValue("priority" + task.getPriority(), this);
		startTime = task.getStartTime().getTime();
		timeout = task.getTimeOut().getTime();

		xmlTemplateDocument.setXmlData("taskid", task.getId() + "");
			
		// choose the right style and buttons
		if(task.getState() == C_TASK_STATE_ENDED) {
			button2 = xmlTemplateDocument.getProcessedXmlDataValue("button_forward", this);
			button6 = xmlTemplateDocument.getProcessedXmlDataValue("button_reakt", this);
			if(timeout < now ) {
				style = xmlTemplateDocument.getProcessedXmlDataValue("style_ok", this);
			} else {
				style = xmlTemplateDocument.getProcessedXmlDataValue("style_ok", this);
			}
		} else if(task.getPercentage() == 0) {
			button2 = xmlTemplateDocument.getProcessedXmlDataValue("button_accept", this);
			button6 = xmlTemplateDocument.getProcessedXmlDataValue("button_ok", this);
			if(timeout < now ) {
				style = xmlTemplateDocument.getProcessedXmlDataValue("style_alert", this);
			} else {
				style = xmlTemplateDocument.getProcessedXmlDataValue("style_new", this);
			}
		} else {
			button2 = xmlTemplateDocument.getProcessedXmlDataValue("button_take", this);
			button6 = xmlTemplateDocument.getProcessedXmlDataValue("button_ok", this);
			if(timeout < now ) {
				style = xmlTemplateDocument.getProcessedXmlDataValue("style_alert", this);
			} else {
				style = xmlTemplateDocument.getProcessedXmlDataValue("style_activ", this);
			}
		}
			  
		String agent = "";
		String group = "";
		String owner = "";
		String due = "";
		String from = "";
		try {
			agent = cms.readAgent(task).getName();
		} catch(Exception exc) {
			// ignore the exception
		}
		try {
			group = cms.readGroup(task).getName();
		} catch(Exception exc) {
			// ignore the exception
		}
		try {
			owner = cms.readOwner(task).getName();
		} catch(Exception exc) {
			// ignore the exception
		}
		try {
			due = Utils.getNiceShortDate(timeout);
		} catch(Exception exc) {
			// ignore the exception
		}
		try {
			from = Utils.getNiceShortDate(startTime);
		} catch(Exception exc) {
			// ignore the exception
		}
		// get the processed list.
		xmlTemplateDocument.setXmlData("style", style);
		xmlTemplateDocument.setXmlData("priority", priority);
		xmlTemplateDocument.setXmlData("task", task.getName());
		xmlTemplateDocument.setXmlData("foruser", agent);
		xmlTemplateDocument.setXmlData("forrole", group);
		xmlTemplateDocument.setXmlData("actuator", owner);
		xmlTemplateDocument.setXmlData("due", due);
		xmlTemplateDocument.setXmlData("from", from);
		xmlTemplateDocument.setXmlData("project", projectname);
		
		// now setting the buttons
		xmlTemplateDocument.setXmlData("button2", button2);
		xmlTemplateDocument.setXmlData("button6", button6);
		
		// Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }
}

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
 * @version $Revision: 1.4 $ $Date: 2000/02/21 14:21:02 $
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
		
        A_CmsRequestContext reqCont = cms.getRequestContext();
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
		
		
        /** StringBuffer for the generated output */
        StringBuffer result = new StringBuffer();
		String priority;
		String projectname = "?";
		String style;
		String button2;
		String button6;
		long startTime;
		long timeout;
		long now = new Date().getTime();

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
				style = xmlTemplateDocument.getProcessedXmlDataValue("style_alertok", this);
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
			  
		// get the processed list.
		xmlTemplateDocument.setXmlData("style", style);
		xmlTemplateDocument.setXmlData("priority", priority);
		xmlTemplateDocument.setXmlData("task", task.getName());
		xmlTemplateDocument.setXmlData("foruser", cms.readAgent(task).getName());
		xmlTemplateDocument.setXmlData("forrole", cms.readGroup(task).getName());
		xmlTemplateDocument.setXmlData("actuator", cms.readOwner(task).getName());
		xmlTemplateDocument.setXmlData("due", Utils.getNiceShortDate(timeout));
		xmlTemplateDocument.setXmlData("from", Utils.getNiceShortDate(startTime));
		xmlTemplateDocument.setXmlData("project", projectname);
		
		// now setting the buttons
		xmlTemplateDocument.setXmlData("button2", button2);
		xmlTemplateDocument.setXmlData("button6", button6);
		
		// Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }
}

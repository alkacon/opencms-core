package com.opencms.workplace;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.util.*;

import java.util.*;
import java.lang.reflect.*;

/**
 * Class for building task list. <BR>
 * Called by CmsXmlTemplateFile for handling the special XML tag <code>&lt;tasklist&gt;</code>.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.7 $ $Date: 2000/02/22 11:22:42 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsTaskList extends A_CmsWpElement implements I_CmsWpElement, I_CmsWpConstants, I_CmsConstants {
	
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
     * Handling of the special workplace <CODE>&lt;TASKLIST&gt;</CODE> tags.
     * <P>
     * Returns the processed code with the actual elements.
     * <P>
     * Projectlists can be referenced in any workplace template by <br>
     * // TODO: insert correct syntax here!
     * <CODE>&lt;TASKLIST /&gt;</CODE>
     * 
     * @param cms A_CmsObject Object for accessing resources.
     * @param n XML element containing the <code>&lt;ICON&gt;</code> tag.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @param lang CmsXmlLanguageFile conataining the currently valid language file.
     * @return Processed button.
     * @exception CmsException
     */    
    public Object handleSpecialWorkplaceTag(A_CmsObject cms, Element n, A_CmsXmlContent doc, Object callingObject, Hashtable parameters, CmsXmlLanguageFile lang) throws CmsException {
        
        // Get list definition values
        CmsXmlWpTemplateFile listdef = getTaskListDefinitions(cms);
		
        String listMethod = n.getAttribute("method");

        // call the method for generating projectlist elements
        Method callingMethod = null;
		Vector list = new Vector();
        try {
            callingMethod = callingObject.getClass().getMethod(listMethod, new Class[] {A_CmsObject.class, CmsXmlLanguageFile.class});
            list = (Vector)callingMethod.invoke(callingObject, new Object[] {cms, lang});
        } catch(NoSuchMethodException exc) {
            // The requested method was not found.
            throwException("Could not find method " + listMethod + " in calling class " + callingObject.getClass().getName() + " for generating lasklist content.", CmsException.C_NOT_FOUND);
        } catch(InvocationTargetException targetEx) {
            // the method could be invoked, but throwed a exception
            // itself. Get this exception and throw it again.              
            Throwable e = targetEx.getTargetException();
            if(!(e instanceof CmsException)) {
                // Only print an error if this is NO CmsException
                throwException("User method " + listMethod + " in calling class " + callingObject.getClass().getName() + " throwed an exception. " + e, CmsException.C_UNKNOWN_EXCEPTION);
            } else {
                // This is a CmsException
                // Error printing should be done previously.
                throw (CmsException)e;
            }
        } catch(Exception exc2) {
            throwException("User method " + listMethod + " in calling class " + callingObject.getClass().getName() + " was found but could not be invoked. " + exc2, CmsException.C_XML_NO_USER_METHOD);
        }
		
        /** StringBuffer for the generated output */
        StringBuffer result = new StringBuffer();
		String priority;
		String projectname;
		String stateIcon;
		String style;
		long startTime;
		long timeout;
		long now = new Date().getTime();

		for(int i = 0; i < list.size(); i++) {
			// get the actual project
			A_CmsTask task = (A_CmsTask) list.elementAt(i);
			projectname = "?";
			try {
				projectname = cms.readTask(task.getRoot()).getName();
			} catch(Exception exc) {
				// no root?!
			}
			priority = listdef.getProcessedXmlDataValue("priority" + task.getPriority(), callingObject);
			startTime = task.getStartTime().getTime();
			timeout = task.getTimeOut().getTime();
			
			// choose the right state-icon
			if(task.getState() == C_TASK_STATE_ENDED) {
				if(timeout < now ) {
					stateIcon = listdef.getProcessedXmlDataValue("alertok", callingObject);
					style = listdef.getProcessedXmlDataValue("style_alertok", callingObject);
				} else {
					stateIcon = listdef.getProcessedXmlDataValue("ok", callingObject);
					style = listdef.getProcessedXmlDataValue("style_ok", callingObject);
				}
			} else if(task.getPercentage() == 0) {
				if(timeout < now ) {
					stateIcon = listdef.getProcessedXmlDataValue("alert", callingObject);
					style = listdef.getProcessedXmlDataValue("style_alert", callingObject);
				} else {
					stateIcon = listdef.getProcessedXmlDataValue("new", callingObject);
					style = listdef.getProcessedXmlDataValue("style_new", callingObject);
				}
			} else {
				if(timeout < now ) {
					stateIcon = listdef.getProcessedXmlDataValue("alert", callingObject);
					style = listdef.getProcessedXmlDataValue("style_alert", callingObject);
				} else {
					stateIcon = listdef.getProcessedXmlDataValue("activ", callingObject);
					style = listdef.getProcessedXmlDataValue("style_activ", callingObject);
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
			listdef.setXmlData("stateicon", stateIcon);
			listdef.setXmlData("style", style);
			listdef.setXmlData("priority", priority);
			listdef.setXmlData("taskid", task.getId() + "");
			listdef.setXmlData("task", task.getName());
			listdef.setXmlData("foruser", agent);
			listdef.setXmlData("forrole", group);
			listdef.setXmlData("actuator", owner);
			listdef.setXmlData("due", due);
			listdef.setXmlData("from", from);
			listdef.setXmlData("project", projectname);
			
			result.append(listdef.getProcessedXmlDataValue("defaulttasklist", callingObject, parameters));
		}		
		return result.toString();
    }
}

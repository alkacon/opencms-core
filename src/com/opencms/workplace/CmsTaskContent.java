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
 * @version $Revision: 1.3 $ $Date: 2000/02/20 10:14:00 $
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
		
        A_CmsRequestContext reqCont = cms.getRequestContext();
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
		String project = (String)session.getValue(C_SESSION_TASK_PROJECTNAME);
		String filter = (String)session.getValue(C_SESSION_TASK_FILTER);
		Vector retValue;
		if(filter == null) {
			filter = "a1";
		}
		int filterNum = Integer.parseInt(filter, 16);
		String userName = cms.getRequestContext().currentUser().getName();
		String roleName = cms.getRequestContext().currentGroup().getName();
		
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
				retValue = cms.readTasksForRole(project, roleName, C_TASKS_NEW, orderBy, groupBy);
				break;
			case 0xb2:
				retValue = cms.readTasksForRole(project, roleName, C_TASKS_ACTIVE, orderBy, groupBy);
				break;
			case 0xb3:
				retValue = cms.readTasksForRole(project, roleName, C_TASKS_DONE, orderBy, groupBy);
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
}

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;

import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace task head screens.
 * <P>
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 2000/02/19 11:44:59 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsTaskHead extends CmsWorkplaceDefault implements I_CmsConstants {
	
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
		
		HttpSession session = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
        A_CmsRequestContext reqCont = cms.getRequestContext();
		CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		
		// read parameters and set them into the session
		// is the checkbox checked?
		if("OK".equals(parameters.get("ALL"))) {
			session.removeValue(C_SESSION_TASK_PROJECTNAME);
		} else {
			session.putValue(C_SESSION_TASK_PROJECTNAME, reqCont.currentProject().getName());
		}
		
		// Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

    /**
     * User method to get the checked value.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.  
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String with the pics URL.
     * @exception CmsException
     */    
    public Object checked(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
		throws CmsException {
		HttpSession session = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
		if(session.getValue(C_SESSION_TASK_PROJECTNAME) == null) {
			// "all projects" is chosen
			return("checked");
		} else {
			return("");
		}
    }
}

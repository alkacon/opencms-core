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
 * @version $Revision: 1.2 $ $Date: 2000/02/20 10:14:00 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsTaskHead extends CmsWorkplaceDefault implements I_CmsConstants {
	
	/** Constant for filter */
	private static final String C_TASK_FILTER = "task.filter.";

	/** Constant for filter */
	private static final String C_SPACER = "------------------------------------------------";
	
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
		
		// is the listbox chosen?
		System.err.println("getContent" + parameters.get("filter"));
		if((parameters.get("filter") != null) && (parameters.get("filter") != "-")) {
			session.putValue(C_SESSION_TASK_FILTER, parameters.get("filter"));
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

    /**
     * Gets all filters available in the task screen.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * <P>
     * <code>names</code> will contain language specific view descriptions
     * and <code>values</code> will contain the correspondig URL for each
     * of these views after returning from this method.
     * <P>
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current filter view in the vectors.
     * @exception CmsException
     */
    public Integer getFilters(A_CmsObject cms, CmsXmlLanguageFile lang, Vector values, Vector names, Hashtable parameters) 
		throws CmsException {
        
        // Let's see if we have a session
        A_CmsRequestContext reqCont = cms.getRequestContext();
        HttpSession session = ((HttpServletRequest)reqCont.getRequest().getOriginalRequest()).getSession(false);
		String filter = (String)session.getValue(C_SESSION_TASK_FILTER);
		System.err.println("getFilters" + filter);

		int selected = 0;
		
		names.addElement("a1");
		values.addElement(lang.getLanguageValue(C_TASK_FILTER + "a1"));
		if("a1".equals(filter)) {
			selected = 0;
		}
		names.addElement("b1");
		values.addElement(lang.getLanguageValue(C_TASK_FILTER + "b1"));
		if("b1".equals(filter)) {
			selected = 1;
		}
		names.addElement("c1");
		values.addElement(lang.getLanguageValue(C_TASK_FILTER + "c1"));
		if("c1".equals(filter)) {
			selected = 2;
		}

		names.addElement("-");
		values.addElement(C_SPACER);		
		
		names.addElement("a2");
		values.addElement(lang.getLanguageValue(C_TASK_FILTER + "a2"));
		if("a2".equals(filter)) {
			selected = 4;
		}
		names.addElement("b2");
		values.addElement(lang.getLanguageValue(C_TASK_FILTER + "b2"));
		if("b2".equals(filter)) {
			selected = 5;
		}
		names.addElement("c2");
		values.addElement(lang.getLanguageValue(C_TASK_FILTER + "c2"));
		if("c2".equals(filter)) {
			selected = 6;
		}
		
		names.addElement("-");
		values.addElement(C_SPACER);		

		names.addElement("a3");
		values.addElement(lang.getLanguageValue(C_TASK_FILTER + "a3"));
		if("a3".equals(filter)) {
			selected = 8;
		}
		names.addElement("b3");
		values.addElement(lang.getLanguageValue(C_TASK_FILTER + "b3"));
		if("b3".equals(filter)) {
			selected = 9;
		}
		names.addElement("c3");
		values.addElement(lang.getLanguageValue(C_TASK_FILTER + "c3"));
		if("c3".equals(filter)) {
			selected = 10;
		}
		
		names.addElement("-");
		values.addElement(C_SPACER);		

		names.addElement("d1");
		values.addElement(lang.getLanguageValue(C_TASK_FILTER + "d1"));
		if("d1".equals(filter)) {
			selected = 12;
		}
		names.addElement("d2");
		values.addElement(lang.getLanguageValue(C_TASK_FILTER + "d2"));
		if("d2".equals(filter)) {
			selected = 13;
		}
		names.addElement("d3");
		values.addElement(lang.getLanguageValue(C_TASK_FILTER + "d3"));
		if("d3".equals(filter)) {
			selected = 14;
		}
		
		System.err.println(selected);
		System.err.println(new Integer(selected));
		return(new Integer(selected));
    }
}

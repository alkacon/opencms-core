package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;
import java.io.*;

import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace admin project resent.
 * <P>
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 2000/02/09 14:24:21 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsAdminProjectPublish extends CmsWorkplaceDefault implements I_CmsConstants, I_CmsLogChannels {

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
		
		CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)
													getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		
		xmlTemplateDocument.setXmlData("projectname", 
									   (String)parameters.get("projectname"));
		
		if(parameters.get("ok") != null) {
			// publish the project
			try {
				cms.getRequestContext().setCurrentProject(I_CmsConstants.C_PROJECT_ONLINE);
				cms.publishProject((String)parameters.get("projectname"));
				// publish process was successfull
				// redirect to the project overview...
				templateSelector = "done";
			} catch (CmsException exc) {
				// error while publishing...
				if(A_OpenCms.isLogging()) {
					A_OpenCms.log(C_OPENCMS_INFO, exc.getMessage());
				}				
				// get errorpage:
				templateSelector = "error";
			}
		}

		// Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }
}

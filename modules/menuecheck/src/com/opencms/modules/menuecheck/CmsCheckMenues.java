package com.opencms.modules.menuecheck;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import com.opencms.workplace.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * This class shows all combinations for context menues.
 *
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsCheckMenues extends CmsWorkplaceDefault {
	/**
	 * Gets the content of a defined section in a given template file and its subtemplates
	 * with the given parameters.
	 *
	 * @see getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters)
	 * @param cms CmsObject Object for accessing system resources.
	 * @param templateFile Filename of the template file.
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 */
	public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
		if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() && C_DEBUG) {
			A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "getting content of element " + ((elementName==null)?"<root>":elementName));
			A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: " + templateFile);
			A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
		}
		//CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // get the parameters
        String initial = (String)parameters.get("initial");
        String res = (String)parameters.get("res");
        //String state = (String)parameters.get("state");
        int resTypeInt = 0;

        if(initial == null || "".equals(initial)){
            xmlTemplateDocument.setData("onload", "");
            try{
                resTypeInt = Integer.parseInt(res);
            }catch(Exception e){}
        }else{
            // first call
            xmlTemplateDocument.setData("onload", "window.top.body.admin_head.location.href='head.html';");
            resTypeInt = 0;
        }

        xmlTemplateDocument.setData("resint", ""+resTypeInt);
        xmlTemplateDocument.setData("resname", cms.getResourceType(resTypeInt).getResourceTypeName());

		// Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
	}
}









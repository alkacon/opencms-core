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
 * This class constructs the head.
 *
 * @author Hanjo Riege
 * @version 1.0
 */


public class CmsCheckMenuesHead extends CmsWorkplaceDefault {
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
		CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);

        Hashtable restypes = cms.getAllResourceTypes();
        Enumeration enu = restypes.keys();
        StringBuffer output = new StringBuffer();
        while (enu.hasMoreElements()) {
            String key=(String)enu.nextElement();
            I_CmsResourceType  res=(I_CmsResourceType)restypes.get(key);
            xmlTemplateDocument.setData("resname", res.getResourceTypeName());
            xmlTemplateDocument.setData("resvalue", ""+res.getResourceType());
            if(res.getResourceType()==0){
                xmlTemplateDocument.setData("selected", "selected");
            }else{
                xmlTemplateDocument.setData("selected", "");
            }
            output.append(xmlTemplateDocument.getProcessedDataValue("res_entry"));
        }

        xmlTemplateDocument.setData("output", output.toString());

		// Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
	}
}
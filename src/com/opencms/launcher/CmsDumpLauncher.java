package com.opencms.launcher;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;

import org.w3c.dom.*;
import org.xml.sax.*;
        
import java.util.*;      
import javax.servlet.http.*;

/**
 * OpenCms launcher class for starting template classes implementing
 * the I_CmsDumpTemplate interface.
 * This can be used for plain text files or files containing graphics.
 * <P>
 * If no other start template class is given, CmsDumpTemplate will
 * be used to create output.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.5 $ $Date: 2000/01/25 14:01:19 $
 */
public class CmsDumpLauncher extends A_CmsLauncher { 	
        
    /**
 	 * Starts generating the output.
 	 * Calls the canonical root with the appropriate template class.
 	 * 
	 * @param cms A_CmsObject Object for accessing system resources
	 * @param file CmsFile Object with the selected resource to be shown
	 * @param startTemplateClass Name of the template class to start with.
     * @exception CmsException
	 */	
    protected void launch(A_CmsObject cms, CmsFile file, String startTemplateClass) throws CmsException {
        
        byte[] result = null;

        String templateClass = startTemplateClass;
        if(templateClass == null || "".equals(templateClass)) {            
            templateClass = "com.opencms.template.CmsDumpTemplate";
        }
         
        Object tmpl = getTemplateClass(cms, templateClass);
               
        if(!(tmpl instanceof com.opencms.template.I_CmsDumpTemplate)) {
            String errorMessage = "Error in " + file.getAbsolutePath() + ": " + templateClass + " is not a Cms dump template class.";
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_XML_WRONG_TEMPLATE_CLASS);
        }
                
        Hashtable newParameters = new Hashtable();
            
        try {
            result = this.callCanonicalRoot(cms, (com.opencms.template.I_CmsTemplate)tmpl, file, newParameters);
        } catch (CmsException e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "There were errors while building output for template file \"" + file.getAbsolutePath() + "\" and template class \"" + templateClass + "\". See above for details.");
            }
            throw e;
        }
            
        if(result != null) {
            writeBytesToResponse(cms, result);
        }
    }

    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */
    public int getLauncherId() {
	    return C_TYPE_DUMP;
    }
 }

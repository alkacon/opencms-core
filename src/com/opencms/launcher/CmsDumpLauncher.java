package com.opencms.launcher;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;

import org.w3c.dom.*;
import org.xml.sax.*;
        
import java.util.*;      
import javax.servlet.http.*;

public class CmsDumpLauncher extends A_CmsLauncher { 	
        
    protected void launch(A_CmsObject cms, CmsFile file) throws CmsException {
        
        byte[] result = null;

        String templateClass = "com.opencms.template.CmsDumpTemplate";
        Object tmpl = getTemplateClass(cms, templateClass);
               
        if(!(tmpl instanceof com.opencms.template.I_CmsDumpTemplate)) {
            System.err.println(templateClass + " is not a Cms dump template class. Sorry.");
            System.err.println("removing cache");
            throw new CmsException("Error in " + file.getAbsolutePath() + ": " + templateClass + " is not a XML template class. Sorry.");
        }
        
        
        Hashtable newParameters = new Hashtable();
            
        try {
            result = this.callCanonicalRoot(cms, (com.opencms.template.I_CmsTemplate)tmpl, file, newParameters);
        } catch (CmsException e) {
            System.err.println("Error while creating document");
            throw e;
        }
            
        if(result != null) {
            writeBytesToResponse(cms, result, "text/plain");
        }
    }

    public int getLauncherId() {
	    return C_TYPE_DUMP;
    }
 }

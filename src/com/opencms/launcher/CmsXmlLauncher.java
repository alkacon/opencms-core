package com.opencms.launcher;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;
import org.w3c.dom.*;
import org.xml.sax.*;
        
import java.util.*;      
import javax.servlet.http.*;

public class CmsXmlLauncher extends A_CmsLauncher implements I_CmsLogChannels { 	
        
    protected void launch(A_CmsObject cms, CmsFile file) throws CmsException {
        // get the CmsRequest 
        I_CmsRequest req = cms.getRequestContext().getRequest();
        
        // Check the clearcache parameter for the xml filecache        
        String clearcache = req.getParameter("_clearcache");
        if(clearcache != null) {
            if("all".equals(clearcache) || "file".equals(clearcache)) {
                A_CmsXmlContent.clearFileCache();                
            }        
        }
        
        byte[] result = null;
        
        CmsXmlControlFile doc = null;
        try {
            //doc.init(cms, file);
            doc = new CmsXmlControlFile(cms, file);
        } catch(Exception e) {
            // there was an error while parsing the document
            handleException(cms, e, "There was an error while parsing XML file " + file.getAbsolutePath());
            return;
        }
        
        String templateClass = doc.getTemplateClass();
        String templateName = doc.getMasterTemplate();
        
        CmsFile masterTemplate = loadMasterTemplateFile(cms, templateName, doc);
                
        Object tmpl = getTemplateClass(cms, templateClass);
        if(!(tmpl instanceof I_CmsXmlTemplate)) {
            System.err.println(templateClass + " is not a XML template class. Sorry.");
            System.err.println("removing cache");
            doc.clearFileCache(doc);
            throw new CmsException("Error in " + file.getAbsolutePath() + ": " + templateClass + " is not a XML template class. Sorry.");
        }
        
        // Now look for parameters in the body file 
        
        Hashtable newParameters = new Hashtable();
        
        Enumeration elementDefinitions = doc.getElementDefinitions();
        while(elementDefinitions.hasMoreElements()) {
            String elementName = (String)elementDefinitions.nextElement();
            
            if(doc.isElementClassDefined(elementName)) {
                newParameters.put(elementName + "._CLASS_", doc.getElementClass(elementName));                
            }

            if(doc.isElementTemplateDefined(elementName)) {
                newParameters.put(elementName + "._TEMPLATE_", doc.getElementTemplate(elementName));                
            }
            
            Enumeration parameters = doc.getParameterNames(elementName);
            while(parameters.hasMoreElements()) {
                String paramName = (String)parameters.nextElement();
                String paramValue = doc.getParameter(elementName, paramName);
                newParameters.put(elementName + "." + paramName, paramValue);
            }     
        }           
        
                
        // Now check URL parameters      
        String datafor = req.getParameter("datafor");
        if(datafor == null) {
            datafor = "";
        } else if(! "".equals(datafor)) {
            datafor = datafor + ".";
        }
        
        Enumeration urlParameterNames = req.getParameterNames();
        while(urlParameterNames.hasMoreElements()) {
            String pname = (String)urlParameterNames.nextElement();
            if((! "datafor".equals(pname)) && (! "_clearcache".equals(pname))) {
                newParameters.put(datafor + pname, req.getParameter(pname));
            }
        }
     
        try {
            result = callCanonicalRoot(cms, (I_CmsTemplate)tmpl, masterTemplate, newParameters);
        } catch (CmsException e) {
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlLauncher] There were exceptions while generating output for " + file.getAbsolutePath());
                A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlLauncher] Clearing template file cache for this file.");
            }
            doc.clearFileCache(doc);
            throw e;
        }
            
        if(result != null) {
            writeBytesToResponse(cms, result, "text/plain");
        }
    }

    private CmsFile loadMasterTemplateFile(A_CmsObject cms, String templateName, CmsXmlControlFile doc) throws CmsException {
        CmsFile masterTemplate = null;
        try {
            masterTemplate = cms.readFile(templateName);
        } catch(Exception e) {
            handleException(cms, e, "Cannot load master template " + templateName + ". ");
            A_CmsXmlContent.clearFileCache(doc);
        }
        return masterTemplate;
    }
        
    
    
    
    
    public int getLauncherId() {
	    return C_TYPE_XML;
    }
    
}

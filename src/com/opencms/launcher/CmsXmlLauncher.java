package com.opencms.launcher;

import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;
import org.w3c.dom.*;
import org.xml.sax.*;
        
import java.util.*;      
import javax.servlet.http.*;

/**
 * OpenCms launcher class for XML templates.
 * This can be used generating output for XML body files using XML template and
 * subtemplate technology.
 * <P>
 * The selected body should define a start template class using <BR> <CODE>
 * &lt;PAGE&gt;<BR>
 * &nbsp;&nbsp;&lt;CLASS&gt;...&lt;/CLASS&gt;<BR>
 * &lt;/PAGE&gt;</CODE><P>
 * 
 * If no start template is defined, the class given by the parameters
 * will be used.
 * <P>
 * If even this is not defined, CmsXmlTemplate will
 * be used to create output.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.3 $ $Date: 2000/01/14 16:17:11 $
 */
public class CmsXmlLauncher extends A_CmsLauncher implements I_CmsLogChannels { 	
        
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
        if(templateClass == null || "".equals(templateClass)) {
            templateClass = startTemplateClass;
        }
        if(templateClass == null || "".equals(templateClass)) {
            templateClass = "com.opencms.template.CmsXmlTemplate";
        }
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
            writeBytesToResponse(cms, result);
        }
    }

    /**
     * Internal utility method for checking and loading a given template file.
     * @param cms A_CmsObject for accessing system resources.
     * @param templateName Name of the requestet template file.
     * @param doc CmsXmlControlFile object containig the parsed body file.
     * @return CmsFile object of the requested template file.
     * @exception CmsException
     */
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
                    
    /**
     * Gets the ID that indicates the type of the launcher.
     * @return launcher ID
     */    
    public int getLauncherId() {
	    return C_TYPE_XML;
    }    
}

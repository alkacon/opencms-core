package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;


public class CmsEditor extends CmsXmlTemplate {
        
    /**
     * Gets the key that should be used to cache the results of
     * <EM>this</EM> template class. For simple template classes, e.g.
     * classes only dumping file contents and not using parameters,
     * the name of the template file may be adequate.
     * Other classes have to return a more complex key.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return key that can be used for caching
     */
    public Object getKey(A_CmsObject cms, String templateFile, Hashtable parameters) {
        Vector v = new Vector();
        A_CmsRequestContext reqContext = cms.getRequestContext();
        
        v.addElement(templateFile);
        v.addElement(parameters);
        return v;
    }
    
    /**
     * Indicates if the results of this class are cacheable.
     * <P>
     * Complex classes that are able top include other subtemplates
     * have to check the cacheability of their subclasses here!
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, Hashtable parameters) {
        return true;
    }

    
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsEditor] getting content of element " + elementName);
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsEditor] template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsEditor] selected template section is: " + ((templateSelector==null)?"<root>":templateSelector));
        }

        String result = null;        

        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile();       
        String fullFileName = CmsXmlWpTemplateFile.lookupAbsoluteFilename(cms, templateFile, xmlTemplateDocument);
        CmsFile file = cms.readFile(fullFileName);
        xmlTemplateDocument.init(cms, file);       
                
        // Try to process the template file
        try {
            result = xmlTemplateDocument.getProcessedTemplateContent(this, parameters);
        } catch(Throwable e) {
           // There were errors while generating output for this template.
            // Clear HTML cache and then throw exception again
            xmlTemplateDocument.clearFileCache(xmlTemplateDocument);
            if(isCacheable(cms, templateFile, parameters)) {
                m_cache.clearCache(getKey(cms, templateFile, parameters));
            }
            if(e instanceof CmsException) {
                throw (CmsException)e;
            } else {
                // under normal cirumstances, this should not happen.
                // any exception should be caught earlier and replaced by 
                // corresponding CmsExceptions.
                String errorMessage = "Exception while getting content for (sub)template " + elementName + ". " + e;                                       
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlTemplate] " + errorMessage);
                }
                throw new CmsException(errorMessage);                
            }
        }        
        return result.getBytes();
    }
        
    /**
     * Indicates if a previous cached result should be reloaded.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return <EM>true</EM> if reload is neccesary, <EM>false</EM> otherwise.
     */    
    public boolean shouldReload(A_CmsObject cms, String templateFile, Hashtable parameters) {
        return false;
    }

    public Object setText(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        Hashtable parameters = (Hashtable)userObj;
        String filename = (String)parameters.get("file");
        
        if(filename==null || "".equals(filename)) {
            String errorMessage = "[CmsButton] No file requested.";
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_BAD_NAME);
        }
        
        CmsFile editFile = cms.readFile(filename);                
        String content = new String(editFile.getContents());
        Encoder enc = new Encoder();
        return enc.escape(content);
    }
}

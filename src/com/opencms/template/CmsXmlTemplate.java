package com.opencms.template;

import java.util.*;
import java.io.*;
import com.opencms.launcher.*;
import com.opencms.file.*;
import com.opencms.core.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class CmsXmlTemplate implements I_CmsXmlTemplate, I_CmsLogChannels {
    

    private static I_CmsTemplateCache m_cache = null;
    
    protected Hashtable subtemplatesCacheable = null;
    
    private int counter = 0;
    
    public CmsXmlTemplate() {
    }
    
    public final void setTemplateCache(I_CmsTemplateCache c) {
        m_cache = c;
    }
    
    public final boolean isTemplateCacheSet() {
        return m_cache != null;
    }
        
    public Object getKey(A_CmsObject cms, String templateFile, Hashtable parameters) {
        Vector v = new Vector();
        A_CmsRequestContext reqContext = cms.getRequestContext();
        
        v.addElement(reqContext.getUri());
        v.addElement(templateFile);
        v.addElement(parameters);
        return v;
    }
        
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters) throws CmsException {
        return getContent(cms, templateFile, elementName, parameters, null);
    }
    
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] getting content of element " + elementName);
        A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] template file is: " + templateFile);
        A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] selected template section is: " + templateSelector);
        String s = null;

        CmsXmlTemplateFile xmlTemplateDocument = new CmsXmlTemplateFile();        
        xmlTemplateDocument.init(cms, templateFile);

        String templateDatablockName = xmlTemplateDocument.getTemplateDatablockName(templateSelector);
        try {
            Element eee = xmlTemplateDocument.getProcessedData(templateDatablockName, this, parameters);
            s = xmlTemplateDocument.getTagValue(eee);
        } catch(Throwable e) {
            // Only clear HTML cache and then throw exception again
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
        return s.getBytes();
    }
    
    public Object templateElement(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
        
        //  Key for the cache
        Object t2Key = null;
        
        // First create a copy of the parameter hashtable
        Hashtable parameterHashtable = (Hashtable)((Hashtable)userObject).clone();
                
        String templateClass = getTemplateClassName(tagcontent, doc, parameterHashtable);
        String templateFilename = getTemplateFileName(tagcontent, doc, parameterHashtable);
                        
        byte[] result = null;

        // if the chaching key only depends on the template filename
        // it ist possible that we can find a previously cached
        // return Object in our cache. Check this first.
        
        /*if(m_cache.has(templateFilename)) {
            result = m_cache.get(templateFilename);
            if(result != null) {
                return result;
            }
        } */              
        
        // no cached object was found.
        // the result could still reside in cache, but in this case it
        // would have a more complex key. 
        // we have to get the class and parameters
        // first, before we know which key to use.
        
        Object tmpl = null;
        try {
            tmpl = CmsTemplateClassManager.getClassInstance(cms, templateClass);
        } catch(Exception e) {
            // There was an error
            // First remove the template file from the file cache
            doc.clearFileCache(doc);     
            A_CmsRequestContext reqContext = cms.getRequestContext();
            if(cms.anonymousUser().equals(reqContext.currentUser())) {
                // the current user is the anonymous user
                String ss = "Error while generating output!";
                return ss;
            } else {
                // the current user is a system user.
                // so we can throw an exception
                // our parent class so can act and delete its caches.
                if(e instanceof ClassNotFoundException) {
                    System.err.println("Class " + templateClass + " could not be loaded!");
                    throw new CmsException("Could not load template class " + templateClass);
                } else {
                    System.err.println("Class " + templateClass + " could not be instantiated!");
                    throw new CmsException("Could not instantiate class " + templateClass + ". Original Exception: " + e);
                }
            }
        }
        
        if(!(tmpl instanceof I_CmsTemplate)) {
            System.err.println(templateClass + " is not a OpenCms template class. Sorry.");
            System.err.println("removing cache");
            throw new CmsException("Error in " + templateClass + " is not a XML template class. Sorry.");
        }
            
        I_CmsTemplate t2 = (I_CmsTemplate) tmpl;                
            
        // Template class is now loaded
        // Next try to read the parameters
        Element elementDefinitionTag = doc.getData("ELEMENTDEF." + tagcontent);
        NodeList parameterTags = elementDefinitionTag.getElementsByTagName("PARAMETER");
        int numParameters = parameterTags.getLength();
               
        for(int i=0; i<numParameters; i++) {
            Element param = (Element)parameterTags.item(i);
            String paramName = param.getAttribute("name");
            String paramValue = doc.getTagValue(param);
            if(! parameterHashtable.containsKey(paramName)) {
                parameterHashtable.put(tagcontent + "." + paramName, paramValue);
            }
        }

        t2Key = t2.getKey(cms, templateFilename, parameterHashtable);            
        if(t2.isCacheable(cms, templateFilename, parameterHashtable) && m_cache.has(t2Key)
                && (! t2.shouldReload(cms, templateFilename, parameterHashtable))) {
            result = m_cache.get(t2Key);
        }        
        
        // all parameters are now parsed
        // let's call the subtemplate
        if(result == null) {
            try {
                result = t2.getContent(cms, templateFilename, tagcontent, parameterHashtable);
            } catch (CmsException e) {
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, "[CmsXmlTemplate] Could not generate output for element " 
                        + tagcontent + " in template file " + templateFilename + ". ");
                }
                A_CmsRequestContext reqContext = cms.getRequestContext();
                if(cms.anonymousUser().equals(reqContext.currentUser())) {
                    result = "ERROR!".getBytes();
                } else {
                    throw e;
                }
            }  
        }     
        
        if(t2.isCacheable(cms, templateFilename, parameterHashtable)) {
            m_cache.put(t2Key, result);
        }
        return result;
    }

    
    public String parameters(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) {
        Hashtable param = (Hashtable)userObject;
        Enumeration keys = param.keys();
        String s = "";
        while(keys.hasMoreElements()) {
            s = s + keys.nextElement() + "<BR>";
        }
        s = s + "<B>" + tagcontent + "</B><BR>";
        return s;
    }    
    
    
    public Integer counter(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
        this.counter++;
        return new Integer(counter);
    }    
        
    public boolean isCacheable(A_CmsObject cms, String templateFile, Hashtable parameters) {
        boolean cacheable = ((m_cache != null) && this.subtemplatesCacheable(cms, templateFile, parameters));
        System.err.print("template class " + getClass().getName() + " with file " + templateFile + " is");
        if(cacheable) {
            System.err.println(" cacheable.");
        } else {
            System.err.println(" not cacheable.");
        }
        return cacheable;
    }

    
    public boolean shouldReload(A_CmsObject cms, String templateFile, Hashtable parameters) {
        return false;
    }
    
    public boolean subtemplatesCacheable(A_CmsObject cms, String templateFile, Hashtable parameters) { 
        boolean cacheable = true;
        CmsXmlTemplateFile doc = null;
        Enumeration subtemplates = null;
        try {
            doc = new CmsXmlTemplateFile();
            doc.init(cms, templateFile);               
            subtemplates = doc.getAllSubElements();
        } catch(Exception e) {
            System.err.println(e);
            return false;
        }

        while(subtemplates.hasMoreElements()) {
            String elName = (String)subtemplates.nextElement();

            String className = getTemplateClassName(elName, doc, parameters);
            String templateName = getTemplateFileName(elName, doc, parameters);
                                              
            try {
                I_CmsTemplate templClass = (I_CmsTemplate)CmsTemplateClassManager.getClassInstance(cms, className);
                cacheable = cacheable && templClass.isCacheable(cms, templateName, parameters);
            } catch(Exception e) {
                System.err.println("E: " + e);
            }                
        }                
        return cacheable;
    }    
    
        // Find the corresponding template class to be loaded.
        // this should be defined in the template file of the parent
        // template and can be overwritten in the body file.
    private String getTemplateClassName(String elementName, A_CmsXmlContent doc, Hashtable parameters) {
        if(parameters.containsKey(elementName + "._CLASS_")) {
            return (String)parameters.get(elementName + "._CLASS_");
        } else {
            return doc.getDataValue("ELEMENTDEF." + elementName + ".CLASS");
        }
    }

        // Find the corresponding template file to be loaded by the template class.
        // this should be defined in the template file of the parent
        // template and can be overwritten in the body file.
    private String getTemplateFileName(String elementName, A_CmsXmlContent doc, Hashtable parameters) {
        if(parameters.containsKey(elementName + "._TEMPLATE_")) {
            return (String)parameters.get(elementName + "._TEMPLATE_");
        } else {
            return doc.getDataValue("ELEMENTDEF." + elementName + ".TEMPLATE");
        }
    }               
}

package com.opencms.template;

import java.util.*;
import java.io.*;
import com.opencms.launcher.*;
import com.opencms.file.*;
import com.opencms.core.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Template class for displaying the processed contents of hierachical XML template files
 * that can include other subtemplates.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/01/14 15:45:21 $
 */
public class CmsXmlTemplate implements I_CmsXmlTemplate, I_CmsLogChannels {
    
    /**
     * Template cache for storing cacheable results of the subtemplates.
     */
    private static I_CmsTemplateCache m_cache = null;
    
    /**
     * For debugging purposes only.
     * Counts the number of re-uses od the instance of this class.
     */
    private int counter = 0;
        
    public CmsXmlTemplate() {
    }
    
    /**
     * Set the instance of template cache that should be used to store 
     * cacheable results of the subtemplates.
     * If the template cache is not set, caching will be disabled.
     * @param c Template cache to be used.
     */
    public final void setTemplateCache(I_CmsTemplateCache c) {
        m_cache = c;
    }
    
    /**
     * Tests, if the template cache is setted.
     * @return <code>true</code> if setted, <code>false</code> otherwise.
     */
    public final boolean isTemplateCacheSet() {
        return m_cache != null;
    }
        
    /**
     * Gets the key that should be used to cache the results of
     * <EM>this</EM> template class. 
     * <P>
     * Since our results may depend on the used template file, 
     * the parameters and the requested body document, we must
     * build a complex key using this three arguments.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return key that can be used for caching
     */
    public Object getKey(A_CmsObject cms, String templateFile, Hashtable parameters) {
        Vector v = new Vector();
        A_CmsRequestContext reqContext = cms.getRequestContext();
        
        v.addElement(reqContext.getUri());
        v.addElement(templateFile);
        v.addElement(parameters);
        return v;
    }
        
    /**
     * Gets the content of a given template file and its subtemplates
     * with the given parameters. The default section in the template file
     * will be used.
     * <P>
     * Parameters are stored in a hashtable and can derive from
     * <UL>
     * <LI>Template file of the parent template</LI>
     * <LI>Body file clicked by the user</LI>
     * <LI>URL parameters</LI>
     * </UL>
     * Paramter names must be in "elementName.parameterName" format.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template
     * @param parameters Hashtable with all template class parameters.
     * @return Content of the template and all subtemplates.
     * @exception CmsException 
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters) throws CmsException {
        return getContent(cms, templateFile, elementName, parameters, null);
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
     * @param templateSelector section that should be processed.
     * @return Content of the template and all subtemplates.
     * @exception CmsException 
     */
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
    
    /**
     * Handles any occurence of an "ELEMENT" tag.
     * <P>
     * Every XML template class should use CmsXmlTemplateFile as
     * the interface to the XML file. Since CmsXmlTemplateFile is
     * an extension of A_CmsXmlContent by the additional tag
     * "ELEMENT" this user method ist mandatory.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object templateElement(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
        
        //  Key for the cache
        Object t2Key = null;
        
        // First create a copy of the parameter hashtable
        Hashtable parameterHashtable = (Hashtable)((Hashtable)userObject).clone();
                
        String templateClass = getTemplateClassName(tagcontent, doc, parameterHashtable);
        String templateFilename = getTemplateFileName(tagcontent, doc, parameterHashtable);
                        
        byte[] result = null;

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
    
    /**
     * For debugging purposes only.
     * Prints out all parameters.
     * <P>
     * May be called from the template file using
     * <code>&lt;METHOD name="parameters"&gt;</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return Debugging information about all parameters.
     */
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
        
    /**
     * For debugging purposes only.
     * Increments the class variable <code>counter</code> and
     * prints out its new value..
     * <P>
     * May be called from the template file using
     * <code>&lt;METHOD name="counter"&gt;</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return Actual value of <code>counter</code>.
     */
    public Integer counter(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
        counter++;
        return new Integer(counter);
    }    
        
    /**
     * Indicates if the results of this class are cacheable.
     * <P>
     * Checks if the templateCache is set and if all subtemplates
     * are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, Hashtable parameters) {
        boolean cacheable = ((m_cache != null) && subtemplatesCacheable(cms, templateFile, parameters));
        System.err.print("template class " + getClass().getName() + " with file " + templateFile + " is");
        if(cacheable) {
            System.err.println(" cacheable.");
        } else {
            System.err.println(" not cacheable.");
        }
        return cacheable;
    }

    
    /**
     * Indicates if a previous cached result should be reloaded.
     * <P>
     * <em>not implemented.</em> Returns always <code>false</code>.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return <code>false</code> 
     */    
    public boolean shouldReload(A_CmsObject cms, String templateFile, Hashtable parameters) {
        return false;
    }
    
    /**
     * Checks if all subtemplates are cacheable.
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return <code>true</code> if all subtemplates are cacheable, <code>false</code> otherwise.
     */
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
    
    /**
     * Find the corresponding template file to be loaded by the template class.
     * this should be defined in the template file of the parent
     * template and can be overwritten in the body file.
     * 
     * @param elementName Element name of this template in our parent template.
     * @param doc A_CmsXmlContent object of our template file including a subtemplate.
     * @param parameters Hashtable with all template class parameters.
     * @return Name of the template file that should be included.
     */    
    private String getTemplateFileName(String elementName, A_CmsXmlContent doc, Hashtable parameters) {
        if(parameters.containsKey(elementName + "._TEMPLATE_")) {
            return (String)parameters.get(elementName + "._TEMPLATE_");
        } else {
            return doc.getDataValue("ELEMENTDEF." + elementName + ".TEMPLATE");
        }
    }               

    /**
     * Find the corresponding template class to be loaded.
     * this should be defined in the template file of the parent
     * template and can be overwritten in the body file.
     * 
     * @param elementName Element name of this template in our parent template.
     * @param doc A_CmsXmlContent object of our template file including a subtemplate.
     * @param parameters Hashtable with all template class parameters.
     * @return Name of the class that should generate the output for the included template file.
     */    
    private String getTemplateClassName(String elementName, A_CmsXmlContent doc, Hashtable parameters) {
        if(parameters.containsKey(elementName + "._CLASS_")) {
            return (String)parameters.get(elementName + "._CLASS_");
        } else {
            return doc.getDataValue("ELEMENTDEF." + elementName + ".CLASS");
        }
    }
}

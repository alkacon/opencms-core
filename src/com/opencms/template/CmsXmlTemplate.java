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
 * @version $Revision: 1.9 $ $Date: 2000/02/14 14:10:23 $
 */
public class CmsXmlTemplate implements I_CmsXmlTemplate, I_CmsLogChannels {
    
    /** Boolean for additional debug output control */
    protected final static boolean C_DEBUG = true;
    
    /**
     * Error string to be inserted for corrupt subtemplates for guest user requests.
     */
    private final static String C_ERRORTEXT = "ERROR!";
    
    /**
     * Template cache for storing cacheable results of the subtemplates.
     */
    protected static com.opencms.launcher.I_CmsTemplateCache m_cache = null;
    
    /**
     * For debugging purposes only.
     * Counts the number of re-uses od the instance of this class.
     */
    private int counter = 0;

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
     * @param templateSelector template section that should be processed.
     * @return key that can be used for caching
     */
    public Object getKey(A_CmsObject cms, String templateFile, Hashtable parameters, String templateSelector) {
        Vector v = new Vector();
        A_CmsRequestContext reqContext = cms.getRequestContext();
        
        v.addElement(reqContext.getUri());
        v.addElement(templateFile);
        v.addElement(parameters);
        v.addElement(templateSelector);
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
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] getting content of element " + ((elementName==null)?"<root>":elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsXmlTemplate] selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }

        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }            
    
    /**
     * Starts the processing of the given template file by calling the
     * <code>getProcessedTemplateContent()</code> method of the content defintition
     * of the corresponding content type.
     * <P>
     * Any exceptions thrown while processing the template will be caught,
     * printed and and thrown again.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param xmlTemplateDocument XML parsed document of the content type "XML template file" or
     * any derived content type.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return Content of the template and all subtemplates.
     * @exception CmsException 
     */
    protected byte[] startProcessing(A_CmsObject cms, CmsXmlTemplateFile xmlTemplateDocument, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        String result = null;
        // Try to process the template file
        try {
            result = xmlTemplateDocument.getProcessedTemplateContent(this, parameters, templateSelector);
        } catch(Throwable e) {
            // There were errors while generating output for this template.
            // Clear HTML cache and then throw exception again
            xmlTemplateDocument.clearFileCache(xmlTemplateDocument);
            if(isCacheable(cms, xmlTemplateDocument.getAbsoluteFilename(), elementName, parameters, templateSelector)) {
                m_cache.clearCache(getKey(cms, xmlTemplateDocument.getAbsoluteFilename(), parameters, templateSelector));
            }
            if(e instanceof CmsException) {
                throw (CmsException)e;
            } else {
                // under normal cirumstances, this should not happen.
                // any exception should be caught earlier and replaced by 
                // corresponding CmsExceptions.
                String errorMessage = "Exception while getting content for (sub)template " + elementName + ". " + e;                                       
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
                }
                throw new CmsException(errorMessage);                
            }
        }        
        return result.getBytes();
    }

    /**
     * Reads in the template file and starts the XML parser for the expected
     * content type.
     * <P>
     * Every extending class using not CmsXmlTemplateFile as content type,
     * but any derived type should override this method.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public CmsXmlTemplateFile getOwnTemplateFile(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        CmsXmlTemplateFile xmlTemplateDocument = new CmsXmlTemplateFile(cms, templateFile);                       
        return xmlTemplateDocument;
    }                
    
    /**
     * Handles any occurence of an <code>&lt;ELEMENT&gt;</code> tag.
     * <P>
     * Every XML template class should use CmsXmlTemplateFile as
     * the interface to the XML file. Since CmsXmlTemplateFile is
     * an extension of A_CmsXmlContent by the additional tag
     * <code>&lt;ELEMENT&gt;</code> this user method ist mandatory.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object templateElement(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
        
        // Our own template file that wants to include a subelement
        CmsXmlTemplateFile templateFile = (CmsXmlTemplateFile)doc;

        // Indicates, if this is a request of a guest user. Needed for error outputs.
        boolean isAnonymousUser = cms.anonymousUser().equals(cms.getRequestContext().currentUser());                
        
        // First create a copy of the parameter hashtable
        Hashtable parameterHashtable = (Hashtable)((Hashtable)userObject).clone();
                
        // Name of the template class that should be used to handle the subtemplate
        String templateClass = getTemplateClassName(tagcontent, templateFile, parameterHashtable);

        // Name of the subtemplate file.
        String templateFilename = getTemplateFileName(tagcontent, templateFile, parameterHashtable);                        

        // Name of the subtemplate template selector
        String templateSelector = getTemplateSelector(tagcontent, templateFile, parameterHashtable);
        
        // Results returned by the subtemplate class
        byte[] result = null;

        // Temporary object for loading the subtemplate class
        Object loadedObject = null;

        // subtemplate class to be used for the include
        I_CmsTemplate subTemplate = null;

        // Key for the cache
        Object subTemplateKey = null;
        
        
        // try to load the subtemplate class
        try {
            loadedObject = CmsTemplateClassManager.getClassInstance(cms, templateClass);
        } catch(CmsException e) {
            // There was an error. First remove the template file from the file cache
            templateFile.clearFileCache(templateFile);     

            if(isAnonymousUser) {
                // The current user is the anonymous user
                return C_ERRORTEXT;
            } else {
                // The current user is a system user, so we throw the exception again.
                throw e;
            }
        }
        
        // Check if the loaded object is really an instance of an OpenCms template class
        if(! (loadedObject instanceof I_CmsTemplate)) {
            String errorMessage = "Class " + templateClass + " is no OpenCms template class.";
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlTemplate] " + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_XML_NO_TEMPLATE_CLASS);
        }        
        subTemplate = (I_CmsTemplate)loadedObject;        

        // Template class is now loaded. Next try to read the parameters        
        Enumeration parameterTags = null;
        try {
            parameterTags = templateFile.getParameterNames(tagcontent);
        } catch(CmsException e) {
            // ignore
        }
        if(parameterTags != null) {
            while(parameterTags.hasMoreElements()) {
                String paramName = (String)parameterTags.nextElement();
                String paramValue = templateFile.getParameter(tagcontent, paramName);
                if(! parameterHashtable.containsKey(paramName)) {
                    parameterHashtable.put(tagcontent + "." + paramName, paramValue);
                }
            }      
        }      
                        
        // all parameters are now parsed. let's call the subtemplate
        if(result == null) {
            try {
                result = subTemplate.getContent(cms, templateFilename, tagcontent, parameterHashtable, templateSelector);
            } catch (CmsException e) {
                // Oh, oh..
                // There were errors while getting the content of the subtemplate
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsXmlTemplate] Could not generate output for element " 
                        + tagcontent + " in template file " + templateFilename + ". ");
                }
                // The anonymous user gets an error String instead of an exception
                if(isAnonymousUser) {
                    return C_ERRORTEXT;
                } else {
                    throw e;
                }
            }  
        }     
        
        // Store the results in the template cache, if cacheable
        if(subTemplate.isCacheable(cms, templateFilename, tagcontent, parameterHashtable, null)) {
            subTemplateKey = subTemplate.getKey(cms, templateFilename, parameterHashtable, null);            
            if(subTemplate.isCacheable(cms, templateFilename, tagcontent, parameterHashtable, null) && m_cache.has(subTemplateKey)
                    && (! subTemplate.shouldReload(cms, templateFilename, tagcontent, parameterHashtable, null))) {
                result = m_cache.get(subTemplateKey);
            }        
            m_cache.put(subTemplateKey, result);
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
            String key = (String)keys.nextElement();
            s = s + "<B>" + key + "</B>: " + param.get(key) + "<BR>";
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
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        boolean cacheable = ((m_cache != null) && subtemplatesCacheable(cms, templateFile, elementName, parameters, templateSelector));
        if(C_DEBUG && A_OpenCms.isLogging()) {
            String errorMessage = getClassName() + "Template class " + getClass().getName() + " with file " + templateFile + " is ";
            if(cacheable) {
                errorMessage = errorMessage + "cacheable.";
            } else {
                errorMessage = errorMessage + "not cacheable.";
            }
            A_OpenCms.log(C_OPENCMS_DEBUG, errorMessage);
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
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <code>false</code> 
     */    
    public boolean shouldReload(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
    
    /**
     * Checks if all subtemplates are cacheable.
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <code>true</code> if all subtemplates are cacheable, <code>false</code> otherwise.
     */
    public boolean subtemplatesCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) { 
        boolean cacheable = true;
        CmsXmlTemplateFile doc = null;
        Enumeration subtemplates = null;
        try {
            doc = this.getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
            doc.init(cms, templateFile);               
            subtemplates = doc.getAllSubElements();
        } catch(Exception e) {
            System.err.println(e);
            return false;
        }

        while(subtemplates.hasMoreElements()) {
            String elName = (String)subtemplates.nextElement();
            
            String className = null;
            String templateName = null;

            try {
                className = getTemplateClassName(elName, doc, parameters);
                templateName = getTemplateFileName(elName, doc, parameters);
            } catch(CmsException e) {
                // There was an error while reading the class name or template name 
                // from the subtemplate.
                // So we cannot determine the cacheability.
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, getClassName() + "Could not determine cacheability of subelement " + elName + " in template file " 
                            + doc.getFilename() + ". There were missing datablocks.");
                }
                return false;
            }
            try {
                I_CmsTemplate templClass = (I_CmsTemplate)CmsTemplateClassManager.getClassInstance(cms, className);
                cacheable = cacheable && templClass.isCacheable(cms, templateName, elName, parameters, null);
            } catch(Exception e) {
                System.err.println("E: " + e);
            }                
        }                
        return cacheable;
    }    

    /**
     * Help method to print nice classnames in error messages
     * @return class name in [ClassName] format
     */
    protected String getClassName() {
        String name = getClass().getName();
        return "[" + name.substring(name.lastIndexOf(".") + 1) + "] ";
    }        

    /**
     * Help method that handles any occuring error by writing
     * an error message to the OpenCms logfile and throwing a 
     * CmsException of the type "unknown".
     * @param errorMessage String with the error message to be printed.
     * @exception CmsException
     */
    protected void throwException(String errorMessage) throws CmsException {
        throwException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
    }

    /**
     * Help method that handles any occuring error by writing
     * an error message to the OpenCms logfile and re-throwing a 
     * caught exception.
     * @param errorMessage String with the error message to be printed.
     * @param e Exception to be re-thrown.
     * @exception CmsException
     */
    protected void throwException(String errorMessage, Exception e) throws CmsException {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + "Exception: " + e);
        }        
        if(e instanceof CmsException) {
            throw (CmsException)e;
        } else {
            throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
        }
    }
        
    /**
     * Help method that handles any occuring error by writing
     * an error message to the OpenCms logfile and throwing a 
     * CmsException of the given type.
     * @param errorMessage String with the error message to be printed.
     * @param type Type of the exception to be thrown.
     * @exception CmsException
     */
    protected void throwException(String errorMessage, int type) throws CmsException {
        if(A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
        }        
        throw new CmsException(errorMessage, type);
    }              
        
    /**
     * Find the corresponding template file to be loaded by the template class.
     * this should be defined in the template file of the parent
     * template and can be overwritten in the body file.
     * 
     * @param elementName Element name of this template in our parent template.
     * @param doc CmsXmlTemplateFile object of our template file including a subtemplate.
     * @param parameters Hashtable with all template class parameters.
     * @return Name of the template file that should be included.
     */    
    protected String getTemplateFileName(String elementName, CmsXmlTemplateFile doc, Hashtable parameters) throws CmsException {
        if(parameters.containsKey(elementName + "._TEMPLATE_")) {
            return (String)parameters.get(elementName + "._TEMPLATE_");
        } else {
            return doc.getSubtemplateFilename(elementName);
        }
    }               

    /**
     * Find the corresponding template class to be loaded.
     * this should be defined in the template file of the parent
     * template and can be overwritten in the body file.
     * 
     * @param elementName Element name of this template in our parent template.
     * @param doc CmsXmlTemplateFile object of our template file including a subtemplate.
     * @param parameters Hashtable with all template class parameters.
     * @return Name of the class that should generate the output for the included template file.
     */    
    protected String getTemplateClassName(String elementName, CmsXmlTemplateFile doc, Hashtable parameters) throws CmsException {
        if(parameters.containsKey(elementName + "._CLASS_")) {
            return (String)parameters.get(elementName + "._CLASS_");
        } else {
            return doc.getSubtemplateClass(elementName);
        }
    }

    /**
     * Find the corresponding template selector to be activated.
     * This may be defined in the template file of the parent
     * template and can be overwritten in the body file.
     * 
     * @param elementName Element name of this template in our parent template.
     * @param doc CmsXmlTemplateFile object of our template file including a subtemplate.
     * @param parameters Hashtable with all template class parameters.
     * @return Name of the class that should generate the output for the included template file.
     */    
    protected String getTemplateSelector(String elementName, CmsXmlTemplateFile doc, Hashtable parameters) throws CmsException {
        if(parameters.containsKey(elementName + "._TEMPLATESELECTOR_")) {
            return (String)parameters.get(elementName + "._TEMPLATESELECTOR_");
        } else if (doc.hasSubtemplateSelector(elementName)) {
            return doc.getSubtemplateSelector(elementName);
        } else {
            return null;
        }
    }

}

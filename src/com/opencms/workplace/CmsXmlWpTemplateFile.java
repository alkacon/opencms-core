package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;

/**
 * Content definition for Workplace template files.
 * 
 * @author Alexander Lucas

 * @author Michael Emmerich
 * @version $Revision: 1.6 $ $Date: 2000/01/26 10:40:22 $
 */
public class CmsXmlWpTemplateFile extends CmsXmlTemplateFile implements I_CmsLogChannels {

    private Hashtable m_wpTags = new Hashtable();
    
    /** Reference to the actual language file. */
    private CmsXmlLanguageFile m_languageFile = null;
    
    /**
     * Default constructor.
     */
    public CmsXmlWpTemplateFile() throws CmsException {
        super();
        registerMyTags();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlWpTemplateFile(A_CmsObject cms, String filename) throws CmsException {
        super();
        registerMyTags();
        init(cms, filename);
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlWpTemplateFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        registerMyTags();
        init(cms, file);
    }            
    
    /**
     * Overridden init method of A_CmsXmlContent.
     * This method is now extended to get an actual instance of the
     * language file.
     * @param cms A_CmsObject Object for accessing resources.
     * @param file CmsFile object of the file to be loaded and parsed.
     * @exception CmsException
     */
    public void init(A_CmsObject cms, String filename) throws CmsException {
        m_languageFile = new CmsXmlLanguageFile(cms);
        super.init(cms, filename);
    }
    
    /**
     * Overridden init method of A_CmsXmlContent.
     * This method is now extended to get an actual instance of the
     * language file.
     * @param cms A_CmsObject Object for accessing resources.
     * @param file CmsFile object of the file to be loaded and parsed.
     * @exception CmsException
     */
    public void init(A_CmsObject cms, CmsFile file) throws CmsException {
        m_languageFile = new CmsXmlLanguageFile(cms);
        super.init(cms, file);
    }
        
    /**
     * Registers the special tags for processing with
     * processNode().
     */
    private void registerMyTags() {
        registerTag("BUTTON", "com.opencms.workplace.CmsButton");
        registerTag("BUTTONSEPARATOR", "com.opencms.workplace.CmsButtonSeparator");
        registerTag("LABEL", "com.opencms.workplace.CmsLabel");
        registerTag("INPUTFIELD", "com.opencms.workplace.CmsInput");
    }    
    
    /**
     * Special registerTag method for this content definition class.
     * Any workplace XML tag will be registered with the superclass for handling with
     * the method <code>handleAnyTag()</code> in this class.
     * Then the tagname together with the name of the class for the template
     * element (e.g. <code>CmsButton</code> or <code>CmsLabel</code>) will be put in an internal Hashtable.
     * <P>
     * Every workplace element class used by this method has to implement the interface
     * <code>I_CmsWpElement</code>
     * 
     * @param tagname XML tag to be registered as a special workplace tag.
     * @param elementClassName Appropriate workplace element class name for this tag.
     * @see com.opencms.workplace.I_CmsWpElement
     */
    private void registerTag(String tagname, String elementClassName) {
        super.registerTag(tagname, CmsXmlWpTemplateFile.class, "handleAnyTag", C_REGISTER_MAIN_RUN); 
        m_wpTags.put(tagname.toLowerCase(), elementClassName);
    }
        
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "WORKPLACE";
    }
    
    /**
     * Gets the actual instance of the language file.
     * @return Language file.
     */
    public CmsXmlLanguageFile getLanguageFile() {
        return m_languageFile; 
    }
    
    /**
     * Gets the processed data of the <code>&lt;TEMPLATE&gt;</code> section of
     * this workplace template file.
     * 
     * @param callingObject reference to the calling object. Used to look up user methods while processing.
     * @param parameters hashtable containing all user parameters.
     * @return Processed template data.
     * @exception CmsException
     */
    public String getProcessedTemplateContent(Object callingObject, Hashtable parameters) throws CmsException {
        return getProcessedDataValue("TEMPLATE", callingObject, parameters);
    }
    
    
    /**
     * Handles any occurence of any special workplace XML tag like <code>&lt;BUTTON&gt;</code> or 
     * <code>&lt;LABEL&gt;</code>. Looks up the appropriate workplace element class for the current
     * tag and calls the <code>handleSpecialWorkplaceTag()</code> method of this class.
     * <P>
     * Every workplace element class used by this method has to implement the interface
     * <code>I_CmsWpElement</code>
     * 
     * @param n XML element containing the current special workplace tag.
     * @param callingObject reference to the calling object <em>(not used here)</em>.
     * @param userObj hashtable containig all user parameters.
     * @exception CmsException
     * @see com.opencms.workplace.I_CmsWpElement
     */
    public Object handleAnyTag(Element n, Object callingObject, Object userObj) throws CmsException {
        Object result = null;        
        I_CmsWpElement workplaceObject = null;        
        String tagname = n.getTagName().toLowerCase();
        String classname = null;
        
        classname = (String)m_wpTags.get(tagname);
        if(classname == null || "".equals(classname)) {
            throwException("Don't know which class handles " + tagname + " tags.");            
        }            
    
        Object loadedClass = CmsTemplateClassManager.getClassInstance(m_cms, classname);
        if(!(loadedClass instanceof I_CmsWpElement)) {
            throwException("Loaded class " + classname + " is not implementing I_CmsWpElement");            
        }
    
        workplaceObject = (I_CmsWpElement)loadedClass;
        result = workplaceObject.handleSpecialWorkplaceTag(m_cms, n, (Hashtable)userObj, m_languageFile);                
    
        return result; 
    }                    
}

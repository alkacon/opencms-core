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
 * @version $Revision: 1.27 $ $Date: 2000/02/11 18:44:19 $
 */
public class CmsXmlWpTemplateFile extends CmsXmlTemplateFile implements I_CmsLogChannels,
                                                                        I_CmsWpConstants {

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
        registerTag("ICON", "com.opencms.workplace.CmsIcon");
        registerTag("BUTTONSEPARATOR", "com.opencms.workplace.CmsButtonSeparator");
        registerTag("ERRORBOX", "com.opencms.workplace.CmsErrorbox");
        registerTag("ERRORPAGE", "com.opencms.workplace.CmsErrorpage");
        registerTag("FILELIST", "com.opencms.workplace.CmsFileList");
        registerTag("INPUTFIELD", "com.opencms.workplace.CmsInput");
        registerTag("JAVASCRIPTBUTTON", "com.opencms.workplace.CmsButtonJavascript");
        registerTag("LABEL", "com.opencms.workplace.CmsLabel");
        registerTag("PASSWORD", "com.opencms.workplace.CmsInputPassword");
        registerTag("SUBMITBUTTON", "com.opencms.workplace.CmsButtonSubmit");
        registerTag("TEXTBUTTON", "com.opencms.workplace.CmsButtonText");
        registerTag("SELECT", "com.opencms.workplace.CmsSelectBox");
        registerTag("PROJECTLIST", "com.opencms.workplace.CmsProjectlist");
        registerTag("CONTEXTMENUE", "com.opencms.workplace.CmsContextmenue");
        registerTag("MESSAGEBOX", "com.opencms.workplace.CmsMessagebox");
        registerTag("RADIOBUTTON", "com.opencms.workplace.CmsRadioButtons");
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
     * Handles any occurence of any special workplace XML tag like <code>&lt;BUTTON&gt;</code> or 
     * <code>&lt;LABEL&gt;</code>. Looks up the appropriate workplace element class for the current
     * tag and calls the <code>handleSpecialWorkplaceTag()</code> method of this class.
     * <P>
     * Every workplace element class used by this method has to implement the interface
     * <code>I_CmsWpElement</code>
     * 
     * @param n XML element containing the current special workplace tag.
     * @param callingObject reference to the calling object.
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
        try {
            result = workplaceObject.handleSpecialWorkplaceTag(m_cms, n, this, callingObject, (Hashtable)userObj, m_languageFile);                
        } catch(Exception e) {
            String errorMessage = "Error while building workplace element \"" + tagname + "\": " + e;
            if(e instanceof CmsException) {
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
                }
                throw (CmsException)e;
            } else {
                e.printStackTrace();
                throwException(errorMessage);
            }            
        }
        return result; 
    }                    
    
    /**
     * Clears the startup tag that can be used in workplace documents.
     */
    public void clearStartup(){
        setData(C_TAG_STARTUP,"");
    }
    
   /**
    * Creates a datablock consisting of a single TextNode containing 
    * data and stores this block into the datablock-hashtable.
    * 
    * @param tag Key for this datablock.
    * @param data String to be put in the datablock.
    */
    public void setXmlData(String tag, String data) {
        setData(tag, data);
    }
        
    /**
     * Removes a datablock from the internal hashtable and
     * from the XML document
     * @param tag Key of the datablock to delete.
     */    
    public void removeXmlData(String tag) {
        removeData(tag);
    }
    
	/**
	 * Gets the text and CDATA content of a datablock from the 
	 * datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @return Datablock content for the given key or null if no datablock
	 * is found for this key.
	 */
    public String getXmlDataValue(String tag) throws CmsException {
        return getDataValue(tag);
    }               
    
  	/**
	 * Gets the text and CDATA content of a processed datablock from the 
	 * datablock hashtable.
	 * <P>
	 * The userObj Object is passed to all called user methods.
	 * By using this, the initiating class can pass customized data to its methods.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @param callingObject Object that should be used to look up user methods.
	 * @param userObj any object that should be passed to user methods
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
	public String getProcessedXmlDataValue(String tag, Object callingObject, Object userObj) 
            throws CmsException {
        return getProcessedDataValue(tag, callingObject, userObj);
	}

  	/**
	 * Gets the text and CDATA content of a processed datablock from the 
	 * datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
	public String getProcessedXmlDataValue(String tag) throws CmsException {
        return getProcessedDataValue(tag);
    }

  	/**
	 * Gets the text and CDATA content of a processed datablock from the 
	 * datablock hashtable.
	 * 
	 * @param tag Key for the datablocks hashtable.
	 * @param callingObject Object that should be used to look up user methods.
	 * @return Processed datablock for the given key.
	 * @exception CmsException
	 */
    public String getProcessedXmlDataValue(String tag, Object callingObject) throws CmsException {
        return getProcessedDataValue(tag, callingObject);
    }    
    
      /**
     * Checks if this Template owns a datablock with the given key.
     * @param key Datablock key to be checked.
     * @return true if a datablock is found, false otherwise.
     */
    public boolean hasXmlData(String tag) throws CmsException {
        return hasData(tag);
    }
    
    
}

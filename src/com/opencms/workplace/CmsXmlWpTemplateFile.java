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
 * @version $Revision: 1.1 $ $Date: 2000/01/25 13:46:52 $
 */
public class CmsXmlWpTemplateFile extends CmsXmlTemplateFile implements I_CmsLogChannels {

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
     * Registers the special tags for processing with
     * processNode().
     */
    private void registerMyTags() {
        registerTag("BUTTON", CmsXmlWpTemplateFile.class, "handleAnyTag", C_REGISTER_MAIN_RUN);            
        registerTag("BUTTONSEPARATOR", CmsXmlWpTemplateFile.class, "handleAnyTag", C_REGISTER_MAIN_RUN);    
    }    
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "WORKPLACE";
    }
    

    public String getProcessedTemplateContent(Object callingObject, Hashtable parameters) throws CmsException {
        return getProcessedDataValue("TEMPLATE", callingObject, parameters);
    }
    
    public Object handleAnyTag(Element n, Object callingObject, Object userObj) throws CmsException {
        Object result = null;        
        I_CmsWpElement workplaceObject = null;        
        String tagname = n.getTagName().toLowerCase();
        String classname = null;
        
        if(tagname.equals("button")) {
            classname = "com.opencms.workplace.CmsButton";
        } else if(tagname.equals("buttonseparator")) {
            classname = "com.opencms.workplace.CmsButtonSeparator";
        } else {
            throwException("Don't know which class handles " + tagname + " tags.");            
        }        
    
        Object loadedClass = CmsTemplateClassManager.getClassInstance(m_cms, classname);
        if(!(loadedClass instanceof I_CmsWpElement)) {
            throwException("Loaded class " + classname + " is not implementing I_CmsWpElement");            
        }
    
        workplaceObject = (I_CmsWpElement)loadedClass;
        result = workplaceObject.handleSpecialWorkplaceTag(m_cms, n, (Hashtable)userObj);                
    
        return result; 
    }                    
}

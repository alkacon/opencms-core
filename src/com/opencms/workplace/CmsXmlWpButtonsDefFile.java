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
public class CmsXmlWpButtonsDefFile extends A_CmsXmlContent implements I_CmsLogChannels {

    /**
     * Default constructor.
     */
    public CmsXmlWpButtonsDefFile() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlWpButtonsDefFile(A_CmsObject cms, String filename) throws CmsException {
        super();
        init(cms, filename);
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlWpButtonsDefFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }        
    
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "WP_BUTTONS";
    }
    
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return "OpenCms workplace buttons";
    }
    

    public String getButton(String name, String action, String alt) throws CmsException {
        setData("name", name);
        setData("action", action);
        setData("alt", alt);
        return getProcessedDataValue("defaultbutton");                
    }        
    
    public String getButtonSeparator() throws CmsException {
        return getProcessedDataValue("buttonseparator");
    }    
}

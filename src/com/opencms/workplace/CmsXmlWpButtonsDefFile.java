package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;


/**
 * Content definition for the workplace button element definition file.
 * 
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.7 $ $Date: 2000/02/02 10:07:59 $
 */
public class CmsXmlWpButtonsDefFile extends A_CmsXmlContent 
        implements I_CmsLogChannels, I_CmsWpConstants {

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
        return "OpenCms workplace buttons definition";
    }    

    /**
     * Gets the processed data for a button.
     * @return Processed button.
     * @exception CmsException
     */
    public String getButton(String name, String action, String alt, String href, Object callingObject) throws CmsException {
        setData("name", name);
        setData("action", action);
        setData("alt", alt);
        setData("href", href);
        return getProcessedDataValue("defaultbutton", callingObject);                
    }        
    
    /**
     * Gets the processed data for a button separator.
     * @return Processed button separator.
     * @exception CmsException
     */
    public String getButtonSeparator(Object callingObject) throws CmsException {
        return getProcessedDataValue("buttonseparator", callingObject);
    }    
    
     /**
     * Gets the processed data for a submit button.
     * @return Processed button.
     * @exception CmsException
     */
    public String getButtonSubmit(String name, String action, 
                                  String value, String style, String width)
        throws CmsException {
        setData(C_BUTTON_NAME, name);
        setData(C_BUTTON_ACTION, action);
        setData(C_BUTTON_VALUE, value);
        setData(C_BUTTON_STYLE, style);
        setData(C_BUTTON_WIDTH,width);
        return getProcessedDataValue("submitbutton");                
    }  

    /**
     * Gets the processed data for a text button.
     * @return Processed button.
     * @exception CmsException
     */
    public String getButtonText(String name, String action, 
                                  String value, String style, String width)
        throws CmsException {
        setData(C_BUTTON_NAME, name);
        setData(C_BUTTON_ACTION, action);
        setData(C_BUTTON_VALUE, value);
        setData(C_BUTTON_STYLE, style);
        setData(C_BUTTON_WIDTH, width);
        return getProcessedDataValue("textbutton");                
    }  


}

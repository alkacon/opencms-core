package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;

/**
 *  Content definition for the workplace error element definition file.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/01/26 19:22:58 $
 */
public class CmsXmlWpErrorDefFile extends A_CmsXmlContent implements I_CmsLogChannels ,
                                                                     I_CmsWpConstants {

    /**
     * Default constructor.
     */
    public CmsXmlWpErrorDefFile() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlWpErrorDefFile(A_CmsObject cms, String filename) throws CmsException {
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
    public CmsXmlWpErrorDefFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }        
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "WP_ERRORS";
    }
    
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return "OpenCms workplace errors";
    }
    
    /**
     * Gets the processed data for a errorbox.
     * @param title The title of this errorbox.
     * @param message The message of this errorbox.
     * @param reason The reason of this errorbox.
     * @param suggestion The suggestion of this errorbox.
     * @param link The reference where this errorbox forwards to.
     * @return Processed errorbox.
     * @exception CmsException
     */
    public String getErrorbox(String title, String message, String reason, 
                           String suggestion, String link)
        throws CmsException {
        setData(C_ERROR_TITLE, title);
        setData(C_ERROR_MESSAGE, message);
        setData(C_ERROR_REASON, reason);
        setData(C_ERROR_SUGGESTION, suggestion);
        setData(C_ERROR_LINK,link);
        return getProcessedDataValue(C_TAG_ERRORBOX);                
     }  

     /**
     * Gets the processed data for a password field.
     * @param styleClass The style class of this password field.
     * @param name The name of this password field.
     * @param size The size of this password field
     * @param length The input length of this password field.
     * @return Processed password field.
     * @exception CmsException
     */
    public String getPassword(String styleClass, String name, String size, String length)
        throws CmsException {
        setData(C_INPUT_CLASS, styleClass);
        setData(C_INPUT_NAME, name);
        setData(C_INPUT_SIZE, size);
        setData(C_INPUT_LENGTH, length);
        return getProcessedDataValue(C_TAG_PASSWORD);                
     }  

    
    
}

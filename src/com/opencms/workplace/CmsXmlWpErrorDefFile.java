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
 * @version $Revision: 1.2 $ $Date: 2000/01/27 10:54:38 $
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
     * @param msgReason Fixed reason text in errorbox.
     * @param msgButton Fixed button text.
     * @return Processed errorbox.
     * @exception CmsException
     */
    public String getErrorbox(String title, String message, String reason, 
                           String suggestion, String link, String msgReason,
                           String msgButton)
        throws CmsException {
        setData(C_ERROR_TITLE, title);
        setData(C_ERROR_MESSAGE, message);
        setData(C_ERROR_REASON, reason);
        setData(C_ERROR_SUGGESTION, suggestion);
        setData(C_ERROR_LINK,link);
        setData(C_ERROR_MSG_REASON,msgReason);
        setData(C_ERROR_MSG_BUTTON,msgButton);
        return getProcessedDataValue(C_TAG_ERRORBOX);                
     }  
    
      /**
     * Gets the processed data for a errorpage.
     * @param title The title of this errorpage.
     * @param message The message of this errorpage.
     * @param reason The reason of this errorpage.
     * @param suggestion The suggestion of this errorpage.
     * @param link The reference where this errorpage forwards to.
     * @param msgReason Fixed reason text in errorbox.
     * @param msgButton Fixed button text.
     * @return Processed errorpage.
     * @exception CmsException
     */
    public String getErrorpage(String title, String message, String reason, 
                           String suggestion, String link, String msgReason,
                           String msgButton)
        throws CmsException {
        setData(C_ERROR_TITLE, title);
        setData(C_ERROR_MESSAGE, message);
        setData(C_ERROR_REASON, reason);
        setData(C_ERROR_SUGGESTION, suggestion);
        setData(C_ERROR_LINK,link);
        setData(C_ERROR_MSG_REASON,msgReason);
        setData(C_ERROR_MSG_BUTTON,msgButton);
        return getProcessedDataValue(C_TAG_ERRORPAGE);                
     }  
    
}

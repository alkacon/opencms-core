package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;

/**
 *  Content definition for the workplace label element definition file.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 2000/01/26 09:55:48 $
 */
public class CmsXmlWpLabelDefFile extends A_CmsXmlContent implements I_CmsLogChannels ,
                                                                     I_CmsWpConstants {

    /**
     * Default constructor.
     */
    public CmsXmlWpLabelDefFile() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlWpLabelDefFile(A_CmsObject cms, String filename) throws CmsException {
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
    public CmsXmlWpLabelDefFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }        
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "WP_LABELS";
    }
    
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return "OpenCms workplace labels";
    }
    
    /**
     * Gets the processed data for a label.
     * @param value The value of this label.
     * @return Processed label.
     * @exception CmsException
     */
     public String getLabel(String value)
        throws CmsException {
        setData(C_LABEL_VALUE, value);
        return getProcessedDataValue(C_TAG_LABEL);                
    }     

}

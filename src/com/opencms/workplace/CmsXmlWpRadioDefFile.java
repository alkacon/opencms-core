package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;

/**
 * Content definition for the workplace radiobutton element definition file.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 2000/02/13 12:36:16 $
 */
public class CmsXmlWpRadioDefFile extends A_CmsXmlContent implements I_CmsLogChannels ,
                                                                     I_CmsWpConstants {

    /**
     * Default constructor.
     */
    public CmsXmlWpRadioDefFile() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlWpRadioDefFile(A_CmsObject cms, String filename) throws CmsException {
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
    public CmsXmlWpRadioDefFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }        
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "WP_RADIO";
    }
    
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return "OpenCms workplace radiobuttons";
    }
    
    /**
     * Gets the processed data for a radio button.
     * @param radioname The name of this radio button.
     * @param name The name displayed for this radio button.
     * @param link The link for this radio button.
     * @param icon The icon displayed for this radio button.
     * @return Processed radio buttons.
     * @exception CmsException
     */
    public String getRadio(String radioname,String name,String link, String icon,Object callingObject)
        throws CmsException {
        setData(C_RADIO_RADIONAME, radioname);
        setData(C_RADIO_NAME,name);
        setData(C_RADIO_LINK,link);
        setData(C_RADIO_IMAGE,"ic_file_"+icon+".gif");
        return getProcessedDataValue("radiobuttons.entry",callingObject);  
     }  

    /**
     * Gets the processed data for a selected radio button.
     * @param radioname The name of this radio button.
     * @param name The name displayed for this radio button.
     * @param link The link for this radio button.
     * @param icon The icon displayed for this radio button.
     * @return Processed radio buttons.
     * @exception CmsException
     */
    public String getRadioSelected(String radioname,String name,String link, String icon,Object callingObject)
        throws CmsException {
        setData(C_RADIO_RADIONAME, radioname);
        setData(C_RADIO_NAME,name);
        setData(C_RADIO_LINK,link);
        setData(C_RADIO_IMAGE,"ic_file_"+icon+".gif");
        return getProcessedDataValue("radiobuttons.entryselected",callingObject);  
     }  
}

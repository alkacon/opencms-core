package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;


/**
 * Content definition for "/workplace/workplace.ini".
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.3 $ $Date: 2000/01/25 17:19:29 $
 */
public class CmsXmlWpConfigFile extends A_CmsXmlContent implements I_CmsLogChannels, I_CmsConstants {

    /**
     * Default constructor.
     */
    public CmsXmlWpConfigFile() throws CmsException {
        super();
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlWpConfigFile(A_CmsObject cms, String filename) throws CmsException {
        super();
        init(cms, filename);
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given CmsFile object.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlWpConfigFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }        

    /**
     * Constructor for creating a new config file object containing the content
     * of the actual system config file.
     * <P>
     * The position of the workplace.ini is defined in I_CmsConstants.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     */        
     public CmsXmlWpConfigFile(A_CmsObject cms) throws CmsException {
        super();
        CmsFile configFile = null;
        try {
           configFile = cms.readFile(C_WORKPLACE_INI);
        } catch(Exception e) {
            throwException("Configuration file \"workplace.ini\" missing.", CmsException.C_NOT_FOUND);
        }        
        init(cms, configFile);
    }
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "WORKPLACEDEF";
    }
    
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return "OpenCms workplace configuration file";
    }        
  
    /**
     * Gets the path for OpenCms language files.
     * @return Path for language files.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getLanguagePath() throws CmsException {      
        return getDataValue("path.language");                    
    }

    /**
     * Gets the path for OpenCms workplaces templates.
     * @return Path for OpenCms workplaces templates.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getWorkplaceTemplatePath() throws CmsException {
        return getDataValue("path.wptemplates");
    }

    /**
     * Gets the path for OpenCms element templates like ButtonTemplate.
     * @return Path for OpenCms workplaces templates.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getWorkplaceElementPath() throws CmsException {
        return getDataValue("path.wpelements");
    }
    
    
    /**
     * Gets the path for OpenCms common templates.
     * @return Path for OpenCms common templates.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getCommonTemplatePath() throws CmsException {
        return getDataValue("path.commontemplates");
    }
    
    /**
     * Gets the path for picture files.
     * @return Path for picture files.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getPicturePath() throws CmsException {
        return getDataValue("path.pictures");
    }
    
    /**
     * Gets the path for the "pics" mountpoint.
     * @return Path for the "pics" mountpoint.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getPictureUrl() throws CmsException {
        return getDataValue("path.picsurl");
    }
    
    /**
     * Overridden internal method for getting datablocks.
     * This method first checkes, if the requested value exists.
     * Otherwise it throws an exception of the type C_XML_TAG_MISSING.
     * 
     * @param tag requested datablock.
     * @return Value of the datablock.
     * @exception CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */
    public String getDataValue(String tag) throws CmsException {
        String result = null;
        if(!hasData(tag)) {
            String errorMessage = "Mandatory tag \"" + tag + "\" missing in workplace definition file.";
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_XML_TAG_MISSING);     
        } else {
            result = super.getDataValue(tag);
        }
        return result;
    }
}

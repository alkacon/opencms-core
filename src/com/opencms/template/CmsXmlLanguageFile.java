package com.opencms.template;

import com.opencms.file.*;
import com.opencms.core.*;

/**
 * Content definition for language files.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/01/25 14:02:39 $
 */
public class CmsXmlLanguageFile extends A_CmsXmlContent implements I_CmsLogChannels {

    /** Name of the class specific language section. */
    private String m_localSection = null;
    
    /**
     * Default constructor.
     */
    public CmsXmlLanguageFile() throws CmsException {
        super();
    }
    
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlLanguageFile(A_CmsObject cms, String filename) throws CmsException {
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
    public CmsXmlLanguageFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }        
    
    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */
    public String getXmlDocumentTagName() {
        return "LANGUAGE";
    }
    
    /**
     * Gets a description of this content type.
     * @return Content type description.
     */
    public String getContentDescription() {
        return "Language definition file";
    }        
  

    /**
     * Sets the class specific language section.
     * When requesting a language value this section will be
     * checked first, before looking up the global value.
     * @param section class specific language section.
     */
    public void setClassSpecificLangSection(String section) {
        m_localSection = section;
    }
    
    /**
     * Gets the language value vor the requested tag.
     * @param tag requested tag.
     * @return Language value.
     */
    public String getLanguageValue(String tag) throws CmsException {
        String result = null;
        if(m_localSection != null) {
            String localizedTag = m_localSection + "." + tag;
            if(hasData(localizedTag)) {
                result = getDataValue(localizedTag);
            }
        }
        if(result == null && hasData(tag)) {
            result = getDataValue(tag);
        }
        if(result == null) {
            result = "?" + tag.substring(tag.lastIndexOf(".")+1) + "?";
        }
        
        return result;
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

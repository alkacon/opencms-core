/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsXmlLanguageFile.java,v $
 * Date   : $Date: 2000/04/11 13:38:09 $
 * Version: $Revision: 1.8 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import java.util.*;


/**
 * Content definition for language files.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.8 $ $Date: 2000/04/11 13:38:09 $
 */
public class CmsXmlLanguageFile extends A_CmsXmlContent implements I_CmsLogChannels,
                                                                   I_CmsWpConstants,
                                                                   I_CmsConstants{

    
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
     * Constructor for creating a new language file object containing the content
     * of the corresponding system language file for the actual user.
     * <P>
     * The position of the language file will be looked up in workplace.ini.
     * The selected language of the current user can be searched in the user object.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlLanguageFile(A_CmsObject cms) throws CmsException {
        super();
        CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
        
        String languagePath = configFile.getLanguagePath();
        
        // select the right language to use
        String currentLanguage=null;
        Hashtable startSettings=null;
        startSettings=(Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);                    
        // try to read it form the user additional info
        if (startSettings != null) {
            currentLanguage = (String)startSettings.get(C_START_LANGUAGE);  
        }
        // if no language was found so far, set it to default
        if (currentLanguage == null) {        
            currentLanguage = C_DEFAULT_LANGUAGE;
        }
     
        
        CmsFile languageFile = null;
        try {
            languageFile = cms.readFile(languagePath + currentLanguage);
        } catch(Exception e) {
            e.printStackTrace();
            throwException("Could not load language file " + languagePath + currentLanguage + ".", CmsException.C_NOT_FOUND);
        }
        
        init(cms, languageFile);
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
     * Checks if there exists a language value vor the requested tag.
     * @param tag requested tag.
     * @return Language value.
     */
    public boolean hasLanguageValue(String tag) {
        if(m_localSection != null) {
            return (hasData(tag) || hasData(m_localSection + "." + tag));
        } else {
            return hasData(tag);
        }        
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
            String errorMessage = "Mandatory tag \"" + tag + "\" missing in language file \"" + getFilename() + "\".";
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

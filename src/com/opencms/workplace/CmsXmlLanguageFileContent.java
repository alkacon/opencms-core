/*
* File   : $Source: /usr/local/cvs/opencms/src/com/opencms/workplace/CmsXmlLanguageFile.java,v $
* Date   : $Date: 2002/12/06 23:16:46 $
* Version: $Revision: 1.36 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.workplace;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.template.A_CmsXmlContent;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Content definition for language files.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.36 $ $Date: 2002/12/06 23:16:46 $
 */
public class CmsXmlLanguageFileContent extends A_CmsXmlContent implements I_CmsLogChannels,I_CmsWpConstants,I_CmsConstants {


    /** Name of the class specific language section. */
    private String m_localSection = null;


    /** Name of the class specific language section. */
    private static String m_languagePath = null;

    /**
     * Default constructor.
     */

    public CmsXmlLanguageFileContent() throws CmsException {
        super();
    }

    /**
     * Constructor for creating a new language file object containing the content
     * of the corresponding system language file for the actual user.
     * <P>
     * The position of the language file will be looked up in workplace.ini.
     * The selected language of the current user can be searched in the user object.
     *
     * @param cms CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */

    public CmsXmlLanguageFileContent(CmsObject cms) throws CmsException {
        super();
        String currentLanguage = getCurrentUserLanguage(cms);
        try {
            mergeLanguageFiles(cms, currentLanguage);
            if("en".equalsIgnoreCase(currentLanguage)) {
                mergeLanguageFiles(cms, "uk");
            } else if("uk".equalsIgnoreCase(currentLanguage)){
                mergeLanguageFiles(cms, "en");
            }
        }
        catch(Exception e) {
            throwException("Error while merging language files in folder " + m_languagePath + currentLanguage + "/.");
        }
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     *
     * @param cms CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */

    public CmsXmlLanguageFileContent(CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }

    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     *
     * @param cms CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */

    public CmsXmlLanguageFileContent(CmsObject cms, String filename) throws CmsException {
        super();
        init(cms, filename);
    }

    /**
     * Gets a description of this content type.
     * @return Content type description.
     */

    public String getContentDescription() {
        return "Language definition file";
    }

    /**
     * Get the code of the language the user prefers.
     * This language will be taken from the user's start settings.
     * If the user hasn't configured a language yet, if will be
     * taken from the "Accept-Language" header of the request.
     * Finally, there is a fallback value (English), if no preferred
     * language can be found or none of the preferred languages exists.
     *
     * @param cms CmsObject for accessing system resources.
     * @return Code of the preferred language (e.g. "en" or "de")
     */
    public static String getCurrentUserLanguage(CmsObject cms) throws CmsException {
        if(m_languagePath == null) {
            CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
            m_languagePath = configFile.getLanguagePath();
        }
        // In case you want only english language (the default), uncomment the following line
        // if (0 < 1) return C_DEFAULT_LANGUAGE;
        
        // select the right language to use
        String currentLanguage = null;
        Hashtable startSettings = null;
        startSettings = (Hashtable)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_STARTSETTINGS);

        // try to read it form the user additional info
        if(startSettings != null) {
            currentLanguage = (String)startSettings.get(C_START_LANGUAGE);
        }

        // no startup language found. Check the browser's preferred languages
        if(currentLanguage == null) {
            Vector langs = cms.getRequestContext().getAcceptedLanguages();
            int numlangs = langs.size();
            for(int i=0; i<numlangs; i++) {
                String loop = (String)langs.elementAt(i);
                boolean supported;
                if("en".equalsIgnoreCase(loop) || "uk".equalsIgnoreCase(loop)) {
                    supported = languageSupported(cms, "en") || languageSupported(cms, "uk");
                    currentLanguage = "en";
                } else {
                    supported = languageSupported(cms, (String)langs.elementAt(i));
                }
                if(supported) {
                    currentLanguage = (String)langs.elementAt(i);
                    break;
                }
            }
        }

        // if no language was found so far, set it to default
        if(currentLanguage == null) {
            currentLanguage = C_DEFAULT_LANGUAGE;
        }

        return currentLanguage;
    }

    /**
     * Overridden internal method for getting datablocks.
     * This method first checkes, if the requested value exists.
     * Otherwise it throws an exception of the type C_XML_TAG_MISSING.
     *
     * @param tag requested datablock.
     * @return Value of the datablock.
     * @throws CmsException if the corresponding XML tag doesn't exist in the workplace definition file.
     */

    public String getDataValue(String tag) throws CmsException {
        String result = null;
        if(!hasData(tag)) {
            String errorMessage = "Mandatory tag \"" + tag + "\" missing in language file \"" + getFilename() + "\".";
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
            }
            throw new CmsException(errorMessage, CmsException.C_XML_TAG_MISSING);
        }
        else {
            result = super.getDataValue(tag);
        }
        return result;
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
            result = "?" + tag.substring(tag.lastIndexOf(".") + 1) + "?";
        }
        return result;
    }
    
    /**
     * Method returns content encoding attached to this language.
     * @param cms
     * @return String
     */
    //Gridnine AB Aug 8, 2002
    public String getEncoding() {
        String result = null;
        try {
            result = getLanguageValue(C_PROPERTY_CONTENT_ENCODING);
        } catch (CmsException e) {;}
        if ((result != null) && result.startsWith("?") && result.endsWith("?")) {
            return null;
        }
        return result;
    }

    /**
     * Gets the expected tagname for the XML documents of this content type
     * @return Expected XML tagname.
     */

    public String getXmlDocumentTagName() {
        return "LANGUAGE";
    }

    /**
     * Checks if there exists a language value vor the requested tag.
     * @param tag requested tag.
     * @return Language value.
     */

    public boolean hasLanguageValue(String tag) {
        if(m_localSection != null) {
            return (hasData(tag) || hasData(m_localSection + "." + tag));
        }
        else {
            return hasData(tag);
        }
    }

    /**
     * Merges all language files available for current language settings.
     * Language files have to be stored in a folder like
     * "system/workplace/config/language/[language shortname]/"
     *
     * @author Matthias Schreiber
     * @param cms CmsObject object for accessing system resources.
     * @param language Current language
     */

    private void mergeLanguageFiles(CmsObject cms, String language) throws CmsException {
        Vector langFiles = new Vector();

        // Make sure old "uk" stuff still works        
        if ("uk".equals(language)) language = "en";
        
        langFiles = cms.getFilesInFolder(m_languagePath + language + "/");

        // get all modules-language Files
        Vector modules = new Vector();
        modules = cms.getSubFolders(I_CmsWpConstants.C_VFS_PATH_MODULES);
        String lang = I_CmsWpConstants.C_VFS_DIR_LOCALES + language + "/";
        // make sure old modules language files still work
        String oldLang = "language/" + language + "/";
        for(int i = 0;i < modules.size();i++) {
            Vector moduleLangFiles = new Vector();
            try {
                moduleLangFiles = cms.getFilesInFolder(((CmsFolder)modules.elementAt(i)).getAbsolutePath() + lang);
            } catch (CmsException e) {
                // try read from old module locales path
                try {
                    moduleLangFiles = cms.getFilesInFolder(((CmsFolder)modules.elementAt(i)).getAbsolutePath() + oldLang);
                    if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_INFO) ) {
                        A_OpenCms.log(C_OPENCMS_INFO, "[" + this.getClass().getName() + ".mergeLanguageFiles/1] Old module 'locales' path used: " + ((CmsFolder)modules.elementAt(i)).getAbsolutePath() + oldLang);
                    }                    
                } catch (CmsException ex) {
                    // no language files found, we can live with that, probably the module just has none                      
                }
            }
            for(int j = 0;j < moduleLangFiles.size();j++) {
                langFiles.addElement(moduleLangFiles.elementAt(j));
            }
        }
        CmsFile file = null;
        for(int i = 0;i < langFiles.size();i++) {
            file = (CmsFile)langFiles.elementAt(i);
            if(file.getState() != C_STATE_DELETED) {
                try {
                    init(cms, file.getAbsolutePath());
                    readIncludeFile(file.getAbsolutePath());
                } catch(Exception exc) {
                    if(C_LOGGING && A_OpenCms.isLogging(C_OPENCMS_CRITICAL) ) {
                        A_OpenCms.log(C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + ".mergeLanguageFiles/3] Error merging language file: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Check, if the language with the given code is supported by the OpenCms
     * workplace. Will be performed by checking the existance of the folder
     * <code>m_languagePath + lang + "/"</code>
     * @return <code>true</code> if language is supported, <code>false</code> otherwise.
     */
    private static boolean languageSupported(CmsObject cms, String lang) {
        try {
            cms.readFolder(m_languagePath + lang);
        } catch(CmsException e) {
            // doesn't seem to exist
            return false;
        }
        return true;
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
}
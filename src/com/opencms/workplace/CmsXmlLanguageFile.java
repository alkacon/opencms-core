/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsXmlLanguageFile.java,v $
* Date   : $Date: 2003/01/31 10:03:25 $
* Version: $Revision: 1.39 $
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

/**
 * Provides access to the localized lables for the workplace.<p>
 * 
 * This class used to read language files from the OpenCms VFS in a proprietary 
 * XML format, hence the name "XmlLanguageFile". Since 5.0rc2 this class has
 * been changed to use the standard <code>java.util.ResouceBundle</code> technology.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.39 $ $Date: 2003/01/31 10:03:25 $
 */
import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.flex.util.CmsMessages;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

public class CmsXmlLanguageFile { 

    /** The name of the property file */
    public static final String C_BUNDLE_NAME = "com.opencms.workplace.workplace";
        
    /** Localized message access object for the default workplace */
    private CmsMessages m_messages;
    
    /** Set of locales form the installed modules */
    private static Set m_moduleMessages = null;     
    
    /** Flag to indicate support for old locale mechanism */
    private boolean m_supportOldLocale = false;
    
    /** CmsObject provided with the constructror */
    private CmsObject m_cms;
    
    /** Locale (2 letter ISO country code like "en") */
    private String m_locale;
    	         
    /**
     * Constructor for creating a new language file 
     * initialized with the workplace preferences locale of the current user.<p>
     * 
     * @param cms for accessing system resources
     */    
	public CmsXmlLanguageFile(CmsObject cms) throws CmsException {
        this(cms, getCurrentUserLanguage(cms));
	}

    /**
     * Constructor for creating a new language file 
     * initialized with the provided locale.<p>
     *
     * @param cms for accessing system resources
     * @param locale the locale to initialize 
     */
    public CmsXmlLanguageFile(CmsObject cms, String locale) throws CmsException {
        m_cms = cms;
        m_locale = locale;
        m_messages = new CmsMessages(C_BUNDLE_NAME, m_locale);        
        m_moduleMessages = collectModuleMessages(m_cms);
        Boolean flag = (Boolean)A_OpenCms.getRuntimeProperty("compatibility.support.oldlocales");
        m_supportOldLocale = (flag!=null)?flag.booleanValue():false;        
    }
    
    /**
     * Gathers all localization files for the workplace from the different modules.<p>
     * 
     * For a module named "my.module.name" the locale file must be named 
     * "my.module.name.workplace" and be localed in the classpath so that the resource loader
     * can find it.<p>
     * 
     * @param cms for accessing system resources
     */
    private Set collectModuleMessages(CmsObject cms) {
        HashSet bundles = new HashSet();
        Enumeration en = null;
        try {
            en = cms.getRegistry().getModuleNames();
        } catch (CmsException e) {
            // no bundles will be read in this case, this is o.k.
        }
        if (en != null) {
            while (en.hasMoreElements()) {
                String bundleName = ((String)en.nextElement()) + ".workplace";
                // this should result in a name like "my.module.name.workplace"
                try {
                    ResourceBundle bundle = ResourceBundle.getBundle(bundleName, new Locale("en"));
                    bundles.add(bundle);
                } catch (MissingResourceException e) {
                    // can be ignored
                }
            }
        }
        return bundles;
    }

    /**
     * Returns the content encoding defined for this language.<p>
     * 
     * @return String the the content encoding defined for this language
     */
    public String getEncoding() {
        String result = null;
        try {
            result = m_messages.getString(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING);
        } catch (MissingResourceException e) {
            result = A_OpenCms.getDefaultEncoding();
        }
        return result;
    }

    /**
     * Returns the language value of the requested label key.<p>
     *
     * @param keyName the key for the desired string 
     * @return the resource string for the given key 
     */
    public String getLanguageValue(String keyName) {       
        if (m_moduleMessages.size() == 0) return m_messages.key(keyName);
        try {
            return m_messages.getString(keyName);
        } catch (MissingResourceException e) {}
        // key was not found in default workplace bundles
        Iterator i = m_moduleMessages.iterator();
        while (i.hasNext()) {
            try {
                return ((ResourceBundle) i.next()).getString(keyName);
                // if no exception is thrown here we have found the result
            } catch (MissingResourceException e) {
                // ignore and continue looking in the other bundles
            }
        }
        if (m_supportOldLocale) {
            // we have not found the key and we are in old compatiblity mode,
            // so let's look up the XML locales
            try {
                CmsXmlLanguageFileContent langFile = new CmsXmlLanguageFileContent(m_cms, m_locale);
                String value = langFile.getLanguageValue(keyName); 
                if (value != null) return value;                              
            } catch (CmsException e) {
                // we have not found the keyName in the XML files either
            }
        }
        
        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INFO)) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, this.getClass().getName() + 
                ".getLanguageValue() - Missing value for locale key: " + keyName);
        }        
        return "??? " + keyName + " ???";	
    }
        
    /**
     * Returns the language set for the current user.<p>
     * 
     * This is look up in the following places:<ol>
     * <li>the current users session
     * <li>the current users workplace preferences
     * <li>the current users browser settings</ol>
     * 
     * If a result is found it is stored in the users session.<p>
     * 
     * @param cms for accessing system resources
     * @return 2-letter ISO code of the preferred language (e.g. "en", "de", ...)
     */
    public static String getCurrentUserLanguage(CmsObject cms) {
        // check out the which language is to be used as default
        String currentLanguage = null;                                   
        I_CmsSession session = cms.getRequestContext().getSession(false);
        // check if there is a session        
        if (session != null) {
            // if so, try to read users current language from the session
            currentLanguage = (String)session.getValue(I_CmsConstants.C_START_LANGUAGE); 
        }                 
        if (currentLanguage == null) {
            // language is not stored in the session yet, must check it out the hard way
            Hashtable startSettings =
                (Hashtable) cms.getRequestContext().currentUser().getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_STARTSETTINGS);  
            // try to read it form the user additional info
            if (startSettings != null) {
                currentLanguage = (String) startSettings.get(I_CmsConstants.C_START_LANGUAGE);
            }    
            // no startup language in user settings found, so check the users browser locale settings
            if (currentLanguage == null) {
                Vector language = (Vector)cms.getRequestContext().getAcceptedLanguages();
                int numlangs = language.size();
                for (int i = 0; i < numlangs; i++) {
                    String lang = (String) language.elementAt(i);
                    try {
                        cms.readFolder(I_CmsWpConstants.C_VFS_PATH_LOCALES + lang);
                        currentLanguage = lang;
                        break;
                    } catch (CmsException e) {
                        // browser language is not supported in OpenCms, continue looking
                    }
                }
            }
            // if no language was found so far, set it to the default language
            if (currentLanguage == null) {
                currentLanguage = I_CmsWpConstants.C_DEFAULT_LANGUAGE;
            }
            // store language in session
            if (session != null) {
                session.putValue(I_CmsConstants.C_START_LANGUAGE, currentLanguage);
            }
        }
        return currentLanguage;
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceMessages.java,v $
 * Date   : $Date: 2003/09/17 08:31:28 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package org.opencms.workplace;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.flex.util.CmsMessages;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Provides access to the localized lables for the workplace.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.8 $
 * 
 * @since 5.1
 */
public class CmsWorkplaceMessages { 

    /** The name of the property file */
    public static final String C_BUNDLE_NAME = "com.opencms.workplace.workplace";
    
    /** Localized message access object for the default workplace */
    private CmsMessages m_messages;
    
    /** CmsObject provided with the constructror */
    private CmsObject m_cms;
    
    /** Locale (2 letter ISO country code like "en") */
    private String m_locale;

    // static data storages to prevent multiple lookups
    /** Map of locales from the installed modules */
    private static Map m_allModuleMessages = null;     
    
    /** Set of locales from the installed modules */
    private static Set m_moduleMessages = null;  
    
    /** Map of encodings from the installed languages */
    private static Map m_allEncodings = null;

    /** DEBUG flag */
    private static final int DEBUG = 0; 

    /**
     * Constructor for creating a new messages object
     * initialized with the provided locale.<p>
     *
     * @param cms for accessing system resources
     * @param locale the locale to initialize 
     */
    public CmsWorkplaceMessages(CmsObject cms, String locale) {
        m_cms = cms;
        m_locale = locale;
        m_messages = new CmsMessages(C_BUNDLE_NAME, m_locale);   
        // initialize the static encodings map if required
        if (m_allEncodings == null) {        
            if (DEBUG > 0) System.err.println("CmsWorkplaceMessages(): initializing the static encodings");
            synchronized (this) {     
                m_allEncodings = new HashMap(); 
            }            
        }  
        // initialize the static hash if not already done
        if (m_allModuleMessages == null) {        
            if (DEBUG > 0) System.err.println("CmsWorkplaceMessages(): initializing module messages hash");
            synchronized (this) {     
                m_allModuleMessages = new HashMap(); 
            }            
        }
        // initialize the static module messages
        Object obj = m_allModuleMessages.get(m_locale);
        if (obj == null) {
            if (DEBUG > 0) System.err.println("CmsWorkplaceMessages(): collecting static module messages");
            synchronized (this) {    
                m_moduleMessages = collectModuleMessages(m_cms, m_locale);
                m_allModuleMessages.put(m_locale, m_moduleMessages);
            }            
        } else {
            m_moduleMessages = (Set)obj;
        } 
    }
    
    /**
     * Gathers all localization files for the workplace from the different modules.<p>
     * 
     * For a module named "my.module.name" the locale file must be named 
     * "my.module.name.workplace" and be located in the classpath so that the resource loader
     * can find it.<p>
     * 
     * @param cms for accessing system resources
     * @param locale the selected locale
     * @return an initialized set of module messages
     */
    private synchronized Set collectModuleMessages(CmsObject cms, String locale) {
        HashSet bundles = new HashSet();
        Enumeration en = cms.getRegistry().getModuleNames();
        if (en != null) {
            while (en.hasMoreElements()) {
                String bundleName = ((String)en.nextElement()) + ".workplace";
                // this should result in a name like "my.module.name.workplace"
                try {
                    ResourceBundle bundle = ResourceBundle.getBundle(bundleName, new Locale(locale));
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
        // try to read from static map
        String result = (String)m_allEncodings.get(m_locale);
        if (result != null) return result;

        // encoding not stored so far, let's try to figure it out
        if (DEBUG > 0) System.err.println("CmsWorkplaceMessages.getEncoding(): looking up encoding for locale " + m_locale);
        try {
            result = m_messages.getString(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING);
        } catch (MissingResourceException e) {
            // exception - just use the default encoding
            result = OpenCms.getDefaultEncoding();
        }
        if (result.startsWith("{")) {
            // this is a "supported set" - try to figure out the encoding to use
            if (result.indexOf(OpenCms.getDefaultEncoding()) >= 0) {
                // the current default encoding is supported, so we use this
                result = OpenCms.getDefaultEncoding();
            } else {
                // default encoding is not supported, so we use the first given encoding in the set       
                int index = result.indexOf(";");
                if (index <= 1) {
                    result = OpenCms.getDefaultEncoding();
                } else { 
                    result = result.substring(1, index);   
                }             
            }
        }
        // now store the result in the static map
        m_allEncodings.put(m_locale, result);
        return result;
    }    
    
    /**
     * Returns a date formatted according to the users language settings.<p>
     *  
     * @param timestamp the date to format
     * @return a date formatted according to the users language settings
     */
    public String getDate(long timestamp) {
        return m_messages.getDate(timestamp);
    }
    
    /**
     * Returns a date/time formatted according to the users language settings.<p>
     *  
     * @param timestamp the date to format
     * @return a date/time formatted according to the users language settings
     */
    public String getDateTime(long timestamp) {
        return m_messages.getDateTime(timestamp);
    }
    
    /**
     * Returns the language value of the requested label key.<p>
     *
     * @param keyName the key for the desired string 
     * @return the resource string for the given key 
     */
    public String key(String keyName) {       
        if (DEBUG > 2) System.err.println("CmsWorkplaceMessages.key(): looking for " + keyName);
        try {
            return m_messages.getString(keyName);
        } catch (MissingResourceException e) { }

        // key was not found in default workplace bundles
        if (DEBUG > 1) System.err.println("CmsWorkplaceMessages.key(): '" + keyName + "' not found in workplace messages");
        Iterator i = m_moduleMessages.iterator();
        while (i.hasNext()) {
            try {
                return ((ResourceBundle) i.next()).getString(keyName);
                // if no exception is thrown here we have found the result
            } catch (MissingResourceException e) {
                // ignore and continue looking in the other bundles
            }
        }
        if (DEBUG > 1) System.err.println("CmsWorkplaceMessages.key(): '" + keyName + "' also not found in module messages (this is not good)");
        
        if (keyName.startsWith("help.")) {
            // online help might not have been installed or missing help key, return default page
            return "index.html";
        }
        
        // key was not found
        if (DEBUG > 1) System.err.println("CmsWorkplaceMessages.key(): '" + keyName + "' not found at all (this is bad)");
        if (OpenCms.getLog(CmsLog.CHANNEL_MAIN).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_MAIN).info(this.getClass().getName() 
            + ".getLanguageValue() - Missing value for locale key: " + keyName);
        }        
        return "??? " + keyName + " ???";
    }
}

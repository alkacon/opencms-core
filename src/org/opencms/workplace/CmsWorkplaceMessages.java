/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceMessages.java,v $
 * Date   : $Date: 2005/04/10 11:00:14 $
 * Version: $Revision: 1.29 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.i18n.CmsMessages;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Provides access to the localized messages for the workplace.<p>
 * 
 * The workplace messages are collected from the workplace resource bundles of all installed modules.
 * To be recognized as a workplace resource bundle,
 * the workplace property file must follow the naming convention <code>${module_package_name}.workplace${locale}.properties</code>,
 * for example like <code>com.mycompany.module.workplace_en.properties</code>.<p> 
 * 
 * Workplace messages are cached for faster lookup. If a localized key is contained in more then one module,
 * it will be used only from the module where it was first found in. The module order is undefined. It is therefore 
 * recommended to ensure the uniqueness of all module keys by placing a special prefix in front of all keys of a module.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.29 $
 * 
 * @since 5.1
 */
public class CmsWorkplaceMessages extends CmsMessages {

    /** The name of the property file. */
    public static final String DEFAULT_WORKPLACE_MESSAGE_BUNDLE = "org.opencms.workplace.workplace";

    /** Null String value for caching of null message results. */
    public static final String NULL_STRING = "null";

    /** Map of encodings from the installed languages. */
    private static Map m_allEncodings;

    /** Map of locales from the installed modules. */
    private static Map m_allModuleMessages;

    /** Static reference to the log. */
    private static final Log LOG = OpenCms.getLog(CmsWorkplaceMessages.class);

    /** Set of locales from the installed modules. */
    private static Set m_moduleMessages;
   
    /** The workplace default encoding. */
    private static String m_workplaceDefaultEncoding;

    /** A cache for the messages to prevent multiple lookups in many bundles. */
    private Map m_messageCache;
    
    /**
     * Constructor for creating a new messages object
     * initialized with the provided locale.<p>
     * 
     * @param locale the locale to initialize 
     */
    public CmsWorkplaceMessages(Locale locale) {

        super(DEFAULT_WORKPLACE_MESSAGE_BUNDLE, locale);

        int todo = 0;
        // TODO: check this static intialization stuff  
        
        // use a hashtable since synchronization is required
        m_messageCache = new Hashtable();
        // initialize the static encodings map if required
        if (m_allEncodings == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("CmsWorkplaceMessages(): initializing the static encodings");
            }
            synchronized (this) {
                m_allEncodings = new HashMap();
                m_workplaceDefaultEncoding = OpenCms.getWorkplaceManager().getDefaultEncoding();
            }
        }
        // initialize the static hash if not already done
        if (m_allModuleMessages == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("CmsWorkplaceMessages(): initializing module messages hash");
            }
            synchronized (this) {
                m_allModuleMessages = new HashMap();
            }
        }
        // initialize the static module messages
        Object obj = m_allModuleMessages.get(m_locale);
        if (obj == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("CmsWorkplaceMessages(): collecting static module messages");
            }
            synchronized (this) {
                m_moduleMessages = collectModuleMessages(m_locale);
                m_allModuleMessages.put(m_locale, m_moduleMessages);
            }
        } else {
            m_moduleMessages = (Set)obj;
        }
    }

    /**
     * Returns the content encoding defined for this language.<p>
     * 
     * @return String the the content encoding defined for this language
     */
    public String getEncoding() {        

        int todo = 0;
        // TODO: check if this is still required - what about the "workplace locale" contained in the WP manager? 
        
        // try to read from static map
        String result = (String)m_allEncodings.get(m_locale);
        if (result != null) {
            return result;
        }
        // encoding not stored so far, let's try to figure it out
        if (LOG.isDebugEnabled()) {
            LOG.debug("CmsWorkplaceMessages.getEncoding(): looking up encoding for locale " + m_locale);
        }
        try {
            result = m_bundle.getString(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING);
        } catch (MissingResourceException e) {
            // exception - just use the default encoding
            result = m_workplaceDefaultEncoding;
            // can usually be ignored
            if (LOG.isDebugEnabled()) {
                LOG.debug(e);
            }
        }
        if (result.startsWith("{")) {
            // this is a "supported set" - try to figure out the encoding to use
            if (result.indexOf(m_workplaceDefaultEncoding) >= 0) {
                // the current default encoding is supported, so we use this
                result = m_workplaceDefaultEncoding;
            } else {
                // default encoding is not supported, so we use the first given encoding in the set       
                int index = result.indexOf(";");
                if (index <= 1) {
                    result = m_workplaceDefaultEncoding;
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
     * @see org.opencms.i18n.CmsMessages#key(java.lang.String, boolean)
     */
    public String key(String keyName, boolean allowNull) {

        // special implementation since the workplace uses several bundles for the messages
        String result = resolveKey(keyName);
        if ((result == null) && !allowNull) {
            result = formatUnknownKey(keyName);
        }

        return result;
    }

    /**
     * Gathers all localization files for the workplace from the different modules.<p>
     * 
     * For a module named "my.module.name" the locale file must be named 
     * "my.module.name.workplace" and be located in the classpath so that the resource loader
     * can find it.<p>
     * 
     * @param locale the selected locale
     * 
     * @return an initialized set of module messages
     */
    private synchronized Set collectModuleMessages(Locale locale) {

        HashSet bundles = new HashSet();
        Set names = OpenCms.getModuleManager().getModuleNames();
        if (names != null) {
            Iterator i = names.iterator();
            while (i.hasNext()) {
                String bundleName = ((String)i.next()) + ".workplace";
                // this should result in a name like "my.module.name.workplace"
                try {
                    ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
                    bundles.add(bundle);
                } catch (MissingResourceException e) {
                    // can usually be ignored
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e);
                    }
                }
            }
        }
        return bundles;
    }

    /**
     * Returns the localized resource string for a given message key,
     * checking the workplace default resources and all module bundles.<p>
     * 
     * If the key was not found, <code>null</code> is returned.<p>
     * 
     * @param keyName the key for the desired string 
     * @return the resource string for the given key 
     */
    private String resolveKey(String keyName) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolving workplace message key '" + keyName + "'");
        }

        String result = (String)m_messageCache.get(keyName);
        if (result == NULL_STRING) {
            // key was already checked and not found   
            return null;
        }
        if (result == null) {
            // so far not in the cache
            try {
                result = m_bundle.getString(keyName);
            } catch (MissingResourceException e) {
                // can usually be ignored
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e);
                }
            }
        } else {
            // result was found in cache
            if (LOG.isDebugEnabled()) {
                LOG.debug("'" + keyName + "' found in message cache, result is '" + result + "'");
            }
            return result;
        }
        if (result == null) {
            // key was not found in cache or default workplace bundles
            if (LOG.isDebugEnabled()) {
                LOG.debug("'" + keyName + "' not found in workplace messages");
            }
            Iterator i = m_moduleMessages.iterator();
            while ((result == null) && i.hasNext()) {
                try {
                    result = ((ResourceBundle)i.next()).getString(keyName);
                    // if no exception is thrown here we have found the result
                } catch (MissingResourceException e) {
                    // can usually be ignored
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e);
                    }
                }
            }
        }
        if (result == null) {
            // key was not found in "regular" bundle as well as module messages
            if (LOG.isDebugEnabled()) {
                LOG.debug("'" + keyName + "' also not found in module messages (this is not good)");
            }
            // ensure null values are also cached
            m_messageCache.put(keyName, NULL_STRING);
        } else {
            // optional debug output
            if (LOG.isDebugEnabled()) {
                LOG.debug("Workplace message for key '" + keyName + "' is '" + result + "'");
            }
            // cache the result
            m_messageCache.put(keyName, result);
        }
        // return the result        
        return result;
    }
}

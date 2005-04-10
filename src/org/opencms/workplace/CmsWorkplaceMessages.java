/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceMessages.java,v $
 * Date   : $Date: 2005/04/10 21:00:47 $
 * Version: $Revision: 1.30 $
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
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Provides access to the localized messages for the workplace.<p>
 * 
 * The workplace messages are collected from the workplace resource bundles of all installed modules,
 * plus the default workplace messages of the OpenCms core.
 * To be recognized as a workplace resource bundle,
 * the workplace property file must follow the naming convention <code>${module_package_name}.workplace${locale}.properties</code>,
 * for example like <code>com.mycompany.module.workplace_en.properties</code>.<p> 
 * 
 * Workplace messages are cached for faster lookup. If a localized key is contained in more then one module,
 * it will be used only from the module where it was first found in. The module order is undefined. It is therefore 
 * recommended to ensure the uniqueness of all module keys by placing a special prefix in front of all keys of a module.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.30 $
 * 
 * @since 5.1
 */
public class CmsWorkplaceMessages extends CmsMessages {

    /** The name of the property file. */
    public static final String DEFAULT_WORKPLACE_MESSAGE_BUNDLE = "org.opencms.workplace.workplace";

    /** Null String value for caching of null message results. */
    public static final String NULL_STRING = "null";

    /** Static reference to the log. */
    private static final Log LOG = OpenCms.getLog(CmsWorkplaceMessages.class);

    /** List of resource bundles from the installed modules. */
    private List m_bundles;

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
        // use "old" Hashtable since it is the most efficient synchronized HashMap implementation
        m_messageCache = new Hashtable();
        // collect the messages from the available modules
        m_bundles = collectModuleMessages(locale);
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
    private synchronized List collectModuleMessages(Locale locale) {

        // create a new list and add the base bundle
        ArrayList result = new ArrayList();
        result.add(m_bundle);

        Set names = OpenCms.getModuleManager().getModuleNames();
        if (names != null) {
            // iterate all module names
            Iterator i = names.iterator();
            while (i.hasNext()) {
                String bundleName = ((String)i.next()) + ".workplace";
                // this should result in a name like "my.module.name.workplace"
                try {
                    // try to load a bundle with the module names
                    ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
                    // bundle was loaded, add to list of bundles
                    result.add(bundle);
                } catch (MissingResourceException e) {
                    // can usually be ignored
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e);
                    }
                }
            }
        }
        return result;
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
            for (int i = 0; (result == null) && (i < m_bundles.size()); i++) {
                try {
                    result = ((ResourceBundle)m_bundles.get(i)).getString(keyName);
                    // if no exception is thrown here we have found the result
                } catch (MissingResourceException e) {
                    // can usually be ignored
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e);
                    }
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
            // key was not found in "regular" bundle as well as module messages
            if (LOG.isDebugEnabled()) {
                LOG.debug("'" + keyName + "' not found in all module messages");
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

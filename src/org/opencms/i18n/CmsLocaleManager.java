/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/CmsLocaleManager.java,v $
 * Date   : $Date: 2004/02/05 13:51:07 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.i18n;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringSubstitution;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Manages the locales configured for this OpenCms installation.<p>
 * 
 * Locale configuration is done in <code>opencms.properties</code>.<p> 
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class CmsLocaleManager {
    
    /** The default locale names (must be a subset of the available locale names) */
    private List m_defaultLocaleNames;    
    
    /** The set of available locale names */
    private List m_availableLocaleNames;

    /** The available locales mapped to their locale names */
    private Map m_availableLocales;
    
    /** The configured locale handler */
    private I_CmsLocaleHandler m_localeHandler;
    
    /** Runtime property name for locale handler */
    public static final String C_LOCALE_HANDLER = "class_locale_handler";
    
    /**
     * Initializes the CmsLocaleManager by reading the properties
     * <code>locale.available</code>
     * <code>locale.default</code>
     * in <code>opencms.properties</code>.<p>
     *
     * @param localeHandler the configured locale handler
     * @param availableLocaleNames the available (i.e. allowed) locale names
     * @param defaultLocaleNames the default locale names
     */
    public CmsLocaleManager (I_CmsLocaleHandler localeHandler, String[] availableLocaleNames, String[] defaultLocaleNames) {

        // set the locale handler
        m_localeHandler = localeHandler;    
        
        // set available locale names
        m_availableLocaleNames = Arrays.asList(availableLocaleNames);
        
        // set default locale names
        m_defaultLocaleNames = checkLocaleNames(Arrays.asList(defaultLocaleNames));
        
        // init locale objects from locale names
        m_availableLocales = new HashMap();
        Iterator i = m_availableLocaleNames.iterator();
        while (i.hasNext()) {
            String localeName = (String)i.next();
            String localeNames[] = CmsStringSubstitution.split(localeName, "_");
            Locale locale = new Locale(localeNames[0],
                    (localeNames.length > 1) ? localeNames[1] : "",
                    (localeNames.length > 2) ? localeNames[2] : ""
            );
            
            m_availableLocales.put(localeName, locale);
            if (localeNames.length > 2 && !m_availableLocales.containsKey(localeNames[0] + "_" + localeNames[1])) {
                m_availableLocales.put(localeNames[0] + "_" + localeNames[1], locale);
            }
            if (localeNames.length > 1 && !m_availableLocales.containsKey(localeNames[0])) {
                m_availableLocales.put(localeNames[0], locale);
            }
        }
    }

    /**
     * Initializes the CmsLocaleManager by reading the properties
     * <code>locale.available</code> and
     * <code>locale.default</code>
     * in <code>opencms.properties</code>.<p>
     * 
     * @param configuration the OpenCms configuration
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @return the initialized locale manager
     */
    public static CmsLocaleManager initialize(
        ExtendedProperties configuration, 
        CmsObject cms
    ) {
        // initialize the locale handler 
        I_CmsLocaleHandler localeHandler = null;
        String localeHandlerClass = OpenCms.getRegistry().getLocaleHandler();
        try {
            localeHandler = (I_CmsLocaleHandler)Class.forName(localeHandlerClass).newInstance();
            localeHandler.initHandler(cms);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Locale handler class : " + localeHandlerClass + " instanciated");
            }
        } catch (Exception e) {
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Locale handler class : error initializing locale handler class '" + localeHandlerClass + "' (using default locale handler instead)");
            }
            // use default locale handler            
            localeHandler = new CmsDefaultLocaleHandler();
            localeHandler.initHandler(cms);
        }
        
        // init available locales
        String[] availableLocaleNames = configuration.getStringArray("locale.available");

        // init default locale names
        String[] defaultLocaleNames = configuration.getStringArray("locale.default");        

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < availableLocaleNames.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(availableLocaleNames[i]);
            }
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Available locales    : " + buf.toString());
            
            buf = new StringBuffer();
            for (int i = 0; i < defaultLocaleNames.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(defaultLocaleNames[i]);
            }                
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Default locales      : " + buf.toString());
        }
        return new CmsLocaleManager(localeHandler, availableLocaleNames, defaultLocaleNames);
    }
    
    /**
     * Returns the name of the default locale configured in <code>opencms.properties</code>.<p>
     *
     * The default locale is the first locale of the configured default locales.
     *
     * @return the name of the default locale
     */
    public String getDefaultLocaleName() {
        return (String)m_defaultLocaleNames.get(0);
    }

    /**
     * Returns the list of default locale names configured in <code>opencms.properties</code>.<p>
     * 
     * @return the list of default locale names, e.g. <code>en, de</code>
     */
    public List getDefaultLocaleNames() {
        return m_defaultLocaleNames;
    }
    
    /**
     * Returns the list of available locale names configured in <code>opencms.properties</code>.<p>
     *
     * @return the list of available locale names, e.g. <code>en, de</code>
     */
    public List getAvailableLocaleNames() {
        return m_availableLocaleNames;
    }
    
    /**
     * Returns the configured locale handler.<p>
     * This handler is used to derive the appropriate locale for a request.
     * 
     * @return the locale handler
     */
    public I_CmsLocaleHandler getLocaleHandler() {
        return m_localeHandler;
    }
    
    /**
     * Returns a set of matching locale names from the given locale names.<p>
     * The set contains all names (eventually shortened) in the given array having
     * parts similar to a (part of a) name in the filter set.
     * 
     * Example:
     * [de, en_GB, en_US], {en_US, de} -> {de, en, en_US}
     * 
     * @param localeNames an array of locale names
     * @param filter a set of locale names
     * @return a set of filtered locale names
     */
    public Set getMatchingLocaleNames(String localeNames[], Set filter) {
    
        Set result = new HashSet();
        
        StringBuffer matching = new StringBuffer();
        for (int i = 0; i < localeNames.length; i++) {
            if (filter != null) {
                for (Iterator j = filter.iterator(); j.hasNext();) {
                    int m = match (localeNames[i], (String)j.next(), matching);
                    if (m > 0) {
                        result.add(matching.toString());
                    }
                }
            } else {
                result.add(localeNames[i]);
            }
        }
        
        return result;
    }
    
    /**
     * Returns the best matching locale name from the given locale names.<p>
     * 
     * The best matching name is the first name (eventually shortened) in the given list with the
     * maximum number of parts similar to a (part of a) name in the filter list.
     * 
     * Example:
     * getBestMatch([en_GB, de], {de, en, en_US} -> en
     *      since en_GB <> de = 0, en_GB <> en = 1, en_GB <> en_US = 1, de <> de = 1, de <> en = 0, de <> en_US = 0 
     * 
     * @param requestedLocaleName the originally requested locale name
     * @param localeNames a list of locale names
     * @param filter a list of locale names to use as filter
     * @return the best matching locale name or null if no name matches
     */
    public String getBestMatchingLocaleName(String requestedLocaleName, List localeNames, List filter) {

        if (filter == null) {
            return null;
        }
        
        String result = null;
               
        StringBuffer matching = new StringBuffer();
        int max = -1;
        for (int i = -1; i < localeNames.size(); i++) {
            String localeName = (i < 0) ? requestedLocaleName : (String)localeNames.get(i);
            if (localeName != null) {
                for (Iterator j = filter.iterator(); j.hasNext();) {
                    int m = match(localeName, (String)j.next(), matching);
                    if (m > max) {
                        max = m;
                        result = matching.toString();
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Returns the number of parts matching in the given locale names.<p>
     * 
     * @param localeName1 the first locale name
     * @param localeName2 the second locale name
     * @param matching the matching parts separated with "_" or ""
     * @return the number of matching parts (0-3)
     */
    private int match(String localeName1, String localeName2, StringBuffer matching) {
        
        String name1[] = CmsStringSubstitution.split(localeName1, "_");
        String name2[] = CmsStringSubstitution.split(localeName2, "_");
        matching.delete(0, matching.length());

        int i;
        for (i = 0; i < name1.length; i++) {
            if (name2.length <= i || !name1[i].trim().equals(name2[i].trim())) {
                return i;
            } else {
                if (i > 0) {
                    matching.append("_");
                }
                matching.append(name1[i]);
            }
        }
        
        return i;
    }
    
    /**
     * Returns an available locale identified by the given full name.<p>
     * The full name consists of language code, country code(optional), variant(optional) separated by "_"
     * 
     * @param fullName the full name
     * @return the locale or <code>null</code> if not available
     */
    public Locale getLocale(String fullName) {
        Locale l;
        
        l = (Locale)m_availableLocales.get(fullName);
        if (l != null) {
            return l;
        }

        String name[] = CmsStringSubstitution.split(fullName, "_");
        
        if (name.length >= 2) {
            l = (Locale)m_availableLocales.get(name[0] + "_" + name[1]);
            if (l != null) {
                return l;
            }
        }
        
        if (name.length >= 1) {
            l = (Locale)m_availableLocales.get(name[0]);
            if (l != null) {
                return l;
            }
        }
        
        return null;
    }    

    /**
     * Returns an array of available locale names for the given resource.<p>
     * 
     * @param cms the current cms permission object
     * @param resourceName the name of the resource
     * @return an array of available locale names
     */
    public List getAvailableLocaleNames(CmsObject cms, String resourceName) {
    
        String availableNames = null;
        try {
            availableNames = cms.readProperty(resourceName, I_CmsConstants.C_PROPERTY_AVAILABLE_LOCALES, true);
        } catch (CmsException exc) {
            //noop
        }
        
        List result = null;
        if (availableNames != null) {
            result = checkLocaleNames(splitNames(availableNames));            
        } 
        if ((result == null) || (result.size() == 0)) {
            return m_availableLocaleNames;
        } else {
            return result;
        }        
    }
    
    /**
     * Returns an array of default locale names for the given resource.<p>
     * 
     * @param cms the current cms permission object
     * @param resourceName the name of the resource
     * @return an array of default locale names
     */    
    public List getDefaultLocaleNames(CmsObject cms, String resourceName) {
        
        String defaultNames = null;
        try {
            defaultNames = cms.readProperty(resourceName, I_CmsConstants.C_PROPERTY_LOCALE, true);
        } catch (CmsException exc) {
            //noop
        }        
        
        List result = null;
        if (defaultNames != null) {
            result = checkLocaleNames(splitNames(defaultNames));            
        } 
        if ((result == null) || (result.size() == 0)) {
            return m_defaultLocaleNames;
        } else {
            return result;
        }
    }
        
    /**
     * Returns an array of locale names from a comma-separated string of locale names.<p>
     * All names are filtered against the generally available locale names.
     * 
     * @param names a comma-separated string of locale names
     * @return an array derived from the given locale names
     */
    public List getLocaleNames(String names) {
        return checkLocaleNames(splitNames(names));
    }

    /**
     * Returns a list of available locale names derived from the given locale names.<p>
     * 
     * Each name in the given list is checked against the internal hash map of allowed locales, 
     * and is appended to the resulting list only if the locale exists.<p>
     * 
     * @param localeNames array of locale names to check
     * @return list of available locale names derived from the given locale names
     */
    private List checkLocaleNames(List localeNames) {
        if (localeNames == null) {
            return null;
        }        
        List result = new ArrayList();        
        Iterator i = m_availableLocaleNames.iterator();
        while (i.hasNext()) {
            String localeName = (String)i.next();
            if (localeNames.contains(localeName)) {
                result.add(localeName);
            }
        }        
        return result;        
    }
    
    /**
     * Returns an array of locale names from a comma-separated string of locale names.<p>
     * 
     * @param names a comma-separated string of locale names
     * @return an array derived from the given locale names
     */
    private List splitNames(String localeNames) {        
        if (localeNames == null) {
            return null;
        }        
        String result[] = CmsStringSubstitution.split(localeNames, ",");
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i].trim();
        }
        return Arrays.asList(result);
    }
}

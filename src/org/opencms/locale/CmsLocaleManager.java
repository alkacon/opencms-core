/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/locale/Attic/CmsLocaleManager.java,v $
 * Date   : $Date: 2004/01/22 10:39:35 $
 * Version: $Revision: 1.4 $
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
package org.opencms.locale;

import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.util.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.ExtendedProperties;

/**
 * @version $Revision: 1.4 $ $Date: 2004/01/22 10:39:35 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsLocaleManager {
    
    private String[] m_defaultLocaleNames;
    
    private String[] m_availableLocaleNames;

    private HashMap m_availableLocales;
    
    private I_CmsLocaleHandler m_localeHandler;
    
    /**
     * Runtime property name for locale handler
     */
    public static final String C_LOCALE_HANDLER = "class_locale_handler";
    
    /**
     * Initializes the CmsLocaleManager by reading the properties
     * <code>locale.available</code>
     * <code>locale.default</code>
     * in <code>opencms.properties</code>
     *
     * @param configuration the configuration from <code>opencms.properties</code>
     */
    public CmsLocaleManager (ExtendedProperties configuration) {

        // init available locales
        m_availableLocaleNames = configuration.getStringArray("locale.available");
        m_availableLocales = new HashMap();
        for (int i = 0; i < m_availableLocaleNames.length; i++) {
            String localeName[] = Utils.split(m_availableLocaleNames[i], "_");
            Locale locale = new Locale(localeName[0],
                    (localeName.length > 1) ? localeName[1] : "",
                    (localeName.length > 2) ? localeName[2] : ""
            );
            
            m_availableLocales.put(m_availableLocaleNames[i], locale);
            if (localeName.length > 2 && !m_availableLocales.containsKey(localeName[0] + "_" + localeName[1])) {
                m_availableLocales.put(localeName[0] + "_" + localeName[1], locale);
            }
            if (localeName.length > 1 && !m_availableLocales.containsKey(localeName[0])) {
                m_availableLocales.put(localeName[0], locale);
            }
        }
    
        // init default locale names
        m_defaultLocaleNames = configuration.getStringArray("locale.default");
        
        // init locale handler
        m_localeHandler = (I_CmsLocaleHandler)OpenCms.getRuntimeProperty(C_LOCALE_HANDLER);
        if (m_localeHandler == null) {
            m_localeHandler = new CmsDefaultLocaleHandler();
        }
    }

    /**
     * Returns the name of the default locale configured in <code>opencms.properties</code>.<p>
     *
     * The default locale is the first locale of the configured default locales.
     *
     * @return the namer of the default locale
     */
    public String getDefaultLocaleName() {
        return m_defaultLocaleNames[0];
    }

    /**
     * Returns the list of default locale names configured in <code>opencms.properties</code>.<p>
     * 
     * @return the list of default locale names, i.e <code>en, de</code>
     */
    public String[] getDefaultLocaleNames() {
        return m_defaultLocaleNames;
    }
    
    /**
     * Returns the list of available locale names configured in <code>opencms.properties</code>.<p>
     *
     * @return the list of available locale names, i.e. <code>en, de</code>
     */
    public String[] getAvailableLocaleNames() {
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
    
    /*
     * Returns the name of the appropriate locale for a given resource.<p>
     * 
     * 1. The available locale names for the resource are matched against the set of locale names (i.e. the available languages of a page)
     * 2. The requestedLocaleName and the default locale names are filtered against the set calculated in step 1 
     *    and the best matching name is returned
     * 
     * Example:
     * getLocaleName(..., ..., "en_GB", {en_US, de}) ->
     *      with available locales = [de, en_GB, en_US], default locales = [de]
     * 
     * 1. getMatching([de, en_GB, en_US], {en_US, de}) -> {de, en, en_US}
     * 2. getBestMatch(en_GB, [de], {de, en, en_US}) -> en
     * 
     * @param cms the cms object
     * @param resourceName the name of the resource
     * @param requestedLocaleName the name of the requested locale
     * @param localeNames a set of available locale names
     * @return the name of the best-matching locale for this resource
     */
    /*
    public String getLocaleName(CmsObject cms, String resourceName, String requestedLocaleName, Set localeNames) {
    
        // 1: calculate the available locale names by filtering the available locales 
        //    against the given set of locale names
        Set available = getMatchingLocales(getAvailableLocaleNames(cms, resourceName), localeNames);      
        
        // 2: get the best match by filtering the requested locale name and the default locale names
        //    against the set calculated in step 1
        return getBestMatchingLocaleName(requestedLocaleName, getDefaultLocaleNames(cms, resourceName), available);
    }
    */
    
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
    public Set getMatchingLocales (String localeNames[], Set filter) {
    
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
     * The best matching name is the first name (eventually shortened) in the given array with the
     * maximum number of parts similar to a (part of a) name in the filter set.
     * 
     * Example:
     * getBestMatch([en_GB, de], {de, en, en_US} -> en
     *      since en_GB <> de = 0, en_GB <> en = 1, en_GB <> en_US = 1, de <> de = 1, de <> en = 0, de <> en_US = 0 
     * 
     * @param requestedLocaleName the originally requested locale name
     * @param localeNames an array of locale names
     * @param filter a set of locale names
     * @return the best matching locale name or null
     */
    public String getBestMatchingLocaleName (String requestedLocaleName, String localeNames[], Set filter) {
        
        String result = null;
        
        if (filter == null) {
            return null;
        }
        
        StringBuffer matching = new StringBuffer();
        int max = -1;
        for (int i = -1; i < localeNames.length; i++) {
            String localeName = (i < 0) ? requestedLocaleName : localeNames[i];
            if (localeName != null) {
                for (Iterator j = filter.iterator(); j.hasNext();) {
                    int m = match (localeName, (String)j.next(), matching);
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
    private int match (String localeName1, String localeName2, StringBuffer matching) {
        
        String name1[] = Utils.split(localeName1, "_");
        String name2[] = Utils.split(localeName2, "_");
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
        String name[] = Utils.split(fullName, "_");
        Locale l;
        
        l = (Locale)m_availableLocales.get(fullName);
        if (l != null) {
            return l;
        }
        
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
     * @param cms the cms object
     * @param resourceName the name of the resource
     * @return an array of available locale names
     */
    public String[] getAvailableLocaleNames(CmsObject cms, String resourceName) {
    
        String availableNames = null;
        try {
            availableNames = cms.readProperty(resourceName, I_CmsConstants.C_PROPERTY_AVAILABLE_LOCALES, true);
        } catch (CmsException exc) {
            //noop
        }
        
        return checkLocaleNames((availableNames != null) ? splitNames(availableNames) : m_availableLocaleNames);
    }
    
    /**
     * Returns an array of default locale names for the given resource.<p>
     * 
     * @param cms the cms object
     * @param resourceName the name of the resource
     * @return an array of default locale names
     */
    public String[] getDefaultLocaleNames(CmsObject cms, String resourceName) {
        
        String defaultNames = null;
        try {
            defaultNames = cms.readProperty(resourceName, I_CmsConstants.C_PROPERTY_LOCALE, true);
        } catch (CmsException exc) {
            //noop
        }        
        
        return checkLocaleNames((defaultNames != null) ? splitNames(defaultNames) : m_defaultLocaleNames);
    }
        
    /**
     * Returns an array of locale names from a comma-separated string of locale names.<p>
     * All names are filtered against the generally available locale names.
     * 
     * @param names a comma-separated string of locale names
     * @return an array derived from the given locale names
     */
    public String[] getLocaleNames (String names) {
        return checkLocaleNames(splitNames(names));
    }

    /**
     * Returns an array of available locale names derived from the given locale names.<p>
     * Each name is checked against the internal hash map of locales, 
     * and is appended to the resulting array only if the locale exists.
     * 
     * @param localeNames array of locale names
     * @return array of checked locale names
     */
    private String[] checkLocaleNames(String[] localeNames) {
        
        if (localeNames == null) {
            return null;
        }
        
        String available[] = new String[localeNames.length];
        int length = 0;
        for (int i = 0; i < localeNames.length; i++) {
            if (m_availableLocales.get(localeNames[i]) != null) {
                available[length++] = localeNames[i];
            }
        }
        
        String result[] = new String[length];
        System.arraycopy (available, 0, result, 0, length);
        return result;        
    }
    
    /**
     * Returns an array of locale names from a comma-separated string of locale names.<p>
     * 
     * @param names a comma-separated string of locale names
     * @return an array derived from the given locale names
     */
    private static String[] splitNames(String names) {
        
        if (names == null) {
            return null;
        }
        
        String result[] = Utils.split(names, ",");
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i].trim();
        }
        return result;
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/CmsLocaleManager.java,v $
 * Date   : $Date: 2004/05/13 11:08:31 $
 * Version: $Revision: 1.15 $
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringSubstitution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;

/**
 * Manages the locales configured for this OpenCms installation.<p>
 * 
 * Locale configuration is done in <code>opencms.xml</code>.<p> 
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.15 $
 */
public class CmsLocaleManager implements I_CmsEventListener {
    
    /** Runtime property name for locale handler */
    public static final String C_LOCALE_HANDLER = "class_locale_handler";

    /** The set of available locale names */
    private List m_availableLocales;
    
    /** The default locale, this is the first configured locale */
    private Locale m_defaultLocale;
    
    /** The default locale names (must be a subset of the available locale names) */
    private List m_defaultLocales;    
    
    /** The configured locale handler */
    private I_CmsLocaleHandler m_localeHandler;
    
    /** A cache for accelerated locale lookup, this should never get so large to require a "real" cache */
    private static Map m_localeCache;
    
    /**
     * Initializes a new CmsLocaleManager, called from the configuration.<p>
     */    
    public CmsLocaleManager() {
        m_availableLocales = new ArrayList();
        m_defaultLocales = new ArrayList();
        m_localeHandler = new CmsDefaultLocaleHandler();
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". i18n configuration   : starting");
        }     
        
        LRUMap lruMap = new LRUMap(256);
        m_localeCache = Collections.synchronizedMap(lruMap);
        if (OpenCms.getMemoryMonitor().enabled()) {
            // map must be of type "LRUMap" so that memory monitor can acecss all information
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_localeCache", lruMap);
        }
        
        // register this object as event listener
        OpenCms.addCmsEventListener(this, new int[] {
                I_CmsEventListener.EVENT_CLEAR_CACHES
        });
    }
    
    /**
     * Adds a locale to the list of available locales.<p>
     * 
     * @param localeName the locale to add
     */
    public void addAvailableLocale(String localeName) {
        Locale locale = getLocale(localeName);
        // add full variation (language / country / variant)
        if (! m_availableLocales.contains(locale)) {
            m_availableLocales.add(locale);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". i18n configuration   : adding locale " + locale);
            }         
        }
        // add variation with only language and country
        locale = new Locale(locale.getLanguage(), locale.getCountry());
        if (! m_availableLocales.contains(locale)) {
            m_availableLocales.add(locale);     
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". i18n configuration   : adding locale " + locale);
            }                
        } 
        // add variation with language only
        locale = new Locale(locale.getLanguage());
        if (! m_availableLocales.contains(locale)) {
            m_availableLocales.add(locale);        
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". i18n configuration   : adding locale " + locale);
            }                
        }            
    }

    /**
     * Adds a locale to the list of default locales.<p>
     * 
     * @param localeName the locale to add
     */
    public void addDefaultLocale(String localeName) {
        Locale locale = getLocale(localeName);
        if (! m_defaultLocales.contains(locale)) {
            m_defaultLocales.add(locale);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". i18n configuration   : default locale " + m_defaultLocales.size() + " is " + locale);
            }                
        }
    }

    /**
     * Implements the CmsEvent interface,
     * the locale manager the events to clear 
     * the list of cached keys .<p>
     *
     * @param event CmsEvent that has occurred
     */
    public synchronized void cmsEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                clearCaches();
                break;
            default:
        // no operation
        }
    }
    
    /**
     * Clears the caches in the locale manager.<p>
     */
    private void clearCaches() {

        // flush all caches   
        m_localeCache.clear();

        if (OpenCms.getLog(this).isDebugEnabled()) {
            String eventType = "EVENT_CLEAR_CACHES";
            OpenCms.getLog(this).debug("Locale manager flushed caches after recieving event " + eventType);
        }
    }

    
    
    /**
     * Returns a locale created from the given full name.<p>
     * 
     * The full name must consist of language code, 
     * country code(optional), variant(optional) separated by "_".<p>
     * 
     * This method will always return a valid Locale!
     * If the provided locale name is not valid (i.e. leads to an Exception
     * when trying to create the Locale, then the configured default Locale is returned.<p> 
     * 
     * @param localeName the full locale name
     * @return the locale or <code>null</code> if not available
     */
    public static Locale getLocale(String localeName) {
        if (localeName == null) {
            return OpenCms.getLocaleManager().getDefaultLocale();
        }
        Locale locale;
        synchronized (m_localeCache) {
            locale = (Locale)m_localeCache.get(localeName);
            if (locale == null) {
                try {
                    String localeNames[] = CmsStringSubstitution.split(localeName, "_");
                    locale = new Locale(localeNames[0],
                            (localeNames.length > 1) ? localeNames[1] : "",
                            (localeNames.length > 2) ? localeNames[2] : ""                        
                    );                
                } catch (Throwable t) {
                    OpenCms.getLog(OpenCms.getLocaleManager()).debug("Could not create a Locale out of '" + localeName + "'", t);
                    // map this error to the default locale
                    locale = OpenCms.getLocaleManager().getDefaultLocale();
                }
                m_localeCache.put(localeName, locale);
            }
        }
        return locale;
    }
    
    /**
     * Returns a List of locales from a comma-separated string of locale names.<p>
     * 
     * @param localeNames a comma-separated string of locale names
     * @return a List of locales derived from the given locale names
     */
    public static List getLocales(String localeNames) {        
        if (localeNames == null) {
            return null;
        }
        return getLocales(CmsStringSubstitution.split(localeNames, ","));
    }
    
    /**
     * Returns a List of locales from an array of locale names.<p>
     * 
     * @param localeNames array of locale names
     * @return a List of locales derived from the given locale names
     */
    public static List getLocales(Object[] localeNames) {
        List result = new ArrayList(localeNames.length);
        for (int i = 0; i < localeNames.length; i++) {
            result.add(getLocale(localeNames[i].toString().trim()));
        }
        return result;
    }    
    
    /**
     * Initializes this locale manager with the OpenCms system configuration.<p>
     * 
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     */  
    public void initialize(CmsObject cms) {
        // init the locale handler
        m_localeHandler.initHandler(cms);
        // set default locale 
        m_defaultLocale = (Locale)m_defaultLocales.get(0);
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". i18n configuration   : vfs access initialized");
        }              
    }        

    /**
     * Returns a list of available locale names derived from the given locale names.<p>
     * 
     * Each name in the given list is checked against the internal hash map of allowed locales, 
     * and is appended to the resulting list only if the locale exists.<p>
     * 
     * @param locales List of locales to check
     * @return list of available locales derived from the given locale names
     */
    private List checkLocaleNames(List locales) {
        if (locales == null) {
            return null;
        }        
        List result = new ArrayList();        
        Iterator i = locales.iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            if (m_availableLocales.contains(locale)) {
                result.add(locale);
            }
        }        
        return result;        
    }
    
    /**
     * Returns the list of available locales configured in.<p>
     *
     * @return the list of available locale names, e.g. <code>en, de</code>
     */
    public List getAvailableLocales() {
        return m_availableLocales;
    }

    /**
     * Returns an array of available locale names for the given resource.<p>
     * 
     * @param cms the current cms permission object
     * @param resourceName the name of the resource
     * @return an array of available locale names
     */
    public List getAvailableLocales(CmsObject cms, String resourceName) {
    
        String availableNames = null;
        try {
            availableNames = cms.readPropertyObject(resourceName, I_CmsConstants.C_PROPERTY_AVAILABLE_LOCALES, true).getValue();
        } catch (CmsException exc) {
            // noop
        }
        
        List result = null;
        if (availableNames != null) {
            result = getAvailableLocales(availableNames);            
        } 
        if ((result == null) || (result.size() == 0)) {
            return m_availableLocales;
        } else {
            return result;
        }        
    }
        
    /**
     * Returns a List of available locales from a comma separated string of locale names.<p>
     * 
     * All names are filtered against the allowed available locales 
     * configured in <code>opencms.properties</code>.<P>
     * 
     * @param names a comma-separated String of locale names
     * @return List of locales created from the given locale names
     */
    public List getAvailableLocales(String names) {
        return checkLocaleNames(getLocales(names));
    }
    
    /**
     * Returns the best matching locale from the given locales.<p>
     * 
     * The best matching locale is the first locale (eventually simplified) 
     * in the given list with the maximum number of parts similar to a (part of a) 
     * locale in the filter list.
     * 
     * Example:
     * getBestMatchingLocale([en_GB, de], {de, en, en_US} -> en
     *      since en_GB <> de = 0, en_GB <> en = 1, en_GB <> en_US = 1, de <> de = 1, de <> en = 0, de <> en_US = 0 
     * 
     * @param requestedLocale the originally requested locale
     * @param locales a list of locale names
     * @param filter a list of locale names to use as filter
     * @return the best matching locale name or null if no name matches
     */
    public Locale getBestMatchingLocale(Locale requestedLocale, List locales, Collection filter) {
    
        if ((filter == null) || (locales == null)) {
            return null;
        }
        
        Locale result = null;
               
        int max = -1;
        for (int i = -1; i < locales.size(); i++) {
            Locale locale = (i < 0) ? requestedLocale : (Locale)locales.get(i);
            if (locale != null) {
                for (Iterator j = filter.iterator(); j.hasNext();) {
                    int m = match(locale, (Locale)j.next());
                    if (m > max) {
                        max = m;
                        result = locale;
                    }
                }
            }
        }
        
        if (max < 0) {
            return null;
        }
        
        return new Locale(result.getLanguage(), (max>1)?result.getCountry():"", (max>2)?result.getVariant():"");
    }
    
    /**
     * Returns the first matching locale.<p>
     * 
     * @param locales must be a ascending sourted list of locales in order of preference
     * @param filter the filter to check the locales agains
     * @return the first precise or simplified match
     */
    public Locale getFirstMatchingLocale(List locales, Collection filter) {
        
        Iterator i;
        // first try a precise match
        i = locales.iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            if (filter.contains(locale)) {
                // precise match
                return locale;
            }
        }

        // now try a match only with language and country
        i = locales.iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            locale = new Locale(locale.getLanguage(), locale.getCountry(), "");
            if (filter.contains(locale)) {
                // match
                return locale;
            }
        }
        
        // finally try a match only with language
        i = locales.iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            locale = new Locale(locale.getLanguage(), "", "");
            if (filter.contains(locale)) {
                // match
                return locale;
            }
        }        
        
        // no match
        return null;
    }
    
    /**
     * Returns the default locale configured in <code>opencms.properties</code>.<p>
     *
     * The default locale is the first locale int the list of configured default locales.
     *
     * @return the default locale
     */    
    public Locale getDefaultLocale() {
        return m_defaultLocale;
    }

    /**
     * Returns the list of default locale names configured in <code>opencms.properties</code>.<p>
     * 
     * @return the list of default locale names, e.g. <code>en, de</code>
     */
    public List getDefaultLocales() {
        return m_defaultLocales;
    }
    
    /**
     * Returns an array of default locales for the given resource.<p>
     * 
     * @param cms the current cms permission object
     * @param resourceName the name of the resource
     * @return an array of default locale names
     */    
    public List getDefaultLocales(CmsObject cms, String resourceName) {
        
        String defaultNames = null;
        try {
            defaultNames = cms.readPropertyObject(resourceName, I_CmsConstants.C_PROPERTY_LOCALE, true).getValue();
        } catch (CmsException e) {
            OpenCms.getLog(this).warn("Could not read locale property for resource " + resourceName, e);
        }        
        
        List result = null;
        if (defaultNames != null) {
            result = getAvailableLocales(defaultNames);            
        } 
        if ((result == null) || (result.size() == 0)) {
            return m_defaultLocales;
        } else {
            return result;
        }
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
     * Sets the configured locale handler.<p>
     * 
     * @param localeHandler the locale handler to set
     */
    public void setLocaleHandler(I_CmsLocaleHandler localeHandler) {
        if (localeHandler != null) { 
            m_localeHandler = localeHandler;
        }
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". i18n configuration   : locale handler is " + m_localeHandler.getClass().getName());
        }            
    }
    
    /**
     * Returns the number of parts matching in the given locales.<p>
     * 
     * @param locale1 the first locale 
     * @param locale2 the second locale 
     * @return the number of matching parts (0-3)
     */
    private int match(Locale locale1, Locale locale2) {
        int result = 0;        
        if (!locale1.getLanguage().equals("") && locale1.getLanguage().equals(locale2.getLanguage())) {
            result = 1;
            if (!locale1.getCountry().equals("") && locale1.getCountry().equals(locale2.getCountry())) {
                result = 2;
                if (!locale1.getVariant().equals("") && locale1.getVariant().equals(locale2.getVariant())) {
                    result = 3;
                }
            }
            
        }       
        return result;
    }
}

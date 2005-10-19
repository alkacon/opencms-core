/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/CmsLocaleManager.java,v $
 * Date   : $Date: 2005/10/19 09:40:13 $
 * Version: $Revision: 1.45.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;

/**
 * Manages the locales configured for this OpenCms installation.<p>
 * 
 * Locale configuration is done in <code>opencms.xml</code>.<p> 
 * 
 * @author Carsten Weinholz 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.45.2.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsLocaleManager implements I_CmsEventListener {

    /** Runtime property name for locale handler. */
    public static final String LOCALE_HANDLER = "class_locale_handler";

    /** Request parameter to force encoding selection. */
    public static final String PARAMETER_ENCODING = "__encoding";

    /** Request parameter to force locale selection. */
    public static final String PARAMETER_LOCALE = "__locale";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLocaleManager.class);

    /** The default locale, this is the first configured locale. */
    private static Locale m_defaultLocale;

    /** A cache for accelerated locale lookup, this should never get so large to require a "real" cache. */
    private static Map m_localeCache;

    /** The set of available locale names. */
    private List m_availableLocales;

    /** The default locale names (must be a subset of the available locale names). */
    private List m_defaultLocales;

    /** Indicates if the locale manager is fully initialized. */
    private boolean m_initialized;

    /** The configured locale handler. */
    private I_CmsLocaleHandler m_localeHandler;

    /**
     * Initializes a new CmsLocaleManager, called from the configuration.<p>
     */
    public CmsLocaleManager() {

        setDefaultLocale();
        m_availableLocales = new ArrayList();
        m_defaultLocales = new ArrayList();
        m_localeHandler = new CmsDefaultLocaleHandler();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_I18N_CONFIG_START_0));
        }

        LRUMap lruMap = new LRUMap(256);
        m_localeCache = Collections.synchronizedMap(lruMap);
        CmsMemoryMonitor monitor = OpenCms.getMemoryMonitor();
        if ((monitor != null) && monitor.enabled()) {
            // map must be of type "LRUMap" so that memory monitor can acecss all information
            monitor.register(this.getClass().getName() + ".m_localeCache", lruMap);
        }

        // register this object as event listener
        OpenCms.addCmsEventListener(this, new int[] {I_CmsEventListener.EVENT_CLEAR_CACHES});
    }

    /**
     * Initializes a new CmsLocaleManager, used for OpenCms runlevel 1 (unit tests) only.<p>
     * 
     * @param defaultLocale the default locale to use
     */
    public CmsLocaleManager(Locale defaultLocale) {

        setDefaultLocale();
        m_initialized = false;

        m_availableLocales = new ArrayList();
        m_defaultLocales = new ArrayList();
        m_localeHandler = new CmsDefaultLocaleHandler();
        m_localeCache = Collections.synchronizedMap(new LRUMap());

        m_defaultLocale = defaultLocale;
        m_defaultLocales.add(defaultLocale);
        m_availableLocales.add(defaultLocale);
    }

    /**
     * Required for setting the default locale on the first possible time.<p>
     */
    static {
        setDefaultLocale();
    }

    /**
     * Returns the default locale configured in <code>opencms-system.xml</code>.<p>
     *
     * The default locale is the first locale int the list of configured default locales.
     *
     * @return the default locale
     */
    public static Locale getDefaultLocale() {

        return m_defaultLocale;
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

        if (CmsStringUtil.isEmpty(localeName)) {
            return getDefaultLocale();
        }
        Locale locale;
        synchronized (m_localeCache) {
            locale = (Locale)m_localeCache.get(localeName);
            if (locale == null) {
                try {
                    String[] localeNames = CmsStringUtil.splitAsArray(localeName, '_');
                    locale = new Locale(
                        localeNames[0],
                        (localeNames.length > 1) ? localeNames[1] : "",
                        (localeNames.length > 2) ? localeNames[2] : "");
                } catch (Throwable t) {
                    LOG.debug(Messages.get().key(Messages.LOG_CREATE_LOCALE_FAILED_1, localeName), t);
                    // map this error to the default locale
                    locale = getDefaultLocale();
                }
                m_localeCache.put(localeName, locale);
            }
        }
        return locale;
    }

    /**
     * Returns the locale names from the given List of locales as a comma separated String.<p>
     * 
     * For example, if the input List contains <code>{@link Locale#ENGLISH}</code> and 
     * <code>{@link Locale#GERMANY}</code>, the result will be <code>"en, de_DE"</code>.<p>
     * 
     * An empty String is returned if the input is <code>null</code>, or contains no elements.<p>
     * 
     * @param localeNames the locale names to generate a String from
     * 
     * @return the locale names from the given List of locales as a comma separated String
     */
    public static String getLocaleNames(List localeNames) {

        StringBuffer result = new StringBuffer();
        if (localeNames != null) {
            Iterator i = localeNames.iterator();
            while (i.hasNext()) {
                result.append(i.next().toString());
                if (i.hasNext()) {
                    result.append(", ");
                }
            }
        }
        return result.toString();
    }

    /**
     * Returns a List of locales from an array of locale names.<p>
     * 
     * @param localeNames array of locale names
     * @return a List of locales derived from the given locale names
     */
    public static List getLocales(List localeNames) {

        List result = new ArrayList(localeNames.size());
        for (int i = 0; i < localeNames.size(); i++) {
            result.add(getLocale(localeNames.get(i).toString().trim()));
        }
        return result;
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
        return getLocales(CmsStringUtil.splitAsList(localeNames, ','));
    }

    /**
     * Sets the default locale of the Java VM to <code>{@link Locale#ENGLISH}</code> if the 
     * current default has any other language then English set.<p>
     *
     * This is required because otherwise the default (English) resource bundles 
     * would not be displayed for the English locale if a translated default locale exists.<p>
     * 
     * Here's an example of how this issues shows up:
     * On a German server, the default locale usually is <code>{@link Locale#GERMAN}</code>.
     * All English translations for OpenCms are located in the "default" message files, for example 
     * <code>org.opencms.i18n.message.properties</code>. If the German localization is installed, it will be
     * located in <code>org.opencms.i18n.message_de.properties</code>. If user has English selected
     * as his locale, the default Java lookup mechanism first tries to find 
     * <code>org.opencms.i18n.message_en.properties</code>. However, this file does not exist, since the
     * English localization is kept in the default file. Next, the Java lookup mechanism tries to find the servers
     * default locale, which in this example is German. Since there is a German message file, the Java lookup mechanism
     * is finished and uses this German localization, not the default file. Therefore the 
     * user get the German localization, not the English one.
     * Setting the default locale explicitly to English avoids this issue.<p>
     */
    private static void setDefaultLocale() {

        // set the default locale to english
        // this is required because otherwise the default (english) resource bundles 
        // would not be displayed for the english locale if a translated locale exists

        Locale oldLocale = Locale.getDefault();
        if (!(Locale.ENGLISH.getLanguage().equals(oldLocale.getLanguage()))) {
            // default language is not English
            try {
                Locale.setDefault(Locale.ENGLISH);
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(Messages.get().key(Messages.INIT_I18N_DEFAULT_LOCALE_2, Locale.ENGLISH, oldLocale));
                }
            } catch (Exception e) {
                // any Exception: the locale has not been changed, so there may be issues with the English
                // localization but OpenCms will run in general
                CmsLog.INIT.error(Messages.get().key(
                    Messages.LOG_UNABLE_TO_SET_DEFAULT_LOCALE_2,
                    Locale.ENGLISH,
                    oldLocale), e);
            }
        } else {
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_I18N_KEEPING_DEFAULT_LOCALE_1, oldLocale));
            }
        }

        // initialize the static member with the new default 
        m_defaultLocale = Locale.getDefault();
    }

    /**
     * Adds a locale to the list of available locales.<p>
     * 
     * @param localeName the locale to add
     */
    public void addAvailableLocale(String localeName) {

        Locale locale = getLocale(localeName);
        // add full variation (language / country / variant)
        if (!m_availableLocales.contains(locale)) {
            m_availableLocales.add(locale);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_I18N_CONFIG_ADD_LOCALE_1, locale));
            }
        }
        // add variation with only language and country
        locale = new Locale(locale.getLanguage(), locale.getCountry());
        if (!m_availableLocales.contains(locale)) {
            m_availableLocales.add(locale);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_I18N_CONFIG_ADD_LOCALE_1, locale));
            }
        }
        // add variation with language only
        locale = new Locale(locale.getLanguage());
        if (!m_availableLocales.contains(locale)) {
            m_availableLocales.add(locale);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_I18N_CONFIG_ADD_LOCALE_1, locale));
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
        if (!m_defaultLocales.contains(locale)) {
            m_defaultLocales.add(locale);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().key(
                    Messages.INIT_I18N_CONFIG_DEFAULT_LOCALE_2,
                    new Integer(m_defaultLocales.size()),
                    locale));

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
            availableNames = cms.readPropertyObject(
                resourceName,
                CmsPropertyDefinition.PROPERTY_AVAILABLE_LOCALES,
                true).getValue();
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
     * Tries to find the given requested locale (eventually simplified) in the collection of available locales, 
     * if the requested locale is not found it will return the first match from the given list of default locales.<p>
     * 
     * @param requestedLocale the requested locale, if this (or a simplified version of it) is available it will be returned
     * @param defaults a list of default locales to use in case the requested locale is not available
     * @param available the available locales to find a match in
     * 
     * @return the best matching locale name or null if no name matches
     */
    public Locale getBestMatchingLocale(Locale requestedLocale, List defaults, Collection available) {

        if ((available == null) || available.isEmpty()) {
            // no locales are available at all
            return null;
        }

        // the requested locale is the match we want to find most
        if (available.contains(requestedLocale)) {
            // check if the requested locale is directly available
            return requestedLocale;
        }
        if (requestedLocale.getVariant().length() > 0) {
            // locale has a variant like "en_EN_whatever", try only with language and country 
            Locale check = new Locale(requestedLocale.getLanguage(), requestedLocale.getCountry(), "");
            if (available.contains(check)) {
                return check;
            }
        }
        if (requestedLocale.getCountry().length() > 0) {
            // locale has a country like "en_EN", try only with language
            Locale check = new Locale(requestedLocale.getLanguage(), "", "");
            if (available.contains(check)) {
                return check;
            }
        }

        // available locales do not match the requested locale
        if ((defaults == null) || defaults.isEmpty()) {
            // if we have no default locales we are out of luck
            return null;
        }

        // no match found for the requested locale, return the first match from the default locales
        return getFirstMatchingLocale(defaults, available);
    }

    /**
     * Returns the "best" default locale for the given resource.<p>
     * 
     * @param cms the current cms permission object
     * @param resourceName the name of the resource
     * @return an array of default locale names
     * 
     * @see #getDefaultLocales(CmsObject, String)
     */
    public Locale getDefaultLocale(CmsObject cms, String resourceName) {

        List defaultLocales = getDefaultLocales(cms, resourceName);
        Locale result;
        if (defaultLocales.size() > 0) {
            result = (Locale)defaultLocales.get(0);
        } else {
            result = getDefaultLocale();
        }
        return result;
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
     * Use this method in case you need to get all available default options for a resource,
     * if you just need the "best" default locale for a resource, 
     * use <code>{@link #getDefaultLocale(CmsObject, String)}</code>.<p>
     * 
     * @param cms the current cms permission object
     * @param resourceName the name of the resource
     * @return an array of default locale names
     * 
     * @see #getDefaultLocale(CmsObject, String)
     */
    public List getDefaultLocales(CmsObject cms, String resourceName) {

        String defaultNames = null;
        try {
            defaultNames = cms.readPropertyObject(resourceName, CmsPropertyDefinition.PROPERTY_LOCALE, true).getValue();
        } catch (CmsException e) {
            LOG.warn(Messages.get().key(Messages.ERR_READ_ENCODING_PROP_1, resourceName), e);
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
     * Returns the first matching locale (eventually simplified) from the available locales.<p>
     * 
     * @param locales must be an ascending sorted list of locales in order of preference
     * @param available the available locales to find a match in
     * 
     * @return the first precise or simplified match
     */
    public Locale getFirstMatchingLocale(List locales, Collection available) {

        Iterator i;
        // first try a precise match
        i = locales.iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            if (available.contains(locale)) {
                // precise match
                return locale;
            }
        }

        // now try a match only with language and country
        i = locales.iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            if (locale.getVariant().length() > 0) {
                // the locale has a variant, try to match without the variant
                locale = new Locale(locale.getLanguage(), locale.getCountry(), "");
                if (available.contains(locale)) {
                    // match
                    return locale;
                }
            }
        }

        // finally try a match only with language
        i = locales.iterator();
        while (i.hasNext()) {
            Locale locale = (Locale)i.next();
            if (locale.getCountry().length() > 0) {
                // the locale has a country, try to match without the country
                locale = new Locale(locale.getLanguage(), "", "");
                if (available.contains(locale)) {
                    // match
                    return locale;
                }
            }
        }

        // no match
        return null;
    }

    /**
     * Returns the the appropriate locale/encoding for a request,
     * using the "right" locale handler for the given resource.<p>
     * 
     * Certain system folders (like the Workplace) require a special
     * locale handler different from the configured handler.
     * Use this method if you want to resolve locales exactly like 
     * the system does for a request.<p>
     * 
     * @param req the current http request
     * @param user the current user
     * @param project the current project
     * @param resource the URI of the requested resource (with full site root added)
     * 
     * @return the i18n information to use for the given request context
     */
    public CmsI18nInfo getI18nInfo(HttpServletRequest req, CmsUser user, CmsProject project, String resource) {

        CmsI18nInfo i18nInfo = null;

        // check if this is a request against a Workplace folder
        if (OpenCms.getSiteManager().isWorkplaceRequest(req)) {
            // The list of configured localized workplace folders
            List wpLocalizedFolders = OpenCms.getWorkplaceManager().getLocalizedFolders();
            for (int i = wpLocalizedFolders.size() - 1; i >= 0; i--) {
                if (resource.startsWith((String)wpLocalizedFolders.get(i))) {
                    // use the workplace locale handler for this resource
                    i18nInfo = OpenCms.getWorkplaceManager().getI18nInfo(req, user, project, resource);
                    break;
                }
            }
        }
        if (i18nInfo == null) {
            // use default locale handler
            i18nInfo = m_localeHandler.getI18nInfo(req, user, project, resource);
        }

        // check the request for special parameters overriding the locale handler
        Locale locale = null;
        String encoding = null;
        if (req != null) {
            String localeParam;
            // check request for parameters
            if ((localeParam = req.getParameter(CmsLocaleManager.PARAMETER_LOCALE)) != null) {
                // "__locale" parameter found in request
                locale = CmsLocaleManager.getLocale(localeParam);
            }
            // check for "__encoding" parameter in request
            encoding = req.getParameter(CmsLocaleManager.PARAMETER_ENCODING);
        }

        // merge values from request with values from locale handler
        if (locale == null) {
            locale = i18nInfo.getLocale();
        }
        if (encoding == null) {
            encoding = i18nInfo.getEncoding();
        }

        // still some values might be "null"
        if (locale == null) {
            locale = getDefaultLocale();
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_LOCALE_NOT_FOUND_1, locale));
            }
        }
        if (encoding == null) {
            encoding = OpenCms.getSystemInfo().getDefaultEncoding();
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_ENCODING_NOT_FOUND_1, encoding));
            }
        }

        // return the merged values
        return new CmsI18nInfo(locale, encoding);
    }

    /**
     * Returns the configured locale handler.<p>
     * This handler is used to derive the appropriate locale/encoding for a request.
     * 
     * @return the locale handler
     */
    public I_CmsLocaleHandler getLocaleHandler() {

        return m_localeHandler;
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
        // set initialized status
        m_initialized = true;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_I18N_CONFIG_VFSACCESS_0));
        }
    }

    /**
     * Returns <code>true</code> if this locale manager is fully initialized.<p>
     *
     * This is required to prevent errors during unit tests,
     * simple unit tests will usually not have a fully
     * initialized locale manager available.<p>
     *
     * @return true if the locale manager is fully initialized
     */
    public boolean isInitialized() {

        return m_initialized;
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
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(
                Messages.INIT_I18N_CONFIG_LOC_HANDLER_1,
                m_localeHandler.getClass().getName()));
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
     * Clears the caches in the locale manager.<p>
     */
    private void clearCaches() {

        // flush all caches   
        m_localeCache.clear();

        if (LOG.isDebugEnabled()) {
            String eventType = "EVENT_CLEAR_CACHES";
            LOG.debug(Messages.get().key(Messages.LOG_LOCALE_MANAGER_FLUSH_CACHE_1, eventType));
        }
    }
}
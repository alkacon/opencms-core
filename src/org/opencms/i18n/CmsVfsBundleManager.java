/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Manages message bundles loaded from the VFS.<p>
 */
public class CmsVfsBundleManager {

    /**
     * Event listener class which updates the cache based on publish events.
     */
    public class Listener implements I_CmsEventListener {

        /**
         * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
         */
        public void cmsEvent(CmsEvent event) {

            if (event.getType() == I_CmsEventListener.EVENT_PUBLISH_PROJECT) {
                try {
                    reload();
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Data holder for a base name and locale of a message bundle.<p>
     */
    public class NameAndLocale {

        /** The locale. */
        private Locale m_locale;

        /** The base name. */
        private String m_name;

        /**
         * Creates a new instance.<p>
         * 
         * @param name the base name 
         * @param locale the locale 
         */
        public NameAndLocale(String name, Locale locale) {

            m_name = name;
            m_locale = locale;
        }

        /**
         * Gets the locale.<p>
         * 
         * @return the locale 
         */
        public Locale getLocale() {

            return m_locale;
        }

        /**
         * Gets the base name.<p>
         * 
         * @return the base name 
         */
        public String getName() {

            return m_name;
        }
    }

    /** Resource type name for plain-text properties files containing messages. */
    public static final String TYPE_PROPERTIES_BUNDLE = "propertyvfsbundle";

    /** Resource type name for XML contents containing messages. */
    public static final String TYPE_XML_BUNDLE = "xmlvfsbundle";

    /** The logger instance for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsVfsBundleManager.class);

    /** The set of bundle base names. */
    private Set<String> m_bundleBaseNames;

    /** The CMS context to use. */
    private CmsObject m_cms;

    /** The event listener used by this class. */
    private Listener m_eventListener = new Listener();

    /**
     * Creates a new instance.<p>
     *  
     * @param cms the CMS  context to use 
     */
    public CmsVfsBundleManager(CmsObject cms) {

        m_cms = cms;
        m_bundleBaseNames = new HashSet<String>();
        CmsVfsResourceBundle.setCmsObject(cms);
        OpenCms.getEventManager().addCmsEventListener(
            new Listener(),
            new int[] {I_CmsEventListener.EVENT_PUBLISH_PROJECT});
    }

    /**
     * Collects all locales possibly used in the system.<p>
     * 
     * @return the collection of all locales 
     */
    private static Collection<Locale> getAllLocales() {

        Set<Locale> result = new HashSet<Locale>();
        result.addAll(OpenCms.getWorkplaceManager().getLocales());
        result.addAll(OpenCms.getLocaleManager().getAvailableLocales());
        return result;
    }

    /**
     * Gets the event listener used by this object.<p>
     * 
     * @return the event listener used by this object 
     */
    public Listener getEventListener() {

        return m_eventListener;
    }

    /**
     * Re-initializes the resource bundles.<p>
     */
    public void reload() {

        flushBundles();
        try {
            int xmlType = OpenCms.getResourceManager().getResourceType(TYPE_XML_BUNDLE).getTypeId();
            List<CmsResource> xmlBundles = m_cms.readResources("/", CmsResourceFilter.ALL.addRequireType(xmlType), true);
            for (CmsResource xmlBundle : xmlBundles) {
                addXmlBundle(xmlBundle);
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        try {
            int propType = OpenCms.getResourceManager().getResourceType(TYPE_PROPERTIES_BUNDLE).getTypeId();
            List<CmsResource> propertyBundles = m_cms.readResources(
                "/",
                CmsResourceFilter.ALL.addRequireType(propType),
                true);
            for (CmsResource propertyBundle : propertyBundles) {
                addPropertyBundle(propertyBundle);
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Internal method for adding a resource bundle to the internal cache.<p>
     * 
     * @param baseName the base name of the resource bundle 
     * @param locale the locale of the resource bundle 
     * @param bundle the resource bundle to add 
     */
    private void addBundle(String baseName, Locale locale, I_CmsResourceBundle bundle) {

        CmsResourceBundleLoader.addBundleToCache(baseName, locale, bundle);
    }

    /**
     * Adds a resource bundle based on a properties file in the VFS.<p>
     * 
     * @param bundleResource the properties file 
     */
    private void addPropertyBundle(CmsResource bundleResource) {

        NameAndLocale nameAndLocale = getNameAndLocale(bundleResource);
        Locale locale = nameAndLocale.getLocale();

        String baseName = nameAndLocale.getName();
        m_bundleBaseNames.add(baseName);
        LOG.info(String.format(
            "Adding property VFS bundle (path=%s, name=%s, locale=%s)",
            bundleResource.getRootPath(),
            baseName,
            "" + locale));
        OpenCms.getLocaleManager();
        Locale paramLocale = locale != null ? locale : CmsLocaleManager.getDefaultLocale();
        CmsVfsBundleParameters params = new CmsVfsBundleParameters(
            nameAndLocale.getName(),
            bundleResource.getRootPath(),
            paramLocale,
            locale == null,
            CmsVfsResourceBundle.TYPE_PROPERTIES);
        CmsVfsResourceBundle bundle = new CmsVfsResourceBundle(params);
        addBundle(baseName, locale, bundle);
    }

    /**
     * Adds an XML based message bundle.<p>
     * 
     * @param xmlBundle the XML content containing the message bundle data 
     */
    private void addXmlBundle(CmsResource xmlBundle) {

        String name = xmlBundle.getName();
        String path = xmlBundle.getRootPath();
        m_bundleBaseNames.add(name);

        LOG.info(String.format("Adding property VFS bundle (path=%s, name=%s)", xmlBundle.getRootPath(), name));
        for (Locale locale : getAllLocales()) {
            CmsVfsBundleParameters params = new CmsVfsBundleParameters(
                name,
                path,
                locale,
                false,
                CmsVfsResourceBundle.TYPE_XML);
            CmsVfsResourceBundle bundle = new CmsVfsResourceBundle(params);
            addBundle(name, locale, bundle);
        }
        CmsVfsBundleParameters params = new CmsVfsBundleParameters(
            name,
            path,
            CmsLocaleManager.getDefaultLocale(),
            true,
            CmsVfsResourceBundle.TYPE_XML);
        CmsVfsResourceBundle defaultLocaleBundle = new CmsVfsResourceBundle(params);
        addBundle(name, null, defaultLocaleBundle);
    }

    /**
     * Clears the internal cache.
     */
    private void flushBundles() {

        for (String baseName : m_bundleBaseNames) {
            CmsResourceBundleLoader.flushBundleCache(baseName, true);
        }
        m_bundleBaseNames.clear();
    }

    /**
     * Extracts the locale and base name from a resource's file name.<p>
     * 
     * @param bundleRes the resource for which to get the base name and locale 
     * @return a bean containing the base name and locale 
     */
    private NameAndLocale getNameAndLocale(CmsResource bundleRes) {

        String fileName = bundleRes.getName();
        if (TYPE_PROPERTIES_BUNDLE.equals(OpenCms.getResourceManager().getResourceType(bundleRes).getTypeName())) {
            String localeSuffix = CmsStringUtil.getLocaleSuffixForName(fileName);
            if (localeSuffix == null) {
                return new NameAndLocale(fileName, null);
            } else {
                String base = fileName.substring(
                    0,
                    fileName.lastIndexOf(localeSuffix) - (1 /* cut off trailing underscore, too*/));
                Locale locale = CmsLocaleManager.getLocale(localeSuffix);
                return new NameAndLocale(base, locale);
            }
        } else {
            return new NameAndLocale(fileName, null);
        }
    }
}

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

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Manages message bundles loaded from the VFS.<p>
 */
public class CmsVfsBundleManager implements I_CmsEventListener {

    /**
     * Data holder for a base name and locale of a message bundle.<p>
     */
    private class NameAndLocale {

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

    /** Indicated if a reload is already scheduled. */
    private boolean m_reloadIsScheduled;

    /** Thread generation counter. */
    private int m_threadCount;

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
            this,
            new int[] {I_CmsEventListener.EVENT_PUBLISH_PROJECT, I_CmsEventListener.EVENT_CLEAR_CACHES});
        // immediately load all bundles for the first time
        reload(true);
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
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        // wrap in try-catch so that errors don't affect other handlers
        try {
            handleEvent(event);
        } catch (Throwable t) {
            LOG.error(t.getLocalizedMessage(), t);
        }
    }

    /**
     * Indicates if a reload thread is currently scheduled.
     *
     * @return <code>true</code> if a reload is currently scheduled
     */
    public boolean isReloadScheduled() {

        return m_reloadIsScheduled;
    }

    /**
     * Re-initializes the resource bundles.<p>
     *
     * @param isStartup true when this is called during startup
     */
    public synchronized void reload(boolean isStartup) {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            List<CmsResource> xmlBundles = Lists.newArrayList();
            List<CmsResource> propertyBundles = Lists.newArrayList();
            try {
                int xmlType = OpenCms.getResourceManager().getResourceType(TYPE_XML_BUNDLE).getTypeId();
                xmlBundles = m_cms.readResources("/", CmsResourceFilter.ALL.addRequireType(xmlType), true);
            } catch (Exception e) {
                logError(e, isStartup);
            }
            try {
                int propType = OpenCms.getResourceManager().getResourceType(TYPE_PROPERTIES_BUNDLE).getTypeId();
                propertyBundles = m_cms.readResources("/", CmsResourceFilter.ALL.addRequireType(propType), true);
            } catch (Exception e) {
                logError(e, isStartup);
            }
            try {
                synchronized (CmsResourceBundleLoader.class) {
                    // Although the methods of CmsResourceBundleLoader which manipulate the cache
                    // are synchronized, we synchronize the whole block to avoid intermediate states
                    // where bundles have been removed from the cache but not re-added again
                    for (String baseName : m_bundleBaseNames) {
                        CmsResourceBundleLoader.flushBundleCache(baseName, true);
                    }
                    m_bundleBaseNames.clear();
                    for (CmsResource xmlBundle : xmlBundles) {
                        addXmlBundle(xmlBundle);
                    }
                    for (CmsResource propertyBundle : propertyBundles) {
                        addPropertyBundle(propertyBundle);
                    }
                    if (OpenCms.getWorkplaceManager() != null) {
                        OpenCms.getWorkplaceManager().flushMessageCache();
                    }
                }
            } catch (Exception e) {
                logError(e, isStartup);
            }
        }
    }

    /**
     * Sets the information if a reload thread is currently scheduled.
     *
     * @param reloadIsScheduled if <code>true</code> there is a reload currently scheduled
     */
    public void setReloadScheduled(boolean reloadIsScheduled) {

        m_reloadIsScheduled = reloadIsScheduled;
    }

    /**
     * Shuts down the VFS bundle manager.<p>
     *
     * This will cause the internal reloading Thread not reload in case it is still running.<p>
     */
    public void shutDown() {

        // we don't want to listen to further events
        OpenCms.getEventManager().removeCmsEventListener(this);
        setReloadScheduled(false);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                org.opencms.staticexport.Messages.get().getBundle().key(
                    org.opencms.staticexport.Messages.INIT_SHUTDOWN_1,
                    this.getClass().getName()));
        }
    }

    /**
     * Logs an exception that occurred.<p>
     *
     * @param e the exception to log
     * @param logToErrorChannel if true erros should be written to the error channel instead of the info channel
     */
    protected void logError(Exception e, boolean logToErrorChannel) {

        if (logToErrorChannel) {
            LOG.error(e.getLocalizedMessage(), e);
        } else {
            LOG.info(e.getLocalizedMessage(), e);
        }
        // if an error was logged make sure that the flag to schedule a reload is reset
        setReloadScheduled(false);
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
        LOG.info(
            String.format(
                "Adding property VFS bundle (path=%s, name=%s, locale=%s)",
                bundleResource.getRootPath(),
                baseName,
                "" + locale));
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

    /**
     * This actually handles the event.<p>
     *
     * @param event the received event
     */
    private void handleEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                //System.out.print(getEventName(event.getType()));
                String publishIdStr = (String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID);
                if (publishIdStr != null) {
                    CmsUUID publishId = new CmsUUID(publishIdStr);
                    try {
                        List<CmsPublishedResource> publishedResources = m_cms.readPublishedResources(publishId);
                        if (publishedResources.isEmpty()) {
                            scheduleReload();
                        } else {
                            String[] typesToMatch = new String[] {TYPE_PROPERTIES_BUNDLE, TYPE_XML_BUNDLE};
                            boolean reload = false;
                            for (CmsPublishedResource res : publishedResources) {
                                for (String typeName : typesToMatch) {
                                    if (OpenCms.getResourceManager().matchResourceType(typeName, res.getType())) {
                                        reload = true;
                                        break;
                                    }
                                }
                            }
                            if (reload) {
                                scheduleReload();
                            }
                        }
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                break;
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
            default:
                scheduleReload();
                break;
        }
    }

    /**
     * Schedules a bundle reload.<p>
     */
    private void scheduleReload() {

        if (!isReloadScheduled() && (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT)) {
            // only schedule a reload if the system is not going down already
            m_threadCount++;
            Thread thread = new Thread("Bundle reload Thread " + m_threadCount) {

                @Override
                public void run() {

                    setReloadScheduled(true);
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        // ignore
                    }
                    if (isReloadScheduled()) {
                        reload(false);
                    }
                    setReloadScheduled(false);
                }
            };
            thread.start();
        }
    }
}

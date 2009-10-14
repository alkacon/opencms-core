/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsADECache.java,v $
 * Date   : $Date: 2009/10/14 14:38:02 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Cache object instance for simultaneously cache online and offline items.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 7.6 
 */
public final class CmsADECache implements I_CmsEventListener {

    /** The log to use (static for performance reasons).<p> */
    private static final Log LOG = CmsLog.getLog(CmsADECache.class);

    /** Cache for ADE recent lists. */
    private Map<String, List<I_CmsContainerElementBean>> m_adeRecentLists;

    /** Cache for ADE search options. */
    private Map<String, CmsSearchOptions> m_adeSearchOptions;

    /** Cache for offline container elements. */
    private Map<String, I_CmsContainerElementBean> m_containerElementsOffline;

    /** Cache for offline container pages. */
    private Map<String, Map<Locale, I_CmsContainerPageBean>> m_containerPagesOffline;

    /** Cache for online container pages. */
    private Map<String, Map<Locale, I_CmsContainerPageBean>> m_containerPagesOnline;

    /**
     * Initializes the cache. Only intended to be called during startup.<p>
     * 
     * @param memMonitor the memory monitor instance
     * @param cacheSettings the system cache settings
     * 
     * @see org.opencms.main.OpenCmsCore#initConfiguration
     */
    public CmsADECache(CmsMemoryMonitor memMonitor, CmsADECacheSettings cacheSettings) {

        Map<String, Map<Locale, I_CmsContainerPageBean>> lruMapCntPage;
        // container page caches
        lruMapCntPage = CmsCollectionsGenericWrapper.createLRUMap(cacheSettings.getContainerPageOfflineSize());
        m_containerPagesOffline = Collections.synchronizedMap(lruMapCntPage);
        memMonitor.register(CmsADECache.class.getName() + ".containerPagesOffline", lruMapCntPage);

        lruMapCntPage = CmsCollectionsGenericWrapper.createLRUMap(cacheSettings.getContainerPageOfflineSize());
        m_containerPagesOnline = Collections.synchronizedMap(lruMapCntPage);
        memMonitor.register(CmsADECache.class.getName() + ".containerPagesOnline", lruMapCntPage);

        // container element caches
        Map<String, I_CmsContainerElementBean> lruMapCntElem = CmsCollectionsGenericWrapper.createLRUMap(cacheSettings.getContainerElementOfflineSize());
        m_containerElementsOffline = Collections.synchronizedMap(lruMapCntElem);
        memMonitor.register(CmsADECache.class.getName() + ".containerElementsOffline", lruMapCntElem);

        // ADE search options
        Map<String, CmsSearchOptions> adeSearchOptions = new HashMap<String, CmsSearchOptions>();
        m_adeSearchOptions = Collections.synchronizedMap(adeSearchOptions);
        memMonitor.register(CmsADEManager.class.getName(), adeSearchOptions);

        // ADE recent lists
        Map<String, List<I_CmsContainerElementBean>> adeRecentList = new HashMap<String, List<I_CmsContainerElementBean>>();
        m_adeRecentLists = Collections.synchronizedMap(adeRecentList);
        memMonitor.register(CmsADEManager.class.getName(), adeRecentList);

        // add this class as an event handler to the cms event listener
        OpenCms.addCmsEventListener(this, new int[] {
            I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
            I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED,
            I_CmsEventListener.EVENT_RESOURCE_MODIFIED,
            I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
            I_CmsEventListener.EVENT_RESOURCE_DELETED,
            I_CmsEventListener.EVENT_PUBLISH_PROJECT,
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES,
            I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES});
    }

    /**
     * Caches the given ADE recent list under the given cache key.<p>
     * 
     * @param key the cache key
     * @param list the recent list to cache
     */
    public void cacheADERecentList(String key, List<I_CmsContainerElementBean> list) {

        m_adeRecentLists.put(key, list);
    }

    /**
     * Caches the given ADE search options under the given cache key.<p>
     * 
     * @param key the cache key
     * @param opts the search options to cache
     */
    public void cacheADESearchOptions(String key, CmsSearchOptions opts) {

        m_adeSearchOptions.put(key, opts);
    }

    /**
     * Caches the given container element under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param containerElement the object to cache
     */
    public void cacheContainerElement(String key, I_CmsContainerElementBean containerElement) {

        m_containerElementsOffline.put(key, containerElement);

    }

    /**
     * Caches the given container page under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param containerPage the object to cache
     * @param online if to cache in online or offline project
     */
    public void cacheContainerPages(String key, Map<Locale, I_CmsContainerPageBean> containerPage, boolean online) {

        if (online) {
            m_containerPagesOnline.put(key, containerPage);
        } else {
            m_containerPagesOffline.put(key, containerPage);
        }
    }

    /**
     * Takes care of cache synchronization and consistency.<p>
     * 
     * @param event the event to handle
     */
    public void cmsEvent(CmsEvent event) {

        CmsResource resource = null;
        List<CmsResource> resources = null;

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED:
            case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                // a resource has been modified in a way that it *IS NOT* necessary also to clear 
                // lists of cached sub-resources where the specified resource might be contained inside.
                // all siblings are removed from the cache, too.
                resource = (CmsResource)event.getData().get(I_CmsEventListener.KEY_RESOURCE);
                uncacheResource(resource);
                break;

            case I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED:
                // a list of resources and all of their properties have been modified
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                uncacheResources(resources);
                break;

            case I_CmsEventListener.EVENT_RESOURCE_DELETED:
            case I_CmsEventListener.EVENT_RESOURCES_MODIFIED:
                // a list of resources has been modified
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                uncacheResources(resources);
                break;

            case I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES:
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                flushContainerPages(true);
                break;

            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                flushContainerPages(true);
                flushContainerPages(false);
                break;

            case I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES:
                flushContainerPages(false);
                break;

            default:
                // noop
                break;
        }
    }

    /**
     * Flushes the ADE recent list cache.<p>
     */
    public void flushADERecentLists() {

        m_adeRecentLists.clear();
    }

    /**
     * Flushes the ADE search options cache.<p>
     */
    public void flushADESearchOptions() {

        m_adeSearchOptions.clear();
    }

    /**
     * Flushes the container elements cache.<p>
     */
    public void flushContainerElements() {

        m_containerElementsOffline.clear();

    }

    /**
     * Flushes the container pages cache.<p>
     * 
     * @param online if to flush the online or offline cache
     */
    public void flushContainerPages(boolean online) {

        if (online) {
            m_containerPagesOnline.clear();
        } else {
            m_containerPagesOffline.clear();
        }
    }

    /**
     * Returns the ADE recent list cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for, this may be the user's uuid
     * 
     * @return the cached recent list with the given cache key
     */
    public List<I_CmsContainerElementBean> getADERecentList(String key) {

        return m_adeRecentLists.get(key);
    }

    /**
     * Returns the ADE search options cached with the given cache key or <code>null</code> if not found.<p>
     * 
     * @param key the cache key to look for, this may be the user's uuid
     * 
     * @return the cached search options with the given cache key
     */
    public CmsSearchOptions getADESearchOptions(String key) {

        return m_adeSearchOptions.get(key);
    }

    /**
     * Returns the cached container page object for the given resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource to look for
     * @param locale the locale
     *  
     * @return the cached container page object
     */
    public I_CmsContainerPageBean getCache(CmsObject cms, CmsResource resource, Locale locale) {

        // get the cached content
        Map<Locale, I_CmsContainerPageBean> containerPageBean = get(cms, resource);
        if (containerPageBean == null) {
            // container page not yet in cache
            try {
                // try to load it
                CmsXmlContainerPage content = CmsXmlContainerPageFactory.unmarshal(cms, cms.readFile(resource));
                setCache(cms, resource, content);
            } catch (CmsException e) {
                // something really bad happened
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_CONTAINER_PAGE_NOT_FOUND_1,
                    cms.getSitePath(resource)), e);
                return null;
            }
            containerPageBean = get(cms, resource);
            if (containerPageBean == null) {
                // container page is still not in cache, should never happen
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_CONTAINER_PAGE_NOT_FOUND_1,
                    cms.getSitePath(resource)));
                return null;
            }
        }
        // get the locale data
        if (!containerPageBean.containsKey(locale)) {
            LOG.warn(Messages.get().container(
                Messages.LOG_CONTAINER_PAGE_LOCALE_NOT_FOUND_2,
                cms.getSitePath(resource),
                locale.toString()).key());
            locale = OpenCms.getLocaleManager().getDefaultLocales(cms, resource).get(0);
            if (!containerPageBean.containsKey(locale)) {
                // locale not found!!
                LOG.error(Messages.get().container(
                    Messages.LOG_CONTAINER_PAGE_LOCALE_NOT_FOUND_2,
                    cms.getSitePath(resource),
                    locale).key());
                return null;
            }
        }
        return containerPageBean.get(locale);
    }

    /**
     * Returns the cached container element under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * 
     * @return  the cached container element or <code>null</code> if not found
     */
    public I_CmsContainerElementBean getCacheContainerElement(String key) {

        return m_containerElementsOffline.get(key);

    }

    /**
     * Returns the cached container page under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param online if cached in online or offline project
     * 
     * @return  the cached container page or <code>null</code> if not found
     */
    public Map<Locale, I_CmsContainerPageBean> getCacheContainerPage(String key, boolean online) {

        if (online) {
            return m_containerPagesOnline.get(key);
        } else {
            return m_containerPagesOffline.get(key);
        }
    }

    /**
     * It first checks if the given container page is already cached, and only if not
     * the container page will be cached.<p>
     * 
     * @param cms the current cms context
     * @param resource the container page itself
     * @param content the xml content of the container page
     * 
     * @throws CmsException if something goes wrong
     */
    public void setCache(CmsObject cms, CmsResource resource, CmsXmlContainerPage content) throws CmsException {

        if (get(cms, resource) == null) {
            set(cms, resource, serialize(cms, content));
        }
    }

    /**
     * Clean up at shutdown time. Only intended to be called at system shutdown.<p>
     * 
     * @see org.opencms.main.OpenCmsCore#shutDown
     */
    public void shutdown() {

        if (OpenCms.getMemoryMonitor() != null) {
            // prevent accidental calls
            return;

        }
        flushContainerPages(true);
        flushContainerPages(false);
        flushContainerElements();
        flushADERecentLists();
        flushADESearchOptions();
    }

    /**
     * Removes the container element identified by the given cache key from the cache.<p>
     * 
     * @param cacheKey the cache key to identify the container element to remove
     */
    public void uncacheContainerElement(String cacheKey) {

        m_containerElementsOffline.remove(cacheKey);

    }

    /**
     * Removes the container page identified by the given cache key from the cache.<p>
     * 
     * @param cacheKey the cache key to identify the container page to remove
     * @param online if online or offline
     */
    public void uncacheContainerPage(String cacheKey, boolean online) {

        if (online) {
            m_containerPagesOnline.remove(cacheKey);
        } else {
            m_containerPagesOffline.remove(cacheKey);
        }
    }

    /**
     * Returns the cached object for the given resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource to look for
     *  
     * @return the cached object
     */
    protected Map<Locale, I_CmsContainerPageBean> get(CmsObject cms, CmsResource resource) {

        if (cms.getRequestContext().currentProject().isOnlineProject()) {
            return lookupOnline(resource.getStructureId().toString());
        } else {
            return lookupOffline(resource.getStructureId().toString());
        }
    }

    /**
     * Lookups for the given key in the offline cache.<p>
     * 
     * @param cacheKey key to lookup
     * 
     * @return the cached object or <code>null</code> if not cached
     */
    protected Map<Locale, I_CmsContainerPageBean> lookupOffline(String cacheKey) {

        Map<Locale, I_CmsContainerPageBean> retValue = getCacheContainerPage(cacheKey, false);
        if (LOG.isDebugEnabled()) {
            if (retValue == null) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_DEBUG_CACHE_MISSED_OFFLINE_1,
                    new Object[] {cacheKey}));

            } else {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_DEBUG_CACHE_MATCHED_OFFLINE_2,
                    new Object[] {cacheKey, retValue}));
            }
        }
        return retValue;
    }

    /**
     * Lookups for the given key in the online cache.<p>
     * 
     * @param cacheKey key to lookup
     * 
     * @return the cached object or <code>null</code> if not cached
     */
    protected Map<Locale, I_CmsContainerPageBean> lookupOnline(String cacheKey) {

        Map<Locale, I_CmsContainerPageBean> retValue = getCacheContainerPage(cacheKey, true);
        if (LOG.isDebugEnabled()) {
            if (retValue == null) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_DEBUG_CACHE_MISSED_ONLINE_1,
                    new Object[] {cacheKey}));

            } else {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_DEBUG_CACHE_MATCHED_ONLINE_2,
                    new Object[] {cacheKey, retValue}));
            }
        }
        return retValue;
    }

    /**
     * Creates a new cachable object from the xml content.<p>
     * 
     * @param cms the cms context
     * @param content the xml content
     * 
     * @return the cachable object for the given content 
     * 
     * @throws CmsException if something goes wrong
     */
    protected Map<Locale, I_CmsContainerPageBean> serialize(CmsObject cms, CmsXmlContent content) throws CmsException {

        Map<Locale, I_CmsContainerPageBean> result = new HashMap<Locale, I_CmsContainerPageBean>();

        // iterate over every locale
        Iterator<Locale> itLocales = content.getLocales().iterator();
        while (itLocales.hasNext()) {
            Locale locale = itLocales.next();

            CmsContainerPageBean cntPage = new CmsContainerPageBean(locale);

            // iterate over every container in the given locale
            Iterator<I_CmsXmlContentValue> itContainers = content.getValues(CmsXmlContainerPage.N_CONTAINER, locale).iterator();
            while (itContainers.hasNext()) {
                I_CmsXmlContentValue container = itContainers.next();
                String containerPath = container.getPath();
                // get the name and type
                String name = content.getValue(
                    CmsXmlUtils.concatXpath(containerPath, CmsXmlContainerPage.N_NAME),
                    locale).getStringValue(cms);
                String type = content.getValue(
                    CmsXmlUtils.concatXpath(containerPath, CmsXmlContainerPage.N_TYPE),
                    locale).getStringValue(cms);

                // HACK: maxElem will be updated later while executing the template
                CmsContainerBean cnt = new CmsContainerBean(name, type, -1);

                // iterate over the container elements
                Iterator<I_CmsXmlContentValue> itElements = content.getValues(
                    CmsXmlUtils.concatXpath(containerPath, CmsXmlContainerPage.N_ELEMENT),
                    locale).iterator();
                while (itElements.hasNext()) {
                    I_CmsXmlContentValue element = itElements.next();
                    String elementPath = element.getPath();
                    // get uri and formatter
                    String elemUri = content.getValue(
                        CmsXmlUtils.concatXpath(elementPath, CmsXmlContainerPage.N_URI),
                        locale).getStringValue(cms);
                    CmsResource elemRes = cms.readResource(elemUri);

                    String formatter = content.getValue(
                        CmsXmlUtils.concatXpath(elementPath, CmsXmlContainerPage.N_FORMATTER),
                        locale).getStringValue(cms);
                    CmsResource formatterRes = cms.readResource(formatter);

                    // get properties
                    HashMap<String, String> properties = new HashMap<String, String>();
                    Iterator<I_CmsXmlContentValue> itProperties = content.getValues(
                        CmsXmlUtils.concatXpath(elementPath, CmsXmlContainerPage.N_PROPERTIES),
                        locale).iterator();
                    while (itProperties.hasNext()) {
                        I_CmsXmlContentValue property = itProperties.next();
                        String propertyPath = property.getPath();
                        String propertyName = content.getValue(
                            CmsXmlUtils.concatXpath(propertyPath, CmsXmlContainerPage.N_NAME),
                            locale).getStringValue(cms);
                        List<I_CmsXmlContentValue> propertyValues = content.getSubValues(CmsXmlUtils.concatXpath(
                            propertyPath,
                            CmsXmlContainerPage.N_VALUE), locale);
                        String propertyValue = null;
                        if (propertyValues.size() >= 1) {
                            propertyValue = propertyValues.get(0).getStringValue(cms);
                        }
                        if (propertyValue != null) {
                            properties.put(propertyName, propertyValue);
                        }
                    }
                    I_CmsContainerElementBean elem = new CmsContainerElementBean(elemRes, formatterRes, properties);
                    cacheContainerElement(elem.getClientId(), elem);
                    // add element to container
                    cnt.addElement(elem);
                }
                // add container to container page
                cntPage.addContainer(cnt);
            }

            // collect containers
            result.put(locale, cntPage);
        }

        return result;
    }

    /**
     * Sets the cached object for the given resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource to set the cache for
     * @param object the object to cache
     */
    protected void set(CmsObject cms, CmsResource resource, Map<Locale, I_CmsContainerPageBean> object) {

        if (cms.getRequestContext().currentProject().isOnlineProject()) {
            setCacheOnline(resource.getStructureId().toString(), object);
        } else {
            setCacheOffline(resource.getStructureId().toString(), object);
        }
    }

    /**
     * Sets a new cached value for the given key in the offline project.<p>
     * 
     * @param cacheKey key to lookup
     * @param data the value to cache
     */
    protected void setCacheOffline(String cacheKey, Map<Locale, I_CmsContainerPageBean> data) {

        cacheContainerPages(cacheKey, data, false);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_DEBUG_CACHE_SET_OFFLINE_2,
                new Object[] {cacheKey, data}));
        }
    }

    /**
     * Sets a new cached value for the given key in the online project.<p>
     * 
     * @param cacheKey key to lookup
     * @param data the value to cache
     */
    protected void setCacheOnline(String cacheKey, Map<Locale, I_CmsContainerPageBean> data) {

        cacheContainerPages(cacheKey, data, true);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_DEBUG_CACHE_SET_ONLINE_2,
                new Object[] {cacheKey, data}));
        }
    }

    /**
     * Removes a cached resource from the cache.<p>
     * 
     * The resource is removed both from the resource and sibling caches.
     * 
     * @param resource the resource
     */
    protected void uncacheResource(CmsResource resource) {

        if (resource == null) {
            LOG.warn(Messages.get().container(Messages.LOG_WARN_UNCACHE_NULL_0));
            return;
        }

        // remove the resource cached by it's structure ID
        uncacheContainerPage(resource.getStructureId().toString(), false);
    }

    /**
     * Removes a bunch of cached resources from the offline cache, but keeps their properties
     * in the cache.<p>
     * 
     * @param resources a list of resources
     * 
     * @see #uncacheResource(CmsResource)
     */
    protected void uncacheResources(List<CmsResource> resources) {

        if (resources == null) {
            LOG.warn(Messages.get().container(Messages.LOG_WARN_UNCACHE_NULL_0));
            return;
        }

        for (int i = 0, n = resources.size(); i < n; i++) {
            // remove the resource
            uncacheResource(resources.get(i));
        }
    }
}

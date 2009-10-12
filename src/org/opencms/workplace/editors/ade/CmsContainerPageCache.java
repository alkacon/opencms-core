/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsContainerPageCache.java,v $
 * Date   : $Date: 2009/10/12 10:14:48 $
 * Version: $Revision: 1.1.2.9 $
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsContainerPageLoader;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

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
 * @version $Revision: 1.1.2.9 $ 
 * 
 * @since 7.6 
 */
public final class CmsContainerPageCache implements I_CmsEventListener {

    /** The log to use (static for performance reasons).<p> */
    private static final Log LOG = CmsLog.getLog(CmsContainerPageCache.class);

    /** The singleton instance. */
    private static CmsContainerPageCache m_instance;

    /** The memory monitor instance. */
    private CmsMemoryMonitor m_cache = OpenCms.getMemoryMonitor();

    /**
     * Default Constructor.<p>
     */
    private CmsContainerPageCache() {

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
     * Returns the singleton instance.<p> 
     * 
     * @return the singleton instance
     */
    public static CmsContainerPageCache getInstance() {

        if (m_instance == null) {
            m_instance = new CmsContainerPageCache();
        }
        return m_instance;
    }

    /**
     * Takes care of cache synchronization and consistency.<p>
     * 
     * @param event the event to handle
     */
    @SuppressWarnings("unchecked")
    public void cmsEvent(CmsEvent event) {

        CmsResource resource = null;
        List<CmsResource> resources = null;

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED:
            case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                // a resource has been modified in a way that it *IS NOT* necessary also to clear 
                // lists of cached sub-resources where the specified resource might be contained inside.
                // all siblings are removed from the cache, too.
                resource = (CmsResource)event.getData().get("resource");
                uncacheResource(resource);
                break;

            case I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED:
                // a list of resources and all of their properties have been modified
                resources = (List<CmsResource>)event.getData().get("resources");
                uncacheResources(resources);
                break;

            case I_CmsEventListener.EVENT_RESOURCE_DELETED:
            case I_CmsEventListener.EVENT_RESOURCES_MODIFIED:
                // a list of resources has been modified
                resources = (List<CmsResource>)event.getData().get("resources");
                uncacheResources(resources);
                break;

            case I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES:
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                m_cache.flushContainerPages(true);
                break;

            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                m_cache.flushContainerPages(true);
                m_cache.flushContainerPages(false);
                break;

            case I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES:
                m_cache.flushContainerPages(false);
                break;

            default:
                // noop
                break;
        }
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
    public CmsContainerPageBean getCache(CmsObject cms, CmsResource resource, Locale locale) {

        // get the cached content
        Map<Locale, CmsContainerPageBean> containerPageBean = get(cms, resource);
        if (containerPageBean == null) {
            // container page not yet in cache
            try {
                // try to load it
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resource));
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
     * It first checks if the given container page is already cached, and only if not
     * the container page will be cached.<p>
     * 
     * @param cms the current cms context
     * @param resource the container page itself
     * @param content the xml content on the container page
     * 
     * @throws CmsException if something goes wrong
     */
    public void setCache(CmsObject cms, CmsResource resource, CmsXmlContent content) throws CmsException {

        if (get(cms, resource) == null) {
            set(cms, resource, serialize(cms, content));
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
    protected Map<Locale, CmsContainerPageBean> get(CmsObject cms, CmsResource resource) {

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
    protected Map<Locale, CmsContainerPageBean> lookupOffline(String cacheKey) {

        Map<Locale, CmsContainerPageBean> retValue = m_cache.getCacheContainerPage(cacheKey, false);
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
    protected Map<Locale, CmsContainerPageBean> lookupOnline(String cacheKey) {

        Map<Locale, CmsContainerPageBean> retValue = m_cache.getCacheContainerPage(cacheKey, true);
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
    protected Map<Locale, CmsContainerPageBean> serialize(CmsObject cms, CmsXmlContent content) throws CmsException {

        Map<Locale, CmsContainerPageBean> result = new HashMap<Locale, CmsContainerPageBean>();

        // iterate over every locale
        Iterator<Locale> itLocales = content.getLocales().iterator();
        while (itLocales.hasNext()) {
            Locale locale = itLocales.next();

            CmsContainerPageBean cntPage = new CmsContainerPageBean(locale);

            // iterate over every container in the given locale
            Iterator<I_CmsXmlContentValue> itContainers = content.getValues(CmsContainerPageLoader.N_CONTAINER, locale).iterator();
            while (itContainers.hasNext()) {
                I_CmsXmlContentValue container = itContainers.next();
                String containerPath = container.getPath();
                // get the name and type
                String name = content.getValue(
                    CmsXmlUtils.concatXpath(containerPath, CmsContainerPageLoader.N_NAME),
                    locale).getStringValue(cms);
                String type = content.getValue(
                    CmsXmlUtils.concatXpath(containerPath, CmsContainerPageLoader.N_TYPE),
                    locale).getStringValue(cms);

                // HACK: maxElem will be updated later while executing the template
                CmsContainerBean cnt = new CmsContainerBean(name, type, -1);

                // iterate over the container elements
                Iterator<I_CmsXmlContentValue> itElements = content.getValues(
                    CmsXmlUtils.concatXpath(containerPath, CmsContainerPageLoader.N_ELEMENT),
                    locale).iterator();
                while (itElements.hasNext()) {
                    I_CmsXmlContentValue element = itElements.next();
                    String elementPath = element.getPath();
                    // get uri and formatter
                    String elemUri = content.getValue(
                        CmsXmlUtils.concatXpath(elementPath, CmsContainerPageLoader.N_URI),
                        locale).getStringValue(cms);
                    CmsResource elemRes = cms.readResource(elemUri);

                    String formatter = content.getValue(
                        CmsXmlUtils.concatXpath(elementPath, CmsContainerPageLoader.N_FORMATTER),
                        locale).getStringValue(cms);
                    CmsResource formatterRes = cms.readResource(formatter);

                    HashMap<String, CmsProperty> properties = new HashMap<String, CmsProperty>();
                    Iterator<I_CmsXmlContentValue> itProperties = content.getValues(
                        CmsXmlUtils.concatXpath(elementPath, CmsContainerPageLoader.N_PROPERTIES),
                        locale).iterator();
                    while (itProperties.hasNext()) {
                        I_CmsXmlContentValue property = itProperties.next();
                        String propertyPath = property.getPath();
                        String propertyName = content.getValue(
                            CmsXmlUtils.concatXpath(propertyPath, CmsContainerPageLoader.N_NAME),
                            locale).getStringValue(cms);
                        List<I_CmsXmlContentValue> propertyValues = content.getSubValues(CmsXmlUtils.concatXpath(
                            propertyPath,
                            CmsContainerPageLoader.N_VALUE), locale);
                        String propertyValue = null;
                        if (propertyValues.size() >= 1) {
                            propertyValue = propertyValues.get(0).getStringValue(cms);
                        }
                        if (propertyValue != null) {
                            properties.put(propertyName, new CmsProperty(propertyName, propertyValue, null));
                        }

                    }
                    CmsContainerElementBean elem = new CmsContainerElementBean(elemRes, formatterRes, properties, cms);
                    m_cache.cacheContainerElement(elem.getClientId(), elem);
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
    protected void set(CmsObject cms, CmsResource resource, Map<Locale, CmsContainerPageBean> object) {

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
    protected void setCacheOffline(String cacheKey, Map<Locale, CmsContainerPageBean> data) {

        m_cache.cacheContainerPages(cacheKey, data, false);
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
    protected void setCacheOnline(String cacheKey, Map<Locale, CmsContainerPageBean> data) {

        m_cache.cacheContainerPages(cacheKey, data, true);
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
        m_cache.uncacheContainerPage(resource.getStructureId().toString(), false);
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

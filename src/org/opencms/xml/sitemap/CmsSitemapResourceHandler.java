/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapResourceHandler.java,v $
 * Date   : $Date: 2009/12/10 10:00:39 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
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

package org.opencms.xml.sitemap;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlSitemap;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsADEManager;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Resource init handler that loads a resource given its sitemap's URI.<p>
 *
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapResourceHandler implements I_CmsResourceInit, I_CmsEventListener {

    /**
     * Entry data. As a result from calling the {@link #getEntry()} method.<p>
     * 
     * @see #getEntry()
     */
    protected class EntryData {

        /** The entry. */
        private CmsSiteEntryBean m_entry;

        /** The properties. */
        private Map<String, String> m_properties;

        /** The sitemap. */
        private CmsXmlSitemap m_sitemap;

        /**
         * Default constructor.<p>
         * 
         * @param entry the entry
         * @param properties the properties
         * @param sitemap the sitemap
         */
        public EntryData(CmsSiteEntryBean entry, Map<String, String> properties, CmsXmlSitemap sitemap) {

            m_entry = entry;
            m_properties = properties;
            m_sitemap = sitemap;
        }

        /**
         * Returns the entry.<p>
         *
         * @return the entry
         */
        public CmsSiteEntryBean getEntry() {

            return m_entry;
        }

        /**
         * Returns the properties.<p>
         *
         * @return the properties
         */
        public Map<String, String> getProperties() {

            return m_properties;
        }

        /**
         * Returns the sitemap.<p>
         *
         * @return the sitemap
         */
        public CmsXmlSitemap getSitemap() {

            return m_sitemap;
        }
    }

    /** Constant property name for sub-sitemap reference. */
    public static final String PROPERTY_SITEMAP = "sitemap";

    /** Request attribute name constant. */
    public static final String SITEMAP_CURRENT_URI = "SITEMAP_CURRENT_URI";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapResourceHandler.class);

    /** The singleton instance. */
    private static CmsSitemapResourceHandler m_instance;

    /** The offline default sitemap properties. */
    private Map<String, String> m_defPropsOffline;

    /** The online default sitemap properties. */
    private Map<String, String> m_defPropsOnline;

    /** Cache for missing offline URIs. */
    private Map<String, Boolean> m_missingUrisOffline;

    /** Cache for missing online URIs. */
    private Map<String, Boolean> m_missingUrisOnline;

    /** Cache for offline search properties. */
    private Map<String, Map<String, String>> m_searchPropsOffline;

    /** Cache for online search properties. */
    private Map<String, Map<String, String>> m_searchPropsOnline;

    /** Cache for offline sitemaps. */
    private Map<String, CmsFile> m_sitemapsOffline;

    /** Cache for online sitemaps. */
    private Map<String, CmsFile> m_sitemapsOnline;

    /** Cache for offline site entries. */
    private Map<String, CmsSiteEntryBean> m_urisOffline;

    /** Cache for online site entries. */
    private Map<String, CmsSiteEntryBean> m_urisOnline;

    /**
     * Constructor, will prevent more than one instantiation.<p>
     */
    public CmsSitemapResourceHandler() {

        if (m_instance != null) {
            throw new CmsIllegalStateException(Messages.get().container(Messages.ERR_SITEMAP_HANDLER_INSTANTIATION_0));
        }
        m_instance = this;
    }

    /**
     * Returns the singleton instance.<p>
     * 
     * @return the singleton instance
     */
    public static CmsSitemapResourceHandler getInstance() {

        if (m_instance == null) {
            LOG.warn(Messages.get().getBundle().key(
                Messages.LOG_WARN_SITEMAP_HANDLER_NOT_CONFIGURED_1,
                CmsSitemapResourceHandler.class.getName()));
            m_instance = new CmsSitemapResourceHandler();
        }
        return m_instance;
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
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
                flush(true);
                break;

            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                flush(true);
                flush(false);
                break;

            case I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES:
                flush(false);
                break;

            default:
                // noop
                break;
        }
    }

    /**
     * Returns the sitemap properties for a given URI, with search.<p> 
     * 
     * @param cms the current cms context
     * @param uri the URI
     * 
     * @return the properties, taken also into account default values, could be <code>null</code> if URI not found
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, String> getSearchProperties(CmsObject cms, String uri) throws CmsException {

        String path = normalizePath(cms.getRequestContext().addSiteRoot(uri));

        // check the cache
        Map<String, String> properties = null;
        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        if (online) {
            properties = m_searchPropsOnline.get(path);
        } else {
            properties = m_searchPropsOffline.get(path);
        }
        if (properties != null) {
            // found in cache
            return properties;
        }

        // check the missed cache
        Boolean missing = null;
        if (online) {
            missing = m_missingUrisOnline.get(path);
        } else {
            missing = m_missingUrisOffline.get(path);
        }
        if (missing != null) {
            // already marked as not found
            return null;
        }

        // try to find the sitemap entry with its properties
        EntryData data = getEntry(cms, path, online, true);
        if (data == null) {
            // cache the missed attempt
            if (online) {
                m_missingUrisOnline.put(path, Boolean.TRUE);
            } else {
                m_missingUrisOffline.put(path, Boolean.TRUE);
            }
            return null;
        }

        // merge default properties
        properties = new HashMap<String, String>();
        properties.putAll(getDefaultProperties(cms, data.getSitemap().getFile(), online));
        properties.putAll(data.getProperties());

        // cache the found properties
        if (online) {
            m_searchPropsOnline.put(path, properties);
        } else {
            m_searchPropsOffline.put(path, properties);
        }

        return properties;
    }

    /**
     * Returns the site entry for the given URI, or <code>null</code> if not found.<p> 
     * 
     * @param cms the current CMS context
     * @param uri the URI to look for
     * 
     * @return the site entry for the given URI, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsSiteEntryBean getUri(CmsObject cms, String uri) throws CmsException {

        String path = normalizePath(cms.getRequestContext().addSiteRoot(uri));

        // check the cache
        CmsSiteEntryBean uriEntry = null;
        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        if (online) {
            uriEntry = m_urisOnline.get(path);
        } else {
            uriEntry = m_urisOffline.get(path);
        }
        if (uriEntry != null) {
            // found in cache
            return uriEntry;
        }

        // check the missed cache
        Boolean missing = null;
        if (online) {
            missing = m_missingUrisOnline.get(path);
        } else {
            missing = m_missingUrisOffline.get(path);
        }
        if (missing != null) {
            // already marked as not found
            return null;
        }

        // get it
        String extension = uri.substring(uri.length() - CmsFileUtil.getExtension(uri).length());
        String uriNoExt = uri.substring(0, uri.length() - extension.length());
        EntryData data = getEntry(cms, uriNoExt, online, false);

        // match the extension 
        if ((data != null) && (extension.length() > 0)) {
            if (!data.getEntry().getExtension().equals(extension.substring(1))) {
                data = null;
            }
        }

        if (data == null) {
            // cache the missed attempt
            if (online) {
                m_missingUrisOnline.put(path, Boolean.TRUE);
            } else {
                m_missingUrisOffline.put(path, Boolean.TRUE);
            }
            return null;
        }

        // cache the found entry
        uriEntry = data.getEntry();
        if (online) {
            m_urisOnline.put(path, uriEntry);
        } else {
            m_urisOffline.put(path, uriEntry);
        }

        return uriEntry;
    }

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(CmsResource resource, CmsObject cms, HttpServletRequest req, HttpServletResponse res)
    throws CmsResourceInitException {

        // check if the resource was already found
        boolean abort = (resource != null);
        // check if the resource comes from the root site
        abort |= CmsStringUtil.isEmptyOrWhitespaceOnly(cms.getRequestContext().getSiteRoot());
        // check if the resource comes from the /system/ folder
        abort |= cms.getRequestContext().getUri().startsWith("/system/");
        if (abort) {
            // skip in all cases above 
            return resource;
        }

        if (m_missingUrisOffline == null) {
            // TODO: find a better way to initialize
            init();
        }
        // check if the resource is in the site map
        try {
            // find the site map entry
            CmsSiteEntryBean entry = getUri(cms, cms.getRequestContext().getUri());
            if (entry == null) {
                return resource;
            }
            // read the resource
            resource = cms.readResource(entry.getResourceId());
            // set the element
            req.setAttribute(CmsADEManager.ATTR_SITEMAP_ENTRY, entry.cloneWithoutSubEntries());
            // store the requested path 
            req.setAttribute(SITEMAP_CURRENT_URI, cms.getRequestContext().getUri());
            // set the resource path
            cms.getRequestContext().setUri(cms.getSitePath(resource));
        } catch (Throwable e) {
            String uri = cms.getRequestContext().getUri();
            CmsMessageContainer msg = Messages.get().container(Messages.ERR_SITEMAP_1, uri);
            if (LOG.isErrorEnabled()) {
                LOG.error(msg.key(), e);
            }
            throw new CmsResourceInitException(msg, e);
        }
        return resource;
    }

    /**
     * Flushes the caches.<p>
     * 
     * @param online if to flush the online or offline caches
     */
    protected void flush(boolean online) {

        if (online) {
            m_missingUrisOnline.clear();
            m_sitemapsOnline.clear();
            m_urisOnline.clear();
            m_defPropsOnline = null;
            m_searchPropsOnline.clear();
        } else {
            m_missingUrisOffline.clear();
            m_sitemapsOffline.clear();
            m_urisOffline.clear();
            m_defPropsOffline = null;
            m_searchPropsOffline.clear();
        }
    }

    /**
     * Returns the default sitemap properties.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource, should a sitemap
     * @param online if online or offline, the same as in the cms context, but just to not access it again
     * 
     * @return the default sitemap properties
     * 
     * @throws CmsException if something goes wrong
     */
    protected Map<String, String> getDefaultProperties(CmsObject cms, CmsResource resource, boolean online)
    throws CmsException {

        if ((!online && (m_defPropsOffline == null)) || (online && (m_defPropsOnline == null))) {
            Map<String, String> defProps = new HashMap<String, String>();

            Map<String, CmsXmlContentProperty> propertiesConf = OpenCms.getADEManager().getElementPropertyConfiguration(
                cms,
                resource);
            Iterator<Map.Entry<String, CmsXmlContentProperty>> itProperties = propertiesConf.entrySet().iterator();
            while (itProperties.hasNext()) {
                Map.Entry<String, CmsXmlContentProperty> entry = itProperties.next();
                String propertyName = entry.getKey();
                CmsXmlContentProperty conf = entry.getValue();
                CmsMacroResolver.resolveMacros(conf.getWidgetConfiguration(), cms, Messages.get().getBundle());
                defProps.put(propertyName, conf.getDefault());
            }
            if (online) {
                m_defPropsOnline = defProps;
            } else {
                m_defPropsOffline = defProps;
            }
        }
        return online ? m_defPropsOnline : m_defPropsOffline;
    }

    /**
     * Returns the site entry for the given URI, or <code>null</code> if not found.<p> 
     * 
     * @param cms the current CMS context
     * @param uri the URI to look for
     * @param online if online or offline, the same than in the cms context, but just to not access it again
     * @param collectProperties if to collect parent entries properties
     * 
     * @return the site entry for the given URI, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong
     */
    protected EntryData getEntry(CmsObject cms, String uri, boolean online, boolean collectProperties)
    throws CmsException {

        CmsUUID logId = null;
        if (LOG.isDebugEnabled()) {
            logId = new CmsUUID(); // unique id to identify the request
            LOG.debug(Messages.get().container(Messages.LOG_DEBUG_SITEMAP_ENTRY_3, logId, uri, Boolean.valueOf(online)).key());
        }
        // find the root sitemap, at the root folder of the site
        CmsXmlSitemap sitemapXml = getSitemap(cms, "/", online);
        CmsSitemapBean sitemap = sitemapXml.getSitemap(cms, cms.getRequestContext().getLocale());
        if (sitemap == null) {
            return null;
        }
        // property collection
        Map<String, String> properties = new HashMap<String, String>();
        // normalize the uri
        String sitePath = normalizePath(uri);
        LinkedList<String> entryPaths = new LinkedList<String>(CmsStringUtil.splitAsList(sitePath, "/"));

        if (entryPaths.isEmpty()) {
            // special case for '/'
            CmsSiteEntryBean entry = sitemap.getSiteEntries().get(0);
            entry.setPosition(0);
            LOG.debug(Messages.get().container(
                Messages.LOG_DEBUG_SITEMAP_FOUND_3,
                logId,
                new Integer(0),
                entry.getName()).key());
            return new EntryData(entry, properties, sitemapXml);
        }

        String uriPath = normalizePath(cms.getRequestContext().getSiteRoot());
        List<CmsSiteEntryBean> subEntries = sitemap.getSiteEntries().get(0).getSubEntries();
        boolean finished = false;
        while (!finished) {
            String name = entryPaths.removeFirst();
            LOG.debug(Messages.get().container(Messages.LOG_DEBUG_SITEMAP_ENTRY_CHECK_2, logId, uriPath).key());
            uriPath += "/" + name;
            // check the missed cache
            Boolean missing;
            if (online) {
                missing = m_missingUrisOnline.get(uriPath);
            } else {
                missing = m_missingUrisOffline.get(uriPath);
            }
            if (missing != null) {
                // already marked as not found
                LOG.debug(Messages.get().container(Messages.LOG_DEBUG_SITEMAP_ENTRY_MISSING_2, logId, uriPath).key());
                return null;
            }
            List<CmsSiteEntryBean> newSubEntries = null;
            int position = 0;
            for (; position < subEntries.size(); position++) {
                CmsSiteEntryBean entry = subEntries.get(position);
                if (!entry.getName().equals(name)) {
                    // no match
                    LOG.debug(Messages.get().container(
                        Messages.LOG_DEBUG_SITEMAP_NO_MATCH_3,
                        logId,
                        new Integer(position),
                        entry.getName()).key());
                    continue;
                }
                LOG.debug(Messages.get().container(
                    Messages.LOG_DEBUG_SITEMAP_MATCH_3,
                    logId,
                    new Integer(position),
                    entry.getName()).key());
                if (collectProperties) {
                    properties.putAll(entry.getProperties());
                }
                if (entryPaths.isEmpty()) {
                    // if nothing left, we got a match
                    LOG.debug(Messages.get().container(
                        Messages.LOG_DEBUG_SITEMAP_FOUND_3,
                        logId,
                        new Integer(position),
                        entry.getName()).key());
                    entry.setPosition(position);
                    return new EntryData(entry, properties, sitemapXml);
                } else {
                    // continue with sub-entries
                    newSubEntries = entry.getSubEntries();
                    if (newSubEntries.isEmpty()) {
                        // check sitemap property
                        String subSitemapId = entry.getProperties().get(CmsSitemapResourceHandler.PROPERTY_SITEMAP);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(subSitemapId)) {
                            // switch to sub-sitemap
                            CmsResource subSitemapPath = cms.readResource(new CmsUUID(subSitemapId));
                            LOG.debug(Messages.get().container(
                                Messages.LOG_DEBUG_SITEMAP_SUBSITEMAP_2,
                                logId,
                                subSitemapPath).key());
                            sitemapXml = getSitemap(cms, cms.getSitePath(subSitemapPath), online);
                            sitemap = sitemapXml.getSitemap(cms, cms.getRequestContext().getLocale());
                            if (sitemap == null) {
                                // no sitemap found
                                return null;
                            }
                            newSubEntries = sitemap.getSiteEntries();
                        }
                    }
                    finished = newSubEntries.isEmpty();
                    if (finished) {
                        LOG.debug(Messages.get().container(
                            Messages.LOG_DEBUG_SITEMAP_NO_SUBENTRIES_3,
                            logId,
                            new Integer(position),
                            entry.getName()).key());
                    }
                }
                break;
            }
            if (position == subEntries.size()) {
                // not found
                finished = true;
                LOG.debug(Messages.get().container(Messages.LOG_DEBUG_SITEMAP_NOT_FOUND_2, logId, uriPath).key());
            }
            subEntries = newSubEntries;
        }

        return null;
    }

    /**
     * Returns the sitemap for the given path.<p> 
     * 
     * @param cms the current cms context
     * @param path the sitemap path, or the site root
     * @param online if online or offline, the same as in the cms context, but just to not access it again
     * 
     * @return the sitemap bean for the given path
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsXmlSitemap getSitemap(CmsObject cms, String path, boolean online) throws CmsException {

        CmsFile sitemapFile;
        // check the cache
        // HACK: we are caching here sitemaps by root path, as well as property reference in case path = "/" 
        String cacheKey = normalizePath(cms.getRequestContext().addSiteRoot(path));
        if (online) {
            sitemapFile = m_sitemapsOnline.get(cacheKey);
        } else {
            sitemapFile = m_sitemapsOffline.get(cacheKey);
        }
        if (sitemapFile != null) {
            // found in cache
            return CmsXmlSitemapFactory.unmarshal(cms, sitemapFile);
        }

        // not found in cache
        String sitemapPath = path;
        if (path.equals("/")) {
            // read the sitemap property from the site folder
            sitemapPath = cms.readPropertyObject("/", CmsPropertyDefinition.PROPERTY_SITEMAP, false).getValue(
                "/sitemap");
            String cacheKey2 = normalizePath(cms.getRequestContext().addSiteRoot(sitemapPath));
            if (!cacheKey.equals(cacheKey2)) {
                // recursive call with the correct path
                CmsXmlSitemap sitemap = getSitemap(cms, sitemapPath, online);
                if (sitemap != null) {
                    // found: cache the sitemap
                    if (online) {
                        m_sitemapsOnline.put(cacheKey2, m_sitemapsOnline.get(cacheKey));
                    } else {
                        m_sitemapsOffline.put(cacheKey2, m_sitemapsOffline.get(cacheKey));
                    }
                }
                return sitemap;
            }
        }

        try {
            // try to read the sitemap file
            sitemapFile = cms.readFile(sitemapPath);
        } catch (Exception e) {
            // can happen in case of bad configuration
            LOG.error(e.getLocalizedMessage(), e);
            // no sitemap found
            return null;
        }

        // found: cache the sitemap
        if (online) {
            m_sitemapsOnline.put(cacheKey, sitemapFile);
        } else {
            m_sitemapsOffline.put(cacheKey, sitemapFile);
        }
        return CmsXmlSitemapFactory.unmarshal(cms, sitemapFile);
    }

    /**
     * Initialization routine.<p>
     */
    protected void init() {

        // TODO: make the cache sizes configurable
        CmsMemoryMonitor memMonitor = OpenCms.getMemoryMonitor();
        Map<String, CmsFile> lruMapCntPage = CmsCollectionsGenericWrapper.createLRUMap(64);
        m_sitemapsOffline = Collections.synchronizedMap(lruMapCntPage);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".sitemapsOffline", lruMapCntPage);
        }

        lruMapCntPage = CmsCollectionsGenericWrapper.createLRUMap(64);
        m_sitemapsOnline = Collections.synchronizedMap(lruMapCntPage);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".sitemapsOnline", lruMapCntPage);
        }

        Map<String, CmsSiteEntryBean> lruMapUri = CmsCollectionsGenericWrapper.createLRUMap(4096);
        m_urisOffline = Collections.synchronizedMap(lruMapUri);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".urisOffline", lruMapUri);
        }

        lruMapUri = CmsCollectionsGenericWrapper.createLRUMap(4096);
        m_urisOnline = Collections.synchronizedMap(lruMapUri);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".urisOnline", lruMapUri);
        }

        Map<String, Boolean> lruMapMissed = CmsCollectionsGenericWrapper.createLRUMap(1024);
        m_missingUrisOffline = Collections.synchronizedMap(lruMapMissed);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".missingUrisOffline", lruMapMissed);
        }

        lruMapMissed = CmsCollectionsGenericWrapper.createLRUMap(1024);
        m_missingUrisOnline = Collections.synchronizedMap(lruMapMissed);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".missingUrisOnline", lruMapMissed);
        }

        Map<String, Map<String, String>> lruMapProperties = CmsCollectionsGenericWrapper.createLRUMap(1024);
        m_searchPropsOffline = Collections.synchronizedMap(lruMapProperties);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".searchPropsOffline", lruMapProperties);
        }

        lruMapProperties = CmsCollectionsGenericWrapper.createLRUMap(1024);
        m_searchPropsOnline = Collections.synchronizedMap(lruMapProperties);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".searchPropsOnline", lruMapProperties);
        }

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
     * Normalizes the given path by removing any leading and trailing slashes.<p>
     * 
     * @param path the path to normalize
     * 
     * @return the normalized path
     */
    protected String normalizePath(String path) {

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Removes a cached resource from the cache.<p>
     * 
     * @param resource the resource
     */
    protected void uncacheResource(CmsResource resource) {

        if (resource == null) {
            LOG.warn(Messages.get().container(Messages.LOG_WARN_UNCACHE_NULL_0));
            return;
        }

        // if sitemap schema changed
        if (resource.getRootPath().equals(CmsResourceTypeXmlSitemap.SCHEMA)) {
            // flush offline default properties 
            m_defPropsOffline = null;
            // flush offline properties 
            m_searchPropsOffline.clear();
            return;
        }

        // this is could be a sitemap file as well as a site root folder, so remove it
        CmsFile file = m_sitemapsOffline.remove(normalizePath(resource.getRootPath()));

        // we care only more if the modified resource is a sitemap
        if (!CmsResourceTypeXmlSitemap.isSitemap(resource)) {
            return;
        }

        // flush all uri's
        m_urisOffline.clear();
        m_missingUrisOffline.clear();
        // flush properties
        m_searchPropsOffline.clear();
        if (file == null) {
            return;
        }

        // this is the case of root sitemaps
        // we already removed the cached sitemap by its root path
        // but know we have also to remove it by the site root path, 
        // which is unknown, so let's iterate an remove all suspicious entries
        Iterator<Map.Entry<String, CmsFile>> i = m_sitemapsOffline.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, CmsFile> e = i.next();
            if (file.equals(e.getValue())) {
                i.remove();
            }
        }
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
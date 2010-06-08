/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapCache.java,v $
 * Date   : $Date: 2010/06/08 07:12:45 $
 * Version: $Revision: 1.15 $
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

package org.opencms.xml.sitemap;

import org.opencms.cache.CmsVfsCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeXmlSitemap;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Cache object instance for simultaneously cache online and offline items.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.15 $ 
 * 
 * @since 7.6 
 */
public final class CmsSitemapCache extends CmsVfsCache {

    /** The log to use (static for performance reasons).<p> */
    private static final Log LOG = CmsLog.getLog(CmsSitemapCache.class);

    /** Cache for active offline sitemap, as localized entry point root path vs sitemap resource root path. */
    private Map<String, String> m_activeOffline;

    /** Cache for active online sitemap, as localized entry point root path vs sitemap resource root path. */
    private Map<String, String> m_activeOnline;

    /** The admin context. */
    private CmsObject m_adminCms;

    /** Offline sitemap by id. */
    private Map<CmsUUID, CmsInternalSitemapEntry> m_byIdOffline;

    /** Online sitemap by id. */
    private Map<CmsUUID, CmsInternalSitemapEntry> m_byIdOnline;

    /** Offline sitemap by path. */
    private Map<String, CmsInternalSitemapEntry> m_byUriOffline;

    /** Online sitemap by path. */
    private Map<String, CmsInternalSitemapEntry> m_byUriOnline;

    /** The offline default sitemap properties. */
    private Map<String, String> m_defPropsOffline;

    /** The online default sitemap properties. */
    private Map<String, String> m_defPropsOnline;

    /** Cache for offline sitemap documents. */
    private Map<String, CmsXmlSitemap> m_documentsOffline;

    /** Cache for online sitemap documents. */
    private Map<String, CmsXmlSitemap> m_documentsOnline;

    /**
     * Initializes the cache. Only intended to be called during startup.<p>
     * 
     * @param adminCms the root admin CMS context for permission independent data retrieval 
     * @param memMonitor the memory monitor instance
     * @param cacheSettings the system cache settings
     * 
     * @see org.opencms.main.OpenCmsCore#initConfiguration
     */
    public CmsSitemapCache(CmsObject adminCms, CmsMemoryMonitor memMonitor, CmsSitemapCacheSettings cacheSettings) {

        m_adminCms = adminCms;
        m_adminCms.getRequestContext().setSiteRoot("");

        initCaches(memMonitor, cacheSettings);
        registerEventListener();
    }

    /**
     * Returns the active sitemap lookup table.<p>
     * 
     * This method is synchronized since it does not make any sense 
     * to concurrently initialize the look up table.<p>
     * 
     * @param cms the current CMS context
     * 
     * @return the active sitemap table, as localized entry point root path vs sitemap resource root path
     * 
     * @throws CmsException if something goes wrong
     */
    public synchronized Map<String, String> getActiveSitemaps(CmsObject cms) throws CmsException {

        CmsObject adminCms = OpenCms.initCmsObject(m_adminCms);
        adminCms.getRequestContext().setCurrentProject(cms.getRequestContext().currentProject());
        // check cache
        boolean online = adminCms.getRequestContext().currentProject().isOnlineProject();
        Map<String, String> active = online ? m_activeOnline : m_activeOffline;
        if (active != null) {
            return active;
        }

        long t = System.currentTimeMillis();

        // not in cache, create
        if (online) {
            m_activeOnline = Collections.synchronizedMap(new HashMap<String, String>());
            OpenCms.getMemoryMonitor().register(
                CmsSitemapCache.class.getName() + ".sitemapActiveOnline",
                m_activeOnline);
        } else {
            m_activeOffline = Collections.synchronizedMap(new HashMap<String, String>());
            OpenCms.getMemoryMonitor().register(
                CmsSitemapCache.class.getName() + ".sitemapActiveOffline",
                m_activeOffline);
        }
        active = online ? m_activeOnline : m_activeOffline;

        // clean up
        if (online) {
            m_byIdOnline.clear();
            m_byUriOnline.clear();
        } else {
            m_byIdOffline.clear();
            m_byUriOffline.clear();
        }

        // iterate sitemap entry points (system wide)
        List<CmsResource> entryPoints = adminCms.readResourcesWithProperty(
            "/",
            CmsPropertyDefinition.PROPERTY_ADE_SITEMAP,
            null,
            CmsResourceFilter.IGNORE_EXPIRATION.addRequireFolder());
        for (CmsResource entryPoint : entryPoints) {
            String sitemapPath = adminCms.readPropertyObject(
                entryPoint,
                CmsPropertyDefinition.PROPERTY_ADE_SITEMAP,
                false).getValue();
            CmsFile sitemapFile;
            try {
                // interpret property value as root path
                sitemapFile = cms.readFile(sitemapPath);
            } catch (CmsVfsResourceNotFoundException e) {
                // interpret property value as site path
                sitemapPath = OpenCms.getSiteManager().getSiteForRootPath(entryPoint.getRootPath()).getSiteRoot()
                    + sitemapPath;
                sitemapFile = cms.readFile(sitemapPath);
            }
            CmsXmlSitemap xmlSitemap = CmsXmlSitemapFactory.unmarshal(adminCms, sitemapFile);
            for (Locale locale : xmlSitemap.getLocales()) {
                // entry point sitemaps can have several locales
                active.put(locale.toString() + entryPoint.getRootPath(), sitemapFile.getRootPath());
                CmsSitemapBean locSitemap = xmlSitemap.getSitemap(adminCms, locale);
                // root sitemaps must have one and only one root entry
                CmsInternalSitemapEntry startEntry = locSitemap.getSiteEntries().get(0);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(startEntry.getName())) {
                    // Root entries of root sitemaps HAVE to have an empty name
                    startEntry.removeName();
                }
                // inherited properties 
                // we can safely use one reference, since CmsSiteEntryBean#setRuntimeInfo(...) will clone it      
                Map<String, String> properties = new HashMap<String, String>();

                // start iterating
                visitEntry(adminCms, active, startEntry, locale, entryPoint.getRootPath(), 0, properties, online);
            }
        }

        LOG.debug(Messages.get().getBundle().key(
            Messages.LOG_DEBUG_CACHE_SITEMAP_2,
            new Boolean(online),
            new Long(System.currentTimeMillis() - t)));

        return active;
    }

    /**
     * Returns the cache key for the given parameters.<p>
     * 
     * @param structureId the sitemap's structure id
     * @param keepEncoding if to keep the encoding while unmarshalling
     * 
     * @return the cache key for the given sitemap and parameters
     */
    public String getCacheKey(CmsUUID structureId, boolean keepEncoding) {

        return structureId.toString() + "_" + keepEncoding;
    }

    /**
     * Returns the default sitemap properties.<p>
     * 
     * @param cms the current cms context
     * 
     * @return the default sitemap properties
     */
    public Map<String, String> getDefaultProperties(CmsObject cms) {

        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        Map<String, String> defProps = online ? m_defPropsOnline : m_defPropsOffline;
        if (defProps != null) {
            return defProps;
        }
        // default properties are not resource dependent, they are system wide, defined in the XSD
        // but we need a resource to get to the content handler so get the first sitemap we can find
        List<CmsResource> sitemaps = new ArrayList<CmsResource>();
        try {
            CmsObject adminCms = OpenCms.initCmsObject(m_adminCms);
            adminCms.getRequestContext().setCurrentProject(cms.getRequestContext().currentProject());
            sitemaps = adminCms.readResources(
                "/",
                CmsResourceFilter.requireType(OpenCms.getSitemapManager().getSitemapTypeId()),
                true);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }
        if (sitemaps.isEmpty()) {
            // can happen if there is no sitemap 
            defProps = new HashMap<String, String>();
        } else {
            CmsResource resource = sitemaps.get(0);
            defProps = CmsXmlContentPropertyHelper.mergeDefaults(cms, resource, Collections.<String, String> emptyMap());
        }
        if (online) {
            m_defPropsOnline = new HashMap<String, String>(defProps);
        } else {
            m_defPropsOffline = new HashMap<String, String>(defProps);
        }
        return defProps;
    }

    /**
     * Returns the cached sitemap under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param online if cached in online or offline project
     * 
     * @return the cached sitemap or <code>null</code> if not found
     */
    public CmsXmlSitemap getDocument(String key, boolean online) {

        CmsXmlSitemap retValue;
        if (online) {
            retValue = m_documentsOnline.get(key);
            if (LOG.isDebugEnabled()) {
                if (retValue == null) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_CACHE_MISSED_ONLINE_1,
                        new Object[] {key}));
                } else {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_CACHE_MATCHED_ONLINE_2,
                        new Object[] {key, retValue}));
                }
            }
        } else {
            retValue = m_documentsOffline.get(key);
            if (LOG.isDebugEnabled()) {
                if (retValue == null) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_CACHE_MISSED_OFFLINE_1,
                        new Object[] {key}));

                } else {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEBUG_CACHE_MATCHED_OFFLINE_2,
                        new Object[] {key, retValue}));
                }
            }
        }
        return retValue;
    }

    /**
     * Returns the sitemap entry for the given id and current project.<p>
     *
     * @param cms the current CMS context
     * @param id the id to look for
     * 
     * @return the sitemap entry, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsInternalSitemapEntry getEntryById(CmsObject cms, CmsUUID id) throws CmsException {

        // ensure sitemap data is cached
        getActiveSitemaps(cms);

        // retrieve data
        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        Map<CmsUUID, CmsInternalSitemapEntry> entries = online ? m_byIdOnline : m_byIdOffline;

        return entries.get(id);
    }

    /**
     * Returns the sitemap entry for the given URI and current project.<p>
     *
     * @param cms the current CMS context
     * @param uri the URI to look for
     * 
     * @return the sitemap entry, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsInternalSitemapEntry getEntryByUri(CmsObject cms, String uri) throws CmsException {

        // ensure sitemap data is cached
        getActiveSitemaps(cms);

        // retrieve data
        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        Map<String, CmsInternalSitemapEntry> entries = online ? m_byUriOnline : m_byUriOffline;

        // adjust path
        String path = cms.getRequestContext().getLocale().toString() + cms.getRequestContext().addSiteRoot(uri);

        return entries.get(path);
    }

    /**
     * Caches the given sitemap under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param sitemap the object to cache
     * @param online if to cache in online or offline project
     */
    public void setDocument(String key, CmsXmlSitemap sitemap, boolean online) {

        Map<String, CmsXmlSitemap> docs = online ? m_documentsOnline : m_documentsOffline;
        if (docs.containsKey(key)) {
            // many false calls due to unmarshal method dependency
            return;
        }
        docs.put(key, sitemap);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                online ? Messages.LOG_DEBUG_CACHE_SET_ONLINE_2 : Messages.LOG_DEBUG_CACHE_SET_OFFLINE_2,
                new Object[] {key, sitemap}));
        }
    }

    /**
     * @see org.opencms.cache.CmsVfsCache#flush(boolean)
     */
    @Override
    protected void flush(boolean online) {

        if (online) {
            m_documentsOnline.clear();
            m_defPropsOnline = null;
            if (m_activeOnline != null) {
                m_activeOnline.clear();
                m_activeOnline = null;
            }
        } else {
            m_documentsOffline.clear();
            m_defPropsOffline = null;
            if (m_activeOffline != null) {
                m_activeOffline.clear();
                m_activeOffline = null;
            }
        }
    }

    /**
     * Returns the subentries, including sub-sitemap lookup.<p>
     * 
     * @param cms the admin CMS context
     * @param locale the current locale
     * @param entry the entry to get the subentries for
     * 
     * @return a list of subentries
     * 
     * @throws CmsException if something goes wrong
     */
    protected List<CmsInternalSitemapEntry> getSubEntries(CmsObject cms, Locale locale, CmsInternalSitemapEntry entry)
    throws CmsException {

        // check sitemap property
        String subSitemapId = entry.getProperties().get(CmsSitemapManager.Property.sitemap.name());
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(subSitemapId)) {
            return entry.getSubEntries();
        }
        // switch to sub-sitemap
        CmsResource subSitemap = cms.readResource(new CmsUUID(subSitemapId));
        CmsXmlSitemap sitemapXml = CmsXmlSitemapFactory.unmarshal(cms, subSitemap);
        CmsSitemapBean sitemap = sitemapXml.getSitemap(cms, locale);
        if (sitemap == null) {
            // be sure the entry has no sub-entries
            entry.setSubEntries(new ArrayList<CmsInternalSitemapEntry>());
            // no sitemap found
            return entry.getSubEntries();
        }
        // set the sub-entries
        entry.setSubEntries(sitemap.getSiteEntries());
        // continue with the sub-sitemap
        return entry.getSubEntries();
    }

    /**
     * @see org.opencms.cache.CmsVfsCache#uncacheResource(org.opencms.file.CmsResource)
     */
    @Override
    protected void uncacheResource(CmsResource resource) {

        if (resource == null) {
            LOG.warn(Messages.get().container(Messages.LOG_WARN_UNCACHE_NULL_0));
            return;
        }

        // if sitemap schema changed
        if (resource.getRootPath().equals(CmsResourceTypeXmlSitemap.SCHEMA)) {
            // flush offline default properties 
            m_defPropsOffline = null;
            return;
        }

        // flush docs
        m_documentsOffline.remove(getCacheKey(resource.getStructureId(), true));
        m_documentsOffline.remove(getCacheKey(resource.getStructureId(), false));

        // we care only more if the modified resource is a sitemap
        if (!CmsResourceTypeXmlSitemap.isSitemap(resource)) {
            return;
        }

        // flush all uri's
        if (m_activeOffline != null) {
            m_activeOffline.clear();
            m_activeOffline = null;
        }
    }

    /**
     * Adds the given entry and all its sub-entries recursively to the cache.<p>
     * 
     * @param cms the admin CMS context
     * @param active the active sitemap lookup table to be filled
     * @param entry the entry itself
     * @param locale the locale to visit
     * @param entryPoint the entry's point root path
     * @param entryPos the entry's position
     * @param properties the inherited properties
     * @param online if online or offline, should be consistent with the current project
     * 
     * @throws CmsException if something goes wrong 
     */
    protected void visitEntry(
        CmsObject cms,
        Map<String, String> active,
        CmsInternalSitemapEntry entry,
        Locale locale,
        String entryPoint,
        int entryPos,
        Map<String, String> properties,
        boolean online) throws CmsException {

        // set runtime data
        String currentEntryPoint = entryPoint;
        entry.setRuntimeInfo(currentEntryPoint, entryPos, properties);

        // cache
        Map<CmsUUID, CmsInternalSitemapEntry> byId = online ? m_byIdOnline : m_byIdOffline;
        Map<String, CmsInternalSitemapEntry> byPath = online ? m_byUriOnline : m_byUriOffline;
        byId.put(entry.getId(), entry);
        byPath.put(locale.toString() + entry.getRootPath(), entry);

        // collect the inherited properties
        properties.putAll(entry.getProperties());

        // get sub-entries
        List<CmsInternalSitemapEntry> subEntries = getSubEntries(cms, locale, entry);
        String sitemapUuid = properties.get(CmsSitemapManager.Property.sitemap.name());
        if (sitemapUuid != null) {
            CmsResource sitemapResource = cms.readResource(new CmsUUID(sitemapUuid));
            // collect sitemap
            active.put(locale.toString() + entry.getRootPath(), sitemapResource.getRootPath());
            // be sure the sub-entries do not inherit the sitemap property
            properties.remove(CmsSitemapManager.Property.sitemap.name());
            // be sure to set the right entry point
            currentEntryPoint = entry.getRootPath();
        }
        int size = subEntries.size();
        for (int position = 0; position < size; position++) {
            // visit sub-entries
            CmsInternalSitemapEntry subEntry = subEntries.get(position);
            visitEntry(cms, active, subEntry, locale, currentEntryPoint, position, properties, online);
        } 
    }

    /**
     * Initializes the caches.<p>
     * 
     * @param memMonitor the memory monitor instance
     * @param cacheSettings the system cache settings
     */
    private void initCaches(CmsMemoryMonitor memMonitor, CmsSitemapCacheSettings cacheSettings) {

        Map<String, CmsXmlSitemap> lruMapDocs = CmsCollectionsGenericWrapper.createLRUMap(cacheSettings.getDocumentOfflineSize());
        m_documentsOffline = Collections.synchronizedMap(lruMapDocs);
        memMonitor.register(CmsSitemapCache.class.getName() + ".sitemapDocsOffline", lruMapDocs);

        lruMapDocs = CmsCollectionsGenericWrapper.createLRUMap(cacheSettings.getDocumentOnlineSize());
        m_documentsOnline = Collections.synchronizedMap(lruMapDocs);
        memMonitor.register(CmsSitemapCache.class.getName() + ".sitemapDocsOnline", lruMapDocs);

        m_byUriOffline = new HashMap<String, CmsInternalSitemapEntry>();
        memMonitor.register(CmsSitemapCache.class.getName() + ".urisOffline", m_byUriOffline);

        m_byUriOnline = new HashMap<String, CmsInternalSitemapEntry>();
        memMonitor.register(CmsSitemapCache.class.getName() + ".urisOnline", m_byUriOnline);

        m_byIdOffline = new HashMap<CmsUUID, CmsInternalSitemapEntry>();
        memMonitor.register(CmsSitemapCache.class.getName() + ".idsOffline", m_byIdOffline);

        m_byIdOnline = new HashMap<CmsUUID, CmsInternalSitemapEntry>();
        memMonitor.register(CmsSitemapCache.class.getName() + ".idsOnline", m_byIdOnline);
    }
}

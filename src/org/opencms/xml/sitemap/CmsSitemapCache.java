/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapCache.java,v $
 * Date   : $Date: 2010/05/12 09:19:10 $
 * Version: $Revision: 1.10 $
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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
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
 * @version $Revision: 1.10 $ 
 * 
 * @since 7.6 
 */
public final class CmsSitemapCache extends CmsVfsCache {

    /** The log to use (static for performance reasons).<p> */
    private static final Log LOG = CmsLog.getLog(CmsSitemapCache.class);

    /** Cache for active offline sitemap. */
    private Map<String, CmsXmlSitemap> m_activeOffline;

    /** Cache for active online sitemap. */
    private Map<String, CmsXmlSitemap> m_activeOnline;

    /** The admin context. */
    private CmsObject m_adminCms;

    /** Offline sitemap by id. */
    private Map<CmsUUID, CmsSitemapEntry> m_byIdOffline;

    /** Online sitemap by id. */
    private Map<CmsUUID, CmsSitemapEntry> m_byIdOnline;

    /** Offline sitemap by path. */
    private Map<String, CmsSitemapEntry> m_byUriOffline;

    /** Online sitemap by path. */
    private Map<String, CmsSitemapEntry> m_byUriOnline;

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
     * This method is synchronized since the cms object is not thread safe,
     * and it does not make any sense anyhow to concurrently initialize the 
     * look up table.<p>
     * 
     * @param cms the current CMS context
     * 
     * @return the active sitemap table 
     * 
     * @throws CmsException if something goes wrong
     */
    public synchronized Map<String, CmsXmlSitemap> getActiveSitemaps(CmsObject cms) throws CmsException {

        CmsObject adminCms = OpenCms.initCmsObject(m_adminCms);
        adminCms.getRequestContext().setCurrentProject(cms.getRequestContext().currentProject());
        // check cache
        boolean online = adminCms.getRequestContext().currentProject().isOnlineProject();
        Map<String, CmsXmlSitemap> active = online ? m_activeOnline : m_activeOffline;
        if (active != null) {
            return active;
        }

        long t = System.currentTimeMillis();

        // not in cache, create
        if (online) {
            m_activeOnline = Collections.synchronizedMap(new HashMap<String, CmsXmlSitemap>());
            OpenCms.getMemoryMonitor().register(
                CmsSitemapCache.class.getName() + ".sitemapActiveOnline",
                m_activeOnline);
        } else {
            m_activeOffline = Collections.synchronizedMap(new HashMap<String, CmsXmlSitemap>());
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
        Map<String, CmsSitemapEntry> byPath = online ? m_byUriOnline : m_byUriOffline;

        // iterate all sitemaps (system wide)
        List<CmsResource> sitemaps = adminCms.readResources("/", CmsResourceFilter.DEFAULT_FILES.addRequireType(
            OpenCms.getSitemapManager().getSitemapTypeId()).addExcludeFlags(CmsResource.FLAG_TEMPFILE));
        for (CmsResource resource : sitemaps) {
            if (CmsResource.isTemporaryFileName(resource.getName())) {
                // skip temporary files
                continue;
            }
            // populate
            CmsXmlSitemap xmlSitemap = CmsXmlSitemapFactory.unmarshal(adminCms, resource);
            Locale entryPointLocale = xmlSitemap.getLocales().get(0); // assume entrypoints are the same across languages
            CmsSitemapBean sitemap = xmlSitemap.getSitemap(adminCms, entryPointLocale);
            active.put(sitemap.getEntryPoint(), xmlSitemap);

            CmsSitemapEntry rootEntry = sitemap.getSiteEntries().get(0);
            String parentPath = CmsResource.getParentFolder(rootEntry.getRootPath());
            if (byPath.containsKey(entryPointLocale.toString() + parentPath)) {
                // clean-up parent node
                for (Locale locale : xmlSitemap.getLocales()) {
                    CmsSitemapEntry parentEntry = byPath.get(locale.toString() + parentPath);
                    parentEntry.setSubEntries(new ArrayList<CmsSitemapEntry>());
                }
                // skip sub-sitemaps
                continue;
            }

            // collect entry data
            for (Locale locale : xmlSitemap.getLocales()) {
                CmsSitemapBean locSitemap = xmlSitemap.getSitemap(adminCms, entryPointLocale);
                CmsSitemapEntry startEntry = locSitemap.getSiteEntries().get(0);
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(startEntry.getName())) {
                    // Root entries of root sitemaps HAVE to have an empty name
                    startEntry.removeName();
                }
                // inherited properties 
                // we can safely use one reference, since CmsSiteEntryBean#setRuntimeInfo(...) will clone it      
                Map<String, String> properties = new HashMap<String, String>();

                // start iterating
                visitEntry(adminCms, locale, startEntry, properties, 0, online);
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
    public CmsSitemapEntry getEntryById(CmsObject cms, CmsUUID id) throws CmsException {

        // ensure sitemap data is cached
        getActiveSitemaps(cms);

        // retrieve data
        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        Map<CmsUUID, CmsSitemapEntry> entries = online ? m_byIdOnline : m_byIdOffline;

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
    public CmsSitemapEntry getEntryByUri(CmsObject cms, String uri) throws CmsException {

        // ensure sitemap data is cached
        getActiveSitemaps(cms);

        // retrieve data
        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        Map<String, CmsSitemapEntry> entries = online ? m_byUriOnline : m_byUriOffline;

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
     * @param entry the entry to get the subentries for
     * 
     * @return a list of subentries
     * 
     * @throws CmsException if something goes wrong
     */
    protected List<CmsSitemapEntry> getSubEntries(CmsObject cms, CmsSitemapEntry entry) throws CmsException {

        // check sitemap property
        String subSitemapId = entry.getProperties().get(CmsSitemapManager.Property.sitemap.name());
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(subSitemapId)) {
            return entry.getSubEntries();
        }
        // switch to sub-sitemap
        CmsResource subSitemap = cms.readResource(new CmsUUID(subSitemapId));
        CmsXmlSitemap sitemapXml = CmsXmlSitemapFactory.unmarshal(cms, subSitemap);
        CmsSitemapBean sitemap = sitemapXml.getSitemap(cms, cms.getRequestContext().getLocale());
        if (sitemap == null) {
            // be sure the entry has no children
            entry.setSubEntries(new ArrayList<CmsSitemapEntry>());
            // no sitemap found
            return entry.getSubEntries();
        }
        List<CmsSitemapEntry> subEntries = sitemap.getSiteEntries();
        // set the children
        entry.setSubEntries(subEntries);
        // continue with the sub-sitemap
        return subEntries;
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
     * Adds the given entry and all its children to the cache.<p>
     * 
     * @param cms the admin cms context
     * @param locale the locale to visit
     * @param entry the entry itself
     * @param properties the inherited properties
     * @param entryPos the entry's position
     * @param online if online or offline, should be consistent with the current project
     * 
     * @throws CmsException if something goes wrong 
     */
    protected void visitEntry(
        CmsObject cms,
        Locale locale,
        CmsSitemapEntry entry,
        Map<String, String> properties,
        int entryPos,
        boolean online) throws CmsException {

        // set runtime data
        entry.setRuntimeInfo(properties, entryPos);

        // cache
        Map<CmsUUID, CmsSitemapEntry> byId = online ? m_byIdOnline : m_byIdOffline;
        Map<String, CmsSitemapEntry> byPath = online ? m_byUriOnline : m_byUriOffline;
        byId.put(entry.getId(), entry);
        byPath.put(locale.toString() + entry.getRootPath(), entry);

        // collect the inherited properties
        properties.putAll(entry.getProperties());

        // get children
        List<CmsSitemapEntry> subEntries = getSubEntries(cms, entry);
        if (properties.get(CmsSitemapManager.Property.sitemap.name()) != null) {
            // be sure the sub-entries do not inherit the sitemap property
            properties.remove(CmsSitemapManager.Property.sitemap.name());
        }
        int size = subEntries.size();
        for (int position = 0; position < size; position++) {
            // visit children
            CmsSitemapEntry child = subEntries.get(position);
            visitEntry(cms, locale, child, properties, position, online);
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

        m_byUriOffline = new HashMap<String, CmsSitemapEntry>();
        memMonitor.register(CmsSitemapCache.class.getName() + ".urisOffline", m_byUriOffline);

        m_byUriOnline = new HashMap<String, CmsSitemapEntry>();
        memMonitor.register(CmsSitemapCache.class.getName() + ".urisOnline", m_byUriOnline);

        m_byIdOffline = new HashMap<CmsUUID, CmsSitemapEntry>();
        memMonitor.register(CmsSitemapCache.class.getName() + ".idsOffline", m_byIdOffline);

        m_byIdOnline = new HashMap<CmsUUID, CmsSitemapEntry>();
        memMonitor.register(CmsSitemapCache.class.getName() + ".idsOnline", m_byIdOnline);
    }
}

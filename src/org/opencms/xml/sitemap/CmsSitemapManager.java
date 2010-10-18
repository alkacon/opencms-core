/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapManager.java,v $
 * Date   : $Date: 2010/10/18 12:19:33 $
 * Version: $Revision: 1.63 $
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

package org.opencms.xml.sitemap;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlSitemap;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsADEDefaultConfiguration;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;
import org.opencms.xml.sitemap.properties.CmsComputedPropertyValue;
import org.opencms.xml.sitemap.properties.CmsPropertyInheritanceState;
import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Sitemap Manager.<p>
 * 
 * Provides all relevant functions for using the sitemap.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.63 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapManager {

    /** Property name constants. */
    public enum Property {

        /** <code>navigation</code> property name. */
        navigation("navigation"),
        /** <code>sitemap</code> property name. */
        sitemap("sitemap"),
        /** <code>template</code> property name. */
        template("template"),
        /** <code>template-inhertited</code> property name. */
        templateInherited("template-inherited");

        /** The name of the property. */
        private final String m_name;

        /**
         * Default constructor.<p>
         * 
         * @param name the name of the property
         */
        private Property(String name) {

            m_name = name;
        }

        /**
         * Returns the name of the property.<p>
         * 
         * @return the name of the property
         */
        public String getName() {

            return m_name;
        }

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {

            return m_name;
        }
    }

    /** Request attribute name constant for the current sitemap entry bean. */
    public static final String ATTR_SITEMAP_ENTRY = "__currentSitemapEntry";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapManager.class);

    /** The internal admin CmsObject. */
    private CmsObject m_adminCms;

    /** The cache instance. */
    private CmsOnlineAndOfflineSitemapCache m_cache;

    /** Cached online project. */
    private CmsProject m_onlineProject;

    /** Lazy initialized sitemap type id. */
    private int m_sitemapTypeId;

    /** The caches for XML sitemaps. */
    private Map<Boolean, CmsSitemapXmlCache> m_sitemapXmlCaches;

    /**
     * Creates a new sitemap manager.<p>
     * 
     * @param adminCms The admin context
     * @param memoryMonitor the memory monitor instance
     * @param systemConfiguration the system configuration
     */
    public CmsSitemapManager(
        CmsObject adminCms,
        CmsMemoryMonitor memoryMonitor,
        CmsSystemConfiguration systemConfiguration) {

        m_adminCms = adminCms;
        // initialize the sitemap cache
        CmsSitemapCacheSettings cacheSettings = systemConfiguration.getSitemapCacheSettings();
        if (cacheSettings == null) {
            cacheSettings = new CmsSitemapCacheSettings();
        }
        CmsVfsMemoryObjectCache structureIdCache = new CmsVfsMemoryObjectCache();
        m_cache = new CmsOnlineAndOfflineSitemapCache(adminCms, memoryMonitor, structureIdCache);
        m_sitemapXmlCaches = new HashMap<Boolean, CmsSitemapXmlCache>();
        CmsSitemapXmlCache onlineXmlCache = new CmsSitemapXmlCache(
            "Online",
            true,
            cacheSettings.getDocumentOnlineSize());
        CmsSitemapXmlCache offlineXmlCache = new CmsSitemapXmlCache(
            "Offline",
            false,
            cacheSettings.getDocumentOfflineSize());
        m_sitemapXmlCaches.put(Boolean.TRUE, onlineXmlCache);
        m_sitemapXmlCaches.put(Boolean.FALSE, offlineXmlCache);

        if (!isSitemapResourceInitConfigured(systemConfiguration)) {
            LOG.warn(Messages.get().getBundle().key(
                Messages.LOG_WARN_SITEMAP_HANDLER_NOT_CONFIGURED_1,
                CmsSitemapResourceHandler.class.getName()));
        }
    }

    /**
     * Creates a dummy root entry for a sub-sitemap from a given sitemap entry.<p>
     * 
     * @param cms the CmsObject to use for VFS operations 
     * @param entry the sitemap entry from which to create the dummy entry 

     * @return a dummy sub-sitemap root
     */
    public static CmsInternalSitemapEntry copyAsSubSitemapRoot(CmsObject cms, CmsInternalSitemapEntry entry) {

        CmsInternalSitemapEntry clone = new CmsInternalSitemapEntry(
            entry.getId(),
            "",
            entry.getStructureId(),
            "",
            entry.getTitle(),
            false,
            entry.getNewProperties(),
            new ArrayList<CmsInternalSitemapEntry>(),
            entry.getContentId());

        clone.setRuntimeInfo(entry.getSitePath(cms), 0, new HashMap<String, CmsComputedPropertyValue>());
        return clone;
    }

    /**
     * Returns the navigation URI from a given request.<p>
     * 
     * @param cms the current CMS context
     * @param request the current request 
     *  
     * @return the current uri for the navigation
     */
    public static String getNavigationUri(CmsObject cms, HttpServletRequest request) {

        CmsSitemapEntry sitemap = OpenCms.getSitemapManager().getRuntimeInfo(request);
        if (sitemap == null) {
            return cms.getRequestContext().getUri();
        }
        return sitemap.getSitePath(cms);
    }

    /**
     * Creates a new element of a given type at the configured location.<p>
     * 
     * @param cms the current opencms context
     * @param sitemapUri the sitemap uri
     * @param request the current request
     * @param type the type of the element to be created
     * 
     * @return the CmsResource representing the newly created element
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource createNewElement(CmsObject cms, String sitemapUri, ServletRequest request, String type)
    throws CmsException {

        // TODO: implement this
        int todo;
        return OpenCms.getADEManager().createNewElement(cms, sitemapUri, request, type);
    }

    /** 
     * Creates a special sitemap cache for checking sitemap links when publishing.<p>
     * 
     * @param name the name for the sitemap cache 
     * @return the created publish sitemap cache
     * 
     */
    public CmsPublishSitemapCache createPublishSitemapCache(String name) {

        return new CmsPublishSitemapCache(m_adminCms, name);
    }

    /**
     * Creates a new empty sitemap from a list of sitemap entries.<p>
     * 
     * @param cms the CmsObject to use for VFS operations
     * @param title the title for the new sitemap
     * @param sitemapUri the URI of the current sitemap
     * @param request the HTTP request
     * 
     * @return the resource representing the new sitemap
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource createSitemap(CmsObject cms, String title, String sitemapUri, ServletRequest request)
    throws CmsException {

        CmsResource newSitemapRes = createNewElement(
            cms,
            sitemapUri,
            request,
            CmsResourceTypeXmlSitemap.getStaticTypeName());
        String sitemapPath = cms.getSitePath(newSitemapRes);
        CmsProperty titleProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, title, title);
        cms.writePropertyObject(sitemapPath, titleProp);
        cms.unlockResource(sitemapPath);
        return newSitemapRes;
    }

    /**
     * Returns the entry points of active sitemaps for the current locale and site.<p>
     * 
     * @param cms the CMS context
     * @return a list of entry points for the active sitemaps 
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<String> getActiveSitemapsForSiteAndLocale(CmsObject cms) throws CmsException {

        List<String> result = new ArrayList<String>();
        Locale locale = cms.getRequestContext().getLocale();
        String site = cms.getRequestContext().getSiteRoot();
        String prefix = CmsStringUtil.joinPaths(locale.toString(), site, "/");
        Map<String, String> activeSitemaps = m_cache.getActiveSitemaps(cms);
        for (Map.Entry<String, String> entry : activeSitemaps.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                result.add(key.substring(prefix.length() - 1));
            }
        }
        return result;
    }

    /**
     * Gets the sitemap entry for a structure id which has a root path closest to a root path passed in as a parameter.<p>
     * 
     * If there is no sitemap entry which references the resource with the given structure id, null will be returned.<p>
     * 
     * @param cms the CMS context 
     * @param structureId the structure id 
     * @param otherRootPath the root path such that the closest sitemap entry to it should be returned 
     * 
     * @return the sitemap entry whose root path is closest to the path passed as a parameter 
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsSitemapEntry getClosestEntryForStructureId(CmsObject cms, CmsUUID structureId, String otherRootPath)
    throws CmsException {

        List<CmsInternalSitemapEntry> entries = getEntriesForStructureId(cms, structureId);
        if (entries.isEmpty()) {
            return null;
        }
        Map<String, CmsInternalSitemapEntry> entriesByString = new HashMap<String, CmsInternalSitemapEntry>();
        for (CmsInternalSitemapEntry entry : entries) {
            entriesByString.put(entry.getRootPath(), entry);
        }
        CmsClosestPathFinder closestPathFinder = new CmsClosestPathFinder();
        closestPathFinder.addPaths(entriesByString.keySet());
        String closestPath = closestPathFinder.getClosestPath(otherRootPath);
        return entriesByString.get(closestPath);
    }

    /**
     * Given the structure id of a resource, this method tries to find the sitemap site path referring to the resource
     * which is most similar to another path passed as a parameter.<p>
     * 
     * If no site path is found, the VFS path will be returned.<p>
     *  
     * @param cms the CMS context 
     * @param structureId the structure id of the resource 
     * @param otherSitePath the site path to which the other site paths should be compared 
     * 
     * @return a sitemap path or a VFS path 
     * 
     * @throws CmsException if something goes wrong 
     */
    public String getClosestSitePathForStructureId(CmsObject cms, CmsUUID structureId, String otherSitePath)
    throws CmsException {

        String otherRootPath = cms.getRequestContext().addSiteRoot(otherSitePath);
        CmsSitemapEntry entry = getClosestEntryForStructureId(cms, structureId, otherRootPath);
        if (entry == null) {
            CmsResource resource = cms.readResource(structureId);
            return cms.getSitePath(resource);
        }
        return entry.getSitePath(cms);

    }

    /**
     * Returns the list of creatable elements.<p>
     * 
     * @param cms the current opencms context
     * @param sitemapUri the sitemap uri
     * @param request the current request
     * 
     * @return the list of creatable elements
     * 
     * @throws CmsException if something goes wrong 
     */
    public Collection<CmsResource> getCreatableElements(CmsObject cms, String sitemapUri, ServletRequest request)
    throws CmsException {

        // TODO: implement this
        int todo;
        return OpenCms.getADEManager().getCreatableElements(cms, sitemapUri, request);
    }

    /**
     * Returns the default sitemap properties.<p>
     * 
     * @param cms the current cms context
     * 
     * @return the default sitemap properties
     */
    public Map<String, String> getDefaultProperties(CmsObject cms) {

        return m_cache.getDefaultProperties(cms);
    }

    /**
     * Returns the default template for the given sitemap, or null if there is none.<p>
     * 
     * @param cms the CmsObject to use for VFS operations
     * @param sitemapUri the URI of the sitemap
     * @param request the servlet request
     * 
     * @return the default template
     *  
     * @throws CmsException if something goes wrong 
     */
    public CmsResource getDefaultTemplate(CmsObject cms, String sitemapUri, ServletRequest request) throws CmsException {

        CmsProperty prop = cms.readPropertyObject(sitemapUri, CmsPropertyDefinition.PROPERTY_TEMPLATE, true);
        String templatePath = prop.getValue();
        try {
            return cms.readResource(templatePath);
        } catch (CmsException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Returns a list of the descendants of a given sitemap entry.<p>
     * 
     * Whether the list includes the entry itself can be controlled by a boolean parameter.<p>
     * 
     * @param rootEntry the root entry whose descendants should be found
     * @param includeRoot if true, the root entry will be included in the resulting list 
     * 
     * @return a list of descendant sitemap entries 
     */
    public List<CmsInternalSitemapEntry> getDescendants(CmsInternalSitemapEntry rootEntry, boolean includeRoot) {

        List<CmsInternalSitemapEntry> result = new ArrayList<CmsInternalSitemapEntry>();
        LinkedList<CmsInternalSitemapEntry> entriesToProcess = new LinkedList<CmsInternalSitemapEntry>();
        if (includeRoot) {
            entriesToProcess.add(rootEntry);

        } else {
            entriesToProcess.addAll(rootEntry.getSubEntries());
        }
        while (!entriesToProcess.isEmpty()) {
            CmsInternalSitemapEntry currentEntry = entriesToProcess.removeFirst();
            result.add(currentEntry);
            entriesToProcess.addAll(currentEntry.getSubEntries());
        }
        return result;
    }

    /**
     * Returns the descendants of a list of sitemap entries.<p>
     * 
     * Whether the resulting list includes the root entries can be controlled with a boolean parameter.<p>
     * 
     * @param rootEntries the root entries whose descendants should be found 
     * @param includeRoots if true, the original root entries will be included in the result list 
     * 
     * @return the list of descendants of the root entries 
     */
    public List<CmsInternalSitemapEntry> getDescendants(List<CmsInternalSitemapEntry> rootEntries, boolean includeRoots) {

        List<CmsInternalSitemapEntry> result = new ArrayList<CmsInternalSitemapEntry>();
        for (CmsInternalSitemapEntry entry : rootEntries) {
            result.addAll(getDescendants(entry, includeRoots));
        }
        return result;
    }

    /**
     * Returns the property configuration for a given resource.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource
     * @param includeNonSchemaProperties if true, the properties defined not in the schema but in a configuration file will also be added to the result 
     * 
     * @return the property configuration
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, CmsXmlContentProperty> getElementPropertyConfiguration(
        CmsObject cms,
        CmsResource resource,
        boolean includeNonSchemaProperties) throws CmsException {

        Map<String, CmsXmlContentProperty> result = new LinkedHashMap<String, CmsXmlContentProperty>();
        Map<String, CmsXmlContentProperty> propertiesFromSchema = CmsXmlContentDefinition.getContentHandlerForResource(
            cms,
            resource).getProperties();
        result.putAll(propertiesFromSchema);
        if (includeNonSchemaProperties) {
            CmsADEDefaultConfiguration conf = new CmsADEDefaultConfiguration();
            List<CmsXmlContentProperty> propertiesFromConfigFile = conf.getProperties(cms, cms.getSitePath(resource));
            for (CmsXmlContentProperty prop : propertiesFromConfigFile) {
                result.put(prop.getPropertyName(), prop);
            }
        }
        return CmsXmlContentPropertyHelper.copyPropertyConfiguration(result);
    }

    /**
     * Returns the sitemap entries which point to the container page at the given VFS root path.<p>
     * 
     * @param cms the current CMS context 
     * @param rootPath a root VFS path 
     * 
     * @return the list of sitemap entries which reference the container page at the given VFS root path 
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<CmsInternalSitemapEntry> getEntriesForRootVfsPath(CmsObject cms, String rootPath) throws CmsException {

        return m_cache.getEntriesByRootVfsPath(cms, rootPath);
    }

    /**
     * Returns the sitemap entries which reference a resource with a given structure id.<p>
     * 
     * @param cms the CMS context 
     * @param structureId the structure id of the resource
     *  
     * @return a list of sitemap entries which reference the resource with the structure id 
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<CmsInternalSitemapEntry> getEntriesForStructureId(CmsObject cms, CmsUUID structureId)
    throws CmsException {

        return m_cache.getEntriesByStructureId(cms, structureId);

    }

    /**
     * Returns the sitemap entry for the given id, or <code>null</code> if not found.<p> 
     * 
     * @param cms the current CMS context
     * @param id the id to look for
     * 
     * @return the sitemap entry for the given id, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsSitemapEntry getEntryForId(CmsObject cms, CmsUUID id) throws CmsException {

        // retrieve it
        CmsSitemapEntry entry = m_cache.getEntryById(cms, id);
        if (entry != null) {
            // security check
            cms.readResource(entry.getStructureId());
        }
        return entry;
    }

    /**
     * Returns the sitemap entry for the given URI, or <code>null</code> if not found.<p>
     * 
     * If the URI passed as an argument is a VFS URI instead of a sitemap URI, a dummy sitemap
     * entry for the VFS location will be returned.<p>
     * 
     * @param cms the current CMS context
     * @param entryUri the sitemap entry URI to look for
     * 
     * @return the sitemap entry for the given URI, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsSitemapEntry getEntryForUri(CmsObject cms, String entryUri) throws CmsException {

        return getEntryForUri(cms, entryUri, m_cache);
    }

    /**
     * Returns the sitemap entry for the given URI, or <code>null</code> if not found.<p>
     * 
     * If the URI passed as an argument is a VFS URI instead of a sitemap URI, a dummy sitemap
     * entry for the VFS location will be returned.<p>
     * 
     * @param cms the current CMS context
     * @param entryUri the sitemap entry URI to look for
     * @param cache the sitemap cache which should be used for the lookup 
     * 
     * @return the sitemap entry for the given URI, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsSitemapEntry getEntryForUri(CmsObject cms, String entryUri, I_CmsSitemapCache cache) throws CmsException {

        // get the entry for the given path
        CmsInternalSitemapEntry entry = cache.getEntryByUri(cms, entryUri);
        if (entry != null) {
            // check permissions
            cms.readResource(entry.getStructureId());
            return entry;
        }

        // if not found try as detail page
        String path = entryUri;
        if (path.endsWith("/") && (path.length() > 1)) {
            path = path.substring(0, path.length() - 1);
        }
        String detailId = CmsResource.getName(path);
        if (!CmsUUID.isValidUUID(detailId)) {
            // not a detail page URI
            return new CmsInternalSitemapEntry(cms, entryUri);
        }
        entry = cache.getEntryByUri(cms, CmsResource.getParentFolder(entryUri));
        if (entry == null) {
            // not a detail page URI
            return new CmsInternalSitemapEntry(cms, entryUri);
        }

        // detail page
        CmsUUID id = new CmsUUID(detailId);
        // check existence / permissions
        CmsResource contentRes = cms.readResource(id);
        // get the title
        String title = cms.readPropertyObject(contentRes, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue(
            id.toString());
        // clone & extend the properties
        HashMap<String, CmsSimplePropertyValue> entryProps = new HashMap<String, CmsSimplePropertyValue>(
            entry.getNewProperties());
        // detail pages are NEVER shown in the navigation
        CmsSimplePropertyValue navValue = new CmsSimplePropertyValue("false", "false");
        entryProps.put(Property.navigation.getName(), navValue);
        // create entry
        CmsInternalSitemapEntry contentEntry = new CmsInternalSitemapEntry(
            entry.getId(),
            entry.getOriginalUri(),
            entry.getStructureId(),
            id.toString(),
            title,
            entry.isRootEntry(),
            new HashMap<String, CmsSimplePropertyValue>(),
            null,
            id);

        Map<String, CmsXmlContentProperty> propDefs = entry.getPropertyDefinitions();
        CmsPropertyInheritanceState propState = new CmsPropertyInheritanceState(entry.getComputedProperties(), propDefs);
        CmsPropertyInheritanceState newPropState = propState.update(
            Collections.<String, CmsSimplePropertyValue> emptyMap(),
            id.toString());
        contentEntry.setRuntimeInfo(entry.getEntryPoint(), 0, newPropState.getInheritedProperties());
        return contentEntry;
    }

    /**
     * Returns the entry point for the given sitemap site path.<p>
     * 
     * @param cms the CMS context
     * @param sitePath the sitemap site path
     * 
     * @return the site relative entry point for the given sitemap, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong
     */
    public String getEntryPoint(CmsObject cms, String sitePath) throws CmsException {

        String rootPath = sitePath;
        if (!rootPath.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
            rootPath = cms.getRequestContext().addSiteRoot(rootPath);
        }
        String locale = cms.getRequestContext().getLocale().toString();
        Map<String, String> active = m_cache.getActiveSitemaps(cms);
        for (Map.Entry<String, String> entry : active.entrySet()) {
            if (!entry.getValue().equals(rootPath)) {
                continue;
            }
            if (!entry.getKey().startsWith(locale)) {
                continue;
            }
            return cms.getRequestContext().removeSiteRoot(entry.getKey().substring(locale.length()));
        }
        return null;
    }

    /**
     * Returns the configured export name for a given site root.<p>
     * 
     * @param siteRoot a site root 
     * @return the configured export name for the site root  
     * 
     * @throws CmsException if something goes wrong 
     */
    public String getExportnameForSiteRoot(String siteRoot) throws CmsException {

        return m_cache.getExportName(siteRoot);
    }

    /**
     * Returns the parent entry of a given sitemap entry.<p>
     * 
     * @param cms the current CMS context 
     * @param entry the entry whose parent should be retrieved
     *  
     * @return the parent of the entry 
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsSitemapEntry getParentEntry(CmsObject cms, CmsSitemapEntry entry) throws CmsException {

        if (entry.isRootEntry()) {
            return null;
        }
        String uri = entry.getSitePath(cms);
        String parentUri = CmsResource.getParentFolder(uri);
        CmsSitemapEntry parent = getEntryForUri(cms, parentUri);
        return parent;
    }

    /**
     * Returns the entry which refers to a sub-sitemap with a given URI.
     * 
     * @param cms the current CMS context 
     * @param sitemapUri the URI of the sub-sitemap
     *  
     * @return the parent entry
     *  
     * @throws CmsException if something goes wrong 
     */
    public CmsSitemapEntry getParentEntryOfSitemap(CmsObject cms, String sitemapUri) throws CmsException {

        Map<String, String> entryPoints = m_cache.getEntryPoints(cms);
        String sitemapRootUri = cms.getRequestContext().addSiteRoot(sitemapUri);
        String entryPoint = entryPoints.get(sitemapRootUri);
        entryPoint = cms.getRequestContext().removeSiteRoot(entryPoint);
        CmsSitemapEntry entry = getEntryForUri(cms, entryPoint);
        if (entry.isRootEntry()) {
            return null;
        }
        String parentUri = CmsResource.getParentFolder(entryPoint);
        return getEntryForUri(cms, parentUri);
    }

    /**
     * Returns the parent sitemap for the given sitemap, 
     * or <code>null</code> if the given sitemap is a root sitemap.<p>
     * 
     * @param cms the current CMS context
     * @param sitemapUri the sitemap URI to get the parent sitemap for
     * 
     * @return the parent sitemap, or <code>null</code> if the given sitemap is a root sitemap
     * 
     * @throws CmsException if something goes wrong
     */
    public String getParentSitemap(CmsObject cms, String sitemapUri) throws CmsException {

        Map<String, String> active = m_cache.getActiveSitemaps(cms);
        // always use root paths
        String sitemapPath = cms.getRequestContext().addSiteRoot(sitemapUri);
        // search for the given sitemap's entry point
        String entryPoint = getEntryPointKeyForSitemapPath(cms, active, sitemapPath);
        if (entryPoint == null) {
            // not found
            return null;
        }
        // search for the longest entry point that matches the given sitemap
        Map.Entry<String, String> bestMatch = null;
        for (Map.Entry<String, String> entry : active.entrySet()) {
            String key = entry.getKey();
            if (entryPoint.equals(key)) {
                // the same
                continue;
            }
            if (!entryPoint.startsWith(key)) {
                // not matching
                continue;
            }
            if ((bestMatch == null) || (key.length() > bestMatch.getKey().length())) {
                // a better match found
                bestMatch = entry;
            }
        }
        if (bestMatch == null) {
            return null;
        }
        // and return the site path
        return cms.getRequestContext().removeSiteRoot(bestMatch.getValue());
    }

    /**
     * Returns the properties of a sitemap entry as {@link CmsProperty} instances.<p>
     * 
     * @param entry the sitemap entry whose properties should be returned 
     * 
     * @return a map from property names to {@link CmsProperty} objects 
     */
    public Map<String, CmsProperty> getProperties(CmsSitemapEntry entry) {

        return CmsXmlContentPropertyHelper.createCmsProperties(entry.getComputedProperties());
    }

    /**
     * Gets the URI of the root sitemap for a given site path.<p>
     * 
     * @param cms the current CMS context 
     * @param uri the URI for which the root sitemap should be retrieved 
     * 
     * @return the root sitemap URI for a given site path
     *  
     * @throws CmsException if something goes wrong 
     */
    @SuppressWarnings("null")
    public String getRootSitemapForUri(CmsObject cms, String uri) throws CmsException {

        Map<String, String> sitemaps = getActiveSitemapsAboveUri(cms, uri);
        Map.Entry<String, String> bestMatch = null;
        for (Map.Entry<String, String> entry : sitemaps.entrySet()) {
            String key = entry.getKey();
            if ((bestMatch == null) || bestMatch.getKey().startsWith(key)) {
                bestMatch = entry;
            }
        }
        return cms.getRequestContext().removeSiteRoot(bestMatch.getValue());
    }

    /**
     * Returns the root entries of root (non-sub-) sitemaps for the current site and locale.<p>
     * 
     * @param cms the CMS context
     * @return a list of root entries of root sitemaps 
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<CmsSitemapEntry> getRootSitemapRootEntries(CmsObject cms) throws CmsException {

        List<CmsSitemapEntry> result = new ArrayList<CmsSitemapEntry>();
        List<String> paths = getActiveSitemapsForSiteAndLocale(cms);
        for (String path : paths) {
            CmsSitemapEntry entry = getEntryForUri(cms, path);
            if (entry.isRootEntry()) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Reads the current sitemap URI bean from the request.<p>
     * 
     * @param req the servlet request
     * 
     * @return the sitemap URI bean, or <code>null</code> if not found
     */
    public CmsSitemapEntry getRuntimeInfo(ServletRequest req) {

        return (CmsSitemapEntry)req.getAttribute(ATTR_SITEMAP_ENTRY);
    }

    /**
     * Returns the searchable resource types.<p>
     * 
     * @return the resource types
     */
    public List<I_CmsResourceType> getSearchableResourceTypes() {

        // TODO: the searchable resource types should be read from configuration
        List<I_CmsResourceType> types = new ArrayList<I_CmsResourceType>();
        try {
            types.add(OpenCms.getResourceManager().getResourceType(CmsResourceTypeXmlContainerPage.getStaticTypeName()));
        } catch (CmsLoaderException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return types;
    }

    /**
     * Tries to find the "best" URI for a search result given as a structure id.<p>
     * 
     * If the search result is a container page, the method tries to find a sitemap URI
     * which references the resource and is closest to a given site path.<p>
     * 
     * If the search result is not a container page and has a detail view page, the result 
     * is the concatenation of the detail view page and the structure id of the search result.<p>
     * 
     * If the search result is not a container page and has no detail view page, its VFS uri is returned.<p>
     * 
     * @param cms the CMS context 
     * @param searchResultStructureId the structure id of the search result 
     * @param otherSitePath the site path such that if the structure id belongs to a container page, the closest sitemap entry URI to that site path is returned
     *   
     * @return the search result URI
     *   
     * @throws CmsException if something goes wrong 
     */
    public String getSearchResultUri(CmsObject cms, CmsUUID searchResultStructureId, String otherSitePath)
    throws CmsException {

        CmsResource resource = cms.readResource(searchResultStructureId);
        if (CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
            return getClosestSitePathForStructureId(cms, searchResultStructureId, otherSitePath);
        }
        String detailView = cms.readPropertyObject(
            resource,
            CmsPropertyDefinition.PROPERTY_ADE_SITEMAP_DETAILVIEW,
            true).getValue("");
        if (detailView.equals("")) {
            return cms.getSitePath(resource);
        }
        return CmsStringUtil.joinPaths(detailView, "/", searchResultStructureId.toString(), "/");
    }

    /**
     * Returns the sitemap URI for the given sitemap entry URI.<p>
     * 
     * @param cms the current CMS context
     * @param uri the sitemap entry URI to get the sitemap URI for
     * 
     * @return the sitemap URI for the given sitemap entry URI
     * 
     * @throws CmsException if something goes wrong
     */
    @SuppressWarnings("null")
    public String getSitemapForUri(CmsObject cms, String uri) throws CmsException {

        Map<String, String> sitemaps = getActiveSitemapsAboveUri(cms, uri);
        Map.Entry<String, String> bestMatch = null;
        for (Map.Entry<String, String> entry : sitemaps.entrySet()) {
            String key = entry.getKey();
            if ((bestMatch == null) || key.startsWith(bestMatch.getKey())) {
                bestMatch = entry;
            }
        }
        return cms.getRequestContext().removeSiteRoot(bestMatch.getValue());
    }

    /**
     * Returns the resources for sitemaps which reference a given resource.<p>
     * 
     * @param cms the current CMS context 
     * @param resource sitemaps which reference this resource should be returned 
     * @return the sitemaps which reference the resource 
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<CmsResource> getSitemapsForResource(CmsObject cms, CmsResource resource) throws CmsException {

        List<CmsRelation> relations = cms.readRelations(CmsRelationFilter.TARGETS.filterResource(resource).filterType(
            CmsRelationType.XML_STRONG));
        List<CmsResource> sitemaps = new ArrayList<CmsResource>();
        for (CmsRelation relation : relations) {
            CmsResource source = relation.getSource(cms, CmsResourceFilter.ALL);
            if (cms.existsResource(source.getStructureId(), CmsResourceFilter.DEFAULT)
                && CmsResourceTypeXmlSitemap.isSitemap(source)) {
                sitemaps.add(source);
            }
        }
        return sitemaps;
    }

    /**
     * Returns the cache for {@link CmsXmlSitemap} instances for the online or offline projects depending on a flag.
     * 
     * @param online if true, the online sitemap XML cache will be returned, else the offline cache
     *  
     * @return either the online or offline sitemap XML cache depending on the value of <code>online</code>
     */
    public CmsSitemapXmlCache getSitemapXmlCache(boolean online) {

        return m_sitemapXmlCaches.get(new Boolean(online));
    }

    /**
     * Tries to find a sitem path for a resource in a given sitemap.<p>
     * 
     * @param cms the current CMS context 
     * @param resource the resource for which the site path should be retrieved 
     * @param sitemapRes the sitemap in which
     *  
     * @return the site path in the sitemap
     * 
     * @throws CmsException if something goes wrong 
     */
    public String getSitePathForResource(CmsObject cms, CmsResource resource, CmsResource sitemapRes)
    throws CmsException {

        m_cache.getActiveSitemaps(cms);
        CmsXmlSitemap xmlSitemap = CmsXmlSitemapFactory.unmarshal(cms, sitemapRes);
        //TODO: is this really the right locale?
        Locale locale = cms.getRequestContext().getLocale();
        CmsSitemapBean sitemapBean = xmlSitemap.getSitemap(cms, locale);
        CmsInternalSitemapEntry entry = getSitemapEntryByStructureId(
            sitemapBean.getSiteEntries(),
            resource.getStructureId());
        return entry.getSitePath(cms);
    }

    /**
     * Returns a map from export names to site roots for which they are configured.<p>
     * 
     * @return a map from export names to site roots 
     *  
     * @throws CmsException if something goes wrong 
     */
    public Map<String, String> getSiteRootsForExportNames() throws CmsException {

        return m_cache.getInternalCache(true).getSiteRootsForExportNames();
    }

    /**
     * Returns the list of sub-entries for the given sitemap entry URI.<p>
     * 
     * @param cms the current CMS context
     * @param entryUri the sitemap entry URI
     * 
     * @return the list of sub-entries for the given sitemap entry URI
     * 
     * @throws CmsException if something goes wrong
     */
    public List<CmsSitemapEntry> getSubEntries(CmsObject cms, String entryUri) throws CmsException {

        List<CmsSitemapEntry> subEntries = new ArrayList<CmsSitemapEntry>();
        CmsInternalSitemapEntry entry = (CmsInternalSitemapEntry)getEntryForUri(cms, entryUri);
        for (CmsInternalSitemapEntry subEntry : entry.getSubEntries()) {
            if (cms.existsResource(subEntry.getStructureId())) {
                subEntries.add(subEntry);
            }
        }
        return subEntries;
    }

    /**
     * Returns true if the site with the given site root is using a sitemap.<p>
     * 
     * @param cms the CMS context 
     * @param siteRoot a site root 
     * 
     * @return true if the site with the given root uses a sitemap 
     * 
     * @throws CmsException if something goes wrong 
     */
    public boolean isSiteUsingSitemap(CmsObject cms, String siteRoot) throws CmsException {

        return m_cache.getSiteRootsWithSitemap(cms).contains(siteRoot);
    }

    /**
     * Clean up at shutdown time. Only intended to be called at system shutdown.<p>
     * 
     * @see org.opencms.main.OpenCmsCore#shutDown
     */
    public void shutdown() {

        m_cache.shutdown();
        getSitemapXmlCache(true).shutdown();
        getSitemapXmlCache(false).shutdown();
    }

    /**
     * Returns the cache.<p>
     *
     * @return the cache
     */
    protected I_CmsSitemapCache getCache() {

        return m_cache;
    }

    /**
     * Finds the entry point key (locale + entry point) of a sitemap given its location in the VFS.<p>
     * 
     * @param cms the current CMS context 
     * @param active the map of active sitemaps 
     * @param sitemapPath the location of the sitemap file 
     * 
     * @return the entry point key of the sitemap 
     */
    protected String getEntryPointKeyForSitemapPath(CmsObject cms, Map<String, String> active, String sitemapPath) {

        String entryPoint = null;
        for (Map.Entry<String, String> entry : active.entrySet()) {
            if (!entry.getValue().equals(sitemapPath)) {
                // wrong sitemap
                continue;
            }
            if (!entry.getKey().startsWith(cms.getRequestContext().getLocale().toString())) {
                // wrong locale
                continue;
            }
            // this one!
            entryPoint = entry.getKey();
            break;
        }
        return entryPoint;
    }

    /**
     * Returns the sitemap type id.<p>
     * 
     * @return the sitemap type id
     * 
     * @throws CmsLoaderException if the type is not configured
     */
    protected int getSitemapTypeId() throws CmsLoaderException {

        if (m_sitemapTypeId == 0) {
            m_sitemapTypeId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeXmlSitemap.getStaticTypeName()).getTypeId();
        }
        return m_sitemapTypeId;
    }

    /**
     * Checks whether the sitemap resource init handler is configured.<p>
     * 
     * @param systemConfiguration the system configuration
     *  
     * @return true if the sitemap resource init handler is configured 
     */
    protected boolean isSitemapResourceInitConfigured(CmsSystemConfiguration systemConfiguration) {

        for (I_CmsResourceInit initHandler : systemConfiguration.getResourceInitHandlers()) {
            if (initHandler instanceof CmsSitemapResourceHandler) {
                // found
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method for retrieving the active sitemaps which lie 'above' a given URI,
     * i.e. the sitemap for the given uri and all of its ancestor sitemaps.<p>
     * 
     * @param cms the CMS context 
     * @param uri the uri for which the sitemaps should be retrieved
     *  
     * @return the map of sitemaps above the URI
     *  
     * @throws CmsException if something goes wrong 
     */
    private Map<String, String> getActiveSitemapsAboveUri(CmsObject cms, String uri) throws CmsException {

        Map<String, String> active = m_cache.getActiveSitemaps(cms);
        Map<String, String> result = new HashMap<String, String>();
        String uriKey = cms.getRequestContext().getLocale().toString() + cms.getRequestContext().addSiteRoot(uri);
        for (Map.Entry<String, String> entry : active.entrySet()) {
            String key = entry.getKey();
            if (uriKey.startsWith(key)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private CmsProject getOnlineProject(CmsObject cms) throws CmsException {

        if (m_onlineProject == null) {
            m_onlineProject = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
        }
        return m_onlineProject;
    }

    /**
     * Searches a sitemap entry with a given resource structure id.<p>
     * 
     * The search starts from a list of sitemap entries passed as an argument, and does not follow references to sub-sitemaps.<p>
     * 
     * @param rootEntries the entries from which the search should be started 
     * @param resourceId the structure id to search for 
     * 
     * @return a sitemap entry with the given structure id, or null if none were found 
     */
    private CmsInternalSitemapEntry getSitemapEntryByStructureId(
        List<CmsInternalSitemapEntry> rootEntries,
        CmsUUID resourceId) {

        LinkedList<CmsInternalSitemapEntry> entriesToProcess = new LinkedList<CmsInternalSitemapEntry>();
        entriesToProcess.addAll(rootEntries);
        while (!entriesToProcess.isEmpty()) {
            CmsInternalSitemapEntry currentEntry = entriesToProcess.removeFirst();
            if (currentEntry.getStructureId().equals(resourceId)) {
                return currentEntry;
            }
            CmsSimplePropertyValue sitemapProp = currentEntry.getNewProperties().get(
                CmsSitemapManager.Property.sitemap.name());
            if (sitemapProp == null) {
                entriesToProcess.addAll(currentEntry.getSubEntries());
            }
        }
        return null;
    }
}

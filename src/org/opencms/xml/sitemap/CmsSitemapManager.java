/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapManager.java,v $
 * Date   : $Date: 2010/04/26 13:41:49 $
 * Version: $Revision: 1.35 $
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

import org.opencms.cache.CmsVfsCache;
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
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;

/**
 * Sitemap Manager.<p>
 * 
 * Provides all relevant functions for using the sitemap.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.35 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapManager extends CmsVfsCache {

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

    /** Cache for active offline sitemap. */
    private Map<String, CmsXmlSitemap> m_activeOffline;

    /** Cache for active online sitemap. */
    private Map<String, CmsXmlSitemap> m_activeOnline;

    /** The admin context. */
    private CmsObject m_adminCms;

    /** The cache instance. */
    private CmsSitemapCache m_cache;

    /** Lazy initialized sitemap type id. */
    private int m_sitemapTypeId;

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
        m_adminCms.getRequestContext().setSiteRoot("");
        // initialize the sitemap cache
        CmsSitemapCacheSettings cacheSettings = systemConfiguration.getSitemapCacheSettings();
        if (cacheSettings == null) {
            cacheSettings = new CmsSitemapCacheSettings();
        }
        m_cache = new CmsSitemapCache(memoryMonitor, cacheSettings);

        // check for the resource init handler
        for (I_CmsResourceInit initHandler : systemConfiguration.getResourceInitHandlers()) {
            if (initHandler instanceof CmsSitemapResourceHandler) {
                // found
                return;
            }
        }

        // not found
        LOG.warn(Messages.get().getBundle().key(
            Messages.LOG_WARN_SITEMAP_HANDLER_NOT_CONFIGURED_1,
            CmsSitemapResourceHandler.class.getName()));
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
    public List<CmsResource> getCreatableElements(CmsObject cms, String sitemapUri, ServletRequest request)
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

        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        Map<String, String> defProps = m_cache.getDefaultProps(online);
        if (defProps != null) {
            return defProps;
        }
        // default properties are not resource dependent, 
        // they are system wide, defined in the XSD
        // but we need a resource to get to the content handler
        // so get the first sitemap we can find
        List<CmsResource> sitemaps = new ArrayList<CmsResource>();
        try {
            sitemaps = cms.readResources("/", CmsResourceFilter.requireType(getSitemapTypeId()), true);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }
        if (sitemaps.isEmpty()) {
            // should never happen
            defProps = new HashMap<String, String>();
        } else {
            CmsResource resource = sitemaps.get(0);
            defProps = CmsXmlContentPropertyHelper.mergeDefaults(cms, resource, Collections.<String, String> emptyMap());
        }
        m_cache.setDefaultProps(defProps, online);
        return defProps;
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

        //TODO: use the properties inherited from super-sitemaps to find the default template
        CmsProperty prop = cms.readPropertyObject(sitemapUri, CmsPropertyDefinition.PROPERTY_TEMPLATE, true);
        String templatePath = prop.getValue();
        try {
            CmsResource resource = cms.readResource(templatePath);
            return resource;
        } catch (CmsException e) {
            LOG.debug(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Returns the property configuration for a given resource.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource
     * 
     * @return the property configuration
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, CmsXmlContentProperty> getElementPropertyConfiguration(CmsObject cms, CmsResource resource)
    throws CmsException {

        return CmsXmlContentDefinition.getContentHandlerForResource(cms, resource).getProperties();
    }

    /**
     * Returns the site entry for the given id, or <code>null</code> if not found.<p> 
     * 
     * @param cms the current CMS context
     * @param id the id to look for
     * 
     * @return the site entry for the given id, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsSitemapEntry getEntryForId(CmsObject cms, CmsUUID id) throws CmsException {

        // check the cache
        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        CmsSitemapEntry uriEntry = m_cache.getUri(id.toString(), online);
        if (uriEntry != null) {
            // found in cache
            return uriEntry;
        }

        CmsObject cloneCms = OpenCms.initCmsObject(cms);
        cloneCms.getRequestContext().setSiteRoot("");

        // this is slow! :(
        CmsSitemapEntry entry = visitEntry(cloneCms, id, "/");
        if ((entry != null) && entry.isSitemap()) {
            return entry;
        }
        return null;
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
    public CmsSitemapEntry getEntryForUri(CmsObject cms, String uri) throws CmsException {

        String path = cms.getRequestContext().addSiteRoot(uri);

        // check the cache
        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        CmsSitemapEntry uriEntry = m_cache.getUri(path, online);
        if (uriEntry != null) {
            // found in cache
            return uriEntry;
        }

        // check the missed cache
        if (m_cache.getMissingUri(path, online) != null) {
            // already marked as not found, return VFS entry if possible
            try {
                return new CmsSitemapEntry(cms, uri);
            } catch (CmsException e) {
                return null;
            }
        }

        // get it
        uriEntry = getEntry(cms, uri, online);
        if (uriEntry == null) {
            // cache the missed attempt
            m_cache.setMissingUri(path, online);
            // return VFS entry if possible
            try {
                return new CmsSitemapEntry(cms, uri);
            } catch (CmsException e) {
                return null;
            }
        }

        // cache the found entry
        m_cache.setUri(path, uriEntry, online);
        return uriEntry;
    }

    /**
     * Returns the parent sitemap for the given sitemap, 
     * or <code>null</code> if the given sitemap is a root sitemap.<p>
     * 
     * @param cms the current CMS context
     * @param sitemap the sitemap resource to get the parent sitemap for
     * 
     * @return the parent sitemap
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsResource getParentSitemap(CmsObject cms, CmsResource sitemap) throws CmsException {

        CmsRelationFilter filter = CmsRelationFilter.SOURCES.filterType(CmsRelationType.XML_WEAK);
        for (CmsRelation relation : cms.getRelationsForResource(sitemap, filter)) {
            if (CmsResource.isTemporaryFileName(relation.getSourcePath())) {
                // temp file
                continue;
            }
            CmsResource source = relation.getSource(cms, CmsResourceFilter.ALL);
            if (((source.getFlags() & CmsResource.FLAG_TEMPFILE) > 0)) {
                // temp file
                continue;
            }
            if (!CmsResourceTypeXmlSitemap.isSitemap(source)) {
                // not sitemap
                continue;
            }
            // found
            return source;
        }
        return null;
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
     * Returns the sitemap for the given sitemap URI.<p>
     * 
     * @param cms the current CMS context
     * @param uri the sitemap URI to get the sitemap for
     * @param findRoot if <code>true</code> it will find a root sitemap, a sub-sitemap
     * 
     * @return the sitemap for the given sitemap URI
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsXmlSitemap getSitemapForUri(CmsObject cms, String uri, boolean findRoot) throws CmsException {

        Map<String, CmsXmlSitemap> active = getActive(cms.getRequestContext().currentProject());
        String rootUri = cms.getRequestContext().addSiteRoot(uri);
        String localeUri = cms.getRequestContext().getLocale().toString() + rootUri;
        Map.Entry<String, CmsXmlSitemap> bestMatch = null;
        for (Map.Entry<String, CmsXmlSitemap> entry : active.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(localeUri)) {
                continue;
            }
            if ((bestMatch == null)
                || (!findRoot && key.startsWith(bestMatch.getKey()) && (key.length() != bestMatch.getKey().length()))
                || (findRoot && bestMatch.getKey().startsWith(key))) {
                // security check
                if (cms.existsResource(entry.getValue().getFile().getStructureId())) {
                    // a better match found
                    bestMatch = entry;
                }
            }
        }
        if (bestMatch == null) {
            return null;
        }
        return bestMatch.getValue();
    }

    /**
     * Clean up at shutdown time. Only intended to be called at system shutdown.<p>
     * 
     * @see org.opencms.main.OpenCmsCore#shutDown
     */
    @Override
    public void shutdown() {

        super.shutdown();
        m_cache.shutdown();
    }

    /**
     * @see org.opencms.cache.CmsVfsCache#flush(boolean)
     */
    @Override
    protected void flush(boolean online) {

        if (online) {
            m_activeOnline.clear();
            m_activeOnline = null;
        } else {
            m_activeOffline.clear();
            m_activeOffline = null;
        }
    }

    /**
     * Returns the cache.<p>
     *
     * @return the cache
     */
    protected CmsSitemapCache getCache() {

        return m_cache;
    }

    /**
     * Returns the site entry for the given URI, or <code>null</code> if not found.<p> 
     * 
     * @param cms the current CMS context
     * @param uri the URI to look for
     * @param online if online or offline, the same than in the cms context, but just to not access it again
     * 
     * @return the site entry for the given URI, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsSitemapEntry getEntry(CmsObject cms, String uri, boolean online) throws CmsException {

        String rootUri = cms.getRequestContext().addSiteRoot(uri);
        CmsUUID logId = null;
        if (LOG.isDebugEnabled()) {
            logId = new CmsUUID(); // unique id to identify the request
            LOG.debug(Messages.get().container(
                Messages.LOG_DEBUG_SITEMAP_ENTRY_3,
                logId,
                rootUri,
                Boolean.valueOf(online)).key());
        }
        // find closest match from cache
        CmsSitemapEntry startEntry = null;
        String startUri = rootUri;
        while (!startUri.equals("/") && (startEntry == null)) {
            startUri = CmsResource.getParentFolder(startUri);
            startEntry = m_cache.getUri(startUri, online);
        }

        // inherited properties 
        // we can safely use one reference, since CmsSiteEntryBean#setRuntimeInfo(...) will clone it      
        Map<String, String> properties = new HashMap<String, String>();

        // if no match found from the cache
        if (startEntry == null) {
            // find the root sitemap for this site
            CmsXmlSitemap sitemapXml = getSitemapForUri(cms, uri, true);
            // validate sitemap
            if (sitemapXml == null) {
                // sitemap not found
                return null;
            }

            CmsSitemapBean sitemap = sitemapXml.getSitemap(cms, cms.getRequestContext().getLocale());
            if ((sitemap == null) || sitemap.getSiteEntries().isEmpty()) {
                // sitemap is empty
                return null;
            }
            startUri = sitemap.getEntryPoint();
            startEntry = sitemap.getSiteEntries().get(0);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(startEntry.getName())) {
                // Root entries of root sitemaps HAVE to have an empty name,
                startEntry.removeName();
            }
            startEntry.setRuntimeInfo(properties, 0);
            // cache the current entry
            m_cache.setUri(startUri, startEntry, online);
            // special case for '/'
            if (normalizePath(rootUri.substring(startUri.length())).length() == 0) {
                LOG.debug(Messages.get().container(
                    Messages.LOG_DEBUG_SITEMAP_FOUND_3,
                    logId,
                    new Integer(0),
                    startEntry.getRootPath()).key());
                return startEntry;
            }
        }
        // start to collect the inherited properties
        properties.putAll(startEntry.getInheritedProperties());

        // get the important part of the uri
        LinkedList<String> entryPaths = new LinkedList<String>(CmsStringUtil.splitAsList(
            normalizePath(rootUri.substring(startUri.length())),
            "/"));
        // get started
        String uriPath = startUri;
        CmsSitemapEntry parentEntry = startEntry;
        boolean finished = false;
        while (!finished) {
            int position = 0;
            List<CmsSitemapEntry> subEntries = getSubEntries(cms, parentEntry, entryPaths, properties, logId);
            if (subEntries == null) {
                return null;
            }
            String name = entryPaths.removeFirst();
            uriPath += name + "/";
            // check the missed cache
            if (m_cache.getMissingUri(uriPath, online) != null) {
                // already marked as not found
                LOG.debug(Messages.get().container(Messages.LOG_DEBUG_SITEMAP_ENTRY_MISSING_2, logId, startUri).key());
                return null;
            }
            LOG.debug(Messages.get().container(Messages.LOG_DEBUG_SITEMAP_ENTRY_CHECK_2, logId, uriPath).key());
            int size = subEntries.size();
            for (; position < size; position++) {
                CmsSitemapEntry entry = subEntries.get(position);
                if (entry.getInheritedProperties() == null) {
                    // update the entry only if needed
                    entry.setRuntimeInfo(properties, position);
                    // cache the current entry
                    m_cache.setUri(uriPath, entry, online);
                }
                if (!entry.getName().equals(name)) {
                    // no match
                    LOG.debug(Messages.get().container(
                        Messages.LOG_DEBUG_SITEMAP_NO_MATCH_3,
                        logId,
                        new Integer(position),
                        entry.getRootPath()).key());
                    continue;
                }
                LOG.debug(Messages.get().container(
                    Messages.LOG_DEBUG_SITEMAP_MATCH_3,
                    logId,
                    new Integer(position),
                    entry.getRootPath()).key());
                if (entryPaths.isEmpty()) {
                    // if nothing left, we got a match
                    LOG.debug(Messages.get().container(
                        Messages.LOG_DEBUG_SITEMAP_FOUND_3,
                        logId,
                        new Integer(position),
                        entry.getRootPath()).key());
                    return entry;
                } else {
                    // inherit properties
                    properties.putAll(entry.getProperties());
                    // continue with sub-entries
                    parentEntry = entry;
                    subEntries = getSubEntries(cms, entry, entryPaths, properties, logId);
                    if (subEntries == null) {
                        return null;
                    }
                    finished = subEntries.isEmpty();
                    if (finished) {
                        LOG.debug(Messages.get().container(
                            Messages.LOG_DEBUG_SITEMAP_NO_SUBENTRIES_3,
                            logId,
                            new Integer(position),
                            entry.getRootPath()).key());
                    }
                }
                break;
            }
            if (position == size) {
                // not found
                finished = true;
                LOG.debug(Messages.get().container(Messages.LOG_DEBUG_SITEMAP_NOT_FOUND_2, logId, uriPath).key());
            }
        }

        return null;
    }

    /**
     * Returns the subentries, including sub-sitemap lookup.<p>
     * 
     * @param cms the current CMS context
     * @param logId the logging id
     * @param entry the entry to get the subentries for
     * @param entryPaths the remaining path
     * @param properties the current inherited properties
     * 
     * @return a list of subentries
     * 
     * @throws CmsException if something goes wrong
     */
    protected List<CmsSitemapEntry> getSubEntries(
        CmsObject cms,
        CmsSitemapEntry entry,
        List<String> entryPaths,
        Map<String, String> properties,
        CmsUUID logId) throws CmsException {

        List<CmsSitemapEntry> subEntries = entry.getSubEntries();
        if (subEntries.isEmpty()) {
            // check sitemap property
            String subSitemapId = entry.getProperties().get(CmsSitemapManager.Property.sitemap.name());
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(subSitemapId)) {
                // switch to sub-sitemap
                CmsResource subSitemap = cms.readResource(new CmsUUID(subSitemapId));
                LOG.debug(Messages.get().container(
                    Messages.LOG_DEBUG_SITEMAP_SUBSITEMAP_2,
                    logId,
                    cms.getSitePath(subSitemap)).key());
                CmsXmlSitemap sitemapXml = CmsXmlSitemapFactory.unmarshal(cms, subSitemap);
                CmsSitemapBean sitemap = sitemapXml.getSitemap(cms, cms.getRequestContext().getLocale());
                if (sitemap == null) {
                    // no sitemap found
                    return null;
                }
                // be sure the sub-entries do not inherit the sitemap property
                properties.remove(CmsSitemapManager.Property.sitemap.name());
                // continue with the sub-sitemap
                subEntries = sitemap.getSiteEntries();
            } else if ((entryPaths.size() == 1) && CmsUUID.isValidUUID(entryPaths.get(0))) {
                // detail pages
                CmsUUID id = new CmsUUID(entryPaths.get(0));
                // check that the content exists
                CmsResource contentRes = cms.readResource(id);
                // get the title
                String title = cms.readPropertyObject(contentRes, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue(
                    id.toString());
                // clone & extend the properties
                HashMap<String, String> entryProps = new HashMap<String, String>(entry.getProperties());
                // detail pages are NEVER shown in the navigation
                entryProps.put(Property.navigation.getName(), Boolean.FALSE.toString());
                // create entry
                CmsSitemapEntry contentEntry = new CmsSitemapEntry(
                    entry.getId(),
                    entry.getOriginalUri(),
                    entry.getResourceId(),
                    id.toString(),
                    title,
                    entryProps,
                    null,
                    id);
                LOG.debug(Messages.get().container(
                    Messages.LOG_DEBUG_SITEMAP_FOUND_3,
                    logId,
                    new Integer(0),
                    contentEntry.getRootPath()).key());
                return Collections.singletonList(contentEntry);
            }
        }
        return subEntries;
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
     * @see org.opencms.cache.CmsVfsCache#uncacheResource(org.opencms.file.CmsResource)
     */
    @Override
    protected void uncacheResource(CmsResource resource) {

        if (resource == null) {
            LOG.warn(Messages.get().container(Messages.LOG_WARN_UNCACHE_NULL_0));
            return;
        }
        if (CmsResourceTypeXmlSitemap.isSitemap(resource)) {
            m_activeOffline.clear();
            m_activeOffline = null;
        }
    }

    /**
     * Recursively visits the sitemap entries to find a match for the given id.<p>
     * 
     * @param cms the CMS context, should be in the root site
     * @param id the id to search for
     * @param path the starting URI (as root path)
     * 
     * @return the matching entry, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsSitemapEntry visitEntry(CmsObject cms, CmsUUID id, String path) throws CmsException {

        CmsSitemapEntry entry = getEntryForUri(cms, path);
        // check if found
        if (entry.getId().equals(id)) {
            return entry;
        }
        if (entry.isSitemap()) {
            // sitemap case
            for (CmsSitemapEntry subEntry : entry.getSubEntries()) {
                CmsSitemapEntry found = visitEntry(cms, id, subEntry.getRootPath());
                if (found != null) {
                    return found;
                }
            }
        } else {
            // vfs case
            List<CmsResource> subresources = cms.readResources(
                path,
                CmsResourceFilter.DEFAULT.addRequireFolder(),
                false);
            for (CmsResource subresource : subresources) {
                CmsSitemapEntry found = visitEntry(cms, id, subresource.getRootPath());
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Initializes the active sitemap lookup table.<p>
     * 
     * This method is synchronized since the cms object is not thread safe,
     * and it does not make any sense anyhow to concurrently initialize the 
     * look up table.<p>
     * 
     * @param online if online or offline
     * 
     * @throws CmsException if something goes wrong
     */
    private synchronized Map<String, CmsXmlSitemap> getActive(CmsProject project) throws CmsException {

        Map<String, CmsXmlSitemap> active;
        if (project.isOnlineProject()) {
            m_activeOnline = Collections.synchronizedMap(new HashMap<String, CmsXmlSitemap>());
            OpenCms.getMemoryMonitor().register(
                CmsSitemapCache.class.getName() + ".sitemapActiveOnline",
                m_activeOnline);
            active = m_activeOnline;
        } else {
            m_activeOffline = Collections.synchronizedMap(new HashMap<String, CmsXmlSitemap>());
            OpenCms.getMemoryMonitor().register(
                CmsSitemapCache.class.getName() + ".sitemapActiveOffline",
                m_activeOffline);
            active = m_activeOffline;
        }
        m_adminCms.getRequestContext().setCurrentProject(project);
        // iterate the sitemaps
        List<CmsResource> sitemaps = m_adminCms.readResources("/", CmsResourceFilter.DEFAULT_FILES.addRequireType(
            getSitemapTypeId()).addExcludeFlags(CmsResource.FLAG_TEMPFILE));
        for (CmsResource resource : sitemaps) {
            if (CmsResource.isTemporaryFileName(resource.getName())) {
                continue;
            }
            CmsXmlSitemap sitemap = CmsXmlSitemapFactory.unmarshal(m_adminCms, resource);
            for (Locale locale : sitemap.getLocales()) {
                active.put(locale.toString() + sitemap.getSitemap(m_adminCms, locale).getEntryPoint(), sitemap);
            }
        }
        return active;
    }

    /**
     * Returns the sitemap type id.<p>
     * 
     * @return the sitemap type id
     * 
     * @throws CmsLoaderException if the type is not configured
     */
    private int getSitemapTypeId() throws CmsLoaderException {

        if (m_sitemapTypeId == 0) {
            m_sitemapTypeId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeXmlSitemap.getStaticTypeName()).getTypeId();
        }
        return m_sitemapTypeId;
    }
}

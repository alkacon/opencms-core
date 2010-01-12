/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapManager.java,v $
 * Date   : $Date: 2010/01/12 11:14:31 $
 * Version: $Revision: 1.8 $
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

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlSitemap;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
 * @version $Revision: 1.8 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapManager {

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

    /** Request attribute name constant fro the current sitemap URI. */
    public static final String ATTR_SITEMAP_CURRENT_URI = "__currentSitemapURI";

    /** Request attribute name constant for the current sitemap entry bean. */
    public static final String ATTR_SITEMAP_ENTRY = "__currentSitemapEntry";

    /** Constant property name for sub-sitemap reference. */
    public static final String PROPERTY_SITEMAP = "sitemap";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapManager.class);

    /** The cache instance. */
    protected CmsSitemapCache m_cache;

    /**
     * Creates a new sitemap manager.<p>
     * 
     * @param memoryMonitor the memory monitor instance
     * @param systemConfiguration the system configuration
     */
    public CmsSitemapManager(CmsMemoryMonitor memoryMonitor, CmsSystemConfiguration systemConfiguration) {

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
        List<CmsProperty> props = new ArrayList<CmsProperty>();
        props.add(titleProp);
        cms.writePropertyObjects(sitemapPath, props);
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
     * Reads the current sitemap entry bean from the request.<p>
     * 
     * @param req the servlet request
     * 
     * @return the sitemap entry bean, or <code>null</code> if not found
     */
    public CmsSiteEntryBean getCurrentEntry(ServletRequest req) {

        return (CmsSiteEntryBean)req.getAttribute(ATTR_SITEMAP_ENTRY);
    }

    /**
     * Reads the current sitemap URI from the request.<p>
     * 
     * @param req the servlet request
     * 
     * @return the sitemap URI, or <code>null</code> if the sitemap is not used
     */
    public String getCurrentUri(ServletRequest req) {

        return (String)req.getAttribute(ATTR_SITEMAP_CURRENT_URI);
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
     * Returns the site entry for the given URI, or <code>null</code> if not found.<p> 
     * 
     * @param cms the current CMS context
     * @param uri the URI to look for
     * 
     * @return the site entry for the given URI, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsSiteEntryBean getEntryForUri(CmsObject cms, String uri) throws CmsException {

        String path = cms.getRequestContext().addSiteRoot(uri);

        // check the cache
        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        CmsSiteEntryBean uriEntry = m_cache.getUri(path, online);
        if (uriEntry != null) {
            // found in cache
            return uriEntry;
        }

        // check the missed cache
        if (m_cache.getMissingUri(path, online) != null) {
            // already marked as not found
            return null;
        }

        // get it
        EntryData data = getEntry(cms, uri, online, false);
        if (data == null) {
            // cache the missed attempt
            m_cache.setMissingUri(path, online);
            return null;
        }

        // cache the found entry
        uriEntry = data.getEntry();
        m_cache.setUri(path, uriEntry, online);

        return uriEntry;
    }

    /**
     * Returns the searchable resource types.<p>
     * 
     * @return the resource types
     */
    public List<I_CmsResourceType> getSearchableResourceTypes() {

        //TODO: the searchable resource types should be read from configuration
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

        String path = cms.getRequestContext().addSiteRoot(uri);

        // check the cache
        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        Map<String, String> properties = m_cache.getSearchProps(path, online);
        if (properties != null) {
            // found in cache
            return properties;
        }

        // check the missed cache
        if (m_cache.getMissingUri(path, online) != null) {
            // already marked as not found
            return null;
        }

        // try to find the sitemap entry with its properties
        EntryData data = getEntry(cms, path, online, true);
        if (data == null) {
            // cache the missed attempt
            m_cache.setMissingUri(path, online);
            return null;
        }

        // merge default properties
        properties = new HashMap<String, String>();
        properties.putAll(getDefaultProperties(cms, data.getSitemap().getFile(), online));
        properties.putAll(data.getProperties());

        // cache the found properties
        m_cache.setSearchProps(path, properties, online);

        return properties;
    }

    /**
     * Clean up at shutdown time. Only intended to be called at system shutdown.<p>
     * 
     * @see org.opencms.main.OpenCmsCore#shutDown
     */
    public void shutdown() {

        m_cache.shutdown();
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

        Map<String, String> defProps = m_cache.getDefaultProps(online);
        if (defProps != null) {
            return defProps;
        }
        defProps = new HashMap<String, String>();
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
        m_cache.setDefaultProps(defProps, online);
        return defProps;
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
        // find the sitemap
        CmsXmlSitemap sitemapXml = null;
        String sitemapFolder = uri;
        while (sitemapFolder != null) {
            if (cms.existsResource(sitemapFolder)) {
                String prop = cms.readPropertyObject(sitemapFolder, CmsPropertyDefinition.PROPERTY_SITEMAP, true).getValue();
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(prop)) {
                    if (cms.getRequestContext().getSiteRoot().equals("")) {
                        // adjust the property path, since it will be a site path, and we are in the root
                        CmsSite site = OpenCms.getSiteManager().getSiteForRootPath(uri);
                        if (site != null) {
                            prop = site.getSiteRoot() + prop;
                        }
                    }
                    if (cms.existsResource(prop)) {
                        sitemapXml = CmsXmlSitemapFactory.unmarshal(cms, cms.readResource(prop));
                        break;
                    }
                }
            }
            sitemapFolder = CmsResource.getParentFolder(sitemapFolder);
        }
        if ((sitemapXml == null) || (sitemapFolder == null)) {
            // sitemap not found 
            return null;
        }
        CmsSitemapBean sitemap = sitemapXml.getSitemap(cms, cms.getRequestContext().getLocale());
        if (sitemap == null) {
            return null;
        }
        LinkedList<String> entryPaths = new LinkedList<String>(CmsStringUtil.splitAsList(
            normalizePath(uri.substring(sitemapFolder.length())),
            "/"));
        // property collection
        Map<String, String> properties = new HashMap<String, String>();
        if (collectProperties) {
            // start with the root entry properties
            properties.putAll(sitemap.getSiteEntries().get(0).getProperties());
        }
        // special case for '/'
        if (entryPaths.isEmpty()) {
            if (sitemap.getSiteEntries().isEmpty()) {
                return null;
            }
            CmsSiteEntryBean entry = sitemap.getSiteEntries().get(0);
            entry.setPosition(0);
            LOG.debug(Messages.get().container(
                Messages.LOG_DEBUG_SITEMAP_FOUND_3,
                logId,
                new Integer(0),
                entry.getName()).key());
            return new EntryData(entry, properties, sitemapXml);
        }
        // get started
        String uriPath = cms.getRequestContext().getSiteRoot() + sitemapFolder;
        List<CmsSiteEntryBean> subEntries = sitemap.getSiteEntries().get(0).getSubEntries();
        boolean finished = false;
        while (!finished) {
            String name = entryPaths.removeFirst();
            LOG.debug(Messages.get().container(Messages.LOG_DEBUG_SITEMAP_ENTRY_CHECK_2, logId, uriPath).key());
            uriPath += "/" + name;
            // check the missed cache
            if (m_cache.getMissingUri(uriPath, online) != null) {
                // already marked as not found
                LOG.debug(Messages.get().container(Messages.LOG_DEBUG_SITEMAP_ENTRY_MISSING_2, logId, uriPath).key());
                return null;
            }
            int position = 0;
            int size = subEntries.size();
            for (; position < size; position++) {
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
                    subEntries = entry.getSubEntries();
                    if (subEntries.isEmpty()) {
                        // check sitemap property
                        String subSitemapId = entry.getProperties().get(CmsSitemapManager.PROPERTY_SITEMAP);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(subSitemapId)) {
                            // switch to sub-sitemap
                            CmsResource subSitemap = cms.readResource(new CmsUUID(subSitemapId));
                            LOG.debug(Messages.get().container(
                                Messages.LOG_DEBUG_SITEMAP_SUBSITEMAP_2,
                                logId,
                                cms.getSitePath(subSitemap)).key());
                            sitemapXml = CmsXmlSitemapFactory.unmarshal(cms, subSitemap);
                            sitemap = sitemapXml.getSitemap(cms, cms.getRequestContext().getLocale());
                            if (sitemap == null) {
                                // no sitemap found
                                return null;
                            }
                            subEntries = sitemap.getSiteEntries();
                        }
                    }
                    finished = subEntries.isEmpty();
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
            if (position == size) {
                // not found
                finished = true;
                LOG.debug(Messages.get().container(Messages.LOG_DEBUG_SITEMAP_NOT_FOUND_2, logId, uriPath).key());
            }
        }

        return null;
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
}

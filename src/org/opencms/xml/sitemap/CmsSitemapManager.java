/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapManager.java,v $
 * Date   : $Date: 2010/02/02 10:10:10 $
 * Version: $Revision: 1.19 $
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
 * @version $Revision: 1.19 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapManager {

    /** Property name constants. */
    public enum Property {

        /** <code>navigation</code> property name. */
        navigation,
        /** <code>sitemap</code> property name. */
        sitemap;
    }

    /** Request attribute name constant for the current sitemap entry bean. */
    public static final String ATTR_SITEMAP_ENTRY = "__currentSitemapEntry";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapManager.class);

    /** The cache instance. */
    private CmsSitemapCache m_cache;

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
            sitemaps = cms.readResources(
                "/",
                CmsResourceFilter.requireType(CmsResourceTypeXmlSitemap.getStaticTypeId()),
                true);
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
            // already marked as not found, return VFS entry if possible
            try {
                return new CmsSiteEntryBean(cms, uri);
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
                return new CmsSiteEntryBean(cms, uri);
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
     * @param xmlSitemap the sitemap to get the parent sitemap for
     * 
     * @return the parent sitemap
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsXmlSitemap getParentSitemap(CmsObject cms, CmsXmlSitemap xmlSitemap) throws CmsException {

        CmsXmlSitemap superSitemap = null;
        CmsRelationFilter filter = CmsRelationFilter.SOURCES.filterType(CmsRelationType.XML_WEAK);
        for (CmsRelation relation : cms.getRelationsForResource(xmlSitemap.getFile(), filter)) {
            CmsResource source = relation.getSource(cms, CmsResourceFilter.ALL);
            if (CmsResourceTypeXmlSitemap.isSitemap(source)) {
                superSitemap = CmsXmlSitemapFactory.unmarshal(cms, source);
                break;
            }
        }
        return superSitemap;
    }

    /**
     * Reads the current sitemap URI bean from the request.<p>
     * 
     * @param req the servlet request
     * 
     * @return the sitemap URI bean, or <code>null</code> if not found
     */
    public CmsSiteEntryBean getRuntimeInfo(ServletRequest req) {

        return (CmsSiteEntryBean)req.getAttribute(ATTR_SITEMAP_ENTRY);
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
     * 
     * @return the sitemap for the given sitemap URI
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsXmlSitemap getSitemapForUri(CmsObject cms, String uri) throws CmsException {

        String rootUri = cms.getRequestContext().addSiteRoot(uri);
        CmsRelation bestMatch = null;
        // find the correct sitemap
        for (CmsRelation relation : cms.readRelations(CmsRelationFilter.TARGETS.filterType(CmsRelationType.ENTRY_POINT))) {
            if (rootUri.startsWith(relation.getTargetPath())) {
                if ((bestMatch == null) || !bestMatch.getTargetPath().startsWith(relation.getTargetPath())) {
                    bestMatch = relation;
                }
            }
        }
        if (bestMatch == null) {
            return null;
        }
        return CmsXmlSitemapFactory.unmarshal(cms, cms.readResource(bestMatch.getSourceId()));

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
    protected CmsSiteEntryBean getEntry(CmsObject cms, String uri, boolean online) throws CmsException {

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
        // find the correct sitemap
        CmsXmlSitemap sitemapXml = getSitemapForUri(cms, uri);
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
        // get the important part of the uri
        LinkedList<String> entryPaths = new LinkedList<String>(CmsStringUtil.splitAsList(
            normalizePath(rootUri.substring(sitemap.getEntryPoint().length())),
            "/"));
        // property collection
        Map<String, String> properties = new HashMap<String, String>();
        // start with the root entry properties
        properties.putAll(sitemap.getSiteEntries().get(0).getProperties());
        // special case for '/'
        if (entryPaths.isEmpty()) {
            CmsSiteEntryBean entry = sitemap.getSiteEntries().get(0);
            entry.setRuntimeInfo(properties, 0, null);
            LOG.debug(Messages.get().container(
                Messages.LOG_DEBUG_SITEMAP_FOUND_3,
                logId,
                new Integer(0),
                entry.getRootPath()).key());
            return entry;
        }
        // get started
        String uriPath = cms.getRequestContext().getSiteRoot() + sitemap.getEntryPoint();
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
                entry.setRuntimeInfo(properties, position, null);
                // cache the found entry
                m_cache.setUri(rootUri, entry, online);
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
                    // continue with sub-entries
                    subEntries = entry.getSubEntries();
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
                            sitemapXml = CmsXmlSitemapFactory.unmarshal(cms, subSitemap);
                            sitemap = sitemapXml.getSitemap(cms, cms.getRequestContext().getLocale());
                            if (sitemap == null) {
                                // no sitemap found
                                return null;
                            }
                            // continue with the sub-sitemap
                            subEntries = sitemap.getSiteEntries();
                        }
                    }
                    finished = subEntries.isEmpty();
                    if (finished) {
                        if ((entryPaths.size() == 1) && CmsUUID.isValidUUID(entryPaths.get(0))) {
                            // detail pages
                            CmsUUID id = new CmsUUID(entryPaths.get(0));
                            // check that the content exists
                            CmsResource contentRes = cms.readResource(id);
                            // get the title
                            String title = cms.readPropertyObject(
                                contentRes,
                                CmsPropertyDefinition.PROPERTY_TITLE,
                                false).getValue(id.toString());
                            // clone & extend the properties
                            HashMap<String, String> entryProps = new HashMap<String, String>(entry.getProperties());
                            // detail pages are NEVER shown in the navigation
                            entryProps.put(Property.navigation.name(), Boolean.FALSE.toString());
                            properties.put(Property.navigation.name(), Boolean.FALSE.toString());
                            // create entry
                            entry = new CmsSiteEntryBean(
                                entry.getId(),
                                entry.getOriginalUri(),
                                entry.getResourceId(),
                                id.toString(),
                                title,
                                entryProps,
                                null);
                            entry.setRuntimeInfo(properties, 0, id);
                            LOG.debug(Messages.get().container(
                                Messages.LOG_DEBUG_SITEMAP_FOUND_3,
                                logId,
                                new Integer(0),
                                entry.getRootPath()).key());
                            return entry;
                        }
                        LOG.debug(Messages.get().container(
                            Messages.LOG_DEBUG_SITEMAP_NO_SUBENTRIES_3,
                            logId,
                            new Integer(position),
                            entry.getRootPath()).key());
                    }
                    properties.putAll(entry.getProperties());
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

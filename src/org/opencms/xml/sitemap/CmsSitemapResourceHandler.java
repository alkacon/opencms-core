/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapResourceHandler.java,v $
 * Date   : $Date: 2009/11/25 15:26:58 $
 * Version: $Revision: 1.3 $
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
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.containerpage.CmsADEManager;

import java.util.Collections;
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
 * @version $Revision: 1.3 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapResourceHandler implements I_CmsResourceInit, I_CmsEventListener {

    /** Constant property name for sub-sitemap reference. */
    public static final String PROPERTY_SITEMAP = "sitemap";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapResourceHandler.class);

    /** The singleton instance. */
    private static CmsSitemapResourceHandler m_instance;

    /** Cache for missing offline URIs. */
    private Map<String, Boolean> m_missingUrisOffline;

    /** Cache for missing online URIs. */
    private Map<String, Boolean> m_missingUrisOnline;

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

        if (m_missingUrisOffline == null) {
            init();
        }

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
        String extension = uri.substring(uri.length() - 1 - CmsFileUtil.getExtension(uri).length());
        String uriNoExt = uri.substring(0, uri.length() - extension.length());
        uriEntry = getEntry(cms, uriNoExt, online);
        // match the extension 
        if ((uriEntry != null) && (extension.length() > 0)) {
            if (!uriEntry.getExtension().equals(extension.substring(1))) {
                uriEntry = null;
            }
        }

        if (uriEntry == null) {
            // cache the missed attempt
            if (online) {
                m_missingUrisOnline.put(path, Boolean.TRUE);
            } else {
                m_missingUrisOffline.put(path, Boolean.TRUE);
            }
            return null;
        }
        // cache the found entry
        if (online) {
            m_urisOnline.put(path, uriEntry);
        } else {
            m_urisOffline.put(path, uriEntry);
        }

        return uriEntry;
    }

    /**
     * Initialization routine.<p>
     */
    public void init() {

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
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(CmsResource resource, CmsObject cms, HttpServletRequest req, HttpServletResponse res)
    throws CmsResourceInitException {

        // only do something if the resource was not found good
        if (resource == null) {
            if (m_missingUrisOffline == null) {
                init();
            }
            // check if the resource is in the site map
            try {
                // find the site map entry
                CmsSiteEntryBean entry = getUri(cms, cms.getRequestContext().getUri());
                if (entry != null) {
                    // read the resource
                    resource = cms.readResource(entry.getResourceId());
                    // set the element
                    req.setAttribute(CmsADEManager.ATTR_CURRENT_ELEMENT, new CmsSiteEntryBean(
                        entry.getResourceId(),
                        entry.getName(),
                        entry.getExtension(),
                        entry.getTitle(),
                        entry.getProperties(),
                        null));
                }
            } catch (Throwable e) {
                String uri = cms.getRequestContext().getUri();
                CmsMessageContainer msg = Messages.get().container(Messages.ERR_SITEMAP_1, uri);
                if (LOG.isErrorEnabled()) {
                    LOG.error(msg.key(), e);
                }
                throw new CmsResourceInitException(msg, e);
            }
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
        } else {
            m_missingUrisOffline.clear();
            m_sitemapsOffline.clear();
            m_urisOffline.clear();
        }
    }

    /**
     * Returns the site entry for the given URI, or <code>null</code> if not found.<p> 
     * 
     * @param cms the current CMS context
     * @param uri the URI to look for
     * @param online if online or offline
     * 
     * @return the site entry for the given URI, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsSiteEntryBean getEntry(CmsObject cms, String uri, boolean online) throws CmsException {

        // find the resource
        CmsSitemapBean sitemap = getSitemap(cms, cms.getRequestContext().getSiteRoot(), online);
        // normalize the uri
        String sitePath = normalizePath(uri);
        LinkedList<String> entryPaths = new LinkedList<String>(CmsStringUtil.splitAsList(sitePath, "/"));

        if (entryPaths.isEmpty()) {
            // special case for '/'
            CmsSiteEntryBean entry = sitemap.getSiteEntries().get(0);
            entry.setPosition(0);
            return entry;
        }

        String uriPath = "";
        List<CmsSiteEntryBean> subEntries = sitemap.getSiteEntries().get(0).getSubEntries();
        boolean finished = false;
        while (!finished) {
            String name = entryPaths.removeFirst();
            uriPath += "/" + name;
            // check the missed cache
            Boolean missing;
            if (online) {
                missing = m_missingUrisOnline.get(uriPath.substring(1));
            } else {
                missing = m_missingUrisOffline.get(uriPath.substring(1));
            }
            if (missing != null) {
                // already marked as not found
                return null;
            }
            int position = 0;
            for (; position < subEntries.size(); position++) {
                CmsSiteEntryBean entry = subEntries.get(position);
                if (!entry.getName().equals(name)) {
                    // no match
                    continue;
                }
                if (entryPaths.isEmpty()) {
                    // if nothing left, we got a match
                    entry.setPosition(position);
                    return entry;
                } else {
                    // continue with sub-entries
                    subEntries = entry.getSubEntries();
                    if (subEntries.isEmpty()) {
                        // check sitemap property
                        String sitemapPath = entry.getProperties().get(CmsSitemapResourceHandler.PROPERTY_SITEMAP);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(sitemapPath)) {
                            // switch to sub-sitemap
                            sitemapPath = cms.getRequestContext().addSiteRoot(sitemapPath);
                            sitemap = getSitemap(cms, sitemapPath, online);
                            if (sitemap == null) {
                                // no sitemap found
                                return null;
                            }
                            subEntries = sitemap.getSiteEntries();
                        }
                    }
                    finished = subEntries.isEmpty();
                }
                break;
            }
            if (position == subEntries.size()) {
                // not found
                finished = true;
            }
        }

        return null;
    }

    /**
     * Returns the sitemap bean for the given path.<p> 
     * 
     * @param cms the current cms context
     * @param path the sitemap path, or the site root
     * @param online if online or offline, has to be equals to cms.getRequestContext().currentProject().isOnlineProject()
     * 
     * @return the sitemap bean for the given path
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsSitemapBean getSitemap(CmsObject cms, String path, boolean online) throws CmsException {

        CmsFile sitemap;
        // check the cache
        if (online) {
            sitemap = m_sitemapsOnline.get(path);
        } else {
            sitemap = m_sitemapsOffline.get(path);
        }
        if (sitemap != null) {
            // found in cache
            return CmsXmlSitemapFactory.unmarshal(cms, sitemap).getSitemap(cms, cms.getRequestContext().getLocale());
        }

        // not found in cache
        String sitemapPath = normalizePath(path);
        if (path.equals(normalizePath(cms.getRequestContext().getSiteRoot()))) {
            // read the sitemap property from the site folder
            sitemapPath = cms.readPropertyObject("/", CmsPropertyDefinition.PROPERTY_SITEMAP, false).getValue(
                "/sitemap");
            sitemapPath = normalizePath(cms.getRequestContext().addSiteRoot(sitemapPath));
        }

        try {
            // try to read the sitemap file
            sitemap = cms.readFile(sitemapPath);
        } catch (Exception e) {
            // can happen in case of bad configuration
            LOG.error(e.getLocalizedMessage(), e);
            // cache the missed attempt
            if (online) {
                m_missingUrisOnline.put(path, Boolean.TRUE);
            } else {
                m_missingUrisOffline.put(path, Boolean.TRUE);
            }
            // no sitemap found
            return null;
        }

        // found: cache the sitemap
        if (online) {
            m_sitemapsOnline.put(sitemapPath, sitemap);
        } else {
            m_sitemapsOffline.put(sitemapPath, sitemap);
        }
        return CmsXmlSitemapFactory.unmarshal(cms, sitemap).getSitemap(cms, cms.getRequestContext().getLocale());
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

        if (!CmsResourceTypeXmlSitemap.isSitemap(resource)) {
            return;
        }
        // flush all uri's
        m_urisOffline.clear();
        m_missingUrisOffline.clear();
        // this is a sitemap, so remove it
        m_sitemapsOffline.remove(normalizePath(resource.getRootPath()));
        // this is the case of site sitemaps, value lookup
        Iterator<Map.Entry<String, CmsFile>> i = m_sitemapsOffline.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, CmsFile> e = i.next();
            if (resource.getStructureId().equals(e.getValue().getStructureId())) {
                i.remove();
                break;
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
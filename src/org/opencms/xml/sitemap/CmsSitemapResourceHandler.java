/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapResourceHandler.java,v $
 * Date   : $Date: 2009/11/10 16:42:18 $
 * Version: $Revision: 1.2 $
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
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.util.CmsCollectionsGenericWrapper;
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
 * @version $Revision: 1.2 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapResourceHandler implements I_CmsResourceInit, I_CmsEventListener {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapResourceHandler.class);

    /** Cache for missing offline URIs. */
    private Map<String, Boolean> m_missingUrisOffline;

    /** Cache for missing online URIs. */
    private Map<String, Boolean> m_missingUrisOnline;

    /** Cache for offline sitemaps. */
    private Map<String, CmsFile> m_sitemapsOffline;

    /** Cache for online sitemaps. */
    private Map<String, CmsFile> m_sitemapsOnline;

    /** Cache for offline URIs. */
    private Map<String, CmsSiteEntryBean> m_urisOffline;

    /** Cache for online URIs. */
    private Map<String, CmsSiteEntryBean> m_urisOnline;

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
     * Initialization routine.<p>
     */
    public void init() {

        CmsMemoryMonitor memMonitor = OpenCms.getMemoryMonitor();
        Map<String, CmsFile> lruMapCntPage = CmsCollectionsGenericWrapper.createLRUMap(128);
        m_sitemapsOffline = Collections.synchronizedMap(lruMapCntPage);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".sitemapsOffline", lruMapCntPage);
        }

        lruMapCntPage = CmsCollectionsGenericWrapper.createLRUMap(128);
        m_sitemapsOnline = Collections.synchronizedMap(lruMapCntPage);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".sitemapsOnline", lruMapCntPage);
        }

        Map<String, CmsSiteEntryBean> lruMapUri = CmsCollectionsGenericWrapper.createLRUMap(128);
        m_urisOffline = Collections.synchronizedMap(lruMapUri);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".urisOffline", lruMapUri);
        }

        lruMapUri = CmsCollectionsGenericWrapper.createLRUMap(128);
        m_urisOnline = Collections.synchronizedMap(lruMapUri);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".urisOnline", lruMapUri);
        }

        Map<String, Boolean> lruMapMissed = CmsCollectionsGenericWrapper.createLRUMap(128);
        m_missingUrisOffline = Collections.synchronizedMap(lruMapMissed);
        if (memMonitor != null) {
            memMonitor.register(CmsSitemapResourceHandler.class.getName() + ".missingUrisOffline", lruMapMissed);
        }

        lruMapMissed = CmsCollectionsGenericWrapper.createLRUMap(128);
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
                CmsSiteEntryBean entry = getUri(cms, req);
                if (entry != null) {
                    // read the resource
                    resource = cms.readResource(entry.getResourceId());
                    // set the element
                    req.setAttribute(CmsADEManager.ATTR_CURRENT_ELEMENT, new CmsSiteEntryBean(
                        entry.getResourceId(),
                        entry.getName(),
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
     * Returns the sitemap bean for the given path.<p> 
     * 
     * @param cms the current cms context
     * @param path the sitemap path, or the site root
     * @param req the current request
     * 
     * @return the sitemap bean for the given path
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsSitemapBean getSitemap(CmsObject cms, String path, HttpServletRequest req) throws CmsException {

        boolean online = cms.getRequestContext().currentProject().isOnlineProject();
        CmsFile sitemap;
        if (online) {
            sitemap = m_sitemapsOnline.get(path);
        } else {
            sitemap = m_sitemapsOffline.get(path);
        }
        if (sitemap == null) {
            if (path.equals(normalizePath(cms.getRequestContext().getSiteRoot()))) {
                // read the sitemap property from the site folder
                String sitemapPath = cms.readPropertyObject("/", CmsPropertyDefinition.PROPERTY_SITEMAP, false).getValue(
                    "/sitemap");
                try {
                    sitemap = cms.readFile(sitemapPath);
                } catch (Exception e) {
                    // should never happen
                    LOG.error(e.getLocalizedMessage(), e);
                    return null;
                }
                sitemapPath = normalizePath(cms.getRequestContext().addSiteRoot(sitemapPath));
                if (online) {
                    m_sitemapsOnline.put(sitemapPath, sitemap);
                } else {
                    m_sitemapsOffline.put(sitemapPath, sitemap);
                }
            } else {
                try {
                    sitemap = cms.readFile(path);
                } catch (Exception e) {
                    // should never happen
                    LOG.error(e.getLocalizedMessage(), e);
                    return null;
                }
            }
            if (online) {
                m_sitemapsOnline.put(path, sitemap);
            } else {
                m_sitemapsOffline.put(path, sitemap);
            }
        }
        return CmsXmlSitemapFactory.unmarshal(cms, sitemap, req).getSitemap(cms, cms.getRequestContext().getLocale());
    }

    /**
     * Returns the resource for the current URI, or <code>null</code> if not found.<p> 
     * 
     * @param cms the current cms context
     * @param req the current request
     * 
     * @return the resource for the current URI, or <code>null</code> if not found
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsSiteEntryBean getUri(CmsObject cms, HttpServletRequest req) throws CmsException {

        String path = normalizePath(cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri()));
        // check the cache
        CmsSiteEntryBean uriEntry;
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

        // find the resource
        CmsSitemapBean sitemap = getSitemap(cms, normalizePath(cms.getRequestContext().getSiteRoot()), req);
        if (sitemap == null) {
            // cache the missed attempt
            if (online) {
                m_missingUrisOnline.put(path, Boolean.TRUE);
            } else {
                m_missingUrisOffline.put(path, Boolean.TRUE);
            }
            // no sitemap found
            return null;
        }
        String sitePath = normalizePath(cms.getRequestContext().getUri());
        // normalize the path
        LinkedList<String> entryPaths = new LinkedList<String>(CmsStringUtil.splitAsList(sitePath, "/"));

        if (entryPaths.isEmpty()) {
            // special case for 'index' or 'home'
            uriEntry = sitemap.getSiteEntries().get(0);
        } else {
            String uriPath = "";
            List<CmsSiteEntryBean> subEntries = sitemap.getSiteEntries().get(0).getSubEntries();
            boolean finished = false;
            while (!finished) {
                String name = entryPaths.removeFirst();
                uriPath += "/" + name;
                // check the missed cache
                if (online) {
                    missing = m_missingUrisOnline.get(uriPath.substring(1));
                } else {
                    missing = m_missingUrisOffline.get(uriPath.substring(1));
                }
                if (missing != null) {
                    // already marked as not found
                    return null;
                }
                int i = 0;
                for (; i < subEntries.size(); i++) {
                    CmsSiteEntryBean entry = subEntries.get(i);
                    if (!entry.getName().equals(name)) {
                        // no match
                        continue;
                    }
                    if (entryPaths.isEmpty()) {
                        // if nothing left, we got a match
                        uriEntry = entry;
                        finished = true;
                    } else {
                        // continue with sub-entries
                        subEntries = entry.getSubEntries();
                        if (subEntries.isEmpty()) {
                            // check sitemap property
                            String sitemapPath = entry.getProperties().get(CmsSiteEntryBean.PROPERTY_SITEMAP);
                            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(sitemapPath)) {
                                // switch to sub-sitemap
                                sitemapPath = normalizePath(cms.getRequestContext().addSiteRoot(sitemapPath));
                                sitemap = getSitemap(cms, sitemapPath, req);
                                if (sitemap == null) {
                                    // cache the missed attempt
                                    if (online) {
                                        m_missingUrisOnline.put(path, Boolean.TRUE);
                                    } else {
                                        m_missingUrisOffline.put(path, Boolean.TRUE);
                                    }
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
                if (i == subEntries.size()) {
                    // not found
                    finished = true;
                }
            }
        }
        if (uriEntry != null) {
            // cache the found resource
            if (online) {
                m_urisOnline.put(path, uriEntry);
            } else {
                m_urisOffline.put(path, uriEntry);
            }
        } else {
            // cache the missed attempt
            if (online) {
                m_missingUrisOnline.put(path, Boolean.TRUE);
            } else {
                m_missingUrisOffline.put(path, Boolean.TRUE);
            }
        }
        return uriEntry;
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
     * The resource is removed both from the resource and sibling caches.
     * 
     * @param resource the resource
     */
    protected void uncacheResource(CmsResource resource) {

        if (resource == null) {
            LOG.warn(Messages.get().container(Messages.LOG_WARN_UNCACHE_NULL_0));
            return;
        }

        // flush all uri's
        m_urisOffline.clear();
        m_missingUrisOffline.clear();
        if (CmsResourceTypeXmlSitemap.isSitemap(resource)) {
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
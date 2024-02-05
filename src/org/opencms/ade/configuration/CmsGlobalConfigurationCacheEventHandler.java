/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 *
 * This event handler manages cache instances which are instances of the interface {@link I_CmsGlobalConfigurationCache}.
 * It keeps a list of cache instance pairs, each containing one cache for the online mode and one for the offline mode,
 * and handles events caused by changed resources by notifying the cache instances.
 *
 * Note that *all* changed resources will get passed to the underlying cache instances, so those instances will need to check
 * whether the resource passed into the update or remove methods is actually a resource with which the cache instance is concerned.<p>
 *
 * This class should be used if you have an indefinite number of configuration files at arbitrary locations in the VFS.
 * If you need to cache e.g. a single configuration file with a known, fixed path, using {@link org.opencms.cache.CmsVfsMemoryObjectCache} is
 * easier.<p>
 */
public class CmsGlobalConfigurationCacheEventHandler implements I_CmsEventListener {

    /**
     * A pair of cache instances, one for the offline mode and one for the online mode.<p>
     */
    private class CachePair {

        /** A name for debugging. */
        @SuppressWarnings("unused")
        private String m_debugName;

        /** The offline cache instance. */
        private I_CmsGlobalConfigurationCache m_offlineCache;

        /** The online cache instance. */
        private I_CmsGlobalConfigurationCache m_onlineCache;

        /**
         * Creates a new cache pair.<p>
         *
         * @param offlineCache the offline cache instance
         * @param onlineCache the online cache instance
         * @param debugName the name for debugging
         */
        public CachePair(
            I_CmsGlobalConfigurationCache offlineCache,
            I_CmsGlobalConfigurationCache onlineCache,
            String debugName) {

            m_offlineCache = offlineCache;
            m_onlineCache = onlineCache;
            m_debugName = debugName;
        }

        /**
         * Gets the offline cache instance.<p>
         *
         * @return the offline cache instance
         */
        public I_CmsGlobalConfigurationCache getOfflineCache() {

            return m_offlineCache;
        }

        /**
         * Gets the online cache instance.<p>
         *
         * @return the online cache instance
         */
        public I_CmsGlobalConfigurationCache getOnlineCache() {

            return m_onlineCache;
        }
    }

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGlobalConfigurationCacheEventHandler.class);

    /** The list of cache pairs. */
    private List<CachePair> m_caches = new ArrayList<CachePair>();

    /** An online CMS object. */
    private CmsObject m_onlineCms;

    /** Creates a new cache event handler.
     *
     * @param onlineCms an online CMS object
     **/
    public CmsGlobalConfigurationCacheEventHandler(CmsObject onlineCms) {

        m_onlineCms = onlineCms;
    }

    /**
     * Adds a new pair of cache instances which should be managed by this event handler.<p>
     *
     * @param offlineCache the offline cache instance
     * @param onlineCache the online cache instance
     * @param debugName an identifier used for debugging
     */
    public void addCache(
        I_CmsGlobalConfigurationCache offlineCache,
        I_CmsGlobalConfigurationCache onlineCache,
        String debugName) {

        CachePair cachePair = new CachePair(offlineCache, onlineCache, debugName);
        m_caches.add(cachePair);
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        CmsResource resource = null;
        List<CmsResource> resources = null;
        List<Object> irrelevantChangeTypes = new ArrayList<Object>();
        irrelevantChangeTypes.add(Integer.valueOf(CmsDriverManager.NOTHING_CHANGED));
        irrelevantChangeTypes.add(Integer.valueOf(CmsDriverManager.CHANGED_PROJECT));
        //System.out.println();
        switch (event.getType()) {
            case I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED:
            case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
            case I_CmsEventListener.EVENT_RESOURCE_CREATED:
                //System.out.print(getEventName(event.getType()));
                Object change = event.getData().get(I_CmsEventListener.KEY_CHANGE);
                if ((change != null) && irrelevantChangeTypes.contains(change)) {
                    // skip lock & unlock, and project changes
                    return;
                }
                resource = (CmsResource)event.getData().get(I_CmsEventListener.KEY_RESOURCE);
                offlineCacheUpdate(resource);
                //System.out.print(" " + resource.getRootPath());
                break;
            case I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED:
                // a list of resources and all of their properties have been modified
                //System.out.print(getEventName(event.getType()));
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                for (CmsResource res : resources) {
                    offlineCacheUpdate(res);
                    //System.out.print(" " + res.getRootPath());
                }
                break;

            case I_CmsEventListener.EVENT_RESOURCE_MOVED:
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                // source, source folder, dest, dest folder
                // - OR -
                // source, dest, dest folder
                offlineCacheRemove(resources.get(0));
                offlineCacheUpdate(resources.get(resources.size() - 2));
                break;

            case I_CmsEventListener.EVENT_RESOURCE_DELETED:
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                for (CmsResource res : resources) {
                    offlineCacheRemove(res);
                }
                break;
            case I_CmsEventListener.EVENT_RESOURCES_MODIFIED:
                //System.out.print(getEventName(event.getType()));
                // a list of resources has been modified
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                for (CmsResource res : resources) {
                    offlineCacheUpdate(res);
                }
                break;
            case I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES:
                onlineCacheClear();
                break;
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                //System.out.print(getEventName(event.getType()));
                String publishIdStr = (String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID);
                if (publishIdStr != null) {
                    CmsUUID publishId = new CmsUUID(publishIdStr);
                    try {
                        List<CmsPublishedResource> publishedResources = m_onlineCms.readPublishedResources(publishId);
                        if (publishedResources.isEmpty()) {
                            // normally, the list of published resources should not be empty.
                            // If it is, the publish event is not coming from a normal publish process,
                            // so we re-initialize the whole cache to be on the safe side.
                            onlineCacheClear();
                        } else {
                            for (CmsPublishedResource res : publishedResources) {
                                if (res.getState().isDeleted()) {
                                    onlineCacheRemove(res);
                                } else {
                                    onlineCacheUpdate(res);
                                }
                            }
                        }
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                break;
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                //System.out.print(getEventName(event.getType()));
                offlineCacheClear();
                onlineCacheClear();
                break;
            case I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES:
                //System.out.print(getEventName(event.getType()));
                offlineCacheClear();
                break;
            default:
                // noop
                break;
        }
    }

    /**
     * Clears the offline caches.<p>
     */
    protected void offlineCacheClear() {

        for (CachePair cachePair : m_caches) {
            try {
                cachePair.getOfflineCache().clear();
            } catch (Throwable t) {
                LOG.error(t.getLocalizedMessage(), t);
            }
        }
    }

    /**
     * Removes a resource from the offline caches.<p>
     *
     * @param resource the resource to remove
     */
    protected void offlineCacheRemove(CmsPublishedResource resource) {

        for (CachePair cachePair : m_caches) {
            try {
                cachePair.getOfflineCache().remove(resource);
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Removes a resource from the offline caches.<p>
     *
     * @param resource the resource to remove
     */
    protected void offlineCacheRemove(CmsResource resource) {

        for (CachePair cachePair : m_caches) {
            try {
                cachePair.getOfflineCache().remove(resource);
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Updates a resource in the offline caches.<p>
     *
     * @param resource the resource to update
     */
    protected void offlineCacheUpdate(CmsPublishedResource resource) {

        for (CachePair cachePair : m_caches) {
            try {
                cachePair.getOfflineCache().update(resource);
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage());
            }
        }

    }

    /**
     * Updates a resource in the offline caches.<p>
     *
     * @param resource the resource to update
     */
    protected void offlineCacheUpdate(CmsResource resource) {

        for (CachePair cachePair : m_caches) {
            try {
                cachePair.getOfflineCache().update(resource);
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage());
            }
        }

    }

    /**
     * Clears the online caches.<p>
     */
    protected void onlineCacheClear() {

        for (CachePair cachePair : m_caches) {
            try {
                cachePair.getOnlineCache().clear();
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Removes a resource from the online caches.<p>
     *
     * @param resource the resource to remove
     */
    protected void onlineCacheRemove(CmsPublishedResource resource) {

        for (CachePair cachePair : m_caches) {
            try {
                cachePair.getOnlineCache().remove(resource);
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Removes a resource from the online caches.<p>
     *
     * @param resource the resource to remove
     */
    protected void onlineCacheRemove(CmsResource resource) {

        for (CachePair cachePair : m_caches) {
            try {
                cachePair.getOnlineCache().remove(resource);
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage());
            }
        }

    }

    /**
     * Updates a resource in the online caches.<p>
     *
     * @param resource the resource to update
     */
    protected void onlineCacheUpdate(CmsPublishedResource resource) {

        for (CachePair cachePair : m_caches) {
            try {
                cachePair.getOnlineCache().update(resource);
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage());
            }
        }

    }

    /**
     * Updates a resource in the online caches.<p>
     *
     * @param resource the resource to update
     */
    protected void onlineCacheUpdate(CmsResource resource) {

        for (CachePair cachePair : m_caches) {
            try {
                cachePair.getOnlineCache().update(resource);
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage());
            }
        }
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsContainerPageCache.java,v $
 * Date   : $Date: 2009/08/13 10:47:35 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Cache object instance for simultaneously cache online and offline items.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.6 
 */
public class CmsContainerPageCache implements I_CmsEventListener {

    /** The log to use (static for performance reasons).<p> */
    private static final Log LOG = CmsLog.getLog(CmsContainerPageCache.class);

    /**
     * Default Constructor.<p>
     */
    public CmsContainerPageCache() {

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
     * Takes care of cache synchronization and consistency.<p>
     * 
     * @param event the event to handle
     */
    public void cmsEvent(CmsEvent event) {

        CmsResource resource = null;
        List resources = null;

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED:
            case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                // a resource has been modified in a way that it *IS NOT* necessary also to clear 
                // lists of cached sub-resources where the specified resource might be contained inside.
                // all siblings are removed from the cache, too.
                resource = (CmsResource)event.getData().get("resource");
                uncacheResource(resource);
                break;

            case I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED:
                // a list of resources and all of their properties have been modified
                resources = (List)event.getData().get("resources");
                uncacheResources(resources);
                break;

            case I_CmsEventListener.EVENT_RESOURCE_DELETED:
            case I_CmsEventListener.EVENT_RESOURCES_MODIFIED:
                // a list of resources has been modified
                resources = (List)event.getData().get("resources");
                uncacheResources(resources);
                break;

            case I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES:
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                OpenCms.getMemoryMonitor().flushContainerPages(true);
                break;

            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                OpenCms.getMemoryMonitor().flushContainerPages(true);
                OpenCms.getMemoryMonitor().flushContainerPages(false);
                break;

            case I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES:
                OpenCms.getMemoryMonitor().flushContainerPages(false);
                break;

            default:
                // noop
                break;
        }
    }

    /**
     * Returns the cached JSON object for the given resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource to look for
     *  
     * @return the cached JSON object
     */
    public JSONObject get(CmsObject cms, CmsResource resource) {

        if (cms.getRequestContext().currentProject().isOnlineProject()) {
            return lookupOnline(resource.getStructureId().toString());
        } else {
            return lookupOffline(resource.getStructureId().toString());
        }
    }

    /**
     * Sets the cached JSON object for the given resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource to set the cache for
     * @param object the JSON object to cache
     */
    public void set(CmsObject cms, CmsResource resource, JSONObject object) {

        if (cms.getRequestContext().currentProject().isOnlineProject()) {
            setCacheOnline(resource.getStructureId().toString(), object);
        } else {
            setCacheOffline(resource.getStructureId().toString(), object);
        }
    }

    /**
     * Lookups for the given key in the offline cache.<p>
     * 
     * @param cacheKey key to lookup
     * 
     * @return the cached object or <code>null</code> if not cached
     */
    protected JSONObject lookupOffline(String cacheKey) {

        JSONObject retValue = OpenCms.getMemoryMonitor().getCacheContainerPage(cacheKey, false);
        if (LOG.isDebugEnabled()) {
            if (retValue == null) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_DEBUG_CACHE_MISSED_OFFLINE_1,
                    new Object[] {cacheKey}));

            } else {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_DEBUG_CACHE_MATCHED_OFFLINE_2,
                    new Object[] {cacheKey, retValue}));
            }
        }
        return retValue;
    }

    /**
     * Lookups for the given key in the online cache.<p>
     * 
     * @param cacheKey key to lookup
     * 
     * @return the cached object or <code>null</code> if not cached
     */
    protected JSONObject lookupOnline(String cacheKey) {

        JSONObject retValue = OpenCms.getMemoryMonitor().getCacheContainerPage(cacheKey, true);
        if (LOG.isDebugEnabled()) {
            if (retValue == null) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_DEBUG_CACHE_MISSED_ONLINE_1,
                    new Object[] {cacheKey}));

            } else {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_DEBUG_CACHE_MATCHED_ONLINE_2,
                    new Object[] {cacheKey, retValue}));
            }
        }
        return retValue;
    }

    /**
     * Sets a new cached value for the given key in the offline project.<p>
     * 
     * @param cacheKey key to lookup
     * @param data the value to cache
     */
    protected void setCacheOffline(String cacheKey, JSONObject data) {

        OpenCms.getMemoryMonitor().cacheContainerPages(cacheKey, data, false);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_DEBUG_CACHE_SET_OFFLINE_2,
                new Object[] {cacheKey, data}));
        }
    }

    /**
     * Sets a new cached value for the given key in the online project.<p>
     * 
     * @param cacheKey key to lookup
     * @param data the value to cache
     */
    protected void setCacheOnline(String cacheKey, JSONObject data) {

        OpenCms.getMemoryMonitor().cacheContainerPages(cacheKey, data, true);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(
                Messages.LOG_DEBUG_CACHE_SET_ONLINE_2,
                new Object[] {cacheKey, data}));
        }
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

        // remove the resource cached by it's structure ID
        OpenCms.getMemoryMonitor().uncacheContainerPage(resource.getStructureId().toString(), false);
    }

    /**
     * Removes a bunch of cached resources from the offline cache, but keeps their properties
     * in the cache.<p>
     * 
     * @param resources a list of resources
     * 
     * @see #uncacheResource(CmsResource)
     */
    protected void uncacheResources(List resources) {

        if (resources == null) {
            LOG.warn(Messages.get().container(Messages.LOG_WARN_UNCACHE_NULL_0));
            return;
        }

        for (int i = 0, n = resources.size(); i < n; i++) {
            // remove the resource
            uncacheResource((CmsResource)resources.get(i));
        }
    }
}

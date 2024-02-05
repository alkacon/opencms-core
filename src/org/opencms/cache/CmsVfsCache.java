/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.cache;

import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.List;

/**
 * Configurable VFS based cache, for caching objects related to offline/online resources.<p>
 *
 * @since 7.6
 */
public abstract class CmsVfsCache implements I_CmsEventListener {

    /**
     * Initializes the cache. Only intended to be called during startup.<p>
     */
    protected CmsVfsCache() {

        // empty
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        CmsResource resource = null;
        List<CmsResource> resources = null;

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED:
            case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                Object change = event.getData().get(I_CmsEventListener.KEY_CHANGE);
                if ((change != null) && change.equals(Integer.valueOf(CmsDriverManager.NOTHING_CHANGED))) {
                    // skip lock & unlock
                    return;
                }
                // a resource has been modified in a way that it *IS NOT* necessary also to clear
                // lists of cached sub-resources where the specified resource might be contained inside.
                resource = (CmsResource)event.getData().get(I_CmsEventListener.KEY_RESOURCE);
                uncacheResource(resource);
                break;

            case I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED:
                // a list of resources and all of their properties have been modified
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                uncacheResources(resources);
                break;

            case I_CmsEventListener.EVENT_RESOURCE_MOVED:
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
     * Clean up at shutdown time. Only intended to be called at system shutdown.<p>
     *
     * @see org.opencms.main.OpenCmsCore#shutDown
     */
    public void shutdown() {

        if (OpenCms.getMemoryMonitor() != null) {
            // prevent accidental calls
            return;

        }
        flush(true);
        flush(false);
    }

    /**
     * Flushes the caches.<p>
     *
     * @param online if to flush the online or offline caches
     */
    protected abstract void flush(boolean online);

    /**
     * Adds this instance as an event listener to the CMS event manager.<p>
     */
    protected void registerEventListener() {

        OpenCms.addCmsEventListener(
            this,
            new int[] {
                I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED,
                I_CmsEventListener.EVENT_RESOURCE_MODIFIED,
                I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
                I_CmsEventListener.EVENT_RESOURCE_MOVED,
                I_CmsEventListener.EVENT_RESOURCE_DELETED,
                I_CmsEventListener.EVENT_PUBLISH_PROJECT,
                I_CmsEventListener.EVENT_CLEAR_CACHES,
                I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES,
                I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES});
    }

    /**
     * Removes a cached resource from the cache.<p>
     *
     * @param resource the resource
     */
    protected abstract void uncacheResource(CmsResource resource);

    /**
     * Removes a bunch of cached resources from the cache.<p>
     *
     * @param resources a list of resources
     *
     * @see #uncacheResource(CmsResource)
     */
    protected void uncacheResources(List<CmsResource> resources) {

        if (resources == null) {
            return;
        }
        for (int i = 0, n = resources.size(); i < n; i++) {
            // remove the resource
            uncacheResource(resources.get(i));
        }
    }
}

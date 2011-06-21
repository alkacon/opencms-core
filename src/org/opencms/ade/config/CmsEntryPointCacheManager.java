/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.config;

import org.opencms.ade.config.CmsEntryPointCache.EntryPointFolder;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsCollectionsGenericWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * The class which should be used to access the entry point cache(s).<p>
 * 
 * @since 8.0.0
 */
public class CmsEntryPointCacheManager {

    /**
     * The event handler class used for flushing the entry point caches.<p>
     */
    protected class EventHandler implements I_CmsEventListener {

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
                    if ((change != null) && change.equals(new Integer(CmsDriverManager.NOTHING_CHANGED))) {
                        // skip lock & unlock
                        return;
                    }
                    // a resource has been modified in a way that it *IS NOT* necessary also to clear 
                    // lists of cached sub-resources where the specified resource might be contained inside.
                    resource = (CmsResource)event.getData().get(I_CmsEventListener.KEY_RESOURCE);
                    if (m_offlineCache.checkFlush(resource)) {
                        fireFlush(false);
                    }
                    break;

                case I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED:
                    // a list of resources and all of their properties have been modified
                    resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                    for (CmsResource res : resources) {
                        if (m_offlineCache.checkFlush(res)) {
                            fireFlush(false);
                        }
                    }
                    break;

                case I_CmsEventListener.EVENT_RESOURCE_MOVED:
                case I_CmsEventListener.EVENT_RESOURCE_DELETED:
                case I_CmsEventListener.EVENT_RESOURCES_MODIFIED:
                    // a list of resources has been modified
                    resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                    for (CmsResource res : resources) {
                        if (m_offlineCache.checkFlush(res)) {
                            fireFlush(false);
                        }
                    }
                    break;

                case I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES:
                case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                    m_onlineCache.flush();
                    fireFlush(true);
                    break;

                case I_CmsEventListener.EVENT_CLEAR_CACHES:
                    m_offlineCache.flush();
                    fireFlush(false);
                    m_onlineCache.flush();
                    fireFlush(true);
                    break;

                case I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES:
                    m_offlineCache.flush();
                    fireFlush(false);
                    break;

                default:
                    // noop
                    break;
            }

        }

    }

    /** The offline entry point cache. */
    protected CmsEntryPointCache m_offlineCache;

    /** The online entry point cache. */
    protected CmsEntryPointCache m_onlineCache;

    /** The cache flush handlers for this cache. */
    private List<I_CmsCacheFlushHandler> m_flushHandlers = new ArrayList<I_CmsCacheFlushHandler>();

    /**
     * Creates a new entry point cache manager.<p>
     * 
     * @param cms the CMS context to use
     *  
     * @throws CmsException if something goes wrong
     */
    public CmsEntryPointCacheManager(CmsObject cms)
    throws CmsException {

        m_offlineCache = new CmsEntryPointCache(OpenCms.initCmsObject(cms), false);
        m_onlineCache = new CmsEntryPointCache(OpenCms.initCmsObject(cms), true);
        OpenCms.getEventManager().addCmsEventListener(new EventHandler());

    }

    /**
     * Returns the list of all entry points.<p>
     * 
     * @param cms the current CMS context
     *  
     * @return the list of entry points 
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<EntryPointFolder> getEntryPoints(CmsObject cms) throws CmsException {

        return getCache(cms).getEntryPoints(cms);
    }

    /**
     * Looks up the entry points for a given path.<p>
     * 
     * The beans representing the entry points are returned in reverse order, i.e. the entry point folder closest to the 
     * given path will be the first in the returned list.<p>
     *   
     * @param cms the cms context 
     * @param path the path for which the entry points should be looked up
     * 
     * @return the list of entry points for the path, in reverse order 
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<EntryPointFolder> lookup(CmsObject cms, String path) throws CmsException {

        return getCache(cms).lookup(cms, path);
    }

    /**
     * Returns the offline or online entry point cache.<p>
     * 
     * @param online if true, returns  the online cache, else the offline one
     *  
     * @return the offline or online entry point cache  
     */
    protected CmsEntryPointCache getCache(boolean online) {

        return online ? m_onlineCache : m_offlineCache;
    }

    /**
     * Gets the online or offline entry point cache, depending on the project of a CMS context.<p> 
     * 
     * @param cms the CMS context 
     * 
     * @return the online entry point cache if the CMS context is in the Online project, else the offline entry point cache
     */
    protected CmsEntryPointCache getCache(CmsObject cms) {

        return getCache(cms.getRequestContext().getCurrentProject().isOnlineProject());
    }

    /**
     * Adds a cache flush handler.<p>
     * 
     * @param flushHandler the cache flush handler
     */
    public void addFlushHandler(I_CmsCacheFlushHandler flushHandler) {

        m_flushHandlers.add(flushHandler);
    }

    /**
     * Notifies the cache flush handlers of a cache flush.<p>
     * 
     * @param online true if the online cache is being flushed 
     */
    protected void fireFlush(boolean online) {

        for (I_CmsCacheFlushHandler handler : m_flushHandlers) {
            handler.onFlushCache(online);
        }
    }

}

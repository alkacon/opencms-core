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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;

import org.apache.commons.logging.Log;

/**
 * A singleton memory cache, that stores objects related with keys.<p>
 *
 * This cache listens to the {@link I_CmsEventListener#EVENT_CLEAR_CACHES} event only.<p>
 *
 * @since 6.2.3
 */
public final class CmsMemoryObjectCache implements I_CmsEventListener {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMemoryObjectCache.class);

    /** A cache that maps VFS resource names to Objects. */
    private static CmsMemoryObjectCache m_instance;

    /**
     * Constructor, creates a new CmsVfsMemoryObjectCache.<p>
     */
    private CmsMemoryObjectCache() {

        // register the event listeners
        registerEventListener();
    }

    /**
     * Returns the singleton memory Object cache instance.<p>
     *
     * @return the singleton memory Object cache instance
     */
    public static CmsMemoryObjectCache getInstance() {

        if (m_instance == null) {
            m_instance = new CmsMemoryObjectCache();
        }
        return m_instance;
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                // flush cache
                OpenCms.getMemoryMonitor().flushCache(CmsMemoryMonitor.CacheType.MEMORY_OBJECT);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        org.opencms.xml.Messages.get().getBundle().key(
                            org.opencms.xml.Messages.LOG_ERR_FLUSHED_CACHES_0));
                }
                break;
            default:
                // no operation
        }
    }

    /**
     * Returns an object from the cache.<p>
     *
     * @param owner the owner class of the cached object (used to ensure keys don't overlap)
     * @param key the key to lookup the object for
     *
     * @return an object from the cache, or <code>null</code> if no object matches the given key
     */
    public Object getCachedObject(Class<?> owner, String key) {

        key = owner.getName().concat(key);
        return OpenCms.getMemoryMonitor().getCachedMemObject(key);
    }

    /**
     * Puts an object into the cache.<p>
     *
     * @param owner the owner class of the cached object (used to ensure keys don't overlap)
     * @param key the key to store the object at
     * @param value the object to store
     */
    public void putCachedObject(Class<?> owner, String key, Object value) {

        key = owner.getName().concat(key);
        OpenCms.getMemoryMonitor().cacheMemObject(key, value);
    }

    /**
     * Registers all required event listeners.<p>
     */
    protected void registerEventListener() {

        // register this object as event listener
        OpenCms.addCmsEventListener(this, new int[] {I_CmsEventListener.EVENT_CLEAR_CACHES});
    }
}
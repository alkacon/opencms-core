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

package org.opencms.monitor;

import org.opencms.file.CmsGroup;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Two-level cache for lists of user groups that doesn't just use a flat key string, but instead a pair of two keys: first, the user id, and second,
 * other parameters for identifying the cache entry. This allows us to specifically flush the cache for only one user.
 */
public class CmsGroupListCache implements I_CmsMemoryMonitorable {

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGroupListCache.class);

    /** The internal cache used. */
    private LoadingCache<CmsUUID, Map<String, List<CmsGroup>>> m_internalCache;

    /**
     * Creates a new cache instance.
     *
     * @param size the maximum size
     */
    public CmsGroupListCache(int size) {

        m_internalCache = CacheBuilder.newBuilder().concurrencyLevel(CmsMemoryMonitor.CONCURRENCY_LEVEL).maximumSize(
            size).build(new CacheLoader<CmsUUID, Map<String, List<CmsGroup>>>() {

                @Override
                public Map<String, List<CmsGroup>> load(CmsUUID key) throws Exception {

                    return new ConcurrentHashMap<>();
                }
            });
    }

    /**
     * Removes all cache entries.
     */
    public void clear() {

        if (LOG.isInfoEnabled()) {
            if (LOG.isDebugEnabled()) {
                // when DEBUG level is enabled, log a dummy exception to generate a stack trace
                LOG.debug("CmsGroupListCache.clear() called", new Exception("dummy exception"));
            } else {
                LOG.info("CmsGroupListCache.clear() called");
            }
        }
        m_internalCache.invalidateAll();
    }

    /**
     * Removes the cache entries for the given user id.
     *
     * @param idKey the user id
     */
    public void clearUser(CmsUUID idKey) {

        if (idKey != null) {
            m_internalCache.invalidate(idKey);
        }
    }

    /**
     * Gets the cached user groups for the given combination of keys, or null if nothing is cached.
     *
     * @param userId the user id
     * @param subKey the subkey
     *
     * @return the groups for the given combination of keys
     */
    public List<CmsGroup> get(CmsUUID userId, String subKey) {

        Map<String, List<CmsGroup>> entriesForUser = m_internalCache.getIfPresent(userId);
        if (entriesForUser == null) {
            return null;
        }
        List<CmsGroup> result = entriesForUser.get(subKey);
        if (result != null) {
            result = Collections.unmodifiableList(result);
        }
        return result;

    }

    /**
     * @see org.opencms.monitor.I_CmsMemoryMonitorable#getMemorySize()
     */
    public int getMemorySize() {

        return (int)CmsMemoryMonitor.getValueSize(m_internalCache.asMap());
    }

    /**
     * Caches a new value for the given combination of keys.
     *
     * @param userId the user id
     * @param subKey the sub-key
     * @param groups the value to cache
     */
    public void put(CmsUUID userId, String subKey, List<CmsGroup> groups) {

        m_internalCache.getUnchecked(userId).put(subKey, new ArrayList<>(groups));
    }

    /**
     * Returns the number of user ids for which group lists are cached.
     *
     * @return the number of user ids for which group lists are cached
     */
    public int size() {

        return (int)m_internalCache.size();
    }

}

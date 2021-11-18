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

    /**
     * Internal entry which stores the cached data for a specific user id.
     */
    class Entry implements I_CmsMemoryMonitorable {

        /** Cache for group lists. */
        private ConcurrentHashMap<String, List<CmsGroup>> m_groupCache = new ConcurrentHashMap<>();

        /** Cache for role memberships. */
        private ConcurrentHashMap<String, Boolean> m_hasRoleCache = new ConcurrentHashMap<>();

        /**
         * Gets the group list cache map.
         *
         * @return the group list cache
         */
        public ConcurrentHashMap<String, List<CmsGroup>> getGroupCache() {

            return m_groupCache;
        }

        /**
         * Gets the 'hasRole' cache map.
         *
         * @return the 'hasRole' cache
         */
        public ConcurrentHashMap<String, Boolean> getHasRoleCache() {

            return m_hasRoleCache;
        }

        /**
         * @see org.opencms.monitor.I_CmsMemoryMonitorable#getMemorySize()
         */
        public int getMemorySize() {

            return (int)(CmsMemoryMonitor.getValueSize(m_groupCache) + CmsMemoryMonitor.getValueSize(m_hasRoleCache));
        }

    }

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGroupListCache.class);

    /** The internal cache used. */
    private LoadingCache<CmsUUID, Entry> m_internalCache;

    /**
     * Creates a new cache instance.
     *
     * @param size the maximum size
     */
    public CmsGroupListCache(int size) {

        m_internalCache = CacheBuilder.newBuilder().concurrencyLevel(CmsMemoryMonitor.CONCURRENCY_LEVEL).maximumSize(
            size).build(new CacheLoader<CmsUUID, Entry>() {

                @Override
                public Entry load(CmsUUID key) throws Exception {

                    return new Entry();
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
    public List<CmsGroup> getGroups(CmsUUID userId, String subKey) {

        Entry userEntry = m_internalCache.getIfPresent(userId);
        if (userEntry == null) {
            return null;
        }
        List<CmsGroup> result = userEntry.getGroupCache().get(subKey);
        if (result != null) {
            result = Collections.unmodifiableList(result);
        }
        return result;

    }

    /**
     * Gets the cached role membership for the given role key, or null if nothing is cached.
     *
     * @param userId the user id
     * @param roleKey the role key
     *
     * @return the cached role membership
     */
    public Boolean getHasRole(CmsUUID userId, String roleKey) {

        Entry userEntry = m_internalCache.getIfPresent(userId);
        if (userEntry == null) {
            return null;
        }
        return userEntry.getHasRoleCache().get(roleKey);
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

        m_internalCache.getUnchecked(userId).getGroupCache().put(subKey, new ArrayList<>(groups));
    }

    /**
     * Caches the role membership for the given user id and role key.
     *
     * @param userId the user id
     * @param roleKey the role key
     * @param value the role membership value
     */
    public void setHasRole(CmsUUID userId, String roleKey, Boolean value) {

        m_internalCache.getUnchecked(userId).getHasRoleCache().put(roleKey, value);
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

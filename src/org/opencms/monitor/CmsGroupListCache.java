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
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsRole;
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
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

/**
 * Cache for users' groups and data derived from those groups, like role membership.
 *
 * <p>The cache can be either flushed completely, or just for a single user id.
 * The data for a user must be flushed when their group membership changes.
 *
 */
public class CmsGroupListCache implements I_CmsMemoryMonitorable {

    /**
     * Internal entry which stores the cached data for a specific user id.<p>
     */
    class Entry implements I_CmsMemoryMonitorable {

        /** Bare roles, with no OU information. */
        private volatile List<CmsRole> m_bareRoles;

        /** Cache for group lists. */
        private Map<String, List<CmsGroup>> m_groupCache = createLRUCacheMap(GROUP_LISTS_PER_USER);

        /** Cache for role memberships. */
        private Map<String, Boolean> m_hasRoleCache = new ConcurrentHashMap<>();

        /**
         * Gets the cached bare roles (with no OU information).
         *
         * @return the cached bare roles
         */
        public List<CmsRole> getBareRoles() {

            return m_bareRoles;
        }

        /**
         * Gets the group list cache map.
         *
         * @return the group list cache
         */
        public Map<String, List<CmsGroup>> getGroupCache() {

            return m_groupCache;
        }

        /**
         * Gets the 'hasRole' cache map.
         *
         * @return the 'hasRole' cache
         */
        public Map<String, Boolean> getHasRoleCache() {

            return m_hasRoleCache;
        }

        /**
         * @see org.opencms.monitor.I_CmsMemoryMonitorable#getMemorySize()
         */
        public int getMemorySize() {

            return (int)(CmsMemoryMonitor.getValueSize(m_groupCache)
                + CmsMemoryMonitor.getValueSize(m_hasRoleCache)
                + CmsMemoryMonitor.getValueSize(m_bareRoles));
        }

        /**
         * Sets the cached bare roles (with no associated OU information).
         *
         * @param bareRoles the bare roles
         */
        public void setBareRoles(List<CmsRole> bareRoles) {

            m_bareRoles = bareRoles;
        }

    }

    /** Max cached group lists per user. Non-final, so can be adjusted at runtime. */
    public static volatile int GROUP_LISTS_PER_USER = 32;

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGroupListCache.class);

    /** The internal cache used. */
    private LoadingCache<CmsUUID, Entry> m_internalCache;

    /** Interner for canonicalizing the role membership cache keys. */
    private Interner<String> m_interner = Interners.newBuilder().concurrencyLevel(8).build();

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
     * Creates a thread safe LRU cache map based on the guava cache builder.<p>
     *
     * @param capacity the cache capacity
     *
     * @return the cache map
     */
    @SuppressWarnings("unchecked")
    static <T, V> Map<T, V> createLRUCacheMap(int capacity) {

        CacheBuilder<?, ?> builder = CacheBuilder.newBuilder().concurrencyLevel(4).maximumSize(capacity);
        return (Map<T, V>)(builder.build().asMap());
    }

    /**
     * Removes all cache entries.
     */
    public void clear() {

        if (LOG.isInfoEnabled()) {
            if (LOG.isDebugEnabled()) {
                // when DEBUG level is enabled, log a dummy exception to generate a stack trace
                LOG.debug("CmsGroupListCache.clear() called", new Exception("(dummy exception)"));
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
     * Gets the cached bare roles for the given user id, or null if none are cached.
     *
     * <p>These are just the roles of the user, but with no OU information.
    
     * @param userId the user id
     * @return the bare roles for the user
     */
    public List<CmsRole> getBareRoles(CmsUUID userId) {

        Entry entry = m_internalCache.getIfPresent(userId);
        if (entry == null) {
            return null;
        }
        return entry.getBareRoles();
    }

    /**
     * Gets the cached user groups for the given combination of keys, or null if nothing is cached.
     *
     * @param userId the user id
     * @param subKey a string that consists of the parameters/flags for the group reading operation
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
     * Sets the bare roles for a user (with no OU information).
     *
     * @param user the user
     * @param bareRoles the list of bare roles
     */
    public void setBareRoles(CmsUser user, List<CmsRole> bareRoles) {

        if (!user.isWebuser()) { // web users take away space from normal workplace users/editors
            CmsUUID userId = user.getId();
            m_internalCache.getUnchecked(userId).setBareRoles(bareRoles);
        }
    }

    /**
     * Caches a new value for the given combination of keys.
     *
     * @param user the user
     * @param subKey a string that consists of the parameters/flags for the group reading operation
     * @param groups the value to cache
     */
    public void setGroups(CmsUser user, String subKey, List<CmsGroup> groups) {

        if (!user.isWebuser()) { // web users take away space from normal workplace users/editors
            CmsUUID userId = user.getId();
            Map<String, List<CmsGroup>> groupCache = m_internalCache.getUnchecked(userId).getGroupCache();
            groupCache.put(subKey, new ArrayList<>(groups));
        }
    }

    /**
     * Caches the role membership for the given user id and role key.
     *
     * @param user the user
     * @param roleKey the role key
     * @param value the role membership value
     */
    public void setHasRole(CmsUser user, String roleKey, Boolean value) {

        if (!user.isWebuser()) { // web users take away space from normal workplace users/editors
            roleKey = m_interner.intern(roleKey);
            CmsUUID userId = user.getId();
            m_internalCache.getUnchecked(userId).getHasRoleCache().put(roleKey, value);
        }
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

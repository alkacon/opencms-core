/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.storage;

import org.opencms.file.CmsStoredContentInfo;
import org.opencms.monitor.CmsMemoryMonitor;

import java.util.Map;

/**
 * In-memory stored content info cache.<p>
 */
public class CmsInMemoryStoredContentInfoCache implements I_CmsStoredContentInfoCache {

    /**
     * Cache entry including its write time.<p>
     */
    private static class Entry {

        /** The stored content info. */
        private CmsStoredContentInfo m_info;

        /** The time when the entry was stored. */
        private long m_time;

        /**
         * Creates a new entry.<p>
         *
         * @param info the stored content info
         */
        Entry(CmsStoredContentInfo info) {

            m_info = info;
            m_time = System.currentTimeMillis();
        }
    }

    /** The cached entries. */
    private Map<CmsStoredContentInfoCacheKey, Entry> m_entries;

    /** The time to live in milliseconds. */
    private long m_ttlMillis;

    /**
     * Creates a new cache.<p>
     *
     * @param size the maximum number of cache entries
     * @param ttlSeconds the time to live in seconds
     */
    public CmsInMemoryStoredContentInfoCache(int size, long ttlSeconds) {

        m_entries = CmsMemoryMonitor.createLRUCacheMap(size);
        m_ttlMillis = ttlSeconds > 0 ? ttlSeconds * 1000 : 0;
    }

    /**
     * @see org.opencms.db.storage.I_CmsStoredContentInfoCache#clear()
     */
    public void clear() {

        m_entries.clear();
    }

    /**
     * @see org.opencms.db.storage.I_CmsStoredContentInfoCache#get(org.opencms.db.storage.CmsStoredContentInfoCacheKey)
     */
    public CmsStoredContentInfo get(CmsStoredContentInfoCacheKey key) {

        Entry entry = m_entries.get(key);
        if (entry == null) {
            return null;
        }
        if ((m_ttlMillis > 0) && ((System.currentTimeMillis() - entry.m_time) > m_ttlMillis)) {
            m_entries.remove(key);
            return null;
        }
        return entry.m_info;
    }

    /**
     * @see org.opencms.db.storage.I_CmsStoredContentInfoCache#put(org.opencms.db.storage.CmsStoredContentInfoCacheKey, org.opencms.file.CmsStoredContentInfo)
     */
    public void put(CmsStoredContentInfoCacheKey key, CmsStoredContentInfo info) {

        m_entries.put(key, new Entry(info));
    }

    /**
     * @see org.opencms.db.storage.I_CmsStoredContentInfoCache#remove(org.opencms.db.storage.CmsStoredContentInfoCacheKey)
     */
    public void remove(CmsStoredContentInfoCacheKey key) {

        m_entries.remove(key);
    }
}

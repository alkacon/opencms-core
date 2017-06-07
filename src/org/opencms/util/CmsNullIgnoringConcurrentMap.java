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

package org.opencms.util;

import org.opencms.main.CmsLog;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

/**
 * Wrapper around ConcurrentHashMap which allows null values.<p>
 *
 * The point of this is the following: Often, HashMaps in older code are accessed concurrently by multiple threads. When these threads modify the
 * map concurrently, an infinite loop may occur due to the standard HashMap implementation. But sometimes we can't just replace the HashMap with a
 * ConcurrentHashMap because that class doesn't allow null values and we don't always know for certain whether null values are used or not.
 *
 * But if we don't care about the distinction about null values and entries not being present, we can use this map class which will just log an error
 * and remove the entry when trying to set a null value.
 *
 * NOTE: Currently this wrapper does *not* check value modifications made to entries returned by entrySet!
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class CmsNullIgnoringConcurrentMap<K, V> implements Map<K, V> {

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNullIgnoringConcurrentMap.class);

    /** The wrapped map. */
    private Map<K, V> m_internalMap = new ConcurrentHashMap<K, V>();

    /**
     * Creates a new instance.<p>
     */
    public CmsNullIgnoringConcurrentMap() {

    }

    /**
     * Creates a new instance from another map.<p>
     *
     * @param otherMap the other map
     */
    public CmsNullIgnoringConcurrentMap(Map<K, V> otherMap) {
        for (Map.Entry<K, V> entry : otherMap.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     *
     * @see java.util.Map#clear()
     */
    public void clear() {

        m_internalMap.clear();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {

        return m_internalMap.containsKey(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {

        return m_internalMap.containsValue(value);
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set<Map.Entry<K, V>> entrySet() {

        return m_internalMap.entrySet();
    }

    /**
     * @see java.util.Map#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {

        return m_internalMap.equals(o);
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public V get(Object key) {

        return m_internalMap.get(key);
    }

    /**
     * @see java.util.Map#hashCode()
     */
    @Override
    public int hashCode() {

        return m_internalMap.hashCode();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {

        return m_internalMap.isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet() {

        return m_internalMap.keySet();
    }

    /**
     * Sets the given map value for the given key, unless either of them is null.<p>
     *
     * If the value is null,
     *
     * @param key the key
     * @param value the value
     *
     * @return the old value
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public V put(K key, V value) {

        if ((key != null) && (value != null)) {
            return m_internalMap.put(key, value);
        }
        Exception e = new Exception();
        try {
            // we want to print a stack trace when null is used as a key/value
            throw e;
        } catch (Exception e2) {
            e = e2;
        }
        if (key == null) {
            LOG.warn("Invalid null key in map", e);
            return null;
        }
        if (value == null) {
            LOG.warn("Invalid null value in map", e);
            return m_internalMap.remove(key);
        }
        return null;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends K, ? extends V> m) {

        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public V remove(Object key) {

        return m_internalMap.remove(key);
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {

        return m_internalMap.size();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection<V> values() {

        return m_internalMap.values();
    }

}

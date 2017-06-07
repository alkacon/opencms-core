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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Special collection class which allows lookup from keys to values  and from values to sets of keys.<p>
 *
 * It also  implements efficient removal of values, not just keys.<p>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class CmsManyToOneMap<K, V> {

    /** Map from keys to values . */
    private Map<K, V> m_forwardMap = Maps.newHashMap();

    /** Map from values to sets of keys. */
    private HashMultimap<V, K> m_reverseMap = HashMultimap.create();

    /**
     * Creates a new instance.<p>
     */
    public CmsManyToOneMap() {

        // do nothing
    }

    /**
     * Creates a new instance by copying the data from another one.<p>
     *
     * @param other the other map to copy the data from
     */
    public CmsManyToOneMap(CmsManyToOneMap<K, V> other) {

        for (Map.Entry<K, V> entry : other.getForwardMap().entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Gets the value for a key.<p>
     *
     * @param key the key
     * @return the value for the key, or null
     */
    public V get(K key) {

        return m_forwardMap.get(key);
    }

    /**
     * Associates a value with a key.<p>
     *
     * @param key the key
     * @param value the value
     */
    public void put(K key, V value) {

        m_forwardMap.put(key, value);
        m_reverseMap.put(value, key);

    }

    /**
     * Removes the entry with the given key.<p>
     *
     * @param key the key
     */
    public void remove(K key) {

        V removedValue = m_forwardMap.remove(key);
        if (removedValue != null) {
            m_reverseMap.remove(removedValue, key);
        }
    }

    /**
     * Removes all entries with the given value.<p>
     *
     * @param value the value
     */
    public void removeValue(V value) {

        Set<K> keys = m_reverseMap.removeAll(value);
        for (K key : keys) {
            m_forwardMap.remove(key);
        }
    }

    /**
     * Gets the (immutable) map from keys to values.
     *
     * @return the map from keys to values
     */
    Map<K, V> getForwardMap() {

        return Collections.unmodifiableMap(m_forwardMap);
    }

    /**
     * Gets the multimap from values to keys.<p>
     *
     * @return the multimap from values to keys
     */
    Multimap<V, K> getReverseMap() {

        return Multimaps.unmodifiableSetMultimap(m_reverseMap);
    }
}

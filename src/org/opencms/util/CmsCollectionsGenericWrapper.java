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

import org.opencms.jsp.util.CmsJspContentAccessValueWrapper;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.collections.map.LazyMap;

/**
 * Provides Map wrapping utility functions for Java generics.<p>
 *
 * @since 8.0.0
 */
public final class CmsCollectionsGenericWrapper {

    /**
     *  Wrapper for lazy maps providing a better containsKey implementation.<p>
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    public static class MapWrapper<K, V> implements Map<K, V> {

        /** The wrapped map. */
        private Map<K, V> m_map;

        /**
         * Constructor.<p>
         *
         * @param map the map to wrap
         */
        MapWrapper(Map<K, V> map) {
            m_map = map;
        }

        /**
         *
         * @see java.util.Map#clear()
         */
        @Override
        public void clear() {

            m_map.clear();
        }

        /**
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        @Override
        public boolean containsKey(Object key) {

            V value = m_map.get(key);
            if (value instanceof CmsJspContentAccessValueWrapper) {
                return ((CmsJspContentAccessValueWrapper)value).getExists();
            } else {
                return value != null;
            }
        }

        /**
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        @Override
        public boolean containsValue(Object value) {

            return m_map.containsValue(value);
        }

        /**
         * @see java.util.Map#entrySet()
         */
        @Override
        public Set<java.util.Map.Entry<K, V>> entrySet() {

            return m_map.entrySet();
        }

        /**
         * @see java.util.Map#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {

            return m_map.equals(o);
        }

        /**
         * @see java.util.Map#get(java.lang.Object)
         */
        @Override
        public V get(Object key) {

            return m_map.get(key);
        }

        /**
         * @see java.util.Map#hashCode()
         */
        @Override
        public int hashCode() {

            return m_map.hashCode();
        }

        /**
         * @see java.util.Map#isEmpty()
         */
        @Override
        public boolean isEmpty() {

            return m_map.isEmpty();
        }

        /**
         * @see java.util.Map#keySet()
         */
        @Override
        public Set<K> keySet() {

            return m_map.keySet();
        }

        /**
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        @Override
        public V put(K key, V value) {

            return m_map.put(key, value);
        }

        /**
         * @see java.util.Map#putAll(java.util.Map)
         */
        @Override
        public void putAll(Map<? extends K, ? extends V> m) {

            m_map.putAll(m);
        }

        /**
         * @see java.util.Map#remove(java.lang.Object)
         */
        @Override
        public V remove(Object key) {

            return m_map.remove(key);
        }

        /**
         * @see java.util.Map#size()
         */
        @Override
        public int size() {

            return m_map.size();
        }

        /**
         * @see java.util.Map#values()
         */
        @Override
        public Collection<V> values() {

            return m_map.values();
        }

    }

    /**
     * Hides the public constructor.<p>
     */
    private CmsCollectionsGenericWrapper() {

        // empty
    }

    /**
     * Provides a wrapper to access the {@link LazyMap} functionality that avoids warnings with Java 1.5 generic code.<p>
     *
     * @param <K> the type of keys maintained by the returned map
     * @param <V> the type of mapped values
     * @param T the transformer to use for the Lazy Map
     *
     * @return a {@link LazyMap} of the required generic type
     */
    public static <K, V> Map<K, V> createLazyMap(Transformer T) {

        return new MapWrapper<K, V>(LazyMap.decorate(new HashMap<K, V>(), T));
    }

    /**
     * Provides a wrapper to create a {@link LRUMap} that avoids warnings with Java 1.5 generic code.<p>
     *
     * @param <K> the type of keys maintained by the returned map
     * @param <V> the type of mapped values
     *
     * @return a {@link LRUMap} of the required generic type
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> createLRUMap() {

        return new LRUMap();
    }

    /**
     * Provides a wrapper to create a {@link LRUMap} with the given size that avoids warnings with Java 1.5 generic code.<p>
     *
     * @param <K> the type of keys maintained by the returned map
     * @param <V> the type of mapped values
     * @param size the initial size of the created Map
     *
     * @return a {@link LRUMap} with the given size of the required generic type
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> createLRUMap(int size) {

        return new LRUMap(size);
    }

    /**
     * Provides a wrapper to convert an enumeration that avoids warnings with Java 1.5 generic code.<p>
     *
     * @param <K> the type of the returned enumeration elements
     * @param enumeration the enumeration to be converted
     *
     * @return a {@link Enumeration} with the required generic type
     */
    @SuppressWarnings("unchecked")
    public static <K> Enumeration<K> enumeration(Enumeration<?> enumeration) {

        return (Enumeration<K>)enumeration;
    }

    /**
     * Provides a wrapper to convert an object into a list that avoids warnings with Java 1.5 generic code.<p>
     *
     * @param <K> the type of the returned list elements
     * @param o the object to be converted
     *
     * @return a {@link List} with the required generic type
     */
    @SuppressWarnings("unchecked")
    public static <K> List<K> list(Object o) {

        return (List<K>)o;
    }

    /**
     * Provides a wrapper to convert an object into a map that avoids warnings with Java 1.5 generic code.<p>
     *
     * @param <K> the type of keys maintained by the returned map
     * @param <V> the type of mapped values
     * @param o the object to be converted
     *
     * @return a {@link Map} of the required generic type
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> map(Object o) {

        return (Map<K, V>)o;
    }
}
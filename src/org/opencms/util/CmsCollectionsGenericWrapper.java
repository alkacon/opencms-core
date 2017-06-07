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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        return LazyMap.decorate(new HashMap<K, V>(), T);
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
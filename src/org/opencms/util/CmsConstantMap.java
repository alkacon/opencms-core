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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Returns the constant Object the map was initialized with for all {@link #get(Object)} calls,
 * regardless of what the actual key is.<p>
 *
 * @since 7.0.2
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@SuppressWarnings("unchecked")
public class CmsConstantMap<K, V> implements Map<K, V> {

    /** Constant Map that always returns {@link Boolean#FALSE}.*/
    public static final Map<String, Boolean> CONSTANT_BOOLEAN_FALSE_MAP = new CmsConstantMap<String, Boolean>(
        Boolean.FALSE);

    /** Constant Map that always returns {@link Boolean#TRUE}.*/
    public static final Map<String, Boolean> CONSTANT_BOOLEAN_TRUE_MAP = new CmsConstantMap<String, Boolean>(
        Boolean.TRUE);

    /** Constant Map that always returns an empty list. */
    public static final Map<String, List<CmsJspContentAccessValueWrapper>> CONSTANT_EMPTY_LIST_MAP = new CmsConstantMap<String, List<CmsJspContentAccessValueWrapper>>(
        Collections.EMPTY_LIST);

    /** Constant Map that always returns an empty list. */
    public static final Map<String, String> CONSTANT_EMPTY_STRING_MAP = new CmsConstantMap<String, String>("".intern());

    /** The constant Object this map always returns. */
    private V m_constant;

    /**
     * Creates a new constant Map.<p>
     *
     * @param constant the constant to return for all {@link #get(Object)} calls.<p>
     */
    public CmsConstantMap(V constant) {

        m_constant = constant;
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {

        // NOOP
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {

        // this Map contains all keys, since all keys return the same value
        return true;
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {

        return m_constant.equals(value);
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set<Map.Entry<K, V>> entrySet() {

        return Collections.emptySet();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsConstantMap) {
            return m_constant.equals(((CmsConstantMap<?, ?>)obj).m_constant);
        }
        return false;
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public V get(Object key) {

        return m_constant;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_constant.hashCode();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {

        return false;
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set<K> keySet() {

        return Collections.emptySet();
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public V put(Object arg0, Object arg1) {

        return null;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends K, ? extends V> arg0) {

        // NOOP
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public V remove(Object key) {

        return m_constant;
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {

        return 0;
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection<V> values() {

        return Collections.singletonList(m_constant);
    }
}
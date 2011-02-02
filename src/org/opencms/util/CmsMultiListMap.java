/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsMultiListMap.java,v $
 * Date   : $Date: 2011/02/02 07:38:40 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a convenience class for working with maps whose value type is a List.<p>
 * 
 * It adds a new <code>addValue</code> method which will add a value to the list for a given
 * key, and create the list if it doesn't already exist.<p>
 * 
 * You can also use this class in GWT client code.<p>
 * 
 * @param <A> the key type
 * @param <B> the type of values which are put in the lists
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsMultiListMap<A, B> implements Map<A, List<B>>, Serializable {

    /** ID for serialization. */
    private static final long serialVersionUID = -2328888728877017303L;

    /** The wrapped map. */
    private Map<A, List<B>> m_impl;

    /**
     * Creates a new multimap.<p>
     */
    public CmsMultiListMap() {

        m_impl = new HashMap<A, List<B>>();
    }

    /**
     * Creates a new multimap which wraps an existing map.<p>
     * @param map
     */
    public CmsMultiListMap(Map<A, List<B>> map) {

        m_impl = map;
    }

    /**
     * Adds a value to the list for the given key, and creates the list if it didn't exist before.<p>
     * 
     * @param key the key 
     * @param value the value to add to the list for the key 
     */
    public void addValue(A key, B value) {

        List<B> vals = m_impl.get(key);
        if (vals == null) {
            vals = new ArrayList<B>();
            m_impl.put(key, vals);
        }
        vals.add(value);
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {

        m_impl.clear();
    }

    /**
    * @see java.lang.Object#clone()
     */
    @Override
    public CmsMultiListMap<A, B> clone() {

        Map<A, List<B>> map = new HashMap<A, List<B>>();
        for (Map.Entry<A, List<B>> entry : m_impl.entrySet()) {
            List<B> list = new ArrayList<B>();
            for (B b : entry.getValue()) {
                list.add(b);
            }
            map.put(entry.getKey(), list);
        }
        return new CmsMultiListMap<A, B>(map);
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {

        return m_impl.containsKey(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {

        return m_impl.containsValue(value);
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<A, List<B>>> entrySet() {

        return m_impl.entrySet();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {

        return m_impl.equals(o);
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public List<B> get(Object key) {

        return m_impl.get(key);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_impl.hashCode();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {

        return m_impl.isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set<A> keySet() {

        return m_impl.keySet();
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public List<B> put(A key, List<B> value) {

        return m_impl.put(key, value);
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends A, ? extends List<B>> m) {

        m_impl.putAll(m);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public List<B> remove(Object key) {

        return m_impl.remove(key);
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {

        return m_impl.size();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection<List<B>> values() {

        return m_impl.values();
    }

}

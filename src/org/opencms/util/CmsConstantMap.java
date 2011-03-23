/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsConstantMap.java,v $
 * Date   : $Date: 2011/03/23 14:50:04 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Returns the constant Object the map was initialized with for all {@link #get(Object)} calls,
 * regardless of what the actual key is.<p> 
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 7.0.2
 */
public class CmsConstantMap implements Map {

    /** Constant Map that always returns {@link Boolean#FALSE}.*/
    public static final Map CONSTANT_BOOLEAN_FALSE_MAP = new CmsConstantMap(Boolean.FALSE);

    /** Constant Map that always returns {@link Boolean#TRUE}.*/
    public static final Map CONSTANT_BOOLEAN_TRUE_MAP = new CmsConstantMap(Boolean.TRUE);

    /** Constant Map that always returns an empty list. */
    public static final Map CONSTANT_EMPTY_LIST_MAP = new CmsConstantMap(Collections.EMPTY_LIST);

    /** The constant Object this map always returns. */
    private Object m_constant;

    /**
     * Creates a new constant Map.<p> 
     * 
     * @param constant the constant to return for all {@link #get(Object)} calls.<p>
     */
    public CmsConstantMap(Object constant) {

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
    public Set entrySet() {

        return Collections.EMPTY_SET;
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
            return m_constant.equals(((CmsConstantMap)obj).m_constant);
        }
        return false;
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {

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
    public Set keySet() {

        return Collections.EMPTY_SET;
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object arg0, Object arg1) {

        return null;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map arg0) {

        // NOOP
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {

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
    public Collection values() {

        return Collections.singletonList(m_constant);
    }
}
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

package org.opencms.jsp.util;

import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.util.CmsStringUtil;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * Wrapper for accessing JSON in JSPs.
 */
public class CmsJspJsonWrapper extends AbstractCollection<Object> {

    /** The wrapped value. */
    private Object m_value;

    /**
     * Creates a new JSON wrapper.
     *
     * @param value the value to wrap
     */
    public CmsJspJsonWrapper(Object value) {

        m_value = value;
    }

    /**
     * Returns the JSON text as single line, that is as compact as possible.
     *
     * @return the JSON text
     */
    public String getCompact() {

        try {
            return JSONObject.valueToString(m_value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the wrapped JSON object.
     *
     * This is an alias for {@link #getObject()}.
     *
     * @return the wrapped JSON object
     *
     * @see #getObject()
     */
    public Object getJson() {

        return getObject();
    }

    /**
     * Returns the wrapped JSON object.
     *
     * Useful in case you want to insert an existing JSON object into another JSON object.
     *
     * @return the wrapped JSON object
     */
    public Object getObject() {

        return m_value;
    }

    /**
     * Returns the JSON text in pretty-printed and indented format.
     *
     * @return the indented JSON
     */
    public String getPretty() {

        try {
            return JSONObject.valueToString(m_value, 4, 0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Supports the use of the <code>empty</code> operator in the JSP EL by implementing the Collection interface.<p>
     *
     * @see java.util.AbstractCollection#isEmpty()
     */
    @Override
    public boolean isEmpty() {

        if (m_value instanceof JSONObject) {
            return ((JSONObject)m_value).length() < 1;
        } else if (m_value instanceof JSONArray) {
            return ((JSONArray)m_value).length() < 1;
        } else if (m_value instanceof String) {
            return CmsStringUtil.isEmptyOrWhitespaceOnly((String)m_value);
        } else {
            return m_value == null;
        }
    }

    /**
     * Supports the use of the <code>empty</code> operator in the JSP EL by implementing the Collection interface.<p>
     *
     * @return an empty Iterator in case {@link #isEmpty()} is <code>true</code>,
     * otherwise an Iterator that will return the String value of this wrapper exactly once.<p>
     *
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public Iterator<Object> iterator() {

        Iterator<Object> it = new Iterator<Object>() {

            private boolean isFirst = true;

            @Override
            public boolean hasNext() {

                return isFirst && !isEmpty();
            }

            @Override
            public Object next() {

                isFirst = false;
                return getObject();
            }

            @Override
            public void remove() {

                throw new UnsupportedOperationException();
            }
        };
        return it;
    }

    /**
     * Supports the use of the <code>empty</code> operator in the JSP EL by implementing the Collection interface.<p>
     *
     * @return always returns 0.<p>
     *
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {

        return isEmpty() ? 0 : 1;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return getCompact();
    }

}

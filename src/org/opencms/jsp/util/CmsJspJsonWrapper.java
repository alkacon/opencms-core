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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper for accessing JSON in JSPs.
 */
public class CmsJspJsonWrapper extends AbstractCollection<Object> {

    /**
     * Helper class representing a "handle" to a JSON object's entry through which it is possible to either access the entry's value or remove the entry.
     */
    private static class JSONObjectEntry {

        /** The key for the entry. */
        private String m_key;

        /** The parent JSON object. */
        private JSONObject m_object;

        /**
         * Creates a new instance.
         *
         * @param obj the parent JSON object
         * @param key the key in the JSON object
         */
        public JSONObjectEntry(JSONObject obj, String key) {

            m_object = obj;
            m_key = key;
        }

        /**
         * Helper method for getting the list of entries of a JSON object with the given key.
         *
         * <p>If the key just normally occurs in the given object, a wrapper for that entry will be returned.
         * If the key is the wildcard '*', a list of all entries for the object is returned. If the key does not exist
         * in the object, an empty list is returned.
         *
         * @param obj a JSON object
         * @param key a JSON key (can be the wildcard '*')
         *
         * @return the list of entries for the given key
         */
        static List<JSONObjectEntry> getEntriesForKey(JSONObject obj, String key) {

            List<JSONObjectEntry> result = new ArrayList<>();
            if ("*".equals(key)) {
                for (String actualKey : obj.keySet()) {
                    result.add(new JSONObjectEntry(obj, actualKey));
                }
                return result;
            } else {
                Object child = obj.opt(key);
                if (child != null) {
                    return Collections.singletonList(new JSONObjectEntry(obj, key));
                } else {
                    return Collections.emptyList();
                }
            }
        }

        /**
         * Gets the JSON key.
         *
         * @return the JSON key
         */
        public String getKey() {

            return m_key;
        }

        /**
         * Gets the entry's value.
         *
         * @return the value
         */
        public Object getValue() {

            return m_object.opt(m_key);
        }

        /**
         * Removes the entry from the parent JSON object.
         */
        public void remove() {

            m_object.remove(m_key);
        }
    }

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
     * Helper method for removing parts from a JSON object with a given path, which is already split into path components.
     *
     * See {@link #removePath(String)} for how the path works.
     *
     * @param obj the JSON object
     * @param pathComponents the path components
     */
    public static void removePathInJson(Object obj, List<String> pathComponents) {

        if (pathComponents.isEmpty()) {
            return;
        }
        String key = pathComponents.get(0);
        List<String> remainingPath = pathComponents.subList(1, pathComponents.size());
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(key)) {
            removePathInJson(obj, remainingPath);
            return;
        }
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray)obj;
            for (int i = 0; i < array.length(); i++) {
                removePathInJson(array.opt(i), pathComponents);
            }
        } else if (obj instanceof JSONObject) {
            List<JSONObjectEntry> childrenForKey = JSONObjectEntry.getEntriesForKey((JSONObject)obj, key);
            if (pathComponents.size() == 1) {
                for (JSONObjectEntry child : childrenForKey) {
                    child.remove();
                }
            } else {
                for (JSONObjectEntry child : childrenForKey) {
                    removePathInJson(child.getValue(), remainingPath);
                }
            }
        }
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
     * @return the pretty-printed and indented JSON
     */
    public String getPretty() {

        try {
            return JSONObject.valueToString(m_value, 4, 0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Synonym for {@link #getPretty()}.
     *
     * @return the pretty-printed and indented JSON
     *
     * @see #getPretty()
     */
    public String getVerbose() {

        return getPretty();
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
     * Removes the parts from the JSON object which match the given path.
     *
     * <p>
     * The path is a slash-separated sequence of path components, where each path component is either the name of a JSON field, or the wildcard '*'.
     * The removal process locates all JSON objects matching the parent path of the given path, and then removes the entry given by the last path component from it.
     * <ul>
     * <li>If a JSON array is encountered while descending the path, the rest of the path is processed for all elements of the array.
     * <li>If an object is encountered which does not have an entry with the same name as the next path component, it and its contents are left unchanged.
     * <li> The wildcard '* matches all keys in a JSON object.
     * </ul>
     *
     * @param path the path which should be deleted
     */
    public void removePath(String path) {

        path = path.trim();
        removePathInJson(m_value, Arrays.asList(path.split("/")));

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

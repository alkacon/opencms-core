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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * This file is based on:
 * com.fasterxml.jackson.databind.node.ObjectNode.
 */

package org.opencms.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class JSONObject extends ObjectNode {
    static final ObjectMapper mapper = new ObjectMapper();

    public JSONObject() {
        super(JsonNodeFactory.instance);
    }

    public JSONObject(String source) throws JSONException {
        this();

        try {
            ObjectNode node = (ObjectNode) mapper.readTree(source);
            this.setAll(node);
        } catch (Exception e) {
            throw new JSONException(e);
        }
    }

    /**
     * Construct a JSONObject from a Map.
     *
     * @param m A map object that can be used to initialize the contents of
     *          the JSONObject.
     */
    public JSONObject(Map<?, ?> m) {
        this();
        if (m != null) {
            for (final Map.Entry<?, ?> e : m.entrySet()) {
                final Object value = e.getValue();
                if (value != null) {
                    this.put(String.valueOf(e.getKey()), wrap(value));
                }
            }
        }
    }

    /**
     * Get the JSONArray value associated with a key.
     *
     * @param key A key string.
     * @return A JSONArray which is the value.
     * @throws JSONException if the key is not found or if the value is not a JSONArray.
     */
    public JSONArray getJSONArray(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        }
        throw new JSONException("JSONObject[" + quote(key)
                + "] is not a JSONArray.");
    }

    /**
     * Get an optional JSONArray associated with a key. It returns null if there
     * is no such key, or if its value is not a JSONArray.
     *
     * @param key
     *            A key string.
     * @return A JSONArray which is the value.
     */
    public JSONArray optJSONArray(String key) {
        Object o = this.opt(key);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }
    /**
     * Get the JSONObject value associated with a key.
     *
     * @param key A key string.
     * @return A JSONObject which is the value.
     * @throws JSONException if the key is not found or if the value is not a JSONObject.
     */
    public JSONObject getJSONObject(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        }
        throw new JSONException("JSONObject[" + quote(key)
                + "] is not a JSONObject.");
    }

    /**
     * Get an optional JSONObject associated with a key. It returns null if
     * there is no such key, or if its value is not a JSONObject.
     *
     * @param key
     *            A key string.
     * @return A JSONObject which is the value.
     */
    public JSONObject optJSONObject(String key) {
        Object object = this.opt(key);
        return object instanceof JSONObject ? (JSONObject) object : null;
    }

    public String getString(String field) {
        JsonNode node = this.get(field);
        return null == node ? null : node.asText();
    }

    public String optString(String field) {
        return optString(field, "");
    }

    public String optString(String field, String defaultValue) {
        JsonNode node = this.get(field);
        return null == node ? defaultValue : node.asText(defaultValue);
    }

    public boolean getBoolean(String field) {
        JsonNode node = this.get(field);
        return null == node ? false : node.asBoolean();
    }

    public boolean optBoolean(String field) {
        JsonNode node = this.get(field);
        return null == node ? false : node.asBoolean();
    }

    public int getInt(String field) {
        JsonNode node = this.get(field);
        return null == node ? 0 : node.asInt();
    }

    public int optInt(String field, int defaultValue) {
        JsonNode node = this.get(field);
        return null == node ? defaultValue : node.asInt(defaultValue);
    }

    public double getDouble(String field) {
        JsonNode node = this.get(field);
        return null == node ? 0 : node.asDouble();
    }

    public double optDouble(String field, double defaultValue) {
        JsonNode node = this.get(field);
        return null == node ? defaultValue : node.asDouble(defaultValue);
    }


    public Iterator<String> keys() {
        return this.fieldNames();
    }

    public Set<String> keySet() {
        Set<String> result = new LinkedHashSet<>();
        this.fieldNames().forEachRemaining(field -> result.add(field));
        return result;
    }

    public void put(String field, Object object) {
        this.set(field, wrap(object));
    }


    /**
     * Put a key/value pair in the JSONObject, but only if the key and the value
     * are both non-null.
     *
     * @param key
     *            A key string.
     * @param value
     *            An object which is the value. It should be of one of these
     *            types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
     *            String, or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException
     *             If the value is a non-finite number.
     */
    public JSONObject putOpt(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            this.put(key, value);
        }
        return this;
    }


    /**
     * Get an optional value associated with a key.
     *
     * @param field A key string.
     * @return An object which is the value, or null if there is no value.
     */
    public JsonNode opt(String field) {
        return field == null ? null : this.get(field);
    }

    public JSONObject append(String key, String value) throws JSONException {
        testValidity(value);
        JsonNode object = this.opt(key);
        if (object == null) {
            this.put(key, new JSONArray().add(value));
        } else if (object instanceof JSONArray) {
            this.put(key, ((JSONArray) object).add(value));
        } else {
            throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
        }
        return this;
    }


    /**
     * Throw an exception if the object is a NaN or infinite number.
     *
     * @param o The object to test.
     * @throws JSONException If o is a non-finite number.
     */
    public static void testValidity(Object o) throws JSONException {
        if (o != null) {
            if (o instanceof Double) {
                if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
                    throw new JSONException(
                            "JSON does not allow non-finite numbers.");
                }
            } else if (o instanceof Float) {
                if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
                    throw new JSONException(
                            "JSON does not allow non-finite numbers.");
                }
            }
        }
    }

    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within </, producing <\/,
     * allowing JSON text to be delivered in HTML. In JSON text, a string cannot
     * contain a control character or an unescaped quote or backslash.
     *
     * @param string A String
     * @return A String correctly formatted for insertion in a JSON text.
     */
    public static String quote(String string) {
        StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            try {
                return quote(string, sw).toString();
            } catch (IOException ignored) {
                // will never happen - we are writing to a string writer
                return "";
            }
        }
    }

    public static Writer quote(String string, Writer w) throws IOException {
        if (string == null || string.length() == 0) {
            w.write("\"\"");
            return w;
        }

        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();

        w.write('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    w.write('\\');
                    w.write(c);
                    break;
                case '/':
                    if (b == '<') {
                        w.write('\\');
                    }
                    w.write(c);
                    break;
                case '\b':
                    w.write("\\b");
                    break;
                case '\t':
                    w.write("\\t");
                    break;
                case '\n':
                    w.write("\\n");
                    break;
                case '\f':
                    w.write("\\f");
                    break;
                case '\r':
                    w.write("\\r");
                    break;
                default:
                    if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                            || (c >= '\u2000' && c < '\u2100')) {
                        w.write("\\u");
                        hhhh = Integer.toHexString(c);
                        w.write("0000", 0, 4 - hhhh.length());
                        w.write(hhhh);
                    } else {
                        w.write(c);
                    }
            }
        }
        w.write('"');
        return w;
    }


    /**
     * Wrap an object, if necessary. If the object is null, return the NULL
     * object. If it is an array or collection, wrap it in a JSONArray. If it is
     * a map, wrap it in a JSONObject. If it is a standard property (Double,
     * String, et al) then it is already wrapped. Otherwise, if it comes from
     * one of the java packages, turn it into a string. And if it doesn't, try
     * to wrap it in a JSONObject. If the wrapping fails, then null is returned.
     *
     * @param object The object to wrap
     * @return The wrapped value
     */
    public static JsonNode wrap(Object object) {
        try {
            JsonNodeFactory jnf = JsonNodeFactory.instance;
            if (object == null) return jnf.nullNode();;
            if (object instanceof JsonNode) return (JsonNode)object;
            if (object instanceof Byte) return jnf.numberNode((Byte)object);
            if (object instanceof Short) return jnf.numberNode((Short)object);
            if (object instanceof Integer) return jnf.numberNode((Integer)object);
            if (object instanceof Long) return jnf.numberNode((Long)object);
            if (object instanceof Float) return jnf.numberNode((Float)object);
            if (object instanceof Double) return jnf.numberNode((Double)object);
            if (object instanceof BigInteger) return jnf.numberNode((BigInteger)object);
            if (object instanceof BigDecimal) return jnf.numberNode((BigDecimal)object);
            if (object instanceof Boolean) return jnf.booleanNode((Boolean)object);
            if (object instanceof Character) return jnf.numberNode(((Character)object).charValue());
            if (object instanceof Enum) jnf.pojoNode(object);

            if (object instanceof Collection) return new JSONArray((Collection<?>) object);
            if (object instanceof Map) return new JSONObject((Map<?, ?>) object);

            if (object.getClass().isArray()) return new JSONArray(object);

            //A string may be just a string or a string of Json data structures.
            if (object instanceof String)
                return mapper.readTree((String)object);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return null;
    }



    /**
     * Merges the current JSON object with the given one, modifying the this.<p>
     *
     * @param jo the JSON object to merge
     * @param overwrite if to overwrite values
     * @param deep if to recurse in object values
     *
     * @throws JSONException if a value is a non-finite number
     *
     * @since 7.6
     */
    public void merge(JSONObject jo, boolean overwrite, boolean deep) throws JSONException {

        Iterator<String> it = jo.keys();
        while (it.hasNext()) {
            String key = it.next();
            if (!has(key)) {
                put(key, jo.get(key));
                continue;
            }
            boolean recurse = deep && (jo.optJSONObject(key) != null) && (optJSONObject(key) != null);
            if (overwrite && !recurse) {
                put(key, jo.get(key));
                continue;
            }
            if (recurse) {
                getJSONObject(key).merge(jo.getJSONObject(key), overwrite, deep);
            }
        }
    }
}

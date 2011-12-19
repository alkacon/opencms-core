/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
 * org.json.JSONObject
 * from the JSON in Java implementation.
 * 
 * Copyright (c) 2002 JSON.org
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * The Software shall be used for Good, not Evil.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.opencms.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its
 * external form is a string wrapped in curly braces with colons between the
 * names and values, and commas between the values and names. The internal form
 * is an object having <code>get</code> and <code>opt</code> methods for
 * accessing the values by name, and <code>put</code> methods for adding or
 * replacing values by name. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the <code>JSONObject.NULL</code>
 * object. A JSONObject constructor can be used to convert an external form
 * JSON text into an internal form whose values can be retrieved with the
 * <code>get</code> and <code>opt</code> methods, or to convert values into a
 * JSON text using the <code>put</code> and <code>toString</code> methods.
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * conversion for you.
 * <p>
 * The <code>put</code> methods adds values to an object. For example, <pre>
 *     myString = new JSONObject().put("JSON", "Hello, World!").toString();</pre>
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON syntax rules.
 * The constructors are more forgiving in the texts they will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 *     before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 *     quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 *     or single quote, and if they do not contain leading or trailing spaces,
 *     and if they do not contain any of these characters:
 *     <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
 *     and if they are not the reserved words <code>true</code>,
 *     <code>false</code>, or <code>null</code>.</li>
 * <li>Keys can be followed by <code>=</code> or <code>=></code> as well as
 *     by <code>:</code>.</li>
 * <li>Values can be followed by <code>;</code> <small>(semicolon)</small> as
 *     well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or
 *     <code>0x-</code> <small>(hex)</small> prefix.</li>
 * <li>Comments written in the slashshlash, slashstar, and hash conventions
 *     will be ignored.</li>
 * </ul>
 * 
 */
public class JSONObject {

    /**
     * JSONObject.NULL is equivalent to the value that JavaScript calls null,
     * whilst Java's null is equivalent to the value that JavaScript calls
     * undefined.<p>
     */
    protected static final class Null {

        /**
         * A Null object is equal to the null value and to itself.<p>
         * 
         * @param object an object to test for nullness
         * @return true if the object parameter is the JSONObject.NULL object or null
         */
        public boolean equals(Object object) {

            return (object == null) || (object == this);
        }

        /**
         * @see Object#hashCode()
         */
        public int hashCode() {

            return super.hashCode();
        }

        /**
         * Get the "null" string value.<p>
         * 
         * @return the string "null".
         */
        public String toString() {

            return "null";
        }

        /**
         * There is only intended to be a single instance of the NULL object,
         * so the clone method returns itself.<p>
         * 
         * @return NULL.
         */
        protected Object clone() {

            return this;
        }
    }

    /**
     * It is sometimes more convenient and less ambiguous to have a
     * <code>NULL</code> object than to use Java's <code>null</code> value.
     * <code>JSONObject.NULL.equals(null)</code> returns <code>true</code>.
     * <code>JSONObject.NULL.toString()</code> returns <code>"null"</code>.
     */
    public static final Object NULL = new Null();

    /**
     * The map where the JSONObject's properties are kept.
     */
    private Map<String, Object> m_map;

    /**
     * Construct an empty JSONObject.<p>
     */
    public JSONObject() {

        this(false);
    }

    /**
     * Construct an empty sorted JSONObject.<p>
     * 
     * @param sorted true for sorted, false for none sorted
     */
    public JSONObject(boolean sorted) {

        if (sorted) {
            this.m_map = new LinkedHashMap<String, Object>();
        } else {
            this.m_map = new HashMap<String, Object>();
        }
    }

    /**
     * Construct a JSONObject from a subset of another JSONObject.<p>
     * 
     * An array of strings is used to identify the keys that should be copied.
     * Missing keys are ignored.<p>
     * 
     * @param jo a JSONObject
     * @param names an array of strings
     * @exception JSONException if a value is a non-finite number
     */
    public JSONObject(JSONObject jo, String[] names)
    throws JSONException {

        this();
        for (int i = 0; i < names.length; i += 1) {
            putOpt(names[i], jo.opt(names[i]));
        }
    }

    /**
     * Construct a JSONObject from a JSONTokener.<p>
     * 
     * @param x a JSONTokener object containing the source string
     * @throws JSONException if there is a syntax error in the source string
     */
    public JSONObject(JSONTokener x)
    throws JSONException {

        this(x, false);
    }

    /**
     * Construct a JSONObject from a JSONTokener, optionally sorted.<p>
     * 
     * @param x a JSONTokener object containing the source string
     * @param sorted true for sorted, false for none sorted
     * @throws JSONException if there is a syntax error in the source string
     */
    public JSONObject(JSONTokener x, boolean sorted)
    throws JSONException {

        this(sorted);
        char c;
        String key;

        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        for (;;) {
            c = x.nextClean();
            switch (c) {
                case 0:
                    throw x.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    return;
                default:
                    x.back();
                    key = x.nextValue().toString();
            }

            /*
             * The key is followed by ':'. We will also tolerate '=' or '=>'.
             */

            c = x.nextClean();
            if (c == '=') {
                if (x.next() != '>') {
                    x.back();
                }
            } else if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }
            put(key, x.nextValue());

            /*
             * Pairs are separated by ','. We will also tolerate ';'.
             */

            switch (x.nextClean()) {
                case ';':
                case ',':
                    if (x.nextClean() == '}') {
                        return;
                    }
                    x.back();
                    break;
                case '}':
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }

    /**
     * Construct a JSONObject from a Map.<p>
     * 
     * @param map a map object that can be used to initialize the contents of the JSONObject
     */
    public JSONObject(Map<String, Object> map) {

        this.m_map = (map == null) ? new HashMap<String, Object>() : map;
    }

    /**
     * Construct a JSONObject from a Map.<p>
     * 
     * Note: Use this constructor when the map contains &lt;key,bean&gt;.<p>
     * 
     * @param map a map with Key-Bean data
     * @param includeSuperClass tell whether to include the super class properties.
     */
    public JSONObject(Map<String, Object> map, boolean includeSuperClass) {

        this.m_map = new HashMap<String, Object>();
        if (map != null) {
            for (Iterator<Map.Entry<String, Object>> i = map.entrySet().iterator(); i.hasNext();) {
                Map.Entry<String, Object> e = i.next();
                this.m_map.put(e.getKey(), new JSONObject(e.getValue(), includeSuperClass));
            }
        }
    }

    /**
     * Construct a JSONObject from an Object using bean getters<p>
     * It reflects on all of the public methods of the object.
     * For each of the methods with no parameters and a name starting
     * with <code>"get"</code> or <code>"is"</code> followed by an uppercase letter,
     * the method is invoked, and a key and the value returned from the getter method
     * are put into the new JSONObject.<p>
     *
     * The key is formed by removing the <code>"get"</code> or <code>"is"</code> prefix. If the second remaining
     * character is not upper case, then the first
     * character is converted to lower case.<p>
     *
     * For example, if an object has a method named <code>"getName"</code>, and
     * if the result of calling <code>object.getName()</code> is <code>"Larry Fine"</code>,
     * then the JSONObject will contain <code>"name": "Larry Fine"</code>.<p>
     *
     * @param bean an object that has getter methods that should be used to make a JSONObject
     */
    public JSONObject(Object bean) {

        this();
        populateInternalMap(bean, false);
    }

    /**
     * Construct JSONObject from the given bean.<p>
     * 
     * This will also create JSONObject for all internal object (List, Map, Inner Objects) of the provided bean.
     * 
     * @see #JSONObject(Object bean) also.
     * 
     * @param bean an object that has getter methods that should be used to make a JSONObject
     * @param includeSuperClass tell whether to include the super class properties.
     */
    public JSONObject(Object bean, boolean includeSuperClass) {

        this();
        populateInternalMap(bean, includeSuperClass);
    }

    /**
    * Construct a JSONObject from an Object, using reflection to find the
    * public members.<p>
    * 
    * The resulting JSONObject's keys will be the strings
    * from the names array, and the values will be the field values associated
    * with those keys in the object. If a key is not found or not visible,
    * then it will not be copied into the new JSONObject.<p>
    * 
    * @param object an object that has fields that should be used to make a JSONObject
    * @param names an array of strings, the names of the fields to be obtained from the object
    */
    public JSONObject(Object object, String[] names) {

        this();
        Class c = object.getClass();
        for (int i = 0; i < names.length; i += 1) {
            String name = names[i];
            try {
                Field field = c.getField(name);
                Object value = field.get(object);
                this.put(name, value);
            } catch (Exception e) {
                /* forget about it */
            }
        }
    }

    /**
     * Construct a JSONObject from a source JSON text string.<p>
     * 
     * This is the most commonly used JSONObject constructor.<p>
     * 
     * @param source a string beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>
     * @exception JSONException if there is a syntax error in the source string
     */
    public JSONObject(String source)
    throws JSONException {

        this(source, false);
    }

    /**
     * Construct a JSONObject from a source JSON text string, optionally sorted.<p>
     * 
     * This is the most commonly used JSONObject constructor.<p>
     * 
     * @param source a string beginning
     * @param sorted true for sorted, false for none sorted
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>
     * @exception JSONException if there is a syntax error in the source string
     */
    public JSONObject(String source, boolean sorted)
    throws JSONException {

        this(new JSONTokener(source), sorted);
    }

    /**
     * Produce a string from a double. The string "null" will be returned if
     * the number is not finite.<p>
     * 
     * @param d a double
     * @return a String
     */
    public static String doubleToString(double d) {

        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return "null";
        }

        // Shave off trailing zeros and decimal point, if possible.

        String s = Double.toString(d);
        if ((s.indexOf('.') > 0) && (s.indexOf('e') < 0) && (s.indexOf('E') < 0)) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    /**
     * Get an array of field names from a JSONObject.<p>
     *
     * @param jo the JSONObject
     * @return an array of field names, or null if there are no names
     */
    public static String[] getNames(JSONObject jo) {

        int length = jo.length();
        if (length == 0) {
            return null;
        }
        Iterator<String> i = jo.keys();
        String[] names = new String[length];
        int j = 0;
        while (i.hasNext()) {
            names[j] = i.next();
            j += 1;
        }
        return names;
    }

    /**
     * Get an array of field names from an Object.<p>
     *
     * @param object the object
     * @return an array of field names, or null if there are no names
     */
    public static String[] getNames(Object object) {

        if (object == null) {
            return null;
        }
        Class klass = object.getClass();
        Field[] fields = klass.getFields();
        int length = fields.length;
        if (length == 0) {
            return null;
        }
        String[] names = new String[length];
        for (int i = 0; i < length; i += 1) {
            names[i] = fields[i].getName();
        }
        return names;
    }

    /**
     * Produce a string from a Number.<p>
     * 
     * @param  n a Number
     * @return a String
     * @throws JSONException if n is a non-finite number
     */
    public static String numberToString(Number n) throws JSONException {

        if (n == null) {
            throw new JSONException("Null pointer");
        }
        testValidity(n);

        // Shave off trailing zeros and decimal point, if possible.

        String s = n.toString();
        if ((s.indexOf('.') > 0) && (s.indexOf('e') < 0) && (s.indexOf('E') < 0)) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places.<p>
     * 
     * A backslash will be inserted, allowing JSON
     * text to be delivered in HTML. In JSON text, a string cannot contain a
     * control character or an unescaped quote or backslash.<p>
     * 
     * @param string a String
     * @return  a String correctly formatted for insertion in a JSON text
     */
    public static String quote(String string) {

        if ((string == null) || (string.length() == 0)) {
            return "\"\"";
        }

        char b;
        char c = 0;
        int i;
        int len = string.length();
        StringBuffer sb = new StringBuffer(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    if (b == '<') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if ((c < ' ') || ((c >= '\u0080') && (c < '\u00a0')) || ((c >= '\u2000') && (c < '\u2100'))) {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Throws an exception if the object is an NaN or infinite number.<p>
     * 
     * @param o the object to test
     * @throws JSONException if o is a non-finite number
     */
    static void testValidity(Object o) throws JSONException {

        if (o != null) {
            if (o instanceof Double) {
                if (((Double)o).isInfinite() || ((Double)o).isNaN()) {
                    throw new JSONException("JSON does not allow non-finite numbers.");
                }
            } else if (o instanceof Float) {
                if (((Float)o).isInfinite() || ((Float)o).isNaN()) {
                    throw new JSONException("JSON does not allow non-finite numbers.");
                }
            }
        }
    }

    /**
     * Make a JSON text of an Object value.<p>
     * 
     * If the object has an value.toJSONString() method, then that method will be used to produce
     * the JSON text. The method is required to produce a strictly
     * conforming text. If the object does not contain a toJSONString
     * method (which is the most common case), then a text will be
     * produced by other means. If the value is an array or Collection,
     * then a JSONArray will be made from it and its toJSONString method
     * will be called. If the value is a MAP, then a JSONObject will be made
     * from it and its toJSONString method will be called. Otherwise, the
     * value's toString method will be called, and the result will be quoted.<p>
     * 
     * Warning: This method assumes that the data structure is acyclical.<p>
     * 
     * @param value the value to be serialized
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>
     * @throws JSONException if the value is or contains an invalid number
     */
    static String valueToString(Object value) throws JSONException {

        if ((value == null) || value.equals(null)) {
            return "null";
        }
        if (value instanceof I_JSONString) {
            Object o;
            try {
                o = ((I_JSONString)value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            if (o instanceof String) {
                return (String)o;
            }
            throw new JSONException("Bad value from toJSONString: " + o);
        }
        if (value instanceof Number) {
            return numberToString((Number)value);
        }
        if ((value instanceof Boolean) || (value instanceof JSONObject) || (value instanceof JSONArray)) {
            return value.toString();
        }
        if (value instanceof Map) {
            return new JSONObject((Map)value).toString();
        }
        if (value instanceof Collection) {
            return new JSONArray((Collection)value).toString();
        }
        if (value.getClass().isArray()) {
            return new JSONArray(value).toString();
        }
        return quote(value.toString());
    }

    /**
     * Make a pretty printed JSON text of an object value.<p>
     * 
     * Warning: This method assumes that the data structure is acyclical.<p>
     * 
     * @param value the value to be serialized
     * @param indentFactor the number of spaces to add to each level of
     *  indentation
     * @param indent the indentation of the top level
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>
     * @throws JSONException if the object contains an invalid number
     */
    static String valueToString(Object value, int indentFactor, int indent) throws JSONException {

        if ((value == null) || value.equals(null)) {
            return "null";
        }
        try {
            if (value instanceof I_JSONString) {
                Object o = ((I_JSONString)value).toJSONString();
                if (o instanceof String) {
                    return (String)o;
                }
            }
        } catch (Exception e) {
            /* forget about it */
        }
        if (value instanceof Number) {
            return numberToString((Number)value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof JSONObject) {
            return ((JSONObject)value).toString(indentFactor, indent);
        }
        if (value instanceof JSONArray) {
            return ((JSONArray)value).toString(indentFactor, indent);
        }
        if (value instanceof Map) {
            return new JSONObject((Map)value).toString(indentFactor, indent);
        }
        if (value instanceof Collection) {
            return new JSONArray((Collection)value).toString(indentFactor, indent);
        }
        if (value.getClass().isArray()) {
            return new JSONArray(value).toString(indentFactor, indent);
        }
        return quote(value.toString());
    }

    /**
     * Accumulate values under a key.<p>
     * 
     * It is similar to the put method except
     * that if there is already an object stored under the key then a
     * JSONArray is stored under the key to hold all of the accumulated values.
     * If there is already a JSONArray, then the new value is appended to it.
     * In contrast, the put method replaces the previous value.<p>
     * 
     * @param key   a key string
     * @param value an object to be accumulated under the key
     * @return this
     * @throws JSONException if the value is an invalid number or if the key is null
     */
    public JSONObject accumulate(String key, Object value) throws JSONException {

        testValidity(value);
        Object o = opt(key);
        if (o == null) {
            put(key, value instanceof JSONArray ? new JSONArray().put(value) : value);
        } else if (o instanceof JSONArray) {
            ((JSONArray)o).put(value);
        } else {
            put(key, new JSONArray().put(o).put(value));
        }
        return this;
    }

    /**
     * Append values to the array under a key.<p>
     * 
     * If the key does not exist in the
     * JSONObject, then the key is put in the JSONObject with its value being a
     * JSONArray containing the value parameter. If the key was already
     * associated with a JSONArray, then the value parameter is appended to it.<p>
     * 
     * @param key   a key string
     * @param value an object to be accumulated under the key
     * @return this
     * @throws JSONException if the key is null or if the current value
     *  associated with the key is not a JSONArray
     */
    public JSONObject append(String key, Object value) throws JSONException {

        testValidity(value);
        Object o = opt(key);
        if (o == null) {
            put(key, new JSONArray().put(value));
        } else if (o instanceof JSONArray) {
            put(key, ((JSONArray)o).put(value));
        } else {
            throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
        }
        return this;
    }

    /**
     * Get the value object associated with a key.<p>
     *
     * @param key   a key string
     * @return      the object associated with the key
     * @throws   JSONException if the key is not found
     */
    public Object get(String key) throws JSONException {

        Object o = opt(key);
        if (o == null) {
            throw new JSONException("JSONObject[" + quote(key) + "] not found.");
        }
        return o;
    }

    /**
     * Get the boolean value associated with a key.<p>
     *
     * @param key   A key string
     * @return      the truth
     * @throws   JSONException if the value is not a Boolean or the String "true" or "false"
     */
    public boolean getBoolean(String key) throws JSONException {

        Object o = get(key);
        if (o.equals(Boolean.FALSE) || ((o instanceof String) && ((String)o).equalsIgnoreCase("false"))) {
            return false;
        } else if (o.equals(Boolean.TRUE) || ((o instanceof String) && ((String)o).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new JSONException("JSONObject[" + quote(key) + "] is not a Boolean.");
    }

    /**
     * Get the double value associated with a key.<p>
     * 
     * @param key   a key string
     * @return      the numeric value
     * @throws JSONException if the key is not found or
     *  if the value is not a Number object and cannot be converted to a number
     */
    public double getDouble(String key) throws JSONException {

        Object o = get(key);
        try {
            return o instanceof Number ? ((Number)o).doubleValue() : Double.valueOf((String)o).doubleValue();
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + quote(key) + "] is not a number.");
        }
    }

    /**
     * Get the int value associated with a key.<p>
     * 
     * If the number value is too  large for an int, it will be clipped.<p>
     *
     * @param key   a key string
     * @return      the integer value
     * @throws   JSONException if the key is not found or if the value cannot
     *  be converted to an integer
     */
    public int getInt(String key) throws JSONException {

        Object o = get(key);
        return o instanceof Number ? ((Number)o).intValue() : (int)getDouble(key);
    }

    /**
     * Get the JSONArray value associated with a key.<p>
     *
     * @param key   a key string
     * @return      a JSONArray which is the value
     * @throws   JSONException if the key is not found or
     *  if the value is not a JSONArray
     */
    public JSONArray getJSONArray(String key) throws JSONException {

        Object o = get(key);
        if (o instanceof JSONArray) {
            return (JSONArray)o;
        }
        throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONArray.");
    }

    /**
     * Get the JSONObject value associated with a key.<p>
     *
     * @param key   a key string
     * @return      a JSONObject which is the value
     * @throws   JSONException if the key is not found or
     *  if the value is not a JSONObject
     */
    public JSONObject getJSONObject(String key) throws JSONException {

        Object o = get(key);
        if (o instanceof JSONObject) {
            return (JSONObject)o;
        }
        throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONObject.");
    }

    /**
     * Get the long value associated with a key.<p>
     * 
     * If the number value is too long for a long, it will be clipped.<p>
     *
     * @param key   a key string
     * @return      the long value.
     * @throws   JSONException if the key is not found or if the value cannot
     *  be converted to a long
     */
    public long getLong(String key) throws JSONException {

        Object o = get(key);
        return o instanceof Number ? ((Number)o).longValue() : (long)getDouble(key);
    }

    /**
     * Get the string associated with a key.<p>
     *
     * @param key   a key string
     * @return      a string which is the value
     * @throws   JSONException if the key is not found
     */
    public String getString(String key) throws JSONException {

        return get(key).toString();
    }

    /**
     * Determine if the JSONObject contains a specific key.<p>
     * 
     * @param key   a key string
     * @return      true if the key exists in the JSONObject
     */
    public boolean has(String key) {

        return this.m_map.containsKey(key);
    }

    /**
     * Determine if the value associated with the key is null or if there is no value.<p>
     * 
     * @param key   a key string
     * @return      true if there is no value associated with the key or if
     *  the value is the JSONObject.NULL object
     */
    public boolean isNull(String key) {

        return JSONObject.NULL.equals(opt(key));
    }

    /**
     * Get an enumeration of the keys of the JSONObject.<p>
     *
     * @return an iterator of the keys
     */
    public Iterator<String> keys() {

        return this.m_map.keySet().iterator();
    }

    /**
     * Get the number of keys stored in the JSONObject.<p>
     *
     * @return The number of keys in the JSONObject
     */
    public int length() {

        return this.m_map.size();
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

    /**
     * Produce a JSONArray containing the names of the elements of this JSONObject.<p>
     * 
     * @return a JSONArray containing the key strings, or null if the JSONObject is empty.
     */
    public JSONArray names() {

        JSONArray ja = new JSONArray();
        Iterator<String> keys = keys();
        while (keys.hasNext()) {
            ja.put(keys.next());
        }
        return ja.length() == 0 ? null : ja;
    }

    /**
     * Get an optional value associated with a key.<p>
     * 
     * @param key   a key string
     * @return      an object which is the value, or null if there is no value
     */
    public Object opt(String key) {

        return key == null ? null : this.m_map.get(key);
    }

    /**
     * Get an optional boolean associated with a key.<p>
     * 
     * It returns false if there is no such key, or if the value is not
     * Boolean.TRUE or the String "true".<p>
     *
     * @param key   a key string
     * @return      the truth
     */
    public boolean optBoolean(String key) {

        return optBoolean(key, false);
    }

    /**
     * Get an optional boolean associated with a key.<p>
     * 
     * It returns the defaultValue if there is no such key, or if it is not
     * a Boolean or the String "true" or "false" (case insensitive).<p>
     *
     * @param key              a key string
     * @param defaultValue     the default
     * @return      the truth
     */
    public boolean optBoolean(String key, boolean defaultValue) {

        try {
            return getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional double associated with a key,
     * or NaN if there is no such key or if its value is not a number.<p>
     * 
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.<p>
     *
     * @param key   a string which is the key
     * @return      an object which is the value
     */
    public double optDouble(String key) {

        return optDouble(key, Double.NaN);
    }

    /**
     * Get an optional double associated with a key, or the
     * defaultValue if there is no such key or if its value is not a number.<p>
     * 
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.<p>
     *
     * @param key   a key string
     * @param defaultValue     the default
     * @return      an object which is the value
     */
    public double optDouble(String key, double defaultValue) {

        try {
            Object o = opt(key);
            return o instanceof Number ? ((Number)o).doubleValue() : new Double((String)o).doubleValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional int value associated with a key,
     * or zero if there is no such key or if the value is not a number.<p>
     * 
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.<p>
     *
     * @param key   a key string
     * @return      an object which is the value
     */
    public int optInt(String key) {

        return optInt(key, 0);
    }

    /**
     * Get an optional int value associated with a key,
     * or the default if there is no such key or if the value is not a number.<p>
     * 
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.<p>
     *
     * @param key   a key string
     * @param defaultValue     the default
     * @return      an object which is the value
     */
    public int optInt(String key, int defaultValue) {

        try {
            return getInt(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional JSONArray associated with a key.<p>
     * 
     * It returns null if there is no such key, or if its value is not a
     * JSONArray.<p>
     *
     * @param key   a key string
     * @return      a JSONArray which is the value
     */
    public JSONArray optJSONArray(String key) {

        Object o = opt(key);
        return o instanceof JSONArray ? (JSONArray)o : null;
    }

    /**
     * Get an optional JSONObject associated with a key.<p>
     * 
     * It returns null if there is no such key, or if its value is not a
     * JSONObject.<p>
     *
     * @param key   a key string
     * @return      a JSONObject which is the value
     */
    public JSONObject optJSONObject(String key) {

        Object o = opt(key);
        return o instanceof JSONObject ? (JSONObject)o : null;
    }

    /**
     * Get an optional long value associated with a key,
     * or zero if there is no such key or if the value is not a number.<p>
     * 
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.<p>
     *
     * @param key   a key string
     * @return      an object which is the value
     */
    public long optLong(String key) {

        return optLong(key, 0);
    }

    /**
     * Get an optional long value associated with a key,
     * or the default if there is no such key or if the value is not a number.<p>
     * 
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.<p>
     *
     * @param key   a key string
     * @param defaultValue     the default
     * @return      an object which is the value
     */
    public long optLong(String key, long defaultValue) {

        try {
            return getLong(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional string associated with a key.<p>
     * 
     * It returns an empty string if there is no such key. If the value is not
     * a string and is not null, then it is coverted to a string.<p>
     *
     * @param key   a key string
     * @return      a string which is the value
     */
    public String optString(String key) {

        return optString(key, "");
    }

    /**
     * Get an optional string associated with a key.
     * It returns the defaultValue if there is no such key.<p>
     *
     * @param key   a key string
     * @param defaultValue     the default
     * @return      a string which is the value
     */
    public String optString(String key, String defaultValue) {

        Object o = opt(key);
        return o != null ? o.toString() : defaultValue;
    }

    /**
     * Put a key/boolean pair in the JSONObject.<p>
     *
     * @param key   a key string
     * @param value a boolean which is the value
     * @return this
     * @throws JSONException if the key is null
     */
    public JSONObject put(String key, boolean value) throws JSONException {

        put(key, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONArray which is produced from a Collection.<p>
     * 
     * @param key   a key string
     * @param value a Collection value
     * @return      this
     * @throws JSONException if something goes wrong
     */
    public JSONObject put(String key, Collection<Object> value) throws JSONException {

        put(key, new JSONArray(value));
        return this;
    }

    /**
     * Put a key/double pair in the JSONObject.<p>
     *
     * @param key   a key string
     * @param value a double which is the value
     * @return this
     * @throws JSONException if the key is null or if the number is invalid.
     */
    public JSONObject put(String key, double value) throws JSONException {

        put(key, new Double(value));
        return this;
    }

    /**
     * Put a key/int pair in the JSONObject.<p>
     *
     * @param key   a key string
     * @param value an int which is the value
     * @return this
     * @throws JSONException if the key is null
     */
    public JSONObject put(String key, int value) throws JSONException {

        put(key, new Integer(value));
        return this;
    }

    /**
     * Put a key/long pair in the JSONObject.<p>
     *
     * @param key   a key string
     * @param value a long which is the value
     * @return this
     * @throws JSONException If the key is null
     */
    public JSONObject put(String key, long value) throws JSONException {

        put(key, new Long(value));
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONObject which is produced from a Map.<p>
     * 
     * @param key   a key string
     * @param value a Map value
     * @return      this
     * @throws JSONException if something goes wrong
     */
    public JSONObject put(String key, Map<String, Object> value) throws JSONException {

        put(key, new JSONObject(value));
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject.<p>
     * 
     * If the value is null,
     * then the key will be removed from the JSONObject if it is present.<p>
     * 
     * @param key   a key string
     * @param value an object which is the value. It should be of one of these
     *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *  or the JSONObject.NULL object
     * @return this
     * @throws JSONException if the value is non-finite number
     *  or if the key is null.
     */
    public JSONObject put(String key, Object value) throws JSONException {

        if (key == null) {
            throw new JSONException("Null key.");
        }
        if (value != null) {
            testValidity(value);
            this.m_map.put(key, value);
        } else {
            remove(key);
        }
        return this;
    }

    /**
     * Put a key/value pair in the JSONObject, but only if the
     * key and the value are both non-null.<p>
     * 
     * @param key   a key string
     * @param value an object which is the value. It should be of one of these
     *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *  or the JSONObject.NULL object
     * @return this
     * @throws JSONException if the value is a non-finite number.
     */
    public JSONObject putOpt(String key, Object value) throws JSONException {

        if ((key != null) && (value != null)) {
            put(key, value);
        }
        return this;
    }

    /**
     * Remove a name and its value, if present.<p>
     * 
     * @param key the name to be removed
     * @return the value that was associated with the name,
     * or null if there was no value
     */
    public Object remove(String key) {

        return this.m_map.remove(key);
    }

    /**
     * Get an enumeration of the keys of the JSONObject.<p>
     * 
     * The keys will be sorted alphabetically.<p>
     *
     * @return an iterator of the keys
     */
    public Iterator<String> sortedKeys() {

        return new TreeSet<String>(this.m_map.keySet()).iterator();
    }

    /**
     * Produce a JSONArray containing the values of the members of this
     * JSONObject.<p>
     * 
     * @param names a JSONArray containing a list of key strings. This
     * determines the sequence of the values in the result
     * @return a JSONArray of values
     * @throws JSONException if any of the values are non-finite numbers.
     */
    public JSONArray toJSONArray(JSONArray names) throws JSONException {

        if ((names == null) || (names.length() == 0)) {
            return null;
        }
        JSONArray ja = new JSONArray();
        for (int i = 0; i < names.length(); i += 1) {
            ja.put(this.opt(names.getString(i)));
        }
        return ja;
    }

    /**
     * Make a JSON text of this JSONObject.<p>
     * 
     * For compactness, no whitespace
     * is added. If this would not result in a syntactically correct JSON text,
     * then null will be returned instead.<p>
     * 
     * Warning: This method assumes that the data structure is acyclical.<p>
     *
     * @return a printable, displayable, portable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     */
    @Override
    public String toString() {

        try {
            Iterator<String> keys = keys();
            StringBuffer sb = new StringBuffer("{");

            while (keys.hasNext()) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                Object o = keys.next();
                sb.append(quote(o.toString()));
                sb.append(':');
                sb.append(valueToString(this.m_map.get(o)));
            }
            sb.append('}');
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Make a pretty printed JSON text of this JSONObject.<p>
     * 
     * Warning: This method assumes that the data structure is acyclical.<p>
     * 
     * @param indentFactor the number of spaces to add to each level of
     *  indentation
     * @return a printable, displayable, portable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>
     * @throws JSONException If the object contains an invalid number
     */
    public String toString(int indentFactor) throws JSONException {

        return toString(indentFactor, 0);
    }

    /**
     * Write the contents of the JSONObject as JSON text to a writer.
     * For compactness, no whitespace is added.<p>
     * 
     * Warning: This method assumes that the data structure is acyclical.<p>
     *
     * @param writer the writer to write the contents to
     * @return the writer
     * @throws JSONException if something goes wrong
     */
    public Writer write(Writer writer) throws JSONException {

        try {
            boolean b = false;
            Iterator<String> keys = keys();
            writer.write('{');

            while (keys.hasNext()) {
                if (b) {
                    writer.write(',');
                }
                String k = keys.next();
                writer.write(quote(k.toString()));
                writer.write(':');
                Object v = this.m_map.get(k);
                if (v instanceof JSONObject) {
                    ((JSONObject)v).write(writer);
                } else if (v instanceof JSONArray) {
                    ((JSONArray)v).write(writer);
                } else {
                    writer.write(valueToString(v));
                }
                b = true;
            }
            writer.write('}');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Make a pretty printed JSON text of this JSONObject.<p>
     * 
     * Warning: This method assumes that the data structure is acyclical.<p>
     * 
     * @param indentFactor the number of spaces to add to each level of
     *  indentation
     * @param indent the indentation of the top level
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>
     * @throws JSONException if the object contains an invalid number
     */
    String toString(int indentFactor, int indent) throws JSONException {

        int j;
        int n = length();
        if (n == 0) {
            return "{}";
        }
        Iterator<String> keys = sortedKeys();
        StringBuffer sb = new StringBuffer("{");
        int newindent = indent + indentFactor;
        String key;
        if (n == 1) {
            key = keys.next();
            sb.append(quote(key));
            sb.append(": ");
            sb.append(valueToString(this.m_map.get(key), indentFactor, indent));
        } else {
            while (keys.hasNext()) {
                key = keys.next();
                if (sb.length() > 1) {
                    sb.append(",\n");
                } else {
                    sb.append('\n');
                }
                for (j = 0; j < newindent; j += 1) {
                    sb.append(' ');
                }
                sb.append(quote(key));
                sb.append(": ");
                sb.append(valueToString(this.m_map.get(key), indentFactor, newindent));
            }
            if (sb.length() > 1) {
                sb.append('\n');
                for (j = 0; j < indent; j += 1) {
                    sb.append(' ');
                }
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private boolean isStandardProperty(Class clazz) {

        return clazz.isPrimitive()
            || clazz.isAssignableFrom(Byte.class)
            || clazz.isAssignableFrom(Short.class)
            || clazz.isAssignableFrom(Integer.class)
            || clazz.isAssignableFrom(Long.class)
            || clazz.isAssignableFrom(Float.class)
            || clazz.isAssignableFrom(Double.class)
            || clazz.isAssignableFrom(Character.class)
            || clazz.isAssignableFrom(String.class)
            || clazz.isAssignableFrom(Boolean.class);
    }

    private void populateInternalMap(Object bean, boolean includeSuperClass) {

        Class klass = bean.getClass();

        //If klass.getSuperClass is System class then includeSuperClass = false;

        if (klass.getClassLoader() == null) {
            includeSuperClass = false;
        }

        Method[] methods = (includeSuperClass) ? klass.getMethods() : klass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i += 1) {
            try {
                Method method = methods[i];
                String name = method.getName();
                String key = "";
                if (name.startsWith("get")) {
                    key = name.substring(3);
                } else if (name.startsWith("is")) {
                    key = name.substring(2);
                }
                if ((key.length() > 0)
                    && Character.isUpperCase(key.charAt(0))
                    && (method.getParameterTypes().length == 0)) {
                    if (key.length() == 1) {
                        key = key.toLowerCase();
                    } else if (!Character.isUpperCase(key.charAt(1))) {
                        key = key.substring(0, 1).toLowerCase() + key.substring(1);
                    }

                    Object result = method.invoke(bean, (Object[])null);
                    if (result == null) {
                        m_map.put(key, NULL);
                    } else if (result.getClass().isArray()) {
                        m_map.put(key, new JSONArray(result, includeSuperClass));
                    } else if (result instanceof Collection) { //List or Set
                        m_map.put(key, new JSONArray((Collection)result, includeSuperClass));
                    } else if (result instanceof Map) {
                        m_map.put(key, new JSONObject((Map)result, includeSuperClass));
                    } else if (isStandardProperty(result.getClass())) { //Primitives, String and Wrapper
                        m_map.put(key, result);
                    } else {
                        if (result.getClass().getPackage().getName().startsWith("java")
                            || (result.getClass().getClassLoader() == null)) {
                            m_map.put(key, result.toString());
                        } else { //User defined Objects
                            m_map.put(key, new JSONObject(result, includeSuperClass));
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

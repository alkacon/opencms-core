/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * This file is based on:
 * org.json.JSONArray
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coersion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 *     before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there
 *     is <code>,</code>&nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 *     quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 *     or single quote, and if they do not contain leading or trailing spaces,
 *     and if they do not contain any of these characters:
 *     <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
 *     and if they are not the reserved words <code>true</code>,
 *     <code>false</code>, or <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as
 *     well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or
 *     <code>0x-</code> <small>(hex)</small> prefix.</li>
 * <li>Comments written in the slashshlash, slashstar, and hash conventions
 *     will be ignored.</li>
 * </ul>

 */
public class JSONArray {

    /**
     * The arrayList where the JSONArray's properties are kept.
     */
    private ArrayList<Object> m_myArrayList;

    /**
     * Construct an empty JSONArray.<p>
     */
    public JSONArray() {

        m_myArrayList = new ArrayList<Object>();
    }

    /**
     * Construct a JSONArray from a Collection.<p>
     *
     * @param collection a Collection.
     */
    public JSONArray(Collection<?> collection) {

        m_myArrayList = (collection == null) ? new ArrayList<Object>() : new ArrayList<Object>(collection);
    }

    /**
     * Construct a JSONArray from a collection of beans.<p>
     *
     * The collection should have Java Beans.<p>
     *
     * @param collection a collection
     * @param includeSuperClass tell whether to include the super class properties
     */

    public JSONArray(Collection<Object> collection, boolean includeSuperClass) {

        m_myArrayList = new ArrayList<Object>();
        if (collection != null) {
            for (Iterator<Object> iter = collection.iterator(); iter.hasNext();) {
                m_myArrayList.add(new JSONObject(iter.next(), includeSuperClass));
            }
        }
    }

    /**
     * Construct a JSONArray from a JSONTokener.<p>
     *
     * @param x a JSONTokener
     * @throws JSONException if there is a syntax error
     */
    public JSONArray(JSONTokener x)
    throws JSONException {

        this();
        char c = x.nextClean();
        char q;
        if (c == '[') {
            q = ']';
        } else if (c == '(') {
            q = ')';
        } else {
            throw x.syntaxError("A JSONArray text must start with '['");
        }
        if (x.nextClean() == ']') {
            return;
        }
        x.back();
        for (;;) {
            if (x.nextClean() == ',') {
                x.back();
                m_myArrayList.add(null);
            } else {
                x.back();
                m_myArrayList.add(x.nextValue());
            }
            c = x.nextClean();
            switch (c) {
                case ';':
                case ',':
                    if (x.nextClean() == ']') {
                        return;
                    }
                    x.back();
                    break;
                case ']':
                case ')':
                    if (q != c) {
                        throw x.syntaxError("Expected a '" + Character.valueOf(q) + "'");
                    }
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or ']'");
            }
        }
    }

    /**
     * Construct a JSONArray from an array.<p>
     *
     * @param array an array
     * @throws JSONException if not an array
     */
    public JSONArray(Object array)
    throws JSONException {

        this();
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i += 1) {
                this.put(Array.get(array, i));
            }
        } else {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
    }

    /**
     * Construct a JSONArray from an array with a bean.<p>
     *
     * The array should have Java Beans.<p>
     *
     * @param array an array
     * @param includeSuperClass tell whether to include the super class properties
     * @throws JSONException if not an array
     */
    public JSONArray(Object array, boolean includeSuperClass)
    throws JSONException {

        this();
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i += 1) {
                this.put(new JSONObject(Array.get(array, i), includeSuperClass));
            }
        } else {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
    }

    /**
     * Construct a JSONArray from a source JSON text.<p>
     *
     * @param source     a string that begins with
     * <code>[</code>&nbsp;<small>(left bracket)</small>
     *  and ends with <code>]</code>&nbsp;<small>(right bracket)</small>
     *  @throws JSONException if there is a syntax error
     */
    public JSONArray(String source)
    throws JSONException {

        this(new JSONTokener(source));
    }

    /**
     * Appends values from another JSON array.
     * 
     * @param array the array whose values should be appended 
     */
    public void append(JSONArray array) {

        for (int i = 0; i < array.length(); i++) {
            put(array.opt(i));
        }
    }

    /**
     * Check if this array contains the given string value.<p>
     *
     * @param value the value to check
     *
     * @return <code>true</code> if found, <code>false</code> if not
     */
    public boolean containsString(String value) {

        return m_myArrayList.contains(value);
    }

    /**
     * Get the object value associated with an index.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return an object value
     * @throws JSONException if there is no value for the index
     */
    public Object get(int index) throws JSONException {

        Object o = opt(index);
        if (o == null) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        return o;
    }

    /**
     * Get the boolean value associated with an index.<p>
     *
     * The string values "true" and "false" are converted to boolean.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return the truth
     * @throws JSONException if there is no value for the index or if the value is not convertable to boolean
     */
    public boolean getBoolean(int index) throws JSONException {

        Object o = get(index);
        if (o.equals(Boolean.FALSE) || ((o instanceof String) && ((String)o).equalsIgnoreCase("false"))) {
            return false;
        } else if (o.equals(Boolean.TRUE) || ((o instanceof String) && ((String)o).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new JSONException("JSONArray[" + index + "] is not a Boolean.");
    }

    /**
     * Get the double value associated with an index.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return the value
     * @throws   JSONException if the key is not found or if the value cannot be converted to a number
     */
    public double getDouble(int index) throws JSONException {

        Object o = get(index);
        try {
            return o instanceof Number ? ((Number)o).doubleValue() : Double.valueOf((String)o).doubleValue();
        } catch (Exception e) {
            throw new JSONException("JSONArray[" + index + "] is not a number.");
        }
    }

    /**
     * Get the int value associated with an index.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return the value
     * @throws   JSONException if the key is not found or if the value cannot be converted to a number
     */
    public int getInt(int index) throws JSONException {

        Object o = get(index);
        return o instanceof Number ? ((Number)o).intValue() : (int)getDouble(index);
    }

    /**
     * Get the JSONArray associated with an index.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return a JSONArray value
     * @throws JSONException if there is no value for the index or if the value is not a JSONArray
     */
    public JSONArray getJSONArray(int index) throws JSONException {

        Object o = get(index);
        if (o instanceof JSONArray) {
            return (JSONArray)o;
        }
        throw new JSONException("JSONArray[" + index + "] is not a JSONArray.");
    }

    /**
     * Get the JSONObject associated with an index.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return a JSONObject value
     * @throws JSONException if there is no value for the index or if the value is not a JSONObject
     */
    public JSONObject getJSONObject(int index) throws JSONException {

        Object o = get(index);
        if (o instanceof JSONObject) {
            return (JSONObject)o;
        }
        throw new JSONException("JSONArray[" + index + "] is not a JSONObject.");
    }

    /**
     * Get the long value associated with an index.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return the value
     * @throws   JSONException if the key is not found or if the value cannot be converted to a number
     */
    public long getLong(int index) throws JSONException {

        Object o = get(index);
        return o instanceof Number ? ((Number)o).longValue() : (long)getDouble(index);
    }

    /**
     * Get the string associated with an index.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return a string value
     * @throws JSONException if there is no value for the index
     */
    public String getString(int index) throws JSONException {

        return get(index).toString();
    }

    /**
     * Determine if the value is null.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return true if the value at the index is null, or if there is no value
     */
    public boolean isNull(int index) {

        return JSONObject.NULL.equals(opt(index));
    }

    /**
     * Make a string from the contents of this JSONArray.<p>
     *
     * The <code>separator</code> string is inserted between each element.<p>
     *
     * Warning: This method assumes that the data structure is acyclical.<p>
     *
     * @param separator a string that will be inserted between the elements
     * @return a string
     * @throws JSONException if the array contains an invalid number
     */
    public String join(String separator) throws JSONException {

        int len = length();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < len; i += 1) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(JSONObject.valueToString(m_myArrayList.get(i)));
        }
        return sb.toString();
    }

    /**
     * Get the number of elements in the JSONArray, included nulls.<p>
     *
     * @return the length (or size)
     */
    public int length() {

        return m_myArrayList.size();
    }

    /**
     * Get the optional object value associated with an index.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return      an object value, or null if there is no object at that index
     */
    public Object opt(int index) {

        return ((index < 0) || (index >= length())) ? null : m_myArrayList.get(index);
    }

    /**
     * Get the optional boolean value associated with an index.<p>
     *
     * It returns false if there is no value at that index,
     * or if the value is not Boolean.TRUE or the String "true".<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return the truth
     */
    public boolean optBoolean(int index) {

        return optBoolean(index, false);
    }

    /**
     * Get the optional boolean value associated with an index.<p>
     *
     * It returns the defaultValue if there is no value at that index or if
     * it is not a Boolean or the String "true" or "false" (case insensitive).<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @param defaultValue a boolean default
     * @return the truth
     */
    public boolean optBoolean(int index, boolean defaultValue) {

        try {
            return getBoolean(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional double value associated with an index.<p>
     *
     * NaN is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return the value
     */
    public double optDouble(int index) {

        return optDouble(index, Double.NaN);
    }

    /**
     * Get the optional double value associated with an index.<p>
     *
     * The defaultValue is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @param defaultValue the default value
     * @return the value
     */
    public double optDouble(int index, double defaultValue) {

        try {
            return getDouble(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional int value associated with an index.<p>
     *
     * Zero is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return the value
     */
    public int optInt(int index) {

        return optInt(index, 0);
    }

    /**
     * Get the optional int value associated with an index.<p>
     *
     * The defaultValue is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @param defaultValue the default value
     * @return the value
     */
    public int optInt(int index, int defaultValue) {

        try {
            return getInt(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional JSONArray associated with an index.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return aA JSONArray value, or null if the index has no value, or if the value is not a JSONArray
     */
    public JSONArray optJSONArray(int index) {

        Object o = opt(index);
        return o instanceof JSONArray ? (JSONArray)o : null;
    }

    /**
     * Get the optional JSONObject associated with an index.<p>
     *
     * Null is returned if the key is not found, or null if the index has
     * no value, or if the value is not a JSONObject.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return a JSONObject value
     */
    public JSONObject optJSONObject(int index) {

        Object o = opt(index);
        return o instanceof JSONObject ? (JSONObject)o : null;
    }

    /**
     * Get the optional long value associated with an index.<p>
     *
     * Zero is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return the value
     */
    public long optLong(int index) {

        return optLong(index, 0);
    }

    /**
     * Get the optional long value associated with an index.<p>
     *
     * The defaultValue is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @param defaultValue the default value
     * @return the value
     */
    public long optLong(int index, long defaultValue) {

        try {
            return getLong(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get the optional string value associated with an index.<p>
     *
     * It returns an empty string if there is no value at that index. If the value
     * is not a string and is not null, then it is coverted to a string.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @return a String value
     */
    public String optString(int index) {

        return optString(index, "");
    }

    /**
     * Get the optional string associated with an index.<p>
     *
     * The defaultValue is returned if the key is not found.<p>
     *
     * @param index tThe index must be between 0 and length() - 1
     * @param defaultValue the default value
     * @return a String value
     */
    public String optString(int index, String defaultValue) {

        Object o = opt(index);
        return o != null ? o.toString() : defaultValue;
    }

    /**
     * Append a boolean value. This increases the array's length by one.<p>
     *
     * @param value a boolean value
     * @return this
     */
    public JSONArray put(boolean value) {

        put(value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONArray which is produced from a Collection.<p>
     *
     * @param value a Collection value
     * @return this
     */
    public JSONArray put(Collection<Object> value) {

        put(new JSONArray(value));
        return this;
    }

    /**
     * Append a double value. This increases the array's length by one.<p>
     *
     * @param value a double value
     * @throws JSONException if the value is not finite
     * @return this
     */
    public JSONArray put(double value) throws JSONException {

        Double d = Double.valueOf(value);
        JSONObject.testValidity(d);
        put(d);
        return this;
    }

    /**
     * Append an int value. This increases the array's length by one.<p>
     *
     * @param value an int value
     * @return this
     */
    public JSONArray put(int value) {

        put(Integer.valueOf(value));
        return this;
    }

    /**
     * Put or replace a boolean value in the JSONArray. If the index is greater
     * than the length of the JSONArray, then null elements will be added as
     * necessary to pad it out.<p>
     *
     * @param index the index
     * @param value a boolean value
     * @return this
     * @throws JSONException if the index is negative
     */
    public JSONArray put(int index, boolean value) throws JSONException {

        put(index, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONArray which is produced from a Collection.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @param value a Collection value
     * @return this
     * @throws JSONException if the index is negative or if the value is
     * not finite
     */
    public JSONArray put(int index, Collection<Object> value) throws JSONException {

        put(index, new JSONArray(value));
        return this;
    }

    /**
     * Put or replace a double value. If the index is greater than the length of
     * the JSONArray, then null elements will be added as necessary to pad
     * it out.<p>
     *
     * @param index the index
     * @param value a double value
     * @return this
     * @throws JSONException if the index is negative or if the value is
     * not finite
     */
    public JSONArray put(int index, double value) throws JSONException {

        put(index, Double.valueOf(value));
        return this;
    }

    /**
     * Put or replace an int value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.<p>
     *
     * @param index the index
     * @param value an int value
     * @return this
     * @throws JSONException if the index is negative
     */
    public JSONArray put(int index, int value) throws JSONException {

        put(index, Integer.valueOf(value));
        return this;
    }

    /**
     * Put or replace a long value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.<p>
     *
     * @param index the index
     * @param value a long value
     * @return this
     * @throws JSONException if the index is negative
     */
    public JSONArray put(int index, long value) throws JSONException {

        put(index, Long.valueOf(value));
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONObject which is produced from a Map.<p>
     *
     * @param index the index must be between 0 and length() - 1
     * @param value the Map value
     * @return this
     * @throws JSONException if the index is negative or if the the value is
     *  an invalid number
     */
    public JSONArray put(int index, Map<?, ?> value) throws JSONException {

        put(index, new JSONObject(value));
        return this;
    }

    /**
     * Put or replace an object value in the JSONArray. If the index is greater
     *  than the length of the JSONArray, then null elements will be added as
     *  necessary to pad it out.<p>
     *
     * @param index the index
     * @param value the value to put into the array. The value should be a
     *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
     *  JSONObject.NULL object
     * @return this
     * @throws JSONException if the index is negative or if the the value is
     *  an invalid number
     */
    public JSONArray put(int index, Object value) throws JSONException {

        JSONObject.testValidity(value);
        if (index < 0) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        if (index < length()) {
            m_myArrayList.set(index, value);
        } else {
            while (index != length()) {
                put(JSONObject.NULL);
            }
            put(value);
        }
        return this;
    }

    /**
     * Append an long value. This increases the array's length by one.<p>
     *
     * @param value a long value
     * @return this
     */
    public JSONArray put(long value) {

        put(Long.valueOf(value));
        return this;
    }

    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONObject which is produced from a Map.<p>
     *
     * @param value a Map value
     * @return      this
     */
    public JSONArray put(Map<?, ?> value) {

        put(new JSONObject(value));
        return this;
    }

    /**
     * Append an object value. This increases the array's length by one.<p>
     *
     * @param value an object value.  The value should be a
     *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
     *  JSONObject.NULL object
     * @return this
     */
    public JSONArray put(Object value) {

        m_myArrayList.add(value);
        return this;
    }

    /**
     * Produce a JSONObject by combining a JSONArray of names with the values
     * of this JSONArray.<p>
     *
     * @param names a JSONArray containing a list of key strings. These will be
     * paired with the values
     * @return a JSONObject, or null if there are no names or if this JSONArray
     * has no values
     * @throws JSONException if any of the names are null
     */
    public JSONObject toJSONObject(JSONArray names) throws JSONException {

        if ((names == null) || (names.length() == 0) || (length() == 0)) {
            return null;
        }
        JSONObject jo = new JSONObject();
        for (int i = 0; i < names.length(); i += 1) {
            jo.put(names.getString(i), opt(i));
        }
        return jo;
    }

    /**
     * Make a JSON text of this JSONArray.<p>
     *
     * For compactness, no unnecessary whitespace is added. If it is not possible to produce a
     * syntactically correct JSON text then null will be returned instead. This
     * could occur if the array contains an invalid number.<p>
     *
     * Warning: This method assumes that the data structure is acyclical.<p>
     *
     * @return a printable, displayable, transmittable representation of the array
     */
    @Override
    public String toString() {

        try {
            return '[' + join(",") + ']';
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Make a pretty printed JSON text of this JSONArray.<p>
     *
     * Warning: This method assumes that the data structure is acyclical.<p>
     *
     * @param indentFactor the number of spaces to add to each level of
     *  indentation
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>[</code>&nbsp;<small>(left bracket)</small> and ending
     *  with <code>]</code>&nbsp;<small>(right bracket)</small>
     * @throws JSONException if something goes wrong
     */
    public String toString(int indentFactor) throws JSONException {

        return toString(indentFactor, 0);
    }

    /**
     * Write the contents of the JSONArray as JSON text to a writer.<p>
     *
     * For compactness, no whitespace is added.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.<p>
     *
     * @param writer the writer to write the contents to
     * @return the writer
     * @throws JSONException if something goes wrong
     */
    public Writer write(Writer writer) throws JSONException {

        try {
            boolean b = false;
            int len = length();

            writer.write('[');

            for (int i = 0; i < len; i += 1) {
                if (b) {
                    writer.write(',');
                }
                Object v = m_myArrayList.get(i);
                if (v instanceof JSONObject) {
                    ((JSONObject)v).write(writer);
                } else if (v instanceof JSONArray) {
                    ((JSONArray)v).write(writer);
                } else {
                    writer.write(JSONObject.valueToString(v));
                }
                b = true;
            }
            writer.write(']');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    /**
     * Make a pretty printed JSON text of this JSONArray.<p>
     *
     * Warning: This method assumes that the data structure is acyclical.<p>
     *
     * @param indentFactor the number of spaces to add to each level of
     *  indentation
     * @param indent the indention of the top level
     * @return a printable, displayable, transmittable
     *  representation of the array
     * @throws JSONException if something goes wrong
     */
    String toString(int indentFactor, int indent) throws JSONException {

        int len = length();
        if (len == 0) {
            return "[]";
        }
        int i;
        StringBuffer sb = new StringBuffer("[");
        if (len == 1) {
            sb.append(JSONObject.valueToString(m_myArrayList.get(0), indentFactor, indent));
        } else {
            int newindent = indent + indentFactor;
            sb.append('\n');
            for (i = 0; i < len; i += 1) {
                if (i > 0) {
                    sb.append(",\n");
                }
                for (int j = 0; j < newindent; j += 1) {
                    sb.append(' ');
                }
                sb.append(JSONObject.valueToString(m_myArrayList.get(i), indentFactor, newindent));
            }
            sb.append('\n');
            for (i = 0; i < indent; i += 1) {
                sb.append(' ');
            }
        }
        sb.append(']');
        return sb.toString();
    }
}

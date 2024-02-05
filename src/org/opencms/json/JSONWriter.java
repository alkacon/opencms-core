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
 * org.json.JSONWriter
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

/**
 * JSONWriter provides a quick and convenient way of producing JSON text.
 * The texts produced strictly conform to JSON syntax rules. No whitespace is
 * added, so the results are ready for transmission or storage. Each instance of
 * JSONWriter can produce one JSON text.
 * <p>
 * A JSONWriter instance provides a <code>value</code> method for appending
 * values to the
 * text, and a <code>key</code>
 * method for adding keys before values in objects. There are <code>array</code>
 * and <code>endArray</code> methods that make and bound array values, and
 * <code>object</code> and <code>endObject</code> methods which make and bound
 * object values. All of these methods return the JSONWriter instance,
 * permitting a cascade style. For example, <pre>
 * new JSONWriter(myWriter)
 *     .object()
 *         .key("JSON")
 *         .value("Hello, World!")
 *     .endObject();</pre> which writes <pre>
 * {"JSON":"Hello, World!"}</pre>
 * <p>
 * The first method called must be <code>array</code> or <code>object</code>.
 * There are no methods for adding commas or colons. JSONWriter adds them for
 * you. Objects and arrays can be nested up to 20 levels deep.
 * <p>
 * This can sometimes be easier than using a JSONObject to build a string.<p>
 *
 */
public class JSONWriter {

    /** The maximum depth. */
    private static final int MAXDEPTH = 20;

    /**
     * The current m_mode. Values:
     * 'a' (array),
     * 'd' (done),
     * 'i' (initial),
     * 'k' (key),
     * 'o' (object).
     */
    protected char m_mode;

    /**
     * The m_writer that will receive the output.
     */
    protected Writer m_writer;

    /**
     * The m_comma flag determines if a m_comma should be output before the next
     * value.
     */
    private boolean m_comma;

    /**
     * The object/array m_stack.
     */
    private char[] m_stack;

    /**
     * The m_stack m_top index. A value of 0 indicates that the m_stack is empty.
     */
    private int m_top;

    /**
     * Make a fresh JSONWriter.<p>
     *
     * It can be used to build one JSON text.<p>
     *
     * @param w the writer to use
     */
    public JSONWriter(Writer w) {

        m_comma = false;
        m_mode = 'i';
        m_stack = new char[MAXDEPTH];
        m_top = 0;
        m_writer = w;
    }

    /**
     * Begin appending a new array.<p>
     *
     * All values until the balancing
     * <code>endArray</code> will be appended to this array. The
     * <code>endArray</code> method must be called to mark the array's end.<p>
     *
     * @return this
     * @throws JSONException if the nesting is too deep, or if the object is
     * started in the wrong place (for example as a key or after the end of the
     * outermost array or object)
     */
    public JSONWriter array() throws JSONException {

        if ((m_mode == 'i') || (m_mode == 'o') || (m_mode == 'a')) {
            push('a');
            append("[");
            m_comma = false;
            return this;
        }
        throw new JSONException("Misplaced array.");
    }

    /**
     * End an array. This method most be called to balance calls to
     * <code>array</code>.<p>
     *
     * @return this
     * @throws JSONException if incorrectly nested
     */
    public JSONWriter endArray() throws JSONException {

        return end('a', ']');
    }

    /**
     * End an object. This method most be called to balance calls to
     * <code>object</code>.<p>
     *
     * @return this
     * @throws JSONException if incorrectly nested
     */
    public JSONWriter endObject() throws JSONException {

        return end('k', '}');
    }

    /**
     * Append a key. The key will be associated with the next value. In an
     * object, every value must be preceded by a key.<p>
     *
     * @param s a key string
     * @return this
     * @throws JSONException if the key is out of place. For example, keys
     *  do not belong in arrays or if the key is null
     */
    public JSONWriter key(String s) throws JSONException {

        if (s == null) {
            throw new JSONException("Null key.");
        }
        if (m_mode == 'k') {
            try {
                if (m_comma) {
                    m_writer.write(',');
                }
                m_writer.write(JSONObject.quote(s));
                m_writer.write(':');
                m_comma = false;
                m_mode = 'o';
                return this;
            } catch (IOException e) {
                throw new JSONException(e);
            }
        }
        throw new JSONException("Misplaced key.");
    }

    /**
     * Begin appending a new object.<p>
     *
     * All keys and values until the balancing
     * <code>endObject</code> will be appended to this object. The
     * <code>endObject</code> method must be called to mark the object's end.<p>
     *
     * @return this
     * @throws JSONException if the nesting is too deep, or if the object is
     * started in the wrong place (for example as a key or after the end of the
     * outermost array or object)
     */
    public JSONWriter object() throws JSONException {

        if (m_mode == 'i') {
            m_mode = 'o';
        }
        if ((m_mode == 'o') || (m_mode == 'a')) {
            append("{");
            push('k');
            m_comma = false;
            return this;
        }
        throw new JSONException("Misplaced object.");

    }

    /**
     * Append either the value <code>true</code> or the value
     * <code>false</code>.<p>
     *
     * @param b a boolean
     * @return this
     * @throws JSONException if something goes wrong
     */
    public JSONWriter value(boolean b) throws JSONException {

        return append(b ? "true" : "false");
    }

    /**
     * Append a double value.<p>
     *
     * @param d a double
     * @return this
     * @throws JSONException if the number is not finite
     */
    public JSONWriter value(double d) throws JSONException {

        return this.value(Double.valueOf(d));
    }

    /**
     * Append a long value.<p>
     *
     * @param l a long
     * @return this
     * @throws JSONException if something goes wrong
     */
    public JSONWriter value(long l) throws JSONException {

        return append(Long.toString(l));
    }

    /**
     * Append an object value.<p>
     *
     * @param o the object to append. It can be null, or a Boolean, Number,
     *   String, JSONObject, or JSONArray, or an object with a toJSONString()
     *   method
     * @return this
     * @throws JSONException if the value is out of sequence
     */
    public JSONWriter value(Object o) throws JSONException {

        return append(JSONObject.valueToString(o));
    }

    /**
     * Append a value.<p>
     *
     * @param s a string value
     * @return this
     * @throws JSONException if the value is out of sequence
     */
    private JSONWriter append(String s) throws JSONException {

        if (s == null) {
            throw new JSONException("Null pointer");
        }
        if ((m_mode == 'o') || (m_mode == 'a')) {
            try {
                if (m_comma && (m_mode == 'a')) {
                    m_writer.write(',');
                }
                m_writer.write(s);
            } catch (IOException e) {
                throw new JSONException(e);
            }
            if (m_mode == 'o') {
                m_mode = 'k';
            }
            m_comma = true;
            return this;
        }
        throw new JSONException("Value out of sequence.");
    }

    /**
     * End something.<p>
     *
     * @param m mode
     * @param c closing character
     * @return this
     * @throws JSONException if unbalanced
     */
    private JSONWriter end(char m, char c) throws JSONException {

        if (m_mode != m) {
            throw new JSONException(m == 'o' ? "Misplaced endObject." : "Misplaced endArray.");
        }
        pop(m);
        try {
            m_writer.write(c);
        } catch (IOException e) {
            throw new JSONException(e);
        }
        m_comma = true;
        return this;
    }

    /**
     * Pop an array or object scope.<p>
     *
     * @param c the scope to close
     * @throws JSONException zf nesting is wrong
     */
    private void pop(char c) throws JSONException {

        if ((m_top <= 0) || (m_stack[m_top - 1] != c)) {
            throw new JSONException("Nesting error.");
        }
        m_top -= 1;
        m_mode = m_top == 0 ? 'd' : m_stack[m_top - 1];
    }

    /**
     * Push an array or object scope.<p>
     *
     * @param c the scope to open
     * @throws JSONException if nesting is too deep
     */
    private void push(char c) throws JSONException {

        if (m_top >= MAXDEPTH) {
            throw new JSONException("Nesting too deep.");
        }
        m_stack[m_top] = c;
        m_mode = c;
        m_top += 1;
    }
}

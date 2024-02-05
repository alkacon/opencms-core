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
 * org.json.JSONTokener
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it.<p>
 *
 * It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.<p>
 *
 */
public class JSONTokener {

    /** The index. */
    private int m_index;

    /** The last character. */
    private char m_lastChar;

    /** Flag which controls whether JSONObjects created by this tokener are ordered. */
    private boolean m_ordered;

    /** The reader. */
    private Reader m_reader;

    /** Flag indicating if the last character should be used. */
    private boolean m_useLastChar;

    /**
     * Construct a JSONTokener from a string.<p>
     *
     * @param reader     a reader.
     */
    public JSONTokener(Reader reader) {

        m_reader = reader.markSupported() ? reader : new BufferedReader(reader);
        m_useLastChar = false;
        m_index = 0;
    }

    /**
     * Construct a JSONTokener from a string.<p>
     *
     * @param s a source string
     */
    public JSONTokener(String s) {

        this(new StringReader(s));
    }

    /**
     * Get the hex value of a character (base16).<p>
     *
     * @param c a character between '0' and '9' or between 'A' and 'F' or
     * between 'a' and 'f'
     * @return  an int between 0 and 15, or -1 if c was not a hex digit
     */
    public static int dehexchar(char c) {

        if ((c >= '0') && (c <= '9')) {
            return c - '0';
        }
        if ((c >= 'A') && (c <= 'F')) {
            return c - ('A' - 10);
        }
        if ((c >= 'a') && (c <= 'f')) {
            return c - ('a' - 10);
        }
        return -1;
    }

    /**
     * Back up one character.<p>
     *
     * This provides a sort of lookahead capability,
     * so that you can test for a digit or letter before attempting to parse
     * the next number or identifier.<p>
     *
     * @throws JSONException if something goes wrong
     */
    public void back() throws JSONException {

        if (m_useLastChar || (m_index <= 0)) {
            throw new JSONException("Stepping back two steps is not supported");
        }
        m_index -= 1;
        m_useLastChar = true;
    }

    /**
     * Determine if the source string still contains characters that next()
     * can consume.<p>
     *
     * @return true if not yet at the end of the source
     * @throws JSONException if something goes wrong
     */
    public boolean more() throws JSONException {

        char nextChar = next();
        if (nextChar == 0) {
            return false;
        }
        back();
        return true;
    }

    /**
     * Get the next character in the source string.<p>
     *
     * @return the next character, or 0 if past the end of the source string
     * @throws JSONException if something goes wrong
     */
    public char next() throws JSONException {

        if (m_useLastChar) {
            m_useLastChar = false;
            if (m_lastChar != 0) {
                m_index += 1;
            }
            return m_lastChar;
        }
        int c;
        try {
            c = m_reader.read();
        } catch (IOException exc) {
            throw new JSONException(exc);
        }

        if (c <= 0) { // End of stream
            m_lastChar = 0;
            return 0;
        }
        m_index += 1;
        m_lastChar = (char)c;
        return m_lastChar;
    }

    /**
     * Consume the next character, and check that it matches a specified
     * character.<p>
     *
     * @param c the character to match
     * @return the character
     * @throws JSONException if the character does not match
     */
    public char next(char c) throws JSONException {

        char n = next();
        if (n != c) {
            throw syntaxError("Expected '" + c + "' and instead saw '" + n + "'");
        }
        return n;
    }

    /**
     * Get the next n characters.<p>
     *
     * @param n the number of characters to take
     * @return a string of n characters
     *
     * @throws JSONException substring bounds error if there are not n characters remaining in the source string
     */
    public String next(int n) throws JSONException {

        if (n == 0) {
            return "";
        }

        char[] buffer = new char[n];
        int pos = 0;

        if (m_useLastChar) {
            m_useLastChar = false;
            buffer[0] = m_lastChar;
            pos = 1;
        }

        try {
            int len = m_reader.read(buffer, pos, n - pos);
            while ((pos < n) && (len != -1)) {
                pos += len;
                len = m_reader.read(buffer, pos, n - pos);
            }
        } catch (IOException exc) {
            throw new JSONException(exc);
        }
        m_index += pos;

        if (pos < n) {
            throw syntaxError("Substring bounds error");
        }

        m_lastChar = buffer[n - 1];
        return new String(buffer);
    }

    /**
     * Get the next char in the string, skipping whitespace
     * and comments (slashslash, slashstar, and hash).<p>
     *
     * @return  a character, or 0 if there are no more characters
     * @throws JSONException if something goes wrong
     */
    public char nextClean() throws JSONException {

        for (;;) {
            char c = next();
            if (c == '/') {
                switch (next()) {
                    case '/':
                        do {
                            c = next();
                        } while ((c != '\n') && (c != '\r') && (c != 0));
                        break;
                    case '*':
                        for (;;) {
                            c = next();
                            if (c == 0) {
                                throw syntaxError("Unclosed comment");
                            }
                            if (c == '*') {
                                if (next() == '/') {
                                    break;
                                }
                                back();
                            }
                        }
                        break;
                    default:
                        back();
                        return '/';
                }
            } else if (c == '#') {
                do {
                    c = next();
                } while ((c != '\n') && (c != '\r') && (c != 0));
            } else if ((c == 0) || (c > ' ')) {
                return c;
            }
        }
    }

    /**
     * Return the characters up to the next close quote character.<p>
     *
     * Backslash processing is done. The formal JSON format does not
     * allow strings in single quotes, but an implementation is allowed to
     * accept them.<p>
     *
     * @param quote The quoting character, either
     *      <code>"</code>&nbsp;<small>(double quote)</small> or
     *      <code>'</code>&nbsp;<small>(single quote)</small>
     * @return      a String
     * @throws JSONException in case of an unterminated string
     */
    public String nextString(char quote) throws JSONException {

        char c;
        StringBuffer sb = new StringBuffer();
        for (;;) {
            c = next();
            switch (c) {
                case 0:
                case '\n':
                case '\r':
                    throw syntaxError("Unterminated string");
                case '\\':
                    c = next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'u':
                            sb.append((char)Integer.parseInt(next(4), 16));
                            break;
                        case 'x':
                            sb.append((char)Integer.parseInt(next(2), 16));
                            break;
                        default:
                            sb.append(c);
                    }
                    break;
                default:
                    if (c == quote) {
                        return sb.toString();
                    }
                    sb.append(c);
            }
        }
    }

    /**
     * Get the text up but not including the specified character or the
     * end of line, whichever comes first.<p>
     *
     * @param  d a delimiter character
     * @return   a string
     * @throws JSONException if something goes wrong
     */
    public String nextTo(char d) throws JSONException {

        StringBuffer sb = new StringBuffer();
        for (;;) {
            char c = next();
            if ((c == d) || (c == 0) || (c == '\n') || (c == '\r')) {
                if (c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }

    /**
     * Get the text up but not including one of the specified delimiter
     * characters or the end of line, whichever comes first.<p>
     *
     * @param delimiters a set of delimiter characters
     * @return a string, trimmed
     * @throws JSONException if something goes wrong
     */
    public String nextTo(String delimiters) throws JSONException {

        char c;
        StringBuffer sb = new StringBuffer();
        for (;;) {
            c = next();
            if ((delimiters.indexOf(c) >= 0) || (c == 0) || (c == '\n') || (c == '\r')) {
                if (c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }

    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.<p>
     *
     * @return an object
     * @throws JSONException if something goes wrong
     */
    public Object nextValue() throws JSONException {

        char c = nextClean();
        String s;

        switch (c) {
            case '"':
            case '\'':
                return nextString(c);
            case '{':
                back();
                return new JSONObject(this, m_ordered);
            case '[':
            case '(':
                back();
                return new JSONArray(this);
            default:
        }

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        StringBuffer sb = new StringBuffer();
        char b = c;
        while ((c >= ' ') && (",:]}/\\\"[{;=#".indexOf(c) < 0)) {
            sb.append(c);
            c = next();
        }
        back();

        /*
         * If it is true, false, or null, return the proper value.
         */

        s = sb.toString().trim();
        if (s.equals("")) {
            throw syntaxError("Missing value");
        }
        if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (s.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (s.equalsIgnoreCase("null")) {
            return JSONObject.NULL;
        }

        /*
         * If it might be a number, try converting it. We support the 0- and 0x-
         * conventions. If a number cannot be produced, then the value will just
         * be a string. Note that the 0-, 0x-, plus, and implied string
         * conventions are non-standard. A JSON parser is free to accept
         * non-JSON forms as long as it accepts all correct JSON forms.
         */

        if (((b >= '0') && (b <= '9')) || (b == '.') || (b == '-') || (b == '+')) {
            if (b == '0') {
                if ((s.length() > 2) && ((s.charAt(1) == 'x') || (s.charAt(1) == 'X'))) {
                    try {
                        return Integer.valueOf(Integer.parseInt(s.substring(2), 16));
                    } catch (Exception e) {
                        /* Ignore the error */
                    }
                } else {
                    try {
                        return Integer.valueOf(Integer.parseInt(s, 8));
                    } catch (Exception e) {
                        /* Ignore the error */
                    }
                }
            }
            try {
                return Integer.valueOf(s);
            } catch (Exception e) {
                try {
                    return Long.valueOf(s);
                } catch (Exception f) {
                    try {
                        return Double.valueOf(s);
                    } catch (Exception g) {
                        return s;
                    }
                }
            }
        }
        return s;
    }

    /**
     * Sets a flag which makes JSONObjects created by this tokener ordered.<p>
     *
     * @param ordered true if JSONObjects created by this should be ordered
     */
    public void setOrdered(boolean ordered) {

        m_ordered = ordered;
    }

    /**
     * Skip characters until the next character is the requested character.
     * If the requested character is not found, no characters are skipped.<p>
     *
     * @param to a character to skip to
     * @return the requested character, or zero if the requested character
     * is not found
     * @throws JSONException if something goes wrong
     */
    public char skipTo(char to) throws JSONException {

        char c;
        try {
            int startIndex = m_index;
            m_reader.mark(Integer.MAX_VALUE);
            do {
                c = next();
                if (c == 0) {
                    m_reader.reset();
                    m_index = startIndex;
                    return c;
                }
            } while (c != to);
        } catch (IOException exc) {
            throw new JSONException(exc);
        }

        back();
        return c;
    }

    /**
     * Make a JSONException to signal a syntax error.<p>
     *
     * @param message the error message
     * @return  a JSONException object, suitable for throwing
     */
    public JSONException syntaxError(String message) {

        return new JSONException(message + toString());
    }

    /**
     * Make a printable string of this JSONTokener.<p>
     *
     * @return " at character [this.index]"
     */
    @Override
    public String toString() {

        return " at character " + m_index;
    }
}

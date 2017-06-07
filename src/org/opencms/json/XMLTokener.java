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
 * org.json.XMLTokener
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

/**
 * The XMLTokener extends the JSONTokener to provide additional methods
 * for the parsing of XML texts.<p>
 *
 */
public class XMLTokener extends JSONTokener {

    /** The table of ENTITY values. It initially contains Character values for
     * amp, apos, gt, lt, quot.
     */
    public static final java.util.HashMap<String, Character> ENTITY;

    /**
     * Construct an XMLTokener from a string.<p>
     *
     * @param s a source string
     */
    public XMLTokener(String s) {

        super(s);
    }

    static {
        ENTITY = new java.util.HashMap<String, Character>(8);
        ENTITY.put("amp", XML.AMP);
        ENTITY.put("apos", XML.APOS);
        ENTITY.put("gt", XML.GT);
        ENTITY.put("lt", XML.LT);
        ENTITY.put("quot", XML.QUOT);
    }

    /**
     * Get the text in the CDATA block.<p>
     *
     * @return the string up to the <code>]]&gt;</code>
     * @throws JSONException if the <code>]]&gt;</code> is not found
     */
    public String nextCDATA() throws JSONException {

        char c;
        int i;
        StringBuffer sb = new StringBuffer();
        for (;;) {
            c = next();
            if (c == 0) {
                throw syntaxError("Unclosed CDATA");
            }
            sb.append(c);
            i = sb.length() - 3;
            if ((i >= 0) && (sb.charAt(i) == ']') && (sb.charAt(i + 1) == ']') && (sb.charAt(i + 2) == '>')) {
                sb.setLength(i);
                return sb.toString();
            }
        }
    }

    /**
     * Get the next XML outer token, trimming whitespace.<p>
     *
     * There are two kinds of tokens: the '<' character which begins a markup tag, and the content
     * text between markup tags.<p>
     *
     * @return  a string, or a '<' Character, or null if there is no more source text
     * @throws JSONException if something goes wrong
     */
    public Object nextContent() throws JSONException {

        char c;
        StringBuffer sb;
        do {
            c = next();
        } while (Character.isWhitespace(c));
        if (c == 0) {
            return null;
        }
        if (c == '<') {
            return XML.LT;
        }
        sb = new StringBuffer();
        for (;;) {
            if ((c == '<') || (c == 0)) {
                back();
                return sb.toString().trim();
            }
            if (c == '&') {
                sb.append(nextEntity(c));
            } else {
                sb.append(c);
            }
            c = next();
        }
    }

    /**
     * Return the next ENTITY. These entities are translated to Characters:
     *     <code>&amp;  &apos;  &gt;  &lt;  &quot;</code>.<p>
     *
     * @param a an ampersand character
     * @return  a Character or an entity String if the entity is not recognized
     * @throws JSONException if missing ';' in XML entity
     */
    public Object nextEntity(char a) throws JSONException {

        StringBuffer sb = new StringBuffer();
        for (;;) {
            char c = next();
            if (Character.isLetterOrDigit(c) || (c == '#')) {
                sb.append(Character.toLowerCase(c));
            } else if (c == ';') {
                break;
            } else {
                throw syntaxError("Missing ';' in XML ENTITY: &" + sb);
            }
        }
        String s = sb.toString();
        Object e = ENTITY.get(s);
        return e != null ? e : a + s + ";";
    }

    /**
     * Returns the next XML meta token. This is used for skipping over <!...>
     * and <?...?> structures.<p>
     *
     * @return syntax characters (<code>< > / = ! ?</code>) are returned as
     *  Character, and strings and names are returned as Boolean. We don't care
     *  what the values actually are
     * @throws JSONException if a string is not properly closed or if the XML
     *  is badly structured
     */
    public Object nextMeta() throws JSONException {

        char c;
        char q;
        do {
            c = next();
        } while (Character.isWhitespace(c));
        switch (c) {
            case 0:
                throw syntaxError("Misshaped meta tag");
            case '<':
                return XML.LT;
            case '>':
                return XML.GT;
            case '/':
                return XML.SLASH;
            case '=':
                return XML.EQ;
            case '!':
                return XML.BANG;
            case '?':
                return XML.QUEST;
            case '"':
            case '\'':
                q = c;
                for (;;) {
                    c = next();
                    if (c == 0) {
                        throw syntaxError("Unterminated string");
                    }
                    if (c == q) {
                        return Boolean.TRUE;
                    }
                }
            default:
                for (;;) {
                    c = next();
                    if (Character.isWhitespace(c)) {
                        return Boolean.TRUE;
                    }
                    switch (c) {
                        case 0:
                        case '<':
                        case '>':
                        case '/':
                        case '=':
                        case '!':
                        case '?':
                        case '"':
                        case '\'':
                            back();
                            return Boolean.TRUE;
                        default:
                    }
                }
        }
    }

    /**
     * Get the next XML Token.<p>
     *
     * These tokens are found inside of angle
     * brackets. It may be one of these characters: <code>/ > = ! ?</code> or it
     * may be a string wrapped in single quotes or double quotes, or it may be a
     * name.<p>
     *
     * @return a string or a Character
     * @throws JSONException if the XML is not well formed
     */
    public Object nextToken() throws JSONException {

        char c;
        char q;
        StringBuffer sb;
        do {
            c = next();
        } while (Character.isWhitespace(c));
        switch (c) {
            case 0:
                throw syntaxError("Misshaped element");
            case '<':
                throw syntaxError("Misplaced '<'");
            case '>':
                return XML.GT;
            case '/':
                return XML.SLASH;
            case '=':
                return XML.EQ;
            case '!':
                return XML.BANG;
            case '?':
                return XML.QUEST;

            // Quoted string

            case '"':
            case '\'':
                q = c;
                sb = new StringBuffer();
                for (;;) {
                    c = next();
                    if (c == 0) {
                        throw syntaxError("Unterminated string");
                    }
                    if (c == q) {
                        return sb.toString();
                    }
                    if (c == '&') {
                        sb.append(nextEntity(c));
                    } else {
                        sb.append(c);
                    }
                }
            default:

                // Name

                sb = new StringBuffer();
                for (;;) {
                    sb.append(c);
                    c = next();
                    if (Character.isWhitespace(c)) {
                        return sb.toString();
                    }
                    switch (c) {
                        case 0:
                            return sb.toString();
                        case '>':
                        case '/':
                        case '=':
                        case '!':
                        case '?':
                        case '[':
                        case ']':
                            back();
                            return sb.toString();
                        case '<':
                        case '"':
                        case '\'':
                            throw syntaxError("Bad character in a name");
                        default:
                    }
                }
        }
    }

    /**
     * Skip characters until past the requested string.<p>
     *
     * If it is not found, we are left at the end of the source with a result of false.<p>
     *
     * @param to a string to skip past
     * @return the truth
     * @throws JSONException if something goes wrong
     */
    public boolean skipPast(String to) throws JSONException {

        boolean b;
        char c;
        int i;
        int j;
        int offset = 0;
        int n = to.length();
        char[] circle = new char[n];

        /*
         * First fill the circle buffer with as many characters as are in the
         * to string. If we reach an early end, bail.
         */

        for (i = 0; i < n; i += 1) {
            c = next();
            if (c == 0) {
                return false;
            }
            circle[i] = c;
        }
        /*
         * We will loop, possibly for all of the remaining characters.
         */
        for (;;) {
            j = offset;
            b = true;
            /*
             * Compare the circle buffer with the to string.
             */
            for (i = 0; i < n; i += 1) {
                if (circle[j] != to.charAt(i)) {
                    b = false;
                    break;
                }
                j += 1;
                if (j >= n) {
                    j -= n;
                }
            }
            /*
             * If we exit the loop with b intact, then victory is ours.
             */
            if (b) {
                return true;
            }
            /*
             * Get the next character. If there isn't one, then defeat is ours.
             */
            c = next();
            if (c == 0) {
                return false;
            }
            /*
             * Shove the character in the circle buffer and advance the
             * circle offset. The offset is mod n.
             */
            circle[offset] = c;
            offset += 1;
            if (offset >= n) {
                offset -= n;
            }
        }
    }
}

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
 * org.json.XML
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

import java.util.Iterator;

/**
 * This provides static methods to convert an XML text into a JSONObject,
 * and to covert a JSONObject into an XML text.<p>
 *
 */
public final class XML {

    /** The Character '&'. */
    public static final Character AMP = Character.valueOf('&');

    /** The Character '''. */
    public static final Character APOS = Character.valueOf('\'');

    /** The Character '!'. */
    public static final Character BANG = Character.valueOf('!');

    /** The Character '='. */
    public static final Character EQ = Character.valueOf('=');

    /** The Character '>'. */
    public static final Character GT = Character.valueOf('>');

    /** The Character '<'. */
    public static final Character LT = Character.valueOf('<');

    /** The Character '?'. */
    public static final Character QUEST = Character.valueOf('?');

    /** The Character '"'. */
    public static final Character QUOT = Character.valueOf('"');

    /** The Character '/'. */
    public static final Character SLASH = Character.valueOf('/');

    /**
     * Hidden constructor.<p>
     */
    private XML() {

        // hide constructor
    }

    /**
     * Replace special characters with XML escapes:
     * <pre>
     * &amp; <small>(ampersand)</small> is replaced by &amp;amp;
     * &lt; <small>(less than)</small> is replaced by &amp;lt;
     * &gt; <small>(greater than)</small> is replaced by &amp;gt;
     * &quot; <small>(double quote)</small> is replaced by &amp;quot;
     * </pre>.<p>
     *
     * @param string the string to be escaped
     * @return the escaped string
     */
    public static String escape(String string) {

        StringBuffer sb = new StringBuffer();
        for (int i = 0, len = string.length(); i < len; i++) {
            char c = string.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONObject.<p>
     *
     * Some information may be lost in this transformation
     * because JSON is a data format and XML is a document format. XML uses
     * elements, attributes, and content text, while JSON uses unordered
     * collections of name/value pairs and arrays of values. JSON does not
     * does not like to distinguish between elements and attributes.<p>
     *
     * Sequences of similar elements are represented as JSONArrays. Content
     * text may be placed in a "content" member. Comments, prologs, DTDs, and
     * <code>&lt;[ [ ]]></code> are ignored.<p>
     *
     * @param string the source string
     * @return a JSONObject containing the structured data from the XML string
     * @throws JSONException if something goes wrong
     */
    public static JSONObject toJSONObject(String string) throws JSONException {

        JSONObject o = new JSONObject();
        XMLTokener x = new XMLTokener(string);
        while (x.more() && x.skipPast("<")) {
            parse(x, o, null);
        }
        return o;
    }

    /**
     * Convert a JSONObject into a well-formed, element-normal XML string.<p>
     *
     * @param o a JSONObject
     * @return  a string
     * @throws  JSONException if something goes wrong
     */
    public static String toString(Object o) throws JSONException {

        return toString(o, null);
    }

    /**
     * Convert a JSONObject into a well-formed, element-normal XML string.<p>
     *
     * @param o a JSONObject
     * @param tagName the optional name of the enclosing tag
     * @return a string
     * @throws JSONException if something goes wrong
     */
    public static String toString(Object o, String tagName) throws JSONException {

        StringBuffer b = new StringBuffer();
        int i;
        JSONArray ja;
        JSONObject jo;
        String k;
        Iterator<String> keys;
        int len;
        String s;
        Object v;
        if (o instanceof JSONObject) {

            // Emit <tagName>

            if (tagName != null) {
                b.append('<');
                b.append(tagName);
                b.append('>');
            }

            // Loop thru the keys.

            jo = (JSONObject)o;
            keys = jo.keys();
            while (keys.hasNext()) {
                k = keys.next();
                v = jo.get(k);
                if (v instanceof String) {
                    s = (String)v;
                } else {
                    s = null;
                }

                // Emit content in body

                if (k.equals("content")) {
                    if (v instanceof JSONArray) {
                        ja = (JSONArray)v;
                        len = ja.length();
                        for (i = 0; i < len; i += 1) {
                            if (i > 0) {
                                b.append('\n');
                            }
                            b.append(escape(ja.get(i).toString()));
                        }
                    } else {
                        b.append(escape(v.toString()));
                    }

                    // Emit an array of similar keys

                } else if (v instanceof JSONArray) {
                    ja = (JSONArray)v;
                    len = ja.length();
                    for (i = 0; i < len; i += 1) {
                        b.append(toString(ja.get(i), k));
                    }
                } else if (v.equals("")) {
                    b.append('<');
                    b.append(k);
                    b.append("/>");

                    // Emit a new tag <k>

                } else {
                    b.append(toString(v, k));
                }
            }
            if (tagName != null) {

                // Emit the </tagname> close tag

                b.append("</");
                b.append(tagName);
                b.append('>');
            }
            return b.toString();

            // XML does not have good support for arrays. If an array appears in a place
            // where XML is lacking, synthesize an <array> element.

        } else if (o instanceof JSONArray) {
            ja = (JSONArray)o;
            len = ja.length();
            for (i = 0; i < len; ++i) {
                b.append(toString(ja.opt(i), (tagName == null) ? "array" : tagName));
            }
            return b.toString();
        } else {
            s = (o == null) ? "null" : escape(o.toString());
            return (tagName == null)
            ? "\"" + s + "\""
            : (s.length() == 0) ? "<" + tagName + "/>" : "<" + tagName + ">" + s + "</" + tagName + ">";
        }
    }

    /**
     * Scan the content following the named tag, attaching it to the context.<p>
     *
     * @param x       the XMLTokener containing the source string
     * @param context the JSONObject that will include the new material
     * @param name    the tag name
     * @return true if the close tag is processed
     * @throws JSONException if something goes wrong
     */
    private static boolean parse(XMLTokener x, JSONObject context, String name) throws JSONException {

        char c;
        int i;
        String n;
        JSONObject o = null;
        String s;
        Object t;

        // Test for and skip past these forms:
        //      <!-- ... -->
        //      <!   ...   >
        //      <![  ... ]]>
        //      <?   ...  ?>
        // Report errors for these forms:
        //      <>
        //      <=
        //      <<

        t = x.nextToken();

        // <!

        if (t == BANG) {
            c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }
                x.back();
            } else if (c == '[') {
                t = x.nextToken();
                if (t.equals("CDATA")) {
                    if (x.next() == '[') {
                        s = x.nextCDATA();
                        if (s.length() > 0) {
                            context.accumulate("content", s);
                        }
                        return false;
                    }
                }
                throw x.syntaxError("Expected 'CDATA['");
            }
            i = 1;
            do {
                t = x.nextMeta();
                if (t == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                } else if (t == LT) {
                    i += 1;
                } else if (t == GT) {
                    i -= 1;
                }
            } while (i > 0);
            return false;
        } else if (t == QUEST) {

            // <?

            x.skipPast("?>");
            return false;
        } else if (t == SLASH) {

            // Close tag </

            t = x.nextToken();
            if (name == null) {
                throw x.syntaxError("Mismatched close tag" + t);
            }
            if (!t.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + t);
            }
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped close tag");
            }
            return true;

        } else if (t instanceof Character) {
            throw x.syntaxError("Misshaped tag");

            // Open tag <

        } else {
            n = (String)t;
            t = null;
            o = new JSONObject();
            for (;;) {
                if (t == null) {
                    t = x.nextToken();
                }

                // attribute = value

                if (t instanceof String) {
                    s = (String)t;
                    t = x.nextToken();
                    if (t == EQ) {
                        t = x.nextToken();
                        if (!(t instanceof String)) {
                            throw x.syntaxError("Missing value");
                        }
                        o.accumulate(s, t);
                        t = null;
                    } else {
                        o.accumulate(s, "");
                    }

                    // Empty tag <.../>

                } else if (t == SLASH) {
                    if (x.nextToken() != GT) {
                        throw x.syntaxError("Misshaped tag");
                    }
                    context.accumulate(n, o);
                    return false;

                    // Content, between <...> and </...>

                } else if (t == GT) {
                    for (;;) {
                        t = x.nextContent();
                        if (t == null) {
                            if (n != null) {
                                throw x.syntaxError("Unclosed tag " + n);
                            }
                            return false;
                        } else if (t instanceof String) {
                            s = (String)t;
                            if (s.length() > 0) {
                                o.accumulate("content", s);
                            }

                            // Nested element

                        } else if (t == LT) {
                            if (parse(x, o, n)) {
                                if (o.length() == 0) {
                                    context.accumulate(n, "");
                                } else if ((o.length() == 1) && (o.opt("content") != null)) {
                                    context.accumulate(n, o.opt("content"));
                                } else {
                                    context.accumulate(n, o);
                                }
                                return false;
                            }
                        }
                    }
                } else {
                    throw x.syntaxError("Misshaped tag");
                }
            }
        }
    }
}

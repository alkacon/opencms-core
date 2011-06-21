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
 */

package org.opencms.util.ant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides String utility functions.<p>
 * 
 * @since 6.0.0 
 */
public final class CmsStringUtil {

    /**
     * Hidden constructor.<p>
     */
    private CmsStringUtil() {

        // empty
    }

    /**
     * Returns <code>true</code> if the provided String is either <code>null</code>
     * or the empty String <code>""</code>.<p> 
     * 
     * @param value the value to check
     * 
     * @return true, if the provided value is null or the empty String, false otherwise
     */
    public static boolean isEmpty(String value) {

        return (value == null) || (value.length() == 0);
    }

    /**
     * Returns <code>true</code> if the provided String is either <code>null</code>
     * or contains only white spaces.<p> 
     * 
     * @param value the value to check
     * 
     * @return true, if the provided value is null or contains only white spaces, false otherwise
     */
    public static boolean isEmptyOrWhitespaceOnly(String value) {

        return isEmpty(value) || (value.trim().length() == 0);
    }

    /**
     * Splits a String into substrings along the provided char delimiter and returns
     * the result as a List of Substrings.<p>
     *
     * @param source the String to split
     * @param delimiter the delimiter to split at
     *
     * @return the List of splitted Substrings
     */
    public static List<String> splitAsList(String source, char delimiter) {

        return splitAsList(source, delimiter, false);
    }

    /**
     * Splits a String into substrings along the provided char delimiter and returns
     * the result as a List of Substrings.<p>
     *
     * @param source the String to split
     * @param delimiter the delimiter to split at
     * @param trim flag to indicate if leading and trailing white spaces should be omitted
     *
     * @return the List of splitted Substrings
     */
    public static List<String> splitAsList(String source, char delimiter, boolean trim) {

        List<String> result = new ArrayList<String>();
        int i = 0;
        int l = source.length();
        int n = source.indexOf(delimiter);
        while (n != -1) {
            // zero - length items are not seen as tokens at start or end
            if ((i < n) || ((i > 0) && (i < l))) {
                result.add(trim ? source.substring(i, n).trim() : source.substring(i, n));
            }
            i = n + 1;
            n = source.indexOf(delimiter, i);
        }
        // is there a non - empty String to cut from the tail? 
        if (n < 0) {
            n = source.length();
        }
        if (i < n) {
            result.add(trim ? source.substring(i).trim() : source.substring(i));
        }
        return result;
    }

    /**
     * Splits a String into substrings along the provided String delimiter and returns
     * the result as List of Substrings.<p>
     *
     * @param source the String to split
     * @param delimiter the delimiter to split at
     *
     * @return the Array of splitted Substrings
     */
    public static List<String> splitAsList(String source, String delimiter) {

        return splitAsList(source, delimiter, false);
    }

    /**
     * Splits a String into substrings along the provided String delimiter and returns
     * the result as List of Substrings.<p>
     * 
     * @param source the String to split
     * @param delimiter the delimiter to split at
     * @param trim flag to indicate if leading and trailing white spaces should be omitted
     * 
     * @return the Array of splitted Substrings
     */
    public static List<String> splitAsList(String source, String delimiter, boolean trim) {

        int dl = delimiter.length();
        if (dl == 1) {
            // optimize for short strings
            return splitAsList(source, delimiter.charAt(0), trim);
        }

        List<String> result = new ArrayList<String>();
        int i = 0;
        int l = source.length();
        int n = source.indexOf(delimiter);
        while (n != -1) {
            // zero - length items are not seen as tokens at start or end:  ",," is one empty token but not three
            if ((i < n) || ((i > 0) && (i < l))) {
                result.add(trim ? source.substring(i, n).trim() : source.substring(i, n));
            }
            i = n + dl;
            n = source.indexOf(delimiter, i);
        }
        // is there a non - empty String to cut from the tail? 
        if (n < 0) {
            n = source.length();
        }
        if (i < n) {
            result.add(trim ? source.substring(i).trim() : source.substring(i));
        }
        return result;
    }

    /**
     * Splits a String into substrings along the provided <code>paramDelim</code> delimiter,
     * then each substring is treat as a key-value pair delimited by <code>keyValDelim</code>.<p>
     * 
     * @param source the string to split
     * @param paramDelim the string to delimit each key-value pair
     * @param keyValDelim the string to delimit key and value
     * 
     * @return a map of splitted key-value pairs
     */
    public static Map<String, String> splitAsMap(String source, String paramDelim, String keyValDelim) {

        int keyValLen = keyValDelim.length();
        Map<String, String> params = new HashMap<String, String>();
        Iterator<String> itParams = CmsStringUtil.splitAsList(source, paramDelim, true).iterator();
        while (itParams.hasNext()) {
            String param = itParams.next();
            int pos = param.indexOf(keyValDelim);
            String key = param;
            String value = "";
            if (pos > 0) {
                key = param.substring(0, pos);
                if (pos + keyValLen < param.length()) {
                    value = param.substring(pos + keyValLen);
                }
            }
            params.put(key, value);
        }
        return params;
    }
}
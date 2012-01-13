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

package org.opencms.gwt.client.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A utility class with functions for dealing with maps.<p>
 * 
 * @since 8.0.0
 */
public final class CmsCollectionUtil {

    /**
     * Hide default constructor.<p>
     */
    private CmsCollectionUtil() {

        // do nothing
    }

    /**
     * Returns the intersection of two sets without modifying the original sets.<p>
     * 
     * @param <A> the type of objects contained in the sets
     * 
     * @param first the first set
     * @param second the second set
     * 
     * @return the intersection of both sets
     */
    public static <A> Set<A> intersection(Set<A> first, Set<A> second) {

        HashSet<A> result = new HashSet<A>(first);
        result.retainAll(second);
        return result;
    }

    /**
     * Checks whether a collection is empty or null.<p>
     * 
     * @param collection a collection 
     * @return true if <code>collection</code> is <code>null</code> or empty.
     */
    public static boolean isEmptyOrNull(Collection<?> collection) {

        return (collection == null) || collection.isEmpty();
    }

    /**
     * Parses properties from a string and returns them in a map.<p>
     * 
     * @param text the text containing the properties 
     * @return the map with the parsed properties 
     */
    public static Map<String, String> parseProperties(String text) {

        String[] lines = text.split("\n");
        Map<String, String> result = new HashMap<String, String>();
        for (String line : lines) {
            line = line.replaceFirst("^ +", "");
            line = line.replaceAll("\r", "");
            if (line.startsWith("#")) {
                continue;
            }
            int eqPos = line.indexOf('=');
            if (eqPos > 0) {
                String key = line.substring(0, eqPos);
                String value = line.substring(eqPos + 1);
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * Returns a new map with all entries of the input map except those which have a value of null.<p>
     * 
     * @param <A> the key type of the map
     * @param <B> the value type of the map 
     * @param map the input map
     *  
     * @return a map with all null entries removed 
     */
    public static <A, B> Map<A, B> removeNullEntries(Map<A, B> map) {

        HashMap<A, B> result = new HashMap<A, B>();

        for (Map.Entry<A, B> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Copies entries from one map to another and deletes those entries in the target map for which
     * the value in the source map is null.<p>
     * 
     * @param <A> the key type of the map 
     * @param <B> the value type of the map 
     * @param source the source map
     * @param target the target map 
     */
    public static <A, B> void updateMapAndRemoveNulls(Map<A, B> source, Map<A, B> target) {

        assert source != target;
        for (Map.Entry<A, B> entry : source.entrySet()) {
            A key = entry.getKey();
            B value = entry.getValue();
            if (value != null) {
                target.put(key, value);
            } else {
                target.remove(key);
            }
        }
    }
}

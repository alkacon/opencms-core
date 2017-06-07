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

package org.opencms.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

/**
 * Generic pair class.<p>
 *
 * @param <A> type of the first component of the pair
 * @param <B> type of the second component of the pair
 *
 * @since 8.0.0
 */
public class CmsPair<A, B> implements Serializable {

    /** Serial version id. */
    private static final long serialVersionUID = 1185676847810113664L;

    /** First component of the pair. */
    private A m_first;

    /** Second component of the pair. */
    private B m_second;

    /**
     * Serialization constructor.<p>
     */
    public CmsPair() {

        // do nothing
    }

    /**
     * Creates a new pair containing two objects.<p>
     *
     * @param a the first component
     * @param b the second component
     */
    public CmsPair(A a, B b) {

        m_first = a;
        m_second = b;
    }

    /**
     * Helper method for constructing a pair which avoids having to explicitly write the generic type parameters.<p>
     *
     * @param <A> the type for the first component
     * @param <B> the type for the second component
     *
     * @param a the first component
     * @param b the second component
     *
     * @return the pair (a, b)
     */
    public static <A, B> CmsPair<A, B> create(A a, B b) {

        return new CmsPair<A, B>(a, b);
    }

    /**
     * Utility method which creates a new comparator for lexically ordering pairs.<p>
     *
     * Lexical ordering means that a pair is considered "less" than another if either its
     * first component is less than that of the other one, or their first components are equal
     * and the second component of the first pair is less than that of the other one.<p>
     *
     * @param <A> the type parameter for the first pair component
     * @param <B> the type parameter for the second pair component
     *
     * @return a new comparator for lexically ordering pairs
     */
    public static <A extends Comparable<A>, B extends Comparable<B>> Comparator<CmsPair<A, B>> getLexicalComparator() {

        return new Comparator<CmsPair<A, B>>() {

            /**
             * @see java.util.Comparator#compare(Object,Object)
             */
            public int compare(CmsPair<A, B> pair1, CmsPair<A, B> pair2) {

                int c = pair1.getFirst().compareTo(pair2.getFirst());
                if (c != 0) {
                    return c;
                }
                return pair1.getSecond().compareTo(pair2.getSecond());
            }
        };
    }

    /**
     * Helper method for converting a list of string pairs to a string map.<p>
     *
     * The first component of each pair is used as a map key, the second component as the
     * value for the key.
     *
     * @param pairs the list of pairs
     *
     * @return a string map
     */
    public static Map<String, String> pairsToMap(List<CmsPair<String, String>> pairs) {

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        for (CmsPair<String, String> pair : pairs) {
            result.put(pair.getFirst(), pair.getSecond());
        }
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object o) {

        if ((o == null) || !(o instanceof CmsPair)) {
            return false;
        }
        CmsPair otherPair = (CmsPair)o;
        return getFirst().equals(otherPair.getFirst()) && getSecond().equals(otherPair.getSecond());
    }

    /**
     * Returns the first component of the pair.<p>
     *
     * @return the first component of the pair
     */
    public A getFirst() {

        return m_first;
    }

    /**
     * Returns the second component of the pair.<p>
     *
     * @return the second component of the pair
     */
    public B getSecond() {

        return m_second;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(getFirst(), getSecond());
    }

}

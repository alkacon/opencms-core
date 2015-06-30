/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.cmis;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class to ease implementation of CMIS service methods which support paging.<p>
 *
 * This class works as an iterator for a given list, and limits the number of iterations based on skip/max parameters
 * which are usually passed to the service methods.<p>
 *
 * @param <A> the content type of the list
 */
public class CmsObjectListLimiter<A> implements Iterable<A>, Iterator<A> {

    /** The list for which this object acts as an iterator. */
    private List<A> m_baseList;

    /** The maximum number of objects which can still be returned. */
    private int m_max;

    /** The index of the next object to return. */
    private int m_next;

    /**
     * Creates a new instance.<p>
     *
     * @param baseList the list over which we want to iterate
     * @param maxItems the maximum number of items
     * @param skipCount the number of items to skip
     */
    public CmsObjectListLimiter(List<A> baseList, BigInteger maxItems, BigInteger skipCount) {

        // skip and max
        m_baseList = baseList;
        m_next = (skipCount == null ? 0 : skipCount.intValue());
        if (m_next < 0) {
            m_next = 0;
        }

        m_max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        if (m_max < 0) {
            m_max = Integer.MAX_VALUE;
        }

    }

    /**
     * Checks if there are more items left in the base list which were not returned.<p>
     *
     * @return true if there are more items left in the base list which were not returned
     */
    public boolean hasMore() {

        return m_next < m_baseList.size();
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {

        return (m_next < m_baseList.size()) && (m_max > 0);
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<A> iterator() {

        return this;
    }

    /**
     * @see java.util.Iterator#next()
     */
    public A next() {

        A result = m_baseList.get(m_next);
        m_next += 1;
        m_max -= 1;
        return result;
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {

        throw new UnsupportedOperationException();
    }
}

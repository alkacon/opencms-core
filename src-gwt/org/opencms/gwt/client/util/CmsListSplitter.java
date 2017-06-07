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

package org.opencms.gwt.client.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for partitioning a list of items (each of which has a given size) into one or more batches
 * of consecutive items such that each batch except the last one consists of just enough items to make the total
 * sum of item sizes in the batch greater or equal than a given parameter.<p>
 *
 * @param <T> the type of items in the list to be partitioned
 *
 * @since 8.0.0
 *
 */
public class CmsListSplitter<T extends I_CmsHasSize> {

    /** The minimum size of the batches (except for the last batch). */
    private int m_batchSize;

    /** The current position in the list of items. */
    private int m_itemIndex;

    /** The list of items to split. */
    private List<T> m_items;

    /**
     * Creates a new instance of a list splitter.<p>
     *
     * @param items the list of items to split
     * @param batchSize the minimum size of the batches (except for the last batch)
     */
    public CmsListSplitter(List<T> items, int batchSize) {

        m_batchSize = batchSize;
        m_items = items;
        m_itemIndex = 0;
    }

    /**
     * Gets the next batch of items.<p>
     *
     * This will fail if there are no more items.
     *
     * @return the next batch of items
     */
    public List<T> getMore() {

        assert m_itemIndex < m_items.size();
        List<T> result = new ArrayList<T>();
        int totalSize = 0;
        while ((m_itemIndex < m_items.size()) && (totalSize < m_batchSize)) {
            T item = m_items.get(m_itemIndex);
            result.add(item);
            m_itemIndex += 1;
            totalSize += item.getSize();
        }
        return result;
    }

    /**
     * Returns true if there are more items left.<p>
     *
     * @return true if there are more items left
     */
    public boolean hasMore() {

        return m_itemIndex < m_items.size();
    }
}

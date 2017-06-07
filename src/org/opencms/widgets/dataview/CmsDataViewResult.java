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

package org.opencms.widgets.dataview;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a search query returned by an implementation of I_CmsDataView.
 *
 * This contains both a list of result items and a total hit count for the given query.
 */
public class CmsDataViewResult {

    /** The list of results. */
    private List<I_CmsDataViewItem> m_resultItems;

    /** The total hit count. */
    private int m_hitCount;

    /**
     * Creates a new result.<p>
     *
     * @param items the list of result items
     * @param hitCount the total hit count
     */
    public CmsDataViewResult(List<I_CmsDataViewItem> items, int hitCount) {
        m_resultItems = new ArrayList<I_CmsDataViewItem>(items);
        m_hitCount = hitCount;
    }

    /**
     * Gets the total number of results, disregarding paging.
     *
     * This count should be as accurate as possible.
     *
     * @return the total hit count
     */
    public int getHitCount() {

        return m_hitCount;
    }

    /**
     * Gets the list of result items.<p>
     *
     * @return the list of result items
     */
    public List<I_CmsDataViewItem> getItems() {

        return m_resultItems;
    }

}

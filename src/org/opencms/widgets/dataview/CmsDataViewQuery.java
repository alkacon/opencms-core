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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a search query which can be submitted to an implementation of I_CmsDataView to retrieve a set of results.<p>
 */
public class CmsDataViewQuery {

    /** The full text query. */
    private String m_fullTextQuery;

    /** The values of the selected filters. */
    private Map<String, String> m_filterValues;

    /** The sort direction (true for ascending). Only used when a sort column is selected. */
    private boolean m_sortAscending;

    /** The column to sort by. */
    private String m_sortColumn;

    /**
     * Gets the selected filter values.<p>
     *
     * @return the selected filter values
     */
    public Map<String, String> getFilterValues() {

        return m_filterValues;
    }

    /**
     * Gets the full text query.<p>
     *
     * If no full text query is provided, this will return the empty string.
     *
     * @return the full text query
     */
    public String getFullTextQuery() {

        return m_fullTextQuery;
    }

    /**
     * Gets the column to sort by.<p>
     *
     * If this method returns null, unsorted results should be returned.
     *
     * @return the column to sort by
     */
    public String getSortColumn() {

        return m_sortColumn;
    }

    /**
     * Returns true if the sorting for the given sort column should be in ascending order.<p>
     *
     * @return true if the results should be sorted in ascending order
     */
    public boolean isSortAscending() {

        return m_sortAscending;
    }

    /**
     * Sets the selected filter values.<p>
     *
     * @param filterValues the filter values
     */
    public void setFilterValues(LinkedHashMap<String, String> filterValues) {

        m_filterValues = new LinkedHashMap<String, String>(filterValues);
    }

    /**
     * Sets the full text query.<p>
     *
     * @param query the full text query
     */
    public void setFullTextQuery(String query) {

        if (query == null) {
            query = "";
        }
        m_fullTextQuery = query;

    }

    /**
     * Sets the sort direction.<p>
     *
     * @param ascending true if results should be sorted in ascending order
     */
    public void setSortAscending(boolean ascending) {

        m_sortAscending = ascending;

    }

    /**
     * Sets the sort column.<p>
     *
     * @param sortColumn the sort column
     */
    public void setSortColumn(String sortColumn) {

        m_sortColumn = sortColumn;

    }

}

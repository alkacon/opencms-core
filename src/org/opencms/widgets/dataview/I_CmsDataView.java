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

import org.opencms.file.CmsObject;

import java.util.List;
import java.util.Locale;

/**
 * Interface for a data source for use by CmsDataViewWidget.<p>
 *
 * This provides everything the widget needs to query an external data source, display the query
 * results (items) in tabular form, and provide the data to be stored when the user selects one or multiple items.
 */
public interface I_CmsDataView {

    /**
     * Retrieves the list of columns to be displayed in the external data view widget.<p>
     *
     * This method should always return the same columns.
     *
     * @return a list of column definitions
     */
    List<CmsDataViewColumn> getColumns();

    /**
     * Gets the initial list of filters.<p>
     *
     * This is only used for the initial display of the data table; if the user changes the filters,
     * the method updateFilters() is called instead.
     *
     * @return the initial list of filters
     */
    List<CmsDataViewFilter> getFilters();

    /**
     * Retrieves the data item with the given id.<p>
     *
     * If no data item with that id exists, this method should return null.<p>
     *
     * @param id the id for which to retrieve the item
     *
     * @return the data item with the given the
     */
    I_CmsDataViewItem getItemById(String id);

    /**
     * Gets the page size, used for paging results.<p>
     *
     * @return the page size
     */
    int getPageSize();

    /**
     * Gets the result for the given query, with the given offset and limited to the given number of results.<p>
     *
     * The returned result includes a list of result items, and a total hit count for the given query (i.e. the number of results
     * that would be returned for offset 0 and unlimited result count). The hit count should be as accurate as possible.
     *
     * @param query the query
     * @param offset position of the first result to return (starts with 0)
     * @param count maximum number of results to return
     *
     * @return the result object
     */
    CmsDataViewResult getResults(CmsDataViewQuery query, int offset, int count);

    /**
     * Initializes this data view instance.<p>
     *
     * @param cms the CMS context to use for VFS operations
     * @param configData a string containing configuration data
     * @param locale the locale to use for the user interface
     */
    void initialize(CmsObject cms, String configData, Locale locale);

    /**
     * Returns an updated list of filters based on the current filters and their values.<p>
     *
     * This is called when the user changes any of the current filters in the data select widget.
     * The list passed as a parameter should not be modified, instead a new filter list should be returned.
     * If you do not need a dynamically changing set of filters, just return the argument 'prevFilters'
     * in your implementation.
     *
     * @param prevFilters the current set of filters
     * @return the new list of filters
     */
    List<CmsDataViewFilter> updateFilters(List<CmsDataViewFilter> prevFilters);

}

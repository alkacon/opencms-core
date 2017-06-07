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

/**
 * Interface describing a data item retrieved by an implementation of I_CmsDataView.<p>
 *
 * This interface provides both the data to display the data item in the widget used to select data items,
 * as well as the data which should be stored by OpenCms when the data item is selected by the user (its ID, title,
 * description and an additional data string for other information)
 */
public interface I_CmsDataViewItem {

    /**
     * Gets the data for the column with the given name.<p>
     *
     * The returned object should be of a type compatible with the type of the column returned by I_CmsDataView.getColumns() with the same name.<p>
     *
     * @param colName the column name
     * @return the value of the column
     */
    Object getColumnData(String colName);

    /**
     * Returns the additional data which should be stored by OpenCms.
     *
     * @return the additional data to be stored
     */
    String getData();

    /**
     * Returns the description to be stored by OpenCms.<p>
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the ID of the data item.<p>
     *
     * The ID should be unique for the given I_CmsDataView implementation which returned this data item.<p>
     *
     * @return the ID of the data item
     */
    String getId();

    /**
     * Gets the URL of the image to be displayed for this data item.<p>
     *
     * @return an URL pointing to an image
     */
    String getImage();

    /**
     * Gets the title to be stored by OpenCms.<p>
     *
     * @return the title
     */
    String getTitle();

}
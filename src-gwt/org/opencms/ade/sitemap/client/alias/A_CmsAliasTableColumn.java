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

package org.opencms.ade.sitemap.client.alias;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;

/**
 * Abstract column class for the alias cell table columns.<p>
 *
 * @param <R> the row type
 * @param <V> the cell value type
 * @param <T> the celll table type
 */
public abstract class A_CmsAliasTableColumn<R, V, T extends CellTable<R>> extends Column<R, V> {

    /**
     * Delegates to the superclass constructor.<p>
     *
     * @param cell the cell for this column
     */
    public A_CmsAliasTableColumn(Cell<V> cell) {

        super(cell);
    }

    /**
     * Adds the column to the table.<p>
     *
     * Subclasses can override this to also set e.g. the column width
     * @param table
     */
    public abstract void addToTable(T table);

    /**
     * Initializes the sort handler to use this column.<p>
     *
     * @param sortHandler the sort handler
     */
    public void initSortHandler(ListHandler<R> sortHandler) {

        // do nothing by default
    }
}

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

package org.opencms.ade.sitemap.client.alias.rewrite;

import org.opencms.ade.sitemap.client.alias.A_CmsAliasTableColumn;
import org.opencms.ade.sitemap.client.alias.CmsAliasMessages;
import org.opencms.gwt.shared.alias.CmsRewriteAliasTableRow;

import java.util.Comparator;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;

/**
 * Column class for entering a rewrite alias pattern.<p>
 */
public class CmsRewriteAliasPatternColumn
extends A_CmsAliasTableColumn<CmsRewriteAliasTableRow, String, CmsRewriteAliasTable>
implements FieldUpdater<CmsRewriteAliasTableRow, String> {

    /** Comparator for sorting this column. */
    private static Comparator<CmsRewriteAliasTableRow> comparator = Ordering.natural().onResultOf(
        new Function<CmsRewriteAliasTableRow, String>() {

            public String apply(CmsRewriteAliasTableRow row) {

                return row.getPatternString();
            }
        });

    /** The cell table to which this column belongs. */
    private CmsRewriteAliasTable m_table;

    /**
     * Creates a new column instance.<p>
     *
     * @param table the table to which this column belongs
     */
    public CmsRewriteAliasPatternColumn(CmsRewriteAliasTable table) {

        super(new EditTextCell());
        m_table = table;
        setFieldUpdater(this);
        setSortable(true);

    }

    /**
     * @see org.opencms.ade.sitemap.client.alias.A_CmsAliasTableColumn#addToTable(com.google.gwt.user.cellview.client.CellTable)
     */
    @Override
    public void addToTable(CmsRewriteAliasTable table) {

        table.addColumn(this, CmsAliasMessages.messageColumnPattern());
        table.setColumnWidth(this, "300px");

    }

    /**
     * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
     */
    @Override
    public String getValue(CmsRewriteAliasTableRow row) {

        return row.getPatternString();
    }

    /**
     * @see org.opencms.ade.sitemap.client.alias.A_CmsAliasTableColumn#initSortHandler(com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler)
     */
    @Override
    public void initSortHandler(ListHandler<CmsRewriteAliasTableRow> sortHandler) {

        sortHandler.setComparator(this, comparator);
    }

    /**
     * @see com.google.gwt.cell.client.FieldUpdater#update(int, java.lang.Object, java.lang.Object)
     */
    public void update(int index, CmsRewriteAliasTableRow object, String value) {

        object.setPatternString(value);
        m_table.getController().editRewriteAlias(object);
    }

}

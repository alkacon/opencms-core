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

package org.opencms.ade.sitemap.client.alias.simple;

import org.opencms.ade.sitemap.client.alias.A_CmsAliasTableColumn;
import org.opencms.ade.sitemap.client.alias.CmsAliasMessages;
import org.opencms.gwt.client.ui.css.I_CmsCellTableResources;
import org.opencms.gwt.shared.alias.CmsAliasTableRow;

import java.util.Comparator;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;

/**
 * The table column for editing/displaying the alias path.<p>
 */
public class CmsAliasPathColumn extends A_CmsAliasTableColumn<CmsAliasTableRow, String, CmsAliasCellTable> {

    /** The cell in which this column is used. */
    CmsAliasCellTable m_table;

    /**
     * Creates a new column instance.<p>
     *
     * @param table the table in which this column is used
     */
    public CmsAliasPathColumn(CmsAliasCellTable table) {

        super(new EditTextCell());
        m_table = table;
        FieldUpdater<CmsAliasTableRow, String> updater = new FieldUpdater<CmsAliasTableRow, String>() {

            public void update(int index, CmsAliasTableRow object, String value) {

                m_table.getController().editAliasPath(object, value);
            }
        };
        setFieldUpdater(updater);
        setSortable(true);
    }

    /**
     * Gets the comparator used for this column.<p>
     *
     * @return the comparator to use for this row
     */
    public static Comparator<CmsAliasTableRow> getComparator() {

        return new Comparator<CmsAliasTableRow>() {

            public int compare(CmsAliasTableRow o1, CmsAliasTableRow o2) {

                return o1.getAliasPath().toString().compareTo(o2.getAliasPath().toString());
            }
        };
    }

    /**
     * @see org.opencms.ade.sitemap.client.alias.A_CmsAliasTableColumn#addToTable(com.google.gwt.user.cellview.client.CellTable)
     */
    @Override
    public void addToTable(CmsAliasCellTable table) {

        table.addColumn(this, CmsAliasMessages.messageColumnAlias());
        table.setColumnWidth(this, 300, Unit.PX);
    }

    /**
     * @see com.google.gwt.user.cellview.client.Column#getCellStyleNames(com.google.gwt.cell.client.Cell.Context, java.lang.Object)
     */
    @Override
    public String getCellStyleNames(com.google.gwt.cell.client.Cell.Context context, CmsAliasTableRow object) {

        if (object.getAliasError() != null) {
            return super.getCellStyleNames(context, object)
                + " "
                + I_CmsCellTableResources.INSTANCE.cellTableStyle().cmsCellError();
        } else {
            return super.getCellStyleNames(context, object);
        }
    }

    /**
     * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
     */
    @Override
    public String getValue(CmsAliasTableRow row) {

        return row.getAliasPath();
    }

    /**
     * @see org.opencms.ade.sitemap.client.alias.A_CmsAliasTableColumn#initSortHandler(com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler)
     */
    @Override
    public void initSortHandler(ListHandler<CmsAliasTableRow> sortHandler) {

        sortHandler.setComparator(this, getComparator());
    }
}

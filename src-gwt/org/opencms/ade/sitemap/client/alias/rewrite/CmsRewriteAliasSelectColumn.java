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
import org.opencms.gwt.shared.alias.CmsRewriteAliasTableRow;

import com.google.gwt.cell.client.CheckboxCell;

/**
 * Column used to select or deselect rows in the rewrite alias cell table.<p>
 */
public class CmsRewriteAliasSelectColumn
extends A_CmsAliasTableColumn<CmsRewriteAliasTableRow, Boolean, CmsRewriteAliasTable> {

    /** The table which this column belongs to. */
    private CmsRewriteAliasTable m_table;

    /**
     * Creates a new column instance.<p>
     *
     * @param table the table which this column belongs to
     */
    public CmsRewriteAliasSelectColumn(CmsRewriteAliasTable table) {

        super(new CheckboxCell());
        m_table = table;
    }

    /**
     * @see org.opencms.ade.sitemap.client.alias.A_CmsAliasTableColumn#addToTable(com.google.gwt.user.cellview.client.CellTable)
     */
    @Override
    public void addToTable(CmsRewriteAliasTable table) {

        table.addColumn(this, "X");
        table.setColumnWidth(this, "30px");
    }

    /**
     * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
     */
    @Override
    public Boolean getValue(CmsRewriteAliasTableRow row) {

        return Boolean.valueOf(m_table.getSelectionModel().isSelected(row));
    }

}

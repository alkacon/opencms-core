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
import org.opencms.ade.sitemap.client.alias.CmsCellTableUtil;
import org.opencms.gwt.client.ui.css.I_CmsCellTableResources;
import org.opencms.gwt.shared.alias.CmsRewriteAliasTableRow;

import java.util.Comparator;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;

/**
 * Column for displaying errors in the rewrite table.<p>
 */
public class CmsRewriteAliasErrorColumn
extends A_CmsAliasTableColumn<CmsRewriteAliasTableRow, SafeHtml, CmsRewriteAliasTable> {

    /** Comparator used for sorting this column. */
    private static Comparator<CmsRewriteAliasTableRow> comparator = Ordering.natural().onResultOf(
        new Function<CmsRewriteAliasTableRow, String>() {

            public String apply(CmsRewriteAliasTableRow row) {

                return row.getError() == null ? "" : row.getError();
            }
        });

    /**
     * Creates a new column instance.<p>
     */
    public CmsRewriteAliasErrorColumn() {

        super(new SafeHtmlCell());
        setSortable(true);
    }

    /**
     * @see org.opencms.ade.sitemap.client.alias.A_CmsAliasTableColumn#addToTable(com.google.gwt.user.cellview.client.CellTable)
     */
    @Override
    public void addToTable(CmsRewriteAliasTable table) {

        table.addColumn(this, CmsAliasMessages.messageColumnError());
    }

    /**
     * @see com.google.gwt.user.cellview.client.Column#getCellStyleNames(com.google.gwt.cell.client.Cell.Context, java.lang.Object)
     */
    @Override
    public String getCellStyleNames(com.google.gwt.cell.client.Cell.Context context, CmsRewriteAliasTableRow object) {

        String result = super.getCellStyleNames(context, object);
        if (object.getError() != null) {
            result += " " + I_CmsCellTableResources.INSTANCE.cellTableStyle().cmsCellError();
        }
        return result;
    }

    /**
     * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
     */
    @Override
    public SafeHtml getValue(CmsRewriteAliasTableRow object) {

        return CmsCellTableUtil.formatErrorHtml(object.getError());
    }

    /**
     * @see org.opencms.ade.sitemap.client.alias.A_CmsAliasTableColumn#initSortHandler(com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler)
     */
    @Override
    public void initSortHandler(ListHandler<CmsRewriteAliasTableRow> sortHandler) {

        sortHandler.setComparator(this, comparator);
    }

}

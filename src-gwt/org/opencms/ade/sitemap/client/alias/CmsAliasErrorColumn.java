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

import org.opencms.gwt.shared.alias.CmsAliasTableRow;

import java.util.Comparator;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;

/**
 * The class for the column of the alias editor table which is used to display validation errors.<p>
 */
public class CmsAliasErrorColumn extends Column<CmsAliasTableRow, String> {

    /**
     * Creates a new instance.<p>
     */
    public CmsAliasErrorColumn() {

        super(new TextCell());
        setSortable(true);
    }

    /**
     * Gets the comparator which should be used for this column.<p>
     * 
     * @return the comparator used for this column 
     */
    public static Comparator<CmsAliasTableRow> getComparator() {

        return new Comparator<CmsAliasTableRow>() {

            public int compare(CmsAliasTableRow o1, CmsAliasTableRow o2) {

                String err1 = getValueInternal(o1);
                String err2 = getValueInternal(o2);
                if ((err1 == null) && (err2 == null)) {
                    return 0;
                }
                if (err1 == null) {
                    return -1;
                }
                if (err2 == null) {
                    return 1;
                }
                return 0;
            }
        };
    }

    /**
     * Static helper method to get the value to display in the column from a row.<p>
     * 
     * @param row the row
     * 
     * @return the value to display
     */
    protected static String getValueInternal(CmsAliasTableRow row) {

        if (row.getAliasError() != null) {
            return row.getAliasError();
        }
        if (row.getPathError() != null) {
            return row.getPathError();
        }
        return null;
    }

    /**
     * @see com.google.gwt.user.cellview.client.Column#getCellStyleNames(com.google.gwt.cell.client.Cell.Context, java.lang.Object)
     */
    @Override
    public String getCellStyleNames(com.google.gwt.cell.client.Cell.Context context, CmsAliasTableRow object) {

        if ((object.getAliasError() != null) || (object.getPathError() != null)) {
            return super.getCellStyleNames(context, object) + " cmsCellError";
        } else {
            return super.getCellStyleNames(context, object);
        }
    }

    /**
     * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
     */
    @Override
    public String getValue(CmsAliasTableRow row) {

        return getValueInternal(row);
    }

}

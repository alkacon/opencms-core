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
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.gwt.shared.alias.CmsAliasTableRow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;

/**
 * The table column for displaying/editing the alias mode.<p>
 */
public class CmsAliasModeColumn extends A_CmsAliasTableColumn<CmsAliasTableRow, String, CmsAliasCellTable> {

    /** A map used to translate between the internal names and the user readable names of the selectable values. */
    private static BiMap<CmsAliasMode, String> nameLookup = HashBiMap.create();

    /** The table for which this column is used. */
    CmsAliasCellTable m_table;

    /**
     * Creates a new column instance.<p>
     *
     * @param table the table for which this column is used
     */
    public CmsAliasModeColumn(CmsAliasCellTable table) {

        super(new SelectionCell(createOptions()));
        m_table = table;
        setSortable(true);
        FieldUpdater<CmsAliasTableRow, String> updater = new FieldUpdater<CmsAliasTableRow, String>() {

            public void update(int index, CmsAliasTableRow object, String value) {

                m_table.getController().editAliasMode(object, getModeFromName(value));
            }
        };
        setFieldUpdater(updater);
    }

    static {
        nameLookup.put(CmsAliasMode.permanentRedirect, CmsAliasMessages.messagePermanentRedirect());
        nameLookup.put(CmsAliasMode.redirect, CmsAliasMessages.messageRedirect());
        nameLookup.put(CmsAliasMode.page, CmsAliasMessages.messagePage());
    }

    /**
     * Creates the options for the select box.<p>
     *
     * @return the options for the select box
     */
    public static List<String> createOptions() {

        List<String> result = new ArrayList<String>();
        result.add(nameLookup.get(CmsAliasMode.permanentRedirect));
        result.add(nameLookup.get(CmsAliasMode.redirect));
        result.add(nameLookup.get(CmsAliasMode.page));
        return result;
    }

    /**
     * Gets the comparator used for this column.<p>
     *
     * @return the comparator used for this column
     */
    public static Comparator<CmsAliasTableRow> getComparator() {

        return new Comparator<CmsAliasTableRow>() {

            public int compare(CmsAliasTableRow o1, CmsAliasTableRow o2) {

                return o1.getMode().toString().compareTo(o2.getMode().toString());
            }
        };
    }

    /**
     * Translates a user-readable mode name to the internal mode value.<p>
     *
     * @param name the user-readable mode name
     *
     * @return the internal mode value
     */
    static CmsAliasMode getModeFromName(String name) {

        return nameLookup.inverse().get(name);
    }

    /**
     * @see org.opencms.ade.sitemap.client.alias.A_CmsAliasTableColumn#addToTable(com.google.gwt.user.cellview.client.CellTable)
     */
    @Override
    public void addToTable(CmsAliasCellTable table) {

        table.addColumn(this, CmsAliasMessages.messageColumnMode());
        table.setColumnWidth(this, 220, Unit.PX);
    }

    /**
     * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
     */
    @Override
    public String getValue(CmsAliasTableRow object) {

        return nameLookup.get(object.getMode());
    }

    /**
     * @see org.opencms.ade.sitemap.client.alias.A_CmsAliasTableColumn#initSortHandler(com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler)
     */
    @Override
    public void initSortHandler(ListHandler<CmsAliasTableRow> sortHandler) {

        sortHandler.setComparator(this, getComparator());
    }

}

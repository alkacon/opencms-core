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

import org.opencms.gwt.client.ui.css.I_CmsCellTableResources;
import org.opencms.gwt.shared.alias.CmsAliasTableRow;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

/**
 * The cell table which is the main widget used for the bulk alias editor.<p>
 */
public class CmsAliasCellTable extends CellTable<CmsAliasTableRow> {

    /** The alias controller. */
    private CmsAliasTableController m_controller;

    /** The data provider. */
    private ListDataProvider<CmsAliasTableRow> m_dataProvider;

    /**
     * Creates a new cell table with the given controller.<p>
     * 
     * @param controller the alias editor controller which should be used by the cell table 
     */
    public CmsAliasCellTable(CmsAliasTableController controller) {

        super(
            Integer.MAX_VALUE,
            (CellTable.Resources)GWT.create(I_CmsCellTableResources.class),
            new ProvidesKey<CmsAliasTableRow>() {

                public Object getKey(CmsAliasTableRow item) {

                    return item.getKey();
                }

            });
        m_controller = controller;
        Column<CmsAliasTableRow, Boolean> selectCol = new CmsAliasSelectionColumn(this);
        Column<CmsAliasTableRow, String> aliasPathCol = new CmsAliasPathColumn(this);
        Column<CmsAliasTableRow, String> resourcePathCol = new CmsResourcePathColumn(this);
        Column<CmsAliasTableRow, String> modeCol = new CmsAliasModeColumn(this);
        Column<CmsAliasTableRow, String> errorCol = new CmsAliasErrorColumn();
        m_dataProvider = new ListDataProvider<CmsAliasTableRow>();
        m_dataProvider.addDataDisplay(this);
        ColumnSortEvent.ListHandler<CmsAliasTableRow> sortHandler = new ColumnSortEvent.ListHandler<CmsAliasTableRow>(
            m_dataProvider.getList());
        sortHandler.setComparator(aliasPathCol, CmsAliasPathColumn.getComparator());
        sortHandler.setComparator(resourcePathCol, CmsResourcePathColumn.getComparator());
        sortHandler.setComparator(modeCol, CmsAliasModeColumn.getComparator());
        sortHandler.setComparator(errorCol, CmsAliasErrorColumn.getComparator());
        addColumnSortHandler(sortHandler);

        MultiSelectionModel<CmsAliasTableRow> selectionModel = new MultiSelectionModel<CmsAliasTableRow>(
            getKeyProvider());
        setSelectionModel(selectionModel, DefaultSelectionEventManager.<CmsAliasTableRow> createCheckboxManager());
        addColumn(selectCol, CmsAliasMessages.messageColumnSelect());
        setColumnWidth(selectCol, 50, Unit.PX);
        addColumn(aliasPathCol, CmsAliasMessages.messageColumnAlias());
        setColumnWidth(aliasPathCol, 350, Unit.PX);
        addColumn(resourcePathCol, CmsAliasMessages.messageColumnPath());
        setColumnWidth(resourcePathCol, 350, Unit.PX);
        addColumn(modeCol, CmsAliasMessages.messageColumnMode());
        setColumnWidth(modeCol, 200, Unit.PX);
        addColumn(errorCol, CmsAliasMessages.messageColumnError());
        setColumnWidth(errorCol, 200, Unit.PX);
        setWidth("100%");
    }

    /**
     * Gets the alias editor controller used by this table.<p>
     * 
     * @return the alias editor controller 
     */
    public CmsAliasTableController getController() {

        return m_controller;
    }

    /**
     * Gets the list of rows internally used by the data provider for this table.<p>
     * 
     * @return the internal data list 
     */
    public List<CmsAliasTableRow> getLiveDataList() {

        return m_dataProvider.getList();
    }

    /**
     * Gets the list of selected rows.<p>
     * 
     * @return the list of selected rows 
     */
    public List<CmsAliasTableRow> getSelectedRows() {

        return new ArrayList<CmsAliasTableRow>(getSelectionModel().getSelectedSet());

    }

    /**
     * @see com.google.gwt.user.cellview.client.AbstractHasData#getSelectionModel()
     * 
     * (Overridden to use a less general return type)
     */
    @SuppressWarnings("unchecked")
    @Override
    public MultiSelectionModel<CmsAliasTableRow> getSelectionModel() {

        return (MultiSelectionModel<CmsAliasTableRow>)(super.getSelectionModel());
    }

}

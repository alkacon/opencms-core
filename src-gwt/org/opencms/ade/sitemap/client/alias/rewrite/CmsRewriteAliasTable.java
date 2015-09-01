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
import org.opencms.ade.sitemap.client.alias.CmsAliasTableController;
import org.opencms.ade.sitemap.client.alias.CmsCellTableUtil;
import org.opencms.gwt.client.ui.css.I_CmsCellTableResources;
import org.opencms.gwt.shared.alias.CmsRewriteAliasTableRow;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * Table class used for editing rewrite aliases.<p>
 */
public class CmsRewriteAliasTable extends CellTable<CmsRewriteAliasTableRow> {

    /** The controller class for the alias dialog. */
    private CmsAliasTableController m_controller;

    /** The list data provider for this table. */
    private ListDataProvider<CmsRewriteAliasTableRow> m_dataProvider;

    /**
     * Creates a new table instance.<p>
     *
     * @param controller the controller instance for the alias table view
     */
    public CmsRewriteAliasTable(CmsAliasTableController controller) {

        super(
            Integer.MAX_VALUE,
            (CellTable.Resources)GWT.create(I_CmsCellTableResources.class),
            new ProvidesKey<CmsRewriteAliasTableRow>() {

                public Object getKey(CmsRewriteAliasTableRow item) {

                    return item.getId();
                }

            });
        m_controller = controller;
        @SuppressWarnings("unchecked")
        A_CmsAliasTableColumn<CmsRewriteAliasTableRow, ?, CmsRewriteAliasTable>[] columns = new A_CmsAliasTableColumn[] {
            new CmsRewriteAliasSelectColumn(this),
            new CmsRewriteAliasPatternColumn(this),
            new CmsRewriteAliasReplacementColumn(this),
            new CmsRewriteAliasModeColumn(this),
            new CmsRewriteAliasErrorColumn()};
        m_dataProvider = new ListDataProvider<CmsRewriteAliasTableRow>();
        m_dataProvider.addDataDisplay(this);
        ColumnSortEvent.ListHandler<CmsRewriteAliasTableRow> sortHandler = new ColumnSortEvent.ListHandler<CmsRewriteAliasTableRow>(
            m_dataProvider.getList());
        for (A_CmsAliasTableColumn<CmsRewriteAliasTableRow, ?, CmsRewriteAliasTable> column : columns) {
            column.initSortHandler(sortHandler);
        }
        addColumnSortHandler(sortHandler);

        final MultiSelectionModel<CmsRewriteAliasTableRow> selectionModel = new MultiSelectionModel<CmsRewriteAliasTableRow>(
            getKeyProvider());
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            public void onSelectionChange(SelectionChangeEvent event) {

                getController().changeRewriteSelection(selectionModel.getSelectedSet());
            }
        });
        setSelectionModel(
            selectionModel,
            DefaultSelectionEventManager.<CmsRewriteAliasTableRow> createCheckboxManager());
        for (A_CmsAliasTableColumn<CmsRewriteAliasTableRow, ?, CmsRewriteAliasTable> column : columns) {
            column.addToTable(this);
        }
        CmsCellTableUtil.ensureCellTableParentResize(this);

    }

    /**
     * Gets the list of rows internally used by the data provider for this table.<p>
     *
     * @return the internal data list
     */
    public List<CmsRewriteAliasTableRow> getLiveDataList() {

        return m_dataProvider.getList();
    }

    /**
     * Gets the list of selected rows.<p>
     *
     * @return the list of selected rows
     */
    public List<CmsRewriteAliasTableRow> getSelectedRows() {

        return new ArrayList<CmsRewriteAliasTableRow>(getSelectionModel().getSelectedSet());

    }

    /**
     * @see com.google.gwt.user.cellview.client.AbstractHasData#getSelectionModel()
     *
     * (Overridden to use a less general return type)
     */
    @SuppressWarnings("unchecked")
    @Override
    public MultiSelectionModel<CmsRewriteAliasTableRow> getSelectionModel() {

        return (MultiSelectionModel<CmsRewriteAliasTableRow>)(super.getSelectionModel());
    }

    /**
     * Gets the controller instance for the alias view.<p>
     *
     * @return the controller instance for the alias view
     **/
    CmsAliasTableController getController() {

        return m_controller;
    }

}

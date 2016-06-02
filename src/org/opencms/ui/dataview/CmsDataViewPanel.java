/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.dataview;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.dataview.CmsPagingControls.I_PagingCallback;
import org.opencms.ui.util.CmsComponentField;
import org.opencms.widgets.dataview.CmsDataViewColumn;
import org.opencms.widgets.dataview.CmsDataViewFilter;
import org.opencms.widgets.dataview.CmsDataViewQuery;
import org.opencms.widgets.dataview.CmsDataViewResult;
import org.opencms.widgets.dataview.I_CmsDataView;
import org.opencms.widgets.dataview.I_CmsDataViewItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Panel containing both the interface elements used to search the data source (query field, filter select boxes) as well
 * as the paged list of search results.<p>
 */
public class CmsDataViewPanel extends VerticalLayout {

    /**
     * Subclass of Table, which we need because we want to trigger a complete refresh when sorting instead of just sorting the in-memory data.<p>
     */
    public class PagedTable extends Table {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance.<p>
         *
         * @param container the data container
         */
        public PagedTable(IndexedContainer container) {
            super();
            setContainerDataSource(container);
        }

        /**
         * @see com.vaadin.ui.Table#sort(java.lang.Object[], boolean[])
         */
        @Override
        public void sort(Object[] propertyId, boolean[] ascending) throws UnsupportedOperationException {

            m_sortCol = propertyId[0];
            m_ascending = ascending[0];
            refreshData(true, null);
        }
    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The ID column name. */
    public static final Object ID_COLUMN = new Object();

    /** The data view instance used to access the data. */
    private I_CmsDataView m_dataView;

    /** The container used to store the current page of search results. */
    private IndexedContainer m_container;

    /** Widget used to move to different pages. */
    private CmsPagingControls m_pagingControls;

    /** The panel containing the table. */
    private Panel m_tablePanel;

    /** The table container. */
    private CssLayout m_tableContainer;

    /** The widget containing the filters. */
    private HorizontalLayout m_filterContainer;

    /** Query text field. */
    private TextField m_fullTextSearch;

    /** The sort column. */
    protected Object m_sortCol;

    /** The sort direction. */
    protected boolean m_ascending;

    /** The current list of filters. */
    private List<CmsDataViewFilter> m_filters = Lists.newArrayList();

    /** The current map of filters, by id. */
    private Map<String, CmsDataViewFilter> m_filterMap = Maps.newLinkedHashMap();

    /** The table with the search results. */
    private CmsComponentField<Table> m_table = CmsComponentField.newInstance();

    /**
     * Creates a new instance.<p>
     *
     * @param viewInstance the data view instance
     * @param multiselect true if multi-selection should be allowed
     */
    public CmsDataViewPanel(I_CmsDataView viewInstance, boolean multiselect) {
        m_dataView = viewInstance;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_pagingControls.addCallback(new I_PagingCallback() {

            public void pageChanged(int page) {

                refreshData(false, null);
            }
        });
        m_fullTextSearch.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                refreshData(true, event.getText());
            }
        });
        m_container = new IndexedContainer();
        for (CmsDataViewColumn column : m_dataView.getColumns()) {
            m_container.addContainerProperty(
                column.getId(),
                CmsColumnValueConverter.getColumnClass(column.getType()),
                null);

        }
        m_container.addContainerProperty(ID_COLUMN, String.class, null);

        PagedTable table = new PagedTable(m_container);
        table.setMultiSelect(multiselect);

        table.addStyleName("o-wrap-table");
        Object[] visibleCols = new String[m_dataView.getColumns().size()];
        int i = 0;
        for (CmsDataViewColumn col : m_dataView.getColumns()) {
            visibleCols[i++] = col.getId();

        }
        table.setVisibleColumns(visibleCols);
        for (CmsDataViewColumn col : m_dataView.getColumns()) {
            table.setColumnHeader(col.getId(), col.getNiceName());
            table.setColumnWidth(col.getId(), col.getPreferredWidth());
        }

        table.setPageLength(0);
        table.setWidth("100%"); //
        m_table.set(table);
        table.setSelectable(true);

        m_tableContainer.addComponent(table);
        addAttachListener(new AttachListener() {

            private static final long serialVersionUID = 1L;

            public void attach(AttachEvent event) {

                refreshData(true, null);
            }
        });
        List<CmsDataViewFilter> filters = new ArrayList<CmsDataViewFilter>(m_dataView.getFilters());
        updateFilters(filters);
    }

    /**
     * Fills the given item.<p>
     *
     * @param item the data view item
     * @param target the table item
     */
    public void fillItem(I_CmsDataViewItem item, Item target) {

        for (CmsDataViewColumn column : m_dataView.getColumns()) {
            String name = column.getId();
            Object value = CmsColumnValueConverter.getColumnValue(item.getColumnData(name), column.getType());
            target.getItemProperty(name).setValue(value);
        }
        target.getItemProperty(CmsDataViewPanel.ID_COLUMN).setValue(item.getId());
    }

    /**
     * Gets the list of selected data items.<p>
     *
     * If this widget is not in multi-select mode, a list with a single result will be returned.<p>
     *
     * @return the selected results
     */
    public List<I_CmsDataViewItem> getSelection() {

        List<I_CmsDataViewItem> result = Lists.newArrayList();
        Object val = m_table.get().getValue();
        if (val == null) {
            return result;
        }
        if (val instanceof Collection) {
            Collection<?> results = (Collection<?>)val;
            for (Object obj : results) {
                result.add(m_dataView.getItemById((String)obj));
            }
        } else {
            result.add(m_dataView.getItemById((String)val));
        }
        return result;

    }

    /**
     * Gets the table.<p>
     *
     * @return the table
     */
    public Table getTable() {

        return m_table.get();
    }

    /**
     * Updates the data displayed in the table.<p>
     *
     * @param resetPaging true if we should go back to page 1
     * @param textQuery the text query to use
     */
    public void refreshData(boolean resetPaging, String textQuery) {

        String fullTextQuery = textQuery != null ? textQuery : m_fullTextSearch.getValue();
        LinkedHashMap<String, String> filterValues = new LinkedHashMap<String, String>();
        for (Map.Entry<String, CmsDataViewFilter> entry : m_filterMap.entrySet()) {
            filterValues.put(entry.getKey(), entry.getValue().getValue());

        }
        CmsDataViewQuery query = new CmsDataViewQuery();
        String sortCol = (String)m_sortCol;
        boolean ascending = m_ascending;
        query.setFullTextQuery(fullTextQuery);
        query.setFilterValues(filterValues);
        query.setSortColumn(sortCol);
        query.setSortAscending(ascending);
        CmsDataViewResult result = m_dataView.getResults(
            query,
            resetPaging ? 0 : getOffset(),
            m_dataView.getPageSize());
        m_container.removeAllItems();
        for (I_CmsDataViewItem item : result.getItems()) {
            fillItem(item, m_container.addItem(item.getId()));
        }
        m_tablePanel.setScrollTop(0);

        if (resetPaging) {
            int total = result.getHitCount();
            m_pagingControls.reset(result.getHitCount(), m_dataView.getPageSize(), false);
        }
    }

    /**
     * Updates the search results after a filter is changed by the user.<p>
     *
     * @param id the filter id
     * @param value the filter value
     */
    public void updateFilter(String id, String value) {

        CmsDataViewFilter oldFilter = m_filterMap.get(id);
        CmsDataViewFilter newFilter = oldFilter.copyWithValue(value);
        m_filterMap.put(id, newFilter);
        List<CmsDataViewFilter> filters = new ArrayList<CmsDataViewFilter>(m_filterMap.values());
        updateFilters(m_dataView.updateFilters(filters));
    }

    /**
     * Changes the displayed filters to a new set.<p>
     *
     * @param newFilters the new filters
     */
    public void updateFilters(List<CmsDataViewFilter> newFilters) {

        if (newFilters.isEmpty()) {
            m_filterContainer.setVisible(false);
        }
        if (m_filters.equals(newFilters)) {
            return;
        }
        m_filterContainer.removeAllComponents();
        m_filters = newFilters;
        m_filterMap.clear();
        for (CmsDataViewFilter filter : newFilters) {
            m_filterMap.put(filter.getId(), filter);
            final CmsDataViewFilter finalFilter = filter;
            ComboBox select = new ComboBox(filter.getNiceName());
            select.setWidth("175px");
            select.setNullSelectionAllowed(false);
            Map<String, String> options = filter.getOptions();
            for (Map.Entry<String, String> entry : options.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                select.addItem(key);
                select.setItemCaption(key, value);
            }
            select.setValue(filter.getValue());
            if (filter.getHelpText() != null) {
                select.setDescription(filter.getHelpText());
            }

            select.addValueChangeListener(new ValueChangeListener() {

                private static final long serialVersionUID = 1L;

                public void valueChange(ValueChangeEvent event) {

                    String newValue = (String)(event.getProperty().getValue());
                    updateFilter(finalFilter.getId(), newValue);
                    refreshData(true, null);
                }
            });
            m_filterContainer.addComponent(select);
        }
    }

    /**
     * Gets the offset.<p>
     *
     * @return the offset
     */
    private int getOffset() {

        return m_pagingControls.getPage() * m_dataView.getPageSize();
    }

}

/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
import org.opencms.ui.components.OpenCmsTheme;
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
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
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

    /** Search button. */
    private Button m_searchButton;

    /** The container used to store the current page of search results. */
    private IndexedContainer m_container;

    /** Widget used to move to different pages. */
    private CmsPagingControls m_pagingControls;

    /** The table container. */
    private CssLayout m_tablePlaceholder;

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

    /** Map of check boxes. */
    private Map<Object, CheckBox> m_checkBoxes = Maps.newHashMap();

    /** The current map of filters, by id. */
    private Map<String, CmsDataViewFilter> m_filterMap = Maps.newLinkedHashMap();

    /** The table with the search results. */
    private CmsComponentField<Table> m_table = CmsComponentField.newInstance();

    /** True if we are currently in a recursive call of the value change event listener. */
    private boolean m_recursiveValueChange;

    /** The real selection (includes item. */
    private Set<Object> m_realSelection = Sets.newHashSet();

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
        m_fullTextSearch.addShortcutListener(new ShortcutListener("Save", KeyCode.ENTER, null) {

            private static final long serialVersionUID = 1L;

            @Override
            public void handleAction(Object sender, Object target) {

                refreshData(false, null);

            }
        });
        m_searchButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                refreshData(true, null);
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

        final PagedTable table = new PagedTable(m_container);
        table.addStyleName(OpenCmsTheme.TABLE_CELL_PADDING);
        table.setMultiSelect(multiselect);

        table.addGeneratedColumn("checked", new ColumnGenerator() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public Object generateCell(final Table source, final Object itemId, final Object columnId) {

                CheckBox cb = getCheckBox(itemId);
                cb.setValue(Boolean.valueOf(source.isSelected(itemId)));
                cb.addValueChangeListener(new ValueChangeListener() {

                    private static final long serialVersionUID = 1L;

                    public void valueChange(ValueChangeEvent event) {

                        boolean val = ((Boolean)(event.getProperty().getValue())).booleanValue();
                        if (val) {
                            source.select(itemId);
                        } else {
                            source.unselect(itemId);
                        }
                    }
                });
                return cb;
            }
        });

        table.addStyleName("o-wrap-table");
        Object[] visibleCols = new String[m_dataView.getColumns().size() + 1];
        visibleCols[0] = "checked";
        int i = 1;
        for (CmsDataViewColumn col : m_dataView.getColumns()) {
            visibleCols[i++] = col.getId();

        }
        table.setVisibleColumns(visibleCols);
        table.setColumnWidth("checked", 45);
        table.setColumnHeader("checked", "");
        for (CmsDataViewColumn col : m_dataView.getColumns()) {
            table.setColumnHeader(col.getId(), col.getNiceName());
            table.setColumnWidth(col.getId(), col.getPreferredWidth());
        }

        table.setPageLength(0);
        table.setWidth("100%"); //
        table.setHeight("100%");
        m_table.set(table);
        table.setSelectable(true);
        replaceComponent(m_tablePlaceholder, table);
        setExpandRatio(table, 1.0f);

        addAttachListener(new AttachListener() {

            private static final long serialVersionUID = 1L;

            public void attach(AttachEvent event) {

                refreshData(true, null);
            }
        });
        table.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void valueChange(ValueChangeEvent event) {

                if (table.isMultiSelect()) {
                    if (m_recursiveValueChange) {
                        updateCheckboxesWithSelectedIds(m_realSelection);
                    } else {
                        updateRealSelection(getIdsFromSelection(event));
                        if (m_realSelection.equals(event.getProperty().getValue())) {
                            updateCheckboxesWithSelectedIds(m_realSelection);
                        } else {
                            try {
                                m_recursiveValueChange = true;
                                m_table.get().setValue(m_realSelection);
                            } finally {
                                m_recursiveValueChange = false;
                            }
                        }
                    }

                } else {
                    Set<Object> ids = getIdsFromSelection(event);
                    updateCheckboxesWithSelectedIds(ids);

                }

            }

            /**
             * Gets the ids from the selection event.<p>
             *
             * @param event a selection event
             *
             * @return the set of ids from the selection event
             */
            protected Set<Object> getIdsFromSelection(ValueChangeEvent event) {

                Set<Object> ids = Sets.newHashSet();
                if (event != null) {
                    if (event.getProperty().getValue() instanceof Collection) {
                        ids.addAll((Collection<?>)event.getProperty().getValue());
                    } else {
                        ids.add(event.getProperty().getValue());
                    }
                }
                return ids;
            }

            @SuppressWarnings("synthetic-access")
            protected void updateCheckboxesWithSelectedIds(Set<Object> selectedIds) {

                for (Map.Entry<Object, CheckBox> entry : m_checkBoxes.entrySet()) {
                    if (!(selectedIds.contains(entry.getKey()))) {
                        entry.getValue().setValue(Boolean.FALSE);
                    }
                }

                for (Object id : selectedIds) {
                    getCheckBox(id).setValue(Boolean.TRUE);
                }
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
        //m_tablePanel.setScrollTop(0);

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
            select.setPageLength(0);
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
                }
            });
            m_filterContainer.addComponent(select);
        }
    }

    /**
     * Updates the real selection, given the item ids from the selection event.<p>
     *
     * @param selectionEventIds the item ids from the selection event
     */
    protected void updateRealSelection(Set<Object> selectionEventIds) {

        Set<Object> pageItems = Sets.newHashSet(m_table.get().getContainerDataSource().getItemIds());
        Set<Object> result = Sets.newHashSet(m_realSelection);
        result.removeAll(pageItems);
        result.addAll(selectionEventIds);
        m_realSelection = result;

    }

    /**
     * Gets the check box for the item with the given id.<p>
     *
     * @param id the item id
     * @return the check box
     */
    private CheckBox getCheckBox(Object id) {

        if (!m_checkBoxes.containsKey(id)) {
            m_checkBoxes.put(id, new CheckBox());
        }
        return m_checkBoxes.get(id);
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

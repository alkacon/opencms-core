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

package org.opencms.ui.util.table;

import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsLog;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.vaadin.data.util.BeanUtil;
import com.vaadin.ui.Button;
import com.vaadin.v7.data.Container.Filter;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.Align;
import com.vaadin.v7.ui.Table.CellStyleGenerator;

/**
 * Builds a table based on a given bean class.<p>
 *
 * The columns of the table correspond to getters of the given bean class with the Column annotation.
 *
 * @param <T> The class of the bean containing the metadata of the table
 */
public class CmsBeanTableBuilder<T> {

    /**
     * Contains information about a single column.<p>
     */
    private class ColumnBean {

        /** The annotation for the getter. */
        private Column m_info;

        /** Property descriptor for the getter for that column. */
        private PropertyDescriptor m_property;

        /**
         * Creates a new instance.<p>
         *
         * @param property the property descriptor for the getter
         * @param info the annotation for the getter
         */
        public ColumnBean(PropertyDescriptor property, Column info) {

            super();
            m_property = property;
            m_info = info;
        }

        /**
         * Returns the info.<p>
         *
         * @return the info
         */
        public Column getInfo() {

            return m_info;
        }

        /**
         * Returns the property.<p>
         *
         * @return the property
         */
        public PropertyDescriptor getProperty() {

            return m_property;
        }
    }

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsBeanTableBuilder.class);

    /** Bean type for the table. */
    private Class<T> m_class;

    /** Beans representing the table columns. */
    private List<ColumnBean> m_columns = Lists.newArrayList();

    /** The macro resolver to use for resolving macros in column headers. */
    private CmsMacroResolver m_macroResolver = new CmsMacroResolver();

    /** The current view. */
    private String m_view;

    /**
     * Creates a new table builder instance for the given bean class and view.<p>
     *
     * Depending on the view configuration of the columns, columns may be hidden depending on the view.
     *
     * @param cls the bean class
     * @param view the selected view
     *
     */
    public CmsBeanTableBuilder(Class<T> cls, String view) {

        m_class = cls;
        m_view = view;
        try {
            List<PropertyDescriptor> descriptors = BeanUtil.getBeanPropertyDescriptors(m_class);
            for (PropertyDescriptor desc : descriptors) {
                Method getter = desc.getReadMethod();
                if (getter != null) {
                    Column columnInfo = getter.getAnnotation(Column.class);
                    if (columnInfo != null) {
                        if ((columnInfo.view() == null) || matchView(m_view, columnInfo.view())) {
                            m_columns.add(new ColumnBean(desc, columnInfo));
                        }
                    }
                }
            }

            Collections.sort(m_columns, new Comparator<ColumnBean>() {

                public int compare(CmsBeanTableBuilder<T>.ColumnBean col1, CmsBeanTableBuilder<T>.ColumnBean col2) {

                    return ComparisonChain.start().compare(col1.getInfo().order(), col2.getInfo().order()).result();
                }
            });
        } catch (IntrospectionException e) {
            // Shouldn't normally happen
            LOG.error(e.getLocalizedMessage(), e);
            throw new IllegalArgumentException(e);

        }
    }

    /**
     * Checks if the given string is likely a message key.<p>
     *
     * @param str the input string
     * @return true if this is probably a message key
     */
    public static boolean isProbablyMessageKey(String str) {

        return str.matches("^[A-Z]+_[A-Z0-9_]*$");
    }

    /**
     * Convenience method used to create a new instance of a table builder.<p>
     *
     * @param cls the bean class
     * @return the new table builder
     */
    public static <V> CmsBeanTableBuilder<V> newInstance(Class<V> cls) {

        return new CmsBeanTableBuilder<V>(cls, null);

    }

    /**
     * Convenience method used to create a new instance of a table builder.<p>
     *
     * @param cls the bean class
     * @param view the selected view
     *
     * @return the new table builder
     */
    public static <V> CmsBeanTableBuilder<V> newInstance(Class<V> cls, String view) {

        return new CmsBeanTableBuilder<V>(cls, view);

    }

    /**
     * Builds a table and uses the given beans to fill its rows.<p>
     *
     * @param beans the beans to display in the table
     *
     * @return the finished table
     */
    public Table buildTable(List<T> beans) {

        Table table = new Table();
        buildTable(table, beans);
        return table;
    }

    /**
     * Sets up a table and uses the given beans to fill its rows, but does not actually create the table instance; it uses the passed in table instance instead.<p>
     *
     * @param table the table to set up
     * @param beans the beans to display in the table
     *
     */
    public void buildTable(Table table, List<T> beans) {

        BeanItemContainer<T> container = new BeanItemContainer<T>(m_class);
        List<String> visibleCols = Lists.newArrayList();
        for (ColumnBean column : m_columns) {
            String propName = column.getProperty().getName();
            String columnHeader = column.getInfo().header();
            String localizedHeader = CmsVaadinUtils.getMessageText(columnHeader);
            if (CmsMessages.isUnknownKey(localizedHeader)) {
                localizedHeader = columnHeader;
            }
            localizedHeader = m_macroResolver.resolveMacros(localizedHeader);
            table.setColumnHeader(propName, localizedHeader);
            if (Button.class.isAssignableFrom(column.getProperty().getPropertyType())) {
                table.setColumnAlignment(propName, Align.CENTER);
            }
            visibleCols.add(propName);
        }
        table.setContainerDataSource(container);
        table.setVisibleColumns(visibleCols.toArray());

        for (ColumnBean column : m_columns) {
            Column info = column.getInfo();
            String name = column.getProperty().getName();
            if (info.width() >= 0) {
                table.setColumnWidth(name, info.width());
            }
            if (info.expandRatio() >= 0) {
                table.setColumnExpandRatio(name, info.expandRatio());
            }
        }
        for (T bean : beans) {
            container.addBean(bean);
        }

    }

    /**
     * Creates a default cell style generator which just returns the value of the styleName attribute in a Column annotation for cells in that column.<p>
     *
     * @return the default cell style generator
     */
    public CellStyleGenerator getDefaultCellStyleGenerator() {

        return new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public String getStyle(Table source, Object itemId, Object propertyId) {

                for (ColumnBean colBean : m_columns) {
                    if (colBean.getProperty().getName().equals(propertyId)) {
                        return colBean.getInfo().styleName();
                    }
                }
                return "";
            }
        };
    }

    /**
     * Creates a default filter which just searches the lower case version of the result of the toString() method applied to all columns with the annotation attribute filterable = true.<P>
     *
     * @param filterString the string for which to filter
     *
     * @return the default filter for the given filter string
     */
    public Filter getDefaultFilter(final String filterString) {

        return new Filter() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public boolean appliesToProperty(Object propertyId) {

                for (ColumnBean col : m_columns) {
                    if (col.getProperty().getName().equals(propertyId) && col.getInfo().filterable()) {
                        return true;
                    }
                }
                return false;
            }

            @SuppressWarnings("synthetic-access")
            public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {

                if (CmsStringUtil.isEmpty(filterString)) {
                    return true;
                }
                T bean = (T)itemId;
                for (ColumnBean col : m_columns) {
                    if (col.getInfo().filterable()) {
                        if (("" + item.getItemProperty(col.getProperty().getName()).getValue()).toLowerCase().contains(
                            filterString)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };

    }

    /**
     * Gets the macro resolver which is used for column headers.<p>
     *
     * @return the macro resolver for column headers
     */
    public CmsMacroResolver getMacroResolver() {

        return m_macroResolver;
    }

    /**
     * Sets the macro resolver.<p>
     *
     * @param resolver the macro resolver
     */
    public void setMacroResolver(CmsMacroResolver resolver) {

        m_macroResolver = resolver;
    }

    /**
     * Checks if the actual view matches a view declaration.<p>
     *
     * @param actualView the actual view
     * @param declaredView the declared view string
     *
     * @return true if the view matches
     */
    private boolean matchView(String actualView, String declaredView) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(declaredView) || CmsStringUtil.isEmptyOrWhitespaceOnly(actualView)) {
            return true;
        }
        return CmsStringUtil.splitAsList(declaredView, "|").contains(actualView);

    }
}

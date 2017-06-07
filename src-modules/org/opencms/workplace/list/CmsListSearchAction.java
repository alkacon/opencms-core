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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.list;

import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Default implementation for a search action in an html list.<p>
 *
 * It allows to search in several columns, including item details.<p>
 *
 * @since 6.0.0
 */
public class CmsListSearchAction extends A_CmsListSearchAction {

    /** the html id for the input element of the search bar. */
    public static final String SEARCH_BAR_INPUT_ID = "listSearchFilter";

    /** Signals whether the search is case sensitive or not. */
    private boolean m_caseInSensitive;

    /** Ids of Columns to search into. */
    private final List<CmsListColumnDefinition> m_columns = new ArrayList<CmsListColumnDefinition>();

    /**
     * Default Constructor.<p>
     *
     * @param column the column to search into
     */
    public CmsListSearchAction(CmsListColumnDefinition column) {

        super();
        useDefaultShowAllAction();
        m_columns.add(column);
    }

    /**
     * Adds a column to search into.<p>
     *
     * @param column the additional column to search into
     */
    public void addColumn(CmsListColumnDefinition column) {

        m_columns.add(column);
    }

    /**
     * Returns the html code for the search bar.<p>
     *
     * @param wp the workplace context
     *
     * @return html code
     */
    public String barHtml(CmsWorkplace wp) {

        if (wp == null) {
            wp = getWp();
        }
        StringBuffer html = new StringBuffer(1024);
        html.append("\t\t<input type='text' name='");
        html.append(SEARCH_BAR_INPUT_ID);
        html.append("' id='");
        html.append(SEARCH_BAR_INPUT_ID);
        html.append("' value='");
        if (wp instanceof A_CmsListDialog) {
            // http://www.securityfocus.com/archive/1/490498: searchfilter cross site scripting vulnerability:
            html.append(
                CmsStringUtil.escapeJavaScript(
                    CmsEncoder.escapeXml(((A_CmsListDialog)wp).getList().getSearchFilter())));
        }
        html.append("' size='20' maxlength='245' style='vertical-align: bottom;' >\n");
        html.append(buttonHtml(wp));
        if (getShowAllAction() != null) {
            html.append("&nbsp;&nbsp;");
            html.append(getShowAllAction().buttonHtml());
        }
        return html.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListSearchAction#buttonHtml(org.opencms.workplace.CmsWorkplace)
     */
    @Override
    public String buttonHtml(CmsWorkplace wp) {

        // delay the composition of the help text as much as possible
        if (getHelpText() == EMPTY_MESSAGE) {
            String columns = "";
            Iterator<CmsListColumnDefinition> it = m_columns.iterator();
            while (it.hasNext()) {
                CmsListColumnDefinition col = it.next();
                columns += "${key." + col.getName().getKey() + "}";
                if (it.hasNext()) {
                    columns += ", ";
                }
            }
            if (columns.lastIndexOf(", ") > 0) {
                columns = columns.substring(0, columns.lastIndexOf(", "))
                    + " and "
                    + columns.substring(columns.lastIndexOf(", ") + 2);
            }
            setHelpText(
                new CmsMessageContainer(
                    Messages.get(),
                    Messages.GUI_LIST_ACTION_SEARCH_HELP_1,
                    new Object[] {columns}));
        }
        return super.buttonHtml(wp);
    }

    /**
     * Returns a sublist of the given items, that match the given filter string.<p>
     *
     * @param items the items to filter
     * @param filter the string to filter
     *
     * @return the filtered sublist
     */
    public List<CmsListItem> filter(List<CmsListItem> items, String filter) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(filter)) {
            return items;
        }
        String filterCriteria = filter;
        if (m_caseInSensitive) {
            filterCriteria = filter.toLowerCase();
        }

        List<CmsListItem> res = new ArrayList<CmsListItem>();
        Iterator<CmsListItem> itItems = items.iterator();
        while (itItems.hasNext()) {
            CmsListItem item = itItems.next();
            if (res.contains(item)) {
                continue;
            }
            Iterator<CmsListColumnDefinition> itCols = m_columns.iterator();
            while (itCols.hasNext()) {
                CmsListColumnDefinition col = itCols.next();
                if (item.get(col.getId()) == null) {
                    continue;
                }
                String columnValue = item.get(col.getId()).toString();
                if (m_caseInSensitive) {
                    columnValue = columnValue.toLowerCase();
                }
                if (columnValue.indexOf(filterCriteria) > -1) {
                    res.add(item);
                    break;
                }
            }
        }
        return res;
    }

    /**
     * Returns the list of columns to be searched.<p>
     *
     * @return a list of {@link CmsListColumnDefinition} objects
     */
    public List<CmsListColumnDefinition> getColumns() {

        return Collections.unmodifiableList(m_columns);
    }

    /**
     * Returns the caseInSensitive.<p>
     *
     * @return the caseInSensitive
     */
    public boolean isCaseInSensitive() {

        return m_caseInSensitive;
    }

    /**
     * Sets the caseInSensitive.<p>
     *
     * @param caseInSensitive the caseInSensitive to set
     */
    public void setCaseInSensitive(boolean caseInSensitive) {

        m_caseInSensitive = caseInSensitive;
    }

    /**
     * Sets the current search filter.<p>
     *
     * @param filter the current search filter
     *
     * @deprecated use {@link CmsHtmlList#setSearchFilter(String)} instead
     */
    @Deprecated
    public void setSearchFilter(String filter) {

        // empty
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListAction#setWp(org.opencms.workplace.list.A_CmsListDialog)
     */
    @Override
    public void setWp(A_CmsListDialog wp) {

        super.setWp(wp);
        if (getShowAllAction() != null) {
            getShowAllAction().setWp(wp);
        }
    }
}
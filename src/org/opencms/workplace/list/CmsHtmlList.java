/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/CmsHtmlList.java,v $
 * Date   : $Date: 2005/04/26 14:59:50 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.list;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * The main class of the html list widget.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsHtmlList {

    /** Constant for item separator char used for coding/encoding multiselection. */
    public static final String C_ITEM_SEPARATOR = "|";

    /** Current displayed page number. */
    private int m_currentPage;

    /** Current sort order. */
    private CmsListOrderEnum m_currentSortOrder;

    /** Filtered list of items or <code>null</code> if no filter is set. */
    private List m_filteredItems;

    /** Dhtml id. */
    private final String m_id;

    /** Maximum number of items per page. */
    private int m_maxItemsPerPage = 20;

    /** Metadata for building the list. */
    private final CmsListMetadata m_metadata;

    /** Display Name of the list. */
    private final CmsMessageContainer m_name;

    /** Really content of the list. */
    private final List m_originalItems = new ArrayList();

    /** Search filter text. */
    private String m_searchFilter;

    /** Column name to be sorted. */
    private String m_sortedColumn;

    /** Items currently displayed. */
    private List m_visibleItems;

    /**
     * Default Constructor.<p>
     * 
     * @param id unique id of the list, is used as name for controls and js functions and vars
     * @param name the display name 
     * @param metadata the list's metadata
     */
    public CmsHtmlList(String id, CmsMessageContainer name, CmsListMetadata metadata) {

        m_id = id;
        m_name = name;
        m_metadata = metadata;
        m_currentPage = 1;
    }

    /**
     * Adds a collection new list items to the content of the list.<p>
     * 
     * @param listItems the collection of list items to add
     * 
     * @see List#addAll(Collection)
     */
    public void addAllItems(Collection listItems) {

        m_originalItems.addAll(listItems);
    }

    /**
     * Adds a new item to the content of the list.<p>
     * 
     * @param listItem the list item
     * 
     * @see List#add(Object)
     */
    public void addItem(CmsListItem listItem) {

        m_originalItems.add(listItem);
    }

    /**
     * Adds a new item at the given position to the content of the list.<p>
     * 
     * @param listItem the list item
     * @param position the insertion point
     * 
     * @see List#add(int, Object)
     */
    public void addItem(CmsListItem listItem, int position) {

        m_originalItems.add(position, listItem);
    }

    // TODO: think about wenn and/or how to refresh
    // TODO: new CmsListInfo class with less data for CmsWorkplaceSettings

    /**
     * This method resets the content of the list (no the metadata).<p>
     * 
     * @param locale the locale for sorting/searching
     */
    public void clear(Locale locale) {

        m_originalItems.clear();
        m_filteredItems = null;
        m_visibleItems.clear();
        setSearchFilter("", locale);
        m_sortedColumn = null;
    }

    /**
     * Returns all list items in the list, may be not visible and sorted.<p> 
     * 
     * @return all list items
     */
    public List getAllContent() {

        return m_originalItems;
    }

    /**
     * Returns the filtered list of list items.<p>
     * 
     * Equals to <code>{@link #getAllContent()}</code> if no filter is set.<p>
     * 
     * @return the filtered list of list items
     */
    public List getContent() {

        if (m_filteredItems == null) {
            return getAllContent();
        } else {
            return m_filteredItems;
        }
    }

    /**
     * returns the number of the current page.<p>
     * 
     * @return the number of the current page
     */
    public int getCurrentPage() {

        return m_currentPage;
    }

    /**
     * Returns the current used sort order.<p>
     * 
     * @return the current used sort order
     */
    public CmsListOrderEnum getCurrentSortOrder() {

        return m_currentSortOrder;
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * This method returns the item identified by the parameter id.<p>
     * 
     * Only current visible item can be retrieved using this method.<p> 
     * 
     * @param id the id of the item to look for
     * 
     * @return the requested item or <code>null</code> if not found
     */
    public CmsListItem getItem(String id) {

        Iterator it = m_visibleItems.iterator();
        while (it.hasNext()) {
            CmsListItem item = (CmsListItem)it.next();
            if (id.equals(item.getId())) {
                return item;
            }
        }
        return null;
    }

    /**
     * Returns the maximum number of items per page.<p>
     *
     * @return the maximum number of items per page
     */
    public int getMaxItemsPerPage() {

        return m_maxItemsPerPage;
    }

    /**
     * Returns the metadata.<p>
     *
     * @return the metadata
     */
    public CmsListMetadata getMetadata() {

        return m_metadata;
    }

    /**
     * Returns the name of the list.<p>
     * 
     * @return the list's name
     */
    public CmsMessageContainer getName() {

        return m_name;
    }

    /**
     * Returns the filtered number of pages.<p>
     * 
     * Equals to <code>{@link #getTotalNumberOfPages()}</code> if no filter is set.<p>
     * 
     * @return the filtered of pages
     */
    public int getNumberOfPages() {

        return (int)Math.ceil((double)getSize() / getMaxItemsPerPage());
    }

    /**
     * Returns the search filter.<p>
     *
     * @return the search filter
     */
    public String getSearchFilter() {

        return m_searchFilter;
    }

    /**
     * Return the filtered number of items.<p>
     * 
     * Equals to <code>{@link #getTotalSize()}</code> if no filter is set.<p>
     * 
     * @return the filtered number of items
     */
    public int getSize() {

        return getContent().size();
    }

    /**
     * Returns the sorted column's name.<p>
     * 
     * @return the sorted column's name
     */
    public String getSortedColumn() {

        return m_sortedColumn;
    }

    /**
     * Returns the total number of pages.<p> 
     * 
     * @return the total number of pages
     */
    public int getTotalNumberOfPages() {

        return (int)Math.ceil((double)m_originalItems.size() / getMaxItemsPerPage());
    }

    /**
     * Return the total number of items.<p>
     * 
     * @return the total number of items
     */
    public int getTotalSize() {

        return m_originalItems.size();
    }

    /**
     * Generates the html code for the list.<p>
     * 
     * @param wp the workplace object
     * 
     * @return html code
     */
    public String listHtml(CmsWorkplace wp) {

        if (displayedFrom() == 0) {
            // empty list
            m_visibleItems = new ArrayList();
        } else {
            m_visibleItems = new ArrayList(getContent().subList(displayedFrom() - 1, displayedTo()));
        }

        StringBuffer html = new StringBuffer(5120);
        html.append(htmlBegin(wp));
        html.append(htmlTitle(wp));
        html.append(htmlToolBar(wp));
        html.append("<table width='100%' cellpadding='1' cellspacing='0' class='list'>\n");
        html.append(m_metadata.htmlHeader(this, wp.getLocale()));
        if (m_visibleItems.isEmpty()) {
            html.append(m_metadata.htmlEmptyTable(wp.getLocale()));
        } else {
            Iterator itItems = m_visibleItems.iterator();
            boolean odd = true;
            while (itItems.hasNext()) {
                CmsListItem item = (CmsListItem)itItems.next();
                html.append(m_metadata.htmlItem(getId(), item, wp, odd));
                odd = !odd;
            }
        }
        html.append("</table>\n");
        html.append(htmlPagingBar(wp.getLocale()));
        html.append(htmlEnd(wp));
        return wp.resolveMacros(html.toString());
    }

    /**
     * Generate the need js code for the list.<p>
     * 
     * @param wp the workplace object
     * 
     * @return js code
     */
    public String listJs(CmsWorkplace wp) {

        StringBuffer js = new StringBuffer(1024);
        js.append("<script language='javascript' type='text/javascript'>\n");
        js.append("\tfunction ");
        js.append(m_id);
        js.append("ListAction(action, confirmation, listItem) {\n");
        js.append("\t\tvar form = document.forms['");
        js.append(getId());
        js.append("-form'];\n");
        js.append("\t\tif (confirmation!='null' && confirmation!='') {\n");
        js.append("\t\t\tif (!confirm(confirmation)) {\n");
        js.append("\t\t\t\treturn false;\n");
        js.append("\t\t\t}\n");
        js.append("\t\t}\n");
        js.append("\t\tform.action.value='");
        js.append(CmsListDialog.LIST_SINGLE_ACTION);
        js.append("';\n");
        js.append("\t\tform.");
        js.append(CmsListDialog.PARAM_LIST_ACTION);
        js.append(".value=action;\n");
        js.append("\t\tform.");
        js.append(CmsListDialog.PARAM_SEL_ITEMS);
        js.append(".value=listItem;\n");
        js.append("\t\tsubmitForm(form);\n");
        js.append("\t}\n");
        js.append("\tfunction ");
        js.append(m_id);
        js.append("ListIndepAction(action, confirmation) {\n");
        js.append("\t\tvar form = document.forms['");
        js.append(getId());
        js.append("-form'];\n");
        js.append("\t\tif (confirmation!='null' && confirmation!='') {\n");
        js.append("\t\t\tif (!confirm(confirmation)) {\n");
        js.append("\t\t\t\treturn false;\n");
        js.append("\t\t\t}\n");
        js.append("\t\t}\n");
        js.append("\t\tif (action=='");
        js.append(CmsSearchAction.SEARCH_ACTION_ID);
        js.append("') {\n");
        js.append("\t\t\tform.action.value = '");
        js.append(CmsListDialog.LIST_SEARCH);
        js.append("';\n");
        js.append("\t\t\tform.");
        js.append(CmsListDialog.PARAM_SEARCH_FILTER);
        js.append(".value = form.");
        js.append(getId());
        js.append("Filter.value;\n");
        js.append("\t\t\tsubmitForm(form);\n");
        js.append("\t\t\treturn;\n");
        js.append("\t\t}\n");
        js.append("\t\tif (action=='");
        js.append(CmsSearchAction.SHOWALL_ACTION_ID);
        js.append("') {\n");
        js.append("\t\t\tform.action.value = '");
        js.append(CmsListDialog.LIST_SEARCH);
        js.append("';\n");
        js.append("\t\t\tform.");
        js.append(CmsListDialog.PARAM_SEARCH_FILTER);
        js.append(".value = '';\n");
        js.append("\t\t\tsubmitForm(form);\n");
        js.append("\t\t\treturn;\n");
        js.append("\t\t}\n");
        js.append("\t\tform.action.value='");
        js.append(CmsListDialog.LIST_INDEPENDENT_ACTION);
        js.append("';\n");
        js.append("\t\tform.");
        js.append(CmsListDialog.PARAM_LIST_ACTION);
        js.append(".value=action;\n");
        js.append("\t\tsubmitForm(form);\n");
        js.append("\t}\n");
        js.append("\n\tfunction ");
        js.append(m_id);
        js.append("ListSelect() {\n");
        js.append("\t\tvar form = document.forms['");
        js.append(getId());
        js.append("-form'];\n");
        js.append("\t\tfor (i = 0 ; i < form.elements.length; i++) {\n");
        js.append("\t\t\tif ((form.elements[i].type == 'checkbox') && (form.elements[i].name == '");
        js.append(m_id);
        js.append("MultiAction')) {\n");
        js.append("\t\t\t\tif (!(form.elements[i].value == 'DISABLED' || form.elements[i].disabled)) {\n");
        js.append("\t\t\t\t\tform.elements[i].checked = form.");
        js.append(m_id);
        js.append("ListSelectAll.checked;\n");
        js.append("\t\t\t\t}\n");
        js.append("\t\t\t}\n");
        js.append("\t\t}\n");
        js.append("\t\treturn true;\n");
        js.append("\t}\n");
        js.append("\tfunction ");
        js.append(m_id);
        js.append("ListMultiAction(action, confirmation) {\n");
        js.append("\t\tvar form = document.forms['");
        js.append(getId());
        js.append("-form'];\n");
        js.append("\t\tvar count = 0;\n");
        js.append("\t\tvar listItems = '';\n");
        js.append("\t\tfor (i = 0 ; i < form.elements.length; i++) {\n");
        js.append("\t\t\tif ((form.elements[i].type == 'checkbox') && (form.elements[i].name == '");
        js.append(m_id);
        js.append("MultiAction')) {\n");
        js
            .append("\t\t\t\tif (form.elements[i].checked && !(form.elements[i].value == 'DISABLED' || form.elements[i].disabled)) {\n");
        js.append("\t\t\t\t\tcount++;\n");
        js.append("\t\t\t\t\tif (listItems!='') {\n");
        js.append("\t\t\t\t\t\tlistItems = listItems + '");
        js.append(C_ITEM_SEPARATOR);
        js.append("';\n");
        js.append("\t\t\t\t\t}\n");
        js.append("\t\t\t\t\tlistItems = listItems + form.elements[i].value;\n");
        js.append("\t\t\t\t}\n");
        js.append("\t\t\t}\n");
        js.append("\t\t}\n");
        js.append("\t\tif (count==0) {\n");
        js.append("\t\t\talert('");
        js.append(CmsStringUtil.escapeJavaScript(Messages.get().key(
            wp.getLocale(),
            Messages.GUI_LIST_ACTION_NO_SELECTION_0,
            null)));
        js.append("');\n");
        js.append("\t\t\treturn false;\n");
        js.append("\t\t}\n");
        js.append("\t\tif (confirmation!='null' && confirmation!='') {\n");
        js.append("\t\t\tif (!confirm(confirmation)) {\n");
        js.append("\t\t\t\treturn false;\n");
        js.append("\t\t\t}\n");
        js.append("\t\t}\n");
        js.append("\t\tform.action.value='");
        js.append(CmsListDialog.LIST_MULTI_ACTION);
        js.append("';\n");
        js.append("\t\tform.");
        js.append(CmsListDialog.PARAM_LIST_ACTION);
        js.append(".value=action;\n");
        js.append("\t\tform.");
        js.append(CmsListDialog.PARAM_SEL_ITEMS);
        js.append(".value=listItems;\n");
        js.append("\t\tsubmitForm(form);\n");
        js.append("\t}\n");
        js.append("\tfunction ");
        js.append(m_id);
        js.append("ListSort(column) {\n");
        js.append("\t\tvar form = document.forms['");
        js.append(getId());
        js.append("-form'];\n");
        js.append("\t\tform.action.value = '");
        js.append(CmsListDialog.LIST_SORT);
        js.append("';\n");
        js.append("\t\tform.");
        js.append(CmsListDialog.PARAM_SORT_COL);
        js.append(".value = column;\n");
        js.append("\t\tsubmitForm(form);\n");
        js.append("\t}\n");
        js.append("\tfunction ");
        js.append(m_id);
        js.append("ListSetPage(page) {\n");
        js.append("\t\tvar form = document.forms['");
        js.append(getId());
        js.append("-form'];\n");
        js.append("\t\tform.action.value = '");
        js.append(CmsListDialog.LIST_SELECT_PAGE);
        js.append("';\n");
        js.append("\t\tform.");
        js.append(CmsListDialog.PARAM_PAGE);
        js.append(".value = page;\n");
        js.append("\t\tsubmitForm(form);\n");
        js.append("\t}\n");
        js.append("</script>\n");
        return wp.resolveMacros(js.toString());
    }

    /**
     * Returns a new list item for this list.<p>
     * 
     * @param id the id of the item has to be unique 
     * @return a new list item
     */
    public CmsListItem newItem(String id) {

        return new CmsListItem(this.getMetadata(), id);
    }

    /**
     * Removes an item from the list, try to use it
     * instead of <code>{@link CmsListDialog#refreshList()}</code>.<p>
     * 
     * @param id the id of the item to remove
     * 
     * @return the removed list item
     */
    public CmsListItem removeItem(String id) {

        CmsListItem item = getItem(id);
        m_visibleItems.remove(item);
        if (m_filteredItems != null) {
            m_filteredItems.remove(item);
        }
        m_originalItems.remove(item);
        return item;
    }

    /**
     * Sets the current page.<p>
     *
     * @param currentPage the current page to set
     */
    public void setCurrentPage(int currentPage) {

        if (getSize() != 0) {
            if (currentPage < 1 || currentPage > getNumberOfPages()) {
                throw new IllegalArgumentException(Messages.get().key(
                    Messages.ERR_LIST_INVALID_PAGE_1,
                    new Integer(currentPage)));
            }
            m_currentPage = currentPage;
        } else {
            m_currentPage = 0;
        }
    }

    /**
     * Sets the maximum number of items per page.<p>
     *
     * @param maxItemsPerPage the maximum number of items per page to set
     */
    public void setMaxItemsPerPage(int maxItemsPerPage) {

        this.m_maxItemsPerPage = maxItemsPerPage;
    }

    /**
     * Sets the search filter.<p>
     *
     * @param searchFilter the search filter to set
     * @param locale the used locale for searching/sorting
     */
    public void setSearchFilter(String searchFilter, Locale locale) {

        if (!m_metadata.isSearchable()) {
            return;
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(searchFilter)) {
            // reset content if filter is empty
            m_filteredItems = null;
            m_searchFilter = "";
        } else {
            // TODO: I_CmsListSearchMethod
            m_filteredItems = new ArrayList();
            m_searchFilter = searchFilter;
            Iterator it = m_originalItems.iterator();
            String colName = m_metadata.getSearchAction().getColumnId();
            while (it.hasNext()) {
                CmsListItem item = (CmsListItem)it.next();
                if (item.get(colName).toString().indexOf(m_searchFilter) > -1) {
                    m_filteredItems.add(item);
                }
            }
        }
        String sCol = m_sortedColumn;
        m_sortedColumn = "";
        CmsListOrderEnum order = getCurrentSortOrder();
        setSortedColumn(sCol, locale);
        if (order == CmsListOrderEnum.ORDER_DESCENDING) {
            setSortedColumn(sCol, locale);
        }
        setCurrentPage(1);
    }

    /**
     * Sets the sorted column.<p>
     *
     * @param sortedColumn the sorted column to set
     * @param locale the used locale for sorting
     */
    public void setSortedColumn(String sortedColumn, Locale locale) {

        if (!m_metadata.isSorteable()) {
            return;
        }
        // check if the parameter is valid
        if (m_metadata.getColumnDefinition(sortedColumn) == null) {
            throw new IllegalArgumentException(Messages.get().key(Messages.ERR_LIST_INVALID_COLUMN_1, sortedColumn));
        }
        // reset view
        setCurrentPage(1);
        // only reverse order if the to sort column is already sorted
        if (sortedColumn.equals(m_sortedColumn)) {
            if (m_currentSortOrder == CmsListOrderEnum.ORDER_ASCENDING) {
                m_currentSortOrder = CmsListOrderEnum.ORDER_DESCENDING;
            } else {
                m_currentSortOrder = CmsListOrderEnum.ORDER_ASCENDING;
            }
            Collections.reverse(m_filteredItems);
            return;
        }
        // sort new column
        m_sortedColumn = sortedColumn;
        m_currentSortOrder = CmsListOrderEnum.ORDER_ASCENDING;
        Comparator c = getMetadata().getColumnDefinition(sortedColumn).getComparator();
        if (m_filteredItems == null) {
            m_filteredItems = new ArrayList(m_originalItems);
        }
        if (c == null) {
            Collections.sort(m_filteredItems, new CmsDefaultListItemComparator(m_sortedColumn, locale));
        } else {
            Collections.sort(m_filteredItems, c);
        }
    }

    /**
     * Returns the number (from 1) of the first displayed item.<p>
     * 
     * @return the number (from 1) of the first displayed item, or zero if the list is empty
     */
    private int displayedFrom() {

        if (getSize() != 0) {
            return (getCurrentPage() - 1) * getMaxItemsPerPage() + 1;
        }
        return 0;
    }

    /**
     * Returns the number (from 1) of the last displayed item.<p>
     * 
     * @return the number (from 1) of the last displayed item, or zero if the list is empty
     */
    private int displayedTo() {

        if (getSize() != 0) {
            if (getCurrentPage() * getMaxItemsPerPage() < getSize()) {
                return getCurrentPage() * getMaxItemsPerPage();
            }
        }
        return getSize();
    }

    /**
     * Generates the initial html code.<p>
     *
     * @param wp the workplace context
     *  
     * @return html code
     */
    private String htmlBegin(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(512);
        html.append("<div class='listArea'>\n");
        html.append(((CmsDialog)wp).dialogBlock(CmsWorkplace.HTML_START, m_name.key(wp.getLocale()), false));
        html.append("\t\t<table width='100%' cellspacing='0' cellpadding='0' border='0'>\n");
        html.append("\t\t\t<tr><td>\n");
        return html.toString();
    }

    /**
     * Generates the need html code for ending a lsit.<p>
     * 
     * @param wp the workplace context
     * 
     * @return html code
     */
    private String htmlEnd(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(512);
        html.append("\t\t\t</td></tr>\n");
        html.append("\t\t</table>\n");
        html.append(((CmsDialog)wp).dialogBlock(CmsWorkplace.HTML_END, m_name.key(wp.getLocale()), false));
        html.append("</div>\n");
        html.append("<script language='javascript'>\n");
        html.append("\tvar form = document.forms['");
        html.append(getId());
        html.append("-form'];\n");
        html.append("\tform.");
        html.append(getId());
        html.append("Filter.value='");
        html.append(getSearchFilter() == null ? "" : CmsStringUtil.escapeJavaScript(getSearchFilter()));
        html.append("';\n");
        html.append("</script>\n");
        return html.toString();
    }

    /**
     * Generates the needed html code for the paging bar.<p>
     * 
     * @param locale for message localization
     * 
     * @return html code
     */
    private String htmlPagingBar(Locale locale) {

        if (getNumberOfPages() < 2) {
            return "";
        }
        StringBuffer html = new StringBuffer(1024);
        html.append("<table width='100%' cellspacing='0' class='buttons'>\n");
        html.append("\t<tr>\n");
        html.append("\t\t<td class='main'>\n");
        // prev button
        String id = m_id + "PrevHelp";
        String name = Messages.get().key(locale, Messages.GUI_LIST_PAGING_PREVIOUS_NAME_0, null);
        String iconPath = Messages.get().key(locale, Messages.GUI_LIST_PAGING_PREVIOUS_ICON_0, null);
        String helpText = Messages.get().key(locale, Messages.GUI_LIST_PAGING_PREVIOUS_HELP_0, null);
        String onClic = m_id + "ListSetPage(" + (getCurrentPage() - 1) + ")";
        html.append(A_CmsHtmlIconButton.defaultButtonHtml(id, name, helpText, getCurrentPage() > 1, iconPath, onClic));
        html.append("\n");
        // next button
        id = m_id + "NextHelp";
        name = Messages.get().key(locale, Messages.GUI_LIST_PAGING_NEXT_NAME_0, null);
        iconPath = Messages.get().key(locale, Messages.GUI_LIST_PAGING_NEXT_ICON_0, null);
        helpText = Messages.get().key(locale, Messages.GUI_LIST_PAGING_NEXT_HELP_0, null);
        onClic = m_id + "ListSetPage(" + (getCurrentPage() + 1) + ")";
        html.append(A_CmsHtmlIconButton.defaultButtonHtml(
            id,
            name,
            helpText,
            getCurrentPage() < getNumberOfPages(),
            iconPath,
            onClic));
        html.append("\n");
        // page selection list
        html.append("\t\t\t<select name='");
        html.append(m_id);
        html.append("PageSet' id='id-page_set' onChange ='");
        html.append(m_id);
        html.append("ListSetPage(this.value);'>\n");
        for (int i = 0; i < getNumberOfPages(); i++) {
            int displayedFrom = i * getMaxItemsPerPage() + 1;
            int displayedTo = (i + 1) * getMaxItemsPerPage() < getSize() ? (i + 1) * getMaxItemsPerPage() : getSize();
            html.append("\t\t\t\t<option value='");
            html.append(i + 1);
            html.append("'");
            html.append((i + 1) == getCurrentPage() ? " selected" : "");
            html.append(">");
            html.append(displayedFrom);
            html.append(" - ");
            html.append(displayedTo);
            html.append("</option>\n");
        }
        html.append("\t\t\t</select>\n");
        html.append("\t\t\t&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        if (m_filteredItems == null) {
            html.append(Messages.get().key(
                locale,
                Messages.GUI_LIST_PAGING_TEXT_2,
                new Object[] {
                    m_name.key(locale),
                    new Integer(getTotalSize())}));
        } else {
            html.append(Messages.get().key(
                locale,
                Messages.GUI_LIST_PAGING_FILTER_TEXT_3,
                new Object[] {
                    m_name.key(locale),
                    new Integer(getSize()),
                    new Integer(getTotalSize())}));
        }
        html.append("\t\t</td>\n");
        html.append("\t</tr>\n");
        html.append("</table>\n");
        return html.toString();
    }

    /**
     * returns the html for the title of the list.<p>
     * 
     * @param wp the workplace context
     * 
     * @return html code
     */
    private String htmlTitle(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(512);
        html.append("<table width='100%' cellspacing='0' class='buttons'>");
        html.append("\t<tr>\n");
        html.append("\t\t<td>\n");
        html.append("\t\t\t");
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_searchFilter)) {
            html.append(Messages.get().key(
                wp.getLocale(),
                Messages.GUI_LIST_TITLE_TEXT_4,
                new Object[] {
                    m_name.key(wp.getLocale()),
                    new Integer(displayedFrom()),
                    new Integer(displayedTo()),
                    new Integer(getTotalSize())}));
        } else {
            html.append(Messages.get().key(
                wp.getLocale(),
                Messages.GUI_LIST_TITLE_FILTERED_TEXT_5,
                new Object[] {
                    m_name.key(wp.getLocale()),
                    new Integer(displayedFrom()),
                    new Integer(displayedTo()),
                    new Integer(getSize()),
                    new Integer(getTotalSize())}));
        }
        html.append("\n");
        html.append("\t\t</td>\n\t\t");
        html.append(getMetadata().htmlActionBar(wp));
        html.append("\n\t</tr>\n");
        html.append("</table>\n");
        return html.toString();
    }

    /**
     * Returns the html code for the toolbar (search bar + multiactions bar).<p>
     * 
     * @param wp the workplace context
     * 
     * @return html code
     */
    private String htmlToolBar(CmsWorkplace wp) {

        StringBuffer html = new StringBuffer(512);
        html.append("<table width='100%' cellspacing='0' class='buttons'>\n");
        html.append("\t<tr>\n");
        html.append(m_metadata.htmlSearchBar(getId(), wp));
        html.append(m_metadata.htmlMultiActionBar(wp));
        html.append("\t</tr>\n");
        html.append("</table>\n");
        return html.toString();
    }

}
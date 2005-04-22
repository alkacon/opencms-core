/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/Attic/CmsListDialog.java,v $
 * Date   : $Date: 2005/04/22 14:44:11 $
 * Version: $Revision: 1.2 $
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides a dialog with a list widget.<p> 
 *
 * @author  Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.2 $
 * @since 5.7.3
 */
public abstract class CmsListDialog extends CmsDialog {

    /** Request parameter key for the column to sort the list. */
    public static final String PARAM_SORT_COL = "sortcol";

    /** Request parameter key for search the filter. */
    public static final String PARAM_SEARCH_FILTER = "searchfilter";

    /** Request parameter key for the selected item(s). */
    public static final String PARAM_SEL_ITEMS = "selitems";

    /** Request parameter key for the requested page. */
    public static final String PARAM_PAGE = "page";

    /** Request parameter value for the list action: select a page. */
    public static final String LIST_SELECT_PAGE = "listselectpage";

    /** Request parameter value for the list action: search/filter. */
    public static final String LIST_SEARCH = "listsearch";

    /** Request parameter value for the list action: sort. */
    public static final String LIST_SORT = "listsort";

    /** Request parameter value for the list action: a single action has been triggered. */
    public static final String LIST_SINGLE_ACTION = "listsingleaction";

    /** Request parameter value for the list action: a list item independent action has been triggered. */
    public static final String LIST_INDEPENDENT_ACTION = "listindependentaction";

    /** Request parameter value for the list action: a multi action has been triggered. */
    public static final String LIST_MULTI_ACTION = "listmultiaction";

    /** Request parameter key for the list action. */
    public static final String PARAM_LIST_ACTION = "listaction";

    /** Value for the action: sort the list. */
    public static final int ACTION_LIST_SORT = 80;

    /** Value for the action: search the list. */
    public static final int ACTION_LIST_SEARCH = 81;

    /** Value for the action: go to a page. */
    public static final int ACTION_LIST_SELECT_PAGE = 82;

    /** Value for the action: execute a list item independent action of the list. */
    public static final int ACTION_LIST_INDEPENDENT_ACTION = 83;

    /** Value for the action: execute a single action of the list. */
    public static final int ACTION_LIST_SINGLE_ACTION = 84;

    /** Value for the action: execute an multi action of the list. */
    public static final int ACTION_LIST_MULTI_ACTION = 85;

    /** The column to sort the list. */
    private String m_paramSortCol;

    /** The search filter text. */
    private String m_paramSearchFilter;

    /** The selected items, comma separated list. */
    private String m_paramSelItems;

    /** The displayed page. */
    private String m_paramPage;

    /** The list action. */
    private String m_paramListAction;

    /** the internal list. */
    private CmsHtmlList m_list;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param sortedColId the a priory sorted column
     */
    protected CmsListDialog(CmsJspActionElement jsp, String sortedColId) {

        super(jsp);
        // try to read the list from the session
        listRecovery();
        // initialization 
        if (getList() == null) {
            // create the list
            setList(createList());
            // set the number of items per page from the user settings
            getList().setMaxItemsPerPage(getSettings().getUserSettings().getExplorerFileEntries());
            // fill the content
            getList().addAllItems(getListItems());
            // sort the list
            getList().setSortedColumn(sortedColId, getLocale());
            // save the current state of the list
            listSave();
        }
    }

    /**
     * Should generate the metadata definition of the list, and return a 
     * new list object associated to it.<p>
     * 
     * @return The list to display in this dialog
     */
    protected abstract CmsHtmlList createList();

    /**
     * Fill the list with data.<p>
     * 
     * @return a list of <code>{@link CmsListItem}</code>s.
     */
    protected abstract List getListItems();

    /**
     * Recover the last list instance that is read from the request attributes.<p>
     * 
     * This is required for keep the whole list in memory while you browse a page.<p>
     */
    public void listRecovery() {

        CmsHtmlList list = null;
        list = getSettings().getList();
        setList(list);
    }

    /**
     * Save the state of the list in the session.<p>
     */
    protected void listSave() {

        getSettings().setList(getList());
    }

    /**
     * This method re-read the rows of the list, the user should call this method after executing an action
     * that added or removed rows to the list. 
     */
    public void refreshList() {

        String sCol = getList().getSortedColumn();
        String sFilter = getList().getSearchFilter();
        int cPage = getList().getCurrentPage();
        CmsListOrderEnum order = getList().getCurrentSortOrder();
        getList().clear(getLocale());
        getList().addAllItems(getListItems());
        getList().setSearchFilter(sFilter, getLocale());
        getList().setSortedColumn(sCol, getLocale());
        if (order == CmsListOrderEnum.DescendingOrder) {
            getList().setSortedColumn(sCol, getLocale());
        }
        if (cPage>0 && cPage<=getList().getNumberOfPages()) {
            getList().setCurrentPage(cPage);
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the action for the JSP switch 
        if (LIST_SEARCH.equals(getParamAction())) {
            setAction(ACTION_LIST_SEARCH);
        } else if (LIST_SORT.equals(getParamAction())) {
            setAction(ACTION_LIST_SORT);
        } else if (LIST_SELECT_PAGE.equals(getParamAction())) {
            setAction(ACTION_LIST_SELECT_PAGE);
        } else if (LIST_INDEPENDENT_ACTION.equals(getParamAction())) {
            setAction(ACTION_LIST_INDEPENDENT_ACTION);
        } else if (LIST_SINGLE_ACTION.equals(getParamAction())) {
            setAction(ACTION_LIST_SINGLE_ACTION);
        } else if (LIST_MULTI_ACTION.equals(getParamAction())) {
            setAction(ACTION_LIST_MULTI_ACTION);
        }
    }

    /**
     * This method execute the default actions for searching, sorting and paging.<p>
     */
    public void executeDefaultActions() {

        switch (getAction()) {

            case ACTION_LIST_SEARCH:
                executeSearch();
                break;
            case ACTION_LIST_SORT:
                executeSort();
                break;
            case ACTION_LIST_SELECT_PAGE:
                executeSelectPage();
                break;
            default:
        //noop
        }
        listSave();
    }

    /**
     * Select a page, given the action is set to <code>LIST_SELECT_PAGE</code> and 
     * the page to go to is set in the <code>PARAM_PAGE</code> parameter.<p>
     */
    protected void executeSelectPage() {

        int page = Integer.valueOf(getParamPage()).intValue();
        m_list.setCurrentPage(page);
    }

    /**
     * Filter a list, given the action is set to <code>LIST_SEARCH</code> and
     * the filter text is set in the <code>PARAM_SEARCH_FILTER</code> parameter.<p>
     */
    protected void executeSearch() {

        m_list.setSearchFilter(getParamSearchFilter(), getLocale());
    }

    /**
     * Sort the list, given the action is set to <code>LIST_SORT</code> and
     * the sort column is set in the <code>PARAM_SORT_COL</code> parameter.<p>
     */
    protected void executeSort() {

        m_list.setSortedColumn(getParamSortCol(), getLocale());
    }

    /**
     * Returns the current selected item.<p>
     * 
     * @return the current selected item
     */
    public CmsListItem getSelectedItem() {

        return m_list.getItem(getParamSelItems());
    }

    /**
     * Returns a list of current selected items.<p>
     * 
     * @return a list of current selected items
     */
    public List getSelectedItems() {

        Iterator it = CmsStringUtil.splitAsList(getParamSelItems(), CmsHtmlList.C_ITEM_SEPARATOR, true).iterator();
        List items = new ArrayList();
        while (it.hasNext()) {
            String id = (String)it.next();
            items.add(m_list.getItem(id));
        }
        return items;
    }

    /**
     * Returns the list.<p>
     *
     * @return the list
     */
    public CmsHtmlList getList() {

        return m_list;
    }

    /**
     * Sets the list.<p>
     *
     * @param list the list to set
     */
    public void setList(CmsHtmlList list) {

        m_list = list;
    }

    /**
     * Returns the List Action.<p>
     *
     * @return the List Action
     */
    public String getParamListAction() {

        return m_paramListAction;
    }

    /**
     * Sets the List Action.<p>
     *
     * @param listAction the list Action to set
     */
    public void setParamListAction(String listAction) {

        m_paramListAction = listAction;
    }

    /**
     * Returns the current Page.<p>
     *
     * @return the current Page
     */
    public String getParamPage() {

        return m_paramPage;
    }

    /**
     * Sets the current Page.<p>
     *
     * @param page the current Page to set
     */
    public void setParamPage(String page) {

        m_paramPage = page;
    }

    /**
     * Returns the Search Filter.<p>
     *
     * @return the Search Filter
     */
    public String getParamSearchFilter() {

        return m_paramSearchFilter;
    }

    /**
     * Sets the Search Filter.<p>
     *
     * @param searchFilter the Search Filter to set
     */
    public void setParamSearchFilter(String searchFilter) {

        m_paramSearchFilter = searchFilter;
    }

    /**
     * Returns the Selected Items.<p>
     *
     * @return the Selelected Items
     */
    public String getParamSelItems() {

        return m_paramSelItems;
    }

    /**
     * Sets the Selelected Items.<p>
     *
     * @param paramSelItems the Selelected Items to set
     */
    public void setParamSelItems(String paramSelItems) {

        m_paramSelItems = paramSelItems;
    }

    /**
     * Returns the sorted Column.<p>
     *
     * @return the sorted Column
     */
    public String getParamSortCol() {

        return m_paramSortCol;
    }

    /**
     * Sets the sorted Column.<p>
     *
     * @param sortCol the sorted Column to set
     */
    public void setParamSortCol(String sortCol) {

        m_paramSortCol = sortCol;
    }
    
    /**
     * A convenient method to throw a list unsupported
     * action runtime exception.<p>
     * 
     * Should be triggered if your list implementation does not 
     * support the <code>{@link #getParamListAction()}</code>
     * action.<p>
     */
    protected void throwListUnsupportedActionException() {
        throw new RuntimeException(Messages.get().key(
            Messages.ERR_LIST_UNSUPPORTED_ACTION_2,
            getList().getName(),
            getParamListAction()));
    }
}
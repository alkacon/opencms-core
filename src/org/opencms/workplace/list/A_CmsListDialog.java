/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/A_CmsListDialog.java,v $
 * Date   : $Date: 2005/05/17 09:52:54 $
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
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides a dialog with a list widget.<p> 
 *
 * @author  Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public abstract class A_CmsListDialog extends CmsDialog {

    /** Value for the action: execute a list item independent action of the list. */
    public static final int ACTION_LIST_INDEPENDENT_ACTION = 83;

    /** Value for the action: execute an multi action of the list. */
    public static final int ACTION_LIST_MULTI_ACTION = 85;

    /** Value for the action: search the list. */
    public static final int ACTION_LIST_SEARCH = 81;

    /** Value for the action: go to a page. */
    public static final int ACTION_LIST_SELECT_PAGE = 82;

    /** Value for the action: execute a single action of the list. */
    public static final int ACTION_LIST_SINGLE_ACTION = 84;

    /** Value for the action: sort the list. */
    public static final int ACTION_LIST_SORT = 80;

    /** Request parameter value for the list action: a list item independent action has been triggered. */
    public static final String LIST_INDEPENDENT_ACTION = "listindependentaction";

    /** Request parameter value for the list action: a multi action has been triggered. */
    public static final String LIST_MULTI_ACTION = "listmultiaction";

    /** Request parameter value for the list action: search/filter. */
    public static final String LIST_SEARCH = "listsearch";

    /** Request parameter value for the list action: select a page. */
    public static final String LIST_SELECT_PAGE = "listselectpage";

    /** Request parameter value for the list action: a single action has been triggered. */
    public static final String LIST_SINGLE_ACTION = "listsingleaction";

    /** Request parameter value for the list action: sort. */
    public static final String LIST_SORT = "listsort";

    /** Request parameter key for the list action. */
    public static final String PARAM_LIST_ACTION = "listaction";

    /** Request parameter key for the requested page. */
    public static final String PARAM_PAGE = "page";

    /** Request parameter key for search the filter. */
    public static final String PARAM_SEARCH_FILTER = "searchfilter";

    /** Request parameter key for the selected item(s). */
    public static final String PARAM_SEL_ITEMS = "selitems";

    /** Request parameter key for the column to sort the list. */
    public static final String PARAM_SORT_COL = "sortcol";

    /** metadata map for all used list metadata objects. */
    private static Map m_metadatas = new HashMap();

    /** the internal list. */
    private CmsHtmlList m_list;

    /** The list action. */
    private String m_paramListAction;

    /** The displayed page. */
    private String m_paramPage;

    /** The search filter text. */
    private String m_paramSearchFilter;

    /** The selected items, comma separated list. */
    private String m_paramSelItems;

    /** The column to sort the list. */
    private String m_paramSortCol;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     * @param listId the id of the displayed list
     * @param listName the name of the list
     * @param sortedColId the a priory sorted column
     * @param searchableColId the column to search into
     */
    protected A_CmsListDialog(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        String sortedColId,
        String searchableColId) {

        super(jsp);
        // try to read the list from the session
        listRecovery(listId);
        // initialization 
        if (getList() == null) {
            // create the list
            setList(new CmsHtmlList(listId, listName, getMetadata(listId)));
            if (searchableColId != null && getList().getMetadata().getColumnDefinition(searchableColId) != null) {
                setSearchAction(listId, getList().getMetadata().getColumnDefinition(searchableColId));
            }
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
     * This method should handle the default list independent actions,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     * 
     * if you want to handle additional independent actions, override this method,
     * handling your actions and FINALLY calling <code>super.executeListIndepActions();</code>.<p> 
     */
    public void executeListIndepActions() {

        Iterator itIndepActions = getList().getMetadata().getIndependentActions().iterator();
        while (itIndepActions.hasNext()) {
            I_CmsListAction action = (I_CmsListAction)itIndepActions.next();
            if (action.getId().equals(CmsListIndependentAction.LIST_ACTION_REFRESH)) {
                if (getParamListAction().equals(CmsListIndependentAction.LIST_ACTION_REFRESH)) {
                    refreshList();
                }
                break;
            }
        }
        // toogle item details
        if (getList().getMetadata().getItemDetailDefinition(getParamListAction()) != null) {
            getList().getMetadata().toogleDetailState(getParamListAction());
        }
        listSave();
    }

    /**
     * Returns the list.<p>
     *
     * @return the list
     */
    public CmsHtmlList getList() {

        if (m_list != null && m_list.getMetadata() == null) {
            m_list.setMetadata((CmsListMetadata)m_metadatas.get(m_list.getId()));
        }
        return m_list;
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
     * Returns the current Page.<p>
     *
     * @return the current Page
     */
    public String getParamPage() {

        return m_paramPage;
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
     * Returns the Selected Items.<p>
     *
     * @return the Selelected Items
     */
    public String getParamSelItems() {

        return m_paramSelItems;
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
        if (order == CmsListOrderEnum.ORDER_DESCENDING) {
            getList().setSortedColumn(sCol, getLocale());
        }
        if (cPage > 0 && cPage <= getList().getNumberOfPages()) {
            getList().setCurrentPage(cPage);
        }
        listSave();
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
     * Sets the List Action.<p>
     *
     * @param listAction the list Action to set
     */
    public void setParamListAction(String listAction) {

        m_paramListAction = listAction;
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
     * Sets the Search Filter.<p>
     *
     * @param searchFilter the Search Filter to set
     */
    public void setParamSearchFilter(String searchFilter) {

        m_paramSearchFilter = searchFilter;
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
     * Sets the sorted Column.<p>
     *
     * @param sortCol the sorted Column to set
     */
    public void setParamSortCol(String sortCol) {

        m_paramSortCol = sortCol;
    }

    /**
     * Filter a list, given the action is set to <code>LIST_SEARCH</code> and
     * the filter text is set in the <code>PARAM_SEARCH_FILTER</code> parameter.<p>
     */
    protected void executeSearch() {

        m_list.setSearchFilter(getParamSearchFilter(), getLocale());
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
     * Sort the list, given the action is set to <code>LIST_SORT</code> and
     * the sort column is set in the <code>PARAM_SORT_COL</code> parameter.<p>
     */
    protected void executeSort() {

        m_list.setSortedColumn(getParamSortCol(), getLocale());
    }

    /**
     * Fill the list with data.<p>
     * 
     * @return a list of <code>{@link CmsListItem}</code>s.
     */
    protected abstract List getListItems();

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
     * Recover the last list instance that is read from the request attributes.<p>
     * 
     * This is required for keep the whole list in memory while you browse a page.<p>
     * 
     * @param listId the id of the expected list
     */
    protected synchronized void listRecovery(String listId) {

        CmsHtmlList list = null;
        list = getSettings().getHtmlList();
        if (list != null && !list.getId().equals(listId)) {
            list = null;
        }
        if (list != null) {
            list.setMetadata(getMetadata(listId));
        }
        setList(list);
    }

    /**
     * Save the state of the list in the session.<p>
     */
    protected synchronized void listSave() {

        CmsHtmlList list = getList();
        list.setMetadata(null);
        getSettings().setHtmlList(list);
    }

    /**
     * Should create the columns and add them to the given list metadata object.<p>
     * 
     * This method will be just executed once, the first time the constructor is called.<p> 
     * 
     * @param metadata the list metadata
     */
    protected abstract void setColumns(CmsListMetadata metadata);

    /**
     * Should add the independent actions to the given list metadata object.<p>
     * 
     * This method will be just executed once, the first time the constructor is called.<p> 
     * 
     * @param metadata the list metadata
     */
    protected abstract void setIndependentActions(CmsListMetadata metadata);

    /**
     * Should add the multi actions to the given list metadata object.<p>
     * 
     * This method will be just executed once, the first time the constructor is called.<p> 
     * 
     * @param metadata the list metadata
     */
    protected abstract void setMultiActions(CmsListMetadata metadata);

    /**
     * Creates the default search action.<p>
     * 
     * Can be overriden for more sofisticated search.<p>
     * 
     * @param listId the id of the list
     * @param columnDefinition the column to search into
     */
    protected void setSearchAction(String listId, CmsListColumnDefinition columnDefinition) {

        // makes the list searchable by login
        CmsListSearchAction searchAction = new CmsListSearchAction(listId, columnDefinition);
        searchAction.useDefaultShowAllAction();
        ((CmsListMetadata)m_metadatas.get(listId)).setSearchAction(searchAction);
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

    /**
     * Should generate the metadata definition for the list, and return the 
     * corresponding <code>{@link CmsListMetadata}</code> object.<p>
     * 
     * @return The metadata for the given list
     */
    private synchronized CmsListMetadata getMetadata(String listId) {

        if (m_metadatas.get(listId) == null) {
            CmsListMetadata metadata = new CmsListMetadata();

            setIndependentActions(metadata);
            setColumns(metadata);
            setMultiActions(metadata);
            m_metadatas.put(listId, metadata);
        }
        return (CmsListMetadata)m_metadatas.get(listId);
    }
}
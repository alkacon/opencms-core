/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/A_CmsListDialog.java,v $
 * Date   : $Date: 2005/06/23 10:11:48 $
 * Version: $Revision: 1.26 $
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
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * Provides a dialog with a list widget.<p> 
 *
 * @author  Michael Moossen 
 * 
 * @version $Revision: 1.26 $ 
 * 
 * @since 6.0.0 
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

    /** Standard list button location. */
    public static final String ICON_ACTIVE = "list/active.png";

    /** Standard list button location. */
    public static final String ICON_ADD = "list/add.png";

    /** Standard list button location. */
    public static final String ICON_DELETE = "list/delete.png";

    /** Standard list button location. */
    public static final String ICON_DETAILS_HIDE = "list/details_hide.png";

    /** Standard list button location. */
    public static final String ICON_DETAILS_SHOW = "list/details_show.png";

    /** Standard list button location. */
    public static final String ICON_DISABLED = "list/disabled.png";

    /** Standard list button location. */
    public static final String ICON_INACTIVE = "list/inactive.png";

    /** Standard list button location. */
    public static final String ICON_MINUS = "list/minus.png";

    /** Standard list button location. */
    public static final String ICON_MULTI_ACTIVATE = "list/multi_activate.png";

    /** Standard list button location. */
    public static final String ICON_MULTI_ADD = "list/multi_add.png";

    /** Standard list button location. */
    public static final String ICON_MULTI_DEACTIVATE = "list/multi_deactivate.png";

    /** Standard list button location. */
    public static final String ICON_MULTI_DELETE = "list/multi_delete.png";

    /** Standard list button location. */
    public static final String ICON_MULTI_MINUS = "list/multi_minus.png";

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

    /** Request parameter key for the requested page. */
    public static final String PARAM_FORMNAME = "formname";

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

    /** Activation decision Flag. */
    private boolean m_active;

    /** the internal list. */
    private CmsHtmlList m_list;

    /** The id of the list. */
    private String m_listId;

    /** The displayed page. */
    private String m_paramFormName;

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
     * @param jsp an initialized JSP action element
     * @param listId the id of the displayed list
     * @param listName the name of the list
     * @param sortedColId the a priory sorted column
     * @param sortOrder the order of the sorted column
     * @param searchableColId the column to search into
     */
    protected A_CmsListDialog(
        CmsJspActionElement jsp,
        String listId,
        CmsMessageContainer listName,
        String sortedColId,
        CmsListOrderEnum sortOrder,
        String searchableColId) {

        super(jsp);
        m_listId = listId;
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
            // sort the list
            getList().setSortedColumn(sortedColId, getLocale());
            if (sortOrder != null && sortOrder == CmsListOrderEnum.ORDER_DESCENDING) {
                getList().setSortedColumn(sortedColId, getLocale());
            }
            // save the current state of the list
            listSave();
        }
    }

    /**
     * Returns the list object for the given list dialog, or <code>null</code>
     * if no list object has been set.<p>
     * 
     * @param listDialog the list dialog class
     * @param settings the wp settings for accessing the session
     * 
     * @return the list object for this list dialog, or <code>null</code>
     */
    public static CmsHtmlList getListObject(Class listDialog, CmsWorkplaceSettings settings) {

        return (CmsHtmlList)getListObjectMap(settings).get(listDialog.getName());
    }

    /**
     * Returns the (internal use only) map of list objects.<p>
     * 
     * @param settings the wp settings for accessing the session 
     * 
     * @return the (internal use only) map of list objects 
     */
    private static Map getListObjectMap(CmsWorkplaceSettings settings) {

        Map objects = (Map)settings.getListObject();
        if (objects == null) {
            // using hashtable as most efficient version of a synchronized map
            objects = new Hashtable();
            settings.setListObject(objects);
        }
        return objects;
    }

    /**
     * Performs the dialog actions depending on the initialized action.<p>
     * 
     * @throws JspException if dialog actions fail
     * @throws IOException in case of errros forwarding to the required result page
     * @throws ServletException in case of errros forwarding to the required result page
     */
    public void actionDialog() throws JspException, ServletException, IOException {

        if (getAction() == ACTION_CANCEL) {
            // ACTION: cancel button pressed
            actionCloseDialog();
            return;
        }
        refreshList();
        switch (getAction()) {

            case ACTION_CANCEL:
                // ACTION: cancel button pressed
                actionCloseDialog();
                return;

            //////////////////// ACTION: default actions
            case ACTION_LIST_SEARCH:
            case ACTION_LIST_SORT:
            case ACTION_LIST_SELECT_PAGE:
                executeDefaultActions();
                break;

            //////////////////// ACTION: execute single list action
            case ACTION_LIST_SINGLE_ACTION:
                executeListSingleActions();
                break;

            //////////////////// ACTION: execute multiple list actions
            case ACTION_LIST_MULTI_ACTION:
                executeListMultiActions();
                break;

            //////////////////// ACTION: execute independent list actions
            case ACTION_LIST_INDEPENDENT_ACTION:
                executeListIndepActions();
                break;

            case ACTION_DEFAULT:
            default:
                // ACTION: show dialog (default)
                setParamAction(DIALOG_INITIAL);
        }
    }

    /**
     * Generates the dialog starting html code.<p>
     * 
     * @return html code
     */
    public String defaultActionHtml() {

        if (getList() != null && getList().getAllContent().isEmpty()) {
            refreshList();
        }
        StringBuffer result = new StringBuffer(2048);
        result.append(defaultActionHtmlStart());
        result.append(defaultActionHtmlContent());
        result.append(defaultActionHtmlEnd());
        return result.toString();
    }

    /**
     * Returns the html code for the default action content.<p>
     * 
     * @return html code
     */
    public String defaultActionHtmlContent() {

        StringBuffer result = new StringBuffer(2048);
        result.append(dialogContentStart(getParamTitle()));
        result.append("<form name='");
        result.append(getList().getId());
        result.append("-form' action='");
        result.append(getDialogUri());
        result.append("' method='post' class='nomargin'");
        if (getMetadata(getListId()).isSearchable()) {
            result.append(" onsubmit=\"listSearchAction('");
            result.append(getListId());
            result.append("', '");
            result.append(getMetadata(getListId()).getSearchAction().getId());
            result.append("', '");
            result.append(getMetadata(getListId()).getSearchAction().getConfirmationMessage().key(getLocale()));
            result.append("');\"");
        }
        result.append(">\n");
        result.append(allParamsAsHidden());
        result.append("\n");
        result.append(getList().listHtml(this));
        result.append("\n</form>\n");
        result.append(dialogContentEnd());
        return result.toString();
    }

    /**
     * Generates the dialog ending html code.<p>
     * 
     * @return html code
     */
    public String defaultActionHtmlEnd() {

        StringBuffer result = new StringBuffer(2048);
        result.append(dialogEnd());
        result.append(bodyEnd());
        result.append(htmlEnd());
        return result.toString();
    }

    /**
     * Generates the dialog starting html code.<p>
     * 
     * @return html code
     */
    public String defaultActionHtmlStart() {

        StringBuffer result = new StringBuffer(2048);
        result.append(htmlStart(null));
        result.append(getList().listJs(getLocale()));
        result.append(bodyStart("dialog", null));
        result.append(dialogStart());
        return result.toString();
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     * 
     * @throws JspException if dialog actions fail
     * @throws IOException if writing to the JSP out fails, or in case of errros forwarding to the required result page
     * @throws ServletException in case of errros forwarding to the required result page
     */
    public void displayDialog() throws JspException, IOException, ServletException {

        actionDialog();
        if (getJsp().getResponse().isCommitted()) {
            return;
        }
        JspWriter out = getJsp().getJspContext().getOut();
        out.print(defaultActionHtml());
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

        if (getList().getMetadata().getItemDetailDefinition(getParamListAction()) != null) {
            // toogle item details
            getList().getMetadata().toogleDetailState(getParamListAction());
            // lazzy initialization
            initializeDetail(getParamListAction());
        }
        listSave();
    }

    /**
     * This method should handle every defined list multi action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     *
     * @throws IOException in case of errors when including a required sub-element
     * @throws ServletException in case of errors when including a required sub-element
     * @throws CmsRuntimeException to signal that an action is not supported
     */
    public abstract void executeListMultiActions() throws IOException, ServletException, CmsRuntimeException;

    /**
     * This method should handle every defined list single action,
     * by comparing <code>{@link #getParamListAction()}</code> with the id 
     * of the action to execute.<p> 
     * 
     * @throws IOException in case of errors when including a required sub-element
     * @throws ServletException in case of errors when including a required sub-element
     * @throws CmsRuntimeException to signal that an action is not supported
     */
    public abstract void executeListSingleActions() throws IOException, ServletException, CmsRuntimeException;

    /**
     * Returns the list.<p>
     *
     * @return the list
     */
    public CmsHtmlList getList() {

        if (m_list != null && m_list.getMetadata() == null) {
            m_list.setMetadata(getMetadata(m_list.getId()));
        }
        return m_list;
    }

    /**
     * Returns the Id of the list.<p>
     *
     * @return the list Id
     */
    public String getListId() {

        return m_listId;
    }

    /**
     * Returns the form name.<p>
     *
     * @return the form name
     */
    public String getParamFormName() {

        return m_paramFormName;
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

        return getList().getItem(getParamSelItems());
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
            items.add(getList().getItem(id));
        }
        return items;
    }

    /**
     * Returns the activation flag.<p>
     *
     * Useful for dialogs with several lists.<p>
     * 
     * Is <code></code> if the original <code>formname</code> parameter 
     * is equals to <code>${listId}-form</code>.<p>
     * 
     * @return the activation flag
     */
    public boolean isActive() {

        return m_active;
    }

    /**
     * This method re-read the rows of the list, the user should call this method after executing an action
     * that add or remove rows to the list. 
     */
    public void refreshList() {

        if (getList() == null) {
            return;
        }
        CmsListState ls = getList().getState();
        getList().clear(getLocale());
        fillList();
        getList().setState(ls, getLocale());
        listSave();
    }

    /**
     * Removes the list from the workplace settings.<p>
     * 
     * Next time the list is displayed the list will be reloaded.<p>
     */
    public void removeList() {

        setList(null);
        listSave();
    }

    /**
     * Sets the activation flag.<p>
     *
     * @param active the activation flag to set
     */
    public void setActive(boolean active) {

        m_active = active;
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
     * Stores the given object as "list object" for the given list dialog in the current users session.<p> 
     * 
     * @param listDialog the list dialog class
     * @param listObject the list to store
     */
    public void setListObject(Class listDialog, CmsHtmlList listObject) {

        if (listObject == null) {
            // null object: remove the entry from the map
            getListObjectMap(getSettings()).remove(listDialog.getName());
        } else {
            listObject.setMetadata(null);
            getListObjectMap(getSettings()).put(listDialog.getName(), listObject);
        }
    }

    /**
     * Sets the form name.<p>
     *
     * @param formName the form name to set
     */
    public void setParamFormName(String formName) {

        m_paramFormName = formName;
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

        getList().setSearchFilter(getParamSearchFilter(), getLocale());
    }

    /**
     * Select a page, given the action is set to <code>LIST_SELECT_PAGE</code> and 
     * the page to go to is set in the <code>PARAM_PAGE</code> parameter.<p>
     */
    protected void executeSelectPage() {

        int page = Integer.valueOf(getParamPage()).intValue();
        getList().setCurrentPage(page);
    }

    /**
     * Sort the list, given the action is set to <code>LIST_SORT</code> and
     * the sort column is set in the <code>PARAM_SORT_COL</code> parameter.<p>
     */
    protected void executeSort() {

        getList().setSortedColumn(getParamSortCol(), getLocale());
    }

    /**
     * Lazzy initialization for detail data.<p>
     * 
     * Should fill the given detail column for every list item in <code>{@link CmsHtmlList#getAllContent()}</code>
     * 
     * Should not throw any kind of exception.<p>
     * 
     * @param detailId the id of the detail to initialize
     */
    protected abstract void fillDetails(String detailId);

    /**
     * Should generate a list with the list items to be displayed.<p>
     * 
     * @return a list of <code>{@link CmsListItem}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    protected abstract List getListItems() throws CmsException;

    /**
     * Should generate the metadata definition for the list, and return the 
     * corresponding <code>{@link CmsListMetadata}</code> object.<p>
     * 
     * @param listId the id of the list
     * 
     * @return The metadata for the given list
     */
    protected synchronized CmsListMetadata getMetadata(String listId) {

        if (m_metadatas.get(listId) == null) {
            CmsListMetadata metadata = new CmsListMetadata(listId);

            setColumns(metadata);
            setIndependentActions(metadata);
            setMultiActions(metadata);
            metadata.checkIds();
            m_metadatas.put(listId, metadata);
        }
        return (CmsListMetadata)m_metadatas.get(listId);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        addMessages(Messages.get().getBundleName());
        addMessages(org.opencms.workplace.tools.Messages.get().getBundleName());
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
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

        // test the needed parameters
        try {
            validateParamaters();
        } catch (Exception e) {
            // redirect to parent if parameters not available
            setAction(ACTION_CANCEL);
            try {
                actionCloseDialog();
            } catch (JspException e1) {
                // noop
            }
            return;
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

        CmsHtmlList list = getListObject(this.getClass(), getSettings());
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

        setListObject(this.getClass(), getList());
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

        if (((CmsListMetadata)m_metadatas.get(listId)).getSearchAction() == null) {
            // makes the list searchable
            CmsListSearchAction searchAction = new CmsListSearchAction(columnDefinition);
            searchAction.useDefaultShowAllAction();
            ((CmsListMetadata)m_metadatas.get(listId)).setSearchAction(searchAction);
        }
    }

    /**
     * A convenient method to throw a list unsupported
     * action runtime exception.<p>
     * 
     * Should be triggered if your list implementation does not 
     * support the <code>{@link #getParamListAction()}</code>
     * action.<p>
     * 
     * @throws CmsRuntimeException always to signal that this operation is not supported
     */
    protected void throwListUnsupportedActionException() throws CmsRuntimeException {

        throw new CmsRuntimeException(Messages.get().container(
            Messages.ERR_LIST_UNSUPPORTED_ACTION_2,
            getList().getName().key(getLocale()),
            getParamListAction()));
    }

    /**
     * Should be overriden for parameter validation.<p>
     * 
     * @throws Exception if the parameters are not valid
     */
    protected void validateParamaters() throws Exception {

        // valid by default
    }

    /**
     * Calls the <code>{@link getListItems}</code> method and catches any exception.<p>
     */
    private void fillList() {

        try {
            getList().addAllItems(getListItems());
            // initialize detail columns
            Iterator itDetails = getList().getMetadata().getListDetails().iterator();
            while (itDetails.hasNext()) {
                initializeDetail(((CmsListItemDetails)itDetails.next()).getId());
            }
        } catch (Exception e) {
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_LIST_FILL_1,
                getList().getName().key(getLocale()),
                null), e);
        }
    }

    /**
     * Lazzy details initialization.<p>
     * 
     * @param detailId the id of the detail column
     */
    private void initializeDetail(String detailId) {

        // if detail column not visible
        if (getList().getMetadata().getItemDetailDefinition(detailId).isVisible()) {
            // if the list is not empty
            if (!getList().getAllContent().isEmpty()) {
                // if the detail column has not been previously initialized
                if (((CmsListItem)getList().getAllContent().get(0)).get(detailId) == null) {
                    fillDetails(detailId);
                }
            }
        }
    }
}
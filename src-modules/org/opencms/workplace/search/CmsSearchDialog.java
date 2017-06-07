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

package org.opencms.workplace.search;

import org.opencms.db.CmsUserSettings.CmsSearchResultStyle;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsMultiSelectWidget;
import org.opencms.widgets.CmsSelectOnChangeReloadWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.tools.CmsToolDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a GUI for the workplace search feature.<p>
 *
 * @since 6.2.0
 */
public class CmsSearchDialog extends CmsWidgetDialog {

    /** Localization key label infix for fields. */
    public static final String LABEL_FIELD_INFIX = "field.";

    /** the dialog type. */
    private static final String DIALOG_TYPE = "search";

    /** Defines which pages are valid for this dialog. */
    private static final String[] PAGES = {"page1"};

    /** the search data. */
    private CmsSearchWorkplaceBean m_search;

    /**
     * Default constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSearchDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSearchDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        List<Throwable> errors = new ArrayList<Throwable>();
        try {
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(CmsDialog.PARAM_ACTION, new String[] {A_CmsListDialog.LIST_SELECT_PAGE});
            params.put(A_CmsListDialog.PARAM_PAGE, new String[] {"1"});
            params.put(CmsToolDialog.PARAM_ROOT, new String[] {"explorer"});
            if (getSettings().getUserSettings().getWorkplaceSearchViewStyle() == CmsSearchResultStyle.STYLE_EXPLORER) {
                getSettings().setExplorerPage(1);
                params.put(A_CmsListExplorerDialog.PARAM_SHOW_EXPLORER, new String[] {Boolean.TRUE.toString()});
            } else {
                CmsSearchResultsList resultsList = new CmsSearchResultsList(getJsp());
                CmsListMetadata metadata = resultsList.getMetadata(
                    CmsSearchResultsList.class.getName(),
                    resultsList.getListId());
                boolean withExcerpts = (getSettings().getUserSettings().getWorkplaceSearchViewStyle() == CmsSearchResultStyle.STYLE_LIST_WITH_EXCERPTS);
                if (metadata == null) {
                    if (!withExcerpts) {
                        // prevent the excerpts to be displayed by default
                        params.put(CmsDialog.PARAM_ACTION, new String[] {A_CmsListDialog.LIST_INDEPENDENT_ACTION});
                        params.put(
                            A_CmsListDialog.PARAM_LIST_ACTION,
                            new String[] {CmsSearchResultsList.LIST_DETAIL_EXCERPT});
                    }
                } else {
                    // toggle excerpts
                    metadata.getItemDetailDefinition(CmsSearchResultsList.LIST_DETAIL_EXCERPT).setVisible(withExcerpts);
                }
                params.put(A_CmsListExplorerDialog.PARAM_SHOW_EXPLORER, new String[] {Boolean.FALSE.toString()});
            }
            getToolManager().jspForwardTool(this, "/search/results", params);
        } catch (Exception e) {
            errors.add(e);
        }
        setCommitErrors(errors);
    }

    /**
     * Returns the list of searchable fields used in the workplace search index.<p>
     *
     * @return the list of searchable fields used in the workplace search index
     */
    public List<CmsLuceneField> getFields() {

        CmsSearchIndex index = getIndex();
        List<CmsLuceneField> result = new ArrayList<CmsLuceneField>();
        Iterator<CmsSearchField> i = index.getFieldConfiguration().getFields().iterator();
        while (i.hasNext()) {
            CmsLuceneField field = (CmsLuceneField)i.next();
            if (field.isIndexed() && field.isDisplayed()) {
                // only include indexed (ie. searchable) fields
                result.add(field);
            }
        }
        return result;
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        // check if the configured search index exists
        CmsSearchIndex index = getIndex();
        if (index == null) {
            throw new CmsIllegalStateException(Messages.get().container(
                Messages.ERR_INDEX_INVALID_1,
                getSettings().getUserSettings().getWorkplaceSearchIndexName()));
        }

        StringBuffer result = new StringBuffer(1024);

        // create widget table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // content
            result.append(createWidgetBlockStart(key(Messages.GUI_SEARCH_QUERY_TITLE_0)));
            result.append(createDialogRowsHtml(0, 3));
            result.append(createWidgetBlockEnd());
            // fields for limiting time ranges
            // result.append(createWidgetBlockStart(key(Messages.GUI_SEARCH_TIME_RANGES_0)));
            // result.append(createDialogRowsHtml(4, 7));
            // result.append(createWidgetBlockEnd());
            result.append(createWidgetBlockStart(key(Messages.GUI_SEARCH_FIELDS_TITLE_0)));
            result.append(createDialogRowsHtml(8, 8));
            result.append(createWidgetBlockEnd());
        }
        // close widget table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initParams();
        addWidget(new CmsWidgetDialogParameter(
            m_search,
            "indexName",
            PAGES[0],
            new CmsSelectOnChangeReloadWidget(getSortNamesIndex())));
        addWidget(new CmsWidgetDialogParameter(m_search, "query", PAGES[0], new CmsInputWidget()));
        addWidget(
            new CmsWidgetDialogParameter(m_search, "sortOrder", PAGES[0], new CmsSelectWidget(getSortNamesConf())));
        addWidget(new CmsWidgetDialogParameter(m_search, "restrictSearch", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "minDateCreated", PAGES[0], new CmsCalendarWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "maxDateCreated", PAGES[0], new CmsCalendarWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "minDateLastModified", PAGES[0], new CmsCalendarWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "maxDateLastModified", PAGES[0], new CmsCalendarWidget()));
        addWidget(
            new CmsWidgetDialogParameter(m_search, "fields", PAGES[0], new CmsMultiSelectWidget(getFieldList(), true)));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        super.initWorkplaceRequestValues(settings, request);
        // save the current state of the parameters (may be changed because of the widget values)
        setDialogObject(m_search);
    }

    /**
     * Returns a list of <code>{@link CmsSelectWidgetOption}</code> objects for field list selection.<p>
     *
     * @return a list of <code>{@link CmsSelectWidgetOption}</code> objects
     */
    private List<CmsSelectWidgetOption> getFieldList() {

        List<CmsSelectWidgetOption> retVal = new ArrayList<CmsSelectWidgetOption>();
        try {
            Iterator<CmsLuceneField> i = getFields().iterator();
            while (i.hasNext()) {
                CmsLuceneField field = i.next();
                if (isInitialCall()) {
                    // search form is in the initial state
                    retVal.add(new CmsSelectWidgetOption(
                        field.getName(),
                        true,
                        getMacroResolver().resolveMacros(field.getDisplayName())));
                } else {
                    // search form is not in the initial state
                    retVal.add(
                        new CmsSelectWidgetOption(
                            field.getName(),
                            false,
                            getMacroResolver().resolveMacros(field.getDisplayName())));
                }
            }
        } catch (Exception e) {
            // noop
        }
        return retVal;
    }

    /** Gets the index to use in the search.
     *
     * @return  the index to use in the search
     */
    private CmsSearchIndex getIndex() {

        CmsSearchIndex index = null;
        // get the configured index or the selected index
        if (isInitialCall()) {
            // the search form is in the initial state
            // get the configured index
            index = OpenCms.getSearchManager().getIndex(getSettings().getUserSettings().getWorkplaceSearchIndexName());
        } else {
            // the search form is not in the inital state, the submit button was used already or the
            // search index was changed already
            // get the selected index in the search dialog
            index = OpenCms.getSearchManager().getIndex(getJsp().getRequest().getParameter("indexName.0"));
        }
        return index;
    }

    /**
     * Creates the select widget configuration for the sort names.<p>
     *
     * @return the select widget configuration for the sort names
     */
    private List<CmsSelectWidgetOption> getSortNamesConf() {

        List<CmsSelectWidgetOption> retVal = new ArrayList<CmsSelectWidgetOption>();
        try {
            String[] names = CmsSearchParameters.SORT_NAMES;
            for (int i = 0; i < names.length; i++) {
                retVal.add(
                    new CmsSelectWidgetOption(
                        names[i],
                        (i == 0),
                        key(A_CmsWidget.LABEL_PREFIX + names[i].toLowerCase())));
            }
        } catch (Exception e) {
            // noop
        }
        return retVal;
    }

    /**
     * Creates the select widget configuration for the index names.<p>
     *
     * @return the select widget configuration for the index names
     */
    private List<CmsSelectWidgetOption> getSortNamesIndex() {

        List<CmsSelectWidgetOption> retVal = new ArrayList<CmsSelectWidgetOption>();
        try {
            List<String> names = OpenCms.getSearchManager().getIndexNames();
            for (int i = 0; i < names.size(); i++) {
                String indexName = names.get(i);
                String wpIndexName = getSettings().getUserSettings().getWorkplaceSearchIndexName();
                boolean isDefault = indexName.toLowerCase().equals(wpIndexName.toLowerCase());
                retVal.add(new CmsSelectWidgetOption(names.get(i), isDefault, names.get(i)));
            }
        } catch (Exception e) {
            // noop
        }
        return retVal;
    }

    /**
     * Initializes the parameters.<p>
     */
    private void initParams() {

        Object o;

        if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
            // read params from config
            o = null;
        } else {
            // this is not the initial call, get params from session
            o = getDialogObject();
        }
        if (!(o instanceof CmsSearchWorkplaceBean)) {
            String oldExplorerMode = getSettings().getExplorerMode();
            getSettings().setExplorerMode(null);
            String explorerResource = getSettings().getExplorerResource();
            getSettings().setExplorerMode(oldExplorerMode);
            m_search = new CmsSearchWorkplaceBean(explorerResource);
        } else {
            // reuse params stored in session
            m_search = (CmsSearchWorkplaceBean)o;
        }
    }

    /**
     * Gets the information if the search form is in the inital state.<p>
     *
     * @return true, the search form is in the inital state. otherwise false
     */
    private boolean isInitialCall() {

        // return false in case the form was submitted already or the submit button was pressed or the index was changed
        return !(((getJsp().getRequest().getParameter(CmsDialog.PARAM_ACTION) != null)
            && (getJsp().getRequest().getParameter(CmsDialog.PARAM_ACTION).equals(
                CmsDialog.PARAM_ACTION_VALUE_FOR_CHANGED_INDEX)))
            || ((getJsp().getRequest().getParameter("indexName.0")) != null));
    }
}
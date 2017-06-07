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

package org.opencms.workplace.tools.searchindex;

import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsMultiSelectWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * A <code>{@link org.opencms.workplace.CmsWidgetDialog}</code> that performs a
 * search on the <code>{@link org.opencms.search.CmsSearchIndex}</code> identified
 * by request parameter
 * <code>{@link org.opencms.workplace.tools.searchindex.A_CmsEditSearchIndexDialog#PARAM_INDEXNAME}</code>
 * using an instance of <code>{@link org.opencms.search.CmsSearchParameters}</code>
 * as widget object to fill.
 * <p>
 *
 * @since 6.0.0
 */
public class CmsSearchWidgetDialog extends A_CmsEditSearchIndexDialog {

    /** The request parameter for the search object. **/
    public static final String PARAM_SEARCH_OBJECT = "searchobject";

    /** The request parameter for the search parameters. **/
    public static final String PARAM_SEARCH_PARAMS = "searchparams";

    /** The search instance used with the search parameters. **/
    protected CmsSearch m_search;

    /** The search parameter instance used for storing widget values and performing search.    */
    protected CmsSearchParameters m_searchParams;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSearchWidgetDialog(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSearchWidgetDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));

    }

    /**
     * Overrides this action that is performed in case an element
     * has been added to a widget parameter
     * (<code>{@link org.opencms.workplace.CmsWidgetDialog#ACTION_ELEMENT_ADD}</code>)
     * or removed
     * (<code>{@link org.opencms.workplace.CmsWidgetDialog#ACTION_ELEMENT_REMOVE}</code>)
     * from a widget parameter to additionally commit these values to the
     * underlying lists.<p>
     *
     * This is necessary because this dialog performs a search for every request
     * (not only if OK is pressed, also if a category or field is added/removed).
     * The search directly uses the underlying Lists of categories, fields,... .
     * More precise: The very same lists that are in the search parameter instance used
     * for search are contained as base collections of the widget parameters.
     * Therefore before every search the changes of categories, fields,... have to
     * be committed here.<p>
     *
     * @see org.opencms.workplace.CmsWidgetDialog#actionToggleElement()
     */
    @Override
    public void actionToggleElement() {

        super.actionToggleElement();
        commitWidgetValues();
    }

    /**
     * Builds the standard javascript for submitting the dialog.<p>
     *
     * Overridden to allow additional validation and encoding of the
     * search query. <p>
     *
     * @return the standard javascript for submitting the dialog
     */
    @Override
    public String dialogScriptSubmit() {

        StringBuffer html = new StringBuffer(512);
        html.append(submitJS());
        html.append("function submitAction(actionValue, theForm, formName) {\n");
        html.append("\tif (theForm == null) {\n");
        html.append("\t\ttheForm = document.forms[formName];\n");
        html.append("\t}\n");
        html.append("\tvar queryOK = validateQuery();\n");
        html.append("\ttheForm." + CmsDialog.PARAM_FRAMENAME + ".value = window.name;\n");
        html.append("\tif (queryOK) {\n");
        html.append("\t\tif (actionValue == '" + CmsDialog.DIALOG_OK + "') {\n");
        //        html.append("\t\t\talert('action :'+actionValue);\n");
        html.append("\t\t\tloadingOn();\n");
        html.append("\t\t\treturn queryOK;\n");
        html.append("\t\t}\n");
        html.append("\t\ttheForm." + CmsDialog.PARAM_ACTION + ".value = actionValue;\n");
        html.append("\t\tsubmitForm(theForm);\n");
        html.append("\t}\n");
        html.append("\treturn queryOK;\n");
        html.append("}\n");
        return html.toString();
    }

    /**
     * Returns the fields parameter value.<p>
     *
     * @return the fields parameter value
     */
    public String getFields() {

        return CmsStringUtil.collectionAsString(m_searchParams.getFields(), ",");
    }

    /**
     * Returns the creation date the resources have to have as maximum.<p>
     *
     * @return the creation date the resources have to have as maximum
     */
    public String getMaxDateCreated() {

        if (m_searchParams.getMaxDateCreated() == Long.MAX_VALUE) {
            return "";
        }
        return Long.toString(m_searchParams.getMaxDateCreated());
    }

    /**
     * Returns the last modification date the resources have to have as maximum.<p>
     *
     * @return the last modification date the resources have to have as maximum
     */
    public String getMaxDateLastModified() {

        if (m_searchParams.getMaxDateLastModified() == Long.MAX_VALUE) {
            return "";
        }
        return Long.toString(m_searchParams.getMaxDateLastModified());
    }

    /**
     * Returns the creation date the resources have to have as minimum.<p>
     *
     * @return the creation date the resources have to have as minimum
     */
    public String getMinDateCreated() {

        if (m_searchParams.getMinDateCreated() == Long.MIN_VALUE) {
            return "";
        }
        return Long.toString(m_searchParams.getMinDateCreated());
    }

    /**
     * Returns the last modification date the resources have to have as minimum.<p>
     *
     * @return the last modification date the resources have to have as minimum
     */
    public String getMinDateLastModified() {

        if (m_searchParams.getMinDateLastModified() == Long.MIN_VALUE) {
            return "";
        }
        return Long.toString(m_searchParams.getMinDateLastModified());
    }

    /**
     * Returns the list of searchable fields used in the workplace search index.<p>
     *
     * @return the list of searchable fields used in the workplace search index
     */
    public List<CmsSearchField> getSearchFields() {

        CmsSearchIndex index = OpenCms.getSearchManager().getIndex(getParamIndexName());
        List<CmsSearchField> result = new ArrayList<CmsSearchField>();
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
     * Sets the fields parameter value.<p>
     *
     * @param fields the fields parameter value to set
     */
    public void setFields(String fields) {

        String searchPage = getJsp().getRequest().getParameter("searchPage");
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(searchPage) && CmsStringUtil.isEmptyOrWhitespaceOnly(fields)) {
            throw new CmsIllegalStateException(
                org.opencms.workplace.search.Messages.get().container(
                    org.opencms.workplace.search.Messages.ERR_VALIDATE_SEARCH_PARAMS_0));
        }
        m_searchParams.setFields(CmsStringUtil.splitAsList(fields, ","));
    }

    /**
     * Sets the creation date the resources have to have as maximum.<p>
     *
     * @param maxCreationDate the creation date the resources have to have as maximum to set
     */
    public void setMaxDateCreated(String maxCreationDate) {

        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(maxCreationDate)) && (!maxCreationDate.equals("0"))) {
            m_searchParams.setMaxDateCreated(Long.parseLong(maxCreationDate));
        } else {
            m_searchParams.setMaxDateCreated(Long.MAX_VALUE);
        }
    }

    /**
     * Sets the last modification date the resources have to have as maximum.<p>
     *
     * @param maxDateLastModified the last modification date the resources have to have as maximum to set
     */
    public void setMaxDateLastModified(String maxDateLastModified) {

        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(maxDateLastModified)) && (!maxDateLastModified.equals("0"))) {
            m_searchParams.setMaxDateLastModified(Long.parseLong(maxDateLastModified));
        } else {
            m_searchParams.setMaxDateLastModified(Long.MAX_VALUE);
        }
    }

    /**
     * Sets the creation date the resources have to have as minimum.<p>
     *
     * @param minCreationDate the creation date the resources have to have as minimum to set
     */
    public void setMinDateCreated(String minCreationDate) {

        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(minCreationDate)) && (!minCreationDate.equals("0"))) {
            m_searchParams.setMinDateCreated(Long.parseLong(minCreationDate));
        } else {
            m_searchParams.setMinDateCreated(Long.MIN_VALUE);
        }
    }

    /**
     * Sets the last modification date the resources have to have as minimum.<p>
     *
     * @param minDateLastModified the last modification date the resources have to have as minimum to set
     */
    public void setMinDateLastModified(String minDateLastModified) {

        if ((CmsStringUtil.isNotEmptyOrWhitespaceOnly(minDateLastModified)) && (!minDateLastModified.equals("0"))) {
            m_searchParams.setMinDateLastModified(Long.parseLong(minDateLastModified));
        } else {
            m_searchParams.setMinDateLastModified(Long.MIN_VALUE);
        }
    }

    /**
     * This dialog does not return on commit but stay for many search requests until it is
     * exited with cancel or up in the workplace. <p>
     *
     * @return false always to ensure the dialog is not left
     *
     * @see org.opencms.workplace.CmsWidgetDialog#closeDialogOnCommit()
     */
    @Override
    protected boolean closeDialogOnCommit() {

        return false;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {

            // first block "Query for index...."
            result.append(dialogBlockStart(key(Messages.GUI_LABEL_SEARCHINDEX_BLOCK_SEARCH_QUERY_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 5));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());

            // 2nd block with limiting to time ranges
            result.append(dialogBlockStart(key(Messages.GUI_LABEL_SEARCHINDEX_BLOCK_SEARCH_TIME_RANGES_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(6, 9));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());

            // 3rd block "Fields to search in"
            result.append(dialogBlockStart(key(Messages.GUI_LABEL_SEARCHINDEX_BLOCK_SEARCH_FIELDS_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(10, 10));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * Returns the html code for the default action content.<p>
     *
     * Overrides <code> {@link org.opencms.workplace.CmsWidgetDialog#defaultActionHtml()}</code>
     * in order to put additional forms for page links and search results after OK - CANCEL buttons
     * and outside the main form.<p>
     *
     * @return html code
     */
    @Override
    protected String defaultActionHtmlContent() {

        StringBuffer result = new StringBuffer(2048);
        result.append("<form name=\"EDITOR\" id=\"EDITOR\" method=\"post\" action=\"").append(getDialogUri());
        result.append("\" class=\"nomargin\" onsubmit=\"return submitAction('").append(DIALOG_OK).append(
            "', null, 'EDITOR');\">\n");
        result.append(dialogContentStart(null));
        result.append(buildDialogForm());
        result.append(dialogContentEnd());
        result.append(dialogButtonsCustom());
        result.append(paramsAsHidden());
        if (getParamFramename() == null) {
            result.append("\n<input type=\"hidden\" name=\"").append(PARAM_FRAMENAME).append("\" value=\"\">\n");
        }

        // add script for filtering categories
        result.append(filterCategoryJS());
        result.append("</form>\n");

        // get search results if query was more than nothing
        // we have to retrieve them before category search results are available because
        // those are collected as a side effect of search iteration.
        String searchResults = createSearchResults();

        // append category search results if there...
        result.append(createCategorySearchResultHtml());
        result.append(searchResults);

        result.append(getWidgetHtmlEnd());
        // Normalize the previous encoded query value on client side
        result.append(normalizePreviousQueryJS());

        return result.toString();
    }

    /**
     *
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        // initialization -> initUserObject
        super.defineWidgets();
        // first block "Query for search index..."
        addWidget(new CmsWidgetDialogParameter(m_searchParams, "index", "", PAGES[0], new CmsDisplayWidget(), 1, 1));
        addWidget(new CmsWidgetDialogParameter(m_searchParams, "query", "", PAGES[0], new CmsInputWidget(), 1, 1));
        addWidget(
            new CmsWidgetDialogParameter(
                m_searchParams,
                "sortName",
                "",
                PAGES[0],
                new CmsSelectWidget(getSortWidgetConfiguration()),
                0,
                1));
        addWidget(new CmsWidgetDialogParameter(
            m_searchParams.getRoots(),
            "roots",
            "/",
            PAGES[0],
            new CmsVfsFileWidget(),
            1,
            10));
        addWidget(
            new CmsWidgetDialogParameter(
                m_searchParams.getCategories(),
                "categories",
                "",
                PAGES[0],
                new CmsInputWidget(),
                0,
                6));

        addWidget(new CmsWidgetDialogParameter(m_searchParams, "calculateCategories", new CmsCheckboxWidget()));

        // 2nd block with limiting to time ranges
        addWidget(new CmsWidgetDialogParameter(this, "minDateCreated", PAGES[0], new CmsCalendarWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "maxDateCreated", PAGES[0], new CmsCalendarWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "minDateLastModified", PAGES[0], new CmsCalendarWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "maxDateLastModified", PAGES[0], new CmsCalendarWidget()));

        // 3rd block "fields to search in"
        addWidget(
            new CmsWidgetDialogParameter(this, "fields", PAGES[0], new CmsMultiSelectWidget(getFieldList(), true)));
    }

    /**
     * Overridden to additionally get a hold on the widget object of type
     * <code>{@link CmsSearchParameters}</code>.<p>
     *
     * @see org.opencms.workplace.tools.searchindex.A_CmsEditSearchIndexDialog#initUserObject()
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected void initUserObject() {

        super.initUserObject();
        Object o = getDialogObject();
        if (o == null) {
            m_searchParams = new CmsSearchParameters();
            // implant a hook upon modifications of the list
            // this will set the search page to 1 if a restriction to the set of categories is performed
            m_searchParams.setCategories(new CmsHookListSearchCategory(m_searchParams, m_searchParams.getCategories()));
            m_search = new CmsSearch();
        } else {
            Map dialogObject = (Map)o;
            m_searchParams = (CmsSearchParameters)dialogObject.get(PARAM_SEARCH_PARAMS);
            if (m_searchParams == null) {
                m_searchParams = new CmsSearchParameters();
            }
            m_search = (CmsSearch)dialogObject.get(PARAM_SEARCH_OBJECT);
            if (m_search == null) {
                m_search = new CmsSearch();
            }
        }
        m_searchParams.setSearchIndex(getSearchIndexIndex());
    }

    /**
     * Additionally saves <code>{@link #PARAM_SEARCH_PARAMS}</code> to the dialog object map.<p>
     *
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        Map dialogMap = (Map)getDialogObject();
        if (dialogMap != null) {
            dialogMap.put(PARAM_SEARCH_PARAMS, m_searchParams);
            dialogMap.put(PARAM_SEARCH_OBJECT, m_search);
        }
    }

    /**
     * Returns the hmtl for the category search results.<p>
     *
     * Note that a valid search (<code>{@link CmsSearch#getSearchResult()}</code> with
     * correct settings and inited) has to be triggered before this call or an empty
     * String will be returned. <p>
     *
     * @return the hmtl for the category search results
     */
    private String createCategorySearchResultHtml() {

        StringBuffer result = new StringBuffer();
        if (m_searchParams.isCalculateCategories()) {
            // trigger calculation of categories, even if we don't need search results
            // this is cached unless more set operation on CmsSearch are performed
            Map<String, Integer> categoryMap = m_search.getSearchResultCategories();
            if (categoryMap != null) {
                result.append(dialogContentStart(null));
                result.append(result.append(createWidgetTableStart()));
                // first block "Query for index...."
                result.append(dialogBlockStart(
                    key(
                        Messages.GUI_LABEL_SEARCHINDEX_BLOCK_SEARCH_CATEGORIES_1,
                        new Object[] {m_searchParams.getQuery()})));
                result.append(createWidgetTableStart());

                // categories:
                result.append("\n<p>\n");
                for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
                    result.append("  ").append("<a class=\"searchcategory\" href=\"#\" onClick=\"filterCategory('");
                    result.append(entry.getKey()).append("')\")>");
                    result.append(entry.getKey());
                    result.append("</a> : ");
                    result.append(entry.getValue()).append("<br>\n");
                }
                result.append("</p>\n");

                result.append(createWidgetTableEnd());
                result.append(dialogBlockEnd());
                result.append(createWidgetTableEnd());
                result.append(dialogContentEnd());
            }
        }
        return result.toString();
    }

    /**
     * Returns the HTML for the search results.<p>
     *
     * @return the HTML for the search results
     */
    private String createSearchResults() {

        String query = m_searchParams.getQuery();
        StringBuffer result = new StringBuffer();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(query) && (query.length() > 3)) {
            CmsSearchResultView resultView = new CmsSearchResultView(getJsp());
            // proprietary workplace admin link for pagelinks of search:
            resultView.setSearchRessourceUrl(getJsp().link(
                "/system/workplace/views/admin/admin-main.jsp?path=/searchindex/singleindex/search&indexname="
                    + getSearchIndexIndex().getName()));
            m_search.init(getCms());

            // custom parameters (non-widget controlled)
            // these are from generated search page links
            String page = getJsp().getRequest().getParameter("searchPage");
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(page)) {
                m_searchParams.setSearchPage(Integer.parseInt(page));
            }
            String categories = getJsp().getRequest().getParameter("searchCategories");
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(categories)) {
                m_searchParams.setCategories(CmsStringUtil.splitAsList(categories, ','));
            }

            String searchRoots = getJsp().getRequest().getParameter("searchRoots");
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(searchRoots)) {
                m_searchParams.setSearchRoots(searchRoots);
            }
            m_search.setParameters(m_searchParams);
            result.append("<div style=\"padding:12px;\">\n").append(resultView.displaySearchResult(m_search)).append(
                "\n</div>\n");
        } else {
            // Just don't perform search
        }
        return result.toString();
    }

    /**
     * Generates the JavaScript to filter categories.<p>
     *
     * @return the JavaScript
     */
    private String filterCategoryJS() {

        StringBuffer result = new StringBuffer();
        // fool the widget with a hidden categories input field
        result.append("<input name=\"dummysearchcategory\" id=\"dummysearchcategory\" type=\"hidden\" value=\"\">\n");
        result.append("<input name=\"dummysearchpage\" id=\"dummysearchpage\" type=\"hidden\" value=\"\">\n");
        // delete all other chosen "cateogries.x" values and put a value here
        result.append("<script type=\"text/javascript\">\n");
        result.append("  function filterCategory(category) {\n");
        result.append("    var searchform = document.forms['EDITOR'];\n");
        result.append("    var inputFields = searchform.elements;\n");
        result.append("    for (var i=0; i<inputFields.length; i++) {\n");
        result.append("      if(inputFields[i].name != null && inputFields[i].name.indexOf('categories') != -1) {\n");
        result.append("        inputFields[i].value='';\n");
        result.append("        inputFields[i].name='invalidsearchcategory';\n");
        result.append("      }\n");
        result.append("    }\n");
        // if we found a category field use it, if not, set the name of our
        // fooling field to "categories.0" or the widget filling will fail
        result.append("    var categoryField = inputFields['dummysearchcategory'];\n");
        result.append("    categoryField.name = 'categories.0';\n");
        result.append("    categoryField.value = category;\n");
        // additionally set searchpage to zero because filtered results may / will be smaller
        result.append("    inputFields['dummysearchpage'].value = '0';\n");
        result.append("    inputFields['dummysearchpage'].name = 'searchpage.0';\n");
        result.append("    validateQuery();\n");
        result.append("    searchform.submit();\n");
        result.append("  }\n");
        result.append("</script>\n");
        return result.toString();
    }

    /**
     * Returns a list of <code>{@link CmsSelectWidgetOption}</code> objects for field list selection.<p>
     *
     * @return a list of <code>{@link CmsSelectWidgetOption}</code> objects
     */
    private List<CmsSelectWidgetOption> getFieldList() {

        List<CmsSelectWidgetOption> retVal = new ArrayList<CmsSelectWidgetOption>();
        try {
            Iterator<CmsSearchField> i = getSearchFields().iterator();
            while (i.hasNext()) {
                CmsLuceneField field = (CmsLuceneField)i.next();
                retVal.add(
                    new CmsSelectWidgetOption(
                        field.getName(),
                        true,
                        getMacroResolver().resolveMacros(field.getDisplayName())));
            }
        } catch (Exception e) {
            // noop
        }
        return retVal;
    }

    /**
     * Returns the different select options for sort search result criteria. <p>
     *
     * @return the different select options for sort search result criteria
     */
    private List<CmsSelectWidgetOption> getSortWidgetConfiguration() {

        List<CmsSelectWidgetOption> result = new LinkedList<CmsSelectWidgetOption>();
        CmsMessages messages = Messages.get().getBundle(getLocale());
        result.add(new CmsSelectWidgetOption(
            CmsSearchParameters.SORT_NAMES[0],
            true,
            messages.key(Messages.GUI_SELECT_LABEL_SEARCH_SORT_SCORE_0)));
        result.add(
            new CmsSelectWidgetOption(
                CmsSearchParameters.SORT_NAMES[1],
                false,
                messages.key(Messages.GUI_SELECT_LABEL_SEARCH_SORT_DATE_CREATED_0)));
        result.add(
            new CmsSelectWidgetOption(
                CmsSearchParameters.SORT_NAMES[2],
                false,
                messages.key(Messages.GUI_SELECT_LABEL_SEARCH_SORT_DATE_LAST_MODIFIED_0)));
        result.add(
            new CmsSelectWidgetOption(
                CmsSearchParameters.SORT_NAMES[3],
                false,
                messages.key(Messages.GUI_SELECT_LABEL_SEARCH_SORT_TITLE_0)));
        return result;
    }

    /**
     * Normalizes the JavaScript for the previous search query.<p>
     *
     * @return the normalized JavaScript
     */
    private String normalizePreviousQueryJS() {

        StringBuffer result = new StringBuffer();
        result.append("<script type=\"text/javascript\">\n");
        result.append("  function normalizeQueryValue() {\n");
        result.append("    var searchform = document.forms['EDITOR'];\n");
        result.append("    var query = searchform.elements['query.0'].value;\n");
        result.append("    query = decodeURI(query);\n");
        result.append("    searchform.elements['query.0'].value = query;\n");
        result.append("    return true;\n");
        result.append("  }\n");
        result.append("  normalizeQueryValue();\n");
        result.append("</script>\n");
        return result.toString();
    }

    /**
     * Returns the JavaScript for submitting the search form.<p>
     *
     * @return the JavaScript for submitting the search form
     */
    private String submitJS() {

        StringBuffer result = new StringBuffer();
        result.append("  function validateQuery() {\n");
        result.append("    var searchform = document.getElementById(\"EDITOR\");\n");
        result.append("    var query = searchform.elements['query.0'].value;\n");
        result.append("    searchform.elements['query.0'].value = query;\n");
        result.append("    return true;\n");
        result.append("  }\n");
        return result.toString();
    }
}
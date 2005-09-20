/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/searchindex/CmsSearchWidgetDialog.java,v $
 * Date   : $Date: 2005/09/20 15:39:06 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.file.CmsRequestContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchParameters;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

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
 * @author Achim Westermann 
 * 
 * @version $Revision: 1.1.2.1 $
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
     * (<code>{@link org.opencms.workplace.CmsWidgetDialog#ACTION_ELEMENT_ADD}</code>) or removed 
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
     * This dialog does not return on commit but stay for many search requests until it is 
     * exited with cancel or up in the workplace. <p>
     * 
     * @return false always to ensure the dialog is not left
     * 
     * @see org.opencms.workplace.CmsWidgetDialog#closeDialogOnCommit()
     */
    protected boolean closeDialogOnCommit() {

        return false;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
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

            // 2nd block "Fields to search in"
            result.append(dialogBlockStart(key(Messages.GUI_LABEL_SEARCHINDEX_BLOCK_SEARCH_FIELDS_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(6, 10));
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
    protected void defineWidgets() {

        // initialization -> initUserObject
        super.defineWidgets();
        // first block "Query for search index..."
        addWidget(new CmsWidgetDialogParameter(m_searchParams, "index", "", PAGES[0], new CmsDisplayWidget(), 1, 1));
        addWidget(new CmsWidgetDialogParameter(m_searchParams, "query", "", PAGES[0], new CmsInputWidget(), 1, 1));
        addWidget(new CmsWidgetDialogParameter(m_searchParams, "sortName", "", PAGES[0], new CmsSelectWidget(
            getSortWidgetConfiguration()), 0, 1));
        addWidget(new CmsWidgetDialogParameter(
            m_searchParams.getRoots(),
            "roots",
            "/",
            PAGES[0],
            new CmsVfsFileWidget(),
            1,
            10));
        addWidget(new CmsWidgetDialogParameter(
            m_searchParams.getCategories(),
            "categories",
            "",
            PAGES[0],
            new CmsInputWidget(),
            0,
            6));

        addWidget(new CmsWidgetDialogParameter(m_searchParams, "calculateCategories", new CmsCheckboxWidget()));

        // 2nd block "fields to search in"
        addWidget(new CmsWidgetDialogParameter(m_searchParams, "searchFieldContent", new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_searchParams, "searchFieldMeta", new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_searchParams, "searchFieldTitle", new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_searchParams, "searchFieldKeywords", new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_searchParams, "searchFieldDescription", new CmsCheckboxWidget()));

    }

    /**
     * Overridden to additionally get a hold on the widget object of type 
     * <code>{@link CmsSearchParameters}</code>.<p>
     * 
     * @see org.opencms.workplace.tools.searchindex.A_CmsEditSearchIndexDialog#initUserObject()
     */
    protected void initUserObject() {

        super.initUserObject();
        Object o = this.getDialogObject();
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
        m_searchParams.setSearchIndex(m_index);
    }

    /**
     * Additionally saves <code>{@link #PARAM_SEARCH_PARAMS}</code> to the dialog object map.<p> 
     * 
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        Map dialogMap = (Map)this.getDialogObject();
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
     * @param search the preconfigured search bean 
     * 
     * @return the hmtl for the category search results
     */
    private String createCategorySearchResultHtml() {

        StringBuffer result = new StringBuffer();
        if (m_searchParams.isCalculateCategories()) {
            // trigger calculation of categories, even if we don't need search results 
            // this is cached unless more set operation on CmsSearch are performed
            Map categoryMap = m_search.getSearchResultCategories();
            if (categoryMap != null) {
                result.append(dialogContentStart(null));
                result.append(result.append(createWidgetTableStart()));
                // first block "Query for index...."
                result.append(dialogBlockStart(key(
                    Messages.GUI_LABEL_SEARCHINDEX_BLOCK_SEARCH_CATEGORIES_1,
                    new Object[] {m_searchParams.getQuery()})));
                result.append(createWidgetTableStart());

                // categories:
                result.append("\n<p>\n");
                Map.Entry entry;
                Iterator it = categoryMap.entrySet().iterator();
                while (it.hasNext()) {
                    entry = (Map.Entry)it.next();
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

    private String createSearchResults() {

        String query = m_searchParams.getQuery();
        StringBuffer result = new StringBuffer();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(query) && query.length() > 3) {
            CmsSearchResultView resultView = new CmsSearchResultView(getJsp());
            // proprietary workplace admin link for pagelinks of search: 
            resultView.setSearchRessourceUrl(getJsp().link(
                "/system/workplace/views/admin/admin-main.html?path=/searchindex/singleindex/search&indexname="
                    + m_index.getName()));
            m_search.init(getCms());

            // custom parameters (non-widget controlled) 
            // these are from generated search page links 
            String page = getJsp().getRequest().getParameter("searchpage");
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(page)) {
                m_searchParams.setSearchPage(Integer.parseInt(page));
            }
            String categories = getJsp().getRequest().getParameter("searchcategories");
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(categories)) {
                m_searchParams.setCategories(CmsStringUtil.splitAsList(categories, ','));
            }

            String searchRoots = getJsp().getRequest().getParameter("searchRoots");
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(searchRoots)) {
                m_searchParams.setSearchRoots(searchRoots);
            }
            m_search.setParameters(m_searchParams);

            // set to root site to allow searching every index in the administration view 
            // regardless of the site selection. This behaviour is not wanted online!
            CmsRequestContext context = getJsp().getRequestContext();
            String siteRoot = context.getSiteRoot();
            context.setSiteRoot("");
            result.append("<div style=\"padding:12px;\">\n").append(resultView.displaySearchResult(m_search)).append(
                "\n</div>\n");
            context.setSiteRoot(siteRoot);

        } else {
            // Just don't perform search
        }
        return result.toString();
    }

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
        result.append("      if(inputFields[i].name.indexOf('categories') != -1) {\n");
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
     * Returns the different select options for sort search result criteria. <p> 
     * 
     * @return the different select options for sort search result criteria 
     */
    private List getSortWidgetConfiguration() {

        List result = new LinkedList();
        result.add(new CmsSelectWidgetOption(CmsSearchParameters.SORT_NAMES[0], true, Messages.get().key(
            getLocale(),
            Messages.GUI_SELECT_LABEL_SEARCH_SORT_SCORE_0)));
        result.add(new CmsSelectWidgetOption(CmsSearchParameters.SORT_NAMES[1], false, Messages.get().key(
            getLocale(),
            Messages.GUI_SELECT_LABEL_SEARCH_SORT_DATE_CREATED_0)));
        result.add(new CmsSelectWidgetOption(CmsSearchParameters.SORT_NAMES[2], false, Messages.get().key(
            getLocale(),
            Messages.GUI_SELECT_LABEL_SEARCH_SORT_DATE_LAST_MODIFIED_0)));
        result.add(new CmsSelectWidgetOption(CmsSearchParameters.SORT_NAMES[3], false, Messages.get().key(
            getLocale(),
            Messages.GUI_SELECT_LABEL_SEARCH_SORT_TITLE_0)));
        return result;
    }

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

    private String submitJS() {

        StringBuffer result = new StringBuffer();
        result.append("  function validateQuery() {\n");
        result.append("    var searchform = document.forms['EDITOR'];\n");
        result.append("    var query = searchform.elements['query.0'].value;\n");
        //        result.append("    alert('query: '+query);\n");
        result.append("    query = encodeURI(query);\n");
        //        result.append("    alert('encoded query: '+query);\n");
        result.append("    searchform.elements['query.0'].value = query;\n");
        result.append("    return true;\n");
        result.append("  }\n");
        result.append("  window.captureEvents(Event.SUBMIT);\n");
        result.append("  window.onsubmit = validateQuery();\n");
        return result.toString();
    }
}

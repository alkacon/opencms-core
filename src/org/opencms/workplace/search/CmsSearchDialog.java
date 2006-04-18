/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/search/CmsSearchDialog.java,v $
 * Date   : $Date: 2006/04/18 16:14:03 $
 * Version: $Revision: 1.1.2.1 $
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 * All rights reserved.
 * 
 * This source code is the intellectual property of Alkacon Software GmbH.
 * It is PROPRIETARY and CONFIDENTIAL.
 * Use of this source code is subject to license terms.
 *
 * In order to use this source code, you need written permission from 
 * Alkacon Software GmbH. Redistribution of this source code, in modified 
 * or unmodified form, is not allowed unless written permission by 
 * Alkacon Software GmbH has been given.
 *
 * ALKACON SOFTWARE GMBH MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOURCE CODE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ALKACON SOFTWARE GMBH SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOURCE CODE OR ITS DERIVATIVES.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 */

package org.opencms.workplace.search;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.search.CmsSearchParameters;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.tools.CmsToolDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a GUI for the workplace search feature.<p> 
 *
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.2.0 
 */
public class CmsSearchDialog extends CmsWidgetDialog {

    /** the dialog type. */
    private static final String DIALOG_TYPE = "search";

    /** Defines which pages are valid for this dialog. */
    private static final String[] PAGES = {"page1"};

    /** the search data. */
    private CmsSearchParameters m_search;

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
    public void actionCommit() {

        List errors = new ArrayList();
        try {
            Map params = new HashMap();
            params.put(CmsSearchResultsList.PARAM_QUERY, m_search.getQuery());
            params.put(CmsSearchResultsList.PARAM_SORT_ORDER, m_search.getSortName());
            params.put(CmsSearchResultsList.PARAM_FIELD_CONTENT, "" + m_search.getSearchFieldContent());
            params.put(CmsSearchResultsList.PARAM_FIELD_META, "" + m_search.getSearchFieldMeta());
            params.put(CmsDialog.PARAM_ACTION, A_CmsListDialog.LIST_SELECT_PAGE);
            params.put(A_CmsListDialog.PARAM_PAGE, "1");
            params.put(CmsToolDialog.PARAM_ROOT, "explorer");
            getToolManager().jspForwardTool(this, "/search/results", params);
        } catch (Exception e) {
            errors.add(e);
        }
        setCommitErrors(errors);
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     * 
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create widget table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // content
            result.append(createWidgetBlockStart(key(Messages.GUI_SEARCH_QUERY_TITLE_0)));
            result.append(createDialogRowsHtml(0, 1));
            result.append(createWidgetBlockEnd());
            result.append(createWidgetBlockStart(key(Messages.GUI_SEARCH_FIELDS_TITLE_0)));
            result.append(createDialogRowsHtml(2, 3));
            result.append(createWidgetBlockEnd());
        }
        // close widget table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    protected void defineWidgets() {

        initParams();
        addWidget(new CmsWidgetDialogParameter(m_search, "query", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "sortName", PAGES[0], new CmsSelectWidget(getSortNamesConf())));
        addWidget(new CmsWidgetDialogParameter(m_search, "searchFieldContent", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "searchFieldMeta", PAGES[0], new CmsCheckboxWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        super.initWorkplaceRequestValues(settings, request);
        // save the current state of the parameters (may be changed because of the widget values)
        setDialogObject(m_search);
    }

    /**
     * Creates the select widget configuration for the sort names.<p>
     * 
     * @return the select widget configuration for the sort names
     */
    private List getSortNamesConf() {

        List retVal = new ArrayList();
        try {
            String[] names = CmsSearchParameters.SORT_NAMES;
            for (int i = 0; i < names.length; i++) {
                retVal.add(new CmsSelectWidgetOption(
                    names[i],
                    names[i].equals(CmsSearchParameters.SORT_DEFAULT),
                    key(A_CmsWidget.LABEL_PREFIX + names[i].toLowerCase())));
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

        if (!(o instanceof CmsSearchParameters)) {
            // read params from config
            m_search = new CmsSearchParameters();
            m_search.setSearchFieldContent(true);
            m_search.setSearchFieldMeta(true);
            m_search.setSortName(CmsSearchParameters.SORT_NAMES[0]);
        } else {
            // reuse params stored in session
            m_search = (CmsSearchParameters)o;
        }
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/search/CmsSearchDialog.java,v $
 * Date   : $Date: 2011/03/23 14:52:48 $
 * Version: $Revision: 1.11 $
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.db.CmsUserSettings.CmsSearchResultStyle;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsMultiSelectWidget;
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
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.11 $ 
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

        List errors = new ArrayList();
        try {
            Map params = new HashMap();
            params.put(CmsDialog.PARAM_ACTION, A_CmsListDialog.LIST_SELECT_PAGE);
            params.put(A_CmsListDialog.PARAM_PAGE, "1");
            params.put(CmsToolDialog.PARAM_ROOT, "explorer");
            if (getSettings().getUserSettings().getWorkplaceSearchViewStyle() == CmsSearchResultStyle.STYLE_EXPLORER) {
                getSettings().setExplorerPage(1);
                params.put(A_CmsListExplorerDialog.PARAM_SHOW_EXPLORER, Boolean.TRUE);
            } else {
                CmsListMetadata metadata = A_CmsListDialog.getMetadata(CmsSearchResultsList.class.getName());
                boolean withExcerpts = (getSettings().getUserSettings().getWorkplaceSearchViewStyle() == CmsSearchResultStyle.STYLE_LIST_WITH_EXCERPTS);
                if (metadata == null) {
                    if (!withExcerpts) {
                        // prevent the excerpts to be displayed by default
                        params.put(CmsDialog.PARAM_ACTION, A_CmsListDialog.LIST_INDEPENDENT_ACTION);
                        params.put(A_CmsListDialog.PARAM_LIST_ACTION, CmsSearchResultsList.LIST_DETAIL_EXCERPT);
                    }
                } else {
                    // toggle excerpts
                    metadata.getItemDetailDefinition(CmsSearchResultsList.LIST_DETAIL_EXCERPT).setVisible(withExcerpts);
                }
                params.put(A_CmsListExplorerDialog.PARAM_SHOW_EXPLORER, Boolean.FALSE);
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
    public List getFields() {

        CmsSearchIndex index = OpenCms.getSearchManager().getIndex(
            getSettings().getUserSettings().getWorkplaceSearchIndexName());

        List result = new ArrayList();
        Iterator i = index.getFieldConfiguration().getFields().iterator();
        while (i.hasNext()) {
            CmsSearchField field = (CmsSearchField)i.next();
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
        CmsSearchIndex index = OpenCms.getSearchManager().getIndex(
            getSettings().getUserSettings().getWorkplaceSearchIndexName());
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
            this,
            "settings.userSettings.workplaceSearchIndexName",
            PAGES[0],
            new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "query", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "sortOrder", PAGES[0], new CmsSelectWidget(getSortNamesConf())));
        addWidget(new CmsWidgetDialogParameter(m_search, "restrictSearch", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "minDateCreated", PAGES[0], new CmsCalendarWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "maxDateCreated", PAGES[0], new CmsCalendarWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "minDateLastModified", PAGES[0], new CmsCalendarWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "maxDateLastModified", PAGES[0], new CmsCalendarWidget()));
        addWidget(new CmsWidgetDialogParameter(m_search, "fields", PAGES[0], new CmsMultiSelectWidget(
            getFieldList(),
            true)));
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
    private List getFieldList() {

        List retVal = new ArrayList();
        try {
            Iterator i = getFields().iterator();
            while (i.hasNext()) {
                CmsSearchField field = (CmsSearchField)i.next();
                retVal.add(new CmsSelectWidgetOption(field.getName(), true, getMacroResolver().resolveMacros(
                    field.getDisplayName())));
            }
        } catch (Exception e) {
            // noop
        }
        return retVal;
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
                retVal.add(new CmsSelectWidgetOption(names[i], (i == 0), key(A_CmsWidget.LABEL_PREFIX
                    + names[i].toLowerCase())));
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
            // read params from config
            m_search = new CmsSearchWorkplaceBean(getSettings().getExplorerResource());
        } else {
            // reuse params stored in session
            m_search = (CmsSearchWorkplaceBean)o;
        }
    }
}
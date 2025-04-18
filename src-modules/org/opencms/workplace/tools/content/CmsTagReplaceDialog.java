/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.content;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Widget dialog that sets the settings to replace HTML Tags in pages below a folder.
 * <p>
 *
 * @since 6.1.7
 */
public class CmsTagReplaceDialog extends CmsWidgetDialog {

    /** Localized message keys prefix. */
    public static final String KEY_PREFIX = "tagreplace";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The import JSP report workplace URI. */
    protected static final String TAGREPLACE_ACTION_REPORT = PATH_WORKPLACE
        + "admin/contenttools/reports/tagreplace.jsp";

    /** The settings object that is edited on this dialog. */
    private CmsTagReplaceSettings m_settings;

    /**
     * Public constructor with JSP action element.
     * <p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsTagReplaceDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.
     * <p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsTagReplaceDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        List errors = new ArrayList();
        setDialogObject(m_settings);

        try {

            Map params = new HashMap();
            // set style to display report in correct layout
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
            // set close link to get back to overview after finishing the import
            params.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/contenttools"));
            // redirect to the report output JSP
            getToolManager().jspForwardPage(this, TAGREPLACE_ACTION_REPORT, params);

        } catch (CmsIllegalArgumentException e) {
            errors.add(e);
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        // create export file name block
        result.append(createWidgetBlockStart(
            Messages.get().getBundle(getLocale()).key(Messages.GUI_TAGREPLACE_DIALOG_BLOCK_SETTINGS_0)));
        result.append(createDialogRowsHtml(0, 2));
        result.append(createWidgetBlockEnd());

        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        // initialize the export object to use for the dialog
        initSettingsObject();

        // set localized key prefix
        setKeyPrefix(KEY_PREFIX);
        addWidget(new CmsWidgetDialogParameter(m_settings, "replacements", PAGES[0], new CmsInputWidget()));
        addWidget(
            new CmsWidgetDialogParameter(
                m_settings,
                "workPath",
                "/",
                PAGES[0],
                new CmsVfsFileWidget(false, getCms().getRequestContext().getSiteRoot()),
                1,
                1));

        addWidget(
            new CmsWidgetDialogParameter(m_settings, "propertyValueTagReplaceID", PAGES[0], new CmsInputWidget("")));
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
        // add workplace messages
        addMessages("org.opencms.workplace.workplace");
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Initializes the settings object to work with depending on the dialog state and request
     * parameters.
     * <p>
     */
    protected void initSettingsObject() {

        Object o;
        if (CmsStringUtil.isEmpty(getParamAction())) {
            o = new CmsTagReplaceSettings(getCms());
        } else {
            // this is not the initial call, get the job object from session
            o = getDialogObject();
        }

        if (o == null) {
            // create a new export handler object
            m_settings = new CmsTagReplaceSettings(getCms());
        } else {
            // reuse export handler object stored in session
            m_settings = (CmsTagReplaceSettings)o;
        }

    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings,
     *      javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the export handler (may be changed because of the widget
        // values)
        setDialogObject(m_settings);
    }
}
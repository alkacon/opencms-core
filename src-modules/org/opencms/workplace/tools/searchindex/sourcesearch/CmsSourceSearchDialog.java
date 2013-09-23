/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.searchindex.sourcesearch;

import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.replace.CmsSearchReplaceSettings;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Widget dialog that collects the folders and the search phrase for content search operation.
 * <p>
 * 
 * @since 7.5.3
 */
public class CmsSourceSearchDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "sourcesearch";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The request parameter for the resources to process from the previous dialog. */
    public static final String PARAM_SEARCHCONTENT = "searchcontent";

    /** The widget mapped data container. */
    private CmsSearchReplaceSettings m_settings;

    /**
     * Public constructor with JSP action element.
     * <p>
     * 
     * @param jsp
     *            an initialized JSP action element
     */
    public CmsSourceSearchDialog(final CmsJspActionElement jsp) {

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
    public CmsSourceSearchDialog(final PageContext context, final HttpServletRequest req, final HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));

    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        setDialogObject(m_dialogObject);
        List<Throwable> errors = new ArrayList<Throwable>();
        // create absolute RFS path and store it in dialog object

        Map<String, String[]> params = new HashMap<String, String[]>();
        // set style to display report in correct layout
        params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
        // set close link to get back to overview after finishing the import
        params.put(PARAM_CLOSELINK, new String[] {CmsToolManager.linkForToolPath(getJsp(), "/sourcesearch")});
        // redirect to the report output JSP
        getToolManager().jspForwardPage(
            this,
            CmsWorkplace.PATH_WORKPLACE + "admin/searchindex/sourcesearch/sourcesearch.jsp",
            params);
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(final String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        // create export file name block
        result.append(createWidgetBlockStart(key(org.opencms.workplace.tools.searchindex.Messages.GUI_SOURCESEARCH_ADMIN_TOOL_BLOCK_0)));

        result.append(createDialogRowsHtml(0, 4));

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

        // initialize the settings object to use for the dialog
        initSettingsObject();

        setKeyPrefix(KEY_PREFIX);
        List<CmsSelectWidgetOption> options = getProjectSelections();

        addWidget(new CmsWidgetDialogParameter(
            m_settings,
            "message",
            key(Messages.GUI_SOURCESEARCH_SELECTFOLDER_DIALOG_MESSAGE_0),
            PAGES[0],
            new CmsDisplayWidget(),
            1,
            1));
        addWidget(new CmsWidgetDialogParameter(m_settings, "paths", "/", PAGES[0], new CmsVfsFileWidget(
            false,
            getCms().getRequestContext().getSiteRoot()), 1, CmsWidgetDialogParameter.MAX_OCCURENCES));
        addWidget(new CmsWidgetDialogParameter(m_settings, "searchpattern", "", PAGES[0], new CmsInputWidget(), 1, 1));
        addWidget(new CmsWidgetDialogParameter(m_settings, "replacepattern", "", PAGES[0], new CmsInputWidget(), 1, 1));
        addWidget(new CmsWidgetDialogParameter(
            m_settings,
            "project",
            getCms().getRequestContext().getCurrentProject().getName(),
            PAGES[0],
            new CmsSelectWidget(options),
            1,
            1));
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
     * Initializes the settings object to work with depending on the dialog state and request
     * parameters.
     * <p>
     */
    protected void initSettingsObject() {

        Object o;
        if (CmsStringUtil.isEmpty(getParamAction())) {
            o = new CmsSearchReplaceSettings();
        } else {
            // this is not the initial call, get the job object from session
            o = getDialogObject();
        }

        if (o == null) {
            // create a new export handler object
            m_settings = new CmsSearchReplaceSettings();
        } else {
            // reuse export handler object stored in session
            m_settings = (CmsSearchReplaceSettings)o;
        }

    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings,
     *      javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(final CmsWorkplaceSettings settings, final HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the export handler (may be changed because of the widget
        // values)
        setDialogObject(m_settings);
    }

    /**
     * Returns a list with the available projects of the current user.
     * <p>
     * 
     * @return a list with the available projects of the current user.
     */
    private List<CmsSelectWidgetOption> getProjectSelections() {

        List<CmsSelectWidgetOption> result = new LinkedList<CmsSelectWidgetOption>();
        List<CmsProject> projects = null;
        try {
            projects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(getCms(), "", true);
        } catch (CmsException e) {
            return result;
        }

        CmsSelectWidgetOption option;
        boolean first = true;
        for (CmsProject project : projects) {
            if (!project.getName().equals("Online")) {
                option = new CmsSelectWidgetOption(project.getName(), first, project.getName());
                first = false;
                result.add(option);
            }
        }
        return result;
    }
}

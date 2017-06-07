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

package org.opencms.workplace.tools.database;

import org.opencms.importexport.CmsExportParameters;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsComboWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Widget dialog that sets the export options to export VFS resources to the OpenCms server.<p>
 *
 * @since 6.0.0
 */
public class CmsDatabaseExportDialog extends CmsWidgetDialog {

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The import JSP report workplace URI. */
    protected static final String EXPORT_ACTION_REPORT = PATH_WORKPLACE + "admin/database/reports/export.jsp";

    /** The export parameters object that is edited on this dialog. */
    private CmsExportParameters m_exportParams;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsDatabaseExportDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDatabaseExportDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        List errors = new ArrayList();
        // create absolute RFS path and store it in dialog object
        String exportFileName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            OpenCms.getSystemInfo().getPackagesRfsPath() + File.separator + m_exportParams.getPath());
        m_exportParams.setPath(exportFileName);
        setDialogObject(m_exportParams);
        Map params = new HashMap();
        // set the name of this class to get dialog object in report
        params.put(CmsDatabaseExportReport.PARAM_CLASSNAME, this.getClass().getName());
        // set style to display report in correct layout
        params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
        // set close link to get back to overview after finishing the import
        params.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/database"));
        // redirect to the report output JSP
        getToolManager().jspForwardPage(this, EXPORT_ACTION_REPORT, params);
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
        result.append(createWidgetBlockStart(key(Messages.GUI_DATABASE_EXPORT_FILE_BLOCK_0)));
        result.append(createDialogRowsHtml(0, 0));
        result.append(createWidgetBlockEnd());

        // create export data type block
        result.append(createWidgetBlockStart(key(Messages.GUI_DATABASE_EXPORT_TYPES_BLOCK_0)));
        result.append(createDialogRowsHtml(1, 3));
        result.append(createWidgetBlockEnd());

        // create export settings block
        result.append(createWidgetBlockStart(key(Messages.GUI_DATABASE_EXPORT_SETTINGS_BLOCK_0)));
        result.append(createDialogRowsHtml(4, 9));
        result.append(createWidgetBlockEnd());

        // create export resource(s) block
        result.append(createWidgetBlockStart(key(Messages.GUI_DATABASE_EXPORT_RESOURCES_BLOCK_0)));
        result.append(createDialogRowsHtml(10, 10));
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

        // initialize the sexport object to use for the dialog
        initDatabaseExportObject();

        List exportFiles = getComboExportFiles();
        if (exportFiles.isEmpty()) {
            // no export files available, display text input field
            addWidget(new CmsWidgetDialogParameter(m_exportParams, "path", PAGES[0], new CmsInputWidget()));
        } else {
            // one or more export files present, create combo widget
            addWidget(new CmsWidgetDialogParameter(m_exportParams, "path", PAGES[0], new CmsComboWidget(exportFiles)));
        }

        addWidget(
            new CmsWidgetDialogParameter(m_exportParams, "exportResourceData", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_exportParams, "exportAccountData", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_exportParams, "exportProjectData", PAGES[0], new CmsCheckboxWidget()));

        addWidget(
            new CmsWidgetDialogParameter(
                m_exportParams,
                "includeUnchangedResources",
                PAGES[0],
                new CmsCheckboxWidget()));
        addWidget(
            new CmsWidgetDialogParameter(m_exportParams, "includeSystemFolder", PAGES[0], new CmsCheckboxWidget()));
        addWidget(
            new CmsWidgetDialogParameter(m_exportParams, "contentAge", "0", PAGES[0], new CmsCalendarWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_exportParams, "recursive", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_exportParams, "inProject", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_exportParams, "exportAsFiles", PAGES[0], new CmsCheckboxWidget()));

        addWidget(
            new CmsWidgetDialogParameter(
                m_exportParams,
                "resources",
                "/",
                PAGES[0],
                new CmsVfsFileWidget(false, getCms().getRequestContext().getSiteRoot()),
                1,
                CmsWidgetDialogParameter.MAX_OCCURENCES));

    }

    /**
     * Returns the present export files on the server to show in the combo box.<p>
     *
     * The result list elements are of type <code>{@link org.opencms.widgets.CmsSelectWidgetOption}</code>.<p>
     *
     * @return the present export files on the server to show in the combo box
     */
    protected List getComboExportFiles() {

        List result = new ArrayList(8);
        Iterator i = CmsDatabaseImportFromServer.getFileListFromServer(true).iterator();
        while (i.hasNext()) {
            String fileName = (String)i.next();
            String helpText = key(Messages.GUI_EDITOR_HELP_EXPORTFILE_1, new String[] {fileName});
            result.add(new CmsSelectWidgetOption(fileName, false, null, helpText));
        }
        return result;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the import/export object to work with depending on the dialog state and request parameters.<p>
     */
    protected void initDatabaseExportObject() {

        Object o;

        if (CmsStringUtil.isEmpty(getParamAction())) {
            o = new CmsExportParameters();
        } else {
            // this is not the initial call, get the job object from session
            o = getDialogObject();
        }

        if (!(o instanceof CmsExportParameters)) {
            // create a new export parameters object
            m_exportParams = new CmsExportParameters();
        } else {
            // reuse export parameters object stored in session
            m_exportParams = (CmsExportParameters)o;
        }

        if (CmsStringUtil.isEmpty(getParamAction()) && (m_exportParams.getResources().size() < 1)) {
            // on initial call, at least on resource input field has to be present
            List initialPaths = new ArrayList(1);
            initialPaths.add("/");
            m_exportParams.setResources(initialPaths);
        }
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

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the export parameters (may be changed because of the widget values)
        setDialogObject(m_exportParams);
    }
}
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

package org.opencms.workplace.tools.history;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.CmsRadioSelectWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
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
 * Dialog to enter the settings to clear the history in the administration view.<p>
 *
 * @since 6.9.1
 */
public class CmsHistoryClearDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "histclear";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The import JSP report workplace URI. */
    protected static final String CLEAR_ACTION_REPORT = PATH_WORKPLACE + "admin/history/reports/clearhistory.jsp";

    /** The history clear object that is edited on this dialog. */
    protected CmsHistoryClear m_historyClear;

    /** Widget value. */
    private String m_clearDeletedMode = MODE_CLEANDELETED_KEEP_RESTORE_VERSION;

    /** Cleanup deleted history files setting. */
    public static final String MODE_CLEANDELETED_KEEP_RESTORE_VERSION = "keeprestore";

    /** Cleanup deleted history files setting. */
    public static final String MODE_CLEANDELETED_DELETE_ALL = "deleteall";

    /** Cleanup deleted history files setting. */
    public static final String MODE_CLEANDELETED_DELETE_NONE = "deletenone";

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsHistoryClearDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsHistoryClearDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        List errors = new ArrayList();
        setDialogObject(m_historyClear);

        if (m_historyClear.getKeepVersions() < 0) {
            errors.add(
                new CmsIllegalArgumentException(
                    Messages.get().container(Messages.GUI_HISTORY_CLEAR_INVALID_SETTINGS_0)));
        } else {

            Map params = new HashMap();

            // set the name of this class to get dialog object in report
            params.put(CmsHistoryClearReport.PARAM_CLASSNAME, this.getClass().getName());

            // set style to display report in correct layout
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);

            // set close link to get back to overview after finishing the import
            params.put(PARAM_CLOSELINK, CmsToolManager.linkForToolPath(getJsp(), "/history"));

            // redirect to the report output JSP
            getToolManager().jspForwardPage(this, CLEAR_ACTION_REPORT, params);
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Returns the cleanDeletedmode.<p>
     *
     * @return the clearDeletedmode
     */
    public final String getClearDeletedMode() {

        // m_clearDeletedMode will be null in initial display because constructor triggers this before member initialization
        String result = m_clearDeletedMode;
        if (result == null) {
            result = MODE_CLEANDELETED_KEEP_RESTORE_VERSION;
        }
        return result;
    }

    /**
     * Sets the cleanDeletedmode.<p>
     *
     * @param clearDeletedmode the cleanDeletedmode to set
     */
    public final void setClearDeletedMode(String clearDeletedmode) {

        m_clearDeletedMode = clearDeletedmode;
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

            // create the widgets for the first dialog page
            result.append(createWidgetBlockStart(key(Messages.GUI_HISTORY_CLEAR_BLOCK_LABEL_0)));

            Object versionStr = (OpenCms.getSystemInfo().getHistoryVersions() == -1)
            ? key(Messages.GUI_HISTORY_SETTINGS_VERSIONS_UNLIMITED_0)
            : String.valueOf(OpenCms.getSystemInfo().getHistoryVersions());
            result.append(key(Messages.GUI_HISTORY_CLEAR_VERSIONINFO_1, new Object[] {versionStr}));
            result.append("<p>");

            result.append(createDialogRowsHtml(0, 2));
            result.append(createWidgetBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initHistoryClearObject();
        setKeyPrefix(KEY_PREFIX);

        addWidget(
            new CmsWidgetDialogParameter(m_historyClear, "keepVersions", PAGES[0], new CmsSelectWidget(getVersions())));
        addWidget(new CmsWidgetDialogParameter(
            m_historyClear,
            "clearDeletedMode",
            PAGES[0],
            new CmsRadioSelectWidget(getClearDeletedModes())));
        addWidget(new CmsWidgetDialogParameter(m_historyClear, "clearOlderThan", PAGES[0], new CmsCalendarWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes this widget dialog's object.<p>
     */
    protected void initHistoryClearObject() {

        Object o;

        if (CmsStringUtil.isEmpty(getParamAction())) {
            o = new CmsHistoryClear();
        } else {
            // this is not the initial call, get the job object from session
            o = getDialogObject();
        }

        if (!(o instanceof CmsHistoryClear)) {

            // create a new history settings handler object
            m_historyClear = new CmsHistoryClear();
        } else {

            // reuse html import handler object stored in session
            m_historyClear = (CmsHistoryClear)o;
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
     * Returns a list with the possible modes for the clean deleted action.<p>
     *
     * @return a list with the possible modes for the clean deleted action
     */
    private List getClearDeletedModes() {

        ArrayList ret = new ArrayList();

        ret.add(
            new CmsSelectWidgetOption(
                MODE_CLEANDELETED_KEEP_RESTORE_VERSION,
                getClearDeletedMode().equals(MODE_CLEANDELETED_KEEP_RESTORE_VERSION),
                key(Messages.GUI_HISTORY_CLEAR_DELETED_KEEPRESTORE_0)));
        ret.add(new CmsSelectWidgetOption(
            MODE_CLEANDELETED_DELETE_ALL,
            getClearDeletedMode().equals(MODE_CLEANDELETED_DELETE_ALL),
            key(Messages.GUI_HISTORY_CLEAR_DELETED_DELETEALL_0)));
        ret.add(new CmsSelectWidgetOption(
            MODE_CLEANDELETED_DELETE_NONE,
            getClearDeletedMode().equals(MODE_CLEANDELETED_DELETE_NONE),
            key(Messages.GUI_HISTORY_CLEAR_DELETED_DELETENONE_0)));

        return ret;
    }

    /**
     * Returns a list with the possible versions to choose from.<p>
     *
     * @return a list with the possible versions to choose from
     */
    private List getVersions() {

        ArrayList ret = new ArrayList();

        int defaultHistoryVersions = OpenCms.getSystemInfo().getHistoryVersions();
        int historyVersions = 0;

        // Add the option for disabled version history
        ret.add(
            new CmsSelectWidgetOption(
                "-1",
                true,
                Messages.get().getBundle().key(Messages.GUI_HISTORY_CLEAR_VERSION_SELECT_0)));

        // Iterate from 1 to 50 with a stepping of 1 for the first 10 entries and a stepping of five for the entries from 10 to 50
        while (historyVersions < 50) {

            // increment the history version
            historyVersions++;

            if (((historyVersions % 5) == 0) || (historyVersions <= 10)) {

                ret.add(
                    new CmsSelectWidgetOption(String.valueOf(historyVersions), false, String.valueOf(historyVersions)));
            }
        }

        // If the default setting for the version history is more than 50
        if (defaultHistoryVersions > historyVersions) {
            ret.add(
                new CmsSelectWidgetOption(
                    String.valueOf(defaultHistoryVersions),
                    false,
                    String.valueOf(defaultHistoryVersions)));
        }

        return ret;
    }

}

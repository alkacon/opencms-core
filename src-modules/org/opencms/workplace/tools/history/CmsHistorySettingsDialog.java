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

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsRadioSelectWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to enter the settings for the history in the administration view.<p>
 *
 * @since 6.9.1
 */
public class CmsHistorySettingsDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "histsettings";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The history settings object that is edited on this dialog. */
    protected CmsHistorySettings m_historySettings;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsHistorySettingsDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsHistorySettingsDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        List errors = new ArrayList();
        setDialogObject(m_historySettings);

        boolean enabled = m_historySettings.getVersions() > -2;
        int versions = m_historySettings.getVersions();

        int versionsDeleted = 0;
        switch (m_historySettings.getMode()) {
            case CmsHistorySettings.MODE_DELETED_HISTORY_DISABLED:
                versionsDeleted = 0;
                break;
            case CmsHistorySettings.MODE_DELETED_HISTORY_KEEP_NO_VERSIONS:
                versionsDeleted = 1;
                break;
            case CmsHistorySettings.MODE_DELETED_HISTORY_KEEP_WITH_VERSIONS:
                if (enabled) {
                    versionsDeleted = versions;
                } else {
                    errors.add(
                        new CmsIllegalArgumentException(
                            Messages.get().container(Messages.GUI_HISTORY_SETTINGS_INVALID_0)));
                }
                break;
            default:
                versionsDeleted = 0;
        }

        OpenCms.getSystemInfo().setVersionHistorySettings(enabled, versions, versionsDeleted);
        OpenCms.writeConfiguration(CmsSystemConfiguration.class);

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
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
            result.append(createWidgetBlockStart(key(Messages.GUI_HISTORY_SETTINGS_BLOCK_LABEL_0)));
            result.append(createDialogRowsHtml(0, 1));
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

        initHistorySettingsObject();
        setKeyPrefix(KEY_PREFIX);

        addWidget(
            new CmsWidgetDialogParameter(m_historySettings, "versions", PAGES[0], new CmsSelectWidget(getVersions())));
        addWidget(
            new CmsWidgetDialogParameter(m_historySettings, "mode", PAGES[0], new CmsRadioSelectWidget(getModes())));
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
    protected void initHistorySettingsObject() {

        Object o;

        if (CmsStringUtil.isEmpty(getParamAction())) {
            o = new CmsHistorySettings();
        } else {
            // this is not the initial call, get the job object from session
            o = getDialogObject();
        }

        if (!(o instanceof CmsHistorySettings)) {

            // create a new history settings handler object
            m_historySettings = new CmsHistorySettings();
        } else {

            // reuse html import handler object stored in session
            m_historySettings = (CmsHistorySettings)o;
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
     * Returns a list with the possible modes for the history to keep.<p>
     *
     * @return a list with the possible modes for the history to keep
     */
    private List getModes() {

        ArrayList ret = new ArrayList();

        ret.add(
            new CmsSelectWidgetOption(
                String.valueOf(CmsHistorySettings.MODE_DELETED_HISTORY_DISABLED),
                m_historySettings.getMode() == CmsHistorySettings.MODE_DELETED_HISTORY_DISABLED,
                key(Messages.GUI_HISTORY_SETTINGS_MODE_DISABLED_0)));
        ret.add(new CmsSelectWidgetOption(
            String.valueOf(CmsHistorySettings.MODE_DELETED_HISTORY_KEEP_NO_VERSIONS),
            m_historySettings.getMode() == CmsHistorySettings.MODE_DELETED_HISTORY_KEEP_NO_VERSIONS,
            key(Messages.GUI_HISTORY_SETTINGS_MODE_KEEP_NO_VERSIONS_0)));
        ret.add(new CmsSelectWidgetOption(
            String.valueOf(CmsHistorySettings.MODE_DELETED_HISTORY_KEEP_WITH_VERSIONS),
            m_historySettings.getMode() == CmsHistorySettings.MODE_DELETED_HISTORY_KEEP_WITH_VERSIONS,
            key(Messages.GUI_HISTORY_SETTINGS_MODE_KEEP_WITH_VERSIONS_0)));

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
                String.valueOf(-2),
                defaultHistoryVersions == -2,
                key(Messages.GUI_HISTORY_SETTINGS_VERSIONS_DISABLED_0)));

        // Iterate from 1 to 50 with a stepping of 1 for the first 10 entries and a stepping of five for the entries from 10 to 50
        while (historyVersions < 50) {

            // increment the history version
            historyVersions++;

            if (((historyVersions % 5) == 0) || (historyVersions <= 10)) {

                boolean defaultValue = defaultHistoryVersions == historyVersions;
                ret.add(
                    new CmsSelectWidgetOption(
                        String.valueOf(historyVersions),
                        defaultValue,
                        String.valueOf(historyVersions)));
            }
        }

        // If the default setting for the version history is more than 50
        if (defaultHistoryVersions > historyVersions) {
            ret.add(
                new CmsSelectWidgetOption(
                    String.valueOf(defaultHistoryVersions),
                    true,
                    String.valueOf(defaultHistoryVersions)));
        }

        // Add the option for unlimited version history
        ret.add(
            new CmsSelectWidgetOption(
                String.valueOf(-1),
                defaultHistoryVersions == -1,
                key(Messages.GUI_HISTORY_SETTINGS_VERSIONS_UNLIMITED_0)));

        return ret;
    }

}

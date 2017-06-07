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

package org.opencms.workplace.tools.workplace;

import org.opencms.db.CmsUserSettings;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.synchronize.CmsSynchronizeSettings;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit the synchronize settings of the OpenCms Workplace.<p>
 *
 * @since 6.0.0
 */
public class CmsSynchronizeSettingsDialog extends CmsWidgetDialog {

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "sync";

    /** The synchronize settings which are edited on this dialog. */
    private CmsSynchronizeSettings m_synchronizeSettings;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSynchronizeSettingsDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSynchronizeSettingsDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited synchronize settings to the user settings.<p>
     */
    @Override
    public void actionCommit() {

        List<Throwable> errors = new ArrayList<Throwable>();

        try {
            // set the synchronize settings
            CmsUserSettings userSettings = new CmsUserSettings(getCms());
            m_synchronizeSettings.checkValues(getCms());
            userSettings.setSynchronizeSettings(m_synchronizeSettings);
            userSettings.save(getCms());
            setDialogObject(null);
        } catch (Throwable t) {
            errors.add(t);
        }

        // set the list of errors to display when saving failed
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
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create widget table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        // create the widgets for the first dialog page
        result.append(dialogBlockStart(key(Messages.GUI_EDITOR_LABEL_ACTIVATE_SYNC_BLOCK_0)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(0, 0));
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());
        result.append(dialogBlockStart(key(Messages.GUI_EDITOR_LABEL_SOURCE_LIST_VFS_BLOCK_0)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(1, 1));
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());
        result.append(dialogBlockStart(key(Messages.GUI_EDITOR_LABEL_DESTINATION_RFS_BLOCK_0)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(2, 2));
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());

        // close widget table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        // initialize the object to use for the dialog
        initSynchronizeSettingsObject();
        setKeyPrefix(KEY_PREFIX);
        addWidget(new CmsWidgetDialogParameter(m_synchronizeSettings, "enabled", PAGES[0], new CmsCheckboxWidget()));
        addWidget(
            new CmsWidgetDialogParameter(
                m_synchronizeSettings,
                "destinationPathInRfs",
                PAGES[0],
                new CmsInputWidget()));
        addWidget(
            new CmsWidgetDialogParameter(
                m_synchronizeSettings,
                "sourceListInVfs",
                "/",
                PAGES[0],
                new CmsVfsFileWidget(false, ""),
                1,
                CmsWidgetDialogParameter.MAX_OCCURENCES));

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
     * Initializes the synchronize settings object for this dialog.<p>
     */
    protected void initSynchronizeSettingsObject() {

        Object o = getDialogObject();

        if ((o == null) || !(o instanceof CmsSynchronizeSettings)) {
            CmsUserSettings userSettings = new CmsUserSettings(getCms());
            o = userSettings.getSynchronizeSettings();
        }

        if (o != null) {
            m_synchronizeSettings = (CmsSynchronizeSettings)o;
        } else {
            m_synchronizeSettings = new CmsSynchronizeSettings();
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current synchronize settings (may be changed because of the widget values)
        setDialogObject(m_synchronizeSettings);
    }

}
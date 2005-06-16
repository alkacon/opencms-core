/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/Attic/CmsProjectSettingsDialog.java,v $
 * Date   : $Date: 2005/06/16 10:55:02 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.projects;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsProjectFilesModeEnum;
import org.opencms.file.CmsUserProjectSettings;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Comment for <code>CmsProjectSettingsDialog</code>.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.2 $
 * @since 5.7.3
 */
public class CmsProjectSettingsDialog extends A_CmsProjectDialog {

    /** localized messages Keys prefix. */
    public static final String C_KEY_PREFIX = "settings";

    /** aux property for mapping the projectFilesMode property of an user project settings object. */
    private String m_mode;

    /** bean for edition of the project settings. */
    private CmsUserProjectSettings m_prjSettings;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsProjectSettingsDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsProjectSettingsDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited project to the db.<p>
     */
    public void actionCommit() {

        List errors = new ArrayList();
        try {
            m_prjSettings.setManagerGroup(getCms().readGroup(getManagerGroup()).getId());
            m_prjSettings.setUserGroup(getCms().readGroup(getUserGroup()).getId());
            m_prjSettings.setProjectFilesMode(CmsProjectFilesModeEnum.valueOf(getMode()));
            CmsUserSettings settings = new CmsUserSettings(getCms().getRequestContext().currentUser());
            settings.setProjectSettings(m_prjSettings);
            settings.save(getCms());
        } catch (Throwable t) {
            errors.add(t);
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Returns the project files mode.<p>
     *
     * @return the mode
     */
    public String getMode() {

        return m_mode;
    }

    /**
     * Sets the project files mode.<p>
     *
     * @param mode the mode to set
     */
    public void setMode(String mode) {

        CmsProjectFilesModeEnum.valueOf(mode);
        m_mode = mode;
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
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_SETTINGS_EDITOR_LABEL_NEWPROJECT_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 2));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_SETTINGS_EDITOR_LABEL_PROJECTFILES_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(3, 3));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        // initialize the project object to use for the dialog
        initSettingsObject();

        setKeyPrefix(C_KEY_PREFIX);

        // widgets to display
        addWidget(new CmsWidgetDialogParameter(this, "managerGroup", PAGES[0], new CmsSelectWidget(
            getSelectGroups(true))));
        addWidget(new CmsWidgetDialogParameter(this, "userGroup", PAGES[0], new CmsSelectWidget(getSelectGroups(false))));
        addWidget(new CmsWidgetDialogParameter(m_prjSettings, "deleteAfterPublishing", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "mode", PAGES[0], new CmsSelectWidget(getSelectModes())));
    }

    /**
     * Initializes the project settings object to work with depending on the dialog state and request parameters.<p>
     * 
     * Two initializations of the settings object on first dialog call are possible:
     * <ul>
     * <li>edit existing settings</li>
     * <li>create new settings</li>
     * </ul>
     */
    protected void initSettingsObject() {

        Object o = null;

        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // edit an existing settings, get the setting object from db
                CmsUserSettings settings = new CmsUserSettings(getCms().getRequestContext().currentUser());
                m_prjSettings = settings.getProjectSettings();
            } else {
                // this is not the initial call, get the setting object from session            
                o = getDialogObject();
                m_prjSettings = (CmsUserProjectSettings)o;
            }
            // test
            m_prjSettings.getProjectFilesMode();
        } catch (Exception e) {
            // create a new settings object
            m_prjSettings = new CmsUserProjectSettings();
        }
        try {
            setManagerGroup(getCms().readGroup(m_prjSettings.getManagerGroup()).getName());
        } catch (Exception e) {
            // ignore
        }
        try {
            setUserGroup(getCms().readGroup(m_prjSettings.getUserGroup()).getName());
        } catch (Exception e) {
            // ignore
        }
        try {
            setMode(m_prjSettings.getProjectFilesMode().toString());
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Returns all diferent project file selection modes.<p>
     * 
     * @return a list of modes
     */
    private List getSelectModes() {

        List retVal = new ArrayList();
        retVal.add(new CmsSelectWidgetOption(CmsProjectFilesModeEnum.ALL_CHANGES.getMode(), true, Messages.get().key(
            getLocale(),
            Messages.GUI_PROJECT_MODE_ALLCHANGES_0,
            null)));
        retVal.add(new CmsSelectWidgetOption(CmsProjectFilesModeEnum.NEW_FILES.getMode(), false, Messages.get().key(
            getLocale(),
            Messages.GUI_PROJECT_MODE_NEWFILES_0,
            null)));
        retVal.add(new CmsSelectWidgetOption(
            CmsProjectFilesModeEnum.MODIFIED_FILES.getMode(),
            false,
            Messages.get().key(getLocale(), Messages.GUI_PROJECT_MODE_MODFILES_0, null)));
        retVal.add(new CmsSelectWidgetOption(
            CmsProjectFilesModeEnum.DELETED_FILES.getMode(),
            false,
            Messages.get().key(getLocale(), Messages.GUI_PROJECT_MODE_DELFILES_0, null)));
        return retVal;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the settings (may be changed because of the widget values)
        setDialogObject(m_prjSettings);
    }
}

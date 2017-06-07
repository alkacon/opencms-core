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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsGroup;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsGroupWidget;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;

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
 * Dialog to export user data.<p>
 *
 * @since 6.7.1
 */
public class CmsUserDataExportDialog extends A_CmsUserDataImexportDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "userdata.export";

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsUserDataExportDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUserDataExportDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserDataImexportDialog#actionCommit()
     */
    @Override
    public void actionCommit() throws IOException, ServletException {

        List<Throwable> errors = new ArrayList<Throwable>();

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(A_CmsOrgUnitDialog.PARAM_OUFQN, new String[] {getParamOufqn()});
        params.put(CmsDialog.PARAM_CLOSELINK, new String[] {getParamCloseLink()});
        params.put(CmsToolDialog.PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
        getToolManager().jspForwardPage(this, getDownloadPath(), params);
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

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_USERDATA_EXPORT_LABEL_HINT_BLOCK_0)));
            result.append(key(Messages.GUI_USERDATA_EXPORT_LABEL_HINT_TEXT_0));
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_USERDATA_EXPORT_LABEL_GROUPS_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 0));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_USERDATA_EXPORT_LABEL_ROLES_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(1, 1));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserDataImexportDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        initExportObject();
        setKeyPrefix(KEY_PREFIX);

        addWidget(
            new CmsWidgetDialogParameter(this, "groups", PAGES[0], new CmsGroupWidget(null, null, getParamOufqn())));
        addWidget(new CmsWidgetDialogParameter(this, "roles", PAGES[0], new CmsSelectWidget(getSelectRoles())));
    }

    /**
     * Returns the download path.<p>
     *
     * @return the download path
     */
    protected String getDownloadPath() {

        return "/system/workplace/admin/accounts/imexport_user_data/dodownload.jsp";
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsUserDataImexportDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the message info object to work with depending on the dialog state and request parameters.<p>
     */
    @SuppressWarnings("unchecked")
    protected void initExportObject() {

        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // create a new list
                setGroups(new ArrayList<CmsGroup>());
                setRoles(new ArrayList<CmsRole>());
            } else {
                // this is not the initial call, get the message info object from session
                setGroups((List<CmsGroup>)((Map<?, ?>)getDialogObject()).get("groups"));
                setRoles((List<CmsRole>)((Map<?, ?>)getDialogObject()).get("roles"));
            }
        } catch (Exception e) {
            // create a new list
            setGroups(new ArrayList<CmsGroup>());
            setRoles(new ArrayList<CmsRole>());
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        HashMap<String, List<?>> objectsMap = new HashMap<String, List<?>>();
        objectsMap.put("groups", getGroups());
        objectsMap.put("roles", getRoles());

        // save the current state of the message (may be changed because of the widget values)
        setDialogObject(objectsMap);
    }
}

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

package org.opencms.workplace.tools.workplace.broadcast;

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.CmsHtmlList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Dialog to send a new email to the selected groups.<p>
 *
 * @since 6.5.6
 */
public class CmsSendPopupGroupsDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "message";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Parameter name constant. */
    public static final String PARAM_GROUPS = "groups";

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSendPopupGroupsDialog.class);

    /** Message info object. */
    protected CmsMessageInfo m_msgInfo;

    /** The selected groups. */
    private String m_paramGroups;

    /** The list of all members of the selected groups. */
    private List<CmsUser> m_users;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSendPopupGroupsDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSendPopupGroupsDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited project to the db.<p>
     */
    @Override
    public void actionCommit() {

        List<Throwable> errors = new ArrayList<Throwable>();

        if (getUsers().isEmpty()) {
            setCommitErrors(
                Collections.singletonList((Throwable)new CmsIllegalStateException(
                    Messages.get().container(Messages.ERR_NO_SELECTED_USER_WITH_EMAIL_0))));
            return;
        }
        try {
            Iterator<CmsUser> itUsers = getUsers().iterator();
            while (itUsers.hasNext()) {
                CmsUser user = itUsers.next();
                OpenCms.getSessionManager().sendBroadcast(
                    getCms().getRequestContext().getCurrentUser(),
                    m_msgInfo.getMsg(),
                    user);
            }
        } catch (Throwable t) {
            errors.add(t);
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Returns the selected groups.<p>
     *
     * @return the selected groups
     */
    public List<String> getGroups() {

        return CmsStringUtil.splitAsList(getParamGroups(), CmsHtmlList.ITEM_SEPARATOR);
    }

    /**
     * Returns the list of selected groups.<p>
     *
     * @return the list of selected groups
     */
    public String getParamGroups() {

        return m_paramGroups;
    }

    /**
     * Sets the list of selected groups.<p>
     *
     * @param paramGroups the list of selected groups to set
     */
    public void setParamGroups(String paramGroups) {

        m_paramGroups = paramGroups;
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
            // create the widgets for the second dialog page
            result.append(dialogBlockStart(key(Messages.GUI_MESSAGE_EDITOR_LABEL_HEADER_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 1));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_MESSAGE_EDITOR_LABEL_CONTENT_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(2, 2));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        // initialize the project object to use for the dialog
        initMessageObject();

        setKeyPrefix(KEY_PREFIX);

        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "from", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "to", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "msg", PAGES[0], new CmsTextareaWidget(12)));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Returns a semicolon separated list of user names.<p>
     *
     * @return a semicolon separated list of user names
     */
    protected String getToNames() {

        StringBuffer result = new StringBuffer(256);
        Iterator<CmsUser> itUsers = getUsers().iterator();
        while (itUsers.hasNext()) {
            CmsUser user = itUsers.next();
            result.append(user.getFullName());
            if (itUsers.hasNext()) {
                result.append("; ");
            }
        }
        return result.toString();
    }

    /**
     * Initializes the message info object to work with depending on the dialog state and request parameters.<p>
     */
    protected void initMessageObject() {

        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // create a new message info object
                m_msgInfo = new CmsMessageInfo();
            } else {
                // this is not the initial call, get the message info object from session
                m_msgInfo = (CmsMessageInfo)getDialogObject();
            }
        } catch (Exception e) {
            // create a new message info object
            m_msgInfo = new CmsMessageInfo();
        }
        m_msgInfo.setFrom(getCms().getRequestContext().getCurrentUser().getFullName());
        m_msgInfo.setTo(getToNames());
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        addMessages(org.opencms.workplace.tools.workplace.Messages.get().getBundleName());
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

        // save the current state of the message (may be changed because of the widget values)
        setDialogObject(m_msgInfo);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        if ((getUsers() == null) || getUsers().isEmpty()) {
            throw new Exception();
        }
    }

    /**
     * Returns a list of all members of the selected groups.<p>
     *
     * @return a list of user objects
     */
    private List<CmsUser> getUsers() {

        if (m_users == null) {
            m_users = new ArrayList<CmsUser>();
            List<CmsUser> manageableUsers = new ArrayList<CmsUser>();
            try {
                manageableUsers = OpenCms.getRoleManager().getManageableUsers(getCms(), "", true);
            } catch (CmsException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            Iterator<String> itGroups = getGroups().iterator();
            while (itGroups.hasNext()) {
                String groupName = itGroups.next();
                try {
                    Iterator<CmsUser> itUsers = getCms().getUsersOfGroup(groupName, true).iterator();
                    while (itUsers.hasNext()) {
                        CmsUser user = itUsers.next();
                        if (OpenCms.getSessionManager().getSessionInfos(user.getId()).isEmpty()) {
                            continue;
                        }
                        if (!manageableUsers.contains(user)) {
                            continue;
                        }
                        m_users.add(user);
                    }
                } catch (CmsException e) {
                    // should never happen
                }
            }
        }
        return m_users;
    }
}
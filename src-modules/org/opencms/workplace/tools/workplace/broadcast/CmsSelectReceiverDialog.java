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
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsGroupWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.tools.CmsToolDialog;
import org.opencms.workplace.tools.CmsToolManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to select the receiver of a new message.<p>
 *
 * @since 6.5.6
 */
public class CmsSelectReceiverDialog extends CmsWidgetDialog {

    /** Localized messages Keys prefix. */
    public static final String KEY_PREFIX = "select";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Parameter name constant. */
    public static final String PARAM_MSGTYPE = "msgtype";

    /** Message type value constant. */
    public static final String MSGTYPE_EMAIL = "email";

    /** Message type value constant. */
    public static final String MSGTYPE_POPUP = "popup";

    /** The selected groups. */
    private List<String> m_groups;

    /** The message type parameter value. */
    private String m_paramMsgtype;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSelectReceiverDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSelectReceiverDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited project to the db.<p>
     */
    @Override
    public void actionCommit() {

        List<Throwable> errors = new ArrayList<Throwable>();

        boolean isEmail = (getParamMsgtype() != null) && getParamMsgtype().equals(MSGTYPE_EMAIL);

        if ((m_groups == null) || m_groups.isEmpty()) {
            setCommitErrors(
                Collections.singletonList((Throwable)new CmsIllegalStateException(
                    Messages.get().container(Messages.ERR_NO_SELECTED_GROUP_0))));
            return;
        }

        boolean hasUser = false;
        Iterator<String> itGroups = getGroups().iterator();
        while (!hasUser && itGroups.hasNext()) {
            String groupName = itGroups.next();
            try {
                Iterator<CmsUser> itUsers = getCms().getUsersOfGroup(groupName, true).iterator();
                while (!hasUser && itUsers.hasNext()) {
                    CmsUser user = itUsers.next();
                    if (!isEmail) {
                        if (!OpenCms.getSessionManager().getSessionInfos(user.getId()).isEmpty()) {
                            hasUser = true;
                        }
                    } else {
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(user.getEmail())) {
                            hasUser = true;
                        }
                    }
                }
            } catch (CmsException e) {
                // should never happen
            }
        }

        if (!hasUser) {
            setCommitErrors(
                Collections.singletonList((Throwable)new CmsIllegalStateException(
                    Messages.get().container(Messages.ERR_NO_SELECTED_RECEIVERS_0))));
            return;
        }

        try {
            Set<String> groups = new HashSet<String>(m_groups);
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(CmsToolDialog.PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});
            params.put(
                CmsSendEmailGroupsDialog.PARAM_GROUPS,
                new String[] {CmsStringUtil.collectionAsString(groups, CmsHtmlList.ITEM_SEPARATOR)});
            params.put(
                CmsDialog.PARAM_CLOSELINK,
                new String[] {CmsToolManager.linkForToolPath(getJsp(), "/workplace/broadcast")});

            if (isEmail) {
                getToolManager().jspForwardPage(
                    this,
                    "/system/workplace/admin/workplace/groups_send_email.jsp",
                    params);
            } else {
                getToolManager().jspForwardPage(
                    this,
                    "/system/workplace/admin/workplace/groups_send_popup.jsp",
                    params);
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

        return m_groups;
    }

    /**
     * Returns the message type parameter value.<p>
     *
     * @return the message type parameter value
     */
    public String getParamMsgtype() {

        return m_paramMsgtype;
    }

    /**
     * Sets the selected groups.<p>
     *
     * @param groups the selected groups to set
     */
    public void setGroups(List<String> groups) {

        m_groups = groups;
    }

    /**
     * Sets the message type parameter value.<p>
     *
     * @param paramMsgtype the message type to set
     */
    public void setParamMsgtype(String paramMsgtype) {

        m_paramMsgtype = paramMsgtype;
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
            result.append(dialogBlockStart(key(Messages.GUI_SELECT_EDITOR_LABEL_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 0));
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

        addWidget(new CmsWidgetDialogParameter(this, "groups", "", PAGES[0], new CmsGroupWidget(), 0, 10));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the message info object to work with depending on the dialog state and request parameters.<p>
     */
    @SuppressWarnings("unchecked")
    protected void initMessageObject() {

        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // create a new list
                m_groups = new ArrayList<String>();
            } else {
                // this is not the initial call, get the message info object from session
                m_groups = (List<String>)getDialogObject();
            }
        } catch (Exception e) {
            // create a new list
            m_groups = new ArrayList<String>();
        }
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
        setDialogObject(m_groups);
    }

}
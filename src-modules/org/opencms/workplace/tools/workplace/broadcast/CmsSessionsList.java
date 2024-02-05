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
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListMultiAction;
import org.opencms.workplace.list.CmsListOrderEnum;
import org.opencms.workplace.list.CmsListTimeIntervalFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Session list for broadcasting messages.<p>
 *
 * @since 6.0.0
 */
public class CmsSessionsList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_MESSAGE = "am";

    /** list action id constant. */
    public static final String LIST_ACTION_PENDING_DISABLED = "apd";

    /** list action id constant. */
    public static final String LIST_ACTION_PENDING_ENABLED = "ape";

    /** list column id constant. */
    public static final String LIST_COLUMN_CREATION = "cc";

    /** list column id constant. */
    public static final String LIST_COLUMN_INACTIVE = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_MESSAGE = "cm";

    /** list column id constant. */
    public static final String LIST_COLUMN_ORGUNIT = "cou";

    /** list column id constant. */
    public static final String LIST_COLUMN_PENDING = "cp";

    /** list column id constant. */
    public static final String LIST_COLUMN_PROJECT = "cj";

    /** list column id constant. */
    public static final String LIST_COLUMN_SITE = "cs";

    /** list column id constant. */
    public static final String LIST_COLUMN_USER = "cu";

    /** list action id constant. */
    public static final String LIST_DEFACTION_MESSAGE = "dm";

    /** list column id constant. */
    public static final String LIST_DETAIL_EMAIL = "de";

    /** list id constant. */
    public static final String LIST_ID = "ls";

    /** list action id constant. */
    public static final String LIST_MACTION_EMAIL = "me";

    /** list multi action id. */
    public static final String LIST_MACTION_KILL_SESSION = "LIST_MACTION_KILL_SESSION";

    /** list action id constant. */
    public static final String LIST_MACTION_MESSAGE = "mm";

    /** Path to the list buttons. */
    public static final String PATH_BUTTONS = "tools/workplace/buttons/";

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSessionsList(CmsJspActionElement jsp) {

        super(
            jsp,
            LIST_ID,
            Messages.get().container(Messages.GUI_SESSIONS_LIST_NAME_0),
            LIST_COLUMN_USER,
            CmsListOrderEnum.ORDER_ASCENDING,
            LIST_COLUMN_USER);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSessionsList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() throws IOException, ServletException {

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(A_CmsMessageDialog.PARAM_SESSIONIDS, new String[] {getParamSelItems()});
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});

        if (getParamListAction().equals(LIST_MACTION_MESSAGE)) {
            // execute the send message multiaction
            // forward to the edit message screen
            getToolManager().jspForwardTool(this, "/workplace/broadcast/message", params);
        } else if (getParamListAction().equals(LIST_MACTION_EMAIL)) {
            // execute the send email multiaction
            // forward to the edit email screen
            getToolManager().jspForwardTool(this, "/workplace/broadcast/email", params);
        } else if (getParamListAction().equals(LIST_MACTION_KILL_SESSION)) {
            List<String> selectedItems = CmsStringUtil.splitAsList(getParamSelItems(), "|");
            for (String selectedItem : selectedItems) {
                try {
                    OpenCms.getSessionManager().killSession(getCms(), new CmsUUID(selectedItem));
                } catch (CmsException e) {
                    throw new CmsRuntimeException(e.getMessageContainer(), e);
                }
            }
        } else {
            throwListUnsupportedActionException();
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() throws IOException, ServletException {

        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(A_CmsMessageDialog.PARAM_SESSIONIDS, new String[] {getSelectedItem().getId()});
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});

        if (getParamListAction().equals(LIST_ACTION_MESSAGE) || getParamListAction().equals(LIST_DEFACTION_MESSAGE)) {
            getToolManager().jspForwardTool(this, "/workplace/broadcast/message", params);
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List<CmsListItem> sessions = getList().getAllContent();
        Iterator<CmsListItem> i = sessions.iterator();
        while (i.hasNext()) {
            CmsListItem item = i.next();
            CmsSessionInfo session = OpenCms.getSessionManager().getSessionInfo(new CmsUUID(item.getId()));
            StringBuffer html = new StringBuffer(32);
            if (detailId.equals(LIST_DETAIL_EMAIL)) {
                // email
                try {
                    CmsUser user = getCms().readUser(session.getUserId());
                    html.append(user.getEmail());
                } catch (CmsException e) {
                    // should never happen
                }
            } else {
                continue;
            }
            item.set(detailId, html.toString());
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() throws CmsException {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();
        // get content
        List<CmsSessionInfo> sessionInfos = OpenCms.getSessionManager().getSessionInfos();
        Iterator<CmsSessionInfo> itSessions = sessionInfos.iterator();
        List<CmsOrganizationalUnit> manageableOus = OpenCms.getRoleManager().getManageableOrgUnits(
            getCms(),
            "",
            true,
            false);
        while (itSessions.hasNext()) {
            CmsSessionInfo sessionInfo = itSessions.next();
            CmsListItem item = getList().newItem(sessionInfo.getSessionId().toString());
            CmsUser user = getCms().readUser(sessionInfo.getUserId());
            CmsOrganizationalUnit userOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(
                getCms(),
                user.getOuFqn());
            if (!(manageableOus.contains(userOu) && !user.isWebuser())) {
                continue;
            }
            item.set(LIST_COLUMN_USER, user.getFullName());
            item.set(LIST_COLUMN_ORGUNIT, userOu.getDisplayName(getLocale()));
            item.set(LIST_COLUMN_CREATION, new Date(sessionInfo.getTimeCreated()));
            item.set(LIST_COLUMN_INACTIVE, Long.valueOf(System.currentTimeMillis() - sessionInfo.getTimeUpdated()));
            try {
                item.set(LIST_COLUMN_PROJECT, getCms().readProject(sessionInfo.getProject()).getName());
            } catch (Exception e) {
                // ignore
            }
            item.set(LIST_COLUMN_SITE, sessionInfo.getSiteRoot());
            ret.add(item);
        }

        // hide ou column if only one ou exists
        try {
            if (OpenCms.getOrgUnitManager().getOrganizationalUnits(getCms(), "", true).isEmpty()) {
                getList().getMetadata().getColumnDefinition(LIST_COLUMN_ORGUNIT).setVisible(false);
                getList().getMetadata().getColumnDefinition(LIST_COLUMN_USER).setWidth("40%");
            } else {
                getList().getMetadata().getColumnDefinition(LIST_COLUMN_ORGUNIT).setVisible(true);
                getList().getMetadata().getColumnDefinition(LIST_COLUMN_USER).setWidth("20%");
            }
        } catch (CmsException e) {
            // noop
        }

        return ret;
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
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        // create column for send message
        CmsListColumnDefinition messageCol = new CmsListColumnDefinition(LIST_COLUMN_MESSAGE);
        messageCol.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_COLS_MESSAGE_0));
        messageCol.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_COLS_MESSAGE_HELP_0));
        messageCol.setWidth("20");
        messageCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        messageCol.setSorteable(false);
        // add send message action
        CmsListDirectAction messageAction = new CmsListDirectAction(LIST_ACTION_MESSAGE);
        messageAction.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_MESSAGE_NAME_0));
        messageAction.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_MESSAGE_HELP_0));
        messageAction.setIconPath(PATH_BUTTONS + "send_message.png");
        messageCol.addDirectAction(messageAction);
        // add it to the list definition
        metadata.addColumn(messageCol);

        // create column for pending
        CmsListColumnDefinition pendingCol = new CmsListColumnDefinition(LIST_COLUMN_PENDING);
        pendingCol.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_COLS_PENDING_0));
        pendingCol.setWidth("20");
        pendingCol.setAlign(CmsListColumnAlignEnum.ALIGN_CENTER);
        pendingCol.setListItemComparator(new CmsListItemActionIconComparator());
        // add pending action
        CmsListDirectAction pendingAction = new CmsListDirectAction(LIST_ACTION_PENDING_ENABLED) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() != null) {
                    return !OpenCms.getSessionManager().getBroadcastQueue(getItem().getId()).isEmpty();
                }
                return super.isVisible();
            }
        };
        pendingAction.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_PENDING_NAME_0));
        pendingAction.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_PENDING_HELP_0));
        pendingAction.setIconPath(PATH_BUTTONS + "message_pending.png");
        pendingAction.setEnabled(false);
        pendingCol.addDirectAction(pendingAction);

        // not pending action
        CmsListDirectAction notPendingAction = new CmsListDirectAction(LIST_ACTION_PENDING_DISABLED) {

            /**
             * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
             */
            @Override
            public boolean isVisible() {

                if (getItem() != null) {
                    return OpenCms.getSessionManager().getBroadcastQueue(getItem().getId()).isEmpty();
                }
                return super.isVisible();
            }
        };
        notPendingAction.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_NOTPENDING_NAME_0));
        notPendingAction.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_NOTPENDING_HELP_0));
        notPendingAction.setIconPath(PATH_BUTTONS + "message_notpending.png");
        notPendingAction.setEnabled(false);
        pendingCol.addDirectAction(notPendingAction);

        // add it to the list definition
        metadata.addColumn(pendingCol);

        // create column for user name
        CmsListColumnDefinition userCol = new CmsListColumnDefinition(LIST_COLUMN_USER);
        userCol.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_COLS_USER_0));
        //userCol.setWidth("20%");

        // create default edit message action
        CmsListDefaultAction messageEditAction = new CmsListDefaultAction(LIST_DEFACTION_MESSAGE);
        messageEditAction.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_MESSAGE_NAME_0));
        messageEditAction.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_MESSAGE_HELP_0));
        userCol.addDefaultAction(messageEditAction);
        // add it to the list definition
        metadata.addColumn(userCol);

        // add column for organizational units
        CmsListColumnDefinition ouCol = new CmsListColumnDefinition(LIST_COLUMN_ORGUNIT);
        ouCol.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_COLS_ORGUNIT_0));
        ouCol.setWidth("30%");
        metadata.addColumn(ouCol);

        // add column for creation date
        CmsListColumnDefinition creationCol = new CmsListColumnDefinition(LIST_COLUMN_CREATION);
        creationCol.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_COLS_CREATION_0));
        creationCol.setWidth("16%");
        creationCol.setFormatter(CmsListDateMacroFormatter.getDefaultDateFormatter());
        metadata.addColumn(creationCol);

        // add column for inactive time
        CmsListColumnDefinition inactiveCol = new CmsListColumnDefinition(LIST_COLUMN_INACTIVE);
        inactiveCol.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_COLS_INACTIVE_0));
        inactiveCol.setWidth("10%");
        inactiveCol.setFormatter(new CmsListTimeIntervalFormatter());
        metadata.addColumn(inactiveCol);

        // add column for project
        CmsListColumnDefinition projectCol = new CmsListColumnDefinition(LIST_COLUMN_PROJECT);
        projectCol.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_COLS_PROJECT_0));
        projectCol.setWidth("12%");
        metadata.addColumn(projectCol);

        // add column for site
        CmsListColumnDefinition siteCol = new CmsListColumnDefinition(LIST_COLUMN_SITE);
        siteCol.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_COLS_SITE_0));
        siteCol.setWidth("12%");
        metadata.addColumn(siteCol);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // create list item detail
        CmsListItemDetails emailDetail = new CmsListItemDetails(LIST_DETAIL_EMAIL);
        emailDetail.setAtColumn(LIST_COLUMN_USER);
        emailDetail.setVisible(false);
        emailDetail.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_SESSIONS_LABEL_EMAIL_0)));
        emailDetail.setShowActionName(Messages.get().container(Messages.GUI_SESSIONS_DETAIL_SHOW_EMAIL_NAME_0));
        emailDetail.setShowActionHelpText(Messages.get().container(Messages.GUI_SESSIONS_DETAIL_SHOW_EMAIL_HELP_0));
        emailDetail.setHideActionName(Messages.get().container(Messages.GUI_SESSIONS_DETAIL_HIDE_EMAIL_NAME_0));
        emailDetail.setHideActionHelpText(Messages.get().container(Messages.GUI_SESSIONS_DETAIL_HIDE_EMAIL_HELP_0));
        metadata.addItemDetails(emailDetail);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // add message multi action
        CmsListMultiAction messageMultiAction = new CmsListMultiAction(LIST_MACTION_MESSAGE);
        messageMultiAction.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_MACTION_MESSAGE_NAME_0));
        messageMultiAction.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_MACTION_MESSAGE_HELP_0));
        messageMultiAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_SESSIONS_LIST_MACTION_MESSAGE_CONF_0));
        messageMultiAction.setIconPath(PATH_BUTTONS + "multi_send_message.png");
        metadata.addMultiAction(messageMultiAction);

        // add email multi action
        CmsListMultiAction emailMultiAction = new CmsListMultiAction(LIST_MACTION_EMAIL);
        emailMultiAction.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_MACTION_EMAIL_NAME_0));
        emailMultiAction.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_MACTION_EMAIL_HELP_0));
        emailMultiAction.setConfirmationMessage(
            Messages.get().container(Messages.GUI_SESSIONS_LIST_MACTION_EMAIL_CONF_0));
        emailMultiAction.setIconPath(PATH_BUTTONS + "multi_send_email.png");
        metadata.addMultiAction(emailMultiAction);

        CmsListMultiAction killMultiAction = new CmsListMultiAction(LIST_MACTION_KILL_SESSION);
        I_CmsMessageBundle m = Messages.get();
        killMultiAction.setName(m.container(Messages.GUI_SESSIONS_LIST_MACTION_KILL_NAME_0));
        killMultiAction.setHelpText(m.container(Messages.GUI_SESSIONS_LIST_MACTION_KILL_HELP_0));
        killMultiAction.setIconPath("list/delete.png");
        metadata.addMultiAction(killMultiAction);
    }

}

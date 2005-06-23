/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/broadcast/CmsSessionsList.java,v $
 * Date   : $Date: 2005/06/23 10:47:27 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.workplace.broadcast;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListColumnAlignEnum;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.CmsListDefaultAction;
import org.opencms.workplace.list.CmsListDirectAction;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemActionIconComparator;
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
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSessionsList extends A_CmsListDialog {

    /** list action id constant. */
    public static final String LIST_ACTION_MESSAGE = "am";

    /** list action id constant. */
    public static final String LIST_ACTION_PENDING = "ap";

    /** list action id constant. */
    public static final String LIST_ACTION_PENDING_DISABLED = "apd";

    /** list action id constant. */
    public static final String LIST_ACTION_PENDING_ENABLED = "ape";

    /** list column id constant. */
    public static final String LIST_COLUMN_CREATION = "cc";

    /** list column id constant. */
    public static final String LIST_COLUMN_EMAIL = "ce";

    /** list column id constant. */
    public static final String LIST_COLUMN_INACTIVE = "ci";

    /** list column id constant. */
    public static final String LIST_COLUMN_MESSAGE = "cm";

    /** list column id constant. */
    public static final String LIST_COLUMN_PENDING = "cp";

    /** list column id constant. */
    public static final String LIST_COLUMN_PROJECT = "cj";

    /** list column id constant. */
    public static final String LIST_COLUMN_SITE = "cs";

    /** list column id constant. */
    public static final String LIST_COLUMN_USER = "cu";

    /** list action id constant. */
    public static final String LIST_DEFACTION_EMAIL = "de";

    /** list action id constant. */
    public static final String LIST_DEFACTION_MESSAGE = "dm";

    /** list id constant. */
    public static final String LIST_ID = "ls";

    /** list action id constant. */
    public static final String LIST_MACTION_EMAIL = "me";

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
    public void executeListMultiActions() throws IOException, ServletException {

        Map params = new HashMap();
        params.put(A_CmsMessageDialog.PARAM_SESSIONIDS, getParamSelItems());
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

        if (getParamListAction().equals(LIST_MACTION_MESSAGE)) {
            // execute the send message multiaction
            // forward to the edit message screen
            getToolManager().jspForwardTool(this, "/workplace/broadcast/message", params);
        } else if (getParamListAction().equals(LIST_MACTION_EMAIL)) {
            // execute the send email multiaction
            // forward to the edit email screen
            getToolManager().jspForwardTool(this, "/workplace/broadcast/email", params);
        } else {
            throwListUnsupportedActionException();
        }
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    public void executeListSingleActions() throws IOException, ServletException {

        Map params = new HashMap();
        params.put(A_CmsMessageDialog.PARAM_SESSIONIDS, getSelectedItem().getId());
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, CmsDialog.DIALOG_INITIAL);

        if (getParamListAction().equals(LIST_DEFACTION_EMAIL)) {
            // forward to the edit user screen
            getToolManager().jspForwardTool(this, "/workplace/broadcast/email", params);
        } else if (getParamListAction().equals(LIST_ACTION_MESSAGE)
            || getParamListAction().equals(LIST_DEFACTION_MESSAGE)) {
            getToolManager().jspForwardTool(this, "/workplace/broadcast/message", params);
        } else if (getParamListAction().equals(LIST_ACTION_PENDING)) {
            // noop
        } else {
            throwListUnsupportedActionException();
        }
        listSave();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    protected void fillDetails(String detailId) {

        // no details
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    protected List getListItems() {

        List ret = new ArrayList();
        // get content
        List sessions = OpenCms.getSessionManager().getSessionInfos();
        Iterator itSessions = sessions.iterator();
        while (itSessions.hasNext()) {
            CmsSessionInfo session = (CmsSessionInfo)itSessions.next();
            CmsListItem item = getList().newItem(session.getSessionId());
            item.set(LIST_COLUMN_USER, session.getUser().getFullName());
            item.set(LIST_COLUMN_EMAIL, session.getUser().getEmail());
            item.set(LIST_COLUMN_CREATION, new Date(session.getTimeCreated()));
            item.set(LIST_COLUMN_INACTIVE, new Long(System.currentTimeMillis() - session.getTimeUpdated()));
            try {
                item.set(LIST_COLUMN_PROJECT, getCms().readProject(session.getProject()).getName());
            } catch (Exception e) {
                // ignore
            }
            item.set(LIST_COLUMN_SITE, session.getSiteRoot());
            ret.add(item);
        }

        return ret;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
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
        CmsListDirectAction pendingAction = new CmsListDirectAction(LIST_ACTION_PENDING_ENABLED);
        pendingAction.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_PENDING_NAME_0));
        pendingAction.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_PENDING_HELP_0));
        pendingAction.setIconPath(PATH_BUTTONS + "message_pending.png");
        pendingAction.setEnabled(false);
        // not pending action
        CmsListDirectAction notPendingAction = new CmsListDirectAction(LIST_ACTION_PENDING_DISABLED);
        notPendingAction.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_NOTPENDING_NAME_0));
        notPendingAction.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_NOTPENDING_HELP_0));
        notPendingAction.setIconPath(PATH_BUTTONS + "message_notpending.png");
        notPendingAction.setEnabled(false);
        // adds a pending/not pending direct action
        CmsMessagePendingAction sessionAction = new CmsMessagePendingAction(LIST_ACTION_PENDING);
        sessionAction.setFirstAction(pendingAction);
        sessionAction.setSecondAction(notPendingAction);
        pendingCol.addDirectAction(sessionAction);
        // add it to the list definition
        metadata.addColumn(pendingCol);

        // create column for user name
        CmsListColumnDefinition userCol = new CmsListColumnDefinition(LIST_COLUMN_USER);
        userCol.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_COLS_USER_0));
        userCol.setWidth("20%");
        // create default edit message action
        CmsListDefaultAction messageEditAction = new CmsListDefaultAction(LIST_DEFACTION_MESSAGE);
        messageEditAction.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_MESSAGE_NAME_0));
        messageEditAction.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_ACTION_MESSAGE_HELP_0));
        userCol.setDefaultAction(messageEditAction);
        // add it to the list definition
        metadata.addColumn(userCol);

        // add column for email
        CmsListColumnDefinition emailCol = new CmsListColumnDefinition(LIST_COLUMN_EMAIL);
        emailCol.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_COLS_EMAIL_0));
        emailCol.setWidth("30%");
        // create default edit email action
        CmsListDefaultAction emailEditAction = new CmsListDefaultAction(LIST_DEFACTION_EMAIL);
        emailEditAction.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_DEFACTION_EMAIL_NAME_0));
        emailEditAction.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_DEFACTION_EMAIL_HELP_0));
        emailCol.setDefaultAction(emailEditAction);
        metadata.addColumn(emailCol);

        // add column for creation date
        CmsListColumnDefinition creationCol = new CmsListColumnDefinition(LIST_COLUMN_CREATION);
        creationCol.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_COLS_CREATION_0));
        creationCol.setWidth("16%");
        CmsListDateMacroFormatter creationDateFormatter = new CmsListDateMacroFormatter(Messages.get().container(
            Messages.GUI_SESSIONS_LIST_COLS_CREATION_FORMAT_1), Messages.get().container(
            Messages.GUI_SESSIONS_LIST_COLS_CREATION_NEVER_0));
        creationCol.setFormatter(creationDateFormatter);
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
    protected void setIndependentActions(CmsListMetadata metadata) {

        // noop        
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    protected void setMultiActions(CmsListMetadata metadata) {

        // add message multi action
        CmsListMultiAction messageMultiAction = new CmsListMultiAction(LIST_MACTION_MESSAGE);
        messageMultiAction.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_MACTION_MESSAGE_NAME_0));
        messageMultiAction.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_MACTION_MESSAGE_HELP_0));
        messageMultiAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_SESSIONS_LIST_MACTION_MESSAGE_CONF_0));
        messageMultiAction.setIconPath(PATH_BUTTONS + "multi_send_message.png");
        metadata.addMultiAction(messageMultiAction);

        // add email multi action
        CmsListMultiAction emailMultiAction = new CmsListMultiAction(LIST_MACTION_EMAIL);
        emailMultiAction.setName(Messages.get().container(Messages.GUI_SESSIONS_LIST_MACTION_EMAIL_NAME_0));
        emailMultiAction.setHelpText(Messages.get().container(Messages.GUI_SESSIONS_LIST_MACTION_EMAIL_HELP_0));
        emailMultiAction.setConfirmationMessage(Messages.get().container(
            Messages.GUI_SESSIONS_LIST_MACTION_EMAIL_CONF_0));
        emailMultiAction.setIconPath(PATH_BUTTONS + "multi_send_email.png");
        metadata.addMultiAction(emailMultiAction);
    }

}

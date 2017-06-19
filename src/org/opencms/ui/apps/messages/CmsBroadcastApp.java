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

package org.opencms.ui.apps.messages;

import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.data.Validator;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Class for the broadcast app.<p>
 */
public class CmsBroadcastApp extends A_CmsWorkplaceApp {

    /**
     * Validator for Message.<p>
     */
    static class MessageValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -7720843154577253852L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (value == null) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_LOGINMESSAGE_VAL_EMPTY_MESSAGE_0));
            }
            String message = (String)value;
            if (message.isEmpty()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_LOGINMESSAGE_VAL_EMPTY_MESSAGE_0));
            }
        }
    }

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsBroadcastApp.class.getName());

    /**Table showing sessions.*/
    CmsBroadcastTable m_table;

    /**
     * Get user names as String from set of sessions.<p>
     *
     * @param ids to gain usernames from
     * @param andLocalized String
     * @return user names as string
     */
    protected static String getUserNames(Set<String> ids, String andLocalized) {

        List<String> userNames = new ArrayList<String>();

        for (String id : ids) {
            CmsSessionInfo session = OpenCms.getSessionManager().getSessionInfo(new CmsUUID(id));
            try {
                String name = A_CmsUI.getCmsObject().readUser(session.getUserId()).getName();
                if (!userNames.contains(name)) {
                    userNames.add(name);
                }
            } catch (CmsException e) {
                LOG.error("Unable to read user information", e);
            }
        }

        Iterator<String> iterator = userNames.iterator();

        String res = "";

        while (iterator.hasNext()) {
            res += iterator.next();
            if (iterator.hasNext()) {
                res += ", ";
            }
        }
        int lastPosSeperation = res.lastIndexOf(", ");

        return lastPosSeperation == -1
        ? res
        : res.substring(0, lastPosSeperation)
            + " "
            + andLocalized
            + " "
            + res.substring(lastPosSeperation + 2, res.length());
    }

    /**
     * Shows dialog to send broadcast.<p>
     *
     * @param ids of sessions to send broadcast to
     * @param caption of window
     * @param table instance of table to be refreshed after sending broadcast
     */
    protected static void showSendBroadcastDialog(Set<String> ids, String caption, final CmsBroadcastTable table) {

        final Window window = CmsBasicDialog.prepareWindow();
        window.setCaption(caption);
        window.setContent(new CmsSendBroadcastDialog(ids, new Runnable() {

            public void run() {

                window.close();
                if (table != null) {
                    try {
                        table.ini();
                    } catch (CmsException e) {
                        LOG.error("Unable to read sessions", e);
                    }

                }
            }

        }));
        A_CmsUI.get().addWindow(window);
    }

    /**
    * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
    */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        //Check if state is empty -> start
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_BROADCAST_ADMIN_TOOL_NAME_0));
            return crumbs;
        }
        return new LinkedHashMap<String, String>();
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        addToolbarButtons();
        addInfoLayoutComponents();
        m_rootLayout.setMainHeightFull(true);
        if (state.isEmpty()) {
            m_table = new CmsBroadcastTable();
            return m_table;
        }

        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Opens dialog for login message settings.<p>
     */
    protected void openEditLoginMessageDialog() {

        Window window = CmsBasicDialog.prepareWindow();
        window.setContent(new CmsEditLoginView(window));
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_LOGINMESSAGE_TOOL_NAME_0));
        A_CmsUI.get().addWindow(window);
    }

    /**
     * Adds info layout components.<p>
     */
    private void addInfoLayoutComponents() {

        TextField siteTableFilter = new TextField();
        siteTableFilter.setIcon(FontOpenCms.FILTER);
        siteTableFilter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        siteTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        siteTableFilter.setWidth("200px");
        siteTableFilter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                m_table.filterTable(event.getText());
            }
        });
        m_infoLayout.addComponent(getStatisticLabel());
        m_infoLayout.addComponent(siteTableFilter);

    }

    /**
     * Adds Buttons to toolbar.<p>
     */
    private void addToolbarButtons() {

        Button add = CmsToolBar.createButton(
            FontAwesome.SIGN_IN,
            CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_LOGINMESSAGE_TOOL_NAME_0));
        add.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openEditLoginMessageDialog();
            }
        });
        m_uiContext.addToolbarButton(add);

        Button broadcastToAll = CmsToolBar.createButton(
            FontOpenCms.BROADCAST,
            CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_BROADCAST_TO_ALL_0));
        broadcastToAll.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                showSendBroadcastDialog(
                    null,
                    CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_BROADCAST_TO_ALL_0),
                    m_table);
            }
        });
        m_uiContext.addToolbarButton(broadcastToAll);
    }

    /**
     * Gets label with statistics to user sessions.<p>
     *
     * @return vaadin component
     */
    private Label getStatisticLabel() {

        Label ret = new Label();
        ret.setContentMode(ContentMode.HTML);

        List<CmsSessionInfo> sessions = OpenCms.getSessionManager().getSessionInfos();
        List<CmsUUID> user = new ArrayList<CmsUUID>();
        for (CmsSessionInfo info : sessions) {
            CmsUUID id = info.getUserId();
            if (!user.contains(id)) {
                user.add(id);
            }
        }
        String mes = CmsVaadinUtils.getMessageText(
            Messages.GUI_MESSAGES_SESSION_COUNT_1,
            String.valueOf(sessions.size()))
            + " / "
            + CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_USER_COUNT_1, String.valueOf(user.size()));
        ret.setValue(mes);
        ret.setWidth("250px");
        return ret;
    }
}

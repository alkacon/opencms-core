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

package org.opencms.ui.apps.sessions;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.sessions.CmsSessionsTable.TableProperty;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsUserInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsDateUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Dialog to show user information and to switch to user session.<p>
 */
public class CmsUserInfoDialog extends CmsBasicDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -8358238253459658269L;

    /** Log instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsUserInfoDialog.class);

    /**CmsObject.*/
    protected CmsObject m_cms;

    /**Session info (if showing specific session). */
    protected CmsSessionInfo m_session;

    /**vaadin component. */
    private Button m_cancelButton;

    /**vaadin component.*/
    private VerticalLayout m_layout;

    /**vaadin component. */
    private Button m_okButton;

    /**User to show iformation for. */
    private CmsUser m_user;

    /**
     * private empty constructor.<p>
     */
    private CmsUserInfoDialog() {

        //
    }

    /**
     * private constructor.<p>
     *
     * @param sessionInfo id to session
     * @param closeRunnable runnable called by closing window
     */
    private CmsUserInfoDialog(final CmsSessionInfo sessionInfo, final Runnable closeRunnable) {

        m_session = sessionInfo;

        m_cms = A_CmsUI.getCmsObject();
        try {
            m_user = m_cms.readUser(sessionInfo.getUserId());
            init(closeRunnable);
        } catch (CmsException e) {
            LOG.error("Can not read user.", e);
        }

    }

    /**
     * private constructor.<p>
     *
     * @param user to show dialog for
     * @param closeRunnable runnable called by closing window
     */
    private CmsUserInfoDialog(final CmsUser user, final Runnable closeRunnable) {

        m_cms = A_CmsUI.getCmsObject();
        m_user = user;
        init(closeRunnable);

    }

    /**
     * Gets the status text from given session.
     *
     * @param lastActivity miliseconds since last activity
     * @return status string
     */
    public static String getStatusForItem(Long lastActivity) {

        if (lastActivity.longValue() < CmsSessionsTable.INACTIVE_LIMIT) {
            return CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_BROADCAST_COLS_STATUS_ACTIVE_0);
        }
        return CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_BROADCAST_COLS_STATUS_INACTIVE_0);
    }

    /**
     * Gets the status style for given session.<p>
     *
     * @param lastActivity miliseconds since last activity
     * @return style
     */
    public static String getStatusStyleForItem(Long lastActivity) {

        if (lastActivity.longValue() < CmsSessionsTable.INACTIVE_LIMIT) {
            return OpenCmsTheme.TABLE_COLUMN_BOX_CYAN;
        }
        return OpenCmsTheme.TABLE_COLUMN_BOX_GRAY;
    }

    /**
     * Shows a dialog with user information for given session.
     *
     * @param session to show information for
     */
    public static void showUserInfo(CmsSessionInfo session) {

        final Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsUserInfoDialog dialog = new CmsUserInfoDialog(session, new Runnable() {

            public void run() {

                window.close();
            }

        });
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_SHOW_USER_0));
        window.setContent(dialog);
        A_CmsUI.get().addWindow(window);
    }

    /**
     * Shows a dialog with user information.<p>
     *
     * @param user to show information for.
     */
    public static void showUserInfo(CmsUser user) {

        final Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsBasicDialog dialog = new CmsUserInfoDialog(user, new Runnable() {

            public void run() {

                window.close();

            }

        });
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_SHOW_USER_0));
        window.setContent(dialog);
        A_CmsUI.get().addWindow(window);
    }

    /**
     * Adds information lines to the user dialog.<p>
     * @param currentSession the session which gets displayed or null
     * @return list of lines
     */
    private List<String> getFurtherInfoLines(CmsSessionInfo currentSession) {

        boolean neverActive = false;
        Long inacTime = Long.valueOf(0L);
        List<String> res = new ArrayList<String>();
        if (currentSession == null) {
            inacTime = Long.valueOf(System.currentTimeMillis() - m_user.getLastlogin());
            neverActive = m_user.getLastlogin() == 0L;
        } else {
            inacTime = Long.valueOf(System.currentTimeMillis() - currentSession.getTimeLastAction());
        }

        String[] inactiveTime = CmsSessionInfo.getHourMinuteSecondTimeString(inacTime.longValue());

        if (!neverActive) {
            if (currentSession != null) {
                res.add("");
                res.add("<p>" + CmsVaadinUtils.getMessageText(Messages.GUI_SESSIONS_SESSION_INFO_0) + "</p>");
                res.add(
                    CmsVaadinUtils.getMessageText(
                        Messages.GUI_MESSAGES_LAST_ACTIVITY_2,
                        inactiveTime[1] + ":" + inactiveTime[2],
                        CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_MINUTES_0)) + getStatusHTML(inacTime));
            } else {
                res.add(getLastLoginMessage(inacTime));
            }
        }
        res.add(
            CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_DATE_CREATED_0)
                + ": "
                + CmsDateUtil.getDateTime(
                    new Date(m_user.getDateCreated()),
                    DateFormat.SHORT,
                    A_CmsUI.get().getLocale()));
        if (currentSession != null) {
            res.add(TableProperty.Site.getLocalizedMessage() + ": " + getSiteTitle(currentSession));

            try {
                res.add(
                    TableProperty.Project.getLocalizedMessage()
                        + ": "
                        + A_CmsUI.getCmsObject().readProject(currentSession.getProject()).getName());
            } catch (CmsException e) {
                LOG.error("Unable to read project", e);
            }
        }
        return res;
    }

    /**
     * Get Message for show last login information.<p>
     *
     * @param inacTime time since last login in milli sec
     * @return HTML String
     */
    private String getLastLoginMessage(Long inacTime) {

        int days = (int)(inacTime.longValue() / (1000 * 60 * 60 * 24));
        if (days == 0) {
            return CmsVaadinUtils.getMessageText(Messages.GUI_USER_INFO_LAST_LOGIN_LESS_A_DAY_0);
        }
        if (days == 1) {
            return CmsVaadinUtils.getMessageText(Messages.GUI_USER_INFO_LAST_LOGIN_YESTERDAY_0);
        }
        return CmsVaadinUtils.getMessageText(Messages.GUI_USER_INFO_LAST_LOGIN_DAYS_AGO_1, Integer.valueOf(days));
    }

    /**
     * Gets the most current session for given user.<p>
     *
     * @param user CmsUser to get session for
     * @return CmsSessionInfo or null if no session is available for user
     */
    private CmsSessionInfo getSessionForUser(CmsUser user) {

        List<CmsSessionInfo> sessions = OpenCms.getSessionManager().getSessionInfos(user.getId());

        if (sessions.isEmpty()) {
            return null;
        }

        CmsSessionInfo currentSession = sessions.get(0);
        for (CmsSessionInfo session : sessions) {
            if (session.getTimeUpdated() > currentSession.getTimeUpdated()) {
                currentSession = session;
            }
        }
        return currentSession;
    }

    /**
     * Gets the title for the site of the session.<p>
     *
     * @param currentSession to get site-title for
     * @return title of site as string
     */
    private String getSiteTitle(CmsSessionInfo currentSession) {

        String siteRoot = currentSession.getSiteRoot();
        return (siteRoot.isEmpty() | siteRoot.equals("/"))
        ? CmsVaadinUtils.getMessageText(org.opencms.ade.galleries.Messages.GUI_ROOT_SITE_0)
        : OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot).getTitle();
    }

    /**
     * Get status span.<p>
     *
     * @param inacTime time
     * @return html
     */
    private String getStatusHTML(Long inacTime) {

        return "<span class=\"" + getStatusStyleForItem(inacTime) + "\">" + getStatusForItem(inacTime) + "</span> ";
    }

    /**
     * Initializes the dialog.<p>
     *
     * @param closeRunnable runnable
     */
    private void init(final Runnable closeRunnable) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        //Fix the width. Dialog can only be used by calling static method which creates a wide window.
        CmsUserInfo info = new CmsUserInfo(m_user.getId(), "640px");
        CmsSessionInfo session = m_session == null ? getSessionForUser(m_user) : m_session;
        Iterator<String> iterator = getFurtherInfoLines(session).iterator();

        while (iterator.hasNext()) {
            info.addDetailLine(iterator.next());
        }
        m_layout.addComponent(info);

        if (session != null) {
            m_okButton.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 3096577957489665752L;

                public void buttonClick(ClickEvent event) {

                    try {
                        A_CmsUI.get().changeProject(m_cms.readProject(session.getProject()));
                        A_CmsUI.get().changeSite(session.getSiteRoot());

                        String path = OpenCms.getSessionManager().switchUserFromSession(
                            m_cms,
                            CmsVaadinUtils.getRequest(),
                            m_cms.readUser(session.getUserId()),
                            session);

                        if (path == null) {
                            Map<String, String[]> parameters = new HashMap<>();
                            parameters.put("_lrid", new String[] {String.valueOf(System.currentTimeMillis())});
                            path = CmsVaadinUtils.getWorkplaceLink(
                                CmsFileExplorerConfiguration.APP_ID,
                                session.getProject().getStringValue()
                                    + A_CmsWorkplaceApp.PARAM_SEPARATOR
                                    + session.getSiteRoot()
                                    + A_CmsWorkplaceApp.PARAM_SEPARATOR
                                    + A_CmsWorkplaceApp.PARAM_SEPARATOR,
                                parameters);
                        }
                        A_CmsUI.get().getPage().setLocation(path);
                        if (path.contains(CmsSystemInfo.WORKPLACE_PATH + "#")) {
                            A_CmsUI.get().getPage().reload();
                        }
                    } catch (CmsException e) {
                        //
                    }
                    closeRunnable.run();
                }

            });
        }
        setHideSwitchButton(session);

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -1033076596404978498L;

            public void buttonClick(ClickEvent event) {

                closeRunnable.run();

            }
        });
    }

    /**
     * Hides the switch user button.<p>
     *
     * @param session to be checked
     */
    @SuppressWarnings("null")
    private void setHideSwitchButton(CmsSessionInfo session) {

        boolean visible = session != null;

        if (visible) {
            visible = !OpenCms.getSessionManager().getSessionInfo(CmsVaadinUtils.getRequest()).getSessionId().equals(
                session.getSessionId());
        }
        m_okButton.setVisible(visible);

    }

}

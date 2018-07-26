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

import org.opencms.file.CmsUser;
import org.opencms.main.CmsBroadcast;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.sessions.CmsSessionsApp.MessageValidator;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsRichTextAreaV7;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.v7.ui.CheckBox;

/**
 * Class for the dialiog to send broadcasts.<p>
 */
public class CmsSendBroadcastDialog extends CmsBasicDialog {

    private static final Map<CmsUser, CmsBroadcast> USER_BROADCAST = new HashMap<CmsUser, CmsBroadcast>();

    /**vaadin serial id.*/
    private static final long serialVersionUID = -7642289972554010162L;

    /**cancel button.*/
    private Button m_cancel;

    /**Message text area.*/
    private CmsRichTextAreaV7 m_message;

    /**ok button.*/
    private Button m_ok;

    private Button m_resetBroadcasts;

    private CheckBox m_repeat;

    /**
     * public constructor.<p>
     *
     * @param sessionIds to send broadcast to
     * @param closeRunnable called on cancel
     */
    public CmsSendBroadcastDialog(final Set<String> sessionIds, final Runnable closeRunnable) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        if (sessionIds != null) {
            displayResourceInfoDirectly(CmsSessionsApp.getUserInfos(sessionIds));
        } else {
            if (USER_BROADCAST.containsKey(A_CmsUI.getCmsObject().getRequestContext().getCurrentUser())) {
                m_message.setValue(
                    USER_BROADCAST.get(A_CmsUI.getCmsObject().getRequestContext().getCurrentUser()).getMessage());
            }
        }

        m_resetBroadcasts.addClickListener(event -> removeAllBroadcasts(sessionIds));

        m_cancel.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 3105449865170606831L;

            public void buttonClick(ClickEvent event) {

                closeRunnable.run();
            }
        });

        m_ok.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = -1148041995591262401L;

            public void buttonClick(ClickEvent event) {

                addValidator();
                if (isMessageValid()) {
                    sendBroadcast(sessionIds);
                    closeRunnable.run();
                }
            }
        });
    }

    /**
     * Adds validator to field.<p>
     */
    protected void addValidator() {

        m_message.removeAllValidators();
        m_message.addValidator(new MessageValidator());
    }

    /**
     * Checks if message is valid.<p>
     *
     * @return true if message is valid, false otherwise
     */
    protected boolean isMessageValid() {

        return m_message.isValid();
    }

    /**
     * Sends broadcast.<p>
     *
     * @param sessionIds to send broadcast to
     */
    protected void sendBroadcast(Set<String> sessionIds) {

        if (sessionIds == null) {
            OpenCms.getSessionManager().sendBroadcast(
                A_CmsUI.getCmsObject(),
                m_message.getValue(),
                m_repeat.getValue().booleanValue());
            USER_BROADCAST.put(
                A_CmsUI.getCmsObject().getRequestContext().getCurrentUser(),
                new CmsBroadcast(
                    A_CmsUI.getCmsObject().getRequestContext().getCurrentUser(),
                    m_message.getValue(),
                    m_repeat.getValue().booleanValue()));
        } else {
            for (String id : sessionIds) {
                OpenCms.getSessionManager().sendBroadcast(
                    A_CmsUI.getCmsObject(),
                    m_message.getValue(),
                    id,
                    m_repeat.getValue().booleanValue());
            }
        }
    }

    /**
     * Removes all pending broadcasts
     *
     * @param sessionIds to remove broadcast for (or null for all sessions)
     */
    private void removeAllBroadcasts(Set<String> sessionIds) {

        if (sessionIds == null) {
            for (CmsSessionInfo info : OpenCms.getSessionManager().getSessionInfos()) {
                OpenCms.getSessionManager().getBroadcastQueue(info.getSessionId().getStringValue()).clear();
            }
            return;
        }
        for (String sessionId : sessionIds) {
            OpenCms.getSessionManager().getBroadcastQueue(sessionId).clear();
        }
    }
}

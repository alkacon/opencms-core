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
import org.opencms.main.CmsBroadcast.ContentMode;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.sessions.CmsSessionsApp.MessageValidator;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsRichTextArea;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.v7.data.Validator.InvalidValueException;

/**
 * Class for the dialiog to send broadcasts.<p>
 */
public class CmsSendBroadcastDialog extends CmsBasicDialog {

    /** Map for storing the last message sent by a user. */
    private static final Map<CmsUser, CmsBroadcast> USER_BROADCAST = new ConcurrentHashMap<CmsUser, CmsBroadcast>();

    /**vaadin serial id.*/
    private static final long serialVersionUID = -7642289972554010162L;

    /**cancel button.*/
    private Button m_cancel;

    /**Message text area.*/
    private CmsRichTextArea m_message;

    /**ok button.*/
    private Button m_ok;

    /** Button for clearing broadcasts. */
    private Button m_resetBroadcasts;

    /** Check box for setting a message to repeating. */
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

                if (validateMessage()) {
                    sendBroadcast(sessionIds);
                    closeRunnable.run();
                }
            }
        });
    }

    /**
     * Sends broadcast.<p>
     *
     * @param sessionIds to send broadcast to
     */
    protected void sendBroadcast(Set<String> sessionIds) {

        String cleanedHtml = CmsRichTextArea.cleanHtml(m_message.getValue(), true);
        if (sessionIds == null) {
            OpenCms.getSessionManager().sendBroadcast(
                A_CmsUI.getCmsObject(),
                cleanedHtml,
                m_repeat.getValue().booleanValue(),
                ContentMode.html);
            USER_BROADCAST.put(
                A_CmsUI.getCmsObject().getRequestContext().getCurrentUser(),
                new CmsBroadcast(
                    A_CmsUI.getCmsObject().getRequestContext().getCurrentUser(),
                    cleanedHtml,
                    m_repeat.getValue().booleanValue(),
                    ContentMode.html));
        } else {
            for (String id : sessionIds) {
                OpenCms.getSessionManager().sendBroadcast(
                    A_CmsUI.getCmsObject(),
                    cleanedHtml,
                    id,
                    m_repeat.getValue().booleanValue(),
                    ContentMode.html);
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

    /**
     * Validates the broadcast message, sets the error status of the field appropriately, and returns the result.
     *
     * @return true if the validation was successful
     */
    private boolean validateMessage() {

        m_message.setComponentError(null);
        try {
            new MessageValidator().validate(m_message.getValue());
            return true;
        } catch (InvalidValueException e) {
            m_message.setComponentError(new UserError(e.getLocalizedMessage()));
            return false;
        }
    }
}

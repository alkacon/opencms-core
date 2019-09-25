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

import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.util.CmsUUID;

import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;

/**
 * Class for the dialog to kill sessions.<p>
 */
public class CmsKillSessionDialog extends CmsBasicDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -7281930091176835024L;

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsKillSessionDialog.class.getName());

    /**cancel button.*/
    private Button m_cancelButton;

    /**warning icon.*/
    private Label m_icon;

    /**vaadin component. */
    private Label m_label;

    /**ok button.*/
    private Button m_okButton;

    /**
     * public constructor. <p>
     *
     * @param sessionIds ids of sessions to be killed
     * @param canelRunnable runnable to be runned on cancel
     */
    public CmsKillSessionDialog(final Set<String> sessionIds, final Runnable canelRunnable) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        displayResourceInfoDirectly(CmsSessionsApp.getUserInfos(sessionIds));

        m_icon.setContentMode(ContentMode.HTML);
        m_icon.setValue(FontOpenCms.WARNING.getHtml());

        if (sessionIds.size() == 1) {
            m_label.setValue(CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_CONFIRM_DESTROY_SESSION_SINGLE_0));
        }

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 5044360626122683306L;

            public void buttonClick(ClickEvent event) {

                for (String sessionId : sessionIds) {
                    try {
                        OpenCms.getSessionManager().killSession(A_CmsUI.getCmsObject(), new CmsUUID(sessionId));
                        LOG.info("Kill session of user with id '" + sessionId + "'");
                    } catch (NumberFormatException | CmsException e) {
                        //current session cannot be killed
                    }
                }
                canelRunnable.run();
            }
        });
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 6872628835561250226L;

            public void buttonClick(ClickEvent event) {

                canelRunnable.run();
            }
        });
    }
}

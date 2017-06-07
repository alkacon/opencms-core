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

package org.opencms.gwt.client;

import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.shared.CmsBroadcastMessage;

import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

/**
 * A timer which sends an RPC call regularly to keep the session alive and receive workplace broadcasts.<p>
 *
 * @since 9.5.0
 */
public class CmsBroadcastTimer {

    /**
     * The interval for the RPC calls.<p>
     */
    public static final int PING_INTERVAL = 1000 * 10;

    /**
     * The static instance.<p>
     */
    private static CmsBroadcastTimer INSTANCE;

    /** Flag indicating if the timer should keep running. */
    private static boolean m_keepRunning;

    /**
     * Aborts the timer.<p>
     */
    public static void abort() {

        m_keepRunning = false;
    }

    /**
     * Starts the timer.<p>
     */
    public static void start() {

        if (!CmsCoreProvider.get().isKeepAlive()) {
            return;
        }

        if (INSTANCE == null) {
            m_keepRunning = true;
            CmsBroadcastTimer timer = new CmsBroadcastTimer();
            timer.run();
            INSTANCE = timer;
        }
    }

    /**
     * Returns if the timer should keep running.<p>
     *
     * @return <code>true</code>  if the ping timer should keep running
     */
    protected static boolean shouldKeepRunning() {

        return m_keepRunning;
    }

    /**
     * Generates the HTML for a single broadcast message.<p>
     *
     * @param message the message
     *
     * @return the HTML string
     */
    protected String createMessageHtml(CmsBroadcastMessage message) {

        StringBuffer result = new StringBuffer();
        result.append("<p class=\"").append(I_CmsLayoutBundle.INSTANCE.notificationCss().messageTime()).append("\">");
        result.append(message.getTime());
        result.append("</p><p>");
        result.append(Messages.get().key(Messages.GUI_BROADCAST_SEND_BY_1, message.getUser()));
        result.append("</p><div class=\"").append(I_CmsLayoutBundle.INSTANCE.notificationCss().messageWrap()).append(
            "\"><p>\n");
        result.append(message.getMessage().replaceAll("\\n", "&nbsp;</p><p>"));
        result.append("\n</p></div>");
        return result.toString();
    }

    /**
     * Installs the timer which fires the RPC calls.<p>
     */
    protected void run() {

        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            public boolean execute() {

                if (CmsBroadcastTimer.shouldKeepRunning()) {
                    CmsCoreProvider.getService().getBroadcast(new AsyncCallback<List<CmsBroadcastMessage>>() {

                        public void onFailure(Throwable caught) {

                            // in case of a status code exception abort, indicates the session is no longer valid
                            if ((caught instanceof StatusCodeException)
                                && (((StatusCodeException)caught).getStatusCode() == 500)) {
                                CmsBroadcastTimer.abort();
                            }
                        }

                        public void onSuccess(List<CmsBroadcastMessage> result) {

                            if (result != null) {
                                for (CmsBroadcastMessage message : result) {
                                    CmsNotification.get().sendAlert(Type.WARNING, createMessageHtml(message));
                                }
                            }
                        }
                    });
                    return true;
                }
                return false;
            }
        }, PING_INTERVAL);
    }
}

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

package org.opencms.ui.client;

import org.opencms.ui.components.extensions.CmsPollServerExtension;
import org.opencms.ui.shared.rpc.I_CmsPollServerRpc;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * This connector will manipulate the CSS classes of the extended widget depending on the scroll position.<p>
 */
@Connect(CmsPollServerExtension.class)
public class CmsPollServerConnector extends AbstractExtensionConnector {

    /** Polls the server. */
    protected class PollingCommand implements RepeatingCommand {

        /** If polling is cancelled. */
        private boolean m_cancelled;

        /**
         * @see com.google.gwt.core.client.Scheduler.RepeatingCommand#execute()
         */
        public boolean execute() {

            if (!m_cancelled) {
                try {
                    m_rpc.poll();
                } catch (@SuppressWarnings("unused") Throwable t) {
                    m_cancelled = true;
                }
            }
            return !m_cancelled;
        }

        /**
         * Cancels polling.<p>
         */
        void cancel() {

            m_cancelled = true;
        }
    }

    /** The polling delay in milliseconds. */
    private static final int POLLING_DELAY_MS = 30 * 1000;

    /** The serial version id. */
    private static final long serialVersionUID = -3661096843568550285L;

    /** The RPC proxy. */
    I_CmsPollServerRpc m_rpc;

    /** The polling command. */
    private PollingCommand m_command;

    /** The widget to enhance. */
    private Widget m_widget;

    /**
     * Constructor.<p>
     */
    public CmsPollServerConnector() {
        m_rpc = getRpcProxy(I_CmsPollServerRpc.class);
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        // Get the extended widget
        m_widget = ((ComponentConnector)target).getWidget();
        m_widget.addAttachHandler(new Handler() {

            public void onAttachOrDetach(AttachEvent event) {

                if (event.isAttached()) {
                    startPolling();
                } else {
                    stopPolling();
                }
            }
        });
        startPolling();
    }

    /**
     * Starts polling the server.<p>
     */
    void startPolling() {

        m_rpc.poll();
        if (m_command == null) {
            m_command = new PollingCommand();
            Scheduler.get().scheduleFixedDelay(m_command, POLLING_DELAY_MS);
        }
    }

    /**
     * Stops polling the server.<p>
     */
    void stopPolling() {

        if (m_command != null) {
            m_command.cancel();
            m_command = null;
        }
    }
}

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

import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.ui.components.extensions.CmsWindowCloseExtension;
import org.opencms.ui.shared.rpc.I_CmsWindowCloseServerRpc;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * The window close connector.<p>
 */
@Connect(CmsWindowCloseExtension.class)
public class CmsWindowCloseConnector extends AbstractExtensionConnector {

    /** The serial version id. */
    private static final long serialVersionUID = 190108090241764065L;

    /** The RPC proxy. */
    private I_CmsWindowCloseServerRpc m_rpc;

    /**
     * Constructor.<p>
     */
    public CmsWindowCloseConnector() {
        m_rpc = getRpcProxy(I_CmsWindowCloseServerRpc.class);
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        Window.addWindowClosingHandler(new ClosingHandler() {

            public void onWindowClosing(ClosingEvent event) {

                handleClose();
            }
        });
    }

    /**
     * Handles the window closing.<p>
     */
    void handleClose() {

        m_rpc.windowClosed(CmsRpcAction.SYNC_TOKEN);
    }
}

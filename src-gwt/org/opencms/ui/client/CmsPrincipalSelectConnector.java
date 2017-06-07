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

import org.opencms.ui.dialogs.permissions.CmsPrincipalSelectExtension;
import org.opencms.ui.shared.rpc.I_CmsPrincipalSelectRpc;

import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * The client principal select dialog connector.<p>
 */
@Connect(CmsPrincipalSelectExtension.class)
public class CmsPrincipalSelectConnector extends AbstractExtensionConnector {

    /** The serial version id. */
    private static final long serialVersionUID = -4612206679727401825L;

    /** The select service instance. */
    private I_CmsPrincipalSelectRpc m_rpc;

    /**
     * Constructor.<p>
     */
    public CmsPrincipalSelectConnector() {
        m_rpc = getRpcProxy(I_CmsPrincipalSelectRpc.class);
    }

    /**
     * @see com.vaadin.client.ui.AbstractConnector#onUnregister()
     */
    @Override
    public void onUnregister() {

        super.onUnregister();
        removeExportedFunctions();
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        exportFunctions();
    }

    /**
     * Sets the principal.<p>
     *
     * @param type the principal type
     * @param principalName the principal name
     */
    void setPrincipal(int type, String principalName) {

        m_rpc.setPrincipal(type, principalName);
        // forcing the RPC to be executed immediately
        getConnection().getHeartbeat().send();
    }

    /**
     * Exports the setPrincipalFormValue function to the window context.<p>
     */
    private native void exportFunctions()/*-{
        var self = this;
        $wnd.setPrincipalFormValue = function(type, name) {
            self.@org.opencms.ui.client.CmsPrincipalSelectConnector::setPrincipal(ILjava/lang/String;)(type,name);
        };

    }-*/;

    /**
     * Removes the setPrincipalFormValue function from the window context.<p>
     */
    private native void removeExportedFunctions()/*-{
        $wnd.setPrincipalFormValue = null;

    }-*/;

}

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

import org.opencms.ui.components.extensions.CmsWindowExtension;
import org.opencms.ui.shared.rpc.I_CmsWindowClientRpc;
import org.opencms.ui.shared.rpc.I_CmsWindowServerRpc;

import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Client connector for CmsWindowExtension.<p>
 */
@Connect(CmsWindowExtension.class)
public class CmsWindowExtensionConnector extends AbstractExtensionConnector implements I_CmsWindowClientRpc {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsWindowClientRpc#open(java.lang.String, java.lang.String, java.lang.String)
     */
    public void open(String url, String target, String id) {

        boolean ok = openInternal(url, target);
        getRpcProxy(I_CmsWindowServerRpc.class).handleOpenResult(id, ok);
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        registerRpc(I_CmsWindowClientRpc.class, this);
    }

    /**
     * Opens the given URI in the window with the given name.<p>
     *
     * Returns false if opening the new page fails.
     *
     * @param uri the URI to open
     * @param target the target window name
     *
     * @return a boolean indicating whether opening the page was successful
     */
    private native boolean openInternal(String uri, String target) /*-{
                                                                   var openResult = $wnd.open(uri, target);
                                                                   return openResult != null;
                                                                   }-*/;

}

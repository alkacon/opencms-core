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

import org.opencms.ui.components.extensions.CmsJSPBrowserFrameExtension;
import org.opencms.ui.shared.rpc.I_CmsJSPBrowserFrameRpc;

import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Client side connector for CmsJSPBrowserFrameExtension.<p>
 */
@Connect(CmsJSPBrowserFrameExtension.class)
public class CmsJSPBrowserFrameConnector extends AbstractExtensionConnector {

    /**vaadin serial id.*/
    private static final long serialVersionUID = -6067831572929214619L;

    /** The RPC proxy. */
    private I_CmsJSPBrowserFrameRpc m_rpc;

    /**
     * Public constructor.<p>
     */
    public CmsJSPBrowserFrameConnector() {
        super();
        m_rpc = getRpcProxy(I_CmsJSPBrowserFrameRpc.class);
    }

    /**
     * Method to call interface to return resources which were changed.<p>
     *
     * @param resource which were changed
     */
    public void closeParentWindow(String[] resource) {

        m_rpc.cancelParentWindow(resource);
        // forcing the RPC to be executed immediately
        getConnection().getHeartbeat().send();
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        closeBrowserFrame();
    }

    /**
     * JavaScript method to run on client.<p>
     */
    private native void closeBrowserFrame()/*-{
		var self = this;
		$wnd.changedResources = function(resources) {
			self.@org.opencms.ui.client.CmsJSPBrowserFrameConnector::closeParentWindow([Ljava/lang/String;)(resources)
		};
    }-*/;
}

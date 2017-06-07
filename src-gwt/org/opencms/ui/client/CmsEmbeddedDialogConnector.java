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

import org.opencms.ui.dialogs.CmsEmbeddedDialogContext;
import org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC;

import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * The embedded dialog connector.<p>
 */
@Connect(CmsEmbeddedDialogContext.class)
public class CmsEmbeddedDialogConnector extends AbstractExtensionConnector implements I_CmsEmbeddedDialogClientRPC {

    /** The serial version id. */
    private static final long serialVersionUID = -7984262078804717197L;

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC#finish(java.lang.String)
     */
    public native void finish(String resourceIds)/*-{
        $wnd.connector.finish(resourceIds);
    }-*/;

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC#finishForProjectOrSiteChange(java.lang.String, java.lang.String)
     */
    public native void finishForProjectOrSiteChange(String sitePath, String serverLink)/*-{

        $wnd.connector.finishForProjectOrSiteChange(sitePath, serverLink);
    }-*/;

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC#leavePage(java.lang.String)
     */
    public native void leavePage(String targetUri)/*-{
        $wnd.connector.leavePage(targetUri);
    }-*/;

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC#reloadParent()
     */
    public native void reloadParent()/*-{
        $wnd.connector.reload();
    }-*/;

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        registerRpc(I_CmsEmbeddedDialogClientRPC.class, this);
    }
}

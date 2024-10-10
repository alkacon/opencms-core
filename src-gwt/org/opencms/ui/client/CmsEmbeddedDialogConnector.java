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

import org.opencms.gwt.client.util.CmsEmbeddedDialogFrameWrapper;
import org.opencms.gwt.client.util.I_CmsEmbeddedDialogLoader;
import org.opencms.ui.components.extensions.CmsEmbeddedDialogExtension;
import org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC;
import org.opencms.ui.shared.rpc.I_CmsEmbeddingServerRpc;

import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * The embedded dialog connector.<p>
 */
@Connect(CmsEmbeddedDialogExtension.class)
public class CmsEmbeddedDialogConnector extends AbstractExtensionConnector
implements I_CmsEmbeddedDialogClientRPC, I_CmsEmbeddedDialogLoader {

    /** The serial version id. */
    private static final long serialVersionUID = -7984262078804717197L;

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC#finish(java.lang.String)
     */
    public void finish(String resourceIds) {

        CmsEmbedWrapper.connector.finish(resourceIds);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC#finishForProjectOrSiteChange(java.lang.String, java.lang.String)
     */
    public void finishForProjectOrSiteChange(String sitePath, String serverLink) {

        CmsEmbedWrapper.connector.finishForProjectOrSiteChange(sitePath, serverLink);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC#initServerRpc()
     */
    public void initServerRpc() {

        // we only should get here if there *is* an embedded frame in the window
        CmsEmbeddedDialogFrameWrapper parentWindow = CmsEmbeddedDialogFrameWrapper.parent;
        parentWindow.embeddedDialogFrameInstance.installEmbeddedDialogLoader(this);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC#leavePage(java.lang.String)
     */
    public void leavePage(String targetUri) {

        CmsEmbedWrapper.connector.leavePage(targetUri);
    }

    /**
     * @see org.opencms.gwt.client.util.I_CmsEmbeddedDialogLoader#loadDialog(java.lang.String)
     */
    @Override
    public void loadDialog(String dialogInfo) {

        getRpcProxy(I_CmsEmbeddingServerRpc.class).loadDialog(dialogInfo);
        // in Chrome, the RPC request does not get sent until the user moves the mouse unless we manually trigger a heartbeat request
        getConnection().getHeartbeat().send();
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC#reloadParent()
     */
    public void reloadParent() {

        CmsEmbedWrapper.connector.reload();
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC#selectString(java.lang.String)
     */
    public void selectString(String principal) {

        CmsEmbedWrapper.connector.selectString(principal);

    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsEmbeddedDialogClientRPC#sendNotification(boolean, java.lang.String)
     */
    @Override
    public void sendNotification(boolean error, String notification) {

        CmsEmbedWrapper.connector.sendNotification(error, notification);
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        registerRpc(I_CmsEmbeddedDialogClientRPC.class, this);
    }
}

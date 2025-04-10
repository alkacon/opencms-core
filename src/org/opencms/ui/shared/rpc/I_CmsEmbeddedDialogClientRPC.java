/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.shared.rpc;

import com.vaadin.shared.communication.ClientRpc;

/**
 * Client RPC to handle embedded dialogs.<p>
 */
public interface I_CmsEmbeddedDialogClientRPC extends ClientRpc {

    /**
     * Removes the dialog iFrame and refreshes the given resources.<p>
     *
     * @param resourceIds the resources to refresh
     */
    void finish(String resourceIds);

    /**
     * Removes the dialog iFrame and reloads the app for the given site path and server link.<p>
     *
     * @param sitePath the site path
     * @param serverLink the server link
     */
    void finishForProjectOrSiteChange(String sitePath, String serverLink);

    /**
     * Tells the client to initialize the client-to-server RPC.
     **/
    void initServerRpc();

    /**
     * Leaves the current page calling the given URI.<p>
     *
     * @param targetUri the target URI
     */
    void leavePage(String targetUri);

    /**
     * Reloads the parent window.<p>
     */
    void reloadParent();

    /**
     * Sets the selected string.<p>
     *
     * @param str the string
     */
    void selectString(String str);

    /**
     * Send a notification to the client to display.
     *
     * @param error true if this is an error notification
     * @param notification the notification HTML
     */
    void sendNotification(boolean error, String notification);
}

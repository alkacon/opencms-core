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

package org.opencms.ui.shared.rpc;

import com.vaadin.shared.communication.ServerRpc;

/**
 * Interface for the client-to-server rpc calls used by the sitemap extension.<p>
 *
 */
public interface I_CmsSitemapServerRpc extends ServerRpc {

    /**
     * Handles changes made via the property dialog.<p>
     *
     * @param id the structure id of the changed resource
     */
    void handleChangedProperties(String id);

    /**
     * Opens the page copy dialog for a resource.<p>
     *
     * @param callId a unique (per client) id representing the RPC call
     * @param structureId the structure id of the resource for which to open the dialog
     */
    void openPageCopyDialog(String callId, String structureId);

    /**
     * Displays the locale comparison view.<p>
     *
     * @param id the locale comparison view
     */
    void showLocaleComparison(String id);

}

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

import com.vaadin.shared.communication.ClientRpc;

/**
 * Interface for the server-to-client RPC calls used by the sitemap extension.<p>
 *
 */
public interface I_CmsSitemapClientRpc extends ClientRpc {

    /**
     * Signals to the client that the page copy dialog has finished.<p>
     *
     * @param callId the call id which was initially used to open the dialog
     * @param response a string representing the result code of the page copy dialog
     */
    void finishPageCopyDialog(String callId, String response);

    /**
     * Opens the property dialog in the locale comparison view.<p>
     *
     * @param structureId the structure id of the resource for which to open the property dialog
     * @param rootId the root structure id of the locale comparison tree
     */
    void openPropertyDialog(String structureId, String rootId);

    /**
     * Displays the header for a sub-sitemap in the locale comparison view.<p>
     *
     * @param title the title
     * @param description the description
     * @param path the path
     * @param locale the locale
     * @param icon the CSS classes for the icon
     */
    void showInfoHeader(String title, String description, String path, String locale, String icon);

}

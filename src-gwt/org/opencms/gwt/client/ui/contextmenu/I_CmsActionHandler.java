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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.util.CmsUUID;

/**
 * Handles actions.<p>
 */
public interface I_CmsActionHandler {

    /**
     * Leaves the current page calling the target URI.<p>
     *
     * @param targetUri the target URI
     */
    void leavePage(String targetUri);

    /**
     * Called when site and or project have been changed.<p>
     *
     * @param sitePath the site path to the target resource
     * @param serverLink the server link to the resource
     */
    void onSiteOrProjectChange(String sitePath, String serverLink);

    /**
     * Reloads the resource edited.<p>
     *
     * @param structureId the structure id of the resource to lock
     */
    void refreshResource(CmsUUID structureId);
}

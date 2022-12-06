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

package org.opencms.gwt.client.util;

import jsinterop.annotations.JsType;

/**
 * Interface used to export callback methods from CmsEmbeddedDialogHandler as JavaScript methods.
 */
@JsType(isNative = true)
public interface I_CmsEmbeddedDialogHandlerJsCallbacks {

    /**
     * Called on dialog close.<p>
     *
     * @param resources the resource ids to update as a ';' separated string.<p>
     */
    public void finish(String resources);

    /**
     * Called when site and or project have been changed.<p>
     *
     * @param sitePath the site path to the resource to display
     * @param serverLink the server link to the resource to display
     */
    public void finishForProjectOrSiteChange(String sitePath, String serverLink);

    /**
     * Navigates to the given URI.<p>
     *
     * @param targetUri the target URI
     */
    public void leavePage(String targetUri);

    /**
     * Reloads the current page.<p>
     */
    public void reload();

    /**
     * Calls the principle select handler and closes the dialog frame.<p>
     *
     * @param principle the principle to select
     */
    public void selectString(String principle);

}

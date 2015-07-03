/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.apps;

import com.vaadin.server.Resource;
import com.vaadin.ui.Component;

/**
 * The app ui context. Allows OpenCms workplace apps access to the surrounding UI.<p>
 */
public interface I_CmsAppUIContext {

    /**
     * Adds a toolbar button.<p>
     *
     * @param button the button to add
     */
    void addToolbarButton(Component button);

    /**
     * Removes the app's toolbar buttons.<p>
     */
    void clearToolbarButtons();

    /**
     * Sets the app content component.<p>
     *
     * @param appContent the app content
     */
    void setAppContent(Component appContent);

    /**
     * Sets the app icon.<p>
     *
     * @param icon the icon resource
     */
    void setAppIcon(Resource icon);

    /**
     * Sets the app info component.<p>
     *
     * @param appInfo the app info
     */
    void setAppInfo(Component appInfo);

    /**
     * Sets the app title.<p>
     *
     * @param title the app title
     */
    void setAppTitle(String title);

    /**
     * Sets the side bar menu component.<p>
     *
     * @param menuContent the menu component
     */
    void setMenuContent(Component menuContent);

}

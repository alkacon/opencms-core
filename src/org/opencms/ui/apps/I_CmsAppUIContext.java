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

import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsUpdateListener;

import com.vaadin.ui.Component;

/**
 * The app ui context. Allows OpenCms workplace apps access to the surrounding UI.<p>
 */
public interface I_CmsAppUIContext {

    /**
     * Hides the the toolbar.<p>
     */
    public void hideToolbar();

    /**
     * Shows the formerly hidden toolbar.<p>
     */
    public void showToolbar();

    /**
     * Adds the publish button to the toolbar.<p>
     *
     * @param updateListener the update listener, called after publishing
     */
    void addPublishButton(I_CmsUpdateListener<String> updateListener);

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
     * Sets the app info component.<p>
     *
     * @param infoContent the info component
     */
    void setAppInfo(Component infoContent);

    /**
     * Sets the app title.<p>
     *
     * @param title the app title
     */
    void setAppTitle(String title);

    /**
     * Sets the dialog context for context menu entries.<p>
     *
     * @param context the dialog context
     */
    void setMenuDialogContext(I_CmsDialogContext context);

    /**
     * Sets the info grid visibility.<p>
     *
     * @param show <code>true</code> to show the info
     */
    void showInfoArea(boolean show);
}

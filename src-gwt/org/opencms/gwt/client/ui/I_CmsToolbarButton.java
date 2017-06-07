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

package org.opencms.gwt.client.ui;

import com.google.gwt.event.dom.client.HasClickHandlers;

/**
 * Interface for all tool-bar buttons.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsToolbarButton extends HasClickHandlers {

    /**
     * Returns the localized button title. Will show in tool-tip.<p>
     *
     * @return the button title
     */
    String getTitle();

    /**
     * Returns whether this button is active (pushed, not disabled).<p>
     *
     * @return <code>true</code> if the button is active
     */
    boolean isActive();

    /**
     * Method executed when the button is activated.<p>
     */
    void onToolbarActivate();

    /**
     * Executed when the tool-bar button is clicked.<p>
     */
    void onToolbarClick();

    /**
     * Method executed when the button is deactivated.<p>
     */
    void onToolbarDeactivate();

    /**
     * Sets the button to active (pushed, not disabled).<p>
     *
     *
     * @param active <code>true</code> if active
     */
    void setActive(boolean active);

    /**
     * Enables or disables the button.<p>
     *
     * @param enabled if true, enable the button, else disable it
     */
    void setEnabled(boolean enabled);
}

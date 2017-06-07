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

package org.opencms.gwt.client.ui.input;

/**
 * Interface for widgets which can contain a "ghost value".<p>
 *
 * A ghost value will be displayed, but not reported when asking the widget for its current value.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsHasGhostValue {

    /**
     * Enables or disables ghost mode, if possible.<p>
     *
     * @param enable if ghost mode should be enabled, else disabled
     */
    void setGhostMode(boolean enable);

    /**
     * Sets the "ghost value" of the widget and optionally sets it to "ghost mode".<p>
     *
     * "Ghost mode" can be used to show the user the value (called "ghost value") which will be used if he
     * either doesn't choose an option in the widget, or explicitly chooses an "empty" or "default" option.<p>
     *
     * @param value the ghost value
     * @param isGhostMode if true, sets the widget to ghost mode
     */
    void setGhostValue(String value, boolean isGhostMode);
}

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

package org.opencms.ui;

import com.vaadin.navigator.View;
import com.vaadin.ui.Component;

/**
 * Factory to create components.<p>
 */
public interface I_CmsAppView extends View {

    /**
     * Disables the global keyboard shortcuts.<p>
     */
    void disableGlobalShortcuts();

    /**
     * Enables the global keyboard shortcuts.<p>
     */
    void enableGlobalShortcuts();

    /**
     * Enters the view.<p>
     *
     * @param state the state to set
     */
    void enter(String state);

    /**
     * Returns the app component, initializes it if required.<p>
     *
     * @return the component
     */
    Component getComponent();

    /**
     * Returns the view name.<p>
     *
     * @return the view name
     */
    String getName();

    /**
     * Returns whether this view should be cached within the user session.<p>
     *
     * @return <code>true</code> if the view is cachable
     */
    boolean isCachable();

    /**
     * Creates a new component instance.<p>
     *
     * @return the new component
     */
    Component reinitComponent();

    /**
     * Returns whether this view needs to be restored from cache.<p>
     *
     * @return <code>true</code> if this view needs to be restored from cache
     */
    boolean requiresRestore();

    /**
     * Restores the view from cache.<p>
     */
    void restoreFromCache();

    /**
     * Sets the requires restore from cache flag.<p>
     *
     * @param restored the requires restore from cache flag
     */
    void setRequiresRestore(boolean restored);
}

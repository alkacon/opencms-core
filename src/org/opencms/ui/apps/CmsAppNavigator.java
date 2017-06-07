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

package org.opencms.ui.apps;

import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.ui.UI;

/**
 * Custom navigator subclass used to prevent "slash accumulation" in the URL fragment if the navigateTo(...) methods are called
 * multiple names in the same navigation.
 */
public class CmsAppNavigator extends Navigator {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.<p>
     *
     * @param ui the UI
     * @param stateManager the state manager
     * @param display the display
     */
    public CmsAppNavigator(UI ui, NavigationStateManager stateManager, ViewDisplay display) {
        super(ui, stateManager, display);
    }

    /**
     * @see com.vaadin.navigator.Navigator#navigateTo(com.vaadin.navigator.View, java.lang.String, java.lang.String)
     */
    @Override
    protected void navigateTo(View view, String viewName, String parameters) {

        if ((parameters != null) && parameters.startsWith("/")) {
            parameters = parameters.substring(1);
        }
        super.navigateTo(view, viewName, parameters);
    }

}

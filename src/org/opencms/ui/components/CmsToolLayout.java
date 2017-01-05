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

package org.opencms.ui.components;

import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp.NavEntry;
import org.opencms.ui.apps.CmsDefaultAppButtonProvider;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Panel;

/**
 * The standard workplace tool layout.<p>
 */
public class CmsToolLayout extends CssLayout {

    /** The serial version id. */
    private static final long serialVersionUID = 2195018534066531670L;

    /** The main panel. */
    private Panel m_main;

    /** The sub navigation. */
    private CssLayout m_subNav;

    /**
     * Constructor.<p>
     */
    public CmsToolLayout() {
        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
    }

    /**
     * Adds a sub navigation entry.<p>
     *
     * @param navEntry the entry to add
     *
     * @return the entry button component
     */
    public Button addSubNavEntry(final NavEntry navEntry) {

        Button button = CmsDefaultAppButtonProvider.createIconButton(
            navEntry.getName(),
            navEntry.getDescription(),
            navEntry.getIcon());
        m_subNav.addComponent(button);
        return button;
    }

    /**
     * Clears the sub navigation.<p>
     */
    public void clearSubNav() {

        m_subNav.removeAllComponents();
    }

    /**
     * Sets the main component.<p>
     *
     * @param component the main component
     */
    public void setMainContent(Component component) {

        component.addStyleName("borderless");
        m_main.setContent(component);
    }

    /**
     * Sets the height of the main panel to 100% to allow for scrollable children.<p>
     * If not set, the height of the main panel will adjust to it's content.<p>
     *
     * @param full <code>true</code> to set the height to 100%
     */
    public void setMainHeightFull(boolean full) {

        if (full) {
            m_main.setHeight("100%");
        } else {
            m_main.setHeightUndefined();
        }
    }

    /**
     * Shows or hides the sub navigation.<p>
     *
     * @param visible <code>true</code> to show the sub navigation
     */
    public void setSubNavVisible(boolean visible) {

        m_subNav.setVisible(visible);
        if (visible) {
            removeStyleName("o-tools-subnav-hidden");
        } else {
            addStyleName("o-tools-subnav-hidden");
        }
    }
}

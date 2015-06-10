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

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The layout used within the app view.<p>
 */
public class CmsAppViewLayout extends HorizontalLayout {

    /** The serial version id. */
    private static final long serialVersionUID = -290796815149968830L;

    /** The app area. */
    private CssLayout m_appArea;

    /** The menu area. */
    private CssLayout m_menuArea;

    /**
     * Constructor.<p>
     */
    public CmsAppViewLayout() {

        m_appArea = new CssLayout();
        m_menuArea = new CssLayout();
        setSizeFull();
        m_menuArea.setPrimaryStyleName(ValoTheme.MENU_ROOT);
        m_appArea.setPrimaryStyleName("valo-content");
        m_appArea.addStyleName("v-scrollable");
        m_appArea.setSizeFull();

        addComponents(m_menuArea, m_appArea);
        setExpandRatio(m_appArea, 1);
    }

    /**
     * Returns the app area component.<p>
     * 
     * @return the app area component
     */
    public ComponentContainer getAppContainer() {

        return m_appArea;
    }

    /**
     * Sets the menu component.<p>
     * 
     * @param menu the menu
     */
    public void setMenu(Component menu) {

        menu.addStyleName(ValoTheme.MENU_PART);
        m_menuArea.addComponent(menu);
    }
}

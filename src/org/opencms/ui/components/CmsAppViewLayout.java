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

package org.opencms.ui.components;

import org.opencms.ui.apps.I_CmsAppUIContext;

import com.vaadin.server.Resource;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.declarative.Design;

/**
 * The layout used within the app view.<p>
 */
public class CmsAppViewLayout extends CssLayout implements I_CmsAppUIContext {

    /** The serial version id. */
    private static final long serialVersionUID = -290796815149968830L;

    /** The app area. */
    private CssLayout m_appArea;

    /** The menu area. */
    private CssLayout m_menuArea;

    /** The toolbar. */
    private CmsToolBar m_toolbar;

    /**
     * Constructor.<p>
     */
    public CmsAppViewLayout() {

        Design.read("AppView.html", this);
        Responsive.makeResponsive(this);
        // setting the width to 100% within the java code is required by the responsive resize listeners
        setWidth("100%");
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#addToolbarButton(com.vaadin.ui.Component)
     */
    public void addToolbarButton(Component button) {

        m_toolbar.addButtonLeft(button);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#clearToolbarButtons()
     */
    public void clearToolbarButtons() {

        m_toolbar.clearButtonsLeft();
    }

    /**
     * Sets the app content component.<p>
     *
     * @param appContent the app content
     */
    public void setAppContent(Component appContent) {

        m_appArea.removeAllComponents();
        if (appContent != null) {
            m_appArea.addComponent(appContent);
        }
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#setAppIcon(com.vaadin.server.Resource)
     */
    public void setAppIcon(Resource icon) {

        m_toolbar.setAppIcon(icon);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#setAppInfo(com.vaadin.ui.Component)
     */
    public void setAppInfo(Component appInfo) {

        m_toolbar.setAppInfo(appInfo);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#setAppTitle(java.lang.String)
     */
    public void setAppTitle(String title) {

        m_toolbar.setAppTitle(title);
    }

    /**
     * Sets the menu component.<p>
     *
     * @param menu the menu
     */
    public void setMenuContent(Component menu) {

        m_menuArea.removeAllComponents();
        if (menu != null) {
            m_menuArea.addComponent(menu);
            addStyleName(OpenCmsTheme.WITH_MENU);
        } else {
            removeStyleName(OpenCmsTheme.WITH_MENU);
        }
    }
}

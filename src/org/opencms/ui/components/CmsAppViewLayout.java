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

import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.I_CmsAppUIContext;

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

    /** The info area grid. */
    private CssLayout m_infoArea;

    /** The toolbar. */
    private CmsToolBar m_toolbar;

    /**
     * Constructor.<p>
     */
    public CmsAppViewLayout() {

        Design.read("CmsAppView.html", this);
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
     * @see org.opencms.ui.apps.I_CmsAppUIContext#setAppInfo(com.vaadin.ui.Component)
     */
    public void setAppInfo(Component infoContent) {

        m_infoArea.removeAllComponents();
        m_infoArea.addComponent(infoContent);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#setAppTitle(java.lang.String)
     */
    public void setAppTitle(String title) {

        CmsAppWorkplaceUi.setWindowTitle(title);
        m_toolbar.setAppTitle(title);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#setMenuDialogContext(org.opencms.ui.I_CmsDialogContext)
     */
    public void setMenuDialogContext(I_CmsDialogContext context) {

        m_toolbar.setDialogContext(context);
    }

    /**
     * @see org.opencms.ui.apps.I_CmsAppUIContext#showInfoArea(boolean)
     */
    public void showInfoArea(boolean show) {

        m_infoArea.setVisible(show);
    }
}

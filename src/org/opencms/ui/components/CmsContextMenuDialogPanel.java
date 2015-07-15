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

import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Panel;

/**
 * Panel used to wrap dialogs triggered by the context menu in the explorer.<p>
 */
public class CmsContextMenuDialogPanel extends CssLayout {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Container for the resource information. */
    private ComponentContainer m_resourceInfos;

    /** Container for the content. */
    private ComponentContainer m_content;

    /** Panel for the resource information. */
    private Panel m_resourceInfoPanel;

    /**
     * Creates a new instance.<p>
     */
    public CmsContextMenuDialogPanel() {
        CmsVaadinUtils.readAndLocalizeDesign(
            this,
            OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale()),
            null);
        m_resourceInfos.setHeightUndefined();
        m_resourceInfoPanel.setVisible(false);

    }

    /**
     * Adds  a resource information widget.<p>
     *
     * @param resourceInfo the resource information widget
     */
    public void addResourceInfo(Component resourceInfo) {

        m_resourceInfoPanel.setVisible(true);

        m_resourceInfos.addComponent(resourceInfo);
    }

    /**
     * Sets the content widget.<p>
     *
     * @param component the content widget
     */
    public void setContent(Component component) {

        m_content.removeAllComponents();
        m_content.addComponent(component);
    }

}

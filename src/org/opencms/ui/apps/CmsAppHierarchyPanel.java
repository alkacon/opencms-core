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

import java.util.Locale;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Widget used to displays a nested hierarchy of app categories and apps.<p>
 */
public class CmsAppHierarchyPanel extends VerticalLayout {

    /** Serial version id.*/
    private static final long serialVersionUID = 1L;

    /** The panel to display the apps at this tree level. */
    private HorizontalLayout m_appPanel;

    /** The object used to generate the buttons for the apps. */
    private I_CmsAppButtonProvider m_appButtonProvider;

    /**
     * Creates a new instance.<p>
     *
     * @param buttonProvider the object to which we delegate the creation of app buttons
     */
    public CmsAppHierarchyPanel(I_CmsAppButtonProvider buttonProvider) {

        m_appPanel = new HorizontalLayout();
        m_appPanel.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        addComponent(m_appPanel);
        setMargin(true);
        setSpacing(true);
        m_appButtonProvider = buttonProvider;
    }

    /**
     * Adds a child category panel.<p>
     *
     * @param label the label
     * @param child the child widget
     */
    public void addChild(String label, CmsAppHierarchyPanel child) {

        Panel panel = new Panel();
        panel.setCaption(label);
        panel.setContent(child);
        addComponent(panel);
    }

    /**
     * Displays the given tree of categories and apps in the tree.<p>
     *
     * @param rootNode the rootNode to render in this panel
     * @param locale the locale to use
     */
    public void fill(CmsAppCategoryNode rootNode, Locale locale) {

        for (I_CmsWorkplaceAppConfiguration appConfig : rootNode.getAppConfigurations()) {
            m_appPanel.addComponent(m_appButtonProvider.createAppButton(appConfig));
        }
        for (CmsAppCategoryNode childNode : rootNode.getChildren()) {
            CmsAppHierarchyPanel childPanel = new CmsAppHierarchyPanel(m_appButtonProvider);
            addChild(childNode.getCategory().getName(locale), childPanel);
            childPanel.fill(childNode, locale);
        }
    }

}

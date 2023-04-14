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

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.dnd.event.DropEvent;
import com.vaadin.ui.dnd.event.DropListener;
import com.vaadin.ui.themes.ValoTheme;

/**
 * App to edit the quick launch menu.<p>
 */
public class CmsQuickLaunchEditor extends VerticalLayout {

    /**
     * The sorting drop listener.<p>
     */
    protected class LayoutDropListener implements DropListener<CssLayout> {

        /** The item height. */
        private static final int ITEM_HEIGHT = 88;

        /** The item width. */
        private static final int ITEM_WIDTH = 176;

        /** The layout width. */
        private static final int LAYOUT_WIDTH = 1158;

        /** The serial version id. */
        private static final long serialVersionUID = 8420945711551716630L;

        /** The drag target ordered layout. */
        private CssLayout m_layout;

        /**
         * Constructor.<p>
         *
         * @param layout the drop target layout
         */
        protected LayoutDropListener(CssLayout layout) {

            m_layout = layout;
        }

        /**
         * @see com.vaadin.ui.dnd.event.DropListener#drop(com.vaadin.ui.dnd.event.DropEvent)
         */
        public void drop(DropEvent<CssLayout> event) {

            // depending on the browser window width, different margins and paddings apply
            int layoutWidth = LAYOUT_WIDTH;
            int windowWidth = UI.getCurrent().getPage().getBrowserWindowWidth();
            if (windowWidth <= 983) {
                layoutWidth = windowWidth - 22;
            } else if (windowWidth <= 1220) {
                layoutWidth = windowWidth - 62;
            }
            int top = event.getMouseEventDetails().getRelativeY();
            int left = event.getMouseEventDetails().getRelativeX();
            int columnCount = layoutWidth / ITEM_WIDTH;
            int column = left / ITEM_WIDTH;
            int row = top / ITEM_HEIGHT;
            int index = (row * columnCount) + column;
            if (((column * ITEM_WIDTH) + (ITEM_WIDTH / 2)) < left) {
                index++;
            }
            Component sourceComponent = event.getDragSourceComponent().get();
            int currentIndex = m_layout.getComponentIndex(sourceComponent);
            if ((currentIndex != -1) && (currentIndex < index)) {
                index--;
            }

            if (currentIndex == index) {
                return;
            }

            // move component within the layout
            m_layout.removeComponent(sourceComponent);
            // avoid index out of bounds exceptions
            if (m_layout.getComponentCount() < index) {
                index = m_layout.getComponentCount();
            }
            m_layout.addComponent(sourceComponent, index);
        }
    }

    /** The serial version id. */
    private static final long serialVersionUID = -6608352673763873030L;

    /** The available apps drop target wrapper. */
    private CssLayout m_availableApps;

    /** The cancel button. */
    private Button m_reset;

    /** The standard apps layout. */
    private CssLayout m_standardApps;

    /** The user apps drop target wrapper. */
    private CssLayout m_userApps;

    /**
     * Constructor.<p>
     */
    public CmsQuickLaunchEditor() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        DropTargetExtension<CssLayout> userDrop = new DropTargetExtension<>(m_userApps);
        userDrop.addDropListener(new LayoutDropListener(m_userApps));
        m_userApps.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        DropTargetExtension<CssLayout> availablesDrop = new DropTargetExtension<>(m_availableApps);
        availablesDrop.addDropListener(new LayoutDropListener(m_availableApps));
        m_reset.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                resetAppIcons();
            }
        });
        addStyleName(OpenCmsTheme.QUICK_LAUNCH_EDITOR);
    }

    /**
     * Initializes the app icon items.<p>
     */
    protected void resetAppIcons() {

        CmsObject cms = A_CmsUI.getCmsObject();
        Locale locale = UI.getCurrent().getLocale();
        m_standardApps.removeAllComponents();
        m_userApps.removeAllComponents();
        m_availableApps.removeAllComponents();
        Collection<I_CmsWorkplaceAppConfiguration> allApps = OpenCms.getWorkplaceAppManager().getWorkplaceApps();
        Collection<I_CmsWorkplaceAppConfiguration> standardApps = OpenCms.getWorkplaceAppManager().getDefaultQuickLaunchConfigurations();
        Collection<I_CmsWorkplaceAppConfiguration> userApps = OpenCms.getWorkplaceAppManager().getUserQuickLauchConfigurations(
            cms);
        for (I_CmsWorkplaceAppConfiguration config : standardApps) {
            CmsAppVisibilityStatus visibility = config.getVisibility(cms);
            if (visibility.isVisible()) {
                Button button = CmsDefaultAppButtonProvider.createAppIconButton(config, locale);
                m_standardApps.addComponent(button);
            }
        }
        for (I_CmsWorkplaceAppConfiguration config : userApps) {
            CmsAppVisibilityStatus visibility = config.getVisibility(cms);
            if (visibility.isVisible() && visibility.isActive()) {
                Button button = CmsDefaultAppButtonProvider.createAppIconButton(config, locale);
                //    button.setWidth("166px");
                DragSourceExtension<Button> extButton = new DragSourceExtension<>(button);
                button.setData(config.getId());
                extButton.setDataTransferText(config.getId());
                m_userApps.addComponent(button);
            }
        }
        for (I_CmsWorkplaceAppConfiguration config : allApps) {
            CmsAppVisibilityStatus visibility = config.getVisibility(cms);
            if (!standardApps.contains(config)
                && !userApps.contains(config)
                && visibility.isVisible()
                && visibility.isActive()) {
                Button button = CmsDefaultAppButtonProvider.createAppIconButton(config, locale);
                //  button.setWidth("166px");
                DragSourceExtension<Button> extButton = new DragSourceExtension<>(button);
                button.setData(config.getId());
                extButton.setDataTransferText(config.getId());
                m_availableApps.addComponent(button);
            }
        }
    }

    /**
     * Saves the changed apps setting.<p>
     */
    void saveToUser() {

        List<String> apps = new ArrayList<String>();
        int count = m_userApps.getComponentCount();
        for (int i = 0; i < count; i++) {
            Button button = (Button)m_userApps.getComponent(i);
            apps.add((String)button.getData());
        }

        try {
            OpenCms.getWorkplaceAppManager().setUserQuickLaunchApps(A_CmsUI.getCmsObject(), apps);
        } catch (Exception e) {
            CmsErrorDialog.showErrorDialog("Could not write user Quicklaunch apps", e);
        }
    }
}

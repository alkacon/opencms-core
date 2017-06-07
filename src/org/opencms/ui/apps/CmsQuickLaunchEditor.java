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
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsWrappedHorizontalLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.WrapperTargetDetails;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * App to edit the quick launch menu.<p>
 */
public class CmsQuickLaunchEditor extends VerticalLayout {

    /**
     * The drag and drop handler to add and sort the apps.<p>
     */
    private static class ReorderLayoutDropHandler implements DropHandler {

        /** The item height. */
        private static final int ITEM_HEIGHT = 88;

        /** The item width. */
        private static final int ITEM_WIDTH = 176;

        /** The layout width. */
        private static final int LAYOUT_WIDTH = 1158;

        /** The serial version id. */
        private static final long serialVersionUID = 7598829826841275823L;

        /** The drag target ordered layout. */
        private AbstractOrderedLayout m_layout;

        /** Flag indicating that the layout is only sorted vertically. */
        private boolean m_verticalOnly;

        /**
         * Constructor.<p>
         *
         * @param layout the drag target layout
         * @param verticalOnly <code>true</code> to only sort vertically
         */
        public ReorderLayoutDropHandler(AbstractOrderedLayout layout, boolean verticalOnly) {
            m_layout = layout;
            m_verticalOnly = verticalOnly;
        }

        /**
         * @see com.vaadin.event.dd.DropHandler#drop(com.vaadin.event.dd.DragAndDropEvent)
         */
        @Override
        public void drop(final DragAndDropEvent dropEvent) {

            WrapperTargetDetails targetDetails = (WrapperTargetDetails)dropEvent.getTargetDetails();
            Transferable transferable = dropEvent.getTransferable();
            Component sourceComponent = transferable.getSourceComponent();
            int top = targetDetails.getMouseEvent().getClientY() - targetDetails.getAbsoluteTop().intValue();

            // calculate the target index using the known components dimensions
            int index;
            if (m_verticalOnly) {
                index = top / ITEM_HEIGHT;
                if (((index * ITEM_HEIGHT) + (ITEM_HEIGHT / 2)) < top) {
                    index++;
                }
            } else {
                int columnCount = LAYOUT_WIDTH / ITEM_WIDTH;
                int left = targetDetails.getMouseEvent().getClientX() - targetDetails.getAbsoluteLeft().intValue();
                int column = left / ITEM_WIDTH;
                int row = top / ITEM_HEIGHT;
                index = (row * columnCount) + column;
                if (((column * ITEM_WIDTH) + (ITEM_WIDTH / 2)) < left) {
                    index++;
                }
            }
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

        /**
         * @see com.vaadin.event.dd.DropHandler#getAcceptCriterion()
         */
        @Override
        public AcceptCriterion getAcceptCriterion() {

            return AcceptAll.get();
        }
    }

    /**
     * The draggable wrapper.<p>
     */
    private static class WrappedDraggableComponent extends DragAndDropWrapper {

        /** The serial version id. */
        private static final long serialVersionUID = 5204771630321411021L;

        /** The item id. */
        private String m_id;

        /**
         * Constructor.<p>
         *
         * @param content the component to wrap
         * @param id the item id
         */
        public WrappedDraggableComponent(Component content, String id) {
            super(content);
            m_id = id;
            setDragStartMode(DragStartMode.WRAPPER);
            setWidth("166px");
        }

        /**
         * Returns the item id.<p>
         *
         * @return the item id
         */
        public String getItemId() {

            return m_id;
        }
    }

    /** The serial version id. */
    private static final long serialVersionUID = -6608352673763873030L;

    /** The available apps drop target wrapper. */
    private CmsWrappedHorizontalLayout m_availableApps;

    /** The cancel button. */
    private Button m_cancel;

    /** The save button. */
    private Button m_save;

    /** The standard apps layout. */
    private HorizontalLayout m_standardApps;

    /** The user apps drop target wrapper. */
    private CmsWrappedHorizontalLayout m_userApps;

    /**
     * Constructor.<p>
     */
    public CmsQuickLaunchEditor() {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_userApps.setDropHandler(new ReorderLayoutDropHandler(m_userApps.getWrappedLayout(), false));
        m_userApps.getWrappedLayout().setMargin(true);
        m_userApps.getWrappedLayout().addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        m_availableApps.setDropHandler(new ReorderLayoutDropHandler(m_availableApps.getWrappedLayout(), false));
        m_availableApps.getWrappedLayout().setMargin(true);
        m_availableApps.getWrappedLayout().addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        m_save.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                saveToUser();
            }
        });
        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                close();
            }
        });
    }

    /**
     * Initializes the app icon items.<p>
     */
    protected void initAppIcons() {

        CmsObject cms = A_CmsUI.getCmsObject();
        Locale locale = UI.getCurrent().getLocale();
        Collection<I_CmsWorkplaceAppConfiguration> allApps = OpenCms.getWorkplaceAppManager().getWorkplaceApps();
        Collection<I_CmsWorkplaceAppConfiguration> standardApps = OpenCms.getWorkplaceAppManager().getDefaultQuickLaunchConfigurations();
        Collection<I_CmsWorkplaceAppConfiguration> userApps = OpenCms.getWorkplaceAppManager().getUserQuickLauchConfigurations(
            cms);
        for (I_CmsWorkplaceAppConfiguration config : standardApps) {
            CmsAppVisibilityStatus visibility = config.getVisibility(cms);
            if (visibility.isVisible() && visibility.isActive()) {
                Button button = CmsDefaultAppButtonProvider.createAppIconButton(config, locale);
                m_standardApps.addComponent(button);
            }
        }
        for (I_CmsWorkplaceAppConfiguration config : userApps) {
            CmsAppVisibilityStatus visibility = config.getVisibility(cms);
            if (visibility.isVisible() && visibility.isActive()) {
                Button button = CmsDefaultAppButtonProvider.createAppIconButton(config, locale);
                m_userApps.getWrappedLayout().addComponent(new WrappedDraggableComponent(button, config.getId()));
            }
        }
        for (I_CmsWorkplaceAppConfiguration config : allApps) {
            CmsAppVisibilityStatus visibility = config.getVisibility(cms);
            if (!standardApps.contains(config)
                && !userApps.contains(config)
                && visibility.isVisible()
                && visibility.isActive()) {
                Button button = CmsDefaultAppButtonProvider.createAppIconButton(config, locale);
                m_availableApps.getWrappedLayout().addComponent(new WrappedDraggableComponent(button, config.getId()));
            }
        }
    }

    /**
     * Cancels editing and restores the previous quick launch apps setting.<p>
     */
    void close() {

        CmsAppWorkplaceUi.get().getNavigator().navigateTo(CmsAppHierarchyConfiguration.APP_ID);
    }

    /**
     * Saves the changed apps setting.<p>
     */
    void saveToUser() {

        List<String> apps = new ArrayList<String>();
        HorizontalLayout appsLayout = m_userApps.getWrappedLayout();
        int count = appsLayout.getComponentCount();
        for (int i = 0; i < count; i++) {
            WrappedDraggableComponent wrapper = (WrappedDraggableComponent)appsLayout.getComponent(i);
            apps.add(wrapper.getItemId());
        }

        try {
            OpenCms.getWorkplaceAppManager().setUserQuickLaunchApps(A_CmsUI.getCmsObject(), apps);
        } catch (CmsException e) {
            CmsErrorDialog.showErrorDialog("Could not write user Quicklaunch apps", e);
        }
        close();
    }
}

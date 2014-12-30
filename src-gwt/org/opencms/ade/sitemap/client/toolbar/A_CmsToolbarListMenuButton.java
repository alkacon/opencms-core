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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsMenuButton;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsToolbarPopup;
import org.opencms.gwt.client.ui.I_CmsListItem;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A menu button with list tabs.<p>
 * 
 * @since 8.0.0
 */
public abstract class A_CmsToolbarListMenuButton extends CmsMenuButton implements I_CmsToolbarActivatable {

    /** The dialog width. */
    public static final int DIALOG_WIDTH = 600;

    /** Text metrics key for truncation. */
    public static final String TM_LITST_MENU = "TM_LITST_MENU";

    /** Flag to indicate if the menu tabs have been initialized. */
    protected boolean m_initialized;

    /** The controller instance. */
    private CmsSitemapController m_controller;

    /** The tab panel. */
    protected CmsTabbedPanel<CmsListTab> m_tabs;

    /** The toolbar instance. */
    private CmsSitemapToolbar m_toolbar;

    /**
     * Constructor.<p>
     * 
     * @param title the button title
     * @param iconClass the icon CSS class
     * @param toolbar the toolbar instance
     * @param controller the controller instance
     */
    public A_CmsToolbarListMenuButton(
        String title,
        String iconClass,
        CmsSitemapToolbar toolbar,
        CmsSitemapController controller) {

        super(null, iconClass);
        m_toolbar = toolbar;
        m_controller = controller;
        setTitle(title);
        setToolbarMode(true);
        setOpenRight(true);
        if (!m_controller.isEditable()) {
            setEnabled(false);
        }

        m_tabs = new CmsTabbedPanel<CmsListTab>();
        m_tabs.addSelectionHandler(new SelectionHandler<Integer>() {

            public void onSelection(SelectionEvent<Integer> event) {

                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    public void execute() {

                        updateSize();
                    }
                });
            }
        });
        SimplePanel tabsContainer = new SimplePanel();
        tabsContainer.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.clipboardCss().menuTabContainer());
        int dialogHeight = CmsToolbarPopup.getAvailableHeight();
        int dialogWidth = CmsToolbarPopup.getAvailableWidth();
        tabsContainer.setHeight(dialogHeight + "px");
        getPopup().setWidth(dialogWidth);
        tabsContainer.add(m_tabs);
        FlowPanel content = new FlowPanel();
        content.add(tabsContainer);
        setMenuWidget(content);
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                if (!isOpen()) {
                    getToolbar().onButtonActivation(A_CmsToolbarListMenuButton.this);
                    if (!m_initialized) {
                        // lazy initialization
                        m_initialized = initContent();
                    }

                    openMenu();
                    updateSize();
                } else {
                    closeMenu();
                }
            }
        });
    }

    /**
     * Adds a new tab to the tab-panel.<p>
     * 
     * @param tab the tab
     * @param title the tab title
     */
    public void addTab(CmsListTab tab, String title) {

        m_tabs.add(tab, title);
    }

    /**
     * Creates a new tab.<p>
     * 
     * @param list list of items
     * 
     * @return the created tab widget
     */
    public CmsListTab createTab(CmsList<? extends I_CmsListItem> list) {

        return new CmsListTab(list);
    }

    /**
     * @see org.opencms.ade.sitemap.client.toolbar.I_CmsToolbarActivatable#onActivation(com.google.gwt.user.client.ui.Widget)
     */
    public void onActivation(Widget widget) {

        closeMenu();
    }

    /**
     * Updates the dialog size according to the current tab content.<p>
     */
    public void updateSize() {

        int width = CmsToolbarPopup.getAvailableWidth();
        getPopup().setWidth(width);
        CmsListTab tab = m_tabs.getWidget(m_tabs.getSelectedIndex());
        tab.truncate(TM_LITST_MENU, width);
        int availableHeight = CmsToolbarPopup.getAvailableHeight();
        int requiredHeight = tab.getRequiredHeight() + 36;
        int height = (availableHeight > requiredHeight) && (requiredHeight > 50) ? requiredHeight : availableHeight;
        m_tabs.getParent().setHeight(height + "px");
        tab.getScrollPanel().onResizeDescendant();
    }

    /**
     * Returns the controller.<p>
     *
     * @return the controller
     */
    protected CmsSitemapController getController() {

        return m_controller;
    }

    /**
     * Returns the toolbar.<p>
     *
     * @return the toolbar
     */
    protected CmsSitemapToolbar getToolbar() {

        return m_toolbar;
    }

    /**
     * Initializes the menu tabs.<p>
     * 
     * @return true if the content does not need to be initialized the next time the menu is opened 
     */
    protected abstract boolean initContent();
}

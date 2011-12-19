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
import org.opencms.gwt.client.ui.I_CmsListItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
    private CmsTabbedPanel<FlowPanel> m_tabs;

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

        m_tabs = new CmsTabbedPanel<FlowPanel>();
        SimplePanel tabsContainer = new SimplePanel();
        tabsContainer.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.clipboardCss().menuTabContainer());
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
                        initContent();
                        m_initialized = true;
                    }

                    openMenu();
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
    public void addTab(FlowPanel tab, String title) {

        m_tabs.add(tab, title);
    }

    /**
     * Creates a new tab.<p>
     * 
     * @param list list of items
     * 
     * @return the created tab widget
     */
    public FlowPanel createTab(CmsList<? extends I_CmsListItem> list) {

        FlowPanel tab = new FlowPanel();
        tab.setStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().tabPanel());
        list.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().buttonCornerAll());
        list.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.clipboardCss().clipboardList());
        tab.add(list);
        list.truncate(TM_LITST_MENU, DIALOG_WIDTH);
        return tab;
    }

    /**
     * @see org.opencms.ade.sitemap.client.toolbar.I_CmsToolbarActivatable#onActivation(com.google.gwt.user.client.ui.Widget)
     */
    public void onActivation(Widget widget) {

        closeMenu();
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
     */
    protected abstract void initContent();
}

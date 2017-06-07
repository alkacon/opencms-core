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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenu;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuCloseHandler;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.util.CmsClientCollectionUtil;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;

import java.util.List;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The context tool-bar menu button.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarContextButton extends A_CmsToolbarMenu<I_CmsToolbarHandler> {

    /** The menu data. */
    protected List<I_CmsContextMenuEntry> m_menuEntries;

    /** The loading panel. */
    private CmsLoadingAnimation m_loadingPanel;

    /** The context menu. */
    private CmsContextMenu m_menu;

    /** The registration for the first close handler. */
    private HandlerRegistration m_menuCloseHandler;

    /** Context used for loading the context menu entries. */
    private AdeContext m_menuContext = AdeContext.containerpage;

    /** The main content widget. */
    private FlexTable m_menuPanel;

    /** The label which is displayed when no entries are found. */
    private CmsLabel m_noEntriesLabel;

    /** The registration for the second close handler. */
    private HandlerRegistration m_popupCloseHandler;

    /**
     * Constructor.<p>
     *
     * @param handler the container-page handler
     */
    public CmsToolbarContextButton(final I_CmsToolbarHandler handler) {

        super(I_CmsButton.ButtonData.CONTEXT, handler);
        m_noEntriesLabel = new CmsLabel(Messages.get().key(Messages.GUI_TOOLBAR_CONTEXT_EMPTY_0));
        m_noEntriesLabel.addStyleName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().menuInfoLabel());
        m_noEntriesLabel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().buttonCornerAll());
        m_loadingPanel = new CmsLoadingAnimation();
        // create the menu panel (it's a table because of ie6)
        m_menuPanel = new FlexTable();
        // set a style name for the menu table
        m_menuPanel.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().menuPanel());

        // set the widget
        setMenuWidget(m_menuPanel);

        // clear the width of the popup content
        getPopup().setWidth(0);
        getPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().contextMenu());
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        getHandler().loadContextMenu(CmsCoreProvider.get().getStructureId(), m_menuContext);
    }

    /**
     * Unregister the resize handler.<p>
     *
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        if (m_resizeRegistration != null) {
            m_resizeRegistration.removeHandler();
            m_resizeRegistration = null;
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsMenuButton#openMenu()
     */
    @Override
    public void openMenu() {

        if (m_menu == null) {
            m_menuPanel.setWidget(0, 0, m_loadingPanel);
        }
        super.openMenu();

    }

    /**
     * Sets the menu context.<p>
     *
     * @param menuContext the new menu context
     */
    public void setMenuContext(AdeContext menuContext) {

        m_menuContext = menuContext;
    }

    /**
     * Creates the menu and adds it to the panel.<p>
     *
     * @param menuEntries the menu entries
     */
    public void showMenu(List<I_CmsContextMenuEntry> menuEntries) {

        if (!CmsClientCollectionUtil.isEmptyOrNull(menuEntries)) {
            // if there were entries found for the menu, create the menu
            m_menu = new CmsContextMenu(menuEntries, true, getPopup());
            // add the resize handler for the menu
            m_resizeRegistration = Window.addResizeHandler(m_menu);
            // set the menu as widget for the panel
            m_menuPanel.setWidget(0, 0, m_menu);
            if (m_menuCloseHandler != null) {
                m_menuCloseHandler.removeHandler();
            }
            if (m_popupCloseHandler != null) {
                m_popupCloseHandler.removeHandler();
            }
            // add the close handler for the menu
            m_menuCloseHandler = getPopup().addCloseHandler(new CmsContextMenuCloseHandler(m_menu));
            m_popupCloseHandler = getPopup().addCloseHandler(new CloseHandler<PopupPanel>() {

                public void onClose(CloseEvent<PopupPanel> event) {

                    setActive(false);
                }
            });
            m_popup.position();
        } else {
            m_menuPanel.setWidget(0, 0, m_noEntriesLabel);
            m_popup.position();

        }
    }
}
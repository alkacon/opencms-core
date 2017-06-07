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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsLoadingAnimation;
import org.opencms.gwt.client.ui.CmsMenuButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.util.CmsClientCollectionUtil;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The result item context menu button.<p>
 */
public class CmsContextMenuButton extends CmsMenuButton {

    /** The menu data. */
    protected List<I_CmsContextMenuEntry> m_menuEntries;

    /** The loading panel. */
    private CmsLoadingAnimation m_loadingPanel;

    /** The context menu. */
    private CmsContextMenu m_menu;

    /** The registration for the first close handler. */
    private HandlerRegistration m_menuCloseHandler;

    /** The main content widget. */
    private FlexTable m_menuPanel;

    /** The label which is displayed when no entries are found. */
    private CmsLabel m_noEntriesLabel;

    /** The registration for the second close handler. */
    private HandlerRegistration m_popupCloseHandler;

    /**
     * Constructor.<p>
     *
     * @param structureId the resource structure id
     * @param handler the context menu handler
     */
    public CmsContextMenuButton(final CmsUUID structureId, final CmsContextMenuHandler handler) {

        super(null, I_CmsButton.CONTEXT_MENU_SMALL);
        m_button.setSize(I_CmsButton.Size.medium);
        setTitle(Messages.get().key(Messages.GUI_TOOLBAR_CONTEXT_0));
        m_noEntriesLabel = new CmsLabel(Messages.get().key(Messages.GUI_TOOLBAR_CONTEXT_EMPTY_0));
        m_noEntriesLabel.addStyleName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().menuInfoLabel());
        m_noEntriesLabel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().buttonCornerAll());
        m_loadingPanel = new CmsLoadingAnimation();
        // create the menu panel (it's a table because of ie6)
        m_menuPanel = new FlexTable();
        // set a style name for the menu table
        m_menuPanel.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().menuPanel());
        m_button.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        // set the widget
        setMenuWidget(m_menuPanel);

        // clear the width of the popup content
        getPopup().setWidth(0);
        getPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().contextMenu());

        setToolbarMode(false);
        getPopup().addAutoHidePartner(getElement());
        getPopup().addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                getParent().removeStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
            }
        });
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                if (!isOpen()) {
                    openMenu();
                    getParent().addStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
                    handler.loadContextMenu(structureId, AdeContext.containerpage, CmsContextMenuButton.this);
                } else {
                    hideMenu();
                }
            }

        });
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

                    closeMenu();
                }
            });
            m_popup.position();
        } else {
            m_menuPanel.setWidget(0, 0, m_noEntriesLabel);
            m_popup.position();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsMenuButton#hideMenu()
     *
     * Needed to increase visibility.<p>
     */
    @Override
    protected void hideMenu() {

        super.hideMenu();
    }
}

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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.CmsFavoritesDNDController;
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.A_CmsToolbarMenu;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsToolbarPopup;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.util.CmsDebugLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The clip-board tool-bar menu.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarClipboardMenu extends A_CmsToolbarMenu<CmsContainerpageHandler> {

    /** The favorite list widget. */
    protected CmsFavoriteTab m_favorites;

    /** Flag to indicate if the favorites are being edited. */
    protected boolean m_isEditingFavorites;

    /** The main content widget. */
    private FlowPanel m_content;

    /** The favorites editing drag and drop controller. */
    private CmsFavoritesDNDController m_dndController;

    /** The recent list widget. */
    private CmsRecentTab m_recent;

    /** The favorite and recent list tabs. */
    CmsTabbedPanel<A_CmsClipboardTab> m_tabs;

    /**
     * Constructor.<p>
     *
     * @param handler the container-page handler
     */
    public CmsToolbarClipboardMenu(CmsContainerpageHandler handler) {

        super(I_CmsButton.ButtonData.CLIPBOARD_BUTTON, handler);

        m_content = new FlowPanel();
        m_tabs = new CmsTabbedPanel<A_CmsClipboardTab>();
        m_favorites = new CmsFavoriteTab(this);
        m_recent = new CmsRecentTab();

        m_tabs.add(m_favorites, Messages.get().key(Messages.GUI_TAB_FAVORITES_TITLE_0));
        m_tabs.add(m_recent, Messages.get().key(Messages.GUI_TAB_RECENT_TITLE_0));
        m_tabs.addSelectionHandler(new SelectionHandler<Integer>() {

            /**
             * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(com.google.gwt.event.logical.shared.SelectionEvent)
             */
            public void onSelection(SelectionEvent<Integer> event) {

                if (m_isEditingFavorites) {
                    m_favorites.saveFavorites();
                }
                CmsContainerpageController.get().saveClipboardTab(event.getSelectedItem().intValue());
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    public void execute() {

                        updateSize();
                    }
                });

            }
        });

        SimplePanel tabsContainer = new SimplePanel();
        tabsContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().menuTabContainer());
        int dialogHeight = CmsToolbarPopup.getAvailableHeight();
        int dialogWidth = CmsToolbarPopup.getAvailableWidth();
        tabsContainer.setHeight(dialogHeight + "px");
        getPopup().setWidth(dialogWidth);
        tabsContainer.add(m_tabs);
        m_content.add(tabsContainer);
        setMenuWidget(m_content);
        m_dndController = new CmsFavoritesDNDController();
    }

    /**
     * Adds an element to the favorite list widget.<p>
     *
     * @param listItem the item widget
     */
    public void addToFavorites(CmsListItem listItem) {

        m_favorites.addListItem(listItem);
    }

    /**
     * Adds an element to the recent list widget.<p>
     *
     * @param listItem the item widget
     */
    public void addToRecent(CmsListItem listItem) {

        m_recent.addListItem(listItem);
    }

    /**
     * Clears the contents of the favorite list widget.<p>
     */
    public void clearFavorites() {

        m_favorites.clearList();
    }

    /**
     * Clears the contents of the recent list widget.<p>
     */
    public void clearRecent() {

        m_recent.clearList();
    }

    /**
     * Enables the favorite list editing.<p>
     */
    public void enableFavoritesEdit() {

        m_isEditingFavorites = true;
        getHandler().enableFavoriteEditing(true, m_dndController);
        Iterator<Widget> it = m_favorites.iterator();
        while (it.hasNext()) {

            CmsMenuListItem element = (CmsMenuListItem)it.next();
            element.hideEditButton();
            element.showRemoveButton();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        Document.get().getBody().addClassName(I_CmsButton.ButtonData.CLIPBOARD_BUTTON.getIconClass());
        getHandler().loadFavorites();
        getHandler().loadRecent();
        CmsRpcAction<Integer> tabAction = new CmsRpcAction<Integer>() {

            @Override
            public void execute() {

                start(1, false);
                CmsContainerpageController.get().getContainerpageService().loadClipboardTab(this);
            }

            @Override
            protected void onResponse(Integer result) {

                stop(false);
                m_tabs.selectTab(result.intValue(), false);
                updateSize();
            }

        };
        tabAction.execute();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        if (m_isEditingFavorites) {
            m_favorites.saveFavorites();
        }
        Document.get().getBody().removeClassName(I_CmsButton.ButtonData.CLIPBOARD_BUTTON.getIconClass());
    }

    /**
     * Reloads the favorite list.<p>
     */
    public void reloadFavorites() {

        m_isEditingFavorites = false;
        getHandler().enableFavoriteEditing(false, m_dndController);
        getHandler().loadFavorites();
    }

    /**
     * Replaces old versions of the given item with the new one.<p>
     *
     * @param listItem the list item
     */
    public void replaceFavoriteItem(CmsListItem listItem) {

        m_favorites.replaceItem(listItem);
    }

    /**
     * Replaces old versions of the given item with the new one.<p>
     *
     * @param listItem the list item
     */
    public void replaceRecentItem(CmsListItem listItem) {

        m_recent.replaceItem(listItem);
    }

    /**
     * Saves the favorite list.<p>
     */
    public void saveFavorites() {

        m_isEditingFavorites = false;
        getHandler().enableFavoriteEditing(false, m_dndController);
        List<String> clientIds = new ArrayList<String>();
        Iterator<Widget> it = m_favorites.iterator();
        while (it.hasNext()) {
            try {
                CmsMenuListItem element = (CmsMenuListItem)it.next();
                element.hideRemoveButton();
                element.showEditButton();
                clientIds.add(element.getId());
            } catch (ClassCastException e) {
                CmsDebugLog.getInstance().printLine("Could not cast widget");
            }
        }
        getHandler().saveFavoriteList(clientIds);
    }

    /**
     * Updates the popup size according to the tab contents.<p>
     */
    public void updateSize() {

        int availableHeight = CmsToolbarPopup.getAvailableHeight();
        int dialogWidth = CmsToolbarPopup.getAvailableWidth();

        A_CmsClipboardTab tab = m_tabs.getWidget(m_tabs.getSelectedIndex());
        int requiredHeight = tab.getRequiredHeight() + 31;
        int dialogHeight = availableHeight > requiredHeight ? requiredHeight : availableHeight;
        m_tabs.getParent().setHeight(dialogHeight + "px");
        getPopup().setWidth(dialogWidth);
        tab.getList().truncate("CLIPBOARD_TM", dialogWidth - 40);
        tab.getScrollPanel().onResizeDescendant();
    }
}

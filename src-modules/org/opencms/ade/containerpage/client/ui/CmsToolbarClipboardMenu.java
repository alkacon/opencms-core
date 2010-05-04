/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsToolbarClipboardMenu.java,v $
 * Date   : $Date: 2010/05/04 13:17:36 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.draganddrop.CmsDragMenuElement;
import org.opencms.ade.containerpage.client.draganddrop.CmsDragTargetMenu;
import org.opencms.ade.containerpage.client.draganddrop.CmsMenuDragHandler;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.I_CmsButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The clip-board tool-bar menu.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.13 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarClipboardMenu extends A_CmsToolbarMenu {

    /** The main content widget. */
    private FlowPanel m_content;

    /** The favorite list drop-zone. */
    private CmsDragTargetMenu m_dropzone;

    /** The favorite list widget. */
    private CmsFavoriteTab m_favorites;

    /** The menu drag handler. */
    private CmsMenuDragHandler m_menuDragHandler;

    /** The recent list widget. */
    private CmsRecentTab m_recent;

    /** The favorite and recent list tabs. */
    private CmsTabbedPanel<Widget> m_tabs;

    /**
     * Constructor.<p>
     * 
     * @param handler the container-page handler
     */
    public CmsToolbarClipboardMenu(CmsContainerpageHandler handler) {

        super(I_CmsButton.ButtonData.CLIPBOARD, handler);

        m_content = new FlowPanel();
        m_content.setStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().menuContent());
        m_tabs = new CmsTabbedPanel<Widget>();
        m_favorites = new CmsFavoriteTab(this);
        m_recent = new CmsRecentTab();

        m_tabs.add(m_favorites, Messages.get().key(Messages.GUI_TAB_FAVORITES_TITLE_0));
        m_tabs.add(m_recent, Messages.get().key(Messages.GUI_TAB_RECENT_TITLE_0));
        SimplePanel tabsContainer = new SimplePanel();
        tabsContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().menuTabContainer());
        tabsContainer.add(m_tabs);
        m_content.add(tabsContainer);

        m_dropzone = new CmsDragTargetMenu();
        m_dropzone.setStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().clipboardDropzone());

        m_content.add(m_dropzone);
        m_menuDragHandler = new CmsMenuDragHandler();

        setMenuWidget(m_content);
    }

    /**
     * Adds an element to the favorite list widget.<p>
     * 
     * @param listItem the item widget
     */
    public void addToFavorites(CmsDragMenuElement listItem) {

        m_favorites.addListItem(listItem);
    }

    /**
     * Adds an element to the recent list widget.<p>
     * 
     * @param listItem the item widget
     */
    public void addToRecent(CmsDragMenuElement listItem) {

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

        Iterator<Widget> it = m_favorites.iterator();
        while (it.hasNext()) {
            CmsDragMenuElement element = (CmsDragMenuElement)it.next();
            element.showDeleteButton();

            // disabling the container-page drag and enabling the menu drag
            element.removeDndMouseHandlers();
            element.setDragParent(m_favorites.getListTarget());
            m_menuDragHandler.registerMouseHandler(element);
        }
    }

    /**
     * Returns the tool-bar drop-zone.<p>
     *
     * @return the drop-zone
     */
    public CmsDragTargetMenu getDropzone() {

        return m_dropzone;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        getHandler().loadFavorites();
        getHandler().loadRecent();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        // nothing to do here
    }

    /**
     * Reloads the favorite list.<p>
     */
    public void reloadFavorites() {

        getHandler().loadFavorites();
    }

    /**
     * Saves the favorite list.<p>
     */
    public void saveFavorites() {

        List<String> clientIds = new ArrayList<String>();
        Iterator<Widget> it = m_favorites.iterator();
        while (it.hasNext()) {
            CmsDragMenuElement element = (CmsDragMenuElement)it.next();
            element.hideDeleteButton();
            clientIds.add(element.getClientId());

            // disabling the menu drag and re-enabling the container-page drag
            element.removeDndMouseHandlers();
            getHandler().enableDragHandler(element);
        }
        getHandler().saveFavoriteList(clientIds);
    }

    /**
     * Opens the menu showing the favorite list drop-zone and hiding all other menu content.<p>
     * 
     * @param show <code>true</code> to show the drop-zone
     */
    public void showDropzone(boolean show) {

        if (show) {
            m_content.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().showDropzone());
            openMenu();
        } else {
            m_content.removeStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().showDropzone());
            closeMenu();
        }
    }

}

/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsToolbarClipboardMenu.java,v $
 * Date   : $Date: 2010/04/14 14:33:47 $
 * Version: $Revision: 1.7 $
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

import org.opencms.ade.containerpage.client.CmsContainerpageDataProvider;
import org.opencms.ade.containerpage.client.draganddrop.CmsContainerDragHandler;
import org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement;
import org.opencms.ade.containerpage.client.draganddrop.CmsDragTargetMenu;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.gwt.client.ui.CmsDraggableListItemWidget;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsToolbarButton;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * The clip-board tool-bar menu.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarClipboardMenu extends A_CmsToolbarMenu {

    /** The button name. */
    public static final String BUTTON_NAME = "clipboard";

    /** The main content widget. */
    private FlowPanel m_content;

    /** The favorite list drop-zone. */
    private CmsDragTargetMenu m_dropzone;

    /** The favorite list widget. */
    private FlowPanel m_favorites;

    /** The recent list widget. */
    private FlowPanel m_recent;

    /** The favorite and recent list tabs. */
    private CmsTabbedPanel m_tabs;

    /**
     * Constructor.<p>
     */
    public CmsToolbarClipboardMenu() {

        super(CmsToolbarButton.ButtonData.CLIPBOARD, BUTTON_NAME, true);

        //TODO: replace the following with the real menu content
        m_content = new FlowPanel();
        m_content.setStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().menuContent());
        m_tabs = new CmsTabbedPanel();
        m_favorites = new FlowPanel();
        m_recent = new FlowPanel();

        // TODO: add localization
        m_tabs.add(m_favorites, "Favorites");
        m_tabs.add(m_recent, "Recent");
        SimplePanel tabsContainer = new SimplePanel();
        tabsContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().clipboardTabs());
        tabsContainer.add(m_tabs);
        m_content.add(tabsContainer);

        m_dropzone = new CmsDragTargetMenu();
        m_dropzone.setStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().clipboardDropzone());

        m_content.add(m_dropzone);

        setMenuWidget(m_content);
    }

    /**
     * Adds an element to the favorite list widget.<p>
     * 
     * @param element the element data
     */
    public void addToFavorites(CmsContainerElement element) {

        CmsDraggableListItemWidget listItem = new CmsDraggableListItemWidget(new CmsListInfoBean(
            element.getTitle(),
            element.getFile(),
            null), true);
        listItem.setClientId(element.getClientId());
        CmsContainerDragHandler.get().registerMouseHandler(listItem);
        m_favorites.add(listItem);
    }

    /**
     * Adds an element to the recent list widget.<p>
     * 
     * @param element the element data
     */
    public void addToRecent(CmsContainerElement element) {

        CmsDraggableListItemWidget listItem = new CmsDraggableListItemWidget(new CmsListInfoBean(
            element.getTitle(),
            element.getFile(),
            null), true);
        listItem.setClientId(element.getClientId());
        CmsContainerDragHandler.get().registerMouseHandler(listItem);
        m_recent.add(listItem);
    }

    /**
     * Clears the contents of the favorite list widget.<p>
     */
    public void clearFavorites() {

        m_favorites.clear();
    }

    /**
     * Clears the contents of the recent list widget.<p>
     */
    public void clearRecent() {

        m_recent.clear();
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
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#hasPermissions(org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement)
     */
    public boolean hasPermissions(CmsDragContainerElement element) {

        // no element option available
        return false;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsMenuButton#hideMenu()
     */
    @Override
    public void hideMenu() {

        super.hideMenu();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#init()
     */
    public void init() {

        // nothing to do here

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        CmsContainerpageDataProvider.get().loadFavorites();
        CmsContainerpageDataProvider.get().loadRecent();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.I_CmsContainerpageToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        // nothing to do here

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

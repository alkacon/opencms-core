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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.ui.CmsMenuButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenu;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuCloseHandler;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Sitemap context menu button.<p>
 *
 * @since 8.0.0
 */
public class CmsHoverbarContextMenuButton extends CmsMenuButton implements I_CmsContextMenuItemProvider {

    /** The context menu entries. */
    private List<A_CmsSitemapMenuEntry> m_entries;

    /** The main content widget. */
    private FlexTable m_menuPanel;

    /**
     * Constructor.<p>
     *
     * @param hoverbar the hoverbar
     * @param menuItemProvider the context menu item provider
     */
    public CmsHoverbarContextMenuButton(
        final CmsSitemapHoverbar hoverbar,
        I_CmsContextMenuItemProvider menuItemProvider) {

        super(null, I_CmsButton.CONTEXT_MENU_SMALL);
        // create the menu panel (it's a table because of ie6)
        m_menuPanel = new FlexTable();
        // set a style name for the menu table
        m_menuPanel.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().menuPanel());
        m_button.setSize(I_CmsButton.Size.medium);
        // set the widget
        setMenuWidget(m_menuPanel);
        //    getPopupContent().removeAutoHidePartner(getElement());
        getPopup().addAutoHidePartner(getElement());
        getPopup().setWidth(0);
        getPopup().addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().contextMenu());

        m_entries = new ArrayList<A_CmsSitemapMenuEntry>();
        if (menuItemProvider == null) {
            menuItemProvider = this;
        }
        m_entries.addAll(menuItemProvider.createContextMenu(hoverbar));

        setTitle(Messages.get().key(Messages.GUI_HOVERBAR_TITLE_0));
        setVisible(true);
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                if (!isOpen()) {
                    showMenu(hoverbar);
                } else {
                    closeMenu();
                }
            }
        });
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.I_CmsContextMenuItemProvider#createContextMenu(org.opencms.ade.sitemap.client.hoverbar.CmsSitemapHoverbar)
     */
    public List<A_CmsSitemapMenuEntry> createContextMenu(CmsSitemapHoverbar hoverbar) {

        List<A_CmsSitemapMenuEntry> result = Lists.newArrayList();

        result.add(new CmsGotoMenuEntry(hoverbar));
        result.add(new CmsGotoExplorerMenuEntry(hoverbar));
        result.add(new CmsOpenGalleryMenuEntry(hoverbar));
        result.add(new CmsEditRedirectMenuEntry(hoverbar));
        result.add(new CmsEditModelPageMenuEntry(hoverbar));
        result.add(new CmsDeleteModelPageMenuEntry(hoverbar));
        result.add(new CmsDisableMenuEntry(hoverbar));
        result.add(new CmsEditMenuEntry(hoverbar));
        result.add(new CmsCopyPageMenuEntry(hoverbar));
        result.add(new CmsCopyModelPageMenuEntry(hoverbar));
        result.add(new CmsSetDefaultModelMenuEntry(hoverbar));
        result.add(new CmsCopyAsModelGroupPageMenuEntry(hoverbar));
        result.add(new CmsCreateGalleryMenuEntry(hoverbar));
        result.add(new CmsResourceInfoMenuEntry(hoverbar));
        result.add(new CmsParentSitemapMenuEntry(hoverbar));
        result.add(new CmsGotoSubSitemapMenuEntry(hoverbar));
        result.add(new CmsNewChoiceMenuEntry(hoverbar));
        result.add(new CmsHideMenuEntry(hoverbar));
        result.add(new CmsShowMenuEntry(hoverbar));
        result.add(new CmsAddToNavMenuEntry(hoverbar));
        result.add(new CmsBumpDetailPageMenuEntry(hoverbar));
        result.add(new CmsRefreshMenuEntry(hoverbar));
        result.add(
            new CmsAdvancedSubmenu(
                hoverbar,
                Arrays.asList(
                    new CmsAvailabilityMenuEntry(hoverbar),
                    new CmsLockReportMenuEntry(hoverbar),
                    new CmsSeoMenuEntry(hoverbar),
                    new CmsSubSitemapMenuEntry(hoverbar),
                    new CmsMergeMenuEntry(hoverbar),
                    new CmsRemoveMenuEntry(hoverbar))));
        result.add(new CmsModelPageLockReportMenuEntry(hoverbar));
        result.add(new CmsDeleteMenuEntry(hoverbar));

        return result;
    }

    /**
     * Rests the button state and hides the hoverbar.<p>
     *
     * @param hoverbar the hoverbar
     */
    protected void onMenuClose(CmsSitemapHoverbar hoverbar) {

        m_button.setDown(false);
        if (!hoverbar.isHovered()) {
            hoverbar.hide();
        } else {
            hoverbar.setLocked(false);
        }
    }

    /**
     * Sets the context menu visible.<p>
     *
     * @param hoverbar the hoverbar instance
     */
    protected void setMenuVisible(final CmsSitemapHoverbar hoverbar) {

        updateVisibility();
        CmsContextMenu menu = new CmsContextMenu(new ArrayList<I_CmsContextMenuEntry>(m_entries), false, getPopup());
        m_menuPanel.setWidget(0, 0, menu);
        // add the close handler for the menu
        getPopup().addCloseHandler(new CmsContextMenuCloseHandler(menu));
        getPopup().addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> closeEvent) {

                onMenuClose(hoverbar);
                closeMenu();
            }
        });
        openMenu();
    }

    /**
     * Shows the context menu.<p>
     *
     * @param hoverbar the hoverbar instance
     */
    protected void showMenu(final CmsSitemapHoverbar hoverbar) {

        // lock the hoverbar visibility to avoid hide on mouse out
        hoverbar.setLocked(true);
        hoverbar.loadEntry(new AsyncCallback<CmsClientSitemapEntry>() {

            public void onFailure(Throwable caught) {

                // TODO Auto-generated method stub

            }

            public void onSuccess(CmsClientSitemapEntry result) {

                setMenuVisible(hoverbar);
            }
        });
    }

    /**
     * Updates the entry visibility.<p>
     */
    private void updateVisibility() {

        for (A_CmsSitemapMenuEntry entry : m_entries) {
            updateVisibility(entry);
        }
    }

    /**
     * Updates the visibility for an entry and its sub-entries.<p>
     *
     * @param entry the entry to update
     */
    private void updateVisibility(A_CmsSitemapMenuEntry entry) {

        if (entry.getSubMenu() != null) {
            for (I_CmsContextMenuEntry subItem : entry.getSubMenu()) {
                updateVisibility((A_CmsSitemapMenuEntry)subItem);
            }
        }
        entry.onShow();
    }
}

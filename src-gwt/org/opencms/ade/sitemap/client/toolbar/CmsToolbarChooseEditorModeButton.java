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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.shared.CmsSitemapData.EditorMode;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsMenuButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenu;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuCloseHandler;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * The sitemap toolbar change sitemap editor mode button.<p>
 *
 * @since 8.0.0
 */
public class CmsToolbarChooseEditorModeButton extends CmsMenuButton {

    /**
     * Context menu entry used to select a sitemap editor mode.<p>
     */
    class EditorModeEntry extends A_CmsSitemapModeEntry {

        /** The sitemap editor mode. */
        private EditorMode m_mode;

        /**
         * Creates a new entry.<p>
         *
         * @param message the context menu item text
         * @param mode the editor mode
         */
        public EditorModeEntry(String message, EditorMode mode) {

            super(message);
            m_mode = mode;
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
         */
        public void execute() {

            CmsSitemapView.getInstance().onBeforeSetEditorMode(m_mode);
        }

        /**
         * @see org.opencms.ade.sitemap.client.toolbar.A_CmsSitemapModeEntry#getIconClass()
         */
        @Override
        public String getIconClass() {

            I_CmsInputCss inputCss = I_CmsInputLayoutBundle.INSTANCE.inputCss();
            EditorMode currentMode = CmsSitemapView.getInstance().getEditorMode();
            return (currentMode == m_mode) ? inputCss.checkBoxImageChecked() : "";
        }

    }

    /** True if we can edit model pages. */
    private boolean m_canEditModelPages;

    /** The context menu entries. */
    private List<I_CmsContextMenuEntry> m_entries;

    /** The main content widget. */
    private FlexTable m_menuPanel;

    /**
     * Constructor.<p>
     *
     * @param canEditModelPages true if editing model pages should be enabled
     */
    public CmsToolbarChooseEditorModeButton(boolean canEditModelPages) {

        super(null, I_CmsButton.ButtonData.SITEMAP_BUTTON.getIconClass());
        m_canEditModelPages = canEditModelPages;
        setTitle(Messages.get().key(Messages.GUI_SELECT_VIEW_0));
        m_menuPanel = new FlexTable();
        // set a style name for the menu table
        m_menuPanel.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.contextmenuCss().menuPanel());
        // set the widget
        setMenuWidget(m_menuPanel);
        getPopup().addAutoHidePartner(getElement());
        getPopup().setWidth(0);
        CmsContextMenu menu = createContextMenu();
        m_menuPanel.setWidget(0, 0, menu);
        // add the close handler for the menu
        getPopup().addCloseHandler(new CmsContextMenuCloseHandler(menu));
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            @SuppressWarnings("synthetic-access")
            public void onClick(ClickEvent event) {

                if (!isOpen()) {
                    m_menuPanel.setWidget(0, 0, createContextMenu()); // we have to create the menu every time because the active mode may change
                    openMenu();
                } else {
                    closeMenu();
                }
            }
        });
    }

    /**
     * Creates the menu widget for this button.<p>
     *
     * @return the menu widget
     */
    public CmsContextMenu createContextMenu() {

        m_entries = new ArrayList<I_CmsContextMenuEntry>();
        m_entries.add(
            new EditorModeEntry(
                Messages.get().key(Messages.GUI_ONLY_NAVIGATION_BUTTON_TITLE_0),
                EditorMode.navigation));
        m_entries.add(
            new EditorModeEntry(Messages.get().key(Messages.GUI_NON_NAVIGATION_BUTTON_TITLE_0), EditorMode.vfs));
        m_entries.add(
            new EditorModeEntry(Messages.get().key(Messages.GUI_ONLY_GALLERIES_BUTTON_TITLE_0), EditorMode.galleries));
        if (CmsCoreProvider.get().getUserInfo().isCategoryManager()) {
            m_entries.add(
                new EditorModeEntry(
                    Messages.get().key(Messages.GUI_CONTEXTMENU_CATEGORY_MODE_0),
                    EditorMode.categories));
        }
        if (m_canEditModelPages) {
            m_entries.add(new EditorModeEntry(Messages.get().key(Messages.GUI_MODEL_PAGES_0), EditorMode.modelpages));
        }
        if (CmsSitemapView.getInstance().getController().isLocaleComparisonEnabled()) {
            m_entries.add(
                new EditorModeEntry(Messages.get().key(Messages.GUI_LOCALECOMPARE_MODE_0), EditorMode.compareLocales));
        }

        CmsContextMenu menu = new CmsContextMenu(m_entries, false, getPopup());
        return menu;
    }
}

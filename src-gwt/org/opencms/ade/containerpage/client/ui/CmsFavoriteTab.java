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

import org.opencms.ade.containerpage.client.Messages;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Content of the tool-bar menu favorite tab.<p>
 *
 * @since 8.0.0
 */
public class CmsFavoriteTab extends A_CmsClipboardTab {

    /** The ui-binder interface for this widget. */
    interface I_CmsFavoriteTabUiBinder extends UiBinder<Widget, CmsFavoriteTab> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder for this widget. */
    private static I_CmsFavoriteTabUiBinder uiBinder = GWT.create(I_CmsFavoriteTabUiBinder.class);

    /** Button panel shown while editing the favorites. */
    @UiField
    protected FlowPanel m_buttonEditingPanel;

    /** Button panel shown while using the favorites. */
    @UiField
    protected FlowPanel m_buttonUsePanel;

    /** The cancel edit button. */
    @UiField
    protected CmsPushButton m_cancelButton;

    /** The clip-board menu. */
    protected CmsToolbarClipboardMenu m_clipboard;

    /** The edit button. */
    @UiField
    protected CmsPushButton m_editButton;

    /** The list panel holding the favorite elements. */
    @UiField(provided = true)
    protected CmsList<CmsListItem> m_listPanel = new CmsList<CmsListItem>();

    /** The save favorites button. */
    @UiField
    protected CmsPushButton m_saveButton;

    /** The scroll panel. */
    @UiField
    protected CmsScrollPanel m_scrollPanel;

    /**
     * Constructor.<p>
     *
     * @param clipboard the clip-board menu
     */
    public CmsFavoriteTab(CmsToolbarClipboardMenu clipboard) {

        initWidget(uiBinder.createAndBindUi(this));
        m_clipboard = clipboard;
        m_buttonEditingPanel.setVisible(false);
        m_editButton.setText(Messages.get().key(Messages.GUI_BUTTON_EDITFAVORITES_TEXT_0));
        m_editButton.setTitle(Messages.get().key(Messages.GUI_BUTTON_EDITFAVORITES_TEXT_0));
        m_editButton.disable(Messages.get().key(Messages.GUI_TAB_FAVORITES_NO_ELEMENTS_0));
        m_saveButton.setText(Messages.get().key(Messages.GUI_BUTTON_SAVE_TEXT_0));
        m_saveButton.setTitle(Messages.get().key(Messages.GUI_BUTTON_SAVE_TEXT_0));
        m_cancelButton.setText(Messages.get().key(Messages.GUI_BUTTON_CANCEL_TEXT_0));
        m_cancelButton.setTitle(Messages.get().key(Messages.GUI_BUTTON_CANCEL_TEXT_0));
        m_listPanel.setDropEnabled(true);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsClipboardTab#addListItem(org.opencms.gwt.client.ui.CmsListItem)
     */
    @Override
    public void addListItem(CmsListItem item) {

        super.addListItem(item);
        if (m_listPanel.getWidgetCount() > 0) {
            m_editButton.enable();
        }
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsClipboardTab#clearList()
     */
    @Override
    public void clearList() {

        super.clearList();
        m_editButton.disable(Messages.get().key(Messages.GUI_TAB_FAVORITES_NO_ELEMENTS_0));
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsClipboardTab#getList()
     */
    @Override
    public CmsList<CmsListItem> getList() {

        return m_listPanel;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.A_CmsClipboardTab#getScrollPanel()
     */
    @Override
    public CmsScrollPanel getScrollPanel() {

        return m_scrollPanel;
    }

    /**
     * Returns the favorite list item iterator.<p>
     *
     * @return the iterator
     */
    public Iterator<Widget> iterator() {

        return m_listPanel.iterator();
    }

    /**
     * Saves the favorites.<p>
     */
    public void saveFavorites() {

        m_clipboard.saveFavorites();
        if (m_listPanel.getWidgetCount() < 1) {
            m_editButton.disable(Messages.get().key(Messages.GUI_TAB_FAVORITES_NO_ELEMENTS_0));
        }
        m_buttonEditingPanel.setVisible(false);
        m_buttonUsePanel.setVisible(true);
    }

    /**
     * Cancels the editing.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_cancelButton")
    void cancelAction(ClickEvent event) {

        m_clipboard.reloadFavorites();
        m_buttonEditingPanel.setVisible(false);
        m_buttonUsePanel.setVisible(true);
    }

    /**
     * Starts the editing.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_editButton")
    void editAction(ClickEvent event) {

        m_clipboard.enableFavoritesEdit();
        m_buttonUsePanel.setVisible(false);
        m_buttonEditingPanel.setVisible(true);
    }

    /**
     * Saves the favorite list.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_saveButton")
    void saveAction(ClickEvent event) {

        saveFavorites();
    }
}

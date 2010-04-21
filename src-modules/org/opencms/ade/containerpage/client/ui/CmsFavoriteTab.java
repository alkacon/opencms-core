/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsFavoriteTab.java,v $
 * Date   : $Date: 2010/04/21 15:05:19 $
 * Version: $Revision: 1.2 $
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
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.draganddrop.CmsContainerDragHandler;
import org.opencms.ade.containerpage.client.draganddrop.CmsDragMenuElement;
import org.opencms.ade.containerpage.client.draganddrop.CmsDragTargetMenu;
import org.opencms.ade.containerpage.client.draganddrop.CmsMenuDragHandler;
import org.opencms.gwt.client.ui.CmsButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Content of the tool-bar menu favorite tab.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsFavoriteTab extends Composite {

    /** The ui-binder interface for this widget. */
    interface I_CmsFavoriteTabUiBinder extends UiBinder<Widget, CmsFavoriteTab> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder for this widget. */
    private static I_CmsFavoriteTabUiBinder uiBinder = GWT.create(I_CmsFavoriteTabUiBinder.class);

    /** Button panel shown while editing the favorites. */
    @UiField
    /*DEFAULT*/FlowPanel m_buttonEditingPanel;

    /** Button panel shown while using the favorites. */
    @UiField
    /*DEFAULT*/FlowPanel m_buttonUsePanel;

    /** The cancel edit button. */
    @UiField
    /*DEFAULT*/CmsButton m_cancelButton;

    /** The edit button. */
    @UiField
    /*DEFAULT*/CmsButton m_editButton;

    /** The list panel holding the favorite elements. */
    @UiField
    /*DEFAULT*/CmsDragTargetMenu m_listPanel;

    /** The save favorites button. */
    @UiField
    /*DEFAULT*/CmsButton m_saveButton;

    /**
     * Constructor.<p>
     */
    public CmsFavoriteTab() {

        initWidget(uiBinder.createAndBindUi(this));
        m_buttonEditingPanel.setVisible(false);
        m_editButton.setUpFace(Messages.get().key(Messages.EDIT_FAVORITES_BUTTON_TEXT_0), null);
        m_editButton.setTitle(Messages.get().key(Messages.EDIT_FAVORITES_BUTTON_TEXT_0));
        m_saveButton.setUpFace(Messages.get().key(Messages.SAVE_BUTTON_TEXT_0), null);
        m_saveButton.setTitle(Messages.get().key(Messages.SAVE_BUTTON_TEXT_0));
        m_cancelButton.setUpFace(Messages.get().key(Messages.CANCEL_BUTTON_TEXT_0), null);
        m_cancelButton.setTitle(Messages.get().key(Messages.CANCEL_BUTTON_TEXT_0));
    }

    /**
     * Adds an item to the favorite list.<p>
     * 
     * @param item the item to add
     */
    public void addListItem(CmsDragMenuElement item) {

        m_listPanel.add(item);
    }

    /**
     * Clears the favorite list.<p>
     */
    public void clearList() {

        m_listPanel.clear();
    }

    /**
     * Cancels the editing.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_cancelButton")
    void cancelAction(ClickEvent event) {

        CmsContainerpageDataProvider.get().loadFavorites();
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

        Iterator<Widget> it = m_listPanel.iterator();
        while (it.hasNext()) {
            CmsDragMenuElement element = (CmsDragMenuElement)it.next();
            element.showDeleteButton();
            element.removeAllMouseHandlers();
            element.setDragParent(m_listPanel);
            CmsMenuDragHandler.get().registerMouseHandler(element);
        }
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

        List<String> clientIds = new ArrayList<String>();
        Iterator<Widget> it = m_listPanel.iterator();
        while (it.hasNext()) {
            CmsDragMenuElement element = (CmsDragMenuElement)it.next();
            element.hideDeleteButton();
            clientIds.add(element.getClientId());
            element.removeAllMouseHandlers();
            CmsContainerDragHandler.get().registerMouseHandler(element);
        }
        CmsContainerpageDataProvider.get().saveFavoriteList(clientIds);
        m_buttonEditingPanel.setVisible(false);
        m_buttonUsePanel.setVisible(true);
    }
}

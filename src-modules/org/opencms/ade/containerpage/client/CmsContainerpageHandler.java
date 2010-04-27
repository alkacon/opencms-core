/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/Attic/CmsContainerpageHandler.java,v $
 * Date   : $Date: 2010/04/27 13:56:00 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement;
import org.opencms.ade.containerpage.client.draganddrop.CmsDragMenuElement;
import org.opencms.ade.containerpage.client.ui.CmsContentEditorDialog;
import org.opencms.ade.containerpage.client.ui.CmsLeavePageDialog;
import org.opencms.ade.containerpage.client.ui.I_CmsToolbarButton;
import org.opencms.ade.containerpage.client.util.CmsContainerpageProvider;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.publish.client.CmsPublishDialog;
import org.opencms.gwt.client.draganddrop.I_CmsDragElement;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The container-page handler.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsContainerpageHandler {

    /** The container-page controller. */
    protected CmsContainerpageController m_controller;

    /** The container-page editor. */
    protected CmsContainerpageEditor m_editor;

    /** The currently active tool-bar button. */
    private I_CmsToolbarButton m_activeButton;

    /**
     * Constructor.<p>
     * 
     * @param controller the container-page controller
     * @param editor the container-page editor
     */
    public CmsContainerpageHandler(CmsContainerpageController controller, CmsContainerpageEditor editor) {

        m_controller = controller;

        m_editor = editor;
    }

    /**
     * Activates the selection button.<p>
     */
    public void activateSelection() {

        //TODO: implement
    }

    /**
     * Adds the given list item widget to the favorite list widget.<p>
     * 
     * @param listItem the list item
     */
    public void addToFavorites(CmsDragMenuElement listItem) {

        m_editor.getClipboard().addToFavorites(listItem);
    }

    /**
     * Adds the given list item widget to the recent list widget.<p>
     * 
     * @param listItem the list item
     */
    public void addToRecent(CmsDragMenuElement listItem) {

        m_editor.getClipboard().addToRecent(listItem);
    }

    /**
     * De-activates the current button.<p> 
     */
    public void deactivateCurrentButton() {

        if (m_activeButton != null) {
            m_activeButton.setActive(false);
            m_activeButton = null;
        }

    }

    /**
     * Enables the drag handler on the given element.<p>
     * 
     * @param element the element
     */
    public void enableDragHandler(I_CmsDragElement element) {

        m_controller.getContainerpageUtil().enableDragHandler(element);
    }

    /**
     * Leaves the current page and opens the sitemap.<p>§
     */
    public void gototSitemap() {

        if (m_controller.hasPageChanged()) {
            CmsLeavePageDialog dialog = new CmsLeavePageDialog(
                CmsContainerpageProvider.get().getSitemapUri(),
                m_controller,
                this);
            dialog.center();
        } else {
            m_controller.leaveUnsaved(CmsContainerpageProvider.get().getSitemapUri());
        }
    }

    /**
     * Loads the favorite list from the server and adds it's items to the clip-board.<p>
     */
    public void loadFavorites() {

        m_controller.loadFavorites(new I_CmsSimpleCallback<List<CmsContainerElement>>() {

            /**
             * Generating the list item widgets and inserting them into the favorite list.<p> 
             * 
             * @param arg the element data
             */
            public void execute(List<CmsContainerElement> arg) {

                m_editor.getClipboard().clearFavorites();
                Iterator<CmsContainerElement> it = arg.iterator();
                while (it.hasNext()) {
                    addToFavorites(m_controller.getContainerpageUtil().createListItem(it.next(), null));
                }
            }

            /**
             * @see org.opencms.gwt.client.util.I_CmsSimpleCallback#onError(java.lang.String)
             */
            public void onError(String message) {

                // TODO: Auto-generated method stub

            }
        });
    }

    /**
     * Loads the recent list from the server and adds it's items to the clip-board.<p>
     */
    public void loadRecent() {

        m_controller.loadRecent(new I_CmsSimpleCallback<List<CmsContainerElement>>() {

            /**
             * Generating the list item widgets and inserting them into the recent list.<p> 
             * 
             * @param arg the element data
             */
            public void execute(List<CmsContainerElement> arg) {

                m_editor.getClipboard().clearRecent();
                Iterator<CmsContainerElement> it = arg.iterator();
                while (it.hasNext()) {
                    addToRecent(m_controller.getContainerpageUtil().createListItem(it.next(), null));
                }
            }

            /**
             * @see org.opencms.gwt.client.util.I_CmsSimpleCallback#onError(java.lang.String)
             */
            public void onError(String message) {

                // TODO: Auto-generated method stub

            }
        });
    }

    /**
     * Opens the edit dialog for the specified element.<p>
     * 
     * @param element the element to edit
     */
    public void openEditorForElement(CmsDragContainerElement element) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(element.getNoEditReason())) {
            CmsContentEditorDialog.get().openEditDialog(element.getClientId(), element.getSitePath());
        } else {
            CmsDebugLog.getInstance().printLine(element.getNoEditReason());
        }
    }

    /**
     * Removes the given container-page element.<p>
     * 
     * @param element the element
     */
    public void removeElement(CmsDragContainerElement element) {

        m_controller.removeElement(element);
    }

    /**
     * Resets the container-page to it's previous state.<p>
     */
    public void resetPage() {

        CmsConfirmDialog dialog = new CmsConfirmDialog(org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_DIALOG_RESET_TITLE_0), org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_DIALOG_RESET_TEXT_0));
        dialog.setHandler(new I_CmsConfirmDialogHandler() {

            /**
             * @see org.opencms.gwt.client.ui.I_CmsCloseDialogHandler#onClose()
             */
            public void onClose() {

                deactivateCurrentButton();
                activateSelection();
            }

            /**
             * @see org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler#onOk()
             */
            public void onOk() {

                m_controller.resetPage();
            }
        });
        dialog.center();
    }

    /**
     * Saves the favorite list.<p>
     * 
     * @param clientIds the client id's of the list's elements
     * 
     * @see org.opencms.ade.containerpage.client.CmsContainerpageController#saveFavoriteList(java.util.List)
     */
    public void saveFavoriteList(List<String> clientIds) {

        m_controller.saveFavoriteList(clientIds);
    }

    /**
     * Saves the current state of the container-page.<p>
     */
    public void savePage() {

        m_controller.saveContainerpage();

        deactivateCurrentButton();
        activateSelection();
    }

    /**
     * Sets the currently active tool-bar button.<p>
     * 
     * @param button the button
     */
    public void setActiveButton(I_CmsToolbarButton button) {

        m_activeButton = button;
    }

    /**
     * Shows the publish dialog.<p>
     */
    public void showPublishDialog() {

        CmsPublishDialog.showPublishDialog(new CloseHandler<PopupPanel>() {

            /**
             * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
             */
            public void onClose(CloseEvent<PopupPanel> event) {

                deactivateCurrentButton();
                activateSelection();

            }
        });
    }

}

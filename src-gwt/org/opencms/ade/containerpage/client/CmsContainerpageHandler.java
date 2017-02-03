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

package org.opencms.ade.containerpage.client;

import org.opencms.ade.containerpage.client.CmsContainerpageController.ElementRemoveMode;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsElementSettingsDialog;
import org.opencms.ade.containerpage.client.ui.CmsGroupContainerElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsSmallElementsHandler;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsElementViewInfo;
import org.opencms.ade.containerpage.shared.CmsLocaleLinkBean;
import org.opencms.ade.publish.client.CmsPublishDialog;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.dnd.I_CmsDNDController;
import org.opencms.gwt.client.ui.A_CmsToolbarHandler;
import org.opencms.gwt.client.ui.A_CmsToolbarMenu;
import org.opencms.gwt.client.ui.CmsAcceptDeclineCancelDialog;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsLockReportDialog;
import org.opencms.gwt.client.ui.CmsModelSelectDialog;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.ui.I_CmsAcceptDeclineCancelHandler;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.ui.I_CmsModelSelectHandler;
import org.opencms.gwt.client.ui.I_CmsToolbarButton;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.ui.contextmenu.A_CmsContextMenuItem;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuEntry;
import org.opencms.gwt.client.ui.contextmenu.CmsPreview;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.resourceinfo.CmsResourceInfoDialog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsClientVariantInfo;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsLockInfo;
import org.opencms.gwt.shared.CmsModelResourceInfo;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * The container-page handler.<p>
 *
 * @since 8.0.0
 */
public class CmsContainerpageHandler extends A_CmsToolbarHandler {

    /**
     * Action which is executed when the user selects a client variant.<p>
     */
    class ClientVariantSelectAction implements Runnable {

        /** The template context name. */
        private String m_context;

        /** The variant bean. */
        private CmsClientVariantInfo m_variant;

        /**
         * Creates a new instance.<p>
         *
         * @param context the context name
         * @param variant the variant information
         */
        public ClientVariantSelectAction(String context, CmsClientVariantInfo variant) {

            m_context = context;
            m_variant = variant;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {

            deactivateCurrentButton();
            m_clientVariantDisplay.show(m_context, m_variant);
        }
    }

    /**
     * Action which does nothing.<p>
     */
    public static final Runnable DO_NOTHING = new Runnable() {

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {

            // do nothing

        }

    };

    /** The container-page controller. */
    protected CmsContainerpageController m_controller;

    /** The container-page editor. */
    protected CmsContainerpageEditor m_editor;

    /** The widget used for displaying the client variants. */
    CmsClientVariantDisplay m_clientVariantDisplay = new CmsClientVariantDisplay(this);

    /** The currently active tool-bar button. */
    private I_CmsToolbarButton m_activeButton;

    /** The edit button visibility style variable. */
    private CmsStyleVariable m_editButtonVibility;

    /** Overlay to prevent user actions while shown. */
    private SimplePanel m_overlay;

    /**
     * Constructor.<p>
     *
     * @param controller the container-page controller
     * @param editor the container-page editor
     */
    public CmsContainerpageHandler(CmsContainerpageController controller, CmsContainerpageEditor editor) {

        m_controller = controller;
        m_editor = editor;
        m_editButtonVibility = new CmsStyleVariable(RootPanel.get());
    }

    /**
     * Activates the selection button.<p>
     */
    public void activateSelection() {

        m_editor.getSelection().setActive(true);
        setEditButtonsVisible(true);
        reInitInlineEditing();
    }

    /**
     * Adds the given list item widget to the favorite list widget.<p>
     *
     * @param listItem the list item
     */
    public void addToFavorites(CmsListItem listItem) {

        m_editor.getClipboard().addToFavorites(listItem);
    }

    /**
     * Adds the element with the given id to the favorite list.<p>
     *
     * @param clientId the client id
     */
    public void addToFavorites(String clientId) {

        m_controller.addToFavoriteList(clientId);
    }

    /**
     * Adds the given list item widget to the recent list widget.<p>
     *
     * @param listItem the list item
     */
    public void addToRecent(CmsListItem listItem) {

        m_editor.getClipboard().addToRecent(listItem);
    }

    /**
     * Adds the element with the given id to the favorite list.<p>
     *
     * @param clientId the client id
     */
    public void addToRecent(String clientId) {

        m_controller.addToRecentList(clientId, null);
    }

    /**
     * Switches the template context.<p>
     *
     * @param cookieName the cookie name
     * @param value the new template context
     */
    @SuppressWarnings("deprecation")
    public void changeTemplateContextManually(final String cookieName, final String value) {

        if (value != null) {
            Cookies.setCookie(cookieName, value, new Date(300, 0, 1), null, "/", false);
        } else {
            Cookies.removeCookie(cookieName, "/");
        }
        Window.Location.reload();
    }

    /**
     * Checks whether GWT widgets are available for all fields of a content.<p>
     *
     * @param structureId the structure id of the content
     *
     * @param asyncCallback the callback for the result
     */
    public void checkNewWidgetsAvailable(CmsUUID structureId, AsyncCallback<Boolean> asyncCallback) {

        m_controller.checkNewWidgetsAvailable(structureId, asyncCallback);
    }

    /**
     * Creates a context menu entry.<p>
     *
     * @param structureId structure id of the resource
     * @param name the label
     * @param checked true if checkbox should be displayed
     * @param action the action to execute
     *
     * @return the menu entry
     */
    public CmsContextMenuEntry createSimpleContextMenuEntry(
        CmsUUID structureId,
        String name,
        boolean checked,
        final Runnable action) {

        CmsContextMenuEntry entry = createRawMenuEntry(structureId, action);
        decorateMenuEntry(entry, name, checked);
        return entry;
    }

    /**
     * Creates a select box used to switch between views from the gallery dialog.<p>
     *
     * @return the created select box
     */
    public CmsSelectBox createViewSelectorForGalleryDialog() {

        List<CmsElementViewInfo> elementViews = m_controller.getData().getElementViews();
        if (elementViews.size() <= 1) {
            return null;
        }
        CmsUUID currentView = m_controller.getElementView().getElementViewId();
        Map<String, String> options = Maps.newHashMap();
        for (CmsElementViewInfo view : elementViews) {
            options.put("" + view.getElementViewId(), view.getTitle());
        }
        CmsSelectBox result = new CmsSelectBox(options);
        result.setFormValueAsString(currentView.toString());
        result.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                m_editor.getAdd().setActive(false);
                CmsElementViewInfo newValue = m_controller.getView(event.getValue());
                setElementView(newValue, new Runnable() {

                    public void run() {

                        m_editor.getAdd().setActive(true);
                    }
                });
            }
        });
        result.getElement().getStyle().setWidth(250, Unit.PX);

        return result;
    }

    /**
     * De-activates the current button.<p>
     */
    public void deactivateCurrentButton() {

        if (m_activeButton != null) {
            if (m_activeButton == m_editor.getSelection()) {
                m_controller.disableInlineEditing(null);
            }
            m_activeButton.setActive(false);
            m_activeButton = null;
        }

    }

    /**
     * De-activates menu button.<p>
     */
    public void deactivateMenuButton() {

        if ((m_activeButton != null) && (m_activeButton instanceof A_CmsToolbarMenu<?>)) {
            ((A_CmsToolbarMenu<?>)m_activeButton).setActive(false);
        }
    }

    /**
     * Deactivates the selection.<p>
     */
    public void deactivateSelection() {

        m_editor.getSelection().setActive(false);
    }

    /**
     * Deactivates all toolbar buttons.<p>
     */
    public void disableToolbarButtons() {

        m_editor.disableToolbarButtons();
    }

    /**
     * Starts the property editor for the given container element.<p>
     *
     * @param elementWidget the container element widget for which the properties should be edited
     */
    public void editElementSettings(final CmsContainerPageElementPanel elementWidget) {

        m_controller.getElementSettingsConfig(
            elementWidget.getId(),
            elementWidget.getParentTarget().getContainerId(),
            new I_CmsSimpleCallback<CmsContainerElementData>() {

                public void execute(final CmsContainerElementData elementBean) {

                    if (!elementBean.getClientId().equals(elementWidget.getId())) {
                        // the client id may have changed, update the element widget
                        elementWidget.setId(elementBean.getClientId());
                    }
                    CmsElementSettingsDialog dialog = new CmsElementSettingsDialog(
                        m_controller,
                        elementWidget,
                        elementBean);
                    dialog.center();
                }
            });
    }

    /**
     * Enables the favorites editing drag and drop controller.<p>
     *
     * @param enable if <code>true</code> favorites editing will enabled, otherwise disabled
     * @param dndController the favorites editing drag and drop controller
     */
    public void enableFavoriteEditing(boolean enable, I_CmsDNDController dndController) {

        m_controller.enableFavoriteEditing(enable, dndController);
    }

    /**
     * Enables the toolbar buttons.<p>
     */
    public void enableToolbarButtons() {

        m_editor.enableToolbarButtons(m_controller.hasPageChanged(), m_controller.getData().getNoEditReason());
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#ensureLockOnResource(org.opencms.util.CmsUUID)
     */
    public boolean ensureLockOnResource(CmsUUID structureId) {

        return m_controller.lockContainerpage();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarHandler#getActiveButton()
     */
    public I_CmsToolbarButton getActiveButton() {

        return m_activeButton;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getContextMenuCommands()
     */
    public Map<String, I_CmsContextMenuCommand> getContextMenuCommands() {

        return m_editor.getContextMenuCommands();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getContextType()
     */
    public String getContextType() {

        return CmsGwtConstants.CONTEXT_TYPE_CONTAINERPAGE_TOOLBAR;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getEditorHandler()
     */
    public I_CmsContentEditorHandler getEditorHandler() {

        return m_controller.getContentEditorHandler();
    }

    /**
     * Leaves the current page and opens the site-map.<p>
     */
    public void gotoSitemap() {

        final String sitemapUri = CmsCoreProvider.get().link(m_controller.getData().getSitemapUri());
        if (sitemapUri.equals("")) {
            return; // normally, we shouldn't even get to this point because the sitemap button should be disabled
        }
        Command leaveCommand = new Command() {

            public void execute() {

                Window.Location.replace(
                    sitemapUri
                        + "?"
                        + CmsCoreData.PARAM_PATH
                        + "="
                        + CmsContainerpageController.getCurrentUri()
                        + "&"
                        + CmsCoreData.PARAM_RETURNCODE
                        + "="
                        + m_controller.getReturnCode());
            }

        };
        leavePage(leaveCommand);
    }

    /**
     * Returns if the selection button is active.<p>
     *
     * @return <code>true</code> if the selection button is active
     */
    public boolean hasActiveSelection() {

        return m_editor.getSelection().isActive();
    }

    /**
     * Hides any open menu.<p>
     */
    public void hideMenu() {

        if ((m_activeButton != null) && (m_activeButton instanceof A_CmsToolbarMenu<?>)) {
            ((A_CmsToolbarMenu<?>)m_activeButton).hideMenu();
        }
    }

    /**
     * Removes page overlay if present.<p>
     */
    public void hidePageOverlay() {

        if (m_overlay != null) {
            m_overlay.removeFromParent();
        }
    }

    /**
     * Inserts the context menu.<p>
     *
     * @param menuBeans the menu beans from the server
     * @param structureId the structure id of the resource for which the context menu entries should be generated
     */
    public void insertContextMenu(List<CmsContextMenuEntryBean> menuBeans, CmsUUID structureId) {

        List<I_CmsContextMenuEntry> menuEntries = transformEntries(menuBeans, structureId);
        m_editor.getContext().showMenu(menuEntries);
    }

    /**
     * Call to leave the page. Will open save/leave/cancel dialog if page contains any changes.<p>
     *
     * @param leavingCommand the page leaving command
     */
    public void leavePage(final Command leavingCommand) {

        if (!m_controller.hasPageChanged() || m_controller.isEditingDisabled()) {
            leavingCommand.execute();
            return;
        }
        CmsAcceptDeclineCancelDialog leavingDialog = getLeaveDialog();
        leavingDialog.setHandler(new I_CmsAcceptDeclineCancelHandler() {

            /**
             * @see org.opencms.gwt.client.ui.I_CmsAcceptDeclineCancelHandler#onAccept()
             */
            public void onAccept() {

                m_controller.saveAndLeave(leavingCommand);
            }

            /**
             * @see org.opencms.gwt.client.ui.I_CmsCloseDialogHandler#onClose()
             */
            public void onClose() {

                deactivateCurrentButton();
                activateSelection();
            }

            /**
             * @see org.opencms.gwt.client.ui.I_CmsAcceptDeclineCancelHandler#onDecline()
             */
            public void onDecline() {

                m_controller.setPageChanged(false, true);
                leavingCommand.execute();
            }
        });
        leavingDialog.center();
    }

    /**
     * Call to leave the page. Will open save/leave/cancel dialog if page contains any changes.<p>
     *
     * @param target the target
     */
    public void leavePage(final String target) {

        if (!m_controller.hasPageChanged() || m_controller.isEditingDisabled()) {
            m_controller.leaveUnsaved(target);
            return;
        }
        CmsAcceptDeclineCancelDialog leavingDialog = getLeaveDialog();
        leavingDialog.setHandler(new I_CmsAcceptDeclineCancelHandler() {

            /**
             * @see org.opencms.gwt.client.ui.I_CmsAcceptDeclineCancelHandler#onAccept()
             */
            public void onAccept() {

                m_controller.saveAndLeave(target);
            }

            /**
             * @see org.opencms.gwt.client.ui.I_CmsCloseDialogHandler#onClose()
             */
            public void onClose() {

                deactivateCurrentButton();
                activateSelection();
            }

            /**
             * @see org.opencms.gwt.client.ui.I_CmsAcceptDeclineCancelHandler#onDecline()
             */
            public void onDecline() {

                m_controller.leaveUnsaved(target);
            }
        });
        leavingDialog.center();
    }

    /**
     * Loads the context menu entries for a given URI.<p>
     *
     * @param structureId the structure id of the resource for which the context menu should be loaded
     * @param context the ade context (sitemap or containerpage)
     */
    public void loadContextMenu(CmsUUID structureId, final AdeContext context) {

        m_controller.loadContextMenu(structureId, context);
    }

    /**
     * Loads the favorite list from the server and adds it's items to the clip-board.<p>
     */
    public void loadFavorites() {

        m_controller.loadFavorites(new I_CmsSimpleCallback<List<CmsContainerElementData>>() {

            /**
             * Generating the list item widgets and inserting them into the favorite list.<p>
             *
             * @param arg the element data
             */
            public void execute(List<CmsContainerElementData> arg) {

                m_editor.getClipboard().clearFavorites();
                Iterator<CmsContainerElementData> it = arg.iterator();
                while (it.hasNext()) {
                    addToFavorites(m_controller.getContainerpageUtil().createListItem(it.next()));
                }
                m_editor.getClipboard().updateSize();
            }
        });
    }

    /**
     * Loads the recent list from the server and adds it's items to the clip-board.<p>
     */
    public void loadRecent() {

        m_controller.loadRecent(new I_CmsSimpleCallback<List<CmsContainerElementData>>() {

            /**
             * Generating the list item widgets and inserting them into the recent list.<p>
             *
             * @param arg the element data
             */
            public void execute(List<CmsContainerElementData> arg) {

                m_editor.getClipboard().clearRecent();
                Iterator<CmsContainerElementData> it = arg.iterator();
                while (it.hasNext()) {
                    addToRecent(m_controller.getContainerpageUtil().createListItem(it.next()));
                }
                m_editor.getClipboard().updateSize();
            }
        });
    }

    /**
     * Should be called when locking the container page failed.<p>
     *
     * @param errorMessage the locking information
     */
    public void onLockFail(String errorMessage) {

        m_editor.disableEditing(errorMessage);
        CmsAlertDialog alert = new CmsAlertDialog(
            Messages.get().key(Messages.ERR_LOCK_TITLE_RESOURCE_LOCKED_0),
            errorMessage);
        alert.center();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#onSiteOrProjectChange(java.lang.String, java.lang.String)
     */
    public void onSiteOrProjectChange(String sitePath, String serverLink) {

        leavePage(serverLink);
    }

    /**
     * Opens the edit dialog for the specified element.<p>
     *
     * @param element the element to edit
     * @param inline <code>true</code> to open the inline editor for the given element if available
     */
    public void openEditorForElement(final CmsContainerPageElementPanel element, boolean inline) {

        if (element.isNew()) {
            //openEditorForElement will be called again asynchronously when the RPC for creating the element has finished
            m_controller.createAndEditNewElement(element, inline);
            return;
        }

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(element.getNoEditReason())) {
            CmsNotification.get().send(
                CmsNotification.Type.WARNING,
                "should be deactivated: " + element.getNoEditReason());
            return;
        }

        if (CmsDomUtil.hasClass(CmsContainerElement.CLASS_GROUP_CONTAINER_ELEMENT_MARKER, element.getElement())) {
            openGroupEditor((CmsGroupContainerElementPanel)element);
        } else {
            m_controller.setContentEditing(true);
            m_controller.disableInlineEditing(element);
            m_controller.getContentEditorHandler().openDialog(element, inline);
            element.removeHighlighting();
        }
    }

    /**
     * Opens the elements info dialog.<p>
     */
    public void openElementsInfo() {

        CmsResourceInfoDialog.load(
            CmsCoreProvider.get().getStructureId(),
            true,
            CmsContainerpageController.get().getData().getDetailId(),
            new CloseHandler<PopupPanel>() {

                public void onClose(CloseEvent<PopupPanel> event) {

                    deactivateCurrentButton();
                    activateSelection();
                }

            });
    }

    /**
     * Opens the lock report for the given element.<p>
     *
     * @param element the element
     */
    public void openLockReportForElement(final CmsContainerPageElementPanel element) {

        CmsLockReportDialog.openDialogForResource(null, element.getStructureId(), new Command() {

            public void execute() {

                m_controller.reloadElements(new String[] {element.getStructureId().toString()});
            }
        }, null);
    }

    /**
     * Opens the model select dialog for the given new element.<p>
     *
     * @param element the element widget
     * @param modelResources the available resource models
     */
    public void openModelResourceSelect(
        final CmsContainerPageElementPanel element,
        List<CmsModelResourceInfo> modelResources) {

        I_CmsModelSelectHandler handler = new I_CmsModelSelectHandler() {

            public void onModelSelect(CmsUUID modelStructureId) {

                m_controller.createAndEditNewElement(element, modelStructureId);
            }
        };
        String title = org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_MODEL_SELECT_TITLE_0);
        String message = org.opencms.gwt.client.Messages.get().key(
            org.opencms.gwt.client.Messages.GUI_MODEL_SELECT_MESSAGE_0);
        CmsModelSelectDialog dialog = new CmsModelSelectDialog(handler, modelResources, title, message);
        dialog.center();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#refreshResource(org.opencms.util.CmsUUID)
     */
    public void refreshResource(CmsUUID structureId) {

        if (!m_controller.hasPageChanged()) {
            m_controller.resetPage();
            return;
        }
        StringBuffer message = new StringBuffer();
        message.append("<p>" + Messages.get().key(Messages.GUI_DIALOG_RELOAD_TEXT_0) + "</p>");
        message.append("<p>" + Messages.get().key(Messages.GUI_DIALOG_SAVE_QUESTION_0) + "</p>");
        CmsConfirmDialog dialog = new CmsConfirmDialog(
            Messages.get().key(Messages.GUI_DIALOG_RELOAD_TITLE_0),
            message.toString());
        dialog.setOkText(Messages.get().key(Messages.GUI_BUTTON_SAVE_TEXT_0));
        dialog.setHandler(new I_CmsConfirmDialogHandler() {

            public void onClose() {

                // do nothing
            }

            public void onOk() {

                m_controller.saveContainerpage();
            }
        });
        dialog.center();

    }

    /**
     * Re-initializes the inline editing.<p>
     */
    public void reInitInlineEditing() {

        m_controller.reInitInlineEditing();
    }

    /**
     * Reloads the content for the given element and all related elements.<p>
     *
     * @param elementIds the element id's
     */
    public void reloadElements(String... elementIds) {

        m_controller.reloadElements(elementIds);
    }

    /**
     * Removes the given container-page element.<p>
     *
     * @param element the element
     *
     */
    public void removeElement(CmsContainerPageElementPanel element) {

        m_controller.removeElement(element);
    }

    /**
     * Removes the given container-page element.<p>
     *
     * @param element the element
     * @param removeMode the element remove mode
     *
     */
    public void removeElement(CmsContainerPageElementPanel element, ElementRemoveMode removeMode) {

        m_controller.removeElement(element, removeMode);
    }

    /**
     * Shows list collector direct edit buttons (old direct edit style), if present.<p>
     */
    public void resetEditableListButtons() {

        m_controller.resetEditButtons();
    }

    /**
     * Resets the container-page to it's previous state.<p>
     */
    public void resetPage() {

        CmsConfirmDialog dialog = new CmsConfirmDialog(
            Messages.get().key(Messages.GUI_DIALOG_RESET_TITLE_0),
            "<p>" + Messages.get().key(Messages.GUI_DIALOG_PAGE_RESET_0) + "</p>");
        dialog.setCloseText(Messages.get().key(Messages.GUI_BUTTON_CANCEL_TEXT_0));
        dialog.setOkText(Messages.get().key(Messages.GUI_BUTTON_DISCARD_TEXT_0));
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
        setEditButtonsVisible((button == null) || m_editor.getSelection().equals(button));
    }

    /**
     * Sets the edit button visible.<p>
     *
     * @param visible the visibility
     */
    public void setEditButtonsVisible(boolean visible) {

        if (visible) {
            m_editButtonVibility.setValue(
                org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle.INSTANCE.containerpageCss().editButtonsVisible());
        } else {
            m_editButtonVibility.setValue(null);
        }
    }

    /**
     * Shows resource information for a given element.<p>
     *
     * @param element the element for which to show the information
     */
    public void showElementInfo(CmsContainerPageElementPanel element) {

        CmsUUID structureId;
        if (element.isModelGroup() && !element.getModelGroupId().isNullUUID()) {
            structureId = element.getModelGroupId();
        } else {
            structureId = element.getStructureId();
        }
        CmsResourceInfoDialog.load(structureId, true, null, null);
    }

    /**
     * Shows a page overlay preventing user actions.<p>
     */
    public void showPageOverlay() {

        if (m_overlay == null) {
            m_overlay = new SimplePanel();
            m_overlay.setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupOverlay());
            Style style = m_overlay.getElement().getStyle();
            style.setWidth(100, Unit.PCT);
            style.setHeight(100, Unit.PCT);
            style.setPosition(Position.FIXED);
        }
        RootPanel.get().add(m_overlay);
    }

    /**
     * Toggles the tool-bars visibility.<p>
     *
     * @param buttonLeft the selection button absolute left position, needed to show the leave preview button in the same location
     */
    public void showPreview(int buttonLeft) {

        UrlBuilder location = Window.Location.createUrlBuilder();
        location.setParameter(CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT, Boolean.toString(true));
        location.setParameter(CmsGwtConstants.PARAM_BUTTON_LEFT, Integer.toString(buttonLeft));
        Window.Location.assign(location.buildString());
    }

    /**
     * Shows the publish dialog.<p>
     */
    public void showPublishDialog() {

        if (m_controller.hasPageChanged()) {

            StringBuffer warningMessage = new StringBuffer();
            warningMessage.append("<p>" + Messages.get().key(Messages.GUI_DIALOG_PUBLISH_NOT_SAVED_0) + "</p>");
            warningMessage.append("<p>" + Messages.get().key(Messages.GUI_DIALOG_SAVE_QUESTION_0) + "</p>");

            CmsAcceptDeclineCancelDialog leavingDialog = new CmsAcceptDeclineCancelDialog(
                Messages.get().key(Messages.GUI_DIALOG_NOT_SAVED_TITLE_0),
                warningMessage.toString());
            leavingDialog.setAcceptText(Messages.get().key(Messages.GUI_BUTTON_SAVE_TEXT_0));
            leavingDialog.setDeclineText(Messages.get().key(Messages.GUI_BUTTON_DISCARD_TEXT_0));
            leavingDialog.setCloseText(Messages.get().key(Messages.GUI_BUTTON_RETURN_TEXT_0));

            leavingDialog.setHandler(new I_CmsAcceptDeclineCancelHandler() {

                /**
                 * @see org.opencms.gwt.client.ui.I_CmsAcceptDeclineCancelHandler#onAccept()
                 */
                public void onAccept() {

                    m_controller.syncSaveContainerpage();
                    openPublish();
                }

                /**
                 * @see org.opencms.gwt.client.ui.I_CmsCloseDialogHandler#onClose()
                 */
                public void onClose() {

                    deactivateCurrentButton();
                    activateSelection();
                }

                /**
                 * @see org.opencms.gwt.client.ui.I_CmsAcceptDeclineCancelHandler#onDecline()
                 */
                public void onDecline() {

                    openPublish();
                }
            });
            leavingDialog.center();
        } else {
            openPublish();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsToolbarHandler#transformSingleEntry(org.opencms.util.CmsUUID, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    @Override
    public I_CmsContextMenuEntry transformSingleEntry(CmsUUID structureId, CmsContextMenuEntryBean menuEntryBean) {

        String name = menuEntryBean.getName();
        if (name == null) {
            return super.transformSingleEntry(structureId, menuEntryBean);
        }
        if (name.equals(CmsGwtConstants.ACTION_TEMPLATECONTEXTS)) {
            return createTemplateContextSelectionMenuEntry(structureId);
        } else if (name.equals(CmsGwtConstants.ACTION_EDITSMALLELEMENTS)) {
            return createToggleEditSmallElementsMenuEntry();
        } else if (name.equals(CmsGwtConstants.ACTION_SELECTELEMENTVIEW)) {
            return createElementViewSelectionMenuEntry();
        } else if (name.equals(CmsGwtConstants.ACTION_SHOWLOCALE)) {
            return createShowLocaleMenuEntry();
        } else if (name.equals(CmsGwtConstants.ACTION_VIEW_ONLINE)) {
            return createViewOnlineEntry();
        } else if (name.equals(CmsPreview.class.getName())) {
            return null;
        } else {
            return super.transformSingleEntry(structureId, menuEntryBean);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#unlockResource(org.opencms.util.CmsUUID)
     */
    public void unlockResource(CmsUUID structureId) {

        // only unlock the container page, if nothing has changed yet
        if (!m_controller.hasPageChanged()) {
            m_controller.unlockContainerpage();
        }
    }

    /**
     * Updates the clip board elements is necessary.<p>
     *
     * @param elements the elements data
     */
    public void updateClipboard(Map<String, CmsContainerElementData> elements) {

        if (m_editor.getClipboard().isOpen()) {
            for (CmsContainerElementData elementData : elements.values()) {
                m_editor.getClipboard().replaceFavoriteItem(
                    m_controller.getContainerpageUtil().createListItem(elementData));
                m_editor.getClipboard().replaceRecentItem(
                    m_controller.getContainerpageUtil().createListItem(elementData));
            }
        }
    }

    /**
     * Creates the element view selection menu entry, returns <code>null</code> in case no other views available.<p>
     *
     * @return the menu entry
     */
    protected I_CmsContextMenuEntry createElementViewSelectionMenuEntry() {

        List<CmsElementViewInfo> elementViews = m_controller.getData().getElementViews();
        if (elementViews.size() > 1) {
            CmsContextMenuEntry parentEntry = new CmsContextMenuEntry(this, null, new I_CmsContextMenuCommand() {

                public void execute(
                    CmsUUID innerStructureId,
                    I_CmsContextMenuHandler handler,
                    CmsContextMenuEntryBean bean) {

                    // do nothing
                }

                public A_CmsContextMenuItem getItemWidget(
                    CmsUUID innerStructureId,
                    I_CmsContextMenuHandler handler,
                    CmsContextMenuEntryBean bean) {

                    return null;
                }

                public boolean hasItemWidget() {

                    return false;
                }

            });
            CmsContextMenuEntryBean parentBean = new CmsContextMenuEntryBean();

            parentBean.setLabel(Messages.get().key(Messages.GUI_SELECT_ELEMENT_VIEW_0));
            parentBean.setActive(true);
            parentBean.setVisible(true);
            parentEntry.setBean(parentBean);
            List<I_CmsContextMenuEntry> viewEntries = new ArrayList<I_CmsContextMenuEntry>();
            for (CmsElementViewInfo viewInfo : elementViews) {
                if (viewInfo.isRoot()) {
                    viewEntries.add(
                        createMenuEntryForElementView(
                            viewInfo,
                            m_controller.matchRootView(viewInfo.getElementViewId()),
                            this));
                }
            }

            parentEntry.setSubMenu(viewEntries);
            return parentEntry;
        } else {
            return null;
        }
    }

    /**
     * Creates a menu entry based on a structure id and action without anything else.<p>
     *
     * @param structureId the structure id
     * @param action the action for the menu entry
     *
     * @return the new menu entry
     */
    protected CmsContextMenuEntry createRawMenuEntry(CmsUUID structureId, final Runnable action) {

        CmsContextMenuEntry entry = new CmsContextMenuEntry(this, structureId, new I_CmsContextMenuCommand() {

            public void execute(
                CmsUUID innerStructureId,
                I_CmsContextMenuHandler handler,
                CmsContextMenuEntryBean bean) {

                if (action != null) {
                    action.run();
                }
            }

            public A_CmsContextMenuItem getItemWidget(
                CmsUUID innerStructureId,
                I_CmsContextMenuHandler handler,
                CmsContextMenuEntryBean bean) {

                return null;
            }

            public boolean hasItemWidget() {

                return false;
            }

        });
        return entry;
    }

    /**
     * Creates the template context selection entry for the context menu.<p>
     *
     * @param structureId the structure id of the page
     *
     * @return the new context menu entry
     */
    protected I_CmsContextMenuEntry createTemplateContextSelectionMenuEntry(CmsUUID structureId) {

        final CmsTemplateContextInfo info = m_controller.getData().getTemplateContextInfo();
        if ((info.getCookieName() != null) && info.shouldShowTemplateContextContextMenuEntry()) {
            CmsContextMenuEntry parentEntry = new CmsContextMenuEntry(this, structureId, new I_CmsContextMenuCommand() {

                public void execute(
                    CmsUUID innerStructureId,
                    I_CmsContextMenuHandler handler,
                    CmsContextMenuEntryBean bean) {

                    // do nothing
                }

                public A_CmsContextMenuItem getItemWidget(
                    CmsUUID innerStructureId,
                    I_CmsContextMenuHandler handler,
                    CmsContextMenuEntryBean bean) {

                    return null;
                }

                public boolean hasItemWidget() {

                    return false;
                }

            });
            CmsContextMenuEntryBean parentBean = new CmsContextMenuEntryBean();

            parentBean.setLabel(
                org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.GUI_TEMPLATE_CONTEXT_PARENT_0));
            parentBean.setActive(true);
            parentBean.setVisible(true);
            parentEntry.setBean(parentBean);

            Map<String, String> contextNames = info.getContextLabels();

            List<I_CmsContextMenuEntry> templateContextEntries = new ArrayList<I_CmsContextMenuEntry>();
            for (Map.Entry<String, String> entry : contextNames.entrySet()) {
                final String key = entry.getKey();
                final String label = entry.getValue();
                if (info.hasClientVariants(key)) {
                    CmsContextMenuEntry singleContextEntry = createRawMenuEntry(structureId, DO_NOTHING);
                    boolean showCheckbox = Objects.equal(info.getSelectedContext(), entry.getKey());
                    decorateMenuEntry(singleContextEntry, label, showCheckbox);
                    List<I_CmsContextMenuEntry> variantEntries = new ArrayList<I_CmsContextMenuEntry>();
                    CmsContextMenuEntry editVariantEntry = createMenuEntryForTemplateContext(
                        info.getCookieName(),
                        key,
                        org.opencms.ade.containerpage.client.Messages.get().key(
                            org.opencms.ade.containerpage.client.Messages.GUI_TEMPLATE_CONTEXT_NO_VARIANT_0),
                        false,
                        this,
                        structureId);
                    variantEntries.add(editVariantEntry);
                    Map<String, CmsClientVariantInfo> variants = info.getClientVariants(key);
                    for (CmsClientVariantInfo variant : variants.values()) {
                        CmsContextMenuEntry currentVariantEntry = createRawMenuEntry(
                            structureId,
                            new ClientVariantSelectAction(key, variant));
                        decorateMenuEntry(currentVariantEntry, variant.getNiceName(), false);
                        variantEntries.add(currentVariantEntry);
                    }
                    singleContextEntry.setSubMenu(variantEntries);
                    templateContextEntries.add(singleContextEntry);

                } else {

                    CmsContextMenuEntry menuEntry = createMenuEntryForTemplateContext(
                        info.getCookieName(),
                        key,
                        label,
                        Objects.equal(key, info.getSelectedContext()),
                        this,
                        structureId);
                    templateContextEntries.add(menuEntry);
                }
            }
            templateContextEntries.add(
                createMenuEntryForTemplateContext(
                    info.getCookieName(),
                    null,
                    org.opencms.gwt.client.Messages.get().key(
                        org.opencms.gwt.client.Messages.GUI_TEMPLATE_CONTEXT_NONE_0),
                    Objects.equal(null, info.getSelectedContext()),
                    this,
                    structureId));
            parentEntry.setSubMenu(templateContextEntries);

            return parentEntry;

        } else {
            return null;
        }
    }

    /**
     * Creates the context menu entry for enabling or disabling editing of small elements.<p>
     *
     * @return the created context menu entry
     */
    protected I_CmsContextMenuEntry createToggleEditSmallElementsMenuEntry() {

        final CmsSmallElementsHandler smallElementsHandler = m_controller.getSmallElementsHandler();
        final boolean isActive = smallElementsHandler.areSmallElementsEditable();
        CmsContextMenuEntryBean entryBean = new CmsContextMenuEntryBean();
        String baseMessage = Messages.get().key(Messages.GUI_EDIT_SMALL_ELEMENTS_0);
        String msgEdit = baseMessage;
        String msgDisable = baseMessage;
        String label = isActive ? msgDisable : msgEdit;
        entryBean.setLabel(label);
        entryBean.setActive(smallElementsHandler.hasSmallElements());
        entryBean.setVisible(true);
        I_CmsInputCss inputCss = I_CmsInputLayoutBundle.INSTANCE.inputCss();
        entryBean.setIconClass(isActive ? inputCss.checkBoxImageChecked() : inputCss.checkBoxImageUnchecked());
        I_CmsContextMenuCommand command = new I_CmsContextMenuCommand() {

            public void execute(CmsUUID structureId, I_CmsContextMenuHandler handler, CmsContextMenuEntryBean bean) {

                smallElementsHandler.setEditSmallElements(!isActive, true);
            }

            public A_CmsContextMenuItem getItemWidget(
                CmsUUID structureId,
                I_CmsContextMenuHandler handler,
                CmsContextMenuEntryBean bean) {

                return null;
            }

            public boolean hasItemWidget() {

                return false;
            }
        };
        CmsContextMenuEntry entry = new CmsContextMenuEntry(this, null, command);
        entry.setBean(entryBean);
        return entry;
    }

    /**
     * Creates the view online entry, if an online link is available.<p>
     *
     * @return the menu entry or null, if not available
     */
    protected I_CmsContextMenuEntry createViewOnlineEntry() {

        final String onlineLink = m_controller.getData().getOnlineLink();
        CmsContextMenuEntry entry = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(onlineLink)) {
            I_CmsContextMenuCommand command = new I_CmsContextMenuCommand() {

                public void execute(
                    CmsUUID structureId,
                    I_CmsContextMenuHandler handler,
                    CmsContextMenuEntryBean bean) {

                    Window.open(onlineLink, "opencms-online", null);
                }

                public A_CmsContextMenuItem getItemWidget(
                    CmsUUID structureId,
                    I_CmsContextMenuHandler handler,
                    CmsContextMenuEntryBean bean) {

                    return null;
                }

                public boolean hasItemWidget() {

                    return false;
                }
            };
            entry = new CmsContextMenuEntry(this, null, command);
            CmsContextMenuEntryBean entryBean = new CmsContextMenuEntryBean();
            entryBean.setLabel(Messages.get().key(Messages.GUI_VIEW_ONLINE_0));
            entryBean.setActive(true);
            entryBean.setVisible(true);
            entry.setBean(entryBean);
        }
        return entry;
    }

    /**
     * Fills in label and checkbox of a menu entry.<p>
     *
     * @param entry the menu entry
     * @param name the label
     * @param checked true if checkbox should be shown
     */
    protected void decorateMenuEntry(CmsContextMenuEntry entry, String name, boolean checked) {

        CmsContextMenuEntryBean bean = new CmsContextMenuEntryBean();
        bean.setLabel(name);
        bean.setActive(true);
        bean.setVisible(true);
        I_CmsInputCss inputCss = I_CmsInputLayoutBundle.INSTANCE.inputCss();
        bean.setIconClass(checked ? inputCss.checkBoxImageChecked() : "");
        entry.setBean(bean);
    }

    /**
     * Helper method for getting the error message for a locking error.<p>
     *
     * @param lockInfo the lock information
     * @return the error message
     */
    protected String getLockErrorMessage(CmsLockInfo lockInfo) {

        switch (lockInfo.getState()) {
            case changed:
                return Messages.get().key(Messages.ERR_LOCK_RESOURCE_CHANGED_BY_1, lockInfo.getUser());
            case locked:
                return Messages.get().key(Messages.ERR_LOCK_RESOURCE_LOCKED_BY_1, lockInfo.getUser());
            case other:
                return lockInfo.getErrorMessage();
            case success:
            default:
                return "";
        }
    }

    /**
     * Helper method for getting the error message box title for a locking error.<p>
     *
     * @param lockInfo the lock information
     * @return the error message box title
     */
    protected String getLockErrorTitle(CmsLockInfo lockInfo) {

        switch (lockInfo.getState()) {
            case changed:
                return Messages.get().key(Messages.ERR_LOCK_TITLE_RESOURCE_CHANGED_0);
            case locked:
                return Messages.get().key(Messages.ERR_LOCK_TITLE_RESOURCE_LOCKED_0);
            case other:
            case success:
            default:
                return Messages.get().key(Messages.GUI_LOCK_FAIL_0);
        }
    }

    /**
     * Opens the publish dialog without changes check.<p>
     */
    protected void openPublish() {

        HashMap<String, String> params = Maps.newHashMap();
        params.put(CmsPublishOptions.PARAM_CONTAINERPAGE, "" + CmsCoreProvider.get().getStructureId());
        params.put(CmsPublishOptions.PARAM_START_WITH_CURRENT_PAGE, "");
        params.put(CmsPublishOptions.PARAM_DETAIL, "" + m_controller.getData().getDetailId());
        CmsPublishDialog.showPublishDialog(params, new CloseHandler<PopupPanel>() {

            /**
             * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
             */
            public void onClose(CloseEvent<PopupPanel> event) {

                deactivateCurrentButton();
                activateSelection();

            }
        }, new Runnable() {

            public void run() {

                openPublish();
            }

        }, m_controller.getContentEditorHandler());
    }

    /**
     * Sets the element view.<p>
     *
     * @param elementView the element view
     * @param nextAction the action to execute after setting the view
     */
    protected void setElementView(CmsElementViewInfo elementView, Runnable nextAction) {

        m_controller.setElementView(elementView, nextAction);
    }

    /**
     * Creates the menu entry for a single element view.<p>
     *
     * @param elementView the element view
     * @param isActive if the group is the currently active group
     * @param handler the menu handler
     *
     * @return the menu entry
     */
    private CmsContextMenuEntry createMenuEntryForElementView(
        final CmsElementViewInfo elementView,
        boolean isActive,
        I_CmsContextMenuHandler handler) {

        CmsContextMenuEntry menuEntry = new CmsContextMenuEntry(handler, null, new I_CmsContextMenuCommand() {

            public void execute(
                CmsUUID innerStructureId,
                I_CmsContextMenuHandler innerHandler,
                CmsContextMenuEntryBean bean) {

                setElementView(elementView, null);
            }

            public A_CmsContextMenuItem getItemWidget(
                CmsUUID innerStructureId,
                I_CmsContextMenuHandler innerHandler,
                CmsContextMenuEntryBean bean) {

                return null;
            }

            public boolean hasItemWidget() {

                return false;
            }
        });
        CmsContextMenuEntryBean bean = new CmsContextMenuEntryBean();
        bean.setLabel(elementView.getTitle());

        I_CmsInputCss inputCss = I_CmsInputLayoutBundle.INSTANCE.inputCss();
        bean.setIconClass(isActive ? inputCss.checkBoxImageChecked() : "");
        bean.setActive(!isActive);
        bean.setVisible(true);
        menuEntry.setBean(bean);
        return menuEntry;

    }

    /**
     * Creates a context menu entry for selecting a template context.<p>
     *
     * @param cookieName the name of the cookie
     * @param value the value of the cookie
     * @param label the text for the menu entry
     * @param isActive true if context is currently active
     * @param handler the context menu handler
     * @param structureId the current page's structure id
     *
     * @return the created context menu entry
     */
    private CmsContextMenuEntry createMenuEntryForTemplateContext(
        final String cookieName,
        final String value,
        String label,
        boolean isActive,
        I_CmsContextMenuHandler handler,
        CmsUUID structureId) {

        CmsContextMenuEntry menuEntry = new CmsContextMenuEntry(handler, structureId, new I_CmsContextMenuCommand() {

            public void execute(
                CmsUUID innerStructureId,
                I_CmsContextMenuHandler innerHandler,
                CmsContextMenuEntryBean bean) {

                changeTemplateContextManually(cookieName, value);
            }

            public A_CmsContextMenuItem getItemWidget(
                CmsUUID innerStructureId,
                I_CmsContextMenuHandler innerHandler,
                CmsContextMenuEntryBean bean) {

                return null;
            }

            public boolean hasItemWidget() {

                return false;
            }
        });
        CmsContextMenuEntryBean bean = new CmsContextMenuEntryBean();
        bean.setLabel(label);

        I_CmsInputCss inputCss = I_CmsInputLayoutBundle.INSTANCE.inputCss();
        bean.setIconClass(isActive ? inputCss.checkBoxImageChecked() : "");
        bean.setActive(true);
        bean.setVisible(true);
        menuEntry.setBean(bean);
        return menuEntry;

    }

    /**
     * Creates the real entries for the  "Show locale" option.<p>
     *
     * @return the real entry
     */
    private I_CmsContextMenuEntry createShowLocaleMenuEntry() {

        Map<String, CmsLocaleLinkBean> localeLinkBeans = CmsContainerpageController.get().getData().getLocaleLinkBeans();
        if ((localeLinkBeans == null) || localeLinkBeans.isEmpty()) {
            return null;
        }

        CmsContextMenuEntry parentEntry = new CmsContextMenuEntry(this, null, new I_CmsContextMenuCommand() {

            public void execute(
                CmsUUID innerStructureId,
                I_CmsContextMenuHandler handler,
                CmsContextMenuEntryBean bean) {

                // do nothing
            }

            public A_CmsContextMenuItem getItemWidget(
                CmsUUID innerStructureId,
                I_CmsContextMenuHandler handler,
                CmsContextMenuEntryBean bean) {

                return null;
            }

            public boolean hasItemWidget() {

                return false;
            }

        });
        CmsContextMenuEntryBean parentBean = new CmsContextMenuEntryBean();

        parentBean.setLabel(
            org.opencms.ade.containerpage.client.Messages.get().key(
                org.opencms.ade.containerpage.client.Messages.GUI_SHOW_LOCALE_0));
        parentBean.setActive(true);
        parentBean.setVisible(true);
        parentEntry.setBean(parentBean);
        List<I_CmsContextMenuEntry> subEntries = Lists.newArrayList();

        for (Map.Entry<String, CmsLocaleLinkBean> entry : localeLinkBeans.entrySet()) {
            String label = entry.getKey();
            final CmsLocaleLinkBean linkBean = entry.getValue();
            CmsContextMenuEntry childEntry = new CmsContextMenuEntry(this, null, new I_CmsContextMenuCommand() {

                public void execute(
                    CmsUUID structureId,
                    I_CmsContextMenuHandler handler,
                    CmsContextMenuEntryBean bean) {

                    if (linkBean.getError() != null) {
                        CmsNotification.get().sendAlert(Type.WARNING, linkBean.getError());
                    } else if (linkBean.getLink() != null) {
                        Window.Location.assign(linkBean.getLink());
                    }
                }

                public A_CmsContextMenuItem getItemWidget(
                    CmsUUID structureId,
                    I_CmsContextMenuHandler handler,
                    CmsContextMenuEntryBean bean) {

                    return null;
                }

                public boolean hasItemWidget() {

                    return false;
                }
            });
            CmsContextMenuEntryBean childBean = new CmsContextMenuEntryBean();
            childBean.setLabel(label);
            childBean.setActive(true);
            childBean.setVisible(true);
            childEntry.setBean(childBean);
            subEntries.add(childEntry);
        }
        parentEntry.setSubMenu(subEntries);
        if (subEntries.isEmpty()) {
            return null;
        } else {
            return parentEntry;
        }
    }

    /**
     * Returns the page leaving dialog.<p>
     *
     * @return the page leaving dialog
     */
    private CmsAcceptDeclineCancelDialog getLeaveDialog() {

        StringBuffer message = new StringBuffer();
        message.append("<p>" + Messages.get().key(Messages.GUI_DIALOG_LEAVE_NOT_SAVED_0) + "</p>");
        message.append("<p>" + Messages.get().key(Messages.GUI_DIALOG_SAVE_QUESTION_0) + "</p>");

        CmsAcceptDeclineCancelDialog leavingDialog = new CmsAcceptDeclineCancelDialog(
            Messages.get().key(Messages.GUI_DIALOG_NOT_SAVED_TITLE_0),
            message.toString());
        leavingDialog.setAcceptText(Messages.get().key(Messages.GUI_BUTTON_SAVE_TEXT_0));
        leavingDialog.setDeclineText(Messages.get().key(Messages.GUI_BUTTON_DISCARD_TEXT_0));
        leavingDialog.setCloseText(Messages.get().key(Messages.GUI_BUTTON_RETURN_TEXT_0));
        return leavingDialog;
    }

    /**
     * Opens the group-container element editor.<p>
     *
     * @param groupContainer the group-container element
     */
    private void openGroupEditor(CmsGroupContainerElementPanel groupContainer) {

        m_controller.startEditingGroupcontainer(groupContainer, groupContainer.isGroupContainer());
    }
}

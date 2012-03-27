/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsGroupContainerElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsGroupcontainerEditor;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.publish.client.CmsPublishDialog;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.dnd.I_CmsDNDController;
import org.opencms.gwt.client.ui.A_CmsToolbarHandler;
import org.opencms.gwt.client.ui.A_CmsToolbarMenu;
import org.opencms.gwt.client.ui.CmsAcceptDeclineCancelDialog;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsModelSelectDialog;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.I_CmsAcceptDeclineCancelHandler;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.ui.I_CmsModelSelectHandler;
import org.opencms.gwt.client.ui.I_CmsToolbarButton;
import org.opencms.gwt.client.ui.contextmenu.CmsAvailabilityDialog;
import org.opencms.gwt.client.ui.contextmenu.CmsEditProperties;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.CmsInfoBoxFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.I_CmsFormHandler;
import org.opencms.gwt.client.util.CmsCollectionUtil;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Method;
import org.opencms.gwt.client.util.CmsDomUtil.Target;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsLockInfo;
import org.opencms.gwt.shared.CmsModelResourceInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The container-page handler.<p>
 * 
 * @since 8.0.0
 */
public class CmsContainerpageHandler extends A_CmsToolbarHandler {

    /** The container-page controller. */
    protected CmsContainerpageController m_controller;

    /** The container-page editor. */
    protected CmsContainerpageEditor m_editor;

    /** The currently active tool-bar button. */
    private I_CmsToolbarButton m_activeButton;

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
    }

    /**
     * Activates the selection button.<p>
     */
    public void activateSelection() {

        m_editor.getSelection().setActive(true);
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

        m_controller.addToRecentList(clientId);
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
     * De-activates menu button.<p>
     */
    public void deactivateMenuButton() {

        if ((m_activeButton != null) && (m_activeButton instanceof A_CmsToolbarMenu<?>)) {
            ((A_CmsToolbarMenu<?>)m_activeButton).setActive(false);
        }
    }

    /**
     * Deactivates all toolbar buttons.<p>
     */
    public void deactivateToolbarButtons() {

        for (Widget button : m_editor.getToolbar().getAll()) {
            if (button instanceof I_CmsToolbarButton) {
                ((I_CmsToolbarButton)button).setEnabled(false);
            }
        }
    }

    /**
     * Starts the property editor for the given container element.<p>
     * 
     * @param elementWidget the container element widget for which the properties should be edited 
     */
    public void editElementSettings(final CmsContainerPageElementPanel elementWidget) {

        final String id = elementWidget.getId();

        m_controller.getElement(id, new I_CmsSimpleCallback<CmsContainerElementData>() {

            public void execute(final CmsContainerElementData elementBean) {

                Map<String, String> settings = elementBean.getSettings();
                Map<String, CmsXmlContentProperty> propertyConfig = elementBean.getSettingConfig();
                if (propertyConfig.size() == 0) {
                    String message = Messages.get().key(Messages.GUI_NO_SETTINGS_0);
                    String title = Messages.get().key(Messages.GUI_NO_SETTINGS_TITLE_0);
                    (new CmsAlertDialog(title, message)).center();
                    return;
                }
                final CmsForm form = new CmsForm(false);
                CmsListInfoBean infoBean = new CmsListInfoBean();
                infoBean.setTitle(elementBean.getTitle());
                infoBean.setSubTitle(elementBean.getSitePath());
                infoBean.setResourceType(elementBean.getResourceType());
                CmsInfoBoxFormFieldPanel formFieldPanel = new CmsInfoBoxFormFieldPanel(infoBean);
                form.setWidget(formFieldPanel);
                I_CmsFormHandler formHandler = new I_CmsFormHandler() {

                    /**
                     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onSubmitForm(java.util.Map, java.util.Set)
                     */
                    public void onSubmitForm(Map<String, String> fieldValues, Set<String> editedFields) {

                        m_controller.reloadElementWithSettings(
                            elementWidget,
                            elementBean.getClientId(),
                            CmsCollectionUtil.removeNullEntries(fieldValues));
                    }
                };
                String title = Messages.get().key(Messages.GUI_PROPERTY_DIALOG_TITLE_0);

                CmsFormDialog dialog = new CmsFormDialog(title, form);

                Map<String, I_CmsFormField> formFields = CmsBasicFormField.createFields(propertyConfig.values());

                for (I_CmsFormField field : formFields.values()) {
                    String fieldId = field.getId();
                    String initialValue = settings.get(fieldId);
                    if (initialValue == null) {
                        CmsXmlContentProperty propDef = propertyConfig.get(fieldId);
                        initialValue = propDef.getDefault();
                    }
                    form.addField(field, initialValue);
                }
                form.render();
                dialog.setFormHandler(formHandler);
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
     * Enables the save and reset button of the tool-bar.<p>
     * 
     * @param enable <code>true</code> to enable
     */
    public void enableSaveReset(boolean enable) {

        if (enable) {
            m_editor.getSave().enable();
            m_editor.getReset().enable();
        } else {
            m_editor.getSave().disable(Messages.get().key(Messages.GUI_BUTTON_SAVE_DISABLED_0));
            m_editor.getReset().disable(Messages.get().key(Messages.GUI_BUTTON_RESET_DISABLED_0));
        }
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
     * Leaves the current page and opens the site-map.<p>
     */
    public void gotoSitemap() {

        final String sitemapUri = CmsCoreProvider.get().link(m_controller.getData().getSitemapUri());
        if (sitemapUri.equals("")) {
            return; // normally, we shouldn't even get to this point because the sitemap button should be disabled  
        }
        Command leaveCommand = new Command() {

            public void execute() {

                Map<String, String> parameter = new HashMap<String, String>();
                parameter.put(CmsCoreData.PARAM_PATH, CmsContainerpageController.getCurrentUri());
                parameter.put(CmsCoreData.PARAM_RETURNCODE, m_controller.getReturnCode());
                FormElement form = CmsDomUtil.generateHiddenForm(sitemapUri, Method.post, Target.TOP, parameter);
                RootPanel.getBodyElement().appendChild(form);
                form.submit();

            }

        };
        leavePage(leaveCommand);
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
            }
        });
    }

    /**
     * Should be called when locking the container page failed.<p>
     * 
     * @param lockInfo the locking information  
     */
    public void onLockFail(CmsLockInfo lockInfo) {

        String errorMessage = getLockErrorMessage(lockInfo);
        String errorTitle = getLockErrorTitle(lockInfo);
        m_editor.disableEditing(errorMessage);
        CmsAlertDialog alert = new CmsAlertDialog(errorTitle, errorMessage);
        alert.center();
    }

    /**
     * Opens the edit dialog for the specified element.<p>
     * 
     * @param element the element to edit
     */
    public void openEditorForElement(CmsContainerPageElementPanel element) {

        if (element.isNew()) {
            //openEditorForElement will be called again asynchronously when the RPC for creating the element has finished 
            m_controller.createAndEditNewElement(element);
            return;
        }

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(element.getNoEditReason())) {
            CmsNotification.get().send(
                CmsNotification.Type.WARNING,
                "should be deactivated: " + element.getNoEditReason());
            Timer timer = new Timer() {

                /**
                 * @see com.google.gwt.user.client.Timer#run()
                 */
                @Override
                public void run() {

                    CmsNotification.get().hide();
                }
            };
            timer.schedule(2000);
            return;
        }

        if (CmsDomUtil.hasClass(CmsContainerpageUtil.CLASS_GROUP_CONTAINER_ELEMENT_MARKER, element.getElement())) {
            openGroupcontainerEditor((CmsGroupContainerElementPanel)element);
        } else {
            m_controller.getContentEditorHandler().openDialog(element.getId(), element.getSitePath());
        }
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
        String title = org.opencms.ade.containerpage.client.Messages.get().key(
            org.opencms.ade.containerpage.client.Messages.GUI_MODEL_SELECT_TITLE_0);
        String message = org.opencms.ade.containerpage.client.Messages.get().key(
            org.opencms.ade.containerpage.client.Messages.GUI_MODEL_SELECT_MESSAGE_0);
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
     */
    public void removeElement(CmsContainerPageElementPanel element) {

        m_controller.removeElement(element);
    }

    /**
     * Shows list collector direct edit buttons (old direct edit style), if present.<p>
     */
    public void resetEditableListButtons() {

        m_controller.resetEditableListButtons();
    }

    /**
     * Resets the container-page to it's previous state.<p>
     */
    public void resetPage() {

        CmsConfirmDialog dialog = new CmsConfirmDialog(Messages.get().key(Messages.GUI_DIALOG_RESET_TITLE_0), "<p>"
            + Messages.get().key(Messages.GUI_DIALOG_PAGE_RESET_0)
            + "</p>");
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
     * Shows the publish dialog.<p>
     */
    public void showPublishDialog() {

        if (m_controller.hasPageChanged()) {

            StringBuffer warningMessage = new StringBuffer();
            warningMessage.append("<p>" + Messages.get().key(Messages.GUI_DIALOG_PUBLISH_NOT_SAVED_0) + "</p>");
            warningMessage.append("<p>" + Messages.get().key(Messages.GUI_DIALOG_SAVE_QUESTION_0) + "</p>");

            CmsAcceptDeclineCancelDialog leavingDialog = new CmsAcceptDeclineCancelDialog(Messages.get().key(
                Messages.GUI_DIALOG_NOT_SAVED_TITLE_0), warningMessage.toString());
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
     * Toggles the tool-bars visibility.<p>
     */
    public void toggleToolbar() {

        if (m_editor.isToolbarVisible()) {
            m_editor.showToolbar(false);
            m_controller.setToolbarVisible(false);
        } else {
            m_editor.showToolbar(true);
            m_controller.setToolbarVisible(true);
            activateSelection();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsToolbarHandler#transformEntries(java.util.List, org.opencms.util.CmsUUID)
     */
    @Override
    public List<I_CmsContextMenuEntry> transformEntries(
        List<CmsContextMenuEntryBean> menuBeans,
        final CmsUUID structureId) {

        List<I_CmsContextMenuEntry> entries = super.transformEntries(menuBeans, structureId);
        List<I_CmsContextMenuEntry> result = new ArrayList<I_CmsContextMenuEntry>();

        for (I_CmsContextMenuEntry entry : entries) {
            if (shouldShowMenuEntry(entry)) {
                result.add(entry);
            }
        }
        return result;
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

    /** 
     * Returns the page leaving dialog.<p>
     * 
     * @return the page leaving dialog
     */
    private CmsAcceptDeclineCancelDialog getLeaveDialog() {

        StringBuffer message = new StringBuffer();
        message.append("<p>" + Messages.get().key(Messages.GUI_DIALOG_LEAVE_NOT_SAVED_0) + "</p>");
        message.append("<p>" + Messages.get().key(Messages.GUI_DIALOG_SAVE_QUESTION_0) + "</p>");

        CmsAcceptDeclineCancelDialog leavingDialog = new CmsAcceptDeclineCancelDialog(Messages.get().key(
            Messages.GUI_DIALOG_NOT_SAVED_TITLE_0), message.toString());
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
    private void openGroupcontainerEditor(CmsGroupContainerElementPanel groupContainer) {

        CmsGroupcontainerEditor.openGroupcontainerEditor(groupContainer, m_controller, this);
    }

    /**
     * Internal method to decide dynamically whether a context menu entry should be shown.<p>
     * 
     * @param entry the context menu entry 
     * @return true if the menu entry should be shown
     */
    private boolean shouldShowMenuEntry(I_CmsContextMenuEntry entry) {

        if (m_controller.isEditingDisabled()) {
            String name = entry.getName();
            if ((name != null)
                && (name.equals(CmsAvailabilityDialog.class.getName()) || name.equals(CmsEditProperties.class.getName()))) {
                return false;
            }
        }
        return true;
    }
}

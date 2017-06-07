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

package org.opencms.ade.containerpage.client.ui.groupeditor;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageEvent;
import org.opencms.ade.containerpage.client.CmsContainerpageEvent.EventType;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsGroupContainerElementPanel;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract group editor.<p>
 *
 * @since 8.5.0
 */
public abstract class A_CmsGroupEditor extends Composite {

    /** The ui-binder interface for this widget. */
    interface I_CmsGroupEditorUiBinder extends UiBinder<HTMLPanel, A_CmsGroupEditor> {
        // GWT interface, nothing to do here
    }

    /** The dialog base height. The height without any content. */
    private static final int DIALOG_BASE_HEIGHT = 103;

    /** The ui-binder for this widget. */
    private static I_CmsGroupEditorUiBinder uiBinder = GWT.create(I_CmsGroupEditorUiBinder.class);

    /** The container marker div element. */
    @UiField
    protected DivElement m_containerMarker;

    /** The dialog element. */
    @UiField
    protected FlowPanel m_dialogContent;

    /** The container element data. */
    protected CmsContainerElementData m_elementData;

    /** The overlay div element. */
    @UiField
    protected DivElement m_overlayDiv;

    /** List of elements when editing started, use to restore on cancel. */
    private List<CmsContainerPageElementPanel> m_backUpElements;

    /** The dialog cancel button. */
    private CmsPushButton m_cancelButton;

    /** The container-page controller. */
    private CmsContainerpageController m_controller;

    /** The group-container place-holder. */
    private Element m_editingPlaceholder;

    /** The editor popup dialog. */
    private CmsPopup m_editorDialog;

    /** The editor HTML-id. */
    private String m_editorId;

    /** The editor widget. */
    private HTMLPanel m_editorWidget;

    /** The group-container. */
    private CmsGroupContainerElementPanel m_groupContainer;

    /** The group container element position. */
    private CmsPositionBean m_groupContainerPosition;

    /** The the container page handler. */
    private CmsContainerpageHandler m_handler;

    /** The index position of the group-container inside it's parent. */
    private int m_indexPosition;

    /** The parent container. */
    private CmsContainerPageContainer m_parentContainer;

    /** The dialog save button. */
    private CmsPushButton m_saveButton;

    /**
     * Constructor.<p>
     *
     * @param groupContainer the group-container
     * @param controller the container-page controller
     * @param handler the container-page handler
     */
    protected A_CmsGroupEditor(
        CmsGroupContainerElementPanel groupContainer,
        CmsContainerpageController controller,
        CmsContainerpageHandler handler) {

        m_controller = controller;
        m_handler = handler;
        m_editorWidget = uiBinder.createAndBindUi(this);
        initWidget(m_editorWidget);
        m_editorId = HTMLPanel.createUniqueId();
        m_editorWidget.getElement().setId(m_editorId);
        m_groupContainer = groupContainer;
        m_backUpElements = new ArrayList<CmsContainerPageElementPanel>();
        Iterator<Widget> it = m_groupContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElementPanel) {
                m_backUpElements.add((CmsContainerPageElementPanel)w);
            }
        }
        m_parentContainer = (CmsContainerPageContainer)m_groupContainer.getParentTarget();
        m_groupContainerPosition = CmsPositionBean.getBoundingClientRect(m_groupContainer.getElement());
        m_editingPlaceholder = createPlaceholder(m_groupContainer.getElement());
        m_groupContainer.setEditingPlaceholder(m_editingPlaceholder);
        m_groupContainer.setEditingMarker(m_containerMarker);
        m_indexPosition = m_parentContainer.getWidgetIndex(m_groupContainer);
        // inserting placeholder element
        m_parentContainer.getElement().insertBefore(m_editingPlaceholder, m_groupContainer.getElement());
        m_editorWidget.add(m_groupContainer, m_editorId);
        Style style = m_groupContainer.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setLeft(m_groupContainerPosition.getLeft(), Unit.PX);
        style.setTop(m_groupContainerPosition.getTop(), Unit.PX);
        style.setWidth(m_groupContainerPosition.getWidth(), Unit.PX);
        style.setZIndex(I_CmsLayoutBundle.INSTANCE.constants().css().zIndexGroupContainer());
        m_containerMarker.getStyle().setLeft(m_groupContainerPosition.getLeft() - 3, Unit.PX);
        m_containerMarker.getStyle().setTop(m_groupContainerPosition.getTop() - 4, Unit.PX);
        m_containerMarker.getStyle().setWidth(m_groupContainerPosition.getWidth() + 4, Unit.PX);
        m_containerMarker.getStyle().setHeight(m_groupContainerPosition.getHeight() + 4, Unit.PX);
        m_containerMarker.getStyle().setBackgroundColor(
            CmsDomUtil.getEffectiveBackgroundColor(m_parentContainer.getElement()));
        m_groupContainer.getElementOptionBar().setVisible(false);
        m_groupContainer.getElementOptionBar().removeStyleName(
            I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().cmsHovering());

        RootPanel.get().addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().groupcontainerEditing());
        addInputFields();
        m_editorDialog = new CmsPopup();
        addButtons();
        if (m_saveButton != null) {
            m_saveButton.disable(Messages.get().key(Messages.GUI_GROUPCONTAINER_LOADING_DATA_0));
        }
        m_editorDialog.setGlassEnabled(false);
        m_editorDialog.setModal(false);
        m_editorDialog.addDialogClose(new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                cancelEdit();
            }
        });
    }

    /**
     * Returns the group container widget.<p>
     *
     * @return the group container widget
     */
    public CmsGroupContainerElementPanel getGroupContainerWidget() {

        return m_groupContainer;
    }

    /**
     * Returns the the container page handler.<p>
     *
     * @return the the container page handler
     */
    public CmsContainerpageHandler getHandler() {

        return m_handler;
    }

    /**
     * Hides the editor pop-up. Use during inline editing.<p>
     */
    public void hidePopup() {

        m_editorDialog.getElement().getStyle().setDisplay(Display.NONE);
    }

    /**
     * Reinitializes the option bar buttons on the contained elements.<p>
     */
    public abstract void reinitializeButtons();

    /**
     * Shows the editor pop-up.<p>
     */
    public void showPopup() {

        m_editorDialog.getElement().getStyle().clearDisplay();
    }

    /**
     * Updates the backup elements.<p>
     *
     * @param updateElements the updated element data
     */
    public void updateBackupElements(Map<String, CmsContainerElementData> updateElements) {

        ArrayList<CmsContainerPageElementPanel> updatedList = new ArrayList<CmsContainerPageElementPanel>();
        String containerId = m_groupContainer.getContainerId();
        for (CmsContainerPageElementPanel element : m_backUpElements) {
            if (updateElements.containsKey(element.getId())
                && CmsStringUtil.isNotEmptyOrWhitespaceOnly(
                    updateElements.get(element.getId()).getContents().get(containerId))) {
                CmsContainerElementData elementData = updateElements.get(element.getId());
                try {
                    CmsContainerPageElementPanel replacer = m_controller.getContainerpageUtil().createElement(
                        elementData,
                        m_groupContainer,
                        false);
                    if (element.getInheritanceInfo() != null) {
                        // in case of inheritance container editing, keep the inheritance info
                        replacer.setInheritanceInfo(element.getInheritanceInfo());
                    }
                    updatedList.add(replacer);

                } catch (Exception e) {
                    // in this case keep the old version
                    updatedList.add(element);
                }
            } else {
                updatedList.add(element);
            }
        }
        m_backUpElements = updatedList;
    }

    /**
     * Adds a button to the dialog.<p>
     *
     * @param button the button to add
     */
    protected void addButton(Widget button) {

        if (m_editorDialog != null) {
            m_editorDialog.addButton(button);
        }
    }

    /**
     * Adds the buttons to the dialog.<p>
     */
    protected abstract void addButtons();

    /**
     * Adds a cancel button to the dialog.<p>
     */
    protected void addCancelButton() {

        if (m_editorDialog != null) {
            m_cancelButton = new CmsPushButton();
            m_cancelButton.setText(Messages.get().key(Messages.GUI_BUTTON_CANCEL_TEXT_0));
            m_cancelButton.setUseMinWidth(true);
            m_cancelButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
            m_cancelButton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    cancelEdit();
                }
            });
            m_editorDialog.addButton(m_cancelButton);
        }
    }

    /**
     * Adds an input field with the given label to the dialog.<p>
     *
     * @param label the label
     * @param inputWidget the input widget
     */
    protected void addInputField(String label, Widget inputWidget) {

        FlowPanel row = new FlowPanel();
        row.setStyleName(I_CmsLayoutBundle.INSTANCE.groupcontainerCss().inputRow());
        CmsLabel labelWidget = new CmsLabel(label);
        labelWidget.setStyleName(I_CmsLayoutBundle.INSTANCE.groupcontainerCss().inputLabel());
        row.add(labelWidget);
        inputWidget.addStyleName(I_CmsLayoutBundle.INSTANCE.groupcontainerCss().inputBox());
        row.add(inputWidget);
        m_dialogContent.add(row);
    }

    /**
     * Adds the required input fields to the dialog.<p>
     */
    protected abstract void addInputFields();

    /**
     * Adds the save button to the dialog.<p>
     */
    protected void addSaveButton() {

        if (m_editorDialog != null) {
            m_saveButton = new CmsPushButton();
            m_saveButton.setText(Messages.get().key(Messages.GUI_BUTTON_SAVE_TEXT_0));
            m_saveButton.setUseMinWidth(true);
            m_saveButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.GREEN);
            m_saveButton.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {

                    saveEdit();
                }
            });
            m_editorDialog.addButton(m_saveButton);
        }
    }

    /**
     * On click function for cancel button.<p>
     */
    protected abstract void cancelEdit();

    /**
     * Clears the static instance reference.<p>
     */
    protected abstract void clearInstance();

    /**
     * Closes the dialog.<p>
     *
     * @param breakingUp <code>true</code> if the group container is to be removed
     */
    protected void closeDialog(boolean breakingUp) {

        m_controller.stopEditingGroupcontainer();
        m_editingPlaceholder.removeFromParent();
        m_editorDialog.hide();
        RootPanel.get().removeStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().groupcontainerEditing());
        if (!breakingUp) {
            m_groupContainer.clearEditingPlaceholder();
            Style style = m_groupContainer.getElement().getStyle();
            style.clearPosition();
            style.clearTop();
            style.clearLeft();
            style.clearZIndex();
            style.clearWidth();
            m_parentContainer.insert(m_groupContainer, m_indexPosition);
            m_groupContainer.getElementOptionBar().setVisible(true);
            if (!m_groupContainer.iterator().hasNext()) {
                // group-container is empty, mark it
                m_groupContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().emptyGroupContainer());
            }
        }
        clearInstance();
        removeFromParent();
        if (!m_controller.getData().isUseClassicEditor()) {
            for (Widget element : m_groupContainer) {
                if (element instanceof CmsContainerPageElementPanel) {
                    ((CmsContainerPageElementPanel)element).removeInlineEditor();
                }
            }
        }
        m_controller.reinitializeButtons();
        m_controller.reInitInlineEditing();
        m_controller.fireEvent(new CmsContainerpageEvent(EventType.elementEdited));
    }

    /**
     * Creates a place-holder for the group-container.<p>
     *
     * @param element the element
     *
     * @return the place-holder widget
     */
    protected Element createPlaceholder(Element element) {

        Element result = CmsDomUtil.clone(element);
        result.addClassName(I_CmsLayoutBundle.INSTANCE.containerpageCss().groupcontainerPlaceholder());
        result.getStyle().setBackgroundColor("transparent");
        return result;
    }

    /**
     * Returns the list of back up elements.<p>
     *
     * @return the back up elements
     */
    protected List<CmsContainerPageElementPanel> getBackUpElements() {

        return m_backUpElements;
    }

    /**
     * Returns the container page controller.<p>
     *
     * @return the container page controller
     */
    protected CmsContainerpageController getController() {

        return m_controller;
    }

    /**
     * Returns the ids of the contained elements and group-container itself.<p>
     *
     * @return the element ids
     */
    protected Set<String> getElementIds() {

        Set<String> subItems = new HashSet<String>();
        Iterator<Widget> it = m_groupContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElementPanel) {
                subItems.add(((CmsContainerPageElementPanel)w).getId());
            }
        }
        subItems.add(m_groupContainer.getId());
        return subItems;
    }

    /**
     * Returns the element data of the contained elements.<p>
     *
     * @return the contained elements data
     */
    protected List<CmsContainerElement> getElements() {

        List<CmsContainerElement> subItems = new ArrayList<CmsContainerElement>();
        Iterator<Widget> it = m_groupContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElementPanel) {
                CmsContainerPageElementPanel elementWidget = (CmsContainerPageElementPanel)w;
                CmsContainerElement element = new CmsContainerElement();
                element.setClientId(elementWidget.getId());
                element.setResourceType(elementWidget.getNewType());
                element.setCreateNew(elementWidget.isCreateNew());
                element.setSitePath(elementWidget.getSitePath());
                element.setNewEditorDisabled(elementWidget.isNewEditorDisabled());
                subItems.add(element);
            }
        }
        return subItems;
    }

    /**
     * Returns the group container widget index position.<p>
     *
     * @return the index position
     */
    protected int getIndexPosition() {

        return m_indexPosition;
    }

    /**
     * Returns the parent container widget.<p>
     *
     * @return the parent container widget
     */
    protected CmsContainerPageContainer getParentContainer() {

        return m_parentContainer;
    }

    /**
     * Opens the group container edit dialog.<p>
     *
     * @param dialogTitle the dialog title
     */
    protected void openDialog(String dialogTitle) {

        m_editorDialog.setCaption(dialogTitle);
        int contentHeight = m_dialogContent.getOffsetHeight();
        m_editorDialog.setMainContent(m_dialogContent);
        // position dialog and show it
        if (m_groupContainerPosition != null) {
            int lefthandSpace = m_groupContainerPosition.getLeft() - Window.getScrollLeft();
            int righthandSpace = (Window.getClientWidth() + Window.getScrollLeft())
                - (m_groupContainerPosition.getLeft() + m_groupContainerPosition.getWidth());
            int requiredWidth = CmsPopup.DEFAULT_WIDTH + 30;
            int left = m_groupContainerPosition.getLeft();
            if (requiredWidth > (righthandSpace + m_groupContainerPosition.getWidth())) {
                left = (Window.getClientWidth() + Window.getScrollLeft()) - requiredWidth;
            }
            if (left < Window.getScrollLeft()) {
                left = 0;
            }
            if (lefthandSpace > requiredWidth) {
                // place left of the group container if there is enough space
                m_editorDialog.setPopupPosition(
                    m_groupContainerPosition.getLeft() - requiredWidth,
                    m_groupContainerPosition.getTop() - 1);
            } else if ((m_groupContainerPosition.getTop() - Window.getScrollTop()) > (contentHeight
                + DIALOG_BASE_HEIGHT
                + 50)) {
                // else place above if there is enough space

                m_editorDialog.setPopupPosition(
                    left,
                    m_groupContainerPosition.getTop() - (contentHeight + DIALOG_BASE_HEIGHT));
            } else if (righthandSpace > requiredWidth) {
                // else on the right if there is enough space
                m_editorDialog.setPopupPosition(
                    m_groupContainerPosition.getLeft() + m_groupContainerPosition.getWidth() + 20,
                    m_groupContainerPosition.getTop() - 1);
            } else {
                // last resort, place below
                m_editorDialog.setPopupPosition(
                    left,
                    m_groupContainerPosition.getTop() + m_groupContainerPosition.getHeight() + 20);
            }
            m_editorDialog.show();
        } else {
            // should never happen
            m_editorDialog.center();
        }
        if (!m_controller.getData().isUseClassicEditor()) {
            for (Widget element : m_groupContainer) {
                if (element instanceof CmsContainerPageElementPanel) {
                    ((CmsContainerPageElementPanel)element).initInlineEditor(m_controller);
                }
            }
        }
    }

    /**
     * Removes all child container elements.<p>
     */
    protected void removeAllChildren() {

        for (int i = getGroupContainerWidget().getWidgetCount() - 1; i >= 0; i--) {
            Widget widget = getGroupContainerWidget().getWidget(i);
            if (widget instanceof CmsContainerPageElementPanel) {
                widget.removeFromParent();
            }
        }
    }

    /**
     * On click function for save button.<p>
     */
    protected abstract void saveEdit();

    /**
     * Enables or disables the save button.<p>
     *
     * @param enabled <code>true</code> to enable the save button
     * @param disabledMessage the message to display when the button is disabled
     */
    protected void setSaveEnabled(boolean enabled, String disabledMessage) {

        if (m_saveButton != null) {
            if (enabled) {
                m_saveButton.enable();
            } else {
                m_saveButton.disable(disabledMessage);
            }
        }
    }
}

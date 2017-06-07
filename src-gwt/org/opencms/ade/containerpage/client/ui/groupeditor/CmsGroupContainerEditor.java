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
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElementPanel;
import org.opencms.ade.containerpage.client.ui.CmsGroupContainerElementPanel;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The group-container editor.<p>
 *
 * @since 8.0.0
 */
public final class CmsGroupContainerEditor extends A_CmsGroupEditor {

    /** The editor instance. */
    private static CmsGroupContainerEditor INSTANCE;

    /** The button to break up the container. */
    private CmsPushButton m_breakUpButton;

    /** The group container bean. */
    private CmsGroupContainer m_groupContainerBean;

    /** The description input. */
    private CmsTextBox m_inputDescription;

    /** The title input. */
    private CmsTextBox m_inputTitle;

    /**
     * Constructor.<p>
     *
     * @param groupContainer the group container widget
     * @param controller the container page controller
     * @param handler the container page handler
     */
    private CmsGroupContainerEditor(
        CmsGroupContainerElementPanel groupContainer,
        CmsContainerpageController controller,
        CmsContainerpageHandler handler) {

        super(groupContainer, controller, handler);
        // Loading data of all contained elements including group-container element
        getController().getElements(getElementIds(), new I_CmsSimpleCallback<Map<String, CmsContainerElementData>>() {

            public void execute(Map<String, CmsContainerElementData> arg) {

                setGroupContainerData(arg);
            }
        });
    }

    /**
     * Gets the editor instance.<p>
     *
     * @return the editor instance
     */
    public static CmsGroupContainerEditor getInstance() {

        return INSTANCE;
    }

    /**
     * Returns true if the editor is active.<p>
     *
     * @return true if the editor is active
     */
    public static boolean isActive() {

        return INSTANCE != null;
    }

    /**
     * Opens the group container editor.<p>
     *
     * @param groupContainer the group container
     * @param controller the container page controller
     * @param handler the container page handler
     *
     * @return the editor instance
     */
    public static CmsGroupContainerEditor openGroupcontainerEditor(
        CmsGroupContainerElementPanel groupContainer,
        CmsContainerpageController controller,
        CmsContainerpageHandler handler) {

        // making sure only a single instance of the group-container editor is open
        if (INSTANCE != null) {
            CmsDebugLog.getInstance().printLine("group-container editor already open");
        } else {
            CmsGroupContainerEditor editor = new CmsGroupContainerEditor(groupContainer, controller, handler);
            RootPanel.get().add(editor);
            editor.openDialog(Messages.get().key(Messages.GUI_GROUPCONTAINER_CAPTION_0));
            editor.getGroupContainerWidget().refreshHighlighting();
            INSTANCE = editor;
        }
        return INSTANCE;
    }

    /**
     * Clears the instance reference.<p>
     */
    private static void clear() {

        INSTANCE = null;
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.groupeditor.A_CmsGroupEditor#reinitializeButtons()
     */
    @Override
    public void reinitializeButtons() {

        for (Widget widget : getGroupContainerWidget()) {
            if (widget instanceof CmsContainerPageElementPanel) {
                CmsContainerPageElementPanel elemWidget = (CmsContainerPageElementPanel)widget;
                if (getController().requiresOptionBar(elemWidget, elemWidget.getParentTarget())) {
                    getController().getContainerpageUtil().addOptionBar(elemWidget);
                } else {
                    // otherwise remove any present option bar
                    elemWidget.setElementOptionBar(null);
                }
            }
        }
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.groupeditor.A_CmsGroupEditor#addButtons()
     */
    @Override
    protected void addButtons() {

        addCancelButton();
        addSaveButton();
        m_breakUpButton = new CmsPushButton();
        m_breakUpButton.setText(Messages.get().key(Messages.GUI_BUTTON_BREAK_UP_TEXT_0));
        m_breakUpButton.setUseMinWidth(true);
        m_breakUpButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        m_breakUpButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                breakUpContainer();
            }
        });
        m_breakUpButton.getElement().getStyle().setFloat(Float.LEFT);
        m_breakUpButton.getElement().getStyle().setMarginLeft(0, Unit.PX);
        addButton(m_breakUpButton);

    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.groupeditor.A_CmsGroupEditor#addInputFields()
     */
    @Override
    protected void addInputFields() {

        m_inputTitle = new CmsTextBox();
        addInputField(Messages.get().key(Messages.GUI_GROUPCONTAINER_LABEL_TITLE_0), m_inputTitle);
        m_inputDescription = new CmsTextBox();
        addInputField(Messages.get().key(Messages.GUI_GROUPCONTAINER_LABEL_DESCRIPTION_0), m_inputDescription);
    }

    /**
     * Breaks up the group container inserting it's elements into the parent container instead.<p>
     */
    protected void breakUpContainer() {

        final List<CmsContainerElement> elements = getElements();
        if (elements.isEmpty()) {
            closeDialog(true);
            getController().setPageChanged();
            return;
        }
        Set<String> elementIds = new HashSet<String>();
        for (CmsContainerElement element : elements) {
            elementIds.add(element.getClientId());
        }
        I_CmsSimpleCallback<Map<String, CmsContainerElementData>> callback = new I_CmsSimpleCallback<Map<String, CmsContainerElementData>>() {

            public void execute(Map<String, CmsContainerElementData> elementsData) {

                breakUpContainer(elements, elementsData);
            }
        };
        getController().getElements(elementIds, callback);
    }

    /**
     * Breaks up the group container inserting the given elements into the parent container instead.<p>
     *
     * @param elements the group container elements
     * @param elementsData the elements data
     */
    protected void breakUpContainer(
        List<CmsContainerElement> elements,
        Map<String, CmsContainerElementData> elementsData) {

        int index = getIndexPosition();
        for (CmsContainerElement element : elements) {
            try {
                CmsContainerPageElementPanel containerElement = getController().getContainerpageUtil().createElement(
                    elementsData.get(element.getClientId()),
                    getParentContainer(),
                    false);
                getParentContainer().insert(containerElement, index);
                index++;
            } catch (Exception e) {
                CmsDebugLog.getInstance().printLine(e.getMessage());
            }
        }
        getController().addToRecentList(m_groupContainerBean.getClientId(), null);
        closeDialog(true);
        getController().unlockResource(
            new CmsUUID(CmsContainerpageController.getServerId(m_groupContainerBean.getClientId())));
        getController().setPageChanged();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.groupeditor.A_CmsGroupEditor#cancelEdit()
     */
    @Override
    protected void cancelEdit() {

        removeAllChildren();
        for (CmsContainerPageElementPanel element : getBackUpElements()) {
            getGroupContainerWidget().add(element);
        }
        if (getBackUpElements().size() == 0) {
            getGroupContainerWidget().addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().emptyGroupContainer());
        }
        getController().unlockResource(
            new CmsUUID(CmsContainerpageController.getServerId(m_groupContainerBean.getClientId())));
        closeDialog(false);
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.groupeditor.A_CmsGroupEditor#clearInstance()
     */
    @Override
    protected void clearInstance() {

        clear();
    }

    /**
     * @see org.opencms.ade.containerpage.client.ui.groupeditor.A_CmsGroupEditor#saveEdit()
     */
    @Override
    protected void saveEdit() {

        m_groupContainerBean.setTitle(m_inputTitle.getFormValueAsString());
        m_groupContainerBean.setDescription(m_inputDescription.getFormValueAsString());
        m_groupContainerBean.setElements(getElements());
        getController().saveGroupcontainer(m_groupContainerBean, getGroupContainerWidget());
        closeDialog(false);
    }

    /**
     * Sets the data of the group-container to edit.<p>
     *
     * @param elementsData the data of all contained elements and the group-container itself
     */
    protected void setGroupContainerData(Map<String, CmsContainerElementData> elementsData) {

        m_elementData = elementsData.get(getGroupContainerWidget().getId());
        if (m_elementData != null) {
            setSaveEnabled(true, null);
            m_groupContainerBean = new CmsGroupContainer();
            m_groupContainerBean.setClientId(m_elementData.getClientId());
            m_groupContainerBean.setResourceType(getGroupContainerWidget().getNewType());
            m_groupContainerBean.setNew(getGroupContainerWidget().isNew());
            m_groupContainerBean.setSitePath(m_elementData.getSitePath());
            if (m_elementData.getTypes().isEmpty()) {
                Set<String> types = new HashSet<String>();
                types.add(((CmsContainerPageContainer)getGroupContainerWidget().getParentTarget()).getContainerType());
                m_elementData.setTypes(types);
                m_groupContainerBean.setTypes(types);
            } else {
                m_groupContainerBean.setTypes(m_elementData.getTypes());
            }
            m_inputDescription.setFormValueAsString(m_elementData.getDescription());
            m_inputTitle.setFormValueAsString(m_elementData.getTitle());
            m_groupContainerBean.setTitle(m_elementData.getTitle());
            m_groupContainerBean.setDescription(m_elementData.getDescription());
        } else {
            CmsDebugLog.getInstance().printLine("Loading groupcontainer error.");
        }
    }
}

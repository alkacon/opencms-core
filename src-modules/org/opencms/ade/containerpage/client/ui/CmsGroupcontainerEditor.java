/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsGroupcontainerEditor.java,v $
 * Date   : $Date: 2011/04/05 13:08:45 $
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

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The group-container editor.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public final class CmsGroupcontainerEditor extends Composite {

    /** The ui-binder interface for this widget. */
    interface I_CmsGroupcontainerEditorUiBinder extends UiBinder<HTMLPanel, CmsGroupcontainerEditor> {
        // GWT interface, nothing to do here
    }

    private static CmsGroupcontainerEditor INSTANCE;

    /** The ui-binder for this widget. */
    private static I_CmsGroupcontainerEditorUiBinder uiBinder = GWT.create(I_CmsGroupcontainerEditorUiBinder.class);

    /** The cancel button. */
    @UiField
    protected CmsPushButton m_cancelButton;

    /** The dialog element. */
    @UiField
    protected DivElement m_dialog;

    /** The save button. */
    @UiField
    protected CmsPushButton m_saveButton;

    /** The title label. */
    @UiField
    protected CmsLabel m_labelTitle;

    /** The descriptionLabel. */
    @UiField
    protected CmsLabel m_labelDescription;

    /** The title input. */
    @UiField
    protected CmsTextBox m_inputTitle;

    /** The description input. */
    @UiField
    protected CmsTextBox m_inputDescription;

    /** List of elements when editing started, use to restore on cancel. */
    private List<CmsContainerPageElement> m_backUpElements;

    /** The container-page controller. */
    private CmsContainerpageController m_controller;

    /** The group-container place-holder. */
    private Element m_editingPlaceholder;

    /** The editor HTML-id. */
    private String m_editorId;

    /** The editor widget. */
    private HTMLPanel m_editorWidget;

    /** The index position of the group-container inside it's parent. */
    private int m_indexPosition;

    /** The parent container. */
    private CmsContainerPageContainer m_parentContainer;

    /** The group-container. */
    private CmsGroupContainerElement m_groupContainer;

    private CmsGroupContainer m_groupContainerBean;

    /**
     * Constructor.<p>
     * 
     * @param groupContainer the group-container
     * @param controller the container-page controller
     * @param handler the container-page handler
     */
    private CmsGroupcontainerEditor(
        CmsGroupContainerElement groupContainer,
        CmsContainerpageController controller,
        CmsContainerpageHandler handler) {

        m_controller = controller;
        m_editorWidget = uiBinder.createAndBindUi(this);
        initWidget(m_editorWidget);
        m_labelDescription.setText("Description");
        m_labelTitle.setText("Title");
        m_editorId = HTMLPanel.createUniqueId();
        m_editorWidget.getElement().setId(m_editorId);
        m_groupContainer = groupContainer;
        m_backUpElements = new ArrayList<CmsContainerPageElement>();
        Iterator<Widget> it = m_groupContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElement) {
                m_backUpElements.add((CmsContainerPageElement)w);
            }
        }
        m_parentContainer = (CmsContainerPageContainer)m_groupContainer.getParentTarget();
        CmsPositionBean position = CmsPositionBean.generatePositionInfo(m_groupContainer);
        m_editingPlaceholder = createPlaceholder(m_groupContainer.getElement());
        m_groupContainer.setEditingPlaceholder(m_editingPlaceholder);
        m_indexPosition = m_parentContainer.getWidgetIndex(m_groupContainer);
        // inserting placeholder element
        m_parentContainer.getElement().insertBefore(m_editingPlaceholder, m_groupContainer.getElement());
        m_editorWidget.add(m_groupContainer, m_editorId);
        Style style = m_groupContainer.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setLeft(position.getLeft(), Unit.PX);
        style.setTop(position.getTop(), Unit.PX);
        style.setWidth(position.getWidth(), Unit.PX);
        style.setZIndex(1000);
        setDialogPosition(position);
        m_groupContainer.getElementOptionBar().setVisible(false);
        m_groupContainer.getElementOptionBar().removeStyleName(
            I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().cmsHovering());

        m_saveButton.setTitle("Save");
        m_saveButton.setText("Save");
        m_saveButton.disable("loading ...");
        m_cancelButton.setTitle("Cancel");
        m_cancelButton.setText("Cancel");

        RootPanel.get().addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().groupcontainerEditing());

        // Loading data of all contained elements including group-container element
        m_controller.getElements(getElementIds(), new I_CmsSimpleCallback<Map<String, CmsContainerElementData>>() {

            public void execute(Map<String, CmsContainerElementData> arg) {

                setGroupContainerData(arg);
            }
        });
    }

    /**
     * Opens the group-container editor.<p>
     * 
     * @param groupContainer the group-container
     * @param controller the container-page controller
     * @param handler the container-page handler
     */
    public static void openGroupcontainerEditor(
        CmsGroupContainerElement groupContainer,
        CmsContainerpageController controller,
        CmsContainerpageHandler handler) {

        // making sure only a single instance of the group-container editor is open
        if (INSTANCE != null) {
            CmsDebugLog.getInstance().printLine("group-container editor already open");
        } else {
            if (controller.startEditingGroupcontainer(groupContainer)) {
                INSTANCE = new CmsGroupcontainerEditor(groupContainer, controller, handler);
                RootPanel.get().add(INSTANCE);
            }
        }
    }

    /**
     * On click function for cancel button.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_cancelButton")
    protected void cancelEdit(ClickEvent event) {

        Iterator<Widget> it = m_groupContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElement) {
                w.removeFromParent();
            }
        }
        for (CmsContainerPageElement element : m_backUpElements) {
            m_groupContainer.add(element);
        }

        closeDialog();
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
     * On click function for save button.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_saveButton")
    protected void saveEdit(ClickEvent event) {

        m_groupContainerBean.setTitle(m_inputTitle.getFormValueAsString());
        m_groupContainerBean.setDescription(m_inputDescription.getFormValueAsString());
        m_groupContainerBean.setElements(getElements());
        m_controller.saveGroupcontainer(m_groupContainerBean, m_groupContainer);
        closeDialog();
    }

    /**
     * Sets the data of the group-container to edit.<p>
     * 
     * @param elementsData the data of all contained elements and the group-container itself
     */
    protected void setGroupContainerData(Map<String, CmsContainerElementData> elementsData) {

        CmsContainerElementData elementData = elementsData.get(m_groupContainer.getId());
        if (elementData != null) {
            m_saveButton.enable();
            m_groupContainerBean = new CmsGroupContainer();
            m_groupContainerBean.setClientId(elementData.getClientId());
            m_groupContainerBean.setResourceType(m_groupContainer.getNewType());
            m_groupContainerBean.setNew(m_groupContainer.isNew());
            m_groupContainerBean.setSitePath(elementData.getSitePath());
            if (elementData.getTypes().isEmpty()) {
                Set<String> types = new HashSet<String>();
                types.add(((CmsContainerPageContainer)m_groupContainer.getParentTarget()).getContainerType());
                elementData.setTypes(types);
                m_groupContainerBean.setTypes(types);
            } else {
                m_groupContainerBean.setTypes(elementData.getTypes());
            }
            m_inputDescription.setFormValueAsString(elementData.getDescription());
            m_inputTitle.setFormValueAsString(elementData.getTitle());
            m_groupContainerBean.setTitle(elementData.getTitle());
            m_groupContainerBean.setDescription(elementData.getDescription());
        } else {
            CmsDebugLog.getInstance().printLine("Loading groupcontainer error.");
        }
    }

    /**
     * Closes the dialog.<p>
     */
    private void closeDialog() {

        m_controller.stopEditingGroupcontainer();
        m_groupContainer.clearEditingPlaceholder();
        m_editingPlaceholder.removeFromParent();
        Style style = m_groupContainer.getElement().getStyle();
        style.clearPosition();
        style.clearTop();
        style.clearLeft();
        style.clearZIndex();
        style.clearWidth();
        m_parentContainer.insert(m_groupContainer, m_indexPosition);
        RootPanel.get().removeStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().groupcontainerEditing());
        m_groupContainer.getElementOptionBar().setVisible(true);
        if (!m_groupContainer.iterator().hasNext()) {
            // group-container is empty, mark it
            m_groupContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().emptyGroupContainer());
        }
        INSTANCE = null;
        this.removeFromParent();
    }

    /**
     * Returns the ids of the contained elements and group-container itself.<p>
     * 
     * @return the element ids
     */
    private Set<String> getElementIds() {

        Set<String> subItems = new HashSet<String>();
        Iterator<Widget> it = m_groupContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElement) {
                subItems.add(((CmsContainerPageElement)w).getId());
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
    private List<CmsContainerElement> getElements() {

        List<CmsContainerElement> subItems = new ArrayList<CmsContainerElement>();
        Iterator<Widget> it = m_groupContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElement) {
                subItems.add(m_controller.getCachedElement(((CmsContainerPageElement)w).getId()));
            }
        }
        return subItems;
    }

    private void setDialogPosition(CmsPositionBean position) {

        m_dialog.getStyle().setLeft(position.getLeft() + position.getWidth() + 20, Unit.PX);
        m_dialog.getStyle().setTop(position.getTop(), Unit.PX);
        m_dialog.getStyle().setPosition(Position.ABSOLUTE);
    }

}

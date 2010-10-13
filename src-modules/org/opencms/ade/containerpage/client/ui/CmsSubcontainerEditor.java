/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsSubcontainerEditor.java,v $
 * Date   : $Date: 2010/10/13 12:53:49 $
 * Version: $Revision: 1.4 $
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
import org.opencms.ade.containerpage.shared.CmsSubContainer;
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
 * The sub-container editor.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public final class CmsSubcontainerEditor extends Composite {

    /** The ui-binder interface for this widget. */
    interface I_CmsSubcontainerEditorUiBinder extends UiBinder<HTMLPanel, CmsSubcontainerEditor> {
        // GWT interface, nothing to do here
    }

    private static CmsSubcontainerEditor INSTANCE;

    /** The ui-binder for this widget. */
    private static I_CmsSubcontainerEditorUiBinder uiBinder = GWT.create(I_CmsSubcontainerEditorUiBinder.class);

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

    /** The sub-container place-holder. */
    private Element m_editingPlaceholder;

    /** The editor HTML-id. */
    private String m_editorId;

    /** The editor widget. */
    private HTMLPanel m_editorWidget;

    /** The index position of the sub-container inside it's parent. */
    private int m_indexPosition;

    /** The parent container. */
    private CmsContainerPageContainer m_parentContainer;

    /** The sub-container. */
    private CmsSubContainerElement m_subContainer;

    private CmsSubContainer m_subContainerBean;

    /**
     * Constructor.<p>
     * 
     * @param subContainer the sub-container
     * @param controller the container-page controller
     * @param handler the container-page handler
     */
    private CmsSubcontainerEditor(
        CmsSubContainerElement subContainer,
        CmsContainerpageController controller,
        CmsContainerpageHandler handler) {

        m_controller = controller;
        m_editorWidget = uiBinder.createAndBindUi(this);
        initWidget(m_editorWidget);
        m_labelDescription.setText("Description");
        m_labelTitle.setText("Title");
        m_editorId = HTMLPanel.createUniqueId();
        m_editorWidget.getElement().setId(m_editorId);
        m_subContainer = subContainer;
        m_backUpElements = new ArrayList<CmsContainerPageElement>();
        Iterator<Widget> it = m_subContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElement) {
                m_backUpElements.add((CmsContainerPageElement)w);
            }
        }
        m_parentContainer = (CmsContainerPageContainer)m_subContainer.getParentTarget();
        CmsPositionBean position = CmsPositionBean.generatePositionInfo(m_subContainer);
        m_editingPlaceholder = createPlaceholder(m_subContainer.getElement());
        m_subContainer.setEditingPlaceholder(m_editingPlaceholder);
        m_indexPosition = m_parentContainer.getWidgetIndex(m_subContainer);
        // inserting placeholder element
        m_parentContainer.getElement().insertBefore(m_editingPlaceholder, m_subContainer.getElement());
        m_editorWidget.add(m_subContainer, m_editorId);
        Style style = m_subContainer.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setLeft(position.getLeft(), Unit.PX);
        style.setTop(position.getTop(), Unit.PX);
        style.setWidth(position.getWidth(), Unit.PX);
        style.setZIndex(1000);
        setDialogPosition(position);
        m_subContainer.getElementOptionBar().setVisible(false);
        m_subContainer.getElementOptionBar().removeStyleName(
            I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().cmsHovering());

        m_saveButton.setTitle("Save");
        m_saveButton.setText("Save");
        m_saveButton.disable("loading ...");
        m_cancelButton.setTitle("Cancel");
        m_cancelButton.setText("Cancel");

        RootPanel.get().addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().subcontainerEditing());

        // Loading data of all contained elements including sub-container element
        m_controller.getElements(getElementIds(), new I_CmsSimpleCallback<Map<String, CmsContainerElementData>>() {

            public void execute(Map<String, CmsContainerElementData> arg) {

                setSubContainerData(arg);
            }

            public void onError(String message) {

                // TODO: Auto-generated method stub

            }
        });
    }

    /**
     * Opens the sub-container editor.<p>
     * 
     * @param subContainer the sub-container
     * @param controller the container-page controller
     * @param handler the container-page handler
     */
    public static void openSubcontainerEditor(
        CmsSubContainerElement subContainer,
        CmsContainerpageController controller,
        CmsContainerpageHandler handler) {

        // making sure only a single instance of the sub-container editor is open
        if (INSTANCE != null) {
            CmsDebugLog.getInstance().printLine("sub-container editor already open");
        } else {
            if (controller.startEditingSubcontainer(subContainer)) {
                INSTANCE = new CmsSubcontainerEditor(subContainer, controller, handler);
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

        Iterator<Widget> it = m_subContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElement) {
                w.removeFromParent();
            }
        }
        for (CmsContainerPageElement element : m_backUpElements) {
            m_subContainer.add(element);
        }

        closeDialog();
    }

    /**
     * Creates a place-holder for the sub-container.<p>
     * 
     * @param element the element
     * 
     * @return the place-holder widget
     */
    protected Element createPlaceholder(Element element) {

        Element result = CmsDomUtil.clone(element);
        result.addClassName(I_CmsLayoutBundle.INSTANCE.containerpageCss().subcontainerPlaceholder());
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

        m_subContainerBean.setTitle(m_inputTitle.getFormValueAsString());
        m_subContainerBean.setDescription(m_inputDescription.getFormValueAsString());
        m_subContainerBean.setElements(getElements());
        m_controller.saveSubcontainer(m_subContainerBean, m_subContainer);
        closeDialog();
    }

    /**
     * Sets the data of the sub-container to edit.<p>
     * 
     * @param elementsData the data of all contained elements and the sub-container itself
     */
    protected void setSubContainerData(Map<String, CmsContainerElementData> elementsData) {

        CmsContainerElementData elementData = elementsData.get(m_subContainer.getId());
        if (elementData != null) {
            m_saveButton.enable();
            m_subContainerBean = new CmsSubContainer();
            m_subContainerBean.setClientId(elementData.getClientId());
            m_subContainerBean.setNewType(m_subContainer.getNewType());
            m_subContainerBean.setSitePath(elementData.getSitePath());
            if (elementData.getTypes().isEmpty()) {
                Set<String> types = new HashSet<String>();
                types.add(((CmsContainerPageContainer)m_subContainer.getParentTarget()).getContainerType());
                elementData.setTypes(types);
                m_subContainerBean.setTypes(types);
            } else {
                m_subContainerBean.setTypes(elementData.getTypes());
            }
            m_inputDescription.setFormValueAsString(elementData.getDescription());
            m_inputTitle.setFormValueAsString(elementData.getTitle());
            m_subContainerBean.setTitle(elementData.getTitle());
            m_subContainerBean.setDescription(elementData.getDescription());
        } else {
            CmsDebugLog.getInstance().printLine("Loading subcontainer error.");
        }
    }

    /**
     * Closes the dialog.<p>
     */
    private void closeDialog() {

        m_controller.stopEditingSubcontainer();
        m_subContainer.clearEditingPlaceholder();
        m_editingPlaceholder.removeFromParent();
        Style style = m_subContainer.getElement().getStyle();
        style.clearPosition();
        style.clearTop();
        style.clearLeft();
        style.clearZIndex();
        style.clearWidth();
        m_parentContainer.insert(m_subContainer, m_indexPosition);
        RootPanel.get().removeStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().subcontainerEditing());
        m_subContainer.getElementOptionBar().setVisible(true);
        if (!m_subContainer.iterator().hasNext()) {
            // sub-container is empty, mark it
            m_subContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().emptySubContainer());
        }
        INSTANCE = null;
        this.removeFromParent();
    }

    /**
     * Returns the ids of the contained elements and sub-container itself.<p>
     * 
     * @return the element ids
     */
    private Set<String> getElementIds() {

        Set<String> subItems = new HashSet<String>();
        Iterator<Widget> it = m_subContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElement) {
                subItems.add(((CmsContainerPageElement)w).getId());
            }
        }
        subItems.add(m_subContainer.getId());
        return subItems;
    }

    /**
     * Returns the element data of the contained elements.<p>
     * 
     * @return the contained elements data
     */
    private List<CmsContainerElement> getElements() {

        List<CmsContainerElement> subItems = new ArrayList<CmsContainerElement>();
        Iterator<Widget> it = m_subContainer.iterator();
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

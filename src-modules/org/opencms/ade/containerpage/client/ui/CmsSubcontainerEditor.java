/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsSubcontainerEditor.java,v $
 * Date   : $Date: 2010/09/30 13:32:25 $
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
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsPositionBean;

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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The sub-container editor.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
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

    /** The container-page controller. */
    private CmsContainerpageController m_controller;

    /** The editor HTML-id. */
    private String m_editorId;

    /** The editor widget. */
    private HTMLPanel m_editorWidget;

    /** The sub-container place-holder. */
    private Widget m_placeholder;

    /** The sub-container. */
    private CmsSubContainerElement m_subContainer;

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
        m_editorId = HTMLPanel.createUniqueId();
        m_editorWidget.getElement().setId(m_editorId);
        m_subContainer = subContainer;
        CmsPositionBean position = CmsPositionBean.generatePositionInfo(m_subContainer);
        m_placeholder = createPlaceholder(m_subContainer.getElement());
        //        m_subContainer.getDragParent().insert(
        //            m_placeholder,
        //            m_subContainer.getDragParent().getWidgetIndex(m_subContainer));
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
        //        m_subContainer.setPlaceholder(m_placeholder);
        m_saveButton.setTitle("Save");
        m_saveButton.setText("Save");
        m_cancelButton.setTitle("Cancel");
        m_cancelButton.setText("Cancel");
        m_controller.startEditingSubcontainer(subContainer);
        RootPanel.get().addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().subcontainerEditing());
        initWidget(m_editorWidget);
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
            INSTANCE = new CmsSubcontainerEditor(subContainer, controller, handler);
            RootPanel.get().add(INSTANCE);
        }
    }

    /**
     * On click function for cancel button.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_cancelButton")
    protected void cancelEdit(ClickEvent event) {

        CmsDebugLog.getInstance().printLine("Should restore and cancel");
        closeDialog();
    }

    /**
     * Creates a place-holder for the sub-container.<p>
     * 
     * @param element the element
     * 
     * @return the place-holder widget
     */
    protected Widget createPlaceholder(Element element) {

        Widget result = new HTML(element.getInnerHTML());
        result.addStyleName(element.getClassName());
        result.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().subcontainerPlaceholder());
        return result;
    }

    /**
     * On click function for save button.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_saveButton")
    protected void saveEdit(ClickEvent event) {

        CmsDebugLog.getInstance().printLine("Should save");
        closeDialog();
    }

    /**
     * Closes the dialog.<p>
     */
    private void closeDialog() {

        m_controller.stopEditingSubcontainer();
        Style style = m_subContainer.getElement().getStyle();
        style.clearPosition();
        style.clearTop();
        style.clearLeft();
        style.clearZIndex();
        style.clearWidth();
        //        m_subContainer.getDragParent().insert(
        //            m_subContainer,
        //            m_subContainer.getDragParent().getWidgetIndex(m_placeholder));
        //        m_subContainer.setPlaceholder(null);
        m_placeholder.removeFromParent();
        RootPanel.get().removeStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().subcontainerEditing());
        m_subContainer.getElementOptionBar().setVisible(true);
        INSTANCE = null;
        this.removeFromParent();
    }

    private void setDialogPosition(CmsPositionBean position) {

        m_dialog.getStyle().setLeft(position.getLeft() + position.getWidth() + 20, Unit.PX);
        m_dialog.getStyle().setTop(position.getTop(), Unit.PX);
        m_dialog.getStyle().setPosition(Position.ABSOLUTE);
    }

}

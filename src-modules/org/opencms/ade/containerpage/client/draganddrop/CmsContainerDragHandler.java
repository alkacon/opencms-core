/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/draganddrop/Attic/CmsContainerDragHandler.java,v $
 * Date   : $Date: 2010/05/21 13:20:08 $
 * Version: $Revision: 1.26 $
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

package org.opencms.ade.containerpage.client.draganddrop;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageEditor;
import org.opencms.ade.containerpage.client.ui.CmsDraggableListItemWidget;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler;
import org.opencms.gwt.client.draganddrop.I_CmsSortableDragTarget;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The container-page editor implementation of the drag and drop handler.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.26 $
 * 
 * @since 8.0.0
 */
public class CmsContainerDragHandler
extends A_CmsSortingDragHandler<I_CmsDragContainerElement<I_CmsDragTargetContainer>, I_CmsDragTargetContainer> {

    /**
     * Bean holding info about draggable elements.<p>
     */
    protected class DragInfo {

        /** The draggable element. */
        private I_CmsDragContainerElement<I_CmsDragTargetContainer> m_draggable;

        /** The cursor offset left. */
        private int m_offsetLeft;

        /** The cursor offset top. */
        private int m_offsetTop;

        /** The elements place-holder. */
        private Widget m_placeholder1;

        /**
         * Constructor.<p>
         * 
         * @param draggable the draggable element
         * @param placeholder the elements place-holder
         * @param offsetLeft the cursor offset left
         * @param offsetTop the cursor offset top
         */
        public DragInfo(
            I_CmsDragContainerElement<I_CmsDragTargetContainer> draggable,
            Widget placeholder,
            int offsetLeft,
            int offsetTop) {

            m_draggable = draggable;
            m_placeholder1 = placeholder;
            m_offsetLeft = offsetLeft;
            m_offsetTop = offsetTop;
        }

        /**
         * Returns the draggable element.<p>
         *
         * @return the draggable element
         */
        public I_CmsDragContainerElement<I_CmsDragTargetContainer> getDraggable() {

            return m_draggable;
        }

        /**
         * Returns the offset left.<p>
         *
         * @return the offset left
         */
        public int getOffsetLeft() {

            return m_offsetLeft;
        }

        /**
         * Returns the offset top.<p>
         *
         * @return the offset top
         */
        public int getOffsetTop() {

            return m_offsetTop;
        }

        /**
         * Returns the place-holder.<p>
         * 
         * @return the place-holder
         */
        public Widget getPlaceholder() {

            return m_placeholder1;
        }
    }

    /** The container-page controller. */
    protected CmsContainerpageController m_controller;

    /** The container-page editor. */
    protected CmsContainerpageEditor m_editor;

    /** The current element info. */
    private DragInfo m_current;

    /** Flag if the drag process started from a tool-bar menu. */
    private boolean m_dragFromMenu;

    /** The element info of the favorite list drop-zone. */
    private DragInfo m_dropZoneInfo;

    /** The is new element type. */
    private String m_newType;

    /** The element info of the start element. */
    private DragInfo m_startInfo;

    /** Map of element info's. */
    private Map<I_CmsDragTargetContainer, DragInfo> m_targetInfos;

    /**
     * Constructor.<p>
     * 
     * @param controller the container-page controller
     * @param editor the container-page editor
     */
    public CmsContainerDragHandler(CmsContainerpageController controller, CmsContainerpageEditor editor) {

        m_controller = controller;
        m_editor = editor;
        m_isScrollEnabled = true;
        m_animationEnabled = true;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#createDraggableListItemWidget(org.opencms.gwt.shared.CmsListInfoBean, java.lang.String)
     */
    public CmsListItemWidget createDraggableListItemWidget(CmsListInfoBean infoBean, String id) {

        boolean isDraggable = false;
        boolean isNew = false;
        String clientId = id;
        if (CmsUUID.isValidUUID(id)) {
            isDraggable = true;
        } else {
            clientId = m_controller.getNewResourceId(id);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(id)) {
                isDraggable = true;
                isNew = true;
            }
        }
        CmsDraggableListItemWidget<I_CmsDragTargetContainer> item = new CmsDraggableListItemWidget<I_CmsDragTargetContainer>(
            infoBean,
            isDraggable);
        item.setClientId(clientId);
        if (isNew) {
            item.setNewType(id);
        }
        registerMouseHandler(item);
        return item;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#getCurrentTarget()
     */
    public I_CmsDragTargetContainer getCurrentTarget() {

        return m_currentTarget;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#getDragElement()
     */
    public I_CmsDragContainerElement<I_CmsDragTargetContainer> getDragElement() {

        return m_dragElement;
    }

    /**
     * Returns the dropZoneInfo.<p>
     *
     * @return the dropZoneInfo
     */
    public DragInfo getDropZoneInfo() {

        return m_dropZoneInfo;
    }

    /**
     * Returns if the current drag process started from a tool-bar menu.<p> 
     * 
     * @return <code>true</code> if the current drag process started from a tool-bar menu
     */
    public boolean isDragFromMenu() {

        return m_dragFromMenu;
    }

    /**
     * Sets the dropZoneInfo.<p>
     *
     * @param dropZoneInfo the dropZoneInfo to set
     */
    public void setDropZoneInfo(DragInfo dropZoneInfo) {

        m_dropZoneInfo = dropZoneInfo;
    }

    /**
     * Adds a target info to the target info's map.<p>
     * 
     * @param target the drag target
     * @param info the drag info object
     */
    protected void addTargetInfo(I_CmsDragTargetContainer target, DragInfo info) {

        m_targetInfos.put(target, info);
    }

    /**
     * Creates a clone of element to be dragged around.<p>
     * 
     * @param element the element to clone
     * @param dragParent the drag parent
     * @param clientId the client id
     * 
     * @return the generated clone
     */
    protected CmsDragContainerElement createDragClone(
        com.google.gwt.user.client.Element element,
        I_CmsDragTargetContainer dragParent,
        String clientId) {

        com.google.gwt.user.client.Element elementClone = DOM.createDiv();
        elementClone.setInnerHTML(element.getInnerHTML());
        elementClone.setClassName(element.getClassName());
        CmsDragContainerElement dragElement = new CmsDragContainerElement(
            elementClone,
            dragParent,
            clientId,
            null,
            null);
        registerMouseHandler(dragElement);
        return dragElement;
    }

    /**
     * Creates a place-holder for the draggable element.<p>
     * 
     * @param element the element
     * 
     * @return the place-holder widget
     */
    protected Widget createPlaceholder(I_CmsDragContainerElement<I_CmsDragTargetContainer> element) {

        Widget result = new HTML(element.getElement().getInnerHTML());
        result.addStyleName(element.getElement().getClassName()
            + " "
            + I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());

        Element overlay = DOM.createDiv();
        overlay.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().placeholderOverlay());
        result.getElement().appendChild(overlay);

        return result;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#elementCancelAction()
     */
    @Override
    protected void elementCancelAction() {

        // nothing to do

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#elementDropAction()
     */
    @Override
    protected void elementDropAction() {

        if (((m_current != m_startInfo) || (Math.abs(m_currentTarget.getWidgetIndex((Widget)m_current.getDraggable())
            - m_currentTarget.getWidgetIndex(m_current.getPlaceholder())) > 1))
            && (m_current != m_dropZoneInfo)) {
            m_controller.addToRecentList(m_dragElement.getClientId());
            m_controller.setPageChanged();
            m_currentTarget.insert(
                (Widget)m_current.getDraggable(),
                m_currentTarget.getWidgetIndex(m_current.getPlaceholder()));
        }
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#elementEnterTargetAction()
     */
    @Override
    protected void elementEnterTargetAction() {

        m_current.getDraggable().setVisible(false);
        // hide the current place-holder if it's not the one from the initial parent target
        if (m_current != m_startInfo) {
            m_current.getPlaceholder().setVisible(false);

        }
        m_startInfo.getDraggable().getDragParent().getElement().removeClassName(
            I_CmsLayoutBundle.INSTANCE.dragdropCss().currentTarget());
        m_currentTarget.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().currentTarget());
        // if the element is dragged into a target that is not the initial parent target and not the tool-bar menu drop-zone,
        // show the overlay on the initial place-holder and place it at the start position
        // otherwise remove the overlay
        if ((m_targetInfos.get(m_currentTarget) != m_startInfo)
            && (m_targetInfos.get(m_currentTarget) != m_dropZoneInfo)) {
            m_startInfo.getPlaceholder().addStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().overlayShow());
            I_CmsSortableDragTarget orgTarget = m_startInfo.getDraggable().getDragParent();
            orgTarget.insert(m_current.getPlaceholder(), orgTarget.getWidgetIndex((Widget)m_startInfo.getDraggable()));
        } else {
            m_startInfo.getPlaceholder().removeStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().overlayShow());
        }

        m_current = m_targetInfos.get(m_currentTarget);
        positionElement();
        m_placeholder = m_current.getPlaceholder();
        m_dragElement = m_current.getDraggable();
        sortTarget();
        m_current.getDraggable().setVisible(true);
        m_current.getPlaceholder().setVisible(true);

        // update the drag target highlighting
        updateHighlighting();
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#elementLeaveTargetAction()
     */
    @Override
    protected void elementLeaveTargetAction() {

        if (m_currentTarget != m_startInfo.getDraggable().getDragParent()) {
            m_current.getDraggable().setVisible(false);
            m_current.getPlaceholder().setVisible(false);

            m_currentTarget.getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().currentTarget());
            m_startInfo.getDraggable().getDragParent().getElement().addClassName(
                I_CmsLayoutBundle.INSTANCE.dragdropCss().currentTarget());

            m_current = m_startInfo;
            positionElement();
            m_placeholder = m_current.getPlaceholder();
            m_dragElement = m_current.getDraggable();
            m_current.getDraggable().setVisible(true);
            m_placeholder.setVisible(true);
            m_placeholder.removeStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().overlayShow());
        } else {
            m_currentTarget.insert(m_placeholder, m_currentTarget.getWidgetIndex((Widget)m_startInfo.getDraggable()));
        }
        updateHighlighting();
    }

    /**
     * Returns the currently dragged new element type.<p>
     * 
     * @return the new element type
     */
    protected String getNewType() {

        return m_newType;
    }

    /**
     * Returns if currently dragged element is a new element.<p>
     * 
     * @return <code>true</code> if the currently dragged element is new
     */
    protected boolean isNew() {

        return m_newType != null;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#positionElement()
     */
    @Override
    protected void positionElement() {

        Element element = m_current.getDraggable().getElement();
        Element parentElement = (Element)element.getParentElement();
        int left = m_currentEvent.getRelativeX(parentElement) - m_current.getOffsetLeft();
        int top = m_currentEvent.getRelativeY(parentElement) - m_current.getOffsetTop();
        //        int left = m_currentEvent.getClientX() - m_current.getOffsetLeft();
        //        int top = m_currentEvent.getClientY() - m_current.getOffsetTop();
        DOM.setStyleAttribute(element, "left", left + "px");
        DOM.setStyleAttribute(element, "top", top + "px");

    }

    /**
     * Adjust the style properties of the draggable element and inserts the place-holder into the target.<p>
     * 
     * @param elementInfo the drag info object
     * @param target the drag target
     * @param setHidden if <code>true</code> the element and it's place-holder will get hidden
     */
    protected void prepareElement(DragInfo elementInfo, I_CmsDragTargetContainer target, boolean setHidden) {

        target.insert(elementInfo.getPlaceholder(), target.getWidgetIndex((Widget)elementInfo.getDraggable()));
        elementInfo.getDraggable().prepareDrag();
        if (setHidden) {
            elementInfo.getPlaceholder().setVisible(false);
            elementInfo.getDraggable().setVisible(false);
        }
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#prepareElementForDrag()
     */
    @Override
    protected void prepareElementForDrag() {

        m_newType = m_dragElement.getNewType();
        if (m_dragElement instanceof CmsDraggableListItemWidget<?>) {
            m_dragFromMenu = true;

            CmsDragTargetMenu dragParent = new CmsDragTargetMenu();
            dragParent.setWidth(m_dragElement.getElement().getOffsetWidth() + "px");
            dragParent.getElement().getStyle().setPosition(Position.ABSOLUTE);

            RootPanel.get().add(dragParent);
            m_currentTarget = dragParent;
            m_dragElement = createDragClone(m_dragElement.getElement(), dragParent, m_dragElement.getClientId());
            m_dragElement.setNewType(m_newType);
            m_dragElement.setDragParent(dragParent);
            m_placeholder = new SimplePanel();
            dragParent.add((Widget)m_dragElement);

            if (m_editor.getClipboard().isActive()) {
                m_editor.getClipboard().hideMenu();
                dragParent.getElement().getStyle().setTop(m_editor.getClipboard().getAbsoluteTop(), Unit.PX);
                dragParent.getElement().getStyle().setLeft(m_editor.getClipboard().getAbsoluteLeft(), Unit.PX);
            } else {
                m_editor.getAdd().hideMenu();
                dragParent.getElement().getStyle().setTop(m_editor.getAdd().getAbsoluteTop(), Unit.PX);
                dragParent.getElement().getStyle().setLeft(m_editor.getAdd().getAbsoluteLeft(), Unit.PX);
            }
            Document.get().getBody().addClassName(I_CmsButton.ButtonData.MOVE.getIconClass());
            DOM.setCapture(m_dragElement.getElement());
        } else {
            m_currentTarget = m_dragElement.getDragParent();
            m_dragFromMenu = false;
            m_placeholder = createPlaceholder(m_dragElement);
        }
        m_targetInfos = new HashMap<I_CmsDragTargetContainer, DragInfo>();
        m_startInfo = new DragInfo(m_dragElement, m_placeholder, m_cursorOffsetLeft, m_cursorOffsetTop);
        m_current = m_startInfo;
        m_targetInfos.put(m_currentTarget, m_current);
        prepareElement(m_current, m_currentTarget, false);
        positionElement();
        m_targets = new ArrayList<I_CmsDragTargetContainer>();
        m_targets.add(m_currentTarget);
        m_currentTarget.getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().currentTarget());
        m_currentTarget.highlightContainer();
        String clientId = m_dragElement.getClientId();
        m_controller.getElement(clientId, new I_CmsSimpleCallback<CmsContainerElementData>() {

            /**
             * Executed with the requested element data.
             * Generates drag element widgets from the contents for each available container type.<p>
             * 
             * @param arg the element data
             * 
             * @see org.opencms.gwt.client.util.I_CmsSimpleCallback#execute(Object)
             */
            public void execute(CmsContainerElementData arg) {

                if ((arg != null) && isDragging()) {
                    if (!isDragFromMenu() && !isNew()) {
                        // preparing the tool-bar menu drop-zone
                        m_editor.getClipboard().showDropzone(true);
                        I_CmsDragTargetContainer dropZone = m_editor.getClipboard().getDropzone();
                        CmsDraggableListItemWidget<I_CmsDragTargetContainer> menuItem = m_controller.getContainerpageUtil().createListWidget(
                            arg,
                            dropZone);
                        dropZone.add(menuItem);
                        DragInfo infoMenu = new DragInfo(
                            menuItem,
                            createPlaceholder(menuItem),
                            menuItem.getOffsetWidth() - 20,
                            20);
                        addTargetInfo(dropZone, infoMenu);
                        prepareElement(infoMenu, dropZone, true);
                        setDropZoneInfo(infoMenu);
                        getTargets().add(0, dropZone);
                    }
                    if (m_controller.isSubcontainerEditing()) {
                        if (isDragFromMenu()) {
                            String type = m_controller.getSubcontainerType();
                            if (arg.getContents().containsKey(type)) {
                                try {
                                    CmsDragContainerElement dragElement;
                                    if (arg.isSubContainer()) {
                                        CmsDebugLog.getInstance().printLine(
                                            "Cannot drop sub-container into subcontainer");
                                        //TODO: use notification
                                    } else {
                                        dragElement = m_controller.getContainerpageUtil().createElement(
                                            arg,
                                            m_controller.getSubcontainer(),
                                            type);
                                        dragElement.setNewType(getNewType());
                                        m_controller.getSubcontainer().add(dragElement);
                                        int offsetLeft = dragElement.getOffsetWidth() - 20;
                                        DragInfo info = new DragInfo(
                                            dragElement,
                                            createPlaceholder(dragElement),
                                            offsetLeft,
                                            20);
                                        addTargetInfo(m_controller.getSubcontainer(), info);
                                        prepareElement(info, m_controller.getSubcontainer(), true);
                                        addDragTarget(m_controller.getSubcontainer());
                                        m_controller.getSubcontainer().highlightContainer();
                                    }

                                } catch (Exception e) {
                                    CmsDebugLog.getInstance().printLine(e.getMessage());
                                }
                            } else {
                                CmsDebugLog.getInstance().printLine("No content for type " + type);
                            }
                        }
                    } else {
                        CmsDebugLog.getInstance().printLine(
                            "Loaded content for " + arg.getContents().size() + " container types");
                        Iterator<Entry<String, CmsDragTargetContainer>> it = m_controller.getContainerTargets().entrySet().iterator();
                        while (it.hasNext()) {
                            Entry<String, CmsDragTargetContainer> entry = it.next();
                            String containerType = m_controller.getContainerType(entry.getKey());
                            if (arg.getContents().containsKey(containerType)
                                && (entry.getValue() != getDragElement().getDragParent())) {
                                try {
                                    CmsDragContainerElement dragElement;
                                    if (arg.isSubContainer()) {
                                        CmsDebugLog.getInstance().printLine("Generating sub-container elements.");
                                        List<CmsContainerElementData> subElements = new ArrayList<CmsContainerElementData>();
                                        Iterator<String> itSub = arg.getSubItems().iterator();
                                        while (itSub.hasNext()) {
                                            CmsContainerElementData element = m_controller.getCachedElement(itSub.next());
                                            if (element != null) {
                                                subElements.add(element);
                                            }
                                        }
                                        dragElement = m_controller.getContainerpageUtil().createSubcontainerElement(
                                            arg,
                                            subElements,
                                            entry.getValue(),
                                            containerType);
                                        CmsDebugLog.getInstance().printLine("Sub-container created.");
                                    } else {
                                        dragElement = m_controller.getContainerpageUtil().createElement(
                                            arg,
                                            entry.getValue(),
                                            containerType);
                                    }
                                    dragElement.setNewType(getNewType());
                                    entry.getValue().add(dragElement);
                                    int offsetLeft = dragElement.getOffsetWidth() - 20;
                                    DragInfo info = new DragInfo(
                                        dragElement,
                                        createPlaceholder(dragElement),
                                        offsetLeft,
                                        20);
                                    addTargetInfo(entry.getValue(), info);
                                    prepareElement(info, entry.getValue(), true);
                                    addDragTarget(entry.getValue());
                                    entry.getValue().highlightContainer();
                                } catch (Exception e) {
                                    CmsDebugLog.getInstance().printLine(e.getMessage());
                                    continue;
                                }

                            }
                        }
                    }
                }

            }

            /**
             * @see org.opencms.gwt.client.util.I_CmsSimpleCallback#onError(java.lang.String)
             */
            public void onError(String message) {

                // nothing to do

            }
        });

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#restoreElementAfterDrag()
     */
    @Override
    protected void restoreElementAfterDrag() {

        m_editor.getClipboard().showDropzone(false);
        if (m_current.equals(getDropZoneInfo())) {
            CmsDebugLog.getInstance().printLine("Droped to menu");
            m_controller.addToFavoriteList(m_dragElement.getClientId());
            m_current = m_startInfo;
        }
        Iterator<Entry<I_CmsDragTargetContainer, DragInfo>> it = m_targetInfos.entrySet().iterator();
        while (it.hasNext()) {
            Entry<I_CmsDragTargetContainer, DragInfo> entry = it.next();
            if (entry.getValue().equals(m_current) && !(m_dragFromMenu && (m_current == m_startInfo))) {
                entry.getValue().getDraggable().clearDrag();
                entry.getValue().getPlaceholder().removeFromParent();
            } else {
                entry.getValue().getPlaceholder().removeFromParent();
                ((Widget)entry.getValue().getDraggable()).removeFromParent();
            }
            entry.getKey().getElement().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().currentTarget());
            entry.getKey().removeHighlighting();
        }
        if (m_dragFromMenu) {
            m_editor.getClipboard().setActive(false);
            m_editor.getAdd().setActive(false);
            ((Widget)m_startInfo.getDraggable().getDragParent()).removeFromParent();
            Document.get().getBody().removeClassName(I_CmsButton.ButtonData.MOVE.getIconClass());
        }
        m_targetInfos = null;
        m_current = null;
        m_startInfo = null;
        m_dropZoneInfo = null;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#targetSortChangeAction()
     */
    @Override
    protected void targetSortChangeAction() {

        updateHighlighting();

    }

    /**
     * Updates the drag target highlighting.<p>
     */
    protected void updateHighlighting() {

        Iterator<Entry<I_CmsDragTargetContainer, DragInfo>> it = m_targetInfos.entrySet().iterator();
        while (it.hasNext()) {
            it.next().getKey().refreshHighlighting();
        }
    }

}

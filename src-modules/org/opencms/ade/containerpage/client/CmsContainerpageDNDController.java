/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/Attic/CmsContainerpageDNDController.java,v $
 * Date   : $Date: 2010/09/30 13:32:25 $
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

import org.opencms.ade.containerpage.client.ui.CmsContainerPageContainer;
import org.opencms.ade.containerpage.client.ui.CmsContainerPageElement;
import org.opencms.ade.containerpage.client.ui.I_CmsDropContainer;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.I_CmsDNDController;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * The container-page editor drag and drop controller.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsContainerpageDNDController implements I_CmsDNDController {

    /**
     * Bean holding info about draggable elements.<p>
     */
    protected class DragInfo {

        /** The drag helper element. */
        private Element m_dragHelper;

        /** The cursor offset top. */
        private int m_offsetX;

        /** The cursor offset left. */
        private int m_offsetY;

        /** The placeholder element. */
        private Element m_placeholder;

        /**
         * Constructor.<p>
         * 
         * @param dragHelper the drag helper element
         * @param placeholder the elements place-holder
         * @param offsetX the cursor offset x
         * @param offsetY the cursor offset y
         */
        protected DragInfo(Element dragHelper, Element placeholder, int offsetX, int offsetY) {

            m_dragHelper = dragHelper;
            m_placeholder = placeholder;
            m_offsetX = offsetX;
            m_offsetY = offsetY;
        }

        /**
         * Returns the offset x.<p>
         *
         * @return the offset x
         */
        public int getOffsetX() {

            return m_offsetX;
        }

        /**
         * Returns the offset y.<p>
         *
         * @return the offset y
         */
        public int getOffsetY() {

            return m_offsetY;
        }

        /**
         * Returns the drag helper element.<p>
         * 
         * @return the drag helper element
         */
        protected Element getDragHelper() {

            return m_dragHelper;
        }

        /**
         * Returns the placeholder element.<p>
         * 
         * @return the placeholder element
         */
        protected Element getPlaceholder() {

            return m_placeholder;
        }
    }

    /** The container page controller. */
    private CmsContainerpageController m_controller;

    /** Map of current drag info beans. */
    private Map<I_CmsDropTarget, DragInfo> m_dragInfos;

    /** The ionitial drop target. */
    private I_CmsDropTarget m_initialDropTarget;

    /** Creating new flag. */
    private boolean m_isNew;

    /** The original position of the draggable. */
    private int m_originalIndex;

    /**
     * Constructor.<p>
     * 
     * @param controller the container page controller
     */
    public CmsContainerpageDNDController(CmsContainerpageController controller) {

        m_controller = controller;
        m_dragInfos = new HashMap<I_CmsDropTarget, DragInfo>();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onBeforeDrop(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onBeforeDrop(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        return true;
    }

    /**
    * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragCancel(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
    */
    public void onDragCancel(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        clear(handler);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragStart(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onDragStart(I_CmsDraggable draggable, I_CmsDropTarget target, final CmsDNDHandler handler) {

        m_isNew = false;
        m_originalIndex = -1;
        m_initialDropTarget = target;
        if (target != null) {
            handler.addTarget(target);
            target.getElement().getStyle().setPosition(Position.RELATIVE);
            if (target instanceof I_CmsDropContainer) {
                m_originalIndex = ((I_CmsDropContainer)target).getWidgetIndex((Widget)draggable);
                target.getElement().insertBefore(handler.getPlaceholder(), draggable.getElement());
                draggable.getElement().getStyle().setDisplay(Display.NONE);
                ((I_CmsDropContainer)target).highlightContainer();
            }
        }
        m_dragInfos.put(
            target,
            new DragInfo(
                handler.getDragHelper(),
                handler.getPlaceholder(),
                handler.getCursorOffsetX(),
                handler.getCursorOffsetY()));
        m_controller.getHandler().hideMenu();
        String clientId = draggable.getId();
        if (!CmsUUID.isValidUUID(clientId)) {
            // for new content elements dragged from the gallery menu, the given id contains the resource type name
            clientId = m_controller.getNewResourceId(clientId);
            m_isNew = true;
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(clientId)) {
                handler.cancel();
            }
        }
        m_controller.getElement(clientId, new I_CmsSimpleCallback<CmsContainerElementData>() {

            @Override
            public void execute(CmsContainerElementData arg) {

                prepareHelperElements(arg, handler);
            }

            @Override
            public void onError(String message) {

                CmsDebugLog.getInstance().printLine(message);
            }
        });
        return true;
    }

    /**
    * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDrop(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
    */
    public void onDrop(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        if (target != m_initialDropTarget) {
            if (target instanceof I_CmsDropContainer) {
                I_CmsDropContainer container = (I_CmsDropContainer)target;
                try {

                    CmsContainerPageElement containerElement = null;
                    if (m_isNew) {
                        // for new content elements dragged from the gallery menu, the given id contains the resource type name
                        containerElement = m_controller.getContainerpageUtil().createElement(
                            m_controller.getCachedElement(m_controller.getNewResourceId(draggable.getId())),
                            container);
                        containerElement.setNewType(draggable.getId());
                    } else {
                        CmsContainerElementData elementData = m_controller.getCachedElement(draggable.getId());
                        if (elementData.isSubContainer()) {
                            List<CmsContainerElementData> subElements = new ArrayList<CmsContainerElementData>();
                            for (String subId : elementData.getSubItems()) {
                                CmsContainerElementData element = m_controller.getCachedElement(subId);
                                if (element != null) {
                                    subElements.add(element);
                                }
                            }
                            containerElement = m_controller.getContainerpageUtil().createSubcontainerElement(
                                elementData,
                                subElements,
                                container);
                        } else {
                            containerElement = m_controller.getContainerpageUtil().createElement(elementData, container);
                        }
                        m_controller.addToRecentList(draggable.getId());
                    }
                    if (container.getPlaceholderIndex() >= container.getWidgetCount()) {
                        container.add(containerElement);
                    } else {
                        container.insert(containerElement, container.getPlaceholderIndex());
                    }
                    m_controller.setPageChanged();
                    if (draggable instanceof CmsContainerPageElement) {
                        ((CmsContainerPageElement)draggable).removeFromParent();
                    }
                } catch (Exception e) {
                    CmsDebugLog.getInstance().printLine(e.getMessage());
                }
            } else if (target instanceof CmsList) {
                m_controller.addToFavoriteList(draggable.getId());
            }
        } else if ((target instanceof I_CmsDropContainer)
            && (draggable instanceof CmsContainerPageElement)
            && isChangedPosition(target)) {

            I_CmsDropContainer container = (I_CmsDropContainer)target;
            int count = container.getWidgetCount();
            CmsDebugLog.getInstance().printLine("Count: " + count + ", position: " + container.getPlaceholderIndex());
            if (container.getPlaceholderIndex() >= count) {
                container.add((CmsContainerPageElement)draggable);
            } else {
                container.insert((CmsContainerPageElement)draggable, container.getPlaceholderIndex());
            }
            m_controller.addToRecentList(draggable.getId());
            m_controller.setPageChanged();
        }
        clear(handler);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onPositionedPlaceholder(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onPositionedPlaceholder(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        updateHighlighting();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetEnter(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onTargetEnter(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        DragInfo info = m_dragInfos.get(target);
        if (info != null) {
            handler.getDragHelper().getStyle().setDisplay(Display.NONE);
            handler.getPlaceholder().getStyle().setDisplay(Display.NONE);
            handler.setDragHelper(info.getDragHelper());
            handler.setPlaceholder(info.getPlaceholder());
            handler.setCursorOffsetX(info.getOffsetX());
            handler.setCursorOffsetY(info.getOffsetY());
            handler.getDragHelper().getStyle().setDisplay(Display.BLOCK);
            handler.getPlaceholder().getStyle().setDisplay(Display.BLOCK);
        }
        if (target != m_initialDropTarget) {
            draggable.getElement().getStyle().setDisplay(Display.BLOCK);
        } else {
            draggable.getElement().getStyle().setDisplay(Display.NONE);
        }
        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetLeave(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onTargetLeave(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        DragInfo info = m_dragInfos.get(m_initialDropTarget);
        if (info != null) {
            handler.getDragHelper().getStyle().setDisplay(Display.NONE);
            handler.getPlaceholder().getStyle().setDisplay(Display.NONE);
            handler.setDragHelper(info.getDragHelper());
            handler.setPlaceholder(info.getPlaceholder());
            handler.setCursorOffsetX(info.getOffsetX());
            handler.setCursorOffsetY(info.getOffsetY());
            handler.getDragHelper().getStyle().setDisplay(Display.BLOCK);
            handler.getPlaceholder().getStyle().setDisplay(Display.NONE);
        }
        draggable.getElement().getStyle().setDisplay(Display.BLOCK);
        updateHighlighting();
    }

    /**
     * Prepares all helper elements for the different drop targets.<p>
     * 
     * @param elementData the element data
     * @param handler the drag and drop handler
     */
    protected void prepareHelperElements(CmsContainerElementData elementData, CmsDNDHandler handler) {

        if (handler.getDraggable() instanceof CmsContainerPageElement) {
            CmsList<CmsListItem> dropzone = m_controller.getHandler().getDropzone();
            m_controller.getHandler().showDropzone(true);
            CmsListItem temp = m_controller.getContainerpageUtil().createListItem(elementData);

            Element placeholder = (Element)temp.getPlaceholder(dropzone);
            Element helper = (Element)temp.getDragHelper(dropzone);
            m_dragInfos.put(
                dropzone,
                new DragInfo(helper, placeholder, helper.getOffsetWidth() - 15, handler.getCursorOffsetY()));
            handler.addTarget(dropzone);
            helper.getStyle().setDisplay(Display.NONE);
        }
        for (String cId : elementData.getContents().keySet()) {
            CmsDebugLog.getInstance().printLine(cId);
        }
        for (CmsContainerPageContainer container : m_controller.getContainerTargets().values()) {

            if ((container != m_initialDropTarget) && elementData.getContents().containsKey(container.getContainerId())) {

                Element helper = null;
                Element placeholder = null;
                if (elementData.isSubContainer()) {
                    helper = DOM.createDiv();
                    String content = "";
                    for (String subId : elementData.getSubItems()) {
                        CmsContainerElementData subData = m_controller.getCachedElement(subId);
                        if ((subData != null) && subData.getContents().containsKey(container.getContainerId())) {
                            content += subData.getContents().get(container.getContainerId());
                        }
                    }
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(content)) {
                        helper.setInnerHTML(content);
                        placeholder = CmsDomUtil.clone(helper);
                    }
                } else {
                    try {
                        String htmlContent = elementData.getContents().get(container.getContainerId());
                        helper = CmsDomUtil.createElement(htmlContent);
                        placeholder = CmsDomUtil.createElement(htmlContent);
                        placeholder.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
                    } catch (Exception e) {
                        CmsDebugLog.getInstance().printLine(e.getMessage());
                    }
                }
                if (helper != null) {
                    prepareDragInfo(helper, placeholder, container, handler);
                    container.highlightContainer();
                }
            } else {
                CmsDebugLog.getInstance().printLine("No content for container: " + container.getContainerId());
            }
        }
    }

    private void clear(final CmsDNDHandler handler) {

        for (I_CmsDropTarget target : m_dragInfos.keySet()) {
            target.getElement().getStyle().clearPosition();
            m_dragInfos.get(target).getDragHelper().removeFromParent();
            if (target instanceof I_CmsDropContainer) {
                ((I_CmsDropContainer)target).removeHighlighting();
            }
        }
        m_isNew = false;
        m_controller.getHandler().showDropzone(false);
        m_controller.getHandler().deactivateMenuButton();
        m_dragInfos.clear();
        DeferredCommand.addCommand(new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                handler.clearTargets();
            }
        });
    }

    /**
     * Checks whether the current placeholder position represents a change to the original draggable position within the tree.<p>
     * 
     * @param target the current drop target
     * 
     * @return <code>true</code> if the position changed
     */
    private boolean isChangedPosition(I_CmsDropTarget target) {

        // if the new index is not next to the old one, the position has changed
        if ((target != m_initialDropTarget)
            || !((target.getPlaceholderIndex() == m_originalIndex + 1) || (target.getPlaceholderIndex() == m_originalIndex))) {
            return true;
        }
        return false;
    }

    /**
     * Sets styles of helper elements, appends the to the drop target and puts them into a drag info bean.<p>
     * 
     * @param dragHelper the drag helper element
     * @param placeholder the placeholder element
     * @param target the drop target
     * @param handler the drag and drop handler
     */
    private void prepareDragInfo(Element dragHelper, Element placeholder, I_CmsDropTarget target, CmsDNDHandler handler) {

        target.getElement().appendChild(dragHelper);
        // preparing helper styles
        int width = CmsDomUtil.getCurrentStyleInt(dragHelper, CmsDomUtil.Style.width);
        Style style = dragHelper.getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setMargin(0, Unit.PX);
        style.setWidth(width, Unit.PX);
        style.setZIndex(100);
        dragHelper.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragging());
        dragHelper.addClassName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().shadow());
        if (!CmsDomUtil.hasBackground(dragHelper)) {
            dragHelper.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBackground());
        }

        if (!CmsDomUtil.hasBorder(dragHelper)) {
            dragHelper.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragElementBorder());
        }
        style.setDisplay(Display.NONE);
        target.getElement().getStyle().setPosition(Position.RELATIVE);

        m_dragInfos.put(target, new DragInfo(dragHelper, placeholder, width - 15, handler.getCursorOffsetY()));
        handler.addTarget(target);

        // adding drag handle
        Element button = (new Image(I_CmsImageBundle.INSTANCE.moveIconActive())).getElement();
        button.addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragHandle());
        dragHelper.appendChild(button);
    }

    /**
     * Updates the drag target highlighting.<p>
     */
    private void updateHighlighting() {

        for (I_CmsDropTarget target : m_dragInfos.keySet()) {
            if (target instanceof I_CmsDropContainer) {
                ((I_CmsDropContainer)target).refreshHighlighting();
            }
        }
    }
}

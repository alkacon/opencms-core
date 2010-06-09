/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsDnDListHandler.java,v $
 * Date   : $Date: 2010/06/09 13:19:35 $
 * Version: $Revision: 1.5 $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Drag and drop handler for generic lists.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.0
 */
public class CmsDnDListHandler extends A_CmsSortingDragHandler<CmsDnDListItem, CmsDnDList<CmsDnDListItem>>
implements NativePreviewHandler {

    /** The handler manager. */
    protected HandlerManager m_handlerManager;

    /** Mouse handler registrations. */
    protected Map<CmsDnDListItem, List<HandlerRegistration>> m_handlerRegs;

    /** The key event handler registration. */
    private HandlerRegistration m_handleReg;

    /** The provisional drag parent. */
    private CmsDnDList<? extends CmsDnDListItem> m_provisionalParent;

    /** The original ID of the draggable element. */
    private String m_srcId;

    /** The source list of the draggable element. */
    private CmsDnDList<CmsDnDListItem> m_srcList;

    /** The source position of the draggable element. */
    private int m_srcPos;

    /**
     * Constructor.<p>
     */
    public CmsDnDListHandler() {

        super();
        m_handlerManager = new HandlerManager(this);
        m_animationEnabled = true;
        // the abstract generic dnd handler should not clear the targets every time!!! see #clearDrag
        m_targets = new ArrayList<CmsDnDList<CmsDnDListItem>>();
        m_handlerRegs = new HashMap<CmsDnDListItem, List<HandlerRegistration>>();
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#addDragTarget(org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    @Override
    public void addDragTarget(CmsDnDList<CmsDnDListItem> target) {

        m_targets.add(0, target);
    }

    /**
     * Adds a new list drop event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addListDropHandler(I_CmsDnDListDropHandler handler) {

        return m_handlerManager.addHandler(CmsDnDListDropEvent.getType(), handler);
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#createDraggableListItemWidget(org.opencms.gwt.shared.CmsListInfoBean, java.lang.String)
     */
    public CmsListItemWidget createDraggableListItemWidget(CmsListInfoBean infoBean, String id) {

        throw new UnsupportedOperationException();
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseDownHandler#onMouseDown(com.google.gwt.event.dom.client.MouseDownEvent)
     */
    @Override
    public void onMouseDown(MouseDownEvent event) {

        if (event.getNativeButton() != NativeEvent.BUTTON_LEFT) {
            // only act on left button down, ignore right click
            return;
        }
        try {
            m_dragElement = (CmsDnDListItem)event.getSource();
        } catch (Exception e) {
            // TODO: add logging
        }
        if ((m_dragElement == null) || !m_dragElement.isHandleEvent(event.getNativeEvent())) {
            // drag element is not listening
            return;
        }
        if (!canDragNow()) {
            m_dragElement = null;
            return;
        }

        // let's drag
        DOM.setCapture(m_dragElement.getElement());
        m_dragging = true;
        Document.get().getBody().addClassName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.dragdropCss().dragStarted());
        m_currentEvent = event;
        m_cursorOffsetLeft = m_currentEvent.getRelativeX(m_dragElement.getElement());
        m_cursorOffsetTop = m_currentEvent.getRelativeY(m_dragElement.getElement());

        prepareElementForDrag();

        positionElement();

        event.preventDefault();
        event.stopPropagation();

        // start key events handling
        m_handleReg = Event.addNativePreviewHandler(this);
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#onMouseUp(com.google.gwt.event.dom.client.MouseUpEvent)
     */
    @Override
    public void onMouseUp(MouseUpEvent event) {

        m_currentEvent = event;

        if (!m_dragging) {
            return;
        }
        if (!canDropNow()) {
            // cancel we are not allowed drop now
            cancelDragging();
            return;
        }

        switch (event.getNativeButton()) {
            case NativeEvent.BUTTON_LEFT:
                // try to execute the drop
                stopDragging(event);
                break;
            default:
                // otherwise cancel
                cancelDragging();
        }
    }

    /**
     * @see com.google.gwt.user.client.Event.NativePreviewHandler#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
     */
    public void onPreviewNativeEvent(NativePreviewEvent event) {

        Event nativeEvent;
        try {
            nativeEvent = Event.as(event.getNativeEvent());
        } catch (Exception e) {
            // sometimes in dev mode, and only in dev mode, we get
            // "Found interface com.google.gwt.user.client.Event, but class was expected"
            return;
        }
        if (event.getTypeInt() != Event.ONKEYUP) {
            return;
        }
        if (m_dragging && (nativeEvent.getKeyCode() == 27)) {
            cancelDragging();
        }
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#registerMouseHandler(org.opencms.gwt.client.draganddrop.I_CmsDragElement)
     */
    @Override
    public void registerMouseHandler(CmsDnDListItem element) {

        element.addMouseDownHandler(this);
    }

    /**
     * Removes the given target.<p>
     * 
     * @param target the target to remove
     */
    public void removeDragTarget(CmsDnDList<CmsDnDListItem> target) {

        m_targets.remove(target);
    }

    /**
     * Cancels dragging.<p>
     */
    protected void cancelDragging() {

        // put the widget back to the source position
        m_placeholder.removeFromParent();
        m_srcList.insert(m_placeholder, m_srcPos);
        // prevent drop action
        m_currentTarget = null;
        // update status and animate
        stopDragging(null);
    }

    /**
     * Checks if the current drag element can be dragged at all before starting.<p>
     * 
     * @return <code>true</code> if the current drag element can be dragged now
     */
    protected boolean canDragNow() {

        return true;
    }

    /**
     * Checks if the current dragged element can be dropped at all.<p>
     * 
     * @return <code>true</code> if the current dragged element can be dropped now
     */
    protected boolean canDropNow() {

        return true;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsDragHandler#clearDrag()
     */
    @Override
    protected void clearDrag() {

        if (m_currentTarget != null) {
            elementDropAction();
            m_currentTarget = null;
        } else {
            elementCancelAction();
        }
        restoreElementAfterDrag();
        Document.get().getBody().removeClassName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.dragdropCss().dragStarted());
        m_dragElement = null;
        m_placeholder = null;
        // the abstract generic dnd handler should not clear the targets every time!!!, see ctor
        // m_targets = null;
        m_currentTarget = null;
    }

    /**
     * Creates a clone of element to be dragged around.<p>
     * 
     * @param element the element to clone
     * @param dragParent the drag parent
     * 
     * @return the generated clone
     */
    protected CmsDnDListItem createDragClone(Element element, CmsDnDList<? extends CmsDnDListItem> dragParent) {

        Element elementClone = DOM.createDiv();
        elementClone.setInnerHTML(element.getInnerHTML());
        elementClone.setClassName(element.getClassName());
        CmsDnDListItem dragElement = new CmsDnDListItem(elementClone);
        dragElement.setDragParent(dragParent);

        // remove all decorations
        List<com.google.gwt.dom.client.Element> elems = CmsDomUtil.getElementsByClass(
            I_CmsLayoutBundle.INSTANCE.floatDecoratedPanelCss().decorationBox(),
            CmsDomUtil.Tag.div,
            dragElement.getElement());
        for (com.google.gwt.dom.client.Element elem : elems) {
            elem.removeFromParent();
        }

        registerHandlersForDrag(dragElement);

        return dragElement;
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#elementCancelAction()
     */
    @Override
    protected void elementCancelAction() {

        // nothing to do here
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#elementDropAction()
     */
    @Override
    protected void elementDropAction() {

        fireEvent(m_currentTarget, ((CmsDnDListItem)m_placeholder).getId());
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#elementEnterTargetAction()
     */
    @Override
    protected void elementEnterTargetAction() {

        // nothing to do here
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#elementLeaveTargetAction()
     */
    @Override
    protected void elementLeaveTargetAction() {

        // nothing to do
    }

    /**
     * Fires the drop event for the given target list and dropped item id.<p>
     * 
     * @param targetList the target list
     * @param droppedItemId the dropped item id
     */
    protected void fireEvent(CmsDnDList<CmsDnDListItem> targetList, String droppedItemId) {

        CmsDnDListDropEvent event = new CmsDnDListDropEvent(m_srcList, m_srcId, targetList, droppedItemId);
        m_handlerManager.fireEvent(event);
        targetList.fireDropEvent(event);
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#prepareElementForDrag()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void prepareElementForDrag() {

        m_currentTarget = (CmsDnDList<CmsDnDListItem>)m_dragElement.getDragParent();

        // keep track of the drag source
        m_srcId = m_dragElement.getId();
        m_srcList = m_currentTarget;
        m_srcPos = m_currentTarget.getWidgetIndex(m_dragElement);
        // create the drag helper
        CmsDnDListItem clone = createDragClone(m_dragElement.getElement(), m_currentTarget);

        // we append the drag element to the body to prevent any kind of issues 
        // (ie when the parent is styled with overflow:hidden)
        // and with put it additionally inside a provisional parent for parent dependent styling 
        // and we position it absolutely on the original parent for the eventual animation when releasing 
        m_provisionalParent = new CmsDnDList<CmsDnDListItem>();
        m_provisionalParent.setWidth(m_dragElement.getElement().getOffsetWidth() + "px");
        m_provisionalParent.getElement().getStyle().setPosition(Position.ABSOLUTE);
        Element listEl = m_currentTarget.getElement();
        m_provisionalParent.getElement().getStyle().setTop(listEl.getAbsoluteTop(), Unit.PX);
        m_provisionalParent.getElement().getStyle().setLeft(listEl.getAbsoluteLeft(), Unit.PX);
        m_provisionalParent.getElement().getStyle().setZIndex(99999);
        RootPanel.get().add(m_provisionalParent);

        m_placeholder = m_dragElement;
        m_placeholder.addStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
        ((CmsDnDListItem)m_placeholder).onDrag();

        m_dragElement = clone;

        // important: capture all mouse events and dispatch them to this element until released
        DOM.setCapture(m_dragElement.getElement());
        m_provisionalParent.add(m_dragElement);
        m_dragElement.prepareDrag();
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#restoreElementAfterDrag()
     */
    @Override
    protected void restoreElementAfterDrag() {

        m_placeholder.removeStyleName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragPlaceholder());
        m_provisionalParent.removeFromParent();
        m_provisionalParent = null;
        m_handleReg.removeHandler();
    }

    /**
     * Stops dragging.<p>
     * 
     * @param event the mouse up event, or <code>null</code> 
     */
    protected void stopDragging(MouseUpEvent event) {

        m_dragging = false;
        if (m_scrollTimer != null) {
            m_scrollTimer.cancel();
            m_scrollTimer = null;
        }
        List<HandlerRegistration> regList = m_handlerRegs.get(m_dragElement);
        if (regList != null) {
            for (HandlerRegistration reg : regList) {
                reg.removeHandler();
            }
            regList.clear();
        }
        DOM.releaseCapture(m_dragElement.getElement());
        if (event != null) {
            event.preventDefault();
            event.stopPropagation();
        }
        if (m_animationEnabled) {
            animateClear();
        } else {
            clearDrag();
        }
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#targetSortChangeAction()
     */
    @Override
    protected void targetSortChangeAction() {

        // nothing to do
    }

    /**
     * Registers the mouse handling on the helper for dragging.<p>
     */
    private void registerHandlersForDrag(CmsDnDListItem dragElement) {

        List<HandlerRegistration> regList = m_handlerRegs.get(dragElement);
        if (regList != null) {
            for (HandlerRegistration reg : regList) {
                reg.removeHandler();
            }
            regList.clear();
        } else {
            regList = new ArrayList<HandlerRegistration>();
            m_handlerRegs.put(dragElement, regList);
        }
        regList.add(dragElement.addMouseMoveHandler(this));
        regList.add(dragElement.addMouseUpHandler(this));
        regList.add(dragElement.addMouseOutHandler(this));
        regList.add(dragElement.addMouseOverHandler(this));
        regList.add(dragElement.addContextMenuHandler(this));
    }
}

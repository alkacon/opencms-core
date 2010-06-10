/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsDnDListHandler.java,v $
 * Date   : $Date: 2010/06/10 13:15:39 $
 * Version: $Revision: 1.7 $
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Drag and drop handler for generic lists.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 8.0.0
 */
public class CmsDnDListHandler extends A_CmsSortingDragHandler<CmsDnDListItem, CmsDnDList<CmsDnDListItem>>
implements NativePreviewHandler {

    /** The collision handler. */
    protected I_CmsDnDListCollisionResolutionHandler m_collisionHandler;

    /** The key event handler registration. */
    protected HandlerRegistration m_handleReg;

    /** The handler manager. */
    protected HandlerManager m_handlerManager;

    /** Mouse handler registrations. */
    protected Map<CmsDnDListItem, List<HandlerRegistration>> m_handlerRegs;

    /** The provisional drag parent. */
    protected CmsDnDList<? extends CmsDnDListItem> m_provisionalParent;

    /** The original ID of the draggable element. */
    protected String m_srcId;

    /** The source list of the draggable element. */
    protected CmsDnDList<CmsDnDListItem> m_srcList;

    /** The source position of the draggable element. */
    protected int m_srcPos;

    /** The status handler. */
    protected I_CmsDnDListStatusHandler m_statusHandler;

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
        m_collisionHandler = new CmsDnDListCollisionResolutionHandler();
        m_statusHandler = null;
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
        if ((m_statusHandler != null) && !m_statusHandler.canDragNow(m_dragElement)) {
            m_dragElement = null;
            return;
        }

        // let's drag
        DOM.setCapture(m_dragElement.getElement());
        m_dragging = true;
        Document.get().getBody().addClassName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.dragdropCss().dragStarted());
        storeEventPos(event);
        m_cursorOffsetLeft = getRelativeX(m_dragElement.getElement());
        m_cursorOffsetTop = getRelativeY(m_dragElement.getElement());

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

        storeEventPos(event);

        if (!m_dragging) {
            return;
        }
        if ((m_statusHandler != null) && !m_statusHandler.canDropNow(m_currentTarget, m_dragElement)) {
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
     * Sets the collision handler.<p>
     *
     * @param collisionHandler the handler to set
     */
    public void setCollisionHandler(I_CmsDnDListCollisionResolutionHandler collisionHandler) {

        m_collisionHandler = collisionHandler;
    }

    /**
     * Sets the status handler.<p>
     *
     * @param statusHandler the handler to set
     */
    public void setStatusHandler(I_CmsDnDListStatusHandler statusHandler) {

        m_statusHandler = statusHandler;
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
        Document.get().getBody().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragStarted());
        m_dragElement = null;
        m_placeholder = null;
        // the abstract generic dnd handler should not clear the targets every time!!!, see ctor
        // m_targets = null;
        m_currentTarget = null;
        m_srcId = null;
        m_srcList = null;
    }

    /**
     * Creates a clone of element to be dragged around.<p>
     * 
     * @param element the element to clone
     * 
     * @return the generated clone
     */
    protected CmsDnDListItem createDragClone(Element element) {

        Element elementClone = DOM.createDiv();
        elementClone.setInnerHTML(element.getInnerHTML());
        elementClone.setClassName(element.getClassName());
        CmsDnDListItem dragElement = new CmsDnDListItem(elementClone);

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

        fireEvent(m_currentTarget);
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
     * Fires the drop event for the given target list.<p>
     * 
     * @param targetList the target list
     */
    protected void fireEvent(final CmsDnDList<CmsDnDListItem> targetList) {

        final CmsDnDList<CmsDnDListItem> srcList = m_srcList;
        final CmsDnDListItem placeholder = (CmsDnDListItem)m_placeholder;
        final int srcPos = m_srcPos;
        final String srcId = m_srcId;
        if ((srcList == targetList) && (srcPos == targetList.getWidgetIndex(placeholder))) {
            // nothing changed
            return;
        }
        CmsDnDListDropEvent event = new CmsDnDListDropEvent(srcList, srcId, targetList, srcId);
        if ((srcList != targetList) && (targetList.getItem(srcId) != null)) {
            // collision detected
            if (m_collisionHandler != null) {
                m_collisionHandler.checkCollision(event, new AsyncCallback<String>() {

                    /**
                     * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
                     */
                    public void onFailure(Throwable caught) {

                        // cancel after dropping
                        placeholder.onDragStop();
                        srcList.insert(placeholder, srcPos);
                    }

                    /**
                     * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
                     */
                    public void onSuccess(String result) {

                        placeholder.onDragStop();
                        CmsDnDListDropEvent resolutionEvent = new CmsDnDListDropEvent(
                            srcList,
                            srcId,
                            targetList,
                            result);
                        m_handlerManager.fireEvent(resolutionEvent);
                        targetList.fireDropEvent(resolutionEvent);
                    }
                });
            } else {
                // cancel now
                placeholder.onDragStop();
                srcList.insert(m_placeholder, srcPos);
            }
        } else {
            placeholder.onDragStop();
            m_handlerManager.fireEvent(event);
            targetList.fireDropEvent(event);
        }
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.A_CmsSortingDragHandler#prepareElementForDrag()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void prepareElementForDrag() {

        CmsDnDList<? extends CmsListItem> parentList = (CmsDnDList<? extends CmsListItem>)m_dragElement.getParentList();
        m_currentTarget = (CmsDnDList<CmsDnDListItem>)parentList;

        // keep track of the drag source
        m_srcId = m_dragElement.getId();
        m_srcList = m_currentTarget;
        m_srcPos = m_currentTarget.getWidgetIndex(m_dragElement);
        // create the drag helper
        CmsDnDListItem clone = createDragClone(m_dragElement.getElement());

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

        // tell the drag element that it is going to be dragged and that it has to be converted into a placeholder
        m_dragElement.onDragStart();

        // drag element becomes the place holder
        m_placeholder = m_dragElement;

        // drag helper becomes the drag element
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

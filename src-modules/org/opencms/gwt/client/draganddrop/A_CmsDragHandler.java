/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/draganddrop/Attic/A_CmsDragHandler.java,v $
 * Date   : $Date: 2010/04/16 13:54:15 $
 * Version: $Revision: 1.6 $
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

package org.opencms.gwt.client.draganddrop;

import org.opencms.gwt.client.util.CmsScrollTimer;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract drag and drop handler implementation covering the most part off a drag and drop process.<p>
 * 
 * @param <E> the draggable element type
 * @param <T> the drag target type
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public abstract class A_CmsDragHandler<E extends I_CmsDragElement, T extends I_CmsDragTarget>
implements I_CmsDragHandler<E, T> {

    /** The current mouse event. */
    @SuppressWarnings("unchecked")
    protected MouseEvent m_currentEvent;

    /** The current drag target. */
    protected T m_currentTarget;

    /** The cursor offset left from the dragged element. */
    protected int m_cursorOffsetLeft;

    /** The cursor offset top from the dragged element. */
    protected int m_cursorOffsetTop;

    /** The element to drag. */
    protected E m_dragElement;

    /** Flag to indicate if the dragging has started. */
    protected boolean m_dragging;

    /** List of handler registrations. */
    protected List<HandlerRegistration> m_handlerRegistrations;

    /** The place-holder widget. */
    protected Widget m_placeholder;

    /** The list of all registered targets. */
    protected List<T> m_targets;

    /** Flag if automatic scrolling is enabled. */
    protected boolean m_isScrollEnabled;

    /** Scroll timer. */
    protected Timer m_scrollTimer;

    /** Current scroll direction. */
    protected CmsScrollTimer.Direction m_scrollDirection;

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#addDragTarget(org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void addDragTarget(T target) {

        m_targets.add(target);

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#getCurrentMouseEvent()
     */
    @SuppressWarnings("unchecked")
    public MouseEvent getCurrentMouseEvent() {

        return m_currentEvent;
    }

    /**
     * Returns the targets.<p>
     *
     * @return the targets
     */
    public List<T> getTargets() {

        return m_targets;
    }

    /**
     * Returns if a dragging process is taking place.<p>
     * 
     * @return <code>true</code> if the handler is currently dragging
     */
    public boolean isDragging() {

        return m_dragging;
    }

    /**
     * @see com.google.gwt.event.dom.client.ContextMenuHandler#onContextMenu(com.google.gwt.event.dom.client.ContextMenuEvent)
     */
    public void onContextMenu(ContextMenuEvent event) {

        if (m_dragging) {
            event.preventDefault();
            event.stopPropagation();
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseDownHandler#onMouseDown(com.google.gwt.event.dom.client.MouseDownEvent)
     */
    @SuppressWarnings("unchecked")
    public void onMouseDown(MouseDownEvent event) {

        // only act on left button down, ignore right click
        if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            try {
                m_dragElement = (E)event.getSource();
            } catch (Exception e) {
                // TODO: add logging
            }
            if ((m_dragElement != null) && m_dragElement.isHandleEvent(event.getNativeEvent())) {
                DOM.setCapture(m_dragElement.getElement());
                m_dragging = true;
                Document.get().getBody().addClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragStarted());
                m_currentEvent = event;
                m_currentTarget = (T)m_dragElement.getDragParent();
                m_cursorOffsetLeft = m_currentEvent.getRelativeX(m_dragElement.getElement());
                m_cursorOffsetTop = m_currentEvent.getRelativeY(m_dragElement.getElement());

                prepareElementForDrag();
                m_dragElement.onDragStart(this);

                positionElement();

                event.preventDefault();
                event.stopPropagation();
            }
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseMoveHandler#onMouseMove(com.google.gwt.event.dom.client.MouseMoveEvent)
     */
    public void onMouseMove(MouseMoveEvent event) {

        m_currentEvent = event;
        if (m_dragging) {

            checkTargets();
            positionElement();
            event.preventDefault();
            event.stopPropagation();

            scrollAction();
        }

    }

    /**
     * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
     */
    public void onMouseOut(MouseOutEvent event) {

        if (m_dragging) {
            event.preventDefault();
            event.stopPropagation();
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
     */
    public void onMouseOver(MouseOverEvent event) {

        if (m_dragging) {
            event.preventDefault();
            event.stopPropagation();
        }
    }

    /**
     * @see com.google.gwt.event.dom.client.MouseUpHandler#onMouseUp(com.google.gwt.event.dom.client.MouseUpEvent)
     */
    public void onMouseUp(MouseUpEvent event) {

        m_currentEvent = event;

        // only act on left button up, ignore right click
        if (m_dragging && (event.getNativeButton() == NativeEvent.BUTTON_LEFT)) {
            m_dragging = false;

            if (m_currentTarget != null) {
                elementDropAction();
                m_dragElement.onDropTarget(this, m_currentTarget);
                m_currentTarget.onDrop(this);
                m_currentTarget = null;
            } else {
                elementCancelAction();
                m_dragElement.onDragCancel(this);
            }
            DOM.releaseCapture(m_dragElement.getElement());
            event.preventDefault();
            event.stopPropagation();
            restoreElementAfterDrag();
            Document.get().getBody().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragStarted());
            m_dragElement.onDragStop(this);
            m_dragElement = null;
            m_placeholder = null;
            m_targets = null;
            m_currentTarget = null;
        }

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#registerMouseHandler(org.opencms.gwt.client.draganddrop.I_CmsDragElement)
     */
    public void registerMouseHandler(E element) {

        element.addMouseDownHandler(this);
        element.addMouseMoveHandler(this);
        element.addMouseUpHandler(this);
        element.addMouseOutHandler(this);
        element.addMouseOverHandler(this);
        element.addContextMenuHandler(this);
    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#setDragTargets(java.util.List)
     */
    public void setDragTargets(List<T> targets) {

        m_targets = targets;
    }

    /**
     * Method will check all registered drag targets if the element is positioned over one of them. 
     * {@link I_CmsDragElement#onDragLeave(I_CmsDragHandler, I_CmsDragTarget)}, {@link I_CmsDragTarget#onDragLeave(I_CmsDragHandler)},
     * {@link I_CmsDragElement#onDragEnter(I_CmsDragHandler, I_CmsDragTarget)}, {@link I_CmsDragTarget#onDragEnter(I_CmsDragHandler)} and
     * {@link I_CmsDragTarget#onDragInside(I_CmsDragHandler)} will be called accordingly.<p>
     */
    protected void checkTargets() {

        if (m_targets == null) {
            return;
        }
        Iterator<T> it = m_targets.iterator();
        while (it.hasNext()) {
            T target = it.next();
            Element element = target.getElement();

            // check if the mouse pointer is within the width of the target 
            int left = m_currentEvent.getRelativeX(element);
            if ((left > 0) && (left < element.getOffsetWidth())) {

                // check if the mouse pointer is within the height of the target 
                int top = m_currentEvent.getRelativeY(element);
                if ((top > 0) && (top < element.getOffsetHeight())) {
                    if (target == m_currentTarget) {
                        sortTarget();
                        m_currentTarget.onDragInside(this);
                    } else {
                        if (m_currentTarget != null) {
                            elementLeaveTargetAction();
                            m_dragElement.onDragLeave(this, m_currentTarget);
                            m_currentTarget.onDragLeave(this);
                        }
                        m_currentTarget = target;
                        elementEnterTargetAction();
                        m_dragElement.onDragEnter(this, m_currentTarget);
                        m_currentTarget.onDragEnter(this);
                        sortTarget();

                    }
                    return;
                }
            }
        }
        if (m_currentTarget != null) {
            elementLeaveTargetAction();
            m_dragElement.onDragLeave(this, m_currentTarget);
            m_currentTarget.onDragLeave(this);
            m_currentTarget = null;
        }
    }

    /**
     * Method executed when the element is dropped outside any target.<p>
     */
    protected abstract void elementCancelAction();

    /**
     * Method executed when the element is dropped on a target.<p>
     */
    protected abstract void elementDropAction();

    /**
     * Method executed when an element is dragged into a target.<p> 
     */
    protected abstract void elementEnterTargetAction();

    /**
     * Method executed when the element is dragged out off a target.<p>
     */
    protected abstract void elementLeaveTargetAction();

    /**
     * Positions an element depending on the current events client position and the cursor offset. This method assumes that the element parent is positioned relative.<p>
     */
    protected void positionElement() {

        Element parentElement = (Element)m_dragElement.getElement().getParentElement();
        int left = m_currentEvent.getRelativeX(parentElement) - m_cursorOffsetLeft;
        int top = m_currentEvent.getRelativeY(parentElement) - m_cursorOffsetTop;
        DOM.setStyleAttribute(m_dragElement.getElement(), "left", left + "px");
        DOM.setStyleAttribute(m_dragElement.getElement(), "top", top + "px");
    }

    /**
     * Prepares the draggable element for the dragging process. Sets styles, creates place-holders and other stuff.<p>
     */
    protected abstract void prepareElementForDrag();

    /**
     * Restores the draggable element to it's static state. Removing styles and place-holders, etc..<p>
     * 
     * Important: Set the new drag parent property on the draggable element if necessary ({@link org.opencms.gwt.client.draganddrop.I_CmsDragElement#setDragParent})!<p>
     */
    protected abstract void restoreElementAfterDrag();

    /**
     * Sorts the elements inside a target depending on the mouse position.<p>
     */
    protected void sortTarget() {

        Iterator<Widget> it = m_currentTarget.iterator();
        while (it.hasNext()) {
            Widget child = it.next();
            Element element = child.getElement();

            // only take visible and not 'position:absolute' elements into account, also ignore the place-holder
            String positioning = element.getStyle().getPosition();
            if (!(positioning.equals(Position.ABSOLUTE.getCssName()) || positioning.equals(Position.FIXED.getCssName()))
                && child.isVisible()
                && (m_placeholder != child)) {

                // check if the mouse pointer is within the width of the element 
                int left = m_currentEvent.getRelativeX(element);
                if ((left > 0) && (left < element.getOffsetWidth())) {

                    // check if the mouse pointer is within the height of the element 
                    int top = m_currentEvent.getRelativeY(element);
                    int height = element.getOffsetHeight();
                    if ((top > 0) && (top < height)) {
                        int index = m_currentTarget.getWidgetIndex(child);

                        // check if the mouse pointer is within the upper half of the element,
                        // only act if the place-holder index has to be changed
                        if (top < height / 2) {
                            if (m_currentTarget.getWidgetIndex(m_placeholder) != index) {
                                m_currentTarget.insert(m_placeholder, index);
                                targetSortChangeAction();
                            }
                        } else {
                            if (m_currentTarget.getWidgetIndex(m_placeholder) != index + 1) {
                                m_currentTarget.insert(m_placeholder, index + 1);
                                targetSortChangeAction();
                            }
                        }
                        return;
                    }
                }
            }
        }
    }

    /**
     * Handles automated scrolling.<p>
     */
    protected void scrollAction() {

        if (m_isScrollEnabled) {

            CmsScrollTimer.Direction direction = CmsScrollTimer.getScrollDirection(m_currentEvent, 100);
            if ((m_scrollTimer != null) && (m_scrollDirection != direction)) {
                m_scrollTimer.cancel();
                m_scrollTimer = null;
            }
            if ((direction != null) && (m_scrollTimer == null)) {
                m_scrollTimer = new CmsScrollTimer(RootPanel.getBodyElement(), 10, direction);
                m_scrollTimer.scheduleRepeating(10);
            }

            m_scrollDirection = direction;
        }
    }

    /**
     * Method executed when the widget order within the current target has been changed.<p> 
     */
    protected abstract void targetSortChangeAction();

}

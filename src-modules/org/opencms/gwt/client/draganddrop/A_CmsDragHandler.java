/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/draganddrop/Attic/A_CmsDragHandler.java,v $
 * Date   : $Date: 2010/05/05 12:39:52 $
 * Version: $Revision: 1.16 $
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

import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsMoveAnimation;
import org.opencms.gwt.client.util.CmsScrollTimer;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.client.util.CmsDomUtil.Style;

import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.shared.EventHandler;
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
 * @version $Revision: 1.16 $
 * 
 * @since 8.0.0
 */
public abstract class A_CmsDragHandler<E extends I_CmsDragElement<T>, T extends I_CmsDragTarget>
implements I_CmsDragHandler<E, T> {

    /** Animation enabled flag. */
    protected boolean m_animationEnabled;

    /** The current mouse event. */
    protected MouseEvent<? extends EventHandler> m_currentEvent;

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

    /** Flag if automatic scrolling is enabled. */
    protected boolean m_isScrollEnabled;

    /** The place-holder widget. */
    protected Widget m_placeholder;

    /** Current scroll direction. */
    protected CmsScrollTimer.Direction m_scrollDirection;

    /** Scroll timer. */
    protected Timer m_scrollTimer;

    /** The list of all registered targets. */
    protected List<T> m_targets;

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#addDragTarget(org.opencms.gwt.client.draganddrop.I_CmsDragTarget)
     */
    public void addDragTarget(T target) {

        m_targets.add(target);

    }

    /**
     * @see org.opencms.gwt.client.draganddrop.I_CmsDragHandler#getCurrentMouseEvent()
     */
    public MouseEvent<? extends EventHandler> getCurrentMouseEvent() {

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

        if (event.getNativeButton() != NativeEvent.BUTTON_LEFT) {
            // only act on left button down, ignore right click
            return;
        }
        try {
            m_dragElement = (E)event.getSource();
        } catch (Exception e) {
            // TODO: add logging
        }
        if ((m_dragElement == null) || !m_dragElement.isHandleEvent(event.getNativeEvent())) {
            // drag element is not listening
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

            DOM.releaseCapture(m_dragElement.getElement());
            event.preventDefault();
            event.stopPropagation();
            if (m_animationEnabled) {
                animateClear();
            } else {
                clearDrag();
            }
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
     * Clears the drag process with a move animation of the drag element to the place-holder position.<p>
     */
    protected void animateClear() {

        I_CmsSimpleCallback<Void> callback = new I_CmsSimpleCallback<Void>() {

            /**
             * Call-back method.<p>
             * 
             * @param arg void
             */
            public void execute(Void arg) {

                clearDrag();
            }

            /**
             * @see org.opencms.gwt.client.util.I_CmsSimpleCallback#onError(java.lang.String)
             */
            public void onError(String message) {

                // nothing to do

            }
        };
        int endTop = DOM.getAbsoluteTop(m_placeholder.getElement())
            - DOM.getAbsoluteTop((Element)m_placeholder.getElement().getParentElement());
        int endLeft = DOM.getAbsoluteLeft(m_placeholder.getElement())
            - DOM.getAbsoluteLeft((Element)m_placeholder.getElement().getParentElement());
        int startTop = CmsDomUtil.getCurrentStyleInt(m_dragElement.getElement(), Style.top);
        int startLeft = CmsDomUtil.getCurrentStyleInt(m_dragElement.getElement(), Style.left);
        CmsMoveAnimation ani = new CmsMoveAnimation(
            m_dragElement.getElement(),
            startTop,
            startLeft,
            endTop,
            endLeft,
            callback);
        ani.run(300);
    }

    /**
     * Method will check all registered drag targets if the element is positioned over one of them. 
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
            if ((left <= 0) || (left >= element.getOffsetWidth())) {
                continue;
            }

            // check if the mouse pointer is within the height of the target 
            int top = m_currentEvent.getRelativeY(element);
            if ((top <= 0) || (top >= element.getOffsetHeight())) {
                continue;
            }

            if (target == m_currentTarget) {
                sortTarget();
            } else {
                if (m_currentTarget != null) {
                    elementLeaveTargetAction();
                }
                m_currentTarget = target;
                elementEnterTargetAction();
                sortTarget();
            }
            return;
        }
        if (m_currentTarget != null) {
            elementLeaveTargetAction();
            m_currentTarget = null;
        }
    }

    /**
     * Restores the dragged element from dragging and clears all references used within the current drag process.<p>
     */
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
        m_targets = null;
        m_currentTarget = null;
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
     * 
     * Important: Set the current target.<p>
     */
    protected abstract void prepareElementForDrag();

    /**
     * Restores the draggable element to it's static state. Removing styles and place-holders, etc..<p>
     */
    protected abstract void restoreElementAfterDrag();

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
                m_scrollTimer = new CmsScrollTimer(RootPanel.getBodyElement(), 20, direction);
                m_scrollTimer.scheduleRepeating(10);
            }

            m_scrollDirection = direction;
        }
    }

    /**
     * Sorts the elements inside a target depending on the mouse position.<p>
     */
    protected abstract void sortTarget();

    /**
     * Method executed when the widget order within the current target has been changed.<p> 
     */
    protected abstract void targetSortChangeAction();

}

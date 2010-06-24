/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/dnd/Attic/CmsDnDManager.java,v $
 * Date   : $Date: 2010/06/24 09:05:26 $
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

package org.opencms.gwt.client.ui.dnd;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsMoveAnimation;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.client.util.CmsDomUtil.Style;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Drag and drop handler for generic lists.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsDnDManager {

    /**
     * Timer to schedule automated scrolling.<p>
     */
    protected class CmsScrollTimer extends Timer {

        /** The current scroll direction. */
        private Direction m_direction;

        /** Flag indicating if the scroll parent is the body element. */
        private boolean m_isBody;

        /** The element that should scrolled. */
        private Element m_scrollParent;

        /** The scroll speed. */
        private int m_scrollSpeed;

        /**
         * Constructor.<p>
         * 
         * @param scrollParent the element that should scrolled
         * @param scrollSpeed the scroll speed
         * @param direction the scroll direction
         */
        public CmsScrollTimer(Element scrollParent, int scrollSpeed, Direction direction) {

            m_scrollParent = scrollParent;
            m_scrollSpeed = scrollSpeed;
            m_isBody = m_scrollParent.getTagName().equalsIgnoreCase(CmsDomUtil.Tag.body.name());
            m_direction = direction;
        }

        /**
         * @see com.google.gwt.user.client.Timer#run()
         */
        @Override
        public void run() {

            int top, left;
            if (m_isBody) {
                top = Window.getScrollTop();
                left = Window.getScrollLeft();
            } else {
                top = m_scrollParent.getScrollTop();
                left = m_scrollParent.getScrollLeft();
            }
            Element element = m_draggable.getElement();

            boolean abort = false;
            switch (m_direction) {
                case down:
                    top += m_scrollSpeed;
                    element.getStyle().setTop(
                        CmsDomUtil.getCurrentStyleInt(element, Style.top) + m_scrollSpeed,
                        Unit.PX);
                    break;
                case up:
                    if (top <= m_scrollSpeed) {
                        abort = true;
                        top = 0;
                        element.getStyle().setTop(CmsDomUtil.getCurrentStyleInt(element, Style.top) - top, Unit.PX);
                        break;
                    }
                    top -= m_scrollSpeed;
                    element.getStyle().setTop(
                        CmsDomUtil.getCurrentStyleInt(element, Style.top) - m_scrollSpeed,
                        Unit.PX);
                    break;
                case left:
                    if (left <= m_scrollSpeed) {
                        abort = true;
                        element.getStyle().setLeft(CmsDomUtil.getCurrentStyleInt(element, Style.left) - left, Unit.PX);
                        left = 0;
                        break;
                    }
                    left -= m_scrollSpeed;
                    element.getStyle().setLeft(
                        CmsDomUtil.getCurrentStyleInt(element, Style.left) - m_scrollSpeed,
                        Unit.PX);
                    break;
                case right:
                    left += m_scrollSpeed;
                    element.getStyle().setLeft(
                        CmsDomUtil.getCurrentStyleInt(element, Style.left) + m_scrollSpeed,
                        Unit.PX);
                    break;
                default:
                    break;

            }

            if (m_isBody) {
                Window.scrollTo(left, top);
            } else {
                m_scrollParent.setScrollLeft(left);
                m_scrollParent.setScrollTop(top);
            }
            if (abort) {
                this.cancel();
                m_scrollTimer = null;
            }
        }
    }

    /** Scroll direction enumeration. */
    protected enum Direction {
        /** Scroll direction. */
        down,

        /** Scroll direction. */
        left,

        /** Scroll direction. */
        right,

        /** Scroll direction. */
        up
    }

    /**
     * The mouse handler to start dragging.<p>
     */
    protected final class DnDMouseDownHandler implements MouseDownHandler {

        /**
         * @see com.google.gwt.event.dom.client.MouseDownHandler#onMouseDown(com.google.gwt.event.dom.client.MouseDownEvent)
         */
        public void onMouseDown(MouseDownEvent event) {

            if (event.getNativeButton() != NativeEvent.BUTTON_LEFT) {
                // only act on left button down, ignore right click
                return;
            }
            m_draggable = ((I_CmsDragHandle)event.getSource()).getDraggable();

            if (m_draggable == null) {
                // cancel dragging
                return;
            }

            m_clientX = event.getClientX();
            m_clientY = event.getClientY();
            // TODO: may be the reference is not the right one?
            m_cursorOffsetLeft = CmsDomUtil.getRelativeX(m_clientX, m_draggable.getElement());
            m_cursorOffsetTop = CmsDomUtil.getRelativeY(m_clientY, m_draggable.getElement());

            if (!m_draggable.onDragStart()) {
                // cancel dragging
                return;
            }

            m_dragging = true;
            Document.get().getBody().addClassName(
                org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.dragdropCss().dragStarted());

            // check if we have already a valid target
            checkTargets();

            positionHelper();

            event.preventDefault();
            event.stopPropagation();

            // important: capture all mouse events and dispatch them to this element until released
            final com.google.gwt.user.client.Element element = RootPanel.getBodyElement();
            DOM.setCapture(element);
            DOM.setEventListener(element, new DragListener(element));

            m_handleReg = Event.addNativePreviewHandler(m_keyHandler);
        }
    }

    /**
     * The drag listener.<p>
     * 
     * It is set on drag start and removed on mouse up
     */
    protected final class DragListener implements EventListener {

        /** The element to listen to. */
        private final com.google.gwt.user.client.Element m_element;

        /**
         * Constructor.<p>
         * 
         * @param element the element to listen to
         */
        protected DragListener(com.google.gwt.user.client.Element element) {

            m_element = element;
        }

        /**
         * @see com.google.gwt.user.client.EventListener#onBrowserEvent(com.google.gwt.user.client.Event)
         */
        public void onBrowserEvent(Event e) {

            switch (DOM.eventGetType(e)) {
                case Event.ONCONTEXTMENU:
                case Event.ONMOUSEOVER:
                case Event.ONMOUSEOUT:
                case Event.ONMOUSEDOWN:
                    // do not care about these
                    e.preventDefault();
                    e.stopPropagation();
                    break;
                case Event.ONMOUSEMOVE:
                    // dragging
                    drag(e);
                    break;
                case Event.ONMOUSEUP:
                    // stop listening
                    DOM.setEventListener(m_element, null);
                    DOM.releaseCapture(m_element);
                    // dropping
                    drop(e);
                    break;
                default:
                    // do nothing
            }
        }
    }

    /**
     * Drag'n drop key handler, to cancel dragging on ESC.<p>
     * 
     * It is set on drag start and removed on mouse up
     */
    protected final class KeyHandler implements NativePreviewHandler {

        /**
         * @see com.google.gwt.user.client.Event.NativePreviewHandler#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
         */
        public void onPreviewNativeEvent(NativePreviewEvent e) {

            if (!m_dragging) {
                return;
            }
            Event nativeEvent;
            try {
                nativeEvent = Event.as(e.getNativeEvent());
            } catch (Exception exc) {
                // sometimes in dev mode, and only in dev mode, we get
                // "Found interface com.google.gwt.user.client.Event, but class was expected"
                return;
            }
            if (e.getTypeInt() != Event.ONKEYDOWN) {
                return;
            }
            if (nativeEvent.getKeyCode() == 27) {
                // cancel on escape
                cancel();
            }
        }
    }

    /** Animation enabled flag. */
    protected boolean m_animationEnabled;

    /** The mouse event horizontal position. */
    protected int m_clientX;

    /** The mouse event vertical position. */
    protected int m_clientY;

    /** The current drag target. */
    protected I_CmsDropTarget m_currentTarget;

    /** The cursor offset left from the dragged element. */
    protected int m_cursorOffsetLeft;

    /** The cursor offset top from the dragged element. */
    protected int m_cursorOffsetTop;

    /** The element to drag. */
    protected I_CmsDraggable m_draggable;

    /** Flag to indicate if the dragging has started. */
    protected boolean m_dragging;

    /** The drag helper. */
    protected Element m_dragHelper;

    /** The key event handler registration. */
    protected HandlerRegistration m_handleReg;

    /** The handler manager. */
    protected HandlerManager m_handlerManager;

    /** Flag if automatic scrolling is enabled. */
    protected boolean m_isScrollEnabled;

    /** The key handler, to cancel dragging with ESC. */
    protected KeyHandler m_keyHandler;

    /** The mouse down handler to start dragging. */
    protected MouseDownHandler m_mouseDownHandler;

    /** The current drop position. */
    protected CmsDropPosition m_position;

    /** Current scroll direction. */
    protected Direction m_scrollDirection;

    /** Scroll timer. */
    protected Timer m_scrollTimer;

    /** The list of all registered targets. */
    protected List<I_CmsDropTarget> m_targets;

    /**
     * Constructor.<p>
     */
    public CmsDnDManager() {

        init();
    }

    /**
     * Adds a new drag target.<p>  
     * 
     * @param target the drag target to add
     */
    public void addDragTarget(I_CmsDropTarget target) {

        m_targets.add(0, target);
    }

    /**
     * Adds a new list drop event handler.<p>
     * 
     * @param handler the handler to add
     * 
     * @return the handler registration
     */
    public HandlerRegistration addDropHandler(I_CmsDropHandler handler) {

        return m_handlerManager.addHandler(CmsDropEvent.getType(), handler);
    }

    /**
     * Returns the current target.<p>
     * 
     * @return the current target
     */
    public I_CmsDropTarget getCurrentTarget() {

        return m_currentTarget;
    }

    /**
     * The dragged widget.<p>
     * 
     * @return the dragged widget
     */
    public I_CmsDraggable getDraggable() {

        return m_draggable;
    }

    /**
     * Returns a suitable mouse down handler to start dragging.<p>
     * 
     * @return a new mouse down handler
     */
    public MouseDownHandler getMouseDownHandler() {

        return m_mouseDownHandler;
    }

    /**
     * Returns the targets.<p>
     *
     * @return the targets
     */
    public List<I_CmsDropTarget> getTargets() {

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
     * Removes the given target.<p>
     * 
     * @param target the target to remove
     */
    public void removeDropTarget(Panel target) {

        m_targets.remove(target);
    }

    /**
     * Tells to use animations or not, while dropping.<p>
     *
     * @param enabled the value to set
     */
    public void setAnimationEnabled(boolean enabled) {

        m_animationEnabled = enabled;
    }

    /**
     * Enables/disables automatic scrolling while dragging.<p>
     *
     * @param enabled <code>true</code> to enable
     */
    public void setScrollEnabled(boolean enabled) {

        m_isScrollEnabled = enabled;
    }

    /**
     * Clears the drag process with a move animation of the drag element to the place-holder position.<p>
     */
    protected void animateDrop() {

        I_CmsSimpleCallback<Void> callback = new I_CmsSimpleCallback<Void>() {

            /**
             * Call-back method.<p>
             * 
             * @param arg void
             */
            public void execute(Void arg) {

                clear();
            }

            /**
             * @see org.opencms.gwt.client.util.I_CmsSimpleCallback#onError(java.lang.String)
             */
            public void onError(String message) {

                // nothing to do
            }
        };
        com.google.gwt.user.client.Element parentElement = (com.google.gwt.user.client.Element)m_dragHelper.getParentElement();
        int endTop = m_position.getTop() - DOM.getAbsoluteTop(parentElement); //DOM.getAbsoluteTop(destElem)
        int endLeft = m_position.getLeft() - DOM.getAbsoluteLeft(parentElement); // DOM.getAbsoluteLeft(destElem)
        int startTop = CmsDomUtil.getCurrentStyleInt(m_dragHelper, Style.top);
        int startLeft = CmsDomUtil.getCurrentStyleInt(m_dragHelper, Style.left);
        CmsMoveAnimation ani = new CmsMoveAnimation(
            (com.google.gwt.user.client.Element)m_dragHelper,
            startTop,
            startLeft,
            endTop,
            endLeft,
            callback);
        ani.run(300);
    }

    /**
     * Cancels dragging.<p>
     */
    protected void cancel() {

        // remove the place holder from the current position
        for (I_CmsDropTarget target : m_targets) {
            target.removePlaceholder();
        }
        // put the place holder back to the source position
        m_draggable.resetPlaceHolder();
        // prevent drop action
        m_currentTarget = null;
        // update status and animate
        stopDragging(null);
    }

    /**
     * Called when drag helper is dragged into a new target.<p>
     *  
     * @param newTarget the new target, can be <code>null</code> 
     */
    protected void changeTarget(I_CmsDropTarget newTarget) {

        // remove old place holder
        for (I_CmsDropTarget target : m_targets) {
            if ((newTarget != null) && target.equals(newTarget)) {
                // but do not remove the new one
                continue;
            }
            target.removePlaceholder();
        }
        if (newTarget != null) {
            // create new drag helper
            // TODO: correctly remove old helper and reposition new 
            m_dragHelper = m_draggable.getDragHelper(newTarget);
        } else {
            m_position = m_draggable.resetPlaceHolder();
        }
        m_currentTarget = newTarget;
    }

    /**
     * Method will check all registered drag targets if the element is positioned over one of them.<p>
     */
    protected void checkTargets() {

        if (m_targets == null) {
            return;
        }
        Iterator<I_CmsDropTarget> it = m_targets.iterator();
        while (it.hasNext()) {
            I_CmsDropTarget target = it.next();

            // check if this target is selected
            if (!target.check(m_clientX, m_clientY)) {
                // if not, check the next one
                continue;
            }

            // correctly position the place holder in the current target
            CmsDropPosition position = target.setPlaceholder(m_clientX, m_clientY, new CmsDropEvent(
                m_draggable,
                m_currentTarget,
                m_position));
            if (position != null) {
                m_position = position;
            }

            // did the target change?
            if (target != m_currentTarget) {
                // change current target
                changeTarget(target);
            }

            // we got already one target, and there can not be two targets selected at the same time
            return;
        }
        // no target matched, clear the current target
        if (m_currentTarget != null) {
            changeTarget(null);
        }
    }

    /**
     * Restores the dragged element from dragging and clears all references used within the current drag process.<p>
     */
    protected void clear() {

        for (I_CmsDropTarget target : m_targets) {
            if (target == m_currentTarget) {
                continue;
            }
            target.removePlaceholder();
        }
        m_draggable.onDragStop();
        if (m_currentTarget != null) {
            m_currentTarget.onDrop();
            m_currentTarget.removePlaceholder();
            onDrop();
        } else {
            onCancel();
        }
        m_handleReg.removeHandler();
        Document.get().getBody().removeClassName(I_CmsLayoutBundle.INSTANCE.dragdropCss().dragStarted());
        m_draggable = null;
        m_currentTarget = null;
        m_position = null;
        m_dragHelper = null;
    }

    /**
     * Handles the dragging itself.<p>
     * 
     * @param event the mouse move event
     */
    protected void drag(Event event) {

        if (!m_dragging) {
            return;
        }
        storeEventPos(event);

        checkTargets();
        positionHelper();
        event.preventDefault();
        event.stopPropagation();

        scroll();
    }

    /**
     * Handles the dropping itself.<p>
     * 
     * @param event the mouse up event
     */
    protected void drop(Event event) {

        if (!m_dragging) {
            return;
        }
        storeEventPos(event);

        if ((m_currentTarget == null) || (m_position == null) || !m_draggable.canDrop(m_currentTarget, m_position)) {
            // the business logic does not allow to drop here
            cancel();
            return;
        }

        if ((event.getButton() & NativeEvent.BUTTON_LEFT) == 0) {
            // we only drop on left mouse button
            cancel();
            return;
        }

        // try to drop
        stopDragging(event);
    }

    /**
     * Fires the drop event.<p>
     */
    protected void onDrop() {

        final I_CmsDraggable draggable = m_draggable;
        CmsDropEvent e = new CmsDropEvent(draggable, m_currentTarget, m_position);
        draggable.beforeDrop(e, new AsyncCallback<CmsDropEvent>() {

            /**
             * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
             */
            public void onFailure(Throwable caught) {

                // cancel after finished
                draggable.onDragCancel();
            }

            /**
             * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
             */
            public void onSuccess(CmsDropEvent result) {

                draggable.onDrop();
                m_handlerManager.fireEvent(result);
            }
        });
    }

    /**
     * Convenience method to get the appropriate scroll direction.<p>
     * 
     * @param offset the scroll parent border offset, if the cursor is within the border offset, scrolling should be triggered
     * 
     * @return the scroll direction
     */
    protected Direction getScrollDirection(int offset) {

        Element body = RootPanel.getBodyElement();
        int windowHeight = Window.getClientHeight();
        int bodyHeight = body.getClientHeight();
        if (windowHeight < bodyHeight) {
            if ((windowHeight - m_clientY < offset) && (Window.getScrollTop() < bodyHeight - windowHeight)) {
                return Direction.down;
            }
            if ((m_clientY < offset) && (Window.getScrollTop() > 0)) {
                return Direction.up;
            }
        }

        int windowWidth = Window.getClientWidth();
        int bodyWidth = body.getClientWidth();
        if (windowWidth < bodyWidth) {
            if ((windowWidth - m_clientX < offset) && (Window.getScrollLeft() < bodyWidth - windowWidth)) {
                return Direction.right;
            }
            if ((m_clientX < offset) && (Window.getScrollLeft() > 0)) {
                return Direction.left;
            }
        }

        return null;
    }

    /**
     * Initializes this handler.<p>
     */
    protected void init() {

        m_handlerManager = new HandlerManager(this);
        m_animationEnabled = true;
        m_targets = new ArrayList<I_CmsDropTarget>();
        m_mouseDownHandler = new DnDMouseDownHandler();
        m_keyHandler = new KeyHandler();
    }

    /**
     * Called when the element is dropped outside any target.<p>
     */
    protected void onCancel() {

        // nothing to do here
    }

    /**
     * Positions an element depending on the current events client position and the cursor offset. This method assumes that the element parent is positioned relative.<p>
     */
    protected void positionHelper() {

        if (m_dragHelper == null) {
            return;
        }
        com.google.gwt.user.client.Element dragHelper = (com.google.gwt.user.client.Element)m_dragHelper;
        Element parentElement = dragHelper.getParentElement();
        int left = CmsDomUtil.getRelativeX(m_clientX, parentElement) - m_cursorOffsetLeft;
        int top = CmsDomUtil.getRelativeY(m_clientY, parentElement) - m_cursorOffsetTop;
        DOM.setStyleAttribute(dragHelper, "left", left + "px");
        DOM.setStyleAttribute(dragHelper, "top", top + "px");
    }

    /**
     * Handles automated scrolling.<p>
     */
    protected void scroll() {

        if (m_isScrollEnabled) {

            Direction direction = getScrollDirection(100);
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
     * Stops dragging.<p>
     * 
     * @param event the mouse up event, or <code>null</code> 
     */
    protected void stopDragging(Event event) {

        m_dragging = false;
        if (m_scrollTimer != null) {
            m_scrollTimer.cancel();
            m_scrollTimer = null;
        }

        if (event != null) {
            event.preventDefault();
            event.stopPropagation();
        }
        if (m_animationEnabled) {
            animateDrop();
        } else {
            clear();
        }
    }

    /**
     * Stores the event position.<p>
     * 
     * @param event the event to store the position for
     */
    protected void storeEventPos(Event event) {

        m_clientX = event.getClientX();
        m_clientY = event.getClientY();
    }
}

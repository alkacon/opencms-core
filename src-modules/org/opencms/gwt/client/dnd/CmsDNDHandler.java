/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/dnd/Attic/CmsDNDHandler.java,v $
 * Date   : $Date: 2010/09/14 14:22:30 $
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

package org.opencms.gwt.client.dnd;

import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;

/**
 * Drag and drop handler.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsDNDHandler implements MouseDownHandler {

    /**
     * Drag and drop event preview handler.<p>
     * 
     * To be used while dragging.<p>
     */
    protected class DNDEventPreviewHandler implements NativePreviewHandler {

        /**
         * @see com.google.gwt.user.client.Event.NativePreviewHandler#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
         */
        public void onPreviewNativeEvent(NativePreviewEvent event) {

            if (!isDragging()) {
                // this should never happen, as the preview handler should be removed after the dragging stopped
                return;
            }
            Event nativeEvent = Event.as(event.getNativeEvent());
            switch (DOM.eventGetType(nativeEvent)) {
                case Event.ONMOUSEMOVE:
                    // dragging
                    onMove(nativeEvent);
                    break;
                case Event.ONMOUSEUP:
                    onUp(nativeEvent);
                    break;
                case Event.ONKEYDOWN:
                    if (nativeEvent.getKeyCode() == 27) {
                        // cancel on escape
                    }
                    break;
                default:
                    // do nothing
            }
            event.cancel();
            nativeEvent.preventDefault();
            nativeEvent.stopPropagation();
        }

    }

    /** The mouse x position of the current mouse event. */
    private int m_clientX;

    /** The mouse y position of the current mouse event. */
    private int m_clientY;

    /** The Drag and drop controller. */
    private I_CmsDNDController m_controller;

    /** The current drop target. */
    private I_CmsDropTarget m_currentTarget;

    /** The x cursor offset to the dragged element. */
    private int m_cursorOffsetX;

    /** The y cursor offset to the dragged element. */
    private int m_cursorOffsetY;

    /** The draggable. */
    private I_CmsDraggable m_draggable;

    /** The dragging flag. */
    private boolean m_dragging;

    /** The drag helper. */
    private Element m_dragHelper;

    /** The placeholder. */
    private Element m_placeholder;

    /** The event preview handler. */
    private DNDEventPreviewHandler m_previewHandler;

    /** The preview handler registration. */
    private HandlerRegistration m_previewHandlerRegistration;

    /** The registered drop targets. */
    private List<I_CmsDropTarget> m_targets;

    /** 
     * Constructor.<p> 
     * 
     * @param controller the drag and drop controller 
     **/
    public CmsDNDHandler(I_CmsDNDController controller) {

        m_targets = new ArrayList<I_CmsDropTarget>();
        m_previewHandler = new DNDEventPreviewHandler();
        m_controller = controller;
    }

    /**
     * Adds a drop target.<p>
     * 
     * @param target the target to add
     */
    public void addTarget(I_CmsDropTarget target) {

        m_targets.add(target);
    }

    /** 
     * Cancels the dragging process.<p>
     */
    public void cancel() {

        m_controller.onDragCancel(m_draggable, m_currentTarget, this);
        m_draggable.onDragCancel();
        clear();
    }

    /**
     * Clears the drop target register.<p>
     */
    public void clearTargets() {

        m_targets.clear();
    }

    /**
     * Drops the draggable.<p>
     */
    public void drop() {

        m_controller.onBeforeDrop(m_draggable, m_currentTarget, this);
        m_draggable.onDrop(m_currentTarget);
        m_currentTarget.onDrop(m_draggable);
        m_controller.onDrop(m_draggable, m_currentTarget, this);
        clear();
    }

    /**
     * Returns the drag and drop controller.<p>
     *
     * @return the drag and drop controller
     */
    public I_CmsDNDController getController() {

        return m_controller;
    }

    /**
     * Returns the current drop target.<p>
     * 
     * @return the current drop target
     */
    public I_CmsDropTarget getCurrentTarget() {

        return m_currentTarget;
    }

    /**
     * Returns the current draggable.<p>
     * 
     * @return the draggable
     */
    public I_CmsDraggable getDraggable() {

        return m_draggable;
    }

    /**
     * Returns the drag helper element.<p>
     * 
     * @return the drag helper
     */
    public Element getDragHelper() {

        return m_dragHelper;
    }

    /**
     * Returns the place holder element.<p>
     * 
     * @return the place holder element
     */
    public Element getPlaceholder() {

        return m_placeholder;
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
     * @see com.google.gwt.event.dom.client.MouseDownHandler#onMouseDown(com.google.gwt.event.dom.client.MouseDownEvent)
     */
    public void onMouseDown(MouseDownEvent event) {

        if (event.getNativeButton() != NativeEvent.BUTTON_LEFT) {
            // only act on left button down, ignore right click
            return;
        }
        Object source = event.getSource();
        if (!(source instanceof I_CmsDragHandle)) {
            // source is no drag handle, wrong DNDHandler assignment ignore
            return;
        }
        m_draggable = ((I_CmsDragHandle)source).getDraggable();

        if (m_draggable == null) {
            // cancel dragging
            return;
        }
        m_clientX = event.getClientX();
        m_clientY = event.getClientY();
        m_cursorOffsetX = CmsDomUtil.getRelativeX(m_clientX, m_draggable.getElement());
        m_cursorOffsetY = CmsDomUtil.getRelativeY(m_clientY, m_draggable.getElement());

        m_currentTarget = m_draggable.getParentTarget();
        m_dragHelper = (Element)m_draggable.getDragHelper(m_currentTarget);
        m_placeholder = (Element)m_draggable.getPlaceholder(m_currentTarget);

        m_controller.onDragStart(m_draggable, m_currentTarget, this);
        m_draggable.onStartDrag(m_currentTarget);

        m_dragging = true;
        // add marker css class to enable drag and drop dependent styles
        Document.get().getBody().addClassName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.dragdropCss().dragStarted());
        m_previewHandlerRegistration = Event.addNativePreviewHandler(m_previewHandler);
    }

    /**
     * Removes a drop target from the register.<p>
     * 
     * @param target the target to remove
     */
    public void removeTarget(I_CmsDropTarget target) {

        m_targets.remove(target);
    }

    /** 
     * Sets the draggable.<p>
     * 
     * @param draggable the draggable
     */
    public void setDraggable(I_CmsDraggable draggable) {

        m_draggable = draggable;
    }

    /**
     * Executed on mouse move while dragging.<p>
     * 
     * @param event the event
     */
    protected void onMove(Event event) {

        m_clientX = event.getClientX();
        m_clientY = event.getClientY();
        checkTargets();
        positionHelper();
    }

    /**
     * Executed on mouse up while dragging.<p>
     * 
     * @param event the event
     */
    protected void onUp(Event event) {

        m_clientX = event.getClientX();
        m_clientY = event.getClientY();
        if (m_currentTarget == null) {

            cancel();
        } else {
            drop();
        }

    }

    /**
     * Positions an element depending on the current events client position and the cursor offset. This method assumes that the element parent is positioned relative.<p>
     */
    protected void positionHelper() {

        if (m_dragHelper == null) {
            // should never happen
            CmsDebugLog.getInstance().printLine("drag helper can not be positioned, as it is null");
            return;
        }
        Element parentElement = (Element)m_dragHelper.getParentElement();
        int left = CmsDomUtil.getRelativeX(m_clientX, parentElement) - m_cursorOffsetX;
        int top = CmsDomUtil.getRelativeY(m_clientY, parentElement) - m_cursorOffsetY;
        DOM.setStyleAttribute(m_dragHelper, "left", left + "px");
        DOM.setStyleAttribute(m_dragHelper, "top", top + "px");
    }

    /**
     * Method will check all registered drop targets if the element is positioned over one of them.<p>
     */
    private void checkTargets() {

        // checking current target first
        if ((m_currentTarget != null) && m_currentTarget.checkPosition(m_clientX, m_clientY)) {
            if (m_currentTarget.getPlaceholderIndex() < 0) {
                m_currentTarget.insertPlaceholder(m_placeholder, m_clientX, m_clientY);
            } else {
                m_currentTarget.repositionPlaceholder(m_clientX, m_clientY);
            }
        } else {
            // leaving the current target
            m_controller.onTargetLeave(m_draggable, m_currentTarget, this);
            for (I_CmsDropTarget target : m_targets) {
                if ((target != m_currentTarget) && target.checkPosition(m_clientX, m_clientY)) {
                    m_controller.onTargetEnter(m_draggable, target, this);
                    target.insertPlaceholder(m_placeholder, m_clientX, m_clientY);
                    m_currentTarget = target;
                    return;
                }
            }
            // mouse position is not over any registered target
            m_currentTarget = null;
        }
    }

    /**
     * Clears all references used within the current drag process.<p>
     */
    private void clear() {

        if (m_previewHandlerRegistration != null) {
            m_previewHandlerRegistration.removeHandler();
        }
        m_dragging = false;
        for (I_CmsDropTarget target : m_targets) {
            target.removePlaceholder();
        }
    }
}

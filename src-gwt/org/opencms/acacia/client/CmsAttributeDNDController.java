/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.client;

import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.acacia.client.ui.CmsAttributeValueView;
import org.opencms.acacia.client.ui.CmsValuePanel;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.dnd.CmsDNDHandler.Orientation;
import org.opencms.gwt.client.dnd.I_CmsDNDController;
import org.opencms.gwt.client.dnd.I_CmsDraggable;
import org.opencms.gwt.client.dnd.I_CmsDropTarget;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.DOM;

/**
 * The drag and drop controller for attribute value sorting.<p>
 */
public class CmsAttributeDNDController implements I_CmsDNDController {

    /** The drag overlay. */
    private Element m_dragOverlay;

    /** The starting position. */
    private int m_startPosition;

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onAnimationStart(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onAnimationStart(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        // nothing to do
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onBeforeDrop(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onBeforeDrop(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        // nothing to do
        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragCancel(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onDragCancel(I_CmsDraggable draggable, I_CmsDropTarget target, final CmsDNDHandler handler) {

        removeDragOverlay();
        clearTargets(handler);
        // remove the drag helper reference from handler, to avoid helper.removeFromParent() call
        handler.setDragHelper(null);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragStart(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onDragStart(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        if ((target instanceof CmsValuePanel)
            && (draggable instanceof CmsAttributeValueView)
            && ((CmsAttributeValueView)draggable).isDragEnabled()) {
            installDragOverlay();
            handler.setOrientation(Orientation.VERTICAL);
            m_startPosition = ((CmsAttributeValueView)draggable).getValueIndex();
            handler.clearTargets();
            handler.addTarget(target);
            target.getElement().getStyle().setPosition(Position.RELATIVE);
            target.getElement().insertBefore(handler.getPlaceholder(), draggable.getElement());
            ((CmsValuePanel)target).highlightOutline();
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDrop(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onDrop(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        CmsAttributeValueView attributeValue = (CmsAttributeValueView)draggable;
        int targetIndex = target.getPlaceholderIndex();
        if (targetIndex > m_startPosition) {
            targetIndex--;
        }
        attributeValue.getHandler().moveAttributeValue(attributeValue, m_startPosition, targetIndex);
        removeDragOverlay();
        clearTargets(handler);
        // remove the drag helper reference from handler, to avoid helper.removeFromParent() call
        handler.setDragHelper(null);
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onPositionedPlaceholder(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onPositionedPlaceholder(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        ((CmsValuePanel)target).updateHighlightingPosition();
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetEnter(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onTargetEnter(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        return true;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetLeave(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onTargetLeave(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        // nothing to do
    }

    /**
     * Clears the handlers drag and drop targets.<p>
     *
     * @param handler the drag and drop handler
     */
    private void clearTargets(final CmsDNDHandler handler) {

        if (handler.getCurrentTarget() != null) {
            ((CmsValuePanel)handler.getCurrentTarget()).removeHighlighting();
            handler.getCurrentTarget().getElement().getStyle().clearPosition();
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                handler.clearTargets();
            }
        });
    }

    /**
     * Installs the drag overlay to avoid any mouse over issues or similar.<p>
     */
    private void installDragOverlay() {

        if (m_dragOverlay != null) {
            m_dragOverlay.removeFromParent();
        }
        m_dragOverlay = DOM.createDiv();
        m_dragOverlay.addClassName(I_CmsLayoutBundle.INSTANCE.form().dragOverlay());
        Document.get().getBody().appendChild(m_dragOverlay);
    }

    /**
     * Removes the drag overlay.<p>
     */
    private void removeDragOverlay() {

        if (m_dragOverlay != null) {
            m_dragOverlay.removeFromParent();
            m_dragOverlay = null;
        }
    }

}

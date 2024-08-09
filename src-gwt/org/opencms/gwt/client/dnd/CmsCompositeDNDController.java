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

package org.opencms.gwt.client.dnd;

import java.util.ArrayList;
import java.util.List;

import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * A DND controller which forwards method calls to multiple other DND controllers.<p>
 */
public class CmsCompositeDNDController implements I_CmsDNDController {

    /** The list of internal controllers. */
    List<I_CmsDNDController> m_controllers = new ArrayList<I_CmsDNDController>();

    /**
     * Adds another sub-controller to this controller.<p>
     *
     * @param controller the controller to add
     *
     * @return a registration object to remove the controller later
     */
    public HandlerRegistration addController(final I_CmsDNDController controller) {

        m_controllers.add(controller);
        return new HandlerRegistration() {

            public void removeHandler() {

                m_controllers.remove(controller);
            }
        };
    }


    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onAnimationStart(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onAnimationStart(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        for (I_CmsDNDController controller : m_controllers) {
            controller.onAnimationStart(draggable, target, handler);
        }
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onBeforeDrop(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onBeforeDrop(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        boolean result = true;
        for (I_CmsDNDController controller : m_controllers) {
            result &= controller.onBeforeDrop(draggable, target, handler);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragCancel(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onDragCancel(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        for (I_CmsDNDController controller : m_controllers) {
            controller.onDragCancel(draggable, target, handler);
        }
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDragStart(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onDragStart(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        boolean result = true;
        for (I_CmsDNDController controller : m_controllers) {
            result &= controller.onDragStart(draggable, target, handler);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onDrop(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onDrop(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        for (I_CmsDNDController controller : m_controllers) {
            controller.onDrop(draggable, target, handler);
        }
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onPositionedPlaceholder(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onPositionedPlaceholder(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        for (I_CmsDNDController controller : m_controllers) {
            controller.onPositionedPlaceholder(draggable, target, handler);
        }
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetEnter(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public boolean onTargetEnter(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        boolean result = true;
        for (I_CmsDNDController controller : m_controllers) {
            result &= controller.onTargetEnter(draggable, target, handler);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#onTargetLeave(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    public void onTargetLeave(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDNDHandler handler) {

        for (I_CmsDNDController controller : m_controllers) {
            controller.onTargetLeave(draggable, target, handler);
        }
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#postClear(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.I_CmsDropTarget)
     */
    @Override
    public void postClear(I_CmsDraggable draggable, I_CmsDropTarget target) {
        m_controllers.forEach(controller -> {
            controller.postClear(draggable, target);

        });
    }

    /**
     * @see org.opencms.gwt.client.dnd.I_CmsDNDController#startPlacementMode(org.opencms.gwt.client.dnd.I_CmsDraggable, org.opencms.gwt.client.dnd.CmsDNDHandler)
     */
    @Override
    public boolean startPlacementMode(I_CmsDraggable draggable, CmsDNDHandler handler) {
        for (I_CmsDNDController controller: m_controllers) {
            if (controller.startPlacementMode(draggable, handler)) {
                return true;
            }
        }
        return false;
    }

}

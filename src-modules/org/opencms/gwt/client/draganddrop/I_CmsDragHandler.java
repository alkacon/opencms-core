/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/draganddrop/Attic/I_CmsDragHandler.java,v $
 * Date   : $Date: 2010/05/05 09:19:16 $
 * Version: $Revision: 1.4 $
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

import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.List;

import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.EventHandler;

/**
 * Interface to be implemented by drag and drop handlers.<p>
 * 
 * @param <E> the draggable element type
 * @param <T> the drag target type
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public interface I_CmsDragHandler<E extends I_CmsDragElement<T>, T extends I_CmsDragTarget>
extends MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOverHandler, MouseOutHandler, ContextMenuHandler {

    /**
     * Adds an additional drag target to the handler. Can be used to extend the possible drag targets even while dragging.<p>
     * 
     * @param target a drag and drop target 
     */
    void addDragTarget(T target);

    /**
     * Returns the current mouse event.<p>
     * 
     * @return the mouse event
     */
    MouseEvent<? extends EventHandler> getCurrentMouseEvent();

    /**
     * Returns the current target, the target the dragged element is currently over and the placeholder is shown in.<p>
     * 
     * @return the current drag target
     */
    T getCurrentTarget();

    /**
     * Returns the element currently being dragged.<p> 
     * 
     * @return the drag element
     */
    E getDragElement();

    /**
     * Registers this handler to the draggable element.<p>
     * 
     * @param element the draggable element
     */
    void registerMouseHandler(E element);

    /**
     * Sets the possible drag targets.<p>
     * 
     * @param targets
     */
    void setDragTargets(List<T> targets);

    /**
     * Factory method to create a draggable list item widget.<p>
     * 
     * @param infoBean the item info
     * @param id the item id
     * 
     * @return the list item
     */
    CmsListItemWidget createDraggableListItemWidget(CmsListInfoBean infoBean, String id);
}

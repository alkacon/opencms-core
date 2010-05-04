/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/draganddrop/Attic/I_CmsDragElement.java,v $
 * Date   : $Date: 2010/05/04 13:17:36 $
 * Version: $Revision: 1.2 $
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

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasContextMenuHandlers;
import com.google.gwt.user.client.Element;

/**
 * Interface defining all methods used by drag and drop on a draggable element.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public interface I_CmsDragElement extends HasAllMouseHandlers, HasContextMenuHandlers {

    /**
     * Returns the parent drag target.<p>
     * 
     * @return the parent drag target
     */
    I_CmsDragTarget getDragParent();

    /**
     * The root element of this widget.<p>
     * 
     * @return the element
     */
    Element getElement();

    /**
     * Call this method to determine if the given event is triggered on the element handle. If no handle is set, it will always return true.<p>
     * 
     * If an element handle is set, the element should only be movable by dragging the handle.
     * 
     * @param event the native DOM event
     * @return true if the event is triggered on the handle of the element
     */
    boolean isHandleEvent(NativeEvent event);

    /**
     * Registers a new drag parent.<p>
     * 
     * @param target the new drag parent
     */
    void setDragParent(I_CmsDragTarget target);

    /**
     * Sets whether this object is visible.<p>
     * 
     * @param visible <code>true</code> to show the object, <code>false</code> to hide it
     * 
     * @see com.google.gwt.user.client.ui.UIObject#setVisible(boolean)
     */
    void setVisible(boolean visible);

}

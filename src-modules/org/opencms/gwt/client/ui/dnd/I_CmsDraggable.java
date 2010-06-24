/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/dnd/Attic/I_CmsDraggable.java,v $
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Draggable widget.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 *  
 * @since 8.0.0 
 */
public interface I_CmsDraggable {

    /**
     * Just before dropping hook mainly for collision detection and resolution.<p>
     * 
     * You can still cancel the drop action by calling the {@link AsyncCallback#onFailure(java.lang.Throwable)} method,
     * or continue dropping by calling the {@link AsyncCallback#onSuccess(Object)} method.<p>
     * 
     * @param e the preliminary event to fire
     * @param asyncCallback the callback
     */
    void beforeDrop(CmsDropEvent e, AsyncCallback<CmsDropEvent> asyncCallback);

    /**
     * Early check if this item can be dropped at the place holder position on the given target.<p>
     * 
     * @param target the target
     * @param position the drop position
     * 
     * @return <code>true</code> to allow dropping, <code>false</code> to cancel
     */
    boolean canDrop(I_CmsDropTarget target, CmsDropPosition position);

    /**
     * Returns the drag helper for the given target.<p>
     * 
     * @param target the target for the placeholder, could be <code>null</code>
     * 
     * @return the drag helper
     */
    Element getDragHelper(I_CmsDropTarget target);

    /**
     * Returns this widget's element.<p>
     * 
     * @return this widget's element
     * 
     * @see com.google.gwt.user.client.ui.UIObject#getElement()
     */
    Element getElement();

    /**
     * Returns the placeholder for the given target.<p>
     * 
     * @param target the target for the placeholder, could be <code>null</code>
     * 
     * @return the placeholder
     */
    Element getPlaceHolder(I_CmsDropTarget target);

    /**
     * Will be executed when dragging has been canceled.<p>
     */
    void onDragCancel();

    /**
     * Called just before starting dragging.<p>
     * 
     * Return <code>false</code> to cancel.<p>
     * 
     * @return <code>true</code> if dragging is allowed, or <code>false</code> if not
     */
    boolean onDragStart();

    /**
     * Will be executed when stopping dragging, before calling {@link #onDrop()} or {@link #onDragCancel()}.<p>
     */
    void onDragStop();

    /**
     * Will be executed just before the drop event is fired.<p>
     */
    void onDrop();

    /**
     * Returns the placeholder to the original position.<p>
     * 
     * @return the drop position
     */
    CmsDropPosition resetPlaceHolder();
}
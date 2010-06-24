/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/dnd/Attic/CmsDropEvent.java,v $
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

import com.google.gwt.event.shared.GwtEvent;

/**
 * Drag and Drop list drop event.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsDropEvent extends GwtEvent<I_CmsDropHandler> {

    /** Event type. */
    private static final Type<I_CmsDropHandler> TYPE = new Type<I_CmsDropHandler>();

    /** The dropped draggable. */
    private I_CmsDraggable m_draggable;

    /** The drop position. */
    private CmsDropPosition m_position;

    /** The target. */
    private I_CmsDropTarget m_target;

    /**
     * Constructor.<p>
     * 
     * @param draggable the dropped draggable
     * @param target target
     * @param position drop position
     */
    public CmsDropEvent(I_CmsDraggable draggable, I_CmsDropTarget target, CmsDropPosition position) {

        m_draggable = draggable;
        m_target = target;
        m_position = position;
    }

    /**
     * Gets the event type associated with change events.<p>
     * 
     * @return the handler type
     */
    public static Type<I_CmsDropHandler> getType() {

        return TYPE;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public final Type<I_CmsDropHandler> getAssociatedType() {

        return TYPE;
    }

    /**
     * Returns the dropped draggable.<p>
     *
     * @return the dropped draggable
     */
    public I_CmsDraggable getDraggable() {

        return m_draggable;
    }

    /**
     * Returns the drop position.<p>
     *
     * @return the drop position
     */
    public CmsDropPosition getPosition() {

        return m_position;
    }

    /**
     * Returns the target.<p>
     *
     * @return the target
     */
    public I_CmsDropTarget getTarget() {

        return m_target;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void dispatch(I_CmsDropHandler handler) {

        handler.onDrop(this);
    }
}

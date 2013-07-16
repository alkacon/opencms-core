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

package org.opencms.ade.containerpage.client;

import com.google.web.bindery.event.shared.Event;

/**
 * This event is fired when a container page's elements or the page itself are edited.<p>
 */
public class CmsContainerpageEvent extends Event<I_CmsContainerpageEventHandler> {

    /** Enum for the event type. */
    public enum EventType {
        /** Fired when an element has been edited. */
        elementEdited,
        /** Fired when the page has been saved. */
        pageSaved;
    }

    /**
     * Handler type.
     */
    public static Type<I_CmsContainerpageEventHandler> TYPE = new Type<I_CmsContainerpageEventHandler>();

    /** The type the event. */
    private EventType m_type;

    /**
     * Creates a new event with a given type.<p>
     * 
     * @param type the event type 
     */
    public CmsContainerpageEvent(EventType type) {

        m_type = type;
    }

    /** 
     * @see com.google.web.bindery.event.shared.Event#getAssociatedType()
     */
    @Override
    public Event.Type<I_CmsContainerpageEventHandler> getAssociatedType() {

        return TYPE;

    }

    /**
     * Gets the type of the event.<p>
     * 
     * @return the type of the event 
     */
    public EventType getEventType() {

        return m_type;
    }

    /**
     * @see com.google.web.bindery.event.shared.Event#dispatch(java.lang.Object)
     */
    @Override
    protected void dispatch(I_CmsContainerpageEventHandler handler) {

        handler.onContainerpageEvent(this);
    }
}

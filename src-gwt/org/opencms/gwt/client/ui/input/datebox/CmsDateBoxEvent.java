/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.input.datebox;

import java.util.Date;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Custom event type for CmsDateBox changes.<p>
 *
 * The difference between this and a ValueChangeEvent is that this event is also fired after user actions
 * which may or may not have changed the value. The event carries information about that user action.
 */
public class CmsDateBoxEvent extends GwtEvent<I_CmsDateBoxEventHandler> {

    /** The event type. */
    public static Type<I_CmsDateBoxEventHandler> TYPE = new Type<I_CmsDateBoxEventHandler>();

    /** The currently selected date. */
    private Date m_date;

    /** True if the event was caused by a user key press that may have changed the value. */
    private boolean m_isUserTyping;

    /**
     * Creates a new event.<p>
     *
     * @param date the date
     * @param isUserTyping true if the event was caused by a user key press that may have changed the value.<p>
     */
    public CmsDateBoxEvent(Date date, boolean isUserTyping) {
        m_date = date;
        m_isUserTyping = isUserTyping;
    }

    /**
     * Fires the event.
     *
     * @param source the event source
     * @param date the date
     * @param isTyping true if event was caused by user pressing key that may have changed the value
     */
    public static void fire(I_CmsHasDateBoxEventHandlers source, Date date, boolean isTyping) {

        if (TYPE != null) {
            CmsDateBoxEvent event = new CmsDateBoxEvent(date, isTyping);
            source.fireEvent(event);
        }
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<I_CmsDateBoxEventHandler> getAssociatedType() {

        return TYPE;
    }

    /**
     * Gets the date.<p>
     *
     * @return the date
     */
    public Date getDate() {

        return m_date;

    }

    /**
     * Returns true if the event was caused by a user key press which may have changed the value.<p>
     *
     * @return true if caused by a keypress which may have changed the value
     */
    public boolean isUserTyping() {

        return m_isUserTyping;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void dispatch(I_CmsDateBoxEventHandler handler) {

        handler.onDateBoxEvent(this);
    }

}

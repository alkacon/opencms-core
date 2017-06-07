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

package org.opencms.gwt.client.ui;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event to be fired by a widget being activated or deactivated.<p>
 *
 * @since 8.0.0
 *
 */
public class CmsToggleEvent extends GwtEvent<I_CmsToggleHandler> {

    /** Handler type. */
    private static Type<I_CmsToggleHandler> TYPE;

    /** If the event source has been activated. */
    private boolean m_isActivated;

    /**
     * Creates a new open event.<p>
     *
     * @param isActivated if the source has been activated
     */
    protected CmsToggleEvent(boolean isActivated) {

        m_isActivated = isActivated;
    }

    /**
     * Fires a toggle event on all registered handlers in the handler manager.If no
     * such handlers exist, this method will do nothing.<p>
     *
     * @param source the event source
     * @param isActivated if the source has been activated
     */
    public static void fire(I_CmsHasToggleHandlers source, boolean isActivated) {

        if (TYPE != null) {
            CmsToggleEvent event = new CmsToggleEvent(isActivated);
            source.fireEvent(event);
        }
    }

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static com.google.gwt.event.shared.GwtEvent.Type<I_CmsToggleHandler> getType() {

        if (TYPE == null) {
            TYPE = new Type<I_CmsToggleHandler>();
        }
        return TYPE;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<I_CmsToggleHandler> getAssociatedType() {

        return CmsToggleEvent.getType();
    }

    /**
     * Returns if the source has been activated.<p>
     *
     * @return if the source has been activated
     */
    public boolean isActivated() {

        return m_isActivated;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void dispatch(I_CmsToggleHandler handler) {

        handler.onToggle(this);

    }

}

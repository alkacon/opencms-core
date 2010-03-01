/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/Attic/CmsFormEvent.java,v $
 * Date   : $Date: 2010/03/01 10:04:33 $
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

package org.opencms.gwt.client;

import com.google.gwt.event.shared.GwtEvent;

/** 
 * General form event to handle all form related events.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsFormEvent extends GwtEvent<I_CmsFormHandler> {

    /**
     * The action type performed on the form.<p>
     */
    public enum ActionType {

        /** Disable action. */
        DISABLE,

        /** Submit action. */
        SUBMIT,

        /** Response action (The response has been received). */
        RESPONSE,

        /** Validate action. */
        VALIDATE,

        /** Value change action. */
        VALUECHANGE
    }

    /** Handler type. */
    private static Type<I_CmsFormHandler> TYPE;

    /** The action type of the event. */
    private ActionType m_actionType;

    /**
     * Creates a new open event.<p>
     * 
     * @param actionType the action type of the form event
     */
    protected CmsFormEvent(ActionType actionType) {

        m_actionType = actionType;
    }

    /**
     * Fires a toggle event on all registered handlers in the handler manager.If no
     * such handlers exist, this method will do nothing.<p>
     * 
     * @param source the event source
     * @param actionType the action type
     */
    public static void fire(I_CmsHasFormHandler source, ActionType actionType) {

        if (TYPE != null) {
            CmsFormEvent event = new CmsFormEvent(actionType);
            source.fireEvent(event);
        }
    }

    /**
     * Gets the type associated with this event.
     * 
     * @return returns the handler type
     */
    public static com.google.gwt.event.shared.GwtEvent.Type<I_CmsFormHandler> getType() {

        if (TYPE == null) {
            TYPE = new Type<I_CmsFormHandler>();
        }
        return TYPE;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<I_CmsFormHandler> getAssociatedType() {

        return CmsFormEvent.getType();
    }

    /**
     * Returns if the source has been activated.<p>
     * 
     * @return if the source has been activated
     */
    public ActionType getActionType() {

        return m_actionType;
    }

    /**
     * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void dispatch(I_CmsFormHandler handler) {

        handler.onFormEvent(this);

    }

}

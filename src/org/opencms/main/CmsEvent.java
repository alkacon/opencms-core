/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsEvent.java,v $
 * Date   : $Date: 2005/06/22 10:38:20 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.main;

import java.util.Map;

/**
 * Event class for OpenCms for system wide events that are thrown by various 
 * operations (e.g. publishing) and can be catched and processed by 
 * classes that implement the {@link I_CmsEventListener} interface.<p>
 *
 * @author  Alexander Kandzior 
 *
 * @version $Revision: 1.7 $
 * @since FLEX alpha 1
 * 
 * @see I_CmsEventListener
 */
public class CmsEvent {    

    /** The event data associated with this event. */
    private Map m_data;

    /** The event type this instance represents. */
    private Integer m_type;

    /**
     * Construct a new CmsEvent with the specified parameters.<p>
     * 
     * The event data <code>Map</code> provides a facility to 
     * pass objects with the event that contain information about 
     * the event environment. For example, if the event is of type
     * {@link I_CmsEventListener#EVENT_LOGIN_USER} the Map contains 
     * a single object with the key <code>"data"</code> and a value 
     * that is the OpenCms user object that represents the user that just logged in.<p>
     * 
     * @param type event type
     * @param data event data
     *
     * @see I_CmsEventListener
     */
    public CmsEvent(int type, Map data) {

        this.m_type = new Integer(type);
        this.m_data = data;
    }

    /**
     * Provides access to the event data that was passed with this event.<p>
     * 
     * @return the event data of this event
     */
    public Map getData() {

        return m_data;
    }

    /**
     * Provides access to the event type that was passed with this event.<p>
     * 
     * Event types of the core OpenCms classes are defined in {@link I_CmsEventListener}.
     * For your extensions, you should define them in a central class 
     * or interface as public member variables. Make sure the integer values 
     * do not confict with the values from the core classes.<p>
     * 
     * @return the event type of this event
     * 
     * @see I_CmsEventListener
     */
    public int getType() {

        return m_type.intValue();
    }

    /**
     * Provides access to the event type as Integer.<p>
     * 
     * @return the event type of this event as Integer
     */
    public Integer getTypeInteger() {

        return m_type;
    }

    /**
     * Return a String representation of this CmsEvent.<p>
     *
     * @return a String representation of this event
     */
    public String toString() {

        return "CmsEvent['" + m_type + "']";
    }
}
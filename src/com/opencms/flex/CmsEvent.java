/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/Attic/CmsEvent.java,v $
 * Date   : $Date: 2002/07/01 11:54:48 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * First created on 18. April 2002, 15:00
 */


package com.opencms.flex;

import com.opencms.file.CmsObject;

/**
 * Description of the class CmsEvent here.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class CmsEvent extends java.util.EventObject {

    /** The CmsObject on which this event occurred */
    private CmsObject m_cms = null;

    /** The event data associated with this event */
    private Object m_data = null;

    /** The event type this instance represents */
    private int m_type = -1;

    /**
     * Construct a new CmsEvent with the specified parameters.
     *
     * @param cms CmsObject on which this event occurred
     * @param type Event type
     * @param data Event data
     */
    public CmsEvent(CmsObject cms, int type, Object data) {
        super(cms);
        this.m_cms = cms;
        this.m_type = type;
        this.m_data = data;

    }

    /**
     * Provides access to the event data.
     *
     * @return The event data of this event
     */
    public Object getData() {
        return (m_data);
    }

    /**
     * Provides access to the CmsObject.
     *
     * @return The CmsObject on which this event occurred
     */
    public CmsObject getCmsObject() {
        return (m_cms);
    }

    /**
     * Provides access to the event type.
     *
     * @return The event type of this event
     */
    public int getType() {
        return (m_type);
    }

    /**
     * Method overloaded from the standard Object API.
     *
     * @return A string representation of this event
     */
    public String toString() {
        return ("CmsEvent['" + m_cms + "','" + m_type + "']");
    }

}

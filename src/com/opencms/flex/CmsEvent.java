/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/Attic/CmsEvent.java,v $
 * Date   : $Date: 2003/08/15 17:38:04 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package com.opencms.flex;

import com.opencms.file.CmsObject;

/**
 * Event class for OpenCms for system wide events that are thrown by various 
 * operations (e.g. publishing) and can be catched and processed by 
 * classes that implement the {@link I_CmsEventListener} interface.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.7 $
 * @since FLEX alpha 1
 * 
 * @see I_CmsEventListener
 */
public class CmsEvent extends java.util.EventObject {
    
    /** The CmsObject on which this event occurred */
    private CmsObject m_cms;

    /** The event data associated with this event */
    private java.util.Map m_data;

    /** The event type this instance represents */
    private Integer m_type;
    
    /** Boolean whether this event should be clustered */
    private boolean m_isClusterEvent;

    /**
     * Construct a new CmsEvent with the specified parameters, 
     * this constructor just calls <code>this(cms, type, data, false)</code>.<p>
     *
     * @param cms CmsObject on which this event occurred
     * @param type event type
     * @param data event data
     */
    public CmsEvent(CmsObject cms, int type, java.util.Map data) {
        this(cms, type, data, false);
    }
    
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
     * If <code>isClusterEvent</code> is <code>true</code>,
     * the event should be forwarded to all servers in the OpenCms cluster.
     * If it is <code>false</code>, is is important only for server
     * running this instance of OpenCms. 
     *
     * @param cms CmsObject on which this event occurred
     * @param type event type
     * @param data event data
     * @param isClusterEvent must be <code>true</code> if this event should be forwarded 
     *         to the other servers in the cluster
     * 
     * @see I_CmsEventListener
     */    
    public CmsEvent(CmsObject cms, int type, java.util.Map data, boolean isClusterEvent) {
        super(cms);
        
        this.m_cms = cms;
        this.m_type = new Integer(type);
        this.m_data = data;
        this.m_isClusterEvent = isClusterEvent;
    }

    /**
     * Provides access to the event data that was passed with this event.<p>
     * 
     * @return the event data of this event
     */
    public java.util.Map getData() {
        return m_data;
    }

    /**
     * Provides access to the CmsObject that was passed with this event.<p>
     *
     * @return the CmsObject on which this event occurred
     */
    public CmsObject getCmsObject() {
        return m_cms;
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
        return "CmsEvent['" + m_cms + "','" + m_type + "']";
    }

    /**
     * Set the boolean flag whether this event should be forwarded 
     * to the other servers in the cluster.<p>
     *
     * @param value <code>true</code> if this event should be forwarded to the other 
     *         servers in the cluster, <code>false</code> otherwise
     */
    public void setClusterEvent(boolean value) {
        this.m_isClusterEvent = value;
    }
    
    /**
     * Check whether this event should be forwarded to the other servers 
     * in the cluster or not.<p>
     *
     * @return <code>true</code> if this event should be forwarded to the other servers 
     *          in the cluster, <code>false</code> otherwise
     */
    public boolean isClusterEvent() {
        return this.m_isClusterEvent;
    }
}

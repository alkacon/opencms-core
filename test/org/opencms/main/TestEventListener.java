/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/main/TestEventListener.java,v $
 * Date   : $Date: 2005/10/05 10:07:45 $
 * Version: $Revision: 1.1.2.1 $
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

import java.util.ArrayList;
import java.util.List;

/**
 * Simple event listener for test purposes.
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.1
 */
public class TestEventListener implements I_CmsEventListener {

    /** The list of recieved events. */
    List m_events;

    /**
     * Default constructor.<p>
     */
    public TestEventListener() {

        m_events = new ArrayList();
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        m_events.add(event);
    }

    /**
     * Returns a list of all recieved events.<p>
     * 
     * @return a list of all recieved events
     */
    public List getEvents() {

        return m_events;
    }

    /**
     * Returns <code>true</code> in case this listener has recieved at last one event of the given type.<p>
     * 
     * @param event the event id to check
     * 
     * @return <code>true</code> in case this listener has recieved at last one event of the given type
     */
    public boolean hasRecievedEvent(int event) {

        return m_events.contains(new CmsEvent(event, null));
    }
}
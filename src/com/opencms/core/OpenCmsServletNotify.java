/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCmsServletNotify.java,v $
 * Date   : $Date: 2003/08/14 15:37:23 $
 * Version: $Revision: 1.14 $
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

package com.opencms.core;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * Implementation of the HttpSessionBindingListener interface. <br>
 * 
 * The OpenCmsServletNotify Object is notified when it is bound or unbound to
 * a HTTPSession. It is required to inform the OpemCms that a session is destroyed
 * and must be removed from the CmsCoreSession storage.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.14 $
 */
public class OpenCmsServletNotify implements HttpSessionBindingListener {
    
    /** String to identify the notif session attribute in the session */
    public static final String C_NOTIFY_ATTRIBUTE = "__OpenCmsServletNotify";
    
    String m_id = null;
    CmsCoreSession m_sessionStorage = null;
    
    /**
     * Constructor, creates a new OpenCmsServletNotify object.
     * 
     * @param id The session Id to which this object is bound to.
     * @param sessionStorage The reference to the session strorage.
     */
    public OpenCmsServletNotify(String id, CmsCoreSession sessionStorage) {
        m_id = id;
        m_sessionStorage = sessionStorage;
    }
    
    /**
     * Called when the listener is bound to a session.<p>
     * 
     * @param event the HttpSessionBindingEvent
     */
    public void valueBound(HttpSessionBindingEvent event) {            
        // nothing to be done here
    }
    
    /**
     * Called when the listener is unbound from to a session,
     * in which case the OpenCms internal session storage must be updated.<p>
     * 
     * @param event the HttpSessionBindingEvent
     */
    public void valueUnbound(HttpSessionBindingEvent event) {
        m_sessionStorage.deleteUser(m_id);
    }
}

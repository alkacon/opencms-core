/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/OpenCmsServletNotify.java,v $
* Date   : $Date: 2002/12/06 23:16:51 $
* Version: $Revision: 1.12 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
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
*/

package com.opencms.core;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * Implementation of the HttpSessionBindingListener interface. <br>
 * 
 * The OpenCmsServletNotify Object is notified when it is bound or unbound to
 * a HTTPSession. It is required to inform the OpemCms that a session is destroyed
 * and must be removed from the CmsCoreSession storage.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.12 $ $Date: 2002/12/06 23:16:51 $  
 */
class OpenCmsServletNotify implements HttpSessionBindingListener {
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
     * Called when the listener is bound to a session.
     * 
     * @param event The HttpSessionBindingEvent
     */
    public void valueBound(HttpSessionBindingEvent event) {
        
    
    // nothing is required to be done here.
    }
    
    /**
     * Called when the listener is unbound from to a session.
     * 
     * @param event The HttpSessionBindingEvent
     */
    public void valueUnbound(HttpSessionBindingEvent event) {
        m_sessionStorage.deleteUser(m_id);
    }
}

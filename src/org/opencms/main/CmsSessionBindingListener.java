/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/Attic/CmsSessionBindingListener.java,v $
 * Date   : $Date: 2004/02/21 17:11:42 $
 * Version: $Revision: 1.2 $
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

package org.opencms.main;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * Implementation of the HttpSessionBindingListener interface. <br>
 * 
 * The CmsSessionBindingListener Object is notified when it is bound or unbound to
 * a HTTPSession. It is required to inform the OpemCms that a session is destroyed
 * and must be removed from the CmsSessionInfoManager storage.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class CmsSessionBindingListener implements HttpSessionBindingListener {
    
    /** String to identify the notif session attribute in the session */
    public static final String C_NOTIFY_ATTRIBUTE = "__OpenCmsServletNotify";
    
    String m_id = null;
    
    /**
     * Constructor, creates a new CmsSessionBindingListener object.
     * 
     * @param id The session Id to which this object is bound to.
     */
    public CmsSessionBindingListener(String id) {
        m_id = id;
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
        OpenCms.getSessionInfoManager().removeUserSession(m_id);
    }
}

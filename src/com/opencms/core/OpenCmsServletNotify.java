package com.opencms.core;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Implementation of the HttpSessionBindingListener interface. <br>
 * 
 * The OpenCmsServletNotify Object is notified when it is bound or unbound to
 * a HTTPSession. It is required to inform the OpemCms that a session is destroyed
 * and must be removed from the CmsSession storage.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/01/14 15:10:22 $  
 */
 class OpenCmsServletNotify implements HttpSessionBindingListener {
    
    String m_id=null;
    CmsSession m_sessionStorage=null;
 
    
    /**
     * Constructor, creates a new OpenCmsServletNotify object.
     * 
     * @param id The session Id to which this object is bound to.
     * @param sessionStorage The reference to the session strorage.
     */
     OpenCmsServletNotify(String id,CmsSession sessionStorage) {
        m_id=id;
        m_sessionStorage=sessionStorage;
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

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/Attic/OpenCmsSessionManager.java,v $
 * Date   : $Date: 2004/06/14 14:25:56 $
 * Version: $Revision: 1.4 $
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

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.4 $
 * @since 5.1
 */
public class OpenCmsSessionManager implements HttpSessionListener {

    /** Counter for the active sessions. */
    private static int m_totalSessions;
    
    /** Counter for all sessions created so far. */
    private static int m_currentSessions;
    
    /**
     * Returns the number of active sessions.<p>
     * 
     * @return the number of active sessions
     */
    public synchronized int getTotalSessions() {
        return m_totalSessions;
    }

    /**
     * Returns the number of current sessions.<p>
     * 
     * @return the number of current sessions
     */
    public synchronized int getCurrentSessions() {
        return m_currentSessions;
    }

    /**
     * Creates a new instance of the OpenCms session manager.<p>
     */
    public OpenCmsSessionManager() {
        super();
        
        // register with the OpenCms context 
        OpenCmsCore.getInstance().setSessionManager(this);
        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Instance of OpenCmsSessionManager was created");
        }   
    }

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    public synchronized void sessionCreated(HttpSessionEvent event) {
        m_currentSessions = (m_currentSessions <= 0)?1:(m_currentSessions + 1);
        m_totalSessions++;
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Session created - Total: " + m_totalSessions + " Current: " + m_currentSessions);
        }           
    }

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    public synchronized void sessionDestroyed(HttpSessionEvent event) {
        m_currentSessions = (m_currentSessions <= 0)?0:(m_currentSessions - 1);
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Session destroyed - Total: " + m_totalSessions + " Current: " + m_currentSessions);
        }           
    }
}

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsSession.java,v $
* Date   : $Date: 2003/10/28 13:28:41 $
* Version: $Revision: 1.31 $
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

import org.opencms.main.OpenCms;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpSession;

/**
 * Implements the I_CmsSession interface and is required by the OpenCms session
 * handling mechanism.<p>
 * 
 * This <code>CmsSession</code> object should be used instead of the 
 * <code>HttpSession</code>. 
 *
 * @author Michael Emmerich
 *
 * @version $Revision: 1.31 $ $Date: 2003/10/28 13:28:41 $
 */
public class CmsSession implements I_CmsSession {

    /**
     * The original HttpSession
     */
    private HttpSession m_session;

    /**
     * The sessiondata.
     */
    private Hashtable m_sessionData;

    /**
     * Constructs a new CmsSession based on a HttpSession.
     *
     * @param originalSession the original session to use.
     */
    public CmsSession(HttpSession originalSession) {
        m_session = originalSession;
        m_sessionData = (Hashtable)m_session.getAttribute(I_CmsConstants.C_SESSION_DATA);

        // if there is no session-data, create a new one.
        if(m_sessionData == null) {
            m_sessionData = new Hashtable();
            m_session.setAttribute(I_CmsConstants.C_SESSION_DATA, m_sessionData);
        }
    }

    /**
     * Gets a value from the session.
     *
     * @param name the key for the value.
     * @return the object associated with this key.
     */
    public Object getValue(String name) {
        return m_sessionData.get(name);
    }

    /**
     * Gets the names of all values stored in the session.
     *
     * @return String array containing all value names.
     */
    public String[] getValueNames() {
        String[] name = new String[m_sessionData.size()];
        Enumeration enu = m_sessionData.keys();
        for(int i = 0;i < m_sessionData.size();i++) {
            name[i] = (String)enu.nextElement();
        }
        return name;
    }

    /**
     * Puts a value into the session.
     *
     * @param name the key for the value.
     * @param value an object to be stored in the session.
     */
    public void putValue(String name, Object value) {
        m_sessionData.put(name, value);

        try {
            // indicate, that the session should be stored after the request.
            m_session.setAttribute(I_CmsConstants.C_SESSION_IS_DIRTY, new Boolean(true));
        } catch (Exception exc) {
            if(OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error marking session as dirty", exc);
            }
        }
    }

    /**
     * Removes a value from the session.
     *
     * @param name the key for the value to be removed.
     */
    public void removeValue(String name) {
        m_sessionData.remove(name);

        // indicate, that the session should be stored after the request.
        m_session.setAttribute(I_CmsConstants.C_SESSION_IS_DIRTY, new Boolean(true));
    }
    
    /**
     * Gets the Session Id.<p>
     * 
     * @return session id
     */
    public String getId(){
        return m_session.getId();
    }
    
    
    /**
     * Invalidates the session.
     */
    public void invalidate() {
        if (m_session != null) m_session.invalidate();
        // if there is session-data, invalidate it as well
        if(m_sessionData != null) {
            m_sessionData = new Hashtable();
        }    
    }        
}

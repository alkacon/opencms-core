/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/core/Attic/CmsSession.java,v $
* Date   : $Date: 2005/05/17 13:47:28 $
* Version: $Revision: 1.1 $
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
 * @version $Revision: 1.1 $ $Date: 2005/05/17 13:47:28 $
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsSession implements I_CmsSession {

    /**
     * The original HttpSession.
     */
    private HttpSession m_session;

    /**
     * Constructs a new CmsSession based on a HttpSession.
     *
     * @param originalSession the original session to use.
     */
    public CmsSession(HttpSession originalSession) {
        m_session = originalSession;
    }

    /**
     * Gets a value from the session.
     *
     * @param name the key for the value.
     * @return the object associated with this key.
     */
    public Object getValue(String name) {
        return m_session.getAttribute(name);
    }

    /**
     * Puts a value into the session.
     *
     * @param name the key for the value.
     * @param value an object to be stored in the session.
     */
    public void putValue(String name, Object value) {
        m_session.setAttribute(name, value);
    }

    /**
     * Removes a value from the session.
     *
     * @param name the key for the value to be removed.
     */
    public void removeValue(String name) {
        m_session.removeAttribute(name);
    }
    
    /**
     * Gets the Session Id.<p>
     * 
     * @return session id
     */
    public String getId() {
        return m_session.getId();
    }
    
    
    /**
     * Invalidates the session.
     */
    public void invalidate() {
        if (m_session != null) {
            m_session.invalidate();
        }
    }        
}

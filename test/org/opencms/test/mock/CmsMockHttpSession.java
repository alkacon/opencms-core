/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.test.mock;

import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * Minimal in-memory {@link HttpSession} mock for OpenCms tests, holding session attributes (e.g. the
 * OpenCms session id and the client token written by the {@link org.opencms.main.CmsSessionManager})
 * so the real session based login flow works without a servlet container.<p>
 *
 * Methods that the OpenCms request flow does not use throw {@link UnsupportedOperationException} so
 * an accidental dependency surfaces instead of returning a silently wrong value.<p>
 *
 * @see CmsMockHttpServletRequest
 */
public class CmsMockHttpSession implements HttpSession {

    /** The session attributes, insertion ordered for deterministic iteration. */
    private final Map<String, Object> m_attributes = new LinkedHashMap<String, Object>();

    /** The creation time. */
    private final long m_creationTime = System.currentTimeMillis();

    /** The session id. */
    private final String m_id = new CmsUUID().toString();

    /** The maximum inactive interval in seconds. */
    private int m_maxInactiveInterval = 1800;

    /** Flag indicating whether the session is still valid (not invalidated). */
    private boolean m_valid = true;

    /** Flag indicating whether the session is new (not yet seen by the client). */
    private boolean m_new = true;

    /**
     * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String name) {

        return m_attributes.get(name);
    }

    /**
     * @see javax.servlet.http.HttpSession#getAttributeNames()
     */
    @Override
    public Enumeration<String> getAttributeNames() {

        return Collections.enumeration(m_attributes.keySet());
    }

    /**
     * @see javax.servlet.http.HttpSession#getCreationTime()
     */
    @Override
    public long getCreationTime() {

        return m_creationTime;
    }

    /**
     * @see javax.servlet.http.HttpSession#getId()
     */
    @Override
    public String getId() {

        return m_id;
    }

    /**
     * @see javax.servlet.http.HttpSession#getLastAccessedTime()
     */
    @Override
    public long getLastAccessedTime() {

        return m_creationTime;
    }

    /**
     * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
     */
    @Override
    public int getMaxInactiveInterval() {

        return m_maxInactiveInterval;
    }

    /**
     * @see javax.servlet.http.HttpSession#getServletContext()
     */
    @Override
    public ServletContext getServletContext() {

        throw new UnsupportedOperationException("getServletContext");
    }

    /**
     * @see javax.servlet.http.HttpSession#getSessionContext()
     *
     * @deprecated as in the servlet API
     */
    @Override
    @Deprecated
    public HttpSessionContext getSessionContext() {

        throw new UnsupportedOperationException("getSessionContext");
    }

    /**
     * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
     *
     * @deprecated as in the servlet API
     */
    @Override
    @Deprecated
    public Object getValue(String name) {

        return getAttribute(name);
    }

    /**
     * @see javax.servlet.http.HttpSession#getValueNames()
     *
     * @deprecated as in the servlet API
     */
    @Override
    @Deprecated
    public String[] getValueNames() {

        return m_attributes.keySet().toArray(new String[0]);
    }

    /**
     * @see javax.servlet.http.HttpSession#invalidate()
     */
    @Override
    public void invalidate() {

        m_attributes.clear();
        m_valid = false;
    }

    /**
     * @see javax.servlet.http.HttpSession#isNew()
     */
    @Override
    public boolean isNew() {

        return m_new;
    }

    /**
     * Returns whether this session is still valid, i.e. has not been invalidated.<p>
     *
     * @return <code>true</code> if the session is still valid
     */
    public boolean isValid() {

        return m_valid;
    }

    /**
     * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
     *
     * @deprecated as in the servlet API
     */
    @Override
    @Deprecated
    public void putValue(String name, Object value) {

        setAttribute(name, value);
    }

    /**
     * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(String name) {

        m_attributes.remove(name);
    }

    /**
     * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
     *
     * @deprecated as in the servlet API
     */
    @Override
    @Deprecated
    public void removeValue(String name) {

        removeAttribute(name);
    }

    /**
     * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String name, Object value) {

        m_new = false;
        if (value == null) {
            m_attributes.remove(name);
        } else {
            m_attributes.put(name, value);
        }
    }

    /**
     * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
     */
    @Override
    public void setMaxInactiveInterval(int interval) {

        m_maxInactiveInterval = interval;
    }

    /**
     * Marks this session as no longer new; used by {@link CmsMockHttpServletRequest} when the session
     * is read back on a subsequent request.<p>
     */
    void markNotNew() {

        m_new = false;
    }
}

/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.webdav;

import org.opencms.repository.I_CmsRepositorySession;

import java.util.ArrayList;
import java.util.List;

import org.apache.jackrabbit.webdav.DavSession;

/**
 * DavSession implementation for Jackrabbit WebDAV, mostly just a wrapper for I_CmsRepositorySession.
 */
public class CmsDavSession implements DavSession {

    /** The underlying repository session. */
    private I_CmsRepositorySession m_repoSession;

    /** Lock tokens. */
    private List<String> m_lockTokens = new ArrayList<>();

    /**
     * Creates a new DAV session.
     *
     * @param session the underlying repository session
     */
    public CmsDavSession(I_CmsRepositorySession session) {

        m_repoSession = session;

    }

    /**
     * @see org.apache.jackrabbit.webdav.DavSession#addLockToken(java.lang.String)
     */
    public void addLockToken(String token) {

        m_lockTokens.add(token);
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavSession#addReference(java.lang.Object)
     */
    public void addReference(Object reference) {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavSession#getLockTokens()
     */
    public String[] getLockTokens() {

        return m_lockTokens.toArray(new String[] {});
    }

    /**
     * Gets the OpenCms repository session.
     *
     * @return the OpenCms repository session
     */
    public I_CmsRepositorySession getRepositorySession() {

        return m_repoSession;

    }

    /**
     * @see org.apache.jackrabbit.webdav.DavSession#removeLockToken(java.lang.String)
     */
    public void removeLockToken(String token) {

        m_lockTokens.remove(token);
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavSession#removeReference(java.lang.Object)
     */
    public void removeReference(Object reference) {

        throw new UnsupportedOperationException();
    }

}

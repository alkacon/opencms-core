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

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.lock.LockManager;

/**
 * The resource factory.
 */
public class CmsDavResourceFactory implements DavResourceFactory {

    private LockManager m_lockManager;

    /**
     * @see org.apache.jackrabbit.webdav.DavResourceFactory#createResource(org.apache.jackrabbit.webdav.DavResourceLocator, org.apache.jackrabbit.webdav.DavServletRequest, org.apache.jackrabbit.webdav.DavServletResponse)
     */
    public DavResource createResource(
        DavResourceLocator locator,
        DavServletRequest request,
        DavServletResponse response)
    throws DavException {

        return new CmsDavResource(locator, this, (CmsDavSession)request.getDavSession(), m_lockManager);

    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResourceFactory#createResource(org.apache.jackrabbit.webdav.DavResourceLocator, org.apache.jackrabbit.webdav.DavSession)
     */
    public DavResource createResource(DavResourceLocator locator, DavSession session) throws DavException {

        return new CmsDavResource(locator, this, (CmsDavSession)session, m_lockManager);
    }

    public void setLockManager(LockManager lockManager) {

        m_lockManager = lockManager;
    }

}

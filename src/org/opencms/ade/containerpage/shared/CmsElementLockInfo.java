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

package org.opencms.ade.containerpage.shared;

import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains lock information for a container page element.
 */
public class CmsElementLockInfo implements IsSerializable {

    /** User id of the lock owner. */
    private CmsUUID m_lockOwner;

    /** True if this is a publish lock. */
    private boolean m_isPublishLock;

    /**
     * Creates a new instance.
     *
     * @param lockOwner the lock owner
     * @param isPublishLock the publish lock status
     */
    public CmsElementLockInfo(CmsUUID lockOwner, boolean isPublishLock) {

        super();
        m_lockOwner = lockOwner;
        m_isPublishLock = isPublishLock;
    }

    /**
     * Empty constructor for serialization.
     */
    protected CmsElementLockInfo() {

        // empty
    }

    /**
     * Gets the lock owner id.
     *
     * @return the lock owner id
     */
    public CmsUUID getLockOwner() {

        return m_lockOwner;
    }

    /**
     * Returns true if the resource is locked.
     *
     * @return true if the resource is locked
     */
    public boolean isLocked() {

        return (m_lockOwner != null) && !m_lockOwner.equals(CmsUUID.getNullUUID());
    }

    /**
     * Returns true if the resource is a publish lock.
     *
     * @return true if the resource is a publish lock
     */
    public boolean isPublishLock() {

        return m_isPublishLock;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "LockInfo(" + m_lockOwner + "," + m_isPublishLock + ")";
    }

}

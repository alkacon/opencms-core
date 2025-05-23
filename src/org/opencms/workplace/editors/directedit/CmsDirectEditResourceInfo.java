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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors.directedit;

import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLock;

/**
 * Contains information about a resource that is direct edited.<p>
 *
 * For example, the information in this class allows implementations
 * of a {@link I_CmsDirectEditProvider} to render HTML
 * with extended information about the resource displayed on the buttons.<p>
 *
 * @since 6.2.3
 */
public class CmsDirectEditResourceInfo {

    /** Constant for inactive permissions without further resource info. */
    public static final CmsDirectEditResourceInfo INACTIVE = new CmsDirectEditResourceInfo(
        CmsDirectEditPermissions.INACTIVE);

    /** The lock on the direct edit resource. */
    CmsLock m_lock;

    /** The direct edit permissions of the resource. */
    CmsDirectEditPermissions m_permissions;

    /** The resource that is to be direct edited. */
    CmsResource m_resource;

    /**
     * Creates a new direct edit resource info container without any
     * specific information about the resource to be direct edited.<p>
     *
     * @param permissions the direct edit permissions of the resource
     */
    public CmsDirectEditResourceInfo(CmsDirectEditPermissions permissions) {

        this(permissions, null, null);
    }

    /**
     * Creates a new direct edit resource info container.<p>
     *
     * @param permissions the direct edit permissions of the resource
     * @param resource the resource that is to be direct edited
     * @param lock the lock on the direct edit resource
     */
    public CmsDirectEditResourceInfo(CmsDirectEditPermissions permissions, CmsResource resource, CmsLock lock) {

        m_permissions = permissions;
        m_resource = resource;
        m_lock = lock;
    }

    /**
     * Returns the lock on the direct edit resource.<p>
     *
     * This may be <code>null</code> in case the result is {@link #INACTIVE}.<p>
     *
     * @return the lock on the direct edit resource
     */
    public CmsLock getLock() {

        return m_lock;
    }

    /**
     * Returns the direct edit permissions of the resource.<p>
     *
     * The result is ensured not to be <code>null</code>.<p>
     *
     * @return the direct edit permissions of the resource
     */
    public CmsDirectEditPermissions getPermissions() {

        return m_permissions;
    }

    /**
     * Returns the resource that is to be direct edited.<p>
     *
     * This may be <code>null</code> in case the result is {@link #INACTIVE}.<p>
     *
     * @return the resource that is to be direct edited
     */
    public CmsResource getResource() {

        return m_resource;
    }
}
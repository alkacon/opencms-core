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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The permission info bean.<p>
 */
public class CmsPermissionInfo implements IsSerializable {

    /** If view is permitted. */
    private boolean m_hasViewPermission;

    /** if write is permitted. */
    private boolean m_hasWritePermission;

    /** The no edit reason. */
    private String m_noEditReason;

    /**
     * Constructor.<p>
     *
     * @param hasViewPermission if view is permitted
     * @param hasWritePermission is write is permitted
     * @param noEditReason the no edit reason
     */
    public CmsPermissionInfo(boolean hasViewPermission, boolean hasWritePermission, String noEditReason) {

        m_hasViewPermission = hasViewPermission;
        m_hasWritePermission = hasWritePermission;
        m_noEditReason = noEditReason;
    }

    /**
     * Constructor for serialization only.<p>
     */
    protected CmsPermissionInfo() {

        // nothing to do
    }

    /**
     * Returns the noEditReason.<p>
     *
     * @return the noEditReason
     */
    public String getNoEditReason() {

        return m_noEditReason;
    }

    /**
     * Returns the hasViewPermission.<p>
     *
     * @return the hasViewPermission
     */
    public boolean hasViewPermission() {

        return m_hasViewPermission;
    }

    /**
     * Returns the hasWritePermission.<p>
     *
     * @return the hasWritePermission
     */
    public boolean hasWritePermission() {

        return m_hasWritePermission;
    }

    /**
     * Sets the no edit reason.<p>
     *
     * @param noEditReason the no edit reason
     */
    public void setNoEditReason(String noEditReason) {

        m_noEditReason = noEditReason;
    }

}

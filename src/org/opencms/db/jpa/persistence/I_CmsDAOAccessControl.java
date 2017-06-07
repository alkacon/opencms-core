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

package org.opencms.db.jpa.persistence;

/**
 * This interface declares the getters and setters for the access control data access objects.<p>
 *
 * @since 8.0.0
 *
 * @see org.opencms.security.CmsAccessControlEntry
 */
public interface I_CmsDAOAccessControl {

    /**
     * Returns the allowed permissions of the permission set.<p>
     *
     * @return the allowed permissions as bitset
     */
    int getAccessAllowed();

    /**
     * Returns the denied permissions of the permission set.<p>
     *
     * @return the denied permissions as bitset.
     */
    int getAccessDenied();

    /**
     * Returns the flags of the access control entry.<p>
     *
     * @return the flags as bitset
     */
    int getAccessFlags();

    /**
     * Returns the id of the principal assigned with this access control entry.<p>
     *
     * @return the principal id
     */
    String getPrincipalId();

    /**
     * Returns the id of the resource assigned with this access control entry.<p>
     *
     * @return the resource id
     */
    String getResourceId();

    /**
     * Sets the allowed permissions for the permission set.<p>
     *
     * @param accessAllowed the allowed permissions as bitset
     */
    void setAccessAllowed(int accessAllowed);

    /**
     * Sets the denied permission for the permission set.<p>
     *
     * @param accessDenied the denied permissions as bitset
     */
    void setAccessDenied(int accessDenied);

    /**
     * Sets the flags of the access control entry.<p>
     *
     * @param accessFlags the flags as bitset
     */
    void setAccessFlags(int accessFlags);

    /**
     * Sets the principal id assigned with this access control entry.<p>
     *
     * @param principalId the principal id
     */
    void setPrincipalId(String principalId);

    /**
     * Sets the resource id assigned with this access control entry.<p>
     *
     * @param resourceId the resource id
     */
    void setResourceId(String resourceId);

}
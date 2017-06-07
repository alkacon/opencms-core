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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.security;

import org.opencms.util.CmsUUID;

import java.security.Principal;

/**
 * Representation of an identity in the cms (currently user or group),
 * used to define permissions on a resource.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsPrincipal extends Principal {

    /** Upper limit for core flags, any principal object with flags greater than this value will be filtered out. */
    int FLAG_CORE_LIMIT = 65536; // 2^16

    /** This flag is set for disabled principals in the database. */
    int FLAG_DISABLED = 1;

    /** This flag is set for enabled principals in the database. */
    int FLAG_ENABLED = 0;

    /** Flag to indicate a role group. */
    int FLAG_GROUP_ROLE = 1048576; // 2^20 >> FLAG_CORE_LIMIT

    /**
     * Flag to indicate a virtual group role, after this bit we need to encode a number between 0 and
     * <code>{@link CmsRole#getSystemRoles()}.size()-1</code> so we will need up to 4 more bits.
     */
    int FLAG_GROUP_VIRTUAL = 1024; // 2^10 << FLAG_CORE_LIMIT

    /** Flag to indicate a user is not able to manage himself. */
    int FLAG_USER_MANAGED = 2;

    /** Flag to indicate a user is a webuser. */
    int FLAG_USER_WEBUSER = 32768; //2^15

    /** Identifier for group principals. */
    String PRINCIPAL_GROUP = "GROUP";

    /** Identifier for user principals. */
    String PRINCIPAL_USER = "USER";

    /**
     * Checks if the provided principal name is valid and can be used as an argument value
     * for {@link #setName(String)}.<p>
     *
     * @param name the principal name to check
     */
    void checkName(String name);

    /**
     * Compares the given object with this principal.<p>
     *
     * @param obj object to compare
     * @return true if the object is equal
     */
    boolean equals(Object obj);

    /**
     * Returns the description of this principal.<p>
     *
     * @return the description of this principal
     */
    String getDescription();

    /**
     * Returns the flags of this principal.<p>
     *
     * The principal flags are used to store special information about the
     * principals state encoded bitwise. Usually the flags int value should not
     * be directly accessed. Utility methods like <code>{@link #isEnabled()}</code>
     * provide a much easier way to access the information contained in the flags.<p>
     *
     * @return the flags of this principal
     */
    int getFlags();

    /**
     * Returns the unique id of this principal.<p>
     *
     * @return the unique id of this principal
     */
    CmsUUID getId();

    /**
     * Returns the unique name of this principal.<p>
     *
     * @return the unique name of this principal
     */
    String getName();

    /**
     * Returns the fully qualified name of the associated organizational unit.<p>
     *
     * @return the fully qualified name of the associated organizational unit
     */
    String getOuFqn();

    /**
     * Returns this principals unique name prefixed with it's type.<p>
     *
     * The type prefix can either be <code>{@link I_CmsPrincipal#PRINCIPAL_GROUP}.</code>
     * (for groups) or <code>{@link I_CmsPrincipal#PRINCIPAL_USER}.</code> (for users).<p>
     *
     * @return this principals unique name prefixed with this principals type
     */
    String getPrefixedName();

    /**
     * Returns the simple name of this organizational unit.
     *
     * @return the simple name of this organizational unit.
     */
    String getSimpleName();

    /**
     * Returns the hash code of this object.<p>
     *
     * @return the hash code
     */
    int hashCode();

    /**
     * Returns <code>true</code> if this principal is enabled.<p>
     *
     * A principal may be disabled in order to disable it, for example to prevent
     * logins of a user. If a principal is just disabled but not deleted,
     * the credentials of the principal in the VFS are still valid.<p>
     *
     * @return <code>true</code> if this principal is enabled
     */
    boolean isEnabled();

    /**
     * Returns <code>true</code> if this principal is of type <code>{@link org.opencms.file.CmsGroup}</code>.<p>
     *
     * @return <code>true</code> if this principal is of type <code>{@link org.opencms.file.CmsGroup}</code>
     */
    boolean isGroup();

    /**
     * Returns <code>true</code> if this principal is of type <code>{@link org.opencms.file.CmsUser}</code>.<p>
     *
     * @return <code>true</code> if this principal is of type <code>{@link org.opencms.file.CmsUser}</code>
     */
    boolean isUser();

    /**
     * Sets the description of this principal.<p>
     *
     * @param description the principal description to set
     */
    void setDescription(String description);

    /**
     * Enables (or disables) this principal, depending on the given status.<p>
     *
     * @param enabled the principal status to set
     */
    void setEnabled(boolean enabled);

    /**
     * Sets this principals flags to the specified value.<p>
     *
     * The principal flags are used to store special information about the
     * principals state encoded bitwise. Usually the flags integer value should not
     * be directly accessed. Utility methods like <code>{@link #setEnabled(boolean)}</code>
     * provide a much easier way to manipulate the information contained in the flags.<p>
     *
     * @param value the value to set this principals flags to
     */
    void setFlags(int value);

    /**
     * Sets the unique name of this principal.<p>
     *
     * @param name the unique name of this principal to set
     */
    void setName(String name);
}
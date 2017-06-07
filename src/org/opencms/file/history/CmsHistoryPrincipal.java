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

package org.opencms.file.history;

import org.opencms.security.CmsPrincipal;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;

/**
 * Describes an OpenCms historical principal entry.<p>
 *
 * @since 6.9.1
 */
public class CmsHistoryPrincipal extends CmsPrincipal implements Cloneable {

    /** The date the principal was deleted. */
    private long m_dateDeleted;

    /** The email address of this deleted user, if this principal is a user. */
    private String m_email;

    /** The type of this deleted principal. */
    private String m_type;

    /** The id of user that deleted this principal. */
    private CmsUUID m_userDeleted;

    /**
     * Default constructor.<p>
     *
     * @param id the unique id of this principal
     * @param name the fully qualified name
     * @param description the description
     * @param type the principal type
     * @param email the email address
     * @param userDeleted the id of user that deleted this principal
     * @param dateDeleted the date the principal was deleted
     */
    public CmsHistoryPrincipal(
        CmsUUID id,
        String name,
        String description,
        String email,
        String type,
        CmsUUID userDeleted,
        long dateDeleted) {

        m_id = id;
        m_name = name;
        m_description = description;
        m_email = email;
        m_type = type;
        m_dateDeleted = dateDeleted;
        m_userDeleted = userDeleted;
    }

    /**
     * @see org.opencms.security.I_CmsPrincipal#checkName(java.lang.String)
     */
    public void checkName(String name) {

        // noop
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        return new CmsHistoryPrincipal(
            getId(),
            getName(),
            getDescription(),
            getEmail(),
            getType(),
            m_userDeleted,
            m_dateDeleted);
    }

    /**
     * Returns the date the user was deleted.
     *
     * @return the date the user was deleted
     */
    public long getDateDeleted() {

        return m_dateDeleted;
    }

    /**
     * Returns the email address of this deleted user, if this principal is a user.<p>
     *
     * @return the email address of this deleted user
     */
    public String getEmail() {

        return m_email;
    }

    /**
     * Returns the principal type.<p>
     *
     * @return the principal type
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the id of user that deleted this user.
     *
     * @return the id of user that deleted this user
     */
    public CmsUUID getUserDeleted() {

        return m_userDeleted;
    }

    /**
     * @see org.opencms.security.CmsPrincipal#isGroup()
     */
    @Override
    public boolean isGroup() {

        return m_type.equals(I_CmsPrincipal.PRINCIPAL_GROUP);
    }

    /**
     * @see org.opencms.security.CmsPrincipal#isUser()
     */
    @Override
    public boolean isUser() {

        return m_type.equals(I_CmsPrincipal.PRINCIPAL_USER);
    }
}

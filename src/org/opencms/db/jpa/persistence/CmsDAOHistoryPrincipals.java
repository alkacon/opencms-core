/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This data access object represents a history principal entry inside the table "cms_history_principals".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_HISTORY_PRINCIPALS")
public class CmsDAOHistoryPrincipals {

    /** The date when the principal was deleted. */
    @Basic
    @Column(name = "PRINCIPAL_DATEDELETED")
    private long m_principalDateDeleted;

    /** The principal description. */
    @Basic
    @Column(name = "PRINCIPAL_DESCRIPTION", nullable = false)
    private String m_principalDescription;

    /** The principal email. */
    @Basic
    @Column(name = "PRINCIPAL_EMAIL", nullable = false, length = 128)
    private String m_principalEmail;

    /** The principal id. */
    @Id
    @Column(name = "PRINCIPAL_ID", length = 36)
    private String m_principalId;

    /** The principal name. */
    @Basic
    @Column(name = "PRINCIPAL_NAME", nullable = false, length = 128)
    private String m_principalName;

    /** The principal ou. */
    @Basic
    @Column(name = "PRINCIPAL_OU", length = 128)
    private String m_principalOu;

    /** The principal type. */
    @Basic
    @Column(name = "PRINCIPAL_TYPE", nullable = false, length = 5)
    private String m_principalType;

    /** The name of the user who deleted this principal. */
    @Basic
    @Column(name = "PRINCIPAL_USERDELETED", nullable = false, length = 36)
    private String m_principalUserDeleted;

    /**
     * The default constructor.<p>
     */
    public CmsDAOHistoryPrincipals() {

        // noop
    }

    /**
     * A constructor that creates a historical principal entry.<p>
     * 
     * @param principalId the id of the principal
     */
    public CmsDAOHistoryPrincipals(String principalId) {

        m_principalId = principalId;
    }

    /**
     * Returns the principalDateDeleted.<p>
     *
     * @return the principalDateDeleted
     */
    public long getPrincipalDateDeleted() {

        return m_principalDateDeleted;
    }

    /**
     * Returns the principalDescription.<p>
     *
     * @return the principalDescription
     */
    public String getPrincipalDescription() {

        return m_principalDescription;
    }

    /**
     * Returns the principalEmail.<p>
     *
     * @return the principalEmail
     */
    public String getPrincipalEmail() {

        return m_principalEmail;
    }

    /**
     * Returns the principalId.<p>
     *
     * @return the principalId
     */
    public String getPrincipalId() {

        return m_principalId;
    }

    /**
     * Returns the principalName.<p>
     *
     * @return the principalName
     */
    public String getPrincipalName() {

        return m_principalName;
    }

    /**
     * Returns the principalOu.<p>
     *
     * @return the principalOu
     */
    public String getPrincipalOu() {

        return m_principalOu;
    }

    /**
     * Returns the principalType.<p>
     *
     * @return the principalType
     */
    public String getPrincipalType() {

        return m_principalType;
    }

    /**
     * Returns the principalUserDeleted.<p>
     *
     * @return the principalUserDeleted
     */
    public String getPrincipalUserDeleted() {

        return m_principalUserDeleted;
    }

    /**
     * Sets the principalDateDeleted.<p>
     *
     * @param principalDateDeleted the principalDateDeleted to set
     */
    public void setPrincipalDateDeleted(long principalDateDeleted) {

        m_principalDateDeleted = principalDateDeleted;
    }

    /**
     * Sets the principalDescription.<p>
     *
     * @param principalDescription the principalDescription to set
     */
    public void setPrincipalDescription(String principalDescription) {

        m_principalDescription = principalDescription;
    }

    /**
     * Sets the principalEmail.<p>
     *
     * @param principalEmail the principalEmail to set
     */
    public void setPrincipalEmail(String principalEmail) {

        m_principalEmail = principalEmail;
    }

    /**
     * Sets the principalId.<p>
     *
     * @param principalId the principalId to set
     */
    public void setPrincipalId(String principalId) {

        m_principalId = principalId;
    }

    /**
     * Sets the principalName.<p>
     *
     * @param principalName the principalName to set
     */
    public void setPrincipalName(String principalName) {

        m_principalName = principalName;
    }

    /**
     * Sets the principalOu.<p>
     *
     * @param principalOu the principalOu to set
     */
    public void setPrincipalOu(String principalOu) {

        m_principalOu = principalOu;
    }

    /**
     * Sets the principalType.<p>
     *
     * @param principalType the principalType to set
     */
    public void setPrincipalType(String principalType) {

        m_principalType = principalType;
    }

    /**
     * Sets the principalUserDeleted.<p>
     *
     * @param principalUserDeleted the principalUserDeleted to set
     */
    public void setPrincipalUserDeleted(String principalUserDeleted) {

        m_principalUserDeleted = principalUserDeleted;
    }

}
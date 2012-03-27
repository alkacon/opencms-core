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
import javax.persistence.UniqueConstraint;

/**
 * This data access object represents a user entry inside the table "cms_users".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_USERS", uniqueConstraints = @UniqueConstraint(columnNames = {"USER_NAME", "USER_OU"}))
public class CmsDAOUsers {

    /** The user date created. */
    @Basic
    @Column(name = "USER_DATECREATED")
    private long m_userDateCreated;

    /** The user email. */
    @Basic
    @Column(name = "USER_EMAIL", nullable = false, length = 128)
    private String m_userEmail;

    /** The user firstname. */
    @Basic
    @Column(name = "USER_FIRSTNAME", nullable = false, length = 128)
    private String m_userFirstName;

    /** The user flags. */
    @Basic
    @Column(name = "USER_FLAGS")
    private int m_userFlags;

    /** The user id. */
    @Id
    @Column(name = "USER_ID", length = 36)
    private String m_userId;

    /** The user last login date. */
    @Basic
    @Column(name = "USER_LASTLOGIN")
    private long m_userLastLogin;

    /** The user lastname. */
    @Basic
    @Column(name = "USER_LASTNAME", nullable = false, length = 128)
    private String m_userLastName;

    /** The user login name. */
    @Basic
    @Column(name = "USER_NAME", nullable = false, length = 128)
    private String m_userName;

    /** The user ou. */
    @Basic
    @Column(name = "USER_OU", nullable = false, length = 128)
    private String m_userOu;

    /** The user password. */
    @Basic
    @Column(name = "USER_PASSWORD", nullable = false, length = 64)
    private String m_userPassword;

    /**
     * The default constructor.<p>
     */
    public CmsDAOUsers() {

        // noop
    }

    /**
     * A public constructor for generating a new user object with an unique id.<p>
     * 
     * @param userId the user id
     */
    public CmsDAOUsers(String userId) {

        m_userId = userId;
    }

    /**
     * Returns the userDateCreated.<p>
     *
     * @return the userDateCreated
     */
    public long getUserDateCreated() {

        return m_userDateCreated;
    }

    /**
     * Returns the userEmail.<p>
     *
     * @return the userEmail
     */
    public String getUserEmail() {

        return m_userEmail;
    }

    /**
     * Returns the userFirstName.<p>
     *
     * @return the userFirstName
     */
    public String getUserFirstName() {

        return m_userFirstName;
    }

    /**
     * Returns the userFlags.<p>
     *
     * @return the userFlags
     */
    public int getUserFlags() {

        return m_userFlags;
    }

    /**
     * Returns the userId.<p>
     *
     * @return the userId
     */
    public String getUserId() {

        return m_userId;
    }

    /**
     * Returns the userLastLogin.<p>
     *
     * @return the userLastLogin
     */
    public long getUserLastLogin() {

        return m_userLastLogin;
    }

    /**
     * Returns the userLastName.<p>
     *
     * @return the userLastName
     */
    public String getUserLastName() {

        return m_userLastName;
    }

    /**
     * Returns the userName.<p>
     *
     * @return the userName
     */
    public String getUserName() {

        return m_userName;
    }

    /**
     * Returns the userOu.<p>
     *
     * @return the userOu
     */
    public String getUserOu() {

        return m_userOu;
    }

    /**
     * Returns the userPassword.<p>
     *
     * @return the userPassword
     */
    public String getUserPassword() {

        return m_userPassword;
    }

    /**
     * Sets the userDateCreated.<p>
     *
     * @param userDateCreated the userDateCreated to set
     */
    public void setUserDateCreated(long userDateCreated) {

        m_userDateCreated = userDateCreated;
    }

    /**
     * Sets the userEmail.<p>
     *
     * @param userEmail the userEmail to set
     */
    public void setUserEmail(String userEmail) {

        m_userEmail = userEmail;
    }

    /**
     * Sets the userFirstName.<p>
     *
     * @param userFirstName the userFirstName to set
     */
    public void setUserFirstName(String userFirstName) {

        m_userFirstName = userFirstName;
    }

    /**
     * Sets the userFlags.<p>
     *
     * @param userFlags the userFlags to set
     */
    public void setUserFlags(int userFlags) {

        m_userFlags = userFlags;
    }

    /**
     * Sets the userId.<p>
     *
     * @param userId the userId to set
     */
    public void setUserId(String userId) {

        m_userId = userId;
    }

    /**
     * Sets the userLastLogin.<p>
     *
     * @param userLastLogin the userLastLogin to set
     */
    public void setUserLastLogin(long userLastLogin) {

        m_userLastLogin = userLastLogin;
    }

    /**
     * Sets the userLastName.<p>
     *
     * @param userLastName the userLastName to set
     */
    public void setUserLastName(String userLastName) {

        m_userLastName = userLastName;
    }

    /**
     * Sets the userName.<p>
     *
     * @param userName the userName to set
     */
    public void setUserName(String userName) {

        m_userName = userName;
    }

    /**
     * Sets the userOu.<p>
     *
     * @param userOu the userOu to set
     */
    public void setUserOu(String userOu) {

        m_userOu = userOu;
    }

    /**
     * Sets the userPassword.<p>
     *
     * @param userPassword the userPassword to set
     */
    public void setUserPassword(String userPassword) {

        m_userPassword = userPassword;
    }

}
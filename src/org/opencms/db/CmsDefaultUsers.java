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

package org.opencms.db;

import org.opencms.main.CmsRuntimeException;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsStringUtil;

/**
 * Provides access to the names of the OpenCms default users and groups.<p>
 *
 * @since 6.0.0
 */
public class CmsDefaultUsers {

    /** Default name for the "Administrators" group. */
    protected static final String DEFAULT_GROUP_ADMINISTRATORS = "Administrators";

    /** Default name for the "Guests" group. */
    protected static final String DEFAULT_GROUP_GUESTS = "Guests";

    /** Default name for the "Users" group. */
    protected static final String DEFAULT_GROUP_USERS = "Users";

    /** Default name for the "Admin" user. */
    protected static final String DEFAULT_USER_ADMIN = "Admin";

    /** Default name for the "Deleted Resource" user. */
    protected static final String DEFAULT_USER_DELETED_RESOURCE = "Admin";

    /** Default name for the "Export" user. */
    protected static final String DEFAULT_USER_EXPORT = "Export";

    /** Default name for the "Guest" user. */
    protected static final String DEFAULT_USER_GUEST = "Guest";

    /** Administrators group name. */
    private String m_groupAdministrators;

    /** Guests group name. */
    private String m_groupGuests;

    /** System Users group name. */
    private String m_groupUsers;

    /** Administrator user name. */
    private String m_userAdmin;

    /** Deleted resource user name. */
    private String m_userDeletedResource;

    /** Export user name. */
    private String m_userExport;

    /** Guest user name. */
    private String m_userGuest;

    /**
     * Constructor that initializes all names with default values.<p>
     *
     * See the constants of this class for the defaule values that are uses.<p>
     */
    public CmsDefaultUsers() {

        m_userAdmin = DEFAULT_USER_ADMIN;
        m_userGuest = DEFAULT_USER_GUEST;
        m_userExport = DEFAULT_USER_EXPORT;
        m_userDeletedResource = DEFAULT_USER_DELETED_RESOURCE;
        m_groupAdministrators = DEFAULT_GROUP_ADMINISTRATORS;
        m_groupUsers = DEFAULT_GROUP_USERS;
        m_groupGuests = DEFAULT_GROUP_GUESTS;
    }

    /**
     * Public constructor. <p>
     *
     * @param userAdmin the name of the default admin user
     * @param userGuest the name of the guest user
     * @param userExport the name of the export user
     * @param userDeletedResource the name of the deleted resource user, can be <code>null</code>
     * @param groupAdministrators the name of the administrators group
     * @param groupUsers the name of the users group
     * @param groupGuests the name of the guests group
     */
    public CmsDefaultUsers(
        String userAdmin,
        String userGuest,
        String userExport,
        String userDeletedResource,
        String groupAdministrators,
        String groupUsers,
        String groupGuests) {

        init(userAdmin, userGuest, userExport, userDeletedResource, groupAdministrators, groupUsers, groupGuests);
    }

    /**
     * Returns the name of the administrators group.<p>
     *
     * @return the name of the administrators group
     */
    public String getGroupAdministrators() {

        return m_groupAdministrators;
    }

    /**
     * Returns the name of the guests group.<p>
     *
     * @return the name of the guests group
     */
    public String getGroupGuests() {

        return m_groupGuests;
    }

    /**
     * Returns the name of the project managers group.<p>
     *
     * @return the name of the project managers group
     *
     * @deprecated
     *      As of OpenCms 9.5, there is no default group for project managers anymore.
     *      Use the role {@link org.opencms.security.CmsRole#PROJECT_MANAGER} instead in case you want a user to be able to manage projects.
     *      For backward compatibility in old installations, this returns the old default group name "Projectmanagers".
     */
    @Deprecated
    public String getGroupProjectmanagers() {

        return "Projectmanagers";
    }

    /**
     * Returns the name of the users group.<p>
     *
     * @return the name of the users group
     */
    public String getGroupUsers() {

        return m_groupUsers;
    }

    /**
     * Returns the name of the default administrator user.<p>
     *
     * @return the name of the default administrator user
     */
    public String getUserAdmin() {

        return m_userAdmin;
    }

    /**
     * Returns the name of the default deleted resource user.<p>
     *
     * @return the name of the default deleted resource user
     */
    public String getUserDeletedResource() {

        return m_userDeletedResource;
    }

    /**
     * Returns the name of the user used to generate the static export.<p>
     *
     * @return the name of the user used to generate the static export
     */
    public String getUserExport() {

        return m_userExport;
    }

    /**
     * Returns the name of the default guest user.<p>
     *
     * @return the name of the default guest user
     */
    public String getUserGuest() {

        return m_userGuest;
    }

    /**
     * Checks if a given group name is the name of one of the OpenCms default groups.<p>
     *
     * @param groupName the group name to check
     * @return <code>true</code> if group name is one of OpenCms default groups, <code>false</code> if it is not
     * or if <code>groupName</code> is <code>null</code> or an empty string (no trim)
     *
     * @see #getGroupAdministrators()
     * @see #getGroupUsers()
     * @see #getGroupGuests()
     */
    public boolean isDefaultGroup(String groupName) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(groupName)) {
            return false;
        }

        // first check without ou prefix, to stay backwards compatible
        boolean isDefault = m_groupAdministrators.equals(groupName);
        isDefault = isDefault || m_groupGuests.equals(groupName);
        isDefault = isDefault || m_groupUsers.equals(groupName);

        // now check with ou prefix
        isDefault = isDefault || groupName.endsWith(CmsOrganizationalUnit.SEPARATOR + m_groupAdministrators);
        isDefault = isDefault || groupName.endsWith(CmsOrganizationalUnit.SEPARATOR + m_groupGuests);
        isDefault = isDefault || groupName.endsWith(CmsOrganizationalUnit.SEPARATOR + m_groupUsers);

        return isDefault;
    }

    /**
     * Checks if a given user name is the name of one of the OpenCms default users.<p>
     *
     * @param userName the group name to check
     *
     * @return <code>true</code> if user name is one of OpenCms default users, <code>false</code> if it is not
     * or if <code>userName</code> is <code>null</code> or an empty string (no trim)
     *
     * @see #getUserAdmin()
     * @see #getUserExport()
     * @see #getUserGuest()
     * @see #getUserDeletedResource()
     */
    public boolean isDefaultUser(String userName) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(userName)) {
            return false;
        }

        // first check without ou prefix, to stay backwards compatible
        boolean isDefault = m_userAdmin.equals(userName);
        isDefault = isDefault || m_userGuest.equals(userName);
        isDefault = isDefault || m_userExport.equals(userName);
        isDefault = isDefault || m_userDeletedResource.equals(userName);

        // now check with ou prefix
        isDefault = isDefault || userName.equals(CmsOrganizationalUnit.SEPARATOR + m_userAdmin);
        isDefault = isDefault || userName.equals(CmsOrganizationalUnit.SEPARATOR + m_userGuest);
        isDefault = isDefault || userName.equals(CmsOrganizationalUnit.SEPARATOR + m_userExport);
        isDefault = isDefault || userName.equals(CmsOrganizationalUnit.SEPARATOR + m_userDeletedResource);

        return isDefault;
    }

    /**
     * Checks if a given group name is the name of the guests group.<p>
     *
     * @param groupName the group name to check
     *
     * @return <code>true</code> if a given group name is the name of the guests group
     */
    public boolean isGroupGuests(String groupName) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(groupName)) {
            return false;
        }
        return m_groupGuests.equals(groupName) || groupName.endsWith(CmsOrganizationalUnit.SEPARATOR + m_groupGuests);
    }

    /**
     * Checks if a given user name is the name of the admin user.<p>
     *
     * @param userName the user name to check
     *
     * @return <code>true</code> if a given user name is the name of the admin user
     */
    public boolean isUserAdmin(String userName) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(userName)) {
            return false;
        }
        return m_userAdmin.equals(userName);
    }

    /**
     * Checks if a given user name is the name of the export user.<p>
     *
     * @param userName the user name to check
     *
     * @return <code>true</code> if a given user name is the name of the export user
     */
    public boolean isUserExport(String userName) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(userName)) {
            return false;
        }
        return m_userExport.equals(userName);
    }

    /**
     * Checks if a given user name is the name of the guest user.<p>
     *
     * @param userName the user name to check
     *
     * @return <code>true</code> if a given user name is the name of the guest user
     */
    public boolean isUserGuest(String userName) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(userName)) {
            return false;
        }
        return m_userGuest.equals(userName);
    }

    /**
     * Initializes this instance.<p>
     *
     * @param userAdmin the name of the default admin user
     * @param userGuest the name of the guest user
     * @param userExport the name of the export user
     * @param userDeletedResource the name of the deleted resource user, can be <code>null</code>
     * @param groupAdministrators the name of the administrators group
     * @param groupUsers the name of the users group
     * @param groupGuests the name of the guests group
     */
    protected void init(
        String userAdmin,
        String userGuest,
        String userExport,
        String userDeletedResource,
        String groupAdministrators,
        String groupUsers,
        String groupGuests) {

        // check if all required user and group names are not null or empty
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(userAdmin)
            || CmsStringUtil.isEmptyOrWhitespaceOnly(userGuest)
            || CmsStringUtil.isEmptyOrWhitespaceOnly(userExport)
            || CmsStringUtil.isEmptyOrWhitespaceOnly(groupAdministrators)
            || CmsStringUtil.isEmptyOrWhitespaceOnly(groupUsers)
            || CmsStringUtil.isEmptyOrWhitespaceOnly(groupGuests)) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_USER_GROUP_NAMES_EMPTY_0));
        }
        // set members
        m_userAdmin = userAdmin.trim();
        m_userGuest = userGuest.trim();
        m_userExport = userExport.trim();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(userDeletedResource)) {
            m_userDeletedResource = userAdmin;
        } else {
            m_userDeletedResource = userDeletedResource.trim();
        }
        m_groupAdministrators = groupAdministrators.trim();
        m_groupUsers = groupUsers.trim();
        m_groupGuests = groupGuests.trim();
    }
}
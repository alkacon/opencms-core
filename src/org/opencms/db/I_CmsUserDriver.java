/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsUserDriver.java,v $
 * Date   : $Date: 2004/11/22 18:03:05 $
 * Version: $Revision: 1.39 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.db;

import org.opencms.db.generic.CmsSqlManager;
import org.opencms.main.CmsException;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.util.CmsUUID;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Definitions of all required user driver methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.39 $ $Date: 2004/11/22 18:03:05 $
 * @since 5.1
 */
public interface I_CmsUserDriver extends I_CmsDriver {

    /** The type ID to identify user driver implementations. */
    int C_DRIVER_TYPE_ID = 2;

    /**
     * Creates an access control entry.<p>
     * 
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param resource the id of the resource
     * @param principal the id of the principal (user or group)
     * @param allowed the bitset of allowed permissions
     * @param denied the bitset of denied permissions
     * @param flags flags
     * 
     * @throws CmsException if something goes wrong
     */
    void createAccessControlEntry(
        CmsDbContext dbc,
        CmsProject project,
        CmsUUID resource,
        CmsUUID principal,
        int allowed,
        int denied,
        int flags) throws CmsException;

    /**
     * Creates a new group.<p>
     * 
     * @param dbc the current database context
     * @param groupId the id of the new group
     * @param groupName the name of the new group
     * @param description The description for the new group
     * @param flags the flags for the new group
     * @param parentGroupName the name of the parent group (or null if the group has no parent)
     * @param reservedParam reserved optional parameter, should be null on standard OpenCms installations
     *
     * @return the created group
     * @throws CmsException if something goes wrong
     */
    CmsGroup createGroup(
        CmsDbContext dbc,
        CmsUUID groupId,
        String groupName,
        String description,
        int flags,
        String parentGroupName,
        Object reservedParam) throws CmsException;

    /**
     * Creates a new user.<p>
     * 
     * @param dbc the current database context
     * @param name the user name
     * @param password the user password
     * @param description the user description
     * @param firstname the user firstname
     * @param lastname the user lastname
     * @param email the user email
     * @param lastlogin the user lastlogin time
     * @param flags the user flags
     * @param additionalInfos the user additional infos
     * @param address the user default address
     * @param type the user type
     * 
     * @return the created user
     * @throws CmsException if something goes wrong
     */
    CmsUser createUser(
        CmsDbContext dbc,
        String name,
        String password,
        String description,
        String firstname,
        String lastname,
        String email,
        long lastlogin,
        int flags,
        Hashtable additionalInfos,
        String address,
        int type) throws CmsException;

    /**
     * Adds a user to a group.<p>
     *
     * @param dbc the current database context
     * @param userid the id of the user that is to be added to the group
     * @param groupid the id of the group
     * @param reservedParam reserved optional parameter, should be null on standard OpenCms installations
     * 
     * @throws CmsException if operation was not succesfull
     */
    void createUserInGroup(CmsDbContext dbc, CmsUUID userid, CmsUUID groupid, Object reservedParam) throws CmsException;

    /**
     * Deletes all access control entries (ACEs) belonging to a resource.<p>
     * 
     * @param dbc the current database context
     * @param project the project to delete the ACEs in
     * @param resource the id of the resource to delete the ACEs from
     * 
     * @throws CmsException if something goes wrong
     */
    void deleteAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource) throws CmsException;

    /**
     * Deletes a group.<p>
     * 
     * Only groups that contain no subgroups can be deleted.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the group that is to be deleted
     *
     * @throws CmsException if something goes wrong
     */
    void deleteGroup(CmsDbContext dbc, String name) throws CmsException;

    /**
     * Deletes a user.<p>
     * 
     * @param dbc the current database context
     * @param userName the name of the user to delete
     * 
     * @throws CmsException if something goes wrong
     */
    void deleteUser(CmsDbContext dbc, String userName) throws CmsException;

    /**
     * Removes a user from a group.<p>
     * 
     * @param dbc the current database context
     * @param userId the id of the user that is to be removed from the group
     * @param groupId the id of the group
     * 
     * @throws CmsException if something goes wrong
     */
    void deleteUserInGroup(CmsDbContext dbc, CmsUUID userId, CmsUUID groupId) throws CmsException;

    /**
     * Destroys this driver.<p>
     * 
     * @throws Throwable if something goes wrong
     * @throws CmsException if something else goes wrong
     */
    void destroy() throws Throwable, CmsException;

    /**
     * Tests if a group with the specified name exists.<p>
     * 
     * @param dbc the current database context
     * @param groupname the user name to be checked
     * @param reservedParam reserved optional parameter, should be null on standard OpenCms installations
     * 
     * @return true, if a group with the specified name exists, false otherwise
     * @throws CmsException if something goes wrong
     */
    boolean existsGroup(CmsDbContext dbc, String groupname, Object reservedParam) throws CmsException;

    /**
     * Tests if a user with the specified name exists.<p>
     * 
     * @param dbc the current database context
     * @param username the user name to be checked
     * @param usertype the type of the user
     * @param reservedParam reserved optional parameter, should be null on standard OpenCms installations
     * 
     * @return true, if a user with the specified name exists, false otherwise
     * @throws CmsException if something goes wrong
     */
    boolean existsUser(CmsDbContext dbc, String username, int usertype, Object reservedParam) throws CmsException;

    /**
     * Returns the SqlManager of this driver.<p>
     * 
     * @return the SqlManager of this driver
     */
    CmsSqlManager getSqlManager();

    /**
     * Imports a user.<p>
     * 
     * @param dbc the current database context
     * @param id the id of the user
     * @param name the user name
     * @param password the user password
     * @param description the user description
     * @param firstname the user firstname
     * @param lastname the user lastname
     * @param email the user email
     * @param lastlogin the user lastlogin time
     * @param flags the user flags
     * @param additionalInfos the user additional infos
     * @param address the user default address
     * @param type the user type
     * @param reservedParam reserved optional parameter, should be null on standard OpenCms installations
     * 
     * @return the imported user
     * @throws CmsException if something goes wrong
     */
    CmsUser importUser(
        CmsDbContext dbc,
        CmsUUID id,
        String name,
        String password,
        String description,
        String firstname,
        String lastname,
        String email,
        long lastlogin,
        int flags,
        Hashtable additionalInfos,
        String address,
        int type,
        Object reservedParam) throws CmsException;

    /**
     * Initializes the SQL manager for this driver.<p>
     * 
     * To obtain JDBC connections from different pools, further 
     * {online|offline|backup} pool Urls have to be specified.<p>
     * 
     * @param classname the classname of the SQL manager
     * 
     * @return the SQL manager for this driver
     */
    org.opencms.db.generic.CmsSqlManager initSqlManager(String classname);

    /**
     * Publish all access control entries of a resource from the given offline project to the online project.<p>
     * 
     * Within the given project, the resource is identified by its offlineId, in the online project,
     * it is identified by the given onlineId.<p>
     * 
     * @param dbc the current database context
     * @param offlineProject an offline project
     * @param onlineProject the onlie project
     * @param offlineId the offline resource id
     * @param onlineId the online resource id
     * 
     * @throws CmsException if something goes wrong
     */
    void publishAccessControlEntries(
        CmsDbContext dbc,
        CmsProject offlineProject,
        CmsProject onlineProject,
        CmsUUID offlineId,
        CmsUUID onlineId) throws CmsException;

    /**
     * Reads all relevant access control entries for a given resource.<p>
     * 
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param resource the id of the resource
     * @param inheritedOnly flag to indicate that only inherited entries should be returned
     * @return a vector of access control entries defining all permissions for the given resource
     * 
     * @throws CmsException if something goes wrong
     */
    Vector readAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource, boolean inheritedOnly)
    throws CmsException;

    /**
     * Reads an access control entry for a given principal that is attached to a resource.<p>
     * 
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param resource the id of the resource
     * @param principal the id of the principal
     * 
     * @return an access control entry that defines the permissions of the principal for the given resource
     * @throws CmsException if something goes wrong
     */
    CmsAccessControlEntry readAccessControlEntry(
        CmsDbContext dbc,
        CmsProject project,
        CmsUUID resource,
        CmsUUID principal) throws CmsException;

    /**
     * Reads all child groups of a group.<p>
     *
     * @param dbc the current database context
     * @param groupname the name of the group to read the child groups from
     * 
     * @return users a Vector of all child groups or null
     * @throws CmsException if operation was not succesful
     */
    Vector readChildGroups(CmsDbContext dbc, String groupname) throws CmsException;

    /**
     * Reads a group based on the group id.<p>
     * 
     * @param dbc the current database context
     * @param groupId the id of the group that is to be read
     * 
     * @return the group that was read
     * @throws CmsException if something goes wrong
     */
    CmsGroup readGroup(CmsDbContext dbc, CmsUUID groupId) throws CmsException;

    /**
     * Reads a group based on the group name.<p>
     * 
     * @param dbc the current database context
     * @param groupName the name of the group that is to be read
     * 
     * @return the group that was read
     * @throws CmsException if something goes wrong
     */
    CmsGroup readGroup(CmsDbContext dbc, String groupName) throws CmsException;

    /**
     * Reads all existing groups.<p>
     *
     * @param dbc the current database context
     * 
     * @return a Vector of all existing groups
     * @throws CmsException if something goes wrong
     */
    Vector readGroups(CmsDbContext dbc) throws CmsException;

    /**
     * Reads all groups the given user is a member in.<p>
     *
     * @param dbc the current database context
     * @param userId the id of the user
     * @param paramStr additional parameter
     * 
     * @return all groups the given user is a member in
     * @throws CmsException if something goes wrong
     */
    Vector readGroupsOfUser(CmsDbContext dbc, CmsUUID userId, String paramStr) throws CmsException;

    /**
     * Reads a user based on the user id.<p>
     * 
     * @param dbc the current database context
     * @param id the id of the user to read
     *
     * @return the user that was read
     * @throws CmsException if something goes wrong
     */
    CmsUser readUser(CmsDbContext dbc, CmsUUID id) throws CmsException;

    /**
     * Reads a user based in the user name and user type.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the user to read
     * @param type the type of the user to read
     *
     * @return the user that was read
     * @throws CmsException if something goes wrong
     */
    CmsUser readUser(CmsDbContext dbc, String name, int type) throws CmsException;

    /**
     * Reads a user from the database, only if the password is correct.<p>
     *
     * @param dbc the current database context
     * @param name the name of the user
     * @param password the password of the user
     * @param type the type of the user
     * 
     * @return the user that was read
     * @throws CmsException if something goes wrong
     */
    CmsUser readUser(CmsDbContext dbc, String name, String password, int type) throws CmsException;

    /**
     * Reads a user from the database, only if the password is correct.<p>
     *
     * @param dbc the current database context
     * @param name the name of the user
     * @param password the password of the user
     * @param remoteAddress the remote address of the request
     * @param type the type of the user
     * 
     * @return the user that was read
     * @throws CmsException if something goes wrong
     */
    CmsUser readUser(CmsDbContext dbc, String name, String password, String remoteAddress, int type)
    throws CmsException;

    /**
     * Reads all existing users of the given type.<p>
     *
     * @param dbc the current database context
     * @param type the type to read the users for
     * 
     * @return all existing users of the given type
     * @throws CmsException if something goes wrong
     */
    Vector readUsers(CmsDbContext dbc, int type) throws CmsException;

    /**
     * Reads all users that are members of the given group.<p>
     *
     * @param dbc the current database context
     * @param name the name of the group to read the users from
     * @param type the type of the users to read
     * 
     * @return all users that are members of the given group
     * @throws CmsException if something goes wrong
     */
    Vector readUsersOfGroup(CmsDbContext dbc, String name, int type) throws CmsException;

    /**
     * Removes all access control entries belonging to a resource.<p>
     * 
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param resource the id of the resource
     * 
     * @throws CmsException if something goes wrong
     */
    void removeAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource) throws CmsException;

    /**
     * Removes all access control entries belonging to a principal.<p>
     * 
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param onlineProject the online project 
     * @param principal the id of the principal
     * 
     * @throws CmsException if something goes wrong
     */
    void removeAccessControlEntriesForPrincipal(
        CmsDbContext dbc,
        CmsProject project,
        CmsProject onlineProject,
        CmsUUID principal) throws CmsException;

    /**
     * Removes an access control entry.<p>
     * 
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param resource the id of the resource
     * @param principal the id of the principal
     * 
     * @throws CmsException if something goes wrong
     */
    void removeAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsUUID resource, CmsUUID principal)
    throws CmsException;

    /**
     * Undeletes all access control entries belonging to a resource.<p>
     * 
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param resource the id of the resource
     * @throws CmsException if something goes wrong
     */
    void undeleteAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource) throws CmsException;

    /**
     * Writes an access control entry.<p>
     * 
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param acEntry the entry to write
     * 
     * @throws CmsException if something goes wrong
     */
    void writeAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsAccessControlEntry acEntry)
    throws CmsException;

    /**
     * Updates an already existing group.<p>
     * 
     * @param dbc the current database context
     * @param group the group to update
     *
     * @throws CmsException if something goes wrong
     */
    void writeGroup(CmsDbContext dbc, CmsGroup group) throws CmsException;

    /**
     * Sets a new password for a user.<p>
     * 
     * @param dbc the current database context
     * @param userName the user to set the password for
     * @param type the type of the user
     * @param oldPassword the current password
     * @param newPassword the password to set
     *
     * @throws CmsException if something goes wrong
     */
    void writePassword(CmsDbContext dbc, String userName, int type, String oldPassword, String newPassword)
    throws CmsException;

    /**
     * Updates an already existing user.<p>
     * 
     * @param dbc the current database context
     * @param user the user to update
     *
     * @throws CmsException if something goes wrong
     */
    void writeUser(CmsDbContext dbc, CmsUser user) throws CmsException;

    /**
     * Changes the user type of the given user.<p>
     * 
     * @param dbc the current database context
     * @param userId the id of the user to change
     * @param userType the new type of the user
     *
     * @throws CmsException if something goes wrong
     */
    void writeUserType(CmsDbContext dbc, CmsUUID userId, int userType) throws CmsException;

}
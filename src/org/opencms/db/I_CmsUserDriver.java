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

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsUserSearchParameters;
import org.opencms.main.CmsInitException;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPasswordEncryptionException;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;

/**
 * Definitions of all required user driver methods. <p>
 *
 * @since 6.0.0
 */
public interface I_CmsUserDriver extends I_CmsDriver {

    /** The type ID to identify user driver implementations. */
    int DRIVER_TYPE_ID = 2;

    /**
     * Adds a resource to the given organizational unit.<p>
     *
     * @param dbc the current db context
     * @param orgUnit the organizational unit to add the resource to
     * @param resource the resource that is to be added to the organizational unit
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void addResourceToOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsResource resource)
    throws CmsDataAccessException;

    /**
     * Counts the total number of users which match the given search criteria.<p>
     *
     * @param dbc the database context
     * @param searchParams the search criteria
     *
     * @return the number of users which match the search criteria
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    long countUsers(CmsDbContext dbc, CmsUserSearchParameters searchParams) throws CmsDataAccessException;

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
     * @throws CmsDataAccessException if something goes wrong
     */
    void createAccessControlEntry(
        CmsDbContext dbc,
        CmsProject project,
        CmsUUID resource,
        CmsUUID principal,
        int allowed,
        int denied,
        int flags) throws CmsDataAccessException;

    /**
     * Creates a new group.<p>
     *
     * @param dbc the current database context
     * @param groupId the id of the new group
     * @param groupFqn the fully qualified name of the new group
     * @param description The description for the new group
     * @param flags the flags for the new group
     * @param parentGroupName the name of the parent group (or null if the group has no parent)
     *
     * @return the created group
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsGroup createGroup(
        CmsDbContext dbc,
        CmsUUID groupId,
        String groupFqn,
        String description,
        int flags,
        String parentGroupName) throws CmsDataAccessException;

    /**
     * Creates a new organizational unit.<p>
     *
     * @param dbc the current db context
     * @param name the name of the new organizational unit
     * @param description the description of the new organizational unit
     * @param flags the flags for the new organizational unit
     * @param parent the parent organizational unit (or <code>null</code>)
     * @param associationRootPath the first associated resource
     *
     * @return a <code>{@link CmsOrganizationalUnit}</code> object representing
     *          the newly created organizational unit
     *
     * @throws CmsDataAccessException if operation was not successful
     */
    CmsOrganizationalUnit createOrganizationalUnit(
        CmsDbContext dbc,
        String name,
        String description,
        int flags,
        CmsOrganizationalUnit parent,
        String associationRootPath) throws CmsDataAccessException;

    /**
     * Creates the default root organizational unit.<p>
     *
     * @param dbc the current database context
     */
    void createRootOrganizationalUnit(CmsDbContext dbc);

    /**
     * Creates a new user.<p>
     *
     * @param dbc the current database context
     * @param id the id of the user
     * @param userFqn the fully qualified name of the new user
     * @param password the already encripted user password
     * @param firstname the user firstname
     * @param lastname the user lastname
     * @param email the user email
     * @param lastlogin the user lastlogin time
     * @param flags the user flags
     * @param dateCreated the creation date
     * @param additionalInfos the user additional infos
     *
     * @return the created user
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsUser createUser(
        CmsDbContext dbc,
        CmsUUID id,
        String userFqn,
        String password,
        String firstname,
        String lastname,
        String email,
        long lastlogin,
        int flags,
        long dateCreated,
        Map<String, Object> additionalInfos) throws CmsDataAccessException;

    /**
     * Adds a user to a group.<p>
     *
     * @param dbc the current database context
     * @param userid the id of the user that is to be added to the group
     * @param groupid the id of the group
     *
     * @throws CmsDataAccessException if operation was not successful
     */
    void createUserInGroup(CmsDbContext dbc, CmsUUID userid, CmsUUID groupid) throws CmsDataAccessException;

    /**
     * Deletes a group.<p>
     *
     * Only groups that contain no subgroups can be deleted.<p>
     *
     * @param dbc the current database context
     * @param groupFqn the fully qualified name of the group that is to be deleted
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteGroup(CmsDbContext dbc, String groupFqn) throws CmsDataAccessException;

    /**
     * Deletes an organizational unit.<p>
     *
     * Only organizational units that contain no suborganizational unit can be deleted.<p>
     *
     * @param dbc the current db context
     * @param organizationalUnit the organizational unit to delete
     *
     * @throws CmsDataAccessException if operation was not successful
     */
    void deleteOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit organizationalUnit)
    throws CmsDataAccessException;

    /**
     * Deletes a user.<p>
     *
     * @param dbc the current database context
     * @param userFqn the fully qualified name of the user to delete
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteUser(CmsDbContext dbc, String userFqn) throws CmsDataAccessException;

    /**
     * Deletes the user additional information table.<p>
     *
     * @param dbc the current database context
     * @param userId the id of the user to update
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteUserInfos(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException;

    /**
     * Removes a user from a group.<p>
     *
     * @param dbc the current database context
     * @param userId the id of the user that is to be removed from the group
     * @param groupId the id of the group
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteUserInGroup(CmsDbContext dbc, CmsUUID userId, CmsUUID groupId) throws CmsDataAccessException;

    /**
     * Destroys this driver.<p>
     *
     * @throws Throwable if something goes wrong
     */
    void destroy() throws Throwable;

    /**
     * Tests if a group with the specified name exists.<p>
     *
     * @param dbc the current database context
     * @param groupFqn the fully qualified group name to be checked
     *
     * @return <code>true</code>, if a group with the specified name exists, <code>false</code> otherwise
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    boolean existsGroup(CmsDbContext dbc, String groupFqn) throws CmsDataAccessException;

    /**
     * Tests if a user with the specified name exists.<p>
     *
     * @param dbc the current database context
     * @param userFqn the fully qualified name of the user to be checked
     *
     * @return true, if a user with the specified name exists, false otherwise
     * @throws CmsDataAccessException if something goes wrong
     */
    boolean existsUser(CmsDbContext dbc, String userFqn) throws CmsDataAccessException;

    /**
     * Initializes the default organizational units, users and groups.<p>
     *
     * @param dbc the current database context, be aware that this dbc has no runtime data!
     *
     * @throws CmsInitException if something goes wrong
     */
    void fillDefaults(CmsDbContext dbc) throws CmsInitException;

    /**
     * Returns all groups of the given organizational unit.<p>
     *
     * @param dbc the current db context
     * @param orgUnit the organizational unit to get all groups for
     * @param includeSubOus flag to signalize the retrieval of groups of sub-organizational units too
     * @param readRoles if to read roles or groups
     *
     * @return all <code>{@link CmsGroup}</code> objects in the organizational unit
     *
     * @throws CmsDataAccessException if operation was not successful
     */
    List<CmsGroup> getGroups(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean includeSubOus, boolean readRoles)
    throws CmsDataAccessException;

    /**
     * Returns all child organizational units of the given parent organizational unit including
     * hierarchical deeper organization units if needed.<p>
     *
     * @param dbc the current db context
     * @param parent the parent organizational unit, or <code>null</code> for the root
     * @param includeChildren if hierarchical deeper organization units should also be returned
     *
     * @return a list of <code>{@link CmsOrganizationalUnit}</code> objects
     *
     * @throws CmsDataAccessException if operation was not succesful
     */
    List<CmsOrganizationalUnit> getOrganizationalUnits(
        CmsDbContext dbc,
        CmsOrganizationalUnit parent,
        boolean includeChildren) throws CmsDataAccessException;

    /**
     * Returns all resources of the given organizational unit.<p>
     *
     * @param dbc the current db context
     * @param orgUnit the organizational unit to get all resources for
     *
     * @return all <code>{@link CmsResource}</code> objects in the organizational unit
     *
     * @throws CmsDataAccessException if operation was not successful
     */
    List<CmsResource> getResourcesForOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit)
    throws CmsDataAccessException;

    /**
     * Returns the SqlManager of this driver.<p>
     *
     * @return the SqlManager of this driver
     */
    CmsSqlManager getSqlManager();

    /**
     * Returns all users of the given organizational unit.<p>
     *
     * @param dbc the current db context
     * @param orgUnit the organizational unit to get all users for
     * @param recursive flag to signalize the retrieval of users of sub-organizational units too
     *
     * @return all <code>{@link CmsUser}</code> objects in the organizational unit
     *
     * @throws CmsDataAccessException if operation was not successful
     */
    List<CmsUser> getUsers(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean recursive)
    throws CmsDataAccessException;

    /**
     * Returns all users of the given organizational unit, without reading their additional infos.<p>
     *
     * @param dbc the current db context
     * @param orgUnit the organizational unit to get all users for
     * @param recursive flag to signalize the retrieval of users of sub-organizational units too
     *
     * @return all <code>{@link CmsUser}</code> objects in the organizational unit
     *
     * @throws CmsDataAccessException if operation was not successful
     */
    List<CmsUser> getUsersWithoutAdditionalInfo(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, boolean recursive)
    throws CmsDataAccessException;

    //    /**
    //     * Mark the given resource as visited by the user.<p>
    //     *
    //     * @param dbc the database context
    //     * @param poolName the name of the database pool to use
    //     * @param resource the resource to mark as visited
    //     * @param user the user that visited the resource
    //     *
    //     * @deprecated
    //     * @see org.opencms.db.I_CmsSubscriptionDriver
    //     *
    //     * @throws CmsDataAccessException if something goes wrong
    //     */
    //    void markResourceAsVisitedBy(CmsDbContext dbc, String poolName, CmsResource resource, CmsUser user)
    //    throws CmsDataAccessException;

    /**
     * Initializes the SQL manager for this driver.<p>
     *
     * To obtain JDBC connections from different pools, further
     * {online|offline|history} pool Urls have to be specified.<p>
     *
     * @param classname the classname of the SQL manager
     *
     * @return the SQL manager for this driver
     */
    CmsSqlManager initSqlManager(String classname);

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
     * @throws CmsDataAccessException if something goes wrong
     */
    void publishAccessControlEntries(
        CmsDbContext dbc,
        CmsProject offlineProject,
        CmsProject onlineProject,
        CmsUUID offlineId,
        CmsUUID onlineId) throws CmsDataAccessException;

    /**
     * Reads all relevant access control entries for a given resource.<p>
     *
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param resource the id of the resource
     * @param inheritedOnly flag to indicate that only inherited entries should be returned
     *
     * @return a list of <code>{@link CmsAccessControlEntry}</code> objects defining all permissions for the given resource
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsAccessControlEntry> readAccessControlEntries(
        CmsDbContext dbc,
        CmsProject project,
        CmsUUID resource,
        boolean inheritedOnly) throws CmsDataAccessException;

    //    /**
    //     * Returns all resources subscribed by the given user or group.<p>
    //     *
    //     * @param dbc the database context
    //     * @param poolName the name of the database pool to use
    //     * @param principal the principal to read the subscribed resources
    //     *
    //     * @return all resources subscribed by the given user or group
    //     *
    //     * @deprecated
    //     * @see org.opencms.db.I_CmsSubscriptionDriver
    //     *
    //     * @throws CmsDataAccessException if something goes wrong
    //     */
    //    List<CmsResource> readAllSubscribedResources(CmsDbContext dbc, String poolName, CmsPrincipal principal)
    //    throws CmsDataAccessException;

    /**
     * Reads an access control entry for a given principal that is attached to a resource.<p>
     *
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param resource the id of the resource
     * @param principal the id of the principal
     *
     * @return an access control entry that defines the permissions of the principal for the given resource
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsAccessControlEntry readAccessControlEntry(
        CmsDbContext dbc,
        CmsProject project,
        CmsUUID resource,
        CmsUUID principal) throws CmsDataAccessException;

    /**
     * Reads all child groups of a group.<p>
     *
     * @param dbc the current database context
     * @param groupFqn the fully qualified name of the group to read the child groups from
     *
     * @return a list of all child <code>{@link CmsGroup}</code> objects or <code>null</code>
     *
     * @throws CmsDataAccessException if operation was not succesful
     */
    List<CmsGroup> readChildGroups(CmsDbContext dbc, String groupFqn) throws CmsDataAccessException;

    /**
     * Reads a group based on the group id.<p>
     *
     * @param dbc the current database context
     * @param groupId the id of the group that is to be read
     *
     * @return the group that was read
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsGroup readGroup(CmsDbContext dbc, CmsUUID groupId) throws CmsDataAccessException;

    /**
     * Reads a group based on the group name.<p>
     *
     * @param dbc the current database context
     * @param groupFqn the fully qualified name of the group that is to be read
     *
     * @return the group that was read
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsGroup readGroup(CmsDbContext dbc, String groupFqn) throws CmsDataAccessException;

    /**
     * Reads all groups the given user is a member in.<p>
     *
     * @param dbc the current database context
     * @param userId the id of the user
     * @param ouFqn the fully qualified name of the organizational unit to restrict the result set for
     * @param includeChildOus include groups of child organizational units
     * @param remoteAddress the IP address to filter the groups in the result list
     * @param readRoles if to read roles or groups
     *
     * @return a list of <code>{@link CmsGroup}</code> objects
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsGroup> readGroupsOfUser(
        CmsDbContext dbc,
        CmsUUID userId,
        String ouFqn,
        boolean includeChildOus,
        String remoteAddress,
        boolean readRoles) throws CmsDataAccessException;

    //    /**
    //     * Returns the resources that were visited by a user set in the filter.<p>
    //     *
    //     * @param dbc the database context
    //     * @param poolName the name of the database pool to use
    //     * @param filter the filter that is used to get the visited resources
    //     *
    //     * @return the resources that were visited by a user set in the filter
    //     *
    //     * @deprecated
    //     * @see org.opencms.db.I_CmsSubscriptionDriver
    //     *
    //     * @throws CmsDataAccessException if something goes wrong
    //     */
    //    List<CmsResource> readResourcesVisitedBy(CmsDbContext dbc, String poolName, CmsVisitedByFilter filter)
    //    throws CmsDataAccessException;
    //
    //    /**
    //     * Returns the subscribed history resources that were deleted.<p>
    //     *
    //     * @param dbc the database context
    //     * @param poolName the name of the database pool to use
    //     * @param user the user that subscribed to the resource
    //     * @param groups the groups to check subscribed resources for
    //     * @param parent the parent resource (folder) of the deleted resources, if <code>null</code> all deleted resources will be returned
    //     * @param includeSubFolders indicates if the sub folders of the specified folder path should be considered, too
    //     * @param deletedFrom the time stamp from which the resources should have been deleted
    //     *
    //     * @return the subscribed history resources that were deleted
    //     *
    //     * @deprecated
    //     * @see org.opencms.db.I_CmsSubscriptionDriver
    //     *
    //     * @throws CmsDataAccessException if something goes wrong
    //     */
    //    List<I_CmsHistoryResource> readSubscribedDeletedResources(
    //        CmsDbContext dbc,
    //        String poolName,
    //        CmsUser user,
    //        List<CmsGroup> groups,
    //        CmsResource parent,
    //        boolean includeSubFolders,
    //        long deletedFrom) throws CmsDataAccessException;
    //
    //    /**
    //     * Returns the resources that were subscribed by a user or group set in the filter.<p>
    //     *
    //     * @param dbc the database context
    //     * @param poolName the name of the database pool to use
    //     * @param filter the filter that is used to get the subscribed resources
    //     *
    //     * @return the resources that were subscribed by a user or group set in the filter
    //     *
    //     * @deprecated
    //     * @see org.opencms.db.I_CmsSubscriptionDriver
    //     *
    //     * @throws CmsDataAccessException if something goes wrong
    //     */
    //    List<CmsResource> readSubscribedResources(CmsDbContext dbc, String poolName, CmsSubscriptionFilter filter)
    //    throws CmsDataAccessException;

    /**
     * Reads an organizational Unit based on its fully qualified name.<p>
     *
     * @param dbc the current db context
     * @param ouFqn the fully qualified name of the organizational Unit to be read
     *
     * @return the organizational Unit with the provided fully qualified name
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsOrganizationalUnit readOrganizationalUnit(CmsDbContext dbc, String ouFqn) throws CmsDataAccessException;

    /**
     * Reads a user based on the user id.<p>
     *
     * @param dbc the current database context
     * @param id the id of the user to read
     *
     * @return the user that was read
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsUser readUser(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException;

    /**
     * Reads a user based in the user fully qualified name.<p>
     *
     * @param dbc the current database context
     * @param userFqn the fully qualified name of the user to read
     *
     * @return the user that was read
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsUser readUser(CmsDbContext dbc, String userFqn) throws CmsDataAccessException;

    /**
     * Reads a user from the database, only if the password is correct.<p>
     *
     * @param dbc the current database context
     * @param userFqn the name of the user
     * @param password the password of the user
     * @param remoteAddress the remote address of the request, may be <code>null</code>
     *
     * @return the user that was read
     *
     * @throws CmsDataAccessException if something goes wrong
     * @throws CmsPasswordEncryptionException if the password of the user could not be encrypted
     */
    CmsUser readUser(CmsDbContext dbc, String userFqn, String password, String remoteAddress)
    throws CmsDataAccessException, CmsPasswordEncryptionException;

    /**
     * Reads the user additional information map.<p>
     *
     * @param dbc the current database context
     * @param userId the id of the user to update
     *
     * @return the user additional information map
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    Map<String, Object> readUserInfos(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException;

    /**
     * Reads all users that are members of the given group.<p>
     *
     * @param dbc the current database context
     * @param groupFqn the fully qualified name of the group to read the users from
     * @param includeOtherOuUsers include users of other organizational units
     *
     * @return all <code>{@link CmsUser}</code> objects in the group
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsUser> readUsersOfGroup(CmsDbContext dbc, String groupFqn, boolean includeOtherOuUsers)
    throws CmsDataAccessException;

    /**
     * Removes all access control entries belonging to a resource.<p>
     *
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param resource the id of the resource
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void removeAccessControlEntries(CmsDbContext dbc, CmsProject project, CmsUUID resource)
    throws CmsDataAccessException;

    /**
     * Removes all access control entries belonging to a principal.<p>
     *
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param onlineProject the online project
     * @param principal the id of the principal
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void removeAccessControlEntriesForPrincipal(
        CmsDbContext dbc,
        CmsProject project,
        CmsProject onlineProject,
        CmsUUID principal) throws CmsDataAccessException;

    /**
     * Removes an access control entry.<p>
     *
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param resource the id of the resource
     * @param principal the id of the principal
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void removeAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsUUID resource, CmsUUID principal)
    throws CmsDataAccessException;

    /**
     * Removes a resource from the given organizational unit.<p>
     *
     * @param dbc the current db context
     * @param orgUnit the organizational unit to remove the resource from
     * @param resource the resource that is to be removed from the organizational unit
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void removeResourceFromOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsResource resource)
    throws CmsDataAccessException;

    /**
     * Searches for users which match the given search criteria.<p>
     *
     * @param dbc the database context
     * @param searchParams the search criteria
     *
     * @return the users which match the given criteria
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsUser> searchUsers(CmsDbContext dbc, CmsUserSearchParameters searchParams) throws CmsDataAccessException;

    //    /**
    //     * Marks a subscribed resource as deleted.<p>
    //     *
    //     * @param dbc the database context
    //     * @param poolName the name of the database pool to use
    //     * @param resource the subscribed resource to mark as deleted
    //     *
    //     * @deprecated
    //     * @see org.opencms.db.I_CmsSubscriptionDriver
    //     *
    //     * @throws CmsDataAccessException if something goes wrong
    //     */
    //    void setSubscribedResourceAsDeleted(CmsDbContext dbc, String poolName, CmsResource resource)
    //    throws CmsDataAccessException;

    /**
     * Sets the driver manager for this driver if possible.<p>
     *
     * @param driverManager the new driver manager
     */
    void setDriverManager(CmsDriverManager driverManager);

    //    /**
    //     * Subscribes the user or group to the resource.<p>
    //     *
    //     * @param dbc the database context
    //     * @param poolName the name of the database pool to use
    //     * @param principal the principal that subscribes to the resource
    //     * @param resource the resource to subscribe to
    //     *
    //     * @deprecated
    //     * @see org.opencms.db.I_CmsSubscriptionDriver
    //     *
    //     * @throws CmsDataAccessException if something goes wrong
    //     */
    //    void subscribeResourceFor(CmsDbContext dbc, String poolName, CmsPrincipal principal, CmsResource resource)
    //    throws CmsDataAccessException;
    //
    //    /**
    //    * Unsubscribes all deleted resources that were deleted before the specified time stamp.<p>
    //    *
    //    * @param dbc the database context
    //    * @param poolName the name of the database pool to use
    //    * @param deletedTo the time stamp to which the resources have been deleted
    //    *
    //    * @deprecated
    //    * @see org.opencms.db.I_CmsSubscriptionDriver
    //    *
    //    * @throws CmsDataAccessException if something goes wrong
    //    */
    //    void unsubscribeAllDeletedResources(CmsDbContext dbc, String poolName, long deletedTo)
    //    throws CmsDataAccessException;
    //
    //    /**
    //     * Unsubscribes the principal from all resources.<p>
    //     *
    //     * @param dbc the database context
    //     * @param poolName the name of the database pool to use
    //     * @param principal the principal that unsubscribes from all resources
    //     *
    //     * @deprecated
    //     * @see org.opencms.db.I_CmsSubscriptionDriver
    //     *
    //     * @throws CmsDataAccessException if something goes wrong
    //     */
    //    void unsubscribeAllResourcesFor(CmsDbContext dbc, String poolName, CmsPrincipal principal)
    //    throws CmsDataAccessException;
    //
    //    /**
    //     * Unsubscribes the principal from the resource.<p>
    //     *
    //     * @param dbc the database context
    //     * @param poolName the name of the database pool to use
    //     * @param principal the principal that unsubscribes from the resource
    //     * @param resource the resource to unsubscribe from
    //     *
    //     * @deprecated
    //     * @see org.opencms.db.I_CmsSubscriptionDriver
    //     *
    //     * @throws CmsDataAccessException if something goes wrong
    //     */
    //    void unsubscribeResourceFor(CmsDbContext dbc, String poolName, CmsPrincipal principal, CmsResource resource)
    //    throws CmsDataAccessException;
    //
    //    /**
    //     * Unsubscribes all groups and users from the resource.<p>
    //     *
    //     * @param dbc the database context
    //     * @param poolName the name of the database pool to use
    //     * @param resource the resource to unsubscribe all groups and users from
    //     *
    //     * @deprecated
    //     * @see org.opencms.db.I_CmsSubscriptionDriver
    //     *
    //     * @throws CmsDataAccessException if something goes wrong
    //     */
    //    void unsubscribeResourceForAll(CmsDbContext dbc, String poolName, CmsResource resource)
    //    throws CmsDataAccessException;

    /**
     * Sets the SQL manager for this driver if possible.<p>
     *
     * @param sqlManager the new SQL manager
     */
    void setSqlManager(CmsSqlManager sqlManager);

    /**
     * Moves an user to the given organizational unit.<p>
     *
     * @param dbc the current db context
     * @param orgUnit the organizational unit to move the user to
     * @param user the user that is to be moved to the given organizational unit
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void setUsersOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit orgUnit, CmsUser user)
    throws CmsDataAccessException;

    /**
     * Writes an access control entry.<p>
     *
     * @param dbc the current database context
     * @param project the project to write the entry
     * @param acEntry the entry to write
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeAccessControlEntry(CmsDbContext dbc, CmsProject project, CmsAccessControlEntry acEntry)
    throws CmsDataAccessException;

    /**
     * Writes an already existing group.<p>
     *
     * The group id has to be a valid OpenCms group id.<br>
     *
     * The group with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param dbc the current database context
     * @param group the group to update
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeGroup(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException;

    /**
     * Writes an already existing organizational unit.<p>
     *
     * The organizational unit id has to be a valid OpenCms organizational unit id.<br>
     *
     * The organizational unit with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param dbc the current db context
     * @param organizationalUnit the organizational unit that should be written
     *
     * @throws CmsDataAccessException if operation was not successful
     */
    void writeOrganizationalUnit(CmsDbContext dbc, CmsOrganizationalUnit organizationalUnit)
    throws CmsDataAccessException;

    /**
     * Sets a new password for a user.<p>
     *
     * @param dbc the current database context
     * @param userFqn the fullyqualified name of the user to set the password for
     * @param oldPassword the current password
     * @param newPassword the password to set
     *
     * @throws CmsDataAccessException if something goes wrong
     * @throws CmsPasswordEncryptionException if the (new) password could not be encrypted
     */
    void writePassword(CmsDbContext dbc, String userFqn, String oldPassword, String newPassword)
    throws CmsDataAccessException, CmsPasswordEncryptionException;

    /**
     * Updates the user information. <p>
     *
     * The user id has to be a valid OpenCms user id.<p>
     *
     * The user with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param dbc the current database context
     * @param user the user to update
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeUser(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException;

    /**
     * Writes an user additional information entry.<p>
     *
     * @param dbc the current database context
     * @param userId the id of the user to update
     * @param key the key of the info to write
     * @param value the value of the info to write
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeUserInfo(CmsDbContext dbc, CmsUUID userId, String key, Object value) throws CmsDataAccessException;

}

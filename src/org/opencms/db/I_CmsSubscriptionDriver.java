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

package org.opencms.db;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.main.CmsException;
import org.opencms.security.CmsPrincipal;

import java.util.List;

/**
 * The interface for drivers handling subscriptions and user tracking.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsSubscriptionDriver {

    /** The type ID to identify subscription driver implementations. */
    int DRIVER_TYPE_ID = 4;

    /**
     * Deletes visit entries matching the given filter.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use, if <code>null</code>, the default pool is used
     * @param filter the log entry filter
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteVisits(CmsDbContext dbc, String poolName, CmsVisitEntryFilter filter) throws CmsDataAccessException;

    /**
     * Returns the date when the resource was last visited by the user.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use
     * @param user the user to check the date
     * @param resource the resource to check the date
     *
     * @return the date when the resource was last visited by the user
     *
     * @throws CmsException if something goes wrong
     */
    long getDateLastVisitedBy(CmsDbContext dbc, String poolName, CmsUser user, CmsResource resource)
    throws CmsException;

    /**
     * Returns the SQL manager of this driver, if possible.<p>
     *
     * @return an SQL manager
     */
    CmsSqlManager getSqlManager();

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
     * Mark the given resource as visited by the user.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use
     * @param resource the resource to mark as visited
     * @param user the user that visited the resource
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void markResourceAsVisitedBy(CmsDbContext dbc, String poolName, CmsResource resource, CmsUser user)
    throws CmsDataAccessException;

    /**
     * Returns all resources subscribed by the given user or group.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use
     * @param principal the principal to read the subscribed resources
     *
     * @return all resources subscribed by the given user or group
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsResource> readAllSubscribedResources(CmsDbContext dbc, String poolName, CmsPrincipal principal)
    throws CmsDataAccessException;

    /**
     * Returns the resources that were visited by a user set in the filter.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use
     * @param filter the filter that is used to get the visited resources
     *
     * @return the resources that were visited by a user set in the filter
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsResource> readResourcesVisitedBy(CmsDbContext dbc, String poolName, CmsVisitedByFilter filter)
    throws CmsDataAccessException;

    /**
     * Returns the subscribed history resources that were deleted.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use
     * @param user the user that subscribed to the resource
     * @param groups the groups to check subscribed resources for
     * @param parent the parent resource (folder) of the deleted resources, if <code>null</code> all deleted resources will be returned
     * @param includeSubFolders indicates if the sub folders of the specified folder path should be considered, too
     * @param deletedFrom the time stamp from which the resources should have been deleted
     *
     * @return the subscribed history resources that were deleted
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<I_CmsHistoryResource> readSubscribedDeletedResources(
        CmsDbContext dbc,
        String poolName,
        CmsUser user,
        List<CmsGroup> groups,
        CmsResource parent,
        boolean includeSubFolders,
        long deletedFrom) throws CmsDataAccessException;

    /**
     * Returns the resources that were subscribed by a user or group set in the filter.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use
     * @param filter the filter that is used to get the subscribed resources
     *
     * @return the resources that were subscribed by a user or group set in the filter
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsResource> readSubscribedResources(CmsDbContext dbc, String poolName, CmsSubscriptionFilter filter)
    throws CmsDataAccessException;

    /**
     * Marks a subscribed resource as deleted.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use
     * @param resource the subscribed resource to mark as deleted
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void setSubscribedResourceAsDeleted(CmsDbContext dbc, String poolName, CmsResource resource)
    throws CmsDataAccessException;

    /**
     * Subscribes the user or group to the resource.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use
     * @param principal the principal that subscribes to the resource
     * @param resource the resource to subscribe to
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void subscribeResourceFor(CmsDbContext dbc, String poolName, CmsPrincipal principal, CmsResource resource)
    throws CmsDataAccessException;

    /**
     * Unsubscribes all deleted resources that were deleted before the specified time stamp.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use
     * @param deletedTo the time stamp to which the resources have been deleted
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void unsubscribeAllDeletedResources(CmsDbContext dbc, String poolName, long deletedTo)
    throws CmsDataAccessException;

    /**
     * Unsubscribes the principal from all resources.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use
     * @param principal the principal that unsubscribes from all resources
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void unsubscribeAllResourcesFor(CmsDbContext dbc, String poolName, CmsPrincipal principal)
    throws CmsDataAccessException;

    /**
     * Unsubscribes the principal from the resource.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use
     * @param principal the principal that unsubscribes from the resource
     * @param resource the resource to unsubscribe from
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void unsubscribeResourceFor(CmsDbContext dbc, String poolName, CmsPrincipal principal, CmsResource resource)
    throws CmsDataAccessException;

    /**
     * Unsubscribes all groups and users from the resource.<p>
     *
     * @param dbc the database context
     * @param poolName the name of the database pool to use
     * @param resource the resource to unsubscribe all groups and users from
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void unsubscribeResourceForAll(CmsDbContext dbc, String poolName, CmsResource resource)
    throws CmsDataAccessException;

}

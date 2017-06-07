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

package org.opencms.db.generic;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsDbConsistencyException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPreparedStatementLongParameter;
import org.opencms.db.CmsPreparedStatementStringParameter;
import org.opencms.db.CmsSubscriptionFilter;
import org.opencms.db.CmsSubscriptionReadMode;
import org.opencms.db.CmsVisitEntry;
import org.opencms.db.CmsVisitEntryFilter;
import org.opencms.db.CmsVisitedByFilter;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsPreparedStatementParameter;
import org.opencms.db.I_CmsSubscriptionDriver;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Generic implementation of the user tracking and subscription driver interface.<p>
 *
 * @since 8.0.0
 */
public class CmsSubscriptionDriver implements I_CmsDriver, I_CmsSubscriptionDriver {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.generic.CmsSubscriptionDriver.class);

    /** A reference to the driver manager used by this driver. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager used by this driver. */
    protected CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#deleteVisits(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.db.CmsVisitEntryFilter)
     */
    public void deleteVisits(CmsDbContext dbc, String poolName, CmsVisitEntryFilter filter)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (CmsStringUtil.isNotEmpty(poolName)) {
                conn = m_sqlManager.getConnection(poolName);
            } else {
                conn = m_sqlManager.getConnection(dbc);
            }

            // compose statement
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery("C_VISIT_DELETE_ENTRIES"));

            CmsPair<String, List<I_CmsPreparedStatementParameter>> conditionsAndParams = prepareVisitConditions(filter);
            queryBuf.append(conditionsAndParams.getFirst());
            if (LOG.isDebugEnabled()) {
                LOG.debug(queryBuf.toString());
            }
            stmt = m_sqlManager.getPreparedStatementForSql(conn, queryBuf.toString());
            List<I_CmsPreparedStatementParameter> params = conditionsAndParams.getSecond();
            for (int i = 0; i < params.size(); i++) {
                I_CmsPreparedStatementParameter param = conditionsAndParams.getSecond().get(i);
                param.insertIntoStatement(stmt, i + 1);
            }

            // execute
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#getDateLastVisitedBy(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsUser, org.opencms.file.CmsResource)
     */
    public long getDateLastVisitedBy(CmsDbContext dbc, String poolName, CmsUser user, CmsResource resource)
    throws CmsException {

        CmsVisitEntryFilter filter = CmsVisitEntryFilter.ALL.filterResource(resource.getStructureId()).filterUser(
            user.getId());
        List<CmsVisitEntry> entries = readVisits(dbc, poolName, filter);
        if (!entries.isEmpty()) {
            return entries.get(0).getDate();
        }
        return 0;
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#getSqlManager()
     */
    public CmsSqlManager getSqlManager() {

        return m_sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(org.opencms.db.CmsDbContext, org.opencms.configuration.CmsConfigurationManager, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(
        CmsDbContext dbc,
        CmsConfigurationManager configurationManager,
        List<String> successiveDrivers,
        CmsDriverManager driverManager) {

        CmsParameterConfiguration config = configurationManager.getConfiguration();

        String poolUrl = config.get("db.subscription.pool");
        String classname = config.get("db.subscription.sqlmanager");
        m_sqlManager = initSqlManager(classname);
        m_sqlManager.init(I_CmsSubscriptionDriver.DRIVER_TYPE_ID, poolUrl);

        m_driverManager = driverManager;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ASSIGNED_POOL_1, poolUrl));
        }

        if ((successiveDrivers != null) && !successiveDrivers.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(
                    Messages.get().getBundle().key(
                        Messages.LOG_SUCCESSIVE_DRIVERS_UNSUPPORTED_1,
                        getClass().getName()));
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#initSqlManager(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#markResourceAsVisitedBy(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsResource, org.opencms.file.CmsUser)
     */
    public void markResourceAsVisitedBy(CmsDbContext dbc, String poolName, CmsResource resource, CmsUser user)
    throws CmsDataAccessException {

        boolean entryExists = false;
        CmsVisitEntryFilter filter = CmsVisitEntryFilter.ALL.filterResource(resource.getStructureId()).filterUser(
            user.getId());
        // delete existing visited entry for the resource
        if (readVisits(dbc, OpenCms.getSubscriptionManager().getPoolName(), filter).size() > 0) {
            entryExists = true;
            deleteVisits(dbc, OpenCms.getSubscriptionManager().getPoolName(), filter);
        }

        CmsVisitEntry entry = new CmsVisitEntry(user.getId(), System.currentTimeMillis(), resource.getStructureId());
        addVisit(dbc, poolName, entry);

        if (!entryExists) {
            // new entry, check if maximum number of stored visited resources is exceeded
            PreparedStatement stmt = null;
            Connection conn = null;
            ResultSet res = null;
            int count = 0;

            List<Long> dates = new ArrayList<Long>();

            try {
                conn = m_sqlManager.getConnection(poolName);
                stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject(), "C_VISITED_USER_COUNT_1");

                stmt.setString(1, user.getId().toString());
                res = stmt.executeQuery();

                if (res.next()) {
                    count = res.getInt(1);
                    while (res.next()) {
                        // do nothing only move through all rows because of mssql odbc driver
                    }
                } else {
                    throw new CmsDbConsistencyException(
                        Messages.get().container(Messages.ERR_COUNTING_VISITED_RESOURCES_1, user.getName()));
                }

                int maxCount = OpenCms.getSubscriptionManager().getMaxVisitedCount();
                if (count > maxCount) {
                    // delete old visited entries
                    m_sqlManager.closeAll(dbc, null, stmt, res);
                    stmt = m_sqlManager.getPreparedStatement(
                        conn,
                        dbc.currentProject(),
                        "C_VISITED_USER_DELETE_GETDATE_2");

                    stmt.setString(1, user.getId().toString());
                    stmt.setInt(2, count - maxCount);
                    res = stmt.executeQuery();
                    while (res.next()) {
                        // get last date of result set
                        dates.add(Long.valueOf(res.getLong(1)));
                    }
                }
            } catch (SQLException e) {
                throw new CmsDbSqlException(
                    Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                    e);
            } finally {
                m_sqlManager.closeAll(dbc, conn, stmt, res);
            }
            long deleteDate = 0;
            for (Long date : dates) {
                deleteDate = date.longValue();
                if (deleteDate > 0) {
                    filter = CmsVisitEntryFilter.ALL.filterUser(user.getId()).filterTo(deleteDate);
                    deleteVisits(dbc, OpenCms.getSubscriptionManager().getPoolName(), filter);
                }
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#readAllSubscribedResources(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.security.CmsPrincipal)
     */
    public List<CmsResource> readAllSubscribedResources(CmsDbContext dbc, String poolName, CmsPrincipal principal)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        CmsResource currentResource = null;
        List<CmsResource> resources = new ArrayList<CmsResource>();

        try {
            conn = m_sqlManager.getConnection(poolName);
            stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject(), "C_SUBSCRIPTION_READ_ALL_1");

            stmt.setString(1, principal.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                currentResource = m_driverManager.getVfsDriver(dbc).createFile(
                    res,
                    dbc.currentProject().getUuid(),
                    false);
                resources.add(currentResource);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#readResourcesVisitedBy(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.db.CmsVisitedByFilter)
     */
    public List<CmsResource> readResourcesVisitedBy(CmsDbContext dbc, String poolName, CmsVisitedByFilter filter)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        CmsResource currentResource = null;
        StringBuffer conditions = new StringBuffer(256);
        List<String> params = new ArrayList<String>(1);
        List<CmsResource> resources = new ArrayList<CmsResource>();

        try {
            // path filter
            if (CmsStringUtil.isNotEmpty(filter.getParentPath())) {
                CmsResource parent = m_driverManager.getVfsDriver(dbc).readResource(
                    dbc,
                    dbc.currentProject().getUuid(),
                    filter.getParentPath(),
                    false);
                conditions.append(BEGIN_INCLUDE_CONDITION);
                if (filter.isIncludeSubFolders()) {
                    conditions.append(
                        m_sqlManager.readQuery(dbc.currentProject(), "C_RESOURCES_SELECT_BY_PATH_PREFIX"));
                    params.add(
                        CmsFileUtil.addTrailingSeparator(CmsVfsDriver.escapeDbWildcard(filter.getParentPath())) + "%");
                } else {
                    conditions.append(
                        m_sqlManager.readQuery(dbc.currentProject(), "C_RESOURCES_SELECT_BY_PARENT_UUID"));
                    params.add(parent.getStructureId().toString());
                }
                conditions.append(END_CONDITION);
            }

            conn = m_sqlManager.getConnection(poolName);
            String query = m_sqlManager.readQuery(dbc.currentProject(), "C_VISITED_USER_READ_4");
            query = CmsStringUtil.substitute(query, "%(CONDITIONS)", conditions.toString());
            stmt = m_sqlManager.getPreparedStatementForSql(conn, query);

            stmt.setString(1, filter.getUser().getId().toString());
            stmt.setLong(2, filter.getFromDate());
            stmt.setLong(3, filter.getToDate());
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 4, params.get(i));
            }

            res = stmt.executeQuery();

            while (res.next()) {
                currentResource = m_driverManager.getVfsDriver(dbc).createFile(
                    res,
                    dbc.currentProject().getUuid(),
                    false);
                resources.add(currentResource);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#readSubscribedDeletedResources(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsUser, java.util.List, org.opencms.file.CmsResource, boolean, long)
     */
    public List<I_CmsHistoryResource> readSubscribedDeletedResources(
        CmsDbContext dbc,
        String poolName,
        CmsUser user,
        List<CmsGroup> groups,
        CmsResource parent,
        boolean includeSubFolders,
        long deletedFrom) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        List<I_CmsHistoryResource> resources = new ArrayList<I_CmsHistoryResource>();
        Set<CmsUUID> historyIDs = new HashSet<CmsUUID>();

        List<String> principalIds = new ArrayList<String>();
        // add user ID
        principalIds.add(user.getId().toString());
        // add group IDs
        if ((groups != null) && !groups.isEmpty()) {
            Iterator<CmsGroup> it = groups.iterator();
            while (it.hasNext()) {
                principalIds.add(it.next().getId().toString());
            }
        }

        StringBuffer conditions = new StringBuffer(256);
        List<String> params = new ArrayList<String>();
        conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_DELETED"));

        if (principalIds.size() == 1) {
            // single principal filter
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_DELETED_FILTER_PRINCIPAL_SINGLE"));
            params.add(principalIds.get(0));
            conditions.append(END_CONDITION);
        } else {
            // multiple principals filter
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_DELETED_FILTER_PRINCIPALS"));
            conditions.append(BEGIN_CONDITION);
            Iterator<String> it = principalIds.iterator();
            while (it.hasNext()) {
                params.add(it.next());
                conditions.append("?");
                if (it.hasNext()) {
                    conditions.append(", ");
                }
            }
            conditions.append(END_CONDITION);
            conditions.append(END_CONDITION);
        }

        try {
            conn = m_sqlManager.getConnection(poolName);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, conditions.toString());

            // set parameters
            stmt.setLong(1, deletedFrom);
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 2, params.get(i));
            }
            res = stmt.executeQuery();
            while (res.next()) {
                historyIDs.add(new CmsUUID(res.getString(1)));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        // get the matching history resources from the found structure IDs
        String parentFolderPath = "";
        if (parent != null) {
            parentFolderPath = CmsResource.getFolderPath(parent.getRootPath());
        }
        for (Iterator<CmsUUID> i = historyIDs.iterator(); i.hasNext();) {
            CmsUUID id = i.next();
            int version = m_driverManager.getHistoryDriver(dbc).readLastVersion(dbc, id);
            if (version > 0) {
                I_CmsHistoryResource histRes = m_driverManager.getHistoryDriver(dbc).readResource(dbc, id, version);
                if (parent != null) {
                    if (!includeSubFolders
                        && !parentFolderPath.equals(CmsResource.getFolderPath(histRes.getRootPath()))) {
                        // deleted history resource is not in the specified parent folder, skip it
                        continue;
                    } else if (includeSubFolders && !histRes.getRootPath().startsWith(parentFolderPath)) {
                        // deleted history resource is not in the specified parent folder or sub folder, skip it
                        continue;
                    }
                }
                resources.add(histRes);
            }
        }

        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#readSubscribedResources(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.db.CmsSubscriptionFilter)
     */
    public List<CmsResource> readSubscribedResources(CmsDbContext dbc, String poolName, CmsSubscriptionFilter filter)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        CmsResource currentResource = null;
        List<CmsResource> resources = new ArrayList<CmsResource>();

        String queryBuf = m_sqlManager.readQuery(dbc.currentProject(), "C_SUBSCRIPTION_FILTER_READ");

        StringBuffer conditions = new StringBuffer(256);
        List<I_CmsPreparedStatementParameter> params = new ArrayList<I_CmsPreparedStatementParameter>();

        boolean userDefined = filter.getUser() != null;
        boolean groupsDefined = !filter.getGroups().isEmpty();
        if (!groupsDefined && !userDefined) {
            filter.setUser(dbc.currentUser());
            userDefined = true;
        }
        // check if a user has been set for the "visited" and "unvisited" mode
        if (!filter.getMode().isAll() && (filter.getUser() == null)) {
            // change the mode, without user the other modes are not applicable
            filter.setMode(CmsSubscriptionReadMode.ALL);
        }

        List<String> principalIds = new ArrayList<String>();
        // add user ID
        if (userDefined) {
            principalIds.add(filter.getUser().getId().toString());
        }
        // add group IDs
        if (groupsDefined) {
            Iterator<CmsGroup> it = filter.getGroups().iterator();
            while (it.hasNext()) {
                principalIds.add(it.next().getId().toString());
            }
        }

        if (principalIds.size() == 1) {
            // single principal filter
            conditions.append(BEGIN_CONDITION);
            conditions.append(m_sqlManager.readQuery(dbc.currentProject(), "C_SUBSCRIPTION_FILTER_PRINCIPAL_SINGLE"));
            params.add(new CmsPreparedStatementStringParameter(principalIds.get(0)));
            conditions.append(END_CONDITION);
        } else {
            // multiple principals filter
            conditions.append(BEGIN_CONDITION);
            conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_FILTER_PRINCIPALS"));
            conditions.append(BEGIN_CONDITION);
            Iterator<String> it = principalIds.iterator();
            while (it.hasNext()) {
                params.add(new CmsPreparedStatementStringParameter(it.next()));
                conditions.append("?");
                if (it.hasNext()) {
                    conditions.append(", ");
                }
            }
            conditions.append(END_CONDITION);
            conditions.append(m_sqlManager.readQuery(dbc.currentProject(), "C_SUBSCRIPTION_FILTER_PRINCIPALS_END"));
            conditions.append(END_CONDITION);
        }

        // path filter
        if (CmsStringUtil.isNotEmpty(filter.getParentPath())) {
            CmsResource parent = m_driverManager.getVfsDriver(dbc).readResource(
                dbc,
                dbc.currentProject().getUuid(),
                filter.getParentPath(),
                false);
            conditions.append(BEGIN_INCLUDE_CONDITION);
            if (filter.isIncludeSubFolders()) {
                conditions.append(m_sqlManager.readQuery(dbc.currentProject(), "C_RESOURCES_SELECT_BY_PATH_PREFIX"));
                params.add(
                    new CmsPreparedStatementStringParameter(
                        CmsFileUtil.addTrailingSeparator(CmsVfsDriver.escapeDbWildcard(filter.getParentPath())) + "%"));
            } else {
                conditions.append(m_sqlManager.readQuery(dbc.currentProject(), "C_RESOURCES_SELECT_BY_PARENT_UUID"));
                params.add(new CmsPreparedStatementStringParameter(parent.getStructureId().toString()));
            }
            conditions.append(END_CONDITION);
        }

        // check from and to date
        if ((filter.getFromDate() > 0) || (filter.getToDate() < Long.MAX_VALUE)) {
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(
                m_sqlManager.readQuery(dbc.currentProject(), "C_SUBSCRIPTION_FILTER_RESOURCES_DATE_MODIFIED"));
            params.add(new CmsPreparedStatementLongParameter(filter.getFromDate()));
            params.add(new CmsPreparedStatementLongParameter(filter.getToDate()));
            conditions.append(END_CONDITION);
        }

        try {
            conn = m_sqlManager.getConnection(poolName);
            queryBuf = CmsStringUtil.substitute(queryBuf, "%(CONDITIONS)", conditions.toString());
            if (LOG.isDebugEnabled()) {
                LOG.debug(queryBuf.toString());
            }
            stmt = m_sqlManager.getPreparedStatementForSql(conn, queryBuf);

            // set parameters
            for (int i = 0; i < params.size(); i++) {
                I_CmsPreparedStatementParameter param = params.get(i);
                param.insertIntoStatement(stmt, i + 1);
            }
            res = stmt.executeQuery();

            while (res.next()) {
                currentResource = m_driverManager.getVfsDriver(dbc).createFile(
                    res,
                    dbc.currentProject().getUuid(),
                    false);
                resources.add(currentResource);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        // filter the result if in visited/unvisited mode (faster as creating a query with even more joined tables)
        if (!filter.getMode().isAll()) {
            List<CmsResource> result = new ArrayList<CmsResource>(resources.size());
            for (Iterator<CmsResource> i = resources.iterator(); i.hasNext();) {
                CmsResource resource = i.next();
                long visitedDate = 0;
                try {
                    visitedDate = m_driverManager.getDateLastVisitedBy(dbc, poolName, filter.getUser(), resource);
                } catch (CmsException e) {
                    throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_0), e);
                }
                if (filter.getMode().isUnVisited() && (visitedDate >= resource.getDateLastModified())) {
                    // unvisited mode: resource was visited after the last modification, skip it
                    continue;
                }
                if (filter.getMode().isVisited() && (resource.getDateLastModified() > visitedDate)) {
                    // visited mode: resource was not visited after last modification, skip it
                    continue;
                }
                // add the current resource to the result
                result.add(resource);
            }
            resources = result;
        }
        return resources;
    }

    /**
     * Reads {@link CmsVisitEntry} objects from the database.<p>
     *
     * @param dbc the database context to use
     * @param poolName the name of the pool which should be used for the database operation
     * @param filter a filter for constraining the list of results
     *
     * @return a list of visit entries
     *
     * @throws CmsDataAccessException if the database operation fails
     */
    public List<CmsVisitEntry> readVisits(CmsDbContext dbc, String poolName, CmsVisitEntryFilter filter)
    throws CmsDataAccessException {

        List<CmsVisitEntry> entries = new ArrayList<CmsVisitEntry>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            if (CmsStringUtil.isNotEmpty(poolName)) {
                conn = m_sqlManager.getConnection(poolName);
            } else {
                conn = m_sqlManager.getConnection(dbc);
            }

            // compose statement
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery("C_VISIT_READ_ENTRIES"));
            CmsPair<String, List<I_CmsPreparedStatementParameter>> conditionsAndParameters = prepareVisitConditions(
                filter);
            List<I_CmsPreparedStatementParameter> params = conditionsAndParameters.getSecond();
            queryBuf.append(conditionsAndParameters.getFirst());

            if (LOG.isDebugEnabled()) {
                LOG.debug(queryBuf.toString());
            }
            stmt = m_sqlManager.getPreparedStatementForSql(conn, queryBuf.toString());
            for (int i = 0; i < params.size(); i++) {
                I_CmsPreparedStatementParameter param = params.get(i);
                param.insertIntoStatement(stmt, i + 1);
            }

            // execute
            res = stmt.executeQuery();
            while (res.next()) {
                // get results
                entries.add(internalReadVisitEntry(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return entries;
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#setSubscribedResourceAsDeleted(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsResource)
     */
    public void setSubscribedResourceAsDeleted(CmsDbContext dbc, String poolName, CmsResource resource)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        long deletedTime = System.currentTimeMillis();

        try {
            conn = getSqlManager().getConnection(poolName);
            // set resource as deleted for all users and groups
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SUBSCRIPTION_UPDATE_DATE_2");
            stmt.setLong(1, deletedTime);
            stmt.setString(2, resource.getStructureId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#subscribeResourceFor(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.security.CmsPrincipal, org.opencms.file.CmsResource)
     */
    public void subscribeResourceFor(CmsDbContext dbc, String poolName, CmsPrincipal principal, CmsResource resource)
    throws CmsDataAccessException {

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = getSqlManager().getConnection(poolName);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SUBSCRIPTION_CHECK_2");
            stmt.setString(1, principal.getId().toString());
            stmt.setString(2, resource.getStructureId().toString());
            res = stmt.executeQuery();

            // only create subscription entry if principal is not subscribed to resource
            if (res.next()) {
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                // subscribe principal
                m_sqlManager.closeAll(dbc, null, stmt, null);
                stmt = m_sqlManager.getPreparedStatement(conn, "C_SUBSCRIPTION_CREATE_2");
                stmt.setString(1, principal.getId().toString());
                stmt.setString(2, resource.getStructureId().toString());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#unsubscribeAllDeletedResources(org.opencms.db.CmsDbContext, java.lang.String, long)
     */
    public void unsubscribeAllDeletedResources(CmsDbContext dbc, String poolName, long deletedTo)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = getSqlManager().getConnection(poolName);
            StringBuffer conditions = new StringBuffer(256);

            // unsubscribe all deleted resources
            conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_DELETE"));
            conditions.append(BEGIN_CONDITION);
            conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_DELETE_FILTER_DATE"));
            conditions.append(END_CONDITION);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, conditions.toString());
            stmt.setLong(1, deletedTo);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#unsubscribeAllResourcesFor(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.security.CmsPrincipal)
     */
    public void unsubscribeAllResourcesFor(CmsDbContext dbc, String poolName, CmsPrincipal principal)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            if (principal != null) {
                conn = getSqlManager().getConnection(poolName);
                StringBuffer conditions = new StringBuffer(256);

                conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_DELETE"));
                conditions.append(BEGIN_CONDITION);
                conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_DELETE_FILTER_PRINCIPAL"));
                conditions.append(END_CONDITION);
                stmt = m_sqlManager.getPreparedStatementForSql(conn, conditions.toString());
                stmt.setString(1, principal.getId().toString());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#unsubscribeResourceFor(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.security.CmsPrincipal, org.opencms.file.CmsResource)
     */
    public void unsubscribeResourceFor(CmsDbContext dbc, String poolName, CmsPrincipal principal, CmsResource resource)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = getSqlManager().getConnection(poolName);
            StringBuffer conditions = new StringBuffer(256);
            conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_DELETE"));
            conditions.append(BEGIN_CONDITION);
            conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_DELETE_FILTER_PRINCIPAL"));
            conditions.append(END_CONDITION);
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_DELETE_FILTER_STRUCTURE"));
            conditions.append(END_CONDITION);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, conditions.toString());
            stmt.setString(1, principal.getId().toString());
            stmt.setString(2, resource.getStructureId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#unsubscribeResourceForAll(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsResource)
     */
    public void unsubscribeResourceForAll(CmsDbContext dbc, String poolName, CmsResource resource)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = getSqlManager().getConnection(poolName);
            StringBuffer conditions = new StringBuffer(256);

            // unsubscribe resource for all principals
            conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_DELETE"));
            conditions.append(BEGIN_CONDITION);
            conditions.append(m_sqlManager.readQuery("C_SUBSCRIPTION_DELETE_FILTER_STRUCTURE"));
            conditions.append(END_CONDITION);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, conditions.toString());
            stmt.setString(1, resource.getStructureId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * Adds an entry to the table of visits.<p>
     *
     * @param dbc the database context to use
     * @param poolName the name of the database pool to use
     * @param visit the visit bean
     *
     * @throws CmsDbSqlException if the database operation fails
     */
    protected void addVisit(CmsDbContext dbc, String poolName, CmsVisitEntry visit) throws CmsDbSqlException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (CmsStringUtil.isNotEmpty(poolName)) {
                conn = m_sqlManager.getConnection(poolName);
            } else {
                conn = m_sqlManager.getConnection(dbc);
            }

            stmt = m_sqlManager.getPreparedStatement(conn, "C_VISIT_CREATE_3");

            stmt.setString(1, visit.getUserId().toString());
            stmt.setLong(2, visit.getDate());
            stmt.setString(3, visit.getStructureId() == null ? null : visit.getStructureId().toString());
            try {
                stmt.executeUpdate();
            } catch (SQLException e) {
                // ignore, most likely a duplicate entry
                LOG.debug(
                    Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)).key(),
                    e);
            }

        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            try {
                m_sqlManager.closeAll(dbc, conn, stmt, null);
            } catch (Throwable t) {
                // this could happen during shutdown
                LOG.debug(t.getLocalizedMessage(), t);
            }
        }
    }

    /**
     * Creates a new {@link CmsVisitEntry} object from the given result set entry.<p>
     *
     * @param res the result set
     *
     * @return the new {@link CmsVisitEntry} object
     *
     * @throws SQLException if something goes wrong
     */
    protected CmsVisitEntry internalReadVisitEntry(ResultSet res) throws SQLException {

        CmsUUID userId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_VISIT_USER_ID")));
        long date = res.getLong(m_sqlManager.readQuery("C_VISIT_DATE"));
        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_VISIT_STRUCTURE_ID")));
        return new CmsVisitEntry(userId, date, structureId);
    }

    /**
     * Build the whole WHERE SQL statement part for the given visit entry filter.<p>
     *
     * @param filter the filter
     *
     * @return a pair containing both the SQL and the parameters for it
     */
    protected CmsPair<String, List<I_CmsPreparedStatementParameter>> prepareVisitConditions(
        CmsVisitEntryFilter filter) {

        List<I_CmsPreparedStatementParameter> params = new ArrayList<I_CmsPreparedStatementParameter>();
        StringBuffer conditions = new StringBuffer();

        // user id filter
        if (filter.getUserId() != null) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery("C_VISIT_FILTER_USER_ID"));
            params.add(new CmsPreparedStatementStringParameter(filter.getUserId().toString()));
            conditions.append(END_CONDITION);
        }

        // resource id filter
        if (filter.getStructureId() != null) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery("C_VISIT_FILTER_STRUCTURE_ID"));
            params.add(new CmsPreparedStatementStringParameter(filter.getStructureId().toString()));
            conditions.append(END_CONDITION);
        }

        // date from filter
        if (filter.getDateFrom() != CmsResource.DATE_RELEASED_DEFAULT) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery("C_VISIT_FILTER_DATE_FROM"));
            params.add(new CmsPreparedStatementLongParameter(filter.getDateFrom()));
            conditions.append(END_CONDITION);
        }

        // date to filter
        if (filter.getDateTo() != CmsResource.DATE_RELEASED_DEFAULT) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery("C_VISIT_FILTER_DATE_TO"));
            params.add(new CmsPreparedStatementLongParameter(filter.getDateTo()));
            conditions.append(END_CONDITION);
        }
        return CmsPair.create(conditions.toString(), params);
    }

}

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

package org.opencms.db.jpa;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsSubscriptionFilter;
import org.opencms.db.CmsSubscriptionReadMode;
import org.opencms.db.CmsVisitEntry;
import org.opencms.db.CmsVisitEntryFilter;
import org.opencms.db.CmsVisitedByFilter;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsSubscriptionDriver;
import org.opencms.db.jpa.persistence.CmsDAOSubscription;
import org.opencms.db.jpa.persistence.CmsDAOSubscriptionVisit;
import org.opencms.db.jpa.utils.CmsQueryLongParameter;
import org.opencms.db.jpa.utils.CmsQueryStringParameter;
import org.opencms.db.jpa.utils.I_CmsQueryParameter;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;

/**
 * JPA database server implementation of the subscription driver methods.<p>
 * 
 * @since 8.0.0 
 */
public class CmsSubscriptionDriver implements I_CmsDriver, I_CmsSubscriptionDriver {

    /** Query key. */
    private static final String C_RESOURCES_SELECT_BY_PARENT_UUID = "C_RESOURCES_SELECT_BY_PARENT_UUID";

    /** Query key. */
    private static final String C_RESOURCES_SELECT_BY_PATH_PREFIX = "C_RESOURCES_SELECT_BY_PATH_PREFIX";

    /** Query key. */
    private static final String C_SUBSCRIPTION_CHECK_2 = "C_SUBSCRIPTION_CHECK_2";

    /** Query key. */
    private static final String C_SUBSCRIPTION_DELETE = "C_SUBSCRIPTION_DELETE";

    /** Query key. */
    private static final String C_SUBSCRIPTION_DELETE_FILTER_DATE = "C_SUBSCRIPTION_DELETE_FILTER_DATE";

    /** Query key. */
    private static final String C_SUBSCRIPTION_DELETE_FILTER_PRINCIPAL = "C_SUBSCRIPTION_DELETE_FILTER_PRINCIPAL";

    /** Query key. */
    private static final String C_SUBSCRIPTION_DELETE_FILTER_STRUCTURE = "C_SUBSCRIPTION_DELETE_FILTER_STRUCTURE";

    /** Query key. */
    private static final String C_SUBSCRIPTION_DELETED = "C_SUBSCRIPTION_DELETED";

    /** Query key. */
    private static final String C_SUBSCRIPTION_DELETED_FILTER_PRINCIPAL_SINGLE = "C_SUBSCRIPTION_DELETED_FILTER_PRINCIPAL_SINGLE";

    /** Query key. */
    private static final String C_SUBSCRIPTION_DELETED_FILTER_PRINCIPALS = "C_SUBSCRIPTION_DELETED_FILTER_PRINCIPALS";

    /** Query key. */
    private static final String C_SUBSCRIPTION_FILTER_PRINCIPAL_SINGLE = "C_SUBSCRIPTION_FILTER_PRINCIPAL_SINGLE";

    /** Query key. */
    private static final String C_SUBSCRIPTION_FILTER_PRINCIPALS = "C_SUBSCRIPTION_FILTER_PRINCIPALS";

    /** Query key. */
    private static final String C_SUBSCRIPTION_FILTER_PRINCIPALS_END = "C_SUBSCRIPTION_FILTER_PRINCIPALS_END";

    /** Query key. */
    private static final String C_SUBSCRIPTION_FILTER_READ = "C_SUBSCRIPTION_FILTER_READ";

    /** Query key. */
    private static final String C_SUBSCRIPTION_FILTER_RESOURCES_DATE_MODIFIED = "C_SUBSCRIPTION_FILTER_RESOURCES_DATE_MODIFIED";

    /** Query key. */
    private static final String C_SUBSCRIPTION_READ_ALL_1 = "C_SUBSCRIPTION_READ_ALL_1";

    /** Query key. */
    private static final String C_SUBSCRIPTION_UPDATE_DATE_2 = "C_SUBSCRIPTION_UPDATE_DATE_2";

    /** Query key. */
    private static final String C_VISIT_DELETE_ENTRIES = "C_VISIT_DELETE_ENTRIES";

    /** Query key. */
    private static final String C_VISIT_FILTER_DATE_FROM = "C_VISIT_FILTER_DATE_FROM";

    /** Query key. */
    private static final String C_VISIT_FILTER_DATE_TO = "C_VISIT_FILTER_DATE_TO";

    /** Query key. */
    private static final String C_VISIT_FILTER_STRUCTURE_ID = "C_VISIT_FILTER_STRUCTURE_ID";

    /** Query key. */
    private static final String C_VISIT_FILTER_USER_ID = "C_VISIT_FILTER_USER_ID";

    /** Query key. */
    private static final String C_VISIT_READ_ENTRIES = "C_VISIT_READ_ENTRIES";

    /** Query key. */
    private static final String C_VISITED_USER_COUNT_1 = "C_VISITED_USER_COUNT_1";

    /** Query key. */
    private static final String C_VISITED_USER_DELETE_GETDATE_2 = "C_VISITED_USER_DELETE_GETDATE_2";

    /** Query key. */
    private static final String C_VISITED_USER_READ_4 = "C_VISITED_USER_READ_4";

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

        try {

            // compose statement 
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery(C_VISIT_DELETE_ENTRIES));

            CmsPair<String, List<I_CmsQueryParameter>> conditionsAndParams = prepareVisitConditions(filter);
            queryBuf.append(conditionsAndParams.getFirst());
            if (LOG.isDebugEnabled()) {
                LOG.debug(queryBuf.toString());
            }
            Query q = m_sqlManager.createQueryFromJPQL(dbc, queryBuf.toString());
            List<I_CmsQueryParameter> params = conditionsAndParams.getSecond();
            for (int i = 0; i < params.size(); i++) {
                I_CmsQueryParameter param = conditionsAndParams.getSecond().get(i);
                param.insertIntoQuery(q, i + 1);
            }

            // execute
            q.executeUpdate();
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_VISIT_DELETE_ENTRIES), e);
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

        // TODO: Auto-generated method stub

        CmsParameterConfiguration config = configurationManager.getConfiguration();

        String poolUrl = config.get("db.subscription.pool");
        String classname = config.get("db.subscription.sqlmanager");

        m_sqlManager = this.initSqlManager(classname);

        m_driverManager = driverManager;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ASSIGNED_POOL_1, poolUrl));
        }

        if ((successiveDrivers != null) && !successiveDrivers.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(
                    Messages.LOG_SUCCESSIVE_DRIVERS_UNSUPPORTED_1,
                    getClass().getName()));
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#initSqlManager(java.lang.String)
     */
    public CmsSqlManager initSqlManager(String classname) {

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
            int count = 0;

            try {
                Query q = m_sqlManager.createQuery(dbc, dbc.currentProject(), C_VISITED_USER_COUNT_1);

                q.setParameter(1, user.getId().toString());
                count = ((Number)q.getSingleResult()).intValue();

                int maxCount = OpenCms.getSubscriptionManager().getMaxVisitedCount();
                if (count > maxCount) {
                    // delete old visited entries
                    q = m_sqlManager.createQuery(dbc, dbc.currentProject(), C_VISITED_USER_DELETE_GETDATE_2);

                    q.setParameter(1, user.getId().toString());
                    q.setMaxResults(count - maxCount);
                    @SuppressWarnings("unchecked")
                    List<Number> res = q.getResultList();
                    long deleteDate = 0;
                    for (Number n : res) {
                        // get last date of result set
                        deleteDate = n.longValue();
                    }
                    if (deleteDate > 0) {
                        filter = CmsVisitEntryFilter.ALL.filterUser(user.getId()).filterTo(deleteDate);
                        deleteVisits(dbc, OpenCms.getSubscriptionManager().getPoolName(), filter);
                    }
                }
            } catch (PersistenceException e) {
                throw new CmsDbSqlException(Messages.get().container(
                    Messages.ERR_GENERIC_SQL_1,
                    C_VISITED_USER_DELETE_GETDATE_2), e);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#readAllSubscribedResources(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.security.CmsPrincipal)
     */
    public List<CmsResource> readAllSubscribedResources(CmsDbContext dbc, String poolName, CmsPrincipal principal)
    throws CmsDataAccessException {

        CmsResource currentResource = null;
        List<CmsResource> resources = new ArrayList<CmsResource>();

        try {
            Query q = m_sqlManager.createQuery(dbc, dbc.currentProject(), C_SUBSCRIPTION_READ_ALL_1);

            q.setParameter(1, principal.getId().toString());
            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();

            for (Object[] obj : res) {
                currentResource = ((CmsVfsDriver)m_driverManager.getVfsDriver(dbc)).createFile(
                    obj,
                    dbc.currentProject().getUuid(),
                    false);
                resources.add(currentResource);
            }
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_SUBSCRIPTION_READ_ALL_1),
                e);
        }
        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#readResourcesVisitedBy(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.db.CmsVisitedByFilter)
     */
    public List<CmsResource> readResourcesVisitedBy(CmsDbContext dbc, String poolName, CmsVisitedByFilter filter)
    throws CmsDataAccessException {

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
                    conditions.append(m_sqlManager.readQuery(dbc.currentProject(), C_RESOURCES_SELECT_BY_PATH_PREFIX));
                    params.add(CmsFileUtil.addTrailingSeparator(CmsVfsDriver.escapeDbWildcard(filter.getParentPath()))
                        + "%");
                } else {
                    conditions.append(m_sqlManager.readQuery(dbc.currentProject(), C_RESOURCES_SELECT_BY_PARENT_UUID));
                    params.add(parent.getStructureId().toString());
                }
                conditions.append(END_CONDITION);
            }

            String query = m_sqlManager.readQuery(dbc.currentProject(), C_VISITED_USER_READ_4);
            query = CmsStringUtil.substitute(query, "%(CONDITIONS)", conditions.toString());
            Query q = m_sqlManager.createQueryFromJPQL(dbc, query);

            q.setParameter(1, filter.getUser().getId().toString());
            q.setParameter(2, Long.valueOf(filter.getFromDate()));
            q.setParameter(3, Long.valueOf(filter.getToDate()));
            for (int i = 0; i < params.size(); i++) {
                q.setParameter(i + 4, params.get(i));
            }

            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();

            for (Object[] obj : res) {
                currentResource = ((CmsVfsDriver)m_driverManager.getVfsDriver(dbc)).createFile(
                    obj,
                    dbc.currentProject().getUuid(),
                    false);
                resources.add(currentResource);
            }
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_VISITED_USER_READ_4), e);
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
        conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_DELETED));

        if (principalIds.size() == 1) {
            // single principal filter
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_DELETED_FILTER_PRINCIPAL_SINGLE));
            params.add(principalIds.get(0));
            conditions.append(END_CONDITION);
        } else {
            // multiple principals filter
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_DELETED_FILTER_PRINCIPALS));
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
            Query q = m_sqlManager.createQueryFromJPQL(dbc, conditions.toString());

            // set parameters
            q.setParameter(1, Long.valueOf(deletedFrom));
            for (int i = 0; i < params.size(); i++) {
                q.setParameter(i + 2, params.get(i));
            }
            @SuppressWarnings("unchecked")
            List<String> result = q.getResultList();
            for (String id : result) {
                historyIDs.add(new CmsUUID(id));
            }
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_SUBSCRIPTION_DELETED), e);
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

        CmsResource currentResource = null;
        List<CmsResource> resources = new ArrayList<CmsResource>();

        String queryBuf = m_sqlManager.readQuery(dbc.currentProject(), C_SUBSCRIPTION_FILTER_READ);

        StringBuffer conditions = new StringBuffer(256);
        List<I_CmsQueryParameter> params = new ArrayList<I_CmsQueryParameter>();

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
            conditions.append(m_sqlManager.readQuery(dbc.currentProject(), C_SUBSCRIPTION_FILTER_PRINCIPAL_SINGLE));
            params.add(new CmsQueryStringParameter(principalIds.get(0)));
            conditions.append(END_CONDITION);
        } else {
            // multiple principals filter
            conditions.append(BEGIN_CONDITION);
            conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_FILTER_PRINCIPALS));
            conditions.append(BEGIN_CONDITION);
            Iterator<String> it = principalIds.iterator();
            while (it.hasNext()) {
                params.add(new CmsQueryStringParameter(it.next()));
                conditions.append("?");
                if (it.hasNext()) {
                    conditions.append(", ");
                }
            }
            conditions.append(END_CONDITION);
            conditions.append(m_sqlManager.readQuery(dbc.currentProject(), C_SUBSCRIPTION_FILTER_PRINCIPALS_END));
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
                conditions.append(m_sqlManager.readQuery(dbc.currentProject(), C_RESOURCES_SELECT_BY_PATH_PREFIX));
                params.add(new CmsQueryStringParameter(
                    CmsFileUtil.addTrailingSeparator(CmsVfsDriver.escapeDbWildcard(filter.getParentPath())) + "%"));
            } else {
                conditions.append(m_sqlManager.readQuery(dbc.currentProject(), C_RESOURCES_SELECT_BY_PARENT_UUID));
                params.add(new CmsQueryStringParameter(parent.getStructureId().toString()));
            }
            conditions.append(END_CONDITION);
        }

        // check from and to date
        if ((filter.getFromDate() > 0) || (filter.getToDate() < Long.MAX_VALUE)) {
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(
                dbc.currentProject(),
                C_SUBSCRIPTION_FILTER_RESOURCES_DATE_MODIFIED));
            params.add(new CmsQueryLongParameter(filter.getFromDate()));
            params.add(new CmsQueryLongParameter(filter.getToDate()));
            conditions.append(END_CONDITION);
        }

        try {
            queryBuf = CmsStringUtil.substitute(queryBuf, "%(CONDITIONS)", conditions.toString());
            if (LOG.isDebugEnabled()) {
                LOG.debug(queryBuf.toString());
            }
            Query q = m_sqlManager.createQueryFromJPQL(dbc, queryBuf);

            // set parameters
            for (int i = 0; i < params.size(); i++) {
                I_CmsQueryParameter param = params.get(i);
                param.insertIntoQuery(q, i + 1);
            }
            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();

            for (Object[] obj : res) {
                currentResource = ((CmsVfsDriver)m_driverManager.getVfsDriver(dbc)).createFile(
                    obj,
                    dbc.currentProject().getUuid(),
                    false);
                resources.add(currentResource);
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
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_SUBSCRIPTION_FILTER_READ),
                e);
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

        try {
            // compose statement 
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery(C_VISIT_READ_ENTRIES));
            CmsPair<String, List<I_CmsQueryParameter>> conditionsAndParameters = prepareVisitConditions(filter);
            List<I_CmsQueryParameter> params = conditionsAndParameters.getSecond();
            queryBuf.append(conditionsAndParameters.getFirst());

            if (LOG.isDebugEnabled()) {
                LOG.debug(queryBuf.toString());
            }
            Query q = m_sqlManager.createQueryFromJPQL(dbc, queryBuf.toString());
            for (int i = 0; i < params.size(); i++) {
                I_CmsQueryParameter param = params.get(i);
                param.insertIntoQuery(q, i + 1);
            }

            // execute
            @SuppressWarnings("unchecked")
            List<CmsDAOSubscriptionVisit> res = q.getResultList();
            for (CmsDAOSubscriptionVisit sv : res) {
                // get results
                entries.add(internalReadVisitEntry(sv));
            }
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_VISIT_READ_ENTRIES), e);
        }

        return entries;
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#setSubscribedResourceAsDeleted(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsResource)
     */
    public void setSubscribedResourceAsDeleted(CmsDbContext dbc, String poolName, CmsResource resource)
    throws CmsDataAccessException {

        long deletedTime = System.currentTimeMillis();

        try {
            // set resource as deleted for all users and groups
            Query q = m_sqlManager.createQuery(dbc, C_SUBSCRIPTION_UPDATE_DATE_2);
            q.setParameter(1, resource.getStructureId().toString());
            @SuppressWarnings("unchecked")
            List<CmsDAOSubscription> res = q.getResultList();

            for (CmsDAOSubscription s : res) {
                s.setDateDeleted(deletedTime);
            }

        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                C_SUBSCRIPTION_UPDATE_DATE_2), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#subscribeResourceFor(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.security.CmsPrincipal, org.opencms.file.CmsResource)
     */
    public void subscribeResourceFor(CmsDbContext dbc, String poolName, CmsPrincipal principal, CmsResource resource)
    throws CmsDataAccessException {

        try {
            Query q = m_sqlManager.createQuery(dbc, C_SUBSCRIPTION_CHECK_2);
            q.setParameter(1, principal.getId().toString());
            q.setParameter(2, resource.getStructureId().toString());
            @SuppressWarnings("unchecked")
            List<CmsDAOSubscription> res = q.getResultList();

            // only create subscription entry if principal is not subscribed to resource
            if (res.size() > 0) {
                // do nothing
            } else {
                // subscribe principal
                CmsDAOSubscription sb = new CmsDAOSubscription();
                sb.setPrincipalId(principal.getId().toString());
                sb.setStructureId(resource.getStructureId().toString());
                sb.setDateDeleted(0);

                m_sqlManager.persist(dbc, sb);
            }
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_SUBSCRIPTION_CHECK_2), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#unsubscribeAllDeletedResources(org.opencms.db.CmsDbContext, java.lang.String, long)
     */
    public void unsubscribeAllDeletedResources(CmsDbContext dbc, String poolName, long deletedTo)
    throws CmsDataAccessException {

        try {
            StringBuffer conditions = new StringBuffer(256);

            // unsubscribe all deleted resources
            conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_DELETE));
            conditions.append(BEGIN_CONDITION);
            conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_DELETE_FILTER_DATE));
            conditions.append(END_CONDITION);
            Query q = m_sqlManager.createQueryFromJPQL(dbc, conditions.toString());
            q.setParameter(1, Long.valueOf(deletedTo));
            @SuppressWarnings("unchecked")
            List<CmsDAOSubscription> res = q.getResultList();

            for (CmsDAOSubscription sb : res) {
                m_sqlManager.remove(dbc, sb);
            }

        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_SUBSCRIPTION_DELETE), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#unsubscribeAllResourcesFor(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.security.CmsPrincipal)
     */
    public void unsubscribeAllResourcesFor(CmsDbContext dbc, String poolName, CmsPrincipal principal)
    throws CmsDataAccessException {

        try {
            if (principal != null) {
                StringBuffer conditions = new StringBuffer(256);

                conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_DELETE));
                conditions.append(BEGIN_CONDITION);
                conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_DELETE_FILTER_PRINCIPAL));
                conditions.append(END_CONDITION);
                Query q = m_sqlManager.createQueryFromJPQL(dbc, conditions.toString());
                q.setParameter(1, principal.getId().toString());
                @SuppressWarnings("unchecked")
                List<CmsDAOSubscription> res = q.getResultList();

                for (CmsDAOSubscription sb : res) {
                    m_sqlManager.remove(dbc, sb);
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_SUBSCRIPTION_DELETE), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#unsubscribeResourceFor(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.security.CmsPrincipal, org.opencms.file.CmsResource)
     */
    public void unsubscribeResourceFor(CmsDbContext dbc, String poolName, CmsPrincipal principal, CmsResource resource)
    throws CmsDataAccessException {

        try {
            StringBuffer conditions = new StringBuffer(256);
            conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_DELETE));
            conditions.append(BEGIN_CONDITION);
            conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_DELETE_FILTER_PRINCIPAL));
            conditions.append(END_CONDITION);
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_DELETE_FILTER_STRUCTURE));
            conditions.append(END_CONDITION);
            Query q = m_sqlManager.createQueryFromJPQL(dbc, conditions.toString());
            q.setParameter(1, principal.getId().toString());
            q.setParameter(2, resource.getStructureId().toString());
            @SuppressWarnings("unchecked")
            List<CmsDAOSubscription> res = q.getResultList();

            for (CmsDAOSubscription sb : res) {
                m_sqlManager.remove(dbc, sb);
            }

        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_SUBSCRIPTION_DELETE), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsSubscriptionDriver#unsubscribeResourceForAll(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsResource)
     */
    public void unsubscribeResourceForAll(CmsDbContext dbc, String poolName, CmsResource resource)
    throws CmsDataAccessException {

        try {
            StringBuffer conditions = new StringBuffer(256);

            // unsubscribe resource for all principals
            conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_DELETE));
            conditions.append(BEGIN_CONDITION);
            conditions.append(m_sqlManager.readQuery(C_SUBSCRIPTION_DELETE_FILTER_STRUCTURE));
            conditions.append(END_CONDITION);
            Query q = m_sqlManager.createQueryFromJPQL(dbc, conditions.toString());
            q.setParameter(1, resource.getStructureId().toString());
            @SuppressWarnings("unchecked")
            List<CmsDAOSubscription> res = q.getResultList();

            for (CmsDAOSubscription sb : res) {
                m_sqlManager.remove(dbc, sb);
            }
        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_SUBSCRIPTION_DELETE), e);
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

        try {
            CmsDAOSubscriptionVisit sv = new CmsDAOSubscriptionVisit();

            sv.setUserId(visit.getUserId().toString());
            sv.setVisitDate(visit.getDate());
            sv.setStructureId(visit.getStructureId() == null ? null : visit.getStructureId().toString());

            m_sqlManager.persist(dbc, sv);

        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, ""), e);
        }
    }

    /**
     * Creates a new {@link CmsVisitEntry} object from the given result set entry.<p>
     * 
     * @param sv the result set 
     *  
     * @return the new {@link CmsVisitEntry} object
     */
    protected CmsVisitEntry internalReadVisitEntry(CmsDAOSubscriptionVisit sv) {

        CmsUUID userId = new CmsUUID(sv.getUserId());
        long date = sv.getVisitDate();
        CmsUUID structureId = new CmsUUID(sv.getStructureId());
        return new CmsVisitEntry(userId, date, structureId);
    }

    /**
     * Build the whole WHERE SQL statement part for the given visit entry filter.<p>
     * 
     * @param filter the filter
     * 
     * @return a pair containing both the SQL and the parameters for it
     */
    protected CmsPair<String, List<I_CmsQueryParameter>> prepareVisitConditions(CmsVisitEntryFilter filter) {

        List<I_CmsQueryParameter> params = new ArrayList<I_CmsQueryParameter>();
        StringBuffer conditions = new StringBuffer();

        // user id filter
        if (filter.getUserId() != null) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(C_VISIT_FILTER_USER_ID));
            params.add(new CmsQueryStringParameter(filter.getUserId().toString()));
            conditions.append(END_CONDITION);
        }

        // resource id filter
        if (filter.getStructureId() != null) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(C_VISIT_FILTER_STRUCTURE_ID));
            params.add(new CmsQueryStringParameter(filter.getStructureId().toString()));
            conditions.append(END_CONDITION);
        }

        // date from filter
        if (filter.getDateFrom() != CmsResource.DATE_RELEASED_DEFAULT) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(C_VISIT_FILTER_DATE_FROM));
            params.add(new CmsQueryLongParameter(filter.getDateFrom()));
            conditions.append(END_CONDITION);
        }

        // date to filter
        if (filter.getDateTo() != CmsResource.DATE_RELEASED_DEFAULT) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(C_VISIT_FILTER_DATE_TO));
            params.add(new CmsQueryLongParameter(filter.getDateTo()));
            conditions.append(END_CONDITION);
        }
        return CmsPair.create(conditions.toString(), params);
    }
}

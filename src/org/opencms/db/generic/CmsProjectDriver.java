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

package org.opencms.db.generic;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsAliasFilter;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbIoException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPreparedStatementIntParameter;
import org.opencms.db.CmsPreparedStatementLongParameter;
import org.opencms.db.CmsPreparedStatementStringParameter;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsResourceState;
import org.opencms.db.CmsVisitEntryFilter;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsHistoryDriver;
import org.opencms.db.I_CmsPreparedStatementParameter;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.db.log.CmsLogEntry;
import org.opencms.db.log.CmsLogEntryType;
import org.opencms.db.log.CmsLogFilter;
import org.opencms.db.userpublishlist.CmsUserPublishListEntry;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryFile;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobInfoBean;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Sets;

/**
 * Generic (ANSI-SQL) implementation of the project driver methods.<p>
 *
 * @since 6.0.0
 */
public class CmsProjectDriver implements I_CmsDriver, I_CmsProjectDriver {

    /**
     * This private class is a temporary storage for the method {@link CmsProjectDriver#readLocks(CmsDbContext)}.<p>
     */
    private class CmsTempResourceLock {

        /** The lock type. */
        private int m_lockType;

        /** The project id. */
        private CmsUUID m_projectId;

        /** The resource path. */
        private String m_resourcePath;

        /** The user id. */
        private CmsUUID m_userId;

        /**
         * The constructor.<p>
         *
         * @param resourcePath resource path
         * @param userId user id
         * @param projectId project id
         * @param lockType lock type
         */
        public CmsTempResourceLock(String resourcePath, CmsUUID userId, CmsUUID projectId, int lockType) {

            m_resourcePath = resourcePath;
            m_userId = userId;
            m_projectId = projectId;
            m_lockType = lockType;
        }

        /**
         * Returns the lockType.<p>
         *
         * @return the lockType
         */
        public int getLockType() {

            return m_lockType;
        }

        /**
         * Returns the projectId.<p>
         *
         * @return the projectId
         */
        public CmsUUID getProjectId() {

            return m_projectId;
        }

        /**
         * Returns the resourcePath.<p>
         *
         * @return the resourcePath
         */
        public String getResourcePath() {

            return m_resourcePath;
        }

        /**
         * Returns the userId.<p>
         *
         * @return the userId
         */
        public CmsUUID getUserId() {

            return m_userId;
        }

    }

    /** Attribute name for reading the project of a resource. */
    public static final String DBC_ATTR_READ_PROJECT_FOR_RESOURCE = "DBC_ATTR_READ_PROJECT_FOR_RESOURCE";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.generic.CmsProjectDriver.class);

    /** The driver manager. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager. */
    protected CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsProjectDriver#cleanupPublishHistory(org.opencms.db.CmsDbContext, org.opencms.db.generic.CmsPublishHistoryCleanupFilter)
     */
    public int cleanupPublishHistory(CmsDbContext dbc, CmsPublishHistoryCleanupFilter filter)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        int rowsAffected = 0;

        try {
            // get a JDBC connection from the OpenCms standard pool
            conn = m_sqlManager.getConnection(dbc);
            switch (filter.getMode()) {

                case single:
                default:
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_CLEANUP_PUBLISH_HISTORY_SINGLE");
                    stmt.setString(1, filter.getHistoryId().toString());
                    rowsAffected = stmt.executeUpdate();
                    break;
                case allUnreferenced:
                    String statementText = m_sqlManager.readQuery("C_CLEANUP_PUBLISH_HISTORY_ALL");
                    if (filter.getExceptions().size() > 0) {
                        List<String> parts = new ArrayList<>();
                        // it's safe to construct the clause as a string here because UUIDs can only contain dashes and hex digits
                        parts.add("'" + CmsUUID.getNullUUID() + "'");
                        for (CmsUUID id : filter.getExceptions()) {
                            parts.add("'" + id.toString() + "'");
                        }
                        String exceptionListStr = "(" + CmsStringUtil.listAsString(parts, ",") + ")";
                        statementText += " AND CMS_PUBLISH_HISTORY.HISTORY_ID NOT IN " + exceptionListStr;
                    }
                    stmt = m_sqlManager.getPreparedStatementForSql(conn, statementText);
                    rowsAffected = stmt.executeUpdate();
                    break;
            }
            LOG.info(
                "executed publish list cleanup in mode " + filter.getMode() + ", " + rowsAffected + " rows deleted");
            return rowsAffected;
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);

        }

    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createProject(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsUser, org.opencms.file.CmsGroup, org.opencms.file.CmsGroup, java.lang.String, java.lang.String, int, CmsProject.CmsProjectType)
     */
    public CmsProject createProject(
        CmsDbContext dbc,
        CmsUUID id,
        CmsUser owner,
        CmsGroup group,
        CmsGroup managergroup,
        String projectFqn,
        String description,
        int flags,
        CmsProject.CmsProjectType type)
    throws CmsDataAccessException {

        CmsProject project = null;

        if ((description == null) || (description.length() < 1)) {
            description = " ";
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // get a JDBC connection from the OpenCms standard pool
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_CREATE_10");

            stmt.setString(1, id.toString());
            stmt.setString(2, owner.getId().toString());
            stmt.setString(3, group.getId().toString());
            stmt.setString(4, managergroup.getId().toString());
            stmt.setString(5, CmsOrganizationalUnit.getSimpleName(projectFqn));
            stmt.setString(6, description);
            stmt.setInt(7, flags);
            stmt.setInt(9, type.getMode());
            stmt.setString(10, CmsOrganizationalUnit.SEPARATOR + CmsOrganizationalUnit.getParentFqn(projectFqn));

            synchronized (this) {
                long createTime = System.currentTimeMillis();
                stmt.setLong(8, createTime);
                stmt.executeUpdate();
                try {
                    // this is an ugly hack, but for MySQL (and maybe other DBs as well)
                    // there is a UNIQUE INDEX constraint on the project name+createTime
                    // so theoretically if 2 projects with the same name are created very fast, this
                    // SQL restraint would be violated if we don't wait here
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // continue
                }
                project = new CmsProject(
                    id,
                    projectFqn,
                    description,
                    owner.getId(),
                    group.getId(),
                    managergroup.getId(),
                    flags,
                    createTime,
                    type);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        return project;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createProjectResource(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String)
     */
    public void createProjectResource(CmsDbContext dbc, CmsUUID projectId, String resourcePath)
    throws CmsDataAccessException {

        // do not create entries for online-project
        PreparedStatement stmt = null;
        Connection conn = null;

        boolean projectResourceExists = false;
        try {
            readProjectResource(dbc, projectId, resourcePath);
            projectResourceExists = true;
        } catch (CmsVfsResourceNotFoundException e) {
            // resource does not exist yet, everything is okay
            projectResourceExists = false;
        }

        if (projectResourceExists) {
            throw new CmsVfsResourceAlreadyExistsException(
                Messages.get().container(
                    Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                    dbc.removeSiteRoot(resourcePath)));
        }

        try {
            conn = getSqlManager().getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_CREATE_2");

            // write new resource to the database
            stmt.setString(1, projectId.toString());
            stmt.setString(2, resourcePath);

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
     * @see org.opencms.db.I_CmsProjectDriver#createPublishJob(org.opencms.db.CmsDbContext, org.opencms.publish.CmsPublishJobInfoBean)
     */
    public void createPublishJob(CmsDbContext dbc, CmsPublishJobInfoBean publishJob) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            CmsPublishJobInfoBean currentJob = readPublishJob(dbc, publishJob.getPublishHistoryId());
            LOG.error("wanted to write: " + publishJob);
            LOG.error("already on db: " + currentJob);
            return;
        } catch (CmsDbEntryNotFoundException e) {
            // ok, this is the expected behavior
        }
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_CREATE");

            stmt.setString(1, publishJob.getPublishHistoryId().toString());
            stmt.setString(2, publishJob.getProjectId().toString());
            stmt.setString(3, publishJob.getProjectName());
            stmt.setString(4, publishJob.getUserId().toString());
            stmt.setString(5, publishJob.getLocale().toString());
            stmt.setInt(6, publishJob.getFlags());
            stmt.setInt(7, publishJob.getSize());
            stmt.setLong(8, publishJob.getEnqueueTime());
            stmt.setLong(9, publishJob.getStartTime());
            stmt.setLong(10, publishJob.getFinishTime());

            byte[] publishList = internalSerializePublishList(publishJob.getPublishList());
            if (publishList.length < 2000) {
                stmt.setBytes(11, publishList);
            } else {
                stmt.setBinaryStream(11, new ByteArrayInputStream(publishList), publishList.length);
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } catch (IOException e) {
            throw new CmsDbIoException(
                Messages.get().container(
                    Messages.ERR_SERIALIZING_PUBLISHLIST_1,
                    publishJob.getPublishHistoryId().toString()),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteAllStaticExportPublishedResources(org.opencms.db.CmsDbContext, int)
     */
    public void deleteAllStaticExportPublishedResources(CmsDbContext dbc, int linkType) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_DELETE_ALL_PUBLISHED_LINKS");
            stmt.setInt(1, linkType);
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
     * @see org.opencms.db.I_CmsProjectDriver#deleteLog(org.opencms.db.CmsDbContext, org.opencms.db.log.CmsLogFilter)
     */
    public void deleteLog(CmsDbContext dbc, CmsLogFilter filter) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            // compose statement
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery("C_LOG_DELETE_ENTRIES"));

            CmsPair<String, List<I_CmsPreparedStatementParameter>> conditionsAndParams = prepareLogConditions(filter);
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
     * @see org.opencms.db.I_CmsProjectDriver#deleteProject(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public void deleteProject(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        // delete the resources from project_resources
        deleteProjectResources(dbc, project);

        // remove the project id form all resources within their project
        unmarkProjectResources(dbc, project);

        // finally delete the project
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_DELETE_1");
            // create the statement
            stmt.setString(1, project.getUuid().toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#deleteProjectResource(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String)
     */
    public void deleteProjectResource(CmsDbContext dbc, CmsUUID projectId, String resourceName)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_DELETE_2");
            // delete resource from the database
            stmt.setString(1, projectId.toString());
            stmt.setString(2, resourceName);
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
     * @see org.opencms.db.I_CmsProjectDriver#deleteProjectResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public void deleteProjectResources(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_DELETEALL_1");
            stmt.setString(1, project.getUuid().toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishHistory(org.opencms.db.CmsDbContext, CmsUUID, int)
     */
    public void deletePublishHistory(CmsDbContext dbc, CmsUUID projectId, int maxpublishTag)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_DELETE_PUBLISH_HISTORY");
            stmt.setInt(1, maxpublishTag);
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
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishHistoryEntry(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, org.opencms.db.CmsPublishedResource)
     */
    public void deletePublishHistoryEntry(
        CmsDbContext dbc,
        CmsUUID publishHistoryId,
        CmsPublishedResource publishedResource)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_DELETE_PUBLISH_HISTORY_ENTRY");
            stmt.setString(1, publishHistoryId.toString());
            stmt.setInt(2, publishedResource.getPublishTag());
            stmt.setString(3, publishedResource.getStructureId().toString());
            stmt.setString(4, publishedResource.getRootPath());
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
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishJob(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public void deletePublishJob(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_DELETE");
            stmt.setString(1, publishHistoryId.toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishList(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public void deletePublishList(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_DELETE_PUBLISHLIST");
            stmt.setString(1, publishHistoryId.toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#deleteStaticExportPublishedResource(org.opencms.db.CmsDbContext, java.lang.String, int, java.lang.String)
     */
    public void deleteStaticExportPublishedResource(
        CmsDbContext dbc,
        String resourceName,
        int linkType,
        String linkParameter)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_DELETE_PUBLISHED_LINKS");
            stmt.setString(1, resourceName);
            stmt.setInt(2, linkType);
            stmt.setString(3, linkParameter);
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
     * @see org.opencms.db.I_CmsProjectDriver#deleteUserPublishListEntries(org.opencms.db.CmsDbContext, java.util.List)
     */
    public void deleteUserPublishListEntries(CmsDbContext dbc, List<CmsUserPublishListEntry> publishListDeletions)
    throws CmsDbSqlException {

        if (publishListDeletions.isEmpty()) {
            return;
        }
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            String sql = m_sqlManager.readQuery("C_USER_PUBLISH_LIST_DELETE_3");
            stmt = m_sqlManager.getPreparedStatementForSql(conn, sql);
            for (CmsUserPublishListEntry entry : publishListDeletions) {
                stmt.setString(1, entry.getStructureId().toString());
                stmt.setString(2, entry.getUserId() != null ? entry.getUserId().toString() : null);
                stmt.setInt(3, entry.getUserId() == null ? 1 : 0);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#destroy()
     */
    public void destroy() throws Throwable {

        m_sqlManager = null;
        m_driverManager = null;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_DRIVER_1, getClass().getName()));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#fillDefaults(org.opencms.db.CmsDbContext)
     */
    public void fillDefaults(CmsDbContext dbc) throws CmsDataAccessException {

        try {
            if (readProject(dbc, CmsProject.ONLINE_PROJECT_ID) != null) {
                // online-project exists - no need of filling defaults
                return;
            }
        } catch (CmsDataAccessException exc) {
            // ignore the exception - the project was not readable so fill in the defaults
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_FILL_DEFAULTS_0));
        }

        String adminUser = OpenCms.getDefaultUsers().getUserAdmin();
        CmsUser admin = m_driverManager.readUser(dbc, adminUser);

        String administratorsGroup = OpenCms.getDefaultUsers().getGroupAdministrators();
        CmsGroup administrators = m_driverManager.readGroup(dbc, administratorsGroup);

        String usersGroup = OpenCms.getDefaultUsers().getGroupUsers();
        CmsGroup users = m_driverManager.readGroup(dbc, usersGroup);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // online project stuff
        ////////////////////////////////////////////////////////////////////////////////////////////

        // create the online project
        CmsProject onlineProject = createProject(
            dbc,
            CmsProject.ONLINE_PROJECT_ID,
            admin,
            users,
            administrators,
            CmsProject.ONLINE_PROJECT_NAME,
            "The Online project",
            I_CmsPrincipal.FLAG_ENABLED,
            CmsProject.PROJECT_TYPE_NORMAL);

        // create the root-folder for the online project
        CmsFolder rootFolder = new CmsFolder(
            new CmsUUID(),
            new CmsUUID(),
            "/",
            CmsResourceTypeFolder.RESOURCE_TYPE_ID,
            0,
            onlineProject.getUuid(),
            CmsResource.STATE_CHANGED,
            0,
            admin.getId(),
            0,
            admin.getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            0);

        m_driverManager.getVfsDriver(dbc).createResource(dbc, onlineProject.getUuid(), rootFolder, null);

        // important: must access through driver manager to ensure proper cascading
        m_driverManager.getProjectDriver(dbc).createProjectResource(
            dbc,
            onlineProject.getUuid(),
            rootFolder.getRootPath());

        // create the system-folder for the online project
        CmsFolder systemFolder = new CmsFolder(
            new CmsUUID(),
            new CmsUUID(),
            "/system",
            CmsResourceTypeFolder.RESOURCE_TYPE_ID,
            0,
            onlineProject.getUuid(),
            CmsResource.STATE_CHANGED,
            0,
            admin.getId(),
            0,
            admin.getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            0);

        m_driverManager.getVfsDriver(dbc).createResource(dbc, onlineProject.getUuid(), systemFolder, null);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // setup project stuff
        ////////////////////////////////////////////////////////////////////////////////////////////

        // important: must access through driver manager to ensure proper cascading
        CmsProject setupProject = m_driverManager.getProjectDriver(dbc).createProject(
            dbc,
            CmsUUID.getConstantUUID(SETUP_PROJECT_NAME),
            admin,
            administrators,
            administrators,
            SETUP_PROJECT_NAME,
            "The Project for the initial import",
            I_CmsPrincipal.FLAG_ENABLED,
            CmsProject.PROJECT_TYPE_TEMPORARY);

        rootFolder.setState(CmsResource.STATE_CHANGED);
        // create the root-folder for the offline project
        CmsResource offlineRootFolder = m_driverManager.getVfsDriver(
            dbc).createResource(dbc, setupProject.getUuid(), rootFolder, null);

        offlineRootFolder.setState(CmsResource.STATE_UNCHANGED);

        m_driverManager.getVfsDriver(
            dbc).writeResource(dbc, setupProject.getUuid(), offlineRootFolder, CmsDriverManager.NOTHING_CHANGED);

        // important: must access through driver manager to ensure proper cascading
        m_driverManager.getProjectDriver(dbc).createProjectResource(
            dbc,
            setupProject.getUuid(),
            offlineRootFolder.getRootPath());

        systemFolder.setState(CmsResource.STATE_CHANGED);
        // create the system-folder for the offline project
        CmsResource offlineSystemFolder = m_driverManager.getVfsDriver(
            dbc).createResource(dbc, setupProject.getUuid(), systemFolder, null);

        offlineSystemFolder.setState(CmsResource.STATE_UNCHANGED);

        m_driverManager.getVfsDriver(
            dbc).writeResource(dbc, setupProject.getUuid(), offlineSystemFolder, CmsDriverManager.NOTHING_CHANGED);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#getSqlManager()
     */
    public CmsSqlManager getSqlManager() {

        return m_sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#getUsersPubList(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public List<CmsResource> getUsersPubList(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {

        List<CmsResource> result = new ArrayList<CmsResource>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            String sql = m_sqlManager.readQuery("C_USER_PUBLISH_LIST_READ_1");
            sql = sql.replace("${PROJECT}", "OFFLINE");
            stmt = m_sqlManager.getPreparedStatementForSql(conn, sql);
            stmt.setString(1, userId.toString());
            res = stmt.executeQuery();
            while (res.next()) {
                CmsResource resource = m_driverManager.getVfsDriver(dbc).createResource(
                    res,
                    dbc.currentProject().getUuid());
                long date = res.getLong("DATE_CHANGED");
                resource.setDateLastModified(date);
                result.add(resource);
            }
            return result;
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(CmsDbContext, CmsConfigurationManager, List, CmsDriverManager)
     */
    public void init(
        CmsDbContext dbc,
        CmsConfigurationManager configurationManager,
        List<String> successiveDrivers,
        CmsDriverManager driverManager) {

        CmsParameterConfiguration configuration = configurationManager.getConfiguration();
        String poolUrl = configuration.get("db.project.pool");
        String classname = configuration.get("db.project.sqlmanager");
        m_sqlManager = initSqlManager(classname);
        m_sqlManager.init(I_CmsProjectDriver.DRIVER_TYPE_ID, poolUrl);

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
     * @see org.opencms.db.I_CmsProjectDriver#initSqlManager(String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#log(org.opencms.db.CmsDbContext, java.util.List)
     */
    public void log(CmsDbContext dbc, List<CmsLogEntry> logEntries) throws CmsDbSqlException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LOG_CREATE_5");

            for (CmsLogEntry logEntry : logEntries) {
                stmt.setString(1, logEntry.getUserId().toString());
                stmt.setLong(2, logEntry.getDate());
                stmt.setString(3, logEntry.getStructureId() == null ? null : logEntry.getStructureId().toString());
                stmt.setInt(4, logEntry.getType().getId());
                stmt.setString(5, CmsStringUtil.arrayAsString(logEntry.getData(), "|"));
                try {
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    // ignore, most likely a duplicate entry
                    LOG.debug(
                        Messages.get().container(
                            Messages.ERR_GENERIC_SQL_1,
                            CmsDbSqlException.getErrorQuery(stmt)).key(),
                        e);
                }
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
     * @see org.opencms.db.I_CmsProjectDriver#publishDeletedFolder(org.opencms.db.CmsDbContext, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsFolder, org.opencms.util.CmsUUID, int)
     */
    public void publishDeletedFolder(
        CmsDbContext dbc,
        I_CmsReport report,
        int m,
        int n,
        CmsProject onlineProject,
        CmsFolder currentFolder,
        CmsUUID publishHistoryId,
        int publishTag)
    throws CmsDataAccessException {

        try {
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(m),
                    String.valueOf(n)),
                I_CmsReport.FORMAT_NOTE);
            report.print(Messages.get().container(Messages.RPT_DELETE_FOLDER_0), I_CmsReport.FORMAT_NOTE);
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(currentFolder.getRootPath())));
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            CmsResourceState folderState = fixMovedResource(
                dbc,
                onlineProject,
                currentFolder,
                publishHistoryId,
                publishTag);

            // read the folder online
            CmsFolder onlineFolder = m_driverManager.readFolder(
                dbc,
                currentFolder.getRootPath(),
                CmsResourceFilter.ALL);

            // if the folder in the online-project contains any files, these need to be removed.
            // this can occur if these files were moved in the offline-project
            List<CmsResource> movedFiles = null;
            I_CmsVfsDriver vfsDriver = m_driverManager.getVfsDriver(dbc);
            movedFiles = vfsDriver.readResourceTree(
                dbc,
                onlineProject.getUuid(),
                currentFolder.getRootPath(),
                CmsDriverManager.READ_IGNORE_TYPE,
                null,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READ_IGNORE_TIME,
                CmsDriverManager.READMODE_INCLUDE_TREE);

            for (CmsResource delFile : movedFiles) {
                try {
                    CmsResource offlineResource = vfsDriver.readResource(
                        dbc,
                        dbc.currentProject().getUuid(),
                        delFile.getStructureId(),
                        true);
                    if (offlineResource.isFile()) {
                        CmsFile offlineFile = new CmsFile(offlineResource);
                        offlineFile.setContents(
                            vfsDriver.readContent(dbc, dbc.currentProject().getUuid(), offlineFile.getResourceId()));
                        internalWriteHistory(
                            dbc,
                            offlineFile,
                            CmsResource.STATE_DELETED,
                            null,
                            publishHistoryId,
                            publishTag);
                        vfsDriver.deletePropertyObjects(
                            dbc,
                            onlineProject.getUuid(),
                            delFile,
                            CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                        vfsDriver.removeFile(dbc, onlineProject.getUuid(), delFile);
                    } else if (offlineResource.isFolder()) {

                        internalWriteHistory(
                            dbc,
                            offlineResource,
                            CmsResource.STATE_DELETED,
                            null,
                            publishHistoryId,
                            publishTag);
                        vfsDriver.deletePropertyObjects(
                            dbc,
                            onlineProject.getUuid(),
                            delFile,
                            CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                        vfsDriver.removeFolder(dbc, onlineProject, delFile);
                    }
                } catch (CmsDataAccessException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(Messages.LOG_REMOVING_RESOURCE_1, delFile.getName()),
                            e);
                    }
                }
            }

            // write history before deleting
            internalWriteHistory(dbc, currentFolder, folderState, null, publishHistoryId, publishTag);

            try {
                // delete the properties online and offline
                vfsDriver.deletePropertyObjects(
                    dbc,
                    onlineProject.getUuid(),
                    onlineFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                vfsDriver.deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getUuid(),
                    currentFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_DELETING_PROPERTIES_1, currentFolder.getRootPath()),
                        e);
                }
                throw e;
            }

            try {
                // remove the folder online and offline
                vfsDriver.removeFolder(dbc, dbc.currentProject(), currentFolder);
                vfsDriver.removeFolder(dbc, onlineProject, currentFolder);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_REMOVING_RESOURCE_1, currentFolder.getRootPath()),
                        e);
                }
                throw e;
            }

            try {
                // remove the ACL online and offline
                m_driverManager.getUserDriver(dbc).removeAccessControlEntries(
                    dbc,
                    onlineProject,
                    onlineFolder.getResourceId());
                m_driverManager.getUserDriver(dbc).removeAccessControlEntries(
                    dbc,
                    dbc.currentProject(),
                    currentFolder.getResourceId());
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_REMOVING_ACL_1, currentFolder.getRootPath()),
                        e);
                }
                throw e;
            }

            // remove relations
            try {
                vfsDriver.deleteRelations(dbc, onlineProject.getUuid(), onlineFolder, CmsRelationFilter.TARGETS);
                vfsDriver.deleteRelations(
                    dbc,
                    dbc.currentProject().getUuid(),
                    currentFolder,
                    CmsRelationFilter.TARGETS);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_REMOVING_RELATIONS_1, currentFolder.getRootPath()),
                        e);
                }
                throw e;
            }

            // remove project resources
            String deletedResourceRootPath = currentFolder.getRootPath();
            Iterator<CmsProject> itProjects = readProjectsForResource(dbc, deletedResourceRootPath).iterator();
            while (itProjects.hasNext()) {
                CmsProject project = itProjects.next();
                deleteProjectResource(dbc, project.getUuid(), deletedResourceRootPath);
            }

            try {
                m_driverManager.getVfsDriver(dbc).deleteAliases(
                    dbc,
                    onlineProject,
                    new CmsAliasFilter(null, null, currentFolder.getStructureId()));
            } catch (CmsDataAccessException e) {
                LOG.error("Could not delete aliases: " + e.getLocalizedMessage(), e);
            }

            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);

            if (LOG.isDebugEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_DEL_FOLDER_3,
                            currentFolder.getRootPath(),
                            String.valueOf(m),
                            String.valueOf(n)));
                }
            }
        } finally {
            // notify the app. that the published folder and it's properties have been modified offline
            OpenCms.fireCmsEvent(
                new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCE, currentFolder)));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFile(org.opencms.db.CmsDbContext, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.Set, org.opencms.util.CmsUUID, int)
     */
    public void publishFile(
        CmsDbContext dbc,
        I_CmsReport report,
        int m,
        int n,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set<CmsUUID> publishedContentIds,
        CmsUUID publishHistoryId,
        int publishTag)
    throws CmsDataAccessException {

        /*
         * Never use onlineResource.getState() here!
         * Only use offlineResource.getState() to determine the state of an offline resource!
         *
         * In case a resource has siblings, after a sibling was published the structure
         * and resource states are reset to UNCHANGED -> the state of the corresponding
         * onlineResource is still NEW, DELETED or CHANGED.
         * Thus, using onlineResource.getState() will inevitably result in unpublished resources!
         */

        try {
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(m),
                    String.valueOf(n)),
                I_CmsReport.FORMAT_NOTE);

            if (offlineResource.getState().isDeleted()) {
                report.print(Messages.get().container(Messages.RPT_DELETE_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                publishDeletedFile(dbc, onlineProject, offlineResource, publishHistoryId, publishTag);

                dbc.pop();
                List<CmsProperty> props = m_driverManager.readPropertyObjects(dbc, offlineResource, true);
                boolean removeDeleted = Boolean.parseBoolean(
                    CmsProperty.get(CmsPropertyDefinition.PROPERTY_HISTORY_REMOVE_DELETED, props).getValue("false"));
                // delete old historical entries
                m_driverManager.getHistoryDriver(dbc).deleteEntries(
                    dbc,
                    new CmsHistoryFile(offlineResource),
                    removeDeleted ? 0 : OpenCms.getSystemInfo().getHistoryVersionsAfterDeletion(),
                    -1);

                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_DEL_FILE_3,
                            String.valueOf(m),
                            String.valueOf(n),
                            offlineResource.getRootPath()));
                }

            } else if (offlineResource.getState().isChanged()) {
                report.print(Messages.get().container(Messages.RPT_PUBLISH_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                publishChangedFile(
                    dbc,
                    onlineProject,
                    offlineResource,
                    publishedContentIds,
                    publishHistoryId,
                    publishTag);

                dbc.pop();
                // delete old historical entries
                m_driverManager.getHistoryDriver(dbc).deleteEntries(
                    dbc,
                    new CmsHistoryFile(offlineResource),
                    OpenCms.getSystemInfo().getHistoryVersions(),
                    -1);

                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_PUBLISHING_FILE_3,
                            offlineResource.getRootPath(),
                            String.valueOf(m),
                            String.valueOf(n)));
                }
            } else if (offlineResource.getState().isNew()) {
                report.print(Messages.get().container(Messages.RPT_PUBLISH_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                publishNewFile(dbc, onlineProject, offlineResource, publishedContentIds, publishHistoryId, publishTag);

                dbc.pop();
                // delete old historical entries
                m_driverManager.getHistoryDriver(dbc).deleteEntries(
                    dbc,
                    new CmsHistoryFile(offlineResource),
                    OpenCms.getSystemInfo().getHistoryVersions(),
                    -1);

                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_PUBLISHING_FILE_3,
                                offlineResource.getRootPath(),
                                String.valueOf(m),
                                String.valueOf(n)));
                    }
                }
            } else {
                // state == unchanged !!?? something went really wrong
                report.print(Messages.get().container(Messages.RPT_PUBLISH_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                    I_CmsReport.FORMAT_ERROR);

                if (LOG.isErrorEnabled()) {
                    // the whole resource is printed out here
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_PUBLISHING_FILE_3,
                            String.valueOf(m),
                            String.valueOf(n),
                            offlineResource));
                }
            }
            m_driverManager.publishUrlNameMapping(dbc, offlineResource);
            if (offlineResource.getState().isDeleted()) {
                m_driverManager.getVfsDriver(dbc).deleteAliases(
                    dbc,
                    onlineProject,
                    new CmsAliasFilter(null, null, offlineResource.getStructureId()));
            }
        } catch (CmsException e) {
            throw new CmsDataAccessException(e.getMessageContainer(), e);
        } finally {
            // notify the app. that the published file and it's properties have been modified offline
            Map<String, Object> data = new HashMap<String, Object>(2);
            data.put(I_CmsEventListener.KEY_RESOURCE, offlineResource);
            data.put(I_CmsEventListener.KEY_SKIPINDEX, Boolean.valueOf(true));

            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, data));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFileContent(CmsDbContext, CmsProject, CmsProject, CmsResource, Set, boolean, int)
     */
    public CmsFile publishFileContent(
        CmsDbContext dbc,
        CmsProject offlineProject,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set<CmsUUID> publishedResourceIds,
        boolean needToUpdateContent,
        int publishTag)
    throws CmsDataAccessException {

        CmsFile newFile = null;
        try {
            // read the file content offline
            CmsUUID projectId = dbc.getProjectId();
            boolean dbcHasProjectId = (projectId != null) && !projectId.isNullUUID();
            CmsUUID projectIdForReading = (!dbcHasProjectId ? offlineProject.getUuid() : CmsProject.ONLINE_PROJECT_ID);
            dbc.setProjectId(offlineProject.getUuid());
            byte[] offlineContent = m_driverManager.getVfsDriver(dbc).readContent(
                dbc,
                projectIdForReading,
                offlineResource.getResourceId());
            CmsFile offlineFile = new CmsFile(offlineResource);
            offlineFile.setContents(offlineContent);
            dbc.setProjectId(projectId);

            // create the file online
            newFile = (CmsFile)offlineFile.clone();
            newFile.setState(CmsResource.STATE_UNCHANGED);

            boolean createSibling = true;
            // check if we are facing with a create new sibling operation
            if (!offlineFile.getState().isNew()) {
                createSibling = false;
            } else {
                // check if the resource entry already exists
                if (!m_driverManager.getVfsDriver(dbc).validateResourceIdExists(
                    dbc,
                    onlineProject.getUuid(),
                    offlineFile.getResourceId())) {
                    // we are creating a normal resource and not a sibling
                    createSibling = false;
                }
            }

            // only update the content if it was not updated before
            boolean alreadyPublished = publishedResourceIds.contains(offlineResource.getResourceId());
            needToUpdateContent &= !alreadyPublished;

            if (createSibling) {
                if (!alreadyPublished) {
                    // create the file online, the first time a sibling is published also the resource entry has to be actualized
                    m_driverManager.getVfsDriver(dbc).createResource(dbc, onlineProject.getUuid(), newFile, null);
                } else {
                    // create the sibling online
                    m_driverManager.getVfsDriver(dbc).createSibling(dbc, onlineProject, offlineResource);
                }
                newFile = new CmsFile(offlineResource);
                newFile.setContents(offlineContent);
            } else {
                // update the online/offline structure and resource records of the file
                m_driverManager.getVfsDriver(dbc).publishResource(dbc, onlineProject, newFile, offlineFile);
            }
            // update version numbers
            m_driverManager.getVfsDriver(dbc).publishVersions(dbc, offlineResource, !alreadyPublished);

            // create/update the content
            m_driverManager.getVfsDriver(dbc).createOnlineContent(
                dbc,
                offlineFile.getResourceId(),
                offlineFile.getContents(),
                publishTag,
                true,
                needToUpdateContent);

            // mark the resource as written to avoid that the same content is written for each sibling instance
            publishedResourceIds.add(offlineResource.getResourceId());
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_PUBLISHING_FILE_CONTENT_1, offlineResource.toString()),
                    e);
            }
            throw e;
        }
        return newFile;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFolder(org.opencms.db.CmsDbContext, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsFolder, org.opencms.util.CmsUUID, int)
     */
    public void publishFolder(
        CmsDbContext dbc,
        I_CmsReport report,
        int m,
        int n,
        CmsProject onlineProject,
        CmsFolder offlineFolder,
        CmsUUID publishHistoryId,
        int publishTag)
    throws CmsDataAccessException {

        try {
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(m),
                    String.valueOf(n)),
                I_CmsReport.FORMAT_NOTE);
            report.print(Messages.get().container(Messages.RPT_PUBLISH_FOLDER_0), I_CmsReport.FORMAT_NOTE);
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(offlineFolder.getRootPath())));
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            CmsResourceState resourceState = fixMovedResource(
                dbc,
                onlineProject,
                offlineFolder,
                publishHistoryId,
                publishTag);

            CmsResource onlineFolder = null;
            if (offlineFolder.getState().isNew()) {
                try {
                    // create the folder online
                    CmsResource newFolder = (CmsFolder)offlineFolder.clone();
                    newFolder.setState(CmsResource.STATE_UNCHANGED);

                    onlineFolder = m_driverManager.getVfsDriver(
                        dbc).createResource(dbc, onlineProject.getUuid(), newFolder, null);
                    m_driverManager.getVfsDriver(dbc).publishResource(dbc, onlineProject, onlineFolder, offlineFolder);
                    // update version numbers
                    m_driverManager.getVfsDriver(dbc).publishVersions(dbc, offlineFolder, true);
                } catch (CmsVfsResourceAlreadyExistsException e) {
                    if (!offlineFolder.getRootPath().equals("/")
                        && !offlineFolder.getRootPath().equals("/system/")
                        && LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_WARN_FOLDER_WRONG_STATE_CN_1,
                                offlineFolder.getRootPath()));
                    }
                    try {
                        onlineFolder = m_driverManager.getVfsDriver(dbc).readFolder(
                            dbc,
                            onlineProject.getUuid(),
                            offlineFolder.getRootPath());
                        m_driverManager.getVfsDriver(
                            dbc).publishResource(dbc, onlineProject, onlineFolder, offlineFolder);
                        // update version numbers
                        m_driverManager.getVfsDriver(dbc).publishVersions(dbc, offlineFolder, true);
                    } catch (CmsDataAccessException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(
                                Messages.get().getBundle().key(
                                    Messages.LOG_READING_RESOURCE_1,
                                    offlineFolder.getRootPath()),
                                e);
                        }
                        throw e1;
                    }
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.LOG_PUBLISHING_RESOURCE_1,
                                offlineFolder.getRootPath()),
                            e);
                    }
                    throw e;
                }
            } else if (offlineFolder.getState().isChanged()) {
                try {
                    // read the folder online
                    onlineFolder = m_driverManager.getVfsDriver(dbc).readFolder(
                        dbc,
                        onlineProject.getUuid(),
                        offlineFolder.getStructureId());
                } catch (CmsVfsResourceNotFoundException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_WARN_FOLDER_WRONG_STATE_NC_1,
                                offlineFolder.getRootPath()));
                    }
                    try {
                        onlineFolder = m_driverManager.getVfsDriver(
                            dbc).createResource(dbc, onlineProject.getUuid(), offlineFolder, null);
                        internalResetResourceState(dbc, onlineFolder);
                    } catch (CmsDataAccessException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(
                                Messages.get().getBundle().key(
                                    Messages.LOG_PUBLISHING_RESOURCE_1,
                                    offlineFolder.getRootPath()),
                                e);
                        }
                        throw e1;
                    }
                }

                try {
                    // update the folder online
                    m_driverManager.getVfsDriver(dbc).publishResource(dbc, onlineProject, onlineFolder, offlineFolder);
                    // update version numbers
                    m_driverManager.getVfsDriver(dbc).publishVersions(dbc, offlineFolder, true);
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.LOG_PUBLISHING_RESOURCE_1,
                                offlineFolder.getRootPath()),
                            e);
                    }
                    throw e;
                }
            }

            if (onlineFolder != null) {
                try {
                    // write the ACL online
                    m_driverManager.getUserDriver(dbc).publishAccessControlEntries(
                        dbc,
                        dbc.currentProject(),
                        onlineProject,
                        offlineFolder.getResourceId(),
                        onlineFolder.getResourceId());
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().getBundle().key(Messages.LOG_PUBLISHING_ACL_1, offlineFolder.getRootPath()),
                            e);
                    }
                    throw e;
                }
            }

            List<CmsProperty> offlineProperties = null;
            try {
                // write the properties online
                m_driverManager.getVfsDriver(dbc).deletePropertyObjects(
                    dbc,
                    onlineProject.getUuid(),
                    onlineFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                offlineProperties = m_driverManager.getVfsDriver(dbc).readPropertyObjects(
                    dbc,
                    dbc.currentProject(),
                    offlineFolder);
                CmsProperty.setAutoCreatePropertyDefinitions(offlineProperties, true);
                m_driverManager.getVfsDriver(
                    dbc).writePropertyObjects(dbc, onlineProject, onlineFolder, offlineProperties);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_PUBLISHING_PROPERTIES_1,
                            offlineFolder.getRootPath()),
                        e);
                }
                throw e;
            }

            internalWriteHistory(dbc, offlineFolder, resourceState, offlineProperties, publishHistoryId, publishTag);

            m_driverManager.getVfsDriver(dbc).updateRelations(dbc, onlineProject, offlineFolder);

            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);

            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_PUBLISHING_FOLDER_3,
                        String.valueOf(m),
                        String.valueOf(n),
                        offlineFolder.getRootPath()));
            }
        } finally {
            // notify the app. that the published folder and it's properties have been modified offline
            OpenCms.fireCmsEvent(
                new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCE, offlineFolder)));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishProject(org.opencms.db.CmsDbContext, org.opencms.report.I_CmsReport, org.opencms.file.CmsProject, org.opencms.db.CmsPublishList, int)
     */
    public void publishProject(
        CmsDbContext dbc,
        I_CmsReport report,
        CmsProject onlineProject,
        CmsPublishList publishList,
        int publishTag)
    throws CmsException {

        int publishedFolderCount = 0;
        int deletedFolderCount = 0;
        int publishedFileCount = 0;
        Set<CmsUUID> publishedContentIds = new HashSet<CmsUUID>();
        Set<CmsUUID> publishedIds = new HashSet<CmsUUID>();

        try {

            ////////////////////////////////////////////////////////////////////////////////////////
            // write the historical project entry

            if (OpenCms.getSystemInfo().isHistoryEnabled()) {
                try {
                    // write an entry in the publish project log
                    m_driverManager.getHistoryDriver(dbc).writeProject(dbc, publishTag, System.currentTimeMillis());
                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(
                        report,
                        Messages.get().container(
                            Messages.ERR_WRITING_HISTORY_OF_PROJECT_1,
                            dbc.currentProject().getName()),
                        t);
                }
            }

            ///////////////////////////////////////////////////////////////////////////////////////
            // publish new/changed folders

            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_START_PUBLISHING_PROJECT_2,
                        dbc.currentProject().getName(),
                        dbc.currentUser().getName()));
            }

            publishedFolderCount = 0;
            int foldersSize = publishList.getFolderList().size();
            if (foldersSize > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_FOLDERS_BEGIN_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }

            Iterator<CmsResource> itFolders = publishList.getFolderList().iterator();
            I_CmsProjectDriver projectDriver = m_driverManager.getProjectDriver(dbc);
            I_CmsHistoryDriver historyDriver = m_driverManager.getHistoryDriver(dbc);
            while (itFolders.hasNext()) {
                CmsResource currentFolder = itFolders.next();
                try {
                    if (currentFolder.getState().isNew() || currentFolder.getState().isChanged()) {
                        // bounce the current publish task through all project drivers
                        projectDriver.publishFolder(
                            dbc,
                            report,
                            ++publishedFolderCount,
                            foldersSize,
                            onlineProject,
                            new CmsFolder(currentFolder),
                            publishList.getPublishHistoryId(),
                            publishTag);

                        dbc.pop();

                        publishedIds.add(currentFolder.getStructureId());
                        // log it
                        CmsLogEntryType type = currentFolder.getState().isNew()
                        ? CmsLogEntryType.RESOURCE_PUBLISHED_NEW
                        : CmsLogEntryType.RESOURCE_PUBLISHED_MODIFIED;
                        m_driverManager.log(
                            dbc,
                            new CmsLogEntry(
                                dbc,
                                currentFolder.getStructureId(),
                                type,
                                new String[] {currentFolder.getRootPath()}),
                            true);

                        // delete old historical entries
                        historyDriver.deleteEntries(
                            dbc,
                            new CmsHistoryFile(currentFolder),
                            OpenCms.getSystemInfo().getHistoryVersions(),
                            -1);

                        // reset the resource state to UNCHANGED and the last-modified-in-project-ID to 0
                        internalResetResourceState(dbc, currentFolder);

                        m_driverManager.unlockResource(dbc, currentFolder, true, true);
                    } else {
                        // state == unchanged !!?? something went really wrong
                        report.print(Messages.get().container(Messages.RPT_PUBLISH_FOLDER_0), I_CmsReport.FORMAT_NOTE);
                        report.print(
                            org.opencms.report.Messages.get().container(
                                org.opencms.report.Messages.RPT_ARGUMENT_1,
                                dbc.removeSiteRoot(currentFolder.getRootPath())));
                        report.print(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                        report.println(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                            I_CmsReport.FORMAT_ERROR);

                        if (LOG.isErrorEnabled()) {
                            // the whole resource is printed out here
                            LOG.error(
                                Messages.get().getBundle().key(
                                    Messages.LOG_PUBLISHING_FILE_3,
                                    String.valueOf(++publishedFolderCount),
                                    String.valueOf(foldersSize),
                                    currentFolder));
                        }
                    }

                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(
                        report,
                        Messages.get().container(Messages.ERR_ERROR_PUBLISHING_FOLDER_1, currentFolder.getRootPath()),
                        t);
                }
            }

            if (foldersSize > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_FOLDERS_END_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }

            ///////////////////////////////////////////////////////////////////////////////////////
            // publish changed/new/deleted files

            publishedFileCount = 0;
            int filesSize = publishList.getFileList().size();

            if (filesSize > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_FILES_BEGIN_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }

            Set<CmsUUID> deletedResourceIds = new HashSet<CmsUUID>();
            Set<CmsUUID> changedResourceIds = new HashSet<CmsUUID>();
            for (CmsResource res : publishList.getFileList()) {
                if (res.getState().isDeleted()) {
                    deletedResourceIds.add(res.getResourceId());
                } else {
                    changedResourceIds.add(res.getResourceId());
                }
            }
            Set<CmsUUID> changedAndDeletedResourceIds = Sets.intersection(deletedResourceIds, changedResourceIds);
            dbc.setAttribute(CmsDriverManager.KEY_CHANGED_AND_DELETED, changedAndDeletedResourceIds);

            Iterator<CmsResource> itFiles = publishList.getFileList().iterator();
            while (itFiles.hasNext()) {
                CmsResource currentResource = itFiles.next();
                try {
                    // bounce the current publish task through all project drivers
                    projectDriver.publishFile(
                        dbc,
                        report,
                        ++publishedFileCount,
                        filesSize,
                        onlineProject,
                        currentResource,
                        publishedContentIds,
                        publishList.getPublishHistoryId(),
                        publishTag);

                    CmsResourceState state = currentResource.getState();
                    if (!state.isDeleted()) {
                        // reset the resource state to UNCHANGED and the last-modified-in-project-ID to 0
                        internalResetResourceState(dbc, currentResource);
                    }

                    // unlock it
                    m_driverManager.unlockResource(dbc, currentResource, true, true);
                    // log it
                    CmsLogEntryType type = state.isNew()
                    ? CmsLogEntryType.RESOURCE_PUBLISHED_NEW
                    : (state.isDeleted()
                    ? CmsLogEntryType.RESOURCE_PUBLISHED_DELETED
                    : CmsLogEntryType.RESOURCE_PUBLISHED_MODIFIED);
                    m_driverManager.log(
                        dbc,
                        new CmsLogEntry(
                            dbc,
                            currentResource.getStructureId(),
                            type,
                            new String[] {currentResource.getRootPath()}),
                        true);

                    publishedIds.add(currentResource.getStructureId());
                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(
                        report,
                        Messages.get().container(Messages.ERR_ERROR_PUBLISHING_FILE_1, currentResource.getRootPath()),
                        t);
                }
            }

            if (filesSize > 0) {
                report.println(Messages.get().container(Messages.RPT_PUBLISH_FILES_END_0), I_CmsReport.FORMAT_HEADLINE);
            }

            ////////////////////////////////////////////////////////////////////////////////////////

            // publish deleted folders
            List<CmsResource> deletedFolders = publishList.getDeletedFolderList();
            if (deletedFolders.isEmpty()) {
                return;
            }

            deletedFolderCount = 0;
            int deletedFoldersSize = deletedFolders.size();
            if (deletedFoldersSize > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_DELETE_FOLDERS_BEGIN_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }

            Iterator<CmsResource> itDeletedFolders = deletedFolders.iterator();
            while (itDeletedFolders.hasNext()) {
                CmsResource currentFolder = itDeletedFolders.next();

                try {
                    // bounce the current publish task through all project drivers
                    projectDriver.publishDeletedFolder(
                        dbc,
                        report,
                        ++deletedFolderCount,
                        deletedFoldersSize,
                        onlineProject,
                        new CmsFolder(currentFolder),
                        publishList.getPublishHistoryId(),
                        publishTag);

                    dbc.pop();
                    // delete old historical entries
                    m_driverManager.getHistoryDriver(dbc).deleteEntries(
                        dbc,
                        new CmsHistoryFile(currentFolder),
                        OpenCms.getSystemInfo().getHistoryVersionsAfterDeletion(),
                        -1);

                    publishedIds.add(currentFolder.getStructureId());
                    // unlock it
                    m_driverManager.unlockResource(dbc, currentFolder, true, true);
                    // log it
                    m_driverManager.log(
                        dbc,
                        new CmsLogEntry(
                            dbc,
                            currentFolder.getStructureId(),
                            CmsLogEntryType.RESOURCE_PUBLISHED_DELETED,
                            new String[] {currentFolder.getRootPath()}),
                        true);

                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(
                        report,
                        Messages.get().container(
                            Messages.ERR_ERROR_PUBLISHING_DELETED_FOLDER_1,
                            currentFolder.getRootPath()),
                        t);
                }
            }

            if (deletedFoldersSize > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_DELETE_FOLDERS_END_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }
        } catch (OutOfMemoryError o) {
            // clear all caches to reclaim memory
            OpenCms.fireCmsEvent(
                new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.<String, Object> emptyMap()));

            CmsMessageContainer message = Messages.get().container(Messages.ERR_OUT_OF_MEMORY_0);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key(), o);
            }
            throw new CmsDataAccessException(message, o);
        } finally {
            // reset vfs driver internal info after publishing
            m_driverManager.getVfsDriver(dbc).publishVersions(dbc, null, false);
            Object[] msgArgs = new Object[] {
                String.valueOf(publishedFileCount),
                String.valueOf(publishedFolderCount),
                String.valueOf(deletedFolderCount),
                report.formatRuntime()};

            CmsMessageContainer message = Messages.get().container(Messages.RPT_PUBLISH_STAT_4, msgArgs);
            if (LOG.isInfoEnabled()) {
                LOG.info(message.key());
            }
            report.println(message);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readLocks(org.opencms.db.CmsDbContext)
     */
    public List<CmsLock> readLocks(CmsDbContext dbc) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        List<CmsTempResourceLock> tmpLocks = new ArrayList<CmsTempResourceLock>(256);
        List<CmsLock> locks = new ArrayList<CmsLock>(256);
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCE_LOCKS_READALL");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String resourcePath = rs.getString(m_sqlManager.readQuery("C_RESOURCE_LOCKS_RESOURCE_PATH"));
                CmsUUID userId = new CmsUUID(rs.getString(m_sqlManager.readQuery("C_RESOURCE_LOCKS_USER_ID")));
                CmsUUID projectId = new CmsUUID(rs.getString(m_sqlManager.readQuery("C_RESOURCE_LOCKS_PROJECT_ID")));
                int lockType = rs.getInt(m_sqlManager.readQuery("C_RESOURCE_LOCKS_LOCK_TYPE"));
                CmsTempResourceLock tmpLock = new CmsTempResourceLock(resourcePath, userId, projectId, lockType);
                tmpLocks.add(tmpLock);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        for (CmsTempResourceLock tmpLock : tmpLocks) {
            CmsProject project;
            try {
                project = readProject(dbc, tmpLock.getProjectId());
            } catch (CmsDataAccessException dae) {
                // the project does not longer exist, ignore this lock (should usually not happen)
                project = null;
            }
            if (project != null) {
                CmsLock lock = new CmsLock(
                    tmpLock.getResourcePath(),
                    tmpLock.getUserId(),
                    project,
                    CmsLockType.valueOf(tmpLock.getLockType()));
                locks.add(lock);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_DBG_READ_LOCKS_1, Integer.valueOf(locks.size())));
        }
        return locks;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readLog(org.opencms.db.CmsDbContext, org.opencms.db.log.CmsLogFilter)
     */
    public List<CmsLogEntry> readLog(CmsDbContext dbc, CmsLogFilter filter) throws CmsDataAccessException {

        List<CmsLogEntry> entries = new ArrayList<CmsLogEntry>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            // compose statement
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery("C_LOG_READ_ENTRIES"));
            CmsPair<String, List<I_CmsPreparedStatementParameter>> conditionsAndParameters = prepareLogConditions(
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
                entries.add(internalReadLogEntry(res));
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
     * @see org.opencms.db.I_CmsProjectDriver#readProject(org.opencms.db.CmsDbContext, CmsUUID)
     */
    public CmsProject readProject(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_1");

            stmt.setString(1, id.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                project = internalCreateProject(res);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_NO_PROJECT_WITH_ID_1, String.valueOf(id)));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return project;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProject(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public CmsProject readProject(CmsDbContext dbc, String projectFqn) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYNAME_2");

            stmt.setString(1, CmsOrganizationalUnit.getSimpleName(projectFqn));
            stmt.setString(2, CmsOrganizationalUnit.SEPARATOR + CmsOrganizationalUnit.getParentFqn(projectFqn));
            res = stmt.executeQuery();

            if (res.next()) {
                project = internalCreateProject(res);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_NO_PROJECT_WITH_NAME_1, projectFqn));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return project;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectResource(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String)
     */
    public String readProjectResource(CmsDbContext dbc, CmsUUID projectId, String resourcePath)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        String resName = null;

        try {
            conn = getSqlManager().getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ_2");

            // select resource from the database
            stmt.setString(1, projectId.toString());
            stmt.setString(2, resourcePath);
            res = stmt.executeQuery();

            if (res.next()) {
                resName = res.getString("RESOURCE_PATH");
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException(
                    Messages.get().container(Messages.ERR_NO_PROJECTRESOURCE_1, resourcePath));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return resName;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public List<String> readProjectResources(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        List<String> result = new ArrayList<String>();

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ_BY_ID_1");
            stmt.setString(1, project.getUuid().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                result.add(res.getString("RESOURCE_PATH"));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjects(org.opencms.db.CmsDbContext, String)
     */
    public List<CmsProject> readProjects(CmsDbContext dbc, String ouFqn) throws CmsDataAccessException {

        if ((dbc.getRequestContext() != null)
            && (dbc.getRequestContext().getAttribute(DBC_ATTR_READ_PROJECT_FOR_RESOURCE) != null)) {
            dbc.getRequestContext().removeAttribute(DBC_ATTR_READ_PROJECT_FOR_RESOURCE);
            // TODO: this should get its own method in the interface
            return readProjectsForResource(dbc, ouFqn);
        }
        List<CmsProject> projects = new ArrayList<CmsProject>();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYOU_1");

            stmt.setString(1, CmsOrganizationalUnit.SEPARATOR + ouFqn + "%");
            res = stmt.executeQuery();

            while (res.next()) {
                projects.add(internalCreateProject(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectsForGroup(org.opencms.db.CmsDbContext, org.opencms.file.CmsGroup)
     */
    public List<CmsProject> readProjectsForGroup(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException {

        List<CmsProject> projects = new ArrayList<CmsProject>();
        ResultSet res = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYGROUP_2");

            stmt.setString(1, group.getId().toString());
            stmt.setString(2, group.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                projects.add(internalCreateProject(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectsForManagerGroup(org.opencms.db.CmsDbContext, org.opencms.file.CmsGroup)
     */
    public List<CmsProject> readProjectsForManagerGroup(CmsDbContext dbc, CmsGroup group)
    throws CmsDataAccessException {

        List<CmsProject> projects = new ArrayList<CmsProject>();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYMANAGER_1");

            stmt.setString(1, group.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                projects.add(internalCreateProject(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return (projects);
    }

    /**
     * Returns the projects of a given resource.<p>
     *
     * @param dbc the database context
     * @param rootPath the resource root path
     *
     * @return the projects of the resource, as a list of projects
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    public List<CmsProject> readProjectsForResource(CmsDbContext dbc, String rootPath) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        List<CmsProject> projects = new ArrayList<CmsProject>();
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYRESOURCE_1");

            stmt.setString(1, rootPath + "%");
            res = stmt.executeQuery();

            if (res.next()) {
                projects.add(internalCreateProject(res));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return projects;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectsForUser(org.opencms.db.CmsDbContext, org.opencms.file.CmsUser)
     */
    public List<CmsProject> readProjectsForUser(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException {

        List<CmsProject> projects = new ArrayList<CmsProject>();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYUSER_1");

            stmt.setString(1, user.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                projects.add(internalCreateProject(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readPublishedResources(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public List<CmsPublishedResource> readPublishedResources(CmsDbContext dbc, CmsUUID publishHistoryId)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        List<CmsPublishedResource> publishedResources = new ArrayList<CmsPublishedResource>();

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SELECT_PUBLISHED_RESOURCES");
            stmt.setString(1, publishHistoryId.toString());
            res = stmt.executeQuery();

            while (res.next()) {
                CmsUUID structureId = new CmsUUID(res.getString("STRUCTURE_ID"));
                CmsUUID resourceId = new CmsUUID(res.getString("RESOURCE_ID"));
                String rootPath = res.getString("RESOURCE_PATH");
                int resourceState = res.getInt("RESOURCE_STATE");
                int resourceType = res.getInt("RESOURCE_TYPE");
                int siblingCount = res.getInt("SIBLING_COUNT");
                int publishTag = res.getInt("PUBLISH_TAG");

                // compose the resource state
                CmsResourceState state;
                if (resourceState == CmsPublishedResource.STATE_MOVED_SOURCE.getState()) {
                    state = CmsPublishedResource.STATE_MOVED_SOURCE;
                } else if (resourceState == CmsPublishedResource.STATE_MOVED_DESTINATION.getState()) {
                    state = CmsPublishedResource.STATE_MOVED_DESTINATION;
                } else {
                    state = CmsResourceState.valueOf(resourceState);
                }

                publishedResources.add(
                    new CmsPublishedResource(
                        structureId,
                        resourceId,
                        publishTag,
                        rootPath,
                        resourceType,
                        structureId.isNullUUID() ? false : CmsFolder.isFolderType(resourceType),
                        state,
                        siblingCount));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return publishedResources;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readPublishJob(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public CmsPublishJobInfoBean readPublishJob(CmsDbContext dbc, CmsUUID publishHistoryId)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        CmsPublishJobInfoBean result = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_READ_JOB");
            stmt.setString(1, publishHistoryId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                result = createPublishJobInfoBean(res);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDbEntryNotFoundException(
                    Messages.get().container(Messages.ERR_READ_PUBLISH_JOB_1, publishHistoryId.toString()));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readPublishJobs(org.opencms.db.CmsDbContext, long, long)
     */
    public List<CmsPublishJobInfoBean> readPublishJobs(CmsDbContext dbc, long startTime, long endTime)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        List<CmsPublishJobInfoBean> result = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_READ_JOBS_IN_TIMERANGE");
            stmt.setLong(1, startTime);
            stmt.setLong(2, endTime);
            res = stmt.executeQuery();

            result = new ArrayList<CmsPublishJobInfoBean>();
            while (res.next()) {
                result.add(createPublishJobInfoBean(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readPublishList(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public CmsPublishList readPublishList(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsPublishList publishList = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_READ_PUBLISHLIST");
            stmt.setString(1, publishHistoryId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                byte[] bytes = m_sqlManager.getBytes(res, "PUBLISH_LIST");
                publishList = internalDeserializePublishList(bytes);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDataAccessException(
                    Messages.get().container(Messages.ERR_READ_PUBLISH_JOB_1, publishHistoryId.toString()));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } catch (Exception e) {
            throw new CmsDataAccessException(
                Messages.get().container(Messages.ERR_PUBLISHLIST_DESERIALIZATION_FAILED_1, publishHistoryId),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return publishList;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readPublishReportContents(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public byte[] readPublishReportContents(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        byte[] bytes = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_READ_REPORT");
            stmt.setString(1, publishHistoryId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                // query to read Array of bytes for the given attribute
                bytes = m_sqlManager.getBytes(res, "PUBLISH_REPORT");
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDataAccessException(
                    Messages.get().container(Messages.ERR_READ_PUBLISH_JOB_1, publishHistoryId.toString()));
            }
        } catch (SQLException e) {
            LOG.error(CmsDbSqlException.getErrorQuery(stmt), e);
            bytes = Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)).key().getBytes();
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return bytes;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readStaticExportPublishedResourceParameters(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public String readStaticExportPublishedResourceParameters(CmsDbContext dbc, String rfsName)
    throws CmsDataAccessException {

        String returnValue = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_READ_PUBLISHED_LINK_PARAMETERS");
            stmt.setString(1, rfsName);
            res = stmt.executeQuery();
            // add all resourcenames to the list of return values
            if (res.next()) {
                returnValue = res.getString(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return returnValue;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readStaticExportResources(org.opencms.db.CmsDbContext, int, long)
     */
    public List<String> readStaticExportResources(CmsDbContext dbc, int parameterResources, long timestamp)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        List<String> returnValue = new ArrayList<String>();

        if (parameterResources == CmsStaticExportManager.EXPORT_LINK_WITHOUT_PARAMETER) {
            timestamp = 0;
        }
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_READ_ALL_PUBLISHED_LINKS");
            stmt.setInt(1, parameterResources);
            stmt.setLong(2, timestamp);
            res = stmt.executeQuery();
            // add all resourcenames to the list of return values
            while (res.next()) {
                returnValue.add(res.getString(1));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return returnValue;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#setDriverManager(org.opencms.db.CmsDriverManager)
     */
    public void setDriverManager(CmsDriverManager driverManager) {

        m_driverManager = driverManager;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#setSqlManager(org.opencms.db.CmsSqlManager)
     */
    public void setSqlManager(org.opencms.db.CmsSqlManager manager) {

        m_sqlManager = (CmsSqlManager)manager;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#unmarkProjectResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public void unmarkProjectResources(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        // finally remove the project id form all resources

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UNMARK");
            // create the statement
            stmt.setString(1, project.getUuid().toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#writeLocks(org.opencms.db.CmsDbContext, java.util.List)
     */
    public void writeLocks(CmsDbContext dbc, List<CmsLock> locks) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCE_LOCKS_DELETEALL");
            int deleted = stmt.executeUpdate();
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DBG_CLEAR_LOCKS_1, Integer.valueOf(deleted)));
            }
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCE_LOCK_WRITE");
            if (LOG.isDebugEnabled()) {
                LOG.debug("SQL :" + m_sqlManager.readQuery("C_RESOURCE_LOCK_WRITE"));
            }
            Iterator<CmsLock> i = locks.iterator();
            int count = 0;
            while (i.hasNext()) {
                CmsLock lock = i.next();
                // only persist locks that should be written to the DB
                CmsLock sysLock = lock.getSystemLock();
                if (sysLock.isPersistent()) {
                    // persist system lock
                    stmt.setString(1, sysLock.getResourceName());
                    stmt.setString(2, sysLock.getUserId().toString());
                    stmt.setString(3, sysLock.getProjectId().toString());
                    stmt.setInt(4, sysLock.getType().hashCode());
                    stmt.executeUpdate();
                    count++;
                }
                CmsLock editLock = lock.getEditionLock();
                if (editLock.isPersistent()) {
                    // persist edition lock
                    stmt.setString(1, editLock.getResourceName());
                    stmt.setString(2, editLock.getUserId().toString());
                    stmt.setString(3, editLock.getProjectId().toString());
                    stmt.setInt(4, editLock.getType().hashCode());
                    stmt.executeUpdate();
                    count++;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DBG_WRITE_LOCKS_1, Integer.valueOf(count)));
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
     * @see org.opencms.db.I_CmsProjectDriver#writeProject(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public void writeProject(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(project.getDescription())) {
            project.setDescription(" ");
        }
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // get a JDBC connection from the OpenCms standard pools
            conn = m_sqlManager.getConnection(dbc);

            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_WRITE_6");
            stmt.setString(1, project.getDescription());
            stmt.setString(2, project.getGroupId().toString());
            stmt.setString(3, project.getManagerGroupId().toString());
            stmt.setInt(4, project.getFlags());
            stmt.setInt(5, project.getType().getMode());
            stmt.setString(6, project.getUuid().toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#writePublishHistory(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, org.opencms.db.CmsPublishedResource)
     */
    public void writePublishHistory(CmsDbContext dbc, CmsUUID publishId, CmsPublishedResource resource)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_WRITE_PUBLISH_HISTORY");
            stmt.setInt(1, resource.getPublishTag());
            stmt.setString(2, resource.getStructureId().toString());
            stmt.setString(3, resource.getResourceId().toString());
            stmt.setString(4, resource.getRootPath());
            stmt.setInt(5, resource.getMovedState().getState());
            stmt.setInt(6, resource.getType());
            stmt.setString(7, publishId.toString());
            stmt.setInt(8, resource.getSiblingCount());
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
     * @see org.opencms.db.I_CmsProjectDriver#writePublishJob(org.opencms.db.CmsDbContext, org.opencms.publish.CmsPublishJobInfoBean)
     */
    public void writePublishJob(CmsDbContext dbc, CmsPublishJobInfoBean publishJob) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_WRITE");
            stmt.setString(1, publishJob.getProjectId().toString());
            stmt.setString(2, publishJob.getProjectName());
            stmt.setString(3, publishJob.getUserId().toString());
            stmt.setString(4, publishJob.getLocale().toString());
            stmt.setInt(5, publishJob.getFlags());
            stmt.setInt(6, publishJob.getSize());
            stmt.setLong(7, publishJob.getEnqueueTime());
            stmt.setLong(8, publishJob.getStartTime());
            stmt.setLong(9, publishJob.getFinishTime());
            stmt.setString(10, publishJob.getPublishHistoryId().toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#writePublishReport(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, byte[])
     */
    public void writePublishReport(CmsDbContext dbc, CmsUUID publishId, byte[] content) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_WRITE_REPORT");

            if (content.length < 2000) {
                stmt.setBytes(1, content);
            } else {
                stmt.setBinaryStream(1, new ByteArrayInputStream(content), content.length);
            }

            stmt.setString(2, publishId.toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#writeStaticExportPublishedResource(org.opencms.db.CmsDbContext, java.lang.String, int, java.lang.String, long)
     */
    public void writeStaticExportPublishedResource(
        CmsDbContext dbc,
        String resourceName,
        int linkType,
        String linkParameter,
        long timestamp)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        int returnValue = 0;
        // first check if a record with this resource name does already exist
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_READ_PUBLISHED_RESOURCES");
            stmt.setString(1, resourceName);
            res = stmt.executeQuery();
            if (res.next()) {
                returnValue = res.getInt(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        // there was no entry found, so add it to the database
        if (returnValue == 0) {
            try {
                conn = m_sqlManager.getConnection(dbc);
                stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_WRITE_PUBLISHED_LINKS");
                stmt.setString(1, new CmsUUID().toString());
                stmt.setString(2, resourceName);
                stmt.setInt(3, linkType);
                stmt.setString(4, linkParameter);
                stmt.setLong(5, timestamp);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, stmt), e);
            } finally {
                m_sqlManager.closeAll(dbc, conn, stmt, null);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writeUserPublishListEntries(org.opencms.db.CmsDbContext, java.util.List)
     */
    public void writeUserPublishListEntries(CmsDbContext dbc, List<CmsUserPublishListEntry> publishListAdditions)
    throws CmsDbSqlException {

        if (publishListAdditions.isEmpty()) {
            return;
        }

        // first remove all entries with the same keys
        deleteUserPublishListEntries(dbc, publishListAdditions);

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            String sql = m_sqlManager.readQuery("C_USER_PUBLISH_LIST_INSERT_3");
            stmt = m_sqlManager.getPreparedStatementForSql(conn, sql);
            for (CmsUserPublishListEntry entry : publishListAdditions) {
                stmt.setString(1, entry.getUserId().toString());
                stmt.setString(2, entry.getStructureId().toString());
                stmt.setLong(3, entry.getDateChanged());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

    }

    /**
     * Creates a <code>CmsPublishJobInfoBean</code> from a result set.<p>
     *
     * @param res the result set
     * @return an initialized <code>CmsPublishJobInfoBean</code>
     * @throws SQLException if something goes wrong
     */
    protected CmsPublishJobInfoBean createPublishJobInfoBean(ResultSet res) throws SQLException {

        return new CmsPublishJobInfoBean(
            new CmsUUID(res.getString("HISTORY_ID")),
            new CmsUUID(res.getString("PROJECT_ID")),
            res.getString("PROJECT_NAME"),
            new CmsUUID(res.getString("USER_ID")),
            res.getString("PUBLISH_LOCALE"),
            res.getInt("PUBLISH_FLAGS"),
            res.getInt("RESOURCE_COUNT"),
            res.getLong("ENQUEUE_TIME"),
            res.getLong("START_TIME"),
            res.getLong("FINISH_TIME"));
    }

    /**
     * Checks if the given resource (by id) is available in the online project,
     * if there exists a resource with a different path (a moved file), then the
     * online entry is moved to the right (new) location before publishing.<p>
     *
     * @param dbc the db context
     * @param onlineProject the online project
     * @param offlineResource the offline resource to check
     * @param publishHistoryId the publish history id
     * @param publishTag the publish tag
     *
     * @return <code>true</code> if the resource has actually been moved
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    protected CmsResourceState fixMovedResource(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        CmsUUID publishHistoryId,
        int publishTag)
    throws CmsDataAccessException {

        CmsResource onlineResource;
        // check if the resource has been moved since last publishing
        try {
            onlineResource = m_driverManager.getVfsDriver(
                dbc).readResource(dbc, onlineProject.getUuid(), offlineResource.getStructureId(), true);
            if (onlineResource.getRootPath().equals(offlineResource.getRootPath())) {
                // resource changed, not moved
                return offlineResource.getState();
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // ok, resource new, not moved
            return offlineResource.getState();
        }

        // move the online resource to the new position
        m_driverManager.getVfsDriver(
            dbc).moveResource(dbc, onlineProject.getUuid(), onlineResource, offlineResource.getRootPath());

        try {
            // write the resource to the publish history
            m_driverManager.getProjectDriver(dbc).writePublishHistory(
                dbc,
                publishHistoryId,
                new CmsPublishedResource(onlineResource, publishTag, CmsPublishedResource.STATE_MOVED_SOURCE));
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.LOG_WRITING_PUBLISHING_HISTORY_1,
                        onlineResource.getRootPath()),
                    e);
            }
            throw e;
        }
        return offlineResource.getState().isDeleted()
        ? CmsResource.STATE_DELETED
        : CmsPublishedResource.STATE_MOVED_DESTINATION;
    }

    /**
     * Returns a SQL parameter string for the given data.<p>
     *
     * @param data the data
     *
     * @return the SQL parameter
     */
    protected String getParameterString(Collection<?> data) {

        StringBuffer conditions = new StringBuffer();
        conditions.append(BEGIN_CONDITION);
        Iterator<?> it = data.iterator();
        while (it.hasNext()) {
            it.next();
            conditions.append("?");
            if (it.hasNext()) {
                conditions.append(", ");
            }
        }
        conditions.append(END_CONDITION);
        return conditions.toString();
    }

    /**
     * Implementation of reading the user publish list which uses the log table.<p>
     *
     * This is the old implementation of the user publish list and can get pretty slow.<p>
     *
     * @param dbc the current database context
     * @param userId the id of the user for which we want the user publish list
     *
     * @return the publish list for the given user
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    protected List<CmsResource> getUsersPubListFromLog(CmsDbContext dbc, CmsUUID userId) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        List<CmsResource> result = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject().getUuid(), "C_LOG_READ_PUBLISH_LIST_2");
            stmt.setString(1, userId.toString());
            stmt.setString(2, userId.toString());
            res = stmt.executeQuery();

            result = new ArrayList<CmsResource>();
            while (res.next()) {
                CmsResource resource = m_driverManager.getVfsDriver(dbc).createResource(
                    res,
                    dbc.currentProject().getUuid());
                long date = res.getLong(m_sqlManager.readQuery("C_LOG_DATE"));
                resource.setDateLastModified(date);
                result.add(resource);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(
                Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return result;
    }

    /**
     * Creates a new project from the current row of the given result set.<p>
     *
     * @param res the result set
     *
     * @return the new project
     *
     * @throws SQLException is something goes wrong
     */
    protected CmsProject internalCreateProject(ResultSet res) throws SQLException {

        String ou = CmsOrganizationalUnit.removeLeadingSeparator(
            res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_OU_0")));
        return new CmsProject(
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_ID_0"))),
            ou + res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_NAME_0")),
            res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_DESCRIPTION_0")),
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_USER_ID_0"))),
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_GROUP_ID_0"))),
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_MANAGERGROUP_ID_0"))),
            res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_FLAGS_0")),
            res.getLong(m_sqlManager.readQuery("C_PROJECTS_DATE_CREATED_0")),
            CmsProject.CmsProjectType.valueOf(res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_TYPE_0"))));
    }

    /**
     * Builds a publish list from serialized data.<p>
     *
     * @param bytes the byte array containing the serailized data for the publish list
     * @return the initialized publish list
     *
     * @throws IOException if deserialization fails
     * @throws ClassNotFoundException if deserialization fails
     */
    protected CmsPublishList internalDeserializePublishList(byte[] bytes) throws IOException, ClassNotFoundException {

        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        ObjectInputStream oin = new ObjectInputStream(bin);
        return (CmsPublishList)oin.readObject();
    }

    /**
     * Creates a new {@link CmsLogEntry} object from the given result set entry.<p>
     *
     * @param res the result set
     *
     * @return the new {@link CmsLogEntry} object
     *
     * @throws SQLException if something goes wrong
     */
    protected CmsLogEntry internalReadLogEntry(ResultSet res) throws SQLException {

        CmsUUID userId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_LOG_USER_ID")));
        long date = res.getLong(m_sqlManager.readQuery("C_LOG_DATE"));
        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_LOG_STRUCTURE_ID")));
        CmsLogEntryType type = CmsLogEntryType.valueOf(res.getInt(m_sqlManager.readQuery("C_LOG_TYPE")));
        String[] data = CmsStringUtil.splitAsArray(res.getString(m_sqlManager.readQuery("C_LOG_DATA")), '|');
        return new CmsLogEntry(userId, date, structureId, type, data);
    }

    /**
     * Resets the state to UNCHANGED for a specified resource.<p>
     *
     * @param dbc the current database context
     * @param resource the Cms resource
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalResetResourceState(CmsDbContext dbc, CmsResource resource) throws CmsDataAccessException {

        try {
            // reset the resource state
            resource.setState(CmsResource.STATE_UNCHANGED);
            m_driverManager.getVfsDriver(
                dbc).writeResourceState(dbc, dbc.currentProject(), resource, CmsDriverManager.UPDATE_ALL, true);
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.LOG_ERROR_RESETTING_RESOURCE_STATE_1,
                        resource.getRootPath()),
                    e);
            }
            throw e;
        }
    }

    /**
     * Serialize publish list to write it as byte array to the database.<p>
     *
     * @param publishList the publish list
     * @return byte array containing the publish list data
     * @throws IOException if something goes wrong
     */
    protected byte[] internalSerializePublishList(CmsPublishList publishList) throws IOException {

        // serialize the publish list
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(publishList);
        oout.close();
        return bout.toByteArray();
    }

    /**
     * Writes the needed history entries.<p>
     *
     * @param dbc the current database context
     * @param resource the offline resource
     * @param state the state to store in the publish history entry
     * @param properties the offline properties
     * @param publishHistoryId the current publish process id
     * @param publishTag the current publish process tag
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalWriteHistory(
        CmsDbContext dbc,
        CmsResource resource,
        CmsResourceState state,
        List<CmsProperty> properties,
        CmsUUID publishHistoryId,
        int publishTag)
    throws CmsDataAccessException {

        try {
            if (OpenCms.getSystemInfo().isHistoryEnabled()) {
                // write the resource to the historical archive
                if (properties == null) {
                    properties = m_driverManager.getVfsDriver(dbc).readPropertyObjects(
                        dbc,
                        dbc.currentProject(),
                        resource);
                }
                m_driverManager.getHistoryDriver(dbc).writeResource(dbc, resource, properties, publishTag);
            }
            // write the resource to the publish history
            m_driverManager.getProjectDriver(dbc).writePublishHistory(
                dbc,
                publishHistoryId,
                new CmsPublishedResource(resource, publishTag, state));
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_WRITING_PUBLISHING_HISTORY_1, resource.getRootPath()),
                    e);
            }
            throw e;
        }
    }

    /**
     * Build the whole WHERE SQL statement part for the given log entry filter.<p>
     *
     * @param filter the filter
     *
     * @return a pair containing both the SQL and the parameters for it
     */
    protected CmsPair<String, List<I_CmsPreparedStatementParameter>> prepareLogConditions(CmsLogFilter filter) {

        List<I_CmsPreparedStatementParameter> params = new ArrayList<I_CmsPreparedStatementParameter>();
        StringBuffer conditions = new StringBuffer();

        // user id filter
        if (filter.getUserId() != null) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery("C_LOG_FILTER_USER_ID"));
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
            conditions.append(m_sqlManager.readQuery("C_LOG_FILTER_RESOURCE_ID"));
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
            conditions.append(m_sqlManager.readQuery("C_LOG_FILTER_DATE_FROM"));
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
            conditions.append(m_sqlManager.readQuery("C_LOG_FILTER_DATE_TO"));
            params.add(new CmsPreparedStatementLongParameter(filter.getDateTo()));
            conditions.append(END_CONDITION);
        }

        // include type filter
        Set<CmsLogEntryType> includeTypes = filter.getIncludeTypes();
        if (!includeTypes.isEmpty()) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery("C_LOG_FILTER_INCLUDE_TYPE"));
            conditions.append(BEGIN_CONDITION);
            Iterator<CmsLogEntryType> it = includeTypes.iterator();
            while (it.hasNext()) {
                CmsLogEntryType type = it.next();
                conditions.append("?");
                params.add(new CmsPreparedStatementIntParameter(type.getId()));
                if (it.hasNext()) {
                    conditions.append(", ");
                }
            }
            conditions.append(END_CONDITION);
            conditions.append(END_CONDITION);
        }

        // exclude type filter
        Set<CmsLogEntryType> excludeTypes = filter.getExcludeTypes();
        if (!excludeTypes.isEmpty()) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery("C_LOG_FILTER_EXCLUDE_TYPE"));
            conditions.append(BEGIN_CONDITION);
            Iterator<CmsLogEntryType> it = excludeTypes.iterator();
            while (it.hasNext()) {
                CmsLogEntryType type = it.next();
                conditions.append("?");
                params.add(new CmsPreparedStatementIntParameter(type.getId()));
                if (it.hasNext()) {
                    conditions.append(", ");
                }
            }
            conditions.append(END_CONDITION);
            conditions.append(END_CONDITION);
        }
        return CmsPair.create(conditions.toString(), params);
    }

    /**
     * Publishes a changed file.<p>
     *
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param offlineResource the resource to publish
     * @param publishedResourceIds contains the UUIDs of already published content records
     * @param publishHistoryId the publish history id
     * @param publishTag the publish tag
     *
     * @throws CmsDataAccessException is something goes wrong
     */
    protected void publishChangedFile(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set<CmsUUID> publishedResourceIds,
        CmsUUID publishHistoryId,
        int publishTag)
    throws CmsDataAccessException {

        CmsResource onlineResource = null;
        boolean needToUpdateContent = true;
        boolean existsOnline = m_driverManager.getVfsDriver(dbc).validateStructureIdExists(
            dbc,
            CmsProject.ONLINE_PROJECT_ID,
            offlineResource.getStructureId());
        CmsResourceState resourceState = existsOnline
        ? fixMovedResource(dbc, onlineProject, offlineResource, publishHistoryId, publishTag)
        : offlineResource.getState();
        try {
            // reset the labeled link flag before writing the online file
            int flags = offlineResource.getFlags();
            flags &= ~CmsResource.FLAG_LABELED;
            offlineResource.setFlags(flags);

            if (existsOnline) {
                // read the file header online
                onlineResource = m_driverManager.getVfsDriver(
                    dbc).readResource(dbc, onlineProject.getUuid(), offlineResource.getStructureId(), false);
                needToUpdateContent = (onlineResource.getDateContent() < offlineResource.getDateContent());
                // delete the properties online
                m_driverManager.getVfsDriver(dbc).deletePropertyObjects(
                    dbc,
                    onlineProject.getUuid(),
                    onlineResource,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

                // if the offline file has a resource ID different from the online file
                // (probably because a deleted file was replaced by a new file with the
                // same name), the properties mapped to the "old" resource ID have to be
                // deleted also offline. if this is the case, the online and offline structure
                // ID's do match, but the resource ID's are different. structure IDs are reused
                // to prevent orphan structure records in the online project.

                // Addendum (2023): It shouldn't be possible for resources to be in this state anymore,
                // since creating new resources over deleted ones isn't really possible anymore,
                // but apparently it can still happen, though I can't reproduce it.
                if (!onlineResource.getResourceId().equals(offlineResource.getResourceId())) {
                    List<CmsProperty> offlineProperties = m_driverManager.getVfsDriver(dbc).readPropertyObjects(
                        dbc,
                        dbc.currentProject(),
                        onlineResource);
                    if (offlineProperties.size() > 0) {
                        List<CmsProperty> newProperties = new ArrayList<>();
                        for (int i = 0; i < offlineProperties.size(); i++) {
                            CmsProperty oldProperty = offlineProperties.get(i);
                            // property may be frozen (non-modifiable), so create a new one
                            CmsProperty newProperty = new CmsProperty(
                                oldProperty.getName(),
                                null,
                                CmsProperty.DELETE_VALUE);
                            newProperties.add(newProperty);
                        }
                        m_driverManager.getVfsDriver(
                            dbc).writePropertyObjects(dbc, dbc.currentProject(), onlineResource, newProperties);
                    }
                }
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_DELETING_PROPERTIES_1, offlineResource.toString()),
                    e);
            }
            throw e;
        }

        CmsFile newFile;
        try {
            // publish the file content
            newFile = m_driverManager.getProjectDriver(dbc).publishFileContent(
                dbc,
                dbc.currentProject(),
                onlineProject,
                offlineResource,
                publishedResourceIds,
                needToUpdateContent,
                publishTag);

        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_PUBLISHING_RESOURCE_1, offlineResource.getRootPath()),
                    e);
            }
            throw e;
        }

        List<CmsProperty> offlineProperties;
        try {
            // write the properties online
            offlineProperties = m_driverManager.getVfsDriver(dbc).readPropertyObjects(
                dbc,
                dbc.currentProject(),
                offlineResource);
            CmsProperty.setAutoCreatePropertyDefinitions(offlineProperties, true);
            m_driverManager.getVfsDriver(dbc).writePropertyObjects(dbc, onlineProject, newFile, offlineProperties);
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_PUBLISHING_PROPERTIES_1, newFile.getRootPath()),
                    e);
            }
            throw e;
        }

        try {
            // write the ACL online
            m_driverManager.getUserDriver(dbc).publishAccessControlEntries(
                dbc,
                dbc.currentProject(),
                onlineProject,
                newFile.getResourceId(),
                offlineResource.getResourceId());
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_PUBLISHING_ACL_1, newFile.getRootPath()), e);
            }
            throw e;
        }

        CmsFile offlineFile = new CmsFile(offlineResource);
        offlineFile.setContents(newFile.getContents());
        internalWriteHistory(dbc, offlineFile, resourceState, offlineProperties, publishHistoryId, publishTag);

        m_driverManager.getVfsDriver(dbc).updateRelations(dbc, onlineProject, offlineResource);
    }

    /**
     * Publishes a deleted file.<p>
     *
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param offlineResource the resource to publish
     * @param publishHistoryId the publish history id
     * @param publishTag the publish tag
     *
     * @throws CmsDataAccessException is something goes wrong
     */
    protected void publishDeletedFile(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        CmsUUID publishHistoryId,
        int publishTag)
    throws CmsDataAccessException {

        CmsResourceState resourceState = fixMovedResource(
            dbc,
            onlineProject,
            offlineResource,
            publishHistoryId,
            publishTag);

        boolean existsOnline = m_driverManager.getVfsDriver(dbc).validateStructureIdExists(
            dbc,
            CmsProject.ONLINE_PROJECT_ID,
            offlineResource.getStructureId());
        CmsResource onlineResource = null;
        if (existsOnline) {
            try {
                // read the file header online
                onlineResource = m_driverManager.getVfsDriver(
                    dbc).readResource(dbc, onlineProject.getUuid(), offlineResource.getStructureId(), true);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_READING_RESOURCE_1, offlineResource.getRootPath()),
                        e);
                }
                throw e;
            }
        }
        if (offlineResource.isLabeled() && !m_driverManager.labelResource(dbc, offlineResource, null, 2)) {
            // update the resource flags to "unlabeled" of the siblings of the offline resource
            int flags = offlineResource.getFlags();
            flags &= ~CmsResource.FLAG_LABELED;
            offlineResource.setFlags(flags);
        }

        // write history before deleting
        CmsFile offlineFile = new CmsFile(offlineResource);
        offlineFile.setContents(
            m_driverManager.getVfsDriver(dbc).readContent(
                dbc,
                dbc.currentProject().getUuid(),
                offlineFile.getResourceId()));
        internalWriteHistory(dbc, offlineFile, resourceState, null, publishHistoryId, publishTag);

        int propertyDeleteOption = -1;
        try {
            // delete the properties online and offline
            if (offlineResource.getSiblingCount() > 1) {
                // there are other siblings- delete only structure property values and keep the resource property values
                propertyDeleteOption = CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES;
            } else {
                // there are no other siblings- delete both the structure and resource property values
                propertyDeleteOption = CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
            }

            if (existsOnline) {
                m_driverManager.getVfsDriver(
                    dbc).deletePropertyObjects(dbc, onlineProject.getUuid(), onlineResource, propertyDeleteOption);
            }
            m_driverManager.getVfsDriver(
                dbc).deletePropertyObjects(dbc, dbc.currentProject().getUuid(), offlineResource, propertyDeleteOption);

            // if the offline file has a resource ID different from the online file
            // (probably because a (deleted) file was replaced by a new file with the
            // same name), the properties with the "old" resource ID have to be
            // deleted also offline
            if (existsOnline
                && (onlineResource != null)
                && !onlineResource.getResourceId().equals(offlineResource.getResourceId())) {
                m_driverManager.getVfsDriver(dbc).deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getUuid(),
                    onlineResource,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_DELETING_PROPERTIES_1, offlineResource.getRootPath()),
                    e);
            }
            throw e;
        }

        try {
            // remove the file online and offline
            m_driverManager.getVfsDriver(dbc).removeFile(dbc, dbc.currentProject().getUuid(), offlineResource);
            if (existsOnline && (onlineResource != null)) {
                m_driverManager.getVfsDriver(dbc).removeFile(dbc, onlineProject.getUuid(), onlineResource);
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_REMOVING_RESOURCE_1, offlineResource.getRootPath()),
                    e);
            }
            throw e;
        }

        // delete the ACL online and offline
        try {
            if (existsOnline && (onlineResource != null) && (onlineResource.getSiblingCount() == 1)) {
                // only if no siblings left
                m_driverManager.getUserDriver(dbc).removeAccessControlEntries(
                    dbc,
                    onlineProject,
                    onlineResource.getResourceId());
            }
            if (offlineResource.getSiblingCount() == 1) {
                // only if no siblings left
                m_driverManager.getUserDriver(dbc).removeAccessControlEntries(
                    dbc,
                    dbc.currentProject(),
                    offlineResource.getResourceId());
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_REMOVING_ACL_1, offlineResource.toString()), e);
            }
            throw e;
        }

        try {
            // delete relations online and offline
            m_driverManager.getVfsDriver(
                dbc).deleteRelations(dbc, onlineProject.getUuid(), offlineResource, CmsRelationFilter.TARGETS);
            m_driverManager.getVfsDriver(
                dbc).deleteRelations(dbc, dbc.currentProject().getUuid(), offlineResource, CmsRelationFilter.TARGETS);
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_REMOVING_RELATIONS_1, offlineResource.toString()),
                    e);
            }
            throw e;
        }

        if (OpenCms.getSubscriptionManager().isEnabled()) {
            try {
                // delete visited information for resource from log
                CmsVisitEntryFilter filter = CmsVisitEntryFilter.ALL.filterResource(offlineResource.getStructureId());
                m_driverManager.getSubscriptionDriver().deleteVisits(
                    dbc,
                    OpenCms.getSubscriptionManager().getPoolName(),
                    filter);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_REMOVING_VISITEDLOG_1, offlineResource.toString()),
                        e);
                }
                throw e;
            }

            try {
                // mark the subscribed resource as deleted
                /* changed to subscription driver */
                //                m_driverManager.getUserDriver(dbc).setSubscribedResourceAsDeleted(
                //                    dbc,
                //                    OpenCms.getSubscriptionManager().getPoolName(),
                //                    offlineResource);
                m_driverManager.getSubscriptionDriver().setSubscribedResourceAsDeleted(
                    dbc,
                    OpenCms.getSubscriptionManager().getPoolName(),
                    offlineResource);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_REMOVING_SUBSCRIPTIONS_1,
                            offlineResource.toString()),
                        e);
                }
                throw e;
            }
        }
    }

    /**
     * Publishes a new file.<p>
     *
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param offlineResource the resource to publish
     * @param publishedContentIds contains the UUIDs of already published content records
     * @param publishHistoryId the publish history id
     * @param publishTag the publish tag
     *
     * @throws CmsDataAccessException is something goes wrong
     */
    protected void publishNewFile(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set<CmsUUID> publishedContentIds,
        CmsUUID publishHistoryId,
        int publishTag)
    throws CmsDataAccessException {

        CmsResourceState resourceState = fixMovedResource(
            dbc,
            onlineProject,
            offlineResource,
            publishHistoryId,
            publishTag);

        CmsFile newFile;
        try {
            // reset the labeled link flag before writing the online file
            int flags = offlineResource.getFlags();
            flags &= ~CmsResource.FLAG_LABELED;
            offlineResource.setFlags(flags);

            // publish the file content
            newFile = m_driverManager.getProjectDriver(dbc).publishFileContent(
                dbc,
                dbc.currentProject(),
                onlineProject,
                offlineResource,
                publishedContentIds,
                true,
                publishTag);

        } catch (CmsVfsResourceAlreadyExistsException e) {
            try {
                // remove the existing file and ensure that it's content is written
                // in any case by removing it's resource ID from the set of published resource IDs
                m_driverManager.getVfsDriver(dbc).removeFile(dbc, onlineProject.getUuid(), offlineResource);
                publishedContentIds.remove(offlineResource.getResourceId());
                newFile = m_driverManager.getProjectDriver(dbc).publishFileContent(
                    dbc,
                    dbc.currentProject(),
                    onlineProject,
                    offlineResource,
                    publishedContentIds,
                    true,
                    publishTag);

            } catch (CmsDataAccessException e1) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_PUBLISHING_RESOURCE_1,
                            offlineResource.getRootPath()),
                        e);
                }
                throw e1;
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_PUBLISHING_RESOURCE_1, offlineResource.getRootPath()),
                    e);
            }
            throw e;
        }

        List<CmsProperty> offlineProperties;
        try {
            // write the properties online
            offlineProperties = m_driverManager.getVfsDriver(dbc).readPropertyObjects(
                dbc,
                dbc.currentProject(),
                offlineResource);
            CmsProperty.setAutoCreatePropertyDefinitions(offlineProperties, true);
            m_driverManager.getVfsDriver(dbc).writePropertyObjects(dbc, onlineProject, newFile, offlineProperties);
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_PUBLISHING_PROPERTIES_1, newFile.getRootPath()),
                    e);
            }

            throw e;
        }

        try {
            // write the ACL online
            m_driverManager.getUserDriver(dbc).publishAccessControlEntries(
                dbc,
                dbc.currentProject(),
                onlineProject,
                offlineResource.getResourceId(),
                newFile.getResourceId());
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_PUBLISHING_ACL_1, newFile.getRootPath()), e);
            }
            throw e;
        }

        CmsFile offlineFile = new CmsFile(offlineResource);
        offlineFile.setContents(newFile.getContents());
        internalWriteHistory(dbc, offlineFile, resourceState, offlineProperties, publishHistoryId, publishTag);

        m_driverManager.getVfsDriver(dbc).updateRelations(dbc, onlineProject, offlineResource);
    }

}
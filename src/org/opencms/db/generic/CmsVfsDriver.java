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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPreparedStatementIntParameter;
import org.opencms.db.CmsPreparedStatementStringParameter;
import org.opencms.db.CmsResourceState;
import org.opencms.db.CmsVfsOnlineResourceAlreadyExistsException;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsPreparedStatementParameter;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.db.urlname.CmsUrlNameMappingEntry;
import org.opencms.db.urlname.CmsUrlNameMappingFilter;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.I_CmsResource;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Generic (ANSI-SQL) database server implementation of the VFS driver methods.<p>
 * 
 * @since 6.0.0 
 */
public class CmsVfsDriver implements I_CmsDriver, I_CmsVfsDriver {

    /** Contains the macro replacement value for the offline project. */
    protected static final String OFFLINE = "OFFLINE";

    /** Contains the macro replacement value for the online project. */
    protected static final String ONLINE = "ONLINE";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.generic.CmsVfsDriver.class);

    /** The driver manager. */
    protected CmsDriverManager m_driverManager;

    /** 
     * This field is temporarily used to compute the versions during publishing.<p>
     * 
     * @see #publishVersions(CmsDbContext, CmsResource, boolean) 
     */
    protected List m_resOp = new ArrayList();

    /** The sql manager. */
    protected CmsSqlManager m_sqlManager;

    /**
     * This method prepares the SQL conditions for mapping entries for a given URL name mapping filter.<p>
     * 
     * @param filter the filter from which the SQL conditions should be generated 
     * 
     * @return a pair consisting of an SQL string and a list of the prepared statement parameters for the SQL 
     */
    public static CmsPair<String, List<I_CmsPreparedStatementParameter>> prepareUrlNameMappingConditions(
        CmsUrlNameMappingFilter filter) {

        List<String> sqlConditions = new ArrayList<String>();
        List<I_CmsPreparedStatementParameter> parameters = new ArrayList<I_CmsPreparedStatementParameter>();
        if (filter.getName() != null) {
            sqlConditions.add("NAME = ?");
            parameters.add(new CmsPreparedStatementStringParameter(filter.getName()));
        }

        if (filter.getStructureId() != null) {
            sqlConditions.add("STRUCTURE_ID = ?");
            parameters.add(new CmsPreparedStatementStringParameter(filter.getStructureId().toString()));
        }

        if (filter.getNamePattern() != null) {
            sqlConditions.add(" NAME LIKE ? ");
            parameters.add(new CmsPreparedStatementStringParameter(filter.getNamePattern()));
        }

        if (filter.getState() != null) {
            sqlConditions.add("STATE = ?");
            parameters.add(new CmsPreparedStatementIntParameter(filter.getState().intValue()));
        }

        if (filter.getRejectStructureId() != null) {
            sqlConditions.add(" STRUCTURE_ID <> ? ");
            parameters.add(new CmsPreparedStatementStringParameter(filter.getRejectStructureId().toString()));
        }

        if (filter.getLocale() != null) {
            sqlConditions.add(" LOCALE = ? ");
            parameters.add(new CmsPreparedStatementStringParameter(filter.getLocale()));
        }

        String conditionString = CmsStringUtil.listAsString(sqlConditions, " AND ");
        return CmsPair.create(conditionString, parameters);
    }

    /**
     * Escapes the database wildcards within the resource path.<p>
     * 
     * This method is required to ensure chars in the resource path that have a special 
     * meaning in SQL (for example "_", which is the "any char" operator) are escaped.<p>
     * 
     * It will escape the following chars: 
     * <ul>
     * <li>"_" to "|_"</li>
     * </ul>
     * 
     * @param path the resource path
     * @return the escaped resource path
     */
    protected static String escapeDbWildcard(String path) {

        return CmsStringUtil.substitute(path, "_", "|_");
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#addUrlNameMappingEntry(org.opencms.db.CmsDbContext, boolean, org.opencms.db.urlname.CmsUrlNameMappingEntry)
     */
    public void addUrlNameMappingEntry(CmsDbContext dbc, boolean online, CmsUrlNameMappingEntry entry)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        String query = m_sqlManager.readQuery("C_ADD_URLNAME_MAPPING");
        query = replaceProject(query, online);
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, query);
            stmt.setString(1, entry.getName());
            stmt.setString(2, entry.getStructureId().toString());
            stmt.setInt(3, entry.getState());
            stmt.setLong(4, entry.getDateChanged());
            stmt.setString(5, entry.getLocale());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw wrapException(stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * Counts the number of siblings of a resource.<p>
     * 
     * @param dbc the current database context
     * @param projectId the current project id
     * @param resourceId the resource id to count the number of siblings from
     * 
     * @return number of siblings
     * @throws CmsDataAccessException if something goes wrong 
     */
    public int countSiblings(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        int count = 0;

        try {
            conn = m_sqlManager.getConnection(dbc);

            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_COUNT_SIBLINGS");
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                count = res.getInt(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return count;
    }

    /**
    * @see org.opencms.db.I_CmsVfsDriver#createContent(CmsDbContext, CmsUUID, CmsUUID, byte[])
    */
    public void createContent(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId, byte[] content)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            // create new offline content
            stmt = m_sqlManager.getPreparedStatement(conn, "C_OFFLINE_CONTENTS_WRITE");
            stmt.setString(1, resourceId.toString());
            if (content.length < 2000) {
                stmt.setBytes(2, content);
            } else {
                stmt.setBinaryStream(2, new ByteArrayInputStream(content), content.length);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(java.sql.ResultSet, CmsUUID)
     */
    public CmsFile createFile(ResultSet res, CmsUUID projectId) throws SQLException {

        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        int resourceType = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));
        String resourcePath = res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_PATH"));
        int resourceFlags = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_FLAGS"));
        int resourceState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STATE"));
        int structureState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_STATE"));
        long dateCreated = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED"));
        long dateLastModified = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED"));
        long dateReleased = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_RELEASED"));
        long dateExpired = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_EXPIRED"));
        int resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        byte[] content = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
        int siblingCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIBLING_COUNT"));
        long dateContent = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CONTENT"));
        int resourceVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
        int structureVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_VERSION"));

        // calculate the overall state
        int newState = (structureState > resourceState) ? structureState : resourceState;

        // in case of folder type ensure, that the root path has a trailing slash
        if (CmsFolder.isFolderType(resourceType)) {
            resourcePath = CmsFileUtil.addTrailingSeparator(resourcePath);
        }

        return new CmsFile(
            structureId,
            resourceId,
            resourcePath,
            resourceType,
            resourceFlags,
            projectId,
            CmsResourceState.valueOf(newState),
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            siblingCount,
            resourceSize,
            dateContent,
            resourceVersion + structureVersion,
            content);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(java.sql.ResultSet, CmsUUID, boolean)
     */
    public CmsFile createFile(ResultSet res, CmsUUID projectId, boolean hasFileContentInResultSet) throws SQLException {

        byte[] content = null;

        CmsUUID resProjectId = null;

        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        String resourcePath = res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_PATH"));
        int resourceType = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));
        int resourceFlags = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_FLAGS"));
        int resourceState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STATE"));
        int structureState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_STATE"));
        long dateCreated = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED"));
        long dateLastModified = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED"));
        long dateReleased = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_RELEASED"));
        long dateExpired = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_EXPIRED"));
        int resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        CmsUUID lockedInProject = new CmsUUID(res.getString("LOCKED_IN_PROJECT"));
        int siblingCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIBLING_COUNT"));
        long dateContent = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CONTENT"));
        int resourceVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
        int structureVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_VERSION"));

        // in case of folder type ensure, that the root path has a trailing slash
        if (CmsFolder.isFolderType(resourceType)) {
            resourcePath = CmsFileUtil.addTrailingSeparator(resourcePath);
        }
        if (hasFileContentInResultSet) {
            content = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
        }
        resProjectId = lockedInProject;
        int newState = (structureState > resourceState) ? structureState : resourceState;

        return new CmsFile(
            structureId,
            resourceId,
            resourcePath,
            resourceType,
            resourceFlags,
            resProjectId,
            CmsResourceState.valueOf(newState),
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            siblingCount,
            resourceSize,
            dateContent,
            resourceVersion + structureVersion,
            content);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFolder(java.sql.ResultSet, CmsUUID, boolean)
     */
    public CmsFolder createFolder(ResultSet res, CmsUUID projectId, boolean hasProjectIdInResultSet)
    throws SQLException {

        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        String resourcePath = res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_PATH"));
        int resourceType = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));
        int resourceFlags = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_FLAGS"));
        int resourceState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STATE"));
        int structureState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_STATE"));
        long dateCreated = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED"));
        long dateLastModified = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED"));
        long dateReleased = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_RELEASED"));
        long dateExpired = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_EXPIRED"));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        CmsUUID resProjectId = new CmsUUID(res.getString("LOCKED_IN_PROJECT"));
        int resourceVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
        int structureVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_VERSION"));
        int resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));

        // in case of folder type ensure, that the root path has a trailing slash
        if (CmsFolder.isFolderSize(resourceSize)) {
            resourcePath = CmsFileUtil.addTrailingSeparator(resourcePath);
        }

        int newState = (structureState > resourceState) ? structureState : resourceState;

        return new CmsFolder(
            structureId,
            resourceId,
            resourcePath,
            resourceType,
            resourceFlags,
            resProjectId,
            CmsResourceState.valueOf(newState),
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            resourceVersion + structureVersion);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createOnlineContent(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, byte[], int, boolean, boolean)
     */
    public void createOnlineContent(
        CmsDbContext dbc,
        CmsUUID resourceId,
        byte[] contents,
        int publishTag,
        boolean keepOnline,
        boolean needToUpdateContent) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            boolean dbcHasProjectId = (dbc.getProjectId() != null) && !dbc.getProjectId().isNullUUID();

            if (needToUpdateContent || dbcHasProjectId) {
                if (dbcHasProjectId || !OpenCms.getSystemInfo().isHistoryEnabled()) {
                    // remove the online content for this resource id
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_DELETE");
                    stmt.setString(1, resourceId.toString());
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                } else {
                    // put the online content in the history, only if explicit requested
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_HISTORY");
                    stmt.setString(1, resourceId.toString());
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }

                // create new online content
                stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_WRITE");

                stmt.setString(1, resourceId.toString());
                if (contents.length < 2000) {
                    stmt.setBytes(2, contents);
                } else {
                    stmt.setBinaryStream(2, new ByteArrayInputStream(contents), contents.length);
                }
                stmt.setInt(3, publishTag);
                stmt.setInt(4, publishTag);
                stmt.setInt(5, keepOnline ? 1 : 0);
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            } else {
                // update old content entry
                stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_CONTENTS_UPDATE");
                stmt.setInt(1, publishTag);
                stmt.setString(2, resourceId.toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);

                if (!keepOnline) {
                    // put the online content in the history 
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_HISTORY");
                    stmt.setString(1, resourceId.toString());
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createPropertyDefinition(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, java.lang.String, org.opencms.file.CmsPropertyDefinition.CmsPropertyType)
     */
    public CmsPropertyDefinition createPropertyDefinition(
        CmsDbContext dbc,
        CmsUUID projectId,
        String name,
        CmsPropertyDefinition.CmsPropertyType type) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_CREATE");
            stmt.setString(1, new CmsUUID().toString());
            stmt.setString(2, name);
            stmt.setInt(3, type.getMode());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        return readPropertyDefinition(dbc, name, projectId);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createRelation(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.relations.CmsRelation)
     */
    public void createRelation(CmsDbContext dbc, CmsUUID projectId, CmsRelation relation) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_CREATE_RELATION");
            stmt.setString(1, relation.getSourceId().toString());
            stmt.setString(2, relation.getSourcePath());
            stmt.setString(3, relation.getTargetId().toString());
            stmt.setString(4, relation.getTargetPath());
            stmt.setInt(5, relation.getType().getId());

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_CREATE_RELATION_2,
                    String.valueOf(projectId),
                    relation));
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createResource(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsResource, byte[])
     */
    public CmsResource createResource(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, byte[] content)
    throws CmsDataAccessException {

        CmsUUID newStructureId = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        // check the resource path
        String resourcePath = CmsFileUtil.removeTrailingSeparator(resource.getRootPath());
        if (resourcePath.length() > CmsDriverManager.MAX_VFS_RESOURCE_PATH_LENGTH) {
            throw new CmsDataAccessException(Messages.get().container(
                Messages.ERR_RESOURCENAME_TOO_LONG_2,
                resourcePath,
                new Integer(CmsDriverManager.MAX_VFS_RESOURCE_PATH_LENGTH)));
        }

        // check if the parent folder of the resource exists and if is not deleted
        if (!resource.getRootPath().equals("/")) {
            String parentFolderName = CmsResource.getParentFolder(resource.getRootPath());
            CmsFolder parentFolder = m_driverManager.getVfsDriver(dbc).readFolder(dbc, projectId, parentFolderName);
            if (parentFolder.getState().isDeleted()) {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_PARENT_FOLDER_DELETED_1,
                    resource.getRootPath()));
            }
        }

        // validate the resource length
        internalValidateResourceLength(resource);

        // set the resource state and modification dates
        CmsResourceState newState;
        long dateModified;
        long dateCreated;
        long dateContent = System.currentTimeMillis();

        if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
            newState = CmsResource.STATE_UNCHANGED;
            dateCreated = resource.getDateCreated();
            dateModified = resource.getDateLastModified();
        } else {
            newState = CmsResource.STATE_NEW;
            if (resource.isTouched()) {
                dateCreated = resource.getDateCreated();
                dateModified = resource.getDateLastModified();
            } else {
                dateCreated = System.currentTimeMillis();
                dateModified = dateCreated;
            }
        }

        // check if the resource already exists
        newStructureId = resource.getStructureId();

        try {
            CmsResource existingResource = m_driverManager.getVfsDriver(dbc).readResource(
                dbc,
                ((dbc.getProjectId() == null) || dbc.getProjectId().isNullUUID()) ? projectId : dbc.getProjectId(),
                resourcePath,
                true);
            if (existingResource.getState().isDeleted()) {
                // if an existing resource is deleted, it will be finally removed now.
                // but we have to reuse its id in order to avoid orphans in the online project
                newStructureId = existingResource.getStructureId();
                newState = CmsResource.STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = m_driverManager.getVfsDriver(dbc).readSiblings(
                    dbc,
                    projectId,
                    existingResource,
                    false);
                int propertyDeleteOption = (existingResource.getSiblingCount() > 1)
                ? CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES
                : CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                deletePropertyObjects(dbc, projectId, existingResource, propertyDeleteOption);
                removeFile(dbc, projectId, existingResource);

                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCES, modifiedResources)));
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCE, existingResource)));
            } else {
                // we have a collision: there exists already a resource with the same path/name which cannot be removed
                throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(
                    Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                    dbc.removeSiteRoot(resource.getRootPath())));
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // that's what we want in the best case- anything else should be thrown
        }

        try {
            // read the parent id
            String parentId = internalReadParentId(dbc, projectId, resourcePath);

            // use consistent version numbers if the file is being restored
            int lastVersion = m_driverManager.getHistoryDriver(dbc).readLastVersion(dbc, newStructureId);
            int newStrVersion = 0;
            int newResVersion = 0;
            if (lastVersion > 0) {
                I_CmsHistoryResource histRes = m_driverManager.getHistoryDriver(dbc).readResource(
                    dbc,
                    newStructureId,
                    lastVersion);
                newStrVersion = histRes.getStructureVersion();
                newResVersion = histRes.getResourceVersion();
            }

            // write the structure
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_STRUCTURE_WRITE");
            stmt.setString(1, newStructureId.toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setString(3, resourcePath);
            stmt.setInt(4, newState.getState());
            stmt.setLong(5, resource.getDateReleased());
            stmt.setLong(6, resource.getDateExpired());
            stmt.setString(7, parentId);
            stmt.setInt(8, newStrVersion); // starting version number
            stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, conn, stmt, null);

            if (!validateResourceIdExists(dbc, projectId, resource.getResourceId())) {
                try {
                    // create the resource record
                    conn = m_sqlManager.getConnection(dbc);
                    stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_WRITE");
                    stmt.setString(1, resource.getResourceId().toString());
                    stmt.setInt(2, resource.getTypeId());
                    stmt.setInt(3, resource.getFlags());
                    stmt.setLong(4, dateCreated);
                    stmt.setString(5, resource.getUserCreated().toString());
                    stmt.setLong(6, dateModified);
                    stmt.setString(7, resource.getUserLastModified().toString());
                    stmt.setInt(8, newState.getState());
                    stmt.setInt(9, resource.getLength());
                    stmt.setLong(10, dateContent);
                    stmt.setString(11, projectId.toString());
                    stmt.setInt(12, 1); // sibling count
                    stmt.setInt(13, newResVersion); // version number
                    stmt.executeUpdate();
                } finally {
                    m_sqlManager.closeAll(dbc, conn, stmt, null);
                }

                if (resource.isFile() && (content != null)) {
                    // create the file content
                    createContent(dbc, projectId, resource.getResourceId(), content);
                }
            } else {
                if ((content != null) || !resource.getState().isKeep()) {
                    CmsUUID projLastMod = projectId;
                    CmsResourceState state = CmsResource.STATE_CHANGED;
                    if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                        // in case a sibling is being published
                        projLastMod = resource.getProjectLastModified();
                        state = CmsResource.STATE_UNCHANGED;
                    }
                    // update the resource record only if state has changed or new content is provided
                    int sibCount = countSiblings(dbc, projectId, resource.getResourceId());
                    conn = m_sqlManager.getConnection(dbc);
                    stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_UPDATE_RESOURCES");
                    stmt.setInt(1, resource.getTypeId());
                    stmt.setInt(2, resource.getFlags());
                    stmt.setLong(3, dateModified);
                    stmt.setString(4, resource.getUserLastModified().toString());
                    stmt.setInt(5, state.getState());
                    stmt.setInt(6, resource.getLength());
                    stmt.setLong(7, resource.getDateContent());
                    stmt.setString(8, projLastMod.toString());
                    stmt.setInt(9, sibCount);
                    stmt.setString(10, resource.getResourceId().toString());
                    stmt.executeUpdate();

                    m_sqlManager.closeAll(dbc, conn, stmt, null);
                }

                if (resource.isFile()) {
                    if (content != null) {
                        // update the file content
                        writeContent(dbc, resource.getResourceId(), content);
                    } else if (resource.getState().isKeep()) {
                        // special case sibling creation - update the link Count
                        int sibCount = countSiblings(dbc, projectId, resource.getResourceId());
                        conn = m_sqlManager.getConnection(dbc);
                        stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_UPDATE_SIBLING_COUNT");
                        stmt.setInt(1, sibCount);
                        stmt.setString(2, resource.getResourceId().toString());
                        stmt.executeUpdate();
                        m_sqlManager.closeAll(dbc, null, stmt, null);

                        // update the resource flags
                        stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_UPDATE_FLAGS");
                        stmt.setInt(1, resource.getFlags());
                        stmt.setString(2, resource.getResourceId().toString());
                        stmt.executeUpdate();
                        m_sqlManager.closeAll(dbc, conn, stmt, null);
                    }
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        repairBrokenRelations(dbc, projectId, resource.getStructureId(), resource.getRootPath());
        return readResource(dbc, projectId, newStructureId, false);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createResource(java.sql.ResultSet, CmsUUID)
     */
    public CmsResource createResource(ResultSet res, CmsUUID projectId) throws SQLException {

        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        String resourcePath = res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_PATH"));
        int resourceType = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));
        int resourceFlags = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_FLAGS"));
        CmsUUID resourceProjectLastModified = new CmsUUID(
            res.getString(m_sqlManager.readQuery("C_RESOURCES_PROJECT_LASTMODIFIED")));
        int resourceState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STATE"));
        int structureState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_STATE"));
        long dateCreated = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED"));
        long dateLastModified = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED"));
        long dateReleased = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_RELEASED"));
        long dateExpired = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_EXPIRED"));
        int resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        boolean isFolder = CmsFolder.isFolderSize(resourceSize);
        if (isFolder) {
            // in case of folder type ensure, that the root path has a trailing slash
            resourcePath = CmsFileUtil.addTrailingSeparator(resourcePath);
        }
        long dateContent = isFolder ? -1 : res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CONTENT"));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        int siblingCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIBLING_COUNT"));
        int resourceVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
        int structureVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_VERSION"));

        int newState = (structureState > resourceState) ? structureState : resourceState;
        // if there is a change increase the version number
        int newVersion = resourceVersion + structureVersion + (newState > 0 ? 1 : 0);

        CmsResource newResource = new CmsResource(
            structureId,
            resourceId,
            resourcePath,
            resourceType,
            isFolder,
            resourceFlags,
            resourceProjectLastModified,
            CmsResourceState.valueOf(newState),
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            siblingCount,
            resourceSize,
            dateContent,
            newVersion);

        return newResource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createSibling(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public void createSibling(CmsDbContext dbc, CmsProject project, CmsResource resource) throws CmsDataAccessException {

        if (!project.getUuid().equals(CmsProject.ONLINE_PROJECT_ID)) {
            // this method is only intended to be used during publishing
            return;
        }

        // check if the resource already exists
        CmsResource existingSibling = null;
        CmsUUID newStructureId = resource.getStructureId();

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            existingSibling = readResource(dbc, project.getUuid(), resource.getRootPath(), true);

            if (existingSibling.getState().isDeleted()) {
                // if an existing resource is deleted, it will be finally removed now.
                // but we have to reuse its id in order to avoid orphans in the online project.
                newStructureId = existingSibling.getStructureId();

                // remove the existing file and it's properties
                List modifiedResources = readSiblings(dbc, project.getUuid(), existingSibling, false);
                int propertyDeleteOption = (existingSibling.getSiblingCount() > 1)
                ? CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES
                : CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                deletePropertyObjects(dbc, project.getUuid(), existingSibling, propertyDeleteOption);
                removeFile(dbc, project.getUuid(), existingSibling);

                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCES, modifiedResources)));
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCE, existingSibling)));
            } else {
                // we have a collision: there exists already a resource with the same path/name which could not be removed
                throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(
                    Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                    dbc.removeSiteRoot(resource.getRootPath())));
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // that's what we want in the best case- anything else should be thrown
        }

        // check if a resource with the specified ID already exists
        if (!validateResourceIdExists(dbc, project.getUuid(), resource.getResourceId())) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_CREATE_SIBLING_FILE_NOT_FOUND_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        // write a new structure referring to the resource
        try {
            // use consistent version numbers if the file is being restored
            int lastVersion = m_driverManager.getHistoryDriver(dbc).readLastVersion(dbc, newStructureId);
            int newStrVersion = 0;
            if (lastVersion > 0) {
                I_CmsHistoryResource histRes = m_driverManager.getHistoryDriver(dbc).readResource(
                    dbc,
                    newStructureId,
                    lastVersion);
                newStrVersion = histRes.getStructureVersion();
            }

            // read the parent id
            String parentId = internalReadParentId(dbc, project.getUuid(), resource.getRootPath());

            conn = m_sqlManager.getConnection(dbc);

            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, newStructureId.toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setString(3, resource.getRootPath());
            stmt.setInt(4, CmsResource.STATE_UNCHANGED.getState());
            stmt.setLong(5, resource.getDateReleased());
            stmt.setLong(6, resource.getDateExpired());
            stmt.setString(7, parentId);
            stmt.setInt(8, newStrVersion); // initial structure version number
            stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, conn, stmt, null);

            int sibCount = countSiblings(dbc, project.getUuid(), resource.getResourceId());
            conn = m_sqlManager.getConnection(dbc);

            // update the link Count
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_SIBLING_COUNT");
            stmt.setInt(1, sibCount);
            stmt.setString(2, resource.getResourceId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(dbc, null, stmt, null);

            // update the project last modified and flags
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCE_PROJECT");
            stmt.setInt(1, resource.getFlags());
            stmt.setString(2, resource.getProjectLastModified().toString());
            stmt.setString(3, resource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        repairBrokenRelations(dbc, project.getUuid(), resource.getStructureId(), resource.getRootPath());
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deletePropertyDefinition(org.opencms.db.CmsDbContext, org.opencms.file.CmsPropertyDefinition)
     */
    public void deletePropertyDefinition(CmsDbContext dbc, CmsPropertyDefinition metadef) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if ((internalCountProperties(dbc, metadef, CmsProject.ONLINE_PROJECT_ID) != 0)
                || (internalCountProperties(dbc, metadef, CmsUUID.getOpenCmsUUID()) != 0)) { // HACK: to get an offline project

                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_DELETE_USED_PROPERTY_1,
                    metadef.getName()));
            }

            conn = m_sqlManager.getConnection(dbc);

            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    // delete the offline property definition
                    stmt = m_sqlManager.getPreparedStatement(conn, CmsUUID.getOpenCmsUUID(), "C_PROPERTYDEF_DELETE"); // HACK: to get an offline project
                } else {
                    // delete the online property definition
                    stmt = m_sqlManager.getPreparedStatement(conn, CmsProject.ONLINE_PROJECT_ID, "C_PROPERTYDEF_DELETE");
                }

                stmt.setString(1, metadef.getId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deletePropertyObjects(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsResource, int)
     */
    public void deletePropertyObjects(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, int deleteOption)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            if (deleteOption == CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES) {
                // delete both the structure and resource property values mapped to the specified resource
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    projectId,
                    "C_PROPERTIES_DELETE_ALL_STRUCTURE_AND_RESOURCE_VALUES");
                stmt.setString(1, resource.getResourceId().toString());
                stmt.setInt(2, CmsProperty.RESOURCE_RECORD_MAPPING);
                stmt.setString(3, resource.getStructureId().toString());
                stmt.setInt(4, CmsProperty.STRUCTURE_RECORD_MAPPING);
            } else if (deleteOption == CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES) {
                // delete the structure values mapped to the specified resource
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    projectId,
                    "C_PROPERTIES_DELETE_ALL_VALUES_FOR_MAPPING_TYPE");
                stmt.setString(1, resource.getStructureId().toString());
                stmt.setInt(2, CmsProperty.STRUCTURE_RECORD_MAPPING);
            } else if (deleteOption == CmsProperty.DELETE_OPTION_DELETE_RESOURCE_VALUES) {
                // delete the resource property values mapped to the specified resource
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    projectId,
                    "C_PROPERTIES_DELETE_ALL_VALUES_FOR_MAPPING_TYPE");
                stmt.setString(1, resource.getResourceId().toString());
                stmt.setInt(2, CmsProperty.RESOURCE_RECORD_MAPPING);
            } else {
                throw new CmsDataAccessException(Messages.get().container(Messages.ERR_INVALID_DELETE_OPTION_1));
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deleteRelations(org.opencms.db.CmsDbContext, CmsUUID, CmsResource, org.opencms.relations.CmsRelationFilter)
     */
    public void deleteRelations(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, CmsRelationFilter filter)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            if (filter.isSource()) {
                List params = new ArrayList(7);

                StringBuffer queryBuf = new StringBuffer(256);
                queryBuf.append(m_sqlManager.readQuery(projectId, "C_DELETE_RELATIONS"));
                queryBuf.append(prepareRelationConditions(projectId, filter, resource, params, true));

                stmt = m_sqlManager.getPreparedStatementForSql(conn, queryBuf.toString());
                for (int i = 0; i < params.size(); i++) {
                    if (params.get(i) instanceof Integer) {
                        stmt.setInt(i + 1, ((Integer)params.get(i)).intValue());
                    } else {
                        stmt.setString(i + 1, (String)params.get(i));
                    }
                }
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }
            if (filter.isTarget()) {
                List params = new ArrayList(7);

                StringBuffer queryBuf = new StringBuffer(256);
                queryBuf.append(m_sqlManager.readQuery(projectId, "C_DELETE_RELATIONS"));
                queryBuf.append(prepareRelationConditions(projectId, filter, resource, params, false));

                stmt = m_sqlManager.getPreparedStatementForSql(conn, queryBuf.toString());
                for (int i = 0; i < params.size(); i++) {
                    if (params.get(i) instanceof Integer) {
                        stmt.setInt(i + 1, ((Integer)params.get(i)).intValue());
                    } else {
                        stmt.setString(i + 1, (String)params.get(i));
                    }
                }
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        // update broken remaining relations
        updateBrokenRelations(dbc, projectId, resource.getRootPath());
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deleteUrlNameMappingEntries(org.opencms.db.CmsDbContext, boolean, org.opencms.db.urlname.CmsUrlNameMappingFilter)
     */
    public void deleteUrlNameMappingEntries(CmsDbContext dbc, boolean online, CmsUrlNameMappingFilter filter)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            String query = m_sqlManager.readQuery("C_DELETE_URLNAME_MAPPINGS");
            query = replaceProject(query, online);
            stmt = getPreparedStatementForFilter(conn, query, filter);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw wrapException(stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#destroy()
     */
    public void destroy() throws Throwable {

        m_sqlManager = null;
        m_driverManager = null;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_DRIVER_1, getClass().getName()));
        }
    }

    /**
     * Returns all organizational units for the given resource.<p>
     * 
     * @param dbc the database context
     * @param projectId the id of the project
     * @param resource the resource
     * 
     * @return a list of {@link org.opencms.security.CmsOrganizationalUnit} objects
     * 
     * @throws CmsDbSqlException if something goes wrong
     */
    public List<CmsOrganizationalUnit> getResourceOus(CmsDbContext dbc, CmsUUID projectId, CmsResource resource)
    throws CmsDbSqlException {

        List<CmsOrganizationalUnit> ous = new ArrayList<CmsOrganizationalUnit>();
        String resName = resource.getRootPath();
        if (resource.isFolder() && !resName.endsWith("/")) {
            resName += "/";
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        List<CmsRelation> rels = new ArrayList<CmsRelation>();

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatementForSql(
                conn,
                m_sqlManager.readQuery(projectId, "C_READ_RESOURCE_OUS"));
            stmt.setInt(1, CmsRelationType.OU_RESOURCE.getId());
            stmt.setString(2, resName);
            res = stmt.executeQuery();
            while (res.next()) {
                rels.add(internalReadRelation(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        for (CmsRelation rel : rels) {
            try {
                ous.add(m_driverManager.readOrganizationalUnit(
                    dbc,
                    rel.getSourcePath().substring(CmsUserDriver.ORGUNIT_BASE_FOLDER.length())));
            } catch (CmsException e) {
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return ous;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#getSqlManager()
     */
    public CmsSqlManager getSqlManager() {

        return m_sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#incrementCounter(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public int incrementCounter(CmsDbContext dbc, String name) throws CmsDataAccessException {

        Integer counterObj = internalReadCounter(dbc, name);
        int result;
        if (counterObj == null) {
            internalCreateCounter(dbc, name, 1);
            result = 0;
        } else {
            result = counterObj.intValue();
            internalIncrementCounter(dbc, name);
        }
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(org.opencms.db.CmsDbContext, org.opencms.configuration.CmsConfigurationManager, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(
        CmsDbContext dbc,
        CmsConfigurationManager configurationManager,
        List successiveDrivers,
        CmsDriverManager driverManager) {

        CmsParameterConfiguration configuration = configurationManager.getConfiguration();
        String poolUrl = configuration.get("db.vfs.pool");
        String classname = configuration.get("db.vfs.sqlmanager");
        m_sqlManager = this.initSqlManager(classname);
        m_sqlManager.init(I_CmsVfsDriver.DRIVER_TYPE_ID, poolUrl);

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
     * @see org.opencms.db.I_CmsVfsDriver#initSqlManager(String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#moveResource(CmsDbContext, CmsUUID, CmsResource, String)
     */
    public void moveResource(CmsDbContext dbc, CmsUUID projectId, CmsResource source, String destinationPath)
    throws CmsDataAccessException {

        if ((dbc.getRequestContext() != null)
            && (dbc.getRequestContext().getAttribute(REQ_ATTR_CHECK_PERMISSIONS) != null)) {
            // only check write permissions
            checkWritePermissionsInFolder(dbc, source);
            return;
        }

        // determine destination folder        
        String destinationFoldername = CmsResource.getParentFolder(destinationPath);

        // read the destination folder (will also check read permissions)
        CmsFolder destinationFolder = m_driverManager.readFolder(dbc, destinationFoldername, CmsResourceFilter.ALL);

        if (!projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
            // check online resource
            try {
                CmsResource onlineResource = m_driverManager.getVfsDriver(dbc).readResource(
                    dbc,
                    CmsProject.ONLINE_PROJECT_ID,
                    destinationPath,
                    true);

                if (!onlineResource.getStructureId().equals(source.getStructureId())) {
                    // source resource has been moved and it is not the 
                    // same as the resource that is being trying to move back
                    CmsResource offlineResource = null;
                    try {
                        // read new location in offline project
                        offlineResource = readResource(
                            dbc,
                            dbc.getRequestContext().getCurrentProject().getUuid(),
                            onlineResource.getStructureId(),
                            true);
                    } catch (CmsException e) {
                        // should never happen
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e.getMessage(), e);
                        }
                    }

                    throw new CmsVfsOnlineResourceAlreadyExistsException(Messages.get().container(
                        Messages.ERR_OVERWRITE_MOVED_RESOURCE_3,
                        dbc.removeSiteRoot(source.getRootPath()),
                        dbc.removeSiteRoot(destinationPath),
                        dbc.removeSiteRoot(offlineResource == null ? "__ERROR__" : offlineResource.getRootPath())));
                }
            } catch (CmsVfsResourceNotFoundException e) {
                // ok, no online resource
            }
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_MOVE");
            stmt.setString(1, CmsFileUtil.removeTrailingSeparator(destinationPath)); // must remove trailing slash
            stmt.setString(2, destinationFolder.getStructureId().toString());
            stmt.setString(3, source.getStructureId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        moveRelations(dbc, projectId, source.getStructureId(), destinationPath);
        repairBrokenRelations(dbc, projectId, source.getStructureId(), destinationPath);
        try {
            m_driverManager.repairCategories(dbc, projectId, readResource(dbc, projectId, destinationPath, true));
        } catch (CmsException e) {
            throw new CmsDataAccessException(e.getMessageContainer(), e);
        }
        // repair project resources
        if (!projectId.equals(CmsProject.ONLINE_PROJECT_ID) && (dbc.getRequestContext() != null)) {
            String deletedResourceRootPath = source.getRootPath();
            dbc.getRequestContext().setAttribute(CmsProjectDriver.DBC_ATTR_READ_PROJECT_FOR_RESOURCE, Boolean.TRUE);
            I_CmsProjectDriver projectDriver = m_driverManager.getProjectDriver(dbc);
            Iterator itProjects = projectDriver.readProjects(dbc, deletedResourceRootPath).iterator();
            while (itProjects.hasNext()) {
                CmsProject project = (CmsProject)itProjects.next();
                projectDriver.deleteProjectResource(dbc, project.getUuid(), deletedResourceRootPath);
                projectDriver.createProjectResource(dbc, project.getUuid(), destinationPath);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#publishResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.file.CmsResource)
     */
    public void publishResource(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource onlineResource,
        CmsResource offlineResource) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        // validate the resource length
        internalValidateResourceLength(offlineResource);
        int resourceSize = offlineResource.getLength();

        String resourcePath = CmsFileUtil.removeTrailingSeparator(offlineResource.getRootPath());

        try {
            int sibCount = countSiblings(dbc, onlineProject.getUuid(), onlineResource.getResourceId());
            boolean resourceExists = validateResourceIdExists(
                dbc,
                onlineProject.getUuid(),
                offlineResource.getResourceId());
            conn = m_sqlManager.getConnection(dbc);
            if (resourceExists) {
                // the resource record exists online already
                // update the online resource record
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_UPDATE_RESOURCES");
                stmt.setInt(1, offlineResource.getTypeId());
                stmt.setInt(2, offlineResource.getFlags());
                stmt.setLong(3, offlineResource.getDateLastModified());
                stmt.setString(4, offlineResource.getUserLastModified().toString());
                stmt.setInt(5, CmsResource.STATE_UNCHANGED.getState());
                stmt.setInt(6, resourceSize);
                stmt.setLong(7, offlineResource.getDateContent());
                stmt.setString(8, offlineResource.getProjectLastModified().toString());
                stmt.setInt(9, sibCount);
                stmt.setString(10, offlineResource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, conn, stmt, null);
            } else {
                // the resource record does NOT exist online yet
                // create the resource record online
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_WRITE");
                stmt.setString(1, offlineResource.getResourceId().toString());
                stmt.setInt(2, offlineResource.getTypeId());
                stmt.setInt(3, offlineResource.getFlags());
                stmt.setLong(4, offlineResource.getDateCreated());
                stmt.setString(5, offlineResource.getUserCreated().toString());
                stmt.setLong(6, offlineResource.getDateLastModified());
                stmt.setString(7, offlineResource.getUserLastModified().toString());
                stmt.setInt(8, CmsResource.STATE_UNCHANGED.getState());
                stmt.setInt(9, resourceSize);
                stmt.setLong(10, offlineResource.getDateContent());
                stmt.setString(11, offlineResource.getProjectLastModified().toString());
                stmt.setInt(12, 1); // initial siblings count
                stmt.setInt(13, 1); // initial resource version
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, conn, stmt, null);
            }

            // read the parent id
            String parentId = internalReadParentId(dbc, onlineProject.getUuid(), resourcePath);
            boolean structureExists = validateStructureIdExists(
                dbc,
                onlineProject.getUuid(),
                offlineResource.getStructureId());
            conn = m_sqlManager.getConnection(dbc);
            if (structureExists) {
                // update the online structure record
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_UPDATE_STRUCTURE");
                stmt.setString(1, offlineResource.getResourceId().toString());
                stmt.setString(2, resourcePath);
                stmt.setInt(3, CmsResource.STATE_UNCHANGED.getState());
                stmt.setLong(4, offlineResource.getDateReleased());
                stmt.setLong(5, offlineResource.getDateExpired());
                stmt.setString(6, parentId);
                stmt.setString(7, offlineResource.getStructureId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            } else {
                // create the structure record online
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_STRUCTURE_WRITE");
                stmt.setString(1, offlineResource.getStructureId().toString());
                stmt.setString(2, offlineResource.getResourceId().toString());
                stmt.setString(3, resourcePath);
                stmt.setInt(4, CmsResource.STATE_UNCHANGED.getState());
                stmt.setLong(5, offlineResource.getDateReleased());
                stmt.setLong(6, offlineResource.getDateExpired());
                stmt.setString(7, parentId);
                stmt.setInt(8, resourceExists ? 1 : 0); // new resources start with 0, new siblings with 1
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#publishVersions(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, boolean)
     */
    public void publishVersions(CmsDbContext dbc, CmsResource resource, boolean firstSibling)
    throws CmsDataAccessException {

        // if resource is null just flush the internal cache
        if (resource == null) {
            m_resOp.clear();
            return;
        }

        if (!dbc.getProjectId().isNullUUID() || dbc.currentProject().isOnlineProject()) {
            // this method is supposed to be used only in the offline project
            return;
        }

        if (firstSibling) {
            // reset the resource operation flag
            m_resOp.remove(resource.getResourceId());
        }

        boolean resOp = false; // assume structure operation

        CmsResourceState resState = internalReadResourceState(dbc, dbc.currentProject().getUuid(), resource);
        CmsResourceState strState = internalReadStructureState(dbc, dbc.currentProject().getUuid(), resource);

        if (!resState.isUnchanged()) {
            if (strState.isDeleted()) {
                resOp = (resState.isDeleted() || (resource.getSiblingCount() == 1) || (countSiblings(
                    dbc,
                    dbc.currentProject().getUuid(),
                    resource.getResourceId()) == 1));
            } else {
                resOp = true;
            }
        }

        if (!firstSibling) {
            if (resOp) {
                return;
            }
            if (m_resOp.contains(resource.getResourceId())) {
                return;
            }
        }

        // read the offline version numbers
        Map versions = readVersions(
            dbc,
            dbc.currentProject().getUuid(),
            resource.getResourceId(),
            resource.getStructureId());
        int strVersion = ((Integer)versions.get("structure")).intValue();
        int resVersion = ((Integer)versions.get("resource")).intValue();

        if (resOp) {
            if (resource.getSiblingCount() > 1) {
                m_resOp.add(resource.getResourceId());
            }
            resVersion++;
        }
        if (!resOp) {
            strVersion++;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            if (resOp) {
                // update the resource version
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    CmsProject.ONLINE_PROJECT_ID,
                    "C_RESOURCES_UPDATE_RESOURCE_VERSION");
                stmt.setInt(1, resVersion);
                stmt.setString(2, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }
            if (!resOp || strState.isNew()) {
                // update the structure version
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    CmsProject.ONLINE_PROJECT_ID,
                    "C_RESOURCES_UPDATE_STRUCTURE_VERSION");
                stmt.setInt(1, strVersion);
                stmt.setString(2, resource.getStructureId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readChildResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, boolean, boolean)
     */
    public List readChildResources(
        CmsDbContext dbc,
        CmsProject currentProject,
        CmsResource resource,
        boolean getFolders,
        boolean getFiles) throws CmsDataAccessException {

        List result = new ArrayList();
        CmsUUID projectId = currentProject.getUuid();

        String resourceTypeClause;
        if (getFolders && getFiles) {
            resourceTypeClause = null;
        } else if (getFolders) {
            resourceTypeClause = m_sqlManager.readQuery(projectId, "C_RESOURCES_GET_SUBRESOURCES_GET_FOLDERS");
        } else {
            resourceTypeClause = m_sqlManager.readQuery(projectId, "C_RESOURCES_GET_SUBRESOURCES_GET_FILES");
        }
        StringBuffer query = new StringBuffer();
        query.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_GET_SUBRESOURCES"));
        if (resourceTypeClause != null) {
            query.append(' ');
            query.append(resourceTypeClause);
        }

        String sizeColumn = m_sqlManager.readQuery("C_RESOURCES_SIZE");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, query.toString());
            stmt.setString(1, resource.getStructureId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                long size = res.getInt(sizeColumn);
                if (CmsFolder.isFolderSize(size)) {
                    result.add(createFolder(res, projectId, false));
                } else {
                    result.add(createFile(res, projectId, false));
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        // sort result in memory, this is to avoid DB dependencies in the result order
        Collections.sort(result, I_CmsResource.COMPARE_ROOT_PATH_IGNORE_CASE_FOLDERS_FIRST);
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readContent(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID)
     */
    public byte[] readContent(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        byte[] byteRes = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_ONLINE_FILES_CONTENT");
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_OFFLINE_FILES_CONTENT");
            }
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                //query to read Array of bytes for the attribute FILE_CONTENT
                byteRes = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_CONTENT_WITH_RESOURCE_ID_2,
                    resourceId,
                    Boolean.valueOf(projectId.equals(CmsProject.ONLINE_PROJECT_ID))));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return byteRes;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolder(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID)
     */
    public CmsFolder readFolder(CmsDbContext dbc, CmsUUID projectId, CmsUUID folderId) throws CmsDataAccessException {

        CmsFolder folder = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READBYID");
            stmt.setString(1, folderId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                folder = createFolder(res, projectId, true);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_FOLDER_WITH_ID_1,
                    folderId));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return folder;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolder(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String)
     */
    public CmsFolder readFolder(CmsDbContext dbc, CmsUUID projectId, String folderPath) throws CmsDataAccessException {

        CmsFolder folder = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        folderPath = CmsFileUtil.removeTrailingSeparator(folderPath);
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ");

            stmt.setString(1, folderPath);
            res = stmt.executeQuery();

            if (res.next()) {
                folder = createFolder(res, projectId, true);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_FOLDER_1,
                    dbc.removeSiteRoot(folderPath)));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return folder;

    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readParentFolder(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID)
     */
    public CmsFolder readParentFolder(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId)
    throws CmsDataAccessException {

        CmsFolder parent = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_PARENT_BY_ID");
            stmt.setString(1, structureId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                parent = new CmsFolder(createResource(res, projectId));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return parent;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinition(org.opencms.db.CmsDbContext, java.lang.String, CmsUUID)
     */
    public CmsPropertyDefinition readPropertyDefinition(CmsDbContext dbc, String name, CmsUUID projectId)
    throws CmsDataAccessException {

        CmsPropertyDefinition propDef = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_READ");
            stmt.setString(1, name);
            res = stmt.executeQuery();

            // if result set exists - return it
            if (res.next()) {
                propDef = new CmsPropertyDefinition(
                    new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_ID"))),
                    res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")),
                    CmsPropertyDefinition.CmsPropertyType.valueOf(res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_TYPE"))));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_NO_PROPERTYDEF_WITH_NAME_1,
                    name));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return propDef;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinitions(org.opencms.db.CmsDbContext, CmsUUID)
     */
    public List readPropertyDefinitions(CmsDbContext dbc, CmsUUID projectId) throws CmsDataAccessException {

        ArrayList propertyDefinitions = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_READALL");

            res = stmt.executeQuery();
            while (res.next()) {
                propertyDefinitions.add(new CmsPropertyDefinition(
                    new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_ID"))),
                    res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")),
                    CmsPropertyDefinition.CmsPropertyType.valueOf(res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_TYPE")))));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return propertyDefinitions;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyObject(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public CmsProperty readPropertyObject(CmsDbContext dbc, String key, CmsProject project, CmsResource resource)
    throws CmsDataAccessException {

        CmsUUID projectId = ((dbc.getProjectId() == null) || dbc.getProjectId().isNullUUID())
        ? project.getUuid()
        : dbc.getProjectId();

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String propertyValue = null;
        int mappingType = -1;
        CmsProperty property = null;
        int resultSize = 0;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_READ");

            stmt.setString(1, key);
            stmt.setString(2, resource.getStructureId().toString());
            stmt.setString(3, resource.getResourceId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                if (resultSize >= 2) {
                    throw new CmsDbConsistencyException(Messages.get().container(
                        Messages.ERR_TOO_MANY_PROPERTIES_3,
                        key,
                        resource.getRootPath(),
                        new Integer(resultSize)));
                }

                if (property == null) {
                    property = new CmsProperty();
                    property.setName(key);
                }

                propertyValue = res.getString(1);
                mappingType = res.getInt(2);

                if (mappingType == CmsProperty.STRUCTURE_RECORD_MAPPING) {
                    property.setStructureValue(propertyValue);
                } else if (mappingType == CmsProperty.RESOURCE_RECORD_MAPPING) {
                    property.setResourceValue(propertyValue);
                } else {
                    throw new CmsDbConsistencyException(Messages.get().container(
                        Messages.ERR_UNKNOWN_PROPERTY_VALUE_MAPPING_3,
                        resource.getRootPath(),
                        new Integer(mappingType),
                        key));
                }

                resultSize++;
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return (property != null) ? property : CmsProperty.getNullProperty();
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyObjects(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public List readPropertyObjects(CmsDbContext dbc, CmsProject project, CmsResource resource)
    throws CmsDataAccessException {

        CmsUUID projectId = ((dbc.getProjectId() == null) || dbc.getProjectId().isNullUUID())
        ? project.getUuid()
        : dbc.getProjectId();

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        int mappingType = -1;
        Map propertyMap = new HashMap();

        String propertyKey;
        String propertyValue;
        CmsProperty property;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_READALL");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setString(2, resource.getResourceId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                propertyKey = null;
                propertyValue = null;
                mappingType = -1;

                propertyKey = res.getString(1);
                propertyValue = res.getString(2);
                mappingType = res.getInt(3);

                property = (CmsProperty)propertyMap.get(propertyKey);
                if (property == null) {
                    // there doesn't exist a property object for this key yet
                    property = new CmsProperty();
                    property.setName(propertyKey);
                    propertyMap.put(propertyKey, property);
                }

                if (mappingType == CmsProperty.STRUCTURE_RECORD_MAPPING) {
                    // this property value is mapped to a structure record
                    property.setStructureValue(propertyValue);
                } else if (mappingType == CmsProperty.RESOURCE_RECORD_MAPPING) {
                    // this property value is mapped to a resource record
                    property.setResourceValue(propertyValue);
                } else {
                    throw new CmsDbConsistencyException(Messages.get().container(
                        Messages.ERR_UNKNOWN_PROPERTY_VALUE_MAPPING_3,
                        resource.getRootPath(),
                        new Integer(mappingType),
                        propertyKey));
                }
                property.setOrigin(resource.getRootPath());
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return new ArrayList(propertyMap.values());
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readRelations(org.opencms.db.CmsDbContext, CmsUUID, CmsResource, org.opencms.relations.CmsRelationFilter)
     */
    public List readRelations(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, CmsRelationFilter filter)
    throws CmsDataAccessException {

        Set relations = new HashSet();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            if (filter.isSource()) {
                List params = new ArrayList(7);

                StringBuffer queryBuf = new StringBuffer(256);
                queryBuf.append(m_sqlManager.readQuery(projectId, "C_READ_RELATIONS"));
                queryBuf.append(prepareRelationConditions(projectId, filter, resource, params, true));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(queryBuf.toString());
                }

                stmt = m_sqlManager.getPreparedStatementForSql(conn, queryBuf.toString());
                for (int i = 0; i < params.size(); i++) {
                    if (params.get(i) instanceof Integer) {
                        stmt.setInt(i + 1, ((Integer)params.get(i)).intValue());
                    } else {
                        stmt.setString(i + 1, (String)params.get(i));
                    }
                }
                res = stmt.executeQuery();
                while (res.next()) {
                    relations.add(internalReadRelation(res));
                }
                m_sqlManager.closeAll(dbc, null, stmt, res);
            }

            if (filter.isTarget()) {
                List params = new ArrayList(7);

                StringBuffer queryBuf = new StringBuffer(256);
                queryBuf.append(m_sqlManager.readQuery(projectId, "C_READ_RELATIONS"));
                queryBuf.append(prepareRelationConditions(projectId, filter, resource, params, false));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(queryBuf.toString());
                }

                stmt = m_sqlManager.getPreparedStatementForSql(conn, queryBuf.toString());
                for (int i = 0; i < params.size(); i++) {
                    if (params.get(i) instanceof Integer) {
                        stmt.setInt(i + 1, ((Integer)params.get(i)).intValue());
                    } else {
                        stmt.setString(i + 1, (String)params.get(i));
                    }
                }
                res = stmt.executeQuery();
                while (res.next()) {
                    relations.add(internalReadRelation(res));
                }
                m_sqlManager.closeAll(dbc, null, stmt, res);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        List result = new ArrayList(relations);
        Collections.sort(result, CmsRelation.COMPARATOR);
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResource(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID, boolean)
     */
    public CmsResource readResource(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId, boolean includeDeleted)
    throws CmsDataAccessException {

        CmsResource resource = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READBYID");

            stmt.setString(1, structureId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                resource = createResource(res, projectId);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_RESOURCE_WITH_ID_1,
                    structureId));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        // check if this resource is marked as deleted and if we are allowed to return a deleted resource
        if ((resource != null) && resource.getState().isDeleted() && !includeDeleted) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_READ_DELETED_RESOURCE_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        return resource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResource(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String, boolean)
     */
    public CmsResource readResource(CmsDbContext dbc, CmsUUID projectId, String path, boolean includeDeleted)
    throws CmsDataAccessException {

        CmsResource resource = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        // must remove trailing slash
        int len = path.length();
        path = CmsFileUtil.removeTrailingSeparator(path);
        boolean endsWithSlash = (len != path.length());

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ");

            stmt.setString(1, path);
            res = stmt.executeQuery();

            if (res.next()) {
                resource = createResource(res, projectId);

                // check if the resource is a file, it is not allowed to end with a "/" then
                if (endsWithSlash && resource.isFile()) {
                    throw new CmsVfsResourceNotFoundException(Messages.get().container(
                        Messages.ERR_READ_RESOURCE_1,
                        dbc.removeSiteRoot(path + "/")));
                }

                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_RESOURCE_1,
                    dbc.removeSiteRoot(path)));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        // check if this resource is marked as deleted and if we are allowed to return a deleted resource
        if ((resource != null) && resource.getState().isDeleted() && !includeDeleted) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_READ_DELETED_RESOURCE_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        return resource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResources(org.opencms.db.CmsDbContext, CmsUUID, CmsResourceState, int)
     */
    public List readResources(CmsDbContext dbc, CmsUUID projectId, CmsResourceState state, int mode)
    throws CmsDataAccessException {

        List result = new ArrayList();

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            if (mode == CmsDriverManager.READMODE_MATCHSTATE) {
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    projectId,
                    "C_RESOURCES_GET_RESOURCE_IN_PROJECT_WITH_STATE");
                stmt.setString(1, projectId.toString());
                stmt.setInt(2, state.getState());
                stmt.setInt(3, state.getState());
                stmt.setInt(4, state.getState());
                stmt.setInt(5, state.getState());
            } else if (mode == CmsDriverManager.READMODE_UNMATCHSTATE) {
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    projectId,
                    "C_RESOURCES_GET_RESOURCE_IN_PROJECT_WITHOUT_STATE");
                stmt.setString(1, projectId.toString());
                stmt.setInt(2, state.getState());
                stmt.setInt(3, state.getState());
            } else {
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    projectId,
                    "C_RESOURCES_GET_RESOURCE_IN_PROJECT_IGNORE_STATE");
                stmt.setString(1, projectId.toString());
            }

            res = stmt.executeQuery();
            while (res.next()) {
                CmsResource resource = createResource(res, projectId);
                result.add(resource);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResourcesForPrincipalACE(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID)
     */
    public List readResourcesForPrincipalACE(CmsDbContext dbc, CmsProject project, CmsUUID principalId)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        CmsResource currentResource = null;
        List resources = new ArrayList();

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_SELECT_RESOURCES_FOR_PRINCIPAL_ACE");

            stmt.setString(1, principalId.toString());
            res = stmt.executeQuery();

            while (res.next()) {
                currentResource = createFile(res, project.getUuid(), false);
                resources.add(currentResource);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResourcesForPrincipalAttr(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID)
     */
    public List readResourcesForPrincipalAttr(CmsDbContext dbc, CmsProject project, CmsUUID principalId)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        CmsResource currentResource = null;
        List resources = new ArrayList();

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_SELECT_RESOURCES_FOR_PRINCIPAL_ATTR");

            stmt.setString(1, principalId.toString());
            stmt.setString(2, principalId.toString());
            res = stmt.executeQuery();

            while (res.next()) {
                currentResource = createFile(res, project.getUuid(), false);
                resources.add(currentResource);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResourcesWithProperty(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID, String, String)
     */
    public List readResourcesWithProperty(
        CmsDbContext dbc,
        CmsUUID projectId,
        CmsUUID propertyDef,
        String path,
        String value) throws CmsDataAccessException {

        List resources = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            if (value == null) {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF");
                stmt.setString(1, propertyDef.toString());
                stmt.setString(2, path + "%");
                stmt.setString(3, propertyDef.toString());
                stmt.setString(4, path + "%");
            } else {
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    projectId,
                    "C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF_VALUE");
                stmt.setString(1, propertyDef.toString());
                stmt.setString(2, path + "%");
                stmt.setString(3, "%" + value + "%");
                stmt.setString(4, propertyDef.toString());
                stmt.setString(5, path + "%");
                stmt.setString(6, "%" + value + "%");
            }
            res = stmt.executeQuery();

            while (res.next()) {
                CmsResource resource = createResource(res, projectId);
                resources.add(resource);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResourceTree(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String, int, CmsResourceState, long, long, long, long, long, long, int)
     */
    public List readResourceTree(
        CmsDbContext dbc,
        CmsUUID projectId,
        String parentPath,
        int type,
        CmsResourceState state,
        long lastModifiedAfter,
        long lastModifiedBefore,
        long releasedAfter,
        long releasedBefore,
        long expiredAfter,
        long expiredBefore,
        int mode) throws CmsDataAccessException {

        List result = new ArrayList();

        StringBuffer conditions = new StringBuffer();
        List params = new ArrayList(5);

        // prepare the selection criteria
        prepareProjectCondition(projectId, mode, conditions, params);
        prepareResourceCondition(projectId, mode, conditions);
        prepareTypeCondition(projectId, type, mode, conditions, params);
        prepareTimeRangeCondition(projectId, lastModifiedAfter, lastModifiedBefore, conditions, params);
        prepareReleasedTimeRangeCondition(projectId, releasedAfter, releasedBefore, conditions, params);
        prepareExpiredTimeRangeCondition(projectId, expiredAfter, expiredBefore, conditions, params);
        preparePathCondition(projectId, parentPath, mode, conditions, params);
        prepareStateCondition(projectId, state, mode, conditions, params);

        // now read matching resources within the subtree 
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_READ_TREE"));
            queryBuf.append(conditions);
            queryBuf.append(" ");
            queryBuf.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_ORDER_BY_PATH"));
            stmt = m_sqlManager.getPreparedStatementForSql(conn, queryBuf.toString());

            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Integer) {
                    stmt.setInt(i + 1, ((Integer)params.get(i)).intValue());
                } else if (params.get(i) instanceof Long) {
                    stmt.setLong(i + 1, ((Long)params.get(i)).longValue());
                } else {
                    stmt.setString(i + 1, (String)params.get(i));
                }
            }

            res = stmt.executeQuery();
            while (res.next()) {
                CmsResource resource = createResource(res, projectId);
                result.add(resource);
            }

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readSiblings(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsResource, boolean)
     */
    public List readSiblings(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, boolean includeDeleted)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        CmsResource currentResource = null;
        List vfsLinks = new ArrayList();

        try {
            conn = m_sqlManager.getConnection(dbc);

            if (includeDeleted) {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_SELECT_VFS_SIBLINGS");
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_SELECT_NONDELETED_VFS_SIBLINGS");
            }

            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                currentResource = createFile(res, projectId, false);
                vfsLinks.add(currentResource);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return vfsLinks;
    }

    /**
     * Reads the URL name mapping entries which match a given filter.<p>
     * 
     * @param dbc the database context 
     * @param online if true, reads from the online mapping, else from the offline mapping 
     * @param filter the filter which the entries to be read should match 
     * 
     * @return the mapping entries which match the given filter 
     * 
     * @throws CmsDataAccessException if something goes wrong 
     */
    public List<CmsUrlNameMappingEntry> readUrlNameMappingEntries(
        CmsDbContext dbc,
        boolean online,
        CmsUrlNameMappingFilter filter) throws CmsDataAccessException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        List<CmsUrlNameMappingEntry> result = new ArrayList<CmsUrlNameMappingEntry>();
        try {
            conn = m_sqlManager.getConnection(dbc);
            String query = m_sqlManager.readQuery("C_READ_URLNAME_MAPPINGS");
            query = replaceProject(query, online);
            stmt = getPreparedStatementForFilter(conn, query, filter);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                CmsUrlNameMappingEntry entry = internalCreateUrlNameMappingEntry(resultSet);
                result.add(entry);
            }
            return result;
        } catch (SQLException e) {
            throw wrapException(stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, resultSet);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readVersions(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public Map readVersions(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId, CmsUUID structureId)
    throws CmsDataAccessException {

        int structureVersion = -1;
        int resourceVersion = -1;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            // read the offline version numbers, first for the resource entry
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_VERSION_RES");
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();
            if (res.next()) {
                resourceVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);
            // then for the structure entry
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_VERSION_STR");
            stmt.setString(1, structureId.toString());
            res = stmt.executeQuery();
            if (res.next()) {
                structureVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_VERSION"));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        Map result = new HashMap();
        result.put("structure", new Integer(structureVersion));
        result.put(I_CmsEventListener.KEY_RESOURCE, new Integer(resourceVersion));
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#removeFile(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsResource)
     */
    public void removeFile(CmsDbContext dbc, CmsUUID projectId, CmsResource resource) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        int siblingCount = 0;

        try {
            conn = m_sqlManager.getConnection(dbc);

            // delete the structure record
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_STRUCTURE_DELETE_BY_STRUCTUREID");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(dbc, conn, stmt, null);

            // count the references to the resource
            siblingCount = countSiblings(dbc, projectId, resource.getResourceId());

            conn = m_sqlManager.getConnection(dbc);
            if (siblingCount > 0) {
                // update the link Count
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_UPDATE_SIBLING_COUNT");
                stmt.setInt(1, siblingCount);
                stmt.setString(2, resource.getResourceId().toString());
                stmt.executeUpdate();

                m_sqlManager.closeAll(dbc, null, stmt, null);

                // update the resource flags
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_UPDATE_FLAGS");
                stmt.setInt(1, resource.getFlags());
                stmt.setString(2, resource.getResourceId().toString());
                stmt.executeUpdate();

            } else {
                // if not referenced any longer, also delete the resource and the content record
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_DELETE_BY_RESOURCEID");
                stmt.setString(1, resource.getResourceId().toString());
                stmt.executeUpdate();

                m_sqlManager.closeAll(dbc, null, stmt, null);

                boolean dbcHasProjectId = (dbc.getProjectId() != null) && !dbc.getProjectId().isNullUUID();

                // if online we have to keep historical content
                if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                    // put the online content in the history 
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_HISTORY");
                    stmt.setString(1, resource.getResourceId().toString());
                    stmt.executeUpdate();
                } else if (dbcHasProjectId) {
                    // remove current online version
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_DELETE");
                    stmt.setString(1, resource.getResourceId().toString());
                    stmt.executeUpdate();
                } else {
                    // delete content records with this resource id
                    stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_OFFLINE_FILE_CONTENT_DELETE");
                    stmt.setString(1, resource.getResourceId().toString());
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#removeFolder(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public void removeFolder(CmsDbContext dbc, CmsProject currentProject, CmsResource resource)
    throws CmsDataAccessException {

        if ((dbc.getRequestContext() != null)
            && (dbc.getRequestContext().getAttribute(REQ_ATTR_CHECK_PERMISSIONS) != null)) {
            // only check write permissions
            checkWritePermissionsInFolder(dbc, resource);
            return;
        }

        // check if the folder has any resources in it
        Iterator childResources = readChildResources(dbc, currentProject, resource, true, true).iterator();

        CmsUUID projectId = CmsProject.ONLINE_PROJECT_ID;
        if (currentProject.isOnlineProject()) {
            projectId = CmsUUID.getOpenCmsUUID(); // HACK: to get an offline project id
        }

        // collect the names of the resources inside the folder, excluding the moved resources
        I_CmsVfsDriver vfsDriver = m_driverManager.getVfsDriver(dbc);
        StringBuffer errorResNames = new StringBuffer(128);
        while (childResources.hasNext()) {
            CmsResource errorRes = (CmsResource)childResources.next();
            // if deleting offline, or not moved, or just renamed inside the deleted folder
            // so, it may remain some orphan online entries for moved resources
            // which will be fixed during the publishing of the moved resources
            boolean error = !currentProject.isOnlineProject();
            if (!error) {
                try {
                    String originalPath = vfsDriver.readResource(dbc, projectId, errorRes.getRootPath(), true).getRootPath();
                    error = originalPath.equals(errorRes.getRootPath())
                        || originalPath.startsWith(resource.getRootPath());
                } catch (CmsVfsResourceNotFoundException e) {
                    // ignore
                }
            }
            if (error) {
                if (errorResNames.length() != 0) {
                    errorResNames.append(", ");
                }
                errorResNames.append("[" + dbc.removeSiteRoot(errorRes.getRootPath()) + "]");
            }
        }

        // the current implementation only deletes empty folders
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(errorResNames.toString())) {

            throw new CmsVfsException(Messages.get().container(
                Messages.ERR_DELETE_NONEMTY_FOLDER_2,
                dbc.removeSiteRoot(resource.getRootPath()),
                errorResNames.toString()));
        }
        internalRemoveFolder(dbc, currentProject, resource);

        // remove project resources
        String deletedResourceRootPath = resource.getRootPath();
        if (dbc.getRequestContext() != null) {
            dbc.getRequestContext().setAttribute(CmsProjectDriver.DBC_ATTR_READ_PROJECT_FOR_RESOURCE, Boolean.TRUE);
            I_CmsProjectDriver projectDriver = m_driverManager.getProjectDriver(dbc);
            Iterator itProjects = projectDriver.readProjects(dbc, deletedResourceRootPath).iterator();
            while (itProjects.hasNext()) {
                CmsProject project = (CmsProject)itProjects.next();
                projectDriver.deleteProjectResource(dbc, project.getUuid(), deletedResourceRootPath);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#replaceResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, byte[], int)
     */
    public void replaceResource(CmsDbContext dbc, CmsResource newResource, byte[] resContent, int newResourceType)
    throws CmsDataAccessException {

        if (resContent == null) {
            // nothing to do
            return;
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            // write the file content
            writeContent(dbc, newResource.getResourceId(), resContent);

            // update the resource record
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject(), "C_RESOURCE_REPLACE");
            stmt.setInt(1, newResourceType);
            stmt.setInt(2, resContent.length);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.setString(4, newResource.getResourceId().toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#setDriverManager(org.opencms.db.CmsDriverManager)
     */
    public void setDriverManager(CmsDriverManager driverManager) {

        m_driverManager = driverManager;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#setSqlManager(org.opencms.db.CmsSqlManager)
     */
    public void setSqlManager(org.opencms.db.CmsSqlManager sqlManager) {

        m_sqlManager = (CmsSqlManager)sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#transferResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public void transferResource(
        CmsDbContext dbc,
        CmsProject project,
        CmsResource resource,
        CmsUUID createdUser,
        CmsUUID lastModifiedUser) throws CmsDataAccessException {

        if (createdUser == null) {
            createdUser = resource.getUserCreated();
        }
        if (lastModifiedUser == null) {
            lastModifiedUser = resource.getUserLastModified();
        }

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_TRANSFER_RESOURCE");
            stmt.setString(1, createdUser.toString());
            stmt.setString(2, lastModifiedUser.toString());
            stmt.setString(3, resource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#updateRelations(CmsDbContext, CmsProject, CmsResource)
     */
    public void updateRelations(CmsDbContext dbc, CmsProject onlineProject, CmsResource offlineResource)
    throws CmsDataAccessException {

        // delete online relations
        I_CmsVfsDriver vfsDriver = m_driverManager.getVfsDriver(dbc);
        vfsDriver.deleteRelations(dbc, onlineProject.getUuid(), offlineResource, CmsRelationFilter.TARGETS);

        CmsUUID projectId;
        if (!dbc.getProjectId().isNullUUID()) {
            projectId = CmsProject.ONLINE_PROJECT_ID;
        } else {
            projectId = dbc.currentProject().getUuid();
        }

        // copy offline to online relations
        CmsUUID dbcProjectId = dbc.getProjectId();
        dbc.setProjectId(CmsUUID.getNullUUID());
        Iterator itRelations = m_driverManager.getVfsDriver(dbc).readRelations(
            dbc,
            projectId,
            offlineResource,
            CmsRelationFilter.TARGETS).iterator();
        dbc.setProjectId(dbcProjectId);
        while (itRelations.hasNext()) {
            vfsDriver.createRelation(dbc, onlineProject.getUuid(), (CmsRelation)itRelations.next());
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#validateResourceIdExists(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID)
     */
    public boolean validateResourceIdExists(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean exists = false;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_RESOURCE_STATE");
            stmt.setString(1, resourceId.toString());

            res = stmt.executeQuery();
            exists = res.next();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return exists;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#validateStructureIdExists(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID)
     */
    public boolean validateStructureIdExists(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean found = false;
        int count = 0;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_SELECT_STRUCTURE_ID");
            stmt.setString(1, structureId.toString());

            res = stmt.executeQuery();
            if (res.next()) {
                count = res.getInt(1);
                found = (count == 1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                found = false;
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return found;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeContent(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, byte[])
     */
    public void writeContent(CmsDbContext dbc, CmsUUID resourceId, byte[] content) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject(), "C_OFFLINE_CONTENTS_UPDATE");
            // update the file content in the database.
            if (content.length < 2000) {
                stmt.setBytes(1, content);
            } else {
                stmt.setBinaryStream(1, new ByteArrayInputStream(content), content.length);
            }
            stmt.setString(2, resourceId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeLastModifiedProjectId(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, CmsUUID, org.opencms.file.CmsResource)
     */
    public void writeLastModifiedProjectId(CmsDbContext dbc, CmsProject project, CmsUUID projectId, CmsResource resource)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_PROJECT_LASTMODIFIED");
            stmt.setString(1, projectId.toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writePropertyObject(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.file.CmsProperty)
     */
    public void writePropertyObject(CmsDbContext dbc, CmsProject project, CmsResource resource, CmsProperty property)
    throws CmsDataAccessException {

        CmsUUID projectId = ((dbc.getProjectId() == null) || dbc.getProjectId().isNullUUID())
        ? project.getUuid()
        : dbc.getProjectId();

        // TODO: check if we need autocreation for link property definition types too
        CmsPropertyDefinition propertyDefinition = null;
        try {
            // read the property definition
            propertyDefinition = readPropertyDefinition(dbc, property.getName(), projectId);
        } catch (CmsDbEntryNotFoundException e) {
            if (property.autoCreatePropertyDefinition()) {
                propertyDefinition = createPropertyDefinition(
                    dbc,
                    projectId,
                    property.getName(),
                    CmsPropertyDefinition.TYPE_NORMAL);
                try {
                    readPropertyDefinition(dbc, property.getName(), CmsProject.ONLINE_PROJECT_ID);
                } catch (CmsDataAccessException e1) {
                    createPropertyDefinition(
                        dbc,
                        CmsProject.ONLINE_PROJECT_ID,
                        property.getName(),
                        CmsPropertyDefinition.TYPE_NORMAL);
                }
                try {
                    m_driverManager.getHistoryDriver(dbc).readPropertyDefinition(dbc, property.getName());
                } catch (CmsDataAccessException e1) {
                    m_driverManager.getHistoryDriver(dbc).createPropertyDefinition(
                        dbc,
                        property.getName(),
                        CmsPropertyDefinition.TYPE_NORMAL);
                }
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_PROPERTY_DEFINITION_CREATED,
                    Collections.<String, Object> singletonMap("propertyDefinition", propertyDefinition)));

            } else {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_NO_PROPERTYDEF_WITH_NAME_1,
                    property.getName()));
            }
        }

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // read the existing property to test if we need the 
            // insert or update query to write a property value
            CmsProperty existingProperty = readPropertyObject(dbc, propertyDefinition.getName(), project, resource);

            if (existingProperty.isIdentical(property)) {
                // property already has the identical values set, no write required
                return;
            }

            conn = m_sqlManager.getConnection(dbc);

            for (int i = 0; i < 2; i++) {
                int mappingType = -1;
                String value = null;
                CmsUUID id = null;
                boolean existsPropertyValue = false;
                boolean deletePropertyValue = false;

                // 1) take any required decisions to choose and fill the correct SQL query

                if (i == 0) {
                    // write/delete the *structure value* on the first cycle
                    if ((existingProperty.getStructureValue() != null) && property.isDeleteStructureValue()) {
                        // this property value is marked to be deleted
                        deletePropertyValue = true;
                    } else {
                        value = property.getStructureValue();
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                            // no structure value set or the structure value is an empty string, 
                            // continue with the resource value
                            continue;
                        }
                    }

                    // set the vars to be written to the database
                    mappingType = CmsProperty.STRUCTURE_RECORD_MAPPING;
                    id = resource.getStructureId();
                    existsPropertyValue = existingProperty.getStructureValue() != null;
                } else {
                    // write/delete the *resource value* on the second cycle
                    if ((existingProperty.getResourceValue() != null) && property.isDeleteResourceValue()) {
                        // this property value is marked to be deleted
                        deletePropertyValue = true;
                    } else {
                        value = property.getResourceValue();
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                            // no resource value set or the resource value is an empty string,
                            // break out of the loop
                            break;
                        }
                    }

                    // set the vars to be written to the database
                    mappingType = CmsProperty.RESOURCE_RECORD_MAPPING;
                    id = resource.getResourceId();
                    existsPropertyValue = existingProperty.getResourceValue() != null;
                }

                // 2) execute the SQL query
                try {
                    if (!deletePropertyValue) {
                        // insert/update the property value                    
                        if (existsPropertyValue) {
                            // {structure|resource} property value already exists- use update statement
                            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_UPDATE");
                            stmt.setString(1, m_sqlManager.validateEmpty(value));
                            stmt.setString(2, id.toString());
                            stmt.setInt(3, mappingType);
                            stmt.setString(4, propertyDefinition.getId().toString());
                        } else {
                            // {structure|resource} property value doesn't exist- use create statement
                            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_CREATE");
                            stmt.setString(1, new CmsUUID().toString());
                            stmt.setString(2, propertyDefinition.getId().toString());
                            stmt.setString(3, id.toString());
                            stmt.setInt(4, mappingType);
                            stmt.setString(5, m_sqlManager.validateEmpty(value));
                        }
                    } else {
                        // {structure|resource} property value marked as deleted- use delete statement
                        stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_DELETE");
                        stmt.setString(1, propertyDefinition.getId().toString());
                        stmt.setString(2, id.toString());
                        stmt.setInt(3, mappingType);
                    }
                    stmt.executeUpdate();
                } finally {
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writePropertyObjects(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.List)
     */
    public void writePropertyObjects(CmsDbContext dbc, CmsProject project, CmsResource resource, List properties)
    throws CmsDataAccessException {

        CmsProperty property = null;

        for (int i = 0; i < properties.size(); i++) {
            property = (CmsProperty)properties.get(i);
            writePropertyObject(dbc, project, resource, property);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResource(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsResource, int)
     */
    public void writeResource(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, int changed)
    throws CmsDataAccessException {

        // validate the resource length
        internalValidateResourceLength(resource);

        String resourcePath = CmsFileUtil.removeTrailingSeparator(resource.getRootPath());

        // this task is split into two statements because some DBs (e.g. Oracle) doesn't support multi-table updates
        PreparedStatement stmt = null;
        Connection conn = null;
        long resourceDateModified;

        if (resource.isTouched()) {
            resourceDateModified = resource.getDateLastModified();
        } else {
            resourceDateModified = System.currentTimeMillis();
        }

        CmsResourceState structureState = resource.getState();
        CmsResourceState resourceState = resource.getState();
        CmsResourceState structureStateOld = internalReadStructureState(dbc, projectId, resource);
        CmsResourceState resourceStateOld = internalReadResourceState(dbc, projectId, resource);
        CmsUUID projectLastModified = projectId;

        if (changed == CmsDriverManager.UPDATE_RESOURCE_STATE) {
            resourceState = resourceStateOld;
            resourceState = (resourceState.isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
            structureState = structureStateOld;
        } else if (changed == CmsDriverManager.UPDATE_STRUCTURE_STATE) {
            structureState = structureStateOld;
            structureState = (structureState.isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
        } else if (changed == CmsDriverManager.NOTHING_CHANGED) {
            projectLastModified = resource.getProjectLastModified();
        } else {
            resourceState = resourceStateOld;
            resourceState = (resourceState.isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
            structureState = structureStateOld;
            structureState = (structureState.isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
        }

        try {

            // read the parent id
            String parentId = internalReadParentId(dbc, projectId, resourcePath);
            int sibCount = countSiblings(dbc, projectId, resource.getResourceId());

            conn = m_sqlManager.getConnection(dbc);

            if (changed != CmsDriverManager.UPDATE_STRUCTURE_STATE) {
                // if the resource was unchanged
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_UPDATE_RESOURCES");
                stmt.setInt(1, resource.getTypeId());
                stmt.setInt(2, resource.getFlags());
                stmt.setLong(3, resourceDateModified);
                stmt.setString(4, resource.getUserLastModified().toString());
                stmt.setInt(5, resourceState.getState());
                stmt.setInt(6, resource.getLength());
                stmt.setLong(7, resource.getDateContent());
                stmt.setString(8, projectLastModified.toString());
                stmt.setInt(9, sibCount);
                stmt.setString(10, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_UPDATE_RESOURCES_WITHOUT_STATE");
                stmt.setInt(1, resource.getTypeId());
                stmt.setInt(2, resource.getFlags());
                stmt.setLong(3, resourceDateModified);
                stmt.setString(4, resource.getUserLastModified().toString());
                stmt.setInt(5, resource.getLength());
                stmt.setLong(6, resource.getDateContent());
                stmt.setString(7, projectLastModified.toString());
                stmt.setInt(8, sibCount);
                stmt.setString(9, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }

            // update the structure
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_UPDATE_STRUCTURE");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.setString(2, resourcePath);
            stmt.setInt(3, structureState.getState());
            stmt.setLong(4, resource.getDateReleased());
            stmt.setLong(5, resource.getDateExpired());
            stmt.setString(6, parentId);
            stmt.setString(7, resource.getStructureId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResourceState(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, int, boolean)
     */
    public void writeResourceState(
        CmsDbContext dbc,
        CmsProject project,
        CmsResource resource,
        int changed,
        boolean isPublishing) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        if (project.getUuid().equals(CmsProject.ONLINE_PROJECT_ID)) {
            return;
        }

        try {
            conn = m_sqlManager.getConnection(dbc);

            if (changed == CmsDriverManager.UPDATE_RESOURCE_PROJECT) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCE_PROJECT");
                stmt.setInt(1, resource.getFlags());
                stmt.setString(2, project.getUuid().toString());
                stmt.setString(3, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }

            if (changed == CmsDriverManager.UPDATE_RESOURCE) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCE_STATELASTMODIFIED");
                stmt.setInt(1, resource.getState().getState());
                stmt.setLong(2, resource.getDateLastModified());
                stmt.setString(3, resource.getUserLastModified().toString());
                stmt.setString(4, project.getUuid().toString());
                stmt.setString(5, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }

            if ((changed == CmsDriverManager.UPDATE_RESOURCE_STATE) || (changed == CmsDriverManager.UPDATE_ALL)) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCE_STATE");
                stmt.setInt(1, resource.getState().getState());
                stmt.setString(2, project.getUuid().toString());
                stmt.setString(3, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }

            if ((changed == CmsDriverManager.UPDATE_STRUCTURE)
                || (changed == CmsDriverManager.UPDATE_ALL)
                || (changed == CmsDriverManager.UPDATE_STRUCTURE_STATE)) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_STRUCTURE_STATE");
                stmt.setInt(1, resource.getState().getState());
                stmt.setString(2, resource.getStructureId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }

            if ((changed == CmsDriverManager.UPDATE_STRUCTURE) || (changed == CmsDriverManager.UPDATE_ALL)) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RELEASE_EXPIRED");
                stmt.setLong(1, resource.getDateReleased());
                stmt.setLong(2, resource.getDateExpired());
                stmt.setString(3, resource.getStructureId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        if (isPublishing) {
            internalUpdateVersions(dbc, resource);
        }
    }

    /**
     * Checks that the current user has write permissions for all subresources of the given folder.<p>
     * 
     * @param dbc the current database context
     * @param folder the folder to check
     * 
     * @throws CmsDataAccessException if something goes wrong 
     */
    protected void checkWritePermissionsInFolder(CmsDbContext dbc, CmsResource folder) throws CmsDataAccessException {

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        CmsUUID projectId = dbc.getRequestContext().getCurrentProject().getUuid();

        // first read all subresources with ACEs
        List resources = new ArrayList();
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_WITH_ACE_1");
            stmt.setString(1, folder.getRootPath() + "%");
            res = stmt.executeQuery();

            while (res.next()) {
                resources.add(createResource(res, projectId));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        // check current user write permission for each of these resources
        Iterator itResources = resources.iterator();
        while (itResources.hasNext()) {
            CmsResource resource = (CmsResource)itResources.next();
            try {
                m_driverManager.getSecurityManager().checkPermissions(
                    dbc.getRequestContext(),
                    resource,
                    CmsPermissionSet.ACCESS_WRITE,
                    false,
                    CmsResourceFilter.ALL);
            } catch (CmsException e) {
                throw new CmsDataAccessException(e.getMessageContainer(), e);
            }
        }

        // then check for possible jsp pages without permissions
        CmsResourceFilter filter = CmsResourceFilter.ALL;
        itResources = readTypesInResourceTree(
            dbc,
            projectId,
            folder.getRootPath(),
            CmsResourceTypeJsp.getJspResourceTypeIds(),
            filter.getState(),
            filter.getModifiedAfter(),
            filter.getModifiedBefore(),
            filter.getReleaseAfter(),
            filter.getReleaseBefore(),
            filter.getExpireAfter(),
            filter.getExpireBefore(),
            CmsDriverManager.READMODE_INCLUDE_TREE).iterator();
        while (itResources.hasNext()) {
            CmsResource resource = (CmsResource)itResources.next();
            try {
                m_driverManager.getSecurityManager().checkPermissions(
                    dbc.getRequestContext(),
                    resource,
                    CmsPermissionSet.ACCESS_WRITE,
                    false,
                    CmsResourceFilter.ALL);
            } catch (CmsException e) {
                throw new CmsDataAccessException(e.getMessageContainer(), e);
            }
        }
    }

    /**
     * Returns the count of properties for a property definition.<p>
     * 
     * @param dbc the current database context
     * @param propertyDefinition the property definition to test
     * @param projectId the ID of the current project
     * 
     * @return the amount of properties for a property definition
     * @throws CmsDataAccessException if something goes wrong
     */
    protected int internalCountProperties(CmsDbContext dbc, CmsPropertyDefinition propertyDefinition, CmsUUID projectId)
    throws CmsDataAccessException {

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        int count = 0;

        try {
            // create statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_READALL_COUNT");
            stmt.setString(1, propertyDefinition.getId().toString());
            res = stmt.executeQuery();

            if (res.next()) {
                count = res.getInt(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsDbConsistencyException(Messages.get().container(
                    Messages.ERR_COUNTING_PROPERTIES_1,
                    propertyDefinition.getName()));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return count;
    }

    /**
     * Creates a new counter.<p>
     * 
     * @param dbc the database context 
     * @param name the name of the counter to create 
     * @param value the inital value of the counter  
     * 
     * @throws CmsDbSqlException if something goes wrong 
     */
    protected void internalCreateCounter(CmsDbContext dbc, String name, int value) throws CmsDbSqlException {

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, CmsProject.ONLINE_PROJECT_ID, "C_CREATE_COUNTER");
            stmt.setString(1, name);
            stmt.setInt(2, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw wrapException(stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * Creates an URL name mapping entry from a result set.<p>
     * 
     * @param resultSet a result set 
     * @return the URL name mapping entry created from the result set 
     * 
     * @throws SQLException if something goes wrong 
     */
    protected CmsUrlNameMappingEntry internalCreateUrlNameMappingEntry(ResultSet resultSet) throws SQLException {

        String name = resultSet.getString(1);
        CmsUUID structureId = new CmsUUID(resultSet.getString(2));
        int state = resultSet.getInt(3);
        long dateChanged = resultSet.getLong(4);
        String locale = resultSet.getString(5);
        return new CmsUrlNameMappingEntry(name, structureId, state, dateChanged, locale);
    }

    /**
     * Increments a counter.<p>
     *  
     * @param dbc the current db context 
     * @param name the name of the counter which should be incremented  
     * 
     * @throws CmsDbSqlException if something goes wrong 
     */
    protected void internalIncrementCounter(CmsDbContext dbc, String name) throws CmsDbSqlException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet resultSet = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, CmsProject.ONLINE_PROJECT_ID, "C_INCREMENT_COUNTER");
            stmt.setString(1, name);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw wrapException(stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, resultSet);
        }
    }

    /**
     * Reads the current value of a counter.<p>
     * 
     * @param dbc the database context 
     * @param name the name of the counter 
     * @return the current value of the  counter, or null if the counter was not found 
     * 
     * @throws CmsDbSqlException if something goes wrong
     */
    protected Integer internalReadCounter(CmsDbContext dbc, String name) throws CmsDbSqlException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet resultSet = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, CmsProject.ONLINE_PROJECT_ID, "C_READ_COUNTER");
            stmt.setString(1, name);
            resultSet = stmt.executeQuery();
            Integer result = null;
            if (resultSet.next()) {
                int counter = resultSet.getInt(1);
                result = new Integer(counter);
                while (resultSet.next()) {
                    // for MSSQL 
                }
            }
            return result;
        } catch (SQLException e) {
            throw wrapException(stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, resultSet);
        }
    }

    /**
     * Returns the parent id of the given resource.<p>
     * 
     * @param dbc the current database context
     * @param projectId the current project id 
     * @param resourcename the resource name to read the parent id for
     * 
     * @return  the parent id of the given resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected String internalReadParentId(CmsDbContext dbc, CmsUUID projectId, String resourcename)
    throws CmsDataAccessException {

        if ("/".equalsIgnoreCase(resourcename)) {
            return CmsUUID.getNullUUID().toString();
        }

        String parent = CmsResource.getParentFolder(resourcename);
        parent = CmsFileUtil.removeTrailingSeparator(parent);

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String parentId = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_PARENT_STRUCTURE_ID");
            stmt.setString(1, parent);
            res = stmt.executeQuery();

            if (res.next()) {
                parentId = res.getString(1);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_PARENT_ID_1,
                    dbc.removeSiteRoot(resourcename)));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return parentId;
    }

    /**
     * Creates a new {@link CmsRelation} object from the given result set entry.<p>
     * 
     * @param res the result set 
     *  
     * @return the new {@link CmsRelation} object
     * 
     * @throws SQLException if something goes wrong
     */
    protected CmsRelation internalReadRelation(ResultSet res) throws SQLException {

        CmsUUID sourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RELATION_SOURCE_ID")));
        String sourcePath = res.getString(m_sqlManager.readQuery("C_RELATION_SOURCE_PATH"));
        CmsUUID targetId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RELATION_TARGET_ID")));
        String targetPath = res.getString(m_sqlManager.readQuery("C_RELATION_TARGET_PATH"));
        int type = res.getInt(m_sqlManager.readQuery("C_RELATION_TYPE"));
        return new CmsRelation(sourceId, sourcePath, targetId, targetPath, CmsRelationType.valueOf(type));
    }

    /**
     * Returns the resource state of the given resource.<p>
     * 
     * @param dbc the database context
     * @param projectId the id of the project
     * @param resource the resource to read the resource state for
     * 
     * @return the resource state of the given resource
     * 
     * @throws CmsDbSqlException if something goes wrong
     */
    protected CmsResourceState internalReadResourceState(CmsDbContext dbc, CmsUUID projectId, CmsResource resource)
    throws CmsDbSqlException {

        CmsResourceState state = CmsResource.STATE_KEEP;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_READ_RESOURCE_STATE");
            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                state = CmsResourceState.valueOf(res.getInt(m_sqlManager.readQuery("C_RESOURCES_STATE")));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return state;
    }

    /**
     * Returns the structure state of the given resource.<p>
     * 
     * @param dbc the database context
     * @param projectId the id of the project
     * @param resource the resource to read the structure state for
     * 
     * @return the structure state of the given resource
     * 
     * @throws CmsDbSqlException if something goes wrong
     */
    protected CmsResourceState internalReadStructureState(CmsDbContext dbc, CmsUUID projectId, CmsResource resource)
    throws CmsDbSqlException {

        CmsResourceState state = CmsResource.STATE_KEEP;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_READ_STRUCTURE_STATE");
            stmt.setString(1, resource.getStructureId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                state = CmsResourceState.valueOf(res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_STATE")));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return state;
    }

    /**
     * Removes a resource physically in the database.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param resource the folder to remove
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalRemoveFolder(CmsDbContext dbc, CmsProject currentProject, CmsResource resource)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            // delete the structure record            
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_STRUCTURE_DELETE_BY_STRUCTUREID");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(dbc, null, stmt, null);

            // delete the resource record
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCES_DELETE_BY_RESOURCEID");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * Updates the offline version numbers.<p>
     *  
     * @param dbc the current database context
     * @param resource the resource to update the version number for
     * 
     * @throws CmsDataAccessException if something goes wrong 
     */
    protected void internalUpdateVersions(CmsDbContext dbc, CmsResource resource) throws CmsDataAccessException {

        if (dbc.getRequestContext() == null) {
            // no needed during initialization 
            return;
        }
        if (dbc.currentProject().isOnlineProject()) {
            // this method is supposed to be used only in the offline project
            return;
        }

        // read the online version numbers
        Map onlineVersions = readVersions(
            dbc,
            CmsProject.ONLINE_PROJECT_ID,
            resource.getResourceId(),
            resource.getStructureId());
        int onlineStructureVersion = ((Integer)onlineVersions.get("structure")).intValue();
        int onlineResourceVersion = ((Integer)onlineVersions.get("resource")).intValue();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            // update the resource version
            stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject(), "C_RESOURCES_UPDATE_RESOURCE_VERSION");
            stmt.setInt(1, onlineResourceVersion);
            stmt.setString(2, resource.getResourceId().toString());
            stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, null, stmt, null);

            // update the structure version
            stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject(), "C_RESOURCES_UPDATE_STRUCTURE_VERSION");
            stmt.setInt(1, onlineStructureVersion);
            stmt.setString(2, resource.getStructureId().toString());
            stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, null, stmt, null);

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * Validates that the length setting of a resource is always correct.<p>
     * 
     * Files need to have a resource length of >= 0, while folders require
     * a resource length of -1.<p>
     * 
     * @param resource the resource to check the length for
     * @throws CmsDataAccessException if the resource length is not correct
     */
    protected void internalValidateResourceLength(CmsResource resource) throws CmsDataAccessException {

        if (resource.isFolder() && (resource.getLength() == -1)) {
            return;
        }

        if (resource.isFile() && (resource.getLength() >= 0)) {
            return;
        }

        throw new CmsDataAccessException(Messages.get().container(
            Messages.ERR_INVALID_RESOURCE_LENGTH_2,
            new Integer(resource.getLength()),
            resource.getRootPath()));
    }

    /**
     * Moves all relations of a resource to the new path.<p>
     * 
     * @param dbc the current database context
     * @param projectId the id of the project to apply the changes 
     * @param structureId the structure id of the resource to apply the changes to
     * @param rootPath the new root path
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void moveRelations(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId, String rootPath)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_MOVE_RELATIONS_SOURCE");
            stmt.setString(1, rootPath);
            stmt.setString(2, structureId.toString());

            stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, null, stmt, null);

            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_MOVE_RELATIONS_TARGET");
            stmt.setString(1, rootPath);
            stmt.setString(2, structureId.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * Appends the appropriate selection criteria related with the expiration date.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param startTime the start time
     * @param endTime the end time
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareExpiredTimeRangeCondition(
        CmsUUID projectId,
        long startTime,
        long endTime,
        StringBuffer conditions,
        List params) {

        if (startTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match expired date against startTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_STRUCTURE_SELECT_BY_DATE_EXPIRED_AFTER"));
            conditions.append(END_CONDITION);
            params.add(new Long(startTime));
        }

        if (endTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match expired date against endTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_STRUCTURE_SELECT_BY_DATE_EXPIRED_BEFORE"));
            conditions.append(END_CONDITION);
            params.add(new Long(endTime));
        }
    }

    /**
     * Appends the appropriate selection criteria related with the parentPath.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param parent the parent path or UUID (if mode is C_READMODE_EXCLUDE_TREE)
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void preparePathCondition(CmsUUID projectId, String parent, int mode, StringBuffer conditions, List params) {

        if (parent == CmsDriverManager.READ_IGNORE_PARENT) {
            // parent can be ignored
            return;
        }

        if ((mode & CmsDriverManager.READMODE_EXCLUDE_TREE) > 0) {
            // only return immediate children - use UUID optimization            
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_PARENT_UUID"));
            conditions.append(END_CONDITION);
            params.add(parent);
            return;
        }

        if ("/".equalsIgnoreCase(parent)) {
            // if root folder is parent, no additional condition is needed since all resources match anyway
            return;
        }

        // add condition to read path subtree        
        conditions.append(BEGIN_INCLUDE_CONDITION);
        conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_PATH_PREFIX"));
        conditions.append(END_CONDITION);
        params.add(CmsFileUtil.addTrailingSeparator(escapeDbWildcard(parent)) + "%");
    }

    /**
     * Appends the appropriate selection criteria related with the projectId.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareProjectCondition(CmsUUID projectId, int mode, StringBuffer conditions, List params) {

        if ((mode & CmsDriverManager.READMODE_INCLUDE_PROJECT) > 0) {
            // C_READMODE_INCLUDE_PROJECT: add condition to match the PROJECT_ID
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_PROJECT_LASTMODIFIED"));
            conditions.append(END_CONDITION);
            params.add(projectId.toString());
        }
    }

    /**
     * Build the whole WHERE sql statement part for the given relation filter.<p>
     * 
     * @param projectId the current project id
     * @param filter the filter
     * @param resource the resource (may be null, if you want to delete all relations for the resource in the filter)
     * @param params the parameter values (return parameter)
     * @param checkSource if the query is for the source relations 
     * 
     * @return the WHERE sql statement part string
     */
    protected String prepareRelationConditions(
        CmsUUID projectId,
        CmsRelationFilter filter,
        CmsResource resource,
        List params,
        boolean checkSource) {

        StringBuffer conditions = new StringBuffer(128);
        params.clear(); // be sure the parameters list is clear

        // source or target filter
        if (filter.isSource() || filter.isTarget()) {
            // source or target id filter from resource
            if (resource != null) {
                conditions.append(BEGIN_CONDITION);
                if (filter.isSource() && checkSource) {
                    if (!filter.isIncludeSubresources()) {
                        conditions.append(m_sqlManager.readQuery(projectId, "C_RELATION_FILTER_TARGET_ID"));
                        params.add(resource.getStructureId().toString());
                    } else {
                        conditions.append(m_sqlManager.readQuery(projectId, "C_RELATION_FILTER_TARGET_PATH"));
                        params.add(resource.getRootPath() + '%');
                    }
                } else if (filter.isTarget() && !checkSource) {
                    if (!filter.isIncludeSubresources()) {
                        conditions.append(m_sqlManager.readQuery(projectId, "C_RELATION_FILTER_SOURCE_ID"));
                        params.add(resource.getStructureId().toString());
                    } else {
                        conditions.append(m_sqlManager.readQuery(projectId, "C_RELATION_FILTER_SOURCE_PATH"));
                        params.add(resource.getRootPath() + '%');
                    }
                }
                conditions.append(END_CONDITION);
            }

            // target or source id filter from filter parameter
            if (filter.getStructureId() != null) {
                if (conditions.length() == 0) {
                    conditions.append(BEGIN_CONDITION);
                } else {
                    conditions.append(BEGIN_INCLUDE_CONDITION);
                }

                if (filter.isSource() && checkSource) {
                    conditions.append(m_sqlManager.readQuery(projectId, "C_RELATION_FILTER_SOURCE_ID"));
                    params.add(filter.getStructureId().toString());
                } else if (filter.isTarget() && !checkSource) {
                    conditions.append(m_sqlManager.readQuery(projectId, "C_RELATION_FILTER_TARGET_ID"));
                    params.add(filter.getStructureId().toString());
                }
                conditions.append(END_CONDITION);
            }

            // target or source path filter from filter parameter
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(filter.getPath())) {
                if (conditions.length() == 0) {
                    conditions.append(BEGIN_CONDITION);
                } else {
                    conditions.append(BEGIN_INCLUDE_CONDITION);
                }

                String queryPath = filter.getPath();
                if (filter.isIncludeSubresources()) {
                    queryPath += '%';
                }
                if (filter.isSource() && checkSource) {
                    conditions.append(m_sqlManager.readQuery(projectId, "C_RELATION_FILTER_SOURCE_PATH"));
                    params.add(queryPath);
                } else if (filter.isTarget() && !checkSource) {
                    conditions.append(m_sqlManager.readQuery(projectId, "C_RELATION_FILTER_TARGET_PATH"));
                    params.add(queryPath);
                }
                conditions.append(END_CONDITION);
            }
        }

        // relation type filter
        Set types = filter.getTypes();
        if (!types.isEmpty()) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(projectId, "C_RELATION_FILTER_TYPE"));
            conditions.append(BEGIN_CONDITION);
            Iterator it = types.iterator();
            while (it.hasNext()) {
                CmsRelationType type = (CmsRelationType)it.next();
                conditions.append("?");
                params.add(new Integer(type.getId()));
                if (it.hasNext()) {
                    conditions.append(", ");
                }
            }
            conditions.append(END_CONDITION);
            conditions.append(END_CONDITION);
        }
        return conditions.toString();
    }

    /**
     * Appends the appropriate selection criteria related with the released date.<p>
     * 
     * @param projectId the id of the project
     * @param startTime the start time
     * @param endTime the stop time
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareReleasedTimeRangeCondition(
        CmsUUID projectId,
        long startTime,
        long endTime,
        StringBuffer conditions,
        List params) {

        if (startTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match released date against startTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_STRUCTURE_SELECT_BY_DATE_RELEASED_AFTER"));
            conditions.append(END_CONDITION);
            params.add(new Long(startTime));
        }

        if (endTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match released date against endTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_STRUCTURE_SELECT_BY_DATE_RELEASED_BEFORE"));
            conditions.append(END_CONDITION);
            params.add(new Long(endTime));
        }
    }

    /**
     * Appends the appropriate selection criteria related with the read mode.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     */
    protected void prepareResourceCondition(CmsUUID projectId, int mode, StringBuffer conditions) {

        if ((mode & CmsDriverManager.READMODE_ONLY_FOLDERS) > 0) {
            // C_READMODE_ONLY_FOLDERS: add condition to match only folders
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_ONLY_FOLDERS"));
            conditions.append(END_CONDITION);
        } else if ((mode & CmsDriverManager.READMODE_ONLY_FILES) > 0) {
            // C_READMODE_ONLY_FILES: add condition to match only files
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_ONLY_FILES"));
            conditions.append(END_CONDITION);
        }
    }

    /**
     * Appends the appropriate selection criteria related with the resource state.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param state the resource state
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareStateCondition(
        CmsUUID projectId,
        CmsResourceState state,
        int mode,
        StringBuffer conditions,
        List params) {

        if (state != null) {
            if ((mode & CmsDriverManager.READMODE_EXCLUDE_STATE) > 0) {
                // C_READ_MODIFIED_STATES: add condition to match against any state but not given state
                conditions.append(BEGIN_EXCLUDE_CONDITION);
            } else {
                // otherwise add condition to match against given state if necessary
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_STATE"));
            conditions.append(END_CONDITION);
            params.add(new Integer(state.getState()));
            params.add(new Integer(state.getState()));
        }
    }

    /**
     * Appends the appropriate selection criteria related with the date of the last modification.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param startTime start of the time range
     * @param endTime end of the time range
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareTimeRangeCondition(
        CmsUUID projectId,
        long startTime,
        long endTime,
        StringBuffer conditions,
        List params) {

        if (startTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match last modified date against startTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_DATE_LASTMODIFIED_AFTER"));
            conditions.append(END_CONDITION);
            params.add(new Long(startTime));
        }

        if (endTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match last modified date against endTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_DATE_LASTMODIFIED_BEFORE"));
            conditions.append(END_CONDITION);
            params.add(new Long(endTime));
        }
    }

    /**
     * Appends the appropriate selection criteria related with the resource type.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param type the resource type
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareTypeCondition(CmsUUID projectId, int type, int mode, StringBuffer conditions, List params) {

        if (type != CmsDriverManager.READ_IGNORE_TYPE) {
            if ((mode & CmsDriverManager.READMODE_EXCLUDE_TYPE) > 0) {
                // C_READ_FILE_TYPES: add condition to match against any type, but not given type
                conditions.append(BEGIN_EXCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_TYPE"));
                conditions.append(END_CONDITION);
                params.add(new Integer(type));
            } else {
                //otherwise add condition to match against given type if necessary
                conditions.append(BEGIN_INCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_TYPE"));
                conditions.append(END_CONDITION);
                params.add(new Integer(type));
            }
        }
    }

    /**
     * Appends the appropriate selection criteria related with the resource type.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param types the resource type id's
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareTypesCondition(
        CmsUUID projectId,
        List<Integer> types,
        int mode,
        StringBuffer conditions,
        List params) {

        if ((types == null) || types.isEmpty()) {
            if ((mode & CmsDriverManager.READMODE_EXCLUDE_TYPE) > 0) {
                // C_READ_FILE_TYPES: add condition to match against any type, but not given type
                conditions.append(BEGIN_EXCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_TYPE"));
                conditions.append(END_CONDITION);
                params.add(new Integer(CmsDriverManager.READ_IGNORE_TYPE));
            } else {
                //otherwise add condition to match against given type if necessary
                conditions.append(BEGIN_INCLUDE_CONDITION);
                Iterator<Integer> typeIt = types.iterator();
                while (typeIt.hasNext()) {
                    conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_TYPE"));
                    params.add(typeIt.next());
                    if (typeIt.hasNext()) {
                        conditions.append(OR_CONDITION);
                    }
                }
                conditions.append(END_CONDITION);
            }
        }
    }

    /**
     * Reads all resources inside a given project matching the criteria specified by parameter values.<p>
     * 
     * Important: If {@link CmsDriverManager#READMODE_EXCLUDE_TREE} is true (or {@link CmsDriverManager#READMODE_INCLUDE_TREE} is false), 
     * the provided parent String must be the UUID of the parent folder, NOT the parent folder path.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project id for matching resources
     * @param parent the path to the resource used as root of the searched subtree or {@link CmsDriverManager#READ_IGNORE_PARENT}, 
     *               {@link CmsDriverManager#READMODE_EXCLUDE_TREE} means to read immediate children only 
     * @param types the resource types of matching resources or <code>null</code> (meaning inverted by {@link CmsDriverManager#READMODE_EXCLUDE_TYPE}
     * @param state the state of matching resources (meaning inverted by {@link CmsDriverManager#READMODE_EXCLUDE_STATE} or <code>null</code> to ignore
     * @param startTime the start of the time range for the last modification date of matching resources or READ_IGNORE_TIME 
     * @param endTime the end of the time range for the last modification date of matching resources or READ_IGNORE_TIME
     * @param releasedAfter the start of the time range for the release date of matching resources
     * @param releasedBefore the end of the time range for the release date of matching resources
     * @param expiredAfter the start of the time range for the expire date of matching resources
     * @param expiredBefore the end of the time range for the expire date of matching resources
     * @param mode additional mode flags:
     * <ul>
     *  <li>{@link CmsDriverManager#READMODE_INCLUDE_TREE}
     *  <li>{@link CmsDriverManager#READMODE_EXCLUDE_TREE}
     *  <li>{@link CmsDriverManager#READMODE_INCLUDE_PROJECT}
     *  <li>{@link CmsDriverManager#READMODE_EXCLUDE_TYPE}
     *  <li>{@link CmsDriverManager#READMODE_EXCLUDE_STATE}
     * </ul>
     * 
     * @return a list of CmsResource objects matching the given criteria
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected List readTypesInResourceTree(
        CmsDbContext dbc,
        CmsUUID projectId,
        String parentPath,
        List<Integer> types,
        CmsResourceState state,
        long lastModifiedAfter,
        long lastModifiedBefore,
        long releasedAfter,
        long releasedBefore,
        long expiredAfter,
        long expiredBefore,
        int mode) throws CmsDataAccessException {

        List result = new ArrayList();

        StringBuffer conditions = new StringBuffer();
        List params = new ArrayList(5);

        // prepare the selection criteria
        prepareProjectCondition(projectId, mode, conditions, params);
        prepareResourceCondition(projectId, mode, conditions);
        prepareTypesCondition(projectId, types, mode, conditions, params);
        prepareTimeRangeCondition(projectId, lastModifiedAfter, lastModifiedBefore, conditions, params);
        prepareReleasedTimeRangeCondition(projectId, releasedAfter, releasedBefore, conditions, params);
        prepareExpiredTimeRangeCondition(projectId, expiredAfter, expiredBefore, conditions, params);
        preparePathCondition(projectId, parentPath, mode, conditions, params);
        prepareStateCondition(projectId, state, mode, conditions, params);

        // now read matching resources within the subtree 
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_READ_TREE"));
            queryBuf.append(conditions);
            queryBuf.append(" ");
            queryBuf.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_ORDER_BY_PATH"));
            stmt = m_sqlManager.getPreparedStatementForSql(conn, queryBuf.toString());

            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Integer) {
                    stmt.setInt(i + 1, ((Integer)params.get(i)).intValue());
                } else if (params.get(i) instanceof Long) {
                    stmt.setLong(i + 1, ((Long)params.get(i)).longValue());
                } else {
                    stmt.setString(i + 1, (String)params.get(i));
                }
            }

            res = stmt.executeQuery();
            while (res.next()) {
                CmsResource resource = createResource(res, projectId);
                result.add(resource);
            }

        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }

    /**
     * Repairs broken links.<p>
     * 
     * When a resource is created any relation pointing to it is updated to use the right id.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project id
     * @param structureId the structure id of the resource that may help to repair broken links
     * @param rootPath the path of the resource that may help to repair broken links
     * 
     * @throws CmsDataAccessException if something goes wrong 
     */
    protected void repairBrokenRelations(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId, String rootPath)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RELATIONS_REPAIR_BROKEN");
            stmt.setString(1, structureId.toString());
            stmt.setString(2, rootPath);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * Updates broken links.<p>
     * 
     * When a resource is deleted, then the relations pointing to 
     * the deleted resource are set to the null uuid.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project id
     * @param rootPath the root path of the resource that has been deleted 
     * 
     * @throws CmsDataAccessException if something goes wrong 
     */
    protected void updateBrokenRelations(CmsDbContext dbc, CmsUUID projectId, String rootPath)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RELATIONS_UPDATE_BROKEN");
            stmt.setString(1, rootPath);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    protected CmsDbSqlException wrapException(PreparedStatement stmt, SQLException e) {

        return new CmsDbSqlException(Messages.get().container(
            Messages.ERR_GENERIC_SQL_1,
            CmsDbSqlException.getErrorQuery(stmt)), e);
    }

    /**
     * Creates a prepared statement by combining a base query with the generated SQL conditions for a given
     * URL name mapping filter.<p>
     *  
     * @param conn the connection to use for creating the prepared statement 
     * @param baseQuery the base query to which the conditions should be appended 
     * @param filter the filter from which to generate the conditions 
     * 
     * @return the created prepared statement 
     * 
     * @throws SQLException if something goes wrong 
     */
    PreparedStatement getPreparedStatementForFilter(Connection conn, String baseQuery, CmsUrlNameMappingFilter filter)
    throws SQLException {

        CmsPair<String, List<I_CmsPreparedStatementParameter>> conditionData = prepareUrlNameMappingConditions(filter);
        String whereClause = "";
        if (!conditionData.getFirst().equals("")) {
            whereClause = " WHERE " + conditionData.getFirst();
        }
        String query = baseQuery + whereClause;
        PreparedStatement stmt = m_sqlManager.getPreparedStatementForSql(conn, query);
        int counter = 1;
        for (I_CmsPreparedStatementParameter param : conditionData.getSecond()) {
            param.insertIntoStatement(stmt, counter);
            counter += 1;
        }
        return stmt;
    }

    /**
     * Replaces the %(PROJECT) macro inside a query with either ONLINE or OFFLINE, depending on the value 
     * of a flag.<p>
     * 
     * We use this instead of the ${PROJECT} replacement mechanism when we need explicit control over the 
     * project, and don't want to implicitly use the project of the DB context.<p>
     * 
     * @param query the query in which the macro should be replaced 
     * @param online if true, the macro will be replaced with "ONLINE", else "OFFLINE"
     * 
     * @return the query with the replaced macro 
     */
    private String replaceProject(String query, boolean online) {

        return query.replace("%(PROJECT)", online ? ONLINE : OFFLINE);
    }

}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsVfsDriver.java,v $
 * Date   : $Date: 2007/05/14 13:10:17 $
 * Version: $Revision: 1.258.4.21 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.db.CmsDbConsistencyException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsResourceState;
import org.opencms.db.CmsVfsOnlineResourceAlreadyExistsException;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsVfsDriver;
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
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Generic (ANSI-SQL) database server implementation of the VFS driver methods.<p>
 * 
 * @author Thomas Weckert 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.258.4.21 $
 * 
 * @since 6.0.0 
 */
public class CmsVfsDriver implements I_CmsDriver, I_CmsVfsDriver {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.generic.CmsVfsDriver.class);

    /** The driver manager. */
    protected CmsDriverManager m_driverManager;

    /** The sql manager. */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

    /**
     * Adds a trailing separator to a path if required.<p>
     * 
     * @param path the path to add the trailing separator to
     * @return the path with a trailing separator
     */
    private static String addTrailingSeparator(String path) {

        int l = path.length();
        if ((l == 0) || (path.charAt(l - 1) != '/')) {
            return path.concat("/");
        } else {
            return path;
        }
    }

    /**
     * Removes a trailing separater from a path if required.<p>
     * 
     * @param path the path to remove the trailing separator from
     * @return the path without a trailing separator
     */
    private static String removeTrailingSeparator(String path) {

        int l = path.length();
        if ((l <= 1) || (path.charAt(l - 1) != '/')) {
            return path;
        } else {
            return path.substring(0, l - 1);
        }
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
            m_sqlManager.closeAll(dbc, null, stmt, null);
        }
        // set the content modification date
        try {
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCE_UPDATE_CONTENT_DATE");
            stmt.setLong(1, System.currentTimeMillis());
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
            resourcePath = addTrailingSeparator(resourcePath);
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
            resourcePath = addTrailingSeparator(resourcePath);
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

        // in case of folder type ensure, that the root path has a trailing slash
        if (CmsFolder.isFolderType(resourceType)) {
            resourcePath = addTrailingSeparator(resourcePath);
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
     * @see org.opencms.db.I_CmsVfsDriver#createOnlineContent(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, byte[], int, boolean)
     */
    public void createOnlineContent(
        CmsDbContext dbc,
        CmsUUID resourceId,
        byte[] contents,
        int publishTag,
        boolean keepOnline) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            // put the online content in the history
            stmt = m_sqlManager.getPreparedStatement(conn, "C_ONLINE_CONTENTS_HISTORY");
            stmt.setString(1, resourceId.toString());
            stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, null, stmt, null);

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
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, null, stmt, null);
        }
        // set the content modification date
        try {
            stmt = m_sqlManager.getPreparedStatement(
                conn,
                CmsProject.ONLINE_PROJECT_ID,
                "C_RESOURCE_UPDATE_CONTENT_DATE");
            stmt.setLong(1, System.currentTimeMillis());
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
            stmt.setLong(5, relation.getDateBegin());
            stmt.setLong(6, relation.getDateEnd());
            stmt.setInt(7, relation.getType().getId());

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
     * @see org.opencms.db.I_CmsVfsDriver#createResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, byte[])
     */
    public CmsResource createResource(CmsDbContext dbc, CmsProject project, CmsResource resource, byte[] content)
    throws CmsDataAccessException {

        CmsUUID newStructureId = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        // check the resource path
        String resourcePath = removeTrailingSeparator(resource.getRootPath());
        if (resourcePath.length() > CmsDriverManager.MAX_VFS_RESOURCE_PATH_LENGTH) {
            throw new CmsDataAccessException(Messages.get().container(
                Messages.ERR_RESOURCENAME_TOO_LONG_2,
                resourcePath,
                new Integer(CmsDriverManager.MAX_VFS_RESOURCE_PATH_LENGTH)));
        }

        // check if the parent folder of the resource exists and if is not deleted
        if (!resource.getRootPath().equals("/")) {
            String parentFolderName = CmsResource.getParentFolder(resource.getRootPath());
            CmsFolder parentFolder = m_driverManager.getVfsDriver().readFolder(dbc, project.getUuid(), parentFolderName);
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

        if (project.getUuid().equals(CmsProject.ONLINE_PROJECT_ID)) {
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
            CmsResource existingResource = m_driverManager.getVfsDriver().readResource(
                dbc,
                project.getUuid(),
                resourcePath,
                true);
            if (existingResource.getState().isDeleted()) {
                // if an existing resource is deleted, it will be finally removed now.
                // but we have to reuse its id in order to avoid orphanes in the online project
                newStructureId = existingResource.getStructureId();
                newState = CmsResource.STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = m_driverManager.getVfsDriver().readSiblings(
                    dbc,
                    project,
                    existingResource,
                    false);
                int propertyDeleteOption = (existingResource.getSiblingCount() > 1) ? CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES
                : CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                deletePropertyObjects(dbc, project.getUuid(), existingResource, propertyDeleteOption);
                removeFile(dbc, project, existingResource);

                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
                    Collections.singletonMap("resources", modifiedResources)));
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.singletonMap("resource", existingResource)));
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
            String parentId = internalReadParentId(dbc, project.getUuid(), resourcePath);

            boolean existsResource = validateResourceIdExists(dbc, project.getUuid(), resource.getResourceId());
            conn = m_sqlManager.getConnection(dbc);

            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, newStructureId.toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setString(3, resourcePath);
            stmt.setInt(4, newState.getState());
            stmt.setLong(5, resource.getDateReleased());
            stmt.setLong(6, resource.getDateExpired());
            stmt.setString(7, parentId);
            stmt.setInt(8, (!existsResource ? 0 : 1)); // starting version number
            stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, null, stmt, null);

            if (!existsResource) {
                try {
                    // create the resource record
                    stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_WRITE");
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
                    stmt.setString(11, project.getUuid().toString());
                    stmt.setInt(12, 1); // sibling count
                    stmt.setInt(13, 1); // version number
                    stmt.executeUpdate();
                } finally {
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }

                if (resource.isFile() && (content != null)) {
                    // create the file content
                    createContent(dbc, project.getUuid(), resource.getResourceId(), content);
                }
            } else {
                if ((content != null) || !resource.getState().isKeep()) {
                    // update the resource record only if state has changed or new content is provided
                    stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCES");
                    stmt.setInt(1, resource.getTypeId());
                    stmt.setInt(2, resource.getFlags());
                    stmt.setLong(3, dateModified);
                    stmt.setString(4, resource.getUserLastModified().toString());
                    stmt.setInt(5, CmsResource.STATE_CHANGED.getState());
                    stmt.setInt(6, resource.getLength());
                    stmt.setLong(7, resource.getDateContent());
                    stmt.setString(8, project.getUuid().toString());
                    stmt.setInt(9, internalCountSiblings(dbc, project.getUuid(), resource.getResourceId()));
                    stmt.setString(10, resource.getResourceId().toString());
                    stmt.executeUpdate();

                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }

                if (resource.isFile()) {
                    if (content != null) {
                        // update the file content
                        writeContent(dbc, project.getUuid(), resource.getResourceId(), content);
                    } else if (resource.getState().isKeep()) {
                        // special case sibling creation - update the link Count
                        stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_SIBLING_COUNT");
                        stmt.setInt(1, internalCountSiblings(dbc, project.getUuid(), resource.getResourceId()));
                        stmt.setString(2, resource.getResourceId().toString());
                        stmt.executeUpdate();
                        m_sqlManager.closeAll(dbc, null, stmt, null);

                        // update the resource flags
                        stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_FLAGS");
                        stmt.setInt(1, resource.getFlags());
                        stmt.setString(2, resource.getResourceId().toString());
                        stmt.executeUpdate();
                        m_sqlManager.closeAll(dbc, null, stmt, null);
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

        return readResource(dbc, project.getUuid(), newStructureId, false);
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
        int resourceSize;

        // in case of folder type ensure, that the root path has a trailing slash
        boolean isFolder = CmsFolder.isFolderType(resourceType);
        if (isFolder) {
            resourcePath = addTrailingSeparator(resourcePath);
            // folders must have -1 size
            resourceSize = -1;
        } else {
            // not a folder
            resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        }
        long dateContent = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CONTENT"));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        int siblingCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIBLING_COUNT"));
        int resourceVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
        int structureVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_VERSION"));

        int newState = (structureState > resourceState) ? structureState : resourceState;

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
            resourceVersion + structureVersion);

        return newResource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createSibling(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public void createSibling(CmsDbContext dbc, CmsProject project, CmsResource resource) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        CmsResourceState newState = CmsResource.STATE_UNCHANGED;

        // force some attribs when creating or publishing a file 
        if (project.getUuid().equals(CmsProject.ONLINE_PROJECT_ID)) {
            newState = CmsResource.STATE_UNCHANGED;
        } else {
            newState = CmsResource.STATE_NEW;
        }

        // check if the resource already exists
        CmsResource existingSibling = null;
        CmsUUID newStructureId = resource.getStructureId();

        try {
            existingSibling = readResource(dbc, project.getUuid(), resource.getRootPath(), true);

            if (existingSibling.getState().isDeleted()) {
                // if an existing resource is deleted, it will be finally removed now.
                // but we have to reuse its id in order to avoid orphanes in the online project.
                newStructureId = existingSibling.getStructureId();
                newState = CmsResource.STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = readSiblings(dbc, project, existingSibling, false);
                int propertyDeleteOption = (existingSibling.getSiblingCount() > 1) ? CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES
                : CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                deletePropertyObjects(dbc, project.getUuid(), existingSibling, propertyDeleteOption);
                removeFile(dbc, project, existingSibling);

                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
                    Collections.singletonMap("resources", modifiedResources)));
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.singletonMap("resource", existingSibling)));
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // that's what we want in the best case- anything else should be thrown
        }

        if ((existingSibling != null) && !existingSibling.getState().isDeleted()) {
            // we have a collision: there exists already a resource with the same path/name which could not be removed
            throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(
                Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        // check if a resource with the specified ID already exists
        if (!validateResourceIdExists(dbc, project.getUuid(), resource.getResourceId())) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_CREATE_SIBLING_FILE_NOT_FOUND_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        // write a new structure referring to the resource
        try {
            conn = m_sqlManager.getConnection(dbc);

            // read the parent id
            String parentId = internalReadParentId(dbc, project.getUuid(), resource.getRootPath());

            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, newStructureId.toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setString(3, resource.getRootPath());
            stmt.setInt(4, newState.getState());
            stmt.setLong(5, resource.getDateReleased());
            stmt.setLong(6, resource.getDateExpired());
            stmt.setString(7, parentId);
            stmt.setInt(8, 1); // initial structure version number
            stmt.executeUpdate();

            m_sqlManager.closeAll(dbc, null, stmt, null);

            // update the link Count
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_SIBLING_COUNT");
            stmt.setInt(1, internalCountSiblings(dbc, project.getUuid(), resource.getResourceId()));
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
                    // delete the offline propertydef
                    stmt = m_sqlManager.getPreparedStatement(conn, CmsUUID.getOpenCmsUUID(), "C_PROPERTYDEF_DELETE"); // HACK: to get an offline project
                } else {
                    // delete the online propertydef
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

        List params = new ArrayList(7);

        // prepare the selection criteria
        String conditionsSQL = prepareRelationConditions(filter, resource, params);

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery(projectId, "C_DELETE_RELATIONS"));
            queryBuf.append(conditionsSQL);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, queryBuf.toString());

            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, (String)params.get(i));
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
     * @see org.opencms.db.I_CmsVfsDriver#getSqlManager()
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
        List successiveDrivers,
        CmsDriverManager driverManager) {

        Map configuration = configurationManager.getConfiguration();
        String poolUrl = (String)configuration.get("db.vfs.pool");
        String classname = (String)configuration.get("db.vfs.sqlmanager");
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
     * @see org.opencms.db.I_CmsVfsDriver#moveRelations(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, org.opencms.file.CmsResource)
     */
    public void moveRelations(CmsDbContext dbc, CmsUUID projectId, CmsResource resource) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_MOVE_RELATIONS_SOURCE");
            stmt.setString(1, resource.getRootPath());
            stmt.setString(2, resource.getStructureId().toString());

            stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, null, stmt, null);

            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_MOVE_RELATIONS_TARGET");
            stmt.setString(1, resource.getRootPath());
            stmt.setString(2, resource.getStructureId().toString());

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
     * @see org.opencms.db.I_CmsVfsDriver#moveResource(CmsDbContext, CmsUUID, CmsResource, String)
     */
    public void moveResource(CmsDbContext dbc, CmsUUID projectId, CmsResource source, String destinationPath)
    throws CmsDataAccessException {

        // do not allow to move a resource into an as deleted marked folder
        CmsResourceFilter filter = CmsResourceFilter.IGNORE_EXPIRATION;
        if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
            // does not matter online
            filter = CmsResourceFilter.ALL;
        }

        // determine destination folder        
        String destinationFoldername = CmsResource.getParentFolder(destinationPath);

        // read the destination folder (will also check read permissions)
        CmsFolder destinationFolder = m_driverManager.readFolder(dbc, destinationFoldername, filter);

        if (!projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
            // check online resource
            try {
                CmsResource onlineResource = m_driverManager.getVfsDriver().readResource(
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
                            dbc.getRequestContext().currentProject().getUuid(),
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
            stmt.setString(1, removeTrailingSeparator(destinationPath)); // must remove trailing slash
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

        String resourcePath = removeTrailingSeparator(offlineResource.getRootPath());

        try {
            conn = m_sqlManager.getConnection(dbc);
            boolean resourceExists = validateResourceIdExists(
                dbc,
                onlineProject.getUuid(),
                offlineResource.getResourceId());
            if (resourceExists) {
                // the resource record exists online already
                try {
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
                    stmt.setInt(9, internalCountSiblings(dbc, onlineProject.getUuid(), onlineResource.getResourceId()));
                    stmt.setString(10, offlineResource.getResourceId().toString());
                    stmt.executeUpdate();
                } finally {
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }
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
                stmt.setInt(12, 1); // siblings count
                stmt.setInt(13, 1); // initial version
                stmt.executeUpdate();

                m_sqlManager.closeAll(dbc, null, stmt, null);
            }

            // read the parent id
            String parentId = internalReadParentId(dbc, onlineProject.getUuid(), resourcePath);

            if (validateStructureIdExists(dbc, onlineProject.getUuid(), offlineResource.getStructureId())) {
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
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        internalPublishVersions(dbc, offlineResource);
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

        String typeColumn = m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, query.toString());
            stmt.setString(1, resource.getStructureId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                int type = res.getInt(typeColumn);
                if (CmsFolder.isFolderType(type)) {
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
        Collections.sort(result, CmsResource.COMPARE_ROOT_PATH_IGNORE_CASE_FOLDERS_FIRST);
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
                //query to read Array of bytes for the atribute FILE_CONTENT
                byteRes = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_FILE_WITH_STRUCTURE_ID_1,
                    resourceId));
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

        folderPath = removeTrailingSeparator(folderPath);
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

            // if resultset exists - return it
            if (res.next()) {
                propDef = new CmsPropertyDefinition(
                    new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_ID"))),
                    res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")),
                    CmsPropertyDefinition.CmsPropertyType.valueOf(res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_TYPE"))));
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

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String propertyValue = null;
        int mappingType = -1;
        CmsProperty property = null;
        int resultSize = 0;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, project.getUuid(), "C_PROPERTIES_READ");

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
            stmt = m_sqlManager.getPreparedStatement(conn, project.getUuid(), "C_PROPERTIES_READALL");
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

        List relations = new ArrayList();

        // prepare the selection criteria
        List params = new ArrayList(7);
        String conditions = prepareRelationConditions(filter, resource, params);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery(projectId, "C_READ_RELATIONS"));
            queryBuf.append(conditions);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, queryBuf.toString());

            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, (String)params.get(i));
            }
            res = stmt.executeQuery();
            while (res.next()) {
                relations.add(internalReadRelation(res));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return relations;
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
            throw new CmsVfsException(Messages.get().container(
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
        path = removeTrailingSeparator(path);

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ");

            stmt.setString(1, path);
            res = stmt.executeQuery();

            if (res.next()) {
                resource = createResource(res, projectId);
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
                stmt.setString(5, "%" + path + "%");
                stmt.setString(6, value + "%");
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
                stmt.setString(i + 1, (String)params.get(i));
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
     * @see org.opencms.db.I_CmsVfsDriver#readSiblings(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, boolean)
     */
    public List readSiblings(CmsDbContext dbc, CmsProject currentProject, CmsResource resource, boolean includeDeleted)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        CmsResource currentResource = null;
        List vfsLinks = new ArrayList();

        try {
            conn = m_sqlManager.getConnection(dbc);

            if (includeDeleted) {
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_SELECT_VFS_SIBLINGS");
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_SELECT_NONDELETED_VFS_SIBLINGS");
            }

            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                currentResource = createFile(res, currentProject.getUuid(), false);
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
     * @see org.opencms.db.I_CmsVfsDriver#removeFile(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public void removeFile(CmsDbContext dbc, CmsProject currentProject, CmsResource resource)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        int siblingCount = 0;

        try {
            conn = m_sqlManager.getConnection(dbc);

            // delete the structure record
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_STRUCTURE_DELETE_BY_STRUCTUREID");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(dbc, null, stmt, null);

            // count the references to the resource
            siblingCount = internalCountSiblings(dbc, currentProject.getUuid(), resource.getResourceId());

            if (siblingCount > 0) {

                // update the link Count
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCES_UPDATE_SIBLING_COUNT");
                stmt.setInt(1, this.internalCountSiblings(dbc, currentProject.getUuid(), resource.getResourceId()));
                stmt.setString(2, resource.getResourceId().toString());
                stmt.executeUpdate();

                m_sqlManager.closeAll(dbc, null, stmt, null);

                // update the resource flags
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCES_UPDATE_FLAGS");
                stmt.setInt(1, resource.getFlags());
                stmt.setString(2, resource.getResourceId().toString());
                stmt.executeUpdate();

            } else {

                // if not referenced any longer, also delete the resource and the content record
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCES_DELETE_BY_RESOURCEID");
                stmt.setString(1, resource.getResourceId().toString());
                stmt.executeUpdate();

                m_sqlManager.closeAll(dbc, null, stmt, null);

                // if online we have to keep historical content
                if (!currentProject.isOnlineProject()) {
                    // delete content records with this resource id
                    stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_OFFLINE_FILE_CONTENT_DELETE");
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

        // check if the folder has any resources in it
        Iterator childResources = readChildResources(dbc, currentProject, resource, true, true).iterator();

        CmsUUID projectId = CmsProject.ONLINE_PROJECT_ID;
        if (currentProject.isOnlineProject()) {
            projectId = CmsUUID.getOpenCmsUUID(); // HACK: to get an offline project id
        }

        // collect the names of the resources inside the folder, excluding the moved resources
        StringBuffer errorResNames = new StringBuffer(128);
        while (childResources.hasNext()) {
            CmsResource errorRes = (CmsResource)childResources.next();
            // if deleting offline, or not moved, or just renamed inside the deleted folder
            // so, it may remain some orphan online entries for moved resources
            // which will be fixed during the publishing of the moved resources
            boolean error = !currentProject.isOnlineProject();
            if (!error) {
                try {
                    String originalPath = m_driverManager.getVfsDriver().readResource(
                        dbc,
                        projectId,
                        errorRes.getRootPath(),
                        true).getRootPath();
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
            writeContent(dbc, dbc.currentProject().getUuid(), newResource.getResourceId(), resContent);

            // update the resource record
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject(), "C_RESOURCE_REPLACE");
            stmt.setInt(1, newResourceType);
            stmt.setInt(2, resContent.length);
            stmt.setString(3, newResource.getResourceId().toString());
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
     * @see org.opencms.db.I_CmsVfsDriver#writeContent(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID, byte[])
     */
    public long writeContent(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId, byte[] content)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_OFFLINE_CONTENTS_UPDATE");
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
            m_sqlManager.closeAll(dbc, null, stmt, null);
        }
        // store the content modification time
        long time = System.currentTimeMillis();
        try {
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCE_UPDATE_CONTENT_DATE");
            stmt.setLong(1, time);
            stmt.setString(2, resourceId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        return time;
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

        // check if we need autocreation for link property definition types too
        int todo;
        CmsPropertyDefinition propertyDefinition = null;
        try {
            // read the property definition
            propertyDefinition = readPropertyDefinition(dbc, property.getName(), project.getUuid());
        } catch (CmsDbEntryNotFoundException e) {
            if (property.autoCreatePropertyDefinition()) {
                propertyDefinition = createPropertyDefinition(
                    dbc,
                    project.getUuid(),
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
                    m_driverManager.getHistoryDriver().readPropertyDefinition(dbc, property.getName());
                } catch (CmsDataAccessException e1) {
                    m_driverManager.getHistoryDriver().createPropertyDefinition(
                        dbc,
                        property.getName(),
                        CmsPropertyDefinition.TYPE_NORMAL);
                }
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
                            stmt = m_sqlManager.getPreparedStatement(conn, project.getUuid(), "C_PROPERTIES_UPDATE");
                            stmt.setString(1, m_sqlManager.validateEmpty(value));
                            stmt.setString(2, id.toString());
                            stmt.setInt(3, mappingType);
                            stmt.setString(4, propertyDefinition.getId().toString());
                        } else {
                            // {structure|resource} property value doesn't exist- use create statement
                            stmt = m_sqlManager.getPreparedStatement(conn, project.getUuid(), "C_PROPERTIES_CREATE");
                            stmt.setString(1, new CmsUUID().toString());
                            stmt.setString(2, propertyDefinition.getId().toString());
                            stmt.setString(3, id.toString());
                            stmt.setInt(4, mappingType);
                            stmt.setString(5, m_sqlManager.validateEmpty(value));
                        }
                    } else {
                        // {structure|resource} property value marked as deleted- use delete statement
                        stmt = m_sqlManager.getPreparedStatement(conn, project.getUuid(), "C_PROPERTIES_DELETE");
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
     * @see org.opencms.db.I_CmsVfsDriver#writeResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, int)
     */
    public void writeResource(CmsDbContext dbc, CmsProject project, CmsResource resource, int changed)
    throws CmsDataAccessException {

        // validate the resource length
        internalValidateResourceLength(resource);

        String resourcePath = removeTrailingSeparator(resource.getRootPath());

        // this task is split into two statements because some DBs (e.g. Oracle) doesnt support muti-table updates
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
        CmsResourceState structureStateOld = internalReadStructureState(dbc, project, resource);
        CmsResourceState resourceStateOld = internalReadResourceState(dbc, project, resource);
        CmsUUID projectId = project.getUuid();

        if (changed == CmsDriverManager.UPDATE_RESOURCE_STATE) {
            resourceState = resourceStateOld;
            resourceState = (resourceState.isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
            structureState = structureStateOld;
        } else if (changed == CmsDriverManager.UPDATE_STRUCTURE_STATE) {
            structureState = structureStateOld;
            structureState = (structureState.isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
        } else if (changed == CmsDriverManager.NOTHING_CHANGED) {
            projectId = resource.getProjectLastModified();
        } else {
            resourceState = resourceStateOld;
            resourceState = (resourceState.isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
            structureState = structureStateOld;
            structureState = (structureState.isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
        }

        boolean updateResource = false; //!resourceStateOld.isNew();
        boolean updateStructure = false; //!structureStateOld.isNew();
        try {
            conn = m_sqlManager.getConnection(dbc);

            if (changed != CmsDriverManager.UPDATE_STRUCTURE_STATE) {
                // if the resource was unchanged
                updateResource = true;
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCES");
                stmt.setInt(1, resource.getTypeId());
                stmt.setInt(2, resource.getFlags());
                stmt.setLong(3, resourceDateModified);
                stmt.setString(4, resource.getUserLastModified().toString());
                stmt.setInt(5, resourceState.getState());
                stmt.setInt(6, resource.getLength());
                stmt.setLong(7, resource.getDateContent());
                stmt.setString(8, projectId.toString());
                stmt.setInt(9, internalCountSiblings(dbc, project.getUuid(), resource.getResourceId()));
                stmt.setString(10, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCES_WITHOUT_STATE");
                stmt.setInt(1, resource.getTypeId());
                stmt.setInt(2, resource.getFlags());
                stmt.setLong(3, resourceDateModified);
                stmt.setString(4, resource.getUserLastModified().toString());
                stmt.setInt(5, resource.getLength());
                stmt.setLong(6, resource.getDateContent());
                stmt.setString(7, projectId.toString());
                stmt.setInt(8, internalCountSiblings(dbc, project.getUuid(), resource.getResourceId()));
                stmt.setString(9, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }

            // read the parent id
            String parentId = internalReadParentId(dbc, project.getUuid(), resourcePath);

            updateStructure = true;
            // update the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_STRUCTURE");
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
            m_sqlManager.closeAll(dbc, conn, null, null);
        }

        if (updateResource || updateStructure) {
            boolean updateOnlyStructure = !updateResource && updateStructure;
            internalUpdateVersions(dbc, resource, updateOnlyStructure);
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

        boolean updateResource = false;
        boolean updateStructure = false;
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
                updateResource = true;
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
                updateResource = true;
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
                updateStructure = true;
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

        if (!isPublishing) {
            // update versions only is not publishing
            if (updateResource || updateStructure) {
                // update versions only if a state has been modified
                boolean updateOnlyStructure = !updateResource && updateStructure;
                internalUpdateVersions(dbc, resource, updateOnlyStructure);
            }
        }
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
    protected String escapeDbWildcard(String path) {

        return CmsStringUtil.substitute(path, "_", "|_");
    }

    /**
     * Returns the count of properties for a property definition.<p>
     * 
     * @param dbc the current database context
     * @param propertyDefinition the propertydefinition to test
     * @param projectId the ID of the current project
     * 
     * @return the amount of properties for a propertydefinition
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
     * Counts the number of siblings of a resource.<p>
     * 
     * @param dbc the current database context
     * @param projectId the current project id
     * @param resourceId the resource id to count the number of siblings from
     * 
     * @return number of siblings
     * @throws CmsDataAccessException if something goes wrong
     */
    protected int internalCountSiblings(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId)
    throws CmsDataAccessException {

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
     * Copies the version number from the offline resource to the online resource,
     * this has to be done during publishing, direct after copying the resource itself.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource that has been publish
     * 
     * @throws CmsDbSqlException if something goes wrong
     */
    protected void internalPublishVersions(CmsDbContext dbc, CmsResource resource) throws CmsDbSqlException {

        if (dbc.currentProject().getUuid().equals(CmsProject.ONLINE_PROJECT_ID)) {
            // this method is suppossed to be used only in the offline project
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            int offlineStructureVersion = -1;
            int offlineResourceVersion = -1;
            // read the offline version numbers
            stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject(), "C_RESOURCES_READ_VERSIONS");
            stmt.setString(1, resource.getStructureId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                offlineResourceVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
                offlineStructureVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_VERSION"));
            } else {
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.ERR_GENERIC_SQL_1,
                        CmsDbSqlException.getErrorQuery(stmt)));
                }
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);

            // update the resource version
            stmt = m_sqlManager.getPreparedStatement(
                conn,
                CmsProject.ONLINE_PROJECT_ID,
                "C_RESOURCES_UPDATE_RESOURCE_VERSION");
            stmt.setInt(1, offlineResourceVersion);
            stmt.setString(2, resource.getResourceId().toString());
            stmt.executeUpdate();
            m_sqlManager.closeAll(dbc, null, stmt, null);

            // update the structure version
            stmt = m_sqlManager.getPreparedStatement(
                conn,
                CmsProject.ONLINE_PROJECT_ID,
                "C_RESOURCES_UPDATE_STRUCTURE_VERSION");
            stmt.setInt(1, offlineStructureVersion);
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
     * Returns the parent id of the given resource.<p>
     * 
     * @param dbc the current database context
     * @param projectId the current project id 
     * @param resourcename the resource name to read the parent id for
     * @return  the parent id of the given resource
     * @throws CmsDataAccessException if something goes wrong
     */
    protected String internalReadParentId(CmsDbContext dbc, CmsUUID projectId, String resourcename)
    throws CmsDataAccessException {

        if ("/".equalsIgnoreCase(resourcename)) {
            return CmsUUID.getNullUUID().toString();
        }

        String parent = CmsResource.getParentFolder(resourcename);
        parent = removeTrailingSeparator(parent);

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
        long dateBegin = res.getLong(m_sqlManager.readQuery("C_RELATION_DATE_BEGIN"));
        long dateEnd = res.getLong(m_sqlManager.readQuery("C_RELATION_DATE_END"));
        int type = res.getInt(m_sqlManager.readQuery("C_RELATION_TYPE"));
        return new CmsRelation(
            sourceId,
            sourcePath,
            targetId,
            targetPath,
            dateBegin,
            dateEnd,
            CmsRelationType.valueOf(type));
    }

    /**
     * Returns the resource state of the given resource.<p>
     * 
     * @param dbc the dbc context
     * @param project the project
     * @param resource the resource to read the resource state for
     * 
     * @return the resource state of the given resource
     * 
     * @throws CmsDbSqlException if somehting goes wrong
     */
    protected CmsResourceState internalReadResourceState(CmsDbContext dbc, CmsProject project, CmsResource resource)
    throws CmsDbSqlException {

        CmsResourceState state = CmsResource.STATE_KEEP;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, project.getUuid(), "C_READ_RESOURCE_STATE");
            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                state = CmsResourceState.valueOf(res.getInt(m_sqlManager.readQuery("C_RESOURCES_STATE")));
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
     * @param dbc the dbc context
     * @param project the project
     * @param resource the resource to read the structure state for
     * 
     * @return the structure state of the given resource
     * 
     * @throws CmsDbSqlException if somehting goes wrong
     */
    protected CmsResourceState internalReadStructureState(CmsDbContext dbc, CmsProject project, CmsResource resource)
    throws CmsDbSqlException {

        CmsResourceState state = CmsResource.STATE_KEEP;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, project.getUuid(), "C_READ_STRUCTURE_STATE");
            stmt.setString(1, resource.getStructureId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                state = CmsResourceState.valueOf(res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_STATE")));
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
     * Updates the offline version numbers, following these rules:<p>
     * 
     * if (offline resource version is equals to the online resource version) and
     *    (offline structure version is equals to the online structure version) then
     *    if (updateOnlyStructure) then increase structure version else increase resource version.<p>
     * if (offline resource version is not equals to the online resource version) and
     *    (offline structure version is equals to the online structure version) then
     *    do nothing, independent of updateOnlyStructure.<p>
     * if (offline resource version is equals to the online resource version) and
     *    (offline structure version is not equals to the online structure version) then
     *    if (updateOnlyStructure) then do nothing else increase resource version and decrease structure version.<p>
     * if (offline resource version is equals to the online resource version) and
     *    (offline structure version is not equals to the online structure version) then
     *    do nothing, independent of updateOnlyStructure (can not happen).<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to update the version number for
     * @param updateOnlyStructure if the last modification applies only one sibling or the whole resource
     * 
     * @throws CmsDbSqlException if something goes wrong 
     */
    protected void internalUpdateVersions(CmsDbContext dbc, CmsResource resource, boolean updateOnlyStructure)
    throws CmsDbSqlException {

        if (dbc.getRequestContext() == null) {
            // no needed during initialization 
            return;
        }
        if (dbc.currentProject().getUuid().equals(CmsProject.ONLINE_PROJECT_ID)) {
            // this method is suppossed to be used only in the offline project
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);

            int onlineResourceVersion = -1;
            int onlineStructureVersion = -1;

            // read the online version numbers
            stmt = m_sqlManager.getPreparedStatement(conn, CmsProject.ONLINE_PROJECT_ID, "C_RESOURCES_READ_VERSIONS");
            stmt.setString(1, resource.getStructureId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                onlineResourceVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
                onlineStructureVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_VERSION"));
            } else {
                // resource is new, do not change the version numbers
                return;
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);

            int offlineStructureVersion = -1;
            int offlineResourceVersion = -1;
            // read the offline version numbers
            stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject(), "C_RESOURCES_READ_VERSIONS");
            stmt.setString(1, resource.getStructureId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                offlineResourceVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION"));
                offlineStructureVersion = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_VERSION"));
            } else {
                LOG.error(Messages.get().getBundle().key(
                    Messages.ERR_READ_RESOURCE_VERSIONS_1,
                    dbc.removeSiteRoot(resource.getRootPath())));
                return;
            }
            m_sqlManager.closeAll(dbc, null, stmt, res);

            if (offlineResourceVersion != onlineResourceVersion) {
                // already updated in a previous operation
                return;
            }

            int newResourceVersion = -1;
            int newStructureVersion = -1;
            if (offlineStructureVersion == onlineStructureVersion) {
                if (updateOnlyStructure) {
                    newStructureVersion = onlineStructureVersion + 1;
                } else {
                    newResourceVersion = onlineResourceVersion + 1;
                }
            } else {
                if (!updateOnlyStructure) {
                    newStructureVersion = onlineStructureVersion; // this will decrease the offline version
                    newResourceVersion = onlineResourceVersion + 1;
                }
            }

            if (newResourceVersion != -1) {
                // update the resource version if needed
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    dbc.currentProject(),
                    "C_RESOURCES_UPDATE_RESOURCE_VERSION");
                stmt.setInt(1, newResourceVersion);
                stmt.setString(2, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }
            if (newStructureVersion != -1) {
                // update the structure version if needed
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    dbc.currentProject(),
                    "C_RESOURCES_UPDATE_STRUCTURE_VERSION");
                stmt.setInt(1, newStructureVersion);
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
     * Appends the appropriate selection criteria related with the expiration date.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param startTime the start time
     * @param endTime the end time
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection params
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
            params.add(String.valueOf(startTime));
        }

        if (endTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match expired date against endTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_STRUCTURE_SELECT_BY_DATE_EXPIRED_BEFORE"));
            conditions.append(END_CONDITION);
            params.add(String.valueOf(endTime));
        }
    }

    /**
     * Appends the appropriate selection criteria related with the parentPath.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param parent the parent path or UUID (if mode is C_READMODE_EXCLUDE_TREE)
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection params
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
        params.add(addTrailingSeparator(escapeDbWildcard(parent)) + "%");
    }

    /**
     * Appends the appropriate selection criteria related with the projectId.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection params
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
     * All source or target (id and path) conditions are concatenated by <code>OR</code> 
     * operators, the condition can be restricted by date and type conditions which are 
     * concatenated by <code>AND</code> operators.<p>
     * 
     * @param filter the filter
     * @param resource the resource (may be null, if you want to delete all relations for the resource in the filter)
     * @param params the parameter values (return parameter)
     * 
     * @return the WHERE sql statement part string
     */
    protected String prepareRelationConditions(CmsRelationFilter filter, CmsResource resource, List params) {

        StringBuffer conditions = new StringBuffer(128);

        // source or target filter
        if (filter.isSource() || filter.isTarget()) {
            // source or target id filter from resource
            if (resource != null) {
                conditions.append(BEGIN_CONDITION);
                if (filter.isSource()) {
                    conditions.append(m_sqlManager.readQuery("C_RELATION_FILTER_TARGET_ID"));
                    params.add(resource.getStructureId().toString());
                    if (filter.isTarget()) {
                        // if both, then 'OR' condition is used
                        conditions.append(OR_CONDITION);
                        conditions.append(m_sqlManager.readQuery("C_RELATION_FILTER_SOURCE_ID"));
                        params.add(resource.getStructureId().toString());
                    }
                } else if (filter.isTarget()) {
                    conditions.append(m_sqlManager.readQuery("C_RELATION_FILTER_SOURCE_ID"));
                    params.add(resource.getStructureId().toString());
                }
                conditions.append(END_CONDITION);
            }

            // target or source id filter from filter parameter
            if ((filter.getStructureId() != null) && (!filter.getStructureId().isNullUUID())) {
                if (conditions.length() == 0) {
                    conditions.append(BEGIN_CONDITION);
                } else {
                    conditions.append(BEGIN_INCLUDE_CONDITION);
                }

                if (filter.isSource()) {
                    conditions.append(m_sqlManager.readQuery("C_RELATION_FILTER_SOURCE_ID"));
                    params.add(filter.getStructureId().toString());
                    if (filter.isTarget()) {
                        // if both, then 'OR' condition is used
                        conditions.append(OR_CONDITION);
                        conditions.append(m_sqlManager.readQuery("C_RELATION_FILTER_TARGET_ID"));
                        params.add(filter.getStructureId().toString());
                    }
                } else if (filter.isTarget()) {
                    conditions.append(m_sqlManager.readQuery("C_RELATION_FILTER_TARGET_ID"));
                    params.add(filter.getStructureId().toString());
                }
                conditions.append(END_CONDITION);
            }

            // target or source id filter from filter parameter
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(filter.getPath())) {
                if (conditions.length() == 0) {
                    conditions.append(BEGIN_CONDITION);
                } else {
                    conditions.append(BEGIN_INCLUDE_CONDITION);
                }

                String queryPath = filter.getPath();
                if (filter.isIncludeChilds()) {
                    queryPath += '%';
                }
                if (filter.isSource()) {
                    conditions.append(m_sqlManager.readQuery("C_RELATION_FILTER_SOURCE_PATH"));
                    params.add(queryPath);
                    if (filter.isTarget()) {
                        // if both, or condition is used
                        conditions.append(OR_CONDITION);
                        conditions.append(m_sqlManager.readQuery("C_RELATION_FILTER_TARGET_PATH"));
                        params.add(queryPath);
                    }
                } else if (filter.isTarget()) {
                    conditions.append(m_sqlManager.readQuery("C_RELATION_FILTER_TARGET_PATH"));
                    params.add(queryPath);
                }
                conditions.append(END_CONDITION);
            }
        }

        // date filter
        if (filter.getDate() > 0) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery("C_RELATION_FILTER_DATE"));
            conditions.append(END_CONDITION);
            // once for the begin
            params.add(String.valueOf(filter.getDate()));
            // once for the end
            params.add(String.valueOf(filter.getDate()));
        }

        // relation type filter
        Set types = filter.getTypes();
        if (!types.isEmpty()) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            Iterator it = types.iterator();
            while (it.hasNext()) {
                CmsRelationType type = (CmsRelationType)it.next();
                conditions.append(m_sqlManager.readQuery("C_RELATION_FILTER_TYPE"));
                params.add(String.valueOf(type.getId()));
                if (it.hasNext()) {
                    conditions.append(OR_CONDITION);
                }
            }
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
     * @param params list to append the selection params
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
            params.add(String.valueOf(startTime));
        }

        if (endTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match released date against endTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_STRUCTURE_SELECT_BY_DATE_RELEASED_BEFORE"));
            conditions.append(END_CONDITION);
            params.add(String.valueOf(endTime));
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
     * @param params list to append the selection params
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
                // otherwise add condition to match against given state if neccessary
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_STATE"));
            conditions.append(END_CONDITION);
            params.add(state.toString());
            params.add(state.toString());
        }
    }

    /**
     * Appends the appropriate selection criteria related with the date of the last modification.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param startTime start of the time range
     * @param endTime end of the time range
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection params
     */
    protected void prepareTimeRangeCondition(
        CmsUUID projectId,
        long startTime,
        long endTime,
        StringBuffer conditions,
        List params) {

        if (startTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match lastmodified date against startTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_DATE_LASTMODIFIED_AFTER"));
            conditions.append(END_CONDITION);
            params.add(String.valueOf(startTime));
        }

        if (endTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match lastmodified date against endTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_DATE_LASTMODIFIED_BEFORE"));
            conditions.append(END_CONDITION);
            params.add(String.valueOf(endTime));
        }
    }

    /**
     * Appends the appropriate selection criteria related with the resource type.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param type the resource type
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection params
     */
    protected void prepareTypeCondition(CmsUUID projectId, int type, int mode, StringBuffer conditions, List params) {

        if (type != CmsDriverManager.READ_IGNORE_TYPE) {
            if ((mode & CmsDriverManager.READMODE_EXCLUDE_TYPE) > 0) {
                // C_READ_FILE_TYPES: add condition to match against any type, but not given type
                conditions.append(BEGIN_EXCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_TYPE"));
                conditions.append(END_CONDITION);
                params.add(String.valueOf(type));
            } else {
                //otherwise add condition to match against given type if neccessary
                conditions.append(BEGIN_INCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_TYPE"));
                conditions.append(END_CONDITION);
                params.add(String.valueOf(type));
            }
        }
    }
}
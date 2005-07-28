/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsVfsDriver.java,v $
 * Date   : $Date: 2005/07/28 10:53:54 $
 * Version: $Revision: 1.253 $
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
import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Generic (ANSI-SQL) database server implementation of the VFS driver methods.<p>
 * 
 * @author Thomas Weckert 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.253 $
 * 
 * @since 6.0.0 
 */
public class CmsVfsDriver implements I_CmsDriver, I_CmsVfsDriver {

    /** Operator to concatenate exclude conditions. */
    protected static final String BEGIN_EXCLUDE_CONDITION = " AND NOT (";

    /** Operator to concatenate include conditions. */
    protected static final String BEGIN_INCLUDE_CONDITION = " AND (";

    /** String to end a single condition. */
    protected static final String END_CONDITION = ") ";

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
     * @see org.opencms.db.I_CmsVfsDriver#createContent(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, byte[], int)
     */
    public void createContent(CmsDbContext dbc, CmsProject project, CmsUUID resourceId, byte[] content, int versionId)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_CONTENTS_WRITE");
            stmt.setString(1, new CmsUUID().toString());
            stmt.setString(2, resourceId.toString());

            if (content.length < 2000) {
                stmt.setBytes(3, content);
            } else {
                stmt.setBinaryStream(3, new ByteArrayInputStream(content), content.length);
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
     * @see org.opencms.db.I_CmsVfsDriver#createFile(java.sql.ResultSet, int)
     */
    public CmsFile createFile(ResultSet res, int projectId) throws SQLException {

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
        CmsUUID contentId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_CONTENT_ID")));
        byte[] content = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
        int siblingCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIBLING_COUNT"));

        // calculate the overall state
        int newState = (structureState > resourceState) ? structureState : resourceState;

        // in case of folder type ensure, that the root path has a trailing slash
        if (CmsFolder.isFolderType(resourceType)) {
            resourcePath = addTrailingSeparator(resourcePath);
        }

        return new CmsFile(
            structureId,
            resourceId,
            contentId,
            resourcePath,
            resourceType,
            resourceFlags,
            projectId,
            newState,
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            siblingCount,
            resourceSize,
            content);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(java.sql.ResultSet, int, boolean)
     */
    public CmsFile createFile(ResultSet res, int projectId, boolean hasFileContentInResultSet) throws SQLException {

        byte[] content = null;

        int resProjectId = CmsDbUtil.UNKNOWN_ID;

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
        int lockedInProject = res.getInt("LOCKED_IN_PROJECT");
        int siblingCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIBLING_COUNT"));

        // in case of folder type ensure, that the root path has a trailing slash
        if (CmsFolder.isFolderType(resourceType)) {
            resourcePath = addTrailingSeparator(resourcePath);
        }

        CmsUUID contentId;
        if (hasFileContentInResultSet) {
            content = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
            contentId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_CONTENT_ID")));
        } else {
            content = new byte[0];
            contentId = CmsUUID.getNullUUID();
        }

        resProjectId = lockedInProject;

        int newState = (structureState > resourceState) ? structureState : resourceState;

        return new CmsFile(
            structureId,
            resourceId,
            contentId,
            resourcePath,
            resourceType,
            resourceFlags,
            resProjectId,
            newState,
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            siblingCount,
            resourceSize,
            content);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFolder(java.sql.ResultSet, int, boolean)
     */
    public CmsFolder createFolder(ResultSet res, int projectId, boolean hasProjectIdInResultSet) throws SQLException {

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
        int resProjectId = res.getInt("LOCKED_IN_PROJECT");
        int siblingCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIBLING_COUNT"));

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
            newState,
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            siblingCount,
            dateReleased,
            dateExpired);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createPropertyDefinition(org.opencms.db.CmsDbContext, int, java.lang.String)
     */
    public CmsPropertyDefinition createPropertyDefinition(CmsDbContext dbc, int projectId, String name)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        // TODO switch the property def. PK into a CmsUUID PK

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_CREATE");
            stmt.setString(1, new CmsUUID().toString());
            stmt.setString(2, name);
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

        // check if the parent folder of the resource exists and is not deleted
        if (!resource.getRootPath().equals("/")) {
            String parentFolderName = CmsResource.getParentFolder(resource.getRootPath());
            CmsFolder parentFolder = readFolder(dbc, project.getId(), parentFolderName);
            if (parentFolder.getState() == CmsResource.STATE_DELETED) {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_PARENT_FOLDER_DELETED_1,
                    resource.getRootPath()));
            }
        }

        // validate the resource length
        internalValidateResourceLength(resource);

        // set the resource state and modification dates
        int newState;
        long dateModified;
        long dateCreated;

        if (project.getId() == CmsProject.ONLINE_PROJECT_ID) {
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
        CmsResource existingResource = null;
        newStructureId = resource.getStructureId();

        try {
            existingResource = readResource(dbc, project.getId(), resourcePath, true);

            if (existingResource.getState() == CmsResource.STATE_DELETED) {

                // if an existing resource is deleted, it will be finally removed now.
                // but we have to reuse its id in order to avoid orphanes in the online project
                newStructureId = existingResource.getStructureId();
                newState = CmsResource.STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = readSiblings(dbc, project, existingResource, false);
                int propertyDeleteOption = (existingResource.getSiblingCount() > 1) ? CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES
                : CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                deletePropertyObjects(dbc, project.getId(), existingResource, propertyDeleteOption);
                removeFile(dbc, project, existingResource, true);

                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
                    Collections.singletonMap("resources", modifiedResources)));
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.singletonMap("resource", existingResource)));
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // that's what we want in the best case- anything else should be thrown
        }

        if (existingResource != null && existingResource.getState() != CmsResource.STATE_DELETED) {
            // we have a collision: there exists already a resource with the same path/name which cannot be removed
            throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(
                Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        try {

            // read the parent id
            String parentId = internalReadParentId(dbc, project.getId(), resourcePath);

            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, newStructureId.toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setString(3, resourcePath);
            stmt.setInt(4, newState);
            stmt.setLong(5, resource.getDateReleased());
            stmt.setLong(6, resource.getDateExpired());
            stmt.setString(7, parentId);
            stmt.executeUpdate();

            m_sqlManager.closeAll(dbc, null, stmt, null);

            if (!validateResourceIdExists(dbc, project.getId(), resource.getResourceId())) {

                // create the resource record
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_WRITE");
                stmt.setString(1, resource.getResourceId().toString());
                stmt.setInt(2, resource.getTypeId());
                stmt.setInt(3, resource.getFlags());
                stmt.setLong(4, dateCreated);
                stmt.setString(5, resource.getUserCreated().toString());
                stmt.setLong(6, dateModified);
                stmt.setString(7, resource.getUserLastModified().toString());
                stmt.setInt(8, newState);
                stmt.setInt(9, resource.getLength());
                stmt.setInt(10, project.getId());
                stmt.setInt(11, 1);
                stmt.executeUpdate();

                if (resource.isFile() && content != null) {
                    // create the file content
                    createContent(dbc, project, resource.getResourceId(), content, 0);
                }

            } else {

                if ((content != null) || (resource.getState() != CmsResource.STATE_KEEP)) {
                    // update the resource record only if state has changed or new content is provided
                    stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCES");
                    stmt.setInt(1, resource.getTypeId());
                    stmt.setInt(2, resource.getFlags());
                    stmt.setLong(3, dateModified);
                    stmt.setString(4, resource.getUserLastModified().toString());
                    stmt.setInt(5, CmsResource.STATE_CHANGED);
                    stmt.setInt(6, resource.getLength());
                    stmt.setInt(7, project.getId());
                    stmt.setInt(8, internalCountSiblings(dbc, project.getId(), resource.getResourceId()));
                    stmt.setString(9, resource.getResourceId().toString());
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }

                if (resource.isFile()) {
                    if (content != null) {

                        // update the file content
                        writeContent(dbc, project, resource.getResourceId(), content);

                    } else if (resource.getState() == CmsResource.STATE_KEEP) {

                        // special case sibling creation - update the link Count
                        stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_SIBLING_COUNT");
                        stmt.setInt(1, this.internalCountSiblings(dbc, project.getId(), resource.getResourceId()));
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

        return readResource(dbc, project.getId(), newStructureId, false);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createResource(java.sql.ResultSet, int)
     */
    public CmsResource createResource(ResultSet res, int projectId) throws SQLException {

        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        String resourcePath = res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_PATH"));
        int resourceType = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));
        int resourceFlags = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_FLAGS"));
        int resourceProjectLastModified = res.getInt(m_sqlManager.readQuery("C_RESOURCES_PROJECT_LASTMODIFIED"));
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
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        int siblingCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIBLING_COUNT"));

        int newState = (structureState > resourceState) ? structureState : resourceState;

        CmsResource newResource = new CmsResource(
            structureId,
            resourceId,
            resourcePath,
            resourceType,
            isFolder,
            resourceFlags,
            resourceProjectLastModified,
            newState,
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            siblingCount,
            resourceSize);

        return newResource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createSibling(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public void createSibling(CmsDbContext dbc, CmsProject project, CmsResource resource) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        int newState = 0;

        // force some attribs when creating or publishing a file 
        if (project.getId() == CmsProject.ONLINE_PROJECT_ID) {
            newState = CmsResource.STATE_UNCHANGED;
        } else {
            newState = CmsResource.STATE_NEW;
        }

        // check if the resource already exists
        CmsResource existingSibling = null;
        CmsUUID newStructureId = resource.getStructureId();

        try {
            existingSibling = readResource(dbc, project.getId(), resource.getRootPath(), true);

            if (existingSibling.getState() == CmsResource.STATE_DELETED) {
                // if an existing resource is deleted, it will be finally removed now.
                // but we have to reuse its id in order to avoid orphanes in the online project.
                newStructureId = existingSibling.getStructureId();
                newState = CmsResource.STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = readSiblings(dbc, project, existingSibling, false);
                int propertyDeleteOption = (existingSibling.getSiblingCount() > 1) ? CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES
                : CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                deletePropertyObjects(dbc, project.getId(), existingSibling, propertyDeleteOption);
                removeFile(dbc, project, existingSibling, true);

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

        if (existingSibling != null && existingSibling.getState() != CmsResource.STATE_DELETED) {
            // we have a collision: there exists already a resource with the same path/name which could not be removed
            throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(
                Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        // check if a resource with the specified ID already exists
        if (!validateResourceIdExists(dbc, project.getId(), resource.getResourceId())) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_CREATE_SIBLING_FILE_NOT_FOUND_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        // write a new structure referring to the resource
        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());

            // read the parent id
            String parentId = internalReadParentId(dbc, project.getId(), resource.getRootPath());

            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, newStructureId.toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setString(3, resource.getRootPath());
            stmt.setInt(4, newState);
            stmt.setLong(5, resource.getDateReleased());
            stmt.setLong(6, resource.getDateExpired());
            stmt.setString(7, parentId);
            stmt.executeUpdate();

            m_sqlManager.closeAll(dbc, null, stmt, null);

            // update the link Count
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_SIBLING_COUNT");
            stmt.setInt(1, this.internalCountSiblings(dbc, project.getId(), resource.getResourceId()));
            stmt.setString(2, resource.getResourceId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(dbc, null, stmt, null);

            // update the resource flags
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_FLAGS");
            stmt.setInt(1, resource.getFlags());
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
     * @see org.opencms.db.I_CmsVfsDriver#deletePropertyDefinition(org.opencms.db.CmsDbContext, org.opencms.file.CmsPropertyDefinition)
     */
    public void deletePropertyDefinition(CmsDbContext dbc, CmsPropertyDefinition metadef) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (internalCountProperties(dbc, metadef, CmsProject.ONLINE_PROJECT_ID) != 0
                || internalCountProperties(dbc, metadef, Integer.MAX_VALUE) != 0) {

                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_DELETE_USED_PROPERTY_1,
                    metadef.getName()));
            }

            conn = m_sqlManager.getConnection(dbc);

            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    // delete the offline propertydef
                    stmt = m_sqlManager.getPreparedStatement(conn, Integer.MAX_VALUE, "C_PROPERTYDEF_DELETE");
                } else if (i == 1) {
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
     * @see org.opencms.db.I_CmsVfsDriver#deletePropertyObjects(org.opencms.db.CmsDbContext, int, org.opencms.file.CmsResource, int)
     */
    public void deletePropertyObjects(CmsDbContext dbc, int projectId, CmsResource resource, int deleteOption)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        String resourceName = resource.getRootPath();

        // add folder separator to folder name if it is not present
        if (resource.isFolder() && !resourceName.endsWith("/")) {
            resourceName += "/";
        }

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);

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

        finalize();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SHUTDOWN_DRIVER_1, getClass().getName()));
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
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_ASSIGNED_POOL_1, poolUrl));
        }

        if (successiveDrivers != null && !successiveDrivers.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_SUCCESSIVE_DRIVERS_UNSUPPORTED_1, getClass().getName()));
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
     * @see org.opencms.db.I_CmsVfsDriver#publishResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.file.CmsResource, boolean)
     */
    public void publishResource(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource onlineResource,
        CmsResource offlineResource,
        boolean writeFileContent) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        // validate the resource length
        internalValidateResourceLength(offlineResource);
        int resourceSize = offlineResource.getLength();

        String resourcePath = removeTrailingSeparator(offlineResource.getRootPath());

        try {
            conn = m_sqlManager.getConnection(dbc, onlineProject.getId());

            if (validateResourceIdExists(dbc, onlineProject.getId(), offlineResource.getResourceId())) {

                // the resource record exists online already

                if (writeFileContent && offlineResource.isFile()) {
                    // update the online file content
                    writeContent(
                        dbc,
                        onlineProject,
                        offlineResource.getResourceId(),
                        ((CmsFile)offlineResource).getContents());
                }

                // update the online resource record
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_UPDATE_RESOURCES");
                stmt.setInt(1, offlineResource.getTypeId());
                stmt.setInt(2, offlineResource.getFlags());
                stmt.setLong(3, offlineResource.getDateLastModified());
                stmt.setString(4, offlineResource.getUserLastModified().toString());
                stmt.setInt(5, CmsResource.STATE_UNCHANGED);
                stmt.setInt(6, resourceSize);
                stmt.setInt(7, offlineResource.getProjectLastModified());
                stmt.setInt(8, this.internalCountSiblings(dbc, onlineProject.getId(), onlineResource.getResourceId()));
                stmt.setString(9, offlineResource.getResourceId().toString());
                stmt.executeUpdate();

                m_sqlManager.closeAll(dbc, null, stmt, null);

                // read the parent id
                String parentId = internalReadParentId(dbc, onlineProject.getId(), resourcePath);

                // update the online structure record
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_UPDATE_STRUCTURE");
                stmt.setString(1, offlineResource.getResourceId().toString());
                stmt.setString(2, resourcePath);
                stmt.setInt(3, CmsResource.STATE_UNCHANGED);
                stmt.setLong(4, offlineResource.getDateReleased());
                stmt.setLong(5, offlineResource.getDateExpired());
                stmt.setString(6, parentId);
                stmt.setString(7, offlineResource.getStructureId().toString());
                stmt.executeUpdate();

            } else {

                // the resource record does NOT exist online yet
                if (writeFileContent && offlineResource.isFile()) {
                    // create the file content online
                    resourceSize = offlineResource.getLength();
                    createContent(
                        dbc,
                        onlineProject,
                        offlineResource.getResourceId(),
                        ((CmsFile)offlineResource).getContents(),
                        0);
                }

                // create the resource record online
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_WRITE");
                stmt.setString(1, offlineResource.getResourceId().toString());
                stmt.setInt(2, offlineResource.getTypeId());
                stmt.setInt(3, offlineResource.getFlags());
                stmt.setLong(4, offlineResource.getDateCreated());
                stmt.setString(5, offlineResource.getUserCreated().toString());
                stmt.setLong(6, offlineResource.getDateLastModified());
                stmt.setString(7, offlineResource.getUserLastModified().toString());
                stmt.setInt(8, CmsResource.STATE_UNCHANGED);
                stmt.setInt(9, resourceSize);
                stmt.setInt(10, offlineResource.getProjectLastModified());
                stmt.setInt(11, 1);
                stmt.executeUpdate();

                m_sqlManager.closeAll(dbc, null, stmt, null);

                // read the parent id
                String parentId = internalReadParentId(dbc, onlineProject.getId(), resourcePath);

                // create the structure record online
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_STRUCTURE_WRITE");
                stmt.setString(1, offlineResource.getStructureId().toString());
                stmt.setString(2, offlineResource.getResourceId().toString());
                stmt.setString(3, resourcePath);
                stmt.setInt(4, CmsResource.STATE_UNCHANGED);
                stmt.setLong(5, offlineResource.getDateReleased());
                stmt.setLong(6, offlineResource.getDateExpired());
                stmt.setString(7, parentId);
                stmt.executeUpdate();
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
     * @see org.opencms.db.I_CmsVfsDriver#readChildResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, boolean, boolean)
     */
    public List readChildResources(
        CmsDbContext dbc,
        CmsProject currentProject,
        CmsResource resource,
        boolean getFolders,
        boolean getFiles) throws CmsDataAccessException {

        List result = new ArrayList();
        int projectId = currentProject.getId();

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
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());
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
     * @see org.opencms.db.I_CmsVfsDriver#readFile(org.opencms.db.CmsDbContext, int, boolean, org.opencms.util.CmsUUID)
     */
    public CmsFile readFile(CmsDbContext dbc, int projectId, boolean includeDeleted, CmsUUID structureId)
    throws CmsDataAccessException {

        CmsFile file = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);

            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_FILES_READ");
            stmt.setString(1, structureId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                file = createFile(res, projectId);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_FILE_WITH_STRUCTURE_ID_1,
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
        if (file != null && file.getState() == org.opencms.file.CmsResource.STATE_DELETED && !includeDeleted) {
            throw new CmsVfsException(Messages.get().container(
                Messages.ERR_READ_DELETED_FILE_1,
                dbc.removeSiteRoot(file.getRootPath())));
        }

        return file;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolder(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID)
     */
    public CmsFolder readFolder(CmsDbContext dbc, int projectId, CmsUUID folderId) throws CmsDataAccessException {

        CmsFolder folder = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
     * @see org.opencms.db.I_CmsVfsDriver#readFolder(org.opencms.db.CmsDbContext, int, java.lang.String)
     */
    public CmsFolder readFolder(CmsDbContext dbc, int projectId, String folderPath) throws CmsDataAccessException {

        CmsFolder folder = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        folderPath = removeTrailingSeparator(folderPath);

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinition(org.opencms.db.CmsDbContext, java.lang.String, int)
     */
    public CmsPropertyDefinition readPropertyDefinition(CmsDbContext dbc, String name, int projectId)
    throws CmsDataAccessException {

        CmsPropertyDefinition propDef = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_READ");
            stmt.setString(1, name);
            res = stmt.executeQuery();

            // if resultset exists - return it
            if (res.next()) {
                propDef = new CmsPropertyDefinition(
                    new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_ID"))),
                    res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")));
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
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinitions(org.opencms.db.CmsDbContext, int)
     */
    public List readPropertyDefinitions(CmsDbContext dbc, int projectId) throws CmsDataAccessException {

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
                    res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME"))));
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

        String resourceName = resource.getRootPath();
        if ((resource.isFolder()) && (!resourceName.endsWith("/"))) {
            resourceName += "/";
        }
        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project.getId(), "C_PROPERTIES_READ");

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
        String propertyKey = null;
        String propertyValue = null;
        int mappingType = -1;
        Map propertyMap = new HashMap();
        CmsProperty property = null;

        String resourceName = resource.getRootPath();
        if ((resource.isFolder()) && (!resourceName.endsWith("/"))) {
            resourceName += "/";
        }

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project.getId(), "C_PROPERTIES_READALL");
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

                if ((property = (CmsProperty)propertyMap.get(propertyKey)) == null) {
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
     * @see org.opencms.db.I_CmsVfsDriver#readResource(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID, boolean)
     */
    public CmsResource readResource(CmsDbContext dbc, int projectId, CmsUUID structureId, boolean includeDeleted)
    throws CmsDataAccessException {

        CmsResource resource = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
        if (resource != null && resource.getState() == org.opencms.file.CmsResource.STATE_DELETED && !includeDeleted) {
            throw new CmsVfsException(Messages.get().container(
                Messages.ERR_READ_DELETED_RESOURCE_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        return resource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResource(org.opencms.db.CmsDbContext, int, java.lang.String, boolean)
     */
    public CmsResource readResource(CmsDbContext dbc, int projectId, String path, boolean includeDeleted)
    throws CmsDataAccessException {

        CmsResource resource = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        // must remove trailing slash
        path = removeTrailingSeparator(path);

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
        if (resource != null && resource.getState() == CmsResource.STATE_DELETED && !includeDeleted) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_READ_DELETED_RESOURCE_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        return resource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResources(org.opencms.db.CmsDbContext, int, int, int)
     */
    public List readResources(CmsDbContext dbc, int projectId, int state, int mode) throws CmsDataAccessException {

        List result = new ArrayList();

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            if (mode == CmsDriverManager.READMODE_MATCHSTATE) {
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    projectId,
                    "C_RESOURCES_GET_RESOURCE_IN_PROJECT_WITH_STATE");
                stmt.setInt(1, projectId);
                stmt.setInt(2, state);
                stmt.setInt(3, state);
                stmt.setInt(4, state);
                stmt.setInt(5, state);
            } else if (mode == CmsDriverManager.READMODE_UNMATCHSTATE) {
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    projectId,
                    "C_RESOURCES_GET_RESOURCE_IN_PROJECT_WITHOUT_STATE");
                stmt.setInt(1, projectId);
                stmt.setInt(2, state);
                stmt.setInt(3, state);
            } else {
                stmt = m_sqlManager.getPreparedStatement(
                    conn,
                    projectId,
                    "C_RESOURCES_GET_RESOURCE_IN_PROJECT_IGNORE_STATE");
                stmt.setInt(1, projectId);
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
     * @see org.opencms.db.I_CmsVfsDriver#readResourcesWithProperty(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID, String)
     */
    public List readResourcesWithProperty(CmsDbContext dbc, int projectId, CmsUUID propertyDef, String path)
    throws CmsDataAccessException {

        List resources = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF");
            stmt.setString(1, propertyDef.toString());
            stmt.setString(2, path + "%");
            stmt.setString(3, propertyDef.toString());
            stmt.setString(4, path + "%");
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
     * @see org.opencms.db.I_CmsVfsDriver#readResourcesWithProperty(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID, String, String)
     */
    public List readResourcesWithProperty(
        CmsDbContext dbc,
        int projectId,
        CmsUUID propertyDef,
        String path,
        String value) throws CmsDataAccessException {

        List resources = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF_VALUE");
            stmt.setString(1, propertyDef.toString());
            stmt.setString(2, path + "%");
            stmt.setString(3, "%" + value + "%");
            stmt.setString(4, propertyDef.toString());
            stmt.setString(5, "%" + path + "%");
            stmt.setString(6, value + "%");
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
     * @see org.opencms.db.I_CmsVfsDriver#readResourceTree(org.opencms.db.CmsDbContext, int, java.lang.String, int, int, long, long, int)
     */
    public List readResourceTree(
        CmsDbContext dbc,
        int projectId,
        String parentPath,
        int type,
        int state,
        long startTime,
        long endTime,
        int mode) throws CmsDataAccessException {

        List result = new ArrayList();

        StringBuffer conditions = new StringBuffer();
        List params = new ArrayList(5);

        // prepare the selection criteria
        prepareProjectCondition(projectId, mode, conditions, params);
        prepareResourceCondition(projectId, mode, conditions);
        prepareTypeCondition(projectId, type, mode, conditions, params);
        prepareTimeRangeCondition(projectId, startTime, endTime, conditions, params);
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
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());

            if (includeDeleted) {
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_SELECT_VFS_SIBLINGS");
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_SELECT_NONDELETED_VFS_SIBLINGS");
            }

            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                currentResource = createFile(res, currentProject.getId(), false);
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
     * @see org.opencms.db.I_CmsVfsDriver#removeFile(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, boolean)
     */
    public void removeFile(CmsDbContext dbc, CmsProject currentProject, CmsResource resource, boolean removeFileContent)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        int siblingCount = 0;

        try {
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());

            // delete the structure recourd
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_STRUCTURE_DELETE_BY_STRUCTUREID");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(dbc, null, stmt, null);

            // count the references to the resource
            siblingCount = internalCountSiblings(dbc, currentProject.getId(), resource.getResourceId());

            if (siblingCount > 0) {

                // update the link Count
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCES_UPDATE_SIBLING_COUNT");
                stmt.setInt(1, this.internalCountSiblings(dbc, currentProject.getId(), resource.getResourceId()));
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

                // delete content records with this resource id
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_FILE_CONTENT_DELETE");
                stmt.setString(1, resource.getResourceId().toString());
                stmt.executeUpdate();
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

        // the current implementation only deletes empty folders

        // check if the folder has any files in it
        List files = readChildResources(dbc, currentProject, resource, false, true);

        // remove deleted files from the child resources
        if (files.size() == 0) {

            // check if the folder has any folders in it
            List folders = readChildResources(dbc, currentProject, resource, true, false);

            // remove deleted folders from the child resources
            if (folders.size() == 0) {
                internalRemoveFolder(dbc, currentProject, resource);
            } else {
                String errorResNames = "";

                for (int i = 0, n = folders.size(); i < n; i++) {
                    CmsResource errorRes = (CmsResource)folders.get(i);
                    errorResNames += "[" + dbc.removeSiteRoot(errorRes.getRootPath()) + "]";
                }

                throw new CmsVfsException(Messages.get().container(
                    Messages.ERR_DELETE_NONEMTY_FOLDER_2,
                    dbc.removeSiteRoot(resource.getRootPath()),
                    errorResNames));
            }
        } else {
            String errorResNames = "";

            for (int i = 0, n = files.size(); i < n; i++) {
                CmsResource errorRes = (CmsResource)files.get(i);
                errorResNames += "[" + errorRes.getName() + "]";
            }

            throw new CmsVfsException(Messages.get().container(
                Messages.ERR_DELETE_NONEMTY_FOLDER_2,
                dbc.removeSiteRoot(resource.getRootPath()),
                errorResNames));
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#replaceResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, byte[], int)
     */
    public void replaceResource(CmsDbContext dbc, CmsResource newResource, byte[] resContent, int newResourceType)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // write the file content
            if (resContent != null) {
                writeContent(dbc, dbc.currentProject(), newResource.getResourceId(), resContent);
            }

            // update the resource record
            conn = m_sqlManager.getConnection(dbc, dbc.currentProject().getId());
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
     * @see org.opencms.db.I_CmsVfsDriver#validateResourceIdExists(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID)
     */
    public boolean validateResourceIdExists(CmsDbContext dbc, int projectId, CmsUUID resourceId)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean exists = false;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
     * @see org.opencms.db.I_CmsVfsDriver#validateStructureIdExists(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID)
     */
    public boolean validateStructureIdExists(CmsDbContext dbc, int projectId, CmsUUID structureId)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean found = false;
        int count = 0;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
     * @see org.opencms.db.I_CmsVfsDriver#writeContent(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, byte[])
     */
    public void writeContent(CmsDbContext dbc, CmsProject project, CmsUUID resourceId, byte[] content)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_CONTENTS_UPDATE");

            // update the file content in the FILES database.
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
     * @see org.opencms.db.I_CmsVfsDriver#writeLastModifiedProjectId(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, int, org.opencms.file.CmsResource)
     */
    public void writeLastModifiedProjectId(CmsDbContext dbc, CmsProject project, int projectId, CmsResource resource)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_PROJECT_LASTMODIFIED");
            stmt.setInt(1, projectId);
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

        CmsPropertyDefinition propertyDefinition = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String value = null;
        int mappingType = -1;
        CmsUUID id = null;
        CmsProperty existingProperty = null;
        boolean existsPropertyValue = false;
        boolean deletePropertyValue = false;

        try {
            // read the property definition
            propertyDefinition = readPropertyDefinition(dbc, property.getName(), project.getId());
        } catch (CmsDbEntryNotFoundException e) {
            propertyDefinition = null;
        }

        if (propertyDefinition == null) {
            if (property.autoCreatePropertyDefinition()) {
                propertyDefinition = createPropertyDefinition(dbc, project.getId(), property.getName());

                try {
                    readPropertyDefinition(dbc, property.getName(), CmsProject.ONLINE_PROJECT_ID);
                } catch (CmsDataAccessException e) {
                    createPropertyDefinition(dbc, CmsProject.ONLINE_PROJECT_ID, property.getName());
                }

                try {
                    m_driverManager.getBackupDriver().readBackupPropertyDefinition(dbc, property.getName());
                } catch (CmsDataAccessException e) {
                    m_driverManager.getBackupDriver().createBackupPropertyDefinition(dbc, property.getName());
                }
            } else {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_NO_PROPERTYDEF_WITH_NAME_1,
                    property.getName()));
            }
        }

        String resourceName = resource.getRootPath();
        if (resource.isFolder() && !resourceName.endsWith("/")) {
            resourceName += "/";
        }

        try {
            // read the existing property to test if we need the 
            // insert or update query to write a property value
            existingProperty = readPropertyObject(dbc, propertyDefinition.getName(), project, resource);

            if (existingProperty.isIdentical(property)) {

                // property already has the identical values set, no write required
                return;
            }

            conn = m_sqlManager.getConnection(dbc, project.getId());

            for (int i = 0; i < 2; i++) {
                mappingType = -1;
                value = null;
                id = null;
                existsPropertyValue = false;
                deletePropertyValue = false;

                // 1) take any required decisions to choose and fill the correct SQL query

                if (i == 0) {
                    // write/delete the *structure value* on the first cycle
                    if (existingProperty.getStructureValue() != null && property.deleteStructureValue()) {
                        // this property value is marked to be deleted
                        deletePropertyValue = true;
                    } else {
                        value = property.getStructureValue();
                        if (value == null || (value != null && "".equalsIgnoreCase(value.trim()))) {
                            // no structure value set or the structure value is an empty string, 
                            // continue with the resource value
                            continue;
                        }
                    }

                    // set the vars to be written to the database
                    mappingType = CmsProperty.STRUCTURE_RECORD_MAPPING;
                    id = resource.getStructureId();
                    existsPropertyValue = existingProperty.getStructureValue() != null;
                } else if (i == 1) {
                    // write/delete the *resource value* on the second cycle
                    if (existingProperty.getResourceValue() != null && property.deleteResourceValue()) {
                        // this property value is marked to be deleted
                        deletePropertyValue = true;
                    } else {
                        value = property.getResourceValue();
                        if (value == null || (value != null && "".equalsIgnoreCase(value.trim()))) {
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

                if (!deletePropertyValue) {
                    // insert/update the property value                    
                    if (existsPropertyValue) {
                        // {structure|resource} property value already exists- use update statement
                        stmt = m_sqlManager.getPreparedStatement(conn, project.getId(), "C_PROPERTIES_UPDATE");
                        stmt.setString(1, m_sqlManager.validateEmpty(value));
                        stmt.setString(2, id.toString());
                        stmt.setInt(3, mappingType);
                        stmt.setString(4, propertyDefinition.getId().toString());
                    } else {
                        // {structure|resource} property value doesen't exist- use create statement
                        stmt = m_sqlManager.getPreparedStatement(conn, project.getId(), "C_PROPERTIES_CREATE");
                        stmt.setString(1, new CmsUUID().toString());
                        stmt.setString(2, propertyDefinition.getId().toString());
                        stmt.setString(3, id.toString());
                        stmt.setInt(4, mappingType);
                        stmt.setString(5, m_sqlManager.validateEmpty(value));
                    }
                } else {
                    // {structure|resource} property value marked as deleted- use delete statement
                    stmt = m_sqlManager.getPreparedStatement(conn, project.getId(), "C_PROPERTIES_DELETE");
                    stmt.setString(1, propertyDefinition.getId().toString());
                    stmt.setString(2, id.toString());
                    stmt.setInt(3, mappingType);
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

        int structureState = resource.getState();
        int resourceState = resource.getState();
        int projectId = project.getId();

        if (changed == CmsDriverManager.UPDATE_RESOURCE_STATE) {
            resourceState = org.opencms.file.CmsResource.STATE_CHANGED;
        } else if (changed == CmsDriverManager.UPDATE_STRUCTURE_STATE) {
            structureState = org.opencms.file.CmsResource.STATE_CHANGED;
        } else if (changed == CmsDriverManager.NOTHING_CHANGED) {
            projectId = resource.getProjectLastModified();
        } else {
            resourceState = org.opencms.file.CmsResource.STATE_CHANGED;
            structureState = org.opencms.file.CmsResource.STATE_CHANGED;
        }

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCES");
            stmt.setInt(1, resource.getTypeId());
            stmt.setInt(2, resource.getFlags());
            stmt.setLong(3, resourceDateModified);
            stmt.setString(4, resource.getUserLastModified().toString());
            stmt.setInt(5, resourceState);
            stmt.setInt(6, resource.getLength());
            stmt.setInt(7, projectId);
            stmt.setInt(8, internalCountSiblings(dbc, project.getId(), resource.getResourceId()));
            stmt.setString(9, resource.getResourceId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(dbc, null, stmt, null);

            // read the parent id
            String parentId = internalReadParentId(dbc, project.getId(), resourcePath);

            // update the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_STRUCTURE");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.setString(2, resourcePath);
            stmt.setInt(3, structureState);
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
     * @see org.opencms.db.I_CmsVfsDriver#writeResourceState(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, int)
     */
    public void writeResourceState(CmsDbContext dbc, CmsProject project, CmsResource resource, int changed)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        if (project.getId() == CmsProject.ONLINE_PROJECT_ID) {
            return;
        }

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());

            // Refactor I_CmsRuntimeInfo in signature to Object
            // rename 'runtimeInfo' to 'param'

            if (changed == CmsDriverManager.UPDATE_RESOURCE) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCE_STATELASTMODIFIED");
                stmt.setInt(1, resource.getState());
                stmt.setLong(2, resource.getDateLastModified());
                stmt.setString(3, resource.getUserLastModified().toString());
                stmt.setInt(4, project.getId());
                stmt.setString(5, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);

                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RELEASE_EXPIRED");
                stmt.setLong(1, resource.getDateReleased());
                stmt.setLong(2, resource.getDateExpired());
                stmt.setString(3, resource.getStructureId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }

            if (changed == CmsDriverManager.UPDATE_RESOURCE_STATE || changed == CmsDriverManager.UPDATE_ALL) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCE_STATE");
                stmt.setInt(1, resource.getState());
                stmt.setInt(2, project.getId());
                stmt.setString(3, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }

            if (changed == CmsDriverManager.UPDATE_STRUCTURE
                || changed == CmsDriverManager.UPDATE_STRUCTURE_STATE
                || changed == CmsDriverManager.UPDATE_ALL) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_STRUCTURE_STATE");
                stmt.setInt(1, resource.getState());
                stmt.setString(2, resource.getStructureId().toString());
                stmt.executeUpdate();
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
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        try {
            m_sqlManager = null;
            m_driverManager = null;
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
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
    protected int internalCountProperties(CmsDbContext dbc, CmsPropertyDefinition propertyDefinition, int projectId)
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
    protected int internalCountSiblings(CmsDbContext dbc, int projectId, CmsUUID resourceId)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        int count = 0;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);

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
     * Returns the parent id of the given resource.<p>
     * 
     * @param dbc the current database context
     * @param projectId the current project id 
     * @param resourcename the resource name to read the parent id for
     * @return  the parent id of the given resource
     * @throws CmsDataAccessException if something goes wrong
     */
    protected String internalReadParentId(CmsDbContext dbc, int projectId, String resourcename)
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
            conn = m_sqlManager.getConnection(dbc, projectId);
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
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());

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
     * Appends the appropriate selection criteria related with the parentPath.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param parent the parent path or UUID (if mode is C_READMODE_EXCLUDE_TREE)
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection params
     */
    private void preparePathCondition(int projectId, String parent, int mode, StringBuffer conditions, List params) {

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
            // if root folder is parent, no additional condition is needed since all resource match anyway
            return;
        }

        // add condition to read path subtree        
        conditions.append(BEGIN_INCLUDE_CONDITION);
        conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_PATH_PREFIX"));
        conditions.append(END_CONDITION);
        params.add(addTrailingSeparator(parent) + "%");
    }

    /**
     * Appends the appropriate selection criteria related with the projectId.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection params
     */
    private void prepareProjectCondition(int projectId, int mode, StringBuffer conditions, List params) {

        if ((mode & CmsDriverManager.READMODE_INCLUDE_PROJECT) > 0) {
            // C_READMODE_INCLUDE_PROJECT: add condition to match the PROJECT_ID
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_PROJECT_LASTMODIFIED"));
            conditions.append(END_CONDITION);
            params.add(String.valueOf(projectId));
        }
    }

    /**
     * Appends the appropriate selection criteria related with the read mode.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     */
    private void prepareResourceCondition(int projectId, int mode, StringBuffer conditions) {

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
    private void prepareStateCondition(int projectId, int state, int mode, StringBuffer conditions, List params) {

        if (state != CmsDriverManager.READ_IGNORE_STATE) {
            if ((mode & CmsDriverManager.READMODE_EXCLUDE_STATE) > 0) {
                // C_READ_MODIFIED_STATES: add condition to match against any state but not given state
                conditions.append(BEGIN_EXCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_STATE"));
                conditions.append(END_CONDITION);
                params.add(String.valueOf(state));
                params.add(String.valueOf(state));
            } else {
                // otherwise add condition to match against given state if neccessary
                conditions.append(BEGIN_INCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_STATE"));
                conditions.append(END_CONDITION);
                params.add(String.valueOf(state));
                params.add(String.valueOf(state));
            }
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
    private void prepareTimeRangeCondition(
        int projectId,
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
    private void prepareTypeCondition(int projectId, int type, int mode, StringBuffer conditions, List params) {

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

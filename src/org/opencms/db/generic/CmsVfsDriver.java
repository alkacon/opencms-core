/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsVfsDriver.java,v $
 * Date   : $Date: 2003/10/01 07:58:17 $
 * Version: $Revision: 1.140 $
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

package org.opencms.db.generic;

import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsAdjacencyTree;
import org.opencms.util.CmsUUID;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsPropertydefinition;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsResourceTypeFolder;
import com.opencms.file.CmsUser;
import com.opencms.file.I_CmsResourceType;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * Generic (ANSI-SQL) database server implementation of the VFS driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * @version $Revision: 1.140 $ $Date: 2003/10/01 07:58:17 $
 * @since 5.1
 */
public class CmsVfsDriver extends Object implements I_CmsDriver, I_CmsVfsDriver {

    protected CmsDriverManager m_driverManager;
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(com.opencms.file.CmsProject, com.opencms.file.CmsFile, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, java.lang.String)
     */
    public CmsFile createFile(CmsProject project, CmsFile file, CmsUUID userId, CmsUUID parentId, String filename) throws CmsException {
        int newState = 0;
        CmsUUID modifiedByUserId = null, createdByUserId = null, newStructureId = null;
        long dateModified = 0, dateCreated = 0;
        Connection conn = null;
        PreparedStatement stmt = null;

        // validate if the file has it's full resource name set
        if (!file.hasFullResourceName()) {
            throw new RuntimeException("Full resource name not set for CmsFile " + file.getName());
        }

        // validate the resource name
        if (filename.length() > I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME) {
            throw new CmsException("The resource name '" + filename + "' is too long! (max. allowed length must be <= " + I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME + " chars.!)", CmsException.C_BAD_NAME);
        }

        // force some attribs when creating or publishing a file 
        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            newState = I_CmsConstants.C_STATE_UNCHANGED;

            dateCreated = file.getDateCreated();
            createdByUserId = file.getUserCreated();

            dateModified = file.getDateLastModified();
            modifiedByUserId = file.getUserLastModified();
        } else {
            newState = I_CmsConstants.C_STATE_NEW;

            dateCreated = System.currentTimeMillis();
            createdByUserId = userId;

            dateModified = dateCreated;
            modifiedByUserId = createdByUserId;
        }

        // check if the resource already exists
        CmsResource res = null;
        newStructureId = file.getStructureId();
        try {
            res = readFileHeader(project.getId(), parentId, filename, true);
            res.setFullResourceName(file.getRootPath());
            if (res.getState() == I_CmsConstants.C_STATE_DELETED) {
                // if an existing resource is deleted, it will be finally removed now
                // but we have to reuse its id in order to avoid orphanes in the online project
                newStructureId = res.getStructureId();
                newState = I_CmsConstants.C_STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = readSiblings(project, res);
                deleteProperties(project.getId(), res);
                removeFile(project, res, true);

                OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCES_MODIFIED, Collections.singletonMap("resources", modifiedResources)));
                OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", res)));
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
            }
        } catch (CmsException e) {
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                // we have a collision which has to be handled in the app.
                throw e;
            }
        }

        try {
            conn = m_sqlManager.getConnection(project);

            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, newStructureId.toString());
            stmt.setString(2, parentId.toString());
            stmt.setString(3, file.getResourceId().toString());
            stmt.setString(4, filename);
            stmt.setInt(5, newState);
            stmt.executeUpdate();

            m_sqlManager.closeAll(null, stmt, null);

            if (!validateResourceIdExists(project.getId(), file.getResourceId())) {

                // write the resource
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_WRITE");
                stmt.setString(1, file.getResourceId().toString());
                stmt.setInt(2, file.getType());
                stmt.setInt(3, file.getFlags());
                stmt.setString(4, file.getFileId().toString());
                stmt.setInt(5, file.getLoaderId());
                stmt.setTimestamp(6, new Timestamp(dateCreated));
                stmt.setString(7, createdByUserId.toString());
                stmt.setTimestamp(8, new Timestamp(dateModified));
                stmt.setString(9, modifiedByUserId.toString());
                stmt.setInt(10, newState);
                stmt.setInt(11, file.getLength());
                stmt.setString(12, CmsUUID.getNullUUID().toString());
                stmt.setInt(13, project.getId());
                stmt.setInt(14, 1);
                stmt.executeUpdate();

                // write the content
                createFileContent(file.getFileId(), file.getContents(), 0, project.getId(), false);
            } else {

                // update the link Count
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_LINK_COUNT");
                stmt.setInt(1, this.internalCountSiblings(project.getId(), file.getResourceId()));
                stmt.setString(2, file.getResourceId().toString());
                stmt.executeUpdate();

                m_sqlManager.closeAll(null, stmt, null);

                // update the resource flags
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_FLAGS");
                stmt.setInt(1, file.getFlags());
                stmt.setString(2, file.getResourceId().toString());
                stmt.executeUpdate();

                //updateResourcestate(file, CmsDriverManager.C_UPDATE_RESOURCE);        
                writeFileContent(file.getFileId(), file.getContents(), project.getId(), false);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readFile(project.getId(), false, newStructureId);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(com.opencms.file.CmsUser, com.opencms.file.CmsProject, java.lang.String, int, com.opencms.file.CmsFolder, byte[], com.opencms.file.I_CmsResourceType)
     */
    public CmsFile createFile(CmsUser user, CmsProject project, String filename, int flags, CmsFolder parentFolder, byte[] contents, I_CmsResourceType resourceType) throws CmsException {

        CmsFile newFile = new CmsFile(new CmsUUID(), new CmsUUID(), parentFolder.getStructureId(), new CmsUUID(), filename, resourceType.getResourceType(), flags, project.getId(), com.opencms.core.I_CmsConstants.C_STATE_NEW, resourceType.getLoaderId(), 0, user.getId(), 0, user.getId(), contents.length, 1, contents);

        newFile.setFullResourceName(parentFolder.getRootPath() + newFile.getName());
        return createFile(project, newFile, user.getId(), parentFolder.getStructureId(), filename);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(java.sql.ResultSet, int)
     */
    public CmsFile createFile(ResultSet res, int projectId) throws SQLException, CmsException {
        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_PARENT_ID")));
        int resourceType = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));
        String resourceName = res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_NAME"));
        int resourceFlags = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_FLAGS"));
        CmsUUID fileId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_FILE_ID")));
        int resourceState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STATE"));
        int structureState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_STATE"));
        int loaderId = res.getInt(m_sqlManager.readQuery("C_RESOURCES_LOADER_ID"));
        long dateCreated = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED")).getTime();
        long dateLastModified = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
        int resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        byte[] content = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
        int linkCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_LINK_COUNT"));

        int newState = (structureState > resourceState) ? structureState : resourceState;

        return new CmsFile(structureId, resourceId, parentId, fileId, resourceName, resourceType, resourceFlags, projectId, newState, loaderId, dateCreated, userCreated, dateLastModified, userLastModified, resourceSize, linkCount, content);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(java.sql.ResultSet, int, boolean)
     */
    public CmsFile createFile(ResultSet res, int projectId, boolean hasFileContentInResultSet) throws SQLException, CmsException {
        byte[] content = null;
        int resProjectId = I_CmsConstants.C_UNKNOWN_ID;

        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_PARENT_ID")));
        String resourceName = res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_NAME"));
        int resourceType = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));
        int resourceFlags = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_FLAGS"));
        CmsUUID fileId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_FILE_ID")));
        int resourceState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STATE"));
        int structureState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_STATE"));
        int loaderId = res.getInt(m_sqlManager.readQuery("C_RESOURCES_LOADER_ID"));
        long dateCreated = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED")).getTime();
        long dateLastModified = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
        int resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        int lockedInProject = res.getInt("LOCKED_IN_PROJECT");
        int linkCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_LINK_COUNT"));

        if (hasFileContentInResultSet) {
            content = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
        } else {
            content = new byte[0];
        }

        resProjectId = lockedInProject;

        int newState = (structureState > resourceState) ? structureState : resourceState;

        return new CmsFile(structureId, resourceId, parentId, fileId, resourceName, resourceType, resourceFlags, resProjectId, newState, loaderId, dateCreated, userCreated, dateLastModified, userLastModified, resourceSize, linkCount, content);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFileContent(org.opencms.util.CmsUUID, byte[], int, int, boolean)
     */
    public void createFileContent(CmsUUID fileId, byte[] fileContent, int versionId, int projectId, boolean writeBackup) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (writeBackup) {
                conn = m_sqlManager.getConnectionForBackup();
                stmt = m_sqlManager.getPreparedStatement(conn, "C_FILES_WRITE_BACKUP");
            } else {
                conn = m_sqlManager.getConnection(projectId);
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_FILES_WRITE");
            }

            stmt.setString(1, fileId.toString());

            if (fileContent.length < 2000) {
                stmt.setBytes(2, fileContent);
            } else {
                stmt.setBinaryStream(2, new ByteArrayInputStream(fileContent), fileContent.length);
            }

            if (writeBackup) {
                stmt.setInt(3, versionId);
                stmt.setString(4, new CmsUUID().toString());
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFolder(com.opencms.file.CmsProject, com.opencms.file.CmsFolder, org.opencms.util.CmsUUID)
     */
    public CmsFolder createFolder(CmsProject project, CmsFolder folder, CmsUUID parentId) throws CmsException {
        int state = 0;

        // validate the resource name
        if (folder.getName().length() > I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME) {
            throw new CmsException("The resource name '" + folder.getName() + "' is too long! (max. allowed length must be <= " + I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME + " chars.!)", CmsException.C_BAD_NAME);
        }

        // adjust the last-modified/creation dates
        long dateModified = folder.getDateLastModified();
        if (dateModified == 0) {
            dateModified = System.currentTimeMillis();
        }

        long dateCreated = folder.getDateCreated();
        if (dateCreated == 0) {
            dateCreated = System.currentTimeMillis();
        }

        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            state = folder.getState();
        } else {
            state = I_CmsConstants.C_STATE_NEW;
        }

        // prove if a deleted folder with the same name inside the same folder exists
        try {
            CmsFolder oldFolder = readFolder(project.getId(), parentId, folder.getName());
            if (oldFolder.getState() == I_CmsConstants.C_STATE_DELETED) {
                throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
            } else {
                if (oldFolder != null) {
                    throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
                }
            }
        } catch (CmsException e) {
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                throw e;
            }
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(project);

            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, folder.getStructureId().toString());
            stmt.setString(2, parentId.toString());
            stmt.setString(3, folder.getResourceId().toString());
            stmt.setString(4, folder.getName());
            stmt.setInt(5, state);
            stmt.executeUpdate();

            m_sqlManager.closeAll(null, stmt, null);

            if (!validateResourceIdExists(project.getId(), folder.getResourceId())) {

                // write the resource
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_WRITE");
                stmt.setString(1, folder.getResourceId().toString());
                stmt.setInt(2, folder.getType());
                stmt.setInt(3, folder.getFlags());
                stmt.setString(4, CmsUUID.getNullUUID().toString());
                stmt.setInt(5, folder.getLoaderId());
                stmt.setTimestamp(6, new Timestamp(dateCreated));
                stmt.setString(7, folder.getUserCreated().toString());
                stmt.setTimestamp(8, new Timestamp(dateModified));
                stmt.setString(9, folder.getUserLastModified().toString());
                stmt.setInt(10, state);
                stmt.setInt(11, folder.getLength());
                stmt.setString(12, CmsUUID.getNullUUID().toString());
                stmt.setInt(13, project.getId());
                stmt.setInt(14, 1);
                stmt.executeUpdate();

            } else {

                // update the link Count
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_LINK_COUNT");
                stmt.setInt(1, this.internalCountSiblings(project.getId(), folder.getResourceId()));
                stmt.setString(2, folder.getResourceId().toString());
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        // if this is the rootfolder or if the parentfolder is the rootfolder
        // try to create the projectresource
        String parentFolderName = "/";
        if (!folder.getName().equals(I_CmsConstants.C_ROOT)) {
            parentFolderName = folder.getName();
            if (parentFolderName.endsWith("/"))
                parentFolderName = parentFolderName.substring(0, parentFolderName.length() - 1);
            parentFolderName = parentFolderName.substring(0, parentFolderName.lastIndexOf("/") + 1);
        }

        if (parentId.isNullUUID() || parentFolderName.equals(I_CmsConstants.C_ROOT)) {
            try {
                String rootFolder = null;
                try {
                    rootFolder = m_driverManager.getProjectDriver().readProjectResource(project.getId(), I_CmsConstants.C_ROOT);
                } catch (CmsException exc) {
                    // NOOP
                }

                if (rootFolder == null) {
                    m_driverManager.getProjectDriver().createProjectResource(project.getId(), folder.getName());
                }

                //createProjectResource(project.getId(), foldername);
            } catch (CmsException e) {
                if (e.getType() != CmsException.C_FILE_EXISTS) {
                    throw e;
                }
            }
        }

        return readFolder(project.getId(), folder.getStructureId());
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFolder(com.opencms.file.CmsProject, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, java.lang.String, int, long, org.opencms.util.CmsUUID, long, org.opencms.util.CmsUUID)
     */
    public CmsFolder createFolder(CmsProject project, CmsUUID parentId, CmsUUID fileId, String folderName, int flags, long dateLastModified, CmsUUID userLastModified, long dateCreated, CmsUUID userCreated) throws CmsException {

        CmsFolder newFolder = new CmsFolder(new CmsUUID(), new CmsUUID(), parentId, CmsUUID.getNullUUID(), folderName, CmsResourceTypeFolder.C_RESOURCE_TYPE_ID, flags, project.getId(), com.opencms.core.I_CmsConstants.C_STATE_NEW, dateCreated, userCreated, dateLastModified, userLastModified, 1);

        return createFolder(project, newFolder, parentId);

    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFolder(java.sql.ResultSet, int, boolean)
     */
    public CmsFolder createFolder(ResultSet res, int projectId, boolean hasProjectIdInResultSet) throws SQLException {
        int resProjectId = I_CmsConstants.C_UNKNOWN_ID;

        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_PARENT_ID")));
        String resourceName = res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_NAME"));
        int resourceType = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));
        int resourceFlags = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_FLAGS"));
        CmsUUID fileId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_FILE_ID")));
        int resourceState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STATE"));
        int structureState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_STATE"));
        CmsUUID lockedBy = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_LOCKED_BY")));
        long dateCreated = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED")).getTime();
        long dateLastModified = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        int lockedInProject = res.getInt("LOCKED_IN_PROJECT");
        int linkCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_LINK_COUNT"));

        // TODO VFS links: refactor all upper methods to support the VFS link type param 

        if (!lockedBy.equals(CmsUUID.getNullUUID())) {
            // resource is locked
            resProjectId = lockedInProject;
        } else {
            // resource is not locked
            resProjectId = projectId;
            lockedInProject = projectId;
        }

        int newState = (structureState > resourceState) ? structureState : resourceState;

        return new CmsFolder(structureId, resourceId, parentId, fileId, resourceName, resourceType, resourceFlags, resProjectId, newState, dateCreated, userCreated, dateLastModified, userLastModified, linkCount);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createPropertyDefinition(java.lang.String, int, int)
     */
    public CmsPropertydefinition createPropertyDefinition(String name, int projectId, int resourcetype) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_CREATE");
            stmt.setInt(1, m_sqlManager.nextId(m_sqlManager.readQuery("C_TABLE_PROPERTYDEF")));
            stmt.setString(2, name);
            stmt.setInt(3, resourcetype);
            stmt.executeUpdate();         
                        
            /*
            for (int i = 0; i < 3; i++) {
                if (i == 0) {
                    // create the offline property definition
                    conn = m_sqlManager.getConnection();
                    stmt = m_sqlManager.getPreparedStatement(conn, Integer.MAX_VALUE, "C_PROPERTYDEF_CREATE");
                    stmt.setInt(1, m_sqlManager.nextId(m_sqlManager.readQuery("C_TABLE_PROPERTYDEF")));
                } else if (i == 1) {
                    // create the online property definition
                    conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                    stmt = m_sqlManager.getPreparedStatement(conn, I_CmsConstants.C_PROJECT_ONLINE_ID, "C_PROPERTYDEF_CREATE");
                    stmt.setInt(1, m_sqlManager.nextId(m_sqlManager.readQuery("C_TABLE_PROPERTYDEF_ONLINE")));
                } else {
                    // create the backup property definition
                    conn = m_sqlManager.getConnectionForBackup();
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_CREATE_BACKUP");
                    stmt.setInt(1, m_sqlManager.nextId(m_sqlManager.readQuery("C_TABLE_PROPERTYDEF_BACKUP")));
                }
                stmt.setString(2, name);
                stmt.setInt(3, resourcetype);
                stmt.executeUpdate();
                m_sqlManager.closeAll(conn, stmt, null);
            }
            */
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readPropertyDefinition(name, projectId, resourcetype);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createResource(java.sql.ResultSet, int)
     */
    public CmsResource createResource(ResultSet res, int projectId) throws SQLException, CmsException {

        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_PARENT_ID")));
        String resourceName = res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_NAME"));
        int resourceType = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));
        int resourceFlags = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_FLAGS"));
        int resourceProjectId = res.getInt(m_sqlManager.readQuery("C_RESOURCES_PROJECT_ID"));
        CmsUUID fileId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_FILE_ID")));
        int resourceState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STATE"));
        int structureState = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_STATE"));
        int loaderId = res.getInt(m_sqlManager.readQuery("C_RESOURCES_LOADER_ID"));
        long dateCreated = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED")).getTime();
        long dateLastModified = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
        int resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        int linkCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_LINK_COUNT"));

        int newState = (structureState > resourceState) ? structureState : resourceState;

        CmsResource newResource = new CmsResource(structureId, resourceId, parentId, fileId, resourceName, resourceType, resourceFlags, resourceProjectId, newState, loaderId, dateCreated, userCreated, dateLastModified, userLastModified, resourceSize, linkCount);

        return newResource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createSibling(com.opencms.file.CmsProject, com.opencms.file.CmsResource, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, java.lang.String)
     */
    public CmsResource createSibling(CmsProject project, CmsResource resource, CmsUUID userId, CmsUUID parentId, String filename) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        int newState = 0;

        // validate if the file has it's full resource name set
        if (!resource.hasFullResourceName()) {
            throw new RuntimeException("Full resource name not set for CmsResource " + resource.getName());
        }

        // validate the resource name
        if (filename.length() > I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME) {
            throw new CmsException("The resource name '" + filename + "' is too long! (max. allowed length must be <= " + I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME + " chars.!)", CmsException.C_BAD_NAME);
        }

        // force some attribs when creating or publishing a file 
        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            newState = I_CmsConstants.C_STATE_UNCHANGED;
        } else {
            newState = I_CmsConstants.C_STATE_NEW;
        }

        // check if the resource already exists
        CmsResource res = null;
        CmsUUID newStructureId = resource.getStructureId();
        try {
            res = readFileHeader(project.getId(), parentId, filename, true);
            res.setFullResourceName(resource.getRootPath());
            if (res.getState() == I_CmsConstants.C_STATE_DELETED) {
                // if an existing resource is deleted, it will be finally removed now
                // but we have to reuse its id in order to avoid orphanes in the online project
                newStructureId = res.getStructureId();
                newState = I_CmsConstants.C_STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = readSiblings(project, res);
                deleteProperties(project.getId(), res);
                removeFile(project, res, true);

                OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCES_MODIFIED, Collections.singletonMap("resources", modifiedResources)));
                OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", res)));
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
            }
        } catch (CmsException e) {
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                // we have a collision which has to be handled in the app.
                throw e;
            }
        }

        // check if the resource already exists
        if (!validateResourceIdExists(project.getId(), resource.getResourceId())) {
            throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_NOT_FOUND);
        }

        // write a new structure referring to the resource
        try {
            conn = m_sqlManager.getConnection(project);

            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, newStructureId.toString());
            stmt.setString(2, parentId.toString());
            stmt.setString(3, resource.getResourceId().toString());
            stmt.setString(4, filename);
            stmt.setInt(5, newState);
            stmt.executeUpdate();

            m_sqlManager.closeAll(null, stmt, null);

            // update the link Count
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_LINK_COUNT");
            stmt.setInt(1, this.internalCountSiblings(project.getId(), resource.getResourceId()));
            stmt.setString(2, resource.getResourceId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(null, stmt, null);

            // update the resource flags
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_FLAGS");
            stmt.setInt(1, resource.getFlags());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readFileHeader(project.getId(), newStructureId, false);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deleteProperties(int, com.opencms.file.CmsResource)
     */
    public void deleteProperties(int projectId, CmsResource resource) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String resourceName = resource.getRootPath();

        // add folder separator to folder name if it is not present
        if (resource.isFolder() && !resourceName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            resourceName += I_CmsConstants.C_FOLDER_SEPARATOR;
        }

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_DELETEALL");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.setString(2, resource.getStructureId().toString() /* resourceName */);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deleteProperty(java.lang.String, int, com.opencms.file.CmsResource, int)
     */
    public void deleteProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException {
        CmsPropertydefinition propdef = readPropertyDefinition(meta, projectId, resourceType);
        String resourceName = resource.getRootPath();

        // add folder separator to folder name if it is not present
        if (resourceType == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID && !resourceName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            resourceName += I_CmsConstants.C_FOLDER_SEPARATOR;
        }

        if (propdef == null) {
            // there is no propdefinition with the overgiven name for the resource
            throw new CmsException("[" + this.getClass().getName() + ".deleteProperty] " + meta, CmsException.C_NOT_FOUND);
        } else {
            // delete the metainfo in the db
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                // create statement
                conn = m_sqlManager.getConnection(projectId);
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_DELETE");
                stmt.setInt(1, propdef.getId());
                stmt.setString(2, resource.getResourceId().toString());
                stmt.setString(3, resource.getStructureId().toString() /* resourceName */);
                stmt.executeUpdate();
            } catch (SQLException exc) {
                throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
            } finally {
                m_sqlManager.closeAll(conn, stmt, null);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deletePropertyDefinition(com.opencms.file.CmsPropertydefinition)
     */
    public void deletePropertyDefinition(CmsPropertydefinition metadef) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (internalCountProperties(metadef) != 0) {
                throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(), CmsException.C_UNKNOWN_EXCEPTION);
            }
            for (int i = 0; i < 3; i++) {
                if (i == 0) {
                    // delete the offline propertydef
                    conn = m_sqlManager.getConnection();
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_DELETE");
                } else if (i == 1) {
                    // delete the online propertydef
                    conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                    stmt = m_sqlManager.getPreparedStatement(conn, I_CmsConstants.C_PROJECT_ONLINE_ID, "C_PROPERTYDEF_DELETE");
                } else {
                    // delete the backup propertydef
                    conn = m_sqlManager.getConnectionForBackup();
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_DELETE_BACKUP");
                }
                stmt.setInt(1, metadef.getId());
                stmt.executeUpdate();
                m_sqlManager.closeAll(conn, stmt, null);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#destroy()
     */
    public void destroy() throws Throwable {
        finalize();

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info("[" + this.getClass().getName() + "] destroyed!");
        }
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        if (m_sqlManager != null) {
            m_sqlManager.finalize();
        }

        m_sqlManager = null;
        m_driverManager = null;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#importResource(com.opencms.file.CmsProject, org.opencms.util.CmsUUID, com.opencms.file.CmsResource, byte[], org.opencms.util.CmsUUID, boolean)
     */
    public CmsResource importResource(CmsProject project, CmsUUID parentId, CmsResource newResource, byte[] filecontent, CmsUUID userId, boolean isFolder) throws CmsException {

        Connection conn = null;
        PreparedStatement stmt = null;

        if (newResource.getName().length() > I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME) {
            throw new CmsException("The resource name '" + newResource.getName() + "' is too long! (max. allowed length must be <= " + I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME + " chars.!)", CmsException.C_BAD_NAME);
        }

        int state = 0;
        CmsUUID modifiedByUserId = newResource.getUserLastModified();
        //long dateModified = newResource.isTouched() ? newResource.getDateLastModified() : System.currentTimeMillis();

        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            state = newResource.getState();
            modifiedByUserId = newResource.getUserLastModified();
            //dateModified = newResource.getDateLastModified();
        } else {
            state = I_CmsConstants.C_STATE_NEW;
        }

        try {
            CmsResource curResource = readFileHeader(project.getId(), parentId, newResource.getName(), true);
            if (curResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_RESOURCE_DELETED);
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
            }

        } catch (CmsException e) {
            // if the resource is marked as deleted remove it!
            if (e.getType() == CmsException.C_RESOURCE_DELETED) {
                if (isFolder) {
                    removeFolder(project, (CmsFolder)newResource);
                    state = I_CmsConstants.C_STATE_CHANGED;
                } else {
                    //removeFile(project, parentId, newResource.getResourceName());
                }
                //throw new CmsException("["+this.getClass().getName()+"] ",CmsException.C_FILE_EXISTS);
            }
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                throw e;
            }
        }

        // check if we can use some existing UUID's             
        CmsUUID newFileId = CmsUUID.getNullUUID();
        CmsUUID resourceId = new CmsUUID();
        CmsUUID structureId = new CmsUUID();
        if (newResource.getStructureId() != CmsUUID.getNullUUID()) {
            structureId = newResource.getStructureId();
        }
        if (newResource.getResourceId() != CmsUUID.getNullUUID()) {
            resourceId = newResource.getResourceId();
        }
        if (newResource.getFileId() != CmsUUID.getNullUUID()) {
            newFileId = newResource.getFileId();
        }

        // now write the resource
        try {
            conn = m_sqlManager.getConnection(project);

            // write the structure                                  
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, structureId.toString());
            stmt.setString(2, parentId.toString());
            stmt.setString(3, resourceId.toString());
            stmt.setString(4, newResource.getName());
            stmt.setInt(5, state);
            stmt.executeUpdate();

            if (!validateResourceIdExists(project.getId(), newResource.getResourceId())) {

                // write the resource
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_WRITE");
                stmt.setString(1, resourceId.toString());
                stmt.setInt(2, newResource.getType());
                stmt.setInt(3, newResource.getFlags());
                stmt.setString(4, newFileId.toString());
                stmt.setInt(5, newResource.getLoaderId());
                stmt.setTimestamp(6, new Timestamp(newResource.getDateCreated()));
                stmt.setString(7, newResource.getUserCreated().toString());
                stmt.setTimestamp(8, new Timestamp(newResource.getDateLastModified()));
                stmt.setString(9, modifiedByUserId.toString());
                stmt.setInt(10, state);
                stmt.setInt(11, newResource.getLength());
                stmt.setString(12, CmsUUID.getNullUUID().toString());
                stmt.setInt(13, project.getId());
                stmt.setInt(14, 1);
                stmt.executeUpdate();
                m_sqlManager.closeAll(null, stmt, null);

                //write the content
                if (!isFolder) {
                    try {
                        createFileContent(newFileId, filecontent, 0, project.getId(), false);
                    } catch (CmsException se) {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("[" + this.getClass().getName() + "] " + se.getMessage());
                        }
                    }
                }

            } else {
                // update the link Count
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_LINK_COUNT");
                stmt.setInt(1, this.internalCountSiblings(project.getId(), newResource.getResourceId()));
                stmt.setString(2, newResource.getResourceId().toString());
                stmt.executeUpdate();

                m_sqlManager.closeAll(null, stmt, null);

                // update the resource flags
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_FLAGS");
                stmt.setInt(1, newResource.getFlags());
                stmt.setString(2, newResource.getResourceId().toString());
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readFileHeader(project.getId(), parentId, newResource.getName(), true);
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(source.org.apache.java.util.Configurations, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(Configurations config, List successiveDrivers, CmsDriverManager driverManager) {
        String offlinePoolUrl = null;
        String onlinePoolUrl = null;
        String backupPoolUrl = null;
        boolean hasDistinctPoolUrls = false;

        if ((offlinePoolUrl = config.getString("db.vfs.pool")) == null) {
            hasDistinctPoolUrls = true;
            offlinePoolUrl = config.getString("db.vfs.offline.pool");
            onlinePoolUrl = config.getString("db.vfs.online.pool");
            backupPoolUrl = config.getString("db.vfs.backup.pool");
        } else {
            hasDistinctPoolUrls = false;
            onlinePoolUrl = offlinePoolUrl;
            backupPoolUrl = offlinePoolUrl;
        }

        m_sqlManager = this.initQueries();
        m_sqlManager.setPoolUrlOffline(offlinePoolUrl);
        m_sqlManager.setPoolUrlOnline(onlinePoolUrl);
        m_sqlManager.setPoolUrlBackup(backupPoolUrl);

        m_driverManager = driverManager;

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            if (hasDistinctPoolUrls) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Assign. offline pool : " + offlinePoolUrl);
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Assign. online pool  : " + onlinePoolUrl);
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Assign. backup pool  : " + backupPoolUrl);
            } else {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Assign. pool         : " + offlinePoolUrl);
            }
        }

        if (successiveDrivers != null && !successiveDrivers.isEmpty()) {
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(this.getClass().toString() + " does not support successive drivers");
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#initQueries(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.generic.CmsSqlManager();
    }

    /**
     * Returns the amount of properties for a propertydefinition.<p>
     *
     * @param metadef the propertydefinition to test
     * @return the amount of properties for a propertydefinition
     * @throws CmsException if something goes wrong
     */
    protected int internalCountProperties(CmsPropertydefinition metadef) throws CmsException {
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        int returnValue;
        try {
            // create statement
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_READALL_COUNT");
            stmt.setInt(1, metadef.getId());
            res = stmt.executeQuery();

            if (res.next()) {
                returnValue = res.getInt(1);
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(), CmsException.C_UNKNOWN_EXCEPTION);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return returnValue;
    }

    /**
     * Counts the number of siblings of a resource
     * 
     * @param projectId the current project id
     * @param resourceId the resource id to count the number of siblings from
     * @return number of siblings
     * @throws CmsException if something goes wrong
     */
    protected int internalCountSiblings(int projectId, CmsUUID resourceId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        int count = 0;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_COUNTLINKS");
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();

            if (res.next())
                count = res.getInt(1);

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return count;
    }

    /**
     * Gets all resources that are marked as undeleted.<p>
     * 
     * @param resources Vector of resources
     * @return all resources that are markes as deleted
     */
    protected List internalFilterUndeletedResources(List resources) {
        List undeletedResources = (List)new ArrayList();

        for (int i = 0; i < resources.size(); i++) {
            CmsResource res = (CmsResource)resources.get(i);
            if (res.getState() != I_CmsConstants.C_STATE_DELETED) {
                undeletedResources.add(res);
            }
        }

        return undeletedResources;
    }

    /**
     * Removes a resource physically in the database.<p>
     *
     * @param currentProject the current project
     * @param folder the folder to remove
     * @throws CmsException if something goes wrong
     */
    protected void internalRemoveFolder(CmsProject currentProject, CmsFolder folder) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(currentProject);

            // delete the structure record            
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_STRUCTURE_DELETE_BY_STRUCTUREID");
            stmt.setString(1, folder.getStructureId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(null, stmt, null);

            // delete the resource record
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCES_DELETE_BY_RESOURCEID");
            stmt.setString(1, folder.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readChildResources(com.opencms.file.CmsProject, com.opencms.file.CmsFolder, boolean)
     */
    public List readChildResources(CmsProject currentProject, CmsFolder parentFolder, boolean getSubFolders) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsResource currentResource = null;
        List subResources = (List)new ArrayList();
        String query = null;
        String resourceTypeClause = null;
        String orderClause = " ORDER BY CMS_T_STRUCTURE.RESOURCE_NAME";

        if (getSubFolders) {
            resourceTypeClause = " AND CMS_T_RESOURCES.RESOURCE_TYPE=0";
        } else {
            resourceTypeClause = " AND CMS_T_RESOURCES.RESOURCE_TYPE<>0";
        }

        try {
            conn = m_sqlManager.getConnection(currentProject);
            query = m_sqlManager.readQuery(currentProject, "C_RESOURCES_GET_SUBRESOURCES") + CmsSqlManager.replaceTableKey(currentProject.getId(), resourceTypeClause + orderClause);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, query);
            stmt.setString(1, parentFolder.getStructureId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                if (getSubFolders) {
                    currentResource = createFolder(res, currentProject.getId(), false);
                } else {
                    currentResource = createFile(res, currentProject.getId(), false);
                }
                subResources.add(currentResource);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return subResources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFile(int, boolean, org.opencms.util.CmsUUID)
     */
    public CmsFile readFile(int projectId, boolean includeDeleted, CmsUUID structureId) throws CmsException {
        CmsFile file = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_FILES_READ");
            stmt.setString(1, structureId.toString());
            //stmt.setInt(2, projectId);
            res = stmt.executeQuery();
            if (res.next()) {
                file = createFile(res, projectId);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
                // check if this resource is marked as deleted
                if (file.getState() == com.opencms.core.I_CmsConstants.C_STATE_DELETED && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + ".readFile] " + file.getName(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + ".readFile] " + structureId.toString(), CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception exc) {
            throw new CmsException("readFile " + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return file;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFileHeader(int, org.opencms.util.CmsUUID, boolean)
     */
    public CmsFile readFileHeader(int projectId, CmsUUID resourceId, boolean includeDeleted) throws CmsException {

        CmsFile file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READBYID");
            // read file data from database
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();
            // create new file
            if (res.next()) {
                file = createFile(res, projectId, false);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
                // check if this resource is marked as deleted
                if ((file.getState() == com.opencms.core.I_CmsConstants.C_STATE_DELETED) && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + ".readFileHeader/2] " + file.getName(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + ".readFileHeader/2] " + resourceId, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return file;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFileHeader(int, org.opencms.util.CmsUUID, java.lang.String, boolean)
     */
    public CmsFile readFileHeader(int projectId, CmsUUID parentId, String filename, boolean includeDeleted) throws CmsException {
        CmsFile file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ");

            stmt.setString(1, filename);
            stmt.setString(2, parentId.toString());
            //stmt.setInt(3, projectId);

            res = stmt.executeQuery();

            if (res.next()) {
                file = createFile(res, projectId, false);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }

                // check if this resource is marked as deleted
                if ((file.getState() == com.opencms.core.I_CmsConstants.C_STATE_DELETED) && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + ".readFileHeader/3] " + file.getName(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + ".readFileHeader/3] " + filename, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return file;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFiles(int)
     */
    public List readFiles(int projectId) throws CmsException {
        List resources = (List)new ArrayList();
        CmsResource currentResource;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        //String queryName = null;

        /*
        if (includeUnchanged && onlyProject) {
            queryName = "C_RESOURCES_READ_FILES_BY_PROJECT";
        } else if (includeUnchanged && !onlyProject) {
            queryName = "C_RESOURCES_READ_FILES";
        } else if (onlyProject) {
            queryName = "C_RESOURCES_READ_CHANGED_FILES_BY_PROJECT";
        } else {
            //queryName = "C_RESOURCES_READ_CHANGED_FILES";
            queryName = "C_RESOURCES_READ_CHANGED_FILEHEADERS";
        }
        */

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_CHANGED_FILEHEADERS");
            /*
            if (onlyProject) {
                stmt.setInt(1, projectId);
            }
            */
            res = stmt.executeQuery();

            while (res.next()) {
                //currentResource = createCmsFileFromResultSet(res, projectId, true);
                currentResource = createResource(res, projectId);
                resources.add(currentResource);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFiles(int, int)
     */
    public Vector readFiles(int projectId, int resourcetype) throws CmsException {
        Vector files = new Vector();
        CmsFile file;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_FILESBYTYPE");
            // read file data from database
            stmt.setInt(1, resourcetype);
            res = stmt.executeQuery();
            // create new file
            while (res.next()) {
                file = createFile(res, projectId, true);
                files.addElement(file);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return files;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolder(int, org.opencms.util.CmsUUID)
     */
    public CmsFolder readFolder(int projectId, CmsUUID folderId) throws CmsException {
        CmsFolder folder = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READBYID");
            stmt.setString(1, folderId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                folder = createFolder(res, projectId, true);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + ".readFolder/1] " + folderId, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException exc) {
            throw exc;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return folder;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolder(int, org.opencms.util.CmsUUID, java.lang.String)
     */
    public CmsFolder readFolder(int projectId, CmsUUID parentId, String foldername) throws CmsException {

        CmsFolder folder = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ");

            stmt.setString(1, foldername);
            stmt.setString(2, parentId.toString());

            res = stmt.executeQuery();

            if (res.next()) {
                folder = createFolder(res, projectId, true);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }

            } else {
                throw new CmsException("[" + this.getClass().getName() + ".readFolder/2] " + foldername, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException exc) {
            throw exc;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return folder;

    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolders(int)
     */
    public List readFolders(int projectId) throws CmsException {
        List folders = (List)new ArrayList();
        CmsFolder currentFolder;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            //query = m_sqlManager.get(projectId, "C_RESOURCES_READ_FOLDERS_BY_PROJECT") + CmsSqlManager.replaceTableKey(projectId, projectClause + changedClause + orderClause);
            //stmt = m_sqlManager.getPreparedStatementForSql(conn, query);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_CHANGED_FOLDERS_BY_PROJECT");
            // stmt.setInt(1, I_CmsConstants.C_STATE_UNCHANGED);
            // stmt.setInt(2, I_CmsConstants.C_STATE_UNCHANGED);
            res = stmt.executeQuery();
            while (res.next()) {
                currentFolder = createFolder(res, projectId, true);
                folders.add(currentFolder);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return folders;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolderTree(com.opencms.file.CmsProject, com.opencms.file.CmsResource)
     */
    public List readFolderTree(CmsProject currentProject, CmsResource parentResource) throws CmsException {
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        CmsAdjacencyTree adjacencyTree = new CmsAdjacencyTree();
        CmsUUID parentId = null;
        CmsUUID structureId = null;

        /*
         * possible other SQL queries to select a tree view:
         * SELECT PARENT.RESOURCE_NAME, CHILD.RESOURCE_NAME FROM CMS_OFFLINE_STRUCTURE PARENT, CMS_OFFLINE_STRUCTURE CHILD WHERE PARENT.STRUCTURE_ID=CHILD.PARENT_ID;
         * SELECT PARENT.RESOURCE_NAME, CHILD.RESOURCE_NAME FROM CMS_OFFLINE_STRUCTURE PARENT LEFT JOIN CMS_OFFLINE_STRUCTURE CHILD ON PARENT.STRUCTURE_ID=CHILD.PARENT_ID; 
         */

        try {
            conn = m_sqlManager.getConnection(currentProject);
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCES_GET_FOLDERTREE");
            stmt.setInt(1, I_CmsConstants.C_STATE_CHANGED);
            stmt.setInt(2, I_CmsConstants.C_STATE_NEW);
            stmt.setInt(3, I_CmsConstants.C_STATE_UNCHANGED);
            res = stmt.executeQuery();

            while (res.next()) {
                parentId = new CmsUUID(res.getString(1));
                structureId = new CmsUUID(res.getString(2));
                adjacencyTree.add(parentId, structureId);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        List dfsList = adjacencyTree.toList(parentResource.getStructureId());
        return dfsList;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readProperties(int, com.opencms.file.CmsResource, int)
     */
    public Map readProperties(int projectId, CmsResource resource, int resourceType) throws CmsException {
        HashMap returnValue = new HashMap();
        ResultSet result = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        String resourceName = resource.getRootPath();
        // hack: this never should happen, but it does.......
        if ((resource.isFolder()) && (!resourceName.endsWith("/"))) {
            resourceName += "/";
        }

        CmsUUID resourceId = resource.getResourceId();
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_READALL");
            stmt.setString(1, resourceId.toString());
            stmt.setString(2, resource.getStructureId().toString() /* resourceName */);
            stmt.setInt(3, resourceType);
            result = stmt.executeQuery();
            while (result.next()) {
                returnValue.put(result.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")), result.getString(m_sqlManager.readQuery("C_PROPERTY_VALUE")));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, result);
        }
        return (returnValue);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readProperty(java.lang.String, int, com.opencms.file.CmsResource, int)
     */
    public String readProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException {

        ResultSet result = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String returnValue = null;

        String resourceName = resource.getRootPath();
        // hack: this never should happen, but it does.......
        if ((resource.isFolder()) && (!resourceName.endsWith("/"))) {
            resourceName += "/";
        }

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_READ");

            String resourceId = resource.getResourceId().toString();
            stmt.setString(1, meta);
            stmt.setInt(2, resourceType);
            stmt.setString(3, resourceId);
            stmt.setString(4, resource.getStructureId().toString() /* resourceName */);

            result = stmt.executeQuery();
            if (result.next()) {
                returnValue = result.getString(m_sqlManager.readQuery("C_PROPERTY_VALUE"));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, result);
        }

        return returnValue;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinition(java.lang.String, int, int)
     */
    public CmsPropertydefinition readPropertyDefinition(String name, int projectId, int type) throws CmsException {
        CmsPropertydefinition propDef = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_READ");

            stmt.setString(1, name);
            stmt.setInt(2, type);
            res = stmt.executeQuery();

            // if resultset exists - return it
            if (res.next()) {
                propDef = new CmsPropertydefinition(res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_ID")), res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")), res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_RESOURCE_TYPE")));
            } else {
                res.close();
                res = null;
                // not found!
                throw new CmsException("[" + this.getClass().getName() + ".readPropertydefinition] " + name, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return propDef;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinitions(int, com.opencms.file.I_CmsResourceType)
     */
    public Vector readPropertyDefinitions(int projectId, I_CmsResourceType resourcetype) throws CmsException {
        Vector metadefs = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_READALL");
            // create statement
            stmt.setInt(1, resourcetype.getResourceType());
            res = stmt.executeQuery();

            while (res.next()) {
                metadefs.addElement(new CmsPropertydefinition(res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_ID")), res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")), res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_RESOURCE_TYPE"))));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (metadefs);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResourceNames(int, java.lang.String, java.lang.String)
     */
    public Vector readResourceNames(int projectId, String propertyDefinition, String propertyValue) throws CmsException {
        Vector names = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_FILES_WITH_PROPERTY");
            stmt.setInt(1, projectId);
            stmt.setString(2, propertyValue);
            stmt.setString(3, propertyDefinition);
            res = stmt.executeQuery();

            // store the result into the vector
            while (res.next()) {
                String result = res.getString(1);
                names.addElement(result);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, "getFilesWithProperty(int, String, String)", CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return names;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResources(int, long, long)
     */
    public List readResources(int projectId, long starttime, long endtime) throws CmsException {
        List result = new ArrayList();

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCE_IN_TIMERANGE");
            stmt.setTimestamp(1, new Timestamp(starttime));
            stmt.setTimestamp(2, new Timestamp(endtime));
            res = stmt.executeQuery();

            while (res.next()) {
                CmsResource resource = createResource(res, projectId);
                result.add(resource);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception exc) {
            throw new CmsException("getResourcesWithProperty" + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResources(int, java.lang.String)
     */
    public Vector readResources(int projectId, String propertyDefName) throws CmsException {
        Vector resources = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF");
            stmt.setString(1, propertyDefName);
            res = stmt.executeQuery();

            while (res.next()) {
                CmsResource resource = createResource(res, projectId);
                resources.addElement(resource);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResources(int, java.lang.String, java.lang.String, int)
     */
    public Vector readResources(int projectId, String propertyDefinition, String propertyValue, int resourceType) throws CmsException {
        Vector resources = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCE_WITH_PROPERTY");
            stmt.setInt(1, projectId);
            stmt.setString(2, propertyValue);
            stmt.setString(3, propertyDefinition);
            stmt.setInt(4, resourceType);
            res = stmt.executeQuery();
            String lastResourcename = "";

            while (res.next()) {
                CmsResource resource = createResource(res, projectId);
                if (!resource.getName().equalsIgnoreCase(lastResourcename)) {
                    resources.addElement(resource);
                }
                lastResourcename = resource.getName();
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception exc) {
            throw new CmsException("getResourcesWithProperty" + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readSiblings(com.opencms.file.CmsProject, com.opencms.file.CmsResource)
     */
    public List readSiblings(CmsProject currentProject, CmsResource resource) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        CmsResource currentResource = null;
        List vfsLinks = (List)new ArrayList();

        try {
            conn = m_sqlManager.getConnection(currentProject);
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_SELECT_NONDELETED_VFS_LINKS");
            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                currentResource = createFile(res, currentProject.getId(), false);
                vfsLinks.add(currentResource);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return vfsLinks;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#removeFile(com.opencms.file.CmsProject, com.opencms.file.CmsResource)
     */
    public void removeFile(CmsProject currentProject, CmsResource resource, boolean removeFileContent) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        int linkCount = 0;

        try {
            conn = m_sqlManager.getConnection(currentProject);

            // delete the structure recourd
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_STRUCTURE_DELETE_BY_STRUCTUREID");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(null, stmt, null);

            // count the references to the resource
            linkCount = internalCountSiblings(currentProject.getId(), resource.getResourceId());

            if (linkCount > 0) {

                // update the link Count
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCES_UPDATE_LINK_COUNT");
                stmt.setInt(1, this.internalCountSiblings(currentProject.getId(), resource.getResourceId()));
                stmt.setString(2, resource.getResourceId().toString());
                stmt.executeUpdate();

                m_sqlManager.closeAll(null, stmt, null);

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

                m_sqlManager.closeAll(null, stmt, null);

                if (!resource.getFileId().equals(CmsUUID.getNullUUID())) {
                    // delete the content record
                    stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_FILE_CONTENT_DELETE");
                    stmt.setString(1, resource.getFileId().toString());
                    stmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#removeFolder(com.opencms.file.CmsProject, com.opencms.file.CmsFolder)
     */
    public void removeFolder(CmsProject currentProject, CmsFolder folder) throws CmsException {
        // the current implementation only deletes empty folders
        // check if the folder has any files in it
        List files = readChildResources(currentProject, folder, false);
        files = internalFilterUndeletedResources(files);
        if (files.size() == 0) {
            // check if the folder has any folders in it
            List folders = readChildResources(currentProject, folder, true);
            folders = internalFilterUndeletedResources(folders);
            if (folders.size() == 0) {
                //this folder is empty, delete it
                // Connection conn = null;
                // PreparedStatement stmt = null;
                // try {
                //     conn = m_sqlManager.getConnection(currentProject);
                //     stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCES_ID_DELETE");
                //    // delete the folder
                //    stmt.setString(1, folder.getId().toString());
                //    stmt.executeUpdate();
                // } catch (SQLException e) {
                //    throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
                //} finally {
                //    m_sqlManager.closeAll(conn, stmt, null);
                // }
                internalRemoveFolder(currentProject, folder);
            } else {
                String errorResNames = "";
                Iterator i = folders.iterator();
                while (i.hasNext()) {
                    CmsResource errorRes = (CmsResource)i.next();
                    errorResNames += "[" + errorRes.getName() + "]";
                }
                throw new CmsException("[" + this.getClass().getName() + "] " + folder.getName() + errorResNames, CmsException.C_NOT_EMPTY);
            }
        } else {
            String errorResNames = "";
            Iterator i = files.iterator();
            while (i.hasNext()) {
                CmsResource errorRes = (CmsResource)i.next();
                errorResNames += "[" + errorRes.getName() + "]";
            }
            throw new CmsException("[" + this.getClass().getName() + "] " + folder.getName() + errorResNames, CmsException.C_NOT_EMPTY);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#removeTempFile(com.opencms.file.CmsResource)
     */
    public void removeTempFile(CmsResource file) throws CmsException {
        PreparedStatement stmt = null;
        PreparedStatement statementCont = null;
        PreparedStatement statementProp = null;
        Connection conn = null;
        ResultSet res = null;
        String fileId = null;
        String structureId = null;
        boolean hasBatch = false;

        //String tempFilename = file.getRootName() + file.getPath() + I_CmsConstants.C_TEMP_PREFIX + file.getName() + "%";
        String tempFilename = I_CmsConstants.C_TEMP_PREFIX + file.getName() + "%";

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_GETTEMPFILES");
            stmt.setString(1, tempFilename);
            stmt.setString(2, file.getParentStructureId().toString());
            res = stmt.executeQuery();

            statementProp = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_DELETEALL");
            statementCont = m_sqlManager.getPreparedStatement(conn, "C_FILE_CONTENT_DELETE");

            while (res.next()) {
                hasBatch = true;

                fileId = res.getString("FILE_ID");
                structureId = res.getString("STRUCTURE_ID");

                // delete the properties
                statementProp.setString(1, structureId);
                statementProp.addBatch();

                // delete the file content
                statementCont.setString(1, fileId);
                statementCont.addBatch();
            }

            if (hasBatch) {
                statementProp.executeBatch();
                statementCont.executeBatch();

                m_sqlManager.closeAll(null, stmt, res);

                stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_DELETETEMPFILES");
                stmt.setString(1, tempFilename);
                stmt.setString(2, file.getParentStructureId().toString());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
            m_sqlManager.closeAll(null, statementProp, null);
            m_sqlManager.closeAll(null, statementCont, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#replaceResource(com.opencms.file.CmsUser, com.opencms.file.CmsProject, com.opencms.file.CmsResource, byte[], int, int)
     */
    public void replaceResource(CmsUser currentUser, CmsProject currentProject, CmsResource res, byte[] resContent, int newResType, int loaderId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // write the file content
            if (resContent != null) {
                writeFileContent(res.getFileId(), resContent, currentProject.getId(), false);
            }

            // update the resource record
            conn = m_sqlManager.getConnection(currentProject);
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCE_REPLACE");
            stmt.setInt(1, newResType);
            stmt.setInt(2, resContent.length);
            stmt.setInt(3, loaderId);
            stmt.setString(4, res.getResourceId().toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#validateContentIdExists(int, org.opencms.util.CmsUUID)
     */
    public boolean validateContentIdExists(int projectId, CmsUUID contentId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean result = false;
        int count = 0;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_SELECT_CONTENT_ID");
            stmt.setString(1, contentId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                count = res.getInt(1);
                result = (count == 1);
            } else {
                result = false;
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#validateResourceIdExists(int, org.opencms.util.CmsUUID)
     */
    public boolean validateResourceIdExists(int projectId, CmsUUID resourceId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean exists = false;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_RESOURCE_STATE");
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();

            exists = res.next();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return exists;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#validateStructureIdExists(int, org.opencms.util.CmsUUID)
     */
    public boolean validateStructureIdExists(int projectId, CmsUUID structureId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean result = false;
        int count = 0;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_SELECT_STRUCTURE_ID");
            stmt.setString(1, structureId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                count = res.getInt(1);
                result = (count == 1);
            } else {
                result = false;
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeFileContent(org.opencms.util.CmsUUID, byte[], int, boolean)
     */
    public void writeFileContent(CmsUUID fileId, byte[] fileContent, int projectId, boolean writeBackup) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (writeBackup) {
                conn = m_sqlManager.getConnectionForBackup();
                stmt = m_sqlManager.getPreparedStatement(conn, "C_FILES_UPDATE_BACKUP");
            } else {
                conn = m_sqlManager.getConnection(projectId);
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_FILES_UPDATE");
            }

            // update the file content in the FILES database.
            if (fileContent.length < 2000) {
                stmt.setBytes(1, fileContent);
            } else {
                stmt.setBinaryStream(1, new ByteArrayInputStream(fileContent), fileContent.length);
            }

            stmt.setString(2, fileId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeFileHeader(com.opencms.file.CmsProject, com.opencms.file.CmsFile, int, org.opencms.util.CmsUUID)
     */
    public void writeFileHeader(CmsProject project, CmsFile file, int changed, CmsUUID userId) throws CmsException {
        // this task is split into two statements because Oracle doesnt support muti-table updates
        PreparedStatement stmt = null;
        Connection conn = null;
        long resourceDateModified = file.isTouched() ? file.getDateLastModified() : System.currentTimeMillis();

        // since we are only writing the file header, the content is unchanged
        // for this reason, the resource state is left unchanged
        int structureState = file.getState();
        int resourceState = file.getState();
        //if (structureState != com.opencms.core.I_CmsConstants.C_STATE_NEW && (changed > CmsDriverManager.C_NOTHING_CHANGED)) {
        if (changed == CmsDriverManager.C_UPDATE_RESOURCE_STATE)
            resourceState = com.opencms.core.I_CmsConstants.C_STATE_CHANGED;
        else if (changed == CmsDriverManager.C_UPDATE_STRUCTURE_STATE)
            structureState = com.opencms.core.I_CmsConstants.C_STATE_CHANGED;
        else if (changed != CmsDriverManager.C_NOTHING_CHANGED) {
            resourceState = com.opencms.core.I_CmsConstants.C_STATE_CHANGED;
            structureState = com.opencms.core.I_CmsConstants.C_STATE_CHANGED;
        }
        //}

        try {
            conn = m_sqlManager.getConnection(project);
            //savepoint = conn.setSavepoint("before_update");

            // update the resource
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCES");
            stmt.setInt(1, file.getType());
            stmt.setInt(2, file.getFlags());
            stmt.setInt(3, file.getLoaderId());
            stmt.setTimestamp(4, new Timestamp(resourceDateModified));
            stmt.setString(5, userId.toString());
            stmt.setInt(6, resourceState);
            stmt.setInt(7, file.getLength());
            stmt.setString(8, file.getFileId().toString());
            stmt.setString(9, CmsUUID.getNullUUID().toString());
            stmt.setInt(10, project.getId());
            stmt.setInt(11, internalCountSiblings(project.getId(), file.getResourceId()));
            stmt.setString(12, file.getResourceId().toString());
            stmt.executeUpdate();
            m_sqlManager.closeAll(null, stmt, null);

            // update the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_STRUCTURE");
            stmt.setString(1, file.getParentStructureId().toString());
            stmt.setString(2, file.getResourceId().toString());
            stmt.setString(3, file.getName());
            stmt.setInt(4, structureState);
            stmt.setString(5, file.getStructureId().toString());
            stmt.executeUpdate();

            //m_sqlManager.commit(conn);
        } catch (SQLException e) {
            //m_sqlManager.rollback(conn, savepoint);
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            //m_sqlManager.releaseSavepoint(conn, savepoint);
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeFolder(com.opencms.file.CmsProject, com.opencms.file.CmsFolder, int, org.opencms.util.CmsUUID)
     */
    public void writeFolder(CmsProject project, CmsFolder folder, int changed, CmsUUID userId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        //CmsUUID modifiedByUserId = userId;
        long resourceDateModified = folder.isTouched() ? folder.getDateLastModified() : System.currentTimeMillis();

        //Savepoint savepoint = null;
        int structureState = folder.getState();
        int resourceState = folder.getState();
        if (structureState != com.opencms.core.I_CmsConstants.C_STATE_NEW && (changed > CmsDriverManager.C_NOTHING_CHANGED)) {
            if (changed == CmsDriverManager.C_UPDATE_RESOURCE_STATE)
                resourceState = com.opencms.core.I_CmsConstants.C_STATE_CHANGED;
            else if (changed == CmsDriverManager.C_UPDATE_STRUCTURE_STATE)
                structureState = com.opencms.core.I_CmsConstants.C_STATE_CHANGED;
            else {
                resourceState = com.opencms.core.I_CmsConstants.C_STATE_CHANGED;
                structureState = com.opencms.core.I_CmsConstants.C_STATE_CHANGED;
            }
        }

        try {
            conn = m_sqlManager.getConnection(project);
            //savepoint = conn.setSavepoint("before_update");

            // update the resource
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCES");
            stmt.setInt(1, folder.getType());
            stmt.setInt(2, folder.getFlags());
            stmt.setInt(3, folder.getLoaderId());
            stmt.setTimestamp(4, new Timestamp(resourceDateModified));
            stmt.setString(5, userId.toString());
            stmt.setInt(6, structureState);
            stmt.setInt(7, 0);
            stmt.setString(8, CmsUUID.getNullUUID().toString());
            stmt.setString(9, CmsUUID.getNullUUID().toString());
            //stmt.setInt(10, folder.getProjectId());
            stmt.setInt(10, project.getId());
            stmt.setInt(11, this.internalCountSiblings(project.getId(), folder.getResourceId()));
            stmt.setString(12, folder.getResourceId().toString());
            stmt.executeUpdate();
            m_sqlManager.closeAll(null, stmt, null);

            // update the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_STRUCTURE");
            stmt.setString(1, folder.getParentStructureId().toString());
            stmt.setString(2, folder.getResourceId().toString());
            stmt.setString(3, folder.getName());
            stmt.setInt(4, resourceState);
            stmt.setString(5, folder.getStructureId().toString());
            stmt.executeUpdate();

            //m_sqlManager.commit(conn);
        } catch (SQLException e) {
            //m_sqlManager.rollback(conn, savepoint);
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            //m_sqlManager.releaseSavepoint(conn, savepoint);
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeLastModifiedProjectId(com.opencms.file.CmsProject, int, com.opencms.file.CmsResource)
     */
    public void writeLastModifiedProjectId(CmsProject project, int projectId, CmsResource resource) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_PROJECT_ID");
            stmt.setInt(1, projectId);
            stmt.setString(2, resource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeProperties(java.util.Map, int, com.opencms.file.CmsResource, int, boolean)
     */
    public void writeProperties(Map propertyinfos, int projectId, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException {
        // get all metadefs
        Iterator keys = propertyinfos.keySet().iterator();
        // one metainfo-name:
        String key;

        while (keys.hasNext()) {
            key = (String)keys.next();
            writeProperty(key, projectId, (String)propertyinfos.get(key), resource, resourceType, addDefinition);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeProperty(java.lang.String, int, java.lang.String, com.opencms.file.CmsResource, int, boolean)
     */
    public void writeProperty(String meta, int projectId, String value, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException {
        CmsPropertydefinition propdef = null;
        try {
            propdef = readPropertyDefinition(meta, projectId, resourceType);
        } catch (CmsException ex) {
            // do nothing
        }
        String resourceName = resource.getRootPath();
        // hack: this never should happen, but it does.......
        if ((resource.isFolder()) && (!resourceName.endsWith("/"))) {
            resourceName += "/";
        }

        if (propdef == null) {
            // there is no propertydefinition for with the overgiven name for the resource
            // add this definition or throw an exception
            if (addDefinition) {
                createPropertyDefinition(meta, projectId, resourceType);
            } else {
                throw new CmsException("[" + this.getClass().getName() + ".writeProperty/1] " + meta, CmsException.C_NOT_FOUND);
            }
        } else {
            // write the property into the db
            PreparedStatement stmt = null;
            Connection conn = null;
            try {
                conn = m_sqlManager.getConnection(projectId);
                if (readProperty(propdef.getName(), projectId, resource, resourceType) != null) {
                    // property exists already - use update.
                    // create statement
                    stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_UPDATE");
                    stmt.setString(1, m_sqlManager.validateNull(value));
                    stmt.setString(2, resource.getResourceId().toString());
                    stmt.setString(3, resource.getStructureId().toString() /* resourceName */);
                    stmt.setInt(4, propdef.getId());
                    stmt.executeUpdate();
                } else {
                    // property dosen't exist - use create.
                    // create statement
                    stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_CREATE");
                    stmt.setInt(1, m_sqlManager.nextId(m_sqlManager.readQuery(projectId, "C_TABLE_PROPERTIES")));
                    stmt.setInt(2, propdef.getId());
                    stmt.setString(3, resource.getResourceId().toString());
                    stmt.setString(4, resource.getStructureId().toString() /* resourceName */);
                    stmt.setString(5, m_sqlManager.validateNull(value));
                    stmt.executeUpdate();
                }
            } catch (SQLException exc) {
                throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
            } finally {
                m_sqlManager.closeAll(conn, stmt, null);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResource(com.opencms.file.CmsProject, com.opencms.file.CmsResource, byte[], int, org.opencms.util.CmsUUID)
     */
    public void writeResource(CmsProject project, CmsResource resource, byte[] filecontent, int changed, CmsUUID userId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        long resourceDateModified = resource.isTouched() ? resource.getDateLastModified() : System.currentTimeMillis();

        boolean isFolder = false;
        //Savepoint savepoint = null;

        if (resource.getType() == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
            isFolder = true;
        }
        if (filecontent == null) {
            filecontent = new byte[0];
        }
        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            userId = resource.getUserLastModified();
        }

        int structureState = resource.getState();
        int resourceState = resource.getState();
        if (structureState != com.opencms.core.I_CmsConstants.C_STATE_NEW && (changed > CmsDriverManager.C_NOTHING_CHANGED)) {
            if (changed == CmsDriverManager.C_UPDATE_RESOURCE_STATE) {
                resourceState = com.opencms.core.I_CmsConstants.C_STATE_CHANGED;
            } else if (changed == CmsDriverManager.C_UPDATE_STRUCTURE_STATE) {
                structureState = com.opencms.core.I_CmsConstants.C_STATE_CHANGED;
            } else {
                resourceState = com.opencms.core.I_CmsConstants.C_STATE_CHANGED;
                structureState = com.opencms.core.I_CmsConstants.C_STATE_CHANGED;
            }
        }

        try {
            conn = m_sqlManager.getConnection(project);
            //savepoint = conn.setSavepoint("before_update");

            // update the resource
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCES");
            stmt.setInt(1, resource.getType());
            stmt.setInt(2, resource.getFlags());
            stmt.setInt(3, resource.getLoaderId());
            stmt.setTimestamp(4, new Timestamp(resourceDateModified));
            stmt.setString(5, userId.toString());
            stmt.setInt(6, resourceState);
            stmt.setInt(7, filecontent.length);
            stmt.setString(8, resource.getFileId().toString());
            stmt.setString(9, CmsUUID.getNullUUID().toString());
            stmt.setInt(10, resource.getProjectLastModified());
            stmt.setInt(11, this.internalCountSiblings(project.getId(), resource.getResourceId()));
            stmt.setString(12, resource.getResourceId().toString());
            stmt.executeUpdate();
            m_sqlManager.closeAll(null, stmt, null);

            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_STRUCTURE");
            stmt.setString(1, resource.getParentStructureId().toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setString(3, resource.getName());
            stmt.setInt(4, structureState);
            stmt.setString(5, resource.getStructureId().toString());
            stmt.executeUpdate();

            //m_sqlManager.commit(conn);            
        } catch (SQLException e) {
            //m_sqlManager.rollback(conn, savepoint);
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            //m_sqlManager.releaseSavepoint(conn, savepoint);
            m_sqlManager.closeAll(conn, stmt, null);
        }

        // write the filecontent if this is a file
        if (!isFolder) {
            this.writeFileContent(resource.getFileId(), filecontent, project.getId(), false);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResource(com.opencms.file.CmsProject, com.opencms.file.CmsResource, com.opencms.file.CmsResource, boolean)
     */
    public void writeResource(CmsProject onlineProject, CmsResource onlineResource, CmsResource offlineResource, boolean writeFileContent) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        int resourceSize = offlineResource.getLength();

        try {
            conn = m_sqlManager.getConnection(onlineProject);

            if (validateResourceIdExists(onlineProject.getId(), offlineResource.getResourceId())) {

                // the resource record exists online already

                if (writeFileContent && offlineResource.isFile()) {
                    // update the online file content
                    writeFileContent(offlineResource.getFileId(), ((CmsFile)offlineResource).getContents(), onlineProject.getId(), false);
                }

                // update the online resource record
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_UPDATE_RESOURCES");
                stmt.setInt(1, offlineResource.getType());
                stmt.setInt(2, offlineResource.getFlags());
                stmt.setInt(3, offlineResource.getLoaderId());
                stmt.setTimestamp(4, new Timestamp(offlineResource.getDateLastModified()));
                stmt.setString(5, offlineResource.getUserLastModified().toString());
                stmt.setInt(6, I_CmsConstants.C_STATE_UNCHANGED);
                stmt.setInt(7, resourceSize);
                stmt.setString(8, offlineResource.getFileId().toString());
                stmt.setString(9, CmsUUID.getNullUUID().toString());
                stmt.setInt(10, offlineResource.getProjectLastModified());
                stmt.setInt(11, this.internalCountSiblings(onlineProject.getId(), onlineResource.getResourceId()));
                stmt.setString(12, offlineResource.getResourceId().toString());
                stmt.executeUpdate();

                m_sqlManager.closeAll(null, stmt, null);

                // update the online structure record
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_UPDATE_STRUCTURE");
                stmt.setString(1, offlineResource.getParentStructureId().toString());
                stmt.setString(2, offlineResource.getResourceId().toString());
                stmt.setString(3, offlineResource.getName());
                stmt.setInt(4, I_CmsConstants.C_STATE_UNCHANGED);
                stmt.setString(5, offlineResource.getStructureId().toString());
                stmt.executeUpdate();

            } else {

                // the resource record does NOT exist online yet

                if (writeFileContent && offlineResource.isFile() && !validateContentIdExists(onlineProject.getId(), offlineResource.getFileId())) {
                    // create the file content online
                    resourceSize = offlineResource.getLength();
                    createFileContent(offlineResource.getFileId(), ((CmsFile)offlineResource).getContents(), 0, onlineProject.getId(), false);
                }

                // create the resource record online
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_WRITE");
                stmt.setString(1, offlineResource.getResourceId().toString());
                stmt.setInt(2, offlineResource.getType());
                stmt.setInt(3, offlineResource.getFlags());
                stmt.setString(4, offlineResource.getFileId().toString());
                stmt.setInt(5, offlineResource.getLoaderId());
                stmt.setTimestamp(6, new Timestamp(offlineResource.getDateCreated()));
                stmt.setString(7, offlineResource.getUserCreated().toString());
                stmt.setTimestamp(8, new Timestamp(offlineResource.getDateLastModified()));
                stmt.setString(9, offlineResource.getUserLastModified().toString());
                stmt.setInt(10, I_CmsConstants.C_STATE_UNCHANGED);
                stmt.setInt(11, resourceSize);
                stmt.setString(12, CmsUUID.getNullUUID().toString());
                stmt.setInt(13, offlineResource.getProjectLastModified());
                stmt.setInt(14, 1);
                stmt.executeUpdate();

                m_sqlManager.closeAll(null, stmt, null);

                // create the structure record online
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_STRUCTURE_WRITE");
                stmt.setString(1, offlineResource.getStructureId().toString());
                stmt.setString(2, offlineResource.getParentStructureId().toString());
                stmt.setString(3, offlineResource.getResourceId().toString());
                stmt.setString(4, offlineResource.getName());
                stmt.setInt(5, I_CmsConstants.C_STATE_UNCHANGED);
                stmt.executeUpdate();

            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResourceState(com.opencms.file.CmsProject, com.opencms.file.CmsResource, int)
     */
    public void writeResourceState(CmsProject project, CmsResource resource, int changed) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            return;
        }

        try {
            conn = m_sqlManager.getConnection(project);

            if (changed == CmsDriverManager.C_UPDATE_RESOURCE) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCE_STATELASTMODIFIED");
                stmt.setInt(1, resource.getState());
                stmt.setTimestamp(2, new Timestamp(resource.getDateLastModified()));
                stmt.setString(3, resource.getUserLastModified().toString());
                stmt.setInt(4, project.getId());
                stmt.setString(5, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(null, stmt, null);
            }

            if (changed == CmsDriverManager.C_UPDATE_RESOURCE_STATE || changed == CmsDriverManager.C_UPDATE_ALL) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCE_STATE");
                stmt.setInt(1, resource.getState());
                stmt.setInt(2, project.getId());
                stmt.setString(3, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(null, stmt, null);
            }

            if (changed == CmsDriverManager.C_UPDATE_STRUCTURE || changed == CmsDriverManager.C_UPDATE_STRUCTURE_STATE || changed == CmsDriverManager.C_UPDATE_ALL) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_STRUCTURE_STATE");
                stmt.setInt(1, resource.getState());
                stmt.setString(2, resource.getStructureId().toString());
                stmt.executeUpdate();
            }

        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#getSqlManager()
     */    
    public CmsSqlManager getSqlManager() {
        return m_sqlManager;
    }

}

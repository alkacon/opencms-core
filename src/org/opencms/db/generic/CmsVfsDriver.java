/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsVfsDriver.java,v $
 * Date   : $Date: 2004/06/25 16:33:07 $
 * Version: $Revision: 1.188 $
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

import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertydefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsAdjacencyTree;
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

import org.apache.commons.collections.ExtendedProperties;

/**
 * Generic (ANSI-SQL) database server implementation of the VFS driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * @version $Revision: 1.188 $ $Date: 2004/06/25 16:33:07 $
 * @since 5.1
 */
public class CmsVfsDriver extends Object implements I_CmsDriver, I_CmsVfsDriver {

    /** The driver manager. */    
    protected CmsDriverManager m_driverManager;
    
    /** The sql manager. */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(java.sql.ResultSet, int)
     */
    public CmsFile createFile(ResultSet res, int projectId) throws SQLException {
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
        long dateCreated = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED"));
        long dateLastModified = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED"));
        long dateReleased = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_RELEASED"));
        long dateExpired = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_EXPIRED"));     
        int resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        byte[] content = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
        int linkCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_LINK_COUNT"));

        int newState = (structureState > resourceState) ? structureState : resourceState;

        return new CmsFile(structureId, resourceId, parentId, fileId, resourceName, resourceType, resourceFlags, projectId, newState, loaderId, dateCreated, userCreated, dateLastModified, userLastModified, dateReleased, dateExpired, linkCount, resourceSize, content);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(java.sql.ResultSet, int, boolean)
     */
    public CmsFile createFile(ResultSet res, int projectId, boolean hasFileContentInResultSet) throws SQLException {
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
        long dateCreated = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED"));
        long dateLastModified = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED"));
        long dateReleased = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_RELEASED"));
        long dateExpired = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_EXPIRED"));     
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

        return new CmsFile(
            structureId, 
            resourceId,
            parentId, 
            fileId, 
            resourceName, 
            resourceType, 
            resourceFlags, 
            resProjectId, 
            newState, 
            loaderId, 
            dateCreated, 
            userCreated, 
            dateLastModified, 
            userLastModified, 
            dateReleased, 
            dateExpired, 
            linkCount, 
            resourceSize, 
            content);
    }    

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createContent(CmsProject, org.opencms.util.CmsUUID, byte[], int, boolean)
     */
    public void createContent(CmsProject project, CmsUUID contentId, byte[] content, int versionId, boolean writeBackup) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (writeBackup) {
                conn = m_sqlManager.getConnectionForBackup();
                stmt = m_sqlManager.getPreparedStatement(conn, "C_FILES_WRITE_BACKUP");
            } else {
                conn = m_sqlManager.getConnection(project);
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_FILES_WRITE");
            }

            stmt.setString(1, contentId.toString());

            if (content.length < 2000) {
                stmt.setBytes(2, content);
            } else {
                stmt.setBinaryStream(2, new ByteArrayInputStream(content), content.length);
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
     * @see org.opencms.db.I_CmsVfsDriver#createFolder(java.sql.ResultSet, int, boolean)
     */
    public CmsFolder createFolder(ResultSet res, int projectId, boolean hasProjectIdInResultSet) throws SQLException {
        
        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_PARENT_ID")));
        String resourceName = res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_NAME"));
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
        int linkCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_LINK_COUNT"));

        int newState = (structureState > resourceState) ? structureState : resourceState;

        return new CmsFolder(structureId, resourceId, parentId, resourceName, resourceType, resourceFlags, resProjectId, newState, dateCreated, userCreated, dateLastModified, userLastModified, linkCount, dateReleased, dateExpired);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createPropertyDefinition(java.lang.String, int, int)
     */
    public CmsPropertydefinition createPropertyDefinition(String name, int projectId, int mappingtype) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        // TODO switch the property def. PK into a CmsUUID PK

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_CREATE");
            stmt.setInt(1, m_sqlManager.nextId(projectId, m_sqlManager.readQuery("C_TABLE_PROPERTYDEF")));
            stmt.setString(2, name);
            stmt.setInt(3, mappingtype);
            stmt.executeUpdate();         
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readPropertyDefinition(name, projectId, mappingtype);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createResource(java.sql.ResultSet, int)
     */
    public CmsResource createResource(ResultSet res, int projectId) throws SQLException {

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
        long dateCreated = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED"));
        long dateLastModified = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED"));
        long dateReleased = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_RELEASED"));
        long dateExpired = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_EXPIRED"));     
        int resourceSize;
        if (resourceType == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
            // folders must have -1 size
            resourceSize = -1;
        } else {
            resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        }
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        int linkCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_LINK_COUNT"));

        int newState = (structureState > resourceState) ? structureState : resourceState;

        CmsResource newResource = new CmsResource(structureId, resourceId, parentId, fileId, resourceName, resourceType, resourceFlags, resourceProjectId, newState, loaderId, dateCreated, userCreated, dateLastModified, userLastModified, dateReleased, dateExpired, linkCount, resourceSize);

        return newResource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createSibling(org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.lang.String)
     */
    public CmsResource createSibling(CmsProject project, CmsResource resource, String resourcename) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        int newState = 0;

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
            res = readFileHeader(project.getId(), resource.getParentStructureId(), resourcename, true);
            res.setFullResourceName(resource.getRootPath());
            if (res.getState() == I_CmsConstants.C_STATE_DELETED) {
                // if an existing resource is deleted, it will be finally removed now
                // but we have to reuse its id in order to avoid orphanes in the online project
                newStructureId = res.getStructureId();
                newState = I_CmsConstants.C_STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = readSiblings(project, res, false);
                deleteProperties(project.getId(), res, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
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
            throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + "] ");
        }

        // write a new structure referring to the resource
        try {
            conn = m_sqlManager.getConnection(project);

            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, newStructureId.toString());
            stmt.setString(2, resource.getParentStructureId().toString());
            stmt.setString(3, resource.getResourceId().toString());
            stmt.setString(4, resourcename);
            stmt.setInt(5, newState);
            stmt.setLong(6, resource.getDateReleased());
            stmt.setLong(7, resource.getDateExpired());
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
     * @see org.opencms.db.I_CmsVfsDriver#deleteProperties(int, org.opencms.file.CmsResource, int)
     */
    public void deleteProperties(int projectId, CmsResource resource, int deleteOption) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String resourceName = resource.getRootPath();

        // add folder separator to folder name if it is not present
        if (resource.isFolder() && !resourceName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            resourceName += I_CmsConstants.C_FOLDER_SEPARATOR;
        }

        try {
            conn = m_sqlManager.getConnection(projectId);
            
            if (deleteOption == CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES) {
                // delete both the structure and resource property values mapped to the specified resource
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_DELETE_ALL_STRUCTURE_AND_RESOURCE_VALUES");
                stmt.setString(1, resource.getResourceId().toString());
                stmt.setInt(2, CmsProperty.C_RESOURCE_RECORD_MAPPING);
                stmt.setString(3, resource.getStructureId().toString());
                stmt.setInt(4, CmsProperty.C_STRUCTURE_RECORD_MAPPING);
            } else if (deleteOption == CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_VALUES) {
                // delete the structure values mapped to the specified resource
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_DELETE_ALL_VALUES_FOR_MAPPING_TYPE");
                stmt.setString(1, resource.getStructureId().toString());
                stmt.setInt(2, CmsProperty.C_STRUCTURE_RECORD_MAPPING);                
            } else if (deleteOption == CmsProperty.C_DELETE_OPTION_DELETE_RESOURCE_VALUES) {
                // delete the resource property values mapped to the specified resource
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_DELETE_ALL_VALUES_FOR_MAPPING_TYPE");
                stmt.setString(1, resource.getResourceId().toString());
                stmt.setInt(2, CmsProperty.C_RESOURCE_RECORD_MAPPING);
            }
            
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deletePropertyDefinition(org.opencms.file.CmsPropertydefinition)
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
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Shutting down        : " + this.getClass().getName() + " ... ok!");
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
     * @see org.opencms.db.I_CmsDriver#init(org.apache.commons.collections.ExtendedProperties, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(ExtendedProperties configuration, List successiveDrivers, CmsDriverManager driverManager) {
        String offlinePoolUrl = null;
        String onlinePoolUrl = null;
        String backupPoolUrl = null;
        boolean hasDistinctPoolUrls = false;

        if ((offlinePoolUrl = configuration.getString("db.vfs.pool")) == null) {
            hasDistinctPoolUrls = true;
            offlinePoolUrl = configuration.getString("db.vfs.offline.pool");
            onlinePoolUrl = configuration.getString("db.vfs.online.pool");
            backupPoolUrl = configuration.getString("db.vfs.backup.pool");
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
     * @see org.opencms.db.I_CmsVfsDriver#initQueries()
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
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return returnValue;
    }

    /**
     * Counts the number of siblings of a resource.<p>
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

            if (res.next()) {
                count = res.getInt(1);
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return count;
    }

    /**
     * Filters all resources that are marked as undeleted.<p>
     * 
     * @param resources list of resources
     * @return all resources that are markes as deleted
     */
    protected List internalFilterUndeletedResources(List resources) {
        List undeletedResources = new ArrayList();

        for (int i = 0; i < resources.size(); i++) {
            CmsResource resource = (CmsResource)resources.get(i);
            if (resource.getState() != I_CmsConstants.C_STATE_DELETED) {
                undeletedResources.add(resource);
            }
        }

        return undeletedResources;
    }

    /**
     * Removes a resource physically in the database.<p>
     *
     * @param currentProject the current project
     * @param resource the folder to remove
     * @throws CmsException if something goes wrong
     */
    protected void internalRemoveFolder(CmsProject currentProject, CmsResource resource) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(currentProject);

            // delete the structure record            
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_STRUCTURE_DELETE_BY_STRUCTUREID");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(null, stmt, null);

            // delete the resource record
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCES_DELETE_BY_RESOURCEID");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readChildResources(org.opencms.file.CmsProject, CmsResource, boolean, boolean)
     */
    public List readChildResources(CmsProject currentProject, CmsResource resource, boolean getFolders, boolean getFiles) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        String query = null;
        String orderClause = " ORDER BY CMS_T_STRUCTURE.RESOURCE_NAME";

        String resourceTypeClause;
        List subFolders;
        List subFiles;
        
        if (getFolders && getFiles) {
            resourceTypeClause = "";
            subFolders = new ArrayList();
            subFiles = new ArrayList();
        } else if (getFolders) {
            resourceTypeClause = " AND CMS_T_RESOURCES.RESOURCE_TYPE=0";
            subFolders = new ArrayList();
            subFiles = null;
        } else {
            resourceTypeClause = " AND CMS_T_RESOURCES.RESOURCE_TYPE<>0";
            subFolders = null;
            subFiles = new ArrayList();
        }
        
        try {
            conn = m_sqlManager.getConnection(currentProject);
            query = m_sqlManager.readQuery(currentProject, "C_RESOURCES_GET_SUBRESOURCES") + CmsSqlManager.replaceTableKey(currentProject.getId(), resourceTypeClause + orderClause);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, query);
            stmt.setString(1, resource.getStructureId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                int type = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));                
                if (type == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
                    subFolders.add(createFolder(res, currentProject.getId(), false));
                } else {
                    subFiles.add(createFile(res, currentProject.getId(), false));
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        // this is required in order to get the "folders first" sort order 
        if (getFolders && getFiles) {
            subFolders.addAll(subFiles);
            return subFolders;
        } else if (getFolders) {
            return subFolders;
        } 
        return subFiles;
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
            res = stmt.executeQuery();
            
            if (res.next()) {
                file = createFile(res, projectId);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
                
                // check if this resource is marked as deleted
                if (file.getState() == org.opencms.main.I_CmsConstants.C_STATE_DELETED && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + ".readFile] " + file.getName(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + ".readFile] " + structureId.toString());
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
            stmt.setString(1, resourceId.toString());
            
            res = stmt.executeQuery();
            // create new file
            if (res.next()) {
                file = createFile(res, projectId, false);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
                
                // check if this resource is marked as deleted
                if ((file.getState() == org.opencms.main.I_CmsConstants.C_STATE_DELETED) && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + ".readFileHeader/2] " + file.getName(), CmsException.C_RESOURCE_DELETED);
                }
            } else {                               
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + ".readFileHeader/2] " + resourceId);
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

            res = stmt.executeQuery();
            if (res.next()) {
                file = createFile(res, projectId, false);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }

                // check if this resource is marked as deleted
                if ((file.getState() == org.opencms.main.I_CmsConstants.C_STATE_DELETED) && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + ".readFileHeader/3] " + file.getName(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + ".readFileHeader/3] " + filename);
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
        List resources = new ArrayList();
        CmsResource currentResource;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_CHANGED_FILEHEADERS");

            res = stmt.executeQuery();
            while (res.next()) {
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
    public List readFiles(int projectId, int resourcetype) throws CmsException {
        List files = new ArrayList();
        CmsFile file;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_FILESBYTYPE");
            stmt.setInt(1, resourcetype);
            
            res = stmt.executeQuery();
            while (res.next()) {
                file = createFile(res, projectId, true);
                files.add(file);
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
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + ".readFolder/1] " + folderId);
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
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + ".readFolder/2] " + foldername);
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
        List folders = new ArrayList();
        CmsFolder currentFolder;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_CHANGED_FOLDERS_BY_PROJECT");

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
     * @see org.opencms.db.I_CmsVfsDriver#readFolderTree(org.opencms.file.CmsProject, org.opencms.file.CmsResource)
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
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinition(java.lang.String, int, int)
     */
    public CmsPropertydefinition readPropertyDefinition(String name, int projectId, int mappingtype) throws CmsException {
        CmsPropertydefinition propDef = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_READ");

            stmt.setString(1, name);
            stmt.setInt(2, mappingtype);
            res = stmt.executeQuery();

            // if resultset exists - return it
            if (res.next()) {
                propDef = new CmsPropertydefinition(res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_ID")), res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")), res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_PROPERTYDEF_MAPPING_TYPE")));
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
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinitions(int, int)
     */
    public List readPropertyDefinitions(int projectId, int mappingtype) throws CmsException {
        ArrayList propertyDefinitions = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_READALL");
            stmt.setInt(1, mappingtype);
            
            res = stmt.executeQuery();
            while (res.next()) {
                propertyDefinitions.add(new CmsPropertydefinition(res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_ID")), res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")), res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_PROPERTYDEF_MAPPING_TYPE"))));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return propertyDefinitions;
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
            stmt.setLong(1, starttime);
            stmt.setLong(2, endtime);
            
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
     * @see org.opencms.db.I_CmsVfsDriver#readResources(int, int, int)
     */
    public List readResources(int projectId, int state, int mode) throws CmsException {
        List result = new ArrayList();

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            if (mode == I_CmsConstants.C_READMODE_MATCHSTATE) {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCE_IN_PROJECT_WITH_STATE");
                stmt.setInt(1, projectId);
                stmt.setInt(2, state);
                stmt.setInt(3, state);
                stmt.setInt(4, state);
                stmt.setInt(5, state);
            } else if (mode == I_CmsConstants.C_READMODE_UNMATCHSTATE) {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCE_IN_PROJECT_WITHOUT_STATE");
                stmt.setInt(1, projectId);
                stmt.setInt(2, state);
                stmt.setInt(3, state);
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCE_IN_PROJECT_IGNORE_STATE");
                stmt.setInt(1, projectId);
            }
            
            res = stmt.executeQuery();
            while (res.next()) {
                CmsResource resource = createResource(res, projectId);
                result.add(resource);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception exc) {
            throw new CmsException("readResources" + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return result;
    }
    
    
    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResources(int, java.lang.String)
     */
    public List readResources(int projectId, String propertyDefName) throws CmsException {
        List resources = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF");
            stmt.setString(1, propertyDefName);
            stmt.setInt(2, CmsProperty.C_STRUCTURE_RECORD_MAPPING);
            stmt.setInt(3, CmsProperty.C_RESOURCE_RECORD_MAPPING);
            res = stmt.executeQuery();

            while (res.next()) {
                CmsResource resource = createResource(res, projectId);
                resources.add(resource);
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
     * @see org.opencms.db.I_CmsVfsDriver#readSiblings(org.opencms.file.CmsProject, org.opencms.file.CmsResource, boolean)
     */
    public List readSiblings(CmsProject currentProject, CmsResource resource, boolean includeDeleted) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        CmsResource currentResource = null;
        List vfsLinks = new ArrayList();

        try {
            conn = m_sqlManager.getConnection(currentProject);
            if (includeDeleted) {
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_SELECT_VFS_LINKS");
            } else {
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_SELECT_NONDELETED_VFS_LINKS");
            }
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
     * @see org.opencms.db.I_CmsVfsDriver#removeFile(org.opencms.file.CmsProject, org.opencms.file.CmsResource, boolean)
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

                if (!resource.getContentId().equals(CmsUUID.getNullUUID())) {
                    // delete the content record
                    stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_FILE_CONTENT_DELETE");
                    stmt.setString(1, resource.getContentId().toString());
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
     * @see org.opencms.db.I_CmsVfsDriver#removeFolder(org.opencms.file.CmsProject, CmsResource)
     */
    public void removeFolder(CmsProject currentProject, CmsResource resource) throws CmsException {
        // the current implementation only deletes empty folders
        // check if the folder has any files in it
        List files = readChildResources(currentProject, resource, false, true);
        files = internalFilterUndeletedResources(files);
        
        if (files.size() == 0) {
            // check if the folder has any folders in it
            List folders = readChildResources(currentProject, resource, true, false);
            folders = internalFilterUndeletedResources(folders);
            
            if (folders.size() == 0) {
                internalRemoveFolder(currentProject, resource);
            } else {
                String errorResNames = "";
                
                for (int i = 0, n = folders.size(); i < n; i++) {
                    CmsResource errorRes = (CmsResource)folders.get(i);
                    errorResNames += "[" + errorRes.getName() + "]";
                }
                
                throw new CmsException("[" + this.getClass().getName() + "] " + resource.getName() + errorResNames, CmsException.C_NOT_EMPTY);
            }
        } else {
            String errorResNames = "";
            
            for (int i = 0, n = files.size(); i < n; i++) {
                CmsResource errorRes = (CmsResource)files.get(i);
                errorResNames += "[" + errorRes.getName() + "]";
            }
            
            throw new CmsException("[" + this.getClass().getName() + "] " + resource.getName() + errorResNames, CmsException.C_NOT_EMPTY);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#replaceResource(org.opencms.file.CmsUser, org.opencms.file.CmsProject, org.opencms.file.CmsResource, byte[], int, int)
     */
    public void replaceResource(CmsUser currentUser, CmsProject currentProject, CmsResource res, byte[] resContent, int newResType, int loaderId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // write the file content
            if (resContent != null) {
                writeContent(currentProject, res.getContentId(), resContent, false);
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
            m_sqlManager.closeAll(conn, stmt, res);
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
     * @see org.opencms.db.I_CmsVfsDriver#writeContent(CmsProject, org.opencms.util.CmsUUID, byte[], boolean)
     */
    public void writeContent(CmsProject project, CmsUUID contentId, byte[] content, boolean writeBackup) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (writeBackup) {
                conn = m_sqlManager.getConnectionForBackup();
                stmt = m_sqlManager.getPreparedStatement(conn, "C_FILES_UPDATE_BACKUP");
            } else {
                conn = m_sqlManager.getConnection(project);
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_FILES_UPDATE");
            }

            // update the file content in the FILES database.
            if (content.length < 2000) {
                stmt.setBytes(1, content);
            } else {
                stmt.setBinaryStream(1, new ByteArrayInputStream(content), content.length);
            }

            stmt.setString(2, contentId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResource(org.opencms.file.CmsProject, CmsResource, int)
     */
    public void writeResource(CmsProject project, CmsResource resource, int changed) throws CmsException {

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

        if (changed == CmsDriverManager.C_UPDATE_RESOURCE_STATE) {
            resourceState = org.opencms.main.I_CmsConstants.C_STATE_CHANGED;
        } else if (changed == CmsDriverManager.C_UPDATE_STRUCTURE_STATE) {
            structureState = org.opencms.main.I_CmsConstants.C_STATE_CHANGED;
        } else if (changed != CmsDriverManager.C_NOTHING_CHANGED) {
            resourceState = org.opencms.main.I_CmsConstants.C_STATE_CHANGED;
            structureState = org.opencms.main.I_CmsConstants.C_STATE_CHANGED;
        }

        try {
            conn = m_sqlManager.getConnection(project);

            // update the resource
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCES");
            stmt.setInt(1, resource.getTypeId());
            stmt.setInt(2, resource.getFlags());
            stmt.setInt(3, resource.getLoaderId());
            stmt.setLong(4, resourceDateModified);
            stmt.setString(5, resource.getUserLastModified().toString());
            stmt.setInt(6, resourceState);
            stmt.setInt(7, resource.getLength());
            stmt.setString(8, resource.getContentId().toString());
            stmt.setString(9, CmsUUID.getNullUUID().toString());
            stmt.setInt(10, project.getId());
            stmt.setInt(11, internalCountSiblings(project.getId(), resource.getResourceId()));
            stmt.setString(12, resource.getResourceId().toString());
            stmt.executeUpdate();
            m_sqlManager.closeAll(null, stmt, null);

            // update the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_STRUCTURE");
            stmt.setString(1, resource.getParentStructureId().toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setString(3, resource.getName());
            stmt.setInt(4, structureState);
            stmt.setLong(5, resource.getDateReleased());
            stmt.setLong(6, resource.getDateExpired());
            stmt.setString(7, resource.getStructureId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeLastModifiedProjectId(org.opencms.file.CmsProject, int, org.opencms.file.CmsResource)
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
     * @see org.opencms.db.I_CmsVfsDriver#publishResource(org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.file.CmsResource, boolean)
     */
    public void publishResource(CmsProject onlineProject, CmsResource onlineResource, CmsResource offlineResource, boolean writeFileContent) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        int resourceSize = offlineResource.getLength();

        try {
            conn = m_sqlManager.getConnection(onlineProject);

            if (validateResourceIdExists(onlineProject.getId(), offlineResource.getResourceId())) {

                // the resource record exists online already

                if (writeFileContent && offlineResource.isFile()) {
                    // update the online file content
                    writeContent(onlineProject, offlineResource.getContentId(), ((CmsFile)offlineResource).getContents(), false);
                }

                // update the online resource record
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_UPDATE_RESOURCES");
                stmt.setInt(1, offlineResource.getTypeId());
                stmt.setInt(2, offlineResource.getFlags());
                stmt.setInt(3, offlineResource.getLoaderId());
                stmt.setLong(4, offlineResource.getDateLastModified());
                stmt.setString(5, offlineResource.getUserLastModified().toString());
                stmt.setInt(6, I_CmsConstants.C_STATE_UNCHANGED);
                stmt.setInt(7, resourceSize);
                stmt.setString(8, offlineResource.getContentId().toString());
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
                stmt.setLong(5, offlineResource.getDateReleased());
                stmt.setLong(6, offlineResource.getDateExpired());
                stmt.setString(7, offlineResource.getStructureId().toString());
                stmt.executeUpdate();
            } else {

                // the resource record does NOT exist online yet

                if (writeFileContent && offlineResource.isFile() && !validateContentIdExists(onlineProject.getId(), offlineResource.getContentId())) {
                    // create the file content online
                    resourceSize = offlineResource.getLength();
                    createContent(onlineProject, offlineResource.getContentId(), ((CmsFile)offlineResource).getContents(), 0, false);
                }

                // create the resource record online
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_WRITE");
                stmt.setString(1, offlineResource.getResourceId().toString());
                stmt.setInt(2, offlineResource.getTypeId());
                stmt.setInt(3, offlineResource.getFlags());
                stmt.setString(4, offlineResource.getContentId().toString());
                stmt.setInt(5, offlineResource.getLoaderId());
                stmt.setLong(6, offlineResource.getDateCreated());
                stmt.setString(7, offlineResource.getUserCreated().toString());
                stmt.setLong(8, offlineResource.getDateLastModified());
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
                stmt.setLong(6, offlineResource.getDateReleased());
                stmt.setLong(7, offlineResource.getDateExpired());
                stmt.executeUpdate();

            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResourceState(org.opencms.file.CmsProject, org.opencms.file.CmsResource, int)
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
                stmt.setLong(2, resource.getDateLastModified());
                stmt.setString(3, resource.getUserLastModified().toString());
                stmt.setInt(4, project.getId());
                stmt.setString(5, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(null, stmt, null);
                
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RELEASE_EXPIRED");
                stmt.setLong(1, resource.getDateReleased());
                stmt.setLong(2, resource.getDateExpired());
                stmt.setString(3, resource.getStructureId().toString());
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
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#getSqlManager()
     */    
    public CmsSqlManager getSqlManager() {
        return m_sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyObject(java.lang.String, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public CmsProperty readPropertyObject(String key, CmsProject project, CmsResource resource) throws CmsException {
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String propertyValue = null;
        int mappingType = -1;
        CmsProperty property = null;
        int resultSize = 0;

        String resourceName = resource.getRootPath();
        if ((resource.isFolder()) && (!resourceName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR))) {
            resourceName += I_CmsConstants.C_FOLDER_SEPARATOR;
}
        try {
            conn = m_sqlManager.getConnection(project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project.getId(), "C_PROPERTIES_READ");

            stmt.setString(1, key);
            stmt.setInt(2, I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);
            stmt.setString(3, resource.getStructureId().toString());
            stmt.setString(4, resource.getResourceId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                if (resultSize >= 2) {
                    throw new CmsException("Properties for resource " + resource.getRootPath() + " are inconsitent! A resource may have set max. two property values, one mapped to it's structure and one mapped to it's resource record.", CmsException.C_UNKNOWN_EXCEPTION);
                }

                if (property == null) {
                    property = new CmsProperty();
                    property.setKey(key);
                }

                propertyValue = res.getString(1);
                mappingType = res.getInt(2);

                if (mappingType == CmsProperty.C_STRUCTURE_RECORD_MAPPING) {
                    property.setStructureValue(propertyValue);
                } else if (mappingType == CmsProperty.C_RESOURCE_RECORD_MAPPING) {
                    property.setResourceValue(propertyValue);
                } else {
                    throw new CmsException("Unknown property value mapping type found: " + mappingType, CmsException.C_UNKNOWN_EXCEPTION);
                }

                resultSize++;
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return (property != null) ? property : CmsProperty.getNullProperty();
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyObjects(org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public List readPropertyObjects(CmsProject project, CmsResource resource) throws CmsException {
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String propertyKey = null;
        String propertyValue = null;
        int mappingType = -1;
        Map propertyMap = new HashMap();
        CmsProperty property = null;

        String resourceName = resource.getRootPath();
        if ((resource.isFolder()) && (!resourceName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR))) {
            resourceName += I_CmsConstants.C_FOLDER_SEPARATOR;
        }

        try {
            conn = m_sqlManager.getConnection(project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project.getId(), "C_PROPERTIES_READALL");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setInt(3, I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);
            res = stmt.executeQuery();

            while (res.next()) {
                propertyKey = null;
                propertyValue = null;
                mappingType = -1;
                
                propertyKey = res.getString(1);
                propertyValue = res.getString(2);
                mappingType = res.getInt(3);

                if ((property = (CmsProperty) propertyMap.get(propertyKey)) == null) {
                    // there doesn't exist a property object for this key yet
                    property = new CmsProperty();
                    property.setKey(propertyKey);
                    propertyMap.put(propertyKey, property);
                }

                if (mappingType == CmsProperty.C_STRUCTURE_RECORD_MAPPING) {
                    // this property value is mapped to a structure record
                    property.setStructureValue(propertyValue);
                } else if (mappingType == CmsProperty.C_RESOURCE_RECORD_MAPPING) {
                    // this property value is mapped to a resource record
                    property.setResourceValue(propertyValue);
                } else {
                    throw new CmsException("Unknown property value mapping type found: " + mappingType, CmsException.C_UNKNOWN_EXCEPTION);
                }
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return new ArrayList(propertyMap.values());
    }
    
    /**
     * @see org.opencms.db.I_CmsVfsDriver#writePropertyObjects(org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.List)
     */
    public void writePropertyObjects(CmsProject project, CmsResource resource, List properties) throws CmsException {

        CmsProperty property = null;

        for (int i = 0; i < properties.size(); i++) {
            property = (CmsProperty)properties.get(i);
            writePropertyObject(project, resource, property);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writePropertyObject(org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.file.CmsProperty)
     */
    public void writePropertyObject(CmsProject project, CmsResource resource, CmsProperty property) throws CmsException {
        CmsPropertydefinition propertyDefinition = null;
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
            propertyDefinition = readPropertyDefinition(property.getKey(), project.getId(), I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);
        } catch (CmsException e) {
            propertyDefinition = null;
        }

        if (propertyDefinition == null) {
            if (property.autoCreatePropertyDefinition()) {
                // create a missing property definition optionally
                propertyDefinition = createPropertyDefinition(property.getKey(), project.getId(), I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);
                if (project.getId() >= 0) {
                    try {
                        // create the property definition implicitly in the backup tables
                        m_driverManager.getBackupDriver().createBackupPropertyDefinition(property.getKey(), I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);
                    } catch (Exception e) {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("[" + this.getClass().getName() + "] " + e.toString());
                        }
                    }
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + ".writePropertyObject/1] " + property.getKey(), CmsException.C_NOT_FOUND);
            }
        }
        
        String resourceName = resource.getRootPath();
        if (resource.isFolder() && !resourceName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            resourceName += I_CmsConstants.C_FOLDER_SEPARATOR;
        }        

        try {
            // read the existing property to test if we need the 
            // insert or update query to write a property value
            existingProperty = readPropertyObject(propertyDefinition.getName(), project, resource);
            
            if (existingProperty.isIdentical(property)) {
                
                // property already has the identical values set, no write required
                return;
            }
            
            conn = m_sqlManager.getConnection(project.getId());

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
                    mappingType = CmsProperty.C_STRUCTURE_RECORD_MAPPING;
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
                    mappingType = CmsProperty.C_RESOURCE_RECORD_MAPPING;
                    id = resource.getResourceId();
                    existsPropertyValue = existingProperty.getResourceValue() != null;
                }
                
                // 2) execute the SQL query
                
                if (!deletePropertyValue) {
                    // insert/update the property value                    
                    if (existsPropertyValue) {
                        // {structure|resource} property value already exists- use update statement
                        stmt = m_sqlManager.getPreparedStatement(conn, project.getId(), "C_PROPERTIES_UPDATE");
                        stmt.setString(1, m_sqlManager.validateNull(value));
                        stmt.setString(2, id.toString());
                        stmt.setInt(3, mappingType);
                        stmt.setInt(4, propertyDefinition.getId());
                    } else {
                        // {structure|resource} property value doesen't exist- use create statement
                        stmt = m_sqlManager.getPreparedStatement(conn, project.getId(), "C_PROPERTIES_CREATE");
                        stmt.setInt(1, m_sqlManager.nextId(project.getId(), m_sqlManager.readQuery(project.getId(), "C_TABLE_PROPERTIES")));
                        stmt.setInt(2, propertyDefinition.getId());
                        stmt.setString(3, id.toString());
                        stmt.setInt(4, mappingType);
                        stmt.setString(5, m_sqlManager.validateNull(value));
                    }
                } else {
                    // {structure|resource} property value marked as deleted- use delete statement
                    stmt = m_sqlManager.getPreparedStatement(conn, project.getId(), "C_PROPERTIES_DELETE");
                    stmt.setInt(1, propertyDefinition.getId());
                    stmt.setString(2, id.toString());
                    stmt.setInt(3, mappingType);                    
                }
                
                stmt.executeUpdate();
                m_sqlManager.closeAll(null, stmt, null);                
            }         
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }    
    
    /**
     * @see org.opencms.db.I_CmsVfsDriver#createResource(org.opencms.file.CmsProject, org.opencms.file.CmsResource, byte[])
     */
    public CmsResource createResource(CmsProject project, CmsResource resource, byte[] content) throws CmsException {

        CmsUUID newStructureId = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        // check if the file has it's full resource name set
        if (!resource.hasFullResourceName()) {
            throw new RuntimeException("Full resource name not set in " + resource.getName());
        }

        // check the resource name
        if (resource.getName().length() > I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME) {
            throw new CmsException("The resource name '" + resource.getName() + "' is too long! (max. allowed length must be <= " + I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME + " chars.!)", CmsException.C_BAD_NAME);
        }

        // set the resource state and modification dates
        int newState;
        long dateModified;
        long dateCreated;
        
        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            newState = I_CmsConstants.C_STATE_UNCHANGED;
            dateCreated = resource.getDateCreated();
            dateModified = resource.getDateLastModified();
        } else {
            newState = I_CmsConstants.C_STATE_NEW;
            if (resource.isTouched()) {
                dateCreated = resource.getDateCreated();
                dateModified = resource.getDateLastModified();
            } else {
                dateCreated = System.currentTimeMillis();
                dateModified = dateCreated;
            }
        }

        // check if the resource already exists
        CmsResource res = null;
        newStructureId = resource.getStructureId();
        
        try {
            res = readFileHeader(
                project.getId(), 
                resource.getParentStructureId(), 
                resource.getName(), 
                true);
            
            res.setFullResourceName(resource.getRootPath());
            
            if (res.getState() == I_CmsConstants.C_STATE_DELETED) {
                // if an existing resource is deleted, it will be finally removed now
                // but we have to reuse its id in order to avoid orphanes in the online project
                newStructureId = res.getStructureId();
                newState = I_CmsConstants.C_STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = readSiblings(project, res, false);
                deleteProperties(project.getId(), res, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                removeFile(project, res, true);

                int warning = 0;
                // TODO: the deleted properties are not handled correct in the cache
                OpenCms.fireCmsEvent(new CmsEvent(
                    new CmsObject(),
                    I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
                    Collections.singletonMap("resources", modifiedResources)));
                OpenCms.fireCmsEvent(new CmsEvent(
                    new CmsObject(),
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.singletonMap("resource", res)));
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

            // write the structure record
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, newStructureId.toString());
            stmt.setString(2, resource.getParentStructureId().toString());
            stmt.setString(3, resource.getResourceId().toString());
            stmt.setString(4, resource.getName());
            stmt.setInt(5, newState);
            stmt.setLong(6, resource.getDateReleased());
            stmt.setLong(7, resource.getDateExpired());
            stmt.executeUpdate();
            m_sqlManager.closeAll(null, stmt, null);

            if (!validateResourceIdExists(project.getId(), resource.getResourceId())) {

                // create the resource record
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_WRITE");
                stmt.setString(1, resource.getResourceId().toString());
                stmt.setInt(2, resource.getTypeId());
                stmt.setInt(3, resource.getFlags());
                stmt.setString(4, resource.getContentId().toString());
                stmt.setInt(5, resource.getLoaderId());
                stmt.setLong(6, dateCreated);
                stmt.setString(7, resource.getUserCreated().toString());
                stmt.setLong(8, dateModified);
                stmt.setString(9, resource.getUserLastModified().toString());
                stmt.setInt(10, newState);
                stmt.setInt(11, resource.getLength());
                stmt.setString(12, CmsUUID.getNullUUID().toString());
                stmt.setInt(13, project.getId());
                stmt.setInt(14, 1);
                stmt.executeUpdate();   
                
                if (resource.isFile() && content != null) {
                    // create the file content
                    createContent(project, resource.getContentId(), content, 0, false);                    
                }
                
            } else {

                if ((content != null) || (resource.getState() != I_CmsConstants.C_STATE_KEEP)) {
                    // update the resource record only if state has changed or new content is provided
                    stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCES");
                    stmt.setInt(1, resource.getTypeId());
                    stmt.setInt(2, resource.getFlags());
                    stmt.setInt(3, resource.getLoaderId());
                    stmt.setLong(4, dateModified);
                    stmt.setString(5, resource.getUserLastModified().toString());
                    stmt.setInt(6, I_CmsConstants.C_STATE_CHANGED);
                    stmt.setInt(7, resource.getLength());
                    stmt.setString(8, resource.getContentId().toString());
                    stmt.setString(9, CmsUUID.getNullUUID().toString());
                    stmt.setInt(10, project.getId());
                    stmt.setInt(11, internalCountSiblings(project.getId(), resource.getResourceId()));
                    stmt.setString(12, resource.getResourceId().toString());
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(null, stmt, null);
                }
                
                if (resource.isFile()) {
                    if (content != null) {
                        
                        // update the file content
                        writeContent(project, resource.getContentId(), content, false);
                        
                    } else if (resource.getState() == I_CmsConstants.C_STATE_KEEP) {

                        // special case sibling creation - update the link Count
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
                        m_sqlManager.closeAll(null, stmt, null);                                                
                    }
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readFileHeader(project.getId(), newStructureId, false);
    }    

}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsVfsDriver.java,v $
 * Date   : $Date: 2004/10/25 14:18:21 $
 * Version: $Revision: 1.209 $
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
import org.opencms.db.I_CmsRuntimeInfo;
import org.opencms.db.I_CmsRuntimeInfoFactory;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertydefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
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

import org.apache.commons.collections.ExtendedProperties;


/**
 * Generic (ANSI-SQL) database server implementation of the VFS driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * @version $Revision: 1.209 $ $Date: 2004/10/25 14:18:21 $
 * @since 5.1
 */
public class CmsVfsDriver extends Object implements I_CmsDriver, I_CmsVfsDriver {

    /** Operator to concatenate include conditions. */
    static String C_BEGIN_INCLUDE_CONDITION = " AND (";
    
    /** Operator to concatenate exclude conditions. */
    static String C_BEGIN_EXCLUDE_CONDITION = " AND NOT (";
    
    /** String to end a single condition. */
    static String C_END_CONDITION = ") ";
    
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
        if (resourceType == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
            resourcePath = addTrailingSeparator(resourcePath);
        }
        
        return new CmsFile(structureId, resourceId, contentId, resourcePath, resourceType, resourceFlags, projectId, newState, dateCreated, userCreated, dateLastModified, userLastModified, dateReleased, dateExpired, siblingCount, resourceSize, content);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(java.sql.ResultSet, int, boolean)
     */
    public CmsFile createFile(ResultSet res, int projectId, boolean hasFileContentInResultSet) throws SQLException {
        byte[] content = null;
        
        int resProjectId = I_CmsConstants.C_UNKNOWN_ID;

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
        if (resourceType == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
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
     * @see org.opencms.db.I_CmsVfsDriver#createContent(I_CmsRuntimeInfo, CmsProject, org.opencms.util.CmsUUID, byte[], int)
     */
    public void createContent(I_CmsRuntimeInfo runtimeInfo, CmsProject project, CmsUUID resourceId, byte[] content, int versionId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
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
        if (resourceType == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
            resourcePath = addTrailingSeparator(resourcePath);
        }
        
        int newState = (structureState > resourceState) ? structureState : resourceState;

        return new CmsFolder(structureId, resourceId, resourcePath, resourceType, resourceFlags, resProjectId, newState, dateCreated, userCreated, dateLastModified, userLastModified, siblingCount, dateReleased, dateExpired);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createPropertyDefinition(I_CmsRuntimeInfo, int, java.lang.String, int)
     */
    public CmsPropertydefinition createPropertyDefinition(I_CmsRuntimeInfo runtimeInfo, int projectId, String name, int mappingtype) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        // TODO switch the property def. PK into a CmsUUID PK

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_CREATE");
            stmt.setString(1, new CmsUUID().toString());
            stmt.setString(2, name);
            stmt.setInt(3, mappingtype);
            stmt.executeUpdate();         
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }

        return readPropertyDefinition(runtimeInfo, name, projectId, mappingtype);
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
        if (resourceType == CmsResourceTypeFolder.C_RESOURCE_TYPE_ID) {
            // ensure trailing slash in path
            resourcePath = addTrailingSeparator(resourcePath);
            
            // folders must have -1 size
            resourceSize = -1;
        } else {
            resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        }
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        int siblingCount = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIBLING_COUNT"));

        int newState = (structureState > resourceState) ? structureState : resourceState;

        CmsResource newResource = new CmsResource(structureId, resourceId, resourcePath, resourceType, resourceFlags, resourceProjectLastModified, newState, dateCreated, userCreated, dateLastModified, userLastModified, dateReleased, dateExpired, siblingCount, resourceSize);

        return newResource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createSibling(I_CmsRuntimeInfo, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.lang.String)
     */
    public void createSibling(I_CmsRuntimeInfo runtimeInfo, CmsProject project, CmsResource resource, String resourcename) throws CmsException {
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
            res = readFileHeader(runtimeInfo, project.getId(), resource.getRootPath(), true);

            if (res.getState() == I_CmsConstants.C_STATE_DELETED) {
                // if an existing resource is deleted, it will be finally removed now
                // but we have to reuse its id in order to avoid orphanes in the online project
                newStructureId = res.getStructureId();
                newState = I_CmsConstants.C_STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = readSiblings(runtimeInfo, project, res, false);
                int propertyDeleteOption = (res.getSiblingCount() > 1) ? CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_VALUES : CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                deletePropertyObjects(runtimeInfo, project.getId(), res, propertyDeleteOption);
                removeFile(runtimeInfo, project, res, true);

                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCES_MODIFIED, Collections.singletonMap("resources", modifiedResources)));
                OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", res)));
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
        if (!validateResourceIdExists(runtimeInfo, project.getId(), resource.getResourceId())) {
            throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + "] ");
        }

        // write a new structure referring to the resource
        try {
            conn = m_sqlManager.getConnection(runtimeInfo);

            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, newStructureId.toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setString(3, resource.getRootPath());
            stmt.setInt(4, newState);
            stmt.setLong(5, resource.getDateReleased());
            stmt.setLong(6, resource.getDateExpired());
            stmt.executeUpdate();

            m_sqlManager.closeAll(null, null, stmt, null);

            // update the link Count
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_SIBLING_COUNT");
            stmt.setInt(1, this.internalCountSiblings(null, project.getId(), resource.getResourceId()));
            stmt.setString(2, resource.getResourceId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(null, null, stmt, null);

            // update the resource flags
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_FLAGS");
            stmt.setInt(1, resource.getFlags());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }

        //return readFileHeader(project.getId(), newStructureId, false);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deletePropertyObjects(I_CmsRuntimeInfo, int, org.opencms.file.CmsResource, int)
     */
    public void deletePropertyObjects(I_CmsRuntimeInfo runtimeInfo, int projectId, CmsResource resource, int deleteOption) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String resourceName = resource.getRootPath();

        // add folder separator to folder name if it is not present
        if (resource.isFolder() && !resourceName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            resourceName += I_CmsConstants.C_FOLDER_SEPARATOR;
        }

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            
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
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deletePropertyDefinition(I_CmsRuntimeInfo, org.opencms.file.CmsPropertydefinition)
     */
    public void deletePropertyDefinition(I_CmsRuntimeInfo runtimeInfo, CmsPropertydefinition metadef) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (internalCountProperties(metadef) != 0) {
                throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(), CmsException.C_UNKNOWN_EXCEPTION);
            }
            
            conn = m_sqlManager.getConnection(runtimeInfo);
            
            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    // delete the offline propertydef
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_DELETE");
                } else if (i == 1) {
                    // delete the online propertydef
                    stmt = m_sqlManager.getPreparedStatement(conn, I_CmsConstants.C_PROJECT_ONLINE_ID, "C_PROPERTYDEF_DELETE");
                }
                
                stmt.setString(1, metadef.getId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(null, conn, stmt, null);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
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
     * @see org.opencms.db.I_CmsDriver#init(org.apache.commons.collections.ExtendedProperties, java.util.List, org.opencms.db.CmsDriverManager, I_CmsRuntimeInfoFactory)
     */
    public void init(ExtendedProperties configuration, List successiveDrivers, CmsDriverManager driverManager, I_CmsRuntimeInfoFactory runtimeInfoFactory) {
        
        String poolUrl = configuration.getString("db.vfs.pool");
        String classname = configuration.getString("db.vfs.sqlmanager");
        m_sqlManager = this.initSqlManager(classname);
        m_sqlManager.init(I_CmsVfsDriver.C_DRIVER_TYPE_ID, poolUrl);

        m_driverManager = driverManager;

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Assign. pool         : " + poolUrl);
        }

        if (successiveDrivers != null && !successiveDrivers.isEmpty()) {
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(this.getClass().toString() + " does not support successive drivers");
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
            stmt.setString(1, metadef.getId().toString());
            res = stmt.executeQuery();

            if (res.next()) {
                returnValue = res.getInt(1);
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(), CmsException.C_UNKNOWN_EXCEPTION);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return returnValue;
    }

    /**
     * Counts the number of siblings of a resource.<p>
     * 
     * @param runtimeInfo a Cms runtimeInfo
     * @param projectId the current project id
     * @param resourceId the resource id to count the number of siblings from
     * 
     * @return number of siblings
     * @throws CmsException if something goes wrong
     */
    protected int internalCountSiblings(I_CmsRuntimeInfo runtimeInfo, int projectId, CmsUUID resourceId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        int count = 0;

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);

            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_COUNTLINKS");
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                count = res.getInt(1);
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, res);
        }

        return count;
    }

    /**
     * Filters all resources that are marked as undeleted.<p>
     * 
     * @param resources list of resources
     * @return all resources that are markes as deleted
     */
//    protected List internalFilterUndeletedResources(List resources) {
//        List undeletedResources = new ArrayList();
//
//        for (int i = 0; i < resources.size(); i++) {
//            CmsResource resource = (CmsResource)resources.get(i);
//            if (resource.getState() != I_CmsConstants.C_STATE_DELETED) {
//                undeletedResources.add(resource);
//            }
//        }
//
//        return undeletedResources;
//    }

    /**
     * Removes a resource physically in the database.<p>
     * 
     * @param runtimeInfo a Cms runtimeInfo
     * @param currentProject the current project
     * @param resource the folder to remove
     *
     * @throws CmsException if something goes wrong
     */
    protected void internalRemoveFolder(I_CmsRuntimeInfo runtimeInfo, CmsProject currentProject, CmsResource resource) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);

            // delete the structure record            
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_STRUCTURE_DELETE_BY_STRUCTUREID");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(null, null, stmt, null);

            // delete the resource record
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCES_DELETE_BY_RESOURCEID");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readChildResources(I_CmsRuntimeInfo, org.opencms.file.CmsProject, CmsResource, boolean, boolean)
     */
    public List readChildResources(I_CmsRuntimeInfo runtimeInfo, CmsProject currentProject, CmsResource resource, boolean getFolders, boolean getFiles) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        String query = null;
        

        String resourceTypeClause;
        List subFolders;
        List subFiles;

        String orderClause = m_sqlManager.readQuery(currentProject, "C_RESOURCES_GET_SUBRESOURCES_ORDER");
        
        if (getFolders && getFiles) {
            resourceTypeClause = "";
            subFolders = new ArrayList();
            subFiles = new ArrayList();
        } else if (getFolders) {
            resourceTypeClause = " " + m_sqlManager.readQuery(currentProject, "C_RESOURCES_GET_SUBRESOURCES_GET_FOLDERS");
            subFolders = new ArrayList();
            subFiles = null;
        } else {
            resourceTypeClause = " " + m_sqlManager.readQuery(currentProject, "C_RESOURCES_GET_SUBRESOURCES_GET_FILES");
            subFolders = null;
            subFiles = new ArrayList();
        }
        
        try {
            conn = m_sqlManager.getConnection(currentProject);
             
            query = m_sqlManager.readQuery(currentProject, "C_RESOURCES_GET_SUBRESOURCES") + " " + resourceTypeClause + " " + orderClause;
            stmt = m_sqlManager.getPreparedStatementForSql(conn, query);
            stmt.setString(1, resource.getRootPath()+"_%");
            stmt.setString(2, resource.getRootPath()+"_%/_%");
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
            m_sqlManager.closeAll(null, conn, stmt, res);
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
     * @see org.opencms.db.I_CmsVfsDriver#readFile(I_CmsRuntimeInfo, int, boolean, org.opencms.util.CmsUUID)
     */
    public CmsFile readFile(I_CmsRuntimeInfo runtimeInfo, int projectId, boolean includeDeleted, CmsUUID structureId) throws CmsException {
        CmsFile file = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        
        try {
            conn = m_sqlManager.getConnection(runtimeInfo);

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
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, res);
        }
        return file;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFileHeader(I_CmsRuntimeInfo, int, org.opencms.util.CmsUUID, boolean)
     */
    public CmsFile readFileHeader(I_CmsRuntimeInfo runtimeInfo, int projectId, CmsUUID structureId, boolean includeDeleted) throws CmsException {

        CmsFile file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        
        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READBYID");
            stmt.setString(1, structureId.toString());
            
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
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + ".readFileHeader/2] " + structureId);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, res);
        }
        return file;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFileHeader(I_CmsRuntimeInfo, int, java.lang.String, boolean)
     */
    public CmsFile readFileHeader(I_CmsRuntimeInfo runtimeInfo, int projectId, String path, boolean includeDeleted) throws CmsException {

        CmsFile file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        
        // must remove trailing slash
        path = removeTrailingSeparator(path);
        
        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ");
            stmt.setString(1, path);

            res = stmt.executeQuery();
            if (res.next()) {
                file = createFile(res, projectId, false);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }

                // check if this resource is marked as deleted
                if ((file.getState() == I_CmsConstants.C_STATE_DELETED) && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + ".readFileHeader/3] " + file.getName(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + ".readFileHeader/3] " + path);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, res);
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
            m_sqlManager.closeAll(null, conn, stmt, res);
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
            m_sqlManager.closeAll(null, conn, stmt, res);
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
            m_sqlManager.closeAll(null, conn, stmt, res);
        }

        return folder;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolder(I_CmsRuntimeInfo, int, java.lang.String)
     */
    public CmsFolder readFolder(I_CmsRuntimeInfo runtimeInfo, int projectId, String folderPath) throws CmsException {

        CmsFolder folder = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        folderPath = removeTrailingSeparator(folderPath);
        
        try {
            conn = m_sqlManager.getConnection(runtimeInfo, projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ");

            stmt.setString(1, folderPath);

            res = stmt.executeQuery();

            if (res.next()) {
                folder = createFolder(res, projectId, true);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }

            } else {
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + ".readFolder/2] " + folderPath);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException exc) {
            throw exc;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, res);
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
            m_sqlManager.closeAll(null, conn, stmt, res);
        }

        return folders;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinition(I_CmsRuntimeInfo, java.lang.String, int, int)
     */
    public CmsPropertydefinition readPropertyDefinition(I_CmsRuntimeInfo runtimeInfo, String name, int projectId, int mappingtype) throws CmsException {
        CmsPropertydefinition propDef = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_READ");
            stmt.setString(1, name);
            stmt.setInt(2, mappingtype);
            res = stmt.executeQuery();

            // if resultset exists - return it
            if (res.next()) {
                propDef = new CmsPropertydefinition(new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_ID"))), res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")), res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_PROPERTYDEF_MAPPING_TYPE")));
            } else {
                res.close();
                res = null;
                // not found!
                throw new CmsException("[" + this.getClass().getName() + ".readPropertydefinition] " + name, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, res);
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
                propertyDefinitions.add(new CmsPropertydefinition(new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_ID"))), res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")), res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_PROPERTYDEF_MAPPING_TYPE"))));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return propertyDefinitions;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResourceTree(int, java.lang.String, int, int, long, long, int)
     */
    public List readResourceTree (int projectId, String parentPath, int type, int state, long startTime, long endTime, int mode) throws CmsException {

        List result = new ArrayList();
        
        String query, order;
        StringBuffer conditions = new StringBuffer();
        List params = new ArrayList(5);
        
        // prepare the selection criteria
        prepareProjectCondition(projectId, mode, conditions, params);
        preparePathCondition(projectId, parentPath, mode, conditions, params);
        prepareTypeCondition(projectId, type, mode, conditions, params);
        prepareStateCondition(projectId, state, mode, conditions, params);
        prepareTimeRangeCondition(projectId, startTime, endTime, conditions, params);

        // now read matching resources within the subtree 
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            query = m_sqlManager.readQuery(projectId, "C_RESOURCES_READ_TREE");
            order = m_sqlManager.readQuery(projectId, "C_RESOURCES_ORDER_BY_PATH");
            stmt = m_sqlManager.getPreparedStatementForSql(conn, query + conditions + order);
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i+1, (String)params.get(i));
            }
            
            res = stmt.executeQuery();
            while (res.next()) {
                CmsResource resource = createResource(res, projectId);
                result.add(resource);
            }
            
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception exc) {
            throw new CmsException("readResources " + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        
        return result;
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
        if ((mode & I_CmsConstants.C_READMODE_INCLUDE_PROJECT) > 0) {    
            // C_READMODE_INCLUDE_PROJECT: add condition to match the PROJECT_ID
            conditions.append(C_BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_PROJECT_LASTMODIFIED"));
            conditions.append(C_END_CONDITION);
            params.add(String.valueOf(projectId));
        }
    }
    
    /**
     * Appends the appropriate selection criteria related with the parentPath.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param parentPath the parent path
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection params
     */
    private void preparePathCondition(int projectId, String parentPath, int mode, StringBuffer conditions, List params) {
        if (parentPath != I_CmsConstants.C_READ_IGNORE_PARENT) {
            // C_READ_IGNORE_PARENT: if NOT set, add condition to match the parent path as prefix of RESOURCE_PATH
            conditions.append(C_BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_PATH_PREFIX"));
            conditions.append(C_END_CONDITION);
            params.add(addTrailingSeparator(parentPath) + "%");
        }
        
        if ((mode & I_CmsConstants.C_READMODE_EXCLUDE_TREE) > 0) {
            // C_READ_CHILDS: add condition to read immediate childs only
            conditions.append(C_BEGIN_EXCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_PATH_PREFIX"));
            conditions.append(C_END_CONDITION);
            params.add(addTrailingSeparator(parentPath) + "%/_%");
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
        if (type != I_CmsConstants.C_READ_IGNORE_TYPE) {
            if ((mode & I_CmsConstants.C_READMODE_EXCLUDE_TYPE) > 0) {
                // C_READ_FILE_TYPES: add condition to match against any type, but not given type
                conditions.append(C_BEGIN_EXCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_TYPE"));
                conditions.append(C_END_CONDITION);
                params.add(String.valueOf(type));
            } else {
                //otherwise add condition to match against given type if neccessary
                conditions.append(C_BEGIN_INCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_TYPE"));
                conditions.append(C_END_CONDITION);
                params.add(String.valueOf(type));
            }
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
        if (state != I_CmsConstants.C_READ_IGNORE_STATE) {
            if ((mode & I_CmsConstants.C_READMODE_EXCLUDE_STATE) > 0) {
                // C_READ_MODIFIED_STATES: add condition to match against any state but not given state
                conditions.append(C_BEGIN_EXCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_STATE"));
                conditions.append(C_END_CONDITION);
                params.add(String.valueOf(state));
                params.add(String.valueOf(state));
            } else {
                // otherwise add condition to match against given state if neccessary
                conditions.append(C_BEGIN_INCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_RESOURCE_STATE"));
                conditions.append(C_END_CONDITION);
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
    private void prepareTimeRangeCondition(int projectId, long startTime, long endTime, StringBuffer conditions, List params) {
        if (startTime > 0L) {
            // C_READ_IGNORE_TIME: if NOT set, add condition to match lastmodified date against startTime
            conditions.append(C_BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_DATE_LASTMODIFIED_AFTER"));
            conditions.append(C_END_CONDITION);
            params.add(String.valueOf(startTime));
        }   
        
        if (endTime > 0L) {
            // C_READ_IGNORE_TIME: if NOT set, add condition to match lastmodified date against endTime
            conditions.append(C_BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_DATE_LASTMODIFIED_BEFORE"));
            conditions.append(C_END_CONDITION);
            params.add(String.valueOf(endTime));
        }        
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
            m_sqlManager.closeAll(null, conn, stmt, res);
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
            m_sqlManager.closeAll(null, conn, stmt, res);
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
            m_sqlManager.closeAll(null, conn, stmt, res);
        }

        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readSiblings(I_CmsRuntimeInfo, org.opencms.file.CmsProject, org.opencms.file.CmsResource, boolean)
     */
    public List readSiblings(I_CmsRuntimeInfo runtimeInfo, CmsProject currentProject, CmsResource resource, boolean includeDeleted) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        CmsResource currentResource = null;
        List vfsLinks = new ArrayList();

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            
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
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, res);
        }

        return vfsLinks;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#removeFile(I_CmsRuntimeInfo, org.opencms.file.CmsProject, org.opencms.file.CmsResource, boolean)
     */
    public void removeFile(I_CmsRuntimeInfo runtimeInfo, CmsProject currentProject, CmsResource resource, boolean removeFileContent) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        int linkCount = 0;

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);

            // delete the structure recourd
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_STRUCTURE_DELETE_BY_STRUCTUREID");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.executeUpdate();

            m_sqlManager.closeAll(null, null, stmt, null);

            // count the references to the resource
            linkCount = internalCountSiblings(runtimeInfo, currentProject.getId(), resource.getResourceId());

            if (linkCount > 0) {

                // update the link Count
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCES_UPDATE_SIBLING_COUNT");
                stmt.setInt(1, this.internalCountSiblings(runtimeInfo, currentProject.getId(), resource.getResourceId()));
                stmt.setString(2, resource.getResourceId().toString());
                stmt.executeUpdate();

                m_sqlManager.closeAll(null, null, stmt, null);

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

                m_sqlManager.closeAll(null, null, stmt, null);

                // delete content records with this resource id
                stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_FILE_CONTENT_DELETE");
                stmt.setString(1, resource.getResourceId().toString());
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#removeFolder(I_CmsRuntimeInfo, org.opencms.file.CmsProject, CmsResource)
     */
    public void removeFolder(I_CmsRuntimeInfo runtimeInfo, CmsProject currentProject, CmsResource resource) throws CmsException {
        
        // the current implementation only deletes empty folders
        
        // check if the folder has any files in it
        List files = readChildResources(runtimeInfo, currentProject, resource, false, true);
        
        // remove deleted files from the child resources
        //files = internalFilterUndeletedResources(files);
        
        if (files.size() == 0) {
            
            // check if the folder has any folders in it
            List folders = readChildResources(runtimeInfo, currentProject, resource, true, false);
            
            // remove deleted folders from the child resources
            //folders = internalFilterUndeletedResources(folders);
            
            if (folders.size() == 0) {
                internalRemoveFolder(runtimeInfo, currentProject, resource);
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
     * @see org.opencms.db.I_CmsVfsDriver#replaceResource(org.opencms.file.CmsUser, org.opencms.file.CmsProject, org.opencms.file.CmsResource, byte[], int)
     */
    public void replaceResource(CmsUser currentUser, CmsProject currentProject, CmsResource res, byte[] resContent, int newResType) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // write the file content
            if (resContent != null) {
                writeContent(null, currentProject, res.getResourceId(), resContent);
            }

            // update the resource record
            conn = m_sqlManager.getConnection(currentProject);
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCE_REPLACE");
            stmt.setInt(1, newResType);
            stmt.setInt(2, resContent.length);
            stmt.setString(3, res.getResourceId().toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
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
            m_sqlManager.closeAll(null, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#validateResourceIdExists(I_CmsRuntimeInfo, int, org.opencms.util.CmsUUID)
     */
    public boolean validateResourceIdExists(I_CmsRuntimeInfo runtimeInfo, int projectId, CmsUUID resourceId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean exists = false;

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);           
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_RESOURCE_STATE");
            stmt.setString(1, resourceId.toString());
            
            res = stmt.executeQuery();
            exists = res.next();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, res);
        }

        return exists;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#validateStructureIdExists(I_CmsRuntimeInfo, int, org.opencms.util.CmsUUID)
     */
    public boolean validateStructureIdExists(I_CmsRuntimeInfo runtimeInfo, int projectId, CmsUUID structureId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean result = false;
        int count = 0;

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
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
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeContent(I_CmsRuntimeInfo, CmsProject, org.opencms.util.CmsUUID, byte[])
     */
    public void writeContent(I_CmsRuntimeInfo runtimeInfo, CmsProject project, CmsUUID resourceId, byte[] content) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResource(I_CmsRuntimeInfo, org.opencms.file.CmsProject, CmsResource, int)
     */
    public void writeResource(I_CmsRuntimeInfo runtimeInfo, CmsProject project, CmsResource resource, int changed) throws CmsException {

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

        if (changed == CmsDriverManager.C_UPDATE_RESOURCE_STATE) {
            resourceState = org.opencms.main.I_CmsConstants.C_STATE_CHANGED;
        } else if (changed == CmsDriverManager.C_UPDATE_STRUCTURE_STATE) {
            structureState = org.opencms.main.I_CmsConstants.C_STATE_CHANGED;
        } else if (changed == CmsDriverManager.C_NOTHING_CHANGED) {
            projectId = resource.getProjectLastModified();
        } else {
            resourceState = org.opencms.main.I_CmsConstants.C_STATE_CHANGED;
            structureState = org.opencms.main.I_CmsConstants.C_STATE_CHANGED;
        }
        
        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCES");
            stmt.setInt(1, resource.getTypeId());
            stmt.setInt(2, resource.getFlags());
            stmt.setLong(3, resourceDateModified);
            stmt.setString(4, resource.getUserLastModified().toString());
            stmt.setInt(5, resourceState);
            stmt.setInt(6, resource.getLength());
            stmt.setInt(7, projectId);
            stmt.setInt(8, internalCountSiblings(null, project.getId(), resource.getResourceId()));
            stmt.setString(9, resource.getResourceId().toString());
            stmt.executeUpdate();
            
            m_sqlManager.closeAll(null, null, stmt, null);

            // update the structure
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_STRUCTURE");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.setString(2, resourcePath);
            stmt.setInt(3, structureState);
            stmt.setLong(4, resource.getDateReleased());
            stmt.setLong(5, resource.getDateExpired());
            stmt.setString(6, resource.getStructureId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);          
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeLastModifiedProjectId(I_CmsRuntimeInfo, org.opencms.file.CmsProject, int, org.opencms.file.CmsResource)
     */
    public void writeLastModifiedProjectId(I_CmsRuntimeInfo runtimeInfo, CmsProject project, int projectId, CmsResource resource) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_PROJECT_LASTMODIFIED");
            stmt.setInt(1, projectId);
            stmt.setString(2, resource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#publishResource(I_CmsRuntimeInfo, org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.file.CmsResource, boolean)
     */
    public void publishResource(I_CmsRuntimeInfo runtimeInfo, CmsProject onlineProject, CmsResource onlineResource, CmsResource offlineResource, boolean writeFileContent) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        int resourceSize = offlineResource.getLength();

        String resourcePath = removeTrailingSeparator(offlineResource.getRootPath());
        
        try {
            conn = m_sqlManager.getConnection(runtimeInfo);

            if (validateResourceIdExists(runtimeInfo, onlineProject.getId(), offlineResource.getResourceId())) {

                // the resource record exists online already

                if (writeFileContent && offlineResource.isFile()) {
                    // update the online file content
                    writeContent(runtimeInfo, onlineProject, offlineResource.getResourceId(), ((CmsFile)offlineResource).getContents());
                }

                // update the online resource record
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_UPDATE_RESOURCES");
                stmt.setInt(1, offlineResource.getTypeId());
                stmt.setInt(2, offlineResource.getFlags());
                stmt.setLong(3, offlineResource.getDateLastModified());
                stmt.setString(4, offlineResource.getUserLastModified().toString());
                stmt.setInt(5, I_CmsConstants.C_STATE_UNCHANGED);
                stmt.setInt(6, resourceSize);
                stmt.setInt(7, offlineResource.getProjectLastModified());
                stmt.setInt(8, this.internalCountSiblings(runtimeInfo, onlineProject.getId(), onlineResource.getResourceId()));
                stmt.setString(9, offlineResource.getResourceId().toString());
                stmt.executeUpdate();

                m_sqlManager.closeAll(null, null, stmt, null);

                // update the online structure record
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_RESOURCES_UPDATE_STRUCTURE");
                stmt.setString(1, offlineResource.getResourceId().toString());
                stmt.setString(2, resourcePath);
                stmt.setInt(3, I_CmsConstants.C_STATE_UNCHANGED);
                stmt.setLong(4, offlineResource.getDateReleased());
                stmt.setLong(5, offlineResource.getDateExpired());
                stmt.setString(6, offlineResource.getStructureId().toString());
                stmt.executeUpdate();
                
            } else {

                // the resource record does NOT exist online yet
                if (writeFileContent && offlineResource.isFile()) {
                    // && !validateContentIdExists(onlineProject.getId(), offlineResource.getContentId())
                    // create the file content online
                    resourceSize = offlineResource.getLength();
                    createContent(runtimeInfo, onlineProject, offlineResource.getResourceId(), ((CmsFile)offlineResource).getContents(), 0);
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
                stmt.setInt(8, I_CmsConstants.C_STATE_UNCHANGED);
                stmt.setInt(9, resourceSize);
                stmt.setInt(10, offlineResource.getProjectLastModified());
                stmt.setInt(11, 1);
                stmt.executeUpdate();

                m_sqlManager.closeAll(null, null, stmt, null);

                // create the structure record online
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_STRUCTURE_WRITE");
                stmt.setString(1, offlineResource.getStructureId().toString());
                stmt.setString(2, offlineResource.getResourceId().toString());
                stmt.setString(3, resourcePath);
                stmt.setInt(4, I_CmsConstants.C_STATE_UNCHANGED);
                stmt.setLong(5, offlineResource.getDateReleased());
                stmt.setLong(6, offlineResource.getDateExpired());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResourceState(I_CmsRuntimeInfo, org.opencms.file.CmsProject, org.opencms.file.CmsResource, int)
     */
    public void writeResourceState(I_CmsRuntimeInfo runtimeInfo, CmsProject project, CmsResource resource, int changed) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            return;
        }

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            
            // Refactor I_CmsRuntimeInfo in signature to Object
            // rename 'runtimeInfo' to 'param'

            if (changed == CmsDriverManager.C_UPDATE_RESOURCE) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCE_STATELASTMODIFIED");
                stmt.setInt(1, resource.getState());
                stmt.setLong(2, resource.getDateLastModified());
                stmt.setString(3, resource.getUserLastModified().toString());
                stmt.setInt(4, project.getId());
                stmt.setString(5, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(null, null, stmt, null);
                
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RELEASE_EXPIRED");
                stmt.setLong(1, resource.getDateReleased());
                stmt.setLong(2, resource.getDateExpired());
                stmt.setString(3, resource.getStructureId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(null, null, stmt, null);
            }

            if (changed == CmsDriverManager.C_UPDATE_RESOURCE_STATE || changed == CmsDriverManager.C_UPDATE_ALL) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCE_STATE");
                stmt.setInt(1, resource.getState());
                stmt.setInt(2, project.getId());
                stmt.setString(3, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(null, null, stmt, null);
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
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#getSqlManager()
     */    
    public CmsSqlManager getSqlManager() {
        return m_sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyObject(I_CmsRuntimeInfo, java.lang.String, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public CmsProperty readPropertyObject(I_CmsRuntimeInfo runtimeInfo, String key, CmsProject project, CmsResource resource) throws CmsException {
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
            conn = m_sqlManager.getConnection(runtimeInfo);
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
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, res);
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
            m_sqlManager.closeAll(null, conn, stmt, res);
        }

        return new ArrayList(propertyMap.values());
    }
    
    /**
     * @see org.opencms.db.I_CmsVfsDriver#writePropertyObjects(I_CmsRuntimeInfo, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.List)
     */
    public void writePropertyObjects(I_CmsRuntimeInfo runtimeInfo, CmsProject project, CmsResource resource, List properties) throws CmsException {

        CmsProperty property = null;

        for (int i = 0; i < properties.size(); i++) {
            property = (CmsProperty)properties.get(i);
            writePropertyObject(runtimeInfo, project, resource, property);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writePropertyObject(I_CmsRuntimeInfo, org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.file.CmsProperty)
     */
    public void writePropertyObject(I_CmsRuntimeInfo runtimeInfo, CmsProject project, CmsResource resource, CmsProperty property) throws CmsException {
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
            propertyDefinition = readPropertyDefinition(runtimeInfo, property.getKey(), project.getId(), I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);
        } catch (CmsException e) {
            propertyDefinition = null;
        }

        if (propertyDefinition == null) {
            if (property.autoCreatePropertyDefinition()) {
                propertyDefinition = createPropertyDefinition(runtimeInfo, project.getId(), property.getKey(), I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);   
                
                try {
                    readPropertyDefinition(runtimeInfo, property.getKey(), I_CmsConstants.C_PROJECT_ONLINE_ID, I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);
                } catch (CmsException e) {
                    createPropertyDefinition(runtimeInfo, I_CmsConstants.C_PROJECT_ONLINE_ID, property.getKey(), I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);
                } 
                
                try {
                    m_driverManager.getBackupDriver().readBackupPropertyDefinition(runtimeInfo, property.getKey(), I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);
                } catch (CmsException e) {
                    m_driverManager.getBackupDriver().createBackupPropertyDefinition(runtimeInfo, property.getKey(), I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);
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
            existingProperty = readPropertyObject(runtimeInfo, propertyDefinition.getName(), project, resource);
            
            if (existingProperty.isIdentical(property)) {
                
                // property already has the identical values set, no write required
                return;
            }
            
            conn = m_sqlManager.getConnection(runtimeInfo);

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
                m_sqlManager.closeAll(null, null, stmt, null);                
            }         
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
    }    
    
    /**
     * @see org.opencms.db.I_CmsVfsDriver#createResource(I_CmsRuntimeInfo, org.opencms.file.CmsProject, org.opencms.file.CmsResource, byte[])
     */
    public CmsResource createResource(I_CmsRuntimeInfo runtimeInfo, CmsProject project, CmsResource resource, byte[] content) throws CmsException {

        CmsUUID newStructureId = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        // check the resource path
        String resourcePath = removeTrailingSeparator(resource.getRootPath());
        if (resourcePath.length() > I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME) {
            throw new CmsException("The resource path '" + resourcePath + "' is too long! (max. allowed length must be <= " + I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME + " chars.!)", CmsException.C_BAD_NAME);
        }
        
        // check if the parent folder of the resource exists and is not deleted
        if (!resource.getRootPath().equals(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            String parentFolderName = CmsResource.getParentFolder(resource.getRootPath());
            CmsFolder parentFolder = readFolder(runtimeInfo, project.getId(), parentFolderName);
            if (parentFolder.getState() == I_CmsConstants.C_STATE_DELETED) {
                throw new CmsException("Parent folder of resource "
                    + resource.getRootPath()
                    + " does not exist or is deleted!", CmsException.C_NOT_FOUND);
            }
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
                runtimeInfo, 
                project.getId(), 
                resourcePath, true);
            
            if (res.getState() == I_CmsConstants.C_STATE_DELETED) {
                // if an existing resource is deleted, it will be finally removed now
                // but we have to reuse its id in order to avoid orphanes in the online project
                newStructureId = res.getStructureId();
                newState = I_CmsConstants.C_STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = readSiblings(runtimeInfo, project, res, false);
                int propertyDeleteOption = (res.getSiblingCount() > 1) ? CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_VALUES : CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                deletePropertyObjects(runtimeInfo, project.getId(), res, propertyDeleteOption);
                removeFile(runtimeInfo, project, res, true);

                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
                    Collections.singletonMap("resources", modifiedResources)));
                OpenCms.fireCmsEvent(new CmsEvent(
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
            conn = m_sqlManager.getConnection(runtimeInfo);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_STRUCTURE_WRITE");
            stmt.setString(1, newStructureId.toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setString(3, resourcePath);
            stmt.setInt(4, newState);
            stmt.setLong(5, resource.getDateReleased());
            stmt.setLong(6, resource.getDateExpired());
            stmt.executeUpdate();
            
            m_sqlManager.closeAll(null, null, stmt, null);

            if (!validateResourceIdExists(runtimeInfo, project.getId(), resource.getResourceId())) {

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
                    createContent(runtimeInfo, project, resource.getResourceId(), content, 0);                    
                }
                
            } else {

                if ((content != null) || (resource.getState() != I_CmsConstants.C_STATE_KEEP)) {
                    // update the resource record only if state has changed or new content is provided
                    stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCES");
                    stmt.setInt(1, resource.getTypeId());
                    stmt.setInt(2, resource.getFlags());
                    stmt.setLong(3, dateModified);
                    stmt.setString(4, resource.getUserLastModified().toString());
                    stmt.setInt(5, I_CmsConstants.C_STATE_CHANGED);
                    stmt.setInt(6, resource.getLength());
                    stmt.setInt(7, project.getId());
                    stmt.setInt(8, internalCountSiblings(runtimeInfo, project.getId(), resource.getResourceId()));
                    stmt.setString(9, resource.getResourceId().toString());
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(null, null, stmt, null);
                }
                
                if (resource.isFile()) {
                    if (content != null) {
                        
                        // update the file content
                        writeContent(runtimeInfo, project, resource.getResourceId(), content);
                        
                    } else if (resource.getState() == I_CmsConstants.C_STATE_KEEP) {

                        // special case sibling creation - update the link Count
                        stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_SIBLING_COUNT");
                        stmt.setInt(1, this.internalCountSiblings(runtimeInfo, project.getId(), resource.getResourceId()));
                        stmt.setString(2, resource.getResourceId().toString());
                        stmt.executeUpdate();
                        m_sqlManager.closeAll(null, null, stmt, null);

                        // update the resource flags
                        stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_FLAGS");
                        stmt.setInt(1, resource.getFlags());
                        stmt.setString(2, resource.getResourceId().toString());
                        stmt.executeUpdate();                        
                        m_sqlManager.closeAll(null, null, stmt, null);                                                
                    }
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }

        return readFileHeader(runtimeInfo, project.getId(), newStructureId, false);
    }    

    /**
     * Removes a trailing separater from a path if required.<p>
     * 
     * @param path the path to remove the trailing separator from
     * @return the path without a trailing separator
     */
    private static String removeTrailingSeparator(String path) {
        int l = path.length();
        if ((l <= 1) || (path.charAt(l-1) != '/')) {
            return path;
        } else {
            return path.substring(0, l-1);
        }
    }
    
    /**
     * Adds a trailing separator to a path if required.<p>
     * 
     * @param path the path to add the trailing separator to
     * @return the path with a trailing separator
     */
    private static String addTrailingSeparator(String path) {
        int l = path.length();
        if ((l == 0) || (path.charAt(l-1) != '/')) {
            return path.concat("/");
        } else {
            return path;
        }
    }    
}

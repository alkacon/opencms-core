/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsVfsDriver.java,v $
 * Date   : $Date: 2004/11/22 18:03:06 $
 * Version: $Revision: 1.220 $
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

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertydefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
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
 * @version $Revision: 1.220 $ $Date: 2004/11/22 18:03:06 $
 * @since 5.1
 */
public class CmsVfsDriver extends Object implements I_CmsDriver, I_CmsVfsDriver {
    
    /** Operator to concatenate exclude conditions. */
    static String C_BEGIN_EXCLUDE_CONDITION = " AND NOT (";

    /** Operator to concatenate include conditions. */
    static String C_BEGIN_INCLUDE_CONDITION = " AND (";
    
    /** String to end a single condition. */
    static String C_END_CONDITION = ") ";
    
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
        if ((l == 0) || (path.charAt(l-1) != '/')) {
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
        if ((l <= 1) || (path.charAt(l-1) != '/')) {
            return path;
        } else {
            return path.substring(0, l-1);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createContent(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, byte[], int)
     */
    public void createContent(CmsDbContext dbc, CmsProject project, CmsUUID resourceId, byte[] content, int versionId) throws CmsException {
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
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

        return new CmsFolder(structureId, resourceId, resourcePath, resourceType, resourceFlags, resProjectId, newState, dateCreated, userCreated, dateLastModified, userLastModified, siblingCount, dateReleased, dateExpired);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createPropertyDefinition(org.opencms.db.CmsDbContext, int, java.lang.String)
     */
    public CmsPropertydefinition createPropertyDefinition(CmsDbContext dbc, int projectId, String name) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        // TODO switch the property def. PK into a CmsUUID PK

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_CREATE");
            stmt.setString(1, new CmsUUID().toString());
            stmt.setString(2, name);
            stmt.executeUpdate();         
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        return readPropertyDefinition(dbc, name, projectId);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, byte[])
     */
    public CmsResource createResource(CmsDbContext dbc, CmsProject project, CmsResource resource, byte[] content) throws CmsException {

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
            CmsFolder parentFolder = readFolder(dbc, project.getId(), parentFolderName);
            if (parentFolder.getState() == I_CmsConstants.C_STATE_DELETED) {
                throw new CmsException("Parent folder of resource "
                    + resource.getRootPath()
                    + " does not exist or is deleted!", CmsException.C_NOT_FOUND);
            }
        }        

        // validate the resource length
        internalValidateResourceLength(resource);
        
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
            res = readResource(
                dbc, 
                project.getId(), 
                resourcePath, true);
            
            if (res.getState() == I_CmsConstants.C_STATE_DELETED) {
                // if an existing resource is deleted, it will be finally removed now
                // but we have to reuse its id in order to avoid orphanes in the online project
                newStructureId = res.getStructureId();
                newState = I_CmsConstants.C_STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = readSiblings(dbc, project, res, false);
                int propertyDeleteOption = (res.getSiblingCount() > 1) ? CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_VALUES : CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                deletePropertyObjects(dbc, project.getId(), res, propertyDeleteOption);
                removeFile(dbc, project, res, true);

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
                    stmt.setInt(8, internalCountSiblings(dbc, project.getId(), resource.getResourceId()));
                    stmt.setString(9, resource.getResourceId().toString());
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }
                
                if (resource.isFile()) {
                    if (content != null) {
                        
                        // update the file content
                        writeContent(dbc, project, resource.getResourceId(), content);
                        
                    } else if (resource.getState() == I_CmsConstants.C_STATE_KEEP) {

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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
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

        CmsResource newResource = 
            new CmsResource(
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
     * @see org.opencms.db.I_CmsVfsDriver#createSibling(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.lang.String)
     */
    public void createSibling(CmsDbContext dbc, CmsProject project, CmsResource resource, String resourcename) throws CmsException {
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
            res = readResource(dbc, project.getId(), resource.getRootPath(), true);

            if (res.getState() == I_CmsConstants.C_STATE_DELETED) {
                // if an existing resource is deleted, it will be finally removed now
                // but we have to reuse its id in order to avoid orphanes in the online project
                newStructureId = res.getStructureId();
                newState = I_CmsConstants.C_STATE_CHANGED;

                // remove the existing file and it's properties
                List modifiedResources = readSiblings(dbc, project, res, false);
                int propertyDeleteOption = (res.getSiblingCount() > 1) ? CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_VALUES : CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                deletePropertyObjects(dbc, project.getId(), res, propertyDeleteOption);
                removeFile(dbc, project, res, true);

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
        if (!validateResourceIdExists(dbc, project.getId(), resource.getResourceId())) {
            throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + "] ");
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deletePropertyDefinition(org.opencms.db.CmsDbContext, org.opencms.file.CmsPropertydefinition)
     */
    public void deletePropertyDefinition(CmsDbContext dbc, CmsPropertydefinition metadef) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (internalCountProperties(dbc, metadef, I_CmsConstants.C_PROJECT_ONLINE_ID) != 0
                || internalCountProperties(dbc, metadef, Integer.MAX_VALUE) != 0) {

                throw new CmsException(
                    "["
                        + this.getClass().getName()
                        + "] "
                        + metadef.getName()
                        + "could not be deleted because property is attached to resources",
                    CmsException.C_UNKNOWN_EXCEPTION);
            }
            
            conn = m_sqlManager.getConnection(dbc);
            
            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    // delete the offline propertydef
                    stmt = m_sqlManager.getPreparedStatement(conn, Integer.MAX_VALUE, "C_PROPERTYDEF_DELETE");
                } else if (i == 1) {
                    // delete the online propertydef
                    stmt = m_sqlManager.getPreparedStatement(conn, I_CmsConstants.C_PROJECT_ONLINE_ID, "C_PROPERTYDEF_DELETE");
                }
                
                stmt.setString(1, metadef.getId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deletePropertyObjects(org.opencms.db.CmsDbContext, int, org.opencms.file.CmsResource, int)
     */
    public void deletePropertyObjects(CmsDbContext dbc, int projectId, CmsResource resource, int deleteOption) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String resourceName = resource.getRootPath();

        // add folder separator to folder name if it is not present
        if (resource.isFolder() && !resourceName.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            resourceName += I_CmsConstants.C_FOLDER_SEPARATOR;
        }

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            
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
            m_sqlManager.closeAll(dbc, conn, stmt, null);
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
     * @see org.opencms.db.I_CmsVfsDriver#getSqlManager()
     */    
    public CmsSqlManager getSqlManager() {
        return m_sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(org.opencms.db.CmsDbContext, org.opencms.configuration.CmsConfigurationManager, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(CmsDbContext dbc, CmsConfigurationManager configurationManager, List successiveDrivers, CmsDriverManager driverManager) {
        
        ExtendedProperties configuration = configurationManager.getConfiguration();
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
     * @see org.opencms.db.I_CmsVfsDriver#publishResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.file.CmsResource, boolean)
     */
    public void publishResource(CmsDbContext dbc, CmsProject onlineProject, CmsResource onlineResource, CmsResource offlineResource, boolean writeFileContent) throws CmsException {
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
                    writeContent(dbc, onlineProject, offlineResource.getResourceId(), ((CmsFile)offlineResource).getContents());
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
                stmt.setInt(3, I_CmsConstants.C_STATE_UNCHANGED);
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
                    createContent(dbc, onlineProject, offlineResource.getResourceId(), ((CmsFile)offlineResource).getContents(), 0);
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

                m_sqlManager.closeAll(dbc, null, stmt, null);
                
                // read the parent id
                String parentId = internalReadParentId(dbc, onlineProject.getId(), resourcePath);

                // create the structure record online
                stmt = m_sqlManager.getPreparedStatement(conn, onlineProject, "C_STRUCTURE_WRITE");
                stmt.setString(1, offlineResource.getStructureId().toString());
                stmt.setString(2, offlineResource.getResourceId().toString());
                stmt.setString(3, resourcePath);
                stmt.setInt(4, I_CmsConstants.C_STATE_UNCHANGED);
                stmt.setLong(5, offlineResource.getDateReleased());
                stmt.setLong(6, offlineResource.getDateExpired());
                stmt.setString(7, parentId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
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
        boolean getFiles) throws CmsException {
        
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
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());
             
            query = m_sqlManager.readQuery(currentProject, "C_RESOURCES_GET_SUBRESOURCES") + " " + resourceTypeClause + " " + orderClause;
            stmt = m_sqlManager.getPreparedStatementForSql(conn, query);
            stmt.setString(1, resource.getStructureId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                int type = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));                
                if (CmsFolder.isFolderType(type)) {
                    subFolders.add(createFolder(res, currentProject.getId(), false));
                } else {
                    subFiles.add(createFile(res, currentProject.getId(), false));
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
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
     * @see org.opencms.db.I_CmsVfsDriver#readFile(org.opencms.db.CmsDbContext, int, boolean, org.opencms.util.CmsUUID)
     */
    public CmsFile readFile(CmsDbContext dbc, int projectId, boolean includeDeleted, CmsUUID structureId) throws CmsException {
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return file;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFiles(org.opencms.db.CmsDbContext, int)
     */
    public List readFiles(CmsDbContext dbc, int projectId) throws CmsException {
        List resources = new ArrayList();
        CmsResource currentResource;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFiles(org.opencms.db.CmsDbContext, int, int)
     */
    public List readFiles(CmsDbContext dbc, int projectId, int resourcetype) throws CmsException {
        List files = new ArrayList();
        CmsFile file;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        
        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        
        return files;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolder(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID)
     */
    public CmsFolder readFolder(CmsDbContext dbc, int projectId, CmsUUID folderId) throws CmsException {
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
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + ".readFolder/1] " + folderId);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException exc) {
            throw exc;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return folder;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolder(org.opencms.db.CmsDbContext, int, java.lang.String)
     */
    public CmsFolder readFolder(CmsDbContext dbc, int projectId, String folderPath) throws CmsException {

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
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + ".readFolder/2] " + folderPath);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException exc) {
            throw exc;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return folder;

    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolders(org.opencms.db.CmsDbContext, int)
     */
    public List readFolders(CmsDbContext dbc, int projectId) throws CmsException {
        List folders = new ArrayList();
        CmsFolder currentFolder;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return folders;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinition(org.opencms.db.CmsDbContext, java.lang.String, int)
     */
    public CmsPropertydefinition readPropertyDefinition(CmsDbContext dbc, String name, int projectId) throws CmsException {
        CmsPropertydefinition propDef = null;
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
                propDef = new CmsPropertydefinition(new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_ID"))), res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")));
            } else {
                res.close();
                res = null;
                // not found!
                throw new CmsException("[" + this.getClass().getName() + ".readPropertydefinition] " + name, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return propDef;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinitions(org.opencms.db.CmsDbContext, int, int)
     */
    public List readPropertyDefinitions(CmsDbContext dbc, int projectId, int mappingtype) throws CmsException {
        ArrayList propertyDefinitions = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTYDEF_READALL");
           
            res = stmt.executeQuery();
            while (res.next()) {
                propertyDefinitions.add(new CmsPropertydefinition(new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_ID"))), res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME"))));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return propertyDefinitions;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyObject(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public CmsProperty readPropertyObject(CmsDbContext dbc, String key, CmsProject project, CmsResource resource) throws CmsException {
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
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project.getId(), "C_PROPERTIES_READ");

            stmt.setString(1, key);
            stmt.setString(2, resource.getStructureId().toString());
            stmt.setString(3, resource.getResourceId().toString());
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return (property != null) ? property : CmsProperty.getNullProperty();
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyObjects(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public List readPropertyObjects(CmsDbContext dbc, CmsProject project, CmsResource resource) throws CmsException {
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return new ArrayList(propertyMap.values());
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResource(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID, boolean)
     */
    public CmsResource readResource(CmsDbContext dbc, int projectId, CmsUUID structureId, boolean includeDeleted) throws CmsException {

        CmsResource resource = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        
        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READBYID");
            stmt.setString(1, structureId.toString());
            
            res = stmt.executeQuery();
            // create new file
            if (res.next()) {
                resource = createResource(res, projectId);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
                
                // check if this resource is marked as deleted
                if ((resource.getState() == org.opencms.main.I_CmsConstants.C_STATE_DELETED) && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + ".readResource/2] " + resource.getName(), CmsException.C_RESOURCE_DELETED);
                }
            } else {                               
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + ".readResource/2] " + structureId);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return resource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResource(org.opencms.db.CmsDbContext, int, java.lang.String, boolean)
     */
    public CmsResource readResource(CmsDbContext dbc, int projectId, String path, boolean includeDeleted) throws CmsException {

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

                // check if this resource is marked as deleted
                if ((resource.getState() == I_CmsConstants.C_STATE_DELETED) && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + ".readResource/3] " + resource.getName(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + ".readResource/3] " + path);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException ex) {
            throw ex;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return resource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResources(org.opencms.db.CmsDbContext, int, int, int)
     */
    public List readResources(CmsDbContext dbc, int projectId, int state, int mode) throws CmsException {
        List result = new ArrayList();

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }
    
    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResources(org.opencms.db.CmsDbContext, int, long, long)
     */
    public List readResources(CmsDbContext dbc, int projectId, long starttime, long endtime) throws CmsException {
        List result = new ArrayList();

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }
    
    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResourcesWithProperty(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID)
     */
    public List readResourcesWithProperty(CmsDbContext dbc, int projectId, CmsUUID propertyDef) throws CmsException {
        List resources = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF");
            stmt.setString(1, propertyDef.toString());
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResourceTree(org.opencms.db.CmsDbContext, int, java.lang.String, int, int, long, long, int)
     */
    public List readResourceTree (
        CmsDbContext dbc, 
        int projectId, 
        String parentPath, 
        int type, 
        int state, 
        long startTime, 
        long endTime, 
        int mode) throws CmsException {

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
            String query = m_sqlManager.readQuery(projectId, "C_RESOURCES_READ_TREE");
            String order = m_sqlManager.readQuery(projectId, "C_RESOURCES_ORDER_BY_PATH");
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readSiblings(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, boolean)
     */
    public List readSiblings(CmsDbContext dbc, CmsProject currentProject, CmsResource resource, boolean includeDeleted) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        CmsResource currentResource = null;
        List vfsLinks = new ArrayList();

        try {
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());
            
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return vfsLinks;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#removeFile(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, boolean)
     */
    public void removeFile(CmsDbContext dbc, CmsProject currentProject, CmsResource resource, boolean removeFileContent) throws CmsException {
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#removeFolder(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public void removeFolder(CmsDbContext dbc, CmsProject currentProject, CmsResource resource) throws CmsException {
        
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
     * @see org.opencms.db.I_CmsVfsDriver#replaceResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsUser, org.opencms.file.CmsProject, org.opencms.file.CmsResource, byte[], int)
     */
    public void replaceResource(CmsDbContext dbc, CmsUser currentUser, CmsProject currentProject, CmsResource res, byte[] resContent, int newResType) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // write the file content
            if (resContent != null) {
                writeContent(dbc, currentProject, res.getResourceId(), resContent);
            }

            // update the resource record
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, currentProject, "C_RESOURCE_REPLACE");
            stmt.setInt(1, newResType);
            stmt.setInt(2, resContent.length);
            stmt.setString(3, res.getResourceId().toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#validateContentIdExists(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID)
     */
    public boolean validateContentIdExists(CmsDbContext dbc, int projectId, CmsUUID contentId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean result = false;
        int count = 0;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#validateResourceIdExists(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID)
     */
    public boolean validateResourceIdExists(CmsDbContext dbc, int projectId, CmsUUID resourceId) throws CmsException {
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return exists;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#validateStructureIdExists(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID)
     */
    public boolean validateStructureIdExists(CmsDbContext dbc, int projectId, CmsUUID structureId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean result = false;
        int count = 0;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeContent(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, byte[])
     */
    public void writeContent(CmsDbContext dbc, CmsProject project, CmsUUID resourceId, byte[] content) throws CmsException {
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeLastModifiedProjectId(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, int, org.opencms.file.CmsResource)
     */
    public void writeLastModifiedProjectId(CmsDbContext dbc, CmsProject project, int projectId, CmsResource resource) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_PROJECT_LASTMODIFIED");
            stmt.setInt(1, projectId);
            stmt.setString(2, resource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writePropertyObject(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.file.CmsProperty)
     */
    public void writePropertyObject(CmsDbContext dbc, CmsProject project, CmsResource resource, CmsProperty property) throws CmsException {
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
            propertyDefinition = readPropertyDefinition(dbc, property.getKey(), project.getId());
        } catch (CmsException e) {
            propertyDefinition = null;
        }

        if (propertyDefinition == null) {
            if (property.autoCreatePropertyDefinition()) {
                propertyDefinition = createPropertyDefinition(dbc, project.getId(), property.getKey());   
                
                try {
                    readPropertyDefinition(dbc, property.getKey(), I_CmsConstants.C_PROJECT_ONLINE_ID);
                } catch (CmsException e) {
                    createPropertyDefinition(dbc, I_CmsConstants.C_PROJECT_ONLINE_ID, property.getKey());
                } 
                
                try {
                    m_driverManager.getBackupDriver().readBackupPropertyDefinition(dbc, property.getKey());
                } catch (CmsException e) {
                    m_driverManager.getBackupDriver().createBackupPropertyDefinition(dbc, property.getKey());
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
                m_sqlManager.closeAll(dbc, null, stmt, null);                
            }         
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }    
    
    /**
     * @see org.opencms.db.I_CmsVfsDriver#writePropertyObjects(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.List)
     */
    public void writePropertyObjects(CmsDbContext dbc, CmsProject project, CmsResource resource, List properties) throws CmsException {

        CmsProperty property = null;

        for (int i = 0; i < properties.size(); i++) {
            property = (CmsProperty)properties.get(i);
            writePropertyObject(dbc, project, resource, property);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, int)
     */
    public void writeResource(CmsDbContext dbc, CmsProject project, CmsResource resource, int changed) throws CmsException {
        
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);          
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResourceState(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, int)
     */
    public void writeResourceState(CmsDbContext dbc, CmsProject project, CmsResource resource, int changed) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            return;
        }

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            
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
                m_sqlManager.closeAll(dbc, null, stmt, null);
                
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RELEASE_EXPIRED");
                stmt.setLong(1, resource.getDateReleased());
                stmt.setLong(2, resource.getDateExpired());
                stmt.setString(3, resource.getStructureId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
            }

            if (changed == CmsDriverManager.C_UPDATE_RESOURCE_STATE || changed == CmsDriverManager.C_UPDATE_ALL) {
                stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE_RESOURCE_STATE");
                stmt.setInt(1, resource.getState());
                stmt.setInt(2, project.getId());
                stmt.setString(3, resource.getResourceId().toString());
                stmt.executeUpdate();
                m_sqlManager.closeAll(dbc, null, stmt, null);
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
     * Returns the amount of properties for a propertydefinition.<p>
     * 
     * @param dbc the current database context
     * @param metadef the propertydefinition to test
     * @param projectId the ID of the current project
     * 
     * @return the amount of properties for a propertydefinition
     * @throws CmsException if something goes wrong
     */
    protected int internalCountProperties(CmsDbContext dbc, CmsPropertydefinition metadef, int projectId) throws CmsException {
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        int returnValue;
        try {
            // create statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_READALL_COUNT");
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
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return returnValue;
    }

    /**
     * Counts the number of siblings of a resource.<p>
     * 
     * @param dbc the current database context
     * @param projectId the current project id
     * @param resourceId the resource id to count the number of siblings from
     * 
     * @return number of siblings
     * @throws CmsException if something goes wrong
     */
    protected int internalCountSiblings(CmsDbContext dbc, int projectId, CmsUUID resourceId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        int count = 0;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);

            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_COUNTLINKS");
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                count = res.getInt(1);
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return count;
    }
    
    /**
     * Validates that the length setting of a resource is always correct.<p>
     * 
     * Files need to have a resource length of >= 0, while folders require
     * a resource length of -1.<p>
     * 
     * @param resource the resource to check the length for
     * @throws CmsException if the resource length is not correct
     */
    protected void internalValidateResourceLength(CmsResource resource) throws CmsException {
        
        if (resource.isFolder() && (resource.getLength() == -1)) {            
            return;
        }
        
        if (resource.isFile() && (resource.getLength() >= 0)) {
            return;
        }
        
        throw new CmsException("Invalid resource length " + resource.getLength() + " for resource " + resource.getRootPath());
    }
    
    /**
     * Returns the parent id of the given resource.<p>
     * 
     * @param dbc the current database context
     * @param projectId the current project id 
     * @param resourcename the resource name to read the parent id for
     * @return  the parent id of the given resource
     * @throws CmsException if something goes wrong
     */
    protected String internalReadParentId(CmsDbContext dbc, int projectId, String resourcename) throws CmsException {

        if ("/".equals(resourcename)) {
            return CmsUUID.getNullUUID().toString();
        }
        
        String parent = CmsResource.getParentFolder(resourcename);
        parent = removeTrailingSeparator(parent);
        
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_PARENT_STRUCTURE_ID");
            stmt.setString(1, parent);            
            res = stmt.executeQuery();

            if (res.next()) {
                return res.getString(1);
            }
            
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        
        throw new CmsVfsResourceNotFoundException("Unable to read parent id of resource " + resourcename);
    }

    /**
     * Removes a resource physically in the database.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param resource the folder to remove
     *
     * @throws CmsException if something goes wrong
     */
    protected void internalRemoveFolder(CmsDbContext dbc, CmsProject currentProject, CmsResource resource) throws CmsException {
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
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
    private void preparePathCondition(int projectId, String parent, int mode, StringBuffer conditions, List params) {
        
        if (parent == I_CmsConstants.C_READ_IGNORE_PARENT) {            
            // parent can be ignored
            return;
        }
        
        if ((mode & I_CmsConstants.C_READMODE_EXCLUDE_TREE) > 0) {
            // only return immediate children - use UUID optimization            
            conditions.append(C_BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_PARENT_UUID"));
            conditions.append(C_END_CONDITION);
            params.add(parent);          
            return;
        }
        
        if ("/".equals(parent)) {
            // if root folder is parent, no additional condition is needed since all resource match anyway
            return;
        }

        // add condition to read path subtree        
        conditions.append(C_BEGIN_INCLUDE_CONDITION);
        conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_PATH_PREFIX"));
        conditions.append(C_END_CONDITION);
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
        if ((mode & I_CmsConstants.C_READMODE_INCLUDE_PROJECT) > 0) {    
            // C_READMODE_INCLUDE_PROJECT: add condition to match the PROJECT_ID
            conditions.append(C_BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_BY_PROJECT_LASTMODIFIED"));
            conditions.append(C_END_CONDITION);
            params.add(String.valueOf(projectId));
        }
    }

    private void prepareResourceCondition(int projectId, int mode, StringBuffer conditions) {
        if ((mode & I_CmsConstants.C_READMODE_ONLY_FOLDERS) > 0) {    
            // C_READMODE_ONLY_FOLDERS: add condition to match only folders
            conditions.append(C_BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_ONLY_FOLDERS"));
            conditions.append(C_END_CONDITION);
        } else if ((mode & I_CmsConstants.C_READMODE_ONLY_FILES) > 0) {
            // C_READMODE_ONLY_FILES: add condition to match only files
            conditions.append(C_BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, "C_RESOURCES_SELECT_ONLY_FILES"));
            conditions.append(C_END_CONDITION);
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
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/generic/Attic/CmsVfsDriver.java,v $
 * Date   : $Date: 2003/06/09 16:37:26 $
 * Version: $Revision: 1.7 $
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
 
package com.opencms.db.generic;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.db.*;
import com.opencms.db.CmsDriverManager;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsPropertydefinition;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.file.I_CmsResourceType;
import com.opencms.flex.util.CmsUUID;
import com.opencms.linkmanagement.CmsPageLinks;
import com.opencms.report.I_CmsReport;
import com.opencms.util.SqlHelper;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * Generic (ANSI-SQL) database server implementation of the VFS driver methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.7 $ $Date: 2003/06/09 16:37:26 $
 * @since 5.1.2
 */
public class CmsVfsDriver extends Object implements I_CmsVfsDriver {
    
    protected CmsDriverManager m_driverManager;
    protected com.opencms.db.generic.CmsSqlManager m_sqlManager;
    
    /**
     * Changes the project-id of a resource to the new project
     * for publishing the resource directly
     *
     * @param newProjectId The new project-id
     * @param resourcename The name of the resource to change
     */
    public void changeLockedInProject(int newProjectId, String resourcename) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
    
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UPDATE_PROJECTID");
            // write data to database
            stmt.setInt(1, newProjectId);
            stmt.setString(2, resourcename);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
	
    /**
     * Deletes all files in CMS_FILES without fileHeader in CMS_RESOURCES
     */
    protected void clearFilesTable() throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_DELETE_LOST_ID");
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
      * Copies the file.
      *
      * @param project The project in which the resource will be used.
      * @param onlineProject The online project of the OpenCms.
      * @param userId The id of the user who wants to copy the file.
      * @param source The complete path of the sourcefile.
      * @param parentId The parentId of the resource.
      * @param destination The complete path of the destinationfile.
      *
      * @throws CmsException Throws CmsException if operation was not succesful.
      */
    public void copyFile(CmsProject project, CmsUUID userId, CmsUUID parentId, String source, String destination) throws CmsException {
        CmsFile file;

        // read sourcefile
        file = readFile(project.getId(), source);
        // create destination file
        createFile(project, file, userId, parentId, destination);
    }
    
    /**
     * Counts the locked resources in this project.
     *
     * @param project The project to be unlocked.
     * @return the amount of locked resources in this project.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public int countLockedResources(CmsProject project) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        int retValue;
        try {
            // create the statement
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_COUNTLOCKED");
            stmt.setString(1, CmsUUID.getNullUUID().toString());
            stmt.setInt(2, project.getId());
            res = stmt.executeQuery();
            if (res.next()) {
                retValue = res.getInt(1);
            } else {
                retValue = 0;
            }
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return retValue;
    }
    /**
     * Returns the amount of properties for a propertydefinition.
     *
     * @param metadef The propertydefinition to test.
     *
     * @return the amount of properties for a propertydefinition.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    protected int countProperties(CmsPropertydefinition metadef) throws CmsException {
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
     * Semi-constructor to create a CmsFile instance from a JDBC result set.
     * 
     * @param res the JDBC ResultSet
     * @param hasProjectIdInResultSet true if the SQL select query includes the PROJECT_ID table attribute
     * @param hasFileContentInResultSet true if the SQL select query includes the FILE_CONTENT attribute
     * @return CmsFile the new CmsFile object
     * @throws SQLException in case the result set does not include a requested table attribute
     * @throws CmsException if the CmsFile object cannot be created by its constructor
     */
    protected final CmsFile createCmsFileFromResultSet(ResultSet res, boolean hasProjectIdInResultSet, boolean hasFileContentInResultSet) throws SQLException, CmsException {
        // this method is final to allow the java compiler to inline this code!
                    
        byte[] content = null;
        int projectId = I_CmsConstants.C_UNKNOWN_ID;

        CmsUUID resId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_ID")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_PARENT_ID")));
        String resName = res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_NAME"));
        int resType = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_TYPE"));
        int resFlags = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_FLAGS"));
        CmsUUID userId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_USER_ID")));
        CmsUUID groupId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_GROUP_ID")));
        CmsUUID fileId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_FILE_ID")));
        int accessFlags = res.getInt(m_sqlManager.get("C_RESOURCES_ACCESS_FLAGS"));
        int state = res.getInt(m_sqlManager.get("C_RESOURCES_STATE"));
        CmsUUID lockedBy = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_LOCKED_BY")));
        int launcherType = res.getInt(m_sqlManager.get("C_RESOURCES_LAUNCHER_TYPE"));
        String launcherClass = res.getString(m_sqlManager.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
        long created = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_CREATED")).getTime();
        long modified = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
        int resSize = res.getInt(m_sqlManager.get("C_RESOURCES_SIZE"));
        CmsUUID modifiedBy = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_LASTMODIFIED_BY")));
        int lockedInProject = res.getInt("LOCKED_IN_PROJECT");

        if (hasFileContentInResultSet) {
            content = m_sqlManager.getBytes(res, m_sqlManager.get("C_RESOURCES_FILE_CONTENT"));
        } else {
            content = new byte[0];
        }

        if (hasProjectIdInResultSet) {
            projectId = res.getInt(m_sqlManager.get("C_RESOURCES_PROJECT_ID"));
        } else {
            projectId = lockedInProject;
        }

        if (com.opencms.db.generic.CmsProjectDriver.C_USE_TARGET_DATE && resType == com.opencms.db.generic.CmsProjectDriver.C_RESTYPE_LINK_ID && resFlags > 0) {
            modified = fetchDateFromResource(projectId, resFlags, modified);
        }

        return new CmsFile(resId, parentId, fileId, resName, resType, resFlags, userId, groupId, projectId, accessFlags, state, lockedBy, launcherType, launcherClass, created, modified, modifiedBy, content, resSize, lockedInProject);
    }

    /**
     * Semi-constructor to create a CmsFile instance from a JDBC result set.
     * 
     * @param res the JDBC ResultSet
     * @param projectId the ID of the current project to adjust the modification date in case the file is a VFS link
     * @param resourceName the name of the selected file
     * @return CmsFile the new CmsFile object
     * @throws SQLException in case the result set does not include a requested table attribute
     * @throws CmsException if the CmsFile object cannot be created by its constructor
     */
    protected final CmsFile createCmsFileFromResultSet(ResultSet res, int projectId, String resourceName) throws SQLException, CmsException {
        // this method is final to allow the java compiler to inline this code!
        
        CmsUUID resId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_ID")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_PARENT_ID")));
        int resType = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_TYPE"));
        int resFlags = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_FLAGS"));
        CmsUUID userId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_USER_ID")));
        CmsUUID groupId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_GROUP_ID")));
        CmsUUID fileId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_FILE_ID")));
        int accessFlags = res.getInt(m_sqlManager.get("C_RESOURCES_ACCESS_FLAGS"));
        int state = res.getInt(m_sqlManager.get("C_RESOURCES_STATE"));
        CmsUUID lockedBy = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_LOCKED_BY")));
        int launcherType = res.getInt(m_sqlManager.get("C_RESOURCES_LAUNCHER_TYPE"));
        String launcherClass = res.getString(m_sqlManager.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
        long created = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_CREATED")).getTime();
        long modified = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
        int resSize = res.getInt(m_sqlManager.get("C_RESOURCES_SIZE"));
        CmsUUID modifiedBy = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_LASTMODIFIED_BY")));
        int lockedInProject = res.getInt("LOCKED_IN_PROJECT");
        byte[] content = m_sqlManager.getBytes(res, m_sqlManager.get("C_RESOURCES_FILE_CONTENT"));
        int resProjectId = res.getInt(m_sqlManager.get("C_RESOURCES_PROJECT_ID"));

        if (com.opencms.db.generic.CmsProjectDriver.C_USE_TARGET_DATE && resType == com.opencms.db.generic.CmsProjectDriver.C_RESTYPE_LINK_ID && resFlags > 0) {
            modified = fetchDateFromResource(projectId, resFlags, modified);
        }

        return new CmsFile(resId, parentId, fileId, resourceName, resType, resFlags, userId, groupId, resProjectId, accessFlags, state, lockedBy, launcherType, launcherClass, created, modified, modifiedBy, content, resSize, lockedInProject);
    }

    /**
     * Semi-constructor to create a CmsFolder instance from a JDBC result set.
     * 
     * @param res the JDBC ResultSet
     * @param hasProjectIdInResultSet true if the SQL select query includes the PROJECT_ID table attribute
     * @return CmsFolder the new CmsFolder object
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    protected final CmsFolder createCmsFolderFromResultSet(ResultSet res, boolean hasProjectIdInResultSet) throws SQLException {
        // this method is final to allow the java compiler to inline this code!
        int projectId = I_CmsConstants.C_UNKNOWN_ID;

        CmsUUID resId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_ID")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_PARENT_ID")));
        String resName = res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_NAME"));
        int resType = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_TYPE"));
        int resFlags = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_FLAGS"));
        CmsUUID userId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_USER_ID")));
        CmsUUID groupId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_GROUP_ID")));
        CmsUUID fileId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_FILE_ID")));
        int accessFlags = res.getInt(m_sqlManager.get("C_RESOURCES_ACCESS_FLAGS"));
        int state = res.getInt(m_sqlManager.get("C_RESOURCES_STATE"));
        CmsUUID lockedBy = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_LOCKED_BY")));
        long created = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_CREATED")).getTime();
        long modified = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
        CmsUUID modifiedBy = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_LASTMODIFIED_BY")));
        int lockedInProject = res.getInt("LOCKED_IN_PROJECT");

        if (hasProjectIdInResultSet) {
            projectId = res.getInt(m_sqlManager.get("C_RESOURCES_PROJECT_ID"));
        } else {
            projectId = lockedInProject;
        }

        // TODO: check if these attribs are unneccessary for a folder, and if so, remove them from the corresponding SQL queries
        //        res.getInt(m_sqlManager.get("C_RESOURCES_LAUNCHER_TYPE"));
        //        res.getString(m_sqlManager.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
        //        res.getInt(m_sqlManager.get("C_RESOURCES_SIZE"));        

        return new CmsFolder(resId, parentId, fileId, resName, resType, resFlags, userId, groupId, projectId, accessFlags, state, lockedBy, created, modified, modifiedBy, lockedInProject);
    }

    /**
     * Semi-constructor to create a CmsResource instance from a JDBC result set.
     * 
     * @param res the JDBC ResultSet
     * @param projectId the ID of the current project to adjust the modification date in case the resource is a VFS link
     * @return CmsResource the new CmsResource object
     * @throws SQLException in case the result set does not include a requested table attribute
     * @throws CmsException if the CmsFile object cannot be created by its constructor
     */
    public final CmsResource createCmsResourceFromResultSet(ResultSet res, int projectId) throws SQLException, CmsException {
        // this method is final to allow the java compiler to inline this code!

        CmsUUID resId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_ID")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_PARENT_ID")));
        String resName = res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_NAME"));
        int resType = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_TYPE"));
        int resFlags = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_FLAGS"));
        CmsUUID userId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_USER_ID")));
        CmsUUID groupId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_GROUP_ID")));
        int projectID = res.getInt(m_sqlManager.get("C_RESOURCES_PROJECT_ID"));
        CmsUUID fileId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_FILE_ID")));
        int accessFlags = res.getInt(m_sqlManager.get("C_RESOURCES_ACCESS_FLAGS"));
        int state = res.getInt(m_sqlManager.get("C_RESOURCES_STATE"));
        CmsUUID lockedBy = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_LOCKED_BY")));
        int launcherType = res.getInt(m_sqlManager.get("C_RESOURCES_LAUNCHER_TYPE"));
        String launcherClass = res.getString(m_sqlManager.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
        long created = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_CREATED")).getTime();
        long modified = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
        int resSize = res.getInt(m_sqlManager.get("C_RESOURCES_SIZE"));
        CmsUUID modifiedBy = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_LASTMODIFIED_BY")));
        int lockedInProject = res.getInt("LOCKED_IN_PROJECT");

        if (com.opencms.db.generic.CmsProjectDriver.C_USE_TARGET_DATE && resType == com.opencms.db.generic.CmsProjectDriver.C_RESTYPE_LINK_ID && resFlags > 0) {
            modified = fetchDateFromResource(projectId, resFlags, modified);
        }

        return new CmsResource(resId, parentId, fileId, resName, resType, resFlags, userId, groupId, projectID, accessFlags, state, lockedBy, launcherType, launcherClass, created, modified, modifiedBy, resSize, lockedInProject);
    }

    /**
     * Creates a new file from an given CmsFile object and a new filename.
     *
     * @param project The project in which the resource will be used.
     * @param onlineProject The online project of the OpenCms.
     * @param file The file to be written to the Cms.
     * @param user The Id of the user who changed the resourse.
     * @param parentId The parentId of the resource.
     * @param filename The complete new name of the file (including pathinformation).
     *
     * @return file The created file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsFile createFile(CmsProject project, CmsFile file, CmsUUID userId, CmsUUID parentId, String filename) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        // check the resource name
        if (filename.length() > I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Resourcename too long(>" + I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME + ") ", CmsException.C_BAD_NAME);
        }

        int state = 0;
        CmsUUID modifiedByUserId = userId;
        long dateModified = System.currentTimeMillis();

        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            state = file.getState();
            modifiedByUserId = file.getResourceLastModifiedBy();
            dateModified = file.getDateLastModified();
        } else {
            state = I_CmsConstants.C_STATE_NEW;
        }

        // Test if the file is already there and marked as deleted.
        // If so, delete it.
        // If the file exists already and is not marked as deleted then throw exception
        try {
            readFileHeader(project.getId(), filename, false);
            throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
        } catch (CmsException e) {
            // if the file is marked as deleted remove it!
            if (e.getType() == CmsException.C_RESOURCE_DELETED) {
                removeFile(project.getId(), filename);
                state = I_CmsConstants.C_STATE_CHANGED;
            }
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                throw e;
            }
        }
        
        CmsUUID resourceId = new CmsUUID();
        CmsUUID fileId = new CmsUUID();
        //CmsUUID treeId = new CmsUUID();

        try {   
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_WRITE");                   
          
            // first write the file content
            try {
                createFileContent(fileId, file.getContents(), 0, project.getId(), false);
            } catch (CmsException se) {
                if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + "] " + se.getMessage());
                }
            }
            
            // now write the file header
            stmt.setString(1, resourceId.toString());
            stmt.setString(2, parentId.toString());
            stmt.setString(3, filename);
            stmt.setInt(4, file.getType());
            stmt.setInt(5, file.getFlags());
            stmt.setString(6, file.getOwnerId().toString());
            stmt.setString(7, file.getGroupId().toString());
            stmt.setInt(8, project.getId());
            stmt.setString(9, fileId.toString());
            stmt.setInt(10, file.getAccessFlags());
            stmt.setInt(11, state);
            stmt.setString(12, file.isLockedBy().toString());
            stmt.setInt(13, file.getLauncherType());
            stmt.setString(14, file.getLauncherClassname());
            stmt.setTimestamp(15, new Timestamp(file.getDateCreated()));
            stmt.setTimestamp(16, new Timestamp(dateModified));
            stmt.setInt(17, file.getLength());
            stmt.setString(18, modifiedByUserId.toString());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readFile(project.getId(), filename);
    }

    /**
     * Creates a new file with the given content and resourcetype.
     *
     * @param user The user who wants to create the file.
     * @param project The project in which the resource will be used.
     * @param onlineProject The online project of the OpenCms.
     * @param filename The complete name of the new file (including pathinformation).
     * @param flags The flags of this resource.
     * @param parentId The parentId of the resource.
     * @param contents The contents of the new file.
     * @param resourceType The resourceType of the new file.
     *
     * @return file The created file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsFile createFile(CmsUser user, CmsProject project, String filename, int flags, CmsUUID parentId, byte[] contents, I_CmsResourceType resourceType) throws CmsException {
        CmsFile newFile = new CmsFile(
            // resource ID
            CmsUUID.getNullUUID(),
            // parent ID
            parentId,
            // file ID
            CmsUUID.getNullUUID(),
            // resource name
            filename,
            // resource type
            resourceType.getResourceType(),
            // resource flags
            flags,
            // user ID
            user.getId(),
            // user group
            user.getDefaultGroupId(),
            // project ID
            project.getId(),
            // access flags
            com.opencms.core.I_CmsConstants.C_ACCESS_DEFAULT_FLAGS,
            // resource state
            com.opencms.core.I_CmsConstants.C_STATE_NEW,
            // locked by
            CmsUUID.getNullUUID(),
            // launcher type
            resourceType.getLauncherType(),
            // launcher class
            resourceType.getLauncherClass(),
            // date created
            0,
            // date last modified
            0,
            // last modified by
            user.getId(),
            // content
            contents,
            // content size
            contents.length,
            // locked in project
            project.getId());
    
        return createFile(project, newFile, user.getId(), parentId, filename);          
    }

    /**
     * Creates the content entry for a file
     *
     * @param fileId The ID of the new file
     * @param fileContent The content of the new file
     * @param versionId For the content of a backup file you need to insert the versionId of the backup
     * @param usedPool The name of the databasepool to use
     * @param usedStatement Specifies which tables must be used: offline, online or backup
     *
     */
    public void createFileContent(CmsUUID fileId, byte[] fileContent, int versionId, int projectId, boolean writeBackup) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (writeBackup) {
                conn = m_sqlManager.getConnectionForBackup();
                stmt = m_sqlManager.getPreparedStatement(conn, "C_FILES_WRITE_BACKUP");
            }
            else {
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
            }

            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Creates a new folder from an existing folder object.
     *
     * @param user The user who wants to create the folder.
     * @param project The project in which the resource will be used.
     * @param onlineProject The online project of the OpenCms.
     * @param folder The folder to be written to the Cms.
     * @param parentId The parentId of the resource.
     *
     * @param foldername The complete path of the new name of this folder.
     *
     * @return The created folder.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsFolder createFolder(CmsUser user, CmsProject project, CmsFolder folder, CmsUUID parentId, String foldername) throws CmsException {

        if (foldername.length() > I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Resourcename too long(>" + I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME + ") ", CmsException.C_BAD_NAME);
        }

        CmsFolder oldFolder = null;
        int state = 0;
        CmsUUID modifiedByUserId = user.getId();
        long dateModified = System.currentTimeMillis();

        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            state = folder.getState();
            modifiedByUserId = folder.getResourceLastModifiedBy();
            dateModified = folder.getDateLastModified();
        } else {
            state = I_CmsConstants.C_STATE_NEW;
        }

        // Test if the file is already there and marked as deleted.
        // If so, delete it
        // No, dont delete it, throw exception (h.riege, 04.01.01)
        try {
            oldFolder = readFolder(project.getId(), foldername);
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

        CmsUUID resourceId = new CmsUUID();

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_WRITE");

            stmt.setString(1, resourceId.toString());
            stmt.setString(2, parentId.toString());
            stmt.setString(3, foldername);
            stmt.setInt(4, folder.getType());
            stmt.setInt(5, folder.getFlags());
            stmt.setString(6, folder.getOwnerId().toString());
            stmt.setString(7, folder.getGroupId().toString());
            stmt.setInt(8, project.getId());
            stmt.setString(9, CmsUUID.getNullUUID().toString());
            stmt.setInt(10, folder.getAccessFlags());
            stmt.setInt(11, state);
            stmt.setString(12, folder.isLockedBy().toString());
            stmt.setInt(13, folder.getLauncherType());
            stmt.setString(14, folder.getLauncherClassname());
            stmt.setTimestamp(15, new Timestamp(folder.getDateCreated()));
            stmt.setTimestamp(16, new Timestamp(dateModified));
            stmt.setInt(17, 0);
            stmt.setString(18, modifiedByUserId.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        // if this is the rootfolder or if the parentfolder is the rootfolder
        // try to create the projectresource
        String parentFolderName = "/";
        if (!folder.getResourceName().equals(I_CmsConstants.C_ROOT)) {
            parentFolderName = folder.getResourceName().substring(0, folder.getResourceName().length() - 1);
            parentFolderName = parentFolderName.substring(0, parentFolderName.lastIndexOf("/") + 1);
        }

        if (parentId.isNullUUID() || parentFolderName.equals(I_CmsConstants.C_ROOT)) {
            try {
                String rootFolder = null;
                try {
                    rootFolder = readProjectResource(project.getId(), I_CmsConstants.C_ROOT);
                } catch (CmsException exc) {
                    // NOOP
                }

                if (rootFolder == null) {
                    createProjectResource(project.getId(), foldername);
                }

                //createProjectResource(project.getId(), foldername);
            } catch (CmsException e) {
                if (e.getType() != CmsException.C_FILE_EXISTS) {
                    throw e;
                }
            }
        }

        return readFolder(project.getId(), foldername);
    }

    /**
      * Creates a new folder
      *
      * @param user The user who wants to create the folder.
      * @param project The project in which the resource will be used.
      * @param parentId The parentId of the folder.
      * @param fileId The fileId of the folder.
      * @param foldername The complete path to the folder in which the new folder will
      * be created.
      * @param flags The flags of this resource.
      *
      * @return The created folder.
      * @throws CmsException Throws CmsException if operation was not succesful.
      */
    public CmsFolder createFolder(CmsUser user, CmsProject project, CmsUUID parentId, CmsUUID fileId, String folderName, int flags) throws CmsException {
        CmsFolder newFolder = new CmsFolder(
            // resource ID
            CmsUUID.getNullUUID(),
            // parent ID
            parentId,
            // file ID
            CmsUUID.getNullUUID(),
            // resource name
            folderName,
            // resource type
            com.opencms.core.I_CmsConstants.C_TYPE_FOLDER,
            // resource flags
            flags,
            // user ID
            user.getId(),
            // user group
            user.getDefaultGroupId(),
            // project ID
            project.getId(),
            // access flags
            com.opencms.core.I_CmsConstants.C_ACCESS_DEFAULT_FLAGS,
            // resource state
            com.opencms.core.I_CmsConstants.C_STATE_NEW,
            // locked by
            CmsUUID.getNullUUID(),
            // date created
            0,
            // date last modified
            0,
            // last modified by
            user.getId(),
            // locked in project
            project.getId());
    
        return createFolder(user, project, newFolder, parentId, folderName);        
    }

    /**
     * Creates a new projectResource from a given CmsResource object.
     *
     * @param project The project in which the resource will be used.
     * @param resource The resource to be written to the Cms.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void createProjectResource(int projectId, String resourceName) throws CmsException {
        // do not create entries for online-project
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            readProjectResource(projectId, resourceName);
            throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
        } catch (CmsException e) {
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                throw e;
            }
        }

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_CREATE");

            // write new resource to the database
            stmt.setInt(1, projectId);
            stmt.setString(2, resourceName);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Creates the propertydefinitions for the resource type.<BR/>
     *
     * Only the admin can do this.
     *
     * @param name The name of the propertydefinitions to overwrite.
     * @param resourcetype The resource-type for the propertydefinitions.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition createPropertydefinition(String name, int projectId, int resourcetype) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {           
            for (int i=0; i<3; i++) {
                // create the offline property definition
                if (i == 0) {
                    conn = m_sqlManager.getConnection();
                    stmt = m_sqlManager.getPreparedStatement(conn, Integer.MAX_VALUE, "C_PROPERTYDEF_CREATE");
                    stmt.setInt(1, m_sqlManager.nextId(m_sqlManager.get("C_TABLE_PROPERTYDEF")));
                }
                // create the online property definition
                else if (i == 1) {
                    conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                    stmt = m_sqlManager.getPreparedStatement(conn, I_CmsConstants.C_PROJECT_ONLINE_ID, "C_PROPERTYDEF_CREATE");
                    stmt.setInt(1, m_sqlManager.nextId(m_sqlManager.get("C_TABLE_PROPERTYDEF_ONLINE")));
                }
                // create the backup property definition
                else {
                    conn = m_sqlManager.getConnectionForBackup();
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_CREATE_BACKUP");
                    stmt.setInt(1, m_sqlManager.nextId(m_sqlManager.get("C_TABLE_PROPERTYDEF_BACKUP")));
                }
                stmt.setString(2, name);
                stmt.setInt(3, resourcetype);
                stmt.executeUpdate();
                m_sqlManager.closeAll(conn, stmt, null);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
        
        return readPropertydefinition(name, projectId, resourcetype);
    }

    /**
     * Creates a new resource from an given CmsResource object.
     *
     * @param project The project in which the resource will be used.
     * @param onlineProject The online project of the OpenCms.
     * @param newResource The resource to be written to the Cms.
     * @param filecontent The filecontent if the resource is a file
     * @param userId The ID of the current user.
     * @param parentId The parentId of the resource.
     *
     * @return resource The created resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsResource createResource(CmsProject project, CmsResource newResource, byte[] filecontent, CmsUUID userId, boolean isFolder) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        // check the resource name
        if (newResource.getResourceName().length() > I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME) {
            throw new CmsException("[" + this.getClass().getName() + "] resource name too long(>" + I_CmsConstants.C_MAX_LENGTH_RESOURCE_NAME + ") ", CmsException.C_BAD_NAME);
        }

        int state = 0;
        CmsUUID modifiedByUserId = userId;
        long dateModified = newResource.isTouched() ? newResource.getDateLastModified() : System.currentTimeMillis();

        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            state = newResource.getState();
            modifiedByUserId = newResource.getResourceLastModifiedBy();
            dateModified = newResource.getDateLastModified();
        } else {
            state = I_CmsConstants.C_STATE_NEW;
        }

        // Test if the file is already there and marked as deleted.
        // If so, delete it.
        // If the file exists already and is not marked as deleted then throw exception
        try {
            readResource(project, newResource.getResourceName());
            throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
        } catch (CmsException e) {
            // if the resource is marked as deleted remove it!
            if (e.getType() == CmsException.C_RESOURCE_DELETED) {
                if (isFolder) {
                    removeFolder(project.getId(), (CmsFolder) newResource);
                } else {
                    removeFile(project.getId(), newResource.getResourceName());
                }
                state = I_CmsConstants.C_STATE_CHANGED;
                //throw new CmsException("["+this.getClass().getName()+"] ",CmsException.C_FILE_EXISTS);
            }
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                throw e;
            }
        }

        CmsUUID newFileId = CmsUUID.getNullUUID();
        CmsUUID resourceId = new CmsUUID();

        // now write the resource
        try {
            conn = m_sqlManager.getConnection(project.getId());
            if (!isFolder) {
                // first write the file content
                newFileId = new CmsUUID();
                try {
                    createFileContent(newFileId, filecontent, 0, project.getId(), false);
                } catch (CmsException se) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + "] " + se.getMessage());
                    }
                }
            }

            // now write the file header
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_WRITE");
            stmt.setString(1, resourceId.toString());
            stmt.setString(2, newResource.getParentId().toString());
            stmt.setString(3, newResource.getResourceName());
            stmt.setInt(4, newResource.getType());
            stmt.setInt(5, newResource.getFlags());
            stmt.setString(6, newResource.getOwnerId().toString());
            stmt.setString(7, newResource.getGroupId().toString());
            stmt.setInt(8, project.getId());
            stmt.setString(9, newFileId.toString());
            stmt.setInt(10, newResource.getAccessFlags());
            stmt.setInt(11, state);
            stmt.setString(12, newResource.isLockedBy().toString());
            stmt.setInt(13, newResource.getLauncherType());
            stmt.setString(14, newResource.getLauncherClassname());
            stmt.setTimestamp(15, new Timestamp(newResource.getDateCreated()));
            stmt.setTimestamp(16, new Timestamp(dateModified));
            stmt.setInt(17, newResource.getLength());
            stmt.setString(18, modifiedByUserId.toString());

            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readResource(project, newResource.getResourceName());
    }

    /**
     * delete all projectResource from an given CmsProject object.
     *
     * @param project The project in which the resource is used.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteAllProjectResources(int projectId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_DELETEALL");
            // delete all projectResources from the database
            stmt.setInt(1, projectId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
    /**
     * Deletes all properties for a file or folder.
     *
     * @param resourceId The id of the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteAllProperties(int projectId, CmsResource resource) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
    
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_DELETEALL");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
    /**
     * Deletes all properties for a file or folder.
     *
     * @param resourceId The id of the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteAllProperties(int projectId, CmsUUID resourceId) throws CmsException {
    
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            // create statement
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_DELETEALL");
            stmt.setString(1, resourceId.toString());
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Deletes the file.
     *
     * @param project The project in which the resource will be used.
     * @param filename The complete path of the file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void deleteFile(CmsProject project, String filename) throws CmsException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_REMOVE");
            // mark the file as deleted
            stmt.setInt(1, com.opencms.core.I_CmsConstants.C_STATE_DELETED);
            stmt.setString(2, CmsUUID.getNullUUID().toString());
            stmt.setString(3, filename);
            //statement.setInt(4,project.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Deletes the folder.
     *
     * Only empty folders can be deleted yet.
     *
     * @param project The project in which the resource will be used.
     * @param orgFolder The folder that will be deleted.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void deleteFolder(CmsProject project, CmsFolder orgFolder) throws CmsException {
        // the current implementation only deletes empty folders
        // check if the folder has any files in it
        Vector files = getFilesInFolder(project.getId(), orgFolder);
        files = getUndeletedResources(files);
        if (files.size() == 0) {
            // check if the folder has any folders in it
            Vector folders = getSubFolders(project.getId(), orgFolder);
            folders = getUndeletedResources(folders);
            if (folders.size() == 0) {
                //this folder is empty, delete it
                Connection conn = null;
                PreparedStatement stmt = null;
                try {
                    conn = m_sqlManager.getConnection(project);
                    stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_REMOVE");
                    // mark the folder as deleted
                    stmt.setInt(1, com.opencms.core.I_CmsConstants.C_STATE_DELETED);
                    stmt.setString(2, CmsUUID.getNullUUID().toString());
                    stmt.setString(3, orgFolder.getResourceName());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
                } finally {
                    m_sqlManager.closeAll(conn, stmt, null);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + orgFolder.getAbsolutePath(), CmsException.C_NOT_EMPTY);
            }
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + orgFolder.getAbsolutePath(), CmsException.C_NOT_EMPTY);
        }
    }

    /**
     * delete a projectResource from an given CmsResource object.
     *
     * @param project The project in which the resource is used.
     * @param resource The resource to be deleted from the Cms.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProjectResource(int projectId, String resourceName) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_DELETE");
            // delete resource from the database
            stmt.setInt(1, projectId);
            stmt.setString(2, resourceName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Deletes a specified project
     *
     * @param project The project to be deleted.
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void deleteProjectResources(CmsProject project) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_DELETE_PROJECT");
            // delete all project-resources.
            stmt.setInt(1, project.getId());
            stmt.executeQuery();
            // delete all project-files.
            //clearFilesTable();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
    /**
     * Deletes a property for a file or folder.
     *
     * @param meta The property-name of which the property has to be read.
     * @param resourceId The id of the resource.
     * @param resourceType The Type of the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException {
        CmsPropertydefinition propdef = readPropertydefinition(meta, 0, resourceType);
        if (propdef == null) {
            // there is no propdefinition with the overgiven name for the resource
            throw new CmsException("[" + this.getClass().getName() + "] " + meta, CmsException.C_NOT_FOUND);
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
                stmt.executeUpdate();
            } catch (SQLException exc) {
                throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
            } finally {
                m_sqlManager.closeAll(conn, stmt, null);
            }
        }
    }
    /**
     * Delete the propertydefinitions for the resource type.<BR/>
     *
     * Only the admin can do this.
     *
     * @param metadef The propertydefinitions to be deleted.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void deletePropertydefinition(CmsPropertydefinition metadef) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            if (countProperties(metadef) != 0) {
                throw new CmsException("[" + this.getClass().getName() + "] " + metadef.getName(), CmsException.C_UNKNOWN_EXCEPTION);
            }
            for (int i = 0; i < 3; i++) {
                // delete the propertydef from offline db
                if (i == 0) {
                    conn = m_sqlManager.getConnection();
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_DELETE");
                }
                // delete the propertydef from online db
                else if (i == 1) {
                    conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                    stmt = m_sqlManager.getPreparedStatement(conn, I_CmsConstants.C_PROJECT_ONLINE_ID, "C_PROPERTYDEF_DELETE");
                }
                // delete the propertydef from backup db
                else {
                    conn = m_sqlManager.getConnectionForBackup();
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_DELETE_BACKUP");
                }
                stmt.setInt(1, metadef.getId());
                stmt.executeUpdate();
                stmt.close();
                conn.close();
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Private helper method to delete a resource.
     *
     * @param id the id of the resource to delete.
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public void deleteResource(CmsResource resource) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // delete resource data from database
            conn = m_sqlManager.getConnection(resource.getProjectId());
            stmt = m_sqlManager.getPreparedStatement(conn, resource.getProjectId(), "C_RESOURCES_DELETEBYID");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.executeUpdate();
            stmt.close();

            // delete the file content
            stmt = m_sqlManager.getPreparedStatement(conn, resource.getProjectId(), "C_FILE_DELETE");
            stmt.setString(1, resource.getFileId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
    
    public void destroy() throws Throwable {
        finalize();
                
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[" + this.getClass().getName() + "] destroyed!");
        }
    }    

    /**
     * Fetch all VFS links pointing to other VFS resources.
     * 
     * @param theProject the resources in this project are updated
     * @param theResourceIDs reference to an ArrayList where the ID's of the fetched links are stored
     * @param theLinkContents reference to an ArrayList where the contents of the fetched links (= VFS resource names of the targets) are stored
     * @param theResourceTypeLinkID the ID of the link resource type
     * @return the count of affected rows
     */
    public int fetchAllVfsLinks(CmsProject theProject, ArrayList theResourceIDs, ArrayList theLinkContents, ArrayList theLinkResources, int theResourceTypeLinkID) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        int rowCount = 0;
        ResultSet res = null;

        try {
            // execute the query
            conn = m_sqlManager.getConnection(theProject);
            stmt = m_sqlManager.getPreparedStatement(conn, theProject, "C_SELECT_VFS_LINK_RESOURCES");
            stmt.setInt(1, theResourceTypeLinkID);
            res = stmt.executeQuery();

            while (res.next()) {
                theResourceIDs.add((String) res.getString(1));
                theLinkContents.add((String) new String(m_sqlManager.getBytes(res, m_sqlManager.get("C_FILE_CONTENT"))));
                theLinkResources.add((String) res.getString(3));
                rowCount++;
            }
        } catch (SQLException e) {
            rowCount = 0;
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return rowCount;
    }

    public long fetchDateFromResource(int theProjectId, int theResourceId, long theDefaultDate) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        long date_lastModified = theDefaultDate;

        try {
            // execute the query
            conn = m_sqlManager.getConnection(theProjectId);
            stmt = m_sqlManager.getPreparedStatement(conn, theProjectId, "C_SELECT_RESOURCE_DATE_LASTMODIFIED");
            stmt.setInt(1, theResourceId);
            res = stmt.executeQuery();

            if (res.next()) {
                date_lastModified = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
                //System.err.println( "date: " + result.getObject(1).toString() );
            } else {
                date_lastModified = theDefaultDate;
            }
        } catch (SQLException e) {
            //System.err.println( "\n[" + this.getClass().getName() + ".fetchDateFromResource()] " + e.toString() );
            date_lastModified = theDefaultDate;
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return date_lastModified;
    }

    /**
     * Fetches the RESOURCE_FLAGS attribute for a given resource name.
     * This method is slighty more efficient that calling readFileHeader().
     * 
     * @param theProject the current project to choose the right SQL query
     * @param theResourceName the name of the resource of which the resource flags are fetched
     * @return the value of the resource flag attribute.
     * @throws CmsException
     */
    public int fetchResourceFlags(CmsProject theProject, String theResourceName) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        int resourceFlags = 0;
        ResultSet res = null;

        try {
            // execute the query
            conn = m_sqlManager.getConnection(theProject);
            stmt = m_sqlManager.getPreparedStatement(conn, theProject, "C_SELECT_RESOURCE_FLAGS");
            stmt.setString(1, theResourceName);
            res = stmt.executeQuery();

            if (res.next()) {
                resourceFlags = res.getInt(1);
            } else {
                resourceFlags = 0;
            }
        } catch (SQLException e) {
            resourceFlags = 0;
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return resourceFlags;
    }

    /**
     * Fetch the ID for a given VFS link target.
     * 
     * @param theProject the CmsProject where the resource is fetched
     * @param theResourceName the name of the resource for which we fetch it's ID
     * @param skipResourceTypeID targets of this resource type are ignored
     * @return the ID of the resource, or -1
     */
    public int fetchResourceID(CmsProject theProject, String theResourceName, int skipResourceTypeID) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        int resourceID = 0;
        ResultSet res = null;

        try {
            // execute the query
            conn = m_sqlManager.getConnection(theProject);
            stmt = m_sqlManager.getPreparedStatement(conn, theProject, "C_SELECT_RESOURCE_ID");
            stmt.setString(1, theResourceName);
            res = stmt.executeQuery();

            if (res.next()) {
                int resourceTypeID = res.getInt(2);

                if (resourceTypeID != skipResourceTypeID) {
                    resourceID = res.getInt(1);
                } else {
                    resourceID = -1;
                }
            } else {
                resourceID = 0;
            }
        } catch (SQLException e) {
            resourceID = 0;
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return resourceID;
    }

    /**
     * Fetches all VFS links pointing to a given resource ID.
     * 
     * @param theProject the current project
     * @param theResourceID the ID of the resource of which the VFS links are fetched
     * @param theResourceTypeLinkID the resource type ID of VFS links
     * @return an ArrayList with the resource names of the fetched VFS links
     * @throws CmsException
     */
    public ArrayList fetchVfsLinksForResourceID(CmsProject theProject, int theResourceID, int theResourceTypeLinkID) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        ArrayList vfsLinks = new ArrayList();

        try {
            // execute the query
            conn = m_sqlManager.getConnection(theProject);
            stmt = m_sqlManager.getPreparedStatement(conn, theProject, "C_SELECT_VFS_LINKS");
            stmt.setInt(1, theResourceID);
            stmt.setInt(2, theResourceTypeLinkID);
            stmt.setInt(3, com.opencms.core.I_CmsConstants.C_STATE_DELETED);
            res = stmt.executeQuery();

            while (res.next()) {
                CmsResource resource = this.readFileHeader(theProject.getId(), res.getString(1), false);

                if (resource != null) {
                    vfsLinks.add(resource);
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return vfsLinks;
    }
    
    protected void finalize() throws Throwable {
        if (m_sqlManager!=null) {
            m_sqlManager.finalize();
        }
        
        m_sqlManager = null;      
        m_driverManager = null;        
    }
    /**
     * helper method for getBrokenLinks.
     */
    protected Vector getAllOnlineReferencesForLink(String link, Vector exceptions) throws CmsException {
        Vector resources = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_GET_ONLINE_REFERENCES");
            stmt.setString(1, link);
            res = stmt.executeQuery();
            while (res.next()) {
                String resName = res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_NAME"));
                if (!exceptions.contains(resName)) {
                    CmsPageLinks pl = new CmsPageLinks(new CmsUUID(res.getString(m_sqlManager.get("C_LM_PAGE_ID"))));
                    pl.setOnline(true);
                    pl.addLinkTarget(link);
                    pl.setResourceName(resName);
                    resources.add(pl);
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "getAllOnlineReferencesForLink(String, Vector)", CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, "getAllOnlineReferencesForLink(String, Vector)", CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return resources;
    }
    /**
     * checks a project for broken links that would appear if the project is published.
     *
     * @param report A cmsReport object for logging while the method is still running.
     * @param changed A vecor (of CmsResources) with the changed resources in the project.
     * @param deleted A vecor (of CmsResources) with the deleted resources in the project.
     * @param newRes A vecor (of CmsResources) with the new resources in the project.
     */
    public void getBrokenLinks(I_CmsReport report, Vector changed, Vector deleted, Vector newRes) throws CmsException {
    
        // first create some Vectors for performance increase
        Vector deletedByName = new Vector(deleted.size());
        for (int i = 0; i < deleted.size(); i++) {
            deletedByName.add(((CmsResource) deleted.elementAt(i)).getResourceName());
        }
        Vector newByName = new Vector(newRes.size());
        for (int i = 0; i < newRes.size(); i++) {
            newByName.add(((CmsResource) newRes.elementAt(i)).getResourceName());
        }
        Vector changedByName = new Vector(changed.size());
        for (int i = 0; i < changed.size(); i++) {
            changedByName.add(((CmsResource) changed.elementAt(i)).getResourceName());
        }
        Vector onlineResNames = getOnlineResourceNames();
    
        // now check the new and the changed resources
        for (int i = 0; i < changed.size(); i++) {
            CmsUUID resId = ((CmsResource) changed.elementAt(i)).getResourceId();
            Vector currentLinks = m_driverManager.getProjectDriver().readLinkEntrys(resId);
            CmsPageLinks aktualBrokenList = new CmsPageLinks(resId);
            for (int index = 0; index < currentLinks.size(); index++) {
                String curElement = (String) currentLinks.elementAt(index);
                if (!((onlineResNames.contains(curElement) && !deletedByName.contains(curElement)) || (newByName.contains(curElement)))) {
                    // this is a broken link
                    aktualBrokenList.addLinkTarget(curElement);
                }
            }
            if (aktualBrokenList.getLinkTargets().size() != 0) {
                aktualBrokenList.setResourceName(((CmsResource) changed.elementAt(i)).getResourceName());
                report.println(aktualBrokenList);
            }
        }
        for (int i = 0; i < newRes.size(); i++) {
            CmsUUID resId = ((CmsResource) newRes.elementAt(i)).getResourceId();
            Vector currentLinks = m_driverManager.getProjectDriver().readLinkEntrys(resId);
            CmsPageLinks aktualBrokenList = new CmsPageLinks(resId);
            for (int index = 0; index < currentLinks.size(); index++) {
                String curElement = (String) currentLinks.elementAt(index);
                if (!((onlineResNames.contains(curElement) && !deletedByName.contains(curElement)) || (newByName.contains(curElement)))) {
                    // this is a broken link
                    aktualBrokenList.addLinkTarget(curElement);
                }
            }
            if (aktualBrokenList.getLinkTargets().size() != 0) {
                aktualBrokenList.setResourceName(((CmsResource) newRes.elementAt(i)).getResourceName());
                report.println(aktualBrokenList);
            }
        }
    
        // now we have to check if the deleted resources make any problems
        Hashtable onlineResults = new Hashtable();
        changedByName.addAll(deletedByName);
        for (int i = 0; i < deleted.size(); i++) {
            Vector refs = getAllOnlineReferencesForLink(((CmsResource) deleted.elementAt(i)).getResourceName(), changedByName);
            for (int index = 0; index < refs.size(); index++) {
                CmsPageLinks pl = (CmsPageLinks) refs.elementAt(index);
                CmsUUID key = pl.getResourceId();
                CmsPageLinks old = (CmsPageLinks) onlineResults.get(key);
                if (old == null) {
                    onlineResults.put(key, pl);
                } else {
                    old.addLinkTarget((String) (pl.getLinkTargets().firstElement()));
                }
            }
        }
        // now lets put the results in the report (behind a seperator)
        Enumeration enu = onlineResults.elements();
        while (enu.hasMoreElements()) {
            report.println((CmsPageLinks) enu.nextElement());
        }
    }

    /**
     * Returns a Vector with all file headers of a folder.<BR/>
     *
     * @param parentFolder The folder to be searched.
     *
     * @return subfiles A Vector with all file headers of the folder.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getFilesInFolder(int projectId, CmsFolder parentFolder) throws CmsException {
        Vector files = new Vector();
        ResultSet res = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            //  get all files in folder
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_FILESINFOLDER");
            stmt.setString(1, parentFolder.getResourceId().toString());
            res = stmt.executeQuery();
            
            // create new file objects
            while (res.next()) {
                files.addElement(createCmsFileFromResultSet(res, false, false));
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return files;
    }
    /**
     * Returns a Vector with all resource-names that have set the given property to the given value.
     *
     * @param projectid, the id of the project to test.
     * @param propertydef, the name of the propertydefinition to check.
     * @param property, the value of the property for the resource.
     *
     * @return Vector with all names of resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getFilesWithProperty(int projectId, String propertyDefinition, String propertyValue) throws CmsException {
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
     * Reads the complete folder-tree for this project.<BR>
     *
     * @param project The project in which the folders are.
     * @param rootName The name of the root, e.g. /default/vfs
     *
     * @return A Vecor of folders.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector getFolderTree(int projectId, String rootName) throws CmsException {
        Vector folders = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        String add1 = " " + m_sqlManager.get(projectId, "C_RESOURCES_GET_FOLDERTREE_ADD1") + rootName;
        String add2 = m_sqlManager.get(projectId, "C_RESOURCES_GET_FOLDERTREE_ADD2");
        
        try {
            // read file data from database
            conn = m_sqlManager.getConnection(projectId);
            String query = m_sqlManager.get(projectId, "C_RESOURCES_GET_FOLDERTREE") + add1 + add2;
            stmt = m_sqlManager.getPreparedStatementForSql(conn,query);
            stmt.setInt(1, projectId);
            res = stmt.executeQuery();
            // create new file
            while (res.next()) {
                folders.addElement(createCmsFolderFromResultSet(res, true));
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw new CmsException("[" + this.getClass().getName() + "]", ex);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return folders;
    }
    /**
     * This method reads all resource names from the table CmsOnlineResources
     *
     * @return A Vector (of Strings) with the resource names (like from getAbsolutePath())
     */
    public Vector getOnlineResourceNames() throws CmsException {
    
        Vector resources = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_GET_ALL_ONLINE_RES_NAMES");
            res = stmt.executeQuery();
            // create new resource
            while (res.next()) {
                String resName = res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_NAME"));
                resources.add(resName);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "getOnlineResourceNames()", CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, "getOnlineResourceNames()", CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return resources;
    }

    /**
     * Reads all resources (including the folders) residing in a folder<BR>
     *
     * @param onlineResource the parent resource id of the online resoure.
     * @param offlineResource the parent resource id of the offline resoure.
     *
     * @return A Vecor of resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector getResourcesInFolder(int projectId, CmsFolder offlineResource) throws CmsException {
        Vector resources = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        // first get the folders
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_FOLDERS_IN_FOLDER");
            stmt.setString(1, offlineResource.getResourceId().toString());
            stmt.setInt(2, projectId);
            res = stmt.executeQuery();

            String lastfolder = null;
            while (res.next()) {
                CmsFolder folder = createCmsFolderFromResultSet(res, true);
                if (!folder.getName().equalsIgnoreCase(lastfolder)) {
                    resources.addElement(folder);
                }
                lastfolder = folder.getName();
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw new CmsException("[" + this.getClass().getName() + "]", ex);
        } finally {
            m_sqlManager.closeAll(null, stmt, res);
        }

        // then get the resources
        try {
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCES_IN_FOLDER");
            stmt.setString(1, offlineResource.getResourceId().toString());
            stmt.setInt(2, projectId);
            res = stmt.executeQuery();

            String lastresource = null;
            while (res.next()) {
                CmsResource resource = createCmsResourceFromResultSet(res, projectId);
                if (!resource.getName().equalsIgnoreCase(lastresource)) {
                    resources.addElement(resource);
                }
                lastresource = resource.getName();
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw new CmsException("[" + this.getClass().getName() + "]", ex);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return resources;
    }

    /**
     * Returns a Vector with all resources of the given type
     * that have set the given property. For the start it is
     * only used by the static export so it reads the online project only.
     *
     * @param projectid, the id of the project to test.
     * @param propertyDefinition, the name of the propertydefinition to check.
     *
     * @return Vector with all resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getResourcesWithProperty(int projectId, String propertyDefinition) throws CmsException {
        Vector resources = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF");
            stmt.setInt(1, projectId);
            stmt.setString(2, propertyDefinition);
            res = stmt.executeQuery();
            String lastResourcename = "";
            // store the result into the vector
            while (res.next()) {
                CmsResource resource = createCmsResourceFromResultSet(res, projectId);
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
     * Returns a Vector with all resources of the given type
     * that have set the given property to the given value.
     *
     * @param projectid, the id of the project to test.
     * @param propertyDefinition, the name of the propertydefinition to check.
     * @param propertyValue, the value of the property for the resource.
     * @param resourceType, the value of the resourcetype.
     *
     * @return Vector with all resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getResourcesWithProperty(int projectId, String propertyDefinition, String propertyValue, int resourceType) throws CmsException {
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
            // store the result into the vector
            while (res.next()) {
                CmsResource resource = createCmsResourceFromResultSet(res, projectId);
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
     * Returns a Vector with all subfolders.<BR/>
     *
     * @param parentFolder The folder to be searched.
     *
     * @return Vector with all subfolders for the given folder.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public Vector getSubFolders(int projectId, CmsFolder parentFolder) throws CmsException {
        Vector folders = new Vector();
        CmsFolder folder = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            //  get all subfolders
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_GET_SUBFOLDER");

            stmt.setString(1, parentFolder.getResourceId().toString());
            res = stmt.executeQuery();

            // create new folder objects
            while (res.next()) {
                folder = createCmsFolderFromResultSet(res, false);
                folders.addElement(folder);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception exc) {
            throw new CmsException("getSubFolders " + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return folders;
    }
    
    /**
     * Gets all resources that are marked as undeleted.
     * @param resources Vector of resources
     * @return Returns all resources that are markes as deleted
     */
    public Vector getUndeletedResources(Vector resources) {
        Vector undeletedResources = new Vector();
        for (int i = 0; i < resources.size(); i++) {
            CmsResource res = (CmsResource) resources.elementAt(i);
            if (res.getState() != I_CmsConstants.C_STATE_DELETED) {
                undeletedResources.addElement(res);
            }
        }
        return undeletedResources;
    }

	public void init(Configurations config, String dbPoolUrl, CmsDriverManager driverManager) {
		m_sqlManager = initQueries(dbPoolUrl);
        m_driverManager = driverManager;

        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". VFS driver init      : ok");
        }
	}

    public com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl) {
        return new com.opencms.db.generic.CmsSqlManager(dbPoolUrl);
    }

    /**
     * Reads all file headers of a file in the OpenCms.<BR>
     * The reading excludes the filecontent.
     *
     * @param filename The name of the file to be read.
     * @return Vector of file headers read from the Cms.
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readAllFileHeaders(int projectId, String resourceName) throws CmsException {
        CmsFile file = null;
        ResultSet res = null;
        Vector allHeaders = new Vector();
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ_ALL");
            // read file header data from database
            stmt.setString(1, resourceName);
            res = stmt.executeQuery();
            // create new file headers
            while (res.next()) {
                file = createCmsFileFromResultSet(res, true, false);
                allHeaders.addElement(file);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception exc) {
            throw new CmsException("readAllFileHeaders " + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return allHeaders;
    }

    /**
     * Reads all propertydefinitions for the given resource type.
     *
     * @param resourcetype The resource type to read the propertydefinitions for.
     *
     * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
     * The Vector is maybe empty.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readAllPropertydefinitions(int projectId, I_CmsResourceType resourcetype) throws CmsException {
        return (readAllPropertydefinitions(projectId, resourcetype.getResourceType()));
    }

    /**
     * Reads all propertydefinitions for the given resource type.
     *
     * @param resourcetype The resource type to read the propertydefinitions for.
     *
     * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
     * The Vector is maybe empty.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    // TODO: check if propertydefs have online and offline tables
    public Vector readAllPropertydefinitions(int projectId, int resourcetype) throws CmsException {
        Vector metadefs = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_READALL");
            // create statement
            stmt.setInt(1, resourcetype);
            res = stmt.executeQuery();

            while (res.next()) {
                metadefs.addElement(new CmsPropertydefinition(res.getInt(m_sqlManager.get("C_PROPERTYDEF_ID")), res.getString(m_sqlManager.get("C_PROPERTYDEF_NAME")), res.getInt(m_sqlManager.get("C_PROPERTYDEF_RESOURCE_TYPE"))));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (metadefs);
    }

    /**
     * select a projectResource from an given project and resourcename
     *
     * @param project The project in which the resource is used.
     * @param resource The resource to be read from the Cms.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readBackupProjectResources(int versionId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        Vector projectResources = new Vector();
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ_BACKUP");
            // select resource from the database
            stmt.setInt(1, versionId);
            res = stmt.executeQuery();
            while (res.next()) {
                projectResources.addElement(res.getString("RESOURCE_NAME"));
            }
            res.close();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return projectResources;
    }

    /**
     * Reads a file from the Cms.<BR/>
     *
     * @param projectId The Id of the project in which the resource will be used.
     * @param onlineProjectId The online projectId of the OpenCms.
     * @param filename The complete name of the new file (including pathinformation).
     *
     * @return file The read file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsFile readFile(int projectId, String filename) throws CmsException {
        CmsFile file = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_FILES_READ");

            stmt.setString(1, filename);
            stmt.setInt(2, projectId);
            res = stmt.executeQuery();

            if (res.next()) {
                file = createCmsFileFromResultSet(res, projectId, filename);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
                // check if this resource is marked as deleted
                if (file.getState() == com.opencms.core.I_CmsConstants.C_STATE_DELETED) {
                    throw new CmsException("[" + this.getClass().getName() + "] " + file.getAbsolutePath(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
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
     * Reads a file from the Cms.<BR/>
     *
     * @param projectId The Id of the project in which the resource will be used.
     * @param onlineProjectId The online projectId of the OpenCms.
     * @param filename The complete name of the new file (including pathinformation).
     *
     * @return file The read file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsFile readFile(int projectId, String filename, boolean includeDeleted) throws CmsException {
        CmsFile file = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_FILES_READ");
            stmt.setString(1, filename);
            stmt.setInt(2, projectId);
            res = stmt.executeQuery();
            if (res.next()) {
                file = createCmsFileFromResultSet(res, projectId, filename);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
                // check if this resource is marked as deleted
                if (file.getState() == com.opencms.core.I_CmsConstants.C_STATE_DELETED && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + "] " + file.getAbsolutePath(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
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
     * Private helper method to read the fileContent for publishProject(export).
     *
     * @param fileId the fileId.
     *
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public byte[] readFileContent(int projectId, int fileId) throws CmsException {
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        byte[] returnValue = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_FILE_READ");
            // read fileContent from database
            stmt.setInt(1, fileId);
            res = stmt.executeQuery();
            if (res.next()) {
                returnValue = res.getBytes(m_sqlManager.get("C_FILE_CONTENT"));
            } else {
                throw new CmsException("[" + this.getClass().getName() + "]" + fileId, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return returnValue;
    }

    /**
     * Reads a file header from the Cms.<BR/>
     * The reading excludes the filecontent.
     *
     * @param projectId The Id of the project
     * @param resourceId The Id of the resource.
     *
     * @return file The read file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsFile readFileHeader(int projectId, CmsResource resource) throws CmsException {

        CmsFile file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READBYID");
            // read file data from database
            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();
            // create new file
            if (res.next()) {
                file = createCmsFileFromResultSet(res, true, false);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
                // check if this resource is marked as deleted
                if (file.getState() == com.opencms.core.I_CmsConstants.C_STATE_DELETED) {
                    throw new CmsException("[" + this.getClass().getName() + "] " + file.getAbsolutePath(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + resource.getResourceId(), CmsException.C_NOT_FOUND);
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
     * Reads a file header from the Cms.<BR/>
     * The reading excludes the filecontent.
     *
     * @param projectId The Id of the project
     * @param resourceId The Id of the resource.
     *
     * @return file The read file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsFile readFileHeader(int projectId, CmsUUID resourceId) throws CmsException {

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
                file = createCmsFileFromResultSet(res, true, false);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
                // check if this resource is marked as deleted
                if (file.getState() == com.opencms.core.I_CmsConstants.C_STATE_DELETED) {
                    throw new CmsException("[" + this.getClass().getName() + "] " + file.getAbsolutePath(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + resourceId, CmsException.C_NOT_FOUND);
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
     * Reads a file header from the Cms.<BR/>
     * The reading excludes the filecontent.
     *
     * @param projectId The Id of the project in which the resource will be used.
     * @param filename The complete name of the new file (including pathinformation).
     *
     * @return file The read file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsFile readFileHeader(int projectId, String filename, boolean includeDeleted) throws CmsException {

        CmsFile file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ");
            // read file data from database
            stmt.setString(1, filename);
            stmt.setInt(2, projectId);
            res = stmt.executeQuery();
            // create new file
            if (res.next()) {
                file = createCmsFileFromResultSet(res, true, false);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
                // check if this resource is marked as deleted
                if ((file.getState() == com.opencms.core.I_CmsConstants.C_STATE_DELETED) && !includeDeleted) {
                    throw new CmsException("[" + this.getClass().getName() + "] " + file.getAbsolutePath(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
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
     * Reads a file header from the Cms.<BR/>
     * The reading excludes the filecontent.
     *
     * @param projectId The Id of the project in which the resource will be used.
     * @param filename The complete name of the new file (including pathinformation).
     *
     * @return file The read file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsFile readFileHeaderInProject(int projectId, String filename) throws CmsException {

        CmsFile file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READINPROJECT");
            // read file data from database
            stmt.setString(1, filename);
            stmt.setInt(2, projectId);
            res = stmt.executeQuery();
            // create new file
            if (res.next()) {
                file = createCmsFileFromResultSet(res, true, false);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
                // check if this resource is marked as deleted
                if (file.getState() == com.opencms.core.I_CmsConstants.C_STATE_DELETED) {
                    throw new CmsException("[" + this.getClass().getName() + "] " + file.getAbsolutePath(), CmsException.C_RESOURCE_DELETED);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
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
     * Reads a file in the project from the Cms.<BR/>
     *
     * @param projectId The Id of the project in which the resource will be used.
     * @param onlineProjectId The online projectId of the OpenCms.
     * @param filename The complete name of the new file (including pathinformation).
     *
     * @return file The read file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsFile readFileInProject(int projectId, int onlineProjectId, String filename) throws CmsException {

        CmsFile file = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_FILES_READINPROJECT");
 
            stmt.setString(1, filename);
            stmt.setInt(2, projectId);
            res = stmt.executeQuery();
            if (res.next()) {
                file = createCmsFileFromResultSet(res, projectId, filename);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
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
     * Reads all files from the Cms, that are in one project.<BR/>
     *
     * @param project The project in which the files are.
     *
     * @return A Vecor of files.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readFiles(int projectId) throws CmsException {
        return readFiles(projectId, true, false);
    }
    /**
     * Reads all files from the Cms, that are in one project.<BR/>
     *
     * @param project The project in which the files are.
     *
     * @return A Vecor of files.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readFiles(int projectId, boolean includeUnchanged, boolean onlyProject) throws CmsException {

        Vector files = new Vector();
        CmsFile file;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String onlyChanged = new String();
        String inProject = new String();

        if (projectId != I_CmsConstants.C_PROJECT_ONLINE_ID && onlyProject) {
            inProject = " AND CMS_OFFLINE_RESOURCES.PROJECT_ID = CMS_PROJECTRESOURCES.PROJECT_ID";
        }
        if (!includeUnchanged) {
            onlyChanged = " AND STATE != " + com.opencms.core.I_CmsConstants.C_STATE_UNCHANGED;
        }
        try {
            conn = m_sqlManager.getConnection(projectId);
            //stmt = conn.prepareStatement(m_sqlManager.get(projectId, "C_RESOURCES_READFILESBYPROJECT") + onlyChanged + inProject);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, m_sqlManager.get(projectId, "C_RESOURCES_READFILESBYPROJECT") + onlyChanged + inProject);
            stmt.setInt(1, projectId);
            res = stmt.executeQuery();

            while (res.next()) {
                file = createCmsFileFromResultSet(res, true, true);
                files.addElement(file);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return files;
    }

    /**
     * Reads all files from the Cms, that are of the given type.<BR/>
     *
     * @param projectId A project id for reading online or offline resources
     * @param resourcetype The type of the files.
     *
     * @return A Vector of files.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readFilesByType(int projectId, int resourcetype) throws CmsException {
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
                file = createCmsFileFromResultSet(res, true, true);
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
     * Reads a folder from the Cms.<BR/>
     *
     * @param project The project in which the resource will be used.
     * @param folderid The id of the folder to be read.
     *
     * @return The read folder.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
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
            // create new resource
            if (res.next()) {
                folder = createCmsFolderFromResultSet(res, true);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + folderId, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException exc) {
            // just throw this exception
            throw exc;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return folder;
    }

    /**
     * Reads a folder from the Cms.<BR/>
     *
     * @param project The project in which the resource will be used.
     * @param foldername The name of the folder to be read.
     *
     * @return The read folder.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsFolder readFolder(int projectId, String foldername) throws CmsException {
        CmsFolder folder = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READ");

            stmt.setString(1, foldername);
            stmt.setInt(2, projectId);
            
            res = stmt.executeQuery();

            // create new resource
            if (res.next()) {
                folder = createCmsFolderFromResultSet(res, true);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + foldername, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException exc) {
            // just throw this exception
            throw exc;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return folder;
    }
    /**
     * Reads a folder from the Cms that exists in the project.<BR/>
     *
     * @param project The project in which the resource will be used.
     * @param foldername The name of the folder to be read.
     *
     * @return The read folder.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public CmsFolder readFolderInProject(int projectId, String foldername) throws CmsException {

        CmsFolder folder = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_READINPROJECT");
            stmt.setString(1, foldername);
            stmt.setInt(2, projectId);
            res = stmt.executeQuery();
            // create new resource
            if (res.next()) {
                folder = createCmsFolderFromResultSet(res, true);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + foldername, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (CmsException exc) {
            // just throw this exception
            throw exc;
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return folder;
    }

    /**
     * Reads all folders from the Cms, that are in one project.<BR/>
     *
     * @param project The project in which the folders are.
     *
     * @return A Vecor of folders.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readFolders(int projectId) throws CmsException {
        return readFolders(projectId, true, false);
    }

    /**
     * Reads all folders from the Cms, that are in one project.<BR/>
     *
     * @param project The project in which the folders are.
     *
     * @return A Vecor of folders.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readFolders(int projectId, boolean includeUnchanged, boolean onlyProject) throws CmsException {

        Vector folders = new Vector();
        CmsFolder folder;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        //String usedStatement;
        String onlyChanged = new String();
        String inProject = new String();
        
        if (projectId != I_CmsConstants.C_PROJECT_ONLINE_ID && onlyProject) {
            inProject = " AND CMS_OFFLINE_RESOURCES.PROJECT_ID = CMS_PROJECTRESOURCES.PROJECT_ID";
        }
        
        if (!includeUnchanged) {
            onlyChanged = " AND CMS_OFFLINE_RESOURCES.STATE != " + com.opencms.core.I_CmsConstants.C_STATE_UNCHANGED + " ORDER BY CMS_OFFLINE_RESOURCES.RESOURCE_NAME";
        } else {
            onlyChanged = " ORDER BY CMS_OFFLINE_RESOURCES.RESOURCE_NAME";
        }
        
        try {
            conn = m_sqlManager.getConnection(projectId);
            // read folder data from database
            //stmt = conn.prepareStatement(m_sqlManager.get("C_RESOURCES_READFOLDERSBYPROJECT" + usedStatement) + inProject + onlyChanged);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, m_sqlManager.get(projectId, "C_RESOURCES_READFOLDERSBYPROJECT") + inProject + onlyChanged);
            stmt.setInt(1, projectId);
            res = stmt.executeQuery();
            // create new folder
            while (res.next()) {
                folder = createCmsFolderFromResultSet(res, true);
                folders.addElement(folder);
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
     * select a projectResource from an given project and resourcename
     *
     * @param project The project in which the resource is used.
     * @param resource The resource to be read from the Cms.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public String readProjectResource(int projectId, String resourcename) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        String resName = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ");

            // select resource from the database
            stmt.setInt(1, projectId);
            stmt.setString(2, resourcename);
            res = stmt.executeQuery();

            if (res.next()) {
                resName = res.getString("RESOURCE_NAME");
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + resourcename, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return resName;
    }

    /**
     * Returns a list of all properties of a file or folder.<p>
     *
     * @param resourceId the id of the resource
     * @param resource the resource to read the properties from
     * @param resourceType the type of the resource
     *
     * @return a Map of Strings representing the properties of the resource
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public HashMap readProperties(int projectId, CmsResource resource, int resourceType) throws CmsException {

        HashMap returnValue = new HashMap();
        ResultSet result = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        CmsUUID resourceId = resource.getResourceId();
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_READALL");
            // create project
            stmt.setString(1, resourceId.toString());
            stmt.setInt(2, resourceType);
            result = stmt.executeQuery();
            while (result.next()) {
                returnValue.put(result.getString(m_sqlManager.get("C_PROPERTYDEF_NAME")), result.getString(m_sqlManager.get("C_PROPERTY_VALUE")));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, result);
        }
        return (returnValue);
    }

    /**
     * Returns a property of a file or folder.
     *
     * @param meta The property-name of which the property has to be read.
     * @param resourceId The id of the resource.
     * @param resourceType The Type of the resource.
     *
     * @return property The property as string or null if the property not exists.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public String readProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException {
        ResultSet result = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String returnValue = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_READ");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.setString(2, meta);
            stmt.setInt(3, resourceType);
            result = stmt.executeQuery();

            if (result.next()) {
                returnValue = result.getString(m_sqlManager.get("C_PROPERTY_VALUE"));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, result);
        }
        return returnValue;
    }

    /**
     * Reads a propertydefinition for the given resource type.
     *
     * @param name The name of the propertydefinition to read.
     * @param type The resource type for which the propertydefinition is valid.
     *
     * @return propertydefinition The propertydefinition that corresponds to the overgiven
     * arguments - or null if there is no valid propertydefinition.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition readPropertydefinition(String name, int projectId, I_CmsResourceType type) throws CmsException {
        return (readPropertydefinition(name, projectId, type.getResourceType()));
    }

    /**
     * Reads a propertydefinition for the given resource type.
     *
     * @param name The name of the propertydefinition to read.
     * @param type The resource type for which the propertydefinition is valid.
     *
     * @return propertydefinition The propertydefinition that corresponds to the overgiven
     * arguments - or null if there is no valid propertydefinition.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition readPropertydefinition(String name, int projectId, int type) throws CmsException {
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
                propDef = new CmsPropertydefinition(res.getInt(m_sqlManager.get("C_PROPERTYDEF_ID")), res.getString(m_sqlManager.get("C_PROPERTYDEF_NAME")), res.getInt(m_sqlManager.get("C_PROPERTYDEF_RESOURCE_TYPE")));
            } else {
                res.close();
                res = null;
                // not found!
                throw new CmsException("[" + this.getClass().getName() + "] " + name, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        
        return propDef;
    }

    /**
     * Reads a resource from the Cms.<BR/>
     * A resource is either a file header or a folder.
     *
     * @param project The project in which the resource will be used.
     * @param filename The complete name of the new file (including pathinformation).
     *
     * @return The resource read.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsResource readResource(CmsProject project, String filename) throws CmsException {
        CmsResource file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_READ");

            // read resource data from database
            stmt.setString(1, filename);
            stmt.setInt(2, project.getId());
            res = stmt.executeQuery();

            // create new resource
            if (res.next()) {
                file = createCmsResourceFromResultSet(res, project.getId());
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                res.close();
                res = null;
                throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception exc) {
            throw new CmsException("readResource " + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return file;
    }

    /**
     * Reads all resource from the Cms, that are in one project.<BR/>
     * A resource is either a file header or a folder.
     *
     * @param project The project in which the resource will be used.
     *
     * @return A Vecor of resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readResources(CmsProject project) throws CmsException {

        Vector resources = new Vector();
        CmsResource file;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_READBYPROJECT");
            // read resource data from database
            stmt.setInt(1, project.getId());
            res = stmt.executeQuery();
            // create new resource
            while (res.next()) {
                file = createCmsResourceFromResultSet(res, project.getId());
                resources.addElement(file);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw new CmsException("[" + this.getClass().getName() + "]", ex);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return resources;
    }

    /**
     * Reads all resources that contains the given string in the resourcename
     * and exists in the current project.<BR/>
     * A resource is either a file header or a folder.
     *
     * @param project The project in which the resource will be used.
     * @param resourcename A part of the resourcename
     *
     * @return A Vecor of resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readResourcesLikeName(CmsProject project, String resourcename) throws CmsException {

        Vector resources = new Vector();
        CmsResource file;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String usedStatement = "";
        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            usedStatement = "_ONLINE";
        } else {
            usedStatement = "";
        }
        try {
            conn = m_sqlManager.getConnection(project);
            // read resource data from database
            //stmt = conn.prepareStatement(m_sqlManager.get("C_RESOURCES_READ_LIKENAME_1" + usedStatement) + resourcename + m_sqlManager.get("C_RESOURCES_READ_LIKENAME_2" + usedStatement));
            stmt = m_sqlManager.getPreparedStatementForSql(conn, m_sqlManager.get("C_RESOURCES_READ_LIKENAME_1" + usedStatement) + resourcename + m_sqlManager.get("C_RESOURCES_READ_LIKENAME_2" + usedStatement));
            stmt.setInt(1, project.getId());
            res = stmt.executeQuery();
            // create new resource
            while (res.next()) {
                file = createCmsResourceFromResultSet(res, project.getId());
                resources.addElement(file);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw new CmsException("[" + this.getClass().getName() + "]", ex);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return resources;
    }
    /**
     * Deletes a file in the database.
     * This method is used to physically remove a file form the database.
     *
     * @param project The project in which the resource will be used.
     * @param filename The complete path of the file.
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void removeFile(int projectId, String filename) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        CmsResource resource = readFileHeader(projectId, filename, true);
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_DELETE");
            // delete the file header
            stmt.setString(1, filename);
            stmt.executeUpdate();
            stmt.close();
            // delete the file content
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_FILE_DELETE");
            stmt.setString(1, resource.getFileId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Deletes a folder in the database.
     * This method is used to physically remove a folder form the database.
     *
     * @param folder The folder.
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void removeFolder(int projectId, CmsFolder folder) throws CmsException {
        // the current implementation only deletes empty folders
        // check if the folder has any files in it
        Vector files = getFilesInFolder(projectId, folder);
        files = getUndeletedResources(files);
        if (files.size() == 0) {
            // check if the folder has any folders in it
            Vector folders = getSubFolders(projectId, folder);
            folders = getUndeletedResources(folders);
            if (folders.size() == 0) {
                //this folder is empty, delete it
                Connection conn = null;
                PreparedStatement stmt = null;
                try {
                    conn = m_sqlManager.getConnection(projectId);
                    stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_ID_DELETE");
                    // delete the folder
                    stmt.setString(1, folder.getResourceId().toString());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
                } finally {
                    m_sqlManager.closeAll(conn, stmt, null);
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + folder.getAbsolutePath(), CmsException.C_NOT_EMPTY);
            }
        } else {
            throw new CmsException("[" + this.getClass().getName() + "] " + folder.getAbsolutePath(), CmsException.C_NOT_EMPTY);
        }
    }

    /**
     * Deletes a folder in the database.
     * This method is used to physically remove a folder form the database.
     * It is internally used by the publish project method.
     *
     * @param project The project in which the resource will be used.
     * @param foldername The complete path of the folder.
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void removeFolderForPublish(int projectId, String foldername) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_DELETE");
            // delete the folder
            stmt.setString(1, foldername);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Removes the temporary files of the given resource
     *
     * @param file The file of which the remporary files should be deleted
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void removeTemporaryFile(CmsFile file) throws CmsException {
        PreparedStatement stmt = null;
        PreparedStatement stmtCont = null;
        PreparedStatement stmtProp = null;
        Connection conn = null;
        ResultSet res = null;

        String tempFilename = file.getRootName() + file.getPath() + com.opencms.core.I_CmsConstants.C_TEMP_PREFIX + file.getName() + "%";
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_GETTEMPFILES");
            // get all temporary files of the resource
            stmt.setString(1, tempFilename);
            res = stmt.executeQuery();
            while (res.next()) {
                int fileId = res.getInt("FILE_ID");
                int resourceId = res.getInt("RESOURCE_ID");
                // delete the properties
                stmtProp = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_DELETEALL");
                stmtProp.setInt(1, resourceId);
                stmtProp.executeQuery();
                stmtProp.close();

                // delete the file content
                stmtCont = m_sqlManager.getPreparedStatement(conn, "C_FILE_DELETE");
                stmtCont.setInt(1, fileId);
                stmtCont.executeUpdate();
                stmtCont.close();
            }
            res.close();
            stmt.close();

            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_DELETETEMPFILES");
            stmt.setString(1, tempFilename);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
            m_sqlManager.closeAll(null, stmtProp, null);
            m_sqlManager.closeAll(null, stmtCont, null);
        }
    }

    /**
     * Renames the file to the new name.
     *
     * @param project The prect in which the resource will be used.
     * @param onlineProject The online project of the OpenCms.
     * @param userId The user id
     * @param oldfileID The id of the resource which will be renamed.
     * @param newname The new name of the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void renameFile(CmsProject project, CmsProject onlineProject, int userId, int oldfileID, String newname) throws CmsException {
        // CHECK: usage of this method, seems to be never used!
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_RENAMERESOURCE");
            stmt.setString(1, newname);
            stmt.setInt(2, userId);
            stmt.setInt(3, oldfileID);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Undeletes the file.
     *
     * @param project The project in which the resource will be used.
     * @param filename The complete path of the file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void undeleteFile(CmsProject project, String filename) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_REMOVE");
            // mark the file as deleted
            stmt.setInt(1, com.opencms.core.I_CmsConstants.C_STATE_CHANGED);
            stmt.setString(2, CmsUUID.getNullUUID().toString());
            stmt.setString(3, filename);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Update the resources flag attribute of all resources.
     * 
     * @param theProject the resources in this project are updated
     * @param theValue the new int value of the resource fags attribute
     * @return the count of affected rows
     */
    public int updateAllResourceFlags(CmsProject theProject, int theValue) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        int rowCount = 0;
        try {
            // execute the query
            conn = m_sqlManager.getConnection(theProject);
            stmt = m_sqlManager.getPreparedStatement(conn, theProject, "C_UPDATE_ALL_RESOURCE_FLAGS");
            stmt.setInt(1, theValue);
            rowCount = stmt.executeUpdate();
        } catch (SQLException e) {
            rowCount = 0;
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return rowCount;
    }
    /**
     * Updates the LOCKED_BY state of a Resource.
     * Creation date: (29.08.00 15:01:55)
     * @param res com.opencms.file.CmsResource
     * @throws com.opencms.core.CmsException The exception description.
     */
    public void updateLockstate(CmsResource res, int projectId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_RESOURCES_UPDATE_LOCK");
            stmt.setString(1, res.isLockedBy().toString());
            stmt.setInt(2, projectId);
            stmt.setString(3, res.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Update the resource flag attribute for a given resource.
     * 
     * @param theProject the CmsProject where the resource is updated
     * @param theResourceID the ID of the resource which is updated
     * @param theValue the new value of the resource flag attribute
     * @return the count of affected rows (should be 1, unless an error occurred)
     */
    public int updateResourceFlags(CmsProject theProject, int theResourceID, int theValue) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        int rowCount = 0;
        try {
            // execute the query
            conn = m_sqlManager.getConnection(theProject);
            stmt = m_sqlManager.getPreparedStatement(conn, theProject, "C_UPDATE_RESOURCE_FLAGS");
            stmt.setInt(1, theValue);
            stmt.setInt(2, theResourceID);
            rowCount = stmt.executeUpdate();
        } catch (SQLException e) {
            rowCount = 0;
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return rowCount;
    }
    /**
     * Updates the state of a Resource.
     *
     * @param res com.opencms.file.CmsResource
     * @throws com.opencms.core.CmsException The exception description.
     */
    public void updateResourcestate(CmsResource res) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(res.getProjectId());
            stmt = m_sqlManager.getPreparedStatement(conn, res.getProjectId(), "C_RESOURCES_UPDATE_STATE");
            stmt.setInt(1, res.getState());
            stmt.setString(2, res.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Writes a file to the Cms.<BR/>
     *
     * @param project The project in which the resource will be used.
     * @param onlineProject The online project of the OpenCms.
     * @param file The new file.
     * @param changed Flag indicating if the file state must be set to changed.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void writeFile(CmsProject project, CmsProject onlineProject, CmsFile file, boolean changed) throws CmsException {
        writeFile(project, onlineProject, file, changed, file.getResourceLastModifiedBy());
    }

    /**
     * Writes a file to the Cms.<BR/>
     *
     * @param project The project in which the resource will be used.
     * @param onlineProject The online project of the OpenCms.
     * @param file The new file.
     * @param changed Flag indicating if the file state must be set to changed.
     * @param userId The id of the user who has changed the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void writeFile(CmsProject project, CmsProject onlineProject, CmsFile file, boolean changed, CmsUUID userId) throws CmsException {
        writeFileHeader(project, file, changed, userId);
        writeFileContent(file.getFileId(), file.getContents(), project.getId(), false);
    }

    /**
    * Writes the file content of an existing file
    *
    * @param fileId The ID of the file to update
    * @param fileContent The new content of the file
    * @param usedPool The name of the database pool to use
    * @param usedStatement Specifies which tables must be used: offline, online or backup
    */
    public void writeFileContent(CmsUUID fileId, byte[] fileContent, int projectId, boolean writeBackup) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            if (writeBackup) {
                conn = m_sqlManager.getConnectionForBackup();
                stmt = m_sqlManager.getPreparedStatement(conn, "C_FILES_UPDATE_BACKUP");
            }
            else {
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
    * Writes the fileheader to the Cms.
    *
    * @param project The project in which the resource will be used.
    * @param onlineProject The online project of the OpenCms.
    * @param file The new file.
    * @param changed Flag indicating if the file state must be set to changed.
    *
    * @throws CmsException Throws CmsException if operation was not succesful.
    */
    public void writeFileHeader(CmsProject project, CmsFile file, boolean changed) throws CmsException {
        writeFileHeader(project, file, changed, file.getResourceLastModifiedBy());
    }

    /**
    * Writes the fileheader to the Cms.
    *
    * @param project The project in which the resource will be used.
    * @param onlineProject The online project of the OpenCms.
    * @param file The new file.
    * @param changed Flag indicating if the file state must be set to changed.
    * @param userId The id of the user who has changed the resource.
    *
    * @throws CmsException Throws CmsException if operation was not succesful.
    */
    public void writeFileHeader(CmsProject project, CmsFile file, boolean changed, CmsUUID userId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        CmsUUID modifiedByUserId = userId;
        long dateModified = file.isTouched() ? file.getDateLastModified() : System.currentTimeMillis();

        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE");
            // update resource in the database
            stmt.setInt(1, file.getType());
            stmt.setInt(2, file.getFlags());
            stmt.setString(3, file.getOwnerId().toString());
            stmt.setString(4, file.getGroupId().toString());
            stmt.setInt(5, file.getProjectId());
            stmt.setInt(6, file.getAccessFlags());
            //STATE
            int state = file.getState();
            if ((state == com.opencms.core.I_CmsConstants.C_STATE_NEW) || (state == com.opencms.core.I_CmsConstants.C_STATE_CHANGED)) {
                stmt.setInt(7, state);
            } else {
                if (changed == true) {
                    stmt.setInt(7, com.opencms.core.I_CmsConstants.C_STATE_CHANGED);
                } else {
                    stmt.setInt(7, file.getState());
                }
            }
            stmt.setString(8, file.isLockedBy().toString());
            stmt.setInt(9, file.getLauncherType());
            stmt.setString(10, file.getLauncherClassname());
            stmt.setTimestamp(11, new Timestamp(dateModified));
            stmt.setString(12, modifiedByUserId.toString());
            stmt.setInt(13, file.getLength());
            stmt.setString(14, file.getFileId().toString());
            stmt.setString(15, file.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
    * Writes a folder to the Cms.<BR/>
    *
    * @param project The project in which the resource will be used.
    * @param folder The folder to be written.
    * @param changed Flag indicating if the file state must be set to changed.
    *
    * @throws CmsException Throws CmsException if operation was not succesful.
    */
    public void writeFolder(CmsProject project, CmsFolder folder, boolean changed) throws CmsException {
        writeFolder(project, folder, changed, folder.getResourceLastModifiedBy());
    }

    /**
    * Writes a folder to the Cms.<BR/>
    *
    * @param project The project in which the resource will be used.
    * @param folder The folder to be written.
    * @param changed Flag indicating if the file state must be set to changed.
    * @param userId The user who has changed the resource
    *
    * @throws CmsException Throws CmsException if operation was not succesful.
    */
    public void writeFolder(CmsProject project, CmsFolder folder, boolean changed, CmsUUID userId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        CmsUUID modifiedByUserId = userId;
        long dateModified = folder.isTouched() ? folder.getDateLastModified() : System.currentTimeMillis();
        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE");
            // update resource in the database
            stmt.setInt(1, folder.getType());
            stmt.setInt(2, folder.getFlags());
            stmt.setString(3, folder.getOwnerId().toString());
            stmt.setString(4, folder.getGroupId().toString());
            stmt.setInt(5, folder.getProjectId());
            stmt.setInt(6, folder.getAccessFlags());
            int state = folder.getState();
            if ((state == com.opencms.core.I_CmsConstants.C_STATE_NEW) || (state == com.opencms.core.I_CmsConstants.C_STATE_CHANGED)) {
                stmt.setInt(7, state);
            } else {
                if (changed == true) {
                    stmt.setInt(7, com.opencms.core.I_CmsConstants.C_STATE_CHANGED);
                } else {
                    stmt.setInt(7, folder.getState());
                }
            }
            stmt.setString(8, folder.isLockedBy().toString());
            stmt.setInt(9, folder.getLauncherType());
            stmt.setString(10, folder.getLauncherClassname());
            stmt.setTimestamp(11, new Timestamp(dateModified));
            stmt.setString(12, modifiedByUserId.toString());
            stmt.setInt(13, 0);
            stmt.setString(14, CmsUUID.getNullUUID().toString());
            stmt.setString(15, folder.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Writes a couple of Properties for a file or folder.
     *
     * @param propertyinfos A Hashtable with propertydefinition- property-pairs as strings.
     * @param projectId The id of the current project.
     * @param resource The CmsResource object of the resource that gets the properties.
     * @param resourceType The Type of the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void writeProperties(Map propertyinfos, int projectId, CmsResource resource, int resourceType) throws CmsException {
        this.writeProperties(propertyinfos, projectId, resource, resourceType, false);
    }

    /**
     * Writes a couple of Properties for a file or folder.
     *
     * @param propertyinfos A Hashtable with propertydefinition- property-pairs as strings.
     * @param projectId The id of the current project.
     * @param resource The CmsResource object of the resource that gets the properties.
     * @param resourceType The Type of the resource.
     * @param addDefinition If <code>true</code> then the propertydefinition is added if it not exists
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void writeProperties(Map propertyinfos, int projectId, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException {

        // get all metadefs
        Iterator keys = propertyinfos.keySet().iterator();
        // one metainfo-name:
        String key;

        while (keys.hasNext()) {
            key = (String) keys.next();
            writeProperty(key, projectId, (String) propertyinfos.get(key), resource, resourceType, addDefinition);
        }
    }

    /**
     * Writes a property for a file or folder.
     *
     * @param meta The property-name of which the property has to be read.
     * @param value The value for the property to be set.
     * @param resourceId The id of the resource.
     * @param resourceType The Type of the resource.
     * @param addDefinition If <code>true</code> then the propertydefinition is added if it not exists
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void writeProperty(String meta, int projectId, String value, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException {
        CmsPropertydefinition propdef = null;
        try {
            propdef = readPropertydefinition(meta, 0, resourceType);
        } catch (CmsException ex) {
            // do nothing
        }
        if (propdef == null) {
            // there is no propertydefinition for with the overgiven name for the resource
            // add this definition or throw an exception
            if (addDefinition) {
                createPropertydefinition(meta, projectId, resourceType);
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
                    stmt.setInt(3, propdef.getId());
                    stmt.executeUpdate();
                } else {
                    // property dosen't exist - use create.
                    // create statement
                    stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_PROPERTIES_CREATE");
                    stmt.setInt(1, m_sqlManager.nextId(m_sqlManager.get(projectId, "C_TABLE_PROPERTIES")));
                    stmt.setInt(2, propdef.getId());
                    stmt.setString(3, resource.getResourceId().toString());
                    stmt.setString(4, m_sqlManager.validateNull(value));
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
     * Updates the name of the propertydefinition for the resource type.<BR/>
     *
     * Only the admin can do this.
     *
     * @param metadef The propertydef to be written.
     * @return The propertydefinition, that was written.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsPropertydefinition writePropertydefinition(CmsPropertydefinition metadef) throws CmsException {
        PreparedStatement stmt = null;
        CmsPropertydefinition returnValue = null;
        Connection conn = null;
        
        try {
            for (int i = 0; i < 3; i++) {
                // write the propertydef in the offline db
                if (i == 0) {
                    conn = m_sqlManager.getConnection();
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_UPDATE");
                }
                // write the propertydef in the online db
                else if (i == 1) {
                    conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_UPDATE_ONLINE");
                }
                // write the propertydef in the backup db
                else {
                    conn = m_sqlManager.getConnectionForBackup();
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_UPDATE_BACKUP");
                }
                stmt.setString(1, metadef.getName());
                stmt.setInt(2, metadef.getId());
                stmt.executeUpdate();
                stmt.close();
                conn.close();
            }
            // read the propertydefinition
            returnValue = readPropertydefinition(metadef.getName(), 0, metadef.getType());
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
        
        return returnValue;
    }

    /**
    * Writes a folder to the Cms.<BR/>
    *
    * @param project The project in which the resource will be used.
    * @param folder The folder to be written.
    * @param changed Flag indicating if the file state must be set to changed.
    * @param userId The user who has changed the resource
    *
    * @throws CmsException Throws CmsException if operation was not succesful.
    */
    public void writeResource(CmsProject project, CmsResource resource, byte[] filecontent, boolean isChanged, CmsUUID userId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        CmsUUID modifiedByUserId = userId;
        long dateModified = resource.isTouched() ? resource.getDateLastModified() : System.currentTimeMillis();
        boolean isFolder = false;

        if (resource.getType() == I_CmsConstants.C_TYPE_FOLDER) {
            isFolder = true;
        }
        if (filecontent == null) {
            filecontent = new byte[0];
        }
        if (project.getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            modifiedByUserId = resource.getResourceLastModifiedBy();
        }
        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UPDATE");
            // update resource in the database
            stmt.setInt(1, resource.getType());
            stmt.setInt(2, resource.getFlags());
            stmt.setString(3, resource.getOwnerId().toString());
            stmt.setString(4, resource.getGroupId().toString());
            stmt.setInt(5, resource.getProjectId());
            stmt.setInt(6, resource.getAccessFlags());
            int state = resource.getState();
            if ((state == I_CmsConstants.C_STATE_NEW) || (state == I_CmsConstants.C_STATE_CHANGED)) {
                stmt.setInt(7, state);
            } else {
                if (isChanged == true) {
                    stmt.setInt(7, I_CmsConstants.C_STATE_CHANGED);
                } else {
                    stmt.setInt(7, resource.getState());
                }
            }
            stmt.setString(8, resource.isLockedBy().toString());
            stmt.setInt(9, resource.getLauncherType());
            stmt.setString(10, resource.getLauncherClassname());
            stmt.setTimestamp(11, new Timestamp(dateModified));
            stmt.setString(12, modifiedByUserId.toString());
            stmt.setInt(13, filecontent.length);
            stmt.setString(14, resource.getFileId().toString());
            stmt.setString(15, resource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        // write the filecontent if this is a file
        if (!isFolder) {
            this.writeFileContent(resource.getFileId(), filecontent, project.getId(), false);
        }
    }
}

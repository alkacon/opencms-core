/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsBackupDriver.java,v $
 * Date   : $Date: 2005/05/13 14:05:17 $
 * Version: $Revision: 1.128 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.db.CmsConsistencyException;
import org.opencms.db.CmsDataAccessException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsObjectNotFoundException;
import org.opencms.db.CmsSqlException;
import org.opencms.db.I_CmsBackupDriver;
import org.opencms.db.I_CmsDriver;
import org.opencms.file.CmsBackupProject;
import org.opencms.file.CmsBackupResource;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;


/**
 * Generic (ANSI-SQL) database server implementation of the backup driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com) 
 * @version $Revision: 1.128 $ $Date: 2005/05/13 14:05:17 $
 * @since 5.1
 */
public class CmsBackupDriver implements I_CmsDriver, I_CmsBackupDriver {
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.generic.CmsBackupDriver.class);

    /** The driver manager instance. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager instance. */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsBackupDriver#createBackupPropertyDefinition(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public CmsPropertyDefinition createBackupPropertyDefinition(CmsDbContext dbc, String name) throws CmsDataAccessException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_CREATE_BACKUP");
            stmt.setString(1, new CmsUUID().toString());
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        return readBackupPropertyDefinition(dbc, name);
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#createBackupResource(java.sql.ResultSet, boolean)
     */
    public CmsBackupResource createBackupResource(ResultSet res, boolean hasContent) throws SQLException {
        byte[] content = null;

        CmsUUID backupId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_BACKUP_ID")));
        int versionId = res.getInt(m_sqlManager.readQuery("C_RESOURCES_VERSION_ID"));
        int tagId = res.getInt(m_sqlManager.readQuery("C_RESOURCES_PUBLISH_TAG"));
        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_ID")));
        String resourcePath = res.getString(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_PATH"));
        int resourceType = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_TYPE"));
        int resourceFlags = res.getInt(m_sqlManager.readQuery("C_RESOURCES_RESOURCE_FLAGS"));
        int projectLastModified = res.getInt(m_sqlManager.readQuery("C_RESOURCES_PROJECT_LASTMODIFIED")); 
        int state = res.getInt(m_sqlManager.readQuery("C_RESOURCES_STATE"));
        long dateCreated = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_CREATED"));
        long dateLastModified = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_LASTMODIFIED"));
        long dateReleased = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_RELEASED"));
        long dateExpired = res.getLong(m_sqlManager.readQuery("C_RESOURCES_DATE_EXPIRED"));           
        int resourceSize = res.getInt(m_sqlManager.readQuery("C_RESOURCES_SIZE"));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_LASTMODIFIED")));
        String userLastModifiedName = res.getString(m_sqlManager.readQuery("C_RESOURCES_LASTMODIFIED_BY_NAME"));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED")));
        String userCreatedName = res.getString(m_sqlManager.readQuery("C_RESOURCES_USER_CREATED_NAME"));

        CmsUUID contentId;
        if (hasContent) {
            content = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
            contentId = new CmsUUID(res.getString(m_sqlManager.readQuery("C_RESOURCES_CONTENT_ID")));
        } else {
            content = new byte[0];
            contentId = CmsUUID.getNullUUID();
        }
        return new CmsBackupResource(
            backupId, 
            tagId, 
            versionId, 
            structureId, 
            resourceId, 
            contentId, 
            resourcePath, 
            resourceType, 
            resourceFlags,
            projectLastModified, 
            state, 
            dateCreated,
            userCreated, 
            userCreatedName, 
            dateLastModified, 
            userLastModified, 
            userLastModifiedName, 
            dateReleased, 
            dateExpired,
            resourceSize,
            content);
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#deleteBackup(org.opencms.db.CmsDbContext, org.opencms.file.CmsBackupResource, int, int)
     */
    public void deleteBackup(CmsDbContext dbc, CmsBackupResource resource, int tag, int versions) throws CmsDataAccessException {
        ResultSet res = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        PreparedStatement stmt4 = null;
        Connection conn = null;
        List backupIds= new ArrayList();
        
        // first get all backup ids of the entries which should be deleted
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_READ_BACKUPID_FOR_DELETION");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setInt(3, versions);
            stmt.setInt(4, tag);           
            res = stmt.executeQuery();            
            // now collect all backupId's for deletion
            while (res.next()) {
                backupIds.add(res.getString(1));
            }
            // we have all the nescessary information, so we can delete the old backups
            stmt1 = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_DELETE_STRUCTURE_BYBACKUPID");
            stmt2 = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_DELETE_RESOURCES_BYBACKUPID");
            stmt3 = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_DELETE_CONTENTS_BYBACKUPID");
            stmt4 = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_DELETE_PROPERTIES_BYBACKUPID");
            Iterator i=backupIds.iterator();
            while (i.hasNext()) {
                String backupId=(String)i.next();
                //delete the structure              
                stmt1.setString(1, backupId);
                stmt1.addBatch();
                //delete the resource
                stmt2.setString(1, backupId);
                stmt2.addBatch();
                //delete the file
                stmt3.setString(1, backupId);
                stmt3.addBatch();
                //delete the properties
                stmt4.setString(1, backupId);
                stmt4.addBatch(); 
            }
            // excecute them
            stmt1.executeBatch();
            stmt2.executeBatch();
            stmt3.executeBatch();
            stmt4.executeBatch();
                                         
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
            m_sqlManager.closeAll(dbc, null, stmt1, null);
            m_sqlManager.closeAll(dbc, null, stmt2, null);
            m_sqlManager.closeAll(dbc, null, stmt3, null);
            m_sqlManager.closeAll(dbc, null, stmt4, null);
        }  
    }
    
    /**
     * @see org.opencms.db.I_CmsBackupDriver#deleteBackupPropertyDefinition(org.opencms.db.CmsDbContext, org.opencms.file.CmsPropertyDefinition)
     */
    public void deleteBackupPropertyDefinition(CmsDbContext dbc, CmsPropertyDefinition metadef) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            if (internalCountProperties(dbc, metadef, I_CmsConstants.C_PROJECT_ONLINE_ID) != 0
                || internalCountProperties(dbc, metadef, Integer.MAX_VALUE) != 0) {

                CmsMessageContainer message = Messages.get().container(Messages.ERR_ERROR_DELETING_PROPERTYDEF_1, metadef.getName());
                throw new CmsConsistencyException(message);
            }

            // delete the backup propertydef
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_DELETE_BACKUP");
            stmt.setString(1, metadef.getId().toString());
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }   

    /**
     * @see org.opencms.db.I_CmsBackupDriver#deleteBackups(org.opencms.db.CmsDbContext, java.util.List, int)
     */
    public void deleteBackups(CmsDbContext dbc, List existingBackups, int maxVersions) throws CmsDataAccessException {
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        PreparedStatement stmt3 = null;
        PreparedStatement stmt4 = null;
        Connection conn = null;
        CmsBackupResource currentResource = null;
        int count = existingBackups.size() - maxVersions;

        try {
            conn = m_sqlManager.getConnection(dbc);  
            stmt1 = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_DELETE_CONTENTS_BYBACKUPID");
            stmt2 = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_DELETE_STRUCTURE_BYBACKUPID");
            stmt3 = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_DELETE_RESOURCES_BYBACKUPID");
            stmt4 = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_DELETEALL_BACKUP");

            for (int i = 0; i < count; i++) {
                currentResource = (CmsBackupResource)existingBackups.get(i);
                // delete the resource
                stmt1.setString(1, currentResource.getBackupId().toString());
                stmt1.addBatch();
                stmt2.setString(1, currentResource.getBackupId().toString());
                stmt2.addBatch();
                stmt3.setString(1, currentResource.getBackupId().toString());
                stmt3.addBatch();

                // delete the properties
                stmt4.setString(1, currentResource.getBackupId().toString());
                stmt4.setInt(2, currentResource.getTagId());
                stmt4.setString(3, currentResource.getStructureId().toString());
                stmt4.setInt(4, CmsProperty.C_STRUCTURE_RECORD_MAPPING);
                stmt4.setString(5, currentResource.getResourceId().toString());
                stmt4.setInt(6, CmsProperty.C_RESOURCE_RECORD_MAPPING);
                stmt4.addBatch();
            }

            if (count > 0) {
                stmt1.executeBatch();
                stmt2.executeBatch();
                stmt3.executeBatch();
                stmt4.executeBatch();
            }

        } catch (SQLException e) {
            throw new CmsSqlException(this, null, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt1, null);
            m_sqlManager.closeAll(dbc, null, stmt2, null);
            m_sqlManager.closeAll(dbc, null, stmt3, null);
            m_sqlManager.closeAll(dbc, null, stmt4, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#destroy()
     */
    public void destroy() throws Throwable {
        finalize();

        CmsMessageContainer message = Messages.get().container(Messages.INIT_SHUTDOWN_BACKUP_DRIVER_1, getClass().getName());
        if (LOG.isInfoEnabled()) {
            LOG.info(message);
        }
    }
    
    /**
     * @see org.opencms.db.I_CmsBackupDriver#getSqlManager()
     */    
    public CmsSqlManager getSqlManager() {
        return m_sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(org.opencms.db.CmsDbContext, org.opencms.configuration.CmsConfigurationManager, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(CmsDbContext dbc, CmsConfigurationManager configurationManager, List successiveDrivers, CmsDriverManager driverManager) {
        
        Map configuration = configurationManager.getConfiguration();
        String poolUrl = configuration.get("db.backup.pool").toString();
        String classname = configuration.get("db.backup.sqlmanager").toString();
        m_sqlManager = this.initSqlManager(classname);
        m_sqlManager.init(I_CmsBackupDriver.C_DRIVER_TYPE_ID, poolUrl);

        m_driverManager = driverManager;

        CmsMessageContainer message = Messages.get().container(Messages.INIT_ASSIGNED_POOL_1, poolUrl);
        if (LOG.isInfoEnabled()) {
            LOG.info(message);
        }

        if (successiveDrivers != null && !successiveDrivers.isEmpty()) {
            CmsMessageContainer message1 = Messages.get().container(Messages.INIT_SUCCESSIVE_DRIVERS_UNSUPPORTED_1, getClass().toString());
            if (LOG.isWarnEnabled()) {
                LOG.warn(message1);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#initSqlManager(String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {
        
        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupFile(CmsDbContext, int, String)
     */
    public CmsBackupResource readBackupFile(CmsDbContext dbc, int tagId, String resourcePath) throws CmsDataAccessException {
        CmsBackupResource file = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_FILES_READ_BACKUP");
            stmt.setString(1, resourcePath);
            stmt.setInt(2, tagId);
            res = stmt.executeQuery();
            if (res.next()) {
                file = createBackupResource(res, true);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + "] " + resourcePath.toString());
            }
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return file;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupFileHeader(org.opencms.db.CmsDbContext, int, java.lang.String)
     */
    public CmsBackupResource readBackupFileHeader(CmsDbContext dbc, int tagId, String resourcePath) throws CmsDataAccessException {
        CmsBackupResource file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_BACKUP");
            stmt.setString(1, resourcePath);
            stmt.setInt(2, tagId);
            res = stmt.executeQuery();
            if (res.next()) {
                file = createBackupResource(res, false);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsVfsResourceNotFoundException("[" + this.getClass().getName() + "] " + resourcePath.toString());
            }
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return file;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupFileHeaders(org.opencms.db.CmsDbContext)
     */
    public List readBackupFileHeaders(CmsDbContext dbc) throws CmsDataAccessException {
        CmsBackupResource currentBackupResource = null;
        ResultSet res = null;
        List allHeaders = new ArrayList();
        PreparedStatement stmt = null;
        Connection conn = null;
        Set storage = new HashSet();

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_ALL_BACKUP");
            res = stmt.executeQuery();
            while (res.next()) {
                currentBackupResource = createBackupResource(res, false);
                // only add each structureId x resourceId combination once
                String key = currentBackupResource.getStructureId().toString() + currentBackupResource.getResourceId().toString();                      
                if (!storage.contains(key)) {
                    // no entry found, so add it
                    allHeaders.add(currentBackupResource);
                    storage.add(key);
                }
            }
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
            storage = null;
        }

        return allHeaders;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupFileHeaders(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public List readBackupFileHeaders(CmsDbContext dbc, String resourcePath) throws CmsDataAccessException {
        CmsBackupResource currentBackupResource = null;
        ResultSet res = null;
        List allHeaders = new ArrayList();
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);           
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_ALL_VERSIONS_BACKUP");
            stmt.setString(1, resourcePath);
            res = stmt.executeQuery();
            while (res.next()) {
                currentBackupResource = createBackupResource(res, false);
                allHeaders.add(currentBackupResource);
            }
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return allHeaders;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupMaxVersion(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public int readBackupMaxVersion(CmsDbContext dbc, CmsUUID resourceId) throws CmsDataAccessException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int maxBackupVersion = 0;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_RESOURCE_MAX_BACKUP_VERSION");
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                maxBackupVersion = res.getInt(1);
            } else {
                maxBackupVersion = 0;
            }
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return maxBackupVersion;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupProject(org.opencms.db.CmsDbContext, int)
     */
    public CmsBackupProject readBackupProject(CmsDbContext dbc, int tagId) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        CmsBackupProject project = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READBYVERSION_BACKUP");

            stmt.setInt(1, tagId);
            res = stmt.executeQuery();

            if (res.next()) {
                List projectresources = readBackupProjectResources(dbc, tagId);
                project =
                    new CmsBackupProject(
                        res.getInt("PUBLISH_TAG"),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_ID")),
                        res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_NAME")),
                        res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_DESCRIPTION")),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_TASK_ID")),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_USER_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_GROUP_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_MANAGERGROUP_ID"))),
                        res.getLong(m_sqlManager.readQuery("C_PROJECTS_DATE_CREATED")),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_TYPE")),
                        CmsDbUtil.getTimestamp(res, "PROJECT_PUBLISHDATE"),
                        new CmsUUID(res.getString("PROJECT_PUBLISHED_BY")),
                        res.getString("PROJECT_PUBLISHED_BY_NAME"),
                        res.getString("USER_NAME"),
                        res.getString("GROUP_NAME"),
                        res.getString("MANAGERGROUP_NAME"),
                        projectresources);
            } else {
                CmsMessageContainer message = Messages.get().container(Messages.ERR_NO_BACKUP_PROJECT_WITH_TAG_ID_1, Integer.toString(tagId));
                throw new CmsObjectNotFoundException(message);
            }
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return project;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupProjectResources(org.opencms.db.CmsDbContext, int)
     */
    public List readBackupProjectResources(CmsDbContext dbc, int tagId) throws CmsDataAccessException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        List projectResources = new ArrayList();

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ_BACKUP");
            stmt.setInt(1, tagId);
            res = stmt.executeQuery();
            while (res.next()) {
                projectResources.add(res.getString("RESOURCE_PATH"));
            }
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return projectResources;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupProjects(org.opencms.db.CmsDbContext)
     */
    public List readBackupProjects(CmsDbContext dbc) throws CmsDataAccessException {
        List projects = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READLAST_BACKUP");
            res = stmt.executeQuery();
            int i = 0;
            int max = 300;

            while (res.next() && (i < max)) {
                List resources = readBackupProjectResources(dbc, res.getInt("PUBLISH_TAG"));
                projects.add(
                    new CmsBackupProject(
                        res.getInt("PUBLISH_TAG"),
                        res.getInt("PROJECT_ID"),
                        res.getString("PROJECT_NAME"),
                        res.getString("PROJECT_DESCRIPTION"),
                        res.getInt("TASK_ID"),
                        new CmsUUID(res.getString("USER_ID")),
                        new CmsUUID(res.getString("GROUP_ID")),
                        new CmsUUID(res.getString("MANAGERGROUP_ID")),
                        res.getLong("DATE_CREATED"),
                        res.getInt("PROJECT_TYPE"),
                        CmsDbUtil.getTimestamp(res, "PROJECT_PUBLISHDATE"),
                        new CmsUUID(res.getString("PROJECT_PUBLISHED_BY")),
                        res.getString("PROJECT_PUBLISHED_BY_NAME"),
                        res.getString("USER_NAME"),
                        res.getString("GROUP_NAME"),
                        res.getString("MANAGERGROUP_NAME"),
                        resources));
                i++;
            }
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupProjectTag(org.opencms.db.CmsDbContext, long)
     */
    public int readBackupProjectTag(CmsDbContext dbc, long maxdate) throws CmsDataAccessException {
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        int maxVersion = 0;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_READ_MAXVERSION");
            stmt.setTimestamp(1, new Timestamp(maxdate));
            res = stmt.executeQuery();
            if (res.next()) {
                maxVersion = res.getInt(1);
            }
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return maxVersion;
    }

    /** 
     * @see org.opencms.db.I_CmsBackupDriver#readBackupProperties(org.opencms.db.CmsDbContext, org.opencms.file.CmsBackupResource)
     */
    public List readBackupProperties(CmsDbContext dbc, CmsBackupResource resource) throws CmsDataAccessException {

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String propertyKey = null;
        String propertyValue = null;
        int mappingType = -1;
        Map propertyMap = new HashMap();
        CmsProperty property = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_READALL_BACKUP");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setInt(3, resource.getTagId());
            res = stmt.executeQuery();
            
            while (res.next()) {
                propertyKey = res.getString(1);
                propertyValue = res.getString(2);
                mappingType = res.getInt(3);

                if ((property = (CmsProperty)propertyMap.get(propertyKey)) != null) {
                    // there exists already a property for this key in the result

                    switch (mappingType) {
                        case CmsProperty.C_STRUCTURE_RECORD_MAPPING :
                            // this property value is mapped to a structure record
                            property.setStructureValue(propertyValue);
                            break;
                        case CmsProperty.C_RESOURCE_RECORD_MAPPING :
                            // this property value is mapped to a resource record
                            property.setResourceValue(propertyValue);
                            break;
                        default :
                            CmsMessageContainer message = Messages.get().container(Messages.ERR_UNKNOWN_PROPERTY_VALUE_MAPPING_1, Integer.toString(mappingType));
                            throw new CmsConsistencyException(message);
                    }
                } else {
                    // there doesn't exist a property for this key yet
                    property = new CmsProperty();
                    property.setName(propertyKey);

                    switch (mappingType) {
                        case CmsProperty.C_STRUCTURE_RECORD_MAPPING :
                            // this property value is mapped to a structure record
                            property.setStructureValue(propertyValue);
                            property.setResourceValue(null);
                            break;
                        case CmsProperty.C_RESOURCE_RECORD_MAPPING : 
                            // this property value is mapped to a resource record
                            property.setStructureValue(null);
                            property.setResourceValue(propertyValue);
                            break;
                        default :
                            CmsMessageContainer message = Messages.get().container(Messages.ERR_UNKNOWN_PROPERTY_VALUE_MAPPING_1, Integer.toString(mappingType));
                            throw new CmsConsistencyException(message);
                    }

                    propertyMap.put(propertyKey, property);
                }
            }
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return new ArrayList(propertyMap.values());
    }
    
    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupPropertyDefinition(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public CmsPropertyDefinition readBackupPropertyDefinition(CmsDbContext dbc, String name) throws CmsDataAccessException {
        CmsPropertyDefinition propDef = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_READ_BACKUP");
            stmt.setString(1, name);
            res = stmt.executeQuery();

            if (res.next()) {
                propDef = new CmsPropertyDefinition(new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_ID"))), res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")));
            } else {
                CmsMessageContainer message = Messages.get().container(Messages.ERR_NO_PROPERTYDEF_WITH_NAME_1, name);
                throw new CmsObjectNotFoundException(message);
            }
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return propDef;
    }    

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readMaxTagId(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource)
     */
    public int readMaxTagId(CmsDbContext dbc, CmsResource resource) throws CmsDataAccessException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int result = 0;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_MAX_PUBLISH_TAG");
            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();
            
            if (res.next()) {
                result = res.getInt(1);
            }
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;        
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readNextBackupTagId(org.opencms.db.CmsDbContext)
     */
    public int readNextBackupTagId(CmsDbContext dbc) {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int projectBackupTagId = 1;
        int resourceBackupTagId = 1;

        try {
            // get the max version id
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_BACKUP_MAXTAG");
            res = stmt.executeQuery();
            
            if (res.next()) {
                projectBackupTagId = res.getInt(1) + 1;
            }

            m_sqlManager.closeAll(dbc, null, stmt, res);

            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_BACKUP_MAXTAG_RESOURCE");
            res = stmt.executeQuery();
            
            if (res.next()) {
                resourceBackupTagId = res.getInt(1) + 1;
            }
            
            if (resourceBackupTagId > projectBackupTagId) {
                projectBackupTagId = resourceBackupTagId;
            }
        } catch (SQLException exc) {
            return 1;
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        
        return projectBackupTagId;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupProject(org.opencms.db.CmsDbContext, int, long)
     */
    public void writeBackupProject(CmsDbContext dbc, int tagId, long publishDate) throws CmsDataAccessException {
        
        Connection conn = null;
        PreparedStatement stmt = null;
        String group = null;
        String managerGroup = null;
        
        CmsProject currentProject = dbc.currentProject();
        CmsUser currentUser = dbc.currentUser();
        
        CmsUser owner = m_driverManager.getUserDriver().readUser(dbc, currentProject.getOwnerId());
        
        StringBuffer buf = new StringBuffer();
        buf.append(owner.getName()).append(" ").append(owner.getFirstname()).append(" ").append(owner.getLastname());
        String ownerName = buf.toString();

        try {
            group = m_driverManager.getUserDriver().readGroup(dbc, currentProject.getGroupId()).getName();
        } catch (CmsObjectNotFoundException e) {
            // the group could not be read
            group = "";
        }
        
        try {
            managerGroup = m_driverManager.getUserDriver().readGroup(dbc, currentProject.getManagerGroupId()).getName();
        } catch (CmsObjectNotFoundException e) {
            // the group could not be read
            managerGroup = "";
        }
        
        List projectresources = m_driverManager.getProjectDriver().readProjectResources(dbc, currentProject);
        
        // write backup project to the database
        try {
            conn = m_sqlManager.getConnection(dbc);
            
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_CREATE_BACKUP");
            // first write the project
            stmt.setInt(1, tagId);
            stmt.setInt(2, currentProject.getId());
            stmt.setString(3, currentProject.getName());
            stmt.setTimestamp(4, new Timestamp(publishDate));
            stmt.setString(5, currentUser.getId().toString());
            stmt.setString(6, currentUser.getName() + " " + currentUser.getFirstname() + " " + currentUser.getLastname());
            stmt.setString(7, currentProject.getOwnerId().toString());
            stmt.setString(8, ownerName);
            stmt.setString(9, currentProject.getGroupId().toString());
            stmt.setString(10, group);
            stmt.setString(11, currentProject.getManagerGroupId().toString());
            stmt.setString(12, managerGroup);
            stmt.setString(13, currentProject.getDescription());
            stmt.setLong(14, currentProject.getCreateDate());
            stmt.setInt(15, currentProject.getType());
            stmt.setInt(16, currentProject.getTaskId());
            stmt.executeUpdate();
            
            m_sqlManager.closeAll(dbc, null, stmt, null);

            // now write the projectresources
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_CREATE_BACKUP");
            Iterator i = projectresources.iterator();
            while (i.hasNext()) {
                stmt.setInt(1, tagId);
                stmt.setInt(2, currentProject.getId());
                stmt.setString(3, (String)i.next());
                stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupProperties(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, java.util.List, org.opencms.util.CmsUUID, int, int)
     */
    public void writeBackupProperties(CmsDbContext dbc, CmsResource resource, List properties, CmsUUID backupId, int tagId, int versionId) throws CmsDataAccessException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String propDefName = null;
        CmsProperty property = null;
        int mappingType = -1;
        String value = null;
        CmsUUID id = null;
        CmsPropertyDefinition propDef = null;
        
        try {
            conn = m_sqlManager.getConnection(dbc);
            
            Iterator dummy = properties.iterator();
            while (dummy.hasNext()) {
                property = (CmsProperty) dummy.next();
                propDefName = property.getName();
                propDef = readBackupPropertyDefinition(dbc, propDefName);
                    
                for (int i = 0; i < 2; i++) {
                    mappingType = -1;
                    value = null;
                    id = null;
                    
                    if (i == 0) {
                        // write the structure value on the first cycle
                        value = property.getStructureValue();
                        mappingType = CmsProperty.C_STRUCTURE_RECORD_MAPPING;
                        id = resource.getStructureId();   
                        
                        if (value == null || "".equals(value)) {
                            continue;
                        }
                    } else {
                        // write the resource value on the second cycle
                        value = property.getResourceValue();
                        mappingType = CmsProperty.C_RESOURCE_RECORD_MAPPING;
                        id = resource.getResourceId();

                        if (value == null || "".equals(value)) {
                            break;
                        }
                    }

                    stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_CREATE_BACKUP");
                    
                    stmt.setString(1, backupId.toString());
                    stmt.setString(2, new CmsUUID().toString());
                    stmt.setString(3, propDef.getId().toString());
                    stmt.setString(4, id.toString());
                    stmt.setInt(5, mappingType);
                    stmt.setString(6, m_sqlManager.validateEmpty(value));
                    stmt.setInt(7, tagId);
                    stmt.setInt(8, versionId);
                    
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }                    
            }
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, java.util.List, int, long, int)
     */
    public void writeBackupResource(CmsDbContext dbc, CmsResource resource, List properties, int tagId, long publishDate, int maxVersions) throws CmsDataAccessException {
        
        Connection conn = null;
        PreparedStatement stmt = null;
        CmsUUID backupPkId = new CmsUUID();
        int versionId = -1;

        String lastModifiedName = "";
        String createdName = "";
        try {
            CmsUser lastModified = m_driverManager.getUserDriver().readUser(dbc, resource.getUserLastModified());
            lastModifiedName = lastModified.getName();
            CmsUser created = m_driverManager.getUserDriver().readUser(dbc, resource.getUserCreated());
            createdName = created.getName();
        } catch (CmsDataAccessException e) {
            lastModifiedName = resource.getUserCreated().toString();
            createdName = resource.getUserLastModified().toString();
        }

        try {
            conn = m_sqlManager.getConnection(dbc);
   
            // now get the new version id for this resource
            versionId = internalReadNextVersionId(dbc, resource);

            if (resource.isFile()) {

                if (!this.internalValidateBackupResource(dbc, resource, tagId)) {

                    // write the file content if any
                    internalWriteBackupFileContent(dbc, backupPkId, resource, tagId, versionId);

                    // write the resource
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_WRITE_BACKUP");
                    stmt.setString(1, resource.getResourceId().toString());
                    stmt.setInt(2, resource.getTypeId());
                    stmt.setInt(3, resource.getFlags());
                    stmt.setLong(4, publishDate);
                    stmt.setString(5, resource.getUserCreated().toString());
                    stmt.setLong(6, resource.getDateLastModified());
                    stmt.setString(7, resource.getUserLastModified().toString());
                    stmt.setInt(8, resource.getState());
                    stmt.setInt(9, resource.getLength());
                    stmt.setInt(10, dbc.currentProject().getId());
                    stmt.setInt(11, 1);
                    stmt.setInt(12, tagId);
                    stmt.setInt(13, versionId);
                    stmt.setString(14, backupPkId.toString());
                    stmt.setString(15, createdName);
                    stmt.setString(16, lastModifiedName);
                    stmt.executeUpdate();

                    m_sqlManager.closeAll(dbc, null, stmt, null);
                }
            }

            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_WRITE_BACKUP");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setString(3, resource.getRootPath());
            stmt.setInt(4, resource.getState());
            stmt.setLong(5, resource.getDateReleased());
            stmt.setLong(6, resource.getDateExpired());
            stmt.setInt(7, tagId);
            stmt.setInt(8, versionId);
            stmt.setString(9, backupPkId.toString());
            stmt.executeUpdate();

            writeBackupProperties(dbc, resource, properties, backupPkId, tagId, versionId);

            // now check if there are old backup versions to delete
            List existingBackups = readBackupFileHeaders(dbc, resource.getRootPath());
            if (existingBackups.size() > maxVersions) {
                // delete redundant backups
                deleteBackups(dbc, existingBackups, maxVersions);
            }

        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupResourceContent(org.opencms.db.CmsDbContext, int, org.opencms.file.CmsResource, org.opencms.file.CmsBackupResource)
     */
    public void writeBackupResourceContent(CmsDbContext dbc, int projectId, CmsResource resource, CmsBackupResource backupResource) throws CmsDataAccessException {
            
        if (!this.internalValidateBackupResource(dbc, resource, backupResource.getTagId())) {
            internalWriteBackupFileContent(dbc, backupResource.getBackupId(), resource, backupResource.getTagId(), backupResource.getVersionId());
        }
    }

    /**
     * Releases any allocated resources during garbage collection.<p>
     * 
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
     * @throws CmsDataAccessException if something goes wrong
     */
    protected int internalCountProperties(CmsDbContext dbc, CmsPropertyDefinition metadef, int projectId) throws CmsDataAccessException {
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
                CmsMessageContainer message = Messages.get().container(Messages.ERR_NO_PROPERTIES_FOR_PROPERTYDEF_1, metadef.getName());
                throw new CmsConsistencyException(message);
            }
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return returnValue;
    }    

    /**
     * Internal method to write the backup content.<p>
     * 
     * @param dbc the current database context
     * @param backupId the backup id
     * @param resource the resource to backup
     * @param tagId the tag revision
     * @param versionId the version revision
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalWriteBackupFileContent(CmsDbContext dbc, CmsUUID backupId, CmsResource resource, int tagId, int versionId) throws CmsDataAccessException {
        Connection conn = null;
        PreparedStatement stmt = null;

        CmsUUID contentId;
        byte[] fileContent;
        if (resource instanceof CmsFile) {
            contentId = ((CmsFile)resource).getContentId();
            fileContent = ((CmsFile)resource).getContents();
        } else {
            contentId = CmsUUID.getNullUUID();
            fileContent = new byte[0];
        }
        
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_CONTENTS_WRITE_BACKUP");
            stmt.setString(1, contentId.toString());
            stmt.setString(2, resource.getResourceId().toString());

            if (fileContent.length < 2000) {
                stmt.setBytes(3, fileContent);
            } else {
                stmt.setBinaryStream(3, new ByteArrayInputStream(fileContent), fileContent.length);
            }

            stmt.setInt(4, tagId);
            stmt.setInt(5, versionId);
            stmt.setString(6, backupId.toString());

            stmt.executeUpdate();
            fileContent = null;
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * Gets the next version id for a given backup resource. <p>
     * 
     * @param dbc the current database context
     * @param resource the resource to get the next version from
     * 
     * @return next version id
     */
    private int internalReadNextVersionId(CmsDbContext dbc, CmsResource resource) {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int versionId = 1;

        try {
            // get the max version id
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_BACKUP_MAXVER");
            stmt.setString(1, resource.getRootPath());
            res = stmt.executeQuery();
            if (res.next()) {
                versionId = res.getInt(1) + 1;
            }

            return versionId;
        } catch (SQLException exc) {
            return 1;
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * Tests is a backup resource does exist.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource to test
     * @param tagId the tadId of the resource to test
     * 
     * @return true if the resource already exists, false otherweise
     * @throws CmsDataAccessException if something goes wrong.
     */
    private boolean internalValidateBackupResource(CmsDbContext dbc, CmsResource resource, int tagId) throws CmsDataAccessException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean exists = false;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_EXISTS_RESOURCE");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.setInt(2, tagId);
            res = stmt.executeQuery();

            exists = res.next();

        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return exists;
    }
    
}

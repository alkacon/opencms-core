/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsBackupDriver.java,v $
 * Date   : $Date: 2004/08/27 14:44:59 $
 * Version: $Revision: 1.104 $
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
import org.opencms.db.I_CmsBackupDriver;
import org.opencms.db.I_CmsDriver;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import org.opencms.file.CmsBackupProject;
import org.opencms.file.CmsBackupResource;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertydefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.CmsUser;

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
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Generic (ANSI-SQL) database server implementation of the backup driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com) 
 * @version $Revision: 1.104 $ $Date: 2004/08/27 14:44:59 $
 * @since 5.1
 */
public class CmsBackupDriver extends Object implements I_CmsDriver, I_CmsBackupDriver {

    /** The driver manager instance. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager instance. */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

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
     * @see org.opencms.db.I_CmsBackupDriver#createBackupPropertyDefinition(java.lang.String, int)
     */
    public CmsPropertydefinition createBackupPropertyDefinition(String name, int mappingtype) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // create the backup property definition
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_CREATE_BACKUP");
            stmt.setString(1, new CmsUUID().toString());
            stmt.setString(2, name);
            stmt.setInt(3, mappingtype);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, "PropertyDefinition="+name+"; ResourceType="+mappingtype, CmsException.C_SQL_ERROR, exc, true);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readBackupPropertyDefinition(name, mappingtype);
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#deleteBackups(java.util.List, int)
     */
    public void deleteBackups(List existingBackups, int maxVersions) throws CmsException {
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        Connection conn = null;
        CmsBackupResource currentResource = null;
        int size = existingBackups.size()-1;
        int count = existingBackups.size() - maxVersions;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt1 = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_DELETE_RESOURCE");
            stmt2 = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_DELETEALL_BACKUP");

            for (int i = 0; i < count; i++) {
                currentResource = (CmsBackupResource)existingBackups.get(i);
                // delete the resource
                stmt1.setString(1, currentResource.getStructureId().toString());
                stmt1.setInt(2, currentResource.getTagId());
                stmt1.addBatch();
                // delete the properties
                stmt2.setString(1, currentResource.getBackupId().toString());
                stmt2.setInt(2, currentResource.getTagId());
                stmt2.setString(3, currentResource.getStructureId().toString());
                stmt2.setInt(4, CmsProperty.C_STRUCTURE_RECORD_MAPPING);
                stmt2.setString(5, currentResource.getResourceId().toString());
                stmt2.setInt(6, CmsProperty.C_RESOURCE_RECORD_MAPPING);
                stmt2.addBatch();
            }

            if (count > 0) {
                stmt1.executeBatch();
                stmt2.executeBatch();
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt1, null);
            m_sqlManager.closeAll(conn, stmt2, null);
        }
    }

    
    /**
     * @see org.opencms.db.I_CmsBackupDriver#deleteBackup(org.opencms.file.CmsBackupResource, int, int)
     */
    public void deleteBackup(CmsBackupResource resource, int tag, int versions) throws CmsException {
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
            conn = m_sqlManager.getConnectionForBackup();
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
            m_sqlManager.closeAll(conn, stmt1, null);
            m_sqlManager.closeAll(conn, stmt2, null);
            m_sqlManager.closeAll(conn, stmt3, null);
            m_sqlManager.closeAll(conn, stmt4, null);
        }  
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#destroy()
     */
    public void destroy() throws Throwable {
        finalize();

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Shutting down        : " + this.getClass().getName() + " ... ok!");
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
     * @see org.opencms.db.I_CmsDriver#init(org.apache.commons.collections.ExtendedProperties, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(ExtendedProperties configuration, List successiveDrivers, CmsDriverManager driverManager) {
        String poolUrl = configuration.getString("db.backup.pool");

        m_sqlManager = this.initQueries();
        m_sqlManager.setPoolUrlOffline(poolUrl);
        m_sqlManager.setPoolUrlOnline(poolUrl);
        m_sqlManager.setPoolUrlBackup(poolUrl);

        m_driverManager = driverManager;

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Assigned pool        : " + poolUrl);
        }

        if (successiveDrivers != null && !successiveDrivers.isEmpty()) {
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(this.getClass().toString() + " does not support successive drivers");
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#initQueries()
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.generic.CmsSqlManager();
    }

    /**
     * Gets the next version id for a given backup resource. <p>
     * 
     * @param resource the resource to get the next version from
     * @return next version id
     */
    private int internalReadNextVersionId(CmsResource resource) {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int versionId = 1;

        try {
            // get the max version id
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_BACKUP_MAXVER");
            //stmt.setString(1, resource.getStructureId().toString());
            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();
            if (res.next()) {
                versionId = res.getInt(1) + 1;
            }

            return versionId;
        } catch (SQLException exc) {
            return 1;
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * Tests is a backup resource does exisit.<p>
     * 
     * @param resource the resource to test
     * @param tagId the tadId of the resource to test
     * @return true if the resource already exists, false otherweise
     * @throws CmsException if something goes wrong.
     */
    private boolean internalValidateBackupResource(CmsResource resource, int tagId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        boolean exists = false;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_EXISTS_RESOURCE");
            stmt.setString(1, resource.getResourceId().toString());
            stmt.setInt(2, tagId);
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
    * Internal method to write the backup content.<p>
    * 
    * @param backupId the backup id
    * @param resource the resource to backup
    * @param tagId the tag revision
    * @param versionId the version revision
    * @throws CmsException if something goes wrong
    */
    protected void internalWriteBackupFileContent(CmsUUID backupId, CmsResource resource, int tagId, int versionId) throws CmsException {
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
            conn = m_sqlManager.getConnectionForBackup();
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupFile(int, String)
     */
    public CmsBackupResource readBackupFile(int tagId, String resourcePath) throws CmsException {
        CmsBackupResource file = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnectionForBackup();
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
     * @see org.opencms.db.I_CmsBackupDriver#readBackupFileHeader(int, String)
     */
    public CmsBackupResource readBackupFileHeader(int tagId, String resourcePath) throws CmsException {
        CmsBackupResource file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnectionForBackup();
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
     * @see org.opencms.db.I_CmsBackupDriver#readBackupFileHeaders()
     */
    public List readBackupFileHeaders() throws CmsException {
        CmsBackupResource currentBackupResource = null;
        ResultSet res = null;
        List allHeaders = new ArrayList();
        PreparedStatement stmt = null;
        Connection conn = null;
        Set storage = new HashSet();

        try {
            conn = m_sqlManager.getConnectionForBackup();
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception exc) {
            throw new CmsException("readAllBackupFileHeaders " + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
            storage = null;
        }

        return allHeaders;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupFileHeaders(String)
     */
    public List readBackupFileHeaders(String resourcePath) throws CmsException {
        CmsBackupResource currentBackupResource = null;
        ResultSet res = null;
        List allHeaders = new ArrayList();
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_ALL_VERSIONS_BACKUP");
            stmt.setString(1, resourcePath);
            res = stmt.executeQuery();
            while (res.next()) {
                currentBackupResource = createBackupResource(res, false);
                allHeaders.add(currentBackupResource);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception exc) {
            throw new CmsException("readAllBackupFileHeaders " + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return allHeaders;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupMaxVersion(org.opencms.util.CmsUUID)
     */
    public int readBackupMaxVersion(CmsUUID resourceId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int maxBackupVersion = 0;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_HISTORY_RESOURCE_MAX_BACKUP_VERSION");
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                maxBackupVersion = res.getInt(1);
            } else {
                maxBackupVersion = 0;
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return maxBackupVersion;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupProject(int)
     */
    public CmsBackupProject readBackupProject(int tagId) throws CmsException {

        PreparedStatement stmt = null;
        CmsBackupProject project = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READBYVERSION_BACKUP");

            stmt.setInt(1, tagId);
            res = stmt.executeQuery();

            if (res.next()) {
                Vector projectresources = readBackupProjectResources(tagId);
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
                // project not found!
                throw new CmsException("[" + this.getClass().getName() + "] version " + tagId, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return project;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupProjectResources(int)
     */
    public Vector readBackupProjectResources(int tagId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        Vector projectResources = new Vector();

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ_BACKUP");
            stmt.setInt(1, tagId);
            res = stmt.executeQuery();
            while (res.next()) {
                projectResources.addElement(res.getString("RESOURCE_PATH"));
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return projectResources;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupProjects()
     */
    public Vector readBackupProjects() throws CmsException {
        Vector projects = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READLAST_BACKUP");
            res = stmt.executeQuery();
            int i = 0;
            int max = 300;

            while (res.next() && (i < max)) {
                Vector resources = readBackupProjectResources(res.getInt("PUBLISH_TAG"));
                projects.addElement(
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupProjectTag(long)
     */
    public int readBackupProjectTag(long maxdate) throws CmsException {
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        int maxVersion = 0;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_READ_MAXVERSION");
            stmt.setTimestamp(1, new Timestamp(maxdate));
            res = stmt.executeQuery();
            if (res.next()) {
                maxVersion = res.getInt(1);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return maxVersion;
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupProperties(org.opencms.file.CmsBackupResource)
     */
    public List readBackupProperties(CmsBackupResource resource) throws CmsException {

        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String propertyKey = null;
        String propertyValue = null;
        int mappingType = -1;
        Map propertyMap = new HashMap();
        CmsProperty property = null;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_READALL_BACKUP");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setString(2, resource.getResourceId().toString());
            stmt.setInt(3, I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);
            stmt.setInt(4, resource.getTagId());
            res = stmt.executeQuery();
            
            while (res.next()) {
                propertyKey = res.getString(1);
                propertyValue = res.getString(2);
                mappingType = res.getInt(3);

                if ((property = (CmsProperty)propertyMap.get(propertyKey)) != null) {
                    // there exists already a property for this key in the result

                    if (mappingType == CmsProperty.C_STRUCTURE_RECORD_MAPPING) {
                        // this property value is mapped to a structure record
                        property.setStructureValue(propertyValue);
                    } else if (mappingType == CmsProperty.C_RESOURCE_RECORD_MAPPING) {
                        // this property value is mapped to a resource record
                        property.setResourceValue(propertyValue);
                    } else {
                        throw new CmsException(
                            "Unknown property value mapping type found: " + mappingType,
                            CmsException.C_UNKNOWN_EXCEPTION);
                    }
                } else {
                    // there doesn't exist a property for this key yet
                    property = new CmsProperty();
                    property.setKey(propertyKey);

                    if (mappingType == CmsProperty.C_STRUCTURE_RECORD_MAPPING) {
                        // this property value is mapped to a structure record
                        property.setStructureValue(propertyValue);
                        property.setResourceValue(null);
                    } else if (mappingType == CmsProperty.C_RESOURCE_RECORD_MAPPING) {
                        // this property value is mapped to a resource record
                        property.setStructureValue(null);
                        property.setResourceValue(propertyValue);
                    } else {
                        throw new CmsException(
                            "Unknown property value mapping type found: " + mappingType,
                            CmsException.C_UNKNOWN_EXCEPTION);
                    }

                    propertyMap.put(propertyKey, property);
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
     * @see org.opencms.db.I_CmsBackupDriver#readBackupPropertyDefinition(java.lang.String, int)
     */
    public CmsPropertydefinition readBackupPropertyDefinition(String name, int mappingtype) throws CmsException {
        CmsPropertydefinition propDef = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTYDEF_READ_BACKUP");
            stmt.setString(1, name);
            stmt.setInt(2, mappingtype);
            res = stmt.executeQuery();

            if (res.next()) {
                propDef = new CmsPropertydefinition(new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_ID"))), res.getString(m_sqlManager.readQuery("C_PROPERTYDEF_NAME")), res.getInt(m_sqlManager.readQuery("C_PROPERTYDEF_PROPERTYDEF_MAPPING_TYPE")));
            } else {
                throw new CmsException("[" + this.getClass().getName() + ".readBackupPropertyDefinition] " + name, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return propDef;
    }    

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readNextBackupTagId()
     */
    public int readNextBackupTagId() {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int versionId = 1;
        int resVersionId = 1;

        try {
            // get the max version id
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_BACKUP_MAXTAG");
            res = stmt.executeQuery();
            if (res.next()) {
                versionId = res.getInt(1) + 1;
            }

            m_sqlManager.closeAll(null, stmt, res);

            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_BACKUP_MAXTAG_RESOURCE");
            res = stmt.executeQuery();
            if (res.next()) {
                resVersionId = res.getInt(1) + 1;
            }
            if (resVersionId > versionId) {
                versionId = resVersionId;
            }

            return versionId;
        } catch (SQLException exc) {
            return 1;
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupProject(org.opencms.file.CmsProject, int, long, org.opencms.file.CmsUser)
     */
    public void writeBackupProject(CmsProject currentProject, int tagId, long publishDate, CmsUser currentUser) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String ownerName = new String();
        String group = new String();
        String managerGroup = new String();

        CmsUser owner = m_driverManager.readUser(currentProject.getOwnerId());
        ownerName = owner.getName() + " " + owner.getFirstname() + " " + owner.getLastname();

        try {
            group = m_driverManager.getUserDriver().readGroup(currentProject.getGroupId()).getName();
        } catch (CmsException e) {
            // the group could not be read
            group = "";
        }
        try {
            managerGroup = m_driverManager.getUserDriver().readGroup(currentProject.getManagerGroupId()).getName();
        } catch (CmsException e) {
            // the group could not be read
            managerGroup = "";
        }
        List projectresources = m_driverManager.getProjectDriver().readProjectResources(currentProject);
        // write backup project to the database
        try {
            conn = m_sqlManager.getConnectionForBackup();
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
            stmt.setTimestamp(14, new Timestamp(currentProject.getCreateDate()));
            stmt.setInt(15, currentProject.getType());
            stmt.setInt(16, currentProject.getTaskId());
            stmt.executeUpdate();
            m_sqlManager.closeAll(null, stmt, null);

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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupProperties(org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.List, org.opencms.util.CmsUUID, int, int)
     */
    public void writeBackupProperties(CmsProject publishProject, CmsResource resource, List properties, CmsUUID backupId, int tagId, int versionId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String key = null;
        CmsProperty property = null;
        int mappingType = -1;
        String value = null;
        CmsUUID id = null;
        CmsPropertydefinition propdef = null;
        
        try {
            conn = m_sqlManager.getConnectionForBackup();
            
            Iterator dummy = properties.iterator();
            while (dummy.hasNext()) {
                property = (CmsProperty) dummy.next();
                key = property.getKey();
                propdef = readBackupPropertyDefinition(key, I_CmsConstants.C_PROPERYDEFINITION_RESOURCE);

                if (propdef == null) {
                    throw new CmsException("[" + this.getClass().getName() + "] " + key, CmsException.C_NOT_FOUND);
                } else {                    
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
                        stmt.setString(3, propdef.getId().toString());
                        stmt.setString(4, id.toString());
                        stmt.setInt(5, mappingType);
                        stmt.setString(6, m_sqlManager.validateEmpty(value));
                        stmt.setInt(7, tagId);
                        stmt.setInt(8, versionId);
                        
                        stmt.executeUpdate();
                        m_sqlManager.closeAll(null, stmt, null);
                    }                    
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupResource(org.opencms.file.CmsUser, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.List, int, long, int)
     */
    public void writeBackupResource(CmsUser currentUser, CmsProject publishProject, CmsResource resource, List properties, int tagId, long publishDate, int maxVersions) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        CmsUUID backupPkId = new CmsUUID();
        int versionId;

        String lastModifiedName = "";
        String createdName = "";
        try {
            CmsUser lastModified = m_driverManager.getUserDriver().readUser(resource.getUserLastModified());
            lastModifiedName = lastModified.getName();
            CmsUser created = m_driverManager.getUserDriver().readUser(resource.getUserCreated());
            createdName = created.getName();
        } catch (CmsException e) {
            lastModifiedName = resource.getUserCreated().toString();
            createdName = resource.getUserLastModified().toString();
        }

        try {
            conn = m_sqlManager.getConnectionForBackup();

            // now get the new version id for this resource
            versionId = internalReadNextVersionId(resource);

            if (resource.isFile()) {

                if (!this.internalValidateBackupResource(resource, tagId)) {

                    // write the file content if any
                    internalWriteBackupFileContent(backupPkId, resource, tagId, versionId);

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
                    stmt.setInt(10, publishProject.getId());
                    stmt.setInt(11, 1);
                    stmt.setInt(12, tagId);
                    stmt.setInt(13, versionId);
                    stmt.setString(14, backupPkId.toString());
                    stmt.setString(15, createdName);
                    stmt.setString(16, lastModifiedName);
                    stmt.executeUpdate();

                    m_sqlManager.closeAll(null, stmt, null);
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

            writeBackupProperties(publishProject, resource, properties, backupPkId, tagId, versionId);

            // now check if there are old backup versions to delete
            List existingBackups = readBackupFileHeaders(resource.getRootPath());
            if (existingBackups.size() > maxVersions) {
                // delete redundant backups
                deleteBackups(existingBackups, maxVersions);
            }

        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
        // TODO: use this in later versions
        //return this.readBackupFileHeader(tagId, resource.getResourceId());
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupResourceContent(int, org.opencms.file.CmsResource, org.opencms.file.CmsBackupResource)
     */
    public void writeBackupResourceContent(int projectId, CmsResource resource, CmsBackupResource backupResource) throws CmsException {
            
        if (!this.internalValidateBackupResource(resource, backupResource.getTagId())) {
            // internalWriteBackupFileContent(backupResource.getBackupId(), resource.getFileId(), offlineFile.getContents(), backupResource.getTagId(), backupResource.getVersionId());
            internalWriteBackupFileContent(backupResource.getBackupId(), resource, backupResource.getTagId(), backupResource.getVersionId());
        }
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readMaxTagId(org.opencms.file.CmsResource)
     */
    public int readMaxTagId(CmsResource resource) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int result = 0;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_MAX_PUBLISH_TAG");
            stmt.setString(1, resource.getResourceId().toString());
            res = stmt.executeQuery();
            
            if (res.next()) {
                result = res.getInt(1);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception e) {
            throw new CmsException("readMaxTagId " + e.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return result;        
    }

}

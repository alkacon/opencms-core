/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsBackupDriver.java,v $
 * Date   : $Date: 2003/09/15 10:51:14 $
 * Version: $Revision: 1.45 $
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
import org.opencms.db.CmsDbUtil;
import org.opencms.db.I_CmsBackupDriver;
import org.opencms.db.I_CmsDriver;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.file.CmsBackupProject;
import com.opencms.file.CmsBackupResource;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsPropertydefinition;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;

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

import source.org.apache.java.util.Configurations;

/**
 * Generic (ANSI-SQL) database server implementation of the backup driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.45 $ $Date: 2003/09/15 10:51:14 $
 * @since 5.1
 */
public class CmsBackupDriver extends Object implements I_CmsDriver, I_CmsBackupDriver {

    /** The driver manager instance. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager instance. */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;


    /**
     * @see org.opencms.db.I_CmsBackupDriver#createCmsBackupResourceFromResultSet(java.sql.ResultSet, boolean)
     */
    public CmsBackupResource createCmsBackupResourceFromResultSet(ResultSet res, boolean hasContent) throws SQLException, CmsException {
        byte[] content = null;

        CmsUUID backupId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_BACKUP_ID")));
        int versionId = res.getInt(m_sqlManager.get("C_RESOURCES_VERSION_ID"));
        int tagId = res.getInt(m_sqlManager.get("C_RESOURCES_TAG_ID"));
        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_ID")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_PARENT_ID")));
        String resourceName = res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_NAME"));
        int resourceType = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_TYPE"));
        int resourceFlags = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_FLAGS"));
        int projectID = res.getInt(m_sqlManager.get("C_RESOURCES_PROJECT_ID"));
        CmsUUID fileId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_FILE_ID")));
        int state = res.getInt(m_sqlManager.get("C_RESOURCES_STATE"));
        int loaderId = res.getInt(m_sqlManager.get("C_RESOURCES_LOADER_ID"));
        long dateCreated = CmsDbUtil.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_CREATED")).getTime();
        long dateLastModified = CmsDbUtil.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
        int resourceSize = res.getInt(m_sqlManager.get("C_RESOURCES_SIZE"));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_USER_LASTMODIFIED")));
        String userLastModifiedName = res.getString(m_sqlManager.get("C_RESOURCES_LASTMODIFIED_BY_NAME"));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_USER_CREATED")));
        String userCreatedName = res.getString(m_sqlManager.get("C_RESOURCES_USER_CREATED_NAME"));

        if (hasContent) {
            content = m_sqlManager.getBytes(res, m_sqlManager.get("C_RESOURCES_FILE_CONTENT"));
        } else {
            content = new byte[0];
        }

        return new CmsBackupResource(backupId, tagId, versionId, structureId, resourceId, parentId, fileId,
                                      resourceName, resourceType, resourceFlags, projectID, state,
                                      loaderId, dateCreated, userCreated, userCreatedName, 
                                      dateLastModified, userLastModified, userLastModifiedName, 
                                      resourceSize, content);
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#deleteBackups(long)
     */
    public int deleteBackups(long maxdate) throws CmsException {
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

            m_sqlManager.closeAll(null, stmt, res);

            if (maxVersion > 0) {
                String[] statements = {"C_BACKUP_DELETE_PROJECT_BYVERSION", "C_BACKUP_DELETE_PROJECTRESOURCES_BYVERSION", "C_BACKUP_DELETE_RESOURCES_BYVERSION", "C_BACKUP_DELETE_FILES_BYVERSION", "C_BACKUP_DELETE_PROPERTIES_BYVERSION", "C_BACKUP_DELETE_MODULEMASTER_BYVERSION", "C_BACKUP_DELETE_MODULEMEDIA_BYVERSION" };

                for (int i = 0; i < statements.length; i++) {
                    stmt = m_sqlManager.getPreparedStatement(conn, statements[i]);
                    stmt.setInt(1, maxVersion);
                    stmt.executeUpdate();
                    m_sqlManager.closeAll(null, stmt, null);
                }
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
     * @see org.opencms.db.I_CmsBackupDriver#destroy()
     */
    public void destroy() throws Throwable {
        finalize();

        if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[" + this.getClass().getName() + "] destroyed!");
        }
    }

    /**
     * Releases any allocated resources during garbage collection.<p>
     * 
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
     * @see org.opencms.db.I_CmsBackupDriver#getAllBackupProjects()
     */
    public Vector getAllBackupProjects() throws CmsException {
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
                Vector resources = readBackupProjectResources(res.getInt("TAG_ID"));
                projects.addElement(
                    new CmsBackupProject(
                        res.getInt("TAG_ID"),
                        res.getInt("PROJECT_ID"),
                        res.getString("PROJECT_NAME"),
                        res.getString("PROJECT_DESCRIPTION"),
                        res.getInt("TASK_ID"),
                        new CmsUUID(res.getString("USER_ID")),
                        new CmsUUID(res.getString("GROUP_ID")),
                        new CmsUUID(res.getString("MANAGERGROUP_ID")),
                        CmsDbUtil.getTimestamp(res, "PROJECT_CREATEDATE"),
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
     * @see org.opencms.db.I_CmsBackupDriver#getBackupProjectTag(long)
     */
    public int getBackupProjectTag(long maxdate) throws CmsException {
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
     * @see org.opencms.db.I_CmsDriver#init(source.org.apache.java.util.Configurations, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(Configurations config, List successiveDrivers, CmsDriverManager driverManager) {
        String poolUrl = config.getString("db.backup.pool");

        m_sqlManager = this.initQueries();
        m_sqlManager.setOfflinePoolUrl(poolUrl);
        m_sqlManager.setOnlinePoolUrl(poolUrl);
        m_sqlManager.setBackupPoolUrl(poolUrl);

        m_driverManager = driverManager;

       if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
            OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Assigned pool        : " + poolUrl);
       }

        if (successiveDrivers != null && !successiveDrivers.isEmpty()) {
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, this.getClass().toString() + " does not support successive drivers.");
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#initQueries(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.generic.CmsSqlManager();
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#nextBackupVersionId()
     */
    public int nextBackupTagId() {
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
     * Gets the next version id for a given backup resource. <p>
     * 
     * @param resource the resource to get the next version from
     * @return next version id
     */
    private int nextVersionId(CmsResource resource) {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int versionId = 1;

        try {
            // get the max version id
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_BACKUP_MAXVER");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setString(2, resource.getResourceId().toString());
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
     * @see org.opencms.db.I_CmsBackupDriver#readAllBackupFileHeaders(com.opencms.flex.util.CmsUUID)
     */
    public List readAllBackupFileHeaders(CmsUUID structureId) throws CmsException {
        CmsBackupResource currentBackupResource = null;
        ResultSet res = null;
        List allHeaders = (List)new ArrayList();
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_ALL_VERSIONS_BACKUP");
            stmt.setString(1, structureId.toString());
            res = stmt.executeQuery();
            while (res.next()) {
                currentBackupResource = createCmsBackupResourceFromResultSet(res, false);
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
     * @see org.opencms.db.I_CmsBackupDriver#readAllBackupFileHeaders()
     */
    public List readAllBackupFileHeaders() throws CmsException {
        CmsBackupResource currentBackupResource = null;
        ResultSet res = null;
        List allHeaders = (List)new ArrayList();
        PreparedStatement stmt = null;
        Connection conn = null;
        Set storage=new HashSet();
        
        
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_ALL_BACKUP");
            res = stmt.executeQuery();
            while (res.next()) {
                currentBackupResource = createCmsBackupResourceFromResultSet(res, false);
                // only add each structureIdxresourceId combination once
                String key=currentBackupResource.getStructureId().toString()+currentBackupResource.getResourceId().toString();
                if (!storage.contains(key)) {                                
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
            storage=null;
        }

        return allHeaders;
    }
    

    /**
     * @see org.opencms.db.I_CmsBackupDriver#readBackupFile(int, com.opencms.flex.util.CmsUUID)
     */
    public CmsBackupResource readBackupFile(int versionId, CmsUUID resourceId) throws CmsException {
        CmsBackupResource file = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_FILES_READ_BACKUP");
            stmt.setString(1, resourceId.toString());
            stmt.setInt(2, versionId);
            res = stmt.executeQuery();
            if (res.next()) {
                file = createCmsBackupResourceFromResultSet(res, true);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + resourceId.toString(), CmsException.C_NOT_FOUND);
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
     * @see org.opencms.db.I_CmsBackupDriver#readBackupFileHeader(int, com.opencms.flex.util.CmsUUID)
     */
    public CmsBackupResource readBackupFileHeader(int tagId, CmsUUID resourceId) throws CmsException {
        CmsBackupResource file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_BACKUP");
            stmt.setString(1, resourceId.toString());
            stmt.setInt(2, tagId);
            res = stmt.executeQuery();
            if (res.next()) {
                file = createCmsBackupResourceFromResultSet(res, false);
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            } else {
                throw new CmsException("[" + this.getClass().getName() + "] " + resourceId.toString(), CmsException.C_NOT_FOUND);
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
                        res.getInt("TAG_ID"),
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_ID")),
                        res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_NAME")),
                        res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_DESCRIPTION")),
                        res.getInt(m_sqlManager.get("C_PROJECTS_TASK_ID")),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_USER_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_GROUP_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_MANAGERGROUP_ID"))),
                        CmsDbUtil.getTimestamp(res, m_sqlManager.get("C_PROJECTS_PROJECT_CREATEDATE")),
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_TYPE")),
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
                projectResources.addElement(res.getString("RESOURCE_NAME"));
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
     * @see org.opencms.db.I_CmsBackupDriver#readBackupProperties(com.opencms.file.CmsBackupResource)
     */
    public HashMap readBackupProperties(CmsBackupResource resource) throws CmsException {
        HashMap returnValue = new HashMap();
        ResultSet result = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        
        String resourceName= resource.getRootPath();
        // hack: this never should happen, but it does.......
        if ((resource.isFolder()) && (!resourceName.endsWith("/"))) {
            resourceName += "/";
        } 
        
        CmsUUID backupId = resource.getBackupId();
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_READALL_BACKUP");
            stmt.setString(1, backupId.toString());
            stmt.setString(2, resourceName);
            stmt.setInt(3, resource.getType());
            stmt.setInt(4, resource.getTagId());
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
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupProject(com.opencms.file.CmsProject, int, long, com.opencms.file.CmsUser)
     */
    public void writeBackupProject(CmsProject currentProject, int tagId, long publishDate, CmsUser currentUser) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String ownerName = new String();
        String group = new String();
        String managerGroup = new String();

        try {
            CmsUser owner = m_driverManager.readUser(currentProject.getOwnerId());
            ownerName = owner.getName() + " " + owner.getFirstname() + " " + owner.getLastname();
        } catch (CmsException e) {
            // the owner could not be read
            ownerName = "";
        }
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
        Vector projectresources = m_driverManager.getProjectDriver().readAllProjectResources(currentProject.getId());
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
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupProperties(com.opencms.file.CmsProject, com.opencms.file.CmsResource, java.util.Map, com.opencms.flex.util.CmsUUID, int, int)
     */
    public void writeBackupProperties(CmsProject publishProject, CmsResource resource, Map properties, CmsUUID backupId, int tagId, int versionId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnectionForBackup();
            // write the properties 
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_CREATE_BACKUP");
            Iterator keys = properties.keySet().iterator();
            String key = null;
            boolean hasBatch=false;
            while (keys.hasNext()) {
                hasBatch = true;
                key = (String) keys.next();
                CmsPropertydefinition propdef = m_driverManager.getVfsDriver().readPropertydefinition(key, publishProject.getId(), resource.getType());
                String value = (String) properties.get(key);
            
                if (propdef == null) {
                    // there is no propertydefinition for with the overgiven name for the resource
                    throw new CmsException("[" + this.getClass().getName() + "] " + key, CmsException.C_NOT_FOUND);
                } else {
                    // write the property into the db
                    stmt.setString(1, backupId.toString());
                    stmt.setInt(2, m_sqlManager.nextId(m_sqlManager.get("C_TABLE_PROPERTIES_BACKUP")));
                    stmt.setInt(3, propdef.getId());
                    stmt.setString(4, resource.getResourceId().toString());
                    stmt.setString(5, resource.getRootPath());
                    stmt.setString(6, m_sqlManager.validateNull(value));
                    stmt.setInt(7, tagId);
                    stmt.setInt(8, versionId);
                    stmt.addBatch();
                }
            }
            
            if (hasBatch) {
                stmt.executeBatch();
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
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupResource(com.opencms.file.CmsUser, com.opencms.file.CmsProject, com.opencms.file.CmsResource, java.util.Map, int, long, int)
     */
    public void writeBackupResource(CmsUser currentUser, CmsProject publishProject, CmsResource resource, Map properties, int tagId, long publishDate, int maxVersions) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        CmsUUID backupPkId = new CmsUUID();
        byte[] content = null;
        int versionId;
        
        String lastModifiedName = "";
        String createdName = "";
        try {
            CmsUser lastModified = m_driverManager.getUserDriver().readUser(resource.getUserLastModified());
            lastModifiedName = "["+lastModified.getName() + "] " + lastModified.getFirstname() + " " + lastModified.getLastname();
            CmsUser created = m_driverManager.getUserDriver().readUser(resource.getUserCreated());
            createdName = created.getName() + " " + created.getFirstname() + " " + created.getLastname();
        } catch (CmsException e) {
            lastModifiedName = resource.getUserCreated().toString();
            createdName = resource.getUserLastModified().toString();
        }

        try {
            conn = m_sqlManager.getConnectionForBackup();


            // now get the new version id for this resource
            versionId=nextVersionId(resource);

            if (resource.isFile()) {

                if (!this.existsBackupResource(resource, tagId)) {

                    // write the file content
                    content = ((CmsFile)resource).getContents();
                    writeBackupFileContent(backupPkId, resource.getFileId(), content, tagId, versionId);

                    // write the resource
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_WRITE_BACKUP");
                    stmt.setString(1, resource.getResourceId().toString());
                    stmt.setInt(2, resource.getType());
                    stmt.setInt(3, resource.getFlags());
                    stmt.setString(4, resource.getFileId().toString());
                    stmt.setInt(5, resource.getLoaderId());
                    stmt.setTimestamp(6, new Timestamp(publishDate));
                    stmt.setString(7, resource.getUserCreated().toString());
                    stmt.setTimestamp(8, new Timestamp(resource.getDateLastModified()));
                    stmt.setString(9, resource.getUserLastModified().toString());
                    stmt.setInt(10, resource.getState());
                    stmt.setInt(11, resource.getLength());
                    stmt.setString(12, CmsUUID.getNullUUID().toString());
                    stmt.setInt(13, publishProject.getId());
                    stmt.setInt(14, 1);
                    stmt.setInt(15, tagId);
                    stmt.setInt(16, versionId);
                    stmt.setString(17, backupPkId.toString());
                    stmt.setString(18, createdName);
                    stmt.setString(19, lastModifiedName);
                    stmt.executeUpdate();

                    m_sqlManager.closeAll(null, stmt, null);
                }
            }


            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_WRITE_BACKUP");
            stmt.setString(1, resource.getStructureId().toString());
            stmt.setString(2, resource.getParentStructureId().toString());
            stmt.setString(3, resource.getResourceId().toString());
            stmt.setString(4, resource.getName());
            stmt.setInt(5, resource.getState());
            stmt.setInt(6, tagId);      
            stmt.setInt(7, versionId);
            stmt.setString(8, backupPkId.toString());
            stmt.executeUpdate();

            writeBackupProperties(publishProject, resource, properties, backupPkId, tagId, versionId);
          
            // now check if there are old backup versions to delete
            List existingBackups = readAllBackupFileHeaders(resource.getStructureId());
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
    }


    /**
     * @see org.opencms.db.I_CmsBackupDriver#readMaxBackupVersion(com.opencms.flex.util.CmsUUID)
     */
    public int readMaxBackupVersion(CmsUUID resourceId) throws CmsException {
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
     * @see org.opencms.db.I_CmsBackupDriver#deleteBackups(java.util.List, int)
     */
    public void deleteBackups(List existingBackups, int maxVersions) throws CmsException {        
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        Connection conn = null;
        CmsBackupResource currentResource = null;
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
                stmt2.setString(2, currentResource.getResourceId().toString());
                stmt2.setInt(3, currentResource.getTagId());
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
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupFileContent(com.opencms.flex.util.CmsUUID, com.opencms.flex.util.CmsUUID, byte[], int, int)
     */
    public void writeBackupFileContent(CmsUUID backupId, CmsUUID fileId, byte[] fileContent, int tagId, int versionId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_FILES_WRITE_BACKUP");

            stmt.setString(1, fileId.toString());

            if (fileContent.length < 2000) {
                stmt.setBytes(2, fileContent);
            } else {
                stmt.setBinaryStream(2, new ByteArrayInputStream(fileContent), fileContent.length);
            }

            stmt.setInt(3, tagId);
            stmt.setInt(4, versionId);
            stmt.setString(5, backupId.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
    private boolean existsBackupResource(CmsResource resource, int tagId) throws CmsException {
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

}

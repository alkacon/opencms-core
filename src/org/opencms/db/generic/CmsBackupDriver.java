/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsBackupDriver.java,v $
 * Date   : $Date: 2003/07/14 18:43:54 $
 * Version: $Revision: 1.14 $
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
import org.opencms.db.I_CmsBackupDriver;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsBackupProject;
import com.opencms.file.CmsBackupResource;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;
import com.opencms.util.SqlHelper;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * Generic (ANSI-SQL) database server implementation of the backup driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.14 $ $Date: 2003/07/14 18:43:54 $
 * @since 5.1
 */
public class CmsBackupDriver extends Object implements I_CmsBackupDriver {

    /** The driver manager instance. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager instance. */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;
    
    /** The max. number of backup versions of a single resource. */
    private int m_maxResourceVersionCount; 

    /**
     * @see org.opencms.db.I_CmsBackupDriver#createCmsBackupResourceFromResultSet(java.sql.ResultSet, boolean)
     */
    public CmsBackupResource createCmsBackupResourceFromResultSet(ResultSet res, boolean hasContent) throws SQLException {
        byte[] content = null;

        int versionId = res.getInt(m_sqlManager.get("C_RESOURCES_VERSION_ID"));
        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_ID")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_PARENT_ID")));
        String resourceName = res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_NAME"));
        int resourceType = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_TYPE"));
        int resourceFlags = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_FLAGS"));
        int projectID = res.getInt(m_sqlManager.get("C_RESOURCES_PROJECT_ID"));
        CmsUUID fileId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_FILE_ID")));
        int state = res.getInt(m_sqlManager.get("C_RESOURCES_STATE"));
        int launcherType = res.getInt(m_sqlManager.get("C_RESOURCES_LAUNCHER_TYPE"));
        String launcherClass = res.getString(m_sqlManager.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
        long dateCreated = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_CREATED")).getTime();
        long dateLastModified = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
        int resourceSize = res.getInt(m_sqlManager.get("C_RESOURCES_SIZE"));
        CmsUUID userLastModified = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_USER_LASTMODIFIED")));
        String userLastModifiedName = res.getString(m_sqlManager.get("C_RESOURCES_LASTMODIFIED_BY_NAME"));
        CmsUUID userCreated = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_USER_CREATED")));
        String userCreatedName = res.getString(m_sqlManager.get("C_RESOURCES_USER_CREATED_NAME"));        
        int lockedInProject = res.getInt("LOCKED_IN_PROJECT");
        int vfsLinkType = res.getInt(m_sqlManager.get("C_RESOURCES_LINK_TYPE"));

        if (hasContent) {
            content = m_sqlManager.getBytes(res, m_sqlManager.get("C_RESOURCES_FILE_CONTENT"));
        } else {
            content = new byte[0];
        }

        return new CmsBackupResource(versionId, structureId, resourceId, parentId, fileId, resourceName, resourceType, resourceFlags, userCreated, userCreatedName, CmsUUID.getNullUUID(), "", projectID, 0, state, launcherType, launcherClass, dateCreated, dateLastModified, userLastModified, userLastModifiedName, content, resourceSize, lockedInProject, vfsLinkType);
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
                String[] statements = {"C_BACKUP_DELETE_PROJECT_BYVERSION",
                    "C_BACKUP_DELETE_PROJECTRESOURCES_BYVERSION", "C_BACKUP_DELETE_RESOURCES_BYVERSION", 
                    "C_BACKUP_DELETE_FILES_BYVERSION", "C_BACKUP_DELETE_PROPERTIES_BYVERSION", 
                    "C_BACKUP_DELETE_MODULEMASTER_BYVERSION", "C_BACKUP_DELETE_MODULEMEDIA_BYVERSION"};
                    
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

        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[" + this.getClass().getName() + "] destroyed!");
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
                Vector resources = readBackupProjectResources(res.getInt("VERSION_ID"));
                projects.addElement(
                    new CmsBackupProject(
                        res.getInt("VERSION_ID"),
                        res.getInt("PROJECT_ID"),
                        res.getString("PROJECT_NAME"),
                        SqlHelper.getTimestamp(res, "PROJECT_PUBLISHDATE"),
                        new CmsUUID(res.getString("PROJECT_PUBLISHED_BY")),
                        res.getString("PROJECT_PUBLISHED_BY_NAME"),
                        res.getString("PROJECT_DESCRIPTION"),
                        res.getInt("TASK_ID"),
                        new CmsUUID(res.getString("USER_ID")),
                        res.getString("USER_NAME"),
                        new CmsUUID(res.getString("GROUP_ID")),
                        res.getString("GROUP_NAME"),
                        new CmsUUID(res.getString("MANAGERGROUP_ID")),
                        res.getString("MANAGERGROUP_NAME"),
                        SqlHelper.getTimestamp(res, "PROJECT_CREATEDATE"),
                        res.getInt("PROJECT_TYPE"),
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
     * @see org.opencms.db.I_CmsBackupDriver#init(source.org.apache.java.util.Configurations, java.lang.String, org.opencms.db.CmsDriverManager)
     */
    public void init(Configurations config, String dbPoolUrl, CmsDriverManager driverManager) {
        m_sqlManager = this.initQueries(dbPoolUrl);
        m_driverManager = driverManager;
                
        m_maxResourceVersionCount = config.getInteger(I_CmsConstants.C_CONFIGURATION_HISTORY + ".maxCountPerResource", 10);

        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". max. backup/resource : " + m_maxResourceVersionCount);
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Backup driver init   : ok");
        }
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#initQueries(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl) {
        return new org.opencms.db.generic.CmsSqlManager(dbPoolUrl);
    }

    /**
     * @see org.opencms.db.I_CmsBackupDriver#nextBackupVersionId()
     */
    public int nextBackupVersionId() {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        int versionId = 1;
        int resVersionId = 1;
        
        try {
            // get the max version id
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_BACKUP_MAXVER");
            res = stmt.executeQuery();
            if (res.next()) {
                versionId = res.getInt(1) + 1;
            }

            m_sqlManager.closeAll(null, stmt, res);
            
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_BACKUP_MAXVER_RESOURCE");
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
     * @see org.opencms.db.I_CmsBackupDriver#readAllBackupFileHeaders(java.lang.String)
     */
    public List readAllBackupFileHeaders(CmsUUID resourceId) throws CmsException {
        CmsBackupResource file = null;
        ResultSet res = null;
        List allHeaders = (List) new ArrayList();
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_ALL_BACKUP");
            stmt.setString(1, resourceId.toString());
            res = stmt.executeQuery();
            while (res.next()) {
                file = createCmsBackupResourceFromResultSet(res, false);
                allHeaders.add(file);
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
     * @see org.opencms.db.I_CmsBackupDriver#readBackupFile(int, java.lang.String)
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
     * @see org.opencms.db.I_CmsBackupDriver#readBackupFileHeader(int, java.lang.String)
     */
    public CmsBackupResource readBackupFileHeader(int versionId, CmsUUID resourceId) throws CmsException {
        CmsBackupResource file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_BACKUP");
            stmt.setString(1, resourceId.toString());
            stmt.setInt(2, versionId);
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
    public CmsBackupProject readBackupProject(int versionId) throws CmsException {

        PreparedStatement stmt = null;
        CmsBackupProject project = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READBYVERSION_BACKUP");

            stmt.setInt(1, versionId);
            res = stmt.executeQuery();

            if (res.next()) {
                Vector projectresources = readBackupProjectResources(versionId);
                project =
                    new CmsBackupProject(
                        res.getInt("VERSION_ID"),
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_ID")),
                        res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_NAME")),
                        SqlHelper.getTimestamp(res, "PROJECT_PUBLISHDATE"),
                        new CmsUUID(res.getString("PROJECT_PUBLISHED_BY")),
                        res.getString("PROJECT_PUBLISHED_BY_NAME"),
                        res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_DESCRIPTION")),
                        res.getInt(m_sqlManager.get("C_PROJECTS_TASK_ID")),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_USER_ID"))),
                        res.getString("USER_NAME"),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_GROUP_ID"))),
                        res.getString("GROUP_NAME"),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_MANAGERGROUP_ID"))),
                        res.getString("MANAGERGROUP_NAME"),
                        SqlHelper.getTimestamp(res, m_sqlManager.get("C_PROJECTS_PROJECT_CREATEDATE")),
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_TYPE")),
                        projectresources);
            } else {
                // project not found!
                throw new CmsException("[" + this.getClass().getName() + "] version " + versionId, CmsException.C_NOT_FOUND);
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
    public Vector readBackupProjectResources(int versionId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        Vector projectResources = new Vector();
        
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ_BACKUP");
            stmt.setInt(1, versionId);
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
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupProject(com.opencms.file.CmsProject, int, long, com.opencms.file.CmsUser)
     */
    public void writeBackupProject(CmsProject currentProject, int versionId, long publishDate, CmsUser currentUser) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String ownerName = new String();
        String group = new String();
        String managerGroup = new String();
        
        try {
            CmsUser owner = m_driverManager.getUserDriver().readUser(currentProject.getOwnerId());
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
            stmt.setInt(1, versionId);
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
                stmt.setInt(1, versionId);
                stmt.setInt(2, currentProject.getId());
                stmt.setString(3, (String) i.next());
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
     * @see org.opencms.db.I_CmsBackupDriver#writeBackupResource(int, com.opencms.file.CmsResource, byte[], java.util.Map, int, long)
     */
    public void writeBackupResource(CmsUser currentUser, CmsProject publishProject, CmsResource resource, Map properties, int versionId, long publishDate) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String lastModifiedName = null;
        String createdName = null;
        CmsUUID backupPkId = new CmsUUID();
        byte[] content = null;
        int vfsLinkType = I_CmsConstants.C_UNKNOWN_ID;

        try {
            CmsUser lastModified = m_driverManager.getUserDriver().readUser(resource.getResourceLastModifiedBy());
            lastModifiedName = lastModified.getName() + " " + lastModified.getFirstname() + " " + lastModified.getLastname();

            CmsUser created = m_driverManager.readUser(null, resource.getOwnerId());
            createdName = created.getName() + " " + created.getFirstname() + " " + created.getLastname();
        } catch (CmsException e) {
            lastModifiedName = "";
            createdName = "";
        }

        try {
            conn = m_sqlManager.getConnectionForBackup();           
            
            if (resource.isHardLink()) {
                if (resource.getType() != I_CmsConstants.C_TYPE_FOLDER) {
    				// write the file content
    				content = ((CmsFile)resource).getContents();
    				writeBackupFileContent(backupPkId,resource.getFileId(),content,versionId);               
                }
    
                // write the resource
                stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_WRITE_BACKUP");
                stmt.setString(1, resource.getResourceId().toString());
                stmt.setInt(2, resource.getType());
                stmt.setInt(3, resource.getFlags());
                stmt.setString(4, resource.getFileId().toString());
                stmt.setInt(5, resource.getLauncherType());
                stmt.setString(6, resource.getLauncherClassname());
                stmt.setTimestamp(7, new Timestamp(publishDate));
    			stmt.setString(8, resource.getOwnerId().toString());
                stmt.setTimestamp(9, new Timestamp(resource.getDateLastModified()));
    			stmt.setString(10, resource.getResourceLastModifiedBy().toString());
    			stmt.setInt(11, resource.getState());
                stmt.setInt(12, resource.getLength());
                stmt.setInt(13, versionId);
                stmt.setString(14, backupPkId.toString());
                stmt.executeUpdate();                
                m_sqlManager.closeAll(null, stmt, null);
                
                vfsLinkType = I_CmsConstants.C_VFS_LINK_TYPE_MASTER;
            } else {
                vfsLinkType = I_CmsConstants.C_VFS_LINK_TYPE_SLAVE;
            }

            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_WRITE_BACKUP");
            stmt.setString(1, resource.getId().toString());
            stmt.setString(2, resource.getParentId().toString());
            stmt.setString(3, resource.getResourceId().toString());
            stmt.setInt(4, publishProject.getId());
            stmt.setString(5, resource.getResourceName());
            stmt.setInt(6, vfsLinkType);
            stmt.setInt(7, resource.getState());
            stmt.setString(8, resource.isLockedBy().toString());
            stmt.setString(9, resource.getResourceLastModifiedBy().toString());
            stmt.setString(10, resource.getOwnerId().toString());
            stmt.setString(11, lastModifiedName);
            stmt.setString(12, createdName);
            stmt.setInt(13, versionId);
            stmt.setString(14, backupPkId.toString());
            stmt.executeUpdate();
            
            List existingBackups = readAllBackupFileHeaders(resource.getId());
            if (existingBackups.size() > getMaxResourceVersionCount()) {
                // delete redundant backups
                deleteBackups(existingBackups);
            }            

            /*
            m_sqlManager.closeAll(null, stmt, null);
            
            // write the properties 
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_CREATE_BACKUP");
            Iterator keys = properties.keySet().iterator();
            String key = null;
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
                    stmt.setInt(1, m_sqlManager.nextId(m_sqlManager.get("C_TABLE_PROPERTIES_BACKUP")));
                    stmt.setInt(2, propdef.getId());
                    stmt.setString(3, resource.getId().toString());
                    stmt.setString(4, m_sqlManager.validateNull(value));
                    stmt.setInt(5, versionId);
                    stmt.addBatch();
                }
            }

            if (hasBatch) {
                stmt.executeBatch();
            }
            */
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
    
    /**
     * @see org.opencms.db.I_CmsBackupDriver#getMaxResourceVersionCount()
     */
    public int getMaxResourceVersionCount() {
        return m_maxResourceVersionCount;
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
                maxBackupVersion = res.getInt(m_sqlManager.get("C_RESOURCES_VERSION_ID"));
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
     * @see org.opencms.db.I_CmsBackupDriver#deleteBackups(java.util.List)
     */
    public void deleteBackups(List existingBackups) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        CmsBackupResource currentResource = null;
        int count = existingBackups.size() - getMaxResourceVersionCount();

		try {
			conn = m_sqlManager.getConnectionForBackup();
			stmt = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_DELETE_RESOURCE");

			for (int i = 0; i < count; i++) {
				currentResource = (CmsBackupResource) existingBackups.get(i);
				stmt.setString(1, currentResource.getId().toString());
				stmt.setInt(2, currentResource.getVersionId());
				stmt.addBatch();
			}

			if (count > 0) {
				stmt.executeBatch();
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
	 * @see org.opencms.db.I_CmsBackupDriver#writeBackupFileContent(com.opencms.flex.util.CmsUUID, com.opencms.flex.util.CmsUUID, byte[], int)
	 */
	public void writeBackupFileContent(CmsUUID backupId, CmsUUID fileId, byte[] fileContent, int versionId) throws CmsException {
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
            
			stmt.setInt(3, versionId);
			stmt.setString(4, backupId.toString());

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
		} finally {
			m_sqlManager.closeAll(conn, stmt, null);
		}
	}
}

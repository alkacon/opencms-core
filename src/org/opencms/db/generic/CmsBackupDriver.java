/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsBackupDriver.java,v $
 * Date   : $Date: 2003/06/13 14:48:16 $
 * Version: $Revision: 1.2 $
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
import com.opencms.file.CmsProject;
import com.opencms.file.CmsPropertydefinition;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;
import com.opencms.util.SqlHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * Generic (ANSI-SQL) database server implementation of the backup driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $ $Date: 2003/06/13 14:48:16 $
 * @since 5.1
 */
public class CmsBackupDriver extends Object implements I_CmsBackupDriver {

    protected CmsDriverManager m_driverManager;

    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;
    
    /**
     * Creates a backup of the published project
     *
     * @param project The project in which the resource was published.
     * @param projectresources The resources of the project
     * @param versionId The version of the backup
     * @param publishDate The date of publishing
     * @param userId The id of the user who had published the project
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void backupProject(CmsProject project, int versionId, long publishDate, CmsUser currentUser) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String ownerName = new String();
        String group = new String();
        String managerGroup = new String();
        try {
            CmsUser owner = m_driverManager.getUserDriver().readUser(project.getOwnerId());
            ownerName = owner.getName() + " " + owner.getFirstname() + " " + owner.getLastname();
        } catch (CmsException e) {
            // the owner could not be read
            ownerName = "";
        }
        try {
            group = m_driverManager.getUserDriver().readGroup(project.getGroupId()).getName();
        } catch (CmsException e) {
            // the group could not be read
            group = "";
        }
        try {
            managerGroup = m_driverManager.getUserDriver().readGroup(project.getManagerGroupId()).getName();
        } catch (CmsException e) {
            // the group could not be read
            managerGroup = "";
        }
        Vector projectresources = m_driverManager.getProjectDriver().readAllProjectResources(project.getId());
        // write backup project to the database
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_CREATE_BACKUP");
            // first write the project
            stmt.setInt(1, versionId);
            stmt.setInt(2, project.getId());
            stmt.setString(3, project.getName());
            stmt.setTimestamp(4, new Timestamp(publishDate));
            stmt.setString(5, currentUser.getId().toString());
            stmt.setString(6, currentUser.getName() + " " + currentUser.getFirstname() + " " + currentUser.getLastname());
            stmt.setString(7, project.getOwnerId().toString());
            stmt.setString(8, ownerName);
            stmt.setString(9, project.getGroupId().toString());
            stmt.setString(10, group);
            stmt.setString(11, project.getManagerGroupId().toString());
            stmt.setString(12, managerGroup);
            stmt.setString(13, project.getDescription());
            stmt.setTimestamp(14, new Timestamp(project.getCreateDate()));
            stmt.setInt(15, project.getType());
            stmt.setInt(16, project.getTaskId());
            stmt.executeUpdate();
            m_sqlManager.closeAll(null, stmt, null);
            
            // now write the projectresources
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_CREATE_BACKUP");
            Iterator i = projectresources.iterator();
            while (i.hasNext()) {
                stmt.setInt(1, versionId);
                stmt.setInt(2, project.getId());
                stmt.setString(3, (String) i.next());
                stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
    
    /**
     * Creates a backup of the published resource
     *
     * @param projectId The project in which the resource was published.
     * @param resource The published resource
     * @param content The file content if the resource is a file.
     * @param properties The properties of the resource.
     * @param versionId The version of the backup
     * @param publishDate The date of publishing
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    public void backupResource(int projectId, CmsResource resource, byte[] content, Map properties, int versionId, long publishDate) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
//        String ownerName = null;
//        String groupName = new String();
        String lastModifiedName = null;
        
//        try {
//            CmsUser owner = m_driverManager.getUserDriver().readUser(resource.getOwnerId());
//            ownerName = owner.getName() + " " + owner.getFirstname() + " " + owner.getLastname();
//        } catch (CmsException e) {
//            // the user could not be read
//            ownerName = "";
//        }
//        
//        try {
//            groupName = m_driverManager.getUserDriver().readGroup(resource.getGroupId()).getName();
//        } catch (CmsException e) {
//            // the group could not be read
//            groupName = "";
//        }
        
        try {
            CmsUser lastModified = m_driverManager.getUserDriver().readUser(resource.getResourceLastModifiedBy());
            lastModifiedName = lastModified.getName() + " " + lastModified.getFirstname() + " " + lastModified.getLastname();
        } catch (CmsException e) {
            // the user could not be read
            lastModifiedName = "";
        }
    
        CmsUUID resourceId = new CmsUUID();
        CmsUUID fileId = CmsUUID.getNullUUID();
        CmsUUID structureId = new CmsUUID();
    
        // write backup resource to the database
    
        try {            
            // if the resource is not a folder then backup the filecontent
            if (resource.getType() != I_CmsConstants.C_TYPE_FOLDER) {
                // write new resource to the database
                fileId = new CmsUUID();
                m_driverManager.getVfsDriver().createFileContent(fileId, content, versionId, projectId, true);
            }
            
            // write the resource
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_WRITE_BACKUP");
            stmt.setString(1, resourceId.toString());
            stmt.setInt(2, resource.getType());
            stmt.setInt(3, resource.getFlags());
            stmt.setString(4, resource.getOwnerId().toString());
            stmt.setString(5, resource.getGroupId().toString());          
            stmt.setString(6, fileId.toString());
            stmt.setInt(7, resource.getAccessFlags());            
            stmt.setInt(8, resource.getLauncherType());
            stmt.setString(9, resource.getLauncherClassname());
            stmt.setTimestamp(10, new Timestamp(publishDate));
            stmt.setTimestamp(11, new Timestamp(resource.getDateLastModified()));
            stmt.setInt(12, resource.getLength());
            stmt.setInt(13,versionId);
            stmt.executeUpdate();
            m_sqlManager.closeAll(null, stmt, null);
            
            // write the structure
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STRUCTURE_WRITE_BACKUP");
            stmt.setString(1, structureId.toString());
            stmt.setString(2, CmsUUID.getNullUUID().toString());
            stmt.setString(3, resourceId.toString());
            stmt.setInt(4, projectId);
            stmt.setString(5, resource.getResourceName());
            stmt.setInt(6, 0);
            stmt.setInt(7, resource.getState());
            stmt.setString(8, resource.isLockedBy().toString()); 
            stmt.setString(9, resource.getResourceLastModifiedBy().toString());
            stmt.setString(10,lastModifiedName);
            stmt.setInt(11,versionId);            
            stmt.executeUpdate();  
            m_sqlManager.closeAll(null, stmt, null);          
            
            // write the properties
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_CREATE_BACKUP");
            Iterator keys = properties.keySet().iterator();
            String key;
            while (keys.hasNext()) {
                key = (String) keys.next();
                CmsPropertydefinition propdef = m_driverManager.getVfsDriver().readPropertydefinition(key, projectId, resource.getType());
                String value = (String) properties.get(key);
                if (propdef == null) {
                    // there is no propertydefinition for with the overgiven name for the resource
                    throw new CmsException("[" + this.getClass().getName() + "] " + key, CmsException.C_NOT_FOUND);
                } else {
                    // write the property into the db
                    stmt.setInt(1, m_sqlManager.nextId(m_sqlManager.get("C_TABLE_PROPERTIES_BACKUP")));
                    stmt.setInt(2, propdef.getId());
                    stmt.setString(3, resourceId.toString());
                    stmt.setString(4, m_sqlManager.validateNull(value));
                    stmt.setInt(5, versionId);
                    stmt.executeUpdate();
                    stmt.clearParameters();
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Semi-constructor to create a CmsBackupResource instance from a JDBC result set.
     * 
     * @param res the JDBC ResultSet
     * @return CmsBackupResource the new CmsBackupResource object
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    public CmsBackupResource createCmsBackupResourceFromResultSet(ResultSet res) throws SQLException {
        int versionId = res.getInt(m_sqlManager.get("C_RESOURCES_VERSION_ID"));
        CmsUUID structureId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_STRUCTURE_ID")));
        CmsUUID resourceId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_ID")));
        CmsUUID parentId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_PARENT_ID")));
        String resName = res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_NAME"));
        int resType = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_TYPE"));
        int resFlags = res.getInt(m_sqlManager.get("C_RESOURCES_RESOURCE_FLAGS"));
        CmsUUID userId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_USER_ID")));
        String userName = res.getString(m_sqlManager.get("C_RESOURCES_USER_NAME"));
        CmsUUID groupId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_GROUP_ID")));
        String groupName = res.getString(m_sqlManager.get("C_RESOURCES_GROUP_NAME"));
        int projectID = res.getInt(m_sqlManager.get("C_RESOURCES_PROJECT_ID"));
        CmsUUID fileId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_FILE_ID")));
        int accessFlags = res.getInt(m_sqlManager.get("C_RESOURCES_ACCESS_FLAGS"));
        int state = res.getInt(m_sqlManager.get("C_RESOURCES_STATE"));
        int launcherType = res.getInt(m_sqlManager.get("C_RESOURCES_LAUNCHER_TYPE"));
        String launcherClass = res.getString(m_sqlManager.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
        long created = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_CREATED")).getTime();
        long modified = SqlHelper.getTimestamp(res, m_sqlManager.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
        int resSize = res.getInt(m_sqlManager.get("C_RESOURCES_SIZE"));
        CmsUUID modifiedBy = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_LASTMODIFIED_BY")));
        String modifiedByName = res.getString(m_sqlManager.get("C_RESOURCES_LASTMODIFIED_BY_NAME"));
        int lockedInProject = res.getInt("LOCKED_IN_PROJECT");

        return new CmsBackupResource(versionId, structureId, resourceId, parentId, fileId, resName, resType, resFlags, userId, userName, groupId, groupName, projectID, accessFlags, state, launcherType, launcherClass, created, modified, modifiedBy, modifiedByName, new byte[0], resSize, lockedInProject);
    }
    
    /**
     * Deletes the versions from the backup tables that are older then the given date
     *
     * @param maxdate The date of the last version that should be remained after deleting
     * @return int The oldest remaining version
     */
    public int deleteBackups(long maxdate) throws CmsException {
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        int maxVersion = 0;
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_BACKUP_READ_MAXVERSION");
            // read the max. version_id from database by the publish_date
            stmt.setTimestamp(1, new Timestamp(maxdate));
            res = stmt.executeQuery();
            if (res.next()) {
                maxVersion = res.getInt(1);
            }
            res.close();
            stmt.close();
            if (maxVersion > 0) {
                String[] statements = { "C_BACKUP_DELETE_PROJECT_BYVERSION", "C_BACKUP_DELETE_PROJECTRESOURCES_BYVERSION", "C_BACKUP_DELETE_RESOURCES_BYVERSION", "C_BACKUP_DELETE_FILES_BYVERSION", "C_BACKUP_DELETE_PROPERTIES_BYVERSION", "C_BACKUP_DELETE_MODULEMASTER_BYVERSION", "C_BACKUP_DELETE_MODULEMEDIA_BYVERSION" };
                for (int i = 0; i < statements.length; i++) {
                    stmt = m_sqlManager.getPreparedStatement(conn, statements[i]);
                    stmt.setInt(1, maxVersion);
                    stmt.executeUpdate();
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, ex, false);
        } finally {
            // close all db-resources
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

    protected void finalize() throws Throwable {
        if (m_sqlManager != null) {
            m_sqlManager.finalize();
        }

        m_sqlManager = null;
        m_driverManager = null;
    }
    
    /**
     * Returns all projects from the history.
     *
     *
     * @return a Vector of projects.
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
            throw m_sqlManager.getCmsException(this, "getAllBackupProjects()", CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (projects);
    }
    
    /**
     * Get the next version id for the published backup resources
     *
     * @return int The new version id
     */
    public int getBackupVersionId() {
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
            res.close();
            stmt.close();
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

    public void init(Configurations config, String dbPoolUrl, CmsDriverManager driverManager) {
        m_sqlManager = this.initQueries(dbPoolUrl);
        m_driverManager = driverManager;

        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging()) {
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
     * Reads all file headers of a file in the OpenCms.<BR>
     * The reading excludes the filecontent.
     *
     * @param filename The name of the file to be read.
     *
     * @return Vector of file headers read from the Cms.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readAllFileHeadersForHist(String resourceName) throws CmsException {
        //CmsBackupResource file = null;
        ResultSet res = null;
        Vector allHeaders = new Vector();
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_ALL_BACKUP");
            // read file header data from database
            stmt.setString(1, resourceName);
            res = stmt.executeQuery();
            // create new file headers
            while (res.next()) {
                allHeaders.addElement(createCmsBackupResourceFromResultSet(res));
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
     * Reads a project from the backup tables.
     *
     * @param versionId The versionId of the backup project.
     * @throws CmsException Throws CmsException if something goes wrong.
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
            throw m_sqlManager.getCmsException(this, "readBackupProjectId(int)", CmsException.C_SQL_ERROR, e, false);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "readBackupProjectId(int)", CmsException.C_UNKNOWN_EXCEPTION, e, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return project;
    }
    
    /**
     * select a projectResource from an given project and resourcename
     *
     * @param project The project in which the resource is used.
     * @param resource The resource to be read from the Cms.
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    protected Vector readBackupProjectResources(int versionId) throws CmsException {
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
     * Reads a file from the history of the Cms.<BR/>
     *
     * @param versionId The versionId of the resource.
     * @param filename The complete name of the file (including pathinformation).
     *
     * @return file The read file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsBackupResource readFileForHist(int versionId, String filename) throws CmsException {
        CmsBackupResource file = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_FILES_READ_BACKUP");
            stmt.setString(1, filename);
            stmt.setInt(2, versionId);
            res = stmt.executeQuery();
            if (res.next()) {
                file = createCmsBackupResourceFromResultSet(res);
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
     * Reads a file header from the backup of the Cms.<BR/>
     * The reading excludes the filecontent.
     *
     * @param versionId The Id of the version of the resource.
     * @param filename The complete name of the new file (including pathinformation).
     *
     * @return file The read file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public CmsBackupResource readFileHeaderForHist(int versionId, String filename) throws CmsException {

        CmsBackupResource file = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnectionForBackup();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_READ_BACKUP");
            // read file data from database
            stmt.setString(1, filename);
            stmt.setInt(2, versionId);
            res = stmt.executeQuery();
            // create new file
            if (res.next()) {
                file = createCmsBackupResourceFromResultSet(res);
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

}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsProjectDriver.java,v $
 * Date   : $Date: 2004/10/28 11:07:27 $
 * Version: $Revision: 1.193 $
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
import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.db.I_CmsRuntimeInfo;
import org.opencms.db.I_CmsRuntimeInfoFactory;
import org.opencms.file.*;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;


/**
 * Generic (ANSI-SQL) implementation of the project driver methods.<p>
 *
 * @version $Revision: 1.193 $ $Date: 2004/10/28 11:07:27 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.1
 */
public class CmsProjectDriver extends Object implements I_CmsDriver, I_CmsProjectDriver {

    /** Internal debugging flag. */
    private static final boolean C_DEBUG = false;

    /** Table key for projects. */
    protected static final String C_TABLE_PROJECTS = "CMS_PROJECTS";

   /** The driver manager. */
    protected CmsDriverManager m_driverManager;

    /** Array containing all max-ids for the tables. */
    protected int[] m_maxIds;

    /** The SQL manager. */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createProject(org.opencms.file.CmsUser, org.opencms.file.CmsGroup, org.opencms.file.CmsGroup, org.opencms.workflow.CmsTask, java.lang.String, java.lang.String, int, int, java.lang.Object)
    */
    public CmsProject createProject(CmsUser owner, CmsGroup group, CmsGroup managergroup, CmsTask task, String name, String description, int flags, int type, Object reservedParam) throws CmsException {
        CmsProject project = null;

        if ((description == null) || (description.length() < 1)) {
            description = " ";
        }

        long createTime = System.currentTimeMillis();
        Connection conn = null;
        PreparedStatement stmt = null;

        int id = I_CmsConstants.C_UNKNOWN_ID;

        try {
            if (reservedParam == null) {
                // get a new primary key ID
                id = m_sqlManager.nextId(C_TABLE_PROJECTS);
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection();
            } else {
                // use the primary key ID passed in the params Map
                id = ((Integer)((Map) reservedParam).get("pkProjectId")).intValue();                               
                // get a JDBC connection from the reserved JDBC pools
                int projectId = ((Integer)((Map) reservedParam).get("projectId")).intValue();                
                conn = m_sqlManager.getConnection(projectId);
            }
            
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_CREATE");

            stmt.setInt(1, id);
            stmt.setString(2, owner.getId().toString());
            stmt.setString(3, group.getId().toString());
            stmt.setString(4, managergroup.getId().toString());
            stmt.setInt(5, task.getId());
            stmt.setString(6, name);
            stmt.setString(7, description);
            stmt.setInt(8, flags);
            stmt.setLong(9, createTime);
            stmt.setInt(10, type);
            stmt.executeUpdate();
            
            project = new CmsProject(id, name, description, task.getId(), owner.getId(), group.getId(), managergroup.getId(), flags, createTime, type);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }

        return project;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createProjectResource(int, java.lang.String, java.lang.Object)
     */
    public void createProjectResource(int projectId, String resourcePath, Object reservedParam) throws CmsException {
        // do not create entries for online-project
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            readProjectResource(projectId, resourcePath, reservedParam);
            throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
        } catch (CmsException e) {
            if (e.getType() == CmsException.C_FILE_EXISTS) {
                throw e;
            }
        }

        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection();
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(((Integer) reservedParam).intValue());
            }
            
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_CREATE");

            // write new resource to the database
            stmt.setInt(1, projectId);
            stmt.setString(2, resourcePath);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteProject(org.opencms.db.I_CmsRuntimeInfo, org.opencms.file.CmsProject)
     */
    public void deleteProject(I_CmsRuntimeInfo runtimeInfo, CmsProject project) throws CmsException {

        // delete the resources from project_resources
        deleteProjectResources(runtimeInfo, project);
        
        // remove the project id form all resources within theis project
        unmarkProjectResources(runtimeInfo, project);

        // finally delete the project
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_DELETE");
            // create the statement
            stmt.setInt(1, project.getId());
            stmt.executeUpdate();
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteProjectResource(int, java.lang.String)
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
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteProjectResources(org.opencms.db.I_CmsRuntimeInfo, org.opencms.file.CmsProject)
     */
    public void deleteProjectResources(I_CmsRuntimeInfo runtimeInfo, CmsProject project) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_DELETEALL");
            stmt.setInt(1, project.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
    }
    
    /**
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishHistory(int, int)
     */
    public void deletePublishHistory(int projectId, int maxBackupTagId) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_DELETE_PUBLISH_HISTORY");
            stmt.setInt(1, maxBackupTagId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }        
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteStaticExportPublishedResource(org.opencms.file.CmsProject, java.lang.String, int, java.lang.String)
     */
    public void deleteStaticExportPublishedResource(CmsProject currentProject, String resourceName, int linkType, String linkParameter) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = m_sqlManager.getConnection(currentProject);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_DELETE_PUBLISHED_LINKS");
            stmt.setString(1, resourceName);
            stmt.setInt(2, linkType);
            stmt.setString(3, linkParameter);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteAllStaticExportPublishedResources(org.opencms.file.CmsProject, int)
     */
    public void deleteAllStaticExportPublishedResources(CmsProject currentProject, int linkType) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = m_sqlManager.getConnection(currentProject);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_DELETE_ALL_PUBLISHED_LINKS");
            stmt.setInt(1, linkType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, null);
        }
    }

    
    /**
     * @see org.opencms.db.I_CmsProjectDriver#destroy()
     */
    public void destroy() throws Throwable {
        finalize();

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Shutting down        : " + this.getClass().getName() + " ... ok!");
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#fillDefaults()
     */
    public void fillDefaults() throws CmsException {
        
        try {
            if (readProject(I_CmsConstants.C_PROJECT_ONLINE_ID) != null) {
                // online-project exists - no need of filling defaults
                return;
            }
        } catch (CmsException exc) {
            // ignore the exception - the project was not readable so fill in the defaults
        }

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Database init        : filling default values");
        }

        // set the groups
        String guestGroup = OpenCms.getDefaultUsers().getGroupGuests();
        CmsGroup guests = m_driverManager.getUserDriver().createGroup(null, CmsUUID.getConstantUUID(guestGroup), guestGroup, "The guest group", I_CmsConstants.C_FLAG_ENABLED, null, null);
        String administratorsGroup = OpenCms.getDefaultUsers().getGroupAdministrators();
        CmsGroup administrators = m_driverManager.getUserDriver().createGroup(null, CmsUUID.getConstantUUID(administratorsGroup), administratorsGroup, "The administrators group", I_CmsConstants.C_FLAG_ENABLED | I_CmsConstants.C_FLAG_GROUP_PROJECTMANAGER, null, null);
        String usersGroup = OpenCms.getDefaultUsers().getGroupUsers();
        CmsGroup users = m_driverManager.getUserDriver().createGroup(null, CmsUUID.getConstantUUID(usersGroup), usersGroup, "The users group", I_CmsConstants.C_FLAG_ENABLED | I_CmsConstants.C_FLAG_GROUP_ROLE | I_CmsConstants.C_FLAG_GROUP_PROJECTCOWORKER, null, null);
        String projectmanagersGroup = OpenCms.getDefaultUsers().getGroupProjectmanagers();
        CmsGroup projectmanager = m_driverManager.getUserDriver().createGroup(null, CmsUUID.getConstantUUID(projectmanagersGroup), projectmanagersGroup, "The projectmanager group", I_CmsConstants.C_FLAG_ENABLED | I_CmsConstants.C_FLAG_GROUP_PROJECTMANAGER | I_CmsConstants.C_FLAG_GROUP_PROJECTCOWORKER | I_CmsConstants.C_FLAG_GROUP_ROLE, users.getName(), null);

        // add the users
        String guestUser = OpenCms.getDefaultUsers().getUserGuest();
        CmsUser guest = m_driverManager.getUserDriver().importUser(null, CmsUUID.getConstantUUID(guestUser), guestUser, m_driverManager.digest(""), "The guest user", " ", " ", " ", 0, I_CmsConstants.C_FLAG_ENABLED, new Hashtable(), " ", I_CmsConstants.C_USER_TYPE_SYSTEMUSER, null);
        String adminUser = OpenCms.getDefaultUsers().getUserAdmin();
        CmsUser admin = m_driverManager.getUserDriver().importUser(null, CmsUUID.getConstantUUID(adminUser), adminUser, m_driverManager.digest("admin"), "The admin user", " ", " ", " ", 0, I_CmsConstants.C_FLAG_ENABLED, new Hashtable(), " ", I_CmsConstants.C_USER_TYPE_SYSTEMUSER, null);
        m_driverManager.getUserDriver().createUserInGroup(null, guest.getId(), guests.getId(), null);
        m_driverManager.getUserDriver().createUserInGroup(null, admin.getId(), administrators.getId(), null);
        // add the export user (if it is not set to guest or admin)
        if (!OpenCms.getDefaultUsers().getUserExport().equals(OpenCms.getDefaultUsers().getUserAdmin()) 
                && !OpenCms.getDefaultUsers().getUserExport().equals(OpenCms.getDefaultUsers().getUserGuest())) {
            String exportUser = OpenCms.getDefaultUsers().getUserExport();
            CmsUser export = m_driverManager.getUserDriver().importUser(null, CmsUUID.getConstantUUID(exportUser), exportUser, m_driverManager.digest((new CmsUUID()).toString()), "The static export user", " ", " ", " ", 0, I_CmsConstants.C_FLAG_ENABLED, new Hashtable(), " ", I_CmsConstants.C_USER_TYPE_SYSTEMUSER, null);
            m_driverManager.getUserDriver().createUserInGroup(null, export.getId(), guests.getId(), null);
        }
        m_driverManager.getWorkflowDriver().writeTaskType(1, 0, "../taskforms/adhoc.asp", "Ad-Hoc", "30308", 1, 1);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // online project stuff
        ////////////////////////////////////////////////////////////////////////////////////////////        
        
        // create the online project
        CmsTask task = m_driverManager.getWorkflowDriver().createTask(0, 0, 1, admin.getId(), admin.getId(), administrators.getId(), I_CmsConstants.C_PROJECT_ONLINE, new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), I_CmsConstants.C_TASK_PRIORITY_NORMAL);
        CmsProject onlineProject = createProject(
            admin, 
            users,
            projectmanager, 
            task, 
            I_CmsConstants.C_PROJECT_ONLINE, 
            "The Online Project", 
            I_CmsConstants.C_FLAG_ENABLED, 
            I_CmsConstants.C_PROJECT_TYPE_NORMAL, 
            null);

        // create the root-folder for the online project
        CmsFolder onlineRootFolder = new CmsFolder(
            new CmsUUID(),
            new CmsUUID(),
            "/",
            CmsResourceTypeFolder.C_RESOURCE_TYPE_ID,
            0,
            onlineProject.getId(),
            I_CmsConstants.C_STATE_NEW,
            0,
            admin.getId(),
            0, 
            admin.getId(), 
            1, 
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT            
        );
        
        m_driverManager.getVfsDriver().createResource(
            null, 
            onlineProject, 
            onlineRootFolder, 
            null);       
        
        onlineRootFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
        
        m_driverManager.getVfsDriver().writeResource(
            null, 
            onlineProject, 
            onlineRootFolder, CmsDriverManager.C_UPDATE_ALL);
        
        m_driverManager.getProjectDriver().createProjectResource(
            onlineProject.getId(), 
            onlineRootFolder.getRootPath(), 
            null);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // setup project stuff
        ////////////////////////////////////////////////////////////////////////////////////////////

        // create the task for the setup project
        task = m_driverManager.getWorkflowDriver().createTask(0, 0, 1, admin.getId(), admin.getId(), administrators.getId(), "_setupProject", new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), I_CmsConstants.C_TASK_PRIORITY_NORMAL);
        CmsProject setupProject = createProject(
            admin, 
            administrators,
            administrators, 
            task, 
            "_setupProject", 
            "Initial site import", 
            I_CmsConstants.C_FLAG_ENABLED,
            I_CmsConstants.C_PROJECT_TYPE_TEMPORARY, 
            null);

        // create the root-folder for the offline project       
        CmsResource offlineRootFolder = m_driverManager.getVfsDriver().createResource(
            null,
            setupProject,
            onlineRootFolder,
            null);
        offlineRootFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
        m_driverManager.getVfsDriver().writeResource(
            null,
            setupProject,
            offlineRootFolder,
            CmsDriverManager.C_UPDATE_ALL);
        m_driverManager.getProjectDriver().createProjectResource(
            setupProject.getId(),
            offlineRootFolder.getRootPath(),
            null);
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
     * @see org.opencms.db.I_CmsProjectDriver#getSqlManager()
     */
    public CmsSqlManager getSqlManager() {
        return m_sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(org.opencms.configuration.CmsConfigurationManager, java.util.List, org.opencms.db.CmsDriverManager, org.opencms.db.I_CmsRuntimeInfoFactory)
     */
    public void init(CmsConfigurationManager configurationManager, List successiveDrivers, CmsDriverManager driverManager, I_CmsRuntimeInfoFactory runtimeInfoFactory) {
        
        ExtendedProperties configuration = configurationManager.getConfiguration();
        String poolUrl = configuration.getString("db.project.pool");
        String classname = configuration.getString("db.project.sqlmanager");
        m_sqlManager = this.initSqlManager(classname);
        m_sqlManager.init(I_CmsProjectDriver.C_DRIVER_TYPE_ID, poolUrl);        

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
     * @see org.opencms.db.I_CmsProjectDriver#initSqlManager(String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {
        
        return CmsSqlManager.getInstance(classname);
    }

    /**
     * Resets the state to UNCHANGED and the last-modified-in-project-ID to the current project for a specified resource.<p>
     * 
     * @param runtimeInfo a Cms runtimeInfo
     * @param context the current request context
     * @param resource the Cms resource
     * 
     * @throws CmsException if something goes wrong
     */
    protected void internalResetResourceState(I_CmsRuntimeInfo runtimeInfo, CmsRequestContext context, CmsResource resource) throws CmsException {
        try {
            // reset the resource state and the last-modified-in-project ID offline
            if (resource.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
                resource.setState(I_CmsConstants.C_STATE_UNCHANGED);
                m_driverManager.getVfsDriver().writeResourceState(runtimeInfo, context.currentProject(), resource, CmsDriverManager.C_UPDATE_ALL);
            }

            // important: the project id must be set to the current project because of siblings 
            // that might have not been published, otherwise the siblings would belong to a non-valid 
            // project (e.g. with id 0) and show a grey flag
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error reseting resource state of " + resource.toString(), e);
            }

            throw e;
        }        
    }
    
    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishDeletedFolder(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsFolder, boolean, long, org.opencms.util.CmsUUID, int, int)
     */
    public void publishDeletedFolder(CmsRequestContext context, I_CmsRuntimeInfo runtimeInfo, I_CmsReport report, int m, int n, CmsProject onlineProject, CmsFolder currentFolder, boolean backupEnabled, long publishDate, CmsUUID publishHistoryId, int backupTagId, int maxVersions) throws Exception {
        CmsFolder onlineFolder = null;
        List offlineProperties = null;

        try {
            report.print("( " + m + " / " + n + " ) " + report.key("report.deleting.folder"), I_CmsReport.C_FORMAT_NOTE);
            report.print(context.removeSiteRoot(currentFolder.getRootPath()));
            report.print(report.key("report.dots"));

            try {
                // write the folder to the backup and publishing history                
                if (backupEnabled) {
                    offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(context.currentProject(), currentFolder);
                    m_driverManager.getBackupDriver().writeBackupResource(runtimeInfo, context.currentUser(), context.currentProject(), currentFolder, offlineProperties, backupTagId, publishDate, maxVersions);
                }               
                
                m_driverManager.getProjectDriver().writePublishHistory(runtimeInfo, context.currentProject(), publishHistoryId, backupTagId, currentFolder);
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error writing backup/publishing history of " + currentFolder.toString(), e);
                }

                throw e;
            }

            try {
                // read the folder online
                onlineFolder = m_driverManager.readFolder(context, currentFolder.getStructureId(), CmsResourceFilter.ALL);
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error reading resource " + currentFolder.toString(), e);
                }

                throw e;
            }

            try {
                // delete the properties online and offline
                m_driverManager.getVfsDriver().deletePropertyObjects(runtimeInfo, onlineProject.getId(), onlineFolder, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                m_driverManager.getVfsDriver().deletePropertyObjects(runtimeInfo, context.currentProject().getId(), currentFolder, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error deleting properties of " + currentFolder.toString(), e);
                }

                throw e;
            }

            try {
                // remove the folder online and offline
                m_driverManager.getVfsDriver().removeFolder(runtimeInfo, context.currentProject(), currentFolder);
                
                try {
                    m_driverManager.getVfsDriver().readFolder(runtimeInfo, context.currentProject().getId(), currentFolder.getRootPath());
                } catch (CmsVfsResourceNotFoundException e) {
                    // remove the online folder only if it is really deleted offline
                    m_driverManager.getVfsDriver().removeFolder(runtimeInfo, onlineProject, currentFolder);
                }
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error removing resource " + currentFolder.toString(), e);
                }

                throw e;
            }

            try {
                // remove the ACL online and offline
                m_driverManager.getUserDriver().removeAccessControlEntries(runtimeInfo, onlineProject, onlineFolder.getResourceId());
                m_driverManager.getUserDriver().removeAccessControlEntries(runtimeInfo, context.currentProject(), currentFolder.getResourceId());
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error removing ACLs of " + currentFolder.toString(), e);
                }

                throw e;
            }
            report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
        } catch (Exception e) {
            // this is a dummy try-catch block to have a finally clause here

            if (C_DEBUG) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
            }

            throw e;
        } finally {
            // notify the app. that the published folder and it's properties have been modified offline
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", currentFolder)));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFile(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.Set, boolean, long, org.opencms.util.CmsUUID, int, int)
     */
    public void publishFile(CmsRequestContext context, I_CmsRuntimeInfo runtimeInfo, I_CmsReport report, int m, int n, CmsProject onlineProject, CmsResource offlineFileHeader, Set publishedContentIds, boolean backupEnabled, long publishDate, CmsUUID publishHistoryId, int backupTagId, int maxVersions) throws Exception {    
        CmsFile newFile = null;
        CmsResource onlineFileHeader = null;
        List offlineProperties = null;
        CmsProperty property = null;
        int propertyDeleteOption = -1;

        /*
         * Things to know:
         * Never use offlineFile.getState() here!
         * Only use offlineFileHeader.getState() to determine the state of an offline resource!
         * 
         * In case a resource has siblings, after a sibling was published the structure
         * and resource states are reset to UNCHANGED -> the state of the corresponding
         * offlineFileHeader is still NEW, DELETED or CHANGED, but the state of offlineFile 
         * is UNCHANGED because offlineFile is read AFTER siblings already got published. 
         * Thus, using offlineFile.getState() will inevitably result in unpublished resources!
         */

        try {
            if (offlineFileHeader.getState() == I_CmsConstants.C_STATE_DELETED) {
                report.print("( " + m + " / " + n + " ) " + report.key("report.deleting.file"), I_CmsReport.C_FORMAT_NOTE);
                report.print(context.removeSiteRoot(offlineFileHeader.getRootPath()));
                report.print(report.key("report.dots"));

                try {
                    // read the file header online
                    onlineFileHeader = m_driverManager.getVfsDriver().readFileHeader(runtimeInfo, onlineProject.getId(), offlineFileHeader.getStructureId(), true);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error reading resource " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }

                if (offlineFileHeader.isLabeled() && !m_driverManager.labelResource(context, offlineFileHeader, null, 2)) {
                    // update the resource flags to "unlabeled" of the siblings of the offline resource
                    int flags = offlineFileHeader.getFlags();
                    flags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
                    offlineFileHeader.setFlags(flags);
                }

                try {
                    // write the file to the backup and publishing history
                    if (backupEnabled && !OpenCms.getSystemInfo().keepVersionHistory()) {
                        // delete all backups as well
                        m_driverManager.deleteBackup(offlineFileHeader);                                                                 
                    }
                    
                    m_driverManager.getProjectDriver().writePublishHistory(runtimeInfo, context.currentProject(), publishHistoryId, backupTagId, offlineFileHeader);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error writing backup/publishing history of " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }                

                try {
                    // delete the properties online and offline
                    if (onlineFileHeader.getSiblingCount() > 1) {                    
                        // there are other siblings- delete only structure property values and keep the resource property values
                        propertyDeleteOption = CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_VALUES;
                    } else {
                        // there are no other siblings- delete both the structure and resource property values
                        propertyDeleteOption = CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                    }
                    
                    m_driverManager.getVfsDriver().deletePropertyObjects(runtimeInfo, onlineProject.getId(), onlineFileHeader, propertyDeleteOption);
                    m_driverManager.getVfsDriver().deletePropertyObjects(runtimeInfo, context.currentProject().getId(), offlineFileHeader, propertyDeleteOption);

                    // if the offline file has a resource ID different from the online file
                    // (probably because a (deleted) file was replaced by a new file with the
                    // same name), the properties with the "old" resource ID have to be
                    // deleted also offline
                    if (!onlineFileHeader.getResourceId().equals(offlineFileHeader.getResourceId())) {
                        m_driverManager.getVfsDriver().deletePropertyObjects(runtimeInfo, context.currentProject().getId(), onlineFileHeader, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                    }
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error deleting properties of " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }

                try {
                    // remove the file online and offline
                    m_driverManager.getVfsDriver().removeFile(runtimeInfo, context.currentProject(), offlineFileHeader, true);
                    
                    try {
                        m_driverManager.getVfsDriver().readFileHeader(runtimeInfo, context.currentProject().getId(), offlineFileHeader.getStructureId(), true);
                    } catch (CmsVfsResourceNotFoundException e) {
                        // remove the online file only if it is really deleted offline
                        m_driverManager.getVfsDriver().removeFile(runtimeInfo, onlineProject, onlineFileHeader, true);
                    }
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error removing resource " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }

                try {
                    // delete the ACL online and offline
                    m_driverManager.getUserDriver().removeAccessControlEntries(runtimeInfo, onlineProject, onlineFileHeader.getResourceId());
                    m_driverManager.getUserDriver().removeAccessControlEntries(runtimeInfo, context.currentProject(), offlineFileHeader.getResourceId());
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error removing ACLs of " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }
                report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
            } else if (offlineFileHeader.getState() == I_CmsConstants.C_STATE_CHANGED) {
                report.print("( " + m + " / " + n + " ) " + report.key("report.publishing.file"), I_CmsReport.C_FORMAT_NOTE);
                report.print(context.removeSiteRoot(offlineFileHeader.getRootPath()));
                report.print(report.key("report.dots"));

                try {
                    // read the file header online                   
                    onlineFileHeader = m_driverManager.getVfsDriver().readFileHeader(runtimeInfo, onlineProject.getId(), offlineFileHeader.getStructureId(), false);

                    // reset the labeled link flag before writing the online file
                    int flags = offlineFileHeader.getFlags();
                    flags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
                    offlineFileHeader.setFlags(flags);                   

                    // delete the properties online
                    m_driverManager.getVfsDriver().deletePropertyObjects(runtimeInfo, onlineProject.getId(), onlineFileHeader, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

                    // if the offline file has a resource ID different from the online file
                    // (probably because a deleted file was replaced by a new file with the
                    // same name), the properties mapped to the "old" resource ID have to be
                    // deleted also offline. if this is the case, the online and offline structure
                    // ID's do match, but the resource ID's are different. structure IDs are reused
                    // to prevent orphan structure records in the online project.
                    if (!onlineFileHeader.getResourceId().equals(offlineFileHeader.getResourceId())) {
                        offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(context.currentProject(), onlineFileHeader);
                        if (offlineProperties.size() > 0) {
                            for (int i = 0; i < offlineProperties.size(); i++) {
                                property = (CmsProperty)offlineProperties.get(i);
                                property.setStructureValue(null);
                                property.setResourceValue(CmsProperty.C_DELETE_VALUE);
                            }
                            m_driverManager.getVfsDriver().writePropertyObjects(
                                runtimeInfo,
                                context.currentProject(),
                                onlineFileHeader, offlineProperties);
                        }
                    }

                    // remove the file online
                    boolean removeContent = !publishedContentIds.contains(offlineFileHeader.getResourceId());
                    m_driverManager.getVfsDriver().removeFile(runtimeInfo, onlineProject, onlineFileHeader, removeContent);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error deleting properties of " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }

                try {
                    // publish the file content
                    newFile = m_driverManager.getProjectDriver().publishFileContent(runtimeInfo, context.currentProject(), onlineProject, offlineFileHeader, publishedContentIds);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error creating resource " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }

                try {
                    // write the properties online
                    offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(context.currentProject(), offlineFileHeader);
                    CmsProperty.setAutoCreatePropertyDefinitions(offlineProperties, true);
                    m_driverManager.getVfsDriver().writePropertyObjects(runtimeInfo, onlineProject, newFile, offlineProperties);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error writing properties of " + newFile.toString(), e);
                    }

                    throw e;
                }

                try {
                    // write the ACL online
                    m_driverManager.getUserDriver().publishAccessControlEntries(runtimeInfo, context.currentProject(), onlineProject, newFile.getResourceId(), onlineFileHeader.getResourceId());
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error writing ACLs of " + newFile.toString(), e);
                    }

                    throw e;
                }

                try {
                    // write the file to the backup and publishing history
                    if (backupEnabled) {
                        // TODO: offlineFile is only initialized with null and never set
                        // if (offlineFile == null) {
                        //    offlineFile = m_driverManager.getVfsDriver().readFile(context.currentProject().getId(), false, offlineFileHeader.getStructureId());
                        //    offlineFile.setFullResourceName(offlineFileHeader.getRootPath());                            
                        //}
                        
                        if (offlineProperties == null) {
                            offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(context.currentProject(), offlineFileHeader);
                        }                        
                        m_driverManager.getBackupDriver().writeBackupResource(runtimeInfo, context.currentUser(), context.currentProject(), newFile, offlineProperties, backupTagId, publishDate, maxVersions);
                                  
                        // TODO for later use
                        //CmsBackupResource backupRes= m_driverManager.getBackupDriver().writeBackupResource(context.currentUser(), context.currentProject(), offlineFile, offlineProperties, backupTagId, publishDate, maxVersions);                        
                        //m_driverManager.getBackupDriver().writeBackupResourceContent(context.currentProject().getId(), offlineFileHeader, backupRes);
                    }
                    
                    m_driverManager.getProjectDriver().writePublishHistory(runtimeInfo, context.currentProject(), publishHistoryId, backupTagId, offlineFileHeader);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error writing backup/publishing history of " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }
                report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
            } else if (offlineFileHeader.getState() == I_CmsConstants.C_STATE_NEW) {
                report.print("( " + m + " / " + n + " ) " + report.key("report.publishing.file"), I_CmsReport.C_FORMAT_NOTE);
                report.print(context.removeSiteRoot(offlineFileHeader.getRootPath()));
                report.print(report.key("report.dots"));

                try {
                    // reset the labeled link flag before writing the online file
                    int flags = offlineFileHeader.getFlags();
                    flags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
                    offlineFileHeader.setFlags(flags);     
                    
                    // publish the file content
                    newFile = m_driverManager.getProjectDriver().publishFileContent(runtimeInfo, context.currentProject(), onlineProject, offlineFileHeader, publishedContentIds);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Caught error "+e.getType(), e);
                    }
                    if (e.getType() == CmsException.C_FILE_EXISTS) {
                        try {
                            // remove the existing file and ensure that it's content is written 
                            // in any case by removing it's content ID from the set of published content IDs
                            m_driverManager.getVfsDriver().removeFile(runtimeInfo, onlineProject, offlineFileHeader, true);
                            publishedContentIds.remove(offlineFileHeader.getResourceId());
                            newFile = m_driverManager.getProjectDriver().publishFileContent(runtimeInfo, context.currentProject(), onlineProject, offlineFileHeader, publishedContentIds);
                        } catch (CmsException e1) {
                            if (OpenCms.getLog(this).isErrorEnabled()) {
                                OpenCms.getLog(this).error("Error creating resource " + offlineFileHeader.toString(), e);
                            }

                            throw e1;
                        }
                    } else {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("Error creating resource " + offlineFileHeader.toString(), e);
                        }
                        
                        throw e;
                    }
                }

                try {
                    // write the properties online
                    offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(context.currentProject(), offlineFileHeader);
                    CmsProperty.setAutoCreatePropertyDefinitions(offlineProperties, true);
                    m_driverManager.getVfsDriver().writePropertyObjects(runtimeInfo, onlineProject, newFile, offlineProperties);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error writing properties of " + newFile.toString(), e);
                    }

                    throw e;
                }

                try {
                    // write the ACL online
                    m_driverManager.getUserDriver().publishAccessControlEntries(runtimeInfo, context.currentProject(), onlineProject, offlineFileHeader.getResourceId(), newFile.getResourceId());
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error writing ACLs of " + newFile.toString(), e);
                    }

                    throw e;
                }

                try {
                    // write the file to the backup and publishing history
                    if (backupEnabled) {
                        // if (offlineFile == null) {
                        //    offlineFile = m_driverManager.getVfsDriver().readFile(context.currentProject().getId(), false, offlineFileHeader.getStructureId());
                        //    offlineFile.setFullResourceName(offlineFileHeader.getRootPath());                            
                        // }
                        
                        if (offlineProperties == null) {
                            offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(context.currentProject(), offlineFileHeader);
                        } 
                        m_driverManager.getBackupDriver().writeBackupResource(runtimeInfo, context.currentUser(), context.currentProject(), newFile, offlineProperties, backupTagId, publishDate, maxVersions);
                        
                        // TODO for later use                                    
                        //CmsBackupResource backupRes= m_driverManager.getBackupDriver().writeBackupResource(context.currentUser(), context.currentProject(), offlineFile, offlineProperties, backupTagId, publishDate, maxVersions);
                        //m_driverManager.getBackupDriver().writeBackupResourceContent(context.currentProject().getId(), offlineFileHeader, backupRes);
                    }
                    
                    m_driverManager.getProjectDriver().writePublishHistory(runtimeInfo, context.currentProject(), publishHistoryId, backupTagId, offlineFileHeader);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error writing backup/publishing history of " + newFile.toString(), e);
                    }

                    throw e;
                }
                report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
            }
        } catch (Exception e) {
            // this is a dummy try-catch block to have a finally clause here 

            if (C_DEBUG) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
            }

            throw e;
        } finally {
            // notify the app. that the published file and it's properties have been modified offline
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", offlineFileHeader)));
        }
    }
    
    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFileContent(I_CmsRuntimeInfo, org.opencms.file.CmsProject, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.Set)
     */
    public CmsFile publishFileContent(I_CmsRuntimeInfo runtimeInfo, CmsProject offlineProject, CmsProject onlineProject, CmsResource offlineFileHeader, Set publishedResourceIds) throws Exception {
        CmsFile newFile = null;
        CmsFile offlineFile = null;

        try {
            // binary content gets only published once while a project is published
            // if (!offlineFile.getContentId().isNullUUID() && !publishedContentIds.contains(offlineFile.getContentId())) {
            if (!publishedResourceIds.contains(offlineFileHeader.getResourceId())) {    
                // read the file content offline
                offlineFile = m_driverManager.getVfsDriver().readFile(runtimeInfo, offlineProject.getId(), false, offlineFileHeader.getStructureId());
                
                // create the file online              
                newFile = (CmsFile)offlineFile.clone();
                newFile.setState(I_CmsConstants.C_STATE_UNCHANGED);
               
                m_driverManager.getVfsDriver().createResource(runtimeInfo, onlineProject, newFile, newFile.getContents());

                // update the online/offline structure and resource records of the file
                m_driverManager.getVfsDriver().publishResource(runtimeInfo, onlineProject, newFile, offlineFile, false);

                // add the content ID to the content IDs that got already published
                // publishedContentIds.add(offlineFileHeader.getContentId());
                publishedResourceIds.add(offlineFile.getResourceId());
                
            } else {
                // create the sibling online
                m_driverManager.getVfsDriver().createSibling(
                    runtimeInfo, 
                    onlineProject, 
                    offlineFileHeader, offlineFileHeader.getName());

                newFile = m_driverManager.getVfsDriver().readFile(runtimeInfo, onlineProject.getId(), false, offlineFileHeader.getStructureId());
            }
        } catch (Exception e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error creating file content " + offlineFileHeader.toString(), e);
            }

            throw e;
        }
        
        return newFile;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFolder(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsFolder, boolean, long, org.opencms.util.CmsUUID, int, int)
     */
    public void publishFolder(CmsRequestContext context, I_CmsRuntimeInfo runtimeInfo, I_CmsReport report, int m, int n, CmsProject onlineProject, CmsFolder offlineFolder, boolean backupEnabled, long publishDate, CmsUUID publishHistoryId, int backupTagId, int maxVersions) throws Exception {
        CmsResource newFolder = null;
        CmsResource onlineFolder = null;
        List offlineProperties = null;

        try {
            report.print("( " + m + " / " + n + " ) " + report.key("report.publishing.folder"), I_CmsReport.C_FORMAT_NOTE);
            report.print(context.removeSiteRoot(offlineFolder.getRootPath()));
            report.print(report.key("report.dots"));

            if (offlineFolder.getState() == I_CmsConstants.C_STATE_NEW) {
                try {
                    // create the folder online
                    newFolder = (CmsFolder) offlineFolder.clone();
                    newFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);

                    onlineFolder = m_driverManager.getVfsDriver().createResource(runtimeInfo, onlineProject, newFolder, null);

                } catch (CmsException e) {
                    if (e.getType() == CmsException.C_FILE_EXISTS) {
                        try {
                            onlineFolder = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), newFolder.getStructureId());
                            m_driverManager.getVfsDriver().publishResource(runtimeInfo, onlineProject, onlineFolder, offlineFolder, false);
                        } catch (CmsException e1) {
                            if (OpenCms.getLog(this).isErrorEnabled()) {
                                OpenCms.getLog(this).error("Error reading resource " + offlineFolder.toString(), e1);
                            }

                            throw e1;
                        }
                    } else {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("Error creating resource " + offlineFolder.toString(), e);
                        }

                        throw e;
                    }
                }
            } else if (offlineFolder.getState() == I_CmsConstants.C_STATE_CHANGED) {
                try {
                    // read the folder online
                    onlineFolder = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), offlineFolder.getStructureId());
                } catch (CmsVfsResourceNotFoundException e) {
                    try {
                        onlineFolder = m_driverManager.getVfsDriver().createResource(runtimeInfo, onlineProject, offlineFolder, null);
                        onlineFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
                        m_driverManager.getVfsDriver().writeResourceState(runtimeInfo, context.currentProject(), onlineFolder, CmsDriverManager.C_UPDATE_ALL);
                    } catch (CmsException e1) {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("Error creating resource " + offlineFolder.toString(), e1);
                        }

                        throw e1;
                    }
                }

                try {
                    // update the folder online
                    m_driverManager.getVfsDriver().publishResource(runtimeInfo, onlineProject, onlineFolder, offlineFolder, false);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error updating resource " + offlineFolder.toString(), e);
                    }

                    throw e;
                }
            }
            
            try {
                // write the ACL online
                m_driverManager.getUserDriver().publishAccessControlEntries(runtimeInfo, context.currentProject(), onlineProject, offlineFolder.getResourceId(), onlineFolder.getResourceId());
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error writing ACLs of " + offlineFolder.toString(), e);
                }

                throw e;
            }

            try {
                // write the properties online
                m_driverManager.getVfsDriver().deletePropertyObjects(runtimeInfo, onlineProject.getId(), onlineFolder, CmsProperty.C_DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(context.currentProject(), offlineFolder);
                CmsProperty.setAutoCreatePropertyDefinitions(offlineProperties, true);
                m_driverManager.getVfsDriver().writePropertyObjects(runtimeInfo, onlineProject, onlineFolder, offlineProperties);
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error writing properties of " + offlineFolder.toString(), e);
                }

                throw e;
            }            

            try {
                // write the folder to the backup and publishing history
                if (backupEnabled) {
                    if (offlineProperties == null) {
                        offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(
                            context.currentProject(),
                            offlineFolder);
                    }

                    m_driverManager.getBackupDriver().writeBackupResource(
                        runtimeInfo,
                        context.currentUser(),
                        context.currentProject(),
                        offlineFolder,
                        offlineProperties,
                        backupTagId,
                        publishDate, maxVersions);
                }

                m_driverManager.getProjectDriver().writePublishHistory(
                    runtimeInfo,
                    context.currentProject(),
                    publishHistoryId,
                    backupTagId, offlineFolder);
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error writing backup/publishing history of " + offlineFolder.toString(), e);
                }

                throw e;
            }
            report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
        } catch (Exception e) {
            // this is a dummy try-catch block to have a finally clause here

            if (C_DEBUG) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
            }

            throw e;
        } finally {
            // notify the app. that the published folder and it's properties have been modified offline
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", offlineFolder)));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishProject(org.opencms.file.CmsRequestContext, I_CmsRuntimeInfo, org.opencms.report.I_CmsReport, org.opencms.file.CmsProject, org.opencms.db.CmsPublishList, boolean, int, int)
     */
    public synchronized void publishProject(
        CmsRequestContext context,
        I_CmsRuntimeInfo runtimeInfo,
        I_CmsReport report,
        CmsProject onlineProject,
        CmsPublishList publishList,
        boolean backupEnabled,
        int backupTagId, int maxVersions) throws Exception {

        CmsResource currentFileHeader = null;
        long publishDate = System.currentTimeMillis();
        Iterator i = null;
        int n;
        int publishedFolderCount = 0;
        int deletedFolderCount = 0;
        int publishedFileCount = 0;
        Set publishedContentIds = new HashSet();

        try {
            
            ////////////////////////////////////////////////////////////////////////////////////////

            // write the backup

            if (backupEnabled) {
                
                try {
                    // write an entry in the publish project log
                    m_driverManager.getBackupDriver().writeBackupProject(
                        runtimeInfo,
                        context.currentProject(),
                        backupTagId,
                        publishDate,
                        context.currentUser());
                    runtimeInfo.pop();
                } catch (Throwable t) {
                    runtimeInfo.report(report, "Error writing backup of project " + context.currentProject().getName(), t);
                }
                
            }

            ///////////////////////////////////////////////////////////////////////////////////////

            // publish new/changed folders

            publishedFolderCount = 0;
            n = publishList.getFolderList().size();
            i = publishList.getFolderList().iterator();

            if (n > 0) {
                report.println(report.key("report.publish_folders_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            }

            while (i.hasNext()) {
                CmsResource currentFolder = (CmsResource)i.next();

                try {
                    if (currentFolder.getState() == I_CmsConstants.C_STATE_NEW) {
    
                        // bounce the current publish task through all project drivers
                        m_driverManager.getProjectDriver().publishFolder(
                            context,
                            runtimeInfo,
                            report,
                            ++publishedFolderCount,
                            n,
                            onlineProject,
                            new CmsFolder(currentFolder),
                            backupEnabled,
                            publishDate,
                            publishList.getPublishHistoryId(),
                            backupTagId,
                            maxVersions);
    
                        // reset the resource state to UNCHANGED and the last-modified-in-project-ID to 0
                        internalResetResourceState(runtimeInfo, context, currentFolder);
    
                    } else if (currentFolder.getState() == I_CmsConstants.C_STATE_CHANGED) {
    
                        // bounce the current publish task through all project drivers
                        m_driverManager.getProjectDriver().publishFolder(
                            context,
                            runtimeInfo,
                            report,
                            ++publishedFolderCount,
                            n,
                            onlineProject,
                            new CmsFolder(currentFolder),
                            backupEnabled,
                            publishDate,
                            publishList.getPublishHistoryId(),
                            backupTagId,
                            maxVersions);
    
                        // reset the resource state to UNCHANGED and the last-modified-in-project-ID to 0
                        internalResetResourceState(runtimeInfo, context, currentFolder);
    
                    }
                    
                    runtimeInfo.pop();
                } catch (Throwable t) {
                    runtimeInfo.report(report, "Error publishing folder " + currentFolder.getRootPath(), t);
                }
            }

            if (n > 0) {
                report.println(report.key("report.publish_folders_end"), I_CmsReport.C_FORMAT_HEADLINE);
            }

            ///////////////////////////////////////////////////////////////////////////////////////

            // publish changed/new/deleted files

            publishedFileCount = 0;
            n = publishList.getFileList().size();
            i = publishList.getFileList().iterator();

            if (n > 0) {
                report.println(report.key("report.publish_files_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            }

            while (i.hasNext()) {
                
                currentFileHeader = (CmsResource)i.next();
                
                try {
                    // bounce the current publish task through all project drivers
                    m_driverManager.getProjectDriver().publishFile(
                        context,
                        runtimeInfo,
                        report,
                        ++publishedFileCount,
                        n,
                        onlineProject,
                        currentFileHeader,
                        publishedContentIds,
                        backupEnabled,
                        publishDate,
                        publishList.getPublishHistoryId(),
                        backupTagId,
                        maxVersions);
    
                    if (currentFileHeader.getState() != I_CmsConstants.C_STATE_DELETED) {
                        // reset the resource state to UNCHANGED and the last-modified-in-project-ID to 0
                        internalResetResourceState(runtimeInfo, context, currentFileHeader);
                    }
                    
                    runtimeInfo.pop();
                } catch (Throwable t) {
                    runtimeInfo.report(report, "Error publishing file " + currentFileHeader.getRootPath(), t);
                }

                // set back all vars. inside the while loop!
                currentFileHeader = null;
            }

            if (n > 0) {
                report.println(report.key("report.publish_files_end"), I_CmsReport.C_FORMAT_HEADLINE);
            }

            ////////////////////////////////////////////////////////////////////////////////////////

            // publish deleted folders
            List deletedFolders = publishList.getDeletedFolderList();
            if (deletedFolders.isEmpty()) {
                return;
            }

            deletedFolderCount = 0;
            n = deletedFolders.size();
            i = deletedFolders.iterator();

            if (n > 0) {
                report.println(report.key("report.publish_delete_folders_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            }

            while (i.hasNext()) {
                
                CmsResource currentFolder = (CmsResource)i.next();
                
                try {
                    // bounce the current publish task through all project drivers
                    m_driverManager.getProjectDriver().publishDeletedFolder(
                        context,
                        runtimeInfo,
                        report,
                        ++deletedFolderCount,
                        n,
                        onlineProject,
                        new CmsFolder(currentFolder),
                        backupEnabled,
                        publishDate,
                        publishList.getPublishHistoryId(),
                        backupTagId,
                        maxVersions);
                    
                    runtimeInfo.pop();
                } catch (Throwable t) {
                    runtimeInfo.report(report, "Error publishing deleted folder " + currentFolder.getRootPath(), t);
                }
            }

            if (n > 0) {
                report.println(report.key("report.publish_delete_folders_end"), I_CmsReport.C_FORMAT_HEADLINE);
            }
        } catch (Exception e) {
            // these are dummy catch blocks to have a finally block for clearing 
            // allocated resources. thus the exceptions are just logged and 
            // immediately thrown to the upper app. layer.

            if (C_DEBUG) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
            }

            throw e;
        } catch (OutOfMemoryError o) {
            if (OpenCms.getLog(this).isFatalEnabled()) {
                OpenCms.getLog(this).fatal("Out of memory error during publishing", o);
            }

            // clear all caches to reclaim memory
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP));

            // force a complete object finalization and garbage collection 
            System.runFinalization();
            Runtime.getRuntime().runFinalization();
            System.gc();
            Runtime.getRuntime().gc();

            throw o;
        } finally {
            
            currentFileHeader = null;

            StringBuffer stats = new StringBuffer();
            stats.append(report.key("report.publish_stats"));
            stats.append(report.key("report.publish_stats_files"));
            stats.append(publishedFileCount + ",");
            stats.append(report.key("report.publish_stats_folders"));
            stats.append(publishedFolderCount + ",");
            stats.append(report.key("report.publish_stats_deleted_folders"));
            stats.append(deletedFolderCount + ",");
            stats.append(report.key("report.publish_stats_duration"));
            stats.append(report.formatRuntime());

            if (OpenCms.getLog(this).isInfoEnabled()) {
                OpenCms.getLog(this).info(stats.toString());
            }

            report.println(stats.toString());
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProject(org.opencms.workflow.CmsTask)
     */
    public CmsProject readProject(CmsTask task) throws CmsException {
        PreparedStatement stmt = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYTASK");

            stmt.setInt(1, task.getId());
            res = stmt.executeQuery();

            if (res.next()) {
                project = new CmsProject(res, m_sqlManager);
            } else {
                // project not found!
                throw new CmsException("[" + this.getClass().getName() + "] " + task, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readProject(CmsTask)", CmsException.C_SQL_ERROR, e, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return project;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProject(int)
     */
    public CmsProject readProject(int id) throws CmsException {
        PreparedStatement stmt = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ");

            stmt.setInt(1, id);
            res = stmt.executeQuery();

            if (res.next()) {
                project =
                    new CmsProject(
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_ID")),
                        res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_NAME")),
                        res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_DESCRIPTION")),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_TASK_ID")),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_USER_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_GROUP_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_MANAGERGROUP_ID"))),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_FLAGS")),
                        res.getLong(m_sqlManager.readQuery("C_PROJECTS_DATE_CREATED")),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_TYPE")));
            } else {
                // project not found!
                throw m_sqlManager.getCmsException(this, "project with ID " + id + " not found", CmsException.C_NOT_FOUND, null, true);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readProject(int)/1 ", CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return project;
    }
    
    /**
     * @see I_CmsProjectDriver#readProject(String)
     */
    public CmsProject readProject(String name) throws CmsException {
        PreparedStatement stmt = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYNAME");

            stmt.setString(1, name);
            res = stmt.executeQuery();

            if (res.next()) {
                project =
                    new CmsProject(
                            res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_ID")),
                            res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_NAME")),
                            res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_DESCRIPTION")),
                            res.getInt(m_sqlManager.readQuery("C_PROJECTS_TASK_ID")),
                            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_USER_ID"))),
                            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_GROUP_ID"))),
                            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_MANAGERGROUP_ID"))),
                            res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_FLAGS")),
                            res.getLong(m_sqlManager.readQuery("C_PROJECTS_DATE_CREATED")),
                            res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_TYPE")));
            } else {
                // project not found!
                throw m_sqlManager.getCmsException(this, "project with name " + name + " not found", CmsException.C_NOT_FOUND, null, true);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readProject(String)/1 ", CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        
        return project;
    }    

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectLogs(int)
     */
    public List readProjectLogs(int projectid) throws CmsException {
        ResultSet res = null;
        Connection conn = null;

        CmsTaskLog tasklog = null;
        List logs = new ArrayList();
        PreparedStatement stmt = null;
        String comment = null;
        java.sql.Timestamp starttime = null;
        int id = I_CmsConstants.C_UNKNOWN_ID;
        CmsUUID user = CmsUUID.getNullUUID();
        int type = I_CmsConstants.C_UNKNOWN_ID;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKLOG_READ_PPROJECTLOGS");
            stmt.setInt(1, projectid);
            res = stmt.executeQuery();
            while (res.next()) {
                comment = res.getString(m_sqlManager.readQuery("C_LOG_COMMENT"));
                id = res.getInt(m_sqlManager.readQuery("C_LOG_ID"));
                starttime = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_LOG_STARTTIME"));
                user = new CmsUUID(res.getString(m_sqlManager.readQuery("C_LOG_USER")));
                type = res.getInt(m_sqlManager.readQuery("C_LOG_TYPE"));

                tasklog = new CmsTaskLog(id, comment, user, starttime, type);
                logs.add(tasklog);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return logs;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectResource(int, java.lang.String, java.lang.Object)
     */
    public String readProjectResource(int projectId, String resourcePath, Object reservedParam) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        String resName = null;
        
        // TODO: CW rename this since it only checks if a resource/project id is available
        // - but the returned resourcePath is already available when method is called

        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection();
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(((Integer) reservedParam).intValue());
            }
            
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ");

            // select resource from the database
            stmt.setInt(1, projectId);
            stmt.setString(2, resourcePath);
            res = stmt.executeQuery();

            if (res.next()) {
                resName = res.getString("RESOURCE_PATH");
            } else {
                throw new CmsException("[" + this.getClass().getName() + ".readProjectResource] " + resourcePath, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return resName;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectResources(org.opencms.file.CmsProject)
     */
    public List readProjectResources(CmsProject project) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        List result = new ArrayList();

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ_BY_ID");
            stmt.setInt(1, project.getId());
            res = stmt.executeQuery();

            while (res.next()) {
                result.add(res.getString("RESOURCE_PATH"));
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjects(int)
     */
    public List readProjects(int state) throws CmsException {
        List projects = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYFLAG");

            stmt.setInt(1, state);
            res = stmt.executeQuery();

            while (res.next()) { 
                projects.add(
                    new CmsProject(
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_ID")),
                        res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_NAME")),
                        res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_DESCRIPTION")),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_TASK_ID")),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_USER_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_GROUP_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_MANAGERGROUP_ID"))),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_FLAGS")),
                        res.getLong(m_sqlManager.readQuery("C_PROJECTS_DATE_CREATED")),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_TYPE"))));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, "getAllProjects(int)", CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        
        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectsForGroup(org.opencms.file.CmsGroup)
     */
    public Vector readProjectsForGroup(CmsGroup group) throws CmsException {
        Vector projects = new Vector();
        ResultSet res = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYGROUP");

            stmt.setString(1, group.getId().toString());
            stmt.setString(2, group.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                projects.addElement(new CmsProject(res, m_sqlManager));
            }
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectsForManagerGroup(org.opencms.file.CmsGroup)
     */
    public Vector readProjectsForManagerGroup(CmsGroup group) throws CmsException {
        Vector projects = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYMANAGER");

            stmt.setString(1, group.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                projects.addElement(new CmsProject(res, m_sqlManager));
            }
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectsForUser(org.opencms.file.CmsUser)
     */
    public List readProjectsForUser(CmsUser user) throws CmsException {
        Vector projects = new Vector();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYUSER");

            stmt.setString(1, user.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                projects.addElement(new CmsProject(res, m_sqlManager));
            }
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }
        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectView(int, java.lang.String)
     */
    public List readProjectView(int project, String filter) throws CmsException {
        List resources = new ArrayList();
        CmsResource currentResource = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String orderClause = " ORDER BY CMS_T_STRUCTURE.STRUCTURE_ID";
        String whereClause = new String();

        // TODO: dangerous - move this somehow into query.properties
        if ("new".equalsIgnoreCase(filter)) {
            whereClause = " AND (CMS_T_STRUCTURE.STRUCTURE_STATE=" + I_CmsConstants.C_STATE_NEW + " OR CMS_T_RESOURCES.RESOURCE_STATE=" + I_CmsConstants.C_STATE_NEW + ")";
        } else if ("changed".equalsIgnoreCase(filter)) {
            whereClause = " AND (CMS_T_STRUCTURE.STRUCTURE_STATE=" + I_CmsConstants.C_STATE_CHANGED + " OR CMS_T_RESOURCES.RESOURCE_STATE=" + I_CmsConstants.C_STATE_CHANGED + ")";
        } else if ("deleted".equalsIgnoreCase(filter)) {
            whereClause = " AND (CMS_T_STRUCTURE.STRUCTURE_STATE=" + I_CmsConstants.C_STATE_DELETED + " OR CMS_T_RESOURCES.RESOURCE_STATE=" + I_CmsConstants.C_STATE_DELETED + ")";
        } else if ("locked".equalsIgnoreCase(filter)) {
            whereClause = "";
        } else {
            whereClause = " AND (CMS_T_STRUCTURE.STRUCTURE_STATE!=" + I_CmsConstants.C_STATE_UNCHANGED + " OR CMS_T_RESOURCES.RESOURCE_STATE!=" + I_CmsConstants.C_STATE_UNCHANGED + ")";
        }

        try {
            // TODO make the getConnection and getPreparedStatement calls project-ID dependent
            conn = m_sqlManager.getConnection();
            String query = m_sqlManager.readQuery("C_RESOURCES_PROJECTVIEW") + whereClause + orderClause;
            stmt = m_sqlManager.getPreparedStatementForSql(conn, CmsSqlManager.replaceProjectPattern(I_CmsConstants.C_PROJECT_ONLINE_ID + 1, query));

            stmt.setInt(1, project);
            res = stmt.executeQuery();

            while (res.next()) {
                currentResource = m_driverManager.getVfsDriver().createResource(res, project);
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
     * @see org.opencms.db.I_CmsProjectDriver#readPublishedResources(int, org.opencms.util.CmsUUID)
     */
    public List readPublishedResources(int projectId, CmsUUID publishHistoryId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsUUID structureId = null;
        CmsUUID resourceId = null;
        String rootPath = null;
        int resourceType = I_CmsConstants.C_UNKNOWN_ID;
        int resourceState = I_CmsConstants.C_UNKNOWN_ID;
        List publishedResources = new ArrayList();
        int siblingCount = I_CmsConstants.C_UNKNOWN_ID;     
        int backupTagId = I_CmsConstants.C_UNKNOWN_ID;  

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SELECT_PUBLISHED_RESOURCES");
            stmt.setString(1, publishHistoryId.toString());
            res = stmt.executeQuery();
            
            while (res.next()) {
                structureId = new CmsUUID(res.getString("STRUCTURE_ID"));
                resourceId = new CmsUUID(res.getString("RESOURCE_ID"));                
                rootPath = res.getString("RESOURCE_PATH");
                resourceState = res.getInt("RESOURCE_STATE");
                resourceType = res.getInt("RESOURCE_TYPE");
                siblingCount = res.getInt("SIBLING_COUNT");
                backupTagId = res.getInt("PUBLISH_TAG");
                
                publishedResources.add(new CmsPublishedResource(structureId, resourceId, backupTagId, rootPath, resourceType, resourceState, siblingCount));
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }  
        
        return publishedResources;      
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readStaticExportResources(org.opencms.file.CmsProject, int, long)
     */
    public List readStaticExportResources(CmsProject currentProject, int parameterResources, long timestamp) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        List returnValue = new ArrayList();

        if (parameterResources == CmsStaticExportManager.C_EXPORT_LINK_WITHOUT_PARAMETER) {
            timestamp = 0;
        }
        try {
            conn = m_sqlManager.getConnection(currentProject);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_READ_ALL_PUBLISHED_LINKS");
            stmt.setInt(1, parameterResources);
            stmt.setLong(2, timestamp);
            res = stmt.executeQuery();
            // add all resourcenames to the list of return values
            while (res.next()) {
                returnValue.add(res.getString(1));               
            }           
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }  

        return returnValue;        
    }   
    
    
    /**
     * @see org.opencms.db.I_CmsProjectDriver#readStaticExportPublishedResourceParameters(org.opencms.file.CmsProject, java.lang.String)
     */
    public String readStaticExportPublishedResourceParameters(CmsProject currentProject, String rfsName) throws CmsException {
        String returnValue = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
      
                
        try {
            conn = m_sqlManager.getConnection(currentProject);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_READ_PUBLISHED_LINK_PARAMETERS");
            stmt.setString(1, rfsName);
            res = stmt.executeQuery();
            // add all resourcenames to the list of return values
            if (res.next()) {
                returnValue=res.getString(1);               
            }           
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }  

        return returnValue;       
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#unmarkProjectResources(I_CmsRuntimeInfo, org.opencms.file.CmsProject)
     */
    public void unmarkProjectResources(I_CmsRuntimeInfo runtimeInfo, CmsProject project) throws CmsException {
        // finally remove the project id form all resources 
  
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UNMARK");
            // create the statement
            stmt.setInt(1, project.getId());
            stmt.executeUpdate();
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writePublishHistory(I_CmsRuntimeInfo, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, int, org.opencms.file.CmsResource)
     */
    public void writePublishHistory(I_CmsRuntimeInfo runtimeInfo, CmsProject currentProject, CmsUUID publishId, int tagId, CmsResource resource) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(runtimeInfo);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_WRITE_PUBLISH_HISTORY");
            stmt.setInt(1, tagId);
            stmt.setString(2, resource.getStructureId().toString());
            stmt.setString(3, resource.getResourceId().toString());
            stmt.setString(4, resource.getRootPath());
            stmt.setInt(5, resource.getState());
            stmt.setInt(6, resource.getTypeId());
            stmt.setString(7, publishId.toString());
            stmt.setInt(8, resource.getSiblingCount());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(runtimeInfo, conn, stmt, null);
        }
    }   

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writeStaticExportPublishedResource(org.opencms.file.CmsProject, java.lang.String, int, java.lang.String, long)
     */
    public void writeStaticExportPublishedResource(CmsProject currentProject, String resourceName, int linkType, String linkParameter, long timestamp) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        int returnValue = 0;
        // first check if a record with this resource name does already exist
        try {
            conn = m_sqlManager.getConnection(currentProject);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_READ_PUBLISHED_RESOURCES");
            stmt.setString(1, resourceName);
            res = stmt.executeQuery();
            if (res.next()) {
                returnValue = res.getInt(1);
            }           
        } catch (SQLException e) {         
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(null, conn, stmt, res);
        }  
        
        // there was no entry found, so add it to the database
        if (returnValue == 0) {                      
            try {
                conn = m_sqlManager.getConnection(currentProject);
                stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_WRITE_PUBLISHED_LINKS");
                stmt.setString(1, new CmsUUID().toString());
                stmt.setString(2, resourceName);
                stmt.setInt(3, linkType);
                stmt.setString(4, linkParameter);
                stmt.setLong(5, timestamp);
                stmt.executeUpdate();
            } catch (SQLException e) {         
                throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
            } finally {
                m_sqlManager.closeAll(null, conn, stmt, null);
            }
        }
    }
}

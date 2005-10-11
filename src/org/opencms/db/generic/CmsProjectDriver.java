/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsProjectDriver.java,v $
 * Date   : $Date: 2005/10/11 14:45:50 $
 * Version: $Revision: 1.238.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Generic (ANSI-SQL) implementation of the project driver methods.<p>
 *
 * 
 * @author Thomas Weckert 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.238.2.2 $
 * 
 * @since 6.0.0 
 */
public class CmsProjectDriver implements I_CmsDriver, I_CmsProjectDriver {

    /** Table key for projects. */
    protected static final String TABLE_PROJECTS = "CMS_PROJECTS";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.generic.CmsProjectDriver.class);

    /** The driver manager. */
    protected CmsDriverManager m_driverManager;

    /** Array containing all max-ids for the tables. */
    protected int[] m_maxIds;

    /** The SQL manager. */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createProject(org.opencms.db.CmsDbContext, org.opencms.file.CmsUser, org.opencms.file.CmsGroup, org.opencms.file.CmsGroup, org.opencms.workflow.CmsTask, java.lang.String, java.lang.String, int, int, java.lang.Object)
     */
    public CmsProject createProject(
        CmsDbContext dbc,
        CmsUser owner,
        CmsGroup group,
        CmsGroup managergroup,
        CmsTask task,
        String name,
        String description,
        int flags,
        int type,
        Object reservedParam) throws CmsDataAccessException {

        CmsProject project = null;

        if ((description == null) || (description.length() < 1)) {
            description = " ";
        }

        long createTime = System.currentTimeMillis();
        Connection conn = null;
        PreparedStatement stmt = null;

        int id = CmsDbUtil.UNKNOWN_ID;

        try {
            if (reservedParam == null) {
                // get a new primary key ID
                id = m_sqlManager.nextId(TABLE_PROJECTS);
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection(dbc);
            } else {
                // use the primary key ID passed in the params Map
                id = ((Integer)((Map)reservedParam).get("pkProjectId")).intValue();
                // get a JDBC connection from the reserved JDBC pools
                int projectId = ((Integer)((Map)reservedParam).get("projectId")).intValue();
                conn = m_sqlManager.getConnection(dbc, projectId);
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

            project = new CmsProject(
                id,
                name,
                description,
                task.getId(),
                owner.getId(),
                group.getId(),
                managergroup.getId(),
                flags,
                createTime,
                type);
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }

        return project;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createProjectResource(org.opencms.db.CmsDbContext, int, java.lang.String, java.lang.Object)
     */
    public void createProjectResource(CmsDbContext dbc, int projectId, String resourcePath, Object reservedParam)
    throws CmsDataAccessException {

        // do not create entries for online-project
        PreparedStatement stmt = null;
        Connection conn = null;

        boolean projectResourceExists = false;
        try {
            readProjectResource(dbc, projectId, resourcePath, reservedParam);
            projectResourceExists = true;
        } catch (CmsVfsResourceNotFoundException e) {
            // resource does not exist yet, everything is okay 
            projectResourceExists = false;
        }

        if (projectResourceExists) {
            throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(
                Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                dbc.removeSiteRoot(resourcePath)));
        }

        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection(dbc);
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(dbc, ((Integer)reservedParam).intValue());
            }

            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_CREATE");

            // write new resource to the database
            stmt.setInt(1, projectId);
            stmt.setString(2, resourcePath);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteAllStaticExportPublishedResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, int)
     */
    public void deleteAllStaticExportPublishedResources(CmsDbContext dbc, CmsProject currentProject, int linkType)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_DELETE_ALL_PUBLISHED_LINKS");
            stmt.setInt(1, linkType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteProject(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public void deleteProject(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        // delete the resources from project_resources
        deleteProjectResources(dbc, project);

        // remove the project id form all resources within theis project
        unmarkProjectResources(dbc, project);

        // finally delete the project     
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_DELETE");
            // create the statement
            stmt.setInt(1, project.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteProjectResource(org.opencms.db.CmsDbContext, int, java.lang.String)
     */
    public void deleteProjectResource(CmsDbContext dbc, int projectId, String resourceName)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_DELETE");
            // delete resource from the database
            stmt.setInt(1, projectId);
            stmt.setString(2, resourceName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteProjectResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public void deleteProjectResources(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_DELETEALL");
            stmt.setInt(1, project.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishHistory(org.opencms.db.CmsDbContext, int, int)
     */
    public void deletePublishHistory(CmsDbContext dbc, int projectId, int maxBackupTagId) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, projectId, "C_DELETE_PUBLISH_HISTORY");
            stmt.setInt(1, maxBackupTagId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishHistoryEntry(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID, org.opencms.db.CmsPublishedResource)
     */
    public void deletePublishHistoryEntry(
        CmsDbContext dbc,
        int projectId,
        CmsUUID publishHistoryId,
        CmsPublishedResource publishedResource) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_DELETE_PUBLISH_HISTORY_ENTRY");
            stmt.setString(1, publishHistoryId.toString());
            stmt.setInt(2, publishedResource.getBackupTagId());
            stmt.setString(3, publishedResource.getStructureId().toString());
            stmt.setString(4, publishedResource.getResourceId().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#deleteStaticExportPublishedResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, java.lang.String, int, java.lang.String)
     */
    public void deleteStaticExportPublishedResource(
        CmsDbContext dbc,
        CmsProject currentProject,
        String resourceName,
        int linkType,
        String linkParameter) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_DELETE_PUBLISHED_LINKS");
            stmt.setString(1, resourceName);
            stmt.setInt(2, linkType);
            stmt.setString(3, linkParameter);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#destroy()
     */
    public void destroy() throws Throwable {

        finalize();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SHUTDOWN_DRIVER_1, getClass().getName()));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#fillDefaults(org.opencms.db.CmsDbContext)
     */
    public void fillDefaults(CmsDbContext dbc) throws CmsDataAccessException {

        try {
            if (readProject(dbc, CmsProject.ONLINE_PROJECT_ID) != null) {
                // online-project exists - no need of filling defaults
                return;
            }
        } catch (CmsDataAccessException exc) {
            // ignore the exception - the project was not readable so fill in the defaults
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_FILL_DEFAULTS_0));
        }

        String adminUser = OpenCms.getDefaultUsers().getUserAdmin();
        CmsUser admin = m_driverManager.readUser(dbc, adminUser);

        String administratorsGroup = OpenCms.getDefaultUsers().getGroupAdministrators();
        CmsGroup administrators = m_driverManager.readGroup(dbc, administratorsGroup);

        String usersGroup = OpenCms.getDefaultUsers().getGroupUsers();
        CmsGroup users = m_driverManager.readGroup(dbc, usersGroup);

        String projectmanagersGroup = OpenCms.getDefaultUsers().getGroupProjectmanagers();
        CmsGroup projectmanager = m_driverManager.readGroup(dbc, projectmanagersGroup);

        m_driverManager.getWorkflowDriver().writeTaskType(dbc, 1, 0, "../taskforms/adhoc.asp", "Ad-Hoc", "30308", 1, 1);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // online project stuff
        ////////////////////////////////////////////////////////////////////////////////////////////        

        // create the online project
        CmsTask task = m_driverManager.getWorkflowDriver().createTask(
            dbc,
            0,
            0,
            1,
            admin.getId(),
            admin.getId(),
            administrators.getId(),
            CmsProject.ONLINE_PROJECT_NAME,
            new java.sql.Timestamp(new java.util.Date().getTime()),
            new java.sql.Timestamp(new java.util.Date().getTime()),
            CmsTaskService.TASK_PRIORITY_NORMAL);
        CmsProject onlineProject = createProject(
            dbc,
            admin,
            users,
            projectmanager,
            task,
            CmsProject.ONLINE_PROJECT_NAME,
            "The Online Project",
            I_CmsPrincipal.FLAG_ENABLED,
            CmsProject.PROJECT_TYPE_NORMAL,
            null);

        // create the root-folder for the online project
        CmsFolder onlineRootFolder = new CmsFolder(
            new CmsUUID(),
            new CmsUUID(),
            "/",
            CmsResourceTypeFolder.RESOURCE_TYPE_ID,
            0,
            onlineProject.getId(),
            CmsResource.STATE_NEW,
            0,
            admin.getId(),
            0,
            admin.getId(),
            1,
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT);

        m_driverManager.getVfsDriver().createResource(dbc, onlineProject, onlineRootFolder, null);

        onlineRootFolder.setState(CmsResource.STATE_UNCHANGED);

        m_driverManager.getVfsDriver().writeResource(dbc, onlineProject, onlineRootFolder, CmsDriverManager.UPDATE_ALL);

        // important: must access through driver manager to ensure proper cascading
        m_driverManager.getProjectDriver().createProjectResource(
            dbc,
            onlineProject.getId(),
            onlineRootFolder.getRootPath(),
            null);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // setup project stuff
        ////////////////////////////////////////////////////////////////////////////////////////////

        // create the task for the setup project
        task = m_driverManager.getWorkflowDriver().createTask(
            dbc,
            0,
            0,
            1,
            admin.getId(),
            admin.getId(),
            administrators.getId(),
            "_setupProject",
            new java.sql.Timestamp(new java.util.Date().getTime()),
            new java.sql.Timestamp(new java.util.Date().getTime()),
            CmsTaskService.TASK_PRIORITY_NORMAL);

        // important: must access through driver manager to ensure proper cascading        
        CmsProject setupProject = m_driverManager.getProjectDriver().createProject(
            dbc,
            admin,
            administrators,
            administrators,
            task,
            "_setupProject",
            "Initial site import",
            I_CmsPrincipal.FLAG_ENABLED,
            CmsProject.PROJECT_TYPE_TEMPORARY,
            null);

        // create the root-folder for the offline project       
        CmsResource offlineRootFolder = m_driverManager.getVfsDriver().createResource(
            dbc,
            setupProject,
            onlineRootFolder,
            null);

        offlineRootFolder.setState(CmsResource.STATE_UNCHANGED);

        m_driverManager.getVfsDriver().writeResource(dbc, setupProject, offlineRootFolder, CmsDriverManager.UPDATE_ALL);

        // important: must access through driver manager to ensure proper cascading        
        m_driverManager.getProjectDriver().createProjectResource(
            dbc,
            setupProject.getId(),
            offlineRootFolder.getRootPath(),
            null);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#getSqlManager()
     */
    public CmsSqlManager getSqlManager() {

        return m_sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(org.opencms.db.CmsDbContext, org.opencms.configuration.CmsConfigurationManager, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(
        CmsDbContext dbc,
        CmsConfigurationManager configurationManager,
        List successiveDrivers,
        CmsDriverManager driverManager) {

        Map configuration = configurationManager.getConfiguration();
        String poolUrl = configuration.get("db.project.pool").toString();
        String classname = configuration.get("db.project.sqlmanager").toString();
        m_sqlManager = this.initSqlManager(classname);
        m_sqlManager.init(I_CmsProjectDriver.DRIVER_TYPE_ID, poolUrl);

        m_driverManager = driverManager;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_ASSIGNED_POOL_1, poolUrl));
        }

        if (successiveDrivers != null && !successiveDrivers.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_SUCCESSIVE_DRIVERS_UNSUPPORTED_1, getClass().getName()));
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
     * @see org.opencms.db.I_CmsProjectDriver#publishDeletedFolder(org.opencms.db.CmsDbContext, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsFolder, boolean, long, org.opencms.util.CmsUUID, int, int)
     */
    public void publishDeletedFolder(
        CmsDbContext dbc,
        I_CmsReport report,
        int m,
        int n,
        CmsProject onlineProject,
        CmsFolder currentFolder,
        boolean backupEnabled,
        long publishDate,
        CmsUUID publishHistoryId,
        int backupTagId,
        int maxVersions) throws CmsDataAccessException {

        CmsFolder onlineFolder = null;
        List offlineProperties = null;

        try {
            report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_2,
                String.valueOf(m),
                String.valueOf(n)), I_CmsReport.FORMAT_NOTE);
            report.print(Messages.get().container(Messages.RPT_DELETE_FOLDER_0), I_CmsReport.FORMAT_NOTE);
            report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                dbc.removeSiteRoot(currentFolder.getRootPath())));
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            try {
                // write the folder to the backup and publishing history                
                if (backupEnabled) {
                    offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(
                        dbc,
                        dbc.currentProject(),
                        currentFolder);
                    m_driverManager.getBackupDriver().writeBackupResource(
                        dbc,
                        currentFolder,
                        offlineProperties,
                        backupTagId,
                        publishDate,
                        maxVersions);
                }

                m_driverManager.getProjectDriver().writePublishHistory(
                    dbc,
                    dbc.currentProject(),
                    publishHistoryId,
                    new CmsPublishedResource(currentFolder, backupTagId));
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().key(Messages.LOG_WRITING_PUBLISHING_HISTORY_1, currentFolder.getRootPath()),
                        e);
                }
                throw e;
            }

            // read the folder online
            onlineFolder = m_driverManager.readFolder(dbc, currentFolder.getRootPath(), CmsResourceFilter.ALL);

            try {
                // delete the properties online and offline
                m_driverManager.getVfsDriver().deletePropertyObjects(
                    dbc,
                    onlineProject.getId(),
                    onlineFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                m_driverManager.getVfsDriver().deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getId(),
                    currentFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().key(Messages.LOG_DELETING_PROPERTIES_1, currentFolder.getRootPath()), e);
                }
                throw e;
            }

            try {
                // remove the folder online and offline
                m_driverManager.getVfsDriver().removeFolder(dbc, dbc.currentProject(), currentFolder);

                try {
                    m_driverManager.getVfsDriver().readFolder(
                        dbc,
                        dbc.currentProject().getId(),
                        currentFolder.getRootPath());
                } catch (CmsVfsResourceNotFoundException e) {
                    // remove the online folder only if it is really deleted offline
                    m_driverManager.getVfsDriver().removeFolder(dbc, onlineProject, currentFolder);
                }
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().key(Messages.LOG_REMOVING_RESOURCE_1, currentFolder.getRootPath()), e);
                }
                throw e;
            }

            try {
                // remove the ACL online and offline
                m_driverManager.getUserDriver().removeAccessControlEntries(
                    dbc,
                    onlineProject,
                    onlineFolder.getResourceId());
                m_driverManager.getUserDriver().removeAccessControlEntries(
                    dbc,
                    dbc.currentProject(),
                    currentFolder.getResourceId());
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().key(Messages.LOG_REMOVING_ACL_1, currentFolder.getRootPath()), e);
                }
                throw e;
            }

            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);

            if (LOG.isDebugEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(
                        Messages.LOG_DEL_FOLDER_3,
                        currentFolder.getRootPath(),
                        String.valueOf(m),
                        String.valueOf(n)));
                }
            }
        } finally {
            // notify the app. that the published folder and it's properties have been modified offline
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                Collections.singletonMap("resource", currentFolder)));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFile(org.opencms.db.CmsDbContext, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.Set, boolean, long, org.opencms.util.CmsUUID, int, int)
     */
    public void publishFile(
        CmsDbContext dbc,
        I_CmsReport report,
        int m,
        int n,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set publishedContentIds,
        boolean backupEnabled,
        long publishDate,
        CmsUUID publishHistoryId,
        int backupTagId,
        int maxVersions) throws CmsDataAccessException {

        CmsFile newFile = null;
        CmsResource onlineResource = null;
        List offlineProperties = null;
        CmsProperty property = null;
        int propertyDeleteOption = -1;

        /*
         * Never use onlineResource.getState() here!
         * Only use offlineResource.getState() to determine the state of an offline resource!
         * 
         * In case a resource has siblings, after a sibling was published the structure
         * and resource states are reset to UNCHANGED -> the state of the corresponding
         * onlineResource is still NEW, DELETED or CHANGED. 
         * Thus, using onlineResource.getState() will inevitably result in unpublished resources!
         */

        try {
            if (offlineResource.getState() == CmsResource.STATE_DELETED) {
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(m),
                    String.valueOf(n)), I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_DELETE_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                try {
                    // read the file header online
                    onlineResource = m_driverManager.getVfsDriver().readResource(
                        dbc,
                        onlineProject.getId(),
                        offlineResource.getStructureId(),
                        true);
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().key(Messages.LOG_READING_RESOURCE_1, offlineResource.getRootPath()), e);
                    }
                    throw e;
                }

                if (offlineResource.isLabeled() && !m_driverManager.labelResource(dbc, offlineResource, null, 2)) {
                    // update the resource flags to "unlabeled" of the siblings of the offline resource
                    int flags = offlineResource.getFlags();
                    flags &= ~CmsResource.FLAG_LABELED;
                    offlineResource.setFlags(flags);
                }

                try {
                    // write the file to the backup and publishing history
                    if (backupEnabled && !OpenCms.getSystemInfo().keepVersionHistory()) {
                        // delete all backups as well
                        m_driverManager.deleteBackup(dbc, offlineResource);
                    }
                    m_driverManager.getProjectDriver().writePublishHistory(
                        dbc,
                        dbc.currentProject(),
                        publishHistoryId,
                        new CmsPublishedResource(offlineResource, backupTagId));
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().key(
                            Messages.LOG_WRITING_PUBLISHING_HISTORY_1,
                            offlineResource.getRootPath()), e);
                    }
                    throw e;
                }

                try {
                    // delete the properties online and offline
                    if (offlineResource.getSiblingCount() > 1) {
                        // there are other siblings- delete only structure property values and keep the resource property values
                        propertyDeleteOption = CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES;
                    } else {
                        // there are no other siblings- delete both the structure and resource property values
                        propertyDeleteOption = CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                    }

                    m_driverManager.getVfsDriver().deletePropertyObjects(
                        dbc,
                        onlineProject.getId(),
                        onlineResource,
                        propertyDeleteOption);
                    m_driverManager.getVfsDriver().deletePropertyObjects(
                        dbc,
                        dbc.currentProject().getId(),
                        offlineResource,
                        propertyDeleteOption);

                    // if the offline file has a resource ID different from the online file
                    // (probably because a (deleted) file was replaced by a new file with the
                    // same name), the properties with the "old" resource ID have to be
                    // deleted also offline
                    if (!onlineResource.getResourceId().equals(offlineResource.getResourceId())) {
                        m_driverManager.getVfsDriver().deletePropertyObjects(
                            dbc,
                            dbc.currentProject().getId(),
                            onlineResource,
                            CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                    }
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().key(Messages.LOG_DELETING_PROPERTIES_1, offlineResource.getRootPath()),
                            e);
                    }
                    throw e;
                }

                try {
                    // remove the file online and offline
                    m_driverManager.getVfsDriver().removeFile(dbc, dbc.currentProject(), offlineResource, true);

                    try {
                        m_driverManager.getVfsDriver().readResource(
                            dbc,
                            dbc.currentProject().getId(),
                            offlineResource.getStructureId(),
                            true);
                    } catch (CmsVfsResourceNotFoundException e) {
                        // remove the online file only if it is really deleted offline
                        m_driverManager.getVfsDriver().removeFile(dbc, onlineProject, onlineResource, true);
                    }
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().key(Messages.LOG_REMOVING_RESOURCE_1, offlineResource.getRootPath()),
                            e);
                    }
                    throw e;
                }

                try {
                    // delete the ACL online and offline
                    m_driverManager.getUserDriver().removeAccessControlEntries(
                        dbc,
                        onlineProject,
                        onlineResource.getResourceId());
                    m_driverManager.getUserDriver().removeAccessControlEntries(
                        dbc,
                        dbc.currentProject(),
                        offlineResource.getResourceId());
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().key(Messages.LOG_REMOVING_ACL_1, offlineResource.toString()), e);
                    }
                    throw e;
                }
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(
                        Messages.LOG_DEL_FILE_3,
                        offlineResource.getRootPath(),
                        String.valueOf(m),
                        String.valueOf(n)));
                }

            } else if (offlineResource.getState() == CmsResource.STATE_CHANGED) {
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(m),
                    String.valueOf(n)), I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_PUBLISH_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                try {
                    // read the file header online                   
                    onlineResource = m_driverManager.getVfsDriver().readResource(
                        dbc,
                        onlineProject.getId(),
                        offlineResource.getStructureId(),
                        false);

                    // reset the labeled link flag before writing the online file
                    int flags = offlineResource.getFlags();
                    flags &= ~CmsResource.FLAG_LABELED;
                    offlineResource.setFlags(flags);

                    // delete the properties online
                    m_driverManager.getVfsDriver().deletePropertyObjects(
                        dbc,
                        onlineProject.getId(),
                        onlineResource,
                        CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

                    // if the offline file has a resource ID different from the online file
                    // (probably because a deleted file was replaced by a new file with the
                    // same name), the properties mapped to the "old" resource ID have to be
                    // deleted also offline. if this is the case, the online and offline structure
                    // ID's do match, but the resource ID's are different. structure IDs are reused
                    // to prevent orphan structure records in the online project.

                    if (!onlineResource.getResourceId().equals(offlineResource.getResourceId())) {
                        offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(
                            dbc,
                            dbc.currentProject(),
                            onlineResource);
                        if (offlineProperties.size() > 0) {
                            for (int i = 0; i < offlineProperties.size(); i++) {
                                property = (CmsProperty)offlineProperties.get(i);
                                property.setStructureValue(null);
                                property.setResourceValue(CmsProperty.DELETE_VALUE);
                            }
                            m_driverManager.getVfsDriver().writePropertyObjects(
                                dbc,
                                dbc.currentProject(),
                                onlineResource,
                                offlineProperties);
                        }
                    }

                    // remove the file online
                    boolean removeContent = !publishedContentIds.contains(offlineResource.getResourceId());
                    m_driverManager.getVfsDriver().removeFile(dbc, onlineProject, onlineResource, removeContent);
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().key(Messages.LOG_DELETING_PROPERTIES_1, offlineResource.toString()), e);
                    }

                    throw e;
                }

                try {
                    // publish the file content
                    newFile = m_driverManager.getProjectDriver().publishFileContent(
                        dbc,
                        dbc.currentProject(),
                        onlineProject,
                        offlineResource,
                        publishedContentIds);
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().key(Messages.LOG_PUBLISHING_RESOURCE_1, offlineResource.getRootPath()),
                            e);
                    }
                    throw e;
                }

                try {
                    // write the properties online
                    offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(
                        dbc,
                        dbc.currentProject(),
                        offlineResource);
                    CmsProperty.setAutoCreatePropertyDefinitions(offlineProperties, true);
                    m_driverManager.getVfsDriver().writePropertyObjects(dbc, onlineProject, newFile, offlineProperties);
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().key(Messages.LOG_PUBLISHING_PROPERTIES_1, newFile.getRootPath()), e);
                    }
                    throw e;
                }

                try {
                    // write the ACL online
                    m_driverManager.getUserDriver().publishAccessControlEntries(
                        dbc,
                        dbc.currentProject(),
                        onlineProject,
                        newFile.getResourceId(),
                        onlineResource.getResourceId());
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().key(Messages.LOG_PUBLISHING_ACL_1, newFile.getRootPath()), e);
                    }
                    throw e;
                }

                try {
                    // write the file to the backup and publishing history
                    if (backupEnabled) {
                        if (offlineProperties == null) {
                            offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(
                                dbc,
                                dbc.currentProject(),
                                offlineResource);
                        }
                        m_driverManager.getBackupDriver().writeBackupResource(
                            dbc,
                            newFile,
                            offlineProperties,
                            backupTagId,
                            publishDate,
                            maxVersions);
                    }
                    m_driverManager.getProjectDriver().writePublishHistory(
                        dbc,
                        dbc.currentProject(),
                        publishHistoryId,
                        new CmsPublishedResource(offlineResource, backupTagId));
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().key(Messages.LOG_WRITING_PUBLISHING_HISTORY_1, newFile.getRootPath()),
                            e);
                    }
                    throw e;
                }
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(
                        Messages.LOG_PUBLISHING_FILE_3,
                        offlineResource.getRootPath(),
                        String.valueOf(m),
                        String.valueOf(n)));
                }

            } else if (offlineResource.getState() == CmsResource.STATE_NEW) {
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(m),
                    String.valueOf(n)), I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_PUBLISH_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                try {
                    // reset the labeled link flag before writing the online file
                    int flags = offlineResource.getFlags();
                    flags &= ~CmsResource.FLAG_LABELED;
                    offlineResource.setFlags(flags);

                    // publish the file content
                    newFile = m_driverManager.getProjectDriver().publishFileContent(
                        dbc,
                        dbc.currentProject(),
                        onlineProject,
                        offlineResource,
                        publishedContentIds);
                } catch (CmsVfsResourceAlreadyExistsException e) {
                    try {
                        // remove the existing file and ensure that it's content is written 
                        // in any case by removing it's content ID from the set of published content IDs
                        m_driverManager.getVfsDriver().removeFile(dbc, onlineProject, offlineResource, true);
                        publishedContentIds.remove(offlineResource.getResourceId());
                        newFile = m_driverManager.getProjectDriver().publishFileContent(
                            dbc,
                            dbc.currentProject(),
                            onlineProject,
                            offlineResource,
                            publishedContentIds);
                    } catch (CmsDataAccessException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(Messages.get().key(
                                Messages.LOG_PUBLISHING_RESOURCE_1,
                                offlineResource.getRootPath()), e);
                        }
                        throw e1;
                    }
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().key(Messages.LOG_PUBLISHING_RESOURCE_1, offlineResource.getRootPath()),
                            e);
                    }
                    throw e;
                }

                try {
                    // write the properties online
                    offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(
                        dbc,
                        dbc.currentProject(),
                        offlineResource);
                    CmsProperty.setAutoCreatePropertyDefinitions(offlineProperties, true);
                    m_driverManager.getVfsDriver().writePropertyObjects(dbc, onlineProject, newFile, offlineProperties);
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().key(Messages.LOG_PUBLISHING_PROPERTIES_1, newFile.getRootPath()), e);
                    }

                    throw e;
                }

                try {
                    // write the ACL online
                    m_driverManager.getUserDriver().publishAccessControlEntries(
                        dbc,
                        dbc.currentProject(),
                        onlineProject,
                        offlineResource.getResourceId(),
                        newFile.getResourceId());
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().key(Messages.LOG_PUBLISHING_ACL_1, newFile.getRootPath()), e);
                    }

                    throw e;
                }

                try {
                    // write the file to the backup and publishing history
                    if (backupEnabled) {
                        if (offlineProperties == null) {
                            offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(
                                dbc,
                                dbc.currentProject(),
                                offlineResource);
                        }
                        m_driverManager.getBackupDriver().writeBackupResource(
                            dbc,
                            newFile,
                            offlineProperties,
                            backupTagId,
                            publishDate,
                            maxVersions);
                    }

                    m_driverManager.getProjectDriver().writePublishHistory(
                        dbc,
                        dbc.currentProject(),
                        publishHistoryId,
                        new CmsPublishedResource(offlineResource, backupTagId));
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().key(Messages.LOG_WRITING_PUBLISHING_HISTORY_1, newFile.getRootPath()),
                            e);
                    }

                    throw e;
                }
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().key(
                            Messages.LOG_PUBLISHING_FILE_3,
                            offlineResource.getRootPath(),
                            String.valueOf(m),
                            String.valueOf(n)));
                    }
                }
            }
        } finally {
            // notify the app. that the published file and it's properties have been modified offline
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                Collections.singletonMap("resource", offlineResource)));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFileContent(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.Set)
     */
    public CmsFile publishFileContent(
        CmsDbContext dbc,
        CmsProject offlineProject,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set publishedResourceIds) throws CmsDataAccessException {

        CmsFile newFile = null;
        CmsFile offlineFile = null;

        try {
            // binary content gets only published once while a project is published
            if (!publishedResourceIds.contains(offlineResource.getResourceId())) {
                // read the file content offline
                offlineFile = m_driverManager.getVfsDriver().readFile(
                    dbc,
                    offlineProject.getId(),
                    false,
                    offlineResource.getStructureId());

                // create the file online              
                newFile = (CmsFile)offlineFile.clone();
                newFile.setState(CmsResource.STATE_UNCHANGED);

                m_driverManager.getVfsDriver().createResource(dbc, onlineProject, newFile, newFile.getContents());

                // update the online/offline structure and resource records of the file
                m_driverManager.getVfsDriver().publishResource(dbc, onlineProject, newFile, offlineFile, false);

                // add the content ID to the content IDs that got already published
                // publishedContentIds.add(offlineFileHeader.getContentId());
                publishedResourceIds.add(offlineFile.getResourceId());

            } else {
                // create the sibling online
                m_driverManager.getVfsDriver().createSibling(dbc, onlineProject, offlineResource);

                newFile = m_driverManager.getVfsDriver().readFile(
                    dbc,
                    onlineProject.getId(),
                    false,
                    offlineResource.getStructureId());
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(Messages.LOG_PUBLISHING_FILE_CONTENT_1, offlineResource.toString()), e);
            }

            throw e;
        }

        return newFile;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFolder(org.opencms.db.CmsDbContext, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsFolder, boolean, long, org.opencms.util.CmsUUID, int, int)
     */
    public void publishFolder(
        CmsDbContext dbc,
        I_CmsReport report,
        int m,
        int n,
        CmsProject onlineProject,
        CmsFolder offlineFolder,
        boolean backupEnabled,
        long publishDate,
        CmsUUID publishHistoryId,
        int backupTagId,
        int maxVersions) throws CmsDataAccessException {

        CmsResource newFolder = null;
        CmsResource onlineFolder = null;
        List offlineProperties = null;

        try {
            report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_2,
                String.valueOf(m),
                String.valueOf(n)), I_CmsReport.FORMAT_NOTE);
            report.print(Messages.get().container(Messages.RPT_PUBLISH_FOLDER_0), I_CmsReport.FORMAT_NOTE);
            report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                dbc.removeSiteRoot(offlineFolder.getRootPath())));
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            if (offlineFolder.getState() == CmsResource.STATE_NEW) {

                try {
                    // create the folder online
                    newFolder = (CmsFolder)offlineFolder.clone();
                    newFolder.setState(CmsResource.STATE_UNCHANGED);

                    onlineFolder = m_driverManager.getVfsDriver().createResource(dbc, onlineProject, newFolder, null);
                } catch (CmsVfsResourceAlreadyExistsException e) {
                    try {
                        onlineFolder = m_driverManager.getVfsDriver().readFolder(
                            dbc,
                            onlineProject.getId(),
                            newFolder.getStructureId());
                        m_driverManager.getVfsDriver().publishResource(
                            dbc,
                            onlineProject,
                            onlineFolder,
                            offlineFolder,
                            false);
                    } catch (CmsDataAccessException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(
                                Messages.get().key(Messages.LOG_READING_RESOURCE_1, offlineFolder.getRootPath()),
                                e);
                        }
                        throw e1;
                    }
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().key(Messages.LOG_PUBLISHING_RESOURCE_1, offlineFolder.getRootPath()),
                            e);
                    }
                    throw e;
                }

            } else if (offlineFolder.getState() == CmsResource.STATE_CHANGED) {

                try {
                    // read the folder online
                    onlineFolder = m_driverManager.getVfsDriver().readFolder(
                        dbc,
                        onlineProject.getId(),
                        offlineFolder.getStructureId());
                } catch (CmsVfsResourceNotFoundException e) {
                    try {
                        onlineFolder = m_driverManager.getVfsDriver().createResource(
                            dbc,
                            onlineProject,
                            offlineFolder,
                            null);
                        onlineFolder.setState(CmsResource.STATE_UNCHANGED);
                        m_driverManager.getVfsDriver().writeResourceState(
                            dbc,
                            dbc.currentProject(),
                            onlineFolder,
                            CmsDriverManager.UPDATE_ALL);
                    } catch (CmsDataAccessException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(Messages.get().key(
                                Messages.LOG_PUBLISHING_RESOURCE_1,
                                offlineFolder.getRootPath()), e);
                        }
                        throw e1;
                    }
                }

                try {
                    // update the folder online
                    m_driverManager.getVfsDriver().publishResource(
                        dbc,
                        onlineProject,
                        onlineFolder,
                        offlineFolder,
                        false);
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().key(Messages.LOG_PUBLISHING_RESOURCE_1, offlineFolder.getRootPath()),
                            e);
                    }
                    throw e;
                }
            }

            try {
                // write the ACL online
                m_driverManager.getUserDriver().publishAccessControlEntries(
                    dbc,
                    dbc.currentProject(),
                    onlineProject,
                    offlineFolder.getResourceId(),
                    onlineFolder.getResourceId());
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().key(Messages.LOG_PUBLISHING_ACL_1, offlineFolder.getRootPath()), e);
                }
                throw e;
            }

            try {
                // write the properties online
                m_driverManager.getVfsDriver().deletePropertyObjects(
                    dbc,
                    onlineProject.getId(),
                    onlineFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(
                    dbc,
                    dbc.currentProject(),
                    offlineFolder);
                CmsProperty.setAutoCreatePropertyDefinitions(offlineProperties, true);
                m_driverManager.getVfsDriver().writePropertyObjects(dbc, onlineProject, onlineFolder, offlineProperties);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().key(Messages.LOG_PUBLISHING_PROPERTIES_1, offlineFolder.getRootPath()), e);
                }
                throw e;
            }

            try {
                // write the folder to the backup and publishing history
                if (backupEnabled) {
                    if (offlineProperties == null) {
                        offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(
                            dbc,
                            dbc.currentProject(),
                            offlineFolder);
                    }

                    m_driverManager.getBackupDriver().writeBackupResource(
                        dbc,
                        offlineFolder,
                        offlineProperties,
                        backupTagId,
                        publishDate,
                        maxVersions);
                }

                m_driverManager.getProjectDriver().writePublishHistory(
                    dbc,
                    dbc.currentProject(),
                    publishHistoryId,
                    new CmsPublishedResource(offlineFolder, backupTagId));
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().key(Messages.LOG_WRITING_PUBLISHING_HISTORY_1, offlineFolder.getRootPath()),
                        e);
                }
                throw e;
            }
            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(
                    Messages.LOG_PUBLISHING_FOLDER_3,
                    offlineFolder.getRootPath(),
                    String.valueOf(m),
                    String.valueOf(n)));
            }
        } finally {
            // notify the app. that the published folder and it's properties have been modified offline
            OpenCms.fireCmsEvent(new CmsEvent(
                I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                Collections.singletonMap("resource", offlineFolder)));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishProject(org.opencms.db.CmsDbContext, org.opencms.report.I_CmsReport, org.opencms.file.CmsProject, org.opencms.db.CmsPublishList, boolean, int, int)
     */
    public synchronized void publishProject(
        CmsDbContext dbc,
        I_CmsReport report,
        CmsProject onlineProject,
        CmsPublishList publishList,
        boolean backupEnabled,
        int backupTagId,
        int maxVersions) throws CmsException {

        CmsResource currentResource = null;
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
                    m_driverManager.getBackupDriver().writeBackupProject(dbc, backupTagId, publishDate);
                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(report, Messages.get().container(
                        Messages.ERR_WRITING_BACKUP_OF_PROJECT_1,
                        dbc.currentProject().getName()), t);
                }
            }

            ///////////////////////////////////////////////////////////////////////////////////////

            // publish new/changed folders

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(
                    Messages.LOG_START_PUBLISHING_PROJECT_2,
                    dbc.currentProject().getName(),
                    dbc.currentUser().getName()));
            }

            publishedFolderCount = 0;
            n = publishList.getFolderList().size();
            i = publishList.getFolderList().iterator();

            if (n > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_FOLDERS_BEGIN_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }

            while (i.hasNext()) {
                CmsResource currentFolder = (CmsResource)i.next();

                try {
                    if (currentFolder.getState() == CmsResource.STATE_NEW) {

                        // bounce the current publish task through all project drivers
                        m_driverManager.getProjectDriver().publishFolder(
                            dbc,
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
                        internalResetResourceState(dbc, currentFolder);

                    } else if (currentFolder.getState() == CmsResource.STATE_CHANGED) {

                        // bounce the current publish task through all project drivers
                        m_driverManager.getProjectDriver().publishFolder(
                            dbc,
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
                        internalResetResourceState(dbc, currentFolder);

                    }

                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(report, Messages.get().container(
                        Messages.ERR_ERROR_PUBLISHING_FOLDER_1,
                        currentFolder.getRootPath()), t);
                }
            }

            if (n > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_FOLDERS_END_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }

            ///////////////////////////////////////////////////////////////////////////////////////

            // publish changed/new/deleted files

            publishedFileCount = 0;
            n = publishList.getFileList().size();
            i = publishList.getFileList().iterator();

            if (n > 0) {
                report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_FILES_BEGIN_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }

            while (i.hasNext()) {

                currentResource = (CmsResource)i.next();

                try {
                    // bounce the current publish task through all project drivers
                    m_driverManager.getProjectDriver().publishFile(
                        dbc,
                        report,
                        ++publishedFileCount,
                        n,
                        onlineProject,
                        currentResource,
                        publishedContentIds,
                        backupEnabled,
                        publishDate,
                        publishList.getPublishHistoryId(),
                        backupTagId,
                        maxVersions);

                    if (currentResource.getState() != CmsResource.STATE_DELETED) {
                        // reset the resource state to UNCHANGED and the last-modified-in-project-ID to 0
                        internalResetResourceState(dbc, currentResource);
                    }

                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(report, Messages.get().container(
                        Messages.ERR_ERROR_PUBLISHING_FILE_1,
                        currentResource.getRootPath()), t);
                }

                // set back all vars. inside the while loop!
                currentResource = null;
            }

            if (n > 0) {
                report.println(Messages.get().container(Messages.RPT_PUBLISH_FILES_END_0), I_CmsReport.FORMAT_HEADLINE);
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
                report.println(
                    Messages.get().container(Messages.RPT_DELETE_FOLDERS_BEGIN_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }

            while (i.hasNext()) {

                CmsResource currentFolder = (CmsResource)i.next();

                try {
                    // bounce the current publish task through all project drivers
                    m_driverManager.getProjectDriver().publishDeletedFolder(
                        dbc,
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

                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(report, Messages.get().container(
                        Messages.ERR_ERROR_PUBLISHING_DELETED_FOLDER_1,
                        currentFolder.getRootPath()), t);
                }
            }

            if (n > 0) {
                report.println(Messages.get().container(Messages.RPT_DELETE_FOLDERS_END_0), I_CmsReport.FORMAT_HEADLINE);
            }
        } catch (OutOfMemoryError o) {
            // clear all caches to reclaim memory
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP));

            CmsMessageContainer message = Messages.get().container(Messages.ERR_OUT_OF_MEMORY_0);
            if (LOG.isErrorEnabled()) {
                LOG.error(message.key(), o);
            }
            throw new CmsDataAccessException(message, o);

        } finally {

            currentResource = null;
            Object[] msgArgs = new Object[] {
                String.valueOf(publishedFileCount),
                String.valueOf(publishedFolderCount),
                String.valueOf(deletedFolderCount),
                report.formatRuntime()};
            CmsMessageContainer message = Messages.get().container(Messages.RPT_PUBLISH_STAT_4, msgArgs);
            if (LOG.isInfoEnabled()) {
                LOG.info(message.key());
            }
            report.println(message);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProject(org.opencms.db.CmsDbContext, int)
     */
    public CmsProject readProject(CmsDbContext dbc, int id) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ");

            stmt.setInt(1, id);
            res = stmt.executeQuery();

            if (res.next()) {
                project = new CmsProject(
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
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_NO_PROJECT_WITH_ID_1,
                    String.valueOf(id)));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return project;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProject(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public CmsProject readProject(CmsDbContext dbc, String name) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYNAME");

            stmt.setString(1, name);
            res = stmt.executeQuery();

            if (res.next()) {
                project = new CmsProject(
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
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_NO_PROJECT_WITH_NAME_1,
                    name));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return project;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectResource(org.opencms.db.CmsDbContext, int, java.lang.String, java.lang.Object)
     */
    public String readProjectResource(CmsDbContext dbc, int projectId, String resourcePath, Object reservedParam)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        String resName = null;

        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
                conn = m_sqlManager.getConnection(dbc);
            } else {
                // get a JDBC connection from the reserved JDBC pools
                conn = m_sqlManager.getConnection(dbc, ((Integer)reservedParam).intValue());
            }

            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ");

            // select resource from the database
            stmt.setInt(1, projectId);
            stmt.setString(2, resourcePath);
            res = stmt.executeQuery();

            if (res.next()) {
                resName = res.getString("RESOURCE_PATH");
            } else {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_NO_PROJECTRESOURCE_1,
                    resourcePath));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return resName;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public List readProjectResources(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        List result = new ArrayList();

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ_BY_ID");
            stmt.setInt(1, project.getId());
            res = stmt.executeQuery();

            while (res.next()) {
                result.add(res.getString("RESOURCE_PATH"));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjects(org.opencms.db.CmsDbContext, int)
     */
    public List readProjects(CmsDbContext dbc, int state) throws CmsDataAccessException {

        List projects = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYFLAG");

            stmt.setInt(1, state);
            res = stmt.executeQuery();

            while (res.next()) {
                projects.add(new CmsProject(
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
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectsForGroup(org.opencms.db.CmsDbContext, org.opencms.file.CmsGroup)
     */
    public List readProjectsForGroup(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException {

        List projects = new ArrayList();
        ResultSet res = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYGROUP");

            stmt.setString(1, group.getId().toString());
            stmt.setString(2, group.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                projects.add(new CmsProject(res, m_sqlManager));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectsForManagerGroup(org.opencms.db.CmsDbContext, org.opencms.file.CmsGroup)
     */
    public List readProjectsForManagerGroup(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException {

        List projects = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYMANAGER");

            stmt.setString(1, group.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                projects.add(new CmsProject(res, m_sqlManager));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectsForUser(org.opencms.db.CmsDbContext, org.opencms.file.CmsUser)
     */
    public List readProjectsForUser(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException {

        List projects = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYUSER");

            stmt.setString(1, user.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                projects.add(new CmsProject(res, m_sqlManager));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return (projects);
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectView(org.opencms.db.CmsDbContext, int, java.lang.String)
     */
    public List readProjectView(CmsDbContext dbc, int project, String filter) throws CmsDataAccessException {

        List resources = new ArrayList();
        CmsResource currentResource = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String orderClause = " ORDER BY CMS_T_STRUCTURE.STRUCTURE_ID";
        String whereClause = new String();

        // TODO: dangerous - move this somehow into query.properties
        if ("new".equalsIgnoreCase(filter)) {
            whereClause = " AND (CMS_T_STRUCTURE.STRUCTURE_STATE="
                + CmsResource.STATE_NEW
                + " OR CMS_T_RESOURCES.RESOURCE_STATE="
                + CmsResource.STATE_NEW
                + ")";
        } else if ("changed".equalsIgnoreCase(filter)) {
            whereClause = " AND (CMS_T_STRUCTURE.STRUCTURE_STATE="
                + CmsResource.STATE_CHANGED
                + " OR CMS_T_RESOURCES.RESOURCE_STATE="
                + CmsResource.STATE_CHANGED
                + ")";
        } else if ("deleted".equalsIgnoreCase(filter)) {
            whereClause = " AND (CMS_T_STRUCTURE.STRUCTURE_STATE="
                + CmsResource.STATE_DELETED
                + " OR CMS_T_RESOURCES.RESOURCE_STATE="
                + CmsResource.STATE_DELETED
                + ")";
        } else if ("locked".equalsIgnoreCase(filter)) {
            whereClause = "";
        } else {
            whereClause = " AND (CMS_T_STRUCTURE.STRUCTURE_STATE!="
                + CmsResource.STATE_UNCHANGED
                + " OR CMS_T_RESOURCES.RESOURCE_STATE!="
                + CmsResource.STATE_UNCHANGED
                + ")";
        }

        try {
            // TODO make the getConnection and getPreparedStatement calls project-ID dependent
            conn = m_sqlManager.getConnection(dbc);
            String query = m_sqlManager.readQuery("C_RESOURCES_PROJECTVIEW") + whereClause + orderClause;
            stmt = m_sqlManager.getPreparedStatementForSql(conn, CmsSqlManager.replaceProjectPattern(
                CmsProject.ONLINE_PROJECT_ID + 1,
                query));

            stmt.setInt(1, project);
            res = stmt.executeQuery();

            while (res.next()) {
                currentResource = m_driverManager.getVfsDriver().createResource(res, project);
                resources.add(currentResource);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readPublishedResources(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID)
     */
    public List readPublishedResources(CmsDbContext dbc, int projectId, CmsUUID publishHistoryId)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsUUID structureId = null;
        CmsUUID resourceId = null;
        String rootPath = null;
        int resourceType = CmsDbUtil.UNKNOWN_ID;
        int resourceState = CmsDbUtil.UNKNOWN_ID;
        List publishedResources = new ArrayList();
        int siblingCount = CmsDbUtil.UNKNOWN_ID;
        int backupTagId = CmsDbUtil.UNKNOWN_ID;

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
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

                publishedResources.add(new CmsPublishedResource(
                    structureId,
                    resourceId,
                    backupTagId,
                    rootPath,
                    resourceType,
                    structureId.isNullUUID() ? false : CmsFolder.isFolderType(resourceType),
                    resourceState,
                    siblingCount));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return publishedResources;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readStaticExportPublishedResourceParameters(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, java.lang.String)
     */
    public String readStaticExportPublishedResourceParameters(
        CmsDbContext dbc,
        CmsProject currentProject,
        String rfsName) throws CmsDataAccessException {

        String returnValue = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_READ_PUBLISHED_LINK_PARAMETERS");
            stmt.setString(1, rfsName);
            res = stmt.executeQuery();
            // add all resourcenames to the list of return values
            if (res.next()) {
                returnValue = res.getString(1);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return returnValue;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readStaticExportResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, int, long)
     */
    public List readStaticExportResources(
        CmsDbContext dbc,
        CmsProject currentProject,
        int parameterResources,
        long timestamp) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        List returnValue = new ArrayList();

        if (parameterResources == CmsStaticExportManager.EXPORT_LINK_WITHOUT_PARAMETER) {
            timestamp = 0;
        }
        try {
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_READ_ALL_PUBLISHED_LINKS");
            stmt.setInt(1, parameterResources);
            stmt.setLong(2, timestamp);
            res = stmt.executeQuery();
            // add all resourcenames to the list of return values
            while (res.next()) {
                returnValue.add(res.getString(1));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return returnValue;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#unmarkProjectResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public void unmarkProjectResources(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        // finally remove the project id form all resources 

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc, project.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UNMARK");
            // create the statement
            stmt.setInt(1, project.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writeProject(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject)
     */
    public void writeProject(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(project.getDescription())) {
            project.setDescription(" ");
        }
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
            conn = m_sqlManager.getConnection(dbc);

            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_WRITE");
            stmt.setString(1, project.getDescription());
            stmt.setString(2, project.getGroupId().toString());
            stmt.setString(3, project.getManagerGroupId().toString());
            stmt.setInt(4, project.getFlags());
            stmt.setInt(5, project.getType());
            stmt.setInt(6, project.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writePublishHistory(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID, org.opencms.db.CmsPublishedResource)
     */
    public void writePublishHistory(
        CmsDbContext dbc,
        CmsProject currentProject,
        CmsUUID publishId,
        CmsPublishedResource resource) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_WRITE_PUBLISH_HISTORY");
            stmt.setInt(1, resource.getBackupTagId());
            stmt.setString(2, resource.getStructureId().toString());
            stmt.setString(3, resource.getResourceId().toString());
            stmt.setString(4, resource.getRootPath());
            stmt.setInt(5, resource.getState());
            stmt.setInt(6, resource.getType());
            stmt.setString(7, publishId.toString());
            stmt.setInt(8, resource.getSiblingCount());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writeStaticExportPublishedResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, java.lang.String, int, java.lang.String, long)
     */
    public void writeStaticExportPublishedResource(
        CmsDbContext dbc,
        CmsProject currentProject,
        String resourceName,
        int linkType,
        String linkParameter,
        long timestamp) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        int returnValue = 0;
        // first check if a record with this resource name does already exist
        try {
            conn = m_sqlManager.getConnection(dbc, currentProject.getId());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_READ_PUBLISHED_RESOURCES");
            stmt.setString(1, resourceName);
            res = stmt.executeQuery();
            if (res.next()) {
                returnValue = res.getInt(1);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        // there was no entry found, so add it to the database
        if (returnValue == 0) {
            try {
                conn = m_sqlManager.getConnection(dbc, currentProject.getId());
                stmt = m_sqlManager.getPreparedStatement(conn, "C_STATICEXPORT_WRITE_PUBLISHED_LINKS");
                stmt.setString(1, new CmsUUID().toString());
                stmt.setString(2, resourceName);
                stmt.setInt(3, linkType);
                stmt.setString(4, linkParameter);
                stmt.setLong(5, timestamp);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, stmt), e);
            } finally {
                m_sqlManager.closeAll(dbc, conn, stmt, null);
            }
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
     * Resets the state to UNCHANGED and the last-modified-in-project-ID to the current project for a specified resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the Cms resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalResetResourceState(CmsDbContext dbc, CmsResource resource) throws CmsDataAccessException {

        try {

            // reset the resource state and the last-modified-in-project ID offline
            if (resource.getState() != CmsResource.STATE_UNCHANGED) {
                resource.setState(CmsResource.STATE_UNCHANGED);
                m_driverManager.getVfsDriver().writeResourceState(
                    dbc,
                    dbc.currentProject(),
                    resource,
                    CmsDriverManager.UPDATE_ALL);
            }

            // important: the project id must be set to the current project because of siblings 
            // that might have not been published, otherwise the siblings would belong to a non-valid 
            // project (e.g. with id 0) and show a grey flag

        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(Messages.LOG_ERROR_RESETTING_RESOURCE_STATE_1, resource.getRootPath()), e);
            }
            throw e;
        }
    }
}

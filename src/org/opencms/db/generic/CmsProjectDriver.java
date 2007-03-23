/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsProjectDriver.java,v $
 * Date   : $Date: 2007/03/23 16:52:32 $
 * Version: $Revision: 1.241.4.22 $
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
import org.opencms.db.*;
import org.opencms.file.*;
import org.opencms.file.CmsProject.CmsProjectType;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobInfoBean;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
 * @author Thomas Weckert 
 * @author Carsten Weinholz 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.241.4.22 $
 * 
 * @since 6.0.0 
 */
public class CmsProjectDriver implements I_CmsDriver, I_CmsProjectDriver {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.generic.CmsProjectDriver.class);

    /** The driver manager. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager. */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createProject(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsUser, org.opencms.file.CmsGroup, org.opencms.file.CmsGroup, java.lang.String, java.lang.String, int, CmsProjectType)
     */
    public CmsProject createProject(
        CmsDbContext dbc,
        CmsUUID id,
        CmsUser owner,
        CmsGroup group,
        CmsGroup managergroup,
        String projectFqn,
        String description,
        int flags,
        CmsProjectType type) throws CmsDataAccessException {

        CmsProject project = null;

        if ((description == null) || (description.length() < 1)) {
            description = " ";
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // get a JDBC connection from the OpenCms standard pool
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_CREATE_10");

            stmt.setString(1, id.toString());
            stmt.setString(2, owner.getId().toString());
            stmt.setString(3, group.getId().toString());
            stmt.setString(4, managergroup.getId().toString());
            stmt.setString(5, CmsOrganizationalUnit.getSimpleName(projectFqn));
            stmt.setString(6, description);
            stmt.setInt(7, flags);
            stmt.setInt(9, type.getMode());
            stmt.setString(10, CmsOrganizationalUnit.SEPARATOR + CmsOrganizationalUnit.getParentFqn(projectFqn));

            synchronized (this) {
                long createTime = System.currentTimeMillis();
                stmt.setLong(8, createTime);
                stmt.executeUpdate();
                try {
                    // this is an ungly hack, but for MySQL (and maybe other DBs as well)
                    // there is a UNIQUE INDEX constraint on the project name+createTime
                    // so theoretically if 2 projects with the same name are created very fast, this
                    // SQL restraint would be violated if we don't wait here
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // continue
                }
                project = new CmsProject(
                    id,
                    projectFqn,
                    description,
                    owner.getId(),
                    group.getId(),
                    managergroup.getId(),
                    flags,
                    createTime,
                    type);
            }
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
     * @see org.opencms.db.I_CmsProjectDriver#createProjectResource(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String)
     */
    public void createProjectResource(CmsDbContext dbc, CmsUUID projectId, String resourcePath)
    throws CmsDataAccessException {

        // do not create entries for online-project
        PreparedStatement stmt = null;
        Connection conn = null;

        boolean projectResourceExists = false;
        try {
            readProjectResource(dbc, projectId, resourcePath);
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
            conn = getSqlManager().getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_CREATE_2");

            // write new resource to the database
            stmt.setString(1, projectId.toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#createPublishJob(org.opencms.db.CmsDbContext, org.opencms.publish.CmsPublishJobInfoBean)
     */
    public void createPublishJob(CmsDbContext dbc, CmsPublishJobInfoBean publishJob) 
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_CREATE");
            stmt.setString(1, publishJob.getPublishHistoryId().toString());
            stmt.setString(2, publishJob.getProjectId().toString());
            stmt.setString(3, publishJob.getProjectName());
            stmt.setString(4, publishJob.getUserId().toString());
            stmt.setString(5, publishJob.getUserName());
            stmt.setString(6, publishJob.getLocale().toString());
            stmt.setInt(7, publishJob.getFlags());
            stmt.setString(8, publishJob.getReportFilePath());
            stmt.setInt(9, publishJob.getSize());
            stmt.setLong(10, publishJob.getEnqueueTime());
            stmt.setLong(11, publishJob.getStartTime());
            stmt.setLong(12, publishJob.getFinishTime());
            m_sqlManager.setBytes(stmt, 13, internalSerializePublishList(publishJob.getPublishList()));

            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_SERIALIZING_PUBLISHLIST_1, publishJob.getPublishHistoryId().toString()), e);
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
            conn = m_sqlManager.getConnection(dbc, currentProject.getUuid());
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
            conn = m_sqlManager.getConnection(dbc, project.getUuid());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_DELETE_1");
            // create the statement
            stmt.setString(1, project.getUuid().toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#deleteProjectResource(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String)
     */
    public void deleteProjectResource(CmsDbContext dbc, CmsUUID projectId, String resourceName)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_DELETE_2");
            // delete resource from the database
            stmt.setString(1, projectId.toString());
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
            conn = m_sqlManager.getConnection(dbc, project.getUuid());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_DELETEALL_1");
            stmt.setString(1, project.getUuid().toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishHistory(org.opencms.db.CmsDbContext, CmsUUID, int)
     */
    public void deletePublishHistory(CmsDbContext dbc, CmsUUID projectId, int maxBackupTagId)
    throws CmsDataAccessException {

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
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishHistoryEntry(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID, org.opencms.db.CmsPublishedResource)
     */
    public void deletePublishHistoryEntry(
        CmsDbContext dbc,
        CmsUUID projectId,
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
            stmt.setString(4, publishedResource.getRootPath());
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
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishJob(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public void deletePublishJob (CmsDbContext dbc, CmsUUID publishHistoryId) 
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_DELETE");
            stmt.setString(1, publishHistoryId.toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#deletePublishList(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public void deletePublishList(
        CmsDbContext dbc,
        CmsUUID publishHistoryId) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_DELETE_PUBLISHLIST");
            stmt.setString(1, publishHistoryId.toString());
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
            conn = m_sqlManager.getConnection(dbc, currentProject.getUuid());
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

        m_sqlManager = null;
        m_driverManager = null;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_DRIVER_1, getClass().getName()));
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
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_FILL_DEFAULTS_0));
        }

        String adminUser = OpenCms.getDefaultUsers().getUserAdmin();
        CmsUser admin = m_driverManager.readUser(dbc, adminUser);

        String administratorsGroup = OpenCms.getDefaultUsers().getGroupAdministrators();
        CmsGroup administrators = m_driverManager.readGroup(dbc, administratorsGroup);

        String usersGroup = OpenCms.getDefaultUsers().getGroupUsers();
        CmsGroup users = m_driverManager.readGroup(dbc, usersGroup);

        String projectmanagersGroup = OpenCms.getDefaultUsers().getGroupProjectmanagers();
        CmsGroup projectmanager = m_driverManager.readGroup(dbc, projectmanagersGroup);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // online project stuff
        ////////////////////////////////////////////////////////////////////////////////////////////        

        // create the online project
        CmsProject onlineProject = createProject(
            dbc,
            CmsProject.ONLINE_PROJECT_ID,
            admin,
            users,
            projectmanager,
            CmsProject.ONLINE_PROJECT_NAME,
            CmsMacroResolver.localizedKeyMacro(Messages.GUI_DEFAULT_PROJECT_ONLINE_DESCRIPTION_0, null),
            I_CmsPrincipal.FLAG_ENABLED,
            CmsProject.PROJECT_TYPE_NORMAL);

        // create the root-folder for the online project
        CmsFolder rootFolder = new CmsFolder(
            new CmsUUID(),
            new CmsUUID(),
            "/",
            CmsResourceTypeFolder.RESOURCE_TYPE_ID,
            0,
            onlineProject.getUuid(),
            CmsResource.STATE_NEW,
            0,
            admin.getId(),
            0,
            admin.getId(),
            1,
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT);

        m_driverManager.getVfsDriver().createResource(dbc, onlineProject, rootFolder, null);

        rootFolder.setState(CmsResource.STATE_UNCHANGED);

        m_driverManager.getVfsDriver().writeResource(dbc, onlineProject, rootFolder, CmsDriverManager.UPDATE_ALL);

        // important: must access through driver manager to ensure proper cascading
        m_driverManager.getProjectDriver().createProjectResource(dbc, onlineProject.getUuid(), rootFolder.getRootPath());

        // create the system-folder for the online project
        CmsFolder systemFolder = new CmsFolder(
            new CmsUUID(),
            new CmsUUID(),
            "/system",
            CmsResourceTypeFolder.RESOURCE_TYPE_ID,
            0,
            onlineProject.getUuid(),
            CmsResource.STATE_NEW,
            0,
            admin.getId(),
            0,
            admin.getId(),
            1,
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT);

        m_driverManager.getVfsDriver().createResource(dbc, onlineProject, systemFolder, null);

        systemFolder.setState(CmsResource.STATE_UNCHANGED);

        m_driverManager.getVfsDriver().writeResource(dbc, onlineProject, systemFolder, CmsDriverManager.UPDATE_ALL);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // setup project stuff
        ////////////////////////////////////////////////////////////////////////////////////////////

        // important: must access through driver manager to ensure proper cascading        
        CmsProject setupProject = m_driverManager.getProjectDriver().createProject(
            dbc,
            CmsUUID.getConstantUUID(SETUP_PROJECT_NAME),
            admin,
            administrators,
            administrators,
            SETUP_PROJECT_NAME,
            CmsMacroResolver.localizedKeyMacro(Messages.GUI_DEFAULT_PROJECT_SETUP_DESCRIPTION_0, null),
            I_CmsPrincipal.FLAG_ENABLED,
            CmsProject.PROJECT_TYPE_TEMPORARY);

        // create the root-folder for the offline project       
        CmsResource offlineRootFolder = m_driverManager.getVfsDriver().createResource(
            dbc,
            setupProject,
            rootFolder,
            null);

        offlineRootFolder.setState(CmsResource.STATE_UNCHANGED);

        m_driverManager.getVfsDriver().writeResource(dbc, setupProject, offlineRootFolder, CmsDriverManager.UPDATE_ALL);

        // important: must access through driver manager to ensure proper cascading        
        m_driverManager.getProjectDriver().createProjectResource(
            dbc,
            setupProject.getUuid(),
            offlineRootFolder.getRootPath());

        // create the system-folder for the offline project       
        CmsResource offlineSystemFolder = m_driverManager.getVfsDriver().createResource(
            dbc,
            setupProject,
            systemFolder,
            null);

        offlineSystemFolder.setState(CmsResource.STATE_UNCHANGED);

        m_driverManager.getVfsDriver().writeResource(
            dbc,
            setupProject,
            offlineSystemFolder,
            CmsDriverManager.UPDATE_ALL);
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
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ASSIGNED_POOL_1, poolUrl));
        }

        if ((successiveDrivers != null) && !successiveDrivers.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(
                    Messages.LOG_SUCCESSIVE_DRIVERS_UNSUPPORTED_1,
                    getClass().getName()));
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

            CmsResourceState histState = fixMovedResource(
                dbc,
                onlineProject,
                currentFolder,
                publishHistoryId,
                backupTagId);

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
                    new CmsPublishedResource(currentFolder, backupTagId, histState));
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_WRITING_PUBLISHING_HISTORY_1,
                        currentFolder.getRootPath()), e);
                }
                throw e;
            }

            // read the folder online
            onlineFolder = m_driverManager.readFolder(dbc, currentFolder.getRootPath(), CmsResourceFilter.ALL);

            try {
                // delete the properties online and offline
                m_driverManager.getVfsDriver().deletePropertyObjects(
                    dbc,
                    onlineProject.getUuid(),
                    onlineFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
                m_driverManager.getVfsDriver().deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getUuid(),
                    currentFolder,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_DELETING_PROPERTIES_1,
                        currentFolder.getRootPath()), e);
                }
                throw e;
            }

            try {
                // remove the folder online and offline
                m_driverManager.getVfsDriver().removeFolder(dbc, dbc.currentProject(), currentFolder);

                try {
                    m_driverManager.getVfsDriver().readFolder(
                        dbc,
                        dbc.currentProject().getUuid(),
                        currentFolder.getRootPath());
                } catch (CmsVfsResourceNotFoundException e) {
                    // remove the online folder only if it is really deleted offline
                    m_driverManager.getVfsDriver().removeFolder(dbc, onlineProject, currentFolder);
                }
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_REMOVING_RESOURCE_1,
                        currentFolder.getRootPath()), e);
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
                    LOG.error(
                        Messages.get().getBundle().key(Messages.LOG_REMOVING_ACL_1, currentFolder.getRootPath()),
                        e);
                }
                throw e;
            }

            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);

            if (LOG.isDebugEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(
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
            report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_2,
                String.valueOf(m),
                String.valueOf(n)), I_CmsReport.FORMAT_NOTE);

            if (offlineResource.getState().isDeleted()) {

                report.print(Messages.get().container(Messages.RPT_DELETE_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                publishDeletedFile(dbc, onlineProject, offlineResource, backupEnabled, publishHistoryId, backupTagId);

                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_DEL_FILE_3,
                        String.valueOf(m),
                        String.valueOf(n),
                        offlineResource.getRootPath()));
                }

            } else if (offlineResource.getState().isChanged()) {

                report.print(Messages.get().container(Messages.RPT_PUBLISH_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                publishChangedFile(
                    dbc,
                    onlineProject,
                    offlineResource,
                    publishedContentIds,
                    backupEnabled,
                    publishDate,
                    publishHistoryId,
                    backupTagId,
                    maxVersions);

                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_PUBLISHING_FILE_3,
                        offlineResource.getRootPath(),
                        String.valueOf(m),
                        String.valueOf(n)));
                }
            } else if (offlineResource.getState().isNew()) {

                report.print(Messages.get().container(Messages.RPT_PUBLISH_FILE_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    dbc.removeSiteRoot(offlineResource.getRootPath())));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                publishNewFile(
                    dbc,
                    onlineProject,
                    offlineResource,
                    publishedContentIds,
                    backupEnabled,
                    publishDate,
                    publishHistoryId,
                    backupTagId,
                    maxVersions);

                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isDebugEnabled()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(
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
                CmsFile offlineContent = m_driverManager.getVfsDriver().readFile(
                    dbc,
                    offlineProject.getUuid(),
                    offlineResource.getResourceId());

                offlineFile = new CmsFile(offlineResource);
                offlineFile.setContents(offlineContent.getContents());

                // create the file online              
                newFile = (CmsFile)offlineFile.clone();
                newFile.setState(CmsResource.STATE_UNCHANGED);

                m_driverManager.getVfsDriver().createResource(dbc, onlineProject, newFile, newFile.getContents());

                // update the online/offline structure and resource records of the file
                m_driverManager.getVfsDriver().publishResource(dbc, onlineProject, newFile, offlineFile, false);

                publishedResourceIds.add(offlineFile.getResourceId());
            } else {
                // create the sibling online
                m_driverManager.getVfsDriver().createSibling(dbc, onlineProject, offlineResource);

                CmsFile offlineContent = m_driverManager.getVfsDriver().readFile(
                    dbc,
                    onlineProject.getUuid(),
                    offlineResource.getResourceId());

                newFile = new CmsFile(offlineResource);
                newFile.setContents(offlineContent.getContents());
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_PUBLISHING_FILE_CONTENT_1,
                    offlineResource.toString()), e);
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

            CmsResourceState histState = fixMovedResource(
                dbc,
                onlineProject,
                offlineFolder,
                publishHistoryId,
                backupTagId);

            if (offlineFolder.getState().isNew()) {

                try {
                    // create the folder online
                    newFolder = (CmsFolder)offlineFolder.clone();
                    newFolder.setState(CmsResource.STATE_UNCHANGED);

                    onlineFolder = m_driverManager.getVfsDriver().createResource(dbc, onlineProject, newFolder, null);
                } catch (CmsVfsResourceAlreadyExistsException e) {
                    try {
                        onlineFolder = m_driverManager.getVfsDriver().readFolder(
                            dbc,
                            onlineProject.getUuid(),
                            offlineFolder.getRootPath());
                        m_driverManager.getVfsDriver().publishResource(
                            dbc,
                            onlineProject,
                            onlineFolder,
                            offlineFolder,
                            false);
                    } catch (CmsDataAccessException e1) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(Messages.get().getBundle().key(
                                Messages.LOG_READING_RESOURCE_1,
                                offlineFolder.getRootPath()), e);
                        }
                        throw e1;
                    }
                } catch (CmsDataAccessException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().getBundle().key(
                            Messages.LOG_PUBLISHING_RESOURCE_1,
                            offlineFolder.getRootPath()), e);
                    }
                    throw e;
                }

            } else if (offlineFolder.getState().isChanged()) {

                try {
                    // read the folder online
                    onlineFolder = m_driverManager.getVfsDriver().readFolder(
                        dbc,
                        onlineProject.getUuid(),
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
                            LOG.error(Messages.get().getBundle().key(
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
                        LOG.error(Messages.get().getBundle().key(
                            Messages.LOG_PUBLISHING_RESOURCE_1,
                            offlineFolder.getRootPath()), e);
                    }
                    throw e;
                }
            }

            if (onlineFolder != null) {
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
                        LOG.error(Messages.get().getBundle().key(
                            Messages.LOG_PUBLISHING_ACL_1,
                            offlineFolder.getRootPath()), e);
                    }
                    throw e;
                }
            }

            try {
                // write the properties online
                m_driverManager.getVfsDriver().deletePropertyObjects(
                    dbc,
                    onlineProject.getUuid(),
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
                    LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_PUBLISHING_PROPERTIES_1,
                        offlineFolder.getRootPath()), e);
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
                    new CmsPublishedResource(offlineFolder, backupTagId, histState));
            } catch (CmsDataAccessException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_WRITING_PUBLISHING_HISTORY_1,
                        offlineFolder.getRootPath()), e);
                }
                throw e;
            }
            report.println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_PUBLISHING_FOLDER_3,
                    String.valueOf(m),
                    String.valueOf(n),
                    offlineFolder.getRootPath()));
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
    public void publishProject(
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
                LOG.debug(Messages.get().getBundle().key(
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
                    if (currentFolder.getState().isNew()) {

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

                    } else if (currentFolder.getState().isChanged()) {

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
                m_driverManager.unlockResource(dbc, currentFolder, true, true);
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

                    if (!currentResource.getState().isDeleted()) {
                        // reset the resource state to UNCHANGED and the last-modified-in-project-ID to 0
                        internalResetResourceState(dbc, currentResource);
                    }

                    dbc.pop();
                } catch (Throwable t) {
                    dbc.report(report, Messages.get().container(
                        Messages.ERR_ERROR_PUBLISHING_FILE_1,
                        currentResource.getRootPath()), t);
                }

                m_driverManager.unlockResource(dbc, currentResource, true, true);
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
                m_driverManager.unlockResource(dbc, currentFolder, true, true);
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
     * @see org.opencms.db.I_CmsProjectDriver#readLocks(org.opencms.db.CmsDbContext)
     */
    public List readLocks(CmsDbContext dbc) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        List locks = new ArrayList(256);
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCE_LOCKS_READALL");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String resourcePath = rs.getString(m_sqlManager.readQuery("C_RESOURCE_LOCKS_RESOURCE_PATH"));
                CmsUUID userId = new CmsUUID(rs.getString(m_sqlManager.readQuery("C_RESOURCE_LOCKS_USER_ID")));
                CmsUUID projectId = new CmsUUID(rs.getString(m_sqlManager.readQuery("C_RESOURCE_LOCKS_PROJECT_ID")));
                int lockType = rs.getInt(m_sqlManager.readQuery("C_RESOURCE_LOCKS_LOCK_TYPE"));
                CmsProject project;
                try {
                    project = readProject(dbc, projectId);
                } catch (CmsDataAccessException dae) {
                    // the project does not longer exist, ignore this lock (should usually not happen)
                    project = null;
                }
                if (project != null) {
                    CmsLock lock = new CmsLock(resourcePath, userId, project, CmsLockType.valueOf(lockType));
                    locks.add(lock);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DBG_READ_LOCKS_1, new Integer(locks.size())));
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        return locks;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProject(org.opencms.db.CmsDbContext, CmsUUID)
     */
    public CmsProject readProject(CmsDbContext dbc, CmsUUID id) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_1");

            stmt.setString(1, id.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                project = internalCreateProject(dbc, res);
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
    public CmsProject readProject(CmsDbContext dbc, String projectFqn) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYNAME_2");

            stmt.setString(1, CmsOrganizationalUnit.getSimpleName(projectFqn));
            stmt.setString(2, CmsOrganizationalUnit.SEPARATOR + CmsOrganizationalUnit.getParentFqn(projectFqn));
            res = stmt.executeQuery();

            if (res.next()) {
                project = internalCreateProject(dbc, res);
            } else {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_NO_PROJECT_WITH_NAME_1,
                    projectFqn));
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
     * @see org.opencms.db.I_CmsProjectDriver#readProjectResource(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String)
     */
    public String readProjectResource(CmsDbContext dbc, CmsUUID projectId, String resourcePath)
    throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        String resName = null;

        try {
            conn = getSqlManager().getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ_2");

            // select resource from the database
            stmt.setString(1, projectId.toString());
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
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ_BY_ID_1");
            stmt.setString(1, project.getUuid().toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#readProjects(org.opencms.db.CmsDbContext, String)
     */
    public List readProjects(CmsDbContext dbc, String ouFqn) throws CmsDataAccessException {

        List projects = new ArrayList();
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // create the statement
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYOU_1");

            stmt.setString(1, ouFqn + "%");
            res = stmt.executeQuery();

            while (res.next()) {
                projects.add(internalCreateProject(dbc, res));
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
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYGROUP_2");

            stmt.setString(1, group.getId().toString());
            stmt.setString(2, group.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                projects.add(internalCreateProject(dbc, res));
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
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYMANAGER_1");

            stmt.setString(1, group.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                projects.add(internalCreateProject(dbc, res));
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
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYUSER_1");

            stmt.setString(1, user.getId().toString());
            res = stmt.executeQuery();

            while (res.next()) {
                projects.add(internalCreateProject(dbc, res));
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
     * @see org.opencms.db.I_CmsProjectDriver#readPublishJob(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public CmsPublishJobInfoBean readPublishJob(CmsDbContext dbc, CmsUUID publishHistoryId) 
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        
        CmsPublishJobInfoBean result = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_READ_JOB");
            stmt.setString(1, publishHistoryId.toString());
            res = stmt.executeQuery();  
            
            if (res.next()) { 
                result = createPublishJobInfoBean(res);
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
     * @see org.opencms.db.I_CmsProjectDriver#readPublishJobs(org.opencms.db.CmsDbContext, long, long)
     */
    public List readPublishJobs(CmsDbContext dbc, long startTime, long endTime) 
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        
        List result = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_READ_JOBS_IN_TIMERANGE");
            stmt.setLong(1, startTime);
            stmt.setLong(2, endTime);
            res = stmt.executeQuery();  
            
            result = new ArrayList();
            while (res.next()) { 
                result.add(createPublishJobInfoBean(res));
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
     * @see org.opencms.db.I_CmsProjectDriver#readPublishList(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public CmsPublishList readPublishList(CmsDbContext dbc, CmsUUID publishHistoryId) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        CmsPublishList publishList = null;
        
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_READ_PUBLISHLIST");
            stmt.setString(1, publishHistoryId.toString());
            res = stmt.executeQuery();
            
            if (res.next()) {
                byte[] bytes = m_sqlManager.getBytes(res, "PUBLISH_LIST");
                publishList = internalDeserializePublishList(bytes);
            }
        } catch (SQLException e) {
            throw new CmsDbSqlException(Messages.get().container(
                Messages.ERR_GENERIC_SQL_1,
                CmsDbSqlException.getErrorQuery(stmt)), e);
        } catch (Exception e) {
            throw new CmsDataAccessException(Messages.get().container(
                Messages.ERR_PUBLISHLIST_DESERIALIZATION_FAILED_1, publishHistoryId), e);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }            
        
        return publishList;
    }   
        
    /**
     * @see org.opencms.db.I_CmsProjectDriver#readPublishedResources(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID)
     */
    public List readPublishedResources(CmsDbContext dbc, CmsUUID projectId, CmsUUID publishHistoryId)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        List publishedResources = new ArrayList();

        try {
            conn = m_sqlManager.getConnection(dbc, projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SELECT_PUBLISHED_RESOURCES");
            stmt.setString(1, publishHistoryId.toString());
            res = stmt.executeQuery();

            while (res.next()) {
                CmsUUID structureId = new CmsUUID(res.getString("STRUCTURE_ID"));
                CmsUUID resourceId = new CmsUUID(res.getString("RESOURCE_ID"));
                String rootPath = res.getString("RESOURCE_PATH");
                int resourceState = res.getInt("RESOURCE_STATE");
                int resourceType = res.getInt("RESOURCE_TYPE");
                int siblingCount = res.getInt("SIBLING_COUNT");
                int backupTagId = res.getInt("PUBLISH_TAG");

                // compose the resource state
                CmsResourceState state;
                if (resourceState == CmsPublishedResource.STATE_MOVED_SOURCE.getState()) {
                    state = CmsPublishedResource.STATE_MOVED_SOURCE;
                } else if (resourceState == CmsPublishedResource.STATE_MOVED_DESTINATION.getState()) {
                    state = CmsPublishedResource.STATE_MOVED_DESTINATION;
                } else {
                    state = CmsResourceState.valueOf(resourceState);
                }

                publishedResources.add(new CmsPublishedResource(
                    structureId,
                    resourceId,
                    backupTagId,
                    rootPath,
                    resourceType,
                    structureId.isNullUUID() ? false : CmsFolder.isFolderType(resourceType),
                    state,
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
            conn = m_sqlManager.getConnection(dbc, currentProject.getUuid());
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
            conn = m_sqlManager.getConnection(dbc, currentProject.getUuid());
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
            conn = m_sqlManager.getConnection(dbc, project.getUuid());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UNMARK");
            // create the statement
            stmt.setString(1, project.getUuid().toString());
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
     * @see org.opencms.db.I_CmsProjectDriver#writeLocks(org.opencms.db.CmsDbContext, java.util.List)
     */
    public void writeLocks(CmsDbContext dbc, List locks) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCE_LOCKS_DELETEALL");
            int deleted = stmt.executeUpdate();
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DBG_CLEAR_LOCKS_1, new Integer(deleted)));
            }
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCE_LOCK_WRITE");
            if (LOG.isDebugEnabled()) {
                LOG.debug("SQL :" + m_sqlManager.readQuery("C_RESOURCE_LOCK_WRITE"));
            }
            Iterator i = locks.iterator();
            int count = 0;
            while (i.hasNext()) {
                CmsLock lock = (CmsLock)i.next();
                // only persist locks that should be written to the DB
                CmsLock sysLock = lock.getSystemLock();
                if (sysLock.isPersistent()) {
                    // persist system lock
                    stmt.setString(1, sysLock.getResourceName());
                    stmt.setString(2, sysLock.getUserId().toString());
                    stmt.setString(3, sysLock.getProjectId().toString());
                    stmt.setInt(4, sysLock.getType().hashCode());
                    stmt.executeUpdate();
                    count++;
                }
                CmsLock editLock = lock.getEditionLock();
                if (editLock.isPersistent()) {
                    // persist edition lock
                    stmt.setString(1, editLock.getResourceName());
                    stmt.setString(2, editLock.getUserId().toString());
                    stmt.setString(3, editLock.getProjectId().toString());
                    stmt.setInt(4, editLock.getType().hashCode());
                    stmt.executeUpdate();
                    count++;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DBG_WRITE_LOCKS_1, new Integer(count)));
            }

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

            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_WRITE_6");
            stmt.setString(1, project.getDescription());
            stmt.setString(2, project.getGroupId().toString());
            stmt.setString(3, project.getManagerGroupId().toString());
            stmt.setInt(4, project.getFlags());
            stmt.setInt(5, project.getType().getMode());
            stmt.setString(6, project.getUuid().toString());
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
            conn = m_sqlManager.getConnection(dbc, currentProject.getUuid());
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_WRITE_PUBLISH_HISTORY");
            stmt.setInt(1, resource.getBackupTagId());
            stmt.setString(2, resource.getStructureId().toString());
            stmt.setString(3, resource.getResourceId().toString());
            stmt.setString(4, resource.getRootPath());
            stmt.setInt(5, resource.getMovedState().getState());
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
     * @see org.opencms.db.I_CmsProjectDriver#writePublishJob(org.opencms.db.CmsDbContext, org.opencms.publish.CmsPublishJobInfoBean)
     */
    public void writePublishJob(CmsDbContext dbc, CmsPublishJobInfoBean publishJob)
    throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PUBLISHJOB_WRITE");
            stmt.setString(1, publishJob.getProjectId().toString());
            stmt.setString(2, publishJob.getProjectName());
            stmt.setString(3, publishJob.getUserId().toString());
            stmt.setString(4, publishJob.getUserName());
            stmt.setString(5, publishJob.getLocale().toString());
            stmt.setInt(6, publishJob.getFlags());
            stmt.setString(7, publishJob.getReportFilePath());
            stmt.setInt(8, publishJob.getSize());
            stmt.setLong(9, publishJob.getEnqueueTime());
            stmt.setLong(10, publishJob.getStartTime());
            stmt.setLong(11, publishJob.getFinishTime());            
            stmt.setString(12, publishJob.getPublishHistoryId().toString());
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
            conn = m_sqlManager.getConnection(dbc, currentProject.getUuid());
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
                conn = m_sqlManager.getConnection(dbc, currentProject.getUuid());
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
     * Creates a <code>CmsPublishJobInfoBean</code> from a result set.<p>
     * 
     * @param res the result set 
     * @return an initialized <code>CmsPublishJobInfoBean</code>
     * @throws SQLException if something goes wrong
     */
    protected CmsPublishJobInfoBean createPublishJobInfoBean(ResultSet res)
    throws SQLException {
      
        return new CmsPublishJobInfoBean(
            new CmsUUID(res.getString("HISTORY_ID")),
            new CmsUUID(res.getString("PROJECT_ID")),
            res.getString("PROJECT_NAME"),
            new CmsUUID(res.getString("USER_ID")),
            res.getString("USER_NAME"),
            res.getString("PUBLISH_LOCALE"),
            res.getInt("PUBLISH_FLAGS"),
            res.getString("REPORT_PATH"),
            res.getInt("RESOURCE_COUNT"),
            res.getLong("ENQUEUE_TIME"),
            res.getLong("START_TIME"),
            res.getLong("FINISH_TIME")
        );
    }
    
    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        destroy();
        super.finalize();
    }

    /**
     * Checks if the given resource (by id) is available in the online project,
     * if there exists a resource with a different path (a moved file), then the 
     * online entry is moved to the right (new) location before publishing.<p>
     * 
     * @param dbc the db context
     * @param onlineProject the online project
     * @param offlineResource the offline resource to check
     * @param publishHistoryId the publish history id
     * @param backupTagId the backup tag id
     * 
     * @return <code>true</code> if the resource has actually been moved
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected CmsResourceState fixMovedResource(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        CmsUUID publishHistoryId,
        int backupTagId) throws CmsDataAccessException {

        CmsResource onlineResource;
        // check if the file has been moved since last publishing
        try {
            onlineResource = m_driverManager.getVfsDriver().readResource(
                dbc,
                onlineProject.getUuid(),
                offlineResource.getStructureId(),
                true);
            if (onlineResource.getRootPath().equals(offlineResource.getRootPath())) {
                // resource changed, not moved 
                return offlineResource.getState();
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // ok, resource new, not moved
            return offlineResource.getState();
        }

        // remove the links of the moved resource
        CmsRelationFilter filter = CmsRelationFilter.TARGETS;
        filter = filter.filterStructureId(onlineResource.getStructureId());
        filter = filter.filterPath(onlineResource.getRootPath());
        m_driverManager.getVfsDriver().deleteRelations(dbc, onlineProject.getUuid(), null, filter);

        // move the online resource to the new position
        m_driverManager.getVfsDriver().moveResource(
            dbc,
            onlineProject.getUuid(),
            onlineResource,
            offlineResource.getRootPath());

        try {
            // write the file to the publishing history
            m_driverManager.getProjectDriver().writePublishHistory(
                dbc,
                dbc.currentProject(),
                publishHistoryId,
                new CmsPublishedResource(onlineResource, backupTagId, CmsPublishedResource.STATE_MOVED_SOURCE));
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_WRITING_PUBLISHING_HISTORY_1,
                    onlineResource.getRootPath()), e);
            }
            throw e;
        }
        return offlineResource.getState().isDeleted() ? CmsResource.STATE_DELETED
        : CmsPublishedResource.STATE_MOVED_DESTINATION;
    }

    /**
     * Creates a new project from the current row of the given result set.<p>
     * 
     * @param dbc the current database context
     * @param res the result set
     * 
     * @return the new project
     * 
     * @throws SQLException is something goes wrong
     */
    protected CmsProject internalCreateProject(CmsDbContext dbc, ResultSet res) throws SQLException {

        String ou = CmsOrganizationalUnit.removeLeadingSeparator(res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_OU_0")));
        return new CmsProject(
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_ID_0"))),
            ou + res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_NAME_0")),
            m_sqlManager.getMacroResolver(dbc).resolveMacros(
                res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_DESCRIPTION_0"))),
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_USER_ID_0"))),
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_GROUP_ID_0"))),
            new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_MANAGERGROUP_ID_0"))),
            res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_FLAGS_0")),
            res.getLong(m_sqlManager.readQuery("C_PROJECTS_DATE_CREATED_0")),
            CmsProjectType.valueOf(res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_TYPE_0"))));
    }

    /**
     * Builds a publish list from serialized data.<p>
     * 
     * @param bytes the byte array containing the serailized data for the publish list
     * @return the initialized publish list
     * 
     * @throws IOException if deserialization fails
     * @throws ClassNotFoundException if deserialization fails
     */
    protected CmsPublishList internalDeserializePublishList(byte[] bytes) throws IOException, ClassNotFoundException {
        
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        ObjectInputStream oin = new ObjectInputStream(bin);
        return (CmsPublishList)oin.readObject();
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
            if (!resource.getState().isUnchanged()) {
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
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_ERROR_RESETTING_RESOURCE_STATE_1,
                    resource.getRootPath()), e);
            }
            throw e;
        }
    }

    /**
     * Serialize publish list to write it as byte array to the database.<p>
     * 
     * @param publishList the publish list
     * @return byte array containing the publish list data
     * @throws IOException if something goes wrong
     */
    protected byte[] internalSerializePublishList(CmsPublishList publishList) throws IOException {

        // serialize the publish list
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(publishList);
        oout.close();
        return bout.toByteArray();
    }
    
    /**
     * Publishes a changed file.<p>
     * 
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param offlineResource the resource to publish
     * @param publishedContentIds contains the UUIDs of already published content records
     * @param backupEnabled if backup is enabled
     * @param publishDate the publish date
     * @param publishHistoryId the publish history id
     * @param backupTagId the backup id
     * @param maxVersions the max number of version
     * 
     * @throws CmsDataAccessException is something goes wrong
     */
    protected void publishChangedFile(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set publishedContentIds,
        boolean backupEnabled,
        long publishDate,
        CmsUUID publishHistoryId,
        int backupTagId,
        int maxVersions) throws CmsDataAccessException {

        CmsResourceState histState = fixMovedResource(
            dbc,
            onlineProject,
            offlineResource,
            publishHistoryId,
            backupTagId);

        CmsResource onlineResource;
        try {
            // read the file header online                   
            onlineResource = m_driverManager.getVfsDriver().readResource(
                dbc,
                onlineProject.getUuid(),
                offlineResource.getStructureId(),
                false);

            // reset the labeled link flag before writing the online file
            int flags = offlineResource.getFlags();
            flags &= ~CmsResource.FLAG_LABELED;
            offlineResource.setFlags(flags);

            // delete the properties online
            m_driverManager.getVfsDriver().deletePropertyObjects(
                dbc,
                onlineProject.getUuid(),
                onlineResource,
                CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);

            // if the offline file has a resource ID different from the online file
            // (probably because a deleted file was replaced by a new file with the
            // same name), the properties mapped to the "old" resource ID have to be
            // deleted also offline. if this is the case, the online and offline structure
            // ID's do match, but the resource ID's are different. structure IDs are reused
            // to prevent orphan structure records in the online project.

            if (!onlineResource.getResourceId().equals(offlineResource.getResourceId())) {
                List offlineProperties = m_driverManager.getVfsDriver().readPropertyObjects(
                    dbc,
                    dbc.currentProject(),
                    onlineResource);
                if (offlineProperties.size() > 0) {
                    for (int i = 0; i < offlineProperties.size(); i++) {
                        CmsProperty property = (CmsProperty)offlineProperties.get(i);
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
            m_driverManager.getVfsDriver().removeFile(dbc, onlineProject, onlineResource);
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_DELETING_PROPERTIES_1, offlineResource.toString()),
                    e);
            }

            throw e;
        }

        CmsFile newFile;
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
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_PUBLISHING_RESOURCE_1,
                    offlineResource.getRootPath()), e);
            }
            throw e;
        }

        List offlineProperties;
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
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_PUBLISHING_PROPERTIES_1, newFile.getRootPath()),
                    e);
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
                LOG.error(Messages.get().getBundle().key(Messages.LOG_PUBLISHING_ACL_1, newFile.getRootPath()), e);
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
                new CmsPublishedResource(offlineResource, backupTagId, histState));
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_WRITING_PUBLISHING_HISTORY_1,
                    newFile.getRootPath()), e);
            }
            throw e;
        }

        updateRelations(dbc, onlineProject, offlineResource);
    }

    /**
     * Publishes a deleted file.<p>
     * 
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param offlineResource the resource to publish
     * @param backupEnabled if backup is enabled
     * @param publishHistoryId the publish history id
     * @param backupTagId the backup id
     * 
     * @throws CmsDataAccessException is something goes wrong
     */
    protected void publishDeletedFile(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        boolean backupEnabled,
        CmsUUID publishHistoryId,
        int backupTagId) throws CmsDataAccessException {

        CmsResourceState histState = fixMovedResource(
            dbc,
            onlineProject,
            offlineResource,
            publishHistoryId,
            backupTagId);

        CmsResource onlineResource;
        try {
            // read the file header online
            onlineResource = m_driverManager.getVfsDriver().readResource(
                dbc,
                onlineProject.getUuid(),
                offlineResource.getStructureId(),
                true);
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_READING_RESOURCE_1, offlineResource.getRootPath()),
                    e);
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
                new CmsPublishedResource(offlineResource, backupTagId, histState));
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_WRITING_PUBLISHING_HISTORY_1,
                    offlineResource.getRootPath()), e);
            }
            throw e;
        }

        int propertyDeleteOption = -1;
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
                onlineProject.getUuid(),
                onlineResource,
                propertyDeleteOption);
            m_driverManager.getVfsDriver().deletePropertyObjects(
                dbc,
                dbc.currentProject().getUuid(),
                offlineResource,
                propertyDeleteOption);

            // if the offline file has a resource ID different from the online file
            // (probably because a (deleted) file was replaced by a new file with the
            // same name), the properties with the "old" resource ID have to be
            // deleted also offline
            if (!onlineResource.getResourceId().equals(offlineResource.getResourceId())) {
                m_driverManager.getVfsDriver().deletePropertyObjects(
                    dbc,
                    dbc.currentProject().getUuid(),
                    onlineResource,
                    CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES);
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_DELETING_PROPERTIES_1,
                    offlineResource.getRootPath()), e);
            }
            throw e;
        }

        try {
            // remove the file online and offline
            m_driverManager.getVfsDriver().removeFile(dbc, dbc.currentProject(), offlineResource);

            try {
                m_driverManager.getVfsDriver().readResource(
                    dbc,
                    dbc.currentProject().getUuid(),
                    offlineResource.getStructureId(),
                    true);
            } catch (CmsVfsResourceNotFoundException e) {
                // remove the online file only if it is really deleted offline
                m_driverManager.getVfsDriver().removeFile(dbc, onlineProject, onlineResource);
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_REMOVING_RESOURCE_1,
                    offlineResource.getRootPath()), e);
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
                LOG.error(Messages.get().getBundle().key(Messages.LOG_REMOVING_ACL_1, offlineResource.toString()), e);
            }
            throw e;
        }

        m_driverManager.getVfsDriver().deleteRelations(
            dbc,
            onlineProject.getUuid(),
            null,
            CmsRelationFilter.TARGETS.filterResource(offlineResource));
    }

    /**
     * Publishes a new file.<p>
     * 
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param offlineResource the resource to publish
     * @param publishedContentIds contains the UUIDs of already published content records
     * @param backupEnabled if backup is enabled
     * @param publishDate the publish date
     * @param publishHistoryId the publish history id
     * @param backupTagId the backup id
     * @param maxVersions the max number of version
     * 
     * @throws CmsDataAccessException is something goes wrong
     */
    protected void publishNewFile(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set publishedContentIds,
        boolean backupEnabled,
        long publishDate,
        CmsUUID publishHistoryId,
        int backupTagId,
        int maxVersions) throws CmsDataAccessException {

        CmsResourceState histState = fixMovedResource(
            dbc,
            onlineProject,
            offlineResource,
            publishHistoryId,
            backupTagId);

        CmsFile newFile;
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
                m_driverManager.getVfsDriver().removeFile(dbc, onlineProject, offlineResource);
                publishedContentIds.remove(offlineResource.getResourceId());
                newFile = m_driverManager.getProjectDriver().publishFileContent(
                    dbc,
                    dbc.currentProject(),
                    onlineProject,
                    offlineResource,
                    publishedContentIds);
            } catch (CmsDataAccessException e1) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_PUBLISHING_RESOURCE_1,
                        offlineResource.getRootPath()), e);
                }
                throw e1;
            }
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_PUBLISHING_RESOURCE_1,
                    offlineResource.getRootPath()), e);
            }
            throw e;
        }

        List offlineProperties;
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
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_PUBLISHING_PROPERTIES_1, newFile.getRootPath()),
                    e);
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
                LOG.error(Messages.get().getBundle().key(Messages.LOG_PUBLISHING_ACL_1, newFile.getRootPath()), e);
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
                new CmsPublishedResource(offlineResource, backupTagId, histState));

            updateRelations(dbc, onlineProject, offlineResource);
        } catch (CmsDataAccessException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_WRITING_PUBLISHING_HISTORY_1,
                    newFile.getRootPath()), e);
            }

            throw e;
        }
    }

    /**
     * Updates the relations on the online project copying the relations from the offline project.<p>
     * 
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param offlineResource the resource to update the relations for
     * 
     * @throws CmsDataAccessException is something goes wrong
     */
    protected void updateRelations(CmsDbContext dbc, CmsProject onlineProject, CmsResource offlineResource)
    throws CmsDataAccessException {

        m_driverManager.getVfsDriver().deleteRelations(
            dbc,
            onlineProject.getUuid(),
            null,
            CmsRelationFilter.TARGETS.filterResource(offlineResource));
        Iterator itRelations = m_driverManager.getVfsDriver().readRelations(
            dbc,
            dbc.currentProject().getUuid(),
            CmsRelationFilter.TARGETS.filterResource(offlineResource)).iterator();
        while (itRelations.hasNext()) {
            m_driverManager.getVfsDriver().createRelation(dbc, onlineProject.getUuid(), (CmsRelation)itRelations.next());
        }
    }

}
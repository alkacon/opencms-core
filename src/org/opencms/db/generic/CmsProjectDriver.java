/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/CmsProjectDriver.java,v $
 * Date   : $Date: 2004/03/22 16:29:00 $
 * Version: $Revision: 1.154 $
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
import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
 * @version $Revision: 1.154 $ $Date: 2004/03/22 16:29:00 $
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @since 5.1
 */
public class CmsProjectDriver extends Object implements I_CmsDriver, I_CmsProjectDriver, I_CmsEventListener {

    /** Constant to get property from configurations */
    protected static final String C_CONFIGURATIONS_DIGEST = "digest";

    /** Constant to get property from configurations */
    protected static final String C_CONFIGURATIONS_DIGEST_FILE_ENCODING = "digest.fileencoding";

    /** Constant to get property from configurations */
    protected static final String C_CONFIGURATIONS_POOL = "pool";

    /** Internal debugging flag.<p> */
    private static final boolean C_DEBUG = false;

    /**
     * The maximum amount of tables.
     */
    protected static final int C_MAX_TABLES = 18;

    /**
     * The session-timeout value:
     * currently six hours. After that time the session can't be restored.
     */
    public static final long C_SESSION_TIMEOUT = 6 * 60 * 60 * 1000;

    /**
     * Table-key for projects
     */
    protected static final String C_TABLE_PROJECTS = "CMS_PROJECTS";

    /**
     * Table-key for properties
     */
    protected static final String C_TABLE_PROPERTIES = "CMS_PROPERTIES";

    /**
     * Table-key for property definitions
     */
    protected static final String C_TABLE_PROPERTYDEF = "CMS_PROPERTYDEF";

    /**
     * Table-key for system properties
     */
    protected static final String C_TABLE_SYSTEMPROPERTIES = "CMS_SYSTEMPROPERTIES";

    /**
     * The driver manager
     */
    protected CmsDriverManager m_driverManager;

    /**
     * Array containing all max-ids for the tables.
     */
    protected int[] m_maxIds;

    /**
     * The sql manager
     */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {
        I_CmsReport report = null;

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_UPDATE_EXPORTS :
                report = (I_CmsReport) event.getData().get("report");
                m_driverManager.updateExportPoints(event.getCmsObject().getRequestContext(), report);
                break;

            case I_CmsEventListener.EVENT_PUBLISH_PROJECT :
                CmsUUID publishHistoryId = new CmsUUID((String) event.getData().get("publishHistoryId"));
                report = (I_CmsReport) event.getData().get("report");
                m_driverManager.writeExportPoints(event.getCmsObject().getRequestContext(), report, publishHistoryId);
                break;

            default :
                // TODO: define default behauvior
                break;
            }
        }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createProject(org.opencms.file.CmsUser, org.opencms.file.CmsGroup, org.opencms.file.CmsGroup, org.opencms.workflow.CmsTask, java.lang.String, java.lang.String, int, int, java.lang.Object)
    */
    public CmsProject createProject(CmsUser owner, CmsGroup group, CmsGroup managergroup, CmsTask task, String name, String description, int flags, int type, Object reservedParam) throws CmsException {
        CmsProject project = null;

        if ((description == null) || (description.length() < 1)) {
            description = " ";
        }

        Timestamp createTime = new Timestamp(new java.util.Date().getTime());
        Connection conn = null;
        PreparedStatement stmt = null;

        int id = I_CmsConstants.C_UNKNOWN_ID;

        try {
            if (reservedParam == null) {
                // get a JDBC connection from the OpenCms standard {online|offline|backup} pools
            conn = m_sqlManager.getConnection();
                // get a new primary key ID          
                id = m_sqlManager.nextId(C_TABLE_PROJECTS);
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
            stmt.setTimestamp(9, createTime);
            stmt.setInt(10, type);
            stmt.executeUpdate();
            
            project = new CmsProject(id, name, description, task.getId(), owner.getId(), group.getId(), managergroup.getId(), flags, createTime, type);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return project;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#createProjectResource(int, java.lang.String, java.lang.Object)
     */
    public void createProjectResource(int projectId, String resourceName, Object reservedParam) throws CmsException {
        // do not create entries for online-project
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            readProjectResource(projectId, resourceName, reservedParam);
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
            stmt.setString(2, resourceName);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Creates a serializable object in the systempropertys.
     *
     * @param name The name of the property.
     * @param object The property-object.
     *
     * @return object The property-object.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Serializable createSystemProperty(String name, Serializable object) throws CmsException {

        byte[] value;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            // serialize the object
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value = bout.toByteArray();

            // create the object
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SYSTEMPROPERTIES_WRITE");
            stmt.setInt(1, m_sqlManager.nextId(C_TABLE_SYSTEMPROPERTIES));
            stmt.setString(2, name);
            m_sqlManager.setBytes(stmt, 3, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e, false);
        } finally {
            value = null;
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
        return readSystemProperty(name);
    }

    /**
     * Deletes a project from the cms.
     * Therefore it deletes all files, resources and properties.
     *
     * @param project the project to delete.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void deleteProject(CmsProject project) throws CmsException {

        // delete the resources from project_resources
        deleteProjectResources(project);
        
        // remove the project id form all resources within theis project
        unmarkProjectResources(project);

        // finally delete the project
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_DELETE");
            // create the statement
            stmt.setInt(1, project.getId());
            stmt.executeUpdate();
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
    
    
    /**
     * delete a projectResource from an given CmsResource object.<p>
     *
     * @param projectId id of the project in which the resource is used
     * @param resourceName name of the resource to be deleted from the Cms
     * @throws CmsException if something goes wrong
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
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_DELETEALL");
            stmt.setInt(1, project.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
            m_sqlManager.closeAll(conn, stmt, null);
        }        
    }

    /**
     * Deletes a serializable object from the systempropertys.
     *
     * @param name The name of the property.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void deleteSystemProperty(String name) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        // this method is currently unused- dont delete it anyway!

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SYSTEMPROPERTIES_DELETE");
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
        CmsGroup guests = m_driverManager.getUserDriver().createGroup(CmsUUID.getConstantUUID(guestGroup), guestGroup, "The guest group", I_CmsConstants.C_FLAG_ENABLED, null, null);
        String administratorsGroup = OpenCms.getDefaultUsers().getGroupAdministrators();
        CmsGroup administrators = m_driverManager.getUserDriver().createGroup(CmsUUID.getConstantUUID(administratorsGroup), administratorsGroup, "The administrators group", I_CmsConstants.C_FLAG_ENABLED | I_CmsConstants.C_FLAG_GROUP_PROJECTMANAGER, null, null);
        String usersGroup = OpenCms.getDefaultUsers().getGroupUsers();
        CmsGroup users = m_driverManager.getUserDriver().createGroup(CmsUUID.getConstantUUID(usersGroup), usersGroup, "The users group", I_CmsConstants.C_FLAG_ENABLED | I_CmsConstants.C_FLAG_GROUP_ROLE | I_CmsConstants.C_FLAG_GROUP_PROJECTCOWORKER, null, null);
        String projectmanagersGroup = OpenCms.getDefaultUsers().getGroupProjectmanagers();
        CmsGroup projectmanager = m_driverManager.getUserDriver().createGroup(CmsUUID.getConstantUUID(projectmanagersGroup), projectmanagersGroup, "The projectmanager group", I_CmsConstants.C_FLAG_ENABLED | I_CmsConstants.C_FLAG_GROUP_PROJECTMANAGER | I_CmsConstants.C_FLAG_GROUP_PROJECTCOWORKER | I_CmsConstants.C_FLAG_GROUP_ROLE, users.getName(), null);

        // add the users
        String guestUser = OpenCms.getDefaultUsers().getUserGuest();
        CmsUser guest = m_driverManager.getUserDriver().importUser(CmsUUID.getConstantUUID(guestUser), guestUser, m_driverManager.getUserDriver().encryptPassword(""), m_driverManager.getUserDriver().encryptPassword(""), "The guest user", " ", " ", " ", 0, 0, I_CmsConstants.C_FLAG_ENABLED, new Hashtable(), guests, " ", " ", I_CmsConstants.C_USER_TYPE_SYSTEMUSER, null);
        String adminUser = OpenCms.getDefaultUsers().getUserAdmin();
        CmsUser admin = m_driverManager.getUserDriver().importUser(CmsUUID.getConstantUUID(adminUser), adminUser, m_driverManager.getUserDriver().encryptPassword("admin"), m_driverManager.getUserDriver().encryptPassword(""), "The admin user", " ", " ", " ", 0, 0, I_CmsConstants.C_FLAG_ENABLED, new Hashtable(), administrators, " ", " ", I_CmsConstants.C_USER_TYPE_SYSTEMUSER, null);
        m_driverManager.getUserDriver().createUserInGroup(guest.getId(), guests.getId(), null);
        m_driverManager.getUserDriver().createUserInGroup(admin.getId(), administrators.getId(), null);
        // add the export user (if it is not set to guest or admin)
        if (!OpenCms.getDefaultUsers().getUserExport().equals(OpenCms.getDefaultUsers().getUserAdmin()) 
                && !OpenCms.getDefaultUsers().getUserExport().equals(OpenCms.getDefaultUsers().getUserGuest())) {
            String exportUser = OpenCms.getDefaultUsers().getUserExport();
            CmsUser export = m_driverManager.getUserDriver().importUser(CmsUUID.getConstantUUID(exportUser), exportUser, m_driverManager.getUserDriver().encryptPassword((new CmsUUID()).toString()), m_driverManager.getUserDriver().encryptPassword(""), "The static export user", " ", " ", " ", 0, 0, I_CmsConstants.C_FLAG_ENABLED, new Hashtable(), guests, " ", " ", I_CmsConstants.C_USER_TYPE_SYSTEMUSER, null);
            m_driverManager.getUserDriver().createUserInGroup(export.getId(), guests.getId(), null);
        }
        m_driverManager.getWorkflowDriver().writeTaskType(1, 0, "../taskforms/adhoc.asp", "Ad-Hoc", "30308", 1, 1);

        ////////////////////////////////////////////////////////////////////////////////////////////
        // online project stuff
        ////////////////////////////////////////////////////////////////////////////////////////////        

        // create the online project
        CmsTask task = m_driverManager.getWorkflowDriver().createTask(0, 0, 1, admin.getId(), admin.getId(), administrators.getId(), I_CmsConstants.C_PROJECT_ONLINE, new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), I_CmsConstants.C_TASK_PRIORITY_NORMAL);
        CmsProject online = createProject(admin, users /* guests */
        , projectmanager, task, I_CmsConstants.C_PROJECT_ONLINE, "The Online Project", I_CmsConstants.C_FLAG_ENABLED, I_CmsConstants.C_PROJECT_TYPE_NORMAL, null);

        // create the root-folder for the online project
        CmsFolder onlineRootFolder = m_driverManager.getVfsDriver().createFolder(online, CmsUUID.getNullUUID(), CmsUUID.getNullUUID(), "/", 0, 0, admin.getId(), 0, admin.getId());
        onlineRootFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
        m_driverManager.getVfsDriver().writeFolder(online, onlineRootFolder, CmsDriverManager.C_UPDATE_ALL, onlineRootFolder.getUserLastModified());

        ////////////////////////////////////////////////////////////////////////////////////////////
        // setup project stuff
        ////////////////////////////////////////////////////////////////////////////////////////////

        // create the task for the setup project
        task = m_driverManager.getWorkflowDriver().createTask(0, 0, 1, admin.getId(), admin.getId(), administrators.getId(), "_setupProject", new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), I_CmsConstants.C_TASK_PRIORITY_NORMAL);
        CmsProject setup = createProject(admin, administrators, administrators, task, "_setupProject", "Initial site import", I_CmsConstants.C_FLAG_ENABLED, I_CmsConstants.C_PROJECT_TYPE_TEMPORARY, null);

        // create the root-folder for the offline project
        CmsFolder setupRootFolder = m_driverManager.getVfsDriver().createFolder(setup, onlineRootFolder, CmsUUID.getNullUUID());
        setupRootFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
        m_driverManager.getVfsDriver().writeFolder(setup, setupRootFolder, CmsDriverManager.C_UPDATE_ALL, setupRootFolder.getUserLastModified());

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
     * @see org.opencms.db.I_CmsDriver#init(org.apache.commons.collections.ExtendedProperties, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(ExtendedProperties configuration, List successiveDrivers, CmsDriverManager driverManager) {
        String poolUrl = configuration.getString("db.project.pool");

        m_sqlManager = this.initQueries();
        m_sqlManager.setPoolUrlOffline(poolUrl);
        m_sqlManager.setPoolUrlOnline(poolUrl);
        m_sqlManager.setPoolUrlBackup(poolUrl);

        m_driverManager = driverManager;

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Assigned pool        : " + poolUrl);
        }

        // add this class as an event handler to the Cms event listener
        OpenCms.addCmsEventListener(this, new int[] { 
            I_CmsEventListener.EVENT_UPDATE_EXPORTS,
            I_CmsEventListener.EVENT_PUBLISH_PROJECT 
        });        

        if (successiveDrivers != null && !successiveDrivers.isEmpty()) {
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isWarnEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).warn(this.getClass().toString() + " does not support successive drivers");
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#initQueries()
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.generic.CmsSqlManager();
    }

    /**
     * Resets the state to UNCHANGED and the last-modified-in-project-ID to 0 for a specified resource.<p>
     * 
     * @param context the current request context
     * @param resource the Cms resource
     * @throws CmsException if something goes wrong
     */
    protected void internalResetResourceState(CmsRequestContext context, CmsResource resource) throws CmsException {
        try {
            // reset the resource state and the last-modified-in-project ID offline
            if (resource.getState() != I_CmsConstants.C_STATE_UNCHANGED) {
                resource.setState(I_CmsConstants.C_STATE_UNCHANGED);
                m_driverManager.getVfsDriver().writeResourceState(context.currentProject(), resource, CmsDriverManager.C_UPDATE_ALL);
            }

            //m_driverManager.getVfsDriver().writeLastModifiedProjectId(context.currentProject(), 0, resource);
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error reseting resource state of " + resource.toString(), e);
            }

            throw e;
        }        
    }
    
    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishDeletedFolder(org.opencms.file.CmsRequestContext, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsFolder, boolean, long, org.opencms.util.CmsUUID, int, int)
     */
    public void publishDeletedFolder(CmsRequestContext context, I_CmsReport report, int m, int n, CmsProject onlineProject, CmsFolder currentFolder, boolean backupEnabled, long publishDate, CmsUUID publishHistoryId, int backupTagId, int maxVersions) throws Exception {
        CmsFolder onlineFolder = null;
        Map offlineProperties = null;

        try {
            report.print("( " + m + " / " + n + " ) " + report.key("report.deleting.folder"), I_CmsReport.C_FORMAT_NOTE);
            report.print(context.removeSiteRoot(currentFolder.getRootPath()));
            report.print(report.key("report.dots"));

            try {
                // write the folder to the backup and publishing history                
                if (backupEnabled) {
                    offlineProperties = m_driverManager.getVfsDriver().readProperties(context.currentProject().getId(), currentFolder, currentFolder.getType());
                    m_driverManager.getBackupDriver().writeBackupResource(context.currentUser(), context.currentProject(), currentFolder, offlineProperties, backupTagId, publishDate, maxVersions);
                }               
                
                m_driverManager.getProjectDriver().writePublishHistory(context.currentProject(), publishHistoryId, backupTagId, currentFolder);
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error writing backup/publishing history of " + currentFolder.toString(), e);
                }

                throw e;
            }

            try {
                // read the folder online
                onlineFolder = m_driverManager.readFolder(context, currentFolder.getStructureId(), true);
                onlineFolder.setFullResourceName(currentFolder.getRootPath());
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error reading resource " + currentFolder.toString(), e);
                }

                throw e;
            }

            try {
                // delete the properties online and offline
                m_driverManager.getVfsDriver().deleteProperties(onlineProject.getId(), onlineFolder);
                m_driverManager.getVfsDriver().deleteProperties(context.currentProject().getId(), currentFolder);
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error deleting properties of " + currentFolder.toString(), e);
                }

                throw e;
            }

            try {
                // remove the folder online and offline
                m_driverManager.getVfsDriver().removeFolder(onlineProject, currentFolder);
                m_driverManager.getVfsDriver().removeFolder(context.currentProject(), currentFolder);
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error removing resource " + currentFolder.toString(), e);
                }

                throw e;
            }

            try {
                // remove the ACL online and offline
                m_driverManager.getUserDriver().removeAccessControlEntries(onlineProject, onlineFolder.getResourceId());
                m_driverManager.getUserDriver().removeAccessControlEntries(context.currentProject(), currentFolder.getResourceId());
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
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", currentFolder)));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFile(org.opencms.file.CmsRequestContext, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.Set, boolean, long, org.opencms.util.CmsUUID, int, int)
     */
    public void publishFile(CmsRequestContext context, I_CmsReport report, int m, int n, CmsProject onlineProject, CmsResource offlineFileHeader, Set publishedContentIds, boolean backupEnabled, long publishDate, CmsUUID publishHistoryId, int backupTagId, int maxVersions) throws Exception {    
        // CmsFile offlineFile = null;
        CmsFile newFile = null;
        CmsResource onlineFileHeader = null;
        Map offlineProperties = null;

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
                    onlineFileHeader = m_driverManager.getVfsDriver().readFileHeader(onlineProject.getId(), offlineFileHeader.getStructureId(), true);
                    onlineFileHeader.setFullResourceName(offlineFileHeader.getRootPath());
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
                    if (backupEnabled) {
                        // TODO this feature might be removed or modified for future backup implementations
                        /*
                        if (offlineFile == null) {
                            offlineFile = m_driverManager.getVfsDriver().readFile(context.currentProject().getId(), true, offlineFileHeader.getStructureId());
                            offlineFile.setFullResourceName(offlineFileHeader.getRootPath());
                        }
                        
                        if (offlineProperties == null) {
                            offlineProperties = m_driverManager.getVfsDriver().readProperties(context.currentProject().getId(), offlineFileHeader, offlineFileHeader.getType());
                        }
                        m_driverManager.getBackupDriver().writeBackupResource(context.currentUser(), context.currentProject(), offlineFile, offlineProperties, backupTagId, publishDate, maxVersions);
                        */

                        // delete all backups as well
                        m_driverManager.deleteBackup(offlineFileHeader);                                                                 
                    }
                    
                    m_driverManager.getProjectDriver().writePublishHistory(context.currentProject(), publishHistoryId, backupTagId, offlineFileHeader);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error writing backup/publishing history of " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }                

                try {
                    // delete the properties online and offline
                    m_driverManager.getVfsDriver().deleteProperties(onlineProject.getId(), onlineFileHeader);
                    m_driverManager.getVfsDriver().deleteProperties(context.currentProject().getId(), offlineFileHeader);

                    // if the offline file has a resource ID different from the online file
                    // (probably because a (deleted) file was replaced by a new file with the
                    // same name), the properties with the "old" resource ID have to be
                    // deleted also offline
                    if (!onlineFileHeader.getResourceId().equals(offlineFileHeader.getResourceId())) {
                        m_driverManager.getVfsDriver().deleteProperties(context.currentProject().getId(), onlineFileHeader);
                    }
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error deleting properties of " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }

                try {
                    // remove the file online and offline
                    m_driverManager.getVfsDriver().removeFile(onlineProject, onlineFileHeader, true);
                    m_driverManager.getVfsDriver().removeFile(context.currentProject(), offlineFileHeader, true);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error removing resource " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }

                try {
                    // delete the ACL online and offline
                    m_driverManager.getUserDriver().removeAccessControlEntries(onlineProject, onlineFileHeader.getResourceId());
                    m_driverManager.getUserDriver().removeAccessControlEntries(context.currentProject(), offlineFileHeader.getResourceId());
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
                    onlineFileHeader = m_driverManager.getVfsDriver().readFileHeader(onlineProject.getId(), offlineFileHeader.getStructureId(), false);
                    onlineFileHeader.setFullResourceName(offlineFileHeader.getRootPath());                    

                    // reset the labeled link flag before writing the online file
                    int flags = offlineFileHeader.getFlags();
                    flags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
                    offlineFileHeader.setFlags(flags);                   

                    // delete the properties online
                    m_driverManager.getVfsDriver().deleteProperties(onlineProject.getId(), onlineFileHeader);

                    // if the offline file has a resource ID different from the online file
                    // (probably because a (deleted) file was replaced by a new file with the
                    // same name), the properties with the "old" resource ID have to be
                    // deleted also offline
                    if (!onlineFileHeader.getResourceId().equals(offlineFileHeader.getResourceId())) {
                        m_driverManager.getVfsDriver().deleteProperties(context.currentProject().getId(), onlineFileHeader);
                    }

                    // remove the file online
                    boolean removeContent = !publishedContentIds.contains(offlineFileHeader.getFileId());
                    m_driverManager.getVfsDriver().removeFile(onlineProject, onlineFileHeader, removeContent);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error deleting properties of " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }

                try {
                    // publish the file content
                    newFile = m_driverManager.getProjectDriver().publishFileContent(context.currentProject(), onlineProject, offlineFileHeader, publishedContentIds);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error creating resource " + offlineFileHeader.toString(), e);
                    }

                    throw e;
                }

                try {
                    // write the properties online
                    offlineProperties = m_driverManager.getVfsDriver().readProperties(context.currentProject().getId(), offlineFileHeader, offlineFileHeader.getType());
                    m_driverManager.getVfsDriver().writeProperties(offlineProperties, onlineProject.getId(), newFile, newFile.getType(), true);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error writing properties of " + newFile.toString(), e);
                    }

                    throw e;
                }

                try {
                    // write the ACL online
                    m_driverManager.getUserDriver().publishAccessControlEntries(context.currentProject(), onlineProject, newFile.getResourceId(), onlineFileHeader.getResourceId());
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
                            offlineProperties = m_driverManager.getVfsDriver().readProperties(context.currentProject().getId(), offlineFileHeader, offlineFileHeader.getType());
                        }                        
                        m_driverManager.getBackupDriver().writeBackupResource(context.currentUser(), context.currentProject(), newFile, offlineProperties, backupTagId, publishDate, maxVersions);
                                  
                        // TODO for later use
                        //CmsBackupResource backupRes= m_driverManager.getBackupDriver().writeBackupResource(context.currentUser(), context.currentProject(), offlineFile, offlineProperties, backupTagId, publishDate, maxVersions);                        
                        //m_driverManager.getBackupDriver().writeBackupResourceContent(context.currentProject().getId(), offlineFileHeader, backupRes);
                    }
                    
                    m_driverManager.getProjectDriver().writePublishHistory(context.currentProject(), publishHistoryId, backupTagId, offlineFileHeader);
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
                    newFile = m_driverManager.getProjectDriver().publishFileContent(context.currentProject(), onlineProject, offlineFileHeader, publishedContentIds);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Caught error "+e.getType(), e);
                    }
                    if (e.getType() == CmsException.C_FILE_EXISTS) {
                        try {
                            // remove the existing file and ensure that it's content is written 
                            // in any case by removing it's content ID from the set of published content IDs
                            m_driverManager.getVfsDriver().removeFile(onlineProject, offlineFileHeader, true);
                            publishedContentIds.remove(offlineFileHeader.getFileId());
                            newFile = m_driverManager.getProjectDriver().publishFileContent(context.currentProject(), onlineProject, offlineFileHeader, publishedContentIds);
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
                    offlineProperties = m_driverManager.getVfsDriver().readProperties(context.currentProject().getId(), offlineFileHeader, offlineFileHeader.getType());
                    m_driverManager.getVfsDriver().writeProperties(offlineProperties, onlineProject.getId(), newFile, newFile.getType(), true);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error writing properties of " + newFile.toString(), e);
                    }

                    throw e;
                }

                try {
                    // write the ACL online
                    m_driverManager.getUserDriver().publishAccessControlEntries(context.currentProject(), onlineProject, offlineFileHeader.getResourceId(), newFile.getResourceId());
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
                            offlineProperties = m_driverManager.getVfsDriver().readProperties(context.currentProject().getId(), offlineFileHeader, offlineFileHeader.getType());
                        } 
                        m_driverManager.getBackupDriver().writeBackupResource(context.currentUser(), context.currentProject(), newFile, offlineProperties, backupTagId, publishDate, maxVersions);
                        
                        // TODO for later use                                    
                        //CmsBackupResource backupRes= m_driverManager.getBackupDriver().writeBackupResource(context.currentUser(), context.currentProject(), offlineFile, offlineProperties, backupTagId, publishDate, maxVersions);
                        //m_driverManager.getBackupDriver().writeBackupResourceContent(context.currentProject().getId(), offlineFileHeader, backupRes);
                    }
                    
                    m_driverManager.getProjectDriver().writePublishHistory(context.currentProject(), publishHistoryId, backupTagId, offlineFileHeader);
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
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", offlineFileHeader)));
        }
    }
    
    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishFileContent(org.opencms.file.CmsProject, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.Set)
     */
    public CmsFile publishFileContent(CmsProject offlineProject, CmsProject onlineProject, CmsResource offlineFileHeader, Set publishedContentIds) throws Exception {
        CmsFile newFile = null;
        CmsFile offlineFile = null;

        try {
            // binary content gets only published once while a project is published
            if (!offlineFileHeader.getFileId().isNullUUID() && !publishedContentIds.contains(offlineFileHeader.getFileId())) {
                // read the file offline
                offlineFile = m_driverManager.getVfsDriver().readFile(offlineProject.getId(), false, offlineFileHeader.getStructureId());
                offlineFile.setFullResourceName(offlineFileHeader.getRootPath());

                // create the file online              
                newFile = (CmsFile) offlineFile.clone();
                newFile.setState(I_CmsConstants.C_STATE_UNCHANGED);
                newFile.setFullResourceName(offlineFileHeader.getRootPath());                
                m_driverManager.getVfsDriver().createFile(onlineProject, newFile, offlineFile.getUserCreated(), newFile.getParentStructureId(), newFile.getName());

                // update the online/offline structure and resource records of the file
                m_driverManager.getVfsDriver().writeResource(onlineProject, newFile, offlineFile, false);

                // add the content ID to the content IDs that got already published
                publishedContentIds.add(offlineFileHeader.getFileId());
            } else {
                // create the sibling online
                m_driverManager.getVfsDriver().createSibling(onlineProject, offlineFileHeader, offlineFileHeader.getUserCreated(), offlineFileHeader.getParentStructureId(), offlineFileHeader.getName());

                newFile = m_driverManager.getVfsDriver().readFile(onlineProject.getId(), false, offlineFileHeader.getStructureId());
                newFile.setFullResourceName(offlineFileHeader.getRootPath());                
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
     * @see org.opencms.db.I_CmsProjectDriver#publishFolder(org.opencms.file.CmsRequestContext, org.opencms.report.I_CmsReport, int, int, org.opencms.file.CmsProject, org.opencms.file.CmsFolder, boolean, long, org.opencms.util.CmsUUID, int, int)
     */
    public void publishFolder(CmsRequestContext context, I_CmsReport report, int m, int n, CmsProject onlineProject, CmsFolder offlineFolder, boolean backupEnabled, long publishDate, CmsUUID publishHistoryId, int backupTagId, int maxVersions) throws Exception {
        CmsFolder newFolder = null;
        CmsFolder onlineFolder = null;
        Map offlineProperties = null;

        try {
            report.print("( " + m + " / " + n + " ) " + report.key("report.publishing.folder"), I_CmsReport.C_FORMAT_NOTE);
            report.print(context.removeSiteRoot(offlineFolder.getRootPath()));
            report.print(report.key("report.dots"));

            if (offlineFolder.getState() == I_CmsConstants.C_STATE_NEW) {
                try {
                    // create the folder online
                    newFolder = (CmsFolder) offlineFolder.clone();
                    newFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
                    newFolder.setFullResourceName(offlineFolder.getRootPath());
                    onlineFolder = m_driverManager.getVfsDriver().createFolder(onlineProject, newFolder, newFolder.getParentStructureId());
                    onlineFolder.setFullResourceName(offlineFolder.getRootPath());
                } catch (CmsException e) {
                    if (e.getType() == CmsException.C_FILE_EXISTS) {
                        try {
                            onlineFolder = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), newFolder.getStructureId());
                            onlineFolder.setFullResourceName(offlineFolder.getRootPath());
                            m_driverManager.getVfsDriver().writeResource(onlineProject, onlineFolder, offlineFolder, false);
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
                    onlineFolder.setFullResourceName(offlineFolder.getRootPath());
                } catch (CmsException e) {
                    if (e.getType() == CmsException.C_NOT_FOUND) {
                        try {
                            onlineFolder = m_driverManager.getVfsDriver().createFolder(onlineProject, offlineFolder, offlineFolder.getParentStructureId());
                            onlineFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
                            onlineFolder.setFullResourceName(offlineFolder.getRootPath());
                            m_driverManager.getVfsDriver().writeResourceState(context.currentProject(), onlineFolder, CmsDriverManager.C_UPDATE_ALL);
                        } catch (CmsException e1) {
                            if (OpenCms.getLog(this).isErrorEnabled()) {
                                OpenCms.getLog(this).error("Error creating resource " + offlineFolder.toString(), e1);
                            }

                            throw e1;
                        }
                    } else {
                        if (OpenCms.getLog(this).isErrorEnabled()) {
                            OpenCms.getLog(this).error("Error reading resource " + offlineFolder.toString(), e);
                        }

                        throw e;   
                    }
                }

                try {
                    // update the folder online
                    m_driverManager.getVfsDriver().writeResource(onlineProject, onlineFolder, offlineFolder, false);
                } catch (CmsException e) {
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Error updating resource " + offlineFolder.toString(), e);
                    }

                    throw e;
                }
            }
            
            try {
                // write the ACL online
                m_driverManager.getUserDriver().publishAccessControlEntries(context.currentProject(), onlineProject, offlineFolder.getResourceId(), onlineFolder.getResourceId());
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error writing ACLs of " + offlineFolder.toString(), e);
                }

                throw e;
            }

            try {
                // write the properties online
                m_driverManager.getVfsDriver().deleteProperties(onlineProject.getId(), onlineFolder);
                offlineProperties = m_driverManager.getVfsDriver().readProperties(context.currentProject().getId(), offlineFolder, offlineFolder.getType());
                m_driverManager.getVfsDriver().writeProperties(offlineProperties, onlineProject.getId(), onlineFolder, offlineFolder.getType(), true);
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
                        offlineProperties = m_driverManager.getVfsDriver().readProperties(context.currentProject().getId(), offlineFolder, offlineFolder.getType());
                    }
                    
                    m_driverManager.getBackupDriver().writeBackupResource(context.currentUser(), context.currentProject(), offlineFolder, offlineProperties, backupTagId, publishDate, maxVersions);
                   }
                
                m_driverManager.getProjectDriver().writePublishHistory(context.currentProject(), publishHistoryId, backupTagId, offlineFolder);
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
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", offlineFolder)));
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#publishProject(org.opencms.file.CmsRequestContext, org.opencms.report.I_CmsReport, org.opencms.file.CmsProject, org.opencms.db.CmsPublishList, boolean, int, int)
     */
    public synchronized void publishProject(CmsRequestContext context, I_CmsReport report, CmsProject onlineProject, CmsPublishList publishList, boolean backupEnabled, int backupTagId, int maxVersions) throws Exception {
        CmsFolder currentFolder = null;
        CmsResource currentFileHeader = null;
        long publishDate = System.currentTimeMillis();
        Iterator i = null;
        int n;
        int publishedFolderCount = 0;
        int deletedFolderCount = 0;
        int publishedFileCount = 0;
        Set publishedContentIds = (Set) new HashSet();

        try {
            ////////////////////////////////////////////////////////////////////////////////////////
            
            // write the backup
            
            if (backupEnabled) {
                // write an entry in the publish project log
                m_driverManager.backupProject(context, context.currentProject(), backupTagId, publishDate);
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
                currentFolder = (CmsFolder) i.next();

                if (currentFolder.getState() == I_CmsConstants.C_STATE_NEW) {
                    // bounce the current publish task through all project drivers
                    m_driverManager.getProjectDriver().publishFolder(context, report, ++publishedFolderCount, n, onlineProject, currentFolder, backupEnabled, publishDate, publishList.getPublishHistoryId(), backupTagId, maxVersions);
                    // reset the resource state to UNCHANGED and the last-modified-in-project-ID to 0
                    internalResetResourceState(context, currentFolder);
                } else if (currentFolder.getState() == I_CmsConstants.C_STATE_CHANGED) {
                    // bounce the current publish task through all project drivers
                    m_driverManager.getProjectDriver().publishFolder(context, report, ++publishedFolderCount, n, onlineProject, currentFolder, backupEnabled, publishDate, publishList.getPublishHistoryId(), backupTagId, maxVersions);
                    // reset the resource state to UNCHANGED and the last-modified-in-project-ID to 0
                    internalResetResourceState(context, currentFolder);
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
                currentFileHeader = (CmsResource) i.next();
                // bounce the current publish task through all project drivers
                m_driverManager.getProjectDriver().publishFile(context, report, ++publishedFileCount, n, onlineProject, currentFileHeader, publishedContentIds, backupEnabled, publishDate, publishList.getPublishHistoryId(), backupTagId, maxVersions);

                if (currentFileHeader.getState() != I_CmsConstants.C_STATE_DELETED) {
                    // reset the resource state to UNCHANGED and the last-modified-in-project-ID to 0
                    internalResetResourceState(context, currentFileHeader);
                }

                // set back all vars. inside the while loop!
                currentFileHeader = null;
            }

            if (n > 0) {
                report.println(report.key("report.publish_files_end"), I_CmsReport.C_FORMAT_HEADLINE);
            }

            ////////////////////////////////////////////////////////////////////////////////////////

            // publish deleted folders
            
            if (publishList.getDeletedFolderList().isEmpty()) {
                return;
            }

            deletedFolderCount = 0;
            n = publishList.getDeletedFolderList().size();
            i = publishList.getDeletedFolderList().iterator();

            if (n > 0) {
                report.println(report.key("report.publish_delete_folders_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            }

            while (i.hasNext()) {
                currentFolder = (CmsFolder) i.next();               
                // bounce the current publish task through all project drivers
                m_driverManager.getProjectDriver().publishDeletedFolder(context, report, ++deletedFolderCount, n, onlineProject, currentFolder, backupEnabled, publishDate, publishList.getPublishHistoryId(), backupTagId, maxVersions);
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
            OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP, false));

            // force a complete object finalization and garbage collection 
            System.runFinalization();
            Runtime.getRuntime().runFinalization();
            System.gc();
            Runtime.getRuntime().gc();

            throw o;
        } finally {
            currentFileHeader = null;
            currentFolder = null;

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
     * Reads a project by task-id.<p>
     *
     * @param task the task to read the project for
     * @return the project the tasks belongs to
     * @throws CmsException if something goes wrong
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
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return project;
    }

    /**
     * Reads a project.<p>
     *
     * @param id the id of the project
     * @return the project with the given id
     * @throws CmsException if something goes wrong
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
                        CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_PROJECTS_PROJECT_CREATEDATE")),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_TYPE")));
            } else {
                // project not found!
                throw m_sqlManager.getCmsException(this, "project with ID " + id + " not found", CmsException.C_NOT_FOUND, null, true);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readProject(int)/1 ", CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
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
                            CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_PROJECTS_PROJECT_CREATEDATE")),
                            res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_TYPE")));
            } else {
                // project not found!
                throw m_sqlManager.getCmsException(this, "project with name " + name + " not found", CmsException.C_NOT_FOUND, null, true);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readProject(String)/1 ", CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        
        return project;
    }
    

    /**
     * Reads log entries for a project.<p>
     *
     * @param projectid the project for tasklog to read
     * @return a vector of new TaskLog objects
     * @throws CmsException if something goes wrong
     */
    public Vector readProjectLogs(int projectid) throws CmsException {
        ResultSet res = null;
        Connection conn = null;

        CmsTaskLog tasklog = null;
        Vector logs = new Vector();
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
                logs.addElement(tasklog);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return logs;
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#readProjectResource(int, java.lang.String, java.lang.Object)
     */
    public String readProjectResource(int projectId, String resourcename, Object reservedParam) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        ResultSet res = null;
        String resName = null;

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
            stmt.setString(2, resourcename);
            res = stmt.executeQuery();

            if (res.next()) {
                resName = res.getString("RESOURCE_NAME");
            } else {
                throw new CmsException("[" + this.getClass().getName() + ".readProjectResource] " + resourcename, CmsException.C_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
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
        List result = (List) new ArrayList();

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READ_BY_ID");
            stmt.setInt(1, project.getId());
            res = stmt.executeQuery();

            while (res.next()) {
                result.add(res.getString("RESOURCE_NAME"));
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return result;
    }

    /**
     * Returns all projects, with the overgiven state.<p>
     *
     * @param state The state of the projects to read
     * @return a Vector of projects
     * @throws CmsException if something goes wrong
     */
    public Vector readProjects(int state) throws CmsException {
        Vector projects = new Vector();
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
                projects.addElement(
                    new CmsProject(
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_ID")),
                        res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_NAME")),
                        res.getString(m_sqlManager.readQuery("C_PROJECTS_PROJECT_DESCRIPTION")),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_TASK_ID")),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_USER_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_GROUP_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.readQuery("C_PROJECTS_MANAGERGROUP_ID"))),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_FLAGS")),
                        CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_PROJECTS_PROJECT_CREATEDATE")),
                        res.getInt(m_sqlManager.readQuery("C_PROJECTS_PROJECT_TYPE"))));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, "getAllProjects(int)", CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (projects);
    }

    /**
     * Returns all projects, which are accessible by a group.<p>
     *
     * @param group the requesting group
     * @return a Vector of projects
     * @throws CmsException if something goes wrong
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
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (projects);
    }

    /**
     * Returns all projects, which are manageable by a group.<p>
     *
     * @param group The requesting group
     * @return a Vector of projects
     * @throws CmsException if something goes wrong
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
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (projects);
    }

    /**
     * Returns all projects, which are owned by a user.<p>
     *
     * @param user The requesting user
     * @return a Vector of projects
     * @throws CmsException if something goes wrong
     */
    public Vector readProjectsForUser(CmsUser user) throws CmsException {
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
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (projects);
    }

    /**
     * Reads all resource from the Cms, that are in one project.<BR/>
     * A resource is either a file header or a folder.
     *
     * @param project The id of the project in which the resource will be used.
     * @param filter The filter for the resources to be read
     * @return A Vecor of resources.
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public List readProjectView(int project, String filter) throws CmsException {
        List resources = (List) new ArrayList();
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
            stmt = m_sqlManager.getPreparedStatementForSql(conn, CmsSqlManager.replaceTableKey(I_CmsConstants.C_PROJECT_ONLINE_ID + 1, query));

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
            m_sqlManager.closeAll(conn, stmt, res);
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
        CmsUUID contentId = null;
        String rootPath = null;
        int resourceType = I_CmsConstants.C_UNKNOWN_ID;
        int resourceState = I_CmsConstants.C_UNKNOWN_ID;
        List publishedResources = (List) new ArrayList();
        int siblingCount = I_CmsConstants.C_UNKNOWN_ID;  
        CmsUUID masterId = CmsUUID.getNullUUID();
        String contentDefinitionName = null;   
        int backupTagId = I_CmsConstants.C_UNKNOWN_ID;  

        try {
            conn = m_sqlManager.getConnection(projectId);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SELECT_PUBLISHED_RESOURCES");
            stmt.setString(1, publishHistoryId.toString());
            res = stmt.executeQuery();
            
            while (res.next()) {
                structureId = new CmsUUID(res.getString(1));
                resourceId = new CmsUUID(res.getString(2));
                contentId = new CmsUUID(res.getString(3));                
                rootPath = res.getString(4);
                resourceState = res.getInt(5);
                resourceType = res.getInt(6);
                siblingCount = res.getInt(7);
                masterId = new CmsUUID(res.getString(8));
                contentDefinitionName = res.getString(9);
                backupTagId = res.getInt(10);
                
                if (masterId.equals(CmsUUID.getNullUUID())) {
                    publishedResources.add(new CmsPublishedResource(structureId, resourceId, contentId, backupTagId, rootPath, resourceType, resourceState, siblingCount));
                } else {
                    publishedResources.add(new CmsPublishedResource(contentDefinitionName, masterId, resourceType, resourceState));
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }  
        
        return publishedResources;      
    }

    /**
     * Reads a serializable object from the systempropertys.
     *
     * @param name The name of the property.
     * @return object The property-object.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Serializable readSystemProperty(String name) throws CmsException {

        Serializable property = null;
        byte[] value;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        // create get the property data from the database
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SYSTEMPROPERTIES_READ");
            stmt.setString(1, name);
            res = stmt.executeQuery();
            if (res.next()) {
                value = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_SYSTEMPROPERTY_VALUE"));
                // now deserialize the object
                ByteArrayInputStream bin = new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                property = (Serializable) oin.readObject();
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e, false);
        } catch (ClassNotFoundException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_CLASSLOADER_ERROR, e, false);
        } finally {
            value = null;
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return property;
    }


    /**
     * @see org.opencms.db.I_CmsProjectDriver#unmarkProjectResources(org.opencms.file.CmsProject)
     */
    public void unmarkProjectResources(CmsProject project) throws CmsException {
        // finally remove the project id form all resources 
  
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_UNMARK");
            // create the statement
            stmt.setInt(1, project.getId());
            stmt.executeUpdate();
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsProjectDriver#writePublishHistory(org.opencms.file.CmsProject, org.opencms.util.CmsUUID, int, org.opencms.file.CmsResource)
     */
    public void writePublishHistory(CmsProject currentProject, CmsUUID publishId, int tagId, CmsResource resource) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(currentProject);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_WRITE_PUBLISH_HISTORY");
            stmt.setInt(1, tagId);
            stmt.setString(2, resource.getStructureId().toString());
            stmt.setString(3, resource.getResourceId().toString());
            stmt.setString(4, resource.getFileId().toString());
            stmt.setString(5, resource.getRootPath());
            stmt.setInt(6, resource.getState());
            stmt.setInt(7, resource.getType());
            stmt.setString(8, publishId.toString());
            stmt.setInt(9, resource.getLinkCount());
            stmt.setString(10, CmsUUID.getNullUUID().toString());
            stmt.setString(11, "");
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
    
    /**
     * @see org.opencms.db.I_CmsProjectDriver#writePublishHistory(org.opencms.file.CmsProject, org.opencms.util.CmsUUID, int, java.lang.String, org.opencms.util.CmsUUID, int, int)
     */
    public void writePublishHistory(CmsProject currentProject, CmsUUID publishId, int tagId, String contentDefinitionName, CmsUUID masterId, int subId, int state) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(currentProject);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_WRITE_PUBLISH_HISTORY");
            stmt.setInt(1, tagId);
            stmt.setString(2, CmsUUID.getNullUUID().toString());
            stmt.setString(3, CmsUUID.getNullUUID().toString());
            stmt.setString(4, CmsUUID.getNullUUID().toString());
            stmt.setString(5, "");
            stmt.setInt(6, state);
            stmt.setInt(7, subId);
            stmt.setString(8, publishId.toString());
            stmt.setInt(9, 0);
            stmt.setString(10, masterId.toString());
            stmt.setString(11, contentDefinitionName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }    

    /**
     * Writes a serializable object to the systemproperties.
     *
     * @param name The name of the property.
     * @param object The property-object.
     *
     * @return object The property-object.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Serializable writeSystemProperty(String name, Serializable object) throws CmsException {
        byte[] value = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            // serialize the object
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(object);
            oout.close();
            value = bout.toByteArray();

            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SYSTEMPROPERTIES_UPDATE");
            m_sqlManager.setBytes(stmt, 1, value);
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e, false);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e, false);
        } finally {
            value = null;
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readSystemProperty(name);
    }
}

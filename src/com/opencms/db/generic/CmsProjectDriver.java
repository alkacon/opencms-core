/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/generic/Attic/CmsProjectDriver.java,v $
 * Date   : $Date: 2003/05/23 16:26:47 $
 * Version: $Revision: 1.4 $
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
import com.opencms.db.CmsDriverManager;
import com.opencms.file.*;
import com.opencms.flex.util.CmsUUID;
import com.opencms.linkmanagement.CmsPageLinks;
import com.opencms.report.I_CmsReport;
import com.opencms.util.SqlHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * This is the generic project driver to execute operations requested by the Cms
 * using the underlying drivers. This code is still messy like a living space.
 *
 * @version $Revision: 1.4 $ $Date: 2003/05/23 16:26:47 $
 * @since 5.1.2
 */
public class CmsProjectDriver extends Object {

    /**
     * Constant to get property from configurations.
     */
    protected static String C_CONFIGURATIONS_DIGEST = "digest";

    /**
     * Constant to get property from configurations.
     */
    protected static String C_CONFIGURATIONS_DIGEST_FILE_ENCODING = "digest.fileencoding";

    /**
     * Constant to get property from configurations.
     */
    protected static String C_CONFIGURATIONS_POOL = "pool";

    /**
     * The maximum amount of tables.
     */
    protected static int C_MAX_TABLES = 18;

    public static int C_RESTYPE_LINK_ID = 2;

    /**
     * The session-timeout value:
     * currently six hours. After that time the session can't be restored.
     */
    public static long C_SESSION_TIMEOUT = 6 * 60 * 60 * 1000;

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_PROJECTS = "CMS_PROJECTS";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_PROPERTIES = "CMS_PROPERTIES";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_PROPERTYDEF = "CMS_PROPERTYDEF";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_SYSTEMPROPERTIES = "CMS_SYSTEMPROPERTIES";
    public static boolean C_USE_TARGET_DATE = true;
    protected CmsDriverManager m_driverManager;

    /**
     * A array containing all max-ids for the tables.
     */
    protected int[] m_maxIds;

    protected com.opencms.db.generic.CmsSqlManager m_sqlManager;

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
    public Serializable addSystemProperty(String name, Serializable object) throws CmsException {

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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
        return readSystemProperty(name);
    }

    /**
     * Private helper method for publihing into the filesystem.
     * test if resource must be written to the filesystem
     *
     * @param filename Name of a resource in the OpenCms system.
     * @return key in exportpoint Hashtable or null.
     */
    protected String checkExport(String filename, Hashtable exportpoints) {

        String key = null;
        String exportpoint = null;
        Enumeration e = exportpoints.keys();

        while (e.hasMoreElements()) {
            exportpoint = (String) e.nextElement();
            if (filename.startsWith(exportpoint)) {
                return exportpoint;
            }
        }
        return key;
    }

    /**
     * Deletes all files in CMS_FILES without fileHeader in CMS_RESOURCES
     *
     *
     */
    protected void clearFilesTable() throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_DELETE_LOST_ID");
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * creates a link entry for each of the link targets in the linktable.
     *
     * @param pageId The resourceId (offline) of the page whose liks should be traced.
     * @param linkTarget A vector of strings (the linkdestinations).
     */
    public void createLinkEntrys(CmsUUID pageId, Vector linkTargets) throws CmsException {
        //first delete old entrys in the database
        deleteLinkEntrys(pageId);
        if (linkTargets == null || linkTargets.size() == 0) {
            return;
        }
        // now write it
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_WRITE_ENTRY");
            stmt.setString(1, pageId.toString());
            for (int i = 0; i < linkTargets.size(); i++) {
                try {
                    stmt.setString(2, (String) linkTargets.elementAt(i));
                    stmt.executeUpdate();
                } catch (SQLException e) {
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "createLinkEntrys(CmsUUID, Vector)", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * creates a link entry for each of the link targets in the online linktable.
     *
     * @param pageId The resourceId (online) of the page whose liks should be traced.
     * @param linkTarget A vector of strings (the linkdestinations).
     */
    public void createOnlineLinkEntrys(CmsUUID pageId, Vector linkTargets) throws CmsException {
        //first delete old entrys in the database
        deleteLinkEntrys(pageId);
        if (linkTargets == null || linkTargets.size() == 0) {
            return;
        }
        // now write it
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_WRITE_ENTRY_ONLINE");
            stmt.setString(1, pageId.toString());
            for (int i = 0; i < linkTargets.size(); i++) {
                try {
                    stmt.setString(2, (String) linkTargets.elementAt(i));
                    stmt.executeUpdate();
                } catch (SQLException e) {
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "createOnlineLinkEntrys(CmsUUID, Vector)", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
    * Creates a project.
    *
    * @param owner The owner of this project.
    * @param group The group for this project.
    * @param managergroup The managergroup for this project.
    * @param task The task.
    * @param name The name of the project to create.
    * @param description The description for the new project.
    * @param flags The flags for the project (e.g. archive).
    * @param type the type for the project (e.g. normal).
    *
    * @throws CmsException Throws CmsException if something goes wrong.
    */
    public CmsProject createProject(CmsUser owner, CmsGroup group, CmsGroup managergroup, CmsTask task, String name, String description, int flags, int type) throws CmsException {

        if ((description == null) || (description.length() < 1)) {
            description = " ";
        }

        Timestamp createTime = new Timestamp(new java.util.Date().getTime());
        Connection conn = null;
        PreparedStatement stmt = null;

        int id = m_sqlManager.nextId(C_TABLE_PROJECTS);

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_CREATE");
            // write data to database
            stmt.setInt(1, id);
            stmt.setString(2, owner.getId().toString());
            stmt.setString(3, group.getId().toString());
            stmt.setString(4, managergroup.getId().toString());
            stmt.setInt(5, task.getId());
            stmt.setString(6, name);
            stmt.setString(7, description);
            stmt.setInt(8, flags);
            stmt.setTimestamp(9, createTime);
            // no publish data
            stmt.setInt(10, type);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readProject(id);
    }

    /**
     * Creates a new projectResource from an given CmsResource object.
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
            m_driverManager.getVfsDriver().readProjectResource(projectId, resourceName);
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * This method creates a new session in the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session.
     * @return data the sessionData.
     */
    public void createSession(String sessionId, Hashtable data) throws CmsException {
        byte[] value = null;

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            value = serializeSession(data);
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SESSION_CREATE");
            // write data to database
            stmt.setString(1, sessionId);
            stmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            m_sqlManager.setBytes(stmt, 3, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Deletes an exportlink from the Cms.
     *
     * @param link the cmsexportlink object to delete.
     * @throws CmsException if something goes wrong.
     */
    public void deleteExportLink(CmsExportLink link) throws CmsException {
        int deleteId = link.getId();
        if (deleteId == 0) {
            CmsExportLink dbLink = readExportLink(link.getLink());
            if (dbLink == null) {
                return;
            } else {
                deleteId = dbLink.getId();
                link.setLinkId(deleteId);
            }
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_LINK_DELETE");
            // delete the link table entry
            stmt.setInt(1, deleteId);
            stmt.executeUpdate();
            // now the dependencies
            try {
                stmt.close();
            } catch (SQLException ex) {
            }
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_DEPENDENCIES_DELETE");
            stmt.setInt(1, deleteId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "deleteExportLink(CmsExportLink)", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Deletes an exportlink from the Cms.
     *
     * @param link the cmsexportlink to delete.
     * @throws CmsException if something goes wrong.
     */
    public void deleteExportLink(String link) throws CmsException {
        CmsExportLink dbLink = readExportLink(link);
        if (dbLink != null) {
            deleteExportLink(dbLink);
        }
    }

    /****************     methods for link management            ****************************/

    /**
     * deletes all entrys in the link table that belong to the pageId
     *
     * @param pageId The resourceId (offline) of the page whose links should be deleted
     */
    public void deleteLinkEntrys(CmsUUID pageId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_DELETE_ENTRYS");
            // delete all project-resources.
            stmt.setString(1, pageId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "deleteLinkEntrys(CmsUUID)", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * deletes all entrys in the online link table that belong to the pageId
     *
     * @param pageId The resourceId (online) of the page whose links should be deleted
     */
    public void deleteOnlineLinkEntrys(CmsUUID pageId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            // CHECK: use online or offline pool here? (was offline before (AZ, 19.05.2003)...)
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_DELETE_ENTRYS_ONLINE");
            // delete all project-resources.
            stmt.setString(1, pageId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "deleteOnlineLinkEntrys(CmsUUID)", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
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
        deleteAllProjectResources(project.getId());

        // delete all lost files
        // Removed because it takes too much time, ednfal 2002/07/19
        //clearFilesTable();

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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Deletes all properties for a project.
     *
     * @param project The project to delete.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProjectProperties(CmsProject project) throws CmsException {

        // delete properties with one statement
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_DELETEALLPROP");
            // create statement
            stmt.setInt(1, project.getId());
            stmt.executeQuery();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
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
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Deletes old sessions.
     */
    public void deleteSessions() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SESSION_DELETE");
            stmt.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis() - C_SESSION_TIMEOUT));
            stmt.execute();
        } catch (Exception e) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error while deleting old sessions: " + com.opencms.util.Utils.getStackTrace(e));
            }
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
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SYSTEMPROPERTIES_DELETE");
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
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
     * Ends a task from the Cms.
     *
     * @param taskid Id of the task to end.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void endTask(int taskId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_END");
            stmt.setInt(1, 100);
            stmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setInt(3, taskId);
            stmt.executeUpdate();

        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Private method to init all default-resources
     */
    // protected 
    public void fillDefaults() throws CmsException {
        try {
            if (readProject(I_CmsConstants.C_PROJECT_ONLINE_ID) != null) {
                // online-project exists - no need of filling defaults
                return;
            }
        } catch (CmsException exc) {
            // ignore the exception - the project was not readable so fill in the defaults
        }

        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsProjectDriver] Initial setup of the OpenCms database starts NOW!");
        }

        // set the groups
        CmsGroup guests = m_driverManager.getUserDriver().createGroup(I_CmsConstants.C_GROUP_GUEST, "the guest-group", I_CmsConstants.C_FLAG_ENABLED, null);
        CmsGroup administrators = m_driverManager.getUserDriver().createGroup(I_CmsConstants.C_GROUP_ADMIN, "the admin-group", I_CmsConstants.C_FLAG_ENABLED | I_CmsConstants.C_FLAG_GROUP_PROJECTMANAGER, null);
        CmsGroup users = m_driverManager.getUserDriver().createGroup(I_CmsConstants.C_GROUP_USERS, "the users-group to access the workplace", I_CmsConstants.C_FLAG_ENABLED | I_CmsConstants.C_FLAG_GROUP_ROLE | I_CmsConstants.C_FLAG_GROUP_PROJECTCOWORKER, I_CmsConstants.C_GROUP_GUEST);
        CmsGroup projectleader = m_driverManager.getUserDriver().createGroup(I_CmsConstants.C_GROUP_PROJECTLEADER, "the projectmanager-group", I_CmsConstants.C_FLAG_ENABLED | I_CmsConstants.C_FLAG_GROUP_PROJECTMANAGER | I_CmsConstants.C_FLAG_GROUP_PROJECTCOWORKER | I_CmsConstants.C_FLAG_GROUP_ROLE, users.getName());

        // add the users
        CmsUser guest = m_driverManager.getUserDriver().addUser(I_CmsConstants.C_USER_GUEST, "", "the guest-user", " ", " ", " ", 0, 0, I_CmsConstants.C_FLAG_ENABLED, new Hashtable(), guests, " ", " ", I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        CmsUser admin = m_driverManager.getUserDriver().addUser(I_CmsConstants.C_USER_ADMIN, "admin", "the admin-user", " ", " ", " ", 0, 0, I_CmsConstants.C_FLAG_ENABLED, new Hashtable(), administrators, " ", " ", I_CmsConstants.C_USER_TYPE_SYSTEMUSER);
        m_driverManager.getUserDriver().addUserToGroup(guest.getId(), guests.getId());
        m_driverManager.getUserDriver().addUserToGroup(admin.getId(), administrators.getId());
        m_driverManager.getWorkflowDriver().writeTaskType(1, 0, "../taskforms/adhoc.asp", "Ad-Hoc", "30308", 1, 1);

        // create the online project
        CmsTask task = m_driverManager.getWorkflowDriver().createTask(0, 0, 1, admin.getId(), admin.getId(), administrators.getId(), I_CmsConstants.C_PROJECT_ONLINE, new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), I_CmsConstants.C_TASK_PRIORITY_NORMAL);
        CmsProject online = createProject(admin, guests, projectleader, task, I_CmsConstants.C_PROJECT_ONLINE, "the online-project", I_CmsConstants.C_FLAG_ENABLED, I_CmsConstants.C_PROJECT_TYPE_NORMAL);

        // create the root-folder for the online project
        CmsUUID siteRootId = CmsUUID.getNullUUID();
        CmsFolder rootFolder = m_driverManager.getVfsDriver().createFolder(admin, online, CmsUUID.getNullUUID(), CmsUUID.getNullUUID(), I_CmsConstants.C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
        m_driverManager.getVfsDriver().writeFolder(online, rootFolder, false);

        // create the folder for the default site
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, online, rootFolder.getResourceId(), CmsUUID.getNullUUID(), I_CmsConstants.C_DEFAULT_SITE + I_CmsConstants.C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
        m_driverManager.getVfsDriver().writeFolder(online, rootFolder, false);
        siteRootId = rootFolder.getResourceId();

        // create the folder for the virtual file system
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, online, siteRootId, CmsUUID.getNullUUID(), I_CmsConstants.C_DEFAULT_SITE + I_CmsConstants.C_ROOTNAME_VFS + I_CmsConstants.C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
        m_driverManager.getVfsDriver().writeFolder(online, rootFolder, false);

        // create the folder for the context objects system
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, online, siteRootId, CmsUUID.getNullUUID(), I_CmsConstants.C_DEFAULT_SITE + I_CmsConstants.C_ROOTNAME_COS + I_CmsConstants.C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
        m_driverManager.getVfsDriver().writeFolder(online, rootFolder, false);

        // create the task for the setup project
        task = m_driverManager.getWorkflowDriver().createTask(0, 0, 1, admin.getId(), admin.getId(), administrators.getId(), "_setupProject", new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), I_CmsConstants.C_TASK_PRIORITY_NORMAL);
        CmsProject setup = createProject(admin, administrators, administrators, task, "_setupProject", "Initial site import", I_CmsConstants.C_FLAG_ENABLED, I_CmsConstants.C_PROJECT_TYPE_TEMPORARY);

        // create the root-folder for the offline project
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, setup, CmsUUID.getNullUUID(), CmsUUID.getNullUUID(), I_CmsConstants.C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
        m_driverManager.getVfsDriver().writeFolder(setup, rootFolder, false);

        // create the folder for the default site
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, setup, rootFolder.getResourceId(), CmsUUID.getNullUUID(), I_CmsConstants.C_DEFAULT_SITE + I_CmsConstants.C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
        m_driverManager.getVfsDriver().writeFolder(setup, rootFolder, false);
        siteRootId = rootFolder.getResourceId();

        // create the folder for the virtual file system
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, setup, siteRootId, CmsUUID.getNullUUID(), I_CmsConstants.C_DEFAULT_SITE + I_CmsConstants.C_ROOTNAME_VFS + I_CmsConstants.C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
        m_driverManager.getVfsDriver().writeFolder(setup, rootFolder, false);

        // create the folder for the context objects system
        rootFolder = m_driverManager.getVfsDriver().createFolder(admin, setup, siteRootId, CmsUUID.getNullUUID(), I_CmsConstants.C_DEFAULT_SITE + I_CmsConstants.C_ROOTNAME_COS + I_CmsConstants.C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
        m_driverManager.getVfsDriver().writeFolder(setup, rootFolder, false);
    }

    protected void finalize() throws Throwable {
        if (m_sqlManager!=null) {
            m_sqlManager.finalize();
        }
        
        m_sqlManager = null;      
        m_driverManager = null;        
    }

    /**
     * Forwards a task to another user.
     *
     * @param taskId The id of the task that will be fowarded.
     * @param newRoleId The new Group the task belongs to
     * @param newUserId User who gets the task.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void forwardTask(int taskId, CmsUUID newRoleId, CmsUUID newUserId) throws CmsException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_FORWARD");
            stmt.setString(1, newRoleId.toString());
            stmt.setString(2, newUserId.toString());
            stmt.setInt(3, taskId);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
    
    /**
     * Returns all projects, which are accessible by a group.
     *
     * @param group The requesting group.
     *
     * @return a Vector of projects.
     */
    public Vector getAllAccessibleProjectsByGroup(CmsGroup group) throws CmsException {
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (projects);
    }
    
    /**
     * Returns all projects, which are manageable by a group.
     *
     * @param group The requesting group.
     *
     * @return a Vector of projects.
     */
    public Vector getAllAccessibleProjectsByManagerGroup(CmsGroup group) throws CmsException {
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (projects);
    }
    
    /**
     * Returns all projects, which are owned by a user.
     *
     * @param user The requesting user.
     *
     * @return a Vector of projects.
     */
    public Vector getAllAccessibleProjectsByUser(CmsUser user) throws CmsException {
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (projects);
    }

    /**
     * Reads all export links.
     *
     * @return a Vector(of Strings) with the links.
     */
    public Vector getAllExportLinks() throws CmsException {
        Vector retValue = new Vector();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_GET_ALL_LINKS");
            res = stmt.executeQuery();
            while (res.next()) {
                retValue.add(res.getString(m_sqlManager.get("C_EXPORT_LINK")));
            }
            return retValue;
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "getAllExportLinks()", CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "getAllExportLinks()", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * Returns all projects, with the overgiven state.
     *
     * @param state The state of the projects to read.
     *
     * @return a Vector of projects.
     */
    public Vector getAllProjects(int state) throws CmsException {
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
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_ID")),
                        res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_NAME")),
                        res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_DESCRIPTION")),
                        res.getInt(m_sqlManager.get("C_PROJECTS_TASK_ID")),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_USER_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_GROUP_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_MANAGERGROUP_ID"))),
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_FLAGS")),
                        SqlHelper.getTimestamp(res, m_sqlManager.get("C_PROJECTS_PROJECT_CREATEDATE")),
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_TYPE"))));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, "getAllProjects(int)", CmsException.C_SQL_ERROR, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return (projects);
    }

    /**
    * Reads all export links that depend on the resource.
    * @param res. The resourceName() of the resources that has changed (or the String
    *              that describes a contentdefinition).
    * @return a Vector(of Strings) with the linkrequest names.
    */
    public Vector getDependingExportLinks(Vector resources) throws CmsException {
        Vector retValue = new Vector();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            Vector firstResult = new Vector();
            Vector secondResult = new Vector();
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_GET_ALL_DEPENDENCIES");
            res = stmt.executeQuery();
            while (res.next()) {
                firstResult.add(res.getString(m_sqlManager.get("C_EXPORT_DEPENDENCIES_RESOURCE")));
                secondResult.add(res.getString(m_sqlManager.get("C_EXPORT_LINK")));
            }
            // now we have all dependencies that are there. We can search now for
            // the ones we need
            for (int i = 0; i < resources.size(); i++) {
                for (int j = 0; j < firstResult.size(); j++) {
                    if (((String) firstResult.elementAt(j)).startsWith((String) resources.elementAt(i))) {
                        if (!retValue.contains(secondResult.elementAt(j))) {
                            retValue.add(secondResult.elementAt(j));
                        }
                    } else if (((String) resources.elementAt(i)).startsWith((String) firstResult.elementAt(j))) {
                        if (!retValue.contains(secondResult.elementAt(j))) {
                            // only direct subfolders count
                            int index = ((String) firstResult.elementAt(j)).length();
                            String test = ((String) resources.elementAt(i)).substring(index);
                            index = test.indexOf("/");
                            if (index == -1 || index + 1 == test.length()) {
                                retValue.add(secondResult.elementAt(j));
                            }
                        }
                    }
                }
            }
            return retValue;
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "getDependingExportlinks(Vector)", CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "getDependingExportLinks(Vector)", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * searches for broken links in the online project.
     *
     * @return A Vector with a CmsPageLinks object for each page containing broken links
     *          this CmsPageLinks object contains all links on the page withouth a valid target.
     */
    public Vector getOnlineBrokenLinks() throws CmsException {
        Vector result = new Vector();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_GET_ONLINE_BROKEN_LINKS");
            res = stmt.executeQuery();
            CmsUUID current = CmsUUID.getNullUUID();
            CmsPageLinks links = null;
            while (res.next()) {
                CmsUUID next = new CmsUUID(res.getString(m_sqlManager.get("C_LM_PAGE_ID")));
                if (!next.equals(current)) {
                    if (links != null) {
                        result.add(links);
                    }
                    links = new CmsPageLinks(next);
                    links.addLinkTarget(res.getString(m_sqlManager.get("C_LM_LINK_DEST")));
                    try {
                        links.setResourceName(((CmsFile) m_driverManager.getVfsDriver().readFileHeader(I_CmsConstants.C_PROJECT_ONLINE_ID, next)).getResourceName());
                    } catch (CmsException e) {
                        links.setResourceName("id=" + next + ". Sorry, can't read resource. " + e.getMessage());
                    }
                    links.setOnline(true);
                } else {
                    links.addLinkTarget(res.getString(m_sqlManager.get("C_LM_LINK_DEST")));
                }
                current = next;
            }
            if (links != null) {
                result.add(links);
            }
            return result;
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "getOnlineBrokenLinks()", CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "getOnlineBrokenLinks()", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Retrieves the online project from the database.
     *
     * @return com.opencms.file.CmsProject the  onlineproject for the given project.
     * @throws CmsException Throws CmsException if the resource is not found, or the database communication went wrong.
     */
    public CmsProject getOnlineProject() throws CmsException {
        return readProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
    }

    public void init(Configurations config, String dbPoolUrl, CmsDriverManager driverManager) throws CmsException {
        m_sqlManager = initQueries(dbPoolUrl);      
        m_driverManager = driverManager;  

        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Project driver init  : ok");
        }
    }

    /**
     * retrieve the correct instance of the queries holder.
     * This method should be overloaded if other query strings should be used.
     */
    public com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl) {
        return new com.opencms.db.generic.CmsSqlManager(dbPoolUrl);
    }

    /**
     * Publishes a specified project to the online project. <br>
     *
     * @param project The project to be published.
     * @param onlineProject The online project of the OpenCms.
     * @param report A report object to provide the loggin messages.
     * @return a vector of changed or deleted resources.
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public Vector publishProject(CmsUser user, int projectId, CmsProject onlineProject, boolean enableHistory, I_CmsReport report, Hashtable exportpoints) throws CmsException {
        CmsExportPointDriver discAccess = new CmsExportPointDriver(exportpoints);
        CmsFolder currentFolder = null;
        CmsFile currentFile = null;
        CmsFolder newFolder = null;
        CmsFile newFile = null;
        Vector offlineFolders;
        Vector offlineFiles;
        Vector deletedFolders = new Vector();
        Vector changedResources = new Vector();

        Map folderIdIndex = (Map) new HashMap();

        CmsProject currentProject = readProject(projectId);
        int versionId = 1;
        long publishDate = System.currentTimeMillis();

        if (enableHistory) {
            // get the version id for the backup
            versionId = m_driverManager.getBackupDriver().getBackupVersionId();
            // store the projectdata to the backuptables for history
            m_driverManager.getBackupDriver().backupProject(currentProject, versionId, publishDate, user);
        }

        // read all folders in offlineProject
        offlineFolders = m_driverManager.getVfsDriver().readFolders(projectId, false, true);
        for (int i = 0; i < offlineFolders.size(); i++) {
            currentFolder = ((CmsFolder) offlineFolders.elementAt(i));
            report.print(report.key("report.publishing"), I_CmsReport.C_FORMAT_NOTE);
            report.println(currentFolder.getAbsolutePath());
            // do not publish the folder if it is locked in another project

            if (currentFolder.isLocked()) {
                // in this case do nothing
                // C_STATE_DELETE
            } else if (currentFolder.getState() == I_CmsConstants.C_STATE_DELETED) {
                deletedFolders.addElement(currentFolder);
                changedResources.addElement(currentFolder.getResourceName());
                // I_CmsConstants.C_STATE_NEW
            } else if (currentFolder.getState() == I_CmsConstants.C_STATE_NEW) {
                changedResources.addElement(currentFolder.getResourceName());
                // export to filesystem if necessary
                String exportKey = checkExport(currentFolder.getAbsolutePath(), exportpoints);
                if (exportKey != null) {
                    discAccess.createFolder(currentFolder.getAbsolutePath(), exportKey);
                }

                // get parentId for onlineFolder either from folderIdIndex or from the database
                CmsUUID parentId = (CmsUUID) folderIdIndex.get(currentFolder.getParentId());
                if (parentId == null) {
                    CmsFolder currentOnlineParent = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFolder.getRootName() + currentFolder.getParent());
                    parentId = currentOnlineParent.getResourceId();
                    folderIdIndex.put(currentFolder.getParentId(), parentId);
                }

                // create the new folder and insert its id in the folderindex
                try {
                    newFolder = m_driverManager.getVfsDriver().createFolder(user, onlineProject, onlineProject, currentFolder, parentId, currentFolder.getResourceName());
                    newFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
                    m_driverManager.getVfsDriver().updateResourcestate(newFolder);
                } catch (CmsException e) {
                    // if the folder already exists in the onlineProject then update the onlineFolder
                    if (e.getType() == CmsException.C_FILE_EXISTS) {
                        CmsFolder onlineFolder = null;
                        try {
                            onlineFolder = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFolder.getResourceName());
                        } catch (CmsException exc) {
                            throw exc;
                        } // end of catch
                        PreparedStatement stmt = null;
                        Connection conn = null;
                        try {
                            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UPDATE_ONLINE");
                            // update the onlineFolder with data from offlineFolder
                            stmt.setInt(1, currentFolder.getType());
                            stmt.setInt(2, currentFolder.getFlags());
                            stmt.setString(3, currentFolder.getOwnerId().toString());
                            stmt.setString(4, currentFolder.getGroupId().toString());
                            stmt.setInt(5, onlineFolder.getProjectId());
                            stmt.setInt(6, currentFolder.getAccessFlags());
                            stmt.setInt(7, I_CmsConstants.C_STATE_UNCHANGED);
                            stmt.setString(8, currentFolder.isLockedBy().toString());
                            stmt.setInt(9, currentFolder.getLauncherType());
                            stmt.setString(10, currentFolder.getLauncherClassname());
                            stmt.setTimestamp(11, new Timestamp(currentFolder.getDateLastModified()));
                            stmt.setString(12, currentFolder.getResourceLastModifiedBy().toString());
                            stmt.setInt(13, 0);
                            stmt.setString(14, onlineFolder.getFileId().toString());
                            stmt.setString(15, onlineFolder.getResourceId().toString());
                            stmt.executeUpdate();
                            newFolder = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFolder.getResourceName());
                        } catch (SQLException exc) {
                            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
                        } finally {
                            m_sqlManager.closeAll(conn, stmt, null);
                        }
                    } else {
                        throw e;
                    }
                }

                folderIdIndex.put(currentFolder.getResourceId(), newFolder.getResourceId());
                // copy properties
                Map props = new HashMap();

                try {
                    props = m_driverManager.getVfsDriver().readProperties(projectId, currentFolder, currentFolder.getType());
                    m_driverManager.getVfsDriver().writeProperties(props, onlineProject.getId(), newFolder, newFolder.getType());
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, copy properties for " + newFolder.toString() + " Message= " + exc.getMessage());
                    }
                }
                if (enableHistory) {
                    // backup the offline resource
                    m_driverManager.getBackupDriver().backupResource(projectId, currentFolder, new byte[0], props, versionId, publishDate);
                }

                // set the state of current folder in the offline project to unchanged
                currentFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
                m_driverManager.getVfsDriver().updateResourcestate(currentFolder);
                // I_CmsConstants.C_STATE_CHANGED
            } else if (currentFolder.getState() == I_CmsConstants.C_STATE_CHANGED) {
                changedResources.addElement(currentFolder.getResourceName());
                // export to filesystem if necessary
                String exportKey = checkExport(currentFolder.getAbsolutePath(), exportpoints);
                if (exportKey != null) {
                    discAccess.createFolder(currentFolder.getAbsolutePath(), exportKey);
                }
                CmsFolder onlineFolder = null;
                try {
                    onlineFolder = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFolder.getResourceName());
                } catch (CmsException exc) {
                    // if folder does not exist create it
                    if (exc.getType() == CmsException.C_NOT_FOUND) {
                        // get parentId for onlineFolder either from folderIdIndex or from the database
                        CmsUUID parentId = (CmsUUID) folderIdIndex.get(currentFolder.getParentId());
                        if (parentId == null) {
                            CmsFolder currentOnlineParent = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFolder.getRootName() + currentFolder.getParent());
                            parentId = currentOnlineParent.getResourceId();
                            folderIdIndex.put(currentFolder.getParentId(), parentId);
                        }
                        // create the new folder
                        onlineFolder = m_driverManager.getVfsDriver().createFolder(user, onlineProject, onlineProject, currentFolder, parentId, currentFolder.getResourceName());
                        onlineFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
                        m_driverManager.getVfsDriver().updateResourcestate(onlineFolder);
                    } else {
                        throw exc;
                    }
                } // end of catch
                Connection conn = null;
                PreparedStatement stmt = null;
                try {
                    conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UPDATE_ONLINE");
                    // update the onlineFolder with data from offlineFolder
                    stmt.setInt(1, currentFolder.getType());
                    stmt.setInt(2, currentFolder.getFlags());
                    stmt.setString(3, currentFolder.getOwnerId().toString());
                    stmt.setString(4, currentFolder.getGroupId().toString());
                    stmt.setInt(5, onlineFolder.getProjectId());
                    stmt.setInt(6, currentFolder.getAccessFlags());
                    stmt.setInt(7, I_CmsConstants.C_STATE_UNCHANGED);
                    stmt.setString(8, currentFolder.isLockedBy().toString());
                    stmt.setInt(9, currentFolder.getLauncherType());
                    stmt.setString(10, currentFolder.getLauncherClassname());
                    stmt.setTimestamp(11, new Timestamp(currentFolder.getDateLastModified()));
                    stmt.setString(12, currentFolder.getResourceLastModifiedBy().toString());
                    stmt.setInt(13, 0);
                    stmt.setString(14, onlineFolder.getFileId().toString());
                    stmt.setString(15, onlineFolder.getResourceId().toString());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
                } finally {
                    m_sqlManager.closeAll(conn, stmt, null);
                }
                folderIdIndex.put(currentFolder.getResourceId(), onlineFolder.getResourceId());
                // copy properties
                Map props = new HashMap();
                try {
                    m_driverManager.getVfsDriver().deleteAllProperties(onlineProject.getId(), onlineFolder);
                    props = m_driverManager.getVfsDriver().readProperties(projectId, currentFolder, currentFolder.getType());
                    m_driverManager.getVfsDriver().writeProperties(props, onlineProject.getId(), onlineFolder, currentFolder.getType());
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, deleting properties for " + onlineFolder.toString() + " Message= " + exc.getMessage());
                    }
                }
                if (enableHistory) {
                    // backup the offline resource
                    m_driverManager.getBackupDriver().backupResource(projectId, currentFolder, new byte[0], props, versionId, publishDate);
                }
                // set the state of current folder in the offline project to unchanged
                currentFolder.setState(I_CmsConstants.C_STATE_UNCHANGED);
                m_driverManager.getVfsDriver().updateResourcestate(currentFolder);
            } // end of else if
        } // end of for(...

        // now read all FILES in offlineProject

        offlineFiles = m_driverManager.getVfsDriver().readFiles(projectId, false, true);

        for (int i = 0; i < offlineFiles.size(); i++) {
            currentFile = ((CmsFile) offlineFiles.elementAt(i));
            report.print(report.key("report.publishing"), I_CmsReport.C_FORMAT_NOTE);
            report.println(currentFile.getAbsolutePath());
            if (!currentFile.isLocked()) {
                // remove the temporary files for this resource
                removeTemporaryFile(currentFile);
            }
            // do not publish files that are locked in another project
            if (currentFile.isLocked()) {
                //in this case do nothing
            } else if (currentFile.getName().startsWith(I_CmsConstants.C_TEMP_PREFIX)) {
                m_driverManager.getVfsDriver().deleteAllProperties(projectId, currentFile);
                m_driverManager.getVfsDriver().removeFile(projectId, currentFile.getResourceName());
                // C_STATE_DELETE
            } else if (currentFile.getState() == I_CmsConstants.C_STATE_DELETED) {
                changedResources.addElement(currentFile.getResourceName());
                String exportKey = checkExport(currentFile.getAbsolutePath(), exportpoints);
                if (exportKey != null) {
                    try {
                        discAccess.removeResource(currentFile.getAbsolutePath(), exportKey);
                    } catch (Exception ex) {
                    }
                }
                CmsFile currentOnlineFile = m_driverManager.getVfsDriver().readFile(onlineProject.getId(), onlineProject.getId(), currentFile.getResourceName());
                if (enableHistory) {
                    // read the properties for backup
                    Map props = m_driverManager.getVfsDriver().readProperties(projectId, currentFile, currentFile.getType());
                    // backup the offline resource
                    m_driverManager.getBackupDriver().backupResource(projectId, currentFile, currentFile.getContents(), props, versionId, publishDate);
                }
                try {
                    m_driverManager.getVfsDriver().deleteAllProperties(onlineProject.getId(), currentOnlineFile);
                    m_driverManager.getVfsDriver().deleteAllProperties(projectId, currentFile);
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, deleting properties for " + currentOnlineFile.toString() + " Message= " + exc.getMessage());
                    }
                }
                try {
                    m_driverManager.getVfsDriver().deleteResource(currentOnlineFile);
                    m_driverManager.getVfsDriver().deleteResource(currentFile);
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, deleting resource for " + currentOnlineFile.toString() + " Message= " + exc.getMessage());
                    }
                }
                // I_CmsConstants.C_STATE_CHANGED
            } else if (currentFile.getState() == I_CmsConstants.C_STATE_CHANGED) {
                changedResources.addElement(currentFile.getResourceName());
                // export to filesystem if necessary
                String exportKey = checkExport(currentFile.getAbsolutePath(), exportpoints);
                if (exportKey != null) {
                    // Encoding project: Make sure files are written in the right encoding 
                    byte[] contents = currentFile.getContents();
                    String encoding = m_driverManager.getVfsDriver().readProperty(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, projectId, currentFile, currentFile.getType());
                    if (encoding != null) {
                        // Only files that have the encodig property set will be encoded,
                        // the other files will be ignored. So images etc. are not touched.                        
                        try {
                            contents = (new String(contents, encoding)).getBytes();
                        } catch (UnsupportedEncodingException uex) {
                            // contents will keep original value
                        }
                    }
                    discAccess.writeFile(currentFile.getAbsolutePath(), exportKey, contents);
                }
                CmsFile onlineFile = null;
                try {
                    onlineFile = m_driverManager.getVfsDriver().readFileHeader(onlineProject.getId(), currentFile.getResourceName(), false);
                } catch (CmsException exc) {
                    if (exc.getType() == CmsException.C_NOT_FOUND) {
                        // get parentId for onlineFolder either from folderIdIndex or from the database
                        CmsUUID parentId = (CmsUUID) folderIdIndex.get(currentFile.getParentId());
                        if (parentId == null) {
                            CmsFolder currentOnlineParent = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFile.getRootName() + currentFile.getParent());
                            parentId = currentOnlineParent.getResourceId();
                            folderIdIndex.put(currentFile.getParentId(), parentId);
                        }
                        // create a new File
                        currentFile.setState(I_CmsConstants.C_STATE_UNCHANGED);
                        onlineFile = m_driverManager.getVfsDriver().createFile(onlineProject, onlineProject, currentFile, user.getId(), parentId, currentFile.getResourceName());
                    }
                } // end of catch
                Connection conn = null;
                PreparedStatement stmt = null;
                try {
                    conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                    stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UPDATE_ONLINE");
                    // update the onlineFile with data from offlineFile
                    stmt.setInt(1, currentFile.getType());
                    stmt.setInt(2, currentFile.getFlags());
                    stmt.setString(3, currentFile.getOwnerId().toString());
                    stmt.setString(4, currentFile.getGroupId().toString());
                    stmt.setInt(5, onlineFile.getProjectId());
                    stmt.setInt(6, currentFile.getAccessFlags());
                    stmt.setInt(7, I_CmsConstants.C_STATE_UNCHANGED);
                    stmt.setString(8, currentFile.isLockedBy().toString());
                    stmt.setInt(9, currentFile.getLauncherType());
                    stmt.setString(10, currentFile.getLauncherClassname());
                    stmt.setTimestamp(11, new Timestamp(currentFile.getDateLastModified()));
                    stmt.setString(12, currentFile.getResourceLastModifiedBy().toString());
                    stmt.setInt(13, currentFile.getLength());
                    stmt.setString(14, onlineFile.getFileId().toString());
                    stmt.setString(15, onlineFile.getResourceId().toString());
                    stmt.executeUpdate();
                    stmt.close();
                    m_driverManager.getVfsDriver().writeFileContent(onlineFile.getFileId(), currentFile.getContents(), I_CmsConstants.C_PROJECT_ONLINE_ID, false);
                } catch (SQLException e) {
                    throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
                } finally {
                    m_sqlManager.closeAll(conn, stmt, null);
                }
                // copy properties
                Map props = new HashMap();
                try {
                    m_driverManager.getVfsDriver().deleteAllProperties(onlineProject.getId(), onlineFile);
                    props = m_driverManager.getVfsDriver().readProperties(projectId, currentFile, currentFile.getType());
                    m_driverManager.getVfsDriver().writeProperties(props, onlineProject.getId(), onlineFile, currentFile.getType());
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, deleting properties for " + onlineFile.toString() + " Message= " + exc.getMessage());
                    }
                }
                if (enableHistory) {
                    // backup the offline resource
                    m_driverManager.getBackupDriver().backupResource(projectId, currentFile, currentFile.getContents(), props, versionId, publishDate);
                }
                // set the file state to unchanged
                currentFile.setState(I_CmsConstants.C_STATE_UNCHANGED);
                m_driverManager.getVfsDriver().updateResourcestate(currentFile);
                // I_CmsConstants.C_STATE_NEW
            } else if (currentFile.getState() == I_CmsConstants.C_STATE_NEW) {
                changedResources.addElement(currentFile.getResourceName());
                // export to filesystem if necessary
                String exportKey = checkExport(currentFile.getAbsolutePath(), exportpoints);
                if (exportKey != null) {
                    // Encoding project: Make sure files are written in the right encoding 
                    byte[] contents = currentFile.getContents();
                    String encoding = m_driverManager.getVfsDriver().readProperty(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, projectId, currentFile, currentFile.getType());
                    if (encoding != null) {
                        // Only files that have the encodig property set will be encoded,
                        // the other files will be ignored. So images etc. are not touched.
                        try {
                            contents = (new String(contents, encoding)).getBytes();
                        } catch (UnsupportedEncodingException uex) {
                            // contents will keep original value
                        }
                    }
                    discAccess.writeFile(currentFile.getAbsolutePath(), exportKey, contents);
                }
                // get parentId for onlineFile either from folderIdIndex or from the database
                CmsUUID parentId = (CmsUUID) folderIdIndex.get(currentFile.getParentId());
                if (parentId == null) {
                    CmsFolder currentOnlineParent = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFile.getRootName() + currentFile.getParent());
                    parentId = currentOnlineParent.getResourceId();
                    folderIdIndex.put(currentFile.getParentId(), parentId);
                }
                // create the new file
                try {
                    newFile = m_driverManager.getVfsDriver().createFile(onlineProject, onlineProject, currentFile, user.getId(), parentId, currentFile.getResourceName());
                    newFile.setState(I_CmsConstants.C_STATE_UNCHANGED);
                    m_driverManager.getVfsDriver().updateResourcestate(newFile);
                } catch (CmsException e) {
                    if (e.getType() == CmsException.C_FILE_EXISTS) {
                        CmsFile onlineFile = null;
                        try {
                            onlineFile = m_driverManager.getVfsDriver().readFileHeader(onlineProject.getId(), currentFile.getResourceName(), false);
                        } catch (CmsException exc) {
                            throw exc;
                        } // end of catch
                        Connection conn = null;
                        PreparedStatement stmt = null;
                        try {
                            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
                            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_UPDATE_ONLINE");
                            // update the onlineFile with data from offlineFile
                            stmt.setInt(1, currentFile.getType());
                            stmt.setInt(2, currentFile.getFlags());
                            stmt.setString(3, currentFile.getOwnerId().toString());
                            stmt.setString(4, currentFile.getGroupId().toString());
                            stmt.setInt(5, onlineFile.getProjectId());
                            stmt.setInt(6, currentFile.getAccessFlags());
                            stmt.setInt(7, I_CmsConstants.C_STATE_UNCHANGED);
                            stmt.setString(8, currentFile.isLockedBy().toString());
                            stmt.setInt(9, currentFile.getLauncherType());
                            stmt.setString(10, currentFile.getLauncherClassname());
                            stmt.setTimestamp(11, new Timestamp(currentFile.getDateLastModified()));
                            stmt.setString(12, currentFile.getResourceLastModifiedBy().toString());
                            stmt.setInt(13, currentFile.getLength());
                            stmt.setString(14, onlineFile.getFileId().toString());
                            stmt.setString(15, onlineFile.getResourceId().toString());
                            stmt.executeUpdate();
                            m_driverManager.getVfsDriver().writeFileContent(onlineFile.getFileId(), currentFile.getContents(), I_CmsConstants.C_PROJECT_ONLINE_ID, false);
                            newFile = m_driverManager.getVfsDriver().readFile(onlineProject.getId(), onlineProject.getId(), currentFile.getResourceName());
                        } catch (SQLException exc) {
                            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
                        } finally {
                            m_sqlManager.closeAll(conn, stmt, null);
                        }
                    } else {
                        throw e;
                    }
                }
                // copy properties
                Map props = new HashMap();
                try {
                    props = m_driverManager.getVfsDriver().readProperties(projectId, currentFile, currentFile.getType());
                    m_driverManager.getVfsDriver().writeProperties(props, onlineProject.getId(), newFile, newFile.getType());
                } catch (CmsException exc) {
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, copy properties for " + newFile.toString() + " Message= " + exc.getMessage());
                    }
                }
                if (enableHistory) {
                    // backup the offline resource
                    m_driverManager.getBackupDriver().backupResource(projectId, currentFile, currentFile.getContents(), props, versionId, publishDate);
                }
                // set the file state to unchanged
                currentFile.setState(I_CmsConstants.C_STATE_UNCHANGED);
                m_driverManager.getVfsDriver().updateResourcestate(currentFile);
            }
        } // end of for(...
        // now delete the "deleted" folders
        for (int i = deletedFolders.size() - 1; i > -1; i--) {
            currentFolder = ((CmsFolder) deletedFolders.elementAt(i));
            report.print(report.key("report.deleting"), I_CmsReport.C_FORMAT_NOTE);
            report.println(currentFolder.getAbsolutePath());
            String exportKey = checkExport(currentFolder.getAbsolutePath(), exportpoints);
            if (exportKey != null) {
                discAccess.removeResource(currentFolder.getAbsolutePath(), exportKey);
            }
            if (enableHistory) {
                Map props = m_driverManager.getVfsDriver().readProperties(projectId, currentFolder, currentFolder.getType());
                // backup the offline resource
                m_driverManager.getBackupDriver().backupResource(projectId, currentFolder, new byte[0], props, versionId, publishDate);
            }
            CmsResource delOnlineFolder = m_driverManager.getVfsDriver().readFolder(onlineProject.getId(), currentFolder.getResourceName());
            try {
                m_driverManager.getVfsDriver().deleteAllProperties(onlineProject.getId(), delOnlineFolder);
                m_driverManager.getVfsDriver().deleteAllProperties(projectId, currentFolder);
            } catch (CmsException exc) {
                if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] error publishing, deleting properties for " + currentFolder.toString() + " Message= " + exc.getMessage());
                }
            }
            m_driverManager.getVfsDriver().removeFolderForPublish(onlineProject.getId(), currentFolder.getResourceName());
            m_driverManager.getVfsDriver().removeFolderForPublish(projectId, currentFolder.getResourceName());
        } // end of for
        return changedResources;
    }

    /**
     * select all projectResources from an given project
     *
     * @param project The project in which the resource is used.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public Vector readAllProjectResources(int projectId) throws CmsException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Vector projectResources = new Vector();
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTRESOURCES_READALL");
            // select all resources from the database
            stmt.setInt(1, projectId);
            res = stmt.executeQuery();
            while (res.next()) {
                projectResources.addElement(res.getString("RESOURCE_NAME"));
            }
            res.close();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
        return projectResources;
    }

    /**
     * Reads a exportrequest from the Cms.
     *
     * @param request The request to be read.
     * @return The exportrequest read from the Cms or null if it is not found.
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public CmsExportLink readExportLink(String request) throws CmsException {
        CmsExportLink link = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_LINK_READ");
            stmt.setString(1, request);
            res = stmt.executeQuery();

            // create new Cms exportlink object
            if (res.next()) {
                link = new CmsExportLink(res.getInt(m_sqlManager.get("C_EXPORT_ID")), res.getString(m_sqlManager.get("C_EXPORT_LINK")), SqlHelper.getTimestamp(res, m_sqlManager.get("C_EXPORT_DATE")).getTime(), null);

                // now the dependencies
                try {
                    res.close();
                    stmt.close();
                } catch (SQLException ex) {
                }
                stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_DEPENDENCIES_READ");
                stmt.setInt(1, link.getId());
                res = stmt.executeQuery();
                while (res.next()) {
                    link.addDependency(res.getString(m_sqlManager.get("C_EXPORT_DEPENDENCIES_RESOURCE")));
                }
            }
            return link;
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readExportLink(String)", CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "readExportLink(String)", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * Reads a exportrequest without the dependencies from the Cms.<p>
     *
     * @param request The request to be read.
     * @return The exportrequest read from the Cms.
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    public CmsExportLink readExportLinkHeader(String request) throws CmsException {
        CmsExportLink link = null;
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_LINK_READ");
            stmt.setString(1, request);
            res = stmt.executeQuery();

            // create new Cms exportlink object
            if (res.next()) {
                link = new CmsExportLink(res.getInt(m_sqlManager.get("C_EXPORT_ID")), res.getString(m_sqlManager.get("C_EXPORT_LINK")), SqlHelper.getTimestamp(res, m_sqlManager.get("C_EXPORT_DATE")).getTime(), null);

            }
            return link;
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readExportLinkHeader(String)", CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "readExportLinkHeader(String)", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * returns a Vector (Strings) with the link destinations of all links on the page with
     * the pageId.
     *
     * @param pageId The resourceId (offline) of the page whose liks should be read.
     */
    public Vector readLinkEntrys(CmsUUID pageId) throws CmsException {
        Vector result = new Vector();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_READ_ENTRYS");
            stmt.setString(1, pageId.toString());
            res = stmt.executeQuery();
            while (res.next()) {
                result.add(res.getString(m_sqlManager.get("C_LM_LINK_DEST")));
            }
            return result;
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readLinkEntrys(CmsUUID)", CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "readLinkEntrys(CmsUUID)", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * reads the online id of a offline file.
     * @param filename
     * @return the id or -1 if not found (should not happen).
     */
    private CmsUUID readOnlineId(String filename) throws CmsException {
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        CmsUUID resourceId = CmsUUID.getNullUUID();

        try {
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_READ_ONLINE_ID");
            // read file data from database
            stmt.setString(1, filename);
            res = stmt.executeQuery();
            // read the id
            if (res.next()) {
                resourceId = new CmsUUID(res.getString(m_sqlManager.get("C_RESOURCES_RESOURCE_ID")));
                while (res.next()) {
                    // do nothing only move through all rows because of mssql odbc driver
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readOnlineId(String)", CmsException.C_SQL_ERROR, e);
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, "readOnlineId(String)", CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return resourceId;
    }

    /**
     * returns a Vector (Strings) with the link destinations of all links on the page with
     * the pageId.
     *
     * @param pageId The resourceId (online) of the page whose liks should be read.
     */
    public Vector readOnlineLinkEntrys(CmsUUID pageId) throws CmsException {
        Vector result = new Vector();
        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(I_CmsConstants.C_PROJECT_ONLINE_ID);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_LM_READ_ENTRYS_ONLINE");
            stmt.setString(1, pageId.toString());
            res = stmt.executeQuery();
            while (res.next()) {
                result.add(res.getString(m_sqlManager.get("C_LM_LINK_DEST")));
            }
            return result;
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readOnlineLinkEntrys(CmsUUID)", CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, "readOnlineLinkEntrys(CmsUUID)", CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
    }

    /**
     * Reads a project by task-id.
     *
     * @param task The task to read the project for.
     * @throws CmsException Throws CmsException if something goes wrong.
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

            if (res.next())
                project = new CmsProject(res, m_sqlManager);
            else
                // project not found!
                throw new CmsException("[" + this.getClass().getName() + "] " + task, CmsException.C_NOT_FOUND);
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readProject(CmsTask)", CmsException.C_SQL_ERROR, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return project;
    }

    /**
     * Reads a project.
     *
     * @param id The id of the project.
     * @throws CmsException Throws CmsException if something goes wrong.
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
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_ID")),
                        res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_NAME")),
                        res.getString(m_sqlManager.get("C_PROJECTS_PROJECT_DESCRIPTION")),
                        res.getInt(m_sqlManager.get("C_PROJECTS_TASK_ID")),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_USER_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_GROUP_ID"))),
                        new CmsUUID(res.getString(m_sqlManager.get("C_PROJECTS_MANAGERGROUP_ID"))),
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_FLAGS")),
                        SqlHelper.getTimestamp(res, m_sqlManager.get("C_PROJECTS_PROJECT_CREATEDATE")),
                        res.getInt(m_sqlManager.get("C_PROJECTS_PROJECT_TYPE")));
            } else {
                // project not found!
                throw m_sqlManager.getCmsException(this, "project with ID " + id + " not found", CmsException.C_NOT_FOUND, null);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readProject(int)/1 ", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return project;
    }

    /**
     * Reads log entries for a project.
     *
     * @param project The projec for tasklog to read.
     * @return A Vector of new TaskLog objects
     * @throws CmsException Throws CmsException if something goes wrong.
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
        int task = I_CmsConstants.C_UNKNOWN_ID;
        CmsUUID user = CmsUUID.getNullUUID();
        int type = I_CmsConstants.C_UNKNOWN_ID;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKLOG_READ_PPROJECTLOGS");
            stmt.setInt(1, projectid);
            res = stmt.executeQuery();
            while (res.next()) {
                comment = res.getString(m_sqlManager.get("C_LOG_COMMENT"));
                id = res.getInt(m_sqlManager.get("C_LOG_ID"));
                starttime = SqlHelper.getTimestamp(res, m_sqlManager.get("C_LOG_STARTTIME"));
                task = res.getInt(m_sqlManager.get("C_LOG_TASK"));
                user = new CmsUUID(res.getString(m_sqlManager.get("C_LOG_USER")));
                type = res.getInt(m_sqlManager.get("C_LOG_TYPE"));

                tasklog = new CmsTaskLog(id, comment, task, user, starttime, type);
                logs.addElement(tasklog);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return logs;
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
    public Vector readProjectView(int currentProject, int project, String filter) throws CmsException {

        Vector resources = new Vector();
        CmsResource file;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        String addStatement = filter + " ORDER BY RESOURCE_NAME";

        try {
            conn = m_sqlManager.getConnection();
            //stmt = conn.prepareStatement(m_sqlManager.get("C_RESOURCES_PROJECTVIEW") + addStatement);
            stmt = m_sqlManager.getPreparedStatementForSql(conn, m_sqlManager.get("C_RESOURCES_PROJECTVIEW") + addStatement);

            stmt.setInt(1, project);
            res = stmt.executeQuery();

            // create new resource
            while (res.next()) {
                file = m_driverManager.getVfsDriver().createCmsResourceFromResultSet(res, project);
                resources.addElement(file);
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "readProjectView(int, int, String)", CmsException.C_SQL_ERROR, e);
        } catch (Exception ex) {
            throw m_sqlManager.getCmsException(this, "readProjectView(int,int, String)", CmsException.C_UNKNOWN_EXCEPTION, ex);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return resources;
    }

    /**
     * Reads a session from the database.
     *
     * @param sessionId, the id og the session to read.
     * @return the read session as Hashtable.
     * @throws thorws CmsException if something goes wrong.
     */
    public Hashtable readSession(String sessionId) throws CmsException {
        PreparedStatement stmt = null;
        ResultSet res = null;
        Hashtable sessionData = new Hashtable();
        Hashtable data = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SESSION_READ");
            stmt.setString(1, sessionId);
            stmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis() - C_SESSION_TIMEOUT));

            res = stmt.executeQuery();

            // create new Cms user object
            if (res.next()) {
                // read the additional infos.
                byte[] value = m_sqlManager.getBytes(res, "SESSION_DATA");
                // now deserialize the object
                ByteArrayInputStream bin = new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                data = (Hashtable) oin.readObject();
                try {
                    for (;;) {
                        Object key = oin.readObject();
                        Object sessionValue = oin.readObject();
                        sessionData.put(key, sessionValue);
                    }
                } catch (EOFException exc) {
                    // reached eof - stop reading all is done now.
                }
                data.put(I_CmsConstants.C_SESSION_DATA, sessionData);
            } else {
                deleteSessions();
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (Exception e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return data;
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
                value = m_sqlManager.getBytes(res, m_sqlManager.get("C_SYSTEMPROPERTY_VALUE"));
                // now deserialize the object
                ByteArrayInputStream bin = new ByteArrayInputStream(value);
                ObjectInputStream oin = new ObjectInputStream(bin);
                property = (Serializable) oin.readObject();
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } catch (ClassNotFoundException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_CLASSLOADER_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return property;
    }

    /**
     * Removes the temporary files of the given resource
     *
     * @param file The file of which the remporary files should be deleted
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    protected void removeTemporaryFile(CmsFile file) throws CmsException {
        PreparedStatement stmt = null;
        PreparedStatement statementCont = null;
        PreparedStatement statementProp = null;
        Connection conn = null;
        ResultSet res = null;

        String tempFilename = file.getRootName() + file.getPath() + I_CmsConstants.C_TEMP_PREFIX + file.getName() + "%";
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
                statementProp = m_sqlManager.getPreparedStatement(conn, "C_PROPERTIES_DELETEALL");
                statementProp.setInt(1, resourceId);
                statementProp.executeQuery();
                statementProp.close();
                // delete the file content
                statementCont = m_sqlManager.getPreparedStatement(conn, "C_FILE_DELETE");
                statementCont.setInt(1, fileId);
                statementCont.executeUpdate();
                statementCont.close();
            }
            res.close();
            stmt.close();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_RESOURCES_DELETETEMPFILES");
            stmt.setString(1, tempFilename);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } finally {
            // close all db-resources          
            if (statementProp != null) {
                try {
                    statementProp.close();
                } catch (SQLException exc) {
                    // nothing to do here
                }
            }
            if (statementCont != null) {
                try {
                    statementCont.close();
                } catch (SQLException exc) {
                    // nothing to do here
                }
            }
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Helper method to serialize the hashtable.
     * This method is used by updateSession() and createSession()
     */
    private byte[] serializeSession(Hashtable data) throws IOException {
        // serialize the hashtable
        byte[] value;
        Hashtable sessionData = (Hashtable) data.remove(I_CmsConstants.C_SESSION_DATA);
        StringBuffer notSerializable = new StringBuffer();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);

        // first write the user data
        oout.writeObject(data);
        if (sessionData != null) {
            Enumeration keys = sessionData.keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object sessionValue = sessionData.get(key);
                if (sessionValue instanceof Serializable) {
                    // this value is serializeable -> write it to the outputstream
                    oout.writeObject(key);
                    oout.writeObject(sessionValue);
                } else {
                    // this object is not serializeable -> remark for warning
                    notSerializable.append(key);
                    notSerializable.append("; ");
                }
            }
        }
        oout.close();
        value = bout.toByteArray();
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() && (notSerializable.length() > 0)) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INFO, "[CmsProjectDriver] warning, following entrys are not serializeable in the session: " + notSerializable.toString() + ".");
        }
        return value;
    }

    /** 
     * Sorts a vector of files or folders alphabetically.
     * This method uses an insertion sort algorithm.
     * NOT IN USE AT THIS TIME
     *
     * @param unsortedList Array of strings containing the list of files or folders.
     * @return Array of sorted strings.
     */
    protected Vector SortEntrys(Vector list) {
        int in, out;
        int nElem = list.size();
        CmsResource[] unsortedList = new CmsResource[list.size()];
        for (int i = 0; i < list.size(); i++) {
            unsortedList[i] = (CmsResource) list.elementAt(i);
        }
        for (out = 1; out < nElem; out++) {
            CmsResource temp = unsortedList[out];
            in = out;
            while (in > 0 && unsortedList[in - 1].getResourceName().compareTo(temp.getResourceName()) >= 0) {
                unsortedList[in] = unsortedList[in - 1];
                --in;
            }
            unsortedList[in] = temp;
        }
        Vector sortedList = new Vector();
        for (int i = 0; i < list.size(); i++) {
            sortedList.addElement(unsortedList[i]);
        }
        return sortedList;
    }

    /**
     * Unlocks all resources in this project.
     *
     * @param project The project to be unlocked.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void unlockProject(CmsProject project) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(project);
            stmt = m_sqlManager.getPreparedStatement(conn, project, "C_RESOURCES_UNLOCK");
            // create the statement
            stmt.setString(1, CmsUUID.getNullUUID().toString());
            stmt.setInt(2, project.getId());
            stmt.executeUpdate();
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * When a project is published this method aktualises the online link table.
     *
     * @param deleted A Vector (of CmsResources) with the deleted resources of the project.
     * @param changed A Vector (of CmsResources) with the changed resources of the project.
     * @param newRes A Vector (of CmsResources) with the newRes resources of the project.
     */
    public void updateOnlineProjectLinks(Vector deleted, Vector changed, Vector newRes, int pageType) throws CmsException {
        if (deleted != null) {
            for (int i = 0; i < deleted.size(); i++) {
                // delete the old values in the online table
                if (((CmsResource) deleted.elementAt(i)).getType() == pageType) {
                    CmsUUID id = readOnlineId(((CmsResource) deleted.elementAt(i)).getResourceName());
                    if (!id.isNullUUID()) {
                        deleteOnlineLinkEntrys(id);
                    }
                }
            }
        }
        if (changed != null) {
            for (int i = 0; i < changed.size(); i++) {
                // delete the old values and copy the new values from the project link table
                if (((CmsResource) changed.elementAt(i)).getType() == pageType) {
                    CmsUUID id = readOnlineId(((CmsResource) changed.elementAt(i)).getResourceName());
                    if (!id.isNullUUID()) {
                        deleteOnlineLinkEntrys(id);
                        createOnlineLinkEntrys(id, readLinkEntrys(((CmsResource) changed.elementAt(i)).getResourceId()));
                    }
                }
            }
        }
        if (newRes != null) {
            for (int i = 0; i < newRes.size(); i++) {
                // copy the values from the project link table
                if (((CmsResource) newRes.elementAt(i)).getType() == pageType) {
                    CmsUUID id = readOnlineId(((CmsResource) newRes.elementAt(i)).getResourceName());
                    if (!id.isNullUUID()) {
                        createOnlineLinkEntrys(id, readLinkEntrys(((CmsResource) newRes.elementAt(i)).getResourceId()));
                    }
                }
            }
        }
    }

    /**
     * This method updates a session in the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session.
     * @return data the sessionData.
     */
    public int updateSession(String sessionId, Hashtable data) throws CmsException {
        byte[] value = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        int retValue;

        try {
            value = serializeSession(data);
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_SESSION_UPDATE");
            // write data to database
            stmt.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            m_sqlManager.setBytes(stmt, 2, value);
            stmt.setString(3, sessionId);
            retValue = stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
        return retValue;
    }
    
    /**
     * Writes an exportlink to the Cms.
     *
     * @param link the cmsexportlink object to write.
     * @throws CmsException if something goes wrong.
     */
    public void writeExportLink(CmsExportLink link) throws CmsException {
        //first delete old entrys in the database
        deleteExportLink(link);
        int id = link.getId();
        if (id == 0) {
            id = m_sqlManager.nextId(m_sqlManager.get("C_TABLE_EXPORT_LINKS"));
            link.setLinkId(id);
        }
        // now write it
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_LINK_WRITE");
            // write the link table entry
            stmt.setInt(1, id);
            stmt.setString(2, link.getLink());
            stmt.setTimestamp(3, new Timestamp(link.getLastExportDate()));
            stmt.setBoolean(4, link.getProcessedState());
            stmt.executeUpdate();
            // now the dependencies
            try {
                stmt.close();
            } catch (SQLException ex) {
            }
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_DEPENDENCIES_WRITE");
            stmt.setInt(1, id);
            Vector deps = link.getDependencies();
            for (int i = 0; i < deps.size(); i++) {
                try {
                    stmt.setString(2, (String) deps.elementAt(i));
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    // this should be an Duplicate entry error and can be ignored
                    // todo: if it is something else we should coutionary delete the whole exportlink
                }
            }
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "writeExportLink(CmsExportLink)", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Sets one exportLink to procecced.
     *
     * @param link the cmsexportlink.
     *
     * @throws CmsException if something goes wrong.
     */
    public void writeExportLinkProcessedState(CmsExportLink link) throws CmsException {
        int linkId = link.getId();
        if (linkId == 0) {
            CmsExportLink dbLink = readExportLink(link.getLink());
            if (dbLink == null) {
                return;
            } else {
                linkId = dbLink.getId();
            }
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_EXPORT_LINK_SET_PROCESSED");
            // delete the link table entry
            stmt.setBoolean(1, link.getProcessedState());
            stmt.setInt(2, linkId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw m_sqlManager.getCmsException(this, "writeExportLinkProcessedState(CmsExportLink)", CmsException.C_SQL_ERROR, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Deletes a project from the cms.
     * Therefore it deletes all files, resources and properties.
     *
     * @param project the project to delete.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void writeProject(CmsProject project) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_WRITE");

            stmt.setString(1, project.getOwnerId().toString());
            stmt.setString(2, project.getGroupId().toString());
            stmt.setString(3, project.getManagerGroupId().toString());
            stmt.setInt(4, project.getFlags());
            // no publishing data
            stmt.setInt(7, project.getId());
            stmt.executeUpdate();
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc);
        } finally {
            // close all db-resources
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, e);
        } catch (IOException e) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SERIALIZATION, e);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return readSystemProperty(name);
    }

}
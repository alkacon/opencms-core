/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/mySql/Attic/CmsDbAccess.java,v $
* Date   : $Date: 2003/05/07 11:43:25 $
* Version: $Revision: 1.87 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file.mySql;


import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsBackupProject;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsTask;
import com.opencms.file.CmsTaskLog;
import com.opencms.file.CmsUser;
import com.opencms.file.I_CmsResourceBroker;
import com.opencms.util.Encoder;
import com.opencms.util.SqlHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Vector;

import source.org.apache.java.util.Configurations;



/**
 * This is the generic access module to load and store resources from and into
 * the database.
 *
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Hanjo Riege
 * @author Anders Fugmann
 * @version $Revision: 1.87 $ $Date: 2003/05/07 11:43:25 $ *
 */
public class CmsDbAccess extends com.opencms.file.genericSql.CmsDbAccess implements I_CmsConstants, I_CmsLogChannels {
    
    /**
     * Instanciates the access-module and sets up all required modules and connections.
     * @param config The OpenCms configuration.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsDbAccess(Configurations config, I_CmsResourceBroker theResourceBroker)
        throws CmsException {

        super(config,theResourceBroker);
    }

    /**
     * Deletes all properties for a project.
     *
     * @param project The project to delete.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProjectProperties(CmsProject project) throws CmsException {
        // get all resources of the project
        Vector resources = m_ResourceBroker.getVfsAccess().readResources(project);
        for (int i = 0; i < resources.size(); i++) {
            // delete the properties for each resource in project
            deleteAllProperties(project.getId(),(CmsResource) resources.elementAt(i));
        }
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
         PreparedStatement statement = null;
         Connection con = null;

         try {
             // create the statement
             con = DriverManager.getConnection(m_poolNameBackup);
             statement = con.prepareStatement(m_SqlQueries.get("C_PROJECTS_READLAST_BACKUP"));
             statement.setInt(1, 300);
             res = statement.executeQuery();
             while(res.next()) {
                 Vector resources = m_ResourceBroker.getVfsAccess().readBackupProjectResources(res.getInt("VERSION_ID"));
                 projects.addElement( new CmsBackupProject(res.getInt("VERSION_ID"),
                                                    res.getInt("PROJECT_ID"),
                                                    res.getString("PROJECT_NAME"),
                                                    SqlHelper.getTimestamp(res,"PROJECT_PUBLISHDATE"),
                                                    res.getInt("PROJECT_PUBLISHED_BY"),
                                                    res.getString("PROJECT_PUBLISHED_BY_NAME"),
                                                    res.getString("PROJECT_DESCRIPTION"),
                                                    res.getInt("TASK_ID"),
                                                    res.getInt("USER_ID"),
                                                    res.getString("USER_NAME"),
                                                    res.getInt("GROUP_ID"),
                                                    res.getString("GROUP_NAME"),
                                                    res.getInt("MANAGERGROUP_ID"),
                                                    res.getString("MANAGERGROUP_NAME"),
                                                    SqlHelper.getTimestamp(res,"PROJECT_CREATEDATE"),
                                                    res.getInt("PROJECT_TYPE"),
                                                    resources));
             }
         } catch( SQLException exc ) {
             throw new CmsException("[" + this.getClass().getName() + ".getAllBackupProjects()] " + exc.getMessage(),
                 CmsException.C_SQL_ERROR, exc);
         } finally {
            // close all db-resources
            if(res != null) {
                 try {
                     res.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
            }
            if(statement != null) {
                 try {
                     statement.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
            }
            if(con != null) {
                 try {
                     con.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
            }
         }
         return(projects);
     }
     
    /**
     * Destroys this access-module
     * @throws throws CmsException if something goes wrong.
     */
    public void destroy() throws CmsException {
        try {
            ((com.opencms.dbpool.CmsDriver) DriverManager.getDriver(m_poolName)).destroy();
        } catch(SQLException exc) {
            // destroy not possible - ignoring the exception
        }

        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[mySql.CmsDbAccess] Destroyed");
        }
    }

    /**
     * Private method to init all default-resources
     */
    protected void fillDefaults() throws CmsException {
        // set the groups
        CmsGroup guests = m_ResourceBroker.getUserAccess().createGroup(C_GROUP_GUEST, "the guest-group", C_FLAG_ENABLED, null);
        CmsGroup administrators = m_ResourceBroker.getUserAccess().createGroup(C_GROUP_ADMIN, "the admin-group", C_FLAG_ENABLED | C_FLAG_GROUP_PROJECTMANAGER, null);
        CmsGroup users = m_ResourceBroker.getUserAccess().createGroup(C_GROUP_USERS, "the users-group to access the workplace", C_FLAG_ENABLED | C_FLAG_GROUP_ROLE | C_FLAG_GROUP_PROJECTCOWORKER, C_GROUP_GUEST);
        CmsGroup projectleader = m_ResourceBroker.getUserAccess().createGroup(C_GROUP_PROJECTLEADER, "the projectmanager-group", C_FLAG_ENABLED | C_FLAG_GROUP_PROJECTMANAGER | C_FLAG_GROUP_PROJECTCOWORKER | C_FLAG_GROUP_ROLE, users.getName());

        // add the users
        CmsUser guest = m_ResourceBroker.getUserAccess().addUser(C_USER_GUEST, "", "the guest-user", "", "", "", 0, 0, C_FLAG_ENABLED, new Hashtable(), guests, "", "", C_USER_TYPE_SYSTEMUSER);
        CmsUser admin = m_ResourceBroker.getUserAccess().addUser(C_USER_ADMIN, "admin", "the admin-user", "", "", "", 0, 0, C_FLAG_ENABLED, new Hashtable(), administrators, "", "", C_USER_TYPE_SYSTEMUSER);
        m_ResourceBroker.getUserAccess().addUserToGroup(guest.getId(), guests.getId());
        m_ResourceBroker.getUserAccess().addUserToGroup(admin.getId(), administrators.getId());
        writeTaskType(1, 0, "../taskforms/adhoc.asp", "Ad-Hoc", "30308", 1, 1);
        // create the online project
        CmsTask task = createTask(0, 0, 1, // standart project type,
                    admin.getId(), admin.getId(), administrators.getId(), C_PROJECT_ONLINE, new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), C_TASK_PRIORITY_NORMAL);
        CmsProject online = createProject(admin, guests, projectleader, task, C_PROJECT_ONLINE, "the online-project", C_FLAG_ENABLED, C_PROJECT_TYPE_NORMAL);

        // create the root-folder for the online project
        int siteRootId = 0;
        CmsFolder rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, online, C_UNKNOWN_ID, C_UNKNOWN_ID, C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(online, rootFolder, false);
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, online, rootFolder.getResourceId(), C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(online, rootFolder, false);
        siteRootId = rootFolder.getResourceId();
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, online, siteRootId, C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOTNAME_VFS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(online, rootFolder, false);
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, online, siteRootId, C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOTNAME_COS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(online, rootFolder, false);
        // create the setup project
        task = createTask(0, 0, 1, admin.getId(), admin.getId(), administrators.getId(),
                                    "_setupProject", new java.sql.Timestamp(new java.util.Date().getTime()),
                                    new java.sql.Timestamp(new java.util.Date().getTime()),
                                    C_TASK_PRIORITY_NORMAL);

        CmsProject setup = createProject(admin, administrators, administrators, task, "_setupProject",
                                           "the project for setup", C_FLAG_ENABLED, C_PROJECT_TYPE_TEMPORARY);

        // create the root-folder for the offline project
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, setup, C_UNKNOWN_ID, C_UNKNOWN_ID, C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(setup, rootFolder, false);
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, setup, rootFolder.getResourceId(), C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(setup, rootFolder, false);
        siteRootId = rootFolder.getResourceId();
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, setup, siteRootId, C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOTNAME_VFS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(setup, rootFolder, false);
        rootFolder = m_ResourceBroker.getVfsAccess().createFolder(admin, setup, siteRootId, C_UNKNOWN_ID, C_DEFAULT_SITE+C_ROOTNAME_COS+C_ROOT, 0);
        rootFolder.setGroupId(users.getId());
        rootFolder.setState(C_STATE_UNCHANGED);
        m_ResourceBroker.getVfsAccess().writeFolder(setup, rootFolder, false);
    }

    /**
     * Finds an agent for a given role (group).
     * @param roleId The Id for the role (group).
     *
     * @return A vector with the tasks
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    protected int findAgent(int roleid)
        throws CmsException {
        return super.findAgent(roleid);
    }

/**
 * retrieve the correct instance of the queries holder.
 * This method should be overloaded if other query strings should be used.
 */
public com.opencms.file.genericSql.CmsQueries initQueries(Configurations config)
{
    m_SqlQueries = new  com.opencms.file.mySql.CmsQueries();
    m_SqlQueries.initJdbcPoolUrls(config); 
    
    com.opencms.file.mySql.CmsQueries queries = new com.opencms.file.mySql.CmsQueries();
    queries.initJdbcPoolUrls(config);
    
    return queries;
}


    /**
     * Creates a new task.<p>
     * 
     * MySQL 4 does not support the SQL from the generic driver, so that's
     * why we have that special implementation here. 
     * This was tested with MySQL 4.0.10. 
     * 
     * @param rootId id of the root task project
     * @param parentId id of the parent task
     * @param tasktype type of the task
     * @param ownerId id of the owner
     * @param agentId id of the agent
     * @param roleId id of the role
     * @param taskname name of the task
     * @param wakeuptime time when the task will be wake up
     * @param timeout time when the task times out
     * @param priority priority of the task
     *
     * @return the Task object of the generated task
     *
     * @throws CmsException if something goes wrong.
     */
    public CmsTask createTask(int rootId, int parentId, int tasktype,
                               int ownerId, int agentId,int  roleId, String taskname,
                               java.sql.Timestamp wakeuptime, java.sql.Timestamp timeout,
                               int priority)
    throws CmsException {
        // fetch new task id
        int newId = nextId(C_TABLE_TASK);        
        // create the task id entry in the DB                 
        PreparedStatement statement = null;
        Connection con = null;
        try {
            con = DriverManager.getConnection(m_poolName);
            statement = con.prepareStatement(m_SqlQueries.get("C_TASK_CREATE"));
            statement.setInt(1, newId);
            statement.executeUpdate();

        } catch( SQLException exc ) {
            throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
        } finally {
            if(statement != null) {
                 try {
                     statement.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
            }
            if(con != null) {
                 try {
                     con.close();
                 } catch(SQLException exc) {
                     // nothing to do here
                 }
            }
        }                
        // create the task object, note that this does not user the "task type" table
        // because the generic SQL does not work with MySQL 4 
        CmsTask task = new CmsTask(newId, taskname, C_TASK_STATE_STARTED, tasktype, rootId, parentId, ownerId, roleId, agentId, 
                        agentId, new java.sql.Timestamp(System.currentTimeMillis()), wakeuptime, timeout, null, 0, 
                        "30308", priority, 0, "../taskforms/adhoc.asp", 0, 1);       
        // write tast
        task = writeTask(task);
        return task;
    }
    
    /**
     * Reads a task from the Cms with
     * added escaping of Strings since MySQL dosen't support Unicode strings
     *
     * @param id the id of the task to read
     * @return a task object or null if the task is not found
     *
     * @throws CmsException if something goes wrong
     */
    public CmsTask readTask(int id) throws CmsException {
        CmsTask task = super.readTask(id);
        if (task != null) task.setName(unescape(task.getName()));
        return task;
    }

    /**
     * Reads all tasks of a user in a project with
     * added escaping of Strings since MySQL dosen't support Unicode strings.
     * 
     * @param project the Project in which the tasks are defined
     * @param agent the task agent
     * @param owner the task owner .
     * @param group the group who has to process the task
     * @param tasktype one of C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy selects filter how to order the tasks
     * @param sort select to sort ascending or descending ("ASC" or "DESC")
     * @return a vector with the tasks read
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readTasks(CmsProject project, CmsUser agent, CmsUser owner, CmsGroup role, int tasktype, String orderBy, String sort) 
    throws CmsException {
        Vector v = super.readTasks(project, agent, owner, role, tasktype, orderBy, sort);
        for (int i=0; i<v.size(); i++) {
            CmsTask task = (CmsTask)v.elementAt(i);
            task.setName(unescape(task.getName()));
            v.set(i, task);
        }    
        return v;          
    }

    /**
     * Writes a task from the Cms with
     * added escaping of Strings since MySQL dosen't support Unicode strings
     *
     * @param id the id of the task to write
     * @return written task object
     *
     * @throws CmsException if something goes wrong
     */
    public CmsTask writeTask(CmsTask task) throws CmsException {
        task.setName(escape(task.getName()));
        task = super.writeTask(task);
        task.setName(unescape(task.getName()));        
        return task;
    }

    /**
     * Writes new log for a task with
     * added escaping of comment as as MySQL doesn't support Unicode strings.
     *
     * @param taskid The id of the task.
     * @param user User who added the Log.
     * @param starttime Time when the log is created.
     * @param comment Description for the log.
     * @param type Type of the log. 0 = Sytem log, 1 = User Log
     *
     * @throws CmsException if something goes wrong
     */
    public void writeTaskLog(int taskId, int userid,
                             java.sql.Timestamp starttime, String comment, int type)
        throws CmsException {
        super.writeTaskLog(taskId, userid, starttime, escape(comment), type);
    }
        
    /**
     * Reads a log for a task with
     * added unescaping of comment as as MySQL doesn't support Unicode strings.
     *
     * @param id The id for the tasklog .
     * @return A new TaskLog object
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsTaskLog readTaskLog(int id)
        throws CmsException {
        CmsTaskLog log = super.readTaskLog(id);
        log.setComment(unescape(log.getComment()));
        return log;
    }    

    /**
     * Reads log entries for a task with
     * added unescaping of comment as as MySQL doesn't support Unicode strings.
     *
     * @param taskid The id of the task for the tasklog to read .
     * @return A Vector of new TaskLog objects
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readTaskLogs(int taskId) throws CmsException {
        Vector v = super.readTaskLogs(taskId);
        for (int i=0; i<v.size(); i++) {
            CmsTaskLog log = (CmsTaskLog)v.elementAt(i);
            log.setComment(unescape(log.getComment()));
            v.set(i, log);
        }    
        return v;    
    }
    
    /**
     * Reads log entries for a project with
     * added unescaping of comment as as MySQL doesn't support Unicode strings.
     *
     * @param project The projec for tasklog to read.
     * @return A Vector of new TaskLog objects
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readProjectLogs(int projectid) throws CmsException {
        Vector v = super.readProjectLogs(projectid);
        for (int i=0; i<v.size(); i++) {
            CmsTaskLog log = (CmsTaskLog)v.elementAt(i);
            log.setComment(unescape(log.getComment()));
            v.set(i, log);
        }
        return v;    
    }    
                                       
    
    private static Boolean m_escapeStrings = null;
    
    /**
     * Escapes a String to prevent issues with UTF-8 encoding, same style as
     * http uses for form data since MySQL doesn't support Unicode/UTF-8 strings.<p>
     * TODO: this method is both in the DbcAccess and VfsAccess!
     * 
     * @param value String to be escaped
     * @return the escaped String
     */
    private String escape(String value) {
        // no need to encode if OpenCms is not running in Unicode mode
        if (singleByteEncoding())
            return value;
        return Encoder.encode(value);
    }    
    
    /**
     * Returns <code>true</code> if Strings must be escaped before they are stored in the DB, 
     * this is required because MySQL does not support multi byte unicode strings.<p>
     * TODO: this method is both in the DbcAccess and VfsAccess!
     * 
     * @return boolean <code>true</code> if Strings must be escaped before they are stored in the DB
     */
    private boolean singleByteEncoding() {
        if (m_escapeStrings == null) {
            String encoding = A_OpenCms.getDefaultEncoding();
            m_escapeStrings = new Boolean("ISO-8859-1".equalsIgnoreCase(encoding) || "ISO-8859-15".equalsIgnoreCase(encoding) || "US-ASCII".equalsIgnoreCase(encoding) || "Cp1252".equalsIgnoreCase(encoding));
        }
        return m_escapeStrings.booleanValue();
    }

    /**
     * Unescapes a String to prevent issues with UTF-8 encoding, same style as
     * http uses for form data since MySQL doesn't support Unicode/UTF-8 strings.<p>
     * TODO: this method is both in the DbcAccess and VfsAccess!
     * 
     * @param value String to be unescaped
     * @return the unescaped String
     */
    private String unescape(String value) {
        // no need to encode if OpenCms is not running in Unicode mode
        if (singleByteEncoding())
            return value;
        return Encoder.decode(value);
    }    
        
}

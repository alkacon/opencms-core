/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/db/mysql/Attic/CmsProjectDriver.java,v $
 * Date   : $Date: 2003/05/21 14:32:53 $
 * Version: $Revision: 1.1 $
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
 
package com.opencms.db.mysql;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsBackupProject;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsTask;
import com.opencms.file.CmsTaskLog;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;
import com.opencms.util.Encoder;
import com.opencms.util.SqlHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

/**
 * This is the generic access module to load and store resources from and into
 * the database.
 *
 * @version $Revision: 1.1 $ $Date: 2003/05/21 14:32:53 $ *
 */
public class CmsProjectDriver extends com.opencms.db.generic.CmsProjectDriver implements I_CmsConstants, I_CmsLogChannels {

    /**
     * Deletes all properties for a project.
     *
     * @param project The project to delete.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    public void deleteProjectProperties(CmsProject project) throws CmsException {
        // get all resources of the project
        Vector resources = m_driverManager.getVfsAccess().readResources(project);
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
         PreparedStatement stmt = null;
         Connection conn = null;

         try {
             // create the statement
             conn = m_sqlManager.getConnectionForBackup();
             stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READLAST_BACKUP");
             stmt.setInt(1, 300);
             res = stmt.executeQuery();
             while(res.next()) {
                 Vector resources = m_driverManager.getVfsAccess().readBackupProjectResources(res.getInt("VERSION_ID"));
                 projects.addElement( new CmsBackupProject(res.getInt("VERSION_ID"),
                                                    res.getInt("PROJECT_ID"),
                                                    res.getString("PROJECT_NAME"),
                                                    SqlHelper.getTimestamp(res,"PROJECT_PUBLISHDATE"),
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
                                                    SqlHelper.getTimestamp(res,"PROJECT_CREATEDATE"),
                                                    res.getInt("PROJECT_TYPE"),
                                                    resources));
             }
         } catch( SQLException exc ) {
             throw m_sqlManager.getCmsException(this, "getAllBackupProjects()", CmsException.C_SQL_ERROR, exc);
         } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
         }
         return(projects);
     }
     
    /**
     * Destroys this access-module
     * @throws throws CmsException if something goes wrong.
     */
    public void destroy() throws CmsException {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[" + this.getClass().getName() + "] Destroyed");
        }
    }

    /**
     * retrieve the correct instance of the queries holder.
     * This method should be overloaded if other query strings should be used.
     */
    public com.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl) {
        return new com.opencms.db.mysql.CmsSqlManager(dbPoolUrl);
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
     * TODO: this method is still both in the DbcAccess and VfsAccess!
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
     * TODO: this method is still both in the DbcAccess and VfsAccess!
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
     * TODO: this method is still both in the DbcAccess and VfsAccess!
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

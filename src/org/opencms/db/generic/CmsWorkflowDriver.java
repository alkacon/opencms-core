/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/Attic/CmsWorkflowDriver.java,v $
 * Date   : $Date: 2003/08/07 18:47:27 $
 * Version: $Revision: 1.3 $
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
import org.opencms.db.I_CmsWorkflowDriver;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsTask;
import com.opencms.file.CmsTaskLog;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;
import com.opencms.util.SqlHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * Generic (ANSI-SQL) database server implementation of the workflow driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.3 $ $Date: 2003/08/07 18:47:27 $
 * @since 5.1
 */
public class CmsWorkflowDriver extends Object implements I_CmsWorkflowDriver {

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_TASK = "CMS_TASKS";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_TASKLOG = "CMS_TASKLOG";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_TASKPAR = "CMS_TASKPAR";

    /**
     * Table-key for max-id
     */
    protected static String C_TABLE_TASKTYPE = "CMS_TASKTYPE";

    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;
    protected CmsDriverManager m_driverManager;

    /**
     * Creates a new task.<p>
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
    public CmsTask createTask(int rootId, int parentId, int tasktype, CmsUUID ownerId, CmsUUID agentId, CmsUUID roleId, String taskname, java.sql.Timestamp wakeuptime, java.sql.Timestamp timeout, int priority) throws CmsException {

        // fetch new task id
        int newId = m_sqlManager.nextId(C_TABLE_TASK);
        // create the task id entry in the DB                 
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_CREATE");
            stmt.setInt(1, newId);
            stmt.executeUpdate();

        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
        // create the task object, note that this does not user the "task type" table
        // because the generic SQL does not work with MySQL 4 
        CmsTask task = new CmsTask(newId, taskname, I_CmsConstants.C_TASK_STATE_STARTED, tasktype, rootId, parentId, ownerId, roleId, agentId, agentId, new java.sql.Timestamp(System.currentTimeMillis()), wakeuptime, timeout, null, 0, "30308", priority, 0, "../taskforms/adhoc.asp", 0, 1);
        // write task
        task = writeTask(task);
        return task;
    }

    /**
     * Semi-constructor to create a CmsTask instance from a JDBC result set.
     */
    protected final CmsTask createTaskFromResultSet(ResultSet res) throws SQLException {
        int autofinish = res.getInt(m_sqlManager.get("C_TASK_AUTOFINISH"));
        java.sql.Timestamp endtime = SqlHelper.getTimestamp(res, m_sqlManager.get("C_TASK_ENDTIME"));
        int escalationtype = res.getInt(m_sqlManager.get("C_TASK_ESCALATIONTYPE"));
        int id = res.getInt(m_sqlManager.get("C_TASK_ID"));
        CmsUUID initiatoruser = new CmsUUID(res.getString(m_sqlManager.get("C_TASK_INITIATORUSER")));
        int milestone = res.getInt(m_sqlManager.get("C_TASK_MILESTONE"));
        String name = res.getString(m_sqlManager.get("C_TASK_NAME"));
        CmsUUID originaluser = new CmsUUID(res.getString(m_sqlManager.get("C_TASK_ORIGINALUSER")));
        CmsUUID agentuser = new CmsUUID(res.getString(m_sqlManager.get("C_TASK_AGENTUSER")));
        int parent = res.getInt(m_sqlManager.get("C_TASK_PARENT"));
        int percentage = res.getInt(m_sqlManager.get("C_TASK_PERCENTAGE"));
        String permission = res.getString(m_sqlManager.get("C_TASK_PERMISSION"));
        int priority = res.getInt(m_sqlManager.get("C_TASK_PRIORITY"));
        CmsUUID role = new CmsUUID(res.getString(m_sqlManager.get("C_TASK_ROLE")));
        int root = res.getInt(m_sqlManager.get("C_TASK_ROOT"));
        java.sql.Timestamp starttime = SqlHelper.getTimestamp(res, m_sqlManager.get("C_TASK_STARTTIME"));
        int state = res.getInt(m_sqlManager.get("C_TASK_STATE"));
        int tasktype = res.getInt(m_sqlManager.get("C_TASK_TASKTYPE"));
        java.sql.Timestamp timeout = SqlHelper.getTimestamp(res, m_sqlManager.get("C_TASK_TIMEOUT"));
        java.sql.Timestamp wakeuptime = SqlHelper.getTimestamp(res, m_sqlManager.get("C_TASK_WAKEUPTIME"));
        String htmllink = res.getString(m_sqlManager.get("C_TASK_HTMLLINK"));

        return new CmsTask(id, name, state, tasktype, root, parent, initiatoruser, role, agentuser, originaluser, starttime, wakeuptime, timeout, endtime, percentage, permission, priority, escalationtype, htmllink, milestone, autofinish);
    }

    /**
     * Finds an agent for a given role (group).
     * @param roleId The Id for the role (group).
     *
     * @return A vector with the tasks
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsUUID findAgent(CmsUUID roleId) throws CmsException {
        CmsUUID result = CmsUUID.getNullUUID();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_FIND_AGENT");
            stmt.setString(1, roleId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                result = new CmsUUID(res.getString(1));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return result;
    }
    /**
     * Get a parameter value for a task.
     *
     * @param task The task.
     * @param parname Name of the parameter.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public String getTaskPar(int taskId, String parname) throws CmsException {

        String result = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKPAR_GET");
            stmt.setInt(1, taskId);
            stmt.setString(2, parname);
            res = stmt.executeQuery();
            if (res.next()) {
                result = res.getString(m_sqlManager.get("C_PAR_VALUE"));
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return result;
    }

    /**
     * Get the template task id fo a given taskname.
     *
     * @param taskName Name of the Task
     *
     * @return id from the task template
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public int getTaskType(String taskName) throws CmsException {
        int result = 1;

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_GET_TASKTYPE");
            stmt.setString(1, taskName);
            res = stmt.executeQuery();
            if (res.next()) {
                result = res.getInt("id");
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return result;
    }
    
    protected String getTaskTypeConditon(boolean first, int tasktype) {

        String result = "";
        // handle the tasktype for the SQL String
        if (!first) {
            result = result + " AND ";
        }

        switch (tasktype) {
            case I_CmsConstants.C_TASKS_ALL :
                {
                    result = result + m_sqlManager.get("C_TASK_ROOT") + "<>0";
                    break;
                }
            case I_CmsConstants.C_TASKS_OPEN :
                {
                    result = result + m_sqlManager.get("C_TASK_STATE") + "=" + I_CmsConstants.C_TASK_STATE_STARTED;
                    break;
                }
            case I_CmsConstants.C_TASKS_ACTIVE :
                {
                    result = result + m_sqlManager.get("C_TASK_STATE") + "=" + I_CmsConstants.C_TASK_STATE_STARTED;
                    break;
                }
            case I_CmsConstants.C_TASKS_DONE :
                {
                    result = result + m_sqlManager.get("C_TASK_STATE") + "=" + I_CmsConstants.C_TASK_STATE_ENDED;
                    break;
                }
            case I_CmsConstants.C_TASKS_NEW :
                {
                    result = result + m_sqlManager.get("C_TASK_PERCENTAGE") + "='0' AND " + m_sqlManager.get("C_TASK_STATE") + "=" + I_CmsConstants.C_TASK_STATE_STARTED;
                    break;
                }
            default :
                {
                }
        }

        return result;
    }
    
    protected void finalize() throws Throwable {
        if (m_sqlManager!=null) {
            m_sqlManager.finalize();
        }
        
        m_sqlManager = null;      
        m_driverManager = null;        
    }
    
    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#destroy()
     */
    public void destroy() throws Throwable {
        finalize();
                
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[" + this.getClass().getName() + "] destroyed!");
        }
    }    

    public void init(Configurations config, String dbPoolUrl, CmsDriverManager driverManager) {
        m_sqlManager = this.initQueries(dbPoolUrl);
        m_driverManager = driverManager;

        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Workflow driver init : ok");
        }        
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#initQueries(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initQueries(String dbPoolUrl) {
        return new org.opencms.db.generic.CmsSqlManager(dbPoolUrl);
    }

    protected int insertTaskPar(int taskId, String parname, String parvalue) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;
        int newId = I_CmsConstants.C_UNKNOWN_ID;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKPAR_INSERT");
            newId = m_sqlManager.nextId(C_TABLE_TASKPAR);
            stmt.setInt(1, newId);
            stmt.setInt(2, taskId);
            stmt.setString(3, m_sqlManager.validateNull(parname));
            stmt.setString(4, m_sqlManager.validateNull(parvalue));
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
        return newId;
    }

    protected int insertTaskType(int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref) throws CmsException {
        PreparedStatement stmt = null;
        Connection conn = null;

        int newId = I_CmsConstants.C_UNKNOWN_ID;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKTYPE_INSERT");
            newId = m_sqlManager.nextId(C_TABLE_TASKPAR);
            stmt.setInt(1, autofinish);
            stmt.setInt(2, escalationtyperef);
            stmt.setString(3, htmllink);
            stmt.setInt(4, newId);
            stmt.setString(5, name);
            stmt.setString(6, permission);
            stmt.setInt(7, priorityref);
            stmt.setInt(8, roleref);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }
        return newId;
    }

    /**
     * Reads a task from the Cms.
     *
     * @param id The id of the task to read.
     *
     * @return a task object or null if the task is not found.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsTask readTask(int id) throws CmsException {
        ResultSet res = null;
        CmsTask task = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_READ");
            stmt.setInt(1, id);
            res = stmt.executeQuery();

            if (res.next()) {
                task = createTaskFromResultSet(res);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return task;
    }

    /**
     * Reads a log for a task.
     *
     * @param id The id for the tasklog .
     * @return A new TaskLog object
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsTaskLog readTaskLog(int id) throws CmsException {
        ResultSet res = null;
        CmsTaskLog tasklog = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKLOG_READ");
            stmt.setInt(1, id);
            res = stmt.executeQuery();
            if (res.next()) {
                String comment = res.getString(m_sqlManager.get("C_LOG_COMMENT"));
                id = res.getInt(m_sqlManager.get("C_LOG_ID"));
                java.sql.Timestamp starttime = SqlHelper.getTimestamp(res, m_sqlManager.get("C_LOG_STARTTIME"));
                int task = res.getInt(m_sqlManager.get("C_LOG_TASK"));
                CmsUUID user = new CmsUUID(res.getString(m_sqlManager.get("C_LOG_USER")));
                int type = res.getInt(m_sqlManager.get("C_LOG_TYPE"));

                tasklog = new CmsTaskLog(id, comment, task, user, starttime, type);
            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }

        return tasklog;
    }

    /**
     * Reads log entries for a task.
     *
     * @param taskid The id of the task for the tasklog to read .
     * @return A Vector of new TaskLog objects
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readTaskLogs(int taskId) throws CmsException {
        Connection conn = null;
        ResultSet res = null;
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
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKLOG_READ_LOGS");
            stmt.setInt(1, taskId);
            res = stmt.executeQuery();
            while (res.next()) {
                comment = res.getString(m_sqlManager.get("C_TASKLOG_COMMENT"));
                id = res.getInt(m_sqlManager.get("C_TASKLOG_ID"));
                starttime = SqlHelper.getTimestamp(res, m_sqlManager.get("C_TASKLOG_STARTTIME"));
                task = res.getInt(m_sqlManager.get("C_TASKLOG_TASK"));
                user = new CmsUUID(res.getString(m_sqlManager.get("C_TASKLOG_USER")));
                type = res.getInt(m_sqlManager.get("C_TASKLOG_TYPE"));
                tasklog = new CmsTaskLog(id, comment, task, user, starttime, type);
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
     * Reads all tasks of a user in a project.
     * @param project The Project in which the tasks are defined.
     * @param agent The task agent
     * @param owner The task owner .
     * @param group The group who has to process the task.
     * @param tasktype C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
     * @param orderBy Chooses, how to order the tasks.
     * @param sort Sort Ascending or Descending (ASC or DESC)
     *
     * @return A vector with the tasks
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public Vector readTasks(CmsProject project, CmsUser agent, CmsUser owner, CmsGroup role, int tasktype, String orderBy, String sort) throws CmsException {
        boolean first = true;
        Vector tasks = new Vector(); // vector for the return result
        CmsTask task = null; // tmp task for adding to vector
        ResultSet recset = null;
        Connection conn = null;

        // create the sql string depending on parameters
        // handle the project for the SQL String
        String sqlstr = "SELECT * FROM " + m_sqlManager.get("C_TABLENAME_TASK") + " WHERE ";
        if (project != null) {
            sqlstr = sqlstr + m_sqlManager.get("C_TASK_ROOT") + "=" + project.getTaskId();
            first = false;
        } else {
            sqlstr = sqlstr + m_sqlManager.get("C_TASK_ROOT") + "<> 0 AND " + m_sqlManager.get("C_TASK_PARENT") + "<> 0";
            first = false;
        }

        // handle the agent for the SQL String
        if (agent != null) {
            if (!first) {
                sqlstr = sqlstr + " AND ";
            }
            sqlstr = sqlstr + m_sqlManager.get("C_TASK_AGENTUSER") + "='" + agent.getId() + "'";
            first = false;
        }

        // handle the owner for the SQL String
        if (owner != null) {
            if (!first) {
                sqlstr = sqlstr + " AND ";
            }
            sqlstr = sqlstr + m_sqlManager.get("C_TASK_INITIATORUSER") + "='" + owner.getId() + "'";
            first = false;
        }

        // handle the role for the SQL String
        if (role != null) {
            if (!first) {
                sqlstr = sqlstr + " AND ";
            }
            sqlstr = sqlstr + m_sqlManager.get("C_TASK_ROLE") + "='" + role.getId() + "'";
            first = false;
        }

        sqlstr = sqlstr + getTaskTypeConditon(first, tasktype);

        // handel the order and sort parameter for the SQL String
        if (orderBy != null) {
            if (!orderBy.equals("")) {
                sqlstr = sqlstr + " ORDER BY " + orderBy;
                if (orderBy != null) {
                    if (!orderBy.equals("")) {
                        sqlstr = sqlstr + " " + sort;
                    }
                }
            }
        }

        Statement stmt = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = conn.createStatement();
            recset = stmt.executeQuery(sqlstr);

            // if resultset exists - return vector of tasks
            while (recset.next()) {
                task = createTaskFromResultSet(recset);
                tasks.addElement(task);
            }

        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } catch (Exception exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_UNKNOWN_EXCEPTION, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, null);
        }

        return tasks;
    }

    /**
     * Set a Parameter for a task.
     *
     * @param task The task.
     * @param parname Name of the parameter.
     * @param parvalue Value if the parameter.
     *
     * @return The id of the inserted parameter or 0 if the parameter exists for this task.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public int setTaskPar(int taskId, String parname, String parvalue) throws CmsException {

        ResultSet res = null;
        int result = 0;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKPAR_TEST");
            // test if the parameter already exists for this task
            stmt.setInt(1, taskId);
            stmt.setString(2, parname);
            res = stmt.executeQuery();

            if (res.next()) {
                //Parameter exisits, so make an update
                updateTaskPar(res.getInt(m_sqlManager.get("C_PAR_ID")), parvalue);
            } else {
                //Parameter is not exisiting, so make an insert
                result = insertTaskPar(taskId, parname, parvalue);

            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return result;
    }

    protected void updateTaskPar(int parid, String parvalue) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKPAR_UPDATE");
            stmt.setString(1, parvalue);
            stmt.setInt(2, parid);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }
    protected void updateTaskType(int taskId, int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKTYPE_UPDATE");
            stmt.setInt(1, autofinish);
            stmt.setInt(2, escalationtyperef);
            stmt.setString(3, htmllink);
            stmt.setString(4, name);
            stmt.setString(5, permission);
            stmt.setInt(6, priorityref);
            stmt.setInt(7, roleref);
            stmt.setInt(8, taskId);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    public void writeSystemTaskLog(int taskid, String comment) throws CmsException {
        this.writeTaskLog(taskid, CmsUUID.getNullUUID(), new java.sql.Timestamp(System.currentTimeMillis()), comment, I_CmsConstants.C_TASKLOG_USER);
    }

    /**
     * Updates a task.
     *
     * @param task The task that will be written.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public CmsTask writeTask(CmsTask task) throws CmsException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_UPDATE");
            stmt.setString(1, task.getName());
            stmt.setInt(2, task.getState());
            stmt.setInt(3, task.getTaskType());
            stmt.setInt(4, task.getRoot());
            stmt.setInt(5, task.getParent());
            stmt.setString(6, task.getInitiatorUser().toString());
            stmt.setString(7, task.getRole().toString());
            stmt.setString(8, task.getAgentUser().toString());
            stmt.setString(9, task.getOriginalUser().toString());
            stmt.setTimestamp(10, task.getStartTime());
            stmt.setTimestamp(11, task.getWakeupTime());
            stmt.setTimestamp(12, task.getTimeOut());
            stmt.setTimestamp(13, task.getEndTime());
            stmt.setInt(14, task.getPercentage());
            stmt.setString(15, task.getPermission());
            stmt.setInt(16, task.getPriority());
            stmt.setInt(17, task.getEscalationType());
            stmt.setString(18, task.getHtmlLink());
            stmt.setInt(19, task.getMilestone());
            stmt.setInt(20, task.getAutoFinish());
            stmt.setInt(21, task.getId());
            stmt.executeUpdate();

        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
        return (readTask(task.getId()));
    }

    /**
     * Writes new log for a task.
     *
     * @param taskid The id of the task.
     * @param user User who added the Log.
     * @param starttime Time when the log is created.
     * @param comment Description for the log.
     * @param type Type of the log. 0 = Sytem log, 1 = User Log
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public void writeTaskLog(int taskId, CmsUUID userId, java.sql.Timestamp starttime, String comment, int type) throws CmsException {

        int newId = I_CmsConstants.C_UNKNOWN_ID;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKLOG_WRITE");
            newId = m_sqlManager.nextId(C_TABLE_TASKLOG);
            stmt.setInt(1, newId);
            stmt.setInt(2, taskId);
            if (!userId.isNullUUID()) {
                stmt.setString(3, userId.toString());
            } else {
                // no user is specified so set to system user is only valid for system task log
                // TODO: this is a workaround. not sure if this is correct
                stmt.setString(3, m_driverManager.getUserDriver().readUser(A_OpenCms.getDefaultUsers().getUserGuest(), I_CmsConstants.C_USER_TYPE_SYSTEMUSER).getId().toString());
            }
            stmt.setTimestamp(4, starttime);
            stmt.setString(5, m_sqlManager.validateNull(comment));
            stmt.setInt(6, type);

            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * Creates a new tasktype set in the database.
     * @return The id of the inserted parameter or 0 if the parameter exists for this task.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    public int writeTaskType(int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref) throws CmsException {
        ResultSet res = null;
        int result = 0;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection();
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_GET_TASKTYPE");
            // test if the parameter already exists for this task
            stmt.setString(1, name);
            res = stmt.executeQuery();

            if (res.next()) {
                //Parameter exists, so make an update
                updateTaskType(res.getInt(m_sqlManager.get("C_PAR_ID")), autofinish, escalationtyperef, htmllink, name, permission, priorityref, roleref);

            } else {
                //Parameter is not existing, so make an insert
                result = insertTaskType(autofinish, escalationtyperef, htmllink, name, permission, priorityref, roleref);

            }
        } catch (SQLException exc) {
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(conn, stmt, res);
        }
        return result;
    }

}

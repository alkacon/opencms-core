/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/Attic/CmsWorkflowDriver.java,v $
 * Date   : $Date: 2003/09/17 08:31:28 $
 * Version: $Revision: 1.20 $
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
import org.opencms.db.CmsDbUtil;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsWorkflowDriver;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import source.org.apache.java.util.Configurations;

/**
 * Generic (ANSI-SQL) database server implementation of the workflow driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.20 $ $Date: 2003/09/17 08:31:28 $
 * @since 5.1
 */
public class CmsWorkflowDriver extends Object implements I_CmsDriver, I_CmsWorkflowDriver {

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
    protected CmsDriverManager m_driverManager;

    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#createTask(int, int, int, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, java.lang.String, java.sql.Timestamp, java.sql.Timestamp, int)
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
     * @see org.opencms.db.I_CmsWorkflowDriver#destroy()
     */
    public void destroy() throws Throwable {
        finalize();

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info("[" + this.getClass().getName() + "] destroyed!");
        }
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#endTask(int)
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
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
     * @see org.opencms.db.I_CmsWorkflowDriver#forwardTask(int, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
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
            throw m_sqlManager.getCmsException(this, null, CmsException.C_SQL_ERROR, exc, false);
        } finally {
            m_sqlManager.closeAll(conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(source.org.apache.java.util.Configurations, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(Configurations config, List successiveDrivers, CmsDriverManager driverManager) {
        String poolUrl = config.getString("db.workflow.pool");

        m_sqlManager = this.initQueries();
        m_sqlManager.setOfflinePoolUrl(poolUrl);
        m_sqlManager.setOnlinePoolUrl(poolUrl);
        m_sqlManager.setBackupPoolUrl(poolUrl);

        m_driverManager = driverManager;

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Assigned pool        : " + poolUrl);
        }

        if (successiveDrivers != null && !successiveDrivers.isEmpty()) {
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(this.getClass().toString() + " does not support successive drivers.");
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#initQueries(java.lang.String)
     */
    public org.opencms.db.generic.CmsSqlManager initQueries() {
        return new org.opencms.db.generic.CmsSqlManager();
    }

    /**
     * Semi-constructor to create a CmsTask instance from a JDBC result set.
     * 
     * @param res the result set from the query
     * @return the CmsTash created from the data
     * @throws SQLException if something goes wrong
     */
    protected final CmsTask internalCreateTask(ResultSet res) throws SQLException {
        int autofinish = res.getInt(m_sqlManager.get("C_TASK_AUTOFINISH"));
        java.sql.Timestamp endtime = CmsDbUtil.getTimestamp(res, m_sqlManager.get("C_TASK_ENDTIME"));
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
        java.sql.Timestamp starttime = CmsDbUtil.getTimestamp(res, m_sqlManager.get("C_TASK_STARTTIME"));
        int state = res.getInt(m_sqlManager.get("C_TASK_STATE"));
        int tasktype = res.getInt(m_sqlManager.get("C_TASK_TASKTYPE"));
        java.sql.Timestamp timeout = CmsDbUtil.getTimestamp(res, m_sqlManager.get("C_TASK_TIMEOUT"));
        java.sql.Timestamp wakeuptime = CmsDbUtil.getTimestamp(res, m_sqlManager.get("C_TASK_WAKEUPTIME"));
        String htmllink = res.getString(m_sqlManager.get("C_TASK_HTMLLINK"));

        return new CmsTask(id, name, state, tasktype, root, parent, initiatoruser, role, agentuser, originaluser, starttime, wakeuptime, timeout, endtime, percentage, permission, priority, escalationtype, htmllink, milestone, autofinish);
    }

    /**
     * Constructs a sql condition for the given task type.<p>
     * 
     * @param first flag to indicate the first condition
     * @param tasktype the type to query
     * @return the sql condition
     */
    protected String internalReadTaskTypeCondition(boolean first, int tasktype) {

        String result = "";
        // handle the tasktype for the SQL String
        if (!first) {
            result = result + " AND ";
        }

        switch (tasktype) {
            case I_CmsConstants.C_TASKS_ALL :
                result = result + m_sqlManager.get("C_TASK_ROOT") + "<>0";
                break;

            case I_CmsConstants.C_TASKS_OPEN :
                result = result + m_sqlManager.get("C_TASK_STATE") + "=" + I_CmsConstants.C_TASK_STATE_STARTED;
                break;

            case I_CmsConstants.C_TASKS_ACTIVE :
                result = result + m_sqlManager.get("C_TASK_STATE") + "=" + I_CmsConstants.C_TASK_STATE_STARTED;
                break;

            case I_CmsConstants.C_TASKS_DONE :
                result = result + m_sqlManager.get("C_TASK_STATE") + "=" + I_CmsConstants.C_TASK_STATE_ENDED;
                break;

            case I_CmsConstants.C_TASKS_NEW :
                result = result + m_sqlManager.get("C_TASK_PERCENTAGE") + "='0' AND " + m_sqlManager.get("C_TASK_STATE") + "=" + I_CmsConstants.C_TASK_STATE_STARTED;
                break;

            default :
                }

        return result;
    }

    /**
     * Updates a task parameter.<p>
     * 
     * @param parid the id of the parameter
     * @param parvalue the value of the parameter 
     * @throws CmsException if something goes wrong
     */
    protected void internalWriteTaskParameter(int parid, String parvalue) throws CmsException {

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

    /**
     * Adds a task parameter to a task.<p>
     * @param taskId the id of the task
     * @param parname the name of the parameter
     * @param parvalue the value of the parameter
     * @return the id of the new parameter
     * @throws CmsException if something goes wrong
     */
    protected int internalWriteTaskParameter(int taskId, String parname, String parvalue) throws CmsException {
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

    /**
     * Updates a task.<p>
     * 
     * @param taskId the id of the task
     * @param autofinish tbd
     * @param escalationtyperef tbd
     * @param htmllink tbd
     * @param name tbd
     * @param permission tbd
     * @param priorityref tbd
     * @param roleref tbd
     * @throws CmsException if something goes wrong
     */
    protected void internalWriteTaskType(int taskId, int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref) throws CmsException {

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

    /**
     * Inserts a new task.<p>
     * 
     * @param autofinish tbd
     * @param escalationtyperef tbd
     * @param htmllink tbd
     * @param name tbd
     * @param permission tbd
     * @param priorityref tbd
     * @param roleref tbd
     * @return tbd
     * @throws CmsException tbd
     */
    protected int internalWriteTaskType(int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref) throws CmsException {
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
     * @see org.opencms.db.I_CmsWorkflowDriver#readAgent(org.opencms.util.CmsUUID)
     */
    public CmsUUID readAgent(CmsUUID roleId) throws CmsException {
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
     * @see org.opencms.db.I_CmsWorkflowDriver#readTask(int)
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
                task = internalCreateTask(res);
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
     * @see org.opencms.db.I_CmsWorkflowDriver#readTaskLog(int)
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
                java.sql.Timestamp starttime = CmsDbUtil.getTimestamp(res, m_sqlManager.get("C_LOG_STARTTIME"));
                CmsUUID user = new CmsUUID(res.getString(m_sqlManager.get("C_LOG_USER")));
                int type = res.getInt(m_sqlManager.get("C_LOG_TYPE"));

                tasklog = new CmsTaskLog(id, comment, user, starttime, type);
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
     * @see org.opencms.db.I_CmsWorkflowDriver#readTaskLogs(int)
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
                starttime = CmsDbUtil.getTimestamp(res, m_sqlManager.get("C_TASKLOG_STARTTIME"));
                user = new CmsUUID(res.getString(m_sqlManager.get("C_TASKLOG_USER")));
                type = res.getInt(m_sqlManager.get("C_TASKLOG_TYPE"));
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
     * @see org.opencms.db.I_CmsWorkflowDriver#readTaskParameter(int, java.lang.String)
     */
    public String readTaskParameter(int taskId, String parname) throws CmsException {

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
     * @see org.opencms.db.I_CmsWorkflowDriver#readTasks(com.opencms.file.CmsProject, com.opencms.file.CmsUser, com.opencms.file.CmsUser, com.opencms.file.CmsGroup, int, java.lang.String, java.lang.String)
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

        sqlstr = sqlstr + internalReadTaskTypeCondition(first, tasktype);

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
                task = internalCreateTask(recset);
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
     * @see org.opencms.db.I_CmsWorkflowDriver#readTaskType(java.lang.String)
     */
    public int readTaskType(String taskName) throws CmsException {
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

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#writeSystemTaskLog(int, java.lang.String)
     */
    public void writeSystemTaskLog(int taskid, String comment) throws CmsException {
        this.writeTaskLog(taskid, CmsUUID.getNullUUID(), new java.sql.Timestamp(System.currentTimeMillis()), comment, I_CmsConstants.C_TASKLOG_USER);
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#writeTask(org.opencms.workflow.CmsTask)
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
     * @see org.opencms.db.I_CmsWorkflowDriver#writeTaskLog(int, org.opencms.util.CmsUUID, java.sql.Timestamp, java.lang.String, int)
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
                stmt.setString(3, m_driverManager.readUser(OpenCms.getDefaultUsers().getUserGuest(), I_CmsConstants.C_USER_TYPE_SYSTEMUSER).getId().toString());
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
     * @see org.opencms.db.I_CmsWorkflowDriver#writeTaskParameter(int, java.lang.String, java.lang.String)
     */
    public int writeTaskParameter(int taskId, String parname, String parvalue) throws CmsException {

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
                internalWriteTaskParameter(res.getInt(m_sqlManager.get("C_PAR_ID")), parvalue);
            } else {
                //Parameter is not exisiting, so make an insert
                result = internalWriteTaskParameter(taskId, parname, parvalue);

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
     * @see org.opencms.db.I_CmsWorkflowDriver#writeTaskType(int, int, java.lang.String, java.lang.String, java.lang.String, int, int)
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
                internalWriteTaskType(res.getInt(m_sqlManager.get("C_PAR_ID")), autofinish, escalationtyperef, htmllink, name, permission, priorityref, roleref);

            } else {
                //Parameter is not existing, so make an insert
                result = internalWriteTaskType(autofinish, escalationtyperef, htmllink, name, permission, priorityref, roleref);

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

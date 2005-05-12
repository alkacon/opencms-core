/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/generic/Attic/CmsWorkflowDriver.java,v $
 * Date   : $Date: 2005/05/12 13:15:29 $
 * Version: $Revision: 1.44 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.db.CmsDataAccessException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbUtil;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsObjectNotFoundException;
import org.opencms.db.CmsSqlException;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsWorkflowDriver;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Generic (ANSI-SQL) database server implementation of the workflow driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.44 $ $Date: 2005/05/12 13:15:29 $
 * @since 5.1
 */
public class CmsWorkflowDriver implements I_CmsDriver, I_CmsWorkflowDriver {

    /** Table key for tasks. */
    protected static final String C_TABLE_TASK = "CMS_TASKS";

    /** Table key for task logs. */
    protected static final String C_TABLE_TASKLOG = "CMS_TASKLOG";

    /** Table key for task par(ameter). */
    protected static final String C_TABLE_TASKPAR = "CMS_TASKPAR";

    /** Table key for task type. */
    protected static final String C_TABLE_TASKTYPE = "CMS_TASKTYPE";
    
    /** The driver manager. */
    protected CmsDriverManager m_driverManager;

    /** The SQL manager. */
    protected org.opencms.db.generic.CmsSqlManager m_sqlManager;
    
    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#createTask(org.opencms.db.CmsDbContext, int, int, int, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, java.lang.String, java.sql.Timestamp, java.sql.Timestamp, int)
     */
    public CmsTask createTask(CmsDbContext dbc, int rootId, int parentId, int tasktype, CmsUUID ownerId, CmsUUID agentId, CmsUUID roleId, String taskname, java.sql.Timestamp wakeuptime, java.sql.Timestamp timeout, int priority) throws CmsDataAccessException {

        // fetch new task id
        int newId = m_sqlManager.nextId(C_TABLE_TASK);
        // create the task id entry in the DB                 
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_CREATE");
            stmt.setInt(1, newId);
            stmt.executeUpdate();

        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        // create the task object, note that this does not user the "task type" table
        // because the generic SQL does not work with MySQL 4 
        CmsTask task = new CmsTask(newId, taskname, I_CmsConstants.C_TASK_STATE_STARTED, tasktype, rootId, parentId, ownerId, roleId, agentId, agentId, new java.sql.Timestamp(System.currentTimeMillis()), wakeuptime, timeout, null, 0, "30308", priority, 0, "../taskforms/adhoc.asp", 0, 1);
        // write task
        task = writeTask(dbc, task);
        return task;
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#destroy()
     */
    public void destroy() throws Throwable {
        finalize();

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Shutting down        : " + this.getClass().getName() + " ... ok!");
        }
    }
    
    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#endTask(org.opencms.db.CmsDbContext, int)
     */
    public void endTask(CmsDbContext dbc, int taskId) throws CmsDataAccessException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_END");
            stmt.setInt(1, 100);
            stmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setInt(3, taskId);
            stmt.executeUpdate();

        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#forwardTask(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public void forwardTask(CmsDbContext dbc, int taskId, CmsUUID newRoleId, CmsUUID newUserId) throws CmsDataAccessException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_FORWARD");
            stmt.setString(1, newRoleId.toString());
            stmt.setString(2, newUserId.toString());
            stmt.setInt(3, taskId);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }
    
    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#getSqlManager()
     */    
    public CmsSqlManager getSqlManager() {
        return m_sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(org.opencms.db.CmsDbContext, org.opencms.configuration.CmsConfigurationManager, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(CmsDbContext dbc, CmsConfigurationManager configurationManager, List successiveDrivers, CmsDriverManager driverManager) {
        
        Map configuration = configurationManager.getConfiguration();
        String poolUrl = (String)configuration.get("db.workflow.pool");
        String classname = (String)configuration.get("db.workflow.sqlmanager");
        m_sqlManager = this.initSqlManager(classname);
        m_sqlManager.init(I_CmsWorkflowDriver.C_DRIVER_TYPE_ID, poolUrl);        

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
     * @see org.opencms.db.I_CmsWorkflowDriver#initSqlManager(String)
     */
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readAgent(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID)
     */
    public CmsUUID readAgent(CmsDbContext dbc, CmsUUID roleId) throws CmsDataAccessException {
        CmsUUID result = CmsUUID.getNullUUID();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_FIND_AGENT");
            stmt.setString(1, roleId.toString());
            res = stmt.executeQuery();

            if (res.next()) {
                result = new CmsUUID(res.getString(1));
            }
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } catch (Exception exc) {
            throw new CmsDataAccessException(exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readProject(org.opencms.db.CmsDbContext, org.opencms.workflow.CmsTask)
     */
    public CmsProject readProject(CmsDbContext dbc, CmsTask task) throws CmsDataAccessException {
        PreparedStatement stmt = null;
        CmsProject project = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_PROJECTS_READ_BYTASK");

            stmt.setInt(1, task.getId());
            res = stmt.executeQuery();

            if (res.next()) {
                project = new CmsProject(res, m_sqlManager);
            } else {
                // project not found!
                throw new CmsObjectNotFoundException("Project not found for task: " + task);
            }
        } catch (SQLException e) {
            throw new CmsSqlException(this, stmt, e);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return project;
    }    
    
    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readProjectLogs(org.opencms.db.CmsDbContext, int)
     */
    public List readProjectLogs(CmsDbContext dbc, int projectid) throws CmsDataAccessException {
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
            conn = m_sqlManager.getConnection(dbc);
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
            throw new CmsSqlException(this, stmt, exc);
        } catch (Exception exc) {
            throw new CmsDataAccessException(exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return logs;
    }    

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readTask(org.opencms.db.CmsDbContext, int)
     */
    public CmsTask readTask(CmsDbContext dbc, int id) throws CmsDataAccessException {
        ResultSet res = null;
        CmsTask task = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_READ");
            stmt.setInt(1, id);
            res = stmt.executeQuery();

            if (res.next()) {
                task = internalCreateTask(res);
            }
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } catch (Exception exc) {
            throw new CmsDataAccessException(exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return task;
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readTaskLog(org.opencms.db.CmsDbContext, int)
     */
    public CmsTaskLog readTaskLog(CmsDbContext dbc, int id) throws CmsDataAccessException {
        ResultSet res = null;
        CmsTaskLog tasklog = null;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKLOG_READ");
            stmt.setInt(1, id);
            res = stmt.executeQuery();
            if (res.next()) {
                String comment = res.getString(m_sqlManager.readQuery("C_LOG_COMMENT"));
                id = res.getInt(m_sqlManager.readQuery("C_LOG_ID"));
                java.sql.Timestamp starttime = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_LOG_STARTTIME"));
                CmsUUID user = new CmsUUID(res.getString(m_sqlManager.readQuery("C_LOG_USER")));
                int type = res.getInt(m_sqlManager.readQuery("C_LOG_TYPE"));

                tasklog = new CmsTaskLog(id, comment, user, starttime, type);
            }
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } catch (Exception exc) {
            throw new CmsDataAccessException(exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return tasklog;
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readTaskLogs(org.opencms.db.CmsDbContext, int)
     */
    public List readTaskLogs(CmsDbContext dbc, int taskId) throws CmsDataAccessException {
        Connection conn = null;
        ResultSet res = null;
        CmsTaskLog tasklog = null;
        List logs = new ArrayList();
        PreparedStatement stmt = null;
        String comment = null;
        java.sql.Timestamp starttime = null;
        int id = I_CmsConstants.C_UNKNOWN_ID;
        CmsUUID user = CmsUUID.getNullUUID();
        int type = I_CmsConstants.C_UNKNOWN_ID;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKLOG_READ_LOGS");
            stmt.setInt(1, taskId);
            res = stmt.executeQuery();
            while (res.next()) {
                comment = res.getString(m_sqlManager.readQuery("C_TASKLOG_COMMENT"));
                id = res.getInt(m_sqlManager.readQuery("C_TASKLOG_ID"));
                starttime = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_TASKLOG_STARTTIME"));
                user = new CmsUUID(res.getString(m_sqlManager.readQuery("C_TASKLOG_USER")));
                type = res.getInt(m_sqlManager.readQuery("C_TASKLOG_TYPE"));
                tasklog = new CmsTaskLog(id, comment, user, starttime, type);
                logs.add(tasklog);
            }
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } catch (Exception exc) {
            throw new CmsDataAccessException(exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return logs;
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readTaskParameter(org.opencms.db.CmsDbContext, int, java.lang.String)
     */
    public String readTaskParameter(CmsDbContext dbc, int taskId, String parname) throws CmsDataAccessException {

        String result = null;
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKPAR_GET");
            stmt.setInt(1, taskId);
            stmt.setString(2, parname);
            res = stmt.executeQuery();
            if (res.next()) {
                result = res.getString(m_sqlManager.readQuery("C_PAR_VALUE"));
            }
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readTasks(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsUser, org.opencms.file.CmsUser, org.opencms.file.CmsGroup, int, java.lang.String, java.lang.String)
     */
    public List readTasks(CmsDbContext dbc, CmsProject project, CmsUser agent, CmsUser owner, CmsGroup role, int tasktype, String orderBy, String sort) throws CmsDataAccessException {
        boolean first = true;
        List tasks = new ArrayList(); // vector for the return result
        CmsTask task = null; // tmp task for adding to vector
        ResultSet res = null;
        Connection conn = null;

        // create the sql string depending on parameters
        // handle the project for the SQL String
        String sqlstr = "SELECT * FROM " + m_sqlManager.readQuery("C_TABLENAME_TASK") + " WHERE ";
        if (project != null) {
            sqlstr = sqlstr + m_sqlManager.readQuery("C_TASK_ROOT") + "=" + project.getTaskId();
            first = false;
        } else {
            sqlstr = sqlstr + m_sqlManager.readQuery("C_TASK_ROOT") + "<> 0 AND " + m_sqlManager.readQuery("C_TASK_PARENT") + "<> 0";
            first = false;
        }

        // handle the agent for the SQL String
        if (agent != null) {
            if (!first) {
                sqlstr = sqlstr + " AND ";
            }
            sqlstr = sqlstr + m_sqlManager.readQuery("C_TASK_AGENTUSER") + "='" + agent.getId() + "'";
            first = false;
        }

        // handle the owner for the SQL String
        if (owner != null) {
            if (!first) {
                sqlstr = sqlstr + " AND ";
            }
            sqlstr = sqlstr + m_sqlManager.readQuery("C_TASK_INITIATORUSER") + "='" + owner.getId() + "'";
            first = false;
        }

        // handle the role for the SQL String
        if (role != null) {
            if (!first) {
                sqlstr = sqlstr + " AND ";
            }
            sqlstr = sqlstr + m_sqlManager.readQuery("C_TASK_ROLE") + "='" + role.getId() + "'";
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
            conn = m_sqlManager.getConnection(dbc);
            stmt = conn.createStatement();
            res = stmt.executeQuery(sqlstr);

            // if resultset exists - return vector of tasks
            while (res.next()) {
                task = internalCreateTask(res);
                tasks.add(task);
            }

        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } catch (Exception exc) {
            throw new CmsDataAccessException(exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }

        return tasks;
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#readTaskType(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public int readTaskType(CmsDbContext dbc, String taskName) throws CmsDataAccessException {
        int result = 1;

        PreparedStatement stmt = null;
        ResultSet res = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_GET_TASKTYPE");
            stmt.setString(1, taskName);
            res = stmt.executeQuery();
            if (res.next()) {
                result = res.getInt("id");
            }
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#writeSystemTaskLog(org.opencms.db.CmsDbContext, int, java.lang.String)
     */
    public void writeSystemTaskLog(CmsDbContext dbc, int taskid, String comment) throws CmsDataAccessException {

        this.writeTaskLog(dbc, taskid, CmsUUID.getNullUUID(), new java.sql.Timestamp(System.currentTimeMillis()), comment, I_CmsConstants.C_TASKLOG_USER);
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#writeTask(org.opencms.db.CmsDbContext, org.opencms.workflow.CmsTask)
     */
    public CmsTask writeTask(CmsDbContext dbc, CmsTask task) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
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
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        return (readTask(dbc, task.getId()));
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#writeTaskLog(org.opencms.db.CmsDbContext, int, org.opencms.util.CmsUUID, java.sql.Timestamp, java.lang.String, int)
     */
    public void writeTaskLog(CmsDbContext dbc, int taskId, CmsUUID userId, java.sql.Timestamp starttime, String comment, int type) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKLOG_WRITE");

            stmt.setInt(1, m_sqlManager.nextId(C_TABLE_TASKLOG));
            stmt.setInt(2, taskId);
            if (!userId.isNullUUID()) {
                stmt.setString(3, userId.toString());
            } else {
                // no user is specified so set to system user is only valid for system task log
                stmt.setString(3, m_driverManager.readUser(dbc, OpenCms.getDefaultUsers().getUserGuest(), I_CmsConstants.C_USER_TYPE_SYSTEMUSER).getId().toString());
            }
            stmt.setTimestamp(4, starttime);
            stmt.setString(5, m_sqlManager.validateEmpty(comment));
            stmt.setInt(6, type);

            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#writeTaskParameter(org.opencms.db.CmsDbContext, int, java.lang.String, java.lang.String)
     */
    public void writeTaskParameter(CmsDbContext dbc, int taskId, String parname, String parvalue) throws CmsDataAccessException {

        ResultSet res = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKPAR_TEST");
            // test if the parameter already exists for this task
            stmt.setInt(1, taskId);
            stmt.setString(2, parname);
            res = stmt.executeQuery();

            if (res.next()) {
                //Parameter exisits, so make an update
                internalWriteTaskParameter(dbc, res.getInt(m_sqlManager.readQuery("C_PAR_ID")), parvalue);
            } else {
                //Parameter is not exisiting, so make an insert
                internalWriteTaskParameter(dbc, taskId, parname, parvalue);

            }
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, res);
        }
    }

    /**
     * @see org.opencms.db.I_CmsWorkflowDriver#writeTaskType(org.opencms.db.CmsDbContext, int, int, java.lang.String, java.lang.String, java.lang.String, int, int)
     */
    public void writeTaskType(CmsDbContext dbc, int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref) throws CmsDataAccessException {
        ResultSet res = null;
        PreparedStatement stmt = null;
        Connection conn = null;

        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASK_GET_TASKTYPE");
            // test if the parameter already exists for this task
            stmt.setString(1, name);
            res = stmt.executeQuery();

            if (res.next()) {
                //Parameter exists, so make an update
                internalWriteTaskType(dbc, res.getInt(m_sqlManager.readQuery("C_PAR_ID")), autofinish, escalationtyperef, htmllink, name, permission, priorityref, roleref);

            } else {
                //Parameter is not existing, so make an insert
                internalWriteTaskType(dbc, autofinish, escalationtyperef, htmllink, name, permission, priorityref, roleref);

            }
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, res);
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
     * Semi-constructor to create a CmsTask instance from a JDBC result set.
     * 
     * @param res the result set from the query
     * @return the CmsTash created from the data
     * @throws SQLException if something goes wrong
     */
    protected CmsTask internalCreateTask(ResultSet res) throws SQLException {
        int autofinish = res.getInt(m_sqlManager.readQuery("C_TASK_AUTOFINISH"));
        java.sql.Timestamp endtime = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_TASK_ENDTIME"));
        int escalationtype = res.getInt(m_sqlManager.readQuery("C_TASK_ESCALATIONTYPE"));
        int id = res.getInt(m_sqlManager.readQuery("C_TASK_ID"));
        CmsUUID initiatoruser = new CmsUUID(res.getString(m_sqlManager.readQuery("C_TASK_INITIATORUSER")));
        int milestone = res.getInt(m_sqlManager.readQuery("C_TASK_MILESTONE"));
        String name = res.getString(m_sqlManager.readQuery("C_TASK_NAME"));
        CmsUUID originaluser = new CmsUUID(res.getString(m_sqlManager.readQuery("C_TASK_ORIGINALUSER")));
        CmsUUID agentuser = new CmsUUID(res.getString(m_sqlManager.readQuery("C_TASK_AGENTUSER")));
        int parent = res.getInt(m_sqlManager.readQuery("C_TASK_PARENT"));
        int percentage = res.getInt(m_sqlManager.readQuery("C_TASK_PERCENTAGE"));
        String permission = res.getString(m_sqlManager.readQuery("C_TASK_PERMISSION"));
        int priority = res.getInt(m_sqlManager.readQuery("C_TASK_PRIORITY"));
        CmsUUID role = new CmsUUID(res.getString(m_sqlManager.readQuery("C_TASK_ROLE")));
        int root = res.getInt(m_sqlManager.readQuery("C_TASK_ROOT"));
        java.sql.Timestamp starttime = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_TASK_STARTTIME"));
        int state = res.getInt(m_sqlManager.readQuery("C_TASK_STATE"));
        int tasktype = res.getInt(m_sqlManager.readQuery("C_TASK_TASKTYPE"));
        java.sql.Timestamp timeout = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_TASK_TIMEOUT"));
        java.sql.Timestamp wakeuptime = CmsDbUtil.getTimestamp(res, m_sqlManager.readQuery("C_TASK_WAKEUPTIME"));
        String htmllink = res.getString(m_sqlManager.readQuery("C_TASK_HTMLLINK"));

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
                result = result + m_sqlManager.readQuery("C_TASK_ROOT") + "<>0";
                break;

            case I_CmsConstants.C_TASKS_OPEN :
                result = result + m_sqlManager.readQuery("C_TASK_STATE") + "=" + I_CmsConstants.C_TASK_STATE_STARTED;
                break;

            case I_CmsConstants.C_TASKS_ACTIVE :
                result = result + m_sqlManager.readQuery("C_TASK_STATE") + "=" + I_CmsConstants.C_TASK_STATE_STARTED;
                break;

            case I_CmsConstants.C_TASKS_DONE :
                result = result + m_sqlManager.readQuery("C_TASK_STATE") + "=" + I_CmsConstants.C_TASK_STATE_ENDED;
                break;

            case I_CmsConstants.C_TASKS_NEW :
                result = result + m_sqlManager.readQuery("C_TASK_PERCENTAGE") + "='0' AND " + m_sqlManager.readQuery("C_TASK_STATE") + "=" + I_CmsConstants.C_TASK_STATE_STARTED;
                break;

            default :
                }

        return result;
    }

    /**
     * Updates a task parameter.<p>
     * 
     * @param dbc the current database context
     * @param parid the id of the parameter
     * @param parvalue the value of the parameter
     *  
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalWriteTaskParameter(CmsDbContext dbc, int parid, String parvalue) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKPAR_UPDATE");
            stmt.setString(1, parvalue);
            stmt.setInt(2, parid);
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * Adds a task parameter to a task.<p>
     * 
     * @param dbc the current database context
     * @param taskId the id of the task
     * @param parname the name of the parameter
     * @param parvalue the value of the parameter
     * 
     * @return the id of the new parameter
     * @throws CmsDataAccessException if something goes wrong
     */
    protected int internalWriteTaskParameter(CmsDbContext dbc, int taskId, String parname, String parvalue) throws CmsDataAccessException {
        PreparedStatement stmt = null;
        Connection conn = null;
        int newId = I_CmsConstants.C_UNKNOWN_ID;
        try {
            conn = m_sqlManager.getConnection(dbc);
            stmt = m_sqlManager.getPreparedStatement(conn, "C_TASKPAR_INSERT");
            newId = m_sqlManager.nextId(C_TABLE_TASKPAR);
            stmt.setInt(1, newId);
            stmt.setInt(2, taskId);
            stmt.setString(3, m_sqlManager.validateEmpty(parname));
            stmt.setString(4, m_sqlManager.validateEmpty(parvalue));
            stmt.executeUpdate();
        } catch (SQLException exc) {
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        return newId;
    }

    /**
     * Updates a task.<p>
     * 
     * @param dbc the current database context
     * @param taskId the id of the task
     * @param autofinish tbd
     * @param escalationtyperef tbd
     * @param htmllink tbd
     * @param name tbd
     * @param permission tbd
     * @param priorityref tbd
     * @param roleref tbd
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalWriteTaskType(CmsDbContext dbc, int taskId, int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref) throws CmsDataAccessException {

        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = m_sqlManager.getConnection(dbc);
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
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
    }

    /**
     * Inserts a new task.<p>
     * 
     * @param dbc the current database context
     * @param autofinish tbd
     * @param escalationtyperef tbd
     * @param htmllink tbd
     * @param name tbd
     * @param permission tbd
     * @param priorityref tbd
     * @param roleref tbd
     * 
     * @return tbd
     * @throws CmsDataAccessException tbd
     */
    protected int internalWriteTaskType(CmsDbContext dbc, int autofinish, int escalationtyperef, String htmllink, String name, String permission, int priorityref, int roleref) throws CmsDataAccessException {
        PreparedStatement stmt = null;
        Connection conn = null;

        int newId = I_CmsConstants.C_UNKNOWN_ID;

        try {
            conn = m_sqlManager.getConnection(dbc);
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
            throw new CmsSqlException(this, stmt, exc);
        } finally {
            // close all db-resources
            m_sqlManager.closeAll(dbc, conn, stmt, null);
        }
        return newId;
    }
    
}

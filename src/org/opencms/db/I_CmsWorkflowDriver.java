/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/Attic/I_CmsWorkflowDriver.java,v $
 * Date   : $Date: 2005/02/17 12:43:46 $
 * Version: $Revision: 1.22 $
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

package org.opencms.db;

import org.opencms.util.*;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;

import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;

import java.util.List;

/**
 * Definitions of all required workflow driver methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.22 $ $Date: 2005/02/17 12:43:46 $
 * @since 5.1
 */
public interface I_CmsWorkflowDriver {

    /** The type ID to identify workflow driver implementations. */
    int C_DRIVER_TYPE_ID = 4;

    /**
     * Creates a new task.<p>
     * 
     * @param dbc the current database context
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
     * @throws CmsException if something goes wrong
     */
    CmsTask createTask(
        CmsDbContext dbc,
        int rootId,
        int parentId,
        int tasktype,
        CmsUUID ownerId,
        CmsUUID agentId,
        CmsUUID roleId,
        String taskname,
        java.sql.Timestamp wakeuptime,
        java.sql.Timestamp timeout,
        int priority) throws CmsException;

    /**
     * Destroys this driver.<p>
     * 
     * @throws Throwable if something goes wrong
     * @throws CmsException if something else goes wrong
     */
    void destroy() throws Throwable, CmsException;

    /**
     * Ends a task.<p>
     *
     * @param dbc the current database context
     * @param taskId Id of the task to end
     * 
     * @throws CmsException if something goes wrong
     */
    void endTask(CmsDbContext dbc, int taskId) throws CmsException;

    /**
     * Forwards a task to a new user.<p>
     *
     * @param dbc the current database context
     * @param taskId the Id of the task to forward
     * @param newRoleId the new group name for the task
     * @param newUserId the new user who gets the task
     * 
     * @throws CmsException if something goes wrong
     */
    void forwardTask(CmsDbContext dbc, int taskId, CmsUUID newRoleId, CmsUUID newUserId) throws CmsException;

    /**
     * Returns the SqlManager of this driver.<p>
     * 
     * @return the SqlManager of this driver
     */
    CmsSqlManager getSqlManager();

    /**
     * Initializes the SQL manager for this driver.<p>
     *  
     * To obtain JDBC connections from different pools, further 
     * {online|offline|backup} pool Urls have to be specified.<p>
     * 
     * @param classname the classname of the SQL manager
     * 
     * @return the SQL manager for this driver
     */
    org.opencms.db.generic.CmsSqlManager initSqlManager(String classname);

    /**
     * Finds an agent for a given role (group).<p>
     * 
     * @param dbc the current database context
     * @param roleId The Id for the role (group)
     *
     * @return A vector with the tasks
     *
     * @throws CmsException if something goes wrong
     */
    CmsUUID readAgent(CmsDbContext dbc, CmsUUID roleId) throws CmsException;

    /**
     * Reads a project of a given task.<p>
     *
     * @param dbc the current database context
     * @param task the task to read the project of
     * 
     * @return the project of the task
     * 
     * @throws CmsException if something goes wrong
     */
    CmsProject readProject(CmsDbContext dbc, CmsTask task) throws CmsException;

    /**
     * Reads all task log entries for a project.
     *
     * @param dbc the current database context
     * @param projectId the id of the project for which the tasklog will be read
     * 
     * @return a list of <code>{@link CmsTaskLog}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    List readProjectLogs(CmsDbContext dbc, int projectId) throws CmsException;

    /**
     * Reads the task with the given id.<p>
     *
     * @param dbc the current database context
     * @param id the id for the task to read
     * 
     * @return the task with the given id
     * 
     * @throws CmsException if something goes wrong
     */
    CmsTask readTask(CmsDbContext dbc, int id) throws CmsException;

    /**
     * Reads a log for a task.<p>
     *
     * @param dbc the current database context
     * @param id The id for the tasklog
     * 
     * @return a new TaskLog object
     * @throws CmsException if something goes wrong
     */
    CmsTaskLog readTaskLog(CmsDbContext dbc, int id) throws CmsException;

    /**
     * Reads log entries for a task.<p>
     *
     * @param dbc the current satabase context
     * @param taskId the task for the tasklog to read
     * 
     * @return a list of <code>{@link CmsTaskLog}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    List readTaskLogs(CmsDbContext dbc, int taskId) throws CmsException;

    /**
     * Returns the value of the given parameter for the given task.<p>
     *
     * @param dbc the current database context
     * @param taskId the Id of the task
     * @param parName name of the parameter
     * 
     * @return task parameter value
     * 
     * @throws CmsException if something goes wrong
     */
    String readTaskParameter(CmsDbContext dbc, int taskId, String parName) throws CmsException;

    /**
     * Reads all given tasks from a user for a project.<p>
     * 
     * Most parameters can be <code>null</code>, 
     * if you do not want to filter the tasks by them.
     *
     * The <code>tasktype</code> parameter will filter the tasks.
     * The possible values for this parameter are:<br>
     * <ul>
     * <il><code>{@link org.opencms.main.I_CmsConstants#C_TASKS_ALL}</code>: Reads all tasks</il>
     * <il><code>{@link org.opencms.main.I_CmsConstants#C_TASKS_OPEN}</code>: Reads all open tasks</il>
     * <il><code>{@link org.opencms.main.I_CmsConstants#C_TASKS_DONE}</code>: Reads all finished tasks</il>
     * <il><code>{@link org.opencms.main.I_CmsConstants#C_TASKS_NEW}</code>: Reads all new tasks</il>
     * </ul>
     *
     * @param dbc the current database context
     * @param project the id of the project in which the tasks are defined
     * @param agent the owner of the task
     * @param owner the owner of the task
     * @param role the owner of the task
     * @param taskType the type of task you want to read
     * @param orderBy specifies how to order the tasks
     * @param sort sorting of the tasks
     * 
     * @return a list of given <code>{@link CmsTask}</code> objects for a user for a project
     * 
     * @throws CmsException if operation was not successful
     */
    List readTasks(
        CmsDbContext dbc,
        CmsProject project,
        CmsUser agent,
        CmsUser owner,
        CmsGroup role,
        int taskType,
        String orderBy,
        String sort) throws CmsException;

    /**
     * Get the template task id fo a given taskname.<p>
     *
     * @param dbc the current database context
     * @param taskName Name of the Task
     *
     * @return id from the task template
     *
     * @throws CmsException if something goes wrong
     */
    int readTaskType(CmsDbContext dbc, String taskName) throws CmsException;

    /**
     * Writes a system task log entry.<p>
     * 
     * @param dbc the current database context
     * @param taskid the id of the task
     * @param comment the log entry
     * 
     * @throws CmsException if something goes wrong
     */
    void writeSystemTaskLog(CmsDbContext dbc, int taskid, String comment) throws CmsException;

    /**
     * Writes a task.<p>
     *
     * @param dbc the current database context
     * @param task the task to write
     * 
     * @return written task object
     * @throws CmsException if something goes wrong
     */
    CmsTask writeTask(CmsDbContext dbc, CmsTask task) throws CmsException;

    /**
     * Writes new log for a task.<p>
     *
     * @param dbc the current database context
     * @param taskId The id of the task
     * @param userId User who added the Log
     * @param starttime Time when the log is created
     * @param comment Description for the log
     * @param type Type of the log. 0 = Sytem log, 1 = User Log
     *
     * @throws CmsException if something goes wrong
     */
    void writeTaskLog(
        CmsDbContext dbc,
        int taskId,
        CmsUUID userId,
        java.sql.Timestamp starttime,
        String comment,
        int type) throws CmsException;

    /**
     * Set a Parameter for a task.<p>
     *
     * @param dbc the current database context
     * @param taskId the task
     * @param parname the name of the parameter
     * @param parvalue the value of the parameter
     *
     * @throws CmsException if something goes wrong
     */
    void writeTaskParameter(CmsDbContext dbc, int taskId, String parname, String parvalue) throws CmsException;

    /**
     * Creates a new tasktype set in the database.<p>
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
     * @throws CmsException if something goes wrong
     */
    void writeTaskType(
        CmsDbContext dbc,
        int autofinish,
        int escalationtyperef,
        String htmllink,
        String name,
        String permission,
        int priorityref,
        int roleref) throws CmsException;

}
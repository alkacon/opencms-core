/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workflow/Attic/CmsTaskService.java,v $
 * Date   : $Date: 2005/06/23 10:47:25 $
 * Version: $Revision: 1.10 $
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

package org.opencms.workflow;

import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;

import java.util.List;

/**
 * Just a convenience wrapper for 
 * workflow related methods.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 6.0.0 
 */
public class CmsTaskService {

    /** Task preferences filter. */
    public static final String TASK_FILTER = "task.filter.";

    /** Task preferences message flags. */
    public static final String TASK_MESSAGES = "TaskMessages";

    /** state values of task messages when accepted. */
    public static final int TASK_MESSAGES_ACCEPTED = 1;

    /** state values of task messages when completed. */
    public static final int TASK_MESSAGES_COMPLETED = 4;

    /** state values of task messages when forwarded. */
    public static final int TASK_MESSAGES_FORWARDED = 2;

    /** state values of task messages when members. */
    public static final int TASK_MESSAGES_MEMBERS = 8;

    /** Task priority high. */
    public static final int TASK_PRIORITY_HIGH = 1;

    /** Task priority low. */
    public static final int TASK_PRIORITY_LOW = 3;

    /** 
     * Task priority normal.
     **/
    public static final int TASK_PRIORITY_NORMAL = 2;

    /** state values of a task ended. */
    public static final int TASK_STATE_ENDED = 4;

    /** state values of a task halted. */
    public static final int TASK_STATE_HALTED = 5;

    /** state values of a task ready to end. */
    public static final int TASK_STATE_NOTENDED = 3;

    /** state values of a task prepared to start. */
    public static final int TASK_STATE_PREPARE = 0;

    /** state values of a task ready to start. */
    public static final int TASK_STATE_START = 1;

    /** state values of a task started. */
    public static final int TASK_STATE_STARTED = 2;

    /**System type values for the task log. */
    public static final int TASKLOG_SYSTEM = 0;

    /**User type value for the task log. */
    public static final int TASKLOG_USER = 1;

    /** Task type value of getting active tasks. */
    public static final int TASKS_ACTIVE = 4;

    /** Task type value of getting all tasks. */
    public static final int TASKS_ALL = 1;

    /** Task type value of getting done tasks. */
    public static final int TASKS_DONE = 5;

    /** Task type value of getting new tasks. */
    public static final int TASKS_NEW = 2;

    /** Task type value of getting open tasks. */
    public static final int TASKS_OPEN = 3;

    /** The request context.     */
    protected CmsRequestContext m_context;

    /** The security manager to access the cms.     */
    protected CmsSecurityManager m_securityManager;

    /**
     * Creates a new  <code>{@link CmsTaskService}</code>.<p>
     * 
     * @param context the request context that contains the user authentification
     * @param securityManager the security manager
     */
    public CmsTaskService(CmsRequestContext context, CmsSecurityManager securityManager) {

        m_context = context;
        m_securityManager = securityManager;
    }

    /**
     * Updates the state of the given task as accepted by the current user.<p>
     *
     * @param taskId the id of the task to accept
     *
     * @throws CmsException if operation was not successful
     */
    public void acceptTask(int taskId) throws CmsException {

        m_securityManager.acceptTask(m_context, taskId);
    }

    /**
     * Creates a new task.<p>
     * 
     * @param projectid the id of the current project task of the user
     * @param agentName the user who will edit the task
     * @param roleName a usergroup for the task
     * @param taskname a name of the task
     * @param tasktype the type of the task
     * @param taskcomment a description of the task, which is written as task log entry
     * @param timeout the time when the task must finished
     * @param priority the id for the priority of the task
     * 
     * @return the created task
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsTask createTask(
        int projectid,
        String agentName,
        String roleName,
        String taskname,
        String taskcomment,
        int tasktype,
        long timeout,
        int priority) throws CmsException {

        return m_securityManager.createTask(
            m_context,
            m_context.currentUser(),
            projectid,
            agentName,
            roleName,
            taskname,
            taskcomment,
            tasktype,
            timeout,
            priority);
    }

    /**
     * Creates a new task.<p>
     * 
     * This is just a more limited version of the 
     * <code>{@link #createTask(int, String, String, String, String, int, long, int)}</code>
     * method, where: <br>
     * <ul>
     * <il>the project id is the current project id.</il>
     * <il>the task type is the standard task type <b>1</b>.</il>
     * <il>with no comments</il>
     * </ul><p>
     * 
     * @param agentName the user who will edit the task
     * @param roleName a usergroup for the task
     * @param taskname the name of the task
     * @param timeout the time when the task must finished
     * @param priority the id for the priority of the task
     * 
     * @return the created task
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsTask createTask(String agentName, String roleName, String taskname, long timeout, int priority)
    throws CmsException {

        return (m_securityManager.createTask(m_context, agentName, roleName, taskname, timeout, priority));
    }

    /**
     * Ends a task.<p>
     *
     * @param taskid the id of the task to end
     *
     * @throws CmsException if operation was not successful
     */
    public void endTask(int taskid) throws CmsException {

        m_securityManager.endTask(m_context, taskid);
    }

    /**
     * Forwards a task to a new user.<p>
     *
     * @param taskid the id of the task which will be forwarded
     * @param newRoleName the new group for the task
     * @param newUserName the new user who gets the task. if it is empty, a new agent will automatic selected
     *
     * @throws CmsException if operation was not successful
     */
    public void forwardTask(int taskid, String newRoleName, String newUserName) throws CmsException {

        m_securityManager.forwardTask(m_context, taskid, newRoleName, newUserName);
    }

    /**
     * Returns the value of the given parameter for the given task.<p>
     *
     * @param taskid the id of the task
     * @param parname the name of the parameter
     * 
     * @return the parameter value
     * 
     * @throws CmsException if operation was not successful
     */
    public String getTaskPar(int taskid, String parname) throws CmsException {

        return m_securityManager.getTaskPar(m_context, taskid, parname);
    }

    /**
     * Returns the template task id for a given taskname.<p>
     *
     * @param taskname the name of the task
     * 
     * @return the id of the task template
     * 
     * @throws CmsException if operation was not successful
     */
    public int getTaskType(String taskname) throws CmsException {

        return m_securityManager.getTaskType(m_context, taskname);
    }

    /**
     * Reactivates a task.<p>
     * 
     * Setting its state to <code>{@link org.opencms.workflow.CmsTaskService#TASK_STATE_STARTED}</code> and
     * the percentage to <b>zero</b>.<p>
     *
     * @param taskId the id of the task to reactivate
     *
     * @throws CmsException if something goes wrong
     */
    public void reactivateTask(int taskId) throws CmsException {

        m_securityManager.reactivateTask(m_context, taskId);
    }

    /**
     * Reads the agent of a task.<p>
     *
     * @param task the task to read the agent from
     * 
     * @return the agent of a task
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readAgent(CmsTask task) throws CmsException {

        return m_securityManager.readAgent(m_context, task);
    }

    /**
     * Reads all given tasks from a user for a project.<p>
     *
     * The <code>tasktype</code> parameter will filter the tasks.
     * The possible values for this parameter are:<br>
     * <ul>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_ALL}</code>: Reads all tasks</il>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_OPEN}</code>: Reads all open tasks</il>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_DONE}</code>: Reads all finished tasks</il>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_NEW}</code>: Reads all new tasks</il>
     * </ul>
     *
     * @param projectId the id of the project in which the tasks are defined
     * @param ownerName the owner of the task
     * @param taskType the type of task you want to read
     * @param orderBy specifies how to order the tasks
     * @param sort sorting of the tasks
     * 
     * @return a list of given <code>{@link CmsTask}</code> objects for a user for a project
     * 
     * @throws CmsException if operation was not successful
     */
    public List readGivenTasks(int projectId, String ownerName, int taskType, String orderBy, String sort)
    throws CmsException {

        return m_securityManager.readGivenTasks(m_context, projectId, ownerName, taskType, orderBy, sort);
    }

    /**
     * Reads the group (role) of a task.<p>
     *
     * @param task the task to read the group (role) from
     * 
     * @return the group (role) of the task
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsGroup readGroup(CmsTask task) throws CmsException {

        return m_securityManager.readGroup(m_context, task);
    }

    /**
     * Reads the original agent of a task.<p>
     *
     * @param task the task to read the original agent from
     * 
     * @return the original agent of the task
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOriginalAgent(CmsTask task) throws CmsException {

        return m_securityManager.readOriginalAgent(m_context, task);
    }

    /**
     * Reads the owner (initiator) of a task.<p>
     *
     * @param task the task to read the owner from
     * 
     * @return the owner of the task
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsTask task) throws CmsException {

        return m_securityManager.readOwner(m_context, task);
    }

    /**
     * Reads the owner of a task log.<p>
     *
     * @param log the task log
     * 
     * @return the owner of the task log
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readOwner(CmsTaskLog log) throws CmsException {

        return m_securityManager.readOwner(m_context, log);
    }

    /**
     * Reads a project of a given task.<p>
     *
     * @param task the task for which the project will be read
     * 
     * @return the project of the task
     * 
     * @throws CmsException if operation was not successful
     */
    public CmsProject readProject(CmsTask task) throws CmsException {

        return m_securityManager.readProject(m_context, task);
    }

    /**
     * Reads all task log entries for a project.
     *
     * @param projectId the id of the project for which the tasklog will be read
     * 
     * @return a list of <code>{@link CmsTaskLog}</code> objects
     * 
     * @throws CmsException if operation was not successful
     */
    public List readProjectLogs(int projectId) throws CmsException {

        return m_securityManager.readProjectLogs(m_context, projectId);
    }

    /**
     * Reads the task with the given id.<p>
     *
     * @param id the id of the task to be read
     * 
     * @return the task with the given id
     *
     * @throws CmsException if operation was not successful
     */
    public CmsTask readTask(int id) throws CmsException {

        return m_securityManager.readTask(m_context, id);
    }

    /**
     * Reads log entries for a task.<p>
     *
     * @param taskid the task for which the tasklog will be read
     * 
     * @return a list of <code>{@link CmsTaskLog}</code> objects
     * 
     * @throws CmsException if operation was not successful
     */
    public List readTaskLogs(int taskid) throws CmsException {

        return m_securityManager.readTaskLogs(m_context, taskid);
    }

    /**
     * Reads all tasks for a project.<p>
     *
     * The <code>tasktype</code> parameter will filter the tasks.
     * The possible values are:<br>
     * <ul>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_ALL}</code>: Reads all tasks</il>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_OPEN}</code>: Reads all open tasks</il>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_DONE}</code>: Reads all finished tasks</il>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_NEW}</code>: Reads all new tasks</il>
     * </ul><p>
     *
     * @param projectId the id of the project in which the tasks are defined. Can be null to select all tasks
     * @param tasktype the type of task you want to read
     * @param orderBy specifies how to order the tasks
     * @param sort sort order: C_SORT_ASC, C_SORT_DESC, or null
     * 
     * @return a list of <code>{@link CmsTask}</code> objects for the project
     * 
     * @throws CmsException if operation was not successful
     */
    public List readTasksForProject(int projectId, int tasktype, String orderBy, String sort) throws CmsException {

        return (m_securityManager.readTasksForProject(m_context, projectId, tasktype, orderBy, sort));
    }

    /**
     * Reads all tasks for a role in a project.<p>
     *
     * The <code>tasktype</code> parameter will filter the tasks.
     * The possible values for this parameter are:<br>
     * <ul>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_ALL}</code>: Reads all tasks</il>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_OPEN}</code>: Reads all open tasks</il>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_DONE}</code>: Reads all finished tasks</il>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_NEW}</code>: Reads all new tasks</il>
     * </ul><p>
     *
     * @param projectId the id of the Project in which the tasks are defined
     * @param roleName the role who has to process the task
     * @param tasktype the type of task you want to read
     * @param orderBy specifies how to order the tasks
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
     * 
     * @return list of <code>{@link CmsTask}</code> objects for the role
     * 
     * @throws CmsException if operation was not successful
     */
    public List readTasksForRole(int projectId, String roleName, int tasktype, String orderBy, String sort)
    throws CmsException {

        return m_securityManager.readTasksForRole(m_context, projectId, roleName, tasktype, orderBy, sort);
    }

    /**
     * Reads all tasks for a user in a project.<p>
     *
     * The <code>tasktype</code> parameter will filter the tasks.
     * The possible values for this parameter are:<br>
     * <ul>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_ALL}</code>: Reads all tasks</il>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_OPEN}</code>: Reads all open tasks</il>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_DONE}</code>: Reads all finished tasks</il>
     * <il><code>{@link org.opencms.workflow.CmsTaskService#TASKS_NEW}</code>: Reads all new tasks</il>
     * </ul>
     *
     * @param projectId the id of the Project in which the tasks are defined
     * @param userName the user who has to process the task
     * @param tasktype the type of task you want to read
     * @param orderBy specifies how to order the tasks
     * @param sort sort order C_SORT_ASC, C_SORT_DESC, or null
     * 
     * @return a list of <code>{@link CmsTask}</code> objects for the user 
     * 
     * @throws CmsException if operation was not successful
     */
    public List readTasksForUser(int projectId, String userName, int tasktype, String orderBy, String sort)
    throws CmsException {

        return m_securityManager.readTasksForUser(m_context, projectId, userName, tasktype, orderBy, sort);
    }

    /**
     * Sets a new name for a task.<p>
     *
     * @param taskId the id of the task
     * @param name the new name of the task
     * 
     * @throws CmsException if something goes wrong
     */
    public void setName(int taskId, String name) throws CmsException {

        m_securityManager.setName(m_context, taskId, name);
    }

    /**
     * Sets the priority of a task.<p>
     *
     * @param taskId the id of the task
     * @param priority the new priority value
     * 
     * @throws CmsException if something goes wrong
     */
    public void setPriority(int taskId, int priority) throws CmsException {

        m_securityManager.setPriority(m_context, taskId, priority);
    }

    /**
     * Sets a parameter for a task.<p>
     *
     * @param taskid the id of the task
     * @param parname the name of the parameter
     * @param parvalue the value of the parameter
     * 
     * @throws CmsException if something goes wrong
     */
    public void setTaskPar(int taskid, String parname, String parvalue) throws CmsException {

        m_securityManager.setTaskPar(m_context, taskid, parname, parvalue);
    }

    /**
     * Sets the timeout of a task.<p>
     *
     * @param taskId the id of the task
     * @param timeout the new timeout value
     * 
     * @throws CmsException if something goes wrong
     */
    public void setTimeout(int taskId, long timeout) throws CmsException {

        m_securityManager.setTimeout(m_context, taskId, timeout);
    }

    /**
     * Writes a new user tasklog for a task.<p>
     *
     * @param taskid the Id of the task
     * @param comment the description for the log
     *
     * @throws CmsException if operation was not successful
     */
    public void writeTaskLog(int taskid, String comment) throws CmsException {

        m_securityManager.writeTaskLog(m_context, taskid, comment);
    }

    /**
     * Writes a new user tasklog for a task.<p>
     *
     * @param taskId the id of the task
     * @param comment the description for the log
     * @param taskType the type of the tasklog, user task types must be greater than 100
     * 
     * @throws CmsException if something goes wrong
     */
    public void writeTaskLog(int taskId, String comment, int taskType) throws CmsException {

        m_securityManager.writeTaskLog(m_context, taskId, comment, taskType);
    }
}

package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This public class describes a resource broker for tasks in the Cms.<BR/>
 * <B>All</B> Methods get a first parameter: A_CmsUser. It is the current user. This 
 * is for security-reasons, to check if this current user has the rights to call the
 * method.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Rüdiger Gutfleisch
 * @version $Revision: 1.8 $ $Date: 2000/02/09 10:24:34 $
 */
interface I_CmsRbTask { 	

	/**
	 * Creates a new project for task handling.
	 * 
	 * @param owner User who creates the project
	 * @param projectname Name of the project
	 * @param role Usergroup for the project
	 * @param taskcomment Description of the task
	 * @param timeout Time when the Project must finished
	 * @param priority Priority for the Project
	 * 
	 * @return The new task project
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsTask createProject(A_CmsUser owner, String projectname, 
								   A_CmsGroup role, java.sql.Timestamp timeout, 
								   int priority)
		throws CmsException;

	/**
	 * Creates a new task.
	 * 
	 * @param callingUser User who hast created the task 
	 * @param project Project to witch the task belongs.
	 * @param agent User who will edit the task 
	 * @param role Usergroup for the task
	 * @param taskname Name of the task
	 * @param taskcomment Description of the task
	 * @param timeout Time when the task must finished
	 * @param priority Id for the priority
	 * 
	 * @return A new Task Object
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	
	public A_CmsTask createTask(A_CmsUser callingUser, A_CmsProject project, A_CmsUser agent, A_CmsGroup role, 
								String taskname, String taskcomment, 
								java.sql.Timestamp timeout, int priority)
		throws CmsException;

	
	/**
	 * Ends a task from the Cms.
	 * 
	 * @param user The úser who end th task	 
	 * @param task The task to end.
	 
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void endTask(A_CmsUser callingUser, A_CmsTask task) 
		throws CmsException;
	
	
	/**
	 * Forwards a task to a new user.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param task The task to forward.
	 * @param newUser The user to forward to.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void forwardTask(A_CmsUser callingUser, A_CmsTask task, 
							A_CmsUser newUser) 
		throws CmsException;
	
	
	/**
	 * Accept a task from the Cms.
	 * 
	 * @param callingUser The user who wants to use this method.	 
	 * @param task The task to accept.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void acceptTask(A_CmsUser callingUser, A_CmsTask task)
		throws CmsException;
	
	/**
	 * Set Percentage of a task
	 * 
	 * @param callingUser The user who wants to use this method.	 
	 * @param task The task to set the percentage.
	 * @param new percentage value
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void setPercentage(A_CmsUser callingUser, A_CmsTask task, int percentage)
		throws CmsException;
	
	
	/**
	 * Read a task by id.
	 * 
	 * @param id The id for the task to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsTask readTask(int id)
		throws CmsException;
	
	// TODO: readTask by name is missing here!
	
	
	//--------------------------------------------
	// Task lists
	//----------------------------------------------
	
	/**
	 * Reads all tasks for a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined. Can be null for all tasks
	 * @param orderBy Chooses, how to order the tasks. Valid tasktypes are:
	 * C_TASKS_NEW, C_TASKS_OPEN, C_TASKS_ACTIVE, C_TASKS_DONE
	 * @param orderBy Chooses, how to order the tasks.
	 * @param sort Choose who to sort the tasks. C_TASK_ASC, C_TASK_DESC, or null
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readTasks(A_CmsUser callingUSer, A_CmsProject project, 
							int tasktype, String orderBy, String sort)
		throws CmsException;
	
	
	/**
	 * Reads all tasks for a user in a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined or null for all projects.
	 * @param agent The user who has to process the task or null for all users.
	 * @param tasktype Type of the tasks
	 * @param orderBy Chooses, how to order the tasks. Valid tasktypes are:
	 * C_TASKS_NEW, C_TASKS_OPEN, C_TASKS_ACTIVE, C_TASKS_DONE
	 * @param orderBy Chooses, how to order the tasks.
	 * @param sort Choose who to sort the tasks. C_TASK_ASC, C_TASK_DESC, or null
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readTasks(A_CmsUser callingUser, A_CmsProject project, 
							A_CmsUser agent, int tasktype, String orderBy, String sort) 
		throws CmsException;
	
	
	/**
	 * Reads all posed tasks of a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined or null for all projects..
	 * // TODO: correct the parameter description
	 * @param group The group who has to process the task or null for all groups.
	 * @param orderBy Chooses, how to order the tasks. Valid tasktypes are:
	 * C_TASKS_NEW, C_TASKS_OPEN, C_TASKS_ACTIVE, C_TASKS_DONE
	 * @param orderBy Chooses, how to order the tasks.
	 * @param sort Choose who to sort the tasks. C_TASK_ASC, C_TASK_DESC, or null
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readPosedTasks(A_CmsUser callingUser, A_CmsProject project, 
								 A_CmsUser owner, int taskType, 
								 String orderBy, String sort) 
		throws CmsException;

	
	/**
	 * Writes a new user tasklog for a task.
	 * 
	 * @param callingUser The user who wants to use this method.	 	 
	 * @param task The task .
	 * @param user User who added the Log
	 * @param comment Description for the log
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void writeTaskLog(A_CmsUser callinguser, A_CmsTask task, A_CmsUser user, String comment)
		throws CmsException; 
	
	
	/**
	 * Reads log entries for a task.
	 * 
	 * @param callingUser The user who wants to use this method.	 	 
	 * @param task The task for the tasklog to read .
	 * @return A Vector of new TaskLog objects 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readTaskLogs(A_CmsUser callingUser, A_CmsTask task)
		throws CmsException;
	
	
	/**
	 * Reads log entries for a project.
	 * 
	 * @param callingUser The user who wants to use this method.	 
	 * @param project The projec for tasklog to read.
	 * @return A Vector of new TaskLog objects 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readProjectLogs(A_CmsUser callingUser, A_CmsProject project)
		throws CmsException; 
	
	
	/**
	 * Set a Parameter for a task.
	 * 
	 * @param callingUser The user who wants to use this method.	 
	 * @param task The task.
	 * @param parname Name of the parameter.
	 * @param parvalue Value if the parameter.
	 * 
	 * @return The id of the inserted parameter or 0 if the parameter already exists for this task.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public int setTaskPar(A_CmsUser callingUser, A_CmsTask task, String parname, String parvalue)
		throws CmsException; 

	
	/**
	 * Get a parameter value for a task.
	 * 
	 * @param callingUser The user who wants to use this method.	 
	 * @param task The task.
	 * @param parname Name of the parameter.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public String getTaskPar(A_CmsUser callingUser, A_CmsTask task, String parname)
		throws CmsException;
	
}

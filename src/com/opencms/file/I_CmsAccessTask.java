package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This interface describes the access to projects in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.2 $ $Date: 2000/01/28 18:46:41 $
 */
interface I_CmsAccessTask {

	/**
	 * Reads a task from the Cms.
	 * 
	 * @param id The id of the task to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsTask readTask(int id)
		throws CmsException;

	
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
	 * @return The id of the new task project
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsTask createProject(A_CmsUser owner, String projectname, A_CmsGroup role, java.sql.Timestamp timeout, int priority)
		throws CmsException;
	
	/**
	 * Creates a new task.
	 * 
	 * @param project Project to witch the task belongs.
	 * @param owner User who hast created the task 
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
	
	public A_CmsTask createTask(A_CmsProject project, A_CmsUser owner, A_CmsUser agent, A_CmsGroup role, 
								String taskname, String taskcomment, 
								java.sql.Timestamp timeout, int priority)
		throws CmsException;
	
	
	/**
	 * Updates a task.
	 * 
	 * @param project The project that will be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsTask writeTask(A_CmsTask task)
		throws CmsException;
	
	/**
	 * Ends a task from the Cms.
	 * 
	 * @param user The user who ends the task
	 * @param task The task to end.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void endTask(A_CmsUser user, A_CmsTask task)
		throws CmsException;
	
	/**
	 * Forwards a task to another user.
	 * 
	 * @param task The task that will be fowarded.
	 * @param user User who will get the forwarded task
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void forwardTask(A_CmsTask task, A_CmsUser user)
		throws CmsException;		 
	
	/**
	 * Reads all tasks of a user in a project.
	 * @param project The Project in which the tasks are defined.
	 * @param agent The task agent   
	 * @param owner The task owner .
	 * @param group The group who has to process the task.	 
	 * @tasktype C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
	 * @param orderBy Chooses, how to order the tasks.
	 * @param sort Sort Ascending or Descending (ASC or DESC)
	 * 
	 * @return A vector with the tasks
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readTasks(A_CmsProject project, A_CmsUser agent, A_CmsUser owner, 
							A_CmsGroup role, int tasktype, 
							String orderBy, String sort)
		throws CmsException;
	
	 /**
	 * Writes new log for a task.
	 * 
	 * @param task The task.
	 * @param user User who added the Log.
	 * @param starttime Time when the log is created.
	 * @param comment Description for the log.
	 * @param type Type of the log. 0 = Sytem log, 1 = User Log
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void writeTaskLog(A_CmsTask task, A_CmsUser user, String comment)
		throws CmsException;

	
	/**
	 * Reads log entries for a task.
	 * 
	 * @param task The task for the tasklog to read .
	 * @return A Vector of new TaskLog objects 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readTaskLogs(A_CmsTask task)
		throws CmsException;
	
	/**
	 * Reads log entries for a project.
	 * 
	 * @param project The projec for tasklog to read.
	 * @return A Vector of new TaskLog objects 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readProjectLogs(A_CmsProject project)
		throws CmsException;
	
	/**
	 * Set a Parameter for a task.
	 * 
	 * @param task The task.
	 * @param parname Name of the parameter.
	 * @param parvalue Value if the parameter.
	 * 
	 * @return The id of the inserted parameter or 0 if the parameter already exists for this task.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public int setTaskPar(A_CmsTask task, String parname, String parvalue)
		throws CmsException; 
	
	
	/**
	 * Get a parameter value for a task.
	 * 
	 * @param task The task.
	 * @param parname Name of the parameter.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public String getTaskPar(A_CmsTask task, String parname)
		throws CmsException;	
}

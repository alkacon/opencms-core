package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This class describes a resource broker for projects in the Cms.<BR/>
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Rüdiger Gutfleisch
 * @version $Revision: 1.2 $ $Date: 2000/01/28 18:46:41 $
 */
 class CmsRbTask implements I_CmsRbTask, I_CmsConstants {
	 /**
	  * The project access object which is required to access the
	  * project database.
	  */
	 private I_CmsAccessTask m_accessTask;
	 
	 /**
	  * Constructor, creates a new Cms Project Resource Broker.
	  * 
	  * @param accessProject The project access object.
	  */
	 public CmsRbTask(I_CmsAccessTask accessTask)
	 {
		 m_accessTask = accessTask;
	 }

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
		 throws CmsException {
		 
		 return m_accessTask.createProject(owner, projectname, role, timeout, priority);
	 }
	 
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
		 throws CmsException {
		 return( m_accessTask.createTask(project, callingUser, agent, role, 
										 taskname, taskcomment, 
										 timeout, priority));
		 
	 }

	 /**
	  * Ends a task from the Cms.
	  * 
	  * @param user The úser who end th task	 
	  * @param task The task to end.
	  
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void endTask(A_CmsUser callingUser, A_CmsTask task) 
		 throws CmsException {
		 
		 m_accessTask.endTask(callingUser, task);
	 }	
	 
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
		 throws CmsException{
		 
		 m_accessTask.forwardTask(task, newUser);
	 }
	 
	 /**
	  * Accept a task from the Cms.
	  * 
	  * @param callingUser The user who wants to use this method.	 
	  * @param task The task to accept.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void acceptTask(A_CmsUser callingUser, A_CmsTask task)
		 throws CmsException {
		 
		 setPercentage(callingUser, task, 1);
		 m_accessTask.writeTaskLog(task, callingUser, 
								   "Task was accepted from " + callingUser.getFirstname() + " " + callingUser.getLastname());
	 }
	 
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
		 throws CmsException {
		 
		 task.setPercentage(percentage);
		 task = m_accessTask.writeTask(task);
		 m_accessTask.writeTaskLog(task, callingUser, 
								   "Percentage was set to " + percentage + "% from " + callingUser.getFirstname() + " " + callingUser.getLastname());
	 }
	 
	 /**
	  * Read a task by id.
	  * 
	  * @param id The id for the task to read.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public A_CmsTask readTask(int id)
		 throws CmsException {
		 return( m_accessTask.readTask(id));
	 }
	 
	 
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
	  * @param tasktype Type of the tasks you want to read
	  * @param orderBy Chooses, how to order the tasks. 
	  * @param sort Sort order C_TASK_ASC, C_TASK_DESC, or null
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasks(A_CmsUser callingUSer, A_CmsProject project, int tasktype, String orderBy, String sort)
		 throws CmsException{
		 
		 return m_accessTask.readTasks(project, null, null, null, tasktype, orderBy, sort);
	 }
	 
	 /**
	  * Reads all tasks for a user in a project.
	  * 
	  * <B>Security:</B>
	  * // TODO: Add security-police here
	  * 
	  * @param callingUser The user who wants to use this method.
	  * @param project The Project in which the tasks are defined.
	  * @param user The user who has to process the task.
	  * @param orderBy Chooses, how to order the tasks.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasks(A_CmsUser callingUser, A_CmsProject project, 
							 A_CmsUser agent, int tasktype, String orderBy, String sort) 
		 throws CmsException{
		 
		 return m_accessTask.readTasks(project, agent, null, null, tasktype, orderBy, sort);
	 }
	 
	 
	 /**
	  * Reads all posed tasks of a project.
	  * 
	  * <B>Security:</B>
	  * // TODO: Add security-police here
	  * 
	  * @param callingUser The user who wants to use this method.
	  * @param project The Project in which the tasks are defined.
	  * @param group The group who has to process the task.
	  * @param tasktype
	  * @param orderBy Chooses, how to order the tasks.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readPosedTasks(A_CmsUser callingUser, A_CmsProject project, 
								  A_CmsUser owner, int taskType, 
								  String orderBy, String sort) 
		 throws CmsException{
		 
		 return m_accessTask.readTasks(project, null, owner, null, taskType, orderBy, sort);
	 }

	 
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
		 throws CmsException {
		 
		 m_accessTask.writeTaskLog(task, user, comment);
	 }
	 
	 /**
	  * Reads log entries for a task.
	  * 
	  * @param callingUser The user who wants to use this method.	 	 
	  * @param task The task for the tasklog to read .
	  * @return A Vector of new TaskLog objects 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTaskLogs(A_CmsUser callingUser, A_CmsTask task)
		 throws CmsException{
		 return m_accessTask.readTaskLogs(task);	 
	 }
	 
	 /**
	  * Reads log entries for a project.
	  * 
	  * @param callingUser The user who wants to use this method.	 
	  * @param project The projec for tasklog to read.
	  * @return A Vector of new TaskLog objects 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readProjectLogs(A_CmsUser callingUser, A_CmsProject project)
		 throws CmsException {
		 return m_accessTask.readProjectLogs(project);	 
	 }
	 
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
		 throws CmsException {
		 
		 // TODO security for calling user
		 return( m_accessTask.setTaskPar(task, parname, parvalue));		
	 }

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
		 throws CmsException {
		 
		 // TODO security for calling user
		 return( m_accessTask.getTaskPar(task, parname));		
	 }
 }
/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsRbTask.java,v $
 * Date   : $Date: 2000/03/15 14:32:14 $
 * Version: $Revision: 1.16 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

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
 * @version $Revision: 1.16 $ $Date: 2000/03/15 14:32:14 $
 */
interface I_CmsRbTask { 	

	 /**
	  * Creates a new project for task handling.
	  * 
	  * @param owner User who creates the project
	  * @param projectname Name of the project
	  * @param projectType Type of the Project
	  * @param role Usergroup for the project
	  * @param timeout Time when the Project must finished
	  * @param priority Priority for the Project
	  * 
	  * @return The new task project
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public A_CmsTask createProject(A_CmsUser owner, String projectname, int projectType,
									A_CmsGroup role, java.sql.Timestamp timeout, 
									int priority)
		 throws CmsException;
	 
	 /**
	  * Creates a new project for task handling.
	  * 
	  * @param owner User who creates the project
	  * @param projectname Name of the project
	  * @param role Usergroup for the project
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
	  * @param currentUser User who create the task 
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
	 
	 public A_CmsTask createTask(A_CmsUser currentUser, A_CmsProject project, A_CmsUser agent, A_CmsGroup role, 
								 String taskname, String taskcomment, 
								 java.sql.Timestamp timeout, int priority)
		 throws CmsException;
	 
	 	 /**
	  * Creates a new task.
	  * 
	  * @param currentUser User who create the task 
	  * @param project Project to witch the task belongs.
	  * @param agent User who will edit the task 
	  * @param role Usergroup for the task
	  * @param taskname Name of the task
	  * @param taskcomment Description of the task
	  * @param tasktype Template Type of the task
	  * @param timeout Time when the task must finished
	  * @param priority Id for the priority
	  * 
	  * @return A new Task Object
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 
	 public A_CmsTask createTask(A_CmsUser currentUser, A_CmsProject project, A_CmsUser agent, A_CmsGroup role, 
								 String taskname, String taskcomment, int tasktype,
								 java.sql.Timestamp timeout, int priority)
		 throws CmsException;
	 
	 	 /**
	  * Creates a new task.
	  * 
	  * @param currentUser User who create the task 
	  * @param projectid TaskProject to witch the task belongs.
	  * @param agent User who will edit the task 
	  * @param role Usergroup for the task
	  * @param taskname Name of the task
	  * @param taskcomment Description of the task
	  * @param tasktype Template Type of the task
	  * @param timeout Time when the task must finished
	  * @param priority Id for the priority
	  * 
	  * @return A new Task Object
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 
	 public A_CmsTask createTask(A_CmsUser currentUser, int projectid, A_CmsUser agent, A_CmsGroup role, 
								 String taskname, String taskcomment, int tasktype,
								 java.sql.Timestamp timeout, int priority)
		 throws CmsException;

	 /**
	  * Reaktivates a task from the Cms.
	  * 
	  * @param currentUser The user who reaktivates the task.	 
	  * @param taskid The Id of the task to accept.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void reaktivateTask(A_CmsUser currentUser, int taskId)
		 throws CmsException;
	 
	 /**
	  * Ends a task from the Cms.
	  * 
	  * @param currentUser User who wants to end the task
	  * @param taskid The ID of the task to end.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void endTask(A_CmsUser currentUser, int taskid) 
		 throws CmsException;
	 
	 /**
	  * Forwards a task to a new user.
	  * 
	  * @param currentUser The user who forwards the task.
	  * @param taskid The Id of the task to forward.
	  * @param newRole The new Group for the task
	  * @param newUser The new user who gets the task.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void forwardTask(A_CmsUser currentUser, int taskid, 
							 A_CmsGroup newRole, A_CmsUser newUser) 
		 throws CmsException;
	 
	 /**
	  * Accept a task from the Cms.
	  * 
	  * @param currentUser The user who accepts the task.	 
	  * @param taskid The Id of the task to accept.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void acceptTask(A_CmsUser currentUser, int taskid)
		 throws CmsException;
	 
	 /**
	  * Set Percentage of a task
	  * 
	  * @param currentUser The user who wants to set the percentage.	 
	  * @param taskid The Id of the task to set the percentage.
	  * @param new percentage value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setPercentage(A_CmsUser currentUser, int taskid, int percentage)
		 throws CmsException;
	 
	 /**
	  * Set a new name for a task
	  * 
	  * @param currentUser The user who wants to set the percentage.	 
	  * @param taskid The Id of the task to set the percentage.
	  * @param name The new name value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setName(A_CmsUser currentUser, int taskId, String name)
		 throws CmsException;
	 
	 /**
	  * Set priority of a task
	  * 
	  * @param currentUser The user who wants to set the percentage.	 
	  * @param taskid The Id of the task to set the percentage.
	  * @param new priority value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setPriority(A_CmsUser currentUser, int taskId, int priority)
		 throws CmsException;
	 
	 /**
	  * Set timeout of a task
	  * 
	  * @param currentUser The user who wants to set the percentage.	 
	  * @param taskid The Id of the task to set the percentage.
	  * @param new timeout value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setTimeout(A_CmsUser currentUser, int taskId, java.sql.Timestamp timeout)
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
	 
	 
	 /**
	  * write a task .
	  * 
	  * @param task The task object to write.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public A_CmsTask writeTask(A_CmsTask task)
		 throws CmsException;
	 
	 //--------------------------------------------
	 // Task lists
	 //----------------------------------------------
	 
	 /**
	  * Reads all tasks for a project.
	  * 
	  * @param project The Project in which the tasks are defined. Can be null for all tasks
	  * @param tasktype Type of the tasks you want to read
	  * @param orderBy Chooses, how to order the tasks. 
	  * @param sort Sort order C_TASK_ASC, C_TASK_DESC, or null
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasks(A_CmsProject project, int tasktype, 
							 String orderBy, String sort)
		 throws CmsException;
	 
	 /**
	  * Reads all tasks for a role in a project.
	  * 
	  * @param project The Project in which the tasks are defined.
	  * @param user The user who has to process the task.
	  * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
	  * @param orderBy Chooses, how to order the tasks.
	  * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasks(A_CmsProject project, A_CmsGroup role, int tasktype, 
							 String orderBy, String sort) 
		 throws CmsException;
	 
	 /**
	  * Reads all tasks for a user in a project.
	  * 
	  * @param project The Project in which the tasks are defined.
	  * @param user The user who has to process the task.
	  * @param orderBy Chooses, how to order the tasks.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasks(A_CmsProject project, A_CmsUser user, int tasktype, 
							 String orderBy, String sort) 
		 throws CmsException;
	 
	 /**
	  * Reads all given tasks of a project.
	  * 
	  * @param project The Project in which the tasks are defined.
	  * @param owner Owner of the task.
	  * @param tasktype
	  * @param orderBy Chooses, how to order the tasks.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readGivenTasks(A_CmsProject project, A_CmsUser owner, int taskType, 
								  String orderBy, String sort) 
		 throws CmsException;

	 /**
	  * Writes a new user tasklog for a task.
	  * 
	  * @param taskid The Id of the task .
	  * @param user User who added the Log
	  * @param comment Description for the log
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void writeTaskLog(int taskid, A_CmsUser user, String comment)
		 throws CmsException;
	 
	 /**
	  * Reads log entries for a task.
	  * 
	  * @param taskid The task for the tasklog to read .
	  * @return A Vector of new TaskLog objects 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTaskLogs(int taskid)
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
	  * @param taskid The Id of the task.
	  * @param parname Name of the parameter.
	  * @param parvalue Value if the parameter.
	  * 
	  * @return The id of the inserted parameter or 0 if the parameter already exists for this task.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public int setTaskPar(int taskid, String parname, String parvalue)
		 throws CmsException;

	 /**
	  * Get a parameter value for a task.
	  * 
	  * @param taskid The Id of the task.
	  * @param parname Name of the parameter.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public String getTaskPar(int taskid, String parname)
		 throws CmsException;

	 
	 /**
	 * Get the template task id fo a given taskname.
	 * 
	 * @param taskName Name of the Task
	 * 
	 * @return id from the task template
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public int getTaskType(String taskName)
		throws CmsException;


	 /**
	  * Writes a new user tasklog for a task.
	  * 
	  * @param taskid The Id of the task .
	  * @param user User who added the Log
	  * @param comment Description for the log
	  * @param tasktype Type of the tasklog. User tasktypes must be greater then 100.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void writeTaskLog(int taskid, A_CmsUser user, String comment, int taskType)
		 throws CmsException;
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsAccessTask.java,v $
 * Date   : $Date: 2000/02/15 17:44:00 $
 * Version: $Revision: 1.4 $
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
 * This interface describes the access to projects in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.4 $ $Date: 2000/02/15 17:44:00 $
 */
interface I_CmsAccessTask {

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
								String taskname, java.sql.Timestamp timeout, int priority)
		
		throws CmsException;
	
	
	
	/**
	 * Updates a task.
	 * 
	 * @param task The task that will be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsTask writeTask(A_CmsTask task)
		throws CmsException;
	
	/**
	 * Reads a task from the Cms.
	 * 
	 * @param id The id of the task to read.
	 * 
	 * @return a task object or null if the task is not found.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public A_CmsTask readTask(int id)
		throws CmsException;

	/**
	 * Ends a task from the Cms.
	 * 
	 * @param task The task to end.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void endTask(A_CmsTask task)
		throws CmsException;
	
	/**
	 * Ends a task from the Cms.
	 * 
	 * @param taskid Id of the task to end.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void endTask(int taskid)
		throws CmsException;
	
	
	/**
	 * Forwards a task to another user.
	 * 
	 * @param task The task that will be fowarded.
	 * @param newRole The new Group the task belongs to.
	 * @param newUser User who gets the task.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void forwardTask(A_CmsTask task, A_CmsGroup newRole, A_CmsUser newUser)
		throws CmsException;
	
	/**
	 * Forwards a task to another user.
	 * 
	 * @param taskid The id of the task that will be fowarded.
	 * @param newRole The new Group the task belongs to.
	 * @param newUser User who gets the task
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void forwardTask(int taskid, A_CmsGroup newRole, A_CmsUser newUser)
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
	 * Writes a new system log for a task.
	 * 
	 * @param taskid The Id of the task .
	 * @param comment Description for the log
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void writeSytemTaskLog(int taskid, String comment)
		throws CmsException;
	
		/**
	 * Writes a new user tasklog for a task.
	 * 
	 * @param taskid The Id of the task .
	 * @param user User who added the Log
	 * @param comment Description for the log
	 * @param type Type of the task log 0=System, 1=User, greater then 100= Userdefined
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void writeTaskLog(int taskid, A_CmsUser user, String comment, int type)
		throws CmsException;
	
	/**
	 * Reads log entries for a task.
	 * 
	 * @param taskid The id of the task for the tasklog to read .
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
	 * @param task The task.
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
	 * @param task The task.
	 * @param parname Name of the parameter.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public String getTaskPar(int task, String parname)
		throws CmsException;
}

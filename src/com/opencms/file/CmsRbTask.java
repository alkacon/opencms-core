/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsRbTask.java,v $
 * Date   : $Date: 2000/02/15 17:43:59 $
 * Version: $Revision: 1.5 $
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
 * This class describes a resource broker for projects in the Cms.<BR/>
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Rüdiger Gutfleisch
 * @version $Revision: 1.5 $ $Date: 2000/02/15 17:43:59 $
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
		 throws CmsException {
		 
		 A_CmsTask task = m_accessTask.createTask(project, currentUser, agent, role, 
												  taskname, timeout, priority);
		 
		 if(!taskcomment.equals("")) {
			m_accessTask.writeTaskLog(task.getId(), currentUser, taskcomment);
		 }
										   
		 return task;
	 }

	 /**
	  * Ends a task from the Cms.
	  * 
	  * @param currentUser User who wants to end the task
	  * @param taskid The ID of the task to end.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void endTask(A_CmsUser currentUser, int taskid) 
		 throws CmsException {
		 
		 m_accessTask.endTask(taskid);
		 m_accessTask.writeSytemTaskLog(taskid, "Task finished by " + 
											  currentUser.getFirstname() + " " +
											  currentUser.getLastname() + ".");
	 }	
	 
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
		 throws CmsException{
		 
		 m_accessTask.forwardTask(taskid, newRole, newUser);
		 m_accessTask.writeSytemTaskLog(taskid, "Task fowarded from " + 	
											  currentUser.getFirstname() + " " +
											  currentUser.getLastname() + " to " + 
											  newUser.getFirstname() + " " +
											  newUser.getLastname() + ".");
	 }
	 
	 /**
	  * Accept a task from the Cms.
	  * 
	  * @param currentUser The user who accepts the task.	 
	  * @param taskid The Id of the task to accept.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void acceptTask(A_CmsUser currentUser, int taskId)
		 throws CmsException {
		 
		 A_CmsTask task = readTask(taskId);
		 task.setPercentage(1);
		 task = m_accessTask.writeTask(task);
		 m_accessTask.writeSytemTaskLog(taskId, 
										"Task was accepted from " + 					
										currentUser.getFirstname() + " " +
										currentUser.getLastname() + ".");
	 }
	 
	 /**
	  * Set Percentage of a task
	  * 
	  * @param currentUser The user who wants to set the percentage.	 
	  * @param taskid The Id of the task to set the percentage.
	  * @param new percentage value
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public void setPercentage(A_CmsUser currentUser, int taskId, int percentage)
		 throws CmsException {
		 
		 A_CmsTask task = readTask(taskId);
		 task.setPercentage(percentage);
		 task = m_accessTask.writeTask(task);
		 m_accessTask.writeSytemTaskLog(taskId, 
										"Percentage was set to " + percentage + "% from " + 
										currentUser.getFirstname() + " " + 
										currentUser.getLastname() + ".");
	 }
	 
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
		 throws CmsException {
		 
		 A_CmsTask task = readTask(taskId);
		 task.setPriority(priority);
		 task = m_accessTask.writeTask(task);
		 m_accessTask.writeSytemTaskLog(taskId, 
										"Priority was set to " + priority + " from " + 
										currentUser.getFirstname() + " " + 
										currentUser.getLastname() + ".");
	 }
	 
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
		 throws CmsException {
		 
		 A_CmsTask task = readTask(taskId);
		 task.setTimeOut(timeout);
		 task = m_accessTask.writeTask(task);
		 m_accessTask.writeSytemTaskLog(taskId, 
										"Timeout was set to " + timeout + " from " + 
										currentUser.getFirstname() + " " + 
										currentUser.getLastname() + ".");
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
	 
	 /**
	  * write a task .
	  * 
	  * @param task The task object to write.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public A_CmsTask writeTask(A_CmsTask task)
		 throws CmsException{
		 return( m_accessTask.writeTask(task));
	 }
	 
	 //--------------------------------------------
	 // Task lists
	 //----------------------------------------------
	 
	 /**
	  * Reads all tasks for a project.
	  * 
	  * @param project The Project in which the tasks are defined. Can be null for all tasks
	  * @tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW
	  * @param orderBy Chooses, how to order the tasks. 
	  * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasks(A_CmsProject project, int tasktype, 
							 String orderBy, String sort)
		 throws CmsException{
		 
		 return m_accessTask.readTasks(project, null, null, null, tasktype, 
									   orderBy, sort);
	 }
	 
	 /**
	  * Reads all tasks for a user in a project.
	  * 
	  * @param project The Project in which the tasks are defined.
	  * @param role The user who has to process the task.
	  * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
	  * @param orderBy Chooses, how to order the tasks.
	  * @param sort Sort order C_SORT_ASC, C_SORT_DESC, or null
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTasks(A_CmsProject project, A_CmsUser user, int tasktype, 
							 String orderBy, String sort) 
		 throws CmsException{
		 
		 return m_accessTask.readTasks(project, user, null, null, tasktype, 
									   orderBy, sort);
	 }
	 
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
		 throws CmsException{
		 
		 return m_accessTask.readTasks(project, null, null, role, tasktype, 
									   orderBy, sort);
	 }
	 
	 /**
	  * Reads all given tasks from a user for a project.
	  * 
	  * @param project The Project in which the tasks are defined.
	  * @param owner Owner of the task.
	  * @param tasktype Task type you want to read: C_TASKS_ALL, C_TASKS_OPEN, C_TASKS_DONE, C_TASKS_NEW.
	  * @param orderBy Chooses, how to order the tasks.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readGivenTasks(A_CmsProject project, A_CmsUser owner, int taskType, 
								  String orderBy, String sort) 
		 throws CmsException{
		 
		 return m_accessTask.readTasks(project, null, owner, null, taskType, orderBy, sort);
	 }

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
		 throws CmsException {
		 
		 m_accessTask.writeTaskLog(taskid, user, comment);
	 }
	 
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
		 throws CmsException {
		 
		 m_accessTask.writeTaskLog(taskid, user, comment, taskType);
	 }
	 
	 /**
	  * Reads log entries for a task.
	  * 
	  * @param taskid The task for the tasklog to read .
	  * @return A Vector of new TaskLog objects 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readTaskLogs(int taskid)
		 throws CmsException{
		 return m_accessTask.readTaskLogs(taskid);	 
	 }
	 
	 /**
	  * Reads log entries for a project.
	  * 
	  * @param project The projec for tasklog to read.
	  * @return A Vector of new TaskLog objects 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public Vector readProjectLogs(A_CmsProject project)
		 throws CmsException {
		 return m_accessTask.readProjectLogs(project);	 
	 }
	 
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
		 throws CmsException {
		 
		 // TODO security for calling user
		 return( m_accessTask.setTaskPar(taskid, parname, parvalue));		
	 }

	 /**
	  * Get a parameter value for a task.
	  * 
	  * @param taskid The Id of the task.
	  * @param parname Name of the parameter.
	  * 
	  * @exception CmsException Throws CmsException if something goes wrong.
	  */
	 public String getTaskPar(int taskid, String parname)
		 throws CmsException {
		 
		 return( m_accessTask.getTaskPar(taskid, parname));		
	 }
 }
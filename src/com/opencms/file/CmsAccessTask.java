/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsAccessTask.java,v $
 * Date   : $Date: 2000/06/05 13:37:53 $
 * Version: $Revision: 1.14 $
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
import java.io.*;
import java.sql.*;

import com.opencms.core.*;
import com.opencms.util.*;

/**
 * This class contains the methods to read, write and delete CmsTask 
 * objects in a MySql user database.
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Ruediger Gutfleisch
 * @author Michael Emmerich
 * @version $Revision: 1.14 $ $Date: 2000/06/05 13:37:53 $
 */
class CmsAccessTask implements I_CmsAccessTask, I_CmsConstants  {
	
	/** Table name for GlobeTask */
	private static final String C_TABLE_TASK     = "GlobeTask";
	
	/** Table name for GlobeTaskLog*/
	private static final String C_TABLE_TASKLOG  = "GlobeTaskLog";
	
	/** Table name for GlobeTaskType*/
	private static final String C_TABLE_TASKTYPE = "GlobeTaskType";
	
	/** Table name for GlobeTaskPar*/
	private static final String C_TABLE_TASKPAR  = "GlobeTaskPar";
	
	/** Column name of table GlobeTask */
	private static final String C_ID				  = "id";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_ID			  = "id";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_NAME			  = "name";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_STATE		  = "state";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_ROOT			  = "root";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_PARENT		  = "parent";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_TASKTYPE		  = "tasktyperef"; 
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_INITIATORUSER  = "initiatoruserref";
	/** Column name of table GlobeTask */
	private static final String C_TASK_ROLE			  = "roleref";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_AGENTUSER	  = "agentuserref";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_ORIGINALUSER   = "originaluserref";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_STARTTIME	  = "starttime";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_WAKEUPTIME	  = "wakeuptime";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_TIMEOUT		  = "timeout";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_ENDTIME		  = "endtime";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_PERCENTAGE	  = "percentage";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_PERMISSION	  = "permission";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_PRIORITY		  = "priorityref"; 
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_ESCALATIONTYPE = "escalationtyperef";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_HTMLLINK		  = "htmllink"; 
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_MILESTONE	  = "milestoneref";
	
	/** Column name of table GlobeTask */
	private static final String C_TASK_AUTOFINISH	  = "autofinish";
	
	/** Column name of table GlobeTaskLog */
	private static final String C_LOG_ID         = C_TABLE_TASKLOG + ".id";
	
	/** Column name of table GlobeTaskLog */
	private static final String C_LOG_COMMENT    = C_TABLE_TASKLOG + ".comment"; 
	
	/** Column name of table GlobeTaskLog */
	private static final String C_LOG_EXUSERNAME = C_TABLE_TASKLOG + ".externalusername"; 
	
	/** Column name of table GlobeTaskLog */
	private static final String C_LOG_STARTTIME  = C_TABLE_TASKLOG + ".starttime"; 
	
	/** Column name of table GlobeTaskLog */
	private static final String C_LOG_TASK		 = C_TABLE_TASKLOG + ".taskref";
	
	/** Column name of table GlobeTaskLog */
	private static final String C_LOG_USER		 = C_TABLE_TASKLOG + ".userref";
	
	/** Column name of table GlobeTaskLog */
	private static final String C_LOG_TYPE       = C_TABLE_TASKLOG + ".type";
	
	/** Column names table GlobeTaskPar */
	private static final String C_PAR_ID      = "id"; 
	
	/** Column names table GlobeTaskPar */
	private static final String C_PAR_NAME    = "parname"; 
	
	/** Column names table GlobeTaskPar */
	private static final String C_PAR_VALUE   = "parvalue";
	
	/** Column names table GlobeTaskPar */
	private static final String C_PAR_TASK	  = "ref"; 
	
	/**
	 * SQL Command for getting the last insert id.
	 */   
	private static final String C_GET_LAST_INSERT_ID = "SELECT LAST_INSERT_ID() AS id";
	
	private static final String C_TASK_TYPE_FIELDS = "autofinish, escalationtyperef, htmllink, name, permission, priorityref, roleref";
	/**
	 * SQL Command for creating a new task by copying a task type.
	 */
	private static final String C_TASK_TYPE_COPY = "INSERT INTO " +  C_TABLE_TASK + "  (" + C_TASK_TYPE_FIELDS + ") " +
												   "SELECT " + C_TASK_TYPE_FIELDS + 
												   " FROM  " + C_TABLE_TASKTYPE + "  WHERE id=?";
	/**
	 * SQL Command for updating tasks.
	 */
	private static final String C_TASK_UPDATE ="UPDATE  " + C_TABLE_TASK + "  SET " +
											   "name=?, " +
											   "state=?, " +
											   "tasktyperef=?, " + 
											   "root=?, " +
											   "parent=?, " +
											   "initiatoruserref=?, " +
											   "roleref=?,  " +											   
											   "agentuserref=?, " +
											   "originaluserref=?, " +
											   "starttime=?, " +
											   "wakeuptime=?, " +
											   "timeout=?, " +
											   "endtime=?, " +
											   "percentage=? , " +
											   "permission=? , " +
											   "priorityref=?, " +
											   "escalationtyperef=?, " +
											   "htmllink=?, " +
											   "milestoneref=?, " + 
											   "autofinish=? " +
											   " WHERE id=?";

	/**
	 * SQL Command for reading tasks by id.
	 */
	private static final String C_TASK_READ = "SELECT * FROM  " + C_TABLE_TASK + "  WHERE id=?";

	/**
	 * SQL Command to end a task.
	 */
	private static final String C_TASK_END = "UPDATE  " + C_TABLE_TASK + "  Set " +
											 "state=" + C_TASK_STATE_ENDED + ", " +
											 "percentage=?, " +
											 "endtime=? " +
											 "WHERE id=?";
	
	/**
	 * SQL Command for tasklog insert.
	 */
	private static final String C_TASKLOG_INSERT = "INSERT INTO " + C_TABLE_TASKLOG + " " +
												   "(" + C_LOG_TASK + ", " + C_LOG_USER + ", " + C_LOG_STARTTIME + ", " + C_LOG_COMMENT + ", " + C_LOG_TYPE + ") " +
												   "VALUES (?, ?, ?, ?, ?)";
	
	/**
	 * SQL Command for reading tasklog by id.
	 */
	private static final String C_TASKLOG_READ = "SELECT * FROM " + C_TABLE_TASKLOG + " WHERE " + C_LOG_ID + "=?";
	
	
	/**
	 * This is the connection object to the database
	 */
	private Connection m_Con  = null;

	/**
	 * Constructor, creates a new CmsAccessTask object and connects it to the
	 * user information database.
	 *
	 * @param driver Name of the mySQL JDBC driver.
	 * @param conUrl The connection string to the database.
	 * 
	 * @exception CmsException Throws CmsException if connection fails.
	 * 
	 */
	public CmsAccessTask(String driver, String conUrl)	
		throws CmsException, ClassNotFoundException {
		Class.forName(driver);
		initConnections(conUrl);
	}
	
	
	/**
	 * Creates a new project for task handling.
	 * 
	 * @param owner User who creates the project
	 * @param projectname Name of the project
	 * @param projectType Type of the Project
	 * @param role Usergroup for the project
	 * @param taskcomment Description of the task
	 * @param timeout Time when the Project must finished
	 * @param priority Priority for the Project
	 * 
	 * @return The new task project
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsTask createProject(CmsUser owner, String projectname, int projectType, CmsGroup role, java.sql.Timestamp timeout, int priority)
		throws CmsException {
	
		return this.createTask(0, owner, owner, role, projectType,projectname,timeout,priority);		
	}
	
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
	
	public CmsTask createTask(CmsProject project, CmsUser owner, CmsUser agent, CmsGroup role, 
								int taskType, String taskname, java.sql.Timestamp timeout, int priority)
		
		throws CmsException {
		
		return this.createTask(project.getTaskId(), owner, agent, role, 
							   taskType, taskname, timeout,  priority);
	}
	
	/**
	 * Creates a new task.
	 * 
	 * @param project Project to witch the task belongs.
	 * @param owner User who hast created the task 
	 * @param agent User who will edit the task 
	 * @param role Usergroup for the task
	 * @param taskname Name of the task
	 * @param timeout Time when the task must finished
	 * @param priority Id for the priority
	 * 
	 * @return A new Task Object
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	
	public CmsTask createTask(int projectid, CmsUser owner, CmsUser agent, CmsGroup role, 
								int taskType, String taskname, java.sql.Timestamp timeout, int priority)
		
		throws CmsException {
		
		int ownerId = C_UNKNOWN_ID;
		int agentId = C_UNKNOWN_ID;
		int roleId = C_UNKNOWN_ID;
		
		if(owner!=null)
		{
			ownerId = owner.getId();
		}
		
		if(agent!=null)
		{
			agentId = agent.getId();
		}
		
		if(role!=null)
		{
			roleId = role.getId();
		}
		
		return this.createTask(projectid ,projectid, taskType, 
							   ownerId, agentId, roleId, 
							   taskname, 
							   new java.sql.Timestamp(System.currentTimeMillis()), timeout, 
							   priority);
	}
		
	/**
	 * Creates a new task.
	 * 
	 * @return The id  of the generated Task Object
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	
	private CmsTask createTask(int rootId, int parentId, int tasktype, 
						   int ownerId, int agentId,int  roleId, String taskname, 
						   java.sql.Timestamp wakeuptime, java.sql.Timestamp timeout, 
						   int priority) 
		throws CmsException {
		int id = C_UNKNOWN_ID;
		CmsTask task = null;
		
		try {
			PreparedStatement statementCreateTask = m_Con.prepareStatement(C_TASK_TYPE_COPY);
			// create task by copying from tasktype table
			statementCreateTask.setInt(1,tasktype);
			//			System.out.println(statementCreateTask);
			statementCreateTask.executeUpdate();
			
			// get insert Id
			id = this.getLastInsertId();
			task = this.readTask(id);
			task.setRoot(rootId);
			task.setParent(parentId);
			
			task.setName(taskname);
			task.setTaskType(tasktype);
			task.setRole(roleId);
			if(agentId==C_UNKNOWN_ID){
				agentId = findAgent(roleId);
			}	
			task.setAgentUser(agentId);				 
			task.setOriginalUser(agentId);
			task.setWakeupTime(wakeuptime);
			task.setTimeOut(timeout);
			task.setPriority(priority);
			task.setPercentage(0);
			task.setState(C_TASK_STATE_STARTED);
			task.setInitiatorUser(ownerId);
			task.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
			task.setMilestone(0);
			task = this.writeTask(task);
		} catch( SQLException exc ) {
			System.err.println(exc.getMessage());
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		}
		return task;
	}
	
	/**
	 * Updates a task.
	 * 
	 * @param task The task that will be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsTask writeTask(CmsTask task)
		throws CmsException {
		try {    
			PreparedStatement statementUpdateTask = m_Con.prepareStatement(C_TASK_UPDATE);
			statementUpdateTask.setString(1,task.getName());
			statementUpdateTask.setInt(2,task.getState());
			statementUpdateTask.setInt(3,task.getTaskType());
			statementUpdateTask.setInt(4,task.getRoot());
			statementUpdateTask.setInt(5,task.getParent());
			statementUpdateTask.setInt(6,task.getInitiatorUser());
			statementUpdateTask.setInt(7,task.getRole());
			statementUpdateTask.setInt(8,task.getAgentUser());
			statementUpdateTask.setInt(9,task.getOriginalUser());
			statementUpdateTask.setTimestamp(10,task.getStartTime());
			statementUpdateTask.setTimestamp(11,task.getWakeupTime());
			statementUpdateTask.setTimestamp(12,task.getTimeOut());
			statementUpdateTask.setTimestamp(13,task.getEndTime());
			statementUpdateTask.setInt(14,task.getPercentage());
			statementUpdateTask.setString(15,task.getPermission());
			statementUpdateTask.setInt(16,task.getPriority());
			statementUpdateTask.setInt(17,task.getEscalationType());
			statementUpdateTask.setString(18,task.getHtmlLink());
			statementUpdateTask.setInt(19,task.getMilestone());
			statementUpdateTask.setInt(20,task.getAutoFinish());
			statementUpdateTask.setInt(21,task.getId());
			//			System.out.println(statementUpdateTask);
			statementUpdateTask.executeUpdate();

		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		}
		return(readTask(task.getId()));
	}
	
	/**
	 * Reads a task from the Cms.
	 * 
	 * @param id The id of the task to read.
	 * 
	 * @return a task object or null if the task is not found.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsTask readTask(int id)
		throws CmsException {
		ResultSet result;
		
		try {
			PreparedStatement statementReadTask = m_Con.prepareStatement(C_TASK_READ);
			statementReadTask.setInt(1,id);
			result = statementReadTask.executeQuery();
			
			// if resultset exists - return it
			if(result.next()) {
				return new CmsTask(result.getInt(C_TASK_ID),
								   result.getString(C_TASK_NAME),
								   result.getInt(C_TASK_STATE),
								   result.getInt(C_TASK_TASKTYPE),
								   result.getInt(C_TASK_ROOT),
								   result.getInt(C_TASK_PARENT),
								   result.getInt(C_TASK_INITIATORUSER),
								   result.getInt(C_TASK_ROLE),
								   result.getInt(C_TASK_AGENTUSER),
								   result.getInt(C_TASK_ORIGINALUSER),
								   SqlHelper.getTimestamp(result,C_TASK_STARTTIME),
								   SqlHelper.getTimestamp(result,C_TASK_WAKEUPTIME),
								   SqlHelper.getTimestamp(result,C_TASK_TIMEOUT),
								   SqlHelper.getTimestamp(result,C_TASK_ENDTIME),
								   result.getInt(C_TASK_PERCENTAGE),
								   result.getString(C_TASK_PERMISSION),
								   result.getInt(C_TASK_PRIORITY),
								   result.getInt(C_TASK_ESCALATIONTYPE),
								   result.getString(C_TASK_HTMLLINK),
								   result.getInt(C_TASK_MILESTONE),
								   result.getInt(C_TASK_AUTOFINISH));
			} else {
				// task not found!
				return(null);
			}
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
	}

	/**
	 * Ends a task from the Cms.
	 * 
	 * @param task The task to end.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void endTask(CmsTask task)
		throws CmsException {
			endTask(task.getId());
			task.setState(C_TASK_STATE_ENDED);
	}
	
	/**
	 * Ends a task from the Cms.
	 * 
	 * @param taskid Id of the task to end.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void endTask(int taskId)
		throws CmsException {
		try{
			PreparedStatement statementEndTask = m_Con.prepareStatement(C_TASK_END);
			statementEndTask.setInt(1, 100);
			statementEndTask.setTimestamp(2,new java.sql.Timestamp(System.currentTimeMillis()));
			statementEndTask.setInt(3,taskId);
			statementEndTask.executeQuery();
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		}
	}
	
	/**
	 * Forwards a task to another user.
	 * 
	 * @param task The task that will be fowarded.
	 * @param newRole The new Group the task belongs to
	 * @param newUser User who gets the task
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void forwardTask(CmsTask task, CmsGroup newRole, CmsUser newUser)
		throws CmsException {
			task.setAgentUser(newUser.getId());
			task.setRole(newRole.getId());
			forwardTask(task.getId(), newRole, newUser);
	}
	
	/**
	 * Forwards a task to another user.
	 * 
	 * @param taskid The id of the task that will be fowarded.
	 * @param newRole The new Group the task belongs to
	 * @param newUser User who gets the task
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void forwardTask(int taskId, CmsGroup newRole, CmsUser newUser)
		throws CmsException {
		try { 
			String sqlstr = "UPDATE "+C_TABLE_TASK+" SET " + 
							C_TASK_ROLE+"=? ," +
							C_TASK_AGENTUSER+"=? "+
							"WHERE "+C_TASK_ID+"=?";
			PreparedStatement statementUpdateTask = m_Con.prepareStatement(sqlstr);
			statementUpdateTask.setInt(1,newRole.getId());
			statementUpdateTask.setInt(2,newUser.getId());
			statementUpdateTask.setInt(3,taskId);
			statementUpdateTask.executeUpdate();
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		}
	}
	
	
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
	public Vector readTasks(CmsProject project, CmsUser agent, CmsUser owner, 
							CmsGroup role, int tasktype, 
							String orderBy, String sort)
		throws CmsException {
		boolean first = true;
		Vector tasks = new Vector(); // vector for the return result
		CmsTask task = null;		 // tmp task for adding to vector
		ResultSet recset = null; 
		
		// create the sql string depending on parameters
		// handle the project for the SQL String
		String sqlstr = "SELECT * FROM " + C_TABLE_TASK+" WHERE ";
		if(project!=null){
			sqlstr = sqlstr + C_TASK_ROOT + "=" + project.getTaskId();
			first = false;
		}
		else
		{
			sqlstr = sqlstr + C_TASK_ROOT + "<>0 AND " + C_TASK_PARENT + "<>0";
			first = false;
		}
		
		// handle the agent for the SQL String
		if(agent!=null){
			if(!first){
				sqlstr = sqlstr + " AND ";
			}
			sqlstr = sqlstr + C_TASK_AGENTUSER + "=" + agent.getId();
			first = false;
		}
		
		// handle the owner for the SQL String
		if(owner!=null){
			if(!first){
				sqlstr = sqlstr + " AND ";
			}
			sqlstr = sqlstr + this.C_TASK_INITIATORUSER + "=" + owner.getId();
			first = false;
		}
		
		// handle the role for the SQL String
		if(role!=null){
			if(!first){
				sqlstr = sqlstr+" AND ";
			}
			sqlstr = sqlstr + C_TASK_ROLE + "=" + role.getId();
			first = false;
		}
		
		sqlstr = sqlstr + getTaskTypeConditon(first, tasktype);
		
		// handel the order and sort parameter for the SQL String
		if(orderBy!=null) {
			if(!orderBy.equals("")) {
				sqlstr = sqlstr + " ORDER BY " + orderBy;
				if(orderBy!=null) {
					if(!orderBy.equals("")) {
						sqlstr = sqlstr + " " + sort;
					}
				}
			}
		}	
		
		try {
			
			Statement statement = m_Con.createStatement();
			
			System.out.println(sqlstr);
			recset = statement.executeQuery(sqlstr);
			
			// if resultset exists - return vector of tasks
			while(recset.next()) {
				task =  new CmsTask(recset.getInt(C_TASK_ID),
									recset.getString(C_TASK_NAME),
									recset.getInt(C_TASK_STATE),
									recset.getInt(C_TASK_TASKTYPE),
									recset.getInt(C_TASK_ROOT),
									recset.getInt(C_TASK_PARENT),
									recset.getInt(C_TASK_INITIATORUSER),
									recset.getInt(C_TASK_ROLE),
									recset.getInt(C_TASK_AGENTUSER),
									recset.getInt(C_TASK_ORIGINALUSER),
									SqlHelper.getTimestamp(recset,C_TASK_STARTTIME),
									SqlHelper.getTimestamp(recset,C_TASK_WAKEUPTIME),
									SqlHelper.getTimestamp(recset,C_TASK_TIMEOUT),
									SqlHelper.getTimestamp(recset,C_TASK_ENDTIME),
									recset.getInt(C_TASK_PERCENTAGE),
									recset.getString(C_TASK_PERMISSION),
									recset.getInt(C_TASK_PRIORITY),
									recset.getInt(C_TASK_ESCALATIONTYPE),
									recset.getString(C_TASK_HTMLLINK),
									recset.getInt(C_TASK_MILESTONE),
									recset.getInt(C_TASK_AUTOFINISH));
				
				
				tasks.addElement(task);
			}
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
		
		return tasks;
	}
	
	
	/**
	 * Finds an agent for a given role (group).
	 * @param roleId The Id for the role (group).
	 * 
	 * @return A vector with the tasks
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	private int findAgent(int roleid)
		throws CmsException {
		
		int result = C_UNKNOWN_ID;
		String sqlstr;
		Statement statement = null;
		ResultSet recset = null; 
		
		try {
			
			sqlstr = "SELECT uai.USER_ID as userid, usr.USER_NAME AS login, uai.USER_EMAIL AS email " +
					 "FROM CMS_GROUPUSERS, CMS_USERS usr, CMS_USERS_ADDITIONALINFO uai " +
					 "WHERE GROUP_ID =" + roleid + " AND CMS_GROUPUSERS.USER_ID =usr.USER_ID AND CMS_GROUPUSERS.USER_ID =uai.USER_ID " +
					 "ORDER BY USER_LASTUSED ASC";
			
			statement = m_Con.createStatement();
			System.out.println(sqlstr);
			recset = statement.executeQuery(sqlstr);

			if(recset.next()) {
				result = recset.getInt("userid");
			} else {
				System.out.println("No User for role "+ roleid + " found");
			}
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			  throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);		  
		}
		return result;
	}
	
	/**
	 * Writes new log for a task.
	 * 
	 * @param taskid The id of the task.
	 * @param user User who added the Log.
	 * @param starttime Time when the log is created.
	 * @param comment Description for the log.
	 * @param type Type of the log. 0 = Sytem log, 1 = User Log
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	private void writeTaskLog(int taskId, CmsUser user, 
							  java.sql.Timestamp starttime, String comment, int type)
		throws CmsException {
		try {
			PreparedStatement statementInsertTaskLog = m_Con.prepareStatement(C_TASKLOG_INSERT);
			statementInsertTaskLog.setInt(1, taskId);
			if(user!=null){
				statementInsertTaskLog.setInt(2, user.getId());
			}
			else {
				// no user is specified so set to system user
				// is only valid for system task log
				statementInsertTaskLog.setInt(2, 1);
			}
			statementInsertTaskLog.setTimestamp(3, starttime);
			statementInsertTaskLog.setString(4, comment);
			statementInsertTaskLog.setInt(5, type);
			
			// System.out.println(statementInsertTaskLog);
			statementInsertTaskLog.executeUpdate();
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		}
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
	public void writeTaskLog(int taskid, CmsUser user, String comment)
		throws CmsException {
		
		this.writeTaskLog(taskid, user, 
						  new java.sql.Timestamp(System.currentTimeMillis()), 
						  comment, C_TASKLOG_USER);
	}
	
	/**
	 * Writes a new system log for a task.
	 * 
	 * @param taskid The Id of the task .
	 * @param comment Description for the log
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void writeSytemTaskLog(int taskId, String comment)
		throws CmsException {
		this.writeTaskLog(taskId, null, 
						  new java.sql.Timestamp(System.currentTimeMillis()), 
						  comment, C_TASKLOG_SYSTEM);
	}
	
	/**
	 * Writes new log for a task.
	 * 
	 * @param taskid The id of the task.
	 * @param user User who added the Log.
	 * @param comment Description for the log.
	 * @param type Type of the log. 0 = Sytem log, 1 = User Log, greater then 100 is userdefined.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void writeTaskLog(int taskId, CmsUser user, String comment, int type)
		throws CmsException {
			this.writeTaskLog(taskId, user, 
						  new java.sql.Timestamp(System.currentTimeMillis()), 
						  comment, type);
	}
	
	/**
	 * Reads a log for a task.
	 * 
	 * @param id The id for the tasklog .
	 * @return A new TaskLog object 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	private CmsTaskLog readTaskLog(int id)
		throws CmsException {
		ResultSet result;
		
		try {
			PreparedStatement statementReadTaskLog = m_Con.prepareStatement(C_TASKLOG_READ);
			statementReadTaskLog.setInt(1, id);
			result = statementReadTaskLog.executeQuery();
			if(result.next()) {				 
				return new CmsTaskLog(result.getInt(C_LOG_ID),
									  result.getString(C_LOG_COMMENT), 
									  result.getInt(C_LOG_TASK),
									  result.getInt(C_LOG_USER),
									  SqlHelper.getTimestamp(result,C_LOG_STARTTIME),
									  result.getInt(C_LOG_TYPE));
			} else {
				// tasklog not found!
				return(null);
			}
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
	}
	
	/**
	 * Reads log entries for a task.
	 * 
	 * @param taskid The id of the task for the tasklog to read .
	 * @return A Vector of new TaskLog objects 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readTaskLogs(int taskId)
		throws CmsException {
		ResultSet recset;
		CmsTaskLog log = null;
		Vector logs = new Vector();
		
		try {
			String sqlstr = "SELECT * FROM "+C_TABLE_TASKLOG + 
							" WHERE " + C_LOG_TASK + "=? "+ 
							" ORDER BY " + C_LOG_STARTTIME;
			
			PreparedStatement statementReadTaskLogs = m_Con.prepareStatement(sqlstr);
			statementReadTaskLogs.setInt(1, taskId);
			recset = statementReadTaskLogs.executeQuery();
			while(recset.next()) {				 
				log = new CmsTaskLog(recset.getInt(C_LOG_ID),
									 recset.getString(C_LOG_COMMENT), 
									 recset.getInt(C_LOG_TASK),
									 recset.getInt(C_LOG_USER),
									 SqlHelper.getTimestamp(recset,C_LOG_STARTTIME),
									 recset.getInt(C_LOG_TYPE));
				
				logs.addElement(log);
			}
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
		return logs;
	}
	
	/**
	 * Reads log entries for a project.
	 * 
	 * @param project The projec for tasklog to read.
	 * @return A Vector of new TaskLog objects 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readProjectLogs(CmsProject project)
		throws CmsException {
		ResultSet recset;
		CmsTaskLog log = null;
		Vector logs = new Vector();
		
		try {
			String sqlstr = "SELECT " + C_LOG_ID + ","+C_LOG_COMMENT+","+C_LOG_TASK+","+C_LOG_USER+"," + C_LOG_STARTTIME + "," + C_LOG_TYPE + " " +
							"FROM " + C_TABLE_TASKLOG + ", " + C_TABLE_TASK + " " +
							"WHERE " + C_LOG_TASK + "=GlobeTask.id AND GlobeTask.root=? " + 
							"ORDER BY " + C_LOG_STARTTIME;
			
			PreparedStatement statementReadTaskLogs = m_Con.prepareStatement(sqlstr);
			statementReadTaskLogs.setInt(1, project.getTaskId());
			System.out.println(statementReadTaskLogs);
			recset = statementReadTaskLogs.executeQuery();
			while(recset.next()) {				 
				log = new CmsTaskLog(recset.getInt(C_LOG_ID),
									 recset.getString(C_LOG_COMMENT), 
									 recset.getInt(C_LOG_TASK),
									 recset.getInt(C_LOG_USER),
									 SqlHelper.getTimestamp(recset,C_LOG_STARTTIME),
									 recset.getInt(C_LOG_TYPE));
				logs.addElement(log);
			}	 
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		} catch( Exception exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
		return logs;
	}
	
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
	public int setTaskPar(int taskId, String parname, String parvalue)
		throws CmsException {
		
		ResultSet recset;
		int result=0;
		
		try {
			// test if the parameter already exists for this task
			String sqlstr = "SELECT * FROM " + C_TABLE_TASKPAR + " WHERE "+C_PAR_TASK+"=? AND " + C_PAR_NAME + "=?";
			PreparedStatement statementTaskPar = m_Con.prepareStatement(sqlstr);
			statementTaskPar.setInt(1, taskId);
			statementTaskPar.setString(2, parname);
			//System.out.println(statementTaskPar);
			recset = statementTaskPar.executeQuery();
			
			if(recset.next()) {
				//Parameter exisits, so make an update
				int parid = recset.getInt(C_PAR_ID);
				sqlstr = "UPDATE " + C_TABLE_TASKPAR + " SET " + C_PAR_VALUE + "=? WHERE "+C_PAR_ID+"=?";
				statementTaskPar = m_Con.prepareStatement(sqlstr);
				statementTaskPar.setString(1, parvalue);
				statementTaskPar.setInt(2, parid);
				//System.out.println(statementTaskPar);
				statementTaskPar.executeUpdate();
			}
			else {
				//Parameter is not exisiting, so make an insert
				sqlstr = "INSERT INTO " + C_TABLE_TASKPAR + "("+C_PAR_TASK+", " + C_PAR_NAME + ", " + C_PAR_VALUE + ") VALUES (?,?,?)";
				statementTaskPar = m_Con.prepareStatement(sqlstr);
				statementTaskPar.setInt(1, taskId);
				statementTaskPar.setString(2, parname);
				statementTaskPar.setString(3, parvalue);
				//System.out.println(statementTaskPar);
				statementTaskPar.executeUpdate();
				result = getLastInsertId();
			}
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		}
		return result;
	}

	/**
	 * Get a parameter value for a task.
	 * 
	 * @param task The task.
	 * @param parname Name of the parameter.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public String getTaskPar(int taskId, String parname)
		throws CmsException {
		
		String result = null;
		ResultSet recset = null;
		try {	
			String sqlstr = "SELECT * FROM " + C_TABLE_TASKPAR + " WHERE " + C_PAR_TASK + "=? AND " + C_PAR_NAME + "=?";
			PreparedStatement statementTaskPar = m_Con.prepareStatement(sqlstr);
			statementTaskPar.setInt(1, taskId);
			statementTaskPar.setString(2, parname);
			//System.out.println(statementTaskPar);
			recset = statementTaskPar.executeQuery();
			if(recset.next()) {
				result = recset.getString(C_PAR_VALUE);
			}
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		}
		return result;
	}
	
	
	/**
	 * Get the template task id fo a given taskname.
	 * 
	 * @param taskName Name of the TAsk
	 * 
	 * @return id from the task template
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public int getTaskType(String taskName)
		throws CmsException {
		
		int result = 1;
		String sqlstr = " SELECT id FROM GlobeTaskType where name=?";
		PreparedStatement statement = null;
		ResultSet res = null;
			
		try {		
			statement = m_Con.prepareStatement(sqlstr);
			statement.setString(1, taskName);
			res = statement.executeQuery();
			if (res.next()) {
				result = res.getInt("id");
			}
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		}
		return result;
	}

	
	/**
	 * Connects to the property database.
	 * 
	 * @param conUrl The connection string to the database.
	 * 
	 * @exception CmsException Throws CmsException if connection fails.
	 */
	private void initConnections(String conUrl)	
		throws CmsException {
		
		try {
			m_Con = DriverManager.getConnection(conUrl);
		} catch (SQLException e) {
			throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
	}
	
	/**
	 * Get the last insert id for a succesful db insert.
	 *
	 * @return the last insert id
	 *  
	 * @exception CmsException Throws CmsException if connection fails.
	 * 
	 */
	private int getLastInsertId()
		throws CmsException {
		ResultSet res =null;
		int id = C_UNKNOWN_ID;
		
		try {
			PreparedStatement statementGetLastInsertId = m_Con.prepareStatement(C_GET_LAST_INSERT_ID);
			res = statementGetLastInsertId.executeQuery();
            if (res.next()) {
                id = res.getInt(C_ID);
            }
		} catch (SQLException e){
			throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
		return id;
	}
	
	private String getTaskTypeConditon(boolean first, int tasktype) {
		
		String result = "";
		// handle the tasktype for the SQL String
		if(!first){
			result = result+" AND ";
		}
		
		switch(tasktype)
		{
		case C_TASKS_ALL:
			{
				result = result + C_TASK_ROOT + "<>0";			
				break;				
			}
			
		case C_TASKS_OPEN:
			{
				result = result + C_TASK_STATE + "=" + C_TASK_STATE_STARTED;
				break;
			}	
		case C_TASKS_ACTIVE:
			{
				result = result + C_TASK_STATE + "=" + C_TASK_STATE_STARTED + " and ";
				result = result + C_TASK_PERCENTAGE + "!=" + "0 ";
				break;
			}
		case C_TASKS_DONE:
			{
				result = result + C_TASK_STATE + "=" + C_TASK_STATE_ENDED;
				break;					
			}
			
		case C_TASKS_NEW:
			{
				result = result + C_TASK_PERCENTAGE + "=0 AND " + C_TASK_STATE + "=" + C_TASK_STATE_STARTED;
				break;					
			}
			
		default:
			{
				
			}	
		}
		return result;
	}
}
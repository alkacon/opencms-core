package com.opencms.file;


import java.util.*;
import java.io.*;
import java.sql.*;

import com.opencms.core.*;

/**
 * This class contains the methods to read, write and delete CmsTask 
 * objects in a MySql user database.
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Ruediger Gutfleisch
 * @version $Revision: 1.3 $ $Date: 2000/01/28 18:46:41 $
 */
class CmsAccessTask implements I_CmsAccessTask, I_CmsConstants  {
	
	/**
	 * Table names
	 */
	
	private static final String C_TABLE_TASK     = "GlobeTask";
	private static final String C_TABLE_TASKLOG  = "GlobeTaskLog";
	private static final String C_TABLE_TASKTYPE = "GlobeTaskType";
	private static final String C_TABLE_TASKPAR  = "GlobeTaskPar";
	
	/**
	 * Column names table GlobeTask and GlobeTaskType
	 */
	private static final String C_ID				  = "id";
	private static final String C_TASK_ID			  = "id";
	private static final String C_TASK_NAME			  = "name";
	private static final String C_TASK_STATE		  = "state";
	private static final String C_TASK_ROOT			  = "root";
	private static final String C_TASK_PARENT		  = "parent";
	private static final String C_TASK_TASKTYPE		  = "tasktyperef"; 
	private static final String C_TASK_INITIATORUSER  = "initiatoruserref";
	private static final String C_TASK_ROLE			  = "roleref";
	private static final String C_TASK_AGENTUSER	  = "agentuserref";
	private static final String C_TASK_ORIGINALUSER   = "originaluserref";
	private static final String C_TASK_STARTTIME	  = "starttime";
	private static final String C_TASK_WAKEUPTIME	  = "wakeuptime";
	private static final String C_TASK_TIMEOUT		  = "timeout";
	private static final String C_TASK_ENDTIME		  = "endtime";
	private static final String C_TASK_PERCENTAGE	  = "percentage";
	private static final String C_TASK_PERMISSION	  = "permission";
	private static final String C_TASK_PRIORITY		  = "priorityref"; 
	private static final String C_TASK_ESCALATIONTYPE = "escalationtyperef";
	private static final String C_TASK_HTMLLINK		  = "htmllink"; 
	private static final String C_TASK_MILESTONE	  = "milestoneref";
	private static final String C_TASK_AUTOFINISH	  = "autofinish";
	
	/**
	 * Column names table GlobeTaskLog
	 */
	private static final String C_LOG_ID         = "id";
	private static final String C_LOG_COMMENT    = "comment"; 
	private static final String C_LOG_EXUSERNAME = "externalusername"; 
	private static final String C_LOG_STARTTIME  = "starttime"; 
	private static final String C_LOG_TASK		 = "taskref";
	private static final String C_LOG_USER		 = "userref";
	private static final String C_LOG_TYPE       = "type";
	/**
	 * Column names table GlobeTaskLog
	 */
	private static final String C_PAR_ID      = "id"; 
	private static final String C_PAR_NAME    = "parname"; 
	private static final String C_PAR_VALUE   = "parvalue";
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
		throws CmsException {
		
		// Create a dummy root project with task id 0
		CmsProject root = new CmsProject(0,"","",0/*taskid*/,0,0,0);
		
		// Create a dummy parent task just for the id 0
		CmsTask parent = new CmsTask(0,"",0,1,0,0,0,0,0,0,null,null,null,null,0,"",0,0,"",0,0);
		
		CmsTask task = (CmsTask)this.createTask(root, parent, 1, 
												owner, 
												owner, //agent, 
												role, 
												projectname, "", //comment 
												new java.sql.Timestamp(System.currentTimeMillis()), timeout, 
												priority);
		
		return task;
	}
	
	/*
	GlobeStartProject=GlobeStartTask(0, 0, tasktyperef, GlobeGetUserRef(), roleref, projectname, "", wakeuptime, timeout, priorityref)  
	end function
	*/
	
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
		
		throws CmsException {
		
		// Create a dummy parent task withe the project taskid
		CmsTask parent = new CmsTask(project.getTaskId(),"",0,1,0,0,0,0,0,0,null,null,null,null,0,"",0,0,"",0,0);	
		
		return this.createTask(project, parent, 1, 
							   owner, agent, role, 
							   taskname, taskcomment, 
							   new java.sql.Timestamp(System.currentTimeMillis()), timeout, 
							   priority);
	}
	
	/**
	 * Creates a new task.
	 * 
	 * @param project Project to witch the task belongs.
	 * @param parent Parent Task to witch the Task belongs.
	 * @param tasktype Tasktype used to create the new task.
	 * @param owner User who hast created the task 
	 * @param agent User who will edit the task 
	 * @param role User group for the task 
	 * @param taskname Name of the task
	 * @param taskcomment Description of the task
	 * @param wakeuptime Time to activate the task
	 * @param timeout Time when the task must finished
	 * @param priority Id for the priority
	 * 
	 * @return A new Task Object
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	
	public A_CmsTask createTask(A_CmsProject project, A_CmsTask parent, int tasktype, 
								A_CmsUser owner, A_CmsUser agent, A_CmsGroup role, 
								String taskname, String taskcomment, 
								java.sql.Timestamp wakeuptime, java.sql.Timestamp timeout, 
								int priority) 
		throws CmsException {
		int id = C_UNKNOWN_ID;
		A_CmsTask task = null;
		
		try {
			PreparedStatement statementCreateTask = m_Con.prepareStatement(C_TASK_TYPE_COPY);
			// create task by copying from tasktype table
			statementCreateTask.setInt(1,tasktype);
			//			System.out.println(statementCreateTask);
			statementCreateTask.executeUpdate();
			
			// get insert Id
			id = this.getLastInsertId();
			task = this.readTask(id);
			
			// fill additional task fields
			task.setRoot(project.getTaskId());
			task.setParent(parent.getId());
			task.setName(taskname);
			task.setTaskType(tasktype);
			task.setRole(role.getId());
			
			if(agent!=null){
				task.setAgentUser(agent.getId());
			}
			else {
				// Try to find an agent for this role
				agent= findAgent(role);
				if(agent!=null){
					task.setAgentUser(agent.getId());
				}
				else{
					task.setAgentUser(C_UNKNOWN_ID);
				}
			}				 
			task.setOriginalUser(agent.getId());
			task.setWakeupTime(wakeuptime);
			task.setTimeOut(timeout);
			task.setPriority(priority);
			task.setPercentage(0);
			task.setState(C_TASK_STATE_STARTED);
			task.setInitiatorUser(owner.getId());
			task.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
			task.setMilestone(0);
			task = this.writeTask(task);
			
			writeTaskLog(task, owner, taskcomment);

		} catch( SQLException exc ) {
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
	public A_CmsTask writeTask(A_CmsTask task)
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
	public A_CmsTask readTask(int id)
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
								   result.getTimestamp(C_TASK_STARTTIME),
								   result.getTimestamp(C_TASK_WAKEUPTIME),
								   result.getTimestamp(C_TASK_TIMEOUT),
								   result.getTimestamp(C_TASK_ENDTIME),
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
		}
	}

	/**
	 * Ends a task from the Cms.
	 * 
	 * @param user The úser who end th task
	 * @param task The task to end.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void endTask( A_CmsUser user, A_CmsTask task)
		throws CmsException{
		try{
			PreparedStatement statementEndTask = m_Con.prepareStatement(C_TASK_END);
			statementEndTask.setTimestamp(1,new java.sql.Timestamp(System.currentTimeMillis()));
			statementEndTask.setInt(2,task.getId());
			statementEndTask.executeQuery();
			writeSytemTaskLog(task, "Task finished by "+user.getName());
			task.setState(C_TASK_STATE_ENDED);
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		}
	}

	
	/**
	 * Forwards a task to another user.
	 * 
	 * @param task The task that will be fowarded.
	 * @param user User who will get the forwarede task
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void forwardTask(A_CmsTask task, A_CmsUser user)
		throws CmsException {
		try { 
			
			task.setAgentUser(user.getId());
			String sqlstr = "UPDATE "+C_TABLE_TASK+" SET "+C_TASK_AGENTUSER+"=? WHERE "+C_TASK_ID+"=?";
			PreparedStatement statementUpdateTask = m_Con.prepareStatement(sqlstr);
			statementUpdateTask.setInt(1,user.getId());
			statementUpdateTask.setInt(2,task.getId());
			//System.out.println(statementUpdateTask);
			statementUpdateTask.executeUpdate();
			writeSytemTaskLog(task, "Task fowarded to "+user.getName());

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
	public Vector readTasks(A_CmsProject project, A_CmsUser agent, A_CmsUser owner, 
							A_CmsGroup role, int tasktype, 
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
			sqlstr = sqlstr + this.C_TASK_ROLE + "=" + role.getId();
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
			
			//System.out.println(sqlstr);
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
									recset.getTimestamp(C_TASK_STARTTIME),
									recset.getTimestamp(C_TASK_WAKEUPTIME),
									recset.getTimestamp(C_TASK_TIMEOUT),
									recset.getTimestamp(C_TASK_ENDTIME),
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
		}		
		
		return tasks;
	}
	
	private A_CmsUser findAgent(A_CmsGroup role){
		
		// todo to be implemented
		return null;
	}
	
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
	private void writeTaskLog(A_CmsTask task, A_CmsUser user, 
							  java.sql.Timestamp starttime, String comment, int type)
		throws CmsException {
		try {
			PreparedStatement statementInsertTaskLog = m_Con.prepareStatement(C_TASKLOG_INSERT);
			statementInsertTaskLog.setInt(1, task.getId());
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
	 * @param task The task .
	 * @param user User who added the Log
	 * @param comment Description for the log
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public void writeTaskLog(A_CmsTask task, A_CmsUser user, String comment)
		throws CmsException {
		
		this.writeTaskLog(task, user, 
						  new java.sql.Timestamp(System.currentTimeMillis()), 
						  comment, C_TASKLOG_USER);
	}
	
	/**
	 * Writes a new system log for a task.
	 * 
	 * @param task The task .
	 * @param comment Description for the log
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	private void writeSytemTaskLog(A_CmsTask task, String comment)
		throws CmsException {
		this.writeTaskLog(task, null, 
						  new java.sql.Timestamp(System.currentTimeMillis()), 
						  comment, C_TASKLOG_SYSTEM);
	}
	
	/**
	 * Reads a log for a task.
	 * 
	 * @param id The id for the tasklog .
	 * @return A new TaskLog object 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	private A_CmsTaskLog readTaskLog(int id)
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
									  result.getTimestamp(C_LOG_STARTTIME),
									  result.getInt(C_LOG_TYPE));
			} else {
				// tasklog not found!
				return(null);
			}
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		}
	}
	
	/**
	 * Reads log entries for a task.
	 * 
	 * @param task The task for the tasklog to read .
	 * @return A Vector of new TaskLog objects 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Vector readTaskLogs(A_CmsTask task)
		throws CmsException {
		ResultSet recset;
		A_CmsTaskLog log = null;
		Vector logs = new Vector();
		
		try {
			String sqlstr = "SELECT * FROM "+C_TABLE_TASKLOG+" WHERE "+C_LOG_TASK+"=?";
			PreparedStatement statementReadTaskLogs = m_Con.prepareStatement(sqlstr);
			statementReadTaskLogs.setInt(1, task.getId());
			recset = statementReadTaskLogs.executeQuery();
			while(recset.next()) {				 
				log = new CmsTaskLog(recset.getInt(C_LOG_ID),
									 recset.getString(C_LOG_COMMENT), 
									 recset.getInt(C_LOG_TASK),
									 recset.getInt(C_LOG_USER),
									 recset.getTimestamp(C_LOG_STARTTIME),
									 recset.getInt(C_LOG_TYPE));
				
				logs.addElement(log);
			}
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
	public Vector readProjectLogs(A_CmsProject project)
		throws CmsException {
		ResultSet recset;
		A_CmsTaskLog log = null;
		Vector logs = new Vector();
		
		try {
			String sqlstr = "SELECT * FROM "+C_TABLE_TASKLOG+" WHERE "+C_LOG_TASK+"=?";
			PreparedStatement statementReadTaskLogs = m_Con.prepareStatement(sqlstr);
			statementReadTaskLogs.setInt(1, project.getId());
			recset = statementReadTaskLogs.executeQuery();
			while(recset.next()) {				 
				log = new CmsTaskLog(recset.getInt(C_LOG_ID),
									 recset.getString(C_LOG_COMMENT), 
									 recset.getInt(C_LOG_TASK),
									 recset.getInt(C_LOG_USER),
									 recset.getTimestamp(C_LOG_STARTTIME),
									 recset.getInt(C_LOG_TYPE));
				logs.addElement(log);
			}	 
			
		} catch( SQLException exc ) {
			throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
	public int setTaskPar(A_CmsTask task, String parname, String parvalue)
		throws CmsException {
		
		ResultSet recset;
		int result=0;
		
		try {
			// test if the parameter already exists for this task
			String sqlstr = "SELECT * FROM " + C_TABLE_TASKPAR + " WHERE "+C_PAR_TASK+"=? AND " + C_PAR_NAME + "=?";
			PreparedStatement statementTaskPar = m_Con.prepareStatement(sqlstr);
			statementTaskPar.setInt(1, task.getId());
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
				statementTaskPar.setInt(1, task.getId());
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
	public String getTaskPar(A_CmsTask task, String parname)
		throws CmsException {
		
		String result = null;
		ResultSet recset = null;
		try {	
			String sqlstr = "SELECT * FROM " + C_TABLE_TASKPAR + " WHERE " + C_PAR_TASK + "=? AND " + C_PAR_NAME + "=?";
			PreparedStatement statementTaskPar = m_Con.prepareStatement(sqlstr);
			statementTaskPar.setInt(1, task.getId());
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
			id = res.getInt(C_ID);
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
				result = result;			
				break;				
			}
			
		case C_TASKS_OPEN:
			{
				result = result + "state=" + C_TASK_STATE_STARTED;
				break;
			}	
		case C_TASKS_ACTIVE:
			{
				result = result + "state=" + C_TASK_STATE_STARTED;
				break;
			}
		case C_TASKS_DONE:
			{
				result = result + "state=" + C_TASK_STATE_ENDED;
				break;					
			}
			
		case C_TASKS_NEW:
			{
				result = result + C_TASK_PERCENTAGE + "=0 AND state=" + C_TASK_STATE_STARTED;
				break;					
			}
			
		default:
			{
				
			}	
		}
		return result;
	}
}
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
 * @version $Revision: 1.1 $ $Date: 2000/01/11 17:41:33 $
 */
//class CmsAccessTask implements I_CmsAccessTask, I_CmsConstants  {
class CmsAccessTask implements I_CmsConstants  {
     
    /**
     * SQL Command for getting the last insert id.
     */   
	private static final String C_GET_LAST_INSERT_ID = "SELECT LAST_INSERT_ID() AS ID";
	
	private static final String C_TASK_TYPE_FIELDS = "autofinish, escalationtyperef, htmllink, name, permission, priorityref, roleref";
	/**
     * SQL Command for creating a new task by copying a task type.
     */
	private static final String C_TASK_TYPE_COPY = "INSERT INTO GlobeTask (" + C_TASK_TYPE_FIELDS + ") " +
											       "SELECT " + C_TASK_TYPE_FIELDS + 
												   " FROM GlobeTaskType WHERE id=?";
	
//	private static final String C_TASK_TYPE_COPY = "INSERT INTO GlobeTask (name,root) VALUES('testtask',?)";
	
	/**
     * SQL Command for updating tasks.
     */
	private static final String C_TASK_UPDATE ="UPDATE GlobeTask SET " +
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
	private static final String C_TASK_READ = "SELECT * FROM GlobeTask WHERE id=?";

	/**
     * SQL Command to end a task.
     */
	private static final String C_TASK_END = "UPDATE GlobeTask Set " +
											 "state="+A_CmsTask.C_TASK_STATE_ENDED+", " +
											 "endtime=? " +
											 "WHERE id=?";

	/**
     * SQL Command for tasklog insert.
     */
	private static final String C_TASKLOG_INSERT = "INSERT INTO GlobeTaskLog " +
												   "(taskref, userref, starttime, comment, type) " +
												   "VALUES (?, ?, ?, ?, ?)";
	
	/**
     * SQL Command for reading tasklog by id.
     */
	private static final String C_TASKLOG_READ = "SELECT * FROM GlobeTaskLog WHERE id=?";
	
	/**
	 * Column names
	 */
	private static final String C_TASK_ID = "id";
	private static final String C_TASK_NAME = "name";
	private static final String C_TASK_STATE = "state";
	private static final String C_TASK_ROOT = "root";
	private static final String C_TASK_PARENT = "parent";
	private static final String C_TASK_TASKTYPE ="tasktyperef"; 
	private static final String C_TASK_INITIATORUSER = "initiatoruserref";
	private static final String C_TASK_ROLE = "roleref";
	private static final String C_TASK_AGENTUSER = "agentuserref";
	private static final String C_TASK_ORIGINALUSER = "originaluserref";
	private static final String C_TASK_STARTTIME = "starttime";
	private static final String C_TASK_WAKEUPTIME = "wakeuptime";
	private static final String C_TASK_TIMEOUT = "timeout";
	private static final String C_TASK_ENDTIME = "endtime";
	private static final String C_TASK_PERCENTAGE = "percentage";
	private static final String C_TASK_PERMISSION = "permission";
	private static final String C_TASK_PRIORITY = "priorityref"; 
	private static final String C_TASK_ESCALATIONTYPE = "escalationtyperef";
	private static final String C_TASK_HTMLLINK = "htmllink"; 
	private static final String C_TASK_MILESTONE = "milestoneref";
	private static final String C_TASK_AUTOFINISH = "autofinish"; 
	
		   
     /**
     * This is the connection object to the database
     */
    private Connection m_Con  = null;

	/**
    * Prepared SQL Statement for getting the last insert id.
    */
	private PreparedStatement m_statementGetLastInsertId;
		
	/**
    * Prepared SQL Statement for creating a task.
    */
    private PreparedStatement m_statementCreateTask;

    /**
    * Prepared SQL Statement for updating a task.
    */
    private PreparedStatement m_statementUpdateTask;
    
	/**
    * Prepared SQL Statement for reading a task.
    */
    private PreparedStatement m_statementReadTask;
	
	/**
    * Prepared SQL Statement for reading a task.
    */
    private PreparedStatement m_statementEndTask;
	
	/**
    * Prepared SQL Statement for inserting a tasklog.
    */
	private PreparedStatement m_statementInsertTaskLog;
	
	/**
    * Prepared SQL Statement for reading a task.
    */
    private PreparedStatement m_statementReadTaskLog;
	
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
        initStatements();
    }
	
	/**
	 * Creates a new task.
	 * 
	 * @param projectid Project id to witch the task belongs.
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
	
	public A_CmsTask createTask(int projectid, A_CmsUser owner, A_CmsUser agent, A_CmsGroup role, 
								String taskname, String taskcomment, 
								java.sql.Timestamp timeout, int priority)
		
		throws CmsException {
		return this.createTask(projectid, 1, 1, 
							   owner, agent, role, 
							   taskname, taskcomment, 
							   new java.sql.Timestamp(System.currentTimeMillis()), timeout, 
							   priority);
	}
	
	/**
	 * Creates a new task.
	 * 
	 * @param projectid Project id to witch the task belongs.
	 * @param parent Parent Id to witch the Task belongs.
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
	
	 public A_CmsTask createTask(int projectid, int parent, int tasktype, 
								 A_CmsUser owner, A_CmsUser agent, A_CmsGroup role, 
								 String taskname, String taskcomment, 
								 java.sql.Timestamp wakeuptime, java.sql.Timestamp timeout, 
								 int priority) 
		 throws CmsException {
		 int id = C_UNKNOWN_ID;
		 A_CmsTask task = null;
		 
		 try {
			 synchronized(m_statementCreateTask) {
				 
				 // create task by copying from tasktype table
				 m_statementCreateTask.setInt(1,tasktype);
				 m_statementCreateTask.executeUpdate();
				 
				 // get insert Id
				 id = this.getLastInsertId();
				 task = this.readTask(id);
				 
				 // fill additional task fields
				 task.setRoot(projectid);
				 task.setParent(parent);
				 task.setTaskType(tasktype);
				 task.setRole(role.getId());
				 
				 if(agent!=null){
	 				task.setAgentUser(agent.getId());
				 }
				 else{
					 // Try to find an agent for this role
					 agent= FindAgent(role);
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
				 task.setState(A_CmsTask.C_TASK_STATE_STARTED);
				 task.setInitiatorUser(owner.getId());
				 task.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
				 task.setMilestone(0);
				 task = this.writeTask(task);
				 
				 InsertTaskLog(task, owner, taskcomment);
			 }
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
			 synchronized(m_statementUpdateTask) {
					m_statementUpdateTask.setString(1,task.getName());
					m_statementUpdateTask.setInt(2,task.getState());
					m_statementUpdateTask.setInt(3,task.getTaskType());
					m_statementUpdateTask.setInt(4,task.getRoot());
					m_statementUpdateTask.setInt(5,task.getParent());
					m_statementUpdateTask.setInt(6,task.getInitiatorUser());
					m_statementUpdateTask.setInt(7,task.getRole());
					m_statementUpdateTask.setInt(8,task.getAgentUser());
					m_statementUpdateTask.setInt(9,task.getOriginalUser());
					m_statementUpdateTask.setTimestamp(10,task.getStartTime());
					m_statementUpdateTask.setTimestamp(11,task.getWakeupTime());
					m_statementUpdateTask.setTimestamp(12,task.getTimeOut());
					m_statementUpdateTask.setTimestamp(13,task.getEndTime());
					m_statementUpdateTask.setInt(14,task.getPercentage());
					m_statementUpdateTask.setString(15,task.getPermission());
					m_statementUpdateTask.setInt(16,task.getPriority());
					m_statementUpdateTask.setInt(17,task.getEscalationType());
					m_statementUpdateTask.setString(18,task.getHtmlLink());
					m_statementUpdateTask.setInt(19,task.getMilestone());
					m_statementUpdateTask.setInt(20,task.getAutoFinish());
					m_statementUpdateTask.setInt(21,task.getId());
					System.out.println(m_statementUpdateTask);
					m_statementUpdateTask.executeUpdate();
			 }
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
			 synchronized(m_statementReadTask) {
				m_statementReadTask.setInt(1,id);
				result = m_statementReadTask.executeQuery();
			 }
			 
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
	 * @param task The task to end.
	 * @param user The úser who end th task
	 * 
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public void endTask(A_CmsTask task, A_CmsUser user)
		 throws CmsException{
		 try{
			 synchronized(m_statementEndTask) {
				 m_statementEndTask.setTimestamp(1,new java.sql.Timestamp(System.currentTimeMillis()));
				 m_statementEndTask.setInt(2,task.getId());
				 m_statementEndTask.executeQuery();
			 }
			 InsertSytemTaskLog(task, "Task finished by "+user.getName());
		 } catch( SQLException exc ) {
			 throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		 }
	 }
	 
	 private A_CmsUser FindAgent(A_CmsGroup role){
	 
		 // todo to be implemented
		return null;
	 }
	 
	 /**
	 * Create a new log for a task.
	 * 
	 * @param task The task.
	 * @param user User who added the Log.
	 * @param starttime Time when the log is created.
	 * @param comment Description for the log.
	 * @param type Type of the log. 0 = Sytem log, 1 = User Log
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 private void InsertTaskLog(A_CmsTask task, A_CmsUser user, 
									   java.sql.Timestamp starttime, String comment, int type)
		 throws CmsException {
		 try {
			 synchronized(m_statementInsertTaskLog) {
				 m_statementInsertTaskLog.setInt(1, task.getId());
				 if(user!=null){
					m_statementInsertTaskLog.setInt(2, user.getId());
				 }
				 else{
					m_statementInsertTaskLog.setInt(2, 1);
				 }
				 m_statementInsertTaskLog.setTimestamp(3, starttime);
				 m_statementInsertTaskLog.setString(4, comment);
				 m_statementInsertTaskLog.setInt(5, type);
				
				 System.out.println(m_statementInsertTaskLog);
				 m_statementInsertTaskLog.executeUpdate();
			 }
			 
		 } catch( SQLException exc ) {
			 throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		 }
	 }
	
	 /**
	 * Create a new user tasklog for a task.
	 * 
	 * @param task The task .
	 * @param user User who added the Log
	 * @param comment Description for the log
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public void InsertTaskLog(A_CmsTask task, A_CmsUser user, String comment)
		 throws CmsException {
		 
		 this.InsertTaskLog(task, user, 
							new java.sql.Timestamp(System.currentTimeMillis()), 
							comment, 1);
	 }
	 
	 /**
	 * Create a new system log for a task.
	 * 
	 * @param task The task .
	 * @param comment Description for the log
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 private void InsertSytemTaskLog(A_CmsTask task, String comment)
		 throws CmsException {
		 this.InsertTaskLog(task, null, 
							new java.sql.Timestamp(System.currentTimeMillis()), 
							comment, 0);
	 }
	 
	 /**
	 * Reads a log for a task.
	 * 
	 * @param id The id for the tasklog .
	 * @return A new TaskLog object 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsTaskLog ReadTaskLog(int id)
		 throws CmsException {
		 ResultSet result;
		 
		 try {
			 synchronized(m_statementReadTaskLog) {
				 m_statementReadTaskLog.setInt(1, id);
				 result = m_statementReadTaskLog.executeQuery();
				 if(result.next()) {				 
					 return new CmsTaskLog(result.getInt("id"),
											 result.getString("comment"), 
											 result.getInt("task"),
											 result.getInt("user"),
											 result.getTimestamp("starttime"),
											 result.getInt("type"));
				 } else {
					 // tasklog not found!
					 return(null);
				 }
			 }
			 
		 } catch( SQLException exc ) {
			 throw new CmsException(exc.getMessage(), CmsException.C_SQL_ERROR, exc);
		 }
	 }
	 
	/**
     * This method creates all preparted SQL statements required in this class.
     * 
     * @exception CmsException Throws CmsException if something goes wrong.
     */
	private void initStatements()
		throws CmsException{
		try{
			// Init Statement's for the task
			m_statementGetLastInsertId = m_Con.prepareStatement(C_GET_LAST_INSERT_ID);
			m_statementCreateTask = m_Con.prepareStatement(C_TASK_TYPE_COPY);
			m_statementReadTask = m_Con.prepareStatement(C_TASK_READ);
			m_statementUpdateTask = m_Con.prepareStatement(C_TASK_UPDATE);
			m_statementEndTask = m_Con.prepareStatement(C_TASK_END);
	
			// Init Statement's for the tasklog
			m_statementInsertTaskLog = m_Con.prepareStatement(C_TASKLOG_INSERT);
			m_statementReadTaskLog = m_Con.prepareStatement(C_TASKLOG_READ);
		} catch (SQLException e){
			throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
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
		} catch (SQLException e)	{
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
			synchronized(m_statementGetLastInsertId) {
				res = m_statementGetLastInsertId.executeQuery();
				id = res.getInt(C_TASK_ID);
			}
		} catch (SQLException e){
			throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
		
		return id;
	}
}
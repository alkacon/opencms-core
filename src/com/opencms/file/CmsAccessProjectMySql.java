/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsAccessProjectMySql.java,v $
 * Date   : $Date: 2000/06/05 13:37:53 $
 * Version: $Revision: 1.24 $
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
import java.sql.*;

import com.opencms.core.*;
import com.opencms.util.*;

/**
 * This class describes the access to projects in the Cms.<BR/>
 * 
 * This class has package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.24 $ $Date: 2000/06/05 13:37:53 $
 */
class CmsAccessProjectMySql implements I_CmsAccessProject, I_CmsConstants {

    /**
     * This is the connection object to the database
     */
    private Connection m_con  = null;

	/**
	 * Column name
	 */
	private static final String C_USER_ID = "USER_ID";
	
	/**
	 * Column name
	 */
	private static final String C_PROJECT_ID = "PROJECT_ID";
	
	/**
	 * Column name
	 */
	private static final String C_GROUP_ID = "GROUP_ID";
	
	/**
	 * Column name
	 */
	private static final String C_MANAGERGROUP_ID = "MANAGERGROUP_ID";
	
	/**
	 * Column name
	 */
	private static final String C_TASK_ID = "TASK_ID";
	
	/**
	 * Column name
	 */
	private static final String C_PROJECT_NAME = "PROJECT_NAME";
	
	/**
	 * Column name
	 */
	private static final String C_PROJECT_DESCRIPTION = "PROJECT_DESCRIPTION";
	
	/**
	 * Column name
	 */
	private static final String C_PROJECT_FLAGS = "PROJECT_FLAGS";
	
	/**
	 * Column name
	 */
	private static final String C_PROJECT_PUBLISHDATE = "PROJECT_PUBLISHDATE";
	
	/**
	 * Column name
	 */
	private static final String C_PROJECT_CREATEDATE = "PROJECT_CREATEDATE";
	
	/**
	 * Column name
	 */
	private static final String C_PROJECT_PUBLISHED_BY = "PROJECT_PUBLISHED_BY";
	
	/**
	 * Column name
	 */
	private static final String C_PROJECT_COUNT_LOCKED = "PROJECT_COUNT_LOCKED_RESOURCES";
	
	/**
     * SQL Command for creating projects.
     */    
    private static final String C_PROJECT_CREATE = "INSERT INTO " + C_DATABASE_PREFIX + "PROJECTS VALUES(?,?,?,?,?,?,?,?,?,null," + C_UNKNOWN_ID + ",0)";

	/**
     * SQL Command for deleting projects.
     */    
    private static final String C_PROJECT_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "PROJECTS where " + C_PROJECT_ID + " = ?";
	
	/**
     * SQL Command for updating projects.
     */    
    private static final String C_PROJECT_UPDATE = "UPDATE " + C_DATABASE_PREFIX + "PROJECTS set " + 
												   C_USER_ID + " = ?, " +
												   C_GROUP_ID + " = ?, " +
												   C_MANAGERGROUP_ID + " = ?, " +
												   C_TASK_ID + " = ?, " +
												   C_PROJECT_DESCRIPTION + " = ?, " +
												   C_PROJECT_FLAGS + " = ?, " +
												   C_PROJECT_CREATEDATE + " = ?, " +
												   C_PROJECT_PUBLISHDATE + " = ?, " +
												   C_PROJECT_PUBLISHED_BY + " = ?, " +
												   C_PROJECT_COUNT_LOCKED + " = ? " +
												   "where " + C_PROJECT_ID + " = ?";

	/**
     * SQL Command for reading projects.
     */    
    private static final String C_PROJECT_READ = "Select * from " + C_DATABASE_PREFIX + "PROJECTS where " + C_PROJECT_ID + " = ?";
	
	/**
     * SQL Command for reading projects.
     */    
    private static final String C_PROJECT_READ_BY_TASK = "Select * from " + C_DATABASE_PREFIX + "PROJECTS where " + C_TASK_ID + " = ?";
	
	/**
     * SQL Command for reading projects.
     */    
    private static final String C_PROJECT_READ2 = "Select * from " + C_DATABASE_PREFIX + "PROJECTS where " + C_PROJECT_NAME + " = ? and " + C_PROJECT_CREATEDATE + " = ?";
	
	/**
     * SQL Command for reading projects.
     */    
    private static final String C_PROJECT_GET_BY_USER = "Select * from " + C_DATABASE_PREFIX + "PROJECTS where " + 
														C_USER_ID + " = ? and " + 
														C_PROJECT_FLAGS + " = " + 
														C_PROJECT_STATE_UNLOCKED;
	
	/**
     * SQL Command for reading projects.
     */    
    private static final String C_PROJECT_GET_BY_GROUP = "Select * from " + C_DATABASE_PREFIX + "PROJECTS where " + 
														 "( " + C_GROUP_ID + " = ? or " +
														 C_MANAGERGROUP_ID + " = ? ) and " +
														 C_PROJECT_FLAGS + " = " + C_PROJECT_STATE_UNLOCKED;
	
	/**
     * SQL Command for reading projects.
     */    
    private static final String C_PROJECT_GET_BY_MANAGERGROUP = "Select * from " + C_DATABASE_PREFIX + "PROJECTS where " + 
																C_MANAGERGROUP_ID + " = ? and " +
																C_PROJECT_FLAGS + " = " + 
																C_PROJECT_STATE_UNLOCKED;
	
	/**
     * SQL Command for reading projects.
     */    
    private static final String C_PROJECT_GET_BY_FLAG = "Select * from " + C_DATABASE_PREFIX + "PROJECTS where " + 
														 C_PROJECT_FLAGS + " = ?";
	
	/**
     * Constructor, creartes a new CmsAccessProject object and connects it to the
     * project database.
     *
     * @param driver Name of the mySQL JDBC driver.
     * @param conUrl The connection string to the database.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     * 
     */
    CmsAccessProjectMySql(String driver,String conUrl)	
        throws CmsException, ClassNotFoundException {
        Class.forName(driver);
        initConnections(conUrl);
    }

	/**
	 * Reads a project from the Cms.
	 * 
	 * @param id The id of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject readProject(int id)
		 throws CmsException {
		 try {
			 ResultSet result;
			 // create the statement
			 PreparedStatement statementReadProject = 
				m_con.prepareStatement(C_PROJECT_READ);
			 
			 statementReadProject.setInt(1,id);
			 result = statementReadProject.executeQuery();			
			
			 // if resultset exists - return it
			 if(result.next()) {
				 return new CmsProject(result.getInt(C_PROJECT_ID),
									   result.getString(C_PROJECT_NAME),
									   result.getString(C_PROJECT_DESCRIPTION),
									   result.getInt(C_TASK_ID),
									   result.getInt(C_USER_ID),
									   result.getInt(C_GROUP_ID),
									   result.getInt(C_MANAGERGROUP_ID),
									   result.getInt(C_PROJECT_FLAGS),
									   SqlHelper.getTimestamp(result,C_PROJECT_CREATEDATE),
									   SqlHelper.getTimestamp(result,C_PROJECT_PUBLISHDATE),
									   result.getInt(C_PROJECT_PUBLISHED_BY),
									   result.getInt(C_PROJECT_COUNT_LOCKED));
			 } else {
				 // project not found!
				 throw new CmsException("[" + this.getClass().getName() + "] " + id, 
					 CmsException.C_NOT_FOUND);
			 }
		 } catch( Exception exc ) {
			 throw new CmsException( "[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }
	 }
	
	/**
	 * Reads a project from the Cms.
	 * 
	 * @param task The task of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject readProject(CmsTask task)
		 throws CmsException {
		 try {
			 ResultSet result;
			 // create the statement
			 PreparedStatement statementReadProject = 
				m_con.prepareStatement(C_PROJECT_READ_BY_TASK);
			 
			 statementReadProject.setInt(1,task.getId());
			 result = statementReadProject.executeQuery();			
			
			 // if resultset exists - return it
			 if(result.next()) {
				 return new CmsProject(result.getInt(C_PROJECT_ID),
									   result.getString(C_PROJECT_NAME),
									   result.getString(C_PROJECT_DESCRIPTION),
									   result.getInt(C_TASK_ID),
									   result.getInt(C_USER_ID),
									   result.getInt(C_GROUP_ID),
									   result.getInt(C_MANAGERGROUP_ID),
									   result.getInt(C_PROJECT_FLAGS),
									   SqlHelper.getTimestamp(result,C_PROJECT_CREATEDATE),
									   SqlHelper.getTimestamp(result,C_PROJECT_PUBLISHDATE),
									   result.getInt(C_PROJECT_PUBLISHED_BY),
									   result.getInt(C_PROJECT_COUNT_LOCKED));
			 } else {
				 // project not found!
				 throw new CmsException("[" + this.getClass().getName() + "] " + task, 
					 CmsException.C_NOT_FOUND);
			 }
		 } catch( Exception exc ) {
			 throw new CmsException( "[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }
	 }
	 
	/**
	 * Creates a project.
	 * 
	 * @param id The new id of the project. (normaly = C_UNKNOWN_ID)
	 * @param name The name of the project to create.
	 * @param description The description for the new project.
	 * @param task The task.
	 * @param owner The owner of this project.
	 * @param group The group for this project.
	 * @param flags The flags for the project (e.g. archive).
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject createProject(int id, String name, String description, CmsTask task, 
								CmsUser owner, CmsGroup group, 
								CmsGroup managergroup, int flags)
		throws CmsException {
		 try {
			 
			 Timestamp createTime = new Timestamp(new java.util.Date().getTime());
			 
			 // create the statement
			 PreparedStatement statementCreateProject = 
				m_con.prepareStatement(C_PROJECT_CREATE);
			 
			 if(id == C_UNKNOWN_ID) {
				// use the auto-increment
				statementCreateProject.setNull(1,Types.INTEGER);
			 } else {
				// set overgiven id
				statementCreateProject.setInt(1,id);
			 }
			 statementCreateProject.setInt(2,owner.getId());
			 statementCreateProject.setInt(3,group.getId());
			 statementCreateProject.setInt(4,managergroup.getId());
			 statementCreateProject.setInt(5,task.getId());
			 statementCreateProject.setString(6,name);
			 statementCreateProject.setString(7,description);
			 statementCreateProject.setInt(8,flags);
			 statementCreateProject.setTimestamp(9,createTime);
			 statementCreateProject.executeUpdate();
			 
			 // now read the created project
			 PreparedStatement statementReadProject = 
				m_con.prepareStatement(C_PROJECT_READ2);
			 statementReadProject.setString(1,name);
			 statementReadProject.setTimestamp(2,createTime);
			 ResultSet result = statementReadProject.executeQuery();
			 
			 result.next();			
			 return new CmsProject(result.getInt(C_PROJECT_ID),
								   result.getString(C_PROJECT_NAME),
								   result.getString(C_PROJECT_DESCRIPTION),
								   result.getInt(C_TASK_ID),
								   result.getInt(C_USER_ID),
								   result.getInt(C_GROUP_ID),
								   result.getInt(C_MANAGERGROUP_ID),
								   result.getInt(C_PROJECT_FLAGS),
								   SqlHelper.getTimestamp(result,C_PROJECT_CREATEDATE),
								   SqlHelper.getTimestamp(result,C_PROJECT_PUBLISHDATE),
								   result.getInt(C_PROJECT_PUBLISHED_BY),													 
								   result.getInt(C_PROJECT_COUNT_LOCKED));
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }
	 }

	/**
	 * Updates a project.
	 * 
	 * @param project The project that will be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public CmsProject writeProject(CmsProject project)
		 throws CmsException {
		 try {
			 // create the statement
			 PreparedStatement statementUpdateProject = 
				m_con.prepareStatement(C_PROJECT_UPDATE);
			 
			 statementUpdateProject.setInt(1,project.getOwnerId());
			 statementUpdateProject.setInt(2,project.getGroupId());
			 statementUpdateProject.setInt(3,project.getManagerGroupId());
			 statementUpdateProject.setInt(4,project.getTaskId());
			 statementUpdateProject.setString(5,project.getDescription());
			 statementUpdateProject.setInt(6,project.getFlags());
			statementUpdateProject.setTimestamp(7,new Timestamp(project.getCreateDate()));
			 if(project.getPublishingDate() == C_UNKNOWN_LONG) {
				statementUpdateProject.setNull(8,java.sql.Types.TIMESTAMP);
			 } else {
				statementUpdateProject.setTimestamp(8,new Timestamp(project.getPublishingDate()));
			 }
			 statementUpdateProject.setInt(9,project.getPublishedBy());
			 statementUpdateProject.setInt(10,project.getCountLockedResources());			 
			 
			 statementUpdateProject.setInt(11,project.getId());
			 
			 statementUpdateProject.executeUpdate();
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }
		 return(readProject(project.getId()));
	 }
	 
	/**
	 * Deletes a project.
	 * 
	 * @param project The project that will be deleted.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public void deleteProject(CmsProject project)
		 throws CmsException {
		 try {
			 // create the statement
			 PreparedStatement statementDeleteProject = 
				m_con.prepareStatement(C_PROJECT_DELETE);
			 
			 statementDeleteProject.setInt(1,project.getId());
			 
			 statementDeleteProject.executeUpdate();
		 } catch( SQLException exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }
	 }
	 
	/**
	 * Returns all projects, which are owned by a user.
	 * 
	 * @param user The requesting user.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjectsByUser(CmsUser user)
		 throws CmsException {
 		 Vector projects = new Vector();

		 try {
			 ResultSet result;
			 
			 // create the statement
			 PreparedStatement statementGetProjectsByUser = 
				m_con.prepareStatement(C_PROJECT_GET_BY_USER);

			 statementGetProjectsByUser.setInt(1,user.getId());
			 result = statementGetProjectsByUser.executeQuery();
			 
			 while(result.next()) {
				 projects.addElement( new CmsProject(result.getInt(C_PROJECT_ID),
													 result.getString(C_PROJECT_NAME),
													 result.getString(C_PROJECT_DESCRIPTION),
													 result.getInt(C_TASK_ID),
													 result.getInt(C_USER_ID),
													 result.getInt(C_GROUP_ID),
													 result.getInt(C_MANAGERGROUP_ID),
													 result.getInt(C_PROJECT_FLAGS),
													 SqlHelper.getTimestamp(result,C_PROJECT_CREATEDATE),
													 SqlHelper.getTimestamp(result,C_PROJECT_PUBLISHDATE),
													 result.getInt(C_PROJECT_PUBLISHED_BY),
													 result.getInt(C_PROJECT_COUNT_LOCKED)));
			 }
			 return(projects);
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }
	 }

	/**
	 * Returns all projects, which the group may access.
	 * 
	 * @param group The group to test.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjectsByGroup(CmsGroup group)
		 throws CmsException {		 
 		 Vector projects = new Vector();

		 try {
			 ResultSet result;
			 
			 // create the statement
			PreparedStatement statementGetProjectsByGroup = 
				m_con.prepareStatement(C_PROJECT_GET_BY_GROUP);
			
			statementGetProjectsByGroup.setInt(1,group.getId());
			statementGetProjectsByGroup.setInt(2,group.getId());
			result = statementGetProjectsByGroup.executeQuery();
			 
			 while(result.next()) {
				 projects.addElement( new CmsProject(result.getInt(C_PROJECT_ID),
													 result.getString(C_PROJECT_NAME),
													 result.getString(C_PROJECT_DESCRIPTION),
													 result.getInt(C_TASK_ID),
													 result.getInt(C_USER_ID),
													 result.getInt(C_GROUP_ID),
													 result.getInt(C_MANAGERGROUP_ID),
													 result.getInt(C_PROJECT_FLAGS),
													 SqlHelper.getTimestamp(result,C_PROJECT_CREATEDATE),
													 SqlHelper.getTimestamp(result,C_PROJECT_PUBLISHDATE),
													 result.getInt(C_PROJECT_PUBLISHED_BY),
													 result.getInt(C_PROJECT_COUNT_LOCKED)));
			 }
			 return(projects);
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }
	 }

	/**
	 * Returns all projects, which the managergroup may manage.
	 * 
	 * @param managergroup The group to test.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjectsByManagerGroup(CmsGroup managergroup)
		 throws CmsException {		 
 		 Vector projects = new Vector();

		 try {
			 ResultSet result;
			 
			 // create the statement
			PreparedStatement statementGetProjectsByGroup = 
				m_con.prepareStatement(C_PROJECT_GET_BY_MANAGERGROUP);
			
			statementGetProjectsByGroup.setInt(1,managergroup.getId());
			result = statementGetProjectsByGroup.executeQuery();
			 
			 while(result.next()) {
				 projects.addElement( new CmsProject(result.getInt(C_PROJECT_ID),
													 result.getString(C_PROJECT_NAME),
													 result.getString(C_PROJECT_DESCRIPTION),
													 result.getInt(C_TASK_ID),
													 result.getInt(C_USER_ID),
													 result.getInt(C_GROUP_ID),
													 result.getInt(C_MANAGERGROUP_ID),
													 result.getInt(C_PROJECT_FLAGS),
													 SqlHelper.getTimestamp(result,C_PROJECT_CREATEDATE),
													 SqlHelper.getTimestamp(result,C_PROJECT_PUBLISHDATE),
													 result.getInt(C_PROJECT_PUBLISHED_BY),
													 result.getInt(C_PROJECT_COUNT_LOCKED)));

			 }
			 return(projects);
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }
	 }
	 
	/**
	 * Returns all projects with the overgiven flag.
	 * 
	 * @param flag The flag for the project.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllProjects(int flag)
		 throws CmsException {
 		 Vector projects = new Vector();

		 try {
			 ResultSet result;
			 
			 // create the statement
			PreparedStatement statementGetProjectsByFlag = 
				m_con.prepareStatement(C_PROJECT_GET_BY_FLAG);
			
			statementGetProjectsByFlag.setInt(1,flag);
			result = statementGetProjectsByFlag.executeQuery();
			 
			 while(result.next()) {
				 projects.addElement( new CmsProject(result.getInt(C_PROJECT_ID),
													 result.getString(C_PROJECT_NAME),
													 result.getString(C_PROJECT_DESCRIPTION),
													 result.getInt(C_TASK_ID),
													 result.getInt(C_USER_ID),
													 result.getInt(C_GROUP_ID),
													 result.getInt(C_MANAGERGROUP_ID),
													 result.getInt(C_PROJECT_FLAGS),
													 SqlHelper.getTimestamp(result,C_PROJECT_CREATEDATE),
													 SqlHelper.getTimestamp(result,C_PROJECT_PUBLISHDATE),
													 result.getInt(C_PROJECT_PUBLISHED_BY),
													 result.getInt(C_PROJECT_COUNT_LOCKED)));

			 }
			 return(projects);
		 } catch( Exception exc ) {
			 throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), 
				 CmsException.C_SQL_ERROR, exc);
		 }
	 }
	 
	 /**
     * Connects to the project database.
     * 
     * @param conUrl The connection string to the database.
     * 
     * @exception CmsException Throws CmsException if connection fails.
     */
    private void initConnections(String conUrl)	
      throws CmsException {
      
        try {
        	m_con = DriverManager.getConnection(conUrl);
       	} catch (SQLException e)	{
         	throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), 
				CmsException.C_SQL_ERROR, e);
		}
    }
}

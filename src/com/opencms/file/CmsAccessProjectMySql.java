package com.opencms.file;

import java.util.*;
import java.sql.*;

import com.opencms.core.*;

/**
 * This class describes the access to projects in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/16 18:55:53 $
 */
class CmsAccessProjectMySql extends A_CmsAccessProject {

    /**
     * This is the connection object to the database
     */
    private Connection m_con  = null;

	/**
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementCreateProject;
	
	/**
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementReadProject;

	/**
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementGetProjectsByUser;

	/**
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementGetProjectsByGroup;

	/**
	 * A prepared statement to access the database.
	 */
	private PreparedStatement m_statementUpdateProject;

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
	private static final String C_GLOBETASK_ID = "GLOBETASK_ID";
	
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
     * SQL Command for creating projects.
     */    
    private static final String C_PROJECT_CREATE = "INSERT INTO PROJECTS VALUES(null,?,?,?,?,?,?)";

	/**
     * SQL Command for updating projects.
     */    
    private static final String C_PROJECT_UPDATE = "UPDATE PROJECTS set " + 
												   C_USER_ID + " = ?, " +
												   C_GROUP_ID + " = ?, " +
												   C_GLOBETASK_ID + " = ?, " +
												   C_PROJECT_DESCRIPTION + " = ?, " +
												   C_PROJECT_FLAGS + " = ? " +
												   "where " + C_PROJECT_NAME + " = ?";

	/**
     * SQL Command for reading projects.
     */    
    private static final String C_PROJECT_READ = "Select * from PROJECTS where " + C_PROJECT_NAME + " = ?";
	
	/**
     * SQL Command for reading projects.
     */    
    private static final String C_PROJECT_GET_BY_USER = "Select * from PROJECTS where " + C_USER_ID + " = ?";
	
	/**
     * SQL Command for reading projects.
     */    
    private static final String C_PROJECT_GET_BY_GROUP = "Select * from PROJECTS where " + C_GROUP_ID + " = ?";
	
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
		initStatements();
    }

	/**
	 * Reads a project from the Cms.
	 * 
	 * @param name The name of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 A_CmsProject readProject(String name)
		 throws CmsException {		 
		 try {
			 m_statementReadProject.setString(1,name);
			 ResultSet result = m_statementReadProject.executeQuery();
			 
			 // if resultset exists - return it
			 if(result.next()) {
				 return( new CmsProject(result.getInt(C_PROJECT_ID),
										result.getString(C_PROJECT_NAME),
										result.getString(C_PROJECT_DESCRIPTION),
										result.getInt(C_GLOBETASK_ID),
										result.getInt(C_USER_ID),
										result.getInt(C_GROUP_ID),
										result.getInt(C_PROJECT_FLAGS)));
			 } else {
				 // project not found!
				 throw new CmsException(CmsException.C_NOT_FOUND);
			 }
		 } catch( SQLException exc ) {
			 throw new CmsException(CmsException.C_SQL_ERROR, exc);
		 }
	 }
	
	/**
	 * Creates a project.
	 * 
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param globetaskId The id of the globe task.
	 * @param ownerId The id of the owner to be set.
	 * @param groupId the id of the group to be set.
	 * @param flags The flags for the project (e.g. archive).
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a project with the same name for this resource-type exists already.
	 */
	 A_CmsProject createProject(String name, String description, int globetaskId, 
								int ownerId, int groupId, int flags)
		throws CmsException, CmsDuplicateKeyException {		 
		 try {
			 m_statementCreateProject.setInt(1,ownerId);
			 m_statementCreateProject.setInt(2,groupId);
			 m_statementCreateProject.setInt(3,globetaskId);
			 m_statementCreateProject.setString(4,name);
			 m_statementCreateProject.setString(5,description);
			 m_statementCreateProject.setInt(6,flags);
			 m_statementCreateProject.executeUpdate();
		 } catch( SQLException exc ) {
			 throw new CmsException(CmsException.C_SQL_ERROR, exc);
		 }
		 return(readProject(name));
	 }

	/**
	 * Updates a project.
	 * 
	 * @param name The name of the project to update.
	 * @param description The description for the project.
	 * @param globetaskId The id of the globe task.
	 * @param ownerId The id of the owner to be set.
	 * @param groupId the id of the group to be set.
	 * @param flags The flags for the project (e.g. archive).
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a project with the same name for this resource-type exists already.
	 */
	 A_CmsProject updateProject(String name, String description, int globetaskId, 
								int ownerId, int groupId, int flags)
		 throws CmsException, CmsDuplicateKeyException {
		 try {    
			 m_statementUpdateProject.setInt(1,ownerId);
			 m_statementUpdateProject.setInt(2,groupId);
			 m_statementUpdateProject.setInt(3,globetaskId);
			 m_statementUpdateProject.setString(4,description);
			 m_statementUpdateProject.setInt(5,flags);
			 m_statementUpdateProject.setString(6,name);
			 m_statementUpdateProject.executeUpdate();
		 } catch( SQLException exc ) {
			 throw new CmsException(CmsException.C_SQL_ERROR, exc);
		 }
		 return(readProject(name));
	 }
	 
	/**
	 * Returns all projects, which are owned by a user.
	 * 
	 * @param userId the id of the user to test.
	 * 
	 * @return a Vector of projects.
	 */
	 Vector getAllAccessibleProjectsByUser(int userId)
		 throws CmsException {
 		 Vector projects = new Vector();

		 try {
			 m_statementGetProjectsByUser.setInt(1,userId);
			 ResultSet result = m_statementGetProjectsByUser.executeQuery();
			 
			 while(result.next()) {
				 projects.addElement( new CmsProject(result.getInt(C_PROJECT_ID),
													 result.getString(C_PROJECT_NAME),
													 result.getString(C_PROJECT_DESCRIPTION),
													 result.getInt(C_GLOBETASK_ID),
													 result.getInt(C_USER_ID),
													 result.getInt(C_GROUP_ID),
													 result.getInt(C_PROJECT_FLAGS)));
			 }
			 return(projects);
		 } catch( SQLException exc ) {
			 throw new CmsException(CmsException.C_SQL_ERROR, exc);
		 }
	 }

	/**
	 * Returns all projects, which the group may access.
	 * 
	 * @param groupId the id of the group to test.
	 * 
	 * @return a Vector of projects.
	 */
	 Vector getAllAccessibleProjectsByGroup(int groupId)
		 throws CmsException {		 
 		 Vector projects = new Vector();

		 try {
			 m_statementGetProjectsByGroup.setInt(1,groupId);
			 ResultSet result = m_statementGetProjectsByGroup.executeQuery();
			 
			 while(result.next()) {
				 projects.addElement( new CmsProject(result.getInt(C_PROJECT_ID),
													 result.getString(C_PROJECT_NAME),
													 result.getString(C_PROJECT_DESCRIPTION),
													 result.getInt(C_GLOBETASK_ID),
													 result.getInt(C_USER_ID),
													 result.getInt(C_GROUP_ID),
													 result.getInt(C_PROJECT_FLAGS)));
			 }
			 return(projects);
		 } catch( SQLException exc ) {
			 throw new CmsException(CmsException.C_SQL_ERROR, exc);
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
         	throw new CmsException(CmsException.C_SQL_ERROR, e);
		}
    }
	
	/**
	 * Inits all prepared statements.
	 */
	private void initStatements()
		throws CmsException {
		try {
			m_statementCreateProject = m_con.prepareStatement(C_PROJECT_CREATE);
			m_statementUpdateProject = m_con.prepareStatement(C_PROJECT_UPDATE);
			m_statementReadProject = m_con.prepareStatement(C_PROJECT_READ);
			m_statementGetProjectsByUser = m_con.prepareStatement(C_PROJECT_GET_BY_USER);
			m_statementGetProjectsByGroup = m_con.prepareStatement(C_PROJECT_GET_BY_GROUP);
		} catch (SQLException exc) {
			throw new CmsException(CmsException.C_SQL_ERROR, exc);
		}
	}
}

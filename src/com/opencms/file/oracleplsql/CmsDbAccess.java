package com.opencms.file.oracleplsql;

import oracle.jdbc.driver.*;
/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oracleplsql/Attic/CmsDbAccess.java,v $
 * Date   : $Date: 2001/06/27 07:25:08 $
 * Version: $Revision: 1.32 $
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

import javax.servlet.http.*;
import java.util.*;
import java.sql.*;
import java.security.*;
import java.io.*;
import source.org.apache.java.io.*;
import source.org.apache.java.util.*;
import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.file.utils.*;
import com.opencms.util.*;



/**
 * This is the generic access module to load and store resources from and into
 * the database.
 *
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Hanjo Riege
 * @author Anders Fugmann
 * @version $Revision: 1.32 $ $Date: 2001/06/27 07:25:08 $ *
 */
public class CmsDbAccess extends com.opencms.file.genericSql.CmsDbAccess implements I_CmsConstants, I_CmsLogChannels {

	/**
	 * Instanciates the access-module and sets up all required modules and connections.
	 * @param config The OpenCms configuration.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public CmsDbAccess(Configurations config)
		throws CmsException {

		super(config);
	}

	/**
	 *  Checks, if the user may create this resource.
	 *
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * @return wether the user has access, or not.
	 */
	public boolean accessCreate(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
		//System.out.println("PL/SQL: accessCreate");
		com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
		CallableStatement statement = null;
		Connection con = null;
		try {
			// create the statement
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareCall(cq.get("C_PLSQL_ACCESS_ACCESSCREATE"));
			statement.registerOutParameter(1, Types.INTEGER);
			statement.setInt(2, currentUser.getId());
			statement.setInt(3, currentProject.getId());
			statement.setInt(4, resource.getResourceId());
			statement.execute();
			if (statement.getInt(1) == 1) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException sqlexc) {
			CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
			throw cmsException;
		} catch (Exception e) {
			throw new CmsException("[" + this.getClass().getName() + "]", e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException exc){
					// nothing to do here
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException exc){
					// nothing to do here
				}
			}
		}
	}

	/**
	 * Checks, if the user may lock this resource.
	 *
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * @return wether the user has access, or not.
	 */
	public boolean accessLock(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
		//System.out.println("PL/SQL: accessLock");
		com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
		CallableStatement statement = null;
		Connection con = null;
		try {
			// create the statement
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareCall(cq.get("C_PLSQL_ACCESS_ACCESSLOCK"));
			statement.registerOutParameter(1, Types.INTEGER);
			statement.setInt(2, currentUser.getId());
			statement.setInt(3, currentProject.getId());
			statement.setInt(4, resource.getResourceId());
			statement.execute();
			if (statement.getInt(1) == 1) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException sqlexc) {
			CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
			throw cmsException;
		} catch (Exception e) {
			throw new CmsException("[" + this.getClass().getName() + "]", e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException exc){
					// nothing to do here
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException exc){
					// nothing to do here
				}
			}
		}
	}

	/**
	 * Checks, if the user may read this resource.
	 *
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * @return wether the user has access, or not
	 */
	public boolean accessProject(CmsUser currentUser, int projectId) throws CmsException {
		//System.out.println("PL/SQL: accessProject");
		com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
		CallableStatement statement = null;
		Connection con = null;
		try {
			// create the statement
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareCall(cq.get("C_PLSQL_ACCESS_ACCESSPROJECT"));
			statement.registerOutParameter(1, Types.INTEGER);
			statement.setInt(2, currentUser.getId());
			statement.setInt(3, projectId);
			statement.execute();
			if (statement.getInt(1) == 1) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException sqlexc) {
			CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
			throw cmsException;
		} catch (Exception e) {
			throw new CmsException("[" + this.getClass().getName() + "]", e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException exc){
					// nothing to do here
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException exc){
					// nothing to do here
				}
			}
		}
	}

	/**
	 * Checks, if the user may read this resource.
	 *
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * @return wether the user has access, or not.
	 */
	public boolean accessRead(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
		//System.out.println("PL/SQL: accessRead");
		com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
		CallableStatement statement = null;
		Connection con = null;
		try {
			// create the statement
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareCall(cq.get("C_PLSQL_ACCESS_ACCESSREAD"));
			statement.registerOutParameter(1, Types.INTEGER);
			statement.setInt(2, currentUser.getId());
			statement.setInt(3, currentProject.getId());
			statement.setString(4, resource.getAbsolutePath());
			statement.execute();
			if (statement.getInt(1) == 1) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException sqlexc) {
			CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
			throw cmsException;
		} catch (Exception e) {
			throw new CmsException("[" + this.getClass().getName() + "]", e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException exc){
					// nothing to do here
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException exc){
					// nothing to do here
				}
			}
		}
	}

	/**
	 * Checks, if the user may write this resource.
	 *
	 * @param currentUser The user who requested this method.
	 * @param currentProject The current project of the user.
	 * @param resource The resource to check.
	 * @return wether the user has access, or not.
	 */
	public boolean accessWrite(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
		//System.out.println("PL/SQL: accessWrite");
		com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
		CallableStatement statement = null;
		Connection con = null;
		try {
			// create the statement
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareCall(cq.get("C_PLSQL_ACCESS_ACCESSWRITE"));
			statement.registerOutParameter(1, Types.INTEGER);
			statement.setInt(2, currentUser.getId());
			statement.setInt(3, currentProject.getId());
			statement.setInt(4, resource.getResourceId());
			statement.execute();
			if (statement.getInt(1) == 1) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException sqlexc) {
			CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
			throw cmsException;
		} catch (Exception e) {
			throw new CmsException("[" + this.getClass().getName() + "]", e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException exc){
					// nothing to do here
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException exc){
					// nothing to do here
				}
			}
		}
	}

	/**
	 * Creates a serializable object in the systempropertys.
	 *
	 * @param name The name of the property.
	 * @param object The property-object.
	 * @return object The property-object.
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Serializable addSystemProperty(String name, Serializable object) throws CmsException {
		//System.out.println("PL/SQL: addSystemProperty");
		com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
		byte[] value;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		PreparedStatement nextStatement = null;
		Connection con = null;
		ResultSet res = null;
		try {
			int id = nextId(C_TABLE_SYSTEMPROPERTIES);
			// serialize the object
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(bout);
			oout.writeObject(object);
			oout.close();
			value = bout.toByteArray();

			// create the object
			// first insert the new systemproperty with empty systemproperty_value, then update
			// the systemproperty_value. These two steps are necessary because of using Oracle BLOB
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareStatement(cq.get("C_PLSQL_SYSTEMPROPERTIES_FORINSERT"));
			statement.setInt(1, id);
			statement.setString(2, name);
			//statement.setBytes(3,value);
			statement.executeUpdate();
			//statement.close();
			// now update the systemproperty_value
			statement2 = con.prepareStatement(cq.get("C_PLSQL_SYSTEMPROPERTIES_FORUPDATE"));
			statement2.setInt(1, id);
			con.setAutoCommit(false);
			res = statement2.executeQuery();
			while (res.next()) {
				oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SYSTEMPROPERTY_VALUE");
				ByteArrayInputStream instream = new ByteArrayInputStream(value);
				OutputStream outstream = blob.getBinaryOutputStream();
				byte[] chunk = new byte[blob.getChunkSize()];
				int i = -1;
				while ((i = instream.read(chunk)) != -1) {
					outstream.write(chunk, 0, i);
				}
				instream.close();
				outstream.close();
			}
			//statement2.close();
			//res.close();
			// for the oracle-driver commit or rollback must be executed manually
			// because setAutoCommit = false
			nextStatement = con.prepareStatement(cq.get("C_COMMIT"));
			nextStatement.execute();
			nextStatement.close();
			con.setAutoCommit(true);
		} catch (SQLException e) {
			throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
		} catch (IOException e) {
			throw new CmsException("[" + this.getClass().getName() + "]" + CmsException.C_SERIALIZATION, e);
		} finally {
			if (res != null) {
				try {
					res.close();
				} catch (SQLException se) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException exc){
				}
			}
			if (statement2 != null) {
				try {
					statement2.close();
				} catch (SQLException exc){
				}
				try {
					nextStatement = con.prepareStatement(cq.get("C_ROLLBACK"));
					nextStatement.execute();
				} catch (SQLException exc){
					// nothing to do here
				}
			}
			if (nextStatement != null) {
				try {
					nextStatement.close();
				} catch (SQLException exc){
				}
			}
			if (con != null) {
				try {
					con.setAutoCommit(true);
				} catch (SQLException exc){
					// nothing to do here
				}
				try {
					con.close();
				} catch (SQLException e){
				}
			}
		}
		return readSystemProperty(name);
	}
	/**
	 * Adds a user to the database.
	 *
	 * @param name username
	 * @param password user-password
	 * @param description user-description
	 * @param firstname user-firstname
	 * @param lastname user-lastname
	 * @param email user-email
	 * @param lastlogin user-lastlogin
	 * @param lastused user-lastused
	 * @param flags user-flags
	 * @param additionalInfos user-additional-infos
	 * @param defaultGroup user-defaultGroup
	 * @param address user-defauladdress
	 * @param section user-section
	 * @param type user-type
	 *
	 * @return the created user.
	 * @exception thorws CmsException if something goes wrong.
	 */
	public CmsUser addUser(String name, String password, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {
		//System.out.println("PL/SQL: addUser");
		com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
		int id = nextId(C_TABLE_USERS);
		byte[] value = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		PreparedStatement nextStatement = null;
		Connection con = null;
		ResultSet res = null;
		try {
			// serialize the hashtable
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(bout);
			oout.writeObject(additionalInfos);
			oout.close();
			value = bout.toByteArray();

			// write data to database
			// first insert the data without user_info
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareStatement(cq.get("C_PLSQL_USERSFORINSERT"));
			statement.setInt(1, id);
			statement.setString(2, name);
			// crypt the password with MD5
			statement.setString(3, digest(password));
			statement.setString(4, digest(""));
			statement.setString(5, checkNull(description));
			statement.setString(6, checkNull(firstname));
			statement.setString(7, checkNull(lastname));
			statement.setString(8, checkNull(email));
			statement.setTimestamp(9, new Timestamp(lastlogin));
			statement.setTimestamp(10, new Timestamp(lastused));
			statement.setInt(11, flags);
			//statement.setBytes(12,value);
			statement.setInt(12, defaultGroup.getId());
			statement.setString(13, checkNull(address));
			statement.setString(14, checkNull(section));
			statement.setInt(15, type);
			statement.executeUpdate();
			statement.close();
			// now update user_info of the new user
			statement2 = con.prepareStatement(cq.get("C_PLSQL_USERSFORUPDATE"));
			statement2.setInt(1, id);
			con.setAutoCommit(false);
			res = statement2.executeQuery();
			while (res.next()) {
				oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("USER_INFO");
				ByteArrayInputStream instream = new ByteArrayInputStream(value);
				OutputStream outstream = blob.getBinaryOutputStream();
				byte[] chunk = new byte[blob.getChunkSize()];
				int i = -1;
				while ((i = instream.read(chunk)) != -1) {
					outstream.write(chunk, 0, i);
				}
				instream.close();
				outstream.close();
			}
			statement2.close();
			res.close();
			// for the oracle-driver commit or rollback must be executed manually
			// because setAutoCommit = false in CmsDbPool.CmsDbPool
			nextStatement = con.prepareStatement(cq.get("C_COMMIT"));
			nextStatement.execute();
			nextStatement.close();
			con.setAutoCommit(true);
		} catch (SQLException e) {
			throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
		} catch (IOException e) {
			throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:" + CmsException.C_SERIALIZATION, e);
		} finally {
			if (res != null) {
				try {
					res.close();
				} catch (SQLException se) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException exc){
				}
			}
			if (statement2 != null) {
				try {
					statement2.close();
				} catch (SQLException exc){
				}
				try {
					nextStatement = con.prepareStatement(cq.get("C_ROLLBACK"));
					nextStatement.execute();
				} catch (SQLException se) {
				}
			}
			if (nextStatement != null) {
				try {
					nextStatement.close();
				} catch (SQLException exc){
				}
			}
			if (con != null) {
				try {
					con.setAutoCommit(true);
				} catch (SQLException se) {
				}
				try {
					con.close();
				} catch (SQLException e) {
				}
			}
		}
		return readUser(id);
	}

	/**
	 * Adds a user to the database.
	 *
	 * @param name username
	 * @param password user-password
     * @param recoveryPassword user-recoveryPassword
	 * @param description user-description
	 * @param firstname user-firstname
	 * @param lastname user-lastname
	 * @param email user-email
	 * @param lastlogin user-lastlogin
	 * @param lastused user-lastused
	 * @param flags user-flags
	 * @param additionalInfos user-additional-infos
	 * @param defaultGroup user-defaultGroup
	 * @param address user-defauladdress
	 * @param section user-section
	 * @param type user-type
	 *
	 * @return the created user.
	 * @exception thorws CmsException if something goes wrong.
	 */
	public CmsUser addImportUser(String name, String password, String recoveryPassword, String description, String firstname, String lastname, String email, long lastlogin, long lastused, int flags, Hashtable additionalInfos, CmsGroup defaultGroup, String address, String section, int type) throws CmsException {
		//System.out.println("PL/SQL: addUser");
		com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
		int id = nextId(C_TABLE_USERS);
		byte[] value = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		PreparedStatement nextStatement = null;
		Connection con = null;
		ResultSet res = null;
		try {
			// serialize the hashtable
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(bout);
			oout.writeObject(additionalInfos);
			oout.close();
			value = bout.toByteArray();

			// write data to database
			// first insert the data without user_info
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareStatement(cq.get("C_PLSQL_USERSFORINSERT"));
			statement.setInt(1, id);
			statement.setString(2, name);
			// crypt the password with MD5
			statement.setString(3, checkNull(password));
			statement.setString(4, checkNull(recoveryPassword));
			statement.setString(5, checkNull(description));
			statement.setString(6, checkNull(firstname));
			statement.setString(7, checkNull(lastname));
			statement.setString(8, checkNull(email));
			statement.setTimestamp(9, new Timestamp(lastlogin));
			statement.setTimestamp(10, new Timestamp(lastused));
			statement.setInt(11, flags);
			//statement.setBytes(12,value);
			statement.setInt(12, defaultGroup.getId());
			statement.setString(13, checkNull(address));
			statement.setString(14, checkNull(section));
			statement.setInt(15, type);
			statement.executeUpdate();
			statement.close();
			// now update user_info of the new user
			statement2 = con.prepareStatement(cq.get("C_PLSQL_USERSFORUPDATE"));
			statement2.setInt(1, id);
			con.setAutoCommit(false);
			res = statement2.executeQuery();
			while (res.next()) {
				oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("USER_INFO");
				ByteArrayInputStream instream = new ByteArrayInputStream(value);
				OutputStream outstream = blob.getBinaryOutputStream();
				byte[] chunk = new byte[blob.getChunkSize()];
				int i = -1;
				while ((i = instream.read(chunk)) != -1) {
					outstream.write(chunk, 0, i);
				}
				instream.close();
				outstream.close();
			}
			statement2.close();
			res.close();
			// for the oracle-driver commit or rollback must be executed manually
			// because setAutoCommit = false in CmsDbPool.CmsDbPool
			nextStatement = con.prepareStatement(cq.get("C_COMMIT"));
			nextStatement.execute();
			nextStatement.close();
			con.setAutoCommit(true);
		} catch (SQLException e) {
			throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
		} catch (IOException e) {
			throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:" + CmsException.C_SERIALIZATION, e);
		} finally {
			if (res != null) {
				try {
					res.close();
				} catch (SQLException se) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException exc){
				}
			}
			if (statement2 != null) {
				try {
					statement2.close();
				} catch (SQLException exc){
				}
				try {
					nextStatement = con.prepareStatement(cq.get("C_ROLLBACK"));
					nextStatement.execute();
				} catch (SQLException se) {
				}
			}
			if (nextStatement != null) {
				try {
					nextStatement.close();
				} catch (SQLException exc){
				}
			}
			if (con != null) {
				try {
					con.setAutoCommit(true);
				} catch (SQLException se) {
				}
				try {
					con.close();
				} catch (SQLException e) {
				}
			}
		}
		return readUser(id);
	}

	/**
	 * Copies the file.
	 *
	 * @param project The project in which the resource will be used.
	 * @param onlineProject The online project of the OpenCms.
	 * @param userId The id of the user who wants to copy the file.
	 * @param source The complete path of the sourcefile.
	 * @param parentId The parentId of the resource.
	 * @param destination The complete path of the destinationfile.
	 *
	 * @exception CmsException Throws CmsException if operation was not succesful.
	 */
	public void copyFile(CmsProject project, int userId, String source, String destination) throws CmsException {
		//System.out.println("PL/SQL: copyFile");
		if (destination.length() > C_MAX_LENGTH_RESOURCE_NAME){
			throw new CmsException("["+this.getClass().getName()+"] "+"Resourcename too long(>"+C_MAX_LENGTH_RESOURCE_NAME+") ",CmsException.C_BAD_NAME);
		}

		com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
		CallableStatement statement = null;
		Connection con = null;
		try {
			// create the statement
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareCall(cq.get("C_PLSQL_RESOURCES_COPYFILE"));
			statement.setInt(1, project.getId());
			statement.setInt(2, userId);
			statement.setString(3, source);
			statement.setString(4, destination);
			statement.execute();
		} catch (SQLException sqlexc) {
			CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
			throw cmsException;
		} catch (Exception e) {
			throw new CmsException("[" + this.getClass().getName() + "]", e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException exc) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
				}
			}
		}
	}
	// methods working with resources

/**
 * Creates a new file with the given content and resourcetype.
 *
 * @param user The user who wants to create the file.
 * @param project The project in which the resource will be used.
 * @param onlineProject The online project of the OpenCms.
 * @param filename The complete name of the new file (including pathinformation).
 * @param flags The flags of this resource.
 * @param parentId The parentId of the resource.
 * @param contents The contents of the new file.
 * @param resourceType The resourceType of the new file.
 *
 * @return file The created file.
 *
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public CmsFile createFile(CmsUser user, CmsProject project, CmsProject onlineProject, String filename, int flags, int parentId, byte[] contents, CmsResourceType resourceType) throws CmsException {
	//System.out.println("PL/SQL: createFile");
	if (filename.length() > C_MAX_LENGTH_RESOURCE_NAME){
		throw new CmsException("["+this.getClass().getName()+"] "+"Resourcename too long(>"+C_MAX_LENGTH_RESOURCE_NAME+") ",CmsException.C_BAD_NAME);
	}

	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	// it is not allowed, that there is no content in the file
	// TODO: check if this can be done in another way:
	if (contents.length == 0) {
		contents = " ".getBytes();
	}
	int state = C_STATE_NEW;
	// Test if the file is already there and marked as deleted.
	// If so, delete it
	try {
		CmsResource resource = readFileHeader(project.getId(), filename);
                throw new CmsException("["+this.getClass().getName()+"] ",CmsException.C_FILE_EXISTS);
	} catch (CmsException e) {
		// if the file is maked as deleted remove it!
		if (e.getType()==CmsException.C_RESOURCE_DELETED) {
		   removeFile(project.getId(),filename);
		   state=C_STATE_CHANGED;
		}
                if (e.getType()==CmsException.C_FILE_EXISTS) {
				   throw e;
		}
                if (e.getType() == CmsException.C_NOT_FOUND) {
                        try{
                                // Test if file is in OnlineProject
                                readFileHeader(onlineProject.getId(), filename);
                                throw new CmsException("[" + this.getClass().getName() + "] ", CmsException.C_FILE_EXISTS);
                        }catch(CmsException e2){
                                if (e2.getType() == CmsException.C_FILE_EXISTS ){
                                        throw e2;
                                }
                        }
                }
	}
	int resourceId = nextId(m_cq.get("C_TABLE_RESOURCES"));
	int fileId = nextId(m_cq.get("C_TABLE_FILES"));

	PreparedStatement statement = null;
	PreparedStatement statementFileIns = null;
	PreparedStatement statementFileUpd = null;
	PreparedStatement nextStatement = null;
	Connection con = null;
	ResultSet res = null;
    String usedPool;
    String usedStatement;
    if (project.getId() == onlineProject.getId()){
        usedPool = m_poolNameOnline;
        usedStatement = "_ONLINE";
    } else {
        usedPool = m_poolName;
        usedStatement = "";
    }
	try {
		con = DriverManager.getConnection(usedPool);
		statement = con.prepareStatement(m_cq.get("C_RESOURCES_WRITE"+usedStatement));
		// write new resource to the database
		statement.setInt(1, resourceId);
		statement.setInt(2, parentId);
		statement.setString(3, filename);
		statement.setInt(4, resourceType.getResourceType());
		statement.setInt(5, flags);
		statement.setInt(6, user.getId());
		statement.setInt(7, user.getDefaultGroupId());
		statement.setInt(8, project.getId());
		statement.setInt(9, fileId);
		statement.setInt(10, C_ACCESS_DEFAULT_FLAGS);
		statement.setInt(11, state);
		statement.setInt(12, C_UNKNOWN_ID);
		statement.setInt(13, resourceType.getLauncherType());
		statement.setString(14, resourceType.getLauncherClass());
		statement.setTimestamp(15, new Timestamp(System.currentTimeMillis()));
		statement.setTimestamp(16, new Timestamp(System.currentTimeMillis()));
		statement.setInt(17, contents.length);
		statement.setInt(18, user.getId());
		statement.executeUpdate();
		statement.close();
		// first insert new file without file_content, then update the file_content
		// these two steps are necessary because of using BLOBs	in the Oracle DB
		statementFileIns = con.prepareStatement(cq.get("C_PLSQL_FILESFORINSERT"+usedStatement));
		statementFileIns.setInt(1, fileId);
		statementFileIns.executeUpdate();
		statementFileIns.close();
		// update the file content in the FILES database.
		statementFileUpd = con.prepareStatement(cq.get("C_PLSQL_FILESFORUPDATE"+usedStatement));
		statementFileUpd.setInt(1, fileId);
		con.setAutoCommit(false);
		res = statementFileUpd.executeQuery();
		try {
			while (res.next()) {
				oracle.sql.BLOB blobnew = ((OracleResultSet) res).getBLOB("FILE_CONTENT");
				ByteArrayInputStream instream = new ByteArrayInputStream(contents);
				OutputStream outstream = blobnew.getBinaryOutputStream();
				byte[] chunk = new byte[blobnew.getChunkSize()];
				int i = -1;
				while ((i = instream.read(chunk)) != -1) {
					outstream.write(chunk, 0, i);
				}
				instream.close();
				outstream.close();
			}
			// for the oracle-driver commit or rollback must be executed manually
			// because setAutoCommit = false
			nextStatement = con.prepareStatement(cq.get("C_COMMIT"));
			nextStatement.execute();
			nextStatement.close();
			con.setAutoCommit(true);
		} catch (IOException e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), e);
		}
		statementFileUpd.close();
		res.close();
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc){
			}
		}
		if (statementFileIns != null) {
			try {
				statementFileIns.close();
			} catch (SQLException exc) {
			}
		}
		if (statementFileUpd != null) {
			try {
				statementFileUpd.close();
			} catch (SQLException exc) {
			}
			try {
				nextStatement = con.prepareStatement(cq.get("C_ROLLBACK"));
				nextStatement.execute();
			} catch (SQLException se) {
			}
		}
		if (nextStatement != null) {
			try {
				nextStatement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.setAutoCommit(true);
			} catch (SQLException se) {
			}
			try {
				con.close();
			} catch (SQLException e){
			}
		}
	}
	return readFile(user.getId(), project.getId(), onlineProject.getId(), filename);
}
// methods working with session-storage

/**
 * This method creates a new session in the database. It is used
 * for sessionfailover.
 *
 * @param sessionId the id of the session.
 * @return data the sessionData.
 */
public void createSession(String sessionId, Hashtable data) throws CmsException {
	//System.out.println("PL/SQL: createSession");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	byte[] value = null;
	PreparedStatement statement = null;
	PreparedStatement statement2 = null;
	PreparedStatement nextStatement = null;
	Connection con = null;
	ResultSet res = null;
	try {
		// serialize the hashtable
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(data);
		oout.close();
		value = bout.toByteArray();

		// write data to database
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareStatement(cq.get("C_PLSQL_SESSION_FORINSERT"));
		statement.setString(1, sessionId);
		statement.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
		statement.executeUpdate();
		statement.close();
		statement2 = con.prepareStatement(cq.get("C_PLSQL_SESSION_FORUPDATE"));
		statement2.setString(1, sessionId);
		con.setAutoCommit(false);
		res = statement2.executeQuery();
		while (res.next()) {
			oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SESSION_DATA");
			ByteArrayInputStream instream = new ByteArrayInputStream(value);
			OutputStream outstream = blob.getBinaryOutputStream();
			byte[] chunk = new byte[blob.getChunkSize()];
			int i = -1;
			while ((i = instream.read(chunk)) != -1) {
				outstream.write(chunk, 0, i);
			}
			instream.close();
			outstream.close();
		}
		statement2.close();
		res.close();
		// for the oracle-driver commit or rollback must be executed manually
		// because setAutoCommit = false
		nextStatement = con.prepareStatement(cq.get("C_COMMIT"));
		nextStatement.execute();
		nextStatement.close();
		con.setAutoCommit(true);
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (IOException e) {
		throw new CmsException("[" + this.getClass().getName() + "]:" + CmsException.C_SERIALIZATION, e);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (statement2 != null) {
			try {
				statement2.close();
			} catch (SQLException exc) {
			}
			try {
				nextStatement = con.prepareStatement(cq.get("C_ROLLBACK"));
				nextStatement.execute();
			} catch (SQLException se) {
			}
		}
		if (nextStatement != null) {
			try {
				nextStatement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.setAutoCommit(true);
			} catch (SQLException se) {
			}
			try {
				con.close();
			} catch (SQLException e) {
			}
		}
	}
}
/**
 * Returns all projects, which are owned by a user.
 *
 * @param user The requesting user.
 *
 * @return a Vector of projects.
 */
public Vector getAllAccessibleProjects(CmsUser user) throws CmsException {
	//System.out.println("PL/SQL: getAllAccessibleProjects");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	Connection con = null;
	Vector projects = new Vector();
	ResultSet res = null;
	try {
		// create the statement
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareCall(cq.get("C_PLSQL_PROJECTS_GETALLACCESS"));
		statement.registerOutParameter(1, oracle.jdbc.driver.OracleTypes.CURSOR);
		statement.setInt(2, user.getId());
		statement.execute();
		res = (ResultSet) statement.getObject(1);
		while (res.next()) {
			projects.addElement(new CmsProject(res.getInt(m_cq.get("C_PROJECTS_PROJECT_ID")),
                                res.getString(m_cq.get("C_PROJECTS_PROJECT_NAME")),
                                res.getString(m_cq.get("C_PROJECTS_PROJECT_DESCRIPTION")),
                                res.getInt(m_cq.get("C_PROJECTS_TASK_ID")),
                                res.getInt(m_cq.get("C_PROJECTS_USER_ID")),
                                res.getInt(m_cq.get("C_PROJECTS_GROUP_ID")),
                                res.getInt(m_cq.get("C_PROJECTS_MANAGERGROUP_ID")),
                                res.getInt(m_cq.get("C_PROJECTS_PROJECT_FLAGS")),
                                SqlHelper.getTimestamp(res, m_cq.get("C_PROJECTS_PROJECT_CREATEDATE")),
                                SqlHelper.getTimestamp(res, m_cq.get("C_PROJECTS_PROJECT_PUBLISHDATE")),
                                res.getInt(m_cq.get("C_PROJECTS_PROJECT_PUBLISHED_BY")),
                                res.getInt(m_cq.get("C_PROJECTS_PROJECT_TYPE"))));
		}
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
			}
		}
	}
	return (projects);
}
/**
 * Returns a list of groups of a user.<P/>
 *
 * @param name The name of the user.
 * @return Vector of groups
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public Vector getAllGroupsOfUser(String username) throws CmsException {
	//System.out.println("PL/SQL: getAllGroupsOfUser");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
    CmsUser user = null;
    try {
	    user = readUser(username, C_USER_TYPE_SYSTEMUSER);
    } catch (CmsException exc){
        user = readUser(username, C_USER_TYPE_WEBUSER);
    }
	CmsGroup group;
	Vector groups = new Vector();
	CallableStatement statement = null;
	Connection con = null;
	ResultSet res = null;
	try {
		//  get all all groups of the user
        con = DriverManager.getConnection(m_poolName);
		statement = con.prepareCall(cq.get("C_PLSQL_GROUPS_GETGROUPSOFUSER"));
		statement.registerOutParameter(1, oracle.jdbc.driver.OracleTypes.CURSOR);
		statement.setInt(2, user.getId());
		statement.execute();
		res = (ResultSet) statement.getObject(1);
		while (res.next()) {
			group = new CmsGroup(res.getInt(m_cq.get("C_GROUPS_GROUP_ID")),
                                 res.getInt(m_cq.get("C_GROUPS_PARENT_GROUP_ID")),
                                 res.getString(m_cq.get("C_GROUPS_GROUP_NAME")),
                                 res.getString(m_cq.get("C_GROUPS_GROUP_DESCRIPTION")),
                                 res.getInt(m_cq.get("C_GROUPS_GROUP_FLAGS")));
			groups.addElement(group);
		}
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e){
			}
		}
	}
	return groups;
}
/**
 * Get the exeption.
 *
 * @return Exception.
 */
private CmsException getCmsException(String errorIn, Exception exc) {
	CmsException cmsException = null;
	String exceptionMessage = null;
	int exceptionNumber = 0;
	exceptionMessage = exc.getMessage();
	try {
		exceptionNumber = Integer.parseInt(exceptionMessage.substring(4, 9));
	} catch(StringIndexOutOfBoundsException iobexc) {
		if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_DEBUG, "Error in getCmsException() " + exceptionMessage);
		}
	} catch(Exception otherExc) {
		cmsException = new CmsException(errorIn + exceptionMessage, CmsException.C_UNKNOWN_EXCEPTION);
	}

	switch (exceptionNumber) {
		case 20000 :
			cmsException = new CmsException(errorIn, CmsException.C_UNKNOWN_EXCEPTION);
			break;
		case 20001 :
			cmsException = new CmsException(errorIn, CmsException.C_NO_ACCESS);
			break;
		case 20002 :
			cmsException = new CmsException(errorIn, CmsException.C_NOT_FOUND);
			break;
		case 20003 :
			cmsException = new CmsException(errorIn, CmsException.C_BAD_NAME);
			break;
		case 20004 :
			cmsException = new CmsException(errorIn + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
			break;
		case 20005 :
			cmsException = new CmsException(errorIn, CmsException.C_NOT_EMPTY);
			break;
		case 20006 :
			cmsException = new CmsException(errorIn, CmsException.C_NOT_ADMIN);
			break;
		case 20007 :
			cmsException = new CmsException(errorIn, CmsException.C_SERIALIZATION);
			break;
		case 20008 :
			cmsException = new CmsException(errorIn, CmsException.C_NO_GROUP);
			break;
		case 20009 :
			cmsException = new CmsException(errorIn, CmsException.C_GROUP_NOT_EMPTY);
			break;
		case 20010 :
			cmsException = new CmsException(errorIn, CmsException.C_NO_USER);
			break;
		case 20011 :
			cmsException = new CmsException(errorIn, CmsException.C_NO_DEFAULT_GROUP);
			break;
		case 20012 :
			cmsException = new CmsException(errorIn, CmsException.C_FILE_EXISTS);
			break;
		case 20013 :
			cmsException = new CmsException(errorIn, CmsException.C_LOCKED);
			break;
		case 20014 :
			cmsException = new CmsException(errorIn, CmsException.C_FILESYSTEM_ERROR);
			break;
		case 20015 :
			cmsException = new CmsException(errorIn, CmsException.C_INTERNAL_FILE);
			break;
		case 20016 :
			cmsException = new CmsException(errorIn, CmsException.C_MANDATORY_PROPERTY);
			break;
		case 20017 :
			cmsException = new CmsException(errorIn, CmsException.C_SERVICE_UNAVAILABLE);
			break;
		case 20028 :
			cmsException = new CmsException(errorIn, CmsException.C_LAUNCH_ERROR);
			break;
		case 20029 :
			cmsException = new CmsException(errorIn, CmsException.C_CLASSLOADER_ERROR);
			break;
		case 20030 :
			cmsException = new CmsException(errorIn, CmsException.C_SHORT_PASSWORD);
			break;
		case 20031 :
			cmsException = new CmsException(errorIn, CmsException.C_ACCESS_DENIED);
			break;
		case 20032 :
			cmsException = new CmsException(errorIn, CmsException.C_RESOURCE_DELETED);
			break;
		case 20033 :
			cmsException = new CmsException(errorIn, CmsException.C_RB_INIT_ERROR);
			break;
		case 20034 :
			cmsException = new CmsException(errorIn, CmsException.C_REGISTRY_ERROR);
			break;
		default :
			cmsException = new CmsException(errorIn + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
			break;
	}
	return cmsException;
}
/**
 * retrieve the correct instance of the queries holder.
 * This method should be overloaded if other query strings should be used.
 */
protected com.opencms.file.genericSql.CmsQueries getQueries()
{
	return new com.opencms.file.oracleplsql.CmsQueries();
}

	/**
	 * Gets all users of a type.
	 *
	 * @param type The type of the user.
	 * @exception thorws CmsException if something goes wrong.
	 */
	public Vector getUsers(int type)
		throws CmsException {
		//System.out.println("PL/SQL: getUsers");
		Vector users = new Vector();
		PreparedStatement statement = null;
		Connection con = null;
		ResultSet res = null;
		try	{
                        con = DriverManager.getConnection(m_poolName);
			statement = con.prepareStatement(m_cq.get("C_USERS_GETUSERS"));
			statement.setInt(1,type);
			res = statement.executeQuery();
			// create new Cms user objects
			while( res.next() ) {
				// read the additional infos.
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.get("C_USERS_USER_INFO"));
                                byte[] value = new byte[(int) blob.length()];
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();
				CmsUser user = new CmsUser(res.getInt(m_cq.get("C_USERS_USER_ID")),
										   res.getString(m_cq.get("C_USERS_USER_NAME")),
										   res.getString(m_cq.get("C_USERS_USER_PASSWORD")),
										   res.getString(m_cq.get("C_USERS_USER_RECOVERY_PASSWORD")),
										   res.getString(m_cq.get("C_USERS_USER_DESCRIPTION")),
										   res.getString(m_cq.get("C_USERS_USER_FIRSTNAME")),
										   res.getString(m_cq.get("C_USERS_USER_LASTNAME")),
										   res.getString(m_cq.get("C_USERS_USER_EMAIL")),
										   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTLOGIN")).getTime(),
										   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTUSED")).getTime(),
										   res.getInt(m_cq.get("C_USERS_USER_FLAGS")),
										   info,
										   new CmsGroup(res.getInt(m_cq.get("C_GROUPS_GROUP_ID")),
														res.getInt(m_cq.get("C_GROUPS_PARENT_GROUP_ID")),
														res.getString(m_cq.get("C_GROUPS_GROUP_NAME")),
														res.getString(m_cq.get("C_GROUPS_GROUP_DESCRIPTION")),
														res.getInt(m_cq.get("C_GROUPS_GROUP_FLAGS"))),
										   res.getString(m_cq.get("C_USERS_USER_ADDRESS")),
										   res.getString(m_cq.get("C_USERS_USER_SECTION")),
										   res.getInt(m_cq.get("C_USERS_USER_TYPE")));
				users.addElement(user);
			}
		} catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
		} catch (Exception e) {
			throw new CmsException("["+this.getClass().getName()+"]", e);
		} finally {
			if (res != null) {
				try {
					res.close();
				} catch (SQLException se) {
				}
			}
			if( statement != null) {
				try {
					statement.close();
				} catch (SQLException exc) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
				}
			}
		}
		return users;
	}

	 /**
	 * Gets all users of a type and namefilter.
	 *
	 * @param type The type of the user.
	 * @param namestart The namefilter
	 * @exception thorws CmsException if something goes wrong.
	 */
	public Vector getUsers(int type, String namefilter)
		throws CmsException {
		//System.out.println("PL/SQL: getUsers");
		Vector users = new Vector();
	    Statement statement = null;
		Connection con = null;
		ResultSet res = null;

		try	{
			con = DriverManager.getConnection(m_poolName);
			statement = con.createStatement();

			/*statement.setInt(1,type);
			statement.setString(2,namefilter+"%");
			res = statement.executeQuery();*/

			//res = statement.executeQuery("SELECT * FROM CMS_USERS,CMS_GROUPS where USER_TYPE = "+type+" and USER_DEFAULT_GROUP_ID = GROUP_ID and USER_NAME like '"+namefilter+"%' ORDER BY USER_NAME");
			res = statement.executeQuery(m_cq.get("C_USERS_GETUSERS_FILTER1")+type+
                                         m_cq.get("C_USERS_GETUSERS_FILTER2")+namefilter+
                                         m_cq.get("C_USERS_GETUSERS_FILTER3"));
			// create new Cms user objects
			while( res.next() ) {
				// read the additional infos.
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.get("C_USERS_USER_INFO"));
				byte[] value = new byte[(int) blob.length()];
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				CmsUser user = new CmsUser(res.getInt(m_cq.get("C_USERS_USER_ID")),
										   res.getString(m_cq.get("C_USERS_USER_NAME")),
										   res.getString(m_cq.get("C_USERS_USER_PASSWORD")),
										   res.getString(m_cq.get("C_USERS_USER_RECOVERY_PASSWORD")),
										   res.getString(m_cq.get("C_USERS_USER_DESCRIPTION")),
										   res.getString(m_cq.get("C_USERS_USER_FIRSTNAME")),
										   res.getString(m_cq.get("C_USERS_USER_LASTNAME")),
										   res.getString(m_cq.get("C_USERS_USER_EMAIL")),
										   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTLOGIN")).getTime(),
										   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTUSED")).getTime(),
										   res.getInt(m_cq.get("C_USERS_USER_FLAGS")),
										   info,
										   new CmsGroup(res.getInt(m_cq.get("C_GROUPS_GROUP_ID")),
														res.getInt(m_cq.get("C_GROUPS_PARENT_GROUP_ID")),
														res.getString(m_cq.get("C_GROUPS_GROUP_NAME")),
														res.getString(m_cq.get("C_GROUPS_GROUP_DESCRIPTION")),
														res.getInt(m_cq.get("C_GROUPS_GROUP_FLAGS"))),
										   res.getString(m_cq.get("C_USERS_USER_ADDRESS")),
										   res.getString(m_cq.get("C_USERS_USER_SECTION")),
										   res.getInt(m_cq.get("C_USERS_USER_TYPE")));

				users.addElement(user);
			}

			//res.close();
		} catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
		} catch (Exception e) {
			throw new CmsException("["+this.getClass().getName()+"]", e);
		} finally {
			if (res != null) {
				try {
					res.close();
				} catch (SQLException se) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException exc) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
				}
			}
		}
		return users;
	}
/**
 * Returns a list of users of a group.<P/>
 *
 * @param name The name of the group.
 * @param type the type of the users to read.
 * @return Vector of users
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public Vector getUsersOfGroup(CmsUser currentUser, String name, int type) throws CmsException {
	//System.out.println("PL/SQL: getUsersOfGroup");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CmsGroup group;
	Vector users = new Vector();
	CallableStatement statement = null;
	Connection con = null;
	ResultSet res = null;
	try {
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareCall(cq.get("C_PLSQL_GROUPS_GETUSERSOFGROUP"));
		statement.registerOutParameter(1, oracle.jdbc.driver.OracleTypes.CURSOR);
		statement.setInt(2, currentUser.getId());
		statement.setString(3, name);
		statement.setInt(4, type);
		statement.execute();
		res = (ResultSet) statement.getObject(1);
		while (res.next()) {
			// read the additional infos.
			oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.get("C_USERS_USER_INFO"));
			byte[] value = new byte[(int) blob.length()];
			value = blob.getBytes(1, (int) blob.length());
			// now deserialize the object
			ByteArrayInputStream bin = new ByteArrayInputStream(value);
			ObjectInputStream oin = new ObjectInputStream(bin);
			Hashtable info = (Hashtable) oin.readObject();
			CmsUser user = new CmsUser(res.getInt(m_cq.get("C_USERS_USER_ID")),
                                       res.getString(m_cq.get("C_USERS_USER_NAME")),
                                       res.getString(m_cq.get("C_USERS_USER_PASSWORD")),
                                       res.getString(m_cq.get("C_USERS_USER_RECOVERY_PASSWORD")),
                                       res.getString(m_cq.get("C_USERS_USER_DESCRIPTION")),
                                       res.getString(m_cq.get("C_USERS_USER_FIRSTNAME")),
                                       res.getString(m_cq.get("C_USERS_USER_LASTNAME")),
                                       res.getString(m_cq.get("C_USERS_USER_EMAIL")),
                                       SqlHelper.getTimestamp(res, m_cq.get("C_USERS_USER_LASTLOGIN")).getTime(),
                                       SqlHelper.getTimestamp(res, m_cq.get("C_USERS_USER_LASTUSED")).getTime(),
                                       res.getInt(m_cq.get("C_USERS_USER_FLAGS")), info,
                                       new CmsGroup(res.getInt(m_cq.get("C_USERS_USER_DEFAULT_GROUP_ID")),
                                       res.getInt(m_cq.get("C_GROUPS_PARENT_GROUP_ID")),
                                       res.getString(m_cq.get("C_GROUPS_GROUP_NAME")),
                                       res.getString(m_cq.get("C_GROUPS_GROUP_DESCRIPTION")),
                                       res.getInt(m_cq.get("C_GROUPS_GROUP_FLAGS"))),
                                       res.getString(m_cq.get("C_USERS_USER_ADDRESS")),
                                       res.getString(m_cq.get("C_USERS_USER_SECTION")),
                                       res.getInt(m_cq.get("C_USERS_USER_TYPE")));
			users.addElement(user);
		}
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}
	return users;
}

 /**
     * Gets all users with a certain Lastname.
     *
     * @param Lastname      the start of the users lastname
     * @param UserType      webuser or systemuser
     * @param UserStatus    enabled, disabled
     * @param wasLoggedIn   was the user ever locked in?
     * @param nMax          max number of results
     *
     * @return the users.
     *
     * @exception CmsException if operation was not successful.
     */
    public Vector getUsersByLastname(String lastname, int userType,
                                     int userStatus, int wasLoggedIn, int nMax)
                                     throws CmsException {
        Vector users = new Vector();
	    PreparedStatement statement = null;
		ResultSet res = null;
		Connection con = null;
        int i = 0;
        // "" =  return (nearly) all users
        if(lastname == null) lastname = "";

		try	{
			con = DriverManager.getConnection(m_poolName);
            //con = DriverManager.getConnection("jdbc:opencmspool:oracle");

            if(wasLoggedIn == C_AT_LEAST_ONCE)
			    statement = con.prepareStatement(
                        m_cq.get("C_USERS_GETUSERS_BY_LASTNAME_ONCE"));
            else if(wasLoggedIn == C_NEVER)
                statement = con.prepareStatement(
                        m_cq.get("C_USERS_GETUSERS_BY_LASTNAME_NEVER"));
            else // C_WHATEVER or whatever else
                statement = con.prepareStatement(
                        m_cq.get("C_USERS_GETUSERS_BY_LASTNAME_WHATEVER"));

            statement.setString(1, lastname + "%");
            statement.setInt(2, userType);
            statement.setInt(3, userStatus);

			res = statement.executeQuery();
			// create new Cms user objects
			while( res.next() && (i++ < nMax)) {
				// read the additional infos.
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.get("C_USERS_USER_INFO"));
				byte[] value = new byte[(int) blob.length()];
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				CmsUser user = new CmsUser(res.getInt(m_cq.get("C_USERS_USER_ID")),
										   res.getString(m_cq.get("C_USERS_USER_NAME")),
										   res.getString(m_cq.get("C_USERS_USER_PASSWORD")),
										   res.getString(m_cq.get("C_USERS_USER_RECOVERY_PASSWORD")),
										   res.getString(m_cq.get("C_USERS_USER_DESCRIPTION")),
										   res.getString(m_cq.get("C_USERS_USER_FIRSTNAME")),
										   res.getString(m_cq.get("C_USERS_USER_LASTNAME")),
										   res.getString(m_cq.get("C_USERS_USER_EMAIL")),
										   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTLOGIN")).getTime(),
										   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTUSED")).getTime(),
										   res.getInt(m_cq.get("C_USERS_USER_FLAGS")),
										   info,
										   new CmsGroup(res.getInt(m_cq.get("C_GROUPS_GROUP_ID")),
														res.getInt(m_cq.get("C_GROUPS_PARENT_GROUP_ID")),
														res.getString(m_cq.get("C_GROUPS_GROUP_NAME")),
														res.getString(m_cq.get("C_GROUPS_GROUP_DESCRIPTION")),
														res.getInt(m_cq.get("C_GROUPS_GROUP_FLAGS"))),
										   res.getString(m_cq.get("C_USERS_USER_ADDRESS")),
										   res.getString(m_cq.get("C_USERS_USER_SECTION")),
										   res.getInt(m_cq.get("C_USERS_USER_TYPE")));

				users.addElement(user);
			}

			//res.close();
		} catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
		} catch (Exception e) {
			throw new CmsException("["+this.getClass().getName()+"]", e);
		} finally {
			if (res != null) {
				try {
					res.close();
				} catch (SQLException se) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException exc) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
				}
			}
		}
		return users;
	}

/**
 * Determines, if the users may manage a project.<BR/>
 * Only the manager of a project may publish it.
 *
 * <B>Security:</B>
 * All users are granted.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @return true, if the may manage this project.
 */
public boolean isManagerOfProject(CmsUser currentUser, CmsProject currentProject) throws CmsException {
	//System.out.println("PL/SQL: isManagerOfProject");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	Connection con = null;
	try {
		// create the statement
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareCall(cq.get("C_PLSQL_GROUPS_ISMANAGEROFPROJECT"));
		statement.registerOutParameter(1, Types.INTEGER);
		statement.setInt(2, currentUser.getId());
		statement.setInt(3, currentProject.getId());
		statement.execute();
		if (statement.getInt(1) == 1) {
			return true;
		} else {
			return false;
		}
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}
}
/**
  * Deletes a project from the cms.
  * Therefore it deletes all files, resources and properties.
  *
  * @param project the project to delete.
  * @exception CmsException Throws CmsException if something goes wrong.
  */
public Vector lockResource(CmsUser currentUser, CmsProject currentProject, String resourcename, boolean force) throws CmsException {
	//System.out.println("PL/SQL: lockResource");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	Connection con = null;
	ResultSet res = null;
	Vector resources = new Vector();
	CmsResource resource = null;
	try {
		// create the statement
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareCall(cq.get("C_PLSQL_RESOURCES_LOCKRESOURCE"));
		statement.setInt(1, currentUser.getId());
		statement.setInt(2, currentProject.getId());
		statement.setString(3, resourcename);
		if (force == true) {
			statement.setString(4, "TRUE");
		} else {
			statement.setString(4, "FALSE");
		}
		statement.registerOutParameter(5, oracle.jdbc.driver.OracleTypes.CURSOR);
		statement.execute();
		res = (ResultSet) statement.getObject(5);
		while (res.next()) {
			int resId = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_ID"));
			int parentId = res.getInt(m_cq.get("C_RESOURCES_PARENT_ID"));
			String resName = res.getString(m_cq.get("C_RESOURCES_RESOURCE_NAME"));
			int resType = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_TYPE"));
			int resFlags = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_FLAGS"));
			int userId = res.getInt(m_cq.get("C_RESOURCES_USER_ID"));
			int groupId = res.getInt(m_cq.get("C_RESOURCES_GROUP_ID"));
			int projectId = res.getInt(m_cq.get("C_RESOURCES_PROJECT_ID"));
			int fileId = res.getInt(m_cq.get("C_RESOURCES_FILE_ID"));
			int accessFlags = res.getInt(m_cq.get("C_RESOURCES_ACCESS_FLAGS"));
			int state = res.getInt(m_cq.get("C_RESOURCES_STATE"));
			int lockedBy = res.getInt(m_cq.get("C_RESOURCES_LOCKED_BY"));
			int launcherType = res.getInt(m_cq.get("C_RESOURCES_LAUNCHER_TYPE"));
			String launcherClass = res.getString(m_cq.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
			long created = SqlHelper.getTimestamp(res, m_cq.get("C_RESOURCES_DATE_CREATED")).getTime();
			long modified = SqlHelper.getTimestamp(res, m_cq.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
			int modifiedBy = res.getInt(m_cq.get("C_RESOURCES_LASTMODIFIED_BY"));
			int resSize = res.getInt(m_cq.get("C_RESOURCES_SIZE"));
			resource = new CmsResource(resId, parentId, fileId, resName, resType, resFlags, userId,
										groupId, projectId, accessFlags, state, lockedBy, launcherType,
										launcherClass, created, modified, modifiedBy, resSize);
			resources.addElement(resource);
		}
		//res.close();
		//statement.close();
		return resources;
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}
}
/**
 * Publishes a project.
 *
 * <B>Security</B>
 * Only the admin or the owner of the project can do this.
 *
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param projectId The id of the project to be published.
 * @return a vector of changed resources.
 *
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public Vector publishProject(CmsUser currentUser, int projectId, CmsProject onlineProject, boolean enableHistory) throws CmsException {
	//System.out.println("PL/SQL: publishProject");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CmsAccessFilesystem discAccess = new CmsAccessFilesystem(m_exportpointStorage);
	CallableStatement statement = null;
	Connection con = null;
	ResultSet res1 = null;
	ResultSet res2 = null;
	ResultSet res3 = null;
	ResultSet res4 = null;
    Vector changedResources = new Vector();
    int enableHistoryInt = 1; // enable history
    if (!enableHistory){
        enableHistoryInt = 0; // disable history
    }
	try {
		// create the statement
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareCall(cq.get("C_PLSQL_PROJECTS_PUBLISHPROJECT"));
		statement.setInt(1, currentUser.getId());
		statement.setInt(2, projectId);
		statement.setInt(3, onlineProject.getId());
        statement.setInt(4, enableHistoryInt);
        statement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
        //statement.setString(5, Utils.getNiceDate(System.currentTimeMillis()));
		statement.registerOutParameter(6, oracle.jdbc.driver.OracleTypes.CURSOR);
		statement.registerOutParameter(7, oracle.jdbc.driver.OracleTypes.CURSOR);
		statement.registerOutParameter(8, oracle.jdbc.driver.OracleTypes.CURSOR);
		statement.registerOutParameter(9, oracle.jdbc.driver.OracleTypes.CURSOR);
		statement.execute();
		// now export to filesystem if necessary
		// for deleted folder
		res1 = (ResultSet) statement.getObject(6);
		while (res1.next()) {
			String exportKey = checkExport(res1.getString("RESOURCE_NAME"));
			if (exportKey != null) {
				discAccess.removeResource(res1.getString("RESOURCE_NAME"), exportKey);
			}
		}
		// for changed/new folder
		res2 = (ResultSet) statement.getObject(7);
		while (res2.next()) {
            changedResources.add(res2.getString("RESOURCE_NAME"));
			String exportKey = checkExport(res2.getString("RESOURCE_NAME"));
			if (exportKey != null) {
				discAccess.createFolder(res2.getString("RESOURCE_NAME"), exportKey);
			}
		}
		// for deleted files
		res3 = (ResultSet) statement.getObject(8);
		while (res3.next()) {
            changedResources.add(res3.getString("RESOURCE_NAME"));
			String exportKey = checkExport(res3.getString("RESOURCE_NAME"));
			if (exportKey != null) {
				discAccess.removeResource(res3.getString("RESOURCE_NAME"), exportKey);
			}
		}
		// for changed/new files
		res4 = (ResultSet) statement.getObject(9);
		while (res4.next()) {
            changedResources.add(res4.getString("RESOURCE_NAME"));
			String exportKey = checkExport(res4.getString("RESOURCE_NAME"));
			if (exportKey != null) {
				discAccess.writeFile(res4.getString("RESOURCE_NAME"), exportKey, readFileContent(projectId, res4.getInt("FILE_ID")));
			}
		}
        res1.close();
        res2.close();
        res3.close();
        res4.close();
        statement.close();
        // now delete the deleted files and folder from offline project and
        // set state = unchanged for all other resources in offline project
        statement = con.prepareCall(cq.get("C_PLSQL_RESOURCES_REMOVEALLDELETED"));
		statement.setInt(1, C_STATE_DELETED);
        statement.setInt(2, projectId);
		statement.executeUpdate();
        statement.close();
        statement = con.prepareCall(cq.get("C_PLSQL_RESOURCES_SETALLUNCHANGED"));
		statement.setInt(1, C_STATE_UNCHANGED);
        statement.setInt(2, projectId);
		statement.executeUpdate();
        statement.close();
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (res1 != null) {
			try {
				res1.close();
			} catch (SQLException se) {
			}
		}
		if (res2 != null) {
			try {
				res2.close();
			} catch (SQLException se) {
			}
		}
		if (res3 != null) {
			try {
				res3.close();
			} catch (SQLException se) {
			}
		}
		if (res4 != null) {
			try {
				res4.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}
        return changedResources;
}
/**
 * Reads a file from the Cms.<BR/>
 *
 * @param projectId The Id of the project in which the resource will be used.
 * @param onlineProjectId The online projectId of the OpenCms.
 * @param filename The complete name of the new file (including pathinformation).
 *
 * @return file The read file.
 *
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public CmsFile readFile(int currentUserId, int currentProjectId, int onlineProjectId, String filename) throws CmsException {
	//System.out.println("PL/SQL: readFile");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CmsFile file = null;
	CallableStatement statement = null;
	Connection con = null;
	ResultSet res = null;
	try {
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareCall(cq.get("C_PLSQL_RESOURCES_READFILE"));
		statement.registerOutParameter(1, oracle.jdbc.driver.OracleTypes.CURSOR);
		statement.setInt(2, currentUserId);
		statement.setInt(3, currentProjectId);
		statement.setInt(4, onlineProjectId);
		statement.setString(5, filename);
		statement.execute();
		res = (ResultSet) statement.getObject(1);
		if (res.next()) {
			int resId = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_ID"));
			int parentId = res.getInt(m_cq.get("C_RESOURCES_PARENT_ID"));
			int resType = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_TYPE"));
			int resFlags = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_FLAGS"));
			int userId = res.getInt(m_cq.get("C_RESOURCES_USER_ID"));
			int groupId = res.getInt(m_cq.get("C_RESOURCES_GROUP_ID"));
			int fileId = res.getInt(m_cq.get("C_RESOURCES_FILE_ID"));
			int accessFlags = res.getInt(m_cq.get("C_RESOURCES_ACCESS_FLAGS"));
			int state = res.getInt(m_cq.get("C_RESOURCES_STATE"));
			int lockedBy = res.getInt(m_cq.get("C_RESOURCES_LOCKED_BY"));
			int launcherType = res.getInt(m_cq.get("C_RESOURCES_LAUNCHER_TYPE"));
			String launcherClass = res.getString(m_cq.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
			long created = SqlHelper.getTimestamp(res, m_cq.get("C_RESOURCES_DATE_CREATED")).getTime();
			long modified = SqlHelper.getTimestamp(res, m_cq.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
			int modifiedBy = res.getInt(m_cq.get("C_RESOURCES_LASTMODIFIED_BY"));
			int resSize = res.getInt(m_cq.get("C_RESOURCES_SIZE"));
            int resProjectId = res.getInt(m_cq.get("C_RESOURCES_PROJECT_ID"));
			// read the file_content from an oracle blob
			oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB(m_cq.get("C_RESOURCES_FILE_CONTENT"));
			byte[] content = new byte[ (int) blob.length()];
			content = blob.getBytes(1, (int) blob.length());
			// output for testing:
			//		String out_buffer = new String(content);
			//		System.out.println(out_buffer);
			file = new CmsFile(resId, parentId, fileId, filename, resType, resFlags, userId, groupId,
                               resProjectId, accessFlags, state, lockedBy, launcherType,
                               launcherClass, created, modified, modifiedBy, content, resSize);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
		}
	} catch (SQLException e) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", e);
		throw cmsException;
		//throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (CmsException ex) {
		throw ex;
	} catch (Exception exc) {
		throw new CmsException("readFile " + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}
	return file;
}
/**
 * Reads a file from the Cms.<BR/>
 *
 * @param projectId The Id of the project in which the resource will be used.
 * @param onlineProjectId The online projectId of the OpenCms.
 * @param filename The complete name of the new file (including pathinformation).
 *
 * @return file The read file.
 *
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public CmsFile readFile(int currentUserId, int currentProjectId, String filename) throws CmsException {
	//System.out.println("PL/SQL: readFile");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CmsFile file = null;
	CallableStatement statement = null;
	Connection con = null;
	ResultSet res = null;
	try {
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareCall(cq.get("C_PLSQL_RESOURCES_READFILEACC"));
		statement.registerOutParameter(1, oracle.jdbc.driver.OracleTypes.CURSOR);
		statement.setInt(2, currentUserId);
		statement.setInt(3, currentProjectId);
		statement.setString(4, filename);
		statement.execute();
		res = (ResultSet) statement.getObject(1);
		if (res.next()) {
			int resId = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_ID"));
			int parentId = res.getInt(m_cq.get("C_RESOURCES_PARENT_ID"));
			int resType = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_TYPE"));
			int resFlags = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_FLAGS"));
			int userId = res.getInt(m_cq.get("C_RESOURCES_USER_ID"));
			int groupId = res.getInt(m_cq.get("C_RESOURCES_GROUP_ID"));
			int fileId = res.getInt(m_cq.get("C_RESOURCES_FILE_ID"));
			int accessFlags = res.getInt(m_cq.get("C_RESOURCES_ACCESS_FLAGS"));
			int state = res.getInt(m_cq.get("C_RESOURCES_STATE"));
			int lockedBy = res.getInt(m_cq.get("C_RESOURCES_LOCKED_BY"));
			int launcherType = res.getInt(m_cq.get("C_RESOURCES_LAUNCHER_TYPE"));
			String launcherClass = res.getString(m_cq.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
			long created = SqlHelper.getTimestamp(res, m_cq.get("C_RESOURCES_DATE_CREATED")).getTime();
			long modified = SqlHelper.getTimestamp(res, m_cq.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
			int modifiedBy = res.getInt(m_cq.get("C_RESOURCES_LASTMODIFIED_BY"));
			int resSize = res.getInt(m_cq.get("C_RESOURCES_SIZE"));
            int resProjectId = res.getInt(m_cq.get("C_RESOURCES_PROJECT_ID"));
			// read the file_content from an oracle blob
			oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB(m_cq.get("C_RESOURCES_FILE_CONTENT"));
			byte[] content = new byte[ (int) blob.length()];
			content = blob.getBytes(1, (int) blob.length());
			// output for testing:
			//		String out_buffer = new String(content);
			//		System.out.println(out_buffer);

			file = new CmsFile(resId, parentId, fileId, filename, resType, resFlags, userId, groupId,
								resProjectId, accessFlags, state, lockedBy, launcherType, launcherClass,
								created, modified, modifiedBy, content, resSize);
		} else {
			throw new CmsException("[" + this.getClass().getName() + "] " + filename, CmsException.C_NOT_FOUND);
		}
	} catch (SQLException e) {
		//throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", e);
		throw cmsException;
	} catch (CmsException ex) {
		throw ex;
	} catch (Exception exc) {
		throw new CmsException("readFile " + exc.getMessage(), CmsException.C_UNKNOWN_EXCEPTION, exc);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}
	return file;
}
	/**
	 * Private helper method to read the fileContent for publishProject(export).
	 *
	 * @param fileId the fileId.
	 *
	 * @exception CmsException  Throws CmsException if operation was not succesful.
	 */
	protected byte[] readFileContent(int projectId, int fileId)
		throws CmsException {
		//System.out.println("PL/SQL: readFileContent");
		PreparedStatement statement = null;
		Connection con = null;
		ResultSet res = null;
		byte[] returnValue = null;
        int onlineProject = getOnlineProject(projectId).getId();
        String usedPool;
        String usedStatement;
        if (projectId == onlineProject){
            usedPool = m_poolNameOnline;
            usedStatement = "_ONLINE";
        } else {
            usedPool = m_poolName;
            usedStatement = "";
        }

		try {
			// read fileContent from database
			con = DriverManager.getConnection(usedPool);
			statement = con.prepareStatement(m_cq.get("C_FILE_READ"+usedStatement));
			statement.setInt(1,fileId);
			res = statement.executeQuery();
			if (res.next()) {
				oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB(m_cq.get("C_FILE_CONTENT"));
				byte[] content = new byte[(int) blob.length()];
				content = blob.getBytes(1, (int) blob.length());
				returnValue = content;
// output for testing:
//		String out_buffer = new String(content);
//		System.out.println(out_buffer);
				} else {
				  throw new CmsException("["+this.getClass().getName()+"]"+fileId,CmsException.C_NOT_FOUND);
			}
		} catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
		}finally {
			if (res != null) {
				try {
					res.close();
				} catch (SQLException se) {
				}
			}
			if( statement != null) {
				try {
					statement.close();
				} catch (SQLException exc) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
				}
			}
		}
		return returnValue;
	}
/**
 * Reads a session from the database.
 *
 * @param sessionId, the id og the session to read.
 * @return the read session as Hashtable.
 * @exception thorws CmsException if something goes wrong.
 */
public Hashtable readSession(String sessionId) throws CmsException {
	//System.out.println("PL/SQL: readSession");
	PreparedStatement statement = null;
	Connection con = null;
	ResultSet res = null;
	Hashtable session = null;
	try {
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareStatement(m_cq.get("C_SESSION_READ"));
		statement.setString(1, sessionId);
		statement.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis() - C_SESSION_TIMEOUT));
		res = statement.executeQuery();

		// create new Cms user object
		if (res.next()) {
			// read the additional infos.
			oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SESSION_DATA");
			byte[] value = new byte[ (int) blob.length()];
			value = blob.getBytes(1, (int) blob.length());
			// now deserialize the object
			ByteArrayInputStream bin = new ByteArrayInputStream(value);
			ObjectInputStream oin = new ObjectInputStream(bin);
			session = (Hashtable) oin.readObject();
		} else {
			deleteSessions();
		}
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}
	return session;
}
	 /**
	 * Reads a serializable object from the systempropertys.
	 *
	 * @param name The name of the property.
	 *
	 * @return object The property-object.
	 *
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	public Serializable readSystemProperty(String name)
		throws CmsException {
		//System.out.println("PL/SQL: readSystemProperty");
		Serializable property=null;

		ResultSet res = null;
		PreparedStatement statement = null;
		Connection con = null;

		// create get the property data from the database
		try {
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareStatement(m_cq.get("C_SYSTEMPROPERTIES_READ"));
			statement.setString(1,name);
			res = statement.executeQuery();
			if(res.next()) {
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.get("C_SYSTEMPROPERTY_VALUE"));
				byte[] value = new byte[(int) blob.length()];
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				property=(Serializable)oin.readObject();
			}
		}
		catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
		catch (IOException e){
			throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);
		}
	    catch (ClassNotFoundException e){
			throw new CmsException("["+this.getClass().getName()+"]"+CmsException. C_SERIALIZATION, e);
		}finally {
			if (res != null) {
				try {
					res.close();
				} catch (SQLException se) {
				}
			}
			if( statement != null) {
				try {
					statement.close();
				} catch (SQLException exc) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
				}
			}
		}
		return property;
	}
	/**
	 * Reads a user from the cms, only if the password is correct.
	 *
	 * @param id the id of the user.
	 * @param type the type of the user.
	 * @return the read user.
	 * @exception thorws CmsException if something goes wrong.
	 */
	public CmsUser readUser(int id)
		throws CmsException {
		//System.out.println("PL/SQL: readUser");
		PreparedStatement statement = null;
		Connection con = null;
		ResultSet res = null;
		CmsUser user = null;

		try	{
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareStatement(m_cq.get("C_USERS_READID"));
			statement.setInt(1,id);
			res = statement.executeQuery();

			// create new Cms user object
			if(res.next()) {
				// read the additional infos.
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.get("C_USERS_USER_INFO"));
				byte[] value = new byte[(int) blob.length()];
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				user = new CmsUser(res.getInt(m_cq.get("C_USERS_USER_ID")),
								   res.getString(m_cq.get("C_USERS_USER_NAME")),
								   res.getString(m_cq.get("C_USERS_USER_PASSWORD")),
								   res.getString(m_cq.get("C_USERS_USER_RECOVERY_PASSWORD")),
								   res.getString(m_cq.get("C_USERS_USER_DESCRIPTION")),
								   res.getString(m_cq.get("C_USERS_USER_FIRSTNAME")),
								   res.getString(m_cq.get("C_USERS_USER_LASTNAME")),
								   res.getString(m_cq.get("C_USERS_USER_EMAIL")),
								   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTLOGIN")).getTime(),
								   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTUSED")).getTime(),
								   res.getInt(m_cq.get("C_USERS_USER_FLAGS")),
								   info,
								   new CmsGroup(res.getInt(m_cq.get("C_GROUPS_GROUP_ID")),
												res.getInt(m_cq.get("C_GROUPS_PARENT_GROUP_ID")),
												res.getString(m_cq.get("C_GROUPS_GROUP_NAME")),
												res.getString(m_cq.get("C_GROUPS_GROUP_DESCRIPTION")),
												res.getInt(m_cq.get("C_GROUPS_GROUP_FLAGS"))),
								   res.getString(m_cq.get("C_USERS_USER_ADDRESS")),
								   res.getString(m_cq.get("C_USERS_USER_SECTION")),
								   res.getInt(m_cq.get("C_USERS_USER_TYPE")));
			} else {
				res.close();
				res = null;
				throw new CmsException("["+this.getClass().getName()+"]"+id,CmsException.C_NO_USER);
			}
			return user;
		 }
		catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
		// a.lucas: catch CmsException here and throw it again.
		// Don't wrap another CmsException around it, since this may cause problems during login.
		catch (CmsException e) {
			throw e;
		}
		catch (Exception e) {
			throw new CmsException("["+this.getClass().getName()+"]", e);
		} finally {
			if (res != null) {
				try {
					res.close();
				} catch (SQLException se) {
				}
			}
			if( statement != null) {
				try {
					statement.close();
				} catch (SQLException exc) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
				}
			}
		}
	}
	/**
	 * Reads a user from the cms.
	 *
	 * @param name the name of the user.
	 * @param type the type of the user.
	 * @return the read user.
	 * @exception thorws CmsException if something goes wrong.
	 */
	public CmsUser readUser(String name, int type)
		throws CmsException {
		//System.out.println("PL/SQL: readUser");
		PreparedStatement statement = null;
		Connection con = null;
		ResultSet res = null;
		CmsUser user = null;

		try	{
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareStatement(m_cq.get("C_USERS_READ"));
			statement.setString(1,name);
			statement.setInt(2,type);

			res = statement.executeQuery();

			// create new Cms user object
			if(res.next()) {
				// read the additional infos.
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.get("C_USERS_USER_INFO"));
				byte[] value = new byte[(int) blob.length()];
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				user = new CmsUser(res.getInt(m_cq.get("C_USERS_USER_ID")),
								   res.getString(m_cq.get("C_USERS_USER_NAME")),
								   res.getString(m_cq.get("C_USERS_USER_PASSWORD")),
								   res.getString(m_cq.get("C_USERS_USER_RECOVERY_PASSWORD")),
								   res.getString(m_cq.get("C_USERS_USER_DESCRIPTION")),
								   res.getString(m_cq.get("C_USERS_USER_FIRSTNAME")),
								   res.getString(m_cq.get("C_USERS_USER_LASTNAME")),
								   res.getString(m_cq.get("C_USERS_USER_EMAIL")),
								   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTLOGIN")).getTime(),
								   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTUSED")).getTime(),
								   res.getInt(m_cq.get("C_USERS_USER_FLAGS")),
								   info,
								   new CmsGroup(res.getInt(m_cq.get("C_GROUPS_GROUP_ID")),
												res.getInt(m_cq.get("C_GROUPS_PARENT_GROUP_ID")),
												res.getString(m_cq.get("C_GROUPS_GROUP_NAME")),
												res.getString(m_cq.get("C_GROUPS_GROUP_DESCRIPTION")),
												res.getInt(m_cq.get("C_GROUPS_GROUP_FLAGS"))),
								   res.getString(m_cq.get("C_USERS_USER_ADDRESS")),
								   res.getString(m_cq.get("C_USERS_USER_SECTION")),
								   res.getInt(m_cq.get("C_USERS_USER_TYPE")));
			} else {
				res.close();
				res = null;
				throw new CmsException("["+this.getClass().getName()+"]"+name,CmsException.C_NO_USER);
			}
			return user;
		 }
		catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
		// a.lucas: catch CmsException here and throw it again.
		// Don't wrap another CmsException around it, since this may cause problems during login.
		catch (CmsException e) {
			throw e;
		}
		catch (Exception e) {
			throw new CmsException("["+this.getClass().getName()+"]", e);
		} finally {
			if (res != null) {
				try {
					res.close();
				} catch (SQLException se) {
				}
			}
			if( statement != null) {
				try {
					statement.close();
				} catch (SQLException exc) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
				}
			}
		}
	}
	/**
	 * Reads a user from the cms, only if the password is correct.
	 *
	 * @param name the name of the user.
	 * @param password the password of the user.
	 * @param type the type of the user.
	 * @return the read user.
	 * @exception thorws CmsException if something goes wrong.
	 */
	public CmsUser readUser(String name, String password, int type)
		throws CmsException {
		//System.out.println("PL/SQL: readUser");
		PreparedStatement statement = null;
		Connection con = null;
		ResultSet res = null;
		CmsUser user = null;

		try	{
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareStatement(m_cq.get("C_USERS_READPW"));
			statement.setString(1,name);
			statement.setString(2,digest(password));
			statement.setInt(3,type);
			res = statement.executeQuery();

			// create new Cms user object
			if(res.next()) {
				// read the additional infos.
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.get("C_USERS_USER_INFO"));
				byte[] value = new byte[(int) blob.length()];
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				user = new CmsUser(res.getInt(m_cq.get("C_USERS_USER_ID")),
								   res.getString(m_cq.get("C_USERS_USER_NAME")),
								   res.getString(m_cq.get("C_USERS_USER_PASSWORD")),
								   res.getString(m_cq.get("C_USERS_USER_RECOVERY_PASSWORD")),
								   res.getString(m_cq.get("C_USERS_USER_DESCRIPTION")),
								   res.getString(m_cq.get("C_USERS_USER_FIRSTNAME")),
								   res.getString(m_cq.get("C_USERS_USER_LASTNAME")),
								   res.getString(m_cq.get("C_USERS_USER_EMAIL")),
								   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTLOGIN")).getTime(),
								   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTUSED")).getTime(),
								   res.getInt(m_cq.get("C_USERS_USER_FLAGS")),
								   info,
								   new CmsGroup(res.getInt(m_cq.get("C_GROUPS_GROUP_ID")),
												res.getInt(m_cq.get("C_GROUPS_PARENT_GROUP_ID")),
												res.getString(m_cq.get("C_GROUPS_GROUP_NAME")),
												res.getString(m_cq.get("C_GROUPS_GROUP_DESCRIPTION")),
												res.getInt(m_cq.get("C_GROUPS_GROUP_FLAGS"))),

								   res.getString(m_cq.get("C_USERS_USER_ADDRESS")),
								   res.getString(m_cq.get("C_USERS_USER_SECTION")),
								   res.getInt(m_cq.get("C_USERS_USER_TYPE")));
			} else {
				res.close();
				res = null;
				throw new CmsException("["+this.getClass().getName()+"]"+name,CmsException.C_NO_USER);
			}
			return user;
		 }
		catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
		// a.lucas: catch CmsException here and throw it again.
		// Don't wrap another CmsException around it, since this may cause problems during login.
		catch (CmsException e) {
			throw e;
		}
		catch (Exception e) {
			throw new CmsException("["+this.getClass().getName()+"]", e);
		} finally {
			if (res != null) {
				try {
					res.close();
				} catch (SQLException se) {
				}
			}
			if( statement != null) {
				try {
					statement.close();
				} catch (SQLException exc) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
				}
			}
		}
	}
	/**
	 * Reads the user, which is agent for the role, from the cms, only if the password is correct.
	 *
	 * @param id the id of the role.
	 * @param type the type of the user.
	 * @return the read user.
	 * @exception throws CmsException if something goes wrong.
	 */
	public CmsUser readUserAgent(int roleId)
		throws CmsException {
		//System.out.println("PL/SQL: readUserAgent");
		int agentId = this.findAgent(roleId);

		PreparedStatement statement = null;
		Connection con = null;
		ResultSet res = null;
		CmsUser user = null;

		try	{
			con = DriverManager.getConnection(m_poolName);
			statement = con.prepareStatement(m_cq.get("C_USERS_READID"));
			statement.setInt(1,agentId);
			res = statement.executeQuery();

			// create new Cms user object
			if(res.next()) {
				// read the additional infos.
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.get("C_USERS_USER_INFO"));
				byte[] value = new byte[(int) blob.length()];
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				user = new CmsUser(res.getInt(m_cq.get("C_USERS_USER_ID")),
								   res.getString(m_cq.get("C_USERS_USER_NAME")),
								   res.getString(m_cq.get("C_USERS_USER_PASSWORD")),
								   res.getString(m_cq.get("C_USERS_USER_RECOVERY_PASSWORD")),
								   res.getString(m_cq.get("C_USERS_USER_DESCRIPTION")),
								   res.getString(m_cq.get("C_USERS_USER_FIRSTNAME")),
								   res.getString(m_cq.get("C_USERS_USER_LASTNAME")),
								   res.getString(m_cq.get("C_USERS_USER_EMAIL")),
								   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTLOGIN")).getTime(),
								   SqlHelper.getTimestamp(res,m_cq.get("C_USERS_USER_LASTUSED")).getTime(),
								   res.getInt(m_cq.get("C_USERS_USER_FLAGS")),
								   info,
								   new CmsGroup(res.getInt(m_cq.get("C_GROUPS_GROUP_ID")),
												res.getInt(m_cq.get("C_GROUPS_PARENT_GROUP_ID")),
												res.getString(m_cq.get("C_GROUPS_GROUP_NAME")),
												res.getString(m_cq.get("C_GROUPS_GROUP_DESCRIPTION")),
												res.getInt(m_cq.get("C_GROUPS_GROUP_FLAGS"))),
								   res.getString(m_cq.get("C_USERS_USER_ADDRESS")),
								   res.getString(m_cq.get("C_USERS_USER_SECTION")),
								   res.getInt(m_cq.get("C_USERS_USER_TYPE")));
			} else {
				res.close();
				res = null;
				throw new CmsException("["+this.getClass().getName()+"]"+roleId,CmsException.C_NO_USER);
			}
			return user;
		 }
		catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
		// a.lucas: catch CmsException here and throw it again.
		// Don't wrap another CmsException around it, since this may cause problems during login.
		catch (CmsException e) {
			throw e;
		}
		catch (Exception e) {
			throw new CmsException("["+this.getClass().getName()+"]", e);
		} finally {
			if (res != null) {
				try {
					res.close();
				} catch (SQLException se) {
				}
			}
			if( statement != null) {
				try {
					statement.close();
				} catch (SQLException exc) {
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException se) {
				}
			}
		}
	}
/**
  * Deletes a project from the cms.
  * Therefore it deletes all files, resources and properties.
  *
  * @param project the project to delete.
  * @exception CmsException Throws CmsException if something goes wrong.
  */
public Vector unlockResource(CmsUser currentUser, CmsProject currentProject, String resourcename) throws CmsException {
	//System.out.println("PL/SQL: unlockResource");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	Connection con = null;
	ResultSet res = null;
	CmsResource resource = null;
	Vector resources = new Vector();
	try {
		con = DriverManager.getConnection(m_poolName);
		// create the statement
		statement = con.prepareCall(cq.get("C_PLSQL_RESOURCES_UNLOCKRESOURCE"));
		statement.setInt(1, currentUser.getId());
		statement.setInt(2, currentProject.getId());
		statement.setString(3, resourcename);
		statement.registerOutParameter(4, oracle.jdbc.driver.OracleTypes.CURSOR);
		statement.execute();
		res = (ResultSet) statement.getObject(4);
		while (res.next()) {
			int resId = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_ID"));
			int parentId = res.getInt(m_cq.get("C_RESOURCES_PARENT_ID"));
			String resName = res.getString(m_cq.get("C_RESOURCES_RESOURCE_NAME"));
			int resType = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_TYPE"));
			int resFlags = res.getInt(m_cq.get("C_RESOURCES_RESOURCE_FLAGS"));
			int userId = res.getInt(m_cq.get("C_RESOURCES_USER_ID"));
			int groupId = res.getInt(m_cq.get("C_RESOURCES_GROUP_ID"));
			int projectId = res.getInt(m_cq.get("C_RESOURCES_PROJECT_ID"));
			int fileId = res.getInt(m_cq.get("C_RESOURCES_FILE_ID"));
			int accessFlags = res.getInt(m_cq.get("C_RESOURCES_ACCESS_FLAGS"));
			int state = res.getInt(m_cq.get("C_RESOURCES_STATE"));
			int lockedBy = res.getInt(m_cq.get("C_RESOURCES_LOCKED_BY"));
			int launcherType = res.getInt(m_cq.get("C_RESOURCES_LAUNCHER_TYPE"));
			String launcherClass = res.getString(m_cq.get("C_RESOURCES_LAUNCHER_CLASSNAME"));
			long created = SqlHelper.getTimestamp(res, m_cq.get("C_RESOURCES_DATE_CREATED")).getTime();
			long modified = SqlHelper.getTimestamp(res, m_cq.get("C_RESOURCES_DATE_LASTMODIFIED")).getTime();
			int modifiedBy = res.getInt(m_cq.get("C_RESOURCES_LASTMODIFIED_BY"));
			int resSize = res.getInt(m_cq.get("C_RESOURCES_SIZE"));
			resource = new CmsResource(resId, parentId, fileId, resName, resType, resFlags, userId, groupId,
										projectId, accessFlags, state, lockedBy, launcherType, launcherClass,
										created, modified, modifiedBy, resSize);
			resources.addElement(resource);
		}
		return resources;
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}
}
/**
 * This method updates a session in the database. It is used
 * for sessionfailover.
 *
 * @param sessionId the id of the session.
 * @return data the sessionData.
 */
public int updateSession(String sessionId, Hashtable data) throws CmsException {
	//System.out.println("PL/SQL: updateSession");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	byte[] value = null;
	PreparedStatement statement = null;
	PreparedStatement statement2 = null;
	PreparedStatement nextStatement = null;
	//OraclePreparedStatement trimStatement = null;
        PreparedStatement trimStatement = null;
	Connection con = null;
	int retValue;
	ResultSet res = null;
	try {
		// serialize the hashtable
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(data);
		oout.close();
		value = bout.toByteArray();

		// write data to database in two steps because of using Oracle BLOB
		// first update the session_time
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareStatement(cq.get("C_PLSQL_SESSION_UPDATE"));
		statement.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
		statement.setString(2, sessionId);
		retValue = statement.executeUpdate();
		statement.close();
		// now update the session_data
		statement2 = con.prepareStatement(cq.get("C_PLSQL_SESSION_FORUPDATE"));
		statement2.setString(1, sessionId);
		con.setAutoCommit(false);
		res = statement2.executeQuery();
		while (res.next()) {
			oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SESSION_DATA");
			// first trim the blob to 0 bytes, otherwise there could be left some bytes
			// of the old content
			//trimStatement = (OraclePreparedStatement) con.prepareStatement(cq.get("C_TRIMBLOB);
			//trimStatement.setBLOB(1, blob);
                        trimStatement = con.prepareStatement(cq.get("C_TRIMBLOB"));
			trimStatement.setBlob(1, blob);
			trimStatement.setInt(2, 0);
			trimStatement.execute();
			ByteArrayInputStream instream = new ByteArrayInputStream(value);
			OutputStream outstream = blob.getBinaryOutputStream();
			byte[] chunk = new byte[blob.getChunkSize()];
			int i = -1;
			while ((i = instream.read(chunk)) != -1) {
				outstream.write(chunk, 0, i);
			}
			instream.close();
			outstream.close();
		}
		statement2.close();
		res.close();
		// for the oracle-driver commit or rollback must be executed manually
		// because setAutoCommit = false in CmsDbPool.CmsDbPool
		nextStatement = con.prepareStatement(cq.get("C_COMMIT"));
		nextStatement.execute();
		nextStatement.close();
		con.setAutoCommit(true);
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (IOException e) {
		throw new CmsException("[" + this.getClass().getName() + "]:" + CmsException.C_SERIALIZATION, e);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (statement2 != null) {
			try {
				statement2.close();
			} catch (SQLException exc) {
			}
			try {
				nextStatement = con.prepareStatement(cq.get("C_ROLLBACK"));
				nextStatement.execute();
			} catch (SQLException se) {
			}
		}
		if (nextStatement != null) {
			try {
				nextStatement.close();
			} catch (SQLException exc) {
			}
		}
		if (trimStatement != null) {
			try {
				trimStatement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.setAutoCommit(true);
			} catch (SQLException se) {
			}
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}
	return retValue;
}
/**
 * Checks if a user is member of a group.<P/>
 *
 * @param nameid The id of the user to check.
 * @param groupid The id of the group to check.
 * @return True or False
 *
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public boolean userInGroup(int userid, int groupid) throws CmsException {
	//System.out.println("PL/SQL: userInGroup");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	boolean userInGroup = false;
	CallableStatement statement = null;
	Connection con = null;
	try {
		// create statement
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareCall(cq.get("C_PLSQL_GROUPS_USERINGROUP"));
		statement.registerOutParameter(1, Types.INTEGER);
		statement.setInt(2, userid);
		statement.setInt(3, groupid);
		statement.execute();
		if (statement.getInt(1) == 1) {
			userInGroup = true;
		}
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}
	return userInGroup;
}
/**
 * Writes a file to the Cms.<BR/>
 *
 * @param project The project in which the resource will be used.
 * @param onlineProject The online project of the OpenCms.
 * @param file The new file.
 * @param changed Flag indicating if the file state must be set to changed.
 *
 * @exception CmsException Throws CmsException if operation was not succesful.
 */
public void writeFile(CmsProject project, CmsProject onlineProject, CmsFile file, boolean changed) throws CmsException {
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	PreparedStatement statement = null;
	PreparedStatement nextStatement = null;
	//OraclePreparedStatement trimStatement = null;
    PreparedStatement trimStatement = null;
	Connection con = null;
	ResultSet res = null;
    String usedPool;
    String usedStatement;
    if (project.getId() == onlineProject.getId()){
        usedPool = m_poolNameOnline;
        usedStatement = "_ONLINE";
    } else {
        usedPool = m_poolName;
        usedStatement = "";
    }
	try {
		// update the file header in the RESOURCE database.
		writeFileHeader(project, file, changed);
		// update the file content in the FILES database.
		con = DriverManager.getConnection(usedPool);
		statement = con.prepareStatement(cq.get("C_PLSQL_FILESFORUPDATE"+usedStatement));
		statement.setInt(1, file.getFileId());
		con.setAutoCommit(false);
		res = statement.executeQuery();
		try {
			while (res.next()) {
				oracle.sql.BLOB blobnew = ((OracleResultSet) res).getBLOB("FILE_CONTENT");
				// first trim the blob to 0 bytes, otherwise ther could be left some bytes
				// of the old content
				//trimStatement = (OraclePreparedStatement) con.prepareStatement(cq.get("C_TRIMBLOB);
				//trimStatement.setBLOB(1, blobnew);
                trimStatement = con.prepareStatement(cq.get("C_TRIMBLOB"));
				trimStatement.setBlob(1, blobnew);
				trimStatement.setInt(2, 0);
				trimStatement.execute();
				ByteArrayInputStream instream = new ByteArrayInputStream(file.getContents());
				OutputStream outstream = blobnew.getBinaryOutputStream();
				byte[] chunk = new byte[blobnew.getChunkSize()];
				int i = -1;
				while ((i = instream.read(chunk)) != -1) {
					outstream.write(chunk, 0, i);
				}
				instream.close();
				outstream.close();
			}
			// for the oracle-driver commit or rollback must be executed manually
			// because setAutoCommit = false in CmsDbPool.CmsDbPool
			nextStatement = con.prepareStatement(cq.get("C_COMMIT"));
			nextStatement.execute();
			nextStatement.close();
		    con.setAutoCommit(true);
		} catch (IOException e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), e);
		}
		statement.close();
		res.close();
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
			try {
				nextStatement = con.prepareStatement(cq.get("C_ROLLBACK"));
				nextStatement.execute();
			} catch (SQLException se) {
			}
		}
		if (nextStatement != null) {
			try {
				nextStatement.close();
			} catch (SQLException exc) {
			}
		}
		if (trimStatement != null) {
			try {
				trimStatement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.setAutoCommit(true);
			} catch (SQLException se) {
			}
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}
}

/**
 * Writes a serializable object to the systemproperties.
 *
 * @param name The name of the property.
 * @param object The property-object.
 *
 * @return object The property-object.
 *
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public Serializable writeSystemProperty(String name, Serializable object) throws CmsException {
	//System.out.println("PL/SQL: writeSystemProperty");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	PreparedStatement statement = null;
	PreparedStatement nextStatement = null;
	//OraclePreparedStatement trimStatement = null;
    PreparedStatement trimStatement = null;
	ResultSet res = null;
	Connection con = null;
	byte[] value = null;
	try {
		// serialize the object
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(object);
		oout.close();
		value = bout.toByteArray();
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareStatement(cq.get("C_PLSQL_SYSTEMPROPERTIES_NAMEFORUPDATE"));
		statement.setString(1, name);
		con.setAutoCommit(false);
		res = statement.executeQuery();
		while (res.next()) {
			oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SYSTEMPROPERTY_VALUE");
			// first trim the blob to 0 bytes, otherwise ther could be left some bytes
			// of the old content
			//trimStatement = (OraclePreparedStatement) con.prepareStatement(cq.get("C_TRIMBLOB);
			//trimStatement.setBLOB(1, blob);
            trimStatement = con.prepareStatement(cq.get("C_TRIMBLOB"));
			trimStatement.setBlob(1, blob);
			trimStatement.setInt(2, 0);
			trimStatement.execute();
			trimStatement.close();
			ByteArrayInputStream instream = new ByteArrayInputStream(value);
			OutputStream outstream = blob.getBinaryOutputStream();
			byte[] chunk = new byte[blob.getChunkSize()];
			int i = -1;
			while ((i = instream.read(chunk)) != -1) {
				outstream.write(chunk, 0, i);
			}
			instream.close();
			outstream.close();
		}
		statement.close();
		res.close();
		// for the oracle-driver commit or rollback must be executed manually
		// because setAutoCommit = false in CmsDbPool.CmsDbPool
		nextStatement = con.prepareStatement(cq.get("C_COMMIT"));
		nextStatement.execute();
		nextStatement.close();
		con.setAutoCommit(true);
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (IOException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + CmsException.C_SERIALIZATION, e);
	} finally {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException se) {
			}
			try {
				nextStatement = con.prepareStatement(cq.get("C_ROLLBACK"));
				nextStatement.execute();
			} catch (SQLException exc){
			}
		}
		if (nextStatement != null) {
			try {
				nextStatement.close();
			} catch (SQLException exc) {
			}
		}
		if (trimStatement != null) {
			try {
				trimStatement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.setAutoCommit(true);
			} catch (SQLException se) {
			}
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
	}
	return readSystemProperty(name);
}
/**
 * Writes a user to the database.
 *
 * @param user the user to write
 * @exception thorws CmsException if something goes wrong.
 */
public void writeUser(CmsUser user) throws CmsException {
	//System.out.println("PL/SQL: writeUser");
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
        byte[] value = null;
	PreparedStatement statement = null;
	PreparedStatement statement2 = null;
	PreparedStatement nextStatement = null;
	//OraclePreparedStatement trimStatement = null;
        PreparedStatement trimStatement = null;

	ResultSet res = null;
	Connection con = null;
	try {
		// serialize the hashtable
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(user.getAdditionalInfo());
		oout.close();
		value = bout.toByteArray();
		// write data to database
		con = DriverManager.getConnection(m_poolName);
		statement = con.prepareStatement(cq.get("C_PLSQL_USERSWRITE"));
		statement.setString(1, checkNull(user.getDescription()));
		statement.setString(2, checkNull(user.getFirstname()));
		statement.setString(3, checkNull(user.getLastname()));
		statement.setString(4, checkNull(user.getEmail()));
		statement.setTimestamp(5, new Timestamp(user.getLastlogin()));
		statement.setTimestamp(6, new Timestamp(user.getLastUsed()));
		statement.setInt(7, user.getFlags());
		//statement.setBytes(8,value);
		statement.setInt(8, user.getDefaultGroupId());
		statement.setString(9, checkNull(user.getAddress()));
		statement.setString(10, checkNull(user.getSection()));
		statement.setInt(11, user.getType());
		statement.setInt(12, user.getId());
		statement.executeUpdate();
		statement.close();
		// update user_info in this special way because of using blob
		statement2 = con.prepareStatement(cq.get("C_PLSQL_USERSFORUPDATE"));
		statement2.setInt(1, user.getId());
		con.setAutoCommit(false);
		res = statement2.executeQuery();
		try {
			while (res.next()) {
				oracle.sql.BLOB blobnew = ((OracleResultSet) res).getBLOB("USER_INFO");
				// first trim the blob to 0 bytes, otherwise ther could be left some bytes
				// of the old content
				//trimStatement = (OraclePreparedStatement) con.prepareStatement(cq.get("C_TRIMBLOB);
				trimStatement = con.prepareStatement(cq.get("C_TRIMBLOB"));
                //trimStatement.setBLOB(1, blobnew);
                trimStatement.setBlob(1, blobnew);
				trimStatement.setInt(2, 0);
				trimStatement.execute();
				trimStatement.close();
				ByteArrayInputStream instream = new ByteArrayInputStream(value);
				OutputStream outstream = blobnew.getBinaryOutputStream();
				byte[] chunk = new byte[blobnew.getChunkSize()];
				int i = -1;
				while ((i = instream.read(chunk)) != -1) {
					outstream.write(chunk, 0, i);
				}
				instream.close();
				outstream.close();
			}
			// for the oracle-driver commit or rollback must be executed manually
			// because setAutoCommit = false in CmsDbPool.CmsDbPool
			nextStatement = con.prepareStatement(cq.get("C_COMMIT"));
			nextStatement.execute();
			nextStatement.close();
			con.setAutoCommit(true);
		} catch (IOException e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), e);
		}
		statement2.close();
		res.close();
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (IOException e) {
		throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:" + CmsException.C_SERIALIZATION, e);
	} finally {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException exc) {
			}
		}
		if (statement2 != null) {
			try {
				statement2.close();
			} catch (SQLException exc) {
			}
			try {
				nextStatement = con.prepareStatement(cq.get("C_ROLLBACK"));
				nextStatement.execute();
			} catch (SQLException se) {
			}
		}
		if (nextStatement != null) {
			try {
				nextStatement.close();
			} catch (SQLException exc) {
			}
		}
		if (trimStatement != null) {
			try {
				trimStatement.close();
			} catch (SQLException exc) {
			}
		}
		if (con != null) {
			try {
				con.setAutoCommit(true);
			} catch (SQLException se) {
			}
			try {
				con.close();
			} catch (SQLException se) {
			}
		}
		if (res != null) {
			try {
				res.close();
			} catch (SQLException se) {
			}
		}
	}
}

	/**
	 * Destroys this access-module
	 * @exception throws CmsException if something goes wrong.
	 */
	public void destroy()
		throws CmsException {
        try {
            ((com.opencms.dbpool.CmsDriver) DriverManager.getDriver(m_poolName)).destroy();
        } catch(SQLException exc) {
            // destroy not possible - ignoring the exception
        }

		if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
			A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] destroy complete.");
		}
	}
}

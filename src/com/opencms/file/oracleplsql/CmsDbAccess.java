package com.opencms.file.oracleplsql;

import oracle.jdbc.driver.*;
/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oracleplsql/Attic/CmsDbAccess.java,v $
 * Date   : $Date: 2000/11/08 13:46:24 $
 * Version: $Revision: 1.6 $
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
import com.opencms.file.genericSql.I_CmsDbPool;



/**
 * This is the generic access module to load and store resources from and into
 * the database.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Hanjo Riege
 * @author Anders Fugmann
 * @version $Revision: 1.6 $ $Date: 2000/11/08 13:46:24 $ * 
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
/*
* Checks, if the user may create this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * 
 * @return wether the user has access, or not.
*/
public boolean accessCreate(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
	//System.out.println("PL/SQL: accessCreate");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSCREATE_KEY);
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
			pool.putPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSCREATE_KEY, statement);
		}
	}
}
/**
 * Checks, if the group may access this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * @param flags The flags to check.
 * 
 * @return wether the user has access, or not.
 */
public boolean accessGroup(CmsUser currentUser, CmsProject currentProject, CmsResource resource, int flags) throws CmsException {
	//System.out.println("PL/SQL: accessGroup");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSGROUP_KEY);
		statement.setInt(2, currentUser.getId());
		statement.setInt(3, currentProject.getId());
		statement.setInt(4, resource.getResourceId());
		statement.setInt(5, flags);
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
			pool.putPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSGROUP_KEY, statement);
		}
	}
}
/*
* Checks, if the user may lock this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * 
 * @return wether the user has access, or not.
*/
public boolean accessLock(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
	//System.out.println("PL/SQL: accessLock");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSLOCK_KEY);
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
			pool.putPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSLOCK_KEY, statement);
		}
	}
}
/**
 * Checks, if the other may access this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * @param flags The flags to check.
 * 
 * @return wether the user has access, or not.
 */
public boolean accessOther(CmsUser currentUser, CmsProject currentProject, CmsResource resource, int flags) throws CmsException {
	//System.out.println("PL/SQL: accessOther");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSOTHER_KEY);
		statement.setInt(2, currentUser.getId());
		statement.setInt(3, currentProject.getId());
		statement.setInt(4, resource.getResourceId());
		statement.setInt(5, flags);
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
			pool.putPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSOTHER_KEY, statement);
		}
	}
}
/**
 * Checks, if the owner may access this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * @param flags The flags to check.
 * 
 * @return wether the user has access, or not.
 */
public boolean accessOwner(CmsUser currentUser, CmsProject currentProject, CmsResource resource, int flags) throws CmsException {
	//System.out.println("PL/SQL: accessOwner");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSOWNER_KEY);
		statement.setInt(2, currentUser.getId());
		statement.setInt(3, currentProject.getId());
		statement.setInt(4, resource.getResourceId());
		statement.setInt(5, flags);
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
			pool.putPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSOWNER_KEY, statement);
		}
	}
}
/*
* Checks, if the user may read this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * 
 * @return wether the user has access, or not.
*/
public boolean accessProject(CmsUser currentUser, CmsProject currentProject) throws CmsException {
	//System.out.println("PL/SQL: accessProject");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSPROJECT_KEY);
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
			pool.putPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSPROJECT_KEY, statement);
		}
	}
}
/*
* Checks, if the user may read this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * 
 * @return wether the user has access, or not.
*/
public boolean accessRead(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
	//System.err.println("PL/SQL: accessRead " + resource);
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSREAD_KEY);
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
			pool.putPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSREAD_KEY, statement);
		}
	}
}
/*
* Checks, if the user may write this resource.
 * 
 * @param currentUser The user who requested this method.
 * @param currentProject The current project of the user.
 * @param resource The resource to check.
 * 
 * @return wether the user has access, or not.
*/
public boolean accessWrite(CmsUser currentUser, CmsProject currentProject, CmsResource resource) throws CmsException {
	//System.out.println("PL/SQL: accessWrite");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSWRITE_KEY);
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
			pool.putPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSWRITE_KEY, statement);
		}
	}
}
/**
>>>>>>> 1.161
 * Creates a serializable object in the systempropertys.
 * 
 * @param name The name of the property.
 * @param object The property-object.
 * 
 * @return object The property-object.
 * 
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public Serializable addSystemProperty(String name, Serializable object) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	byte[] value;
	PreparedStatement statement = null;
	PreparedStatement statement2 = null;
	PreparedStatement nextStatement = null;
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
		statement = pool.getPreparedStatement(cq.C_PLSQL_SYSTEMPROPERTIES_FORINSERT_KEY);
		statement.setInt(1, id);
		statement.setString(2, name);
		//statement.setBytes(3,value);
		statement.executeUpdate();
		// now update the systemproperty_value
		statement2 = pool.getNextPreparedStatement(statement,cq.C_PLSQL_SYSTEMPROPERTIES_FORUPDATE_KEY);
		statement2.setInt(1, id);
		Connection conn = pool.getConnectionOfStatement(statement);
		conn.setAutoCommit(false);	
		ResultSet res = statement2.executeQuery();
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
		// for the oracle-driver commit or rollback must be executed manually
		// because setAutoCommit = false
		nextStatement = pool.getNextPreparedStatement(statement, cq.C_COMMIT_KEY);
		nextStatement.execute();
		conn.setAutoCommit(true);
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (IOException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + CmsException.C_SERIALIZATION, e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_SYSTEMPROPERTIES_FORINSERT_KEY, statement);
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	int id = nextId(C_TABLE_USERS);
	byte[] value = null;
	PreparedStatement statement = null;
	PreparedStatement statement2 = null;
	PreparedStatement nextStatement = null;
	OraclePreparedStatement trimStatement = null;
	try {
		// serialize the hashtable
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(additionalInfos);
		oout.close();
		value = bout.toByteArray();

		// write data to database
		// first insert the data without user_info 
		statement = pool.getPreparedStatement(cq.C_PLSQL_USERSFORINSERT_KEY);
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

		// now update user_info of the new user
		statement2 = pool.getNextPreparedStatement(statement, cq.C_PLSQL_USERSFORUPDATE_KEY);
		statement2.setInt(1, id);
		Connection conn = pool.getConnectionOfStatement(statement);
		conn.setAutoCommit(false);
		ResultSet res = statement2.executeQuery();
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
		res.close();
		// for the oracle-driver commit or rollback must be executed manually
		// because setAutoCommit = false in CmsDbPool.CmsDbPool
		nextStatement = pool.getNextPreparedStatement(statement, cq.C_COMMIT_KEY);
		nextStatement.execute();
		conn.setAutoCommit(true);
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (IOException e) {
		throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:" + CmsException.C_SERIALIZATION, e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_USERSFORINSERT_KEY, statement);
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	String exceptionMessage = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_RESOURCES_COPYFILE_KEY);
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
			pool.putPreparedStatement(cq.C_PLSQL_RESOURCES_COPYFILE_KEY, statement);
		}
	}
}
// methods working with resources

/**
 * Copies a resource from the online project to a new, specified project.<br>
 *
 * @param project The project to be published.
 * @param onlineProject The online project of the OpenCms.
 * @param resource The resource to be copied to the offline project.
 * @exception CmsException  Throws CmsException if operation was not succesful.
 */
public void copyResourceToProject(CmsUser currentUser, CmsProject currentProject, String resource) throws CmsException {
	//	System.out.println("PL/SQL: copyResourceToProject");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_PROJECTS_COPYRESOURCETOPROJECT_KEY);
		statement.setInt(1, currentUser.getId());
		statement.setInt(2, currentProject.getId());
		statement.setString(3, resource);
		statement.execute();
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_PROJECTS_COPYRESOURCETOPROJECT_KEY, statement);
		}
	}
}
/**
 * Create a new Connection guard.
 * This method should be overloaded if another connectionguard should be used.
 * Creation date: (06-09-2000 14:33:30)
 * @return com.opencms.file.genericSql.CmsConnectionGuard
 * @param m_pool com.opencms.file.genericSql.I_CmsDbPool
 * @param sleepTime long
 */
public com.opencms.file.genericSql.CmsConnectionGuard createCmsConnectionGuard(I_CmsDbPool m_pool, long sleepTime) {
	return new com.opencms.file.oracleplsql.CmsConnectionGuard(m_pool, sleepTime);
}
/**
 * Creates a CmsDbPool
 * Creation date: (06-09-2000 14:08:10)
 * @return com.opencms.file.genericSql.CmsDbPool
 * @param driver java.lang.String
 * @param url java.lang.String
 * @param user java.lang.String
 * @param passwd java.lang.String
 * @param maxConn int
 * @exception com.opencms.core.CmsException The exception description.
 */
public I_CmsDbPool createCmsDbPool(String driver, String url, String user, String passwd, int maxConn) throws com.opencms.core.CmsException {
	return new com.opencms.file.oracleplsql.CmsDbPool(driver,url,user,passwd,maxConn);
}
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
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
	} catch (CmsException e) {
		// if the file is maked as deleted remove it!
		if (e.getType()==CmsException.C_RESOURCE_DELETED) {
		   removeFile(project.getId(),filename);
		   state=C_STATE_CHANGED;
		}              
	}
	int resourceId = nextId(C_TABLE_RESOURCES);
	int fileId = nextId(C_TABLE_FILES);

	PreparedStatement statement = null;
	PreparedStatement statementFileIns = null;
	PreparedStatement statementFileUpd = null;
	PreparedStatement nextStatement = null;
	try {
		statement = m_pool.getPreparedStatement(m_cq.C_RESOURCES_WRITE_KEY);
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

		// first insert new file without file_content, then update the file_content
		// these two steps are necessary because of using BLOBs	in the Oracle DB
		statementFileIns = pool.getNextPreparedStatement(statement, cq.C_PLSQL_FILESFORINSERT_KEY);
		statementFileIns.setInt(1, fileId);
		statementFileIns.executeUpdate();

		// update the file content in the FILES database.
		statementFileUpd = pool.getNextPreparedStatement(statement, cq.C_PLSQL_FILESFORUPDATE_KEY);
		statementFileUpd.setInt(1, fileId);
		Connection conn = pool.getConnectionOfStatement(statement);
		conn.setAutoCommit(false);
		ResultSet res = statementFileUpd.executeQuery();
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
			nextStatement = pool.getNextPreparedStatement(statement, cq.C_COMMIT_KEY);
			nextStatement.execute();
			conn.setAutoCommit(true);
		} catch (IOException e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), e);
		}
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} finally {
		if (statement != null) {
			m_pool.putPreparedStatement(m_cq.C_RESOURCES_WRITE_KEY, statement);
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	byte[] value = null;
	PreparedStatement statement = null;
	PreparedStatement statement2 = null;
	PreparedStatement nextStatement = null;	
	try {
		// serialize the hashtable
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(data);
		oout.close();
		value = bout.toByteArray();

		// write data to database     
		statement = pool.getPreparedStatement(cq.C_PLSQL_SESSION_FORINSERT_KEY);
		statement.setString(1, sessionId);
		statement.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
		statement.executeUpdate();
		statement2 = pool.getNextPreparedStatement(statement, cq.C_PLSQL_SESSION_FORUPDATE_KEY);
		statement2.setString(1, sessionId);
		Connection conn = pool.getConnectionOfStatement(statement);
		conn.setAutoCommit(false);
		ResultSet res = statement2.executeQuery();
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
		// for the oracle-driver commit or rollback must be executed manually
		// because setAutoCommit = false
		nextStatement = pool.getNextPreparedStatement(statement, cq.C_COMMIT_KEY);
		nextStatement.execute();
		conn.setAutoCommit(true);
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (IOException e) {
		throw new CmsException("[" + this.getClass().getName() + "]:" + CmsException.C_SERIALIZATION, e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_SESSION_FORINSERT_KEY, statement);
		}
	}
}
/**
 * Private method to init all default-resources
 */
protected void fillDefaults() throws CmsException
{
	if(A_OpenCms.isLogging()) {		A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsDbAccess] fillDefaults() starting NOW!");	}
	// insert the first Id
	initId();

	// the resourceType "folder" is needed always - so adding it
	Hashtable resourceTypes = new Hashtable(1);
	resourceTypes.put(C_TYPE_FOLDER_NAME, new CmsResourceType(C_TYPE_FOLDER, 0, C_TYPE_FOLDER_NAME, ""));

	// sets the last used index of resource types.
	resourceTypes.put(C_TYPE_LAST_INDEX, new Integer(C_TYPE_FOLDER));

	// add the resource-types to the database
	addSystemProperty(C_SYSTEMPROPERTY_RESOURCE_TYPE, resourceTypes);

	// set the mimetypes
	addSystemProperty(C_SYSTEMPROPERTY_MIMETYPES, initMimetypes());

	// set the groups
	CmsGroup guests = createGroup(C_GROUP_GUEST, "the guest-group", C_FLAG_ENABLED, null);
	CmsGroup administrators = createGroup(C_GROUP_ADMIN, "the admin-group", C_FLAG_ENABLED | C_FLAG_GROUP_PROJECTMANAGER, null);
	CmsGroup projectleader = createGroup(C_GROUP_PROJECTLEADER, "the projectmanager-group", C_FLAG_ENABLED | C_FLAG_GROUP_PROJECTMANAGER | C_FLAG_GROUP_PROJECTCOWORKER | C_FLAG_GROUP_ROLE, null);
	CmsGroup users = createGroup(C_GROUP_USERS, "the users-group to access the workplace", C_FLAG_ENABLED | C_FLAG_GROUP_ROLE | C_FLAG_GROUP_PROJECTCOWORKER, C_GROUP_GUEST);

	// add the users
	CmsUser guest = addUser(C_USER_GUEST, "", "the guest-user", " ", " ", " ", 0, 0, C_FLAG_ENABLED, new Hashtable(), guests, " ", " ", C_USER_TYPE_SYSTEMUSER);
	CmsUser admin = addUser(C_USER_ADMIN, "admin", "the admin-user", " ", " ", " ", 0, 0, C_FLAG_ENABLED, new Hashtable(), administrators, " ", " ", C_USER_TYPE_SYSTEMUSER);
	addUserToGroup(guest.getId(), guests.getId());
	addUserToGroup(admin.getId(), administrators.getId());
	CmsTask task = createTask(0, 0, 1, // standart project type,
	admin.getId(), admin.getId(), administrators.getId(), C_PROJECT_ONLINE, new java.sql.Timestamp(new java.util.Date().getTime()), new java.sql.Timestamp(new java.util.Date().getTime()), C_TASK_PRIORITY_NORMAL);
	CmsProject online = createProject(admin, guests, projectleader, task, C_PROJECT_ONLINE, "the online-project", C_FLAG_ENABLED, C_PROJECT_TYPE_NORMAL);

	// create the root-folder
	CmsFolder rootFolder = createFolder(admin, online, C_UNKNOWN_ID, C_UNKNOWN_ID, C_ROOT, 0);
	rootFolder.setGroupId(users.getId());
	writeFolder(online, rootFolder, false);
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	Vector projects = new Vector();
	ResultSet res;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_PROJECTS_GETALLACCESS_KEY);
		statement.setInt(2, user.getId());
		statement.execute();
		res = (ResultSet) statement.getObject(1);
		while (res.next()) {
			projects.addElement(new CmsProject(res.getInt(m_cq.C_PROJECTS_PROJECT_ID), res.getString(m_cq.C_PROJECTS_PROJECT_NAME), res.getString(m_cq.C_PROJECTS_PROJECT_DESCRIPTION), res.getInt(m_cq.C_PROJECTS_TASK_ID), res.getInt(m_cq.C_PROJECTS_USER_ID), res.getInt(m_cq.C_PROJECTS_GROUP_ID), res.getInt(m_cq.C_PROJECTS_MANAGERGROUP_ID), res.getInt(m_cq.C_PROJECTS_PROJECT_FLAGS), SqlHelper.getTimestamp(res, m_cq.C_PROJECTS_PROJECT_CREATEDATE), SqlHelper.getTimestamp(res, m_cq.C_PROJECTS_PROJECT_PUBLISHDATE), res.getInt(m_cq.C_PROJECTS_PROJECT_PUBLISHED_BY), res.getInt(m_cq.C_PROJECTS_PROJECT_TYPE)));
		}
		res.close();
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_PROJECTS_GETALLACCESS_KEY, statement);
		}
	}
	return (projects);
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
		System.err.println("Error in getCmsException() " + exceptionMessage);	
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
 * Returns a list of groups of a user.<P/>
 * 
 * @param name The name of the user.
 * @return Vector of groups
 * @exception CmsException Throws CmsException if operation was not succesful
 */
public Vector getGroupsOfUser(String username) throws CmsException {
	//	System.out.println("PL/SQL: getGroupsOfUser");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CmsUser user = readUser(username, 0);
	CmsGroup group;
	Vector groups = new Vector();
	CallableStatement statement = null;
	ResultSet res;
	try {
		//  get all all groups of the user
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_GROUPS_GETGROUPSOFUSER_KEY);
		statement.setInt(2, user.getId());
		statement.execute();
		res = (ResultSet) statement.getObject(1);
		while (res.next()) {
			group = new CmsGroup(res.getInt(m_cq.C_GROUPS_GROUP_ID), res.getInt(m_cq.C_GROUPS_PARENT_GROUP_ID), res.getString(m_cq.C_GROUPS_GROUP_NAME), res.getString(m_cq.C_GROUPS_GROUP_DESCRIPTION), res.getInt(m_cq.C_GROUPS_GROUP_FLAGS));
			groups.addElement(group);
		}
		res.close();
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_GROUPS_GETGROUPSOFUSER_KEY, statement);
		}
	}
	return groups;
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
		Vector users = new Vector();
		PreparedStatement statement = null;
		ResultSet res = null;
		
		try	{			
			statement = m_pool.getPreparedStatement(m_cq.C_USERS_GETUSERS_KEY);
			statement.setInt(1,type);
			res = statement.executeQuery();
			// create new Cms user objects
			while( res.next() ) {
				// read the additional infos.
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.C_USERS_USER_INFO); 
				byte[] value = new byte[(int) blob.length()]; 
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				CmsUser user = new CmsUser(res.getInt(m_cq.C_USERS_USER_ID),
										   res.getString(m_cq.C_USERS_USER_NAME),
										   res.getString(m_cq.C_USERS_USER_PASSWORD),
										   res.getString(m_cq.C_USERS_USER_RECOVERY_PASSWORD),
										   res.getString(m_cq.C_USERS_USER_DESCRIPTION),
										   res.getString(m_cq.C_USERS_USER_FIRSTNAME),
										   res.getString(m_cq.C_USERS_USER_LASTNAME),
										   res.getString(m_cq.C_USERS_USER_EMAIL),
										   SqlHelper.getTimestamp(res,m_cq.C_USERS_USER_LASTLOGIN).getTime(),
										   SqlHelper.getTimestamp(res,m_cq.C_USERS_USER_LASTUSED).getTime(),
										   res.getInt(m_cq.C_USERS_USER_FLAGS),
										   info,
										   new CmsGroup(res.getInt(m_cq.C_GROUPS_GROUP_ID),
														res.getInt(m_cq.C_GROUPS_PARENT_GROUP_ID),
														res.getString(m_cq.C_GROUPS_GROUP_NAME),
														res.getString(m_cq.C_GROUPS_GROUP_DESCRIPTION),
														res.getInt(m_cq.C_GROUPS_GROUP_FLAGS)),
										   res.getString(m_cq.C_USERS_USER_ADDRESS),
										   res.getString(m_cq.C_USERS_USER_SECTION),
										   res.getInt(m_cq.C_USERS_USER_TYPE));
				
				users.addElement(user);
			} 

			res.close();
		} catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"]"+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		} catch (Exception e) {
			throw new CmsException("["+this.getClass().getName()+"]", e);			
		} finally {
			if( statement != null) {
				m_pool.putPreparedStatement(m_cq.C_USERS_GETUSERS_KEY, statement);
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
	//	System.out.println("PL/SQL: getUsersOfGroup");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CmsGroup group;
	Vector users = new Vector();
	CallableStatement statement = null;
	ResultSet res = null;
	try {
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_GROUPS_GETUSERSOFGROUP_KEY);
		statement.setInt(2, currentUser.getId());
		statement.setString(3, name);
		statement.setInt(4, type);
		statement.execute();
		res = (ResultSet) statement.getObject(1);
		while (res.next()) {
			// read the additional infos.
			oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.C_USERS_USER_INFO);
			byte[] value = new byte[(int) blob.length()];
			value = blob.getBytes(1, (int) blob.length());
			// now deserialize the object
			ByteArrayInputStream bin = new ByteArrayInputStream(value);
			ObjectInputStream oin = new ObjectInputStream(bin);
			Hashtable info = (Hashtable) oin.readObject();
			CmsUser user = new CmsUser(res.getInt(m_cq.C_USERS_USER_ID), res.getString(m_cq.C_USERS_USER_NAME), res.getString(m_cq.C_USERS_USER_PASSWORD), res.getString(m_cq.C_USERS_USER_RECOVERY_PASSWORD), res.getString(m_cq.C_USERS_USER_DESCRIPTION), res.getString(m_cq.C_USERS_USER_FIRSTNAME), res.getString(m_cq.C_USERS_USER_LASTNAME), res.getString(m_cq.C_USERS_USER_EMAIL), SqlHelper.getTimestamp(res, m_cq.C_USERS_USER_LASTLOGIN).getTime(), SqlHelper.getTimestamp(res, m_cq.C_USERS_USER_LASTUSED).getTime(), res.getInt(m_cq.C_USERS_USER_FLAGS), info, new CmsGroup(res.getInt(m_cq.C_USERS_USER_DEFAULT_GROUP_ID), res.getInt(m_cq.C_GROUPS_PARENT_GROUP_ID), res.getString(m_cq.C_GROUPS_GROUP_NAME), res.getString(m_cq.C_GROUPS_GROUP_DESCRIPTION), res.getInt(m_cq.C_GROUPS_GROUP_FLAGS)), res.getString(m_cq.C_USERS_USER_ADDRESS), res.getString(m_cq.C_USERS_USER_SECTION), res.getInt(m_cq.C_USERS_USER_TYPE));
			users.addElement(user);
		}
		res.close();
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_GROUPS_GETUSERSOFGROUP_KEY, statement);
		}
	}
	return users;
}
/**
 * Private method to init all statements in the pool.
 */
protected void initStatements() throws CmsException {
	super.initStatements();
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;

	// init statements for commit and rollback
	// must be executed manually with getNextPreparedStatement 
	// because of setAutoCommit = false in CmsDbPool.CmsDbPool
	pool.initCallableStatement(cq.C_COMMIT_KEY, cq.C_COMMIT);
	pool.initCallableStatement(cq.C_ROLLBACK_KEY, cq.C_ROLLBACK);
	pool.initOracleCallableStatement(cq.C_TRIMBLOB_KEY, cq.C_TRIMBLOB);
		
	// init statements for resources
	pool.initCallableStatement(cq.C_PLSQL_RESOURCES_LOCKRESOURCE_KEY, cq.C_PLSQL_RESOURCES_LOCKRESOURCE);
	pool.initRegisterOutParameter(cq.C_PLSQL_RESOURCES_LOCKRESOURCE_KEY, 5, oracle.jdbc.driver.OracleTypes.CURSOR);
	pool.initCallableStatement(cq.C_PLSQL_RESOURCES_UNLOCKRESOURCE_KEY, cq.C_PLSQL_RESOURCES_UNLOCKRESOURCE);
	pool.initRegisterOutParameter(cq.C_PLSQL_RESOURCES_UNLOCKRESOURCE_KEY, 4, oracle.jdbc.driver.OracleTypes.CURSOR);
	
	pool.initCallableStatement(cq.C_PLSQL_RESOURCES_READFOLDER_KEY, cq.C_PLSQL_RESOURCES_READFOLDER);
	pool.initRegisterOutParameter(cq.C_PLSQL_RESOURCES_READFOLDER_KEY, 1, oracle.jdbc.driver.OracleTypes.CURSOR);
	pool.initCallableStatement(cq.C_PLSQL_RESOURCES_READFILEHEADER_KEY, cq.C_PLSQL_RESOURCES_READFILEHEADER);
	pool.initRegisterOutParameter(cq.C_PLSQL_RESOURCES_READFILEHEADER_KEY, 1, oracle.jdbc.driver.OracleTypes.CURSOR);		
	pool.initCallableStatement(cq.C_PLSQL_RESOURCES_READFILE_KEY, cq.C_PLSQL_RESOURCES_READFILE);
	pool.initRegisterOutParameter(cq.C_PLSQL_RESOURCES_READFILE_KEY, 1, oracle.jdbc.driver.OracleTypes.CURSOR);
	pool.initCallableStatement(cq.C_PLSQL_RESOURCES_READFILEACC_KEY, cq.C_PLSQL_RESOURCES_READFILE);
	pool.initRegisterOutParameter(cq.C_PLSQL_RESOURCES_READFILEACC_KEY, 1, oracle.jdbc.driver.OracleTypes.CURSOR);
	
	pool.initCallableStatement(cq.C_PLSQL_RESOURCES_WRITEFILEHEADER_KEY, cq.C_PLSQL_RESOURCES_WRITEFILEHEADER);
	pool.initCallableStatement(cq.C_PLSQL_RESOURCES_COPYFILE_KEY, cq.C_PLSQL_RESOURCES_COPYFILE);
		
	// init statements for projects
	pool.initCallableStatement(cq.C_PLSQL_PROJECTS_GETALLACCESS_KEY, cq.C_PLSQL_PROJECTS_GETALLACCESS);
	pool.initRegisterOutParameter(cq.C_PLSQL_PROJECTS_GETALLACCESS_KEY, 1, oracle.jdbc.driver.OracleTypes.CURSOR);
	pool.initCallableStatement(cq.C_PLSQL_PROJECTS_COPYRESOURCETOPROJECT_KEY, cq.C_PLSQL_PROJECTS_COPYRESOURCETOPROJECT);
	pool.initCallableStatement(cq.C_PLSQL_PROJECTS_PUBLISHPROJECT_KEY, cq.C_PLSQL_PROJECTS_PUBLISHPROJECT);
	pool.initRegisterOutParameter(cq.C_PLSQL_PROJECTS_PUBLISHPROJECT_KEY, 4, oracle.jdbc.driver.OracleTypes.CURSOR);
	pool.initRegisterOutParameter(cq.C_PLSQL_PROJECTS_PUBLISHPROJECT_KEY, 5, oracle.jdbc.driver.OracleTypes.CURSOR);
	pool.initRegisterOutParameter(cq.C_PLSQL_PROJECTS_PUBLISHPROJECT_KEY, 6, oracle.jdbc.driver.OracleTypes.CURSOR);
	pool.initRegisterOutParameter(cq.C_PLSQL_PROJECTS_PUBLISHPROJECT_KEY, 7, oracle.jdbc.driver.OracleTypes.CURSOR);		
	
	// init statements for access
	pool.initCallableStatement(cq.C_PLSQL_ACCESS_ACCESSCREATE_KEY, cq.C_PLSQL_ACCESS_ACCESSCREATE);
	pool.initRegisterOutParameter(cq.C_PLSQL_ACCESS_ACCESSCREATE_KEY, 1, Types.INTEGER);	
	pool.initCallableStatement(cq.C_PLSQL_ACCESS_ACCESSLOCK_KEY, cq.C_PLSQL_ACCESS_ACCESSLOCK);
	pool.initRegisterOutParameter(cq.C_PLSQL_ACCESS_ACCESSLOCK_KEY, 1, Types.INTEGER);	
	pool.initCallableStatement(cq.C_PLSQL_ACCESS_ACCESSPROJECT_KEY, cq.C_PLSQL_ACCESS_ACCESSPROJECT);
	pool.initRegisterOutParameter(cq.C_PLSQL_ACCESS_ACCESSPROJECT_KEY, 1, Types.INTEGER);	
	pool.initCallableStatement(cq.C_PLSQL_ACCESS_ACCESSREAD_KEY, cq.C_PLSQL_ACCESS_ACCESSREAD);
	pool.initRegisterOutParameter(cq.C_PLSQL_ACCESS_ACCESSREAD_KEY, 1, Types.INTEGER);
	pool.initCallableStatement(cq.C_PLSQL_ACCESS_ACCESSWRITE_KEY, cq.C_PLSQL_ACCESS_ACCESSWRITE);
	pool.initRegisterOutParameter(cq.C_PLSQL_ACCESS_ACCESSWRITE_KEY, 1, Types.INTEGER);
	pool.initCallableStatement(cq.C_PLSQL_ACCESS_ACCESSOWNER_KEY, cq.C_PLSQL_ACCESS_ACCESSOWNER);
	pool.initRegisterOutParameter(cq.C_PLSQL_ACCESS_ACCESSOWNER_KEY, 1, Types.INTEGER);			
	pool.initCallableStatement(cq.C_PLSQL_ACCESS_ACCESSOTHER_KEY, cq.C_PLSQL_ACCESS_ACCESSOTHER);
	pool.initRegisterOutParameter(cq.C_PLSQL_ACCESS_ACCESSOTHER_KEY, 1, Types.INTEGER);
	pool.initCallableStatement(cq.C_PLSQL_ACCESS_ACCESSGROUP_KEY, cq.C_PLSQL_ACCESS_ACCESSGROUP);
	pool.initRegisterOutParameter(cq.C_PLSQL_ACCESS_ACCESSGROUP_KEY, 1, Types.INTEGER);		

	// init statements for groups
	pool.initCallableStatement(cq.C_PLSQL_GROUPS_USERINGROUP_KEY, cq.C_PLSQL_GROUPS_USERINGROUP);
	pool.initRegisterOutParameter(cq.C_PLSQL_GROUPS_USERINGROUP_KEY, 1, Types.INTEGER);		
	pool.initCallableStatement(cq.C_PLSQL_GROUPS_GETGROUPSOFUSER_KEY, cq.C_PLSQL_GROUPS_GETGROUPSOFUSER);
	pool.initRegisterOutParameter(cq.C_PLSQL_GROUPS_GETGROUPSOFUSER_KEY, 1, oracle.jdbc.driver.OracleTypes.CURSOR);
	pool.initCallableStatement(cq.C_PLSQL_GROUPS_ISMANAGEROFPROJECT_KEY, cq.C_PLSQL_GROUPS_ISMANAGEROFPROJECT);
	pool.initRegisterOutParameter(cq.C_PLSQL_GROUPS_ISMANAGEROFPROJECT_KEY, 1, Types.INTEGER);
	pool.initCallableStatement(cq.C_PLSQL_GROUPS_GETUSERSOFGROUP_KEY, cq.C_PLSQL_GROUPS_GETUSERSOFGROUP);
	pool.initRegisterOutParameter(cq.C_PLSQL_GROUPS_GETUSERSOFGROUP_KEY, 1, oracle.jdbc.driver.OracleTypes.CURSOR);	

	// init statements for files
	pool.initCallableStatement(cq.C_PLSQL_FILESFORUPDATE_KEY, cq.C_PLSQL_FILESFORUPDATE);
	pool.initCallableStatement(cq.C_PLSQL_FILESFORINSERT_KEY, cq.C_PLSQL_FILESFORINSERT);

	// init statements for users
	pool.initCallableStatement(cq.C_PLSQL_USERSWRITE_KEY, cq.C_PLSQL_USERSWRITE);
	pool.initCallableStatement(cq.C_PLSQL_USERSFORUPDATE_KEY, cq.C_PLSQL_USERSFORUPDATE);
	pool.initCallableStatement(cq.C_PLSQL_USERSFORINSERT_KEY, cq.C_PLSQL_USERSFORINSERT);	

	// init statements for systemproperties
	pool.initCallableStatement(cq.C_PLSQL_SYSTEMPROPERTIES_FORUPDATE_KEY, cq.C_PLSQL_SYSTEMPROPERTIES_FORUPDATE);
	pool.initCallableStatement(cq.C_PLSQL_SYSTEMPROPERTIES_NAMEFORUPDATE_KEY, cq.C_PLSQL_SYSTEMPROPERTIES_NAMEFORUPDATE);
	pool.initCallableStatement(cq.C_PLSQL_SYSTEMPROPERTIES_FORINSERT_KEY, cq.C_PLSQL_SYSTEMPROPERTIES_FORINSERT);	
		
	// init statements for sessions
	pool.initCallableStatement(cq.C_PLSQL_SESSION_FORINSERT_KEY, cq.C_PLSQL_SESSION_FORINSERT);	
	pool.initCallableStatement(cq.C_PLSQL_SESSION_FORUPDATE_KEY, cq.C_PLSQL_SESSION_FORUPDATE);		
	pool.initCallableStatement(cq.C_PLSQL_SESSION_UPDATE_KEY, cq.C_PLSQL_SESSION_UPDATE);	
		
	pool.initLinkConnections();
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
	//	System.out.println("PL/SQL: isManagerOfProject");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_GROUPS_ISMANAGEROFPROJECT_KEY);
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
			pool.putPreparedStatement(cq.C_PLSQL_GROUPS_ISMANAGEROFPROJECT_KEY, statement);
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	String exceptionMessage = null;
	ResultSet res = null;
	Vector resources = new Vector();
	CmsResource resource = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_RESOURCES_LOCKRESOURCE_KEY);
		statement.setInt(1, currentUser.getId());
		statement.setInt(2, currentProject.getId());
		statement.setString(3, resourcename);
		if (force == true) {
			statement.setString(4, "TRUE");
		} else {
			statement.setString(4, "FALSE");
		}
		statement.execute();
		res = (ResultSet) statement.getObject(5);
		while (res.next()) {
			int resId = res.getInt(m_cq.C_RESOURCES_RESOURCE_ID);
			int parentId = res.getInt(m_cq.C_RESOURCES_PARENT_ID);
			String resName = res.getString(m_cq.C_RESOURCES_RESOURCE_NAME);
			int resType = res.getInt(m_cq.C_RESOURCES_RESOURCE_TYPE);
			int resFlags = res.getInt(m_cq.C_RESOURCES_RESOURCE_FLAGS);
			int userId = res.getInt(m_cq.C_RESOURCES_USER_ID);
			int groupId = res.getInt(m_cq.C_RESOURCES_GROUP_ID);
			int projectId = res.getInt(m_cq.C_RESOURCES_PROJECT_ID);
			int fileId = res.getInt(m_cq.C_RESOURCES_FILE_ID);
			int accessFlags = res.getInt(m_cq.C_RESOURCES_ACCESS_FLAGS);
			int state = res.getInt(m_cq.C_RESOURCES_STATE);
			int lockedBy = res.getInt(m_cq.C_RESOURCES_LOCKED_BY);
			int launcherType = res.getInt(m_cq.C_RESOURCES_LAUNCHER_TYPE);
			String launcherClass = res.getString(m_cq.C_RESOURCES_LAUNCHER_CLASSNAME);
			long created = SqlHelper.getTimestamp(res, m_cq.C_RESOURCES_DATE_CREATED).getTime();
			long modified = SqlHelper.getTimestamp(res, m_cq.C_RESOURCES_DATE_LASTMODIFIED).getTime();
			int modifiedBy = res.getInt(m_cq.C_RESOURCES_LASTMODIFIED_BY);
			int resSize = res.getInt(m_cq.C_RESOURCES_SIZE);
			resource = new CmsResource(resId, parentId, fileId, resName, resType, resFlags, userId, 
										groupId, projectId, accessFlags, state, lockedBy, launcherType, 
										launcherClass, created, modified, modifiedBy, resSize);
			resources.addElement(resource);
		}
		res.close();
		return resources;
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_RESOURCES_LOCKRESOURCE_KEY, statement);
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
 * @param id The id of the project to be published.
 * @return a vector of changed resources.
 * 
 * @exception CmsException Throws CmsException if something goes wrong.
 */
public void publishProject(CmsUser currentUser, int id, CmsProject onlineProject) throws CmsException {
	//	System.out.println("PL/SQL: publishProject");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CmsAccessFilesystem discAccess = new CmsAccessFilesystem(m_exportpointStorage);
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_PROJECTS_PUBLISHPROJECT_KEY);
		statement.setInt(1, currentUser.getId());
		statement.setInt(2, id);
		statement.setInt(3, onlineProject.getId());
		statement.execute();
		// now export to filesystem if necessary
		// for deleted folder		
		ResultSet res1 = (ResultSet) statement.getObject(4);
		while (res1.next()) {
			String exportKey = checkExport(res1.getString("RESOURCE_NAME"));
			if (exportKey != null) {
				discAccess.removeResource(res1.getString("RESOURCE_NAME"), exportKey);
			}
		}
		res1.close();
		// for changed/new folder
		ResultSet res2 = (ResultSet) statement.getObject(5);
		while (res2.next()) {
			String exportKey = checkExport(res2.getString("RESOURCE_NAME"));
			if (exportKey != null) {
				discAccess.createFolder(res2.getString("RESOURCE_NAME"), exportKey);
			}
		}
		res2.close();
		// for deleted files
		ResultSet res3 = (ResultSet) statement.getObject(6);
		while (res3.next()) {
			String exportKey = checkExport(res3.getString("RESOURCE_NAME"));
			if (exportKey != null) {
				discAccess.removeResource(res3.getString("RESOURCE_NAME"), exportKey);
			}
		}
		res3.close();
		// for changed/new files
		ResultSet res4 = (ResultSet) statement.getObject(7);
		while (res4.next()) {
			String exportKey = checkExport(res4.getString("RESOURCE_NAME"));
			if (exportKey != null) {
				discAccess.writeFile(res4.getString("RESOURCE_NAME"), exportKey, readFileContent(res4.getInt("FILE_ID")));
			}
		}
		res4.close();
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_RESOURCES_LOCKRESOURCE_KEY, statement);
		}
	}
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CmsFile file = null;
	CallableStatement statement = null;
	ResultSet res;
	try {
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_RESOURCES_READFILE_KEY);
		statement.setInt(2, currentUserId);
		statement.setInt(3, currentProjectId);
		statement.setInt(4, onlineProjectId);
		statement.setString(5, filename);
		statement.execute();
		res = (ResultSet) statement.getObject(1);
		if (res.next()) {
			int resId = res.getInt(m_cq.C_RESOURCES_RESOURCE_ID);
			int parentId = res.getInt(m_cq.C_RESOURCES_PARENT_ID);
			int resType = res.getInt(m_cq.C_RESOURCES_RESOURCE_TYPE);
			int resFlags = res.getInt(m_cq.C_RESOURCES_RESOURCE_FLAGS);
			int userId = res.getInt(m_cq.C_RESOURCES_USER_ID);
			int groupId = res.getInt(m_cq.C_RESOURCES_GROUP_ID);
			int fileId = res.getInt(m_cq.C_RESOURCES_FILE_ID);
			int accessFlags = res.getInt(m_cq.C_RESOURCES_ACCESS_FLAGS);
			int state = res.getInt(m_cq.C_RESOURCES_STATE);
			int lockedBy = res.getInt(m_cq.C_RESOURCES_LOCKED_BY);
			int launcherType = res.getInt(m_cq.C_RESOURCES_LAUNCHER_TYPE);
			String launcherClass = res.getString(m_cq.C_RESOURCES_LAUNCHER_CLASSNAME);
			long created = SqlHelper.getTimestamp(res, m_cq.C_RESOURCES_DATE_CREATED).getTime();
			long modified = SqlHelper.getTimestamp(res, m_cq.C_RESOURCES_DATE_LASTMODIFIED).getTime();
			int modifiedBy = res.getInt(m_cq.C_RESOURCES_LASTMODIFIED_BY);
			int resSize = res.getInt(m_cq.C_RESOURCES_SIZE);
			// read the file_content from an oracle blob
			oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB(m_cq.C_RESOURCES_FILE_CONTENT);
			byte[] content = new byte[ (int) blob.length()];
			content = blob.getBytes(1, (int) blob.length());
			// output for testing:
			//		String out_buffer = new String(content);
			//		System.out.println(out_buffer);
			/*InputStream inStream = res.getBinaryStream(m_cq.C_RESOURCES_FILE_CONTENT);
			   
			  ByteArrayOutputStream outStream=new ByteArrayOutputStream();
			  byte[] buffer= new byte[128];
			  while (true) {
			  int bytesRead = inStream.read(buffer);
			  if (bytesRead ==-1) break;
			  outStream.write(buffer,0,bytesRead);
			  }
			  byte[] content=outStream.toByteArray();*/

			file = new CmsFile(resId, parentId, fileId, filename, resType, resFlags, userId, groupId, currentProjectId, accessFlags, state, lockedBy, launcherType, launcherClass, created, modified, modifiedBy, content, resSize);
			res.close();
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
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_RESOURCES_READFILE_KEY, statement);
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CmsFile file = null;
	CallableStatement statement = null;
	ResultSet res;
	try {
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_RESOURCES_READFILEACC_KEY);
		statement.setInt(2, currentUserId);
		statement.setInt(3, currentProjectId);
		statement.setString(4, filename);
		statement.execute();
		res = (ResultSet) statement.getObject(1);
		if (res.next()) {
			int resId = res.getInt(m_cq.C_RESOURCES_RESOURCE_ID);
			int parentId = res.getInt(m_cq.C_RESOURCES_PARENT_ID);
			int resType = res.getInt(m_cq.C_RESOURCES_RESOURCE_TYPE);
			int resFlags = res.getInt(m_cq.C_RESOURCES_RESOURCE_FLAGS);
			int userId = res.getInt(m_cq.C_RESOURCES_USER_ID);
			int groupId = res.getInt(m_cq.C_RESOURCES_GROUP_ID);
			int fileId = res.getInt(m_cq.C_RESOURCES_FILE_ID);
			int accessFlags = res.getInt(m_cq.C_RESOURCES_ACCESS_FLAGS);
			int state = res.getInt(m_cq.C_RESOURCES_STATE);
			int lockedBy = res.getInt(m_cq.C_RESOURCES_LOCKED_BY);
			int launcherType = res.getInt(m_cq.C_RESOURCES_LAUNCHER_TYPE);
			String launcherClass = res.getString(m_cq.C_RESOURCES_LAUNCHER_CLASSNAME);
			long created = SqlHelper.getTimestamp(res, m_cq.C_RESOURCES_DATE_CREATED).getTime();
			long modified = SqlHelper.getTimestamp(res, m_cq.C_RESOURCES_DATE_LASTMODIFIED).getTime();
			int modifiedBy = res.getInt(m_cq.C_RESOURCES_LASTMODIFIED_BY);
			int resSize = res.getInt(m_cq.C_RESOURCES_SIZE);
			// read the file_content from an oracle blob
			oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB(m_cq.C_RESOURCES_FILE_CONTENT);
			byte[] content = new byte[ (int) blob.length()];
			content = blob.getBytes(1, (int) blob.length());
			// output for testing:
			//		String out_buffer = new String(content);
			//		System.out.println(out_buffer);
			/*InputStream inStream = res.getBinaryStream(m_cq.C_RESOURCES_FILE_CONTENT);
			   
			  ByteArrayOutputStream outStream=new ByteArrayOutputStream();
			  byte[] buffer= new byte[128];
			  while (true) {
			  int bytesRead = inStream.read(buffer);
			  if (bytesRead ==-1) break;
			  outStream.write(buffer,0,bytesRead);
			  }
			  byte[] content=outStream.toByteArray();*/ 

			file = new CmsFile(resId, parentId, fileId, filename, resType, resFlags, userId, groupId, 
								currentProjectId, accessFlags, state, lockedBy, launcherType, launcherClass, 
								created, modified, modifiedBy, content, resSize);
			res.close();	
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
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_RESOURCES_READFILEACC_KEY, statement);
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
	protected byte[] readFileContent(int fileId)
		throws CmsException {
		PreparedStatement statement = null;
		ResultSet res = null;
		byte[] returnValue = null;
		try {  
			// read fileContent from database
			statement = m_pool.getPreparedStatement(m_cq.C_FILE_READ_KEY);
			statement.setInt(1,fileId);
			res = statement.executeQuery();
			if (res.next()) {
				oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB(m_cq.C_FILE_CONTENT);
				byte[] content = new byte[(int) blob.length()];
				content = blob.getBytes(1, (int) blob.length());
				returnValue = content;
// output for testing:
//		String out_buffer = new String(content);
//		System.out.println(out_buffer);
				} else {
				  throw new CmsException("["+this.getClass().getName()+"]"+fileId,CmsException.C_NOT_FOUND);  
			}
			res.close();       
		} catch (SQLException e){
			throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}finally {
			if( statement != null) {
				m_pool.putPreparedStatement(m_cq.C_FILE_READ_KEY, statement);
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
	PreparedStatement statement = null;
	ResultSet res = null;
	Hashtable session = null;
	try {
		statement = m_pool.getPreparedStatement(m_cq.C_SESSION_READ_KEY);
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
		res.close();
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (statement != null) {
			m_pool.putPreparedStatement(m_cq.C_SESSION_READ_KEY, statement);
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
		
		Serializable property=null;

		ResultSet res = null;
		PreparedStatement statement = null;
			
		// create get the property data from the database
		try {
		  statement=m_pool.getPreparedStatement(m_cq.C_SYSTEMPROPERTIES_READ_KEY);
		  statement.setString(1,name);
		  res = statement.executeQuery();
		  if(res.next()) {
			    oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.C_SYSTEMPROPERTY_VALUE);
				byte[] value = new byte[(int) blob.length()];			    
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				property=(Serializable)oin.readObject();                
			}	
		   res.close();
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
			if( statement != null) {
				m_pool.putPreparedStatement(m_cq.C_SYSTEMPROPERTIES_READ_KEY, statement);
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
		PreparedStatement statement = null;
		ResultSet res = null;
		CmsUser user = null;

		try	{			
			statement = m_pool.getPreparedStatement(m_cq.C_USERS_READID_KEY);
			statement.setInt(1,id);
			res = statement.executeQuery();
			
			// create new Cms user object
			if(res.next()) {
				// read the additional infos.
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.C_USERS_USER_INFO); 
				byte[] value = new byte[(int) blob.length()]; 
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				user = new CmsUser(res.getInt(m_cq.C_USERS_USER_ID),
								   res.getString(m_cq.C_USERS_USER_NAME),
								   res.getString(m_cq.C_USERS_USER_PASSWORD),
								   res.getString(m_cq.C_USERS_USER_RECOVERY_PASSWORD),
								   res.getString(m_cq.C_USERS_USER_DESCRIPTION),
								   res.getString(m_cq.C_USERS_USER_FIRSTNAME),
								   res.getString(m_cq.C_USERS_USER_LASTNAME),
								   res.getString(m_cq.C_USERS_USER_EMAIL),
								   SqlHelper.getTimestamp(res,m_cq.C_USERS_USER_LASTLOGIN).getTime(),
								   SqlHelper.getTimestamp(res,m_cq.C_USERS_USER_LASTUSED).getTime(),
								   res.getInt(m_cq.C_USERS_USER_FLAGS),
								   info,
								   new CmsGroup(res.getInt(m_cq.C_GROUPS_GROUP_ID),
												res.getInt(m_cq.C_GROUPS_PARENT_GROUP_ID),
												res.getString(m_cq.C_GROUPS_GROUP_NAME),
												res.getString(m_cq.C_GROUPS_GROUP_DESCRIPTION),
												res.getInt(m_cq.C_GROUPS_GROUP_FLAGS)),
								   res.getString(m_cq.C_USERS_USER_ADDRESS),
								   res.getString(m_cq.C_USERS_USER_SECTION),
								   res.getInt(m_cq.C_USERS_USER_TYPE));
			} else {
				res.close();
				throw new CmsException("["+this.getClass().getName()+"]"+id,CmsException.C_NO_USER);
			}

			res.close();
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
			if( statement != null) {
				m_pool.putPreparedStatement(m_cq.C_USERS_READID_KEY, statement);
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
		PreparedStatement statement = null;
		ResultSet res = null;
		CmsUser user = null;
		
		try	{			
			statement = m_pool.getPreparedStatement(m_cq.C_USERS_READ_KEY);
			statement.setString(1,name);
			statement.setInt(2,type);
   
			res = statement.executeQuery();
			
			// create new Cms user object
			if(res.next()) {
				// read the additional infos.
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.C_USERS_USER_INFO);
				byte[] value = new byte[(int) blob.length()];
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				user = new CmsUser(res.getInt(m_cq.C_USERS_USER_ID),
								   res.getString(m_cq.C_USERS_USER_NAME),
								   res.getString(m_cq.C_USERS_USER_PASSWORD),
								   res.getString(m_cq.C_USERS_USER_RECOVERY_PASSWORD),
								   res.getString(m_cq.C_USERS_USER_DESCRIPTION),
								   res.getString(m_cq.C_USERS_USER_FIRSTNAME),
								   res.getString(m_cq.C_USERS_USER_LASTNAME),
								   res.getString(m_cq.C_USERS_USER_EMAIL),
								   SqlHelper.getTimestamp(res,m_cq.C_USERS_USER_LASTLOGIN).getTime(),
								   SqlHelper.getTimestamp(res,m_cq.C_USERS_USER_LASTUSED).getTime(),
								   res.getInt(m_cq.C_USERS_USER_FLAGS),
								   info,
								   new CmsGroup(res.getInt(m_cq.C_GROUPS_GROUP_ID),
												res.getInt(m_cq.C_GROUPS_PARENT_GROUP_ID),
												res.getString(m_cq.C_GROUPS_GROUP_NAME),
												res.getString(m_cq.C_GROUPS_GROUP_DESCRIPTION),
												res.getInt(m_cq.C_GROUPS_GROUP_FLAGS)),
								   res.getString(m_cq.C_USERS_USER_ADDRESS),
								   res.getString(m_cq.C_USERS_USER_SECTION),
								   res.getInt(m_cq.C_USERS_USER_TYPE));
			} else {
				res.close();
				throw new CmsException("["+this.getClass().getName()+"]"+name,CmsException.C_NO_USER);
			}

			res.close();            
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
			if( statement != null) {
				m_pool.putPreparedStatement(m_cq.C_USERS_READ_KEY, statement);
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
		PreparedStatement statement = null;
		ResultSet res = null;
		CmsUser user = null;

		try	{			
			statement = m_pool.getPreparedStatement(m_cq.C_USERS_READPW_KEY);
			statement.setString(1,name);
			statement.setString(2,digest(password));
			statement.setInt(3,type);
			res = statement.executeQuery();
			
			// create new Cms user object
			if(res.next()) {
				// read the additional infos.
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.C_USERS_USER_INFO);
				byte[] value = new byte[(int) blob.length()]; 
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				user = new CmsUser(res.getInt(m_cq.C_USERS_USER_ID),
								   res.getString(m_cq.C_USERS_USER_NAME),
								   res.getString(m_cq.C_USERS_USER_PASSWORD),
								   res.getString(m_cq.C_USERS_USER_RECOVERY_PASSWORD),
								   res.getString(m_cq.C_USERS_USER_DESCRIPTION),
								   res.getString(m_cq.C_USERS_USER_FIRSTNAME),
								   res.getString(m_cq.C_USERS_USER_LASTNAME),
								   res.getString(m_cq.C_USERS_USER_EMAIL),
								   SqlHelper.getTimestamp(res,m_cq.C_USERS_USER_LASTLOGIN).getTime(),
								   SqlHelper.getTimestamp(res,m_cq.C_USERS_USER_LASTUSED).getTime(),
								   res.getInt(m_cq.C_USERS_USER_FLAGS),
								   info,
								   new CmsGroup(res.getInt(m_cq.C_GROUPS_GROUP_ID),
												res.getInt(m_cq.C_GROUPS_PARENT_GROUP_ID),
												res.getString(m_cq.C_GROUPS_GROUP_NAME),
												res.getString(m_cq.C_GROUPS_GROUP_DESCRIPTION),
												res.getInt(m_cq.C_GROUPS_GROUP_FLAGS)),
								   res.getString(m_cq.C_USERS_USER_ADDRESS),
								   res.getString(m_cq.C_USERS_USER_SECTION),
								   res.getInt(m_cq.C_USERS_USER_TYPE));
			} else {
				res.close();
				throw new CmsException("["+this.getClass().getName()+"]"+name,CmsException.C_NO_USER);
			}

			res.close();
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
			if( statement != null) {
				m_pool.putPreparedStatement(m_cq.C_USERS_READPW_KEY, statement);
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
			
		int agentId = this.findAgent(roleId);
		
		PreparedStatement statement = null;
		ResultSet res = null;
		CmsUser user = null;

		try	{			
			statement = m_pool.getPreparedStatement(m_cq.C_USERS_READID_KEY);
			statement.setInt(1,agentId);
			res = statement.executeQuery();
			
			// create new Cms user object
			if(res.next()) {
				// read the additional infos.
				oracle.sql.BLOB blob = ((OracleResultSet)res).getBLOB(m_cq.C_USERS_USER_INFO); 
				byte[] value = new byte[(int) blob.length()]; 
				value = blob.getBytes(1, (int) blob.length());
				// now deserialize the object
				ByteArrayInputStream bin= new ByteArrayInputStream(value);
				ObjectInputStream oin = new ObjectInputStream(bin);
				Hashtable info=(Hashtable)oin.readObject();

				user = new CmsUser(res.getInt(m_cq.C_USERS_USER_ID),
								   res.getString(m_cq.C_USERS_USER_NAME),
								   res.getString(m_cq.C_USERS_USER_PASSWORD),
								   res.getString(m_cq.C_USERS_USER_RECOVERY_PASSWORD),
								   res.getString(m_cq.C_USERS_USER_DESCRIPTION),
								   res.getString(m_cq.C_USERS_USER_FIRSTNAME),
								   res.getString(m_cq.C_USERS_USER_LASTNAME),
								   res.getString(m_cq.C_USERS_USER_EMAIL),
								   SqlHelper.getTimestamp(res,m_cq.C_USERS_USER_LASTLOGIN).getTime(),
								   SqlHelper.getTimestamp(res,m_cq.C_USERS_USER_LASTUSED).getTime(),
								   res.getInt(m_cq.C_USERS_USER_FLAGS),
								   info,
								   new CmsGroup(res.getInt(m_cq.C_GROUPS_GROUP_ID),
												res.getInt(m_cq.C_GROUPS_PARENT_GROUP_ID),
												res.getString(m_cq.C_GROUPS_GROUP_NAME),
												res.getString(m_cq.C_GROUPS_GROUP_DESCRIPTION),
												res.getInt(m_cq.C_GROUPS_GROUP_FLAGS)),
								   res.getString(m_cq.C_USERS_USER_ADDRESS),
								   res.getString(m_cq.C_USERS_USER_SECTION),
								   res.getInt(m_cq.C_USERS_USER_TYPE));
			} else {
				res.close();
				throw new CmsException("["+this.getClass().getName()+"]"+roleId,CmsException.C_NO_USER);
			}

			res.close();
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
			if( statement != null) {
				m_pool.putPreparedStatement(m_cq.C_USERS_READID_KEY, statement);
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	ResultSet res = null;
	CmsResource resource = null;
	Vector resources = new Vector();
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_RESOURCES_UNLOCKRESOURCE_KEY);
		statement.setInt(1, currentUser.getId());
		statement.setInt(2, currentProject.getId());
		statement.setString(3, resourcename);
		statement.execute();
		res = (ResultSet) statement.getObject(4);
		while (res.next()) {
			int resId = res.getInt(m_cq.C_RESOURCES_RESOURCE_ID);
			int parentId = res.getInt(m_cq.C_RESOURCES_PARENT_ID);
			String resName = res.getString(m_cq.C_RESOURCES_RESOURCE_NAME);
			int resType = res.getInt(m_cq.C_RESOURCES_RESOURCE_TYPE);
			int resFlags = res.getInt(m_cq.C_RESOURCES_RESOURCE_FLAGS);
			int userId = res.getInt(m_cq.C_RESOURCES_USER_ID);
			int groupId = res.getInt(m_cq.C_RESOURCES_GROUP_ID);
			int projectId = res.getInt(m_cq.C_RESOURCES_PROJECT_ID);
			int fileId = res.getInt(m_cq.C_RESOURCES_FILE_ID);
			int accessFlags = res.getInt(m_cq.C_RESOURCES_ACCESS_FLAGS);
			int state = res.getInt(m_cq.C_RESOURCES_STATE);
			int lockedBy = res.getInt(m_cq.C_RESOURCES_LOCKED_BY);
			int launcherType = res.getInt(m_cq.C_RESOURCES_LAUNCHER_TYPE);
			String launcherClass = res.getString(m_cq.C_RESOURCES_LAUNCHER_CLASSNAME);
			long created = SqlHelper.getTimestamp(res, m_cq.C_RESOURCES_DATE_CREATED).getTime();
			long modified = SqlHelper.getTimestamp(res, m_cq.C_RESOURCES_DATE_LASTMODIFIED).getTime();
			int modifiedBy = res.getInt(m_cq.C_RESOURCES_LASTMODIFIED_BY);
			int resSize = res.getInt(m_cq.C_RESOURCES_SIZE);
			resource = new CmsResource(resId, parentId, fileId, resName, resType, resFlags, userId, groupId, 
										projectId, accessFlags, state, lockedBy, launcherType, launcherClass, 
										created, modified, modifiedBy, resSize);
			resources.addElement(resource);
		}
		res.close();
		return resources;
	} catch (SQLException sqlexc) {
		CmsException cmsException = getCmsException("[" + this.getClass().getName() + "] ", sqlexc);
		throw cmsException;
	} catch (Exception e) {
		throw new CmsException("[" + this.getClass().getName() + "]", e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_RESOURCES_UNLOCKRESOURCE_KEY, statement);
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	byte[] value = null;
	PreparedStatement statement = null;
	PreparedStatement statement2 = null;
	PreparedStatement nextStatement = null;
	OraclePreparedStatement trimStatement = null;
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
		statement = pool.getPreparedStatement(cq.C_PLSQL_SESSION_UPDATE_KEY);
		statement.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
		statement.setString(2, sessionId);
		retValue = statement.executeUpdate();
		// now update the session_data	
		statement2 = pool.getNextPreparedStatement(statement, cq.C_PLSQL_SESSION_FORUPDATE_KEY);
		statement2.setString(1, sessionId);
		Connection conn = pool.getConnectionOfStatement(statement);
		conn.setAutoCommit(false);
		res = statement2.executeQuery();
		while (res.next()) {
			oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SESSION_DATA");
			// first trim the blob to 0 bytes, otherwise there could be left some bytes
			// of the old content
			trimStatement = (OraclePreparedStatement) pool.getNextPreparedStatement(statement, cq.C_TRIMBLOB_KEY);
			trimStatement.setBLOB(1, blob);
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
		res.close();
		// for the oracle-driver commit or rollback must be executed manually
		// because setAutoCommit = false in CmsDbPool.CmsDbPool
		nextStatement = pool.getNextPreparedStatement(statement, cq.C_COMMIT_KEY);
		nextStatement.execute();
		conn.setAutoCommit(true);
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (IOException e) {
		throw new CmsException("[" + this.getClass().getName() + "]:" + CmsException.C_SERIALIZATION, e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_SESSION_UPDATE_KEY, statement);
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	boolean userInGroup = false;
	CallableStatement statement = null;
	try {
		// create statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_GROUPS_USERINGROUP_KEY);
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
			pool.putPreparedStatement(cq.C_PLSQL_GROUPS_USERINGROUP_KEY, statement);
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	PreparedStatement statement = null;
	PreparedStatement nextStatement = null;
	OraclePreparedStatement trimStatement = null;	
	try {
		// update the file header in the RESOURCE database.
		writeFileHeader(project, file, changed);
		// update the file content in the FILES database.
		statement = pool.getPreparedStatement(cq.C_PLSQL_FILESFORUPDATE_KEY);
		statement.setInt(1, file.getFileId());
		Connection conn = pool.getConnectionOfStatement(statement);
		conn.setAutoCommit(false);
		ResultSet res = statement.executeQuery();
		try {
			while (res.next()) {
				oracle.sql.BLOB blobnew = ((OracleResultSet) res).getBLOB("FILE_CONTENT");
				// first trim the blob to 0 bytes, otherwise ther could be left some bytes
				// of the old content
				trimStatement = (OraclePreparedStatement) pool.getNextPreparedStatement(statement, cq.C_TRIMBLOB_KEY);
				trimStatement.setBLOB(1, blobnew);
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
			res.close();
			// for the oracle-driver commit or rollback must be executed manually
			// because setAutoCommit = false in CmsDbPool.CmsDbPool
			nextStatement = pool.getNextPreparedStatement(statement, cq.C_COMMIT_KEY);
			nextStatement.execute();
		    conn.setAutoCommit(true);			
		} catch (IOException e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), e);
		}
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_FILESFORUPDATE_KEY, statement);
		}
	}
}
/**
 * Writes the fileheader to the Cms.
 * 
 * @param project The project in which the resource will be used.
 * @param onlineProject The online project of the OpenCms.
 * @param file The new file.
 * @param changed Flag indicating if the file state must be set to changed.
 *
 * @exception CmsException Throws CmsException if operation was not succesful.
 */
public void writeFileHeader(CmsProject project, CmsFile file, boolean changed) throws CmsException {
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	ResultSet res;
	ResultSet tmpres;
	byte[] content;
	PreparedStatement statementFileRead = null;
	PreparedStatement statementResourceUpdate = null;
	PreparedStatement statementFileIns = null;
	PreparedStatement statementFileUpd = null;
	PreparedStatement commitStatement = null;
	try {
		// check if the file content for this file is already existing in the
		// offline project. If not, load it from the online project and add it
		// to the offline project.
		if ((file.getState() == C_STATE_UNCHANGED) && (changed == true)) {
			// read file content form the online project
			statementFileRead = m_pool.getPreparedStatement(m_cq.C_FILE_READ_KEY);
			statementFileRead.setInt(1, file.getFileId());
			res = statementFileRead.executeQuery();
			if (res.next()) {
				// read the file_content from an oracle blob
				oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB(m_cq.C_RESOURCES_FILE_CONTENT);
				content = new byte[ (int) blob.length()];
				content = blob.getBytes(1, (int) blob.length());
			} else {
				throw new CmsException("[" + this.getClass().getName() + "]" + file.getAbsolutePath(), CmsException.C_NOT_FOUND);
			}
			res.close();
			// add the file content to the offline project.
			// first insert new file without file_content, then update the file_content
			// these two steps are necessary because of using BLOBs	in the Oracle DB
			file.setFileId(nextId(C_TABLE_FILES));
			statementFileIns = pool.getPreparedStatement(cq.C_PLSQL_FILESFORINSERT_KEY);
			statementFileIns.setInt(1, file.getFileId());
			statementFileIns.executeUpdate();

			// update the file content in the FILES database.
			try {
				statementFileUpd = pool.getNextPreparedStatement(statementFileIns, cq.C_PLSQL_FILESFORUPDATE_KEY);
				statementFileUpd.setInt(1, file.getFileId());
				Connection conn = pool.getConnectionOfStatement(statementFileIns);
				conn.setAutoCommit(false);
				ResultSet resUpd = statementFileUpd.executeQuery();
				while (resUpd.next()) {
					oracle.sql.BLOB blobnew = ((OracleResultSet) resUpd).getBLOB("FILE_CONTENT");
					ByteArrayInputStream instream = new ByteArrayInputStream(content);
					OutputStream outstream = blobnew.getBinaryOutputStream();
					byte[] chunk = new byte[blobnew.getChunkSize()];
					int i = -1;
					while ((i = instream.read(chunk)) != -1) {
						outstream.write(chunk, 0, i);
					}
					instream.close();
					outstream.close();
				}
				resUpd.close();
				// for the oracle-driver commit or rollback must be executed manually
				// because setAutoCommit = false in CmsDbPool.CmsDbPool
				commitStatement = pool.getNextPreparedStatement(statementFileIns, cq.C_COMMIT_KEY);
				commitStatement.execute();
				conn.setAutoCommit(true);
			} catch (IOException e) {
				throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), e);
			}


			/*			
			PreparedStatement statementFileWrite = null;
			try {
			file.setFileId(nextId(C_TABLE_FILES));
			statementFileWrite = m_pool.getPreparedStatement(m_cq.C_FILES_WRITE_KEY);
			statementFileWrite.setInt(1, file.getFileId());
			statementFileWrite.setBytes(2, content);
			statementFileWrite.executeUpdate();
			*/
			/*
			} catch (SQLException se) {
			if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsAccessFileMySql] " + se.getMessage());
			se.printStackTrace();
			}
			} finally {
			if (statementFileWrite != null) {
			m_pool.putPreparedStatement(m_cq.C_FILES_WRITE_KEY, statementFileWrite);
			}
			}
			*/
		}
		// update resource in the database
		statementResourceUpdate = m_pool.getPreparedStatement(m_cq.C_RESOURCES_UPDATE_KEY);
		statementResourceUpdate.setInt(1, file.getType());
		statementResourceUpdate.setInt(2, file.getFlags());
		statementResourceUpdate.setInt(3, file.getOwnerId());
		statementResourceUpdate.setInt(4, file.getGroupId());
		statementResourceUpdate.setInt(5, file.getProjectId());
		statementResourceUpdate.setInt(6, file.getAccessFlags());
		//STATE       
		int state = file.getState();
		if ((state == C_STATE_NEW) || (state == C_STATE_CHANGED)) {
			statementResourceUpdate.setInt(7, state);
		} else {
			if (changed == true) {
				statementResourceUpdate.setInt(7, C_STATE_CHANGED);
			} else {
				statementResourceUpdate.setInt(7, file.getState());
			}
		}
		statementResourceUpdate.setInt(8, file.isLockedBy());
		statementResourceUpdate.setInt(9, file.getLauncherType());
		statementResourceUpdate.setString(10, file.getLauncherClassname());
		statementResourceUpdate.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
		statementResourceUpdate.setInt(12, file.getResourceLastModifiedBy());
		statementResourceUpdate.setInt(13, file.getLength());
		statementResourceUpdate.setInt(14, file.getFileId());
		statementResourceUpdate.setInt(15, file.getResourceId());
		statementResourceUpdate.executeUpdate();
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} finally {
		if (statementFileRead != null) {
			m_pool.putPreparedStatement(m_cq.C_FILE_READ_KEY, statementFileRead);
		}
		if (statementFileIns != null) {
			pool.putPreparedStatement(cq.C_PLSQL_FILESFORINSERT_KEY, statementFileIns);
		}
		if (statementResourceUpdate != null) {
			m_pool.putPreparedStatement(m_cq.C_RESOURCES_UPDATE_KEY, statementResourceUpdate);
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	PreparedStatement statement = null;
	PreparedStatement nextStatement = null;
	OraclePreparedStatement trimStatement = null;
	byte[] value = null;
	try {
		// serialize the object	
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(object);
		oout.close();
		value = bout.toByteArray();
		statement = pool.getPreparedStatement(cq.C_PLSQL_SYSTEMPROPERTIES_NAMEFORUPDATE_KEY);
		statement.setString(1, name);
		Connection conn = pool.getConnectionOfStatement(statement);
		conn.setAutoCommit(false);
		ResultSet res = statement.executeQuery();	
		while (res.next()) {
			oracle.sql.BLOB blob = ((OracleResultSet) res).getBLOB("SYSTEMPROPERTY_VALUE");
			// first trim the blob to 0 bytes, otherwise ther could be left some bytes
			// of the old content
			trimStatement = (OraclePreparedStatement) pool.getNextPreparedStatement(statement, cq.C_TRIMBLOB_KEY);
			trimStatement.setBLOB(1, blob);
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
		res.close();
		// for the oracle-driver commit or rollback must be executed manually
		// because setAutoCommit = false in CmsDbPool.CmsDbPool
		nextStatement = pool.getNextPreparedStatement(statement, cq.C_COMMIT_KEY);
		nextStatement.execute();
		conn.setAutoCommit(true);
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (IOException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + CmsException.C_SERIALIZATION, e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_SYSTEMPROPERTIES_NAMEFORUPDATE_KEY, statement);
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
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	byte[] value = null;
	PreparedStatement statement = null;
	PreparedStatement statement2 = null;
	PreparedStatement nextStatement = null;
	OraclePreparedStatement trimStatement = null;
	try {
		// serialize the hashtable
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(user.getAdditionalInfo());
		oout.close();
		value = bout.toByteArray();

		// write data to database     
		statement = pool.getPreparedStatement(cq.C_PLSQL_USERSWRITE_KEY);
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
		// update user_info in this special way because of using blob
		statement2 = pool.getPreparedStatement(cq.C_PLSQL_USERSFORUPDATE_KEY);
		statement2.setInt(1, user.getId());
		Connection conn = pool.getConnectionOfStatement(statement2);
		conn.setAutoCommit(false);
		ResultSet res = statement2.executeQuery();
		try {
			while (res.next()) {
				oracle.sql.BLOB blobnew = ((OracleResultSet) res).getBLOB("USER_INFO");
				// first trim the blob to 0 bytes, otherwise ther could be left some bytes
				// of the old content
				trimStatement = (OraclePreparedStatement) pool.getNextPreparedStatement(statement2, cq.C_TRIMBLOB_KEY);
				trimStatement.setBLOB(1, blobnew);
				trimStatement.setInt(2, 0);
				trimStatement.execute();
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
			res.close();
			// for the oracle-driver commit or rollback must be executed manually
			// because setAutoCommit = false in CmsDbPool.CmsDbPool
			nextStatement = pool.getNextPreparedStatement(statement2, cq.C_COMMIT_KEY);
			nextStatement.execute();
			conn.setAutoCommit(true);
		} catch (IOException e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), e);
		}
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "]" + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} catch (IOException e) {
		throw new CmsException("[CmsAccessUserInfoMySql/addUserInformation(id,object)]:" + CmsException.C_SERIALIZATION, e);
	} finally {
		if (statement != null) {
			m_pool.putPreparedStatement(m_cq.C_USERS_WRITE_KEY, statement);
		}
		if (statement2 != null) {
			pool.putPreparedStatement(cq.C_PLSQL_USERSFORUPDATE_KEY, statement2);
		}
	}
}
}

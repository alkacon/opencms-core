package com.opencms.file.oracleplsql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oracleplsql/Attic/CmsDbAccess.java,v $
 * Date   : $Date: 2000/09/29 15:36:24 $
 * Version: $Revision: 1.2 $
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
 * @version $Revision: 1.2 $ $Date: 2000/09/29 15:36:24 $ * 
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
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
//System.out.println("PL/SQL: accessRead");
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
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_ACCESS_ACCESSWRITE_KEY, statement);
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
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
			projects.addElement(new CmsProject(res.getInt(m_cq.C_PROJECTS_PROJECT_ID), 
				                               res.getString(m_cq.C_PROJECTS_PROJECT_NAME), 
				                               res.getString(m_cq.C_PROJECTS_PROJECT_DESCRIPTION), 
				                               res.getInt(m_cq.C_PROJECTS_TASK_ID), 
				                               res.getInt(m_cq.C_PROJECTS_USER_ID), 
				                               res.getInt(m_cq.C_PROJECTS_GROUP_ID), 
				                               res.getInt(m_cq.C_PROJECTS_MANAGERGROUP_ID), 
				                               res.getInt(m_cq.C_PROJECTS_PROJECT_FLAGS), 
				                               SqlHelper.getTimestamp(res, m_cq.C_PROJECTS_PROJECT_CREATEDATE), 
				                               SqlHelper.getTimestamp(res, m_cq.C_PROJECTS_PROJECT_PUBLISHDATE), 
				                               res.getInt(m_cq.C_PROJECTS_PROJECT_PUBLISHED_BY), 
				                               res.getInt(m_cq.C_PROJECTS_PROJECT_TYPE)));
		}
		res.close();
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_PROJECTS_GETALLACCESS_KEY, statement);
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
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
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
			byte[] value = res.getBytes(m_cq.C_USERS_USER_INFO);
			// now deserialize the object
			ByteArrayInputStream bin = new ByteArrayInputStream(value);
			ObjectInputStream oin = new ObjectInputStream(bin);
			Hashtable info = (Hashtable) oin.readObject();
			CmsUser user = new CmsUser(res.getInt(m_cq.C_USERS_USER_ID), 
				                       res.getString(m_cq.C_USERS_USER_NAME), 
				                       res.getString(m_cq.C_USERS_USER_PASSWORD), 
				                       res.getString(m_cq.C_USERS_USER_RECOVERY_PASSWORD), 
				                       res.getString(m_cq.C_USERS_USER_DESCRIPTION), 
				                       res.getString(m_cq.C_USERS_USER_FIRSTNAME), 
				                       res.getString(m_cq.C_USERS_USER_LASTNAME), 
				                       res.getString(m_cq.C_USERS_USER_EMAIL), 
				                       SqlHelper.getTimestamp(res, m_cq.C_USERS_USER_LASTLOGIN).getTime(),
				                       SqlHelper.getTimestamp(res, m_cq.C_USERS_USER_LASTUSED).getTime(), 
				                       res.getInt(m_cq.C_USERS_USER_FLAGS), 
				                       info, 
				                       new CmsGroup(res.getInt(m_cq.C_USERS_USER_DEFAULT_GROUP_ID), 
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
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
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
	// init statements for resources
	pool.initCallableStatement(cq.C_PLSQL_RESOURCES_LOCKRESOURCE_KEY, cq.C_PLSQL_RESOURCES_LOCKRESOURCE);
	pool.initCallableStatement(cq.C_PLSQL_RESOURCES_UNLOCKRESOURCE_KEY, cq.C_PLSQL_RESOURCES_UNLOCKRESOURCE);

	pool.initCallableStatement(cq.C_PLSQL_RESOURCES_READFOLDER_KEY, cq.C_PLSQL_RESOURCES_READFOLDER);
	pool.initRegisterOutParameter(cq.C_PLSQL_RESOURCES_READFOLDER_KEY, 1, Types.INTEGER);
	pool.initCallableStatement(cq.C_PLSQL_RESOURCES_READFILEHEADER_KEY, cq.C_PLSQL_RESOURCES_READFILEHEADER);
	pool.initRegisterOutParameter(cq.C_PLSQL_RESOURCES_READFILEHEADER_KEY, 1, Types.INTEGER);		
	
	// init statements for projects
	pool.initCallableStatement(cq.C_PLSQL_PROJECTS_GETALLACCESS_KEY, cq.C_PLSQL_PROJECTS_GETALLACCESS);
	pool.initRegisterOutParameter(cq.C_PLSQL_PROJECTS_GETALLACCESS_KEY, 1, oracle.jdbc.driver.OracleTypes.CURSOR);
	pool.initCallableStatement(cq.C_PLSQL_PROJECTS_COPYRESOURCETOPROJECT_KEY, cq.C_PLSQL_PROJECTS_COPYRESOURCETOPROJECT);
	pool.initCallableStatement(cq.C_PLSQL_PROJECTS_PUBLISHPROJECT_KEY, cq.C_PLSQL_PROJECTS_PUBLISHPROJECT);

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
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
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
public void lockResource(CmsUser currentUser, CmsProject currentProject, String resourcename, boolean force) throws CmsException {
//System.out.println("PL/SQL: lockResource");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
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
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_RESOURCES_LOCKRESOURCE_KEY, statement);
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
public void unlockResource(CmsUser currentUser, CmsProject currentProject, String resourcename) throws CmsException {
//System.out.println("PL/SQL: unlockResource");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_RESOURCES_UNLOCKRESOURCE_KEY);
		statement.setInt(1, currentUser.getId());
		statement.setInt(2, currentProject.getId());
		statement.setString(3, resourcename);
		statement.execute();
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_RESOURCES_UNLOCKRESOURCE_KEY, statement);
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
public void unlockResource(CmsUser currentUser, CmsProject currentProject, String resourcename, boolean force) throws CmsException {
System.out.println("PL/SQL: lockResource");
	com.opencms.file.oracleplsql.CmsDbPool pool = (com.opencms.file.oracleplsql.CmsDbPool) m_pool;
	com.opencms.file.oracleplsql.CmsQueries cq = (com.opencms.file.oracleplsql.CmsQueries) m_cq;
	CallableStatement statement = null;
	try {
		// create the statement
		statement = (CallableStatement) pool.getPreparedStatement(cq.C_PLSQL_RESOURCES_UNLOCKRESOURCE_KEY);
		statement.setInt(1, currentUser.getId());
		statement.setInt(2, currentProject.getId());
		statement.setString(3, resourcename);
		if (force == true) {
			statement.setString(4, "TRUE");
		} else {
			statement.setString(4, "FALSE");
		}
		statement.execute();
	} catch (Exception exc) {
		throw new CmsException("[" + this.getClass().getName() + "] " + exc.getMessage(), CmsException.C_SQL_ERROR, exc);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_RESOURCES_UNLOCKRESOURCE_KEY, statement);
		}
	}
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
	System.out.println("PL/SQL: userInGroup");
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
	} catch (SQLException e) {
		throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(), CmsException.C_SQL_ERROR, e);
	} finally {
		if (statement != null) {
			pool.putPreparedStatement(cq.C_PLSQL_GROUPS_USERINGROUP_KEY, statement);
		}
	}
	return userInGroup;
}
}

package com.opencms.file.oracleplsql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oracleplsql/Attic/CmsQueries.java,v $
 * Date   : $Date: 2001/02/01 15:37:21 $
 * Version: $Revision: 1.7 $
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

public class CmsQueries extends com.opencms.file.genericSql.CmsQueries
{
	public String C_COMMIT = "commit";
	public String C_ROLLBACK = "rollback";
	public String C_TRIMBLOB = "{call dbms_lob.trim(?,?)}";
			
	// oracle PL/SQL specific statement.
	// statements for resources
	public String C_PLSQL_RESOURCES_LOCKRESOURCE = "{call opencmsResource.lockResource(?,?,?,?,?)}";
	public String C_PLSQL_RESOURCES_UNLOCKRESOURCE = "{call opencmsResource.unlockResource(?,?,?,?)}";
	public String C_PLSQL_RESOURCES_READFOLDER = "{? = call opencmsResource.readFolder(?,?,?)}";
	public String C_PLSQL_RESOURCES_READFILEHEADER = "{? = call opencmsResource.readFileHeader(?,?,?)}";

	// read the file without checking accessRead
	public String C_PLSQL_RESOURCES_READFILE = "{? = call opencmsResource.readFile(?,?,?,?)}"; 
	// read the file with checking accessRead    
	public String C_PLSQL_RESOURCES_READFILEACC = "{? = call opencmsResource.readFile(?,?,?)}"; 

	public String C_PLSQL_RESOURCES_WRITEFILEHEADER = "{? = call opencmsResource.writeFileHeader(?,?,?)}"; 	
	public String C_PLSQL_RESOURCES_COPYFILE = "{call opencmsResource.copyFile(?,?,?,?)}";

	// statements for projects	
	public String C_PLSQL_PROJECTS_GETALLACCESS = "{? = call opencmsProject.getAllAccessibleProjects(?)}";
	public String C_PLSQL_PROJECTS_COPYRESOURCETOPROJECT = "{call opencmsProject.copyResourceToProject(?,?,?)}";
	public String C_PLSQL_PROJECTS_PUBLISHPROJECT = "{call opencmsProject.publishProject(?,?,?,?,?,?,?)}";
		
	// statements for access
	public String C_PLSQL_ACCESS_ACCESSCREATE = "{? = call opencmsAccess.accessCreate(?,?,?)}";	
	public String C_PLSQL_ACCESS_ACCESSPROJECT = "{? = call opencmsAccess.accessProject(?,?)}";
	public String C_PLSQL_ACCESS_ACCESSREAD = "{? = call opencmsAccess.accessRead(?,?,?)}";
	public String C_PLSQL_ACCESS_ACCESSWRITE = "{? = call opencmsAccess.accessWrite(?,?,?)}";
	public String C_PLSQL_ACCESS_ACCESSLOCK = "{? = call opencmsAccess.accessLock(?,?,?)}";		

	// statements for groups
	public String C_PLSQL_GROUPS_USERINGROUP = "{? = call opencmsGroup.userInGroup(?,?)}";
	public String C_PLSQL_GROUPS_GETGROUPSOFUSER = "{? = call opencmsGroup.getGroupsOfUser(?)}";
	public String C_PLSQL_GROUPS_ISMANAGEROFPROJECT = "{? = call opencmsGroup.isManagerOfProject(?,?)}";
	public String C_PLSQL_GROUPS_GETUSERSOFGROUP = "{? = call opencmsGroup.getUsersOfGroup(?,?,?)}";	

	// statements for files
	public String C_PLSQL_FILESFORUPDATE = "select file_content from cms_files where file_id = ? for update nowait";
	public String C_PLSQL_FILESFORINSERT = "insert into cms_files (file_id, file_content) values (?, empty_blob())";

	
	// statements for users
	public String C_PLSQL_USERSWRITE = "update cms_users set user_description = ?, USER_FIRSTNAME = ?, USER_LASTNAME = ?, USER_EMAIL = ?, USER_LASTLOGIN = ?, USER_LASTUSED = ?, USER_FLAGS = ?, USER_DEFAULT_GROUP_ID = ?, USER_ADDRESS = ?, USER_SECTION = ?, USER_TYPE = ? WHERE USER_ID = ? ";
	public String C_PLSQL_USERSFORUPDATE = "select user_info from cms_users where user_id = ? for update nowait";
	public String C_PLSQL_USERSFORINSERT = "insert into cms_users (USER_ID, USER_NAME, USER_PASSWORD, USER_RECOVERY_PASSWORD, USER_DESCRIPTION, USER_FIRSTNAME, USER_LASTNAME, USER_EMAIL, USER_LASTLOGIN, USER_LASTUSED, USER_FLAGS, USER_INFO, USER_DEFAULT_GROUP_ID, USER_ADDRESS, USER_SECTION, USER_TYPE) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, empty_blob(), ?, ?, ?, ?)";
	
	// statements for systemproperties
	public String C_PLSQL_SYSTEMPROPERTIES_FORINSERT = "INSERT INTO CMS_SYSTEMPROPERTIES (SYSTEMPROPERTY_ID, SYSTEMPROPERTY_NAME, SYSTEMPROPERTY_VALUE) VALUES(?,?,empty_blob())";
	public String C_PLSQL_SYSTEMPROPERTIES_FORUPDATE = "select systemproperty_value from cms_systemproperties where systemproperty_id = ? for update nowait";
	public String C_PLSQL_SYSTEMPROPERTIES_NAMEFORUPDATE = "select systemproperty_value from cms_systemproperties where systemproperty_name = ? for update nowait";
		
	// statements for sessions
	public String C_PLSQL_SESSION_FORINSERT = "INSERT into CMS_SESSIONS (SESSION_ID, SESSION_LASTUSED, SESSION_DATA) values (?,?,empty_blob())";	
	public String C_PLSQL_SESSION_FORUPDATE = "select SESSION_DATA from CMS_SESSIONS where SESSION_ID = ? for update nowait";	
	public String C_PLSQL_SESSION_UPDATE = "UPDATE CMS_SESSIONS set SESSION_LASTUSED = ? where SESSION_ID = ?";
	
}

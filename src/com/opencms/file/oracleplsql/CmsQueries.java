package com.opencms.file.oracleplsql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/oracleplsql/Attic/CmsQueries.java,v $
 * Date   : $Date: 2000/11/02 17:10:38 $
 * Version: $Revision: 1.4 $
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
	public Integer C_COMMIT_KEY = new Integer(20000);
	public String C_COMMIT = "commit";
	public Integer C_ROLLBACK_KEY = new Integer(20001);
	public String C_ROLLBACK = "rollback";
	public Integer C_TRIMBLOB_KEY = new Integer(20002);
	public String C_TRIMBLOB = "{call dbms_lob.trim(?,?)}";
			
	// oracle PL/SQL specific statement.
	// statements for resources
	public Integer C_PLSQL_RESOURCES_LOCKRESOURCE_KEY = new Integer(10000);
	public String C_PLSQL_RESOURCES_LOCKRESOURCE = "{call opencmsResource.lockResource(?,?,?,?,?)}";
//	public String C_PLSQL_RESOURCES_LOCKRESOURCE = "{call opencmsResource.lockResource(?,?,?,?)}";
	public Integer C_PLSQL_RESOURCES_UNLOCKRESOURCE_KEY = new Integer(10001);
	public String C_PLSQL_RESOURCES_UNLOCKRESOURCE = "{call opencmsResource.unlockResource(?,?,?,?)}";
//	public String C_PLSQL_RESOURCES_UNLOCKRESOURCE = "{call opencmsResource.unlockResource(?,?,?)}";
	public Integer C_PLSQL_RESOURCES_READFOLDER_KEY = new Integer(10002);
	public String C_PLSQL_RESOURCES_READFOLDER = "{? = call opencmsResource.readFolder(?,?,?)}";
	public Integer C_PLSQL_RESOURCES_READFILEHEADER_KEY = new Integer(10003);
	public String C_PLSQL_RESOURCES_READFILEHEADER = "{? = call opencmsResource.readFileHeader(?,?,?)}";

	// read the file without checking accessRead
	public Integer C_PLSQL_RESOURCES_READFILE_KEY = new Integer(10004);
	public String C_PLSQL_RESOURCES_READFILE = "{? = call opencmsResource.readFile(?,?,?,?)}"; 
	// read the file with checking accessRead    
	public Integer C_PLSQL_RESOURCES_READFILEACC_KEY = new Integer(10005);
	public String C_PLSQL_RESOURCES_READFILEACC = "{? = call opencmsResource.readFile(?,?,?)}"; 

	public Integer C_PLSQL_RESOURCES_WRITEFILEHEADER_KEY = new Integer(10006);
	public String C_PLSQL_RESOURCES_WRITEFILEHEADER = "{? = call opencmsResource.writeFileHeader(?,?,?)}"; 	
	public Integer C_PLSQL_RESOURCES_COPYFILE_KEY = new Integer(10007);
	public String C_PLSQL_RESOURCES_COPYFILE = "{call opencmsResource.copyFile(?,?,?,?)}";

	// statements for projects	
	public Integer C_PLSQL_PROJECTS_GETALLACCESS_KEY = new Integer(10010);
	public String C_PLSQL_PROJECTS_GETALLACCESS = "{? = call opencmsProject.getAllAccessibleProjects(?)}";
	public Integer C_PLSQL_PROJECTS_COPYRESOURCETOPROJECT_KEY = new Integer(10011);
	public String C_PLSQL_PROJECTS_COPYRESOURCETOPROJECT = "{call opencmsProject.copyResourceToProject(?,?,?)}";
	public Integer C_PLSQL_PROJECTS_PUBLISHPROJECT_KEY = new Integer(10012);
	public String C_PLSQL_PROJECTS_PUBLISHPROJECT = "{call opencmsProject.publishProject(?,?,?,?,?,?,?)}";
	//public String C_PLSQL_PROJECTS_PUBLISHPROJECT = "{call opencmsProject.publishProject(?,?,?)}";
		
	// statements for access
	public Integer C_PLSQL_ACCESS_ACCESSCREATE_KEY = new Integer(10020);
	public String C_PLSQL_ACCESS_ACCESSCREATE = "{? = call opencmsAccess.accessCreate(?,?,?)}";	
	public Integer C_PLSQL_ACCESS_ACCESSLOCK_KEY = new Integer(10021);
	public String C_PLSQL_ACCESS_ACCESSLOCK = "{? = call opencmsAccess.accessLock(?,?,?)}";		
	public Integer C_PLSQL_ACCESS_ACCESSPROJECT_KEY = new Integer(10022);
	public String C_PLSQL_ACCESS_ACCESSPROJECT = "{? = call opencmsAccess.accessProject(?,?)}";
	public Integer C_PLSQL_ACCESS_ACCESSREAD_KEY = new Integer(10023);
	public String C_PLSQL_ACCESS_ACCESSREAD = "{? = call opencmsAccess.accessRead(?,?,?)}";
	public Integer C_PLSQL_ACCESS_ACCESSWRITE_KEY = new Integer(10024);
	public String C_PLSQL_ACCESS_ACCESSWRITE = "{? = call opencmsAccess.accessWrite(?,?,?)}";
	public Integer C_PLSQL_ACCESS_ACCESSOWNER_KEY = new Integer(10025);
	public String C_PLSQL_ACCESS_ACCESSOWNER = "{? = call opencmsAccess.accessOwner(?,?,?,?)}";		
	public Integer C_PLSQL_ACCESS_ACCESSOTHER_KEY = new Integer(10026);
	public String C_PLSQL_ACCESS_ACCESSOTHER = "{? = call opencmsAccess.accessOther(?,?,?,?)}";
	public Integer C_PLSQL_ACCESS_ACCESSGROUP_KEY = new Integer(10027);
	public String C_PLSQL_ACCESS_ACCESSGROUP = "{? = call opencmsAccess.accessGroup(?,?,?,?)}";		

	// statements for groups
	public Integer C_PLSQL_GROUPS_USERINGROUP_KEY = new Integer(10030);
	public String C_PLSQL_GROUPS_USERINGROUP = "{? = call opencmsGroup.userInGroup(?,?)}";
	public Integer C_PLSQL_GROUPS_GETGROUPSOFUSER_KEY = new Integer(10031);
	public String C_PLSQL_GROUPS_GETGROUPSOFUSER = "{? = call opencmsGroup.getGroupsOfUser(?)}";
	public Integer C_PLSQL_GROUPS_ISMANAGEROFPROJECT_KEY = new Integer(10032);
	public String C_PLSQL_GROUPS_ISMANAGEROFPROJECT = "{? = call opencmsGroup.isManagerOfProject(?,?)}";
	public Integer C_PLSQL_GROUPS_GETUSERSOFGROUP_KEY = new Integer(10033);
	public String C_PLSQL_GROUPS_GETUSERSOFGROUP = "{? = call opencmsGroup.getUsersOfGroup(?,?,?)}";	

	// statements for files
	public Integer C_PLSQL_FILESFORUPDATE_KEY = new Integer(10040);
	public String C_PLSQL_FILESFORUPDATE = "select file_content from cms_files where file_id = ? for update nowait";
	public Integer C_PLSQL_FILESFORINSERT_KEY = new Integer(10041);
	public String C_PLSQL_FILESFORINSERT = "insert into cms_files (file_id, file_content) values (?, empty_blob())";
	}

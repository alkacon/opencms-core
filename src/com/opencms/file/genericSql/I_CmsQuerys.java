/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/I_CmsQuerys.java,v $
 * Date   : $Date: 2000/06/08 16:00:43 $
 * Version: $Revision: 1.23 $
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

package com.opencms.file.genericSql;

/**
 * This interface is defines all queries used in the DB-Access class.  
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.23 $ $Date: 2000/06/08 16:00:43 $
 */
public interface I_CmsQuerys {
    
    // Common constants   
	static String C_DATABASE_PREFIX = "CMS_";
	
	// Constants for resources tables
	public static final String C_RESOURCES_RESOURCE_ID="RESOURCE_ID";
    public static final String C_RESOURCES_PARENT_ID="PARENT_ID";
    public static final String C_RESOURCES_RESOURCE_TYPE="RESOURCE_TYPE";
    public static final String C_RESOURCES_RESOURCE_FLAGS="RESOURCE_FLAGS";
    public static final String C_RESOURCES_FILE_ID="FILE_ID";
    public static final String C_RESOURCES_RESOURCE_NAME="RESOURCE_NAME";
    public static final String C_RESOURCES_USER_ID="USER_ID";
    public static final String C_RESOURCES_GROUP_ID="GROUP_ID";
    public static final String C_RESOURCES_PROJECT_ID="PROJECT_ID";
	public static final String C_PROJECT_ID_RESOURCES=C_DATABASE_PREFIX + "RESOURCES.PROJECT_ID";
	public static final String C_RESOURCE_FLAGS="RESOURCE_FLAGS";
    public static final String C_RESOURCES_ACCESS_FLAGS="ACCESS_FLAGS";
    public static final String C_RESOURCES_STATE="STATE";
    public static final String C_RESOURCES_LOCKED_BY="LOCKED_BY";
    public static final String C_RESOURCES_LAUNCHER_TYPE="LAUNCHER_TYPE";
    public static final String C_RESOURCES_LAUNCHER_CLASSNAME="LAUNCHER_CLASSNAME";    
	public static final String C_RESOURCES_DATE_CREATED="DATE_CREATED";    
    public static final String C_RESOURCES_DATE_LASTMODIFIED="DATE_LASTMODIFIED";    
    public static final String C_RESOURCES_SIZE="SIZE";
	public static final String C_RESOURCES_LASTMODIFIED_BY="RESOURCE_LASTMODIFIED_BY";
    public static final String C_RESOURCES_FILE_CONTENT="FILE_CONTENT";
    
	
	// Constants for resources
	public static final Integer C_RESOURCES_MAXID_KEY = new Integer(100);
	public static final String C_RESOURCES_MAXID = "SELECT MAX(RESOURCE_ID) FROM " + C_DATABASE_PREFIX + "RESOURCES";	
	 
    public static final Integer C_RESOURCES_READ_KEY = new Integer(102);
	public static final String C_RESOURCES_READ = "SELECT * FROM " + C_DATABASE_PREFIX + "RESOURCES WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";
  
    public static final Integer C_RESOURCES_WRITE_KEY = new Integer(103);
	public static final String C_RESOURCES_WRITE = "INSERT INTO " + C_DATABASE_PREFIX + "RESOURCES VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
	
	// Constants for files
	public static final Integer C_FILES_MAXID_KEY = new Integer(150);
	public static final String C_FILES_MAXID = "SELECT MAX(FILE_ID) FROM " + C_DATABASE_PREFIX + "FILES";	
	
	

    // Constants for Groups table
    public static final String C_GROUPS_GROUP_ID="GROUP_ID";
    public static final String C_GROUPS_PARENT_GROUP_ID="PARENT_GROUP_ID";
    public static final String C_GROUPS_GROUP_NAME="GROUP_NAME"; 
    public static final String C_GROUPS_GROUP_DESCRIPTION="GROUP_DESCRIPTION";      
    public static final String C_GROUPS_GROUP_FLAGS="GROUP_FLAGS";  
                         
    // Constants for Groups
    public static final Integer C_GROUPS_MAXID_KEY = new Integer(200);
	public static final String C_GROUPS_MAXID = "SELECT MAX(GROUP_ID) FROM " + C_DATABASE_PREFIX + "GROUPS";	
	
    public static final Integer C_GROUPS_READGROUP_KEY = new Integer(201);
    public static final String C_GROUPS_READGROUP = "SELECT * FROM " + C_DATABASE_PREFIX 
                                                  + "GROUPS WHERE GROUP_NAME = ?";

    public static final Integer C_GROUPS_READGROUP2_KEY = new Integer(202);
    public static final String C_GROUPS_READGROUP2 = "SELECT * FROM " + C_DATABASE_PREFIX 
                                                  + "GROUPS WHERE GROUP_ID = ?";
  
    public static final Integer C_GROUPS_CREATEGROUP_KEY = new Integer(203);
    public static final String C_GROUPS_CREATEGROUP = "INSERT INTO " + C_DATABASE_PREFIX + "GROUPS VALUES(?,?,?,?,?)";
    
	public static final Integer C_GROUPS_WRITEGROUP_KEY = new Integer(204);
    public static final String C_GROUPS_WRITEGROUP = "UPDATE " + C_DATABASE_PREFIX + "GROUPS SET GROUP_DESCRIPTION = ?, GROUP_FLAGS = ?, PARENT_GROUP_ID = ? WHERE GROUP_ID = ? ";

    public static final Integer C_GROUPS_DELETEGROUP_KEY = new Integer(205);
    public static final String C_GROUPS_DELETEGROUP = "DELETE FROM " + C_DATABASE_PREFIX + "GROUPS WHERE GROUP_NAME = ?";
   
    public static final Integer C_GROUPS_GETGROUPS_KEY = new Integer(206);
    public static final String C_GROUPS_GETGROUPS = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS";
  
    public static final Integer C_GROUPS_GETCHILD_KEY = new Integer(207);
    public static final String C_GROUPS_GETCHILD = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS WHERE PARENT_GROUP_ID = ?";
    
    public static final Integer C_GROUPS_GETPARENT_KEY = new Integer(208);
    public static final String C_GROUPS_GETPARENT = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS WHERE GROUP_ID = ?";
       
    public static final Integer C_GROUPS_GETGROUPSOFUSER_KEY = new Integer(209);
    public static final String C_GROUPS_GETGROUPSOFUSER = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS AS G, "
                                                        + C_DATABASE_PREFIX + "USERS AS U, "
                                                        + C_DATABASE_PREFIX + "GROUPUSERS AS GU  "
                                                        + "where U.USER_NAME = ? AND U.USER_ID=GU.USER_ID "
                                                        + "AND GU.GROUP_ID = G.GROUP_ID";
    
    public static final Integer C_GROUPS_ADDUSERTOGROUP_KEY = new Integer(210);
    public static final String C_GROUPS_ADDUSERTOGROUP = "INSERT INTO " + C_DATABASE_PREFIX + "GROUPUSERS VALUES(?,?,?)";

    public static final Integer C_GROUPS_USERINGROUP_KEY = new Integer(211);
    public static final String C_GROUPS_USERINGROUP = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPUSERS WHERE GROUP_ID = ? AND USER_ID = ?";

    public static final Integer C_GROUPS_GETUSERSOFGROUP_KEY = new Integer(212);
    public static final String C_GROUPS_GETUSERSOFGROUP = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS AS G, "
														  + C_DATABASE_PREFIX + "USERS AS U, "
														  + C_DATABASE_PREFIX + "GROUPUSERS AS GU, "
														  + C_DATABASE_PREFIX + "GROUPS AS DG "
														  + "where G.GROUP_NAME = ? AND U.USER_ID=GU.USER_ID "
														  + "AND GU.GROUP_ID = G.GROUP_ID "
														  + "AND U.USER_DEFAULT_GROUP_ID = DG.GROUP_ID "
														  + "AND U.USER_TYPE = ?";

	public static final Integer C_GROUPS_REMOVEUSERFROMGROUP_KEY = new Integer(213);
    public static final String C_GROUPS_REMOVEUSERFROMGROUP = "DELETE FROM " + C_DATABASE_PREFIX + "GROUPUSERS WHERE GROUP_ID = ? AND USER_ID = ?";
    
    // Constants for Systemproperties
    public static final String C_SYSTEMPROPERTY_VALUE="SYSTEMPROPERTY_VALUE";
    
    public static final Integer C_SYSTEMPROPERTIES_MAXID_KEY = new Integer(600);	
	public static final String C_SYSTEMPROPERTIES_MAXID = "SELECT MAX(SYSTEMPROPERTY_ID) FROM " + C_DATABASE_PREFIX + "SYSTEMPROPERTIES";

    public static final Integer C_SYSTEMPROPERTIES_READ_KEY = new Integer(601);	
	public static final String C_SYSTEMPROPERTIES_READ = "SELECT * FROM " + C_DATABASE_PREFIX + "SYSTEMPROPERTIES WHERE SYSTEMPROPERTY_NAME = ? ";

    public static final Integer C_SYSTEMPROPERTIES_WRITE_KEY = new Integer(602);
    public static final String C_SYSTEMPROPERTIES_WRITE = "INSERT INTO " + C_DATABASE_PREFIX + "SYSTEMPROPERTIES VALUES(?,?,?)";
    
    public static final Integer C_SYSTEMPROPERTIES_UPDATE_KEY = new Integer(603);
    public static final String C_SYSTEMPROPERTIES_UPDATE="UPDATE " + C_DATABASE_PREFIX + "SYSTEMPROPERTIES SET SYSTEMPROPERTY_VALUE = ? WHERE SYSTEMPROPERTY_NAME = ? ";
     
    public static final Integer C_SYSTEMPROPERTIES_DELETE_KEY = new Integer(604);
	public static final String C_SYSTEMPROPERTIES_DELETE="DELETE FROM " + C_DATABASE_PREFIX + "SYSTEMPROPERTIES WHERE SYSTEMPROPERTY_NAME = ?";
    
	// Constants for PropertyDef table
    public static final String C_PROPERTYDEF_ID = "PROPERTYDEF_ID";
	public static final String C_PROPERTYDEF_NAME = "PROPERTYDEF_NAME";
	public static final String C_PROPERTYDEF_RESOURCE_TYPE = "RESOURCE_TYPE";
	public static final String C_PROPERTYDEF_TYPE = "PROPERTYDEF_TYPE";

	// Constans for PropertyDef
	public static final Integer C_PROPERTYDEF_MAXID_KEY = new Integer(300);	
	public static final String C_PROPERTYDEF_MAXID = "SELECT MAX(PROPERTYDEF_ID) FROM " + C_DATABASE_PREFIX + "PROPERTYDEF";
	
	public static final Integer C_PROPERTYDEF_READ_KEY = new Integer(301);	
	public static final String C_PROPERTYDEF_READ = "Select * from " + C_DATABASE_PREFIX + "PROPERTYDEF where " + 
												C_PROPERTYDEF_NAME + " = ? and " +
												C_PROPERTYDEF_RESOURCE_TYPE + " = ? ";
												 
	public static final Integer C_PROPERTYDEF_READALL_A_KEY = new Integer(302);	
    public static final String C_PROPERTYDEF_READALL_A = "Select * from " + C_DATABASE_PREFIX + "PROPERTYDEF where " + 
												C_PROPERTYDEF_RESOURCE_TYPE + " = ? ";

	public static final Integer C_PROPERTYDEF_READALL_B_KEY = new Integer(303);	
    public static final String C_PROPERTYDEF_READALL_B = "Select * from " + C_DATABASE_PREFIX + "PROPERTYDEF where " + 
												C_PROPERTYDEF_RESOURCE_TYPE + " = ? and " +
												C_PROPERTYDEF_TYPE + " = ? ";
	
	public static final Integer C_PROPERTYDEF_CREATE_KEY = new Integer(304);	
    public static final String C_PROPERTYDEF_CREATE = "INSERT INTO " + C_DATABASE_PREFIX + "PROPERTYDEF VALUES(?,?,?,?)";
 
	public static final Integer C_PROPERTYDEF_DELETE_KEY = new Integer(305);
	public static final String C_PROPERTYDEF_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "PROPERTYDEF WHERE " + 
												C_PROPERTYDEF_ID + " = ? ";
		
    public static final Integer C_PROPERTYDEF_UPDATE_KEY = new Integer(306);
	public static final String C_PROPERTYDEF_UPDATE = "UPDATE " + C_DATABASE_PREFIX + "PROPERTYDEF SET " + 
												C_PROPERTYDEF_TYPE + " = ? WHERE " + 
												C_PROPERTYDEF_ID + " = ? ";
	
	// Constants for properties table
    public static final String C_PROPERTY_VALUE = "PROPERTY_VALUE";
	public static final String C_PROPERTY_RESOURCE_ID = "RESOURCE_ID";

	
	// Constants for properties
	public static final Integer C_PROPERTIES_MAXID_KEY = new Integer(350);	
	public static final String C_PROPERTIES_MAXID = "SELECT MAX(PROPERTY_ID) FROM " + C_DATABASE_PREFIX + "PROPERTIES";
	
	public static final Integer C_PROPERTIES_READALL_COUNT_KEY = new Integer(351);	
	public static final String C_PROPERTIES_READALL_COUNT = "SELECT count(*) FROM " + C_DATABASE_PREFIX + "PROPERTIES WHERE " +
												C_PROPERTYDEF_ID + " = ?";
	
	public static final Integer C_PROPERTIES_READ_KEY = new Integer(352);	
	public static final String C_PROPERTIES_READ = "SELECT " + C_DATABASE_PREFIX + "PROPERTIES.* FROM " + C_DATABASE_PREFIX + "PROPERTIES, " + C_DATABASE_PREFIX + "PROPERTYDEF " + 
												"WHERE " + C_DATABASE_PREFIX + "PROPERTIES.PROPERTYDEF_ID = " + C_DATABASE_PREFIX + "PROPERTYDEF.PROPERTYDEF_ID and " +
												C_DATABASE_PREFIX + "PROPERTIES.RESOURCE_ID = ? and " +
												C_DATABASE_PREFIX + "PROPERTYDEF.PROPERTYDEF_NAME = ? and " +
												C_DATABASE_PREFIX + "PROPERTYDEF.RESOURCE_TYPE = ?";

	public static final Integer C_PROPERTIES_UPDATE_KEY = new Integer(353);	
	public static final String C_PROPERTIES_UPDATE = "UPDATE " + C_DATABASE_PREFIX + "PROPERTIES SET " + 
												C_PROPERTY_VALUE + " = ? WHERE " +
												C_PROPERTY_RESOURCE_ID + " = ? and " +
												C_PROPERTYDEF_ID + " = ? ";
													
	public static final Integer C_PROPERTIES_CREATE_KEY = new Integer(354);	
	public static final String C_PROPERTIES_CREATE = "INSERT INTO " + C_DATABASE_PREFIX + "PROPERTIES VALUES(?,?,?,?)";

	public static final Integer C_PROPERTIES_READALL_KEY = new Integer(355);	
	public static final String C_PROPERTIES_READALL = "SELECT " + C_DATABASE_PREFIX + "PROPERTIES.*, " + C_DATABASE_PREFIX + "PROPERTYDEF.PROPERTYDEF_NAME FROM " + C_DATABASE_PREFIX + "PROPERTIES, " + C_DATABASE_PREFIX + "PROPERTYDEF " + 
												"WHERE " + C_DATABASE_PREFIX + "PROPERTIES.PROPERTYDEF_ID = " + C_DATABASE_PREFIX + "PROPERTYDEF.PROPERTYDEF_ID and " +
												C_DATABASE_PREFIX + "PROPERTIES.RESOURCE_ID = ? and " +
												C_DATABASE_PREFIX + "PROPERTYDEF.RESOURCE_TYPE = ?";

	public static final Integer C_PROPERTIES_DELETEALL_KEY = new Integer(356);	
	public static final String C_PROPERTIES_DELETEALL = "DELETE FROM " + C_DATABASE_PREFIX + "PROPERTIES " + 
												"WHERE RESOURCE_ID = ?";

	public static final Integer C_PROPERTIES_DELETE_KEY = new Integer(357);	
	public static final String C_PROPERTIES_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "PROPERTIES " + 
												"WHERE " + C_PROPERTYDEF_ID + " = ? and " +
												C_PROPERTY_RESOURCE_ID + " = ? ";

	
    // Constants for Projects table
	public static final String C_PROJECTS_PROJECT_ID = "PROJECT_ID";
	public static final String C_PROJECTS_USER_ID = "USER_ID";
	public static final String C_PROJECTS_GROUP_ID = "GROUP_ID";
	public static final String C_PROJECTS_MANAGERGROUP_ID = "MANAGERGROUP_ID";
	public static final String C_PROJECTS_TASK_ID = "TASK_ID";
	public static final String C_PROJECTS_PROJECT_NAME = "PROJECT_NAME";
	public static final String C_PROJECTS_PROJECT_DESCRIPTION = "PROJECT_DESCRIPTION";
	public static final String C_PROJECTS_PROJECT_FLAGS = "PROJECT_FLAGS";
	public static final String C_PROJECTS_PROJECT_CREATEDATE = "PROJECT_CREATEDATE";
	public static final String C_PROJECTS_PROJECT_PUBLISHDATE = "PROJECT_PUBLISHDATE";
	public static final String C_PROJECTS_PROJECT_PUBLISHED_BY = "PROJECT_PUBLISHED_BY";
	public static final String C_PROJECTS_PROJECT_TYPE = "PROJECT_TYPE";
	
	// Constants for Projects	
    public static final Integer C_PROJECTS_MAXID_KEY = new Integer(400);
	public static final String C_PROJECTS_MAXID = "SELECT MAX(PROJECT_ID) FROM " + C_DATABASE_PREFIX + "PROJECTS";
	
	public static final Integer C_PROJECTS_CREATE_KEY = new Integer(401);
	public static final String C_PROJECTS_CREATE = "INSERT INTO " + C_DATABASE_PREFIX + "PROJECTS VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

    // Constants for Users table
	public static final String C_USERS_USER_ID = "USER_ID";
	public static final String C_USERS_USER_NAME = "USER_NAME";
	public static final String C_USERS_USER_PASSWORD = "USER_PASSWORD";
	public static final String C_USERS_USER_DESCRIPTION = "USER_DESCRIPTION";
	public static final String C_USERS_USER_FIRSTNAME = "USER_FIRSTNAME";
	public static final String C_USERS_USER_LASTNAME = "USER_EMAIL";
	public static final String C_USERS_USER_LASTLOGIN = "USER_LASTLOGIN";
	public static final String C_USERS_USER_LASTUSED = "USER_LASTUSED";
	public static final String C_USERS_USER_FLAGS = "USER_FLAGS";
	public static final String C_USERS_USER_INFO = "USER_INFO";
	public static final String C_USERS_USER_DEFAULT_GROUP_ID = "USER_DEFAULT_GROUP_ID";
	public static final String C_USERS_USER_ADDRESS = "USER_ADDRESS";
	public static final String C_USERS_USER_SECTION = "USER_SECTION";
	public static final String C_USERS_USER_TYPE = "USER_TYPE";
	public static final String C_USERS_USER_EMAIL = "USER_EMAIL";
	
	// Constants for Users	
    public static final Integer C_USERS_MAXID_KEY = new Integer(250);
	public static final String C_USERS_MAXID = "SELECT MAX(USER_ID) FROM " + C_DATABASE_PREFIX + "USERS";

    public static final Integer C_USERS_ADD_KEY = new Integer(251);
	public static final String C_USERS_ADD = "INSERT INTO " + C_DATABASE_PREFIX + "USERS VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static final Integer C_USERS_READ_KEY = new Integer(252);
	public static final String C_USERS_READ = "SELECT * FROM " + C_DATABASE_PREFIX + "USERS, " + C_DATABASE_PREFIX + "GROUPS WHERE USER_NAME = ? and USER_TYPE = ? and USER_DEFAULT_GROUP_ID = GROUP_ID";

	public static final Integer C_USERS_READID_KEY = new Integer(253);
	public static final String C_USERS_READID = "SELECT * FROM " + C_DATABASE_PREFIX + "USERS, " + C_DATABASE_PREFIX + "GROUPS WHERE USER_ID = ? and USER_TYPE = ? and USER_DEFAULT_GROUP_ID = GROUP_ID";

	public static final Integer C_USERS_READPW_KEY = new Integer(254);
	public static final String C_USERS_READPW = "SELECT * FROM " + C_DATABASE_PREFIX + "USERS, " + C_DATABASE_PREFIX + "GROUPS WHERE USER_NAME = ? and USER_PASSWORD = ? and USER_TYPE = ? and USER_DEFAULT_GROUP_ID = GROUP_ID";

	public static final Integer C_USERS_WRITE_KEY = new Integer(255);
	public static final String C_USERS_WRITE = "UPDATE " + C_DATABASE_PREFIX + "USERS " + 
											   "SET USER_DESCRIPTION = ?, " +
											   "USER_FIRSTNAME = ?, " +
											   "USER_LASTNAME = ?, " +
											   "USER_EMAIL = ?, " +
											   "USER_LASTLOGIN = ?, " +
											   "USER_LASTUSED = ?, " +
											   "USER_FLAGS = ?, " +
											   "USER_INFO = ?, " +
											   "USER_ADDRESS = ?, " +
											   "USER_SECTION = ?, " +
											   "USER_TYPE = ? " +
											   "WHERE USER_ID = ? ";

	public static final Integer C_USERS_DELETE_KEY = new Integer(256);
	public static final String C_USERS_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "USERS WHERE USER_NAME = ?";

	public static final Integer C_USERS_GETUSERS_KEY = new Integer(257);
	public static final String C_USERS_GETUSERS = "SELECT * FROM " + C_DATABASE_PREFIX + "USERS, " + C_DATABASE_PREFIX + "GROUPS where USER_TYPE = ? and USER_DEFAULT_GROUP_ID = GROUP_ID";

	public static final Integer C_USERS_SETPW_KEY = new Integer(258);
	public static final String C_USERS_SETPW = "UPDATE " + C_DATABASE_PREFIX + "USERS SET USER_PASSWORD = ? WHERE USER_NAME = ? ";
}


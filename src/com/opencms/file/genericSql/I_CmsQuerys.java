/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/I_CmsQuerys.java,v $
 * Date   : $Date: 2000/06/07 09:11:44 $
 * Version: $Revision: 1.9 $
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
 * @version $Revision: 1.9 $ $Date: 2000/06/07 09:11:44 $
 */
public interface I_CmsQuerys {
    
    // Common constants   
	static String C_DATABASE_PREFIX = "CMS_";

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
   
    public static final Integer C_GROUPS_GETALLGROUP_KEY = new Integer(206);
    public static final String C_GROUPS_GETALLGROUP = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS";
    
    
    // Constants for Systemproperties
    public static final String C_SYSTEMPROPERTY_VALUE="SYSTEMPROPERTY_VALUE";
    
    public static final Integer C_SYSTEMPROPERTIES_MAXID_KEY = new Integer(600);	
	public static final String C_SYSTEMPROPERTIES_MAXID = "SELECT MAX(SYSTEMPROPERTY_ID) FROM " + C_DATABASE_PREFIX + "SYSTEMPROPERTIES";

    public static final Integer C_SYSTEMPROPERTIES_READ_KEY = new Integer(601);	
	public static final String C_SYSTEMPROPERTIES_READ = "SELECT * FROM " + C_DATABASE_PREFIX + "SYSTEMPROPERTIES WHERE SYSTEMPROPERTY_NAME = ? ";

    public static final Integer C_SYSTEMPROPERTIES_WRITE_KEY = new Integer(602);
    public static final String C_SYSTEMPROPERTIES_WRITE = "INSERT INTO " + C_DATABASE_PREFIX + "SYSTEMPROPERTIES VALUES(?,?)";
    
    public static final Integer C_SYSTEMPROPERTIES_UPDATE_KEY = new Integer(603);
    public static final String C_SYSTEMPROPERTIES_UPDATE="UPDATE " + C_DATABASE_PREFIX + "SYSTEMPROPERTIES SET SYSTEMPROPERTY_VALUE = ? WHERE SYSTEMPROPERTY_NAME = ? ";
     
    public static final Integer C_SYSTEMPROPERTIES_DELETE_KEY = new Integer(604);
	public static final String C_SYSTEMPROPERTIES_DELETE="DELETE FROM " + C_DATABASE_PREFIX + "SYSTEMPROPERTIES WHERE SYSTEMPROPERTY_NAME = ?";
    
	
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
	
	// Constants for Users	
    public static final Integer C_USERS_MAXID_KEY = new Integer(250);
	public static final String C_USERS_MAXID = "SELECT MAX(USER_ID) FROM " + C_DATABASE_PREFIX + "USERS";

    public static final Integer C_USERS_ADD_KEY = new Integer(251);
	public static final String C_USERS_ADD = "INSERT INTO " + C_DATABASE_PREFIX + "USERS VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
}


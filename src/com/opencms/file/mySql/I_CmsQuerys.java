package com.opencms.file.mySql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/mySql/Attic/I_CmsQuerys.java,v $
 * Date   : $Date: 2000/08/29 09:28:13 $
 * Version: $Revision: 1.9 $
 *
 * Copyright (C) 200C_RESOURCES_GET_FILESINFOLDER_KEY0  The OpenCms Group 
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

import com.opencms.core.*;

/**
 * This interface is defines all queries used in the DB-Access class.  
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.9 $ $Date: 2000/08/29 09:28:13 $
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
	public static final String C_RESOURCES_SIZE="RESOURCE_SIZE";
	public static final String C_RESOURCES_LASTMODIFIED_BY="RESOURCE_LASTMODIFIED_BY";
	public static final String C_RESOURCES_FILE_CONTENT="FILE_CONTENT";
	
	
	// Constants for resources
	public static final Integer C_RESOURCES_MAXID_KEY = new Integer(100);
	public static final String C_RESOURCES_MAXID = "SELECT MAX(RESOURCE_ID) FROM " + C_DATABASE_PREFIX + "RESOURCES";	
	 
	public static final Integer C_RESOURCES_READ_KEY = new Integer(102);
	public static final String C_RESOURCES_READ = "SELECT * FROM " + C_DATABASE_PREFIX + "RESOURCES WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";
  
	public static final Integer C_RESOURCES_WRITE_KEY = new Integer(103);
	public static final String C_RESOURCES_WRITE = "INSERT INTO " + C_DATABASE_PREFIX + "RESOURCES VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static final Integer C_RESOURCES_GET_LOST_ID_KEY = new Integer(104);
	
	// mySQL specific statement (no use of nested select-statements.
	public static final String C_RESOURCES_GET_LOST_ID = "SELECT A.FILE_ID FROM "+C_DATABASE_PREFIX+"FILES A LEFT JOIN "
										+C_DATABASE_PREFIX+"RESOURCES B ON A.FILE_ID=B.FILE_ID WHERE B.FILE_ID is NULL";
	
	public static final Integer C_RESOURCES_DELETE_PROJECT_KEY = new Integer(105);
	public static final String C_RESOURCES_DELETE_PROJECT = "DELETE FROM " + C_DATABASE_PREFIX + "RESOURCES "
															 + "WHERE PROJECT_ID = ?";
   	
	public static final Integer C_RESOURCES_DELETE_KEY = new Integer(106);
	public static final String C_RESOURCES_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "RESOURCES WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";
																 
	public static final Integer C_RESOURCES_UPDATE_KEY = new Integer(107);
	public static final String C_RESOURCES_UPDATE ="UPDATE " + C_DATABASE_PREFIX + "RESOURCES SET "
											   +"RESOURCE_TYPE = ? , "
											   +"RESOURCE_FLAGS = ? , "
											   +"USER_ID = ? , "
											   +"GROUP_ID = ? , "
											   +"PROJECT_ID = ? ,"
											   +"ACCESS_FLAGS = ? ,"
											   +"STATE = ? , "
											   +"LOCKED_BY = ? , "
											   +"LAUNCHER_TYPE = ? , "
											   +"LAUNCHER_CLASSNAME = ? ," 
											   +"DATE_LASTMODIFIED = ? ,"
											   +"RESOURCE_LASTMODIFIED_BY = ? ,"
											   +"RESOURCE_SIZE = ? , "
											   +"FILE_ID = ? "
											   +"WHERE RESOURCE_ID = ?";
	
	public static final Integer C_RESOURCES_ID_DELETE_KEY = new Integer(108);
	public static final String C_RESOURCES_ID_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "RESOURCES WHERE RESOURCE_ID = ?";
	
	public static final Integer C_RESOURCES_GET_SUBFOLDER_KEY = new Integer(109);
	public static final String C_RESOURCES_GET_SUBFOLDER = "SELECT * FROM " + C_DATABASE_PREFIX + "RESOURCES WHERE PARENT_ID = ? AND RESOURCE_TYPE = "
																	+ I_CmsConstants.C_TYPE_FOLDER + " ORDER BY RESOURCE_NAME";
	
	public static final Integer C_RESOURCES_PUBLISH_PROJECT_READFILE_KEY = new Integer(110);
	public static final String C_RESOURCES_PUBLISH_PROJECT_READFILE =  "SELECT " + C_DATABASE_PREFIX + "RESOURCES.RESOURCE_NAME, "
													                +"RESOURCE_ID,PARENT_ID,FILE_ID,"
																	+"RESOURCE_TYPE,"
																	+"RESOURCE_FLAGS,USER_ID,"
																	+"GROUP_ID,ACCESS_FLAGS,STATE,"
																	+"LOCKED_BY,LAUNCHER_TYPE,LAUNCHER_CLASSNAME,"
																	+"DATE_CREATED,DATE_LASTMODIFIED,RESOURCE_LASTMODIFIED_BY,RESOURCE_SIZE "
																	+" FROM " + C_DATABASE_PREFIX + "RESOURCES "
																	+"WHERE " + C_DATABASE_PREFIX + "RESOURCES.PROJECT_ID = ? "
																	+"AND " + C_DATABASE_PREFIX + "RESOURCES.RESOURCE_TYPE <> "+I_CmsConstants.C_TYPE_FOLDER;
		
	public static final Integer C_RESOURCES_PUBLISH_PROJECT_READFOLDER_KEY = new Integer(111);
	public static final String C_RESOURCES_PUBLISH_PROJECT_READFOLDER =  "SELECT " + C_DATABASE_PREFIX + "RESOURCES.RESOURCE_NAME, "
													                  +"RESOURCE_ID,PARENT_ID,FILE_ID,"
																	  +"RESOURCE_TYPE,"
																	  +"RESOURCE_FLAGS,USER_ID,"
																	  +"GROUP_ID,ACCESS_FLAGS,STATE,"
																	  +"LOCKED_BY,LAUNCHER_TYPE,LAUNCHER_CLASSNAME,"
																	  +"DATE_CREATED,DATE_LASTMODIFIED,RESOURCE_LASTMODIFIED_BY,RESOURCE_SIZE "
																	  +" FROM " + C_DATABASE_PREFIX + "RESOURCES "
																	  +"WHERE " + C_DATABASE_PREFIX + "RESOURCES.PROJECT_ID = ? "
																	  +"AND " + C_DATABASE_PREFIX + "RESOURCES.RESOURCE_TYPE <> "+I_CmsConstants.C_TYPE_FOLDER;        
	
	public static final Integer C_RESOURCES_UPDATE_FILE_KEY = new Integer(112);
	public static final String C_RESOURCES_UPDATE_FILE ="UPDATE " + C_DATABASE_PREFIX + "RESOURCES SET "
											   +"RESOURCE_TYPE = ? , "
											   +"RESOURCE_FLAGS = ? , "
											   +"USER_ID = ? , "
											   +"GROUP_ID = ? , "
											   +"PROJECT_ID = ? ,"
											   +"ACCESS_FLAGS = ? ,"
											   +"STATE = ? , "
											   +"LOCKED_BY = ? , "
											   +"LAUNCHER_TYPE = ? , "
											   +"LAUNCHER_CLASSNAME = ? ," 
											   +"DATE_LASTMODIFIED = ? ,"
											   +"RESOURCE_LASTMODIFIED_BY = ? ,"
											   +"RESOURCE_SIZE = ? , "
											   +"FILE_ID = ? "
											   +"WHERE RESOURCE_ID = ?";
	
	public static final Integer C_RESOURCES_UNLOCK_KEY = new Integer(120);
	public static final String C_RESOURCES_UNLOCK = "UPDATE " + C_DATABASE_PREFIX + "RESOURCES SET "
													+"LOCKED_BY = " + I_CmsConstants.C_UNKNOWN_ID
													+"WHERE PROJECT_ID = ?";	

	public static final Integer C_RESOURCES_COUNTLOCKED_KEY = new Integer(121);
	public static final String C_RESOURCES_COUNTLOCKED = "SELECT COUNT(RESOURCE_ID) FROM " + C_DATABASE_PREFIX + "RESOURCES where LOCKED_BY <> " + 
														 I_CmsConstants.C_UNKNOWN_ID + " and PROJECT_ID = ?";
	
	public static final Integer C_RESOURCES_READBYPROJECT_KEY = new Integer(122);
	public static final String C_RESOURCES_READBYPROJECT = "SELECT * FROM " + C_DATABASE_PREFIX + "RESOURCES where PROJECT_ID = ?";

	public static final Integer C_RESOURCES_PUBLISH_MARKED_KEY = new Integer(123);
	public static final String C_RESOURCES_PUBLISH_MARKED = "select OL.*, OFF.* from " + C_DATABASE_PREFIX + "RESOURCES  OL, " + C_DATABASE_PREFIX + "RESOURCES  OFF " + 
															 "where OFF.PROJECT_ID = ? and OL.PROJECT_ID = ? and OFF.RESOURCE_NAME = OL.RESOURCE_NAME and OFF.STATE = ? " +
															 "order by OFF.RESOURCE_NAME";

	public static final Integer C_RESOURCES_DELETEBYID_KEY = new Integer(124);
	public static final String C_RESOURCES_DELETEBYID = "DELETE FROM " + C_DATABASE_PREFIX + "RESOURCES " + 
														"WHERE RESOURCE_ID = ?";

	public static final Integer C_RESOURCES_READFOLDERSBYPROJECT_KEY = new Integer(125);
	public static final String C_RESOURCES_READFOLDERSBYPROJECT = "SELECT * FROM " + C_DATABASE_PREFIX +
						 "RESOURCES where PROJECT_ID = ? AND RESOURCE_TYPE = "+ I_CmsConstants.C_TYPE_FOLDER
						 + " order by RESOURCE_NAME";

	public static final Integer C_RESOURCES_READFILESBYPROJECT_KEY = new Integer(126);
	public static final String C_RESOURCES_READFILESBYPROJECT = "SELECT * FROM " + C_DATABASE_PREFIX +
						 "RESOURCES where PROJECT_ID = ? AND RESOURCE_TYPE <> "+ I_CmsConstants.C_TYPE_FOLDER;

	public static final Integer C_RESOURCES_GET_FILESINFOLDER_KEY = new Integer(130);
	public static final String C_RESOURCES_GET_FILESINFOLDER = "SELECT * FROM " + C_DATABASE_PREFIX + "RESOURCES WHERE PARENT_ID = ? AND RESOURCE_TYPE <> "
																	+ I_CmsConstants.C_TYPE_FOLDER + " ORDER BY RESOURCE_NAME";

	public static final Integer C_RESOURCES_REMOVE_KEY = new Integer(131);
	public static final String C_RESOURCES_REMOVE = "UPDATE " + C_DATABASE_PREFIX + "RESOURCES SET "
												   +"STATE = ?, LOCKED_BY = ?"
												   +"WHERE RESOURCE_NAME = ? AND PROJECT_ID = ?";

	public static final Integer C_RESOURCES_READ_ALL_KEY = new Integer(132);
	public static final String C_RESOURCES_READ_ALL = "SELECT * FROM " + C_DATABASE_PREFIX + "RESOURCES WHERE RESOURCE_NAME = ?";

	public static final Integer C_RESOURCES_READBYID_KEY = new Integer(133);
	public static final String C_RESOURCES_READBYID = "SELECT * FROM " + C_DATABASE_PREFIX + "RESOURCES WHERE RESOURCE_ID = ?";
  
	public static final Integer C_RESOURCES_RENAMERESOURCE_KEY = new Integer(140);
	public static final String C_RESOURCES_RENAMERESOURCE = "UPDATE " + C_DATABASE_PREFIX + "RESOURCES SET "
														   +"RESOURCE_NAME = ? , "
														   +"RESOURCE_LASTMODIFIED_BY = ? "
														   +"WHERE RESOURCE_ID = ?";


	// Constants for files table
	public static final String C_FILE_ID="FILE_ID";
	public static final String C_FILE_CONTENT="FILE_CONTENT";
	
		
	// Constants for files
	public static final Integer C_FILES_MAXID_KEY = new Integer(150);
	public static final String C_FILES_MAXID = "SELECT MAX(FILE_ID) FROM " + C_DATABASE_PREFIX + "FILES";	
	
	public static final Integer C_FILE_DELETE_KEY = new Integer(151);
	public static final String C_FILE_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "FILES WHERE FILE_ID = ?";

	public static final Integer C_FILE_READ_KEY = new Integer(152);
	public static final String C_FILE_READ = "SELECT FILE_CONTENT FROM " + C_DATABASE_PREFIX + "FILES " +"WHERE FILE_ID = ? ";
   
	public static final Integer C_FILE_READ_ONLINE_KEY = new Integer(153);
	public static final String C_FILE_READ_ONLINE = "SELECT RESOURCE_ID,PARENT_ID,"
													 +"RESOURCE_TYPE,"
													 +"RESOURCE_FLAGS,USER_ID,"
													 + "GROUP_ID,"+C_DATABASE_PREFIX+"FILES.FILE_ID,ACCESS_FLAGS,STATE,"
													 +"LOCKED_BY,LAUNCHER_TYPE,LAUNCHER_CLASSNAME,"
													 +"DATE_CREATED,DATE_LASTMODIFIED,RESOURCE_LASTMODIFIED_BY,RESOURCE_SIZE, "
													 + C_DATABASE_PREFIX + "FILES.FILE_CONTENT FROM " + C_DATABASE_PREFIX + "RESOURCES," + C_DATABASE_PREFIX + "FILES "
													 +"WHERE " + C_DATABASE_PREFIX + "RESOURCES.FILE_ID = " + C_DATABASE_PREFIX + "FILES.FILE_ID "
													 +"AND " + C_DATABASE_PREFIX + "RESOURCES.RESOURCE_NAME = ? "
													 +"AND " + C_DATABASE_PREFIX + "RESOURCES.PROJECT_ID = ?";
	
	public static final Integer C_FILES_WRITE_KEY = new Integer(154);
	public static final String C_FILES_WRITE = "INSERT INTO " + C_DATABASE_PREFIX + "FILES VALUES(?,?)";
	
	public static final Integer C_FILES_UPDATE_KEY = new Integer(155);
	public static final String C_FILES_UPDATE ="UPDATE " + C_DATABASE_PREFIX + "FILES SET "
											   +"FILE_CONTENT = ? "                                     
											   +"WHERE FILE_ID = ? ";
	
	
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
	public static final String C_GROUPS_GETGROUPS = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS ORDER BY GROUP_NAME";
  
	public static final Integer C_GROUPS_GETCHILD_KEY = new Integer(207);
	public static final String C_GROUPS_GETCHILD = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS WHERE PARENT_GROUP_ID = ?";
	
	public static final Integer C_GROUPS_GETPARENT_KEY = new Integer(208);
	public static final String C_GROUPS_GETPARENT = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS WHERE GROUP_ID = ?";
	   
	public static final Integer C_GROUPS_GETGROUPSOFUSER_KEY = new Integer(209);
	public static final String C_GROUPS_GETGROUPSOFUSER = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPS G, "
														+ C_DATABASE_PREFIX + "USERS U, "
														+ C_DATABASE_PREFIX + "GROUPUSERS GU  "
														+ "where U.USER_NAME = ? AND U.USER_ID=GU.USER_ID "
														+ "AND GU.GROUP_ID = G.GROUP_ID";
	
	public static final Integer C_GROUPS_ADDUSERTOGROUP_KEY = new Integer(210);
	public static final String C_GROUPS_ADDUSERTOGROUP = "INSERT INTO " + C_DATABASE_PREFIX + "GROUPUSERS VALUES(?,?,?)";

	public static final Integer C_GROUPS_USERINGROUP_KEY = new Integer(211);
	public static final String C_GROUPS_USERINGROUP = "SELECT * FROM " + C_DATABASE_PREFIX + "GROUPUSERS WHERE GROUP_ID = ? AND USER_ID = ?";

	public static final Integer C_GROUPS_GETUSERSOFGROUP_KEY = new Integer(212);
	public static final String C_GROUPS_GETUSERSOFGROUP = "SELECT U.USER_INFO, U.USER_ID, U.USER_NAME,U.USER_PASSWORD, "+
														  "U.USER_RECOVERY_PASSWORD, U.USER_DESCRIPTION, "+
														  "U.USER_FIRSTNAME,U.USER_LASTNAME,U.USER_EMAIL, "+
														  "U.USER_LASTLOGIN,U.USER_LASTUSED,U.USER_FLAGS, " +
														  "U.USER_DEFAULT_GROUP_ID, DG.PARENT_GROUP_ID, "+
														  "DG.GROUP_NAME, DG.GROUP_DESCRIPTION, DG.GROUP_FLAGS, "+
														  "U.USER_ADDRESS, U.USER_SECTION, U.USER_TYPE "+
										    			  " FROM " + C_DATABASE_PREFIX + "GROUPS  G, "
														  + C_DATABASE_PREFIX + "USERS  U, "
														  + C_DATABASE_PREFIX + "GROUPUSERS  GU, "
														  + C_DATABASE_PREFIX + "GROUPS  DG "
														  + "where G.GROUP_NAME = ? AND U.USER_ID=GU.USER_ID "
														  + "AND GU.GROUP_ID = G.GROUP_ID "
														  + "AND U.USER_DEFAULT_GROUP_ID = DG.GROUP_ID "
														  + "AND U.USER_TYPE = ? ORDER BY USER_NAME";

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

	public static final Integer C_PROJECTS_READ_KEY = new Integer(402);
	public static final String C_PROJECTS_READ = "SELECT * from " + C_DATABASE_PREFIX + "PROJECTS where PROJECT_ID = ? ORDER BY PROJECT_NAME";

	public static final Integer C_PROJECTS_READ_BYTASK_KEY = new Integer(403);
	public static final String C_PROJECTS_READ_BYTASK = "Select * from " + C_DATABASE_PREFIX + "PROJECTS where TASK_ID = ? ORDER BY PROJECT_NAME";
	
	public static final Integer C_PROJECTS_READ_BYUSER_KEY = new Integer(404);
	public static final String C_PROJECTS_READ_BYUSER = "Select * from " + C_DATABASE_PREFIX + "PROJECTS where USER_ID = ? and PROJECT_FLAGS = " + I_CmsConstants.C_PROJECT_STATE_UNLOCKED + "  ORDER BY PROJECT_NAME";

	public static final Integer C_PROJECTS_READ_BYGROUP_KEY = new Integer(405);
	public static final String C_PROJECTS_READ_BYGROUP = "Select * from " + C_DATABASE_PREFIX + "PROJECTS where (GROUP_ID = ? or MANAGERGROUP_ID = ? ) and PROJECT_FLAGS = " + I_CmsConstants.C_PROJECT_STATE_UNLOCKED + " ORDER BY PROJECT_NAME";

	public static final Integer C_PROJECTS_READ_BYFLAG_KEY = new Integer(406);
	public static final String C_PROJECTS_READ_BYFLAG = "Select * from " + C_DATABASE_PREFIX + "PROJECTS where PROJECT_FLAGS = ? ORDER BY PROJECT_NAME";
	
	public static final Integer C_PROJECTS_READ_BYMANAGER_KEY = new Integer(407);
	public static final String C_PROJECTS_READ_BYMANAGER = "Select * from " + C_DATABASE_PREFIX + "PROJECTS where MANAGERGROUP_ID = ? and PROJECT_FLAGS = " + I_CmsConstants.C_PROJECT_STATE_UNLOCKED + " ORDER BY PROJECT_NAME";

	public static final Integer C_PROJECTS_DELETE_KEY = new Integer(408);
	public static final String C_PROJECTS_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "PROJECTS where PROJECT_ID = ?";

	public static final Integer C_PROJECTS_WRITE_KEY = new Integer(409);
	public static final String C_PROJECTS_WRITE = "UPDATE " + C_DATABASE_PREFIX + "PROJECTS " + 
												  "SET USER_ID = ?, " +
												  "GROUP_ID = ?, " +
												  "MANAGERGROUP_ID = ?, " +
												  "PROJECT_FLAGS = ?, " +
												  "PROJECT_PUBLISHDATE = ?, " +
												  "PROJECT_PUBLISHED_BY = ? " + 
												  "where PROJECT_ID = ?";

	// Constants for Users table
	public static final String C_USERS_USER_ID = "USER_ID";
	public static final String C_USERS_USER_NAME = "USER_NAME";
	public static final String C_USERS_USER_PASSWORD = "USER_PASSWORD";
	public static final String C_USERS_USER_RECOVERY_PASSWORD = "USER_RECOVERY_PASSWORD";
	public static final String C_USERS_USER_DESCRIPTION = "USER_DESCRIPTION";
	public static final String C_USERS_USER_FIRSTNAME = "USER_FIRSTNAME";
	public static final String C_USERS_USER_LASTNAME = "USER_LASTNAME";
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
	public static final String C_USERS_ADD = "INSERT INTO " + C_DATABASE_PREFIX + "USERS VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static final Integer C_USERS_READ_KEY = new Integer(252);
	public static final String C_USERS_READ = "SELECT * FROM " + C_DATABASE_PREFIX + "USERS, " + C_DATABASE_PREFIX + "GROUPS WHERE USER_NAME = ? and USER_TYPE = ? and USER_DEFAULT_GROUP_ID = GROUP_ID";

	public static final Integer C_USERS_READID_KEY = new Integer(253);
	public static final String C_USERS_READID = "SELECT * FROM " + C_DATABASE_PREFIX + "USERS, " + C_DATABASE_PREFIX + "GROUPS WHERE USER_ID = ?  and USER_DEFAULT_GROUP_ID = GROUP_ID";

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
											   "USER_DEFAULT_GROUP_ID = ?, "+
											   "USER_ADDRESS = ?, " +
											   "USER_SECTION = ?, " +
											   "USER_TYPE = ? " +
											   "WHERE USER_ID = ? ";

	public static final Integer C_USERS_DELETE_KEY = new Integer(256);
	public static final String C_USERS_DELETE = "DELETE FROM " + C_DATABASE_PREFIX + "USERS WHERE USER_NAME = ?";

	public static final Integer C_USERS_GETUSERS_KEY = new Integer(257);
	public static final String C_USERS_GETUSERS = "SELECT * FROM " + C_DATABASE_PREFIX + "USERS, " + C_DATABASE_PREFIX + "GROUPS where USER_TYPE = ? and USER_DEFAULT_GROUP_ID = GROUP_ID ORDER BY USER_NAME";

	public static final Integer C_USERS_SETPW_KEY = new Integer(258);
	public static final String C_USERS_SETPW = "UPDATE " + C_DATABASE_PREFIX + "USERS SET USER_PASSWORD = ? WHERE USER_NAME = ? ";

	public static final Integer C_USERS_SETRECPW_KEY = new Integer(259);
	public static final String C_USERS_SETRECPW = "UPDATE " + C_DATABASE_PREFIX + "USERS SET USER_RECOVERY_PASSWORD = ? WHERE USER_NAME = ? ";
	
	public static final Integer C_USERS_RECOVERPW_KEY = new Integer(260);
	public static final String  C_USERS_RECOVERPW = "UPDATE " + C_DATABASE_PREFIX + "USERS SET USER_PASSWORD = ? WHERE USER_NAME = ? and USER_RECOVERY_PASSWORD = ?";
	
	public static final Integer C_USERS_DELETEBYID_KEY = new Integer(261);
	public static final String C_USERS_DELETEBYID = "DELETE FROM " + C_DATABASE_PREFIX + "USERS WHERE USER_ID = ?";
	
	
	// Constants for Task table
	public static final String C_TABLENAME_TASK     = C_DATABASE_PREFIX + "Task";
	public static final String C_TABLENAME_TASKLOG  = C_DATABASE_PREFIX + "TaskLog";
	public static final String C_TABLENAME_TASKTYPE = C_DATABASE_PREFIX + "TaskType";
	public static final String C_TABLENAME_TASKPAR  = C_DATABASE_PREFIX + "TaskPar";
	public static final String C_ID				  = "id"; 
	public static final String C_TASK_ID			  = "id";
	public static final String C_TASK_NAME			  = "name";
	public static final String C_TASK_STATE		  = "state";
	public static final String C_TASK_ROOT			  = "root";
	public static final String C_TASK_PARENT		  = "parent";
	public static final String C_TASK_TASKTYPE		  = "tasktyperef"; 
	public static final String C_TASK_INITIATORUSER  = "initiatoruserref";
	public static final String C_TASK_ROLE			  = "roleref";
	public static final String C_TASK_AGENTUSER	  = "agentuserref";
	public static final String C_TASK_ORIGINALUSER   = "originaluserref";
	public static final String C_TASK_STARTTIME	  = "starttime";
	public static final String C_TASK_WAKEUPTIME	  = "wakeuptime";
	public static final String C_TASK_TIMEOUT		  = "timeout";
	public static final String C_TASK_ENDTIME		  = "endtime";
	public static final String C_TASK_PERCENTAGE	  = "percentage";
	public static final String C_TASK_PERMISSION	  = "permission";
	public static final String C_TASK_PRIORITY		  = "priorityref"; 
	public static final String C_TASK_ESCALATIONTYPE = "escalationtyperef";
	public static final String C_TASK_HTMLLINK		  = "htmllink"; 
	public static final String C_TASK_MILESTONE	  = "milestoneref";
	public static final String C_TASK_AUTOFINISH	  = "autofinish";
	
	// Constants for TaskLog table
	public static final String C_LOG_ID         = C_TABLENAME_TASKLOG + ".id";
	public static final String C_LOG_COMMENT    = C_TABLENAME_TASKLOG + ".comment"; 
	public static final String C_LOG_EXUSERNAME = C_TABLENAME_TASKLOG + ".externalusername"; 
	public static final String C_LOG_STARTTIME  = C_TABLENAME_TASKLOG + ".starttime"; 
	public static final String C_LOG_TASK		 = C_TABLENAME_TASKLOG + ".taskref";
	public static final String C_LOG_USER		 = C_TABLENAME_TASKLOG + ".userref";
	public static final String C_LOG_TYPE       = C_TABLENAME_TASKLOG + ".type";
	
	// Constants for TaskPar table
	public static final String C_PAR_ID      = "id"; 
	public static final String C_PAR_NAME    = "parname"; 
	public static final String C_PAR_VALUE   = "parvalue";
	public static final String C_PAR_TASK	  = "ref"; 
	
	public static final String C_TASK_TYPE_FIELDS = "autofinish, escalationtyperef, htmllink, name, permission, priorityref, roleref";
	
	public static final Integer C_TASK_TYPE_COPY_KEY = new Integer(801);	
	public static final String C_TASK_TYPE_COPY = "INSERT INTO " +  C_TABLENAME_TASK + "  (id," + C_TASK_TYPE_FIELDS + ") " +
												   "SELECT ?," + C_TASK_TYPE_FIELDS + 
												   " FROM  " + C_TABLENAME_TASKTYPE + "  WHERE id=?";
	
	public static final Integer C_TASK_UPDATE_KEY = new Integer(802);
	public static final String C_TASK_UPDATE ="UPDATE  " + C_TABLENAME_TASK + "  SET " +
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

	public static final Integer C_TASK_READ_KEY = new Integer(803);
	public static final String C_TASK_READ = "SELECT * FROM  " + C_TABLENAME_TASK + "  WHERE id=?";

	public static final Integer C_TASK_END_KEY = new Integer(804);
	public static final String C_TASK_END = "UPDATE  " + C_TABLENAME_TASK + "  Set " +
											 "state=" + CmsDbAccess.C_TASK_STATE_ENDED + ", " +
											 "percentage=?, " +
											 "endtime=? " +
											 "WHERE id=?";
	
	public static final Integer C_TASKLOG_WRITE_KEY = new Integer(805);
	public static final String C_TASKLOG_WRITE = "INSERT INTO " + C_TABLENAME_TASKLOG + " " +
												   "(" + C_LOG_ID + "," + C_LOG_TASK + ", " + C_LOG_USER + "," + C_LOG_STARTTIME + "," + C_LOG_COMMENT + "," + C_LOG_TYPE + ") " +
												   "VALUES (?,?,?,?,?,?)";
	
	public static final Integer C_TASKLOG_READ_KEY = new Integer(806);
	public static final String C_TASKLOG_READ = "SELECT * FROM " + C_TABLENAME_TASKLOG + " WHERE " + C_LOG_ID + "=?";
	
	public static final Integer C_TASK_FIND_AGENT_KEY = new Integer(807);
	public static final String C_TASK_FIND_AGENT = "SELECT * " +
													"FROM "+C_DATABASE_PREFIX+"GROUPUSERS, "+C_DATABASE_PREFIX+"USERS " +
													"WHERE GROUP_ID=? AND "+
													C_DATABASE_PREFIX+"GROUPUSERS.USER_ID="+C_DATABASE_PREFIX+"USERS.USER_ID " +
													"ORDER BY USER_LASTUSED ASC";
	
	public static final Integer C_TASK_FORWARD_KEY = new Integer(808);
	public static final String C_TASK_FORWARD = "UPDATE " + C_TABLENAME_TASK+" SET " + 
												C_TASK_ROLE + "=? ," +
												C_TASK_AGENTUSER + "=? " +
												"WHERE " + C_TASK_ID+"=?";
	
	public static final Integer C_TASKLOG_READ_LOGS_KEY = new Integer(809);
	public static final String C_TASKLOG_READ_LOGS = "SELECT * FROM "+ C_TABLENAME_TASKLOG + 
													 " WHERE " + C_LOG_TASK + "=? "+ 
													 " ORDER BY " + C_LOG_STARTTIME;
	
	public static final Integer C_TASKLOG_READ_PPROJECTLOGS_KEY = new Integer(810);
	public static final String C_TASKLOG_READ_PPROJECTLOGS =  "SELECT " + C_LOG_ID + ","+C_LOG_COMMENT+","+C_LOG_TASK+","+C_LOG_USER+"," + C_LOG_STARTTIME + "," + C_LOG_TYPE + "," + C_LOG_EXUSERNAME + " " +
															  "FROM " + C_TABLENAME_TASKLOG + ", " + C_TABLENAME_TASK + " " +
															  "WHERE " + C_LOG_TASK + "="+C_DATABASE_PREFIX+"Task.id AND "+C_DATABASE_PREFIX+"Task.root=? " + 
															  "ORDER BY " + C_LOG_STARTTIME;

	public static final Integer C_TASKPAR_TEST_KEY = new Integer(811);
	public static final String C_TASKPAR_TEST = "SELECT * FROM " + C_TABLENAME_TASKPAR + " WHERE "+C_PAR_TASK+"=? AND " + C_PAR_NAME + "=?";
	
	public static final Integer C_TASKPAR_UPDATE_KEY = new Integer(812);
	public static final String C_TASKPAR_UPDATE = "UPDATE " + C_TABLENAME_TASKPAR + " SET " + C_PAR_VALUE + "=? WHERE "+C_PAR_ID+"=?";
	
	public static final Integer C_TASKPAR_INSERT_KEY = new Integer(813);
	public static final String C_TASKPAR_INSERT = "INSERT INTO " + C_TABLENAME_TASKPAR + "(" + C_PAR_ID + "," + C_PAR_TASK+"," + C_PAR_NAME + "," + C_PAR_VALUE + ") VALUES (?,?,?,?)";
	
	public static final Integer C_TASKPAR_GET_KEY = new Integer(814);
	public static final String C_TASKPAR_GET = "SELECT * FROM " + C_TABLENAME_TASKPAR + " WHERE " + C_PAR_TASK + "=? AND " + C_PAR_NAME + "=?";
	
	public static final Integer C_TASK_GET_TASKTYPE_KEY = new Integer(815);
	public static final String C_TASK_GET_TASKTYPE = "SELECT id FROM " + C_TABLENAME_TASKTYPE + " where name=?";

	// Constants for systemid table
	public static final String C_SYSTEMID_ID = "ID";

	// Constants for systemid	
	
	public static final Integer C_SYSTEMID_INIT_KEY = new Integer(1);
	public static final String C_SYSTEMID_INIT = "INSERT INTO " + C_DATABASE_PREFIX + "SYSTEMID VALUES(?,1)";
	
	public static final Integer C_SYSTEMID_LOCK_KEY = new Integer(2);
	public static final String C_SYSTEMID_LOCK = "LOCK TABLES " + C_DATABASE_PREFIX + "SYSTEMID WRITE";

	public static final Integer C_SYSTEMID_READ_KEY = new Integer(3);
	public static final String C_SYSTEMID_READ = "SELECT ID FROM " + C_DATABASE_PREFIX + "SYSTEMID WHERE TABLE_KEY=?";

	public static final Integer C_SYSTEMID_WRITE_KEY = new Integer(4);
	public static final String C_SYSTEMID_WRITE = "UPDATE " + C_DATABASE_PREFIX + "SYSTEMID SET ID = ? WHERE TABLE_KEY = ? ";
	
	public static final Integer C_SYSTEMID_UNLOCK_KEY = new Integer(5);
	public static final String C_SYSTEMID_UNLOCK = "UNLOCK TABLES ";
	
	
	// Constants for session-failover
	
	public static final Integer C_SESSION_CREATE_KEY = new Integer(900);
	public static final String C_SESSION_CREATE = "INSERT into " + C_DATABASE_PREFIX + "SESSIONS (SESSION_ID, SESSION_LASTUSED, SESSION_DATA) values(?,?,?)";

	public static final Integer C_SESSION_UPDATE_KEY = new Integer(901);
	public static final String C_SESSION_UPDATE = "UPDATE " + C_DATABASE_PREFIX + "SESSIONS set SESSION_LASTUSED = ?, SESSION_DATA = ? where SESSION_ID = ?";

	public static final Integer C_SESSION_READ_KEY = new Integer(902);
	public static final String C_SESSION_READ = "select SESSION_DATA from " + C_DATABASE_PREFIX + "SESSIONS where SESSION_ID = ? and SESSION_LASTUSED > ?";

	public static final Integer C_SESSION_DELETE_KEY = new Integer(903);
	public static final String C_SESSION_DELETE = "delete from " + C_DATABASE_PREFIX + "SESSIONS where SESSION_LASTUSED < ?";
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/I_CmsConstants.java,v $
 * Date   : $Date: 2000/02/17 17:06:09 $
 * Version: $Revision: 1.40 $
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

package com.opencms.core;

/**
 * This interface is a pool for cms-constants. All classes may implement this
 * class to get access to this contsnats.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Michaela Schleich
 * 
 * @version $Revision: 1.40 $ $Date: 2000/02/17 17:06:09 $
 */
public interface I_CmsConstants
{
	/**
	 * The version-string for the cvs.
	 */
	static String C_VERSION = "Version 4.0a9 Giedi Prime: Rabban";
	
	/**
	 * The copyright message for the cvs.
	 */
	static String C_COPYRIGHT[] = {"",
								   "Copyright (C) 2000 The OpenCms Group",
								   "OpenCms comes with ABSOLUTELY NO WARRANTY",
								   "This is free software, and you are welcome to",
								   "redistribute it under certain conditions.",
								   "Please see the GNU General Public Licence for",
								   "further details.",
								   ""};
	
	/**
	 * The prefix for all database-tables.
	 */
	static String C_DATABASE_PREFIX = "CMS_";

	/**
	 * The minimum-size of a passwordstring.
	 */
	static final int C_PASSWORD_MINIMUMSIZE = 4;
	 
	/**
	 * This flag is set for enabled entrys in the database.
	 * (GROUP_FLAGS for example)
	 */
	 static final int C_FLAG_ENABLED = 0;
	
	/**
	 * This flag is set for disabled entrys in the database.
	 * (GROUP_FLAGS for example)
	 */
	 static final int C_FLAG_DISABLED = 1;	

     /**
      * Path to the workplace ini file
      */     
     static final String C_WORKPLACE_INI = "/system/workplace/config/workplace.ini";
     
	 /**
	  * The last index, that was used for resource-types.
	  */
	 final static String C_TYPE_LAST_INDEX = "lastIndex";
	 
	/**
	 * The resource type-id for a folder.
	 */
	 final static int C_TYPE_FOLDER		= 0;
	
	/**
	 * The resource type-id for a folder.
	 */
	 final static String C_TYPE_FOLDER_NAME		= "folder";

	 /**
	 * The resource type-id for a plain text file.
	 */
	 final static int C_TYPE_TEXTPLAIN		= 1;
	
	/**
	 * The resource type-id for a binary file.
	 */
	 final static int C_TYPE_BINARY		= 2;
	
	/**
	 * The resource type-id for a xml base file.
	 */
	 final static int C_TYPE_XMLBASE		= 3;

	/**
	 * The resource type-id for a xml templatefile.
	 */
	 final static int C_TYPE_XMLTEMPLATE	= 4;
	
	/**
	 * The resource type-id for a docloader file.
	 */
	 final static int C_TYPE_DOCLOADER	= 5;
	 
	/**
	 * The resource type-id for a page file.
	 */ 
	 final static int C_TYPE_PAGE	= 6;
	 
	 /**
	 * The resource type-name for a page file.
	 */
	 final static String C_TYPE_PAGE_NAME		= "page";
	 
								
	/**
	 * This constant signs a normal "classic" metadefinition.
	 */
	static final int C_METADEF_TYPE_NORMAL		= 0;

	/**
	 * This constant signs a optional metadefinition.
	 * These metadefinitions will be shown at creation-time for a resource.
	 * They may be filled out by the user.
	 */
	static final int C_METADEF_TYPE_OPTIONAL	= 1;

	/**
	 * This constant signs a mandatory metadefinition.
	 * These metadefinitions will be shown at creation-time for a resource.
	 * They must be filled out by the user, else the resource will not be created.
	 */
	static final int C_METADEF_TYPE_MANDATORY	= 2;

    /**
     * Metainformation for resource title
     */
    static final String C_METAINFO_TITLE="Title";

     /**
     * Metainformation for resource navigation title
     */
    static final String C_METAINFO_NAVTITLE="NavTitle";
   
    /**
     * Metainformation for resource navigation position
     */
    static final String C_METAINFO_NAVPOS="NavPos";
    
	/**
	 * This is the group for guests.
	 */
	 static final String C_GROUP_GUEST = "Guests";
	
	/**
	 * This is the group for administrators.
	 */
	 static final String C_GROUP_ADMIN = "Administrators";
	
	/**
	 * This is the group for projectleaders. It is the only group, which
	 * can create new projects.
	 */
	 static final String C_GROUP_PROJECTLEADER = "Projectmanager";

	/**
	 * This is the group for users. If you are in this group, you can use
	 * the workplace.
	 */
	 static final String C_GROUP_USERS = "Users";
	 
	/**
	 * This is the group for guests.
	 */
	 static final String C_USER_GUEST = "Guest";
	
	/**
	 * This is the group for administrators.
	 */
	 static final String C_USER_ADMIN = "Admin";

	 /**
	 * Key for additional info emailaddress.
	 */
	 final static String C_ADDITIONAL_INFO_EMAIL		= "USER_EMAIL";

	/**
	 * Key for additional info firstname.
	 */
	 final static String C_ADDITIONAL_INFO_FIRSTNAME	= "USER_FIRSTNAME";

	/**
	 * Key for additional info surname.
	 */
	 final static String C_ADDITIONAL_INFO_LASTNAME	= "USER_LASTNAME";

	/**
	 * Key for additional info address.
	 */
	 final static String C_ADDITIONAL_INFO_ADDRESS	= "USER_ADDRESS";

	/**
	 * Key for additional info section.
	 */
	 final static String C_ADDITIONAL_INFO_SECTION	= "USER_SECTION";
     
     /**
	 * Key for additional info default group id.
	 */
	 final static String C_ADDITIONAL_INFO_DEFAULTGROUP_ID	= "USER_DEFAULTGROUP_ID";
     
     /**
	 * Key for additional info last login.
	 */
	 final static String C_ADDITIONAL_INFO_LASTLOGIN	= "USER_LASTLOGIN";

     /**
	 * Key for additional info flags.
	 */
	 final static String C_ADDITIONAL_INFO_FLAGS	= "USER_FLAGS";
     
     /**
	 * Key for additional info flags.
	 */
	 final static String C_ADDITIONAL_INFO_PREFERENCES	= "USER_PREFERENCES";
     
     
	/**
	 * This constant is used to order the tasks by date.
	 */
	 static final int C_TASK_ORDER_BY_DATE =		1;

	/**
	 * This constant is used to order the tasks by name.
	 */
	 static final int C_TASK_ORDER_BY_NAME =		2;
	
	// TODO: add order-criteria here.

	/**
	 * This constant defines the onlineproject. This is the project which
	 * is used to show the resources for guestusers
	 */
	 static final String C_PROJECT_ONLINE	= "Online";
	
	/**
	 * This constant defines a unlocked project. 
	 * Resources may be changed in this project.
	 */
	 static final int C_PROJECT_STATE_UNLOCKED			= 0;

	/**
	 * This constant defines a locked project.
	 * Resources can't be changed in this project.
	 */
	 static final int C_PROJECT_STATE_LOCKED			= 1;

	/**
	 * This constant defines a project in a archive.
	 * Resources can't be changed in this project. Its state will never
	 * go back to the previos one.
	 */
	 static final int C_PROJECT_STATE_ARCHIVE			= 2;

	/**
	 * This id will be returned for resources with no id.
	 * (filesystem resources)
	 */
	 final static int C_UNKNOWN_ID		= -1;

	/**
	 * Owner may read this resource
	 */
	 final static int C_ACCESS_OWNER_READ		= 1;

	/**
	 * Owner may write this resource
	 */
	 final static int C_ACCESS_OWNER_WRITE		= 2;
	
	/**
	 * Owner may view this resource
	 */
	 final static int C_ACCESS_OWNER_VISIBLE		= 4;
	
	/**
	 * Group may read this resource
	 */	
	 final static int C_ACCESS_GROUP_READ		= 8;
	
	/**
	 * Group may write this resource
	 */
	 final static int C_ACCESS_GROUP_WRITE		= 16;
	
	/**
	 * Group may view this resource
	 */
	 final static int C_ACCESS_GROUP_VISIBLE		= 32;
	
	/**
	 *  may read this resource
	 */	
	 final static int C_ACCESS_PUBLIC_READ		= 64;
	
	/**
	 *  may write this resource
	 */	
	 final static int C_ACCESS_PUBLIC_WRITE		= 128;

	/**
	 *  may view this resource
	 */
	 final static int C_ACCESS_PUBLIC_VISIBLE	= 256;

	/**
	 * This is an internal resource, it can't be launched directly.
	 */
	 final static int C_ACCESS_INTERNAL_READ		= 512;
	
	/**
	 * All may read this resource.
	 */
	 final static int	C_ACCESS_READ				= C_ACCESS_OWNER_READ + C_ACCESS_GROUP_READ + C_ACCESS_PUBLIC_READ;

	/**
	 * All may write this resource.
	 */
	 final static int	C_ACCESS_WRITE				= C_ACCESS_OWNER_WRITE + C_ACCESS_GROUP_WRITE + C_ACCESS_PUBLIC_WRITE;
	
	/**
	 * All may view this resource.
	 */
	 final static int	C_ACCESS_VISIBLE			= C_ACCESS_OWNER_VISIBLE + C_ACCESS_GROUP_VISIBLE + C_ACCESS_PUBLIC_VISIBLE;
	
	/**
	 * Owner has full access to this resource.
	 */
	 final static int C_ACCESS_OWNER				= C_ACCESS_OWNER_READ + C_ACCESS_OWNER_WRITE + C_ACCESS_OWNER_VISIBLE;

	/**
	 * Group has full access to this resource.
	 */
	 final static int C_ACCESS_GROUP				= C_ACCESS_GROUP_READ + C_ACCESS_GROUP_WRITE + C_ACCESS_GROUP_VISIBLE;

	/**
	 *  has full access to this resource.
	 */
	 final static int C_ACCESS_PUBLIC			= C_ACCESS_PUBLIC_READ + C_ACCESS_PUBLIC_WRITE + C_ACCESS_PUBLIC_VISIBLE;
	
	/**
	 * The default-flags for a new resource.
	 */
	 final static int C_ACCESS_DEFAULT_FLAGS		= C_ACCESS_OWNER_READ + C_ACCESS_OWNER_WRITE + C_ACCESS_OWNER_VISIBLE + C_ACCESS_GROUP_READ + C_ACCESS_GROUP_WRITE + C_ACCESS_GROUP_VISIBLE + C_ACCESS_PUBLIC_READ + C_ACCESS_PUBLIC_VISIBLE;
	
	/**
	 * Is set, if the resource is unchanged in this project.
	 */
	 static final int C_STATE_UNCHANGED	= 0;

	/**
	 * Is set, if the resource was changed in this project.
	 */
	 static final int C_STATE_CHANGED		= 1;
	
	/**
	 * Is set, if the resource is new in this project.
	 */	
	 static final int C_STATE_NEW			= 2;
	
	/**
	 * Is set, if the resource was deleted in this project.
	 */	
	 static final int C_STATE_DELETED		= 3;
     
     /**
      * This value will be returned for int's withaout a value.
      */
     static final int C_UNKNOWN_INT         = -1;
     
     /**
      * This value will be returned for long's withaout a value.
      */
     static final int C_UNKNOWN_LONG        = -1;  
     
     /**
      * This is the id for an undefined launcher.
      */
     static final int C_UNKNOWN_LAUNCHER_ID  = -1;

      /**
      * This is the classname for an undefined launcher.
      */
     static final String C_UNKNOWN_LAUNCHER  = "UNKNOWN";
     
     /**
      * This is the defintion for a filesystem mountpoint.
      */
     static final int C_MOUNTPOINT_FILESYSTEM=1;

      /**
      * This is the defintion for a database mountpoint.
      */
     static final int C_MOUNTPOINT_MYSQL=2;
     
	 /**
	  * The folder - seberator in this system
	  */
     static final String C_FOLDER_SEPERATOR = "/";
	 
     /**
      * The name of the rood folder
      */
     static final String C_ROOT = C_FOLDER_SEPERATOR;
	 
	 /**
	  * The name of the mountpoint-property.
	  */
	 static final String C_PROPERTY_MOUNTPOINT = "MOUNTPOINT";
    
      /**
	  * The name of the mimetypes-property.
	  */
	 static final String C_PROPERTY_MIMETYPES = "MIMETYPES";
     
	 /**
	  * The name of the resourcetype-property.
	  */
	 static final String C_PROPERTY_RESOURCE_TYPE = "RESOURCE_TYPE";
	 
    /**
     * The key for the username in the user information hashtable.
     */
    static final String C_SESSION_USERNAME="USERNAME";
    
    /**
     * The key for the current usergroup the user information hashtable.
     */
    static final String C_SESSION_CURRENTGROUP="CURRENTGROUP";    
    
     /**
     * The key for the project in the user information hashtable.
     */
    static final String C_SESSION_PROJECT="PROJECT";
     
    /** Identifier for request type http */
    public static final int C_REQUEST_HTTP = 0;
    
    /** Identifier for request type console */
    public static final int C_REQUEST_CONSOLE = 1;
    
    
    /** Identifier for request type http */
    public static final int C_RESPONSE_HTTP = 0;
    
    /** Identifier for request type console */
    public static final int C_RESPONSE_CONSOLE = 1;

	/** Task type value of getting all tasks  */
	public static final int C_TASKS_ALL    = 1;
	
	/** Task type value of getting new tasks  */
	public static final int C_TASKS_NEW    = 2;
	
	/** Task type value of getting open tasks  */
	public static final int C_TASKS_OPEN   = 3;
	
	/** Task type value of getting active tasks  */
	public static final int C_TASKS_ACTIVE = 4;
	
	/** Task type value of getting done tasks  */
	public static final int C_TASKS_DONE   = 5;
	
	/** Task order value */
	public static final String C_ORDER_ID			  = "id";
	
	/** Task order value */
	public static final String C_ORDER_NAME			  = "name";
	
	/** Task order value */
	public static final String C_ORDER_STATE		  = "state";
	
	/** Task order value */
	public static final String C_ORDER_TASKTYPE		  = "tasktyperef"; 
	
	/** Task order value */
	public static final String C_ORDER_INITIATORUSER  = "initiatoruserref";
	
	/** Task order value */
	public static final String C_ORDER_ROLE			  = "roleref";
	
	/** Task order value */
	public static final String C_ORDER_AGENTUSER	  = "agentuserref";
	
	/** Task order value */
	public static final String C_ORDER_ORIGINALUSER   = "originaluserref";
	
	/** Task order value */
	public static final String C_ORDER_STARTTIME	  = "starttime";
	
	/** Task order value */
	public static final String C_ORDER_WAKEUPTIME	  = "wakeuptime";
	
	/** Task order value */
	public static final String C_ORDER_TIMEOUT		  = "timeout";
	
	/** Task order value */
	public static final String C_ORDER_ENDTIME		  = "endtime";
	
	/** Task order value */
	public static final String C_ORDER_PERCENTAGE	  = "percentage";
	
	/** Task order value */
	public static final String C_ORDER_PRIORITY		  = "priorityref"; 
	
	/** Task sort value ascending  */
	public static final String C_SORT_ASC = "ASC";
	
	/** Task sort value descending */
	public static final String C_SORT_DESC = "DESC";
	
	/** Task priority high */
	public static final int C_TASK_PRIORITY_HIGH   = 1;
	
	/** Task priority normal */
	public static final int C_TASK_PRIORITY_NORMAL = 2;
	
	/** Task priority low */
	public static final int C_TASK_PRIORITY_LOW    = 3;
	
	/** Value for order tasks by none */
	public static final int C_TASKORDER_NONE      = 0;
	
	/** Value for order tasks by startdate */
	public static final int C_TASKORDER_STARTDATE = 1;
	
	/** Value for order tasks by timeout */
	public static final int C_TASKORDER_TIMEOUT   = 2;
	
	/** Value for order tasks by taskname */
	public static final int C_TASKSORDER_TASKNAME = 3;
		
	
	/** state values of a task prepared to start*/
	public static final int C_TASK_STATE_PREPARE  = 0;
	
	/** state values of a task ready to start */
	public static final int C_TASK_STATE_START	  = 1;
	
	/** state values of a task started */
	public static final int C_TASK_STATE_STARTED  = 2;
	
	/** state values of a task ready to end */
	public static final int C_TASK_STATE_NOTENDED = 3;
	
	/** state values of a task ended */
	public static final int C_TASK_STATE_ENDED	  = 4;
	
	/** state values of a task halted */
	public static final int C_TASK_STATE_HALTED   = 5;

	/**System type values for the task log */
	public static final int C_TASKLOG_SYSTEM = 0;
	
	/**User type value for the task log */
	public static final int C_TASKLOG_USER   = 1;	
	

	/**
	 * Values for the database import and export
	 */
	
	/** Export type value - exports users and resources */
	final static int C_EXPORTUSERSFILES=0;
	/**Export type value - exports only users */
	final static int C_EXPORTONLYUSERS=1;
	/**Export type value - exports only resources */
	final static int C_EXPORTONLYFILES=2;
	
	/**Are files imported - NO */
	final static int C_NO_FILES_IMPORTED=0;
	/**Are files imported - yes */
	final static int C_FILES_IMPORTED=1;
	
	/** 
	 * root element in the XML file (the document node)
	 * needed, to insert other elements
	 */
	final static String C_FELEMENT = "CMS_EXPORT";
	/** first XML element tag for the resources */
	final static String C_TFILES = "FILES";
	/** XML tag to defines one resource */
	final static String C_TFILEOBJ = "FILEOBJ";
	/** XML tag to defines the resource name */
	final static String C_TFNAME = "NAME";
	/** XML tag to defines the resource type */
	final static String C_TFTYPE = "TYPE";
	/** XML tag to defines the resource typename */
	final static String C_TFTYPENAME = "TYPENAME";
    /** XML tag to defines user acces */
	final static String C_TFUSER = "USER";
    /** XML tag to defines group acces */
	final static String C_TFGROUP = "GROUP";
    /** XML tag to defines file acces */
	final static String C_TFACCESS = "ACCESFLAG";
	
	/** XML tag to defines the resource metainfo */
	final static String C_TFMETAINFO = "METAINFO";
	/** XML tag to defines the resource metaname */
	final static String C_TFMETANAME = "METANAME";
	/** XML tag to defines the resource metatype */
	final static String C_TFMETATYPE = "METATYP";
	/** XML tag to defines the resource metavalue */
	final static String C_TFMETAVALUE = "METAVALUE";
	/** XML tag to defines the resource content if resource is a file */
	final static String C_FCONTENT = "CONTENT";	
	
	/** first XML element tag for the groups */
	final static String C_TGROUPS = "GROUPS";
	/** XML tag to defines one group */
	final static String C_TGROUPOBJ = "GROUPOBJ";
	/** XML tag to defines the group name */
	final static String C_TGNAME = "NAME";
	/** XML tag to defines the parentgroup name */
	final static String C_TGPARENTGROUP = "PARENTGROUP";	
	/** XML tag to defines the description of group */
	final static String C_TGDESC = "DESC";	
	/** XML tag to defines the flag of group */
	final static String C_TGFLAG = "FLAG";
	/** XML tag to defines all users of the group */
	final static String C_TGROUPUSERS = "GROUPUSERS";
	/** XML tag to defines the name of  user in group */
	final static String C_TGUSER = "USER";
	
	/** first XML element tag for the users */
	final static String C_TUSERS = "USERS";
	/** XML tag to defines one user */
	final static String C_TUSEROBJ = "USEROBJ";
	/** XML tag to defines the user login */
	final static String C_TULOGIN = "LOGIN";
	/** XML tag to defines the user PASSWD default "Kennwort" */
	final static String C_TUPASSWD = "PASSWD";
	/** XML tag to defines the user Lastname */
	final static String C_TUNAME = "NAME";	
	/** XML tag to defines the user Firstname */
	final static String C_TUFIRSTNAME = "FIRSTNAME";	
	/** XML tag to defines the user Description */
	final static String C_TUDESC = "DESC";
	/** XML tag to defines the user EMail */
	final static String C_TUEMAIL = "EMAIL";
	/** XML tag to defines the user defaultgroup */
	final static String C_TUDGROUP = "DEFAULTGROUP";	
	/** XML tag to defines if the user is disabled */
	final static String C_TUDISABLED = "DISABLED";
	/** XML tag to defines the user flag */
	final static String C_TUFLAG = "FLAG";
	/** XML tag to defines the groups in which the user is in */
	final static String C_TUSERGROUPS = "USERGROUPS";
	/** XML tag to defines the user group name */
	final static String C_TUGROUP = "GROUP";
	/** XML tag to defines additional user info */
	final static String C_TUADDINFO = "ADDINFO";
	/** XML tag to defines additional user info key */
	final static String C_TUINFOKEY = "INFOKEY";
	/** XML tag to defines additional user info value */
	final static String C_TUINFOVALUE = "INFOVALUE";
}


/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/I_CmsConstants.java,v $
* Date   : $Date: 2004/01/07 11:10:46 $
* Version: $Revision: 1.277 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.core;

import org.opencms.security.CmsPermissionSet;

/**
 * This interface is a pool for constants in OpenCms.<p>
 * 
 * Other classes may implement this class to get easy access to this constants.
 *
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Michaela Schleich
 * @author Thomas Weckert
 *
 * @version $Revision: 1.277 $ $Date: 2004/01/07 11:10:46 $
 */
public interface I_CmsConstants {

    /**
     * The copyright message for OpenCms.
     */
    String C_COPYRIGHT[] = {
            "", 
            "Copyright (c) 2002-2004 Alkacon Software", 
            "Copyright (c) 2000-2002 The OpenCms Group", 
            "OpenCms comes with ABSOLUTELY NO WARRANTY", 
            "This is free software, and you are welcome to", 
            "redistribute it under certain conditions.", 
            "Please see the GNU Lesser General Public Licence for", 
            "further details.", 
            "" 
    };

    /**
     * The maximum length of a resource name (incl. path).
     */
    int C_MAX_LENGTH_RESOURCE_NAME = 240;

    /**
     * This flag is set for enabled entrys in the database.
     * (GROUP_FLAGS for example)
     */
    int C_FLAG_ENABLED = 0;

    /**
     * This flag is set for disabled entrys in the database.
     * (GROUP_FLAGS for example)
     */
    int C_FLAG_DISABLED = 1;

    /**
     * Flag constant: Projectmanager
     * flag for groups
     */
    int C_FLAG_GROUP_PROJECTMANAGER = 2;

    /**
     * Flag constant: ProjectCoWorker
     * flag for groups
     */
    int C_FLAG_GROUP_PROJECTCOWORKER = 4;

    /**
     * Flag constant: Role (for coworkers)
     * flag for groups
     */
    int C_FLAG_GROUP_ROLE = 8;

    /** prefix for temporary files */
    String C_TEMP_PREFIX = "~";

    /** Property for the resource title */
    String C_PROPERTY_TITLE = "Title";

    /** Property for the navigation title */
    String C_PROPERTY_NAVTEXT = "NavText";

    /** Property for the navigation position */
    String C_PROPERTY_NAVPOS = "NavPos";

    /** Property for the keywords */
    String C_PROPERTY_KEYWORDS = "Keywords";

    /** Property for the description */
    String C_PROPERTY_DESCRIPTION = "Description";

    /** Property for the channel id */
    String C_PROPERTY_CHANNELID = "ChannelId";

    /** Property for the visible method in the administration view */
    String C_PROPERTY_VISIBLE = "visiblemethod";

    /** Property for the active method in the administration view */
    String C_PROPERTY_ACTIV = "activemethod";

    /** Property for internal use (e.g. delete) */
    String C_PROPERTY_INTERNAL = "internal";

    /** Property for the static export */
    String C_PROPERTY_EXPORT = "export";

    /** Property for the resource export name, during export this name is used instead of the resource name */
    String C_PROPERTY_EXPORTNAME = "exportname";

    /** Property for the default file in folders */
    String C_PROPERTY_DEFAULT_FILE = "default-file";

    /** Property for the relative root link substitution */
    String C_PROPERTY_RELATIVEROOT = "relativeroot";

    /** Property to control the template */
    String C_PROPERTY_TEMPLATE = "template";

    /** Property for the content encoding */
    String C_PROPERTY_CONTENT_ENCODING = "content-encoding";
    
    /**
     * A user-type
     */
    int C_USER_TYPE_SYSTEMUSER = 0;

    /**
     * A user-type
     */
    int C_USER_TYPE_WEBUSER = 2;

    /**
     * Key for additional info address.
     */
    String C_ADDITIONAL_INFO_ZIPCODE = "USER_ZIPCODE";

    /**
     * Key for additional info address.
     */
    String C_ADDITIONAL_INFO_TOWN = "USER_TOWN";

    /**
     * Key for additional info flags.
     */
    String C_ADDITIONAL_INFO_PREFERENCES = "USER_PREFERENCES";

    /** Key for additional info explorer settings. */
    String C_ADDITIONAL_INFO_EXPLORERSETTINGS = "USER_EXPLORERSETTINGS";

    /** Key for additional info task settings. */
    String C_ADDITIONAL_INFO_TASKSETTINGS = "USER_TASKSETTINGS";

    /** Key for additional info start settings. */
    String C_ADDITIONAL_INFO_STARTSETTINGS = "USER_STARTSETTINGS";

    /**
     * This constant is used to order the tasks by date.
     */
    int C_TASK_ORDER_BY_DATE = 1;

    /**
     * This constant is used to order the tasks by name.
     */
    int C_TASK_ORDER_BY_NAME = 2;

    /**
     * This constant defines the onlineproject. This is the project which
     * is used to show the resources for guestusers
     */
    String C_PROJECT_ONLINE = "Online";

    /**
     * This constant defines the onlineproject. This is the project which
     * is used to show the resources for guestusers
     */
    int C_PROJECT_ONLINE_ID = 1;

    /**
     * This constant defines a normal project-type.
     */
    int C_PROJECT_TYPE_NORMAL = 0;

    /**
     * This constant defines a temporary project-type.
     * The project will be deleted after publishing
     */
    int C_PROJECT_TYPE_TEMPORARY = 1;

    /**
     * This constant defines a direct-publish project.<p>
     */
    int C_PROJECT_TYPE_DIRECT_PUBLISH = 3;

    /**
     * This constant defines a unlocked project.
     * Resources may be changed in this project.
     */
    int C_PROJECT_STATE_UNLOCKED = 0;

    /**
     * This constant defines a locked project.
     * Resources can't be changed in this project.
     */
    int C_PROJECT_STATE_LOCKED = 1;

    /**
     * This constant defines a project in a archive.
     * Resources can't be changed in this project. Its state will never
     * go back to the previos one.
     */
    int C_PROJECT_STATE_ARCHIVE = 2;

    /**
     * This constant defines a project that is invisible.
     * The project is invisible for users. It is needed
     * for creating and editing temporary files
     */
    int C_PROJECT_STATE_INVISIBLE = 3;

    /**
     * This id will be returned for resources with no id.
     * (filesystem resources)
     */
    int C_UNKNOWN_ID = -1;

    /**
     * The permission to read a resource
     */
    int C_PERMISSION_READ = 1;

    /**
     * The permission to write a resource
     */
    int C_PERMISSION_WRITE = 2;

    /**
     * The permission to view a resource
     */
    int C_PERMISSION_VIEW = 4;

    /**
     * The permission to control a resource
     */
    int C_PERMISSION_CONTROL = 8;

    /**
     * Permission set to check read acces
     */
    CmsPermissionSet C_READ_ACCESS = new CmsPermissionSet(C_PERMISSION_READ);

    /**
     * Permission set to check write access
     */
    CmsPermissionSet C_WRITE_ACCESS = new CmsPermissionSet(C_PERMISSION_WRITE);

    /**
     * Permission set to check view access
     */
    CmsPermissionSet C_VIEW_ACCESS = new CmsPermissionSet(C_PERMISSION_VIEW);

    /**
     * Permission set to check control access
     */
    CmsPermissionSet C_CONTROL_ACCESS = new CmsPermissionSet(C_PERMISSION_CONTROL);

    /**
     * Permission set to check read and/or view access
     */
    CmsPermissionSet C_READ_OR_VIEW_ACCESS = new CmsPermissionSet(C_PERMISSION_READ | C_PERMISSION_VIEW);

    /**
     * Group may read this resource
     */
    int C_ACCESS_GROUP_READ = 8;

    /**
     * Group may write this resource
     */
    int C_ACCESS_GROUP_WRITE = 16;

    /**
     * Group may view this resource
     */
    int C_ACCESS_GROUP_VISIBLE = 32;

    /**
     *  may read this resource
     */
    int C_ACCESS_PUBLIC_READ = 64;

    /**
     *  may write this resource
     */
    int C_ACCESS_PUBLIC_WRITE = 128;

    /**
     *  may view this resource
     */
    int C_ACCESS_PUBLIC_VISIBLE = 256;

    /**
     * This is an internal resource, it can't be accessed directly.
     */
    int C_ACCESS_INTERNAL_READ = 512;

    /**
     * All may read this resource.
     */
    int C_ACCESS_READ = C_PERMISSION_READ + C_ACCESS_PUBLIC_READ;
    
    /**
     * All may write this resource.
     */
    int C_ACCESS_WRITE = C_PERMISSION_WRITE + C_ACCESS_GROUP_WRITE + C_ACCESS_PUBLIC_WRITE;

    /**
     * All may view this resource.
     */
    int C_ACCESS_VISIBLE = C_PERMISSION_VIEW + C_ACCESS_GROUP_VISIBLE + C_ACCESS_PUBLIC_VISIBLE;

    /**
     * Owner has full access to this resource.
     */
    int C_ACCESS_OWNER = C_PERMISSION_READ + C_PERMISSION_WRITE + C_PERMISSION_VIEW;

    /**
     * Group has full access to this resource.
     */
    int C_ACCESS_GROUP = C_ACCESS_GROUP_WRITE + C_ACCESS_GROUP_VISIBLE;

    /**
     *  has full access to this resource.
     */
    int C_ACCESS_PUBLIC = C_ACCESS_PUBLIC_READ + C_ACCESS_PUBLIC_WRITE + C_ACCESS_PUBLIC_VISIBLE;

    /**
     * The default-flags for a new resource.
     */
    int C_ACCESS_DEFAULT_FLAGS = C_PERMISSION_READ + C_PERMISSION_WRITE + C_PERMISSION_VIEW + C_ACCESS_GROUP_WRITE + C_ACCESS_GROUP_VISIBLE + C_ACCESS_PUBLIC_READ + C_ACCESS_PUBLIC_VISIBLE;
    // + C_ACCESS_GROUP_READ

    /**
     * Flag to indicate that an access control entry is currently deleted
     */
    int C_ACCESSFLAGS_DELETED = 1;

    /**
     * Flag to indicate that an access control entry should be inherited
     */
    int C_ACCESSFLAGS_INHERIT = 2;

    /*
     * Flag to indicate that an access control entry overwrites inherited entries 
     */
    int C_ACCESSFLAGS_OVERWRITE = 4;

    /**
     * Flag to indicate that an access control entry was inherited (read only)
     */
    int C_ACCESSFLAGS_INHERITED = 8;

    /**
     * Flag to signal the principal type user
     */
    int C_ACCESSFLAGS_USER = 16;

    /**
     * Flag to signal the pricipal type group
     */
    int C_ACCESSFLAGS_GROUP = 32;

    /**
     * Is set, if the resource is unchanged in this project.
     * WARNING: Don't change the state values, they are used to calculate the importance !
     */
    int C_STATE_UNCHANGED = 0;

    /**
     * Is set, if the resource was changed in this project.
     * WARNING: Don't change the state values, they are used to calculate the importance !
     */
    int C_STATE_CHANGED = 1;

    /**
     * Is set, if the resource is new in this project.
     * WARNING: Don't change the state values, they are used to calculate the importance !
     */
    int C_STATE_NEW = 2;

    /**
     * Is set, if the resource was deleted in this project.
     * WARNING: Don't change the state values, they are used to calculate the importance !
     */
    int C_STATE_DELETED = 3;

    /**
     * This value will be returned for int's withaout a value.
     */
    int C_UNKNOWN_INT = -1;

    /**
     * This value will be returned for long's withaout a value.
     */
    int C_UNKNOWN_LONG = -1;

    /**
     * This is the defintion for a filesystem mountpoint.
     */
    int C_MOUNTPOINT_FILESYSTEM = 1;

    /**
     * This is the defintion for a database mountpoint.
     */
    int C_MOUNTPOINT_MYSQL = 2;

    /**
     * A string in the configuration-file.
     */
    String C_EXPORTPOINT = "exportpoint.";

    /**
     * A string in the configuration-file.
     */
    String C_EXPORTPOINT_PATH = "exportpoint.path.";

    /**
     * The folder - seberator in this system
     */
    String C_FOLDER_SEPARATOR = "/";

    /**
     * The name of the rood folder
     */
    String C_ROOT = C_FOLDER_SEPARATOR;

    /**
     * The name of the exportpath-systemproperty.
     */
    String C_SYSTEMPROPERTY_CRONTABLE = "CRONTABLE";

    /**
     * The name of the exportpath-systemproperty.
     */
    String C_SYSTEMPROPERTY_PACKAGEPATH = "PACKAGEPATH";

    /**
     * The name of the mountpoint-systemproperty.
     */
    String C_SYSTEMPROPERTY_MOUNTPOINT = "MOUNTPOINT";

    /**
     * The name of the mimetypes-systemproperty.
     */
    String C_SYSTEMPROPERTY_MIMETYPES = "MIMETYPES";

    /**
     * The name of the resourcetype-systemproperty.
     */
    String C_SYSTEMPROPERTY_RESOURCE_TYPE = "RESOURCE_TYPE";

    /**
     * The name of the resourcetype-extension.
     */
    String C_SYSTEMPROPERTY_EXTENSIONS = "EXTENSIONS";

    /**
     * The name of the linkchecktable-systemproperty.
     */
    String C_SYSTEMPROPERTY_LINKCHECKTABLE = "LINKCHECKTABLE";

    /**
     * The key for the username in the user information hashtable.
     */
    String C_SESSION_USERNAME = "USERNAME";

    /**
     * The key for the current user group in the user information hashtable.
     */
    String C_SESSION_CURRENTGROUP = "CURRENTGROUP";

    /**
     * The key for the project in the user information hashtable.
     */
    String C_SESSION_PROJECT = "PROJECT";

    /**
     * The key for the current site in the user information hashtable.
     */
    String C_SESSION_CURRENTSITE = "CURRENTSITE";

    /**
     * The key for the dirty-flag in the session.
     */
    String C_SESSION_IS_DIRTY = "_core_session_is_dirty_";

    /**
     * The key for the original session to store the session data.
     */
    String C_SESSION_DATA = "_session_data_";

    /**
     * The key for the session to store Broadcast messages.
     */
    String C_SESSION_BROADCASTMESSAGE = "BROADCASTMESSAGE";

    /**
     * The key for the session to store, if a message is pending for this user.
     */
    String C_SESSION_MESSAGEPENDING = "BROADCASTMESSAGE_PENDING";

    /**
     * Session key for storing the possition in the administration navigation
     */
    String C_SESSION_ADMIN_POS = "adminposition";

    /**
     * Session key for storing a possible error while executing a thread
     */
    String C_SESSION_THREAD_ERROR = "threaderror";

    /**
     * Session key for storing the files Vector for moduleimport.
     */
    String C_SESSION_MODULE_VECTOR = "modulevector";

    /**
     * Session key for storing the current charcter encoding to be used in HTTP
     * requests and responses.
     */
    // Encoding project:
    String C_SESSION_CONTENT_ENCODING = "content-encoding";
    
    /** Identifier for request type http */
    int C_REQUEST_HTTP = 0;

    /** Identifier for request type console */
    int C_REQUEST_CONSOLE = 1;

    /** Identifier for request type http */
    int C_RESPONSE_HTTP = 0;

    /** Identifier for request type console */
    int C_RESPONSE_CONSOLE = 1;

    /** Task type value of getting all tasks  */
    int C_TASKS_ALL = 1;

    /** Task type value of getting new tasks  */
    int C_TASKS_NEW = 2;

    /** Task type value of getting open tasks  */
    int C_TASKS_OPEN = 3;

    /** Task type value of getting active tasks  */
    int C_TASKS_ACTIVE = 4;

    /** Task type value of getting done tasks  */
    int C_TASKS_DONE = 5;

    /** Task order value */
    String C_ORDER_ID = "id";

    /** Task order value */
    String C_ORDER_NAME = "name";

    /** Task order value */
    String C_ORDER_STATE = "state";

    /** Task order value */
    String C_ORDER_TASKTYPE = "tasktyperef";

    /** Task order value */
    String C_ORDER_INITIATORUSER = "initiatoruserref";

    /** Task order value */
    String C_ORDER_ROLE = "roleref";

    /** Task order value */
    String C_ORDER_AGENTUSER = "agentuserref";

    /** Task order value */
    String C_ORDER_ORIGINALUSER = "originaluserref";

    /** Task order value */
    String C_ORDER_STARTTIME = "starttime";

    /** Task order value */
    String C_ORDER_WAKEUPTIME = "wakeuptime";

    /** Task order value */
    String C_ORDER_TIMEOUT = "timeout";

    /** Task order value */
    String C_ORDER_ENDTIME = "endtime";

    /** Task order value */
    String C_ORDER_PERCENTAGE = "percentage";

    /** Task order value */
    String C_ORDER_PRIORITY = "priorityref";

    /** Task priority high */
    int C_TASK_PRIORITY_HIGH = 1;

    /** Task priority normal */
    int C_TASK_PRIORITY_NORMAL = 2;

    /** Task priority low */
    int C_TASK_PRIORITY_LOW = 3;

    /** Value for order tasks by none */
    int C_TASKORDER_NONE = 0;

    /** Value for order tasks by startdate */
    int C_TASKORDER_STARTDATE = 1;

    /** Value for order tasks by timeout */
    int C_TASKORDER_TIMEOUT = 2;

    /** Value for order tasks by taskname */
    int C_TASKSORDER_TASKNAME = 3;

    /** state values of a task prepared to start*/
    int C_TASK_STATE_PREPARE = 0;

    /** state values of a task ready to start */
    int C_TASK_STATE_START = 1;

    /** state values of a task started */
    int C_TASK_STATE_STARTED = 2;

    /** state values of a task ready to end */
    int C_TASK_STATE_NOTENDED = 3;

    /** state values of a task ended */
    int C_TASK_STATE_ENDED = 4;

    /** state values of a task halted */
    int C_TASK_STATE_HALTED = 5;

    /**System type values for the task log */
    int C_TASKLOG_SYSTEM = 0;

    /**User type value for the task log */
    int C_TASKLOG_USER = 1;

    /** state values of task messages when accepted */
    int C_TASK_MESSAGES_ACCEPTED = 1;

    /** state values of task messages when forwared */
    int C_TASK_MESSAGES_FORWARDED = 2;

    /** state values of task messages when completed */
    int C_TASK_MESSAGES_COMPLETED = 4;

    /** state values of task messages when members */
    int C_TASK_MESSAGES_MEMBERS = 8;

    /** Task preferenses filter */
    String C_TASK_FILTER = "task.filter.";

    /** Task preferenses view all */
    String C_TASK_VIEW_ALL = "TaskViewAll";

    /** Task preferenses message flags */
    String C_TASK_MESSAGES = "TaskMessages";

    /** Start preferenses Language */
    String C_START_LANGUAGE = "StartLanguage";

    /** Start preferenses Project */
    String C_START_PROJECT = "StartProject";

    /** Start preferenses View */
    String C_START_VIEW = "StartView";

    /** Start preferenses DefaultGroup */
    String C_START_DEFAULTGROUP = "StartDefaultGroup";

    /** Start preferenses lock dialog */
    String C_START_LOCKDIALOG = "StartLockDialog";
    
    /** Start preferenses lock dialog */
    String C_START_UPLOADAPPLET = "StartUploadApplet";
    

    /** Start preferenses AccessFlags */
    String C_START_ACCESSFLAGS = "StartAccessFlags";

    /** Template element name used for the canonical root template */
    String C_ROOT_TEMPLATE_NAME = "root";

    // Constants for import/export

    /** 
     * The filename of the xml manifest.
     */
    String C_EXPORT_XMLFILENAME = "manifest.xml";

    /**
     * The version of the opencms export (appears in the export manifest-file).
     */
    String C_EXPORT_VERSION = "4"; 

    /**
     * A tag in the export manifest-file.
     */
    String C_EXPORT_TAG_INFO = "info";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    String C_EXPORT_TAG_CREATOR = "creator";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    String C_EXPORT_TAG_OC_VERSION = "opencms_version";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    String C_EXPORT_TAG_DATE = "createdate";

    /**
     * A tag in the manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    String C_EXPORT_TAG_PROJECT = "project";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    String C_EXPORT_TAG_VERSION = "export_version";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_FILE = "file";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_SOURCE = "source";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_DESTINATION = "destination";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_TYPE = "type";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_USER = "user";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_GROUP = "group";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_ACCESS = "access";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_PROPERTY = "property";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_NAME = "name";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_VALUE = "value";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_EXPORT = "export";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_MODULEXPORT = "modulexport";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_PROPERTIES = "properties";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_LOADER_START_CLASS = "startclass";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_USERGROUPDATA = "usergroupdata";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_USERDATA = "userdata";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_GROUPDATA = "groupdata";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_DESCRIPTION = "description";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_FLAGS = "flags";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_PARENTGROUP = "parentgroup";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_PASSWORD = "password";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_RECOVERYPASSWORD = "recoverypassword";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_FIRSTNAME = "firstname";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_LASTNAME = "lastname";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_EMAIL = "email";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_DEFAULTGROUP = "defaultgroup";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_ADDRESS = "address";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_SECTION = "section";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_USERINFO = "userinfo";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_USERGROUPS = "usergroups";

    /**
     * A tag in the manifest-file.
     */
    String C_EXPORT_TAG_GROUPNAME = "groupname";

    /**
     * The "lastmodified" tag in the manifest-file.
     */
    String C_EXPORT_TAG_LASTMODIFIED = "lastmodified";

    /**
     * The "uuid" tag in the manifest-file.
     */
    String C_EXPORT_TAG_UUIDSTRUCTURE = "uuidstructure";
    
    /**
     * The "uuidfile" tag in the manifest-file.
     */
    String C_EXPORT_TAG_UUIDCONTENT = "uuidcontent";
    
    /**
     * The "uuidresource" tag in the manifest-file.
     */
    String C_EXPORT_TAG_UUIDRESOURCE = "uuidresource";
    
    /**
     * The "datelastmodified" tag in the manifest-file.
     */
    String C_EXPORT_TAG_DATELASTMODIFIED = "datelastmodified";

    /**
     * The "userlastmodified" tag in the manifest-file.
     */
    String C_EXPORT_TAG_USERLASTMODIFIED = "userlastmodified";
    
    /**
     * The "datecreated" tag in the manifest-file.
     */
    String C_EXPORT_TAG_DATECREATED = "datecreated";

    /**
     * The "usercreated" tag in the manifest-file.
     */
    String C_EXPORT_TAG_USERCREATED = "usercreated";

    /**
     * The "link" tag in the manifest-file.
     */
    String C_EXPORT_TAG_LINK = "link";


    /**
     * Tag to identify a generic id
     */
    String C_EXPORT_TAG_ID = "id";

    /**
     * Tag to identify accesscontrolentries 
     */
    String C_EXPORT_TAG_ACCESSCONTROL_ENTRIES = "accesscontrol";

    /**
     * Tag to identify a single access control entry
     */
    String C_EXPORT_TAG_ACCESSCONTROL_ENTRY = "accessentry";


    /**
     * Tag to identify a principal set
     */
    String C_EXPORT_TAG_ACCESSCONTROL_PRINCIPAL = "uuidprincipal";


    /**
     * Tag to identify a permission set
     */
    String C_EXPORT_TAG_ACCESSCONTROL_PERMISSIONSET = "permissionset";


    /**
     * Tag to identify allowed permissions
     */
    String C_EXPORT_TAG_ACCESSCONTROL_ALLOWEDPERMISSIONS = "allowed";

    /**
     * Tag to identify denied permissions
     */
    String C_EXPORT_TAG_ACCESSCONTROL_DENIEDPERMISSIONS = "denied";
    
    /** prefix for ace principal group */
    String C_EXPORT_ACEPRINCIPAL_GROUP="GROUP.";

    /** prefix for ace principal user */
    String C_EXPORT_ACEPRINCIPAL_USER="USER.";

    /**
     * A string in the configuration-file.
     */
    String C_CONFIGURATION_DB = "db";

    /**
     * 
     */
    String C_CONFIGURATION_VFS = "driver.vfs";
    
    /**
     * 
     */
    String C_CONFIGURATION_PROJECT = "driver.project";
    
    /**
     * 
     */
    String C_CONFIGURATION_USER = "driver.user";
    
    /**
     * 
     */
    String C_CONFIGURATION_WORKFLOW = "driver.workflow";
    
    /**
     * 
     */
    String C_CONFIGURATION_BACKUP = "driver.backup";
    
    /**
     * A string in the configuration-file.
     */
    String C_CONFIGURATION_CACHE = "cache";

    /**
     * Prefix for history/backup config keys.
     */
    String C_CONFIGURATION_HISTORY = "history";

    /**
     * A string in the configuration-file.
     */
    String C_CONFIGURATION_REGISTRY = "registry";

    /**
     * wasLoggedIn
     */
    int C_NEVER = 1;

    /**
     * wasLoggedIn
     */
    int C_AT_LEAST_ONCE = 2;

    /**
     * wasLoggedIn
     */
    int C_WHATEVER = 3;

    /**
     * The name of the synchronizationpath-tag in registry.
     */
    String C_SYNCHRONISATION_PATH = "syncpath";

    /**
     * The name of the synchronizationproject-tag in registry.
     */
    String C_SYNCHRONISATION_PROJECT = "syncproject";

    /**
     * The name of the synchronizationresource-tag in registry.
     */
    String C_SYNCHRONISATION_RESOURCE = "syncresource";

    /**
     * The name of the synchronizationresource-tag in registry.
     */
    String C_SYNCHRONISATION_RESOURCETAG = "res";

    /**
     * The name of the exportpoint source tag in registry.
     */
    String C_REGISTRY_SOURCE = "source";

    /**
     * The name of the exportpoint destination tag in registry.
     */
    String C_REGISTRY_DESTINATION = "destination";

    /**
     * The name of the error tag seperator in backoffice templates
     */
    String C_ERRSPERATOR = "_";

    /**
    * The name of the error tag prefix in backoffice templates
    */
    String C_ERRPREFIX = "err";
    
    /**
     * The vfs path of the sites master folder
     */
    String VFS_FOLDER_SITES = "/sites";

    /**
     * The vfs path of the default site
     */
    String VFS_FOLDER_DEFAULT_SITE = VFS_FOLDER_SITES + "/default";
    
    /**
     * The vfs path of the cos channel folders
     */    
    String VFS_FOLDER_COS = "/channels";
    
    /**
     * The vfs path of the system folder
     */
    String VFS_FOLDER_SYSTEM = "/system";

    /**
     * The name of the entry for the id generator to create new channelid's
     */
    String C_TABLE_CHANNELID = "CORE_CHANNEL_ID";

    /**
     * The modus the cmsObject runs in. There are three diffrent: online, offline
     * and export. It is used by the link replacement mechanism. (the extern mode is
     * used for the names in the filesystem for the exported files.)
     */
    int C_MODUS_AUTO = -1;
    int C_MODUS_ONLINE = 0;
    int C_MODUS_OFFLINE = 1;
    int C_MODUS_EXPORT = 2;
    int C_MODUS_EXTERN = 3;

    /**
     * The attribute of the publishclass tag in the modules registry used to show
     * that this method needs the vector of the changed links for publishing.
     * (i.e. the search module)
     */
    String C_PUBLISH_METHOD_LINK = "linkpublish";

    /**
     * The key for the date of the last linkcheck in the linkchecktable
     */
    String C_LINKCHECKTABLE_DATE = "linkcheckdate";

    /**
     * The name of the tag in registry for history properties.
     */
    String C_REGISTRY_HISTORY = "history";

    /**
     * The name of the tag in registry if versions of the history should be deleted 
     */
    String C_REGISTRY_HISTORY_DELETE = "deleteversions";

    /**
     * The name of the tag in registry for the number of weeks the versions should remain in the history.
     */
    String C_REGISTRY_HISTORY_WEEKS = "weeks";

    /**
     * The name of the tag in registry if history is enabled.
     */
    String C_REGISTRY_HISTORY_ENABLE = "enabled";
    
    /**
     * The name of the tag in registry storign the max number of versions
     */
    String C_REGISTRY_HISTORY_VERSIONS = "versions";
    

    /**
     * The module property key name to specifiy additional resources which are
     * part of a module outside of {system/modules}.
     */
    String C_MODULE_PROPERTY_ADDITIONAL_RESOURCES = "additionalresources";

    /**
     * Character to separate additional resources specified in the module properties.
     */
    String C_MODULE_PROPERTY_ADDITIONAL_RESOURCES_SEPARATOR = ";";

    /** name of the special body element from an XMLTemplate */
    String C_XML_BODY_ELEMENT = "body";

    /** Type ID of a hard VFS link.<p> */
    int C_VFS_LINK_TYPE_MASTER = 1;

    /** Type ID of a soft VFS link.<p> */
    int C_VFS_LINK_TYPE_SLAVE = 2;

    /** suffix for caching of simple pages */
    String C_XML_CONTROL_FILE_SUFFIX = ".xmlcontrol";

    /** default class for templates */
    String C_XML_CONTROL_DEFAULT_CLASS = "com.opencms.template.CmsXmlTemplate";
    
    /** 
     * Signals that VFS links pointing to this resource should not be 
     * deleted and will remain unchanged at all.
     */
    int C_DELETE_OPTION_IGNORE_VFS_LINKS = 0;
    
    /** 
     * Signals that VFS links pointing to this resource should not be deleted. 
     * An existing soft link of this resource will be converted into a new hard link. 
     */
    int C_DELETE_OPTION_PRESERVE_VFS_LINKS = 1;

    /** 
     * Signals that VFS links pointing to this resource should be deleted.
     */
    int C_DELETE_OPTION_DELETE_VFS_LINKS = 2;

    /** copy mode for copy resources as new resource */
    int C_COPY_AS_NEW = 1;

    /** copy mode for copy resources as link */
    int C_COPY_AS_LINK = 2;

    /** copy mode to preserve links during copy */
    int C_COPY_PRESERVE_LINK = 3;

    /** the vfs path of the loast and found folder */
    String C_VFS_LOST_AND_FOUND = "/system/lost-found";

    /** the resource is marked as internal */
    int C_RESOURCEFLAG_INTERNAL = 1;

    /** the resource is linked inside a site folder specified in the OpenCms properties  */
    int C_RESOURCEFLAG_LABELLINK = 2;

    /** identifier for x-forwarded-for (i.e. proxied) request headers */
    String C_REQUEST_FORWARDED_FOR = "x-forwarded-for";
    
    /** localhost ip used in fallback cases */
    String C_IP_LOCALHOST = "127.0.0.1";
    
    /** mode for reading project resources vorm the db */
    int C_READMODE_IGNORESTATE = 0;
    
    /** mode for reading project resources vorm the db */
    int C_READMODE_MATCHSTATE = 1;
    
    /** mode for reading project resources vorm the db */
    int C_READMODE_UNMATCHSTATE = 2;
}
